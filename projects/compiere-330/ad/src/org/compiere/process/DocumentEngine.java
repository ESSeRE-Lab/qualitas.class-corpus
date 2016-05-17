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

import java.io.*;
import java.math.*;

import javax.naming.*;

import org.compiere.db.*;
import org.compiere.interfaces.*;
import org.compiere.model.*;
import org.compiere.util.*;
import org.compiere.vos.*;

/**
 *	Document Action Engine
 *	
 *  @author Jorg Janke
 *  @version $Id: DocumentEngine.java,v 1.2 2006/07/30 00:54:44 jjanke Exp $
 */
public class DocumentEngine implements DocAction
{
	/**
	 * 	Doc Engine (Drafted)
	 * 	@param po document
	 */
	public DocumentEngine (DocAction po)
	{
		this (po, DocActionConstants.STATUS_Drafted);
	}	//	DocActionEngine
	
	/**
	 * 	Doc Engine
	 * 	@param po document
	 * 	@param docStatus initial document status
	 */
	public DocumentEngine (DocAction po, String docStatus)
	{
		m_document = po;
		if (docStatus != null)
			m_status = docStatus;
	}	//	DocActionEngine

	/** Persistent Document 	*/
	private DocAction	m_document;
	/** Document Status			*/
	private String		m_status = DocActionConstants.STATUS_Drafted;
	/**	Process Message 		*/
	private String		m_message = null;
	/** Actual Doc Action		*/
	private String		m_action = null;
	
	/**
	 * 	Get Doc Status
	 *	@return document status
	 */
	public String getDocStatus()
	{
		return m_status;
	}	//	getDocStatus

	/**
	 * 	Set Doc Status - Ignored
	 *	@param ignored Status is not set directly
	 * @see org.compiere.process.DocAction#setDocStatus(String)
	 */
	public void setDocStatus(String ignored)
	{
	}	//	setDocStatus

	/**
	 * 	Document is Drafted
	 *	@return true if drafted
	 */
	public boolean isDrafted()
	{
		return DocActionConstants.STATUS_Drafted.equals(m_status);
	}	//	isDrafted
	
	/**
	 * 	Document is Invalid
	 *	@return true if Invalid
	 */
	public boolean isInvalid()
	{
		return DocActionConstants.STATUS_Invalid.equals(m_status);
	}	//	isInvalid
	
	/**
	 * 	Document is In Progress
	 *	@return true if In Progress
	 */
	public boolean isInProgress()
	{
		return DocActionConstants.STATUS_InProgress.equals(m_status);
	}	//	isInProgress
	
	/**
	 * 	Document is Approved
	 *	@return true if Approved
	 */
	public boolean isApproved()
	{
		return DocActionConstants.STATUS_Approved.equals(m_status);
	}	//	isApproved
	
	/**
	 * 	Document is Not Approved
	 *	@return true if Not Approved
	 */
	public boolean isNotApproved()
	{
		return DocActionConstants.STATUS_NotApproved.equals(m_status);
	}	//	isNotApproved
	
	/**
	 * 	Document is Waiting Payment or Confirmation
	 *	@return true if Waiting Payment
	 */
	public boolean isWaiting()
	{
		return DocActionConstants.STATUS_WaitingPayment.equals(m_status)
			|| DocActionConstants.STATUS_WaitingConfirmation.equals(m_status);
	}	//	isWaitingPayment
	
	/**
	 * 	Document is Completed
	 *	@return true if Completed
	 */
	public boolean isCompleted()
	{
		return DocActionConstants.STATUS_Completed.equals(m_status);
	}	//	isCompleted
	
	/**
	 * 	Document is Reversed
	 *	@return true if Reversed
	 */
	public boolean isReversed()
	{
		return DocActionConstants.STATUS_Reversed.equals(m_status);
	}	//	isReversed
	
	/**
	 * 	Document is Closed
	 *	@return true if Closed
	 */
	public boolean isClosed()
	{
		return DocActionConstants.STATUS_Closed.equals(m_status);
	}	//	isClosed
	
	/**
	 * 	Document is Voided
	 *	@return true if Voided
	 */
	public boolean isVoided()
	{
		return DocActionConstants.STATUS_Voided.equals(m_status);
	}	//	isVoided
	
