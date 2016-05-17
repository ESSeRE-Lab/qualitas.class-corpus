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

package org.objectweb.cjdbc.console.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ProgressMonitor;

import org.objectweb.cjdbc.common.i18n.GuiTranslate;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.constants.GuiIcons;
import org.objectweb.cjdbc.console.gui.dnd.listeners.BackendTransferListener;
import org.objectweb.cjdbc.console.gui.dnd.listeners.ControllerTransferListener;
import org.objectweb.cjdbc.console.gui.frames.GuiExceptionFrame;
import org.objectweb.cjdbc.console.gui.frames.GuiNewControllerFrame;
import org.objectweb.cjdbc.console.gui.jtools.JTextAreaWriter;
import org.objectweb.cjdbc.console.gui.model.JNewList;
import org.objectweb.cjdbc.console.gui.popups.ControllerListPopUpMenu;
import org.objectweb.cjdbc.console.gui.popups.LoggingPopUpMenu;
import org.objectweb.cjdbc.console.gui.popups.XmlEditPopUpMenu;
import org.objectweb.cjdbc.console.gui.session.GuiSession;
import org.objectweb.cjdbc.console.gui.threads.GuiParsingThread;

/**
 * This class defines a CjdbcGuiLoader
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class CjdbcGuiLoader implements Runnable
{
  static final int TOTAL_LOAD_METHOD = 9;
  private CjdbcGui gui;

  /**
   * Creates a new <code>CjdbcGuiLoader.java</code> object
   * 
   * @param gui the main gui
   */
  public CjdbcGuiLoader(CjdbcGui gui)
  {
    this.gui = gui;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    // Define progress monitor
    ProgressMonitor pm = new ProgressMonitor(gui, GuiTranslate
        .get("gui.init.loading"), "", 0, 100);
    pm.setMillisToDecideToPopup(0);
    pm.setMillisToPopup(0);

    pm.setNote(GuiTranslate.get("gui.init.variables"));
    pm.setProgress(getProgress(0));
    defineMembers();

    pm.setNote(GuiTranslate.get("gui.init.size"));
    pm.setProgress(getProgress(1));
    defineSize();

    pm.setNote(GuiTranslate.get("gui.init.left.pane"));
    pm.setProgress(getProgress(2));
    defineLeftPane();

    pm.setNote(GuiTranslate.get("gui.init.center.pane"));
    pm.setProgress(getProgress(3));
    defineCenterPane();

    pm.setNote(GuiTranslate.get("gui.init.right.pane"));
    pm.setProgress(getProgress(4));
    // defineRightPane();

    pm.setNote(GuiTranslate.get("gui.init.frame.controller"));
    pm.setProgress(getProgress(5));
    defineNewControllerFrame();

    pm.setNote(GuiTranslate.get("gui.init.frame.menu"));
    pm.setProgress(getProgress(6));
    defineMenu();

    pm.setNote(GuiTranslate.get("gui.init.session"));
    pm.setProgress(getProgress(7));
    defineSession();

    pm.setNote(GuiTranslate.get("gui.init.rendering"));
    pm.setProgress(getProgress(8));
    defineMainFrame();

  }

  private int getProgress(int index)
  {
    return (index + 1) * 100 / TOTAL_LOAD_METHOD;
  }

  private void defineMembers()
  {

    // Initialize private members
    gui.guiSession = new GuiSession();
    gui.backendsState = new Hashtable();
    gui.backendList = new Hashtable();
    gui.databaseMBeans = new Hashtable();
    gui.controllerMBeans = new Hashtable();
    gui.databaseList = new Hashtable();
    gui.controllerList = new Hashtable();
    gui.jmxClients = new Hashtable();

    // Define transfer handlers
    gui.backendTransferListener = new BackendTransferListener(gui);
    gui.configurationFileTransferListener = new ControllerTransferListener(gui);

    // Define mouse listeners
    gui.controllerListPopUpMenu = new ControllerListPopUpMenu(gui);
    gui.guiActionListener = new CjdbcGuiListener(gui);

    // Define exception frame
    gui.exceptionFrame = new GuiExceptionFrame(gui);
  }

  private void defineSize()
  {
    // Initialize basic layout properties
    gui.setBackground(Color.lightGray);
    gui.getContentPane().setLayout(new BorderLayout());
    // Set the frame's display to be WIDTH x HEIGHT in the middle of the screen
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension dim = toolkit.getScreenSize();
    int screenHeight = dim.height;
    int screenWidth = dim.width;
    gui.setBounds((screenWidth - GuiConstants.MAIN_FRAME_WIDTH) / 2,
        (screenHeight - GuiConstants.MAIN_FRAME_HEIGHT) / 2,
        GuiConstants.MAIN_FRAME_WIDTH, GuiConstants.MAIN_FRAME_HEIGHT);

  }

  private void defineMenu()
  {
    // /////////////////////////////////////////////////////////////////////////
    // Define main menu
    // /////////////////////////////////////////////////////////////////////////
    Color toolBarColor = Color.white;
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu(GuiTranslate.get("gui.menu.action"));
    JMenuItem item1 = new JMenuItem(GuiCommands.COMMAND_QUIT);
    item1.setBackground(toolBarColor);
    JMenuItem item2 = new JMenuItem(GuiCommands.COMMAND_ADD_CONFIG_FILE);
    item2.setBackground(toolBarColor);
    JMenuItem item3 = new JMenuItem(GuiCommands.COMMAND_ADD_CONTROLLER);
    item3.setBackground(toolBarColor);
    JMenuItem item4 = new JMenuItem(GuiCommands.COMMAND_SAVE_CONFIGURATION_FILE);
    item4.setBackground(toolBarColor);
    JMenuItem item5 = new JMenuItem(GuiCommands.COMMAND_CLEAN_DEBUG_BUFFER);
    item5.setBackground(toolBarColor);
    JMenuItem item6 = new JMenuItem(GuiCommands.COMMAND_REFRESH_LOGS);
    item6.setBackground(toolBarColor);
    JMenuItem item7 = new JMenuItem(GuiCommands.COMMAND_CLEAN_LOGGING_PANEL);
    item7.setBackground(toolBarColor);
    JMenuItem item8 = new JMenuItem(
        GuiCommands.COMMAND_MONITOR_CURRENT_CONTROLLER);
    item8.setBackground(toolBarColor);
    menu.add(item2).addActionListener(gui.guiActionListener);
    menu.add(item3).addActionListener(gui.guiActionListener);
    menu.add(item4).addActionListener(gui.guiActionListener);
    menu.add(item5).addActionListener(gui.guiActionListener);
    menu.add(item6).addActionListener(gui.guiActionListener);
    menu.add(item7).addActionListener(gui.guiActionListener);
    menu.add(item8).addActionListener(gui.guiActionListener);
    menu.addSeparator();
    menu.add(item1).addActionListener(gui.guiActionListener);
    menu.setVisible(true);
    menu.setBackground(toolBarColor);
    menuBar.add(menu);
    menuBar.setBackground(toolBarColor);
    gui.setJMenuBar(menuBar);
  }

  private void defineSession()
  {
    // /////////////////////////////////////////////////////////////////////////
    // Loading gui session
    // /////////////////////////////////////////////////////////////////////////
    try
    {
      gui.guiSession.loadSessionFromFile(new File(
          GuiConstants.CJDBC_DEFAULT_SESSION_FILE));
    }
    catch (IOException e)
    {
      gui.appendDebugText(e.getMessage());
    }
    gui.actionLoadXmlList();
    gui.publicActionLoadControllerList();
  }

  private void defineMainFrame()
  {
    // Put the final touches to the JFrame object
    gui.publicActionRefreshCursorShape();
    gui.validate();
    gui.setVisible(true);
    gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  private void defineLeftPane()
  {
    // /////////////////////////////////////////////////////////////////////////
    // Define controller panel
    // /////////////////////////////////////////////////////////////////////////
    gui.controllerListPanel = new JPanel(new GridLayout(1, 1));
    gui.controllerListPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    gui.controllerListPanel.setName(GuiConstants.LIST_CONTROLLER);
    gui.controllerListPanel.addMouseListener(gui.controllerListPopUpMenu);
    JScrollPane controllerScroll = new JScrollPane();
    controllerScroll.getViewport().add(gui.controllerListPanel);
    JPanel controllerPane = new JPanel(new BorderLayout());
    controllerPane.add(controllerScroll, BorderLayout.CENTER);
    controllerPane.add(new JLabel(GuiTranslate.get("gui.panel.controllers")),
        BorderLayout.NORTH);

    // /////////////////////////////////////////////////////////////////////////
    // Define virtual database panel
    // /////////////////////////////////////////////////////////////////////////
    gui.vdbListPanel = new JPanel(new GridLayout(1, 1));
    gui.vdbListPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    gui.vdbListPanel.setName(GuiConstants.LIST_DATABASE);
    JScrollPane vdbScroll = new JScrollPane();
    vdbScroll.getViewport().add(gui.vdbListPanel);
    JPanel vdbPane = new JPanel(new BorderLayout());
    vdbPane.add(vdbScroll, BorderLayout.CENTER);
    vdbPane.add(new JLabel(GuiTranslate.get("gui.panel.virtualdatabases")),
        BorderLayout.NORTH);

    // /////////////////////////////////////////////////////////////////////////
    // Define configuration files panel
    // /////////////////////////////////////////////////////////////////////////
    gui.fileListPanel = new JPanel(new GridLayout(1, 1));
    // gui.fileListPanel.setMaximumSize(new Dimension(200, gui.getHeight()));
    // gui.fileListPanel.setPreferredSize(new Dimension(200, gui.getHeight()));
    // gui.fileListPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    gui.fileListPanel.setName(GuiConstants.LIST_FILES);
    gui.fileListPanel.setVisible(true);

    gui.fileScroll = new JScrollPane();
    gui.fileScroll
        .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    gui.fileScroll
        .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    gui.fileScroll.getViewport().add(gui.fileListPanel);
    JPanel filePane = new JPanel(new BorderLayout());
    filePane.add(gui.fileScroll, BorderLayout.CENTER);
    filePane.add(new JLabel(GuiTranslate.get("gui.panel.configuration.files")),
        BorderLayout.NORTH);

    // /////////////////////////////////////////////////////////////////////////
    // Define logo panel
    // /////////////////////////////////////////////////////////////////////////
    JPanel logo = new JPanel();
    logo.setBackground(Color.white);
    JLabel logoImg = new JLabel();
    logoImg.setIcon(GuiIcons.CJDBC_LOGO);
    logoImg.setMinimumSize(new Dimension(GuiIcons.CJDBC_LOGO.getIconWidth(),
        GuiIcons.CJDBC_LOGO.getIconHeight()));
    logo.add(logoImg);

    // /////////////////////////////////////////////////////////////////////////
    // Define left panel
    // /////////////////////////////////////////////////////////////////////////
    // Get the left pane for controllers and virtual databases
    JPanel leftPane = new JPanel(new GridLayout(4, 1));
    leftPane.setMaximumSize(new Dimension(250, gui.getHeight()));
    leftPane.setPreferredSize(new Dimension(250, gui.getHeight()));

    leftPane.add(controllerPane);
    leftPane.add(vdbPane);
    leftPane.add(filePane);
    leftPane.add(logo);
    // Add it to the main frame
    gui.getContentPane().add(leftPane, BorderLayout.WEST);
  }

  private void defineNewControllerFrame()
  {
    // ////////////////////////////////////////////////////////////////////////
    // Define new controller frame
    // /////////////////////////////////////////////////////////////////////////
    gui.newControllerFrame = new GuiNewControllerFrame(gui.guiActionListener);
  }

  private void defineCenterPane()
  {

    // ////////////////////////////////////////////////////////////////////////
    // Define center panel
    // /////////////////////////////////////////////////////////////////////////
    gui.centerPane = new JTabbedPane();
    gui.centerPane.setTabPlacement(JTabbedPane.TOP); // or
    // BOTTOM,
    // LEFT,
    // RIGHT,TOP

    // ////////////////////////////////////////////////////////////////////////
    // Define jmx panel
    // /////////////////////////////////////////////////////////////////////////
    gui.jmxPanel = new JDesktopPane();
    gui.jmxPanel.addFocusListener(gui.guiActionListener);
    gui.mbeanList = new JNewList();
    gui.mbeanList.addListSelectionListener(gui.guiActionListener);
    gui.jmxScroll = new JScrollPane(gui.mbeanList);

    // Mbean List frame
    gui.mbeanFrame = new JInternalFrame("Jmx MBeans List", true);
    gui.mbeanFrame.setBackground(Color.WHITE);
    gui.mbeanFrame.getContentPane().add(gui.jmxScroll);
    gui.mbeanFrame.setSize(300, 400);
    gui.mbeanFrame.setVisible(true);
    
    gui.jmxPanel.add(gui.mbeanFrame);

    // Attribute List frame
    gui.attributeFrame = new JInternalFrame("Jmx MBean Attributes", true);
    gui.attributeFrame.setBackground(Color.WHITE);
    gui.attributeTable = new JTable();
    gui.attributeTable.setName(GuiConstants.TABLE_JMX_ATTRIBUTES);
    gui.attributeTable.addMouseListener(gui.guiActionListener);
    gui.attributePane = new JScrollPane(gui.attributeTable);
    gui.attributeFrame.getContentPane().add(gui.attributePane);
    gui.attributeFrame.setSize(300, 400);
    Point point = ((Point) gui.jmxPanel.getLocation().clone());
    point.move(300, 0);
    gui.attributeFrame.setLocation(point);
    gui.attributeFrame.setVisible(true);
    gui.attributeFrame.validate();
    gui.jmxPanel.add(gui.attributeFrame);

    // Operation List frame
    gui.operationFrame = new JInternalFrame("Jmx MBean Operations", true);
    gui.operationFrame.setBackground(Color.WHITE);
    gui.operationTable = new JTable();
    gui.operationTable.setName(GuiConstants.TABLE_JMX_OPERATIONS);
    gui.operationTable.addMouseListener(gui.guiActionListener);
    gui.operationPane = new JScrollPane(gui.operationTable);
    gui.operationFrame.getContentPane().add(gui.operationPane);
    gui.operationFrame.setSize(300, 400);
    Point point2 = ((Point) gui.jmxPanel.getLocation().clone());
    point2.move(600, 0);
    gui.operationFrame.setLocation(point2);
    gui.operationFrame.setVisible(true);
    gui.operationFrame.validate();
    gui.jmxPanel.add(gui.operationFrame);

    // ////////////////////////////////////////////////////////////////////////
    // Define debug panel
    // /////////////////////////////////////////////////////////////////////////
    gui.debugScroll = new JScrollPane();
    gui.debugText = "";
    gui.debugTextPane = new JTextArea();
    gui.debugTextPane.setFont(GuiConstants.CENTER_PANE_FONT);
    gui.debugTextPane.setEditable(false);
    gui.debugTextPane.setText(gui.debugText);
    gui.debugTextPane.setBackground(Color.white);

    gui.debugTraceTextPane = new JTextArea();
    gui.debugTraceTextPane.setFont(GuiConstants.CENTER_PANE_FONT);
    gui.debugTraceTextPane.setEditable(false);
    gui.debugTraceTextPane.setBackground(Color.white);
    gui.traceWriter = new JTextAreaWriter(gui.debugTraceTextPane);

    JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
        gui.debugTextPane, gui.debugTraceTextPane);
    gui.debugScroll.getViewport().add(pane);

    // ////////////////////////////////////////////////////////////////////////
    // Define backend panel
    // /////////////////////////////////////////////////////////////////////////
    gui.actionLoadBackendPane(true);

    // ////////////////////////////////////////////////////////////////////////
    // Define help panel
    // /////////////////////////////////////////////////////////////////////////
    gui.helpScroll = new JScrollPane();
    gui.xmlTextPane = new JTextPane();
    JTextPane helpPanel = new JTextPane();
    URL localUrl = this.getClass().getResource("/userGuide.html");
    if (localUrl != null)
    {
      try
      {
        helpPanel.setPage(localUrl);
      }
      catch (IOException e)
      {
        gui.appendDebugText("Failed to load local help...", e);
        helpPanel.setEnabled(false);
      }
    }
    else
    {
      gui.appendDebugText("Failed to load local help...");
      try
      {
        URL url = new URL(GuiConstants.CJDBC_URL_DOC);
        helpPanel.setPage(url);
      }
      catch (Exception e)
      {
        helpPanel.setText("Could not load online help...");
        helpPanel.setEnabled(false);
      }
    }
    helpPanel.setEditable(false);
    gui.helpScroll = new JScrollPane();
    gui.helpScroll.getViewport().add(helpPanel);

    // ////////////////////////////////////////////////////////////////////////
    // Define xml panel
    // /////////////////////////////////////////////////////////////////////////
    gui.xmlScroll = new JScrollPane();
    gui.xmlTextPane = new JTextPane();
    gui.xmlTextPane.setText(gui.actionLoadXmlText(null));
    gui.xmlTextPane.setFont(GuiConstants.CENTER_PANE_FONT);
    gui.xmlTextPane.setBackground(Color.white);
    gui.xmlTextPane.setEditable(true);
    gui.xmlTextPane.addMouseListener(new XmlEditPopUpMenu(gui));
    gui.xmlScroll.getViewport().add(gui.xmlTextPane);
    // Parsing thread
    gui.parsingThread = new GuiParsingThread(gui.xmlTextPane);
    gui.parsingThread.start();
    gui.xmlTextPane.addKeyListener(gui.parsingThread);

    // ////////////////////////////////////////////////////////////////////////
    // Define logging panel
    // /////////////////////////////////////////////////////////////////////////
    gui.loggingScroll = new JScrollPane();
    gui.loggingTextPane = new JTextArea();
    gui.loggingTextPane.setFont(GuiConstants.CENTER_PANE_FONT);
    gui.loggingTextPane.setEditable(false);
    gui.loggingTextPane.addMouseListener(new LoggingPopUpMenu(gui));
    gui.loggingTextPane.setBackground(Color.white);
    gui.loggingScroll.getViewport().add(gui.loggingTextPane);

    // ////////////////////////////////////////////////////////////////////////
    // Define info panel
    // /////////////////////////////////////////////////////////////////////////
    gui.infoScroll = new JScrollPane();
    gui.infoTextPane = new JTextPane();
    gui.infoTextPane.setFont(GuiConstants.CENTER_PANE_FONT);
    gui.infoTextPane.setEditable(false);
    gui.infoTextPane.setBackground(Color.white);
    gui.infoScroll.getViewport().add(gui.infoTextPane);

    // ////////////////////////////////////////////////////////////////////////
    // Define log configuration panel
    // /////////////////////////////////////////////////////////////////////////
    gui.logConfigScroll = new JScrollPane();
    gui.logConfigTextPane = new JTextPane();
    gui.logConfigTextPane.setFont(GuiConstants.CENTER_PANE_FONT);
    gui.logConfigTextPane.setEditable(true);
    gui.logConfigTextPane.setBackground(Color.white);
    gui.logConfigScroll.getViewport().add(gui.logConfigTextPane);

    // ////////////////////////////////////////////////////////////////////////
    // Add tabs to panel and add tabbed panel to main frame
    // /////////////////////////////////////////////////////////////////////////
    gui.centerPane.addTab(GuiTranslate.get("gui.panel.backends"),
        GuiIcons.BACKEND_PANEL_ICON, gui.backendPanel);

    gui.centerPane.addFocusListener(gui.guiActionListener);

    gui.centerPane.addTab(GuiTranslate.get("gui.panel.jmx"),
        GuiIcons.JMX_PANEL_ICON, gui.jmxPanel);

    gui.centerPane.addTab(GuiTranslate.get("gui.panel.xml"),
        GuiIcons.XML_PANEL_ICON, gui.xmlScroll);
    gui.centerPane.addTab(GuiTranslate.get("gui.panel.info"),
        GuiIcons.INFO_PANEL_ICON, gui.infoScroll);
    gui.centerPane.addTab(GuiTranslate.get("gui.panel.logging"),
        GuiIcons.LOGGING_PANEL_ICON, gui.loggingScroll);
    gui.centerPane.addTab(GuiTranslate.get("gui.panel.log.config"),
        GuiIcons.LOG_CONFIG_PANEL_ICON, gui.logConfigScroll);
    gui.getContentPane().add(gui.centerPane, BorderLayout.CENTER);
    gui.centerPane.addTab(GuiTranslate.get("gui.panel.debug"),
        GuiIcons.DEBUG_PANEL_ICON, gui.debugScroll);
    gui.centerPane.addTab(GuiTranslate.get("gui.panel.help"),
        GuiIcons.HELP_PANEL_ICON, gui.helpScroll);
  }

  // private void defineRightPane()
  // {
  // ///////////////////////////////////////////////////////////////////////////
  // // Define right panel
  // ///////////////////////////////////////////////////////////////////////////
  // JPanel rightPane = new JPanel(new BorderLayout());
  // rightPane.setSize(150, 480);
  // JTextPane rightTextPane = new JTextPane();
  // Font rightPaneFont = new Font("Verdana", Font.PLAIN, 9);
  // rightTextPane.setFont(rightPaneFont);
  // rightTextPane.setForeground(Color.red);
  // rightPane.add(rightTextPane, BorderLayout.CENTER);
  // gui.parsingThread.setOutputPane(rightTextPane);
  // gui.getContentPane().add(rightPane, BorderLayout.EAST);
  // }
}