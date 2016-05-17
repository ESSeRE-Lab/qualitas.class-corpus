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
package org.compiere.process;

import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 * 	Standard Cost Update
 *	
 *  @author Jorg Janke
 *  @version $Id: CostUpdate.java,v 1.3 2006/07/30 00:51:02 jjanke Exp $
 */
public class CostUpdate extends SvrProcess
{
	/**	Product Category		*/
	private int		p_M_Product_Category_ID = 0;
	/** Future Costs			*/
	private String	p_SetFutureCostTo = null;
	/** Standard Costs			*/
	private String	p_SetStandardCostTo = null;
	/** PLV						*/
	private int 	p_M_PriceList_Version_ID = 0;
	
	
	private static final String	TO_AveragePO = "A";
	private static final String	TO_AverageInvoiceHistory = "DI";
	private static final String	TO_AveragePOHistory = "DP";
	private static final String	TO_FiFo = "F";
	private static final String	TO_AverageInvoice = "I";
	private static final String	TO_LiFo = "L";
	private static final String	TO_PriceListLimit = "LL";
	private static final String	TO_StandardCost = "S";
	private static final String	TO_FutureStandardCost = "f";
	private static final String	TO_LastInvoicePrice = "i";
	private static final String	TO_LastPOPrice = "p";
	private static final String	TO_OldStandardCost = "x";

