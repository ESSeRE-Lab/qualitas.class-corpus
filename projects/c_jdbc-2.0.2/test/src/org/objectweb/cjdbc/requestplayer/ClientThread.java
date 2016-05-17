/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: c-jdbc@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 *
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Julie Marguerite.
 */

package org.objectweb.cjdbc.requestplayer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.objectweb.cjdbc.common.util.Stats;

/**
 * C-JDBC client emulator worker thread. Reads SQL requests in a file and
 * forwards them to the cache. If the cache returns no reply, this class
 * forwards the request to the database. Then it returns the reply and updates
 * the cache if needed.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:julie.marguerite@inria.fr">Julie Marguerite </a>
 * @version 1.0
 */
public class ClientThread extends Thread
{
  /** Debug on standard output. */
  private static final boolean DEBUG              = false;

  /** Number of read requests. */
  private Stats                selectStats        = null;

  /** Number of unknown requests. */
  private Stats                unknownStats       = null;

  /** Number of update requests. */
  private Stats                updateStats        = null;

  /** Number of insert requests. */
  private Stats                insertStats        = null;

  /** Number of delete requests. */
  private Stats                deleteStats        = null;

  /** Number of transaction begin. */
  private Stats                beginStats         = null;

  /** Number of transaction commit. */
  private Stats                commitStats        = null;

  /** Number of transaction rollback. */
  private Stats                rollbackStats      = null;

  /** Statistics about get connection from driver */
  private Stats                getConnectionStats = null;

  /** Statistics about closing a connection */
  private Stats                closeStats         = null;

  /** Statistics about getting request from the log file */
  private Stats                getRequestStats    = null;

  private Connection           conn               = null;

  private ClientEmulator       father;
  private int                  threadId;

  /** Type of connection management: standard, fixed or pooling */
  private int                  connectionType;

  /**
   * Creates a new <code>ClientThread</code> instance.
   * 
   * @param threadId thread id
   * @param father father client emulator
   * @param connectionType connection type
   */
  public ClientThread(int threadId, ClientEmulator father, int connectionType)
  {
    super("ClientThread" + threadId);

    // Init the pointers to the stats
    selectStats = father.getSelectStats();
    unknownStats = father.getUnknownStats();
    updateStats = father.getUpdateStats();
    insertStats = father.getInsertStats();
    deleteStats = father.getDeleteStats();
    beginStats = father.getBeginStats();
    commitStats = father.getCommitStats();
    rollbackStats = father.getRollbackStats();
    getRequestStats = father.getGetRequestStats();
    getConnectionStats = father.getGetConnectionStats();
    closeStats = father.getCloseStats();

    this.father = father;
    this.threadId = threadId;
    this.connectionType = connectionType;

    if (this.connectionType == RequestPlayerProperties.FIXED_CONNECTION)
    {
      // Get a new connection to the virtual database
      conn = father.getConnection();
    }
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    String request = null;
    int tid = 0; // current transaction id

    if (DEBUG)
      System.out.println(threadId + ": Starting");

    // Get SQL requests from the trace file and send them to the DB
    while (true)
    {
      long startg = System.currentTimeMillis();
      request = father.parallelGetNextSQLRequest(tid);
      long endg = System.currentTimeMillis();
      getRequestStats.incrementCount();
      getRequestStats.updateTime(endg - startg);

      if (request == null)
      { // Could be the end of file or a transaction whose
        // commit/rollback was never logged in the trace file
        if (tid != 0)
        {
          System.out.println(threadId
              + ": Warning! Rollbacking unterminated transaction " + tid);
          request = "R";
        }
        else
          break; // No more requests
      }

      try
      {
        switch (request.charAt(0))
        {
          case 'B' :
            // Begin
            if (DEBUG)
              System.out.println(threadId + ": " + request);
            long startb = System.currentTimeMillis();
            if (connectionType != RequestPlayerProperties.FIXED_CONNECTION)
            {
              // Get a new connection to the virtual database
              conn = getConnection();
            }
            conn.setAutoCommit(false);
            long endb = System.currentTimeMillis();
            beginStats.incrementCount();
            beginStats.updateTime(endb - startb);
            tid = new Integer(request.substring(2)).intValue();
            break;
          case 'C' :
            // Commit
            if (DEBUG)
              System.out.println(threadId + ": " + request);
            long startc = System.currentTimeMillis();
            conn.commit();
            long endc = System.currentTimeMillis();
            commitStats.incrementCount();
            commitStats.updateTime(endc - startc);
            tid = 0;
            if (connectionType != RequestPlayerProperties.FIXED_CONNECTION)
            { // Close the connection
              closeConnection();
            }
            break;
          case 'R' :
            // Rollback
            if (DEBUG)
              System.out.println(threadId + ": " + request);
            long startr = System.currentTimeMillis();
            conn.rollback();
            long endr = System.currentTimeMillis();
            rollbackStats.incrementCount();
            rollbackStats.updateTime(endr - startr);
            tid = 0;
            if (connectionType != RequestPlayerProperties.FIXED_CONNECTION)
            { // Close the connection
              closeConnection();
            }
            break;
          case 'S' :
            // Select
            if (tid == 0
                && (connectionType != RequestPlayerProperties.FIXED_CONNECTION))
            {
              // Get a new connection to the virtual database
              conn = getConnection();
              // Execute the request
              execReadRequest(request);
              // Close the connection
              closeConnection();
            }
            else
            {
              execReadRequest(request);
            }
            break;
          case 'W' :
            // Write
            if (tid == 0
                && (connectionType != RequestPlayerProperties.FIXED_CONNECTION))
            {
              // Get a new connection to the virtual database
              conn = getConnection();
              // Execute the request
              execWriteRequest(request);
              // Close the connection
              closeConnection();
            }
            else
            {
              execWriteRequest(request);
            }

            break;
          default :
            System.err.println(threadId + ": Error! Unsupported request "
                + request);
            break;
        }
      }
      catch (Exception e)
      {
        System.err.println(threadId
            + ": An error occured while executing SQL request ("
            + e.getMessage() + ")");
        if (request.charAt(0) != 'S' && request.charAt(0) != 'W')
        { // Reset the tid for begin/commit/rollback
          if (tid != 0)
          {
            try
            {
              conn.rollback();
            }
            catch (Exception ignore)
            {
            }
            father.ignoreTid(tid);
            tid = 0;
          }
        }
        if (connectionType != RequestPlayerProperties.FIXED_CONNECTION)
          // Close the connection
          closeConnection();
      }
    }

    if (connectionType == RequestPlayerProperties.FIXED_CONNECTION)
      father.closeConnection(conn);

    //    if (DEBUG)
    System.out.println(threadId + ": Ending.");
  }

