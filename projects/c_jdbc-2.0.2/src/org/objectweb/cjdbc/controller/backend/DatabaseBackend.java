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
 * Contributor(s): Mathieu Peltier, Sara Bouchenak, Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.controller.backend;

import java.io.StringReader;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.management.NotCompliantMBeanException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.objectweb.cjdbc.common.exceptions.NoTransactionStartWhenDisablingException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean;
import org.objectweb.cjdbc.common.jmx.notifications.CjdbcNotificationList;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.monitor.backend.BackendStatistics;
import org.objectweb.cjdbc.common.shared.BackendInfo;
import org.objectweb.cjdbc.common.shared.BackendState;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.CreateRequest;
import org.objectweb.cjdbc.common.sql.metadata.MetadataContainer;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;
import org.objectweb.cjdbc.controller.backend.rewriting.AbstractRewritingRule;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.connection.FailFastPoolConnectionManager;
import org.objectweb.cjdbc.controller.connection.RandomWaitPoolConnectionManager;
import org.objectweb.cjdbc.controller.connection.SimpleConnectionManager;
import org.objectweb.cjdbc.controller.connection.VariablePoolConnectionManager;
import org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean;
import org.objectweb.cjdbc.controller.jmx.MBeanServerManager;
import org.objectweb.cjdbc.controller.jmx.RmiConnector;
import org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer;

