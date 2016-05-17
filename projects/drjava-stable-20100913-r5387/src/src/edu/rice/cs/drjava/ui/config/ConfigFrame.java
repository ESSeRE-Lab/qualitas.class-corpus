/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.io.IOException;
import java.io.File;
import java.util.TreeMap;
import java.util.Iterator;

import javax.swing.tree.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.ui.KeyBindingManager.KeyStrokeData;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.util.swing.DirectoryChooser;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.SwingWorker;
import edu.rice.cs.util.swing.ProcessingDialog;
import edu.rice.cs.plt.lambda.Runnable1;

/** The frame for setting Configuration options on the fly
 *  @version $Id: ConfigFrame.java 5363 2010-08-14 04:07:34Z mgricken $
 */
public class ConfigFrame extends SwingFrame {

  private static final int FRAME_WIDTH = 850;
  private static final int FRAME_HEIGHT = 550;

  private final MainFrame _mainFrame;

//  private JSplitPane _splitPane;
  private final JTree _tree;
  private final DefaultTreeModel _treeModel;
  private final PanelTreeNode _rootNode;

  private final JButton _okButton;
  private final JButton _applyButton;
  private final JButton _cancelButton;
//  private final JButton _saveSettingsButton;
  private final JPanel _mainPanel;
  private final JFileChooser _fileOptionChooser;
  private final JFileChooser _browserChooser;
  private final JFileChooser _jarChooser;
  private final DirectoryChooser _dirChooser;
  private final ConfigOptionListeners.RequiresInteractionsRestartListener<Boolean> _junitLocationEnabledListener;
  private final ConfigOptionListeners.RequiresInteractionsRestartListener<File> _junitLocationListener;
  private final ConfigOptionListeners.RequiresInteractionsRestartListener<String> _concJUnitChecksEnabledListener;
  private final ConfigOptionListeners.RequiresInteractionsRestartListener<File> _rtConcJUnitLocationListener;
    
  private StringOptionComponent javadocCustomParams;
  
  protected final String SEPS = " \t\n-,;.(";
  
  private OptionComponent.ChangeListener _changeListener = new OptionComponent.ChangeListener() {
    public Object value(Object oc) {
      _applyButton.setEnabled(true);
      return null;
    }
  };
  
