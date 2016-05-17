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

package org.objectweb.cjdbc.scenario.raidb1.loadbalancer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.objectweb.cjdbc.scenario.templates.Raidb1RecoveryTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a EnableDisableBackendScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class EnableDisableBackendScenario extends Raidb1RecoveryTemplate
{
  /**
   * Marc got a dead lock on this after exception
   * 
   * @throws Exception if fails
   */
  public void testDeadLockInUniqueConstraintViolation() throws Exception
  {
    Connection aConnection = getCJDBCConnection();
    Statement stmt = aConnection.createStatement();
    stmt
        .executeUpdate("create table test2 (id int, CONSTRAINT test_PK PRIMARY KEY(id))");
    PreparedStatement insert = aConnection
        .prepareStatement("insert into test2 (id) values (?)");
    insert.setInt(1, 1);
    insert.executeUpdate();

    mainVdb.disableBackendWithCheckpoint("localhost");
    mainVdb.enableBackendFromCheckpoint("localhost");

    String query = "select * from test2";

    Connection hsqlcon = getHypersonicConnection(9001);
    ScenarioUtility.displaySingleQueryResult(query, hsqlcon);
    Connection hsqlcon2 = getHypersonicConnection(9002);
    ScenarioUtility.displaySingleQueryResult(query, hsqlcon2);

    PreparedStatement insert2 = aConnection
        .prepareStatement("insert into test2 (id) values (?)");
    insert2.setInt(1, 2);
    insert2.executeUpdate();

    hsqlcon = getHypersonicConnection(9001);
    ScenarioUtility.displaySingleQueryResult(query, hsqlcon);
    hsqlcon2 = getHypersonicConnection(9002);
    ScenarioUtility.displaySingleQueryResult(query, hsqlcon2);
  }
}