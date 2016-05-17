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
 * Contributor(s): Nicolas Modrzyk, Marc Herbert, Jean-Bernard van Zuylen
 */

package org.objectweb.cjdbc.driver.protocol;

/*
 * DO NOT FORMAT THIS FILE (using eclipse for instance), not to wreck the
 * hand-crafted formatting of the comments/documentation below. At least not
 * until custom taglets are implemented so eclipse can do a decent job with
 * them. And maybe never.
 */

/*
 * TODO: replace <br>~argument by @argument etc. and implement the
 * corresponding taglet. A interesting alternative is to dedicate a new "RPC"
 * class with exactly one method per command. This class would be a subset of
 * today's Connection class. We could then use standard @param and @return tags
 * instead of custom tags.
 */

/**
 * Protocol commands between C-JDBC driver (client) and controller (server). All
 * communications follow a classic RPC scheme: the driver sends a Command code,
 * followed by argument(s) for some of the commands, and expects some answer(s),
 * at the very least an error code or an exception. The server is event-driven;
 * communications are inited by the client which is the one sending Protocol
 * commands, so the verbs <cite>send</cite> and <cite>receive</cite> must be
 * understood as from driver point of view. Almost all these commands are put on
 * the wire by client class {@link org.objectweb.cjdbc.driver.Connection} and
 * read (and answered) by class {@link
 * org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#run()}.
 * The only exceptions are the following commands: <br>- ProtocolVersion<br>
 * read only by {@link
 * org.objectweb.cjdbc.controller.core.ControllerWorkerThread#run()}, which then
 * constructs the connection and pass it to VirtualDatabaseWorkerThread for the
 * rest of the execution of this command. <br>- Close<br>
 * in addition to Connection, also sent by
 * {@link org.objectweb.cjdbc.driver.ConnectionClosingThread#closeConnection(Connection)}
 * <br>- Ping <br>
 * sent on a dedicated connection only by {@link
 * org.objectweb.cjdbc.driver.connectpolicy.ControllerPingThread#run()} and
 * silently received only by (again) {@link
 * org.objectweb.cjdbc.controller.core.ControllerWorkerThread#run()}
 * <h2>Protocol Data Types</h2>
 * <strong>optUTF</strong> is a custom type defined like this:
 * 
 * <pre>
 * (boolean false) | (boolean true; UTF somestring)
 * </pre>
 * 
 * <h3>Sent types</h3>
 * <p>
 * Several commands send a SQL query. All SQL queries sent on the wire use the
 * same starting pattern, a <em>requestStub</em> defined below and in
 * {@link org.objectweb.cjdbc.common.sql.AbstractRequest#AbstractRequest(CJDBCInputStream, int)}
 * <br>
 * <strong>requestStub</strong>
 * 
 * <pre>
 * UTF     request           : SQL query
 * boolean EscapeProcessing
 * UTF     LINE_SEPARATOR
 * Int     timeout
 * boolean autoCommit
 * boolean isDriverProcessed
 * </pre>
 * 
 * <p>
 * Queries that expect a result set (read commands mostly) send immediately
 * after the requestStub a <em>subsetLengths</em> parameter, of type: <br>
 * <strong>subsetLengths</strong>. See
 * {@link org.objectweb.cjdbc.common.sql.AbstractRequest#receiveResultSetParams(CJDBCInputStream)}
 * 
 * <pre>
 * Int    maxRows
 * Int    fetchSize
 * </pre>
 * 
 * <p>
 * Depending on some configuration flag/state (shared by driver and controller),
 * most query commands add an optional <em>skeleton</em> parameter of type
 * optUTF.
 * <h3>Received types</h3>
 * <p>
 * Several commands receive a ResultSet of type: <br>
 * <strong>ResultSet</strong>
 * {@link org.objectweb.cjdbc.driver.DriverResultSet#DriverResultSet(CJDBCInputStream)}
 * 
 * <pre>
 *  {@link org.objectweb.cjdbc.driver.Field Field}[]   fields
 *  {@link org.objectweb.cjdbc.driver.protocol.TypeTag}[] java column types
 *  ArrayList   data
 *  optUTF      hasMoreData: cursor name
 * </pre> - <em>fields</em> is the description of the ResultSet columns. <br>-
 * <em>data</em> is the actual data of the ResultSet. Each element of this
 * list is an Object array holding one row of the ResultSet. The whole arraylist
 * is serialized using standard Java serialization/readUnshared().
 * <h3>Exceptions</h3>
 * For almost every command sent, the driver checks if the reply is an exception
 * serialized by the controller instead of the regular reply type. Most
 * exceptions are put on the wire by
 * {@link org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#run()}
 * TODO: finish the classification below. <br>
 * Exception reply recognized by the driver in: <br>
 * FetchNextResultSetRows, closeRemoteResultSet, RestoreConnectionState,
 * setAutoCommit, getCatalog, getCatalogs, setCatalog,
 * getControllerVersionNumber, DatabaseMetaDataGetTables,
 * DatabaseMetaDataGetColumns, DatabaseMetaDataGetPrimaryKeys,
 * DatabaseMetaDataGetProcedureColumns, DatabaseMetaDataGetTableTypes,
 * DatabaseMetaDataGetTablePrivileges, DatabaseMetaDataGetSchemas,
 * DatabaseMetaDataGetDatabaseProductName, DatabaseStaticMetadata <br>
 * Exception reply ignored by the driver in: Close, Reset <br>
 * Exceptions catched by instanceof(catch-all) default clause in: <br>
 * setAutoCommit, <br>
 * Commands not implemented at all by the driver: <br>
 * GetVirtualDatabaseName, <br>
 * TODO: <br>- exceptions and the server side (VirtualDatabaseWorkerThread)
 * <br>- double-check arguments and replies <br>- better topic ordering
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */

