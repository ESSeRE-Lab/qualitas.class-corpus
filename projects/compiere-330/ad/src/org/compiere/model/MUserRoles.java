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
import java.util.*;
import java.util.logging.*;

import org.compiere.util.*;

/**
 *	User Roles Model
 *
 *  @author Jorg Janke
 *  @version $Id: MUserRoles.java,v 1.3 2006/07/30 00:58:37 jjanke Exp $
 */
public class MUserRoles extends X_AD_User_Roles
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 	Get User Roles Of Role
	 *	@param ctx context
	 *	@param AD_Role_ID role
	 *	@return array of user roles
	 */
	public static MUserRoles[] getOfRole (Ctx ctx, int AD_Role_ID)
	{
		String sql = "SELECT * FROM AD_User_Roles WHERE AD_Role_ID=?";
		ArrayList<MUserRoles> list = new ArrayList<MUserRoles>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MUserRoles (ctx, rs, null));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		MUserRoles[] retValue = new MUserRoles[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getOfRole

	/**
	 * 	Get User Roles Of User
	 *	@param ctx context
	 *	@param AD_User_ID role
	 *	@return array of user roles
	 */
	public static MUserRoles[] getOfUser (Ctx ctx, int AD_User_ID)
	{
		String sql = "SELECT * FROM AD_User_Roles WHERE AD_User_ID=?";
		ArrayList<MUserRoles> list = new ArrayList<MUserRoles>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MUserRoles (ctx, rs, null));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		MUserRoles[] retValue = new MUserRoles[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getOfUser

	/**	Static Logger	*/
	private static CLogger	s_log	= CLogger.getCLogger (MUserRoles.class);


	/**************************************************************************
	 * 	Persistence Constructor
	 *	@param ctx context
	 *	@param ignored invalid
	 *	@param trx transaction
	 */
	public MUserRoles (Ctx ctx, int ignored, Trx trx)
	{
		super (ctx, ignored, trx);
		if (ignored != 0)
			throw new IllegalArgumentException("Multi-Key");
	}	//	MUserRoles

	/**
	 * 	Load constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MUserRoles (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MUserRoles

	/**
	 * 	Full Constructor
	 *	@param ctx context
	 *	@param AD_User_ID user
	 *	@param AD_Role_ID role
	 *	@param trx transaction
	 */
	public MUserRoles (Ctx ctx, int AD_User_ID, int AD_Role_ID, Trx trx)
	{
		this (ctx, 0, trx);
		setAD_User_ID(AD_User_ID);
		setAD_Role_ID(AD_Role_ID);
	}	//	MUserRoles

	/**
	 * 	Set User/Contact.
	 *	User within the system - Internal or Business Partner Contact
	 *	@param AD_User_ID user
	 */
	@Override
	public void setAD_User_ID (int AD_User_ID)
	{
		set_ValueNoCheck ("AD_User_ID", Integer.valueOf(AD_User_ID));
	}	//	setAD_User_ID

	/**
	 * 	Set Role.
	 * 	Responsibility Role
	 * 	@param AD_Role_ID role
	 **/
	@Override
	public void setAD_Role_ID (int AD_Role_ID)
	{
		set_ValueNoCheck ("AD_Role_ID", Integer.valueOf(AD_Role_ID));
	}	//	setAD_Role_ID

	/**
     * 	String Representation
     *	@return info
     */
    @Override
    public String toString()
    {
	    StringBuffer sb = new StringBuffer("MUserRoles[")
	    	.append("AD_User_ID=").append(getAD_User_ID())
	        .append(",AD_Role_ID=").append(getAD_Role_ID());
	    sb.append("]");
	    return sb.toString();
    }	//	toString

}	//	MUserRoles
