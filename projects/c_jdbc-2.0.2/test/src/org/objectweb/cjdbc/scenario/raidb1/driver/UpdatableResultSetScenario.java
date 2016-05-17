/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Initial developer(s): Jean-Bernard van Zuylen.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;

/**
 * This class defines a UpdatableResultSetScenario
 * 
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen </a>
 * @version 1.0
 */
public class UpdatableResultSetScenario extends Raidb1Template
{

  String tableName;
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    tableName = ("test" + System.currentTimeMillis()).toUpperCase();
    super.setUp();
    
    // Create test table
    try
    {
      Connection con = getCJDBCConnection();
      Statement stmt = con.createStatement();
      stmt.executeUpdate("CREATE TABLE " + tableName + "("
          + "ACCOUNT VARCHAR(20) NOT NULL, AMOUNT INTEGER, BLOCKED BOOLEAN, "
          + "PRIMARY KEY(ACCOUNT))");
      stmt.close();
      con.close();
    }
    catch (Exception e)
    {
    }
  }
  
  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {
    // Drop test table
    try
    {
      Connection con = getCJDBCConnection();
      Statement stmt = con.createStatement();
      stmt.executeUpdate("DROP TABLE " + tableName);
      stmt.close();
      con.close();
    }
    catch (Exception e)
    {
    }
    
    super.tearDown();
  }
  
  /**
   * Test inserting rows with Updatable ResultSets 
   * 
   * @throws Exception if fails
   */
  public void testInsertRow() throws Exception
  {
    Connection con = getCJDBCConnection();
    Statement stmt = con.createStatement(
        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    ResultSet res = stmt.executeQuery("SELECT * FROM " + tableName);

    // insert a new row
    res.moveToInsertRow();
    
    assertNull(res.getString(1));
    res.updateString(1, "111-2222222-33");
    assertEquals(res.getString(1), "111-2222222-33");
    
    assertEquals(res.getInt(2), 0);
    res.updateInt(2, 1000);
    assertEquals(res.getInt(2), 1000);
    
    assertFalse(res.getBoolean(3));
    res.updateBoolean(3, true);
    assertTrue(res.getBoolean(3));
    
    res.insertRow();
    
    // check row added to resultset 
    res.next();
    assertEquals(res.getString(1), "111-2222222-33");
    assertEquals(res.getInt(2), 1000);
    assertTrue(res.getBoolean(3));
    res.close();
    
    // check row added to database
    res = stmt.executeQuery("SELECT * FROM " + tableName);
    res.next();
    assertEquals(res.getString(1), "111-2222222-33");
    assertEquals(res.getInt(2), 1000);
    //assertTrue(res.getBoolean(3));
    res.close();
    
    // Clean up
    stmt.close();
    con.close();
  }
  
  /**
   * Test deleting rows with updatable ResultSets
   * 
   * @throws Exception if fails
   */
  public void testDeleteRow() throws Exception
  {
    Connection con = getCJDBCConnection();
    Statement stmt = con.createStatement();
    stmt.executeUpdate("INSERT INTO " + tableName
        + " VALUES ('111-2222222-33', 1000, TRUE)");
    stmt.close();
    
    stmt = con.createStatement(
        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    ResultSet res = stmt.executeQuery("SELECT * FROM " + tableName);

    // delete row in resultset
    res.next();
    res.deleteRow();
    
    // check row removed from resultset
    assertFalse(res.next());
    res.close();
    
    // check row added from database
    res = stmt.executeQuery("SELECT * FROM " + tableName);
    assertFalse(res.next());
    
    // clean up
    stmt.close();
    con.close();
  }
  
  /**
   * Test updating rows with Updatable ResultSets
   * 
   * @throws Exception if fails
   */
  public void testUpdateRow() throws Exception
  {
    Connection con = getCJDBCConnection();
    Statement stmt = con.createStatement();
    stmt.executeUpdate("INSERT INTO " + tableName
        + " VALUES ('111-2222222-33', 1000, TRUE)");
    stmt.close();
    
    stmt = con.createStatement(
        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    ResultSet res = stmt.executeQuery("SELECT * FROM " + tableName);

    // update row
    res.next();
    
    assertEquals(res.getString(1), "111-2222222-33");
    res.updateString(1, "777-8888888-99");
    assertEquals(res.getString(1), "777-8888888-99");
    
    assertEquals(res.getInt(2), 1000);
    res.updateInt(2, 2000);
    assertEquals(res.getInt(2), 2000);
    
    assertTrue(res.getBoolean(3));
    res.updateBoolean(3, false);
    assertFalse(res.getBoolean(3));
    
    res.updateRow();

    // check row updated in resultset
    res.first();
    assertEquals(res.getString(1), "777-8888888-99");
    assertEquals(res.getInt(2), 2000);
    assertFalse(res.getBoolean(3));
    res.close();
    
    // check row added to database
    res = stmt.executeQuery("SELECT * FROM " + tableName);
    res.next();
    assertEquals(res.getString(1), "777-8888888-99");
    assertEquals(res.getInt(2), 2000);
    assertFalse(res.getBoolean(3));
    res.close();
    
    // Clean up
    stmt.close();
    con.close();
  }
  
  /**
   * Test refreshing rows with Updatable ResultSets
   * 
   * @throws Exception if fails
   */
  public void testRefreshRow() throws Exception
  {
    Connection con = getCJDBCConnection();
    Statement stmt = con.createStatement();
    stmt.executeUpdate("INSERT INTO " + tableName
        + " VALUES ('111-2222222-33', 1000, TRUE)");
    
    Statement stmt2 = con.createStatement(
        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    ResultSet res = stmt2.executeQuery("select * from " + tableName);
    
    // update row in database
    stmt.executeUpdate("UPDATE " + tableName + " SET AMOUNT = 2000,"
        + " BLOCKED = FALSE WHERE ACCOUNT = '111-2222222-33'");

    // refresh row in resultset
    res.next();
    res.refreshRow();
    assertEquals(res.getString(1), "111-2222222-33");
    assertEquals(res.getInt(2), 2000);
    assertFalse(res.getBoolean(3));
    res.close();
    
    // clean up
    stmt2.close();
    stmt.close();
    con.close();
  }
}
