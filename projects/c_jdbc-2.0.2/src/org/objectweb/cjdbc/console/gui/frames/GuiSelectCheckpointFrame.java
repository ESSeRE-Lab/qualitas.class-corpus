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

package org.objectweb.cjdbc.console.gui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.objectweb.cjdbc.common.i18n.GuiTranslate;
import org.objectweb.cjdbc.console.gui.FrameConfirmKeyListener;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;

/**
 * This class defines a GuiSelectCheckpoint
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class GuiSelectCheckpointFrame extends JDialog
    implements
      WindowListener,
      WindowStateListener
{
  private JList      sampleJList;
  private JTextField valueField;
  private FrameConfirmKeyListener keyListener;

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
    this.setVisible(false);
    valueField = null;
  }

  /**
   * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
   */
  public void windowClosing(WindowEvent e)
  {
    valueField = null;
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

  }

  /**
   * @see java.awt.event.WindowStateListener#windowStateChanged(java.awt.event.WindowEvent)
   */
  public void windowStateChanged(WindowEvent e)
  {

  }

  /**
   * Creates a new <code>GuiSelectCheckpoint</code> object
   * 
   * @param owner frame owner
   * @param entries choices for selection
   * @param listener to receive events
   * @throws java.awt.HeadlessException if fails
   */
  public GuiSelectCheckpointFrame(Frame owner, String[] entries,
      ActionListener listener) throws HeadlessException
  {
    super(owner, GuiTranslate.get("frame.checkpoint.title"), true);

    // Center
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension dim = toolkit.getScreenSize();
    int screenHeight = dim.height;
    int screenWidth = dim.width;
    int frameWidth = 450;
    int frameHeight = 50;
    this.setBounds((screenWidth - frameWidth) / 2,
        (screenHeight - frameHeight) / 2, frameWidth, frameHeight);

    this.addWindowListener(this);
    this.addWindowStateListener(this);

    JButton optionConfirm = new JButton(GuiTranslate.get("frame.ok"));
    optionConfirm.setActionCommand(GuiCommands.COMMAND_HIDE_CHECKPOINT_FRAME);
    optionConfirm.addActionListener(listener);
    
    keyListener = new FrameConfirmKeyListener(optionConfirm);
    this.addKeyListener(keyListener);
    
    Container content = getContentPane();
    sampleJList = new JList(entries);
    sampleJList.setVisibleRowCount(4);
    Font displayFont = new Font("Serif", Font.BOLD, 12);
    sampleJList.setFont(displayFont);
    sampleJList.addListSelectionListener(new ValueReporter());
    JScrollPane listPane = new JScrollPane(sampleJList);
    sampleJList.addKeyListener(keyListener);

    JPanel listPanel = new JPanel();
    listPanel.setBackground(Color.white);
    Border listPanelBorder = BorderFactory.createTitledBorder(GuiTranslate
        .get("frame.checkpoint.list"));
    listPanel.setBorder(listPanelBorder);
    listPanel.add(listPane);
    content.add(listPanel, BorderLayout.CENTER);
    JLabel valueLabel = new JLabel(GuiTranslate
        .get("frame.checkpoint.selection"));
    valueLabel.setFont(displayFont);
    valueField = new JTextField(GuiConstants.BACKEND_NO_CHECKPOINT, 7);
    valueField.setFont(displayFont);
    valueField.addKeyListener(keyListener);

    JPanel valuePanel = new JPanel();
    valuePanel.setBackground(Color.white);
    Border valuePanelBorder = BorderFactory.createTitledBorder(GuiTranslate
        .get("frame.checkpoint"));
    valuePanel.setBorder(valuePanelBorder);
    valuePanel.add(valueLabel);
    valuePanel.add(valueField);
    content.add(valuePanel, BorderLayout.NORTH);

    JPanel selectPanel = new JPanel();
    selectPanel.setBackground(Color.white);
    Border selectPanelBorder = BorderFactory.createTitledBorder(GuiTranslate
        .get("frame.select"));
    selectPanel.setBorder(selectPanelBorder);
    
    selectPanel.add(optionConfirm);
    content.add(selectPanel, BorderLayout.SOUTH);
    pack();
  }

  private class ValueReporter implements ListSelectionListener
  {
    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent event)
    {
      if (!event.getValueIsAdjusting())
        valueField.setText(sampleJList.getSelectedValue().toString());
    }
  }

  /**
   * Returns the valueField value.
   * 
   * @return Returns the valueField.
   */
  public JTextField getValueField()
  {
    return valueField;
  }
}