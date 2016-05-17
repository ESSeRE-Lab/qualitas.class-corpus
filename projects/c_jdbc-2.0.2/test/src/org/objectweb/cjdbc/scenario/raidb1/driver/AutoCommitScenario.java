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

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a AutoCommitScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class AutoCommitScenario extends SimpleRaidb1Template
{
  /**
   * Test change of autocommit is done properly.
   * 
   * @throws Exception if fails
   */
  public void testAutoCommit() throws Exception
  {
    cm.loadVirtualDatabases(controller, "myDB",
        "hsqldb-raidb1-pessimistic-roundrobin-wfall.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();

    Connection aConnection = getCJDBCConnection();
    Statement stmt = aConnection.createStatement();
    stmt.executeUpdate("create table test (id int)");
    aConnection.setAutoCommit(false);
    stmt.executeUpdate("insert into test (id) values (2)");
    aConnection.setAutoCommit(true);

    try
    {
      aConnection.commit();
      fail("Expected exception here because committing an autocommit connection");
    }
    catch (Exception expected)
    {

    }

    aConnection.close();

    Properties props = new Properties();
    props.put("user", "TEST");
    props.put("password", "");
    Connection hsqlCon = DriverManager.getConnection(
        "jdbc:hsqldb:hsql://localhost:9001", props);

    ResultSet set = hsqlCon.createStatement()
        .executeQuery("Select * from test");
    ArrayList list = ScenarioUtility.convertResultSet(set);
    System.out.println(list);
    assertTrue(list.size() == 1);
  }
}