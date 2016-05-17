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

package org.objectweb.cjdbc.console.gui.frames;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.objectweb.cjdbc.common.i18n.GuiTranslate;
import org.objectweb.cjdbc.console.gui.FrameConfirmKeyListener;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.session.GuiSession;

/**
 * This class defines a GuiVirtualDatabaseLoginFrame
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class GuiVirtualDatabaseLoginFrame extends JDialog
{
  private JPasswordField     passwordBox;
  private JTextField         loginBox;
  private ActionListener     actionListener;
  private String             databaseName;
  private String             controllerHost;
  private String             controllerPort;
  private JButton            optionConfirm;
  private FrameConfirmKeyListener keyListener;

  /**
   * Creates a new <code>GuiVirtualDatabaseLoginFrame.java</code> object
   * 
   * @param listener that listens for actions
   * @param databaseName the name of the virtual database
   * @param parent the parent frame
   * @param controllerHost controller ip address
   * @param controllerPort controller port
   * @param session the session to retrieve database stored parameters
   */
  public GuiVirtualDatabaseLoginFrame(JFrame parent, ActionListener listener,
      String databaseName, String controllerHost, String controllerPort,
      GuiSession session)

  {
    super(parent, GuiTranslate.get("frame.database.title", databaseName), true);

    this.actionListener = listener;
    this.controllerHost = controllerHost;
    this.controllerPort = controllerPort;
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension dim = toolkit.getScreenSize();
    int screenHeight = dim.height;
    int screenWidth = dim.width;
    int frameWidth = 450;
    int frameHeight = 80;
    this.setBounds((screenWidth - frameWidth) / 2,
        (screenHeight - frameHeight) / 2, frameWidth, frameHeight);
    this.validate();
    this.setVisible(false);
    this.setResizable(false);
    this.getContentPane().setLayout(new FlowLayout());
    this.databaseName = databaseName;

    // Define confirm button before listener
    optionConfirm = new JButton(GuiTranslate.get("frame.database.approve"));
    optionConfirm.setActionCommand(GuiCommands.COMMAND_DATABASE_AUTHENTICATE);
    optionConfirm.addActionListener(actionListener);

    keyListener = new FrameConfirmKeyListener(optionConfirm);
    this.addKeyListener(keyListener);

    Dimension buttonDim = new Dimension(80, 20);

    this.getContentPane().add(
        new JLabel(GuiTranslate.get("frame.database.login")));
    loginBox = new JTextField(0);
    loginBox.setAlignmentX(CENTER_ALIGNMENT);
    String login = session.getAuthenticatedDatabaseLogin(databaseName);
    if (login != null)
      loginBox.setText(login);
    loginBox.setPreferredSize(buttonDim);
    loginBox.addActionListener(actionListener);
    loginBox.addKeyListener(keyListener);
    this.getContentPane().add(loginBox);

    this.getContentPane().add(
        new JLabel(GuiTranslate.get("frame.database.password")));
    passwordBox = new JPasswordField(0);
    passwordBox.setPreferredSize(buttonDim);
    String pass = session.getAuthenticatedDatabasePassword(databaseName);
    if (pass != null)
      passwordBox.setText(pass);
    passwordBox.setAlignmentX(CENTER_ALIGNMENT);
    passwordBox.addKeyListener(keyListener);
    passwordBox.addActionListener(actionListener);
    this.getContentPane().add(passwordBox);

    this.getContentPane().add(optionConfirm);
    this.validate();
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
  }

  /**
   * Returns the loginBox value.
   * 
   * @return Returns the loginBox.
   */
  public JTextField getLoginBox()
  {
    return loginBox;
  }

  /**
   * Returns the passwordBox value.
   * 
   * @return Returns the passwordBox.
   */
  public JTextField getPasswordBox()
  {
    return passwordBox;
  }

  /**
   * Returns the databaseName value.
   * 
   * @return Returns the databaseName.
   */
  public String getDatabaseName()
  {
    return databaseName;
  }

  /**
   * Returns the controllerHost value.
   * 
   * @return Returns the controllerHost.
   */
  public String getControllerHost()
  {
    return controllerHost;
  }

  /**
   * Returns the controllerPort value.
   * 
   * @return Returns the controllerPort.
   */
  public String getControllerPort()
  {
    return controllerPort;
  }
}