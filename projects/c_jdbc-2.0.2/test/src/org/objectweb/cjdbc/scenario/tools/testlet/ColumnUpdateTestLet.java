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

/**
 * Same as the <tt>executeUpdateLet</tt>, except the update is done on a
 * specific column
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ColumnUpdateTestLet extends AbstractConnectionTestLet
{

  /**
   * Creates a new <code>ColumnUpdateTestLet</code> object
   * 
   * @param con connection to use for testing
   */
  public ColumnUpdateTestLet(Connection con)
  {
    super(con);
    config.put(TABLE_NAME, "ADDRESS");
    config.put(COLUMN_NAME, "ID");
    config.put(UPDATED_COLUMN_VALUE, "emmanuel");
    config.put(SELECTED_COLUMNS, "FIRSTNAME");
    config.put(USE_PREPARED_STATEMENT, "true");
    config.put(USE_TRANSACTIONS,"false");
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet#execute()
   */
  public void execute() throws Exception
  {
    String selectQuery = "select " + config.get(SELECTED_COLUMNS) + " from "
        + config.get(TABLE_NAME) + " where " + config.get(COLUMN_NAME) + "=0";
    String updateQuery = "update " + config.get(TABLE_NAME) + " set "
        + config.get(SELECTED_COLUMNS) + "='"
        + config.get(UPDATED_COLUMN_VALUE) + "' where "
        + config.get(COLUMN_NAME) + "=0";
    ResultSet rs1, rs2;


    if (usePreparedStatement())
    {
      if(useTransaction())
        jdbcConnection.setAutoCommit(false);
      rs1 = jdbcConnection.createStatement().executeQuery(selectQuery);
      jdbcConnection.createStatement().executeUpdate(updateQuery);
      rs2 = jdbcConnection.createStatement().executeQuery(selectQuery);
      if(useTransaction())
        jdbcConnection.commit();
    }
    else
    {
      if(useTransaction())
        jdbcConnection.setAutoCommit(false);
      rs1 = jdbcConnection.prepareStatement(selectQuery).executeQuery();
      jdbcConnection.createStatement().executeUpdate(updateQuery);
      rs2 = jdbcConnection.prepareStatement(selectQuery).executeQuery();
      if(useTransaction())
        jdbcConnection.commit();
    }

    assertTrue("No results", rs1.next());
    String laura = rs1.getString((String) config.get(SELECTED_COLUMNS));
    assertEquals("Was expecting Laura", laura, "Laura");

    assertTrue("No results", rs2.next());
    String emmanuel = rs2.getString((String) config.get(SELECTED_COLUMNS));
    System.out.println(emmanuel);
    assertEquals("Was expecting Emmanuel", emmanuel, (String) config.get(UPDATED_COLUMN_VALUE));
  }

}