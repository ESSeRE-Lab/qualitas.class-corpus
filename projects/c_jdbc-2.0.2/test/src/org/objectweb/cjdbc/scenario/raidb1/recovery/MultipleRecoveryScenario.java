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

import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.shared.BackupListener;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.scenario.templates.MultipleBackendsRaidb1Template;

/**
 * This class defines a MultipleRecoveryScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class MultipleRecoveryScenario extends MultipleBackendsRaidb1Template
{
  private static final String BACKUP_LOGIN    = "user";
  private static final String BACKUP_PASSWORD = "";
  private static final String BACKUPER        = "Octopus";
  private static final String BACKUP_PATH     = "../backup";

  int                         success         = 0;
  int                         failures        = 0;
  static final int            BACKENDS_TOTAL  = 2;

  Trace                       logger          = Trace
                                                  .getLogger(MultipleRecoveryScenario.class
                                                      .getName());

  /**
   * Test if we can start multiple octopus threads
   * 
   * @throws Exception if fails
   */
  public void testMultipleBackup() throws Exception
  {
    BackupListener listener = new BackupListener()
    {

      /**
       * @see org.objectweb.cjdbc.common.shared.BackupListener#success(java.lang.String)
       */
      public void success(String backendName)
      {
        success++;
        logger.info("LISTENER:Success for backend:" + backendName);
      }

      /**
       * @see org.objectweb.cjdbc.common.shared.BackupListener#failure(java.lang.String,
       *      java.lang.Exception)
       */
      public void failure(String backendName, Exception failure)
      {
        logger.info("LISTENER:Failure for backend:" + backendName
            + "because of:" + failure.getMessage());
        failure.printStackTrace();
        failures++;
      }
    };
    for (int i = 1; i <= BACKENDS_TOTAL; i++)
    {
      mainVdb.getRequestManager().backupBackend(
          mainVdb.getAndCheckBackend("localhost" + i,
              VirtualDatabase.NO_CHECK_BACKEND), BACKUP_LOGIN, BACKUP_PASSWORD,
          System.currentTimeMillis() + "dump" + i, BACKUPER, BACKUP_PATH, null);
    }

    for (int i = 1; i <= BACKENDS_TOTAL; i++)
    {
      logger.info("Waiting for backup:" + i);
      while (success + failures < BACKENDS_TOTAL)
      {
        synchronized (listener)
        {
          listener.wait();
        }
      }
      logger.info("backup " + i + " is finished");
    }

    assertEquals("Some backups have failed", BACKENDS_TOTAL, success);
  }
}