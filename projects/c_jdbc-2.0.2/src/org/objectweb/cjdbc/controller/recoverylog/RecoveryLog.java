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
 * Contributor(s): Julie Marguerite, Greg Ward, Jess Sightler, Jean-Bernard van Zuylen, Charles Cordingley.
 */

package org.objectweb.cjdbc.controller.recoverylog;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.exceptions.VirtualDatabaseException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.shared.BackendState;
import org.objectweb.cjdbc.common.shared.DumpInfo;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.AlterRequest;
import org.objectweb.cjdbc.common.sql.CreateRequest;
import org.objectweb.cjdbc.common.sql.DeleteRequest;
import org.objectweb.cjdbc.common.sql.DropRequest;
import org.objectweb.cjdbc.common.sql.InsertRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.UpdateRequest;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;
import org.objectweb.cjdbc.controller.connection.DriverManager;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.BeginTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.CommitTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.ReadStoredProcedureTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.ReleaseSavepointTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.RollbackTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.RollbackToSavepointTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.SavepointTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.WriteRequestTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.WriteStoredProcedureTask;
import org.objectweb.cjdbc.controller.recoverylog.events.LogEntry;
import org.objectweb.cjdbc.controller.recoverylog.events.LogRequestEvent;
import org.objectweb.cjdbc.controller.recoverylog.events.LogRollbackEvent;
import org.objectweb.cjdbc.controller.recoverylog.events.ResetLogEvent;
import org.objectweb.cjdbc.controller.recoverylog.events.StoreDumpCheckpointEvent;
import org.objectweb.cjdbc.controller.recoverylog.events.UnlogRequestEvent;
import org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData;

