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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.filechooser.FileFilter;

import org.objectweb.cjdbc.common.exceptions.CJDBCException;
import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.i18n.GuiTranslate;
import org.objectweb.cjdbc.common.jmx.JmxException;
import org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean;
import org.objectweb.cjdbc.common.jmx.mbeans.DataCollectorMBean;
import org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.common.jmx.notifications.CjdbcNotificationList;
import org.objectweb.cjdbc.common.shared.BackendInfo;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.common.util.Strings;
import org.objectweb.cjdbc.common.xml.XmlTools;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.constants.GuiIcons;
import org.objectweb.cjdbc.console.gui.dnd.listeners.BackendTransferListener;
import org.objectweb.cjdbc.console.gui.dnd.listeners.ControllerTransferListener;
import org.objectweb.cjdbc.console.gui.frames.GuiExceptionFrame;
import org.objectweb.cjdbc.console.gui.frames.GuiInputBackupFrame;
import org.objectweb.cjdbc.console.gui.frames.GuiNewControllerFrame;
import org.objectweb.cjdbc.console.gui.frames.GuiSelectCheckpointFrame;
import org.objectweb.cjdbc.console.gui.frames.GuiSelectShutdownFrame;
import org.objectweb.cjdbc.console.gui.frames.GuiVirtualDatabaseLoginFrame;
import org.objectweb.cjdbc.console.gui.frames.NewBackendFrame;
import org.objectweb.cjdbc.console.gui.frames.jmxdesktop.AttributeChangeDialog;
import org.objectweb.cjdbc.console.gui.frames.jmxdesktop.OperationCallDialog;
import org.objectweb.cjdbc.console.gui.jtools.JMultiLineToolTip;
import org.objectweb.cjdbc.console.gui.jtools.JTextAreaWriter;
import org.objectweb.cjdbc.console.gui.model.AttributeModel;
import org.objectweb.cjdbc.console.gui.model.JNewList;
import org.objectweb.cjdbc.console.gui.model.OperationModel;
import org.objectweb.cjdbc.console.gui.objects.BackendObject;
import org.objectweb.cjdbc.console.gui.objects.ConfigurationFileObject;
import org.objectweb.cjdbc.console.gui.objects.ControllerObject;
import org.objectweb.cjdbc.console.gui.objects.DatabaseObject;
import org.objectweb.cjdbc.console.gui.objects.DumpFileObject;
import org.objectweb.cjdbc.console.gui.objects.tooltips.BackendToolTip;
import org.objectweb.cjdbc.console.gui.popups.ConfigurationFilePopUpMenu;
import org.objectweb.cjdbc.console.gui.popups.ControllerListPopUpMenu;
import org.objectweb.cjdbc.console.gui.popups.ControllerPopUpMenu;
import org.objectweb.cjdbc.console.gui.popups.DatabasePopUpMenu;
import org.objectweb.cjdbc.console.gui.popups.DumpPopUpMenu;
import org.objectweb.cjdbc.console.gui.popups.LogEditPopUpMenu;
import org.objectweb.cjdbc.console.gui.session.GuiSession;
import org.objectweb.cjdbc.console.gui.threads.GuiLoggingThread;
import org.objectweb.cjdbc.console.gui.threads.GuiParsingThread;
import org.objectweb.cjdbc.console.gui.threads.task.BackupBackendTask;
import org.objectweb.cjdbc.console.gui.threads.task.RestoreBackendTask;
import org.objectweb.cjdbc.console.jmx.RmiJmxClient;
import org.objectweb.cjdbc.console.monitoring.MonitoringConsole;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.views.CacheStatsViewer;
import org.objectweb.cjdbc.console.views.CacheViewer;
import org.objectweb.cjdbc.console.views.RecoveryLogViewer;
import org.objectweb.cjdbc.console.views.SQLStatViewer;

