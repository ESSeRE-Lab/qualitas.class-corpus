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

import org.compiere.framework.*;
import org.compiere.util.*;

/**
 * 	Cost Element Model
 *  @author Jorg Janke
 *  @version $Id: MCostElement.java,v 1.2 2006/07/30 00:58:04 jjanke Exp $
 */
public class MCostElement extends X_M_CostElement
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 	Get Material Cost Element or create it
	 *	@param po parent
	 *	@param CostingMethod method
	 *	@return cost element
	 */
	public static MCostElement getMaterialCostElement (PO po, String CostingMethod)
	{
		if (CostingMethod == null || CostingMethod.length() == 0)
		{
			s_log.severe("No CostingMethod");
			return null;
		}
		//
		MCostElement retValue = null;
		String sql = "SELECT * FROM M_CostElement WHERE AD_Client_ID=? AND CostingMethod=? ORDER BY AD_Org_ID";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, po.get_Trx());
			pstmt.setInt (1, po.getAD_Client_ID());
			pstmt.setString(2, CostingMethod);
			ResultSet rs = pstmt.executeQuery ();
			boolean n = rs.next(); //jz to fix DB2 resultSet closed problem
			if (n)
				retValue = new MCostElement (po.getCtx(), rs, po.get_Trx());
			if (n && rs.next())
				s_log.warning("More then one Material Cost Element for CostingMethod=" + CostingMethod);
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log (Level.SEVERE, sql, e);
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
		if (retValue != null)
			return retValue;
		
		//	Create New
		retValue = new MCostElement (po.getCtx(), 0, po.get_Trx());
		retValue.setClientOrg(po.getAD_Client_ID(), 0);
		String name = MRefList.getListName(po.getCtx(), COSTINGMETHOD_AD_Reference_ID, CostingMethod);
		if (name == null || name.length() == 0)
			name = CostingMethod;
		retValue.setName(name);
		retValue.setCostElementType(COSTELEMENTTYPE_Material);
		retValue.setCostingMethod(CostingMethod);
		retValue.save();
		//
		return retValue;
	}	//	getMaterialCostElement

	/**
	 * 	Get first Material Cost Element
	 *	@param ctx context
	 *	@param CostingMethod costing method
	 *	@return Cost Element or null
	 */
	public static MCostElement getMaterialCostElement(Ctx ctx, String CostingMethod)
	{
		MCostElement retValue = null;
		String sql = "SELECT * FROM M_CostElement WHERE AD_Client_ID=? AND CostingMethod=? ORDER BY AD_Org_ID";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, ctx.getAD_Client_ID());
			pstmt.setString(2, CostingMethod);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
				retValue = new MCostElement (ctx, rs, null);
			if (rs.next())
				s_log.info("More then one Material Cost Element for CostingMethod=" + CostingMethod);
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log (Level.SEVERE, sql, e);
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
	}	//	getMaterialCostElement
	
	
	/**
	 * 	Get active Material Cost Element for client 
	 *	@param po parent
	 *	@return cost element array
	 */
	public static MCostElement[] getCostingMethods (PO po)
	{
		ArrayList<MCostElement> list = new ArrayList<MCostElement>();
		String sql = "SELECT * FROM M_CostElement "
			+ "WHERE AD_Client_ID=?"
			+ " AND IsActive='Y' AND CostElementType='M' AND CostingMethod IS NOT NULL";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, po.get_Trx());
			pstmt.setInt (1, po.getAD_Client_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MCostElement (po.getCtx(), rs, po.get_Trx()));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log (Level.SEVERE, sql, e);
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
		MCostElement[] retValue = new MCostElement[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getMaterialCostElement

	
	/**
	 * 	Get Cost Element from Cache
	 *	@param ctx context
	 *	@param M_CostElement_ID id
	 *	@return Cost Element
	 */
	public static MCostElement get (Ctx ctx, int M_CostElement_ID)
	{
		Integer key = Integer.valueOf (M_CostElement_ID);
		MCostElement retValue = s_cache.get (ctx, key);
		if (retValue != null)
			return retValue;
		retValue = new MCostElement (ctx, M_CostElement_ID, null);
		if (retValue.get_ID () != 0)
			s_cache.put (key, retValue);
		return retValue;
	} //	get

	/**	Cache						*/
	private static final CCache<Integer,MCostElement>	s_cache	= new CCache<Integer,MCostElement>("M_CostElement", 20);
	
	/**	Logger	*/
	private static final CLogger	s_log	= CLogger.getCLogger (MCostElement.class);
	
	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param M_CostElement_ID id
	 *	@param trx p_trx
	 */
	public MCostElement (Ctx ctx, int M_CostElement_ID, Trx trx)
	{
		super (ctx, M_CostElement_ID, trx);
		if (M_CostElement_ID == 0)
		{
		//	setName (null);
			setCostElementType (COSTELEMENTTYPE_Material);
			setIsCalculated (false);
		}
	}	//	MCostElement

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx p_trx
	 */
	public MCostElement (Ctx ctx, ResultSet rs, Trx trx)
	{
		super (ctx, rs, trx);
	}	//	MCostElement
	
	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	@Override
	protected boolean beforeSave (boolean newRecord)
	{
		//	Check Unique Costing Method
		if (COSTELEMENTTYPE_Material.equals(getCostElementType())
			&& (newRecord || is_ValueChanged("CostingMethod")))
		{
			String sql = "SELECT COALESCE(MAX(M_CostElement_ID),0) FROM M_CostElement "
				+ "WHERE AD_Client_ID=? AND CostingMethod=?";
			int id = DB.getSQLValue(get_Trx(), sql, getAD_Client_ID(), getCostingMethod());
			if (id > 0 && id != get_ID())
			{
				log.saveError("AlreadyExists", Msg.getElement(getCtx(), "CostingMethod"));
				return false;
			}
		}

		//	Maintain Calclated
		if (COSTELEMENTTYPE_Material.equals(getCostElementType()))
		{
			String cm = getCostingMethod();
			if (cm == null || cm.length() == 0
				|| COSTINGMETHOD_StandardCosting.equals(cm))
				setIsCalculated(false);
			else
				setIsCalculated(true);
		}
		else
		{
			if (isCalculated())
				setIsCalculated(false);
			if (getCostingMethod() != null)
				setCostingMethod(null);
		}
		
		if (getAD_Org_ID() != 0)
			setAD_Org_ID(0);
		return true;
	}	//	beforeSave
	
	/**
	 * 	Before Delete
	 *	@return true if can be deleted
	 */
	@Override
	protected boolean beforeDelete ()
	{
		String cm = getCostingMethod();
		if (cm == null
			|| !COSTELEMENTTYPE_Material.equals(getCostElementType()))
			return true;
		
		//	Costing Methods on AS level
		MAcctSchema[] ass = MAcctSchema.getClientAcctSchema(getCtx(), getAD_Client_ID());
		for (MAcctSchema element : ass) {
			if (element.getCostingMethod().equals(getCostingMethod()))
			{
				log.saveError("CannotDeleteUsed", Msg.getElement(getCtx(), "C_AcctSchema_ID") 
					+ " - " + element.getName());
				return false;
			}
		}
		
		//	Costing Methods on PC level
		String sql = "SELECT M_Product_Category_ID FROM M_Product_Category_Acct WHERE AD_Client_ID=? AND CostingMethod=?";
		int M_Product_Category_ID = 0;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, getAD_Client_ID());
			pstmt.setString (2, getCostingMethod());
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				M_Product_Category_ID = rs.getInt(1);
			}
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
		if (M_Product_Category_ID != 0)
		{
			log.saveError("CannotDeleteUsed", Msg.getElement(getCtx(), "M_Product_Category_ID") 
				+ " (ID=" + M_Product_Category_ID + ")");
			return false;
		}
		return true;
	}	//	beforeDelete
	
	/**
	 * 	Is this a Costing Method
	 *	@return true if not Material cost or no costing method.
	 */
	public boolean isCostingMethod()
	{
		return COSTELEMENTTYPE_Material.equals(getCostElementType())
			&& getCostingMethod() != null;
	}	//	isCostingMethod
	
	/**
	 * 	Is Avg Invoice Costing Method
	 *	@return true if AverageInvoice
	 */
	public boolean isAverageInvoice()
	{
		String cm = getCostingMethod();
		return cm != null 
			&& cm.equals(COSTINGMETHOD_AverageInvoice)
			&& COSTELEMENTTYPE_Material.equals(getCostElementType());
	}	//	isAverageInvoice
	
	/**
	 * 	Is Avg PO Costing Method
	 *	@return true if AveragePO
	 */
	public boolean isAveragePO()
	{
		String cm = getCostingMethod();
		return cm != null 
			&& cm.equals(COSTINGMETHOD_AveragePO)
			&& COSTELEMENTTYPE_Material.equals(getCostElementType());
	}	//	isAveragePO
	/**
	 * 	Is FiFo Costing Method
	 *	@return true if Fifo
	 */
	public boolean isFifo()
	{
		String cm = getCostingMethod();
		return cm != null 
			&& cm.equals(COSTINGMETHOD_FiFo)
			&& COSTELEMENTTYPE_Material.equals(getCostElementType());
	}	//	isFifo
	/**
	 * 	Is Last Invoice Costing Method
	 *	@return true if LastInvoice
	 */
	public boolean isLastInvoice()
	{
		String cm = getCostingMethod();
		return cm != null 
			&& cm.equals(COSTINGMETHOD_LastInvoice)
			&& COSTELEMENTTYPE_Material.equals(getCostElementType());
	}	//	isLastInvoice
	/**
	 * 	Is Last PO Costing Method
	 *	@return true if LastPOPrice
	 */
	public boolean isLastPOPrice()
	{
		String cm = getCostingMethod();
		return cm != null 
			&& cm.equals(COSTINGMETHOD_LastPOPrice)
			&& COSTELEMENTTYPE_Material.equals(getCostElementType());
	}	//	isLastPOPrice
	/**
	 * 	Is LiFo Costing Method
	 *	@return true if Lifo
	 */
	public boolean isLifo()
	{
		String cm = getCostingMethod();
		return cm != null 
			&& cm.equals(COSTINGMETHOD_LiFo)
			&& COSTELEMENTTYPE_Material.equals(getCostElementType());
	}	//	isLiFo
	/**
	 * 	Is Std Costing Method
	 *	@return true if StandardCosting
	 */
	public boolean isStandardCosting()
	{
		String cm = getCostingMethod();
		return cm != null 
			&& cm.equals(COSTINGMETHOD_StandardCosting)
			&& COSTELEMENTTYPE_Material.equals(getCostElementType());
	}	//	isStandardCosting
	/**
	 * 	Is User Costing Method
	 *	@return true if User Defined
	 */
	public boolean isUserDefined()
	{
		String cm = getCostingMethod();
		return cm != null 
			&& cm.equals(COSTINGMETHOD_UserDefined)
			&& COSTELEMENTTYPE_Material.equals(getCostElementType());
	}	//	isAveragePO
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	@Override
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MCostElement[");
		sb.append (get_ID ())
			.append ("-").append (getName())
			.append(",Type=").append(getCostElementType())
			.append(",Method=").append(getCostingMethod())
			.append ("]");
		return sb.toString ();
	} //	toString
	
}	//	MCostElement
