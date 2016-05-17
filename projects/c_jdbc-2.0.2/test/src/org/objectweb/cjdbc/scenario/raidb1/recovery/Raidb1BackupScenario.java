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
public class Raidb1BackupScenario extends Raidb1RecoveryTemplate
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
    Connection con = getCJDBCConnection();
    ResultSet rs = con.createStatement().executeQuery("select * from document");
    ArrayList list1 = ScenarioUtility.convertResultSet(rs);
    System.out.println(list1);
    rs.close();
    String dump = "dump" + System.currentTimeMillis();
    mainVdb.backupBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD, dump,
        BACKUPER, BACKUP_PATH, null);

    rs = con.createStatement().executeQuery("select * from document");
    ArrayList list2 = ScenarioUtility.convertResultSet(rs);
    rs.close();

    assertEquals("ResultSets are different", list1, list2);
    // mainVdb.enableBackendFromCheckpoint("localhost", checkpoint);
  }

  /**
   * Test complete recovery scenario, dropping a backend and putting it back
   * online
   * 
   * @throws Exception if fails
   */
  public void testCompleteRecoveryScenario() throws Exception
  {
    String dump = "dump" + System.currentTimeMillis();
    mainVdb.backupBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD, dump,
        BACKUPER, BACKUP_PATH, null);
    hm.stop(hm1);
    Connection con = getCJDBCConnection();
    con.createStatement().executeQuery("select * from document");
    ArrayList current = null;
    ArrayList previous = null;
    for (int i = 0; i < 50; i++)
    {
      // con = getCJDBCConnection();
      current = ScenarioUtility.convertResultSet(con.createStatement()
          .executeQuery("select * from document"));
      if (previous != null)
        assertEquals("Result sets are different before recovery", previous,
            current);
      previous = current;
    }
    mainVdb.forceDisableBackend("localhost2");
    hm1 = hm.start("9001");
    hm1.loadDatabase("database-useronly.template");
    mainVdb.restoreDumpOnBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD,
        dump, null);
    mainVdb.enableBackendFromCheckpoint("localhost");

    for (int i = 0; i < 50; i++)
    {
      // con = getCJDBCConnection();
      current = ScenarioUtility.convertResultSet(con.createStatement()
          .executeQuery("select * from document"));
      assertEquals("Results sets are different after recovery", previous,
          current);
      previous = current;
    }
  }

}