  /** Sets up the frame and displays it.  This a Swing view class!  With the exception of initialization,
   *  this code should only be executed in the event-handling thread. */
  public ConfigFrame(MainFrame frame) {
    super("Preferences");

    _mainFrame = frame;
    _junitLocationEnabledListener = new ConfigOptionListeners.
      RequiresInteractionsRestartListener<Boolean>(this, "Use External JUnit");
    _junitLocationListener = new ConfigOptionListeners.
      RequiresInteractionsRestartListener<File>(this, "JUnit Location");
    _concJUnitChecksEnabledListener = new ConfigOptionListeners.
      RequiresInteractionsRestartListener<String>(this, "Enabled ConcJUnit Checks");
    _rtConcJUnitLocationListener = new ConfigOptionListeners.
      RequiresInteractionsRestartListener<File>(this, "ConcJUnit Runtime Location");
    
    Action applyAction = new AbstractAction("Apply") {
      public void actionPerformed(ActionEvent e) {
        // Always save settings
        try {
//          _mainFrame.enableResetInteractions();
          saveSettings(); 
          _applyButton.setEnabled(false); 
          
        }
        catch (IOException ioe) {
        }
      }
    };

    _applyButton = new JButton(applyAction);
    _applyButton.setEnabled(false);
    
    Action okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) {
        // Always apply and save settings
        boolean successful = true;
        try {
//          _mainFrame.enableResetInteractions();
          successful = saveSettings();
        }
        catch (IOException ioe) {
          // oh well...
        }
        if (successful) _applyButton.setEnabled(false);
        ConfigFrame.this.setVisible(false);
      }
    };
    _okButton = new JButton(okAction);


    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    };
    _cancelButton = new JButton(cancelAction);

    File workDir = _getWorkDir();
    /* Following line was inserted becuase the statement below it would occasionally cause swing to throw a
    NullPointerException. workDir == null is supposed to be impossible. */
    if (workDir == null || workDir == FileOps.NULL_FILE) workDir = new File(System.getProperty("user.dir"));
    _fileOptionChooser = new JFileChooser(workDir);
    _jarChooser = new JFileChooser(workDir);
    _browserChooser = new JFileChooser(workDir);
    _dirChooser = new DirectoryChooser(this);
  
    /* Create tree and initialize tree. */
    _rootNode = new PanelTreeNode("Preferences");
    _treeModel = new DefaultTreeModel(_rootNode);
    _tree = new JTree(_treeModel);
    
    _initTree();
    
    /* Create Panels. */
    _createPanels();

    _mainPanel= new JPanel();
    _mainPanel.setLayout(new BorderLayout());
    _tree.addTreeSelectionListener(new PanelTreeSelectionListener());

    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());

    // Select the first panel by default
    if (_rootNode.getChildCount() != 0) {
      PanelTreeNode firstChild = (PanelTreeNode)_rootNode.getChildAt(0);
      TreeNode[] firstChildPath = firstChild.getPath();
      TreePath path = new TreePath(firstChildPath);
      _tree.expandPath(path);
      _tree.setSelectionPath(path);
    }

    JScrollPane treeScroll = new JScrollPane(_tree);
    JPanel treePanel = new JPanel();
    treePanel.setLayout(new BorderLayout());
    treeScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Categories"));
    treePanel.add(treeScroll, BorderLayout.CENTER);
    cp.add(treePanel, BorderLayout.WEST);
    cp.add(_mainPanel, BorderLayout.CENTER);

    // Add buttons
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5,5,5,5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    //bottom.add(_saveSettingsButton);
    //bottom.add(Box.createHorizontalGlue());
    bottom.add(_applyButton);
    bottom.add(_okButton);
    bottom.add(_cancelButton);
    bottom.add(Box.createHorizontalGlue());

    cp.add(bottom, BorderLayout.SOUTH);

    // Set all dimensions ----
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    if (dim.width>FRAME_WIDTH) { dim.width = FRAME_WIDTH; }
    else { dim.width -= 80; }
    if (dim.height>FRAME_HEIGHT) { dim.height = FRAME_HEIGHT; }
    else { dim.height -= 80; }
    setSize(dim);

    _mainFrame.setPopupLoc(this);

    // Make sure each row is expanded
    int row = 0;
    while(row<_tree.getRowCount()) {
      _tree.expandRow(row);
      ++row;
    }
    
    initDone(); // call mandated by SwingFrame contract
  }
  
  /** Performs deferred initialization.  Only runs in the event thread.  Some of this code occasionally generated swing
   *  exceptions  when run in themain thread as part of MainFrame construction prior to making MainFrame visible. */
  public void setUp() {
    assert EventQueue.isDispatchThread();
    /* Set up _fileOptionChooser, _browserChooser, and _dirChooser.  The line _dirChooser.setSelectedFile(...) caused
     * java.lang.ArrayIndexOutOfBoundsException within swing code in a JUnit test setUp() routine that constructed a
     * a MainFrame.
     */

    _fileOptionChooser.setDialogTitle("Select");
    _fileOptionChooser.setApproveButtonText("Select");
    _fileOptionChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    _fileOptionChooser.setFileFilter(ClassPathFilter.ONLY);
    
    _jarChooser.setDialogTitle("Select");
    _jarChooser.setApproveButtonText("Select");
    _jarChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    _jarChooser.setFileFilter(ClassPathFilter.ONLY);
    
    _browserChooser.setDialogTitle("Select Web Browser");
    _browserChooser.setApproveButtonText("Select");
    _browserChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    
    _dirChooser.setSelectedFile(_getWorkDir());
    _dirChooser.setDialogTitle("Select");
    _dirChooser.setApproveButtonText("Select");
    _dirChooser.setMultiSelectionEnabled(false);
  }
  
  private void enableChangeListeners() {
    DrJava.getConfig().addOptionListener(OptionConstants.JUNIT_LOCATION_ENABLED,
                                         _junitLocationEnabledListener);
    DrJava.getConfig().addOptionListener(OptionConstants.JUNIT_LOCATION,
                                         _junitLocationListener);
    DrJava.getConfig().addOptionListener(OptionConstants.CONCJUNIT_CHECKS_ENABLED,
                                         _concJUnitChecksEnabledListener);
    DrJava.getConfig().addOptionListener(OptionConstants.RT_CONCJUNIT_LOCATION,
                                         _rtConcJUnitLocationListener);
  }

  private void disableChangeListeners() {
    DrJava.getConfig().removeOptionListener(OptionConstants.JUNIT_LOCATION_ENABLED,
                                            _junitLocationEnabledListener);
    DrJava.getConfig().removeOptionListener(OptionConstants.JUNIT_LOCATION,
                                            _junitLocationListener);
    DrJava.getConfig().removeOptionListener(OptionConstants.CONCJUNIT_CHECKS_ENABLED,
                                            _concJUnitChecksEnabledListener);
    DrJava.getConfig().removeOptionListener(OptionConstants.RT_CONCJUNIT_LOCATION,
                                            _rtConcJUnitLocationListener);
  }

  /** Returns the current master working directory, or the user's current directory if none is set. 20040213 Changed default 
   *  value to user's current directory.
   */
  private File _getWorkDir() {
    File workDir = _mainFrame.getModel().getMasterWorkingDirectory();  // cannot be null
    assert workDir != null;
    if (workDir.isDirectory()) return workDir;
    
    if (workDir.getParent() != null) workDir = workDir.getParentFile();
    return workDir;
  }

  /** Call the update method to propagate down the tree, parsing input values into their config options. */
  public boolean apply() {
    // returns false if the update did not succeed
    return _rootNode.update();
  }

  /** Resets the field of each option in the Preferences window to its actual stored value. */
  public void resetToCurrent() {
    _rootNode.resetToCurrent();
    // must reset the "current keystroke map" when resetting
    VectorKeyStrokeOptionComponent.resetCurrentKeyStrokeMap();
  }

  /** Resets the frame and hides it. */
  public void cancel() {
    resetToCurrent();
    _applyButton.setEnabled(false);
    ConfigFrame.this.setVisible(false);
  }

  /** Thunk that calls _cancel. */
  protected final Runnable1<WindowEvent> CANCEL = new Runnable1<WindowEvent>() {
    public void run(WindowEvent e) { cancel(); }
  };
  
  /** Validates before changing visibility.  Only runs in the event thread.
    * @param vis true if frame should be shown, false if it should be hidden.
    */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    // made modal for now
    if (vis) {
//      _mainFrame.hourglassOn();
//      _mainFrame.installModalWindowAdapter(this, NO_OP, CANCEL);
      enableChangeListeners();
      toFront();
    }
    else {
//      _mainFrame.removeModalWindowAdapter(this);
//      _mainFrame.hourglassOff();
      disableChangeListeners();
      _mainFrame.toFront();
    }
    super.setVisible(vis);
  }

  /** Write the configured option values to disk. */
  public boolean saveSettings() throws IOException {
    boolean successful = apply();
    if (successful) {
      try { DrJava.getConfig().saveConfiguration(); }
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(this,
                                      "Could not save changes to your \".drjava\" file in your home directory. \n\n" + ioe,
                                      "Could Not Save Changes",
                                      JOptionPane.ERROR_MESSAGE);
        //return false;
        throw ioe;
      }
    }
    return successful;
  }

  /** Sets the given ConfigPanel as the visible panel. */
  private void _displayPanel(ConfigPanel cf) {

    _mainPanel.removeAll();
    _mainPanel.add(cf, BorderLayout.CENTER);
    _mainPanel.revalidate();
    _mainPanel.repaint();
  }

  /** Creates the JTree to display preferences categories. */
  private void _initTree() {
    _tree.setEditable(false);
    _tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    _tree.setShowsRootHandles(true);
    _tree.setRootVisible(false);

    DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer();
    dtcr.setLeafIcon(null);
    dtcr.setOpenIcon(null);
    dtcr.setClosedIcon(null);
    _tree.setCellRenderer(dtcr);
  }

  /**Creates an individual panel, adds it to the JTree and the list of panels, and returns the tree node.
   * @param t the title of this panel
   * @param parent the parent tree node
   * @return this tree node
   */
  private PanelTreeNode _createPanel(String t, PanelTreeNode parent) {
    PanelTreeNode ptNode = new PanelTreeNode(t);
    //parent.add(ptNode);
    _treeModel.insertNodeInto(ptNode, parent, parent.getChildCount());

    // Make sure tree node is visible
    TreeNode[] pathArray = ptNode.getPath();
    TreePath path = new TreePath(pathArray);
//     System.out.println("path has class " + pathArray.getClass());
//     System.out.println("last path compenent has class " + path.getLastPathComponent().getClass());
    _tree.expandPath(path);

    return ptNode;
  }

  /** Creates an individual panel, adds it to the JTree and the list of panels, and returns the tree node. Adds to the root node.
   *  @param t the title of this panel
   *  @return this tree node
   */
  private PanelTreeNode _createPanel(String t) { return _createPanel(t, _rootNode); }

  /** Creates all of the panels contained within the frame. */
  private void _createPanels() {

    PanelTreeNode resourceLocNode = _createPanel("Resource Locations");
    _setupResourceLocPanel(resourceLocNode.getPanel());

    PanelTreeNode displayNode = _createPanel("Display Options");
    _setupDisplayPanel(displayNode.getPanel());
    
    PanelTreeNode fontNode = _createPanel("Fonts", displayNode);
    _setupFontPanel(fontNode.getPanel());

    PanelTreeNode colorNode = _createPanel("Colors", displayNode);
    _setupColorPanel(colorNode.getPanel());

    PanelTreeNode positionsNode = _createPanel("Window Positions", displayNode);
    _setupPositionsPanel(positionsNode.getPanel());

    PanelTreeNode keystrokesNode = _createPanel("Key Bindings");
    _setupKeyBindingsPanel(keystrokesNode.getPanel());
    
    PanelTreeNode compilerOptionsNode = _createPanel("Compiler Options");
    _setupCompilerPanel(compilerOptionsNode.getPanel());
    
    PanelTreeNode interactionsNode = _createPanel("Interactions Pane");
    _setupInteractionsPanel(interactionsNode.getPanel());
    
    PanelTreeNode debugNode = _createPanel("Debugger");
    _setupDebugPanel(debugNode.getPanel());

    PanelTreeNode junitNode = _createPanel("JUnit");
    _setupJUnitPanel(junitNode.getPanel());
    
    PanelTreeNode javadocNode = _createPanel("Javadoc");
    _setupJavadocPanel(javadocNode.getPanel());

    PanelTreeNode notificationsNode = _createPanel("Notifications");
    _setupNotificationsPanel(notificationsNode.getPanel());
    
    PanelTreeNode miscNode = _createPanel("Miscellaneous");
    _setupMiscPanel(miscNode.getPanel());
    
    PanelTreeNode fileTypesNode = _createPanel("File Types", miscNode);
    _setupFileTypesPanel(fileTypesNode.getPanel());
    
    PanelTreeNode jvmsNode = _createPanel("JVMs", miscNode);
    _setupJVMsPanel(jvmsNode.getPanel());
    
    // Expand the display options node
    //DrJava.consoleOut().println("expanding path...");
    //_tree.expandPath(new TreePath(jvmsNode.getPath()));
  }

  public <X,C extends JComponent> void addOptionComponent(ConfigPanel panel, OptionComponent<X,C> oc) {
    panel.addComponent(oc);
    oc.addChangeListener(_changeListener);
  }
  
  /** Add all of the components for the Resource Locations panel of the preferences window. */
  private void _setupResourceLocPanel(ConfigPanel panel) {
    FileOptionComponent browserLoc =
      new FileOptionComponent(OptionConstants.BROWSER_FILE, "Web Browser", this,
                              "<html>Location of a web browser to use for Javadoc and Help links.<br>" +
                              "If left blank, only the Web Browser Command will be used.<br>" +
                              "This is not necessary if a default browser is available on your system.",
                              _browserChooser);
    addOptionComponent(panel, browserLoc);    

    StringOptionComponent browserCommand =
      new StringOptionComponent(OptionConstants.BROWSER_STRING, "Web Browser Command", this,
                              "<html>Command to send to the web browser to view a web location.<br>" +
                              "The string <code>&lt;URL&gt;</code> will be replaced with the URL address.<br>" +
                              "This is not necessary if a default browser is available on your system.");
    addOptionComponent(panel, browserCommand);

    FileOptionComponent javacLoc =
      new FileOptionComponent(OptionConstants.JAVAC_LOCATION, "Tools.jar Location", this,
                              "Optional location of the JDK's tools.jar, which contains the compiler and debugger.",
                              _fileOptionChooser);
    javacLoc.setFileFilter(ClassPathFilter.ONLY);
    addOptionComponent(panel, javacLoc);

    BooleanOptionComponent displayAllCompilerVersions =
      new BooleanOptionComponent(OptionConstants.DISPLAY_ALL_COMPILER_VERSIONS, "Display All Compiler Versions", this,
                              "Display all compiler versions, even if they have the same major version.");
    addOptionComponent(panel, displayAllCompilerVersions );
   
    addOptionComponent(panel, new VectorFileOptionComponent(OptionConstants.EXTRA_CLASSPATH,
                                                            "Extra Classpath", this,
                                                            "<html>Any directories or jar files to add to the classpath<br>"+
                                                            "of the Compiler and Interactions Pane.</html>", true));
    
    panel.displayComponents();
    
  }

  /** Add all of the components for the Display Options panel of the preferences window. */
  private void _setupDisplayPanel(ConfigPanel panel) {

    final ForcedChoiceOptionComponent lookAndFeelComponent =
      new ForcedChoiceOptionComponent(OptionConstants.LOOK_AND_FEEL, "Look and Feel", this,
                                      "Changes the general appearance of DrJava.");
    addOptionComponent(panel, lookAndFeelComponent);

    final ForcedChoiceOptionComponent plasticComponent =
      new ForcedChoiceOptionComponent(OptionConstants.PLASTIC_THEMES, "Plastic Theme", this,
                                      "Pick the theme to be used by the Plastic family of Look and Feels");
    lookAndFeelComponent.addChangeListener(new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        plasticComponent.getComponent().setEnabled(lookAndFeelComponent.getCurrentComboBoxValue().startsWith("com.jgoodies.looks.plastic."));
        return null;
      }
    });
    plasticComponent.getComponent().setEnabled(lookAndFeelComponent.getCurrentComboBoxValue().startsWith("com.jgoodies.looks.plastic."));
    addOptionComponent(panel, plasticComponent);

    //ToolbarOptionComponent is a degenerate option component
    addOptionComponent(panel, new ToolbarOptionComponent("Toolbar Buttons", this,
                                                  "How to display the toolbar buttons."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.LINEENUM_ENABLED,
                                                  "Show All Line Numbers", this,
                                                  "Whether to show line numbers on the left side of the Definitions Pane."));
   
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.SHOW_SOURCE_WHEN_SWITCHING, 
                                                  "Show sample of source code when fast switching", 
                                                  this,
                                                  "Whether to show a sample of the source code under the document's filename when fast switching documents."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_CODE_PREVIEW_POPUPS, 
                                                  "Show Code Preview Popups", this,
                                                  "<html>Whether to show a popup window with a code preview when the mouse is hovering<br>"+
                                                  "over an item in the Breakpoints, Bookmarks and Find All panes.</html>"));
        
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.CLIPBOARD_HISTORY_SIZE,
                                                  "Size of Clipboard History", this,
                                                         "Determines how many entries are kept in the clipboard history."));
    
    BooleanOptionComponent checkbox = 
      new BooleanOptionComponent(OptionConstants.DIALOG_GOTOFILE_FULLY_QUALIFIED,
                                 "<html><p align=\"right\">" + 
                                 StringOps.
                                   splitStringAtWordBoundaries("Display Fully-Qualified Class Names in \"Go to File\" Dialog",
                                                               40, "<br>", SEPS)+"</p></html>", this,
                                 "<html>Whether to also display fully-qualified class names in the \"Go to File\" dialog.<br>"+
                                 "Enabling this option on network drives might cause the dialog to display after a slight delay.</html>");
    addOptionComponent(panel, checkbox);
    
    checkbox =
      new BooleanOptionComponent(OptionConstants.DIALOG_COMPLETE_SCAN_CLASS_FILES,
                                 "<html><p align=\"right\">" + 
                                 StringOps.
                                   splitStringAtWordBoundaries("Scan Class Files After Each Compile for Auto-Completion and Auto-Import",
                                                               40, "<br>", SEPS)+"</p></html>", this,
                                 "<html>Whether to scan the class files after a compile to generate class names<br>"+
                                 "used for auto-completion and auto-import.<br>"+
                                 "Enabling this option will slow compiles down.</html>");
    addOptionComponent(panel, checkbox);
    
    checkbox =
      new BooleanOptionComponent(OptionConstants.DIALOG_COMPLETE_JAVAAPI,
                                 "<html><p align=\"right\">" + 
                                 StringOps.
                                   splitStringAtWordBoundaries("Consider Java API Classes for Auto-Completion",
                                                               40, "<br>", SEPS)+"</p></html>", this,
                                 "Whether to use the names of the Java API classes for auto-completion as well.");
    addOptionComponent(panel, checkbox);

    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    
    final BooleanOptionComponent drmComponent =
      new BooleanOptionComponent(OptionConstants.DISPLAY_RIGHT_MARGIN, "Display right margin", this,
                                 "Whether to display a line at the right margin.");
    addOptionComponent(panel, drmComponent);
    final IntegerOptionComponent rmcComponent =
      new IntegerOptionComponent(OptionConstants.RIGHT_MARGIN_COLUMNS,
                                 "Right Margin Position", this,
                                 "The number of columns after which the right margin is displayed.");
    addOptionComponent(panel, rmcComponent);

    OptionComponent.ChangeListener drmListener = new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        rmcComponent.getComponent().setEnabled(drmComponent.getComponent().isSelected());
        return null;
      }
    };
    drmComponent.addChangeListener(drmListener);
    drmListener.value(drmComponent);
    
    panel.displayComponents();
  }

  /** Add all of the components for the Font panel of the preferences window. */
  private void _setupFontPanel(ConfigPanel panel) {
    addOptionComponent(panel, new FontOptionComponent(OptionConstants.FONT_MAIN, "Main Font", this,
                                               "The font used for most text in DrJava."));
    addOptionComponent(panel, new FontOptionComponent(OptionConstants.FONT_LINE_NUMBERS, "Line Numbers Font", this,
                                               "<html>The font for displaying line numbers on the left side of<br>" +
                                               "the Definitions Pane if Show All Line Numbers is enabled.<br>" +
                                               "Cannot be displayed larger than the Main Font.</html>"));
    addOptionComponent(panel, new FontOptionComponent(OptionConstants.FONT_DOCLIST, "Document List Font", this,
                                               "The font used in the list of open documents."));
    addOptionComponent(panel, new FontOptionComponent(OptionConstants.FONT_TOOLBAR, "Toolbar Font", this,
                                               "The font used in the toolbar buttons."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.TEXT_ANTIALIAS, "Use anti-aliased text", this,
                                                    "Whether to graphically smooth the text."));
    panel.displayComponents();
  }

  /** Adds all of the components for the Color panel of the preferences window.
   */
  private void _setupColorPanel(ConfigPanel panel) {
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_NORMAL_COLOR, "Normal Color", this,
                                                "The default color for text in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_KEYWORD_COLOR, "Keyword Color", this,
                                                "The color for Java keywords in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_TYPE_COLOR, "Type Color", this,
                                                "The color for classes and types in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_COMMENT_COLOR, "Comment Color", this,
                                                "The color for comments in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_DOUBLE_QUOTED_COLOR, "Double-quoted Color", this,
                                                "The color for quoted strings (eg. \"...\") in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_SINGLE_QUOTED_COLOR, "Single-quoted Color", this,
                                                "The color for quoted characters (eg. 'a') in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_NUMBER_COLOR, "Number Color", this,
                                                "The color for numbers in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_BACKGROUND_COLOR, "Background Color", this,
                                                "The background color of the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_LINE_NUMBER_COLOR, "Line Number Color", this,
                                                "The color for line numbers in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_LINE_NUMBER_BACKGROUND_COLOR, "Line Number Background Color", this,
                                                "The background color for line numbers in the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_MATCH_COLOR, "Brace-matching Color", this,
                                                "The color for matching brace highlights in the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.COMPILER_ERROR_COLOR, "Compiler Error Color", this,
                                                "The color for compiler error highlights in the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.BOOKMARK_COLOR, "Bookmark Color", this,
                                                "The color for bookmarks in the Definitions Pane.", true));
    for (int i = 0; i < OptionConstants.FIND_RESULTS_COLORS.length; ++i) {
      addOptionComponent(panel, new ColorOptionComponent(OptionConstants.FIND_RESULTS_COLORS[i], "Find Results Color "+(i+1), this,
                                                         "A color for highlighting find results in the Definitions Pane.", true));
    }
    addOptionComponent(panel, 
                       new ColorOptionComponent(OptionConstants.DEBUG_BREAKPOINT_COLOR, "Debugger Breakpoint Color", this,
                                                "The color for breakpoints in the Definitions Pane.", true));
    addOptionComponent(panel, 
                       new ColorOptionComponent(OptionConstants.DEBUG_BREAKPOINT_DISABLED_COLOR, "Disabled Debugger Breakpoint Color", this,
                                                "The color for disabled breakpoints in the Definitions Pane.", true));
    addOptionComponent(panel, 
                       new ColorOptionComponent(OptionConstants.DEBUG_THREAD_COLOR, "Debugger Location Color", this,
                                                "The color for the location of the current suspended thread in the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.SYSTEM_OUT_COLOR, "System.out Color", this,
                                                       "The color for System.out in the Interactions and Console Panes."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.SYSTEM_ERR_COLOR, "System.err Color", this,
                                                       "The color for System.err in the Interactions and Console Panes."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.SYSTEM_IN_COLOR, "System.in Color", this,
                                                       "The color for System.in in the Interactions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.INTERACTIONS_ERROR_COLOR, "Interactions Error Color", this,
                                                       "The color for interactions errors in the Interactions Pane.", false, true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEBUG_MESSAGE_COLOR, "Debug Message Color", this,
                                                       "The color for debugger messages in the Interactions Pane.", false, true));
    addOptionComponent(panel, 
                       new ColorOptionComponent(OptionConstants.DRJAVA_ERRORS_BUTTON_COLOR, "DrJava Errors Button Background Color", this,
                                                "The background color of the \"Errors\" button used to show internal DrJava errors.", true));
    addOptionComponent(panel, 
                       new ColorOptionComponent(OptionConstants.RIGHT_MARGIN_COLOR, "Right Margin Color", this,
                                                "The color of the right margin line, if displayed.", true));
    
    panel.displayComponents();
  }

  /** Add all of the components for the Positions panel of the preferences window. */
  private void _setupPositionsPanel(ConfigPanel panel) {
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.WINDOW_STORE_POSITION,
                                                         "Save Main Window Position", this,
                                                         "Whether to save and restore the size and position of the main window.", false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.DIALOG_CLIPBOARD_HISTORY_STORE_POSITION,
                                                  "Save \"Clipboard History\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Clipboard History\" dialog.", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetClipboardHistoryDialogPosition(); }
    }, "Reset \"Clipboard History\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.DIALOG_GOTOFILE_STORE_POSITION,
                                                  "Save \"Go to File\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Go to File\" dialog.", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetGotoFileDialogPosition(); }
    }, "Reset \"Go to File\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.DIALOG_COMPLETE_WORD_STORE_POSITION,
                                                  "Save \"Auto-Complete Word\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Auto-Complete Word\" dialog.", 
                                                  false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetCompleteWordDialogPosition(); }
    }, 
                                                  "Reset \"Auto-Complete Word\" Dialog Position and Size", this, 
                                                  "This resets the dialog position and size to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.DIALOG_JAROPTIONS_STORE_POSITION,
                                                  "Save \"Create Jar File from Project\" Dialog Position", this,
                                                  "Whether to save and restore the position of the \"Create Jar File from Project\" dialog.", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetJarOptionsDialogPosition();
      }
    }, "Reset \"Create Jar File from Project\" Dialog Position", this, "This resets the dialog position to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_OPENJAVADOC_STORE_POSITION,
                                                  "Save \"Open Javadoc\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Open Javadoc\" dialog.", false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetOpenJavadocDialogPosition(); }
    }, "Reset \"Open Javadoc\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_AUTOIMPORT_STORE_POSITION,
                                                  "Save \"Auto Import\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Auto Import\" dialog.", false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetAutoImportDialogPosition(); }
    }, "Reset \"Auto Import\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.DIALOG_EXTERNALPROCESS_STORE_POSITION,
                                                  "Save \"Execute External Process\" Dialog Position", this,
                                                  "Whether to save and restore the position of the \"Execute External Process\" dialog.", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetExecuteExternalProcessPosition();
      }
    }, "Reset \"Execute External Process\" Dialog Position", this, "This resets the dialog position to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_EDITEXTERNALPROCESS_STORE_POSITION,
                                                  "Save \"Edit External Process\" Dialog Position", this,
                                                  "Whether to save and restore the position of the \"Edit External Process\" dialog.", false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetEditExternalProcessPosition();
      }
    }, "Reset \"Execute External Process\" Dialog Position", this, "This resets the dialog position to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_OPENJAVADOC_STORE_POSITION,
                                                  "Save \"Open Javadoc\" Dialog Position", this,
                                                  "Whether to save and restore the position of the \"Open Javadoc\" dialog.", false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetOpenJavadocDialogPosition();
      }
    }, "Reset \"Open Javadoc\" Dialog Position", this, "This resets the dialog position to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_TABBEDPANES_STORE_POSITION,
                                                  "Save \"Tabbed Panes\" Window Position", this,
                                                  "Whether to save and restore the position of the \"Tabbed Panes\" window.", false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetTabbedPanesFrame();
      }
    }, "Reset \"Tabbed Panes\" Window Position", this, "This resets the window position to its default values."));

    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.DETACH_TABBEDPANES,
                                                  "Detach Tabbed Panes", 
                                                  this,
                                                  "Whether to detach the tabbed panes and display them in a separate window.", 
                                                  false)
                         .setEntireColumn(true));

    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.DIALOG_DEBUGFRAME_STORE_POSITION,
                                                  "Save \"Debugger\" Window Position", 
                                                  this,
                                                  "Whether to save and restore the position of the \"Debugger\" window.", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetDebugFrame();
      }
    }, "Reset \"Debugger\" Window Position", this, "This resets the window position to its default values."));

    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DETACH_DEBUGGER,
                                                  "Detach Debugger", this,
                                                  "Whether to detach the debugger and display it in a separate window.", false)
                         .setEntireColumn(true));

    panel.displayComponents();
  }
  
  /** Adds all of the components for the Key Bindings panel of the preferences window.
   */
  private void _setupKeyBindingsPanel(ConfigPanel panel) {
    // using a treemap because it automatically sorts element upon insertion
    TreeMap<String,VectorKeyStrokeOptionComponent> _comps = new TreeMap<String,VectorKeyStrokeOptionComponent>();

    VectorKeyStrokeOptionComponent vksoc;

    for (KeyStrokeData ksd: KeyBindingManager.ONLY.getKeyStrokeData()) {
      if (ksd.getOption() != null) {
        // Get the tooltip, or default to its name, if none
        Action a = ksd.getAction();
        // pick the short description as name, if available
        String name = (String) a.getValue(Action.SHORT_DESCRIPTION);
        // if not available, pick the KeyStrokeData name instead
        if (name == null || name.trim().equals("")) name = ksd.getName();
        // pick the long description as name, if available
        String desc = (String) a.getValue(Action.LONG_DESCRIPTION);
        // if not available, pick the name from above instead
        if (desc == null || desc.trim().equals("")) desc = name;
        // if the map already contains this name, use the description instead
        if (_comps.containsKey(name)) {
          name = desc;
          // if the map already contains the description as well (bad developers!), then use the option's name
          if (_comps.containsKey(name)) {
            name = ksd.getOption().getName();
          }
        }
        vksoc = new VectorKeyStrokeOptionComponent(ksd.getOption(), name, this, desc);
        if (vksoc != null) _comps.put(name, vksoc);
      }
    }

    Iterator<VectorKeyStrokeOptionComponent> iter = _comps.values().iterator();
    while (iter.hasNext()) {
      VectorKeyStrokeOptionComponent x = iter.next();
      addOptionComponent(panel, x);
    }
    panel.displayComponents();
  }

  /** Add all of the components for the Debugger panel of the preferences window. */
  private void _setupDebugPanel(ConfigPanel panel) {
    if (!_mainFrame.getModel().getDebugger().isAvailable()) {
      // Explain how to use debugger
      String howto =
        "\nThe debugger is not currently available. To use the debugger,\n" +
        "you can enter the location of the tools.jar file in the\n" +
        "\"Resource Locations\" pane, in case DrJava does not automatically find it.\n" +
        "See the user documentation for more details.\n";
      LabelComponent label = new LabelComponent(howto, this);
      label.setEntireColumn(true);
      addOptionComponent(panel, label);
    }

    VectorFileOptionComponent sourcePath =
      new VectorFileOptionComponent(OptionConstants.DEBUG_SOURCEPATH, "Sourcepath", this,
                                    "<html>Any directories in which to search for source<br>" +
                                    "files when stepping in the Debugger.</html>", true);
    // Source path can only include directories
    sourcePath.getFileChooser().setFileFilter(new DirectoryFilter("Source Directories"));
    addOptionComponent(panel, sourcePath);
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.DEBUG_STEP_JAVA,
                                                  "Step Into Java Classes", 
                                                  this,
                                                  "<html>Whether the Debugger should step into Java library classes,<br>" +
                                                  "including java.*, javax.*, sun.*, com.sun.*, com.apple.eawt.*, and com.apple.eio.*</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DEBUG_STEP_INTERPRETER,
                                                  "Step Into Interpreter Classes", this,
                                                  "<html>Whether the Debugger should step into the classes<br>" +
                                                  "used by the Interactions Pane (DynamicJava).</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DEBUG_STEP_DRJAVA,
                                                  "Step Into DrJava Classes", this,
                                                  "Whether the Debugger should step into DrJava's own class files."));
    addOptionComponent(panel, 
                       new LabelComponent("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>", 
                                          this, true));
    addOptionComponent(panel, 
                       new VectorStringOptionComponent(OptionConstants.DEBUG_STEP_EXCLUDE,
                                                       "Classes/Packages To Exclude", 
                                                       this,
                                                       "<html>Any classes that the debuggger should not step into.<br>" +
                                                       "Should be a list of fully-qualified class names.<br>" +
                                                       "To exclude a package, add <code>packagename.*</code> to the list.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DEBUG_AUTO_IMPORT,
                                                  "Auto-Import after Breakpoint/Step", this,
                                                  "<html>Whether the Debugger should automatically import packages<br>"+
                                                  "and classes again after a breakpoint or step.</html>"));
    
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.AUTO_STEP_RATE,
                                                         "Auto-Step Rate in ms",
                                                         this,
                                                         "<html>A defined rate in ms at which the debugger automatically steps into/over each line of code.<br>" +
                                                         "Value entered must be an integer value. </html>"));                                                            
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DEBUG_EXPRESSIONS_AND_METHODS_IN_WATCHES,
                                                         "Allow Expressions and Method Calls in Watches", this,
                                                         "<html>Whether the Debugger should allow expressions and method<br>"+
                                                         "calls in watches. These may have side effects and can cause<br>"+
                                                         "delays during the debug process.</html>"));
    panel.displayComponents();
  }

  /** Add all of the components for the Javadoc panel of the preferences window. */
  private void _setupJavadocPanel(ConfigPanel panel) {
    addOptionComponent(panel, 
                       new ForcedChoiceOptionComponent(OptionConstants.JAVADOC_API_REF_VERSION,
                                                       "Java Version for \"Open Java API Javadoc\"", 
                                                       this,
                                                       "Version of the Java API documentation to be used."));
    addOptionComponent(panel, 
                       new ForcedChoiceOptionComponent(OptionConstants.JAVADOC_ACCESS_LEVEL,
                                                       "Access Level", 
                                                       this,
                                                       "<html>Fields and methods with access modifiers at this level<br>" +
                                                       "or higher will be included in the generated Javadoc.</html>"));
    addOptionComponent(panel, 
                       new ForcedChoiceOptionComponent(OptionConstants.JAVADOC_LINK_VERSION,
                                                       "Java Version for Javadoc Links", 
                                                       this,
                                                       "Version of Java for generating links to online Javadoc documentation."));
    addOptionComponent(panel, 
                       new StringOptionComponent(OptionConstants.JAVADOC_1_5_LINK,
                                                 "Javadoc 1.5 URL", this,
                                                 "URL for the Java 1.5 API, for generating links to library classes."));
    addOptionComponent(panel, 
                       new StringOptionComponent(OptionConstants.JAVADOC_1_6_LINK,
                                                 "Javadoc 1.6 URL", this,
                                                 "URL for the Java 1.6 API, for generating links to library classes."));
    addOptionComponent(panel, 
                       new StringOptionComponent(OptionConstants.JUNIT_LINK,
                                                 "JUnit URL", this,
                                                 "URL for the JUnit API, for \"Open Java API Javadoc\"."));

    VectorStringOptionComponent additionalJavadoc =
      new VectorStringOptionComponent(OptionConstants.JAVADOC_ADDITIONAL_LINKS, "Additional Javadoc URLs", this,
                                      "<html>Additional URLs with Javadoc, for \"Open Java API Javadoc\"<br>" +
                                      "and auto-completion.</html>") {
      protected boolean verify(String s) {
        // verify that the allclasses-frame.html file exists at that URL. do not actually parse it now
        boolean result = true;
        try {
          java.net.URL url = new java.net.URL(s+"/allclasses-frame.html");
          java.io.InputStream urls = url.openStream();
          java.io.InputStreamReader is = null;
          java.io.BufferedReader br = null;
          try {
            is = new java.io.InputStreamReader(urls);
            br = new java.io.BufferedReader(is);
            String line = br.readLine();
            if (line == null) { result = false; }
          }
          finally {
            if (br != null) { br.close(); }
            if (is != null) { is.close(); }
            if (urls != null) { urls.close(); }
          }
        }
        catch(java.io.IOException ioe) { result = false; }
        if (!result) {
          JOptionPane.showMessageDialog(ConfigFrame.this,
                                        "Could not find the Javadoc at the URL\n"+
                                        s,
                                        "Error Adding Javadoc",
                                        JOptionPane.ERROR_MESSAGE); 
        }
        return result;
      }
    };
    addOptionComponent(panel, additionalJavadoc);
    
    addOptionComponent(panel, 
                       new DirectoryOptionComponent(OptionConstants.JAVADOC_DESTINATION,
                                                    "Default Destination Directory", this,
                                                    "Optional default directory for saving Javadoc documentation.",
                                                    _dirChooser));
    
    addOptionComponent(panel, 
                       javadocCustomParams = new StringOptionComponent(OptionConstants.JAVADOC_CUSTOM_PARAMS,
                                                 "Custom Javadoc Parameters", this,
                                                 "Any extra flags or parameters to pass to Javadoc."));
    
    // Note: JAVADOC_FROM_ROOTS is intended to set the -subpackages flag, but I don't think that's something
    // we should support -- in general, we only support performing operations on the files that are open.
    // (dlsmith r4189)
//    addOptionComponent(panel, 
//                       new BooleanOptionComponent(OptionConstants.JAVADOC_FROM_ROOTS,
//                                                  "Generate Javadoc From Source Roots", this,
//                                                  "<html>Whether \"Javadoc All\" should generate Javadoc for all packages<br>" +
//                                                  "in an open document's source tree, rather than just the document's<br>" +
//                                                  "own package and sub-packages.</html>"));
    
    panel.displayComponents();
  }

  /** Adds all of the components for the Prompts panel of the preferences window. */
  private void _setupNotificationsPanel(ConfigPanel panel) {
    // Quit
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.QUIT_PROMPT, "Prompt Before Quit", this,
                                                         "Whether DrJava should prompt the user before quitting.", false)
                         .setEntireColumn(true));

    // Interactions
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.INTERACTIONS_RESET_PROMPT,
                                                         "Prompt Before Resetting Interactions Pane", this,
                                                         "<html>Whether DrJava should prompt the user before<br>" +
                                                         "manually resetting the interactions pane.</html>", false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.INTERACTIONS_EXIT_PROMPT,
                                                  "Prompt if Interactions Pane Exits Unexpectedly", 
                                                  this,
                                                  "<html>Whether DrJava should show a dialog box if a program<br>" +
                                                  "in the Interactions Pane exits without the user clicking Reset.</html>",
                                                  false)
                         .setEntireColumn(true));

    // Javadoc
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.JAVADOC_PROMPT_FOR_DESTINATION,
                                                         "Prompt for Javadoc Destination", 
                                                         this,
                                                         "<html>Whether Javadoc should always prompt the user<br>" +
                                                         "to select a destination directory.</html>", 
                                                         false)
                         .setEntireColumn(true));


    // Clean
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.PROMPT_BEFORE_CLEAN,
                                                         "Prompt before Cleaning Build Directory", this,
                                                         "<html>Whether DrJava should prompt before cleaning the<br>" +
                                                         "build directory of a project</html>", false)
                         .setEntireColumn(true));

    // Prompt to change the language level extensions (.dj0/.dj1->.dj, .dj2->.java)
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.PROMPT_RENAME_LL_FILES, "Prompt to Rename Old Language Level Files When Saving", this,
                                                         "<html>Whether DrJava should prompt the user to rename old language level files.<br>"+
                                                         "DrJava suggests to rename .dj0 and .dj1 files to .dj, and .dj2 files to .java.</html>", false)
                         .setEntireColumn(true));

    
    // Save before X
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_COMPILE,
                                                         "Automatically Save Before Compiling", this,
                                                         "<html>Whether DrJava should automatically save before<br>" +
                                                         "recompiling or ask the user each time.</html>", false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.ALWAYS_COMPILE_BEFORE_JUNIT, 
                                                         "Automatically Compile Before Testing", this,
                                                         "<html>Whether DrJava should automatically compile before<br>" +
                                                         "testing with JUnit or ask the user each time.</html>", false)
                         .setEntireColumn(true)); 
    
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_JAVADOC,
                                                         "Automatically Save Before Generating Javadoc", this,
                                                         "<html>Whether DrJava should automatically save before<br>" +
                                                         "generating Javadoc or ask the user each time.</html>", false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.ALWAYS_COMPILE_BEFORE_JAVADOC,
                                                         "Automatically Compile Before Generating Javadoc", this,
                                                         "<html>Whether DrJava should automatically compile before<br>" +
                                                         "generating Javadoc or ask the user each time.</html>", false)
                         .setEntireColumn(true));


    // These are very problematic features, and so are disabled for the forseeable future.
