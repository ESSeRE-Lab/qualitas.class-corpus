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

import javax.sql.*;
import javax.sql.rowset.*;

import org.compiere.db.*;


/**
 *	Compiere Cached Row Set Implementation
 *	
 *  @author Jorg Janke
 *  @version $Id: CCachedRowSet.java,v 1.6 2006/07/30 00:54:36 jjanke Exp $
 */
public class CCachedRowSet extends CachedRowSetImpl implements CachedRowSet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 	Get Cached Row Set.
	 * 	Required due to Java Sun bug 393865
	 *	@return Cached Row Set
	 *	@throws SQLException
	 */
	public static CCachedRowSet get() throws SQLException
	{
		CCachedRowSet crs = null;
		//	only first time call
		if (s_loc == null)
		{
			s_loc = Locale.getDefault();
			Locale.setDefault(Locale.US);
			crs = new CCachedRowSet();
			Locale.setDefault(s_loc);
		}
		else
			crs = new CCachedRowSet();
		//
		return crs;
	}	//	get
	
	/**
	 * 	Get Row Set.
	 * 	Read-Only, Scroll Insensitive
	 * 	Need to set parameters and call  execute(Commection)
	 *	@param sql sql
	 *	@return row set
	 *	@throws SQLException
	 */
	public static RowSet getRowSet (String sql) throws SQLException
	{
		CachedRowSet crs = get();
		crs.setConcurrency(ResultSet.CONCUR_READ_ONLY);
		crs.setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
		crs.setCommand(sql);
		//	Set Parameters
	//	crs.execute(conn);
		return crs;
	}	//	get

	/**
	 * 	Get and Execute Row Set.
	 * 	No parameters, Read-Only, Scroll Insensitive
	 *	@param sql sql
	 *	@param conn connection
	 *	@param db database
	 *	@return row set
	 *	@throws SQLException
	 */
	public static RowSet getRowSet (String sql, Connection conn, CompiereDatabase db) throws SQLException
	{
		/**
		if (db.getName().equals(Environment.DBTYPE_ORACLE))
		{
			Statement stmt = conn.createStatement
				(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(sql);
			OracleCachedRowSet crs = new OracleCachedRowSet();
			crs.populate(rs);
			stmt.close();
			return crs;
		}
		**/
		CachedRowSetImpl crs = get();
		crs.setConcurrency(ResultSet.CONCUR_READ_ONLY);
		crs.setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
		crs.setCommand(sql);
		crs.execute(conn);
		return crs;
	}	//	get
	
	/**
	 * 	Get Cached Row Set.
	 * 	Required due to Java Sun bug 393865. 
	 * 	Also, Oracle NUMBER returns scale -127 
	 * 	@param rs result set
	 * 	@param db database 
	 *	@return Cached Row Set
	 *	@throws SQLException
	 */
	public static RowSet getRowSet (ResultSet rs, CompiereDatabase db) throws SQLException
	{
		/**
		if (db.getName().equals(Environment.DBTYPE_ORACLE))
		{
			OracleCachedRowSet crs = new OracleCachedRowSet();
			crs.populate(rs);
			return crs;
		}
		**/
		CachedRowSetImpl crs = get();
		crs.populate(rs);
		return crs;
	}	//	getRowSet
	
	/**
	 * 	Get Cached Row Set.
	 * 	Gets Database from DB
	 * 	Required due to Java Sun bug 393865. 
	 * 	Also, Oracle NUMBER returns scale -127 
	 * 	@param rs result set
	 *	@return Cached Row Set
	 *	@throws SQLException
	 */
	public static RowSet getRowSet (ResultSet rs) throws SQLException
	{
		return getRowSet(rs, DB.getDatabase());
	}	//	getRowSet

	
	/**	Private Locale Marker	*/
	private static Locale s_loc = null;
	
	
	/**************************************************************************
	 * 	Compiere Cached RowSet
	 *	@throws java.sql.SQLException
	 */
	private CCachedRowSet() throws SQLException
	{
		super ();
		setSyncProvider("com.sun.rowset.providers.RIOptimisticProvider");
	}	//	CCachedRowSet

	
	
	
	
	/**************************************************************************
	 * 	Test
	 *	@param args ignored
	 */
	public static void main (String[] args)
	{
		try
		{
			Locale.setDefault(Locale.CANADA);
			get();
			System.out.println("OK 1");
			get();
			System.out.println("OK 1a");
			new CachedRowSetImpl();
			System.out.println("OK 2");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	//	main
	
	/**
	 *	To Collection
	 *	@return a <code>Collection</code> object that contains the values in 
	 * 		each row in this <code>CachedRowSet</code> object
	 *	@throws SQLException
	 */
	@Override
	public Collection<?> toCollection () throws SQLException
	{
		return super.toCollection ();
	}
	/**
	 *	To Collection
	 *	@param column an <code>int</code> indicating the column whose values
	 *	are to be represented in a <code>Collection</code> object
	 *	@return a <code>Collection</code> object that contains the values
	 *	stored in the specified column of this <code>CachedRowSet</code> object
	 *	@throws SQLException
	 */
	@Override
	public Collection<?> toCollection (int column) throws SQLException
	{
		return super.toCollection (column);
	}
	/**
	 *	To Collection
	 * @param column a <code>String</code> object giving the name of the 
	 *        column whose values are to be represented in a collection
	 * @return a <code>Collection</code> object that contains the values
	 * 		stored in the specified column of this <code>CachedRowSet</code> object
	 * @throws SQLException if an error occurs generating the collection or
	 * 	an invalid column id is provided
	 */
	@Override
	public Collection<?> toCollection (String column) throws SQLException
	{
		return super.toCollection (column);
	}
}	//	CCachedRowSet
