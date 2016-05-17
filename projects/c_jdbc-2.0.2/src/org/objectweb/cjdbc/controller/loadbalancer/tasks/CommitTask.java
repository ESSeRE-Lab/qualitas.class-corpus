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

import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;

/**
 * Task to commit a transaction.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @version 1.0
 */
public class CommitTask extends AbstractTask
{
  /** Login used by the connection. */
  private String login;

  /** Unique transaction identifier. */
  private long   transactionId;

  /** Request timeout in milliseconds. */
  private long   timeout;

  /**
   * Commits a transaction given a login and a transaction id.
   * 
   * @param nbToComplete number of threads that must succeed before returning
   * @param totalNb total number of threads
   * @param timeout request timeout in ms
   * @param login the login used by the connection
   * @param transactionId a unique transaction identifier
   */
  public CommitTask(int nbToComplete, int totalNb, long timeout, String login,
      long transactionId)
  {
    super(nbToComplete, totalNb);
    this.login = login;
    this.transactionId = transactionId;
    this.timeout = timeout;
  }

  /**
   * Commits a transaction with the given backend thread.
   * 
   * @param backendThread the backend thread that will execute the task
   * @throws SQLException if an error occurs
   */
  public void executeTask(BackendWorkerThread backendThread) throws SQLException
  {
    DatabaseBackend db = backendThread.getBackend();
    Long lTid = new Long(transactionId);

    AbstractConnectionManager cm = db.getConnectionManager(login);
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
    Connection c = cm.retrieveConnection(transactionId);

    // Sanity check
    if (c == null)
    { // Bad connection
      db.stopTransaction(lTid);
      SQLException se = new SQLException(
          "Unable to retrieve connection for transaction " + transactionId);

      try
      { // All backends failed, just ignore
        if (!notifyFailure(backendThread, timeout, se))
          return;
      }
      catch (SQLException ignore)
      {
      }
      // Disable this backend (it is no more in sync) by killing the backend
      // thread
      backendThread.kill();
      String msg = "Failed to commit transaction " + transactionId
          + " on backend " + db.getName() + " but " + getSuccess()
          + " succeeded (" + se + ")";
      backendThread.getLogger().error(msg);
      throw new SQLException(msg);
    }

    // Execute Query
    try
    {
      c.commit();
      c.setAutoCommit(true);
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
      // Disable this backend (it is no more in sync) by killing the backend
      // thread
      backendThread.kill();
      String msg = "Failed to commit transaction " + transactionId
          + " on backend " + db.getName() + " but " + getSuccess()
          + " succeeded (" + e + ")";
      backendThread.getLogger().error(msg);
      throw new SQLException(msg);
    }
    finally
    {
      cm.releaseConnection(transactionId);
      db.stopTransaction(lTid);
    }
    notifySuccess();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "CommitTask (" + transactionId + ")";
  }
}