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

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a PreparedStatementRecoveryTestLet
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class PreparedStatementRecoveryTestLet extends AbstractVdbTestLet
{

  SecureRandom rand = new SecureRandom();

  int          numberOfUpdates;

  /**
   * Creates a new <code>PreparedStatementRecoveryTestLet</code> object
   * 
   * @param vdb virtual database object
   */
  public PreparedStatementRecoveryTestLet(VirtualDatabase vdb)
  {
    super(vdb);
    config.put(NUMBER_OF_UPDATES, "50");
    config.put(USE_OPTIMIZED_STATEMENT, Boolean.TRUE);

  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet#execute()
   */
  public void execute() throws Exception
  {

    if (vdb.getAllBackendNames().size() < 2)
      throw new TestLetException("This testlet needs at least two backends");

    Properties props = new Properties();
    boolean use = ((Boolean) config.get(USE_OPTIMIZED_STATEMENT))
        .booleanValue();
    props.put("driverProcessed", "" + !use);

    Connection con = getCJDBCConnection(props);

    int numberOfUpdates = Integer.parseInt((String) config
        .get(NUMBER_OF_UPDATES));
    System.out.println("number of updates:" + numberOfUpdates);

    String create = "create table setget (col1 varchar(10), col2 varchar(10),col3 varchar(10),col4 varchar(10),col5 varchar(10),col6 varchar(10),col7 varchar(10),col8 varchar(10), col9 varchar)";
    String insert = "insert into setget values(?,?,?,?,?,?,?,?,?)";
    con.createStatement().executeUpdate(create);

    //Disable backend
    String checkpoint = "checkpoint" + System.currentTimeMillis();
    String backendName = getBackend(0).getName();
    vdb.disableBackendWithCheckpoint(backendName);

    // Execute random queries
    PreparedStatement ps = con.prepareStatement(insert);
    for (int i = 0; i < numberOfUpdates; i++)
      doExecuteRandomPStatement(ps, 9);
    
    con.close();

    // Enable backend
    vdb.enableBackendFromCheckpoint(backendName);
//
//    ArrayList resultRecovery = ScenarioUtility.getSingleQueryResult(
//        "select * from RECOVERY",
//        getDatabaseConnection("jdbc:hsqldb:hsql://localhost:9003"));
//    ScenarioUtility.displayResultOnScreen(resultRecovery);

    // Get original result
    String query = "select * from setget";
    ArrayList result1 = ScenarioUtility.getSingleQueryResult(query,
        getBackendConnection(0));
    ScenarioUtility.displayResultOnScreen(result1);
    // Get new result after recovery
    ArrayList result2 = ScenarioUtility.getSingleQueryResult(query,
        getBackendConnection(1));
    ScenarioUtility.displayResultOnScreen(result2);

    assertEquals(result1, result2);
  }

  private void doExecuteRandomPStatement(PreparedStatement ps, int size)
      throws SQLException
  {
    for (int i = 0; i < size; i++)
      doSetRandomParameter(i + 1, ps);

    int result = ps.executeUpdate();
    assertTrue("Updated columns returned false value", result == 1);
  }

  private void doSetRandomParameter(int index, PreparedStatement ps)
      throws SQLException
  {
    int type = rand.nextInt(12);
    if (type == 0)
      ps.setBoolean(index, rand.nextBoolean());
    else if (type == 1)
      ps.setByte(index, (byte) rand.nextInt());
    else if (type == 2)
    {
      byte[] b = new byte[10];
      rand.nextBytes(b);
      ps.setBytes(index, b);
    }
    else if (type == 3)
      ps.setDate(index, new java.sql.Date(2004, 10, 10));
    else if (type == 4)
      ps.setDouble(index, rand.nextDouble());
    else if (type == 5)
      ps.setFloat(index, rand.nextFloat());
    else if (type == 6)
      ps.setInt(index, rand.nextInt(10));
    else if (type == 7)
      ps.setLong(index, rand.nextLong());
    else if (type == 8)
      ps.setNull(index, java.sql.Types.VARCHAR);
    else if (type == 9)
      ps.setObject(index, new String("niko"));
    else if (type == 10)
      ps.setShort(index, (short) rand.nextInt(10));
    else if (type == 11)
      ps.setString(index, "niko");
    //else if (type == 12)
    //  ps.setURL(index, new URL("http://www.google.com"));
  }

}