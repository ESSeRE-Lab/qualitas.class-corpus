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
 * Contributor(s): Mathieu Peltier, Nicolas Modrzyk, Vadim Kassin, Olivier Fambon, Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.controller.virtualdatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.objectweb.cjdbc.common.exceptions.BackupException;
import org.objectweb.cjdbc.common.exceptions.VirtualDatabaseException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.JmxConstants;
import org.objectweb.cjdbc.common.jmx.JmxException;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.common.jmx.notifications.CjdbcNotificationList;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.monitor.backend.BackendStatistics;
import org.objectweb.cjdbc.common.shared.BackendInfo;
import org.objectweb.cjdbc.common.shared.BackendState;
import org.objectweb.cjdbc.common.shared.DumpInfo;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.users.AdminUser;
import org.objectweb.cjdbc.common.users.VirtualDatabaseUser;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.common.util.ReadPrioritaryFIFOWriteLock;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;
import org.objectweb.cjdbc.common.xml.XmlTools;
import org.objectweb.cjdbc.controller.authentication.AuthenticationManager;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.backup.Backuper;
import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.core.shutdown.VirtualDatabaseForceShutdownThread;
import org.objectweb.cjdbc.controller.core.shutdown.VirtualDatabaseSafeShutdownThread;
import org.objectweb.cjdbc.controller.core.shutdown.VirtualDatabaseShutdownThread;
import org.objectweb.cjdbc.controller.core.shutdown.VirtualDatabaseWaitShutdownThread;
import org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean;
import org.objectweb.cjdbc.controller.jmx.MBeanServerManager;
import org.objectweb.cjdbc.controller.jmx.RmiConnector;
import org.objectweb.cjdbc.controller.loadbalancer.AllBackendsFailedException;
import org.objectweb.cjdbc.controller.monitoring.SQLMonitoring;
import org.objectweb.cjdbc.controller.recoverylog.BackendRecoveryInfo;
import org.objectweb.cjdbc.controller.recoverylog.RecoverThread;
import org.objectweb.cjdbc.controller.recoverylog.RecoveryLog;
import org.objectweb.cjdbc.controller.requestmanager.RAIDbLevels;
import org.objectweb.cjdbc.controller.requestmanager.RequestManager;

