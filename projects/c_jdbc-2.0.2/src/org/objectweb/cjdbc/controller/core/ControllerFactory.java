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
 * Contributor(s): Mathieu Peltier, Nicolas Modrzyk, Duncan Smith.
 */

package org.objectweb.cjdbc.controller.core;

import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Hashtable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.JmxConstants;
import org.objectweb.cjdbc.common.jmx.JmxException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.net.SSLConfiguration;
import org.objectweb.cjdbc.controller.authentication.PasswordAuthenticator;
import org.objectweb.cjdbc.controller.core.security.ControllerSecurityManager;
import org.objectweb.cjdbc.controller.jmx.HttpAdaptor;
import org.objectweb.cjdbc.controller.jmx.MBeanServerManager;
import org.objectweb.cjdbc.controller.jmx.RmiConnector;
import org.objectweb.cjdbc.controller.monitoring.datacollector.DataCollector;
import org.objectweb.cjdbc.controller.xml.ControllerParser;

/**
 * The <code>ControllerFactory</code> class prepares a <code>Controller</code>
 * object by configurating ports, security, loaded databases.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:duncan@mightybot.com">Duncan Smith </a>
 * @version 1.0
 */
public class ControllerFactory extends Hashtable
{
  private static final long  serialVersionUID   = -3766086549425915891L;

  /**
   * The different fields that can be set on the command line.
   */
  /** The Rmi port value */
  public static final String RMI_PORT           = "rmiPort";

  /** The jmx port value */
  public static final String JMX_PORT           = "jmxPort";

  /** The jmx enable value */
  public static final String JMX_ENABLE         = "jmxEnable";

  /** The xml file possibly used to configure controller */
  public static final String XML_FILE           = "xmlFile";

  /** The NIC IP address to bind the controller to */
  public static final String CONTROLLER_IP      = "controllerIP";

  /** The controller port number */
  public static final String CONTROLLER_PORT    = "controllerPort";

  /** The controller backlog size */
  public static final String CONTROLLER_BACKLOG = "controllerBackLogSize";

  /** Add driver enable */
  public static final String ADD_DRIVER_ENABLE  = "addDriverEnable";

  /** Logger instance. */
  static Trace               logger             = Trace
                                                    .getLogger(Controller.class
                                                        .getName());

  private Controller         controller         = null;

  /**
   * Configure the controller with parameters
   * 
   * @param args parameters from the command line
   */
  public ControllerFactory(String[] args)
  {
    System.setProperty("org.xml.sax.driver",
        "org.apache.crimson.parser.XMLReaderImpl");

    URL defaultControllerXmlFile = ControllerFactory.class.getResource("/"
        + ControllerConstants.DEFAULT_CONFIG_FILE);
    if (defaultControllerXmlFile == null)
      logger
          .warn("Unable to find default controller.xml configuration file in CLASSPATH.");
    else
    {
      String file = URLDecoder.decode(defaultControllerXmlFile.getFile());
      this.put(XML_FILE, file);
    }
    this.put(CONTROLLER_IP, ControllerConstants.DEFAULT_IP);
    this.put(CONTROLLER_PORT, "" + ControllerConstants.DEFAULT_PORT);
    this.put(CONTROLLER_BACKLOG, "" + ControllerConstants.DEFAULT_BACKLOG_SIZE);

    // Create options object
    Options options = createOptions();

    // Parse command line
    CommandLineParser parser = new GnuParser();
    CommandLine commandLine = null;
    try
    {
      commandLine = parser.parse(options, args);
    }
    catch (ParseException e)
    {
      logger.fatal(Translate.get("controller.configure.commandline.error", e),
          e);
      printUsage(options);
      Runtime.getRuntime().exit(1);
    }

    // Non-recognized options
    int n = commandLine.getArgs().length;
    for (int i = 0; i < n; i++)
    {
      logger.fatal(Translate.get("controller.configure.unknown.option",
          commandLine.getArgs()[i]));
      printUsage(options);
      Runtime.getRuntime().exit(1);
    }
    // Handle --help option
    if (commandLine.hasOption('h'))
    {
      if (commandLine.getOptions().length > 1)
        logger.fatal(Translate.get("controller.configure.commandline.error"));

      printUsage(options);
      Runtime.getRuntime().exit(1);
    }

    // Handle --version option
    if (commandLine.hasOption('v'))
    {
      if (commandLine.getOptions().length > 1)
      {
        logger.fatal(Translate.get("controller.configure.commandline.error"));
        printUsage(options);
      }
      else
        logger.info(Controller.getVersion());
      Runtime.getRuntime().exit(1);
    }

    // Handle -rmi option
    if (commandLine.hasOption('r'))
    {
      String s = commandLine.getOptionValue('r');
      if (s != null)
      {
        this.put(JMX_ENABLE, "true");
        this.put(RMI_PORT, s);
        this.put(JmxConstants.ADAPTOR_TYPE_RMI, s);
      }
    }

    // Handle -jmx option
    if (commandLine.hasOption('j'))
    {
      String s = commandLine.getOptionValue('j');
      if (s != null)
      {
        this.put(JMX_ENABLE, "true");
        this.put(JMX_PORT, s);
        this.put(JmxConstants.ADAPTOR_TYPE_HTTP, s);
      }
    }

    // Handle --ip option
    if (commandLine.hasOption('i'))
    {
      String ipAddress = commandLine.getOptionValue('i');
      if (ipAddress != null)
        this.put(CONTROLLER_IP, ipAddress);
    }

    // Handle --port option
    if (commandLine.hasOption('p'))
    {
      String port = commandLine.getOptionValue('p');
      if (port != null)
        this.put(CONTROLLER_PORT, port);
    }

    // Handle -f option
    if (commandLine.hasOption('f'))
    {
      // If a config file is specified we ignore the default file.
      this.remove(XML_FILE);
      String filePath = commandLine.getOptionValue('f');
      File f = new File(filePath);
      logger.debug(f.getAbsolutePath());
      if (f.exists() == false || f.isFile() == false)
        logger
            .warn(Translate.get("controller.configure.optional.file.invalid"));
      else
        this.put(XML_FILE, filePath);
    }
  }

