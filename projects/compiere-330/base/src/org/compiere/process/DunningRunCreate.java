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
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.util.*;


/**
 *	Create Dunning Run Entries/Lines
 *	
 *  @author Jorg Janke
 *  @version $Id: DunningRunCreate.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class DunningRunCreate extends SvrProcess
{
	private boolean 	p_IncludeInDispute = false;
	private boolean		p_OnlySOTrx = false;
	private boolean		p_IsAllCurrencies = false;
	private int			p_SalesRep_ID = 0;
	private int			p_C_Currency_ID = 0;
	private int			p_C_BPartner_ID = 0;
	private int			p_C_BP_Group_ID = 0;
	private int			p_C_DunningRun_ID = 0;
	
	private MDunningRun m_run = null;
	private MDunningLevel m_level = null;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) {
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("IncludeInDispute"))
				p_IncludeInDispute = "Y".equals(element.getParameter());
			else if (name.equals("OnlySOTrx"))
				p_OnlySOTrx = "Y".equals(element.getParameter());
			else if (name.equals("IsAllCurrencies"))
				p_IsAllCurrencies = "Y".equals(element.getParameter());
			else if (name.equals("SalesRep_ID"))
				p_SalesRep_ID = element.getParameterAsInt();
			else if (name.equals("C_Currency_ID"))
				p_C_Currency_ID = element.getParameterAsInt();
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = element.getParameterAsInt();
			else if (name.equals("C_BP_Group_ID"))
				p_C_BP_Group_ID = element.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		p_C_DunningRun_ID = getRecord_ID();
	}	//	prepare
	
	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt () throws Exception
	{
		log.info("C_DunningRun_ID=" + p_C_DunningRun_ID
			+ ", Dispute=" + p_IncludeInDispute
			+ ", C_BP_Group_ID=" + p_C_BP_Group_ID
			+ ", C_BPartner_ID=" + p_C_BPartner_ID);
		m_run = new MDunningRun (getCtx(),p_C_DunningRun_ID, get_TrxName());
		if (m_run.get_ID() == 0)
			throw new IllegalArgumentException ("Not found MDunningRun");
		if (!m_run.deleteEntries(true))
			throw new IllegalArgumentException ("Cannot delete existing entries");
		if (p_SalesRep_ID == 0)
			throw new IllegalArgumentException ("No SalesRep");
		if (p_C_Currency_ID == 0)
			throw new IllegalArgumentException ("No Currency");
		
		// Pickup the Runlevel
		m_level = m_run.getLevel ();
		
		addInvoices();
		addPayments();
		
		// If the level should charge a fee do it now...
		if (m_level.isChargeFee()) 
			addFees();
		if (m_level.isChargeInterest()) 
			addFees();
		
		// we need to check whether this is a statement or not and some other rules
		checkDunningEntry();
		
		int entries = 0;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement ("SELECT count(*) FROM C_DunningRunEntry WHERE C_DunningRun_ID=?", get_TrxName());
			pstmt.setInt (1, m_run.get_ID ());
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				entries = rs.getInt (1);
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "countResults", e);
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

		
		return "@C_DunningRunEntry_ID@ #" + entries;
	}	//	doIt

	
	/**************************************************************************
	 * 	Add Invoices to Run
	 *	@return no of invoices
	 */
	private int addInvoices()
	{
		int count = 0;
		String sql = "SELECT i.C_Invoice_ID, i.C_Currency_ID,"
			+ " i.GrandTotal*i.MultiplierAP,"
			+ " invoiceOpen(i.C_Invoice_ID,i.C_InvoicePaySchedule_ID)*MultiplierAP,"
			+ " COALESCE(daysBetween(?,ips.DueDate),paymentTermDueDays(i.C_PaymentTerm_ID,i.DateInvoiced,?))," // ##1/2
			+ " i.IsInDispute, i.C_BPartner_ID "
			+ "FROM C_Invoice_v i "
			+ " LEFT OUTER JOIN C_InvoicePaySchedule ips ON (i.C_InvoicePaySchedule_ID=ips.C_InvoicePaySchedule_ID) "
			+ "WHERE i.IsPaid='N' AND i.AD_Client_ID=?"				//	##3
			+ " AND i.DocStatus IN ('CO','CL')"
		//  Invoice Collection Status Collection Agency, Uncollectable, Legal will not been dunned any longer as per Def. YS + KP 12/02/06
			+ " AND (NOT i.InvoiceCollectionType IN ('" + X_C_Invoice.INVOICECOLLECTIONTYPE_CollectionAgency + "', "
				+ "'" + X_C_Invoice.INVOICECOLLECTIONTYPE_LegalProcedure + "', '" + X_C_Invoice.INVOICECOLLECTIONTYPE_Uncollectable + "')"
				+ " OR InvoiceCollectionType IS NULL)"
		//  Do not show future docs...
			+ " AND DateInvoiced<=?" // ##4
		//	Only BP(Group) with Dunning defined
			+ " AND EXISTS (SELECT * FROM C_DunningLevel dl "
				+ "WHERE dl.C_DunningLevel_ID=?"	//	//	##5
				+ " AND dl.C_Dunning_ID IN "
					+ "(SELECT COALESCE(bp.C_Dunning_ID, bpg.C_Dunning_ID) "
					+ "FROM C_BPartner bp"
					+ " INNER JOIN C_BP_Group bpg ON (bp.C_BP_Group_ID=bpg.C_BP_Group_ID) "
					+ "WHERE i.C_BPartner_ID=bp.C_BPartner_ID))";
		// for specific Business Partner
		if (p_C_BPartner_ID != 0)
			sql += " AND i.C_BPartner_ID=?";	//	##6
		// or a specific group
		else if (p_C_BP_Group_ID != 0)
			sql += " AND EXISTS (SELECT * FROM C_BPartner bp "
				+ "WHERE i.C_BPartner_ID=bp.C_BPartner_ID AND bp.C_BP_Group_ID=?)";	//	##6
		// Only Sales Trx
		if (p_OnlySOTrx)
			sql += " AND i.IsSOTrx='Y'";
		// Only single currency
		if (!p_IsAllCurrencies) 
			sql += " AND i.C_Currency_ID=" + p_C_Currency_ID;
	//	log.info(sql);
		
		String sql2=null;
		
		// if sequentially we must check for other levels with smaller days for
		// which this invoice is not yet included!
		if (m_level.getParent ().isCreateLevelsSequentially ()) {
			// Build a list of all topmost Dunning Levels
			MDunningLevel[] previousLevels = m_level.getPreviousLevels();
			if (previousLevels!=null && previousLevels.length>0) {
				String sqlAppend = "";
				for (MDunningLevel element : previousLevels) {
					sqlAppend += " AND i.C_Invoice_ID IN (SELECT C_Invoice_ID FROM C_DunningRunLine WHERE " +
					"C_DunningRunEntry_ID IN (SELECT C_DunningRunEntry_ID FROM C_DunningRunEntry WHERE " +
					"C_DunningRun_ID IN (SELECT C_DunningRun_ID FROM C_DunningRun WHERE " +
					"C_DunningLevel_ID=" + element.get_ID () + ")) AND Processed<>'N')";
				}
				sql += sqlAppend;
			}
		}
		// ensure that we do only dunn what's not yet dunned, so we lookup the max of last Dunn Date which was processed
		sql2 = "SELECT COUNT(*), COALESCE(DAYSBETWEEN(MAX(dr2.DunningDate), MAX(dr.DunningDate)),0)"		
			+ "FROM C_DunningRun dr2, C_DunningRun dr"
			+ " INNER JOIN C_DunningRunEntry dre ON (dr.C_DunningRun_ID=dre.C_DunningRun_ID)"
			+ " INNER JOIN C_DunningRunLine drl ON (dre.C_DunningRunEntry_ID=drl.C_DunningRunEntry_ID) "
			+ "WHERE drl.Processed='Y' AND dr2.C_DunningRun_ID=? AND drl.C_Invoice_ID=?"; // ##1 ##2
		
		BigDecimal DaysAfterDue = m_run.getLevel().getDaysAfterDue();
		int DaysBetweenDunning = m_run.getLevel().getDaysBetweenDunning();
		
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			pstmt.setTimestamp(1, m_run.getDunningDate());
			pstmt.setTimestamp(2, m_run.getDunningDate());
			pstmt.setInt (3, m_run.getAD_Client_ID());
			pstmt.setTimestamp(4, m_run.getDunningDate ());
			pstmt.setInt(5, m_run.getC_DunningLevel_ID());
			if (p_C_BPartner_ID != 0)
				pstmt.setInt (6, p_C_BPartner_ID);
			else if (p_C_BP_Group_ID != 0)
				pstmt.setInt (6, p_C_BP_Group_ID);
			//
			pstmt2 = DB.prepareStatement (sql2, get_TrxName());
			//
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				int C_Invoice_ID = rs.getInt(1);
				int C_Currency_ID = rs.getInt(2);
				BigDecimal GrandTotal = rs.getBigDecimal(3);
				BigDecimal Open = rs.getBigDecimal(4);
				int DaysDue = rs.getInt(5);
				boolean IsInDispute = "Y".equals(rs.getString(6));
				int C_BPartner_ID = rs.getInt(7);
				//
				// Check for Dispute
				if (!p_IncludeInDispute && IsInDispute)
					continue;
				// Check the day again based on rulesets
				if (DaysDue < DaysAfterDue.intValue() && !m_level.isShowAllDue ())
					continue;
				// Check for an open amount
				if (Env.ZERO.compareTo(Open) == 0)
					continue;
				//
				int TimesDunned = 0;
				int DaysAfterLast = 0;
				//	SubQuery
				pstmt2.setInt (1, m_run.get_ID ());
				pstmt2.setInt (2, C_Invoice_ID);
				ResultSet rs2 = pstmt2.executeQuery ();
				if (rs2.next())
				{
					TimesDunned = rs2.getInt(1);
					DaysAfterLast = rs2.getInt(2);
				}
				rs2.close();
				//	SubQuery
				
				// Ensure that Daysbetween Dunning is enforced
				// Ensure Not ShowAllDue and Not ShowNotDue is selected
				// PROBLEM: If you have ShowAll activated then DaysBetweenDunning is not working, because we don't know whether
				//          there is something which we really must Dunn.
				if (DaysBetweenDunning != 0 && DaysAfterLast < DaysBetweenDunning && !m_level.isShowAllDue () && !m_level.isShowNotDue ())
					continue;
				
				// We don't want to show non due documents
				if (DaysDue<0 && !m_level.isShowNotDue ())
					continue;
							
				// We will minus the timesDunned if this is the DaysBetweenDunning is not fullfilled.
				// Remember in checkup later we must reset them!
				// See also checkDunningEntry()
				if (DaysAfterLast < DaysBetweenDunning)
					TimesDunned = TimesDunned*-1;
				//
				createInvoiceLine (C_Invoice_ID, C_Currency_ID, GrandTotal, Open,
					DaysDue, IsInDispute, C_BPartner_ID, 
					TimesDunned, DaysAfterLast);
				count++;
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
			pstmt2.close();
			pstmt2 = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "addInvoices", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			if (pstmt2 != null)
				pstmt2.close ();
			pstmt = null;
			pstmt2 = null;
		}
		catch (Exception e)
		{
			pstmt = null;
			pstmt2 = null;
		}
		return count;
	}	//	addInvoices

	/**
	 * 	Create Invoice Line
	 *	@param C_Invoice_ID invoice
	 *	@param C_Currency_ID currency
	 *	@param GrandTotal total
	 *	@param Open open amount
	 *	@param DaysDue days due
	 *	@param IsInDispute in dispute
	 *	@param C_BPartner_ID bp
	 *	@param TimesDunned nuber of dunnings
	 *	@param DaysAfterLast days after last dunning
	 */
	private void createInvoiceLine (int C_Invoice_ID, int C_Currency_ID, 
		BigDecimal GrandTotal, BigDecimal Open, 
		int DaysDue, boolean IsInDispute, 
		int C_BPartner_ID, int TimesDunned, int DaysAfterLast)
	{
		MDunningRunEntry entry = m_run.getEntry (C_BPartner_ID, p_C_Currency_ID, p_SalesRep_ID);
		if (entry.get_ID() == 0)
			if (!entry.save())
				throw new IllegalStateException("Cannot save MDunningRunEntry");
		//
		MDunningRunLine line = new MDunningRunLine (entry);
		line.setInvoice(C_Invoice_ID, C_Currency_ID, GrandTotal, Open, 
			new BigDecimal(0), DaysDue, IsInDispute, TimesDunned, 
			DaysAfterLast);
		if (!line.save())
			throw new IllegalStateException("Cannot save MDunningRunLine");
	}	//	createInvoiceLine

	
	/**************************************************************************
	 * 	Add Payments to Run
	 *	@return no of payments
	 */
	private int addPayments()
	{
		String sql = "SELECT C_Payment_ID, C_Currency_ID, PayAmt,"
			+ " paymentAvailable(C_Payment_ID), C_BPartner_ID "
			+ "FROM C_Payment_v p "
			+ "WHERE AD_Client_ID=?"			//	##1
			+ " AND IsAllocated='N' AND C_BPartner_ID IS NOT NULL"
			+ " AND C_Charge_ID IS NULL"
			+ " AND DocStatus IN ('CO','CL')"
		//	Only BP with Dunning defined
			+ " AND EXISTS (SELECT * FROM C_BPartner bp "
				+ "WHERE p.C_BPartner_ID=bp.C_BPartner_ID"
				+ " AND bp.C_Dunning_ID=(SELECT C_Dunning_ID FROM C_DunningLevel WHERE C_DunningLevel_ID=?))";	// ##2
		if (p_C_BPartner_ID != 0)
			sql += " AND C_BPartner_ID=?";		//	##3
		else if (p_C_BP_Group_ID != 0)
			sql += " AND EXISTS (SELECT * FROM C_BPartner bp "
				+ "WHERE p.C_BPartner_ID=bp.C_BPartner_ID AND bp.C_BP_Group_ID=?)";	//	##3
		// If it is not a statement we will add lines only if InvoiceLines exists,
		// because we do not want to dunn for money we owe the customer!
		if (!m_level.getDaysAfterDue ().equals (new BigDecimal(-9999)))
			sql += " AND C_BPartner_ID IN (SELECT C_BPartner_ID FROM C_DunningRunEntry WHERE C_DunningRun_ID=" + m_run.get_ID () + ")";
		// show only receipts / if only Sales
		if (p_OnlySOTrx)
			sql += " AND IsReceipt='Y'";
		
		int count = 0;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			pstmt.setInt (1, getAD_Client_ID());
			pstmt.setInt (2, m_run.getC_DunningLevel_ID());
			if (p_C_BPartner_ID != 0)
				pstmt.setInt (3, p_C_BPartner_ID);
			else if (p_C_BP_Group_ID != 0)
				pstmt.setInt (3, p_C_BP_Group_ID);

			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				int C_Payment_ID = rs.getInt(1);
				int C_Currency_ID = rs.getInt(2);
				BigDecimal PayAmt = rs.getBigDecimal(3).negate();
				BigDecimal OpenAmt = rs.getBigDecimal(4).negate();
				int C_BPartner_ID = rs.getInt(5);
				// checkup the amount
				if (Env.ZERO.compareTo(OpenAmt) == 0)
					continue;
				//
				createPaymentLine (C_Payment_ID, C_Currency_ID, PayAmt, OpenAmt,
					C_BPartner_ID);
				count++;
			}
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
		return count;
	}	//	addPayments

	/**
	 * 	Create Payment Line
	 *	@param C_Payment_ID payment
	 *	@param C_Currency_ID currency
	 *	@param PayAmt amount
	 *	@param OpenAmt open
	 *	@param C_BPartner_ID bp
	 */
	private void createPaymentLine (int C_Payment_ID, int C_Currency_ID, 
		BigDecimal PayAmt, BigDecimal OpenAmt, int C_BPartner_ID)
	{
		MDunningRunEntry entry = m_run.getEntry (C_BPartner_ID, p_C_Currency_ID, p_SalesRep_ID);
		if (entry.get_ID() == 0)
			if (!entry.save())
				throw new IllegalStateException("Cannot save MDunningRunEntry");
		//
		MDunningRunLine line = new MDunningRunLine (entry);
		line.setPayment(C_Payment_ID, C_Currency_ID, PayAmt, OpenAmt);
		if (!line.save())
			throw new IllegalStateException("Cannot save MDunningRunLine");
	}	//	createPaymentLine

	/**
	 * 	Add Fees for every line
	 */
	private void addFees()
	{
		// Only add a fee if it contains InvoiceLines and is not a statement
		// TODO: Assumes Statement = -9999 and 
		boolean onlyInvoices = m_level.getDaysAfterDue().equals(new BigDecimal(-9999));
		MDunningRunEntry [] entries = m_run.getEntries (true, onlyInvoices);
		if (entries != null && entries.length > 0) 
		{
			for (MDunningRunEntry element : entries) {
				MDunningRunLine line = new MDunningRunLine (element);
				line.setFee (p_C_Currency_ID, m_level.getFeeAmt ());
				if (!line.save())
					throw new IllegalStateException("Cannot save MDunningRunLine");
				element.setQty (element.getQty ().subtract (new BigDecimal(1)));
			}
		}
	}	//	addFees
	
	/**
	 * 	Check the dunning run
	 *  1) Check for following Rule: ShowAll should produce only a record if at least one new line is found
	 */
	private void checkDunningEntry() 
	{
		// Check rule 1)
		if (m_level.isShowAllDue ()) {
			MDunningRunEntry [] entries = m_run.getEntries (true);
			if (entries != null && entries.length > 0) 
			{
				for (MDunningRunEntry element : entries) {
					// We start with saying we delete this entry as long as we don't find something new
					boolean entryDelete = true;
					MDunningRunLine [] lines = element.getLines (true);
					for (int j = 0; j < lines.length; j++)
					{
						if (lines[j].getTimesDunned() < 0) 
						{
							// We clean up the *-1 from line 255
							lines[j].setTimesDunned (lines[j].getTimesDunned()*-1);
							if (!lines[j].save())
								throw new IllegalStateException("Cannot save MDunningRunLine");
						} 
						else 
						{
							// We found something new, so we would not save anything...
							entryDelete = false;
						}
					}
					if(entryDelete)
						element.delete (false);
				}
			}
		}
	}	//	checkDunningEntry
	
}	//	DunningRunCreate
