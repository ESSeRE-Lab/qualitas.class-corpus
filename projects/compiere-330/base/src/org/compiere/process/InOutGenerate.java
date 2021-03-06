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
import org.compiere.vos.*;

/**
 *	Generate Shipments.
 *	Manual or Automatic
 *	
 *  @author Jorg Janke
 *  @version $Id: InOutGenerate.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class InOutGenerate extends SvrProcess
{
	/**	Manual Selection		*/
	private boolean 	p_Selection = false;
	/** Warehouse				*/
	private int			p_M_Warehouse_ID = 0;
	/** BPartner				*/
	private int			p_C_BPartner_ID = 0;
	/** Promise Date			*/
	private Timestamp	p_DatePromised = null;
	/** Include Orders w. unconfirmed Shipments	*/
	private boolean		p_IsUnconfirmedInOut = false;
	/** DocAction				*/
	private String		p_docAction = DocActionConstants.ACTION_Complete;
	/** Consolidate				*/
	private boolean		p_ConsolidateDocument = true;
	
	/**	The current Shipment	*/
	private MInOut 		m_shipment = null;
	/** Numner of Shipments		*/
	private int			m_created = 0;
	/**	Line Number				*/
	private int			m_line = 0;
	/** Movement Date			*/
	private Timestamp	m_movementDate = null;
	/**	Last BP Location		*/
	private int			m_lastC_BPartner_Location_ID = -1;

	/** The Query sql			*/
	private String 		m_sql = null;

	
	/** Storages temp space				*/
	private HashMap<SParameter,MStorage[]> m_map = new HashMap<SParameter,MStorage[]>();
	/** Last Parameter					*/
	private SParameter		m_lastPP = null;
	/** Last Storage					*/
	private MStorage[]		m_lastStorages = null;

	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) 
		{
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("M_Warehouse_ID"))
				p_M_Warehouse_ID = element.getParameterAsInt();
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = element.getParameterAsInt();
			else if (name.equals("DatePromised"))
				p_DatePromised = (Timestamp)element.getParameter();
			else if (name.equals("Selection"))
				p_Selection = "Y".equals(element.getParameter());
			else if (name.equals("IsUnconfirmedInOut"))
				p_IsUnconfirmedInOut = "Y".equals(element.getParameter());
			else if (name.equals("ConsolidateDocument"))
				p_ConsolidateDocument = "Y".equals(element.getParameter());
			else if (name.equals("DocAction"))
				p_docAction = (String)element.getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			
			//	Login Date
			m_movementDate = new Timestamp(getCtx().getContextAsTime("#Date"));
			//	DocAction check
			if (!DocActionConstants.ACTION_Complete.equals(p_docAction))
				p_docAction = DocActionConstants.ACTION_Prepare;
		}
	}	//	prepare

	/**
	 * 	Generate Shipments
	 *	@return info
	 *	@throws Exception
	 */
	@Override
	protected String doIt () throws Exception
	{
		log.info("Selection=" + p_Selection
			+ ", M_Warehouse_ID=" + p_M_Warehouse_ID 
			+ ", C_BPartner_ID=" + p_C_BPartner_ID 
			+ ", Consolidate=" + p_ConsolidateDocument
			+ ", IsUnconfirmed=" + p_IsUnconfirmedInOut
			+ ", Movement=" + m_movementDate);
		
		if (p_M_Warehouse_ID == 0)
			throw new CompiereUserException("@NotFound@ @M_Warehouse_ID@");
		
		if (p_Selection)	//	VInOutGen
		{
			m_sql = "SELECT * FROM C_Order "
				+ "WHERE IsSelected='Y' AND DocStatus='CO' AND IsSOTrx='Y' AND AD_Client_ID=?";
		}
		else
		{
			m_sql = "SELECT * FROM C_Order o "
				+ "WHERE DocStatus='CO' AND IsSOTrx='Y'"
				//	No Offer,POS
				+ " AND o.C_DocType_ID IN (SELECT C_DocType_ID FROM C_DocType "
					+ "WHERE DocBaseType='SOO' AND DocSubTypeSO NOT IN ('ON','OB','WR'))"
				+ "	AND o.IsDropShip='N'"
				//	No Manual
				+ " AND o.DeliveryRule<>'M'"
				//	Open Order Lines with Warehouse
				+ " AND EXISTS (SELECT * FROM C_OrderLine ol "
					+ "WHERE ol.M_Warehouse_ID=?";					//	#1
			if (p_DatePromised != null)
				m_sql += " AND TRUNC(ol.DatePromised,'DD')<=?";		//	#2
			m_sql += " AND o.C_Order_ID=ol.C_Order_ID AND ol.QtyOrdered<>ol.QtyDelivered)";
			//
			if (p_C_BPartner_ID != 0)
				m_sql += " AND o.C_BPartner_ID=?";					//	#3
		}
		m_sql += " ORDER BY M_Warehouse_ID, PriorityRule, M_Shipper_ID, C_BPartner_ID, C_BPartner_Location_ID, C_Order_ID";
	//	m_sql += " FOR UPDATE";

		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (m_sql, get_TrxName());
			int index = 1;
			if (p_Selection)
				pstmt.setInt(index++, getCtx().getAD_Client_ID());
			else	
			{
				pstmt.setInt(index++, p_M_Warehouse_ID);
				if (p_DatePromised != null)
					pstmt.setTimestamp(index++, p_DatePromised);
				if (p_C_BPartner_ID != 0)
					pstmt.setInt(index++, p_C_BPartner_ID);
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, m_sql, e);
		}
		return generate(pstmt);
	}	//	doIt
	
	/**
	 * 	Generate Shipments
	 * 	@param pstmt Order Query
	 *	@return info
	 */
	private String generate (PreparedStatement pstmt)
	{
		MClient client = MClient.get(getCtx());
		try
		{
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())		//	Order
			{
				MOrder order = new MOrder (getCtx(), rs, get_TrxName());
				//	New Header different Shipper, Shipment Location
				if (!p_ConsolidateDocument 
					|| (m_shipment != null 
					&& (m_shipment.getC_BPartner_Location_ID() != order.getC_BPartner_Location_ID()
						|| m_shipment.getM_Shipper_ID() != order.getM_Shipper_ID() )))
					completeShipment();
				log.fine("check: " + order + " - DeliveryRule=" + order.getDeliveryRule());
				//
				Timestamp minGuaranteeDate = m_movementDate;
				boolean completeOrder = X_C_Order.DELIVERYRULE_CompleteOrder.equals(order.getDeliveryRule());
				//	OrderLine WHERE
				String where = " AND M_Warehouse_ID=" + p_M_Warehouse_ID;
				if (p_DatePromised != null)
					where += " AND (TRUNC(DatePromised,'DD')<=" + DB.TO_DATE(p_DatePromised, true)
						+ " OR DatePromised IS NULL)";		
				//	Exclude Auto Delivery if not Force
				if (!X_C_Order.DELIVERYRULE_Force.equals(order.getDeliveryRule()))
					where += " AND (C_OrderLine.M_Product_ID IS NULL"
						+ " OR EXISTS (SELECT * FROM M_Product p "
						+ "WHERE C_OrderLine.M_Product_ID=p.M_Product_ID"
						+ " AND IsExcludeAutoDelivery='N'))";
				//	Exclude Unconfirmed
				if (!p_IsUnconfirmedInOut)
					where += " AND NOT EXISTS (SELECT * FROM M_InOutLine iol"
							+ " INNER JOIN M_InOut io ON (iol.M_InOut_ID=io.M_InOut_ID) "
								+ "WHERE iol.C_OrderLine_ID=C_OrderLine.C_OrderLine_ID AND io.DocStatus IN ('IP','WC'))";
				//	Deadlock Prevention - Order by M_Product_ID
				MOrderLine[] lines = order.getLines (where, "ORDER BY C_BPartner_Location_ID, M_Product_ID");
				for (MOrderLine line : lines) {
					if (line.getM_Warehouse_ID() != p_M_Warehouse_ID)
						continue;
					log.fine("check: " + line);
					BigDecimal onHand = Env.ZERO;
					BigDecimal toDeliver = line.getQtyOrdered()
								.subtract(line.getQtyDelivered())
								.subtract(line.getQtyAllocated())
								.subtract(line.getQtyDedicated());
					
					if(toDeliver.signum()<=0)
						continue;
					
					MProduct product = line.getProduct();
					//	Nothing to Deliver
					if (product != null && toDeliver.signum() == 0)
						continue;
					
					//	Check / adjust for confirmations
					BigDecimal unconfirmedShippedQty = Env.ZERO;
					if (p_IsUnconfirmedInOut && product != null && toDeliver.signum() != 0)
					{
						String where2 = "EXISTS (SELECT * FROM M_InOut io WHERE io.M_InOut_ID=M_InOutLine.M_InOut_ID AND io.DocStatus IN ('IP','WC'))";
						MInOutLine[] iols = MInOutLine.getOfOrderLine(getCtx(), 
							line.getC_OrderLine_ID(), where2, null);
						for (MInOutLine element : iols)
							unconfirmedShippedQty = unconfirmedShippedQty.add(element.getMovementQty());
						String logInfo = "Unconfirmed Qty=" + unconfirmedShippedQty 
							+ " - ToDeliver=" + toDeliver + "->";					
						toDeliver = toDeliver.subtract(unconfirmedShippedQty);
						logInfo += toDeliver;
						if (toDeliver.signum() < 0)
						{
							toDeliver = Env.ZERO;
							logInfo += " (set to 0)";
						}
						//	Adjust On Hand
						onHand = onHand.subtract(unconfirmedShippedQty);
						log.fine(logInfo);					
					}
					
					//	Comments & lines w/o product & services
					if ((product == null || !product.isStocked())
						&& (line.getQtyOrdered().signum() == 0 	//	comments
							|| toDeliver.signum() != 0))		//	lines w/o product
					{
						if (!X_C_Order.DELIVERYRULE_CompleteOrder
								.equals(order.getDeliveryRule()))	//	printed later
							createLine (order, line, toDeliver, null, false);
						continue;
					}

					//	Stored Product
					MProductCategory pc = MProductCategory.get(order.getCtx(), 
						product.getM_Product_Category_ID());
					String MMPolicy = pc.getMMPolicy();
					if (MMPolicy == null || MMPolicy.length() == 0)
						MMPolicy = client.getMMPolicy();
					//
					Timestamp bpGuaranteeDate = MBPartnerProduct
						.getMinDate(getCtx(), minGuaranteeDate, 
							line.getC_BPartner_ID(), line.getM_Product_ID());
					MStorage[] storages = getStorages(line.getM_Warehouse_ID(), 
						line.getM_Product_ID(), line.getM_AttributeSetInstance_ID(),
						product.getM_AttributeSet_ID(),
						line.getM_AttributeSetInstance_ID()==0, bpGuaranteeDate, 
						X_AD_Client.MMPOLICY_FiFo.equals(MMPolicy)); 
					
					for (MStorage storage : storages) {
						onHand = onHand.add(storage.getQtyOnHand()
											.subtract(storage.getQtyDedicated())
											.subtract(storage.getQtyAllocated()));
					}
					boolean fullLine = onHand.compareTo(toDeliver) >= 0
						|| toDeliver.signum() < 0;
					
					//	Complete Order
					if (completeOrder && !fullLine)
					{
						log.fine("Failed CompleteOrder - OnHand=" + onHand 
							+ " (Unconfirmed=" + unconfirmedShippedQty
							+ "), ToDeliver=" + toDeliver + " - " + line);
						completeOrder = false;
						break;
					}
					//	Complete Line
					else if (fullLine && X_C_Order.DELIVERYRULE_CompleteLine
							.equals(order.getDeliveryRule()))
					{
						log.fine("CompleteLine - OnHand=" + onHand 
							+ " (Unconfirmed=" + unconfirmedShippedQty
							+ ", ToDeliver=" + toDeliver + " - " + line);
						//	
						createLine (order, line, toDeliver, storages, false);
					}
					//	Availability
					else if (X_C_Order.DELIVERYRULE_Availability.equals(order.getDeliveryRule())
						&& (onHand.signum() > 0
							|| toDeliver.signum() < 0))
					{
						BigDecimal deliver = toDeliver;
						if (deliver.compareTo(onHand) > 0)
							deliver = onHand;
						log.fine("Available - OnHand=" + onHand 
							+ " (Unconfirmed=" + unconfirmedShippedQty
							+ "), ToDeliver=" + toDeliver 
							+ ", Delivering=" + deliver + " - " + line);
						//	
						createLine (order, line, deliver, storages, false);
					}
					//	Force
					else if (X_C_Order.DELIVERYRULE_Force.equals(order.getDeliveryRule()))
					{
						BigDecimal deliver = toDeliver;
						log.fine("Force - OnHand=" + onHand 
							+ " (Unconfirmed=" + unconfirmedShippedQty
							+ "), ToDeliver=" + toDeliver 
							+ ", Delivering=" + deliver + " - " + line);
						//	
						createLine (order, line, deliver, storages, true);
					}
					//	Manual
					else if (X_C_Order.DELIVERYRULE_Manual.equals(order.getDeliveryRule()))
						log.fine("Manual - OnHand=" + onHand 
							+ " (Unconfirmed=" + unconfirmedShippedQty
							+ ") - " + line);
					else
						log.fine("Failed: " + order.getDeliveryRule() + " - OnHand=" + onHand 
							+ " (Unconfirmed=" + unconfirmedShippedQty
							+ "), ToDeliver=" + toDeliver + " - " + line);
				}	//	for all order lines
				
				//	Complete Order successful
				if (completeOrder && X_C_Order.DELIVERYRULE_CompleteOrder.equals(order.getDeliveryRule()))
				{
					for (MOrderLine line : lines) {
						if (line.getM_Warehouse_ID() != p_M_Warehouse_ID)
							continue;
						MProduct product = line.getProduct();
						BigDecimal toDeliver = line.getQtyOrdered().subtract(line.getQtyDelivered())
													.subtract(line.getQtyDedicated().subtract(line.getQtyAllocated()));
						//
						MStorage[] storages = null;
						if (product != null && product.isStocked())
						{
							MProductCategory pc = MProductCategory.get(order.getCtx(), 
								product.getM_Product_Category_ID());
							String MMPolicy = pc.getMMPolicy();
							if (MMPolicy == null || MMPolicy.length() == 0)
								MMPolicy = client.getMMPolicy();
							//
							Timestamp bpGuaranteeDate = MBPartnerProduct
								.getMinDate(getCtx(), minGuaranteeDate, 
									line.getC_BPartner_ID(), line.getM_Product_ID());
							storages = getStorages(line.getM_Warehouse_ID(), 
								line.getM_Product_ID(), line.getM_AttributeSetInstance_ID(),
								product.getM_AttributeSet_ID(),
								line.getM_AttributeSetInstance_ID()==0, bpGuaranteeDate, 
								X_AD_Client.MMPOLICY_FiFo.equals(MMPolicy));
						}
						//	
						createLine (order, line, toDeliver, storages, false);
					}
				}
				m_line += 1000;
			}	//	while order
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, m_sql, e);
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
		completeShipment();
		return "@Created@ = " + m_created;
	}	//	generate
	
	
	
	/**************************************************************************
	 * 	Create Line
	 *	@param order order
	 *	@param orderLine line
	 *	@param qty qty
	 *	@param storages storage info
	 *	@param force force delivery
	 */
	private void createLine (MOrder order, MOrderLine orderLine, BigDecimal qty, 
		MStorage[] storages, boolean force)
	{
		//	Complete last Shipment - can have multiple shipments
		if (m_lastC_BPartner_Location_ID != orderLine.getC_BPartner_Location_ID() )
			completeShipment();
		m_lastC_BPartner_Location_ID = orderLine.getC_BPartner_Location_ID();
		//	Create New Shipment
		if (m_shipment == null)
		{
			m_shipment = new MInOut (order, 0, m_movementDate);
			m_shipment.setM_Warehouse_ID(orderLine.getM_Warehouse_ID());	//	sets Org too
			if (order.getC_BPartner_ID() != orderLine.getC_BPartner_ID())
				m_shipment.setC_BPartner_ID(orderLine.getC_BPartner_ID());
			if (order.getC_BPartner_Location_ID() != orderLine.getC_BPartner_Location_ID())
				m_shipment.setC_BPartner_Location_ID(orderLine.getC_BPartner_Location_ID());
			if (!m_shipment.save())
				throw new IllegalStateException("Could not create Shipment");
		}
		//	Non Inventory Lines
		if (storages == null)
		{
			MInOutLine line = new MInOutLine (m_shipment);
			line.setOrderLine(orderLine, 0, Env.ZERO);
			line.setQty(qty);	//	Correct UOM
			if (orderLine.getQtyEntered().compareTo(orderLine.getQtyOrdered()) != 0)
				line.setQtyEntered(qty
					.multiply(orderLine.getQtyEntered())
					.divide(orderLine.getQtyOrdered(), 12, BigDecimal.ROUND_HALF_UP));
			line.setLine(m_line + orderLine.getLine());
			if (!line.save())
				throw new IllegalStateException("Could not create Shipment Line");
			log.fine(line.toString());
			return;
		}
		
		//	Product
		MProduct product = orderLine.getProduct();
		boolean linePerASI = false;
		if (product.getM_AttributeSet_ID() != 0)
		{
			MAttributeSet mas = MAttributeSet.get(getCtx(), product.getM_AttributeSet_ID());
			linePerASI = mas.isInstanceAttribute();
		}
		
		//	Inventory Lines
		ArrayList<MInOutLine> list = new ArrayList<MInOutLine>();
		BigDecimal toDeliver = qty;
		for (MStorage storage : storages) {
			BigDecimal deliver = toDeliver;
			BigDecimal qtyAvailable = storage.getQtyOnHand().subtract(
					storage.getQtyDedicated()).subtract(
					storage.getQtyAllocated());
			if(qtyAvailable.compareTo(Env.ZERO) <= 0)
				continue;

			//	Not enough On Hand
			if (deliver.compareTo(qtyAvailable) > 0) 
			{
					deliver = qtyAvailable;
			}
			
			if (deliver.signum() == 0 || (storage.getQtyOnHand().signum() <= 0 ))	//	zero deliver
				continue;
			
			int M_Locator_ID = storage.getM_Locator_ID();
			//
			MInOutLine line = null;
			if (!linePerASI)	//	find line with Locator
			{
				for (int ll = 0; ll < list.size(); ll++)
				{
					MInOutLine test = list.get(ll);
					if (test.getM_Locator_ID() == M_Locator_ID)
					{
						line = test;
						break;
					}
				}
			}
			if (line == null)	//	new line
			{
				line = new MInOutLine (m_shipment);
				line.setOrderLine(orderLine, M_Locator_ID, order.isSOTrx() ? deliver : Env.ZERO);
				line.setQty(deliver);
				list.add(line);
			}
			else				//	existing line
				line.setQty(line.getMovementQty().add(deliver));
			if (orderLine.getQtyEntered().compareTo(orderLine.getQtyOrdered()) != 0)
				line.setQtyEntered(line.getMovementQty().multiply(orderLine.getQtyEntered())
					.divide(orderLine.getQtyOrdered(), 12, BigDecimal.ROUND_HALF_UP));
			line.setLine(m_line + orderLine.getLine());
			if (linePerASI)
				line.setM_AttributeSetInstance_ID(storage.getM_AttributeSetInstance_ID());
			if (!line.save())
				throw new IllegalStateException("Could not create Shipment Line");
			log.fine("ToDeliver=" + qty + "/" + deliver + " - " + line);
			toDeliver = toDeliver.subtract(deliver);
			//	Temp adjustment
			storage.setQtyOnHand(storage.getQtyOnHand().subtract(deliver));
			//
			if (toDeliver.signum() == 0)
				break;
		}		

		// Force remaining quantity to negative for Delivery Rule of Force
		if (force && toDeliver.signum() != 0)
		{
			BigDecimal deliver = toDeliver;
			int M_Locator_ID = storages[0].getM_Locator_ID();
			if (M_Locator_ID == 0)		//	Get default Location
			{
				int M_Warehouse_ID = orderLine.getM_Warehouse_ID();
				M_Locator_ID = MProductLocator.getFirstM_Locator_ID (product, M_Warehouse_ID);
				if (M_Locator_ID == 0)
				{
					MWarehouse wh = MWarehouse.get (getCtx(), M_Warehouse_ID);
					M_Locator_ID = wh.getDefaultM_Locator_ID();
				}
			}
			//
			MInOutLine line = null;
			if (!linePerASI)	//	find line with Locator
			{
				for (int ll = 0; ll < list.size(); ll++)
				{
					MInOutLine test = list.get(ll);
					if (test.getM_Locator_ID() == M_Locator_ID)
					{
						line = test;
						break;
					}
				}
			}
			if (line == null)	//	new line
			{
				line = new MInOutLine (m_shipment);
				line.setOrderLine(orderLine, M_Locator_ID, order.isSOTrx() ? deliver : Env.ZERO);
				line.setQty(deliver);
				list.add(line);
			}
			else				//	existing line
				line.setQty(line.getMovementQty().add(deliver));
			if (orderLine.getQtyEntered().compareTo(orderLine.getQtyOrdered()) != 0)
				line.setQtyEntered(line.getMovementQty().multiply(orderLine.getQtyEntered())
					.divide(orderLine.getQtyOrdered(), 12, BigDecimal.ROUND_HALF_UP));
			line.setLine(m_line+orderLine.getLine());
			if (!line.save())
				throw new IllegalStateException("Could not create Shipment Line");

		}

		if (toDeliver.signum() != 0)
			throw new IllegalStateException("Not All Delivered - Remainder=" + toDeliver);
	}	//	createLine

	
	/**
	 * 	Get Storages
	 *	@param M_Warehouse_ID
	 *	@param M_Product_ID
	 *	@param M_AttributeSetInstance_ID
	 *	@param M_AttributeSet_ID
	 *	@param allAttributeInstances
	 *	@param minGuaranteeDate
	 *	@param FiFo
	 *	@return storages
	 */
	private MStorage[] getStorages(int M_Warehouse_ID, 
		int M_Product_ID, int M_AttributeSetInstance_ID, int M_AttributeSet_ID,
		boolean allAttributeInstances, Timestamp minGuaranteeDate,
		boolean FiFo)
	{
		m_lastPP = new SParameter(M_Warehouse_ID, 
			M_Product_ID, M_AttributeSetInstance_ID, M_AttributeSet_ID,
			allAttributeInstances, minGuaranteeDate, FiFo);
		//
		m_lastStorages = m_map.get(m_lastPP); 
		
		if (m_lastStorages == null)
		{
			m_lastStorages = MStorage.getWarehouse(getCtx(), 
				M_Warehouse_ID, M_Product_ID, M_AttributeSetInstance_ID,
				M_AttributeSet_ID, allAttributeInstances, minGuaranteeDate, 
				FiFo, get_TrxName());
			m_map.put(m_lastPP, m_lastStorages);
		}
		return m_lastStorages;
	}	//	getStorages
	
	
	/**
	 * 	Complete Shipment
	 */
	private void completeShipment()
	{
		if (m_shipment != null)
		{
			//	Fails if there is a confirmation
			boolean processOK = m_shipment.processIt(p_docAction);
			m_shipment.save();
			//
			if (processOK)
				addLog(m_shipment.getM_InOut_ID(), m_shipment.getMovementDate(), null, m_shipment.getDocumentNo());
			else
			{
				log.warning("Failed: " + m_shipment);
				String msg = Msg.getMsg(getCtx(), "Error") + " " + m_shipment.getDocumentNo();
				addLog(m_shipment.getM_InOut_ID(), m_shipment.getMovementDate(), null, msg);
			}
			m_created++;
			m_map = new HashMap<SParameter,MStorage[]>();
			if (m_lastPP != null && m_lastStorages != null)
				m_map.put(m_lastPP, m_lastStorages);
		}
		m_shipment = null;
		m_line = 0;
	}	//	completeOrder
	
	/**
	 * 	InOutGenerate Parameter
	 */
	static class SParameter
	{
		/**
		 * 	Parameter
		 *	@param p_Warehouse_ID warehouse
		 *	@param p_Product_ID 
		 *	@param p_AttributeSetInstance_ID 
		 *	@param p_AttributeSet_ID
		 *	@param p_allAttributeInstances 
		 *	@param p_minGuaranteeDate
		 *	@param p_FiFo
		 */
		protected SParameter (int p_Warehouse_ID, 
			int p_Product_ID, int p_AttributeSetInstance_ID, int p_AttributeSet_ID,
			boolean p_allAttributeInstances, Timestamp p_minGuaranteeDate,
			boolean p_FiFo)
		{
			this.M_Warehouse_ID = p_Warehouse_ID;
			this.M_Product_ID = p_Product_ID;
			this.M_AttributeSetInstance_ID = p_AttributeSetInstance_ID; 
			this.M_AttributeSet_ID = p_AttributeSet_ID;
			this.allAttributeInstances = p_allAttributeInstances;
			this.minGuaranteeDate = p_minGuaranteeDate;
			this.FiFo = p_FiFo;	
		}
		/** Warehouse		*/
		public int M_Warehouse_ID;
		/** Product			*/
		public int M_Product_ID;
		/** ASI				*/
		public int M_AttributeSetInstance_ID;
		/** AS				*/
		public int M_AttributeSet_ID;
		/** All instances	*/
		public boolean allAttributeInstances;
		/** Mon Guarantee Date	*/
		public Timestamp minGuaranteeDate;
		/** FiFo			*/
		public boolean FiFo;

		/**
		 * 	Equals
		 *	@param obj
		 *	@return true if equal
		 */
		@Override
		public boolean equals (Object obj)
		{
			if (obj != null && obj instanceof SParameter)
			{
				SParameter cmp = (SParameter)obj;
				boolean eq = cmp.M_Warehouse_ID == M_Warehouse_ID
					&& cmp.M_Product_ID == M_Product_ID
					&& cmp.M_AttributeSetInstance_ID == M_AttributeSetInstance_ID
					&& cmp.M_AttributeSet_ID == M_AttributeSet_ID
					&& cmp.allAttributeInstances == allAttributeInstances
					&& cmp.FiFo == FiFo;
				if (eq)
				{
					if (cmp.minGuaranteeDate == null && minGuaranteeDate == null)
						;
					else if (cmp.minGuaranteeDate != null && minGuaranteeDate != null
						&& cmp.minGuaranteeDate.equals(minGuaranteeDate))
						;
					else
						eq = false;
				}
				return eq;
			}
			return false;
		}	//	equals
		
		/**
		 * 	hashCode
		 *	@return hash code
		 */
		@Override
		public int hashCode ()
		{
			long hash = M_Warehouse_ID
				+ (M_Product_ID * 2)
				+ (M_AttributeSetInstance_ID * 3)
				+ (M_AttributeSet_ID * 4);

			if (allAttributeInstances)
				hash *= -1;
			if (FiFo)	
				hash *= -2;
			if (hash < 0)
				hash = -hash + 7;
			while (hash > Integer.MAX_VALUE)
				hash -= Integer.MAX_VALUE;
			//
			if (minGuaranteeDate != null)
			{
				hash += minGuaranteeDate.hashCode();
				while (hash > Integer.MAX_VALUE)
					hash -= Integer.MAX_VALUE;
			}
				
			return (int)hash;
		}	//	hashCode
		
	}	//	Parameter
	
}	//	InOutGenerate
