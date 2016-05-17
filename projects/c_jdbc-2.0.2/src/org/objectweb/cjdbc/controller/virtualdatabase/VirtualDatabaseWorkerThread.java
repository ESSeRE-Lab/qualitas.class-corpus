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
 * Contributor(s): Nicolas Modrzyk, Jean-Bernard van Zuylen.
 * Refactored by Marc Herbert to remove the use of Java serialization.
 */

package org.objectweb.cjdbc.controller.virtualdatabase;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.NoMoreControllerException;
import org.objectweb.cjdbc.common.exceptions.NotImplementedException;
import org.objectweb.cjdbc.common.exceptions.driver.protocol.BackendDriverException;
import org.objectweb.cjdbc.common.exceptions.driver.protocol.ControllerCoreException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.AlterRequest;
import org.objectweb.cjdbc.common.sql.CreateRequest;
import org.objectweb.cjdbc.common.sql.DeleteRequest;
import org.objectweb.cjdbc.common.sql.DropRequest;
import org.objectweb.cjdbc.common.sql.InsertRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.RequestType;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.UpdateRequest;
import org.objectweb.cjdbc.common.sql.metadata.MetadataContainer;
import org.objectweb.cjdbc.common.sql.metadata.MetadataDescription;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;
import org.objectweb.cjdbc.common.users.VirtualDatabaseUser;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.driver.Connection;
import org.objectweb.cjdbc.driver.DriverResultSet;
import org.objectweb.cjdbc.driver.protocol.Commands;
import org.objectweb.cjdbc.driver.protocol.SQLDataSerialization;
import org.objectweb.cjdbc.driver.protocol.TypeTag;

/**
 * This class handles a connection with a C-JDBC driver.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 2.0
 */
public class VirtualDatabaseWorkerThread extends Thread
{
  //
  // How the code is organized?
  //
  // 1. Member variables
  // 2. Constructor(s)
  // 3. Request management
  // 4. Getter/Setters

  /** <code>true</code> ifthis has been killed. */
  private boolean             isKilled             = false;

  /** Do we require the templates of PreparedStatements? Needed for parsing */
  boolean                     needSkeleton         = false;

  /** Virtual database instantiating this thread. */
  private VirtualDatabase     vdb;

  /** Logger instance. */
  private Trace               logger               = null;

  private CJDBCInputStream    in                   = null;
  private CJDBCOutputStream   out                  = null;

  private VirtualDatabaseUser user;

  private Controller          controller;

  private boolean             waitForCommand;

  private HashMap             streamedResultSet;

  /**
   * The following variables represent the state of the connection with the
   * client
   */
  private long                currentTid;
  private boolean             transactionStarted;
  private boolean             queryExecutedInThisTransaction;
  private boolean             writeQueryExecutedInThisTransaction;
  private String              login;
  private boolean             closed;
  private int                 transactionIsolation = Connection.DEFAULT_TRANSACTION_ISOLATION_LEVEL;

  /*
   * Constructor
   */

  /**
   * Creates a new <code>VirtualDatabaseWorkerThread</code> instance.
   * 
   * @param controller the thread was originated from
   * @param vdb the virtual database instantiating this thread.
   */
  public VirtualDatabaseWorkerThread(Controller controller, VirtualDatabase vdb)
  {
    super("VirtualDatabaseWorkerThread-" + vdb.getVirtualDatabaseName());
    this.vdb = vdb;
    this.controller = controller;
    try
    {
      this.logger = Trace
          .getLogger("org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread."
              + vdb.getVirtualDatabaseName());
    }
    catch (Exception e)
    {
      this.logger = vdb.logger;
    }
  }

  //
  // Decoding commands from the stream
  //

  /**
   * Read a write request send by the <code>Connection</code> object. Only the
   * needed parameters are sent, so we can reconstruct the object on the
   * controller side, without serializing everything.
   * 
   * @return an instance of the <code>AbstractWriteRequest</code>
   * @see AbstractRequest#AbstractRequest(CJDBCInputStream, int)
   */
  private AbstractWriteRequest decodeWriteRequestFromStream()
      throws IOException
  {
    AbstractWriteRequest writeRequest;
    int requestType = in.readInt();

    switch (requestType)
    {
      case RequestType.CREATE :
        writeRequest = new CreateRequest(in);
        break;
      case RequestType.ALTER :
        writeRequest = new AlterRequest(in);
        break;
      case RequestType.DELETE :
        writeRequest = new DeleteRequest(in);
        break;
      case RequestType.DROP :
        writeRequest = new DropRequest(in);
        break;
      case RequestType.INSERT :
        writeRequest = new InsertRequest(in);
        break;
      case RequestType.UPDATE :
        writeRequest = new UpdateRequest(in);
        break;
      default :
        // TODO should this really be an IOException ?
        throw new IOException("Invalid Write Query Type");
    }
    return writeRequest;
  }

  /**
   * Set the login and transaction id on the given request. If the request is
   * autocommit and a transaction was started, the transaction is first commited
   * to return in autocommit mode.
   * 
   * @param request The request to set
   * @param login user login to set
   * @param tid the transaction id to set
   * @return new value of transaction started
   */
  private boolean setRequestParameters(AbstractRequest request, String login,
      long tid, boolean transactionStarted) throws SQLException
  {
    request.setLogin(login);
    request.setTransactionIsolation(transactionIsolation);
    if (request.isAutoCommit() && transactionStarted)
    {
      vdb.commit(tid, writeQueryExecutedInThisTransaction);
      return false;
    }
    else
      request.setTransactionId(tid);
    return transactionStarted;
  }

