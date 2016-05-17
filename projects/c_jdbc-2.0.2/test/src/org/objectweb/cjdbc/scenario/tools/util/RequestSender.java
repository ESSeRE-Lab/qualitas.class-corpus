/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.tools.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class defines a RequestSender. This will send request in the background.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class RequestSender implements Runnable
{
  boolean              quit;
  Random               rand;
  ArrayList            exceptions;
  Connection           con;
  long                 runtime;
  QueryGenerator       queryGenerator;

  int                  loopInThread;
  int                  doWriteEvery;
  int                  queryLoop;
  int                  maxIdValue;
  int                  maxResponseTime;
  int                  requestInterval;
  boolean              useTransactions;
  int                  commitIntervalMax;
  boolean              usePreparedStatement;
  boolean              monitorSpeed;

  // monitoring
  int                  requestCount           = 0;
  long                 average                = 0;

  // request generator
  boolean              useQueryGenerator;

  static final int     LOOP_IN_THREAD         = 10;
  static final int     DO_WRITE_EVERY         = 5;
  static final int     MAIN_THREAD_QUERY_LOOP = 5;
  static final int     MAX_ID_VALUE           = 49;
  static final int     MAX_RESPONSE_TIME      = 2000;
  static final int     TIME_BETWEEN_REQUEST   = 100;

  static final boolean USE_TRANSACTION        = true;
  static final int     DO_COMMIT_RAND_NUMBER  = 10;
  static final boolean USE_PREPARED_STATEMENT = true;
  static final boolean MONITOR_SPEED          = true;
  static final boolean USE_QUERY_GENERATOR    = false;

  /**
   * Creates a new <code>RequestSender</code> object with standard settings
   * 
   * @param con the connection to the database
   */
  public RequestSender(Connection con)
  {
    this.con = con;
    loopInThread = LOOP_IN_THREAD;
    doWriteEvery = DO_WRITE_EVERY;
    queryLoop = MAIN_THREAD_QUERY_LOOP;
    maxIdValue = MAX_ID_VALUE;
    maxResponseTime = MAX_RESPONSE_TIME;
    requestInterval = TIME_BETWEEN_REQUEST;
    useTransactions = USE_TRANSACTION;
    commitIntervalMax = DO_COMMIT_RAND_NUMBER;
    usePreparedStatement = USE_PREPARED_STATEMENT;
    monitorSpeed = MONITOR_SPEED;
    useQueryGenerator = USE_QUERY_GENERATOR;

    quit = false;
    rand = new Random(System.currentTimeMillis());
    exceptions = new ArrayList();
  }

  /**
   * @param monitorSpeed The monitorSpeed to set.
   */
  public void setMonitorSpeed(boolean monitorSpeed)
  {
    this.monitorSpeed = monitorSpeed;
  }

  private String getSelectStatement(boolean preparedStatement, int id)
  {
    if (preparedStatement)
      return "Select * from DOCUMENT where id=?";
    else
      return "Select * from DOCUMENT where id=" + id;
  }

  private String getUpdateStatement(boolean preparedStatement, int addressid,
      int id)
  {
    if (preparedStatement)
      return "update DOCUMENT set ADDRESSID=? where id=?";
    else
      return "update DOCUMENT set ADDRESSID=" + addressid + " where id=" + id;
  }

  public QueryGenerator getQueryGenerator() throws SQLException
  {
    //if (queryGenerator == null)
    //  queryGenerator = new QueryGenerator(con);
    return queryGenerator;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      Statement pread = null, pwrite = null;

      if (usePreparedStatement)
      {
        pread = con.prepareStatement(getSelectStatement(true, 0));
        pwrite = con.prepareStatement(getUpdateStatement(true, 0, 0));
      }
      else
      {
        pread = con.createStatement();
        pwrite = con.createStatement();
      }
      int commitInterval = 1 + rand.nextInt(commitIntervalMax);
      long start = System.currentTimeMillis();
      while (!quit)
      {
        for (int i = 0; i < loopInThread; i++)
        {
          requestCount++;
          if (requestInterval > 0)
          {
            synchronized (this)
            {
              wait(requestInterval);
            }
          }
          if (useTransactions)
            con.setAutoCommit(false);
          try
          {
            long t1 = System.currentTimeMillis();

            if (useQueryGenerator)
            {
              pread.execute(getQueryGenerator().generateQuery());
            }
            else
            {

              if (doWriteEvery != -1 && i % doWriteEvery == 0)
              {
                int id = rand.nextInt(maxIdValue);
                int addressid = rand.nextInt();
                // write
                if (usePreparedStatement)
                {
                  ((PreparedStatement) pwrite).setInt(1, id);
                  ((PreparedStatement) pwrite).setInt(2, addressid);
                  ((PreparedStatement) pwrite).executeUpdate();
                }
                else
                {
                  pwrite
                      .executeUpdate(getUpdateStatement(false, addressid, id));
                }
                //int updated = pwrite.executeUpdate();
                //System.out.println("Updated=" + updated);
              }
              else
              {
                int id = rand.nextInt(maxIdValue);
                if (usePreparedStatement)
                {
                  // read
                  ((PreparedStatement) pread).setInt(1, id);
                  ((PreparedStatement) pread).executeQuery();
                }
                else
                {
                  pread.executeQuery(getSelectStatement(false, id));
                }
              }
            }

            if (useTransactions && (i % commitInterval == 0))
              con.commit();

            long t2 = System.currentTimeMillis();
            long diff = t2 - t1;
            if (monitorSpeed)
            {
              average = (t2 - start) / requestCount;
              //System.out.println("REQUEST TIME:"+diff+"ms");
              //System.out.println("Request("+requestCount+"):" + average + "
              // ms");
            }
            if (diff > maxResponseTime)
              throw new Exception("Response time to slow for client thread("
                  + diff + ")");
          }
          catch (Exception e)
          { // this catch exceptions in the loop
            exceptions.add(e);
            e.printStackTrace();
          }
        }
      }
      // Commit last transaction
      if (useTransactions)
        con.setAutoCommit(false);

      // Compute runtime
      long end = System.currentTimeMillis();
      runtime = end - start;
    }
    catch (Exception e)
    {// this catch exception while creating connection and statements
      exceptions.add(e);
      e.printStackTrace();
    }
    finally
    {
      try
      {
        this.con.close();
      }
      catch (SQLException e1)
      {
        e1.printStackTrace();
      }
    }
  }

  /**
   * Returns the quit value.
   * 
   * @return Returns the quit.
   */
  public boolean isQuit()
  {
    return quit;
  }

  /**
   * Sets the quit value.
   * 
   * @param quit The quit to set.
   */
  public void setQuit(boolean quit)
  {
    this.quit = quit;
  }

  /**
   * Returns the exceptions value.
   * 
   * @return Returns the exceptions.
   */
  public ArrayList getExceptions()
  {
    return exceptions;
  }

  /**
   * Sets the commitIntervalMax value.
   * 
   * @param commitIntervalMax The commitIntervalMax to set.
   */
  public void setCommitIntervalMax(int commitIntervalMax)
  {
    this.commitIntervalMax = commitIntervalMax;
  }

  /**
   * Sets the doWriteEvery value.
   * 
   * @param doWriteEvery The doWriteEvery to set.
   */
  public void setDoWriteEvery(int doWriteEvery)
  {
    this.doWriteEvery = doWriteEvery;
  }

  /**
   * Sets the loopInThread value.
   * 
   * @param loopInThread The loopInThread to set.
   */
  public void setLoopInThread(int loopInThread)
  {
    this.loopInThread = loopInThread;
  }

  /**
   * Sets the maxIdValue value.
   * 
   * @param maxIdValue The maxIdValue to set.
   */
  public void setMaxIdValue(int maxIdValue)
  {
    this.maxIdValue = maxIdValue;
  }

  /**
   * Sets the maxResponseTime value.
   * 
   * @param maxResponseTime The maxResponseTime to set.
   */
  public void setMaxResponseTime(int maxResponseTime)
  {
    this.maxResponseTime = maxResponseTime;
  }

  /**
   * Sets the queryLoop value.
   * 
   * @param queryLoop The queryLoop to set.
   */
  public void setQueryLoop(int queryLoop)
  {
    this.queryLoop = queryLoop;
  }

  /**
   * Sets the rand value.
   * 
   * @param rand The rand to set.
   */
  public void setRand(Random rand)
  {
    this.rand = rand;
  }

  /**
   * Sets the requestInterval value.
   * 
   * @param requestInterval The requestInterval to set.
   */
  public void setRequestInterval(int requestInterval)
  {
    this.requestInterval = requestInterval;
  }

  /**
   * Sets the useTransactions value.
   * 
   * @param useTransactions The useTransactions to set.
   */
  public void setUseTransactions(boolean useTransactions)
  {
    this.useTransactions = useTransactions;
  }

  /**
   * Returns the runtime value.
   * 
   * @return Returns the runtime.
   */
  public long getRuntime()
  {
    return runtime;
  }

  /**
   * Sets the usePreparedStatement value.
   * 
   * @param usePreparedStatement The usePreparedStatement to set.
   */
  public void setUsePreparedStatement(boolean usePreparedStatement)
  {
    this.usePreparedStatement = usePreparedStatement;
  }

  /**
   * Returns the average value.
   * 
   * @return Returns the average.
   */
  public long getAverage()
  {
    return average;
  }

  /**
   * Returns the requestCount value.
   * 
   * @return Returns the requestCount.
   */
  public int getRequestCount()
  {
    return requestCount;
  }

  /**
   * Sets the useQueryGenerator value.
   * 
   * @param useQueryGenerator The useQueryGenerator to set.
   */
  public void setUseQueryGenerator(boolean useQueryGenerator)
  {
    this.useQueryGenerator = useQueryGenerator;
  }

  /**
   * Sets the queryGenerator value.
   * 
   * @param queryGenerator The queryGenerator to set.
   */
  public void setQueryGenerator(QueryGenerator queryGenerator)
  {
    this.queryGenerator = queryGenerator;
  }
}