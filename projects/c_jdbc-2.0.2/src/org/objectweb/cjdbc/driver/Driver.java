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
 * Contributor(s): Julie Marguerite, Mathieu Peltier, Marek Prochazka, Sara
 * Bouchenak, Jaco Swart.
 */

package org.objectweb.cjdbc.driver;

import java.net.Socket;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.net.SocketFactory;

import org.objectweb.cjdbc.common.exceptions.AuthenticationException;
import org.objectweb.cjdbc.common.exceptions.NoMoreControllerException;
import org.objectweb.cjdbc.common.exceptions.driver.DriverSQLException;
import org.objectweb.cjdbc.common.net.SSLConfiguration;
import org.objectweb.cjdbc.common.net.SocketFactoryFactory;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.controller.core.ControllerConstants;
import org.objectweb.cjdbc.driver.protocol.Commands;

/**
 * C-JDBC Driver for client side. This driver is a generic driver that is
 * designed to replace any specific JDBC driver that could be used by a client.
 * The client only has to know the node where the C-JDBC controller is running
 * and the database he wants to access (the RDBMS could be PostgreSQL, Oracle,
 * DB2, Sybase, MySQL or whatever, we only need the name of the database and the
 * C-JDBC controller will be responsible for finding the RDBMs hosting this
 * database).
 * <p>
 * The C-JDBC driver can be loaded from the client with:
 * <code>Class.forName("org.objectweb.cjdbc.driver.Driver");</code>
 * <p>
 * The URL expected for the use with C-JDBC is:
 * <code>jdbc:cjdbc://host1:port1,host2:port2/database</code>.
 * <p>
 * At least one host must be specified. If several hosts are given, one is
 * picked up randomly from the list. If the currently selected controller fails,
 * another one is automatically picked up from the list.
 * <p>
 * Default port number is 25322 if omitted.
 * <p>
 * Those 2 examples are equivalent:
 * 
 * <pre>
 * DriverManager.getConnection(&quot;jdbc:cjdbc://localhost:/tpcw&quot;);
 * DriverManager.getConnection(&quot;jdbc:cjdbc://localhost:25322/tpcw&quot;);
 * </pre>
 * 
 * <p>
 * Examples using 2 controllers for fault tolerance:
 * 
 * <pre>
 * DriverManager
 *     .getConnection(&quot;jdbc:cjdbc://cluster1.objectweb.org:25322,cluster2.objectweb.org:25322/tpcw&quot;);
 * DriverManager
 *     .getConnection(&quot;jdbc:cjdbc://localhost:25322,remote.objectweb.org:25322/tpcw&quot;);
 * DriverManager
 *     .getConnection(&quot;jdbc:cjdbc://smpnode.com:25322,smpnode.com:1098/tpcw&quot;);
 * </pre>
 * 
 * <p>
 * The driver accepts a number of options that starts after a ? sign and are
 * separated by an & sign. Each option is a name=value pair. Example:
 * jdbc:cjdbc://host/db?option1=value1;option2=value2.
 * <p>
 * Currently supported options are:
 * 
 * <pre>
 * user: user login
 * password: user password
 * booleanTrue: value for the 'true' value when using PreparedStatement.setBoolean method
 * booleanFalse: value for the 'false' value when using PreparedStatement.setBoolean method
 * escapeBackslash: set this to true to escape backslashes when performing escape processing of PreparedStatements
 * escapeSingleQuote: set this to true to escape single quotes (') when performing escape processing of PreparedStatements
 * escapeCharacter: use this character to prepend and append to the values when performing escape processing of PreparedStatements
 * driverProcessed: set this to false to let queries be passed and prepared for each individual backend
 * connectionPooling: set this to false if you do not want the driver to perform transparent connection pooling
 * preferredController: defines the strategy to use to choose a preferred controller to connect to
 *  - jdbc:cjdbc://node1,node2,node3/myDB?preferredController=ordered 
 *      Always connect to node1, and if not available then try to node2 and
 *      finally if none are available try node3.
 *  - jdbc:cjdbc://node1,node2,node3/myDB?preferredController=random
 *      Pickup a controller node randomly (default strategy)
 *  - jdbc:cjdbc://node1,node2:25343,node3/myDB?preferredController=node2:25343,node3 
 *      Round-robin between node2 and node3, fallback to node1 if none of node2
 *      and node3 is available.
 *  - jdbc:cjdbc://node1,node2,node3/myDB?preferredController=roundRobin
 *      Round robin starting with first node in URL.
 * retryIntervalInMs: once a controller has died, the driver will try to 
 *   reconnect to this controller every retryIntervalInMs to see if the backend
 *   is back online. The default is 5000 (5 seconds).
 * </pre>
 * 
 * <p>
 * This original code has been inspired from the PostgreSQL JDBC driver by Peter
 * T. Mount <peter@retep.org.uk>and the MM MySQL JDBC Drivers from Mark Matthews
 * <mmatthew@worldserver.com>.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Marek.Prochazka@inrialpes.fr">Marek Prochazka </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:jaco.swart@iblocks.co.uk">Jaco Swart </a>
 * @version 1.0
 */

