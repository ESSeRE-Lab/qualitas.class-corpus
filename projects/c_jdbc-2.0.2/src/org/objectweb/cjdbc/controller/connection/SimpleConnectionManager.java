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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.connection;

import java.sql.Connection;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This connection manager creates a new <code>Connection</code> every time
 * the {@link #getConnection}method is called.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class SimpleConnectionManager extends AbstractConnectionManager
{
  private int nbOfConnections = 0;

  /**
   * Creates a new <code>SimpleConnectionManager</code> instance.
   * 
   * @param backendUrl URL of the <code>DatabaseBackend</code> owning this
   *          connection manager.
   * @param backendName name of the <code>DatabaseBackend</code> owning this
   *          connection manager.
   * @param login backend connection login to be used by this connection
   *          manager.
   * @param password backend connection password to be used by this connection
   *          manager.
   * @param driverPath path for driver
   * @param driverClassName class name for driver
   */
  public SimpleConnectionManager(String backendUrl, String backendName,
      String login, String password, String driverPath, String driverClassName)
  {
    super(backendUrl, backendName, login, password, driverPath, driverClassName);
  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    return new SimpleConnectionManager(backendUrl, backendName, rLogin,
        rPassword, driverPath, driverClassName);
  }

  /**
   * Does nothing.
   * 
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#initializeConnections()
   */
  public void initializeConnections() throws SQLException
  {
    initialized = true;
  }

  /**
   * Does nothing.
   * 
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#finalizeConnections()
   */
  public void finalizeConnections() throws SQLException
  {
    initialized = false;
  }

  /**
   * Gets a new connection from the underlying driver.
   * 
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getConnection()
   */
  public Connection getConnection() throws UnreachableBackendException
  {
    if (!initialized)
    {
      logger
          .error("Requesting a connection from a non-initialized connection manager");
      return null;
    }

    addConnection();
    Connection c = getConnectionFromDriver();
    if (c == null)
    {
      removeConnection();
      logger.error("Unable to get connection from " + backendUrl);
      if (nbOfConnections == 0)
      {
        logger.error("Backend '" + backendUrl + "' is considered unreachable. "
            + "(No active connection and none can be opened)");
        throw new UnreachableBackendException();
      }
    }
    return c;
  }

  /**
   * Closes the connection.
   * 
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#releaseConnection(Connection)
   */
  public void releaseConnection(Connection connection)
  {
    removeConnection();
    try
    {
      connection.close();
    }
    catch (SQLException e)
    {
      logger.error("Failed to close connection for '" + backendUrl + "'", e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#deleteConnection(Connection)
   */
  public void deleteConnection(Connection c)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getCurrentNumberOfConnections()
   */
  public int getCurrentNumberOfConnections()
  {
    return nbOfConnections;
  }

  private synchronized void addConnection()
  {
    nbOfConnections++;
  }

  private synchronized void removeConnection()
  {
    nbOfConnections--;
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getXmlImpl()
   */
  public String getXmlImpl()
  {
    return "<" + DatabasesXmlTags.ELT_SimpleConnectionManager + "/>";
  }

}