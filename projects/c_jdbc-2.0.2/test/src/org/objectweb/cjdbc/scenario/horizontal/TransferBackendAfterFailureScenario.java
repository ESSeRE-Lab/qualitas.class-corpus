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

package org.objectweb.cjdbc.scenario.horizontal;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.scenario.templates.HorizontalWithRecoveryTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;
import org.objectweb.cjdbc.scenario.tools.testlet.UpdateTestLet;

/**
 * This class defines a TransferBackendAfterFailureScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class TransferBackendAfterFailureScenario
    extends HorizontalWithRecoveryTemplate
{
  private static final String DUMP_NAME       = "dump1";
  private static final String BACKUP_LOGIN    = "user";
  private static final String BACKUP_PASSWORD = "";
  private static final String BACKUPER        = "Octopus";
  private static final String BACKUP_PATH     = "../backup";

  /**
   * Is this feasible ?
   * 
   * @throws Exception if fails
   */
  public void testDataIntegrity() throws Exception
  {
    // get Connection
    Connection con = getCJDBCConnection("25323");
    ScenarioUtility.displaySingleQueryResult("select * from PRODUCT", con);

    // Disable and backup backend localhost and enable backend
    mainVdb1.backupBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD,
        DUMP_NAME, BACKUPER, BACKUP_PATH, null);

    // Simulate a failure on controller 2
    controller2.shutdown(Constants.SHUTDOWN_WAIT);

    // Execute some queries so the group communication figures out a controller
    // died
    for (int i = 0; i < 10; i++)
    {
      Thread.sleep(100);
      con.createStatement().execute("update PRODUCT set name='loose'");
    }

    // Replicate the backend to the remaining controller
    Map map1 = new HashMap();
    map1.put("url", "jdbc:hsqldb:hsql://localhost:9003");
    mainVdb1.replicateBackend("localhost", "localhost3", map1);
    Map map2 = new HashMap();
    map2.put("url", "jdbc:hsqldb:hsql://localhost:9004");
    mainVdb1.replicateBackend("localhost", "localhost4", map2);

    // Restore the backends
    mainVdb1.restoreDumpOnBackend("localhost3", BACKUP_LOGIN, BACKUP_PASSWORD,
        DUMP_NAME, null);
    mainVdb1.restoreDumpOnBackend("localhost4", BACKUP_LOGIN, BACKUP_PASSWORD,
        DUMP_NAME, null);

    // Enable the newly restored backends
    mainVdb1.enableBackendFromCheckpoint("localhost3");
    mainVdb1.enableBackendFromCheckpoint("localhost4");

    // Execute some writes
    UpdateTestLet let = new UpdateTestLet(con);
    let.execute();

    // Query each backend independently
    Connection con1 = getHypersonicConnection(9001);
    Connection con2 = getHypersonicConnection(9002);
    Connection con3 = getHypersonicConnection(9003);
    Connection con4 = getHypersonicConnection(9004);
    ArrayList list1 = ScenarioUtility.getSingleQueryResult(
        "select * from PRODUCT", con1);
    ArrayList list2 = ScenarioUtility.getSingleQueryResult(
        "select * from PRODUCT", con2);
    ArrayList list3 = ScenarioUtility.getSingleQueryResult(
        "select * from PRODUCT", con3);
    ArrayList list4 = ScenarioUtility.getSingleQueryResult(
        "select * from PRODUCT", con4);

    // Check results
    assertEquals(list1, list2);
    assertEquals(list1, list3);
    assertEquals(list1, list4);
  }
}