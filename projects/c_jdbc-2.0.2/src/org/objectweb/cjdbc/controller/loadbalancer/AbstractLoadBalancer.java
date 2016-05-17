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
 * Contributor(s): Vadim Kassin, Jaco Swart, Jean-Bernard van Zuylen
 */

package org.objectweb.cjdbc.controller.loadbalancer;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.management.NotCompliantMBeanException;

import org.objectweb.cjdbc.common.exceptions.BadConnectionException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.mbeans.AbstractLoadBalancerMBean;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.filters.MacrosHandler;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.backend.DriverCompliance;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean;
import org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * The Request Load Balancer should implement the load balancing of the requests
 * among the backend nodes.
 * <p>
 * The requests comes from the Request Controller and are sent to the Connection
 * Managers.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:vadim@kase.kz">Vadim Kassin </a>
 * @author <a href="mailto:jaco.swart@iblocks.co.uk">Jaco Swart </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public abstract class AbstractLoadBalancer extends AbstractStandardMBean
    implements
      XmlComponent,
      AbstractLoadBalancerMBean
{

  //
  // How the code is organized ?
  //
  // 1. Member variables/Constructor
  // 2. Getter/Setter (possibly in alphabetical order)
  // 3. Request handling
  // 4. Transaction management
  // 5. Backend management
  // 6. Debug/Monitoring
  //

  // Virtual Database this load balancer is attached to.
  protected VirtualDatabase vdb;
  protected int             raidbLevel;
  protected int             parsingGranularity;
  /** Reference to distributed virtual database total order queue */
  protected LinkedList      totalOrderQueue;

  protected static Trace    logger = Trace
                                       .getLogger("org.objectweb.cjdbc.controller.loadbalancer");

  protected MacrosHandler   macroHandler;

  /**
   * Generic constructor that sets some member variables and checks that
   * backends are in the disabled state
   * 
   * @param vdb The virtual database this load balancer belongs to
   * @param raidbLevel The RAIDb level of this load balancer
   * @param parsingGranularity The parsing granularity needed by this load
   *          balancer
   */
  protected AbstractLoadBalancer(VirtualDatabase vdb, int raidbLevel,
      int parsingGranularity) throws SQLException, NotCompliantMBeanException
  {
    super(AbstractLoadBalancerMBean.class);
    this.raidbLevel = raidbLevel;
    this.parsingGranularity = parsingGranularity;
    this.vdb = vdb;
    this.totalOrderQueue = vdb.getTotalOrderQueue();
    try
    {
      vdb.acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.readlock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }
    int size = vdb.getBackends().size();
    ArrayList backends = vdb.getBackends();
    for (int i = 0; i < size; i++)
    {
      DatabaseBackend backend = (DatabaseBackend) backends.get(i);
      if (backend.isReadEnabled() || backend.isWriteEnabled())
      {
        if (logger.isWarnEnabled())
          logger.warn(Translate.get(
              "loadbalancer.constructor.backends.not.disabled", backend
                  .getName()));
        try
        {
          disableBackend(backend);
        }
        catch (Exception e)
        { // Set the disabled state anyway
          backend.disable();
        }
      }
    }
    vdb.releaseReadLockBackendLists();
  }

  //
  // Getter/Setter methods
  //

  /**
   * Returns the RAIDbLevel.
   * 
   * @return int the RAIDb level
   */
  public int getRAIDbLevel()
  {
    return raidbLevel;
  }

  /**
   * Sets the RAIDbLevel.
   * 
   * @param raidbLevel The RAIDb level to set
   */
  public void setRAIDbLevel(int raidbLevel)
  {
    this.raidbLevel = raidbLevel;
  }

  /**
   * Get the needed query parsing granularity.
   * 
   * @return needed query parsing granularity
   */
  public int getParsingGranularity()
  {
    return parsingGranularity;
  }

  /**
   * Set the needed query parsing granularity.
   * 
   * @param parsingGranularity the granularity to set
   */
  public void setParsingGranularity(int parsingGranularity)
  {
    this.parsingGranularity = parsingGranularity;
  }

  //
  // Request Handling
  //

  /**
   * Interprets the macros in the request (depending on the
   * <code>MacroHandler</code> set for this class) and modify either the
   * skeleton or the query itself. Note that the given object is directly
   * modified.
   * 
   * @param request the request to process
   */
  public void handleMacros(AbstractRequest request)
  {
    if (macroHandler == null)
      return;

    // Do not handle macros for requests of type: create, alter, drop and
    // select.
    if (!request.needsMacroProcessing())
      return;

    if (request.isDriverProcessed() || (request.getSqlSkeleton() == null))
      request.setSQL(macroHandler.processMacros(request.getSQL()));
    else
      request.setSqlSkeleton(macroHandler.processMacros(request
          .getSqlSkeleton()));
  }

  /**
   * If we are executing in a distributed virtual database, we have to make sure
   * that we post the query in the queue following the total order. This method
   * does not remove the request from the total order queue. You have to call
   * removeHeadFromAndNotifyTotalOrderQueue() to do so.
   * 
   * @param request the request to wait for (can be any object but usually a
   *          DistributedRequest, Commit or Rollback)
   * @param errorIfNotFound true if an error message should be logged if the
   *          request is not found in the total order queue
   * @return true if the element was found and wait has succeeded, false
   *         otherwise
   * @see #removeHeadFromAndNotifyTotalOrderQueue()
   */
  public boolean waitForTotalOrder(Object request, boolean errorIfNotFound)
  {
    if (totalOrderQueue != null)
    {
      synchronized (totalOrderQueue)
      {
        int index = totalOrderQueue.indexOf(request);
        while (index > 0)
        {
          if (logger.isDebugEnabled())
            logger.debug("Waiting for " + index
                + " queries to execute (current is " + totalOrderQueue.get(0)
                + ")");
          try
          {
            totalOrderQueue.wait();
          }
          catch (InterruptedException ignore)
          {
          }
          index = totalOrderQueue.indexOf(request);
        }
        if (index == -1)
        {
          if (errorIfNotFound)
            logger
                .error("Request was not found in total order queue, posting out of order ("
                    + request + ")");
          return false;
        }
        else
          return true;
      }
    }
    return false;
  }

  /**
   * Remove the first entry of the total order queue and notify the queue so
   * that the next queries can be scheduled.
   */
  public void removeHeadFromAndNotifyTotalOrderQueue()
  {
    if (totalOrderQueue != null)
    {
      synchronized (totalOrderQueue)
      {
        totalOrderQueue.removeFirst();
        totalOrderQueue.notifyAll();
      }
    }
  }

  /**
   * Perform a read request. It is up to the implementation to choose to which
   * backend node(s) this request should be sent.
   * 
   * @param request an <code>SelectRequest</code>
   * @param metadataCache MetadataCache (null if none)
   * @return the corresponding <code>ControllerResultSet</code>
   * @exception SQLException if an error occurs
   */
  public abstract ControllerResultSet execReadRequest(SelectRequest request,
      MetadataCache metadataCache) throws SQLException;

  /**
   * Perform a write request. This request should usually be broadcasted to all
   * nodes.
   * 
   * @param request an <code>AbstractWriteRequest</code>
   * @return number of rows affected by the request
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @exception SQLException if an error occurs
   */
  public abstract int execWriteRequest(AbstractWriteRequest request)
      throws AllBackendsFailedException, SQLException;

  /**
   * Perform a write request and return a ResultSet containing the auto
   * generated keys.
   * 
   * @param request an <code>AbstractWriteRequest</code>
   * @param metadataCache MetadataCache (null if none)
   * @return auto generated keys
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @exception SQLException if an error occurs
   */
  public abstract ControllerResultSet execWriteRequestWithKeys(
      AbstractWriteRequest request, MetadataCache metadataCache)
      throws AllBackendsFailedException, SQLException;

  /**
   * Call a read-only stored procedure that returns a ResultSet. The stored
   * procedure will be executed by one node only.
   * 
   * @param proc the stored procedure call
   * @param metadataCache MetadataCache (null if none)
   * @return a <code>ControllerResultSet</code> value
   * @exception SQLException if an error occurs
   */
  public abstract ControllerResultSet execReadOnlyReadStoredProcedure(
      StoredProcedure proc, MetadataCache metadataCache) throws SQLException;

  /**
   * Call a stored procedure that returns a ResultSet. This stored procedure can
   * possibly perform writes and will therefore be executed by all nodes.
   * 
   * @param proc the stored procedure call
   * @param metadataCache MetadataCache (null if none)
   * @return a <code>ControllerResultSet</code> value
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @exception SQLException if an error occurs
   */
  public abstract ControllerResultSet execReadStoredProcedure(
      StoredProcedure proc, MetadataCache metadataCache)
      throws AllBackendsFailedException, SQLException;

  /**
   * Call a stored procedure that performs an update.
   * 
   * @param proc the stored procedure call
   * @return number of rows affected
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @throws SQLException if an error occurs
   */
  public abstract int execWriteStoredProcedure(StoredProcedure proc)
      throws AllBackendsFailedException, SQLException;

  /**
   * Execute a statement on a backend. If the execution fails, the connection is
   * checked for validity. If the connection was not valid, the query is
   * automatically retried on a new connection.
   * 
   * @param request the request to execute
   * @param backend the backend on which the request is executed
   * @param c connection used to create the statement
   * @param metadataCache MetadataCache (null if none)
   * @return the ControllerResultSet
   * @throws SQLException if an error occurs
   * @throws BadConnectionException if the connection was bad
   */
  public static final ControllerResultSet executeSelectRequestOnBackend(
      SelectRequest request, DatabaseBackend backend, Connection c,
      MetadataCache metadataCache) throws SQLException, BadConnectionException
  {
    ControllerResultSet rs = null;
    try
    {
      backend.addPendingReadRequest(request);
      String sql = request.getSQL();
      // Rewrite the query if needed
      sql = backend.rewriteQuery(sql);

      Statement s; // Can also be used as a PreparedStatement
      if (request.isDriverProcessed() || (request.getSqlSkeleton() == null))
        s = c.createStatement();
      else
      {
        s = c.prepareStatement(request.getSqlSkeleton());
        org.objectweb.cjdbc.driver.PreparedStatement.setPreparedStatement(sql,
            (PreparedStatement) s);
      }

      // Execute the query
      DriverCompliance driverCompliance = backend.getDriverCompliance();
      if (driverCompliance.supportSetQueryTimeout())
        s.setQueryTimeout(request.getTimeout());
      if ((request.getCursorName() != null)
          && (driverCompliance.supportSetCursorName()))
        s.setCursorName(request.getCursorName());
      if ((request.getFetchSize() != 0)
          && driverCompliance.supportSetFetchSize())
        s.setFetchSize(request.getFetchSize());
      if ((request.getMaxRows() > 0) && driverCompliance.supportSetMaxRows())
        s.setMaxRows(request.getMaxRows());
      if (request.isDriverProcessed() || (request.getSqlSkeleton() == null))
        rs = new ControllerResultSet(request, s.executeQuery(sql),
            metadataCache, s);
      else
        rs = new ControllerResultSet(request, ((PreparedStatement) s)
            .executeQuery(), metadataCache, s);
    }
    catch (SQLException e)
    { // Something bad happened
      if (backend.isValidConnection(c))
        throw e; // Connection is valid, throw the exception
      else
        throw new BadConnectionException(e);
    }
    finally
    {
      backend.removePendingRequest(request);
    }
    return rs;
  }

  /**
   * Execute an update prepared statement on a backend. If the execution fails,
   * the connection is checked for validity. If the connection was not valid,
   * the query is automatically retried on a new connection.
   * 
   * @param request the request to execute
   * @param backend the backend on which the request is executed
   * @param c connection used to create the statement
   * @return int Number of rows effected
   * @throws SQLException if an error occurs
   * @throws BadConnectionException if the connection was bad
   */
  public static final int executeUpdateRequestOnBackend(
      AbstractWriteRequest request, DatabaseBackend backend, Connection c)
      throws SQLException, BadConnectionException
  {
    try
    {
      backend.addPendingWriteRequest(request);
      String sql = request.getSQL();
      // Rewrite the query if needed
      sql = backend.rewriteQuery(sql);

      Statement s; // Can also be used as a PreparedStatement
      if (request.isDriverProcessed() || (request.getSqlSkeleton() == null))
        s = c.createStatement();
      else
      {
        s = c.prepareStatement(request.getSqlSkeleton());
        org.objectweb.cjdbc.driver.PreparedStatement.setPreparedStatement(sql,
            (PreparedStatement) s);
      }

      // Execute the query
      DriverCompliance driverCompliance = backend.getDriverCompliance();
      if (driverCompliance.supportSetQueryTimeout())
        s.setQueryTimeout(request.getTimeout());

      int rows = 0;
      if (request.isDriverProcessed() || (request.getSqlSkeleton() == null))
        rows = s.executeUpdate(sql);
      else
        rows = ((PreparedStatement) s).executeUpdate();

      s.close();
      return rows;
    }
    catch (SQLException e)
    { // Something bad happened
      if (backend.isValidConnection(c))
        throw e; // Connection is valid, throw the exception
      else
        throw new BadConnectionException(e);
    }
    finally
    {
      backend.removePendingRequest(request);
    }
  }

  /**
   * Execute an update prepared statement on a backend. If the execution fails,
   * the connection is checked for validity. If the connection was not valid,
   * the query is automatically retried on a new connection.
   * 
   * @param request the request to execute
   * @param backend the backend on which the request is executed
   * @param c connection used to create the statement
   * @param metadataCache MetadataCache (null if none)
   * @return ControllerResultSet containing the auto-generated keys
   * @throws SQLException if an error occurs
   * @throws BadConnectionException if the connection was bad
   */
  public static final ControllerResultSet executeUpdateRequestOnBackendWithKeys(
      AbstractWriteRequest request, DatabaseBackend backend, Connection c,
      MetadataCache metadataCache) throws SQLException, BadConnectionException
  {
    try
    {
      backend.addPendingWriteRequest(request);
      String sql = request.getSQL();
      // Rewrite the query if needed
      sql = backend.rewriteQuery(sql);

      Statement s = c.createStatement();

      // Execute the query
      DriverCompliance driverCompliance = backend.getDriverCompliance();
      if (driverCompliance.supportSetQueryTimeout())
        s.setQueryTimeout(request.getTimeout());
      if (!driverCompliance.supportGetGeneratedKeys())
        throw new SQLException("Backend " + backend.getName()
            + " does not support RETURN_GENERATED_KEYS");

      s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
      ControllerResultSet rs = new ControllerResultSet(request, s
          .getGeneratedKeys(), metadataCache, s);
      return rs;
    }
    catch (SQLException e)
    { // Something bad happened
      if (backend.isValidConnection(c))
        throw e; // Connection is valid, throw the exception
      else
        throw new BadConnectionException(e);
    }
    finally
    {
      backend.removePendingRequest(request);
    }
  }

  /**
   * Execute a read stored procedure on the given backend. The callable
   * statement is setXXX if the driver has not processed the statement.
   * 
   * @param proc the stored procedure to execute
   * @param backend the backend on which to execute the stored procedure
   * @param c the connection on which to execute the stored procedure
   * @param metadataCache the matedatacache to build the ControllerResultSet
   * @return the controllerResultSet
   * @throws SQLException if an error occurs
   * @throws BadConnectionException if the connection was bad
   */
  public static final ControllerResultSet executeReadStoredProcedureOnBackend(
      StoredProcedure proc, DatabaseBackend backend, Connection c,
      MetadataCache metadataCache) throws SQLException, BadConnectionException
  {
    try
    {
      backend.addPendingReadRequest(proc);

      // We suppose here that the request does not modify the schema since
      // it is a read-only query.
      CallableStatement cs;
      if (proc.isDriverProcessed())
        cs = c.prepareCall(proc.getSQL());
      else
      {
        cs = c.prepareCall(proc.getSqlSkeleton());
        org.objectweb.cjdbc.driver.PreparedStatement.setPreparedStatement(proc
            .getSQL(), cs);
      }
      if (backend.getDriverCompliance().supportSetQueryTimeout())
        cs.setQueryTimeout(proc.getTimeout());
      if ((proc.getMaxRows() > 0)
          && backend.getDriverCompliance().supportSetMaxRows())
        cs.setMaxRows(proc.getMaxRows());
      ControllerResultSet rs = new ControllerResultSet(proc, cs.executeQuery(),
          metadataCache, cs);
      return rs;
    }
    catch (SQLException e)
    { // Something bad happened
      if (backend.isValidConnection(c))
        throw e; // Connection is valid, throw the exception
      else
        throw new BadConnectionException(e);
    }
    finally
    {
      backend.removePendingRequest(proc);
    }
  }

  /**
   * Execute a write stored procedure on the given backend. The callable
   * statement is setXXX if the driver has not processed the statement.
   * 
   * @param proc the stored procedure to execute
   * @param backend the backend on which to execute the stored procedure
   * @param c the connection on which to execute the stored procedure
   * @return the number of updated rows
   * @throws SQLException if an error occurs
   * @throws BadConnectionException if the connection was bad
   */
  public static final int executeWriteStoredProcedureOnBackend(
      StoredProcedure proc, DatabaseBackend backend, Connection c)
      throws SQLException, BadConnectionException
  {
    try
    {
      backend.addPendingWriteRequest(proc);

      // We suppose here that the request does not modify the schema since
      // it is a read-only query.
      CallableStatement cs;
      if (proc.isDriverProcessed())
        cs = c.prepareCall(proc.getSQL());
      else
      {
        cs = c.prepareCall(proc.getSqlSkeleton());
        org.objectweb.cjdbc.driver.PreparedStatement.setPreparedStatement(proc
            .getSQL(), cs);
      }
      if (backend.getDriverCompliance().supportSetQueryTimeout())
        cs.setQueryTimeout(proc.getTimeout());
      if ((proc.getMaxRows() > 0)
          && backend.getDriverCompliance().supportSetMaxRows())
        cs.setMaxRows(proc.getMaxRows());
      int rows = cs.executeUpdate();
      cs.close();
      return rows;
    }
    catch (SQLException e)
    { // Something bad happened
      if (backend.isValidConnection(c))
        throw e; // Connection is valid, throw the exception
      else
        throw new BadConnectionException(e);
    }
    finally
    {
      backend.removePendingRequest(proc);
    }
  }

  //
  // Transaction management
  //

  /**
   * Begin a new transaction.
   * 
   * @param tm The transaction marker metadata
   * @throws SQLException if an error occurs
   */
  public abstract void begin(TransactionMarkerMetaData tm) throws SQLException;

  /**
   * Commit a transaction.
   * 
   * @param tm The transaction marker metadata
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @throws SQLException if an error occurs
   */
  public abstract void commit(TransactionMarkerMetaData tm)
      throws AllBackendsFailedException, SQLException;

  /**
   * Rollback a transaction.
   * 
   * @param tm The transaction marker metadata
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @throws SQLException if an error occurs
   */
  public abstract void rollback(TransactionMarkerMetaData tm)
      throws AllBackendsFailedException, SQLException;

  /**
   * Rollback a transaction to a savepoint
   * 
   * @param tm The transaction marker metadata
   * @param savepointName The name of the savepoint
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @throws SQLException if an error occurs
   */
  public abstract void rollback(TransactionMarkerMetaData tm,
      String savepointName) throws AllBackendsFailedException, SQLException;

  /**
   * Set a savepoint to a transaction.
   * 
   * @param tm The transaction marker metadata
   * @param name The name of the new savepoint
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @throws SQLException if an error occurs
   */
  public abstract void setSavepoint(TransactionMarkerMetaData tm, String name)
      throws AllBackendsFailedException, SQLException;

  /**
   * Release a savepoint from a transaction
   * 
   * @param tm The transaction marker metadata
   * @param name The name of the savepoint ro release
   * @throws AllBackendsFailedException if all backends failed to execute the
   *           request
   * @throws SQLException if an error occurs
   */
  public abstract void releaseSavepoint(TransactionMarkerMetaData tm,
      String name) throws AllBackendsFailedException, SQLException;

  /**
   * Factorized code to start a transaction on a backend and to retrieve a
   * connection on this backend
   * 
   * @param backend the backend needed to check valid connection against this
   *          backend test statement
   * @param cm the connection manager to use to retrieve connections
   * @param tid the id of the transaction to start
   * @param transactionIsolationLevel transaction isolation level to use for a
   *          new transaction (does nothing if equals to
   *          Connection.DEFAULT_TRANSACTION_ISOLATION_LEVEL)
   * @return a valid connection with a started transaction
   * @throws SQLException if the backend is valid but set autocommit cannot be
   *           set to false
   * @throws UnreachableBackendException if the backend is not reachable, ie not
   *           valid connection can be retrieved
   * @see org.objectweb.cjdbc.driver.Connection#DEFAULT_TRANSACTION_ISOLATION_LEVEL
   */
  public static final Connection getConnectionAndBeginTransaction(
      DatabaseBackend backend, AbstractConnectionManager cm, long tid,
      int transactionIsolationLevel) throws SQLException,
      UnreachableBackendException
  {
    Connection c = null;
    boolean isConnectionValid = false;
    do
    {
      c = cm.getConnection(tid);

      // Sanity check
      if (c == null)
        throw new UnreachableBackendException(Translate.get(
            "loadbalancer.unable.get.connection", new String[]{
                String.valueOf(tid), backend.getName()}));
      try
      {
        if (transactionIsolationLevel != org.objectweb.cjdbc.driver.Connection.DEFAULT_TRANSACTION_ISOLATION_LEVEL)
          c.setTransactionIsolation(transactionIsolationLevel);
        c.setAutoCommit(false);
        isConnectionValid = true;
      }
      catch (SQLException e)
      {
        if (backend.isValidConnection(c))
          throw e; // Connection is valid, throw the exception
        else
        {
          cm.deleteConnection(tid);
        }
      }
    }
    while (!isConnectionValid);
    return c;
  }

  //
  // Backends management
  //

  /**
   * Enable a backend without further check. The backend is at least read
   * enabled but could also be enabled for writes. Ask the corresponding
   * connection manager to initialize the connections if needed.
   * 
   * @param db The database backend to enable
   * @param writeEnabled True if the backend must be enabled for writes
   * @throws SQLException if an error occurs
   */
  public abstract void enableBackend(DatabaseBackend db, boolean writeEnabled)
      throws SQLException;

  /**
   * Disable a backend without further check. Ask the corresponding connection
   * manager to finalize the connections if needed. This method should not be
   * called directly but instead should access the
   * <code>RequestManager.disableBackeknd(...)</code> method.
   * 
   * @param db The database backend to disable
   * @throws SQLException if an error occurs
   */
  public abstract void disableBackend(DatabaseBackend db) throws SQLException;

  /**
   * Get the number of currently enabled backends. 0 means that no backend is
   * available.
   * 
   * @return number of currently enabled backends
   */
  public abstract int getNumberOfEnabledBackends();

  /**
   * Associate a weight to a backend identified by its logical name.
   * 
   * @param name the backend name
   * @param w the weight
   * @throws SQLException if an error occurs
   */
  public void setWeight(String name, int w) throws SQLException
  {
    throw new SQLException("Weight is not supported by this load balancer");
  }

  //
  // Debug/Monitoring
  //

  /**
   * Get information about the Request Load Balancer
   * 
   * @return <code>String</code> containing information
   */
  public abstract String getInformation();

  /**
   * Get information about the Request Load Balancer in xml
   * 
   * @return <code>String</code> containing information, xml formatted
   */
  public abstract String getXmlImpl();

  /**
   * This sets the macro handler for this load balancer. Handling macros
   * prevents different backends to generate different values when interpreting
   * the macros which could result in data inconsitencies.
   * 
   * @param handler <code>MacrosHandler</code> instance
   */
  public void setMacroHandler(MacrosHandler handler)
  {
    this.macroHandler = handler;
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_LoadBalancer + ">");
    info.append(getXmlImpl());
    info.append("</" + DatabasesXmlTags.ELT_LoadBalancer + ">");
    return info.toString();
  }

  /**
   * @see org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean#getAssociatedString()
   */
  public String getAssociatedString()
  {
    return "loadbalancer";
  }
}
