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
 * Initial developer(s): Frederic Laugier
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.raidb1;

import java.sql.Connection;
import java.sql.Statement;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;

/**
 * This class defines a Raidb1WaitForAllCompletionPolicyScenario. Test the
 * 'wait for all' completion policy when databases are failing
 *
 * @author <a href="mailto:flaugier@micropole-univers.com">Frederic Laugier</a>
 * @version 1.0
 */
public class Raidb1WaitForAllCompletionPolicyScenario extends SimpleRaidb1Template
{

  /**
   * Test CJDBC write transaction failover in raidb1 with variable pool
   */
  public void testWriteTransactionFailOverWithVariablePool()
  {
    try
    {
      // Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb1-variablepool-waitforall.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();

      // Drop a backend
      hm.stop(hm1);

      // Get a connection
      Connection con = getCJDBCConnection();
      assertNotNull("Connection is null", con);
      
      // Starts a transaction
      con.setAutoCommit(false);

      // Executes an update with only one backend enabled
      Statement stmt1 = con.createStatement();
      stmt1.executeUpdate("update product set name='horizontalTest1'");
      stmt1.close();

      // Executes an second update in a row
      Statement stmt2 = con.createStatement();
      stmt2.executeUpdate("update product set name='horizontalTest2'");
      stmt2.close();

      // Closes the transaction, and the connection
      con.rollback();
      con.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("failed to test c-jdbc transaction failover in variable pool connection manager");
    }
  }

  /**
   * Test CJDBC write failover in raidb1 with variable pool
   */
  public void testWriteFailOverWithVariablePool()
  {
    try
    {
      // Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb1-variablepool-waitforall.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();

      // Drop a backend
      hm.stop(hm1);

      // Get a connection
      Connection con = getCJDBCConnection();
      assertNotNull("Connection is null", con);

      // Executes an update with only one backend enabled
      Statement stmt1 = con.createStatement();
      stmt1.executeUpdate("update product set name='horizontalTest1'");
      stmt1.close();

      // Executes an second update in a row
      Statement stmt2 = con.createStatement();
      stmt2.executeUpdate("update product set name='horizontalTest2'");
      stmt2.close();

      // Closes the connection
      con.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("failed to test c-jdbc transaction failover in variable pool connection manager");
    }
  }

  /**
   * Test CJDBC write transaction failover in raidb1 with simple connection manager
   */
  public void testWriteTransactionFailOverWithNoPool()
  {
    try
    {
      // Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb1-nopool-waitforall.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();

      // Drop a backend
      hm.stop(hm1);

      // Get a connection
      Connection con = getCJDBCConnection();
      assertNotNull("Connection is null", con);

      // Starts a transaction
      con.setAutoCommit(false);

      // Executes an update with only one backend enabled
      Statement stmt1 = con.createStatement();
      stmt1.executeUpdate("update product set name='horizontalTest1'");
      stmt1.close();

      // Executes an second update in a row
      Statement stmt2 = con.createStatement();
      stmt2.executeUpdate("update product set name='horizontalTest2'");
      stmt2.close();

      // Closes the transaction, and the connection
      con.rollback();
      con.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("failed to test c-jdbc transaction failover in simple connection manager");
    }
  }

  /**
   * Test CJDBC write failover in raidb1 with simple connection manager
   */
  public void testWriteFailOverWithNoPool()
  {
    try
    {
      // Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb1-nopool-waitforall.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();

      // Drop a backend
      hm.stop(hm1);

      // Get a connection
      Connection con = getCJDBCConnection();
      assertNotNull("Connection is null", con);

      // Executes an update with only one backend enabled
      Statement stmt1 = con.createStatement();
      stmt1.executeUpdate("update product set name='horizontalTest1'");
      stmt1.close();

      // Executes an second update in a row
      Statement stmt2 = con.createStatement();
      stmt2.executeUpdate("update product set name='horizontalTest2'");
      stmt2.close();

      // Closes the connection
      con.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("failed to test c-jdbc transaction failover in simple connection manager");
    }
  }


}
