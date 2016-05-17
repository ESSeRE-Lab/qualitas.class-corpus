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

package org.objectweb.cjdbc.controller.scheduler.raidb1;

import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.exceptions.RollbackException;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.requestmanager.RAIDbLevels;
import org.objectweb.cjdbc.controller.scheduler.AbstractScheduler;
import org.objectweb.cjdbc.controller.scheduler.schema.SchedulerDatabaseSchema;
import org.objectweb.cjdbc.controller.scheduler.schema.SchedulerDatabaseTable;
import org.objectweb.cjdbc.controller.scheduler.schema.TransactionExclusiveLock;

/**
 * This scheduler provides transaction level scheduling for RAIDb-1 controllers.
 * Each write takes a lock on the table it affects. All following writes are
 * blocked until the transaction of the first write completes. This scheduler
 * automatically detects simple deadlocks and rollbacks the transaction inducing
 * the deadlock. Note that transitive deadlocks (involving more than 2 tables
 * are not detected).
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class RAIDb1OptimisticTransactionLevelScheduler
    extends AbstractScheduler
{

  //
  // How the code is organized ?
  //
  // 1. Member variables
  // 2. Constructor
  // 3. Request handling
  // 4. Transaction management
  // 5. Debug/Monitoring
  //

  private long                    requestId;
  private SchedulerDatabaseSchema schedulerDatabaseSchema = null;

  //
  // Constructor
  //

  /**
   * Creates a new Optimistic Transaction Level Scheduler
   */
  public RAIDb1OptimisticTransactionLevelScheduler()
  {
    super(RAIDbLevels.RAIDb1, ParsingGranularities.TABLE);
    requestId = 0;
  }

  //
  // Request Handling
  //

  /**
   * Sets the <code>DatabaseSchema</code> of the current virtual database.
   * This is only needed by some schedulers that will have to define their own
   * scheduler schema
   * 
   * @param dbs a <code>DatabaseSchema</code> value
   * @see org.objectweb.cjdbc.controller.scheduler.schema.SchedulerDatabaseSchema
   */
  public synchronized void setDatabaseSchema(DatabaseSchema dbs)
  {
    if (schedulerDatabaseSchema == null)
    {
      logger.info("Setting new database schema");
      schedulerDatabaseSchema = new SchedulerDatabaseSchema(dbs);
    }
    else
    { // Schema is updated, compute the diff !
      SchedulerDatabaseSchema newSchema = new SchedulerDatabaseSchema(dbs);
      ArrayList tables = schedulerDatabaseSchema.getTables();
      ArrayList newTables = newSchema.getTables();
      if (newTables == null)
      { // New schema is empty (no backend is active anymore)
        logger.info("Removing all tables.");
        schedulerDatabaseSchema = null;
        return;
      }

      // Remove extra-tables
      for (int i = 0; i < tables.size(); i++)
      {
        SchedulerDatabaseTable t = (SchedulerDatabaseTable) tables.get(i);
        if (!newSchema.hasTable(t.getName()))
        {
          schedulerDatabaseSchema.removeTable(t);
          if (logger.isInfoEnabled())
            logger.info("Removing table " + t.getName());
        }
      }

      // Add missing tables
      int size = newTables.size();
      for (int i = 0; i < size; i++)
      {
        SchedulerDatabaseTable t = (SchedulerDatabaseTable) newTables.get(i);
        if (!schedulerDatabaseSchema.hasTable(t.getName()))
        {
          schedulerDatabaseSchema.addTable(t);
          if (logger.isInfoEnabled())
            logger.info("Adding table " + t.getName());
        }
      }
    }
  }

  /**
   * Merge the given <code>DatabaseSchema</code> with the current one.
   * 
   * @param dbs a <code>DatabaseSchema</code> value
   * @see org.objectweb.cjdbc.controller.scheduler.schema.SchedulerDatabaseSchema
   */
  public void mergeDatabaseSchema(DatabaseSchema dbs)
  {
    try
    {
      logger.info("Merging new database schema");
      schedulerDatabaseSchema.mergeSchema(new SchedulerDatabaseSchema(dbs));
    }
    catch (Exception e)
    {
      logger.error("Error while merging new database schema", e);
    }
  }

  /**
   * Additionally to scheduling the request, this method replaces the SQL Date
   * macros such as now() with the current date.
   * 
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleReadRequest(SelectRequest)
   */
  public final void scheduleReadRequest(SelectRequest request)
      throws SQLException
  {
    synchronized (this)
    {
      request.setId(requestId++);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#readCompletedNotify(SelectRequest)
   */
  public final void readCompletedNotify(SelectRequest request)
  {
  }

  /**
   * Additionally to scheduling the request, this method replaces the SQL Date
   * macros such as now() with the current date.
   * 
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleWriteRequest(AbstractWriteRequest)
   */
  public void scheduleNonSuspendedWriteRequest(AbstractWriteRequest request)
      throws SQLException, RollbackException
  {
    if (request.isCreate())
    {
      synchronized (this)
      {
        request.setId(requestId++);
      }
      return;
    }

    SchedulerDatabaseTable t = schedulerDatabaseSchema.getTable(request
        .getTableName());
    if (t == null)
    {
      String msg = "No table found for request " + request.getId();
      logger.error(msg);
      throw new SQLException(msg);
    }

    // Deadlock detection
    TransactionExclusiveLock tableLock = t.getLock();
    if (!request.isAutoCommit())
    {
      synchronized (this)
      {
        if (tableLock.isLocked())
        { // Is the lock owner blocked by a lock we already own?
          long owner = tableLock.getLocker();
          long us = request.getTransactionId();
          if (owner != us)
          { // Parse all tables
            ArrayList tables = schedulerDatabaseSchema.getTables();
            ArrayList weAreblocking = new ArrayList();
            int size = tables.size();
            for (int i = 0; i < size; i++)
            {
              SchedulerDatabaseTable table = (SchedulerDatabaseTable) tables
                  .get(i);
              if (table == null)
                continue;
              TransactionExclusiveLock lock = table.getLock();
              // Are we the lock owner ?
              if (lock.isLocked())
              {
                if (lock.getLocker() == us)
                {
                  // Is 'owner' in the list of the blocked transactions?
                  if (lock.isWaiting(owner))
                  { // Deadlock detected, we must rollback
                    releaseLocks(us);
                    throw new RollbackException(
                        "Deadlock detected, rollbacking transaction " + us);
                  }
                  else
                    weAreblocking.addAll(lock.getWaitingList());
                }
              }
            }
          }
          else
          { // We are the lock owner and already synchronized on this
            // Assign the request id and exit
            request.setId(requestId++);
            return;
          }
        }
        else
        { // Lock is free, take it in the synchronized block
          acquireLockAndSetRequestId(request, tableLock);
          return;
        }
      }
    }

    acquireLockAndSetRequestId(request, tableLock);
  }

  private void acquireLockAndSetRequestId(AbstractWriteRequest request,
      TransactionExclusiveLock tableLock) throws SQLException
  {
    // Acquire the lock
    if (tableLock.acquire(request))
    {
      synchronized (this)
      {
        request.setId(requestId++);
      }
      if (logger.isDebugEnabled())
        logger.debug("Request " + request.getId() + " scheduled for write ("
            + getPendingWrites() + " pending writes)");
    }
    else
    {
      if (logger.isWarnEnabled())
        logger.warn("Request " + request.getId() + " timed out ("
            + request.getTimeout() + " ms)");
      throw new SQLException("Timeout (" + request.getTimeout()
          + ") for request: " + request.getId());
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#notifyWriteCompleted(AbstractWriteRequest)
   */
  public final void notifyWriteCompleted(AbstractWriteRequest request)
  {
    if (request.isCreate())
    { // Add table to schema
      if (logger.isDebugEnabled())
        logger.debug("Adding table '" + request.getTableName()
            + "' to scheduler schema");
      synchronized (this)
      {
        schedulerDatabaseSchema.addTable(new SchedulerDatabaseTable(
            new DatabaseTable(request.getTableName())));
      }
    }
    else if (request.isDrop())
    { // Drop table from schema
      if (logger.isDebugEnabled())
        logger.debug("Removing table '" + request.getTableName()
            + "' to scheduler schema");
      synchronized (this)
      {
        schedulerDatabaseSchema.removeTable(schedulerDatabaseSchema
            .getTable(request.getTableName()));
      }
      return;
    }

    // Requests outside transaction delimiters must release the lock
    // as soon as they have executed
    if (request.isAutoCommit())
    {
      SchedulerDatabaseTable t = schedulerDatabaseSchema.getTable(request
          .getTableName());
      if (t == null)
      {
        String msg = "No table found to release lock for request "
            + request.getId();
        logger.error(msg);
      }
      else
        t.getLock().release();
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleNonSuspendedStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public void scheduleNonSuspendedStoredProcedure(StoredProcedure proc)
      throws SQLException, RollbackException
  {
    throw new SQLException(
        "Stored procedures are not supported by the RAIDb-1 optimistic transaction level scheduler.");
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#notifyStoredProcedureCompleted(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public void notifyStoredProcedureCompleted(StoredProcedure proc)
  {
    // We should never execute here since scheduleNonSuspendedStoredProcedure
    // should have failed prior calling us
    throw new RuntimeException(
        "Stored procedures are not supported by the RAIDb-1 optimistic transaction level scheduler.");
  }

  //
  // Transaction Management
  //

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#commitTransaction(long)
   */
  protected final void commitTransaction(long transactionId)
  {
    releaseLocks(transactionId);
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#rollbackTransaction(long)
   */
  protected final void rollbackTransaction(long transactionId)
  {
    releaseLocks(transactionId);
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#rollbackTransaction(long,
   *      String)
   */
  protected final void rollbackTransaction(long transactionId,
      String savepointName)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#setSavepointTransaction(long,
   *      String)
   */
  protected final void setSavepointTransaction(long transactionId, String name)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#releaseSavepointTransaction(long,
   *      String)
   */
  protected final void releaseSavepointTransaction(long transactionId,
      String name)
  {
  }

  /**
   * Release all locks we may own on tables.
   * 
   * @param transactionId id of the transaction that releases the locks
   */
  private synchronized void releaseLocks(long transactionId)
  {
    ArrayList tables = schedulerDatabaseSchema.getTables();
    int size = tables.size();
    for (int i = 0; i < size; i++)
    {
      SchedulerDatabaseTable t = (SchedulerDatabaseTable) tables.get(i);
      if (t == null)
        continue;
      TransactionExclusiveLock lock = t.getLock();
      // Are we the lock owner ?
      if (lock.isLocked())
        if (lock.getLocker() == transactionId)
          lock.release();
    }
  }

  //
  // Debug/Monitoring
  //

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#getXmlImpl()
   */
  public String getXmlImpl()
  {
    return "<" + DatabasesXmlTags.ELT_RAIDb1Scheduler + " "
        + DatabasesXmlTags.ATT_level + "=\""
        + DatabasesXmlTags.VAL_optimisticTransaction + "\"/>";
  }
}
