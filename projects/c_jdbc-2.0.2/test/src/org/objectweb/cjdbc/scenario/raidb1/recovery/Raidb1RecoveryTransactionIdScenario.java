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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.raidb1.recovery;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.recoverylog.RecoveryLog;
import org.objectweb.cjdbc.scenario.templates.Raidb1RecoveryTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a RecoveryTransactionIdScenario This was proposed by: Jeff
 * Kolesky <jeff@edusoft.com> Start the controller. Begin issuing queries: A
 * begin B insert into table_one ... C commit Restart the controller Issue
 * queries: D begin E insert into table_two ... F rollback
 * 
 * @author <a href="mailto:Nicolas Modrzyk@inria.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class Raidb1RecoveryTransactionIdScenario extends Raidb1RecoveryTemplate
{
  /**
   * Test transaction id of the recovery log is properly initialized from last
   * index.
   * 
   * @throws Exception if fails
   */
  public void testRecoveryLogTransactionID() throws Exception
  {
    // Get reference on the recovery log
    RecoveryLog log = mainVdb.getRequestManager().getRecoveryLog();

    // Get the first transaction id
    long idZero = log.getLastTransactionId();

    // Execute a transaction with INSERT that will update the recovery log
    Connection con = getCJDBCConnection();
    con.setAutoCommit(false);
    Statement statement = con.createStatement();
    statement.executeUpdate("INSERT INTO PRODUCT VALUES(50,'myproduct',5.5)");
    con.commit();
    statement.close();

    // ReStart the controller
    cm.stop("25322");
    controller = (Controller) cm.start("25322").getProcess();
    cm.loadVirtualDatabases(controller, "myDB", "hsqldb-raidb1-recovery.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();
    // Get the last transaction id from the recovery log
    log = mainVdb.getRequestManager().getRecoveryLog();
    long firstId = log.getLastTransactionId();

    // Execute a transaction and roll it back
    con = getCJDBCConnection();
    con.setAutoCommit(false);
    Statement statement2 = con.createStatement();
    statement2.executeUpdate("INSERT INTO DOCUMENT VALUES(50,32,5000.60)");
    con.rollback();
    statement2.close();

    Statement statement3 = con.createStatement();
    con.setAutoCommit(true);
    ResultSet rs = statement3
        .executeQuery("Select name from product where id=50");
    assertNotNull("Result set should not be null after restarting controller",
        rs);
    String myproduct = null;
    if (rs.next())
      myproduct = rs.getString("name");

    if (myproduct == null || myproduct.equalsIgnoreCase("myproduct") == false)
      fail("Could not get rigth value for product");
    long lastId = log.getLastTransactionId();

    System.out.println("firstId:" + firstId + ":lastId:" + lastId + "idZero:"
        + idZero);

    Connection hsqlcon = getHypersonicConnection(9003);
    ScenarioUtility.displaySingleQueryResult("Select * from RECOVERY", hsqlcon);
    assertTrue("RecoveryLog firstId should be greater than idZero.",
        firstId > idZero);
    assertTrue("RecoveryLog lastId is not properly set with firstId.[" + lastId
        + "==" + firstId + "]", lastId > firstId + 2);

    statement3.close();
    con.close();
  }
}