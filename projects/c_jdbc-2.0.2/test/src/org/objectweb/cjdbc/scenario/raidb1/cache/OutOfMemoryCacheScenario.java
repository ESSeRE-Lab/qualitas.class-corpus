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

import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;
import org.objectweb.cjdbc.scenario.tools.util.MemoryProfiler;

/**
 * This class defines a OutOfMemoryCacheScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class OutOfMemoryCacheScenario extends Raidb1Template
{

  int     NUMBER_ROUND = 5000;

  boolean cleanTables  = true;

  public void testMultipleAdd() throws Exception
  {
    Connection con = getCJDBCConnection();

    MemoryProfiler memProp = new MemoryProfiler();
    memProp.start();

    for (int i = 0; i < NUMBER_ROUND; i++)
      round(con);
    
    memProp.quit();
    memProp.join();
  }

  private void round(Connection con) throws Exception
  {

    batchAdd(con, "DOCUMENT");
    batchAdd(con, "ADDRESS");
    batchAdd(con, "PRODUCT");

    AbstractResultCache cache = mainVdb.getRequestManager().getResultCache();

    if (cleanTables)
    {
      cleanTableCache(con, "DOCUMENT", "TOTAL");
      cleanTableCache(con, "ADDRESS", "FIRSTNAME");
      cleanTableCache(con, "PRODUCT", "NAME");
    }

  }

  private void cleanTableCache(Connection con, String table, String column)
      throws Exception
  {
    con.createStatement().executeUpdate(
        "update " + table + " set " + column + "=0");
  }

  private void batchAdd(Connection con, String table) throws Exception
  {
    for (int i = 0; i < 50; i++)
      ScenarioUtility.getSingleQueryResult("select * from " + table
          + " where ID=" + i, con, true);
  }
}