public class Commands
{
  /**
   * Command used to create a new connection, while checking that driver and
   * controller are compatible with each other. This constant is not a Command
   * and should be moved out of this file.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF dataBaseName <br>
   * ~argument UTF user <br>
   * ~argument UTF password
   * <p>
   * <br>
   * ~reply boolean needSkeleton: Does the controller require the SQL skeleton?
   * This is only a boolean here, not the full optUTF skeleton often used
   * elsewhere. <br>
   * ~reply UTF sfilter: the kind of blob encoding the server wants the driver
   * to use<br>
   * 
   * @see org.objectweb.cjdbc.controller.core.ControllerWorkerThread#run()
   * @see org.objectweb.cjdbc.driver.ConnectionClosingThread#closeConnection(Connection)
   */

  public static final int ProtocolVersion                        = 20;

  /**
   * Ping is used by the ControllerPingThread to check if a controller is back
   * online after a failure. This command is quite special because it is sent on
   * a new, dedicated socket. There is neither any argument nor any answer: the
   * controller immediately closes the connection after receiving it. This
   * constant is not a Command and should be moved out of this file.
   * 
   * @see org.objectweb.cjdbc.controller.core.ControllerWorkerThread#run()
   * @see org.objectweb.cjdbc.driver.connectpolicy.ControllerPingThread#run()
   */
  public static final int Ping                                   = -1;

  /*
   * SQL requests handling
   */

  /**
   * Performs a read request and returns the reply.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument requestStub <br>
   * ~argument subsetLengths <br>
   * ~argument optUTF cursorname <br>
   * ~argument optUTF skeleton
   * <p>
   * <br>
   * ~reply ResultSet
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#execReadRequest()
   */
  public static final int ExecReadRequest                        = 0;

  /**
   * Performs a write request and returns the number of rows affected.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument Int {@link org.objectweb.cjdbc.common.sql.RequestType} <br>
   * ~argument requestStub <br>
   * ~argument optUTF skeleton
   * <p>
   * <br>
   * ~reply nbRows
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#execWriteRequest()
   */
  public static final int ExecWriteRequest                       = 1;

  /**
   * Performs a write request and returns the auto generated keys.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument Int {@link org.objectweb.cjdbc.common.sql.RequestType} <br>
   * ~argument requestStub <br>
   * ~argument subsetLengths <br>
   * ~argument optUTF skeleton
   * <p>
   * <br>
   * ~reply ResultSet
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#execWriteRequestWithKeys()
   */
  public static final int ExecWriteRequestWithKeys               = 2;

  /**
   * Calls a stored procedure and returns the reply (ResultSet).
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument requestStub <br>
   * ~argument subsetLengths <br>
   * ~argument optUTF skeleton
   * <p>
   * <br>
   * ~reply ResultSet
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase#execReadStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public static final int ExecReadStoredProcedure                = 3;

  /**
   * Calls a stored procedure and returns the number of rows affected (write
   * query).
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument requestStub <br>
   * ~argument optUTF skeleton
   * <p>
   * <br>
   * ~reply Int nbRows
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase#execReadStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public static final int ExecWriteStoredProcedure               = 4;

  /*
   * Transaction management
   */