/**
 * A <code>VirtualDatabase</code> represents a database from client point of
 * view and hide the complexity of the cluster distribution to the client. The
 * client always uses the virtual database name and the C-JDBC Controller will
 * use the real connections when an SQL request comes in.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:vadim@kase.kz">Vadim Kassin </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class VirtualDatabase extends AbstractStandardMBean
    implements
      VirtualDatabaseMBean,
      XmlComponent
{
  private static final long                serialVersionUID      = 1399418136380336827L;

  //
  // How the code is organized ?
  //
  // 1. Member variables
  // 2. Constructor(s)
  // 3. Request handling
  // 4. Transaction handling
  // 5. Database backend management
  // 6. Checkpoint management
  // 7. Getter/Setter (possibly in alphabetical order)
  // 8. Shutdown
  //

  /** Virtual database name */
  protected String                         name;

  /**
   * Authentification manager matching virtual database login/password to
   * backends login/password
   */
  protected AuthenticationManager          authenticationManager;

  /** <code>ArrayList</code> of <code>DatabaseBackend</code> objects */
  protected ArrayList                      backends;

  /** Read/Write lock for backend list */
  protected ReadPrioritaryFIFOWriteLock    rwLock;

  /** The request manager to use for this database */
  protected RequestManager                 requestManager;

  /** ArrayList to store the order of requests */
  protected LinkedList                     totalOrderQueue       = null;

  /** Virtual database logger */
  public Trace                             logger                = null;
  protected Trace                          requestLogger         = null;

  // List of current active Worker Threads
  private ArrayList                        activeThreads         = new ArrayList();
  // List of current idle Worker Threads
  private int                              idleThreads           = 0;
  // List of current pending connections (Socket objects)
  private ArrayList                        pendingConnections    = new ArrayList();

  /** Maximum number of concurrent accepted for this virtual database */
  protected int                            maxNbOfConnections;

  /** If false one worker thread is forked per connection else */
  protected boolean                        poolConnectionThreads;

  /** Maximum time a worker thread can remain idle before dying */
  protected long                           maxThreadIdleTime;

  /**
   * Minimum number of worker threads to keep in the pool if
   * poolConnectionThreads is true
   */
  protected int                            minNbOfThreads;

  /** Maximum number of worker threads to fork */
  protected int                            maxNbOfThreads;

  /** Current number of worker threads */
  protected int                            currentNbOfThreads;

  /** Virtual Database MetaData */
  protected VirtualDatabaseDynamicMetaData metadata;
  private VirtualDatabaseStaticMetaData    staticMetadata;

  private SQLMonitoring                    sqlMonitor            = null;

  /** Use for method getAndCheck */
  public static final int                  CHECK_BACKEND_ENABLE  = 1;
  /** Use for method getAndCheck */
  public static final int                  CHECK_BACKEND_DISABLE = 0;
  /** Use for method getAndCheck */
  public static final int                  NO_CHECK_BACKEND      = -1;

  /** Short form of SQL statements to include in traces and exceptions */
  private int                              sqlShortFormLength;

  /** The filter used to store blobs in the database */
  private AbstractBlobFilter               blobFilter;

  /** The controller we belong to */
  Controller                               controller;

  /** Comma separated list of database product names (one instance per name) */
  private String                           databaseProductNames  = "C-JDBC";

  /** Marker to see if the database is shutting down */
  private boolean                          shuttingDown          = false;

  /* Constructors */

  /**
   * Creates a new <code>VirtualDatabase</code> instance.
   * 
   * @param name the virtual database name.
   * @param maxConnections maximum number of concurrent connections.
   * @param pool should we use a pool of threads for handling connections?
   * @param minThreads minimum number of threads in the pool
   * @param maxThreads maximum number of threads in the pool
   * @param maxThreadIdleTime maximum time a thread can remain idle before being
   *          removed from the pool.
   * @param sqlShortFormLength maximum number of characters of an SQL statement
   *          to diplay in traces or exceptions
   * @param blobFilter encoding method for blobs
   * @param controller the controller we belong to
   * @exception NotCompliantMBeanException in case the bean does not comply with
   *              jmx
   * @exception JmxException could not register mbean
   */
  public VirtualDatabase(Controller controller, String name,
      int maxConnections, boolean pool, int minThreads, int maxThreads,
      long maxThreadIdleTime, int sqlShortFormLength,
      AbstractBlobFilter blobFilter) throws NotCompliantMBeanException,
      JmxException
  {
    super(VirtualDatabaseMBean.class);
    this.controller = controller;
    this.name = name;
    this.maxNbOfConnections = maxConnections;
    this.poolConnectionThreads = pool;
    this.minNbOfThreads = minThreads;
    this.maxNbOfThreads = maxThreads;
    this.maxThreadIdleTime = maxThreadIdleTime;
    this.sqlShortFormLength = sqlShortFormLength;
    this.blobFilter = blobFilter;
    backends = new ArrayList();

    ObjectName objectName = JmxConstants.getVirtualDbObjectName(name);
    MBeanServerManager.registerMBean(this, objectName);

    rwLock = new ReadPrioritaryFIFOWriteLock();
    logger = Trace.getLogger("org.objectweb.cjdbc.controller.virtualdatabase."
        + name);
    requestLogger = Trace
        .getLogger("org.objectweb.cjdbc.controller.virtualdatabase.request."
            + name);
  }

  /**
   * Acquires a read lock on the backend lists (both enabled and disabled
   * backends). This should be called prior traversing the backend
   * <code>ArrayList</code>.
   * 
   * @throws InterruptedException if an error occurs
   */
  public final void acquireReadLockBackendLists() throws InterruptedException
  {
    rwLock.acquireRead();
  }

  /**
   * Releases the read lock on the backend lists (both enabled and disabled
   * backends). This should be called after traversing the backend
   * <code>ArrayList</code>.
   */
  public final void releaseReadLockBackendLists()
  {
    rwLock.releaseRead();
  }

  /**
   * Is this virtual database distributed ?
   * 
   * @return false
   */
  public boolean isDistributed()
  {
    return false;
  }

  /* Request Handling */

  /**
   * Checks if a given virtual login/password is ok.
   * 
   * @param virtualLogin the virtual user login
   * @param virtualPassword the virtual user password
   * @return <code>true</code> if the login/password is known from the
   *         <code>AuthenticationManager</code>. Returns <code>false</code>
   *         if no <code>AuthenticationManager</code> is defined.
   */
  public boolean checkUserAuthentication(String virtualLogin,
      String virtualPassword)
  {
    if (authenticationManager == null)
    {
      logger.error("No authentification manager defined to check login '"
          + virtualLogin + "'");
      return false;
    }
    else
      return authenticationManager.isValidVirtualUser(new VirtualDatabaseUser(
          virtualLogin, virtualPassword));
  }

  /**
   * Checks if a given admin login/password is ok.
   * 
   * @param adminLogin admin user login
   * @param adminPassword admin user password
   * @return <code>true</code> if the login/password is known from the
   *         <code>AuthenticationManager</code>. Returns <code>false</code>
   *         if no <code>AuthenticationManager</code> is defined.
   */
  public boolean checkAdminAuthentication(String adminLogin,
      String adminPassword)
  {
    if (authenticationManager == null)
    {
      logger.error("No authentification manager defined to check admin login '"
          + adminLogin + "'");
      return false;
    }
    else
      return authenticationManager.isValidAdminUser(new AdminUser(adminLogin,
          adminPassword));
  }

  /**
   * Performs a read request and returns the reply.
   * 
   * @param request the request to execute
   * @return a <code>ControllerResultSet</code> value
   * @exception SQLException if the request fails
   */
  public ControllerResultSet execReadRequest(SelectRequest request)
      throws SQLException
  {
    if (request == null)
    {
      String msg = "Request failed (null read request received)";
      logger.warn(msg);
      throw new SQLException(msg);
    }

    try
    {
      if (requestLogger.isInfoEnabled())
        requestLogger.info("S " + request.getTransactionId() + " "
            + request.getSQL());

      long start = 0;
      if (sqlMonitor != null && sqlMonitor.isActive())
        start = System.currentTimeMillis();

      ControllerResultSet rs = requestManager.execReadRequest(request);

      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logRequestTime(request, System.currentTimeMillis() - start);

      return rs;
    }
    catch (SQLException e)
    {
      String msg = "Request '" + request.getId() + "' failed ("
          + e.getMessage() + ")";
      logger.warn(msg);
      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logError(request);
      throw e;
    }
  }

  /**
   * Performs a write request and returns the number of rows affected.
   * 
   * @param request the request to execute
   * @return number of rows affected
   * @exception SQLException if the request fails
   */
  public int execWriteRequest(AbstractWriteRequest request) throws SQLException
  {
    if (request == null)
    {
      String msg = "Request failed (null write request received)";
      logger.warn(msg);
      throw new SQLException(msg);
    }

    try
    {
      if (requestLogger.isInfoEnabled())
        requestLogger.info("W " + request.getTransactionId() + " "
            + request.getSQL());

      long start = 0;
      if (sqlMonitor != null && sqlMonitor.isActive())
        start = System.currentTimeMillis();

      int result = requestManager.execWriteRequest(request);

      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logRequestTime(request, System.currentTimeMillis() - start);

      return result;
    }
    catch (SQLException e)
    {
      String msg = "Request '" + request.getId() + "' failed ("
          + e.getMessage() + ")";
      logger.warn(msg);
      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logError(request);
      throw e;
    }
  }

  /**
   * Performs a write request and returns the auto generated keys.
   * 
   * @param request the request to execute
   * @return auto generated keys
   * @exception SQLException if the request fails
   */
  public ControllerResultSet execWriteRequestWithKeys(
      AbstractWriteRequest request) throws SQLException
  {
    if (request == null)
    {
      String msg = "Request failed (null write request received)";
      logger.warn(msg);
      throw new SQLException(msg);
    }

    try
    {
      if (requestLogger.isInfoEnabled())
        requestLogger.info("W " + request.getTransactionId() + " "
            + request.getSQL());

      long start = 0;
      if (sqlMonitor != null && sqlMonitor.isActive())
        start = System.currentTimeMillis();

      ControllerResultSet result = requestManager
          .execWriteRequestWithKeys(request);

      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logRequestTime(request, System.currentTimeMillis() - start);

      return result;
    }
    catch (SQLException e)
    {
      String msg = "Request '" + request.getId() + "' failed ("
          + e.getMessage() + ")";
      logger.warn(msg);
      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logError(request);
      throw e;
    }
  }

  /**
   * Call a stored procedure that returns a ResultSet.
   * 
   * @param proc the stored procedure call
   * @return a <code>java.sql.ResultSet</code> value
   * @exception SQLException if an error occurs
   */
  public ControllerResultSet execReadStoredProcedure(StoredProcedure proc)
      throws SQLException
  {
    if (proc == null)
    {
      String msg = "Request failed (null stored procedure received)";
      logger.warn(msg);
      throw new SQLException(msg);
    }

    try
    {
      if (requestLogger.isInfoEnabled())
        requestLogger
            .info("S " + proc.getTransactionId() + " " + proc.getSQL());

      long start = 0;
      if (sqlMonitor != null && sqlMonitor.isActive())
        start = System.currentTimeMillis();

      ControllerResultSet rs = requestManager.execReadStoredProcedure(proc);

      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logRequestTime(proc, System.currentTimeMillis() - start);

      return rs;
    }
    catch (AllBackendsFailedException e)
    {
      String msg = Translate.get(
          "loadbalancer.storedprocedure.failed.on.all.backends", new String[]{
              String.valueOf(proc.getId()), e.getMessage()});
      logger.warn(msg);
      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logError(proc);
      throw new SQLException(msg);
    }
    catch (SQLException e)
    {
      String msg = Translate.get("loadbalancer.storedprocedure.failed",
          new String[]{String.valueOf(proc.getId()), e.getMessage()});
      logger.warn(msg);
      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logError(proc);
      throw e;
    }
  }

  /**
   * Call a stored procedure that performs an update.
   * 
   * @param proc the stored procedure call
   * @return number of rows affected
   * @exception SQLException if an error occurs
   */
  protected int execWriteStoredProcedure(StoredProcedure proc)
      throws SQLException
  {
    if (proc == null)
    {
      String msg = "Request failed (null stored procedure received)";
      logger.warn(msg);
      throw new SQLException(msg);
    }

    try
    {
      if (requestLogger.isInfoEnabled())
        requestLogger
            .info("W " + proc.getTransactionId() + " " + proc.getSQL());

      long start = 0;
      if (sqlMonitor != null && sqlMonitor.isActive())
        start = System.currentTimeMillis();

      int result = requestManager.execWriteStoredProcedure(proc);

      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logRequestTime(proc, System.currentTimeMillis() - start);

      return result;
    }
    catch (AllBackendsFailedException e)
    {
      String msg = Translate.get(
          "loadbalancer.storedprocedure.failed.on.all.backends", new String[]{
              String.valueOf(proc.getId()), e.getMessage()});
      logger.warn(msg);
      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logError(proc);
      throw new SQLException(msg);
    }
    catch (SQLException e)
    {
      String msg = Translate.get("loadbalancer.storedprocedure.failed",
          new String[]{String.valueOf(proc.getId()), e.getMessage()});
      logger.warn(msg);
      if (sqlMonitor != null && sqlMonitor.isActive())
        sqlMonitor.logError(proc);
      throw e;
    }
  }

  /* Transaction management */

  /**
   * Begins a new transaction and returns the corresponding transaction
   * identifier. This method is called from the driver when
   * {@link org.objectweb.cjdbc.driver.Connection#setAutoCommit(boolean)}is
   * called with <code>false</code> argument.
   * <p>
   * Note that the transaction begin is not logged in the recovery log by this
   * method, you will have to call logLazyTransactionBegin.
   * 
   * @param login the login used by the connection
   * @return an unique transaction identifier
   * @exception SQLException if an error occurs
   * @see RequestManager#logLazyTransactionBegin(long)
   */
  public long begin(String login) throws SQLException
  {
    try
    {
      long tid = requestManager.begin(login);
      if (requestLogger.isInfoEnabled())
        requestLogger.info("B " + tid);
      return tid;
    }
    catch (SQLException e)
    {
      String msg = "Begin failed (" + e.getMessage() + ")";
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * Abort a transaction that has been started but in which no query was
   * executed. As we use lazy transaction begin, there is no need to rollback
   * such transaction but just to cleanup the metadata associated with this not
   * effectively started transaction.
   * 
   * @param transactionId id of the transaction to abort
   * @param logAbort true if the abort (in fact rollback) should be logged in
   *          the recovery log
   * @throws SQLException if an error occurs
   */
  public void abort(long transactionId, boolean logAbort) throws SQLException
  {
    requestManager.abort(transactionId, logAbort);
    // Emulate this as a rollback for the RequestPlayer
    if (requestLogger.isInfoEnabled())
      requestLogger.info("R " + transactionId);
  }

  /**
   * Commits a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @param logCommit true if the commit should be logged in the recovery log
   * @exception SQLException if an error occurs
   */
  public void commit(long transactionId, boolean logCommit) throws SQLException
  {
    try
    {
      if (requestLogger.isInfoEnabled())
        requestLogger.info("C " + transactionId);
      requestManager.commit(transactionId, logCommit);
    }
    catch (SQLException e)
    {
      String msg = "Commit of transaction '" + transactionId + "' failed ("
          + e.getMessage() + ")";
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * Rollbacks a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @param logRollback true if the rollback should be logged in the recovery
   *          log
   * @exception SQLException if an error occurs
   */
  public void rollback(long transactionId, boolean logRollback)
      throws SQLException
  {
    try
    {
      if (requestLogger.isInfoEnabled())
        requestLogger.info("R " + transactionId);
      requestManager.rollback(transactionId, logRollback);
    }
    catch (SQLException e)
    {
      String msg = "Rollback of transaction '" + transactionId + "' failed ("
          + e.getMessage() + ")";
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * Rollbacks a transaction given its id to a savepoint given its name
   * 
   * @param transactionId the transaction id
   * @param savepointName the name of the savepoint
   * @exception SQLException if an error occurs
   */
  public void rollback(long transactionId, String savepointName)
      throws SQLException
  {
    try
    {
      if (requestLogger.isInfoEnabled())
        requestLogger.info("R " + transactionId + " " + savepointName);
      requestManager.rollback(transactionId, savepointName);
    }
    catch (SQLException e)
    {
      String msg = "Rollback to savepoint '" + savepointName + "' for "
          + "transaction '" + transactionId + "' failed (" + e.getMessage()
          + ")";
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * Sets a unnamed savepoint to a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @return the savepoint id
   * @exception SQLException if an error occurs
   */
  public int setSavepoint(long transactionId) throws SQLException
  {
    try
    {
      int savepointId = requestManager.setSavepoint(transactionId);
      if (requestLogger.isInfoEnabled())
        requestLogger.info("P " + transactionId + " " + savepointId);
      return savepointId;
    }
    catch (SQLException e)
    {
      String msg = "Setting unnamed savepoint to transaction '" + transactionId
          + "' failed (" + e.getMessage() + ")";
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * Sets a savepoint given its desired name to a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @param name the desired name of the savepoint
   * @exception SQLException if an error occurs
   */
  public void setSavepoint(long transactionId, String name) throws SQLException
  {
    try
    {
      if (requestLogger.isInfoEnabled())
        requestLogger.info("P " + transactionId + " " + name);
      requestManager.setSavepoint(transactionId, name);
    }
    catch (SQLException e)
    {
      String msg = "Setting savepoint with name '" + name + "' to transaction "
          + "'" + transactionId + "' failed (" + e.getMessage() + ")";
      logger.warn(msg);
      throw e;
    }
  }

  /**
   * Releases a savepoint given its name from a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @param name the name of the savepoint
   * @exception SQLException if an error occurs
   */
  public void releaseSavepoint(long transactionId, String name)
      throws SQLException
  {
    try
    {
      if (requestLogger.isInfoEnabled())
        requestLogger.info("F " + transactionId + " " + name);
      requestManager.releaseSavepoint(transactionId, name);
    }
    catch (SQLException e)
    {
      String msg = "Releasing savepoint with name '" + name + "' from "
          + "transaction '" + transactionId + "' failed (" + e.getMessage()
          + ")";
      logger.warn(msg);
      throw e;
    }
  }

  //
  // Database backends management
  //

  /**
   * Add a backend to this virtual database.
   * 
   * @param db the database backend to add
   * @throws VirtualDatabaseException if an error occurs
   */
  public void addBackend(DatabaseBackend db) throws VirtualDatabaseException
  {
    this.addBackend(db, true);
  }

  /**
   * Add a backend to this virtual database.
   * 
   * @param db the database backend to add
   * @param checkForCompliance should load the driver ?
   * @throws VirtualDatabaseException if an error occurs
   */
  public void addBackend(DatabaseBackend db, boolean checkForCompliance)
      throws VirtualDatabaseException
  {
    if (db == null)
    {
      String msg = "Illegal null database backend in addBackend(DatabaseBackend) method";
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    if (db.isReadEnabled())
    {
      String msg = "It is not allowed to add an enabled database.";
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    // Get the lock on the list of backends
    try
    {
      rwLock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    // Check that the backend is not already up
    if (backends.indexOf(db) != -1)
    {
      rwLock.releaseWrite();
      String msg = "Duplicate backend " + db.getURL();
      logger.warn(msg);
      throw new VirtualDatabaseException(msg);
    }

    // Check the authentication manager has all virtual logins defined
    ArrayList logins = authenticationManager.getVirtualLogins();
    VirtualDatabaseUser vdu;
    String login;
    for (int i = 0; i < logins.size(); i++)
    {
      vdu = (VirtualDatabaseUser) logins.get(i);
      login = vdu.getLogin();
      if (db.getConnectionManager(login) == null)
      {
        rwLock.releaseWrite();
        throw new VirtualDatabaseException(Translate.get(
            "backend.missing.connection.manager", login));
      }
    }

    // Initialize the driver and check the compliance
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Checking driver compliance");
      if (checkForCompliance)
        db.checkDriverCompliance(); // Also loads the driver
    }
    catch (Exception e)
    {
      rwLock.releaseWrite();
      String msg = "Error while adding database backend " + db.getName() + " ("
          + e + ")";
      logger.warn(msg);
      throw new VirtualDatabaseException(msg);
    }

    db.setSqlShortFormLength(getSQLShortFormLength());

    // Add the backend to the list
    backends.add(db);
    if (logger.isDebugEnabled())
      logger.debug("Backend " + db.getName() + " added successfully");

    // Set the backend state listener so that the state is logged into the
    // recovery log - if any. When there is no recovery log,
    // getBackendStateListener returns null, and stateListener is consequently
    // set to null. Looks like the only state listner ever is the recoveryLog.
    /*
     * Note: getRequestManager() is null, at load time, thus the test. At load
     * time, if there is a recovery log, the state listner eventually gets set,
     * but in a different fashion: it is set by RequestManager c'tor, when
     * calling setRecoveryLog().
     */
    if (getRequestManager() != null)
    {
      db.setStateListener(getRequestManager().getBackendStateListener());
    }

    // Release the lock
    rwLock.releaseWrite();

    // Notify Jmx listeners of the backend addition
    if (MBeanServerManager.isJmxEnabled())
    {
      // Send notification
      Hashtable data = new Hashtable();
      data.put(CjdbcNotificationList.DATA_DATABASE, this.name);
      data.put(CjdbcNotificationList.DATA_DRIVER, db.getDriverClassName());
      String checkpoint = db.getLastKnownCheckpoint();
      checkpoint = (checkpoint == null) ? "" : checkpoint;
      data.put(CjdbcNotificationList.DATA_CHECKPOINT, checkpoint);
      data.put(CjdbcNotificationList.DATA_NAME, db.getName());
      data.put(CjdbcNotificationList.DATA_URL, db.getURL());
      RmiConnector.broadcastNotification(this,
          CjdbcNotificationList.VIRTUALDATABASE_BACKEND_ADDED,
          CjdbcNotificationList.NOTIFICATION_LEVEL_INFO, Translate.get(
              "notification.backend.added", db.getName()), data);

      // Add backend mbean to jmx server
      ObjectName objectName = JmxConstants.getDatabaseBackendObjectName(name,
          db.getName());
      try
      {
        MBeanServerManager.registerMBean(db, objectName);
      }
      catch (JmxException e1)
      {
        logger.error(Translate.get(
            "virtualdatabase.fail.register.backend.mbean", db.getName()), e1);
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#forceDisableBackend(String)
   */
  public void forceDisableBackend(String backendName)
      throws VirtualDatabaseException
  {
    try
    {
      DatabaseBackend db = getAndCheckBackend(backendName,
          CHECK_BACKEND_DISABLE);
      requestManager.disableBackend(db);
      requestManager.setDatabaseSchema(
          getDatabaseSchemaFromActiveBackendsAndRefreshDatabaseProductNames(),
          false);

      // Send notification
      if (MBeanServerManager.isJmxEnabled())
      {
        Hashtable data = new Hashtable();
        data.put("driver", db.getDriverClassName());
        String checkpoint = db.getLastKnownCheckpoint();
        checkpoint = (checkpoint == null) ? "" : checkpoint;
        data.put("checkpoint", checkpoint);
        data.put("name", db.getName());
        data.put("url", db.getURL());
        RmiConnector.broadcastNotification(this,
            CjdbcNotificationList.VIRTUALDATABASE_BACKEND_DISABLED,
            CjdbcNotificationList.NOTIFICATION_LEVEL_INFO, Translate.get(
                "notification.backend.disabled", db.getName()), data);
      }
    }
    catch (Exception e)
    {
      logger.error("An error occured while disabling backend " + backendName
          + " (" + e + ")");
      throw new VirtualDatabaseException(e.getMessage());
    }
  }

  /**
   * Prepare this virtual database for shutdown. This turns off all the backends
   * by cutting communication from this database. This does not prevents other
   * virtual database to use shared backends. This doesn't create checkpoints
   * either.
   * 
   * @throws VirtualDatabaseException if an error occurs
   */
  public void disableAllBackends() throws VirtualDatabaseException
  {
    try
    {
      int size = this.backends.size();
      DatabaseBackend dbe;
      for (int i = 0; i < size; i++)
      {
        dbe = (DatabaseBackend) backends.get(i);
        if (dbe.isReadEnabled())
          requestManager.disableBackend(getAndCheckBackend(dbe.getName(),
              CHECK_BACKEND_DISABLE));
      }
    }
    catch (Exception e)
    {
      throw new VirtualDatabaseException(e.getMessage());
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#disableBackendWithCheckpoint(String)
   */
  public void disableBackendWithCheckpoint(String backendName)
      throws VirtualDatabaseException
  {
    try
    {
      requestManager.disableBackendForCheckpoint(getAndCheckBackend(
          backendName, CHECK_BACKEND_DISABLE), "disable_backend_" + backendName
          + "_" + new Date(System.currentTimeMillis()).toString());
      requestManager.setDatabaseSchema(
          getDatabaseSchemaFromActiveBackendsAndRefreshDatabaseProductNames(),
          false);
    }
    catch (Exception e)
    {
      logger.error("An error occured while disabling backend " + backendName
          + " (" + e + ")");
      throw new VirtualDatabaseException(e.getMessage());
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#disableAllBackendsWithCheckpoint(java.lang.String)
   */
  public void disableAllBackendsWithCheckpoint(String checkpoint)
      throws VirtualDatabaseException
  {
    if (checkpoint == null)
    {
      disableAllBackends();
      return;
    }

    try
    {
      this.acquireReadLockBackendLists();
      requestManager.disableBackendsForCheckpoint(backends, checkpoint);
      this.releaseReadLockBackendLists();
    }
    catch (Exception e)
    {
      throw new VirtualDatabaseException(e.getMessage());
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#forceEnableBackend(String)
   */
  public void forceEnableBackend(String backendName)
      throws VirtualDatabaseException
  {
    // Call the Request Manager
    try
    {
      DatabaseBackend backend = getAndCheckBackend(backendName,
          CHECK_BACKEND_ENABLE);

      requestManager.enableBackend(backend);
      requestManager.setSchemaIsDirty(true);

      // Update the list of database product names
      if (databaseProductNames.indexOf(backend.getDatabaseProductName()) == -1)
        databaseProductNames += "," + backend.getDatabaseProductName();

      // Update the static metadata
      getStaticMetaData().gatherStaticMetadata(backend);

      // Send notification
      if (MBeanServerManager.isJmxEnabled())
      {
        Hashtable data = new Hashtable();
        data.put("driver", backend.getDriverClassName());
        String checkpoint = backend.getLastKnownCheckpoint();
        checkpoint = (checkpoint == null) ? "" : checkpoint;
        data.put("checkpoint", checkpoint);
        data.put("name", backend.getName());
        data.put("url", backend.getURL());
        RmiConnector.broadcastNotification(this,
            CjdbcNotificationList.VIRTUALDATABASE_BACKEND_ENABLED,
            CjdbcNotificationList.NOTIFICATION_LEVEL_INFO, Translate.get(
                "notification.backend.enabled", backend.getName()), data);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new VirtualDatabaseException(e.getMessage());
    }
  }

  /**
   * Enable the given backend from the given checkpoint. This method returns
   * once the recovery is complete.
   * 
   * @param backendName backend to enable
   * @param checkpointName checkpoint to enable from
   * @throws VirtualDatabaseException if an error occurs
   */
  public void enableBackendFromCheckpoint(String backendName,
      String checkpointName) throws VirtualDatabaseException
  {
    // Call the Request Manager
    try
    {
      DatabaseBackend backend = getAndCheckBackend(backendName,
          CHECK_BACKEND_ENABLE);
      RecoverThread recoverThread = requestManager.enableBackendFromCheckpoint(
          backend, checkpointName);
      // Wait for recovery to complete
      recoverThread.join();
      requestManager.setSchemaIsDirty(true);

      // Update the static metadata
      getStaticMetaData().gatherStaticMetadata(backend);

      // Update the list of database product names
      if (databaseProductNames.indexOf(backend.getDatabaseProductName()) == -1)
        databaseProductNames += "," + backend.getDatabaseProductName();
    }
    catch (Exception e)
    {
      throw new VirtualDatabaseException(
          "Failed to enable backend from checkpoint: " + e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#enableBackendFromCheckpoint(java.lang.String)
   */
  public void enableBackendFromCheckpoint(String backendName)
      throws VirtualDatabaseException
  {
    DatabaseBackend backend = getAndCheckBackend(backendName, NO_CHECK_BACKEND);
    String checkpoint = backend.getLastKnownCheckpoint();
    if (checkpoint == null)
      throw new VirtualDatabaseException("No last checkpoint for backend:"
          + backendName);
    else
    {
      if (logger.isDebugEnabled())
        logger.debug("Enabling backend:" + backendName
            + " from its last checkpoint " + backend.getLastKnownCheckpoint());
    }
    enableBackendFromCheckpoint(backendName, backend.getLastKnownCheckpoint());
  }

  /**
   * Enable all the backends without any check.
   * 
   * @throws VirtualDatabaseException if fails
   */
  public void enableAllBackends() throws VirtualDatabaseException
  {
    try
    {
      int size = this.backends.size();
      DatabaseBackend dbe;
      for (int i = 0; i < size; i++)
      {
        dbe = (DatabaseBackend) backends.get(i);
        if (!dbe.isReadEnabled())
          forceEnableBackend(((DatabaseBackend) backends.get(i)).getName());
      }
    }
    catch (Exception e)
    {
      logger.error(e);
      throw new VirtualDatabaseException(e.getMessage());
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#enableAllBackendsFromCheckpoint()
   */
  public void enableAllBackendsFromCheckpoint() throws VirtualDatabaseException
  {
    RecoveryLog log = requestManager.getRecoveryLog();
    if (log == null)
    {// If no recovery log is defined ignore fallback to a forced enable
      logger
          .warn("No recovery log has been configured, enabling backend without checkpoint.");
      enableAllBackends();
    }
    else
    {
      try
      {
        int size = this.backends.size();
        DatabaseBackend dbe;
        String backendName;
        BackendRecoveryInfo info;
        for (int i = 0; i < size; i++)
        {
          dbe = (DatabaseBackend) backends.get(i);
          backendName = dbe.getName();
          info = log.getBackendRecoveryInfo(name, backendName);
          switch (info.getBackendState())
          {
            case BackendState.DISABLED :
              String checkpoint = info.getCheckpoint();
              if (checkpoint == null || checkpoint.equals(""))
              {
                logger.warn("Enabling backend " + backendName
                    + " with no checkpoint.");
                forceEnableBackend(dbe.getName());
              }
              else
              {
                logger.info("Enabling backend " + backendName
                    + " from checkpoint " + checkpoint);
                enableBackendFromCheckpoint(dbe.getName(), checkpoint);
              }
              continue;
            case BackendState.UNKNOWN :
              logger.info("Unknown last state for backend " + backendName
                  + ". Leaving node in "
                  + (dbe.isReadEnabled() ? "enabled" : "disabled") + " state.");
              continue;
            case BackendState.BACKUPING :
            case BackendState.DISABLING :
            case BackendState.RECOVERING :
            case BackendState.REPLAYING :
              if (!dbe.isReadEnabled())
              {
                logger.info("Unexpected transition state ("
                    + info.getBackendState() + ") for backend " + backendName
                    + ". Forcing backend to disabled state.");
                info.setBackendState(BackendState.DISABLED);
                log.storeBackendRecoveryInfo(name, info);
              }
              else
                logger.info("Unexpected transition state ("
                    + info.getBackendState() + ") for backend " + backendName
                    + ". Leaving backend in its current state.");
              continue;
            default :
              if (!dbe.isReadEnabled())
              {
                logger.info("Unexpected enabled state ("
                    + info.getBackendState() + ") for backend " + backendName
                    + ". Forcing backend to disabled state.");
                info.setBackendState(BackendState.DISABLED);
                log.storeBackendRecoveryInfo(name, info);
              }
              else
                logger.info("Unexpected enabled state ("
                    + info.getBackendState() + ") for backend " + backendName
                    + ". Leaving backend in its current state.");
              continue;
          }
        }
      }
      catch (Exception e)
      {
        throw new VirtualDatabaseException(e.getMessage());
      }
    }

  }

  /**
   * Prepare this virtual database for startup. This turns on all the backends
   * from the given checkpoint. If the checkpoint is null or an empty String,
   * the backends are enabled without further check else the backend states are
   * overriden to use the provided checkpoint.
   * 
   * @param checkpoint checkpoint for recovery log
   * @throws VirtualDatabaseException if fails
   */
  public void forceEnableAllBackendsFromCheckpoint(String checkpoint)
      throws VirtualDatabaseException
  {
    if (checkpoint == null || checkpoint.equals(""))
      enableAllBackends();
    else
    {
      try
      {
        int size = this.backends.size();
        DatabaseBackend backend;
        for (int i = 0; i < size; i++)
        {
          backend = (DatabaseBackend) backends.get(i);
          if (!backend.isReadEnabled())
          {
            backend.setLastKnownCheckpoint(checkpoint);
            enableBackendFromCheckpoint(backend.getName(), checkpoint);
          }
        }
      }
      catch (Exception e)
      {
        throw new VirtualDatabaseException(e.getMessage());
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#getAllBackendNames()
   */
  public ArrayList getAllBackendNames() throws VirtualDatabaseException
  {
    try
    {
      acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = "Unable to acquire read lock on backend list in getAllBackendNames ("
          + e + ")";
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    int size = backends.size();
    ArrayList result = new ArrayList();
    for (int i = 0; i < size; i++)
    {
      result.add(((DatabaseBackend) backends.get(i)).getName());
    }

    releaseReadLockBackendLists();
    return result;
  }

  /**
   * Find the DatabaseBackend corresponding to the given backend name and check
   * if it is possible to disable this backend. In the case enable, this method
   * also updates the virtual database schema by merging it with the one
   * provided by this backend.
   * 
   * @param backendName backend to look for
   * @param testEnable NO_CHECK_BACKEND no check is done, CHECK_BACKEND_DISABLE
   *          check if it is possible to disable the backend,
   *          CHECK_BACKEND_ENABLE check if it is possible to enable the backend
   * @return the backend to disable
   * @throws VirtualDatabaseException if an error occurs
   */
  public DatabaseBackend getAndCheckBackend(String backendName, int testEnable)
      throws VirtualDatabaseException
  {
    try
    {
      acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = "Unable to acquire read lock on backend list in getAndCheckBackend ("
          + e + ")";
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    // Find the backend
    int size = backends.size();
    DatabaseBackend b = null;
    for (int i = 0; i < size; i++)
    {
      b = (DatabaseBackend) backends.get(i);
      if (b.getName().equals(backendName))
        break;
      else
        b = null;
    }

    // Check not null
    if (b == null)
    {
      releaseReadLockBackendLists();
      String msg = "Trying to access a non-existing backend " + backendName;
      logger.warn(msg);
      throw new VirtualDatabaseException(msg);
    }

    // Check enable/disable
    switch (testEnable)
    {
      case NO_CHECK_BACKEND :
        break;
      case CHECK_BACKEND_DISABLE :
        if (!b.isReadEnabled())
        {
          releaseReadLockBackendLists();
          String msg = "Backend " + backendName + " is already disabled";
          logger.warn(msg);
          throw new VirtualDatabaseException(msg);
        }
        break;
      case CHECK_BACKEND_ENABLE :
        if (b.isReadEnabled())
        {
          releaseReadLockBackendLists();
          String msg = "Backend " + backendName + " is already enabled";
          logger.warn(msg);
          throw new VirtualDatabaseException(msg);
        }
        break;
      default :
        releaseReadLockBackendLists();
        String msg = "Unexpected parameter in getAndCheckBackend(...)";
        logger.error(msg);
        throw new VirtualDatabaseException(msg);
    }

    releaseReadLockBackendLists();

    if (testEnable == CHECK_BACKEND_ENABLE)
    {
      // Initialize backend for enable
      try
      {
        if (logger.isDebugEnabled())
          logger.debug("Initializing connections for backend " + b.getName());
        b.initializeConnections();

        b.checkDriverCompliance();

        if (logger.isDebugEnabled())
          logger.debug("Checking schema for backend " + b.getName());
        b.checkDatabaseSchema();

        DatabaseSchema backendSchema = b.getDatabaseSchema();

        if (backendSchema != null)
          requestManager.mergeDatabaseSchema(backendSchema);
        else
          logger.warn("Backend " + b.getName() + " has no defined schema.");
      }
      catch (SQLException e)
      {
        String msg = "Error while initalizing database backend " + b.getName()
            + " (" + e + ")";
        logger.warn(msg, e);
        throw new VirtualDatabaseException(msg);
      }
    }

    return b;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#replicateBackend(java.lang.String,
   *      java.lang.String, java.util.Map)
   */
  public void replicateBackend(String backendName, String newBackendName,
      Map parameters) throws VirtualDatabaseException
  {
    // Access the backend we want to replicate
    DatabaseBackend backend = getAndCheckBackend(backendName, NO_CHECK_BACKEND);
    DatabaseBackend newBackend = null;

    // Create a clone of the backend with additionnal parameters
    try
    {
      newBackend = backend.copy(newBackendName, parameters);
    }
    catch (Exception e)
    {
      String msg = Translate.get("virtualdatabase.fail.backend.copy");
      logger.warn(msg, e);
      throw new VirtualDatabaseException(msg);
    }

    // Add the backend to the virtual database.
    addBackend(newBackend);
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#removeBackend(java.lang.String)
   */
  public void removeBackend(String backend) throws VirtualDatabaseException
  {
    removeBackend(getAndCheckBackend(backend, NO_CHECK_BACKEND));
  }

  /**
   * Remove a backend from this virtual database.
   * 
   * @param db the database backend to remove
   * @throws VirtualDatabaseException if an error occurs
   */
  public void removeBackend(DatabaseBackend db) throws VirtualDatabaseException
  {
    if (db == null)
    {
      String msg = "Illegal null database backend in removeBackend(DatabaseBackend) method";
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    try
    {
      rwLock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    // Sanity checks
    int idx = backends.indexOf(db);
    if (idx == -1)
    {
      rwLock.releaseWrite(); // Release the lock
      String msg = "Trying to remove a non-existing backend " + db.getName();
      logger.warn(msg);
      throw new VirtualDatabaseException(msg);
    }

    if (((DatabaseBackend) backends.get(idx)).isReadEnabled())
    {
      rwLock.releaseWrite(); // Release the lock
      String msg = "Trying to remove an enabled backend " + db.getName();
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    // Remove it
    backends.remove(idx);
    rwLock.releaseWrite(); // Relase the lock

    // Send notification
    if (MBeanServerManager.isJmxEnabled())
    {
      // Send notification
      Hashtable data = new Hashtable();
      data.put(CjdbcNotificationList.DATA_DATABASE, this.name);
      data.put(CjdbcNotificationList.DATA_DRIVER, db.getDriverClassName());
      String checkpoint = db.getLastKnownCheckpoint();
      checkpoint = (checkpoint == null) ? "" : checkpoint;
      data.put(CjdbcNotificationList.DATA_CHECKPOINT, checkpoint);
      data.put(CjdbcNotificationList.DATA_NAME, db.getName());
      data.put(CjdbcNotificationList.DATA_URL, db.getURL());
      RmiConnector.broadcastNotification(this,
          CjdbcNotificationList.VIRTUALDATABASE_BACKEND_REMOVED,
          CjdbcNotificationList.NOTIFICATION_LEVEL_INFO, Translate.get(
              "notification.backend.removed", db.getName()), data);

      // Remove backend mbean to jmx server
      ObjectName objectName = JmxConstants.getDatabaseBackendObjectName(name,
          db.getName());
      try
      {
        MBeanServerManager.unregister(objectName);
      }
      catch (JmxException e1)
      {
        logger.error(Translate.get(
            "virtualdatabase.fail.unregister.backend.mbean", db.getName()), e1);
      }
    }

    if (logger.isDebugEnabled())
      logger.debug("Backend " + db.getName() + " removed successfully");
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#transferBackend(java.lang.String,
   *      java.lang.String)
   */
  public void transferBackend(String backend, String controllerDestination)
      throws VirtualDatabaseException
  {
    throw new VirtualDatabaseException("Cannot transfer backend to controller:"
        + controllerDestination + " because database is not distributed");
  }

  //
  // Backup & Checkpoint management
  //

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#getBackuperNames()
   */
  public String[] getBackuperNames()
  {
    return requestManager.getBackupManager().getBackuperNames();
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#getDumpFormatForBackuper(java.lang.String)
   */
  public String getDumpFormatForBackuper(String backuperName)
  {
    Backuper backuper = requestManager.getBackupManager().getBackuperByName(
        backuperName);
    if (backuper == null)
    {
      return null;
    }
    return backuper.getDumpFormat();
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#backupBackend(String,
   *      String, String, String, String, String, ArrayList)
   */
  public void backupBackend(String backendName, String login, String password,
      String dumpName, String backuperName, String path, ArrayList tables)
      throws VirtualDatabaseException
  {
    try
    {
      DatabaseBackend db = getAndCheckBackend(backendName, NO_CHECK_BACKEND);
      requestManager.backupBackend(db, login, password, dumpName, backuperName,
          path, tables);
    }
    catch (SQLException sql)
    {
      throw new VirtualDatabaseException(sql);
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#getAvailableDumps()
   */
  public DumpInfo[] getAvailableDumps() throws VirtualDatabaseException
  {
    try
    {
      RecoveryLog recoveryLog = requestManager.getRecoveryLog();
      if (recoveryLog == null)
      {
        return new DumpInfo[0];
      }
      else
      {
        ArrayList dumps = recoveryLog.getDumpList();
        return (DumpInfo[]) dumps.toArray(new DumpInfo[dumps.size()]);
      }
    }
    catch (SQLException e)
    {
      throw new VirtualDatabaseException(e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#updateDumpPath(java.lang.String,
   *      java.lang.String)
   */
  public void updateDumpPath(String dumpName, String newPath)
      throws VirtualDatabaseException
  {
    try
    {
      RecoveryLog recoveryLog = requestManager.getRecoveryLog();
      if (recoveryLog == null)
      {
        throw new VirtualDatabaseException("no recovery log"); // TODO I18N
      }
      else
      {
        recoveryLog.updateDumpPath(dumpName, newPath);
      }
    }
    catch (SQLException e)
    {
      throw new VirtualDatabaseException(e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#removeDump(String)
   */
  public boolean removeDump(String dumpName)
  {
    if (dumpName == null)
    {
      return false;
    }
    RecoveryLog recoveryLog = requestManager.getRecoveryLog();
    if (recoveryLog == null)
    {
      return false;
    }
    else
    {
      try
      {
        DumpInfo dumpInfo = recoveryLog.getDumpInfo(dumpName);
        if (dumpInfo == null)
        {
          return false;
        }
        Backuper backuper = requestManager.getBackupManager()
            .getBackuperByFormat(dumpInfo.getDumpFormat());
        if (backuper == null)
        {
          return false;
        }
        recoveryLog.removeDump(dumpInfo);
        backuper.deleteDump(dumpInfo.getDumpPath(), dumpInfo.getDumpName());
        return true;
      }
      catch (Exception e)
      {
        String msg = Translate.get("virtualdatabase.removeDump.failure",
            new String[]{dumpName, e.getMessage()});
        logger.error(msg);
        return false;
      }
    }
  }

  /**
   * Remove a checkpoint from the recovery log of this virtual database
   * 
   * @param checkpointName to remove
   * @throws VirtualDatabaseException if an error occurs
   */
  public void removeCheckpoint(String checkpointName)
      throws VirtualDatabaseException
  {
    try
    {
      requestManager.removeCheckpoint(checkpointName);
    }
    catch (Exception e)
    {
      throw new VirtualDatabaseException(e.getMessage());
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#restoreDumpOnBackend(String,
   *      String, String, String, ArrayList)
   */
  public void restoreDumpOnBackend(String databaseBackendName, String login,
      String password, String dumpName, ArrayList tables)
      throws VirtualDatabaseException
  {
    DatabaseBackend backend = getAndCheckBackend(databaseBackendName,
        NO_CHECK_BACKEND);
    // Backend cannot be null, otherwise the above throws an
    // VirtualDatabaseException
    try
    {
      requestManager.restoreBackendFromBackupCheckpoint(backend, login,
          password, dumpName, tables);
    }
    catch (BackupException e)
    {
      throw new VirtualDatabaseException(e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#setBackendLastKnownCheckpoint
   */
  public void setBackendLastKnownCheckpoint(String backendName,
      String checkpoint) throws VirtualDatabaseException
  {
    RecoveryLog log = requestManager.getRecoveryLog();
    DatabaseBackend backend = getAndCheckBackend(backendName, NO_CHECK_BACKEND);
    if (log == null)
      throw new VirtualDatabaseException("No recovery log has been defined");
    else
    {
      if (!backend.isDisabled())
        throw new VirtualDatabaseException(
            "Cannot setLastKnownCheckpoint on a non-disabled backend");
      else
      {
        try
        {
          log.storeBackendRecoveryInfo(this.name,
              new BackendRecoveryInfo(backend.getName(), checkpoint, backend
                  .getStateValue(), this.name));

          backend.setLastKnownCheckpoint(checkpoint);
        }
        catch (SQLException e)
        {
          throw new VirtualDatabaseException(
              "Failed to store recovery info for backend '" + backendName
                  + "' (" + e + ")");
        }
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#viewCheckpointNames()
   */
  public ArrayList viewCheckpointNames()
  {
    try
    {
      RecoveryLog recoveryLog = requestManager.getRecoveryLog();
      if (recoveryLog == null)
        return new ArrayList();
      else
        return recoveryLog.getCheckpointNames();
    }
    catch (SQLException e)
    {
      return new ArrayList();
    }
  }

  //
  // Thread management mainly used by controller and monitoring
  //

  /**
   * Adds one to currentNbOfThreads. Warning! This method is not synchronized.
   */
  public void addCurrentNbOfThread()
  {
    currentNbOfThreads++;
  }

  /**
   * Method add an idle thread. Warning! This method must be called in a
   * synchronized block on activeThreads.
   */
  public void addIdleThread()
  {
    idleThreads++;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#getCurrentNbOfThreads()
   */
  public int getCurrentNbOfThreads()
  {
    return currentNbOfThreads;
  }

  /**
   * Returns the number of idle zorker threads. Warning! This method must be
   * called in a synchronized block on activeThreads.
   * 
   * @return int
   */
  public int getIdleThreads()
  {
    return idleThreads;
  }

  /**
   * Returns the maxNbOfThreads.
   * 
   * @return int maximum number of threads
   */
  public int getMaxNbOfThreads()
  {
    return maxNbOfThreads;
  }

  /**
   * Returns the maxThreadIdleTime.
   * 
   * @return long maximum thread idle time in ms
   */
  public long getMaxThreadIdleTime()
  {
    return maxThreadIdleTime;
  }

  /**
   * Returns the minNbOfThreads.
   * 
   * @return int minimum number of threads
   */
  public int getMinNbOfThreads()
  {
    return minNbOfThreads;
  }

  /**
   * Returns the poolConnectionThreads.
   * 
   * @return boolean true if threads are pooled
   */
  public boolean isPoolConnectionThreads()
  {
    return poolConnectionThreads;
  }

  /**
   * Substract one to currentNbOfThreads. Warning! This method is not
   * synchronized.
   */
  public void removeCurrentNbOfThread()
  {
    currentNbOfThreads--;
  }

  /**
   * Remove an idle thread. Warning! This method must be called in a
   * synchronized block on activeThreads.
   */
  public void removeIdleThread()
  {
    idleThreads--;
  }

  /**
   * Sets the maxThreadIdleTime.
   * 
   * @param maxThreadIdleTime The maxThreadIdleTime to set
   */
  public void setMaxThreadIdleTime(long maxThreadIdleTime)
  {
    this.maxThreadIdleTime = maxThreadIdleTime;
  }

  /**
   * Sets the minNbOfThreads.
   * 
   * @param minNbOfThreads The minNbOfThreads to set
   */
  public void setMinNbOfThreads(int minNbOfThreads)
  {
    this.minNbOfThreads = minNbOfThreads;
  }

  /**
   * Sets the poolConnectionThreads.
   * 
   * @param poolConnectionThreads The poolConnectionThreads to set
   */
  public void setPoolConnectionThreads(boolean poolConnectionThreads)
  {
    this.poolConnectionThreads = poolConnectionThreads;
  }

  //
  // Getter/Setter and tools (equals, ...)
  //

  /**
   * Returns the activeThreads.
   * 
   * @return ArrayList
   */
  public ArrayList getActiveThreads()
  {
    return activeThreads;
  }

  /**
   * Returns the authentication manager of this virtual database.
   * 
   * @return an <code>AuthenticationManager</code> instance
   */
  public AuthenticationManager getAuthenticationManager()
  {
    return authenticationManager;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#getBackendInformation(String)
   */
  public String getBackendInformation(String backendName)
      throws VirtualDatabaseException
  {
    try
    {
      acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = "Unable to acquire read lock on backend list in getBackendInformation ("
          + e + ")";
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    // Find the backend
    int size = backends.size();
    DatabaseBackend b = null;
    for (int i = 0; i < size; i++)
    {
      b = (DatabaseBackend) backends.get(i);
      if (b.getName().equals(backendName))
        break;
      else
        b = null;
    }

    if (b == null)
    {
      releaseReadLockBackendLists();
      String msg = "Backend " + backendName + " does not exists.";
      logger.warn(msg);
      throw new VirtualDatabaseException(msg);
    }

    releaseReadLockBackendLists();
    return b.getXml();
  }

  /**
   * Return the list of all backends
   * 
   * @return <code>ArrayList</code> of <code>DatabaseBackend</code> Objects
   */
  public ArrayList getBackends()
  {
    return backends;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#getBackendSchema(java.lang.String)
   */
  public String getBackendSchema(String backendName)
      throws VirtualDatabaseException
  {
    DatabaseBackend backend = getAndCheckBackend(backendName, NO_CHECK_BACKEND);
    // we know the backend is not null, otherwise we have a
    // VirtualDatabaseException ...
    try
    {
      return XmlTools.prettyXml(backend.getSchemaXml(true));
    }
    catch (Exception e)
    {
      throw new VirtualDatabaseException(e.getMessage());
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#getBackendState(java.lang.String)
   */
  public String getBackendState(String backendName)
      throws VirtualDatabaseException
  {
    DatabaseBackend backend = getAndCheckBackend(backendName, NO_CHECK_BACKEND);
    return backend.getState();
  }

  /**
   * Return the BLOB filter used for this database
   * 
   * @return the BLOB filter used for this database.
   */
  public AbstractBlobFilter getBlobFilter()
  {
    return blobFilter;
  }

  /**
   * Gets the virtual database name to be used by the client (C-JDBC driver)
   * This method should be used for local references only (it is faster). For
   * remote RMI calls, use {@link #getVirtualDatabaseName()}.
   * 
   * @return the virtual database name
   * @see VirtualDatabase#getVirtualDatabaseName()
   */
  public String getDatabaseName()
  {
    return name;
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData#getDatabaseProductName()
   */
  public String getDatabaseProductName()
  {
    return databaseProductNames;
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData
   * @return associated metada for this database
   */
  public VirtualDatabaseDynamicMetaData getDynamicMetaData()
  {
    if (metadata == null)
    {
      metadata = new VirtualDatabaseDynamicMetaData(this);
    }
    return metadata;
  }

  /**
   * Get the current database schema from merging the schemas of all active
   * backends.
   * 
   * @return the current database schema dynamically gathered
   * @throws SQLException if an error occurs
   */
  public DatabaseSchema getDatabaseSchemaFromActiveBackends()
      throws SQLException
  {
    boolean isRaidb1 = requestManager.getLoadBalancer().getRAIDbLevel() == RAIDbLevels.RAIDb1;

    try
    {
      acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = "Unable to acquire read lock on backend list in getDatabaseSchemaFromActiveBackends ("
          + e + ")";
      logger.error(msg);
      throw new SQLException(msg);
    }

    // Build the new schema from all active backend's schemas
    int size = backends.size();
    DatabaseSchema schema = null;
    DatabaseBackend b = null;
    for (int i = 0; i < size; i++)
    {
      b = (DatabaseBackend) backends.get(i);
      if (b.isReadEnabled())
      {
        if (schema == null)
          schema = new DatabaseSchema(b.getDatabaseSchema());
        else
          schema.mergeSchema(b.getDatabaseSchema());

        // In RAIDb-1 all backends are the same so there is no need to merge and
        // we just take the schema of the first backend
        if (isRaidb1)
          break;
      }
    }

    // Note that if the RecoveryLog points to the same database it will appear
    // in the database schema but this is a normal behavior.

    releaseReadLockBackendLists();

    return schema;
  }

  /**
   * Get the current database schema from merging the schemas of all active
   * backends. This is needed when a backend is disabled.
   * 
   * @return the current database schema dynamically gathered
   * @throws SQLException if an error occurs
   */
  public DatabaseSchema getDatabaseSchemaFromActiveBackendsAndRefreshDatabaseProductNames()
      throws SQLException
  {
    try
    {
      acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = "Unable to acquire read lock on backend list in getDatabaseSchemaFromActiveBackendsAndRefreshDatabaseProductNames ("
          + e + ")";
      logger.error(msg);
      throw new SQLException(msg);
    }

    // Build the new schema from all active backend's schemas
    int size = backends.size();
    DatabaseSchema schema = null;
    DatabaseBackend b = null;
    String dbProductNames = "C-JDBC";
    for (int i = 0; i < size; i++)
    {
      b = (DatabaseBackend) backends.get(i);
      if (b.isReadEnabled())
      {
        if (schema == null)
          schema = new DatabaseSchema(b.getDatabaseSchema());
        else
          schema.mergeSchema(b.getDatabaseSchema());
      }

      // Update the list of database product names
      if (dbProductNames.indexOf(b.getDatabaseProductName()) == -1)
        dbProductNames += "," + b.getDatabaseProductName();
    }

    releaseReadLockBackendLists();
    databaseProductNames = dbProductNames;

    // Note that if the RecoveryLog points to the same database it will appear
    // in the database schema but this is a normal behavior.

    return schema;
  }

  /**
   * Returns the maxNbOfConnections.
   * 
   * @return int
   */
  public int getMaxNbOfConnections()
  {
    return maxNbOfConnections;
  }

  /**
   * Returns the pendingConnections.
   * 
   * @return ArrayList
   */
  public ArrayList getPendingConnections()
  {
    return pendingConnections;
  }

  /**
   * Gets the request manager associated to this database.
   * 
   * @return a <code>RequestManager</code> instance
   */
  public RequestManager getRequestManager()
  {
    return requestManager;
  }

  /**
   * Get the whole static metadata for this virtual database. A new empty
   * metadata object is created if there was none yet. It will be filled later
   * by gatherStaticMetadata() when the backend is enabled.
   * 
   * @return Virtual database static metadata
   */
  public VirtualDatabaseStaticMetaData getStaticMetaData()
  {
    if (staticMetadata == null)
    {
      staticMetadata = new VirtualDatabaseStaticMetaData(this);
    }
    return staticMetadata;
  }

  /**
   * Gets the virtual database name to be used by the client (C-JDBC driver)
   * 
   * @return the virtual database name
   */
  public String getVirtualDatabaseName()
  {
    return name;
  }

  /**
   * Returns the current SQL monitor
   * 
   * @return a <code>SQLMonitoring</code> instance or null if no monitor is
   *         defined
   */
  public SQLMonitoring getSQLMonitor()
  {
    return sqlMonitor;
  }

  /**
   * Return the sql short form length to use when reporting an error.
   * 
   * @return sql short form length
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#getSQLShortForm(int)
   */
  public int getSQLShortFormLength()
  {
    return sqlShortFormLength;
  }

  /**
   * Returns the totalOrderQueue value.
   * 
   * @return Returns the totalOrderQueue.
   */
  public LinkedList getTotalOrderQueue()
  {
    return totalOrderQueue;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#hasRecoveryLog()
   */
  public boolean hasRecoveryLog()
  {
    RecoveryLog log = requestManager.getRecoveryLog();
    if (log == null)
      return false;
    else
      return true;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#hasResultCache()
   */
  public boolean hasResultCache()
  {
    AbstractResultCache cache = requestManager.getResultCache();
    if (cache == null)
      return false;
    else
      return true;
  }

  /**
   * Sets the authentication manager for this virtual database.
   * 
   * @param authenticationManager the <code>AuthenticationManager</code> to
   *          set
   */
  public void setAuthenticationManager(
      AuthenticationManager authenticationManager)
  {
    this.authenticationManager = authenticationManager;
  }

  /**
   * Set the BLOB filter to use for this database.
   * 
   * @param filter the filter to use.
   */
  public void setBlobFilter(AbstractBlobFilter filter)
  {
    this.blobFilter = filter;
  }

  /**
   * Sets a new static database schema for this database if no one exist or
   * merge the given schema to the existing one. A static schema can only be
   * replaced by another static schema.
   * 
   * @param schema the new database shema
   */
  public void setStaticDatabaseSchema(DatabaseSchema schema)
  {
    if (requestManager != null)
      requestManager.setDatabaseSchema(schema, true);
    else
      logger
          .warn("Unable to set database schema, no request manager has been defined.");
  }

  /**
   * Sets the maxNbOfConnections.
   * 
   * @param maxNbOfConnections The maxNbOfConnections to set
   */
  public void setMaxNbOfConnections(int maxNbOfConnections)
  {
    this.maxNbOfConnections = maxNbOfConnections;
  }

  /**
   * Sets the maxNbOfThreads.
   * 
   * @param maxNbOfThreads The maxNbOfThreads to set
   */
  public void setMaxNbOfThreads(int maxNbOfThreads)
  {
    this.maxNbOfThreads = maxNbOfThreads;
  }

  /**
   * Sets a new request manager for this database.
   * 
   * @param requestManager the new request manager.
   */
  public void setRequestManager(RequestManager requestManager)
  {
    this.requestManager = requestManager;
  }

  /**
   * Sets a new SQL Monitor
   * 
   * @param sqlMonitor the new SQL monitor
   */
  public void setSQLMonitor(SQLMonitoring sqlMonitor)
  {
    this.sqlMonitor = sqlMonitor;
  }

  /**
   * Sets the totalOrderQueue.
   * 
   * @param newQueue the new queue to use
   */
  public void setTotalOrderQueue(LinkedList newQueue)
  {
    if (totalOrderQueue != null)
    {
      if (totalOrderQueue.isEmpty())
        logger.info("Overriding local total order queue");
      else
        logger.error("Non-empty local order queue redefined (still contains"
            + totalOrderQueue.size() + " entries)");
    }
    totalOrderQueue = newQueue;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#setMonitoringToActive(boolean)
   */
  public void setMonitoringToActive(boolean active)
      throws VirtualDatabaseException
  {
    if (sqlMonitor == null)
      throw new VirtualDatabaseException(Translate
          .get("virtualdatabase.monitoring.not.defined"));
    else
      sqlMonitor.setActive(active);
  }

  /**
   * Two virtual databases are equal if they have the same name and group.
   * 
   * @param other the object to compare with
   * @return <code>true</code> if the two virtual databases are equals
   */
  public boolean equals(Object other)
  {
    if ((other == null) || (!(other instanceof VirtualDatabase)))
      return false;
    else
    {
      VirtualDatabase db = (VirtualDatabase) other;
      return name.equals(db.getDatabaseName());
    }
  }

  // /////////////////////////////////////////
  // JMX
  // ////////////////////////////////////////

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#cleanMonitoringData()
   */
  public void cleanMonitoringData() throws VirtualDatabaseException
  {
    if (sqlMonitor == null)
      throw new VirtualDatabaseException(Translate
          .get("virtualdatabase.monitoring.not.defined"));
    else
      sqlMonitor.cleanStats();
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#retrieveBackendsData()
   */
  public String[][] retrieveBackendsData() throws VirtualDatabaseException
  {
    try
    {
      acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get("virtualdatabase.fail.read.lock", e);
      throw new VirtualDatabaseException(msg);
    }
    ArrayList localBackends = this.getBackends();
    int backendListSize = localBackends.size();
    String[][] data = new String[backendListSize][];
    for (int i = 0; i < backendListSize; i++)
    {
      data[i] = ((DatabaseBackend) localBackends.get(i)).getBackendData();
    }
    releaseReadLockBackendLists();
    return data;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#getBackendStatistics(java.lang.String)
   */
  public BackendStatistics getBackendStatistics(String backendName)
      throws VirtualDatabaseException
  {
    try
    {
      acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get("virtualdatabase.fail.read.lock", e);
      throw new VirtualDatabaseException(msg);
    }
    BackendStatistics stat = null;
    ArrayList backendList = this.getBackends();
    for (Iterator iter = backendList.iterator(); iter.hasNext();)
    {
      DatabaseBackend backend = (DatabaseBackend) iter.next();
      if (backend.getName().equals(backendName))
      {
        stat = backend.getBackendStats();
      }
    }
    releaseReadLockBackendLists();
    return stat;
  }

  /**
   * Return true if this database is shutting down.
   * 
   * @return true if this database is shutting down.
   */
  public boolean isShuttingDown()
  {
    return shuttingDown;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#shutdown(int)
   */
  public void shutdown(int level)
  {
    VirtualDatabaseShutdownThread vdst = null;
    synchronized (this)
    {
      if (shuttingDown)
        return;
      switch (level)
      {
        case Constants.SHUTDOWN_WAIT :
          vdst = new VirtualDatabaseWaitShutdownThread(this);
          logger.info(Translate.get("controller.shutdown.type.wait", this
              .getVirtualDatabaseName()));
          break;
        case Constants.SHUTDOWN_SAFE :
          shuttingDown = true;
          vdst = new VirtualDatabaseSafeShutdownThread(this);
          logger.info(Translate.get("controller.shutdown.type.safe", this
              .getVirtualDatabaseName()));
          break;
        case Constants.SHUTDOWN_FORCE :
          shuttingDown = true;
          vdst = new VirtualDatabaseForceShutdownThread(this);
          logger.warn(Translate.get("controller.shutdown.type.force", this
              .getVirtualDatabaseName()));
          break;
        default :
          String msg = Translate
              .get("controller.shutdown.unknown.level", level);
          logger.error(msg);
          throw new RuntimeException(msg);
      }
    }

    new Thread(vdst.getShutdownGroup(), vdst, "VirtualDatabase Shutdown Thread")
        .start();
  }

  /**
   * Write the checkpoints for all backends on the recovery log
   */
  public void storeBackendsInfo()
  {
    requestManager.storeBackendsInfo(this.name, getBackends());
  }

  /**
   * Get all users connected to that database
   * 
   * @return an <code>ArrayList</code> of strings containing the clients
   *         username
   */
  public ArrayList viewAllClientNames()
  {
    ArrayList list = this.getActiveThreads();
    int size = list.size();
    ArrayList clients = new ArrayList(size);
    for (int i = 0; i < list.size(); i++)
      clients.add(((VirtualDatabaseWorkerThread) list.get(i)).getUser());
    return clients;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#viewBackendInformation(java.lang.String)
   */
  public String[] viewBackendInformation(String backendName)
      throws VirtualDatabaseException
  {
    DatabaseBackend backend = getAndCheckBackend(backendName, NO_CHECK_BACKEND);
    return backend.getBackendData();
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#viewControllerList()
   */
  public String[] viewControllerList()
  {
    return new String[]{viewOwningController()};
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#viewGroupBackends()
   */
  public Hashtable viewGroupBackends() throws VirtualDatabaseException
  {
    Hashtable map = new Hashtable();
    try
    {
      acquireReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      String msg = "Unable to acquire read lock on backend list in getAllBackendNames ("
          + e + ")";
      logger.error(msg);
      throw new VirtualDatabaseException(msg);
    }

    // Create an ArrayList<BackendInfo> from the backend list
    int size = backends.size();
    ArrayList backendInfos = new ArrayList(size);
    for (int i = 0; i < size; i++)
      backendInfos.add(new BackendInfo(((DatabaseBackend) backends.get(i))));

    releaseReadLockBackendLists();

    // Return a map with the controller JMX name and its ArrayList<BackendInfo>
    map.put(controller.getJmxName(), backendInfos);
    return map;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#viewOwningController()
   */
  public String viewOwningController()
  {
    return controller.getJmxName();
  }

  /**
   * @see org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean#getAssociatedString()
   */
  public String getAssociatedString()
  {
    return "virtualdatabase";
  }

  /**
   * Retrieves this <code>VirtualDatabase</code> object in xml format
   * 
   * @return xml formatted string that conforms to c-jdbc.dtd
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_VirtualDatabase + " "
        + DatabasesXmlTags.ATT_name + "=\"" + this.getVirtualDatabaseName()
        + "\" " + DatabasesXmlTags.ATT_maxNbOfConnections + "=\""
        + this.getMaxNbOfConnections() + "\" "
        + DatabasesXmlTags.ATT_poolThreads + "=\""
        + this.isPoolConnectionThreads() + "\" "
        + DatabasesXmlTags.ATT_minNbOfThreads + "=\""
        + this.getMinNbOfThreads() + "\" "
        + DatabasesXmlTags.ATT_maxNbOfThreads + "=\""
        + this.getMaxNbOfThreads() + "\" "
        + DatabasesXmlTags.ATT_maxThreadIdleTime + "=\""
        + this.getMaxThreadIdleTime() / 1000 + "\" "
        + DatabasesXmlTags.ATT_sqlDumpLength + "=\"" + this.sqlShortFormLength
        + "\" " + DatabasesXmlTags.ATT_blobEncodingMethod + "=\""
        + this.blobFilter.getXml() + "\">");

    info.append(getDistributionXml());

    if (this.getSQLMonitor() != null)
      info.append(sqlMonitor.getXml());

    info.append(requestManager.getBackupManager().getXml());

    if (this.getAuthenticationManager() != null)
      info.append(authenticationManager.getXml());

    try
    {
      acquireReadLockBackendLists();
      int size = backends.size();
      for (int i = 0; i < size; i++)
        info.append(((DatabaseBackend) backends.get(i)).getXml());
      releaseReadLockBackendLists();
    }
    catch (InterruptedException e)
    {
      logger.error(Translate.get("virtualdatabase.fail.read.lock", e));
    }
    if (requestManager != null)
      info.append(requestManager.getXml());
    info.append("</" + DatabasesXmlTags.ELT_VirtualDatabase + ">");
    return info.toString();
  }

  /**
   * Get the XML dump of the Distribution element if any.
   * 
   * @return ""
   */
  protected String getDistributionXml()
  {
    return "";
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#deleteLogUpToCheckpoint(java.lang.String)
   */
  public void deleteLogUpToCheckpoint(String checkpointName)
      throws VirtualDatabaseException
  {
    if (!hasRecoveryLog())
      throw new VirtualDatabaseException(Translate
          .get("virtualdatabase.no.recovery.log"));

    try
    {
      getRequestManager().getRecoveryLog().deleteLogEntriesBeforeCheckpoint(
          checkpointName);
    }
    catch (SQLException e)
    {
      throw new VirtualDatabaseException(e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#copyLogFromCheckpoint(java.lang.String,
   *      java.lang.String)
   */
  public void copyLogFromCheckpoint(String dumpName, String controllerName)
      throws VirtualDatabaseException
  {
    if (!hasRecoveryLog())
      throw new VirtualDatabaseException(Translate
          .get("virtualdatabase.no.recovery.log"));
    if (!isDistributed())
      throw new VirtualDatabaseException(Translate
          .get("virtualdatabase.not.distributed"));

    /**
     * Implemented in the distributed incarnation of the vdb.
     * 
     * @see org.objectweb.cjdbc.controller.virtualdatabase.DistributedVirtualDatabase#copyLogFromCheckpoint(String,
     *      String)
     */
  }

  /**
   * Set a checkpoint for this virtual database. This operation requires that a
   * recovery log be defined for this vdb, as a checkpoint is a mark in the
   * recovery log. This operation blocks writes vdb-wide (in scheduler), to
   * allow a clean write of the checkpoint mark. This is the local part of the
   * distributed, cluster-wide checkpoint procedure. Overriden with a
   * distributed operation on all controllers recovery logs if the vdb is
   * distributed.
   * 
   * @param checkpointName the desired name of the checkpoint
   * @throws VirtualDatabaseException in case of error. Wraps any error: local
   *           recovery log failure, comm failure, remote failure.
   */
  public void setCheckpoint(String checkpointName)
      throws VirtualDatabaseException
  {
    if (!hasRecoveryLog())
      throw new VirtualDatabaseException(Translate
          .get("virtualdatabase.no.recovery.log"));

    try
    {
      // Wait for all pending writes to finish
      logger.info(Translate.get("requestmanager.wait.pending.writes"));
      getRequestManager().getScheduler().suspendWrites();

      // Store checkpoint
      getRequestManager().getRecoveryLog().storeCheckpoint(checkpointName);
      logger.info(Translate.get("recovery.checkpoint.stored", checkpointName));

      // Resume writes
      logger.info(Translate.get("requestmanager.resume.pending.writes"));
      getRequestManager().getScheduler().resumeWrites();

    }
    catch (SQLException e)
    {
      String msg = "set checkpoint failed";
      logger.error(msg, e);
      throw new VirtualDatabaseException(msg, e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#copyDump(java.lang.String,
   *      java.lang.String)
   */
  public void copyDump(String dumpName, String remoteControllerName)
      throws VirtualDatabaseException
  {
    if (!isDistributed())
      throw new VirtualDatabaseException(
          "can not copy dumps on non-distributed virtual database");
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean#transferDump(java.lang.String,
   *      java.lang.String, boolean)
   */
  public void transferDump(String dumpName, String remoteControllerName,
      boolean noCopy) throws VirtualDatabaseException
  {
    if (!isDistributed())
      throw new VirtualDatabaseException(
          "can not transfer dumps on non-distributed virtual database");
  }

}