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

package org.objectweb.cjdbc.scenario.raidb0;

import java.sql.Connection;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb0Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This test will try to update and execute queries on tables that are on
 * different backend each time. It will perform the test in autocommit and
 * transaction mode.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Raidb0BasicConnectScenario extends SimpleRaidb0Template
{

  /**
   * Test CJDBC basic raidb0 with randomwait pool
   * 
   * @throws Exception if fails
   */
  public void testFailOverWithRandomWaitPool() throws Exception
  {
    //Load database
    cm.loadVirtualDatabases(controller, "myDB",
        "hsqldb-raidb0-randomwaitpool.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();

    completeTest("PRODUCT");
    completeTest("DOCUMENT");
  }

  private void completeTest(String tableName) throws Exception
  {
    queryUpdateTable(tableName, true, true);
    queryUpdateTable(tableName, true, false);
    queryUpdateTable(tableName, false, true);
    queryUpdateTable(tableName, false, false);
  }

  String documentUpdate = "update DOCUMENT set ADDRESSID=0";
  String productUpdate  = "update PRODUCT set NAME='product'";

  private void queryUpdateTable(String tableName, boolean autoCommit,
      boolean prepare) throws Exception
  {
    Connection con = getCJDBCConnection();
    con.setAutoCommit(autoCommit);
    ScenarioUtility.displaySingleQueryResult("Select * from " + tableName, con,
        prepare);
    if (tableName.equalsIgnoreCase("DOCUMENT"))
    {
      if (prepare)
        con.prepareStatement(documentUpdate).executeUpdate();
      else
        con.createStatement().executeUpdate(documentUpdate);
    }
    if (tableName.equalsIgnoreCase("PRODUCT"))
    {
      if (prepare)
        con.prepareStatement(productUpdate).executeUpdate();
      else
        con.createStatement().executeUpdate(productUpdate);
    }
    ScenarioUtility.displaySingleQueryResult("Select * from " + tableName, con,
        prepare);
    if (!autoCommit)
      con.commit();
    con.close();
  }
}