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

package org.objectweb.cjdbc.scenario.raidb2;

import java.sql.Connection;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb2Template;
import org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.FailoverOn2BackendsTestLet;

/**
 * This class defines a Raidb1BasicFailoverScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Raidb2BasicFailoverScenario extends SimpleRaidb2Template
{
  /**
   * Test CJDBC failover in raidb1 with variable pool
   * @throws Exception if fails
   */
  public void testFailOverWithVariablePool() throws Exception
  {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb2-variablepool.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      execute();
  }

  /**
   * Test CJDBC failover in raidb1 with randomwait pool
   * @throws Exception if fails
   */
  public void testFailOverWithRandomWaitPool() throws Exception
  {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb2-randomwaitpool.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      execute();
  }

  /**
   * Test CJDBC failover in raidb1 with fail fast pool
   * @throws Exception if fails
   */
  public void testFailOverWithFailFastPool() throws Exception
  {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb2-failfastpool.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      execute();
  }

  /**
   * Test CJDBC failover in raidb1 with simple connection manager
   * @throws Exception if fails
   */
  public void testFailOverWithNoPool() throws Exception
  {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB", "hsqldb-raidb2-nopool.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      execute();
  }

  /**
   * Execute the test for failover once the database is loaded
   */
  private void execute() throws Exception
  {
    //  Execute a request
    Connection con = getCJDBCConnection();
    FailoverOn2BackendsTestLet let = new FailoverOn2BackendsTestLet(con);
    let.set(AbstractTestLet.LIST_FAILOVER_BACKENDS,new Object[]{hm1,hm2});
    let.execute();
  }
}
