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

import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.console.wizard.WizardConstants;
import org.objectweb.cjdbc.console.wizard.WizardTab;
import org.objectweb.cjdbc.console.wizard.WizardTabs;

/**
 * RequestManager is the main panel for the information regarding the request
 * manager. This tab will open tabs like <code>CachingTab</code> and
 * <code>RecoveryTab</code>
 * 
 * @see <code>WizardTab</code>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class RequestManagerTab extends WizardTab implements ItemListener
{
  /** Request manager case sensitive parsing */
  public JCheckBox          caseSensitiveParsing;
  /** Request manager begin timeout */
  public JSlider            beginTimeout;
  /** Request manager commit timeout */
  public JSlider            commitTimeout;
  /** Request manager rollback timeout */
  public JSlider            rollbackTimeout;
  /** Request manager scheduler */
  public JComboBox          scheduler;
  /** Request manager scheduler panel */
  public JPanel             shedulerPanel;
  /** Request manager scheduler panel constraints */
  public GridBagConstraints shedulerPanelconstraints;
  /** Request manager scheduler level */
  public JComboBox          schedulerLevel;
  /** Request manager load balancer */
  public JComboBox          loadbalancer;
  /** Request manager load balancer panel constraints */
  public GridBagConstraints loadbalancerPanelconstraints;
  /** Request manager load balancer panel */
  public JPanel             loadbalancerPanel;
  /** Request manager caching usage */
  public JCheckBox          usecaching;
  /** Request manager recovery log usage */
  public JCheckBox          userecoverylog;
  /** Request manager load balancer Wait For Completion policy */
  public JComboBox          wait4completion;

  /**
   * Creates a new <code>RequestManagerTab</code> object
   * 
   * @param tabs the wizard tabs
   */
  public RequestManagerTab(WizardTabs tabs)
  {
    super(tabs, WizardConstants.TAB_REQUEST_MANAGER);

    ///////////////////////////////////////////////////////////////////////////
    // general panel
    ///////////////////////////////////////////////////////////////////////////

    JPanel general = new JPanel();
    general.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.scheduler")));
    general.setLayout(new GridBagLayout());
    GridBagConstraints generalconstraints = new GridBagConstraints();
    generalconstraints.fill = GridBagConstraints.HORIZONTAL;
    generalconstraints.weightx = 1.0;

    // caseSensitiveParsing
    generalconstraints.gridy = ++generalconstraints.gridy;
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.caseSensitiveParsing")),
        generalconstraints);
    generalconstraints.gridx = 1;
    caseSensitiveParsing = new JCheckBox();
    caseSensitiveParsing.setName("label.caseSensitiveParsing");
    general.add(caseSensitiveParsing, generalconstraints);

    // beginTimeout
    generalconstraints.gridy = ++generalconstraints.gridy;
    beginTimeout = new JSlider(JSlider.HORIZONTAL, 0, 2000, 60);
    beginTimeout.setPaintTicks(true);
    beginTimeout.setPaintLabels(true);
    beginTimeout.setMajorTickSpacing(500);
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.beginTimeout")),
        generalconstraints);
    generalconstraints.gridx = 1;
    general.add(beginTimeout, generalconstraints);

    // commitTimeout
    generalconstraints.gridy = ++generalconstraints.gridy;
    commitTimeout = new JSlider(JSlider.HORIZONTAL, 0, 2000, 60);
    commitTimeout.setPaintTicks(true);
    commitTimeout.setPaintLabels(true);
    commitTimeout.setMajorTickSpacing(500);
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.commitTimeout")),
        generalconstraints);
    generalconstraints.gridx = 1;
    general.add(commitTimeout, generalconstraints);

    // beginTimeout
    generalconstraints.gridy = ++generalconstraints.gridy;
    rollbackTimeout = new JSlider(JSlider.HORIZONTAL, 0, 2000, 60);
    rollbackTimeout.setPaintTicks(true);
    rollbackTimeout.setPaintLabels(true);
    rollbackTimeout.setMajorTickSpacing(500);
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.rollbackTimeout")),
        generalconstraints);
    generalconstraints.gridx = 1;
    general.add(rollbackTimeout, generalconstraints);

    // use caching
    generalconstraints.gridy = ++generalconstraints.gridy;
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.usecaching")),
        generalconstraints);
    generalconstraints.gridx = 1;
    usecaching = new JCheckBox();
    usecaching.setName("label.usecaching");
    usecaching.addItemListener(this);
    general.add(usecaching, generalconstraints);

    // use recovery log
    generalconstraints.gridy = ++generalconstraints.gridy;
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.userecoverylog")),
        generalconstraints);
    generalconstraints.gridx = 1;
    userecoverylog = new JCheckBox();
    userecoverylog.addItemListener(this);
    userecoverylog.setName("label.userecoverylog");
    general.add(userecoverylog, generalconstraints);

    this.add(general, constraints);
    constraints.gridy = ++constraints.gridy;

    ///////////////////////////////////////////////////////////////////////////
    // scheduler panel
    ///////////////////////////////////////////////////////////////////////////

    shedulerPanel = new JPanel();
    shedulerPanel.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.scheduler")));
    shedulerPanel.setLayout(new GridBagLayout());
    shedulerPanelconstraints = new GridBagConstraints();
    shedulerPanelconstraints.fill = GridBagConstraints.HORIZONTAL;
    shedulerPanelconstraints.weightx = 1.0;

    // scheduler list
    scheduler = new JComboBox(WizardConstants.SCHEDULERS_STANDARD);
    scheduler.setSelectedIndex(0);
    scheduler.addItemListener(this);
    shedulerPanelconstraints.gridx = 0;
    shedulerPanel.add(new JLabel(WizardTranslate.get("label.scheduler.type")),
        shedulerPanelconstraints);
    shedulerPanelconstraints.gridx = 1;
    shedulerPanel.add(scheduler, shedulerPanelconstraints);

    shedulerPanelconstraints.gridy = 1;
    schedulerLevel = new JComboBox(WizardConstants.SCHEDULER_SINGLEDB_LEVELS);
    schedulerLevel.setSelectedIndex(0);
    schedulerLevel.addItemListener(this);
    shedulerPanelconstraints.gridx = 0;
    shedulerPanel.add(new JLabel(WizardTranslate.get("label.scheduler.level")),
        shedulerPanelconstraints);
    shedulerPanelconstraints.gridx = 1;
    shedulerPanel.add(schedulerLevel, shedulerPanelconstraints);

    this.add(shedulerPanel, constraints);
    constraints.gridy = ++constraints.gridy;

    ///////////////////////////////////////////////////////////////////////////
    // loadbalancer panel
    ///////////////////////////////////////////////////////////////////////////

    loadbalancerPanel = new JPanel();
    loadbalancerPanel.setBorder(BorderFactory
        .createTitledBorder(WizardTranslate.get("label.loadbalancer")));
    loadbalancerPanel.setLayout(new GridBagLayout());
    loadbalancerPanelconstraints = new GridBagConstraints();
    loadbalancerPanelconstraints.fill = GridBagConstraints.HORIZONTAL;
    loadbalancerPanelconstraints.weightx = 1.0;

    loadbalancer = new JComboBox(WizardConstants.LOAD_BALANCER_SINGLEDB);
    loadbalancer.setSelectedIndex(0);
    loadbalancer.addItemListener(this);
    loadbalancerPanelconstraints.gridx = 0;
    loadbalancerPanel.add(new JLabel(WizardTranslate
        .get("label.loadbalancer.type")), loadbalancerPanelconstraints);
    loadbalancerPanelconstraints.gridx = 1;
    loadbalancerPanel.add(loadbalancer, loadbalancerPanelconstraints);

    loadbalancerPanelconstraints.gridy = 1;
    wait4completion = new JComboBox(WizardConstants.WAIT_POLICIES);
    wait4completion.setEnabled(false);
    loadbalancerPanelconstraints.gridx = 0;
    loadbalancerPanel.add(new JLabel(WizardTranslate
        .get("label.loadbalancer.wait")), loadbalancerPanelconstraints);
    loadbalancerPanelconstraints.gridx = 1;
    loadbalancerPanel.add(wait4completion, loadbalancerPanelconstraints);

    this.add(loadbalancerPanel, constraints);
  }

  /**
   * @see org.objectweb.cjdbc.console.wizard.listeners.WizardListener#distributionChanged()
   */
  public void distributionChanged()
  {
    shedulerPanel.remove(scheduler);
    if (tabs.isDistributedDatabase())
    {
      scheduler = new JComboBox(WizardConstants.SCHEDULERS_DISTRIBUTED);
    }
    else
    {
      scheduler = new JComboBox(WizardConstants.SCHEDULERS_STANDARD);
    }
    scheduler.setSelectedIndex(0);
    scheduler.addItemListener(this);
    shedulerPanelconstraints.gridx = 1;
    shedulerPanelconstraints.gridy = 0;
    shedulerPanel.add(scheduler, shedulerPanelconstraints);
    shedulerPanel.validate();
    shedulerPanel.repaint();
  }

  /**
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged(ItemEvent e)
  {
    JComponent source = (JComponent) e.getSource();
    if (source.equals(scheduler))
    {
      // change scheduler level
      shedulerPanel.remove(schedulerLevel);
      if (scheduler.getSelectedIndex() == 0)
        schedulerLevel = new JComboBox(
            WizardConstants.SCHEDULER_SINGLEDB_LEVELS);
      else if (scheduler.getSelectedIndex() == 1)
        schedulerLevel = new JComboBox(WizardConstants.SCHEDULER_RAIDB0_LEVELS);
      else if (scheduler.getSelectedIndex() == 2)
        schedulerLevel = new JComboBox(WizardConstants.SCHEDULER_RAIDB1_LEVELS);
      else if (scheduler.getSelectedIndex() == 3)
        schedulerLevel = new JComboBox(WizardConstants.SCHEDULER_RAIDB2_LEVELS);

      shedulerPanelconstraints.gridx = 1;
      shedulerPanelconstraints.gridy = 1;
      schedulerLevel.setSelectedIndex(0);
      schedulerLevel.addItemListener(this);
      shedulerPanel.add(schedulerLevel, shedulerPanelconstraints);
      shedulerPanel.validate();
      shedulerPanel.repaint();

      // change loadbalancer list
      loadbalancerPanel.remove(loadbalancer);
      if (scheduler.getSelectedIndex() == 0)
        loadbalancer = new JComboBox(WizardConstants.LOAD_BALANCER_SINGLEDB);
      else if (scheduler.getSelectedIndex() == 1)
        loadbalancer = new JComboBox(WizardConstants.LOAD_BALANCER_RAIDB0);
      else if (scheduler.getSelectedIndex() == 2)
        loadbalancer = new JComboBox(WizardConstants.LOAD_BALANCER_RAIDB1);
      else if (scheduler.getSelectedIndex() == 3)
        loadbalancer = new JComboBox(WizardConstants.LOAD_BALANCER_RAIDB2);

      loadbalancerPanelconstraints.gridx = 1;
      loadbalancerPanelconstraints.gridy = 0;
      loadbalancer.setSelectedIndex(0);
      loadbalancer.addItemListener(this);
      loadbalancerPanel.add(loadbalancer, loadbalancerPanelconstraints);
      loadbalancerPanel.validate();
      loadbalancerPanel.repaint();

    }
    else if (source.equals(usecaching))
    {
      if (usecaching.getSelectedObjects() != null)
        tabs.setTabEnabled(WizardConstants.TAB_CACHING, true);
      else
        tabs.setTabEnabled(WizardConstants.TAB_CACHING, false);
    }
    else if (source.equals(userecoverylog))
    {
      if (userecoverylog.getSelectedObjects() != null)
        tabs.setTabEnabled(WizardConstants.TAB_RECOVERY, true);
      else
        tabs.setTabEnabled(WizardConstants.TAB_RECOVERY, false);
    }

    // enable wait4completion combo ?
    if (source.equals(loadbalancer) || source.equals(scheduler))
    {
      String selected = (String) loadbalancer.getSelectedItem();
      if (selected.startsWith(DatabasesXmlTags.ELT_RAIDb_1)
          || selected.startsWith(DatabasesXmlTags.ELT_RAIDb_2))
        wait4completion.setEnabled(true);
      else
        wait4completion.setEnabled(false);
    }
  }
}