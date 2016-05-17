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
import java.util.*;
import java.util.logging.*;

import org.compiere.util.*;

/**
 *	Match Invoice (Receipt<>Invoice) Model.
 *	Accounting:
 *	- Not Invoiced Receipts (relief)
 *	- IPV
 *	
 *  @author Jorg Janke
 *  @version $Id: MMatchInv.java,v 1.3 2006/07/30 00:51:05 jjanke Exp $
 */
public class MMatchInv extends X_M_MatchInv
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 	Get InOut-Invoice Matches
	 *	@param ctx context
	 *	@param M_InOutLine_ID shipment
	 *	@param C_InvoiceLine_ID invoice
	 *	@param trx transaction
	 *	@return array of matches
	 */
	public static MMatchInv[] get (Ctx ctx, 
		int M_InOutLine_ID, int C_InvoiceLine_ID, Trx trx)
	{
		if (M_InOutLine_ID == 0 || C_InvoiceLine_ID == 0)
			return new MMatchInv[]{};
		//
		String sql = "SELECT * FROM M_MatchInv WHERE M_InOutLine_ID=? AND C_InvoiceLine_ID=?";
		ArrayList<MMatchInv> list = new ArrayList<MMatchInv>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_InOutLine_ID);
			pstmt.setInt (2, C_InvoiceLine_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MMatchInv (ctx, rs, trx));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e); 
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
		MMatchInv[] retValue = new MMatchInv[list.size()];
		list.toArray (retValue);
		return retValue;
	}	//	get

	/**
	 * 	Get InOut Invoice Matches
	 *	@param ctx context
	 *	@param M_InOutLine_ID shipment
	 *	@param trx transaction
	 *	@return array of matches
	 */
	public static MMatchInv[] get (Ctx ctx, 
		int M_InOutLine_ID, Trx trx)
	{
		if (M_InOutLine_ID == 0)
			return new MMatchInv[]{};
		//
		String sql = "SELECT * FROM M_MatchInv WHERE M_InOutLine_ID=?";
		ArrayList<MMatchInv> list = new ArrayList<MMatchInv>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_InOutLine_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MMatchInv (ctx, rs, trx));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e); 
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
		MMatchInv[] retValue = new MMatchInv[list.size()];
		list.toArray (retValue);
		return retValue;
	}	//	get

	/**
	 * 	Get Inv Matches for InOut
	 *	@param ctx context
	 *	@param M_InOut_ID shipment
	 *	@param trx transaction
	 *	@return array of matches
	 */
	public static MMatchInv[] getInOut (Ctx ctx, 
		int M_InOut_ID, Trx trx)
	{
		if (M_InOut_ID == 0)
			return new MMatchInv[]{};
		//
		String sql = "SELECT * FROM M_MatchInv m"
			+ " INNER JOIN M_InOutLine l ON (m.M_InOutLine_ID=l.M_InOutLine_ID) "
			+ "WHERE l.M_InOut_ID=?"; 
		ArrayList<MMatchInv> list = new ArrayList<MMatchInv>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, M_InOut_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MMatchInv (ctx, rs, trx));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e); 
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
		MMatchInv[] retValue = new MMatchInv[list.size()];
		list.toArray (retValue);
		return retValue;
	}	//	getInOut

	/**
	 * 	Get Inv Matches for Invoice
	 *	@param ctx context
	 *	@param C_Invoice_ID invoice
	 *	@param trx transaction
	 *	@return array of matches
	 */
	public static MMatchInv[] getInvoice (Ctx ctx, 
		int C_Invoice_ID, Trx trx)
	{
		if (C_Invoice_ID == 0)
			return new MMatchInv[]{};
		//
		String sql = "SELECT * FROM M_MatchInv mi"
			+ " INNER JOIN C_InvoiceLine il ON (mi.C_InvoiceLine_ID=il.C_InvoiceLine_ID) "
			+ "WHERE il.C_Invoice_ID=?";
		ArrayList<MMatchInv> list = new ArrayList<MMatchInv>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setInt (1, C_Invoice_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MMatchInv (ctx, rs, trx));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e); 
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
		MMatchInv[] retValue = new MMatchInv[list.size()];
		list.toArray (retValue);
		return retValue;
	}	//	getInvoice

	
	/**	Static Logger	*/
	private static CLogger	s_log	= CLogger.getCLogger (MMatchInv.class);

	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param M_MatchInv_ID id
	 *	@param trx transaction
	 */
	public MMatchInv (Ctx ctx, int M_MatchInv_ID, Trx trx)
	{
		super (ctx, M_MatchInv_ID, trx);
		if (M_MatchInv_ID == 0)
		{
		//	setDateTrx (new Timestamp(System.currentTimeMillis()));
		//	setC_InvoiceLine_ID (0);
		//	setM_InOutLine_ID (0);
		//	setM_Product_ID (0);
			setM_AttributeSetInstance_ID(0);
		//	setQty (Env.ZERO);
			setPosted (false);
			setProcessed (false);
			setProcessing (false);
		}
	}	//	MMatchInv

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MMatchInv (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MMatchInv
	
	/**
	 * 	Invoice Line Constructor
	 *	@param iLine invoice line
	 *	@param dateTrx optional date
	 *	@param qty matched quantity
	 */
	public MMatchInv (MInvoiceLine iLine, Timestamp dateTrx, BigDecimal qty)
	{
		this (iLine.getCtx(), 0, iLine.get_Trx());
		setClientOrg(iLine);
		setC_InvoiceLine_ID(iLine.getC_InvoiceLine_ID());
		setM_InOutLine_ID(iLine.getM_InOutLine_ID());
		if (dateTrx != null)
			setDateTrx (dateTrx);
		setM_Product_ID (iLine.getM_Product_ID());
		setM_AttributeSetInstance_ID(iLine.getM_AttributeSetInstance_ID());
		setQty (qty);
		setProcessed(true);		//	auto
	}	//	MMatchInv

	
	
	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	@Override
	protected boolean beforeSave (boolean newRecord)
	{
		//	Set Trx Date
		if (getDateTrx() == null)
			setDateTrx (new Timestamp(System.currentTimeMillis()));
		//	Set Acct Date
		if (getDateAcct() == null)
		{
			Timestamp ts = getNewerDateAcct();
			if (ts == null)
				ts = getDateTrx();
			setDateAcct (ts);
		}
		if (getM_AttributeSetInstance_ID() == 0 && getM_InOutLine_ID() != 0)
		{
			MInOutLine iol = new MInOutLine (getCtx(), getM_InOutLine_ID(), get_Trx());
			setM_AttributeSetInstance_ID(iol.getM_AttributeSetInstance_ID());
		}
		return true;
	}	//	beforeSave
	
	/**
	 * 	Get the later Date Acct from invoice or shipment
	 *	@return date or null
	 */
	private Timestamp getNewerDateAcct()
	{
		Timestamp invoiceDate = null;
		Timestamp shipDate = null;
		
		String sql = "SELECT i.DateAcct "
			+ "FROM C_InvoiceLine il"
			+ " INNER JOIN C_Invoice i ON (i.C_Invoice_ID=il.C_Invoice_ID) "
			+ "WHERE C_InvoiceLine_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, getC_InvoiceLine_ID());
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
				invoiceDate = rs.getTimestamp(1);
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log (Level.SEVERE, sql, e);
		}
		sql = "SELECT io.DateAcct "
			+ "FROM M_InOutLine iol"
			+ " INNER JOIN M_InOut io ON (io.M_InOut_ID=iol.M_InOut_ID) "
			+ "WHERE iol.M_InOutLine_ID=?";
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, getM_InOutLine_ID());
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
				shipDate = rs.getTimestamp(1);
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log (Level.SEVERE, sql, e);
		}
		//
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
		
		if (invoiceDate == null)
			return shipDate;
		if (shipDate == null)
			return invoiceDate;
		if (invoiceDate.after(shipDate))
			return invoiceDate;
		return shipDate;
	}	//	getNewerDateAcct
	
	
	/**
	 * 	Before Delete
	 *	@return true if acct was deleted
	 */
	@Override
	protected boolean beforeDelete ()
	{
		if (isPosted())
		{
			String msg = MPeriod.isOpen(this, null, getDateAcct(), MDocBaseType.DOCBASETYPE_MatchInvoice);
			if (msg != null)
			{
				log.warning(msg);
				return false;
			}
			setPosted(false);
			return MFactAcct.delete (Table_ID, get_ID(), get_Trx()) >= 0;
		}
		return true;
	}	//	beforeDelete

	
	/**
	 * 	After Delete
	 *	@param success success
	 *	@return success
	 */
	@Override
	protected boolean afterDelete (boolean success)
	{
		if (success)
		{
			//	Get Order and decrease invoices
			MInvoiceLine iLine = new MInvoiceLine (getCtx(), getC_InvoiceLine_ID(), get_Trx());
			int C_OrderLine_ID = iLine.getC_OrderLine_ID();
			if (C_OrderLine_ID == 0)
			{
				MInOutLine ioLine = new MInOutLine (getCtx(), getM_InOutLine_ID(), get_Trx());
				C_OrderLine_ID = ioLine.getC_OrderLine_ID();
			}
			//	No Order Found
			if (C_OrderLine_ID == 0)
				return success;
			//	Find MatchPO
			MMatchPO[] mPO = MMatchPO.get(getCtx(), C_OrderLine_ID, 
				getC_InvoiceLine_ID(), get_Trx());
			for (MMatchPO element : mPO) {
				if (element.getM_InOutLine_ID() == 0)
					element.delete(true);
				else
				{
					element.setC_InvoiceLine_ID(null);
					element.save();
				}
			}
		}
		return success;
	}	//	afterDelete
	
}	//	MMatchInv
