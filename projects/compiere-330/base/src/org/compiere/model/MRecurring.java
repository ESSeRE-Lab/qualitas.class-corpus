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

import org.compiere.util.*;

/**
 * 	Recurring Model
 *
 *	@author Jorg Janke
 *	@version $Id: MRecurring.java,v 1.2 2006/07/30 00:51:03 jjanke Exp $
 */
public class MRecurring extends X_C_Recurring
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MRecurring (Ctx ctx, int C_Recurring_ID, Trx trx)
	{
		super (ctx, C_Recurring_ID, trx);
		if (C_Recurring_ID == 0)
		{
		//	setC_Recurring_ID (0);		//	PK
			setDateNextRun (new Timestamp(System.currentTimeMillis()));
			setFrequencyType (FREQUENCYTYPE_Monthly);
			setFrequency(1);
		//	setName (null);
		//	setRecurringType (null);
			setRunsMax (1);
			setRunsRemaining (0);
		}
	}	//	MRecurring

	public MRecurring (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MRecurring

	/**
	 *	String representation
	 * 	@return info
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer ("MRecurring[")
			.append(get_ID()).append("-").append(getName());
		if (getRecurringType().equals(X_C_Recurring.RECURRINGTYPE_Order))
			sb.append(",C_Order_ID=").append(getC_Order_ID());
		else if (getRecurringType().equals(X_C_Recurring.RECURRINGTYPE_Invoice))
			sb.append(",C_Invoice_ID=").append(getC_Invoice_ID());
		else if (getRecurringType().equals(X_C_Recurring.RECURRINGTYPE_Project))
			sb.append(",C_Project_ID=").append(getC_Project_ID());
		else if (getRecurringType().equals(X_C_Recurring.RECURRINGTYPE_GLJournal))
			sb.append(",GL_JournalBatch_ID=").append(getGL_JournalBatch_ID());
		sb.append(",Fequency=").append(getFrequencyType()).append("*").append(getFrequency());
		sb.append("]");
		return sb.toString();
	}	//	toString

	
	/**************************************************************************
	 * 	Execute Run.
	 *	@return clear text info
	 */
	public String executeRun()
	{
		Timestamp dateDoc = getDateNextRun();
		if (!calculateRuns())
			throw new IllegalStateException ("No Runs Left");

		//	log
		MRecurringRun run = new MRecurringRun (getCtx(), this);
		String msg = "@Created@ ";


		//	Copy
		if (getRecurringType().equals(X_C_Recurring.RECURRINGTYPE_Order))
		{
			MOrder from = new MOrder (getCtx(), getC_Order_ID(), get_Trx());
			MOrder order = MOrder.copyFrom (from, dateDoc, 
				from.getC_DocType_ID(), false, false, get_Trx());
			run.setC_Order_ID(order.getC_Order_ID());
			msg += order.getDocumentNo();
		}
		else if (getRecurringType().equals(X_C_Recurring.RECURRINGTYPE_Invoice))
		{
			MInvoice from = new MInvoice (getCtx(), getC_Invoice_ID(), get_Trx());
			MInvoice invoice = MInvoice.copyFrom (from, dateDoc, 
				from.getC_DocType_ID(), false, get_Trx(), false);
			run.setC_Invoice_ID(invoice.getC_Invoice_ID());
			msg += invoice.getDocumentNo();
		}
		else if (getRecurringType().equals(X_C_Recurring.RECURRINGTYPE_Project))
		{
			MProject project = MProject.copyFrom (getCtx(), getC_Project_ID(), dateDoc, get_Trx());
			run.setC_Project_ID(project.getC_Project_ID());
			msg += project.getValue();
		}
		else if (getRecurringType().equals(X_C_Recurring.RECURRINGTYPE_GLJournal))
		{
			MJournalBatch journal = MJournalBatch.copyFrom (getCtx(), getGL_JournalBatch_ID(), dateDoc, get_Trx());
			run.setGL_JournalBatch_ID(journal.getGL_JournalBatch_ID());
			msg += journal.getDocumentNo();
		}
		else
			return "Invalid @RecurringType@ = " + getRecurringType();
		run.save(get_Trx());

		//
		setDateLastRun (run.getUpdated());
		setRunsRemaining (getRunsRemaining()-1);
		setDateNextRun();
		save(get_Trx());
		return msg;
	}	//	execureRun

	/**
	 *	Calculate & set remaining Runs
	 *	@return true if runs left
	 */
	private boolean calculateRuns()
	{
		String sql = "SELECT COUNT(*) FROM C_Recurring_Run WHERE C_Recurring_ID=?";
		int current = DB.getSQLValue(get_Trx(), sql, getC_Recurring_ID());
		int remaining = getRunsMax() - current;
		setRunsRemaining(remaining);
		save();
		return remaining > 0;
	}	//	calculateRuns

	/**
	 *	Calculate next run date
	 */
	private void setDateNextRun()
	{
		if (getFrequency() < 1)
			setFrequency(1);
		int frequency = getFrequency();
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDateNextRun());
		//
		if (getFrequencyType().equals(FREQUENCYTYPE_Daily))
			cal.add(Calendar.DAY_OF_YEAR, frequency);
		else if (getFrequencyType().equals(FREQUENCYTYPE_Weekly))
			cal.add(Calendar.WEEK_OF_YEAR, frequency);
		else if (getFrequencyType().equals(FREQUENCYTYPE_Monthly))
			cal.add(Calendar.MONTH, frequency);
		else if (getFrequencyType().equals(FREQUENCYTYPE_Quarterly))
			cal.add(Calendar.MONTH, 3*frequency);
		Timestamp next = new Timestamp (cal.getTimeInMillis());
		setDateNextRun(next);
	}	//	setDateNextRun

	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	@Override
	protected boolean beforeSave (boolean newRecord)
	{
		String rt = getRecurringType();
		if (rt == null)
		{
			log.saveError("FillMandatory", Msg.getElement(getCtx(), "RecurringType"));
			return false;
		}
		if (rt.equals(X_C_Recurring.RECURRINGTYPE_Order)
			&& getC_Order_ID() == 0)
		{
			log.saveError("FillMandatory", Msg.getElement(getCtx(), "C_Order_ID"));
			return false;
		}
		if (rt.equals(X_C_Recurring.RECURRINGTYPE_Invoice)
			&& getC_Invoice_ID() == 0)
		{
			log.saveError("FillMandatory", Msg.getElement(getCtx(), "C_Invoice_ID"));
			return false;
		}
		if (rt.equals(X_C_Recurring.RECURRINGTYPE_GLJournal)
			&& getGL_JournalBatch_ID() == 0)
		{
			log.saveError("FillMandatory", Msg.getElement(getCtx(), "GL_JournalBatch_ID"));
			return false;
		}
		if (rt.equals(X_C_Recurring.RECURRINGTYPE_Project)
			&& getC_Project_ID() == 0)
		{
			log.saveError("FillMandatory", Msg.getElement(getCtx(), "C_Project_ID"));
			return false;
		}
		return true;
	}	//	beforeSave
	
}	//	MRecurring
