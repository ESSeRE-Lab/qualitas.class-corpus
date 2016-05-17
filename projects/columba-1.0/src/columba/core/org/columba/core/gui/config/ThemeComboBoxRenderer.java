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

package org.columba.core.gui.config;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.columba.api.exception.PluginHandlerNotFoundException;
import org.columba.core.gui.dialog.NotifyDialog;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ThemeExtensionHandler;

/**
 * Renders UI themes.
 */
public class ThemeComboBoxRenderer extends DefaultListCellRenderer {
	protected ThemeExtensionHandler pluginHandler;

	public ThemeComboBoxRenderer() {
		super();

		try {
			pluginHandler = (ThemeExtensionHandler) PluginManager.getInstance()
					.getHandler(ThemeExtensionHandler.NAME);
		} catch (PluginHandlerNotFoundException ex) {
			NotifyDialog d = new NotifyDialog();
			d.showDialog(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
	 *      java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
				index, isSelected, cellHasFocus);
		// id = org.columba.example.HelloWorld$HelloWorldPlugin
		String id = (String) value;
		//String userVisibleName = pluginHandler.getUserVisibleName(id);
		String userVisibleName = id;
		label.setText(userVisibleName);
		return label;
	}
}
