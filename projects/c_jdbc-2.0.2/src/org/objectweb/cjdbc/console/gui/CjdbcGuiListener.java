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

package org.objectweb.cjdbc.console.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.objectweb.cjdbc.common.jmx.notifications.CjdbcNotificationList;
import org.objectweb.cjdbc.common.jmx.notifications.JmxNotification;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.model.AttributeModel;
import org.objectweb.cjdbc.console.gui.model.OperationModel;
import org.objectweb.cjdbc.console.gui.objects.DatabaseObject;

/**
 * This class defines a CjdbcGuiListener
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class CjdbcGuiListener
    implements
      MouseListener,
      ActionListener,
      NotificationListener,
      FocusListener,
      ListSelectionListener
{
  private CjdbcGui  gui;
  static final int  JMX_SEQUENCE_CACHE = 100;
  private Hashtable sequences;

  /**
   * Creates a new <code>CjdbcGuiListener.java</code> object
   * 
   * @param gui the main frame
   */
  public CjdbcGuiListener(CjdbcGui gui)
  {
    this.gui = gui;
  }

  /**
   * Handle jmx notifications
   * 
   * @param notification the jmx notification from javax.managerment
   * @param handback not used
   */
  public void handleNotification(Notification notification, Object handback)
  {
    String xml = (String) notification.getUserData();
    // If the content of the notification is null, just debug it
    if (xml == null)
    {
      gui.appendDebugText("Got notification with null string");
      return;
    }
    try
    {
      JmxNotification notif = JmxNotification
          .createNotificationFromXmlString(xml);
      gui.appendDebugText("----- Jmx Notification -------");
      gui.appendDebugText(notif.toString());
      gui.appendDebugText("-----------------------------------");
      handleNotification(notif.getType(), notif);
    }
    catch (Exception e)
    {
      gui.appendDebugText("Exception while handling notification", e);
    }
  }

  private void handleNotification(String type, JmxNotification notification)
  {
    // have we processed the notification already ?
    if (processedSequence(notification.getSequence()))
    {
      gui.appendDebugText("Notification [" + notification.getSequence()
          + "] already processed");
      return;
    }

    if (type.equals(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_ADDED))
    {
      String backendName = notification
          .getDataValue(CjdbcNotificationList.DATA_NAME);
      String databaseName = notification
          .getDataValue(CjdbcNotificationList.DATA_DATABASE);
      String controller = notification.getControllerJmxName();
      gui.actionLoadBackend(databaseName, backendName, controller, true);
    }
    else if (type.equals(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_ENABLED))
    {
      String backendName = notification
          .getDataValue(CjdbcNotificationList.DATA_NAME);
      gui.actionSetBackendState(backendName);
    }
    else if (type
        .equals(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_ENABLED_WRITE))
    {
      String backendName = notification
          .getDataValue(CjdbcNotificationList.DATA_NAME);
      gui.actionSetBackendState(backendName);
    }
    else if (type
        .equals(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_DISABLED))
    {
      String backendName = notification
          .getDataValue(CjdbcNotificationList.DATA_NAME);
      gui.actionSetBackendState(backendName);
    }
    else if (type
        .equals(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_RECOVERING))
    {
      String backendName = notification
          .getDataValue(CjdbcNotificationList.DATA_NAME);
      gui.actionSetBackendState(backendName);
    }
    else if (type
        .equals(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_BACKINGUP))
    {
      String backendName = notification
          .getDataValue(CjdbcNotificationList.DATA_NAME);
      gui.actionSetBackendState(backendName);
    }
    else if (type
        .equals(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_DISABLING))
    {
      String backendName = notification
          .getDataValue(CjdbcNotificationList.DATA_NAME);
      gui.actionSetBackendState(backendName);
    }
    else if (type
        .equals(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_REPLAYING))
    {
      String backendName = notification
          .getDataValue(CjdbcNotificationList.DATA_NAME);
      gui.actionSetBackendState(backendName,
          GuiConstants.BACKEND_STATE_RECOVERY);
      gui.actionSetBackendState(backendName);
    }
    else if (type.equals(CjdbcNotificationList.VIRTUALDATABASE_NEW_DUMP_LIST))
    {
      String databaseName = notification
          .getDataValue(CjdbcNotificationList.DATA_DATABASE);
      gui.publicActionLoadDumpList(databaseName);
    }
    else if (type.equals(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_REMOVED))
    {
      String backendName = notification
          .getDataValue(CjdbcNotificationList.DATA_NAME);
      String controller = notification.getControllerJmxName();
      gui.publicActionRemoveBackendFromGui(backendName, controller);
    }
    else
    {
      gui.appendDebugText("Jmx notification type not recognized:" + type);
    }

  }

  /**
   * Check whether we have received this notification already...
   * 
   * @param sequence the sequence id
   * @return <tt>true</tt> if this id was already received
   */
  private boolean processedSequence(String sequence)
  {
    if (sequences == null)
    {
      sequences = new Hashtable();
      return false;
    }
    Object o = sequences.get(sequence);
    if (o == null)
    {
      sequences.put(sequence, Boolean.TRUE);
      if (sequences.size() > JMX_SEQUENCE_CACHE)
        sequences.clear();
      return false;
    }
    else
      return true;
  }

  /**
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e)
  {

    if (e.getClickCount() <= 1)
    {
      return;
    }
    if (e.getSource() instanceof JTable)
    {
      JTable table = (JTable) e.getSource();
      if (table.getName().equals(GuiConstants.TABLE_JMX_ATTRIBUTES))
      {
        int row = table.getSelectedRow();
        AttributeModel model = (AttributeModel) table.getModel();
        MBeanAttributeInfo[] info = model.getInfo();
        Object o = gui.mbeanList.getSelectedValue();
        gui.appendDebugText("Got attribute selection for mbean:" + o
            + " and attribute is:" + info[row].getName());
        gui.getAttributeChangeDialog((ObjectName) o, info[row]);
      }
      if (table.getName().equals(GuiConstants.TABLE_JMX_OPERATIONS))
      {
        int row = table.getSelectedRow();
        OperationModel model = (OperationModel) table.getModel();
        MBeanOperationInfo[] info = model.getInfo();
        Object o = gui.mbeanList.getSelectedValue();
        gui.appendDebugText("Got operation selection for mbean:" + o
            + " and operation is:" + info[row].getName());
        gui.getOperationCallDialog((ObjectName) o, info[row]);
      }
    }
  }

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e)
  {

  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e)
  {

  }

  /**
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e)
  {

  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e)
  {

  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    String action = e.getActionCommand();
    gui.appendDebugText("Got action:" + action);
    if (action.equals(GuiCommands.COMMAND_QUIT))
    {
      gui.publicActionQuit();
    }
    else if (action.equals(GuiCommands.COMMAND_ADD_CONTROLLER))
    {
      gui.publicActionAddControllerView();
    }
    else if (action.equals(GuiCommands.COMMAND_SAVE_CONFIGURATION_FILE))
    {
      gui.publicActionSaveConfigurationFile();
    }
    if (action.equals(GuiCommands.COMMAND_CLEAN_LOGGING_PANEL))
    {
      gui.publicActioncleanLoggingPane();
    }
    else if (action.equals(GuiCommands.COMMAND_ADD_CONFIG_FILE))
    {
      gui.publicActionAddXmlFile();
    }
    else if (action.equals(GuiCommands.COMMAND_SELECT_CONTROLLER))
    {
      String controllerName = ((JButton) e.getSource()).getName();
      gui.appendDebugText("Changed controller selection to:" + controllerName);
      gui.publicActionSelectNewController(controllerName);
    }
    else if (action.equals(GuiCommands.COMMAND_SELECT_DATABASE))
    {
      DatabaseObject source = (DatabaseObject) e.getSource();
      String databaseName = source.getName();
      gui.appendDebugText("Changed database selection to:" + databaseName);
      gui.publicActionSelectNewDatabase(databaseName);
    }
    else if (action.equals(GuiCommands.COMMAND_ADD_CONTROLLER_CANCEL))
    {
      gui.newControllerFrame.setVisible(false);
    }
    else if (action.equals(GuiCommands.COMMAND_ADD_CONTROLLER_APPROVE))
    {
      gui.publicActionAddController();
    }
    else if (action.equals(GuiCommands.COMMAND_SAVE_CONFIGURATION_FILE))
    {
      gui.publicActionSaveConfigurationFile();
    }
    else if (action.equals(GuiCommands.COMMAND_CLEAN_DEBUG_BUFFER))
    {
      gui.publicActionCleanDebugBuffer();
    }
    else if (action.equals(GuiCommands.COMMAND_DATABASE_AUTHENTICATE))
    {
      gui.publicActionLoadAuthenticatedDatabase();
    }
    else if (action.equals(GuiCommands.COMMAND_CREATE_BACKEND_APPROVE))
    {
      gui.publicActionCreateBackendExecute();
    }
    else if (action.equals(GuiCommands.COMMAND_CREATE_BACKEND_CANCEL))
    {
      gui.newBackendFrame.setVisible(false);
    }
    else if (action.equals(GuiCommands.COMMAND_HIDE_CHECKPOINT_FRAME))
    {
      gui.selectCheckpointFrame.setVisible(false);
    }
    else if (action.equals(GuiCommands.COMMAND_HIDE_SHUTDOWN_FRAME))
    {
      gui.selectShutdownFrame.setVisible(false);
    }
    else if (action.equals(GuiCommands.COMMAND_HIDE_BACKUP_FRAME))
    {
      gui.inputBackupFrame.setVisible(false);
    }
    else if (action.equals(GuiCommands.COMMAND_MONITOR_CURRENT_CONTROLLER))
    {
      gui.publicActionStartMonitor(gui.getSelectedController(), true, true,
          true);
    }

  }

  /**
   * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
   */
  public void focusGained(FocusEvent e)
  {
    gui.publicActionTileJmxFrames(false);
    gui.publicActionRefreshMBeans();
  }

  /**
   * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
   */
  public void focusLost(FocusEvent e)
  {

  }

  /**
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  public void valueChanged(ListSelectionEvent e)
  {
    JList list = (JList) e.getSource();
    ObjectName name = (ObjectName) list.getSelectedValue();
    gui.publicActionRefreshMBeanAttributes(name);
    gui.publicActionRefreshMBeanMethods(name);
  }
}