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
 * Initial developer(s): Mathieu Peltier. 
 * Contributor(s): Nicolas Modrzyk, Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.controller.core;

import java.io.File;
import java.net.URL;

/**
 * Constants relative to C-JDBC controller.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:duncan@mightybot.com">Duncan Smith </a>
 * @version 1.0
 */
public class ControllerConstants
{
  /** Default controller port number value. */
  public static final int     DEFAULT_PORT                                = 25322;

  /**
   * Default IP address to bind controller to. InetAddress.anyLocalAddress()
   * would be more IPv12 compliant
   */
  public static final String  DEFAULT_IP                                  = "0.0.0.0";

  /** Default backlog size for driver connections. */
  public static final int     DEFAULT_BACKLOG_SIZE                        = 10;

  /** This allow to send a command from the ControllerShutdown class */
  public static final String  SHUTDOWN                                    = "CJDBC_SHUTDOWN";

  /** Backup directory */
  public static final String  DEFAULT_BACKUP_DIR                          = ".."
                                                                              + File.separator
                                                                              + "backup";

  /** C-JDBC-Controller DTD file name (must be found in classpath). */
  public static final String  C_JDBC_CONTROLLER_DTD_FILE                  = "c-jdbc-controller.dtd";

  /**
   * Default sleep time in ms for a controller worker thread. If no job is ready
   * after this time, the thread dies.
   */
  public static final int     DEFAULT_CONTROLLER_WORKER_THREAD_SLEEP_TIME = 15000;

  /** JMX Enable by default */
  public static final boolean JMX_ENABLE                                  = false;

  /** Add Driver enable by default */
  public static final boolean ADD_DRIVER_ENABLE                           = false;

  /** Checkpoint to take by default */
  public static final String  DATABASE_DEFAULT_CHECKPOINT                 = "last";

  /** Default configuration file */
  public static final String  DEFAULT_CONFIG_FILE                         = "controller.xml";

  /** Log4j property file resource (must be found in classpath). */
  public static final String  LOG4J_RESOURCE                              = "/log4j.properties";

  /** Default log directory name (will be stored into CJDBC_HOME directory). */
  public static final String  DEFAULT_LOG_DIR_NAME                        = "log";

  /** Default report location */
  public static final String  REPORT_LOCATION                             = ".."
                                                                              + File.separator
                                                                              + DEFAULT_LOG_DIR_NAME;

  /** Report file */
  public static final String  REPORT_FILE                                 = "cjdbc.report";

  /**
   * Name of the C-JDBC JAR file (must be found in classpath). This information
   * is used to find the drivers directory.
   */
  public static final String  C_JDBC_DRIVER_JAR_FILE                      = "/c-jdbc-driver.jar";

  /**
   * Return default path and name for saving of configuration file
   * 
   * @param resource name of the resource to get save file for
   * @return path
   */
  public static final String getSaveFile(String resource)
  {
    URL url = ControllerConstants.class.getResource("/" + DEFAULT_CONFIG_FILE);
    File dir = (new File(url.getFile())).getParentFile();
    return dir.getPath() + File.separator + resource + "-saved.xml";
  }

  /** Enable all backend from their last known state at controller start */
  public static final int AUTO_ENABLE_TRUE    = 0;
  /** Do not enable any backend when starting controller */
  public static final int AUTO_ENABLE_FALSE   = 1;
  /** Restore the state from an existing recovery log */
  public static final int AUTO_ENABLE_FORCE   = 2;

  /** Auto Enable Backend default */
  public static final int AUTO_ENABLE_BACKEND = AUTO_ENABLE_FALSE;

}