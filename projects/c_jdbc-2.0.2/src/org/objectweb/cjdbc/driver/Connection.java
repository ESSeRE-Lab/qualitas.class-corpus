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
 * Contributor(s): Julie Marguerite, Guillaume Bort, Duncan Smith, Vadim Kassin,
 * Nicolas Modrzyk, Jaco Swart,  Jean-Bernard van Zuylen
 * Completely refactored by Marc Herbert to remove the use of Java serialization.
 */

package org.objectweb.cjdbc.driver;

import java.io.IOException;
import java.net.Socket;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.objectweb.cjdbc.common.exceptions.AuthenticationException;
import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.NoMoreControllerException;
import org.objectweb.cjdbc.common.exceptions.NotImplementedException;
import org.objectweb.cjdbc.common.exceptions.ProtocolException;
import org.objectweb.cjdbc.common.exceptions.driver.DriverSQLException;
import org.objectweb.cjdbc.common.exceptions.driver.protocol.BackendDriverException;
import org.objectweb.cjdbc.common.exceptions.driver.protocol.ControllerCoreException;
import org.objectweb.cjdbc.common.exceptions.driver.protocol.SerializableException;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.driver.connectpolicy.AbstractControllerConnectPolicy;
import org.objectweb.cjdbc.driver.protocol.Commands;
import org.objectweb.cjdbc.driver.protocol.SQLDataSerialization;
import org.objectweb.cjdbc.driver.protocol.TypeTag;

/**
 * This class implements the communication protocol to the Controller.
 * <p>
 * Connection.java was inspired from the PostgreSQL JDBC driver by Peter T.
 * Mount.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:vadim@kase.kz">Vadim Kassin </a>
 * @author <a href="mailto:duncan@mightybot.com">Duncan Smith </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:jaco.swart@iblocks.co.uk">Jaco Swart </a>
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 2.0
 */
public class Connection implements java.sql.Connection
{
  /** Does the controller wants SQL templates of PreparedStatements? */
  protected boolean           controllerNeedsSqlSkeleton          = false;

  /** Status of the connection. */
  protected boolean           isClosed                            = false;

  protected String            escapeChar                          = "\'";

  /** C-JDBC controller we are connected to */
  protected ControllerInfo    controllerInfo                      = null;

  // ConnectionClosingThread
  /** Driver that created us. */
  protected Driver            driver                              = null;

  /** Connection with the controller. */
  protected Socket            socket;
  /** Socket input stream. */
  protected CJDBCInputStream  socketInput;
  /** Socket output stream. */
  protected CJDBCOutputStream socketOutput;

  // used by Statement (and maybe also by some others _below_)
  static final String         LINE_SEPARATOR                      = System
                                                                      .getProperty("line.separator");

  // Member variables describing the state of the connection

  /** Commit mode of the connection (<code>true</code>= automatic). */
  protected boolean           autoCommit                          = true;

  /** Is the connection in read-only mode ? */
  protected boolean           readOnly                            = false;

  /** Has a write request been executed in the current transaction? */
  boolean                     writeExecutedInTransaction          = false;

  /** Default transaction isolation level if the user has not enforced one */
  public static final int     DEFAULT_TRANSACTION_ISOLATION_LEVEL = -1;
  /** Current transaction isolation level. */
  protected int               isolationLevel                      = DEFAULT_TRANSACTION_ISOLATION_LEVEL;

  /** transaction identifier. */
  protected long              transactionId                       = 0;

  /** List of <code>Warnings</code> for this connection. */
  protected SQLWarning        firstWarning                        = null;

  /** Meta-data of C-JDBC connections. */
  protected DatabaseMetaData  metaData                            = null;

  /** Parsed URL to the database. */
  private final CjdbcUrl      cjdbcUrl;

  /** Virtual database user used for this connection. */
  protected String            vdbUser                             = null;
  protected String            vdbPassword                         = null;

  private AbstractBlobFilter  blobFilter;
  private boolean             connectionPooling                   = true;

  // Escape processing tuning
  protected boolean           escapeBackslash                     = true;
  protected boolean           escapeSingleQuote                   = true;

  // Parsing Query values
  protected boolean           driverProcessed                     = true;

  // PreparedStatement.setBoolean values
  protected String            preparedStatementBooleanTrue        = "'1'";
  protected String            preparedStatementBooleanFalse       = "'0'";

  private boolean             closeSocketOnGC                     = true;

  /*****************************************************************************
   * *************** * Constructor and get/set methods * ***********************
   * ****************************************************************************
   */

  /**
   * Creates a new <code>Connection</code> instance.
   * 
   * @param driver calling driver
   * @param socket connection with the controller
   * @param in socket input stream
   * @param out socket output stream
   * @param cjdbcUrl C-JDBC URL of the database
   * @param controller controller we are connected to
   * @param userName user login
   * @param password user password
   * @throws AuthenticationException login error
   * @throws IOException stream error
   * @throws ProtocolException unexpected answer
   */

  public Connection(Driver driver, Socket socket, CJDBCInputStream in,
      CJDBCOutputStream out, CjdbcUrl cjdbcUrl, ControllerInfo controller,
      String userName, String password) throws AuthenticationException,
      IOException, ProtocolException
  {
    this.driver = driver;
    this.socket = socket;
    this.socketInput = in;
    this.socketOutput = out;
    this.cjdbcUrl = cjdbcUrl;
    this.controllerInfo = controller;
    this.vdbUser = userName;
    this.vdbPassword = password;

    if (!in.readBoolean()) // failed
      throw new AuthenticationException(in.readUTF());

    this.controllerNeedsSqlSkeleton = in.readBoolean();
    String sfilter = in.readUTF();
    this.blobFilter = AbstractBlobFilter.getBlobFilterInstance(sfilter);

    setUrlParametersOptionsOnConnection(cjdbcUrl);
    if (cjdbcUrl.isDebugEnabled())
      System.out.println("New connection:" + this.toString());
  }

  /**
   * Set CjdbcUrl parameters options on connection.
   * 
   * @param cjdbcUrl the C-JDBC URL to use
   */
  private void setUrlParametersOptionsOnConnection(CjdbcUrl cjdbcUrl)
  {
    HashMap cjdbcUrlParameters = cjdbcUrl.getParameters();

    String booleanTrue = (String) cjdbcUrlParameters
        .get(Driver.BOOLEAN_TRUE_PROPERTY);
    if (booleanTrue != null)
      setPreparedStatementBooleanTrue(booleanTrue);

    String booleanFalse = (String) cjdbcUrlParameters
        .get(Driver.BOOLEAN_FALSE_PROPERTY);
    if (booleanFalse != null)
      setPreparedStatementBooleanFalse(booleanFalse);

    String escapeBaskslash = (String) cjdbcUrlParameters
        .get(Driver.ESCAPE_BACKSLASH_PROPERTY);
    if (escapeBaskslash != null)
      setEscapeBackslash(new Boolean(escapeBaskslash).booleanValue());

    String escapeQuote = (String) cjdbcUrlParameters
        .get(Driver.ESCAPE_SINGLE_QUOTE_PROPERTY);
    if (escapeQuote != null)
      setEscapeSingleQuote(new Boolean(escapeQuote).booleanValue());

    String escapeCharacter = (String) cjdbcUrlParameters
        .get(Driver.ESCAPE_CHARACTER_PROPERTY);
    if (escapeCharacter != null)
      setEscapeChar(escapeCharacter);

    String isDriverProcessed = (String) cjdbcUrlParameters
        .get(Driver.DRIVER_PROCESSED_PROPERTY);
    if (isDriverProcessed != null)
      setDriverProcessed(Boolean.valueOf(isDriverProcessed).booleanValue());

    // true if transparent connection pooling must be used
    this.connectionPooling = !"false".equals(cjdbcUrlParameters
        .get(Driver.CONNECTION_POOLING_PROPERTY));

    if (cjdbcUrl.isDebugEnabled())
    {
      // Give a warning for unrecognized driver options
      for (Iterator iter = cjdbcUrlParameters.entrySet().iterator(); iter
          .hasNext();)
      {
        Map.Entry e = (Map.Entry) iter.next();
        String param = (String) e.getKey();
        if (!Driver.driverProperties.contains(param))
          System.out.println("Unrecognized driver parameter: " + param + " = "
              + (String) e.getValue());
      }
    }
  }

  /**
   * @see java.lang.Object#finalize()
   */
  protected void finalize() throws Throwable
  {
    if (this.closeSocketOnGC)
    {
      Throwable t = null;
      try
      {
        rollback();
      }
      catch (Exception e)
      {
        t = e;
      }
      try
      {
        close();
      }
      catch (Exception e)
      {
        t = e;
      }

      if (t != null)
      {
        throw t;
      }

    }
    super.finalize();
  }

  /**
   * Gets the C-JDBC URL of the database of the connection.
   * 
   * @return value of url.
   */
  public String getUrl()
  {
    return cjdbcUrl.getUrl();
  }

  /**
   * Gets the user name used to login to the database.
   * 
   * @return login name
   */
  public String getUserName()
  {
    return vdbUser;
  }

  /**
   * Gets the password used to login to the database.
   * 
   * @return password
   */
  public String getPassword()
  {
    return vdbPassword;
  }

  /**
   * Get the information about the controller we are connected to
   * 
   * @return <code>ControllerInfo</code> object of the controller
   */
  public ControllerInfo getControllerInfo()
  {
    return controllerInfo;
  }

  //
  // Connection interface methods
  //

  /**
   * After this call, <code>getWarnings()</code> returns <code>null</code>
   * until a new warning is reported for this connection.
   */
  public void clearWarnings()
  {
    firstWarning = null;
  }

  /**
   * @see #close()
   */
  private void throwSQLExceptionIfClosed(String message)
      throws DriverSQLException
  {
    if (isClosed)
      throw new DriverSQLException(message);
  }

  /**
   * @see #close()
   */
  private void throwSQLExceptionIfClosed() throws DriverSQLException
  {
    // default message
    throwSQLExceptionIfClosed("Tried to operate on a closed Connection");
  }

