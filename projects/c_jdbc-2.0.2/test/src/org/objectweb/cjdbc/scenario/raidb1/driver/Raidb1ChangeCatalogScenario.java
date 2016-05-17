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

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;

/**
 * Catalogs scenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modryzk </a>
 * @version 1.0
 */
public class Raidb1ChangeCatalogScenario extends SimpleRaidb1Template
{

  /**
   * Test changing of catalogs from connection
   * 
   * @throws Exception if something's wrong
   */
  public void testChangeCatalog() throws Exception
  {
    Connection con = getCJDBCConnection("25322", "myDB1");
    con.setCatalog("myDB1");
    Statement statement = con.createStatement();
    ResultSet rs = statement.executeQuery("Select * from document");
    assertNotNull("ResultSet is null", rs);
    statement.close();
    int count1 = controller.getVirtualDatabase("myDB1").getRequestManager()
        .getScheduler().getNumberRead();
    con.setCatalog("myDB2");
    statement = con.createStatement();
    rs = statement.executeQuery("Select * from document");
    assertNotNull("ResultSet is null", rs);
    statement.close();
    int count2 = controller.getVirtualDatabase("myDB2").getRequestManager()
        .getScheduler().getNumberRead();
    assertTrue("Expected only one request on catalog myDB1,was:" + count1,
        count1 == 1);
    assertTrue("Expected only one request on catalog myDB2,was:" + count2,
        count2 == 1);
    assertTrue("Expected same number of requests on both catalogs",
        count2 == count1);
  }

  /**
   * try to change catalog without a proper login
   * 
   * @throws Exception if fails
   */
  public void testChangeCatalogWithWrongUser() throws Exception
  {
    Exception exception = null;
    Connection con = null;
    try
    {
      con = getCJDBCConnection("25322", "myDB1", "user2", "");
    }
    catch (Exception e)
    {
      exception = e;
      e.printStackTrace();
    }
    assertNull("The user should be properly authenticated.", exception);
    try
    {
      con.setCatalog("myDB2");
    }
    catch (SQLException sql)
    {
      //expected since not authentified
      exception = sql;
    }
    assertNotNull("Changing of catalog should have thrown an exception",
        exception);
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    super.setUp();
    try
    {
    cm.loadVirtualDatabases(controller, "myDB1",
        "hsqldb-raidb1-with2virtualdatabases.xml");
    cm.loadVirtualDatabases(controller, "myDB2",
        "hsqldb-raidb1-with2virtualdatabases.xml");
    mainVdb = controller.getVirtualDatabase("myDB1");
    mainVdb.enableAllBackends();
    mainVdb = controller.getVirtualDatabase("myDB2");
    mainVdb.enableAllBackends();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}
