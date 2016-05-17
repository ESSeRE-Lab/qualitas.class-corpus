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

package org.objectweb.cjdbc.scenario.tools.testlet;

import java.sql.Connection;
import java.sql.ResultSet;

import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * Execute a Create table statement using the given connection, and then checks
 * on the given virtual database that the proper table can be retrieved.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class CreateTableTestLet extends AbstractConnectionTestLet
{

  /**
   * Creates a new <code>CreateTableTestLet</code> object
   * 
   * @param con connection to use for testing
   */
  public CreateTableTestLet(Connection con)
  {
    super(con);
    config.put(TABLE_NAME, "newtable" + System.currentTimeMillis());
    config.put(IGNORE_CASE, "true");
    config.put(TABLE_METADATA_COLUMNS, new String[]{"TABLE"});
    config.put(ITERATION, "" + 1);
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet#execute()
   */
  public void execute() throws Exception
  {
    String myTableName = (String) config.get(TABLE_NAME);
    VirtualDatabase mainVdb = (VirtualDatabase) config.get(VIRTUAL_DATABASE);
    if (mainVdb == null)
      throw new TestLetException("No virtual database defined");

    int iteration = Integer.parseInt((String) config.get(ITERATION));

    for (int i = 0; i < iteration; i++)
    {
      String tableName = myTableName + i;
      String createQuery = "CREATE TABLE " + tableName
          + "(ID INTEGER NOT NULL PRIMARY KEY,NAME VARCHAR(255),COST DECIMAL)";

      jdbcConnection.createStatement().executeUpdate(createQuery);
      ResultSet set = jdbcConnection.getMetaData().getTables(null, "%", tableName,
          (String[]) config.get(TABLE_METADATA_COLUMNS));
      assertTrue("No results after create table", set.next());
      String table = set.getString(3);
      if (ignoreCase())
        assertTrue(table.equalsIgnoreCase(tableName));
      else
        assertTrue(table.equals(tableName));
    }
  }
}