public class Driver implements java.sql.Driver
{
  /** Driver major version. */
  public static final int        MAJOR_VERSION                             = Constants
                                                                               .getMajorVersion();

  /** Driver minor version. */
  public static final int        MINOR_VERSION                             = Constants
                                                                               .getMinorVersion();
  /** Get the cjdbc.ssl.enabled system property to check if SSL is enabled */
  protected static final boolean SSL_ENABLED_PROPERTY                      = "true"
                                                                               .equalsIgnoreCase(System
                                                                                   .getProperty("cjdbc.ssl.enabled"));

  /**
   * Default interval in milliseconds before retrying to re-connect to a
   * controller that has failed (default is 5 seconds or 5000 milliseconds)
   */
  public static final long       DEFAULT_RETRY_INTERVAL_IN_MS              = 5000;

  /**
   * List of driver properties initialized in the static class initializer
   * <p>
   * !!! Static intializer needs to be udpated when new properties are added !!!
   */
  protected static ArrayList     driverProperties;

  /** C-JDBC driver property name (if you add one, read driverProperties above). */
  protected static final String  HOST_PROPERTY                             = "HOST";
  protected static final String  PORT_PROPERTY                             = "PORT";
  protected static final String  DATABASE_PROPERTY                         = "DATABASE";
  protected static final String  USER_PROPERTY                             = "user";
  protected static final String  PASSWORD_PROPERTY                         = "password";
  protected static final String  BOOLEAN_TRUE_PROPERTY                     = "booleanTrue";
  protected static final String  BOOLEAN_FALSE_PROPERTY                    = "booleanFalse";
  protected static final String  ESCAPE_BACKSLASH_PROPERTY                 = "escapeBackslash";
  protected static final String  ESCAPE_SINGLE_QUOTE_PROPERTY              = "escapeSingleQuote";
  protected static final String  ESCAPE_CHARACTER_PROPERTY                 = "escapeCharacter";
  protected static final String  DRIVER_PROCESSED_PROPERTY                 = "driverProcessed";
  protected static final String  CONNECTION_POOLING_PROPERTY               = "connectionPooling";
  protected static final String  PREFERRED_CONTROLLER_PROPERTY             = "preferredController";
  protected static final String  RETRY_INTERVAL_IN_MS_PROPERTY             = "retryIntervalInMs";
  protected static final String  DEBUG_PROPERTY                            = "debugLevel";

  /** C-JDBC driver property description. */
  private static final String    HOST_PROPERTY_DESCRIPTION                 = "Hostname of C-JDBC controller";
  private static final String    PORT_PROPERTY_DESCRIPTION                 = "Port number of C-JDBC controller";
  private static final String    DATABASE_PROPERTY_DESCRIPTION             = "Database name";
  private static final String    USER_PROPERTY_DESCRIPTION                 = "Username to authenticate as";
  private static final String    PASSWORD_PROPERTY_DESCRIPTION             = "Password to use for authentication";
  private static final String    BOOLEAN_TRUE_PROPERTY_DESCRIPTION         = "Use this value for the 'true' value when using PreparedStatement.setBoolean method";
  private static final String    BOOLEAN_FALSE_PROPERTY_DESCRIPTION        = "Use this value for the 'false' value when using PreparedStatement.setBoolean method";
  private static final String    ESCAPE_BACKSLASH_PROPERTY_DESCRIPTION     = "Set this to true to escape backslashes when performing escape processing of PreparedStatements";
  private static final String    ESCAPE_SINGLE_QUOTE_PROPERTY_DESCRIPTION  = "Set this to true to escape single quotes (') when performing escape processing of PreparedStatements";
  private static final String    ESCAPE_CHARACTER_PROPERTY_DESCRIPTION     = "Use this character to prepend and append to the values when performing escape processing of PreparedStatements";
  private static final String    DRIVER_PROCESSED_PROPERTY_DESCRIPTION     = "Set this to false to let queries be passed and prepared for each individual backend";
  protected static final String  CONNECTION_POOLING_PROPERTY_DESCRIPTION   = "Set this to false if you do not want the driver to perform transparent connection pooling";
  protected static final String  PREFERRED_CONTROLLER_PROPERTY_DESCRIPTION = "Defines the strategy to use to choose a preferred controller to connect to";
  protected static final String  RETRY_INTERVAL_IN_MS_PROPERTY_DESCRIPTION = "Interval in milliseconds before retrying to re-connect to a controller that has failed";
  protected static final String  DEBUG_PROPERTY_DESCRIPTION                = "Debug level that can be set to 'debug', 'info' or 'off'";

