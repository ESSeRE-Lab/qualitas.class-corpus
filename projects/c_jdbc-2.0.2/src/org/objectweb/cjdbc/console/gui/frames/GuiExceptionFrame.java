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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.objectweb.cjdbc.common.i18n.GuiTranslate;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.constants.GuiIcons;
import org.objectweb.cjdbc.console.gui.jtools.JTextAreaWriter;

/**
 * This class defines a GuiExceptionFrame
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class GuiExceptionFrame extends JDialog implements ActionListener
{
  JTextField       errorMessage;
  JTextField       classMessage;
  JTextArea        traceMessage;
  JTextAreaWriter  writer;
  PrintWriter      printWriter;
  JButton          showMe;
  static final int FRAME_WIDTH  = 500;
  static final int SHORT_HEIGHT = 200;
  static final int LONG_HEIGHT  = 400;
  private JScrollPane scrollPane;

  /**
   * Creates a new <code>GuiExceptionFrame.java</code> object
   * 
   * @param gui the parent frame
   */
  public GuiExceptionFrame(JFrame gui)
  {
    super(gui, true);
    setTitle(GuiTranslate.get("frame.exception.title"));

    
    GuiConstants.centerComponent(this,FRAME_WIDTH,SHORT_HEIGHT);
    setVisible(false);

    getContentPane().setLayout(new BorderLayout());

    GridLayout layout = new GridLayout(6, 1);
    JPanel messagePanel = new JPanel(layout);
    messagePanel.add(new JLabel(GuiTranslate.get("frame.exception.error.type")));
    classMessage = new JTextField(0);
    classMessage.setBackground(Color.white);
    classMessage.setAlignmentX(CENTER_ALIGNMENT);
    classMessage.setEditable(false);
    messagePanel.add(classMessage);

    messagePanel.add(new JLabel(GuiTranslate.get("frame.exception.error.message")));
    errorMessage = new JTextField(0);
    errorMessage.setBackground(Color.white);
    errorMessage.setAlignmentX(CENTER_ALIGNMENT);
    errorMessage.setEditable(false);
    messagePanel.add(errorMessage);

    // Trace
    JLabel label = new JLabel(GuiTranslate.get("frame.exception.stack.trace"));
    // Show/hide trace
    showMe = new JButton();
    showMe.addActionListener(this);
    setShowMeToShow();

    messagePanel.add(label);
    messagePanel.add(showMe);

    traceMessage = new JTextArea();
    traceMessage.setVisible(false);
    traceMessage.setAlignmentX(CENTER_ALIGNMENT);
    traceMessage.setEditable(false);
    traceMessage.setFont(GuiConstants.CENTER_PANE_FONT);
    traceMessage.setPreferredSize(new Dimension(FRAME_WIDTH, LONG_HEIGHT / 2));
    writer = new JTextAreaWriter(traceMessage);
    printWriter = new PrintWriter(writer);
    scrollPane = new JScrollPane();
    scrollPane.getViewport().add(traceMessage);
    scrollPane.setVisible(false);

    // on the left
    JButton iconPane = new JButton();
    Icon icon = GuiIcons.FRAME_ERROR_ICON;
    iconPane.setIcon(icon);
    Dimension dime = new Dimension(icon.getIconWidth(), icon.getIconHeight());
    iconPane.setMaximumSize(dime);
    iconPane.setPreferredSize(dime);
    iconPane.setActionCommand(GuiCommands.COMMAND_HIDE_ERROR_FRAME);
    iconPane.addActionListener(this);
    getContentPane().add(iconPane, BorderLayout.EAST);

    // on the center
    getContentPane().add(messagePanel, BorderLayout.CENTER);
    
    // on the right
    getContentPane().add(scrollPane, BorderLayout.SOUTH);

    setVisible(false);
    setBackground(Color.white);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    validate();
  }

  /**
   * Show the exception in a dialog box
   * 
   * @param e the exception
   */
  public void showException(Exception e)
  {
    errorMessage.setText(e.getMessage());
    classMessage.setText(e.getClass().getName());
    traceMessage.setText("");
    e.printStackTrace(printWriter);
    try
    {
      writer.flush();
    }
    catch (IOException e1)
    {
      // can't show, do not show
    }
    setVisible(true);
  }

  private void setShowMeToShow()
  {
    showMe.setText(GuiTranslate.get("frame.exception.show.trace"));
    showMe.setActionCommand(GuiCommands.COMMAND_SHOW_ERROR_TRACE);
    showMe.validate();
    showMe.repaint();
  }

  private void setShowMeToHide()
  {
    showMe.setText(GuiTranslate.get("frame.exception.hide.trace"));
    showMe.setActionCommand(GuiCommands.COMMAND_HIDE_ERROR_TRACE);
    showMe.validate();
    showMe.repaint();
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals(GuiCommands.COMMAND_HIDE_ERROR_FRAME))
    {
      scrollPane.setVisible(false);
      this.setVisible(false);
    }
    else if (e.getActionCommand().equals(GuiCommands.COMMAND_SHOW_ERROR_TRACE))
    {
      setSize(FRAME_WIDTH, LONG_HEIGHT);
      traceMessage.setVisible(true);
      scrollPane.setVisible(true);
      validate();
      repaint();
      scrollPane.repaint();
      setShowMeToHide();
    }
    else if (e.getActionCommand().equals(GuiCommands.COMMAND_HIDE_ERROR_TRACE))
    {
      setSize(FRAME_WIDTH, SHORT_HEIGHT);
      traceMessage.setVisible(false);
      scrollPane.setVisible(false);
      validate();
      repaint();
      scrollPane.repaint();
      setShowMeToShow();
    }
  }
}
