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
 * Contributor(s): Julie Marguerite, Mathieu Peltier.
 */

package org.objectweb.cjdbc.requestplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Checks and gets all properties needed by the request player tools.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inria.fr">Julie Marguerite</a>
 * @author <a href="mailto:mathieu.peltier@inrialpes.fr">Mathieu Peltier</a>
 * @version 1.0
 */
public class RequestPlayerProperties extends Properties
{
  /** Default Request player configuration file. */
  public static final String DEFAULT_CONFIG_FILE = "requestplayer.properties";

  /** Standard connection management type. */
  public static final int STANDARD_CONNECTION = 0;

  /** Optimized connection management type. */
  public static final int FIXED_CONNECTION = 1;

  /** Pooling connection management type. */
  public static final int POOLING_CONNECTION = 2;

  /** Configuration file. Default value is {@link #DEFAULT_CONFIG_FILE}. */
  private File configFile;

  /** Trace file. */
  private String traceFile;

  /**
   * How many requests from the trace file should be executed (0 means the
   * whole trace is executed).
   */
  private int nbRequests = 0;

  /** Number of clients to run in parallel to issue the requests. */
  private int nbClients = 0;

  /** Request timeout in seconds (0 means no timeout). */
  private int timeout = 0;

  /** Database driver. */
  private String databaseDriver;

  /** Database URL. */
  private String databaseUrl;

  /** Database login. */
  private String databaseLogin;

  /** Database password. */
  private String databasePassword;

  /**
   * Connection management type used in client emulator: standard, fixed or
   * pooling.
   */
  private int connectionType;

  /**
   * Connection pool size. Must be greater than 0 if connection type is
   * pooling.
   */
  private int poolSize = 0;

  /**
   * Creates a new <code>RequestPlayerProperties</code> instance. If the
   * given configuration file cannot be read or if the
   * {@link #DEFAULT_CONFIG_FILE}file is not found in the classpath, the
   * current thread is killed.
   * 
   * @param configFileString configuration file or <code>null</code> if the
   *          default file must be used.
   */
  public RequestPlayerProperties(String configFileString)
  {
    if (configFileString == null)
    {
      configFile = new File(DEFAULT_CONFIG_FILE);
    }
    else
    {
      configFile = new File(configFileString);

      // Check that the configuration file exists
      if (!configFile.isFile() || !configFile.exists())
      {
        System.err.println(
          "Cannot read the request player configuration file '"
            + configFile.toString()
            + "'");
        Runtime.getRuntime().exit(1);
      }
    }

    // Load properties from configuration file
    try
    {
      InputStream stream = null;
      if (configFileString == null)
      {

        stream =
          RequestPlayerProperties.class.getResourceAsStream(
            "/" + DEFAULT_CONFIG_FILE);
        if (stream == null)
        {
          System.err.println(
            "Cannot find request player properties file '"
              + DEFAULT_CONFIG_FILE
              + "' in classpath");
        }
      }
      else
      {
        stream = new FileInputStream(configFile);
      }

      super.load(stream);
      stream.close();
    }
    catch (IOException e)
    {
      System.err.println(
        "An error occured when reading the request player properties file '"
          + configFile.toString()
          + "'");
      Runtime.getRuntime().exit(1);
    }
  }

  /**
   * Checks for all needed fields in <code>requestplayer.properties</code>
   * and initialize corresponding values.
   * 
   * @return <code>true</code> if so
   */
  public boolean checkPropertiesFile()
  {
    try
    {
      System.out.println();
      System.out.println("### Database information ###");
      databaseUrl = getProperty("db_url");
      System.out.println("Database url       : " + databaseUrl);
      databaseDriver = getProperty("db_driver");
      System.out.println("Database driver    : " + databaseDriver);
      databaseLogin = getProperty("db_username");
      System.out.println("Username           : " + databaseLogin);
      databasePassword = getProperty("db_password");
      System.out.println("Password           : " + databasePassword);

      System.out.println();
      System.out.println("### General information ###");
      traceFile = getProperty("trace_file");
      System.out.println("Trace file          : " + traceFile);
      nbRequests = new Integer(getProperty("nb_requests")).intValue();
      System.out.println("Number of requests  : " + nbRequests);
      timeout = new Integer(getProperty("timeout")).intValue();
      System.out.println("Timeout on requests : " + timeout + " seconds");
      nbClients = new Integer(getProperty("nb_clients")).intValue();

      System.out.println("Number of clients   : " + nbClients);
      String connType = getProperty("connection_type");
      System.out.println("Connection type     : " + connType);
      if (connType.equalsIgnoreCase("fixed"))
        connectionType = FIXED_CONNECTION;
      else if (connType.equalsIgnoreCase("standard"))
        connectionType = STANDARD_CONNECTION;
      else if (connType.equalsIgnoreCase("pooling"))
      {
        connectionType = POOLING_CONNECTION;
        poolSize = new Integer(getProperty("poolsize")).intValue();
        System.out.println("Connection pool size: " + poolSize);
      }

      return true;
    }
    catch (Exception e)
    {
      System.err.println(
        "Error while checking request player properties file '"
          + configFile.toString()
          + "': "
          + e.getMessage());
      return false;
    }
  }

  /**
   * Gets the trace file.
   * 
   * @return name of the trace file
   */
  public String getTraceFile()
  {
    return traceFile;
  }

  /**
   * Gets the number of requests to execute from the trace file (0 means the
   * whole trace is executed).
   * 
   * @return number of requests
   */
  public int getNbRequests()
  {
    return nbRequests;
  }

  /**
   * Gets the database URL for the SQL database.
   * 
   * @return the database url
   */
  public String getDatabaseURL()
  {
    return databaseUrl;
  }

  /**
   * Gets the database driver class name for the SQL database.
   * 
   * @return database driver class name
   */
  public String getDatabaseDriver()
  {
    return databaseDriver;
  }

  /**
   * Gets the login for the SQL database.
   * 
   * @return username
   */
  public String getDatabaseUsername()
  {
    return databaseLogin;
  }

  /**
   * Gets the password for the SQL database
   * 
   * @return password
   */
  public String getDatabasePassword()
  {
    return databasePassword;
  }

  /**
   * Returns the number of clients to run in parallel to issue the requests.
   * 
   * @return the number of clients
   */
  public int getNbClients()
  {
    return nbClients;
  }

  /**
   * Returns the connection type.
   * 
   * @return the connection type
   */
  public int getConnectionType()
  {
    return connectionType;
  }

  /**
   * Returns the connection pool size.
   * 
   * @return the pool size
   */
  public int getPoolSize()
  {
    return poolSize;
  }

  /**
   * Returns the request timeout in seconds (0 means no timeout).
   * 
   * @return an <code>int</code> value
   */
  public int getTimeout()
  {
    return timeout;
  }
}
