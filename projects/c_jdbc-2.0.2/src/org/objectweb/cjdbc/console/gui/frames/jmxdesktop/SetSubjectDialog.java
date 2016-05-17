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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.objectweb.cjdbc.common.i18n.GuiTranslate;
import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;

/**
 * This class defines a SetSubjectDialog
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class SetSubjectDialog extends JDialog implements ActionListener
{

  private JTextField user;
  private JTextField password;
  private CjdbcGui   gui;

  /**
   * Creates a new <code>SetSubjectDialog</code> object
   * 
   * @param gui the GUI
   * @throws java.awt.HeadlessException
   */
  public SetSubjectDialog(CjdbcGui gui)
  {
    super(gui, "Set Subject", true);
    GuiConstants.centerComponent(this, 300, 100);

    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().setLayout(new GridLayout(3, 2));

    user = new JTextField("");
    JLabel userLabel = new JLabel("User");
    this.getContentPane().add(userLabel);
    this.getContentPane().add(user);

    password = new JTextField("");
    JLabel passwordLabel = new JLabel("Password");
    this.getContentPane().add(passwordLabel);
    this.getContentPane().add(password);

    JButton ok = new JButton(GuiTranslate
        .get("frame.ok"));
    ok.setActionCommand(GuiCommands.COMMAND_CONFIRM_ACTION);
    ok.addActionListener(this);
    this.getContentPane().add(ok);

    JButton cancel = new JButton(GuiTranslate
        .get("frame.cancel"));
    cancel.setActionCommand(GuiCommands.COMMAND_CANCEL_ACTION);
    cancel.addActionListener(this);
    this.getContentPane().add(cancel);

    this.validate();
    this.gui = gui;
    this.setVisible(true);

  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equalsIgnoreCase(
        GuiCommands.COMMAND_CONFIRM_ACTION))
    {
      gui.getCurrentJmxClient().setCurrentSubject(user.getText(),
          password.getText());
      this.setVisible(false);
    }
    else if (e.getActionCommand().equalsIgnoreCase(
        GuiCommands.COMMAND_CANCEL_ACTION))
    {
      this.setVisible(false);
    }
  }
}
