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

import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.util.*;

/**
 * 	Inventory Storage Model
 *
 *	@author Jorg Janke
 *	@version $Id: MStorage.java,v 1.3 2006/07/30 00:51:05 jjanke Exp $
 */
public class MStorage extends X_M_Storage
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 	Get Storage Info
	 *	@param ctx context
	 *	@param M_Locator_ID locator
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID instance
	 *	@param trx transaction
	 *	@return existing or null
	 */
	public static MStorage get (Ctx ctx, int M_Locator_ID, 
		int M_Product_ID, int M_AttributeSetInstance_ID, Trx trx)
	{
		MStorage retValue = null;
		String sql = "SELECT * FROM M_Storage "
			+ "WHERE M_Locator_ID=? AND M_Product_ID=? AND ";
		if (M_AttributeSetInstance_ID == 0)
			sql += "(M_AttributeSetInstance_ID=? OR M_AttributeSetInstance_ID IS NULL)";
		else
			sql += "M_AttributeSetInstance_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_Locator_ID);
			pstmt.setInt (2, M_Product_ID);
			pstmt.setInt (3, M_AttributeSetInstance_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
				retValue = new MStorage (ctx, rs, trx);
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			s_log.log(Level.SEVERE, sql, ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		if (retValue == null)
			s_log.fine("Not Found - M_Locator_ID=" + M_Locator_ID 
				+ ", M_Product_ID=" + M_Product_ID + ", M_AttributeSetInstance_ID=" + M_AttributeSetInstance_ID);
		else
			s_log.fine("M_Locator_ID=" + M_Locator_ID 
				+ ", M_Product_ID=" + M_Product_ID + ", M_AttributeSetInstance_ID=" + M_AttributeSetInstance_ID);
		return retValue;
	}	//	get

	/**
	 * 	Get all Storages for Product with ASI
	 *	@param ctx context
	 *	@param M_Product_ID product
	 *	@param M_Locator_ID locator
	 *	@param FiFo first in-first-out
	 *	@param trx transaction
	 *	@return existing or null
	 */
	public static MStorage[] getAllWithASI (Ctx ctx, int M_Product_ID, int M_Locator_ID, 
		boolean FiFo, Trx trx)
	{
		ArrayList<MStorage> list = new ArrayList<MStorage>();
		String sql = "SELECT * FROM M_Storage "
			+ "WHERE M_Product_ID=? AND M_Locator_ID=?"
			+ " AND M_AttributeSetInstance_ID > 0"
			+ " AND QtyOnHand > 0 "
			+ "ORDER BY M_AttributeSetInstance_ID";
		if (!FiFo)
			sql += " DESC";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_Product_ID);
			pstmt.setInt (2, M_Locator_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MStorage (ctx, rs, trx));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			s_log.log(Level.SEVERE, sql, ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		MStorage[] retValue = new MStorage[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getAllWithASI

	/**
	 * 	Get all Storages for Product
	 *	@param ctx context
	 *	@param M_Product_ID product
	 *	@param M_Locator_ID locator
	 *	@param trx transaction
	 *	@return existing or null
	 */
	public static MStorage[] getAll (Ctx ctx, 
		int M_Product_ID, int M_Locator_ID, Trx trx)
	{
		ArrayList<MStorage> list = new ArrayList<MStorage>();
		String sql = "SELECT * FROM M_Storage "
			+ "WHERE M_Product_ID=? AND M_Locator_ID=?"
			+ " AND QtyOnHand <> 0 "
			+ "ORDER BY M_AttributeSetInstance_ID";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_Product_ID);
			pstmt.setInt (2, M_Locator_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MStorage (ctx, rs, trx));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			s_log.log(Level.SEVERE, sql, ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		MStorage[] retValue = new MStorage[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getAll

	
	/**
	 * 	Get Storage Info for Product across warehouses
	 *	@param ctx context
	 *	@param M_Product_ID product
	 *	@param trx transaction
	 *	@return existing or null
	 */
	public static MStorage[] getOfProduct (Ctx ctx, int M_Product_ID, Trx trx)
	{
		ArrayList<MStorage> list = new ArrayList<MStorage>();
		String sql = "SELECT * FROM M_Storage "
			+ "WHERE M_Product_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_Product_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MStorage (ctx, rs, trx));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			s_log.log(Level.SEVERE, sql, ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		MStorage[] retValue = new MStorage[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getOfProduct

	/*
	 * Trace back from storage record to original receipt line.
	 * @return MInOutLine or null
	 */
	public MInOutLine getM_InOutLineOf()
	{
		MInOutLine retValue = null;
		String sql = "SELECT * FROM M_InOutLine line "
			+ "WHERE M_AttributeSetInstance_ID=? "
			+ "OR EXISTS (SELECT 1 FROM "
			+ "M_InOutLineMA ma WHERE line.M_InOutLine_ID = ma.M_InOutLine_ID "
			+ "AND M_AttributeSetInstance_ID=?)";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_Trx());
			pstmt.setInt (1, getM_AttributeSetInstance_ID());
			pstmt.setInt (2, getM_AttributeSetInstance_ID());
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
				retValue = new MInOutLine (getCtx(), rs, get_Trx());
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			s_log.log(Level.SEVERE, sql, ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;

		return retValue;		
	}

	public static MStorage[] getWarehouse (Ctx ctx, int M_Warehouse_ID, 
			int M_Product_ID, int M_AttributeSetInstance_ID, int M_AttributeSet_ID,
			boolean allAttributeInstances, Timestamp minGuaranteeDate,
			boolean FiFo, Trx trx)
	{
		return getWarehouse(ctx, M_Warehouse_ID, M_Product_ID, M_AttributeSetInstance_ID, 
				M_AttributeSet_ID, allAttributeInstances, minGuaranteeDate, FiFo, false, 0, trx);
	}
	/**
	 * 	Get Storage Info for Warehouse
	 *	@param ctx context
	 *	@param M_Warehouse_ID 
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID instance
	 *	@param M_AttributeSet_ID attribute set
	 *	@param allAttributeInstances if true, all attribute set instances
	 *	@param minGuaranteeDate optional minimum guarantee date if all attribute instances
	 *	@param FiFo first in-first-out
	 *	@param trx transaction
	 *	@return existing - ordered by location priority (desc) and/or guarantee date
	 */
	public static MStorage[] getWarehouse (Ctx ctx, int M_Warehouse_ID, 
		int M_Product_ID, int M_AttributeSetInstance_ID, int M_AttributeSet_ID,
		boolean allAttributeInstances, Timestamp minGuaranteeDate,
		boolean FiFo, boolean allocationCheck, int M_SourceZone_ID, Trx trx)
	{
		if (M_Warehouse_ID == 0 || M_Product_ID == 0)
			return new MStorage[0];
		
		if (M_AttributeSet_ID == 0)
			allAttributeInstances = true;
		else
		{
			MAttributeSet mas = MAttributeSet.get(ctx, M_AttributeSet_ID);
			if (!mas.isInstanceAttribute())
				allAttributeInstances = true;
		}
		
		ArrayList<MStorage> list = new ArrayList<MStorage>();
		//	Specific Attribute Set Instance
		String sql = "SELECT s.M_Product_ID,s.M_Locator_ID,s.M_AttributeSetInstance_ID,"
			+ "s.AD_Client_ID,s.AD_Org_ID,s.IsActive,s.Created,s.CreatedBy,s.Updated,s.UpdatedBy,"
			+ "s.QtyOnHand,s.QtyReserved,s.QtyOrdered,s.DateLastInventory, s.QtyAllocated, "
			+ "s.QtyDedicated, s.QtyExpected "
			+ "FROM M_Storage s"
			+ " INNER JOIN M_Locator l ON (l.M_Locator_ID=s.M_Locator_ID) "
			+ "WHERE l.M_Warehouse_ID=?" 
			+ " AND s.M_Product_ID=?"
			+ " AND COALESCE(s.M_AttributeSetInstance_ID,0)=? ";
		
		if(allocationCheck)
			sql += "AND l.IsAvailableForAllocation='Y' ";
		
		if(M_SourceZone_ID != 0)
			sql += "AND l.M_Locator_ID IN " +
					" (SELECT M_Locator_ID FROM M_ZoneLocator WHERE M_Zone_ID = ? ) ";
		
		sql+= "ORDER BY l.PriorityNo DESC, M_AttributeSetInstance_ID";
		
		if (!FiFo)
			sql += " DESC";
		//	All Attribute Set Instances
		if (allAttributeInstances)
		{
			sql = "SELECT s.M_Product_ID,s.M_Locator_ID,s.M_AttributeSetInstance_ID,"
				+ "s.AD_Client_ID,s.AD_Org_ID,s.IsActive,s.Created,s.CreatedBy,s.Updated,s.UpdatedBy,"
				+ "s.QtyOnHand,s.QtyReserved,s.QtyOrdered,s.DateLastInventory, s.QtyAllocated, "
				+ "s.QtyDedicated, s.QtyExpected "
				+ "FROM M_Storage s"
				+ " INNER JOIN M_Locator l ON (l.M_Locator_ID=s.M_Locator_ID)"
				+ " LEFT OUTER JOIN M_AttributeSetInstance asi ON (s.M_AttributeSetInstance_ID=asi.M_AttributeSetInstance_ID) "
				+ "WHERE l.M_Warehouse_ID=?"
				+ " AND s.M_Product_ID=? ";

			if(allocationCheck)
				sql += "AND l.IsAvailableForAllocation='Y' ";

			if(M_SourceZone_ID != 0)
				sql += "AND l.M_Locator_ID IN " +
						" (SELECT M_Locator_ID FROM M_ZoneLocator WHERE M_Zone_ID = ? ) ";

			if (minGuaranteeDate != null)
			{
				sql += "AND (asi.GuaranteeDate IS NULL OR asi.GuaranteeDate>?) "
					+ "ORDER BY asi.GuaranteeDate,l.PriorityNo DESC, M_AttributeSetInstance_ID";	//	Has Prio over Locator
				if (!FiFo)
					sql += " DESC";
				sql += ", s.QtyOnHand DESC";
			}
			else
			{
				sql += "ORDER BY l.PriorityNo DESC, s.M_AttributeSetInstance_ID";
				if (!FiFo)
					sql += " DESC";
				sql += ", s.QtyOnHand DESC";
			}
		} 
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trx);
			int index = 1;
			pstmt.setInt(index++, M_Warehouse_ID);
			pstmt.setInt(index++, M_Product_ID);
			if(M_SourceZone_ID != 0)
				pstmt.setInt(index++, M_SourceZone_ID);
			if (!allAttributeInstances)
				pstmt.setInt(index++, M_AttributeSetInstance_ID);
			else if (minGuaranteeDate != null)
				pstmt.setTimestamp(index++, minGuaranteeDate);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add (new MStorage (ctx, rs, trx));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		MStorage[] retValue = new MStorage[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getWarehouse

	
	/**
	 * 	Create or Get Storage Info
	 *	@param ctx context
	 *	@param M_Locator_ID locator
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID instance
	 *	@param trx transaction
	 *	@return existing/new or null
	 */
	public static MStorage getCreate (Ctx ctx, int M_Locator_ID, 
		int M_Product_ID, int M_AttributeSetInstance_ID, Trx trx)
	{
		if (M_Locator_ID == 0)
			throw new IllegalArgumentException("M_Locator_ID=0");
		if (M_Product_ID == 0)
			throw new IllegalArgumentException("M_Product_ID=0");
		MStorage retValue = get(ctx, M_Locator_ID, M_Product_ID, M_AttributeSetInstance_ID, trx);
		if (retValue != null)
			return retValue;
		
		//	Insert row based on locator
		MLocator locator = new MLocator (ctx, M_Locator_ID, trx);
		if (locator.get_ID() != M_Locator_ID)
			throw new IllegalArgumentException("Not found M_Locator_ID=" + M_Locator_ID);
		//
		retValue = new MStorage (locator, M_Product_ID, M_AttributeSetInstance_ID);
		retValue.save(trx);
		s_log.fine("New " + retValue);
		return retValue;
	}	//	getCreate

	
	/**
	 * 	Update Storage Info add.
	 * 	Called from MProjectIssue
	 *	@param ctx context
	 *	@param M_Warehouse_ID warehouse
	 *	@param M_Locator_ID locator
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID AS Instance
	 *	@param reservationAttributeSetInstance_ID reservation AS Instance
	 *	@param diffQtyOnHand add on hand
	 *	@param diffQtyReserved add reserved
	 *	@param diffQtyOrdered add order
	 *	@param trx transaction
	 *	@return true if updated
	 */
	public static boolean add (Ctx ctx, int M_Warehouse_ID, int M_Locator_ID, 
		int M_Product_ID, int M_AttributeSetInstance_ID, int reservationAttributeSetInstance_ID,
		BigDecimal diffQtyOnHand, BigDecimal diffQtyReserved, 
		BigDecimal diffQtyOrdered, Trx trx)
	{
		MStorage storage = null;
		StringBuffer diffText = new StringBuffer("(");

		//	Get Storage
		if (storage == null)
			storage = getCreate (ctx, M_Locator_ID, 
				M_Product_ID, M_AttributeSetInstance_ID, trx);
		//	Verify
		if (storage.getM_Locator_ID() != M_Locator_ID 
			&& storage.getM_Product_ID() != M_Product_ID
			&& storage.getM_AttributeSetInstance_ID() != M_AttributeSetInstance_ID)
		{
			s_log.severe ("No Storage found - M_Locator_ID=" + M_Locator_ID 
				+ ",M_Product_ID=" + M_Product_ID + ",ASI=" + M_AttributeSetInstance_ID);
			return false;
		}
		MStorage storageASI = null;
		if (M_AttributeSetInstance_ID != reservationAttributeSetInstance_ID)
		{
			int reservationM_Locator_ID = M_Locator_ID;
			if (reservationAttributeSetInstance_ID == 0)
			{
				MWarehouse wh = MWarehouse.get (ctx, M_Warehouse_ID);
				reservationM_Locator_ID = wh.getDefaultM_Locator_ID();
			}
			storageASI = get(ctx, reservationM_Locator_ID, 
				M_Product_ID, reservationAttributeSetInstance_ID, trx);
			if (storageASI == null)	//	create if not existing - should not happen
			{
				MProduct product = MProduct.get(ctx, M_Product_ID);
				int xM_Locator_ID = MProductLocator.getFirstM_Locator_ID (product, M_Warehouse_ID);
				if (xM_Locator_ID == 0)
				{
					MWarehouse wh = MWarehouse.get (ctx, M_Warehouse_ID);
					xM_Locator_ID = wh.getDefaultM_Locator_ID();
				}
				storageASI = getCreate (ctx, xM_Locator_ID, 
					M_Product_ID, reservationAttributeSetInstance_ID, trx);
			}
		}		
		boolean changed = false;
		if (diffQtyOnHand != null && diffQtyOnHand.signum() != 0)
		{
			storage.setQtyOnHand (storage.getQtyOnHand().add (diffQtyOnHand));
			diffText.append("OnHand=").append(diffQtyOnHand);
			changed = true;
		}
		//	Reserved Qty
		if (diffQtyReserved != null && diffQtyReserved.signum() != 0)
		{
			if (storageASI == null)	
				storage.setQtyReserved (storage.getQtyReserved().add (diffQtyReserved));
			else
				storageASI.setQtyReserved (storageASI.getQtyReserved().add (diffQtyReserved));
			diffText.append(" Reserved=").append(diffQtyReserved);
			changed = true;
		}
		if (diffQtyOrdered != null && diffQtyOrdered.signum() != 0)
		{
			if (storageASI == null)
				storage.setQtyOrdered (storage.getQtyOrdered().add (diffQtyOrdered));
			else
				storageASI.setQtyOrdered (storageASI.getQtyOrdered().add (diffQtyOrdered));
			diffText.append(" Ordered=").append(diffQtyOrdered);
			changed = true;
		}
		if (changed)
		{
			diffText.append(") -> ").append(storage.toString());
			s_log.fine(diffText.toString());
			if (storageASI != null)
				storageASI.save(trx);		//	No AttributeSetInstance (reserved/ordered)
			return storage.save (trx);
		}
		
		return true;
	}	//	add

	/**
	 * 	Update Storage Info add.
	 * 	Called from MProjectIssue
	 *	@param ctx context
	 *	@param M_Warehouse_ID warehouse
	 *	@param M_Locator_ID locator
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID AS Instance
	 *	@param reservationAttributeSetInstance_ID reservation AS Instance
	 *	@param diffQtyOnHand add on hand
	 *	@param diffQtyReserved add reserved
	 *	@param diffQtyOrdered add order
	 *	@param trx transaction
	 *	@return true if updated
	 */
	public static boolean add (Ctx ctx, int M_Warehouse_ID, int M_Locator_ID, 
		int M_Product_ID, int M_AttributeSetInstance_ID, int reservationAttributeSetInstance_ID,
		BigDecimal diffQtyOnHand, BigDecimal diffQtyReserved, 
		BigDecimal diffQtyOrdered, BigDecimal diffQtyDedicated, 
		BigDecimal diffQtyExpected, BigDecimal diffQtyAllocated, 
		Trx trx)
	{
		MStorage storage = null;
		StringBuffer diffText = new StringBuffer("(");

		//	Get Storage
		if (storage == null)
			storage = getCreate (ctx, M_Locator_ID, 
				M_Product_ID, M_AttributeSetInstance_ID, trx);
		//	Verify
		if (storage.getM_Locator_ID() != M_Locator_ID 
			&& storage.getM_Product_ID() != M_Product_ID
			&& storage.getM_AttributeSetInstance_ID() != M_AttributeSetInstance_ID)
		{
			s_log.severe ("No Storage found - M_Locator_ID=" + M_Locator_ID 
				+ ",M_Product_ID=" + M_Product_ID + ",ASI=" + M_AttributeSetInstance_ID);
			return false;
		}
		MStorage storageASI = null;
		if (M_AttributeSetInstance_ID != reservationAttributeSetInstance_ID)
		{
			int reservationM_Locator_ID = M_Locator_ID;
			if (reservationAttributeSetInstance_ID == 0)
			{
				MWarehouse wh = MWarehouse.get (ctx, M_Warehouse_ID);
				reservationM_Locator_ID = wh.getDefaultM_Locator_ID();
			}
			storageASI = get(ctx, reservationM_Locator_ID, 
				M_Product_ID, reservationAttributeSetInstance_ID, trx);
			if (storageASI == null)	//	create if not existing - should not happen
			{
				MProduct product = MProduct.get(ctx, M_Product_ID);
				int xM_Locator_ID = MProductLocator.getFirstM_Locator_ID (product, M_Warehouse_ID);
				if (xM_Locator_ID == 0)
				{
					MWarehouse wh = MWarehouse.get (ctx, M_Warehouse_ID);
					xM_Locator_ID = wh.getDefaultM_Locator_ID();
				}
				storageASI = getCreate (ctx, xM_Locator_ID, 
					M_Product_ID, reservationAttributeSetInstance_ID, trx);
			}
		}		
		boolean changed = false;
		if (diffQtyOnHand != null && diffQtyOnHand.signum() != 0)
		{
			storage.setQtyOnHand (storage.getQtyOnHand().add (diffQtyOnHand));
			diffText.append("OnHand=").append(diffQtyOnHand);
			changed = true;
		}
		//	Reserved Qty
		if (diffQtyReserved != null && diffQtyReserved.signum() != 0)
		{
			if (storageASI == null)	
				storage.setQtyReserved (storage.getQtyReserved().add (diffQtyReserved));
			else
				storageASI.setQtyReserved (storageASI.getQtyReserved().add (diffQtyReserved));
			diffText.append(" Reserved=").append(diffQtyReserved);
			changed = true;
		}
		if (diffQtyOrdered != null && diffQtyOrdered.signum() != 0)
		{
			if (storageASI == null)
				storage.setQtyOrdered (storage.getQtyOrdered().add (diffQtyOrdered));
			else
				storageASI.setQtyOrdered (storageASI.getQtyOrdered().add (diffQtyOrdered));
			diffText.append(" Ordered=").append(diffQtyOrdered);
			changed = true;
		}
		if (diffQtyDedicated != null && diffQtyDedicated.signum() != 0)
		{
			if (storageASI == null)
				storage.setQtyDedicated (storage.getQtyDedicated().add (diffQtyDedicated));
			else
				storageASI.setQtyDedicated (storageASI.getQtyDedicated().add (diffQtyDedicated));
			diffText.append(" Dedicated=").append(diffQtyDedicated);
			changed = true;
		}
		if (diffQtyAllocated != null && diffQtyAllocated.signum() != 0)
		{
			if (storageASI == null)
				storage.setQtyAllocated (storage.getQtyAllocated().add (diffQtyAllocated));
			else
				storageASI.setQtyAllocated (storageASI.getQtyAllocated().add (diffQtyAllocated));
			diffText.append(" Allocated=").append(diffQtyAllocated);
			changed = true;
		}
		if (diffQtyExpected != null && diffQtyExpected.signum() != 0)
		{
			if (storageASI == null)
				storage.setQtyExpected (storage.getQtyExpected().add (diffQtyExpected));
			else
				storageASI.setQtyExpected (storageASI.getQtyExpected().add (diffQtyExpected));
			diffText.append(" Expected=").append(diffQtyExpected);
			changed = true;
		}
		if (changed)
		{
			diffText.append(") -> ").append(storage.toString());
			s_log.fine(diffText.toString());
			if (storageASI != null)
				storageASI.save(trx);		//	No AttributeSetInstance (reserved/ordered)
			return storage.save (trx);
		}
		
		return true;
	}	//	add
	
	/**************************************************************************
	 * 	Get Location with highest Locator Priority and a sufficient OnHand Qty
	 * 	@param M_Warehouse_ID warehouse
	 * 	@param M_Product_ID product
	 * 	@param M_AttributeSetInstance_ID asi
	 * 	@param Qty qty
	 *	@param trx transaction
	 * 	@return id
	 */
	public static int getM_Locator_ID (int M_Warehouse_ID, 
		int M_Product_ID, int M_AttributeSetInstance_ID, BigDecimal Qty,
		Trx trx)
	{
		int M_Locator_ID = 0;
		int firstM_Locator_ID = 0;
		String sql = "SELECT s.M_Locator_ID, s.QtyOnHand "
			+ "FROM M_Storage s"
			+ " INNER JOIN M_Locator l ON (s.M_Locator_ID=l.M_Locator_ID)"
			+ " INNER JOIN M_Product p ON (s.M_Product_ID=p.M_Product_ID)"
			+ " LEFT OUTER JOIN M_AttributeSet mas ON (p.M_AttributeSet_ID=mas.M_AttributeSet_ID) "
			+ "WHERE l.M_Warehouse_ID=?"
			+ " AND s.M_Product_ID=?"
			+ " AND (mas.IsInstanceAttribute IS NULL OR mas.IsInstanceAttribute='N' OR s.M_AttributeSetInstance_ID=?)"
			+ " AND l.IsActive='Y' "
			+ "ORDER BY l.PriorityNo DESC, s.QtyOnHand DESC";
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trx);
			pstmt.setInt(1, M_Warehouse_ID);
			pstmt.setInt(2, M_Product_ID);
			pstmt.setInt(3, M_AttributeSetInstance_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				BigDecimal QtyOnHand = rs.getBigDecimal(2);
				if (QtyOnHand != null && Qty.compareTo(QtyOnHand) <= 0)
				{
					M_Locator_ID = rs.getInt(1);
					break;
				}
				if (firstM_Locator_ID == 0)
					firstM_Locator_ID = rs.getInt(1);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			s_log.log(Level.SEVERE, sql, ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		if (M_Locator_ID != 0)
			return M_Locator_ID;
		return firstM_Locator_ID;
	}	//	getM_Locator_ID

	/**
	 * 	Get Available Qty.
	 * 	The call is accurate only if there is a storage record 
	 * 	and assumes that the product is stocked 
	 *	@param M_Warehouse_ID wh
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID masi
	 *	@param trx transaction
	 *	@return qty available (QtyOnHand-QtyReserved) or null
	 */
	public static BigDecimal getQtyAvailable (int M_Warehouse_ID, 
		int M_Product_ID, int M_AttributeSetInstance_ID, Trx trx)
	{
		BigDecimal QtyOnHand = Env.ZERO;
		BigDecimal QtyReserved = Env.ZERO;
		
		PreparedStatement pstmt = null;
		String sql = "SELECT SUM(QtyOnHand) "
			+ "FROM M_Storage s"
			+ " INNER JOIN M_Locator l ON (s.M_Locator_ID=l.M_Locator_ID AND l.IsAvailableToPromise='Y') "
			+ "WHERE s.M_Product_ID=?"		//	#1
			+ " AND l.M_Warehouse_ID=?";
		if (M_AttributeSetInstance_ID != 0)
			sql += " AND M_AttributeSetInstance_ID=?";
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_Product_ID);
			pstmt.setInt (2, M_Warehouse_ID);
			if (M_AttributeSetInstance_ID != 0)
				pstmt.setInt(3, M_AttributeSetInstance_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				QtyOnHand = rs.getBigDecimal(1);
				if (rs.wasNull())
					QtyOnHand = Env.ZERO;
			}
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
		
		sql = "SELECT SUM(QtyReserved) "
			+ "FROM M_Storage s"
			+ " INNER JOIN M_Locator l ON (s.M_Locator_ID=l.M_Locator_ID) "
			+ "WHERE s.M_Product_ID=?"		//	#1
			+ " AND l.M_Warehouse_ID=?";
		if (M_AttributeSetInstance_ID != 0)
			sql += " AND M_AttributeSetInstance_ID=?";
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_Product_ID);
			pstmt.setInt (2, M_Warehouse_ID);
			if (M_AttributeSetInstance_ID != 0)
				pstmt.setInt(3, M_AttributeSetInstance_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				QtyReserved = rs.getBigDecimal(1);
				if (rs.wasNull())
					QtyReserved = Env.ZERO;
			}
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
		s_log.fine("M_Warehouse_ID=" + M_Warehouse_ID 
			+ ",M_Product_ID=" + M_Product_ID + " : " 
			+ " QtyOnHand=" + QtyOnHand + ", QtyReserved="+QtyReserved);

		return QtyOnHand.subtract(QtyReserved);
	}	//	getQtyAvailable
	
	/**
	 * 	Get Onhand Qty in a given locator.
	 *	@param M_Warehouse_ID wh
	 *	 @param M_Locator_ID locator
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID masi
	 *	@param trx transaction
	 *	@return qty onhand(QtyOnHand) or zero
	 */
	public BigDecimal getQtyOnHand (int M_Warehouse_ID, int M_Locator_ID,
		int M_Product_ID, int M_AttributeSetInstance_ID, Trx trx)
	{
		BigDecimal retValue = Env.ZERO;
		PreparedStatement pstmt = null;
		String sql = "SELECT SUM(QtyOnHand) "
			+ "FROM M_Storage s"
			+ " INNER JOIN M_Locator l ON (s.M_Locator_ID=l.M_Locator_ID) "
			+ "WHERE s.M_Product_ID=?"		//	#1
			+ " AND l.M_Warehouse_ID=?"
			+ " AND l.M_Locator_ID=?"
			+ " AND M_AttributeSetInstance_ID<>?";
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_Product_ID);
			pstmt.setInt (2, M_Warehouse_ID);
			pstmt.setInt (3, M_Locator_ID);
			pstmt.setInt(4, M_AttributeSetInstance_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				retValue = rs.getBigDecimal(1);
				if (rs.wasNull())
					retValue = Env.ZERO;
			}
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
		
		// Add qty onhand for current record
		retValue = retValue.add(getQtyOnHand());
		s_log.fine("M_Warehouse_ID=" + M_Warehouse_ID 
			+ ",M_Product_ID=" + M_Product_ID + " = " + retValue);
		return retValue;
	}	//	getQtynhand

	/**
	 * 	Get Dedicated Qty in a given locator.
	 *	@param M_Warehouse_ID wh
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID masi
	 *	@param trx transaction
	 *	@return qty Dedicated (QtyDedicated) or zero
	 */
	public BigDecimal getQtyDedicated (int M_Warehouse_ID, int M_Locator_ID,
		int M_Product_ID, int M_AttributeSetInstance_ID, Trx trx)
	{
		BigDecimal retValue = Env.ZERO;
		PreparedStatement pstmt = null;
		String sql = "SELECT SUM(QtyDedicated) "
			+ "FROM M_Storage s"
			+ " INNER JOIN M_Locator l ON (s.M_Locator_ID=l.M_Locator_ID) "
			+ "WHERE s.M_Product_ID=?"		//	#1
			+ " AND l.M_Warehouse_ID=?"
			+ " AND l.M_Locator_ID=?"
			+ " AND M_AttributeSetInstance_ID<>?";
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_Product_ID);
			pstmt.setInt (2, M_Warehouse_ID);
			pstmt.setInt (3, M_Locator_ID);
			pstmt.setInt(4, M_AttributeSetInstance_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				retValue = rs.getBigDecimal(1);
				if (rs.wasNull())
					retValue = Env.ZERO;
			}
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
		
		// Add qty dedicated for current record
		retValue = retValue.add(getQtyDedicated());
		
		s_log.fine("M_Warehouse_ID=" + M_Warehouse_ID 
			+ ",M_Product_ID=" + M_Product_ID + " = " + retValue);
		return retValue;
	}	//	getQtyDedicated
	
	/**
	 * 	Get Allocated Qty.
	 *	@param M_Warehouse_ID wh
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID masi
	 *	@param trx transaction
	 *	@return qty Dedicated (QtyDedicated) or zero
	 */
	public BigDecimal getQtyAllocated (int M_Warehouse_ID,int M_Locator_ID, 
		int M_Product_ID, int M_AttributeSetInstance_ID, Trx trx)
	{
		BigDecimal retValue = Env.ZERO;
		PreparedStatement pstmt = null;
		String sql = "SELECT SUM(QtyAllocated) "
			+ "FROM M_Storage s"
			+ " INNER JOIN M_Locator l ON (s.M_Locator_ID=l.M_Locator_ID) "
			+ "WHERE s.M_Product_ID=?"		//	#1
			+ " AND l.M_Warehouse_ID=?"
			+ " AND l.M_Locator_ID=?"
			+ " AND M_AttributeSetInstance_ID<>?";
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_Product_ID);
			pstmt.setInt (2, M_Warehouse_ID);
			pstmt.setInt (3, M_Locator_ID);
			pstmt.setInt(4, M_AttributeSetInstance_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				retValue = rs.getBigDecimal(1);
				if (rs.wasNull())
					retValue = Env.ZERO;
			}
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
		
		// Add qty dedicated for current record
		retValue = retValue.add(getQtyAllocated());
		
		s_log.fine("M_Warehouse_ID=" + M_Warehouse_ID 
			+ ",M_Product_ID=" + M_Product_ID + " = " + retValue);
		return retValue;
	}	//	getQtyAllocated


	/**************************************************************************
	 * 	Persistency Constructor
	 *	@param ctx context
	 *	@param ignored ignored
	 *	@param trx transaction
	 */
	public MStorage (Ctx ctx, int ignored, Trx trx)
	{
		super(ctx, 0, trx);
		if (ignored != 0)
			throw new IllegalArgumentException("Multi-Key");
		//
		setQtyOnHand (Env.ZERO);
		setQtyOrdered (Env.ZERO);
		setQtyReserved (Env.ZERO);
		setQtyAllocated (Env.ZERO);
		setQtyDedicated (Env.ZERO);
	}	//	MStorage

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MStorage (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MStorage

	/**
	 * 	Full NEW Constructor
	 *	@param locator (parent) locator
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID attribute
	 */
	private MStorage (MLocator locator, int M_Product_ID, int M_AttributeSetInstance_ID)
	{
		this (locator.getCtx(), 0, locator.get_Trx());
		setClientOrg(locator);
		setM_Locator_ID (locator.getM_Locator_ID());
		setM_Product_ID (M_Product_ID);
		setM_AttributeSetInstance_ID (M_AttributeSetInstance_ID);
	}	//	MStorage

	/** Log								*/
	private static CLogger		s_log = CLogger.getCLogger (MStorage.class);
	/** Warehouse						*/
	private int		m_M_Warehouse_ID = 0;

	
	/**
	 * 	Change Qty OnHand
	 *	@param qty quantity
	 *	@param add add if true 
	 */
	public void changeQtyOnHand (BigDecimal qty, boolean add)
	{
		if (qty == null || qty.signum() == 0)
			return;
		if (add)
			setQtyOnHand(getQtyOnHand().add(qty));
		else
			setQtyOnHand(getQtyOnHand().subtract(qty));
	}	//	changeQtyOnHand

	/**
	 * 	Get M_Warehouse_ID of Locator
	 *	@return warehouse
	 */
	public int getM_Warehouse_ID()
	{
		if (m_M_Warehouse_ID == 0)
		{
			MLocator loc = MLocator.get(getCtx(), getM_Locator_ID());
			m_M_Warehouse_ID = loc.getM_Warehouse_ID();
		}
		return m_M_Warehouse_ID;
	}	//	getM_Warehouse_ID

	/**
	 * Before Save
	 * @param newRecord new
	 * @param success success
	 * @return success
	 */
	@Override
	protected boolean beforeSave(boolean newRecord) 
	{
		//	Negative Inventory check
		if (newRecord || is_ValueChanged("QtyOnHand") 
						|| is_ValueChanged("QtyDedicated")
						|| is_ValueChanged("QtyAllocated")
						|| is_ValueChanged("QtyExpected"))
		{
			
			MWarehouse wh = new MWarehouse(getCtx(), getM_Warehouse_ID(), get_Trx());
			if(wh.isDisallowNegativeInv())
			{
				PreparedStatement pstmt = null;
				BigDecimal QtyOnHand =Env.ZERO;
				BigDecimal QtyDedicated =Env.ZERO;
				BigDecimal QtyAllocated =Env.ZERO;
				
				String sql = "SELECT SUM(QtyOnHand),SUM(QtyDedicated),SUM(QtyAllocated) "
					+ "FROM M_Storage s"
					+ " INNER JOIN M_Locator l ON (s.M_Locator_ID=l.M_Locator_ID) "
					+ "WHERE s.M_Product_ID=?"		//	#1
					+ " AND l.M_Warehouse_ID=?"
					+ " AND l.M_Locator_ID=?"
					+ " AND M_AttributeSetInstance_ID<>?";
				try
				{
					pstmt = DB.prepareStatement (sql, get_Trx());
					pstmt.setInt (1, getM_Product_ID());
					pstmt.setInt (2, getM_Warehouse_ID());
					pstmt.setInt (3, getM_Locator_ID());
					pstmt.setInt(4, getM_AttributeSetInstance_ID());
					ResultSet rs = pstmt.executeQuery ();
					if (rs.next ())
					{
						QtyOnHand = rs.getBigDecimal(1);
						if (rs.wasNull())
							QtyOnHand = Env.ZERO;

						QtyDedicated = rs.getBigDecimal(2);
						if (rs.wasNull())
							QtyDedicated = Env.ZERO;

						QtyAllocated = rs.getBigDecimal(3);
						if (rs.wasNull())
							QtyAllocated = Env.ZERO;

					}
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
				
				QtyOnHand = QtyOnHand.add(getQtyOnHand());
				QtyDedicated = QtyDedicated.add(getQtyDedicated());
				QtyAllocated = QtyAllocated.add(getQtyAllocated());
				
				if(getQtyOnHand().signum() < 0 ||
						getQtyDedicated().signum() < 0 ||
						getQtyAllocated().signum() < 0 ||
						getQtyExpected().signum() < 0 ||
						QtyOnHand.signum() < 0 ||
						QtyOnHand.compareTo(QtyDedicated.add(QtyAllocated)) < 0 ||
						getQtyOnHand().compareTo(getQtyDedicated().add(getQtyAllocated())) < 0)
				{
					log.saveError("Error", Msg.getMsg(getCtx(), "NegativeInventoryDisallowed"));
					return false;
				}
			}
		}

		return super.beforeSave(newRecord);
	}
	/**
	 *	String Representation
	 * 	@return info
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MStorage[")
			.append("M_Locator_ID=").append(getM_Locator_ID())
				.append(",M_Product_ID=" +
						"").append(getM_Product_ID())
				.append(",M_AttributeSetInstance_ID=").append(getM_AttributeSetInstance_ID())
			.append(": OnHand=").append(getQtyOnHand())
			.append(",Reserved=").append(getQtyReserved())
			.append(",Ordered=").append(getQtyOrdered())
			.append(",Dedicated=").append(getQtyDedicated())
			.append(",Allocated=").append(getQtyAllocated())
			.append("]");
		return sb.toString();
	}	//	toString

}	//	MStorage
