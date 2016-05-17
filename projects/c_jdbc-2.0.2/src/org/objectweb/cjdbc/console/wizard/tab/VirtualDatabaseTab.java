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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
 * This is the general panel for the configuration of the virtual database. It
 * mainly contains pools information
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class VirtualDatabaseTab extends WizardTab implements ItemListener
{
  /** Virtual database minimum of threads */
  public JSlider    minNbOfThreads;
  /** Virtual database maximum of threads */
  public JSlider    maxNbOfThreads;
  /** Virtual database maximum thread idle time */
  public JSlider    maxThreadIdleTime;
  /** Virtual database SQL dump length */
  public JSlider    sqlDumpLength;
  /** Is Virtual database distributed? */
  public JCheckBox  distributed;
  /** Virtual database BLOB filter */
  public JComboBox  blob;
  /** Virtual database pooling? */
  public JCheckBox  pool;
  /** Virtual database maximum number of connections */
  public JSlider    maxNbOfConnections;
  /** Virtual database name */
  public JTextField vdbName;

  /**
   * Creates a new <code>VirtualDatabaseTab</code> object
   * 
   * @param tabs Wizard tabs
   */
  public VirtualDatabaseTab(WizardTabs tabs)
  {
    super(tabs, WizardConstants.TAB_VIRTUAL_DATABASE);

    // panels
    JPanel general = new JPanel();
    general.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.database.general")));
    general.setLayout(new GridBagLayout());
    this.add(general, constraints);
    constraints.gridy = ++constraints.gridy;
    JPanel poolPanel = new JPanel();
    poolPanel.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.database.pool")));
    poolPanel.setLayout(new GridBagLayout());
    this.add(poolPanel, constraints);
    constraints.gridy = ++constraints.gridy;
    JPanel miscellaneous = new JPanel();
    miscellaneous.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.database.miscellaneous")));
    miscellaneous.setLayout(new GridBagLayout());
    this.add(miscellaneous, constraints);

    GridBagConstraints localconstraints = new GridBagConstraints();
    localconstraints.fill = GridBagConstraints.HORIZONTAL;
    localconstraints.weightx = 1.0;
    localconstraints.gridy = 0;

    // Name
    localconstraints.gridy = ++localconstraints.gridy;
    vdbName = new JTextField("");
    localconstraints.gridx = 0;
    general
        .add(new JLabel(WizardTranslate.get("label.name")), localconstraints);
    localconstraints.gridx = 1;
    general.add(vdbName, localconstraints);

    // Distributed
    localconstraints.gridy = ++localconstraints.gridy;
    localconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.distributed")),
        localconstraints);
    localconstraints.gridx = 1;
    distributed = new JCheckBox();
    distributed.setName("label.distributed");
    distributed.addItemListener(this);
    general.add(distributed, localconstraints);

    // maxNbOfConnections
    localconstraints.gridy = ++localconstraints.gridy;
    maxNbOfConnections = new JSlider(JSlider.HORIZONTAL, 0, 2000, 0);
    maxNbOfConnections.setPaintTicks(true);
    maxNbOfConnections.setPaintLabels(true);
    maxNbOfConnections.setMajorTickSpacing(500);
    localconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.maxNbOfConnections")),
        localconstraints);
    localconstraints.gridx = 1;
    general.add(maxNbOfConnections, localconstraints);

    localconstraints.gridy = 0;

    // pool
    localconstraints.gridy = ++localconstraints.gridy;
    pool = new JCheckBox("", true);
    pool.addItemListener(this);
    localconstraints.gridx = 0;
    poolPanel.add(new JLabel(WizardTranslate.get("label.poolThreads")),
        localconstraints);
    localconstraints.gridx = 1;
    poolPanel.add(pool, localconstraints);

    // threads
    localconstraints.gridy = ++localconstraints.gridy;
    minNbOfThreads = new JSlider(JSlider.HORIZONTAL, 0, 2000, 0);
    minNbOfThreads.setPaintTicks(true);
    minNbOfThreads.setPaintLabels(true);
    minNbOfThreads.setMajorTickSpacing(500);
    localconstraints.gridx = 0;
    poolPanel.add(new JLabel(WizardTranslate.get("label.minNbOfThreads")),
        localconstraints);
    localconstraints.gridx = 1;
    poolPanel.add(minNbOfThreads, localconstraints);

    localconstraints.gridy = ++localconstraints.gridy;
    maxNbOfThreads = new JSlider(JSlider.HORIZONTAL, 0, 2000, 0);
    maxNbOfThreads.setPaintTicks(true);
    maxNbOfThreads.setPaintLabels(true);
    maxNbOfThreads.setMajorTickSpacing(500);
    localconstraints.gridx = 0;
    poolPanel.add(new JLabel(WizardTranslate.get("label.maxNbOfThreads")),
        localconstraints);
    localconstraints.gridx = 1;
    poolPanel.add(maxNbOfThreads, localconstraints);

    localconstraints.gridy = ++localconstraints.gridy;
    maxThreadIdleTime = new JSlider(JSlider.HORIZONTAL, 0, 2000, 0);
    maxThreadIdleTime.setPaintTicks(true);
    maxThreadIdleTime.setPaintLabels(true);
    maxThreadIdleTime.setMajorTickSpacing(500);
    localconstraints.gridx = 0;
    poolPanel.add(new JLabel(WizardTranslate.get("label.maxThreadIdleTime")),
        localconstraints);
    localconstraints.gridx = 1;
    poolPanel.add(maxThreadIdleTime, localconstraints);

    localconstraints.gridy = 0;

    // sqlDumpLength
    localconstraints.gridy = ++localconstraints.gridy;
    sqlDumpLength = new JSlider(JSlider.HORIZONTAL, 0, 512, 40);
    sqlDumpLength.setPaintTicks(true);
    sqlDumpLength.setPaintLabels(true);
    sqlDumpLength.setMajorTickSpacing(100);
    localconstraints.gridx = 0;
    miscellaneous.add(new JLabel(WizardTranslate.get("label.sqlDumpLength")),
        localconstraints);
    localconstraints.gridx = 1;
    miscellaneous.add(sqlDumpLength, localconstraints);

    // blobEncodingMethod
    localconstraints.gridy = ++localconstraints.gridy;
    blob = new JComboBox(WizardConstants.BLOB);
    blob.setSelectedIndex(0);
    blob.addItemListener(this);
    localconstraints.gridx = 0;
    miscellaneous.add(new JLabel(WizardTranslate
        .get("label.blobEncodingMethod")), localconstraints);
    localconstraints.gridx = 1;
    miscellaneous.add(blob, localconstraints);

  }

  /**
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged(ItemEvent e)
  {
    JComponent box = (JComponent) e.getSource();
    if (box == pool)
    {
      if (pool.getSelectedObjects() != null)
      {
        minNbOfThreads.setEnabled(true);
        maxNbOfThreads.setEnabled(true);
        maxThreadIdleTime.setEnabled(true);
      }
      else
      {
        minNbOfThreads.setEnabled(false);
        maxNbOfThreads.setEnabled(false);
        maxThreadIdleTime.setEnabled(false);
      }
    }
    else if (box == distributed)
    {
      tabs.distributionChanged();
    }
  }

  /**
   * Is it a distributed database
   * 
   * @return true or false
   */
  public boolean isDistributedDB()
  {
    return distributed.getSelectedObjects() != null;
  }

}