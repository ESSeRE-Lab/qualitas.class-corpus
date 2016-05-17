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

package org.objectweb.cjdbc.console.gui.threads.task;

import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.objects.BackendObject;

/**
 * This class defines a BackupBackendTask
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BackupBackendTask implements Runnable
{

  private VirtualDatabaseMBean database;
  private BackendObject        bob;
  private String               dumpName;
  private CjdbcGui             gui;

  /**
   * Creates a new <code>BackupBackendTask</code> object
   * 
   * @param gui the main frame
   * @param database the jmx client
   * @param bob the backend reference
   * @param dumpName the dump name
   */
  public BackupBackendTask(CjdbcGui gui, VirtualDatabaseMBean database,
      BackendObject bob, String dumpName)
  {
    this.database = database;
    this.dumpName = dumpName;
    this.bob = bob;
    this.gui = gui;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    String backendName = bob.getName();
    gui.actionChangeBackendState(bob, GuiConstants.BACKEND_STATE_BACKUP);

    gui.appendDebugText("Backing up backend:" + backendName);

    try
    {
      // FIXME: We don't gather the necessary parameters for a backup
      database.backupBackend(backendName, null, null, dumpName, null, null,
          null);
      gui.appendDebugText("Backup of backend:" + backendName + " completed");
    }
    catch (Exception e)
    {
      gui.appendDebugText("Backup of backend:" + backendName + " failed", e);
    }
    /*
     * gui.actionChangeBackendState(bob, GuiConstants.BACKEND_STATE_RECOVERY);
     * gui.appendDebugText("Enabling backend:" + backendName); try {
     * database.enableBackendFromCheckpoint(backendName, dumpName);
     * gui.appendDebugText("Backend:" + backendName + " enabled"); } catch
     * (Exception e) { gui .appendDebugText("Backend:" + backendName + " could
     * not be enabled", e); }
     */
    gui.paintBackendPane();
  }

}