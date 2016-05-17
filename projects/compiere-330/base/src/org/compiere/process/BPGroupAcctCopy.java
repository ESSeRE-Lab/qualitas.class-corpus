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
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.util.*;


/**
 * 	Copy BP Group default Accounts
 *	
 *  @author Jorg Janke
 *  @version $Id: BPGroupAcctCopy.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class BPGroupAcctCopy extends SvrProcess
{
	/** BP Group					*/
	private int			p_C_BP_Group_ID = 0;
	/**	Acct Schema					*/
	private int			p_C_AcctSchema_ID = 0;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare ()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) 
		{
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("C_BP_Group_ID"))
				p_C_BP_Group_ID = element.getParameterAsInt();
			else if (name.equals("C_AcctSchema_ID"))
				p_C_AcctSchema_ID = element.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare

	
	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt () throws Exception
	{
		log.info("C_AcctSchema_ID=" + p_C_AcctSchema_ID);
		if (p_C_AcctSchema_ID == 0)
			throw new CompiereSystemException("C_AcctSchema_ID=0");
		MAcctSchema as = MAcctSchema.get(getCtx(), p_C_AcctSchema_ID);
		if (as.get_ID() == 0)
			throw new CompiereSystemException("Not Found - C_AcctSchema_ID=" + p_C_AcctSchema_ID);
		//
		String sql = null;
		int updated = 0;
		int created = 0;
		int updatedTotal = 0;
		int createdTotal = 0;

		//	Update existing Customers
		sql = "UPDATE C_BP_Customer_Acct ca "
			+ "SET (C_Receivable_Acct,C_Receivable_Services_Acct,C_PrePayment_Acct)="
			 + " (SELECT C_Receivable_Acct,C_Receivable_Services_Acct,C_PrePayment_Acct "
			 + " FROM C_BP_Group_Acct"
			 + " WHERE C_BP_Group_ID=" + p_C_BP_Group_ID
			 + " AND C_AcctSchema_ID=" + p_C_AcctSchema_ID
			+ "), Updated=SysDate, UpdatedBy=0 "
			+ "WHERE ca.C_AcctSchema_ID=" + p_C_AcctSchema_ID
			+ " AND EXISTS (SELECT * FROM C_BPartner p "
				+ "WHERE p.C_BPartner_ID=ca.C_BPartner_ID"
				+ " AND p.C_BP_Group_ID=" + p_C_BP_Group_ID + ")";
		updated = DB.executeUpdate(sql, get_TrxName());
		addLog(0, null, new BigDecimal(updated), "@Updated@ @C_BPartner_ID@ @IsCustomer@");
		updatedTotal += updated;
		
		//	Insert new Customer
		sql = "INSERT INTO C_BP_Customer_Acct "
			+ "(C_BPartner_ID, C_AcctSchema_ID,"
			+ " AD_Client_ID, AD_Org_ID, IsActive, Created, CreatedBy, Updated, UpdatedBy,"
			+ " C_Receivable_Acct, C_Receivable_Services_Acct, C_PrePayment_Acct) "
			+ "SELECT p.C_BPartner_ID, acct.C_AcctSchema_ID,"
			+ " p.AD_Client_ID, p.AD_Org_ID, 'Y', SysDate, 0, SysDate, 0,"
			+ " acct.C_Receivable_Acct, acct.C_Receivable_Services_Acct, acct.C_PrePayment_Acct "
			+ "FROM C_BPartner p"
			+ " INNER JOIN C_BP_Group_Acct acct ON (acct.C_BP_Group_ID=p.C_BP_Group_ID)"
			+ "WHERE acct.C_AcctSchema_ID=" + p_C_AcctSchema_ID			//	#
			+ " AND p.C_BP_Group_ID=" + p_C_BP_Group_ID
			+ " AND NOT EXISTS (SELECT * FROM C_BP_Customer_Acct ca "
				+ "WHERE ca.C_BPartner_ID=p.C_BPartner_ID"
				+ " AND ca.C_AcctSchema_ID=acct.C_AcctSchema_ID)";
		created = DB.executeUpdate(sql, get_TrxName());
		addLog(0, null, new BigDecimal(created), "@Created@ @C_BPartner_ID@ @IsCustomer@");
		createdTotal += created;

		
		//	Update existing Vendors
		sql = "UPDATE C_BP_Vendor_Acct va "
			+ "SET (V_Liability_Acct,V_Liability_Services_Acct,V_PrePayment_Acct)="
			 + " (SELECT V_Liability_Acct,V_Liability_Services_Acct,V_PrePayment_Acct "
			 + " FROM C_BP_Group_Acct"
			 + " WHERE C_BP_Group_ID=" + p_C_BP_Group_ID
			 + " AND C_AcctSchema_ID=" + p_C_AcctSchema_ID
			+ "), Updated=SysDate, UpdatedBy=0 "
			+ "WHERE va.C_AcctSchema_ID=" + p_C_AcctSchema_ID
			+ " AND EXISTS (SELECT * FROM C_BPartner p "
				+ "WHERE p.C_BPartner_ID=va.C_BPartner_ID"
				+ " AND p.C_BP_Group_ID=" + p_C_BP_Group_ID + ")";
		updated = DB.executeUpdate(sql, get_TrxName());
		addLog(0, null, new BigDecimal(updated), "@Updated@ @C_BPartner_ID@ @IsVendor@");
		updatedTotal += updated;
		
		//	Insert new Vendors
		sql = "INSERT INTO C_BP_Vendor_Acct "
			+ "(C_BPartner_ID, C_AcctSchema_ID,"
			+ " AD_Client_ID, AD_Org_ID, IsActive, Created, CreatedBy, Updated, UpdatedBy,"
			+ " V_Liability_Acct, V_Liability_Services_Acct, V_PrePayment_Acct) "
			+ "SELECT p.C_BPartner_ID, acct.C_AcctSchema_ID,"
			+ " p.AD_Client_ID, p.AD_Org_ID, 'Y', SysDate, 0, SysDate, 0,"
			+ " acct.V_Liability_Acct, acct.V_Liability_Services_Acct, acct.V_PrePayment_Acct "
			+ "FROM C_BPartner p"
			+ " INNER JOIN C_BP_Group_Acct acct ON (acct.C_BP_Group_ID=p.C_BP_Group_ID)"
			+ "WHERE acct.C_AcctSchema_ID=" + p_C_AcctSchema_ID			//	#
			+ " AND p.C_BP_Group_ID=" + p_C_BP_Group_ID
			+ " AND NOT EXISTS (SELECT * FROM C_BP_Vendor_Acct va "
				+ "WHERE va.C_BPartner_ID=p.C_BPartner_ID AND va.C_AcctSchema_ID=acct.C_AcctSchema_ID)";
		created = DB.executeUpdate(sql, get_TrxName());
		addLog(0, null, new BigDecimal(created), "@Created@ @C_BPartner_ID@ @IsVendor@");
		createdTotal += created;

		return "@Created@=" + createdTotal + ", @Updated@=" + updatedTotal;
	}	//	doIt

}	//	BPGroupAcctCopy
