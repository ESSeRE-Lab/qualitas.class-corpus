/**
 * Tribe: Group communication library.
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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.raidb1.cache;

import java.sql.Connection;
import java.util.ArrayList;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a CacheInvalidateScenario.
 * <p>
 * Check if the cache is properly invalidated
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class CacheInvalidateScenario extends Raidb1Template
{

  /**
   * Test cache invalidation
   * 
   * @throws Exception if fails
   */
  public void testCacheInvalidate() throws Exception
  {
    Connection con = getCJDBCConnection();
    con.setAutoCommit(false);
    String create = "CREATE TABLE TEST_TABLE ( TEST_ID VARCHAR(12) NOT NULL, TEST_VALUE VARCHAR(70) NOT NULL, CONSTRAINT TEST_PK PRIMARY KEY(TEST_ID))";
    String insert = "INSERT INTO TEST_TABLE VALUES('TEST', 'VVK')";
    String update = "UPDATE TEST_TABLE SET TEST_VALUE='VVK UPDATED' WHERE TEST_ID='TEST'";
    String delete = "DELETE FROM TEST_TABLE WHERE TEST_ID='TEST'";
    String drop = "DROP TABLE TEST_TABLE";
    String select = "SELECT TEST_VALUE FROM TEST_TABLE WHERE TEST_ID LIKE 'TEST'";

    con.createStatement().executeUpdate(create);
    ArrayList set1 = ScenarioUtility.getSingleQueryResult(select,con);
    //ResultSet set1 = con.createStatement().executeQuery(select);

    con.createStatement().executeUpdate(insert);
    ArrayList set2 = ScenarioUtility.getSingleQueryResult(select,con);
    //ResultSet set2 = con.createStatement().executeQuery(select);
    assertNotSame("Insert did not invalidate", set1,set2);

    con.createStatement().executeUpdate(update);
    ArrayList set3 = ScenarioUtility.getSingleQueryResult(select,con);
    //ResultSet set3 = con.createStatement().executeQuery(select);
    assertNotSame("Update did not invalidate", set2,set3);

    con.createStatement().executeUpdate(delete);
    ArrayList set4 = ScenarioUtility.getSingleQueryResult(select,con);
    //ResultSet set4 = con.createStatement().executeQuery(select);
    assertNotSame("Delete did not invalidate", set3,set4);

    con.createStatement().executeUpdate(insert);
    ArrayList set5 = ScenarioUtility.getSingleQueryResult(select,con);
    //ResultSet set5 = con.createStatement().executeQuery(select);
    assertNotSame("Insert2 did not invalidate", set4,set5);

    con.createStatement().executeUpdate(drop);
    try
    {
      con.createStatement().executeQuery(select);
      fail("Drop did not invalidate");
    }
    catch (Exception e)
    {
      // Request should fail is table has been droped
    }

    con.rollback();
    con.setAutoCommit(true);
    con.close();
  }
}