/**
 * Recovery Log using a database accessed through JDBC.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>*
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class RecoveryLog implements XmlComponent
{
  static Trace         logger                                           = Trace
                                                                            .getLogger("org.objectweb.cjdbc.controller.recoverylog");

  /** Number of backends currently recovering */
  private long         recoveringNb                                     = 0;

  /** Size of the pendingRecoveryTasks queue used by the recover thread */
  protected int        recoveryBatchSize;

  /** Driver class name. */
  private String       driverClassName;

  /** Driver name. */
  private String       driverName;

  /** Driver URL. */
  private String       url;

  /** User's login. */
  private String       login;

  /** User's password. */
  private String       password;

  /** Database connection */
  private Connection   internalConnectionManagedByGetDatabaseConnection = null;

  /**
   * Recovery log table options.
   * 
   * @see #setLogTableCreateStatement(String, String, String, String, String,
   *      String, String, String)
   */
  private String       logTableCreateTable;
  private String       logTableName;
  private String       logTableCreateStatement;
  private String       logTableIdType;
  private String       logTableVloginType;
  private String       logTableSqlColumnName;
  private String       logTableSqlType;
  private String       logTableTransactionIdType;
  private String       logTableExtraStatement;

  /**
   * Checkpoint table options.
   * 
   * @see #setCheckpointTableCreateStatement(String, String, String, String,
   *      String)
   */
  private String       checkpointTableCreateTable;
  private String       checkpointTableName;
  private String       checkpointTableCreateStatement;
  private String       checkpointTableNameType;
  private String       checkpointTableRequestIdType;
  private String       checkpointTableExtraStatement;

  /**
   * Backend table options
   * 
   * @see #setBackendTableCreateStatement(String, String, String, String,
   *      String, String, String)
   */
  private String       backendTableCreateStatement;
  private String       backendTableName;
  private String       backendTableCreateTable;
  private String       backendTableDatabaseName;
  private String       backendTableExtraStatement;
  private String       backendTableCheckpointName;
  private String       backendTableBackendState;
  private String       backendTableBackendName;

  /**
   * Dump table options
   * 
   * @see #setDumpTableCreateStatement(String, String, String, String, String,
   *      String, String, String, String, String)
   */
  private String       dumpTableCreateStatement;
  private String       dumpTableCreateTable;
  private String       dumpTableName;
  private String       dumpTableDumpNameColumnType;
  private String       dumpTableDumpDateColumnType;
  private String       dumpTableDumpPathColumnType;
  private String       dumpTableDumpFormatColumnType;
  private String       dumpTableCheckpointNameColumnType;
  private String       dumpTableBackendNameColumnType;
  private String       dumpTableTablesColumnName;
  private String       dumpTableTablesColumnType;
  private String       dumpTableExtraStatementDefinition;

  /** Current maximum value of the primary key in logTableName. */
  private long         logTableId                                       = 0;

  /** Timeout for SQL requests. */
  private int          timeout;

  private LoggerThread loggerThread;

  /**
   * Creates a new <code>RecoveryLog</code> instance.
   * 
   * @param driverName the driverClassName name.
   * @param driverClassName the driverClassName class name.
   * @param url the JDBC URL.
   * @param login the login to use to connect to the database.
   * @param password the password to connect to the database.
   * @param requestTimeout timeout in seconds for update queries.
   * @param recoveryBatchSize number of queries that can be accumulated into a
   *          batch when recovering
   */
  public RecoveryLog(String driverName, String driverClassName, String url,
      String login, String password, int requestTimeout, int recoveryBatchSize)
  {
    this.driverName = driverName;
    this.driverClassName = driverClassName;
    this.url = url;
    this.login = login;
    this.password = password;
    this.timeout = requestTimeout;
    if (recoveryBatchSize < 1)
    {
      logger
          .warn("RecoveryBatchSize was set to a value lesser than 1, resetting value to 1.");
      recoveryBatchSize = 1;
    }
    this.recoveryBatchSize = recoveryBatchSize;

    // Connect to the database
    try
    {
      getDatabaseConnection();
    }
    catch (SQLException e)
    {
      throw new RuntimeException("Unable to connect to the database: " + e);
    }

    // Logger thread will be created in checkRecoveryLogTables()
    // after database has been initialized
  }

  //
  // Database manipulation and access
  //

  /**
   * Gets a connection to the database.
   * 
   * @return a connection to the database
   * @exception SQLException if an error occurs.
   * @see #invalidateInternalConnection()
   */
  protected Connection getDatabaseConnection() throws SQLException
  {
    try
    {
      if (internalConnectionManagedByGetDatabaseConnection == null)
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("recovery.jdbc.connect", new String[]{url,
              login}));
        internalConnectionManagedByGetDatabaseConnection = DriverManager
            .getConnection(url, login, password, driverName, driverClassName);
      }
      return internalConnectionManagedByGetDatabaseConnection;
    }
    catch (RuntimeException e)
    {
      String msg = Translate.get("recovery.jdbc.connect.failed", e);
      if (logger.isDebugEnabled())
        logger.debug(msg, e);
      throw new SQLException(msg);
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();
      String msg = Translate.get("recovery.jdbc.connect.failed", e);
      if (logger.isDebugEnabled())
        logger.debug(msg, e);
      throw new SQLException(msg);
    }
  }

  /**
   * Increments the value of logTableId.
   */
  private synchronized long incrementLogTableId()
  {
    logTableId++;
    return logTableId;
  }

  /**
   * Checks if the tables (log and checkpoint) already exist, and create them if
   * needed.
   */
  private void intializeDatabase() throws SQLException
  {
    boolean createLogTable = true;
    boolean createCheckpointTable = true;
    boolean createBackendTable = true;
    boolean createDumpTable = true;
    Connection connection;
    // Check if tables exist
    try
    {
      connection = getDatabaseConnection();
      connection.setAutoCommit(false);
      // Get DatabaseMetaData
      DatabaseMetaData metaData = connection.getMetaData();

      // Get a description of tables matching the catalog, schema, table name
      // and type.
      // Sending in null for catalog and schema drop them from the selection
      // criteria. Replace the last argument in the getTables method with
      // types below to obtain only database tables. (Sending in null
      // retrieves all types).
      String[] types = {"TABLE", "VIEW"};
      ResultSet rs = metaData.getTables(null, null, "%", types);

      // Get tables metadata
      String tableName;
      while (rs.next())
      {
        // 1 is table catalog, 2 is table schema, 3 is table name, 4 is type
        tableName = rs.getString(3);
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("recovery.jdbc.table.found", tableName));
        if (tableName.equalsIgnoreCase(logTableName))
        {
          if (tableName.compareTo(logTableName) != 0)
            logger.warn(Translate.get("recovery.jdbc.logtable.case.mismatch",
                new String[]{logTableName, tableName}));
          createLogTable = false;
          // initialize logTableId
          PreparedStatement p = null;
          try
          {
            ResultSet result = null;
            p = connection.prepareStatement("SELECT MAX(id) AS max_id FROM "
                + logTableName);
            result = p.executeQuery();
            if (result.next())
              logTableId = result.getLong("max_id");
            else
              logTableId = 0;
            p.close();
          }
          catch (SQLException e)
          {
            try
            {
              if (p != null)
                p.close();
            }
            catch (Exception ignore)
            {
            }
            throw new RuntimeException(Translate.get(
                "recovery.jdbc.logtable.getvalue.failed", e));
          }

        }
        if (tableName.equalsIgnoreCase(checkpointTableName))
        {
          if (tableName.compareTo(checkpointTableName) != 0)
            logger.warn(Translate.get(
                "recovery.jdbc.checkpointtable.case.mismatch", new String[]{
                    checkpointTableName, tableName}));
          createCheckpointTable = false;
        }
        else if (tableName.equalsIgnoreCase(backendTableName))
        {
          if (tableName.compareTo(backendTableName) != 0)
            logger.warn(Translate.get(
                "recovery.jdbc.backendtable.case.mismatch", new String[]{
                    backendTableName, tableName}));
          createBackendTable = false;
        }
        else if (tableName.equalsIgnoreCase(dumpTableName))
        {
          if (tableName.compareTo(dumpTableName) != 0)
            logger.warn(Translate.get("recovery.jdbc.dumptable.case.mismatch",
                new String[]{backendTableName, tableName}));
          createDumpTable = false;
        }
      }
      try
      {
        connection.commit();
        connection.setAutoCommit(true);
      }
      catch (Exception ignore)
      {
        // Read-only transaction we don't care
      }
    }
    catch (SQLException e)
    {
      logger.error(Translate.get("recovery.jdbc.table.no.description"), e);
      throw e;
    }

    // Create the missing tables
    Statement stmt = null;
    if (createLogTable)
    {
      if (logger.isInfoEnabled())
        logger.info(Translate
            .get("recovery.jdbc.logtable.create", logTableName));
      try
      {
        stmt = connection.createStatement();
        stmt.executeUpdate(logTableCreateStatement);
        stmt.close();
      }
      catch (SQLException e)
      {
        throw new SQLException(Translate.get(
            "recovery.jdbc.logtable.create.failed", new String[]{logTableName,
                e.getMessage()}));
      }
    }
    if (createCheckpointTable)
    {
      if (logger.isInfoEnabled())
        logger.info(Translate.get("recovery.jdbc.checkpointtable.create",
            checkpointTableName));
      try
      {
        stmt = connection.createStatement();
        stmt.executeUpdate(checkpointTableCreateStatement);
        stmt.close();
      }
      catch (SQLException e)
      {
        throw new SQLException(Translate.get(
            "recovery.jdbc.checkpointtable.create.failed", new String[]{
                logTableName, e.getMessage()}));
      }

      // Add an initial checkpoint in the table
      String checkpointName = "Initial_empty_recovery_log";
      PreparedStatement pstmt = null;
      try
      {
        if (logger.isDebugEnabled())
          logger.debug("Storing checkpoint " + checkpointName
              + " at request id " + logTableId);
        pstmt = connection.prepareStatement("INSERT INTO "
            + checkpointTableName + " VALUES(?,?)");
        pstmt.setString(1, checkpointName);
        pstmt.setLong(2, logTableId);
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException e)
      {
        try
        {
          if (pstmt != null)
            pstmt.close();
        }
        catch (Exception ignore)
        {
        }
        throw new SQLException(Translate.get(
            "recovery.jdbc.checkpoint.store.failed", new String[]{
                checkpointName, e.getMessage()}));
      }
    }
    if (createBackendTable)
    {
      if (logger.isInfoEnabled())
        logger.info(Translate.get("recovery.jdbc.backendtable.create",
            backendTableName));
      try
      {
        stmt = connection.createStatement();
        stmt.executeUpdate(backendTableCreateStatement);
        stmt.close();
      }
      catch (SQLException e)
      {
        throw new SQLException(Translate.get(
            "recovery.jdbc.backendtable.create.failed", new String[]{
                logTableName, e.getMessage()}));
      }
    }
    if (createDumpTable)
    {
      if (logger.isInfoEnabled())
        logger.info(Translate.get("recovery.jdbc.dumptable.create",
            dumpTableName));
      try
      {
        stmt = connection.createStatement();
        stmt.executeUpdate(dumpTableCreateStatement);
        stmt.close();
      }
      catch (SQLException e)
      {
        throw new SQLException(Translate.get(
            "recovery.jdbc.dumptable.create.failed", new String[]{
                dumpTableName, e.getMessage()}));
      }
    }
  }

  /**
   * Invalidate the connection when an error occurs so that the next call to
   * getDatabaseConnection() re-allocates a new connection.
   * 
   * @see #getDatabaseConnection()
   */
  protected void invalidateInternalConnection()
  {
    try
    {
      internalConnectionManagedByGetDatabaseConnection.close();
    }
    catch (Exception ignore)
    {
    }
    internalConnectionManagedByGetDatabaseConnection = null;
  }

  //
  //
  // Logging related methods
  //
  //

  /**
   * Log a transaction abort. This is used only for transaction that were
   * started but where no request was executed, which is in fact an empty
   * transaction. The underlying implementation might safely discard the
   * corresponding begin from the log as an optimization.
   * 
   * @param tm The transaction marker metadata
   * @return the identifier of the entry in the recovery log
   */
  public long logAbort(TransactionMarkerMetaData tm)
  {
    // We have to perform exactly the same job as a rollback
    return logRollback(tm);
  }

  /**
   * Log the beginning of a new transaction.
   * 
   * @param tm The transaction marker metadata
   * @return the identifier of the entry in the recovery log
   */
  public long logBegin(TransactionMarkerMetaData tm)
  {
    // Store the begin in the database
    long id = incrementLogTableId();
    loggerThread.log(new LogRequestEvent(new LogEntry(id, tm.getLogin(),
        "begin", tm.getTransactionId(), false)));
    return id;
  }

  /**
   * Log a transaction commit.
   * 
   * @param tm The transaction marker metadata
   * @return the identifier of the entry in the recovery log
   */
  public long logCommit(TransactionMarkerMetaData tm)
  {
    long id = incrementLogTableId();
    loggerThread.log(new LogRequestEvent(new LogEntry(id, tm.getLogin(),
        "commit", tm.getTransactionId(), false)));
    return id;
  }

  /**
   * Log a log entry in the recovery log.
   * 
   * @param logEntry the log entry to to be written in the recovery log.
   */
  public void logLogEntry(LogEntry logEntry)
  {
    loggerThread.log(new LogRequestEvent(logEntry));
  }

  /**
   * Log a transaction savepoint removal.
   * 
   * @param tm The transaction marker metadata
   * @param name The name of the savepoint to log
   * @return the identifier of the entry in the recovery log
   */
  public long logReleaseSavepoint(TransactionMarkerMetaData tm, String name)
  {
    long id = incrementLogTableId();
    loggerThread.log(new LogRequestEvent(new LogEntry(id, tm.getLogin(),
        "release " + name, tm.getTransactionId(), false)));
    return id;
  }

  /**
   * Log a write request.
   * 
   * @param request The write request to log
   * @return the identifier of the entry in the recovery log
   */
  public long logRequest(AbstractWriteRequest request)
  {
    long id = incrementLogTableId();
    loggerThread.log(new LogRequestEvent(new LogEntry(id, request.getLogin(),
        request.getSQL(), request.getTransactionId(), request
            .getEscapeProcessing())));
    return id;
  }

  /**
   * Log a call to a stored procedure.
   * 
   * @param proc The stored procedure call to log
   * @param isRead True if the stored procedure call returns a ResultSet
   * @return the identifier of the entry in the recovery log
   */
  public long logRequest(StoredProcedure proc, boolean isRead)
  {
    long id = incrementLogTableId();
    if (isRead)
      loggerThread.log(new LogRequestEvent(new LogEntry(incrementLogTableId(),
          proc.getLogin(), proc.getSQL(), proc.getTransactionId(), proc
              .getEscapeProcessing())));
    else
    { // Reverse the first bracket so that we can identify a write call
      StringBuffer writeCall = new StringBuffer(proc.getSQL());
      writeCall.setCharAt(0, '}');
      loggerThread.log(new LogRequestEvent(new LogEntry(incrementLogTableId(),
          proc.getLogin(), writeCall.toString(), proc.getTransactionId(), proc
              .getEscapeProcessing())));
    }
    return id;
  }

  /**
   * Log a transaction rollback.
   * 
   * @param tm The transaction marker metadata
   * @return the identifier of the entry in the recovery log
   */
  public long logRollback(TransactionMarkerMetaData tm)
  {
    long id = incrementLogTableId();
    // Some backends started a recovery process, log the rollback
    loggerThread.log(new LogRollbackEvent(new LogEntry(id, tm.getLogin(),
        "rollback", tm.getTransactionId(), false)));
    return id;
  }

  /**
   * Log a transaction rollback to a savepoint
   * 
   * @param tm The transaxtion marker metadata
   * @param savepointName The name of the savepoint
   * @return the identifier of the entry in the recovery log
   */
  public long logRollback(TransactionMarkerMetaData tm, String savepointName)
  {
    long id = incrementLogTableId();
    loggerThread.log(new LogRequestEvent(new LogEntry(id, tm.getLogin(),
        "rollback " + savepointName, tm.getTransactionId(), false)));
    return id;
  }

  /**
   * Log a transaction savepoint.
   * 
   * @param tm The transaction marker metadata
   * @param name The name of the savepoint to log
   * @return the identifier of the entry in the recovery log
   */
  public long logSetSavepoint(TransactionMarkerMetaData tm, String name)
  {
    long id = incrementLogTableId();
    loggerThread.log(new LogRequestEvent(new LogEntry(id, tm.getLogin(),
        "savepoint " + name, tm.getTransactionId(), false)));
    return id;
  }

  /**
   * Reset the current log table id and delete the recovery log information
   * older than the given checkpoint. This method also deletes all entries in
   * the checkpoint table. This method is asynchronous: the delete is performed
   * via a post to the loggger thread.
   * 
   * @param checkpointName the checkpoint name to delete from.
   * @param newCheckpointId the new checkpoint identifier
   * @throws SQLException if an error occurs
   */
  public void resetLogTableIdAndDeleteRecoveryLog(String checkpointName,
      long newCheckpointId) throws SQLException
  {
    long oldId = getCheckpointRequestId(checkpointName);
    synchronized (this)
    {
      // resetLog cleans the recovery log table, resets the checkpointTable and
      // renumber the queries since oldId (checkpoint assigned to the transfer).
      loggerThread
          .log(new ResetLogEvent(oldId, newCheckpointId, checkpointName));
      logTableId = newCheckpointId + logTableId - oldId;
    }
  }

  /**
   * Remove a transaction commit from the recovery log. This commit was logged
   * because no backend was available locally to execute it but that finally
   * ended up in failing at all other controllers.
   * 
   * @param tm the commited transaction
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#removeFailedCommitFromRecoveryLog(TransactionMarkerMetaData)
   */
  public void unlogCommit(TransactionMarkerMetaData tm)
  {
    // Timeout is used here to transport the log id of the commit
    loggerThread.log(new UnlogRequestEvent(new LogEntry(tm.getTimeout(), tm
        .getLogin(), "commit", tm.getTransactionId(), false)));
  }

  /**
   * Remove a request from the recovery log. This request was logged because no
   * backend was available locally to execute it but that finally ended up in
   * failing at all other controllers.
   * 
   * @param request request that was logged but failed at all controllers and
   *          must be removed from recovery log
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#removeFailedRequestFromRecoveryLog(AbstractWriteRequest,
   *      long)
   */
  public void unlogRequest(AbstractRequest request)
  {
    loggerThread.log(new UnlogRequestEvent(new LogEntry(request.getId(),
        request.getLogin(), request.getSQL(), request.getTransactionId(),
        request.getEscapeProcessing())));
  }

  /**
   * Remove a stored procedure from the recovery log. This stored procedure was
   * logged because no backend was available locally to execute it but that
   * finally ended up in failing at all other controllers.
   * 
   * @param proc stored procedure that was logged but failed at all controllers
   *          and must be removed from recovery log
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#removeFailedStoredProcedureFromRecoveryLog(StoredProcedure)
   */
  public void unlogRequest(StoredProcedure proc)
  {
    // If we have to unlog this can only be a write stored procedure, a read
    // would not have been broadcasted.
    StringBuffer writeCall = new StringBuffer(proc.getSQL());
    writeCall.setCharAt(0, '}');
    loggerThread.log(new UnlogRequestEvent(new LogEntry(proc.getId(), proc
        .getLogin(), writeCall.toString(), proc.getTransactionId(), proc
        .getEscapeProcessing())));
  }

  /**
   * Remove a transaction rollback from the recovery log. This rollback was
   * logged because no backend was available locally to execute it but that
   * finally ended up in failing at all other controllers.
   * 
   * @param tm the commited transaction
   * @see org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager#removeFailedRollbackFromRecoveryLog(TransactionMarkerMetaData)
   */
  public void unlogRollback(TransactionMarkerMetaData tm)
  {
    // Timeout is used here to transport the log id of the rollback
    loggerThread.log(new UnlogRequestEvent(new LogEntry(tm.getTimeout(), tm
        .getLogin(), "rollback", tm.getTransactionId(), false)));
  }

  /**
   * Shutdown the recovery log and all its threads.
   */
  public void shutdown()
  {
    if (loggerThread != null)
      loggerThread.shutdown();
  }

  //
  // Recovery process
  //

  /**
   * Notify the recovery log that a recovery process has started.
   */
  public synchronized void beginRecovery()
  {
    recoveringNb++;
  }

  /**
   * Possibly clean the recovery log after all recovery process are done. This
   * removes all rollbacked transaction from the recovery log.
   * 
   * @exception SQLException if an error occurs.
   */
  public void cleanRecoveryLog() throws SQLException
  {
    PreparedStatement stmt = null;

    // Remove the rollback statements and associated requests from the database
    ResultSet rs = null;
    try
    {
      // Get the list of transaction ids on which a rollback occurred
      stmt = getDatabaseConnection().prepareStatement(
          "SELECT transaction_id FROM " + logTableName + " WHERE "
              + logTableSqlColumnName + " LIKE ?");
      stmt.setString(1, "rollback");
      rs = stmt.executeQuery();
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
      throw new SQLException("Unable get rollback statements : " + e);
    }
    PreparedStatement pstmt = null;
    long transactionId = -1;
    try
    {
      // remove the rollbacked transaction from the database
      while (rs.next())
      {
        transactionId = rs.getLong("transaction_id");
        pstmt = getDatabaseConnection().prepareStatement(
            "DELETE FROM " + logTableName + " WHERE transaction_id=?");
        pstmt.setLong(1, transactionId);
        pstmt.executeUpdate();
        pstmt.close();
      }
      rs.close();
      stmt.close();
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get(
          "recovery.jdbc.transaction.remove.failed", new String[]{
              String.valueOf(transactionId), e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
      try
      {
        if (pstmt != null)
          pstmt.close();
      }
      catch (Exception ignore)
      {
      }

    }
  }

  /**
   * Notify the recovery log that a recovery process has finished. If this is
   * the last recovery process to finish, the cleanRecoveryLog method is called
   * 
   * @see #cleanRecoveryLog()
   */
  public synchronized void endRecovery()
  {
    recoveringNb--;
    if (recoveringNb == 0)
    {
      try
      {
        cleanRecoveryLog();
      }
      catch (SQLException e)
      {
        logger.error(Translate.get("recovery.cleaning.failed"), e);
      }
    }
  }

  /**
   * Retrieve recovery information on a backend. This includes, the last known
   * state of the backend, and the last known checkpoint
   * 
   * @param databaseName the virtual database name
   * @param backendName the backend name
   * @return <code>BackendRecoveryInfo<code> instance or <code>null</code> if the backend does not exist
   */
  public BackendRecoveryInfo getBackendRecoveryInfo(String databaseName,
      String backendName)
  {
    PreparedStatement stmt = null;
    String checkpoint = null;
    int backendState = BackendState.UNKNOWN;
    try
    {
      // 1. Get the reference point to delete
      stmt = getDatabaseConnection().prepareStatement(
          "SELECT * FROM " + backendTableName
              + " WHERE backend_name LIKE ? AND database_name LIKE ?");
      stmt.setString(1, backendName);
      stmt.setString(2, databaseName);
      ResultSet rs = stmt.executeQuery();

      if (rs.next())
      {
        checkpoint = rs.getString("checkpoint_name");
        backendState = rs.getInt("backend_state");
      }
      rs.close();
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();
      logger.info(
          "An error occured while retrieving backend recovery information", e);
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
    return new BackendRecoveryInfo(backendName, checkpoint, backendState,
        databaseName);
  }

  /**
   * Get the id of the last transaction logged in the recovery log.
   * 
   * @return the last transaction id.
   * @throws SQLException if an error occured while retrieving the id.
   */
  public long getLastTransactionId() throws SQLException
  {
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = getDatabaseConnection().createStatement();
      rs = stmt.executeQuery("select max(transaction_id) from " + logTableName);
      if (rs.next())
        return rs.getLong(1);
      else
        // Table is empty
        return 0;
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();
      throw e;
    }
    finally
    {
      try
      {
        if (rs != null)
          rs.close();
      }
      catch (Exception ignore)
      {
      }
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Returns the recoveringNb value.
   * 
   * @return Returns the recoveringNb.
   */
  public long getRecoveringNb()
  {
    return recoveringNb;
  }

  /**
   * Number of queries that can be accumulated into a batch when recovering
   * 
   * @return the recovery batch size
   */
  public int getRecoveryBatchSize()
  {
    return recoveryBatchSize;
  }

  /**
   * Returns <code>true</code> if at least one backend has started a recover
   * process.
   * 
   * @return <code>boolean</code>
   */
  public synchronized boolean isRecovering()
  {
    return recoveringNb > 0;
  }

  /**
   * Get the next log entry from the recovery log given the id of the previous
   * log entry.
   * 
   * @param previousLogEntryId previous log entry identifier
   * @return the next log entry from the recovery log or null if no further
   *         entry can be found
   * @throws SQLException if an error occurs while accesing the recovery log
   */
  public LogEntry getNextLogEntry(long previousLogEntryId) throws SQLException
  {
    ResultSet rs = null;
    boolean emptyResult;
    PreparedStatement stmt = null;
    try
    {
      stmt = getDatabaseConnection().prepareStatement(
          "SELECT * FROM " + logTableName + " WHERE id=?");
      // Note that the statement is closed in the finally block
      do
      {
        previousLogEntryId++;
        stmt.setLong(1, previousLogEntryId);
        // Close ResultSet of previous loop step to free resources.
        if (rs != null)
          rs.close();
        rs = stmt.executeQuery();
        emptyResult = !rs.next();
      }
      while (emptyResult && (previousLogEntryId <= logTableId));

      // No more request after this one
      if (emptyResult)
        return null;

      // Read columns in order to prevent issues with MS SQL Server as reported
      // Charles Cordingley.
      long id = rs.getLong("id");
      String user = rs.getString("vlogin");
      String sql = rs.getString(logTableSqlColumnName);
      long transactionId = rs.getLong("transaction_id");
      // Note that booleanProcessing = true is the default value in
      // AbstractRequest
      return new LogEntry(id, user, sql, transactionId, true);
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get("recovery.jdbc.recover.failed", e));
    }
    finally
    {
      try
      {
        if (rs != null)
          rs.close();
      }
      catch (Exception ignore)
      {
      }
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Get the next request (begin/commit/rollback or WriteRequest) from the
   * recovery log given the id of the previously recovered request.
   * <p>
   * The id of the request before the first one to recover is given by
   * getCheckpointRequestId.
   * 
   * @param previousRequestId id of the previously recovered request
   * @return AbstractTask task corresponding to the next request to recover or
   *         null if no such request exists
   * @exception SQLException if an error occurs
   * @see #getCheckpointRequestId(String)
   */
  public RecoveryTask recoverNextRequest(long previousRequestId)
      throws SQLException
  {
    RecoveryTask task = null;

    // Get the request with the id after previousRequestId.
    LogEntry logEntry = getNextLogEntry(previousRequestId);
    if (logEntry == null)
      return null;

    // Construct the request object according to its type
    long transactionId = logEntry.getTid();
    long id = logEntry.getId();
    String user = logEntry.getLogin();
    String sql = logEntry.getQuery().trim();

    boolean escapeProcessing = true;
    // Check that the command starts with (only 2 letters are needed)
    // in[sert]/up[date]/de[lete]/cr[eate]/dr[op]/be[gin]/co[mmit]/ro[llback]
    // sa[vepoint]/re[lease]/{c[all]/}c[all] (write stored procedure call)
    String lower = sql.substring(0, 2).toLowerCase();
    if (lower.equals("in"))
    { // insert
      // we don't care about auto-generated keys here
      AbstractWriteRequest wr = new InsertRequest(sql, escapeProcessing,
          timeout, "\n", false /* isRead */);
      wr.setLogin(user);
      if (logger.isDebugEnabled())
        logger.debug("insert request: " + sql);
      setDriverProcessedAndSkeleton(wr);
      if (transactionId != 0)
      {
        wr.setIsAutoCommit(false);
        wr.setTransactionId(transactionId);
      }
      else
        wr.setIsAutoCommit(true);
      task = new RecoveryTask(transactionId, id, new WriteRequestTask(1, 1, wr));
    }
    else if (lower.equals("up"))
    { // update
      AbstractWriteRequest wr = new UpdateRequest(sql, escapeProcessing,
          timeout, "\n");
      wr.setLogin(user);
      setDriverProcessedAndSkeleton(wr);
      if (logger.isDebugEnabled())
        logger.debug("update request: " + sql);
      if (transactionId != 0)
      {
        wr.setIsAutoCommit(false);
        wr.setTransactionId(transactionId);
      }
      else
        wr.setIsAutoCommit(true);
      task = new RecoveryTask(transactionId, id, new WriteRequestTask(1, 1, wr));
    }
    else if (lower.equals("de"))
    { // delete
      AbstractWriteRequest wr = new DeleteRequest(sql, escapeProcessing,
          timeout, "\n");
      wr.setLogin(user);
      setDriverProcessedAndSkeleton(wr);
      if (logger.isDebugEnabled())
        logger.debug("delete request: " + sql);
      if (transactionId != 0)
      {
        wr.setIsAutoCommit(false);
        wr.setTransactionId(transactionId);
      }
      else
        wr.setIsAutoCommit(true);
      task = new RecoveryTask(transactionId, id, new WriteRequestTask(1, 1, wr));
    }
    else if (lower.equals("cr"))
    { // create
      AbstractWriteRequest wr = new CreateRequest(sql, escapeProcessing,
          timeout, "\n");
      wr.setLogin(user);
      setDriverProcessedAndSkeleton(wr);
      if (logger.isDebugEnabled())
        logger.debug("create request: " + sql);
      if (transactionId != 0)
      {
        wr.setIsAutoCommit(false);
        wr.setTransactionId(transactionId);
      }
      else
        wr.setIsAutoCommit(true);
      task = new RecoveryTask(transactionId, id, new WriteRequestTask(1, 1, wr));
    }
    else if (lower.equals("al"))
    { // alter
      AbstractWriteRequest wr = new AlterRequest(sql, escapeProcessing,
          timeout, "\n");
      wr.setLogin(user);
      setDriverProcessedAndSkeleton(wr);
      if (logger.isDebugEnabled())
        logger.debug("alter request: " + sql);
      if (transactionId != 0)
      {
        wr.setIsAutoCommit(false);
        wr.setTransactionId(transactionId);
      }
      else
        wr.setIsAutoCommit(true);
      task = new RecoveryTask(transactionId, id, new WriteRequestTask(1, 1, wr));
    }
    else if (lower.equals("dr"))
    { // drop
      AbstractWriteRequest wr = new DropRequest(sql, escapeProcessing, timeout,
          "\n");
      wr.setLogin(user);
      setDriverProcessedAndSkeleton(wr);
      if (logger.isDebugEnabled())
        logger.debug("drop request: " + sql);
      if (transactionId != 0)
      {
        wr.setIsAutoCommit(false);
        wr.setTransactionId(transactionId);
      }
      else
        wr.setIsAutoCommit(true);
      task = new RecoveryTask(transactionId, id, new WriteRequestTask(1, 1, wr));
    }
    else if (lower.equals("be"))
    { // begin
      task = new RecoveryTask(transactionId, id, new BeginTask(1, 1,
          (long) timeout * 1000, user, transactionId));
      if (logger.isDebugEnabled())
        logger.debug("begin transaction: " + transactionId);
    }
    else if (lower.equals("co"))
    { // commit
      task = new RecoveryTask(transactionId, id, new CommitTask(1, 1,
          (long) timeout * 1000, user, transactionId));
      if (logger.isDebugEnabled())
        logger.debug("commit transaction: " + transactionId);
    }
    else if (lower.equals("ro"))
    { // rollback
      int index = sql.indexOf(' ');
      if (index == -1)
      {
        task = new RecoveryTask(transactionId, id, new RollbackTask(1, 1,
            (long) timeout * 1000, user, transactionId));
        if (logger.isDebugEnabled())
          logger.debug("rollback transaction: " + transactionId);
      }
      else
      {
        String savepointName = sql.substring(index);
        task = new RecoveryTask(transactionId, id, new RollbackToSavepointTask(
            1, 1, (long) timeout * 1000, user, transactionId, savepointName));
        if (logger.isDebugEnabled())
          logger.debug("rollback transaction to savepoint: " + transactionId);
      }
    }
    else if (lower.equals("sa"))
    { // set savepoint
      String savepointName = sql.substring(sql.indexOf(' '));
      task = new RecoveryTask(transactionId, id, new SavepointTask(1, 1,
          (long) timeout * 1000, user, transactionId, savepointName));
      if (logger.isDebugEnabled())
        logger.debug("transaction set savepoint: " + transactionId);
    }
    else if (lower.equals("re"))
    { // release savepoint
      String savepointName = sql.substring(sql.indexOf(' '));
      task = new RecoveryTask(transactionId, id, new ReleaseSavepointTask(1, 1,
          (long) timeout * 1000, user, transactionId, savepointName));
      if (logger.isDebugEnabled())
        logger.debug("transaction release savepoint: " + transactionId);
    }
    else if (lower.equals("{c"))
    { // read stored procedure call "{call ...}"
      StoredProcedure proc = new StoredProcedure(sql, escapeProcessing,
          timeout, "\n", true /* isRead */);
      proc.setLogin(user);
      setDriverProcessedAndSkeleton(proc);
      if (logger.isDebugEnabled())
        logger.debug("read stored procedure call: " + sql);
      if (transactionId != 0)
      {
        proc.setIsAutoCommit(false);
        proc.setTransactionId(transactionId);
      }
      else
        proc.setIsAutoCommit(true);
      task = new RecoveryTask(transactionId, id, new ReadStoredProcedureTask(1,
          1, proc, null));
    }
    else if (lower.equals("}c"))
    { // write stored procedure call,
      // we must replace "}call ...}" with "{call ...}"
      StringBuffer writeCall = new StringBuffer(sql);
      writeCall.setCharAt(0, '{');
      StoredProcedure proc = new StoredProcedure(writeCall.toString(),
          escapeProcessing, timeout, "\n", false /* isRead */);
      proc.setLogin(user);
      setDriverProcessedAndSkeleton(proc);
      if (logger.isDebugEnabled())
        logger.debug("write stored procedure call: " + sql);
      if (transactionId != 0)
      {
        proc.setIsAutoCommit(false);
        proc.setTransactionId(transactionId);
      }
      else
        proc.setIsAutoCommit(true);
      task = new RecoveryTask(transactionId, id, new WriteStoredProcedureTask(
          1, 1, proc));
    }
    else
      throw new SQLException(Translate.get("recovery.jdbc.sql.unkwown", sql));
    return task;
  }

  //
  //
  // Checkpoint Management
  //
  //

  /**
   * Deletes recovery log entries that are older than specified checkpoint.
   * Entries are deleted directly into the recovery log database, as opposed to
   * via a post to the logger thread (as resetLogTableIdAndDeleteRecoveryLog).
   * 
   * @param checkpointName the name of the checkpoint uptil which log entries
   *          should be removed
   * @throws SQLException in case of error.
   */
  public void deleteLogEntriesBeforeCheckpoint(String checkpointName)
      throws SQLException
  {
    long id = getCheckpointRequestId(checkpointName);
    PreparedStatement stmt = null;
    try
    {
      stmt = getDatabaseConnection().prepareStatement(
          "DELETE FROM " + getLogTableName() + " WHERE id<=?");
      stmt.setLong(1, id);
      stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      // TODO: Check error message below
      throw new SQLException(Translate.get(
          "recovery.jdbc.transaction.remove.failed", new String[]{
              String.valueOf(id), e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Returns an array of names of all the checkpoint available in the recovery
   * log
   * 
   * @return <code>ArrayList</code> of <code>String</code> checkpoint names
   * @throws SQLException if fails
   */
  public ArrayList getCheckpointNames() throws SQLException
  {
    PreparedStatement stmt = null;

    waitForLogQueueToEmpty();

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Retrieving checkpoint names list");
      stmt = getDatabaseConnection().prepareStatement(
          "SELECT name from " + checkpointTableName);
      ResultSet rs = stmt.executeQuery();
      ArrayList list = new ArrayList();
      while (rs.next())
      {
        list.add(rs.getString(1));
      }
      rs.close();
      return list;
    }
    catch (Exception e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get(
          "recovery.jdbc.checkpoint.list.failed", e));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (SQLException ignore)
      {
      }
    }
  }

  /**
   * Get the request id corresponding to a given checkpoint. This is the first
   * step in a recovery process. Following steps consist in calling
   * recoverNextRequest.
   * 
   * @param checkpointName Name of the checkpoint
   * @return int the request identifier corresponding to this checkpoint.
   * @exception SQLException if an error occurs
   * @see #recoverNextRequest(long)
   */
  public long getCheckpointRequestId(String checkpointName) throws SQLException
  {
    long requestId = -1;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = getDatabaseConnection().prepareStatement(
          "SELECT request_id FROM " + checkpointTableName
              + " WHERE name LIKE ?");
      stmt.setString(1, checkpointName);
      rs = stmt.executeQuery();

      if (rs.next())
        requestId = rs.getLong("request_id");
      else
      {
        String msg = Translate.get("recovery.jdbc.checkpoint.not.found",
            checkpointName);
        logger.info(msg);
        throw new SQLException(msg);
      }
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get(
          "recovery.jdbc.checkpoint.not.found.error", new String[]{
              checkpointName, e.getMessage()}));
    }
    finally
    {
      try
      {
        if (rs != null)
          rs.close();
      }
      catch (Exception ignore)
      {
      }
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
    return requestId;
  }

  /**
   * Remove a checkpoint from the recovery. This is useful for recovery
   * maintenant
   * 
   * @param checkpointName to remove
   * @throws SQLException if an error occurs
   */
  public void removeCheckpoint(String checkpointName) throws SQLException
  {
    PreparedStatement stmt = null;

    waitForLogQueueToEmpty();

    try
    {
      // 1. Get the reference point to delete
      stmt = getDatabaseConnection().prepareStatement(
          "SELECT * FROM " + checkpointTableName + " WHERE name LIKE ?");
      stmt.setString(1, checkpointName);
      ResultSet rs = stmt.executeQuery();
      boolean checkpointExists = rs.next();
      if (!checkpointExists)
      {
        rs.close();
        stmt.close();
        throw new SQLException("Checkpoint " + checkpointName
            + " does not exist");
      }

      long requestId = rs.getLong("request_id");
      rs.close();
      stmt.close();

      // Delete all entries below
      stmt = getDatabaseConnection().prepareStatement(
          "DELETE FROM " + logTableName + " WHERE id <= ?");
      stmt.setLong(1, requestId);
      stmt.executeUpdate();
      stmt.close();

      // Delete checkpoint name
      stmt = getDatabaseConnection().prepareStatement(
          "DELETE FROM " + checkpointTableName + " WHERE name like ?");
      stmt.setString(1, checkpointName);
      stmt.executeUpdate();
      stmt.close();
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get(
          "recovery.jdbc.checkpoint.remove.failed", new String[]{
              checkpointName, e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }

    }
  }

  /**
   * Store the state of the backend in the recovery log
   * 
   * @param databaseName the virtual database name
   * @param backendRecoveryInfo the backend recovery information to store
   * @throws SQLException if cannot proceed
   */
  public void storeBackendRecoveryInfo(String databaseName,
      BackendRecoveryInfo backendRecoveryInfo) throws SQLException
  {
    PreparedStatement stmt = null;
    PreparedStatement stmt2 = null;
    if ((backendRecoveryInfo.getCheckpoint() == null)
        || ((backendRecoveryInfo.getBackendState() != BackendState.DISABLED) && (backendRecoveryInfo
            .getBackendState() != BackendState.UNKNOWN)))
      backendRecoveryInfo.setCheckpoint(""); // No checkpoint
    else
    { // Check checkpoint name validity
      getCheckpointRequestId(backendRecoveryInfo.getCheckpoint());
    }

    try
    {
      // 1. Get the reference point to delete
      stmt = getDatabaseConnection().prepareStatement(
          "SELECT * FROM " + backendTableName
              + " WHERE backend_name LIKE ? and database_name LIKE ?");
      stmt.setString(1, backendRecoveryInfo.getBackendName());
      stmt.setString(2, databaseName);
      ResultSet rs = stmt.executeQuery();
      boolean mustUpdate = rs.next();
      rs.close();
      if (!mustUpdate)
      {
        stmt2 = getDatabaseConnection().prepareStatement(
            "INSERT INTO " + backendTableName + " values(?,?,?,?)");
        stmt2.setString(1, databaseName);
        stmt2.setString(2, backendRecoveryInfo.getBackendName());
        stmt2.setInt(3, backendRecoveryInfo.getBackendState());
        stmt2.setString(4, backendRecoveryInfo.getCheckpoint());
        if (stmt2.executeUpdate() != 1)
          throw new SQLException(
              "Error while inserting new backend reference. Incorrect number of rows");
      }
      else
      {
        stmt2 = getDatabaseConnection()
            .prepareStatement(
                "UPDATE "
                    + backendTableName
                    + " set backend_state=?,checkpoint_name=? where backend_name=? and database_name=?");
        stmt2.setInt(1, backendRecoveryInfo.getBackendState());
        stmt2.setString(2, backendRecoveryInfo.getCheckpoint());
        stmt2.setString(3, backendRecoveryInfo.getBackendName());
        stmt2.setString(4, databaseName);
        if (stmt2.executeUpdate() != 1)
          throw new SQLException(
              "Error while updating backend reference. Incorrect number of rows");
      }
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();

      logger.warn("Failed to store backend recovery info", e);

      throw new SQLException("Unable to update checkpoint '"
          + backendRecoveryInfo.getCheckpoint() + "' for backend:"
          + backendRecoveryInfo.getBackendName());
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
      try
      {
        if (stmt2 != null)
          stmt2.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Store a Checkpoint using the current log state.
   * 
   * @param checkpointName Name of the checkpoint
   * @exception SQLException if an error occurs
   */
  public void storeCheckpoint(String checkpointName) throws SQLException
  {
    storeCheckpoint(checkpointName, logTableId);
  }

  /**
   * Store a Checkpoint using the given request id.
   * 
   * @param checkpointName Name of the checkpoint
   * @param requestId request identifier
   * @exception SQLException if an error occurs
   */
  public void storeCheckpoint(String checkpointName, long requestId)
      throws SQLException
  {
    PreparedStatement stmt = null;

    // Check if a checkpoint with the name checkpointName already exists
    if (!validCheckpointName(checkpointName))
    {
      throw new SQLException(Translate.get(
          "recovery.jdbc.checkpoint.duplicate", checkpointName));
    }

    waitForLogQueueToEmpty();

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Storing checkpoint " + checkpointName + " at request id "
            + requestId);
      stmt = getDatabaseConnection().prepareStatement(
          "INSERT INTO " + checkpointTableName + " VALUES(?,?)");
      stmt.setString(1, checkpointName);
      stmt.setLong(2, requestId);
      stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get(
          "recovery.jdbc.checkpoint.store.failed", new String[]{checkpointName,
              e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  //
  // 
  // Dump management
  //
  //

  /**
   * Get the DumpInfo element corresponding to the given dump name. Returns null
   * if no information is found for this dump name.
   * 
   * @param dumpName the name of the dump to look for
   * @return a <code>DumpInfo</code> object or null if not found in the table
   * @throws SQLException if a recovery log database access error occurs
   */
  public DumpInfo getDumpInfo(String dumpName) throws SQLException
  {
    PreparedStatement stmt = null;

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Retrieving dump " + dumpName + " information");
      stmt = getDatabaseConnection().prepareStatement(
          "SELECT * from " + dumpTableName + " WHERE dump_name LIKE ?");
      stmt.setString(1, dumpName);

      ResultSet rs = stmt.executeQuery();
      DumpInfo dumpInfo = null;
      if (rs.next())
      {
        dumpInfo = new DumpInfo(rs.getString("dump_name"), rs
            .getString("dump_date"), rs.getString("dump_path"), rs
            .getString("dump_format"), rs.getString("checkpoint_name"), rs
            .getString("backend_name"), rs.getString(dumpTableTablesColumnName));
      }
      // else not found, return dumpInfo=null;

      rs.close();
      return dumpInfo;
    }
    catch (Exception e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get("recovery.jdbc.dump.info.failed",
          new String[]{dumpName, e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (SQLException ignore)
      {
      }
    }
  }

  /**
   * Retrieve the list of available dumps.
   * 
   * @return an <code>ArrayList</code> of <code>DumpInfo</code> objects
   * @throws SQLException if a recovery log database access error occurs
   */
  public ArrayList getDumpList() throws SQLException
  {
    PreparedStatement stmt = null;

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Retrieving dump list");
      stmt = getDatabaseConnection().prepareStatement(
          "SELECT * FROM " + dumpTableName + " ORDER BY dump_date DESC");
      ResultSet rs = stmt.executeQuery();
      ArrayList list = new ArrayList();
      while (rs.next())
      {
        list
            .add(new DumpInfo(rs.getString("dump_name"), rs
                .getString("dump_date"), rs.getString("dump_path"), rs
                .getString("dump_format"), rs.getString("checkpoint_name"), rs
                .getString("backend_name"), rs
                .getString(dumpTableTablesColumnName)));
      }
      rs.close();
      return list;
    }
    catch (Exception e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get("recovery.jdbc.dump.list.failed", e));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (SQLException ignore)
      {
      }
    }
  }

  /**
   * Remove a dump information from the dump table base.
   * 
   * @param dumpInfo the <code>DumpInfo</code> to remove
   * @throws SQLException if the dump has has not been removed from the dump
   *           table
   */
  public void removeDump(DumpInfo dumpInfo) throws SQLException
  {
    PreparedStatement stmt = null;

    try
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("removing dump " + dumpInfo.getDumpName());
      }
      stmt = getDatabaseConnection().prepareStatement(
          "DELETE FROM " + dumpTableName + " WHERE dump_name=?");
      stmt.setString(1, dumpInfo.getDumpName());

      stmt.executeUpdate();
    }
    catch (Exception e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get("recovery.jdbc.dump.remove.failed",
          new String[]{dumpInfo.getDumpName(), e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (SQLException ignore)
      {
      }
    }
  }

  /**
   * Set DumpInfo, thereby making a new dump available for restore.
   * 
   * @param dumpInfo the dump info to create.
   * @throws VirtualDatabaseException if an error occurs
   */
  public void setDumpInfo(DumpInfo dumpInfo) throws VirtualDatabaseException
  {
    try
    {
      storeDump(dumpInfo);
    }
    catch (SQLException e)
    {
      throw new VirtualDatabaseException(e);
    }
  }

  /**
   * Store the given dump information in the dump table
   * 
   * @param dump the <code>DumpInfo</code> to store
   * @throws SQLException if a recovery log database access error occurs
   */
  public void storeDump(DumpInfo dump) throws SQLException
  {
    PreparedStatement stmt = null;

    if (dump == null)
      throw new NullPointerException(
          "Invalid null dump in JDBCRecoverylog.storeDump");

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Storing dump " + dump.getDumpName());
      stmt = getDatabaseConnection().prepareStatement(
          "INSERT INTO " + dumpTableName + " VALUES (?,?,?,?,?,?,?)");
      stmt.setString(1, dump.getDumpName());
      stmt.setString(2, dump.getDumpDate());
      stmt.setString(3, dump.getDumpPath());
      stmt.setString(4, dump.getDumpFormat());
      stmt.setString(5, dump.getCheckpointName());
      stmt.setString(6, dump.getBackendName());
      stmt.setString(7, dump.getTables());

      stmt.executeUpdate();
    }
    catch (Exception e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get("recovery.jdbc.dump.store.failed",
          new String[]{dump.getDumpName(), e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (SQLException ignore)
      {
      }
    }
  }

  /**
   * Update the path name for a given checkpoint.
   * 
   * @param dumpName the dump name
   * @param newPath the new path to set
   * @return if the dump path was successfully updated, false if it failed
   *         (usually because the dump does not exist)
   * @throws SQLException if a recovery log database access error occurs
   */
  public boolean updateDumpPath(String dumpName, String newPath)
      throws SQLException
  {
    DumpInfo dumpInfo = getDumpInfo(dumpName);
    if (dumpInfo == null)
      return false;

    PreparedStatement stmt = null;

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Changing old path " + dumpInfo.getDumpPath()
            + " for dump " + dumpInfo.getDumpName() + " to " + newPath);
      stmt = getDatabaseConnection().prepareStatement(
          "UPDATE " + dumpTableName + " SET dump_path=? WHERE dump_name=?");
      stmt.setString(1, newPath);
      stmt.setString(2, dumpName);

      return stmt.executeUpdate() == 1;
    }
    catch (Exception e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get(
          "recovery.jdbc.dump.update.path.failed", new String[]{dumpName,
              e.getMessage()}));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (SQLException ignore)
      {
      }
    }
  }

  //
  // 
  // Recovery log database tables management
  //
  //

  /**
   * Checks if the recovery log and checkpoint tables exist, and create them if
   * they do not exist. This method also starts the logger thread.
   */
  public void checkRecoveryLogTables()
  {
    try
    {
      intializeDatabase();
    }
    catch (SQLException e)
    {
      throw new RuntimeException("Unable to initialize the database: " + e);
    }

    // Start the logger thread
    loggerThread = new LoggerThread(this);
    loggerThread.start();
  }

  /**
   * Returns the backendTableName value.
   * 
   * @return Returns the backendTableName.
   */
  public String getBackendTableName()
  {
    return backendTableName;
  }

  /**
   * Returns the checkpointTableName value.
   * 
   * @return Returns the checkpointTableName.
   */
  public String getCheckpointTableName()
  {
    return checkpointTableName;
  }

  /**
   * Returns the logTableName value.
   * 
   * @return Returns the logTableName.
   */
  public String getLogTableName()
  {
    return logTableName;
  }

  /**
   * Returns the logTableSqlColumnName value.
   * 
   * @return Returns the logTableSqlColumnName.
   */
  public String getLogTableSqlColumnName()
  {
    return logTableSqlColumnName;
  }

  /**
   * Sets the backend table create statement
   * 
   * @param createTable statement to create the table
   * @param tableName the backend table name
   * @param checkpointNameType type for the checkpointName column
   * @param backendNameType type for the backendName column
   * @param backendStateType type for the backendState column
   * @param databaseNameType type for the databaseName column
   * @param extraStatement like primary keys
   */
  public void setBackendTableCreateStatement(String createTable,
      String tableName, String checkpointNameType, String backendNameType,
      String backendStateType, String databaseNameType, String extraStatement)
  {
    this.backendTableCreateTable = createTable;
    this.backendTableName = tableName;
    this.backendTableDatabaseName = databaseNameType;
    this.backendTableBackendName = backendNameType;
    this.backendTableBackendState = backendStateType;
    this.backendTableCheckpointName = checkpointNameType;
    this.backendTableExtraStatement = extraStatement;
    this.backendTableCreateStatement = createTable + " " + backendTableName
        + " (database_name " + databaseNameType + ", backend_name "
        + backendNameType + ",backend_state " + backendStateType
        + ", checkpoint_name " + checkpointNameType + " " + extraStatement
        + ")";

    if (logger.isDebugEnabled())
      logger.debug(Translate.get("recovery.jdbc.backendtable.statement",
          backendTableCreateStatement));
  }

  /**
   * Sets the checkpoint table name and create statement.
   * 
   * @param createTable statement to create the table
   * @param tableName name of the checkpoint table.
   * @param nameType type for the name column
   * @param requestIdType type for the requestId column
   * @param extraStatement like primary keys
   */
  public void setCheckpointTableCreateStatement(String createTable,
      String tableName, String nameType, String requestIdType,
      String extraStatement)
  {
    this.checkpointTableCreateTable = createTable;
    this.checkpointTableName = tableName;
    this.checkpointTableNameType = nameType;
    this.checkpointTableRequestIdType = requestIdType;
    this.checkpointTableExtraStatement = extraStatement;
    // CREATE TABLE tableName (
    // name checkpointNameColumnType,
    // request_id requestIdColumnType,
    // extraStatement)

    checkpointTableCreateStatement = createTable + " " + tableName + " (name "
        + nameType + ",request_id " + requestIdType + extraStatement + ")";
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("recovery.jdbc.checkpointtable.statement",
          checkpointTableCreateStatement));
  }

  /**
   * Sets the dump table name and create statement.
   * 
   * @param createTable statement to create the table
   * @param tableName name of the checkpoint table.
   * @param dumpNameColumnType the dump name column type
   * @param dumpDateColumnType the dump data column type
   * @param dumpPathColumnType the dump path column type
   * @param dumpFormatColumnType the dump tpe column type
   * @param checkpointNameColumnType the checkpoint name column type
   * @param backendNameColumnType the backend name column type
   * @param tablesColumnName the database tables column name
   * @param tablesColumnType the database tables column type
   * @param extraStatement like primary keys
   */
  public void setDumpTableCreateStatement(String createTable, String tableName,
      String dumpNameColumnType, String dumpDateColumnType,
      String dumpPathColumnType, String dumpFormatColumnType,
      String checkpointNameColumnType, String backendNameColumnType,
      String tablesColumnName, String tablesColumnType, String extraStatement)
  {
    this.dumpTableCreateTable = createTable;
    this.dumpTableName = tableName;
    this.dumpTableDumpNameColumnType = dumpNameColumnType;
    this.dumpTableDumpDateColumnType = dumpDateColumnType;
    this.dumpTableDumpPathColumnType = dumpPathColumnType;
    this.dumpTableDumpFormatColumnType = dumpFormatColumnType;
    this.dumpTableCheckpointNameColumnType = checkpointNameColumnType;
    this.dumpTableBackendNameColumnType = backendNameColumnType;
    this.dumpTableTablesColumnName = tablesColumnName;
    this.dumpTableTablesColumnType = tablesColumnType;
    this.dumpTableExtraStatementDefinition = extraStatement;

    // CREATE TABLE DumpTable (
    // dump_name TEXT NOT NULL,
    // dump_date DATE,
    // dump_path TEXT NOT NULL,
    // dump_type TEXT NOT NULL,
    // checkpoint_name TEXT NOT NULL,
    // backend_name TEXT NOT NULL,
    // tables TEXT NOT NULL
    // )

    dumpTableCreateStatement = dumpTableCreateTable + " " + dumpTableName
        + " (dump_name " + dumpTableDumpNameColumnType + ",dump_date "
        + dumpDateColumnType + ",dump_path " + dumpPathColumnType
        + ",dump_format " + dumpFormatColumnType + ",checkpoint_name "
        + checkpointNameColumnType + ",backend_name " + backendNameColumnType
        + "," + dumpTableTablesColumnName + " " + tablesColumnType
        + extraStatement + ")";
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("recovery.jdbc.dumptable.statement",
          dumpTableCreateStatement));
  }

  /**
   * Sets the log table name and create statement.
   * 
   * @param createTable statement to create the table
   * @param tableName name of the log table
   * @param idType type of the id column
   * @param vloginType type of the login column
   * @param sqlName name of the sql statement column
   * @param sqlType type of the sql column
   * @param transactionIdType type of the transaction column
   * @param extraStatement extra statement like primary keys ...
   */
  public void setLogTableCreateStatement(String createTable, String tableName,
      String idType, String vloginType, String sqlName, String sqlType,
      String transactionIdType, String extraStatement)
  {
    this.logTableCreateTable = createTable;
    this.logTableName = tableName;
    this.logTableIdType = idType;
    this.logTableVloginType = vloginType;
    this.logTableSqlColumnName = sqlName;
    this.logTableSqlType = sqlType;
    this.logTableTransactionIdType = transactionIdType;
    this.logTableExtraStatement = extraStatement;
    logTableCreateStatement = createTable + " " + tableName + " (id " + idType
        + ",vlogin " + vloginType + "," + logTableSqlColumnName + " " + sqlType
        + ",transaction_id " + transactionIdType + extraStatement + ")";
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("recovery.jdbc.logtable.statement",
          logTableCreateStatement));
  }

  //
  //
  // Log utility functions
  //
  //

  /**
   * Recreate the skeleton of the query from the query itself with the param tag
   * 
   * @param sql the sql query as it is recorded in the recovery log
   * @return the sql skelton with '?' instead of the <?...|...?>tags
   * @see org.objectweb.cjdbc.driver.PreparedStatement#setWithTag(int, String,
   *      String)
   */
  private String recreateSkeleton(String sql)
  {
    // Here we have to rebuild the query skeleton to be able to call setXXX on
    // the PreparedStatement
    StringBuffer skeleton = new StringBuffer();
    int start = 0;
    int end;
    while ((end = sql.indexOf(
        org.objectweb.cjdbc.driver.PreparedStatement.START_PARAM_TAG, start)) != -1)
    {
      skeleton.append(sql.substring(start, end)).append('?');
      start = sql.indexOf(
          org.objectweb.cjdbc.driver.PreparedStatement.END_PARAM_TAG, end);
      if (start == -1)
        throw new RuntimeException("Malformed query in recovery log: " + sql);
      else
        start += org.objectweb.cjdbc.driver.PreparedStatement.END_PARAM_TAG
            .length();
    }
    if (start < sql.length())
      skeleton.append(sql.substring(start));
    return skeleton.toString();
  }

  /**
   * Set the driverProcessed flag of the given request according to its SQL
   * content and rebuild the SQL skeleton if necessary.
   * 
   * @param request Request to process
   */
  private void setDriverProcessedAndSkeleton(AbstractRequest request)
  {
    String sql = request.getSQL();
    boolean isDriverProcessed = sql
        .indexOf(org.objectweb.cjdbc.driver.PreparedStatement.END_PARAM_TAG) == -1;
    request.setDriverProcessed(isDriverProcessed);
    if (isDriverProcessed)
      return; // No need to set the skeleton

    request.setSqlSkeleton(recreateSkeleton(sql));
  }

  /**
   * Checks if a checkpoint with the name checkpointName is already stored in
   * the database.
   * 
   * @param checkpointName name of the checkpoint.
   * @return ResultSet empty if no checkpoint was found.
   */
  private boolean validCheckpointName(String checkpointName)
      throws SQLException
  {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = getDatabaseConnection().prepareStatement(
          "SELECT * FROM " + checkpointTableName + " WHERE name LIKE ?");
      stmt.setString(1, checkpointName);
      rs = stmt.executeQuery();

      // If the query returned any rows, the checkpoint name is already
      // in use and therefore invalid.
      boolean checkpointExists = rs.next();
      rs.close();
      return !checkpointExists;
    }
    catch (SQLException e)
    {
      invalidateInternalConnection();
      throw new SQLException(Translate.get(
          "recovery.jdbc.checkpoint.check.failed", e));
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (SQLException ignore)
      {
      }
    }
  }

  /**
   * Synchronizes with the loggerThread, waiting for log requests to be flushed
   * to the recovery log.
   */
  private void waitForLogQueueToEmpty()
  {
    synchronized (loggerThread)
    {
      while (!loggerThread.getLogQueueIsEmpty())
      {
        try
        {
          loggerThread.wait();
        }
        catch (Exception e)
        {
          logger.warn("Exception " + e
              + " while waiting for end of transactions");
        }
      }
    }
  }

  //
  //
  // Info/Monitoring/Debug related functions
  //
  //

  /**
   * @see org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean#getAssociatedString()
   */
  public String getAssociatedString()
  {
    return "jdbcrecoverylog";
  }

  /**
   * Allow to get the content of the recovery log for viewing
   * 
   * @return <code>String[][]</code>
   * @see org.objectweb.cjdbc.console.views.RecoveryLogViewer
   */
  public String[][] getData()
  {
    Statement stmt = null;
    ResultSet rs = null;
    try
    {
      stmt = getDatabaseConnection().createStatement();
      rs = stmt.executeQuery("select * from " + logTableName);
      ArrayList list = new ArrayList();
      while (rs.next())
      {
        // 3: Query 2: User 1: ID 4: TID
        list.add(new String[]{rs.getString(3), rs.getString(2),
            rs.getString(1), rs.getString(4)});
      }
      String[][] result = new String[list.size()][4];
      for (int i = 0; i < list.size(); i++)
        result[i] = (String[]) list.get(i);
      return result;
    }
    catch (SQLException e)
    {
      return null;
    }
    finally
    {
      try
      {
        rs.close();
      }
      catch (SQLException ignore)
      {
      }
      try
      {
        stmt.close();
      }
      catch (SQLException ignore)
      {
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.recoverylog.AbstractRecoveryLog#getXmlImpl()
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_RecoveryLog + " "
        + DatabasesXmlTags.ATT_driver + "=\"" + driverClassName + "\" "
        + DatabasesXmlTags.ATT_url + "=\"" + url + "\" ");
    if (driverName != null)
    {
      info.append(DatabasesXmlTags.ATT_driverPath + "=\"" + driverName + "\" ");
    }
    info.append(DatabasesXmlTags.ATT_login + "=\"" + login + "\" "
        + DatabasesXmlTags.ATT_password + "=\"" + password + "\" "
        + DatabasesXmlTags.ATT_requestTimeout + "=\"" + (timeout / 1000)
        + "\" " + DatabasesXmlTags.ATT_recoveryBatchSize + "=\""
        + recoveryBatchSize + "\">");
    // Recovery Log table
    info.append("<" + DatabasesXmlTags.ELT_RecoveryLogTable + " "
        + DatabasesXmlTags.ATT_createTable + "=\"" + logTableCreateTable
        + "\" " + DatabasesXmlTags.ATT_tableName + "=\"" + logTableName + "\" "
        + DatabasesXmlTags.ATT_idColumnType + "=\"" + logTableIdType + "\" "
        + DatabasesXmlTags.ATT_vloginColumnType + "=\"" + logTableVloginType
        + "\" " + DatabasesXmlTags.ATT_sqlColumnType + "=\"" + logTableSqlType
        + "\" " + DatabasesXmlTags.ATT_transactionIdColumnType + "=\""
        + logTableTransactionIdType + "\" "
        + DatabasesXmlTags.ATT_extraStatementDefinition + "=\""
        + logTableExtraStatement + "\"/>");
    // Checkpoint table
    info.append("<" + DatabasesXmlTags.ELT_CheckpointTable + " "
        + DatabasesXmlTags.ATT_createTable + "=\"" + checkpointTableCreateTable
        + "\" " + DatabasesXmlTags.ATT_tableName + "=\"" + checkpointTableName
        + "\" " + DatabasesXmlTags.ATT_checkpointNameColumnType + "=\""
        + checkpointTableNameType + "\" "
        + DatabasesXmlTags.ATT_requestIdColumnType + "=\""
        + checkpointTableRequestIdType + "\" "
        + DatabasesXmlTags.ATT_extraStatementDefinition + "=\""
        + checkpointTableExtraStatement + "\"" + "/>");
    // BackendLog table
    info.append("<" + DatabasesXmlTags.ELT_BackendTable + " "
        + DatabasesXmlTags.ATT_createTable + "=\"" + backendTableCreateTable
        + "\" " + DatabasesXmlTags.ATT_tableName + "=\"" + backendTableName
        + "\" " + DatabasesXmlTags.ATT_databaseNameColumnType + "=\""
        + backendTableDatabaseName + "\" "
        + DatabasesXmlTags.ATT_backendNameColumnType + "=\""
        + backendTableBackendName + "\" "
        + DatabasesXmlTags.ATT_backendStateColumnType + "=\""
        + backendTableBackendState + "\" "
        + DatabasesXmlTags.ATT_checkpointNameColumnType + "=\""
        + backendTableCheckpointName + "\" "
        + DatabasesXmlTags.ATT_extraStatementDefinition + "=\""
        + backendTableExtraStatement + "\"" + "/>");
    // Dump table
    info.append("<" + DatabasesXmlTags.ELT_DumpTable + " "
        + DatabasesXmlTags.ATT_createTable + "=\"" + dumpTableCreateTable
        + "\" " + DatabasesXmlTags.ATT_tableName + "=\"" + dumpTableName
        + "\" " + DatabasesXmlTags.ATT_dumpNameColumnType + "=\""
        + dumpTableDumpNameColumnType + "\" "
        + DatabasesXmlTags.ATT_dumpDateColumnType + "=\""
        + dumpTableDumpDateColumnType + "\" "
        + DatabasesXmlTags.ATT_dumpPathColumnType + "=\""
        + dumpTableDumpPathColumnType + "\" "
        + DatabasesXmlTags.ATT_dumpFormatColumnType + "=\""
        + dumpTableDumpFormatColumnType + "\" "
        + DatabasesXmlTags.ATT_checkpointNameColumnType + "=\""
        + dumpTableCheckpointNameColumnType + "\" "
        + DatabasesXmlTags.ATT_backendNameColumnType + "=\""
        + dumpTableBackendNameColumnType + "\" "
        + DatabasesXmlTags.ATT_tablesColumnName + "=\""
        + dumpTableTablesColumnName + "\" "
        + DatabasesXmlTags.ATT_tablesColumnType + "=\""
        + dumpTableTablesColumnType + "\" "
        + DatabasesXmlTags.ATT_extraStatementDefinition + "=\""
        + dumpTableExtraStatementDefinition + "\"" + "/>");
    info.append("</" + DatabasesXmlTags.ELT_RecoveryLog + ">");

    return info.toString();
  }

  /**
   * @see StoreDumpCheckpointEvent
   * @param dumpCheckpointName name of the checkpoint to store
   * @param checkpointId id of the checkpoint
   */
  public void storeDumpCheckpointName(String dumpCheckpointName,
      long checkpointId)
  {
    loggerThread.log(new StoreDumpCheckpointEvent(dumpCheckpointName,
        checkpointId));
  }

}