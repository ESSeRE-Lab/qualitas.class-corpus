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

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.api.*;
import org.compiere.framework.*;
import org.compiere.process.*;
import org.compiere.util.*;
import org.compiere.vos.*;

/**
 *  Payment Allocation Model.
 * 	Allocation Trigger update C_BPartner
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: MAllocationHdr.java,v 1.3 2006/07/30 00:51:03 jjanke Exp $
 */
public final class MAllocationHdr extends X_C_AllocationHdr implements DocAction
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 	Get Allocations of Payment
	 *	@param ctx context
	 *	@param C_Payment_ID payment
	 *	@return allocations of payment
	 *	@param trx transaction
	 */
	public static MAllocationHdr[] getOfPayment (Ctx ctx, int C_Payment_ID, Trx trx)
	{
		String sql = "SELECT * FROM C_AllocationHdr h "
			+ "WHERE IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM C_AllocationLine l "
				+ "WHERE h.C_AllocationHdr_ID=l.C_AllocationHdr_ID AND l.C_Payment_ID=?)";
		ArrayList<MAllocationHdr> list = new ArrayList<MAllocationHdr>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trx);
			pstmt.setInt(1, C_Payment_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add (new MAllocationHdr(ctx, rs, trx));
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
		MAllocationHdr[] retValue = new MAllocationHdr[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getOfPayment

	/**
	 * 	Get Allocations of Invoice
	 *	@param ctx context
	 *	@param C_Invoice_ID payment
	 *	@return allocations of payment
	 *	@param trx transaction
	 */
	public static MAllocationHdr[] getOfInvoice (Ctx ctx, int C_Invoice_ID, Trx trx)
	{
		String sql = "SELECT * FROM C_AllocationHdr h "
			+ "WHERE IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM C_AllocationLine l "
				+ "WHERE h.C_AllocationHdr_ID=l.C_AllocationHdr_ID AND l.C_Invoice_ID=?)";
		ArrayList<MAllocationHdr> list = new ArrayList<MAllocationHdr>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trx);
			pstmt.setInt(1, C_Invoice_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add (new MAllocationHdr(ctx, rs, trx));
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
		MAllocationHdr[] retValue = new MAllocationHdr[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getOfInvoice

	/**	Logger						*/
	private static CLogger s_log = CLogger.getCLogger(MAllocationHdr.class);


	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param C_AllocationHdr_ID id
	 *	@param trx transaction
	 */
	public MAllocationHdr (Ctx ctx, int C_AllocationHdr_ID, Trx trx)
	{
		super (ctx, C_AllocationHdr_ID, trx);
		if (C_AllocationHdr_ID == 0)
		{
		//	setDocumentNo (null);
			setDateTrx (new Timestamp(System.currentTimeMillis()));
			setDateAcct (getDateTrx());
			setDocAction (DOCACTION_Complete);	// CO
			setDocStatus (DOCSTATUS_Drafted);	// DR
		//	setC_Currency_ID (0);
			setApprovalAmt (Env.ZERO);
			setIsApproved (false);
			setIsManual (false);
			//
			setPosted (false);
			setProcessed (false);
			setProcessing(false);
		}
	}	//	MAllocation

	/**
	 * 	Mandatory New Constructor
	 *	@param ctx context
	 *	@param IsManual manual p_trx
	 *	@param DateTrx date (if null today)
	 *	@param C_Currency_ID currency
	 *	@param description description
	 *	@param trx transaction
	 */
	public MAllocationHdr (Ctx ctx, boolean IsManual, Timestamp DateTrx,
		int C_Currency_ID, String description, Trx trx)
	{
		this (ctx, 0, trx);
		setIsManual(IsManual);
		if (DateTrx != null)
		{
			setDateTrx (DateTrx);
			setDateAcct (DateTrx);
		}
		setC_Currency_ID (C_Currency_ID);
		if (description != null)
			setDescription(description);
	}	//  create Allocation

	/**
	 * 	Load Constructor
	 * 	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MAllocationHdr (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MAllocation

	/**	Lines						*/
	private MAllocationLine[]	m_lines = null;

	/**
	 * 	Get Lines
	 *	@param requery if true requery
	 *	@return lines
	 */
	public MAllocationLine[] getLines (boolean requery)
	{
		if ((m_lines != null) && (m_lines.length != 0) && !requery)
			return m_lines;
		//
		String sql = "SELECT * FROM C_AllocationLine WHERE C_AllocationHdr_ID=?";
		ArrayList<MAllocationLine> list = new ArrayList<MAllocationLine>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_Trx());
			pstmt.setInt (1, getC_AllocationHdr_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				MAllocationLine line = new MAllocationLine(getCtx(), rs, get_Trx());
				line.setParent(this);
				list.add (line);
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
		} catch (Exception e)
		{
			pstmt = null;
		}
		//
		m_lines = new MAllocationLine[list.size ()];
		list.toArray (m_lines);
		return m_lines;
	}	//	getLines

	/**
	 * 	Create new Allocation by copying
	 * 	@param from allocation
	 * 	@param dateAcct date of the document accounting date
	 *  @param dateTrx date of the document transaction.
	 * 	@param trx p_trx
	 *	@return Allocation
	 */
	public static MAllocationHdr copyFrom (MAllocationHdr from, Timestamp dateAcct, Timestamp dateTrx,
		Trx trx)
	{
		MAllocationHdr to = new MAllocationHdr (from.getCtx(), 0, null);
		to.set_Trx(trx);
		PO.copyValues (from, to, from.getAD_Client_ID(), from.getAD_Org_ID());
		to.set_ValueNoCheck ("DocumentNo", null);
		//
		to.setDocStatus (DOCSTATUS_Drafted);		//	Draft
		to.setDocAction(DOCACTION_Complete);
		//
		to.setDateTrx (dateAcct);
		to.setDateAcct (dateTrx);
		to.setIsManual(false);
		//
		to.setIsApproved (false);
		//
		to.setPosted (false);
		to.setProcessed (false);

		if (!to.save(trx))
			throw new IllegalStateException("Could not create Allocation");

		//	Lines
		if (to.copyLinesFrom(from) == 0)
			throw new IllegalStateException("Could not create Allocation Lines");

		return to;
	}	//	copyFrom

	/**
	 * 	Copy Lines From other Allocation.
	 *	@param otherAllocation allocation
	 *	@return number of lines copied
	 */
	public int copyLinesFrom (MAllocationHdr otherAllocation)
	{
		if (isProcessed() || isPosted() || (otherAllocation == null))
			return 0;
		MAllocationLine[] fromLines = otherAllocation.getLines(false);
		int count = 0;
		for (MAllocationLine fromLine : fromLines) {
			MAllocationLine line = new MAllocationLine (getCtx(), 0, get_Trx());
			PO.copyValues (fromLine, line, fromLine.getAD_Client_ID(), fromLine.getAD_Org_ID());
			line.setC_AllocationHdr_ID(getC_AllocationHdr_ID());
			line.setParent(this);
			line.set_ValueNoCheck ("C_AllocationLine_ID", I_ZERO);	// new

			if (line.getC_Payment_ID() != 0)
			{
				MPayment payment = new MPayment(getCtx(), line.getC_Payment_ID(), get_Trx());
				if (DOCSTATUS_Reversed.equals(payment.getDocStatus()))
				{
					MPayment reversal = payment.getReversal();
					if (reversal != null)
					{
						line.setPaymentInfo(reversal.getC_Payment_ID(), 0);
					}
				}
				
				if (fromLine.getC_Invoice_ID() != 0)
				{
					MPaySelectionCheck psc = MPaySelectionCheck.getOfPayment(getCtx(), fromLine.getC_Payment_ID(), get_Trx());
					if (psc != null)
					{
						MPaySelectionLine psl = MPaySelectionLine.getOfInvoiceCheck(getCtx(), fromLine.getC_Invoice_ID(), psc.getC_PaySelectionCheck_ID(), get_Trx());
						if (psl != null)
						{
							psl.setIsCancelled(true);
							psl.save(get_Trx());
						}
					}
				}
			}

			if (line.save(get_Trx()))
				count++;
		}
		if (fromLines.length != count)
			log.log(Level.SEVERE, "Line difference - From=" + fromLines.length + " <> Saved=" + count);
		return count;
	}	//	copyLinesFrom

	/** Reversal Flag		*/
	private boolean m_reversal = false;

	/**
	 * 	Set Reversal
	 *	@param reversal reversal
	 */
	private void setReversal(boolean reversal)
	{
		m_reversal = reversal;
	}	//	setReversal
	/**
	 * 	Is Reversal
	 *	@return reversal
	 */
	private boolean isReversal()
	{
		return m_reversal;
	}	//	isReversal


	/**
	 * 	Add to Description
	 *	@param description text
	 */
	public void addDescription (String description)
	{
		String desc = getDescription();
		if (desc == null)
			setDescription(description);
		else
			setDescription(desc + " | " + description);
	}	//	addDescription

	/**
	 * 	Set Processed
	 *	@param processed Processed
	 */
	@Override
	public void setProcessed (boolean processed)
	{
		super.setProcessed (processed);
		if (get_ID() == 0)
			return;
		String sql = "UPDATE C_AllocationHdr SET Processed='"
			+ (processed ? "Y" : "N")
			+ "' WHERE C_AllocationHdr_ID=" + getC_AllocationHdr_ID();
		int no = DB.executeUpdate(sql, get_Trx());
		m_lines = null;
		log.fine(processed + " - #" + no);
	}	//	setProcessed


	/**************************************************************************
	 * 	Before Save
	 *	@param newRecord
	 *	@return save
	 */
	@Override
	protected boolean beforeSave (boolean newRecord)
	{
		//	Changed from Not to Active
		if (!newRecord && is_ValueChanged("IsActive") && isActive())
		{
			log.severe ("Cannot Re-Activate deactivated Allocations");
			return false;
		}
		return true;
	}	//	beforeSave

	/**
	 * 	Before Delete.
	 *	@return true if acct was deleted
	 */
	@Override
	protected boolean beforeDelete ()
	{
		Trx trx = get_Trx();
		if ((trx == null))
			log.warning ("No transaction");
		//
		getLines(true);
		if (isPosted())
		{
			String msg = MPeriod.isOpen(this, m_lines, getDateAcct(), MDocBaseType.DOCBASETYPE_PaymentAllocation);
			if (msg != null)
			{
				log.warning (msg);
				return false;
			}
			setPosted(false);
			if (MFactAcct.delete (Table_ID, get_ID(), trx) < 0)
				return false;
		}
		//	Mark as Inactive
		setIsActive(false);		//	updated DB for line delete/process
		String sql = "UPDATE C_AllocationHdr SET IsActive='N' WHERE C_AllocationHdr_ID=?";
		DB.executeUpdate(sql, getC_AllocationHdr_ID(), trx);

		//	Unlink
		getLines(true);
		HashSet<Integer> bps = new HashSet<Integer>();
		for (MAllocationLine line : m_lines) {
			bps.add(Integer.valueOf(line.getC_BPartner_ID()));
			if (!line.delete(true, trx))
				return false;
		}

		return(updateBP(true));
	}	//	beforeDelete

	/**
	 * 	After Save
	 *	@param newRecord
	 *	@param success
	 *	@return success
	 */
	@Override
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		return success;
	}	//	afterSave

	/**************************************************************************
	 * 	Process document
	 *	@param processAction document action
	 *	@return true if performed
	 */
	public boolean processIt (String processAction)
	{
		m_processMsg = null;
		DocumentEngine engine = new DocumentEngine (this, getDocStatus());
		return engine.processIt (processAction, getDocAction());
	}	//	processIt

	/**	Process Message 			*/
	private String		m_processMsg = null;
	/**	Just Prepared Flag			*/
	private boolean		m_justPrepared = false;

	/**
	 * 	Unlock Document.
	 * 	@return true if success
	 */
	public boolean unlockIt()
	{
		log.info(toString());
		setProcessing(false);
		return true;
	}	//	unlockIt

	/**
	 * 	Invalidate Document
	 * 	@return true if success
	 */
	public boolean invalidateIt()
	{
		log.info(toString());
		setDocAction(DOCACTION_Prepare);
		return true;
	}	//	invalidateIt

	/**
	 *	Prepare Document
	 * 	@return new status (In Progress or Invalid)
	 */
	public String prepareIt()
	{
		log.info(toString());
		m_processMsg = ModelValidationEngine.get().fireDocValidate
			(this, ModelValidator.DOCTIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocActionConstants.STATUS_Invalid;

		//	Std Period open?
		MAllocationLine[] lines = getLines(true);
		if (lines.length == 0)
		{
			m_processMsg = "@NoLines@";
			return DocActionConstants.STATUS_Invalid;
		}
		m_processMsg = MPeriod.isOpen(this, lines, getDateAcct(), MDocBaseType.DOCBASETYPE_PaymentAllocation);
		if (m_processMsg != null)
			return DocActionConstants.STATUS_Invalid;
		//	Add up Amounts & validate
		BigDecimal approval = Env.ZERO;
		for (MAllocationLine line : lines) {
			approval = approval.add(line.getWriteOffAmt()).add(line.getDiscountAmt());
			//	Make sure there is BP
			if (line.getC_BPartner_ID() == 0)
			{
				m_processMsg = "No Business Partner";
				return DocActionConstants.STATUS_Invalid;
			}
		}
		setApprovalAmt(approval);
		//
		m_justPrepared = true;
		if (!DOCACTION_Complete.equals(getDocAction()))
			setDocAction(DOCACTION_Complete);
		return DocActionConstants.STATUS_InProgress;
	}	//	prepareIt

	/**
	 * 	Approve Document
	 * 	@return true if success
	 */
	public boolean  approveIt()
	{
		log.info(toString());
		setIsApproved(true);
		return true;
	}	//	approveIt

	/**
	 * 	Reject Approval
	 * 	@return true if success
	 */
	public boolean rejectIt()
	{
		log.info(toString());
		setIsApproved(false);
		return true;
	}	//	rejectIt

	/**
	 * 	Complete Document
	 * 	@return new status (Complete, In Progress, Invalid, Waiting ..)
	 */
	public String completeIt()
	{
		//	Re-Check
		if (!m_justPrepared)
		{
			String status = prepareIt();
			if (!DocActionConstants.STATUS_InProgress.equals(status))
				return status;
		}
		//	Implicit Approval
		if (!isApproved())
			approveIt();
		log.info(toString());

		//	Link
		getLines(m_justPrepared);
		HashSet<Integer> bps = new HashSet<Integer>();
		for (MAllocationLine line : m_lines) {
			bps.add(Integer.valueOf(line.processIt(isReversal())));
		}

		if(!updateBP(false))
			return DocActionConstants.STATUS_Invalid;

		//	User Validation
		String valid = ModelValidationEngine.get().fireDocValidate
			(this, ModelValidator.DOCTIMING_AFTER_COMPLETE);
		if (valid != null)
		{
			m_processMsg = valid;
			return DocActionConstants.STATUS_Invalid;
		}

		setProcessed(true);
		setDocAction(DOCACTION_Close);


		return DocActionConstants.STATUS_Completed;
	}	//	completeIt

	/**
	 * 	Void Document.
	 * 	Same as Close.
	 * 	@return true if success
	 */
	public boolean voidIt()
	{
		log.info(toString());
		boolean retvalue = false;
		if (DOCSTATUS_Closed.equals(getDocStatus())
			|| DOCSTATUS_Reversed.equals(getDocStatus())
			|| DOCSTATUS_Voided.equals(getDocStatus()))
		{
			m_processMsg = "Document Closed: " + getDocStatus();
			setDocAction(DOCACTION_None);
			return false;
		}

		//	Not Processed
		if (DOCSTATUS_Drafted.equals(getDocStatus())
			|| DOCSTATUS_Invalid.equals(getDocStatus())
			|| DOCSTATUS_InProgress.equals(getDocStatus())
			|| DOCSTATUS_Approved.equals(getDocStatus())
			|| DOCSTATUS_NotApproved.equals(getDocStatus()) )
		{
			//	Set lines to 0
			HashSet<Integer> bps = new HashSet<Integer>();
			MAllocationLine[] lines = getLines(false);
			for (MAllocationLine line : lines) {
				// Unlink invoices
				bps.add(Integer.valueOf(line.processIt(true)));

				line.setAmount(Env.ZERO);
				line.setDiscountAmt(Env.ZERO);
				line.setWriteOffAmt(Env.ZERO);
				line.setOverUnderAmt(Env.ZERO);

				line.save();
			}
			if(!updateBP(true))
				return false;

			addDescription(Msg.getMsg(getCtx(), "Voided"));
			retvalue = true;
		}
		else
		{
			retvalue = reverseCorrectIt();
		}

		setProcessed(true);
		return retvalue;
	}	//	voidIt

	/**
	 * 	Close Document.
	 * 	Cancel not delivered Qunatities
	 * 	@return true if success
	 */
	public boolean closeIt()
	{
		log.info(toString());

		setDocAction(DOCACTION_None);
		return true;
	}	//	closeIt

	/**
	 * 	Reverse Correction
	 * 	@return true if success
	 */
	public boolean reverseCorrectIt()
	{
		log.info(toString());
		boolean retValue = reverseIt();
		return retValue;
	}	//	reverseCorrectionIt

	/**
	 * 	Reverse Accrual - none
	 * 	@return false
	 */
	public boolean reverseAccrualIt()
	{
		log.info(toString());
		boolean retValue = reverseIt();
		return retValue;
	}	//	reverseAccrualIt

	/**
	 * 	Re-activate
	 * 	@return false
	 */
	public boolean reActivateIt()
	{
		log.info(toString());
		return false;
	}	//	reActivateIt

	/**
	 * 	String Representation
	 *	@return info
	 */
	@Override
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MAllocationHdr[");
		sb.append(get_ID()).append("-").append(getSummary()).append ("]");
		return sb.toString ();
	}	//	toString

	/**
	 * 	Get Document Info
	 *	@return document info (untranslated)
	 */
	public String getDocumentInfo()
	{
		return Msg.getElement(getCtx(), "C_AllocationHdr_ID") + " " + getDocumentNo();
	}	//	getDocumentInfo

	/**
	 * 	Create PDF
	 *	@return File or null
	 */
	public File createPDF ()
	{
		try
		{
			File temp = File.createTempFile(get_TableName()+get_ID()+"_", ".pdf");
			return createPDF (temp);
		}
		catch (Exception e)
		{
			log.severe("Could not create PDF - " + e.getMessage());
		}
		return null;
	}	//	getPDF

	/**
	 * 	Create PDF file
	 *	@param file output file
	 *	@return file if success
	 */
	public File createPDF (File file)
	{
	//	ReportEngine re = ReportEngine.get (getCtx(), ReportEngine.INVOICE, getC_Invoice_ID());
	//	if (re == null)
			return null;
	//	return re.getPDF(file);
	}	//	createPDF

	/*************************************************************************
	 * 	Get Summary
	 *	@return Summary of Document
	 */
	public String getSummary()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getDocumentNo());
		//	: Total Lines = 123.00 (#1)
		sb.append(": ")
			.append(Msg.translate(getCtx(),"ApprovalAmt")).append("=").append(getApprovalAmt())
			.append(" (#").append(getLines(false).length).append(")");
		//	 - Description
		if ((getDescription() != null) && (getDescription().length() > 0))
			sb.append(" - ").append(getDescription());
		return sb.toString();
	}	//	getSummary

	/**
	 * 	Get Process Message
	 *	@return clear text error message
	 */
	public String getProcessMsg()
	{
		return m_processMsg;
	}	//	getProcessMsg

	/**
	 * 	Get Document Owner (Responsible)
	 *	@return AD_User_ID
	 */
	public int getDoc_User_ID()
	{
		return getCreatedBy();
	}	//	getDoc_User_ID


	/**************************************************************************
	 * 	Reverse Allocation.
	 * 	Period needs to be open
	 *	@return true if reversed
	 */
	private boolean reverseIt()
	{
		MAllocationLine[] iLines = getLines(true);
		String msg = MPeriod.isOpen(this, iLines, getDateAcct(), MDocBaseType.DOCBASETYPE_PaymentAllocation);
		if (msg != null)
			throw new IllegalStateException(msg);

		//	Deep Copy
		MAllocationHdr reversal = copyFrom (this, getDateAcct(), getDateTrx(),
			get_Trx());
		if (reversal == null)
		{
			m_processMsg = "Could not create Payment Allocation Reversal";
			return false;
		}
		reversal.setReversal(true);

		//	Reverse Line Amt
		MAllocationLine[] rLines = reversal.getLines(false);
		for (MAllocationLine rLine : rLines) {
			rLine.setAmount(rLine.getAmount().negate());
			rLine.setDiscountAmt(rLine.getDiscountAmt().negate());
			rLine.setWriteOffAmt(rLine.getWriteOffAmt().negate());
			rLine.setOverUnderAmt(rLine.getOverUnderAmt().negate());
			if (!rLine.save(get_Trx()))
			{
				m_processMsg = "Could not correct Payment Allocation Reversal Line";
				return false;
			}
		}
		reversal.addDescription("{->" + getDocumentNo() + ")");
		//
		if (!reversal.processIt(DocActionConstants.ACTION_Complete))
		{
			m_processMsg = "Reversal ERROR: " + reversal.getProcessMsg();
			return false;
		}

		reversal.closeIt();
		reversal.setProcessing (false);
		reversal.setDocStatus(DOCSTATUS_Reversed);
		reversal.setDocAction(DOCACTION_None);
		reversal.save(get_Trx());
		m_processMsg = reversal.getDocumentNo();
		addDescription("(" + reversal.getDocumentNo() + "<-)");

		setProcessed(true);
		setDocStatus(DOCSTATUS_Reversed);	//	may come from void
		setDocAction(DOCACTION_None);
		return true;
	}	//	reverse


	private boolean updateBP(boolean reverse)
	{

		getLines(false);
		for (MAllocationLine line : m_lines) {
			int C_Payment_ID = line.getC_Payment_ID();
			int C_BPartner_ID = line.getC_BPartner_ID();
			int M_Invoice_ID = line.getC_Invoice_ID();
			if ((C_Payment_ID !=0) || (C_BPartner_ID == 0) || (M_Invoice_ID == 0) )
				continue;

			MInvoice inv = new MInvoice (getCtx(), M_Invoice_ID, get_Trx());

			MBPartner bp = new MBPartner (getCtx(), line.getC_BPartner_ID(), get_Trx());
			//	Update total revenue and balance / credit limit (reversed on AllocationLine.processIt)
			BigDecimal cashAmt = MConversionRate.convertBase(getCtx(), line.getAmount().add(line.getDiscountAmt()).add(line.getWriteOffAmt()),	//	CM adjusted
			getC_Currency_ID(), getDateAcct(), 0, getAD_Client_ID(), getAD_Org_ID());
			if (cashAmt == null)
			{
				m_processMsg = "Could not convert C_Currency_ID=" + getC_Currency_ID()
					+ " to base C_Currency_ID=" + MClient.get(Env.getCtx()).getC_Currency_ID();
				return false;
			}

			//	Total Balance
			BigDecimal newBalance = bp.getTotalOpenBalance(false);
			if (newBalance == null)
				newBalance = Env.ZERO;

			if(reverse)
				newBalance = newBalance.add(cashAmt);
			else
				newBalance = newBalance.subtract(cashAmt);

			BigDecimal newCreditAmt = Env.ZERO;
			if (inv.isSOTrx())
			{
				newCreditAmt = bp.getSO_CreditUsed();

				if(reverse)
				{
					if (newCreditAmt == null)
						newCreditAmt = cashAmt;
					else
						newCreditAmt = newCreditAmt.add(cashAmt);
				}
				else
				{
					if (newCreditAmt == null)
						newCreditAmt = cashAmt.negate();
					else
						newCreditAmt = newCreditAmt.subtract(cashAmt);
				}
			}

			log.fine("TotalOpenBalance=" + bp.getTotalOpenBalance(false) + "(" + cashAmt
				+ ", Credit=" + bp.getSO_CreditUsed() + "->" + newCreditAmt
				+ ", Balance=" + bp.getTotalOpenBalance(false) + " -> " + newBalance);
			bp.setSO_CreditUsed(newCreditAmt);

			bp.setTotalOpenBalance(newBalance);
			bp.setSOCreditStatus();
			if (!bp.save(get_Trx()))
			{
				m_processMsg = "Could not update Business Partner";
				return false;
			}

		} // for all lines

		return true;
	}	//	updateBP

}   //  MAllocation
