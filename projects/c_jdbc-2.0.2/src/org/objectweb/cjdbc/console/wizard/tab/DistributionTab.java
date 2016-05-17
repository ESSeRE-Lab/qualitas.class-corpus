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

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.console.wizard.WizardConstants;
import org.objectweb.cjdbc.console.wizard.WizardTab;
import org.objectweb.cjdbc.console.wizard.WizardTabs;

/**
 * Distribution tab has the fields for distributed virtual database.
 * 
 * @see <code>WizardTab</code>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class DistributionTab extends WizardTab
{
  /** Group communication cast timeout */
  public JSlider    castTimeout;
  /** Macro clock for distributed databases */
  public JComboBox  macroClock;
  /** Group communication name */
  public JTextField groupName;

  /**
   * Creates a new <code>DistributionTab</code> object
   * 
   * @param tabs the wizard tabs
   */
  public DistributionTab(WizardTabs tabs)
  {
    super(tabs, WizardConstants.TAB_DISTRIBUTION);
    this.setVisible(false);

    // panels
    JPanel general = new JPanel();
    general.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.distribution.general")));
    general.setLayout(new GridBagLayout());
    constraints.fill = GridBagConstraints.HORIZONTAL;
    this.add(general, constraints);

    // constraints
    GridBagConstraints localconstraints = new GridBagConstraints();
    localconstraints.fill = GridBagConstraints.HORIZONTAL;
    localconstraints.weightx = 1.0;
    localconstraints.gridy = 0;

    // groupName
    localconstraints.gridy = ++localconstraints.gridy;
    groupName = new JTextField("");
    localconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.groupName")),
        localconstraints);
    localconstraints.gridx = 1;
    general.add(groupName, localconstraints);

    // macroClock
    localconstraints.gridy = ++localconstraints.gridy;
    macroClock = new JComboBox(WizardConstants.MACRO_CLOCK);
    macroClock.setSelectedIndex(0);
    localconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.macroClock")),
        localconstraints);
    localconstraints.gridx = 1;
    general.add(macroClock, localconstraints);

    // castTimeout
    localconstraints.gridy = ++localconstraints.gridy;
    castTimeout = new JSlider(JSlider.HORIZONTAL, 0, 2000, 0);
    castTimeout.setPaintTicks(true);
    castTimeout.setPaintLabels(true);
    castTimeout.setMajorTickSpacing(500);
    localconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.castTimeout")),
        localconstraints);
    localconstraints.gridx = 1;
    general.add(castTimeout, localconstraints);

  }

}