/**
 * This class is the main class for the graphical version of the C-JDBC
 * administration console.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class CjdbcGui extends JFrame
{
  // /////////////////////////////////////////////////////////////
  // DATA CONTAINERS
  // /////////////////////////////////////////////////////////////
  /** Stored values for session */
  GuiSession                   guiSession;
  /** Virtual database MBeans [DatabaseName] / [VirtualDatabaseJmxClient] */
  Hashtable                    databaseMBeans;
  /** Controller MBeans [URL] / [ControllerJmxClient] */
  Hashtable                    controllerMBeans;
  /** Hastable of backends states: [StateName] / [Panel] */
  Hashtable                    backendsState;
  /** List of backends [backendName] : [BackendObject] */
  Hashtable                    backendList;
  /** List of databases [databaseName] : [DatabaseObject] */
  Hashtable                    databaseList;
  /** List of controllers [controllerName] : [ControllerObject] */
  Hashtable                    controllerList;

  /** List of jmx clients [url] : [RmiJmxClient] */
  Hashtable                    jmxClients;
  RmiJmxClient                 currentJmxClient;
  String                       selectedController;
  String                       selectedDatabase;

  Object                       credentials = null;

  JNewList                     mbeanList;

  // /////////////////////////////////////////////////////////////
  // THREADS
  // /////////////////////////////////////////////////////////////
  /** The thread that connects to the log4j server on a host */
  GuiLoggingThread             logginThread;

  // /////////////////////////////////////////////////////////////
  // MIXED CONTAINERS
  // /////////////////////////////////////////////////////////////
  /** The panel for the list of databases */
  JPanel                       vdbListPanel;
  /** The list containing the xml file NAMES, no paths */
  JPanel                       fileListPanel;
  /** Panel for controller list */
  JPanel                       controllerListPanel;

  // /////////////////////////////////////////////////////////////
  // FLOATING FRAMES
  // /////////////////////////////////////////////////////////////
  /** Login frame */
  GuiVirtualDatabaseLoginFrame loginFrame;
  /** New controller frame objects */
  GuiNewControllerFrame        newControllerFrame;
  /** Exception frame for reporting errors from the server */
  GuiExceptionFrame            exceptionFrame;
  /** New Backend frame object */
  NewBackendFrame              newBackendFrame;
  /** Select checkpoint frame */
  GuiSelectCheckpointFrame     selectCheckpointFrame;
  /** Select shutdown frame */
  GuiSelectShutdownFrame       selectShutdownFrame;
  /** Select backup frame */
  GuiInputBackupFrame          inputBackupFrame;

  // /////////////////////////////////////////////////////////////
  // LISTENERS
  // /////////////////////////////////////////////////////////////
  /** Backend transfer listener used for drag and drop */
  BackendTransferListener      backendTransferListener;
  /** Configuration File transfer listener used for drag and drop */
  ControllerTransferListener   configurationFileTransferListener;

  /** GuiAction listener */
  CjdbcGuiListener             guiActionListener;

  // /////////////////////////////////////////////////////////////
  // GRAPHIC CONTAINERS
  // /////////////////////////////////////////////////////////////

  /** The texts of the debug,logging panel */
  String                       debugText, loggingText;
  /** The debug panel itself */
  JTextArea                    debugTextPane, loggingTextPane;
  /** The log panel itself */
  JTextPane                    infoTextPane;
  /** The thread that parse the xml panel */
  GuiParsingThread             parsingThread;
  /** The xml editor panel */
  JTextPane                    xmlTextPane;
  /** the file chooser for configuration files */
  JFileChooser                 configurationFileChooser;
  /** the file chooser for jar files */
  JFileChooser                 jarFileChooser;
  /** the save file chooser for files */
  JFileChooser                 saveFileChooser;
  /** Backend pane */
  JPanel                       backendPanel;
  /** The controller list mouse listener */
  ControllerListPopUpMenu      controllerListPopUpMenu;
  /** The center Pane */
  JTabbedPane                  centerPane;
  /** Scroll panel for selection in center pane */
  JScrollPane                  helpScroll, debugScroll, xmlScroll,
      loggingScroll, infoScroll, logConfigScroll;
  /** Scroll panels from the left pane */
  JScrollPane                  fileScroll;
  /** The panel for exceptions tracing */
  JTextArea                    debugTraceTextPane;
  /** Writer to the trace exception area in the backend panel */
  JTextAreaWriter              traceWriter;
  /** The panel for the log4j configuration */
  JTextPane                    logConfigTextPane;
  /** Panels for the backend panel */
  JPanel                       backendButtons, backendIcons;

  JDesktopPane                 jmxPanel;
  JScrollPane                  jmxScroll;
  JScrollPane                  attributePane;
  JTable                       attributeTable;
  JTable                       operationTable;
  JScrollPane                  operationPane;

  JInternalFrame               mbeanFrame;
  JInternalFrame               attributeFrame;
  JInternalFrame               operationFrame;

  /**
   * Creates a new <code>CjdbcGui.java</code> object
   */
  public CjdbcGui()
  {
    super(GuiTranslate.get("gui.name"));
    new Thread(new CjdbcGuiLoader(this)).start();
  }

  private ControllerMBean actionGetControllerBean(String controllerName)
  {
    if (controllerMBeans.containsKey(controllerName))
    {
      currentJmxClient = (RmiJmxClient) jmxClients.get(controllerName);
      if (currentJmxClient.isValidConnection())
      {
        ControllerMBean controllerMBean = (ControllerMBean) controllerMBeans
            .get(controllerName);
        ((ControllerObject) controllerList.get(controllerName))
            .setIcon(GuiIcons.CONTROLLER_READY);
        return controllerMBean;
      }
      else
      {
        appendDebugText("Controller :" + controllerName
            + " cannot be accessed anymore. Trying to reconnect...");
        controllerMBeans.remove(controllerName);
        return actionGetControllerBean(controllerName);
      }
    }
    else
    {
      try
      {
        RmiJmxClient jmxClient = new RmiJmxClient(controllerName, credentials);
        appendDebugText("Setting listener for controller:" + controllerName);
        jmxClient.setNotificationListener(guiActionListener);
        jmxClients.put(controllerName, jmxClient);
        controllerMBeans.put(controllerName, jmxClient.getControllerProxy());
        if (selectedController == null
            || controllerName.equalsIgnoreCase(selectedController))
          currentJmxClient = jmxClient;

        appendDebugText("Connected to: " + controllerName);
        actionStartControllerLoggingThread(controllerName);

        ControllerObject co = ((ControllerObject) controllerList
            .get(controllerName));
        if (co != null)
          co.setIcon(GuiIcons.CONTROLLER_READY);
        return jmxClient.getControllerProxy();
      }
      catch (Exception e)
      {
        appendDebugText("Failed to connect to: " + controllerName);
        ControllerObject co = ((ControllerObject) controllerList
            .get(controllerName));
        if (co != null)
          co.setIcon(GuiIcons.CONTROLLER_DOWN);
        return null;
      }
    }
  }

  private void actionStartControllerLoggingThread(String controllerName)
  {
    try
    {
      RmiJmxClient client = (RmiJmxClient) jmxClients.get(controllerName);
      String ip = client.getRemoteHostAddress();

      if (logginThread != null)
        logginThread.quit();
      logginThread = new GuiLoggingThread(loggingTextPane, ip);
      logginThread.start();
      appendDebugText("Log4j logging thread started for: <" + controllerName
          + ">");
    }
    catch (Exception e1)
    {
      e1.printStackTrace();
      appendDebugText("Could not start log4j logging thread to: <"
          + controllerName + ">");
    }
  }

  private VirtualDatabaseMBean actionGetDatabaseBean(String databaseName)
  {
    if (databaseMBeans.containsKey(databaseName))
    {
      return (VirtualDatabaseMBean) databaseMBeans.get(databaseName);
    }
    else
    {
      appendDebugText("Login to database:" + databaseName);
      DatabaseObject dob = (DatabaseObject) databaseList.get(databaseName);
      if (dob == null)
      {
        appendDebugText("Failed to retrieve " + databaseName
            + " from internal list");
        return null;
      }
      loginFrame = new GuiVirtualDatabaseLoginFrame(this, guiActionListener,
          databaseName, dob.getIpAdress(), dob.getPort(), guiSession);
      loginFrame.setVisible(true);
      return null;
    }
  }

  private void actionLoadDatabaseList(String controllerName)
  {
    // /////////////////////////////////////////////////////////////////////////
    // Load list of database
    // /////////////////////////////////////////////////////////////////////////
    ControllerMBean controllerMBean = actionGetControllerBean(controllerName);
    ArrayList databases;
    try
    {
      vdbListPanel.removeAll();
      databases = controllerMBean.getVirtualDatabaseNames();
      int size = databases.size();
      appendDebugText("Loading virtual databases list...");
      for (int i = 0; i < size; i++)
        actionLoadDatabase((String) databases.get(i), controllerName);
    }
    catch (Exception e1)
    {
      e1.printStackTrace();
      appendDebugText("Cannot load virtual database list (" + e1.getMessage()
          + ")");
      vdbListPanel.removeAll();
    }
    paintDatabasePanel();
    repaint();
  }

  void actionUnloadBackends(String controller)
  {
    appendDebugText("Unloading backends from controller:" + controller);
    Enumeration enume;
    BackendObject bo;
    String name;
    while ((enume = backendList.keys()).hasMoreElements())
    {
      name = (String) enume.nextElement();
      bo = (BackendObject) backendList.get(name);
      bo.setVisible(false);
      backendList.remove(name);
      bo = null;
    }
  }

  void paintDatabasePanel()
  {
    vdbListPanel.validate();
    vdbListPanel.repaint();
    validate();
    repaint();
  }

  private void actionLoadDatabase(String databaseName, String controllerName)
  {
    // /////////////////////////////////////////////////////////////////////////
    // Load graphic object for a virtual database
    // /////////////////////////////////////////////////////////////////////////
    DatabaseObject dob = new DatabaseObject(databaseName, controllerName, false);
    dob.setActionCommand(GuiCommands.COMMAND_SELECT_DATABASE);
    dob.addActionListener(guiActionListener);
    dob.addMouseListener(new DatabasePopUpMenu(this, dob));
    databaseList.put(databaseName, dob);
    // We need the object to be in the list before calling getDatabaseBean
    VirtualDatabaseMBean virtualDatabaseMBean = actionGetDatabaseBean(databaseName);
    if (virtualDatabaseMBean != null)
      dob.setDistributed(virtualDatabaseMBean.isDistributed());
    actionAddObjectToGridLayout(vdbListPanel, dob);
    appendDebugText("Loaded:" + databaseName + " for controller:"
        + controllerName);
  }

  private void actionAddObjectToGridLayout(JPanel panel, JButton button)
  {
    GridLayout layout = (GridLayout) panel.getLayout();
    int pcount = panel.getComponentCount();
    layout.setRows(pcount + 1);
    panel.add(button);
    panel.validate();
    panel.repaint();
  }

  /**
   * Load the list of backends for the given database
   * 
   * @param databaseName the database to load the backends from
   */
  public void publicActionLoadBackendsList(String databaseName)
  {
    try
    {
      VirtualDatabaseMBean databaseMBean = actionGetDatabaseBean(databaseName);
      // ArrayList list = actionLoadCheckpointNames(databaseName);
      ArrayList backends = databaseMBean.getAllBackendNames();
      appendDebugText("Local Backend list for controller("
          + currentJmxClient.getRemoteName() + ") is:" + backends);
      for (int i = 0; i < backends.size(); i++)
        actionLoadBackend(databaseName, (String) backends.get(i),
            currentJmxClient.getRemoteName(), true);

      if (databaseMBean.isDistributed())
      {
        databaseMBean = actionGetDatabaseBean(databaseName);
        Hashtable map;
        try
        {
          map = databaseMBean.viewGroupBackends();
          Enumeration enumeration = map.keys();
          while (enumeration.hasMoreElements())
          {
            String controllerName = (String) enumeration.nextElement();
            ArrayList list = (ArrayList) map.get(controllerName);
            for (int i = 0; i < list.size(); i++)
            {
              BackendInfo info = (BackendInfo) list.get(i);
              String backendName = info.getName();
              actionLoadBackend(databaseName, backendName, controllerName,
                  false);
            }
          }
        }
        catch (RuntimeException e1)
        {
          appendDebugText(
              "Runtime exception while loading distributed database:"
                  + databaseName, e1);
        }

      }
      paintBackendPane();
    }
    catch (Exception e)
    {
      appendDebugText("Could not retrieve backend list for database"
          + databaseName, e);
    }
  }

  /**
   * Create a new database backend object and load its state
   * 
   * @param database the virtual database name
   * @param backendName the backend name
   * @param controllerName the controller that owns this backend
   * @param enable if the backend object should be enabled. (not used?)
   */
  public void actionLoadBackend(String database, String backendName,
      String controllerName, boolean enable)
  {
    BackendObject backend = null;
    if (!backendList.containsKey(backendName))
    {
      appendDebugText("Loading backend:" + backendName + " from controller:"
          + controllerName);
      try
      {
        backend = new BackendObject(this, backendTransferListener, database,
            backendName, controllerName)
        {
          public JToolTip createToolTip()
          {
            return new JMultiLineToolTip();
          }
        };
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      backend.setBorder(BorderFactory.createTitledBorder(
          GuiConstants.LOWERED_BORDER, controllerName));
      backend.setControllerName(controllerName);
      backendList.put(backendName, backend);
    }
    else
    {
      backend = (BackendObject) backendList.get(backendName);
      backend.setBorder(BorderFactory.createTitledBorder(
          GuiConstants.LOWERED_BORDER, controllerName));
      backend.setControllerName(controllerName);
      backend.setEnabled(controllerName.equals(selectedController));
      appendDebugText("ReLoading backend:" + backendName);
    }

    try
    {
      actionSetBackendState(backendName);
    }
    catch (RuntimeException e)
    {
      appendDebugText("cannot access mbean anymore");
      publicActionRemoveBackendFromGui(backendName);
    }
  }

  /**
   * Converts a JMX state to a Gui constants values
   * 
   * @param jmxState the JMX state to convert
   * @return the GUI constant value or null if not found
   * @see GuiConstants
   */
  private String actionConvertState(String jmxState)
  {
    if (jmxState == null)
      return null;

    appendDebugText("Converting new jmx state:" + jmxState);

    if (jmxState
        .equalsIgnoreCase(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_ENABLED))
      return GuiConstants.BACKEND_STATE_ENABLED;
    else if (jmxState
        .equalsIgnoreCase(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_ENABLED_WRITE))
      return GuiConstants.BACKEND_STATE_ENABLED;
    else if (jmxState
        .equalsIgnoreCase(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_DISABLED))
      return GuiConstants.BACKEND_STATE_DISABLED;
    else if (jmxState
        .equalsIgnoreCase(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_DISABLING))
      return GuiConstants.BACKEND_STATE_DISABLED;
    else if (jmxState
        .equalsIgnoreCase(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_BACKINGUP))
      return GuiConstants.BACKEND_STATE_BACKUP;
    else if (jmxState
        .equalsIgnoreCase(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_RECOVERING))
      return GuiConstants.BACKEND_STATE_RESTORE;
    else if (jmxState
        .equalsIgnoreCase(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_REPLAYING))
      return GuiConstants.BACKEND_STATE_RECOVERY;
    else if (jmxState
        .equalsIgnoreCase(CjdbcNotificationList.VIRTUALDATABASE_BACKEND_UNKNOWN))
      return GuiConstants.BACKEND_STATE_DISABLED;
    else
      return null;
  }

  /**
   * Change the state of a backend object Checks the new state is valid, and
   * then display the backend object into the new panel. Removes it from the old
   * panel as well. Calls repaint of each panel. This is not connected to the
   * actual database the backend object is referenced from because many states
   * do not exist on the controller
   * 
   * @param bo the backend object
   * @param newState new state to assign to the backend
   */
  public void actionChangeBackendState(BackendObject bo, String newState)
  {
    // test if the new state is valid
    if (!GuiConstants.isValidBackendState(newState))
      return;

    // State is valid, process

    // Removed from old panel
    String oldState = bo.getState();
    JPanel oldpanel = (JPanel) backendsState.get(oldState);
    oldpanel.remove(bo);
    oldpanel.validate();
    oldpanel.repaint();

    // Change the state of the backend object
    bo.setState(newState);

    // Add it to thew new state panel
    JPanel panel = (JPanel) backendsState.get(newState);
    panel.add(bo);
    panel.validate();
    panel.repaint();
  }

  /**
   * Set the state of the given backend. The state is retrieved via a jmx call
   * on the backend corresponding MBean.
   * 
   * @param backendName the backend to set the state
   */
  public void actionSetBackendState(String backendName)
  {
    try
    {
      BackendObject bo = (BackendObject) backendList.get(backendName);
      if (bo == null)
      {
        appendDebugText("Backend (" + backendName + ") cannot be found");
        return;
      }
      String controllerName = bo.getControllerName();
      if (controllerName == null)
      {
        appendDebugText("Backend (" + backendName
            + ") has lost its controller reference.");
        appendDebugText("Removing backend (" + backendName
            + ") from display list.");
        backendList.remove(backendName);
        return;
      }
      if (selectedController.equals(controllerName))
        bo.setEnabled(true);
      else
        bo.setEnabled(false);
      String state = bo.getState();
      appendDebugText("STATE:" + state);
      String login = guiSession.getAuthenticatedDatabaseLogin(bo.getDatabase());
      String password = guiSession.getAuthenticatedDatabasePassword(bo
          .getDatabase());
      RmiJmxClient client = (RmiJmxClient) jmxClients.get(controllerName);
      DatabaseBackendMBean backend = null;
      VirtualDatabaseMBean databaseMBean = null;

      try
      {
        backend = client.getDatabaseBackendProxy(bo.getDatabase(), backendName,
            login, password);
        databaseMBean = client.getVirtualDatabaseProxy(bo.getDatabase(), login,
            password);
      }
      catch (Exception e)
      {
        appendDebugText("Could not change state of backend:" + backendName,
            new JmxException("MBean connection was lost"));
      }
      String newState = actionConvertState(backend.getState());

      actionSetBackendState(backendName, newState);

      // set Tool tip after state
      try
      {
        String[] data = databaseMBean.viewBackendInformation(backendName);
        bo.setToolTipText(new BackendToolTip(data).getFormattedToolTip());
      }
      catch (Exception e)
      {
        appendDebugText("Tool tip could not be collected for backend:"
            + backendName);
      }
    }
    catch (Exception e)
    {
      appendDebugText("Could not change state of backend:" + backendName, e);
    }
  }

  /**
   * Change panel of backend
   * 
   * @param backendName name of backend
   * @param newState new state
   */
  public void actionSetBackendState(String backendName, String newState)
  {
    BackendObject bo = (BackendObject) backendList.get(backendName);
    appendDebugText("Setting backend(" + backendName + "):" + bo.getName()
        + " to state:" + newState);

    String state = bo.getState();
    if (state == null)
    {
      // state has not been inited
    }
    else
    {
      // remove previous state
      JPanel panel = (JPanel) backendsState.get(state);
      panel.remove(bo);
      panel.validate();
      panel.repaint();
    }

    // Set new state
    JPanel panel = (JPanel) backendsState.get(newState);
    bo.setState(newState);

    panel.add(bo);
    panel.validate();
    panel.repaint();

  }

  /**
   * Validate and Repaint the backend split
   */
  public void paintBackendPane()
  {
    // ////////////////////////////////////////////////////////////////////////
    // Paint backends panel
    // /////////////////////////////////////////////////////////////////////////
    backendPanel.validate();
    backendPanel.repaint();
  }

  void paintConfigurationPane()
  {
    // ////////////////////////////////////////////////////////////////////////
    // Paint configuration file panel
    // /////////////////////////////////////////////////////////////////////////
    fileScroll.setVisible(true);
    fileListPanel.setVisible(true);
    fileScroll.validate();
    fileListPanel.validate();
    fileScroll.repaint();
  }

  String actionLoadXmlText(File filePath)
  {
    // /////////////////////////////////////////////////////////////////////////
    // Load xml file content
    // /////////////////////////////////////////////////////////////////////////
    try
    {
      if (filePath == null || !filePath.exists())
      {
        return "";
      }
      StringBuffer buffer = new StringBuffer();
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      String line = "";
      while ((line = reader.readLine()) != null)
      {
        buffer.append(line + System.getProperty("line.separator"));
      }
      reader.close();
      return buffer.toString();
    }
    catch (Exception e)
    {
      appendDebugText("Error while reading from file:" + e.getMessage());
      return "";
    }
  }

  /**
   * Append debug text to the debug panel
   * 
   * @param text to append
   */
  public void appendDebugText(String text)
  {
    debugText += text + System.getProperty("line.separator");
    debugTextPane.setText(debugText);
  }

  /**
   * Same as above and displays the stack trace ...
   * 
   * @param text text to display
   * @param e the exception to get the trace from
   */
  public void appendDebugText(String text, Exception e)
  {
    try
    {
      if (GuiConstants.DEBUG_LEVEL <= GuiConstants.DEBUG_NO_EXCEPTION_WINDOW)
      {
        if (e instanceof MBeanException)
          exceptionFrame.showException(((MBeanException) e)
              .getTargetException());
        else
          exceptionFrame.showException(e);
      }
      appendDebugText(text + "[Message:" + e.getMessage() + "]");
      traceWriter.write("---- Exception ----\n");
      e.printStackTrace(new PrintWriter(traceWriter));
      traceWriter.flush();
    }
    catch (IOException e1)
    {
      // ignore . . .
    }
  }

  /**
   * Show the file chooser to add an xml file to the configuration list
   */
  public void publicActionAddXmlFile()
  {
    if (configurationFileChooser == null)
    {
      configurationFileChooser = new JFileChooser(".");
      configurationFileChooser.setFileFilter(new FileFilter()
      {
        /**
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f)
        {
          if (f.getAbsolutePath().endsWith(".xml") || f.isDirectory())
            return true;
          else
            return false;
        }

        /**
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        public String getDescription()
        {
          return "Xml Files";
        }
      });
    }
    configurationFileChooser.showOpenDialog(this);
    File selFile = configurationFileChooser.getSelectedFile();
    if (selFile != null)
    {
      appendDebugText("Selected new file:" + selFile.getAbsolutePath());
      guiSession.addFileToConfigurationFiles(selFile);
      ConfigurationFileObject cfo = new ConfigurationFileObject(
          configurationFileTransferListener, selFile);
      ConfigurationFilePopUpMenu cfpum = new ConfigurationFilePopUpMenu(this,
          cfo);
      cfo.addActionListener(cfpum);
      cfo.addMouseListener(cfpum);
      actionAddObjectToGridLayout(fileListPanel, cfo);
      paintConfigurationPane();
    }
  }

  void actionLoadXmlList()
  {
    if (fileListPanel != null)
    {
      ArrayList vfileItems = guiSession.getConfigurationFiles();
      int vsize = vfileItems.size();
      ConfigurationFileObject cfo;
      for (int i = 0; i < vsize; i++)
      {
        cfo = new ConfigurationFileObject(configurationFileTransferListener,
            (File) vfileItems.get(i));
        ConfigurationFilePopUpMenu cfpum = new ConfigurationFilePopUpMenu(this,
            cfo);
        cfo.addActionListener(cfpum);
        cfo.addMouseListener(cfpum);
        fileListPanel.add(cfo);
        actionAddObjectToGridLayout(fileListPanel, cfo);
      }
      // paintConfigurationPane();
    }
  }

  private ArrayList actionLoadCheckpointNames(String databaseName)
  {
    ArrayList list = actionGetDatabaseBean(databaseName).viewCheckpointNames();
    for (int i = 0; i < list.size(); i++)
      appendDebugText("Found checkpoint:" + list.get(i) + " for database:"
          + databaseName);
    return list;
  }

  /**
   * Load the controller list. Removes all the controller icons from the panel.
   * and get all the controllers from the session hashtable Then calls
   * <code>actionLoadController</code>
   */
  public void publicActionLoadControllerList()
  {
    if (controllerListPanel != null)
    {
      controllerListPanel.removeAll();
      ArrayList controllerItems = guiSession.getControllerItems();
      int size = controllerItems.size();
      for (int i = 0; i < size; i++)
        actionLoadController((String) controllerItems.get(i));
      paintControllerPane();
    }
  }

  /**
   * Paint the controller panel
   */
  void paintControllerPane()
  {
    controllerListPanel.validate();
    controllerListPanel.repaint();
  }

  /**
   * View the controller frame to add a new controller reference to the list
   */
  public void publicActionAddControllerView()
  {
    newControllerFrame.setVisible(true);
  }

  /**
   * actionDatabaseEnableAll definition. Call the proper MBean to enable all
   * backends
   * 
   * @param databaseName the name of the database to operate
   */
  public void publicActionDatabaseEnableAll(String databaseName)
  {
    try
    {
      actionGetDatabaseBean(databaseName).enableAllBackends();
      publicActionLoadBackendsList(databaseName);
      centerPane.setSelectedComponent(backendPanel);
      appendDebugText("Enabled All backends for :" + databaseName
          + " was a success");
    }
    catch (Exception e)
    {
      appendDebugText("Enabled All backends for :" + databaseName + " failed",
          e);
    }
  }

  /**
   * Load a panel for the backends
   * 
   * @param name the name of the panel
   */
  public void actionLoadBackendActionButton(String name)
  {// ////////////////////////////////////////////////////////////////////////
    // Define backend action buttons
    // /////////////////////////////////////////////////////////////////////////
    appendDebugText("Loading backend action panel:" + name);
    Color color = GuiConstants.getBackendBgColor(name);

    JPanel paneContent = new JPanel(new FlowLayout());
    paneContent.setBackground(color);
    paneContent.setName(name);
    paneContent.setVisible(true);

    JButton paneLabel = new JButton(name);
    paneLabel.setActionCommand(name);
    paneLabel.setBackground(color);
    paneLabel.setSize(100, 50);
    paneLabel.setVisible(true);

    DropTarget target1 = new DropTarget(paneLabel, DnDConstants.ACTION_MOVE,
        backendTransferListener);
    target1.setActive(true);

    DropTarget target2 = new DropTarget(paneContent, DnDConstants.ACTION_MOVE,
        backendTransferListener);
    target2.setActive(true);

    GridLayout iconLayout = (GridLayout) backendIcons.getLayout();
    iconLayout.setColumns(iconLayout.getColumns() + 1);
    GridLayout buttonsLayout = (GridLayout) backendButtons.getLayout();
    buttonsLayout.setColumns(buttonsLayout.getColumns() + 1);
    backendIcons.add(paneContent);
    backendButtons.add(paneLabel);
    // Adding to the main hashtable of states
    backendsState.put(name, paneContent);
  }

  /**
   * Load standard version for the panel
   * 
   * @param recoveryEnabled more options are available when recovery log is
   *          enabled for the database
   */
  public void actionLoadBackendPane(boolean recoveryEnabled)
  {
    // ////////////////////////////////////////////////////////////////////////
    // Define backend panel
    // /////////////////////////////////////////////////////////////////////////
    if (backendPanel == null)
    {
      backendPanel = new JPanel(new BorderLayout());
      backendPanel.setEnabled(true);
      backendPanel.setBackground(Color.white);
      backendPanel.setVisible(true);
    }
    else
    {
      backendPanel.remove(backendButtons);
      backendPanel.remove(backendIcons);
    }

    backendButtons = new JPanel(new GridLayout(1, 0));
    backendIcons = new JPanel(new GridLayout(1, 0));
    backendIcons.setBackground(Color.white);
    backendIcons.setVisible(true);
    backendButtons.setVisible(true);

    actionLoadBackendActionButton(GuiConstants.BACKEND_STATE_ENABLED);
    if (recoveryEnabled)
      actionLoadBackendActionButton(GuiConstants.BACKEND_STATE_RECOVERY);
    actionLoadBackendActionButton(GuiConstants.BACKEND_STATE_DISABLED);
    if (recoveryEnabled)
      actionLoadBackendActionButton(GuiConstants.BACKEND_STATE_BACKUP);
    if (recoveryEnabled)
      actionLoadBackendActionButton(GuiConstants.BACKEND_STATE_RESTORE);
    backendPanel.add(backendIcons, BorderLayout.CENTER);
    backendPanel.add(backendButtons, BorderLayout.NORTH);

    centerPane.validate();
    centerPane.repaint();
    validate();
    repaint();
  }

  /**
   * Load the virtual database after authentication.
   */
  public void publicActionLoadAuthenticatedDatabase()
  {
    loginFrame.setVisible(false);
    try
    {
      String databaseName = loginFrame.getDatabaseName();
      String login = loginFrame.getLoginBox().getText().trim();
      String password = loginFrame.getPasswordBox().getText().trim();

      VirtualDatabaseMBean mbean = null;
      try
      {
        mbean = currentJmxClient.getVirtualDatabaseProxy(databaseName, login,
            password);
        if (mbean == null)
          throw new IOException("MBean connection lost");
      }
      catch (IOException ioe)
      {
        appendDebugText(ioe.getMessage());
        actionUnloadBackends(currentJmxClient.getRemoteName());
        return;
      }

      if (!mbean.checkAdminAuthentication(login, password))
      {
        throw new Exception("Authentication failed");
      }

      databaseMBeans.put(databaseName, mbean);
      guiSession.addDatabaseToSession(databaseName, login, password);

      String[] list = mbean.viewControllerList();
      for (int i = 0; i < list.length; i++)
      {
        appendDebugText("Found controllerL" + list[i]);
        if (!guiSession.checkControllerInSession(list[i]))
        {
          actionLoadController(list[i]);
          RmiJmxClient client = (RmiJmxClient) jmxClients.get(list[i]);
          // get the listeners on the virtual database
          client.getVirtualDatabaseProxy(databaseName, login, password);
          paintControllerPane();
        }
      }

      boolean recoveryExist = mbean.hasRecoveryLog();
      appendDebugText("RecoveryLog is defined for this database:"
          + recoveryExist);
      actionLoadBackendPane(recoveryExist);

      publicActionLoadBackendsList(databaseName);
      actionLoadCheckpointNames(databaseName);
      publicActionLoadDumpList(databaseName);
    }
    catch (Exception e1)
    {
      appendDebugText("Could not connect to database", e1);
    }
  }

  /**
   * Quit the GUI
   */
  public void publicActionQuit()
  {
    try
    {
      guiSession.saveSessionToFile(new File(
          GuiConstants.CJDBC_DEFAULT_SESSION_FILE));
    }
    catch (IOException e)
    {
      System.out.println("Could not save session");
    }
    System.exit(0);
  }

  /**
   * Add a new driver to the controller. Start the file chooser and use the
   * proper MBean
   * 
   * @param controllerName name of the controller
   */
  public void publicActionLoadDriver(String controllerName)
  {
    if (jarFileChooser == null)
    {
      jarFileChooser = new JFileChooser(".");
      jarFileChooser.setFileFilter(new FileFilter()
      {
        /**
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f)
        {
          if (f.getAbsolutePath().endsWith(".jar") || f.isDirectory())
            return true;
          else
            return false;
        }

        /**
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        public String getDescription()
        {
          return "Jar Files";
        }
      });
    }
    jarFileChooser.showOpenDialog(this);
    File selFile = jarFileChooser.getSelectedFile();
    try
    {
      if (selFile != null)
      {
        ControllerMBean controllerMBean = actionGetControllerBean(controllerName);
        controllerMBean.addDriver(readDriver(selFile.getAbsolutePath()));
      }
    }
    catch (Exception e)
    {
      appendDebugText("Could not load driver from jar file:"
          + selFile.getName());
    }
  }

  private byte[] readDriver(String filename) throws FileNotFoundException,
      IOException
  {
    File file;
    FileInputStream fileInput = null;
    file = new File(filename);
    fileInput = new FileInputStream(file);

    // Read the file into an array of bytes
    long size = file.length();
    if (size > Integer.MAX_VALUE)
      throw new IOException(ConsoleTranslate
          .get("controller.command.adddriver.file.too.big"));
    byte[] bytes = new byte[(int) size];
    int nb = fileInput.read(bytes);
    fileInput.close();
    if (nb != size)
      throw new IOException(ConsoleTranslate
          .get("controller.command.adddriver.file.not.read"));
    return bytes;
  }

  /**
   * Fetch the logs from the controller
   * 
   * @param controllerName the name of the controller
   */
  public void publicActionRefreshLogs(String controllerName)
  {
    try
    {
      ControllerMBean controllerMBean = actionGetControllerBean(controllerName);
      loggingTextPane.setText(controllerMBean.generateLogReport());
      appendDebugText("Re-Fetched logs for controller:" + controllerName);
    }
    catch (Exception e)
    {
      appendDebugText("Fail to fetch logs for controller:" + controllerName, e);
    }
  }

  /**
   * Clean the debug buffer
   */
  public void publicActionCleanDebugBuffer()
  {
    debugText = "";
    debugTextPane.setText("");
  }

  /**
   * saveConfigurationFile definition. Starts the JFile Chooser and write the
   * xml content to the selected file
   */
  public void publicActionSaveConfigurationFile()
  {
    try
    {
      if (saveFileChooser == null)
      {
        saveFileChooser = new JFileChooser();
        saveFileChooser.showSaveDialog(this);
        File selected = saveFileChooser.getSelectedFile();
        if (selected != null)
        {
          BufferedWriter writer = new BufferedWriter(new FileWriter(selected));
          writer.write(xmlTextPane.getText());
          writer.close();
        }
        else
        {
          appendDebugText("Did not select a file was saving...");
        }
      }
    }
    catch (Exception e)
    {
      appendDebugText("Error while writing to file:" + e.getMessage());
    }
  }

  /**
   * add a Controller to the list.
   */
  public void publicActionAddController()
  {
    newControllerFrame.setVisible(false);
    String ipAddress = newControllerFrame.getIpAddressBox().getText().trim();
    String port = newControllerFrame.getPortNumber().getText().trim();
    appendDebugText("Add controller with ip:" + ipAddress + " amd port:" + port);
    String name = ipAddress + ":" + port;
    if (guiSession.checkControllerInSession(name))
    {
      String message = GuiTranslate.get("error.controller.already.in.session");
      CJDBCException e = new CJDBCException(message);
      appendDebugText(message, e);
    }
    else
    {
      actionLoadController(name);
      paintControllerPane();
    }
  }

  /**
   * Load a new controller, the graphic object, the connection state and add it
   * to the controller pane list.
   * 
   * @param name the name(url) of the controller
   */
  private void actionLoadController(String name)
  {
    guiSession.addControllerToList(name);
    ControllerObject co = new ControllerObject(name);
    co.setActionCommand(GuiCommands.COMMAND_SELECT_CONTROLLER);
    co.addActionListener(guiActionListener);
    co.addMouseListener(new ControllerPopUpMenu(this, co));
    ControllerMBean controllerMBean = actionGetControllerBean(name);
    if (controllerMBean == null)
    {
      appendDebugText("Cannot load controller:" + name);
      return;
    }
    DropTarget target = new DropTarget(co, DnDConstants.ACTION_MOVE,
        configurationFileTransferListener);
    co.setDropTarget(target);
    appendDebugText("Loading controller:" + controllerMBean.getJmxName());
    if (currentJmxClient.isValidConnection())
    {
      co.setState(GuiConstants.CONTROLLER_STATE_UP);
    }
    else
    {
      co.setState(GuiConstants.CONTROLLER_STATE_DOWN);
    }
    actionAddObjectToGridLayout(controllerListPanel, co);
    controllerList.put(name, co);
  }

  /**
   * Action when a new controller has been selected
   * 
   * @param connectUrl the url of the controller that was selected
   */
  public void publicActionSelectNewController(String connectUrl)
  {
    Enumeration enume = controllerList.keys();
    String key = "";
    ControllerObject controller;
    while (enume.hasMoreElements())
    {
      key = (String) enume.nextElement();
      controller = (ControllerObject) controllerList.get(key);
      if (key.equalsIgnoreCase(connectUrl))
      {
        controller.setBorder(GuiConstants.TITLED_BORDER);
        controller.setBorderPainted(true);
      }
      else
        controller.setBorderPainted(false);
    }
    actionGetControllerBean(connectUrl);
    selectedController = connectUrl;
    actionLoadDatabaseList(connectUrl);
    publicActionLoadXmlController(connectUrl);

    if (selectedDatabase != null)
      publicActionLoadAuthenticatedDatabase();

    actionStartControllerLoggingThread(connectUrl);
  }

  /**
   * Returns the selectedController value.
   * 
   * @return Returns the selectedController.
   */
  public String getSelectedController()
  {
    return selectedController;
  }

  /**
   * Loads the graphic dump list for this databaser
   * 
   * @param databaseName the virtual database name
   */
  public void publicActionLoadDumpList(String databaseName)
  {
    VirtualDatabaseMBean databaseMBean = actionGetDatabaseBean(databaseName);
    try
    {
      // FIXME Recovery Refactoring: getAvailableDumpFiles() -> getAvailableDumps()
      // File[] dumps = databaseMBean.getAvailableDumpFiles();
      File[] dumps = new File[0];
        appendDebugText("Loaded dumps for virtual database:" + databaseName);
      JPanel dumpPane = (JPanel) backendsState
          .get(GuiConstants.BACKEND_STATE_RESTORE);
      dumpPane.removeAll();
      for (int i = 0; i < dumps.length; i++)
      {
        appendDebugText("Adding dump:" + dumps[i]);
        DumpFileObject dfo = new DumpFileObject(dumps[i])
        {
          public JToolTip createToolTip()
          {
            return new JMultiLineToolTip();
          }
        };
        dfo.addMouseListener(new DumpPopUpMenu(this, databaseName, dfo));
        DropTarget target = new DropTarget(dfo, DnDConstants.ACTION_MOVE,
            backendTransferListener);
        dfo.setDropTarget(target);
        dumpPane.add(dfo);
      }
      paintBackendPane();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      appendDebugText("Failed to load dumps for virtual database:"
          + databaseName + " because of:" + e.getMessage());
    }
  }

  /**
   * Load the xml for the given controller Gives the focus to the xml panel with
   * the new content
   * 
   * @param connectUrl controllerName
   */
  public void publicActionLoadXmlController(String connectUrl)
  {
    try
    {
      ControllerMBean controllerMBean = actionGetControllerBean(connectUrl);
      xmlTextPane.setText(controllerMBean.getXml());
      appendDebugText("Loaded xml configuration for controller:" + connectUrl);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to get xml configuration for controller:"
          + connectUrl);
    }
  }

  /**
   * Select a new database action
   * 
   * @param value the name of the virtual dababase
   */
  public void publicActionSelectNewDatabase(String value)
  {
    selectedDatabase = value;
    try
    {
      VirtualDatabaseMBean databaseClient = actionGetDatabaseBean(value);
      if (databaseClient != null)
      {
        databaseClient.viewControllerList();
        publicActionLoadAuthenticatedDatabase();
      }
      Enumeration enume = databaseList.keys();
      String key = "";
      DatabaseObject database;
      while (enume.hasMoreElements())
      {
        key = (String) enume.nextElement();
        database = (DatabaseObject) databaseList.get(key);
        if (key.equalsIgnoreCase(value))
        {
          database.setBorder(GuiConstants.TITLED_BORDER);
          database.setBorderPainted(true);
        }
        else
          database.setBorderPainted(false);
      }
    }
    catch (Exception e)
    {
      appendDebugText("Failed to get access to database:" + value, e);
    }
  }

  /**
   * Load the xml configuration of the given database and display the xml buffer
   * 
   * @param databaseName virtual database name to get configuration from
   */
  public void publicActionLoadXmlDatabase(String databaseName)
  {
    try
    {
      VirtualDatabaseMBean databaseMBean = actionGetDatabaseBean(databaseName);
      xmlTextPane.setText(XmlTools.prettyXml(databaseMBean.getXml()));
      appendDebugText("Loaded xml configuration for database:" + databaseName);
      centerPane.setSelectedComponent(xmlScroll);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to get xml configuration for database:"
          + databaseName, e);
    }
  }

  /**
   * Select a new configuration load the content of the xml file into the
   * xmlTextPane panel
   * 
   * @param file the path to the xml file
   */
  public void publicActionSelectNewConfigurationFile(String file)
  {
    try
    {
      xmlTextPane.setText(actionLoadXmlText(new File(file)));
      appendDebugText("Loaded xml configuration for file:" + file);
      centerPane.setSelectedComponent(xmlScroll);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to load xml for file:" + file);
    }
  }

  /**
   * Send a shutdown command to the corresponding database
   * 
   * @param dob database object representing the database to shutdown
   * @param shutdownLevel the level to apply for the shutdown
   */
  public void publicActionShutdownDatabase(DatabaseObject dob, int shutdownLevel)
  {
    try
    {
      String databaseName = dob.getName();
      VirtualDatabaseMBean vdbMBean = actionGetDatabaseBean(databaseName);
      vdbMBean.shutdown(shutdownLevel);
      controllerListPanel.remove(dob);
      guiSession.getDatabaseItems().remove(databaseName);
      String controllerName = dob.getControllerName();
      actionUnloadBackends(controllerName);
      actionLoadDatabaseList(controllerName);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to shutdown database", e);
    }
  }

  /**
   * Execute the backend drop action with this gui
   * 
   * @param backendName name of the backend
   * @param target the drop target used for the Dnd
   */
  public void publicActionExecuteBackendDrop(JButton target, String backendName)
  {

    String actionName = target.getActionCommand();
    if (actionName.equals(GuiConstants.BACKEND_STATE_RESTORE))
      publicActionRestoreBackend(backendName, ((DumpFileObject) target)
          .getDumpName());
    else
      publicActionExecuteBackendDrop(actionName, backendName);
  }

  private String actionDisplaySelectCheckpointFrame(String database)
  {
    VirtualDatabaseMBean databaseMBean = actionGetDatabaseBean(database);
    if (selectCheckpointFrame == null)
    {
      ArrayList list = databaseMBean.viewCheckpointNames();
      list.add(GuiConstants.BACKEND_NO_CHECKPOINT);
      String[] entries = (String[]) list.toArray(new String[list.size()]);
      selectCheckpointFrame = new GuiSelectCheckpointFrame(this, entries,
          guiActionListener);
      selectCheckpointFrame.setVisible(true);
    }

    JTextField field = selectCheckpointFrame.getValueField();
    selectCheckpointFrame = null;
    if (field == null)
      return null;
    String checkpoint = field.getText();
    return checkpoint;
  }

  /**
   * Check if the target column and the current state are the same
   * 
   * @param actionName the action from the DnD
   * @param backendName the name of the backend that is the target of the action
   * @return true if nothing has changed
   */
  private boolean isDropInSameColumn(String actionName, String backendName)
  {
    BackendObject bob = (BackendObject) backendList.get(backendName);
    String state = bob.getState();
    if (state.equalsIgnoreCase(actionName))
      return true;
    else
      return false;
  }

  /**
   * Execute the backend drop action with this gui
   * 
   * @param actionName action name on the backend
   * @param backendName the drop target used for the Dnd
   */
  public void publicActionExecuteBackendDrop(String actionName,
      String backendName)
  {
    appendDebugText("Got drop backend action:" + actionName + " from:"
        + backendName);

    BackendObject bob = (BackendObject) backendList.get(backendName);
    String database = bob.getDatabase();
    VirtualDatabaseMBean databaseMBean = actionGetDatabaseBean(bob
        .getDatabase());

    /*
     * This code does not do anything if
     * (actionName.equals(GuiConstants.BACKEND_STATE_RESTORE)) { ControllerMBean
     * controller = actionGetControllerBean(databaseMBean
     * .viewOwningController()); controller.listAvailableDumpFiles(); //
     * GuiSelectDumpFrame dump = new //
     * GuiSelectDumpFrame(this,,guiActionListener); }
     */

    if (isDropInSameColumn(actionName, backendName))
    {
      appendDebugText("Drop action is not relevant");
      return;
    }

    if (actionName.equals(GuiConstants.BACKEND_STATE_ENABLED))
    {
      try
      {
        DatabaseBackendMBean bean = bob.getMbean();
        String lastcheck = bean.getLastKnownCheckpoint();
        if (lastcheck != null)
        {
          actionSetBackendState(backendName,
              GuiConstants.BACKEND_STATE_RECOVERY);
          databaseMBean.enableBackendFromCheckpoint(backendName);
          return;
        }
        else
        {
          String checkpoint = actionDisplaySelectCheckpointFrame(database);
          if (checkpoint == null)
          {
            appendDebugText("Cancelling enable backend...");
            return;
          }
          appendDebugText("Using checkpoint:" + checkpoint
              + " to enable backend:" + backendName);
          if (checkpoint.equals(GuiConstants.BACKEND_NO_CHECKPOINT))
            databaseMBean.forceEnableBackend(backendName);
          else
            databaseMBean.enableBackendFromCheckpoint(backendName);
        }
        // actionSetBackendState(backendName);
      }
      catch (Exception e)
      {
        appendDebugText("Failed to enable backend", e);
      }
    }
    else if (actionName.equals(GuiConstants.BACKEND_STATE_DISABLED))
    {
      try
      {
        // No recovery log defined.
        if (!databaseMBean.hasRecoveryLog())
          databaseMBean.forceDisableBackend(backendName);
        else
        {
          String checkpoint = generateCheckpoint(backendName);
          //          
          // actionDisplaySelectCheckpointFrame(database);
          // if (checkpoint == null)
          // {
          // appendDebugText("Cancelling disable backend...");
          // return;
          // }
          appendDebugText("Using autogenerated checkpoint:" + checkpoint
              + " to disable backend:" + backendName);
          // if (checkpoint.equals(GuiConstants.BACKEND_NO_CHECKPOINT))
          // databaseMBean.disableBackend(backendName);
          // else
          databaseMBean.disableBackendWithCheckpoint(backendName);
        }
        actionSetBackendState(backendName);
      }
      catch (Exception e)
      {
        appendDebugText("Failed to disable backend", e);
      }
    }
    else if (actionName.equals(GuiConstants.BACKEND_STATE_NEW))
    {
      appendDebugText("Creating new backend from backend:" + bob.getName());
      publicActionNewBackendPrompt(bob);
    }
    else if (actionName.equals(GuiConstants.BACKEND_STATE_BACKUP))
    {
      publicActionBackupBackendPrompt(bob);
    }

    actionRefreshBackendState(bob);
  }

  private void actionRefreshBackendState(BackendObject bob)
  {
    String state = bob.getState();
    JPanel panel = (JPanel) backendsState.get(state);
    if (panel.getParent().equals(panel))
    {
      appendDebugText("refresh of backend:" + bob.getName() + " not needed");
    }
    else
    {
      appendDebugText("refresh of backend:" + bob.getName() + " needed");
      actionLoadBackend(bob.getDatabase(), bob.getName(), bob
          .getControllerName(), bob.isEnabled());
      // actionChangeBackendState(bob, bob.getState());
    }
  }

  /**
   * Generate a checkpoint name from the given backend and the current date.
   * 
   * @param backendName the backend to generate a checkpoint for
   * @return backend name concat date
   */
  private String generateCheckpoint(String backendName)
  {
    String check = backendName + ":"
        + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    return Strings.replace(check, ":", "-");
  }

  /**
   * Open the frame to fill in details to create a new backend
   * 
   * @param bob the backend to replicate
   */
  public void publicActionNewBackendPrompt(BackendObject bob)
  {
    if (newBackendFrame == null)
      newBackendFrame = new NewBackendFrame(bob, guiActionListener);
    else
      newBackendFrame.setBob(bob);
    newBackendFrame.setVisible(true);
  }

  /**
   * Restore a backend from a dump file
   * 
   * @param backendName name of the backend
   * @param dumpName name of the dump file
   */
  public void publicActionRestoreBackend(String backendName, String dumpName)
  {
    BackendObject bob = (BackendObject) backendList.get(backendName);
    try
    {
      if (bob.getMbean().isReadEnabled())
      {
        String message = "Cannot restore an enabled backend";
        appendDebugText(message, new ConsoleException(message));
        return;
      }
    }
    catch (Exception e)
    {
      appendDebugText(e.getMessage(), e);
      return;
    }
    appendDebugText("Restoring backend:" + backendName + " with dump name:"
        + dumpName);

    VirtualDatabaseMBean database = actionGetDatabaseBean(bob.getDatabase());
    new Thread(new RestoreBackendTask(this, database, bob, dumpName)).start();
  }

  /**
   * Execute a backup of the given backend
   * 
   * @param bob the backend object
   */
  public void publicActionBackupBackendPrompt(BackendObject bob)
  {
    String checkpoint = actionDisplayInputBackupFrame(bob.getDatabase(), bob
        .getName());
    if (checkpoint == null)
    {
      appendDebugText("No dump name received, cancelling action...");
      return;
    }
    VirtualDatabaseMBean database = actionGetDatabaseBean(bob.getDatabase());
    new Thread(new BackupBackendTask(this, database, bob, checkpoint)).start();
  }

  /**
   * Display the dump input frame
   * 
   * @param database virtual database to dump
   * @param backend backend to dump
   * @return dump name
   */
  private String actionDisplayInputBackupFrame(String database, String backend)
  {
    VirtualDatabaseMBean databaseMBean = actionGetDatabaseBean(database);
    if (inputBackupFrame == null)
    {
      // FIXME Recovery Refactoring : getAvailableDumpFiles() -> getAvailableDumps()
      //File[] files = databaseMBean.getAvailableDumpFiles();
      File[] files = new File[0];
      int size = files.length;
      String[] entries = new String[size];
      for (int i = 0; i < size; i++)
      {
        entries[i] = files[i].getName();
      }
      inputBackupFrame = new GuiInputBackupFrame(this, entries,
          guiActionListener);
      inputBackupFrame.setValue(generateCheckpoint(backend));
      inputBackupFrame.setVisible(true);
    }

    JTextField field = inputBackupFrame.getValueField();
    inputBackupFrame = null;
    if (field == null)
      return null;
    String dump = field.getText();
    return dump;
  }

  /**
   * Set the cursor to be a hand
   */
  public void publicActionRefreshCursorShape()
  {
    appendDebugText("Refresh cursor for main frame");
    Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    setCursor(cursor);
    backendPanel.setCursor(cursor);
    validate();
    repaint();
  }

  /**
   * Load a configuration file on a controller
   * 
   * @param filePath the configuration file
   * @param controllerName the name of the controller
   */
  public void publicActionExecuteControllerDrop(String filePath,
      String controllerName)
  {
    File file = new File(filePath);
    try
    {
      ControllerMBean controllerMBean = actionGetControllerBean(controllerName);
      controllerMBean.addVirtualDatabases(actionLoadXmlText(file));
      appendDebugText("Execute public action on controller drop for:"
          + filePath + " and:" + controllerName);
      actionLoadDatabaseList(controllerName);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to load configuration file :" + file.getName(), e);
    }
  }

  /**
   * actionDatabaseDisableAll definition. Call the proper MBean to disable all
   * backends
   * 
   * @param databaseName virtual database name
   */
  public void publicActionDatabaseDisableAll(String databaseName)
  {
    try
    {
      actionGetDatabaseBean(databaseName).disableAllBackends();
      publicActionLoadBackendsList(databaseName);
      centerPane.setSelectedComponent(backendPanel);
      appendDebugText("Disable All backends for :" + databaseName
          + " was a success");
    }
    catch (Exception e)
    {
      appendDebugText("Disable All backends for :" + databaseName + " failed.",
          e);
    }
  }

  /**
   * GetInfo for the given controller
   * 
   * @param controllerName the controllerName
   */
  public void publicActionGetControllerInfo(String controllerName)
  {
    try
    {
      ControllerMBean controllerMBean = actionGetControllerBean(controllerName);
      infoTextPane.setText(controllerMBean.getXml());
      centerPane.setSelectedComponent(infoScroll);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to get info for controller:" + controllerName, e);
    }
  }

  /**
   * Shutdown the given controller
   * 
   * @param controllerName name of the controller to shutdown
   */
  public void publicActionShutdownController(String controllerName)
  {
    ControllerMBean controllerMBean = null;
    int shutdownLevel = 2;
    try
    {
      controllerMBean = actionGetControllerBean(controllerName);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to get a proxy to shutdown the controller:"
          + controllerName, e);
    }
    try
    {
      if (controllerMBean != null)
      {
        ArrayList databases = controllerMBean.getVirtualDatabaseNames();
        for (int i = 0; i < databases.size(); i++)
        {
          DatabaseObject dob = (DatabaseObject) databaseList.get(databases
              .get(i));
          appendDebugText("Shutting down database:" + dob.getName()
              + " with level:" + shutdownLevel);
          publicActionShutdownDatabase(dob, shutdownLevel);
        }
        controllerMBean.shutdown(shutdownLevel);
      }
      else
        appendDebugText("Failed to get a proxy to shutdown the controller:"
            + controllerName);
    }
    catch (Exception e)
    {
      // lost connection expected
    }
    publicActionLoadControllerList();

  }

  /**
   * Get a report for the given controller
   * 
   * @param controllerName the controller to get the report from
   */
  public void publicActionControllerReport(String controllerName)
  {
    try
    {
      ControllerMBean controllerMBean = actionGetControllerBean(controllerName);
      infoTextPane.setText(controllerMBean.generateReport());
      centerPane.setSelectedComponent(infoScroll);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to get info for controller:" + controllerName, e);
    }
  }

  /**
   * Delete a dump of a database from the virtual database
   * 
   * @param databaseName the url of the controller
   * @param dump the dump object
   */
  public void publicActionDeleteDump(String databaseName, DumpFileObject dump)
  {
    boolean confirm = actionDisplayConfirmFrame(GuiTranslate
        .get("gui.confirm.delete.dump"));
    if (!confirm)
    {
      appendDebugText("Action is not confirmed, cancelling deletion");
      return;
    }
    VirtualDatabaseMBean database = actionGetDatabaseBean(databaseName);
    // FIXME Recovery Refactoring: removeDumpFile(File) -> removeDump(String)
    /*
    try
    {
      database.removeDumpFile(dump.getDumpFile());
      appendDebugText("Removed dump file:" + dump.getDumpFile().getName()
          + " from controller:" + databaseName);
      publicActionLoadDumpList(databaseName);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to removed dump file:"
          + dump.getDumpFile().getName() + " from controller:" + databaseName,
          e);
    }
    */
  }

  /**
   * Display frame to confirm action
   * 
   * @param message message to confirm
   * @return true if YES is selected, false otherwise
   */
  private boolean actionDisplayConfirmFrame(String message)
  {
    int value = JOptionPane.showConfirmDialog(this, message, message,
        JOptionPane.YES_NO_OPTION);
    if (value == JOptionPane.YES_OPTION)
      return true;
    else
      return false;
  }

  /**
   * Execute the backend drop action with this gui
   * 
   * @param panel the target of the drop
   * @param backendName name of the backend that is the target of the drop
   */
  public void publicActionExecuteBackendDrop(JPanel panel, String backendName)
  {
    publicActionExecuteBackendDrop(panel.getName(), backendName);
  }

  /**
   * Load the controller log4j configuration file in the info buffer
   * 
   * @param controllerName the controller's url
   */
  public void publicActionControllerLogConfiguration(String controllerName)
  {
    try
    {
      ControllerMBean controllerMBean = actionGetControllerBean(controllerName);
      logConfigTextPane.setText(controllerMBean.viewLogConfigurationFile());
      centerPane.setSelectedComponent(logConfigScroll);
      logConfigTextPane.addMouseListener(new LogEditPopUpMenu(this,
          controllerName, logConfigTextPane));
    }
    catch (Exception e)
    {
      appendDebugText("Failed to get log4j configuration for controller:"
          + controllerName, e);
    }
  }

  /**
   * Update the controller log4j configuration file, and restart the logger
   * thread
   * 
   * @param controllerName the controller's url
   * @param newContent the new log4j configuration
   */
  public void publicActionUpdateControllerLogConfiguration(
      String controllerName, String newContent)
  {
    try
    {
      appendDebugText("Updating log4j configuration for controller:"
          + controllerName);
      ControllerMBean controllerMBean = actionGetControllerBean(controllerName);
      controllerMBean.updateLogConfigurationFile(newContent);
      actionStartControllerLoggingThread(controllerName);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to update log4j configuration for controller:"
          + controllerName, e);
    }
  }

  /**
   * Change the log4j configuration to Debug mode for all loggers
   * 
   * @param controllerName the name of the controller of this configuration
   * @param logConfiguration the content as a <code>String</code> of the log4j
   *          configuration
   */
  public void publicActionSetLogConfigurationDebug(String controllerName,
      String logConfiguration)
  {
    try
    {
      String newContent = actionReplaceInConfiguration(logConfiguration,
          "INFO", "DEBUG");
      publicActionUpdateControllerLogConfiguration(controllerName, newContent);
    }
    catch (Exception e)
    {
      appendDebugText("Setting log configation to debug failed for controller:"
          + controllerName, e);
    }
  }

  /**
   * Add/remove log4j server output
   * 
   * @param enableServer <tt>true</tt> to enable
   * @param controllerName of the controller for this configuration
   * @param logConfiguration logFile name
   */
  public void publicActionSetLogConfigurationServer(boolean enableServer,
      String controllerName, String logConfiguration)
  {
    try
    {
      String msg = "Setting server configuration to " + enableServer
          + " for controller:" + controllerName;
      appendDebugText(msg);
      String newContent = "";
      if (enableServer)
      {
        // if it was enabled before ?
        String tmp = actionReplaceInConfiguration(logConfiguration,
            "Console,server", "Console");
        newContent = actionReplaceInConfiguration(tmp, "Console,",
            "Console,server,");
      }
      else
      {
        newContent = actionReplaceInConfiguration(logConfiguration,
            "Console,server", "Console");
      }
      publicActionUpdateControllerLogConfiguration(controllerName, newContent);
    }
    catch (Exception e)
    {
      appendDebugText(
          "Setting log configation of appender server failed for controller:"
              + controllerName, e);
    }

  }

  private String actionReplaceInConfiguration(String logConfiguration,
      String oldMode, String newMode) throws IOException
  {
    BufferedReader reader = new BufferedReader(new StringReader(
        logConfiguration));
    StringBuffer buffer = new StringBuffer();
    String line;
    int index = 0;
    while ((line = reader.readLine()) != null)
    {
      while ((index = line.indexOf(oldMode, index + 1)) != -1)
      {
        line = line.substring(0, index) + newMode
            + line.substring(index + oldMode.length());
      }
      buffer.append(line + System.getProperty("line.separator"));
    }
    logConfigTextPane.setText(buffer.toString());
    return buffer.toString();
  }

  /**
   * Change the log4j configuration to Info mode for all loggers
   * 
   * @param controllerName the name of the controller of this configuration
   * @param logConfiguration the content as a <code>String</code> of the log4j
   *          configuration
   */
  public void publicActionSetLogConfigurationInfo(String controllerName,
      String logConfiguration)
  {
    try
    {
      String newContent = actionReplaceInConfiguration(logConfiguration,
          "DEBUG", "INFO");
      publicActionUpdateControllerLogConfiguration(controllerName, newContent);
    }
    catch (Exception e)
    {
      appendDebugText("Setting log configation to debug failed for controller:"
          + controllerName, e);
    }

  }

  /**
   * Returns the backendList value.
   * 
   * @return Returns the backendList.
   */
  public Hashtable getBackendList()
  {
    return backendList;
  }

  /**
   * Clean the content of the logging text pane and repaint
   */
  public void publicActioncleanLoggingPane()
  {
    loggingTextPane.setText("");
    loggingTextPane.validate();
    loggingTextPane.repaint();
  }

  /**
   * Remove a configuration file from the list
   * 
   * @param cfo the file object associated
   */
  public void publicActionRemoveConfigurationFile(ConfigurationFileObject cfo)
  {
    Container container = cfo.getParent();
    container.remove(cfo);
    System.out.println(guiSession.getConfigurationFiles().remove(
        new File(cfo.getFilePath())));
    container.validate();
    container.repaint();
  }

  /**
   * Removes a controller from the gui
   * 
   * @param controllerName the name of the controller
   */
  public void publicActionControllerRemove(String controllerName)
  {
    ControllerObject co = (ControllerObject) controllerList
        .remove(controllerName);
    if (co != null)
    {
      controllerListPanel.remove(co);
      controllerListPanel.validate();
      controllerListPanel.repaint();
    }
    guiSession.getControllerItems().remove(controllerName);
  }

  /**
   * Start monitoring console.
   * 
   * @param controllerName the controller to monitor
   * @param displayController true if controller pane should be displayed
   * @param displayVdb true if virtual database pane should be displayed
   * @param displayBackends true if backend pane should be displayed
   */
  public void publicActionStartMonitor(String controllerName,
      boolean displayController, boolean displayVdb, boolean displayBackends)
  {
    appendDebugText("Creating monitoring console for controller:"
        + controllerName);
    try
    {
      new MonitoringConsole(controllerName, displayController
          ? actionGetControllerBean(controllerName)
          : null, displayVdb ? actionGetDatabaseBean(selectedDatabase) : null,
          displayBackends);
    }
    catch (Exception e)
    {
      appendDebugText("Loading of monitoring console failed for controller:"
          + controllerName, e);
    }
  }

  /**
   * Create the new backend by sending the jmx command
   */
  public void publicActionCreateBackendExecute()
  {
    newBackendFrame.setVisible(false);
    BackendObject bob = newBackendFrame.getBob();
    VirtualDatabaseMBean database = actionGetDatabaseBean(bob.getDatabase());
    String oldname = bob.getName();
    String newname = newBackendFrame.getNewName().getText();
    String url = newBackendFrame.getNewUrl().getText();
    HashMap map = new HashMap();
    map.put("url", url);
    try
    {
      database.replicateBackend(oldname, newname, map);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to create new backend:" + newname, e);
    }
  }

  /**
   * Display the cache content in a new frame
   * 
   * @param databaseName database name that contains the cache
   */
  public void publicActionViewCache(String databaseName)
  {
    try
    {
      DataCollectorMBean collector = currentJmxClient.getDataCollectorProxy();
      CacheViewer viewer = new CacheViewer(collector
          .retrieveCacheData(databaseName));
      viewer.display();
    }
    catch (Exception e)
    {
      appendDebugText("Failed to collect cache data for database:"
          + databaseName, e);
    }
  }

  /**
   * Display the SQL stats
   * 
   * @param databaseName virtual database name
   */
  public void publicActionViewSQLStats(String databaseName)
  {
    try
    {
      DataCollectorMBean collector = currentJmxClient.getDataCollectorProxy();
      SQLStatViewer viewer = new SQLStatViewer(collector
          .retrieveSQLStats(databaseName));
      viewer.display();
    }
    catch (Exception e)
    {
      appendDebugText(
          "Failed to collect sql data for database:" + databaseName, e);
    }

  }

  /**
   * Display the cache stats
   * 
   * @param databaseName virtual database name
   */
  public void publicActionViewCacheStats(String databaseName)
  {
    try
    {
      DataCollectorMBean collector = currentJmxClient.getDataCollectorProxy();
      CacheStatsViewer viewer = new CacheStatsViewer(collector
          .retrieveCacheStatsData(databaseName));
      viewer.display();
    }
    catch (Exception e)
    {
      appendDebugText("Failed to collect cache stats for database:"
          + databaseName, e);
    }
  }

  /**
   * Get the action listener for the gui
   * 
   * @return the listener of events
   */
  public CjdbcGuiListener getGuiActionListener()
  {
    return guiActionListener;
  }

  /**
   * Returns the guiSession value.
   * 
   * @return Returns the guiSession.
   */
  public GuiSession getGuiSession()
  {
    return guiSession;
  }

  /**
   * Returns the jmxClients value.
   * 
   * @return Returns the jmxClients.
   */
  public Hashtable getJmxClients()
  {
    return jmxClients;
  }

  /**
   * Remove the backend graphic object from the gui
   * 
   * @param backendName the backend to remove
   */
  public void publicActionRemoveBackendFromGui(String backendName)
  {
    BackendObject bo = (BackendObject) backendList.remove(backendName);
    if (bo != null)
    {
      backendIcons.remove(bo);
      JPanel panel = (JPanel) backendsState.get(bo.getState());
      panel.remove(bo);
      paintBackendPane();
    }
  }

  /**
   * Remove a backend from the backend list, and from the controller
   * 
   * @param bo the <code>BackendObject</code> that originated the action
   */
  public void publicActionRemoveBackend(BackendObject bo)
  {
    boolean confirm = actionDisplayConfirmFrame(GuiTranslate.get(
        "gui.confirm.remove.backend", bo.getName()));
    if (!confirm)
    {
      appendDebugText("Cancelling backend deletion...");
      return;
    }

    String controllerName = bo.getControllerName();
    String databaseName = bo.getDatabase();
    String backendName = bo.getName();
    RmiJmxClient jmxClient = (RmiJmxClient) jmxClients.get(controllerName);
    String user = guiSession.getAuthenticatedDatabaseLogin(databaseName);
    String password = guiSession.getAuthenticatedDatabasePassword(databaseName);
    try
    {
      VirtualDatabaseMBean mbean = jmxClient.getVirtualDatabaseProxy(
          databaseName, user, password);
      mbean.removeBackend(backendName);
      publicActionRemoveBackendFromGui(backendName);
    }
    catch (Exception e)
    {
      appendDebugText("Cannot remove backend:" + backendName, e);
    }

  }

  /**
   * Execute a backend transfer from one controller to an other one
   * 
   * @param backendName the name of the backend to transfer
   * @param controllerName the target controller to transfer to
   */
  public void publicActionExecuteTransfer(String backendName,
      String controllerName)
  {
    BackendObject bo = (BackendObject) backendList.get(backendName);
    String controllerNameOrigin = bo.getControllerName();
    RmiJmxClient jmxClient = (RmiJmxClient) jmxClients
        .get(controllerNameOrigin);
    String databaseName = bo.getDatabase();
    String user = guiSession.getAuthenticatedDatabaseLogin(databaseName);
    String password = guiSession.getAuthenticatedDatabasePassword(databaseName);
    try
    {
      VirtualDatabaseMBean mbean = jmxClient.getVirtualDatabaseProxy(
          databaseName, user, password);
      mbean.transferBackend(backendName, controllerName);
      bo.setControllerName(controllerName);
      // publicActionRemoveBackendFromGui(backendName);
    }
    catch (Exception e)
    {
      appendDebugText("Cannot remove backend:" + backendName, e);
    }
  }

  /**
   * Performs checks before calling <method>publicActionRemoveBackendFromGui
   * </method>
   * 
   * @param backendName the backend name to remove
   * @param controller the controller that it was removed from
   */
  public void publicActionRemoveBackendFromGui(String backendName,
      String controller)
  {
    BackendObject bo = (BackendObject) backendList.get(backendName);
    if (bo != null)
    {
      String boController = bo.getControllerName();
      if (boController.equals(controller))
        publicActionRemoveBackendFromGui(backendName);
      else
      {
        appendDebugText("Call for removing backend:" + backendName
            + " from controller:" + controller
            + " but backend is on controller:" + boController);
      }
    }

  }

  /**
   * Display the shutdown dialog and then shutdown the database with the
   * corresponding level
   * 
   * @param database the database name we want to shutdown.
   */
  public void publicActionDisplayShutdownFrame(DatabaseObject database)
  {
    if (selectShutdownFrame == null)
    {
      selectShutdownFrame = new GuiSelectShutdownFrame(this, guiActionListener);
      selectShutdownFrame.setVisible(true);
    }
    String shutdownLevel = selectShutdownFrame.getValueField().getText();
    // Defaults to safe mode
    int iLevel = Constants.SHUTDOWN_SAFE;
    if (shutdownLevel.equals(GuiCommands.COMMAND_SHUTDOWN_SAFE))
      iLevel = Constants.SHUTDOWN_SAFE;
    else if (shutdownLevel.equals(GuiCommands.COMMAND_SHUTDOWN_FORCE))
      iLevel = Constants.SHUTDOWN_FORCE;
    else if (shutdownLevel.equals(GuiCommands.COMMAND_SHUTDOWN_WAIT))
      iLevel = Constants.SHUTDOWN_WAIT;
    appendDebugText("shudown level of database:" + database.getName() + " is:"
        + shutdownLevel);
    selectShutdownFrame = null;
    publicActionShutdownDatabase(database, iLevel);
  }

  /**
   * Display the content of the recovery log in a frame
   * 
   * @param databaseName the database that containts the recovery log we want
   */
  public void publicActionViewRecoveryLog(String databaseName)
  {
    try
    {
      DataCollectorMBean collector = currentJmxClient.getDataCollectorProxy();
      RecoveryLogViewer viewer = new RecoveryLogViewer(collector
          .retrieveRecoveryLogData(databaseName));
      viewer.display();
    }
    catch (Exception e)
    {
      appendDebugText("Failed to collect cache stats for database:"
          + databaseName, e);
    }
  }

  /**
   * Set the checkpoint of a backend
   * 
   * @param bo the backend to change the checkpoint
   */
  public void publicActionSetCheckpoint(BackendObject bo)
  {
    String backendName = bo.getName();
    String database = bo.getDatabase();
    String checkpoint = actionDisplaySelectCheckpointFrame(database);
    try
    {
      appendDebugText("Setting checkpoint for backend:" + backendName + ":"
          + checkpoint);
      bo.getMbean().setLastKnownCheckpoint(checkpoint);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to set checkpoint for backend:" + backendName, e);
    }
  }

  /**
   * Unset checkpoint of a backend (set it to null)
   * 
   * @param bo backend object
   */
  public void publicActionUnSetCheckpoint(BackendObject bo)
  {
    String backendName = bo.getName();
    boolean confirm = actionDisplayConfirmFrame(GuiTranslate.get(
        "gui.confirm.unset.backend.checkpoint", backendName));
    if (!confirm)
    {
      appendDebugText("Cancelling backend null checkpoint...");
      return;
    }
    try
    {
      appendDebugText("UnSetting checkpoint for backend:" + backendName);
      bo.getMbean().setLastKnownCheckpoint(null);
    }
    catch (Exception e)
    {
      appendDebugText("Failed to unset checkpoint for backend:" + backendName,
          e);
    }
    finally
    {
      actionRefreshBackendState(bo);
    }

  }

  /**
   * Test jdbc connection on this backend
   * 
   * @param bo the backend object to test the connection on
   */
  public void publicActionTestBackendConnection(BackendObject bo)
  {
    try
    {
      DatabaseBackendMBean bean = bo.getMbean();
      boolean ok = bean.isJDBCConnected();
      String message = "Backend " + bo.getName();
      if (ok)
        message += " has ";
      else
        message += " does not have ";
      message += "JDBC Connectivity.";

      appendDebugText("Result of JDBC Connect[" + bo.getName() + "]:" + ok);

      Icon icon = (ok)
          ? GuiIcons.BACKEND_ENABLED_ICON
          : GuiIcons.BACKEND_DISABLED_ICON;
      JOptionPane.showMessageDialog(this, message, bo.getName(),
          JOptionPane.INFORMATION_MESSAGE, icon);
    }
    catch (Exception e)
    {
      appendDebugText(
          "Cannot determine if backend has still JDBC connectivity", e);
    }
  }

  /**
   * Refresh the list of mbeans
   */
  public void publicActionRefreshMBeans()
  {
    Set set;
    if (currentJmxClient == null)
    {
      appendDebugText("Jmx Client has not been set yet");
      return;
    }
    try
    {
      appendDebugText("Refreshing mbeans for jmx server:"
          + currentJmxClient.getRemoteName());
      set = currentJmxClient.listCJDBCMBeans();
      int size = set.size();
      Iterator iter = set.iterator();
      Object[] data = new Object[size];
      for (int i = 0; i < size; i++)
        data[i] = ((ObjectInstance) iter.next()).getObjectName();
      mbeanList.setListData(data);
      mbeanList.validate();
      appendDebugText("Finished Refreshing mbeans for jmx server:"
          + currentJmxClient.getRemoteName());
    }
    catch (Exception e)
    {
      appendDebugText("Failed Refreshing mbeans for jmx server:"
          + currentJmxClient.getRemoteName());
    }
  }

  /**
   * Position the jmx frames on the desktop
   * 
   * @param vertical tile the frames vertically if true, horizontally if false
   */
  public void publicActionTileJmxFrames(boolean vertical)
  {
    Dimension dim = jmxPanel.getSize();
    double height = dim.getHeight();
    double width = dim.getWidth();
    if (vertical)
      width = width / 3;
    else
      height = height / 3;

    Dimension newDim = new Dimension((int) width, (int) height);
    attributeFrame.setSize(newDim);
    operationFrame.setSize(newDim);
    mbeanFrame.setSize(newDim);

    int x = (int) jmxPanel.getLocation().getX();
    int y = 0; // problem with y value

    mbeanFrame.setLocation(x, y);
    if (vertical)
    {
      mbeanFrame.setLocation(x, y);
      attributeFrame.setLocation((int) (x + width), y);
      operationFrame.setLocation((int) (x + 2 * width), y);
    }
    else
    {
      mbeanFrame.setLocation(x, y);
      attributeFrame.setLocation(x, (int) (y + height));
      operationFrame.setLocation(x, (int) (y + 2 * height));
    }

    jmxPanel.validate();

  }

  /**
   * Refresh MBean attributes.
   * 
   * @param mbean the MBean to refresh
   */
  public void publicActionRefreshMBeanAttributes(ObjectName mbean)
  {
    try
    {
      appendDebugText("Fetching attributes for mbean:" + mbean);
      MBeanInfo info = currentJmxClient.getMBeanInfo(mbean);
      MBeanAttributeInfo[] attrInfo = info.getAttributes();

      AttributeModel dataModel = new AttributeModel(attrInfo, currentJmxClient,
          mbean);
      attributeTable.setModel(dataModel);
    }
    catch (Exception e)
    {
      appendDebugText("Could not fetch attributes for mbean:" + mbean);
    }
  }

  /**
   * Refresh MBean methods.
   * 
   * @param mbean the MBean to refresh
   */
  public void publicActionRefreshMBeanMethods(ObjectName mbean)
  {
    try
    {
      appendDebugText("Fetching methods for mbean:" + mbean);
      MBeanInfo info = currentJmxClient.getMBeanInfo(mbean);
      MBeanOperationInfo[] operInfo = info.getOperations();

      OperationModel dataModel = new OperationModel(operInfo);
      operationTable.setModel(dataModel);
    }
    catch (Exception e)
    {
      appendDebugText("Could not fetch methods for mbean:" + mbean);
    }
  }

  /**
   * Returns the currentJmxClient value.
   * 
   * @return Returns the currentJmxClient.
   */
  public RmiJmxClient getCurrentJmxClient()
  {
    return currentJmxClient;
  }

  /**
   * Sets the currentJmxClient value.
   * 
   * @param currentJmxClient The currentJmxClient to set.
   */
  public void setCurrentJmxClient(RmiJmxClient currentJmxClient)
  {
    this.currentJmxClient = currentJmxClient;
  }

  /**
   * Opens a window to perform a jmx operation
   * 
   * @param info an <code>MBeanOperationInfo</code> object
   * @param name name of the JMX object
   */
  public void getOperationCallDialog(ObjectName name, MBeanOperationInfo info)
  {
    new OperationCallDialog(this, name, info).setVisible(true);
  }

  /**
   * Opens a window t ochange an attribute
   * 
   * @param info an <code>MBeanOperationInfo</code> object
   * @param name name of the JMX object
   */
  public void getAttributeChangeDialog(ObjectName name, MBeanAttributeInfo info)
  {
    new AttributeChangeDialog(this, name, info).setVisible(true);
  }

}