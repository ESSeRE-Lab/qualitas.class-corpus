/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: c-jdbc@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General public final License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General public final License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General public final License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Emmanuel Cecchet. 
 * Contributor(s): Mathieu Peltier, Nicolas Modrzyk.
 */

package org.objectweb.cjdbc.controller.core;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.objectweb.cjdbc.common.exceptions.ControllerException;
import org.objectweb.cjdbc.common.exceptions.VirtualDatabaseException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.JmxConstants;
import org.objectweb.cjdbc.common.jmx.JmxException;
import org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean;
import org.objectweb.cjdbc.common.jmx.notifications.CjdbcNotificationList;
import org.objectweb.cjdbc.common.log.LogManager;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.common.xml.ControllerXmlTags;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;
import org.objectweb.cjdbc.common.xml.XmlTools;
import org.objectweb.cjdbc.controller.core.security.ControllerSecurityManager;
import org.objectweb.cjdbc.controller.core.shutdown.ControllerForceShutdownThread;
import org.objectweb.cjdbc.controller.core.shutdown.ControllerSafeShutdownThread;
import org.objectweb.cjdbc.controller.core.shutdown.ControllerShutdownThread;
import org.objectweb.cjdbc.controller.core.shutdown.ControllerWaitShutdownThread;
import org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean;
import org.objectweb.cjdbc.controller.jmx.MBeanServerManager;
import org.objectweb.cjdbc.controller.jmx.RmiConnector;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.controller.xml.DatabasesParser;

/**
 * The C-JDBC controller main class. It registers itself in the RMI registry and
 * waits for C-JDBC driver requests.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:duncan@mightybot.com">Duncan Smith </a>
 * @version 1.0
 */
