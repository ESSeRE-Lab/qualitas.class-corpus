/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.scenario.templates.Raidb1Template;

/**
 * This class defines a PreparedStatementSetStringScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modryzk </a>
 */
public class PreparedStatementSetStringScenario extends Raidb1Template
{
  /**
   * Test setString of <code>PreparedStatement</code>
   * 
   * @throws Exception if fails
   */
  public void testSetString() throws Exception
  {
    Properties props = new Properties();
    props.put("user", "user");
    props.put("password", "");
    props.put("escapeBackslash", "false");
    props.put("escapeSingleQuote", "true");

    Connection con = getCJDBCConnection(
        new ControllerInfo[]{new ControllerInfo("localhost", 25322)}, "myDB",
        props);

    String mygeorge = "" + 'G' + 'e' + '\'' + 'o' + 'r' + '\\' + 'g' + '\\'
        + 'e';
    String statement = "INSERT INTO ADDRESS VALUES(50,?,'Fuller','534 - 20th Ave.','Olten')";
    PreparedStatement ps = con.prepareStatement(statement);
    ps.setString(1, mygeorge);
    ps.setEscapeProcessing(true);
    ps.executeUpdate();

    PreparedStatement ps2 = con
        .prepareStatement("Select * from ADDRESS where id=?");
    ps2.setString(1, "50");
    ResultSet rs = ps2.executeQuery();
    assertTrue("Empty result set", rs.next());
    String george = rs.getString("firstname");
    assertEquals("Wrong value for george (was :" + george + " )", george,
        mygeorge);
  }
}