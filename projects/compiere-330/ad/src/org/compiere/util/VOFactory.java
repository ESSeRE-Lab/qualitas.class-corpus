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
package org.compiere.util;

import java.sql.*;
import java.util.*;
import java.util.logging.*;


/**
 *	Value Object Factory base class
 *	
 *  @author Jorg Janke
 *  @version $Id$
 */
public abstract class VOFactory<VO>
{
	/**	Logger	*/
	protected CLogger log = CLogger.getCLogger (getClass());
	
	/**
	 * 	Get VO 
	 *	@param sql single row sql command
	 *	@param id key parameter
	 *	@return VO
	 */
	protected VO get (String sql, int id)
	{
		VO retValue = null;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, id);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next())
				retValue = load(rs);
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log (Level.SEVERE, sql, e);
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
		return retValue;
	}	//	get

	/**
	 * 	Get VO 
	 *	@param sql single row sql command
	 *	@param id1 query parameter
	 *	@param id2 query parameter
	 *	@return VO
	 */
	protected VO get (String sql, int id1, int id2)
	{
		VO retValue = null;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, id1);
			pstmt.setInt (2, id2);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next())
				retValue = load(rs);
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log (Level.SEVERE, sql, e);
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
		return retValue;
	}	//	get

	/**
	 * 	Get all VOs 
	 *	@param sql sql command
	 *	@param id key parameter
	 *	@return Array of VO
	 */
	public ArrayList<VO> getAll (String sql, int id)
	{
		ArrayList<VO> list = new ArrayList<VO>(); 
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, id);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next())
				list.add (load(rs));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log (Level.SEVERE, sql, e);
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
		log.fine("#" + list.size());
		return list;
	}	//	getAll

	/** 
	 * 	Load from ResultSet
	 * 	@param rs result set 
	 */
	abstract protected VO load (ResultSet rs);

}	//	VOFactory
