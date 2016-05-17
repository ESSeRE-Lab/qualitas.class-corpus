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
import java.sql.Statement;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class test different virtual database configuration in raidb1
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class Raidb1TestLoadBalancersScenario extends SimpleRaidb1Template
{
  /**
   * Test Round Robyn configuration in Raidb1
   * 
   * @throws Exception if an error occurs
   */
  public void testRoundRobyn() throws Exception
  {
    try
    {
    cm.loadVirtualDatabases(controller, "myDB", "hsqldb-raidb1-round-robin.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();
    
    Connection con = getCJDBCConnection();
    Statement statement = con.createStatement();
    ResultSet rs1 = statement.executeQuery("Select * from document");
    assertTrue("ResultSet is null", rs1.next());
    System.out.println(ScenarioUtility.convertResultSet(rs1));
    }
    catch(Exception e)
    {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}