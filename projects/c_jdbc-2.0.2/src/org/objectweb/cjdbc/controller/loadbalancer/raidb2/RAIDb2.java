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

package org.objectweb.cjdbc.controller.loadbalancer.raidb2;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import org.objectweb.cjdbc.common.exceptions.BadConnectionException;
import org.objectweb.cjdbc.common.exceptions.NoTransactionStartWhenDisablingException;
import org.objectweb.cjdbc.common.exceptions.SQLExceptionFactory;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.util.ReadPrioritaryFIFOWriteLock;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.AllBackendsFailedException;
import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;
import org.objectweb.cjdbc.controller.loadbalancer.policies.WaitForCompletionPolicy;
import org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTableException;
import org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTablePolicy;
import org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTableRule;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.AbstractTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.CommitTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.KillThreadTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.ReadStoredProcedureTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.ReleaseSavepointTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.RollbackTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.RollbackToSavepointTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.SavepointTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.WriteRequestTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.WriteRequestWithKeysTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.WriteStoredProcedureTask;
import org.objectweb.cjdbc.controller.requestmanager.RAIDbLevels;
import org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.Commit;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.ReleaseSavepoint;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.Rollback;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.RollbackToSavepoint;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.SetSavepoint;

/**
 * RAIDb-2 load balancer.
 * <p>
 * This class is an abstract call because the read requests coming from the
 * Request Manager are NOT treated here but in the subclasses. Transaction
 * management and write requests are broadcasted to all backends owning the
 * written table.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public abstract class RAIDb2 extends AbstractLoadBalancer
{
  //
  // How the code is organized ?
  // 1. Member variables
  // 2. Constructor(s)
  // 3. Request handling
  // 4. Transaction handling
  // 5. Backend management
  //

  protected ArrayList                   backendBlockingThreads;
  protected ArrayList                   backendNonBlockingThreads;
  protected ReadPrioritaryFIFOWriteLock backendBlockingThreadsRWLock    = new ReadPrioritaryFIFOWriteLock();
  protected ReadPrioritaryFIFOWriteLock backendNonBlockingThreadsRWLock = new ReadPrioritaryFIFOWriteLock();
  // Should we wait for all backends to commit before returning ?
  protected WaitForCompletionPolicy     waitForCompletionPolicy;
  protected CreateTablePolicy           createTablePolicy;

  protected static Trace                logger                          = Trace
                                                                            .getLogger("org.objectweb.cjdbc.controller.loadbalancer.raidb2");

  /*
   * Constructors
   */

  /**
   * Creates a new RAIDb-1 Round Robin request load balancer. A new backend
   * worker thread is created for each backend.
   * 
   * @param vdb the virtual database this load balancer belongs to.
   * @param waitForCompletionPolicy how many backends must complete before
   *          returning the result ?
   * @param createTablePolicy the policy defining how 'create table' statements
   *          should be handled
   * @exception Exception if an error occurs
   */
  public RAIDb2(VirtualDatabase vdb,
      WaitForCompletionPolicy waitForCompletionPolicy,
      CreateTablePolicy createTablePolicy) throws Exception
  {
    super(vdb, RAIDbLevels.RAIDb2, ParsingGranularities.TABLE);

    this.waitForCompletionPolicy = waitForCompletionPolicy;
    backendBlockingThreads = new ArrayList();
    backendNonBlockingThreads = new ArrayList();
    this.createTablePolicy = createTablePolicy;
  }

  /*
   * Request Handling
   */

  /**
   * Returns the number of nodes to wait for according to the defined
   * <code>waitForCompletion</code> policy.
   * 
   * @param nbOfThreads total number of threads
   * @return int number of threads to wait for
   */
  private int getNbToWait(int nbOfThreads)
  {
    int nbToWait;
    switch (waitForCompletionPolicy.getPolicy())
    {
      case WaitForCompletionPolicy.FIRST :
        nbToWait = 1;
        break;
      case WaitForCompletionPolicy.MAJORITY :
        nbToWait = nbOfThreads / 2 + 1;
        break;
      case WaitForCompletionPolicy.ALL :
        nbToWait = nbOfThreads;
        break;
      default :
        logger
            .warn(Translate.get("loadbalancer.waitforcompletion.unsupported"));
        nbToWait = nbOfThreads;
        break;
    }
    return nbToWait;
  }

  /**
   * Performs a write request. This request is broadcasted to all nodes that
   * owns the table to be written.
   * 
   * @param request an <code>AbstractWriteRequest</code>
   * @return number of rows affected by the request
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @exception SQLException if an error occurs
   */
  public int execWriteRequest(AbstractWriteRequest request)
      throws AllBackendsFailedException, SQLException
  {
    return ((WriteRequestTask) execWriteRequest(request, false, null))
        .getResult();
  }

  /**
   * Perform a write request and return the auto generated keys.
   * 
   * @param request the request to execute
   * @param metadataCache the metadataCache if any or null
   * @return auto generated keys.
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @exception SQLException if an error occurs
   */
  public ControllerResultSet execWriteRequestWithKeys(
      AbstractWriteRequest request, MetadataCache metadataCache)
      throws AllBackendsFailedException, SQLException
  {
    return ((WriteRequestWithKeysTask) execWriteRequest(request, true,
        metadataCache)).getResult();
  }

  /**
   * Common code for execWriteRequest(AbstractWriteRequest) and
   * execWriteRequestWithKeys(AbstractWriteRequest).
   * <p>
   * Note that macros are processed here.
   * <p>
   * The result is given back in AbstractTask.getResult().
   * 
   * @param request the request to execute
   * @param useKeys true if this must give an auto generated keys ResultSet
   * @param metadataCache the metadataCache if any or null
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @throws SQLException if an error occurs
   */
  private AbstractTask execWriteRequest(AbstractWriteRequest request,
      boolean useKeys, MetadataCache metadataCache)
      throws AllBackendsFailedException, SQLException
  {
    ArrayList backendThreads;
    ReadPrioritaryFIFOWriteLock lock;

    // Total ordering mainly for distributed virtual databases.
    // If waitForTotalOrder returns true then the query has been scheduled in
    // total order and there is no need to take a write lock later to resolve
    // potential conflicts.
    boolean canTakeReadLock = waitForTotalOrder(request, true);

    // Handle macros
    handleMacros(request);

    // Determine which list (blocking or not) to use
    if (request.mightBlock())
    { // Blocking
      backendThreads = backendBlockingThreads;
      lock = backendBlockingThreadsRWLock;
    }
    else
    { // Non-blocking
      backendThreads = backendNonBlockingThreads;
      lock = backendNonBlockingThreadsRWLock;
      if ((waitForCompletionPolicy.getPolicy() != WaitForCompletionPolicy.ALL)
          && (request.getTransactionId() != 0))
        waitForAllWritesToComplete(request.getTransactionId());
    }

    try
    {
      if (canTakeReadLock)
        lock.acquireRead();
      else
        lock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    int nbOfThreads = backendThreads.size();
    ArrayList writeList = new ArrayList();
    String tableName = request.getTableName();

    if (request.isCreate())
    { // Choose the backend according to the defined policy
      CreateTableRule rule = createTablePolicy.getTableRule(request
          .getTableName());
      if (rule == null)
        rule = createTablePolicy.getDefaultRule();

      // Ask the rule to pickup the backends
      ArrayList chosen;
      try
      {
        chosen = rule.getBackends(vdb.getBackends());
      }
      catch (CreateTableException e)
      {
        throw new SQLException(Translate.get(
            "loadbalancer.create.table.rule.failed", e.getMessage()));
      }

      // Build the thread list from the backend list
      if (chosen != null)
      {
        for (int i = 0; i < nbOfThreads; i++)
        {
          BackendWorkerThread thread = (BackendWorkerThread) backendThreads
              .get(i);
          if (chosen.contains(thread.getBackend()))
            writeList.add(thread);
        }
      }
    }
    else
    { // Build the list of backends that need to execute this request
      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) backendThreads
            .get(i);
        if (thread.getBackend().hasTable(tableName))
          writeList.add(thread);
      }
    }

    nbOfThreads = writeList.size();
    if (nbOfThreads == 0)
    {
      if (canTakeReadLock)
        lock.releaseRead();
      else
        lock.releaseWrite();

      String msg = Translate.get("loadbalancer.execute.no.backend.found",
          request.getSQLShortForm(vdb.getSQLShortFormLength()));
      logger.warn(msg);

      // Unblock next query from total order queue
      removeHeadFromAndNotifyTotalOrderQueue();
      throw new SQLException(msg);
    }
    else
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("loadbalancer.execute.on.several",
            new String[]{String.valueOf(request.getId()),
                String.valueOf(nbOfThreads)}));
    }

    // Create the task
    AbstractTask task;
    if (useKeys)
      task = new WriteRequestWithKeysTask(getNbToWait(nbOfThreads),
          nbOfThreads, request, metadataCache);
    else
      task = new WriteRequestTask(getNbToWait(nbOfThreads), nbOfThreads,
          request);

    // We have to first post the request on each backend before letting the
    // first backend to execute the request. Therefore we have 2 phases:
    // 1. post the task in each thread queue
    // 2. notify each thread to execute the query

    // 1. Post the task
    if (request.isAutoCommit())
    {
      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) writeList.get(i);
        synchronized (thread)
        {
          thread.addTask(task);
        }
      }
    }
    else
    {
      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) writeList.get(i);
        synchronized (thread)
        {
          thread.addTask(task, request.getTransactionId());
        }
      }
    }

    // 2. Start the task execution on each backend
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) writeList.get(i);
      synchronized (thread)
      {
        thread.notify();
      }
    }

    // Release lock
    if (canTakeReadLock)
      lock.releaseRead();
    else
      lock.releaseWrite();

    // Unblock next query from total order queue
    removeHeadFromAndNotifyTotalOrderQueue();

    synchronized (task)
    {
      if (!task.hasCompleted())
      {
        // Wait for completion (notified by the task)
        try
        {
          // Wait on task
          long timeout = request.getTimeout() * 1000;
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            task.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining <= 0)
            {
              if (task.setExpiredTimeout())
              { // Task will be ignored by all backends
                String msg = Translate.get("loadbalancer.request.timeout",
                    new String[]{String.valueOf(request.getId()),
                        String.valueOf(task.getSuccess()),
                        String.valueOf(task.getFailed())});
                logger.warn(msg);
                throw new SQLException(msg);
              }
              // else task execution already started, to late to cancel
            }
            // No need to update request timeout since the execution is finished
          }
          else
            task.wait();
        }
        catch (InterruptedException e)
        {
          if (task.setExpiredTimeout())
          { // Task will be ignored by all backends
            String msg = Translate.get("loadbalancer.request.timeout",
                new String[]{String.valueOf(request.getId()),
                    String.valueOf(task.getSuccess()),
                    String.valueOf(task.getFailed())});
            logger.warn(msg);
            throw new SQLException(msg);
          }
          // else task execution already started, to late to cancel
        }
      }

      if (task.getSuccess() > 0)
        return task;
      else
      { // All tasks failed
        ArrayList exceptions = task.getExceptions();
        if (exceptions == null)
          throw new AllBackendsFailedException(Translate.get(
              "loadbalancer.request.failed.all", request.getId()));
        else
        {
          String errorMsg = Translate.get("loadbalancer.request.failed.stack",
              request.getId())
              + "\n";
          for (int i = 0; i < exceptions.size(); i++)
            errorMsg += ((SQLException) exceptions.get(i)).getMessage() + "\n";
          logger.error(errorMsg);
          throw new SQLException(errorMsg);
        }
      }
    }
  }

  /**
   * Implementation specific load balanced read execution.
   * 
   * @param request an <code>SelectRequest</code>
   * @param metadataCache the metadataCache if any or null
   * @return the corresponding <code>java.sql.ResultSet</code>
   * @exception SQLException if an error occurs
   */
  public abstract ControllerResultSet execReadRequest(SelectRequest request,
      MetadataCache metadataCache) throws SQLException;

  /**
   * Execute a read request on the selected backend.
   * 
   * @param request the request to execute
   * @param backend the backend that will execute the request
   * @param metadataCache a metadataCache if any or null
   * @return the ResultSet
   * @throws SQLException if an error occurs
   */
  protected ControllerResultSet executeRequestOnBackend(SelectRequest request,
      DatabaseBackend backend, MetadataCache metadataCache)
      throws SQLException, UnreachableBackendException
  {
    // Handle macros
    handleMacros(request);

    // Ok, we have a backend, let's execute the request
    AbstractConnectionManager cm = backend.getConnectionManager(request
        .getLogin());

    // Sanity check
    if (cm == null)
    {
      String msg = Translate.get("loadbalancer.connectionmanager.not.found",
          new String[]{request.getLogin(), backend.getName()});
      logger.error(msg);
      throw new SQLException(msg);
    }

    // Execute the query
    if (request.isAutoCommit())
    {
      if (waitForCompletionPolicy.getPolicy() != WaitForCompletionPolicy.ALL)
        // We could do something finer grain here by waiting
        // only for writes that depend on the tables we need
        // but is that really worth the overhead ?
        waitForAllWritesToComplete(backend);

      ControllerResultSet rs = null;
      boolean badConnection;
      do
      {
        badConnection = false;
        // Use a connection just for this request
        Connection c = null;
        try
        {
          c = cm.getConnection();
        }
        catch (UnreachableBackendException e1)
        {
          logger.error(Translate.get(
              "loadbalancer.backend.disabling.unreachable", backend.getName()));
          disableBackend(backend);
          throw new UnreachableBackendException(Translate.get(
              "loadbalancer.backend.unreacheable", backend.getName()));
        }

        // Sanity check
        if (c == null)
          throw new UnreachableBackendException(
              "No more connections on backend " + backend.getName());

        // Execute Query
        try
        {
          rs = executeSelectRequestOnBackend(request, backend, c, metadataCache);
          cm.releaseConnection(c);
        }
        catch (SQLException e)
        {
          cm.releaseConnection(c);
          throw new SQLException(Translate.get(
              "loadbalancer.request.failed.on.backend", new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName(), e.getMessage()}));
        }
        catch (BadConnectionException e)
        { // Get rid of the bad connection
          cm.deleteConnection(c);
          badConnection = true;
        }
      }
      while (badConnection);
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("loadbalancer.execute.on", new String[]{
            String.valueOf(request.getId()), backend.getName()}));
      return rs;
    }
    else
    { // Inside a transaction
      Connection c;
      long tid = request.getTransactionId();
      Long lTid = new Long(tid);

      // Wait for previous writes to complete
      if (waitForCompletionPolicy.getPolicy() != WaitForCompletionPolicy.ALL)
        waitForAllWritesToComplete(backend, request.getTransactionId());

      try
      {
        c = backend.getConnectionForTransactionAndLazyBeginIfNeeded(lTid, cm,
            request.getTransactionIsolation());
      }
      catch (UnreachableBackendException e1)
      {
        logger.error(Translate.get(
            "loadbalancer.backend.disabling.unreachable", backend.getName()));
        disableBackend(backend);
        throw new SQLException(Translate.get(
            "loadbalancer.backend.unreacheable", backend.getName()));
      }
      catch (NoTransactionStartWhenDisablingException e)
      {
        String msg = Translate.get("loadbalancer.backend.is.disabling",
            new String[]{request.getSQLShortForm(vdb.getSQLShortFormLength()),
                backend.getName()});
        logger.error(msg);
        throw new SQLException(msg);
      }

      // Sanity check
      if (c == null)
        throw new SQLException(Translate.get(
            "loadbalancer.unable.retrieve.connection", new String[]{
                String.valueOf(tid), backend.getName()}));

      // Execute Query
      ControllerResultSet rs = null;
      try
      {
        rs = executeSelectRequestOnBackend(request, backend, c, metadataCache);
      }
      catch (SQLException e)
      {
        throw new SQLException(Translate.get(
            "loadbalancer.request.failed.on.backend", new String[]{
                request.getSQLShortForm(vdb.getSQLShortFormLength()),
                backend.getName(), e.getMessage()}));
      }
      catch (BadConnectionException e)
      { // Connection failed, so did the transaction
        // Disable the backend.
        cm.deleteConnection(tid);
        String msg = Translate.get(
            "loadbalancer.backend.disabling.connection.failure", backend
                .getName());
        logger.error(msg);
        disableBackend(backend);
        throw new SQLException(msg);
      }
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("loadbalancer.execute.transaction.on",
            new String[]{String.valueOf(tid), String.valueOf(request.getId()),
                backend.getName()}));
      return rs;
    }
  }

  /**
   * Execute a stored procedure on the selected backend.
   * 
   * @param proc the stored procedure to execute
   * @param backend the backend that will execute the request
   * @param metadataCache the metadataCache if any or null
   * @return the ResultSet
   * @throws SQLException if an error occurs
   */
  protected ControllerResultSet executeStoredProcedureOnBackend(
      StoredProcedure proc, DatabaseBackend backend, MetadataCache metadataCache)
      throws SQLException, UnreachableBackendException
  {
    // Ok, we have a backend, let's execute the request
    AbstractConnectionManager cm = backend
        .getConnectionManager(proc.getLogin());

    // Sanity check
    if (cm == null)
    {
      String msg = Translate.get("loadbalancer.connectionmanager.not.found",
          new String[]{proc.getLogin(), backend.getName()});
      logger.error(msg);
      throw new SQLException(msg);
    }

    // Execute the query
    if (proc.isAutoCommit())
    {
      if (waitForCompletionPolicy.getPolicy() != WaitForCompletionPolicy.ALL)
        // We could do something finer grain here by waiting
        // only for writes that depend on the tables we need
        // but is that really worth the overhead ?
        waitForAllWritesToComplete(backend);

      // Use a connection just for this request
      Connection c = null;
      try
      {
        c = cm.getConnection();
      }
      catch (UnreachableBackendException e1)
      {
        logger.error(Translate.get(
            "loadbalancer.backend.disabling.unreachable", backend.getName()));
        disableBackend(backend);
        throw new UnreachableBackendException(Translate.get(
            "loadbalancer.backend.unreacheable", backend.getName()));
      }

      // Sanity check
      if (c == null)
        throw new SQLException(Translate.get(
            "loadbalancer.backend.no.connection", backend.getName()));

      // Execute Query
      ControllerResultSet rs = null;
      try
      {
        rs = AbstractLoadBalancer.executeReadStoredProcedureOnBackend(proc,
            backend, c, metadataCache);
      }
      catch (Exception e)
      {
        throw new SQLException(Translate.get(
            "loadbalancer.storedprocedure.failed.on.backend", new String[]{
                proc.getSQLShortForm(vdb.getSQLShortFormLength()),
                backend.getName(), e.getMessage()}));
      }
      finally
      {
        cm.releaseConnection(c);
      }
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("loadbalancer.storedprocedure.on",
            new String[]{String.valueOf(proc.getId()), backend.getName()}));
      return rs;
    }
    else
    { // Inside a transaction
      Connection c;
      long tid = proc.getTransactionId();
      Long lTid = new Long(tid);

      // Wait for previous writes to complete
      if (waitForCompletionPolicy.getPolicy() != WaitForCompletionPolicy.ALL)
        waitForAllWritesToComplete(backend, proc.getTransactionId());

      try
      {
        c = backend.getConnectionForTransactionAndLazyBeginIfNeeded(lTid, cm,
            proc.getTransactionIsolation());
      }
      catch (UnreachableBackendException e1)
      {
        logger.error(Translate.get(
            "loadbalancer.backend.disabling.unreachable", backend.getName()));
        disableBackend(backend);
        throw new SQLException(Translate.get(
            "loadbalancer.backend.unreacheable", backend.getName()));
      }
      catch (NoTransactionStartWhenDisablingException e)
      {
        String msg = Translate.get("loadbalancer.backend.is.disabling",
            new String[]{proc.getSQLShortForm(vdb.getSQLShortFormLength()),
                backend.getName()});
        logger.error(msg);
        throw new SQLException(msg);
      }

      // Sanity check
      if (c == null)
        throw new SQLException(Translate.get(
            "loadbalancer.unable.retrieve.connection", new String[]{
                String.valueOf(tid), backend.getName()}));

      // Execute Query
      ControllerResultSet rs;
      try
      {
        rs = AbstractLoadBalancer.executeReadStoredProcedureOnBackend(proc,
            backend, c, metadataCache);
      }
      catch (Exception e)
      {
        throw new SQLException(Translate.get(
            "loadbalancer.storedprocedure.failed.on.backend", new String[]{
                proc.getSQLShortForm(vdb.getSQLShortFormLength()),
                backend.getName(), e.getMessage()}));
      }
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("loadbalancer.execute.transaction.on",
            new String[]{String.valueOf(tid), String.valueOf(proc.getId()),
                backend.getName()}));
      return rs;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadStoredProcedure(StoredProcedure,
   *      MetadataCache)
   */
  public ControllerResultSet execReadStoredProcedure(StoredProcedure proc,
      MetadataCache metadataCache) throws SQLException
  {
    ReadStoredProcedureTask task = (ReadStoredProcedureTask) callStoredProcedure(
        proc, true, metadataCache);
    return task.getResult();
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execWriteStoredProcedure(StoredProcedure)
   */
  public int execWriteStoredProcedure(StoredProcedure proc) throws SQLException
  {
    WriteStoredProcedureTask task = (WriteStoredProcedureTask) callStoredProcedure(
        proc, false, null);
    return task.getResult();
  }

  /**
   * Post the stored procedure call in the threads task list.
   * <p>
   * Note that macros are processed here.
   * 
   * @param proc the stored procedure to call
   * @param isRead true if the call returns a ResultSet
   * @param metadataCache the metadataCache if any or null
   * @return the task that has been executed (caller can get the result by
   *         calling getResult())
   * @throws SQLException if an error occurs
   */
  private AbstractTask callStoredProcedure(StoredProcedure proc,
      boolean isRead, MetadataCache metadataCache) throws SQLException
  {
    ArrayList backendThreads = backendBlockingThreads;
    ReadPrioritaryFIFOWriteLock lock = backendBlockingThreadsRWLock;

    // Total ordering mainly for distributed virtual databases.
    // If waitForTotalOrder returns true then the query has been scheduled in
    // total order and there is no need to take a write lock later to resolve
    // potential conflicts.
    boolean canTakeReadLock = waitForTotalOrder(proc, true);

    // Handle macros
    handleMacros(proc);

    try
    {
      // Note that a read stored procedure here is supposed to also execute
      // writes and as the scheduler cannot block atomically on multiple tables
      // for a writes, we have to lock as a write even for a read stored
      // procedure. A read-only stored procedure will execute in a separate
      // method call (see executeReadOnlyStoredProcedure).
      if (canTakeReadLock)
        lock.acquireRead();
      else
        lock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg;
      msg = Translate.get("loadbalancer.backendlist.acquire.writelock.failed",
          e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    int nbOfThreads = backendThreads.size();

    // Create the task
    AbstractTask task;
    if (isRead)
      task = new ReadStoredProcedureTask(getNbToWait(nbOfThreads), nbOfThreads,
          proc, metadataCache);
    else
      task = new WriteStoredProcedureTask(getNbToWait(nbOfThreads),
          nbOfThreads, proc);

    int nbOfBackends = 0;

    // Post the task in each backendThread tasklist and wakeup the threads
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendThreads.get(i);
      if (thread.getBackend().hasStoredProcedure(proc.getProcedureName()))
      {
        nbOfBackends++;
        synchronized (thread)
        {
          if (proc.isAutoCommit())
            thread.addTask(task);
          else
            thread.addTask(task, proc.getTransactionId());
          thread.notify();
        }
      }
    }

    if (canTakeReadLock)
      lock.releaseRead();
    else
      lock.releaseWrite();

    // Unblock next query from total order queue
    removeHeadFromAndNotifyTotalOrderQueue();

    if (nbOfBackends == 0)
    {
      throw new SQLException(Translate.get(
          "loadbalancer.backend.no.required.storedprocedure", proc
              .getProcedureName()));
    }
    else
      task.setTotalNb(nbOfBackends);

    synchronized (task)
    {
      if (!task.hasCompleted())
      {
        // Wait for completion (notified by the task)
        try
        {
          // Wait on task
          long timeout = proc.getTimeout() * 1000;
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            task.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining <= 0)
            {
              if (task.setExpiredTimeout())
              { // Task will be ignored by all backends
                String msg = Translate.get(
                    "loadbalancer.storedprocedure.timeout", new String[]{
                        String.valueOf(proc.getId()),
                        String.valueOf(task.getSuccess()),
                        String.valueOf(task.getFailed())});
                logger.warn(msg);
                throw new SQLException(msg);
              }
              // else task execution already started, to late to cancel
            }
            // No need to update request timeout since the execution is finished
          }
          else
            task.wait();
        }
        catch (InterruptedException e)
        {
          if (task.setExpiredTimeout())
          { // Task will be ignored by all backends
            String msg = Translate.get("loadbalancer.storedprocedure.timeout",
                new String[]{String.valueOf(proc.getId()),
                    String.valueOf(task.getSuccess()),
                    String.valueOf(task.getFailed())});
            logger.warn(msg);
            throw new SQLException(msg);
          }
          // else task execution already started, to late to cancel
        }
      }

      if (task.getSuccess() > 0)
        return task;
      else
      { // All tasks failed
        ArrayList exceptions = task.getExceptions();
        if (exceptions == null)
          throw new SQLException(Translate.get(
              "loadbalancer.storedprocedure.all.failed", proc.getId()));
        else
        {
          String errorMsg = Translate.get(
              "loadbalancer.storedprocedure.failed.stack", proc.getId())
              + "\n";
          for (int i = 0; i < exceptions.size(); i++)
            errorMsg += ((SQLException) exceptions.get(i)).getMessage() + "\n";
          logger.error(errorMsg);
          throw new SQLException(errorMsg);
        }
      }
    }
  }

  /*
   * Transaction management
   */

  /**
   * Begins a new transaction.
   * 
   * @param tm the transaction marker metadata
   * @exception SQLException if an error occurs
   */
  public final void begin(TransactionMarkerMetaData tm) throws SQLException
  {
  }

  /**
   * Commits a transaction.
   * 
   * @param tm the transaction marker metadata
   * @exception SQLException if an error occurs
   */
  public void commit(TransactionMarkerMetaData tm) throws SQLException
  {
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);
    // List of backends that still have pending queries for the transaction to
    // commit
    ArrayList asynchronousBackends = null;
    CommitTask task = null;

    // Ordering for distributed virtual database
    Commit totalOrderCommit = null;
    boolean canTakeReadLock = false;
    if (vdb.getTotalOrderQueue() != null)
    {
      totalOrderCommit = new Commit(tm.getLogin(), tid);
      // Total ordering mainly for distributed virtual databases.
      // If waitForTotalOrder returns true then the query has been scheduled in
      // total order and there is no need to take a write lock later to resolve
      // potential conflicts.
      canTakeReadLock = waitForTotalOrder(totalOrderCommit, false);
      if (!canTakeReadLock)
        // This is a local commit no total order info
        totalOrderCommit = null;
    }

    if (waitForCompletionPolicy.getPolicy() != WaitForCompletionPolicy.ALL)
    { // Insert commit after last write
      try
      {
        if (canTakeReadLock)
          backendBlockingThreadsRWLock.acquireRead();
        else
        {
          // Lock in write to ensure that all writes are posted and we wait in
          // the queue, else a read lock has the priority with the
          // implementation we are using.
          backendBlockingThreadsRWLock.acquireWrite();
        }
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get(
            "loadbalancer.backendlist.acquire.writelock.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }

      int nbOfThreads = backendBlockingThreads.size();
      // Create the task
      task = new CommitTask(getNbToWait(nbOfThreads), nbOfThreads, tm
          .getTimeout(), tm.getLogin(), tid);

      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) backendBlockingThreads
            .get(i);
        if (thread.hasTaskForTransaction(lTid))
        {
          if (asynchronousBackends == null)
            asynchronousBackends = new ArrayList();
          asynchronousBackends.add(thread.getBackend());
          synchronized (thread)
          {
            thread.insertTaskAfterLastWriteForTransaction(task, lTid);
            thread.notify();
          }
        }
      }

      if (canTakeReadLock)
        backendBlockingThreadsRWLock.releaseRead();
      else
        backendBlockingThreadsRWLock.releaseWrite();

      // Unset the task
      if (asynchronousBackends == null)
        task = null;
    }

    try
    {
      if (canTakeReadLock)
        backendNonBlockingThreadsRWLock.acquireRead();
      else
      {
        // Lock in write to ensure that all writes are posted and we wait in
        // the queue, else a read lock has the priority with the
        // implementation we are using.
        backendNonBlockingThreadsRWLock.acquireWrite();
      }
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    int nbOfThreads = backendNonBlockingThreads.size();
    ArrayList commitList = new ArrayList();

    // Build the list of backends that need to commit this transaction
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendNonBlockingThreads
          .get(i);
      DatabaseBackend backend = thread.getBackend();
      // If the transaction has been started on this backend and it was not
      // previously treated in the asynchronous backend list (late nodes), then
      // we have to post the task now in the asynchronous list.
      if (backend.isStartedTransaction(lTid)
          && ((asynchronousBackends == null) || (!asynchronousBackends
              .contains(backend))))
        commitList.add(thread);
    }

    // If no backend was late and the commit task has not been posted to any
    // backend yet, then we have to create a task for the backends that really
    // need to commit the transaction (in the blocking queue).
    // Backends for which we have to post in the non blocking queue
    int nbOfThreadsToCommit = commitList.size();
    if ((task == null) && (nbOfThreadsToCommit != 0))
      task = new CommitTask(getNbToWait(nbOfThreadsToCommit),
          nbOfThreadsToCommit, tm.getTimeout(), tm.getLogin(), tid);

    // Post the task in each backendThread tasklist and wakeup the threads. This
    // could either be the remaining threads that were not in the asynchronous
    // queue or all the backends that started the transaction.
    for (int i = 0; i < nbOfThreadsToCommit; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) commitList.get(i);
      synchronized (thread)
      {
        thread.addTask(task, tid);
        thread.notify();
      }
    }

    if (canTakeReadLock)
      backendNonBlockingThreadsRWLock.releaseRead();
    else
      backendNonBlockingThreadsRWLock.releaseWrite();

    // Unblock next query from total order queue
    if (totalOrderCommit != null)
      removeHeadFromAndNotifyTotalOrderQueue();

    // Check if someone had something to commit
    if (task == null)
      return;

    synchronized (task)
    {
      if (!task.hasCompleted())
      {
        // Wait for completion (notified by the task)
        try
        {
          // Wait on task
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            task.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining <= 0)
            {
              if (task.setExpiredTimeout())
              { // Task will be ignored by all backends
                String msg = Translate.get("loadbalancer.commit.timeout",
                    new String[]{String.valueOf(tid),
                        String.valueOf(task.getSuccess()),
                        String.valueOf(task.getFailed())});
                logger.warn(msg);
                throw new SQLException(msg);
              }
              // else task execution already started, too late to cancel
            }
          }
          else
            task.wait();
        }
        catch (InterruptedException e)
        {
          if (task.setExpiredTimeout())
          { // Task will be ignored by all backends
            String msg = Translate.get("loadbalancer.commit.timeout",
                new String[]{String.valueOf(tid),
                    String.valueOf(task.getSuccess()),
                    String.valueOf(task.getFailed())});
            logger.warn(msg);
            throw new SQLException(msg);
          }
          // else task execution already started, too late to cancel
        }
      }

      if (task.getSuccess() > 0)
        return;
      else
      { // All tasks failed
        ArrayList exceptions = task.getExceptions();
        if (exceptions == null)
          throw new SQLException(Translate.get(
              "loadbalancer.commit.all.failed", tid));
        else
        {
          String errorMsg = Translate.get("loadbalancer.commit.failed.stack",
              tid)
              + "\n";
          for (int i = 0; i < exceptions.size(); i++)
            errorMsg += ((SQLException) exceptions.get(i)).getMessage() + "\n";
          logger.error(errorMsg);
          throw new SQLException(errorMsg);
        }
      }
    }
  }

  /**
   * Rollbacks a transaction.
   * 
   * @param tm the transaction marker metadata
   * @exception SQLException if an error occurs
   */
  public void rollback(TransactionMarkerMetaData tm) throws SQLException
  {
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);
    // List of backends that still have pending queries for the transaction to
    // rollback
    ArrayList asynchronousBackends = null;
    RollbackTask task = null;

    // Ordering for distributed virtual database
    Rollback totalOrderRollback = null;
    boolean canTakeReadLock = false;
    if (vdb.getTotalOrderQueue() != null)
    {
      totalOrderRollback = new Rollback(tm.getLogin(), tid);
      // Total ordering mainly for distributed virtual databases.
      // If waitForTotalOrder returns true then the query has been scheduled in
      // total order and there is no need to take a write lock later to resolve
      // potential conflicts.
      canTakeReadLock = waitForTotalOrder(totalOrderRollback, false);
      if (!canTakeReadLock)
        // This is a local rollback no total order info
        totalOrderRollback = null;
    }

    if (waitForCompletionPolicy.getPolicy() != WaitForCompletionPolicy.ALL)
    {
      try
      {
        if (canTakeReadLock)
          backendBlockingThreadsRWLock.acquireRead();
        else
        {
          // Lock in write to ensure that all writes are posted and we wait in
          // the queue, else a read lock has the priority with the
          // implementation we are using.
          backendBlockingThreadsRWLock.acquireWrite();
        }
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get(
            "loadbalancer.backendlist.acquire.writelock.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }

      int nbOfThreads = backendBlockingThreads.size();
      // Create the task
      task = new RollbackTask(getNbToWait(nbOfThreads), nbOfThreads, tm
          .getTimeout(), tm.getLogin(), tid);

      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) backendBlockingThreads
            .get(i);
        if (thread.hasTaskForTransaction(lTid))
        {
          if (asynchronousBackends == null)
            asynchronousBackends = new ArrayList();
          asynchronousBackends.add(thread.getBackend());
          synchronized (thread)
          {
            thread.insertTaskAfterLastWriteForTransaction(task, lTid);
            thread.notify();
          }
        }
      }

      if (canTakeReadLock)
        backendBlockingThreadsRWLock.releaseRead();
      else
        backendBlockingThreadsRWLock.releaseWrite();

      // Unset the task
      if (asynchronousBackends == null)
        task = null;
    }

    try
    {
      if (canTakeReadLock)
        backendNonBlockingThreadsRWLock.acquireRead();
      else
        backendNonBlockingThreadsRWLock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    int nbOfThreads = backendNonBlockingThreads.size();
    ArrayList rollbackList = new ArrayList();

    // Build the list of backend that need to rollback this transaction
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendNonBlockingThreads
          .get(i);
      DatabaseBackend backend = thread.getBackend();
      // If the transaction has been started on this backend and it was not
      // previously treated in the asynchronous backend list (late nodes), then
      // we have to post the task now in the asynchronous list.
      if (backend.isStartedTransaction(lTid)
          && ((asynchronousBackends == null) || (!asynchronousBackends
              .contains(backend))))
        rollbackList.add(thread);
    }

    int nbOfThreadsToRollback = rollbackList.size();
    // If no backend was late and the rollback task has not been posted to any
    // backend yet, then we have to create a task for the backends that really
    // need to rollback the transaction.
    if ((task == null) && (nbOfThreadsToRollback != 0))
      task = new RollbackTask(getNbToWait(nbOfThreadsToRollback),
          nbOfThreadsToRollback, tm.getTimeout(), tm.getLogin(), tid);

    // Post the task in each backendThread tasklist and wakeup the threads. This
    // could either be the remaining threads that were not in the asynchronous
    // queue or all the backends that started the transaction.
    for (int i = 0; i < nbOfThreadsToRollback; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) rollbackList.get(i);
      synchronized (thread)
      {
        thread.addTask(task, tid);
        thread.notify();
      }
    }

    // Release lock
    if (canTakeReadLock)
      backendNonBlockingThreadsRWLock.releaseRead();
    else
      backendNonBlockingThreadsRWLock.releaseWrite();

    // Unblock next query from total order queue
    if (totalOrderRollback != null)
      removeHeadFromAndNotifyTotalOrderQueue();

    // Check if someone had something to rollback
    if (task == null)
      return;

    synchronized (task)
    {
      if (!task.hasCompleted())
      {
        // Wait for completion (notified by the task)
        try
        {
          // Wait on task
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            task.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining <= 0)
            {
              if (task.setExpiredTimeout())
              { // Task will be ignored by all backends
                String msg = Translate.get("loadbalancer.rollback.timeout",
                    new String[]{String.valueOf(tid),
                        String.valueOf(task.getSuccess()),
                        String.valueOf(task.getFailed())});
                logger.warn(msg);
                throw new SQLException(msg);
              }
              // else task execution already started, to late to cancel
            }
          }
          else
            task.wait();
        }
        catch (InterruptedException e)
        {
          if (task.setExpiredTimeout())
          { // Task will be ignored by all backends
            String msg = Translate.get("loadbalancer.rollback.timeout",
                new String[]{String.valueOf(tid),
                    String.valueOf(task.getSuccess()),
                    String.valueOf(task.getFailed())});
            logger.warn(msg);
            throw new SQLException(msg);
          }
          // else task execution already started, to late to cancel
        }
      }

      if (task.getSuccess() > 0)
        return;
      else
      { // All tasks failed
        ArrayList exceptions = task.getExceptions();
        if (exceptions == null)
          throw new SQLException(Translate.get(
              "loadbalancer.rollback.all.failed", tid));
        else
        {
          String errorMsg = Translate.get("loadbalancer.rollback.failed.stack",
              tid)
              + "\n";
          for (int i = 0; i < exceptions.size(); i++)
            errorMsg += ((SQLException) exceptions.get(i)).getMessage() + "\n";
          logger.error(errorMsg);
          throw new SQLException(errorMsg);
        }
      }
    }
  }

  /**
   * Rollback a transaction to a savepoint
   * 
   * @param tm The transaction marker metadata
   * @param savepointName The name of the savepoint
   * @throws AllBackendsFailedException if all backends failed to perform the
   *           rollback
   * @throws SQLException if an error occurs
   */
  public void rollback(TransactionMarkerMetaData tm, String savepointName)
      throws AllBackendsFailedException, SQLException
  {
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);
    // List of backends that still have pending queries for the transaction for
    // which to rollback to a savepoint
    ArrayList asynchronousBackends = null;
    RollbackToSavepointTask task = null;

    // Ordering for distributed virtual database
    RollbackToSavepoint totalOrderRollback = null;
    boolean canTakeReadLock = false;
    if (vdb.getTotalOrderQueue() != null)
    {
      totalOrderRollback = new RollbackToSavepoint(tid, savepointName);
      // Total ordering mainly for distributed virtual databases.
      // If waitForTotalOrder returns true then the query has been scheduled in
      // total order and there is no need to take a write lock later to resolve
      // potential conflicts.
      canTakeReadLock = waitForTotalOrder(totalOrderRollback, false);
      if (!canTakeReadLock)
        // This is a local commit no total order info
        totalOrderRollback = null;
    }

    if (waitForCompletionPolicy.getPolicy() != WaitForCompletionPolicy.ALL)
    {
      try
      {
        if (canTakeReadLock)
          backendBlockingThreadsRWLock.acquireRead();
        else
        {
          // Lock in write to ensure that all writes are posted and we wait in
          // the queue, else a read lock has the priority with the
          // implementation we are using.
          backendBlockingThreadsRWLock.acquireWrite();
        }
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get(
            "loadbalancer.backendlist.acquire.writelock.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }

      int nbOfThreads = backendBlockingThreads.size();
      // Create the task
      task = new RollbackToSavepointTask(getNbToWait(nbOfThreads), nbOfThreads,
          tm.getTimeout(), tm.getLogin(), tid, savepointName);

      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) backendBlockingThreads
            .get(i);
        if (thread.hasTaskForTransaction(lTid))
        {
          if (asynchronousBackends == null)
            asynchronousBackends = new ArrayList();
          asynchronousBackends.add(thread.getBackend());
          synchronized (thread)
          {
            thread.insertTaskAfterLastWriteForTransaction(task, lTid);
            thread.notify();
          }
        }
      }

      if (canTakeReadLock)
        backendBlockingThreadsRWLock.releaseRead();
      else
        backendBlockingThreadsRWLock.releaseWrite();

      // Unset the task
      if (asynchronousBackends == null)
        task = null;
    }

    try
    {
      if (canTakeReadLock)
        backendNonBlockingThreadsRWLock.acquireRead();
      else
        backendNonBlockingThreadsRWLock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    int nbOfThreads = backendNonBlockingThreads.size();
    ArrayList rollbackList = new ArrayList();

    // Build the list of backend that need to rollback to savepoint for this
    // transaction
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendNonBlockingThreads
          .get(i);
      DatabaseBackend backend = thread.getBackend();
      // If the transaction has been started on this backend and it was not
      // previously treated in the asynchronous backend list (late nodes), then
      // we have to post the task now in the asynchronous list.
      if (backend.isStartedTransaction(lTid)
          && ((asynchronousBackends == null) || (!asynchronousBackends
              .contains(backend))))
        rollbackList.add(thread);
    }

    int nbOfThreadsToRollback = rollbackList.size();
    // If no backend was late and the rollback task has not been posted to any
    // backend yet, then we have to create a task for the backends that really
    // need to rollback the transaction.
    if ((task == null) && (nbOfThreadsToRollback != 0))
      task = new RollbackToSavepointTask(getNbToWait(nbOfThreadsToRollback),
          nbOfThreadsToRollback, tm.getTimeout(), tm.getLogin(), tid,
          savepointName);

    // Post the task in each backendThread tasklist and wakeup the threads. This
    // could either be the remaining threads that were not in the asynchronous
    // queue or all the backends that started the transaction.
    for (int i = 0; i < nbOfThreadsToRollback; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) rollbackList.get(i);
      synchronized (thread)
      {
        thread.addTask(task, tid);
        thread.notify();
      }
    }

    // Release lock
    if (canTakeReadLock)
      backendNonBlockingThreadsRWLock.releaseRead();
    else
      backendNonBlockingThreadsRWLock.releaseWrite();

    // Unblock next query from total order queue
    if (totalOrderRollback != null)
      removeHeadFromAndNotifyTotalOrderQueue();

    // Check if someone had something to rollback
    if (task == null)
      return;

    synchronized (task)
    {
      if (!task.hasCompleted())
      {
        // Wait for completion (notified by the task)
        try
        {
          // Wait on task
          long timeout = tm.getTimeout();
          if (timeout > 0)
          {
            long start = System.currentTimeMillis();
            task.wait(timeout);
            long end = System.currentTimeMillis();
            long remaining = timeout - (end - start);
            if (remaining <= 0)
            {
              if (task.setExpiredTimeout())
              { // Task will be ignored by all backends
                String msg = Translate.get(
                    "loadbalancer.rollbacksavepoint.timeout", new String[]{
                        savepointName, String.valueOf(tid),
                        String.valueOf(task.getSuccess()),
                        String.valueOf(task.getFailed())});
                logger.warn(msg);
                throw new SQLException(msg);
              }
              // else task execution already started, to late to cancel
            }
          }
          else
            task.wait();
        }
        catch (InterruptedException e)
        {
          if (task.setExpiredTimeout())
          { // Task will be ignored by all backends
            String msg = Translate.get(
                "loadbalancer.rollbacksavepoint.timeout", new String[]{
                    savepointName, String.valueOf(tid),
                    String.valueOf(task.getSuccess()),
                    String.valueOf(task.getFailed())});
            logger.warn(msg);
            throw new SQLException(msg);
          }
          // else task execution already started, to late to cancel
        }
      }

      if (task.getSuccess() > 0)
        return;
      else
      { // All tasks failed
        ArrayList exceptions = task.getExceptions();
        if (exceptions == null)
          throw new SQLException(Translate.get(
              "loadbalancer.rollbacksavepoint.all.failed", new String[]{
                  savepointName, String.valueOf(tid)}));
        else
        {
          String errorMsg = Translate.get(
              "loadbalancer.rollbacksavepoint.failed.stack", new String[]{
                  savepointName, String.valueOf(tid)})
              + "\n";
          SQLException ex = SQLExceptionFactory.getSQLException(exceptions,
              errorMsg);
          logger.error(ex.getMessage());
          throw ex;
        }
      }
    }
  }

  /**
   * Release a savepoint from a transaction
   * 
   * @param tm The transaction marker metadata
   * @param name The name of the savepoint ro release
   * @throws SQLException if an error occurs
   */
  public void releaseSavepoint(TransactionMarkerMetaData tm, String name)
      throws SQLException
  {
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);

    // List of backends that still have pending queries for the transaction for
    // which a savepoint will be released
    ArrayList asynchronousBackends = null;
    ReleaseSavepointTask task = null;

    // Ordering for distributed virtual database
    ReleaseSavepoint totalOrderRelease = null;
    boolean canTakeReadLock = false;
    if (vdb.getTotalOrderQueue() != null)
    {
      totalOrderRelease = new ReleaseSavepoint(tid, name);
      // Total ordering mainly for distributed virtual databases.
      // If waitForTotalOrder returns true then the query has been scheduled in
      // total order and there is no need to take a write lock later to resolve
      // potential conflicts.
      canTakeReadLock = waitForTotalOrder(totalOrderRelease, false);
      if (!canTakeReadLock)
        // This is a local commit no total order info
        totalOrderRelease = null;
    }

    if (waitForCompletionPolicy.getPolicy() != WaitForCompletionPolicy.ALL)
    {
      try
      {
        if (canTakeReadLock)
          backendBlockingThreadsRWLock.acquireRead();
        else
        {
          // Lock in write to ensure that all writes are posted and we wait in
          // the queue, else a read lock has the priority with the
          // implementation we are using.
          backendBlockingThreadsRWLock.acquireWrite();
        }
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get(
            "loadbalancer.backendlist.acquire.writelock.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }

      int nbOfThreads = backendBlockingThreads.size();

      // Create the task
      task = new ReleaseSavepointTask(getNbToWait(nbOfThreads), nbOfThreads, tm
          .getTimeout(), tm.getLogin(), tid, name);

      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) backendBlockingThreads
            .get(i);
        if (thread.hasTaskForTransaction(lTid))
        {
          if (asynchronousBackends == null)
            asynchronousBackends = new ArrayList();
          asynchronousBackends.add(thread.getBackend());
          synchronized (thread)
          {
            thread.insertTaskAfterLastWriteForTransaction(task, lTid);
            thread.notify();
          }
        }
      }

      // Release lock
      if (canTakeReadLock)
        backendBlockingThreadsRWLock.releaseRead();
      else
        backendBlockingThreadsRWLock.releaseWrite();

      // Unset the task
      if (asynchronousBackends == null)
        task = null;
    }

    try
    {
      if (canTakeReadLock)
        backendNonBlockingThreadsRWLock.acquireRead();
      else
        backendNonBlockingThreadsRWLock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    int nbOfThreads = backendNonBlockingThreads.size();
    ArrayList savepointList = new ArrayList();
    // Build the list of backend that need to release a savepoint for this
    // transaction
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendNonBlockingThreads
          .get(i);
      DatabaseBackend backend = thread.getBackend();
      // If the transaction has been started on this backend and it was not
      // previously treated in the asynchronous backend list (late nodes), then
      // we have to post the task now in the asynchronous list.
      if (backend.isStartedTransaction(lTid)
          && ((asynchronousBackends == null) || (!asynchronousBackends
              .contains(backend))))
        savepointList.add(thread);
    }

    nbOfThreads = savepointList.size();
    if (nbOfThreads == 0)
    {
      if (canTakeReadLock)
        backendNonBlockingThreadsRWLock.releaseRead();
      else
        backendNonBlockingThreadsRWLock.releaseWrite();
      return;
    }

    if (task == null)
      task = new ReleaseSavepointTask(getNbToWait(nbOfThreads), nbOfThreads, tm
          .getTimeout(), tm.getLogin(), tid, name);

    synchronized (task)
    {
      // Post the task in each backendThread tasklist and wakeup the threads
      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) savepointList.get(i);
        synchronized (thread)
        {
          thread.addTask(task, tid);
          thread.notify();
        }
      }

      // Release lock
      if (canTakeReadLock)
        backendNonBlockingThreadsRWLock.releaseRead();
      else
        backendNonBlockingThreadsRWLock.releaseWrite();

      // Unblock next query from total order queue
      if (totalOrderRelease != null)
        removeHeadFromAndNotifyTotalOrderQueue();

      // Wait for completion (notified by the task)
      try
      {
        // Wait on task
        long timeout = tm.getTimeout();
        if (timeout > 0)
        {
          long start = System.currentTimeMillis();
          task.wait(timeout);
          long end = System.currentTimeMillis();
          long remaining = timeout - (end - start);
          if (remaining <= 0)
          {
            if (task.setExpiredTimeout())
            { // Task will be ignored by all backends
              String msg = Translate.get(
                  "loadbalancer.releasesavepoint.timeout", new String[]{name,
                      String.valueOf(tid), String.valueOf(task.getSuccess()),
                      String.valueOf(task.getFailed())});
              logger.warn(msg);
              throw new SQLException(msg);
            }
            // else task execution already started, to late to cancel
          }
        }
        else
          task.wait();
      }
      catch (InterruptedException e)
      {
        if (task.setExpiredTimeout())
        { // Task will be ignored by all backends
          String msg = Translate.get("loadbalancer.releasesavepoint.timeout",
              new String[]{name, String.valueOf(tid),
                  String.valueOf(task.getSuccess()),
                  String.valueOf(task.getFailed())});
          logger.warn(msg);
          throw new SQLException(msg);
        }
        // else task execution already started, to late to cancel
      }

      if (task.getSuccess() > 0)
        return;
      else
      { // All tasks failed
        ArrayList exceptions = task.getExceptions();
        if (exceptions == null)
          throw new SQLException(Translate.get(
              "loadbalancer.releasesavepoint.all.failed", new String[]{name,
                  String.valueOf(tid)}));
        else
        {
          String errorMsg = Translate.get(
              "loadbalancer.releasesavepoint.failed.stack", new String[]{name,
                  String.valueOf(tid)})
              + "\n";
          SQLException ex = SQLExceptionFactory.getSQLException(exceptions,
              errorMsg);
          logger.error(ex.getMessage());
          throw ex;
        }
      }
    }
  }

  /**
   * Set a savepoint to a transaction.
   * 
   * @param tm The transaction marker metadata
   * @param name The name of the new savepoint
   * @throws SQLException if an error occurs
   */
  public void setSavepoint(TransactionMarkerMetaData tm, String name)
      throws SQLException
  {
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);

    // List of backends that still have pending queries for the transaction for
    // which a savepoint will be set
    ArrayList asynchronousBackends = null;
    SavepointTask task = null;

    // Ordering for distributed virtual database
    SetSavepoint totalOrderSavepoint = null;
    boolean canTakeReadLock = false;
    if (vdb.getTotalOrderQueue() != null)
    {
      totalOrderSavepoint = new SetSavepoint(tid, name);
      // Total ordering mainly for distributed virtual databases.
      // If waitForTotalOrder returns true then the query has been scheduled in
      // total order and there is no need to take a write lock later to resolve
      // potential conflicts.
      canTakeReadLock = waitForTotalOrder(totalOrderSavepoint, false);
      if (!canTakeReadLock)
        // This is a local commit no total order info
        totalOrderSavepoint = null;
    }

    if (waitForCompletionPolicy.getPolicy() != WaitForCompletionPolicy.ALL)
    {
      try
      {
        if (canTakeReadLock)
          backendBlockingThreadsRWLock.acquireRead();
        else
        {
          // Lock in write to ensure that all writes are posted and we wait in
          // the queue, else a read lock has the priority with the
          // implementation we are using.
          backendBlockingThreadsRWLock.acquireWrite();
        }
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get(
            "loadbalancer.backendlist.acquire.writelock.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }

      int nbOfThreads = backendBlockingThreads.size();

      // Create the task
      task = new SavepointTask(getNbToWait(nbOfThreads), nbOfThreads, tm
          .getTimeout(), tm.getLogin(), tid, name);

      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) backendBlockingThreads
            .get(i);
        if (thread.hasTaskForTransaction(lTid))
        {
          if (asynchronousBackends == null)
            asynchronousBackends = new ArrayList();
          asynchronousBackends.add(thread.getBackend());
          synchronized (thread)
          {
            thread.insertTaskAfterLastWriteForTransaction(task, lTid);
            thread.notify();
          }
        }
      }

      // Release lock
      if (canTakeReadLock)
        backendBlockingThreadsRWLock.releaseRead();
      else
        backendBlockingThreadsRWLock.releaseWrite();

      // Unset the task
      if (asynchronousBackends == null)
        task = null;
    }

    try
    {
      if (canTakeReadLock)
        backendNonBlockingThreadsRWLock.acquireRead();
      else
        backendNonBlockingThreadsRWLock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    int nbOfThreads = backendNonBlockingThreads.size();
    ArrayList savepointList = new ArrayList();
    // Build the list of backend that need to set a savepoint for this
    // transaction
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendNonBlockingThreads
          .get(i);
      DatabaseBackend backend = thread.getBackend();
      // If the transaction has been started on this backend and it was not
      // previously treated in the asynchronous backend list (late nodes), then
      // we have to post the task now in the asynchronous list.
      if (backend.isStartedTransaction(lTid)
          && ((asynchronousBackends == null) || (!asynchronousBackends
              .contains(backend))))
        savepointList.add(thread);
    }

    nbOfThreads = savepointList.size();
    if (nbOfThreads == 0)
    {
      if (canTakeReadLock)
        backendNonBlockingThreadsRWLock.releaseRead();
      else
        backendNonBlockingThreadsRWLock.releaseWrite();
      return;
    }

    if (task == null)
      task = new SavepointTask(getNbToWait(nbOfThreads), nbOfThreads, tm
          .getTimeout(), tm.getLogin(), tid, name);

    synchronized (task)
    {
      // Post the task in each backendThread tasklist and wakeup the threads
      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) savepointList.get(i);
        synchronized (thread)
        {
          thread.addTask(task, tid);
          thread.notify();
        }
      }

      // Release lock
      if (canTakeReadLock)
        backendNonBlockingThreadsRWLock.releaseRead();
      else
        backendNonBlockingThreadsRWLock.releaseWrite();

      // Unblock next query from total order queue
      if (totalOrderSavepoint != null)
        removeHeadFromAndNotifyTotalOrderQueue();

      // Wait for completion (notified by the task)
      try
      {
        // Wait on task
        long timeout = tm.getTimeout();
        if (timeout > 0)
        {
          long start = System.currentTimeMillis();
          task.wait(timeout);
          long end = System.currentTimeMillis();
          long remaining = timeout - (end - start);
          if (remaining <= 0)
          {
            if (task.setExpiredTimeout())
            { // Task will be ignored by all backends
              String msg = Translate.get("loadbalancer.setsavepoint.timeout",
                  new String[]{name, String.valueOf(tid),
                      String.valueOf(task.getSuccess()),
                      String.valueOf(task.getFailed())});
              logger.warn(msg);
              throw new SQLException(msg);
            }
            // else task execution already started, to late to cancel
          }
        }
        else
          task.wait();
      }
      catch (InterruptedException e)
      {
        if (task.setExpiredTimeout())
        { // Task will be ignored by all backends
          String msg = Translate.get("loadbalancer.setsavepoint.timeout",
              new String[]{name, String.valueOf(tid),
                  String.valueOf(task.getSuccess()),
                  String.valueOf(task.getFailed())});
          logger.warn(msg);
          throw new SQLException(msg);
        }
        // else task execution already started, to late to cancel
      }

      if (task.getSuccess() > 0)
        return;
      else
      { // All tasks failed
        ArrayList exceptions = task.getExceptions();
        if (exceptions == null)
          throw new SQLException(Translate.get(
              "loadbalancer.setsavepoint.all.failed", new String[]{name,
                  String.valueOf(tid)}));
        else
        {
          String errorMsg = Translate.get(
              "loadbalancer.setsavepoint.failed.stack", new String[]{name,
                  String.valueOf(tid)})
              + "\n";
          SQLException ex = SQLExceptionFactory.getSQLException(exceptions,
              errorMsg);
          logger.error(ex.getMessage());
          throw ex;
        }
      }
    }
  }

  /**
   * Wait for all writes to be posted on BackendBlockingThreads by simply
   * acquiring the RW lock in write and releasing it.
   * 
   * @throws SQLException if we fail to acquire the lock
   */
  private void waitForAllWritesToBePostedOnBackendBlockingThreads()
      throws SQLException
  {
    // Lock in write to ensure that all writes are posted and we wait in the
    // queue, else a read lock has the priority with the implementation we are
    // using.
    try
    {
      backendBlockingThreadsRWLock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }
    backendBlockingThreadsRWLock.releaseWrite();
  }

  /**
   * Waits for all writes of the given transaction in the blocking thread queue
   * to complete before being able to complete the transaction.
   * 
   * @throws SQLException if a locking error occurs
   */
  protected void waitForAllWritesToComplete(long transactionId)
      throws SQLException
  {
    waitForAllWritesToBePostedOnBackendBlockingThreads();

    boolean success = false;
    while (!success)
    {
      try
      { // Note that we are not synchronized here and we might have concurrent
        // modifications of the backend list.
        for (Iterator iter = backendBlockingThreads.iterator(); iter.hasNext();)
        {
          BackendWorkerThread thread = (BackendWorkerThread) iter.next();
          thread.waitForAllTasksToComplete(transactionId);
        }
        success = true;
      }
      catch (ConcurrentModificationException e)
      { // List has been modified while we were iterating
        // Retry until we succeed
      }
    }
  }

  /**
   * Waits for all writes of the given transaction in the blocking thread queue
   * of the given backend to complete before being able to complete the
   * transaction.
   * 
   * @throws SQLException if we fail to acquire the lock
   * @see #executeRequestOnBackend
   */
  protected void waitForAllWritesToComplete(DatabaseBackend backend,
      long transactionId) throws SQLException
  {
    waitForAllWritesToBePostedOnBackendBlockingThreads();

    boolean success = false;
    while (!success)
    {
      try
      { // Note that we are not synchronized here and we might have concurrent
        // modifications of the backend list.
        for (Iterator iter = backendBlockingThreads.iterator(); iter.hasNext();)
        {
          BackendWorkerThread thread = (BackendWorkerThread) iter.next();
          if (thread.getBackend() == backend)
          {
            thread.waitForAllTasksToComplete(transactionId);
            break;
          }
        }
        success = true;
      }
      catch (ConcurrentModificationException e)
      { // List has been modified while we were iterating
        // Retry until we succeed
      }
    }
  }

  /**
   * Waits for all writes in the blocking thread queue of the given backend to
   * complete.
   * 
   * @throws SQLException if we fail to acquire the lock
   * @see #executeRequestOnBackend
   */
  protected void waitForAllWritesToComplete(DatabaseBackend backend)
      throws SQLException
  {
    waitForAllWritesToBePostedOnBackendBlockingThreads();

    boolean success = false;
    while (!success)
    {
      try
      { // Note that we are not synchronized here and we might have concurrent
        // modifications of the backend list.
        for (Iterator iter = backendBlockingThreads.iterator(); iter.hasNext();)
        {
          BackendWorkerThread thread = (BackendWorkerThread) iter.next();
          if (thread.getBackend() == backend)
          {
            thread.waitForAllTasksToComplete();
            break;
          }
        }
        success = true;
      }
      catch (ConcurrentModificationException e)
      { // List has been modified while we were iterating
        // Retry until we succeed
      }
    }
  }

  /*
   * Backends management
   */

  /**
   * Enables a Backend that was previously disabled.
   * <p>
   * Ask the corresponding connection manager to initialize the connections if
   * needed.
   * <p>
   * No sanity checks are performed by this function.
   * 
   * @param db The database backend to enable
   * @param writeEnabled True if the backend must be enabled for writes
   * @throws SQLException if an error occurs
   */
  public void enableBackend(DatabaseBackend db, boolean writeEnabled)
      throws SQLException
  {
    if (writeEnabled)
    {
      // Create 2 worker threads
      BackendWorkerThread blockingThread = new BackendWorkerThread(db, this);
      BackendWorkerThread nonBlockingThread = new BackendWorkerThread(db, this);

      // Add first to the blocking thread list
      try
      {
        backendBlockingThreadsRWLock.acquireWrite();
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get(
            "loadbalancer.backendlist.acquire.writelock.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }
      backendBlockingThreads.add(blockingThread);
      backendBlockingThreadsRWLock.releaseWrite();
      blockingThread.start();
      logger.info(Translate.get(
          "loadbalancer.backend.workerthread.blocking.add", db.getName()));

      // Then add to the non-blocking thread list
      try
      {
        backendNonBlockingThreadsRWLock.acquireWrite();
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get(
            "loadbalancer.backendlist.acquire.writelock.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }
      backendNonBlockingThreads.add(nonBlockingThread);
      backendNonBlockingThreadsRWLock.releaseWrite();
      nonBlockingThread.start();
      logger.info(Translate.get(
          "loadbalancer.backend.workerthread.non.blocking.add", db.getName()));
      db.enableWrite();
    }

    if (!db.isInitialized())
      db.initializeConnections();
    db.enableRead();
  }

  /**
   * Disables a backend that was previously enabled.
   * <p>
   * Ask the corresponding connection manager to finalize the connections if
   * needed.
   * <p>
   * No sanity checks are performed by this function.
   * 
   * @param db the database backend to disable
   * @throws SQLException if an error occurs
   */
  public synchronized void disableBackend(DatabaseBackend db)
      throws SQLException
  {
    if (db.isWriteEnabled())
    {
      KillThreadTask killBlockingThreadTask = new KillThreadTask(1, 1);

      // Starts with backendBlockingThreads
      try
      {
        backendBlockingThreadsRWLock.acquireWrite();
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get(
            "loadbalancer.backendlist.acquire.writelock.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }

      int nbOfThreads = backendBlockingThreads.size();

      // Find the right blocking thread
      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) backendBlockingThreads
            .get(i);
        if (thread.getBackend().equals(db))
        {
          logger.info(Translate
              .get("loadbalancer.backend.workerthread.blocking.remove", db
                  .getName()));

          // Remove it from the backendBlockingThread list
          backendBlockingThreads.remove(i);

          synchronized (thread)
          {
            // Kill the thread
            thread.addPriorityTask(killBlockingThreadTask);
            thread.notify();
          }
          break;
        }
      }

      backendBlockingThreadsRWLock.releaseWrite();

      // Wait for the thread to be killed
      synchronized (killBlockingThreadTask)
      {
        if (!killBlockingThreadTask.hasFullyCompleted())
          try
          {
            killBlockingThreadTask.wait();
          }
          catch (InterruptedException ignore)
          {
          }
      }

      // Continue with backendNonBlockingThreads
      KillThreadTask killNonBlockingThreadTask = new KillThreadTask(1, 1);

      try
      {
        backendNonBlockingThreadsRWLock.acquireWrite();
      }
      catch (InterruptedException e)
      {
        String msg = Translate.get(
            "loadbalancer.backendlist.acquire.writelock.failed", e);
        logger.error(msg);
        throw new SQLException(msg);
      }

      // Find the right non-blocking thread
      nbOfThreads = backendNonBlockingThreads.size();
      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) backendNonBlockingThreads
            .get(i);
        if (thread.getBackend().equals(db))
        {
          logger.info(Translate.get(
              "loadbalancer.backend.workerthread.non.blocking.remove", db
                  .getName()));

          // Remove it from the backendNonBlockingThreads list
          backendNonBlockingThreads.remove(i);

          synchronized (thread)
          {
            // Kill the thread
            thread.addPriorityTask(killNonBlockingThreadTask);
            thread.notify();
          }
          break;
        }
      }

      backendNonBlockingThreadsRWLock.releaseWrite();

      // Wait for the thread to be killed
      synchronized (killNonBlockingThreadTask)
      {
        if (!killNonBlockingThreadTask.hasFullyCompleted())
          try
          {
            killNonBlockingThreadTask.wait();
          }
          catch (InterruptedException ignore)
          {
          }
      }
    }

    db.disable();
    if (db.isInitialized())
      db.finalizeConnections();
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#getNumberOfEnabledBackends()
   */
  public int getNumberOfEnabledBackends()
  {
    return backendBlockingThreads.size();
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#getXmlImpl
   */
  public String getXmlImpl()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_RAIDb_2 + ">");
    if (createTablePolicy != null)
      info.append(createTablePolicy.getXml());
    if (waitForCompletionPolicy != null)
      info.append(waitForCompletionPolicy.getXml());
    if (macroHandler != null)
      info.append(macroHandler.getXml());
    this.getRaidb2Xml();
    info.append("</" + DatabasesXmlTags.ELT_RAIDb_2 + ">");
    return info.toString();
  }

  /**
   * return xml formatted information about this raidb2 load balancer
   * 
   * @return xml formatted string
   */
  public abstract String getRaidb2Xml();

}