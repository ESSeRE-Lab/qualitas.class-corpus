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
 * Contributor(s): Julie Marguerite.
 */

package org.objectweb.cjdbc.controller.loadbalancer.tasks;

import java.sql.Connection;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.NoTransactionStartWhenDisablingException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;

/**
 * Task to begin a transaction. Note that this task does not properly set the
 * transaction isolation but this is not a real issue since it is meant to be
 * used by the recovery log that does not execute reads and provide its own
 * serial order.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @version 1.0
 */
public class BeginTask extends AbstractTask
{
  /** Login used by the connection. */
  private String login;

  /** Unique transaction identifier. */
  private long   transactionId;

  /** Request timeout in milliseconds. */
  private long   timeout;

  /**
   * Begins a new transaction given a login and a transaction id.
   * 
   * @param nbToComplete number of threads that must succeed before returning
   * @param totalNb total number of threads
   * @param timeout request timeout in milliseconds
   * @param login the login used by the connection
   * @param transactionId a unique transaction identifier
   */
  public BeginTask(int nbToComplete, int totalNb, long timeout, String login,
      long transactionId)
  {
    super(nbToComplete, totalNb);
    this.login = login;
    this.transactionId = transactionId;
    this.timeout = timeout;
  }

  /**
   * Begins a new transaction with the given backend thread.
   * 
   * @param backendThread the backend thread that will execute the task
   * @exception SQLException if an error occurs
   */
  public void executeTask(BackendWorkerThread backendThread)
      throws SQLException
  {
    DatabaseBackend backend = backendThread.getBackend();
    if (backend.isDisabling())
    {
      // Backend is disabling, we do not execute queries except the one in the
      // transaction we already started. Just notify the completion for the
      // others.
      notifyCompletion();
      return;
    }

    try
    {
      AbstractConnectionManager cm = backend.getConnectionManager(login);
      if (cm == null)
      {
        SQLException se = new SQLException(
            "No Connection Manager for Virtual Login:" + login);
        try
        {
          notifyFailure(backendThread, 1, se);
        }
        catch (SQLException ignore)
        {

        }
        throw se;
      }

      Connection c;
      Long lTid = new Long(transactionId);
      Trace logger = backendThread.getLogger();

      try
      {
        c = backend
            .getConnectionForTransactionAndLazyBeginIfNeeded(
                lTid,
                cm,
                org.objectweb.cjdbc.driver.Connection.DEFAULT_TRANSACTION_ISOLATION_LEVEL);
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
            "Unable to get connection for transaction " + lTid);
        try
        { // All backends failed, just ignore
          if (!notifyFailure(backendThread, timeout * 1000, se))
            return;
        }
        catch (SQLException ignore)
        {
        }
        // Disable this backend (it is no more in sync) by killing the
        // backend thread
        backendThread.kill();
        String msg = "Begin of transaction " + transactionId
            + " failed on backend " + backend.getName() + " but "
            + getSuccess() + " succeeded (" + se + ")";
        logger.error(msg);
        throw new SQLException(msg);
      }

      // Sanity check
      if (c == null)
      { // Bad connection
        SQLException se = new SQLException(
            "No more connection to start a new transaction.");
        try
        { // All backends failed, just ignore
          if (!notifyFailure(backendThread, timeout, se))
            return;
        }
        catch (SQLException ignore)
        {
        }
      }
      else
      {
        notifyCompletion();
      }
    }
    catch (Exception e)
    {
      try
      {
        if (!notifyFailure(backendThread, timeout, new SQLException(e
            .getMessage())))
          return;
      }
      catch (SQLException ignore)
      {
      }
      String msg = "Failed to begin transaction " + transactionId
          + " on backend " + backend.getName() + " (" + e + ")";
      backendThread.getLogger().error(msg);
      throw new SQLException(msg);
    }
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "BeginTask (" + transactionId + ")";
  }
}