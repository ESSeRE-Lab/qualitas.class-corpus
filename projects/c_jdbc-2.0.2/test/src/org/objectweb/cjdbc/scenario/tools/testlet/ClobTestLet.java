
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

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * This class defines a ClobTestLet
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ClobTestLet extends AbstractConnectionTestLet
{

  /**
   * Creates a new <code>ClobTestLet</code> object
   * 
   * @param con connection to use for testing
   */
  public ClobTestLet(Connection con)
  {
    super(con);
    config.put(TABLE_NAME, "BLOB");
    config.put(UPDATED_COLUMN_VALUE, "I am a CLOB");
    config.put(COLUMN_NAME, "blob");
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet#execute()
   */
  public void execute() throws Exception
  {
    String ss = (String) config.get(UPDATED_COLUMN_VALUE);
    String tableName = (String) config.get(TABLE_NAME);
    String columnName = (String) config.get(COLUMN_NAME);

    String insertQuery = "Insert into " + tableName + " values(3,?)";
    PreparedStatement ps;
    if (useCJDBCClass())
    {
      Clob clob = new org.objectweb.cjdbc.driver.Clob(ss);
      ps = jdbcConnection.prepareStatement(insertQuery);
      ps.setClob(1, clob);
      ps.executeUpdate();
    }
    else
    {
      ps = jdbcConnection.prepareStatement(insertQuery);
      ps.setString(1, ss);
      ps.executeUpdate();
    }
    // Test retrieval implementation independant
    ps = jdbcConnection.prepareStatement("Select * from " + tableName + " where id=3");
    ResultSet rs = ps.executeQuery();
    rs.first();
    Clob rsclob = rs.getClob(columnName);
    String ret = rsclob.getSubString(0, (int) rsclob.length());
    if (ret.equalsIgnoreCase(ss) == false)
    {
      fail("Retrieved:" + ret + " is different from:" + ss);
    }
    // Test without own implementation
    String clob2 = "I am a clob as well";
    PreparedStatement ps1 = jdbcConnection.prepareStatement("Insert into " + tableName
        + " values(4,?)");
    ps1.setString(1, clob2);
    ps1.executeUpdate();

    PreparedStatement ps2 = jdbcConnection.prepareStatement("Select * from " + tableName
        + " where id=4");
    ResultSet rs2 = ps2.executeQuery();
    rs2.first();
    Clob rs2clob = rs2.getClob(columnName);
    String ret2 = rs2clob.getSubString(0, (int) rs2clob.length());
    if (ret2.equalsIgnoreCase(clob2) == false)
    {
      fail("Retrieved:" + ret2 + " is different from:" + clob2);
    }

  }

}