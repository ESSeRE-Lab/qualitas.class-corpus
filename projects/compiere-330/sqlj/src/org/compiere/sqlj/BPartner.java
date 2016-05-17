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
package org.compiere.sqlj;

import java.sql.*;


/**
 *	SQLJ Business Partner related Functions
 *	
 *  @author Jorg Janke
 *  @version $Id: BPartner.java,v 1.3 2006/07/30 00:59:07 jjanke Exp $
 */
public class BPartner
{
	/**
	 * 	Return first Remit Location of BPartner.
	 * 	C_BPartner_RemitLocation - bpartnerRemitLocation
	 *	@param p_C_BPartner_ID business partner
	 *	@return remit to location
	 *	@throws SQLException
	 */
	public static int remitLocation (int p_C_BPartner_ID)
		throws SQLException
	{
		int C_Location_ID = 0;
		String sql = "SELECT IsRemitTo, C_Location_ID "
			+ "FROM C_BPartner_Location "
			+ "WHERE C_BPartner_ID=? "
			+ "ORDER BY IsRemitTo DESC";
		PreparedStatement pstmt = Compiere.prepareStatement(sql);
		pstmt.setInt(1, p_C_BPartner_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
			C_Location_ID = rs.getInt(2);
		rs.close();
		pstmt.close();
		//
		return C_Location_ID;
	}	//	remitLocation
	
}	//	BPartner
