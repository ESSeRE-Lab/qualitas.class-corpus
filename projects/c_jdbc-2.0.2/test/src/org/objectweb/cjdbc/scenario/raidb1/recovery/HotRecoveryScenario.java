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

package org.objectweb.cjdbc.scenario.raidb1.recovery;

import java.sql.Connection;

import org.objectweb.cjdbc.scenario.templates.Raidb1RecoveryWithRequestSenderTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a HotRecoveryScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class HotRecoveryScenario
    extends Raidb1RecoveryWithRequestSenderTemplate
{
  private static final String BACKUP_LOGIN    = "user";
  private static final String BACKUP_PASSWORD = "";
  private static final String BACKUPER        = "Octopus";
  private static final String BACKUP_PATH     = "../backup";

  /**
   * A thread is sending requests in the background and we perform recovery on
   * spare backends. Also we want to add a backend
   * 
   * @throws Exception if fails
   */
  public void testHotDisableEnableWithCheckpoint() throws Exception
  {
    // Disable a backend
    String checkpoint = "check";
    String backend = "localhost2";
    mainVdb.disableBackendWithCheckpoint(backend);

    // Enable backend
    mainVdb.enableBackendFromCheckpoint(backend);

    if (sender.getExceptions().size() != 0)
      fail("Got exceptions during run:" + sender.getExceptions());
  }

  /**
   * A thread is sending requests in the background and we perform recovery on
   * spare backends. Also we want to add a backend
   * 
   * @throws Exception if fails
   */
  public void testHotRecovery() throws Exception
  {
    // Create a checkpoint and a backup from a backend
    String dumpName = "check" + System.currentTimeMillis();
    String backend = "localhost";
    mainVdb.backupBackend(backend, BACKUP_LOGIN, BACKUP_PASSWORD, dumpName,
        BACKUPER, BACKUP_PATH, null);

    Connection recoveryC = getHypersonicConnection(9003);
    ScenarioUtility.displayResultOnScreen(ScenarioUtility.getSingleQueryResult(
        "select * from recovery", recoveryC));

    // Disable other backend
    backend = "localhost2";
    String checkpoint2 = dumpName + "2";
    mainVdb.disableBackendWithCheckpoint(backend);

    // Restore the backend with the backup
    mainVdb.restoreDumpOnBackend(backend, BACKUP_LOGIN, BACKUP_PASSWORD,
        dumpName, null);

    // Enable the newly restored backend from the checkpoint and make it active
    mainVdb.enableBackendFromCheckpoint(backend, dumpName);

    if (sender.getExceptions().size() != 0)
      fail("Got exceptions during run:" + sender.getExceptions());
  }
}