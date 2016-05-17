/******************************************************************************
 * Product: Compiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 3600 Bridge Parkway #102, Redwood City, CA 94065, USA      *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.grid.ed;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 *	Renderer for RowID Column
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: VRowIDRenderer.java,v 1.2 2006/07/30 00:51:28 jjanke Exp $
 */
public final class VRowIDRenderer implements TableCellRenderer
{
	/**
	 *	Constructor
	 */
	public VRowIDRenderer(boolean enableSelection)
	{
		m_select = enableSelection;
	}	//	VRowIDRenderer

	private boolean		m_select = false;
	private JButton 	m_button = new JButton();
	private JCheckBox	m_check = null;

	/**
	 *	Enable Selection to be displayed
	 */
	public void setEnableSelection(boolean showSelection)
	{
		m_select = showSelection;
	}	//	setEnableSelection

	/**
	 *	Return TableCell Renderer Component
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (m_select)
		{
			if (m_check == null)
				m_check = new JCheckBox();
			Object[] data = (Object[])value;
			if (data == null || data[1] == null)
				m_check.setSelected(false);
			else
			{
				Boolean sel = (Boolean)data[1];
				m_check.setSelected(sel.booleanValue());
			}
			return m_check;
		}
		else
			return m_button;
	}	//	getTableCellRenderereComponent

}	//	VRowIDRenderer
