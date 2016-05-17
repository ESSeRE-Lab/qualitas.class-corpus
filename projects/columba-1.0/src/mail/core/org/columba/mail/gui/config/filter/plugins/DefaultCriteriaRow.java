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
package org.columba.mail.gui.config.filter.plugins;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.columba.api.plugin.IExtensionInterface;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.gui.base.ComboMenu;
import org.columba.mail.gui.config.filter.CriteriaList;
import org.columba.mail.plugin.FilterExtensionHandler;

public class DefaultCriteriaRow implements IExtensionInterface {

	protected FilterCriteria criteria;

	protected CriteriaList criteriaList;

	protected JPanel panel;

	protected JButton removeButton;

	protected GridBagLayout gridbag = new GridBagLayout();

	protected GridBagConstraints c = new GridBagConstraints();

	FilterExtensionHandler pluginHandler;

	protected int count;

	private ComboMenu comboMenu;

	public DefaultCriteriaRow(FilterExtensionHandler pluginHandler,
			CriteriaList criteriaList, FilterCriteria c) {
		this.pluginHandler = pluginHandler;

		this.criteria = c;
		this.criteriaList = criteriaList;

		panel = new JPanel();

		initComponents();

		updateComponents(true);

	}

	public void updateComponents(boolean b) {
		if (b) {
			String conditionString = criteria.getTypeString();

			comboMenu.setText(conditionString);

		} else {

			String conditionString = comboMenu.getText();
			criteria.setTypeString(conditionString);

		}
	}

	public void initComponents() {
		panel.setLayout(gridbag);

		String[] list = pluginHandler.getPluginIdList();
		comboMenu = new ComboMenu(list);
		comboMenu.setText(criteria.getTypeString());
		comboMenu.addItemListener(criteriaList);

		c.fill = GridBagConstraints.VERTICAL;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;

		gridbag.setConstraints(comboMenu, c);

		panel.add(comboMenu);

		count = 0;
	}

	public JPanel getContentPane() {
		return panel;
	}

	public void addComponent(JComponent component) {
		c.gridx = ++count;
		gridbag.setConstraints(component, c);
		panel.add(component);
	}

	/**
	 * Returns the criteria.
	 * 
	 * @return FilterCriteria
	 */
	public FilterCriteria getCriteria() {
		return criteria;
	}

	/**
	 * Returns the pluginHandler.
	 * 
	 * @return AbstractPluginHandler
	 */
	public FilterExtensionHandler getPluginHandler() {
		return pluginHandler;
	}

	/**
	 * Sets the pluginHandler.
	 * 
	 * @param pluginHandler
	 *            The pluginHandler to set
	 */
	public void setPluginHandler(FilterExtensionHandler pluginHandler) {
		this.pluginHandler = pluginHandler;
	}

}