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
 *	POS Function Key Layout
 *	
 *  @author Jorg Janke
 *  @version $Id: MPOSKeyLayout.java,v 1.3 2006/07/30 00:51:05 jjanke Exp $
 */
public class MPOSKeyLayout extends X_C_POSKeyLayout
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Get POS Key Layout from Cache
	 *	@param ctx context
	 *	@param C_POSKeyLayout_ID id
	 *	@return MPOSKeyLayout
	 */
	public static MPOSKeyLayout get (Ctx ctx, int C_POSKeyLayout_ID)
	{
		Integer key = Integer.valueOf (C_POSKeyLayout_ID);
		MPOSKeyLayout retValue = s_cache.get (ctx, key);
		if (retValue != null)
			return retValue;
		retValue = new MPOSKeyLayout (ctx, C_POSKeyLayout_ID, null);
		if (retValue.get_ID () != 0)
			s_cache.put (key, retValue);
		return retValue;
	} //	get

	/**	Cache						*/
	private static final CCache<Integer,MPOSKeyLayout> s_cache = new CCache<Integer,MPOSKeyLayout>("C_POSKeyLayout", 3);

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param C_POSKeyLayout_ID id
	 *	@param trx transaction
	 */
	public MPOSKeyLayout (Ctx ctx, int C_POSKeyLayout_ID, Trx trx)
	{
		super (ctx, C_POSKeyLayout_ID, trx);
	}	//	MPOSKeyLayout

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MPOSKeyLayout (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MPOSKeyLayout

	/**	Keys				*/
	private MPOSKey[]	m_keys = null;
	
	/**
	 * 	Get Keys
	 *	@param requery requery
	 *	@return keys
	 */
	public MPOSKey[] getKeys (boolean requery)
	{
		if (m_keys != null && !requery)
			return m_keys;
		
		ArrayList<MPOSKey> list = new ArrayList<MPOSKey>();
		String sql = "SELECT * FROM C_POSKey WHERE C_POSKeyLayout_ID=? ORDER BY SeqNo";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_Trx());
			pstmt.setInt (1, getC_POSKeyLayout_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MPOSKey(getCtx(), rs, get_Trx()));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
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
		
		m_keys = new MPOSKey[list.size ()];
		list.toArray (m_keys);
		return m_keys;
	}	//	getKeys
	
	/**
	 * 	Get Number of Keys
	 *	@return keys
	 */
	public int getNoOfKeys()
	{
		return getKeys(false).length;
	}	//	getNoOfKeys
	
}	//	MPOSKeyLayout

