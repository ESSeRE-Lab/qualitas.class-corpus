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
package org.compiere.report;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.process.*;
import org.compiere.util.*;

/**
 *  Financial Balance Maintenance Engine
 *
 *  @author Jorg Janke
 *  @version $Id: FinBalance.java,v 1.2 2006/07/30 00:51:05 jjanke Exp $
 */
public class FinBalance extends SvrProcess
{
	/**	Logger						*/
	protected static final CLogger	s_log = CLogger.getCLogger (FinBalance.class);

	/** Acct Schema					*/
	private int			p_C_AcctSchema_ID = 0;
	/** Full recreate				*/
	private boolean		p_IsRecreate = false;
	/** Date From					*/
	private Timestamp	p_DateFrom = null;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		//	Parameter
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) {
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("C_AcctSchema_ID"))
				p_C_AcctSchema_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("IsRecreate"))
				p_IsRecreate = "Y".equals(element.getParameter());
			else if (name.equals("DateFrom"))
				p_DateFrom = (Timestamp)element.getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare


	/**
	 *  Perform process.
	 *  @return Message to be translated
	 *  @throws Exception
	 */
	@Override
	protected String doIt() throws java.lang.Exception
	{
		log.fine("C_AcctSchema_ID=" + p_C_AcctSchema_ID
			+ ",IsRecreate=" + p_IsRecreate
			+ ",DateFrom=" + p_DateFrom);

		String msg = "";
		if (p_C_AcctSchema_ID != 0)
			msg = updateBalance(getCtx(), p_C_AcctSchema_ID,
				p_IsRecreate, p_DateFrom, get_TrxName(), this);
		else
			msg = updateBalanceClient(getCtx(),
				p_IsRecreate, p_DateFrom, get_TrxName(), this);
		return msg;
	}	//	doIt

	/**
	 * 	Delete Balances
	 * 	@param AD_Client_ID client
	 * 	@param C_AcctSchema_ID	accounting schema 0 for all
	 * 	@param dateFrom null for all or first date to delete
	 * 	@param trx transaction
	 * 	@param svrPrc optional server process
	 *  @return Message to be translated
	 */
	public static String deleteBalance (int AD_Client_ID, int C_AcctSchema_ID,
		Timestamp dateFrom, Trx trx, SvrProcess svrPrc)
	{
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer ("DELETE FROM Fact_Acct_Balance WHERE AD_Client_ID=?");
		params.add(new Integer(AD_Client_ID));
		if (C_AcctSchema_ID != 0)
		{
			sql.append (" AND C_AcctSchema_ID=?");
			params.add(new Integer(C_AcctSchema_ID));
		}
		if (dateFrom != null)
		{
			sql.append(" AND DateAcct>=?");
			params.add(dateFrom);
		}
		//
		int no = DB.executeUpdate(sql.toString(), params, false, trx);
		String msg = "@Deleted@=" + no;
		s_log.info("C_AcctSchema_ID=" + C_AcctSchema_ID
			+ ",DateAcct=" + dateFrom
			+ " #=" + no);
		if (svrPrc != null)
			svrPrc.addLog(0, dateFrom, new BigDecimal(no), "Deleted");
		//
		return msg;
	}	//	deleteBalance

	/**
	 * 	Update / Create Balances.
	 * 	Called from FinReport, FactAcctReset (indirect)
	 * 	@param AD_Client_ID client
	 * 	@param C_AcctSchema_ID	accounting schema 0 for all
	 * 	@param deleteFirst delete balances first
	 * 	@param dateFrom null for all or first date to delete/calculate
	 * 	@param trx transaction
	 * 	@param svrPrc optional server process
	 *  @return Message to be translated
	 */
	public static String updateBalance (Ctx ctx, int C_AcctSchema_ID,
		boolean deleteFirst, Timestamp dateFrom, Trx trx,
		SvrProcess svrPrc)
	{
		s_log.info("C_AcctSchema_ID=" + C_AcctSchema_ID
			+ ",DeleteFirst=" + deleteFirst
			+ "DateFrom=" + dateFrom);
		long start = System.currentTimeMillis();

		ArrayList<MFactAccumulation> accums = MFactAccumulation.getAll(ctx, C_AcctSchema_ID);
		dateFrom = MFactAccumulation.getDateFrom(accums, dateFrom);
		//	Potentially updating accumulated History - need to delete it.
		if (!deleteFirst && (accums.size() > 0))
			deleteFirst = true;

		if (deleteFirst)
			deleteBalance(ctx.getAD_Client_ID(), C_AcctSchema_ID,
				dateFrom, trx, svrPrc);

		//	Update existing
		ArrayList<Object> params = new ArrayList<Object>();
		String sql = "UPDATE Fact_Acct_Balance ab "
			+ "SET (AmtAcctDr, AmtAcctCr, Qty)= "
			+ "(SELECT COALESCE(SUM(AmtAcctDr),0), COALESCE(SUM(AmtAcctCr),0), COALESCE(SUM(Qty),0) "
			+ "FROM Fact_Acct a "
			+ "WHERE a.AD_Client_ID=ab.AD_Client_ID AND a.AD_Org_ID=ab.AD_Org_ID"
			+ " AND a.C_AcctSchema_ID=ab.C_AcctSchema_ID AND TRUNC(a.DateAcct)=TRUNC(ab.DateAcct)"
			+ " AND a.Account_ID=ab.Account_ID AND a.PostingType=ab.PostingType"
			+ " AND COALESCE(a.M_Product_ID,0)=COALESCE(ab.M_Product_ID,0) AND COALESCE(a.C_BPartner_ID,0)=COALESCE(ab.C_BPartner_ID,0)"
			+ " AND COALESCE(a.C_Project_ID,0)=COALESCE(ab.C_Project_ID,0) AND COALESCE(a.AD_OrgTrx_ID,0)=COALESCE(ab.AD_OrgTrx_ID,0)"
			+ " AND COALESCE(a.C_SalesRegion_ID,0)=COALESCE(ab.C_SalesRegion_ID,0) AND COALESCE(a.C_Activity_ID,0)=COALESCE(ab.C_Activity_ID,0)"
			+ " AND COALESCE(a.C_Campaign_ID,0)=COALESCE(ab.C_Campaign_ID,0) AND COALESCE(a.C_LocTo_ID,0)=COALESCE(ab.C_LocTo_ID,0) AND COALESCE(a.C_LocFrom_ID,0)=COALESCE(ab.C_LocFrom_ID,0)"
			+ " AND COALESCE(a.User1_ID,0)=COALESCE(ab.User1_ID,0) AND COALESCE(a.User2_ID,0)=COALESCE(ab.User2_ID,0) AND COALESCE(a.GL_Budget_ID,0)=COALESCE(ab.GL_Budget_ID,0) "
			+ "GROUP BY AD_Client_ID,AD_Org_ID, C_AcctSchema_ID, TRUNC(DateAcct),"
			+ " Account_ID, PostingType, M_Product_ID, C_BPartner_ID,"
			+ " C_Project_ID, AD_OrgTrx_ID, C_SalesRegion_ID, C_Activity_ID,"
			+ " C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID, User1_ID, User2_ID, GL_Budget_ID) "
			+ "WHERE C_AcctSchema_ID=?";
		params.add(new Integer(C_AcctSchema_ID));
		if (dateFrom != null)
		{
			sql += " AND DateAcct>=?";
			params.add(dateFrom);
		}
		sql += " AND EXISTS (SELECT 1 FROM Fact_Acct a "
				+ "WHERE a.AD_Client_ID=ab.AD_Client_ID AND a.AD_Org_ID=ab.AD_Org_ID"
				+ " AND a.C_AcctSchema_ID=ab.C_AcctSchema_ID AND TRUNC(a.DateAcct)=TRUNC(ab.DateAcct)"
				+ " AND a.Account_ID=ab.Account_ID AND a.PostingType=ab.PostingType"
				+ " AND COALESCE(a.M_Product_ID,0)=COALESCE(ab.M_Product_ID,0) AND COALESCE(a.C_BPartner_ID,0)=COALESCE(ab.C_BPartner_ID,0)"
				+ " AND COALESCE(a.C_Project_ID,0)=COALESCE(ab.C_Project_ID,0) AND COALESCE(a.AD_OrgTrx_ID,0)=COALESCE(ab.AD_OrgTrx_ID,0)"
				+ "	AND COALESCE(a.C_SalesRegion_ID,0)=COALESCE(ab.C_SalesRegion_ID,0) AND COALESCE(a.C_Activity_ID,0)=COALESCE(ab.C_Activity_ID,0)"
				+ " AND COALESCE(a.C_Campaign_ID,0)=COALESCE(ab.C_Campaign_ID,0) AND COALESCE(a.C_LocTo_ID,0)=COALESCE(ab.C_LocTo_ID,0) AND COALESCE(a.C_LocFrom_ID,0)=COALESCE(ab.C_LocFrom_ID,0)"
				+ " AND COALESCE(a.User1_ID,0)=COALESCE(ab.User1_ID,0) AND COALESCE(a.User2_ID,0)=COALESCE(ab.User2_ID,0) AND COALESCE(a.GL_Budget_ID,0)=COALESCE(ab.GL_Budget_ID,0) "
				/* maybe redundant
				+ "GROUP BY AD_Client_ID,AD_Org_ID,"
				+ " C_AcctSchema_ID, TRUNC(DateAcct,'DD'),"
				+ " Account_ID, PostingType,"
				+ " M_Product_ID, C_BPartner_ID,"
				+ " C_Project_ID, AD_OrgTrx_ID,"
				+ " C_SalesRegion_ID, C_Activity_ID,"
				+ " C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID,"
				+ " User1_ID, User2_ID, GL_Budget_ID
				*/
				+ ")";
		if (!deleteFirst)
		{
			int no = DB.executeUpdate(sql, params, false, trx);
			s_log.config("Updates=" + no);
			if (svrPrc != null)
				svrPrc.addLog(0, dateFrom, new BigDecimal(no), "Updates");
		}

		/** Insert		**/
		params = new ArrayList<Object>();
		sql = "INSERT INTO Fact_Acct_Balance "
			+ "(AD_Client_ID, AD_Org_ID, C_AcctSchema_ID, DateAcct,"
			+ " Account_ID, PostingType, M_Product_ID, C_BPartner_ID,"
			+ "	C_Project_ID, AD_OrgTrx_ID,	C_SalesRegion_ID,C_Activity_ID,"
			+ " C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID, User1_ID, User2_ID, GL_Budget_ID,"
			+ " AmtAcctDr, AmtAcctCr, Qty) "
		//
			+ "SELECT AD_Client_ID, AD_Org_ID, C_AcctSchema_ID, TRUNC(DateAcct),"
			+ " Account_ID, PostingType, M_Product_ID, C_BPartner_ID,"
			+ " C_Project_ID, AD_OrgTrx_ID, C_SalesRegion_ID,C_Activity_ID,"
			+ " C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID, User1_ID, User2_ID, GL_Budget_ID,"
			+ " COALESCE(SUM(AmtAcctDr),0), COALESCE(SUM(AmtAcctCr),0), COALESCE(SUM(Qty),0) "
			+ "FROM Fact_Acct a "
			+ "WHERE C_AcctSchema_ID=?";
		params.add(new Integer(C_AcctSchema_ID));
		if (dateFrom != null)
		{
			sql += " AND DateAcct>=?";
			params.add(dateFrom);
		}
		if (!deleteFirst)
			sql += " AND NOT EXISTS (SELECT 1 FROM Fact_Acct_Balance x "
				+ "WHERE a.AD_Client_ID=x.AD_Client_ID AND a.AD_Org_ID=x.AD_Org_ID"
				+ " AND a.C_AcctSchema_ID=x.C_AcctSchema_ID AND TRUNC(a.DateAcct)=TRUNC(x.DateAcct)"
				+ " AND a.Account_ID=x.Account_ID AND a.PostingType=x.PostingType"
				+ " AND COALESCE(a.M_Product_ID,0)=COALESCE(x.M_Product_ID,0) AND COALESCE(a.C_BPartner_ID,0)=COALESCE(x.C_BPartner_ID,0)"
				+ " AND COALESCE(a.C_Project_ID,0)=COALESCE(x.C_Project_ID,0) AND COALESCE(a.AD_OrgTrx_ID,0)=COALESCE(x.AD_OrgTrx_ID,0)"
				+ " AND COALESCE(a.C_SalesRegion_ID,0)=COALESCE(x.C_SalesRegion_ID,0) AND COALESCE(a.C_Activity_ID,0)=COALESCE(x.C_Activity_ID,0)"
				+ " AND COALESCE(a.C_Campaign_ID,0)=COALESCE(x.C_Campaign_ID,0) AND COALESCE(a.C_LocTo_ID,0)=COALESCE(x.C_LocTo_ID,0) AND COALESCE(a.C_LocFrom_ID,0)=COALESCE(x.C_LocFrom_ID,0)"
				+ " AND COALESCE(a.User1_ID,0)=COALESCE(x.User1_ID,0) AND COALESCE(a.User2_ID,0)=COALESCE(x.User2_ID,0) AND COALESCE(a.GL_Budget_ID,0)=COALESCE(x.GL_Budget_ID,0) )";
		sql += " GROUP BY AD_Client_ID,AD_Org_ID, C_AcctSchema_ID, TRUNC(DateAcct),"
			+ " Account_ID, PostingType, M_Product_ID, C_BPartner_ID,"
			+ " C_Project_ID, AD_OrgTrx_ID, C_SalesRegion_ID, C_Activity_ID,"
			+ " C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID, User1_ID, User2_ID, GL_Budget_ID";

		int no = DB.executeUpdate(sql, params, false, trx);
		s_log.config("Inserts=" + no);
		if (svrPrc != null)
			svrPrc.addLog(0, dateFrom, new BigDecimal(no), "Inserts");

		//	Accumulation
		Timestamp acctToDate = null;
		Timestamp acctFromDate = dateFrom; //	greater than (dateTo is LE)
		for (MFactAccumulation accum : accums)
        {
			String type = accum.getBalanceAccumulation();
			boolean ok = true;
			if (X_Fact_Accumulation.BALANCEACCUMULATION_Daily.equals(type))
				;
			else if (X_Fact_Accumulation.BALANCEACCUMULATION_CalendarWeek.equals(type))
			{
				acctToDate = accum.getDateTo();
				ok = summarizeBalance(TimeUtil.TRUNC_WEEK, C_AcctSchema_ID,
					acctFromDate, acctToDate, trx, svrPrc);
				acctFromDate = accum.getDateTo();
			}
			else if (X_Fact_Accumulation.BALANCEACCUMULATION_CalendarMonth.equals(type))
			{
				acctToDate = accum.getDateTo();
				ok = summarizeBalance(TimeUtil.TRUNC_MONTH, C_AcctSchema_ID,
					acctFromDate, acctToDate, trx,svrPrc);
				acctFromDate = accum.getDateTo();
			}
			else if (X_Fact_Accumulation.BALANCEACCUMULATION_PeriodOfACompiereCalendar.equals(type))
			{
				int C_Calendar_ID = accum.getC_Calendar_ID();
				if (C_Calendar_ID != 0)
				{
					acctToDate = accum.getDateTo();


					acctFromDate = accum.getDateTo();
				}
			}
			if (!ok)
			{
				s_log.warning("Check Errors and re-run");
			}
        }

		start = System.currentTimeMillis() - start;
		s_log.info((start/1000) + " sec");
		return "#" + no;
	}	//	updateBalance

	/**
	 * 	Update Balances based on Truncate Function
	 *	@param trunc TimeUtil.TRUNC_ (Oracle function)
	 *	@param C_AcctSchema_ID acct schema
	 *	@param acctFromDate optional from
	 *	@param acctToDate to
	 *	@param p_trx transaction name
	 * 	@param svrPrc optional server process
	 *	@return true if no error
	 */
	private static boolean summarizeBalance(String trunc, int C_AcctSchema_ID,
		Timestamp acctFromDate, Timestamp acctToDate, Trx trx,
		SvrProcess svrPrc)
	{
		SimpleDateFormat format = DisplayType.getDateFormat();
		String toDate = "\u2264" + format.format(acctToDate) + ": ";
		String whereTrunc = "TRUNC(DateAcct,'" + trunc + "')";
		StringBuffer whereRange = new StringBuffer("WHERE ");
		if (acctFromDate != null)
			whereRange.append("DateAcct>=").append(DB.TO_DATE(acctFromDate, true))
				.append(" AND ");
		whereRange.append("DateAcct<=").append(DB.TO_DATE(acctToDate, true));

		//	Insert zeroes if not exists
		String sql = "INSERT INTO Fact_Acct_Balance"
			+ " (AD_Client_ID, AD_Org_ID, C_AcctSchema_ID, DateAcct,"
			+ " Account_ID, PostingType, M_Product_ID, C_BPartner_ID,"
			+ " C_Project_ID, AD_OrgTrx_ID,	C_SalesRegion_ID,C_Activity_ID,"
			+ " C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID, User1_ID, User2_ID, GL_Budget_ID,"
			+ " AmtAcctDr, AmtAcctCr, Qty) "
		//	Insert
			+ "SELECT AD_Client_ID, AD_Org_ID, C_AcctSchema_ID, " + whereTrunc
			+ ", Account_ID, PostingType, M_Product_ID, C_BPartner_ID,"
			+ " C_Project_ID, AD_OrgTrx_ID, C_SalesRegion_ID,C_Activity_ID,"
			+ " C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID, User1_ID, User2_ID, GL_Budget_ID,"
			+ " 0,0,0 "
			+ "FROM Fact_Acct_Balance a "
			+ whereRange
			+ " AND C_AcctSchema_ID=" + C_AcctSchema_ID
			//
			+ " AND NOT EXISTS (SELECT 1 FROM Fact_Acct_Balance x "
				+ "WHERE a.AD_Client_ID=x.AD_Client_ID AND a.AD_Org_ID=x.AD_Org_ID"
				+ " AND a.C_AcctSchema_ID=x.C_AcctSchema_ID AND TRUNC(a.DateAcct,'" + trunc + "')=TRUNC(x.DateAcct)"
				+ " AND a.Account_ID=x.Account_ID AND a.PostingType=x.PostingType"
				+ " AND COALESCE(a.M_Product_ID,0)=COALESCE(x.M_Product_ID,0) AND COALESCE(a.C_BPartner_ID,0)=COALESCE(x.C_BPartner_ID,0)"
				+ " AND COALESCE(a.C_Project_ID,0)=COALESCE(x.C_Project_ID,0) AND COALESCE(a.AD_OrgTrx_ID,0)=COALESCE(x.AD_OrgTrx_ID,0)"
				+ " AND COALESCE(a.C_SalesRegion_ID,0)=COALESCE(x.C_SalesRegion_ID,0) AND COALESCE(a.C_Activity_ID,0)=COALESCE(x.C_Activity_ID,0)"
				+ " AND COALESCE(a.C_Campaign_ID,0)=COALESCE(x.C_Campaign_ID,0) AND COALESCE(a.C_LocTo_ID,0)=COALESCE(x.C_LocTo_ID,0) AND COALESCE(a.C_LocFrom_ID,0)=COALESCE(x.C_LocFrom_ID,0)"
				+ " AND COALESCE(a.User1_ID,0)=COALESCE(x.User1_ID,0) AND COALESCE(a.User2_ID,0)=COALESCE(x.User2_ID,0) AND COALESCE(a.GL_Budget_ID,0)=COALESCE(x.GL_Budget_ID,0) )"
			//
			+ " GROUP BY AD_Client_ID, AD_Org_ID, C_AcctSchema_ID, " + whereTrunc
			+ ", Account_ID, PostingType, M_Product_ID, C_BPartner_ID,"
			+ " C_Project_ID, AD_OrgTrx_ID, C_SalesRegion_ID,C_Activity_ID,"
			+ " C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID, User1_ID, User2_ID, GL_Budget_ID";
		int noInsert = DB.executeUpdate(sql, trx);
		s_log.config("Insert " + trunc + " #" + noInsert);
		if (svrPrc != null)
			svrPrc.addLog(0, acctFromDate, new BigDecimal(noInsert), toDate + "Accumulation Insert");

		//	Update
		sql = "UPDATE Fact_Acct_Balance a SET (AmtAcctDr, AmtAcctCr, Qty) = "
				+ "(SELECT COALESCE(SUM(AmtAcctDr),0), COALESCE(SUM(AmtAcctCr),0), COALESCE(SUM(Qty),0) "
				+ "FROM Fact_Acct_Balance x "
				+ whereRange
				+ " AND TRUNC(a.DateAcct,'" + trunc + "')=TRUNC(x.DateAcct,'" + trunc + "')"
				+ " AND a.AD_Client_ID=x.AD_Client_ID AND a.AD_Org_ID=x.AD_Org_ID"
				+ " AND a.C_AcctSchema_ID=x.C_AcctSchema_ID"
				+ " AND a.Account_ID=x.Account_ID AND a.PostingType=x.PostingType"
				+ " AND COALESCE(a.M_Product_ID,0)=COALESCE(x.M_Product_ID,0) AND COALESCE(a.C_BPartner_ID,0)=COALESCE(x.C_BPartner_ID,0)"
				+ " AND COALESCE(a.C_Project_ID,0)=COALESCE(x.C_Project_ID,0) AND COALESCE(a.AD_OrgTrx_ID,0)=COALESCE(x.AD_OrgTrx_ID,0)"
				+ " AND COALESCE(a.C_SalesRegion_ID,0)=COALESCE(x.C_SalesRegion_ID,0) AND COALESCE(a.C_Activity_ID,0)=COALESCE(x.C_Activity_ID,0)"
				+ " AND COALESCE(a.C_Campaign_ID,0)=COALESCE(x.C_Campaign_ID,0) AND COALESCE(a.C_LocTo_ID,0)=COALESCE(x.C_LocTo_ID,0) AND COALESCE(a.C_LocFrom_ID,0)=COALESCE(x.C_LocFrom_ID,0)"
				+ " AND COALESCE(a.User1_ID,0)=COALESCE(x.User1_ID,0) AND COALESCE(a.User2_ID,0)=COALESCE(x.User2_ID,0) AND COALESCE(a.GL_Budget_ID,0)=COALESCE(x.GL_Budget_ID,0) "
				+ "GROUP BY AD_Client_ID, AD_Org_ID, C_AcctSchema_ID, " + whereTrunc
				+ ", Account_ID, PostingType, M_Product_ID, C_BPartner_ID,"
				+ " C_Project_ID, AD_OrgTrx_ID, C_SalesRegion_ID,C_Activity_ID,"
				+ " C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID, User1_ID, User2_ID, GL_Budget_ID"
				+ ") "
			//	Update WHERE
			+ whereRange
			+ " AND C_AcctSchema_ID=" + C_AcctSchema_ID
			+ " AND DateAcct=" + whereTrunc;
		s_log.finest(sql);
		int noUpdate = DB.executeUpdate(sql, trx);
		s_log.config("Update " + trunc + " #" + noUpdate);
		if (svrPrc != null)
			svrPrc.addLog(0, acctFromDate, new BigDecimal(noUpdate), toDate + "Accumulation Update");

		if (noUpdate == 0)	//	nothing to delete if nothing summarized
			return true;

		//	Delete
		sql = "DELETE FROM Fact_Acct_Balance "
			+ whereRange
			+ " AND C_AcctSchema_ID=" + C_AcctSchema_ID
			+ " AND DateAcct<>" + whereTrunc;
		s_log.finest(sql);
		int noDelete = DB.executeUpdate(sql, trx);
		float factor = 1;
		if (noInsert != 0)
			factor = noDelete / noInsert;
		else if (noUpdate != 0)
			factor = noDelete / noUpdate;
		s_log.config("Delete " + trunc + " #" + noDelete + " - Factor=" + factor);
		if (svrPrc != null)
			svrPrc.addLog(0, acctFromDate, new BigDecimal(noDelete), toDate + "Accumulation Delete - Factor=" + factor);

		return (noUpdate >= 0) && (noDelete >= 0);
	}	//	summarizeBalance


	/**
	 * 	Update Balance of Client
	 *	@param ctx context
	 *	@param deleteFirst delete first
	 * 	@param dateFrom null for all or first date to delete/calculate
	 * 	@param trx transaction
	 * 	@param svrPrc optional server process
	 *	@return info
	 */
	public static String updateBalanceClient (Ctx ctx,
		boolean deleteFirst, Timestamp dateFrom, Trx trx, SvrProcess svrPrc)
	{
		int AD_Client_ID = ctx.getAD_Client_ID();
		StringBuffer info = new StringBuffer();
		MAcctSchema[] ass = MAcctSchema.getClientAcctSchema(ctx, AD_Client_ID);
		for (MAcctSchema as : ass)
		{
			if (info.length() > 0)
				info.append(" - ");
			String msg = updateBalance(ctx, as.getC_AcctSchema_ID(),
				deleteFirst, dateFrom, trx, svrPrc);
			info.append(as.getName()).append(":").append(msg);
		}
		return info.toString();
	}	//	updateBalanceClient


}	//	FinBalance
