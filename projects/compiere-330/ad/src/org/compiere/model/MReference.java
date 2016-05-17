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
 * @author Jorg Janke
 *
 */
public class MReference extends X_AD_Reference
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 	Get Reference from Cache
	 *	@param ctx context
	 *	@param AD_Reference_ID id
	 *	@return MReference
	 */
	public static MReference get (Ctx ctx, int AD_Reference_ID)
	{
		Integer key = Integer.valueOf(AD_Reference_ID);
		MReference retValue = s_cache.get(ctx, key);
		if (retValue == null)
			return new MReference (ctx, AD_Reference_ID, null);
		if (retValue.get_ID () != 0)
			s_cache.put (key, retValue);
		return retValue;
	}	//	get
	
	/**	Cache						*/
	private static final CCache<Integer,MReference> s_cache = new CCache<Integer,MReference>("AD_Reference", 20);

	
	/**
	 * @param ctx
	 * @param AD_Reference_ID
	 * @param trx
	 */
	public MReference(Ctx ctx, int AD_Reference_ID, Trx trx)
	{
		super(ctx, AD_Reference_ID, trx);
	}

	/**
	 * @param ctx
	 * @param rs
	 * @param trx
	 */
	public MReference(Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}

	/**
	 * 	String Representation
	 * 	@return info
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MReference[")
			.append(get_ID()).append("-").append(getName()).append("]");
		return sb.toString();
	}	//	toString

}
