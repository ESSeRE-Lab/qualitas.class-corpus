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

import java.io.File;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class test repeated PreparedStatement usage
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class PreparedStatementScenario extends Raidb1Template
{

  // Number of loops in each test
  private static final int N = 50;

  /**
   * Test repeated create statement
   * 
   * @throws Exception if fails
   */
  public void testLoopCreateStatement() throws Exception
  {
    Connection con = getCJDBCConnection();
    String tableName = "testblob" + System.currentTimeMillis();
    PreparedStatement stmt = null;
    for (int i = 0; i < N; i++)
    {
      stmt = con.prepareStatement("create table " + tableName + i
          + " (id INTEGER,name VARCHAR)");
      stmt.executeUpdate();
    }
  }

  /**
   * Test repeated create statement and close the statement after each execution
   * 
   * @throws Exception if fails
   */
  public void testLoopCreateStatementAndClose() throws Exception
  {
    Connection con = getCJDBCConnection();
    String tableName = "testblob" + System.currentTimeMillis();
    for (int i = 0; i < N; i++)
    {
      PreparedStatement stmt = null;
      stmt = con.prepareStatement("create table " + tableName + i
          + " (id INTEGER,name VARCHAR)");
      stmt.executeUpdate();
      stmt.close();
    }
  }

  /**
   * Test repeated blob insert
   * 
   * @throws Exception if fails
   */
  public void testLoopInsertBlobStatementAndClose() throws Exception
  {
    String imageFile = "/image/logo-noel.jpg";
    File fis = new File(getClass().getResource(imageFile).getFile());
    Connection con = getCJDBCConnection();

    String tableName = "testblob" + System.currentTimeMillis();
    PreparedStatement stmt = con.prepareStatement("create table " + tableName
        + " (id INTEGER,name VARCHAR)");

    stmt.executeUpdate();
    stmt.close();

    String query = "Insert into " + tableName + " values(1,?)";
    PreparedStatement ps1 = con.prepareStatement(query);
    Blob bob = new org.objectweb.cjdbc.driver.Blob(ScenarioUtility
        .readBinary(fis));
    ps1.setBlob(1, bob);
    ps1.executeUpdate();
    ps1.close();
  }

}