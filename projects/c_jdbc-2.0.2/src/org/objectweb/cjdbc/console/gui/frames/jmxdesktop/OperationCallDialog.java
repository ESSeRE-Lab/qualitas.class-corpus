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
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.objectweb.cjdbc.common.jmx.JmxConstants;
import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.jtools.JTextAreaWriter;

/**
 * This class defines a OperationCallDialog
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class OperationCallDialog extends JDialog implements ActionListener
{

  CjdbcGui                     gui;
  ObjectName                   objectN;
  MBeanOperationInfo           info;
  private JTextArea            area;
  private JScrollPane          scrollPane;
  private MBeanParameterInfo[] params;
  private int                  length;
  private JTextField[]         fields;

  /**
   * Creates a new <code>OperationCallDialog</code> object
   * 
   * @param gui the GUI
   * @param name the object name
   * @param info the MBean operation info
   * @throws java.awt.HeadlessException if an error occurs
   */
  public OperationCallDialog(CjdbcGui gui, ObjectName name,
      MBeanOperationInfo info) throws HeadlessException
  {
    super(gui, "Operation Call Dialog", true);
    GuiConstants.centerComponent(this, 400, 500);
    this.gui = gui;
    this.objectN = name;
    this.info = info;

    // Define the panels and areas
    GridBagLayout gbl = new GridBagLayout();
    this.getContentPane().setLayout(gbl);
    this.setFont(GuiConstants.CENTER_PANE_FONT);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.gridheight = 1;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    this.getContentPane().setBackground(Color.white);

    JLabel objectName = new JLabel(name.toString());
    objectName.setBorder(BorderFactory.createTitledBorder(
        GuiConstants.LINE_BORDER, "MBean Name"));
    gbl.setConstraints(objectName, gbc);
    this.getContentPane().add(objectName);

    scrollPane = new JScrollPane();
    scrollPane.setBackground(Color.white);
    scrollPane.setViewportBorder(BorderFactory.createTitledBorder(
        GuiConstants.LINE_BORDER, "Operation Result"));
    area = new JTextArea();
    area.setBackground(Color.white);
    scrollPane.getViewport().add(area);

    JLabel operation = new JLabel(info.getName());
    operation.setBorder(BorderFactory.createTitledBorder(
        GuiConstants.LINE_BORDER, "Operation Name"));
    gbl.setConstraints(operation, gbc);
    this.getContentPane().add(operation);

    JPanel operationPane = new JPanel();
    operationPane.setBackground(Color.white);
    operationPane.setBorder(BorderFactory.createTitledBorder(
        GuiConstants.LINE_BORDER, "MBean parameters"));
    params = info.getSignature();
    length = params.length;
    GridLayout gl = new GridLayout(info.getSignature().length, 2);
    operationPane.setLayout(gl);
    fields = new JTextField[length];
    for (int i = 0; i < length; i++)
    {
      operationPane.add(new JLabel(params[i].getType()));
      fields[i] = new JTextField("");
      operationPane.add(fields[i]);
    }
    gbl.setConstraints(operationPane, gbc);
    this.getContentPane().add(operationPane);

    JButton ok = new JButton("Run Jmx operation");
    ok.setBackground(new Color(198, 226, 255));
    ok.setActionCommand(GuiCommands.COMMAND_CONFIRM_ACTION);
    ok.addActionListener(this);
    gbl.setConstraints(ok, gbc);
    this.getContentPane().add(ok);

    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weighty = 2.0;
    gbl.setConstraints(scrollPane, gbc);
    this.getContentPane().add(scrollPane);

    this.validate();

  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent event)
  {
    try
    {
      area.setForeground(Color.BLACK);
      if (gui.getCurrentJmxClient().isSubjectSet() == false
          && JmxConstants.mbeanNeedAuthentication(objectN))
        new SetSubjectDialog(gui);
      Object[] args = new Object[length];
      for (int i = 0; i < length; i++)
      {
        args[i] = getParameter(i);
      }
      Object result = gui.getCurrentJmxClient().invokeOperation(objectN, info,
          args);
      if (result != null)
        area.setText(result.toString());
      else
        area.setText("Command did not return a result");

      area.validate();
      scrollPane.validate();
    }
    catch (Exception e)
    {
      area.setForeground(Color.RED);
      area.setText(e.getMessage());
      JTextAreaWriter areaWriter = new JTextAreaWriter(area);
      PrintWriter writer = new PrintWriter(areaWriter);
      e.printStackTrace(writer);
    }
  }

  private Object getParameter(int i)
  {
    String value = fields[i].getText();
    String type = params[i].getType();
    return GuiConstants.convertType(value, type);
  }

}
