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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.console.gui.threads.task;

import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.objects.BackendObject;

/**
 * This class defines a JmxTaskThread
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class RestoreBackendTask implements Runnable
{

  private VirtualDatabaseMBean database;
  private BackendObject        bob;
  private String               dumpName;
  private CjdbcGui             gui;

  /**
   * Creates a new <code>JmxTaskThread.java</code> object
   * 
   * @param gui the main frame
   * @param database the jmx client
   * @param bob the backend reference
   * @param dumpName the dump file name
   */
  public RestoreBackendTask(CjdbcGui gui, VirtualDatabaseMBean database,
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
    gui.actionChangeBackendState(bob, GuiConstants.BACKEND_STATE_RESTORE);

    try
    {
      // FIXME: We don't gather the necessary parameters for a backup
      database.restoreDumpOnBackend(bob.getName(), null, null, dumpName, null);
    }
    catch (Exception e)
    {
      gui.appendDebugText("Failed to enable backend:" + bob.getName(), e);
    }
    gui.publicActionLoadBackendsList(database.getVirtualDatabaseName());
  }

}