  /**
   * Keep a reference to both ResultSets, but garbage collect data already sent.
   */
  private void putStreamingReferences(ControllerResultSet crs,
      DriverResultSet drs)
  {
    drs.setData(null);
    crs.driverResultSet = drs;
    streamedResultSet.put(crs.getCursorName(), crs);
  }

  /**
   * Gets a connection from the connection queue and process it.
   */
  public void run()
  {
    ArrayList vdbActiveThreads = vdb.getActiveThreads();
    ArrayList vdbPendingQueue = vdb.getPendingConnections();
    // List of open ResultSets for streaming. This is not synchronized since the
    // connection does only handle one request at a time
    streamedResultSet = new HashMap();
    boolean isActive = true;

    if (vdbActiveThreads == null)
    {
      logger
          .error("Got null active threads queue in VirtualDatabaseWorkerThread");
      isKilled = true;
    }
    if (vdbPendingQueue == null)
    {
      logger.error("Got null connection queue in VirtualDatabaseWorkerThread");
      isKilled = true;
    }

    // Main loop
    while (!isKilled)
    {
      // Get a connection from the pending queue
      synchronized (vdbPendingQueue)
      {
        while (vdbPendingQueue.isEmpty())
        {
          if (!vdb.poolConnectionThreads)
          { // User does not want thread pooling, kill this thread!
            isKilled = true;
            break;
          }
          boolean timeout = false;
          try
          {
            if (isActive)
            {
              isActive = false;
              // Remove ourselves from the active thread list
              synchronized (vdbActiveThreads)
              {
                vdbActiveThreads.remove(this);
                vdb.addIdleThread();
              }
            }
            long before = System.currentTimeMillis();
            vdbPendingQueue.wait(vdb.getMaxThreadIdleTime());
            long now = System.currentTimeMillis();
            // Check if timeout has expired
            timeout = now - before >= vdb.getMaxThreadIdleTime();
          }
          catch (InterruptedException e)
          {
            logger.warn("VirtualDatabaseWorkerThread wait() interrupted");
          }
          if (timeout && vdbPendingQueue.isEmpty())
          {
            if (vdb.currentNbOfThreads > vdb.minNbOfThreads)
            { // We have enough threads, kill this one
              isKilled = true;
              break;
            }
          }
        }

        if (isKilled)
        { // Cleaning up
          synchronized (vdbActiveThreads)
          { // Remove ourselves from the appropriate thread list
            if (isActive)
            {
              vdbActiveThreads.remove(this);
              vdb.removeCurrentNbOfThread();
            }
            else
              vdb.removeIdleThread();
          }
          // Get out of the while loop
          continue;
        }

        // Get a connection
        try
        {
          in = (CJDBCInputStream) vdbPendingQueue.remove(0);
          out = (CJDBCOutputStream) vdbPendingQueue.remove(0);
        }
        catch (Exception e)
        {
          logger.error("Error while getting streams from connection");
          continue;
        }

        synchronized (vdbActiveThreads)
        {
          if (!isActive)
          {
            vdb.removeIdleThread();
            isActive = true;
            // Add this thread to the active thread list
            vdbActiveThreads.add(this);
          }
        }
      }

      // Handle connection
      // Read the user information and check authentication
      /**
       * @see org.objectweb.cjdbc.driver.Driver#connectToController(Properties,
       *      CjdbcUrl, ControllerInfo)
       */
      boolean success = false;
      try
      {
        login = in.readUTF();
        String password = in.readUTF();
        user = new VirtualDatabaseUser(login, password);

        if (vdb.getAuthenticationManager().isValidVirtualUser(user))
        { // Authentication ok
          out.writeBoolean(true); // success code

          this.needSkeleton = vdb.getRequestManager()
              .getRequiredParsingGranularity() != ParsingGranularities.NO_PARSING;
          // tell the driver if we want the templates of PreparedStatements
          out.writeBoolean(this.needSkeleton);
          // tell the driver which blob encoding to use on this controller
          out.writeUTF(vdb.getBlobFilter().getXml());
          out.flush();
          success = true;

          if (logger.isDebugEnabled())
            logger.debug("Login accepted for " + login);
        }
        else
        { // Authentication failed, close the connection
          String msg = "Authentication failed for user '" + login + "'";
          out.writeBoolean(false); // authentication failed
          out.writeUTF(msg); // error message
          if (logger.isDebugEnabled())
            logger.debug(msg);
          continue;
        }
      }
      catch (IOException e)
      {
        logger.error("I/O error during user authentication (" + e + ")");
        continue;
      }
      finally
      {
        if (!success)
        {
          try
          {
            out.close();
            in.close();
          }
          catch (IOException ignore)
          {
          }
        }
      }

      currentTid = 0;
      transactionStarted = false;
      queryExecutedInThisTransaction = false;
      writeQueryExecutedInThisTransaction = false;
      closed = false;
      int command;
      while (!closed && !isKilled)
      {
        try
        {
          // Get the query
          waitForCommand = true;
          command = in.readInt();
          waitForCommand = false;

          // Process it
          switch (command)
          {
            case Commands.ExecReadRequest :
              execReadRequest();
              break;
            case Commands.ExecWriteRequest :
              execWriteRequest();
              break;
            case Commands.ExecWriteRequestWithKeys :
              execWriteRequestWithKeys();
              break;
            case Commands.ExecReadStoredProcedure :
              execReadStoredProcedure();
              break;
            case Commands.ExecWriteStoredProcedure :
              execWriteStoredProcedure();
              break;
            case Commands.Begin :
              begin();
              break;
            case Commands.Commit :
              commit();
              break;
            case Commands.SetAutoCommit :
              setAutoCommit();
              break;
            case Commands.Rollback :
              rollback();
              break;
            case Commands.SetNamedSavepoint :
              setNamedSavepoint();
              break;
            case Commands.SetUnnamedSavepoint :
              setUnnamedSavepoint();
              break;
            case Commands.ReleaseSavepoint :
              releaseSavepoint();
              break;
            case Commands.RollbackToSavepoint :
              rollbackToSavepoint();
              break;
            case Commands.SetTransactionIsolation :
              connectionSetTransactionIsolation();
              break;
            case Commands.GetVirtualDatabaseName :
              getVirtualDatabaseName();
              break;
            case Commands.DatabaseMetaDataGetDatabaseProductName :
              databaseMetaDataGetDatabaseProductName();
              break;
            case Commands.GetControllerVersionNumber :
              getControllerVersionNumber();
              break;
            case Commands.DatabaseMetaDataGetTables :
              databaseMetaDataGetTables();
              break;
            case Commands.DatabaseMetaDataGetColumns :
              databaseMetaDataGetColumns();
              break;
            case Commands.DatabaseMetaDataGetPrimaryKeys :
              databaseMetaDataGetPrimaryKeys();
              break;
            case Commands.DatabaseMetaDataGetProcedures :
              databaseMetaDataGetProcedures();
              break;
            case Commands.DatabaseMetaDataGetProcedureColumns :
              databaseMetaDataGetProcedureColumns();
              break;
            case Commands.ConnectionGetCatalogs :
              connectionGetCatalogs();
              break;
            case Commands.ConnectionGetCatalog :
              connectionGetCatalog();
              break;
            case Commands.DatabaseMetaDataGetTableTypes :
              databaseMetaDataGetTableTypes();
              break;
            case Commands.DatabaseMetaDataGetSchemas :
              databaseMetaDataGetSchemas();
              break;
            case Commands.DatabaseMetaDataGetTablePrivileges :
              databaseMetaDataGetTablePrivileges();
              break;
            case Commands.DatabaseMetaDataGetAttributes :
              databaseMetaDataGetAttributes();
              break;
            case Commands.DatabaseMetaDataGetBestRowIdentifier :
              databaseMetaDataGetBestRowIdentifier();
              break;
            case Commands.DatabaseMetaDataGetColumnPrivileges :
              databaseMetaDataGetColumnPrivileges();
              break;
            case Commands.DatabaseMetaDataGetCrossReference :
              databaseMetaDataGetCrossReference();
              break;
            case Commands.DatabaseMetaDataGetExportedKeys :
              databaseMetaDataGetExportedKeys();
              break;
            case Commands.DatabaseMetaDataGetImportedKeys :
              databaseMetaDataGetImportedKeys();
              break;
            case Commands.DatabaseMetaDataGetIndexInfo :
              databaseMetaDataGetIndexInfo();
              break;
            case Commands.DatabaseMetaDataGetSuperTables :
              databaseMetaDataGetSuperTables();
              break;
            case Commands.DatabaseMetaDataGetSuperTypes :
              databaseMetaDataGetSuperTypes();
              break;
            case Commands.DatabaseMetaDataGetTypeInfo :
              databaseMetaDataGetTypeInfo();
              break;
            case Commands.DatabaseMetaDataGetUDTs :
              databaseMetaDataGetUDTs();
              break;
            case Commands.DatabaseMetaDataGetVersionColumns :
              databaseMetaDataGetVersionColumns();
              break;
            case Commands.ConnectionSetCatalog :
              connectionSetCatalog();
              break;
            case Commands.Close :
              close();
              break;
            case Commands.Reset :
              reset();
              break;
            case Commands.FetchNextResultSetRows :
              fetchNextResultSetRows();
              break;
            case Commands.CloseRemoteResultSet :
              closeRemoteResultSet();
              break;
            case Commands.DatabaseStaticMetadata :
              databaseStaticMetadata();
              break;
            case Commands.RestoreConnectionState :
              restoreConnectionState();
              break;
            default :
              String errorMsg = "Unsupported protocol command: " + command;
              logger.error(errorMsg);
              sendToDriver(new RuntimeException(errorMsg));
              out.flush();
              break;
          }
        }
        catch (EOFException e)
        {
          logger.warn("Client (login:" + login + ",host:"
              + in.getSocket().getInetAddress().getHostName()
              + " closed connection with server");
          closed = true;
        }
        catch (SocketException e)
        {
          // shutting down
          closed = true;
        }
        catch (IOException e)
        {
          closed = true;
          logger.warn("Closing connection with client " + login
              + " because of IOException.(" + e + ")");
        }
        catch (SQLException e)
        {
          logger
              .warn("Error during command execution (" + e.getMessage() + ")");
          try
          {
            sendToDriver(e);
            out.flush();
          }
          catch (IOException ignore)
          {
          }
        }
        catch (RuntimeException e)
        {
          logger.warn("Runtime error during command execution ("
              + e.getMessage() + ")", e);
          try
          {
            sendToDriver(new SQLException(e.getMessage()));
            out.flush();
          }
          catch (IOException ignore)
          {
          }
        }
      } // while (!closed && !isKilled) get and process command from driver

      // Do the cleanup
      if (transactionStarted)
      {
        if (logger.isDebugEnabled())
          logger.debug("Forcing transaction " + currentTid + " rollback");
        try
        {
          vdb.rollback(currentTid, writeQueryExecutedInThisTransaction);
        }
        catch (Exception e)
        {
          if (logger.isDebugEnabled())
            logger.debug("Error during rollback of transaction " + currentTid
                + "(" + e + ")");
        }
      }
      if (!streamedResultSet.isEmpty())
      {
        for (Iterator iter = streamedResultSet.values().iterator(); iter
            .hasNext();)
        {
          ControllerResultSet crs = (ControllerResultSet) iter.next();
          crs.closeResultSet();
        }
        streamedResultSet.clear();
      }
      try
      {
        in.close();
      }
      catch (IOException ignore)
      {
      }
      try
      {
        out.close();
      }
      catch (IOException ignore)
      {
      }
    }

    if (logger.isDebugEnabled())
      logger.debug("VirtualDatabaseWorkerThread associated to login: "
          + this.getUser() + " terminating.");
  }

