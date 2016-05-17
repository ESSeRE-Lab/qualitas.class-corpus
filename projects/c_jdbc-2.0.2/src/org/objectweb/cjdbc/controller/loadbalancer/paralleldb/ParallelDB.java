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
 * Contributor(s): Jaco Swart, Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.controller.loadbalancer.paralleldb;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Hashtable;

import javax.management.NotCompliantMBeanException;

import org.objectweb.cjdbc.common.exceptions.BadConnectionException;
import org.objectweb.cjdbc.common.exceptions.NoTransactionStartWhenDisablingException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.UnknownRequest;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.AllBackendsFailedException;
import org.objectweb.cjdbc.controller.requestmanager.RAIDbLevels;
import org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * This class defines a ParallelDB
 *
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
/**
 * These are generic functions for all ParallelDB load balancers.
 * <p>
 * Read and write queries are load balanced on the backends without any
 * replication (assuming that the underlying parallel database takes care of
 * data replication). The load balancers provide failover for reads and writes.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jaco.swart@iblocks.co.uk">Jaco Swart </a>
 * @version 1.0
 */
public abstract class ParallelDB extends AbstractLoadBalancer
{
  //transaction id -> DatabaseBackend
  private Hashtable backendPerTransactionId;
  private int       numberOfEnabledBackends = 0;

  /**
   * Creates a new <code>ParallelDB</code> load balancer with NO_PARSING and a
   * SingleDB RAIDb level.
   * 
   * @param vdb the virtual database this load balancer belongs to.
   * @throws SQLException if an error occurs
   * @throws NotCompliantMBeanException if the MBean is not JMX compliant
   */
  public ParallelDB(VirtualDatabase vdb) throws SQLException,
      NotCompliantMBeanException
  {
    super(vdb, RAIDbLevels.SingleDB, ParsingGranularities.NO_PARSING);
  }

  //
  // Request handling
  //

  /**
   * Choose a backend using the implementation specific load balancing algorithm
   * for read request execution.
   * 
   * @param request request to execute
   * @return the chosen backend
   * @throws SQLException if an error occurs
   */
  public abstract DatabaseBackend chooseBackendForReadRequest(
      AbstractRequest request) throws SQLException;

