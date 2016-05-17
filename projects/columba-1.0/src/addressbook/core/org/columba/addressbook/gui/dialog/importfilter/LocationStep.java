//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.addressbook.gui.dialog.importfilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.javaprog.ui.wizard.AbstractStep;
import net.javaprog.ui.wizard.DataLookup;
import net.javaprog.ui.wizard.DataModel;

import org.columba.addressbook.folder.IFolder;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.gui.tree.util.ISelectFolderDialog;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.gui.base.LabelWithMnemonic;
import org.columba.core.gui.base.MultiLineLabel;
import org.columba.core.gui.base.WizardTextField;


class LocationStep extends AbstractStep implements ActionListener {
    protected File sourceFile;
    protected IFolder destinationFolder;
    protected JButton sourceButton;
    protected JButton destinationButton;

    public LocationStep(DataModel data) {
        super(AddressbookResourceLoader.getString("dialog",
                "addressbookimport", "location"),
            AddressbookResourceLoader.getString("dialog", "addressbookimport",
                "location_description"));
        data.registerDataLookup("Location.source",
            new DataLookup() {
                public Object lookupData() {
                    return sourceFile;
                }
            });
        data.registerDataLookup("Location.destination",
            new DataLookup() {
                public Object lookupData() {
                    return destinationFolder;
                }
            });
        setCanGoNext(false);
    }

    protected JComponent createComponent() {
        JComponent component = new JPanel();
        component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
        component.add(new MultiLineLabel(AddressbookResourceLoader.getString(
                    "dialog", "addressbookimport", "location_text")));
        component.add(Box.createVerticalStrut(40));

        WizardTextField middlePanel = new WizardTextField();
        LabelWithMnemonic sourceLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
                    "dialog", "addressbookimport", "source"));
        middlePanel.addLabel(sourceLabel);
        sourceButton = new JButton("...");
        sourceButton.addActionListener(this);
        sourceLabel.setLabelFor(sourceButton);
        middlePanel.addTextField(sourceButton);
        middlePanel.addExample(new JLabel());

        LabelWithMnemonic destinationLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
                    "dialog", "addressbookimport", "destination"));
        middlePanel.addLabel(destinationLabel);
        destinationButton = new JButton("...");
        destinationButton.addActionListener(this);
        destinationLabel.setLabelFor(destinationButton);
        middlePanel.addTextField(destinationButton);
        middlePanel.addExample(new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "addressbookimport", "explanation")));
        component.add(middlePanel);

        return component;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == sourceButton) {
            JFileChooser fc = new JFileChooser();
            fc.setFileHidingEnabled(false);

            if (fc.showOpenDialog(getComponent()) == JFileChooser.APPROVE_OPTION) {
                sourceFile = fc.getSelectedFile();
                sourceButton.setText(sourceFile.getPath());
                updateCanFinish();
            }
        } else if (source == destinationButton) {
            ISelectFolderDialog dialog = AddressbookTreeModel.getInstance().getSelectAddressbookFolderDialog();

            if (dialog.success()) {
                destinationFolder = dialog.getSelectedFolder();
                //destinationButton.setText(destinationFolder.getTreePath());
                destinationButton.setText(destinationFolder.getName());
                updateCanFinish();
            }
        }
    }

    protected void updateCanFinish() {
        setCanFinish((sourceFile != null) && (destinationFolder != null));
    }

    public void prepareRendering() {
    }
}