  /** C-JDBC URL header. */
  public static final String     CJDBC_URL_HEADER                          = "jdbc:cjdbc://";

  /** C-JDBC URL header length. */
  public static final int        CJDBC_URL_HEADER_LENGTH                   = CJDBC_URL_HEADER
                                                                               .length();

  /**
   * Cache of parsed URLs used to connect to the controller. It always grows and
   * is never purged: we don't yet handle the unlikely case of a long-lived
   * driver using zillions of different URLs.
   * <p>
   * Hashmap is URL=> <code>CjdbcUrl</code>
   */
  private HashMap                parsedUrlsCache                           = new HashMap();

  /** List of connections that are ready to be closed. */
  protected ArrayList            pendingConnectionClosing                  = new ArrayList();
  protected boolean              connectionClosingThreadisAlive            = false;

  // The static initializer registers ourselves with the DriverManager
  // and try to bind the C-JDBC Controller
  static
  {
    // Register with the DriverManager (see JDBC API Tutorial and Reference,
    // Second Edition p. 941)
    try
    {
      java.sql.DriverManager.registerDriver(new Driver());
    }
    catch (SQLException e)
    {
      throw new RuntimeException("Unable to register C-JDBC driver");
    }

    // Build the static list of driver properties
    driverProperties = new ArrayList();
    driverProperties.add(Driver.HOST_PROPERTY);
    driverProperties.add(Driver.PORT_PROPERTY);
    driverProperties.add(Driver.DATABASE_PROPERTY);
    driverProperties.add(Driver.USER_PROPERTY);
    driverProperties.add(Driver.PASSWORD_PROPERTY);
    driverProperties.add(Driver.BOOLEAN_TRUE_PROPERTY);
    driverProperties.add(Driver.BOOLEAN_FALSE_PROPERTY);
    driverProperties.add(Driver.ESCAPE_BACKSLASH_PROPERTY);
    driverProperties.add(Driver.ESCAPE_SINGLE_QUOTE_PROPERTY);
    driverProperties.add(Driver.ESCAPE_CHARACTER_PROPERTY);
    driverProperties.add(Driver.DRIVER_PROCESSED_PROPERTY);
    driverProperties.add(Driver.CONNECTION_POOLING_PROPERTY);
    driverProperties.add(Driver.PREFERRED_CONTROLLER_PROPERTY);
    driverProperties.add(Driver.RETRY_INTERVAL_IN_MS_PROPERTY);
    driverProperties.add(Driver.DEBUG_PROPERTY);
  }

  /**
   * Creates a new <code>Driver</code> and register it with
   * <code>DriverManager</code>.
   */
  public Driver()
  {
    // Required for Class.forName().newInstance()
  }

