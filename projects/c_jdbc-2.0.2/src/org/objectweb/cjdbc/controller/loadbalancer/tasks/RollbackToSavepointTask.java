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
 * Initial developer(s): Jean-Bernard van Zuylen.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.loadbalancer.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;

/**
 * This class defines a RollbackToSavepointTask
 * 
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class RollbackToSavepointTask extends AbstractTask
{
  /** Login used by the connection. */
  private String login;

  /** Unique transaction identifier. */
  private long   transactionId;

  /** Request timeout in milliseconds. */
  private long   timeout;

  /** Name of the savepoint. */
  private String savepointName;
  
  /**
   * Creates a new <code>RollbackToSavepointTask</code> object
   * 
   * @param nbToComplete number of threads that must succeed before returning
   * @param totalNb total number of threads
   * @param timeout request timeout in ms
   * @param login the login used by the connection
   * @param transactionId a unique transaction identifier
   * @param savepointName the name of the savepoint
   */
  public RollbackToSavepointTask(int nbToComplete, int totalNb, long timeout,
      String login, long transactionId, String savepointName)
  {
    super(nbToComplete, totalNb);
    this.login = login;
    this.transactionId = transactionId;
    this.timeout = timeout;
    this.savepointName = savepointName;
  }
  
  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.tasks.AbstractTask#executeTask(org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread)
   */
  public void executeTask(BackendWorkerThread backendThread)
      throws SQLException
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
      String msg = "Failed to rollback transaction " + transactionId
          + " to savepoint " + savepointName + " on backend " + db.getName()
          + " but " + getSuccess() + " succeeded (" + se + ")";
      backendThread.getLogger().error(msg);
      throw new SQLException(msg);
    }

    // Execute Query
    boolean hasSavepoint = false;
    try
    {
      hasSavepoint = db.hasSavepointForTransaction(lTid, savepointName);
      if (hasSavepoint)
      {
        Savepoint savepoint = db.getSavepoint(lTid, savepointName);
        c.rollback(savepoint);
      }
      else
      {
        c.rollback();
        c.setAutoCommit(true);
      }
      // Flag the schema as dirty in case the transaction contained a DDL
      backendThread.getBackend().setSchemaIsDirty(true);
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
      String msg = "Failed to rollback transaction " + transactionId
          + " to savepoint " + savepointName + " on backend " + db.getName()
          + " but " + getSuccess() + " succeeded (" + e + ")";
      backendThread.getLogger().error(msg);
      throw new SQLException(msg);
    }
    finally
    {
      if (! hasSavepoint)
      {
        cm.releaseConnection(transactionId);
        db.stopTransaction(lTid);
      }
    }
    notifySuccess();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "RollbackToSavepointTask for transaction " + transactionId + " ("
        + savepointName + ")";
  }
}
