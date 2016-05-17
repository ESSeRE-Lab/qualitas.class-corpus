/* $Id: SettingsTabCpp.java 374 2010-01-12 18:05:49Z linus $
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    euluis
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 1996-2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.language.cpp.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.apache.log4j.Logger;
import org.argouml.application.api.GUISettingsTabInterface;
import org.argouml.i18n.Translator;
import org.argouml.language.cpp.generator.GeneratorCpp;
import org.argouml.language.cpp.generator.Section;
import org.argouml.language.cpp.generator.Inline;
import org.argouml.moduleloader.ModuleInterface;
import org.argouml.ui.GUI;


/**
 * Settings tab for the C++ code generator.
 */
public class SettingsTabCpp implements ModuleInterface, GUISettingsTabInterface
{
    private static final String REVISION_DATE = 
        "$Date: 2010-01-12 19:05:49 +0100 (Tue, 12 Jan 2010) $"; //$NON-NLS-1$

    private static final Logger LOG = Logger.getLogger(SettingsTabCpp.class);

    private JPanel topPanel;
    private JSpinner indent;
    private JCheckBox verboseDocs;
    private JCheckBox lfBeforeCurly;
    private JComboBox useSect;
    private JCheckBox headerGuardUpperCase;
    private JCheckBox headerGuardGUID;    
    private JComboBox defaultInline;
    
    /*
     * Build the panel to be used for our settings tab.
     */
    private JPanel buildPanel() {
        LOG.debug("SettingsTabCpp being created...");
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(0, 30, 0, 5);

        // adds indent width spinner
        JLabel label = new JLabel(Translator.localize("cpp.indent"));
        // The actual value is loaded in handleSettingsTabRefresh()
        Integer spinVal = Integer.valueOf(4); 
        Integer spinMin = Integer.valueOf(0);
        Integer spinStep = Integer.valueOf(1);
        indent = new JSpinner(
                new SpinnerNumberModel(spinVal, spinMin, null, spinStep));
        label.setLabelFor(indent);
        
        JPanel indentPanel = new JPanel();
        indentPanel.setLayout(new BoxLayout(indentPanel, BoxLayout.LINE_AXIS));
        indentPanel.add(label);
        indentPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        indentPanel.add(indent);
        indentPanel.add(Box.createHorizontalGlue());
        panel.add(indentPanel, constraints);
        
        verboseDocs = new JCheckBox(Translator.localize("cpp.verbose-docs"));
        panel.add(verboseDocs, constraints);

        lfBeforeCurly = new JCheckBox(Translator
                .localize("cpp.lf-before-curly"));
        panel.add(lfBeforeCurly, constraints);
        
        // adds section combobox
        String[] sectOpts = new String[3];
        sectOpts[Section.SECT_NONE] = Translator.localize("cpp.sections.none");
        sectOpts[Section.SECT_NORMAL] = Translator
                .localize("cpp.sections.normal");
        sectOpts[Section.SECT_BRIEF] = Translator
                .localize("cpp.sections.brief"); 
        useSect = new JComboBox(sectOpts);
        label = new JLabel(Translator.localize("cpp.sections"));
        label.setLabelFor(useSect);
        JPanel sectPanel =
            new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sectPanel.add(label);
        sectPanel.add(useSect);
        panel.add(sectPanel, constraints);
        
        headerGuardUpperCase = new JCheckBox(Translator
                .localize("cpp.header-guard-case"));
        panel.add(headerGuardUpperCase, constraints);

        headerGuardGUID = new JCheckBox(Translator
                .localize("cpp.header-guard-guid"));
        panel.add(headerGuardGUID, constraints);

        // adds 'default inline' combobox
        String[] inlineOpts = Inline.getStyleLabels();
        for (int i = 0; i < inlineOpts.length; i++) {
            inlineOpts[i] = Translator.localize(inlineOpts[i]);
        }
        defaultInline = new JComboBox(inlineOpts);
        label = new JLabel(Translator.localize("cpp.default-inline"));
        label.setLabelFor(defaultInline);

        panel.add(label, constraints);
        panel.add(defaultInline, constraints);

        // TODO: add more options

        top.add(panel, BorderLayout.NORTH);

        LOG.debug("SettingsTabCpp created!");
        return top;
    }
    