  /**
   * Begins a new transaction and returns the corresponding transaction
   * identifier. This method is called from the driver when
   * {@link org.objectweb.cjdbc.driver.Connection#setAutoCommit(boolean)}is
   * called with <code>false</code> argument.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply Long transactionId
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase#begin(String)
   */
  public static final int Begin                                  = 20;

  /**
   * Commits the current transaction.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply Long transactionId: id of next transaction
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase#commit(long,
   *      boolean)
   */
  public static final int Commit                                 = 21;

  /**
   * Rollbacks the current transaction.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply Long transactionId: id of next transaction
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase#rollback(long,
   *      boolean)
   */
  public static final int Rollback                               = 22;

  /**
   * Sets a named savepoint to a transaction given its id
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase#setSavepoint(long,
   *      String)
   */
  public static final int SetNamedSavepoint                      = 23;

  /**
   * Sets a unnamed savepoint to a transaction given its id
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase#setSavepoint(long)
   */
  public static final int SetUnnamedSavepoint                    = 24;

  /**
   * Releases a savepoint from a transaction given its id
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase#releaseSavepoint(long,
   *      String)
   */
  public static final int ReleaseSavepoint                       = 25;

  /**
   * Rollbacks the current transaction back to the given savepoint
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#rollbackToSavepoint()
   */
  public static final int RollbackToSavepoint                    = 26;

  /*
   * Connection management
   */

  /**
   * Close the connection. The controller replies a CommandCompleted
   * CJDBCException, ignored by the driver.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply &lt;anything&gt;
   */
  public static final int Close                                  = 30;

  /**
   * Reset the connection.
   * <p>
   * ~commandcode {@value}
   */
  public static final int Reset                                  = 31;

  /**
   * Fetch next rows of data for ResultSet streaming.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF cursorName <br>
   * ~argument Int fetchSize
   * <p>
   * <br>
   * ~reply ArrayList data <br>
   * ~reply boolean hasMoreData
   */
  public static final int FetchNextResultSetRows                 = 32;

  /**
   * Closes a remote ResultSet that was opened for streaming.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF cursorName
   * <p>
   * <br>
   * ~reply CJDBCException CommandCompleted
   */
  public static final int CloseRemoteResultSet                   = 33;

  /**
   * Restore a connection state after an automatic reconnection. Tell the
   * controller if we were in autoCommit mode (i.e.: a transaction was
   * <em>not</em> started), and if we were not then give the current
   * transactionId. Warning: this is not an optUTF type at all.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument (boolean true) | (boolean false; Long transactionId)
   */
  public static final int RestoreConnectionState                 = 34;

  /**
   * Command to change the autocommit value from false to true. We want to
   * commit the current transaction but we don't want to start a new one.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply boolean true (meaning: not an exception)
   */
  public static final int SetAutoCommit                          = 35;

  /**
   * Retrieve the catalog (database) we are connected to.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply String vdbName
   * 
   * @see org.objectweb.cjdbc.driver.Connection#getCatalog()
   */
  public static final int ConnectionGetCatalog                   = 36;

  /**
   * Retrieve the list of available catalogs (databases).
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply ResultSet virtualDatabasesList
   * 
   * @see org.objectweb.cjdbc.driver.Connection#getCatalogs()
   */
  public static final int ConnectionGetCatalogs                  = 37;

  /**
   * Connect to another catalog/database (as the same user).
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF catalog
   * <p>
   * <br>
   * ~reply boolean isValidCatalog
   * 
   * @see org.objectweb.cjdbc.driver.Connection#setCatalog(String)
   */
  public static final int ConnectionSetCatalog                   = 38;

  /**
   * Set the new transaction isolation level to use for this connection.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument int transaction isolation level
   * <p>
   * <br>
   * ~reply boolean true (meaning: not an exception)
   * 
   * @see org.objectweb.cjdbc.driver.Connection#setTransactionIsolation(int)
   */
  public static final int SetTransactionIsolation                = 39;

  /*
   * MetaData functions
   */

  /**
   * Gets the virtual database name to be used by the client (C-JDBC driver). It
   * currently returns the same result as ConnectionGetCatalog(). It is
   * currently never used by the driver.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply String dbName
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase#getVirtualDatabaseName()
   */
  public static final int GetVirtualDatabaseName                 = 50;

  /**
   * Gets the controller version number.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply String controllerVersion
   * 
   * @see org.objectweb.cjdbc.controller.core.Controller#getVersionNumber()
   */
  public static final int GetControllerVersionNumber             = 51;

