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

package org.objectweb.cjdbc.console.wizard.objects;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.wizard.WizardConstants;

/**
 * This class defines a ConnectionParameterDialog, all the forms and fields
 * needed to define a connection manager on a backend
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ConnectionParameterDialog extends JDialog
    implements
      ActionListener
{

  private ArrayList values;

  /**
   * Creates a new <code>ConnectionParameterDialog</code> object
   * 
   * @param type connection type information
   * @throws java.awt.HeadlessException if an error occurs
   */
  public ConnectionParameterDialog(ConnectionTypeInfo type)
      throws HeadlessException
  {
    super();
    this.setModal(true);
    this.setTitle(type.getType());
    this.setResizable(false);
    this.setBackground(Color.white);

    values = new ArrayList();

    this.setSize(WizardConstants.CONNECTION_FRAME_WIDTH,
        WizardConstants.CONNECTION_FRAME_HEIGHT);
    GuiConstants.centerComponent(this, WizardConstants.CONNECTION_FRAME_WIDTH,
        WizardConstants.CONNECTION_FRAME_HEIGHT);

    JPanel pane = new JPanel();
    pane.setBorder(BorderFactory.createTitledBorder(type.getType()));
    this.add(pane);

    pane.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.CENTER;

    constraints.weightx = 1.0;

    String[] atts = type.getAttributes();
    for (int i = 0; i < atts.length; i++)
    {
      constraints.gridy = ++constraints.gridy;
      constraints.gridx = 0;
      pane.add(new JLabel(atts[i]), constraints);
      constraints.gridx = 1;
      JSlider slider = new JSlider(0, 600, type.getValue(i));
      slider.setPaintLabels(true);
      slider.setPaintTicks(true);
      slider.setPaintTrack(true);
      slider.setMajorTickSpacing(100);
      pane.add(slider, constraints);
      values.add(slider);
    }

    constraints.gridy = ++constraints.gridy;
    constraints.gridx = 0;
    JButton button = new JButton(WizardTranslate.get("label.finish.edit"));
    button.addActionListener(this);
    pane.add(button, constraints);

    this.validate();
    this.setVisible(true);

  }

  /**
   * Returns the values value.
   * 
   * @return Returns the values.
   */
  public ArrayList getValues()
  {
    int size = values.size();
    ArrayList results = new ArrayList(size);
    for (int i = 0; i < size; i++)
      results.add(new Integer((((JSlider) values.get(i)).getValue())));
    return results;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    this.setVisible(false);
  }
}