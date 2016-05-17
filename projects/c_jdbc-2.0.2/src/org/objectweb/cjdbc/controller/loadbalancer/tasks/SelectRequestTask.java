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
 * Contributor(s): Julie Marguerite, Jaco Swart.
 */

package org.objectweb.cjdbc.controller.loadbalancer.tasks;

import java.sql.Connection;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.NoTransactionStartWhenDisablingException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * Executes a <code>SELECT</code> statement.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:jaco.swart@iblocks.co.uk">Jaco Swart </a>
 * @version 1.0
 */
public class SelectRequestTask extends AbstractTask
{
  private SelectRequest       request;
  private ControllerResultSet result;

  /**
   * Creates a new <code>WriteRequestTask</code> instance.
   * 
   * @param nbToComplete number of threads that must succeed before returning
   * @param totalNb total number of threads
   * @param request an <code>AbstractWriteRequest</code>
   */
  public SelectRequestTask(int nbToComplete, int totalNb, SelectRequest request)
  {
    super(nbToComplete, totalNb);
    this.request = request;
  }

  /**
   * Executes a write request with the given backend thread
   * 
   * @param backendThread the backend thread that will execute the task
   * @throws SQLException if an error occurs
   */
  public void executeTask(BackendWorkerThread backendThread)
      throws SQLException
  {
    DatabaseBackend backend = backendThread.getBackend();

    AbstractConnectionManager cm = backend.getConnectionManager(request
        .getLogin());
    if (cm == null)
    {
      SQLException se = new SQLException(
          "No Connection Manager for Virtual Login:" + request.getLogin());
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
    if (request.isAutoCommit())
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
          if (!notifyFailure(backendThread, (long) request.getTimeout() * 1000,
              se))
            return;
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the backend
        // thread
        backendThread.kill();
        throw new SQLException("Request '"
            + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH)
            + "' failed on backend " + backend.getName() + " (" + se + ")");
      }

      // Execute Query
      try
      {
        result = AbstractLoadBalancer.executeSelectRequestOnBackend(request,
            backend, c, null);
      }
      catch (Exception e)
      {
        try
        { // All backends failed, just ignore
          if (!notifyFailure(backendThread, (long) request.getTimeout() * 1000,
              e))
            return;
        }
        catch (SQLException ignore)
        {
        }
        throw new SQLException("Request '"
            + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH)
            + "' failed on backend " + backend.getName() + " (" + e + ")");
      }
      finally
      {
        cm.releaseConnection(c);
      }
    }
    else
    {
      Connection c;
      long tid = request.getTransactionId();
      Long lTid = new Long(tid);

      try
      {
        c = backend.getConnectionForTransactionAndLazyBeginIfNeeded(lTid, cm,
            request.getTransactionIsolation());
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
        notifyFailure(backendThread, (long) request.getTimeout() * 1000, e);
        return;
      }
      catch (SQLException e1)
      {
        SQLException se = new SQLException(
            "Unable to get connection for transaction " + tid);
        try
        { // All backends failed, just ignore
          if (!notifyFailure(backendThread, (long) request.getTimeout() * 1000,
              se))
            return;
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the
        // backend thread
        backendThread.kill();
        String msg = "Request '"
            + request.getSQLShortForm(backend.getSQLShortFormLength())
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
          if (!notifyFailure(backendThread, (long) request.getTimeout() * 1000,
              se))
            return;
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the
        // backend thread
        backendThread.kill();
        String msg = "Request '"
            + request.getSQLShortForm(backend.getSQLShortFormLength())
            + "' failed on backend " + backend.getName() + " but "
            + getSuccess() + " succeeded (" + se + ")";
        logger.error(msg);
        throw new SQLException(msg);
      }

      // Execute Query
      try
      {
        result = AbstractLoadBalancer.executeSelectRequestOnBackend(request,
            backend, c, null);
      }
      catch (Exception e)
      {
        try
        { // All backends failed, just ignore
          if (!notifyFailure(backendThread, (long) request.getTimeout() * 1000,
              e))
            return;
        }
        catch (SQLException ignore)
        {
        }
        throw new SQLException("Request '"
            + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH)
            + "' failed on backend " + backend.getName() + " (" + e + ")");
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
    return "SelectRequestTask (" + request.getSQL() + ")";
  }
}