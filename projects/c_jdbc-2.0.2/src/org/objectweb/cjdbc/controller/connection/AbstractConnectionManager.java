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
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.controller.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;

/**
 * A <code>ConnectionManager</code> object is responsible to talk directly
 * with a database backend.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class AbstractConnectionManager
    implements
      XmlComponent,
      Cloneable
{
  /*
   * How the code is organized ? 1. Member variables 2. Connection handling 3.
   * Getter/Setter (possibly in alphabetical order)
   */

  /** Logger instance. */
  static Trace                logger = Trace
                                         .getLogger("org.objectweb.cjdbc.controller.connection");

  /** URL of the <code>DatabaseBackend</code> owning this connection manager. */
  protected String            backendUrl;

  /**
   * Name of the <code>DatabaseBackend</code> owning this connection manager.
   */
  protected String            backendName;

  /** Backend connection login to be used by this connection manager. */
  protected String            rLogin;

  /** Backend connection password to be used by this connection manager. */
  protected String            rPassword;

  /** The class name of the driver */
  protected String            driverClassName;

  /**
   * The path to the driver if null the default directory is used
   */
  protected String            driverPath;

  /** <code>true</code> if the connection pool has been initialized. */
  protected boolean           initialized;

  /** Hastable of connections associated to a transaction. */
  private transient Hashtable connectionForTransaction;

  /** Virtual Login to be that use this connection manager */
  private String              vLogin;

  /*
   * Constructor(s)
   */

  /**
   * Creates a new <code>AbstractConnectionManager</code> instance: assigns
   * login/password and instanciates transaction id/connection mapping.
   * 
   * @param backendUrl URL of the <code>DatabaseBackend</code> owning this
   *          connection manager
   * @param backendName name of the <code>DatabaseBackend</code> owning this
   *          connection manager
   * @param rLogin backend connection login to be used by this connection
   *          manager
   * @param rPassword backend connection password to be used by this connection
   *          manager
   * @param driverPath path for driver
   * @param driverClassName class name for driver
   */
  public AbstractConnectionManager(String backendUrl, String backendName,
      String rLogin, String rPassword, String driverPath, String driverClassName)
  {
    if (backendUrl == null)
      throw new IllegalArgumentException(
          "Illegal null database backend URL in AbstractConnectionManager constructor");

    if (backendName == null)
      throw new IllegalArgumentException(
          "Illegal null database backend name in AbstractConnectionManager constructor");

    if (rLogin == null)
      throw new IllegalArgumentException(
          "Illegal null database backend login in AbstractConnectionManager constructor");

    if (rPassword == null)
      throw new IllegalArgumentException(
          "Illegal null database backend password in AbstractConnectionManager constructor");

    if (driverPath != null)
    {
      if (driverClassName == null)
      {
        throw new IllegalArgumentException(
            "Illegal null database backend driverClassName in AbstractConnectionManager constructor");
      }
    }
    this.backendUrl = backendUrl;
    this.backendName = backendName;
    this.rLogin = rLogin;
    this.rPassword = rPassword;
    this.driverPath = driverPath;
    this.driverClassName = driverClassName;
    connectionForTransaction = new Hashtable();

  }

  /**
   * Copy this connection manager and replace the name of the backend and its
   * url Every other parameter is the same
   * 
   * @param url the url to the backend associated to this ConnectionManager
   * @param name the name of the backend
   * @return <code>AbstractConnectionManager</code>
   * @throws Exception if clone fails
   */
  public AbstractConnectionManager copy(String url, String name)
      throws Exception
  {
    AbstractConnectionManager connectionManager = (AbstractConnectionManager) this
        .clone();
    connectionManager.backendName = name;
    connectionManager.backendUrl = url;
    return connectionManager;
  }

  /*
   * Connection handling
   */

  /**
   * Initializes the connection(s) to the database. The caller must ensure that
   * the driver has already been loaded else an exception will be thrown.
   * 
   * @exception SQLException if an error occurs.
   */
  public abstract void initializeConnections() throws SQLException;

  /**
   * Releases all the connections to the database.
   * 
   * @exception SQLException if an error occurs.
   */
  public abstract void finalizeConnections() throws SQLException;

  /**
   * Get a connection from DriverManager.
   * 
   * @return a new connection or null if Driver.getConnection() failed.
   * @see DriverManager#getConnection(String, String, String, String, String)
   */
  public Connection getConnectionFromDriver()

  {
    try
    {
      return DriverManager.getConnection(backendUrl, rLogin, rPassword,
          driverPath, driverClassName);
    }
    catch (SQLException ignore)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("failed to get connection for driver ", ignore);
      }
      return null;
    }
  }

  /**
   * Gets a connection from the pool (round-robin).
   * 
   * @return a <code>Connection</code> or <code>null</code> if no connection
   *         is available or if the connection has not been initialized.
   * @throws UnreachableBackendException if the backend must be disabled
   */
  public abstract Connection getConnection() throws UnreachableBackendException;

  /**
   * Gets a new connection for a transaction. This function calls
   * {@link #getConnection()}to get the connection and store the mapping
   * between the connection and the transaction id.
   * 
   * @param transactionId the transaction id.
   * @return a <code>Connection</code> or <code>null</code> if no connection
   *         is available .
   * @throws UnreachableBackendException if the backend must be disabled
   * @see #getConnection()
   */
  public Connection getConnection(long transactionId)
      throws UnreachableBackendException
  {
    Long lTid = new Long(transactionId);
    Connection c = getConnection();
    if (c != null)
    {
      if (connectionForTransaction.put(lTid, c) != null)
      {
        logger
            .error("A new connection for transaction "
                + lTid
                + " has been opened but there was a remaining connection for this transaction that has not been closed.");
      }
    }
    return c;
  }

  /**
   * Retrieves a connection used for a transaction. This connection must have
   * been allocated by calling {@link #getConnection(long)}.
   * 
   * @param transactionId the transaction id.
   * @return a <code>Connection</code> or <code>null</code> if no connection
   *         has been found for this transaction id.
   * @see #getConnection(long)
   */
  public Connection retrieveConnection(long transactionId)
  {
    Long id = new Long(transactionId);
    synchronized (connectionForTransaction)
    {
      return (Connection) connectionForTransaction.get(id);
    }
  }

  /**
   * Releases a connection.
   * 
   * @param connection the connection to release.
   */
  public abstract void releaseConnection(Connection connection);

  /**
   * Releases a connection used for a transaction. The corresponding connection
   * is released by calling {@link #releaseConnection(Connection)}.
   * 
   * @param transactionId the transaction id.
   * @see #releaseConnection(Connection)
   */
  public void releaseConnection(long transactionId)
  {
    Connection c = (Connection) connectionForTransaction.remove(new Long(
        transactionId));

    if (c == null)
      logger.error(Translate.get("connection.transaction.unknown",
          transactionId));
    else
      releaseConnection(c);
  }

  /**
   * Delete a connection that is no more valid.
   * 
   * @param connection the connection to delete.
   */
  public abstract void deleteConnection(Connection connection);

  /**
   * Delete a bad connection that was used for a transaction. The corresponding
   * connection is deleted by calling {@link #deleteConnection(Connection)}.
   * 
   * @param transactionId the transaction id.
   * @see #releaseConnection(Connection)
   */
  public void deleteConnection(long transactionId)
  {
    Connection c = (Connection) connectionForTransaction.remove(new Long(
        transactionId));

    if (c == null)
      logger.error(Translate.get("connection.transaction.unknown",
          transactionId));
    else
      deleteConnection(c);
  }

  /**
   * Tests if the connections have been initialized.
   * 
   * @return <code>true</code> if the connections have been initialized.
   */
  public boolean isInitialized()
  {
    return initialized;
  }

  /*
   * Getter/setter methods
   */

  /**
   * Returns the login used by this connection manager.
   * 
   * @return a <code>String</code> value.
   */
  public String getLogin()
  {
    return rLogin;
  }

  /**
   * Sets the login to be used by this connection manager.
   * 
   * @param rLogin the login to set.
   */
  public void setLogin(String rLogin)
  {
    this.rLogin = rLogin;
  }

  /**
   * Returns the password used by this connection manager.
   * 
   * @return a <code>String</code> value.
   */
  public String getPassword()
  {
    return rPassword;
  }

  /**
   * Sets the password to be used by this connection manager.
   * 
   * @param rPassword the password to set.
   */
  public void setPassword(String rPassword)
  {
    this.rPassword = rPassword;
  }

  /*
   * Debug/monitoring information
   */

  /**
   * Gets xml formatted information on this connection manager
   * 
   * @return xml formatted string that conforms to c-jdbc.dtd
   */
  public abstract String getXmlImpl();

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_ConnectionManager + " "
        + DatabasesXmlTags.ATT_vLogin + "=\"" + vLogin + "\"  " + ""
        + DatabasesXmlTags.ATT_rLogin + "=\"" + rLogin + "\"  " + ""
        + DatabasesXmlTags.ATT_rPassword + "=\"" + rPassword + "\"  " + ">");
    info.append(this.getXmlImpl());
    info.append("</" + DatabasesXmlTags.ELT_ConnectionManager + ">");
    return info.toString();
  }

  /**
   * Ensures that the connections are closed when the object is garbage
   * collected.
   * 
   * @exception Throwable if an error occurs.
   */
  protected void finalize() throws Throwable
  {
    if (isInitialized())
      finalizeConnections();
    super.finalize();
  }

  /**
   * @return Returns the vLogin.
   */
  public String getVLogin()
  {
    return vLogin;
  }

  /**
   * @param login The vLogin to set.
   */
  public void setVLogin(String login)
  {
    vLogin = login;
  }

  /**
   * Get the current number of connections open for this connection manager.
   * 
   * @return the current number of open connections
   */
  public abstract int getCurrentNumberOfConnections();

  /**
   * Returns the driverClassName value.
   * 
   * @return Returns the driverClassName.
   */
  public String getDriverClassName()
  {
    return driverClassName;
  }

  /**
   * Sets the driverClassName value.
   * 
   * @param driverClassName The driverClassName to set.
   */
  public void setDriverClassName(String driverClassName)
  {
    this.driverClassName = driverClassName;
  }

  /**
   * Returns the driverPath value.
   * 
   * @return Returns the driverPath.
   */
  public String getDriverPath()
  {
    return driverPath;
  }

  /**
   * Sets the driverPath value.
   * 
   * @param driverPath The driverPath to set.
   */
  public void setDriverPath(String driverPath)
  {
    this.driverPath = driverPath;
  }

  protected abstract Object clone() throws CloneNotSupportedException;
}