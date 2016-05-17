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
 * Initial developer(s): Emmanuel Cecchet. 
 * Contributor(s): Mathieu Peltier,Nicolas Modrzyk
 */

package org.objectweb.cjdbc.console.gui.frames;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.objectweb.cjdbc.common.i18n.GuiTranslate;
import org.objectweb.cjdbc.console.gui.FrameConfirmKeyListener;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.objects.BackendObject;

/**
 * This class defines a NewBackendFrame.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class NewBackendFrame extends JFrame
{
  private JTextField newName;
  private JTextField newUrl;
  private JTextField newDriver;
  private JTextField newLoader;
  private BackendObject bob;
  private FrameConfirmKeyListener keyListener;

  /**
   * Returns the bob value.
   * 
   * @return Returns the bob.
   */
  public BackendObject getBob()
  {
    return bob;
  }
  /**
   * Sets the bob value.
   * 
   * @param bob The bob to set.
   */
  public void setBob(BackendObject bob)
  {
    this.bob = bob;
    newName.setText(bob.getName());
    try
    {
      newUrl.setText(bob.getMbean().getURL());
      newDriver.setText(bob.getMbean().getDriverClassName());
      newLoader.setText(bob.getMbean().getDriverPath());
    }
    catch (Exception e)
    {
      e.printStackTrace();
      newUrl.setText("");
      newDriver.setText("");
      newLoader.setText("");
    }
    
  }
  /**
   * Creates a new <code>NewBackendFrame</code> object
   * 
   * @param listener that listens to actions
   * @param bob the backend object
   */
  public NewBackendFrame(BackendObject bob,ActionListener listener)
  {
    super(GuiTranslate.get("frame.backend.title"));
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension dim = toolkit.getScreenSize();
    int screenHeight = dim.height;
    int screenWidth = dim.width;
    int frameWidth = 450;
    int frameHeight = 450;
    this.setBounds((screenWidth - frameWidth) / 2,
        (screenHeight - frameHeight) / 2, frameWidth, frameHeight);
    this.validate();
    this.setVisible(false);
    this.getContentPane().setLayout(new GridLayout(5,2));

    JButton optionConfirm = new JButton(GuiTranslate.get("frame.ok"));
    optionConfirm.setActionCommand(GuiCommands.COMMAND_CREATE_BACKEND_APPROVE);
    optionConfirm.addActionListener(listener);
    
    keyListener = new FrameConfirmKeyListener(optionConfirm);
    this.addKeyListener(keyListener);
    
    // New Name
    this.getContentPane().add(new JLabel(GuiTranslate.get("frame.backend.new.name")));
    newName = new JTextField(0);
    newName.setAlignmentX(CENTER_ALIGNMENT);
    newName.setText("");
    newName.addKeyListener(keyListener);
    this.getContentPane().add(newName);
    
    // New URL
    this.getContentPane().add(new JLabel(GuiTranslate.get("frame.backend.new.url")));
    newUrl = new JTextField(0);
    newUrl.setAlignmentX(CENTER_ALIGNMENT);
    newUrl.setText("");
    newUrl.addKeyListener(keyListener);
    this.getContentPane().add(newUrl);
    
    // New Driver
    this.getContentPane().add(new JLabel(GuiTranslate.get("frame.backend.new.driver")));
    newDriver = new JTextField(0);
    newDriver.setAlignmentX(CENTER_ALIGNMENT);
    newDriver.setText("");
    newDriver.addKeyListener(keyListener);
    this.getContentPane().add(newDriver);
    
    // New Loader
    this.getContentPane().add(new JLabel(GuiTranslate.get("frame.backend.new.loader")));
    newLoader = new JTextField(0);
    newLoader.setAlignmentX(CENTER_ALIGNMENT);
    newLoader.setText("");
    newLoader.addKeyListener(keyListener);
    this.getContentPane().add(newLoader);

    this.getContentPane().add(optionConfirm);

    JButton optionCancel = new JButton(GuiTranslate.get("frame.cancel"));
    optionCancel.setActionCommand(GuiCommands.COMMAND_CREATE_BACKEND_CANCEL);
    optionCancel.addActionListener(listener);
    this.getContentPane().add(optionCancel);

    setBob(bob);
    this.setVisible(false);
    this.setDefaultCloseOperation(HIDE_ON_CLOSE);
    this.validate();
  }
  /**
   * @return Returns the newDriver.
   */
  public JTextField getNewDriver()
  {
    return newDriver;
  }
  /**
   * @return Returns the newLoader.
   */
  public JTextField getNewLoader()
  {
    return newLoader;
  }
  /**
   * @return Returns the newName.
   */
  public JTextField getNewName()
  {
    return newName;
  }
  /**
   * @return Returns the newUrl.
   */
  public JTextField getNewUrl()
  {
    return newUrl;
  }
}


