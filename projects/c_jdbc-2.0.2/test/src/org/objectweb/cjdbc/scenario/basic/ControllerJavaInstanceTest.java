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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.scenario.basic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.objectweb.cjdbc.common.exceptions.ControllerException;
import org.objectweb.cjdbc.common.sql.filters.HexaBlobFilter;
import org.objectweb.cjdbc.common.users.DatabaseBackendUser;
import org.objectweb.cjdbc.common.users.VirtualDatabaseUser;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.controller.authentication.AuthenticationManager;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.cache.result.ResultCacheColumnUnique;
import org.objectweb.cjdbc.controller.cache.result.ResultCacheRule;
import org.objectweb.cjdbc.controller.cache.result.rules.EagerCaching;
import org.objectweb.cjdbc.controller.connection.VariablePoolConnectionManager;
import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.core.ControllerConstants;
import org.objectweb.cjdbc.controller.loadbalancer.policies.WaitForCompletionPolicy;
import org.objectweb.cjdbc.controller.loadbalancer.raidb1.RAIDb1_RR;
import org.objectweb.cjdbc.controller.loadbalancer.singledb.SingleDB;
import org.objectweb.cjdbc.controller.requestmanager.RequestManager;
import org.objectweb.cjdbc.controller.requestmanager.distributed.RAIDb1DistributedRequestManager;
import org.objectweb.cjdbc.controller.scheduler.raidb1.RAIDb1PessimisticTransactionLevelScheduler;
import org.objectweb.cjdbc.controller.scheduler.singledb.SingleDBPassThroughScheduler;
import org.objectweb.cjdbc.controller.virtualdatabase.DistributedVirtualDatabase;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.scenario.templates.DatabaseTemplate;

