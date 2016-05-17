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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.console.wizard.WizardConstants;
import org.objectweb.cjdbc.console.wizard.WizardTab;
import org.objectweb.cjdbc.console.wizard.WizardTabs;

/**
 * This tab defines the fields to fill for caching.
 * 
 * @see <code>WizardTab</code>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class CachingTab extends WizardTab implements ItemListener
{
  /** Is metadata enabled? */
  public JCheckBox metadataenable;
  /** Is parsing enabled? */
  public JCheckBox parsingenable;
  /** Is result enabled? */
  public JCheckBox resultenable;
  /** Max nb of metadata */
  public JSlider   maxNbOfMetadata;
  /** Max nb of fields */
  public JSlider   maxNbOfField;
  /** Is background parsing enabled? */
  public JCheckBox backgroundParsing;
  /** Max nb of entries */
  public JSlider   maxNbOfEntries;
  /** Max nb of result entries */
  public JSlider   resultMaxNbOfEntries;
  /** Pending timeout */
  public JSlider   pendingTimeout;
  /** Cache granularity */
  public JComboBox granularity;

  /**
   * Creates a new <code>CachingTab</code> object
   * 
   * @param tabs the wizard tas
   */
  public CachingTab(WizardTabs tabs)
  {
    super(tabs, WizardConstants.TAB_CACHING);

    ///////////////////////////////////////////////////////////////////////////
    // metadatacache panel
    ///////////////////////////////////////////////////////////////////////////

    JPanel metadatacache = new JPanel();
    metadatacache.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.metadatacache")));
    metadatacache.setLayout(new GridBagLayout());
    GridBagConstraints metadatacacheconstraints = new GridBagConstraints();
    metadatacacheconstraints.fill = GridBagConstraints.HORIZONTAL;
    metadatacacheconstraints.weightx = 1.0;

    // enable
    metadatacacheconstraints.gridy = ++metadatacacheconstraints.gridy;
    metadatacacheconstraints.gridx = 0;
    metadatacache.add(new JLabel(WizardTranslate.get("label.enable")),
        metadatacacheconstraints);
    metadatacacheconstraints.gridx = 1;
    metadataenable = new JCheckBox();
    metadataenable.addItemListener(this);
    metadatacache.add(metadataenable, metadatacacheconstraints);

    // maxNbOfMetadata
    metadatacacheconstraints.gridy = ++metadatacacheconstraints.gridy;
    maxNbOfMetadata = new JSlider(JSlider.HORIZONTAL, 0, 200000, 10000);
    maxNbOfMetadata.setPaintTicks(true);
    maxNbOfMetadata.setPaintLabels(true);
    maxNbOfMetadata.setMajorTickSpacing(50000);
    maxNbOfMetadata.setEnabled(false);
    metadatacacheconstraints.gridx = 0;
    metadatacache.add(new JLabel(WizardTranslate.get("label.maxNbOfMetadata")),
        metadatacacheconstraints);
    metadatacacheconstraints.gridx = 1;
    metadatacache.add(maxNbOfMetadata, metadatacacheconstraints);

    // maxNbOfField
    metadatacacheconstraints.gridy = ++metadatacacheconstraints.gridy;
    maxNbOfField = new JSlider(JSlider.HORIZONTAL, 0, 1000, 0);
    maxNbOfField.setPaintTicks(true);
    maxNbOfField.setPaintLabels(true);
    maxNbOfField.setMajorTickSpacing(100);
    maxNbOfField.setEnabled(false);
    metadatacacheconstraints.gridx = 0;
    metadatacache.add(new JLabel(WizardTranslate.get("label.maxNbOfFields")),
        metadatacacheconstraints);
    metadatacacheconstraints.gridx = 1;
    metadatacache.add(maxNbOfField, metadatacacheconstraints);

    this.add(metadatacache, constraints);
    constraints.gridy = ++constraints.gridy;

    ///////////////////////////////////////////////////////////////////////////
    // parsingcache panel
    ///////////////////////////////////////////////////////////////////////////

    JPanel parsingcache = new JPanel();
    parsingcache.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.parsingcache")));
    parsingcache.setLayout(new GridBagLayout());
    GridBagConstraints parsingcacheconstraints = new GridBagConstraints();
    parsingcacheconstraints.fill = GridBagConstraints.HORIZONTAL;
    parsingcacheconstraints.weightx = 1.0;

    // enable
    parsingcacheconstraints.gridy = ++parsingcacheconstraints.gridy;
    parsingcacheconstraints.gridx = 0;
    parsingcache.add(new JLabel(WizardTranslate.get("label.enable")),
        parsingcacheconstraints);
    parsingcacheconstraints.gridx = 1;
    parsingenable = new JCheckBox();
    parsingenable.addItemListener(this);
    parsingcache.add(parsingenable, parsingcacheconstraints);

    // backgroundParsing
    parsingcacheconstraints.gridy = ++parsingcacheconstraints.gridy;
    parsingcacheconstraints.gridx = 0;
    parsingcache.add(
        new JLabel(WizardTranslate.get("label.backgroundParsing")),
        parsingcacheconstraints);
    parsingcacheconstraints.gridx = 1;
    backgroundParsing = new JCheckBox();
    backgroundParsing.addItemListener(this);
    backgroundParsing.setEnabled(false);
    parsingcache.add(backgroundParsing, parsingcacheconstraints);

    // maxNbOfEntries
    parsingcacheconstraints.gridy = ++parsingcacheconstraints.gridy;
    maxNbOfEntries = new JSlider(JSlider.HORIZONTAL, 0, 10000, 5000);
    maxNbOfEntries.setPaintTicks(true);
    maxNbOfEntries.setPaintLabels(true);
    maxNbOfEntries.setMajorTickSpacing(2500);
    maxNbOfEntries.setEnabled(false);
    parsingcacheconstraints.gridx = 0;
    parsingcache.add(new JLabel(WizardTranslate.get("label.maxNbOfEntries")),
        parsingcacheconstraints);
    parsingcacheconstraints.gridx = 1;
    parsingcache.add(maxNbOfEntries, parsingcacheconstraints);

    this.add(parsingcache, constraints);
    constraints.gridy = ++constraints.gridy;

    ///////////////////////////////////////////////////////////////////////////
    // resultcache panel
    ///////////////////////////////////////////////////////////////////////////

    JPanel resultcache = new JPanel();
    resultcache.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.resultcache")));
    resultcache.setLayout(new GridBagLayout());
    GridBagConstraints resultcacheconstraints = new GridBagConstraints();
    resultcacheconstraints.fill = GridBagConstraints.HORIZONTAL;
    resultcacheconstraints.weightx = 1.0;

    // enable
    resultcacheconstraints.gridy = ++resultcacheconstraints.gridy;
    resultcacheconstraints.gridx = 0;
    resultcache.add(new JLabel(WizardTranslate.get("label.enable")),
        resultcacheconstraints);
    resultcacheconstraints.gridx = 1;
    resultenable = new JCheckBox();
    resultenable.addItemListener(this);
    resultcache.add(resultenable, resultcacheconstraints);

    // granularity
    resultcacheconstraints.gridy = ++resultcacheconstraints.gridy;
    granularity = new JComboBox(WizardConstants.RESULT_CACHE_GRANULARITY);
    granularity.setSelectedIndex(0);
    granularity.addItemListener(this);
    granularity.setEnabled(false);
    resultcacheconstraints.gridx = 0;
    resultcache.add(new JLabel(WizardTranslate.get("label.granularity")),
        resultcacheconstraints);
    resultcacheconstraints.gridx = 1;
    resultcache.add(granularity, resultcacheconstraints);

    // maxNbOfEntries
    resultcacheconstraints.gridy = ++resultcacheconstraints.gridy;
    resultMaxNbOfEntries = new JSlider(JSlider.HORIZONTAL, 0, 1000000, 100000);
    resultMaxNbOfEntries.setPaintTicks(true);
    resultMaxNbOfEntries.setMajorTickSpacing(100000);
    resultMaxNbOfEntries.setEnabled(false);
    resultcacheconstraints.gridx = 0;
    resultcache.add(new JLabel(WizardTranslate.get("label.maxNbOfEntries")),
        resultcacheconstraints);
    resultcacheconstraints.gridx = 1;
    resultcache.add(resultMaxNbOfEntries, resultcacheconstraints);

    // pendingTimeout
    resultcacheconstraints.gridy = ++resultcacheconstraints.gridy;
    pendingTimeout = new JSlider(JSlider.HORIZONTAL, 0, 3600, 0);
    pendingTimeout.setPaintTicks(true);
    pendingTimeout.setPaintLabels(true);
    pendingTimeout.setMajorTickSpacing(600);
    pendingTimeout.setEnabled(false);
    resultcacheconstraints.gridx = 0;
    resultcache.add(new JLabel(WizardTranslate.get("label.pendingTimeout")),
        resultcacheconstraints);
    resultcacheconstraints.gridx = 1;
    resultcache.add(pendingTimeout, resultcacheconstraints);

    this.add(resultcache, constraints);
    constraints.gridy = ++constraints.gridy;

  }

  /**
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged(ItemEvent e)
  {
    Object source = e.getSource();
    if (source == resultenable)
    {
      boolean enable = resultenable.getSelectedObjects() != null;
      granularity.setEnabled(enable);
      pendingTimeout.setEnabled(enable);
      resultMaxNbOfEntries.setEnabled(enable);
    }
    else if (source == parsingenable)
    {
      boolean enable = parsingenable.getSelectedObjects() != null;
      backgroundParsing.setEnabled(enable);
      maxNbOfEntries.setEnabled(enable);
    }
    else if (source == metadataenable)
    {
      boolean enable = metadataenable.getSelectedObjects() != null;
      maxNbOfMetadata.setEnabled(enable);
      maxNbOfField.setEnabled(enable);
    }
  }
}