  /**
   * Choose a backend using the implementation specific load balancing algorithm
   * for write request execution.
   * 
   * @param request request to execute
   * @return the chosen backend
   * @throws SQLException if an error occurs
   */
  public abstract DatabaseBackend chooseBackendForWriteRequest(
      AbstractWriteRequest request) throws SQLException;

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadRequest(SelectRequest,
   *      MetadataCache)
   */
  public ControllerResultSet execReadRequest(SelectRequest request,
      MetadataCache metadataCache) throws SQLException
  {
    DatabaseBackend backend;
    if (request.isAutoCommit())
      backend = chooseBackendForReadRequest(request);
    else
      backend = (DatabaseBackend) backendPerTransactionId.get(new Long(request
          .getTransactionId()));

    if (backend == null)
      throw new SQLException(Translate.get(
          "loadbalancer.execute.no.backend.found", request.getSQLShortForm(vdb
              .getSQLShortFormLength())));

    ControllerResultSet rs = null;
    // Execute the request on the chosen backend
    try
    {
      rs = executeReadRequestOnBackend(request, backend, metadataCache);
    }
    catch (UnreachableBackendException urbe)
    {
      // Try to execute query on different backend
      return execReadRequest(request, metadataCache);
    }
    catch (SQLException se)
    {
      String msg = Translate.get("loadbalancer.request.failed", new String[]{
          String.valueOf(request.getId()), se.getMessage()});
      if (logger.isInfoEnabled())
        logger.info(msg);
      throw new SQLException(msg);
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.request.failed.on.backend",
          new String[]{request.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.error(msg, e);
      throw new SQLException(msg);
    }

    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadOnlyReadStoredProcedure(StoredProcedure,
   *      MetadataCache)
   */
  public ControllerResultSet execReadOnlyReadStoredProcedure(
      StoredProcedure proc, MetadataCache metadataCache) throws SQLException
  {
    return execReadStoredProcedure(proc, metadataCache);
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execReadStoredProcedure(StoredProcedure,
   *      MetadataCache)
   */
  public ControllerResultSet execReadStoredProcedure(StoredProcedure proc,
      MetadataCache metadataCache) throws SQLException
  {
    DatabaseBackend backend;
    if (proc.isAutoCommit())
      backend = chooseBackendForReadRequest(proc);
    else
      backend = (DatabaseBackend) backendPerTransactionId.get(new Long(proc
          .getTransactionId()));

    if (backend == null)
      throw new SQLException(Translate.get(
          "loadbalancer.storedprocedure.no.backend.found", proc
              .getSQLShortForm(vdb.getSQLShortFormLength())));

    ControllerResultSet rs = null;
    // Execute the request on the chosen backend
    try
    {
      rs = executeReadStoredProcedureOnBackend(proc, backend, metadataCache);
    }
    catch (UnreachableBackendException urbe)
    {
      // Try to execute query on different backend
      return execReadStoredProcedure(proc, metadataCache);
    }
    catch (SQLException se)
    {
      String msg = Translate.get("loadbalancer.storedprocedure.failed",
          new String[]{String.valueOf(proc.getId()), se.getMessage()});
      if (logger.isInfoEnabled())
        logger.info(msg);
      throw new SQLException(msg);
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get(
          "loadbalancer.storedprocedure.failed.on.backend", new String[]{
              proc.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.error(msg, e);
      throw new SQLException(msg);
    }

    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execWriteRequest(org.objectweb.cjdbc.common.sql.AbstractWriteRequest)
   */
  public int execWriteRequest(AbstractWriteRequest request)
      throws AllBackendsFailedException, SQLException
  {
    DatabaseBackend backend;
    if (request.isAutoCommit())
      backend = chooseBackendForWriteRequest(request);
    else
      backend = (DatabaseBackend) backendPerTransactionId.get(new Long(request
          .getTransactionId()));

    if (backend == null)
      throw new SQLException(Translate.get(
          "loadbalancer.execute.no.backend.found", request.getSQLShortForm(vdb
              .getSQLShortFormLength())));

    int result;
    // Execute the request on the chosen backend
    try
    {
      result = executeWriteRequestOnBackend(request, backend);
    }
    catch (UnreachableBackendException urbe)
    {
      // Try to execute query on different backend
      return execWriteRequest(request);
    }
    catch (SQLException se)
    {
      String msg = Translate.get("loadbalancer.request.failed", new String[]{
          String.valueOf(request.getId()), se.getMessage()});
      if (logger.isInfoEnabled())
        logger.info(msg);
      throw new SQLException(msg);
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.request.failed.on.backend",
          new String[]{request.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.error(msg, e);
      throw new SQLException(msg);
    }

    return result;
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execWriteRequestWithKeys(AbstractWriteRequest,
   *      MetadataCache)
   */
  public ControllerResultSet execWriteRequestWithKeys(
      AbstractWriteRequest request, MetadataCache metadataCache)
      throws AllBackendsFailedException, SQLException
  {
    DatabaseBackend backend;
    if (request.isAutoCommit())
      backend = chooseBackendForWriteRequest(request);
    else
      backend = (DatabaseBackend) backendPerTransactionId.get(new Long(request
          .getTransactionId()));

    if (backend == null)
      throw new SQLException(Translate.get(
          "loadbalancer.execute.no.backend.found", request.getSQLShortForm(vdb
              .getSQLShortFormLength())));

    ControllerResultSet rs;
    // Execute the request on the chosen backend
    try
    {
      rs = executeWriteRequestWithKeysOnBackend(request, backend, metadataCache);
    }
    catch (UnreachableBackendException urbe)
    {
      // Try to execute query on different backend
      return execWriteRequestWithKeys(request, metadataCache);
    }
    catch (SQLException se)
    {
      String msg = Translate.get("loadbalancer.request.failed", new String[]{
          String.valueOf(request.getId()), se.getMessage()});
      if (logger.isInfoEnabled())
        logger.info(msg);
      throw new SQLException(msg);
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.request.failed.on.backend",
          new String[]{request.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.error(msg, e);
      throw new SQLException(msg);
    }

    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execWriteStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public int execWriteStoredProcedure(StoredProcedure proc) throws SQLException
  {
    DatabaseBackend backend;
    if (proc.isAutoCommit())
      backend = chooseBackendForReadRequest(proc);
    else
      backend = (DatabaseBackend) backendPerTransactionId.get(new Long(proc
          .getTransactionId()));

    if (backend == null)
      throw new SQLException(Translate.get(
          "loadbalancer.storedprocedure.no.backend.found", proc
              .getSQLShortForm(vdb.getSQLShortFormLength())));

    int result;
    // Execute the request on the chosen backend
    try
    {
      result = executeWriteStoredProcedureOnBackend(proc, backend);
    }
    catch (UnreachableBackendException urbe)
    {
      // Try to execute query on different backend
      return execWriteStoredProcedure(proc);
    }
    catch (SQLException se)
    {
      String msg = Translate.get("loadbalancer.storedprocedure.failed",
          new String[]{String.valueOf(proc.getId()), se.getMessage()});
      if (logger.isInfoEnabled())
        logger.info(msg);
      throw new SQLException(msg);
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get(
          "loadbalancer.storedprocedure.failed.on.backend", new String[]{
              proc.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.error(msg, e);
      throw new SQLException(msg);
    }

    return result;
  }

  /**
   * Execute a read request on the selected backend.
   * 
   * @param request the request to execute
   * @param backend the backend that will execute the request
   * @param metadataCache MetadataCache (null if none)
   * @return the ControllerResultSet
   * @throws SQLException if an error occurs
   */
  private ControllerResultSet executeReadRequestOnBackend(
      SelectRequest request, DatabaseBackend backend,
      MetadataCache metadataCache) throws SQLException,
      UnreachableBackendException
  {
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
          throw new UnreachableBackendException(Translate.get(
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
   * @param metadataCache MetadataCache (null if none)
   * @return the ControllerResultSet
   * @throws SQLException if an error occurs
   */
  private ControllerResultSet executeReadStoredProcedureOnBackend(
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
        throw new UnreachableBackendException(Translate.get(
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
   * Execute a write request on the selected backend.
   * 
   * @param request the request to execute
   * @param backend the backend that will execute the request
   * @return the number of modified rows
   * @throws SQLException if an error occurs
   */
  private int executeWriteRequestOnBackend(AbstractWriteRequest request,
      DatabaseBackend backend) throws SQLException, UnreachableBackendException
  {
    if (backend == null)
      throw new SQLException(Translate.get(
          "loadbalancer.execute.no.backend.available", request.getId()));

    try
    {
      AbstractConnectionManager cm = backend.getConnectionManager(request
          .getLogin());
      if (request.isAutoCommit())
      { // Use a connection just for this request
        Connection c = null;
        try
        {
          c = cm.getConnection();
        }
        catch (UnreachableBackendException e1)
        {
          String backendName = backend.getName();
          logger.error(Translate.get(
              "loadbalancer.backend.disabling.unreachable", backendName));
          disableBackend(backend);
          throw new UnreachableBackendException(Translate.get(
              "loadbalancer.backend.unreacheable", backendName));
        }

        // Sanity check
        if (c == null)
          throw new UnreachableBackendException(Translate.get(
              "loadbalancer.backend.no.connection", backend.getName()));

        // Execute Query
        int result;
        try
        {
          result = executeUpdateRequestOnBackend(request, backend, c);
        }
        catch (Exception e)
        {
          throw new SQLException(Translate.get(
              "loadbalancer.request.failed.on.backend", new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName(), e.getMessage()}));
        }
        finally
        {
          cm.releaseConnection(c);
        }
        return result;
      }
      else
      { // Re-use the connection used by this transaction
        Connection c = cm.retrieveConnection(request.getTransactionId());

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
              "loadbalancer.unable.retrieve.connection",
              new String[]{String.valueOf(request.getTransactionId()),
                  backend.getName()}));

        // Execute Query
        int result;
        try
        {
          result = executeUpdateRequestOnBackend(request, backend, c);
          return result;
        }
        catch (Exception e)
        {
          throw new SQLException(Translate.get(
              "loadbalancer.request.failed.on.backend", new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName(), e.getMessage()}));
        }
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.request.failed.on.backend",
          new String[]{request.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
    }
  }

  /**
   * Execute a write request on the selected backend and return the
   * autogenerated keys.
   * 
   * @param request the request to execute
   * @param backend the backend that will execute the request
   * @param metadataCache MetadataCache (null if none)
   * @return the ResultSet containing the auto-generated keys
   * @throws SQLException if an error occurs
   */
  private ControllerResultSet executeWriteRequestWithKeysOnBackend(
      AbstractWriteRequest request, DatabaseBackend backend,
      MetadataCache metadataCache) throws SQLException,
      UnreachableBackendException
  {
    if (backend == null)
      throw new SQLException(Translate.get(
          "loadbalancer.execute.no.backend.available", request.getId()));

    if (!backend.getDriverCompliance().supportGetGeneratedKeys())
      throw new SQLException(Translate.get(
          "loadbalancer.backend.autogeneratedkeys.unsupported", backend
              .getName()));

    try
    {
      AbstractConnectionManager cm = backend.getConnectionManager(request
          .getLogin());
      if (request.isAutoCommit())
      { // Use a connection just for this request
        Connection c = null;
        try
        {
          c = cm.getConnection();
        }
        catch (UnreachableBackendException e1)
        {
          String backendName = backend.getName();
          logger.error(Translate.get(
              "loadbalancer.backend.disabling.unreachable", backendName));
          disableBackend(backend);
          throw new UnreachableBackendException(Translate.get(
              "loadbalancer.backend.unreacheable", backendName));
        }

        // Sanity check
        if (c == null)
          throw new UnreachableBackendException(Translate.get(
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
          throw new SQLException(Translate.get(
              "loadbalancer.request.failed.on.backend", new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName(), e.getMessage()}));
        }
        finally
        {
          cm.releaseConnection(c);
        }
        return result;
      }
      else
      { // Re-use the connection used by this transaction
        Connection c = cm.retrieveConnection(request.getTransactionId());

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
              "loadbalancer.unable.retrieve.connection",
              new String[]{String.valueOf(request.getTransactionId()),
                  backend.getName()}));

        // Execute Query
        try
        {
          return executeUpdateRequestOnBackendWithKeys(request, backend, c,
              metadataCache);
        }
        catch (Exception e)
        {
          throw new SQLException(Translate.get(
              "loadbalancer.request.failed.on.backend", new String[]{
                  request.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName(), e.getMessage()}));
        }
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
  }

  /**
   * Execute a stored procedure on the selected backend.
   * 
   * @param proc the stored procedure to execute
   * @param backend the backend that will execute the request
   * @return the ResultSet
   * @throws SQLException if an error occurs
   */
  private int executeWriteStoredProcedureOnBackend(StoredProcedure proc,
      DatabaseBackend backend) throws SQLException, UnreachableBackendException
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
        throw new UnreachableBackendException(Translate.get(
            "loadbalancer.backend.no.connection", backend.getName()));

      // Execute Query
      int result;
      try
      {
        result = AbstractLoadBalancer.executeWriteStoredProcedureOnBackend(
            proc, backend, c);

        // Warning! No way to detect if schema has been modified unless
        // we ask the backend again using DatabaseMetaData.getTables().
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
      return result;
    }
    else
    { // Inside a transaction
      Connection c;
      long tid = proc.getTransactionId();
      Long lTid = new Long(tid);

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
      int result;
      try
      {
        result = AbstractLoadBalancer.executeWriteStoredProcedureOnBackend(
            proc, backend, c);

        // Warning! No way to detect if schema has been modified unless
        // we ask the backend again using DatabaseMetaData.getTables().
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
      return result;
    }
  }

  //
  // Transaction Management
  //

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#getInformation()
   */
  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#begin(org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData)
   */
  public void begin(TransactionMarkerMetaData tm) throws SQLException
  {
    Long lTid = new Long(tm.getTransactionId());
    if (backendPerTransactionId.containsKey(lTid))
      throw new SQLException(Translate.get(
          "loadbalancer.transaction.already.started", lTid.toString()));

    DatabaseBackend backend = chooseBackendForReadRequest(new UnknownRequest(
        "begin", false, 0, "\n"));
    backendPerTransactionId.put(lTid, backend);
    backend.startTransaction(lTid);
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#commit(org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData)
   */
  public void commit(TransactionMarkerMetaData tm) throws SQLException
  {
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);
    DatabaseBackend db = (DatabaseBackend) backendPerTransactionId.remove(lTid);

    AbstractConnectionManager cm = db.getConnectionManager(tm.getLogin());
    Connection c = cm.retrieveConnection(tid);

    // Sanity check
    if (c == null)
    { // Bad connection
      db.stopTransaction(lTid);

      throw new SQLException(Translate.get(
          "loadbalancer.unable.retrieve.connection", new String[]{
              String.valueOf(tid), db.getName()}));
    }

    // Execute Query
    try
    {
      c.commit();
      c.setAutoCommit(true);
    }
    catch (Exception e)
    {
      String msg = Translate.get("loadbalancer.commit.failed", new String[]{
          String.valueOf(tid), db.getName(), e.getMessage()});
      logger.error(msg);
      throw new SQLException(msg);
    }
    finally
    {
      cm.releaseConnection(tid);
      db.stopTransaction(lTid);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#rollback(org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData)
   */
  public void rollback(TransactionMarkerMetaData tm) throws SQLException
  {
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);
    DatabaseBackend db = (DatabaseBackend) backendPerTransactionId.remove(lTid);

    AbstractConnectionManager cm = db.getConnectionManager(tm.getLogin());
    Connection c = cm.retrieveConnection(tid);

    // Sanity check
    if (c == null)
    { // Bad connection
      db.stopTransaction(lTid);

      throw new SQLException(Translate.get(
          "loadbalancer.unable.retrieve.connection", new String[]{
              String.valueOf(tid), db.getName()}));
    }

    // Execute Query
    try
    {
      c.rollback();
      c.setAutoCommit(true);
    }
    catch (Exception e)
    {
      String msg = Translate.get("loadbalancer.rollback.failed", new String[]{
          String.valueOf(tid), db.getName(), e.getMessage()});
      logger.error(msg);
      throw new SQLException(msg);
    }
    finally
    {
      cm.releaseConnection(tid);
      db.stopTransaction(lTid);
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
    long tid = tm.getTransactionId();
    Long lTid = new Long(tid);
    DatabaseBackend db = (DatabaseBackend) backendPerTransactionId.remove(lTid);

    AbstractConnectionManager cm = db.getConnectionManager(tm.getLogin());
    Connection c = cm.retrieveConnection(tid);

    // Sanity check
    if (c == null)
    { // Bad connection
      db.stopTransaction(lTid);

      throw new SQLException(Translate.get(
          "loadbalancer.unable.retrieve.connection", new String[]{
              String.valueOf(tid), db.getName()}));
    }

    // Retrieve savepoint 
    Savepoint savepoint = db.getSavepoint(lTid, savepointName);
    if (savepoint == null)
    {
      throw new SQLException(Translate.get(
          "loadbalancer.unable.retrieve.savepoint", new String[]{savepointName,
              String.valueOf(tid), db.getName()}));
    }
    
    // Execute Query
    try
    {
      c.rollback(savepoint);
    }
    catch (Exception e)
    {
      String msg = Translate.get("loadbalancer.rollbacksavepoint.failed",
          new String[]{savepointName, String.valueOf(tid), db.getName(),
          e.getMessage()});
      logger.error(msg);
      throw new SQLException(msg);
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
    
    DatabaseBackend db = (DatabaseBackend) backendPerTransactionId.get(lTid);
    AbstractConnectionManager cm = db.getConnectionManager(tm.getLogin());
    Connection c = cm.retrieveConnection(tid);

    // Sanity check
    if (c == null)
    { // Bad connection
      db.stopTransaction(lTid);

      throw new SQLException(Translate.get(
          "loadbalancer.unable.retrieve.connection", new String[]{
              String.valueOf(tid), db.getName()}));
    }

    // Retrieve savepoint 
    Savepoint savepoint = db.getSavepoint(lTid, name);
    if (savepoint == null)
    {
      throw new SQLException(Translate.get(
          "loadbalancer.unable.retrieve.savepoint", new String[]{
              String.valueOf(tid), name, db.getName()}));
    }
    
    // Execute Query
    try
    {
      c.releaseSavepoint(savepoint);
    }
    catch (Exception e)
    {
      String msg = Translate.get("loadbalancer.releasesavepoint.failed",
          new String[]{name ,String.valueOf(tid), db.getName(),
              e.getMessage()});
      logger.error(msg);
      throw new SQLException(msg);
    }
    finally
    {
      db.removeSavepoint(lTid, savepoint);
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
    
    DatabaseBackend db = (DatabaseBackend) backendPerTransactionId.get(lTid);
    AbstractConnectionManager cm = db.getConnectionManager(tm.getLogin());
    Connection c = cm.retrieveConnection(tid);

    // Sanity check
    if (c == null)
    { // Bad connection
      db.stopTransaction(lTid);

      throw new SQLException(Translate.get(
          "loadbalancer.unable.retrieve.connection", new String[]{
              String.valueOf(tid), db.getName()}));
    }

    // Execute Query
    Savepoint savepoint = null;
    try
    {
      savepoint = c.setSavepoint(name);
    }
    catch (Exception e)
    {
      String msg = Translate.get("loadbalancer.setsavepoint.failed",
          new String[]{name, String.valueOf(tid), db.getName(),
              e.getMessage()});
      logger.error(msg);
      throw new SQLException(msg);
    }
    finally
    {
      if (savepoint != null)
        db.addSavepoint(lTid, savepoint);
    }
  }
  
  /**
   * Enables a backend that was previously disabled. Asks the corresponding
   * connection manager to initialize the connections if needed.
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
    logger.info(Translate.get("loadbalancer.backend.enabling", db.getName()));
    if (!db.isInitialized())
      db.initializeConnections();
    db.enableRead();
    if (writeEnabled)
      db.enableWrite();
    numberOfEnabledBackends++;
  }

  /**
   * Disables a backend that was previously enabled. Asks the corresponding
   * connection manager to finalize the connections if needed.
   * <p>
   * No sanity checks are performed by this function.
   * 
   * @param db the database backend to disable
   * @throws SQLException if an error occurs
   */
  public void disableBackend(DatabaseBackend db) throws SQLException
  {
    logger.info(Translate.get("loadbalancer.backend.disabling", db.getName()));
    numberOfEnabledBackends--;
    db.disable();
    if (db.isInitialized())
      db.finalizeConnections();
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#getNumberOfEnabledBackends()
   */
  public int getNumberOfEnabledBackends()
  {
    return numberOfEnabledBackends = 0;
  }

  //
  // Debug/Monitoring
  //

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#getXmlImpl
   */
  public String getXmlImpl()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_ParallelDB + ">");
    info.append(getParallelDBXml());
    info.append("</" + DatabasesXmlTags.ELT_ParallelDB + ">");
    return info.toString();
  }

  /**
   * Return the XML tags of the ParallelDB load balancer implementation.
   * 
   * @return content of ParallelDB xml
   */
  public abstract String getParallelDBXml();

}