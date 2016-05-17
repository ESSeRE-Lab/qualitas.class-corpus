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
package org.columba.addressbook.gui.table.renderer;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import org.columba.addressbook.gui.table.model.SortDecorator;
import org.columba.core.gui.base.AscendingIcon;
import org.columba.core.gui.base.DescendingIcon;

/**
 * @author fdietz
 */
public class DefaultHeaderRenderer extends DefaultTableCellRenderer {

	private String name;

	private ImageIcon ascending = new AscendingIcon();

	private ImageIcon descending = new DescendingIcon();

	private SortDecorator sorter;

	public DefaultHeaderRenderer(SortDecorator sorter, String name) {
		super();

		this.name = name;
		this.sorter = sorter;

		setHorizontalAlignment(SwingConstants.LEFT);
		setHorizontalTextPosition(SwingConstants.LEFT);

		setOpaque(true); // MUST do this for background to show up.

		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	}

	public Component getTableCellRendererComponent(JTable table, Object str,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if (table != null) {
			JTableHeader header = table.getTableHeader();

			if (header != null) {
				setForeground(header.getForeground());
				setBackground(header.getBackground());
				setFont(header.getFont());
			}
		}

		if (sorter.getColumnName(sorter.getSelectedColumn()).equals(
				(String) str)) {
			if (sorter.isSortOrder()) {
				setIcon(descending);
			} else {
				setIcon(ascending);
			}
		} else {
			setIcon(null);
		}

		setText(this.name);

		return this;
	}
}