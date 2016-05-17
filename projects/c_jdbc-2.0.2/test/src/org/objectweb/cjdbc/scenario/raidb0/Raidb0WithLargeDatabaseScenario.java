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

package org.objectweb.cjdbc.scenario.raidb0;

import java.sql.Connection;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb0WithLargeDatabaseTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a Raidb0WithLargeDatabaseScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Raidb0WithLargeDatabaseScenario
    extends SimpleRaidb0WithLargeDatabaseTemplate
{
  /**
   * Test a RAIDb-0 controller with randowm wait pool and Blobs.
   * 
   * @throws Exception if an error occurs
   */
  public void testForBug() throws Exception
  {
    //Load database
    cm.loadVirtualDatabases(controller, "myDB",
        "hsqldb-raidb0-randomwaitpool.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();

    Connection con = getCJDBCConnection();
    ScenarioUtility.displaySingleQueryResult("Select * from blob34", con, true);
    ScenarioUtility
        .displaySingleQueryResult("Select * from blob34", con, false);
    ScenarioUtility.displaySingleQueryResult("Select * from blob3600", con,
        true);
    ScenarioUtility.displaySingleQueryResult("Select * from blob3600", con,
        false);
  }
}