	/**
	 * 	Document Status is Unknown
	 *	@return true if unknown
	 */
	public boolean isUnknown()
	{
		return DocActionConstants.STATUS_Unknown.equals(m_status) || 
			!(isDrafted() || isInvalid() || isInProgress() || isNotApproved()
				|| isApproved() || isWaiting() || isCompleted()
				|| isReversed() || isClosed() || isVoided() );
	}	//	isUnknown

	
	/**
	 * 	Process actual document.
	 * 	Checks if user (document) action is valid and then process action 
	 * 	Calls the individual actions which call the document action
	 *	@param processAction document action based on workflow
	 *	@param docAction document action based on document
	 *	@return true if performed
	 */
	public boolean processIt (String processAction, String docAction)
	{
		m_message = null;
		m_action = null;
		//	Std User Workflows - see MWFNodeNext.isValidFor
		
		if (isValidAction(processAction))	//	WF Selection first
			m_action = processAction;
		//
		else if (isValidAction(docAction))	//	User Selection second
			m_action = docAction;
		//	Nothing to do
		else if (processAction.equals(DocActionConstants.ACTION_None)
			|| docAction.equals(DocActionConstants.ACTION_None))
		{
			if (m_document != null)
				m_document.get_Logger().info ("**** No Action (Prc=" + processAction + "/Doc=" + docAction + ") " + m_document);
			return true;	
		}
		else
		{
			throw new IllegalStateException("Status=" + getDocStatus() 
				+ " - Invalid Actions: Process="  + processAction + ", Doc=" + docAction);
		}
		if (m_document != null)
			m_document.get_Logger().info ("**** Action=" + m_action + " (Prc=" + processAction + "/Doc=" + docAction + ") " + m_document);
		boolean success = processIt (m_action);
		if (m_document != null)
			m_document.get_Logger().fine("**** Action=" + m_action + " - Success=" + success);
		return success;
	}	//	process
	
	/**
	 * 	Process actual document - do not call directly.
	 * 	Calls the individual actions which call the document action
	 *	@param action document action
	 *	@return true if performed
	 */
	public boolean processIt (String action)
	{
		m_message = null;
		m_action = action;
		//
		if (DocActionConstants.ACTION_Unlock.equals(m_action))
			return unlockIt();
		if (DocActionConstants.ACTION_Invalidate.equals(m_action))
			return invalidateIt();
		if (DocActionConstants.ACTION_Prepare.equals(m_action))
			return DocActionConstants.STATUS_InProgress.equals(prepareIt());
		if (DocActionConstants.ACTION_Approve.equals(m_action))
			return approveIt();
		if (DocActionConstants.ACTION_Reject.equals(m_action))
			return rejectIt();
		if (DocActionConstants.ACTION_Complete.equals(m_action) || DocActionConstants.ACTION_WaitComplete.equals(m_action))
		{
			String status = null;
			if (isDrafted() || isInvalid())		//	prepare if not prepared yet
			{
				status = prepareIt();
				if (!DocActionConstants.STATUS_InProgress.equals(status))
					return false;
			}
			status = completeIt();
			if (m_document != null 
				&& !Ini.isClient())		//	Post Immediate if on Server
			{
				MClient client = MClient.get(m_document.getCtx(), m_document.getAD_Client_ID());
				if (DocActionConstants.STATUS_Completed.equals(status) && client.isPostImmediate())
				{
					m_document.save();
					postIt();
				}
			}
			return DocActionConstants.STATUS_Completed.equals(status)
				|| DocActionConstants.STATUS_InProgress.equals(status)
				|| DocActionConstants.STATUS_WaitingPayment.equals(status)
				|| DocActionConstants.STATUS_WaitingConfirmation.equals(status);
		}
		if (DocActionConstants.ACTION_ReActivate.equals(m_action))
			return reActivateIt();
		if (DocActionConstants.ACTION_Reverse_Accrual.equals(m_action))
			return reverseAccrualIt();
		if (DocActionConstants.ACTION_Reverse_Correct.equals(m_action))
			return reverseCorrectIt();
		if (DocActionConstants.ACTION_Close.equals(m_action))
			return closeIt();
		if (DocActionConstants.ACTION_Void.equals(m_action))
			return voidIt();
		if (DocActionConstants.ACTION_Post.equals(m_action))
			return postIt();
		//
		return false;
	}	//	processDocument
	
