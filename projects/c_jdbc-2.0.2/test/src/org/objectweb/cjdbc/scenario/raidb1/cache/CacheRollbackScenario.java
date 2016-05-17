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

package org.objectweb.cjdbc.scenario.raidb1.cache;

import java.sql.Connection;
import java.util.ArrayList;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a CacheRollbackScenario. to test simple cache functionalities
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class CacheRollbackScenario extends Raidb1Template
{

  /**
   * Test rollback is actually executed on the cache. WAS NOT ;)
   * 
   * @throws Exception if fails
   */
  public void testRollback() throws Exception
  {
    Connection con = getCJDBCConnection();
    con.setAutoCommit(false);
    String sql = "Select * from DOCUMENT";
    String update = "update DOCUMENT set TOTAL=0.0";
    
    // Get initial result
    ArrayList set1 = ScenarioUtility.getSingleQueryResult(sql,con);
    
    // Execute update
    con.createStatement().executeUpdate(update);

    // Get updated result and compare
    ArrayList set2 = ScenarioUtility.getSingleQueryResult(sql,con);
    assertNotSame("Should be different", set1,set2);

    // Rollback and compare, should be same as first
    con.rollback();
    ArrayList set3 = ScenarioUtility.getSingleQueryResult(sql,con);
    assertEquals("Should be the same", set1,set3);
  }
}