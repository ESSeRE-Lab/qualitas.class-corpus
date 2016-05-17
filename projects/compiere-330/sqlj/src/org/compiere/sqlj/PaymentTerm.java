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
package org.compiere.sqlj;

import java.math.*;
import java.sql.*;
import java.util.*;


/**
 *	SQLJ Payment Term related Functions
 *	
 *  @author Jorg Janke
 *  @version $Id: PaymentTerm.java,v 1.3 2006/07/30 00:59:07 jjanke Exp $
 */
public class PaymentTerm
{
	/**
	 * 	Get Due Days.
	 	SELECT Name, paymentTermDueDays(C_PaymentTerm_ID, SysDate, SysDate) "DueDays" FROM C_PaymentTerm
	 *	@param p_C_PaymentTerm_ID payment term
	 *	@param p_DocDate document date
	 *	@param p_PayDate payment date (or today)
	 *	@return days due
	 *	@throws SQLException
	 */
	public static int dueDays (int p_C_PaymentTerm_ID,
		Timestamp p_DocDate, Timestamp p_PayDate)
		throws SQLException
	{
		//	Parameter
		if (p_C_PaymentTerm_ID == 0 || p_DocDate == null)
			return 0;
		//	Pay Date
		Timestamp PayDate = p_PayDate;
		if (PayDate == null)
			PayDate = new Timestamp(System.currentTimeMillis());
		PayDate = Compiere.trunc(PayDate);
		
		//	Get Due Date
		Timestamp DueDate = null;
		String sql = "SELECT * "
			+ "FROM C_PaymentTerm "
			+ "WHERE C_PaymentTerm_ID=?";
		PreparedStatement pstmt = Compiere.prepareStatement(sql);
		pstmt.setInt(1, p_C_PaymentTerm_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
		{
			boolean IsDueFixed = "Y".equals(rs.getString("IsDueFixed"));
			//
			if (IsDueFixed)
			{
				int FixMonthDay = rs.getInt("FixMonthDay");
				int FixMonthOffset = rs.getInt("FixMonthOffset");
				int FixMonthCutoff = rs.getInt("FixMonthCutoff");
				//
				DueDate = calculateDateDue (p_DocDate, FixMonthDay, FixMonthOffset, FixMonthCutoff);
			}
			else
			{
				int NetDays = rs.getInt("NetDays");
				DueDate = Compiere.addDays(p_DocDate, NetDays);
			}
		}
		rs.close();
		pstmt.close();

		//	
		if (DueDate == null)
			return 0;
		return Compiere.getDaysBetween(DueDate, PayDate);
	}	//	dueDays

	/**
	 * 	Get Due Days of invoice
	 *	@param p_C_Invoice_ID Invoice
	 *	@param p_PayDate paument date (or today)
	 *	@return days due
	 *	@throws SQLException
	 */
	public static int invoiceDueDays (int p_C_Invoice_ID, Timestamp p_PayDate)
		throws SQLException
	{
		//	Parameter
		if (p_C_Invoice_ID == 0)
			return 0;
		int retValue = 0;
		
		String sql = "SELECT C_PaymentTerm_ID, DateInvoiced "
			+ "FROM C_Invoice "
			+ "WHERE C_Invoice_ID=?";
		PreparedStatement pstmt = Compiere.prepareStatement(sql);
		pstmt.setInt(1, p_C_Invoice_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
		{
			int C_PaymentTerm_ID = rs.getInt(1);
			Timestamp DocDate = rs.getTimestamp(2);
			retValue = dueDays(C_PaymentTerm_ID, DocDate, p_PayDate);
		}
		rs.close();
		pstmt.close();
		
		return retValue;
	}	//	invoiceDueDays

	/**
	 * 	Get Due Date.
	 	SELECT Name, paymentTermDueDate(C_PaymentTerm_ID, SysDate) "DueDate" FROM C_PaymentTerm
	 *	@param p_C_PaymentTerm_ID payment term
	 *	@param p_DocDate document date
	 *	@return due date
	 *	@throws SQLException
	 */
	public static Timestamp dueDate (int p_C_PaymentTerm_ID,
		Timestamp p_DocDate)
		throws SQLException
	{
		//	Parameter
		if (p_C_PaymentTerm_ID == 0 || p_DocDate == null)
			return null;
		//	Due Date
		Timestamp DueDate = Compiere.trunc(p_DocDate);
		
		//	Get Due Date
		String sql = "SELECT * "
			+ "FROM C_PaymentTerm "
			+ "WHERE C_PaymentTerm_ID=?";
		PreparedStatement pstmt = Compiere.prepareStatement(sql);
		pstmt.setInt(1, p_C_PaymentTerm_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
		{
			boolean IsDueFixed = "Y".equals(rs.getString("IsDueFixed"));
			//
			if (IsDueFixed)
			{
				int FixMonthDay = rs.getInt("FixMonthDay");
				int FixMonthOffset = rs.getInt("FixMonthOffset");
				int FixMonthCutoff = rs.getInt("FixMonthCutoff");
				//
				DueDate = calculateDateDue (p_DocDate, FixMonthDay, FixMonthOffset, FixMonthCutoff);
			}
			else
			{
				int NetDays = rs.getInt("NetDays");
				if (NetDays != 0)
					DueDate = Compiere.addDays(DueDate, NetDays);
			}
		}
		rs.close();
		pstmt.close();

		//	
		return DueDate;
	}	//	dueDate

	/**
	 * 	Get Invoice Due Date
	 *	@param p_C_Invoice_ID payment term
	 *	@return due date
	 *	@throws SQLException
	 */
	public static Timestamp invoiceDueDate (int p_C_Invoice_ID)
		throws SQLException
	{
		//	Parameter
		if (p_C_Invoice_ID == 0)
			return null;
		//	Due Date
		Timestamp DueDate = null;
		
		String sql = "SELECT C_PaymentTerm_ID, DateInvoiced "
			+ "FROM C_Invoice "
			+ "WHERE C_Invoice_ID=?";
		PreparedStatement pstmt = Compiere.prepareStatement(sql);
		pstmt.setInt(1, p_C_Invoice_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
		{
			int C_PaymentTerm_ID = rs.getInt(1);
			Timestamp DocDate = rs.getTimestamp(2);
			DueDate = dueDate(C_PaymentTerm_ID, DocDate);
		}
		rs.close();
		pstmt.close();
		//	
		return DueDate;
	}	//	invoiceDueDate

	/**
	 * 	Calculate Date
	 *	@param DocDate document date
	 *	@param FixMonthDay day
	 *	@param FixMonthOffset offset
	 *	@param FixMonthCutoff cuttof
	 *	@return date due
	 */
	private static Timestamp calculateDateDue (Timestamp DocDate, int FixMonthDay, int FixMonthOffset, int FixMonthCutoff)
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(DocDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		//	Cutoff
		int maxDayCut = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		if (FixMonthCutoff > maxDayCut)	//	28-Feb
			cal.set(Calendar.DAY_OF_MONTH, maxDayCut);
		else
			cal.set(Calendar.DAY_OF_MONTH, FixMonthCutoff);
		if (DocDate.after(cal.getTime()))
			FixMonthOffset += 1;
		cal.add(Calendar.MONTH, FixMonthOffset);
		//	Due Date
		int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		if (FixMonthDay > maxDay)							//	32 -> 28
			cal.set(Calendar.DAY_OF_MONTH, maxDay);
		else if (FixMonthDay >= 30 && maxDay > FixMonthDay)	//	30 -> 31
			cal.set(Calendar.DAY_OF_MONTH, maxDay);
		else
			cal.set(Calendar.DAY_OF_MONTH, FixMonthDay);
		//
		java.util.Date temp = cal.getTime();
		return new Timestamp(temp.getTime());
	}	//	calculateDateDue

	
	/**
	 * 	Get Discount amount.
	 	SELECT C_PaymentTerm_ID, Name, paymentTermDiscount(111.1111, 100, C_PaymentTerm_ID, SysDate, SysDate) "DiscountUSD", paymentTermDiscount(111.1111, 100, C_PaymentTerm_ID, SysDate, SysDate) "DiscountJPY" FROM C_PaymentTerm
	 *	@param p_Amount amount
	 *	@param p_C_Currency_ID currency
	 *	@param p_C_PaymentTerm_ID payment term
	 *	@param p_DocDate document date
	 *	@param p_PayDate payment date
	 *	@return discount amount
	 *	@throws SQLException
	 */
	public static BigDecimal discount (BigDecimal p_Amount, int p_C_Currency_ID, 
		int p_C_PaymentTerm_ID,
		Timestamp p_DocDate, Timestamp p_PayDate)
		throws SQLException
	{
		//	No Data - No Discount
		if (p_Amount == null || p_C_PaymentTerm_ID == 0 || p_DocDate == null)
			return null;
		if (p_Amount.signum() == 0)
			return Compiere.ZERO;
		//	Parameters
		Timestamp PayDate = p_PayDate;
		if (PayDate == null)
			PayDate = new Timestamp (System.currentTimeMillis());
		PayDate = Compiere.trunc(PayDate);
		//
		BigDecimal discount = null;
		String sql = "SELECT * "
			+ "FROM C_PaymentTerm "
			+ "WHERE C_PaymentTerm_ID=?";
		PreparedStatement pstmt = Compiere.prepareStatement(sql);
		pstmt.setInt(1, p_C_PaymentTerm_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
		{
			int DiscountDays = rs.getInt("DiscountDays");
			int DiscountDays2 = rs.getInt("DiscountDays2");
			int GraceDays = rs.getInt("GraceDays");
			boolean IsNextBusinessDay = "Y".equals(rs.getString("IsNextBusinessDay"));
			BigDecimal Discount = rs.getBigDecimal("Discount");
			BigDecimal Discount2 = rs.getBigDecimal("Discount2");
			//
			Timestamp Discount1Date = Compiere.addDays(p_DocDate, DiscountDays + GraceDays);
			Timestamp Discount2Date = Compiere.addDays(p_DocDate, DiscountDays2 + GraceDays);
			//	Next Business Day
			if (IsNextBusinessDay)
			{
				Discount1Date = Compiere.nextBusinessDay(Discount1Date);
				Discount2Date = Compiere.nextBusinessDay(Discount2Date);
			}

			//	Discount 1
			if (!PayDate.after(Discount1Date))
				discount = p_Amount.multiply(Discount);
			//	Discount 2
			else if (!PayDate.after(Discount2Date))
				discount = p_Amount.multiply(Discount2);
			else
				discount = Compiere.ZERO;
			//	Divide
			if (discount.signum() != 0)
			{
				discount = discount.divide(Compiere.HUNDRED, 6, BigDecimal.ROUND_HALF_UP);
				discount = Currency.round(discount, p_C_Currency_ID, "N");
			}
		}	
		rs.close();
		pstmt.close();
		//
		return discount;
	}	//	discount
	
	
	/**
	 * 	Test
	 *	@param args ignored
	 *
	public static void main (String[] args)
	{
		
		try
		{
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			Compiere.s_type = Compiere.TYPE_ORACLE;
			Compiere.s_url = "jdbc:oracle:thin:@//dev:1521/dev.compiere.org";
		//	Compiere.s_url = "jdbc:oracle:thin:@//dev1:1521/dev1.compiere.org";
			Compiere.s_uid = "compiere";
			Compiere.s_pwd = "compiere";
			//
			Timestamp today = new Timestamp(System.currentTimeMillis());
		//	System.out.println(PaymentTerm.dueDays(1000000, today, today));
		//	System.out.println(PaymentTerm.dueDate(1000000, today));
		//	System.out.println(PaymentTerm.invoiceDueDate(1000008));
			System.out.println(PaymentTerm.discount(new BigDecimal(111.11111), 100, 106, today, today));
			System.out.println(PaymentTerm.discount(new BigDecimal(111.11111), 113, 106, today, today));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}	//	main	/* */
	
}	//	PaymentTerm
