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
import java.util.ArrayList;

import org.objectweb.cjdbc.scenario.templates.Raidb1RecoveryTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a Raidb1BackupScenario We had a problem where some tasks
 * were left on the backend pending requests and thus we could not start the
 * backup process.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modryzk </a>
 * @version 1.0
 */
public class Raidb1BackupMetadataScenario extends Raidb1RecoveryTemplate
{
  private static final String BACKUP_LOGIN    = "user";
  private static final String BACKUP_PASSWORD = "";
  private static final String BACKUPER        = "Octopus";
  private static final String BACKUP_PATH     = "../backup";

  /**
   * Test recovery scenario
   * 
   * @throws Exception if fails
   */
  public void testBasicRecoveryScenario() throws Exception
  {
    String dump1 = "dump1" + System.currentTimeMillis();
    String dump2 = "dump2" + System.currentTimeMillis();
    mainVdb.backupBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD, dump1,
        BACKUPER, BACKUP_PATH, null);
    mainVdb.restoreDumpOnBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD,
        dump1, null);
    mainVdb.enableBackendFromCheckpoint("localhost");

    Connection con = getCJDBCConnection();
    ResultSet rs = con.createStatement().executeQuery("select * from document");
    ArrayList list1 = ScenarioUtility.convertResultSet(rs);
    rs.close();

    mainVdb.backupBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD, dump2,
        BACKUPER, BACKUP_PATH, null);
    mainVdb.restoreDumpOnBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD,
        dump2, null);
    mainVdb.enableBackendFromCheckpoint("localhost");

    mainVdb.forceDisableBackend("localhost2");

    rs = con.createStatement().executeQuery("select * from document");
    ArrayList list2 = ScenarioUtility.convertResultSet(rs);
    rs.close();
    assertEquals("ResultSets are different", list1, list2);
  }
}
