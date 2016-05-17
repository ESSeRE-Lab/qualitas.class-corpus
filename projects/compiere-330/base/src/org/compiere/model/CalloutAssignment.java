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
package org.compiere.model;

import java.math.*;
import java.sql.*;
import java.util.logging.*;

import org.compiere.util.*;


/**
 *	Resource Assignment Callout
 *
 *  @author Jorg Janke
 *  @version $Id: CalloutAssignment.java,v 1.3 2006/07/30 00:51:03 jjanke Exp $
 */
public class CalloutAssignment extends CalloutEngine
{

	/** Logger					*/
	private CLogger		log = CLogger.getCLogger(getClass());

	/**
	 *	Assignment_Product.
	 *		- called from S_ResourceAssignment_ID
	 *		- sets M_Product_ID, Description
	 *			- Qty..
	 *	@param ctx context
	 *	@param WindowNo window no
	 *	@param mTab tab
	 *	@param mField field
	 *	@param value value
	 *	@return null or error message
	 */
	public String product (Ctx ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		if (isCalloutActive() || value == null)
			return "";
		//	get value
		int S_ResourceAssignment_ID = ((Integer)value).intValue();
		if (S_ResourceAssignment_ID == 0)
			return "";
		setCalloutActive(true);

		int M_Product_ID = 0;
		String Name = null;
		String Description = null;
		BigDecimal Qty = null;
		String sql = "SELECT p.M_Product_ID, ra.Name, ra.Description, ra.Qty "
			+ "FROM S_ResourceAssignment ra"
			+ " INNER JOIN M_Product p ON (p.S_Resource_ID=ra.S_Resource_ID) "
			+ "WHERE ra.S_ResourceAssignment_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt(1, S_ResourceAssignment_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				M_Product_ID = rs.getInt (1);
				Name = rs.getString(2);
				Description = rs.getString(3);
				Qty = rs.getBigDecimal(4);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, "product", e);
		}

		log.fine("S_ResourceAssignment_ID=" + S_ResourceAssignment_ID + " - M_Product_ID=" + M_Product_ID);
		if (M_Product_ID != 0)
		{
			mTab.setValue ("M_Product_ID", Integer.valueOf (M_Product_ID));
			if (Description != null)
				Name += " (" + Description + ")";
			if (!".".equals(Name))
				mTab.setValue("Description", Name);
			//
			String variable = "Qty";	//	TimeExpenseLine
			if (mTab.getTableName().startsWith("C_Order"))
				variable = "QtyOrdered";
			else if (mTab.getTableName().startsWith("C_Invoice"))
				variable = "QtyInvoiced";
			if (Qty != null)
				mTab.setValue(variable, Qty);
		}
		setCalloutActive(false);
		return "";
	}	//	product

}	//	CalloutAssignment
