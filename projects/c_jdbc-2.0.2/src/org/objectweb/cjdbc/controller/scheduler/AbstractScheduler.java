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
 * Contributor(s): Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.controller.scheduler;

import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.RollbackException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;
import org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData;

/**
 * The Request Scheduler should schedule the request according to a given
 * policy.
 * <p>
 * The requests comes from the Request Controller and are sent later to the next
 * ccontroller omponents (cache and load balancer).
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public abstract class AbstractScheduler implements XmlComponent
{

  //
  // How the code is organized ?
  //
  // 1. Member variables
  // 2. Constructor
  // 3. Getter/Setter (possibly in alphabetical order)
  // 4. Request handling
  // 5. Transaction management
  // 6. Checkpoint management
  // 7. Debug/Monitoring
  //

  protected int          raidbLevel;
  protected int          parsingGranularity;

  // Transaction management
  private long           tid;
  private int            sid;
  private boolean        suspendedTransactions    = false;
  private int            pendingTransactions;
  private Object         transactionSync          = new Object();
  private Object         endOfCurrentTransactions = new Object();

  // Writes management
  private boolean        suspendedWrites          = false;
  private int            pendingWrites;
  private Object         writesSync               = new Object();
  private Object         endOfCurrentWrites       = new Object();

  protected static Trace logger                   = Trace
                                                      .getLogger("org.objectweb.cjdbc.controller.scheduler");

  // Monitoring values
  private int            numberRead               = 0;
  private int            numberWrite              = 0;

  //
  // Constructor
  //

  /**
   * Default scheduler to assign scheduler RAIDb level, needed granularity and
   * SQL macro handling (on the fly instanciation of NOW(), RAND(), ...).
   * 
   * @param raidbLevel RAIDb level of this scheduler
   * @param parsingGranularity Parsing granularity needed by the scheduler
   */
  public AbstractScheduler(int raidbLevel, int parsingGranularity)
  {
    this.raidbLevel = raidbLevel;
    this.parsingGranularity = parsingGranularity;
    this.tid = 0;
    this.sid = 0;
    this.pendingTransactions = 0;
    this.pendingWrites = 0;
  }

  //
  // Getter/Setter methods
  //

  /**
   * Initialize the transaction id with the given value (usually retrieved from
   * the recovery log).
   * 
   * @param transactionId new current transaction identifier
   */
  public final void initializeTransactionId(long transactionId)
  {
    this.tid = transactionId;
  }

  /**
   * Get the needed query parsing granularity.
   * 
   * @return needed query parsing granularity
   */
  public final int getParsingGranularity()
  {
    return parsingGranularity;
  }

  /**
   * Set the needed query parsing granularity.
   * 
   * @param parsingGranularity Parsing granularity needed by the scheduler
   */
  public final void setParsingGranularity(int parsingGranularity)
  {
    this.parsingGranularity = parsingGranularity;
  }

  /**
   * Returns the number of pending writes.
   * 
   * @return int
   */
  public final int getPendingWrites()
  {
    return pendingWrites;
  }

  /**
   * Returns the RAIDbLevel.
   * 
   * @return int
   */
  public final int getRAIDbLevel()
  {
    return raidbLevel;
  }

  /**
   * Sets the RAIDb level.
   * 
   * @param raidbLevel The RAIDbLevel to set
   */
  public final void setRAIDbLevel(int raidbLevel)
  {
    this.raidbLevel = raidbLevel;
  }

  /**
   * Sets the <code>DatabaseSchema</code> of the current virtual database.
   * This is only needed by some schedulers that will have to define their own
   * scheduler schema
   * 
   * @param dbs a <code>DatabaseSchema</code> value
   * @see org.objectweb.cjdbc.controller.scheduler.schema.SchedulerDatabaseSchema
   */
  public void setDatabaseSchema(DatabaseSchema dbs)
  {
    if (logger.isInfoEnabled())
      logger.info(Translate.get("scheduler.doesnt.support.schemas"));
  }

  /**
   * Merge the given <code>DatabaseSchema</code> with the current one.
   * 
   * @param dbs a <code>DatabaseSchema</code> value
   * @see org.objectweb.cjdbc.controller.scheduler.schema.SchedulerDatabaseSchema
   */
  public void mergeDatabaseSchema(DatabaseSchema dbs)
  {
    logger.info(Translate.get("scheduler.doesnt.support.schemas"));
  }

  /**
   * Increments the savepoint id for un-named savepoints
   * 
   * @return the next savepoint Id
   */
  public synchronized int incrementSavepointId()
  {
    sid++;
    return sid;
  }

  //
  // Request Scheduling
  //

  /**
   * Schedule a read request (implementation specific). This method blocks until
   * the read can be executed.
   * 
   * @param request Select request to schedule (SQL macros are already handled
   *          if needed)
   * @exception SQLException if a timeout occurs
   */
  public abstract void scheduleReadRequest(SelectRequest request)
      throws SQLException;

  /**
   * Notify the completion of a read statement.
   * 
   * @param request the completed request
   */
  public abstract void readCompletedNotify(SelectRequest request);

  /**
   * Notify the completion of a read statement.
   * 
   * @param request the completed request
   */
  public final void readCompleted(SelectRequest request)
  {
    numberRead++;
    this.readCompletedNotify(request);
  }

  /**
   * Schedule a write request. This method blocks if the writes are suspended.
   * Then the number of pending writes is updated and the implementation
   * specific scheduleNonSuspendedWriteRequest function is called. SQL macros
   * are replaced in the request if the scheduler has needSQLMacroHandling set
   * to true.
   * 
   * @param request Write request to schedule
   * @exception SQLException if a timeout occurs
   * @exception RollbackException if an error occurs
   * @see #scheduleNonSuspendedWriteRequest(AbstractWriteRequest)
   */
  public final void scheduleWriteRequest(AbstractWriteRequest request)
      throws SQLException, RollbackException
  {
    suspendWriteIfNeeded(request);
    scheduleNonSuspendedWriteRequest(request);
  }

  /**
   * Schedule a write request (implementation specific). This method blocks
   * until the request can be executed.
   * 
   * @param request Write request to schedule (SQL macros are already handled if
   *          needed)
   * @exception SQLException if a timeout occurs
   * @exception RollbackException if the transaction must be rollbacked
   */
  public abstract void scheduleNonSuspendedWriteRequest(
      AbstractWriteRequest request) throws SQLException, RollbackException;

  /**
   * Notify the completion of a write statement.
   * <p>
   * This method updates the number of pending writes and calls the
   * implementation specific notifyWriteCompleted function.
   * <p>
   * Finally, the suspendWrites() function is notified if needed.
   * 
   * @param request the completed request
   * @see #notifyWriteCompleted(AbstractWriteRequest)
   * @see #suspendWrites()
   */
  public final void writeCompleted(AbstractWriteRequest request)
  {
    synchronized (writesSync)
    {
      pendingWrites--;

      if (logger.isDebugEnabled())
        logger.debug("Write completed, remaining pending writes: "
            + pendingWrites);

      notifyWriteCompleted(request);

      // It this is the last write to complete and writes are
      // suspended we have to notify suspendedWrites()
      if (suspendedWrites && (pendingWrites == 0))
      {
        synchronized (endOfCurrentWrites)
        {
          endOfCurrentWrites.notifyAll();
        }
      }
    }
    numberWrite++;
  }

  /**
   * Notify the completion of a write statement. This method does not need to be
   * synchronized, it is enforced by the caller.
   * 
   * @param request the completed request
   * @see #writeCompleted(AbstractWriteRequest)
   */
  public abstract void notifyWriteCompleted(AbstractWriteRequest request);

  /**
   * Schedule a write request. This method blocks if the writes are suspended.
   * Then the number of pending writes is updated and the implementation
   * specific scheduleNonSuspendedWriteRequest function is called. SQL macros
   * are replaced in the request if the scheduler has needSQLMacroHandling set
   * to true.
   * 
   * @param proc Stored procedure to schedule
   * @exception SQLException if a timeout occurs
   * @exception RollbackException if an error occurs
   * @see #scheduleNonSuspendedStoredProcedure(StoredProcedure)
   */
  public final void scheduleStoredProcedure(StoredProcedure proc)
      throws SQLException, RollbackException
  {
    suspendWriteIfNeeded(proc);
    scheduleNonSuspendedStoredProcedure(proc);
  }

  /**
   * Schedule a write request (implementation specific). This method blocks
   * until the request can be executed.
   * 
   * @param proc Stored procedure to schedule
   * @exception SQLException if a timeout occurs
   * @exception RollbackException if the transaction must be rollbacked
   */
  public abstract void scheduleNonSuspendedStoredProcedure(StoredProcedure proc)
      throws SQLException, RollbackException;

  /**
   * Notify the completion of a stored procedure.
   * <p>
   * This method updates the number of pending writes and calls the
   * implementation specific notifyStoredProcedureCompleted function.
   * <p>
   * Finally, the suspendWrites() function is notified if needed.
   * 
   * @param proc the completed stored procedure
   * @see #notifyStoredProcedureCompleted(StoredProcedure)
   * @see #suspendWrites()
   */
  public final void storedProcedureCompleted(StoredProcedure proc)
  {
    synchronized (writesSync)
    {
      pendingWrites--;

      if (logger.isDebugEnabled())
        logger.debug("Stored procedure completed, remaining pending writes: "
            + pendingWrites);

      notifyStoredProcedureCompleted(proc);

      // It this is the last write to complete and writes are
      // suspended we have to notify suspendedWrites()
      if (suspendedWrites && (pendingWrites == 0))
      {
        synchronized (endOfCurrentWrites)
        {
          endOfCurrentWrites.notifyAll();
        }
      }
    }
    numberWrite++;
  }

  /**
   * Notify the completion of a stored procedure. This method does not need to
   * be synchronized, it is enforced by the caller.
   * 
   * @param proc the completed stored procedure
   * @see #storedProcedureCompleted(StoredProcedure)
   */
  public abstract void notifyStoredProcedureCompleted(StoredProcedure proc);

  /**
   * Suspend write requests if suspendedWrites is active.
   * 
   * @param request the request to suspend (a write request or a stored
   *          procedure)
   * @throws SQLException if the request timeout has expired
   */
  private void suspendWriteIfNeeded(AbstractRequest request)
      throws SQLException
  {
    synchronized (writesSync)
    {
      if (suspendedWrites)
      {
        try
        {
          // Wait on writesSync
          int timeout = request.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            long lTimeout = timeout * 1000;
            writesSync.wait(lTimeout);
            long end = System.currentTimeMillis();
            int remaining = (int) (lTimeout - (end - start));
            if (remaining > 0)
              request.setTimeout(remaining);
            else
            {
              String msg = Translate.get("scheduler.request.timeout",
                  new String[]{String.valueOf(request.getId()),
                      String.valueOf(request.getTimeout())});
              logger.warn(msg);
              throw new SQLException(msg);
            }
          }
          else
            this.writesSync.wait();
        }
        catch (InterruptedException e)
        {
          String msg = Translate.get("scheduler.request.timeout.failed", e);
          logger.warn(msg);
          throw new SQLException(msg);
        }
      }
      pendingWrites++;

      if (logger.isDebugEnabled())
        logger.debug("Schedule " + request.getSQL()
            + " - Current pending writes: " + pendingWrites);
    }
  }

  //
  // Transaction management
  //

  /**
   * Begin a new transaction and return the corresponding transaction
   * identifier. This method is called from the driver when setAutoCommit(false)
   * is called.
   * 
   * @param tm The transaction marker metadata
   * @return the transaction identifier
   * @throws SQLException if an error occurs
   */
  public final long begin(TransactionMarkerMetaData tm) throws SQLException
  {
    // Check if writes are suspended
    synchronized (writesSync)
    {
      if (suspendedWrites)
      {
        try
        {
          // Wait on writesSync
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            writesSync.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining > 0)
              tm.setTimeout(remaining);
            else
            {
              String msg = Translate.get("scheduler.begin.timeout.writeSync");
              logger.warn(msg);
              throw new SQLException(msg);
            }
          }
          else
            writesSync.wait();
        }
        catch (InterruptedException e)
        {
          String msg = Translate.get("scheduler.begin.timeout.writeSync")
              + " (" + e + ")";
          logger.error(msg);
          throw new SQLException(msg);
        }
      }
      pendingWrites++;

      if (logger.isDebugEnabled())
        logger.debug("Begin scheduled - current pending writes: "
            + pendingWrites);
    }

    // Check if transactions are suspended
    synchronized (transactionSync)
    {
      if (suspendedTransactions)
        try
        {
          // Wait on transactionSync
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            transactionSync.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining > 0)
              tm.setTimeout(remaining);
            else
            {
              String msg = Translate
                  .get("scheduler.begin.timeout.transactionSync");
              logger.warn(msg);
              throw new SQLException(msg);
            }
          }
          else
            transactionSync.wait();
        }
        catch (InterruptedException e)
        {
          String msg = Translate.get("scheduler.begin.timeout.transactionSync")
              + " (" + e + ")";
          logger.error(msg);
          throw new SQLException(msg);
        }
      tid++;
      pendingTransactions++;

      if (logger.isDebugEnabled())
        logger.debug("Begin scheduled - current pending transactions: "
            + pendingTransactions);
      return tid;
    }
  }

  /**
   * Notify the completion of a begin command.
   * 
   * @param transactionId of the completed begin
   */
  public final void beginCompleted(long transactionId)
  {
    // Take care of suspended write
    synchronized (writesSync)
    {
      pendingWrites--;

      if (logger.isDebugEnabled())
        logger.debug("Begin completed, remaining pending writes: "
            + pendingWrites);

      // It this is the last write to complete and writes are
      // suspended we have to notify suspendedWrites()
      if (suspendedWrites && (pendingWrites == 0))
      {
        synchronized (endOfCurrentWrites)
        {
          endOfCurrentWrites.notifyAll();
        }
      }
    }
  }

  /**
   * Commit a transaction.
   * <p>
   * Calls the implementation specific commitTransaction()
   * 
   * @param tm The transaction marker metadata
   * @throws SQLException if an error occurs
   * @see #commitTransaction(long)
   */
  public final void commit(TransactionMarkerMetaData tm) throws SQLException
  {
    // Check if writes are suspended
    synchronized (writesSync)
    {
      if (suspendedWrites)
      {
        try
        {
          // Wait on writesSync
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            writesSync.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining > 0)
              tm.setTimeout(remaining);
            else
            {
              String msg = Translate.get("scheduler.commit.timeout.writeSync");
              logger.warn(msg);
              throw new SQLException(msg);
            }
          }
          else
            writesSync.wait();
        }
        catch (InterruptedException e)
        {
          String msg = Translate.get("scheduler.commit.timeout.writeSync")
              + " (" + e + ")";
          logger.error(msg);
          throw new SQLException(msg);
        }
      }
      pendingWrites++;

      if (logger.isDebugEnabled())
        logger.debug("Commit scheduled - current pending writes: "
            + pendingWrites);
    }
    commitTransaction(tm.getTransactionId());
  }

  /**
   * Commit a transaction given its id.
   * 
   * @param transactionId the transaction id
   */
  protected abstract void commitTransaction(long transactionId);

  /**
   * Notify the completion of a commit command.
   * 
   * @param transactionId of the completed commit
   */
  public final void commitCompleted(long transactionId)
  {
    // Take care of suspended transactions
    synchronized (transactionSync)
    {
      pendingTransactions--;

      if (logger.isDebugEnabled())
        logger.debug("Commit completed, remaining pending transactions: "
            + pendingTransactions);

      // If it is the last pending transaction to complete and we
      // are waiting for pending transactions to complete, then wake
      // up suspendNewTransactionsForCheckpoint()
      if (suspendedTransactions && (pendingTransactions == 0))
      {
        synchronized (endOfCurrentTransactions)
        {
          endOfCurrentTransactions.notifyAll();
        }
      }
    }
    // Take care of suspended write
    synchronized (writesSync)
    {
      pendingWrites--;

      if (logger.isDebugEnabled())
        logger.debug("Commit completed, remaining pending writes: "
            + pendingWrites);

      // It this is the last write to complete and writes are
      // suspended we have to notify suspendedWrites()
      if (suspendedWrites && (pendingWrites == 0))
      {
        synchronized (endOfCurrentWrites)
        {
          endOfCurrentWrites.notifyAll();
        }
      }
    }
  }

  /**
   * Rollback a transaction.
   * <p>
   * Calls the implementation specific rollbackTransaction()
   * 
   * @param tm The transaction marker metadata
   * @exception SQLException if an error occurs
   * @see #rollbackTransaction(long)
   */
  public final void rollback(TransactionMarkerMetaData tm) throws SQLException
  {
    // Check if writes are suspended
    synchronized (writesSync)
    {
      if (suspendedWrites)
      {
        try
        {
          // Wait on writesSync
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            writesSync.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining > 0)
              tm.setTimeout(remaining);
            else
            {
              String msg = Translate
                  .get("scheduler.rollback.timeout.writeSync");
              logger.warn(msg);
              throw new SQLException(msg);
            }
          }
          else
            writesSync.wait();
        }
        catch (InterruptedException e)
        {
          String msg = Translate.get("scheduler.rollback.timeout.writeSync")
              + " (" + e + ")";
          logger.error(msg);
          throw new SQLException(msg);
        }
      }
      pendingWrites++;

      if (logger.isDebugEnabled())
        logger.debug("Rollback scheduled - current pending writes: "
            + pendingWrites);
    }
    rollbackTransaction(tm.getTransactionId());
  }

  /**
   * Rollback a transaction to a savepoint.
   * <p>
   * Calls the implementation specific rollbackTransaction()
   * 
   * @param tm transaction marker metadata
   * @param savepointName name of the savepoint
   * @throws SQLException if an error occurs
   */
  public final void rollback(TransactionMarkerMetaData tm, String savepointName)
      throws SQLException
  {
    // Check if writes are suspended
    synchronized (writesSync)
    {
      if (suspendedWrites)
        try
        {
          // Wait on writesSync
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            writesSync.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining > 0)
              tm.setTimeout(remaining);
            else
            {
              String msg = Translate
                  .get("scheduler.rollbacksavepoint.timeout.writeSync");
              logger.warn(msg);
              throw new SQLException(msg);
            }
          }
          else
            writesSync.wait();
        }
        catch (InterruptedException e)
        {
          String msg = Translate
              .get("scheduler.rollbacksavepoint.timeout.writeSync")
              + " (" + e + ")";
          logger.error(msg);
          throw new SQLException(msg);
        }
      pendingWrites++;

      if (logger.isDebugEnabled())
        logger.debug("Rollback " + savepointName
            + " scheduled - current pending writes: " + pendingWrites);
    }

    this.rollbackTransaction(tm.getTransactionId(), savepointName);
  }

  /**
   * Rollback a transaction given its id.
   * 
   * @param transactionId the transaction id
   */
  protected abstract void rollbackTransaction(long transactionId);

  /**
   * Rollback a transaction given its id to a savepoint given its name.
   * 
   * @param transactionId the transaction id
   * @param savepointName the name of the savepoint
   */
  protected abstract void rollbackTransaction(long transactionId,
      String savepointName);

  /**
   * Notify the completion of a rollback command.
   * 
   * @param transactionId of the rollback commit
   */
  public final void rollbackCompleted(long transactionId)
  {
    // Take care of suspended transactions
    synchronized (transactionSync)
    {
      pendingTransactions--;

      if (logger.isDebugEnabled())
        logger.debug("Rollback completed, remaining pending transactions: "
            + pendingTransactions);

      // If it is the last pending transaction to complete and we
      // are waiting for pending transactions to complete, then wake
      // up suspendNewTransactionsForCheckpoint()
      if (suspendedTransactions && (pendingTransactions == 0))
      {
        synchronized (endOfCurrentTransactions)
        {
          endOfCurrentTransactions.notifyAll();
        }
      }
    }
    // Take care of suspended write
    synchronized (writesSync)
    {
      pendingWrites--;

      if (logger.isDebugEnabled())
        logger.debug("Rollback completed, remaining pending writes: "
            + pendingWrites);

      // It this is the last write to complete and writes are
      // suspended we have to notify suspendedWrites()
      if (suspendedWrites && (pendingWrites == 0))
      {
        synchronized (endOfCurrentWrites)
        {
          endOfCurrentWrites.notifyAll();
        }
      }
    }
  }

  /**
   * Set an unnamed savepoint.
   * <p>
   * Calls the implementation specific setSavepointTransaction()
   * 
   * @param tm transaction marker metadata
   * @return savepoint Id
   * @throws SQLException if an error occurs
   */
  public final int setSavepoint(TransactionMarkerMetaData tm)
      throws SQLException
  {
    // Check if writes are suspended
    synchronized (writesSync)
    {
      if (suspendedWrites)
        try
        {
          // Wait on writesSync
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            writesSync.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining > 0)
              tm.setTimeout(remaining);
            else
            {
              String msg = Translate
                  .get("scheduler.setsavepoint.timeout.writeSync");
              logger.warn(msg);
              throw new SQLException(msg);
            }
          }
          else
            writesSync.wait();
        }
        catch (InterruptedException e)
        {
          String msg = Translate
              .get("scheduler.setsavepoint.timeout.writeSync")
              + " (" + e + ")";
          logger.error(msg);
          throw new SQLException(msg);
        }
      pendingWrites++;

      if (logger.isDebugEnabled())
        logger.debug("Set savepoint scheduled - current pending writes: "
            + pendingWrites);
    }

    int savepointId = this.incrementSavepointId();
    this.setSavepointTransaction(tm.getTransactionId(), String
        .valueOf(savepointId));
    return savepointId;
  }

  /**
   * Set a named savepoint.
   * <p>
   * Calls the implementation specific setSavepointTransaction()
   * 
   * @param tm transaction marker metadata
   * @param name name of the savepoint
   * @throws SQLException if an error occurs
   */
  public final void setSavepoint(TransactionMarkerMetaData tm, String name)
      throws SQLException
  {
    // Check if writes are suspended
    synchronized (writesSync)
    {
      if (suspendedWrites)
        try
        {
          // Wait on writesSync
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            writesSync.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining > 0)
              tm.setTimeout(remaining);
            else
            {
              String msg = Translate
                  .get("scheduler.setsavepoint.timeout.writeSync");
              logger.warn(msg);
              throw new SQLException(msg);
            }
          }
          else
            writesSync.wait();
        }
        catch (InterruptedException e)
        {
          String msg = Translate
              .get("scheduler.setsavepoint.timeout.writeSync")
              + " (" + e + ")";
          logger.error(msg);
          throw new SQLException(msg);
        }
      pendingWrites++;

      if (logger.isDebugEnabled())
        logger.debug("Set savepoint " + name
            + " scheduled - current pending writes: " + pendingWrites);
    }

    this.setSavepointTransaction(tm.getTransactionId(), name);
  }

  /**
   * Set a savepoint given its name to a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @param name the name of the savepoint
   */
  protected abstract void setSavepointTransaction(long transactionId,
      String name);

  /**
   * Release a savepoint.
   * <p>
   * Calls the implementation specific releaseSavepointTransaction()
   * 
   * @param tm transaction marker metadata
   * @param name name of the savepoint
   * @throws SQLException if an error occurs
   */
  public final void releaseSavepoint(TransactionMarkerMetaData tm, String name)
      throws SQLException
  {
    // Check if writes are suspended
    synchronized (writesSync)
    {
      if (suspendedWrites)
        try
        {
          // Wait on writesSync
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            writesSync.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining > 0)
              tm.setTimeout(remaining);
            else
            {
              String msg = Translate
                  .get("scheduler.releasesavepoint.timeout.writeSync");
              logger.warn(msg);
              throw new SQLException(msg);
            }
          }
          else
            writesSync.wait();
        }
        catch (InterruptedException e)
        {
          String msg = Translate
              .get("scheduler.releasesavepoint.timeout.writeSync")
              + " (" + e + ")";
          logger.error(msg);
          throw new SQLException(msg);
        }
      pendingWrites++;

      if (logger.isDebugEnabled())
        logger.debug("Release savepoint " + name
            + " scheduled - current pending writes: " + pendingWrites);
    }

    this.releaseSavepointTransaction(tm.getTransactionId(), name);
  }

  /**
   * Release a savepoint given its name from a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @param name the name of the savepoint
   */
  protected abstract void releaseSavepointTransaction(long transactionId,
      String name);

  /**
   * Notify the conpletion of a savepoint action.
   * 
   * @param transactionId the transaction identifier
   */
  public final void savepointCompleted(long transactionId)
  {
    synchronized (writesSync)
    {
      pendingWrites--;

      if (logger.isDebugEnabled())
        logger.debug("Savepoint completed, remaining pending writes: "
            + pendingWrites);

      // It this is the last write to complete and writes are
      // suspended we have to notify suspendedWrites()
      if (suspendedWrites && (pendingWrites == 0))
      {
        synchronized (endOfCurrentWrites)
        {
          endOfCurrentWrites.notifyAll();
        }
      }
    }
  }

  //
  // Checkpoint management
  //

  /**
   * Suspend all calls to begin() until all current transactions are finished in
   * order to store a checkpoint. This method returns when all pending
   * transactions have finished.
   * <p>
   * New transactions remain suspended until resumeNewTransactions() is called.
   * 
   * @throws SQLException if an error occurs
   * @see #resumeNewTransactions()
   */
  public final void suspendNewTransactionsForCheckpoint() throws SQLException
  {
    synchronized (transactionSync)
    {
      suspendedTransactions = true;
      if (pendingTransactions == 0)
        return;
    }

    synchronized (endOfCurrentTransactions)
    {
      // Here we have a potential synchronization problem since the last
      // transaction completion could have happened before we entered this
      // synchronized block. Therefore we recheck if there is effectively
      // still pending transactions. If this is not the case, we don't have
      // to sleep and we can immediately return.
      if (pendingTransactions == 0)
        return;

      // Wait for pending transactions to end
      try
      {
        endOfCurrentTransactions.wait();
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get("scheduler.suspend.transaction.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }
    }
  }

  /**
   * Resume new transactions that were suspended by
   * suspendNewTransactionsForCheckpoint().
   * 
   * @see #suspendNewTransactionsForCheckpoint()
   */
  public final void resumeNewTransactions()
  {
    synchronized (transactionSync)
    {
      suspendedTransactions = false;
      // Wake up all pending begin statements
      transactionSync.notifyAll();
    }
  }

  /**
   * Suspend all write queries. This method blocks until all pending writes are
   * completed.
   * <p>
   * Writes execution is resumed by calling resumeWrites()
   * 
   * @throws SQLException if an error occurs
   * @see #resumeWrites()
   */
  public void suspendWrites() throws SQLException
  {
    synchronized (writesSync)
    {
      suspendedWrites = true;
      if (pendingWrites == 0)
        return;
    }

    synchronized (endOfCurrentWrites)
    {
      // Here we have a potential synchronization problem since the last
      // write completion could have happened before we entered this
      // synchronized block. Therefore we recheck if there is effectively
      // still pending writes. If this is not the case, we don't have
      // to sleep and we can immediately return.
      if (pendingWrites == 0)
        return;

      // Wait for pending transactions to end
      try
      {
        endOfCurrentWrites.wait();
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get("scheduler.suspend.writes.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }
    }
  }

  /**
   * Resume the execution of write queries that were suspended by
   * suspendWrites().
   * 
   * @see #suspendWrites()
   */
  public void resumeWrites()
  {
    synchronized (writesSync)
    {
      suspendedWrites = false;
      // Wake up all waiting writes
      writesSync.notifyAll();
    }
  }

  //
  // Debug/Monitoring
  //

  protected abstract String getXmlImpl();

  /**
   * Get information about the Request Scheduler in xml format
   * 
   * @return <code>String</code> containing information in xml
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_RequestScheduler + ">");
    info.append(this.getXmlImpl());
    info.append("</" + DatabasesXmlTags.ELT_RequestScheduler + ">");
    return info.toString();
  }

  /**
   * Returns live information on the scheduler
   * 
   * @return array of data
   */
  public String[] getSchedulerData()
  {
    String[] data = new String[7];
    data[0] = "" + numberRead;
    data[1] = "" + numberWrite;
    data[2] = "" + pendingTransactions;
    data[3] = "" + pendingWrites;
    data[4] = "" + numberRead + numberWrite;
    data[5] = (suspendedTransactions) ? "1" : "0";
    data[6] = (suspendedWrites) ? "1" : "0";
    return data;
  }

  /**
   * @return Returns the numberRead.
   */
  public int getNumberRead()
  {
    return numberRead;
  }

  /**
   * @return Returns the numberWrite.
   */
  public int getNumberWrite()
  {
    return numberWrite;
  }

  /**
   * @return Returns the pendingTransactions.
   */
  public int getPendingTransactions()
  {
    return pendingTransactions;
  }

  /**
   * @return Returns the suspendedTransactions.
   */
  public boolean isSuspendedTransactions()
  {
    return suspendedTransactions;
  }

  /**
   * @return Returns the suspendedWrites.
   */
  public boolean isSuspendedWrites()
  {
    return suspendedWrites;
  }
}