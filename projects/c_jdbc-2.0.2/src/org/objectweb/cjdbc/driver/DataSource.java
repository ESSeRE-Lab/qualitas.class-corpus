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
 * Initial developer(s): Marek Prochazka. 
 * Contributor(s):
 */

package org.objectweb.cjdbc.driver;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

/**
 * An implementation of the JDBC 2.0 optional package <code>DataSource</code>
 * interface. It allows to set the URL, user name, and password to its
 * properties. It can be bound via JNDI so that the properties can be set by an
 * "application server" and a "ready-to-use" reference to <code>DataSource</code>
 * can be retrieved via JNDI.
 * 
 * @author <a href="mailto:Marek.Prochazka@inrialpes.fr">Marek Prochazka </a>
 * @version 1.0
 */
public class DataSource
    implements
      javax.sql.DataSource,
      Referenceable,
      Serializable
{

  /** DataSource properties */
  protected static final String URL_PROPERTY         = "url";
  protected static final String USER_PROPERTY        = Driver.USER_PROPERTY;
  protected static final String PASSWORD_PROPERTY    = Driver.PASSWORD_PROPERTY;
  protected static final String DRIVER_CLASSNAME     = "org.objectweb.cjdbc.driver.Driver";
  protected static final String FACTORY_CLASSNAME    = "org.objectweb.cjdbc.driver.DataSourceFactory";
  protected static final String DESCRIPTION_PROPERTY = "description";

  /** Wrapped driver for to get connections. */
  protected static Driver       driver               = null;
  static
  {
    try
    {
      driver = (Driver) Class.forName(DRIVER_CLASSNAME).newInstance();
    }
    catch (Exception e)
    {
      throw new RuntimeException("Can't load " + DRIVER_CLASSNAME);
    }
  }

  /** DataSource properties */
  protected String              url                  = null;
  protected String              user                 = null;
  protected String              password             = null;
  protected PrintWriter         logWriter            = null;

  /**
   * Default constructor.
   */
  public DataSource()
  {
  }

  //---------------------------------
  //--- DataSource methods
  //---------------------------------
  /**
   * Gets connection. Retrieves a new connection using the user name and
   * password that have been already set.
   * 
   * @throws SQLException if an error occurs.
   * @return a new connection.
   */
  public java.sql.Connection getConnection() throws SQLException
  {
    return getConnection(user, password);
  }

  /**
   * Gets connection. Retrieves a new connection using the user name and
   * password specified.
   * 
   * @param user user name.
   * @param password password.
   * @return a new connection.
   * @throws SQLException if an error occurs.
   */
  public java.sql.Connection getConnection(String user, String password)
      throws SQLException
  {
    if (user == null)
    {
      user = "";
    }
    if (password == null)
    {
      password = "";
    }
    Properties props = new Properties();
    props.put(USER_PROPERTY, user);
    props.put(PASSWORD_PROPERTY, password);

    return getConnection(props);
  }

  /**
   * Sets the log writer for this data source.
   * 
   * @param output print writer.
   * @throws SQLException in case of an error occurs.
   */
  public void setLogWriter(PrintWriter output) throws SQLException
  {
    logWriter = output;
  }

  /**
   * Gets the log writer.
   * 
   * @return log writer.
   */
  public java.io.PrintWriter getLogWriter()
  {
    return logWriter;
  }

  /**
   * Sets the timeout. Actually does nothing.
   * 
   * @param seconds timeout in seconds.
   * @throws SQLException in case of an error occurs.
   */
  public void setLoginTimeout(int seconds) throws SQLException
  {
  }

  /**
   * Gets the login timeout.
   * 
   * @return login timeout
   * @throws SQLException in case of an error occurs.
   */
  public int getLoginTimeout() throws SQLException
  {
    return 0;
  }

  //---------------------------------
  //--- Referenceable methods
  //---------------------------------
  /**
   * Gets a reference to this. The factory used for this class is the
   * {@link DataSourceFactory}class.
   * 
   * @return a reference to this.
   * @throws NamingException if <code>DataSourceFactory</code> not found.
   */
  public Reference getReference() throws NamingException
  {
    Reference ref = new Reference(getClass().getName(), FACTORY_CLASSNAME, null);
    ref.add(new StringRefAddr(DESCRIPTION_PROPERTY, getDescription()));
    ref.add(new StringRefAddr(USER_PROPERTY, getUser()));
    ref.add(new StringRefAddr(PASSWORD_PROPERTY, password));
    ref.add(new StringRefAddr(URL_PROPERTY, getUrl()));
    return ref;
  }

  //---------------------------------
  //--- Properties methods
  //---------------------------------

  /**
   * Return the description of this Datasource with the Driver version number.
   * 
   * @return Datasource description
   */
  public String getDescription()
  {
    return "C-JDBC " + driver.getMajorVersion() + "."
        + driver.getMinorVersion() + " Datasource";
  }

  /**
   * Sets url of the C-JDBC controller(s) to connect. The method is used by the
   * "application server" to set the URL (potentially according a deployment
   * descriptor). The url is stored in the {@link #URL_PROPERTY}property.
   * 
   * @param url URL to be used to connect C-JDBC controller(s)
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  /**
   * Sets URL of the C-JDBC controller(s) to connect. The method is used by the
   * "application server" to set the URL (potentially according a deployment
   * descriptor). The URL is stored in the "url" property.
   * 
   * @param url URL to be used to connect C-JDBC controller(s).
   */
  public void setURL(String url)
  {
    setUrl(url);
  }

  /**
   * Gets url of the C-JDBC controller(s) to connect. The URL is stored in the
   * {@link #URL_PROPERTY}property.
   * 
   * @return URL to be used to connect C-JDBC controller(s).
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Gets URL of the C-JDBC controller(s) to connect. The URL is stored in the
   * {@link #URL_PROPERTY}property.
   * 
   * @return URL to be used to connect C-JDBC controller(s).
   */
  public String getURL()
  {
    return getUrl();
  }

  /**
   * Sets user name to be used to connect the C-JDBC controller(s). The method
   * can be used by the "application server" to set the user name (potentially
   * according a deployment descriptor). The user name is stored in the
   * {@link #USER_PROPERTY}property.
   * 
   * @param userName user name to be used to connect C-JDBC controller(s).
   */
  public void setUser(String userName)
  {
    user = userName;
  }

  /**
   * Gets user name to be used to connect the C-JDBC controller(s). The user
   * name is stored in the {@link #USER_PROPERTY}property.
   * 
   * @return user name to be used to connect C-JDBC controller(s).
   */
  public String getUser()
  {
    return user;
  }

  /**
   * Sets password to be used to connect the C-JDBC controller(s). The method
   * can be used by the "application server" to set the password (potentially
   * according a deployment descriptor). The password is stored in the
   * {@link #PASSWORD_PROPERTY}property. Note that there is not a <code>getPassword</code>
   * method.
   * 
   * @param pwd password to be used to connect C-JDBC controller(s).
   */
  public void setPassword(String pwd)
  {
    password = pwd;
  }

  //---------------------------------
  //--- Protected methods
  //---------------------------------
  /**
   * Creates a connection using the specified properties.
   * 
   * @param props connection properties.
   * @throws SQLException if an error occurs.
   * @return a new connection.
   */
  protected java.sql.Connection getConnection(Properties props)
      throws SQLException
  {
    return driver.connect(url, props);
  }

}