  /**
   * Asks the C-JDBC controller if the requested database can be accessed with
   * the provided user name and password. If the C-JDBC controller can't access
   * the requested database, an <code>SQLException</code> is thrown, else a
   * "fake" <code>Connection</code> is returned to the user so that he or she
   * can create <code>Statements</code>.
   * 
   * @param url the URL of the C-JDBC controller to which to connect.
   * @param properties a list of arbitrary string tag/value pairs as connection
   *          arguments (usually at least a "user" and "password").
   * @return a <code>Connection</code> object that represents a connection to
   *         the database through the C-JDBC Controller.
   * @exception SQLException if an error occurs.
   */
  public java.sql.Connection connect(String url, Properties properties)
      throws SQLException
  {
    if (url == null)
      throw new SQLException("Invalid null URL in connect");

    /**
     * We cannot raise a SQLException as the driver manager tries to connect to
     * all registered drivers. So if the CJDBC_URL_HEADER is not found we should
     * probably pass... and return null
     */
    if (!url.startsWith(CJDBC_URL_HEADER))
      return null;

    // In the common case, we do not synchronize
    CjdbcUrl cjdbcUrl = (CjdbcUrl) parsedUrlsCache.get(url);
    if (cjdbcUrl == null) // Not in the cache
    {
      synchronized (this)
      {
        // Recheck here in case someone updated before we entered the
        // synchronized block
        cjdbcUrl = (CjdbcUrl) parsedUrlsCache.get(url);
        if (cjdbcUrl == null)
        {
          cjdbcUrl = new CjdbcUrl(url);
          parsedUrlsCache.put(url, cjdbcUrl);
        }
      }
    }

    ControllerInfo controller = null;
    try
    {
      // Choose a controller according to the policy
      controller = cjdbcUrl.getControllerConnectPolicy().getController();
      return connectToController(properties, cjdbcUrl, controller);
    }
    catch (AuthenticationException e)
    {
      throw new SQLException(e.getMessage());
    }
    catch (NoMoreControllerException e)
    {
      throw new SQLException("No controller is available to accept connections");
    }
    catch (SQLException e)
    {
      // Add controller to suspect list
      if (controller != null)
      {
        cjdbcUrl.getControllerConnectPolicy().suspectControllerOfFailure(
            controller);
        // Retry
        System.out.println("retrying");
        return connect(url, properties);
      }
      else
        throw e;
    }
    catch (RuntimeException e)
    {
      e.printStackTrace();
      throw new SQLException(
          "Unable to connect to the virtual database - Unexpected runtime error ("
              + e + ")");
    }
  }

  /**
   * Connect to the given controller with the specified parameters.
   * 
   * @param properties properties given in connect call
   * @param cjdbcUrl C-JDBC URL object including parameters
   * @param controller the controller to connect to
   * @return the connection to the controller 
   * @throws AuthenticationException if the authentication has failed or the
   *           database name is wrong
   * @throws DriverSQLException if the connection cannot be established with the
   *           controller
   */
  protected java.sql.Connection connectToController(Properties properties,
      CjdbcUrl cjdbcUrl, ControllerInfo controller) throws
      AuthenticationException, DriverSQLException
  {
    // Check user name
    String user = null;
    if (properties != null)
      user = properties.getProperty(USER_PROPERTY);
    if (user == null)
      user = (String) cjdbcUrl.getParameters().get(USER_PROPERTY);
    if (user == null || user.equals(""))
      throw new AuthenticationException("Invalid user name in connect");

    // Check the password
    String password = null;
    if (properties != null)
      password = properties.getProperty(PASSWORD_PROPERTY);
    if (password == null)
      password = (String) cjdbcUrl.getParameters().get(PASSWORD_PROPERTY);
    if (password == null)
      password = "";

    // Try to retrieve a reusable connection
    if (!"false".equals(cjdbcUrl.getParameters().get(
        CONNECTION_POOLING_PROPERTY)))
    { // Connection pooling is activated
      java.sql.Connection c = retrievePendingClosingConnection(properties,
          cjdbcUrl.getUrl(), controller, user, password);
      if (c != null)
      {
        if (cjdbcUrl.isDebugEnabled())
          System.out.println("Reusing connection from pool");
        return c; // Re-use this one
      }
    }

    // Let's go for a new connection

    // This is actually a connection constructor,
    // we should try to move most of it below.
    boolean sentVdbName = false;
    boolean sentUserInfo = false;
    try
    {
      // Connect to the controller
      Socket socket = null;

      // SSL enabled ?
      if (SSL_ENABLED_PROPERTY)
      {
        SocketFactory sslFact = SocketFactoryFactory
            .createFactory(SSLConfiguration.getDefaultConfig());
        socket = sslFact.createSocket(controller.getHostname(), controller
            .getPort());
      }
      else
      {
        // no ssl - we use ordinary socket
        socket = new Socket(controller.getHostname(), controller.getPort());
      }

      // Disable Nagle algorithm else small messages are not sent
      // (at least under Linux) even if we flush the output stream.
      socket.setTcpNoDelay(true);

      if (cjdbcUrl.isInfoEnabled())
        System.out.println("Authenticating with controller " + controller);

      CJDBCOutputStream out = new CJDBCOutputStream(socket);
      // Send protocol version and database name
      out.writeInt(Commands.ProtocolVersion);
      out.writeUTF(cjdbcUrl.getDatabaseName());
      out.flush();
      sentVdbName = true;

      // Send user information
      out.writeUTF(user);
      out.writeUTF(password);
      out.flush();
      sentUserInfo = true;

      CJDBCInputStream in;
      Connection con;

      // Create input stream only here else it will block
      in = new CJDBCInputStream(socket);

      con = new Connection(this, socket, in, out, cjdbcUrl, controller, user,
          password);

      return setParametersOnConnection(properties, con);

    } // try connect to the controller/connection constructor
    catch (Exception re)
    {
      if (!sentVdbName)
        throw new DriverSQLException("Unable to connect to controller on "
            + controller.getHostname() + ":" + controller.getPort() + " (" + re
            + ")");
      else if (re instanceof AuthenticationException)
        throw (AuthenticationException) re;
      else if (!sentUserInfo)
        throw new AuthenticationException(
            "Unable to connect to the virtual database (virtual database name is probably not correct)");
      else
        throw new DriverSQLException(
            "Unable to connect to the virtual database ("
                + re.getLocalizedMessage() + ")", re);
    }
  }

