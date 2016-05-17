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

package org.objectweb.cjdbc.scenario.horizontal;

import java.sql.Connection;
import java.sql.SQLException;

import org.objectweb.cjdbc.scenario.templates.HorizontalTemplate;
import org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.ProcedureTestLet;

/**
 * This class defines a HorizontalStoredProcedureScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class HorizontalStoredProcedureScenario extends HorizontalTemplate
{
  /**
   * Test a simple stored procedure with hypersonic backends in distributed mode
   * 
   * @throws Exception if fails
   */
  public void testStoredProcedureWithStatementQuery() throws Exception
  {
    Connection con = getCJDBCConnection();
    con.setCatalog("myDB");
    ProcedureTestLet let = new ProcedureTestLet(con);
    let.set(AbstractTestLet.USE_UPDATE_STATEMENT, Boolean.FALSE);
    let.executeBatch(AbstractTestLet.USE_PREPARED_STATEMENT, new Boolean[]{
        Boolean.TRUE, Boolean.FALSE});
  }

  /**
   * Test a simple stored procedure with hypersonic backends in distributed mode
   * 
   * @throws Exception if fails
   */
  public void testStoredProcedureWithStatementUpdate() throws Exception
  {
    String msg = "Expected exception because update do not generate a row count";
    Connection con = getCJDBCConnection();
    con.setCatalog("myDB");
    ProcedureTestLet let = new ProcedureTestLet(con);
    let.set(AbstractTestLet.USE_UPDATE_STATEMENT, Boolean.TRUE);
    try
    {
      let.executeBatch(AbstractTestLet.USE_PREPARED_STATEMENT, new Boolean[]{
          Boolean.TRUE, Boolean.FALSE});
    }
    catch (Exception expected)
    {
      assertTrue(msg, expected instanceof SQLException);
      return;
    }
    fail(msg);
  }
}