/**
 * This class defines a ControllerJavaInstanceTest. Creates a Controller from
 * java code (as opposed to load it with an xml configuration file) and check
 * that it is able to handle requests.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class ControllerJavaInstanceTest extends DatabaseTemplate
{
  String     cjdbcDriver     = "c-jdbc-driver.jar";
  String     driverJar       = "hsqldb.jar";
  String     driverClass     = "org.hsqldb.jdbcDriver";
  String     driverPath      = null;
  String     backendTest     = "call now()";
  String     backendUrl      = "jdbc:hsqldb:hsql://localhost:9001";
  String     backendUser     = "TEST";
  String     backendPassword = "";

  Controller controller;

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  public void tearDown()
  {
    if (controller != null)
    {
      try
      {
        controller.shutdown(Constants.SHUTDOWN_SAFE);
      }
      catch (ControllerException e)
      {
        e.printStackTrace();
      }
    }
    super.tearDown();
  }

  /**
   * Test the test statement on a hypersonic backend
   * 
   * @throws Exception if fails
   */
  public void testBackendTestStatement() throws Exception
  {
    Class.forName(driverClass);
    Properties props = new Properties();
    props.put("user", backendUser);
    props.put("password", backendPassword);
    Connection con = DriverManager.getConnection(backendUrl, props);
    assertTrue("Statement not executed", con.createStatement().execute(
        backendTest));
    con.close();
  }

  /**
   * Test the creation of a virtual database and send some requests.
   * 
   * @throws Exception if fails
   */
  public void testCreateVirtualDatabase() throws Exception
  {
    // Prepare the controller instance
    controller = new Controller("127.0.0.1", 25322, 10);

    // Add the driver
    // TODO: addDriver does not work properly, as some mysterious files need to
    // be on the class path
    //cont.addDriver(jar.getAbsolutePath());

    // Prepare the distributed database instance
    VirtualDatabase vdb = new VirtualDatabase(controller, "blob", 120, true,
        20, 120, 15000L, 30, new HexaBlobFilter());

    // Set the authentication manager for the virtual database
    AuthenticationManager am = new AuthenticationManager();
    am.addVirtualUser(new VirtualDatabaseUser("blob", ""));
    vdb.setAuthenticationManager(am);

    // Prepare the backend instance
    DatabaseBackend dbb = new DatabaseBackend("blob", driverPath, driverClass,
        backendUrl, vdb.getVirtualDatabaseName(), true, backendTest);
    am.addRealUser("blob", new DatabaseBackendUser(dbb.getName(), backendUser,
        backendPassword));

    // Prepapre the connection manager for the backend
    VariablePoolConnectionManager vcpm = new VariablePoolConnectionManager(dbb
        .getURL(), dbb.getName(), backendUser, backendPassword, driverPath,
        driverClass, 20, 120, 15, 45);
    dbb.addConnectionManager("blob", vcpm);

    // Prepare the cache
    ResultCacheColumnUnique rccu = new ResultCacheColumnUnique(0, 0);
    ResultCacheRule rcr = new ResultCacheRule("default", false, false, 36000);
    rcr.setCacheBehavior(new EagerCaching(0));
    rccu.setDefaultRule(rcr);

    // Set the request manager for the virtual database
    RequestManager sddrm = new RequestManager(vdb,
        new SingleDBPassThroughScheduler(), rccu, new SingleDB(vdb), null, 45,
        45, 45);
    vdb.setRequestManager(sddrm);

    // add the backend
    vdb.addBackend(dbb);

    // Add the virtual database to the controller
    // This cannot be done before, as the backends are going to be enabled
    // and we need the request manager active
    controller.addVirtualDatabase(vdb, ControllerConstants.AUTO_ENABLE_TRUE,
        null);

    // Start the controller thread
    controller.launch();

    // Test Connection
    Connection con = getCJDBCConnection("25322", "blob", "blob", "");
    Statement s = con.createStatement();
    ResultSet rset = s.executeQuery("select * from document");
    assertTrue("Failed to move result set to last", rset.last());
    assertTrue(rset.getRow() == 50);

    rset = s.executeQuery("select * from document");
    assertTrue("Failed to move result set to last after hit on cache", rset
        .last());
    assertTrue(rset.getRow() == 50);
  }

  /**
   * Test the creation of a DistributedVirtualDatabase and send some requests
   * 
   * @throws Exception if fails
   */
  public void testCreateDistributedVirtualDatabase() throws Exception
  {

    // File jar = new File(getClass().getResource("/" + driverJar).getFile());

    // Prepare the controller instance
    controller = new Controller("127.0.0.1", 25322, 10);

    // Add the driver
    // TODO: addDriver does not work properly, as some mysterious files need to
    // be on the class path
    // cont.addDriver(jar.getAbsolutePath());

    // Prepare the distributed database instance
    DistributedVirtualDatabase vdb = new DistributedVirtualDatabase(controller,
        "blob", "blob", 120, true, 20, 120, 15000L, 30, new HexaBlobFilter());

    // Set the authentication manager for the virtual database
    AuthenticationManager am = new AuthenticationManager();
    am.addVirtualUser(new VirtualDatabaseUser("blob", ""));
    vdb.setAuthenticationManager(am);

    // Prepare the backend instance
    DatabaseBackend dbb = new DatabaseBackend("blob", driverPath, driverClass,
        backendUrl, vdb.getVirtualDatabaseName(), true, backendTest);
    am.addRealUser("blob", new DatabaseBackendUser(dbb.getName(), backendUser,
        backendPassword));

    // Prepapre the connection manager for the backend
    VariablePoolConnectionManager vcpm = new VariablePoolConnectionManager(dbb
        .getURL(), dbb.getName(), backendUser, backendPassword, driverPath,
        driverClass, 20, 120, 15, 45);
    dbb.addConnectionManager("blob", vcpm);

    // Prepare the cache
    ResultCacheColumnUnique rccu = new ResultCacheColumnUnique(0, 0);
    ResultCacheRule rcr = new ResultCacheRule("default", false, false, 36000);
    rcr.setCacheBehavior(new EagerCaching(0));
    rccu.setDefaultRule(rcr);

    // Set the request manager for the virtual database
    RAIDb1DistributedRequestManager sddrm = new RAIDb1DistributedRequestManager(
        vdb, new RAIDb1PessimisticTransactionLevelScheduler(), rccu,
        new RAIDb1_RR(vdb, new WaitForCompletionPolicy()), null, 45, 45, 45);
    vdb.setRequestManager(sddrm);

    // We join the group once the backend has been added
    vdb.addBackend(dbb);
    vdb.joinGroup();

    // Add the virtual database to the controller
    // This cannot be done before, as the backends are going to be enabled
    // and we need the request manager active, and we need to join the group
    controller.addVirtualDatabase(vdb, ControllerConstants.AUTO_ENABLE_TRUE,
        null);

    // Start the controller thread
    controller.launch();

    // Test Connection
    Connection con = getCJDBCConnection("25322", "blob", "blob", "");
    Statement s = con.createStatement();
    ResultSet rset = s.executeQuery("select * from document");
    assertTrue("Failed to move result set to last", rset.last());
    assertTrue(rset.getRow() == 50);

    rset = s.executeQuery("select * from document");
    assertTrue("Failed to move result set to last after hit on cache", rset
        .last());
    assertTrue(rset.getRow() == 50);
  }
}