	/**
	 * 	Unlock Document.
	 * 	Status: Drafted
	 * 	@return true if success 
	 * 	@see org.compiere.process.DocAction#unlockIt()
	 */
	public boolean unlockIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_Unlock))
			return false;
		if (m_document != null)
		{
			if (m_document.unlockIt())
			{
				m_status = DocActionConstants.STATUS_Drafted;
				m_document.setDocStatus(m_status);
				return true;
			}
			return false;
		}
		m_status = DocActionConstants.STATUS_Drafted;
		return true;
	}	//	unlockIt
	
	/**
	 * 	Invalidate Document.
	 * 	Status: Invalid
	 * 	@return true if success 
	 * 	@see org.compiere.process.DocAction#invalidateIt()
	 */
	public boolean invalidateIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_Invalidate))
			return false;
		if (m_document != null)
		{
			if (m_document.invalidateIt())
			{
				m_status = DocActionConstants.STATUS_Invalid;
				m_document.setDocStatus(m_status);
				return true;
			}
			return false;
		}
		m_status = DocActionConstants.STATUS_Invalid;
		return true;
	}	//	invalidateIt
	
	/**
	 *	Process Document.
	 * 	Status is set by process
	 * 	@return new status (In Progress or Invalid) 
	 * 	@see org.compiere.process.DocAction#prepareIt()
	 */
	public String prepareIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_Prepare))
			return m_status;
		if (m_document != null)
		{
			m_status = m_document.prepareIt();
			m_document.setDocStatus(m_status);
		}
		return m_status;
	}	//	processIt

	/**
	 * 	Approve Document.
	 * 	Status: Approved
	 * 	@return true if success 
	 * 	@see org.compiere.process.DocAction#approveIt()
	 */
	public boolean  approveIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_Approve))
			return false;
		if (m_document != null)
		{
			if (m_document.approveIt())
			{
				m_status = DocActionConstants.STATUS_Approved;
				m_document.setDocStatus(m_status);
				return true;
			}
			return false;
		}
		m_status = DocActionConstants.STATUS_Approved;
		return true;
	}	//	approveIt
	
	/**
	 * 	Reject Approval.
	 * 	Status: Not Approved
	 * 	@return true if success 
	 * 	@see org.compiere.process.DocAction#rejectIt()
	 */
	public boolean rejectIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_Reject))
			return false;
		if (m_document != null)
		{
			if (m_document.rejectIt())
			{
				m_status = DocActionConstants.STATUS_NotApproved;
				m_document.setDocStatus(m_status);
				return true;
			}
			return false;
		}
		m_status = DocActionConstants.STATUS_NotApproved;
		return true;
	}	//	rejectIt
	
	/**
	 * 	Complete Document.
	 * 	Status is set by process
	 * 	@return new document status (Complete, In Progress, Invalid, Waiting ..)
	 * 	@see org.compiere.process.DocAction#completeIt()
	 */
	public String completeIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_Complete))
			return m_status;
		if (m_document != null)
		{
			m_status = m_document.completeIt();
			m_document.setDocStatus(m_status);
		}
		return m_status;
	}	//	completeIt
	
	/**
	 * 	Post Document
	 * 	Does not change status
	 * 	@return true if success 
	 */
	public boolean postIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_Post) 
			|| m_document == null)
			return false;
		try
		{
			//	Should work on Client and Server
			InitialContext ctx = CConnection.get().getInitialContext(true);
			ServerHome serverHome = (ServerHome)ctx.lookup (ServerHome.JNDI_NAME);
			if (serverHome != null)
			{
				Server server = serverHome.create();
				if (server != null)
				{
					String error = server.postImmediate(Env.getCtx(), 
						m_document.getAD_Client_ID(),
						m_document.get_Table_ID(), m_document.get_ID(), 
						true, m_document.get_Trx());
					m_document.get_Logger().config("Server: " + error == null ? "OK" : error);
					return error == null;
				}
			}
			else
				m_document.get_Logger().config("NoServerHome");
		}
		catch (Exception e)
		{
			m_document.get_Logger().config("(ex) " + e.getMessage());
		}
		return false;
	}	//	postIt
	
	/**
	 * 	Void Document.
	 * 	Status: Voided
	 * 	@return true if success 
	 * 	@see org.compiere.process.DocAction#voidIt()
	 */
	public boolean voidIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_Void))
			return false;
		if (m_document != null)
		{
			if (m_document.voidIt())
			{
				m_status = DocActionConstants.STATUS_Voided;
				m_document.setDocStatus(m_status);
				return true;
			}
			return false;
		}
		m_status = DocActionConstants.STATUS_Voided;
		return true;
	}	//	voidIt
	
	/**
	 * 	Close Document.
	 * 	Status: Closed
	 * 	@return true if success 
	 * 	@see org.compiere.process.DocAction#closeIt()
	 */
	public boolean closeIt()
	{
		if (m_document != null 	//	orders can be closed any time
			&& m_document.get_Table_ID() == X_C_Order.Table_ID)
			;
		else if (!isValidAction(DocActionConstants.ACTION_Close))
			return false;
		if (m_document != null)
		{
			if (m_document.closeIt())
			{
				m_status = DocActionConstants.STATUS_Closed;
				m_document.setDocStatus(m_status);
				return true;
			}
			return false;
		}
		m_status = DocActionConstants.STATUS_Closed;
		return true;
	}	//	closeIt
	
	/**
	 * 	Reverse Correct Document.
	 * 	Status: Reversed
	 * 	@return true if success 
	 * 	@see org.compiere.process.DocAction#reverseCorrectIt()
	 */
	public boolean reverseCorrectIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_Reverse_Correct))
			return false;
		if (m_document != null)
		{
			if (m_document.reverseCorrectIt())
			{
				m_status = DocActionConstants.STATUS_Reversed;
				m_document.setDocStatus(m_status);
				return true;
			}
			return false;
		}
		m_status = DocActionConstants.STATUS_Reversed;
		return true;
	}	//	reverseCorrectIt
	
	/**
	 * 	Reverse Accrual Document.
	 * 	Status: Reversed
	 * 	@return true if success 
	 * 	@see org.compiere.process.DocAction#reverseAccrualIt()
	 */
	public boolean reverseAccrualIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_Reverse_Accrual))
			return false;
		if (m_document != null)
		{
			if (m_document.reverseAccrualIt())
			{
				m_status = DocActionConstants.STATUS_Reversed;
				m_document.setDocStatus(m_status);
				return true;
			}
			return false;
		}
		m_status = DocActionConstants.STATUS_Reversed;
		return true;
	}	//	reverseAccrualIt
	
	/** 
	 * 	Re-activate Document.
	 * 	Status: In Progress
	 * 	@return true if success 
	 * 	@see org.compiere.process.DocAction#reActivateIt()
	 */
	public boolean reActivateIt()
	{
		if (!isValidAction(DocActionConstants.ACTION_ReActivate))
			return false;
		if (m_document != null)
		{
			if (m_document.reActivateIt())
			{
				m_status = DocActionConstants.STATUS_InProgress;
				m_document.setDocStatus(m_status);
				return true;
			}
			return false;
		}
		m_status = DocActionConstants.STATUS_InProgress;
		return true;
	}	//	reActivateIt

	
	/**
	 * 	Set Document Status to new Status
	 *	@param newStatus new status
	 */
	void setStatus (String newStatus)
	{
		m_status = newStatus;
	}	//	setStatus

	
	/**************************************************************************
	 * 	Get Action Options based on current Status
	 *	@return array of actions
	 */
	public String[] getActionOptions()
	{
		if (isInvalid())
			return new String[] {DocActionConstants.ACTION_Prepare, DocActionConstants.ACTION_Invalidate, 
				DocActionConstants.ACTION_Unlock, DocActionConstants.ACTION_Void};

		if (isDrafted())
			return new String[] {DocActionConstants.ACTION_Prepare, DocActionConstants.ACTION_Invalidate, DocActionConstants.ACTION_Complete, 
				DocActionConstants.ACTION_Unlock, DocActionConstants.ACTION_Void};
		
		if (isInProgress() || isApproved())
			return new String[] {DocActionConstants.ACTION_Complete, DocActionConstants.ACTION_WaitComplete, 
				DocActionConstants.ACTION_Approve, DocActionConstants.ACTION_Reject, 
				DocActionConstants.ACTION_Unlock, DocActionConstants.ACTION_Void, DocActionConstants.ACTION_Prepare};
		
		if (isNotApproved())
			return new String[] {DocActionConstants.ACTION_Reject, DocActionConstants.ACTION_Prepare, 
				DocActionConstants.ACTION_Unlock, DocActionConstants.ACTION_Void};
		
		if (isWaiting())
			return new String[] {DocActionConstants.ACTION_Complete, DocActionConstants.ACTION_WaitComplete,
				DocActionConstants.ACTION_ReActivate, DocActionConstants.ACTION_Void, DocActionConstants.ACTION_Close};
		
		if (isCompleted())
			return new String[] {DocActionConstants.ACTION_Close, DocActionConstants.ACTION_ReActivate, 
				DocActionConstants.ACTION_Reverse_Accrual, DocActionConstants.ACTION_Reverse_Correct, 
				DocActionConstants.ACTION_Post, DocActionConstants.ACTION_Void};
		
		if (isClosed())
			return new String[] {DocActionConstants.ACTION_Post, DocActionConstants.ACTION_ReOpen};
		
		if (isReversed() || isVoided())
			return new String[] {DocActionConstants.ACTION_Post};
		
		return new String[] {};
	}	//	getActionOptions

	/**
	 * 	Is The Action Valid based on current state
	 *	@param action action
	 *	@return true if valid
	 */
	public boolean isValidAction (String action)
	{
		String[] options = getActionOptions();
		for (String element : options) {
			if (element.equals(action))
				return true;
		}
		return false;
	}	//	isValidAction

	/**
	 * 	Get Process Message
	 *	@return clear text error message
	 */
	public String getProcessMsg ()
	{
		return m_message;
	}	//	getProcessMsg
	
	/**
	 * 	Get Process Message
	 *	@param msg clear text error message
	 */
	public void setProcessMsg (String msg)
	{
		m_message = msg;
	}	//	setProcessMsg
	
	
	/**	Document Exception Message		*/
	private static String EXCEPTION_MSG = "Document Engine is no Document"; 
	
	/*************************************************************************
	 * 	Get Summary
	 *	@return throw exception
	 */
	public String getSummary()
	{
		throw new IllegalStateException(EXCEPTION_MSG);
	}
	
	/**
	 * 	Get Document No
	 *	@return throw exception
	 */
	public String getDocumentNo()
	{
		throw new IllegalStateException(EXCEPTION_MSG);
	}

	/**
	 * 	Get Document Info
	 *	@return throw exception
	 */
	public String getDocumentInfo()
	{
		throw new IllegalStateException(EXCEPTION_MSG);
	}

	/**
	 * 	Get Document Owner
	 *	@return throw exception
	 */
	public int getDoc_User_ID()
	{
		throw new IllegalStateException(EXCEPTION_MSG);
	}
	
	/**
	 * 	Get Document Currency
	 *	@return throw exception
	 */
	public int getC_Currency_ID()
	{
		throw new IllegalStateException(EXCEPTION_MSG);
	}

	/**
	 * 	Get Document Approval Amount
	 *	@return throw exception
	 */
	public BigDecimal getApprovalAmt()
	{
		throw new IllegalStateException(EXCEPTION_MSG);
	}

	/**
	 * 	Get Document Client
	 *	@return throw exception
	 */
	public int getAD_Client_ID()
	{
		throw new IllegalStateException(EXCEPTION_MSG);
	}

	/**
	 * 	Get Document Organization
	 *	@return throw exception
	 */
	public int getAD_Org_ID()
	{
		throw new IllegalStateException(EXCEPTION_MSG);
	}
	
	/**
	 * 	Get Doc Action
	 *	@return Document Action
	 */
	public String getDocAction()
	{
		return m_action;
	}

	/**
	 * 	Save Document
	 *	@return throw exception
	 */
	public boolean save()
	{
		throw new IllegalStateException(EXCEPTION_MSG);
	}
	
	/**
	 * 	Get Context
	 *	@return context
	 */
	public Ctx getCtx()
	{
		if (m_document != null)
			return m_document.getCtx();
		throw new IllegalStateException(EXCEPTION_MSG);
	}	//	getCtx

	/**
	 * 	Get ID of record
	 *	@return ID
	 */
	public int get_ID()
	{
		if (m_document != null)
			return m_document.get_ID();
		throw new IllegalStateException(EXCEPTION_MSG);
	}	//	get_ID
	
	/**
	 * 	Get AD_Table_ID
	 *	@return AD_Table_ID
	 */
	public int get_Table_ID()
	{
		if (m_document != null)
			return m_document.get_Table_ID();
		throw new IllegalStateException(EXCEPTION_MSG);
	}	//	get_Table_ID
	
	/**
	 * 	Get Logger
	 *	@return logger
	 */
	public CLogger get_Logger()
	{
		if (m_document != null)
			return m_document.get_Logger();
		throw new IllegalStateException(EXCEPTION_MSG);
	}	//	get_Logger

	/**
	 * 	Get Transaction
	 *	@return p_trx name
	 */
	public Trx get_Trx()
	{
		return null;
	}	//	get_TrxName

	/**
	 * 	CreatePDF
	 *	@return null
	 */
	public File createPDF ()
	{
		return null;
	}
	
}	//	DocumentEnine