  /**
   * This method is going to call a <code>ControllerParser</code> object to
   * configure controller while parsing file. This method will call <method>
   * setUpRmi() </method> and <method>setUpJmx() </method> as well as <method>
   * setUpVirtualDatabases </method> while parsing.
   * 
   * @param filename path to the xml file to parse from
   * @throws Exception if configuration fails
   */
  public void setUpByXml(String filename) throws Exception
  {
    logger.info(Translate.get("controller.configure.loading.file", filename));
    FileReader fileReader = null;
    try
    {
      fileReader = new FileReader(filename);
      ControllerParser cparser = new ControllerParser(this);
      cparser.readXML(fileReader, true);
      fileReader.close();
    }
    catch (Exception e)
    {

      logger.warn(Translate.get("controller.configure.xml.file.error", e), e);
      throw e;
    }
    finally
    {
      if (fileReader != null)
        fileReader.close();
    }
  }

  /**
   * Test if there is a file to take configuration from, if so call <method>
   * setUpByXml() </method>
   * 
   * @return an instanciated and configured object of class
   *         <code>Controller</code>
   * @throws Exception if configuration fails
   */
  private Controller setup() throws Exception
  {
    String xml = (String) this.get(XML_FILE);

    int portNumber = Integer.parseInt((String) this.get(CONTROLLER_PORT));
    int backlog = Integer.parseInt((String) this.get(CONTROLLER_BACKLOG));
    String ipAddress = (String) this.get(CONTROLLER_IP);

    controller = new Controller(ipAddress, portNumber, backlog);
    controller.setConfiguration(this);

    if (xml != null)
    {
      try
      {
        setUpByXml(xml);
      }
      catch (Exception e)
      {
        logger.error(Translate.get(
            "controller.configure.load.file.failed.minimum.configuration",
            new String[]{xml, e.getMessage()}), e);
      }
    }
    else
      setUpJmx();

    return this.controller;
  }

  /**
   * Retrieve the controller associated with this <code>ControllerFactory</code>
   * instance.
   * 
   * @return <code>Controller</code> object. Can be null if this method is
   *         called before setup
   * @throws Exception if an error occurs
   */
  public Controller getController() throws Exception
  {
    if (controller == null)
      setup();
    return this.controller;
  }

