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

package org.objectweb.cjdbc.console.gui.popups;

import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.objectweb.cjdbc.common.jmx.mbeans.DataCollectorMBean;
import org.objectweb.cjdbc.common.monitor.DataCollection;
import org.objectweb.cjdbc.common.monitor.DataCollectionNames;
import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.objects.BackendObject;
import org.objectweb.cjdbc.console.jmx.RmiJmxClient;
import org.objectweb.cjdbc.console.monitoring.MonitoringConsole;

/**
 * This class defines a BackendPopUpMenu
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class BackendPopUpMenu extends AbstractPopUpMenu
    implements
      MouseListener
{

  private BackendObject bo;
  JMenuItem             backendRemove;
  JMenuItem             backendCreate;
  JMenuItem             backendCheckpoint;
  JMenuItem             backendUnsetCheckpoint;
  JMenuItem             backendEnable;
  JMenuItem             backendDisable;
  JMenuItem             backendRestore;
  JMenuItem             backendBackup;
  JMenuItem             backendTestConnection;
  JMenu                 monitor;

  /**
   * Creates a new <code>BackendPopUpMenu</code> object
   * 
   * @param gui the referenced gui
   * @param bo the backend object
   */
  public BackendPopUpMenu(CjdbcGui gui, BackendObject bo)
  {
    super(gui);
    this.bo = bo;

    JSeparator separator = new JSeparator();

    backendRemove = new JMenuItem(GuiCommands.COMMAND_BACKEND_REMOVE);
    backendCreate = new JMenuItem(GuiCommands.COMMAND_BACKEND_CREATE_NEW);
    backendCheckpoint = new JMenuItem(
        GuiCommands.COMMAND_BACKEND_SET_CHECKPOINT);
    backendEnable = new JMenuItem(GuiConstants.BACKEND_STATE_ENABLED);
    backendDisable = new JMenuItem(GuiConstants.BACKEND_STATE_DISABLED);
    backendRestore = new JMenuItem(GuiConstants.BACKEND_STATE_RESTORE);
    backendBackup = new JMenuItem(GuiConstants.BACKEND_STATE_BACKUP);
    backendTestConnection = new JMenuItem(
        GuiCommands.COMMAND_BACKEND_TEST_CONNECTION);
    backendUnsetCheckpoint = new JMenuItem(
        GuiCommands.COMMAND_BACKEND_UNSET_CHECKPOINT);

    this.add(backendRemove).addActionListener(this);
    this.add(backendCreate).addActionListener(this);
    this.add(backendCheckpoint).addActionListener(this);
    this.add(backendUnsetCheckpoint).addActionListener(this);
    this.add(backendTestConnection).addActionListener(this);
    this.add(separator);
    this.add(backendEnable).addActionListener(this);
    this.add(backendDisable).addActionListener(this);
    this.add(backendRestore).addActionListener(this);
    this.add(backendBackup).addActionListener(this);
    this.add(separator);

    buildMonitorMenu();
  }

  private void buildMonitorMenu()
  {
    monitor = new JMenu("Monitor");
    addToMonitorMenu(DataCollection.BACKEND_ACTIVE_TRANSACTION);
    addToMonitorMenu(DataCollection.BACKEND_PENDING_REQUESTS);
    addToMonitorMenu(DataCollection.BACKEND_TOTAL_ACTIVE_CONNECTIONS);
    addToMonitorMenu(DataCollection.BACKEND_TOTAL_READ_REQUEST);
    addToMonitorMenu(DataCollection.BACKEND_TOTAL_REQUEST);
    addToMonitorMenu(DataCollection.BACKEND_TOTAL_TRANSACTIONS);
    addToMonitorMenu(DataCollection.BACKEND_TOTAL_WRITE_REQUEST);
    this.add(monitor);
  }

  private void addToMonitorMenu(int type)
  {
    String typeName = DataCollectionNames.get(type);
    JMenuItem item = new JMenuItem(typeName);
    String action = MonitoringConsole.getBackendActionCommand(typeName, bo
        .getDatabase(), bo.getName());
    item.setActionCommand(action);
    monitor.add(item).addActionListener(this);
  }

  /**
   * Returns the backendCheckpoint value.
   * 
   * @return Returns the backendCheckpoint.
   */
  public final JMenuItem getBackendCheckpoint()
  {
    return backendCheckpoint;
  }

  /**
   * Returns the backendCreate value.
   * 
   * @return Returns the backendCreate.
   */
  public final JMenuItem getBackendCreate()
  {
    return backendCreate;
  }

  /**
   * Returns the backendRemove value.
   * 
   * @return Returns the backendRemove.
   */
  public final JMenuItem getBackendRemove()
  {
    return backendRemove;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    String action = e.getActionCommand();
    if (action.startsWith("graph"))
    {
      RmiJmxClient controllerMBean = bo.getJmxClient();
      DataCollectorMBean dataCollectorMBean;
      try
      {
        dataCollectorMBean = controllerMBean.getDataCollectorProxy();
        MonitoringConsole.graph(action, dataCollectorMBean, -1, 3600, 1000, 1,
            null);
        return;
      }
      catch (Exception e1)
      {
        e1.printStackTrace();
        return;
      }
    }

    if (action.equals(GuiCommands.COMMAND_BACKEND_REMOVE))
    {
      gui.publicActionRemoveBackend(bo);
    }
    else if (action.equals(GuiCommands.COMMAND_BACKEND_CREATE_NEW))
    {
      gui.publicActionNewBackendPrompt(bo);
    }
    else if (action.equals(GuiCommands.COMMAND_BACKEND_SET_CHECKPOINT))
    {
      gui.publicActionSetCheckpoint(bo);
    }
    else if (action.equals(GuiCommands.COMMAND_BACKEND_TEST_CONNECTION))
    {
      gui.publicActionTestBackendConnection(bo);
    }
    else if (action.equals(GuiCommands.COMMAND_BACKEND_UNSET_CHECKPOINT))
    {
      gui.publicActionUnSetCheckpoint(bo);
    }
    else
    {
      gui.publicActionExecuteBackendDrop(action, bo.getName());
    }

  }

  /**
   * Returns the backendBackup value.
   * 
   * @return Returns the backendBackup.
   */
  public final JMenuItem getBackendBackup()
  {
    return backendBackup;
  }

  /**
   * Returns the backendDisable value.
   * 
   * @return Returns the backendDisable.
   */
  public final JMenuItem getBackendDisable()
  {
    return backendDisable;
  }

  /**
   * Returns the backendEnable value.
   * 
   * @return Returns the backendEnable.
   */
  public final JMenuItem getBackendEnable()
  {
    return backendEnable;
  }

  /**
   * Returns the backendRestore value.
   * 
   * @return Returns the backendRestore.
   */
  public final JMenuItem getBackendRestore()
  {
    return backendRestore;
  }
}