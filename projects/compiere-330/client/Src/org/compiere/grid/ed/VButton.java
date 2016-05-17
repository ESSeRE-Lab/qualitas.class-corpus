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
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.common.constants.*;
import org.compiere.model.*;
import org.compiere.swing.*;
import org.compiere.util.*;

/**
 *  General Button.
 *  <pre>
 *  Special Buttons:
 *      Payment,
 *      Processing,
 *      CreateFrom,
 *      Record_ID       - Zoom
 *  </pre>
 *  Maintains all values for display in m_values.
 *  see org.compiere.apps.APanel#actionButton(VButton)
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: VButton.java,v 1.2 2006/07/30 00:51:28 jjanke Exp $
 */
public final class VButton extends CButton
	implements VEditor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *	Constructor
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param text text
	 *  @param description description
	 *  @param help help
	 *  @param AD_Process_ID process to start
	 */
	public VButton (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		String text, String description, String help, int AD_Process_ID)
	{
		super (text);
		super.setName(columnName);
		super.setActionCommand(columnName);
		m_text = text;
		m_columnName = columnName;
		//
		setMandatory (mandatory);
		if (isReadOnly || !isUpdateable)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Special Buttons
		if (columnName.equals("PaymentRule"))
		{
			readReference(195);
			this.setForeground(Color.blue);
			setIcon(Env.getImageIcon("Payment16.gif"));    //  29*14
		}
		else if (columnName.equals("DocAction"))
		{
			readReference(135);
			this.setForeground(Color.blue);
			setIcon(Env.getImageIcon("Process16.gif"));    //  16*16
		}
		else if (columnName.equals("CreateFrom"))
		{
			setIcon(Env.getImageIcon("Copy16.gif"));       //  16*16
		}
		else if (columnName.equals("Record_ID"))
		{
			setIcon(Env.getImageIcon("Zoom16.gif"));       //  16*16
			this.setText(Msg.getMsg(Env.getCtx(), "ZoomDocument"));
		}
		else if (columnName.equals("Posted"))
		{
			readReference(234);
			this.setForeground(Color.magenta);
			setIcon(Env.getImageIcon("InfoAccount16.gif"));    //  16*16
		}

		//	Deescription & Help
		m_description = description;
		if (description == null || description.length() == 0)
			m_description = " ";
		else
			setToolTipText(m_description);
		//
		m_help = help;
		if (help == null)
			m_help = "";
		m_AD_Process_ID = AD_Process_ID;
	}	//	VButton

	/** Mnemonic saved			*/
	private char	m_savedMnemonic = 0;

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		if (m_values != null)
			m_values.clear();
		m_values = null;
		m_field = null;
	}   //  dispose

	private String			m_columnName;
	private String			m_text;
	private boolean			m_mandatory;
	private Object			m_value;
	/** List of Key/Name        */
	private HashMap<String,String>	m_values = null;
	/** Description as ToolTip  */
	private	String			m_description = "";
	private String			m_help;
	private int				m_AD_Process_ID;
	private MLookup			m_lookup;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(VButton.class);

	/**
	 *	Set Value
	 *  @param value value
	 */
	@Override
	public void setValue(Object value)
	{
		m_value = value;
		String text = m_text;

		//	Nothing to show or Record_ID
		if (value == null || m_columnName.equals("Record_ID"))
			;
		else if (m_values != null)
			text = m_values.get(value);
		else if (m_lookup != null)
		{
			NamePair pp = m_lookup.get (value);
			if (pp != null)
				text = pp.getName();
		}
		//	Display it
		setText (text != null ? text : "");
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt event
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(org.compiere.model.GridField.PROPERTY))
			setValue(evt.getNewValue());
	}   //  propertyChange

	/**
	 *	Return Value
	 *  @return value
	 */
	@Override
	public Object getValue()
	{
		return m_value;
	}	//	getValue

	/**
	 *  Return Display Value
	 *  @return String value
	 */
	@Override
	public String getDisplay()
	{
		return m_value.toString();
	}   //  getDisplay

	/**
	 *	Set Mandatory	- NOP
	 *  @param mandatory mandatory
	 */
	@Override
	public void setMandatory (boolean mandatory)
	{
		m_mandatory = mandatory;
	}	//	setMandatory

	/**
	 *	Mandatory?
	 *  @return true if mandatory
	 */
	@Override
	public boolean isMandatory()
	{
		return m_mandatory;
	}	//	isMandatory

	/**
	 *	Set Background - NOP
	 *  @param error error
	 */
	@Override
	public void setBackground(boolean error)
	{
	}	//	setBackground

	/**
	 *	Get ColumnName
	 *  @return column name
	 */
	public String getColumnName()
	{
		return m_columnName;
	}	//	getColumnName

	/**
	 *	Get Description
	 *  @return description string
	 */
	public String getDescription()
	{
		return m_description;
	}	//	getDescription

	/**
	 *	Get Help
	 *  @return help string
	 */
	public String getHelp()
	{
		return m_help;
	}	//	getHelp

	/**
	 *	Get AD_Process_ID
	 *  @return AD_Process_ID or 0
	 */
	public int getProcess_ID()
	{
		return m_AD_Process_ID;
	}	//	getProcess_ID

	/**
	 *	Add ActionListener
	 *  @param aListener listener
	 */
	@Override
	public void addActionListener(ActionListener aListener)
	{
		super.addActionListener(aListener);
	}	//	addActionListener

	/**
	 *	String representation
	 *  @return String representation
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer ("VButton[");
		sb.append(m_columnName);
		sb.append("=").append(m_value).append("]");
		return sb.toString();
	}	//	toString

	/**
	 *	Fill m_Values with Ref_List values
	 *  @param AD_Reference_ID reference
	 */
	private void readReference( int AD_Reference_ID)
	{
		m_values = new HashMap<String,String>();
		String SQL;
		if (Env.isBaseLanguage(Env.getCtx(), "AD_Ref_List"))
			SQL = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=?"
				+ " AND IsActive='Y'";
		else
			SQL = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Ref_List_ID=t.AD_Ref_List_ID"
				+ " AND t.AD_Language='" + Env.getAD_Language(Env.getCtx()) + "'"
				+ " AND l.AD_Reference_ID=?"
				+ " AND l.IsActive='Y'";

		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL, (Trx) null);
			pstmt.setInt(1, AD_Reference_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				String value = rs.getString(1);
				String name = rs.getString(2);
				m_values.put(value, name);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, SQL, e);
		}
	}	//	readReference

	/**
	 *	Return value/name
	 *  @return HashMap with Value/Names
	 */
	public HashMap<String, String> getValues()
	{
		return m_values;
	}	//	getValues

	/**
	 *  Set Field/WindowNo
	 *  @param mField field model
	 */
	public void setField (GridField mField)
	{
		m_field = mField;
		if (mField.getColumnName().endsWith("_ID") && !mField.getColumnName().equals("Record_ID"))
		{
			m_lookup = MLookupFactory.get(Env.getCtx(), mField.getWindowNo(), 
				mField.getAD_Column_ID(), DisplayTypeConstants.Search);
		}
		else if (mField.getAD_Reference_Value_ID() != 0)
		{
			//	Assuming List
			m_lookup = MLookupFactory.get(Env.getCtx(), mField.getWindowNo(), 
				mField.getAD_Column_ID(), DisplayTypeConstants.List);
		}
	}   //  setField
	
	/** Grid Field				*/
	private GridField 	m_field = null;
	
	/**
	 *  Get Field
	 *  @return gridField
	 */
	public GridField getField()
	{
		return m_field;
	}   //  getField

	/**
	 * @return Returns the savedMnemonic.
	 */
	public char getSavedMnemonic ()
	{
		return m_savedMnemonic;
	}	//	getSavedMnemonic
	
	/**
	 * @param savedMnemonic The savedMnemonic to set.
	 */
	public void setSavedMnemonic (char savedMnemonic)
	{
		m_savedMnemonic = savedMnemonic;
	}	//	getSavedMnemonic
	
	/**
	 * 	Get Focus Component
	 *	@return component
	 */
	public Component getFocusableComponent()
	{
		return this;
	}	//	getFocusableComponent

}	//	VButton
