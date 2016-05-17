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

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.common.constants.*;
import org.compiere.framework.*;
import org.compiere.util.*;

/**
 *	Product Attribute Lookup Model (not Cached)
 *	
 *  @author Jorg Janke
 *  @version $Id: MPAttributeLookup.java,v 1.2 2006/07/30 00:58:38 jjanke Exp $
 */
public class MPAttributeLookup extends Lookup
	implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Constructor
	 * 	@param ctx context
	 *	@param WindowNo window no
	 */
	public MPAttributeLookup(Ctx ctx, int WindowNo)
	{
		super (ctx, WindowNo, DisplayTypeConstants.TableDir);
	}	//	MPAttribute

	/**	No Instance Value			*/
	private static KeyNamePair	NO_INSTANCE = new KeyNamePair (0,"");

	/**
	 *	Get Display for Value (not cached)
	 *  @param value Location_ID
	 *  @return String Value
	 */
	@Override
	public String getDisplay (Object value)
	{
		if (value == null)
			return "";
		NamePair pp = get (value);
		if (pp == null)
			return "<" + value.toString() + ">";
		return pp.getName();
	}	//	getDisplay

	/**
	 *  The Lookup contains the key (not cached)
	 *  @param key Location_ID
	 *  @return true if key known
	 */
	@Override
	public boolean containsKey (Object key)
	{
		return get(key) != null;
	}   //  containsKey

	/**
	 *	Get Object of Key Value
	 *  @param value value
	 *  @return Object or null
	 */
	@Override
	public NamePair get (Object value)
	{
		if (value == null)
			return null;
		int M_AttributeSetInstance_ID = 0;
		if (value instanceof Integer)
			M_AttributeSetInstance_ID = ((Integer)value).intValue();
		else
		{
			try
			{
				M_AttributeSetInstance_ID = Integer.parseInt(value.toString());
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "Value=" + value, e);
			}
		}
		if (M_AttributeSetInstance_ID == 0)
			return NO_INSTANCE;
		//
		//	Statement
		PreparedStatement	pstmt = DB.prepareStatement("SELECT Description "
				+ "FROM M_AttributeSetInstance "
				+ "WHERE M_AttributeSetInstance_ID=?", (Trx) null);
		//
		String Description = null;
		try
		{
			pstmt.setInt(1, M_AttributeSetInstance_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				Description = rs.getString(1);			//	Description
				if (Description == null || Description.length() == 0)
				{
					if (CLogMgt.isLevelFine())
						Description = "{" + M_AttributeSetInstance_ID + "}";
					else
						Description = "";
				}
			}
			pstmt.close();
			rs.close();
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "get", e);
		}
		if (Description == null)
			return null;
		return new KeyNamePair (M_AttributeSetInstance_ID, Description);
	}	//	get

	/**
	 * 	Dispose
	 *	@see org.compiere.model.Lookup#dispose()
	 *
	@Override
	public void dispose()
	{
		try
		{
			if (m_pstmt != null)
				m_pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, "dispose", e);
		}
		log.fine("");
		super.dispose();
	}	//	dispose
*/
	/**
	 *	Return data as sorted Array - not implemented
	 *  @param mandatory mandatory
	 *  @param onlyValidated only validated
	 *  @param onlyActive only active
	 * 	@param temporary force load for temporary display
	 *  @return null
	 */
	@Override
	public ArrayList<NamePair> getData (boolean mandatory, boolean onlyValidated, boolean onlyActive, boolean temporary)
	{
		
		ArrayList<NamePair> list = new ArrayList<NamePair>();
		if (!mandatory)
			list.add(new KeyNamePair (-1, ""));
		//
		StringBuffer sql = new StringBuffer(
				"SELECT ASI.M_AttributeSetInstance_ID, ASI.Description from M_AttributeSetInstance ASI, M_Product P WHERE ASI.M_AttributeSet_ID = P.M_AttributeSet_ID AND P.M_Product_ID = ?" );
		if (onlyActive)
			sql.append(" AND ASI.IsActive='Y'");
		sql.append(" ORDER BY 2");
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), (Trx) null);
			pstmt.setInt(1, getCtx().getContextAsInt( m_WindowNo, "M_Product_ID" ));
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int key = rs.getInt(1);
				String desc = rs.getString(2);
				if( desc == null )
					desc = "{" + key + "}";
				list.add (new KeyNamePair(key, desc));
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}

		//  Sort & return
		return list;
	}   //  getArray

	/**
	 *	Get underlying fully qualified Table.Column Name.
	 *	Used for VLookup.actionButton (Zoom)
	 *  @return column name
	 */
	@Override
	public String getColumnName()
	{
		return "M_AttributeSetInstance_ID";
	}	//	getColumnName

}	//	MPAttribute
