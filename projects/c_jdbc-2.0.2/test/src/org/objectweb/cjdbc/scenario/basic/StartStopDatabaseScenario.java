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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.basic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.objectweb.cjdbc.scenario.templates.Template;
import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;
import org.objectweb.cjdbc.scenario.tools.components.backend.DatabaseManager;

/**
 * This class defines a StartStopHsqlScenario
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class StartStopDatabaseScenario extends Template
{

  DatabaseManager hm;
  ComponentInterface hsql1;
  String            port1 = "9011";
  String            port2 = "9012";
  String            port3 = "9013";

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {

  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {

  }

  /**
   * testStart
   */
  public void testStart()
  {
    hm = new DatabaseManager();
    try
    {
      hsql1 = hm.start(port1);
      assertTrue("Hypersonic was not started", hm.isStarted(port1));
    }
    catch (Exception e)
    {
      fail("Could not start hsql.");
    }
    hm.stop(hsql1);
  }

  /**
   * testStartStop
   */
  public void testStartStop()
  {
    hm = new DatabaseManager();

    try
    {
      hsql1 = hm.start(port2);
      hm.stop(hsql1);
    }
    catch (Exception e)
    {
      fail("Could not control hsql");
    }
  }

  /**
   * Start Stop Connect Test
   */
  public void testStartStopConnect()
  {
    hm = new DatabaseManager();
    Connection con = null;
    Statement statement = null;
    ResultSet rs = null;

    try
    {
      hsql1 = hm.start(port3);
      hm.loaddatabase(port3);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      hm.stop(hsql1);
      fail("Could not start hsql.");
    }
    try
    {
      Class.forName("org.hsqldb.jdbcDriver");
    }
    catch (ClassNotFoundException e)
    {
      hm.stop(hsql1);
      fail("Could not find hsql driver");
    }
    try
    {
      Properties props = new Properties();
      props.put("user", "test");
      props.put("password", "");
      con = DriverManager.getConnection(
          "jdbc:hsqldb:hsql://localhost:" + port3, props);
    }
    catch (SQLException e1)
    {
      hm.stop(hsql1);
      e1.printStackTrace();
      fail("Could not get connection to hsql");
    }
    try
    {
      statement = con.createStatement();
      statement.setFetchSize(5);
    }
    catch (SQLException e2)
    {
      hm.stop(hsql1);
      fail("could not prepare statement");
    }
    try
    {

      rs = statement.executeQuery("select * from document");
    }
    catch (SQLException e3)
    {
      hm.stop(hsql1);
      System.out.println(e3.getMessage());
      e3.printStackTrace();
      fail("Could not get result set");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    try
    {
      while (rs.next())
      {
        rs.getString("ID");
      }
    }
    catch (Exception e)
    {
      hm.stop(hsql1);
      e.printStackTrace();
      fail("Could not access result set");
    }
    hm.stop(hsql1);
  }

  /**
   * Stupid test for new hsqldb
   * 
   * @throws Exception if fails
   */
  public void testNewHsqldbRelease() throws Exception
  {
    hm = new DatabaseManager();
    hsql1 = hm.start(port1);
    hm.loaddatabase(port1);
    Connection con = getHypersonicConnection(Integer.parseInt(port1));
    Statement stm = con.createStatement();
    //stm.executeUpdate("drop table test");
    stm
            .executeUpdate("create table test (id int,atime timestamp default current_timestamp)");
     int row = stm.executeUpdate("insert into test (id) values (1)");
     System.out.println(row);
     hm.stop(hsql1);
  }

}
