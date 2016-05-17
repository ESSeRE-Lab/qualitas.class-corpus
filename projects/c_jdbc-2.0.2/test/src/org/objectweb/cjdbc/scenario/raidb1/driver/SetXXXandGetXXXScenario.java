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
 *  Initial developer(s): Marc Wick.
 *  Contributor(s): Emmanuel Cecchet
 */

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.scenario.templates.Raidb1Template;

/**
 * This class is testing the setter for preparedstatement and the getters for
 * ResultSet.
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 */
public class SetXXXandGetXXXScenario extends Raidb1Template
{

  String tableName;

  /**
   * The connection to be used to the database for the test
   */

  /**
   * Load C-JDBC driver and retrieve connection from database
   * 
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    tableName = "test" + System.currentTimeMillis();
    super.setUp();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {
    try
    {
      Connection con = getCJDBCConnection();
      con.createStatement().executeUpdate("drop table " + tableName);
      con.close();
    }
    catch (Exception e)
    {

    }
    super.tearDown();
  }

  /**
   * Test method for boolean
   * 
   * @throws Exception possibly ...
   */
  public void testBoolean() throws Exception
  {
    Properties props = new Properties();
    props.put("user", "user");
    props.put("password", "");
    // This is database specific value so for testing we use the hypersonic ones
    props.put("booleanTrue", "true");
    props.put("booleanFalse", "false");

    Connection con = getCJDBCConnection(
        new ControllerInfo[]{new ControllerInfo("localhost", 25322)}, "myDB",
        props);
    Statement stmt = con.createStatement();
    stmt.executeUpdate("create table " + tableName
        + " (isOk BIT, booleanChar char(1))");
    java.sql.PreparedStatement pstmt = con.prepareStatement("insert into "
        + tableName + " (isok) values (?)");
    pstmt.setBoolean(1, false);
    pstmt.executeUpdate();
    ResultSet rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      boolean isok = rs.getBoolean(1);
      assertEquals("problems with setBoolean/getBoolean for value false",
          false, isok);
    }
    stmt.executeUpdate("delete from " + tableName);
    pstmt.setBoolean(1, true);
    pstmt.executeUpdate();
    rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      boolean isok = rs.getBoolean(1);
      assertEquals("problems with setBoolean/getBoolean for value true", true,
          isok);
      boolean isokCharNull = rs.getBoolean(2);
      assertEquals("null columns should return false", false, isokCharNull);
    }

    // now we test conversion from char/varchar columns
    pstmt = con
        .prepareStatement("update " + tableName + " set booleanChar =? ");
    pstmt.setString(1, "t");
    pstmt.executeUpdate();
    rs = stmt.executeQuery("select booleanChar from " + tableName);
    while (rs.next())
    {
      boolean isok = rs.getBoolean(1);
      assertEquals("problems with setBoolean/getBoolean for value true", true,
          isok);
    }

    con.close();
  }

  /**
   * Test method for BigDecimal
   * 
   * @throws Exception possibly ...
   */
  public void testBigDecimal() throws Exception
  {
    Connection con = getCJDBCConnection();
    Statement stmt = con.createStatement();
    stmt.executeUpdate("create table " + tableName
        + " (numbervalue decimal(10,2),floatvalue float, intvalue int)");
    java.sql.PreparedStatement pstmt = con.prepareStatement("insert into "
        + tableName + " (numbervalue, floatvalue,intvalue) values (?,?,?)");
    BigDecimal expectedValue = new BigDecimal("0.00");
    pstmt.setBigDecimal(1, expectedValue);
    pstmt.setFloat(2, expectedValue.floatValue());
    pstmt.setInt(3, expectedValue.intValue());
    pstmt.executeUpdate();
    ResultSet rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      BigDecimal bigDecimal = rs.getBigDecimal("numbervalue");
      assertEquals(expectedValue, bigDecimal);
      bigDecimal = rs.getBigDecimal("floatvalue");
      assertTrue(expectedValue.floatValue() == bigDecimal.floatValue());
      bigDecimal = rs.getBigDecimal("intvalue");
      assertEquals(expectedValue.intValue(), bigDecimal.intValue());
    }

    // test null value
    expectedValue = null;
    pstmt = con
        .prepareStatement("update " + tableName + " set numbervalue = ?");
    pstmt.setBigDecimal(1, expectedValue);
    pstmt.executeUpdate();
    rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      BigDecimal bigDecimal = rs.getBigDecimal("numbervalue");
      assertEquals(expectedValue, bigDecimal);
    }

    // test some values
    expectedValue = new BigDecimal("98765.41");
    pstmt = con.prepareStatement("update " + tableName
        + " set numbervalue = ?,floatvalue=?");
    pstmt.setBigDecimal(1, expectedValue);
    pstmt.setFloat(2, 0.1f);
    pstmt.executeUpdate();
    rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      BigDecimal bigDecimal = rs.getBigDecimal("numbervalue");
      assertEquals(expectedValue, bigDecimal);
      BigDecimal floatValue = rs.getBigDecimal("floatvalue");
      assertTrue(0.1f == floatValue.floatValue());
    }
    con.close();
  }

  /**
   * Test method for String
   * 
   * @throws Exception possibly ...
   */
  public void testString() throws Exception
  {
    Properties props = new Properties();
    props.put("user", "user");
    props.put("password", "");
    props.put("escapeBackslash", "false");
    props.put("escapeSingleQuote", "true");

    Connection con = getCJDBCConnection(
        new ControllerInfo[]{new ControllerInfo("localhost", 25322)}, "myDB",
        props);
    Statement stmt = con.createStatement();
    stmt.executeUpdate("create table " + tableName
        + " (stringvalue varchar(1000))");
    java.sql.PreparedStatement pstmt = con.prepareStatement("insert into "
        + tableName + " (stringvalue) values (?)");
    String expectedValue = "A text with lower and UPPERCASE letters and some numbers 12345143";
    pstmt.setString(1, expectedValue);
    pstmt.executeUpdate();
    ResultSet rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      String string = rs.getString(1);
      assertEquals(expectedValue, string);
    }

    // test null value
    expectedValue = null;
    pstmt = con
        .prepareStatement("update " + tableName + " set stringvalue = ?");
    pstmt.setString(1, expectedValue);
    pstmt.executeUpdate();
    rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      String string = rs.getString(1);
      assertEquals(expectedValue, string);
    }

    // test 'null' value
    expectedValue = "null";
    pstmt = con
        .prepareStatement("update " + tableName + " set stringvalue = ?");
    pstmt.setString(1, expectedValue);
    pstmt.executeUpdate();
    rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      String string = rs.getString(1);
      assertEquals(expectedValue, string);
    }

    // test quotes
    expectedValue = "\"this is a quote\" and 'single quotes' and other strange things \\ \\0 / {d  } ";
    pstmt = con
        .prepareStatement("update " + tableName + " set stringvalue = ?");
    pstmt.setEscapeProcessing(true);
    pstmt.setString(1, expectedValue);
    pstmt.executeUpdate();
    rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      String string = rs.getString(1);
      assertEquals(expectedValue, string);
    }

    con.close();
  }

  /**
   * Test macros are not replaced within string
   * 
   * @throws Exception if fails
   */
  public void testMacros() throws Exception
  {
    Properties props = new Properties();
    props.put("user", "user");
    props.put("password", "");
    props.put("escapeBackslash", "false");
    props.put("escapeSingleQuote", "true");
    // test macros
    Connection con = getCJDBCConnection(
        new ControllerInfo[]{new ControllerInfo("localhost", 25322)}, "myDB",
        props);
    Statement stmt = con.createStatement();
    stmt.executeUpdate("create table " + tableName
        + " (stringvalue varchar(1000))");
    String expectedValue = "macros in string should not be replaced , like rand(), now() and current_timestamp";
    PreparedStatement pstmt = con.prepareStatement("update " + tableName
        + " set stringvalue = ?");
    pstmt.setEscapeProcessing(true);
    pstmt.setString(1, expectedValue);
    pstmt.executeUpdate();
    ResultSet rs = pstmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      String string = rs.getString(1);
      assertEquals(expectedValue, string);
    }
    con.close();
  }

  /**
   * Test method for Time fields (timestamp, date, time)
   * 
   * @throws Exception possibly ...
   */
  public void testTimefields() throws Exception
  {
    Connection con = getCJDBCConnection();
    Statement stmt = con.createStatement();
    stmt
        .executeUpdate("create table "
            + tableName
            + " (timestampvalue timestamp, stringtsvalue varchar(20),stringtimevalue varchar(8))");
    java.sql.PreparedStatement pstmt = con.prepareStatement("insert into "
        + tableName
        + " (timestampvalue,stringtsvalue,stringtimevalue) values (?,?,?)");
    Date expectedValue = new Date();
    pstmt.setTimestamp(1, new java.sql.Timestamp(expectedValue.getTime()));
    pstmt.setString(2, new java.sql.Timestamp(expectedValue.getTime())
        .toString());
    pstmt.setString(3, new java.sql.Time(expectedValue.getTime()).toString());
    pstmt.executeUpdate();
    ResultSet rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      Date timestamp = rs.getTimestamp(1);
      assertEquals(expectedValue, timestamp);

      // test string column
      timestamp = rs.getTimestamp(2);
      assertEquals(expectedValue, timestamp);

      Date date = rs.getDate(1);

      // clear all time fields to get date
      Calendar cal = Calendar.getInstance();
      cal.setTime(expectedValue);
      cal.clear(Calendar.HOUR_OF_DAY);
      cal.clear(Calendar.HOUR);
      cal.clear(Calendar.MINUTE);
      cal.clear(Calendar.SECOND);
      cal.clear(Calendar.MILLISECOND);
      cal.clear(Calendar.AM_PM);
      assertEquals(cal.getTime(), date);

      date = rs.getDate(2);
      assertEquals(cal.getTime(), date);

      Date time = rs.getTime(1);
      // clear all date fields to get time
      cal = Calendar.getInstance();
      cal.setTime(expectedValue);
      cal.clear(Calendar.YEAR);
      cal.clear(Calendar.MONTH);
      cal.clear(Calendar.DATE);
      cal.clear(Calendar.MILLISECOND);
      assertEquals(cal.getTimeInMillis(), time.getTime());

      date = rs.getTime(3);
      assertEquals(cal.getTimeInMillis(), time.getTime());

    }

    // test null value
    expectedValue = null;
    pstmt = con.prepareStatement("update " + tableName
        + " set timestampvalue = ?");
    pstmt.setTimestamp(1, null);
    pstmt.executeUpdate();
    rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      Date date = rs.getTimestamp(1);
      assertEquals(expectedValue, date);
    }

    con.close();
  }

  /**
   * Test method for Object
   * 
   * @throws Exception possibly ...
   */
  public void testObject() throws Exception
  {
    Connection con = getCJDBCConnection();
    Statement stmt = con.createStatement();
    stmt.executeUpdate("create table " + tableName + " (numbervalue int)");
    java.sql.PreparedStatement pstmt = con.prepareStatement("insert into "
        + tableName + " (numbervalue) values (?)");
    Integer intObj = new Integer(0);
    pstmt.setObject(1, intObj);
    pstmt.executeUpdate();
    ResultSet rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      Integer intObjResult = new Integer(rs.getInt(1));
      assertEquals(intObj, intObjResult);
    }

    // test null value
    pstmt = con
        .prepareStatement("update " + tableName + " set numbervalue = ?");
    pstmt.setObject(1, null);
    pstmt.executeUpdate();
    rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      Object obj = rs.getObject(1);
      assertEquals(null, obj);
    }
    con.close();
  }

  /**
   * Test method for long
   * 
   * @throws Exception possibly ...
   */
  public void testLong() throws Exception
  {
    Connection con = getCJDBCConnection();
    Statement stmt = con.createStatement();
    stmt.executeUpdate("create table " + tableName
        + " (numbervalue int, stringvalue varchar(20),charvalue char)");
    java.sql.PreparedStatement pstmt = con.prepareStatement("insert into "
        + tableName + " (numbervalue,stringvalue,charvalue) values (?,22,3)");
    Integer intObj = new Integer(11);
    pstmt.setObject(1, intObj);
    pstmt.executeUpdate();
    ResultSet rs = stmt.executeQuery("select * from " + tableName);
    rs.next();
    assertEquals("convert int to long", 11, rs.getLong(1));
    assertEquals("convert varchar to long", 22, rs.getLong(2));
    assertEquals("convert char to long", 3, rs.getLong(3));
    rs.close();

    stmt
        .executeUpdate("update " + tableName + " set stringvalue = 'no number'");

    rs = stmt.executeQuery("select * from " + tableName);
    rs.next();
    try
    {
      rs.getLong(2);
      fail("invalid number should throw an SQLException");
    }
    catch (SQLException success)
    {
      // fine we were excpecting an SQLException
    }

    con.close();
  }

  /**
   * Test method for Bytes
   * 
   * @throws Exception possibly ...
   */
  public void testBytes() throws Exception
  {
    Connection con = getCJDBCConnection();
    Statement stmt = con.createStatement();
    stmt.executeUpdate("create table " + tableName + " (binaryvalue binary)");
    java.sql.PreparedStatement pstmt = con.prepareStatement("insert into "
        + tableName + " (binaryvalue) values (?)");

    byte[] bbuf = new byte[256];
    for (int i = -128; i < 128; i++)
    {
      bbuf[128 + i] = (byte) i;
    }

    pstmt.setBytes(1, bbuf);
    pstmt.executeUpdate();
    ResultSet rs = stmt.executeQuery("select * from " + tableName);
    while (rs.next())
    {
      bbuf = rs.getBytes(1);
      for (int i = -128; i < 128; i++)
      {
        assertEquals(bbuf[128 + i], i);
      }
    }

    con.close();
  }
}
