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

package org.objectweb.cjdbc.scenario.raidb1.loadbalancer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a Raidb1AddingBackendScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Raidb1AddingBackendScenario extends Raidb1Template
{
  /**
   * Add a new backend to the current database
   * 
   * @throws Exception if fails
   */
  public void testAddNewBackend() throws Exception
  {
    // Start new database
    hm.start("9003");
    hm.loaddatabase("9003");

    // Prepare parameters
    HashMap parameters = new HashMap();
    parameters.put(DatabasesXmlTags.ATT_url,
        "jdbc:hsqldb:hsql://localhost:9003");

    // Replicate the backend
    mainVdb.replicateBackend("localhost", "localhost3", parameters);

    mainVdb.forceEnableBackend("localhost3");

    // Release other backends
    hm.stop(hm1);
    hm.stop(hm2);

    //Test new backend is really useable
    Connection con = getCJDBCConnection();
    ResultSet rs = con.createStatement().executeQuery("select * from document");
    ArrayList list1 = ScenarioUtility.convertResultSet(rs);
    System.out.println(list1);
  }
}