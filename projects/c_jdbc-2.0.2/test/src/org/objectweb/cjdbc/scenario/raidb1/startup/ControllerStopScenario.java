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

package org.objectweb.cjdbc.scenario.raidb1.startup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.scenario.templates.Raidb1Template;

/**
 * This class defines a ControllerStopScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ControllerStopScenario extends Raidb1Template
{
  /**
   * Test reconnect after the restart of the controller
   * 
   * @throws Exception if fails
   */

  public void testReconnectAfterFailure() throws Exception
  {

    ArrayList list = new ArrayList();

    // Get a Connection
    Connection con = getCJDBCConnection();
    PreparedStatement stmt = con
        .prepareStatement("create table ATEST (id int, text varchar(1000))");
    stmt.executeUpdate();

    stmt = con.prepareStatement("insert into ATEST (id,text) values (?,?)");

    for (int i = 0; i < 100; i++)
    {

      stmt.setInt(1, i);
      stmt
          .setString(
              2,
              "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Phasellus quam nunc, dignissim sit amet, placerat at, feugiat quis, purus. Vivamus suscipit elementum velit. Nam ac libero. Mauris fermentum ultricies felis. Suspendisse tempor consectetuer risus. Aenean rutrum, orci sit amet tristique congue, mauris libero luctus nibh, vitae malesuada magna nulla nec velit. Etiam dapibus aliquet justo. Pellentesque posuere urna sit amet nunc. Fusce augue quam, venenatis a, ornare id, convallis non, leo. Aliquam laoreet tortor sit amet wisi. Donec dolor.");
      stmt.execute();
    }

    ResultSet rs = stmt.executeQuery("select * from ATEST");
    while (rs.next())
    {
      list.add(rs.getString("text"));
    }

    // Stop the controller
    cm.stop(25322);

    // Reload the controller
    controller = (Controller) cm.start("25322").getProcess();
    cm.loaddatabase("25322", "hsqldb-raidb1.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();

    //ScenarioUtility.getSingleQueryResult("select * from DOCUMENT", con);

    // Resend a query with the same connection as before
    stmt.setFetchSize(5);
    for (int i = 0; i < 1000; i++)
    {
      System.out.println("continue " + i + " "
          + Runtime.getRuntime().freeMemory() / 1000000f);
      stmt.setInt(1, i);
      stmt
          .setString(
              2,
              "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Phasellus quam nunc, dignissim sit amet, placerat at, feugiat quis, purus. Vivamus suscipit elementum velit. Nam ac libero. Mauris fermentum ultricies felis. Suspendisse tempor consectetuer risus. Aenean rutrum, orci sit amet tristique congue, mauris libero luctus nibh, vitae malesuada magna nulla nec velit. Etiam dapibus aliquet justo. Pellentesque posuere urna sit amet nunc. Fusce augue quam, venenatis a, ornare id, convallis non, leo. Aliquam laoreet tortor sit amet wisi. Donec dolor.");
      stmt.execute();
      
      // Execute a query
      rs = stmt.executeQuery("select * from ATEST");
//      while (rs.next())
//      {
//        //list.add(rs.getString("text"));
//      }
    }
  }
}
