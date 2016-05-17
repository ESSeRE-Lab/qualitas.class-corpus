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
 * Initial developer(s): Marc Wick.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.connection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.core.ControllerConstants;

/**
 * This class defines a DriverManager. In contrast to java.sql.DriverManager
 * this class allows to use Drivers with the same name but with different
 * versions, if no drivername is used it is a wrapper around
 * java.sql.DriverManager.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class DriverManager
{

  /** Logger instance. */
  static Trace       logger         = Trace
                                        .getLogger("org.objectweb.cjdbc.controller.connection.DriverManager");

  /**
   * Driver class names read from default drivers, without driverPath
   */
  private static Set defaultDrivers = new HashSet();

  /**
   * We keep a reference to already loaded named drivers. Each named driver has
   * been loaded with a separate classloader.
   */
  private static Map namedDrivers   = new HashMap();

  /**
   * Attempts to establish a connection to the given database URL. The
   * <code>DriverManager</code> attempts to select an appropriate driver from
   * the set of registered JDBC drivers.
   * 
   * @param url a database url of the form
   *          <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
   * @param user the database user on whose behalf the connection is being made
   * @param password the user's password
   * @param driverPathName the path where the driver classes are located, null
   *          if default directory
   * @param driverClassName the class name of the driver
   * @return a connection to the URL
   * @exception SQLException if a database access error occurs
   */
  public static Connection getConnection(String url, String user,
      String password, String driverPathName, String driverClassName)
      throws SQLException
  {
    Driver driver = null;
    boolean isDefaultPath = false;

    if (driverPathName == null)
    {
      // no path specified
      // have we already loaded this driver
      driver = (Driver) namedDrivers.get(driverClassName);
      if (driver == null)
      {
        // the driver has not yet been loaded
        // first we try to load class from classpath
        try
        {
          if (driverClassName != null)
          {
            loadDriverClass(driverClassName);
          }
          return java.sql.DriverManager.getConnection(url, user, password);
        }
        catch (ClassNotFoundException e)
        {
          if (driverClassName == null)
          {
            throw new SQLException(
                "could not load driver as no class name is specified ");
          }
          try
          {
            driverPathName = getDriversDir().getAbsolutePath();
            isDefaultPath = true;
          }
          catch (IOException ioExc)
          {
            throw new SQLException("could not find default drivers directory");
          }
        }
      }
    }

    if (driver == null)
    {
      // have we already loaded this named driver ?
      driver = (Driver) namedDrivers.get(driverPathName);
    }

    if (driver == null)
    {
      // no driver with this name has been loaded so far
      try
      {
        File path = convertToAbsolutePath(driverPathName);
        // we load the driver now
        if (logger.isDebugEnabled())
        {
          logger.debug("loading driver with name " + driverPathName
              + " for class " + driverClassName);
        }
        driver = loadDriver(path, driverClassName);
      }
      catch (Exception e)
      {
        logger.error("Could not load driver for class " + driverClassName, e);
        throw new SQLException("could not load driver for class name "
            + driverClassName + " and driverPath " + driverPathName);
      }

      // driver has been loaded successfully, we cache it for
      // further use
      if (isDefaultPath)
      {// we cache it with the class name
        namedDrivers.put(driverClassName, driver);
      }
      else
      {
        // we cache it with the pathName
        namedDrivers.put(driverPathName, driver);
      }
    }

    return getConnectionForDriver(url, user, password, driver);
  }

  /**
   * Load the driver class
   * 
   * @param driverClassName the class name of the driver
   * @throws ClassNotFoundException if the class could not be found
   */
  public static void loadDriverClass(String driverClassName)
      throws ClassNotFoundException
  {
    if (!defaultDrivers.contains(driverClassName))
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("we are using default classloader and driverClassName ="
            + driverClassName);
      }
      Class.forName(driverClassName);
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("backend.driver.loaded", driverClassName));
      // the driver was successfully loaded
      defaultDrivers.add(driverClassName);
    }
  }

  /**
   * convert a path into an absolute path if the path is already an absolute
   * path, it is just returned otherwise a relative path is considered to be
   * relative to the drivers directory
   * 
   * @param pathName the relativ or absolute path
   * @return the converted path
   * @throws IOException if the converted path does not exist
   */
  public static File convertToAbsolutePath(String pathName) throws IOException
  {
    File dir = null;

    if (pathName != null)
    {
      File path = new File(pathName);
      if (path.canRead())
        return path;
      else
        throw new IOException("Invalid path name " + pathName);
    }
    else
    {
      dir = getDriversDir();
    }

    if (!dir.canRead())
    {
      String msg = Translate.get("controller.driver.dir.not.found");
      logger.error(msg);
      throw new IOException(msg);
    }

    return dir;
  }

  private static File getDriversDir() throws IOException
  {
    URL url = Controller.class
        .getResource(ControllerConstants.C_JDBC_DRIVER_JAR_FILE);
    if (url == null)
    {
      String msg = Translate.get("controller.driver.dir.not.found");
      logger.error(msg);
      throw new IOException(msg);
    }

    File driversDir = new File(url.getFile()).getParentFile();

    if (!driversDir.exists())
    {
      String msg = Translate.get("controller.driver.dir.not.found");
      logger.error(msg);
      throw new IOException(msg);
    }
    return driversDir;
  }

  private static Connection getConnectionForDriver(String url, String user,
      String password, Driver driver) throws SQLException
  {
    java.util.Properties info = new java.util.Properties();
    if (user != null)
    {
      info.put("user", user);
    }
    if (password != null)
    {
      info.put("password", password);
    }

    return driver.connect(url, info);
  }

  private static Driver loadDriver(File path, String driverClassName)
      throws ClassNotFoundException, InstantiationException,
      IllegalAccessException
  {
    ClassLoader loader = new DriverClassLoader(null, path);

    // load java.sql.DriverManager with the new classloader
    // Driver instances register with the DriverManager and we want the new
    // Driver to register with its own DriverManager
    // Otherwise the new Driver would register with the default DriverManager
    // and possibly overwrite an other driver with the same name
    Class.forName(java.sql.DriverManager.class.getName(), true, loader);

    // load class
    Class driverClass = Class.forName(driverClassName, true, loader);

    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.loaded", driverClassName));

    // get an instance of the class and return it
    return (Driver) driverClass.newInstance();

  }

}
