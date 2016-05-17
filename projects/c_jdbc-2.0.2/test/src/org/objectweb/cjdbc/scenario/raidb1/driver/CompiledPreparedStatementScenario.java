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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;
import java.util.Properties;

import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.scenario.templates.Raidb1RecoveryTemplate;
import org.objectweb.cjdbc.scenario.tools.util.RequestSender;

/**
 * This class defines a CompiledPreparedStatementScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class CompiledPreparedStatementScenario extends Raidb1RecoveryTemplate
{

  static final String CJDBC_PREPARED_OPTIMIZED    = "cjdbc with preparedstatement optimized";
  static final String CJDBC_PREPARED_NO_OPTIMIZED = "cjdbc with preparedstatement non optimized";
  static final String CJDBC_NO_PREPARED           = "cjdbc without preparedstatement";
  static final String DATABASE_PREPARED           = "hsqldb with preparedstatement";
  static final String DATABASE_NO_PREPARED        = "hsqldb without preparedstatement";
  static final int    NUMBER_OF_EXECUTIONS        = 15000;

  /**
   * Test prepared statements with/without jaco's optimization
   * 
   * @throws Exception if fails
   */
  public void testComparePreparedStatementOptimization() throws Exception
  {
    // No prepared statement
    Connection con = getCJDBCConnection();
    dotest(CJDBC_NO_PREPARED, con, true);
    con.close();

    // prepared statement non optimized
    con = getCJDBCConnection();
    dotest(CJDBC_PREPARED_NO_OPTIMIZED, con, true);
    con.close();
    Properties props = new Properties();
    props.put("user", "user");
    props.put("password", "");
    props.put("driverProcessed", "false");
    con = getCJDBCConnection(new ControllerInfo[]{new ControllerInfo(
        "localhost", 25322)}, "myDB", props);

    // prepared statement optimized
    dotest(CJDBC_PREPARED_OPTIMIZED, con, true);
    con.close();

    // Compare with hypersonic
    con = getHypersonicConnection(9001);
    dotest(DATABASE_PREPARED, con, true);
    con.close();
    con = getHypersonicConnection(9001);
    dotest(DATABASE_NO_PREPARED, con, false);
    con.close();
  }

  private void dotest(String name, Connection con, boolean usePreparedStatement)
      throws Exception
  {
    System.gc();
    RequestSender sender = new RequestSender(con);
    sender.setLoopInThread(NUMBER_OF_EXECUTIONS);
    sender.setRequestInterval(-1);
    sender.setUsePreparedStatement(usePreparedStatement);
    Thread t = new Thread(sender);
    t.start();
    sender.setQuit(true);
    t.join();
    System.out.println("Test [" + name + "] lasted:" + sender.getRuntime());
    assertTrue("Got errors", sender.getExceptions().size() == 0);
  }
}