  /**
   * This method is used to implement the transparent connection pooling and try
   * to retrieve a connection that was recently closed to the given controller
   * with the provided login/password information.
   * 
   * @param connectionInfo the connectino information
   * @param url
   * @return a connection that could be reuse or null if none
   */
  private java.sql.Connection retrievePendingClosingConnection(
      Properties connectionInfo, String url, ControllerInfo controllerInfo,
      String user, String password)
  {
    // Check if there is a connection that is about to be closed that could
    // be reused. We take the bet that if a connection has been released by
    // a client, in the general case, it will reuse the same connection.
    // As we need to keep the work in the synchronized block as minimal as
    // possible, we have to extract the string comparison
    // (url,name,password,controller)from the sync block. This way, we cannot
    // just read/compare/take the connection without synchronizing the whole
    // thing. A solution is to systematically extract the first available
    // connection in the sync block, and do the checkings outside the block. If
    // we fail, we re-sync to put the connection back but in practice it is
    // almost always a success and we don't really care to pay this extra cost
    // once in a while.
    try
    {
      Connection c;
      synchronized (pendingConnectionClosing)
      {
        // Take the last one to prevent shifting all elements
        c = (Connection) pendingConnectionClosing
            .remove(pendingConnectionClosing.size() - 1);
      }
      if (url.equals(c.getUrl())
          && controllerInfo.equals(c.getControllerInfo())
          && user.equals(c.getUserName()) && password.equals(c.getPassword()))
      { // Great! Take this one.
        c.isClosed = false;
        return setParametersOnConnection(connectionInfo, c);
      }
      else
        // Put this connection back, it is not good for us
        synchronized (pendingConnectionClosing)
        {
          pendingConnectionClosing.add(c);
        }
    }
    catch (IndexOutOfBoundsException ignore)
    {
      // No connection available
    }
    return null;
  }

  /**
   * Tests if the URL is understood by the driver. Calls the
   * <code>parseURL()</code> method.
   * 
   * @param url the JDBC URL.
   * @return <code>true</code> if the URL is correct, otherwise an exception
   *         with extensive error message is thrown.
   * @exception SQLException if the URL is incorrect an explicit error message
   *              is given.
   */
  public synchronized boolean acceptsURL(String url) throws SQLException
  {
    try
    {
      CjdbcUrl cjdbcUrl = (CjdbcUrl) parsedUrlsCache.get(url);
      if (cjdbcUrl == null) // Not in the cache
      {
        synchronized (this)
        {
          // Recheck here in case someone updated before we entered the
          // synchronized block
          cjdbcUrl = (CjdbcUrl) parsedUrlsCache.get(url);
          if (cjdbcUrl == null)
          {
            cjdbcUrl = new CjdbcUrl(url); // URL parsed here
            // Update the cache anyway that can be useful later on
            parsedUrlsCache.put(url, cjdbcUrl);
          }
        }
      }
      return true;
    }
    catch (SQLException e)
    {
      return false;
    }
  }