	/** Standard Cost Element		*/
	private MCostElement 	m_ce = null;
	/** Client Accounting SChema	*/
	private MAcctSchema[]	m_ass = null;
	/** Map of Cost Elements		*/
	private HashMap<String,MCostElement>	m_ces = new HashMap<String,MCostElement>();
	
	
	/**
	 * 	Prepare
	 */
	@Override
	protected void prepare ()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) {
			String name = element.getParameterName();
		//	log.fine("prepare - " + para[i]);
			if (element.getParameter() == null)
				;
			else if (name.equals("M_Product_Category_ID"))
				p_M_Product_Category_ID = element.getParameterAsInt();
			else if (name.equals("SetFutureCostTo"))
				p_SetFutureCostTo = (String)element.getParameter();
			else if (name.equals("SetStandardCostTo"))
				p_SetStandardCostTo = (String)element.getParameter();
			else if (name.equals("M_PriceList_Version_ID"))
				p_M_PriceList_Version_ID = element.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);		
		}

	}	//	prepare	

	/**
	 * 	Process
	 *	@return info
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		boolean success = false;
		log.info("M_Product_Category_ID=" + p_M_Product_Category_ID
			+ ", Future=" + p_SetFutureCostTo
			+ ", Standard=" + p_SetStandardCostTo
			+ "; M_PriceList_Version_ID=" + p_M_PriceList_Version_ID);
		if (p_SetFutureCostTo == null)
			p_SetFutureCostTo = "";
		if (p_SetStandardCostTo == null)
			p_SetStandardCostTo = "";
		//	Nothing to Do
		if (p_SetFutureCostTo.length() == 0 && p_SetStandardCostTo.length() == 0)
		{
			return "-";
		}
		//	PLV required
		if (p_M_PriceList_Version_ID == 0
			&& (p_SetFutureCostTo.equals(TO_PriceListLimit) || p_SetStandardCostTo.equals(TO_PriceListLimit)))
			throw new CompiereUserException ("@FillMandatory@  @M_PriceList_Version_ID@");
		
		//	Validate Source
		if (!isValid(p_SetFutureCostTo))
			throw new CompiereUserException ("@NotFound@ @M_CostElement_ID@ (Future) " + p_SetFutureCostTo);
		if (!isValid(p_SetStandardCostTo))
			throw new CompiereUserException ("@NotFound@ @M_CostElement_ID@ (Standard) " + p_SetStandardCostTo);
		
		// Check for unprocessed Cost Update transactions. If one exists, then error out
		if (!isUnprocessedExist())
			throw new CompiereUserException (" Unprocessed/unposted Cost Update transaction exists. Please process them first");

		//	Prepare
		MClient client = MClient.get(getCtx());
		m_ce = MCostElement.getMaterialCostElement(client, X_C_AcctSchema.COSTINGMETHOD_StandardCosting);
		if (m_ce.get_ID() == 0)
			throw new CompiereUserException ("@NotFound@ @M_CostElement_ID@ (StdCost)");
		log.config(m_ce.toString());
		m_ass = MAcctSchema.getClientAcctSchema(getCtx(), client.getAD_Client_ID());
		for (MAcctSchema element : m_ass)
			createNew(element);
		commit();
		
	
		//	Update Cost
		int counter = update();
		
		// Create Document 
		if (counter !=0)
		{
			success = createDoc();
		}
				
		// Commit or Roll back the changes.
		if (success)
				get_Trx().commit();
		else {
				get_Trx().rollback();
				throw new CompiereUserException("Error in updating standard cost");
		}
		
		log.info("#" + counter);
		addLog(0, null, new BigDecimal(counter), "@Updated@");

		return "#" + counter;
	}	//	doIt
	
	/**
	 * 	Costing Method must exist
	 *	@param to test
	 *	@return true valid
	 */
	private boolean isValid(String to)
	{
		if (p_SetFutureCostTo.length() == 0)
			return true;
		
		if (to.equals(TO_AverageInvoiceHistory))
			to = TO_AverageInvoice;
		if (to.equals(TO_AveragePOHistory))
			to = TO_AveragePO;
		if (to.equals(TO_FutureStandardCost))
			to = TO_StandardCost;
		//
		if (to.equals(TO_AverageInvoice)
			|| to.equals(TO_AveragePO)
			|| to.equals(TO_FiFo)
			|| to.equals(TO_LiFo)
			|| to.equals(TO_StandardCost))
		{
			MCostElement ce = getCostElement(p_SetFutureCostTo);
			return ce != null;
		}
		return true;
	}	//	isValid
	
	/**************************************************************************
	 * 	Create New Standard Costs
	 * 	@param as accounting schema
	 */
	private void createNew (MAcctSchema as)
	{
		if (!as.getCostingLevel().equals(X_C_AcctSchema.COSTINGLEVEL_Tenant))
		{
			String txt = "Costing Level prevents creating new Costing records for " + as.getName();
			log.warning(txt);
			addLog(0, null, null, txt);
			return;
		}
		
		String sql = "SELECT * FROM M_Product p "
			+ "WHERE NOT EXISTS (SELECT * FROM M_Cost c WHERE c.M_Product_ID=p.M_Product_ID"
			+ " AND c.M_CostType_ID=? AND c.C_AcctSchema_ID=? AND c.M_CostElement_ID=?"
			+ " AND c.M_AttributeSetInstance_ID=0) "
			+ "AND AD_Client_ID=?";
		if (p_M_Product_Category_ID != 0)
			sql += " AND M_Product_Category_ID=?"; 
		int counter = 0;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, as.getM_CostType_ID());
			pstmt.setInt (2, as.getC_AcctSchema_ID());
			pstmt.setInt (3, m_ce.getM_CostElement_ID());
			pstmt.setInt (4, as.getAD_Client_ID());
			if (p_M_Product_Category_ID != 0)
				pstmt.setInt (5, p_M_Product_Category_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				if (createNew (new MProduct (getCtx(), rs, null), as))
					counter++;
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
		log.info("#" + counter);
		addLog(0, null, new BigDecimal(counter), "Created for " + as.getName());
	}	//	createNew
	
	/**
	 * 	Create New Client level Costing Record
	 *	@param product product
	 *	@param as acct schema
	 *	@return true if created
	 */
	private boolean createNew (MProduct product, MAcctSchema as)
	{
		MCost cost = MCost.get(product, 0, as, 0, m_ce.getM_CostElement_ID());
		if (cost.is_new())
			return cost.save();
		return false;
	}	//	createNew

	/**************************************************************************
	 * 	Update Cost Records
	 * 	@return no updated
	 */
	private int update()
	{
		int counter = 0;
		String sql = "SELECT * FROM M_Cost c WHERE M_CostElement_ID=?" 
			       + " AND AD_Client_ID = ?";
		if (p_M_Product_Category_ID != 0)
			sql += " AND EXISTS (SELECT * FROM M_Product p "
				+ " WHERE c.M_Product_ID=p.M_Product_ID AND p.M_Product_Category_ID=? AND p.AD_Client_ID = c.AD_Client_ID)";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, m_ce.getM_CostElement_ID());
			pstmt.setInt (2, getAD_Client_ID());
			if (p_M_Product_Category_ID != 0)
				pstmt.setInt (3, p_M_Product_Category_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				MCost cost = new MCost (getCtx(), rs, get_TrxName());
				for (MAcctSchema element : m_ass) {
					//	Update Costs only for default Cost Type
					if (element.getC_AcctSchema_ID() == cost.getC_AcctSchema_ID() 
						&& element.getM_CostType_ID() == cost.getM_CostType_ID())
					{
						if (update (cost))
							counter++;
						else
							return 0;
						
					}
				}
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
		return counter;
	}	//	update

	/**
	 * 	Update Cost Records
	 *	@param cost cost
	 *	@return true if updated
	 *	@throws Exception
	 */
	private boolean update (MCost cost) throws Exception
	{
		boolean updated = false;
		
		// update Last cost price
		BigDecimal lastCostPrice = cost.getCurrentCostPrice();
		cost.setLastCostPrice(lastCostPrice);
				
		// get current standard cost
		if (p_SetFutureCostTo.equals(p_SetStandardCostTo))
		{
			BigDecimal costs = getCosts(cost, p_SetFutureCostTo);
			if (costs != null)
			{
				cost.setFutureCostPrice(costs);
				cost.setCurrentCostPrice(costs);
				updated = true;
			}
		}
		else
		{
			if (p_SetStandardCostTo.length() > 0)
			{
				BigDecimal costs = getCosts(cost, p_SetStandardCostTo);
				if (costs != null)
				{   cost.setCurrentCostPrice(costs);
					updated = true;
				}
			}
			if (p_SetFutureCostTo.length() > 0)
			{
				BigDecimal costs = getCosts(cost, p_SetFutureCostTo);
				if (costs != null)
				{
					cost.setFutureCostPrice(costs);
					updated = true;
				}
			}
		}
		if (updated)
			updated = cost.save(get_TrxName());
			
		return updated;
	}	//	update
	
	/**
	 * 	Get Costs
	 *	@param cost cost
	 *	@param to where to get costs from 
	 *	@return costs (could be 0) or null if not found
	 *	@throws Exception
	 */
	private BigDecimal getCosts (MCost cost, String to) throws Exception
	{
		BigDecimal retValue = Env.ZERO;
		
		//	Average Invoice
		if (to.equals(TO_AverageInvoice))
		{
			MCostElement ce = getCostElement(TO_AverageInvoice);
			if (ce == null)
				throw new CompiereSystemException("CostElement not found: " + TO_AverageInvoice);
			MCost xCost = MCost.get(getCtx(), cost.getAD_Client_ID(), cost.getAD_Org_ID(), cost.getM_Product_ID(), cost.getM_CostType_ID(), cost.getC_AcctSchema_ID(), ce.getM_CostElement_ID(), cost.getM_AttributeSetInstance_ID());
			if (xCost != null)
				retValue = xCost.getCurrentCostPrice();
		}
		//	Average Invoice History
		else if (to.equals(TO_AverageInvoiceHistory))
		{
			MCostElement ce = getCostElement(TO_AverageInvoice);
			if (ce == null)
				throw new CompiereSystemException("CostElement not found: " + TO_AverageInvoice);
			MCost xCost = MCost.get(getCtx(), cost.getAD_Client_ID(), cost.getAD_Org_ID(), cost.getM_Product_ID(), cost.getM_CostType_ID(), cost.getC_AcctSchema_ID(), ce.getM_CostElement_ID(), cost.getM_AttributeSetInstance_ID());
			if (xCost != null) 
				retValue = xCost.getHistoryAverage();
		}
		
		//	Average PO
		else if (to.equals(TO_AveragePO))
		{
			MCostElement ce = getCostElement(TO_AveragePO);
			if (ce == null)
				throw new CompiereSystemException("CostElement not found: " + TO_AveragePO);
			MCost xCost = MCost.get(getCtx(), cost.getAD_Client_ID(), cost.getAD_Org_ID(), cost.getM_Product_ID(), cost.getM_CostType_ID(), cost.getC_AcctSchema_ID(), ce.getM_CostElement_ID(), cost.getM_AttributeSetInstance_ID());
			if (xCost != null)
				retValue = xCost.getCurrentCostPrice();
		}
		//	Average PO History
		else if (to.equals(TO_AveragePOHistory))
		{
			MCostElement ce = getCostElement(TO_AveragePO);
			if (ce == null)
				throw new CompiereSystemException("CostElement not found: " + TO_AveragePO);
			MCost xCost = MCost.get(getCtx(), cost.getAD_Client_ID(), cost.getAD_Org_ID(), cost.getM_Product_ID(), cost.getM_CostType_ID(), cost.getC_AcctSchema_ID(), ce.getM_CostElement_ID(), cost.getM_AttributeSetInstance_ID());
			if (xCost != null) 
				retValue = xCost.getHistoryAverage();
		}
		
		//	FiFo
		else if (to.equals(TO_FiFo))
		{
			MCostElement ce = getCostElement(TO_FiFo);
			if (ce == null)
				throw new CompiereSystemException("CostElement not found: " + TO_FiFo);
			MCost xCost = MCost.get(getCtx(), cost.getAD_Client_ID(), cost.getAD_Org_ID(), cost.getM_Product_ID(), cost.getM_CostType_ID(), cost.getC_AcctSchema_ID(), ce.getM_CostElement_ID(), cost.getM_AttributeSetInstance_ID());
			if (xCost != null)
				retValue = xCost.getCurrentCostPrice();
		}

		//	Future Std Costs
		else if (to.equals(TO_FutureStandardCost))
			retValue = cost.getFutureCostPrice();
		
		//	Last Inv Price
		else if (to.equals(TO_LastInvoicePrice))
		{
			MCostElement ce = getCostElement(TO_LastInvoicePrice);
			if (ce != null)
			{
				MCost xCost = MCost.get(getCtx(), cost.getAD_Client_ID(), cost.getAD_Org_ID(), cost.getM_Product_ID(), cost.getM_CostType_ID(), cost.getC_AcctSchema_ID(), ce.getM_CostElement_ID(), cost.getM_AttributeSetInstance_ID());
				if (xCost != null)
					retValue = xCost.getCurrentCostPrice();
			}
			if (retValue == null)
			{
				MProduct product = MProduct.get(getCtx(), cost.getM_Product_ID());
				MAcctSchema as = MAcctSchema.get(getCtx(), cost.getC_AcctSchema_ID());
				retValue = MCost.getLastInvoicePrice(product, 
					cost.getM_AttributeSetInstance_ID(), cost.getAD_Org_ID(), as.getC_Currency_ID());				
			}
		}
		
		//	Last PO Price
		else if (to.equals(TO_LastPOPrice))
		{
			MCostElement ce = getCostElement(TO_LastPOPrice);
			if (ce != null)
			{
				MCost xCost = MCost.get(getCtx(), cost.getAD_Client_ID(), cost.getAD_Org_ID(), cost.getM_Product_ID(), cost.getM_CostType_ID(), cost.getC_AcctSchema_ID(), ce.getM_CostElement_ID(), cost.getM_AttributeSetInstance_ID());
				if (xCost != null)
					retValue = xCost.getCurrentCostPrice();
			}
			if (retValue == null)
			{
				MProduct product = MProduct.get(getCtx(), cost.getM_Product_ID());
				MAcctSchema as = MAcctSchema.get(getCtx(), cost.getC_AcctSchema_ID());
				retValue = MCost.getLastPOPrice(product, 
					cost.getM_AttributeSetInstance_ID(), cost.getAD_Org_ID(), as.getC_Currency_ID());				
			}
		}
	
		//	FiFo
		else if (to.equals(TO_LiFo))
		{
			MCostElement ce = getCostElement(TO_LiFo);
			if (ce == null)
				throw new CompiereSystemException("CostElement not found: " + TO_LiFo);
			MCost xCost = MCost.get(getCtx(), cost.getAD_Client_ID(), cost.getAD_Org_ID(), cost.getM_Product_ID(), cost.getM_CostType_ID(), cost.getC_AcctSchema_ID(), ce.getM_CostElement_ID(), cost.getM_AttributeSetInstance_ID());
			if (xCost != null)
				retValue = xCost.getCurrentCostPrice();
		}
		
		//	Old Std Costs
		else if (to.equals(TO_OldStandardCost))
			retValue = getOldCurrentCostPrice(cost);
		
		//	Price List
		else if (to.equals(TO_PriceListLimit))
			retValue = getPrice(cost);
		
		//	Standard Costs
		else if (to.equals(TO_StandardCost))
			retValue = cost.getCurrentCostPrice();
		
		if(retValue == null)
			retValue = Env.ZERO;
		
		return retValue;
	}	//	getCosts
	
	
	/**
	 * 	Get Cost Element
	 *	@param CostingMethod method
	 *	@return costing element or null
	 */
	private MCostElement getCostElement (String CostingMethod)
	{
		MCostElement ce = m_ces.get(CostingMethod);
		if (ce == null)
		{
			ce = MCostElement.getMaterialCostElement(getCtx(), CostingMethod);
			m_ces.put(CostingMethod, ce);
		}
		return ce;
	}	//	getCostElement

	/**
	 * 	Get Old Current Cost Price
	 *	@param cost costs
	 *	@return price if found
	 */
	private BigDecimal getOldCurrentCostPrice(MCost cost)
	{
		BigDecimal retValue = null;
		String sql = "SELECT CostStandard, CurrentCostPrice "
			+ "FROM M_Product_Costing "
			+ "WHERE M_Product_ID=? AND C_AcctSchema_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, cost.getM_Product_ID());
			pstmt.setInt (2, cost.getC_AcctSchema_ID());
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				retValue = rs.getBigDecimal(1);
				if (retValue == null || retValue.signum() == 0)
					retValue = rs.getBigDecimal(2);
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
		return retValue;
	}	//	getOldCurrentCostPrice

	/**
	 * 	Get Price from Price List
	 * 	@param cost cost record
	 *	@return price or null
	 */
	private BigDecimal getPrice (MCost cost)
	{
		BigDecimal retValue = null;
		String sql = "SELECT PriceLimit "
			+ "FROM M_ProductPrice "
			+ "WHERE M_Product_ID=? AND M_PriceList_Version_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, cost.getM_Product_ID());
			pstmt.setInt (2, p_M_PriceList_Version_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				retValue = rs.getBigDecimal(1);
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
		return retValue;
	}	//	getPrice
	
	private boolean createDoc(){

		// Create Header for Cost Update Doc.
		MCostUpdate CostUpdate = new MCostUpdate(getCtx(),0,get_TrxName());
		CostUpdate.setM_Product_Category_ID(p_M_Product_Category_ID);
		CostUpdate.setDateAcct(CostUpdate.getCreated());
		CostUpdate.save(get_TrxName());
		
		// Create Lines for Cost Update Doc
		String sql = "SELECT M_Product_ID FROM M_Product p "
			       + " WHERE p.AD_Client_ID = ? "
			       + " AND EXISTS (SELECT 1 FROM M_Cost c WHERE c.M_Product_ID = p.M_Product_ID"
			                     + " AND c.AD_Client_ID = p.AD_Client_ID"
			                     + " AND c.M_CostElement_ID = ?)";
		if (p_M_Product_Category_ID !=0)
			sql += " AND M_Product_Category_ID=?";

		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, getAD_Client_ID());
			pstmt.setInt (2, m_ce.getM_CostElement_ID());
			
			if (p_M_Product_Category_ID != 0)
				pstmt.setInt (3, p_M_Product_Category_ID);
			
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next())
			{
			    int M_Product_ID = rs.getInt(1);
				MProduct p = MProduct.get(getCtx(), M_Product_ID);
				MCostUpdateLine l = new MCostUpdateLine(getCtx(),CostUpdate,get_TrxName());
				l.setM_Product_ID(M_Product_ID);
				l.setC_UOM_ID(p.getC_UOM_ID());
				l.setProcessed(true);
				l.save(get_TrxName());
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log (Level.SEVERE, sql, e);
			return false;
		}
		return true;	
	}
	
	private boolean isUnprocessedExist()
	{
		boolean retVal = true;
		String sql = " SELECT count(*) FROM M_CostUpdate"
			       + " WHERE (processed <> 'Y' OR posted <> 'Y')"
			       + " AND AD_Client_ID = ? ";
		if (p_M_Product_Category_ID !=0)
			sql += " AND (M_Product_Category_ID = ? OR M_Product_Category_ID IS NULL)";

		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, getAD_Client_ID());
			if (p_M_Product_Category_ID != 0)
				pstmt.setInt (2, p_M_Product_Category_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next())
			    retVal= (rs.getInt(1)==0);

			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log (Level.SEVERE, sql, e);
			return false;
		}
		return retVal;
	}
	
	
}	//	CostUpdate
