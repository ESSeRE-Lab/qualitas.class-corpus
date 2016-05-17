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

package org.objectweb.cjdbc.console.wizard.tab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.console.wizard.WizardConstants;
import org.objectweb.cjdbc.console.wizard.WizardTab;
import org.objectweb.cjdbc.console.wizard.WizardTabs;

/**
 * The recovery tab has the fields needed to define a
 * <code>RecoveryLog</code> This tab can be extended later to use for
 * other recovery log types.
 * 
 * @see <code>WizardTab</code>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class RecoveryTab extends WizardTab implements ActionListener
{
  /** Recovery log driver */
  public JTextField driver;
  /** Recovery log driver path */
  public JTextField driverPath;
  /** Recovery log login */
  public JTextField login;
  /** Recovery log url */
  public JTextField url;
  /** Recovery log password */
  public JTextField password;
  /** Recovery log request timeout */
  public JSlider    requestTimeout;
  /** Recovery log backend select */
  public JButton    selectBackend;

  /**
   * Creates a new <code>RecoveryTab</code> object
   * 
   * @param tabs the wizard tabs
   */
  public RecoveryTab(WizardTabs tabs)
  {
    super(tabs, WizardConstants.TAB_RECOVERY);

    ///////////////////////////////////////////////////////////////////////////
    // jdbc recovery panel
    ///////////////////////////////////////////////////////////////////////////

    JPanel general = new JPanel();
    general.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.jdbcrecovery")));
    general.setLayout(new GridBagLayout());
    GridBagConstraints generalconstraints = new GridBagConstraints();
    generalconstraints.fill = GridBagConstraints.HORIZONTAL;
    generalconstraints.weightx = 1.0;

    // Driver
    generalconstraints.gridy = ++generalconstraints.gridy;
    driver = new JTextField("");
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.backendDriver")),
        generalconstraints);
    generalconstraints.gridx = 1;
    general.add(driver, generalconstraints);

    //  Driver path
    generalconstraints.gridy = ++generalconstraints.gridy;
    driverPath = new JTextField("");
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.backendDriverPath")),
        generalconstraints);
    generalconstraints.gridx = 1;
    general.add(driverPath, generalconstraints);

    //  url
    generalconstraints.gridy = ++generalconstraints.gridy;
    url = new JTextField("");
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.backendUrl")),
        generalconstraints);
    generalconstraints.gridx = 1;
    general.add(url, generalconstraints);

    //  login
    generalconstraints.gridy = ++generalconstraints.gridy;
    login = new JTextField("");
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.username")),
        generalconstraints);
    generalconstraints.gridx = 1;
    general.add(login, generalconstraints);

    //  password
    generalconstraints.gridy = ++generalconstraints.gridy;
    password = new JTextField("");
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.password")),
        generalconstraints);
    generalconstraints.gridx = 1;
    general.add(password, generalconstraints);

    // request timeout
    generalconstraints.gridy = ++generalconstraints.gridy;
    requestTimeout = new JSlider(JSlider.HORIZONTAL, 0, 2000, 60);
    requestTimeout.setPaintTicks(true);
    requestTimeout.setPaintLabels(true);
    requestTimeout.setMajorTickSpacing(500);
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.requestTimeout")),
        generalconstraints);
    generalconstraints.gridx = 1;
    general.add(requestTimeout, generalconstraints);

    // select backend type button
    generalconstraints.gridy = ++generalconstraints.gridy;
    generalconstraints.gridy = ++generalconstraints.gridy;
    selectBackend = new JButton(WizardTranslate.get("label.selectbackend"));
    selectBackend.addActionListener(this);
    generalconstraints.gridx = 1;
    general.add(selectBackend, generalconstraints);

    this.add(general, constraints);
    constraints.gridy = ++constraints.gridy;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    JComponent source = (JComponent) e.getSource();
    if (source == selectBackend)
    {
      String select = WizardTab.showBackendSelectDialog();
      if (select != null)
      {
        url.setText(types.getString(select + ".url"));
        driver.setText(types.getString(select + ".driver"));
      }
    }

  }

}