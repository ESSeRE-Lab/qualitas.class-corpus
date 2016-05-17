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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.recoverylog;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.recoverylog.events.LogEvent;

/**
 * Logger thread for the RecoveryLog.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class LoggerThread extends Thread
{
  private boolean           killed = false; // Control thread death
  private LinkedList        logQueue;
  private Trace             logger;
  private PreparedStatement logStmt;
  private PreparedStatement unlogStmt;
  private RecoveryLog       recoveryLog;

  /**
   * Creates a new <code>LoggerThread</code> object
   * 
   * @param log the RecoveryLog that instanciates this thread
   */
  public LoggerThread(RecoveryLog log)
  {
    super("LoggerThread");
    this.recoveryLog = log;
    this.logger = RecoveryLog.logger;
    logStmt = null;
    unlogStmt = null;
    logQueue = new LinkedList();
  }

  /**
   * Returns the logger value.
   * 
   * @return Returns the logger.
   */
  public Trace getLogger()
  {
    return logger;
  }

  /**
   * Tells whether there are pending logs
   * 
   * @return true if no more jobs in the log queue
   */
  public synchronized boolean getLogQueueIsEmpty()
  {
    if (logQueue.isEmpty())
    {
      // Notifies the Recovery log that the queue is empty.
      notify();
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Return a PreparedStatement to log an entry as follows:
   * <p>
   * INSERT INTO LogTableName VALUES(?,?,?,?)
   * 
   * @return a PreparedStatement
   * @throws SQLException if an error occurs
   */
  public PreparedStatement getLogPreparedStatement() throws SQLException
  {
    if (logStmt == null)
    {
      logStmt = recoveryLog.getDatabaseConnection().prepareStatement(
          "INSERT INTO " + recoveryLog.getLogTableName() + " VALUES(?,?,?,?)");
    }
    return logStmt;
  }

  /**
   * Returns the recoveryLog value.
   * 
   * @return Returns the recoveryLog.
   */
  public RecoveryLog getRecoveryLog()
  {
    return recoveryLog;
  }

  /**
   * Return a PreparedStatement to unlog an entry as follows:
   * <p>
   * DELETE FROM LogTableName WHERE id=? AND vlogin=? AND SqlColumnName=? AND
   * transaction_id=?
   * 
   * @return a PreparedStatement
   * @throws SQLException if an error occurs
   */
  public PreparedStatement getUnlogPreparedStatement() throws SQLException
  {
    if (unlogStmt == null)
    {
      unlogStmt = recoveryLog.getDatabaseConnection().prepareStatement(
          "DELETE FROM " + recoveryLog.getLogTableName()
              + " WHERE id=? AND vlogin=? AND "
              + recoveryLog.getLogTableSqlColumnName()
              + "=? AND transaction_id=?");
    }
    return unlogStmt;
  }

  /**
   * Invalidate both logStmt and unlogStmt so that they can be renewed from a
   * fresh connection.
   * 
   * @see #getLogPreparedStatement()
   * @see #getUnlogPreparedStatement()
   */
  public void invalidateLogStatements()
  {
    try
    {
      logStmt.close();
    }
    catch (Exception ignore)
    {
    }
    try
    {
      unlogStmt.close();
    }
    catch (Exception ignore)
    {
    }
    logStmt = null;
    unlogStmt = null;
    recoveryLog.invalidateInternalConnection();
  }

  /**
   * Log a write-query into the recovery log. This posts the specified logObject
   * (query) into this loggerThread queue. The actual write to the recoverly-log
   * db is performed asynchronously by the thread.
   * 
   * @param logObject the log event to be processed
   */
  public synchronized void log(LogEvent logObject)
  {
    logQueue.addLast(logObject);
    notify();
  }

  /**
   * Put back a log entry at the head of the queue in case a problem happened
   * with this entry and we need to retry it right away.
   * 
   * @param event the event to be used next by the logger thread.
   */
  public synchronized void putBackAtHeadOfQueue(LogEvent event)
  {
    logQueue.addFirst(event);
    notify();
  }

  /**
   * Remove all queries that have not been logged yet and belonging to the
   * specified transaction.
   * 
   * @param tid transaction id to rollback
   */
  public synchronized void removeQueriesOfTransactionFromQueue(long tid)
  {
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("recovery.jdbc.loggerthread.removing", tid));
    LogEvent logEvent;
    for (int i = 0; i < logQueue.size(); i++)
    {
      logEvent = (LogEvent) logQueue.get(i);
      if (logEvent.belongToTransaction(tid))
      {
        logQueue.remove(i);
        i--;
      }
    }
  }

  /**
   * Remove a transaction that has rollbacked (no check is made if the
   * transaction has really rollbacked or not).
   * 
   * @param transactionId the id of the transaction
   * @throws SQLException if an error occurs
   */
  public void removeRollbackedTransaction(long transactionId)
      throws SQLException
  {
    // The transaction failed
    // Remove the requests with this transactionId from the database
    removeQueriesOfTransactionFromQueue(transactionId);
    PreparedStatement stmt = null;
    try
    {
      stmt = recoveryLog.getDatabaseConnection().prepareStatement(
          "DELETE FROM " + recoveryLog.getLogTableName()
              + " WHERE transaction_id=?");
      stmt.setLong(1, transactionId);
      stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      throw new SQLException(Translate.get(
          "recovery.jdbc.transaction.remove.failed", new String[]{
              String.valueOf(transactionId), e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Delete all entries from the CheckpointTable.
   * 
   * @throws SQLException if an error occurs
   */
  public void deleteCheckpointTable() throws SQLException
  {
    // First delete from the checkpoint table
    PreparedStatement stmt = null;
    try
    {
      stmt = recoveryLog.getDatabaseConnection().prepareStatement(
          "DELETE FROM " + recoveryLog.getCheckpointTableName());
      stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      String msg = "Failed to delete checkpoint table";
      logger.warn(msg, e);
      throw new SQLException(msg);
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Looks like a copy paste from RecoveryLog#storeCheckpoint(String, long), but
   * does not wait for queue completion to store the checkpoint. Moreover, in
   * case of error, additionely closes and invalidates log and unlog statements
   * (internal) before calling RecoveryLog#invalidateInternalConnection().
   * 
   * @param checkpointName checkpoint name to insert
   * @param checkpointId checkpoint request identifier
   * @throws SQLException if a database access error occurs
   * @see RecoveryLog#storeCheckpoint(String, long)
   * @see #invalidateLogStatements()
   */
  public void storeCheckpoint(String checkpointName, long checkpointId)
      throws SQLException
  {
    PreparedStatement stmt = null;
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Storing checkpoint " + checkpointName + " at request id "
            + checkpointId);
      stmt = recoveryLog.getDatabaseConnection().prepareStatement(
          "INSERT INTO " + recoveryLog.getCheckpointTableName()
              + " VALUES(?,?)");
      stmt.setString(1, checkpointName);
      stmt.setLong(2, checkpointId);
      stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      invalidateLogStatements();
      throw new SQLException(Translate.get(
          "recovery.jdbc.checkpoint.store.failed", new String[]{checkpointName,
              e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Delete all LogEntries with an identifier lower than oldId (inclusive).
   * oldId is normally derived from a checkpoint name, which marks the last
   * request before the checkpoint.
   * 
   * @param oldId the id up to which entries should be removed.
   * @throws SQLException if an error occurs
   */
  public void deleteLogEntriesBeforeId(long oldId) throws SQLException
  {
    PreparedStatement stmt = null;
    try
    {
      stmt = recoveryLog.getDatabaseConnection().prepareStatement(
          "DELETE FROM " + recoveryLog.getLogTableName() + " WHERE id<=?");
      stmt.setLong(1, oldId);
      stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      // TODO: Check error message below
      throw new SQLException(Translate.get(
          "recovery.jdbc.transaction.remove.failed", new String[]{
              String.valueOf(oldId), e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Shift LogEntries identifiers from the specified value (value is added to
   * existing identifiers).
   * 
   * @param shiftValue the value to shift
   * @throws SQLException if an error occurs
   */
  public void shiftLogEntriesIds(long shiftValue) throws SQLException
  {
    PreparedStatement stmt = null;
    try
    {
      stmt = recoveryLog.getDatabaseConnection().prepareStatement(
          "UPDATE " + recoveryLog.getLogTableName() + " SET id=id+?");
      stmt.setLong(1, shiftValue);
      stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      // TODO: Check error message below
      throw new SQLException(Translate.get(
          "recovery.jdbc.transaction.remove.failed", new String[]{
              String.valueOf(shiftValue), e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Log the requests from queue until the thread is explicetly killed. The
   * logger used is the one of the RecoveryLog.
   */
  public void run()
  {
    LogEvent event;

    while (!killed)
    {
      synchronized (this)
      {
        while (getLogQueueIsEmpty() && !killed)
        {
          try
          {
            wait();
          }
          catch (InterruptedException e)
          {
            logger.warn(Translate.get("recovery.jdbc.loggerthread.awaken"), e);
          }
        }
        if (killed)
          break;
        // Pump first log entry from the queue
        event = (LogEvent) logQueue.remove(0);
        event.execute(this);
      }
    }
    logger.debug("JDBC Logger thread ending");
    invalidateLogStatements();
  }

  /**
   * Shutdown the current thread.
   */
  public synchronized void shutdown()
  {
    killed = true;
    notify();
  }

}
