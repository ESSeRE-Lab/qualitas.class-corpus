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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.wizard;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.common.xml.XmlValidator;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;

/**
 * This is the main class for the XmlWizard.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class XmlWizard extends JFrame implements ActionListener
{

  private Document     document;
  private JFileChooser chooser;
  private WizardTabs   wizardTabs;

  /**
   * Creates a new <code>XmlWizard</code> object. The wizard will edit an
   * already existing file
   * 
   * @param xmlFile XML file name
   * @throws IOException if the file cannot be read
   * @throws DocumentException if the xml document cannot be parsed
   */
  public XmlWizard(String xmlFile) throws DocumentException, IOException
  {
    this();
    loadDocument(xmlFile);
  }

  /**
   * Creates a new <code>XmlWizard</code> object The wizard will create a new
   * document from scratch
   */
  public XmlWizard()
  {
    // basic define
    super(WizardTranslate.get("init.title.main.frame"));
    chooser = new JFileChooser()
    {
      /**
       * @see javax.swing.JFileChooser#accept(java.io.File)
       */
      public boolean accept(File f)
      {
        if (f.isDirectory())
          return true;
        if (f.getName().endsWith(".xml"))
          return true;
        else
          return false;
      }
    };

    // set some fonts

    UIManager.getDefaults().put("Label.font",
        new Font("Helvetica", Font.BOLD, 10));
    UIManager.getDefaults().put("ComboBox.font",
        new Font("Helvetica", Font.BOLD, 10));
    UIManager.getDefaults().put("Border.font",
        new Font("Helvetica", Font.BOLD, 10));
    UIManager.getDefaults().put("Menu.font",
        new Font("Helvetica", Font.BOLD, 10));
    UIManager.getDefaults().put("Button.font",
        new Font("Helvetica", Font.BOLD, 10));
    UIManager.getDefaults().put("MenuItem.font",
        new Font("Helvetica", Font.BOLD, 10));

    this.setSize(WizardConstants.FRAME_WIDTH, WizardConstants.FRAME_HEIGHT);
    this.getContentPane().setLayout(new BorderLayout());
    GuiConstants.centerComponent(this, WizardConstants.FRAME_WIDTH,
        WizardConstants.FRAME_HEIGHT);

    // define the menu
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu(WizardTranslate.get("init.menu.1"));
    // quit
    JMenuItem item1 = new JMenuItem(WizardTranslate
        .get(WizardConstants.COMMAND_QUIT));
    item1.setActionCommand(WizardConstants.COMMAND_QUIT);
    // import
    JMenuItem item2 = new JMenuItem(WizardTranslate
        .get(WizardConstants.COMMAND_EXPORT_XML));
    item2.setActionCommand(WizardConstants.COMMAND_EXPORT_XML);
    // export
    JMenuItem item3 = new JMenuItem(WizardTranslate
        .get(WizardConstants.COMMAND_IMPORT_XML));
    item3.setActionCommand(WizardConstants.COMMAND_IMPORT_XML);
    // check
    JMenuItem item4 = new JMenuItem(WizardTranslate
        .get(WizardConstants.COMMAND_CHECK_WIZARD));
    item4.setActionCommand(WizardConstants.COMMAND_CHECK_WIZARD);
    //  validate
    JMenuItem item5 = new JMenuItem(WizardTranslate
        .get(WizardConstants.COMMAND_VALIDATE_XML));
    item5.setActionCommand(WizardConstants.COMMAND_VALIDATE_XML);
    menu.add(item1).addActionListener(this);
    menu.add(item2).addActionListener(this);
    menu.add(item3).addActionListener(this);
    menu.add(item4).addActionListener(this);
    menu.add(item5).addActionListener(this);
    menu.setVisible(true);
    menuBar.add(menu);

    // define the panel that contains the menu
    JPanel menuPane = new JPanel();
    menuPane.add(menuBar);
    this.getContentPane().add(menuPane, BorderLayout.NORTH);

    startWizardTabs();

    // Finish creating the frame
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.validate();
    //this.pack();
    this.setVisible(true);
  }

  private void startWizardTabs()
  {
    if (wizardTabs != null)
      this.getContentPane().remove(wizardTabs);
    wizardTabs = new WizardTabs();
    this.getContentPane().add(wizardTabs, BorderLayout.CENTER);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    String command = e.getActionCommand();
    if (command.equals(WizardConstants.COMMAND_QUIT))
      System.exit(1);
    else if (command.equals(WizardConstants.COMMAND_EXPORT_XML))
      try
      {
        chooser.showSaveDialog(this);
        File file = chooser.getSelectedFile();
        if (file != null)
          exportDocument(file.getAbsolutePath());
      }
      catch (IOException e1)
      {
        e1.printStackTrace();
      }
    else if (command.equals(WizardConstants.COMMAND_IMPORT_XML))
    {
      try
      {
        chooser.showOpenDialog(this);
        File file = chooser.getSelectedFile();
        if (file != null)
          loadDocument(file.getAbsolutePath());
      }
      catch (Exception e2)
      {
        e2.printStackTrace();
      }
    }
    else if (command.equals(WizardConstants.COMMAND_CHECK_WIZARD))
    {
      chooser.showOpenDialog(this);
      File file = chooser.getSelectedFile();
      if (file != null)
      {
        try
        {
          loadDocument(file.getAbsolutePath());
          String newDoc = file.getParentFile().getAbsolutePath()
              + File.separator + file.getName() + ".check";
          exportDocument(newDoc);
          validateDocument(newDoc);
        }
        catch (Exception e2)
        {
          e2.printStackTrace();
        }
      }
    }
    else if (command.equals(WizardConstants.COMMAND_VALIDATE_XML))
    {
      chooser.showOpenDialog(this);
      File file = chooser.getSelectedFile();
      if (file != null)
        try
        {
          validateDocument(file.getAbsolutePath());
        }
        catch (IOException e2)
        {
          e2.printStackTrace();
        }
    }
  }

  /**
   * Load document from a xml file
   * 
   * @param xmlFile the path to the xml document
   * @throws DocumentException if fails to parse
   * @throws IOException if fails to read
   */
  public void loadDocument(String xmlFile) throws DocumentException,
      IOException
  {
    SAXReader reader = new SAXReader();
    this.document = reader.read(new FileReader(xmlFile));
    if (document != null)
    {
      startWizardTabs();
      wizardTabs.importDocumentFromXml(document);
    }
  }

  /**
   * Save the document to an xml file
   * 
   * @param xmlFile the path to the xml file to save to
   * @throws IOException if fails to write
   */
  public void saveDocument(String xmlFile) throws IOException
  {
    OutputFormat format = OutputFormat.createPrettyPrint();
    XMLWriter writer = new XMLWriter(new FileWriter(xmlFile), format);
    writer.write(document);
    writer.close();
  }

  /**
   * Validate the given XML document contained in the given file.
   * 
   * @param xmlFile XML file name
   * @throws IOException if an error occurs
   */
  public void validateDocument(String xmlFile) throws IOException
  {
    XmlValidator validator = new XmlValidator(Constants.C_JDBC_DTD_FILE,
        new FileReader(new File(xmlFile)));
    XmlValidatorFrame frame = new XmlValidatorFrame(xmlFile);

    if (validator.isDtdValid())
      frame.writeLine(Translate.get("virtualdatabase.xml.dtd.validated"));
    if (validator.isXmlValid())
      frame.writeLine(Translate.get("virtualdatabase.xml.document.validated"));

    if (validator.getWarnings().size() > 0)
    {
      frame.setWarning();
      ArrayList warnings = validator.getWarnings();
      for (int i = 0; i < warnings.size(); i++)
        frame.writeLine(Translate.get("virtualdatabase.xml.parsing.warning",
            warnings.get(i)));
    }

    if (!validator.isDtdValid())
    {
      frame.setWarning();
      frame.writeLine(Translate.get("virtualdatabase.xml.dtd.not.validated"));
    }
    if (!validator.isXmlValid())
    {
      frame.setWarning();
      frame.writeLine(Translate
          .get("virtualdatabase.xml.document.not.validated"));
    }

    ArrayList errors = validator.getExceptions();
    if (errors.size() > 0)
      frame.setWarning();
    for (int i = 0; i < errors.size(); i++)
      frame.writeLine(((Exception) errors.get(i)).getMessage());

  }

  /**
   * Save the document to an xml file
   * 
   * @param xmlFile the path to the xml file to save to
   * @throws IOException if fails to write
   */
  public void exportDocument(String xmlFile) throws IOException
  {
    this.document = wizardTabs.exportDocumentToXml();
    if (document != null)
      saveDocument(xmlFile);
  }

  /**
   * Start the XmlWizard
   * 
   * @param args Command line arguments
   */
  public static void main(String[] args)
  {

    if (args == null || args.length == 0)
      new XmlWizard();
    else
    {
      try
      {
        new XmlWizard(args[0]);
      }
      catch (DocumentException e)
      {
        System.out.println(WizardTranslate.get("init.error.parse.failed"));
        new XmlWizard();
      }
      catch (IOException e)
      {
        System.out.println(WizardTranslate.get("init.error.read.failed"));
        new XmlWizard();
      }
    }
  }
}