/**
 * A <code>DatabaseBackend</code> represents a real database backend that will
 * have to be bound to a virtual C-JDBC database. All connections opened will
 * use the same url but possibly different login/password.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Sara.Bouchenak@epfl.ch">Sara Bouchenak </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public final class DatabaseBackend extends AbstractStandardMBean
    implements
      XmlComponent,
      DatabaseBackendMBean
{
  //
  // How the code is organized?
  // 1. Member variables
  // 2. Constructor(s)
  // 3. Lookup functions
  // 4. Connections management
  // 5. State management
  // 6. Getter/Setter (possibly in alphabetical order)
  // 7. Debug/Monitoring
  //

  /** Logical name assigned to this backend. */
  private String                         name;

  /** Path for driver */
  private String                         driverPath;

  /** Database native JDBC driver class name. */
  private String                         driverClassName;

  /** Driver compliance to C-JDBC requirements */
  private transient DriverCompliance     driverCompliance;

  /** Real URL to access the database (JDBC URL). */
  private String                         url;

  /** Name of the virtual database this backend is attached to */
  private String                         virtualDatabaseName;

  /** A boolean to know if we should allow this backend to be enabled for write */
  private boolean                        writeCanBeEnabled;

  /** SQL statement used to check if a connection is still valid */
  private String                         connectionTestStatement;

  /**
   * The schema of the database. This should be accessed in a synchronized(this)
   * block since it can be updated dynamically.
   */
  private transient DatabaseSchema       schema;

  /** <code>true</code> if schema is static. */
  private boolean                        schemaIsStatic      = false;

  /**
   * <code>true</code> if the backend must maintain its schema dynamically for
   * the virtual database needs
   */
  private boolean                        schemaIsNeededByVdb = true;

  /** <code>true</code> if schema is no more up-to-date and needs a refresh */
  private boolean                        schemaIsDirty       = true;

  /** Connection managers for this backend. */
  private transient HashMap              connectionManagers;

  /** Logger instance. */
  protected transient Trace              logger;

  /** List of started transactions. */
  private transient ArrayList            activeTransactions  = new ArrayList();

  /** List of savepoints for each transaction */
  private transient Map                  savepoints          = new HashMap();

  /** List of pending requests. */
  private transient Vector               pendingRequests     = new Vector();

  /** Monitoring Values */
  private int                            totalRequest;
  private int                            totalWriteRequest;
  private int                            totalReadRequest;
  private int                            totalTransactions;

  /** List of <code>AbstractRewritingRule</code> objects. */
  private ArrayList                      rewritingRules;

  /** For metadata information generation */
  private int                            dynamicPrecision;
  private boolean                        gatherSystemTables  = false;
  private String                         schemaName          = null;

  /** Short form of SQL statements to include in traces and exceptions */
  private int                            sqlShortFormLength  = 40;

  private String                         lastKnownCheckpoint;

  /**
   * The current state of the backend
   * 
   * @see org.objectweb.cjdbc.common.shared.BackendState
   */
  private int                            state               = BackendState.DISABLED;

  private transient BackendStateListener stateListener;

  /**
   * Creates a new <code>DatabaseBackend</code> instance.
   * 
   * @param name logical name assigned to this backend
   * @param driverPath path for driver
   * @param driverClassName class name of the database native JDBC driver to
   *          load
   * @param url URL to access the database
   * @param vdbName Name of the virtual database this backend is attached to
   * @param writeCanBeEnabled if writes can be enabled on this backend
   * @param connectionTestStatement SQL statement used to check if a connection
   *          is still valid
   * @throws NotCompliantMBeanException if the mbean can not be created (unless
   *           we refactor the code this cannot happend)
   */
  public DatabaseBackend(String name, String driverPath,
      String driverClassName, String url, String vdbName,
      boolean writeCanBeEnabled, String connectionTestStatement)
      throws NotCompliantMBeanException
  {
    super(DatabaseBackendMBean.class);
    if (name == null)
      throw new IllegalArgumentException(Translate
          .get("backend.null.backend.name"));

    if (driverClassName == null)
      throw new IllegalArgumentException(Translate.get("backend.null.driver"));

    if (url == null)
      throw new IllegalArgumentException(Translate.get("backend.null.url"));

    if (vdbName == null)
      throw new IllegalArgumentException(Translate
          .get("backend.null.virtualdatabase.name"));

    if (connectionTestStatement == null)
      throw new IllegalArgumentException(Translate
          .get("backend.null.connection.test"));

    this.name = name;
    this.writeCanBeEnabled = writeCanBeEnabled;
    this.driverPath = driverPath;
    this.driverClassName = driverClassName;
    this.url = url;
    this.virtualDatabaseName = vdbName;
    this.connectionTestStatement = connectionTestStatement;
    this.connectionManagers = new HashMap();
    logger = Trace
        .getLogger("org.objectweb.cjdbc.controller.backend.DatabaseBackend."
            + name);
    this.driverCompliance = new DriverCompliance(logger);
    totalRequest = 0;
    dynamicPrecision = DatabaseBackendSchemaConstants.DynamicPrecisionAll;
  }

  /**
   * Creates a new <code>DatabaseBackend</code> object
   * 
   * @param info a backend info object to create a database backend object from
   * @throws NotCompliantMBeanException if mbean is not compliant (unless we
   *           refactor the code this cannot happend)
   */
  public DatabaseBackend(BackendInfo info) throws NotCompliantMBeanException
  {
    this(info.getName(), info.getDriverPath(), info.getDriverClassName(), info
        .getUrl(), info.getVirtualDatabaseName(), true, info
        .getConnectionTestStatement());
    setDynamicPrecision(info.getDynamicPrecision(),
        info.isGatherSystemTables(), info.getSchemaName());
    try
    {
      String xml = info.getXml();
      StringReader sreader = new StringReader(xml);
      SAXReader reader = new SAXReader();
      Document document = reader.read(sreader);
      Element root = document.getRootElement();
      Iterator iter1 = root.elementIterator();
      while (iter1.hasNext())
      {
        Element elem = (Element) iter1.next();
        if (elem.getName().equals(DatabasesXmlTags.ELT_ConnectionManager))
        {
          String vuser = elem.valueOf("@" + DatabasesXmlTags.ATT_vLogin);
          String rlogin = elem.valueOf("@" + DatabasesXmlTags.ATT_rLogin);
          String rpassword = elem.valueOf("@" + DatabasesXmlTags.ATT_rPassword);
          Iterator iter2 = elem.elementIterator();
          while (iter2.hasNext())
          {
            Element connectionManager = (Element) iter2.next();
            String cname = connectionManager.getName();
            if (cname
                .equals(DatabasesXmlTags.ELT_VariablePoolConnectionManager))
            {
              int minPoolSize = Integer.parseInt(connectionManager.valueOf("@"
                  + DatabasesXmlTags.ATT_minPoolSize));
              int maxPoolSize = Integer.parseInt(connectionManager.valueOf("@"
                  + DatabasesXmlTags.ATT_maxPoolSize));
              int idleTimeout = Integer.parseInt(connectionManager.valueOf("@"
                  + DatabasesXmlTags.ATT_idleTimeout));
              int waitTimeout = Integer.parseInt(connectionManager.valueOf("@"
                  + DatabasesXmlTags.ATT_waitTimeout));
              this.addConnectionManager(vuser,
                  new VariablePoolConnectionManager(url, name, rlogin,
                      rpassword, driverPath, driverClassName, minPoolSize,
                      maxPoolSize, idleTimeout, waitTimeout));
            }
            else if (cname.equals(DatabasesXmlTags.ELT_SimpleConnectionManager))
            {
              this.addConnectionManager(vuser, new SimpleConnectionManager(url,
                  name, rlogin, rpassword, driverPath, driverClassName));
            }
            else if (cname
                .equals(DatabasesXmlTags.ELT_RandomWaitPoolConnectionManager))
            {
              int poolSize = Integer.parseInt(connectionManager.valueOf("@"
                  + DatabasesXmlTags.ATT_poolSize));
              int timeout = Integer.parseInt(connectionManager.valueOf("@"
                  + DatabasesXmlTags.ATT_timeout));
              this
                  .addConnectionManager(vuser,
                      new RandomWaitPoolConnectionManager(url, name, rlogin,
                          rpassword, driverPath, driverClassName, poolSize,
                          timeout));
            }
            else if (cname
                .equals(DatabasesXmlTags.ELT_FailFastPoolConnectionManager))
            {
              int poolSize = Integer.parseInt(connectionManager.valueOf("@"
                  + DatabasesXmlTags.ATT_poolSize));
              this.addConnectionManager(vuser,
                  new FailFastPoolConnectionManager(url, name, rlogin,
                      rpassword, driverPath, driverClassName, poolSize));
            }
          }
        }
      }

    }
    catch (Exception e)
    {
      logger
          .error(Translate.get("backend.add.connection.manager.failed", e), e);
    }
  }

  /**
   * Additionnal constructor for setting a different dynamic schema level.
   * Default was to gather all information Creates a new
   * <code>DatabaseBackend</code> instance.
   * 
   * @param name logical name assigned to this backend
   * @param driverPath path for driver
   * @param driverClassName class name of the database native JDBC driver to
   *          load
   * @param url URL to access the database
   * @param vdbName Name of the virtual database this backend is attached to
   * @param connectionTestStatement SQL statement used to check if a connection
   *          is still valid
   * @param dynamicSchemaLevel for dynamically gathering schema from backend
   * @throws NotCompliantMBeanException (unless we refactor the code this cannot
   *           happend)
   */
  public DatabaseBackend(String name, String driverPath,
      String driverClassName, String url, String vdbName,
      String connectionTestStatement, String dynamicSchemaLevel)
      throws NotCompliantMBeanException
  {
    this(name, driverPath, driverClassName, url, vdbName, true,
        connectionTestStatement);
    this.dynamicPrecision = DatabaseBackendSchemaConstants
        .getDynamicSchemaLevel(dynamicSchemaLevel);
  }

  /**
   * Sets the sqlShortFormLength value.
   * 
   * @param sqlShortFormLength The sqlShortFormLength to set.
   */
  public void setSqlShortFormLength(int sqlShortFormLength)
  {
    this.sqlShortFormLength = sqlShortFormLength;
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

  /* Lookup functions */

  /**
   * Two database backends are considered equal if they have the same name, URL
   * and driver class name.
   * 
   * @param other an object
   * @return a <code>boolean</code> value
   */
  public boolean equals(Object other)
  {
    if ((other == null) || (!(other instanceof DatabaseBackend)))
      return false;
    else
    {
      DatabaseBackend b = (DatabaseBackend) other;
      return name.equals(b.getName())
          && driverClassName.equals(b.getDriverClassName())
          && url.equals(b.getURL());
    }
  }

  /**
   * Returns <code>true</code> if this backend has the given list of tables in
   * its schema. The caller must ensure that the database schema has been
   * defined, using the {@link #setDatabaseSchema(DatabaseSchema, boolean)}or
   * {@link #checkDatabaseSchema()}methods.
   * 
   * @param tables the list of table names (<code>ArrayList</code> of
   *          <code>String</code>) to look for
   * @return <code>true</code> if all the tables are found
   */
  public boolean hasTables(ArrayList tables)
  {
    DatabaseSchema schemaPtr = getDatabaseSchema();
    if (schemaPtr == null)
      throw new NullPointerException(Translate.get("backend.schema.not.set"));

    if (tables == null)
      throw new IllegalArgumentException(Translate.get("backend.null.tables"));

    int size = tables.size();
    for (int i = 0; i < size; i++)
    {
      if (!schemaPtr.hasTable((String) tables.get(i)))
        return false;
    }
    return true;
  }

  /**
   * Returns <code>true</code> if this backend has the given table in its
   * schema. The caller must ensure that the database schema has been defined,
   * using the {@link #setDatabaseSchema(DatabaseSchema, boolean)}or
   * {@link #checkDatabaseSchema()}
   * 
   * @param table The table name to look for
   * @return <code>true</code> if tables is found in the schema
   */
  public boolean hasTable(String table)
  {
    DatabaseSchema schemaPtr = getDatabaseSchema();
    if (schemaPtr == null)
      throw new NullPointerException(Translate.get("backend.schema.not.set"));

    return schemaPtr.hasTable(table);
  }

  /**
   * Get all the names of tables of this database
   * 
   * @return <code>ArrayList</code> of <code>DatabaseTable</code>
   */
  public ArrayList getTables()
  {
    DatabaseSchema schemaPtr = getDatabaseSchema();
    if (schemaPtr == null)
      throw new NullPointerException(Translate.get("backend.schema.not.set"));
    return schemaPtr.getTables();
  }

  /**
   * Returns <code>true</code> if this backend has the given stored procedure
   * in its schema. The caller must ensure that the database schema has been
   * defined, using the {@link #setDatabaseSchema(DatabaseSchema, boolean)}or
   * {@link #checkDatabaseSchema()}
   * 
   * @param procedureName The stored procedure name to look for
   * @return <code>true</code> if procedure name is found in the schema
   */
  public boolean hasStoredProcedure(String procedureName)
  {
    DatabaseSchema schemaPtr = getDatabaseSchema();
    if (schemaPtr == null)
      throw new NullPointerException(Translate.get("backend.schema.not.set"));

    return schemaPtr.hasProcedure(procedureName);
  }

  /* Connection management */

  /**
   * Initializes the connection managers' connections. The caller must ensure
   * that the driver has already been loaded else an exception will be thrown.
   * 
   * @exception SQLException if an error occurs
   */
  public synchronized void initializeConnections() throws SQLException
  {
    if (connectionManagers.isEmpty())
      throw new SQLException(Translate.get("backend.not.defined", new String[]{
          name, url}));

    AbstractConnectionManager connectionManager;
    Iterator iter = connectionManagers.values().iterator();
    while (iter.hasNext())
    {
      connectionManager = (AbstractConnectionManager) iter.next();
      if (!connectionManager.isInitialized())
        connectionManager.initializeConnections();
    }
  }

  /**
   * Releases all the connections to the database held by the connection
   * managers.
   * 
   * @throws SQLException if an error occurs
   */
  public synchronized void finalizeConnections() throws SQLException
  {
    if (connectionManagers.isEmpty())
      throw new SQLException(Translate.get("backend.not.defined", new String[]{
          name, url}));

    AbstractConnectionManager connectionManager;
    Iterator iter = connectionManagers.values().iterator();
    while (iter.hasNext())
    {
      connectionManager = (AbstractConnectionManager) iter.next();
      if (connectionManager.isInitialized())
        connectionManager.finalizeConnections();
    }
  }

  /**
   * Check if the given connection is valid or not. This function issues the
   * connectionTestStatement query on the connection and if it succeeds then the
   * connection is declared valid. If an exception occurs, the connection is
   * declared invalid.
   * 
   * @param connection the connection to test
   * @return true if the connection is valid
   */
  public boolean isValidConnection(Connection connection)
  {
    try
    {
      Statement s = connection.createStatement();
      s.executeQuery(connectionTestStatement);
    }
    catch (SQLException e)
    {
      if ("25P02".equals(e.getSQLState())
          || (e.getMessage() != null && e
              .getMessage()
              .indexOf(
                  "current transaction is aborted, queries ignored until end of transaction block") > 0))
      {
        // see bug item #300873 on the forge for details
        // postgres throws an exception if a query is issued after a request has
        // failed within a transaction, we now have to check for this exception
        // as it is means the connection is valid
        //
        // postgres versions after 7.4 will return the SQLState, whereas
        // postgres versions prior to 7.4 will have to be checked for the
        // message text
        return true;
      }
      return false;
    }
    return true;
  }

  /**
   * Adds a <code>ConnectionManager</code> to this backend. Note that the
   * <code>ConnectionManager</code> is not initialized in this method.
   * 
   * @param vLogin the virtual login corresponding to this connection manager
   * @param connectionManager the <code>ConnectionManager</code> to add
   */
  public void addConnectionManager(String vLogin,
      AbstractConnectionManager connectionManager)
  {
    if (connectionManager == null)
      throw new IllegalArgumentException(Translate.get(
          "backend.null.connection.manager", new String[]{name, url}));
    if (logger.isInfoEnabled())
      logger.info(Translate.get("backend.add.connection.manager.for.user",
          vLogin));
    connectionManager.setVLogin(vLogin);
    connectionManagers.put(vLogin, connectionManager);
  }

  /**
   * Retrieve a connection for a given transaction or create a new connection
   * and start a new transaction. <br>
   * This method is synchronized so that concurrent writes within the same
   * transaction that are allowed to execute out of order will not open separate
   * connection if they race on transaction begin.
   * 
   * @param tid transaction identifier
   * @param cm connection manager to get the connection from
   * @param transactionIsolationLevel transaction isolation level to use for a
   *          new transaction (does nothing if equals to
   *          Connection.DEFAULT_TRANSACTION_ISOLATION_LEVEL)
   * @return the connection for the given transaction id
   * @throws UnreachableBackendException if the backend is no more reachable
   * @throws NoTransactionStartWhenDisablingException if a new transaction
   *           needed to be started but the backend is in the disabling state
   * @throws SQLException if another error occurs
   */
  public synchronized Connection getConnectionForTransactionAndLazyBeginIfNeeded(
      Long tid, AbstractConnectionManager cm, int transactionIsolationLevel)
      throws UnreachableBackendException,
      NoTransactionStartWhenDisablingException, SQLException
  {
    if (isStartedTransaction(tid))
    { // Transaction has already been started, retrieve connection
      return cm.retrieveConnection(tid.longValue());
    }
    else
    {
      if (isDisabling())
        throw new NoTransactionStartWhenDisablingException();

      // begin transaction
      startTransaction(tid);

      // Transaction has not been started yet, this is a lazy begin
      return AbstractLoadBalancer.getConnectionAndBeginTransaction(this, cm,
          tid.longValue(), transactionIsolationLevel);
    }
  }

  /* State management */

  /**
   * Signals that a transaction has been started on this backend. It means that
   * a connection has been allocated for this transaction.
   * 
   * @param tid transaction identifier
   */
  public void startTransaction(Long tid)
  {
    synchronized (activeTransactions)
    {
      totalTransactions++;
      activeTransactions.add(tid);
    }
  }

  /**
   * Signals that a transaction has been stopped on this backend. It means that
   * the connection has been released for this transaction.
   * 
   * @param tid transaction identifier
   */
  public void stopTransaction(Long tid)
  {
    synchronized (activeTransactions)
    {
      if (!activeTransactions.remove(tid))
        throw new IllegalArgumentException(Translate.get(
            "backend.transaction.not.started", new String[]{"" + tid, name}));
      // If this was the last open transaction, we notify people possibly
      // waiting on waitForAllTransactionsToComplete()
      if (activeTransactions.isEmpty())
      {
        activeTransactions.notifyAll();
      }
    }

    synchronized (savepoints)
    {
      savepoints.remove(tid);
    }
  }

  /**
   * This method waits until all currently open transactions on this backend
   * complete. If no transaction are currently running on this backend, this
   * method immediately returns.
   */
  public void waitForAllTransactionsToComplete()
  {
    synchronized (activeTransactions)
    {
      if (activeTransactions.isEmpty())
        return;
      else
        try
        {
          activeTransactions.wait();
        }
        catch (InterruptedException ignore)
        {
        }
    }
  }

  /**
   * Returns <code>true</code> if the specified transaction has been started
   * on this backend (a connection has been allocated for this transaction).
   * 
   * @param tid transaction identifier
   * @return <code>true</code> if the transaction has been started
   */
  public boolean isStartedTransaction(Long tid)
  {
    synchronized (activeTransactions)
    {
      return activeTransactions.contains(tid);
    }
  }

  /**
   * Adds a savepoint to a given transaction
   * 
   * @param tid transaction identifier
   * @param savepoint savepoint to add
   */
  public void addSavepoint(Long tid, Savepoint savepoint)
  {
    synchronized (savepoints)
    {
      List savepointList = (List) savepoints.get(tid);
      if (savepointList == null)
      { // Lazy list creation
        savepointList = new ArrayList();
        savepoints.put(tid, savepointList);
      }
      savepointList.add(savepoint);
    }
  }

  /**
   * Removes a savepoint for a given transaction
   * 
   * @param tid transaction identifier
   * @param savepoint savepoint to remove
   */
  public void removeSavepoint(Long tid, Savepoint savepoint)
  {
    synchronized (savepoints)
    {
      List savepointList = (List) savepoints.get(tid);
      if (savepointList == null)
        logger.error("No savepoints found for transaction " + tid);
      else
        savepointList.remove(savepoint);
    }
  }

  /**
   * Retrieves a savepoint object by its name for a given transaction
   * 
   * @param tid transaction identifier
   * @param savepointName name of the savepoint
   * @return a savepoint
   */
  public Savepoint getSavepoint(Long tid, String savepointName)
  {
    synchronized (savepoints)
    {
      List savepointList = (List) savepoints.get(tid);
      if (savepointList == null)
        return null; // No checkpoint for that transaction

      Iterator i = savepointList.iterator();
      while (i.hasNext())
      {
        try
        {
          Savepoint savepoint = (Savepoint) i.next();
          if (savepointName.equals(savepoint.getSavepointName()))
            return savepoint;
        }
        catch (SQLException ignore)
        {
          // We should never get here because we always use named savepoints
          // on backends
        }
      }
    }

    // No savepoint has been found for given savepoint name
    return null;
  }

  /**
   * Checks if this backend has a savepoint with given name for a given
   * transaction
   * 
   * @param tid transaction identifier
   * @param savepointName name of the savepoint
   * @return <code>true</code> if this backend has a savepoint with a given
   *         name for a given transaction
   */
  public boolean hasSavepointForTransaction(Long tid, String savepointName)
  {
    return (this.getSavepoint(tid, savepointName) != null);
  }

  /**
   * Tests if this backend is enabled (active and synchronized).
   * 
   * @return <code>true</code> if this backend is enabled
   * @throws SQLException if an error occurs
   */
  public synchronized boolean isInitialized() throws SQLException
  {
    if (connectionManagers.isEmpty())
      throw new SQLException(Translate.get("backend.null.connection.manager",
          new String[]{name, url}));
    Iterator iter = connectionManagers.values().iterator();
    while (iter.hasNext())
    {
      if (!((AbstractConnectionManager) iter.next()).isInitialized())
        return false;
    }
    return true;
  }

  /**
   * Is the backend accessible ?
   * 
   * @return <tt>true</tt> if a jdbc connection is still possible from the
   *         controller
   */
  public synchronized boolean isJDBCConnected()
  {
    try
    {
      if (connectionManagers.isEmpty())
        throw new SQLException(Translate.get("backend.null.connection.manager",
            new String[]{name, url}));

      AbstractConnectionManager connectionManager;
      Iterator iter = connectionManagers.values().iterator();
      connectionManager = (AbstractConnectionManager) iter.next();

      Connection con = connectionManager.getConnectionFromDriver();
      con.createStatement().execute(this.connectionTestStatement);
      return true;
    }
    catch (Exception e)
    {
      String msg = Translate.get("loadbalancer.backend.unreacheable", name);
      logger.warn(msg, e);
      return false;
    }
  }

  /**
   * Tests if this backend is read enabled (active and synchronized).
   * 
   * @return <code>true</code> if this backend is enabled.
   */
  public synchronized boolean isReadEnabled()
  {
    return state == BackendState.READ_ENABLED_WRITE_DISABLED
        || state == BackendState.READ_ENABLED_WRITE_ENABLED;
  }

  /**
   * Tests if this backend is write enabled (active and synchronized).
   * 
   * @return <code>true</code> if this backend is enabled.
   */
  public synchronized boolean isWriteEnabled()
  {
    return state == BackendState.READ_ENABLED_WRITE_ENABLED
        || state == BackendState.READ_DISABLED_WRITE_ENABLED;
  }

  /**
   * Returns the isRecovering value.
   * 
   * @return Returns the isRecovering.
   */
  public boolean isRecovering()
  {
    return state == BackendState.RECOVERING;
  }

  /**
   * Returns the isDisabling value.
   * 
   * @return Returns the isDisabling.
   */
  public boolean isDisabling()
  {
    return state == BackendState.DISABLING;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean#isDisabled()
   */
  public boolean isDisabled()
  {
    return state == BackendState.DISABLED;
  }

  /**
   * Returns true if the backend cannot be used anymore
   * 
   * @return Returns true if the backend was removed from activity by the load
   *         balancer
   */
  public boolean isKilled()
  {
    return state == BackendState.UNKNOWN;
  }

  /**
   * Retrieve the state of the backend.
   * 
   * @see CjdbcNotificationList#VIRTUALDATABASE_BACKEND_DISABLED
   * @see CjdbcNotificationList#VIRTUALDATABASE_BACKEND_RECOVERING
   * @see CjdbcNotificationList#VIRTUALDATABASE_BACKEND_BACKINGUP
   * @see CjdbcNotificationList#VIRTUALDATABASE_BACKEND_DISABLING
   * @see CjdbcNotificationList#VIRTUALDATABASE_BACKEND_ENABLED
   * @see CjdbcNotificationList#VIRTUALDATABASE_BACKEND_DISABLED
   * @return one of the above
   */
  public String getState()
  {
    switch (state)
    {
      case BackendState.READ_ENABLED_WRITE_DISABLED :
        return CjdbcNotificationList.VIRTUALDATABASE_BACKEND_ENABLED;
      case BackendState.READ_ENABLED_WRITE_ENABLED :
        return CjdbcNotificationList.VIRTUALDATABASE_BACKEND_ENABLED_WRITE;
      case BackendState.READ_DISABLED_WRITE_ENABLED :
        return CjdbcNotificationList.VIRTUALDATABASE_BACKEND_ENABLED_WRITE;
      case BackendState.DISABLING :
        return CjdbcNotificationList.VIRTUALDATABASE_BACKEND_DISABLING;
      case BackendState.BACKUPING :
        return CjdbcNotificationList.VIRTUALDATABASE_BACKEND_BACKINGUP;
      case BackendState.RECOVERING :
        return CjdbcNotificationList.VIRTUALDATABASE_BACKEND_RECOVERING;
      case BackendState.REPLAYING :
        return CjdbcNotificationList.VIRTUALDATABASE_BACKEND_REPLAYING;
      case BackendState.DISABLED :
        return CjdbcNotificationList.VIRTUALDATABASE_BACKEND_DISABLED;
      case BackendState.UNKNOWN :
        return CjdbcNotificationList.VIRTUALDATABASE_BACKEND_UNKNOWN;
      default :
        throw new IllegalArgumentException("Unknown backend state:" + state);
    }
  }

  /**
   * Return the integer value corresponding to the state of the backend. The
   * values are defined in <code>BackendState</code>
   * 
   * @return <tt>int</tt> value
   * @see BackendState
   */
  public int getStateValue()
  {
    return state;
  }

  /**
   * Enables the database backend for reads. This method should only be called
   * when the backend is synchronized with the others.
   */
  public synchronized void enableRead()
  {
    if (isWriteEnabled())
      setState(BackendState.READ_ENABLED_WRITE_ENABLED);
    else
      setState(BackendState.READ_ENABLED_WRITE_DISABLED);
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean#disableRead()
   */
  public synchronized void disableRead()
  {
    if (isWriteEnabled())
      setState(BackendState.READ_DISABLED_WRITE_ENABLED);
    else
      setState(BackendState.DISABLED);
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean#disableWrite()
   */
  public synchronized void disableWrite()
  {
    if (isReadEnabled())
      setState(BackendState.READ_ENABLED_WRITE_DISABLED);
    else
      setState(BackendState.DISABLED);
  }

  /**
   * Enables the database backend for writes. This method should only be called
   * when the backend is synchronized with the others.
   */
  public synchronized void enableWrite()
  {
    // Remove last known checkpoint since backend will now be modified and no
    // more synchronized with the checkpoint.
    setLastKnownCheckpoint(null);
    if (isReadEnabled())
      setState(BackendState.READ_ENABLED_WRITE_ENABLED);
    else
      setState(BackendState.READ_DISABLED_WRITE_ENABLED);
  }

  /**
   * This is used when the backend must be disabled but currently open
   * transactions must terminate. This is a transitional state. When disabling
   * is complete the caller must set the backend state to disabled.
   * <p>
   * Reads are no more allowed on the backend and the state is updated so that
   * isReadEnabled() returns false.
   * 
   * @see #disable()
   * @see #isReadEnabled()
   * @deprecated not used anymore. Please use the setState method instead
   */
  public void setDisabling()
  {
    setState(BackendState.DISABLING);
  }

  /**
   * Sets the database backend state to disable. This state is just an
   * indication and it has no semantic effect. It is up to the request manager
   * (especially the load balancer) to ensure that no more requests are sent to
   * this backend.
   */
  public synchronized void disable()
  {
    setState(BackendState.DISABLED);
  }

  /* Getter/setter methods */

  /**
   * Returns the <code>ConnectionManager</code> associated to this backend for
   * a given virtual login.
   * 
   * @param vLogin the virtual login
   * @return an <code>AbstractConnectionManager</code> instance
   */
  public AbstractConnectionManager getConnectionManager(String vLogin)
  {
    return (AbstractConnectionManager) connectionManagers.get(vLogin);
  }

  /**
   * Returns a <code>HashMap</code> of all the <code>ConnectionManager</code>
   * associated with this <code>DatabaseBackend</code>
   * 
   * @return the hashmap of connection managers
   */
  public HashMap getConnectionManagers()
  {
    return this.connectionManagers;
  }

  /**
   * Returns the SQL statement to use to check the connection validity.
   * 
   * @return a <code>String</code> containing a SQL statement
   */
  public String getConnectionTestStatement()
  {
    return connectionTestStatement;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean#getDriverPath()
   */
  public String getDriverPath()
  {
    return driverPath;
  }

  /**
   * @see org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean#getAssociatedString()
   */
  public String getAssociatedString()
  {
    return "backend";
  }

  /**
   * Returns the database native JDBC driver class name.
   * 
   * @return the driver class name
   */
  public String getDriverClassName()
  {
    return driverClassName;
  }

  /**
   * Returns the backend logical name.
   * 
   * @return the backend logical name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the virtual database name this backend belongs to.
   * 
   * @return Returns the virtual database name.
   */
  public String getVirtualDatabaseName()
  {
    return virtualDatabaseName;
  }

  /**
   * Returns the list of pending requests for this backend.
   * 
   * @return <code>Vector</code> of <code>AbstractRequests</code> or
   *         <code>AbstractTask</code> objects
   */
  public Vector getPendingRequests()
  {
    return pendingRequests;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean#getPendingRequestsDescription(int,
   *      boolean, boolean)
   */
  public ArrayList getPendingRequestsDescription(int count, boolean fromFirst,
      boolean clone)
  {
    int size = pendingRequests.size();
    int limit = (count == 0 || count > size) ? size : Math.min(size, count);
    ArrayList list = new ArrayList(limit);
    int start = (fromFirst) ? 0 : Math.min(limit - count, 0);
    if (!clone)
    {
      synchronized (pendingRequests)
      {
        for (int i = start; i < limit; i++)
          list.add(pendingRequests.get(i).toString());
      }
      return list;
    }
    else
    {
      Vector cloneVector = (Vector) pendingRequests.clone();
      for (int i = start; i < limit; i++)
        list.add(cloneVector.get(i).toString());
      return list;
    }
  }

  /**
   * Adds a pending request (or task) to this backend. Note that the underlying
   * vector is synchronized.
   * 
   * @param request the request to add
   */
  public void addPendingReadRequest(Object request)
  {
    synchronized (this)
    {
      totalRequest++;
      totalReadRequest++;
    }
    pendingRequests.add(request);
  }

  /**
   * Adds a pending request (or task) to this backend. Note that the underlying
   * vector is synchronized.
   * 
   * @param request the request to add
   */
  public void addPendingWriteRequest(Object request)
  {
    synchronized (this)
    {
      totalRequest++;
      totalWriteRequest++;
    }
    pendingRequests.add(request);
  }

  /**
   * Removes a pending request from this backend. Note that the underlying
   * vector is synchronized.
   * 
   * @param request the request to remove
   * @return <code>true</code> if the request has been found and removed
   */
  public boolean removePendingRequest(Object request)
  {
    return pendingRequests.remove(request);
  }

  //
  // Schema manipulation
  //

  /**
   * @see DatabaseBackendMBean#checkDatabaseSchema()
   */
  public synchronized boolean checkDatabaseSchema()
  {
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.dynamic.schema",
          DatabaseBackendSchemaConstants
              .getDynamicSchemaLevel(dynamicPrecision)));

    boolean checked = true;
    AbstractConnectionManager connectionMananger;
    Iterator iter = connectionManagers.values().iterator();
    while (iter.hasNext())
    {
      connectionMananger = (AbstractConnectionManager) iter.next();

      // Gather the database schema from this connection manager
      DatabaseBackendMetaData meta = new DatabaseBackendMetaData(
          connectionMananger, logger, dynamicPrecision, gatherSystemTables,
          schemaName);

      DatabaseSchema metaSchema;
      try
      {
        if (logger.isInfoEnabled())
          logger.info(Translate.get("backend.gathering.database.schema"));
        metaSchema = meta.getDatabaseSchema();
      }
      catch (SQLException e)
      {
        if (logger.isWarnEnabled())
          logger.warn(Translate.get("backend.gather.schema.failed", e));
        return false;
      }
      if (schema == null)
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("backend.use.gathered.schema.as.new"));
        schema = metaSchema;
      }
      else
      {
        if (dynamicPrecision == DatabaseBackendSchemaConstants.DynamicPrecisionStatic)
        {
          if (logger.isInfoEnabled())
            logger.info(Translate.get("backend.schema.static.no.check", name));
        }
        else
        {
          if (logger.isInfoEnabled())
            logger.info(Translate.get("backend.check.schema.compatibility"));
          if (schema.isCompatibleSubset(metaSchema))
            logger.info(Translate.get("backend.schema.compatible.for.login",
                connectionMananger.getLogin()));
          else
          {
            checked = false;
            logger.warn(Translate.get(
                "backend.schema.not.compatible.for.login", connectionMananger
                    .getLogin()));
          }
        }
      }
    }
    setSchemaIsDirty(false);
    return checked;
  }

  /**
   * Returns the schema of this database.
   * 
   * @return the schema of this database. Returns <code>null</code> if the
   *         schema has not been set.
   * @see #setDatabaseSchema(DatabaseSchema, boolean)
   */
  public synchronized DatabaseSchema getDatabaseSchema()
  {
    if (schemaIsNeededByVdb && schemaIsDirty && !schemaIsStatic)
      refreshSchema();
    return schema;
  }

  /**
   * Get the Database static metadata from this backend using a connection from
   * the first available connection manager.
   * 
   * @return Static metadata information
   */
  public MetadataContainer getDatabaseStaticMetadata()
  {
    AbstractConnectionManager connectionMananger;
    Iterator iter = connectionManagers.values().iterator();
    if (iter.hasNext())
    {
      connectionMananger = (AbstractConnectionManager) iter.next();
      // Gather the static metadata from the first connection manager
      DatabaseBackendMetaData meta = new DatabaseBackendMetaData(
          connectionMananger, logger, dynamicPrecision, gatherSystemTables,
          schemaName);
      try
      {
        return meta.retrieveDatabaseMetadata();
      }
      catch (SQLException e)
      {
        return null;
      }
    }
    else
      return null;
  }

  /**
   * @return Returns the dynamicPrecision.
   */
  public int getDynamicPrecision()
  {
    return dynamicPrecision;
  }

  /**
   * Returns the schemaName value.
   * 
   * @return Returns the schemaName.
   */
  public String getSchemaName()
  {
    return schemaName;
  }

  /**
   * Returns the gatherSystemTables value.
   * 
   * @return Returns the gatherSystemTables.
   */
  public boolean isGatherSystemTables()
  {
    return gatherSystemTables;
  }

  /**
   * Returns the schemaIsDirty value.
   * 
   * @return Returns true if the backend database schema is dirty and needs a
   *         refresh.
   */
  public boolean isSchemaDirty()
  {
    return schemaIsDirty;
  }

  /**
   * @return Returns the schemaIsStatic.
   */
  public boolean isSchemaStatic()
  {
    return schemaIsStatic;
  }

  /**
   * Returns true if an up-to-date schema is needed by the virtual database.
   * 
   * @return Returns the schemaIsNeededByVdb.
   */
  public boolean isSchemaNeededByVdb()
  {
    return schemaIsNeededByVdb;
  }

  /**
   * Erase the current schema and force a re-fetch of all the meta data
   */
  private synchronized void refreshSchema()
  {
    setDatabaseSchema(null, isSchemaStatic());
    checkDatabaseSchema(); // set dirty to false as well
  }

  /**
   * Sets the database schema.
   * 
   * @param databaseSchema the schema to set
   * @param isStatic <code>true</code> if the schema should be static
   * @see #getDatabaseSchema()
   */
  public synchronized void setDatabaseSchema(DatabaseSchema databaseSchema,
      boolean isStatic)
  {
    if (schema == null)
    {
      schemaIsStatic = isStatic;
      schema = databaseSchema;
    }
    else
    {
      if (!isStatic)
        schema = databaseSchema;
    }
  }

  /**
   * Set the amount of information that must be gathered when fetching database
   * schema information.
   * 
   * @param dynamicPrecision The dynamicPrecision to set.
   * @param gatherSystemTables True if we must gather system tables
   * @param schemaName Schema name to use to gather tables
   */
  public void setDynamicPrecision(int dynamicPrecision,
      boolean gatherSystemTables, String schemaName)
  {
    this.dynamicPrecision = dynamicPrecision;
    this.gatherSystemTables = gatherSystemTables;
    this.schemaName = schemaName;
  }

  /**
   * Sets the schemaIsDirty value if the backend schema needs to be refreshed.
   * 
   * @param schemaIsDirty The schemaIsDirty to set.
   */
  public void setSchemaIsDirty(boolean schemaIsDirty)
  {
    this.schemaIsDirty = schemaIsDirty;
  }

  /**
   * Sets the schemaIsNeededByVdb value.
   * 
   * @param schemaIsNeededByVdb The schemaIsNeededByVdb to set.
   */
  public void setSchemaIsNeededByVdb(boolean schemaIsNeededByVdb)
  {
    this.schemaIsNeededByVdb = schemaIsNeededByVdb;
  }

  /**
   * Update the DatabaseBackend schema definition according to the successful
   * execution of the provided request. Note that the schema is only updated it
   * the provided request is a DDL statement.
   * 
   * @param request the request that possibly updates the schema
   */
  public void updateDatabaseBackendSchema(AbstractWriteRequest request)
  {
    if (!request.isDDL())
      return;

    // Update schema
    if (isSchemaNeededByVdb())
    {
      if (request.isCreate())
      { // Add the table to the schema
        DatabaseSchema dbs = getDatabaseSchema();
        if (dbs != null)
        {
          CreateRequest createRequest = (CreateRequest) request;
          if (createRequest.altersDatabaseSchema())
          {
            DatabaseTable t = createRequest.getDatabaseTable();
            if (t != null)
            {
              dbs.addTable(t);
              if (logger.isDebugEnabled())
                logger.debug("Added table '" + request.getTableName()
                    + "' to backend database schema");
            }
            else
              // Unsupported force re-fetch from db
              setSchemaIsDirty(true);
          }
          else
            // Unsupported force re-fetch from db
            setSchemaIsDirty(true);
        }
      }
      else if (request.isDrop())
      { // Delete the table from the schema
        DatabaseSchema dbs = getDatabaseSchema();
        if (dbs != null)
        {
          DatabaseTable t = dbs.getTable(request.getTableName());
          if (t != null)
          {
            dbs.removeTable(t);
            if (logger.isDebugEnabled())
              logger.debug("Removed table '" + request.getTableName()
                  + "' from backend database schema");
          }
          else
            // Unsupported force re-fetch from db
            setSchemaIsDirty(true);
        }
        else
          // Unsupported force re-fetch from db
          setSchemaIsDirty(true);
      }
      else
        // Unsupported force re-fetch from db
        setSchemaIsDirty(true);
    }
    else
      // Unsupported force re-fetch from db
      setSchemaIsDirty(true);
  }

  //
  // Driver compliance
  //

  /**
   * @return the driver compliance to C-JDBC requirements.
   */
  public DriverCompliance getDriverCompliance()
  {
    return driverCompliance;
  }

  /**
   * Check if the driver used by this backend is compliant with C-JDBC needs.
   * 
   * @throws SQLException if the driver is not compliant
   */
  public void checkDriverCompliance() throws SQLException
  {
    if (connectionManagers.isEmpty())
      throw new SQLException(Translate.get("backend.null.connection.manager",
          new String[]{name, url}));

    AbstractConnectionManager connectionManager;
    Iterator iter = connectionManagers.values().iterator();
    connectionManager = (AbstractConnectionManager) iter.next();

    try
    {
      if (!driverCompliance.complianceTest(url, connectionManager.getLogin(),
          connectionManager.getPassword(), connectionManager.getDriverPath(),
          connectionManager.getDriverClassName(), connectionTestStatement))
        throw new SQLException(Translate.get("backend.driver.not.compliant",
            driverClassName));
    }
    catch (ConnectException e)
    {
      throw new SQLException(Translate.get("backend.cannot.connect.to", e));
    }
  }

  /**
   * Returns the JDBC URL used to access the database.
   * 
   * @return a JDBC URL
   */
  public String getURL()
  {
    return url;
  }

  /*
   * Rewriting Rule management
   */

  /**
   * Add a <code>AbstractRewritingRule</code> at the end of the rule list.
   * 
   * @param rule a AbstractRewritingRule
   */
  public void addRewritingRule(AbstractRewritingRule rule)
  {
    if (rewritingRules == null)
      rewritingRules = new ArrayList();
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.rewriting.rule.add", new String[]{
          rule.getQueryPattern(), rule.getRewrite()}));
    rewritingRules.add(rule);
  }

  /**
   * Rewrite the current query according to the rewriting rules.
   * 
   * @param sqlQuery request to rewrite
   * @return the rewritten SQL query according to rewriting rules.
   */
  public String rewriteQuery(String sqlQuery)
  {
    if (rewritingRules == null)
      return sqlQuery;
    int size = rewritingRules.size();
    for (int i = 0; i < size; i++)
    {
      AbstractRewritingRule rule = (AbstractRewritingRule) rewritingRules
          .get(i);
      sqlQuery = rule.rewrite(sqlQuery);
      if (rule.hasMatched())
      { // Rule matched, query rewriten
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("backend.rewriting.query", sqlQuery));
        if (rule.isStopOnMatch())
          break; // Ok, stop here.
      }
    }
    return sqlQuery;
  }

  /*
   * Debug/Monitoring
   */

  /**
   * Get xml information about this backend.
   * 
   * @return xml formatted information on this database backend.
   */
  public synchronized String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_DatabaseBackend + " "
        + DatabasesXmlTags.ATT_name + "=\"" + name + "\" "
        + DatabasesXmlTags.ATT_driver + "=\"" + driverClassName + "\" "
        + DatabasesXmlTags.ATT_url + "=\"" + url + "\" "
        + DatabasesXmlTags.ATT_connectionTestStatement + "=\""
        + connectionTestStatement + "\">");

    boolean expandSchema = this.schema != null
        && dynamicPrecision == DatabaseBackendSchemaConstants.DynamicPrecisionStatic;

    info.append(getSchemaXml(expandSchema));

    if (rewritingRules != null)
    {
      int size = rewritingRules.size();
      for (int i = 0; i < size; i++)
        info.append(((AbstractRewritingRule) rewritingRules.get(i)).getXml());
    }
    if (connectionManagers != null)
    {
      if (connectionManagers.isEmpty() == false)
      {
        AbstractConnectionManager connectionManager;
        Iterator iter = connectionManagers.values().iterator();
        while (iter.hasNext())
        {
          connectionManager = (AbstractConnectionManager) iter.next();
          info.append(connectionManager.getXml());
        }
      }
    }
    info.append("</" + DatabasesXmlTags.ELT_DatabaseBackend + ">");
    return info.toString();
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean#getSchemaXml(boolean)
   */
  public String getSchemaXml(boolean expandSchema)
  {
    StringBuffer info = new StringBuffer();
    info.append("<"
        + DatabasesXmlTags.ELT_DatabaseSchema
        + " "
        + DatabasesXmlTags.ATT_dynamicPrecision
        + "=\""
        + DatabaseBackendSchemaConstants
            .getDynamicSchemaLevel(dynamicPrecision) + "\" "
        + DatabasesXmlTags.ATT_gatherSystemTables + "=\""
        + (gatherSystemTables ? "true" : "false") + "\">");
    synchronized (this)
    {
      if (expandSchema && (schema != null))
        info.append(schema.getXml());
    }
    info.append("</" + DatabasesXmlTags.ELT_DatabaseSchema + ">");
    return info.toString();
  }

  /**
   * @return Returns the activeTransactions.
   */
  public ArrayList getActiveTransactions()
  {
    return activeTransactions;
  }

  /**
   * Get data about this backend. Format is:
   * 
   * <pre>
   * data[0] = this.name;
   * data[1] = this.driverClassName;
   * data[2] = this.url;
   * data[3] = String.valueOf(this.activeTransactions.size());
   * data[4] = String.valueOf(this.pendingRequests.size());
   * data[5] = String.valueOf(this.isReadEnabled());
   * data[6] = String.valueOf(this.isWriteEnabled());
   * data[7] = String.valueOf(this.isInitialized());
   * data[8] = String.valueOf(this.schemaIsStatic);
   * data[9] = String.valueOf(this.connectionManagers.size());
   * data[10] = String.valueOf(getTotalActiveConnections());
   * data[11] = String.valueOf(totalRequest);
   * data[12] = String.valueOf(totalTransactions);
   * data[13] = lastKnownCheckpoint;
   *</pre>
   * 
   * @return an array of strings
   */
  public String[] getBackendData()
  {
    String[] data = new String[14];
    data[0] = this.name;
    data[1] = this.driverClassName;
    data[2] = this.url;
    data[3] = String.valueOf(this.activeTransactions.size());
    data[4] = String.valueOf(this.pendingRequests.size());
    data[5] = String.valueOf(this.isReadEnabled());
    data[6] = String.valueOf(this.isWriteEnabled());
    try
    {
      data[7] = String.valueOf(this.isInitialized());
    }
    catch (Exception e)
    {
      data[7] = "unknown";
    }
    data[8] = String.valueOf(this.schemaIsStatic);

    data[9] = String.valueOf(this.connectionManagers.size());
    data[10] = String.valueOf(getTotalActiveConnections());
    data[11] = String.valueOf(totalRequest);
    data[12] = String.valueOf(totalTransactions);
    if (lastKnownCheckpoint == null || lastKnownCheckpoint.equalsIgnoreCase(""))
      data[13] = "<unknown>";
    else
      data[13] = lastKnownCheckpoint;
    return data;
  }

  /**
   * Get the statistics of the backend.
   * 
   * @return a BackendStatistics
   */
  public BackendStatistics getBackendStats()
  {
    BackendStatistics stats = new BackendStatistics();
    stats.setBackendName(name);
    stats.setDriverClassName(driverClassName);
    stats.setUrl(url);
    stats.setNumberOfActiveTransactions(activeTransactions.size());
    stats.setNumberOfPendingRequests(pendingRequests.size());
    stats.setReadEnabled(isReadEnabled());
    stats.setWriteEnabled(isWriteEnabled());
    String initializationStatus = "<unknown>";
    try
    {
      initializationStatus = String.valueOf(this.isInitialized());
    }
    catch (Exception e)
    {
    }
    stats.setInitializationStatus(initializationStatus);
    stats.setSchemaStatic(schemaIsStatic);
    stats.setNumberOfConnectionManagers(connectionManagers.size());
    stats.setNumberOfTotalActiveConnections(getTotalActiveConnections());
    stats.setNumberOfTotalRequests(totalRequest);
    stats.setNumberOfTotalTransactions(totalTransactions);
    if (lastKnownCheckpoint == null || lastKnownCheckpoint.equalsIgnoreCase(""))
      stats.setLastKnownCheckpoint("<unknown>");
    else
      stats.setLastKnownCheckpoint(lastKnownCheckpoint);
    return stats;
  }

  /**
   * Get the total number of active connections for this backend
   * 
   * @return number of active connections for all
   *         <code>AbstractConnectionManager</code> connected to this backend
   */
  public long getTotalActiveConnections()
  {
    int activeConnections = 0;
    Iterator iter = connectionManagers.keySet().iterator();
    while (iter.hasNext())
      activeConnections += ((AbstractConnectionManager) connectionManagers
          .get(iter.next())).getCurrentNumberOfConnections();
    return activeConnections;
  }

  /**
   * Returns the total number of transactions executed by this backend.
   * 
   * @return Total number of transactions.
   */
  public int getTotalTransactions()
  {
    return totalTransactions;
  }

  /**
   * Returns the total number of read requests executed by this backend.
   * 
   * @return Returns the totalReadRequest.
   */
  public int getTotalReadRequest()
  {
    return totalReadRequest;
  }

  /**
   * Returns the total number of write requests executed by this backend.
   * 
   * @return Returns the totalWriteRequest.
   */
  public int getTotalWriteRequest()
  {
    return totalWriteRequest;
  }

  /**
   * Returns the total number of requests executed by this backend.
   * 
   * @return Returns the totalRequest.
   */
  public int getTotalRequest()
  {
    return totalRequest;
  }

  /**
   * setLastKnownCheckpoint for this backend
   * 
   * @param checkpoint the checkpoint
   */
  public void setLastKnownCheckpoint(String checkpoint)
  {
    this.lastKnownCheckpoint = checkpoint;
  }

  /**
   * Returns the lastKnownCheckpoint value.
   * 
   * @return Returns the lastKnownCheckpoint.
   */
  public String getLastKnownCheckpoint()
  {
    return lastKnownCheckpoint;
  }

  /**
   * Returns the databaseProductName value.
   * 
   * @return Returns the databaseProductName.
   */
  public String getDatabaseProductName()
  {
    return driverCompliance.getDatabaseProductName();
  }

  /**
   * Returns the rewritingRules value.
   * 
   * @return Returns the rewritingRules.
   */
  public ArrayList getRewritingRules()
  {
    return rewritingRules;
  }

  /**
   * Sets the rewritingRules value.
   * 
   * @param rewritingRules The rewritingRules to set.
   */
  public void setRewritingRules(ArrayList rewritingRules)
  {
    this.rewritingRules = rewritingRules;
  }

  /**
   * Returns a deeply copied clone of this backend Will use the same rewriting
   * rules and will get new instance of connection managers with the same
   * configuration
   * 
   * @param newName the new name for this new backend
   * @param parameters a set of parameters to use to replace values from the
   *          copied backend. <br>
   *          The different parameters are: <br>
   *          <ul>
   *          <li><tt>driverPath</tt>: the path to the driver</li>
   *          <li><tt>driver</tt>: the driver class name</li>
   *          <li><tt>url</tt>: the url to connect to the database</li>
   *          <li><tt>connectionTestStatement</tt>: the query to test the
   *          connection</li>
   *          </ul>
   *          <br>
   * @return <code>DatabaseBackend</code> instance
   * @throws Exception if cannot proceed the copy
   */
  public DatabaseBackend copy(String newName, Map parameters) throws Exception
  {
    // Get the parameters from the backend if they are not specified, or take
    // them from the map of parameters otherwise.
    String fromDriverPath = parameters
        .containsKey(DatabasesXmlTags.ATT_driverPath) ? (String) parameters
        .get(DatabasesXmlTags.ATT_driverPath) : this.getDriverPath();

    String fromDriverClassName = parameters
        .containsKey(DatabasesXmlTags.ATT_driver) ? (String) parameters
        .get(DatabasesXmlTags.ATT_driver) : this.getDriverClassName();

    String fromUrl = parameters.containsKey(DatabasesXmlTags.ATT_url)
        ? (String) parameters.get(DatabasesXmlTags.ATT_url)
        : this.getURL();

    String fromConnectionTestStatement = parameters
        .containsKey(DatabasesXmlTags.ATT_connectionTestStatement)
        ? (String) parameters.get(DatabasesXmlTags.ATT_connectionTestStatement)
        : this.getConnectionTestStatement();

    // Create the new backend object
    DatabaseBackend newBackend = new DatabaseBackend(newName, fromDriverPath,
        fromDriverClassName, fromUrl, virtualDatabaseName, writeCanBeEnabled,
        fromConnectionTestStatement);

    // Clone dynamic precision
    newBackend.setDynamicPrecision(this.dynamicPrecision,
        this.gatherSystemTables, this.schemaName);

    // Set the rewriting rules and the connection managers as the backend we
    // are copying
    newBackend.setRewritingRules(this.getRewritingRules());

    // Set Connection managers
    HashMap fromConnectionManagers = this.getConnectionManagers();
    Iterator iter = fromConnectionManagers.keySet().iterator();

    String vlogin = null;
    AbstractConnectionManager connectionManager;
    while (iter.hasNext())
    {
      vlogin = (String) iter.next();
      connectionManager = (AbstractConnectionManager) fromConnectionManagers
          .get(vlogin);
      newBackend.addConnectionManager(vlogin, connectionManager.copy(fromUrl,
          newName));
    }

    return newBackend;
  }

  /**
   * Returns the isBackuping value.
   * 
   * @return Returns the isBackuping.
   */
  public boolean isBackuping()
  {
    return state == BackendState.BACKUPING;
  }

  /**
   * Set the state of a backend
   * 
   * @param state see BackendState for a possible list of the different state
   * @see org.objectweb.cjdbc.common.shared.BackendState
   */
  public synchronized void setState(int state)
  {
    switch (state)
    {
      case BackendState.UNKNOWN :
        lastKnownCheckpoint = null;
        break;
      case BackendState.READ_ENABLED_WRITE_DISABLED :
      case BackendState.READ_ENABLED_WRITE_ENABLED :
      case BackendState.READ_DISABLED_WRITE_ENABLED :
      case BackendState.DISABLING :
      case BackendState.BACKUPING :
      case BackendState.RECOVERING :
      case BackendState.REPLAYING :
      case BackendState.DISABLED :
        break;
      default :
        throw new IllegalArgumentException("Unknown backend state:" + state);
    }
    this.state = state;
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.state.changed", new String[]{name,
          getState()}));
    notifyStateChange();
  }

  /**
   * Notify the state of the backend has changed. This does two things: 1.
   * Change the state of the backend stored in the recovery log 2. Sends a jmx
   * notification. This method has all the data prefilled because we know all
   * the parameters in advance, except the type of the notification.
   * 
   * @see CjdbcNotificationList
   */
  public void notifyStateChange()
  {
    if (stateListener != null)
      stateListener.changeState(this);
    notifyJmx(getState());
  }

  /**
   * Sends JMX notification
   * 
   * @param type notification type
   * @see CjdbcNotificationList
   */
  public void notifyJmx(String type)
  {
    notifyJmx(type, CjdbcNotificationList.NOTIFICATION_LEVEL_INFO, Translate
        .get(type, getName()));
  }

  /**
   * Sends JMX error notification
   * 
   * @param e <tt>Exception</tt> object. Only the message will be used
   * @param type notification type
   * @see CjdbcNotificationList
   */
  public void notifyJmxError(String type, Exception e)
  {
    notifyJmx(type, CjdbcNotificationList.NOTIFICATION_LEVEL_ERROR, Translate
        .get(type, new String[]{getName(), e.getMessage()}));

  }

  private void notifyJmx(String type, String level, String message)
  {
    if (MBeanServerManager.isJmxEnabled())
    {
      // Send notification
      Hashtable data = new Hashtable();
      data.put(CjdbcNotificationList.DATA_DATABASE, getVirtualDatabaseName());
      data.put(CjdbcNotificationList.DATA_DRIVER, getDriverClassName());
      String checkpoint = getLastKnownCheckpoint();
      checkpoint = (checkpoint == null) ? "" : checkpoint;
      data.put(CjdbcNotificationList.DATA_CHECKPOINT, checkpoint);
      data.put(CjdbcNotificationList.DATA_NAME, getName());
      data.put(CjdbcNotificationList.DATA_URL, getURL());
      RmiConnector.broadcastNotification(this, type, level, message, data);
    }
  }

  /**
   * Returns the writeCanBeEnabled value.
   * 
   * @return Returns the writeCanBeEnabled.
   */
  public boolean isWriteCanBeEnabled()
  {
    return writeCanBeEnabled;
  }

  /**
   * Sets the stateListener value.
   * 
   * @param stateListener The stateListener to set.
   */
  public void setStateListener(BackendStateListener stateListener)
  {
    this.stateListener = stateListener;
  }

  /**
   * String description
   * 
   * @return a string description of the backend.
   */
  public String toString()
  {
    return "Backend: Name[" + this.name + "] State[" + this.state
        + "] JDBCConnected[" + isJDBCConnected() + "] ActiveTransactions["
        + activeTransactions.size() + "] PendingRequests["
        + pendingRequests.size() + "]";
  }

}