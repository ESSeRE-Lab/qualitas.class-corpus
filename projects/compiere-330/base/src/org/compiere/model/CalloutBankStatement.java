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
import java.util.logging.*;

import org.compiere.util.*;


/**
 *	Bank Statement Callout
 *
 *  @author Jorg Janke
 *  @version $Id: CalloutBankStatement.java,v 1.3 2006/07/30 00:51:05 jjanke Exp $
 */
public class CalloutBankStatement extends CalloutEngine
{
	/** Logger					*/
	private CLogger		log = CLogger.getCLogger(getClass());

	/**
	 * 	Bank Account Changed.
	 * 	Update Beginning Balance
	 *	@param ctx context
	 *	@param WindowNo window no
	 *	@param mTab tab
	 *	@param mField field
	 *	@param value value
	 *	@return null or error message
	 */
	public String bankAccount (Ctx ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		if (value == null)
			return "";
		int C_BankAccount_ID = ((Integer)value).intValue();
		MBankAccount ba = MBankAccount.get(ctx, C_BankAccount_ID);
		mTab.setValue("BeginningBalance", ba.getCurrentBalance());
		return "";
	}	//	bankAccount

	/**
	 *	BankStmt - Amount.
	 *  Calculate ChargeAmt = StmtAmt - TrxAmt - InterestAmt
	 *    or id Charge is entered - InterestAmt = StmtAmt - TrxAmt - ChargeAmt
	 *	@param ctx context
	 *	@param WindowNo window no
	 *	@param mTab tab
	 *	@param mField field
	 *	@param value value
	 *	@return null or error message
	 */
	public String amount (Ctx ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		if (isCalloutActive())
			return "";
		setCalloutActive(true);

		//  Get Stmt & Trx
		BigDecimal stmt = (BigDecimal)mTab.getValue("StmtAmt");
		if (stmt == null)
			stmt = Env.ZERO;
		BigDecimal p_trx = (BigDecimal)mTab.getValue("TrxAmt");
		if (p_trx == null)
			p_trx = Env.ZERO;
		BigDecimal bd = stmt.subtract(p_trx);

		//  Charge - calculate Interest
		if (mField.getColumnName().equals("ChargeAmt"))
		{
			BigDecimal charge = (BigDecimal)value;
			if (charge == null)
				charge = Env.ZERO;
			bd = bd.subtract(charge);
		//	log.trace(log.l5_DData, "Interest (" + bd + ") = Stmt(" + stmt + ") - Trx(" + p_trx + ") - Charge(" + charge + ")");
			mTab.setValue("InterestAmt", bd);
		}
		//  Calculate Charge
		else
		{
			BigDecimal interest = (BigDecimal)mTab.getValue("InterestAmt");
			if (interest == null)
				interest = Env.ZERO;
			bd = bd.subtract(interest);
		//	log.trace(log.l5_DData, "Charge (" + bd + ") = Stmt(" + stmt + ") - Trx(" + p_trx + ") - Interest(" + interest + ")");
			mTab.setValue("ChargeAmt", bd);
		}
		setCalloutActive(false);
		return "";
	}   //  amount


	/**
	 *	BankStmt - Payment.
	 *  Update Transaction Amount when payment is selected
	 *	@param ctx context
	 *	@param WindowNo window no
	 *	@param mTab tab
	 *	@param mField field
	 *	@param value value
	 *	@return null or error message
	 */
	public String payment (Ctx ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		Integer C_Payment_ID = (Integer)value;
		if (C_Payment_ID == null || C_Payment_ID.intValue() == 0)
			return "";
		//
		BigDecimal stmt = (BigDecimal)mTab.getValue("StmtAmt");
		if (stmt == null)
			stmt = Env.ZERO;

		String sql = "SELECT PayAmt FROM C_Payment_v WHERE C_Payment_ID=?";		//	1
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt(1, C_Payment_ID.intValue());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				BigDecimal bd = rs.getBigDecimal(1);
				mTab.setValue("TrxAmt", bd);
				if (stmt.compareTo(Env.ZERO) == 0)
					mTab.setValue("StmtAmt", bd);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, "BankStmt_Payment", e);
			return e.getLocalizedMessage();
		}
		//  Recalculate Amounts
		amount (ctx, WindowNo, mTab, mField, value);
		return "";
	}	//	payment

	/**
	 * 	Org Changed.
	 * 	Update Bank Account
	 *	@param ctx context
	 *	@param WindowNo window no
	 *	@param mTab tab
	 *	@param mField field
	 *	@param value value
	 *	@return null or error message
	 */
	public String org (Ctx ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		mTab.setValue("BankAccount", null);
		return "";
	}	//	org


}	//	CalloutBankStatement
