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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.raidb1.loadbalancer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;

/**
 * This class defines a Raidb1RoundRobinScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class Raidb1RoundRobinScenario extends SimpleRaidb1Template
{
  /**
   * Test Round Robin against NPE
   * 
   * @throws Exception if fails
   */
  public void testRoundRobin() throws Exception
  {
    //Load database
    cm.loadVirtualDatabases(controller, "myDB",
        "hsqldb-raidb1-round-robin.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();
    
    //  Execute a request
    Connection con = getCJDBCConnection();
    assertNotNull("Connection is null", con);
    Statement statement = con.createStatement();
    ResultSet rs1 = statement.executeQuery("Select * from document");
    assertNotNull("ResultSet is null", rs1);
  }
  
}
