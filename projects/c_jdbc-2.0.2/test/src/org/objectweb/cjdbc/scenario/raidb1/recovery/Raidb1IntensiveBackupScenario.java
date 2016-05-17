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

import org.objectweb.cjdbc.scenario.templates.OneHundredTablesRaidb1Template;

/**
 * This class defines a Raidb1IntensiveBackupScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Raidb1IntensiveBackupScenario
    extends OneHundredTablesRaidb1Template
{
  private static final String BACKUP_LOGIN    = "user";
  private static final String BACKUP_PASSWORD = "";
  private static final String BACKUPER        = "Octopus";
  private static final String BACKUP_PATH     = "../backup";

  /**
   * Launch multiple backup sessions in a row
   * 
   * @throws Exception if fails
   */
  public void testMultipleBackupSessions() throws Exception
  {
    long time = System.currentTimeMillis();
    String dump = "dump" + time;
    mainVdb.backupBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD, dump,
        BACKUPER, BACKUP_PATH, null);
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    try
    {
      super.setUp();
      cm.loadVirtualDatabases(controller, "myDB", "hsqldb-raidb1.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("Could not start controller");
      tearDown();
    }
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {
    super.tearDown();
  }

}