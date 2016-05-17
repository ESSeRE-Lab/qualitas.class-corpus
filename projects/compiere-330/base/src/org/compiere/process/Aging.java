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

import java.math.*;
import java.sql.*;
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *	Invoice Aging Report.
 *	Based on RV_Aging.
 *  @author Jorg Janke
 *  @version $Id: Aging.java,v 1.5 2006/10/07 00:58:44 jjanke Exp $
 */
public class Aging extends SvrProcess
{
	/** The date to calculate the days due from			*/
	private Timestamp	p_StatementDate = null;
	private boolean 	p_IsSOTrx = false;
	private int			p_AD_Org_ID = 0;
	private int			p_C_Currency_ID = 0;
	private int			p_C_BP_Group_ID = 0;
	private int			p_C_BPartner_ID = 0;
	private boolean		p_IsListInvoices = false;
	/** Number of days between today and statement date	*/
	private int			m_statementOffset = 0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) {
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("StatementDate"))
				p_StatementDate = (Timestamp)element.getParameter();
			else if (name.equals("IsSOTrx"))
				p_IsSOTrx = "Y".equals(element.getParameter());
			else if (name.equals("C_Currency_ID"))
				p_C_Currency_ID = element.getParameterAsInt();
			else if (name.equals("AD_Org_ID"))
				p_AD_Org_ID = element.getParameterAsInt();
			else if (name.equals("C_BP_Group_ID"))
				p_C_BP_Group_ID = element.getParameterAsInt();
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = element.getParameterAsInt();
			else if (name.equals("IsListInvoices"))
				p_IsListInvoices = "Y".equals(element.getParameter());
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		if (p_StatementDate == null)
			p_StatementDate = new Timestamp (System.currentTimeMillis());
		else
			m_statementOffset = TimeUtil.getDaysBetween( 
				new Timestamp(System.currentTimeMillis()), p_StatementDate);
	}	//	prepare

	/**
	 * 	DoIt
	 *	@return Message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		log.info("StatementDate=" + p_StatementDate + ", IsSOTrx=" + p_IsSOTrx
			+ ", C_Currency_ID=" + p_C_Currency_ID + ",AD_Org_ID=" + p_AD_Org_ID
			+ ", C_BP_Group_ID=" + p_C_BP_Group_ID + ", C_BPartner_ID=" + p_C_BPartner_ID
			+ ", IsListInvoices=" + p_IsListInvoices);
		//
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT bp.C_BP_Group_ID, oi.C_BPartner_ID,oi.C_Invoice_ID,oi.C_InvoicePaySchedule_ID, " 
			+ "oi.C_Currency_ID, oi.IsSOTrx, "								//	5..6
			+ "oi.DateInvoiced, oi.NetDays,oi.DueDate,oi.DaysDue, ");		//	7..10
		if (p_C_Currency_ID == 0)
			sql.append("oi.GrandTotal, oi.PaidAmt, oi.OpenAmt ");			//	11..13
		else
		{
			String s = ",oi.C_Currency_ID," + p_C_Currency_ID + ",oi.DateAcct,oi.C_ConversionType_ID,oi.AD_Client_ID,oi.AD_Org_ID)";
			sql.append("currencyConvert(oi.GrandTotal").append(s)		//	11..
				.append(", currencyConvert(oi.PaidAmt").append(s)
				.append(", currencyConvert(oi.OpenAmt").append(s);
		}
		sql.append(",oi.C_Activity_ID,oi.C_Campaign_ID,oi.C_Project_ID "	//	14
			+ "FROM RV_OpenItem oi"
			+ " INNER JOIN C_BPartner bp ON (oi.C_BPartner_ID=bp.C_BPartner_ID) "
			+ "WHERE oi.ISSoTrx=").append(p_IsSOTrx ? "'Y'" : "'N'");
		if (p_AD_Org_ID > 0)
			sql.append(" AND oi.AD_Org_ID=").append(p_AD_Org_ID);
		if (p_C_BPartner_ID > 0)
			sql.append(" AND oi.C_BPartner_ID=").append(p_C_BPartner_ID);
		else if (p_C_BP_Group_ID > 0)
			sql.append(" AND bp.C_BP_Group_ID=").append(p_C_BP_Group_ID);
		sql.append(" ORDER BY oi.C_BPartner_ID, oi.C_Currency_ID, oi.C_Invoice_ID");
		
		log.finest(sql.toString());
		String finalSql = MRole.getDefault(getCtx(), false).addAccessSQL(
			sql.toString(), "oi", MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);	
		log.finer(finalSql);

		PreparedStatement pstmt = null;
		//
		MAging aging = null;
		int counter = 0;
		int rows = 0;
		int AD_PInstance_ID = getAD_PInstance_ID();
		//
		try
		{
			pstmt = DB.prepareStatement(finalSql, get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int C_BP_Group_ID = rs.getInt(1);
				int C_BPartner_ID = rs.getInt(2);
				int C_Invoice_ID = p_IsListInvoices ? rs.getInt(3) : 0;
				int C_InvoicePaySchedule_ID = p_IsListInvoices ? rs.getInt(4) : 0;
				int C_Currency_ID = rs.getInt(5);
				boolean IsSOTrx = "Y".equals(rs.getString(6));
				//
			//	Timestamp DateInvoiced = rs.getTimestamp(7);
			//	int NetDays = rs.getInt(8);
				Timestamp DueDate = rs.getTimestamp(9);
				//	Days Due
				int DaysDue = rs.getInt(10)		//	based on today
					+ m_statementOffset;
				//
				BigDecimal GrandTotal = rs.getBigDecimal(11);
			//	BigDecimal PaidAmt = rs.getBigDecimal(12);
				BigDecimal OpenAmt = rs.getBigDecimal(13);
				//
				int C_Activity_ID = p_IsListInvoices ? rs.getInt(14) : 0;
				int C_Campaign_ID = p_IsListInvoices ? rs.getInt(15) : 0;
				int C_Project_ID = p_IsListInvoices ? rs.getInt(16) : 0;
				
				rows++;
				//	New Aging Row
				if (aging == null 		//	Key
					|| AD_PInstance_ID != aging.getAD_PInstance_ID()
					|| C_BPartner_ID != aging.getC_BPartner_ID()
					|| C_Currency_ID != aging.getC_Currency_ID()
					|| C_Invoice_ID != aging.getC_Invoice_ID()
					|| C_InvoicePaySchedule_ID != aging.getC_InvoicePaySchedule_ID())
				{
					if (aging != null)
					{
						if (aging.save())
							log.fine("#" + ++counter + " - " + aging);
						else
						{
							log.log(Level.SEVERE, "Not saved " + aging);
							break;
						}
					}
					aging = new MAging (getCtx(), AD_PInstance_ID, p_StatementDate, 
						C_BPartner_ID, C_Currency_ID, 
						C_Invoice_ID, C_InvoicePaySchedule_ID, 
						C_BP_Group_ID, DueDate, IsSOTrx, get_TrxName());
					if (p_AD_Org_ID > 0)
						aging.setAD_Org_ID(p_AD_Org_ID);
					aging.setC_Activity_ID(C_Activity_ID);
					aging.setC_Campaign_ID(C_Campaign_ID);
					aging.setC_Project_ID(C_Project_ID);
				}
				//	Fill Buckets
				aging.add (DueDate, DaysDue, GrandTotal, OpenAmt);
			}
			if (aging != null)
			{
				if (aging.save())
					log.fine("#" + ++counter + " - " + aging);
				else
					log.log(Level.SEVERE, "Not saved " + aging);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, finalSql, e);
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
		//	
		log.info("#" + counter + " - rows=" + rows);
		return "";
	}	//	doIt

}	//	Aging

