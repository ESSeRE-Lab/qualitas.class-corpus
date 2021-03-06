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
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import org.compiere.model.*;
import org.compiere.plaf.*;
import org.compiere.util.*;

/**
 *  Cell Editor.
 *  <pre>
 *		Sequence of events:
 *			isCellEditable
 *			getTableCellEditor
 *			shouldSelectCell
 *			getCellEditorValue
 *  </pre>
 *  A new Editor is created for editable columns.
 *  @author 	Jorg Janke
 *  @version 	$Id: VCellEditor.java,v 1.2 2006/07/30 00:51:28 jjanke Exp $
 */
public final class VCellEditor extends AbstractCellEditor
	implements TableCellEditor, VetoableChangeListener, ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *	Constructor for Grid
	 *  @param mField
	 */
	public VCellEditor (GridField mField)
	{
		super();
		m_mField = mField;
		//  Click
	}	//	VCellEditor

	/** The Field               */
	private GridField		m_mField = null;
	/** The Table Editor        */
	private VEditor	        m_editor = null;
	/** Table                   */
	private JTable          m_table = null;
	/** ClickCount              */
	private static int      CLICK_TO_START = 1;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(VCellEditor.class);

	/**
	 *  Create Editor
	 */
	private void createEditor()
	{
		m_editor = VEditorFactory.getEditor(m_mField, true);
		m_editor.addVetoableChangeListener(this);
		m_editor.addActionListener(this);
	}   //  createEditor

	/**
	 *	Ask the editor if it can start editing using anEvent.
	 *	If editing can be started this method returns true.
	 *	Previously called: MTable.isCellEditable
	 *  @param anEvent event
	 *  @return true if editable
	 */
	@Override
	public boolean isCellEditable (EventObject anEvent)
	{
		if (!m_mField.isEditable (true))	//	row data is in context
			return false;
		log.fine(m_mField.getHeader());		//	event may be null if CellEdit
		//	not enough mouse clicks
		if (anEvent instanceof MouseEvent 
			&& ((MouseEvent)anEvent).getClickCount() < CLICK_TO_START)
			return false;
			
		if (m_editor == null)
			createEditor();
		return true;
	}	//	isCellEditable

	/**
	 *	Sets an initial value for the editor. This will cause the editor to
	 *	stopEditing and lose any partially edited value if the editor is editing
	 *	when this method is called.
	 *	Returns the component that should be added to the client's Component hierarchy.
	 *	Once installed in the client's hierarchy this component
	 *	will then be able to draw and receive user input.
	 *
	 *  @param table
	 *  @param value
	 *  @param isSelected
	 *  @param row
	 *  @param col
	 *  @return component
	 */
	public Component getTableCellEditorComponent (JTable table, Object value, boolean isSelected, int row, int col)
	{
		log.finest(m_mField.getColumnName() + ": Value=" + value + ", row=" + row + ", col=" + col);
		if (row >= 0)
			table.setRowSelectionInterval(row,row);     //  force moving to new row
		if (m_editor == null)
			createEditor();

		m_table = table;

		//	Set Value
		m_editor.setValue(value);

		//	Set Background/Foreground to "normal" (unselected) colors
		m_editor.setBackground(m_mField.isError());
		m_editor.setForeground(CompierePLAF.getTextColor_Normal());

		//  Other UI
		m_editor.setFont(table.getFont());
		m_editor.setBorder(null);
	//	m_editor.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		//
		return (Component)m_editor;
	}	//	getTableCellEditorComponent

	/**
	 *	The editing cell should be selected or not
	 *  @param e event
	 *  @return true (constant)
	 */
	@Override
	public boolean shouldSelectCell(EventObject e)
	{
		log.finest(m_mField.getColumnName());
		return true;
	}	//	shouldSelectCell

	/**
	 *	Returns the value contained in the editor
	 *  @return value
	 */
	public Object getCellEditorValue()
	{
		log.finest(m_mField.getColumnName() + ": " + m_editor.getValue());
		return m_editor.getValue();
	}	//	getCellEditorValue

	/**
	 *  VEditor Change Listener (property name is columnName).
	 *  - indicate change  (for String/Text/..) <br>
	 *  When editing is complete the value is retrieved via getCellEditorValue
	 *  @param e event
	 */
	public void vetoableChange(PropertyChangeEvent e)
	{
		if (m_table == null)
			return;
		log.fine(e.getPropertyName() + "=" + e.getNewValue());
	}   //  vetoableChange

	/**
	 *  Get Actual Editor.
	 *  Called from GridController to add ActionListener to Button
	 *  @return VEditor
	 */
	public VEditor getEditor()
	{
		return m_editor;
	}   //  getEditor

	/**
	 *  Action Editor - Stop Editor
	 *  @param e event
	 */
	public void actionPerformed (ActionEvent e)
	{
		log.finer(m_mField.getColumnName() + ": Value=" + m_editor.getValue());
//		super.stopCellEditing();	//	causes VLookup.Search Text not to work
	}   //  actionPerformed

	/**
	 * 	Dispose
	 */
	public void dispose()
	{
		m_editor = null;
		m_mField = null;
		m_table = null;
	}	//	dispose

}	//	VCellEditor
