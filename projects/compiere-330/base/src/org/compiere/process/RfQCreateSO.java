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
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.util.*;


/**
 *	Create SO for RfQ.
 *	
 *  @author Jorg Janke
 *  @version $Id: RfQCreateSO.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class RfQCreateSO extends SvrProcess
{
	/**	RfQ 			*/
	private int		p_C_RfQ_ID = 0;
	private int		p_C_DocType_ID = 0;

	/**	100						*/
	private static BigDecimal 	ONEHUNDRED = new BigDecimal (100);

	/**
	 * 	Prepare
	 */
	@Override
	protected void prepare ()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) {
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("C_DocType_ID"))
				p_C_DocType_ID = element.getParameterAsInt();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
		p_C_RfQ_ID = getRecord_ID();
	}	//	prepare

	/**
	 * 	Process.
	 * 	A Sales Order is created for the entered Business Partner.  
	 * 	A sales order line is created for each RfQ line quantity, 
	 * 	where "Offer Quantity" is selected.  
	 * 	If on the RfQ Line Quantity, an offer amount is entered (not 0), 
	 * 	that price is used. 
	 *	If a magin is entered on RfQ Line Quantity, it overwrites the 
	 *	general margin.  The margin is the percentage added to the 
	 *	Best Response Amount.
	 *	@return message
	 */
	@Override
	protected String doIt () throws Exception
	{
		MRfQ rfq = new MRfQ (getCtx(), p_C_RfQ_ID, get_TrxName());
		if (rfq.get_ID() == 0)
			throw new IllegalArgumentException("No RfQ found");
		log.info("doIt - " + rfq);
		
		if (rfq.getC_BPartner_ID() == 0 || rfq.getC_BPartner_Location_ID() == 0)
			throw new Exception ("No Business Partner/Location");
		MBPartner bp = new MBPartner (getCtx(), rfq.getC_BPartner_ID(), get_TrxName());
		
		MOrder order = new MOrder (getCtx(), 0, get_TrxName());
		order.setIsSOTrx(true);
		if (p_C_DocType_ID != 0)
			order.setC_DocTypeTarget_ID(p_C_DocType_ID);
		else
			order.setC_DocTypeTarget_ID();
		order.setBPartner(bp);
		order.setC_BPartner_Location_ID(rfq.getC_BPartner_Location_ID());
		order.setSalesRep_ID(rfq.getSalesRep_ID());
		if (rfq.getDateWorkComplete() != null)
			order.setDatePromised(rfq.getDateWorkComplete());
		order.save();

		MRfQLine[] lines = rfq.getLines();
		for (MRfQLine line : lines) {
			MRfQLineQty[] qtys = line.getQtys();
			for (MRfQLineQty qty : qtys) {
				if (qty.isActive() && qty.isOfferQty())
				{
					MOrderLine ol = new MOrderLine (order);
					ol.setM_Product_ID(line.getM_Product_ID(),
						qty.getC_UOM_ID());
					ol.setDescription(line.getDescription());
					ol.setQty(qty.getQty());
					//
					BigDecimal price = qty.getOfferAmt();
					if (price == null || price.signum() == 0)
					{
						price = qty.getBestResponseAmt();
						if (price == null || price.signum() == 0)
						{
							price = Env.ZERO;
							log.warning(" - BestResponse=0 - " + qty);
						}
						else
						{
							BigDecimal margin = qty.getMargin();
							if (margin == null || margin.signum() == 0)
								margin = rfq.getMargin();
							if (margin != null && margin.signum() != 0)
							{
								margin = margin.add(ONEHUNDRED);
								price = price.multiply(margin)
									.divide(ONEHUNDRED, 2, BigDecimal.ROUND_HALF_UP);
							}
						}
					}	//	price
					ol.setPrice(price);
					ol.save();
				}	//	Offer Qty
			}	//	All Qtys
		}	//	All Lines

		//
		rfq.setC_Order_ID(order.getC_Order_ID());
		rfq.save();
		return order.getDocumentNo();
	}	//	doIt
}
