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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * This class defines a ProcedureTestLet
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ProcedureTestLet extends AbstractConnectionTestLet
{

  /**
   * Creates a new <code>ProcedureTestLet</code> object
   * 
   * @param con connection to use for testing
   */
  public ProcedureTestLet(Connection con)
  {
    super(con);
    config.put(PROCEDURE_NAME, "SIN(100)");
    config.put(USE_PREPARED_STATEMENT, Boolean.FALSE);
    config.put(USE_UPDATE_STATEMENT, Boolean.FALSE);
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet#execute()
   */
  public void execute() throws Exception
  {
    boolean useStatement = ((Boolean) config.get(USE_PREPARED_STATEMENT))
        .booleanValue();
    boolean useUpdate = ((Boolean) config.get(USE_UPDATE_STATEMENT))
        .booleanValue();
    ResultSet rs = null;
    int updated = -10;
    String procedure = "{call " + config.get(PROCEDURE_NAME);
    System.out.println(procedure);
    if (useStatement)
    {
      CallableStatement cs = jdbcConnection.prepareCall(procedure);
      if (!useUpdate)
        rs = cs.executeQuery();
      else
        updated = cs.executeUpdate();
    }
    else
    {
      Statement statement = jdbcConnection.createStatement();
      if (!useUpdate)
        rs = statement.executeQuery(procedure);
      else
        updated = statement.executeUpdate(procedure);
    }
    if (!useUpdate)
    {
      assertTrue("No result set", rs.next());
      String result = rs.getString(1);
      System.out.println(result);
    }
    else
    {
      System.out.println(updated + " rows updated");
      assertTrue("Expected updated value:", updated >= -1);
    }
  }

}