  /**
   * Start up the jmx services if enabled.
   * 
   * @throws JmxException an exception
   */
  public void setUpJmx() throws JmxException
  {
    boolean jmxEnable = new Boolean((String) get(JMX_ENABLE)).booleanValue();
    if (jmxEnable == false)
    {
      MBeanServerManager.setJmxEnabled(false);
      logger.info(Translate.get("jmx.configure.disabled"));
    }
    else
    {
      MBeanServerManager.setJmxEnabled(true);
      logger.info(Translate.get("jmx.configure.enabled"));
      // Create and start the JMX agent
      try
      {
        new DataCollector(controller);
        String hostIP = controller.getIPAddress();

        logger.info(Translate.get("controller.configure.start.jmx", hostIP));

        if (this.containsKey(JmxConstants.ADAPTOR_TYPE_HTTP))
        {
          int port = Integer.parseInt((String) this
              .get(JmxConstants.ADAPTOR_TYPE_HTTP));
          HttpAdaptor http = new HttpAdaptor(hostIP, port, null);
          http.start();
        }
        if (this.containsKey(JmxConstants.ADAPTOR_TYPE_RMI))
        {
          SSLConfiguration ssl = null;
          PasswordAuthenticator authenticator = null;
          int port = Integer.parseInt((String) this
              .get(JmxConstants.ADAPTOR_TYPE_RMI));
          if (this.containsKey(JmxConstants.CONNECTOR_AUTH_USERNAME))
          {
            String username = (String) this
                .get(JmxConstants.CONNECTOR_AUTH_USERNAME);
            String password = (String) this
                .get(JmxConstants.CONNECTOR_AUTH_PASSWORD);
            authenticator = new PasswordAuthenticator(username, password);
          }
          if (this.containsKey(JmxConstants.CONNECTOR_RMI_SSL))
          {
            ssl = (SSLConfiguration) this.get(JmxConstants.CONNECTOR_RMI_SSL);
          }
          RmiConnector rmi = new RmiConnector(controller.getControllerName(),
              hostIP, port, authenticator, ssl);
          rmi.start();
        }
        logger.debug(Translate.get("controller.configure.jmx.started"));
      }
      catch (Exception e)
      {
        logger
            .error(Translate.get("controller.configure.jmx.fail.start", e), e);
      }
    }
    controller.setJmxEnable(jmxEnable);
  }

  /**
   * Set up security settings if needed here.
   * 
   * @param security to enforce
   */
  public void setUpSecurity(ControllerSecurityManager security)
  {
    controller.setSecurity(security);
  }

  /**
   * Will load the <code>VirtualDatabase</code> configuration into the
   * controller.
   * 
   * @param filePath the path to xml definition of the virtual database
   * @param virtualName the name of the virtualDatabase to load
   * @param autoLoad specified if backend should be enabled.
   * @param checkPoint the check point to load the database from.
   */
  public void setUpVirtualDatabase(String filePath, String virtualName,
      int autoLoad, String checkPoint)
  {
    try
    {
      controller.loadXmlConfiguration(filePath, virtualName, autoLoad,
          checkPoint);
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("controller.configure.file.autoload",
            new String[]{filePath, "" + autoLoad}));

    }
    catch (Exception e)
    {
      logger.error(Translate.get("controller.configure.load.file.failed",
          new String[]{filePath, e.getMessage()}), e);
    }
  }

  /**
   * Displays usage message.
   * 
   * @param options available command line options
   */
  private static void printUsage(Options options)
  {
    String header = Translate.get("controller.commandline.header");
    header += System.getProperty("line.separator");
    header += Translate.get("controller.commandline.options");
    String footer = Translate.get("controller.commandline.footer");

    (new HelpFormatter()).printHelp(80, "controller(.sh|.bat) [options]",
        header, options, footer);
  }

  /**
   * Creates <code>Options</code> object that contains all available options
   * that can be used launching C-JDBC controller.
   * 
   * @return an <code>Options</code> instance
   */
  private static Options createOptions()
  {
    Options options = new Options();
    OptionGroup group = new OptionGroup();

    // help and verbose options
    group.addOption(new Option("h", "help", false, Translate
        .get("controller.commandline.option.help")));
    group.addOption(new Option("v", "version", false, Translate
        .get("controller.commandline.option.version")));
    options.addOptionGroup(group);

    // RMI port option
    options.addOption(new Option("r", "rmi", true, Translate.get(
        "controller.commandline.option.rmi", ""
            + JmxConstants.DEFAULT_JMX_RMI_PORT)));
    // JMX port option
    options.addOption(new Option("j", "jmx", true, Translate.get(
        "controller.commandline.option.jmx", ""
            + JmxConstants.DEFAULT_JMX_HTTP_PORT)));

    // IP option
    String defaultIp = "127.0.0.1";
    try
    {
      defaultIp = InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException e)
    {

    }
    options.addOption(new Option("i", "ip", true, Translate.get(
        "controller.commandline.option.ip", "" + defaultIp)));

    // Port options
    options.addOption(new Option("p", "port", true, Translate.get(
        "controller.commandline.option.port", ""
            + ControllerConstants.DEFAULT_PORT)));

    // configuration file option
    options.addOption(new Option("f", "file", true, Translate
        .get("controller.commandline.option.file")));

    return options;
  }

}