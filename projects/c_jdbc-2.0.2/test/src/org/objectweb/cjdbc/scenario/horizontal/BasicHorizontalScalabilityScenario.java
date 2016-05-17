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

import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.scenario.templates.HorizontalTemplate;

/**
 * This class defines a BasicHorizontalScalabilityScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modryzk </a>
 * @version 1.0
 */
public class BasicHorizontalScalabilityScenario extends HorizontalTemplate
{

  /**
   * test horizontal scalability starts and stop ok
   * 
   * @throws Exception if fails
   */
  public void testAvailability() throws Exception
  {
    ControllerInfo[] controllers = new ControllerInfo[]{
        new ControllerInfo("localhost", 25322),
        new ControllerInfo("localhost", 25323)};
    Connection con = getCJDBCConnection(controllers);
    Statement statement = con.createStatement();
    ResultSet rs = statement.executeQuery("Select * from document");
    assertNotNull("ResultSet before failure is null", rs);
    cm.stop(controller1.getPortNumber());

    assertFalse("Controller1 should be stopped now", cm.isStarted("25322"));

    Statement statement2 = con.createStatement();
    ResultSet rs2 = statement2
        .executeQuery("Select * from document where id='0'");
    assertNotNull("ResultSet after failure is null", rs2);
  }

  /**
   * Test the non availability of a backend.
   * 
   * @throws Exception if an error occurs
   */
  public void testNonAvailability() throws Exception
  {
    ControllerInfo[] controllers = new ControllerInfo[]{new ControllerInfo(
        "localhost", 25322)};
    Connection con = getCJDBCConnection(controllers);
    Statement statement = con.createStatement();
    ResultSet rs = statement.executeQuery("Select * from document");
    assertNotNull("ResultSet before failure is null", rs);
    cm.stop(controller1.getPortNumber());

    assertFalse("Controller1 should be stopped now", cm.isStarted("25322"));

    Statement statement2 = con.createStatement();
    Exception notexpected = null;
    try
    {
      statement2.executeQuery("Select * from document where id='0'");
    }
    catch (Exception e)
    {
      notexpected = e;
    }
    assertNotNull("Should not switch to other controller [not in URL]",
        notexpected);
  }

}