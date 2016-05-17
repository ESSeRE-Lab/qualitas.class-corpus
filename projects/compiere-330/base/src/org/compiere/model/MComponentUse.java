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
 * 	Component Registration Use
 *	@author Jorg Janke
 */
public class MComponentUse extends X_AD_ComponentUse
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_ComponentUse_ID id
	 *	@param trx p_trx
	 */
	public MComponentUse(Ctx ctx, int AD_ComponentUse_ID, Trx trx)
	{
		super(ctx, AD_ComponentUse_ID, trx);
		if (AD_ComponentUse_ID == 0)
		{
		//	setAD_ComponentReg_ID (0);
		//	setEMail (null);
		}
	}	//	MComponentUse

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx p_trx
	 */
	public MComponentUse(Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MComponentUse

	/**
	 * 	Parent Constructor
	 *	@param parent registration
	 */
	public MComponentUse(MComponentReg parent)
	{
		this (parent.getCtx(), 0, parent.get_Trx());
		setClientOrg(parent);
		setAD_ComponentReg_ID(parent.getAD_ComponentReg_ID());
	}	//	MComponentUse
	
}	//	MComponentUse
