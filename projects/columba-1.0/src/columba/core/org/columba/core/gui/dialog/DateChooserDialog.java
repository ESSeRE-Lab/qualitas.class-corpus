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

package org.columba.core.gui.dialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.DateChooser;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.resourceloader.GlobalResourceLoader;

/**
 * @author fdietz
 */
public class DateChooserDialog extends JDialog implements ActionListener {
    protected DateChooser dateChooser;
    
    protected JButton okButton;
    protected JButton cancelButton;
    protected JPanel panel;
    
    protected boolean success = false;
    
    protected JDialog dialog;
    
    public DateChooserDialog() {
        super(FrameManager.getInstance().getActiveFrame(), true);
        
        //TODO: i18n
        setTitle("Choose Date...");
        
        dateChooser = new DateChooser();
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        getContentPane().add(panel, BorderLayout.CENTER);
        
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(dateChooser, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        
        /*
         * bottomPanel.setBorder(new WizardTopBorder()); Border border =
         * bottomPanel.getBorder(); Border margin =
         * BorderFactory.createEmptyBorder(15, 10, 10, 10);
         * bottomPanel.setBorder(new CompoundBorder(border, margin));
         */
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 10));
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        cancelButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
                "global", "global", "cancel"));
        cancelButton.setActionCommand("CANCEL");
        cancelButton.addActionListener(this);
        okButton = new JButton(GlobalResourceLoader.getString("global",
                "global", "ok"));
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        pack();
    }
    
    public Date getDate() {
        return dateChooser.getSelectedDate().getTime();
    }
    
    public void setDate(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        dateChooser.setSelectedDate(c);
    }
    
    public boolean success() {
        return success;
    }
    
    public void actionPerformed(ActionEvent ev) {
        String action = ev.getActionCommand();
        
        if (action.equals("OK")) {
            success = true;
            setVisible(false);
        } else if (action.equals("CANCEL")) {
            success = false;
            setVisible(false);
        }
    }
}
