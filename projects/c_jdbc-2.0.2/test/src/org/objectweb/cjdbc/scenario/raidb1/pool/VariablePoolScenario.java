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

package org.objectweb.cjdbc.scenario.raidb1.pool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;

/**
 * Having problems with the variable pool
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class VariablePoolScenario extends SimpleRaidb1Template
{
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    super.setUp();
    try
    {
      cm.loadVirtualDatabases(controller, "myDB", "hsqldb-thinks-oracle.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Test proposed by Marc 
   * 
   * @throws Exception if fails
   */
  public void testGetManyConnection() throws Exception
  {
    Connection aConnection = getCJDBCConnection();
    aConnection.setAutoCommit(false);
    Statement stmt = aConnection.createStatement();
    try
    {
      stmt.executeUpdate("drop table test2");
    }
    catch (SQLException ignore)
    {
      // table unknown ?
    }
    stmt
        .executeUpdate("create table test2 (id int, CONSTRAINT test_PK PRIMARY KEY(id))");
    PreparedStatement insert = aConnection
        .prepareStatement("insert into test2 (id) values (?)");
    insert.setInt(1, 1);
    insert.executeUpdate();
    aConnection.commit();
    aConnection.setAutoCommit(true);
    aConnection.setAutoCommit(false);
    insert = aConnection.prepareStatement("insert into test2 (id) values (?)");
    insert.setInt(1, 2);
    insert.executeUpdate();
    aConnection.commit();
    aConnection.setAutoCommit(true);
    aConnection.close();
  }
}