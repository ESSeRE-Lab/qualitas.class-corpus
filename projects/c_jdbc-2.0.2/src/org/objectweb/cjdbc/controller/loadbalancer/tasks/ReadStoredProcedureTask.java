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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.loadbalancer.tasks;

import java.sql.Connection;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.NoTransactionStartWhenDisablingException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * Executes a <code>StoredProcedure</code> call that returns a ResultSet.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ReadStoredProcedureTask extends AbstractTask
{
  private StoredProcedure     proc;
  private ControllerResultSet result;
  private MetadataCache       metadataCache;

  /**
   * Creates a new <code>ReadStoredProcedureTask</code>.
   * 
   * @param nbToComplete number of threads that must succeed before returning
   * @param totalNb total number of threads
   * @param proc the <code>StoredProcedure</code> to call
   * @param metadataCache the metadataCache if any or null
   */
  public ReadStoredProcedureTask(int nbToComplete, int totalNb,
      StoredProcedure proc, MetadataCache metadataCache)
  {
    super(nbToComplete, totalNb);
    this.proc = proc;
    this.metadataCache = metadataCache;
  }

  /**
   * Call a stored procedure that returns a ResultSet on the given backend
   * thread.
   * 
   * @param backendThread the backend thread that will execute the task
   * @throws SQLException if an error occurs
   */
  public void executeTask(BackendWorkerThread backendThread)
      throws SQLException
  {
    DatabaseBackend backend = backendThread.getBackend();

    AbstractConnectionManager cm = backend
        .getConnectionManager(proc.getLogin());
    if (cm == null)
    {
      SQLException se = new SQLException(
          "No Connection Manager for Virtual Login:" + proc.getLogin());
      try
      {
        notifyFailure(backendThread, 1, se);
      }
      catch (SQLException ignore)
      {

      }
      throw se;
    }

    Trace logger = backendThread.getLogger();
    if (proc.isAutoCommit())
    { // Use a connection just for this request
      Connection c = null;
      try
      {
        c = cm.getConnection();
      }
      catch (UnreachableBackendException e1)
      {
        SQLException se = new SQLException("Backend " + backend.getName()
            + " is no more reachable.");
        try
        {
          notifyFailure(backendThread, 1, se);
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the backend
        // thread
        backendThread.kill();
        logger.error("Disabling backend " + backend.getName()
            + " because it is no more reachable.");
        throw se;
      }

      // Sanity check
      if (c == null)
      {
        SQLException se = new SQLException("No more connections");
        try
        { // All backends failed, just ignore
          if (!notifyFailure(backendThread, (long) proc.getTimeout() * 1000, se))
            return;
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the backend
        // thread
        backendThread.kill();
        String msg = "Stored procedure '"
            + proc.getSQLShortForm(backend.getSQLShortFormLength())
            + "' failed on backend " + backend.getName() + " but "
            + getSuccess() + " succeeded (" + se + ")";
        logger.error(msg);
        throw new SQLException(msg);
      }

      // Execute Query
      try
      {
        result = AbstractLoadBalancer.executeReadStoredProcedureOnBackend(proc,
            backend, c, metadataCache);
        
        backend.setSchemaIsDirty(true);
      }
      catch (Exception e)
      {
        try
        { // All backends failed, just ignore
          if (!notifyFailure(backendThread, (long) proc.getTimeout() * 1000, e))
            return;
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the backend
        // thread
        backendThread.kill();
        String msg = "Stored procedure '"
            + proc.getSQLShortForm(backend.getSQLShortFormLength())
            + "' failed on backend " + backend.getName() + " but "
            + getSuccess() + " succeeded (" + e + ")";
        logger.error(msg);
        throw new SQLException(msg);
      }
      finally
      {
        cm.releaseConnection(c);
      }
    }
    else
    { // Re-use the connection used by this transaction
      Connection c;
      long tid = proc.getTransactionId();
      Long lTid = new Long(tid);

      try
      {
        c = backend.getConnectionForTransactionAndLazyBeginIfNeeded(lTid, cm,
            proc.getTransactionIsolation());
      }
      catch (UnreachableBackendException ube)
      {
        SQLException se = new SQLException("Backend " + backend.getName()
            + " is no more reachable.");
        try
        {
          notifyFailure(backendThread, 1, se);
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the backend
        // thread
        backendThread.kill();
        logger.error("Disabling backend " + backend.getName()
            + " because it is no more reachable.");
        throw se;
      }
      catch (NoTransactionStartWhenDisablingException e)
      {
        logger
            .error("Disabling backend "
                + backend.getName()
                + " has been assigned a select request but it cannot start a new transaction for it.");
        notifyFailure(backendThread, (long) proc.getTimeout() * 1000, e);
        return;
      }
      catch (SQLException e1)
      {
        SQLException se = new SQLException(
            "Unable to get connection for transaction " + tid);
        try
        { // All backends failed, just ignore
          if (!notifyFailure(backendThread, (long) proc.getTimeout() * 1000, se))
            return;
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the
        // backend thread
        backendThread.kill();
        String msg = "Request '"
            + proc.getSQLShortForm(backend.getSQLShortFormLength())
            + "' failed on backend " + backend.getName() + " but "
            + getSuccess() + " succeeded (" + se + ")";
        logger.error(msg);
        throw new SQLException(msg);
      }

      // Sanity check
      if (c == null)
      { // Bad connection
        SQLException se = new SQLException(
            "Unable to retrieve connection for transaction " + tid);
        try
        { // All backends failed, just ignore
          if (!notifyFailure(backendThread, (long) proc.getTimeout() * 1000, se))
            return;
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the
        // backend thread
        backendThread.kill();
        String msg = "Request '"
            + proc.getSQLShortForm(backend.getSQLShortFormLength())
            + "' failed on backend " + backend.getName() + " but "
            + getSuccess() + " succeeded (" + se + ")";
        logger.error(msg);
        throw new SQLException(msg);
      }

      // Execute Query
      try
      {
        result = AbstractLoadBalancer.executeReadStoredProcedureOnBackend(proc,
            backend, c, metadataCache);

        backend.setSchemaIsDirty(true);
      }
      catch (Exception e)
      {
        try
        { // All backends failed, just ignore
          if (!notifyFailure(backendThread, (long) proc.getTimeout() * 1000, e))
            return;
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the backend
        // thread
        backendThread.kill();
        String msg = "Stored procedure '"
            + proc.getSQLShortForm(backend.getSQLShortFormLength())
            + "' failed on backend " + backend.getName() + " but "
            + getSuccess() + " succeeded (" + e + ")";
        logger.error(msg);
        throw new SQLException(msg);
      }
    }
    notifySuccess();
  }

  /**
   * Returns the result.
   * 
   * @return a <code>ResultSet</code>
   */
  public ControllerResultSet getResult()
  {
    return result;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "ReadStoredProcedureTask (" + proc.getSQL() + ")";
  }
}