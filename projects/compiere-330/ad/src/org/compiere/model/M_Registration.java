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

import java.sql.*;
import org.compiere.util.*;


/**
 *	System Registration Model
 *	
 *  @author Jorg Janke
 *  @version $Id: M_Registration.java,v 1.3 2006/07/30 00:58:18 jjanke Exp $
 */
public class M_Registration extends X_AD_Registration
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Return the latest valid Registration
	 *	@param ctx context
	 *	@return Registration
	 */
	public static M_Registration get (Ctx ctx)
	{
		M_Registration registration = null;
		
		String sql = "SELECT * FROM AD_Registration WHERE IsActive='Y' AND " +
					 "IsRegistered='Y' AND Record_ID!=0 ORDER BY AD_Registration_ID DESC";
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				registration = new M_Registration (ctx, rs, null);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			String info = "Cannot retrieve completed registration - " + ex.getLocalizedMessage();
			System.err.println(info);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
		}
		
		pstmt = null;
		
		return registration;
	}	//	get

	/**
	 * 	Default Constructor
	 *	@param ctx context
	 *	@param AD_Registration_ID id
	 *	@param trx transaction
	 */
	public M_Registration (Ctx ctx, int AD_Registration_ID, Trx trx)
	{
		super (ctx, AD_Registration_ID, trx);
		setAD_Client_ID(0);
		setAD_Org_ID(0);
	}	//	M_Registration

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public M_Registration (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	M_Registration

}	//	M_Registration
