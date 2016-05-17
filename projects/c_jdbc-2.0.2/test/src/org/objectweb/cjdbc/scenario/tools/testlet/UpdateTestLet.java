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

import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This test executes in order: a select, an update and a select on the update
 * and checks the result set are as expected
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class UpdateTestLet extends AbstractConnectionTestLet
{
  
  private int totalExecute;
  
  /**
   * Creates a new <code>UpdateTestLet</code> object
   * 
   * @param con connection to use for testing
   */
  public UpdateTestLet(Connection con)
  {
    super(con);
    config.put(TABLE_NAME, "PRODUCT");
    config.put(COLUMN_NAME, "NAME");
    config.put(UPDATED_COLUMN_VALUE, "horizontalTest");
    config.put(SELECTED_COLUMNS, "*");
    config.put(USE_PREPARED_STATEMENT, "false");
    config.put(USE_TRANSACTIONS,"false");
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet#execute()
   */
  public void execute() throws Exception
  {
    String selectQuery = "Select " + config.get(SELECTED_COLUMNS) + " from "
        + config.get(TABLE_NAME);
    String updateQuery = "update " + config.get(TABLE_NAME) + " set "
        + config.get(COLUMN_NAME) + "='" + config.get(UPDATED_COLUMN_VALUE)+(totalExecute++)
        + "'";
    boolean usePrepared = Boolean.valueOf((String)config.get(USE_PREPARED_STATEMENT)).booleanValue();
    
    ResultSet rs1,rs2;
    if(usePrepared)
    {
      if(useTransaction())
        jdbcConnection.setAutoCommit(false);
      rs1 = jdbcConnection.prepareStatement(selectQuery).executeQuery();
      jdbcConnection.prepareStatement(updateQuery).executeUpdate();
      rs2 = jdbcConnection.prepareStatement(selectQuery).executeQuery(); 
      if(useTransaction())
        jdbcConnection.commit();
    }
    else
    {
      if(useTransaction())
        jdbcConnection.setAutoCommit(false);
      rs1 = jdbcConnection.createStatement().executeQuery(selectQuery);
      jdbcConnection.createStatement().executeUpdate(updateQuery);
      rs2 = jdbcConnection.createStatement().executeQuery(selectQuery);
      if(useTransaction())
        jdbcConnection.commit();
    }
    
    assertNotNull("ResultSet before update is null", rs1);
    assertNotNull("ResultSet after update is null", rs2);
    assertFalse(ScenarioUtility.checkEquals(rs1, rs2));
  }
}