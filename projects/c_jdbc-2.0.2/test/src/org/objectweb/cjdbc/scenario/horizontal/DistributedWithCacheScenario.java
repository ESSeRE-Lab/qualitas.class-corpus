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

package org.objectweb.cjdbc.scenario.horizontal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.objectweb.cjdbc.scenario.templates.HorizontalWithCacheTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a DistributedWithCacheScenario class
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class DistributedWithCacheScenario extends HorizontalWithCacheTemplate
{
  /**
   * Test the setup of a DistributedVirtualDatabase with each controller having a cache.
   * This test fails if requests cannot be send to the group after start up.
   * @throws Exception if fails
   */
  public void testSetup() throws Exception
  {
      Connection con = getCJDBCConnection("25322");
      Statement statement = con.createStatement();
      ResultSet rs = statement.executeQuery("Select * from document");
      System.out.println(ScenarioUtility.convertResultSet(rs));
      assertNotNull("ResultSet before failure is null", rs);
      
      statement.executeUpdate("update product set name='horizontalTest'");
      
      rs = statement.executeQuery("Select * from document");
      System.out.println(ScenarioUtility.convertResultSet(rs));
      assertNotNull("ResultSet before failure is null", rs);
  }
}