  /**
   * Set the different parameters on the connection. Possible values are:
   * <code>BOOLEAN_TRUE_PROPERTY</code><br>
   * <code>BOOLEAN_FALSE_PROPERTY</code><br>
   * <code>ESCAPE_BACKSLASH_PROPERTY</code><br>
   * <code>ESCAPE_SINGLE_QUOTE_PROPERTY</code><br>
   * <code>ESCAPE_CHARACTER_PROPERTY</code><br>
   * <code>DRIVER_PROCESSED_PROPERTY</code><br>
   * <code>CONNECTION_POOLING_PROPERTY</code><br>
   * 
   * @param props the properties used to connect to the controller. These
   *          properties should be collected from both the url and the
   *          <code>Properties</code> object passed in to the connect method
   * @param connection the connection to set the parameters on. Previous
   *          parameters will be overriden
   * @return the same connection with the parameters set
   */
  private java.sql.Connection setParametersOnConnection(Properties props,
      org.objectweb.cjdbc.driver.Connection connection)
  {
    if ((props == null) || props.isEmpty())
      return connection;

    String booleanTrue = props.getProperty(BOOLEAN_TRUE_PROPERTY);
    if (booleanTrue != null)
      connection.setPreparedStatementBooleanTrue(booleanTrue);
    String booleanFalse = props.getProperty(BOOLEAN_FALSE_PROPERTY);
    if (booleanFalse != null)
      connection.setPreparedStatementBooleanFalse(booleanFalse);
    String escapeBaskslash = props.getProperty(ESCAPE_BACKSLASH_PROPERTY);
    if (escapeBaskslash != null)
      connection
          .setEscapeBackslash(new Boolean(escapeBaskslash).booleanValue());
    String escapeQuote = props.getProperty(ESCAPE_SINGLE_QUOTE_PROPERTY);
    if (escapeQuote != null)
      connection.setEscapeSingleQuote(new Boolean(escapeQuote).booleanValue());

    String escapeChar = props.getProperty(ESCAPE_CHARACTER_PROPERTY);
    if (escapeChar != null)
      connection.setEscapeChar(escapeChar);

    String driverProcessed = props.getProperty(DRIVER_PROCESSED_PROPERTY);
    if (driverProcessed != null)
      connection.setDriverProcessed(Boolean.valueOf(driverProcessed)
          .booleanValue());

    String connectionPooling = props.getProperty(CONNECTION_POOLING_PROPERTY);
    if (connection != null)
      connection.setConnectionPooling(Boolean.valueOf(connectionPooling)
          .booleanValue());

    return connection;
  }

  /**
   * Change the database name in the provided URL.
   * 
   * @param url URL to parse
   * @param newDbName new database name to insert
   * @return the updated URL
   * @throws SQLException if an error occurs while parsing the url
   */
  public String changeDatabaseName(String url, String newDbName)
      throws SQLException
  {
    StringBuffer sb = new StringBuffer();
    sb.append(CJDBC_URL_HEADER);
    CjdbcUrl cjdbcUrl = (CjdbcUrl) parsedUrlsCache.get(url);
    if (cjdbcUrl == null)
    {
      acceptsURL(url); // parse and put in cache
      cjdbcUrl = (CjdbcUrl) parsedUrlsCache.get(url);
    }
    ControllerInfo[] controllerList = cjdbcUrl.getControllerList();
    for (int i = 0; i < controllerList.length; i++)
    {
      if (i == 0)
        sb.append(controllerList[i].toString());
      else
        sb.append("," + controllerList[i].toString());
    }
    sb.append("/" + newDbName);
    HashMap params = cjdbcUrl.getParameters();
    if (params != null)
    {
      Iterator paramsKeys = params.keySet().iterator();
      String element = null;
      while (paramsKeys.hasNext())
      {
        if (element == null)
          sb.append("?");
        else
          sb.append("&");
        element = (String) paramsKeys.next();
        sb.append(element + "=" + params.get(paramsKeys));
      }
    }
    return sb.toString();
  }

