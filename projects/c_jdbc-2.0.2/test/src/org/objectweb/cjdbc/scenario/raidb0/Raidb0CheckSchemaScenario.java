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

package org.objectweb.cjdbc.scenario.raidb0;

import java.sql.Connection;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb0Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a Raidb0CheckSchemaScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Raidb0CheckSchemaScenario extends SimpleRaidb0Template
{

  /**
   * Test schema construction in raidb0
   * 
   * @throws Exception if an error occurs
   */
  public void testSchemaRaidb0() throws Exception
  {
    cm.loadVirtualDatabases(controller, "myDB",
        "hsqldb-raidb0-randomwaitpool.xml");
    deepTestSchema();
  }

  private void deepTestSchema() throws Exception
  {
    mainVdb = controller.getVirtualDatabase("myDB");
    Connection con = getCJDBCConnection();

    testTheSchema(con, new boolean[]{true, true, true, true, true});
    // Localhost has ADDRESS and PRODUCT tables
    mainVdb.forceDisableBackend("localhost");
    testTheSchema(con, new boolean[]{true, true, true, false, false});
    ScenarioUtility.displayResultOnScreen(con.getMetaData().getTables(null,
        null, null, null));
    mainVdb.forceEnableBackend("localhost");
    testTheSchema(con, new boolean[]{true, true, true, true, true});
    // localhost2 has tables BLOB PPOSITION DOCUMENT
    mainVdb.forceDisableBackend("localhost2");
    testTheSchema(con, new boolean[]{false, false, false, true, true});
    ScenarioUtility.displayResultOnScreen(con.getMetaData().getTables(null,
        null, null, null));
    mainVdb.forceEnableBackend("localhost2");
    testTheSchema(con, new boolean[]{true, true, true, true, true});
    ScenarioUtility.displayResultOnScreen(con.getMetaData().getTables(null,
        null, null, null));
  }

  private void testTheSchema(Connection con, boolean[] success)
      throws Exception
  {
    testTheTable(con, "DOCUMENT", success[0]);
    testTheTable(con, "BLOB", success[1]);
    testTheTable(con, "PPOSITION", success[2]);
    testTheTable(con, "PRODUCT", success[3]);
    testTheTable(con, "ADDRESS", success[4]);
  }

  private void testTheTable(Connection con, String table, boolean assertTrue)
  {
    try
    {
      ScenarioUtility.getSingleQueryResult("select * from " + table, con);
      //      if(!assertTrue)
      //        fail("failure was expected on table:"+table);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      if (assertTrue)
        fail("success was expected on table:" + table);
    }
  }
}