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
import java.util.*;
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.process.*;
import org.compiere.util.*;


/**
 *	Trial Balance
 *	
 *  @author Jorg Janke
 *  @version $Id: TrialBalance.java,v 1.2 2006/07/30 00:51:05 jjanke Exp $
 */
public class TrialBalance extends SvrProcess
{
	/** AcctSchame Parameter			*/
	private int					p_C_AcctSchema_ID = 0;
	/**	Period Parameter				*/
	private int					p_C_Period_ID = 0;
	private Timestamp			p_DateAcct_From = null;
	private Timestamp			p_DateAcct_To = null;
	/**	Org Parameter					*/
	private int					p_AD_Org_ID = 0;
	/**	Account Parameter				*/
	private int					p_Account_ID = 0;
	private String				p_AccountValue_From = null;
	private String				p_AccountValue_To = null;
	/**	BPartner Parameter				*/
	private int					p_C_BPartner_ID = 0;
	/**	Product Parameter				*/
	private int					p_M_Product_ID = 0;
	/**	Project Parameter				*/
	private int					p_C_Project_ID = 0;
	/**	Activity Parameter				*/
	private int					p_C_Activity_ID = 0;
	/**	SalesRegion Parameter			*/
	private int					p_C_SalesRegion_ID = 0;
	/**	Campaign Parameter				*/
	private int					p_C_Campaign_ID = 0;
	/** Posting Type					*/
	private String				p_PostingType = "A";
	/** Hierarchy						*/
	private int					p_PA_Hierarchy_ID = 0;
	
	private int					p_AD_OrgTrx_ID = 0;
	private int					p_C_LocFrom_ID = 0;
	private int					p_C_LocTo_ID = 0;
	private int					p_User1_ID = 0;
	private int					p_User2_ID = 0;
	
	
	/**	Parameter Where Clause			*/
	private StringBuffer		m_parameterWhere = new StringBuffer();
	/**	Account							*/ 
	private MElementValue 		m_acct = null;
	/** Accounting Schema				*/
	private MAcctSchema			m_acctSchema = null;
	/** Calendar						*/
	private int					m_C_Calendar_ID = 0;
	