  //
  // Connection management
  //

  private void close() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("Close command");
    sendToDriver(true);

    closed = true;
  }

  private void closeRemoteResultSet() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("CloseRemoteResultSet command");

    String cursor = in.readUTF();
    ControllerResultSet crsToClose = (ControllerResultSet) streamedResultSet
        .remove(cursor);
    if (crsToClose == null)
    {
      sendToDriver(new SQLException("No valid RemoteResultSet to close."));
    }
    else
    {
      crsToClose.closeResultSet();
      sendToDriver(true);

    }
  }

  private void reset()
  {
    // The client application has closed the connection but it is kept
    // open in case the transparent connection pooling reuses it.
    if (logger.isDebugEnabled())
      logger.debug("Reset command");

    // Do the cleanup
    if (transactionStarted)
    {
      if (queryExecutedInThisTransaction)
      { // Force rollback of this transaction
        if (logger.isDebugEnabled())
          logger.debug("Forcing transaction " + currentTid + " rollback");
        try
        {
          vdb.rollback(currentTid, writeQueryExecutedInThisTransaction);
        }
        catch (Exception e)
        {
          if (logger.isDebugEnabled())
            logger.debug("Error during rollback of transaction " + currentTid
                + "(" + e + ")");
        }
      }
      else
      { // We need to abort the begin to cleanup the metadata
        // associated with the started transaction.
        if (logger.isDebugEnabled())
          logger.debug("Aborting transaction " + currentTid);
        try
        {
          vdb.abort(currentTid, writeQueryExecutedInThisTransaction);
        }
        catch (Exception e)
        {
          if (logger.isDebugEnabled())
            logger.debug("Error while aborting transaction " + currentTid + "("
                + e + ")", e);
        }
      }
      currentTid = 0;
      transactionStarted = false;
    }
  }

  private void restoreConnectionState() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("RestoreConnectionState command");
    // We receive autocommit from driver
    transactionStarted = !in.readBoolean();
    if (transactionStarted)
      currentTid = in.readLong();
  }

  //
  // Catalog
  //

  private void connectionSetCatalog() throws IOException
  {
    // Warning! This could bypass the security checkings based on client IP
    // address. If a user has access to a virtual database, through setCatalog()
    // is will be able to access all other virtual databases where his
    // login/password is valid regardless of the IP filtering settings.
    if (logger.isDebugEnabled())
      logger.debug("ConnectionSetCatalog command");
    String catalog = in.readUTF();
    boolean change = controller.hasVirtualDatabase(catalog);
    if (change)
    {
      VirtualDatabase tempvdb = controller.getVirtualDatabase(catalog);
      if (!tempvdb.getAuthenticationManager().isValidVirtualUser(user))
        sendToDriver(new SQLException(
            "User authentication has failed for asked catalog. No change"));
      else
      {
        this.vdb = tempvdb;
        sendToDriver(true);
      }
    }
    else
      sendToDriver(false);

  }

  private void connectionGetCatalog() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("ConnectionGetCatalog command");

    sendToDriver(vdb.getVirtualDatabaseName());
  }

  private void connectionGetCatalogs() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("ConnectionGetCatalogs command");
    ArrayList list = controller.getVirtualDatabaseNames();
    sendToDriver(vdb.getDynamicMetaData().getCatalogs(list));
  }

  private void connectionSetTransactionIsolation() throws IOException
  {
    int level = in.readInt();
    if (logger.isDebugEnabled())
      logger.debug("SetTransactionIsolation command (level=" + level + ")");

    // Check that we are not in a running transaction
    if (transactionStarted && queryExecutedInThisTransaction)
    {
      sendToDriver(new SQLException(
          "Cannot change the transaction isolation in a running transaction"));
      out.flush();
      return;
    }

    MetadataContainer metadataContainer = vdb.getStaticMetaData()
        .getMetadataContainer();
    if (metadataContainer != null)
    {
      Object value = metadataContainer.get(MetadataContainer.getContainerKey(
          MetadataDescription.SUPPORTS_TRANSACTION_ISOLATION_LEVEL,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(level)}));

      if (value != null)
      {
        if (!((Boolean) value).booleanValue())
        {
          sendToDriver(new SQLException("Transaction isolation level " + level
              + " is not supported by the database"));
          out.flush();
          return;
        }
      }
      else
        logger.warn("Unable to check validity of transaction isolation level "
            + level);
    }
    else
      logger.warn("Unable to check validity of transaction isolation level "
          + level);
    transactionIsolation = level;
    sendToDriver(true);
  }

  //
  // Database MetaData
  //

  /**
   * @see java.sql.DatabaseMetaData#getAttributes(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetAttributes() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetAttributes command");
    String catalog = in.readUTF();
    String schemaPattern = in.readUTF();
    String typeNamePattern = in.readUTF();
    String attributeNamePattern = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getAttributes(login, catalog,
          schemaPattern, typeNamePattern, attributeNamePattern));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetAttributes", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getBestRowIdentifier(java.lang.String,
   *      java.lang.String, java.lang.String, int, boolean)
   */
  private void databaseMetaDataGetBestRowIdentifier() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetBestRowIdentifier command");

    String catalog = in.readUTF();
    String schema = in.readUTF();
    String table = in.readUTF();
    int scope = in.readInt();
    boolean nullable = in.readBoolean();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getBestRowIdentifier(login,
          catalog, schema, table, scope, nullable));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetBestRowIdentifier",
            e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getColumnPrivileges(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetColumnPrivileges() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetColumnPrivileges command");

    String catalog = in.readUTF();
    String schema = in.readUTF();
    String table = in.readUTF();
    String columnNamePattern = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getColumnPrivileges(login, catalog,
          schema, table, columnNamePattern));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetColumnPrivileges",
            e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getColumns(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetColumns() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetColumns command");
    String ccatalog = in.readUTF();
    String cschemaPattern = in.readUTF();
    String ctableNamePattern = in.readUTF();
    String ccolumnNamePattern = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getColumns(login, ccatalog,
          cschemaPattern, ctableNamePattern, ccolumnNamePattern));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetColumns", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getCrossReference(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetCrossReference() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetCrossReference command");

    String primaryCatalog = in.readUTF();
    String primarySchema = in.readUTF();
    String primaryTable = in.readUTF();
    String foreignCatalog = in.readUTF();
    String foreignSchema = in.readUTF();
    String foreignTable = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getCrossReference(login,
          primaryCatalog, primarySchema, primaryTable, foreignCatalog,
          foreignSchema, foreignTable));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetCrossReference", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getDatabaseProductName()
   */
  private void databaseMetaDataGetDatabaseProductName() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("GetDatabaseProductName command");

    sendToDriver(vdb.getDatabaseProductName());
  }

  /**
   * @see java.sql.DatabaseMetaData#getExportedKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetExportedKeys() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetExportedKeys command");

    String catalog = in.readUTF();
    String schema = in.readUTF();
    String table = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getExportedKeys(login, catalog,
          schema, table));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetExportedKeys", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getImportedKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetImportedKeys() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetImportedKeys command");

    String catalog = in.readUTF();
    String schema = in.readUTF();
    String table = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getImportedKeys(login, catalog,
          schema, table));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetImportedKeys", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getIndexInfo(java.lang.String,
   *      java.lang.String, java.lang.String, boolean, boolean)
   */
  private void databaseMetaDataGetIndexInfo() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("databaseMetaDataGetIndexInfo command");

    String catalog = in.readUTF();
    String schema = in.readUTF();
    String table = in.readUTF();
    boolean unique = in.readBoolean();
    boolean approximate = in.readBoolean();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getIndexInfo(login, catalog,
          schema, table, unique, approximate));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetIndexInfo", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getPrimaryKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetPrimaryKeys() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetPrimaryKeys command");

    String pcatalog = in.readUTF();
    String pschemaPattern = in.readUTF();
    String ptableNamePattern = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getPrimaryKeys(login, pcatalog,
          pschemaPattern, ptableNamePattern));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetPrimaryKeys", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getProcedureColumns(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetProcedureColumns() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetProcedureColumns command");

    String pccatalog = in.readUTF();
    String pcschemaPattern = in.readUTF();
    String pcprocedureNamePattern = in.readUTF();
    String pccolumnNamePattern = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getProcedureColumns(login,
          pccatalog, pcschemaPattern, pcprocedureNamePattern,
          pccolumnNamePattern));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetProcedureColumns",
            e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getProcedures(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetProcedures() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetProcedures command");

    String rcatalog = in.readUTF();
    String rschemaPattern = in.readUTF();
    String procedureNamePattern = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getProcedures(login, rcatalog,
          rschemaPattern, procedureNamePattern));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetProcedures", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getSchemas()
   */
  private void databaseMetaDataGetSchemas() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetSchemas Types command");

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getSchemas(login));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetSchemas", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getSuperTables(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetSuperTables() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetSuperTables command");

    String catalog = in.readUTF();
    String schemaPattern = in.readUTF();
    String tableNamePattern = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getSuperTables(login, catalog,
          schemaPattern, tableNamePattern));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetSuperTables", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getSuperTypes(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetSuperTypes() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetSuperTables command");

    String catalog = in.readUTF();
    String schemaPattern = in.readUTF();
    String tableNamePattern = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getSuperTypes(login, catalog,
          schemaPattern, tableNamePattern));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetSuperTypes", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getTablePrivileges(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetTablePrivileges() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetTablePrivileges command");

    String tpcatalog = in.readUTF();
    String tpschemaPattern = in.readUTF();
    String tptablePattern = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getTablePrivileges(login,
          tpcatalog, tpschemaPattern, tptablePattern));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger
            .warn("Error while calling databaseMetaDataGetTablePrivileges", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getTables(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String[])
   */
  private void databaseMetaDataGetTables() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetTables command");

    String tcatalog = in.readUTF();
    String tschemaPattern = in.readUTF();
    String ttableNamePattern = in.readUTF();

    String[] ttypes = null;
    if (in.readBoolean())
    {
      int size = in.readInt();
      ttypes = new String[size];
      for (int i = 0; i < size; i++)
        ttypes[i] = in.readUTF();
    }

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getTables(login, tcatalog,
          tschemaPattern, ttableNamePattern, ttypes));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetTables", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getTableTypes()
   */
  private void databaseMetaDataGetTableTypes() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetTableTypes command");

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getTableTypes(login));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetTableTypes", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getTypeInfo()
   */
  private void databaseMetaDataGetTypeInfo() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetTypeInfo command");

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getTypeInfo(login));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetTypeInfo", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getUDTs(java.lang.String, java.lang.String,
   *      java.lang.String, int[])
   */
  private void databaseMetaDataGetUDTs() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetUDTs command");

    String catalog = in.readUTF();
    String schemaPattern = in.readUTF();
    String tableNamePattern = in.readUTF();

    int[] types = null;
    if (in.readBoolean())
    {
      int size = in.readInt();
      types = new int[size];
      for (int i = 0; i < size; i++)
        types[i] = in.readInt();
    }

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getUDTs(login, catalog,
          schemaPattern, tableNamePattern, types));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetUDTs", e);
      sendToDriver(e);
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getVersionColumns(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private void databaseMetaDataGetVersionColumns() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("DatabaseMetaDataGetVersionColumns command");

    String catalog = in.readUTF();
    String schema = in.readUTF();
    String table = in.readUTF();

    try
    {
      sendToDriver(vdb.getDynamicMetaData().getVersionColumns(login, catalog,
          schema, table));
    }
    catch (SQLException e)
    {
      if (logger.isWarnEnabled())
        logger.warn("Error while calling databaseMetaDataGetVersionColumns", e);
      sendToDriver(e);
    }
  }

  /**
   * Get the static metadata key from the socket and return the corresponding
   * metadata.
   * 
   * @throws IOException if an IO error occurs
   * @throws NotImplementedException if the underlying metadata access method is
   *           not implemented
   */
  private void databaseStaticMetadata() throws IOException,
      NotImplementedException
  {
    // the "getXXX(Y,Z,...)" hash key of the metadata
    // query called by the client using the driver.
    String key = in.readUTF();
    if (logger.isDebugEnabled())
      logger.debug("DatabaseStaticMetadata command for " + key);
    MetadataContainer container = vdb.getStaticMetaData()
        .getMetadataContainer();
    if (container == null) // no metadata has been gathered yet from backends
    {
      String msg = "No metadata is available probably because no backend is enabled on that controller.";
      logger.info(msg);
      sendToDriver(new SQLException(msg));
    }
    else
    {
      /**
       * To get an exhaustive list of all the types of java objects stored in
       * this hash table, search for all callers of
       * {@link org.objectweb.cjdbc.driver.DatabaseMetaData#getMetadata(String, Class[], Object[], boolean)}
       * and see also
       * {@link org.objectweb.cjdbc.controller.backend.DatabaseBackendMetaData#retrieveDatabaseMetadata()}
       * At this time it's limited to the following types: String, int and
       * boolean. boolean is the most frequent.
       */
      /*
       * Since we don't expect that any of these metadata methods will ever
       * return a non- java.sql.Types, we re-use here the serialization
       * implemented for SQL Data/ResultSets elements.
       */

      SQLDataSerialization.Serializer serializer;
      Object result = container.get(key);

      try
      {
        serializer = SQLDataSerialization.getSerializer(result);
      }
      catch (NotImplementedException innerEx)
      { // Should we just print a warning in case result == null ?
        // This should never happen with decent drivers.
        String msg;
        if (null == result)
          msg = " returned a null object.";
        else
          msg = " returned an object of an unsupported java type:"
              + result.getClass().getName() + ".";

        NotImplementedException outerEx = new NotImplementedException(
            "Backend driver method " + key + msg);
        outerEx.initCause(innerEx);
        throw outerEx;
      }

      TypeTag.NOT_EXCEPTION.sendToStream(out);
      serializer.getTypeTag().sendToStream(out);
      serializer.sendToStream(result, out);
    }

    out.flush();
  }

  private void getControllerVersionNumber() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("GetControllerVersionNumber command");

    sendToDriver(Constants.VERSION);
  }

  private void getVirtualDatabaseName() throws IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("GetVirtualDatabaseName command");

    sendToDriver(vdb.getDatabaseName());
  }

  //
  // Transaction management
  //

  private void begin() throws SQLException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("Begin command");

    currentTid = vdb.begin(login);
    sendToDriver(currentTid);

    transactionStarted = true;
    queryExecutedInThisTransaction = false;
    writeQueryExecutedInThisTransaction = false;
  }

  private void commit() throws SQLException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("Commit command");

    vdb.commit(currentTid, writeQueryExecutedInThisTransaction);
    currentTid = vdb.begin(login);
    sendToDriver(currentTid);

    queryExecutedInThisTransaction = false;
    writeQueryExecutedInThisTransaction = false;
  }

  private void rollback() throws SQLException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("Rollback command");

    vdb.rollback(currentTid, writeQueryExecutedInThisTransaction);
    currentTid = vdb.begin(login);
    sendToDriver(currentTid);

    queryExecutedInThisTransaction = false;
    writeQueryExecutedInThisTransaction = false;
  }

  private void setAutoCommit() throws SQLException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("Set Auto commit command");

    vdb.commit(currentTid, writeQueryExecutedInThisTransaction);
    currentTid = 0;
    transactionStarted = false;
    queryExecutedInThisTransaction = false;
    writeQueryExecutedInThisTransaction = false;
    sendToDriver(true);
  }

  private void setNamedSavepoint() throws SQLException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("Set named savepoint command");

    String savepointName = in.readUTF();
    vdb.setSavepoint(currentTid, savepointName);
    sendToDriver(true);
  }

  private void setUnnamedSavepoint() throws SQLException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("Set unnamed savepoint command");

    int savepointId = vdb.setSavepoint(currentTid);
    sendToDriver(savepointId);
  }

  private void releaseSavepoint() throws SQLException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("Release savepoint command");

    String savepointName = in.readUTF();
    vdb.releaseSavepoint(currentTid, savepointName);
    sendToDriver(true);
  }

  private void rollbackToSavepoint() throws SQLException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("Rollback to savepoint command");
    String savepointName = in.readUTF();
    vdb.rollback(currentTid, savepointName);
    sendToDriver(true);
  }

  //
  // Request execution
  //

  private void execReadRequest() throws IOException, SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug("ExecReadRequest command");
    SelectRequest select = new SelectRequest(in);
    transactionStarted = setRequestParameters(select, login, currentTid,
        transactionStarted);
    if (!transactionStarted)
      currentTid = 0;
    else
      queryExecutedInThisTransaction = true;

    // send the resultset
    ControllerResultSet crs = vdb.execReadRequest(select);
    DriverResultSet drs = new DriverResultSet(crs.getFields(), crs.getData(),
        crs.hasMoreData(), crs.getCursorName());
    sendToDriver(drs);

    // streaming
    if (crs.hasMoreData())
      putStreamingReferences(crs, drs);

  }

  // TODO : try to share code with execWriteStoredProcedure below
  private void execReadStoredProcedure() throws IOException, SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug("ExecReadStoredProcedure command");
    StoredProcedure readProc = new StoredProcedure(in);
    transactionStarted = setRequestParameters(readProc, login, currentTid,
        transactionStarted);
    if (!transactionStarted)
      currentTid = 0;
    else
    { // Transaction not started, check if we should do a lazy start
      queryExecutedInThisTransaction = true;
      if (!(writeQueryExecutedInThisTransaction || readProc.isReadOnly()))
      {
        vdb.getRequestManager().logLazyTransactionBegin(currentTid);
        writeQueryExecutedInThisTransaction = true;
      }
    }
    ControllerResultSet sprs = vdb.execReadStoredProcedure(readProc);
    DriverResultSet drs = new DriverResultSet(sprs.getFields(), sprs.getData(),
        sprs.hasMoreData(), sprs.getCursorName());

    sendToDriver(drs);

    if (sprs.hasMoreData())
      putStreamingReferences(sprs, drs);

  }

  private void execWriteRequest() throws IOException, SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug("ExecWriteRequest command");
    AbstractWriteRequest write = decodeWriteRequestFromStream();
    transactionStarted = setRequestParameters(write, login, currentTid,
        transactionStarted);
    if (!transactionStarted)
      currentTid = 0;
    else
    { // Transaction not started, check if we should do a lazy start
      queryExecutedInThisTransaction = true;
      if (!writeQueryExecutedInThisTransaction)
      {
        vdb.getRequestManager().logLazyTransactionBegin(currentTid);
        writeQueryExecutedInThisTransaction = true;
      }
    }
    sendToDriver(vdb.execWriteRequest(write));
  }

  /**
   * execWriteRequestWithKeys() will probably have soon no difference with
   * execWriteRequest() anymore; then we can get rid of it.
   */
  private void execWriteRequestWithKeys() throws IOException, SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug("ExecWriteRequestWithKeys command");
    AbstractWriteRequest writeWithKeys = decodeWriteRequestFromStream();
    transactionStarted = setRequestParameters(writeWithKeys, login, currentTid,
        transactionStarted);
    if (!transactionStarted)
      currentTid = 0;
    else
    { // Transaction not started, check if we should do a lazy start
      queryExecutedInThisTransaction = true;
      if (!writeQueryExecutedInThisTransaction)
      {
        vdb.getRequestManager().logLazyTransactionBegin(currentTid);
        writeQueryExecutedInThisTransaction = true;
      }
    }
    ControllerResultSet keys = vdb.execWriteRequestWithKeys(writeWithKeys);
    // currently UNTESTED (which backend does support auto-generated keys ?)
    DriverResultSet drs = new DriverResultSet(keys.getFields(), keys.getData(),
        keys.hasMoreData(), keys.getCursorName());

    sendToDriver(drs);

    if (keys.hasMoreData())
      putStreamingReferences(keys, drs);

  }

  private void execWriteStoredProcedure() throws IOException, SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug("ExecWriteStoredProcedure command");
    StoredProcedure writeProc = new StoredProcedure(in);
    transactionStarted = setRequestParameters(writeProc, login, currentTid,
        transactionStarted);
    if (!transactionStarted)
      currentTid = 0;
    else
    {
      queryExecutedInThisTransaction = true;
      if (!writeQueryExecutedInThisTransaction)
      {
        vdb.getRequestManager().logLazyTransactionBegin(currentTid);
        writeQueryExecutedInThisTransaction = true;
      }
    }
    sendToDriver(vdb.execWriteStoredProcedure(writeProc));
  }

  /**
   * Serialize a DriverResultSet answer, prefixed with the appropriate TypeTag
   * 
   * @param drs the resultset to send
   * @throws IOException stream error
   */
  private void sendToDriver(DriverResultSet drs) throws IOException
  {

    if (null == drs) // should not happen with well-behaved drivers
    {
      TypeTag.NULL_RESULTSET.sendToStream(out);
      out.flush();
    }
    else
    {
      TypeTag.RESULTSET.sendToStream(out);
      drs.sendToStream(out);
    }
  }

  /**
   * Send a protocol String, prefixed with the appropriate TypeTag
   */
  private void sendToDriver(String str) throws IOException
  {
    TypeTag.NOT_EXCEPTION.sendToStream(out);
    out.writeUTF(str);
    out.flush();
  }

  /**
   * Send a protocol boolean, prefixed with the appropriate TypeTag
   */
  private void sendToDriver(boolean b) throws IOException
  {
    TypeTag.NOT_EXCEPTION.sendToStream(out);
    out.writeBoolean(b);
    out.flush();
  }

  /**
   * Send a protocol int, prefixed with the appropriate TypeTag
   */
  private void sendToDriver(int i) throws IOException
  {
    TypeTag.NOT_EXCEPTION.sendToStream(out);
    out.writeInt(i);
    out.flush();
  }

  /**
   * Send a protocol long, prefixed with the appropriate TypeTag
   */
  private void sendToDriver(long l) throws IOException
  {
    TypeTag.NOT_EXCEPTION.sendToStream(out);
    out.writeLong(l);
    out.flush();
  }

  private void sendToDriver(Exception e) throws IOException
  {
    TypeTag.EXCEPTION.sendToStream(out);
    // This is the place where we convert Exceptions to something
    // serializable and that the driver can understand
    // So this is the place where it's possible to trap all unknown exceptions

    if (e instanceof SQLException)
    { // we assume that an SQLexception comes from the backend

      // since this is currently false because some ControllerCoreExceptions
      // subclass SQLException, here are a few workarounds
      if (e instanceof NoMoreBackendException
          || e instanceof NoMoreControllerException
          || e instanceof NotImplementedException)
      {
        TypeTag.CORE_EXCEPTION.sendToStream(out);
        new ControllerCoreException(e).sendToStream(out);
        return;
      }

      // non-workaround, regular SQLException from backend
      TypeTag.BACKEND_EXCEPTION.sendToStream(out);
      new BackendDriverException(e).sendToStream(out);
      return;
    }

    // else we assume this is an exception from the core (currently...?)
    TypeTag.CORE_EXCEPTION.sendToStream(out);
    new ControllerCoreException(e).sendToStream(out);
    return;

  }

  /**
   * Implements streaming: send the next ResultSet chunk to driver, pulling it
   * from ControllerResultSet. The driver decides of the chunk size at each
   * call. Note that virtualdatabase streaming is independent from backend
   * streaming (which may not be supported). They even could be configured with
   * two different fetchSize -s (it's not currently the case).
   * 
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#executeSelectRequestOnBackend(SelectRequest,
   *      org.objectweb.cjdbc.controller.backend.DatabaseBackend, Connection,
   *      org.objectweb.cjdbc.controller.cache.metadata.MetadataCache)
   * @see ControllerResultSet#fetchData(int)
   */
  private void fetchNextResultSetRows() throws IOException, SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug("FetchNextResultSetRows command");

    String cursorName = in.readUTF();
    int fetchSize = in.readInt();
    ControllerResultSet fetchCrs = (ControllerResultSet) streamedResultSet
        .get(cursorName);
    if (fetchCrs == null)
    {
      sendToDriver(new SQLException(
          "No valid ControllerResultSet to fetch data from"));
      out.flush();
    }
    else
    {
      // refresh driverResultSet with a new chunk of rows
      DriverResultSet drs = fetchCrs.driverResultSet;
      fetchCrs.fetchData(fetchSize);
      drs.setData(fetchCrs.getData());
      // at this point, we could probably do some kind of crs.setData(null)
      // as a memory optimization, but in doubt we leave it as is.
      drs.setHasMoreData(fetchCrs.hasMoreData());

      // send it
      TypeTag.NOT_EXCEPTION.sendToStream(out);
      drs.sendRowsToStream(out);

      // garbage collect sent data
      drs.setData(null);

      if (!fetchCrs.hasMoreData())
        streamedResultSet.remove(cursorName);
    }
  }

  //
  // Public API
  //

  /**
   * Retrieve general information on this client
   * 
   * @return an array of string
   */
  public String[] retrieveClientData()
  {
    String[] data = new String[4];
    data[0] = in.getSocket().getInetAddress().getHostName();
    data[1] = in.getSocket().getInetAddress().getHostAddress();
    data[2] = String
        .valueOf(((System.currentTimeMillis() - in.getDateCreated()) / 1000));
    return data;
  }

  /**
   * Get time active
   * 
   * @return time active since started
   */
  public long getTimeActive()
  {
    return ((System.currentTimeMillis() - in.getDateCreated()) / 1000);
  }

  /**
   * @return Returns the login of the current user.
   */
  public String getUser()
  {
    return user.getLogin();
  }

  /**
   * Shutdown this thread by setting <code>isKilled</code> value to true. This
   * gives time to check for needed rollback transactions
   */
  public void shutdown()
  {
    // Tell this thread to stop working gently.
    // This will cancel transaction if needed
    this.isKilled = true;
    try
    {
      if (waitForCommand)
      {
        // close only the streams if we're not in the middle of a request
        in.close();
        out.close();
      }
    }
    catch (IOException e)
    {
      // ignore, only the input stream should be close
      // for this thread to end
    }
  }

}