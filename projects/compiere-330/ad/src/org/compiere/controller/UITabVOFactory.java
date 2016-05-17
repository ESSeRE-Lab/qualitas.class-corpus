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
package org.compiere.controller;

import java.util.*;

import org.compiere.util.*;


/**
 *	Tab VO Factory	
 *	
 *  @author Jorg Janke
 *  @version $Id$
 */
public class UITabVOFactory extends UITabVOFT
{
	/**
	 * 	Get all Tab VOs for window
	 * 	@param ctx context (to get Language)
	 *	@param AD_Window_ID window
	 *	@return Tab VOs
	 */
	public ArrayList<UITabVO> getAll (Ctx ctx, int AD_Window_ID, int AD_UserDef_Win_ID)
	{
		log.info("AD_Window_ID=" + AD_Window_ID);
		StringBuffer sql = new StringBuffer("SELECT * FROM AD_Tab_v WHERE AD_Window_ID=?");
		if (!Env.isBaseLanguage(ctx, "AD_Tab"))
			sql = new StringBuffer("SELECT * FROM AD_Tab_vt WHERE AD_Window_ID=? AND AD_Language='")
				.append(Env.getAD_Language(ctx)).append("'");
		if (AD_UserDef_Win_ID != 0)
			sql.append(" AND AD_UserDef_Win_ID=").append(AD_UserDef_Win_ID);
		sql.append(" ORDER BY SeqNo");
		return getAll(sql.toString(), AD_Window_ID);
	}	//	getAll

	/**
	 * 	Get Tab VO
	 * 	@param ctx context
	 *	@param AD_Tab_ID tab
	 *	@return Tab VO
	 */
	public UITabVO get (Ctx ctx, int AD_Tab_ID)
	{
		log.info("AD_Tab_ID=" + AD_Tab_ID);
		StringBuffer sql = new StringBuffer("SELECT * FROM AD_Tab_v WHERE AD_Tab_ID=?");
		if (!Env.isBaseLanguage(ctx, "AD_Tab"))
			sql = new StringBuffer("SELECT * FROM AD_Tab_vt WHERE AD_Tab_ID=? AND AD_Language='")
				.append(Env.getAD_Language(ctx)).append("'");
	//	sql.append(" AND AD_UserDef_Win_ID IS NULL");
		return get(sql.toString(), AD_Tab_ID);
	}	//	get
	
}	//	UITabVOFactory