    /*
     * @see org.argouml.ui.GUISettingsTabInterface#handleSettingsTabSave()
     */
    public void handleSettingsTabSave() {
        GeneratorCpp cpp = GeneratorCpp.getInstance();
        int indWidth = ((Integer) indent.getValue()).intValue();
        cpp.setIndent(indWidth);
        cpp.setLfBeforeCurly(lfBeforeCurly.isSelected());
        cpp.setVerboseDocs(verboseDocs.isSelected());
        cpp.setUseSect(useSect.getSelectedIndex());
        cpp.setHeaderGuardUpperCase(headerGuardUpperCase.isSelected());
        cpp.setHeaderGuardGUID(headerGuardGUID.isSelected());
        cpp.setDefaultInlineStyle(defaultInline.getSelectedIndex());
    }

    /*
     * @see org.argouml.ui.GUISettingsTabInterface#handleSettingsTabCancel()
     */
    public void handleSettingsTabCancel() {
    }

    /*
     * @see org.argouml.ui.GUISettingsTabInterface#handleSettingsTabRefresh()
     */
    public void handleSettingsTabRefresh() {
        GeneratorCpp cpp = GeneratorCpp.getInstance();
        lfBeforeCurly.setSelected(cpp.isLfBeforeCurly());
        verboseDocs.setSelected(cpp.isVerboseDocs());
        indent.setValue(Integer.valueOf(cpp.getIndent()));
        useSect.setSelectedIndex(cpp.getUseSect());
        headerGuardUpperCase.setSelected(cpp.isHeaderGuardUpperCase());
        headerGuardGUID.setSelected(cpp.isHeaderGuardGUID());
        defaultInline.setSelectedIndex(cpp.getDefaultInlineStyle());
    }

    /*
     * @see org.argouml.ui.GUISettingsTabInterface#handleResetToDefault()
     */
    public void handleResetToDefault() {
        // Do nothing - these buttons are not shown.
    }

    /*
     * @see org.argouml.ui.GUISettingsTabInterface#getTabKey()
     */
    public String getTabKey() { return "cpp.tabname"; }
    
    /*
     * @see org.argouml.ui.GUISettingsTabInterface#getTabPanel()
     */
    public JPanel getTabPanel() {
        // defer building this until needed
        if (topPanel == null) {
            topPanel = buildPanel();
        }
        return topPanel;
    }


    /*
     * @see org.argouml.moduleloader.ModuleInterface#getName()
     */
    public String getName() {
        return "SettingsTabCpp";
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getInfo(int)
     */
    public String getInfo(int type) {
        switch (type) {
        case ModuleInterface.AUTHOR:
            return "Daniele Tamino"; //$NON-NLS-1$
        case ModuleInterface.DESCRIPTION:
            // TODO: i18n
            return "C++ Settings";
        case ModuleInterface.VERSION:
            return "Revision date: " // TODO: i18n
                + REVISION_DATE;
        // TODO: remove duplication here and in ProfileModule.getInfo.
        case ModuleInterface.DOWNLOADSITE:
            return "http://argouml-downloads.tigris.org/"; //$NON-NLS-1$
        default:
            return null;
        }
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#enable()
     */
    public boolean enable() {
        GUI.getInstance().addSettingsTab(this);
        return true;
    }
    
    /*
     * Does nothing.  Settings tabs can't be removed after they've been added.
     * 
     * @see org.argouml.moduleloader.ModuleInterface#disable()
     */
    public boolean disable() {
        return false;
    }

}