  /**
   * Releases the connection. In fact, the connection is marked to be released
   * but will be effectively closed by the <code>ConnectionClosingThread</code>
   * if the connection has not been reused before.
   * 
   * @exception DriverSQLException if an error occurs
   */
  public void close() throws DriverSQLException
  {
    synchronized (this) // Wait until other methods/Commands are done
    {
      throwSQLExceptionIfClosed();
      isClosed = true;
      /*
       * All JDBC entry points (methods) of this Connection have to
       * throwSQLExceptionIfClosed(). Relaxed: at least every JDBC method _with
       * some side-effect_ has to throwSQLExceptionIfClosed(). So now we are
       * safe and can leave the lock, since they will fail anyway.
       */
    }

    if (connectionPooling)
    { // Try to pool the connection for later reuse
      try
      {
        if (cjdbcUrl.isDebugEnabled())
          System.out.println("Resetting connection and adding it to the pool");
        autoCommit = true;
        readOnly = false;
        socketOutput.writeInt(Commands.Reset);
        socketOutput.flush();
      }
      catch (IOException e)
      {
        throw new DriverSQLException("I/O Error while closing the connection\n"
            + e.getLocalizedMessage(), e);
      }

      // only one (Connection) accessing the pool at a time
      synchronized (driver.pendingConnectionClosing)
      {
        if (!driver.connectionClosingThreadisAlive)
        { // First connection to close, start a new closing thread
          if (cjdbcUrl.isDebugEnabled())
            System.out.println("Starting a new connection closing thread");
          ConnectionClosingThread t = new ConnectionClosingThread(driver);
          t.start();
        }
        // Add to the list
        driver.pendingConnectionClosing.add(this);
      }
    }
    else
    { // Close connection
      try
      {
        driver = null; // probably useless since we use now
        // throwSQLExceptionIfClosed(), but
        // harmless anyway
        if (socketOutput != null)
        {
          if (cjdbcUrl.isDebugEnabled())
            System.out.println("Closing connection");
          socketOutput.writeInt(Commands.Close);
          socketOutput.flush();
          if (socketInput != null)
          { // Wait for the controller to receive the connection and close the
            // stream. If we do not wait for the controller ack, the connection
            // is closed on the controller before the closing is handled which
            // results in an ugly warning message on the controller side. We are
            // not in a hurry when closing the connection so let do the things
            // nicely!
            receiveBoolean();
            socketInput.close();
          }
          socketOutput.close();
        }

        if (socket != null)
          socket.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Makes all changes made since the previous commit/rollback permanent and
   * releases any database locks currently held by the <code>Connection</code>.
   * This method should only be used when auto-commit has been disabled. (If
   * <code>autoCommit</code>== <code>true</code>, then we throw a
   * DriverSQLException).
   * 
   * @exception DriverSQLException if a database access error occurs or the
   *              connection is in autocommit mode
   * @see Connection#setAutoCommit(boolean)
   */
  public synchronized void commit() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    if (autoCommit)
      throw new DriverSQLException(
          "Trying to commit a connection in autocommit mode");

    long firstTransactionId = this.transactionId;
    try
    {
      socketOutput.writeInt(Commands.Commit);
      socketOutput.flush();
      // Commit is followed by a BEGIN
      this.transactionId = receiveLong();
      writeExecutedInTransaction = false;
      if (cjdbcUrl.isDebugEnabled())
        System.out.println("New transaction " + transactionId
            + " has been started");
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw new DriverSQLException(
          "I/O Error occured around commit of transaction '"
              + firstTransactionId + "\n" + e.getLocalizedMessage(), e);
    }
  }

  /**
   * SQL statements without parameters are normally executed using
   * <code>Statement</code> objects. If the same SQL statement is executed
   * many times, it is more efficient to use a <code>PreparedStatement</code>.
   * The <code>ResultSet</code> will be
   * <code>TYPE_FORWARD_ONLY</cde>/<code>CONCUR_READ_ONLY</code>.
   *    *
   * @return a new <code>Statement</code> object
   * @exception DriverSQLException passed through from the constructor
   */
  public java.sql.Statement createStatement() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    return new Statement(this);
  }

  /**
   * SQL statements without parameters are normally executed using
   * <code>Statement</code> objects. If the same SQL statement is executed
   * many times, it is more efficient to use a <code>PreparedStatement</code>.
   * 
   * @param resultSetType resultSetType to use
   * @param resultSetConcurrency resultSetConcurrency to use
   * @return a new <code>Statement</code> object
   * @exception SQLException passed through from the constructor
   */
  public java.sql.Statement createStatement(int resultSetType,
      int resultSetConcurrency) throws SQLException
  {
    throwSQLExceptionIfClosed();
    Statement s = new Statement(this);
    s.setResultSetType(resultSetType);
    s.setResultSetConcurrency(resultSetConcurrency);
    return s;
  }

  /**
   * Gets the current auto-commit state.
   * 
   * @return current state of the auto-commit mode
   * @exception DriverSQLException is connection is closed
   * @see Connection#setAutoCommit
   */
  public boolean getAutoCommit() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    return this.autoCommit;
  }

