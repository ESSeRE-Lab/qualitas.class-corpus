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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.scenario.templates;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.driver.Driver;
import org.objectweb.cjdbc.scenario.tools.ScenarioConstants;

/**
 * This class defines a Template for CJDBC.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:mathieu.peltier@emicnetworks.com">Mathieu Peltier
 *         </a>
 * @version 1.0
 */
public abstract class Template extends NoTemplate
{
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected abstract void setUp();

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected abstract void tearDown();

  /**
   * Returns a connection to hypersonic backend.
   * 
   * @param port the port on which the backend is running.
   * @return <code>Connection</code> to hsqldb.
   * @throws Exception if fails.
   */
  public static Connection getHypersonicConnection(int port) throws Exception
  {
    Properties props = new Properties();
    props.put("user", "test");
    props.put("password", "");
    Connection con = DriverManager.getConnection(
        "jdbc:hsqldb:hsql://localhost:" + port, props);
    return con;
  }

  /**
   * Returns a connection to C-JDBC controller using default values (controller
   * hostname, virtaul database name and user).
   * 
   * @return a connection to the cjdbc myDB database
   * @throws Exception if fails
   */
  public static Connection getCJDBCConnection() throws Exception
  {
    return getCJDBCConnection(ScenarioConstants.DEFAULT_CONTROLLER_PORT,
        ScenarioConstants.DEFAULT_VDB_NAME,
        ScenarioConstants.DEFAULT_VDB_USER_NAME,
        ScenarioConstants.DEFAULT_VDB_USER_PASSWORD);
  }

  /**
   * Returns a connection to C-JDBC controller on the given port.
   * 
   * @param port to connect on to
   * @return a connection to the cjdbc myDB database
   * @throws Exception if fails
   */
  public static Connection getCJDBCConnection(String port) throws Exception
  {
    return getCJDBCConnection(port, ScenarioConstants.DEFAULT_VDB_NAME,
        ScenarioConstants.DEFAULT_VDB_USER_NAME,
        ScenarioConstants.DEFAULT_VDB_USER_PASSWORD);
  }

  /**
   * Returns a connection to C-JDBC controller on the given port and the given
   * database and the given user authentication.
   * 
   * @param port to connect to
   * @param database to connect to
   * @param user login name
   * @param password password
   * @return a connection to the cjdbc given database
   * @throws Exception if fails
   */
  public static Connection getCJDBCConnection(String port, String database,
      String user, String password) throws Exception
  {
    Properties props = new Properties();
    props.put("user", user);
    props.put("password", password);
    ControllerInfo[] controllerList = new ControllerInfo[]{new ControllerInfo(
        ScenarioConstants.DEFAULT_CONTROLLER_HOSTNAME, Integer.parseInt(port))};
    return getCJDBCConnection(controllerList, database, props);
  }

  /**
   * Returns a connection to cjdbc controller on the given port and the given
   * database.
   * 
   * @param port to connect on to
   * @param database to connect to
   * @return a connection to the cjdbc given database
   * @throws Exception if fails
   */
  public static Connection getCJDBCConnection(String port, String database)
      throws Exception
  {
    return getCJDBCConnection(port, database,
        ScenarioConstants.DEFAULT_VDB_USER_NAME,
        ScenarioConstants.DEFAULT_VDB_USER_PASSWORD);
  }

  /**
   * Get connection with list of controllers
   * 
   * @param controllersList list of ControllerInfo objects
   * @param database to connect to
   * @param props containing user and password
   * @return <code>Connection</code> to cjdbc controller
   * @throws Exception if fails
   */
  public static Connection getCJDBCConnection(ControllerInfo[] controllersList,
      String database, Properties props) throws Exception
  {
    if (controllersList == null || controllersList.length < 1)
      return getCJDBCConnection(new ControllerInfo[]{new ControllerInfo(
          ScenarioConstants.DEFAULT_CONTROLLER_HOSTNAME, Integer
              .parseInt(ScenarioConstants.DEFAULT_CONTROLLER_PORT))}, database,
          props);
    else
    {
      Class.forName("org.objectweb.cjdbc.driver.Driver");
      String controllers = controllersList[0].toString();
      for (int i = 1; i < controllersList.length; i++)
        controllers += "," + controllersList[i].toString();
      Connection con = DriverManager.getConnection(Driver.CJDBC_URL_HEADER
          + controllers + "/" + database, props);
      assertNotNull("Connection to cjdbc controller was null", con);
      assertFalse("Connection should not be closed", con.isClosed());
      return con;
    }
  }

  /**
   * @see #getCJDBCConnection(ControllerInfo[],String,Properties)
   */
  public static Connection getCJDBCConnection(ControllerInfo[] controllersList,
      String database) throws Exception
  {
    Properties props = new Properties();
    props.put("user", ScenarioConstants.DEFAULT_VDB_USER_NAME);
    props.put("password", ScenarioConstants.DEFAULT_VDB_USER_PASSWORD);
    return getCJDBCConnection(controllersList, database, props);
  }

  /**
   * @see #getCJDBCConnection(ControllerInfo[],String,Properties)
   */
  public static Connection getCJDBCConnection(ControllerInfo[] controllersList)
      throws Exception
  {
    return getCJDBCConnection(controllersList,
        ScenarioConstants.DEFAULT_VDB_NAME);
  }
}