  /**
   * This method is intended to allow a generic GUI tool to discover what
   * properties it should prompt a human for in order to get enough information
   * to connect to a database.
   * <p>
   * The only properties supported by C-JDBC are:
   * <ul>
   * <li>HOST_PROPERTY</li>
   * <li>PORT_PROPERTY</li>
   * <li>DATABASE_PROPERTY</li>
   * <li>USER_PROPERTY</li>
   * <li>PASSWORD_PROPERTY</li>
   * <li>ESCAPE_BACKSLASH_PROPERTY</li>
   * <li>ESCAPE_CHARACTER_PROPERTY</li>
   * <li>ESCAPE_SINGLE_QUOTE</li>
   * <li>BOOLEAN_FALSE_PROPERTY</li>
   * <li>BOOLEAN_TRUE_PROPERTY</li>
   * <li>DRIVER_PROCESSED_PROPERTY</li>
   * <li>CONNECTION_POOLING_PROPERTY</li>
   * <li>PREFERRED_CONTROLLER_PROPERTY
   * <li>
   * </ul>
   * 
   * @param url the URL of the database to connect to
   * @param info a proposed list of tag/value pairs that will be sent on connect
   *          open.
   * @return an array of <code>DriverPropertyInfo</code> objects describing
   *         possible properties. This array may be an empty array if no
   *         properties are required (note that this override any setting that
   *         might be set in the URL).
   * @exception SQLException if the url is not valid
   * @see java.sql.Driver#getPropertyInfo
   */
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
      throws SQLException
  {
    if (!acceptsURL(url))
      throw new SQLException("Invalid url " + url);

    CjdbcUrl cjdbcUrl;
    synchronized (this)
    {
      cjdbcUrl = (CjdbcUrl) parsedUrlsCache.get(url);
      if (cjdbcUrl == null)
        throw new SQLException("Error while retrieving URL information");
    }
    HashMap params = cjdbcUrl.getParameters();

    String host = info.getProperty(HOST_PROPERTY);
    if (host == null)
    {
      ControllerInfo[] controllerList = cjdbcUrl.getControllerList();
      for (int i = 0; i < controllerList.length; i++)
      {
        ControllerInfo controller = controllerList[i];
        if (i == 0)
          host = controller.toString();
        else
          host += "," + controller.toString();
      }
    }
    DriverPropertyInfo hostProp = new DriverPropertyInfo(HOST_PROPERTY, host);
    hostProp.required = true;
    hostProp.description = HOST_PROPERTY_DESCRIPTION;

    DriverPropertyInfo portProp = new DriverPropertyInfo(PORT_PROPERTY, info
        .getProperty(PORT_PROPERTY, Integer
            .toString(ControllerConstants.DEFAULT_PORT)));
    portProp.required = false;
    portProp.description = PORT_PROPERTY_DESCRIPTION;

    String database = info.getProperty(DATABASE_PROPERTY);
    if (database == null)
      database = cjdbcUrl.getDatabaseName();
    DriverPropertyInfo databaseProp = new DriverPropertyInfo(DATABASE_PROPERTY,
        database);
    databaseProp.required = true;
    databaseProp.description = DATABASE_PROPERTY_DESCRIPTION;

    String user = info.getProperty(USER_PROPERTY);
    if (user == null)
      user = (String) params.get(USER_PROPERTY);
    DriverPropertyInfo userProp = new DriverPropertyInfo(USER_PROPERTY, user);
    userProp.required = true;
    userProp.description = USER_PROPERTY_DESCRIPTION;

    String password = info.getProperty(PASSWORD_PROPERTY);
    if (password == null)
      password = (String) params.get(PASSWORD_PROPERTY);
    DriverPropertyInfo passwordProp = new DriverPropertyInfo(PASSWORD_PROPERTY,
        password);
    passwordProp.required = true;
    passwordProp.description = PASSWORD_PROPERTY_DESCRIPTION;

    String escapeChar = info.getProperty(ESCAPE_CHARACTER_PROPERTY);
    if (escapeChar == null)
      escapeChar = (String) params.get(ESCAPE_CHARACTER_PROPERTY);
    DriverPropertyInfo escapeCharProp = new DriverPropertyInfo(
        ESCAPE_CHARACTER_PROPERTY, escapeChar);
    escapeCharProp.required = false;
    escapeCharProp.description = ESCAPE_CHARACTER_PROPERTY_DESCRIPTION;

    String escapeBackslash = info.getProperty(ESCAPE_BACKSLASH_PROPERTY);
    if (escapeBackslash == null)
      escapeBackslash = (String) params.get(ESCAPE_BACKSLASH_PROPERTY);
    DriverPropertyInfo escapeBackProp = new DriverPropertyInfo(
        ESCAPE_BACKSLASH_PROPERTY, escapeBackslash);
    escapeBackProp.required = false;
    escapeBackProp.description = ESCAPE_BACKSLASH_PROPERTY_DESCRIPTION;

    String escapeSingleQuote = info.getProperty(ESCAPE_SINGLE_QUOTE_PROPERTY);
    if (escapeSingleQuote == null)
      escapeSingleQuote = (String) params.get(ESCAPE_SINGLE_QUOTE_PROPERTY);
    DriverPropertyInfo escapeSingleProp = new DriverPropertyInfo(
        ESCAPE_SINGLE_QUOTE_PROPERTY, escapeSingleQuote);
    escapeSingleProp.required = false;
    escapeSingleProp.description = ESCAPE_SINGLE_QUOTE_PROPERTY_DESCRIPTION;

    String booleanFalse = info.getProperty(BOOLEAN_FALSE_PROPERTY);
    if (booleanFalse == null)
      booleanFalse = (String) params.get(BOOLEAN_FALSE_PROPERTY);
    DriverPropertyInfo booleanFalseProp = new DriverPropertyInfo(
        BOOLEAN_FALSE_PROPERTY, booleanFalse);
    booleanFalseProp.required = false;
    booleanFalseProp.description = BOOLEAN_FALSE_PROPERTY_DESCRIPTION;

    String booleanTrue = info.getProperty(BOOLEAN_TRUE_PROPERTY);
    if (booleanTrue == null)
      booleanTrue = (String) params.get(BOOLEAN_TRUE_PROPERTY);
    DriverPropertyInfo booleanTrueProp = new DriverPropertyInfo(
        BOOLEAN_TRUE_PROPERTY, booleanTrue);
    booleanTrueProp.required = false;
    booleanTrueProp.description = BOOLEAN_TRUE_PROPERTY_DESCRIPTION;

    String driverProcessed = info.getProperty(DRIVER_PROCESSED_PROPERTY);
    if (driverProcessed == null)
      driverProcessed = (String) params.get(DRIVER_PROCESSED_PROPERTY);
    DriverPropertyInfo parseQueryProp = new DriverPropertyInfo(
        DRIVER_PROCESSED_PROPERTY, driverProcessed);
    escapeSingleProp.required = false;
    escapeSingleProp.description = DRIVER_PROCESSED_PROPERTY_DESCRIPTION;

    String connectionPooling = info.getProperty(CONNECTION_POOLING_PROPERTY);
    if (connectionPooling == null)
      connectionPooling = (String) params.get(CONNECTION_POOLING_PROPERTY);
    DriverPropertyInfo connectionPoolingProp = new DriverPropertyInfo(
        CONNECTION_POOLING_PROPERTY, connectionPooling);
    connectionPoolingProp.required = false;
    connectionPoolingProp.description = CONNECTION_POOLING_PROPERTY_DESCRIPTION;

    String preferredController = info
        .getProperty(PREFERRED_CONTROLLER_PROPERTY);
    if (preferredController == null)
      preferredController = (String) params.get(PREFERRED_CONTROLLER_PROPERTY);
    DriverPropertyInfo preferredControllerProp = new DriverPropertyInfo(
        PREFERRED_CONTROLLER_PROPERTY, preferredController);
    preferredControllerProp.required = false;
    preferredControllerProp.description = PREFERRED_CONTROLLER_PROPERTY_DESCRIPTION;

    return new DriverPropertyInfo[]{hostProp, portProp, databaseProp, userProp,
        passwordProp, escapeCharProp, escapeBackProp, escapeSingleProp,
        booleanFalseProp, booleanTrueProp, parseQueryProp,
        connectionPoolingProp, preferredControllerProp};
  }

  /**
   * Gets the river's major version number
   * 
   * @return the driver's major version number
   */
  public int getMajorVersion()
  {
    return MAJOR_VERSION;
  }

  /**
   * Gets the driver's minor version number
   * 
   * @return the driver's minor version number
   */
  public int getMinorVersion()
  {
    return MINOR_VERSION;
  }

  /**
   * Reports whether the driver is a genuine JDBC compliant driver. A driver may
   * only report <code>true</code> here if it passes the JDBC compliance
   * tests, otherwise it is required to return <code>false</code>. JDBC
   * compliance requires full support for the JDBC API and full support for SQL
   * 92 Entry Level. We cannot ensure that the underlying JDBC drivers will be
   * JDBC compliant, so it is safer to return <code>false</code>.
   * 
   * @return always <code>false</code>
   */
  public boolean jdbcCompliant()
  {
    return false;
  }

}
