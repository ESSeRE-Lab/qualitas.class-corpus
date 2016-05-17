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
package org.compiere.framework;

import java.sql.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 * 	Generic PO Class
 *	@author Jorg Janke
 */
public class X extends PO
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Constructor
	 *	@param ctx context
	 *	@param AD_Table_ID table to load
	 *	@param id id of table
	 *	@param trx transaction
	 */
	public X (Ctx ctx, int AD_Table_ID, int id, Trx trx)
	{
		super();
		initPO (ctx, AD_Table_ID, null);
		init (ctx, id, null, trx);
	}	//	X

	/**
	 * 	Constructor
	 *	@param ctx context
	 *	@param tableName table to load
	 *	@param id id of table
	 *	@param trx transaction
	 */
	public X (Ctx ctx, String tableName, int id, Trx trx)
	{
		super();
		initPO (ctx, 0, tableName);
		init (ctx, id, null, trx);
	}	//	X

	/**
	 * 	Constructor
	 *	@param ctx context
	 *	@param AD_Table_ID table to load
	 *	@param rs result set of table
	 *	@param trx transaction
	 */
	public X (Ctx ctx, int AD_Table_ID, ResultSet rs, Trx trx)
	{
		super();
		initPO (ctx, AD_Table_ID, null);
		init (ctx, 0, rs, trx);
	}	//	X

	/**
	 * 	Constructor
	 *	@param ctx context
	 *	@param tableName table to load
	 *	@param rs result set of table
	 *	@param trx transaction
	 */
	public X (Ctx ctx, String tableName, ResultSet rs, Trx trx)
	{
		super();
		initPO (ctx, 0, tableName);
		init (ctx, 0, rs, trx);
	}	//	X

	/**
	 * 	Constructor
	 *	@param ctx context
	 *	@param table table to load
	 *	@param id id of table
	 *	@param trx transaction
	 */
	public X (Ctx ctx, MTable table, int id, Trx trx)
	{
		super();
		initPO (table);
		init (ctx, id, null, trx);
	}	//	X

	/**
	 * 	Constructor
	 *	@param ctx context
	 *	@param table table to load
	 *	@param rs result set of table
	 *	@param trx transaction
	 */
	public X (Ctx ctx, MTable table, ResultSet rs, Trx trx)
	{
		super();
		initPO (table);
		init (ctx, 0, rs, trx);
	}	//	X

	/**	AD_Table_ID				*/
	public int 		Table_ID = 0;
	/** Table Name				*/
	public String	Table_Name = null;
	/*	Model					*/
	protected KeyNamePair 	Model = null;

	/**
	 * 	Initialize
	 *	@param ctx ctx
	 *	@param AD_Table_ID table
	 *	@param tableName table name
	 */
	private void initPO (Ctx ctx, int AD_Table_ID, String tableName)
	{
		MTable table = null;
		if (AD_Table_ID > 0)
			table = MTable.get(ctx, AD_Table_ID);
		else
			table = MTable.get(ctx, tableName);
		if (table == null)
		{
			if (AD_Table_ID > 0)
				throw new IllegalArgumentException("Not found Table ID="
					+ AD_Table_ID);
			else
				throw new IllegalArgumentException("Not found Table "
					+ tableName);
		}
		initPO (table);
	}	//	initPO

	/**
	 * 	Initialize
	 *	@param ctx ctx
	 *	@param AD_Table_ID table
	 *	@param tableName table name
	 */
	private void initPO (MTable table)
	{
		Table_ID = table.getAD_Table_ID();
		Table_Name = table.getTableName();
		Model = new KeyNamePair(Table_ID,Table_Name);
	}	//	initPO

	/**
	 * 	Initialize PO
	 * 	@param ctx context
	 * 	@return POInfo
	 */
	@Override
	protected POInfo initPO(Ctx ctx)
	{
		POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
		return poi;
	}	//	initPO

	/**
     * 	String Representation
     *	@return info
     */
    @Override
	public String toString()
    {
	    StringBuffer sb = new StringBuffer("X[").append(get_ID())
	    	.append("-")
	        .append(Table_Name);
	    sb.append("]");
	    return sb.toString();
    }	//	toString

	@Override
	public int get_Table_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

}	//	X
