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

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import org.compiere.util.*;

/**
 *	Window Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MWindow.java,v 1.2 2006/07/30 00:58:05 jjanke Exp $
 */
public class MWindow extends X_AD_Window
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Get MWindow from Cache
	 *	@param ctx context
	 *	@param AD_Window_ID id
	 *	@return MWindow
	 */
	public static MWindow get(Ctx ctx, int AD_Window_ID)
	{
		Integer key = Integer.valueOf (AD_Window_ID);
		MWindow retValue = s_cache.get (ctx, key);
		if (retValue != null)
			return retValue;
		retValue = new MWindow (ctx, AD_Window_ID, null);
		if (retValue.get_ID () != 0)
			s_cache.put (key, retValue);
		return retValue;
	}	//	get

	
	/**
	 * 	Get workflow nodes with where clause.
	 * 	Is here as MWFNode is in base
	 *	@param ctx context
	 *	@param whereClause where clause w/o the actual WHERE
	 *	@return nodes
	 */
	public static X_AD_WF_Node[] getWFNodes (Ctx ctx, String whereClause)
	{
		String sql = "SELECT * FROM AD_WF_Node";
		if (whereClause != null && whereClause.length() > 0)
			sql += " WHERE " + whereClause;
		ArrayList<X_AD_WF_Node> list = new ArrayList<X_AD_WF_Node>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new X_AD_WF_Node (ctx, rs, null));
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
		X_AD_WF_Node[] retValue = new X_AD_WF_Node[list.size()];
		list.toArray (retValue);
		return retValue;
	}	//	getWFNode
	
	
	/**	Cache						*/
	private static final CCache<Integer, MWindow> s_cache 
		= new CCache<Integer, MWindow> ("AD_Window_ID", 20);
	/**	Static Logger	*/
	private static final CLogger	s_log	= CLogger.getCLogger (MWindow.class);
	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Window_ID
	 *	@param trx transaction
	 */
	public MWindow (Ctx ctx, int AD_Window_ID, Trx trx)
	{
		super (ctx, AD_Window_ID, trx);
		if (AD_Window_ID == 0)
		{
			setWindowType (WINDOWTYPE_Maintain);	// M
			setEntityType (ENTITYTYPE_UserMaintained);	// U
			setIsBetaFunctionality (false);
			setIsDefault (false);
			setIsCustomDefault(false);
		}	}	//	M_Window

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MWindow (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	M_Window
	
	/**
	 * 	Set Window Size
	 *	@param size size
	 */
	public void setWindowSize (Dimension size)
	{
		if (size != null)
		{
			setWinWidth(size.width);
			setWinHeight(size.height);
		}
		else
		{
			setWinWidth(0);
			setWinHeight(0);
		}
	}	//	setWindowSize
	
	/**	The Lines						*/
	private MTab[]		m_tabs	= null;

	/**
	 * 	Get Fields
	 *	@param reload reload data
	 *	@return array of lines
	 *	@param trx transaction
	 */
	public MTab[] getTabs (boolean reload, Trx trx)
	{
		if (m_tabs != null && !reload)
			return m_tabs;
		String sql = "SELECT * FROM AD_Tab WHERE AD_Window_ID=? ORDER BY SeqNo";
		ArrayList<MTab> list = new ArrayList<MTab>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, getAD_Window_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MTab (getCtx(), rs, trx));
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
		//
		m_tabs = new MTab[list.size ()];
		list.toArray (m_tabs);
		return m_tabs;
	}	//	getFields

	/**
	 * 	Get Tab with ID
	 * 	@param AD_Tab_ID id
	 *	@return tab or null
	 */
	public MTab getTab(int AD_Tab_ID)
	{
		MTab[] tabs = getTabs(false, get_Trx());
		for (MTab element : tabs) {
			if (element.getAD_Tab_ID() == AD_Tab_ID)
				return element;
        }
		return null;
	}	//	getTab
	
	/**
	 * 	After Save
	 *	@param newRecord new
	 *	@param success success
	 *	@return success
	 */
	@Override
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		if (newRecord)	//	Add to all automatic roles
		{
			MRole[] roles = MRole.getOf(getCtx(), "IsManual='N'");
			for (MRole element : roles) {
				MWindowAccess wa = new MWindowAccess(this, element.getAD_Role_ID());
				wa.save();
			}
		}
		//	Menu/Workflow
		else if (is_ValueChanged("IsActive") || is_ValueChanged("Name") 
			|| is_ValueChanged("Description") || is_ValueChanged("Help"))
		{
			MMenu[] menues = MMenu.get(getCtx(), "AD_Window_ID=" + getAD_Window_ID());
			for (MMenu element : menues) {
				element.setName(getName());
				element.setDescription(getDescription());
				element.setIsActive(isActive());
				element.save();
			}
			//
			X_AD_WF_Node[] nodes = getWFNodes(getCtx(), "AD_Window_ID=" + getAD_Window_ID());
			for (X_AD_WF_Node element : nodes) {
				boolean changed = false;
				if (element.isActive() != isActive())
				{
					element.setIsActive(isActive());
					changed = true;
				}
				if (element.isCentrallyMaintained())
				{
					element.setName(getName());
					element.setDescription(getDescription());
					element.setHelp(getHelp());
					changed = true;
				}
				if (changed)
					element.save();
			}
		}
		return success;
	}	//	afterSave
	
	/**
	 * 	String Representation
	 * 	@return info
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MWindow[")
			.append(get_ID()).append("-").append(getName()).append("]");
		return sb.toString();
	}	//	toString

}	//	MWindow
