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
 * Contributor(s): Emmanuel Cecchet
 */

package org.objectweb.cjdbc.console.monitoring;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PropertyResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.objectweb.cjdbc.common.exceptions.DataCollectorException;
import org.objectweb.cjdbc.common.i18n.MonitorTranslate;
import org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean;
import org.objectweb.cjdbc.common.jmx.mbeans.DataCollectorMBean;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.common.monitor.AbstractDataCollector;
import org.objectweb.cjdbc.common.monitor.DataCollection;
import org.objectweb.cjdbc.common.monitor.DataCollectionNames;
import org.objectweb.cjdbc.console.jmx.RmiJmxClient;

/**
 * New Monitoring Console bootstrap for starting stopping monitoring graphs
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class MonitoringConsole extends JFrame
    implements
      MouseListener,
      ActionListener,
      WindowListener
{
  // Window parameters
  private int                  frameWidth             = 400;
  private int                  frameHeight            = 700;

  // Swing Components
  private JLabel               label;
  private JScrollPane          jScroll;

  // MBean components
  private ControllerMBean      controllerMBean;
  private VirtualDatabaseMBean virtualDatabaseMBean;
  private DataCollectorMBean   dataCollectorMBean;

  // Menu components
  private static final String  GRAPH_CONTROLLER       = MonitorTranslate
                                                          .get("heading.controller");
  private static final String  GRAPH_VIRTUAL_DATABASE = MonitorTranslate
                                                          .get("heading.virtualdatabase");
  private static final String  GRAPH_CACHE            = MonitorTranslate
                                                          .get("heading.cache");
  private static final String  GRAPH_SCHEDULER        = MonitorTranslate
                                                          .get("heading.scheduler");
  private static final String  GRAPH_BACKENDS         = MonitorTranslate
                                                          .get("heading.backends");

  private Hashtable            windows                = new Hashtable();

  private Vector               comboBoxesItems        = new Vector();
  private Hashtable            comboBoxes             = new Hashtable();

  // Fonts & Colors
  private Font                 boxFont                = new Font("Arial",
                                                          Font.PLAIN, 10);
  private Font                 labelFont              = new Font("Arial",
                                                          Font.BOLD, 10);
  private Font                 headerFont             = new Font("Arial",
                                                          Font.BOLD, 12);
  private Color                toolBarColor           = Color.white;

  // Graph options
  private int                  graphRepeat            = -1;
  private int                  graphTimeframe         = 3600;
  private int                  graphFrequency         = 1000;
  private int                  graphDisplayFrequency  = 1;

  // Actions
  private static final String  COMMAND_SAVE           = MonitorTranslate
                                                          .get("command.save");
  private static final String  COMMAND_LOAD           = MonitorTranslate
                                                          .get("command.load");
  private static final String  COMMAND_CLOSE_GRAPHS   = MonitorTranslate
                                                          .get("command.close.all");
  private static final String  COMMAND_OPTIONS        = MonitorTranslate
                                                          .get("command.set.options");
  private static final String  COMMAND_CLOSE          = MonitorTranslate
                                                          .get("command.quit");
  private static final String  COMMAND_REFRESH        = MonitorTranslate
                                                          .get("command.refresh");

  private static final String  OPTIONS_APPLY          = "OptionsApply";
  private static final String  OPTIONS_CANCEL         = "OptionsCancel";

  // Combox
  private static final String  COMBO_HIDE             = MonitorTranslate
                                                          .get("command.hide");
  private static final String  COMBO_FLOATING         = MonitorTranslate
                                                          .get("command.float");

  private boolean              isLoading              = false;

  // Option window
  private JFrame               options;
  private JTextField           ftimeframe;
  private JTextField           ffrequency;
  private JTextField           frepeat;
  private JTextField           displayBuffer;

  private boolean              displayController;
  private boolean              displayVirtualDatabase;
  private boolean              displayBackends;

  /**
   * Creates a new <code>MonitoringConsole</code> object
   * 
   * @param jmxUrl JMX URL
   * @param controllerMBean controller MBean if controller monitoring must be
   *          activated
   * @param virtualDatabaseMBean virtual database MBean if virtual database
   *          monitoring must be activated
   * @param backends display backends monitoring menu
   * @throws IOException if an error occurs
   */
  public MonitoringConsole(String jmxUrl, ControllerMBean controllerMBean,
      VirtualDatabaseMBean virtualDatabaseMBean, boolean backends)
      throws IOException
  {
    super(MonitorTranslate.get("monitor.frame.title", jmxUrl));

    this.displayController = controllerMBean == null;
    this.displayVirtualDatabase = virtualDatabaseMBean == null;
    this.displayBackends = backends;

    // Get MBeans reference
    RmiJmxClient jmxClient = new RmiJmxClient(jmxUrl, null);
    dataCollectorMBean = jmxClient.getDataCollectorProxy();
    this.controllerMBean = controllerMBean;
    this.virtualDatabaseMBean = virtualDatabaseMBean;

    // Get options for combo boxes
    comboBoxesItems.add(COMBO_HIDE);
    comboBoxesItems.add(COMBO_FLOATING);

    Toolkit toolkit;
    Dimension dim;
    int screenHeight, screenWidth;

    // Initialize basic layout properties
    setForeground(Color.white);
    getContentPane().setLayout(new BorderLayout());

    // Set the frame's display to be WIDTH x HEIGHT in the middle of the screen
    toolkit = Toolkit.getDefaultToolkit();
    dim = toolkit.getScreenSize();
    screenHeight = dim.height;
    screenWidth = dim.width;

    // Reduce height by two if display only controller
    if (displayController && (!displayVirtualDatabase) && (!displayBackends))
      frameHeight = 270;

    setBounds((screenWidth - frameWidth) / 2, (screenHeight - frameHeight) / 2,
        frameWidth, frameHeight);

    try
    {
      // Init Frame with dynamic content
      initConsole();
    }
    catch (Exception e)
    {
      throw new IOException(e.getMessage());
    }

    // Status Bar
    label = new JLabel("Select Graphs ...");
    label.setFont(labelFont);
    getContentPane().add(label, BorderLayout.SOUTH);
    getContentPane().setBackground(toolBarColor);
    getContentPane().setForeground(toolBarColor);

    // Menu Bar
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu(MonitorTranslate.get("monitor.menu.bar"));
    JMenuItem item1 = new JMenuItem(COMMAND_SAVE);
    JMenuItem item2 = new JMenuItem(COMMAND_LOAD);
    JMenuItem item3 = new JMenuItem(COMMAND_CLOSE);
    JMenuItem item4 = new JMenuItem(COMMAND_CLOSE_GRAPHS);
    JMenuItem item5 = new JMenuItem(COMMAND_OPTIONS);
    JMenuItem item6 = new JMenuItem(COMMAND_REFRESH);
    item1.setBackground(toolBarColor);
    item2.setBackground(toolBarColor);
    item3.setBackground(toolBarColor);
    item4.setBackground(toolBarColor);
    item5.setBackground(toolBarColor);
    item6.setBackground(toolBarColor);
    menu.add(item1).addActionListener(this);
    menu.add(item2).addActionListener(this);
    menu.add(item4).addActionListener(this);
    menu.add(item5).addActionListener(this);
    menu.add(item6).addActionListener(this);
    menu.addSeparator();
    menu.add(item3).addActionListener(this);
    menu.setVisible(true);
    menu.setBackground(toolBarColor);
    menuBar.add(menu);
    menuBar.setBackground(toolBarColor);
    this.setJMenuBar(menuBar);

    // Prepare options window
    options = new JFrame(MonitorTranslate.get("options.frame.title"));
    options.setSize(200, 200);
    JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout(new GridLayout(5, 2));
    optionsPanel.add(new JLabel(MonitorTranslate.get("options.repeat")));
    frepeat = new JTextField(0);
    frepeat.setAlignmentX(RIGHT_ALIGNMENT);
    frepeat.setText(graphRepeat + "");
    frepeat.addActionListener(this);
    optionsPanel.add(frepeat);

    optionsPanel
        .add(new JLabel(MonitorTranslate.get("options.display.buffer")));
    displayBuffer = new JTextField(0);
    displayBuffer.setText("" + graphDisplayFrequency);
    displayBuffer.addActionListener(this);
    displayBuffer.setAlignmentX(RIGHT_ALIGNMENT);
    optionsPanel.add(displayBuffer);

    optionsPanel.add(new JLabel(MonitorTranslate.get("options.frequency")));
    ffrequency = new JTextField(0);
    ffrequency.setText("" + graphFrequency);
    ffrequency.addActionListener(this);
    ffrequency.setAlignmentX(RIGHT_ALIGNMENT);
    optionsPanel.add(ffrequency);

    optionsPanel.add(new JLabel(MonitorTranslate.get("options.timeframe")));
    ftimeframe = new JTextField(0);
    ftimeframe.setText(graphTimeframe + "");
    ftimeframe.addActionListener(this);
    ftimeframe.setAlignmentX(RIGHT_ALIGNMENT);
    optionsPanel.add(ftimeframe);

    JButton optionConfirm = new JButton(MonitorTranslate.get("options.ok"));
    optionConfirm.setActionCommand(OPTIONS_APPLY);
    optionConfirm.addActionListener(this);
    optionsPanel.add(optionConfirm);

    JButton optionCancel = new JButton(MonitorTranslate.get("options.cancel"));
    optionCancel.setActionCommand(OPTIONS_CANCEL);
    optionCancel.addActionListener(this);
    optionsPanel.add(optionCancel);

    options.getContentPane().add(optionsPanel);
    options.setVisible(false);
    options.setDefaultCloseOperation(HIDE_ON_CLOSE);
    options.validate();

    //Put the final touches to the JFrame object
    validate();
    setVisible(true);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  private void initConsole() throws Exception
  {
    // Get the menus with all commands
    this.setVisible(false);
    BorderLayout layout = new BorderLayout();
    JPanel pane = new JPanel(layout);
    if (displayController)
      pane.add(initializeControllerBar(), BorderLayout.NORTH);
    if (displayVirtualDatabase)
      pane.add(initializaDatabaseBar(displayBackends), BorderLayout.CENTER);
    jScroll = new JScrollPane();
    jScroll.getViewport().add(pane);
    getContentPane().add(jScroll, BorderLayout.CENTER);

    validate();
    this.setVisible(true);
  }

  private JLabel getHeaderLabel(String text)
  {
    JLabel headerLabel = new JLabel(text);
    headerLabel.setFont(headerFont);
    headerLabel.setAlignmentX(CENTER_ALIGNMENT);
    return headerLabel;
  }

  private JLabel getSubHeaderLabel(String text)
  {
    JLabel subHeaderLabel = new JLabel(text);
    subHeaderLabel.setFont(labelFont);
    subHeaderLabel.setAlignmentX(CENTER_ALIGNMENT);
    return subHeaderLabel;
  }

  private JToolBar initializeControllerBar()
  {
    JToolBar toolbar = new JToolBar(GRAPH_CONTROLLER, SwingConstants.VERTICAL);
    toolbar.setOrientation(SwingConstants.VERTICAL);
    toolbar.addMouseListener(this);
    toolbar.add(getHeaderLabel(GRAPH_CONTROLLER));
    toolbar
        .add(getGraphMenuItem(DataCollection.CONTROLLER_USED_MEMORY, "", ""));
    toolbar.add(getGraphMenuItem(DataCollection.CONTROLLER_THREADS_NUMBER, "",
        ""));
    toolbar.add(getGraphMenuItem(
        DataCollection.CONTROLLER_WORKER_PENDING_QUEUE, "", ""));
    toolbar.add(getGraphMenuItem(DataCollection.CONTROLLER_IDLE_WORKER_THREADS,
        "", ""));
    toolbar.setVisible(true);
    return toolbar;
  }

  private JToolBar initializeCacheBar(String vdb)
  {
    JToolBar toolbar = new JToolBar(GRAPH_CACHE, SwingConstants.VERTICAL);

    try
    {
      if (virtualDatabaseMBean.hasResultCache() == false)
        toolbar.setEnabled(false);
      else
      {
        toolbar.add(getSubHeaderLabel(GRAPH_CACHE + " [" + vdb + "]"));
        toolbar.add(getGraphMenuItem(DataCollection.CACHE_STATS_COUNT_HITS,
            vdb, ""));
        toolbar.add(getGraphMenuItem(DataCollection.CACHE_STATS_COUNT_INSERT,
            vdb, ""));
        toolbar.add(getGraphMenuItem(DataCollection.CACHE_STATS_COUNT_SELECT,
            vdb, ""));
        toolbar.add(getGraphMenuItem(
            DataCollection.CACHE_STATS_HITS_PERCENTAGE, vdb, ""));
        toolbar.add(getGraphMenuItem(DataCollection.CACHE_STATS_NUMBER_ENTRIES,
            vdb, ""));
      }
    }
    catch (Exception e)
    {
      toolbar.setEnabled(false);
    }
    return toolbar;
  }

  private JToolBar initializeSchedulerBar(String vdb)
  {
    JToolBar toolbar = new JToolBar(GRAPH_SCHEDULER, SwingConstants.VERTICAL);
    toolbar.add(getSubHeaderLabel(GRAPH_SCHEDULER + " [" + vdb + "]"));
    toolbar
        .add(getGraphMenuItem(DataCollection.SCHEDULER_NUMBER_READ, vdb, ""));
    toolbar.add(getGraphMenuItem(DataCollection.SCHEDULER_NUMBER_REQUESTS, vdb,
        ""));
    toolbar.add(getGraphMenuItem(DataCollection.SCHEDULER_NUMBER_WRITES, vdb,
        ""));
    toolbar.add(getGraphMenuItem(DataCollection.SCHEDULER_PENDING_TRANSACTIONS,
        vdb, ""));
    toolbar.add(getGraphMenuItem(DataCollection.SCHEDULER_PENDING_WRITES, vdb,
        ""));
    return toolbar;
  }

  private JToolBar initializaBackendBar(String vdb, String backendName)
  {
    JToolBar backendMenu = new JToolBar(GRAPH_BACKENDS, SwingConstants.VERTICAL);
    backendMenu.add(getSubHeaderLabel(GRAPH_BACKENDS + " [" + backendName
        + " on " + vdb + "]"));
    backendMenu.add(getGraphMenuItem(DataCollection.BACKEND_ACTIVE_TRANSACTION,
        vdb, backendName));
    backendMenu.add(getGraphMenuItem(DataCollection.BACKEND_PENDING_REQUESTS,
        vdb, backendName));
    backendMenu.add(getGraphMenuItem(
        DataCollection.BACKEND_TOTAL_ACTIVE_CONNECTIONS, vdb, backendName));
    backendMenu.add(getGraphMenuItem(DataCollection.BACKEND_TOTAL_REQUEST, vdb,
        backendName));
    backendMenu.add(getGraphMenuItem(DataCollection.BACKEND_TOTAL_READ_REQUEST,
        vdb, backendName));
    backendMenu.add(getGraphMenuItem(
        DataCollection.BACKEND_TOTAL_WRITE_REQUEST, vdb, backendName));
    backendMenu.add(getGraphMenuItem(DataCollection.BACKEND_TOTAL_TRANSACTIONS,
        vdb, backendName));
    return backendMenu;
  }

  private JToolBar initializaDatabaseBar(boolean dispBackends) throws Exception
  {
    JToolBar toolbar = new JToolBar(GRAPH_VIRTUAL_DATABASE, JToolBar.VERTICAL);
    toolbar.addMouseListener(this);

    ArrayList dbs = controllerMBean.getVirtualDatabaseNames();
    ArrayList backends;
    String vdb = null;
    for (int i = 0; i < dbs.size(); i++)
    {
      vdb = (String) dbs.get(i);
      toolbar.add(getHeaderLabel(GRAPH_VIRTUAL_DATABASE + " [" + vdb + "]"));
      // Virtual Database main graphs
      toolbar.add(getGraphMenuItem(DataCollection.DATABASES_ACTIVE_THREADS,
          vdb, ""));
      toolbar.add(getGraphMenuItem(DataCollection.DATABASES_NUMBER_OF_THREADS,
          vdb, ""));
      toolbar.add(getGraphMenuItem(
          DataCollection.DATABASES_PENDING_CONNECTIONS, vdb, ""));
      // Cache
      toolbar.add(initializeCacheBar(vdb));
      // Scheduler
      toolbar.add(initializeSchedulerBar(vdb));

      if (dispBackends)
      { // Backends
        backends = virtualDatabaseMBean.getAllBackendNames();
        for (int j = 0; j < backends.size(); j++)
        {
          String backendName = (String) backends.get(j);
          toolbar.add(initializaBackendBar(vdb, backendName));
        }
      }
    }
    return toolbar;
  }

  private JComponent getGraphMenuItem(int type, String virtualDbName,
      String targetName)
  {
    String name = DataCollectionNames.get(type);
    JComboBox item = new JComboBox(comboBoxesItems);
    item.setFont(boxFont);
    item.setName(name);
    item.addActionListener(this);
    item.addMouseListener(this);
    if (virtualDbName == null)
      virtualDbName = "";
    if (targetName == null)
      targetName = "";
    String actionCommand = getBackendActionCommand(name, virtualDbName,
        targetName);
    actionCommand = actionCommand.trim();
    item.setActionCommand(actionCommand);
    item.setVisible(true);
    BorderLayout layout = new BorderLayout();
    JPanel panel = new JPanel(layout);
    JLabel nameLabel = new JLabel(name);
    nameLabel.setAlignmentX(CENTER_ALIGNMENT);
    nameLabel.setFont(labelFont);
    panel.add(nameLabel, BorderLayout.WEST);
    panel.add(item, BorderLayout.EAST);

    comboBoxes.put(actionCommand, item);
    return panel;
  }

  /**
   * Get the backend action command for displaying monitoring window
   * 
   * @param typeName type of info to monitor
   * @param vdbName database name
   * @param backendName backend name
   * @return <code>String</code> describing the command
   */
  public static String getBackendActionCommand(String typeName, String vdbName,
      String backendName)
  {
    return "graph " + typeName.toLowerCase().replace(' ', '_') + " " + vdbName
        + " " + backendName;
  }

  /**
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e)
  {
    status(e.getComponent().getName() + " was clicked");
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

  private void doSaveConfiguration() throws IOException
  {
    Iterator iter = comboBoxes.keySet().iterator();
    File f = new File("monitor.properties");
    BufferedOutputStream bof = new BufferedOutputStream(new FileOutputStream(f));
    String temp;
    while (iter.hasNext())
    {
      Object o = iter.next();
      String key = o.toString().trim().replace(' ', '.');
      JComboBox box = (JComboBox) comboBoxes.get(o);
      temp = (key + "=" + box.getSelectedItem())
          + System.getProperty("line.separator");
      bof.write(temp.getBytes());
    }

    Iterator iter2 = windows.keySet().iterator();
    Window win;
    Point p;
    String name, winX, winY;
    while (iter2.hasNext())
    {
      win = ((Window) windows.get(iter2.next()));
      p = win.getLocation();
      name = win.getName().trim().replace(' ', '.');
      winX = name + ".X=" + (int) p.getX()
          + System.getProperty("line.separator");
      winY = name + ".Y=" + (int) p.getY()
          + System.getProperty("line.separator");
      bof.write(winX.getBytes());
      bof.write(winY.getBytes());
    }
    bof.write(("options.repeat=" + graphRepeat + System
        .getProperty("line.separator")).getBytes());
    bof.write(("options.timeframe=" + graphTimeframe + System
        .getProperty("line.separator")).getBytes());
    bof.write(("options.frequency=" + graphFrequency + System
        .getProperty("line.separator")).getBytes());
    bof.write(("options.displayfrequency=" + graphDisplayFrequency + System
        .getProperty("line.separator")).getBytes());

    bof.flush();
    bof.close();
  }

  private void doLoadConfiguration() throws IOException
  {
    closeAllWindows();
    isLoading = true;
    File f = new File("monitor.properties");
    PropertyResourceBundle props = new PropertyResourceBundle(
        new FileInputStream(f));
    Enumeration enume = props.getKeys();
    String key = "", keyr = "", value = "";
    JFrame frame;

    try
    {
      graphRepeat = Integer
          .parseInt((String) props.getObject("options.repeat"));
      frepeat.setText("" + graphRepeat);
      graphFrequency = Integer.parseInt((String) props
          .getObject("options.frequency"));
      ffrequency.setText("" + graphFrequency);
      graphTimeframe = Integer.parseInt((String) props
          .getObject("options.timeframe"));
      ftimeframe.setText("" + graphTimeframe);
      graphDisplayFrequency = Integer.parseInt((String) props
          .getObject("options.displayfrequency"));
      displayBuffer.setText("" + graphDisplayFrequency);
    }
    catch (Exception e)
    {
      error(e.getMessage());
    }

    while (enume.hasMoreElements())
    {
      key = (String) enume.nextElement();
      value = (String) props.getObject(key);
      if (key.startsWith("options"))
      {
        // done
      }
      else if (key.endsWith(".X") || key.endsWith(".Y"))
      {
        // do nothing
      }
      else
      {
        if (value.equals(COMBO_FLOATING))
          try
          {
            frame = graph(key);
            keyr = key.trim().replace('.', ' ');
            //System.out.println(key);
            JComboBox box = (JComboBox) comboBoxes.get(keyr);
            if (box != null)
              box.setSelectedItem(COMBO_FLOATING);
            try
            {
              int x = Integer.parseInt((String) props.getObject(key + ".X"));
              int y = Integer.parseInt((String) props.getObject(key + ".Y"));

              //Window win = ((Window) windows.get(keyr));
              //System.out.println(frame.getName()+"-"+x+"-"+y);
              frame.setLocation(x, y);
              //win.getComponent(0).setLocation(x, y);
              //win.setVisible(true);
              //win.validate();
            }
            catch (Exception e)
            {
              // ignore does not exist
              error(e.getMessage());
            }
          }
          catch (Exception e)
          {
            // cannot load ...
          }
      }
    }
    isLoading = false;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    if (isLoading)
      return;
    String actionCommand = e.getActionCommand();
    status("Action:" + actionCommand);
    if (actionCommand.equals(COMMAND_SAVE))
    {
      try
      {
        doSaveConfiguration();
      }
      catch (IOException e1)
      {
        // TODO: display dialog box
        //        Dialog d = new Dialog(this, "Saving Failed");
        //        d.add(new JLabel("Saving Failed because of:" + e1.getMessage()));
        //        d.setVisible(true);
        //        d.setSize(100,50);
        //        d.setModal(true);
        //d.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        e1.printStackTrace();
      }
    }
    else if (actionCommand.equals(COMMAND_LOAD))
    {
      try
      {
        doLoadConfiguration();
      }
      catch (IOException e1)
      {
        // TODO: display dialog box
        e1.printStackTrace();
      }
    }
    else if (actionCommand.equals(OPTIONS_APPLY))
    {

      try
      {
        graphRepeat = Integer.parseInt(frepeat.getText());
        graphTimeframe = Integer.parseInt(ftimeframe.getText());
        graphFrequency = Integer.parseInt(ffrequency.getText());
        graphDisplayFrequency = Integer.parseInt(displayBuffer.getText());
        options.setVisible(false);
      }
      catch (Exception failed)
      {
        error(failed.getMessage());
      }
    }
    else if (actionCommand.equals(OPTIONS_CANCEL))
    {
      options.setVisible(false);
      frepeat.setText(graphRepeat + "");
      ftimeframe.setText(graphTimeframe + "");
      ffrequency.setText(graphFrequency + "");
    }
    else if (actionCommand.equals(COMMAND_REFRESH))
    {
      try
      {
        initConsole();
      }
      catch (Exception error)
      {
        error(error.getMessage());
      }
    }
    else if (actionCommand.equals(COMMAND_OPTIONS))
    {
      options.setVisible(true);
    }
    else if (actionCommand.equals(COMMAND_CLOSE))
    {
      closeAllWindows();
      this.dispose();
    }
    else if (actionCommand.equals(COMMAND_CLOSE_GRAPHS))
    {
      closeAllWindows();
    }
    Object o = e.getSource();
    if (o instanceof JComboBox)
    {
      JComboBox box = (JComboBox) o;
      String selected = (String) box.getSelectedItem();
      status(selected.toString());
      try
      {
        Window win = (Window) windows.get(e.getActionCommand().trim());
        if (!selected.equals(COMBO_HIDE))
        {
          if (win == null)
          {
            graph(e.getActionCommand());
          }
        }
        else
        {
          windows.remove(win.getName());
          win.setVisible(false);
          win.dispose();
        }
      }
      catch (Exception f)
      {
        error(f.getMessage());
      }
    }

  }

  private void closeAllWindows()
  {
    while (true)
    {
      try
      {
        Iterator iter = windows.keySet().iterator();
        while (iter.hasNext())
        {
          Window win = ((Window) windows.get(iter.next()));
          JComboBox box = (JComboBox) comboBoxes.get(win.getName());
          if (box != null)
            box.setSelectedItem(COMBO_HIDE);
        }
        break;
      }
      catch (RuntimeException e)
      {
        //concurrent modification exception
        continue;
      }
    }
  }

  private void status(String message)
  {
    label.setBackground(Color.white);
    label.setText(message);
  }

  private void error(String message)
  {
    label.setBackground(Color.red);
    label.setText(message);
  }

  /**
   * Starts a new graph
   * 
   * @param command command line
   * @throws DataCollectorException if fails
   */
  private JFrame graph(String command) throws DataCollectorException
  {

    return graph(command, dataCollectorMBean, graphRepeat, graphTimeframe,
        graphFrequency, graphDisplayFrequency, this);
  }

  /**
   * Starts a graph !
   * 
   * @param command graph command
   * @param dataCollectorMBean jmx client to get info from
   * @param graphRepeat parameter
   * @param graphTimeframe parameter
   * @param graphFrequency parameter
   * @param graphDisplayFrequency parameter
   * @param listener to receive updates
   * @return <code>JFrame</code> containing the monitoring window
   * @throws DataCollectorException if an error occurs
   */
  public static final JFrame graph(String command,
      DataCollectorMBean dataCollectorMBean, int graphRepeat,
      int graphTimeframe, int graphFrequency, int graphDisplayFrequency,
      WindowListener listener) throws DataCollectorException
  {
    // Used for saving configuration
    command = command.replace('.', ' ');

    StringTokenizer tokenizer = new StringTokenizer(command, " ");
    String token0 = tokenizer.nextToken();
    if (token0.equals("graph"))
    {
      String token1 = tokenizer.nextToken();
      int type = DataCollectionNames.getTypeFromCommand(token1);
      String token2 = (tokenizer.hasMoreTokens()) ? tokenizer.nextToken() : "";
      String token3 = (tokenizer.hasMoreTokens()) ? tokenizer.nextToken() : "";
      AbstractDataCollector collector = dataCollectorMBean
          .retrieveDataCollectorInstance(type, token3, token2);
      MonitoringGraph graph = new MonitoringGraph(collector, dataCollectorMBean);
      graph.setRepeat(graphRepeat);
      graph.setTimeFrame(graphTimeframe);
      graph.setFrequency(graphFrequency);
      graph.setDisplayFrequency(graphDisplayFrequency);
      graph.start();
      graph.setText(command);
      // Do not do before, as frame is null before starts of thread!
      if (listener != null)
        graph.getFrame().addWindowListener(listener);
      graph.getFrame().setName(command.trim());
      return graph.getFrame();
    }
    else
    {
      return null;
    }
  }

  /**
   * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
   */
  public void windowActivated(WindowEvent e)
  {

  }

  /**
   * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
   */
  public void windowClosed(WindowEvent e)
  {

  }

  /**
   * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
   */
  public void windowClosing(WindowEvent e)
  {
    Window win = e.getWindow();
    JComboBox box = (JComboBox) comboBoxes.get(win.getName());
    windows.remove(win.getName());
    if (box != null)
      box.setSelectedIndex(0);
    status(win.getName() + " is closing");
  }

  /**
   * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
   */
  public void windowDeactivated(WindowEvent e)
  {

  }

  /**
   * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
   */
  public void windowDeiconified(WindowEvent e)
  {

  }

  /**
   * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
   */
  public void windowIconified(WindowEvent e)
  {

  }

  /**
   * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
   */
  public void windowOpened(WindowEvent e)
  {
    Window win = e.getWindow();
    status(win.getName() + " has opened");
    windows.put(win.getName(), e.getWindow());
  }
}