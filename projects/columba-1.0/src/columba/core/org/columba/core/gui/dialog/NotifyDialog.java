// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.core.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.columba.core.gui.base.MultiLineLabel;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.resourceloader.ImageLoader;

public class NotifyDialog extends JDialog {

	private MultiLineLabel textField;

	public NotifyDialog() {
		super(FrameManager.getInstance().getActiveFrame(), true);
	}

	public void showDialog(Exception ex) {
		showDialog(ex.getMessage());
	}

	public void showDialog(String message) {
		JLabel topLabel = new JLabel("An Error occured:", ImageLoader
				.getImageIcon("stock_dialog_error_48.png"), SwingConstants.LEFT);

		JButton[] buttons = new JButton[1];

		MultiLineLabel textArea = new MultiLineLabel(message);

		buttons[0] = new JButton("Close");
		buttons[0].setActionCommand("CLOSE");
		buttons[0].setDefaultCapable(true);

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		setTitle("Error occured...");
		getContentPane().setLayout(layout);
		getRootPane().setDefaultButton(buttons[0]);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 1;
		c.insets = new Insets(10, 10, 0, 20);
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(topLabel, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 1;
		c.insets = new Insets(10, 20, 10, 15);
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(textArea, c);

		JPanel panel = new JPanel();

		//panel.add( buttons[1] );
		panel.add(buttons[0]);

		c.gridx = 0;
		c.gridy = 5;
		c.weightx = 1.0;

		//c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridwidth = 1;
		c.insets = new Insets(5, 10, 10, 10);
		c.anchor = GridBagConstraints.SOUTHEAST;
		layout.setConstraints(panel, c);

		getContentPane().add(textArea);
		getContentPane().add(topLabel);
		getContentPane().add(panel);

		//dialog.getContentPane().add( buttons[1] );
		pack();
		setLocationRelativeTo(null);

		buttons[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String action = e.getActionCommand();

				if (action.equals("CLOSE")) {
					dispose();
				}
			}
		});

		show();
	}
}