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
 * Contributor(s): Jaco Swart, Jean-Bernard van Zuylen
 */

package org.objectweb.cjdbc.controller.loadbalancer.raidb0;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.exceptions.BadConnectionException;
import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.NoTransactionStartWhenDisablingException;
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
import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;
import org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTableException;
import org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTablePolicy;
import org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTableRule;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.CommitTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.KillThreadTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.ReleaseSavepointTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.RollbackTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.RollbackToSavepointTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.SavepointTask;
import org.objectweb.cjdbc.controller.requestmanager.RAIDbLevels;
import org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * RAIDb-0: database partitioning.
 * <p>
 * The requests are sent to the backend nodes hosting the tables needed to
 * execute the request. If no backend has the needed tables to perform a
 * request, it will fail.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jaco.swart@iblocks.co.uk">Jaco Swart </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class RAIDb0 extends AbstractLoadBalancer
{
  //
  // How the code is organized?
  // 1. Member variables
  // 2. Constructor(s)
  // 3. Request handling
  // 4. Transaction handling
  // 5. Backend management
  // 6. Debug/Monitoring
  //

  private ArrayList                   backendThreads;
  private ReadPrioritaryFIFOWriteLock backendThreadsRWLock = new ReadPrioritaryFIFOWriteLock();
  private CreateTablePolicy           createTablePolicy;

  protected static Trace              logger               = Trace
                                                               .getLogger("org.objectweb.cjdbc.controller.loadbalancer.RAIDb0");

  /*
   * Constructors
   */

  /**
   * Creates a new RAIDb-0 request load balancer.
   * 
   * @param vdb the virtual database this load balancer belongs to.
   * @param createTablePolicy the policy defining how 'create table' statements
   *          should be handled
   * @throws Exception if an error occurs
   */
  public RAIDb0(VirtualDatabase vdb, CreateTablePolicy createTablePolicy)
      throws Exception
  {
    super(vdb, RAIDbLevels.RAIDb0, ParsingGranularities.TABLE);
    backendThreads = new ArrayList();
    this.createTablePolicy = createTablePolicy;
  }

  /*
   * Request Handling
   */

  /**
   * Performs a read request on the backend that has the needed tables to
   * executes the request.
   * 
   * @param request an <code>SelectRequest</code>
   * @param metadataCache the metadataCache if any or null
   * @return the corresponding <code>java.sql.ResultSet</code>
   * @exception SQLException if an error occurs
   * @see AbstractLoadBalancer#execReadRequest(SelectRequest, MetadataCache)
   */
  public ControllerResultSet execReadRequest(SelectRequest request,
      MetadataCache metadataCache) throws SQLException
  {
    try
    {
      vdb.acquireReadLockBackendLists(); // Acquire
      // read
      // lock
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    try
    {
      ControllerResultSet rs = null;
      ArrayList fromTables = request.getFrom();

      if (fromTables == null)
        throw new SQLException(Translate.get("loadbalancer.from.not.found",
            request.getSQLShortForm(vdb.getSQLShortFormLength())));

      // Find the backend that has the needed tables
      ArrayList backends = vdb.getBackends();
      int size = backends.size();
      int enabledBackends = 0;

      DatabaseBackend backend = null;
      // The backend that will execute the query
      for (int i = 0; i < size; i++)
      {
        backend = (DatabaseBackend) backends.get(i);
        if (backend.isReadEnabled())
          enabledBackends++;
        if (backend.isReadEnabled() && backend.hasTables(fromTables))
          break;
        else
          backend = null;
      }

      if (backend == null)
      {
        if (enabledBackends == 0)
          throw new NoMoreBackendException(Translate.get(
              "loadbalancer.execute.no.backend.enabled", request.getId()));
        else
          throw new SQLException(Translate.get(
              "loadbalancer.backend.no.required.tables", fromTables.toString()));
      }

      if (logger.isDebugEnabled())
      {
        logger.debug("Backend " + backend.getName()
            + " has all tables which are:");
        for (int i = 0; i < fromTables.size(); i++)
        {
          logger.debug(fromTables.get(i));
        }
      }

      // Execute the request on the chosen backend
      try
      {
        rs = executeRequestOnBackend(request, backend, metadataCache);
      }
      catch (SQLException se)
      {
        String msg = Translate.get("loadbalancer.request.failed", new String[]{
            String.valueOf(request.getId()), se.getMessage()});
        if (logger.isInfoEnabled())
          logger.info(msg);
        throw new SQLException(msg);
      }

      return rs;
    }
    catch (RuntimeException e)
    {
      String msg = Translate
          .get("loadbalancer.request.failed", new String[]{
              request.getSQLShortForm(vdb.getSQLShortFormLength()),
              e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
    }
    finally
    {
      vdb.releaseReadLockBackendLists(); // Release
      // the
      // lock
    }
  }

  /**
   * Performs a write request on the backend that has the needed tables to
   * executes the request.
   * 
   * @param request an <code>AbstractWriteRequest</code>
   * @return number of rows affected by the request
   * @exception SQLException if an error occurs
   */
  public int execWriteRequest(AbstractWriteRequest request) throws SQLException
  {
    // Handle macros
    handleMacros(request);

    try
    {
      vdb.acquireReadLockBackendLists(); // Acquire
      // read
      // lock
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    try
    {
      String table = request.getTableName();
      AbstractConnectionManager cm = null;

      if (table == null)
        throw new SQLException(Translate.get(
            "loadbalancer.request.target.table.not.found", request
                .getSQLShortForm(vdb.getSQLShortFormLength())));

      // Find the backend that has the needed table
      ArrayList backends = vdb.getBackends();
      int size = backends.size();

      DatabaseBackend backend = null;
      // The backend that will execute the query
      if (request.isCreate())
      { // Choose the backend according to the defined policy
        CreateTableRule rule = createTablePolicy.getTableRule(request
            .getTableName());
        if (rule == null)
          rule = createTablePolicy.getDefaultRule();

        // Ask the rule to pickup a backend
        ArrayList choosen;
        try
        {
          choosen = rule.getBackends(backends);
        }
        catch (CreateTableException e)
        {
          throw new SQLException(Translate.get(
              "loadbalancer.create.table.rule.failed", e.getMessage()));
        }

        // Get the connection manager from the chosen backend
        if (choosen != null)
          backend = (DatabaseBackend) choosen.get(0);
        if (backend != null)
          cm = backend.getConnectionManager(request.getLogin());
      }
      else
      { // Find the backend that has the table
        for (int i = 0; i < size; i++)
        {
          backend = (DatabaseBackend) backends.get(i);
          if ((backend.isWriteEnabled() || backend.isDisabling()) && backend.hasTable(table))
          {
            cm = backend.getConnectionManager(request.getLogin());
            break;
          }
        }
      }

      // Sanity check
      if (cm == null)
        throw new SQLException(Translate.get(
            "loadbalancer.backend.no.required.table", table));

      // Ok, let's execute the query

      if (request.isAutoCommit())
      {
        // We do not execute request outside the already open transactions if we
        // are disabling the backend.
        if (backend.isDisabling())
          throw new SQLException(Translate.get(
              "loadbalancer.backend.is.disabling", new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName()}));

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
          throw new SQLException(Translate.get(
              "loadbalancer.backend.unreacheable", backend.getName()));
        }

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
              "loadbalancer.backend.no.connection", backend.getName()));

        int result;
        try
        {
          result = executeUpdateRequestOnBackend(request, backend, c);
        }
        catch (Exception e)
        {
          throw new SQLException(Translate.get("loadbalancer.request.failed",
              new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  e.getMessage()}));
        }
        finally
        {
          cm.releaseConnection(c);
        }
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("loadbalancer.execute.on", new String[]{
              String.valueOf(request.getId()), backend.getName()}));
        return result;
      }
      else
      { // Inside a transaction
        Connection c;
        long tid = request.getTransactionId();
        Long lTid = new Long(tid);

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
              new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName()});
          logger.error(msg);
          throw new SQLException(msg);
        }

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
              "loadbalancer.unable.retrieve.connection", new String[]{
                  String.valueOf(tid), backend.getName()}));

        // Execute the query
        int result;
        try
        {
          result = executeUpdateRequestOnBackend(request, backend, c);
        }
        catch (Exception e)
        {
          throw new SQLException(Translate.get("loadbalancer.request.failed",
              new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  e.getMessage()}));
        }
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("loadbalancer.execute.on", new String[]{
              String.valueOf(request.getId()), backend.getName()}));
        return result;
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate
          .get("loadbalancer.request.failed", new String[]{
              request.getSQLShortForm(vdb.getSQLShortFormLength()),
              e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
    }
    finally
    {
      vdb.releaseReadLockBackendLists(); // Release
      // the
      // lock
    }
  }

  /**
   * @see AbstractLoadBalancer#execWriteRequestWithKeys(AbstractWriteRequest,
   *      MetadataCache)
   */
  public ControllerResultSet execWriteRequestWithKeys(
      AbstractWriteRequest request, MetadataCache metadataCache)
      throws SQLException
  {
    // Handle macros
    handleMacros(request);

    try
    {
      vdb.acquireReadLockBackendLists(); // Acquire
      // read
      // lock
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    try
    {
      String table = request.getTableName();
      AbstractConnectionManager cm = null;

      if (table == null)
        throw new SQLException(Translate.get(
            "loadbalancer.request.target.table.not.found", request
                .getSQLShortForm(vdb.getSQLShortFormLength())));

      // Find the backend that has the needed table
      ArrayList backends = vdb.getBackends();
      int size = backends.size();

      DatabaseBackend backend = null;
      // The backend that will execute the query
      if (request.isCreate())
      { // Choose the backend according to the defined policy
        CreateTableRule rule = createTablePolicy.getTableRule(request
            .getTableName());
        if (rule == null)
          rule = createTablePolicy.getDefaultRule();

        // Ask the rule to pickup a backend
        ArrayList choosen;
        try
        {
          choosen = rule.getBackends(backends);
        }
        catch (CreateTableException e)
        {
          throw new SQLException(Translate.get(
              "loadbalancer.create.table.rule.failed", e.getMessage()));
        }

        // Get the connection manager from the chosen backend
        if (choosen != null)
          backend = (DatabaseBackend) choosen.get(0);
        if (backend != null)
          cm = backend.getConnectionManager(request.getLogin());
      }
      else
      { // Find the backend that has the table
        for (int i = 0; i < size; i++)
        {
          backend = (DatabaseBackend) backends.get(i);
          if ((backend.isWriteEnabled() || backend.isDisabling()) && backend.hasTable(table))
          {
            cm = backend.getConnectionManager(request.getLogin());
            break;
          }
        }
      }

      // Sanity check
      if (cm == null)
        throw new SQLException(Translate.get(
            "loadbalancer.backend.no.required.table", table));

      if (!backend.getDriverCompliance().supportGetGeneratedKeys())
        throw new SQLException(Translate.get(
            "loadbalancer.backend.autogeneratedkeys.unsupported", backend
                .getName()));

      // Ok, let's execute the query

      if (request.isAutoCommit())
      {
        // We do not execute request outside the already open transactions if we
        // are disabling the backend.
        if (backend.isDisabling())
          throw new SQLException(Translate.get(
              "loadbalancer.backend.is.disabling", new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName()}));

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
          throw new SQLException(Translate.get(
              "loadbalancer.backend.unreacheable", backend.getName()));
        }

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
              "loadbalancer.backend.no.connection", backend.getName()));

        // Execute Query
        ControllerResultSet result;
        try
        {
          result = executeUpdateRequestOnBackendWithKeys(request, backend, c,
              metadataCache);
        }
        catch (Exception e)
        {
          throw new SQLException(Translate.get("loadbalancer.request.failed",
              new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  e.getMessage()}));
        }
        finally
        {
          backend.removePendingRequest(request);
          cm.releaseConnection(c);
        }
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("loadbalancer.execute.on", new String[]{
              String.valueOf(request.getId()), backend.getName()}));
        return result;
      }
      else
      { // Inside a transaction
        Connection c;
        long tid = request.getTransactionId();
        Long lTid = new Long(tid);

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
              new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName()});
          logger.error(msg);
          throw new SQLException(msg);
        }

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
              "loadbalancer.unable.retrieve.connection", new String[]{
                  String.valueOf(tid), backend.getName()}));

        // Execute the query
        ControllerResultSet result;
        try
        {
          result = executeUpdateRequestOnBackendWithKeys(request, backend, c,
              metadataCache);
        }
        catch (Exception e)
        {
          throw new SQLException(Translate.get("loadbalancer.request.failed",
              new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  e.getMessage()}));
        }
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("loadbalancer.execute.on", new String[]{
              String.valueOf(request.getId()), backend.getName()}));
        return result;
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate
          .get("loadbalancer.request.failed", new String[]{
              request.getSQLShortForm(vdb.getSQLShortFormLength()),
              e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
    }
    finally
    {
      vdb.releaseReadLockBackendLists(); // Release
      // the
      // lock
    }
  }

  /**
   * Execute a read request on the selected backend.
   * 
   * @param request the request to execute
   * @param backend the backend that will execute the request
   * @param metadataCache the metadataCache if any or null
   * @return the ControllerResultSet
   * @throws SQLException if an error occurs
   */
  protected ControllerResultSet executeRequestOnBackend(SelectRequest request,
      DatabaseBackend backend, MetadataCache metadataCache) throws SQLException
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
          throw new SQLException(Translate.get(
              "loadbalancer.backend.unreacheable", backend.getName()));
        }

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
              "loadbalancer.backend.no.connection", backend.getName()));

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
      { // Get rid of the bad connection
        cm.deleteConnection(tid);
        throw new SQLException(Translate
            .get("loadbalancer.connection.failed", new String[]{
                String.valueOf(tid), backend.getName(), e.getMessage()}));
      }
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("loadbalancer.execute.transaction.on",
            new String[]{String.valueOf(tid), String.valueOf(request.getId()),
                backend.getName()}));
      return rs;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadOnlyReadStoredProcedure(StoredProcedure,
   *      MetadataCache)
   */
  public ControllerResultSet execReadOnlyReadStoredProcedure(
      StoredProcedure proc, MetadataCache metadataCache) throws SQLException
  {
    throw new SQLException(
        "Stored procedure calls are not supported with RAIDb-0 load balancers.");
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadStoredProcedure(StoredProcedure,
   *      MetadataCache)
   */
  public ControllerResultSet execReadStoredProcedure(StoredProcedure proc,
      MetadataCache metadataCache) throws SQLException
  {
    throw new SQLException(
        "Stored procedure calls are not supported with RAIDb-0 load balancers.");
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execWriteStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public int execWriteStoredProcedure(StoredProcedure proc) throws SQLException
  {
    throw new SQLException(
        "Stored procedure calls are not supported with RAIDb-0 load balancers.");
  }

  /*
   * Transaction management
   */

  /**
   * Begins a new transaction.
   * 
   * @param tm the transaction marker metadata
   * @throws SQLException if an error occurs
   */
  public final void begin(TransactionMarkerMetaData tm) throws SQLException
  {
  }

  /**
   * Commits a transaction.
   * 
   * @param tm the transaction marker metadata
   * @throws SQLException if an error occurs
   */
  public void commit(TransactionMarkerMetaData tm) throws SQLException
  {
    try
    {
      backendThreadsRWLock.acquireRead();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }

    int nbOfThreads = backendThreads.size();
    ArrayList commitList = new ArrayList();
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);

    // Build the list of backend that need to commit this transaction
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendThreads.get(i);
      if (thread.getBackend().isStartedTransaction(lTid))
        commitList.add(thread);
    }

    nbOfThreads = commitList.size();
    if (nbOfThreads == 0)
    { // Empty transaction or read-only with cache hits, then no backend
      // actually executed any query for that transaction. Simply return.
      // Bug reported by Ganesh (ssriganesha@rogers.com).
      backendThreadsRWLock.releaseRead();
      return;
    }

    // Create the task
    CommitTask task = new CommitTask(nbOfThreads, // Wait for all to commit
        nbOfThreads, tm.getTimeout(), tm.getLogin(), tid);

    synchronized (task)
    {
      // Post the task in each backendThread tasklist and wakeup the threads
      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) commitList.get(i);
        synchronized (thread)
        {
          thread.addTask(task, tid);
          thread.notify();
        }
      }

      backendThreadsRWLock.releaseRead();

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
          String msg = Translate.get("loadbalancer.commit.timeout",
              new String[]{String.valueOf(tid),
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
   * @throws SQLException if an error occurs
   */
  public void rollback(TransactionMarkerMetaData tm) throws SQLException
  {
    try
    {
      backendThreadsRWLock.acquireRead();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }
    int nbOfThreads = backendThreads.size();
    ArrayList rollbackList = new ArrayList();
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);

    // Build the list of backend that need to rollback this transaction
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendThreads.get(i);
      if (thread.getBackend().isStartedTransaction(lTid))
        rollbackList.add(thread);
    }

    nbOfThreads = rollbackList.size();
    if (nbOfThreads == 0)
    { // Empty transaction or read-only with cache hits, then no backend
      // actually executed any query for that transaction. Simply return.
      // Bug reported by Ganesh (ssriganesha@rogers.com).
      backendThreadsRWLock.releaseRead();
      return;
    }

    // Create the task
    RollbackTask task = new RollbackTask(nbOfThreads, // Wait for all to
        // rollback
        nbOfThreads, tm.getTimeout(), tm.getLogin(), tid);

    synchronized (task)
    {
      // Post the task in each backendThread tasklist and wakeup the threads
      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) rollbackList.get(i);
        synchronized (thread)
        {
          thread.addTask(task, tid);
          thread.notify();
        }
      }

      backendThreadsRWLock.releaseRead();

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
   * @throws SQLException if an error occurs
   */
  public void rollback(TransactionMarkerMetaData tm, String savepointName)
      throws SQLException
  {
    try
    {
      backendThreadsRWLock.acquireRead();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }
    int nbOfThreads = backendThreads.size();
    ArrayList rollbackList = new ArrayList();
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);
    
    // Build the list of backend that need to rollback to the savepoint
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendThreads.get(i);
      if (thread.getBackend().isStartedTransaction(lTid))
        rollbackList.add(thread);
    }
    
    nbOfThreads = rollbackList.size();
    if (nbOfThreads == 0)
    { // Empty transaction or read-only with cache hits, then no backend
      // actually executed any query for that transaction. Simply return.
      // Bug reported by Ganesh (ssriganesha@rogers.com).
      backendThreadsRWLock.releaseRead();
      return;
    }
    
    // Create the task
    RollbackToSavepointTask task = new RollbackToSavepointTask(nbOfThreads,
        nbOfThreads, tm.getTimeout(), tm.getLogin(), tid, savepointName);
    
    synchronized (task)
    {
      // Post the task in each backendThread tasklist and wakeup the threads
      for (int i = 0; i < nbOfThreads; i++)
      {
        BackendWorkerThread thread = (BackendWorkerThread) rollbackList.get(i);
        synchronized (thread)
        {
          thread.addTask(task, tid);
          thread.notify();
        }
      }
      
      backendThreadsRWLock.releaseRead();
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
                  "loadbalancer.rollbacksavepoint.timeout",
                  new String[]{savepointName, String.valueOf(tid),
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
              "loadbalancer.rollbacksavepoint.timeout",
              new String[]{savepointName, String.valueOf(tid),
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
              "loadbalancer.rollbacksavepoint.all.failed",
              new String[]{savepointName, String.valueOf(tid)}));
        else
        {
          String errorMsg = Translate.get(
              "loadbalancer.rollbacksavepoint.failed.stack",
              new String[]{savepointName, String.valueOf(tid)})
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
    
    // Acquire lock on backend thread list
    try
    {
      backendThreadsRWLock.acquireRead();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }
    
    // Build the list of backend that need to release a savepoint
    // for this transaction
    ArrayList savepointList = new ArrayList();
    int nbOfThreads = backendThreads.size();
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendThreads.get(i);
      if (thread.getBackend().isStartedTransaction(lTid))
        savepointList.add(thread);
    }

    nbOfThreads = savepointList.size();
    if (nbOfThreads == 0)
    { // Empty transaction or read-only with cache hits, then no backend
      // actually executed any query for that transaction. Simply return.
      backendThreadsRWLock.releaseRead();
      return;
    }

    // Create the task
    ReleaseSavepointTask task = new ReleaseSavepointTask(nbOfThreads,
        nbOfThreads, tm.getTimeout(), tm.getLogin(), tid, name);
    
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

      backendThreadsRWLock.releaseRead();

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
                  "loadbalancer.releasesavepoint.timeout",
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
              "loadbalancer.releasesavepoint.all.failed",
              new String[]{name, String.valueOf(tid)}));
        else
        {
          String errorMsg = Translate.get(
              "loadbalancer.releasesavepoint.failed.stack",
              new String[]{name, String.valueOf(tid)})
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
    
    // Acquire lock on backend thread list
    try
    {
      backendThreadsRWLock.acquireRead();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }
    
    // Build the list of backend that need to set a savepoint
    // for this transaction
    ArrayList savepointList = new ArrayList();
    int nbOfThreads = backendThreads.size();
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendThreads.get(i);
      if (thread.getBackend().isStartedTransaction(lTid))
        savepointList.add(thread);
    }

    nbOfThreads = savepointList.size();
    if (nbOfThreads == 0)
    { // Empty transaction or read-only with cache hits, then no backend
      // actually executed any query for that transaction. Simply return.
      backendThreadsRWLock.releaseRead();
      return;
    }

    // Create the task
    SavepointTask task = new SavepointTask(nbOfThreads, nbOfThreads,
        tm.getTimeout(), tm.getLogin(), tid, name);
    
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

      backendThreadsRWLock.releaseRead();

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
              "loadbalancer.setsavepoint.all.failed",
              new String[]{name, String.valueOf(tid)}));
        else
        {
          String errorMsg = Translate.get(
              "loadbalancer.setsavepoint.failed.stack",
              new String[]{name, String.valueOf(tid)})
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
   * @param db the database backend to enable
   * @param writeEnabled True if the backend must be enabled for writes
   * @throws SQLException if an error occurs
   */
  public void enableBackend(DatabaseBackend db, boolean writeEnabled)
      throws SQLException
  {
    // Create a worker thread and add it to the list
    BackendWorkerThread thread = new BackendWorkerThread(db, this);
    try
    {
      backendThreadsRWLock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }
    backendThreads.add(thread);
    backendThreadsRWLock.releaseWrite();
    thread.start();
    logger.info(Translate.get("loadbalancer.backend.workerthread.add", db
        .getName()));

    if (!db.isInitialized())
      db.initializeConnections();
    db.enableRead();
    if (writeEnabled)
      db.enableWrite();
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
    int nbOfThreads = backendThreads.size();

    // Find the right thread
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendThreads.get(i);
      if (thread.getBackend().equals(db))
      {
        logger.info(Translate.get("loadbalancer.backend.workerthread.remove",
            db.getName()));

        // Remove it from the backendThread list
        try
        {
          backendThreadsRWLock.acquireWrite();
        }
        catch (InterruptedException e)
        {
          String msg = Translate.get(
              "loadbalancer.backendlist.acquire.writelock.failed", e);
          logger.error(msg);
          throw new SQLException(msg);
        }
        backendThreads.remove(thread);
        backendThreadsRWLock.releaseWrite();

        synchronized (thread)
        {
          // Kill the thread
          thread.addPriorityTask(new KillThreadTask(1, 1));
          thread.notify();
        }
        break;
      }
    }

    db.disable();
    if (db.isInitialized())
      db.finalizeConnections();
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#setWeight(String,
   *      int)
   */
  public void setWeight(String name, int w) throws SQLException
  {
    throw new SQLException("Weight is not supported with this load balancer");
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#getNumberOfEnabledBackends()
   */
  public int getNumberOfEnabledBackends()
  {
    return backendThreads.size();
  }

  /*
   * Debug/Monitoring
   */

  /**
   * Get information about the Request load balancer
   * 
   * @return <code>String</code> containing information
   */
  public String getInformation()
  {
    return "RAIDb-0 Request load balancer\n";
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#getXmlImpl
   */
  public String getXmlImpl()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_RAIDb_0 + ">");
    createTablePolicy.getXml();
    info.append("</" + DatabasesXmlTags.ELT_RAIDb_0 + ">");
    return info.toString();
  }

}