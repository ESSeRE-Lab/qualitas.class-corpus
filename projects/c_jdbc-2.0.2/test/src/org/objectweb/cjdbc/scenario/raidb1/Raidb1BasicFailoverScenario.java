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

package org.objectweb.cjdbc.scenario.raidb1;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;

/**
 * This class defines a Raidb1BasicFailoverScenario. Test the stability of the
 * controller when databases are failing
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Raidb1BasicFailoverScenario extends SimpleRaidb1Template
{
  /**
   * Test CJDBC failover in raidb1 with variable pool
   */
  public void testFailOverWithVariablePool()
  {
    try
    {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb1-variablepool.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      execute();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("failed to test c-jdbc failover in variable pool");
    }
  }

  /**
   * Test CJDBC failover in raidb1 with randomwait pool
   */
  public void testFailOverWithRandomWaitPool()
  {
    try
    {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb1-randomwaitpool.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      execute();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("failed to test c-jdbc failover in random wait pool");
    }
  }

  /**
   * Test CJDBC failover in raidb1 with fail fast pool
   */
  public void testFailOverWithFailFastPool()
  {
    try
    {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb1-failfastpool.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      execute();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("failed to test c-jdbc failover in fail fast wait pool");
    }
  }

  /**
   * Test CJDBC failover in raidb1 with simple connection manager
   */
  public void testFailOverWithNoPool()
  {
    try
    {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB", "hsqldb-raidb1-nopool.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      execute();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("failed to test c-jdbc failover in simple connection manager");
    }
  }

  /**
   * Execute the test for failover once the database is loaded
   */
  private void execute() throws Exception
  {
    //  Execute a request
    Connection con = getCJDBCConnection();
    assertNotNull("Connection is null", con);
    Statement statement = con.createStatement();
    ResultSet rs1 = statement.executeQuery("Select * from document");
    assertNotNull("ResultSet is null", rs1);

    // Drop a backend
    hm.stop(hm1);

    // Wait for backend to receive the dead command
    synchronized (this)
    {
      wait(1000);
    }

    // Execute requests with same connection
    Statement statement2 = con.createStatement();
    ResultSet rs2 = statement2.executeQuery("Select * from document");
    assertNotNull("ResultSet after failover is null", rs2);
    rs1.last();
    rs2.last();
    assertTrue("Row numbers are different", rs1.getRow() == rs2.getRow());
    rs1.first();
    rs2.first();
    while (rs1.next() & rs2.next())
    {
      assertTrue("Some result differs from expect result set", rs1.getString(
          "id").equals(rs2.getString("id")));
    }

    // Drop other backend
    hm.stop(hm2);

    // Wait for backend to receive the dead command
    synchronized (this)
    {
      wait(1000);
    }
    // Execute request
    Statement statement3 = con.createStatement();
    ResultSet rs3 = null;
    try
    {
      rs3 = statement3.executeQuery("Select * from document");
    }
    catch (SQLException expected)
    {
      // expected cause no more backends.
    }
    assertNull("Should not be able to get a result set anymore", rs3);
  }
}
