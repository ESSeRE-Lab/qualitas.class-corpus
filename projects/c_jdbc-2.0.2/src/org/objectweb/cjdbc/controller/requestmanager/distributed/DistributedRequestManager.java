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
 * Contributor(s): Olivier Fambon, Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.controller.requestmanager.distributed;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.management.NotCompliantMBeanException;

import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.VirtualDatabaseException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.shared.BackendInfo;
import org.objectweb.cjdbc.common.shared.BackendState;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.AllBackendsFailedException;
import org.objectweb.cjdbc.controller.recoverylog.RecoveryLog;
import org.objectweb.cjdbc.controller.requestmanager.RequestManager;
import org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData;
import org.objectweb.cjdbc.controller.scheduler.AbstractScheduler;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.DistributedVirtualDatabase;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.CJDBCGroupMessage;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.DisableBackend;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.EnableBackend;
import org.objectweb.tribe.adapters.MulticastRequestAdapter;

/**
 * This class defines a Distributed Request Manager.
 * <p>
 * The DRM is composed of a Request Scheduler, an optional Query Cache, and a
 * Load Balancer and an optional Recovery Log. Unlike a non-dsitributed Request
 * Manager, this implementation is responsible for synchronizing the different
 * controllers components (schedulers, ...). Functions that are RAIDb level
 * dependent are implemented in sub-classes.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public abstract class DistributedRequestManager extends RequestManager
{
  protected DistributedVirtualDatabase dvdb;
  /** List of queries that failed on all backends */
  private Vector                       failedOnAllBackends;
  /** Unique controller identifier */
  private long                         controllerId;
  /** List of transactions that have executed on multiple controllers */
  private ArrayList                    distributedTransactions;

  // Used to check if a result was received or not
  protected static final int           NO_RESULT = -5;

  /**
   * Builds a new <code>DistributedRequestManager</code> instance without
   * cache.
   * 
   * @param vdb the virtual database this request manager belongs to
   * @param scheduler the Request Scheduler to use
   * @param cache a Query Cache implementation
   * @param loadBalancer the Request Load Balancer to use
   * @param recoveryLog the Log Recovery to use
   * @param beginTimeout timeout in seconds for begin
   * @param commitTimeout timeout in seconds for commit
   * @param rollbackTimeout timeout in seconds for rollback
   * @throws SQLException if an error occurs
   * @throws NotCompliantMBeanException if this class is not a compliant JMX
   *           MBean
   */
  public DistributedRequestManager(DistributedVirtualDatabase vdb,
      AbstractScheduler scheduler, AbstractResultCache cache,
      AbstractLoadBalancer loadBalancer, RecoveryLog recoveryLog,
      long beginTimeout, long commitTimeout, long rollbackTimeout)
      throws SQLException, NotCompliantMBeanException
  {
    super(vdb, scheduler, cache, loadBalancer, recoveryLog, beginTimeout,
        commitTimeout, rollbackTimeout);
    dvdb = vdb;
    failedOnAllBackends = new Vector();
    distributedTransactions = new ArrayList();
  }

  //
  // Controller identifier related functions
  //

  /**
   * Effective controllerIds are on the upper 16 bits of a long (64 bits).
   * Distributed transaction ids (longs) are layed out as [ControllerId(16bits) |
   * LocalTransactionId(64bits)]. <br/>This constant used in
   * DistributedVirtualDatabase.
   */
  public static final long CONTROLLER_ID_BIT_MASK   = 0xffff000000000000L;
  /**
   * TRANSACTION_ID_BIT_MASK is used to get the transaction id local to the
   * originating controller
   */
  public static final long TRANSACTION_ID_BIT_MASK  = ~CONTROLLER_ID_BIT_MASK;

  /**
   * @see #CONTROLLER_ID_BIT_MASK
   */
  public static final int  CONTROLLER_ID_SHIFT_BITS = 48;

  /**
   * @see #CONTROLLER_ID_BIT_MASK
   */
  public static final long CONTROLLER_ID_BITS       = 0x000000000000ffffL;

  /**
   * Returns the unique controller identifier.
   * 
   * @return Returns the controllerId.
   */
  public long getControllerId()
  {
    return controllerId;
  }

  /**
   * Sets the controller identifier value (this id must be unique). Parameter id
   * must hold on 16 bits (&lt; 0xffff), otherwise an exception is thrown.
   * Effective this.controllerId is <strong>not </strong> set to passed
   * parameter id, but to id &lt;&lt; ControllerIdShiftBits. The reason for all
   * this is that controllerIds are to be carried into ditributed transactions
   * ids, in the upper 16 bits.
   * 
   * @param id The controllerId to set.
   */
  public void setControllerId(long id)
  {
    if ((id & ~CONTROLLER_ID_BITS) != 0)
    {
      String msg = "Out of range controller id (" + id + ")";
      logger.error(msg);
      throw new RuntimeException(msg);
    }
    this.controllerId = (id << CONTROLLER_ID_SHIFT_BITS)
        & CONTROLLER_ID_BIT_MASK;
    if (logger.isDebugEnabled())
      logger.debug("Setting controller identifier to " + id
          + " (shifted value is " + controllerId + ")");
  }

  /**
   * Get the trace logger of this DistributedRequestManager
   * 
   * @return a <code>Trace</code> object
   */
  public Trace getLogger()
  {
    return logger;
  }

  /**
   * Returns the vdb value.
   * 
   * @return Returns the vdb.
   */
  public VirtualDatabase getVirtualDatabase()
  {
    return dvdb;
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#setScheduler(org.objectweb.cjdbc.controller.scheduler.AbstractScheduler)
   */
  public void setScheduler(AbstractScheduler scheduler)
  {
    super.setScheduler(scheduler);
    // Note: don't try to use this.dvdb here: setScheduler is called by the
    // c'tor, and dvdb is not set at this time.
    if (vdb.getTotalOrderQueue() == null)
      throw new RuntimeException(
          "New scheduler does not support total ordering and is not compatible with distributed virtual databases.");
  }

  //
  // Database Backends management
  //

  /**
   * Enable a backend that has been previously added to this virtual database
   * and that is in the disabled state. We check we the other controllers if
   * this backend must be enabled in read-only or read-write. The current policy
   * is that the first one to enable this backend will have read-write access to
   * it and others will be in read-only.
   * 
   * @param db The database backend to enable
   * @throws SQLException if an error occurs
   */
  public void enableBackend(DatabaseBackend db) throws SQLException
  {
    int size = dvdb.getAllMemberButUs().size();
    if (size > 0)
    {
      logger.debug(Translate
          .get("virtualdatabase.distributed.enable.backend.check"));

      try
      {
        // Notify other controllers that we enable this backend.
        // No answer is expected.
        dvdb.getMulticastRequestAdapter().multicastMessage(
            dvdb.getAllMemberButUs(), new EnableBackend(new BackendInfo(db)),
            MulticastRequestAdapter.WAIT_NONE,
            CJDBCGroupMessage.defaultCastTimeOut);
      }
      catch (Exception e)
      {
        String msg = "Error while enabling backend " + db.getName();
        logger.error(msg, e);
        throw new SQLException(msg + "(" + e + ")");
      }
    }

    super.enableBackend(db);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#disableBackend(org.objectweb.cjdbc.controller.backend.DatabaseBackend)
   */
  public void disableBackend(DatabaseBackend db) throws SQLException
  {
    int size = dvdb.getAllMemberButUs().size();
    if (size > 0)
    {
      logger.debug(Translate.get("virtualdatabase.distributed.disable.backend",
          db.getName()));

      try
      {
        // Notify other controllers that we disable this backend.
        // No answer is expected.
        dvdb.getMulticastRequestAdapter().multicastMessage(
            dvdb.getAllMemberButUs(), new DisableBackend(new BackendInfo(db)),
            MulticastRequestAdapter.WAIT_NONE,
            CJDBCGroupMessage.defaultCastTimeOut);
      }
      catch (Exception e)
      {
        String msg = "Error while disabling backend " + db.getName();
        logger.error(msg, e);
        throw new SQLException(msg + "(" + e + ")");
      }
    }

    super.disableBackend(db);
  }

  /**
   * The backend must belong to this virtual database and be in the enabled
   * state.
   * <p>
   * The backend is disabled once all the pending write queries are executed. A
   * checkpoint is inserted in the recovery log.
   * 
   * @param db The database backend to enable
   * @param checkpointName The checkpoint name to restart from
   * @throws SQLException if an error occurs
   */
  public void disableBackendForCheckpoint(DatabaseBackend db,
      String checkpointName) throws SQLException
  {
    // Sanity checks
    if (recoveryLog == null)
    {
      String msg = Translate.get("recovery.store.checkpoint.failed.cause.null",
          checkpointName);
      logger.error(msg);
      throw new SQLException(msg);
    }

    // Set dvdb-wide (distributed) checkpoint before we disable the backend.
    try
    {
      dvdb.setGroupCheckpoint(checkpointName, dvdb.getAllMembers());
    }
    catch (VirtualDatabaseException e)
    {
      String msg = "set group checkpoint failed";
      logger.error(msg, e);
      throw new SQLException(msg);
    }

    // Signal the backend should not begin any new transaction
    db.setState(BackendState.DISABLING);
    logger.info(Translate.get("backend.state.disabling", db.getName()));

    // Wait for all current transactions on the backend to finish
    db.waitForAllTransactionsToComplete();

    // Now we can safely disable the backend
    db.setLastKnownCheckpoint(checkpointName);
    loadBalancer.disableBackend(db);
    logger.info(Translate.get("backend.state.disabled", db.getName()));

  }

  /**
   * Add a request that failed on all backends.
   * 
   * @param request the request that failed
   * @see #completeFailedOnAllBackends(AbstractRequest, boolean)
   */
  public void addFailedOnAllBackends(AbstractRequest request)
  {
    failedOnAllBackends.add(request);
  }

  /**
   * Notify completion of a request that failed on all backends. If completion
   * was successful, all local backends are disabled.
   * 
   * @param request request that completed
   * @param success true if completion is successful
   * @see #addFailedOnAllBackends(AbstractRequest)
   */
  public void completeFailedOnAllBackends(AbstractRequest request,
      boolean success)
  {
    if (!failedOnAllBackends.remove(request))
    {
      logger.warn("Unable to find request "
          + request.getSQLShortForm(dvdb.getSQLShortFormLength())
          + " in list of requests that failed on all backends.");
      return;
    }
    if (success)
    { // We have to invalidate all backends
      logger
          .error("Request "
              + request.getSQLShortForm(dvdb.getSQLShortFormLength())
              + " failed on all local backends but succeeded on other controllers. Disabling all local backends.");
      try
      {
        dvdb.disableAllBackends();
      }
      catch (VirtualDatabaseException e)
      {
        logger.error("An error occured while disabling all backends", e);
      }
    }
    else
      // Notify scheduler now, the notification was postponed in
      // ExecWriteRequest or ExecWriteRequestWithKeys
      scheduler.notifyWriteCompleted((AbstractWriteRequest) request);
  }

  /**
   * Remove a request that was logged because no backend was available locally
   * to execute it but that finally ended up in failing at all other
   * controllers.
   * 
   * @param request request that was logged but failed at all controllers
   * @param recoveryLogId request identifier in the recovery log on controller
   *          where the request should be unlogged
   */
  public void removeFailedRequestFromRecoveryLog(AbstractWriteRequest request,
      long recoveryLogId)
  {
    if (logger.isDebugEnabled())
      logger.debug("Request "
          + request.getSQLShortForm(dvdb.getSQLShortFormLength())
          + " failed at all controllers, removing it from recovery log.");

    // Now that the request object has been deserialized here, we have our own
    // copy and we can safely override the id. This is not possible on the
    // sender side in DistributedVirtualDatabase.
    request.setId(recoveryLogId);
    recoveryLog.unlogRequest(request);
  }

  /**
   * Remove a stored procedure call that was logged because no backend was
   * available locally to execute it but that finally ended up in failing at all
   * other controllers.
   * 
   * @param proc stored procedure that was logged but failed at all controllers
   */
  public void removeFailedStoredProcedureFromRecoveryLog(StoredProcedure proc)
  {
    if (logger.isDebugEnabled())
      logger.debug("Request "
          + proc.getSQLShortForm(dvdb.getSQLShortFormLength())
          + " failed at all controllers, removing it from recovery log.");

    recoveryLog.unlogRequest(proc);
  }

  /**
   * Remove a commit call that was logged because no backend was available
   * locally to execute it but that finally ended up in failing at all other
   * controllers.
   * 
   * @param tm the identifier of the transaction that failed to commit
   */
  public void removeFailedCommitFromRecoveryLog(TransactionMarkerMetaData tm)
  {
    if (logger.isDebugEnabled())
      logger
          .debug("Transaction "
              + tm.getTransactionId()
              + " commit failed at all controllers, removing it from recovery log.");

    recoveryLog.unlogCommit(tm);
  }

  /**
   * Remove a rollback call that was logged because no backend was available
   * locally to execute it but that finally ended up in failing at all other
   * controllers.
   * 
   * @param tm the identifier of the transaction that failed to rollback
   */
  public void removeFailedRollbackFromRecoveryLog(TransactionMarkerMetaData tm)
  {
    if (logger.isDebugEnabled())
      logger
          .debug("Transaction "
              + tm.getTransactionId()
              + " rollback failed at all controllers, removing it from recovery log.");

    recoveryLog.unlogRollback(tm);
  }

  //
  // Transaction management
  //

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#begin(java.lang.String)
   */
  public long begin(String login) throws SQLException
  {
    try
    {
      TransactionMarkerMetaData tm = new TransactionMarkerMetaData(0,
          beginTimeout, login);

      // Wait for the scheduler to give us the authorization to execute
      long tid = scheduler.begin(tm);
      // 2 first bytes are used for controller id
      // 6 right-most bytes are used for transaction id
      tid = tid & TRANSACTION_ID_BIT_MASK;
      tid = tid | controllerId;
      tm.setTransactionId(tid);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.begin", String.valueOf(tid)));

      try
      {
        // Send to load balancer
        loadBalancer.begin(tm);
      }
      catch (SQLException e)
      {
        throw e;
      }
      finally
      {
        // Notify scheduler for completion in any case
        scheduler.beginCompleted(tid);
      }

      tidLoginTable.put(new Long(tid), tm);
      return tid;
    }
    catch (RuntimeException e)
    {
      logger.fatal(Translate.get(
          "fatal.runtime.exception.requestmanager.begin", e));
      throw new SQLException(e.getMessage());
    }
  }

  /**
   * Check if the transaction corresponding to the given query has been started
   * remotely and start the transaction locally in a lazy manner if needed.
   * 
   * @param request query to execute
   * @throws SQLException if an error occurs
   */
  public void lazyTransactionStart(AbstractRequest request) throws SQLException
  {
    // Check if this is a remotely started transaction that we need to lazyly
    // start locally
    if (!request.isAutoCommit())
    {
      long tid = request.getTransactionId();
      if ((tid & CONTROLLER_ID_BIT_MASK) != controllerId)
      { // Remote transaction, check that it is started
        if (!tidLoginTable.containsKey(new Long(tid)))
        { // Begin this transaction
          try
          {
            TransactionMarkerMetaData tm = new TransactionMarkerMetaData(0,
                beginTimeout, request.getLogin());
            tm.setTransactionId(tid);

            if (logger.isDebugEnabled())
              logger.debug(Translate.get("transaction.begin.lazy", String
                  .valueOf(tid)));

            try
            {
              scheduler.begin(tm);

              // Send to load balancer
              loadBalancer.begin(tm);

              // We need to update the tid table first so that
              // logLazyTransactionBegin can retrieve the metadata
              tidLoginTable.put(new Long(tid), tm);
              if (recoveryLog != null)
                logLazyTransactionBegin(tid);
            }
            catch (SQLException e)
            {
              if (recoveryLog != null)
                // In case logLazyTransactionBegin failed
                tidLoginTable.remove(new Long(tid));
              throw e;
            }
            finally
            {
              // Notify scheduler for completion in any case
              scheduler.beginCompleted(tid);
            }
          }
          catch (RuntimeException e)
          {
            logger.fatal(Translate.get(
                "fatal.runtime.exception.requestmanager.begin", e));
            throw new SQLException(e.getMessage());
          }
        }
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#commit(long,
   *      boolean)
   */
  public void commit(long transactionId, boolean logCommit) throws SQLException
  {
    Long lTid = new Long(transactionId);
    boolean isAWriteTransaction;
    synchronized (distributedTransactions)
    {
      isAWriteTransaction = distributedTransactions.remove(lTid);
    }
    if (isAWriteTransaction)
    {
      TransactionMarkerMetaData tm = getTransactionMarker(lTid);
      distributedCommit(tm.getLogin(), transactionId);
    }
    else
      // read-only transaction, it is local
      super.commit(transactionId, logCommit);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#rollback(long,
   *      boolean)
   */
  public void rollback(long transactionId, boolean logRollback)
      throws SQLException
  {
    Long lTid = new Long(transactionId);
    boolean isAWriteTransaction;
    synchronized (distributedTransactions)
    {
      isAWriteTransaction = distributedTransactions.remove(lTid);
    }
    if (isAWriteTransaction)
    {
      TransactionMarkerMetaData tm = getTransactionMarker(lTid);
      distributedRollback(tm.getLogin(), transactionId);
    }
    else
      // read-only transaction, it is local
      super.rollback(transactionId, logRollback);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#rollback(long,
   *      String)
   */
  public void rollback(long transactionId, String savepointName)
      throws SQLException
  {
    Long lTid = new Long(transactionId);
    boolean isAWriteTransaction;
    synchronized (distributedTransactions)
    {
      isAWriteTransaction = distributedTransactions.contains(lTid);
    }
    if (isAWriteTransaction)
      distributedRollback(transactionId, savepointName);
    else
      // read-only transaction, it is local
      super.rollback(transactionId, savepointName);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#setSavepoint(long)
   */
  public int setSavepoint(long transactionId) throws SQLException
  {
    Long lTid = new Long(transactionId);
    boolean isAWriteTransaction;
    synchronized (distributedTransactions)
    {
      isAWriteTransaction = distributedTransactions.contains(lTid);
    }
    if (isAWriteTransaction)
    {
      int savepointId = scheduler.incrementSavepointId();
      distributedSetSavepoint(transactionId, String.valueOf(savepointId));
      return savepointId;
    }
    else
      // read-only transaction, it is local
      return super.setSavepoint(transactionId);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#setSavepoint(long,
   *      String)
   */
  public void setSavepoint(long transactionId, String name) throws SQLException
  {
    Long lTid = new Long(transactionId);
    boolean isAWriteTransaction;
    synchronized (distributedTransactions)
    {
      isAWriteTransaction = distributedTransactions.contains(lTid);
    }
    if (isAWriteTransaction)
      distributedSetSavepoint(transactionId, name);
    else
      // read-only transaction, it is local
      super.setSavepoint(transactionId, name);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#releaseSavepoint(long,
   *      String)
   */
  public void releaseSavepoint(long transactionId, String name)
      throws SQLException
  {
    Long lTid = new Long(transactionId);
    boolean isAWriteTransaction;
    synchronized (distributedTransactions)
    {
      isAWriteTransaction = distributedTransactions.contains(lTid);
    }
    if (isAWriteTransaction)
      distributedReleaseSavepoint(transactionId, name);
    else
      // read-only transaction, it is local
      super.releaseSavepoint(transactionId, name);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#scheduleExecWriteRequest(org.objectweb.cjdbc.common.sql.AbstractWriteRequest)
   */
  public void scheduleExecWriteRequest(AbstractWriteRequest request)
      throws SQLException
  {
    lazyTransactionStart(request);
    super.scheduleExecWriteRequest(request);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#execReadRequest(org.objectweb.cjdbc.common.sql.SelectRequest)
   */
  public ControllerResultSet execReadRequest(SelectRequest request)
      throws SQLException
  {
    try
    {
      return execLocalReadRequest(request);
    }
    catch (NoMoreBackendException ignored) // other SQLException thrown
    {
      // We have no local backend available to execute the request: try on
      // other controllers.
      // Send our identity to avoid a loop in case of boucing read requests
      // (no backends at all in the system).
      return execRemoteReadRequest(request);
    }
  }

  /**
   * Execute a read request on some remote controller - one in the group. Used
   * when the local controller has no backend available to execute the request.
   * 
   * @param request the request to execute
   * @return the query ResultSet
   * @throws SQLException in case of bad request
   */
  public abstract ControllerResultSet execRemoteReadRequest(
      SelectRequest request) throws SQLException;

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#execWriteRequest(org.objectweb.cjdbc.common.sql.AbstractWriteRequest)
   */
  public int execWriteRequest(AbstractWriteRequest request) throws SQLException
  {
    if (!request.isAutoCommit())
    { // Add this transaction to the list of write transactions
      Long lTid = new Long(request.getTransactionId());
      synchronized (distributedTransactions)
      {
        if (!distributedTransactions.contains(lTid))
          distributedTransactions.add(lTid);
      }
    }
    return execDistributedWriteRequest(request);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#execWriteRequestWithKeys(org.objectweb.cjdbc.common.sql.AbstractWriteRequest)
   */
  public ControllerResultSet execWriteRequestWithKeys(
      AbstractWriteRequest request) throws SQLException
  {
    if (!request.isAutoCommit())
    { // Add this transaction to the list of write transactions
      Long lTid = new Long(request.getTransactionId());
      synchronized (distributedTransactions)
      {
        if (!distributedTransactions.contains(lTid))
          distributedTransactions.add(lTid);
      }
    }
    return execDistributedWriteRequestWithKeys(request);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#execReadStoredProcedure(StoredProcedure)
   */
  public ControllerResultSet execReadStoredProcedure(StoredProcedure proc)
      throws SQLException
  {
    // If connection is read-only, we don't broadcast
    if (proc.isReadOnly())
    {
      try
      {
        return execReadStoredProcedureLocally(proc);
      }
      catch (AllBackendsFailedException ignore)
      {
        // This failed locally, try it remotely
      }
    }

    if (!proc.isAutoCommit())
    { // Add this transaction to the list of write transactions
      Long lTid = new Long(proc.getTransactionId());
      synchronized (distributedTransactions)
      {
        if (!distributedTransactions.contains(lTid))
          distributedTransactions.add(lTid);
      }
    }
    return execDistributedReadStoredProcedure(proc);
  }

  /**
   * @see org.objectweb.cjdbc.controller.requestmanager.RequestManager#execWriteStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public int execWriteStoredProcedure(StoredProcedure proc) throws SQLException
  {
    if (!proc.isAutoCommit())
    { // Add this transaction to the list of write transactions
      Long lTid = new Long(proc.getTransactionId());
      synchronized (distributedTransactions)
      {
        if (!distributedTransactions.contains(lTid))
          distributedTransactions.add(lTid);
      }
    }
    return execDistributedWriteStoredProcedure(proc);
  }

  //
  // RAIDb level specific methods
  //

  /**
   * Distributed implementation of a commit
   * 
   * @param login login that commit the transaction
   * @param transactionId id of the commiting transaction
   * @throws SQLException if an error occurs
   */
  public abstract void distributedCommit(String login, long transactionId)
      throws SQLException;

  /**
   * Distributed implementation of a rollback
   * 
   * @param login login that rollback the transaction
   * @param transactionId id of the rollbacking transaction
   * @throws SQLException if an error occurs
   */
  public abstract void distributedRollback(String login, long transactionId)
      throws SQLException;

  /**
   * Distributed implementation of a rollback to a savepoint
   * 
   * @param transactionId id of the transaction
   * @param savepointName name of the savepoint
   * @throws SQLException if an error occurs
   */
  public abstract void distributedRollback(long transactionId,
      String savepointName) throws SQLException;

  /**
   * Distributed implementation of setting a savepoint to a transaction
   * 
   * @param transactionId id of the transaction
   * @param name name of the savepoint to set
   * @throws SQLException if an error occurs
   */
  public abstract void distributedSetSavepoint(long transactionId, String name)
      throws SQLException;

  /**
   * Distributed implementation of releasing a savepoint from a transaction
   * 
   * @param transactionId id of the transaction
   * @param name name of the savepoint to release
   * @throws SQLException if an error occurs
   */
  public abstract void distributedReleaseSavepoint(long transactionId,
      String name) throws SQLException;

  /**
   * Distributed implementation of a write request execution.
   * 
   * @param request request to execute
   * @return number of modified rows
   * @throws SQLException if an error occurs
   */
  public abstract int execDistributedWriteRequest(AbstractWriteRequest request)
      throws SQLException;

  /**
   * Distributed implementation of a write request execution that returns
   * auto-generated keys.
   * 
   * @param request request to execute
   * @return ResultSet containing the auto-generated keys.
   * @throws SQLException if an error occurs
   */
  public abstract ControllerResultSet execDistributedWriteRequestWithKeys(
      AbstractWriteRequest request) throws SQLException;

  /**
   * Distributed implementation of a read stored procedure execution.
   * 
   * @param proc stored procedure to execute
   * @return ResultSet corresponding to this stored procedure execution
   * @throws SQLException if an error occurs
   */
  public abstract ControllerResultSet execDistributedReadStoredProcedure(
      StoredProcedure proc) throws SQLException;

  /**
   * Distributed implementation of a write stored procedure execution.
   * 
   * @param proc stored procedure to execute
   * @return number of modified rows
   * @throws SQLException if an error occurs
   */
  public abstract int execDistributedWriteStoredProcedure(StoredProcedure proc)
      throws SQLException;

  /**
   * Once the request has been dispatched, it can be executed using the code
   * from <code>RequestManager</code>
   * 
   * @param proc stored procedure to execute
   * @return ResultSet corresponding to this stored procedure execution
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           stored procedure
   * @throws SQLException if an error occurs
   */
  public ControllerResultSet execReadStoredProcedureLocally(StoredProcedure proc)
      throws AllBackendsFailedException, SQLException
  {
    return super.execReadStoredProcedure(proc);
  }

  /**
   * Once the request has been dispatched, it can be executed using the code
   * from <code>RequestManager</code>
   * 
   * @param proc stored procedure to execute
   * @return number of modified rows
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           stored procedure
   * @throws SQLException if an error occurs
   */
  public int execDistributedWriteStoredProcedureLocally(StoredProcedure proc)
      throws AllBackendsFailedException, SQLException
  {
    return super.execWriteStoredProcedure(proc);
  }

  /**
   * Performs a local read operation, as opposed to execReadRequest() which
   * attempts to use distributed reads when there is NoMoreBackendException.
   * 
   * @param request the read request to perform
   * @return a ControllerResultSet
   * @throws NoMoreBackendException when no more local backends are available to
   *           execute the request
   * @throws SQLException in case of error
   */
  public ControllerResultSet execLocalReadRequest(SelectRequest request)
      throws NoMoreBackendException, SQLException
  {
    return super.execReadRequest(request);
  }

}