  /**
   * Used to get the schema tables by calling DatabaseMetaData.getTables().
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF catalog <br>
   * ~argument UTF schemaPattern <br>
   * ~argument UTF tableNamePattern <br>
   * ~argument String[] types
   * <p>
   * <br>
   * ~reply ResultSet tables
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetTables()
   */
  public static final int DatabaseMetaDataGetTables              = 52;

  /**
   * Used to get the schema columns by calling DatabaseMetaData.getColumns().
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF catalog <br>
   * ~argument UTF schemaPattern <br>
   * ~argument UTF tableNamePattern <br>
   * ~argument UTF columnNamePattern
   * <p>
   * <br>
   * ~reply ResultSet schemaColumns
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetColumns()
   */
  public static final int DatabaseMetaDataGetColumns             = 53;

  /**
   * Used to get the schema primary keys by calling
   * DatabaseMetaData.getColumns().
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF catalog <br>
   * ~argument UTF schemaPattern <br>
   * ~argument UTF tableNamePattern
   * <p>
   * <br>
   * ~reply ResultSet pKeys
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetPrimaryKeys()
   */
  public static final int DatabaseMetaDataGetPrimaryKeys         = 54;

  /**
   * Used to get the schema procedures by calling
   * DatabaseMetaData.getProcedures().
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF catalog <br>
   * ~argument UTF schemaPattern <br>
   * ~argument UTF procedureNamePattern
   * <p>
   * <br>
   * ~reply ResultSet procedures
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetProcedures()
   */
  public static final int DatabaseMetaDataGetProcedures          = 55;

  /**
   * Used to get the schema procedure columns by calling
   * DatabaseMetaData.getProcedureColumns().
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF catalog <br>
   * ~argument UTF schemaPattern <br>
   * ~argument UTF procedureNamePattern <br>
   * ~argument UTF columnNamePattern
   * <p>
   * <br>
   * ~reply ResultSet procColumns
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetProcedureColumns()
   */
  public static final int DatabaseMetaDataGetProcedureColumns    = 56;

  /**
   * Retrieve the database table types.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply ResultSet tableTypes
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetTableTypes()
   */
  public static final int DatabaseMetaDataGetTableTypes          = 58;

  /**
   * Retrieve the table privileges.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF catalog <br>
   * ~argument UTF schemaPattern <br>
   * ~argument UTF tableNamePattern
   * <p>
   * <br>
   * ~reply ResultSet accessRights
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetTablePrivileges()
   */
  public static final int DatabaseMetaDataGetTablePrivileges     = 59;

  /**
   * Retrieve the schemas.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply ResultSet schemas
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetSchemas()
   */
  public static final int DatabaseMetaDataGetSchemas             = 60;

  /**
   * Retrieve the database product name.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~reply String productName
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetDatabaseProductName()
   */
  public static final int DatabaseMetaDataGetDatabaseProductName = 61;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetAttributes()
   */
  public static final int DatabaseMetaDataGetAttributes          = 62;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetBestRowIdentifier()
   */
  public static final int DatabaseMetaDataGetBestRowIdentifier   = 63;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetColumnPrivileges()
   */
  public static final int DatabaseMetaDataGetColumnPrivileges    = 64;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetCrossReference()
   */
  public static final int DatabaseMetaDataGetCrossReference      = 65;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetExportedKeys()
   */
  public static final int DatabaseMetaDataGetExportedKeys        = 66;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetImportedKeys()
   */
  public static final int DatabaseMetaDataGetImportedKeys        = 67;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetIndexInfo()
   */
  public static final int DatabaseMetaDataGetIndexInfo           = 68;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetSuperTables()
   */
  public static final int DatabaseMetaDataGetSuperTables         = 69;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetSuperTypes()
   */
  public static final int DatabaseMetaDataGetSuperTypes          = 70;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetTypeInfo()
   */
  public static final int DatabaseMetaDataGetTypeInfo            = 71;

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseMetaDataGetUDTs()
   */
  public static final int DatabaseMetaDataGetUDTs                = 72;

  /**
   * @see java.sql.DatabaseMetaData#getVersionColumns(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public static final int DatabaseMetaDataGetVersionColumns      = 73;

  /**
   * Retrieve one value from the virtual database metadata.
   * <p>
   * ~commandcode {@value}
   * <p>
   * <br>
   * ~argument UTF: serialized DatabaseMetaData method call.
   * <p>
   * <br>
   * ~reply Integer|Boolean|String|other ? value
   * 
   * @see org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread#databaseStaticMetadata()
   */
  public static final int DatabaseStaticMetadata                 = 80;

}