	/**	Start Time						*/
	private long 				m_start = System.currentTimeMillis();
	/**	Insert Statement				*/
	private static String		s_insert = "INSERT INTO T_TrialBalance "
		+ "(AD_PInstance_ID, Fact_Acct_ID,"
		+ " AD_Client_ID, AD_Org_ID, Created,CreatedBy, Updated,UpdatedBy,"
		+ " C_AcctSchema_ID, Account_ID, AccountValue, DateTrx, DateAcct, C_Period_ID,"
		+ " AD_Table_ID, Record_ID, Line_ID,"
		+ " GL_Category_ID, GL_Budget_ID, C_Tax_ID, M_Locator_ID, PostingType,"
		+ " C_Currency_ID, AmtSourceDr, AmtSourceCr, AmtSourceBalance,"
		+ " AmtAcctDr, AmtAcctCr, AmtAcctBalance, C_UOM_ID, Qty,"
		+ " M_Product_ID, C_BPartner_ID, AD_OrgTrx_ID, C_LocFrom_ID,C_LocTo_ID,"
		+ " C_SalesRegion_ID, C_Project_ID, C_Campaign_ID, C_Activity_ID,"
		+ " User1_ID, User2_ID, A_Asset_ID, Description, PA_Hierarchy_ID)";

	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		StringBuffer sb = new StringBuffer ("AD_PInstance_ID=")
			.append(getAD_PInstance_ID());
		//	Parameter
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) {
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("C_AcctSchema_ID"))
				p_C_AcctSchema_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("C_Period_ID"))
				p_C_Period_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("DateAcct"))
			{
				p_DateAcct_From = (Timestamp)element.getParameter();
				p_DateAcct_To = (Timestamp)element.getParameter_To();
			}
			else if (name.equals("PA_Hierarchy_ID"))
				p_PA_Hierarchy_ID = element.getParameterAsInt();
			else if (name.equals("AD_Org_ID"))
				p_AD_Org_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("Account_ID"))
				p_Account_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("AccountValue"))
			{
				p_AccountValue_From = (String)element.getParameter();
				p_AccountValue_To = (String)element.getParameter_To();
			}
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("M_Product_ID"))
				p_M_Product_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("C_Project_ID"))
				p_C_Project_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("C_Activity_ID"))
				p_C_Activity_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("C_SalesRegion_ID"))
				p_C_SalesRegion_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("C_Campaign_ID"))
				p_C_Campaign_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("PostingType"))
				p_PostingType = (String)element.getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		//	Mandatory C_AcctSchema_ID
		m_parameterWhere.append("C_AcctSchema_ID=").append(p_C_AcctSchema_ID);
		//	Optional Account_ID
		if (p_Account_ID != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				p_PA_Hierarchy_ID,X_C_AcctSchema_Element.ELEMENTTYPE_Account, p_Account_ID));
		if (p_AccountValue_From != null && p_AccountValue_From.length() == 0)
			p_AccountValue_From = null;
		if (p_AccountValue_To != null && p_AccountValue_To.length() == 0)
			p_AccountValue_To = null;
		if (p_AccountValue_From != null && p_AccountValue_To != null)
			m_parameterWhere.append(" AND (Account_ID IS NULL OR EXISTS (SELECT * FROM C_ElementValue ev ")
				.append("WHERE Account_ID=ev.C_ElementValue_ID AND ev.Value >= ")
				.append(DB.TO_STRING(p_AccountValue_From)).append(" AND ev.Value <= ")
				.append(DB.TO_STRING(p_AccountValue_To)).append("))");
		else if (p_AccountValue_From != null && p_AccountValue_To == null)
			m_parameterWhere.append(" AND (Account_ID IS NULL OR EXISTS (SELECT * FROM C_ElementValue ev ")
			.append("WHERE Account_ID=ev.C_ElementValue_ID AND ev.Value >= ")
			.append(DB.TO_STRING(p_AccountValue_From)).append("))");
		else if (p_AccountValue_From == null && p_AccountValue_To != null)
			m_parameterWhere.append(" AND (Account_ID IS NULL OR EXISTS (SELECT * FROM C_ElementValue ev ")
			.append("WHERE Account_ID=ev.C_ElementValue_ID AND ev.Value <= ")
			.append(DB.TO_STRING(p_AccountValue_To)).append("))");
		//	Optional Org
		if (p_AD_Org_ID != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				p_PA_Hierarchy_ID, X_C_AcctSchema_Element.ELEMENTTYPE_Organization, p_AD_Org_ID));
		//	Optional BPartner
		if (p_C_BPartner_ID != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				p_PA_Hierarchy_ID, X_C_AcctSchema_Element.ELEMENTTYPE_BPartner, p_C_BPartner_ID));
		//	Optional Product
		if (p_M_Product_ID != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				p_PA_Hierarchy_ID, X_C_AcctSchema_Element.ELEMENTTYPE_Product, p_M_Product_ID));
		//	Optional Project
		if (p_C_Project_ID != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				p_PA_Hierarchy_ID, X_C_AcctSchema_Element.ELEMENTTYPE_Project, p_C_Project_ID));
		//	Optional Activity
		if (p_C_Activity_ID != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				p_PA_Hierarchy_ID, X_C_AcctSchema_Element.ELEMENTTYPE_Activity, p_C_Activity_ID));
		//	Optional Campaign
		if (p_C_Campaign_ID != 0)
			m_parameterWhere.append(" AND C_Campaign_ID=").append(p_C_Campaign_ID);
		//	m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
		//		MAcctSchemaElement.ELEMENTTYPE_Campaign, p_C_Campaign_ID));
		//	Optional Sales Region
		if (p_C_SalesRegion_ID != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				p_PA_Hierarchy_ID, X_C_AcctSchema_Element.ELEMENTTYPE_SalesRegion, p_C_SalesRegion_ID));
		//	Mandatory Posting Type
		m_parameterWhere.append(" AND PostingType='").append(p_PostingType).append("'");
		//
		setDateAcct();
		sb.append(" - DateAcct ").append(p_DateAcct_From).append("-").append(p_DateAcct_To);
		sb.append(" - Where=").append(m_parameterWhere);
		log.fine(sb.toString());
	}	//	prepare

	/**
	 * 	Set Start/End Date of Report - if not defined current Month
	 */
	private void setDateAcct()
	{
		//	Get Default Calendar
		m_acctSchema = MAcctSchema.get(getCtx(), p_C_AcctSchema_ID);
	   	m_C_Calendar_ID = MClientInfo.get(getCtx(), m_acctSchema.getAD_Client_ID()).getC_Calendar_ID();
		//	Date defined
		if (p_DateAcct_From != null)
		{
			if (p_DateAcct_To == null)
				p_DateAcct_To = new Timestamp (System.currentTimeMillis());
			return;
		}
		//	Get Date from Period
		if (p_C_Period_ID == 0)
		{
			GregorianCalendar cal = new GregorianCalendar(Language.getLoginLanguage().getLocale());
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);		//	set to first of month
			p_DateAcct_From = new Timestamp (cal.getTimeInMillis());
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DAY_OF_YEAR, -1);		//	last of month
			p_DateAcct_To = new Timestamp (cal.getTimeInMillis());
		}
		else
		{
			MPeriod period = MPeriod.get(getCtx(), p_C_Period_ID);
			p_DateAcct_From = period.getStartDate();
			p_DateAcct_To = period.getEndDate();
			m_C_Calendar_ID = period.getC_Calendar_ID();
		}
	}	//	setDateAcct

	
	/**************************************************************************
	 *  Perform process.
	 *  @return Message to be translated
	 */
	@Override
	protected String doIt()
	{
		createBalanceLine();
		createDetailLines();

	//	int AD_PrintFormat_ID = 134;
	//	getProcessInfo().setTransientObject (MPrintFormat.get (getCtx(), AD_PrintFormat_ID, false));

		log.fine((System.currentTimeMillis() - m_start) + " ms");
		return "";
	}	//	doIt

	/**
	 * 	Create Beginning Balance Line
	 */
	private void createBalanceLine()
	{
		StringBuffer sql = new StringBuffer (s_insert);
		//	(AD_PInstance_ID, Fact_Acct_ID,
		sql.append("SELECT ").append(getAD_PInstance_ID()).append(",0,");
		//	AD_Client_ID, AD_Org_ID, Created,CreatedBy, Updated,UpdatedBy,
		sql.append(getAD_Client_ID()).append(",");
		if (p_AD_Org_ID == 0)
			sql.append("0");
		else
			sql.append(p_AD_Org_ID);
		sql.append(", SysDate,").append(getAD_User_ID())
			.append(",SysDate,").append(getAD_User_ID()).append(",");
		//	C_AcctSchema_ID, Account_ID, AccountValue, DateTrx, DateAcct, C_Period_ID,
		sql.append(p_C_AcctSchema_ID).append(",");
		if (p_Account_ID == 0)
			sql.append ("null");
		else
			sql.append (p_Account_ID);
		if (p_AccountValue_From != null)
			sql.append(",").append(DB.TO_STRING(p_AccountValue_From));
		else if (p_AccountValue_To != null)
			sql.append(",' '");
		else
			sql.append(",null");
		Timestamp balanceDay = p_DateAcct_From; // TimeUtil.addDays(p_DateAcct_From, -1);
		sql.append(",null,").append(DB.TO_DATE(balanceDay, true)).append(",");
		if (p_C_Period_ID == 0)
			sql.append("null");
		else
			sql.append(p_C_Period_ID);
		sql.append(",");
		//	AD_Table_ID, Record_ID, Line_ID,
		sql.append("null,null,null,");
		//	GL_Category_ID, GL_Budget_ID, C_Tax_ID, M_Locator_ID, PostingType,
		sql.append("null,null,null,null,'").append(p_PostingType).append("',");
		//	C_Currency_ID, AmtSourceDr, AmtSourceCr, AmtSourceBalance,
		sql.append("null,null,null,null,");
		//	AmtAcctDr, AmtAcctCr, AmtAcctBalance, C_UOM_ID, Qty,
		sql.append(" COALESCE(SUM(AmtAcctDr),0),COALESCE(SUM(AmtAcctCr),0),"
				  + "COALESCE(SUM(AmtAcctDr),0)-COALESCE(SUM(AmtAcctCr),0),"
			+ " null,COALESCE(SUM(Qty),0),");
		//	M_Product_ID, C_BPartner_ID, AD_OrgTrx_ID, C_LocFrom_ID,C_LocTo_ID,
		if (p_M_Product_ID == 0)
			sql.append ("null");
		else
			sql.append (p_M_Product_ID);
		sql.append(",");
		if (p_C_BPartner_ID == 0)
			sql.append ("null");
		else
			sql.append (p_C_BPartner_ID);
		sql.append(",");
		if (p_AD_OrgTrx_ID == 0)
			sql.append ("null");
		else
			sql.append (p_AD_OrgTrx_ID);
		sql.append(",");
		if (p_C_LocFrom_ID == 0)
			sql.append ("null");
		else
			sql.append (p_C_LocFrom_ID);
		sql.append(",");
		if (p_C_LocTo_ID == 0)
			sql.append ("null");
		else
			sql.append (p_C_LocTo_ID);
		sql.append(",");
		//	C_SalesRegion_ID, C_Project_ID, C_Campaign_ID, C_Activity_ID,
		if (p_C_SalesRegion_ID == 0)
			sql.append ("null");
		else
			sql.append (p_C_SalesRegion_ID);
		sql.append(",");
		if (p_C_Project_ID == 0)
			sql.append ("null");
		else
			sql.append (p_C_Project_ID);
		sql.append(",");
		if (p_C_Campaign_ID == 0)
			sql.append ("null");
		else
			sql.append (p_C_Campaign_ID);
		sql.append(",");
		if (p_C_Activity_ID == 0)
			sql.append ("null");
		else
			sql.append (p_C_Activity_ID);
		sql.append(",");
		//	User1_ID, User2_ID, A_Asset_ID, Description)
		if (p_User1_ID == 0)
			sql.append ("null");
		else
			sql.append (p_User1_ID);
		sql.append(",");
		if (p_User2_ID == 0)
			sql.append ("null");
		else
			sql.append (p_User2_ID);
		sql.append(", null,null");
		sql.append(",");
		sql.append( p_PA_Hierarchy_ID );
		//
		sql.append(" FROM Fact_Acct WHERE AD_Client_ID=").append(getAD_Client_ID())
			.append (" AND ").append(m_parameterWhere)
			.append(" AND DateAcct < ").append(DB.TO_DATE(p_DateAcct_From, true));
		//	Start Beginning of Year
		if (p_Account_ID > 0)
		{
			m_acct = new MElementValue (getCtx(), p_Account_ID, get_TrxName());
			if (!m_acct.isBalanceSheet())
			{
				MPeriod first = MPeriod.getFirstInYear (getCtx(), m_C_Calendar_ID, p_DateAcct_From);
				if (first != null)
					sql.append(" AND DateAcct >= ").append(DB.TO_DATE(first.getStartDate(), true));
				else
					log.log(Level.SEVERE, "first period not found");
			}
		}
		//
		int no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no == 0)
			log.fine(sql.toString());
		log.fine("#" + no + " (Account_ID=" + p_Account_ID + ")");
	}	//	createBalanceLine

	/**
	 * 	Create Beginning Balance Line
	 */
	private void createDetailLines()
	{
		StringBuffer sql = new StringBuffer (s_insert);
		//	(AD_PInstance_ID, Fact_Acct_ID,
		sql.append("SELECT ").append(getAD_PInstance_ID()).append(",Fact_Acct_ID,");
		//	AD_Client_ID, AD_Org_ID, Created,CreatedBy, Updated,UpdatedBy,
		sql.append(getAD_Client_ID()).append(",AD_Org_ID,Created,CreatedBy, Updated,UpdatedBy,");
		//	C_AcctSchema_ID, Account_ID, DateTrx, AccountValue, DateAcct, C_Period_ID,
		sql.append("C_AcctSchema_ID, Account_ID, null, DateTrx, DateAcct, C_Period_ID,");
		//	AD_Table_ID, Record_ID, Line_ID,
		sql.append("AD_Table_ID, Record_ID, Line_ID,");
		//	GL_Category_ID, GL_Budget_ID, C_Tax_ID, M_Locator_ID, PostingType,
		sql.append("GL_Category_ID, GL_Budget_ID, C_Tax_ID, M_Locator_ID, PostingType,");
		//	C_Currency_ID, AmtSourceDr, AmtSourceCr, AmtSourceBalance,
		sql.append("C_Currency_ID, AmtSourceDr,AmtSourceCr, AmtSourceDr-AmtSourceCr,");
		//	AmtAcctDr, AmtAcctCr, AmtAcctBalance, C_UOM_ID, Qty,
		sql.append(" AmtAcctDr,AmtAcctCr, AmtAcctDr-AmtAcctCr, C_UOM_ID,Qty,");
		//	M_Product_ID, C_BPartner_ID, AD_OrgTrx_ID, C_LocFrom_ID,C_LocTo_ID,
		sql.append ("M_Product_ID, C_BPartner_ID, AD_OrgTrx_ID, C_LocFrom_ID,C_LocTo_ID,");
		//	C_SalesRegion_ID, C_Project_ID, C_Campaign_ID, C_Activity_ID,
		sql.append ("C_SalesRegion_ID, C_Project_ID, C_Campaign_ID, C_Activity_ID,");
		//	User1_ID, User2_ID, A_Asset_ID, Description)
		sql.append ("User1_ID, User2_ID, A_Asset_ID, Description");
		sql.append( ", " + p_PA_Hierarchy_ID );
		//
		sql.append(" FROM Fact_Acct WHERE AD_Client_ID=").append(getAD_Client_ID())
			.append (" AND ").append(m_parameterWhere)
			.append(" AND DateAcct >= ").append(DB.TO_DATE(p_DateAcct_From, true))
			.append(" AND TRUNC(DateAcct,'DD') <= ").append(DB.TO_DATE(p_DateAcct_To, true));
		//
		int no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no == 0)
			log.fine(sql.toString());
		log.fine("#" + no + " (Account_ID=" + p_Account_ID + ")");
		
		//	Update AccountValue
		String sql2 = "UPDATE T_TrialBalance tb SET AccountValue = "
			+ "(SELECT Value FROM C_ElementValue ev WHERE ev.C_ElementValue_ID=tb.Account_ID) "
			+ "WHERE tb.Account_ID IS NOT NULL";
		no = DB.executeUpdate(sql2, get_TrxName());
		if (no > 0)
			log.fine("Set AccountValue #" + no);
		
	}	//	createDetailLines

}	//	TrialBalance