//    addOptionComponent(panel, 
//                       new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_RUN, 
//                                                  "Automatically Save and Compile Before Running Main Method", 
//                                                  this,
//                                                  "<html>Whether DrJava automatically saves and compiles before running<br>" +
//                                                  "a document's main method or explicitly asks the user each time.</html>"));
//    addOptionComponent(panel, 
//                       new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_DEBUG, 
//                                                  "Automatically Save and Compile Before Debugging", 
//                                                  this,
//                                                  "<html>Whether DrJava automatically saves and compiles before<br>" +
//                                                  "debugging or explicitly asks the user each time.</html>"));
    
    // Warnings
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.WARN_BREAKPOINT_OUT_OF_SYNC,
                                                  "Warn on Breakpoint if Out of Sync", 
                                                  this,
                                                  "<html>Whether DrJava should warn the user if the class file<br>" +
                                                  "is out of sync before setting a breakpoint in that file.</html>", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.WARN_DEBUG_MODIFIED_FILE,
                                                  "Warn if Debugging Modified File", 
                                                  this,
                                                  "<html>Whether DrJava should warn the user if the file being<br>" +
                                                  "debugged has been modified since its last save.</html>", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.WARN_CHANGE_LAF,
                                                  "Warn to Restart to Change Look and Feel", 
                                                  this,
                                                  "<html>Whether DrJava should warn the user that look and feel<br>" +
                                                  "changes will not be applied until DrJava is restarted.</html>.", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.WARN_CHANGE_THEME,
                                                  "Warn to Restart to Change Theme", 
                                                  this,
                                                  "<html>Whether DrJava should warn the user that theme<br>" +
                                                  "changes will not be applied until DrJava is restarted.</html>.", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.WARN_CHANGE_DCP,
                                                  "Warn to Restart to Change Default Compiler Preference", 
                                                  this,
                                                  "<html>Whether DrJava should warn the user that default compiler preference<br>" +
                                                  "changes will not be applied until DrJava is restarted.</html>.", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.WARN_CHANGE_MISC,
                                                  "Warn to Restart to Change Preferences (other)", 
                                                  this,
                                                  "<html>Whether DrJava should warn the user that preference<br>" +
                                                  "changes will not be applied until DrJava is restarted.</html>.", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.WARN_CHANGE_INTERACTIONS,
                                                  "Warn to Reset to Change Interactions", 
                                                  this,
                                                  "<html>Whether DrJava should warn the user that preference<br>" +
                                                  "changes will not be applied until the Interactions Pane<br>" +
                                                  "is reset.</html>.", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.WARN_PATH_CONTAINS_POUND,
                                                  "Warn if File's Path Contains a '#' Symbol", 
                                                  this,
                                                  "<html>Whether DrJava should warn the user if the file being<br>" +
                                                  "saved has a path that contains a '#' symbol.<br>" +
                                                  "Users cannot use such files in the Interactions Pane<br>" +
                                                  "because of a bug in Java.</html>", 
                                                  false)
                         .setEntireColumn(true));

    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.DIALOG_DRJAVA_ERROR_POPUP_ENABLED, 
                                                  "Show a notification window when the first DrJava error occurs", 
                                                  this,
                                                  "<html>Whether to show a notification window when the first DrJava error occurs.<br>" +
                                                  "If this is disabled, only the \"DrJava Error\" button will appear.</html>", 
                                                  false)
                         .setEntireColumn(true));
    addOptionComponent(panel,
                       new BooleanOptionComponent(OptionConstants.WARN_IF_COMPIZ, 
                                                  "Warn If Compiz Detected",
                                                  this,
                                                  "<html>Whether DrJava should warn the user if Compiz is running.<br>"+
                                                  "Compiz and Java Swing are incompatible and can lead to crashes.</html>",
                                                  false)
                         .setEntireColumn(true));
    
    
    addOptionComponent(panel, 
                       new LabelComponent("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>", this, true));

    addOptionComponent(panel, 
                       new ForcedChoiceOptionComponent(OptionConstants.DELETE_LL_CLASS_FILES,
                                                       "Delete language level class files?", this,
                                                       "Whether DrJava should delete class files in directories with language level files."));

    addOptionComponent(panel, 
                       new LabelComponent("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>", this, true));

    addOptionComponent(panel, 
                       new ForcedChoiceOptionComponent(OptionConstants.NEW_VERSION_NOTIFICATION,
                                                       "Check for new versions?", this,
                                                       "Whether DrJava should check for new versions on drjava.org."));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.NEW_VERSION_NOTIFICATION_DAYS,
                                                         "Days between new version check", this,
                                                         "The number of days between automatic new version checks."));

    panel.displayComponents();
  }

  /** Adds all of the components for the Miscellaneous panel of the preferences window. */
  private void _setupMiscPanel(ConfigPanel panel) {
    /* Dialog box options */
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.INDENT_LEVEL,
                                                  "Indent Level", this,
                                                  "The number of spaces to use for each level of indentation."));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.RECENT_FILES_MAX_SIZE, "Recent Files List Size", this,
                                                  "<html>The number of files to remember in<br>" +
                                                  "the recently used files list in the File menu.</html>"));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.BROWSER_HISTORY_MAX_SIZE,
                                                         "Maximum Size of Browser History", 
                                                         this,
                                                         "Determines how many entries are kept in the browser history."));
    
    /* Check box options */
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.AUTO_CLOSE_COMMENTS, "Automatically Close Block Comments", 
                                                  this,
                                                  "<html>Whether to automatically insert a closing comment tag (\"*/\")<br>" +
                                                  "when the enter key is pressed after typing a new block comment<br>" +
                                                  "tag (\"/*\" or \"/**\").</html>"));
    String runWithAssertMsg = 
      "<html>Whether to execute <code>assert</code> statements in classes running in the interactions pane.</html>";
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.RUN_WITH_ASSERT, "Enable Assert Statement Execution", 
                                                  this,
                                                  runWithAssertMsg));
    
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.BACKUP_FILES, "Keep Emacs-style Backup Files", 
                                                  this,
                                                  "<html>Whether DrJava should keep a backup copy of each file that<br>" +
                                                  "the user modifies, saved with a '~' at the end of the filename.</html>"));
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.RESET_CLEAR_CONSOLE, "Clear Console After Interactions Reset", 
                                                  this,
                                                  "Whether to clear the Console output after resetting the Interactions Pane."));

    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.FIND_REPLACE_FOCUS_IN_DEFPANE, 
                                                  "Focus on the definitions pane after find/replace", 
                                                  this,
                                                  "<html>Whether to focus on the definitions pane after executing a find/replace operation.<br>" +
                                                  "If this is not selected, the focus will be in the Find/Replace pane.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DRJAVA_USE_FORCE_QUIT, 
                                                  "Forcefully Quit DrJava", this,
                                                  "<html>On some platforms, DrJava does not shut down properly when files are open<br>"+
                                                  "(namely tablet PCs). Check this option to force DrJava to close.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.REMOTE_CONTROL_ENABLED, 
                                                  "Enable Remote Control", this,
                                                  "<html>Whether DrJava should listen to a socket (see below) so it<br>"+
                                                         "can be remote controlled and told to open files.<br>"+
                                                         "(Changes will not be applied until DrJava is restarted.)</html>"));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.REMOTE_CONTROL_PORT, 
                                                         "Remote Control Port", this,
                                                         "<html>A running instance of DrJava can be remote controlled and<br>"+
                                                         "told to open files. This specifies the port used for remote control.<br>" + 
                                                         "(Changes will not be applied until DrJava is restarted.)</html>"));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.FOLLOW_FILE_DELAY, 
                                                         "Follow File Delay", this,
                                                         "<html>The delay in milliseconds that has to elapse before DrJava will check<br>"+
                                                         "if a file that is being followed or the output of an external process has changed.</html>"));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.FOLLOW_FILE_LINES, 
                                                         "Maximum Lines in \"Follow File\" Window", this,
                                                         "<html>The maximum number of lines to keep in a \"Follow File\"<br>"+
                                                         "or \"External Process\" pane. Enter 0 for unlimited.</html>"));
    
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
//    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.LIGHTWEIGHT_PARSING_ENABLED, 
//                                                  "Perform lightweight parsing", this,
//                                                  "<html>Whether to continuously parse the source file for useful information.<br>" +
//                                                  "Enabling this option might introduce delays when editing files.<html>"));
//    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.DIALOG_LIGHTWEIGHT_PARSING_DELAY, "Light-weight parsing delay in milliseconds", this,
//                                                  "The amount of time DrJava will wait after the last keypress before beginning to parse."));
    
    panel.displayComponents();
  }  

  /** Adds all of the components for the JVMs panel of the preferences window. */
  private void _setupJVMsPanel(ConfigPanel panel) {
    addOptionComponent(panel, 
                       new ForcedChoiceOptionComponent(OptionConstants.MASTER_JVM_XMX, 
                                                       "Maximum Heap Size for Main JVM in MB", 
                                                       this,
                                                       "The maximum heap the Main JVM can use. Select blank for default."));
    addOptionComponent(panel, 
                       new StringOptionComponent(OptionConstants.MASTER_JVM_ARGS, "JVM Args for Main JVM", 
                                                 this,
                                                 "The command-line arguments to pass to the Main JVM."));
    addOptionComponent(panel, 
                       new ForcedChoiceOptionComponent(OptionConstants.SLAVE_JVM_XMX, 
                                                       "Maximum Heap Size for Interactions JVM in MB", 
                                                       this,
                                                       "The maximum heap the Interactions JVM can use. Select blank for default"));
    addOptionComponent(panel, 
                       new StringOptionComponent(OptionConstants.SLAVE_JVM_ARGS, "JVM Args for Interactions JVM", 
                                                 this,
                                                 "The command-line arguments to pass to the Interactions JVM."));    
    panel.displayComponents();
  }

  /** Adds all of the components for the file types panel of the preferences window. */
  private void _setupFileTypesPanel(ConfigPanel panel) {
    if (PlatformFactory.ONLY.canRegisterFileExtensions()) {
      addOptionComponent(panel, new LabelComponent("<html>Assign DrJava project files and DrJava extensions<br>"+
                                                   "(with the extensions .drjava and .djapp) to DrJava.<br>"+
                                                   "When double-clicking on a .drjava file, DrJava will open it.</html>", this, true));
      
      panel.addComponent(new ButtonComponent(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (PlatformFactory.ONLY.registerDrJavaFileExtensions()) {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Successfully set .drjava and .djapp file associations.",
                                          "Success",
                                          JOptionPane.INFORMATION_MESSAGE); 
          }
          else {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Could not set .drjava and .djapp file associations.",
                                          "File Types Error",
                                          JOptionPane.ERROR_MESSAGE); 
          }
        }
      }, "Associate .drjava and .djapp Files with DrJava", this, "This associates .drjava and .djapp files with DrJava."));

      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      
      panel.addComponent(new ButtonComponent(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (PlatformFactory.ONLY.unregisterDrJavaFileExtensions()) {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Successfully removed .drjava and .djapp file associations.",
                                          "Success",
                                          JOptionPane.INFORMATION_MESSAGE); 
          }
          else {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Could not remove .drjava and .djapp file associations.",
                                          "File Types Error",
                                          JOptionPane.ERROR_MESSAGE); 
          }
        }
      }, "Remove .drjava and .djapp File Associations", this, "This removes the association of .drjava and .djapp files with DrJava."));
      
      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      addOptionComponent(panel, new LabelComponent("<html>Assign Java source files with the<br>"+
                                                   "extension .java to DrJava. When double-clicking<br>"+
                                                   "on a .java file, DrJava will open it.</html>", this, true));

      panel.addComponent(new ButtonComponent(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (PlatformFactory.ONLY.registerJavaFileExtension()) {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Successfully set .java file association.",
                                          "Success",
                                          JOptionPane.INFORMATION_MESSAGE); 
          }
          else {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Could not set .java file association.",
                                          "File Types Error",
                                          JOptionPane.ERROR_MESSAGE); 
          }
        }
      }, "Associate .java Files with DrJava", this, "This associates .java source files with DrJava."));

      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));

      panel.addComponent(new ButtonComponent(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (PlatformFactory.ONLY.unregisterJavaFileExtension()) {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Successfully removed .java file association.",
                                          "Success",
                                          JOptionPane.INFORMATION_MESSAGE); 
          }
          else {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Could not remove .java file association.",
                                          "File Types Error",
                                          JOptionPane.ERROR_MESSAGE); 
          }
        }
      }, "Remove .java File Association", this, "This removes the association of .java project files with DrJava."));

      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      
      addOptionComponent(panel, new ForcedChoiceOptionComponent(OptionConstants.FILE_EXT_REGISTRATION,
                                                                "<html>Automatically assign .java, .drjava and .djapp Files to DrJava</html>", this,
                                                                "<html>Assign files with the extensions .java, .drjava and .djapp to DrJava.<br>"+
                                                                "When double-clicking those files, they will be opened in DrJava.<br><br>"+
                                                                "Selecting 'always' will re-establish this association every time DrJava<br>"+
                                                                "started, without asking. Selecting 'ask me' will ask the user at start up<br>"+
                                                                "if the association has been changed. Selecting 'never' will not assign<br>"+
                                                                ".java, .drjava and .djapp files to DrJava."));
    }
    else {
      addOptionComponent(panel, 
                         new LabelComponent("<html><br><br>"+
                                            (PlatformFactory.ONLY.isMacPlatform()?
                                               "File associations are managed automatically by Mac OS.":
                                               (PlatformFactory.ONLY.isWindowsPlatform()?
                                                  "To set file associations, please use the .exe file version of DrJava.<br>"+
                                                "Configuring file associations is not supported for the .jar file version.":
                                                  "Managing file associations is not supported yet on this operating system."))+
                                            "</html>",
                                            this, true));
    }
    panel.displayComponents();
  }
  
  /** Adds all of the components for the Compiler Options Panel of the preferences window
    */
  private void _setupCompilerPanel(ConfigPanel panel) {
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.SHOW_UNCHECKED_WARNINGS, "Show Unchecked Warnings", this, 
                                                  "<html>Warn about unchecked conversions involving parameterized types.</html>", false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.SHOW_DEPRECATION_WARNINGS, "Show Deprecation Warnings", this, 
                                                  "<html>Warn about each use or override of a deprecated method, field, or class.</html>", false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.SHOW_PATH_WARNINGS, "Show Path Warnings", this, 
                                                  "<html>Warn about nonexistent members of the classpath and sourcepath.</html>", false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.SHOW_SERIAL_WARNINGS, "Show Serial Warnings", this, 
                                                  "<html>Warn about missing <code>serialVersionUID</code> definitions on serializable classes.</html>", 
                                                  false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.SHOW_FINALLY_WARNINGS, "Show Finally Warnings", this,
                                                  "<html>Warn about <code>finally</code> clauses that cannot complete normally.</html>", false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.SHOW_FALLTHROUGH_WARNINGS, "Show Fall-Through Warnings", this,
                                                  "<html>Warn about <code>switch</code> block cases that fall through to the next case.</html>", 
                                                  false)
                         .setEntireColumn(true));
    /*
     * The drop down box containing the compiler names
     */
    final ForcedChoiceOptionComponent CPC = new ForcedChoiceOptionComponent(OptionConstants.COMPILER_PREFERENCE_CONTROL.evaluate(), "Compiler Preference", 
                         this,
                         "Which compiler is prefered?");
    
    /*
     * Action listener that saves the selected compiler name into the DEFAULT_COMPILER_PREFERENCE setting
     */
    ActionListener CPCActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(!edu.rice.cs.drjava.DrJava.getConfig().getSetting(OptionConstants.DEFAULT_COMPILER_PREFERENCE).equals(CPC.getCurrentComboBoxValue())){
          edu.rice.cs.drjava.DrJava.getConfig().setSetting(OptionConstants.DEFAULT_COMPILER_PREFERENCE,CPC.getCurrentComboBoxValue());
        }
      }
    };
   
    /*
     * insures that the change is made only when the apply or ok button is hit
     */
    _applyButton.addActionListener(CPCActionListener);
    _okButton.addActionListener(CPCActionListener);
    
    /*
     * adds the drop down box to the panel
     */
    addOptionComponent(panel, 
                       CPC.setEntireColumn(false)
                      );
    
    addOptionComponent(panel, 
                       new LabelComponent("<html><br><br>Note: Compiler warnings not shown if compiling any Java language level files.</html>", 
                                          this, true));
    panel.displayComponents();
  }
  
  /** Add all of the components for the Interactions panel of the preferences window. */
  private void _setupInteractionsPanel(ConfigPanel panel) {
    final DirectoryOptionComponent wdComponent =
      new DirectoryOptionComponent(OptionConstants.FIXED_INTERACTIONS_DIRECTORY,
                                   "Interactions Working Directory", this,
                                   "<html>Working directory for the Interactions Pane (unless<br>"+
                                   "a project working directory has been set).</html>",
                                   _dirChooser);
    addOptionComponent(panel, wdComponent);
    final BooleanOptionComponent stickyComponent = 
      new BooleanOptionComponent(OptionConstants.STICKY_INTERACTIONS_DIRECTORY,
                                 "<html><p align=\"right\">" + 
                                 StringOps.
                                   splitStringAtWordBoundaries("Restore last working directory of the Interactions pane on start up",
                                                               33, "<br>", SEPS), this,
                                 "<html>Whether to restore the last working directory of the Interaction pane on start up,<br>" +
                                 "or to always use the value of the \"user.home\" Java property<br>"+
                                 "(currently "+System.getProperty("user.home")+").");
    addOptionComponent(panel, stickyComponent);
    
    OptionComponent.ChangeListener wdListener = new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        File f = wdComponent.getComponent().getFileFromField();
        boolean enabled = (f == null) || (f.equals(FileOps.NULL_FILE));
        stickyComponent.getComponent().setEnabled(enabled);
        return null;
      }
    };
    wdComponent.addChangeListener(wdListener);
    wdListener.value(wdComponent);

    addOptionComponent(panel, new BooleanOptionComponent
                         (OptionConstants.SMART_RUN_FOR_APPLETS_AND_PROGRAMS, 
                          "Smart Run Command", this,
                          "<html>Whether the Run button and meni item should automatically detect<br>"+
                          "applets and ACM Java Task Force programs (subclasses of acm.program.Program).</html>"));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.HISTORY_MAX_SIZE, "Size of Interactions History", this,
                                                  "The number of interactions to remember in the history."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_AUTOIMPORT_ENABLED, 
                                                         "Enable the \"Auto Import\" Dialog", this,
                                                         "<html>Whether DrJava should open the \"Auto Import\" dialog when<br>"+
                                                         "an undefined class is encountered in the Interactions Pane.</html>"));
    VectorStringOptionComponent autoImportClasses =
      new VectorStringOptionComponent(OptionConstants.INTERACTIONS_AUTO_IMPORT_CLASSES, "Classes to Auto-Import", this,
                                      "<html>List of classes to auto-import every time the<br>"+
                                      "Interaction Pane is reset or started. Examples:<br><br>"+
                                      "java.io.File<br>"+
                                      "java.util.*</html>") {
      protected boolean verify(String s) {
        boolean result = true;
        // verify that the string contains only Java identifier characters, dots and stars
        for(int i = 0; i < s.length(); ++i) {
          char ch = s.charAt(i);
          if ((ch!='.') && (ch!='*') && (!Character.isJavaIdentifierPart(ch))) {
            result = false;
            break;
          }
        }
        if (!result) {
          JOptionPane.showMessageDialog(ConfigFrame.this,
                                        "This is not a valid class name:\n"+
                                        s,
                                        "Error Adding Class Name",
                                        JOptionPane.ERROR_MESSAGE); 
        }
        return result;
      }
    };
    addOptionComponent(panel, autoImportClasses);

    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      
    addOptionComponent(panel, 
                       new ForcedChoiceOptionComponent(OptionConstants.DYNAMICJAVA_ACCESS_CONTROL,
                                                       "Enforce access control", 
                                                       this,
                                                       "What kind of access control should DrJava enforce in the Interactions Pane?"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DYNAMICJAVA_REQUIRE_SEMICOLON, 
                                                         "Require Semicolon", this,
                                                         "<html>Whether DrJava should require a semicolon at the<br>"+
                                                         "end of a statement in the Interactions Pane.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DYNAMICJAVA_REQUIRE_VARIABLE_TYPE, 
                                                         "Require Variable Type", this,
                                                         "<html>Whether DrJava should require a variable type for<br>"+
                                                         "variable declarations in the Interactions Pane.</html>"));
    
    panel.displayComponents();
  }

  /** Add all of the components for the JUnit panel of the preferences window. */
  private void _setupJUnitPanel(ConfigPanel panel) {
    final BooleanOptionComponent junitLocEnabled =
      new BooleanOptionComponent(OptionConstants.JUNIT_LOCATION_ENABLED, "Use external JUnit", this,
                                 "<html>If this is enabled, DrJava will use the JUnit configured<br>"+
                                 "below under 'JUnit/ConcJUnit Location'. If it is disabled,<br>"+
                                 "DrJava will use the JUnit that is built-in.</html>", false)
      .setEntireColumn(true);
    addOptionComponent(panel, junitLocEnabled);
    final FileOptionComponent junitLoc =
      new FileOptionComponent(OptionConstants.JUNIT_LOCATION, "JUnit/ConcJUnit Location", this,
                              "<html>Optional location of the JUnit or ConcJUnit jar file.<br>"+
                              "(Changes will not be applied until the Interactions Pane<br>"+
                              "is reset.)</html>",
                              new FileSelectorComponent(this, _jarChooser, 30, 10f) {
      public void setFileField(File file) {
        if (edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidJUnitFile(file) ||
            edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidConcJUnitFile(file)) {
          super.setFileField(file);
        }
        else if (file.exists()) { // invalid JUnit/ConcJUnit file, but exists
          new edu.rice.cs.drjava.ui.DrJavaScrollableDialog(_parent, "Invalid JUnit/ConcJUnit File", "Stack trace:",
                                                           edu.rice.cs.util.StringOps.getStackTrace(), 600, 400, false).show();
          JOptionPane.showMessageDialog(_parent, "The file '"+ file.getName() + "'\nis not a valid JUnit/ConcJUnit file.",
                                        "Invalid JUnit/ConcJUnit File", JOptionPane.ERROR_MESSAGE);
          resetFileField(); // revert if not valid          
        }
      }
      public boolean validateTextField() {
        String newValue = _fileField.getText().trim();
        
        File newFile = FileOps.NULL_FILE;
        if (!newValue.equals(""))
          newFile = new File(newValue);
        
        if (newFile != FileOps.NULL_FILE && !newFile.exists()) {
          JOptionPane.showMessageDialog(_parent, "The file '"+ newFile.getName() + "'\nis invalid because it does not exist.",
                                        "Invalid File Name", JOptionPane.ERROR_MESSAGE);
          if (_file != null && ! _file.exists()) _file = FileOps.NULL_FILE;
          resetFileField(); // revert if not valid
          
          return false;
        }
        else {
          if (edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidJUnitFile(newFile) ||
              edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidConcJUnitFile(newFile) ||
              FileOps.NULL_FILE.equals(newFile)) {
            setFileField(newFile);
            return true;
          }
          else {
            new edu.rice.cs.drjava.ui.DrJavaScrollableDialog(_parent, "Invalid JUnit/ConcJUnit File", "newFile is NULL_FILE? "+(FileOps.NULL_FILE.equals(newFile)),
                                                             edu.rice.cs.util.StringOps.getStackTrace(), 600, 400, false).show();
            JOptionPane.showMessageDialog(_parent, "The file '"+ newFile.getName() + "'\nis not a valid JUnit/ConcJUnit file.",
                                          "Invalid JUnit/ConcJUnit File", JOptionPane.ERROR_MESSAGE);
            resetFileField(); // revert if not valid
            
            return false;
          }
        }
      }    
    });
    junitLoc.setFileFilter(ClassPathFilter.ONLY);
    addOptionComponent(panel, junitLoc);

    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));

    final ForcedChoiceOptionComponent concJUnitChecksEnabledComponent =
      new ForcedChoiceOptionComponent(OptionConstants.CONCJUNIT_CHECKS_ENABLED, "Enabled ConcJUnit Checks", this,
                                      "<html>The concurrent unit testing checks that should be performed.<br>"+
                                      "'none' uses plain JUnit. ConcJUnit can also detect failures in<br>"+
                                      "all threads ('all-threads'), detect threads that did not end in<br>"+
                                      "time ('all-threads, join'), and threads that ended in time only<br>"+
                                      "because they were lucky ('all-threads, nojoin, lucky).<br>"+
                                      "The last setting requires a 'ConcJUnit Runtime Location' to be set.</html>");
    addOptionComponent(panel, concJUnitChecksEnabledComponent);

    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));    

    final FileOptionComponent rtConcJUnitLoc =
      new FileOptionComponent(OptionConstants.RT_CONCJUNIT_LOCATION, "ConcJUnit Runtime Location", this,
                              "<html>Optional location of the Java Runtime Library processed<br>"+
                              "to generate &quot;lucky&quot; warnings. If left blank, &quot;lucky&quot; warnings<br>"+
                              "will not be generated. This setting is deactivated if the path to<br>"+
                              "ConcJUnit has not been specified above.<br>" + 
                              "(Changes will not be applied until the Interactions Pane is reset.)</html>",
                              new FileSelectorComponent(this, _jarChooser, 30, 10f) {
      public void setFileField(File file) {
        if (edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidRTConcJUnitFile(file)) {
          super.setFileField(file);
        }
        else if (file.exists()) { // invalid but exists
          JOptionPane.showMessageDialog(_parent, "The file '"+ file.getName() + "'\nis not a valid ConcJUnit Runtime file.",
                                        "Invalid ConcJUnit Runtime File", JOptionPane.ERROR_MESSAGE);
          resetFileField(); // revert if not valid          
        }
      }
      public boolean validateTextField() {
        String newValue = _fileField.getText().trim();
        
        File newFile = FileOps.NULL_FILE;
        if (!newValue.equals(""))
          newFile = new File(newValue);
        
        if (newFile != FileOps.NULL_FILE && !newFile.exists()) {
          JOptionPane.showMessageDialog(_parent, "The file '"+ newFile.getName() + "'\nis invalid because it does not exist.",
                                        "Invalid File Name", JOptionPane.ERROR_MESSAGE);
          if (_file != null && ! _file.exists()) _file = FileOps.NULL_FILE;
          resetFileField(); // revert if not valid
          
          return false;
        }
        else {
          if (edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidRTConcJUnitFile(newFile) ||
              FileOps.NULL_FILE.equals(newFile)) {
            setFileField(newFile);
            return true;
          }
          else {
            JOptionPane.showMessageDialog(_parent, "The file '"+ newFile.getName() + "'\nis not a valid ConcJUnit Runtime file.",
                                          "Invalid ConcJUnit Runtime File", JOptionPane.ERROR_MESSAGE);
            resetFileField(); // revert if not valid
            
            return false;
          }
        }
      }    
    });
    rtConcJUnitLoc.setFileFilter(ClassPathFilter.ONLY);
    
    ActionListener processRTListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File concJUnitJarFile = FileOps.getDrJavaFile();
        if (junitLocEnabled.getComponent().isSelected()) {
          concJUnitJarFile = junitLoc.getComponent().getFileFromField();
        }
        File rtFile = rtConcJUnitLoc.getComponent().getFileFromField();
        edu.rice.cs.drjava.model.junit.ConcJUnitUtils.
          showGenerateRTConcJUnitJarFileDialog(ConfigFrame.this,
                                               rtFile,
                                               concJUnitJarFile,
                                               new Runnable1<File>() {
          public void run(File targetFile) {
            rtConcJUnitLoc.getComponent().setFileField(targetFile);
          }
        },
                                               new Runnable() { public void run() { } });
      }
    };
    final ButtonComponent processRT =
      new ButtonComponent(processRTListener, "Generate ConcJUnit Runtime File", this,
                          "<html>Generate the ConcJUnit Runtime file specified above.<br>"+
                          "This setting is deactivated if the path to ConcJUnit has not been specified above.</html>");
    
    OptionComponent.ChangeListener rtConcJUnitListener = new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        File f = junitLoc.getComponent().getFileFromField();
        boolean enabled = (!junitLocEnabled.getComponent().isSelected()) ||
          edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidConcJUnitFile(f);
        rtConcJUnitLoc.getComponent().setEnabled(enabled);
        processRT.getComponent().setEnabled(enabled);
        concJUnitChecksEnabledComponent.getComponent().setEnabled(enabled);
        return null;
      }
    };

    OptionComponent.ChangeListener junitLocListener = new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        boolean enabled = junitLocEnabled.getComponent().isSelected();
        junitLoc.getComponent().setEnabled(enabled);
        return null;
      }
    };
    junitLocEnabled.addChangeListener(junitLocListener);
    junitLocEnabled.addChangeListener(rtConcJUnitListener);
    junitLoc.addChangeListener(rtConcJUnitListener);
    addOptionComponent(panel, rtConcJUnitLoc);
    addOptionComponent(panel, processRT);
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    final LabelComponent internalExternalStatus = new LabelComponent("<html>&nbsp;</html>", this, true);
    final LabelComponent threadsStatus = new LabelComponent("<html>&nbsp;</html>", this, true);
    final LabelComponent joinStatus = new LabelComponent("<html>&nbsp;</html>", this, true);
    final LabelComponent luckyStatus = new LabelComponent("<html>&nbsp;</html>", this, true);
    OptionComponent.ChangeListener junitStatusChangeListener = new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        File f = junitLoc.getComponent().getFileFromField();
        String[] s = new String[] { " ", " ", " ", " " };
        boolean isConcJUnit = true;
        if ((!junitLocEnabled.getComponent().isSelected()) || (f==null) || FileOps.NULL_FILE.equals(f) || !f.exists()) {
          s[0] = "DrJava uses the built-in ConcJUnit framework.";
        }
        else {
          String type = "ConcJUnit";
          if (!edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidConcJUnitFile(f)) {
            type = "JUnit";
            isConcJUnit = false;
          }
          s[0] = "DrJava uses an external "+type+" framework.";
        }
        if (!isConcJUnit) {
          s[1] = "JUnit does not support all-thread, no-join";
          s[2] = "or lucky checks. They are all disabled.";
        }
        else {
          s[1] = "All-thread checks are disabled.";
          s[2] = "No-join checks are disabled.";
          s[3] = "Lucky checks are disabled.";
          if (!concJUnitChecksEnabledComponent.getCurrentComboBoxValue().
                equals(OptionConstants.ConcJUnitCheckChoices.NONE)) {
            s[1] = "All-thread checks are enabled.";
            if (concJUnitChecksEnabledComponent.getCurrentComboBoxValue().
                  equals(OptionConstants.ConcJUnitCheckChoices.ALL) ||
                concJUnitChecksEnabledComponent.getCurrentComboBoxValue().
                  equals(OptionConstants.ConcJUnitCheckChoices.NO_LUCKY)) {
              s[2] = "No-join checks are enabled.";
              if (concJUnitChecksEnabledComponent.getCurrentComboBoxValue().
                    equals(OptionConstants.ConcJUnitCheckChoices.ALL)) {
                File rtf = rtConcJUnitLoc.getComponent().getFileFromField();
                if ((rtf!=null) && !FileOps.NULL_FILE.equals(rtf) && rtf.exists() &&
                    edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidRTConcJUnitFile(rtf)) {
                  s[3] = "Lucky checks are enabled.";
                }
              }
            }
          }
        }
        internalExternalStatus.getComponent().setText(s[0]);
        threadsStatus.getComponent().setText(s[1]);
        joinStatus.getComponent().setText(s[2]);
        luckyStatus.getComponent().setText(s[3]);
        return null;
      }
    };
    concJUnitChecksEnabledComponent.addChangeListener(junitStatusChangeListener);
    junitLocEnabled.addChangeListener(junitStatusChangeListener);
    junitLoc.addChangeListener(junitStatusChangeListener);
    rtConcJUnitLoc.addChangeListener(junitStatusChangeListener);
    addOptionComponent(panel, internalExternalStatus);
    addOptionComponent(panel, threadsStatus);
    addOptionComponent(panel, joinStatus);
    addOptionComponent(panel, luckyStatus);

    junitLocListener.value(null);
    rtConcJUnitListener.value(null);
    junitStatusChangeListener.value(null);
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    final BooleanOptionComponent forceTestSuffix  =
      new BooleanOptionComponent(OptionConstants.FORCE_TEST_SUFFIX,
                                 "Require test classes in projects to end in \"Test\"",
                                 this,
                                 "Whether to force test classes in projects to end in \"Test\".",
                                 false)
      .setEntireColumn(true);
    addOptionComponent(panel, forceTestSuffix);
    
    panel.displayComponents();
  }
  
  /** Private class to handle rendering of tree nodes, each of which
    *  corresponds to a ConfigPanel.  These nodes should only be accessed
    *  from the event handling thread.
    */
  private class PanelTreeNode extends DefaultMutableTreeNode {
    
    private final ConfigPanel _panel;
    
    public PanelTreeNode(String t) {
      super(t);
      _panel = new ConfigPanel(t);
    }

    public PanelTreeNode(ConfigPanel c) {
      super(c.getTitle());
      _panel = c;
    }
    private ConfigPanel getPanel() { return _panel; }

    /** Tells its panel to update, and tells all of its child nodes to update their panels.
     *  @return whether the update succeeded.
     */
    private boolean update() {
      
      boolean isValidUpdate = _panel.update();
       
      //if this panel encountered an error while attempting to update, return false
      if (!isValidUpdate) {
        //System.out.println("Panel.update() returned false");

        //TreePath path = new TreePath(this.getPath());
        // causes ClassCastException under jsr14 v2.0 for no apparent reason.
        // Workaround:  store result of getPath() to temporary array.

        TreeNode[] nodes = getPath();
        TreePath path = new TreePath(nodes);
        _tree.expandPath(path);
        _tree.setSelectionPath(path);
        return false;
      }

      Enumeration<?> childNodes = children();
      while (childNodes.hasMoreElements()) {
        boolean isValidUpdateChildren = ((PanelTreeNode)childNodes.nextElement()).update();
        //if any of the children nodes encountered an error, return false
        if (!isValidUpdateChildren) {
          return false;
        }
      }

      return true;
    }

    /** Tells its panel to reset its displayed value to the currently set value for this component, and tells all of
      * its children to reset their panels.  Should be performed in the event thread!
      */
    public void resetToCurrent() {
      _panel.resetToCurrent();

      Enumeration<?> childNodes = children();
      while (childNodes.hasMoreElements()) {
        ((PanelTreeNode)childNodes.nextElement()).resetToCurrent();
      }
    }
  }

  private class PanelTreeSelectionListener implements TreeSelectionListener {
    public void valueChanged(TreeSelectionEvent e) {
      Object o = _tree.getLastSelectedPathComponent();
      //System.out.println("Object o : "+o);
      if (o instanceof PanelTreeNode) {
        //System.out.println("o is instanceof PanelTreeNode");
        PanelTreeNode child = (PanelTreeNode) _tree.getLastSelectedPathComponent();
        _displayPanel(child.getPanel());
      }
    }
  }
}
