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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.objectweb.cjdbc.common.i18n.GuiTranslate;
import org.objectweb.cjdbc.console.gui.FrameConfirmKeyListener;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;

/**
 * This class defines a GuiNewControllerFrame
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class GuiNewControllerFrame extends JFrame
{
  private JTextField     portNumber;
  private JTextField     ipAddressBox;
  private ActionListener actionListener;
  private FrameConfirmKeyListener keyListener;

  /**
   * Creates a new <code>GuiNewControllerFrame.java</code> object
   * 
   * @param listener that listens to actions
   */
  public GuiNewControllerFrame(ActionListener listener)
  {
    super(GuiTranslate.get("frame.controller.title"));
    this.actionListener = listener;
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension dim = toolkit.getScreenSize();
    int screenHeight = dim.height;
    int screenWidth = dim.width;
    int frameWidth = 450;
    int frameHeight = 50;
    this.setBounds((screenWidth - frameWidth) / 2,
        (screenHeight - frameHeight) / 2, frameWidth, frameHeight);
    this.validate();
    this.setVisible(false);
    this.getContentPane().setLayout(new FlowLayout());

    JButton optionConfirm = new JButton(GuiTranslate.get("frame.ok"));
    optionConfirm.setActionCommand(GuiCommands.COMMAND_ADD_CONTROLLER_APPROVE);
    optionConfirm.addActionListener(actionListener);
    
    keyListener = new FrameConfirmKeyListener(optionConfirm);
    this.addKeyListener(keyListener);
    
    this.getContentPane().add(new JLabel(GuiTranslate.get("frame.controller.host")));
    ipAddressBox = new JTextField(0);
    ipAddressBox.setAlignmentX(CENTER_ALIGNMENT);
    ipAddressBox.setText("localhost");
    ipAddressBox.addActionListener(actionListener);
    ipAddressBox.addKeyListener(keyListener);
    this.getContentPane().add(ipAddressBox);

    this.getContentPane().add(new JLabel(GuiTranslate.get("frame.controller.port")));
    portNumber = new JTextField(0);
    portNumber.setAlignmentX(CENTER_ALIGNMENT);
    portNumber.setText("1090");
    portNumber.addActionListener(actionListener);
    portNumber.addKeyListener(keyListener);
    this.getContentPane().add(portNumber);

    
    this.getContentPane().add(optionConfirm);

    JButton optionCancel = new JButton(GuiTranslate.get("frame.cancel"));
    optionCancel.setActionCommand(GuiCommands.COMMAND_ADD_CONTROLLER_CANCEL);
    optionCancel.addActionListener(actionListener);
    this.getContentPane().add(optionCancel);

    this.setVisible(false);
    this.setDefaultCloseOperation(HIDE_ON_CLOSE);
    this.validate();
  }
  /**
   * Returns the ipAddressBox value.
   * 
   * @return Returns the ipAddressBox.
   */
  public JTextField getIpAddressBox()
  {
    return ipAddressBox;
  }
  /**
   * Returns the portNumber value.
   * 
   * @return Returns the portNumber.
   */
  public JTextField getPortNumber()
  {
    return portNumber;
  }
}
