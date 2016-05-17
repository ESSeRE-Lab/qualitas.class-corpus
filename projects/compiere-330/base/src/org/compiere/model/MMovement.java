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
 *	Inventory Movement Model
 *
 *  @author Jorg Janke
 *  @version $Id: MMovement.java,v 1.3 2006/07/30 00:51:03 jjanke Exp $
 */
public class MMovement extends X_M_Movement implements DocAction
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param M_Movement_ID id
	 *	@param trx transaction
	 */
	public MMovement (Ctx ctx, int M_Movement_ID, Trx trx)
	{
		super (ctx, M_Movement_ID, trx);
		if (M_Movement_ID == 0)
		{
		//	setC_DocType_ID (0);
			setDocAction (DOCACTION_Complete);	// CO
			setDocStatus (DOCSTATUS_Drafted);	// DR
			setIsApproved (false);
			setIsInTransit (false);
			setMovementDate (new Timestamp(System.currentTimeMillis()));	// @#Date@
			setPosted (false);
			super.setProcessed (false);
		}
	}	//	MMovement

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MMovement (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MMovement

	/**	Lines						*/
	private MMovementLine[]		m_lines = null;
	/** Confirmations				*/
	private MMovementConfirm[]	m_confirms = null;

	/**
	 * 	Get Lines
	 *	@param requery requery
	 *	@return array of lines
	 */
	public MMovementLine[] getLines (boolean requery)
	{
		if ((m_lines != null) && !requery)
			return m_lines;
		//
		ArrayList<MMovementLine> list = new ArrayList<MMovementLine>();
		String sql = "SELECT * FROM M_MovementLine WHERE M_Movement_ID=? ORDER BY Line";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_Trx());
			pstmt.setInt (1, getM_Movement_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				list.add (new MMovementLine (getCtx(), rs, get_Trx()));
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		} catch (Exception e)
		{
			log.log(Level.SEVERE, "getLines", e);
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

		m_lines = new MMovementLine[list.size ()];
		list.toArray (m_lines);
		return m_lines;
	}	//	getLines

	/**
	 * 	Get Confirmations
	 * 	@param requery requery
	 *	@return array of Confirmations
	 */
	public MMovementConfirm[] getConfirmations(boolean requery)
	{
		if ((m_confirms != null) && !requery)
			return m_confirms;

		ArrayList<MMovementConfirm> list = new ArrayList<MMovementConfirm>();
		String sql = "SELECT * FROM M_MovementConfirm WHERE M_Movement_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_Trx());
			pstmt.setInt (1, getM_Movement_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MMovementConfirm(getCtx(), rs, get_Trx()));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "getConfirmations", e);
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

		m_confirms = new MMovementConfirm[list.size ()];
		list.toArray (m_confirms);
		return m_confirms;
	}	//	getConfirmations

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
	 * 	Get Document Info
	 *	@return document info (untranslated)
	 */
	public String getDocumentInfo()
	{
		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		return dt.getName() + " " + getDocumentNo();
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


	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	@Override
	protected boolean beforeSave (boolean newRecord)
	{
		if (getC_DocType_ID() == 0)
		{
			MDocType types[] = MDocType.getOfDocBaseType(getCtx(), MDocBaseType.DOCBASETYPE_MaterialMovement);
			if (types.length > 0)	//	get first
				setC_DocType_ID(types[0].getC_DocType_ID());
			else
			{
				log.saveError("Error", Msg.parseTranslation(getCtx(), "@NotFound@ @C_DocType_ID@"));
				return false;
			}
		}
		return true;
	}	//	beforeSave

	/**
	 * 	Set Processed.
	 * 	Propergate to Lines/Taxes
	 *	@param processed processed
	 */
	@Override
	public void setProcessed (boolean processed)
	{
		super.setProcessed (processed);
		if (get_ID() == 0)
			return;
		String sql = "UPDATE M_MovementLine SET Processed='"
			+ (processed ? "Y" : "N")
			+ "' WHERE M_Movement_ID=" + getM_Movement_ID();
		int noLine = DB.executeUpdate(sql, get_Trx());
		m_lines = null;
		log.fine("Processed=" + processed + " - Lines=" + noLine);
	}	//	setProcessed


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
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.DOCTIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocActionConstants.STATUS_Invalid;
		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());

		MMovementLine[] lines = getLines(true);
		if (lines.length == 0)
		{
			m_processMsg = "@NoLines@";
			return DocActionConstants.STATUS_Invalid;
		}

		//	Std Period open?
		ArrayList<MLocator> locList = new ArrayList<MLocator>();
		for (MMovementLine mLine : lines) {
			locList.add(new MLocator(getCtx(), mLine.getM_Locator_ID(), null));
			locList.add(new MLocator(getCtx(), mLine.getM_LocatorTo_ID(), null));
		}
		MLocator[] locLines = new MLocator[locList.size()];
		locList.toArray(locLines);
		m_processMsg = MPeriod.isOpen(this, locLines, getMovementDate(), dt.getDocBaseType());
		if (m_processMsg != null)
			return DocActionConstants.STATUS_Invalid;

		//	Add up Amounts

		/* nnayak - Bug 1750251 : check material policy and update storage
		   at the line level in completeIt()*/
		//checkMaterialPolicy();

		//	Confirmation
		if (dt.isInTransit())
			createConfirmation();

		m_justPrepared = true;
		if (!DOCACTION_Complete.equals(getDocAction()))
			setDocAction(DOCACTION_Complete);
		return DocActionConstants.STATUS_InProgress;
	}	//	prepareIt

	/**
	 * 	Create Movement Confirmation
	 */
	private void createConfirmation()
	{
		MMovementConfirm[] confirmations = getConfirmations(false);
		if (confirmations.length > 0)
			return;

		//	Create Confirmation
		MMovementConfirm.create (this, false);
	}	//	createConfirmation

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

		//	Outstanding (not processed) Incoming Confirmations ?
		MMovementConfirm[] confirmations = getConfirmations(true);
		for (MMovementConfirm confirm : confirmations)
		{
			if (!confirm.isProcessed())
			{
				m_processMsg = "Open: @M_MovementConfirm_ID@ - "
					+ confirm.getDocumentNo();
				return DocActionConstants.STATUS_InProgress;
			}
		}

		//	Implicit Approval
		if (!isApproved())
			approveIt();
		log.info(toString());

		//
		MMovementLine[] lines = getLines(m_justPrepared);
		for (MMovementLine line : lines)
		{
			/* nnayak - Bug 1750251 : If you have multiple lines for the same product
			in the same Sales Order, or if the generate shipment process was generating
			multiple shipments for the same product in the same run, the first layer
			was getting consumed by all the shipments. As a result, the first layer had
			negative Inventory even though there were other positive layers. */
			checkMaterialPolicy(line);

			MTransaction trxFrom = null;
			if (line.getM_AttributeSetInstance_ID() == 0)
			{
				MMovementLineMA mas[] = MMovementLineMA.get(getCtx(),
					line.getM_MovementLine_ID(), get_Trx());
				for (MMovementLineMA ma : mas) {
					//
					MStorage storageFrom = MStorage.get(getCtx(), line.getM_Locator_ID(),
						line.getM_Product_ID(), ma.getM_AttributeSetInstance_ID(), get_Trx());
					if (storageFrom == null)
						storageFrom = MStorage.getCreate(getCtx(), line.getM_Locator_ID(),
							line.getM_Product_ID(), ma.getM_AttributeSetInstance_ID(), get_Trx());
					//
					MStorage storageTo = MStorage.get(getCtx(), line.getM_LocatorTo_ID(),
						line.getM_Product_ID(), ma.getM_AttributeSetInstance_ID(), get_Trx());
					if (storageTo == null)
						storageTo = MStorage.getCreate(getCtx(), line.getM_LocatorTo_ID(),
							line.getM_Product_ID(), ma.getM_AttributeSetInstance_ID(), get_Trx());
					//
					storageFrom.setQtyOnHand(storageFrom.getQtyOnHand().subtract(ma.getMovementQty()));
					if (!storageFrom.save(get_Trx()))
					{
						ValueNamePair pp = CLogger.retrieveError();
						if (pp != null)
							m_processMsg = pp.getName();
						else
							m_processMsg = "Storage From not updated (MA)";
						return DocActionConstants.STATUS_Invalid;
					}
					//
					storageTo.setQtyOnHand(storageTo.getQtyOnHand().add(ma.getMovementQty()));
					if (!storageTo.save(get_Trx()))
					{
						ValueNamePair pp = CLogger.retrieveError();
						if (pp != null)
							m_processMsg = pp.getName();
						else
							m_processMsg = "Storage To not updated (MA)";
						return DocActionConstants.STATUS_Invalid;
					}

					//
					trxFrom = new MTransaction (getCtx(), line.getAD_Org_ID(),
						X_M_Transaction.MOVEMENTTYPE_MovementFrom,
						line.getM_Locator_ID(), line.getM_Product_ID(), ma.getM_AttributeSetInstance_ID(),
						ma.getMovementQty().negate(), getMovementDate(), get_Trx());
					trxFrom.setM_MovementLine_ID(line.getM_MovementLine_ID());
					if (!trxFrom.save())
					{
						m_processMsg = "Transaction From not inserted (MA)";
						return DocActionConstants.STATUS_Invalid;
					}
					//
					MTransaction trxTo = new MTransaction (getCtx(), line.getAD_Org_ID(),
						X_M_Transaction.MOVEMENTTYPE_MovementTo,
						line.getM_LocatorTo_ID(), line.getM_Product_ID(), ma.getM_AttributeSetInstance_ID(),
						ma.getMovementQty(), getMovementDate(), get_Trx());
					trxTo.setM_MovementLine_ID(line.getM_MovementLine_ID());
					if (!trxTo.save())
					{
						m_processMsg = "Transaction To not inserted (MA)";
						return DocActionConstants.STATUS_Invalid;
					}
				}
			}
			//	Fallback - We have ASI
			if (trxFrom == null)
			{
				MStorage storageFrom = MStorage.get(getCtx(), line.getM_Locator_ID(),
					line.getM_Product_ID(), line.getM_AttributeSetInstance_ID(), get_Trx());
				if (storageFrom == null)
					storageFrom = MStorage.getCreate(getCtx(), line.getM_Locator_ID(),
						line.getM_Product_ID(), line.getM_AttributeSetInstance_ID(), get_Trx());
				//
				MStorage storageTo = MStorage.get(getCtx(), line.getM_LocatorTo_ID(),
					line.getM_Product_ID(), line.getM_AttributeSetInstanceTo_ID(), get_Trx());
				if (storageTo == null)
					storageTo = MStorage.getCreate(getCtx(), line.getM_LocatorTo_ID(),
						line.getM_Product_ID(), line.getM_AttributeSetInstanceTo_ID(), get_Trx());
				//
				storageFrom.setQtyOnHand(storageFrom.getQtyOnHand().subtract(line.getMovementQty()));
				if (!storageFrom.save(get_Trx()))
				{
					ValueNamePair pp = CLogger.retrieveError();
					if (pp != null)
						m_processMsg = pp.getName();
					else
						m_processMsg = "Storage From not updated";
					return DocActionConstants.STATUS_Invalid;
				}
				//
				storageTo.setQtyOnHand(storageTo.getQtyOnHand().add(line.getMovementQty()));
				if (!storageTo.save(get_Trx()))
				{
					ValueNamePair pp = CLogger.retrieveError();
					if (pp != null)
						m_processMsg = pp.getName();
					else
						m_processMsg = "Storage To not updated";
					return DocActionConstants.STATUS_Invalid;
				}

				//
				trxFrom = new MTransaction (getCtx(), line.getAD_Org_ID(),
					X_M_Transaction.MOVEMENTTYPE_MovementFrom,
					line.getM_Locator_ID(), line.getM_Product_ID(), line.getM_AttributeSetInstance_ID(),
					line.getMovementQty().negate(), getMovementDate(), get_Trx());
				trxFrom.setM_MovementLine_ID(line.getM_MovementLine_ID());
				if (!trxFrom.save())
				{
					m_processMsg = "Transaction From not inserted";
					return DocActionConstants.STATUS_Invalid;
				}
				//
				MTransaction trxTo = new MTransaction (getCtx(), line.getAD_Org_ID(),
					X_M_Transaction.MOVEMENTTYPE_MovementTo,
					line.getM_LocatorTo_ID(), line.getM_Product_ID(), line.getM_AttributeSetInstanceTo_ID(),
					line.getMovementQty(), getMovementDate(), get_Trx());
				trxTo.setM_MovementLine_ID(line.getM_MovementLine_ID());
				if (!trxTo.save())
				{
					m_processMsg = "Transaction To not inserted";
					return DocActionConstants.STATUS_Invalid;
				}
			}	//	Fallback
		}	//	for all lines
		//	User Validation
		String valid = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.DOCTIMING_AFTER_COMPLETE);
		if (valid != null)
		{
			m_processMsg = valid;
			return DocActionConstants.STATUS_Invalid;
		}

		//
		setProcessed(true);
		setDocAction(DOCACTION_Close);
		return DocActionConstants.STATUS_Completed;
	}	//	completeIt


	/**
	 * 	Check Material Policy
	 * 	Sets line ASI
	 */
	private void checkMaterialPolicy(MMovementLine line)
	{
		int no = MMovementLineMA.deleteMovementLineMA(line.getM_MovementLine_ID(), get_Trx());
		if (no > 0)
			log.config("Delete old #" + no);

		MClient client = MClient.get(getCtx());
		boolean needSave = false;

		//	Attribute Set Instance
		if (line.getM_AttributeSetInstance_ID() == 0)
		{
			MProduct product = MProduct.get(getCtx(), line.getM_Product_ID());
			MProductCategory pc = MProductCategory.get(getCtx(), product.getM_Product_Category_ID());
			String MMPolicy = pc.getMMPolicy();
			if ((MMPolicy == null) || (MMPolicy.length() == 0))
				MMPolicy = client.getMMPolicy();
				//
			MStorage[] storages = MStorage.getAllWithASI(getCtx(),
				line.getM_Product_ID(),	line.getM_Locator_ID(),
				X_AD_Client.MMPOLICY_FiFo.equals(MMPolicy), get_Trx());
			BigDecimal qtyToDeliver = line.getMovementQty();
			for (int ii = 0; ii < storages.length; ii++)
			{
				MStorage storage = storages[ii];
				BigDecimal qtyAvailable = storage.getQtyOnHand().subtract(
						storage.getQtyDedicated()).subtract(
						storage.getQtyAllocated());
				if(qtyAvailable.compareTo(Env.ZERO) <= 0)
					continue;

				if (ii == 0)
				{
					if (qtyAvailable.compareTo(qtyToDeliver) >= 0)
					{
						line.setM_AttributeSetInstance_ID(storage.getM_AttributeSetInstance_ID());
						needSave = true;
						log.config("Direct - " + line);
						qtyToDeliver = Env.ZERO;
					}
					else
					{
						log.config("Split - " + line);
						MMovementLineMA ma = new MMovementLineMA (line,
							storage.getM_AttributeSetInstance_ID(),
							qtyAvailable);
						if (!ma.save())
							;
						qtyToDeliver = qtyToDeliver.subtract(qtyAvailable);
						log.fine("#" + ii + ": " + ma + ", QtyToDeliver=" + qtyToDeliver);
					}
				}
				else	//	 create addl material allocation
				{
					MMovementLineMA ma = new MMovementLineMA (line,
						storage.getM_AttributeSetInstance_ID(),
						qtyToDeliver);
					if (qtyAvailable.compareTo(qtyToDeliver) >= 0)
						qtyToDeliver = Env.ZERO;
					else
					{
						ma.setMovementQty(qtyAvailable);
						qtyToDeliver = qtyToDeliver.subtract(qtyAvailable);
					}
						if (!ma.save())
							;
						log.fine("#" + ii + ": " + ma + ", QtyToDeliver=" + qtyToDeliver);
				}
				if (qtyToDeliver.signum() == 0)
					break;
			}	//	 for all storages

			//	No AttributeSetInstance found for remainder
			if (qtyToDeliver.signum() != 0)
			{
				MMovementLineMA ma = new MMovementLineMA (line,
					0, qtyToDeliver);
				if (!ma.save())
					;
				log.fine("##: " + ma);
			}
		}	//	attributeSetInstance


		if (needSave && !line.save())
			log.severe("NOT saved " + line);

	}	//	checkMaterialPolicy

	/**
	 * 	Void Document.
	 * 	@return true if success
	 */
	public boolean voidIt()
	{
		log.info(toString());
		if (DOCSTATUS_Closed.equals(getDocStatus())
			|| DOCSTATUS_Reversed.equals(getDocStatus())
			|| DOCSTATUS_Voided.equals(getDocStatus()))
		{
			m_processMsg = "Document Closed: " + getDocStatus();
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
			MMovementLine[] lines = getLines(false);
			for (MMovementLine line : lines) {
				BigDecimal old = line.getMovementQty();
				if (old.compareTo(Env.ZERO) != 0)
				{
					line.setMovementQty(Env.ZERO);
					line.addDescription("Void (" + old + ")");
					line.save(get_Trx());
				}
			}
		}
		else
		{
			return reverseCorrectIt();
		}

		setProcessed(true);
		setDocAction(DOCACTION_None);
		return true;
	}	//	voidIt

	/**
	 * 	Close Document.
	 * 	@return true if success
	 */
	public boolean closeIt()
	{
		log.info(toString());

		//	Close Not delivered Qty
		setDocAction(DOCACTION_None);
		return true;
	}	//	closeIt

	/**
	 * 	Reverse Correction
	 * 	@return false
	 */
	public boolean reverseCorrectIt()
	{
		log.info(toString());
		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		//
		MMovementLine[] oLines = getLines(true);
		m_processMsg = MPeriod.isOpen(this, oLines, getMovementDate(), dt.getDocBaseType());
		if (m_processMsg != null)
			return false;

		//	Deep Copy
		MMovement reversal = new MMovement(getCtx(), 0, get_Trx());
		copyValues(this, reversal, getAD_Client_ID(), getAD_Org_ID());
		reversal.setDocStatus(DOCSTATUS_Drafted);
		reversal.setDocAction(DOCACTION_Complete);
		reversal.setIsApproved (false);
		reversal.setIsInTransit (false);
		reversal.setPosted(false);
		reversal.setProcessed(false);
		reversal.addDescription("{->" + getDocumentNo() + ")");
		if (!reversal.save())
		{
			m_processMsg = "Could not create Movement Reversal";
			return false;
		}

		//	Reverse Line Qty
		for (MMovementLine oLine : oLines) {
			MMovementLine rLine = new MMovementLine(getCtx(), 0, get_Trx());
			copyValues(oLine, rLine, oLine.getAD_Client_ID(), oLine.getAD_Org_ID());
			rLine.setM_Movement_ID(reversal.getM_Movement_ID());
			//
			rLine.setMovementQty(rLine.getMovementQty().negate());
			rLine.setTargetQty(Env.ZERO);
			rLine.setScrappedQty(Env.ZERO);
			rLine.setConfirmedQty(Env.ZERO);
			rLine.setProcessed(false);
			if (!rLine.save())
			{
				m_processMsg = "Could not create Movement Reversal Line";
				return false;
			}
		}
		//
		if (!reversal.processIt(DocActionConstants.ACTION_Complete))
		{
			m_processMsg = "Reversal ERROR: " + reversal.getProcessMsg();
			return false;
		}
		reversal.closeIt();
		reversal.setDocStatus(DOCSTATUS_Reversed);
		reversal.setDocAction(DOCACTION_None);
		reversal.save();
		m_processMsg = reversal.getDocumentNo();

		//	Update Reversed (this)
		addDescription("(" + reversal.getDocumentNo() + "<-)");
		setProcessed(true);
		setDocStatus(DOCSTATUS_Reversed);	//	may come from void
		setDocAction(DOCACTION_None);

		return true;
	}	//	reverseCorrectionIt

	/**
	 * 	Reverse Accrual - none
	 * 	@return false
	 */
	public boolean reverseAccrualIt()
	{
		log.info(toString());
		return false;
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
	 * 	String Representation
	 *	@return info
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer ("MMovement[");
		sb.append (get_ID ())
			.append ("-").append (getDocumentNo())
			.append ("]");
		return sb.toString ();
	}	//	toString

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

	/**
	 * 	Get Document Currency
	 *	@return C_Currency_ID
	 */
	public int getC_Currency_ID()
	{
	//	MPriceList pl = MPriceList.get(getCtx(), getM_PriceList_ID());
	//	return pl.getC_Currency_ID();
		return 0;
	}	//	getC_Currency_ID

}	//	MMovement

