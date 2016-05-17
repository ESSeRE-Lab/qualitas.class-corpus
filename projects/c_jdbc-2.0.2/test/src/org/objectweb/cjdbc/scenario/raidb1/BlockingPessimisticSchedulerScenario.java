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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.raidb1;

import java.sql.Connection;
import java.sql.SQLException;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;

/**
 * This class defines a BlockingPessimisticSchedulerScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BlockingPessimisticSchedulerScenario extends SimpleRaidb1Template
{
  /**
   * TODO: testFakeDrop definition.
   * 
   * @throws Exception if an error occurs
   */
  public void testFakeDrop() throws Exception
  {
    //Load database
    cm
        .loadVirtualDatabases(controller, "myDB",
            "hsqldb-pessimistic-raidb1.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();

    Connection con = getCJDBCConnection();
    String query = "insert into table quizz values(0,0)";
    //String query = "drop table quizz";
    try
    {
      con.createStatement().executeUpdate(query);
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    try
    {
      con.createStatement().executeUpdate(query);
    }
    catch (SQLException e1)
    {
      e1.printStackTrace();
    }
  }
}