  /**
   * Executes a write request.
   * 
   * @param req request to execute
   */
  private void execWriteRequest(String req)
  {
    Statement stmt = null;

    String request = req.substring(2);
    if (DEBUG)
      System.out.println(threadId + ": " + request.substring(0, 5));
    long startw = System.currentTimeMillis();
    try
    {
      stmt = conn.createStatement();
      stmt.setQueryTimeout(father.getTimeout());
      stmt.executeUpdate(request);
      stmt.close();
    }
    catch (SQLException e)
    {
      if ((request.charAt(0) == 'i') || (request.charAt(0) == 'I')) // insert
      {
        insertStats.incrementError();
      }
      else if ((request.charAt(0) == 'u') || (request.charAt(0) == 'U')) // update
      {
        updateStats.incrementError();
      }
      else if ((request.charAt(0) == 'd') || (request.charAt(0) == 'D')) // delete
      {
        deleteStats.incrementError();
      }
      else
      {
        unknownStats.incrementError();
      }
      System.err.println(threadId + ": Failed to execute request: " + request
          + "(" + e + ")");
      return;
    }
    long endw = System.currentTimeMillis();
    if ((request.charAt(0) == 'i') || (request.charAt(0) == 'I')) // insert
    {
      insertStats.incrementCount();
      insertStats.updateTime(endw - startw);
    }
    else if ((request.charAt(0) == 'u') || (request.charAt(0) == 'U')) // update
    {
      updateStats.incrementCount();
      updateStats.updateTime(endw - startw);
    }
    else if ((request.charAt(0) == 'd') || (request.charAt(0) == 'D')) // delete
    {
      deleteStats.incrementCount();
      deleteStats.updateTime(endw - startw);
    }
    else
    {
      unknownStats.incrementCount();
      unknownStats.updateTime(endw - startw);
    }
  }

  /**
   * Executes a select request.
   * 
   * @param req request to execute
   */
  private void execReadRequest(String req)
  {
    Statement stmt = null;
    ResultSet dbReply = null; // The reply from the database
    String request = req.substring(2);
    if (DEBUG)
      System.out.println(threadId + ": " + request.substring(0, 5));
    long startr = System.currentTimeMillis();
    try
    {
      stmt = conn.createStatement();
      stmt.setQueryTimeout(father.getTimeout());
      dbReply = stmt.executeQuery(request);
      // Parse the result if any
      if (dbReply != null)
        dbReply.next(); // Fetch only the first row
      stmt.close();
    }
    catch (SQLException e)
    {
      selectStats.incrementError();
      System.err.println(threadId + ": Failed to execute request: " + request
          + "(" + e + ")");
    }
    long endr = System.currentTimeMillis();
    selectStats.incrementCount();
    selectStats.updateTime(endr - startr);
  }

  /**
   * Closes the connection to the database.
   */
  private void closeConnection()
  {
    long start = System.currentTimeMillis();
    if (connectionType == RequestPlayerProperties.STANDARD_CONNECTION)
    {
      father.closeConnection(conn);
    }
    else if (connectionType == RequestPlayerProperties.POOLING_CONNECTION)
    {
      father.releaseConnectionToPool(conn);
    }
    long end = System.currentTimeMillis();
    closeStats.incrementCount();
    closeStats.updateTime(end - start);
  }

  /**
   * Gets a new connection to the database.
   * 
   * @return Connection
   */
  private Connection getConnection()
  {
    Connection c = null;
    long start = System.currentTimeMillis();
    if (connectionType == RequestPlayerProperties.STANDARD_CONNECTION)
    {
      c = father.getConnection();
    }
    else if (connectionType == RequestPlayerProperties.POOLING_CONNECTION)
    {
      c = father.getConnectionFromPool();
    }
    long end = System.currentTimeMillis();
    getConnectionStats.incrementCount();
    getConnectionStats.updateTime(end - start);
    return c;
  }
}