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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.horizontal;

import java.sql.ResultSet;
import java.sql.Statement;

import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.scenario.templates.HorizontalTemplate;

/**
 * This class defines a TransparentHorizontalBalancingScenario class
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class TransparentHorizontalBalancingScenario extends HorizontalTemplate
{

  /**
   * Test we can still get new connections even if a node failed
   * 
   * @throws Exception if fails
   */
  public void testTransparentBalancing() throws Exception
  {
    ControllerInfo[] controllers = new ControllerInfo[]{
        new ControllerInfo("localhost", 25322),
        new ControllerInfo("localhost", 25323)};
    String readQuery = "Select * from document";
    int connectionsLoad = 50;

    // Test balancing between controllers
    int[] result1 = execute(controllers, connectionsLoad);
    System.out.println(result1[0] + ":" + result1[1]);
    assertTrue("controller1 did not get connection request before failure",
        result1[0] > 0);
    assertTrue("controller2 did not get connection request before failure",
        result1[1] > 0);

    // Get a specific connection
    org.objectweb.cjdbc.driver.Connection con = (org.objectweb.cjdbc.driver.Connection) getCJDBCConnection(controllers);
    Statement statement = con.createStatement();
    ResultSet rs = statement.executeQuery(readQuery);
    assertNotNull("ResultSet before failure is null", rs);

    // Simulate a failure on the controller the connection was
    // connected to
    int wasStopped = -1;
    System.out.println("########Stopping controller");
    if (con.getControllerInfo().equals(controllers[0]))
    {
      cm.stop(controller1.getPortNumber());
      assertFalse("Controller1 should be stopped now", cm.isStarted("25322"));
      wasStopped = 1;
    }
    else
    {
      cm.stop(controller2.getPortNumber());
      assertFalse("Controller2 should be stopped now", cm.isStarted("25323"));
      wasStopped = 2;
    }

    // See if we can still get connections to non-stopped controller
    int[] result2 = execute(controllers, connectionsLoad);
    int fresult = (wasStopped == 1) ? result2[1] : result2[0];
    assertTrue(
        "remaining controller did not get connection request during failure",
        fresult > 0);

    // Execute other queries with the previously opened connection
    rs = statement.executeQuery(readQuery);
    Statement statement2 = con.createStatement();
    ResultSet rs2 = statement2.executeQuery(readQuery);
    assertNotNull("ResultSet after failure is null", rs2);

    System.out.println("wasStopped" + wasStopped);

    // Restart failed controller
    if (wasStopped == 1)
    {
      String port1 = "25322";
      controller1 = (Controller) cm.start(port1).getProcess();
      cm.loadVirtualDatabases(controller1, "myDB",
          "hsqldb-raidb1-distribution-1.xml");
      mainVdb1 = controller1.getVirtualDatabase("myDB");
      mainVdb1.enableAllBackends();
    }
    else if (wasStopped == 2)
    {
      String port2 = "25323";
      controller2 = (Controller) cm.start(port2).getProcess();
      cm.loadVirtualDatabases(controller2, "myDB",
          "hsqldb-raidb1-distribution-2.xml");
      mainVdb2 = controller2.getVirtualDatabase("myDB");
      mainVdb2.enableAllBackends();
    }

    // Execute a last test to see if balancing was re-enabled
    int[] result3 = execute(controllers, connectionsLoad);
    assertTrue("controller1 did not get connection request after failure",
        result3[0] > 0);
    assertTrue("controller2 did not get connection request after failure",
        result3[1] > 0);
  }

  /**
   * Execute an update each time
   * 
   * @param controllers the list of controllers to connect to
   * @param connections how many connections to ask
   * @return connections on each controller
   * @throws Exception if fails
   */
  private int[] execute(ControllerInfo[] controllers, int connections)
      throws Exception
  {
    org.objectweb.cjdbc.driver.Connection con = null;
    int[] count = new int[controllers.length];
    ControllerInfo connected = null;
    for (int i = 0; i < connections; i++)
    {
      con = (org.objectweb.cjdbc.driver.Connection) getCJDBCConnection(controllers);
      assertNotNull("Received null connection", con);
      Statement statement = con.createStatement();
      statement.executeUpdate("update product set name='horizontalTest'");
      connected = con.getControllerInfo();
      System.out.println("Client connected to:" + connected);
      assertNotNull("Received null for connected controller", connected);
      for (int j = 0; j < controllers.length; j++)
      {
        if (connected.equals(controllers[j]))
        {
          count[j]++;
          break;
        }
      }
    }
    return count;
  }
}