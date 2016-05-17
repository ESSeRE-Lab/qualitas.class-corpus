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
 * Contributor(s): Vadim Kassin, Jaco Swart, Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.controller.loadbalancer.singledb;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.objectweb.cjdbc.common.exceptions.BadConnectionException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
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
 * Single Database request load balancer.
 * <p>
 * The requests coming from the request controller are directly forwarded to the
 * single backend. This load balancer does not support multiple backends.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:vadim@kase.kz">Vadim Kassin </a>
 * @author <a href="mailto:jaco.swart@iblocks.co.uk">Jaco Swart </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class SingleDB extends AbstractLoadBalancer
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

  private DatabaseBackend backend;

  private static Trace    logger = Trace
                                     .getLogger("org.objectweb.cjdbc.controller.loadbalancer.SingleDB");

  /*
   * Constructors
   */

  /**
   * Creates a new <code>SingleDB</code> instance.
   * 
   * @param vdb the <code>VirtualDatabase</code> this load balancer belongs to
   * @throws Exception if there is not exactly one backend attached to the
   *           <code>VirtualDatabase</code>
   */
  public SingleDB(VirtualDatabase vdb) throws Exception
  {
    // We don't need to parse the requests, just send them to the backend
    super(vdb, RAIDbLevels.SingleDB, ParsingGranularities.NO_PARSING);
  }

  /*
   * Request Handling
   */

  /**
   * Performs a read request. It is up to the implementation to choose to which
   * backend node(s) this request should be sent.
   * 
   * @param request an <code>SelectRequest</code>
   * @param metadataCache MetadataCache (null if none)
   * @return the corresponding <code>java.sql.ResultSet</code>
   * @exception SQLException if an error occurs
   */
  public ControllerResultSet execReadRequest(SelectRequest request,
      MetadataCache metadataCache) throws SQLException
  {
    if (backend == null)
      throw new SQLException(Translate.get(
          "loadbalancer.execute.no.backend.available", request.getId()));

    try
    {
      AbstractConnectionManager cm = backend.getConnectionManager(request
          .getLogin());
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
            String backendName = backend.getName();
            logger.error(Translate.get(
                "loadbalancer.backend.disabling.unreachable", backendName));
            disableBackend(backend);
            backend = null;
            throw new SQLException(Translate.get(
                "loadbalancer.backend.unreacheable", backendName));
          }

          // Sanity check
          if (c == null)
            throw new SQLException(Translate.get(
                "loadbalancer.backend.no.connection", backend.getName()));

          // Execute Query
          try
          {
            rs = executeSelectRequestOnBackend(request, backend, c,
                metadataCache);
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
        return rs;
      }
      else
      {
        long tid = request.getTransactionId();
        // Re-use the connection used by this transaction
        Connection c = cm.retrieveConnection(tid);

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
          throw new SQLException(Translate.get(
              "loadbalancer.connection.failed", new String[]{
                  String.valueOf(tid), backend.getName(), e.getMessage()}));
        }
        return rs;
      }
    }
    catch (RuntimeException e)
    {
      String msg = "Request '"
          + request.getSQLShortForm(vdb.getSQLShortFormLength())
          + "' failed on backend " + backend.getURL() + " (" + e + ")";
      logger.fatal(msg, e);
      throw new SQLException(msg);
    }
  }

  /**
   * Performs a write request on the backend.
   * 
   * @param request an <code>AbstractWriteRequest</code>
   * @return number of rows affected by the request
   * @exception SQLException if an error occurs
   */
  public int execWriteRequest(AbstractWriteRequest request) throws SQLException
  {
    if (backend == null)
      throw new SQLException(Translate.get(
          "loadbalancer.execute.no.backend.available", request.getId()));

    try
    {
      AbstractConnectionManager cm = backend.getConnectionManager(request
          .getLogin());
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
          String backendName = backend.getName();
          logger.error(Translate.get(
              "loadbalancer.backend.disabling.unreachable", backendName));
          disableBackend(backend);
          backend = null;
          throw new SQLException(Translate.get(
              "loadbalancer.backend.unreacheable", backendName));
        }

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
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
        try
        {
          return executeUpdateRequestOnBackend(request, backend, c);
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
   * @see AbstractLoadBalancer#execWriteRequestWithKeys(AbstractWriteRequest,
   *      MetadataCache)
   */
  public ControllerResultSet execWriteRequestWithKeys(
      AbstractWriteRequest request, MetadataCache metadataCache)
      throws SQLException
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
          String backendName = backend.getName();
          logger.error(Translate.get(
              "loadbalancer.backend.disabling.unreachable", backendName));
          disableBackend(backend);
          backend = null;
          throw new SQLException(Translate.get(
              "loadbalancer.backend.unreacheable", backendName));
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
      {
        // Re-use the connection used by this transaction
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
        finally
        {
          backend.removePendingRequest(request);
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
    if (backend == null)
      throw new SQLException(
          "No available backend to execute stored procedure " + proc.getId());

    try
    {
      AbstractConnectionManager cm = backend.getConnectionManager(proc
          .getLogin());
      if (proc.isAutoCommit())
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
          backend = null;
          throw new SQLException(Translate.get(
              "loadbalancer.backend.unreacheable", backendName));
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
        return rs;
      }
      else
      { // Re-use the connection used by this transaction
        Connection c = cm.retrieveConnection(proc.getTransactionId());

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
              "loadbalancer.unable.retrieve.connection", new String[]{
                  String.valueOf(proc.getTransactionId()), backend.getName()}));

        // Execute Query
        try
        {
          return AbstractLoadBalancer.executeReadStoredProcedureOnBackend(proc,
              backend, c, metadataCache);
        }
        catch (Exception e)
        {
          throw new SQLException(Translate.get(
              "loadbalancer.storedprocedure.failed.on.backend", new String[]{
                  proc.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName(), e.getMessage()}));
        }
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get(
          "loadbalancer.storedprocedure.failed.on.backend", new String[]{
              proc.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#execWriteStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public int execWriteStoredProcedure(StoredProcedure proc) throws SQLException
  {
    if (backend == null)
      throw new SQLException(
          "No available backend to execute stored procedure " + proc.getId());

    try
    {
      AbstractConnectionManager cm = backend.getConnectionManager(proc
          .getLogin());
      if (proc.isAutoCommit())
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
          backend = null;
          throw new SQLException(Translate.get(
              "loadbalancer.backend.unreacheable", backendName));
        }

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
              "loadbalancer.backend.no.connection", backend.getName()));

        // Execute Query
        int result;
        try
        {
          result = AbstractLoadBalancer.executeWriteStoredProcedureOnBackend(
              proc, backend, c);
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
        return result;
      }
      else
      { // Re-use the connection used by this transaction
        Connection c = cm.retrieveConnection(proc.getTransactionId());

        // Sanity check
        if (c == null)
          throw new SQLException(Translate.get(
              "loadbalancer.unable.retrieve.connection", new String[]{
                  String.valueOf(proc.getTransactionId()), backend.getName()}));

        // Execute Query
        try
        {
          return AbstractLoadBalancer.executeWriteStoredProcedureOnBackend(
              proc, backend, c);
        }
        catch (Exception e)
        {
          throw new SQLException(Translate.get(
              "loadbalancer.storedprocedure.failed.on.backend", new String[]{
                  proc.getSQLShortForm(vdb.getSQLShortFormLength()),
                  backend.getName(), e.getMessage()}));
        }
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get(
          "loadbalancer.storedprocedure.failed.on.backend", new String[]{
              proc.getSQLShortForm(vdb.getSQLShortFormLength()),
              backend.getName(), e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
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
  public void begin(TransactionMarkerMetaData tm) throws SQLException
  {
    if (backend == null)
      throw new SQLException("No available backend to begin transaction "
          + tm.getTransactionId());

    // We do not accept new transactions if we are disabling the backend
    if (backend.isDisabling())
      throw new SQLException(Translate.get("loadbalancer.backend.is.disabling",
          new String[]{"begin transaction " + tm.getTransactionId(),
              backend.getName()}));

    try
    {
      Connection c = backend.getConnectionManager(tm.getLogin()).getConnection(
          tm.getTransactionId());

      if (c == null)
        throw new SQLException(Translate.get(
            "loadbalancer.backend.no.connection", backend.getName()));

      c.setAutoCommit(false);
    }
    catch (Exception e)
    {
      throw new SQLException("Begin of transaction " + tm.getTransactionId()
          + " failed on backend " + backend.getURL() + " (" + e + ")");
    }
  }

  /**
   * Commits a transaction.
   * 
   * @param tm the transaction marker metadata
   * @exception SQLException if an error occurs
   */
  public void commit(TransactionMarkerMetaData tm) throws SQLException
  {
    if (backend == null)
      throw new SQLException("No available backend to commit transaction "
          + tm.getTransactionId());

    try
    {
      AbstractConnectionManager cm = backend
          .getConnectionManager(tm.getLogin());
      Connection c = cm.retrieveConnection(tm.getTransactionId());

      if (c == null)
        throw new SQLException("No connection found for transaction "
            + tm.getTransactionId());

      try
      {
        c.commit();
        c.setAutoCommit(true);
      }
      catch (SQLException e)
      {
        throw new SQLException(Translate.get("loadbalancer.commit.failed",
            new String[]{String.valueOf(tm.getTransactionId()),
                backend.getName(), e.getMessage()}));
      }
      finally
      {
        cm.releaseConnection(tm.getTransactionId());
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.commit.failed", new String[]{
          String.valueOf(tm.getTransactionId()), backend.getName(),
          e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
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
    if (backend == null)
      throw new SQLException("No available backend to rollback transaction "
          + tm.getTransactionId());

    try
    {
      AbstractConnectionManager cm = backend
          .getConnectionManager(tm.getLogin());
      Connection c = cm.retrieveConnection(tm.getTransactionId());

      if (c == null)
        throw new SQLException("No connection found for transaction "
            + tm.getTransactionId());

      try
      {
        c.rollback();
        c.setAutoCommit(true);
      }
      catch (SQLException e)
      {
        throw new SQLException(Translate.get("loadbalancer.rollback.failed",
            new String[]{String.valueOf(tm.getTransactionId()),
                backend.getName(), e.getMessage()}));
      }
      finally
      {
        cm.releaseConnection(tm.getTransactionId());
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.rollback.failed", new String[]{
          String.valueOf(tm.getTransactionId()), backend.getName(),
          e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
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
    if (backend == null)
      throw new SQLException("No available backend to rollback transaction "
          + tm.getTransactionId());

    try
    {
      AbstractConnectionManager cm = backend
          .getConnectionManager(tm.getLogin());
      Connection c = cm.retrieveConnection(tm.getTransactionId());

      if (c == null)
        throw new SQLException("No connection found for transaction "
            + tm.getTransactionId());

      Savepoint savepoint = backend.getSavepoint(new Long(
          tm.getTransactionId()), savepointName);
      
      if (savepoint == null)
        throw new SQLException("No savepoint with name " + savepointName
            + " has been found for transaction " + tm.getTransactionId());
      
      try
      {
        c.rollback(savepoint);
      }
      catch (SQLException e)
      {
        throw new SQLException(Translate.get(
            "loadbalancer.rollbacksavepoint.failed", new String[]{
                savepointName, String.valueOf(tm.getTransactionId()),
                backend.getName(), e.getMessage()}));
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.rollbacksavepoint.failed",
          new String[]{savepointName, String.valueOf(tm.getTransactionId()),
          backend.getName(), e.getMessage()});
      logger.fatal(msg, e);
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
    if (backend == null)
      throw new SQLException("No available backend to release savepoint from "
          + " transaction " + tm.getTransactionId());

    try
    {
      AbstractConnectionManager cm = backend.getConnectionManager(
          tm.getLogin());
      Connection c = cm.retrieveConnection(tm.getTransactionId());

      if (c == null)
        throw new SQLException("No connection found for transaction "
            + tm.getTransactionId());

      Savepoint savepoint = backend.getSavepoint(new Long(
          tm.getTransactionId()), name);
      
      if (savepoint == null)
        throw new SQLException("No savepoint with name " + name + " has been "
            + "found for transaction " + tm.getTransactionId());

      try
      {
        c.releaseSavepoint(savepoint);
      }
      catch (SQLException e)
      {
        throw new SQLException(Translate.get(
            "loadbalancer.releasesavepoint.failed", new String[]{name,
                String.valueOf(tm.getTransactionId()), backend.getName(),
                e.getMessage()}));
      }
      finally
      {
        backend.removeSavepoint(new Long(tm.getTransactionId()), savepoint);
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.releasesavepoint.failed",
          new String[]{name, String.valueOf(tm.getTransactionId()),
              backend.getName(), e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
    }
  }

  /**
   * Set a savepoint to a transaction.
   * 
   * @param tm The transaction marker metadata
   * @param name The name of the new savepoint
   * @throws AllBackendsFailedException if no backend succeeded in setting the
   *           savepoint.
   * @throws SQLException if an error occurs
   */
  public void setSavepoint(TransactionMarkerMetaData tm, String name)
      throws AllBackendsFailedException, SQLException
  {
    if (backend == null)
      throw new SQLException("No available backend to set savepoint to "
          + " transaction " + tm.getTransactionId());

    try
    {
      AbstractConnectionManager cm = backend
          .getConnectionManager(tm.getLogin());
      Connection c = cm.retrieveConnection(tm.getTransactionId());

      if (c == null)
        throw new SQLException("No connection found for transaction "
            + tm.getTransactionId());

      Savepoint savepoint = null;
      try
      {
        savepoint = c.setSavepoint(name);
      }
      catch (SQLException e)
      {
        throw new SQLException(Translate.get(
            "loadbalancer.setsavepoint.failed", new String[]{
                name, String.valueOf(tm.getTransactionId()), backend.getName(),
                e.getMessage()}));
      }
      finally
      {
        if (savepoint != null)
          backend.addSavepoint(new Long(tm.getTransactionId()), savepoint);
      }
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("loadbalancer.setsavepoint.failed",
          new String[]{name, String.valueOf(tm.getTransactionId()),
              backend.getName(), e.getMessage()});
      logger.fatal(msg, e);
      throw new SQLException(msg);
    }
  }

  /*
   * Backends management
   */

  /**
   * Enables a backend that was previously disabled. Asks the corresponding
   * connection manager to initialize the connections if needed.
   * 
   * @param db the database backend to enable
   * @param writeEnabled True if the backend must be enabled for writes
   * @throws SQLException if an error occurs
   */
  public void enableBackend(DatabaseBackend db, boolean writeEnabled)
      throws SQLException
  {
    if (backend != null)
    {
      if (backend.isReadEnabled())
        throw new SQLException(
            "SingleDB load balancer accepts only one backend and "
                + backend.getName() + " is already enabled. Skipping "
                + db.getName() + " initialization.");
    }
    backend = db;
    logger.info(Translate.get("loadbalancer.backend.enabling", db.getName()));
    if (!backend.isInitialized())
      backend.initializeConnections();
    backend.enableRead();
    if (writeEnabled)
      backend.enableWrite();
  }

  /**
   * Disables a backend that was previously enabled. Asks the corresponding
   * connection manager to finalize the connections if needed.
   * 
   * @param db the database backend to disable
   * @throws SQLException if an error occurs
   */
  public void disableBackend(DatabaseBackend db) throws SQLException
  {
    if (backend.equals(db))
    {
      logger
          .info(Translate.get("loadbalancer.backend.disabling", db.getName()));
      backend.disable();
      if (backend.isInitialized())
        backend.finalizeConnections();
      backend = null;
    }
    else
    {
      String msg = "Trying to disable a non-existing backend " + db.getName();
      logger.warn(msg);
      throw new SQLException(msg);
    }
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
    if (backend == null)
      return 0;
    else
      return 1;
  }

  /*
   * Debug/Monitoring
   */

  /**
   * Gets information about the request load balancer
   * 
   * @return <code>String</code> containing information
   */
  public String getInformation()
  {
    if (backend == null)
      return "SingleDB Request load balancer: !!!Warning!!! No enabled backend node found\n";
    else
      return "SingleDB Request load balancer using " + backend.getURL() + "\n";
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#getXmlImpl()
   */
  public String getXmlImpl()
  {
    return "<" + DatabasesXmlTags.ELT_SingleDB + "/>";
  }

}