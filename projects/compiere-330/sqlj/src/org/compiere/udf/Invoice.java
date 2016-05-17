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
package org.compiere.udf;

import java.math.*;
import java.sql.*;
import COM.ibm.db2.app.*;


/**
 *	SQLJ Invoice related Functions
 *	
 *  @author Jorg Janke
 *  @version $Id: Invoice.java,v 1.3 2006/07/30 00:59:07 jjanke Exp $
 */
public class Invoice extends UDF
{
	/**
	 * 	Open Invoice Amount.
	 * 	- invoiceOpen
	 *	@param p_C_Invoice_ID invoice
	 *	@param p_C_InvoicePaySchedule_ID payment schedule
	 *	@return open amount
	 *	@throws SQLException
	 */
	//public static BigDecimal open (int p_C_Invoice_ID, int p_C_InvoicePaySchedule_ID)
	public static double open (int p_C_Invoice_ID, int p_C_InvoicePaySchedule_ID)
		throws SQLException
	{
		//	Invoice info
		int C_Currency_ID = 0;
		int C_ConversionType_ID = 0;
		BigDecimal GrandTotal = null;
		BigDecimal MultiplierAP = null;
		BigDecimal MultiplierCM = null;
		//
		String sql = "SELECT MAX(C_Currency_ID),MAX(C_ConversionType_ID),"
			+ " SUM(GrandTotal), MAX(MultiplierAP), MAX(Multiplier) "
			+ "FROM	C_Invoice_v "	//	corrected for CM / Split Payment
			+ "WHERE C_Invoice_ID=?";
		if (p_C_InvoicePaySchedule_ID != 0)
			sql += " AND C_InvoicePaySchedule_ID=?";
		PreparedStatement pstmt = Compiere.getInstance().prepareStatement(sql);
		pstmt.setInt(1, p_C_Invoice_ID);
		if (p_C_InvoicePaySchedule_ID != 0)
			pstmt.setInt(2, p_C_InvoicePaySchedule_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
		{
			C_Currency_ID = rs.getInt(1);
			C_ConversionType_ID = rs.getInt(2);
			//jz C_ConversionType_ID may be null 
			if (rs.wasNull())
				C_ConversionType_ID = 0;
			GrandTotal = rs.getBigDecimal(3);
			MultiplierAP = rs.getBigDecimal(4);
			MultiplierCM = rs.getBigDecimal(5);
		}
		rs.close();
		pstmt.close();
		//	No Invoice
		if (GrandTotal == null)
			return 0;
		//jz return null;

		BigDecimal paidAmt = BigDecimal.valueOf(allocatedAmt(p_C_Invoice_ID, C_Currency_ID, 
			C_ConversionType_ID, MultiplierAP));
        BigDecimal TotalOpenAmt = GrandTotal.subtract(paidAmt);
        
		/**
		 	GrandTotal	Paid	TotalOpen	Remaining	Due		x
		 	100			0		100			=0
		 1a						=50-0					50		x
		 1b									=0-50 =0	50
		 2a									=0-50 =0	50		
		 2b						=50-0					50 		x
		 --
		 	100			10		100			=10
		 1a						=50-10					50		x
		 1b									=10-50 =0	50
		 2a									=10-50 =0	50		
		 2b						=50-0					50 		x
		 --
		 	100			60		100			=60
		 1a						=50-60 =0					50		x
		 1b									=60-50 		50
		 2a									=60-50 =10	50		
		 2b						=50-10					50 		x
		 --
		**/
		
		//	Do we have a Payment Schedule ?
		if (p_C_InvoicePaySchedule_ID > 0)	//	if not valid = lists invoice amount
		{
			TotalOpenAmt = GrandTotal;
			BigDecimal remainingAmt = paidAmt;
			sql = "SELECT C_InvoicePaySchedule_ID, DueAmt "
				+ "FROM C_InvoicePaySchedule "
				+ "WHERE C_Invoice_ID=?"
				+ " AND IsValid='Y' "
				+ "ORDER BY DueDate";
			pstmt = Compiere.getInstance().prepareStatement(sql);
			pstmt.setInt(1, p_C_Invoice_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				int C_InvoicePaySchedule_ID = rs.getInt(1); 
				BigDecimal DueAmt = rs.getBigDecimal(2);
				//
				if (C_InvoicePaySchedule_ID == p_C_InvoicePaySchedule_ID)
				{
					if (DueAmt.signum() > 0)	//	positive
					{
						if (DueAmt.compareTo(remainingAmt) < 0)		// paid more 
							TotalOpenAmt = Compiere.ZERO;
						else
							TotalOpenAmt = DueAmt.multiply(MultiplierCM)
								.subtract(remainingAmt);
					}
					else
					{
						if (DueAmt.compareTo(remainingAmt) > 0)		// paid more 
							TotalOpenAmt = Compiere.ZERO;
						else
							TotalOpenAmt = DueAmt.multiply(MultiplierCM)
								.add(remainingAmt);
					}
				}
				else
				{
					if (DueAmt.signum() > 0)	//	positive
					{
						remainingAmt = remainingAmt.subtract(DueAmt);
						if (remainingAmt.signum() < 0)
							remainingAmt = Compiere.ZERO;
					}
					else
					{
						remainingAmt = remainingAmt.add(DueAmt);
						if (remainingAmt.signum() < 0)
							remainingAmt = Compiere.ZERO;
					}
				}
			}
			rs.close();
			pstmt.close();
		}	//	Invoice Schedule
	
		//	Rounding
		double dTotalOpenAmt = Currency.round(TotalOpenAmt, C_Currency_ID, null);
		
		//	Ignore Penny if there is a payment
		if (paidAmt.signum() != 0)
		{
			//jz double open = TotalOpenAmt.doubleValue();
			if (dTotalOpenAmt >= -0.01 && dTotalOpenAmt <= 0.01)
				dTotalOpenAmt = 0;
		}
		//
		return dTotalOpenAmt;
	}	//	open
	
	
	/**
	 * 	Get Invoice paid(allocated) amount.
	 * 	- invoicePaid
	 *	@param p_C_Invoice_ID invoice
	 *	@param p_C_Currency_ID currency
	 *	@param p_MultiplierAP multiplier
	 *	@return paid amount
	 *	@throws SQLException
	 */
	public static double  paid (int p_C_Invoice_ID, int p_C_Currency_ID, int p_MultiplierAP)
		throws SQLException
	{
		//	Invalid Parameters
		if (p_C_Invoice_ID == 0 || p_C_Currency_ID == 0)
			return 0;//jz
		//	Parameters
		BigDecimal MultiplierAP = new BigDecimal((double)p_MultiplierAP);
		if (p_MultiplierAP == 0)
			MultiplierAP = Compiere.ONE;
		int C_ConversionType_ID = 0;
		
		//	Calculate Allocated Amount
		BigDecimal paymentAmt = BigDecimal.valueOf(allocatedAmt(p_C_Invoice_ID, 
			p_C_Currency_ID, C_ConversionType_ID, MultiplierAP));//jz
		return Currency.round(paymentAmt, p_C_Currency_ID, null);
	}	//	paid

	
	/**
	 * 	Get Allocated Amt (not directly used)
	 *	@param C_Invoice_ID invoice
	 *	@param C_Currency_ID currency
	 *	@param C_ConversionType_ID conversion type
	 *	@param MultiplierAP multiplier
	 *	@return allocated amount
	 *	@throws SQLException
	 */
	public static double  allocatedAmt(int C_Invoice_ID, 
		int C_Currency_ID, int C_ConversionType_ID, BigDecimal MultiplierAP)
		throws SQLException
	{
		//	Calculate Allocated Amount
		BigDecimal paidAmt = Compiere.ZERO;
		String sql = "SELECT a.AD_Client_ID, a.AD_Org_ID,"
			+ " al.Amount, al.DiscountAmt, al.WriteOffAmt,"
			+ " a.C_Currency_ID, a.DateTrx "
			+ "FROM C_AllocationLine al"
			+ " INNER JOIN C_AllocationHdr a ON (al.C_AllocationHdr_ID=a.C_AllocationHdr_ID) "
			+ "WHERE al.C_Invoice_ID=?"
			+ " AND a.IsActive='Y'";
		PreparedStatement pstmt = Compiere.getInstance().prepareStatement(sql);
		pstmt.setInt(1, C_Invoice_ID);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next())
		{
			int AD_Client_ID = rs.getInt(1);
			int AD_Org_ID = rs.getInt(2);
			BigDecimal Amount = rs.getBigDecimal(3);
			BigDecimal DiscountAmt = rs.getBigDecimal(4);
			BigDecimal WriteOffAmt = rs.getBigDecimal(5);
			int C_CurrencyFrom_ID = rs.getInt(6);
			Timestamp DateTrx = rs.getTimestamp(7);
			//
			BigDecimal invAmt = Amount.add(DiscountAmt).add(WriteOffAmt);
			BigDecimal allocation = BigDecimal.valueOf(Currency.convert(invAmt.multiply(MultiplierAP),
				C_CurrencyFrom_ID, C_Currency_ID, DateTrx, C_ConversionType_ID, 
				AD_Client_ID, AD_Org_ID));//jz
			if (allocation != null)
				paidAmt = paidAmt.add(allocation);
		}
		rs.close();
		pstmt.close();
		//
		return paidAmt.doubleValue();//jz
	}	//	getAllocatedAmt

	
	/**
	 * 	Get Invoice discount.
	 * 	C_Invoice_Discount - invoiceDiscount
	 *	@param p_C_Invoice_ID invoice
	 *	@param p_PayDate pay date
	 *	@param p_C_InvoicePaySchedule_ID pay schedule
	 *	@return discount amount or null
	 *	@throws SQLException
	 */
	public static double  discount (int p_C_Invoice_ID, 
		Timestamp p_PayDate, int p_C_InvoicePaySchedule_ID)
		throws SQLException
	{
		//	Parameters
		if (p_C_Invoice_ID == 0)
			return 0;//
		Timestamp PayDate = p_PayDate;
		if (PayDate == null)
			PayDate = new Timestamp (System.currentTimeMillis());
		PayDate = Compiere.trunc(PayDate);
		
		//	Invoice Info
		boolean IsDiscountLineAmt = false;
		BigDecimal GrandTotal = null;
		BigDecimal TotalLines = null;
		int C_PaymentTerm_ID = 0;
		Timestamp DateInvoiced = null;
		boolean IsPayScheduleValid = false;
		int C_Currency_ID = 0;
		String sql = "SELECT ci.IsDiscountLineAmt, i.GrandTotal, i.TotalLines, "
			+ " i.C_PaymentTerm_ID, i.DateInvoiced, i.IsPayScheduleValid, i.C_Currency_ID "
			+ "FROM C_Invoice i"
			+ " INNER JOIN AD_ClientInfo ci ON (ci.AD_Client_ID=i.AD_Client_ID) "
			+ "WHERE i.C_Invoice_ID=?";
		PreparedStatement pstmt = Compiere.getInstance().prepareStatement(sql);
		pstmt.setInt(1, p_C_Invoice_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
		{
			IsDiscountLineAmt = "Y".equals(rs.getString(1));
			GrandTotal = rs.getBigDecimal(2);
			TotalLines = rs.getBigDecimal(3);
			C_PaymentTerm_ID = rs.getInt(4);
			DateInvoiced = rs.getTimestamp(5);
			IsPayScheduleValid = "Y".equals(rs.getString(6));
			C_Currency_ID = rs.getInt(7);
		}
		rs.close();
		pstmt.close();
		//	Not found
		if (GrandTotal == null)
			return 0;//jz
		
		//	What Amount is the Discount Base?
		BigDecimal amount = GrandTotal;
		if (IsDiscountLineAmt)
			amount = TotalLines;
		
		//	Anything to discount?
		if (amount.signum() == 0)
			return 0; //jz Compiere.ZERO;
		
		//	Valid Payment Schedule (has discount)
		if (IsPayScheduleValid && p_C_InvoicePaySchedule_ID > 0)
		{
			BigDecimal discount = Compiere.ZERO;
			sql = "SELECT DiscountAmt "
				+ "FROM C_InvoicePaySchedule "
				+ "WHERE C_InvoicePaySchedule_ID=?"
				+ " AND TRUNC(DiscountDate, 'DD') <= ?";
			pstmt = Compiere.getInstance().prepareStatement(sql);
			pstmt.setInt(1, p_C_InvoicePaySchedule_ID);
			pstmt.setTimestamp(2, PayDate);
			rs = pstmt.executeQuery();
			if (rs.next())
				discount = rs.getBigDecimal(1);
			rs.close();
			pstmt.close();
			//
			return discount.doubleValue();//jz
		}

		//	return discount amount	
		return PaymentTerm.discount (amount, C_Currency_ID, 
			C_PaymentTerm_ID, DateInvoiced, PayDate);
	}	//	discount
	
	/**
	 * 	Test
	 *	@param args
	 */
	public static void main (String[] args)
	{
		
		try
		{
			//DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			//String url = "jdbc:derby://jl-laptop:1527/c:\\compiere\\compiere-all2\\derby\\compiere;create=true";
			String url = "jdbc:derby:c:\\compiere\\compiere-all2\\derby\\compiere";
			//Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			Connection conn =DriverManager.getConnection(url, "compiere", "compiere");	
			/*
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			Compiere.s_type = Compiere.TYPE_DERBY;
			Compiere.s_url = "jdbc:oracle:thin:@//dev1:1521/dev1.compiere.org";
			Compiere.s_uid = "compiere";
			Compiere.s_pwd = "compiere";
			*/
			//Compiere.s_url = "jdbc:derby://jl-laptop:1527/c:\\compiere\\compiere-all2\\derby\\compiere;create=true";
			/**	Connection User				*/
			//Compiere.s_uid = "compiere";
			/**	Connection Password			*/
			//Compiere.s_pwd = "compiere";
			
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery("SELECT invoiceOpen(C_Invoice_ID, C_InvoicePaySchedule_ID) FROM C_Invoice_v "); 
			int i=0;
			while (rs.next())
			{
				i++;
				BigDecimal bd =rs.getBigDecimal(1);
				System.out.println("invoice.open SQL i = " +i+" and bd = " + bd.toString());
			}
			s.close();
			//
			
		//	System.out.println(Invoice.open(1000000, 1000004));
		//	System.out.println(Invoice.open(1000000, 1000005));
		//	System.out.println(Invoice.open(1000001, 1000006));
		//	System.out.println(Invoice.open(1000001, 1000007));
			System.out.println("invoice.open (109,102) = " + Invoice.open (109,102));
			System.out.println("invoice.open (103,0) = " + Invoice.open (103,0));
			System.out.println(Invoice.paid(101, 100, 1));
			System.out.println(Invoice.paid(1000000, 100, 1));
			System.out.println(Invoice.paid(1000001, 100, 1));
			System.out.println(Invoice.paid(1000002, 100, 1));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			e=e.getNextException();
			e.printStackTrace();
			e=e.getNextException();
			e.printStackTrace();
			e=e.getNextException();			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	//	main	/* */
	
}	//	Invoice