public final class Controller extends AbstractStandardMBean
    implements
      ControllerMBean,
      XmlComponent
{

  /** C-JDBC controller port number listening for driver connections */
  private int                       portNumber;
  private int                       backlogSize;

  /**
   * The IP address to bind the controller to. Useful for machines that contain
   * multiple network interface cards and wish to bind to a specific card.
   * Default evaluates to localhost IP address (127.0.0.1).
   */
  private String                    ipAddress;

  /** Thread that listens for driver connections */
  private ControllerServerThread    connectionThread;

  /** Logger instance. */
  static Trace                      logger = Trace
                                               .getLogger("org.objectweb.cjdbc.controller.core.Controller");

  /** Hashtable of <code>VirtualDatabase</code> objects. */
  private Hashtable                 virtualDatabases;

  /** Hashtable of options */
  private Hashtable                 configuration;

  /** Security Manager */
  private ControllerSecurityManager security;

  /** Report Manager */
  private ReportManager             report;

  private boolean                   isShuttingDown;

  /* Constructor(s) */

  /**
   * Creates a new <code>Controller</code> instance.
   * 
   * @param ipAddress bind the controller to this ipAddress
   * @param port bind the controller to listen to this port
   * @param backlog backlog connection size
   * @exception NotCompliantMBeanException in case the bean does not comply with
   *              jmx
   * @exception JmxException the bean could not be registered
   */
  public Controller(String ipAddress, int port, int backlog)
      throws NotCompliantMBeanException, JmxException
  {
    super(ControllerMBean.class);
    virtualDatabases = new Hashtable();
    this.ipAddress = ipAddress;
    this.portNumber = port;
    this.backlogSize = backlog;
    ObjectName name = JmxConstants.getControllerObjectName();
    MBeanServerManager.registerMBean(this, name);
  }

  //
  // Virtual databases management
  //

  /**
   * Adds virtual databases contained in the XML document given as a String. If
   * a virtual database name is provided, only this database is loaded with the
   * provided autoLoad and checkpoint information.
   * 
   * @param xml XML configuration file content
   * @param vdbName optional virtual database name to autoload
   * @param autoEnable autoenable backend mode for virtual database
   * @param checkpoint checkpoint name if autoEnable is set to force
   * @throws ControllerException if an error occurs
   */
  public void addVirtualDatabases(String xml, String vdbName, int autoEnable,
      String checkpoint) throws ControllerException
  {
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("controller.add.virtualdatabase", vdbName));
    if (vdbName != null && this.hasVirtualDatabase(vdbName))
    {
      throw new ControllerException(Translate
          .get("controller.add.virtualdatabase.already.used"));
    }
    try
    {
      DatabasesParser parser = new DatabasesParser(this, vdbName, autoEnable,
          checkpoint);
      parser.readXML(xml, true);
    }
    catch (Exception e)
    {
      String msg = Translate.get("controller.add.virtualdatabase.failed", e);
      logger.warn(msg, e);
      throw new ControllerException(msg);
    }
  }

  /**
   * Register a VirtualDatabase with default options
   * 
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#addVirtualDatabases(String)
   */
  public void addVirtualDatabases(String xml) throws ControllerException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug(Translate.get("controller.loading.virtualdatabase"));
    }
    this.addVirtualDatabases(xml, null,
        ControllerConstants.AUTO_ENABLE_BACKEND,
        ControllerConstants.DATABASE_DEFAULT_CHECKPOINT);
  }

  /**
   * Registers a new <code>VirtualDatabase</code> in this controller.
   * 
   * @param vdb the <code>VirtualDatabase</code> to register
   * @throws ControllerException if an error occurs
   */
  public void addVirtualDatabase(VirtualDatabase vdb)
      throws ControllerException
  {
    this.addVirtualDatabase(vdb, ControllerConstants.AUTO_ENABLE_BACKEND,
        ControllerConstants.DATABASE_DEFAULT_CHECKPOINT);
  }

  /**
   * Add the virtual database with the specified options
   * 
   * @param vdb the <code>VirtualDatabase</code> object to add
   * @param autoLoad specified if backends should be enabled
   * @param checkPoint specified the checkPoint to recover from, leave null if
   *          no recovery speficied
   * @throws ControllerException if database already exists on the specified
   *           <code>Controller</code> object
   */
  public synchronized void addVirtualDatabase(VirtualDatabase vdb,
      int autoLoad, String checkPoint) throws ControllerException
  {
    // Add the database or retrieve it if it already exists
    if (hasVirtualDatabase(vdb.getDatabaseName()))
    {
      String msg = Translate.get("controller.database.already.exists", vdb
          .getDatabaseName());
      logger.warn(msg);
      throw new ControllerException(msg);
    }
    else
    {
      virtualDatabases.put(vdb.getDatabaseName(), vdb);

      // Send notification
      if (MBeanServerManager.isJmxEnabled())
      {
        Hashtable databases = new Hashtable();
        try
        {
          databases.put("backends", vdb.getAllBackendNames());
        }
        catch (VirtualDatabaseException e)
        {
          // ignore
        }
        RmiConnector.broadcastNotification(this,
            CjdbcNotificationList.CONTROLLER_VIRTUALDATABASE_ADDED,
            CjdbcNotificationList.NOTIFICATION_LEVEL_INFO, Translate.get(
                "notification.virtualdatabase.added", vdb
                    .getVirtualDatabaseName()), databases);
      }
    }

    // Enable backends with the proper states
    try
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("controller.database.autoenable", autoLoad));

      switch (autoLoad)
      {
        case ControllerConstants.AUTO_ENABLE_TRUE :
          vdb.enableAllBackendsFromCheckpoint();
          break;
        case ControllerConstants.AUTO_ENABLE_FALSE :
          break;
        case ControllerConstants.AUTO_ENABLE_FORCE :
          logger.warn("Backends enabled in force mode from checkpoint "
              + checkPoint);
          vdb.forceEnableAllBackendsFromCheckpoint(checkPoint);
          break;
        default :
          logger
              .error("Unsupported autoEnabledBackends mode in controller configuration");
          break;
      }
    }
    catch (VirtualDatabaseException e)
    {
      throw new ControllerException(e);
    }

    logger.info(Translate.get("controller.add.virtualdatabase", vdb
        .getDatabaseName()));
  }

  /**
   * Gets the <code>VirtualDatabase</code> object corresponding to a virtual
   * database name.
   * 
   * @param virtualDatabaseName the virtual database name
   * @return a <code>VirtualDatabase</code> object or null if not found
   */
  public VirtualDatabase getVirtualDatabase(String virtualDatabaseName)
  {
    return (VirtualDatabase) virtualDatabases.get(virtualDatabaseName);
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#getVirtualDatabaseNames()
   */
  public ArrayList getVirtualDatabaseNames()
  {
    ArrayList result = new ArrayList();
    for (Iterator iter = virtualDatabases.values().iterator(); iter.hasNext();)
      result.add(((VirtualDatabase) iter.next()).getVirtualDatabaseName());
    return result;
  }

  /**
   * Returns information about the available virtual databases.
   * 
   * @return ArrayList of information about virtual databases.
   */
  public ArrayList getVirtualDatabases()
  {
    ArrayList result = new ArrayList();
    for (Iterator iter = virtualDatabases.values().iterator(); iter.hasNext();)
      result.add(iter.next());
    return result;
  }

  /**
   * Tests if a <code>VirtualDatabase</code> of a given name exists in this
   * controller.
   * 
   * @param name the virtual database name
   * @return <code>true</code> if the virtual database exists
   */
  public boolean hasVirtualDatabase(String name)
  {
    return virtualDatabases.containsKey(name);
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#removeVirtualDatabase(String)
   */
  public String removeVirtualDatabase(String virtualname)
      throws ControllerException
  {
    if (hasVirtualDatabase(virtualname))
    {
      VirtualDatabase vdb = (VirtualDatabase) virtualDatabases.get(virtualname);
      try
      {
        vdb.disableAllBackends();
      }
      catch (VirtualDatabaseException e)
      {
        throw new ControllerException(e);
      }
      this.virtualDatabases.remove(virtualname);

      // Send notification
      if (MBeanServerManager.isJmxEnabled())
      {
        Hashtable databases = new Hashtable();
        try
        {
          databases.put("backends", vdb.getAllBackendNames());
        }
        catch (VirtualDatabaseException e)
        {
          // ignore
        }
        RmiConnector.broadcastNotification(this,
            CjdbcNotificationList.CONTROLLER_VIRTUALDATABASE_REMOVED,
            CjdbcNotificationList.NOTIFICATION_LEVEL_INFO, Translate.get(
                "notification.virtualdatabase.removed", vdb
                    .getVirtualDatabaseName()), databases);
      }
    }
    return Translate.get("controller.removeVirtualDatabase.success",
        virtualname);
  }

  //
  // Controller operations
  //

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#addDriver(byte[])
   */
  public void addDriver(byte[] bytes) throws Exception
  {
    // Try to find drivers directory in the classpath
    File driversDirectory = null;
    URL url = Controller.class
        .getResource(ControllerConstants.C_JDBC_DRIVER_JAR_FILE);
    boolean error = false;
    if (url != null)
    {
      driversDirectory = (new File(URLDecoder.decode(url.getFile())))
          .getParentFile();
      error = (driversDirectory == null) || !driversDirectory.exists();
    }

    if (error)
    {
      String msg = Translate.get("controller.driver.dir.not.found");
      logger.error(msg);
      throw new ControllerException(msg);
    }

    // Read the array of bytes to a file
    File temp = null;
    try
    {
      temp = File.createTempFile("driver", "zip", driversDirectory);
      FileOutputStream output = new FileOutputStream(temp);
      output.write(bytes);
      output.close();
    }
    catch (IOException e)
    {
      String msg = Translate.get("controller.add.jar.read.failed", e);
      logger.error(msg);
      throw new ControllerException(msg);
    }

    // Unzip the file content
    try
    {
      Enumeration entries;
      ZipFile zipFile = new ZipFile(temp);

      // Read the file
      int lenght;
      InputStream in;
      BufferedOutputStream out;
      byte[] buffer = new byte[1024];

      entries = zipFile.entries();
      while (entries.hasMoreElements())
      {
        ZipEntry entry = (ZipEntry) entries.nextElement();

        if (entry.isDirectory())
        {
          // Create the directory
          if (logger.isDebugEnabled())
            logger.debug(Translate.get("controller.add.jar.extract.dir", entry
                .getName()));

          (new File(driversDirectory, entry.getName())).mkdir();
          continue;
        }

        // Extract the file
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("controller.add.jar.extract.file", entry
              .getName()));

        in = zipFile.getInputStream(entry);
        out = new BufferedOutputStream(new FileOutputStream(driversDirectory
            + System.getProperty("file.separator") + entry.getName()));
        while ((lenght = in.read(buffer)) >= 0)
          out.write(buffer, 0, lenght);

        in.close();
        out.close();
      }

      zipFile.close();
      temp.delete();
      logger.info(Translate.get("controller.add.jar.to.directory",
          driversDirectory.toString()));
    }
    catch (IOException e)
    {
      String msg = Translate.get("controller.driver.extract.failed", e);
      logger.error(msg);
      throw new ControllerException(msg);
    }
  }

  /**
   * Read a XML configuration file and load only the
   * <code>VirtualDatabase</code> specified in the arguments list
   * 
   * @param filename XML configuration file name to take info on
   *          <code>VirtualDatabase</code>
   * @param virtualName the only database to load, null if should load all
   * @param autoLoad specifies if the backends should be enabled automatically
   *          after loading
   * @param checkPoint checkPoint to recover from when enabling backends. Leave
   *          <code>null</code> if no recovery option is needed.
   * @return a diagnostic message (success or error)
   * @throws Exception if an error occurs
   */
  public String loadXmlConfiguration(String filename, String virtualName,
      int autoLoad, String checkPoint) throws Exception
  {
    FileReader fileReader = null;
    try
    {
      filename = filename.trim();
      try
      {
        fileReader = new FileReader(filename);
      }
      catch (FileNotFoundException fnf)
      {
        return Translate.get("controller.file.not.found", filename);
      }

      // Read the file
      BufferedReader in = new BufferedReader(fileReader);
      StringBuffer xml = new StringBuffer();
      String line;
      do
      {
        line = in.readLine();
        if (line != null)
          xml.append(line);
      }
      while (line != null);

      // Send it to the controller
      addVirtualDatabases(xml.toString(), virtualName, autoLoad, checkPoint);
      return Translate.get("controller.file.send", filename);
    }
    catch (Exception e)
    {
      logger.error(Translate.get("controller.loadXml.failed", e), e);
      throw new ControllerException(Translate.get("controller.loadXml.failed",
          e));
    }
    finally
    {
      if (fileReader != null)
        fileReader.close();
    }
  }

  /**
   * Save current configuration of the controller to a default file
   * 
   * @return Status message
   * @throws VirtualDatabaseException if an error occurs
   * @see org.objectweb.cjdbc.controller.core.ControllerConstants#getSaveFile
   */
  public String saveConfiguration() throws VirtualDatabaseException
  {
    String msg = Translate.get("controller.save.configuration.failed");
    try
    {
      String configurationFile = ControllerConstants
          .getSaveFile(new SimpleDateFormat("yyyy-MM-dd-HH-mm")
              .format(new Date()));
      DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
          new FileOutputStream(configurationFile)));
      StringBuffer xml = new StringBuffer();
      xml.append(XmlTools.prettyXml(getXmlVirtualDatabases()));
      String prettyXml = xml.toString();
      // ugly hack to insert the doctype which has been stripped
      // when prettyfying the xml
      prettyXml = XmlTools.insertCjdbcDoctype(prettyXml);
      dos.write(prettyXml.getBytes());
      dos.close();
      msg = Translate.get("controller.save.configuration", configurationFile);
    }
    catch (Exception e)
    {
      msg = Translate.get("controller.save.configuration.failed", e);
      logger.error(msg);
    }
    return msg;
  }

  //
  // Controller shutdown
  //
  /**
   * Create report about fatal error
   * 
   * @param fatal the cause of the fatal error
   */
  public void endOfController(Exception fatal)
  {
    logger.fatal(Translate.get("fatal.error"));
    if (report.isGenerateOnFatal())
    {
      new ReportManager(this, fatal).generate();
      logger.info(Translate.get("fatal.report.generated", report
          .getReportLocation()
          + File.separator + ControllerConstants.REPORT_FILE));
    }
    Runtime.getRuntime().exit(1);
  }

  /**
   * Access the connection thread. Need this for shutting down
   * 
   * @return <code>connectionThread</code>
   */
  public ControllerServerThread getConnectionThread()
  {
    return connectionThread;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#isShuttingDown()
   */
  public boolean isShuttingDown()
  {
    return isShuttingDown;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#shutdown(int)
   */
  public void shutdown(int level) throws ControllerException
  {
    ControllerShutdownThread shutdownThread = null;
    synchronized (this)
    {
      if (isShuttingDown())
      {
        logger.info(Translate.get("controller.already.shutting.down", this
            .getControllerName()));
        return;
      }

      if (isSecurityEnabled() && !security.getAllowConsoleShutdown())
        throw new ControllerException(Translate
            .get("controller.shutdown.refused"));

      switch (level)
      {
        case Constants.SHUTDOWN_WAIT :
          shutdownThread = new ControllerWaitShutdownThread(this);
          logger.info(Translate.get("controller.shutdown.type.wait", this
              .getControllerName()));
          break;
        case Constants.SHUTDOWN_SAFE :
          isShuttingDown = true;
          shutdownThread = new ControllerSafeShutdownThread(this);
          logger.info(Translate.get("controller.shutdown.type.safe", this
              .getControllerName()));
          break;
        case Constants.SHUTDOWN_FORCE :
          isShuttingDown = true;
          shutdownThread = new ControllerForceShutdownThread(this);
          logger.warn(Translate.get("controller.shutdown.type.force", this
              .getControllerName()));
          break;
        default :
          String msg = Translate
              .get("controller.shutdown.unknown.level", level);
          logger.error(msg);
          throw new RuntimeException(msg);
      }
    }

    Thread thread = new Thread(shutdownThread.getShutdownGroup(),
        shutdownThread, "Controller Shutdown Thread");
    thread.start();

    try
    {
      logger.info("Waiting for shutdown");
      thread.join();
      logger.info("Shutdown over");
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Launches the C-JDBC controller and bind it with RMI registry. The available
   * options are:
   * <ul>
   * <li><code>-h</code> or <code>--help</code> <code>&lt;port&gt;</code>:
   * displays usage informations.</li>
   * <li><code>-j</code> or <code>--jmx</code> <code>&lt;port&gt;</code>:
   * optinal JMX server HTTP adaptor port number.</li>
   * <li><code>-n</code> or <code>--name</code> <code>&lt;name&gt;</code>:
   * optional controller name.</li>
   * <li><code>-i</code> or <code>--ip</code> <code>&lt;ip&gt;</code>:
   * optional IP address to beind the controller to.</li>
   * <li><code>-r</code> or <code>--rmi</code> <code>&lt;port&gt;</code>:
   * optional RMI registry port number.</li>
   * <li><code>-v</code> or <code>--version</code>: displays version
   * informations.</li>
   * </ul>
   * <p>
   * The controller starts listening for socket connections on the default port.
   * Jmx is configured, and a virtual database can be added.
   * <p>
   * {@link org.objectweb.cjdbc.controller.core.ControllerConstants#DEFAULT_PORT}
   * Default Listening port
   * 
   * @param args command line arguments (see above)
   * @throws Exception when everything goes wrong
   */
  public static void main(String[] args) throws Exception
  {
    logger.info(getVersion());

    System.setProperty("javax.management.builder.initial",
        org.objectweb.cjdbc.controller.jmx.MBeanServerBuilder.class.getName());

    ControllerFactory conf = new ControllerFactory(args);
    Controller controller = conf.getController();
    if (controller != null)
      controller.launch();
    else
      throw new Exception(Translate.get("controller.configure.failed"));
  }

  /**
   * Actively launch the <code>controller</code>. Add startup actions here to
   * avoid them in <method>main </method>
   */
  public void launch()
  {
    connectionThread = new ControllerServerThread(this);
    connectionThread.start();

    SimpleDateFormat formatter = new SimpleDateFormat(
        "yyyy.MM.dd ww 'at' hh:mm:ss a zzz");
    Date day = new Date();
    String date = formatter.format(day);
    logger.info(Translate.get("controller.date", date));
    logger.info(Translate.get("controller.ready", getControllerName()));
  }

  //
  // Controller information
  //

  /**
   * Returns the controller name.
   * 
   * @return String
   */
  public String getControllerName()
  {
    return ipAddress + ":" + portNumber;
  }

  /**
   * Get the IP address to bind the controller to
   * 
   * @return the IP address
   */
  public String getIPAddress()
  {
    return ipAddress;
  }

  /**
   * Set the IP address to bind the controller to
   * 
   * @param ipAddress the IP address to use
   */
  public void setIPAddress(String ipAddress)
  {
    this.ipAddress = ipAddress;
  }

  /**
   * Get the controller port number
   * 
   * @return the port number
   */
  public int getPortNumber()
  {
    return portNumber;
  }

  /**
   * Set the controller backlog size.
   * 
   * @param port the port number to set
   */
  public void setPortNumber(int port)
  {
    portNumber = port;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#getBacklogSize()
   */
  public int getBacklogSize()
  {
    return backlogSize;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#setBacklogSize(int)
   */
  public void setBacklogSize(int size)
  {
    backlogSize = size;
  }

  /**
   * Returns jmx enable
   * 
   * @return jmxEnabled
   */
  public boolean getJmxEnable()
  {
    return MBeanServerManager.isJmxEnabled();
  }

  /**
   * Return the jmx name of this controller (hostname:rmiport)
   * 
   * @return jmx name
   */
  public String getJmxName()
  {
    if (getJmxEnable())
    {
      RmiConnector connector = ((RmiConnector) RmiConnector.getRmiConnectors()
          .get(0));
      return connector.getHostName() + ":" + connector.getPort();
    }
    else
      return getControllerName();
  }

  /**
   * set enable JMX
   * 
   * @param enable true if jmx should be enable.
   */
  public void setJmxEnable(boolean enable)
  {
    configuration.put(ControllerFactory.JMX_ENABLE, "" + enable);
  }

  /**
   * Returns Version as a long String
   * 
   * @return version
   */
  public static String getVersion()
  {
    return Translate.get("controller.info", Constants.VERSION);
  }

  /**
   * Get current configuration options
   * 
   * @return configure a <code>Hashtable</code> with controller options
   */
  public Hashtable getConfiguration()
  {
    return configuration;
  }

  /**
   * Check whether security is enabled or not
   * 
   * @return true if there is not null controller security manager
   */
  public boolean isSecurityEnabled()
  {
    return security != null;
  }

  /**
   * @return Returns the security.
   */
  public ControllerSecurityManager getSecurity()
  {
    return security;
  }

  /**
   * @param security The security to set.
   */
  public void setSecurity(ControllerSecurityManager security)
  {
    this.security = security;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#generateReport()
   */
  public String generateReport() throws Exception
  {
    report.startReport();
    return report.generate();
  }
  
  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#getLoggingConfiguration()
   */
  public String getLoggingConfiguration() throws Exception
  {
    return report.generate();    
  }

  /**
   * Sets the configuration value.
   * 
   * @param configuration The configuration to set.
   */
  public void setConfiguration(Hashtable configuration)
  {
    this.configuration = configuration;
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#getVersionNumber()
   */
  public String getVersionNumber()
  {
    return Constants.VERSION;
  }

  /**
   * @see org.objectweb.cjdbc.controller.jmx.AbstractStandardMBean#getAssociatedString()
   */
  public String getAssociatedString()
  {
    return "controller";
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    try
    {
      String prettyXml = XmlTools.prettyXml(getXmlController());
      return XmlTools.insertCjdbcControllerDoctype(prettyXml);
    }
    catch (Exception e)
    {
      logger.error(Translate.get("controller.xml.transformation.failed", e));
      return e.getMessage();
    }
  }

  /**
   * Return the xml version of the controller.xml file without doc type
   * declaration, just data.
   * 
   * @return controller xml data
   */
  public String getXmlController()
  {
    StringBuffer info = new StringBuffer();
    info.append("<C-JDBC-CONTROLLER>");
    info.append("<" + ControllerXmlTags.ELT_CONTROLLER + " "
        + ControllerXmlTags.ATT_CONTROLLER_IP + "=\"" + this.getIPAddress()
        + "\" " + ControllerXmlTags.ATT_CONTROLLER_PORT + "=\""
        + this.getPortNumber() + "\" " + ">");

    info.append("<" + ControllerXmlTags.ELT_INTERNATIONALIZATION + " "
        + ControllerXmlTags.ATT_LANGUAGE + "=\""
        + Locale.getDefault().getLanguage() + "\"/>");

    if (report.isReportEnabled())
    {
      info.append("<" + ControllerXmlTags.ELT_REPORT + " "
          + ControllerXmlTags.ATT_REPORT_ENABLE_FILE_LOGGING + "=\""
          + report.isEnableFileLogging() + "\" "
          + ControllerXmlTags.ATT_REPORT_HIDE_SENSITIVE_DATA + "=\""
          + report.isHideSensitiveData() + "\" "
          + ControllerXmlTags.ATT_REPORT_GENERATE_ON_FATAL + "=\""
          + report.isGenerateOnFatal() + "\" "
          + ControllerXmlTags.ATT_REPORT_GENERATE_ON_SHUTDOWN + "=\""
          + report.isGenerateOnShutdown() + "\" "
          + ControllerXmlTags.ATT_REPORT_REPORT_LOCATION + "=\""
          + report.getReportLocation() + "\" />");
    }

    if (getJmxEnable())
    {
      info.append("<" + ControllerXmlTags.ELT_JMX + ">");
      if (configuration.containsKey(JmxConstants.ADAPTOR_TYPE_HTTP))
      {
        info.append("<" + ControllerXmlTags.ELT_HTTP_JMX_ADAPTOR + " "
            + ControllerXmlTags.ATT_JMX_ADAPTOR_PORT + "=\""
            + configuration.get(JmxConstants.ADAPTOR_TYPE_HTTP) + "\" />");
      }
      if (configuration.containsKey(JmxConstants.ADAPTOR_TYPE_RMI))
      {
        info.append("<" + ControllerXmlTags.ELT_RMI_JMX_ADAPTOR + " "
            + ControllerXmlTags.ATT_JMX_ADAPTOR_PORT + "=\""
            + configuration.get(JmxConstants.ADAPTOR_TYPE_RMI) + "\" />");
      }

      info.append("</" + ControllerXmlTags.ELT_JMX + ">");
    }

    if (this.isSecurityEnabled())
      info.append(this.getSecurity().getXml());
    info.append("</" + ControllerXmlTags.ELT_CONTROLLER + ">");
    info.append("</C-JDBC-CONTROLLER>");
    return info.toString();
  }

  /**
   * Same as above but for the virtual databases.
   * 
   * @return xml virtual databases data.
   */
  public String getXmlVirtualDatabases()
  {
    try
    {
      StringBuffer info = new StringBuffer();
      info.append(XmlComponent.XML_VERSION);
      info.append("\n");
      info.append("<" + DatabasesXmlTags.ELT_CJDBC + ">");
      ArrayList vdbs = this.getVirtualDatabases();
      for (int i = 0, size = vdbs.size(); i < size; i++)
        info.append(((VirtualDatabase) vdbs.get(i)).getXml());
      info.append("</" + DatabasesXmlTags.ELT_CJDBC + ">");
      return info.toString();
    }
    catch (Exception e)
    {
      logger.error(e.getMessage(), e);
      return e.getMessage();
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#generateLogReport()
   */
  public String generateLogReport() throws Exception
  {
    ReportManager logReport = new ReportManager(this, true);
    return logReport.generateJustLogs();
  }

  // 
  // Logging system
  //

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#refreshLogConfiguration()
   */
  public void refreshLogConfiguration() throws ControllerException
  {
    try
    {
      LogManager.configure(URLDecoder.decode(this.getClass().getResource(
          ControllerConstants.LOG4J_RESOURCE).getFile()));
      if (logger.isDebugEnabled())
        logger.info(Translate.get("controller.refresh.log.success"));
    }
    catch (Exception e)
    {
      throw new ControllerException(Translate
          .get("controller.logconfigfile.not.found"));
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#updateLogConfigurationFile(java.lang.String)
   */
  public void updateLogConfigurationFile(String newConfiguration)
      throws IOException, ControllerException
  {
    File logFile = new File(URLDecoder.decode(getClass().getResource(
        ControllerConstants.LOG4J_RESOURCE).getFile()));
    BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
    writer.write(newConfiguration);
    writer.flush();
    writer.close();
    refreshLogConfiguration();
  }

  /**
   * @see org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean#viewLogConfigurationFile()
   */
  public String viewLogConfigurationFile() throws IOException
  {
    File logFile = new File(URLDecoder.decode(getClass().getResource(
        ControllerConstants.LOG4J_RESOURCE).getFile()));
    BufferedReader reader = new BufferedReader(new FileReader(logFile));
    StringBuffer buffer = new StringBuffer();
    String line;
    while ((line = reader.readLine()) != null)
      buffer.append(line + System.getProperty("line.separator"));
    reader.close();
    return buffer.toString();
  }

  /**
   * Returns the report value.
   * 
   * @return Returns the report.
   */
  public ReportManager getReport()
  {
    return report;
  }

  /**
   * Sets the report value.
   * 
   * @param report The report to set.
   */
  public void setReport(ReportManager report)
  {
    this.report = report;
  }
}