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

package org.objectweb.cjdbc.console.gui.frames.jmxdesktop;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;

/**
 * This class defines a AttributeChangeFrame
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class AttributeChangeDialog extends JDialog implements ActionListener
{

  private static final Color BUTTON_COLOR  = new Color(198, 226, 255);
  private static final Color WARNING_COLOR = new Color(238, 169, 184);
  private JScrollPane        scrollPane;
  private JTextArea          area;
  private MBeanAttributeInfo info;
  private CjdbcGui           gui;
  private ObjectName         objectName;
  private JTextField         newValue;

  /**
   * Creates a new <code>AttributeChangeDialog</code> object
   * 
   * @param gui The GUI
   * @param name Object name
   * @param info MBean attribute info
   */
  public AttributeChangeDialog(CjdbcGui gui, ObjectName name,
      MBeanAttributeInfo info)
  {

    super(gui, "Attribute Change", true);

    this.info = info;
    this.gui = gui;
    this.objectName = name;
    GuiConstants.centerComponent(this, 400, 300);

    this.setFont(GuiConstants.CENTER_PANE_FONT);

    // Define the panels and areas
    GridBagLayout gbl = new GridBagLayout();
    this.getContentPane().setLayout(gbl);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.gridheight = 1;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    this.getContentPane().setBackground(Color.white);

    JLabel object = new JLabel(name.toString());
    object.setBorder(BorderFactory.createTitledBorder(GuiConstants.LINE_BORDER,
        "MBean Name"));
    gbl.setConstraints(object, gbc);
    this.getContentPane().add(object);

    JLabel attribute = new JLabel(info.getName());
    attribute.setBorder(BorderFactory.createTitledBorder(
        GuiConstants.LINE_BORDER, "Attribute Name (" + info.getType() + ")"));
    gbl.setConstraints(attribute, gbc);
    this.getContentPane().add(attribute);

    JPanel values = new JPanel();
    values.setBorder(BorderFactory.createTitledBorder(GuiConstants.LINE_BORDER,
        "Attribute Value"));
    values.setBackground(Color.white);
    values.setLayout(new GridLayout(2, 2));
    values.add(new JLabel("Old Value"));

    JTextField oldValue = new JTextField();
    Object attributeValue = null;
    try
    {
      attributeValue = gui.getCurrentJmxClient().getAttributeValue(name,
          info.getName());
    }
    catch (Exception e)
    {
      oldValue.setForeground(WARNING_COLOR);
      attributeValue = "<error>";
    }
    finally
    {
      if (attributeValue == null)
        attributeValue = "";
    }
    oldValue.setText(attributeValue.toString());
    oldValue.setEditable(false);
    values.add(oldValue);
    values.add(new JLabel("New Value"));
    newValue = new JTextField();
    if (!info.isWritable())
    {
      newValue.setText(" ");
      newValue.setEnabled(false);
    }
    values.add(newValue);
    gbl.setConstraints(values, gbc);
    this.getContentPane().add(values);

    JButton ok;
    if (!info.isWritable())
    {
      ok = new JButton("Cannot change attribute value");
      ok.setBackground(WARNING_COLOR);
      ok.setEnabled(false);
    }
    else
    {
      ok = new JButton("Change attribute value");
      ok.setBackground(BUTTON_COLOR);
      ok.setEnabled(true);
    }
    ok.setActionCommand(GuiCommands.COMMAND_CONFIRM_ACTION);
    ok.addActionListener(this);
    gbl.setConstraints(ok, gbc);
    this.getContentPane().add(ok);

    scrollPane = new JScrollPane();
    scrollPane.setBackground(Color.white);
    scrollPane.setViewportBorder(BorderFactory.createTitledBorder(
        GuiConstants.LINE_BORDER, "Attribute Change Result"));
    area = new JTextArea();
    area.setBackground(Color.white);
    scrollPane.getViewport().add(area);
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weighty = 2.0;
    gbl.setConstraints(scrollPane, gbc);
    this.getContentPane().add(scrollPane);

    this.validate();

  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    if (!info.isWritable())
    {
      area.setForeground(WARNING_COLOR);
      area.setText("This attribute cannot be changed");
    }
    else
    {
      try
      {
        Object o = GuiConstants.convertType(newValue.getText(), info.getType());
        gui.getCurrentJmxClient().setAttributeValue(objectName, info.getName(),
            o);
        area.setForeground(Color.BLACK);
        area.setText("Attribute value chaned");
      }
      catch (Exception e1)
      {
        area.setForeground(WARNING_COLOR);
        area.setText(e1.getMessage());
      }
    }
  }
}
