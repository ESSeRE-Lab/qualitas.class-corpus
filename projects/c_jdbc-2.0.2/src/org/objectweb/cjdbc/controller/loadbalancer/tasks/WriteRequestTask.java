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
 * Contributor(s): Julie Marguerite, Jaco Swart.
 */

package org.objectweb.cjdbc.controller.loadbalancer.tasks;

import java.sql.Connection;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.NoTransactionStartWhenDisablingException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;

/**
 * Executes an <code>AbstractWriteRequest</code> statement.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:jaco.swart@iblocks.co.uk">Jaco Swart </a>
 * @version 1.0
 */
public class WriteRequestTask extends AbstractTask
{
  private AbstractWriteRequest request;
  private int                  result;

  /**
   * Creates a new <code>WriteRequestTask</code>.
   * 
   * @param nbToComplete number of threads that must succeed before returning
   * @param totalNb total number of threads
   * @param request an <code>AbstractWriteRequest</code>
   */
  public WriteRequestTask(int nbToComplete, int totalNb,
      AbstractWriteRequest request)
  {
    super(nbToComplete, totalNb);
    this.request = request;
  }

  /**
   * Executes a write request with the given backend thread.
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
    {
      if (backend.isDisabling())
      {
        // Backend is disabling, we do not execute queries except the one in the
        // transaction we already started. Just notify the completion for the
        // others.
        notifyCompletion();
        return;
      }

      // Use a connection just for this request
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
          {
            return;
          }
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the backend
        // thread
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
        result = AbstractLoadBalancer.executeUpdateRequestOnBackend(request,
            backend, c);

        backend.updateDatabaseBackendSchema(request);
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
        // Disable this backend (it is no more in sync) by killing the backend
        // thread
        backendThread.kill();
        String msg = "Request '"
            + request.getSQLShortForm(backend.getSQLShortFormLength())
            + "' failed on backend " + backend.getName() + " but "
            + getSuccess() + " succeeded (" + e + ")";

        if (logger.isDebugEnabled())
          logger.debug(msg, e);
        else
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
        // Backend is disabling, we do not execute queries except the one in the
        // transaction we already started. Just notify the completion for the
        // others.
        notifyCompletion();
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
        result = AbstractLoadBalancer.executeUpdateRequestOnBackend(request,
            backend, c);

        backend.updateDatabaseBackendSchema(request);
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
        // Disable this backend (it is no more in sync) by killing the backend
        // thread
        backendThread.kill();
        String msg = "Request '"
            + request.getSQLShortForm(backend.getSQLShortFormLength())
            + "' failed on backend " + backend.getName() + " but "
            + getSuccess() + " succeeded (" + e + ")";
        if (logger.isDebugEnabled())
          logger.debug(msg, e);
        else
          logger.error(msg);
        throw new SQLException(msg);
      }
    }
    notifySuccess();
  }

  /**
   * Returns the result.
   * 
   * @return int
   */
  public int getResult()
  {
    return result;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    if (request.isAutoCommit())
      return "WriteAutocommit Task (" + request.getSQL() + ")";
    else
      return "Write Task from transaction:" + request.getTransactionId() + "("
          + request.getSQL() + ")";
  }

}