  /**
   * A connection's database is able to provide information describing its
   * tables, its supported SQL grammar, its stored procedures, the capabilities
   * of this connection, etc. This information is made available through a
   * DatabaseMetaData object.
   * 
   * @return a <code>DatabaseMetaData</code> object for this connection
   * @exception DriverSQLException if connection is closed
   */
  public java.sql.DatabaseMetaData getMetaData() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    if (metaData == null)
    {
      metaData = new DatabaseMetaData(this);
    }
    return metaData;
  }

  /**
   * Return current catalog name.
   * 
   * @return name of the current <code>VirtualDatabase</code>
   * @throws DriverSQLException if any error occurs
   * @see Connection#getCatalog()
   */
  public synchronized String getCatalog() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    try
    {
      socketOutput.writeInt(Commands.ConnectionGetCatalog);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Connection.getCatalog");

      return receiveString();
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getCatalog", e);
    }
  }

  /**
   * getCatalogs definition.
   * 
   * @return instace of <code>ResultSet<code>
   * @throws DriverSQLException if fails (include ANY exception that can be thrown in the code)
   */
  public synchronized ResultSet getCatalogs() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getCatalogs";
    try
    {
      socketOutput.writeInt(Commands.ConnectionGetCatalogs);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName);

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getCatalogs", e);
    }
  }

  protected synchronized java.sql.ResultSet getProcedures(String catalog,
      String schemaPattern, String procedureNamePattern)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getProcedures";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetProcedures);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(procedureNamePattern);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + procedureNamePattern + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getProcedures", e);
    }
  }

  protected synchronized java.sql.ResultSet getProcedureColumns(String catalog,
      String schemaPattern, String procedureNamePattern,
      String columnNamePattern) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getProcedureColumns";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetProcedureColumns);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(procedureNamePattern);
      socketOutput.writeUTF(columnNamePattern);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + procedureNamePattern + "," + columnNamePattern + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getProcedureColumns", e);
    }
  }

  /**
   * Gets this Connection's current transaction isolation mode. If the
   * transaction isolation has not been set using setTransactionIsolation, this
   * method will return by default
   * java.sql.Connection.TRANSACTION_READ_UNCOMMITTED whatever transaction
   * isolation is really used by the cluster nodes. If you want to enfore
   * TRANSACTION_READ_UNCOMMITTED, you have to explicitely call
   * setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED)
   * 
   * @return the current <code>TRANSACTION_*</code> mode value
   * @exception DriverSQLException if a database access error occurs
   * @see #setTransactionIsolation(int)
   */
  public int getTransactionIsolation() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    // Warning, here we assume that if no transaction isolation is set the
    // database will provide READ_UNCOMMITED.
    if (isolationLevel == DEFAULT_TRANSACTION_ISOLATION_LEVEL)
      return java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;
    return isolationLevel;
  }

  /**
   * C-JDBC does NOT support type map.
   * 
   * @return an exception
   * @exception SQLException not supported
   */
  public java.util.Map getTypeMap() throws SQLException
  {
    throw new NotImplementedException("getTypeMap()");
  }

  /**
   * The first warning reported by calls on this connection is returned. <B>
   * Note: </B> Sebsequent warnings will be changed to this SQLWarning
   * 
   * @return the first SQLWarning or null
   */
  public SQLWarning getWarnings()
  {
    return firstWarning;
  }

  /**
   * Returns <code>true</code> if the connection has been closed by the user
   * (but C-JDBC may leave it open underneath, unknown to the user).
   * 
   * @return <code>true</code> if connection has never been opened or
   *         <code>close()</code> has been called
   */
  public boolean isClosed()
  {
    return isClosed;
  }

  /**
   * Tests to see if the connection is in read only Mode. Note that we cannot
   * really put the database in read only mode, but we pretend we can by
   * returning the value of the <code>readOnly</code> flag.
   * 
   * @return <code>true</code> if the connection is read only
   */
  public boolean isReadOnly()
  {
    return readOnly;
  }

  /**
   * As we can't know for sure which database will execute this request (now or
   * later), we can't translate it in the native query language of the
   * underlying DBMS. Therefore the query is returned unchanged.
   * 
   * @param query the query to change
   * @return the original query
   */
  public String nativeSQL(String query)
  {
    return query;
  }

  /**
   * Creates a CallableStatement that contains sql and produces a ResultSet that
   * is TYPE_SCROLL_INSENSITIVE and CONCUR_READ_ONLY.
   * 
   * @param sql SQL request
   * @return nothing
   * @exception SQLException not supported
   */
  public java.sql.CallableStatement prepareCall(String sql) throws SQLException
  {
    throwSQLExceptionIfClosed();
    return prepareCall(sql, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
        java.sql.ResultSet.CONCUR_READ_ONLY);
  }

  /**
   * Stored procedure call are not yet supported by C-JDBC.
   * 
   * @param sql a <code>String</code> value
   * @param resultSetType an <code>int</code> value
   * @param resultSetConcurrency an <code>int</code> value
   * @return nothing
   * @exception SQLException not supported
   */
  public java.sql.CallableStatement prepareCall(String sql, int resultSetType,
      int resultSetConcurrency) throws SQLException
  {
    throwSQLExceptionIfClosed();
    CallableStatement c = new CallableStatement(this, sql);
    c.setResultSetType(resultSetType);
    c.setResultSetConcurrency(resultSetConcurrency);
    return c;
  }

  /**
   * A SQL statement with or without <code>IN</code> parameters can be
   * pre-compiled and stored in a PreparedStatement object. This object can then
   * be used to efficiently execute this statement multiple times.
   * 
   * @param sql a SQL statement that may contain one or more '?' IN * parameter
   *          placeholders
   * @return a new <code>PreparedStatement</code> object containing the
   *         pre-compiled statement.
   * @exception SQLException if a database access error occurs.
   */
  public java.sql.PreparedStatement prepareStatement(String sql)
      throws SQLException
  {
    throwSQLExceptionIfClosed();
    return new PreparedStatement(this, sql);
  }

  /**
   * A SQL statement with or without IN parameters can be pre-compiled and
   * stored in a <code>PreparedStatement</code> object. This object can then
   * be used to efficiently execute this statement multiple times.
   * 
   * @param sql a SQL statement that may contain one or more '?' IN
   * @param resultSetType <code>ResultSetType</code> to use
   * @param resultSetConcurrency <code>ResultSetConcurrency</code> to use
   * @return a new <code>PreparedStatement</code> object
   * @exception SQLException passed through from the constructor
   */
  public java.sql.PreparedStatement prepareStatement(String sql,
      int resultSetType, int resultSetConcurrency) throws SQLException
  {
    throwSQLExceptionIfClosed();
    PreparedStatement s = new PreparedStatement(this, sql);
    s.setResultSetType(resultSetType);
    s.setResultSetConcurrency(resultSetConcurrency);
    return s;
  }

  /**
   * Drops all changes made since the previous commit/rollback and releases any
   * database locks currently held by this connection. If the connection was in
   * autocommit mode, we throw a DriverSQLException.
   * 
   * @exception DriverSQLException if a database access error occurs or the
   *              connection is in autocommit mode
   * @see Connection#commit()
   */
  public synchronized void rollback() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    if (autoCommit)
      throw new DriverSQLException(
          "Trying to rollback a connection in autocommit mode");

    long initialTransactionId = this.transactionId;
    try
    {
      socketOutput.writeInt(Commands.Rollback);
      socketOutput.flush();
      // Rollback is followed by a BEGIN
      this.transactionId = receiveLong();
      writeExecutedInTransaction = false;

      if (cjdbcUrl.isDebugEnabled())
        System.out
            .println("Transaction " + transactionId + " has been started");
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw new DriverSQLException(
          "I/O Error occured around rollback of transaction '"
              + initialTransactionId + "\n" + e.getLocalizedMessage(), e);
    }
  }

  /**
   * If a connection is in auto-commit mode, then all its SQL statements will be
   * executed and committed as individual transactions. Otherwise, its SQL
   * statements are grouped into transactions that are terminated by either
   * {@link #commit()}or {@link #rollback()}. By default, new connections are
   * in auto-commit mode. The commit occurs when the statement completes or the
   * next execute occurs, whichever comes first. In the case of statements
   * returning a <code>ResultSet</code>, the statement completes when the
   * last row of the <code>ResultSet</code> has been retrieved or the
   * <code>ResultSet</code> has been closed. In advanced cases, a single
   * statement may return multiple results as well as output parameter values.
   * Here the commit occurs when all results and output param values have been
   * retrieved.
   * 
   * @param autoCommit <code>true</code> enables auto-commit;
   *          <code>false</code> disables it
   * @exception DriverSQLException if a database access error occurs
   */
  public synchronized void setAutoCommit(boolean autoCommit)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    if (this.autoCommit == autoCommit)
      return;

    if (autoCommit)
    { // enable autocommit
      try
      {
        if (cjdbcUrl.isDebugEnabled())
          System.out.println("Setting connection in autocommit mode");
        socketOutput.writeInt(Commands.SetAutoCommit);
        socketOutput.flush();

        receiveBoolean();
        writeExecutedInTransaction = false;
        transactionId = 0;
        this.autoCommit = true;
        return;

      }
      catch (SerializableException se)
      {
        throw new DriverSQLException(se);
      }
      catch (IOException e)
      {
        throw new DriverSQLException(
            "Error while trying to enable autocommit\n"
                + e.getLocalizedMessage(), e);
      }
    }
    else
    { // disable autocommit
      try
      {
        socketOutput.writeInt(Commands.Begin);
        socketOutput.flush();

        transactionId = receiveLong();
        this.autoCommit = false;

        if (cjdbcUrl.isDebugEnabled())
          System.out.println("Transaction " + transactionId
              + " has been started");
      }
      catch (SerializableException e)
      {
        throw new DriverSQLException(e);
      }
      catch (IOException e)
      {
        throw new DriverSQLException(
            "I/O Error while trying to disable autocommit\n"
                + e.getLocalizedMessage(), e);
      }
    }
  }

  /**
   * Change the current catalog
   * 
   * @param catalog a <code>String</code> value
   * @exception SQLException if fails or if catalog name is invalid
   */
  public synchronized void setCatalog(String catalog) throws SQLException
  {
    throwSQLExceptionIfClosed();
    if (catalog == null)
      throw new DriverSQLException("Invalid Catalog");
    cjdbcUrl.setUrl(driver.changeDatabaseName(cjdbcUrl.getUrl(), catalog));

    try
    {
      socketOutput.writeInt(Commands.ConnectionSetCatalog);
      socketOutput.writeUTF(catalog);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Connection.setCatalog(" + catalog + ")");

      if (!receiveBoolean())
        throw new DriverSQLException("Invalid Catalog");

    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("setCatalog", e);
    }
  }

  /**
   * You can put a connection in read-only mode as a hint to enable database
   * optimizations
   * <p>
   * <B>Note: </B> setReadOnly cannot be called while in the middle of a
   * transaction with write requests.
   * 
   * @param readOnly <code>true</code> enables read-only mode;
   *          <code>false</code> disables it
   * @exception DriverSQLException if a database access error occurs
   */
  public void setReadOnly(boolean readOnly) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    if ((autoCommit == false) && writeExecutedInTransaction)
      throw new DriverSQLException(
          "setReadOnly cannot be called in a transaction that has executed write requests.");

    this.readOnly = readOnly;
  }

  /**
   * You can call this method to try to change the transaction isolation level
   * using one of the TRANSACTION_* values.
   * <p>
   * <B>Note: </B> this method cannot be called while in the middle of a
   * transaction.
   * 
   * @param level one of the TRANSACTION_* isolation values with * the exception
   *          of TRANSACTION_NONE; some databases may * not support other values
   * @exception DriverSQLException if a database access error occurs
   * @see java.sql.DatabaseMetaData#supportsTransactionIsolationLevel
   */
  public synchronized void setTransactionIsolation(int level)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    // Check if we are in a transaction or not. We have no trace on the driver
    // side if a read query has already been executed or not in the current
    // transaction (if any). We let the controller check for this (we only check
    // for writes here) as well as if the underlying databases support the
    // transaction isolation level. If this is not supported, the driver will
    // send back an exception.
    if ((autoCommit == false) && writeExecutedInTransaction)
      throw new DriverSQLException(
          "setTransactionIsolation cannot be called in a transaction that has executed write requests.");

    if (level != isolationLevel)
    { // Only try to change if there is a new value
      if ((level == TRANSACTION_READ_COMMITTED)
          || (level == TRANSACTION_READ_UNCOMMITTED)
          || (level == TRANSACTION_REPEATABLE_READ)
          || (level == TRANSACTION_SERIALIZABLE))
      {
        try
        {
          socketOutput.writeInt(Commands.SetTransactionIsolation);
          socketOutput.writeInt(level);
          socketOutput.flush();

          if (cjdbcUrl.isDebugEnabled())
            System.out.println("Setting transaction isolation level to "
                + level);

          receiveBoolean();
          // Success
          isolationLevel = level;
          return;

        }
        catch (SerializableException e)
        {
          throw new DriverSQLException(e);
        }
        catch (IOException ioe)
        {
          throw new DriverSQLException(
              "I/O Error while setting transaction isolation level to " + level
                  + "\n" + ioe.getLocalizedMessage(), ioe);
        }
      }
      else
        throw new DriverSQLException("Invalid transaction isolation level "
            + level);
    } // we were already in that level; do nothing.
  }

  /**
   * C-JDBC does NOT support type map.
   * 
   * @param map ignored
   * @exception SQLException not supported
   */
  public void setTypeMap(java.util.Map map) throws SQLException
  {
    throw new NotImplementedException("setTypeMap()");
  }

  /*
   * Connection C-JDBC internals
   */

  /**
   * Sets the closeSocketOnGC value.
   * 
   * @param closeSocketOnGC The closeSocketOnGC to set.
   */
  private void setCloseSocketOnGC(boolean closeSocketOnGC)
  {
    this.closeSocketOnGC = closeSocketOnGC;
  }

  /**
   * Set the autocommit mode and read-only status on this request.
   * 
   * @param request The request to set
   */
  private void setConnectionParametersOnRequest(AbstractRequest request)
  {
    request.setIsAutoCommit(autoCommit);
    request.setIsReadOnly(readOnly);
    request.setDriverProcessed(driverProcessed);
  }

  /**
   * Returns a DriverResultSet read from the stream or throws the
   * SerializableException that came instead
   * 
   * @param callerName used for error messages. Is this really useful?
   * @return received ResultSet
   * @throws IOException stream or protocol error
   * @throws SerializableException received from the controller
   */
  private DriverResultSet receiveResultSet(String callerName)
      throws IOException, ProtocolException, SerializableException
  {
    TypeTag tag = new TypeTag(socketInput);

    if (TypeTag.NULL_RESULTSET.equals(tag))
      return null;

    if (TypeTag.RESULTSET.equals(tag))
    {
      DriverResultSet drs = new DriverResultSet(this);
      return drs;
    }

    if (TypeTag.EXCEPTION.equals(tag))
      throw receiveException();

    throw new ProtocolException(callerName
        + ": expected a resultset, received unexpected tag: " + tag);
  }

  /**
   * Serialize a procedure on the output stream by sending only the needed
   * parameters to reconstruct it on the controller
   * 
   * @param proc the procedure to send
   * @param isRead true if this is a read stored procedure
   * @throws IOException if fails
   */
  private void procedureOnStream(StoredProcedure proc, boolean isRead)
      throws IOException
  {
    if (!isRead)
      writeExecutedInTransaction = true;

    proc.sendToStream(socketOutput, this.controllerNeedsSqlSkeleton);
  }

  /**
   * Serialize a write request on the output stream by sending only the needed
   * parameters to reconstruct it on the controller
   * 
   * @param request the write request to send
   * @throws IOException if fails
   */
  private void writeRequestOnStream(AbstractWriteRequest request)
      throws IOException
  {
    if (!autoCommit)
      writeExecutedInTransaction = true;
    socketOutput.writeInt(org.objectweb.cjdbc.common.sql.RequestType
        .getRequestType(request));

    request.sendToStream(socketOutput, this.controllerNeedsSqlSkeleton);

  }

  /**
   * Deserialize an exception from the stream: converts explicit protocol typing
   * into java types.
   * 
   * @return the deserialized exception read from the stream
   * @throws IOException stream error
   */
  private SerializableException receiveException() throws IOException
  {
    TypeTag exceptionType = new TypeTag(socketInput);

    if (TypeTag.BACKEND_EXCEPTION.equals(exceptionType))
      return new BackendDriverException(socketInput);
    if (TypeTag.CORE_EXCEPTION.equals(exceptionType))
      return new ControllerCoreException(socketInput);

    throw new ProtocolException("received unknown exception type");
  }

  /**
   * Returns a String read from the stream or throws the SerializableException
   * that came instead.
   * 
   * @throws IOException stream or protocol error
   * @throws SerializableException coming from the controller
   */
  private String receiveString() throws IOException, SerializableException
  {
    TypeTag tag = new TypeTag(socketInput);
    if (TypeTag.NOT_EXCEPTION.equals(tag))
    {
      String answer = socketInput.readUTF();
      return answer;
    }

    throw receiveException();
  }

  /**
   * Returns a boolean read from the stream or throws the SerializableException
   * that came instead.
   * 
   * @throws IOException stream or protocol error
   * @throws SerializableException coming from the controller
   */
  private boolean receiveBoolean() throws IOException, SerializableException
  {
    TypeTag tag = new TypeTag(socketInput);
    if (TypeTag.NOT_EXCEPTION.equals(tag))
    {
      boolean answer = socketInput.readBoolean();
      return answer;
    }

    throw receiveException();
  }

  /**
   * Returns a int read from the stream or throws the SerializableException that
   * came instead.
   * 
   * @throws IOException stream or protocol error
   * @throws SerializableException coming from the controller
   */
  private int receiveInt() throws IOException, SerializableException
  {
    TypeTag tag = new TypeTag(socketInput);
    if (TypeTag.NOT_EXCEPTION.equals(tag))
    {
      int answer = socketInput.readInt();
      return answer;
    }

    throw receiveException();
  }

  /**
   * Returns a long read from the stream or throws the SerializableException
   * that came instead.
   * 
   * @throws IOException stream or protocol error
   * @throws SerializableException coming from the controller
   */
  private long receiveLong() throws IOException, SerializableException
  {
    TypeTag tag = new TypeTag(socketInput);
    if (TypeTag.NOT_EXCEPTION.equals(tag))
    {
      long answer = socketInput.readLong();
      return answer;
    }

    throw receiveException();
  }

  /**
   * Serialize a savepoint on the output stream by sending only the needed
   * parameters to reconstruct it on the controller
   * 
   * @param savepoint the savepoint to send
   * @throws IOException if fails
   */
  private void savepointOnStream(Savepoint savepoint) throws IOException
  {
    writeExecutedInTransaction = true;

    try
    {
      socketOutput.writeUTF(savepoint.getSavepointName());
      return;
    }
    catch (SQLException ignore)
    {
      // Ignoring because we are dealing with an un-named savepoint
    }

    try
    {
      socketOutput.writeUTF(String.valueOf(savepoint.getSavepointId()));
      return;
    }
    catch (SQLException ignore)
    {
      // We should never get here
    }
  }

  /**
   * Try first to reconnect to the same controller and if we don't succeed then
   * we notify the suspicion of failure if the current controller and we
   * reconnect to a controller chosen using the policy specified in the JDBC URL
   * of this connection. FIXME: we need a less coarse exception signature
   * 
   * @throws DriverSQLException if an error occured during reconnect
   */
  private synchronized void reconnect() throws DriverSQLException
  {
    // Get rid of current connection
    try
    {
      this.socket.close();
    }
    catch (IOException ignore)
    {
    }
    try
    {
      this.socketInput.close();
    }
    catch (IOException ignore)
    {
    }
    try
    {
      this.socketOutput.close();
    }
    catch (IOException ignore)
    {
    }
    // only one (Connection) accessing the pool at a time
    synchronized (driver.pendingConnectionClosing)
    {
      if (driver.pendingConnectionClosing.remove(this))
        System.out.println("Warning! Closed call before reconnect");
    }

    Connection newconn = null;
    // The CjdbcUrl does not carry the login/password info so we have to
    // re-create these properties to reconnect
    Properties properties = new Properties();
    properties.setProperty(Driver.USER_PROPERTY, vdbUser);
    properties.setProperty(Driver.PASSWORD_PROPERTY, vdbPassword);

    AbstractControllerConnectPolicy controllerConnectPolicy = cjdbcUrl
        .getControllerConnectPolicy();
    // If we are the first to fail on this controller, we have to retry it might
    // just be our connection that has been lost.
    if (!controllerConnectPolicy.isSuspectedOfFailure(controllerInfo))
    { // Let's retry the current controller
      try
      {
        newconn = (Connection) driver.connectToController(properties, cjdbcUrl,
            controllerInfo);
        if (cjdbcUrl.isDebugEnabled())
          System.out.println("Succeeded to reconnect to current controller: "
              + controllerInfo);
      }
      catch (Exception e)
      {
        if (cjdbcUrl.isDebugEnabled())
          System.out.println("Failed to reconnect to current controller "
              + controllerInfo);
        newconn = null; // flag that we failed
        controllerConnectPolicy.suspectControllerOfFailure(controllerInfo);
      }
    }

    if (newconn == null)
    {
      try
      {
        // At this point, the current controller is down and we have to try a
        // new one that will be allocated by the policy specified in the URL.
        controllerInfo = controllerConnectPolicy.getController();
        if (cjdbcUrl.isDebugEnabled())
          System.out.println("Trying to reconnect to another controller: "
              + controllerInfo);
        newconn = (Connection) driver.connectToController(properties, cjdbcUrl,
            controllerInfo);
      }
      catch (AuthenticationException e)
      {
        // Should not happen, this probably mean an inconsistency in controller
        // configuration but safely ignore (see below)
        String msg = "Warning! Authentication exception received on connection retry, controller configuration might be inconsistent";
        if (cjdbcUrl.isInfoEnabled())
          System.out.println(msg);
        throw new DriverSQLException(msg, e);
      }
      catch (NoMoreControllerException nmc)
      {
        throw new DriverSQLException(nmc);
      }
      catch (DriverSQLException e1)
      {
        // Impossible to connect to the new controller
        String msg = "Failed to reconnect to other controller: "
            + controllerInfo;
        if (cjdbcUrl.isDebugEnabled())
          System.out.println(msg);
        newconn = null;
        controllerConnectPolicy.suspectControllerOfFailure(controllerInfo);
        throw new DriverSQLException(msg, e1);
      }
    }

    // success: let's steal the new connection for ourselves
    newconn.setCloseSocketOnGC(false);
    this.socket = newconn.socket;
    this.socketInput = newconn.socketInput;
    this.socketOutput = newconn.socketOutput;
    this.controllerInfo = newconn.controllerInfo;
    this.isClosed = false;
    try
    {
      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Restoring connection state on controller "
            + controllerInfo);
      socketOutput.writeInt(Commands.RestoreConnectionState);
      socketOutput.writeBoolean(autoCommit);
      if (!autoCommit)
        socketOutput.writeLong(transactionId);
    }
    catch (IOException e)
    {
      throw new DriverSQLException("Failed to reconnect to controller\n"
          + e.getLocalizedMessage(), e);
    }
  }

  /**
   * Performs a read request and return the reply.
   * 
   * @param request the read request to execute
   * @return a <code>java.sql.ResultSet</code> value
   * @exception DriverSQLException if an error occurs
   */
  protected synchronized java.sql.ResultSet execReadRequest(
      SelectRequest request) throws DriverSQLException
  {
    throwSQLExceptionIfClosed("Closed connection cannot process request '"
        + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH) + "'");

    try
    {
      setConnectionParametersOnRequest(request);
      socketOutput.writeInt(Commands.ExecReadRequest);
      request.sendToStream(socketOutput, this.controllerNeedsSqlSkeleton);
      socketOutput.flush();
      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Executing read request " + request);

      TypeTag tag = new TypeTag(socketInput);

      // First case, we received our ResultSet, let's fetch it.
      if (TypeTag.RESULTSET.equals(tag))
      {
        try
        {
          java.sql.ResultSet drs = new DriverResultSet(this);
          return drs;
        }
        catch (ProtocolException e)
        {
          throw new DriverSQLException(
              "Protocol corruption in Connection.execReadRequest"
                  + " with request "
                  + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH), e);
        }
        catch (IOException e)
        { // Error while reading, retry
          if (cjdbcUrl.isInfoEnabled())
            System.out.println("IOException occured trying to reconnect ("
                + e.getLocalizedMessage() + ")");
          reconnect();
          return execReadRequest(request);
        }
      }

      if (TypeTag.NULL_RESULTSET.equals(tag))
        return null;

      // From this point on, we had an exception
      if (TypeTag.EXCEPTION.equals(tag))
      {
        Exception recvEx = null;
        recvEx = receiveException();
        // dirty hack until cleanup
        if (recvEx instanceof ControllerCoreException)
          recvEx = ((ControllerCoreException) recvEx)
              .compatibilityWrapperHack();

        if (recvEx instanceof NoMoreBackendException)
        {
          if (cjdbcUrl.isInfoEnabled())
            System.out.println("No more backend available on controller");
          try
          {
            // If there is only one controller available rethrow the exception
            if (cjdbcUrl.getControllerList().length == 1)
              throw new DriverSQLException(recvEx);
            else
            {
              // otherwise try to connect to an other controller and re-execute
              // the query
              reconnect();
              // a recursive call in a catch clause is probably not a very good
              // idea why not a simple retry loop ?
              return execReadRequest(request);
            }
          }
          catch (SQLException e1)
          {
            // We deal with this exception in the follwoing if block
            recvEx = e1;
          }
        }
        else if (recvEx instanceof IOException)
        {
          if (cjdbcUrl.isInfoEnabled())
            System.out.println("IOException occured trying to reconnect ("
                + ((IOException) recvEx).getMessage() + ")");
          reconnect();
          // a recursive call in a catch clause is probably not a very good idea
          // why not a simple retry loop?
          return execReadRequest(request);
        }
        else if (recvEx instanceof BackendDriverException)
        {
          // TODO: temporary fix until DriverSQLException is fixed
          throw new DriverSQLException((SerializableException) recvEx);
        }
      }

      // Error, unexpected answer
      throw new ProtocolException(
          "Protocol corruption in Connection.execReadRequest for request "
              + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH));
    }
    catch (RuntimeException e)
    {
      e.printStackTrace();
      throw new DriverSQLException(
          "Connection.execReadRequest: Error occured while request '"
              + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH)
              + "' was processed by C-JDBC Controller", e);
    }
    catch (IOException e)
    { // Connection failed, try to reconnect and re-exec the query
      try
      {
        if (cjdbcUrl.isInfoEnabled())
          System.out.println("IOException occured trying to reconnect ("
              + e.getMessage() + ")");
        reconnect();
        return execReadRequest(request);
      }
      catch (DriverSQLException e1)
      {
        throw new DriverSQLException(
            "Connection lost while executing request '"
                + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH)
                + "' and automatic reconnect failed", e1);
      }
    }
  }

  /**
   * Performs a write request and return the number of rows affected.
   * 
   * @param request the write request to execute
   * @return number of rows affected
   * @exception DriverSQLException if an error occurs
   */
  protected synchronized int execWriteRequest(AbstractWriteRequest request)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed("Closed connection cannot process request '"
        + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH) + "'");

    try
    {
      setConnectionParametersOnRequest(request);
      socketOutput.writeInt(Commands.ExecWriteRequest);
      writeRequestOnStream(request);
      socketOutput.flush();
      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Executing write request " + request);

      return receiveInt();

    }
    catch (SerializableException se)
    {
      throw new DriverSQLException(se);
    }
    catch (IOException e)
    { // Connection failed, try to reconnect and re-exec the query
      try
      {
        reconnect();
        // FIXME:
        // a recursive call in a catch clause is probably not a very good idea
        // why not a simple retry loop ?
        return execWriteRequest(request);
      }
      catch (DriverSQLException e1)
      {
        throw new DriverSQLException("Connection lost while executing request'"
            + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH)
            + "' and automatic reconnect failed (", e1);
      }
    }
  }

  /**
   * Performs a write request and returns the auto-generated keys
   * 
   * @param request the write request to execute
   * @return auto generated keys
   * @exception DriverSQLException if an error occurs
   */
  protected synchronized ResultSet execWriteRequestWithKeys(
      AbstractWriteRequest request) throws DriverSQLException
  {
    throwSQLExceptionIfClosed("Closed Connection cannot process request '"
        + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH) + "'");

    try
    {
      setConnectionParametersOnRequest(request);
      socketOutput.writeInt(Commands.ExecWriteRequestWithKeys);
      writeRequestOnStream(request);
      socketOutput.flush();
      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Executing write request with keys: " + request);

      Exception recvEx = null;
      TypeTag tag = new TypeTag(socketInput);

      /*
       * TODO: the code below is a complete mess. One reason is it's still half
       * legacy design from old protocol, half from the new procotol. It could
       * easily be made much simpler. We should use #receiveResultSet() TODO:
       * test NoMoreBackendException
       */
      if (TypeTag.NULL_RESULTSET.equals(tag))
        return null;

      if (TypeTag.EXCEPTION.equals(tag))
        recvEx = receiveException();
      // dirty hack until cleanup
      if (recvEx instanceof ControllerCoreException)
        recvEx = ((ControllerCoreException) recvEx).compatibilityWrapperHack();

      if (recvEx instanceof BackendDriverException)
        throw new DriverSQLException(recvEx);
      else if (!TypeTag.RESULTSET.equals(tag))
      {
        throw new DriverSQLException(
            "Connection.execWriteRequestWithKeys: Unexpected response "
                + "for request "
                + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH),
            recvEx);
      }
      else
      {
        DriverResultSet drs;
        try
        {
          drs = new DriverResultSet(this);
        }
        catch (IOException e)
        {
          throw new DriverSQLException(
              "Connection.execWriteRequestWithKeys: IOException "
                  + e.getLocalizedMessage()
                  + " while reading keys RS for request "
                  + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH), e);
        }
        return drs;
      }

    }
    catch (IOException e)
    { // Connection failed, try to reconnect and re-exec the query
      try
      {
        reconnect();
        // a recursive call in a catch clause is probably not a very good idea
        // why not a simple retry loop?
        return execWriteRequestWithKeys(request);
      }
      catch (DriverSQLException e1)
      {
        throw new DriverSQLException("Connection lost while executing request'"
            + request.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH)
            + "' and automatic reconnect failed", e1);
      }
    }
  }

  /**
   * Call a stored procedure that returns a ResultSet.
   * 
   * @param proc the stored procedure call
   * @return a <code>java.sql.ResultSet</code> value
   * @exception DriverSQLException if an error occurs
   */
  public synchronized ResultSet execReadStoredProcedure(StoredProcedure proc)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed("Closed Connection cannot process request '"
        + proc.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH) + "'");

    try
    {
      setConnectionParametersOnRequest(proc);
      socketOutput.writeInt(Commands.ExecReadStoredProcedure);
      procedureOnStream(proc, true);
      socketOutput.flush();
      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Executing read stored procedure " + proc);

      Exception recvEx = null;
      TypeTag tag = new TypeTag(socketInput);

      /*
       * TODO: the code below is a complete mess. One reason is it's still half
       * legacy design from old protocol, half from the new procotol. It could
       * easily be made much simpler. We should use #receiveResultSet() TODO:
       * test NoMoreBackendException
       */
      if (TypeTag.NULL_RESULTSET.equals(tag))
        return null;

      if (TypeTag.EXCEPTION.equals(tag))
        recvEx = receiveException();
      // dirty hack until cleanup
      if (recvEx instanceof ControllerCoreException)
        recvEx = ((ControllerCoreException) recvEx).compatibilityWrapperHack();

      if (recvEx instanceof BackendDriverException)
        throw new DriverSQLException(recvEx);
      else if (!TypeTag.RESULTSET.equals(tag))
        throw new DriverSQLException(
            "Connection.execReadStoredProcedure: Unexpected response "
                + " for request "
                + proc.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH), recvEx);
      else
      {
        DriverResultSet drs;
        try
        {
          drs = new DriverResultSet(this);
        }
        catch (IOException e)
        {
          throw new DriverSQLException(
              "Connection.execReadStoredProcedure: IOException "
                  + "while receiving RS for request "
                  + proc.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH), e);
        }
        return drs;
      }
    }
    catch (RuntimeException e)
    {
      throw new DriverSQLException(
          "Connection.execReadStoredProcedure: Error occured while request '"
              + proc.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH)
              + "' was processed by C-JDBC Controller", e);
    }
    catch (IOException e)
    { // Connection failed, try to reconnect and re-exec the query
      try
      {
        reconnect();
        // a recursive call in a catch clause is probably not a very good idea
        // why not a simple retry loop ?
        return execReadStoredProcedure(proc);
      }
      catch (DriverSQLException e1)
      {
        throw new DriverSQLException("Connection lost while executing request'"
            + proc.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH)
            + "' and automatic reconnect failed ", e1);
      }
    }
  }

  /**
   * Call a stored procedure that performs an update.
   * 
   * @param proc the stored procedure call
   * @return number of rows affected
   * @exception DriverSQLException if an error occurs
   */
  protected synchronized int execWriteStoredProcedure(StoredProcedure proc)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed("Closed Connection cannot process request '"
        + proc.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH) + "'");

    try
    {
      setConnectionParametersOnRequest(proc);
      socketOutput.writeInt(Commands.ExecWriteStoredProcedure);
      procedureOnStream(proc, false);
      socketOutput.flush();
      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Executing write stored procedure " + proc);

      return receiveInt();

    }
    catch (SerializableException se)
    {
      throw new DriverSQLException(se);
    }
    catch (IOException e)
    {
      throw new DriverSQLException(
          "execWriteStoredProcedure: I/O Error occured while request '"
              + proc.getSQLShortForm(Constants.SQL_SHORT_FORM_LENGTH)
              + "' was processed by C-JDBC Controller (", e);
    }
  }

  /**
   * @see Connection#getBlobFilter()
   */
  AbstractBlobFilter getBlobFilter()
  {
    return blobFilter;
  }

  //
  // Database Metadata methods
  //

  /**
   * @see java.sql.DatabaseMetaData#getAttributes(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  protected synchronized ResultSet getAttributes(String catalog,
      String schemaPattern, String typeNamePattern, String attributeNamePattern)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getAttributes";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetAttributes);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(typeNamePattern);
      socketOutput.writeUTF(attributeNamePattern);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + typeNamePattern + "," + attributeNamePattern + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getAttributes", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getBestRowIdentifier(java.lang.String,
   *      java.lang.String, java.lang.String, int, boolean)
   */
  protected synchronized ResultSet getBestRowIdentifier(String catalog,
      String schema, String table, int scope, boolean nullable)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getBestRowIdentifier";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetBestRowIdentifier);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schema);
      socketOutput.writeUTF(table);
      socketOutput.writeInt(scope);
      socketOutput.writeBoolean(nullable);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schema + "," + table
            + "," + scope + "," + nullable + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getBestRowIdentifier", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getColumnPrivileges(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  protected synchronized ResultSet getColumnPrivileges(String catalog,
      String schemaPattern, String tableName, String columnNamePattern)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getColumnPrivileges";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetColumnPrivileges);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(tableName);
      socketOutput.writeUTF(columnNamePattern);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + tableName + "," + columnNamePattern + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getColumnPrivileges", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getColumns(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  protected synchronized ResultSet getColumns(String catalog,
      String schemaPattern, String tableNamePattern, String columnNamePattern)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getColumns";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetColumns);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(tableNamePattern);
      socketOutput.writeUTF(columnNamePattern);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + tableNamePattern + "," + columnNamePattern + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getColumns", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getCrossReference(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  protected synchronized ResultSet getCrossReference(String primaryCatalog,
      String primarySchema, String primaryTable, String foreignCatalog,
      String foreignSchema, String foreignTable) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getCrossReference";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetCrossReference);
      socketOutput.writeUTF(primaryCatalog);
      socketOutput.writeUTF(primarySchema);
      socketOutput.writeUTF(primaryTable);
      socketOutput.writeUTF(foreignCatalog);
      socketOutput.writeUTF(foreignSchema);
      socketOutput.writeUTF(foreignTable);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + primaryCatalog + "," + primarySchema
            + "," + primaryTable + "," + foreignCatalog + "," + foreignSchema
            + "," + foreignTable + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getCrossReference", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getExportedKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public ResultSet getExportedKeys(String catalog, String schema, String table)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getExportedKeys";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetExportedKeys);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schema);
      socketOutput.writeUTF(table);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schema + "," + table
            + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getExportedKeys", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getImportedKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  protected synchronized ResultSet getImportedKeys(String catalog,
      String schema, String table) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getImportedKeys";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetImportedKeys);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schema);
      socketOutput.writeUTF(table);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schema + "," + table
            + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getImportedKeys", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getIndexInfo(java.lang.String,
   *      java.lang.String, java.lang.String, boolean, boolean)
   */
  protected synchronized ResultSet getIndexInfo(String catalog, String schema,
      String table, boolean unique, boolean approximate)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getIndexInfo";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetIndexInfo);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schema);
      socketOutput.writeUTF(table);
      socketOutput.writeBoolean(unique);
      socketOutput.writeBoolean(approximate);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schema + "," + table
            + "," + unique + "," + approximate + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getIndexInfo", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getPrimaryKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  protected synchronized ResultSet getPrimaryKeys(String catalog,
      String schemaPattern, String tableNamePattern) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getPrimaryKeys";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetPrimaryKeys);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(tableNamePattern);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + tableNamePattern + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getPrimaryKeys", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getSuperTables(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  protected synchronized ResultSet getSuperTables(String catalog,
      String schemaPattern, String tableNamePattern) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getSuperTables";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetSuperTables);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(tableNamePattern);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + tableNamePattern + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getSuperTables", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getSuperTypes(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  protected synchronized ResultSet getSuperTypes(String catalog,
      String schemaPattern, String typeNamePattern) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getSuperTypes";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetSuperTypes);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(typeNamePattern);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + typeNamePattern + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getSuperTypes", e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData#getTables(String, String,
   *      String, String[])
   */
  protected synchronized ResultSet getTables(String catalog,
      String schemaPattern, String tableNamePattern, String[] types)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getTables";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetTables);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(tableNamePattern);

      if (null == types)
        socketOutput.writeBoolean(false);
      else
      {
        socketOutput.writeBoolean(true);
        socketOutput.writeInt(types.length);
        for (int i = 0; i < types.length; i++)
          socketOutput.writeUTF(types[i]);
      }
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + tableNamePattern + "," + types + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getTables", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getTypeInfo()
   */
  protected synchronized ResultSet getTypeInfo() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getTypeInfo";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetTypeInfo);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "()");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getTypeInfo", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getUDTs(java.lang.String, java.lang.String,
   *      java.lang.String, int[])
   */
  protected synchronized ResultSet getUDTs(String catalog,
      String schemaPattern, String typeNamePattern, int[] types)
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getUDTs";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetUDTs);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(typeNamePattern);

      if (null == types)
        socketOutput.writeBoolean(false);
      else
      {
        socketOutput.writeBoolean(true);
        socketOutput.writeInt(types.length);
        for (int i = 0; i < types.length; i++)
          socketOutput.writeInt(types[i]);
      }
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + typeNamePattern + "," + types + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getUDTs", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getVersionColumns(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public synchronized ResultSet getVersionColumns(String catalog,
      String schema, String table) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getVersionColumns";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetVersionColumns);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schema);
      socketOutput.writeUTF(table);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schema + "," + table
            + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getVersionColumns", e);
    }
  }

  /**
   * Retrieve a static metadata from the controller.
   * 
   * @param key the "getXXX(Y,Z,...)" hash key of the metadata query
   * @return an Object that will be an <tt>Integer</tt> or <tt>Boolean</tt>
   *         or <tt>String</tt>
   * @throws DriverSQLException if fails
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseStaticMetadata()
   * @see org.objectweb.cjdbc.controller.backend.DatabaseBackendMetaData#retrieveDatabaseMetadata()
   */
  synchronized Object getStaticMetadata(String key) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    try
    {
      socketOutput.writeInt(Commands.DatabaseStaticMetadata);
      socketOutput.writeUTF(key);
      socketOutput.flush();
      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Getting " + key + " metadata");

      TypeTag tag = new TypeTag(socketInput);

      if (TypeTag.EXCEPTION.equals(tag))
        throw new DriverSQLException(receiveException());
      else
      {
        tag = new TypeTag(socketInput);
        Object result = SQLDataSerialization.getSerializer(tag)
            .receiveFromStream(socketInput);
        return result;
      }
    }
    catch (NotImplementedException nie)
    {
      throw new DriverSQLException("Internal bug: getSerializer failed"
          + " in getStaticMetadata", nie);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getStaticMetadata", e);
    }
  }

  private DriverSQLException wrapIOExceptionInDriverSQLException(
      String callerName, IOException ioe)
  {
    return new DriverSQLException("I/O Error on method " + callerName + "():\n"
        + ioe.getLocalizedMessage(), ioe);
  }

  /**
   * Get the C-JDBC controller version number.
   * 
   * @return a String containing the controller version
   * @exception DriverSQLException if an error occurs
   */
  public synchronized String getControllerVersionNumber()
      throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    try
    {
      socketOutput.writeInt(Commands.GetControllerVersionNumber);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Connection.getControllerVersionNumber()");

      return receiveString();
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getControllerVersionNumber", e);
    }
  }

  // --------------------------JDBC 3.0-----------------------------

  /**
   * Changes the holdability of <code>ResultSet</code> objects created using
   * this <code>Connection</code> object to the given holdability.
   * 
   * @param holdability a <code>ResultSet</code> holdability constant; one of
   *          <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
   *          <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
   * @throws SQLException if a database access occurs, the given parameter is
   *           not a <code>ResultSet</code> constant indicating holdability,
   *           or the given holdability is not supported
   * @see #getHoldability
   * @see ResultSet
   * @since JDK 1.4
   */
  public void setHoldability(int holdability) throws SQLException
  {
    throw new NotImplementedException("setHoldability");
  }

  /**
   * Retrieves the current holdability of <code>ResultSet</code> objects
   * created using this <code>Connection</code> object.
   * 
   * @return the holdability, one of
   *         <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
   *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
   * @throws SQLException if a database access occurs
   * @see #setHoldability
   * @see ResultSet
   * @since JDK 1.4
   */
  public int getHoldability() throws SQLException
  {
    throw new NotImplementedException("getHoldability");
  }

  /**
   * Creates an unnamed savepoint in the current transaction and returns the new
   * <code>Savepoint</code> object that represents it.
   * 
   * @return the new <code>Savepoint</code> object
   * @exception DriverSQLException if a database access error occurs or this
   *              <code>Connection</code> object is currently in auto-commit
   *              mode
   * @see Savepoint
   * @since JDK 1.4
   */
  public Savepoint setSavepoint() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    if (autoCommit)
      throw new DriverSQLException(
          "Trying to set a savepoint in autocommit mode");

    if (driver == null)
      throw new DriverSQLException("No driver to set a savepoint");

    try
    {
      socketOutput.writeInt(Commands.SetUnnamedSavepoint);
      socketOutput.flush();

      int savepointId = receiveInt();
      return new org.objectweb.cjdbc.driver.Savepoint(savepointId);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException ioe)
    {
      throw wrapIOExceptionInDriverSQLException("setSavePoint", ioe);
    }
  }

  /**
   * Creates a savepoint with the given name in the current transaction and
   * returns the new <code>Savepoint</code> object that represents it.
   * 
   * @param name a <code>String</code> containing the name of the savepoint
   * @return the new <code>Savepoint</code> object
   * @exception DriverSQLException if a database access error occurs or this
   *              <code>Connection</code> object is currently in auto-commit
   *              mode
   * @see Savepoint
   * @since JDK 1.4
   */
  public Savepoint setSavepoint(String name) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    if (name == null)
      throw new IllegalArgumentException("Savepoint name cannot be null");

    if (autoCommit)
      throw new DriverSQLException(
          "Trying to set a savepoint in autocommit mode");

    if (driver == null)
      throw new DriverSQLException("No driver to set a savepoint");

    try
    {
      socketOutput.writeInt(Commands.SetNamedSavepoint);
      socketOutput.writeUTF(name);
      socketOutput.flush();

      this.receiveBoolean();
      return new org.objectweb.cjdbc.driver.Savepoint(name);
    }
    catch (SerializableException se)
    {
      throw new DriverSQLException(se);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("setSavePoint", e);
    }
  }

  /**
   * Undoes all changes made after the given <code>Savepoint</code> object was
   * set.
   * <p>
   * This method should be used only when auto-commit has been disabled.
   * 
   * @param savepoint the <code>Savepoint</code> object to roll back to
   * @exception DriverSQLException if a database access error occurs, the
   *              <code>Savepoint</code> object is no longer valid, or this
   *              <code>Connection</code> object is currently in auto-commit
   *              mode
   * @see Savepoint
   * @see #rollback()
   * @since JDK 1.4
   */
  public void rollback(Savepoint savepoint) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    if (savepoint == null)
      throw new IllegalArgumentException("Savepoint cannot be null");

    if (autoCommit)
      throw new DriverSQLException(
          "Trying to release a savepoint in autocommit mode");

    if (driver == null)
      throw new DriverSQLException("No driver to release a savepoint");

    try
    {
      socketOutput.writeInt(Commands.RollbackToSavepoint);
      savepointOnStream(savepoint);
      socketOutput.flush();

      this.receiveBoolean();
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("(savepoint) rollback", e);
    }
  }

  /**
   * Removes the given <code>Savepoint</code> object from the current
   * transaction. Any reference to the savepoint after it have been removed will
   * cause an <code>SQLException</code> to be thrown.
   * 
   * @param savepoint the <code>Savepoint</code> object to be removed
   * @exception DriverSQLException if a database access error occurs or the
   *              given <code>Savepoint</code> object is not a valid savepoint
   *              in the current transaction
   * @since JDK 1.4
   */
  public void releaseSavepoint(Savepoint savepoint) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    if (savepoint == null)
      throw new IllegalArgumentException("Savepoint cannot be null");

    if (autoCommit)
      throw new DriverSQLException(
          "Trying to release a savepoint in autocommit mode");

    if (driver == null)
      throw new DriverSQLException("No driver to release a savepoint");

    try
    {
      socketOutput.writeInt(Commands.ReleaseSavepoint);
      savepointOnStream(savepoint);
      socketOutput.flush();

      this.receiveBoolean();
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("releaseSavepoint", e);
    }
  }

  /**
   * Creates a <code>Statement</code> object that will generate
   * <code>ResultSet</code> objects with the given type, concurrency, and
   * holdability.
   * <p>
   * This method is the same as the <code>createStatement</code> method above,
   * but it allows the default result set type, concurrency, and holdability to
   * be overridden.
   * 
   * @param resultSetType one of the following <code>ResultSet</code>
   *          constants: <code>ResultSet.TYPE_FORWARD_ONLY</code>,
   *          <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
   *          <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
   * @param resultSetConcurrency one of the following <code>ResultSet</code>
   *          constants: <code>ResultSet.CONCUR_READ_ONLY</code> or
   *          <code>ResultSet.CONCUR_UPDATABLE</code>
   * @param resultSetHoldability one of the following <code>ResultSet</code>
   *          constants: <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
   *          <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
   * @return a new <code>Statement</code> object that will generate
   *         <code>ResultSet</code> objects with the given type, concurrency,
   *         and holdability
   * @exception SQLException if a database access error occurs or the given
   *              parameters are not <code>ResultSet</code> constants
   *              indicating type, concurrency, and holdability
   * @see ResultSet
   * @since JDK 1.4
   */
  public java.sql.Statement createStatement(int resultSetType,
      int resultSetConcurrency, int resultSetHoldability) throws SQLException
  {
    throw new NotImplementedException("createStatement");
  }

  /**
   * Creates a <code>PreparedStatement</code> object that will generate
   * <code>ResultSet</code> objects with the given type, concurrency, and
   * holdability.
   * <p>
   * This method is the same as the <code>prepareStatement</code> method
   * above, but it allows the default result set type, concurrency, and
   * holdability to be overridden.
   * 
   * @param sql a <code>String</code> object that is the SQL statement to be
   *          sent to the database; may contain one or more ? IN parameters
   * @param resultSetType one of the following <code>ResultSet</code>
   *          constants: <code>ResultSet.TYPE_FORWARD_ONLY</code>,
   *          <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
   *          <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
   * @param resultSetConcurrency one of the following <code>ResultSet</code>
   *          constants: <code>ResultSet.CONCUR_READ_ONLY</code> or
   *          <code>ResultSet.CONCUR_UPDATABLE</code>
   * @param resultSetHoldability one of the following <code>ResultSet</code>
   *          constants: <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
   *          <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
   * @return a new <code>PreparedStatement</code> object, containing the
   *         pre-compiled SQL statement, that will generate
   *         <code>ResultSet</code> objects with the given type, concurrency,
   *         and holdability
   * @exception SQLException if a database access error occurs or the given
   *              parameters are not <code>ResultSet</code> constants
   *              indicating type, concurrency, and holdability
   * @see ResultSet
   * @since JDK 1.4
   */
  public java.sql.PreparedStatement prepareStatement(String sql,
      int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException
  {
    throw new NotImplementedException("prepareStatement");
  }

  /**
   * Creates a <code>CallableStatement</code> object that will generate
   * <code>ResultSet</code> objects with the given type and concurrency. This
   * method is the same as the <code>prepareCall</code> method above, but it
   * allows the default result set type, result set concurrency type and
   * holdability to be overridden.
   * 
   * @param sql a <code>String</code> object that is the SQL statement to be
   *          sent to the database; may contain on or more ? parameters
   * @param resultSetType one of the following <code>ResultSet</code>
   *          constants: <code>ResultSet.TYPE_FORWARD_ONLY</code>,
   *          <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
   *          <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
   * @param resultSetConcurrency one of the following <code>ResultSet</code>
   *          constants: <code>ResultSet.CONCUR_READ_ONLY</code> or
   *          <code>ResultSet.CONCUR_UPDATABLE</code>
   * @param resultSetHoldability one of the following <code>ResultSet</code>
   *          constants: <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
   *          <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
   * @return a new <code>CallableStatement</code> object, containing the
   *         pre-compiled SQL statement, that will generate
   *         <code>ResultSet</code> objects with the given type, concurrency,
   *         and holdability
   * @exception SQLException if a database access error occurs or the given
   *              parameters are not <code>ResultSet</code> constants
   *              indicating type, concurrency, and holdability
   * @see ResultSet
   * @since JDK 1.4
   */
  public java.sql.CallableStatement prepareCall(String sql, int resultSetType,
      int resultSetConcurrency, int resultSetHoldability) throws SQLException
  {
    throw new NotImplementedException("prepareCall");
  }

  /**
   * Creates a default <code>PreparedStatement</code> object that has the
   * capability to retrieve auto-generated keys. The given constant tells the
   * driver whether it should make auto-generated keys available for retrieval.
   * This parameter is ignored if the SQL statement is not an
   * <code>INSERT</code> statement.
   * <p>
   * <b>Note: </b> This method is optimized for handling parametric SQL
   * statements that benefit from precompilation. If the driver supports
   * precompilation, the method <code>prepareStatement</code> will send the
   * statement to the database for precompilation. Some drivers may not support
   * precompilation. In this case, the statement may not be sent to the database
   * until the <code>PreparedStatement</code> object is executed. This has no
   * direct effect on users; however, it does affect which methods throw certain
   * SQLExceptions.
   * <p>
   * Result sets created using the returned <code>PreparedStatement</code>
   * object will by default be type <code>TYPE_FORWARD_ONLY</code> and have a
   * concurrency level of <code>CONCUR_READ_ONLY</code>.
   * 
   * @param sql an SQL statement that may contain one or more '?' IN parameter
   *          placeholders
   * @param autoGeneratedKeys a flag indicating whether auto-generated keys
   *          should be returned; one of
   *          <code>Statement.RETURN_GENERATED_KEYS</code> or
   *          <code>Statement.NO_GENERATED_KEYS</code>
   * @return a new <code>PreparedStatement</code> object, containing the
   *         pre-compiled SQL statement, that will have the capability of
   *         returning auto-generated keys
   * @exception SQLException if a database access error occurs or the given
   *              parameter is not a <code>Statement</code> constant
   *              indicating whether auto-generated keys should be returned
   * @since JDK 1.4
   */
  public java.sql.PreparedStatement prepareStatement(String sql,
      int autoGeneratedKeys) throws SQLException
  {
    throwSQLExceptionIfClosed();
    PreparedStatement ps = new PreparedStatement(this, sql);
    ps.setGeneratedKeysFlag(autoGeneratedKeys);
    return ps;
  }

  /**
   * Creates a default <code>PreparedStatement</code> object capable of
   * returning the auto-generated keys designated by the given array. This array
   * contains the indexes of the columns in the target table that contain the
   * auto-generated keys that should be made available. This array is ignored if
   * the SQL statement is not an <code>INSERT</code> statement.
   * <p>
   * An SQL statement with or without IN parameters can be pre-compiled and
   * stored in a <code>PreparedStatement</code> object. This object can then
   * be used to efficiently execute this statement multiple times.
   * <p>
   * <b>Note: </b> This method is optimized for handling parametric SQL
   * statements that benefit from precompilation. If the driver supports
   * precompilation, the method <code>prepareStatement</code> will send the
   * statement to the database for precompilation. Some drivers may not support
   * precompilation. In this case, the statement may not be sent to the database
   * until the <code>PreparedStatement</code> object is executed. This has no
   * direct effect on users; however, it does affect which methods throw certain
   * SQLExceptions.
   * <p>
   * Result sets created using the returned <code>PreparedStatement</code>
   * object will by default be type <code>TYPE_FORWARD_ONLY</code> and have a
   * concurrency level of <code>CONCUR_READ_ONLY</code>.
   * 
   * @param sql an SQL statement that may contain one or more '?' IN parameter
   *          placeholders
   * @param columnIndexes an array of column indexes indicating the columns that
   *          should be returned from the inserted row or rows
   * @return a new <code>PreparedStatement</code> object, containing the
   *         pre-compiled statement, that is capable of returning the
   *         auto-generated keys designated by the given array of column indexes
   * @exception SQLException if a database access error occurs
   * @since JDK 1.4
   */
  public java.sql.PreparedStatement prepareStatement(String sql,
      int[] columnIndexes) throws SQLException
  {
    throw new NotImplementedException("prepareStatement");
  }

  /**
   * Creates a default <code>PreparedStatement</code> object capable of
   * returning the auto-generated keys designated by the given array. This array
   * contains the names of the columns in the target table that contain the
   * auto-generated keys that should be returned. This array is ignored if the
   * SQL statement is not an <code>INSERT</code> statement.
   * <p>
   * An SQL statement with or without IN parameters can be pre-compiled and
   * stored in a <code>PreparedStatement</code> object. This object can then
   * be used to efficiently execute this statement multiple times.
   * <p>
   * <b>Note: </b> This method is optimized for handling parametric SQL
   * statements that benefit from precompilation. If the driver supports
   * precompilation, the method <code>prepareStatement</code> will send the
   * statement to the database for precompilation. Some drivers may not support
   * precompilation. In this case, the statement may not be sent to the database
   * until the <code>PreparedStatement</code> object is executed. This has no
   * direct effect on users; however, it does affect which methods throw certain
   * <code>SQLExceptions</code>.
   * <p>
   * Result sets created using the returned <code>PreparedStatement</code>
   * object will by default be type <code>TYPE_FORWARD_ONLY</code> and have a
   * concurrency level of <code>CONCUR_READ_ONLY</code>.
   * 
   * @param sql an SQL statement that may contain one or more '?' IN parameter
   *          placeholders
   * @param columnNames an array of column names indicating the columns that
   *          should be returned from the inserted row or rows
   * @return a new <code>PreparedStatement</code> object, containing the
   *         pre-compiled statement, that is capable of returning the
   *         auto-generated keys designated by the given array of column names
   * @exception SQLException if a database access error occurs
   * @since JDK 1.4
   */
  public java.sql.PreparedStatement prepareStatement(String sql,
      String[] columnNames) throws SQLException
  {
    throw new NotImplementedException("prepareStatement");
  }

  /**
   * Gets the table types available in this database. The results are ordered by
   * table type.
   * 
   * @return <code>ResultSet</code> each row has a single String column that
   *         is a catalog name
   * @throws SQLException if a database error occurs
   */
  public synchronized ResultSet getTableTypes() throws SQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getTableTypes";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetTableTypes);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName);

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getTableTypes", e);
    }
  }

  /**
   * Gets a description of the access rights for each table available in a
   * catalog. Note that a table privilege applies to one or more columns in the
   * table. It would be wrong to assume that this priviledge applies to all
   * columns (this may be true for some systems but is not true for all.) Only
   * privileges matching the schema and table name criteria are returned. They
   * are ordered by TABLE_SCHEM, TABLE_NAME, and PRIVILEGE.
   * 
   * @param catalog a catalog name; "" retrieves those without a catalog; null
   *          means drop catalog name from the selection criteria
   * @param schemaPattern a schema name pattern; "" retrieves those without a
   *          schema
   * @param tableNamePattern a table name pattern
   * @return <code>ResultSet</code> each row is a table privilege description
   * @throws DriverSQLException if a database access error occurs
   */
  public synchronized ResultSet getTablePrivileges(String catalog,
      String schemaPattern, String tableNamePattern) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getTablePrivileges";
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetTablePrivileges);
      socketOutput.writeUTF(catalog);
      socketOutput.writeUTF(schemaPattern);
      socketOutput.writeUTF(tableNamePattern);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName + "(" + catalog + "," + schemaPattern + ","
            + tableNamePattern + ")");

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getTablePrivileges", e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getSchemas()
   */
  public synchronized ResultSet getSchemas() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    String myName = "Connection.getSchemas()";

    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetSchemas);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println(myName);

      return receiveResultSet(myName);
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getSchemas", e);
    }
  }

  /**
   * @see DatabaseMetaData#getDatabaseProductName()
   */
  public synchronized String getDatabaseProductName() throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    try
    {
      socketOutput.writeInt(Commands.DatabaseMetaDataGetDatabaseProductName);
      socketOutput.flush();

      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Connection.getDatabaseProductName()");

      return receiveString();
    }
    catch (SerializableException e)
    {
      throw new DriverSQLException(e);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("getDatabaseProductName", e);
    }
  }

  /**
   * Fetch next fetchSize rows of data and update the given ResultSet.
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#fetchNextResultSetRows()
   * @param cursorName name of the ResultSet cursor
   * @param fetchSize number of rows to fetch
   * @param drsToUpdate DriverResultSet to update
   * @throws DriverSQLException if an error occurs
   */
  public synchronized void fetchNextData(String cursorName, int fetchSize,
      DriverResultSet drsToUpdate) throws DriverSQLException
  {
    throwSQLExceptionIfClosed();
    try
    {
      socketOutput.writeInt(Commands.FetchNextResultSetRows);
      socketOutput.writeUTF(cursorName);
      socketOutput.writeInt(fetchSize);
      socketOutput.flush();
      if (cjdbcUrl.isDebugEnabled())
        System.out
            .println("Fetching next " + fetchSize + " from " + cursorName);

      TypeTag tag = new TypeTag(socketInput);

      if (TypeTag.EXCEPTION.equals(tag))
        throw new DriverSQLException(receiveException());

      if (!TypeTag.NOT_EXCEPTION.equals(tag))
        throw new ProtocolException();

      drsToUpdate.receiveRows(socketInput);

    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("fetchNextData", e);
    }
  }

  /**
   * Closes the remote ResultSet given its cursor name.
   * 
   * @param cursorName cursor name of the ResultSet to close.
   * @throws SQLException if an error occurs
   */
  public synchronized void closeRemoteResultSet(String cursorName)
      throws SQLException
  {
    throwSQLExceptionIfClosed();
    try
    {
      socketOutput.writeInt(Commands.CloseRemoteResultSet);
      socketOutput.writeUTF(cursorName);
      socketOutput.flush();
      if (cjdbcUrl.isDebugEnabled())
        System.out.println("Closing remote ResultSet");

      receiveBoolean();
    }
    catch (SerializableException se)
    {
      throw new DriverSQLException(se);
    }
    catch (IOException e)
    {
      throw wrapIOExceptionInDriverSQLException("closeRemoteResultSet", e);
    }
  }

  /**
   * Returns the booleanFalse value.
   * 
   * @return Returns the booleanFalse.
   */
  public String getPreparedStatementBooleanFalse()
  {
    return preparedStatementBooleanFalse;
  }

  /**
   * Sets the booleanFalse value.
   * 
   * @param booleanFalse The booleanFalse to set.
   */
  public void setPreparedStatementBooleanFalse(String booleanFalse)
  {
    this.preparedStatementBooleanFalse = booleanFalse;
  }

  /**
   * Returns the booleanTrue value.
   * 
   * @return Returns the booleanTrue.
   */
  public String getPreparedStatementBooleanTrue()
  {
    return preparedStatementBooleanTrue;
  }

  /**
   * Sets the booleanTrue value.
   * 
   * @param booleanTrue The booleanTrue to set.
   */
  public void setPreparedStatementBooleanTrue(String booleanTrue)
  {
    this.preparedStatementBooleanTrue = booleanTrue;
  }

  /**
   * Returns the escapeBackslash value.
   * 
   * @return Returns the escapeBackslash.
   */
  public boolean isEscapeBackslash()
  {
    return escapeBackslash;
  }

  /**
   * Sets the escapeBackslash value.
   * 
   * @param escapeBackslash The escapeBackslash to set.
   */
  public void setEscapeBackslash(boolean escapeBackslash)
  {
    this.escapeBackslash = escapeBackslash;
  }

  /**
   * Returns the escapeSingleQuote value.
   * 
   * @return Returns the escapeSingleQuote.
   */
  public boolean isEscapeSingleQuote()
  {
    return escapeSingleQuote;
  }

  /**
   * Sets the escapeSingleQuote value.
   * 
   * @param escapeSingleQuote The escapeSingleQuote to set.
   */
  public void setEscapeSingleQuote(boolean escapeSingleQuote)
  {
    this.escapeSingleQuote = escapeSingleQuote;
  }

  /**
   * Sets the driverProcessed value
   * 
   * @param processedByDriver true if the PreparedStatement are processed by the
   *          C-JDBC driver.
   */
  void setDriverProcessed(boolean processedByDriver)
  {
    this.driverProcessed = processedByDriver;
  }

  /**
   * @see Connection#isDriverProcessed()
   */
  boolean isDriverProcessed()
  {
    return driverProcessed;
  }

  /**
   * Sets the escapeCharacter value
   * 
   * @param escapeChar the escapeChar value to set
   */
  public void setEscapeChar(String escapeChar)
  {
    this.escapeChar = escapeChar;
  }

  /**
   * @return Returns the escapeChar.
   */
  public String getEscapeChar()
  {
    return escapeChar;
  }

  /**
   * Returns the connectionPooling value.
   * 
   * @return Returns the connectionPooling.
   */
  public boolean isConnectionPooling()
  {
    return connectionPooling;
  }

  /**
   * Sets the connectionPooling value.
   * 
   * @param connectionPooling The connectionPooling to set.
   */
  public void setConnectionPooling(boolean connectionPooling)
  {
    this.connectionPooling = connectionPooling;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    // couldn't we use println() or something here ?
    return "url:" + getUrl() + LINE_SEPARATOR + "user:" + getUserName()
        + LINE_SEPARATOR + "blobFilter:" + blobFilter + LINE_SEPARATOR
        + "connection pooling:" + connectionPooling + LINE_SEPARATOR
        + "driver processed:" + driverProcessed + LINE_SEPARATOR
        + "escape backslash:" + escapeBackslash + LINE_SEPARATOR
        + "escape char:" + escapeChar + LINE_SEPARATOR + "escape single quote:"
        + escapeSingleQuote + LINE_SEPARATOR + "preparedStatementBooleanTrue:"
        + preparedStatementBooleanTrue + LINE_SEPARATOR
        + "preparedStatementBooleanFalse" + preparedStatementBooleanFalse
        + LINE_SEPARATOR;
  }

@Override
public <T> T unwrap(Class<T> iface) throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public boolean isWrapperFor(Class<?> iface) throws SQLException {
	// TODO Auto-generated method stub
	return false;
}

@Override
public Clob createClob() throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Blob createBlob() throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public NClob createNClob() throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public SQLXML createSQLXML() throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public boolean isValid(int timeout) throws SQLException {
	// TODO Auto-generated method stub
	return false;
}

@Override
public void setClientInfo(String name, String value)
		throws SQLClientInfoException {
	// TODO Auto-generated method stub
	
}

@Override
public void setClientInfo(Properties properties) throws SQLClientInfoException {
	// TODO Auto-generated method stub
	
}

@Override
public String getClientInfo(String name) throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Properties getClientInfo() throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Array createArrayOf(String typeName, Object[] elements)
		throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Struct createStruct(String typeName, Object[] attributes)
		throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

}
