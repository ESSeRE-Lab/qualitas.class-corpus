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
import java.sql.SQLException;
import java.sql.Statement;

import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;

/**
 * This class defines a FailoverOn2BackendsTestLet
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class FailoverOn2BackendsTestLet extends AbstractConnectionTestLet
{

  /**
   * Creates a new <code>FailoverOn2BackendsTestLet</code> object
   * 
   * @param con connection to use for testing
   */
  public FailoverOn2BackendsTestLet(Connection con)
  {
    super(con);
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet#execute()
   */
  public void execute() throws Exception
  {
    
    Object[] co = (Object[])config.get(LIST_FAILOVER_BACKENDS);
    if(co ==null || co.length<2)
      throw new Exception("Failover needs at least two backends");
    
    ComponentInterface hm1 = (ComponentInterface)co[0];
    ComponentInterface hm2 = (ComponentInterface)co[1];
    
    Statement statement = jdbcConnection.createStatement();
    ResultSet rs1 = statement.executeQuery("Select * from document");
    assertNotNull("ResultSet is null", rs1);

    // Drop a backend
    hm1.release();

    // Execute requests with same connection
    Statement statement2 = jdbcConnection.createStatement();
    ResultSet rs2 = statement2.executeQuery("Select * from document");
    assertNotNull("ResultSet after failover is null", rs2);
    rs1.last();
    rs2.last();
    assertTrue("Row numbers are different", rs1.getRow() == rs2.getRow());
    rs1.first();
    rs2.first();
    while (rs1.next() & rs2.next())
    {
      assertTrue("Some result differs from expect result set", rs1.getString(
          "id").equals(rs2.getString("id")));
    }

    // Drop other backend
    hm2.release();

    // Execute request
    Statement statement3 = jdbcConnection.createStatement();
    ResultSet rs3 = null;
    try
    {
      rs3 = statement3.executeQuery("Select * from document");
    }
    catch (SQLException expected)
    {
      // expected cause no more backends.
    }
    assertNull("Should not be able to get a result set anymore", rs3);

  }

}
