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

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a PrimaryKeysScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class PrimaryKeysScenario extends Raidb1Template
{
  public void testPrimaryKeys() throws Exception
  {
    Connection con = getCJDBCConnection();
    ResultSet keys = con.getMetaData().getPrimaryKeys(null, null, "PRODUCT");
    ArrayList list = ScenarioUtility.convertResultSet(keys);
    assertTrue(list.size()==1);
    ScenarioUtility.displayResultOnScreen(list);
    
    ResultSet keys2 = con.getMetaData().getPrimaryKeys(null, null, "PPOSITION");
    ArrayList list2 = ScenarioUtility.convertResultSet(keys2);
    ScenarioUtility.displayResultOnScreen(list2);
    assertTrue(list2.size()==0);
  }
}