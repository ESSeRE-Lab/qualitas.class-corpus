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
 *	Import Accounts from I_ElementValue
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ImportAccount.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class ImportAccount extends SvrProcess
{
	/**	Client to be imported to		*/
	private int				m_AD_Client_ID = 0;
	/** Default Element					*/
	private int				m_C_Element_ID = 0;
	/**	Update Default Accounts			*/
	private boolean			m_updateDefaultAccounts = false;
	/** Create New Combination			*/
	private boolean			m_createNewCombination = true;

	/**	Delete old Imported				*/
	private boolean			m_deleteOldImported = false;

	/** Effective						*/
	private Timestamp		m_DateValue = null;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) 
		{
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("C_Element_ID"))
				m_C_Element_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("UpdateDefaultAccounts"))
				m_updateDefaultAccounts = "Y".equals(element.getParameter());
			else if (name.equals("CreateNewCombination"))
				m_createNewCombination = "Y".equals(element.getParameter());
			else if (name.equals("DeleteOldImported"))
				m_deleteOldImported = "Y".equals(element.getParameter());
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		if (m_DateValue == null)
			m_DateValue = new Timestamp (System.currentTimeMillis());
	}	//	prepare


	/**
	 *  Perrform process.
	 *  @return Message
	 *  @throws Exception
	 */
	@Override
	protected String doIt() throws java.lang.Exception
	{
		StringBuffer sql = null;
		int no = 0;
		String clientCheck = " AND AD_Client_ID=" + m_AD_Client_ID;

		//	****	Prepare	****

		//	Delete Old Imported
		if (m_deleteOldImported)
		{
			sql = new StringBuffer ("DELETE FROM I_ElementValue "
				+ "WHERE I_IsImported='Y'").append(clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			log.fine("Delete Old Impored =" + no);
		}

		//	Set Client, Org, IsActive, Created/Updated
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET AD_Client_ID = COALESCE (AD_Client_ID, ").append(m_AD_Client_ID).append("),"
			+ " AD_Org_ID = COALESCE (AD_Org_ID, 0),"
			+ " IsActive = COALESCE (IsActive, 'Y'),"
			+ " Created = COALESCE (Created, SysDate),"
			+ " CreatedBy = COALESCE (CreatedBy, 0),"
			+ " Updated = COALESCE (Updated, SysDate),"
			+ " UpdatedBy = COALESCE (UpdatedBy, 0),"
			+ " I_ErrorMsg = NULL,"
			+ " I_IsImported = 'N' "
			+ "WHERE I_IsImported<>'Y' OR I_IsImported IS NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Reset=" + no);

		//	****	Prepare	****

		//	Set Element
		if (m_C_Element_ID != 0)
		{
			sql = new StringBuffer ("UPDATE I_ElementValue "
				+ "SET ElementName=(SELECT Name FROM C_Element WHERE C_Element_ID=")
					.append(m_C_Element_ID).append(") "
				+ "WHERE ElementName IS NULL AND C_Element_ID IS NULL"
				+ " AND I_IsImported<>'Y'").append(clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			log.fine("Set Element Default=" + no);
		}
		//
		sql = new StringBuffer ("UPDATE I_ElementValue i "
			+ "SET C_Element_ID = (SELECT C_Element_ID FROM C_Element e"
			+ " WHERE i.ElementName=e.Name AND i.AD_Client_ID=e.AD_Client_ID)"
			+ "WHERE C_Element_ID IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Element=" + no);
		//
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET I_IsImported='E', I_ErrorMsg='ERR=Invalid Element, ' "
			+ "WHERE C_Element_ID IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Invalid Element=" + no);

		//	No Name, Value
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET I_IsImported='E', I_ErrorMsg='ERR=No Name, ' "
			+ "WHERE (Value IS NULL OR Name IS NULL)"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Invalid Name=" + no);

		
		//	Set Column
		sql = new StringBuffer ("UPDATE I_ElementValue i "
			+ "SET AD_Column_ID = (SELECT AD_Column_ID FROM AD_Column c"
			+ " WHERE UPPER(i.Default_Account)=UPPER(c.ColumnName)"
			+ " AND c.AD_Table_ID IN (315,266) AND AD_Reference_ID=25) "
			+ "WHERE Default_Account IS NOT NULL AND AD_Column_ID IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Column=" + no);
		//
		String ts = DB.isPostgreSQL() ? "COALESCE(I_ErrorMsg,'')" 
			: "I_ErrorMsg";  //java bug, it could not be used directly
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Column, ' "
			+ "WHERE AD_Column_ID IS NULL AND Default_Account IS NOT NULL"
			+ " AND UPPER(Default_Account)<>'DEFAULT_ACCT'"		//	ignore default account
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Invalid Column=" + no);

		//	Set Post* Defaults (ignore errors)
		String[] yColumns = new String[] {"PostActual", "PostBudget", "PostStatistical", "PostEncumbrance"};
		for (String element : yColumns) {
			sql = new StringBuffer ("UPDATE I_ElementValue SET ")
				.append(element).append("='Y' WHERE ")
				.append(element).append(" IS NULL OR ")
				.append(element).append(" NOT IN ('Y','N')"
				+ " AND I_IsImported<>'Y'").append(clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			log.fine("Set " + element + " Default=" + no);
		}
		//	Summary
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET IsSummary='N' "
			+ "WHERE IsSummary IS NULL OR IsSummary NOT IN ('Y','N')"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set IsSummary Default=" + no);

		//	Doc Controlled
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET IsDocControlled = CASE WHEN AD_Column_ID IS NOT NULL THEN 'Y' ELSE 'N' END "
			+ "WHERE IsDocControlled IS NULL OR IsDocControlled NOT IN ('Y','N')"
			+ " AND I_IsImported='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set IsDocumentControlled Default=" + no);

		//	Check Account Type A (E) L M O R
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET AccountType='E' "
			+ "WHERE AccountType IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set AccountType Default=" + no);
		//
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET I_IsImported='E', I_ErrorMsg="+ts+"||'ERR=Invalid AccountType, ' "
			+ "WHERE AccountType NOT IN ('A','E','L','M','O','R')"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Invalid AccountType=" + no);

		//	Check Account Sign (N) C B
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET AccountSign='N' "
			+ "WHERE AccountSign IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set AccountSign Default=" + no);
		//
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid AccountSign, ' "
			+ "WHERE AccountSign NOT IN ('N','C','D')"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Invalid AccountSign=" + no);

		//	No Value
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=No Key, ' "
			+ "WHERE (Value IS NULL OR Value='')"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Invalid Key=" + no);

		//	****	Update ElementValue from existing
		sql = new StringBuffer ("UPDATE I_ElementValue i "
			+ "SET C_ElementValue_ID=(SELECT C_ElementValue_ID FROM C_ElementValue ev"
			+ " INNER JOIN C_Element e ON (ev.C_Element_ID=e.C_Element_ID)"
			+ " WHERE i.C_Element_ID=e.C_Element_ID AND i.AD_Client_ID=e.AD_Client_ID"
			+ " AND i.Value=ev.Value) "
			+ "WHERE C_ElementValue_ID IS NULL"
			+ " AND I_IsImported='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Found ElementValue=" + no);

		commit();

		//	-------------------------------------------------------------------
		int noInsert = 0;
		int noUpdate = 0;

		//	Go through Records
		sql = new StringBuffer ("SELECT * "
			+ "FROM I_ElementValue "
			+ "WHERE I_IsImported='N'").append(clientCheck)
			.append(" ORDER BY I_ElementValue_ID");
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				X_I_ElementValue impEV = new X_I_ElementValue(getCtx(), rs, get_TrxName());
				int C_ElementValue_ID = impEV.getC_ElementValue_ID();
				int I_ElementValue_ID = impEV.getI_ElementValue_ID();

				//	****	Create/Update ElementValue
				if (C_ElementValue_ID == 0)		//	New
				{
					MElementValue ev = new MElementValue(impEV);
					if (ev.save())
					{
						noInsert++;
						impEV.setC_ElementValue_ID(ev.getC_ElementValue_ID());
						impEV.setI_IsImported(X_I_ElementValue.I_ISIMPORTED_Yes);
						impEV.save();
					}
					else
					{
						sql = new StringBuffer ("UPDATE I_ElementValue i "
							+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||").append(DB.TO_STRING("Insert ElementValue "))
							.append(" WHERE I_ElementValue_ID=").append(I_ElementValue_ID);
						DB.executeUpdate(sql.toString(), get_TrxName());
					}
				}
				else							//	Update existing
				{
					MElementValue ev = new MElementValue (getCtx(), C_ElementValue_ID, null);
					if (ev.get_ID() != C_ElementValue_ID)
					{
					}
					ev.set(impEV);
					if (ev.save())
					{
						noUpdate++;
						impEV.setI_IsImported(X_I_ElementValue.I_ISIMPORTED_Yes);
						impEV.save();
					}
					else
					{
						sql = new StringBuffer ("UPDATE I_ElementValue i "
							+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||").append(DB.TO_STRING("Update ElementValue"))
							.append("WHERE I_ElementValue_ID=").append(I_ElementValue_ID);
						DB.executeUpdate(sql.toString(), get_TrxName());
					}
				}
			}	//	for all I_Product
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			throw new Exception ("create", e);
		}

		//	Set Error to indicator to not imported
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET I_IsImported='N', Updated=SysDate "
			+ "WHERE I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		addLog (0, null, new BigDecimal (no), "@Errors@");
		addLog (0, null, new BigDecimal (noInsert), "@C_ElementValue_ID@: @Inserted@");
		addLog (0, null, new BigDecimal (noUpdate), "@C_ElementValue_ID@: @Updated@");

		commit();

		//	*****	Set Parent
		sql = new StringBuffer ("UPDATE I_ElementValue i "
			+ "SET ParentElementValue_ID=(SELECT C_ElementValue_ID"
			+ " FROM C_ElementValue ev WHERE i.C_Element_ID=ev.C_Element_ID"
			+ " AND i.ParentValue=ev.Value AND i.AD_Client_ID=ev.AD_Client_ID) "
			+ "WHERE ParentElementValue_ID IS NULL"
			+ " AND I_IsImported='Y' AND Processed='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Found Parent ElementValue=" + no);
		//
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET I_ErrorMsg="+ts +"||'Info=ParentNotFound, ' "
			+ "WHERE ParentElementValue_ID IS NULL AND ParentValue IS NOT NULL"
			+ " AND I_IsImported='Y' AND Processed='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Not Found Patent ElementValue=" + no);
		//
		sql = new StringBuffer ("SELECT i.ParentElementValue_ID, i.I_ElementValue_ID,"
			+ " e.AD_Tree_ID, i.C_ElementValue_ID, i.Value||'-'||i.Name AS Info "
			+ "FROM I_ElementValue i"
			+ " INNER JOIN C_Element e ON (i.C_Element_ID=e.C_Element_ID) "
			+ "WHERE i.C_ElementValue_ID IS NOT NULL AND e.AD_Tree_ID IS NOT NULL"
			+ " AND i.ParentElementValue_ID IS NOT NULL"
			+ " AND i.I_IsImported='Y' AND Processed='N' AND i.AD_Client_ID=").append(m_AD_Client_ID);
		int noParentUpdate = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			//
			String updateSQL = "UPDATE AD_TreeNode SET Parent_ID=?, SeqNo=? "
				+ "WHERE AD_Tree_ID=? AND Node_ID=?";
			PreparedStatement updateStmt = DB.prepareStatement(updateSQL, get_TrxName());
			//
			while (rs.next())
			{
				updateStmt.setInt(1, rs.getInt(1));		//	Parent
				updateStmt.setInt(2, rs.getInt(2));		//	SeqNo (assume sequenec in import is the same)
				updateStmt.setInt(3, rs.getInt(3));		//	Tree
				updateStmt.setInt(4, rs.getInt(4));		//	Node
				try
				{
					no = updateStmt.executeUpdate();
					noParentUpdate += no;
				}
				catch (SQLException ex)
				{
					log.log(Level.SEVERE, "(ParentUpdate)", ex);
					no = 0;
				}
				if (no == 0)
					log.info("Parent not found for " + rs.getString(5));
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, "(ParentUpdateLoop) " + sql.toString(), e);
		}
		addLog (0, null, new BigDecimal (noParentUpdate), "@ParentElementValue_ID@: @Updated@");

		//	Reset Processing Flag
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET Processing='-'"
			+ "WHERE I_IsImported='Y' AND Processed='N'"
			+ " AND C_ElementValue_ID IS NOT NULL")
			.append(clientCheck);
		if (m_updateDefaultAccounts)
			sql.append(" AND AD_Column_ID IS NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Reset Processing Flag=" + no);

		if (m_updateDefaultAccounts)
			updateDefaults(clientCheck);

		//	Update Description
		sql = new StringBuffer("SELECT * FROM C_ValidCombination vc "
			+ "WHERE EXISTS (SELECT * FROM I_ElementValue i "
				+ "WHERE vc.Account_ID=i.C_ElementValue_ID)");
		
		//	Done
		sql = new StringBuffer ("UPDATE I_ElementValue "
			+ "SET Processing='N', Processed='Y'"
			+ "WHERE I_IsImported='Y'")
			.append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Processed=" + no);

		return "";
	}	//	doIt

	
	/**************************************************************************
	 * 	Update Default Accounts
	 * 	@param clientCheck client where cluase
	 */
	private void updateDefaults (String clientCheck)
	{
		log.config("CreateNewCombination=" + m_createNewCombination);

		//	****	Update Defaults
		StringBuffer sql = new StringBuffer ("SELECT C_AcctSchema_ID FROM C_AcctSchema_Element "
			+ "WHERE C_Element_ID=?").append(clientCheck);
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			pstmt.setInt(1, m_C_Element_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				updateDefaultAccounts (rs.getInt(1));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}

		//	Default Account		DEFAULT_ACCT
		sql = new StringBuffer ("UPDATE C_AcctSchema_Element e "
			+ "SET C_ElementValue_ID=(SELECT C_ElementValue_ID FROM I_ElementValue i"
			+ " WHERE e.C_Element_ID=i.C_Element_ID AND i.C_ElementValue_ID IS NOT NULL"
			+ " AND UPPER(i.Default_Account)='DEFAULT_ACCT') "
			+ "WHERE EXISTS (SELECT * FROM I_ElementValue i"
			+ " WHERE e.C_Element_ID=i.C_Element_ID AND i.C_ElementValue_ID IS NOT NULL"
			+ " AND UPPER(i.Default_Account)='DEFAULT_ACCT' "
			+ "	AND i.I_IsImported='Y' AND i.Processing='-')")
			.append(clientCheck);
		int no = DB.executeUpdate(sql.toString(), get_TrxName());
		addLog (0, null, new BigDecimal (no), "@C_AcctSchema_Element_ID@: @Updated@");
	}	//	updateDefaults

	/**
	 * 	Update Default Accounts.
	 *	_Default.xxxx = C_ValidCombination_ID  =>  Account_ID=C_ElementValue_ID
	 * 	@param C_AcctSchema_ID Accounting Schema
	 */
	private void updateDefaultAccounts (int C_AcctSchema_ID)
	{
		log.config("C_AcctSchema_ID=" + C_AcctSchema_ID);

		MAcctSchema as = new MAcctSchema (getCtx(), C_AcctSchema_ID, null);
		if (as.getAcctSchemaElement("AC").getC_Element_ID() != m_C_Element_ID)
		{
			log.log(Level.SEVERE, "C_Element_ID=" + m_C_Element_ID + " not in AcctSchema=" + as);
			return;
		}

		int[] counts = new int[] {0, 0, 0};

		String sql = "SELECT i.C_ElementValue_ID, t.TableName, c.ColumnName, i.I_ElementValue_ID "
			+ "FROM I_ElementValue i"
			+ " INNER JOIN AD_Column c ON (i.AD_Column_ID=c.AD_Column_ID)"
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE i.I_IsImported='Y' AND Processing='-'"
			+ " AND i.C_ElementValue_ID IS NOT NULL AND C_Element_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1, m_C_Element_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int C_ElementValue_ID = rs.getInt(1);
				String TableName = rs.getString(2);
				String ColumnName = rs.getString(3);
				int I_ElementValue_ID = rs.getInt(4);
				//	Update it
				int u = updateDefaultAccount(TableName, ColumnName, C_AcctSchema_ID, C_ElementValue_ID);
				counts[u]++;
				if (u != UPDATE_ERROR)
				{
					sql = "UPDATE I_ElementValue SET Processing='N' "
						+ "WHERE I_ElementValue_ID=" + I_ElementValue_ID;
					int no = DB.executeUpdate(sql, get_TrxName());
					if (no != 1)
						log.log(Level.SEVERE, "Updated=" + no);
				}
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, "", e);
		}
		addLog (0, null, new BigDecimal (counts[UPDATE_ERROR]), as.toString() + ": @Errors@");
		addLog (0, null, new BigDecimal (counts[UPDATE_YES]), as.toString() + ": @Updated@");
		addLog (0, null, new BigDecimal (counts[UPDATE_SAME]), as.toString() + ": OK");

	}	//	createDefaultAccounts


	private static final int	UPDATE_ERROR = 0;
	private static final int	UPDATE_YES = 1;
	private static final int	UPDATE_SAME = 2;

	/**
	 * 	Update Default Account.
	 *  This is the sql to delete unused accounts - with the import still in the table(!):
		DELETE C_ElementValue e
		WHERE NOT EXISTS (SELECT * FROM Fact_Acct f WHERE f.Account_ID=e.C_ElementValue_ID)
		 AND NOT EXISTS (SELECT * FROM C_ValidCombination vc WHERE vc.Account_ID=e.C_ElementValue_ID)
		 AND NOT EXISTS (SELECT * FROM I_ElementValue i WHERE i.C_ElementValue_ID=e.C_ElementValue_ID);
	 * 	@param TableName Table Name
	 * 	@param ColumnName Column Name
	 * 	@param C_AcctSchema_ID Account Schema
	 * 	@param C_ElementValue_ID new Account
	 * 	@return UPDATE_* status
	 */
	private int updateDefaultAccount (String TableName, String ColumnName, int C_AcctSchema_ID, int C_ElementValue_ID)
	{
		log.fine(TableName + "." + ColumnName + " - " + C_ElementValue_ID);
		int retValue = UPDATE_ERROR;
		StringBuffer sql = new StringBuffer ("SELECT x.")
			.append(ColumnName).append(",Account_ID FROM ")
			.append(TableName).append(" x INNER JOIN C_ValidCombination vc ON (x.")
			.append(ColumnName).append("=vc.C_ValidCombination_ID) ")
			.append("WHERE x.C_AcctSchema_ID=").append(C_AcctSchema_ID);
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				int C_ValidCombination_ID = rs.getInt(1);
				int Account_ID = rs.getInt(2);
				//	The current account value is the same
				if (Account_ID == C_ElementValue_ID)
				{
					retValue = UPDATE_SAME;
					log.fine("Account_ID same as new value");
				}
				//	We need to update the Account Value
				else
				{
					if (m_createNewCombination)
					{
   					    MAccount oldacct = MAccount.get(getCtx(), C_ValidCombination_ID);
   					    MAccount newacct = MAccount.get(getCtx(), oldacct.getAD_Client_ID(), oldacct.getAD_Org_ID(),
   					    		                     C_AcctSchema_ID, C_ElementValue_ID, oldacct.getC_SubAcct_ID(),
   					    		                     oldacct.getM_Product_ID(),oldacct.getC_BPartner_ID(),
   					    		                     oldacct.getAD_OrgTrx_ID(),oldacct.getC_LocFrom_ID(),
   					    		                     oldacct.getC_LocTo_ID(), oldacct.getC_SalesRegion_ID(),
   					    		                     oldacct.getC_Project_ID(), oldacct.getC_Campaign_ID(),
   					    		                     oldacct.getC_Activity_ID(), oldacct.getUser1_ID(),oldacct.getUser2_ID(),
   					    		                     oldacct.getUserElement1_ID(),oldacct.getUserElement2_ID());
						if (newacct.save())
						{
							int newC_ValidCombination_ID = newacct.getC_ValidCombination_ID();
							if (C_ValidCombination_ID != newC_ValidCombination_ID)
							{
								sql = new StringBuffer ("UPDATE ").append(TableName)
									.append(" SET ").append(ColumnName).append("=").append(newC_ValidCombination_ID)
									.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
								int no = DB.executeUpdate(sql.toString(), get_TrxName());
								log.fine("New #" + no + " - "
									+ TableName + "." + ColumnName + " - " + C_ElementValue_ID
									+ " -- " + C_ValidCombination_ID + " -> " + newC_ValidCombination_ID);
								if (no == 1)
									retValue = UPDATE_YES;
							}
							else 
								retValue = UPDATE_SAME;
						}
						else
							log.log(Level.SEVERE, "Account not saved - " + newacct);
					}
					else	//	Replace Combination
					{
						//	Only Acct Combination directly
						sql = new StringBuffer ("UPDATE C_ValidCombination SET Account_ID=")
							.append(C_ElementValue_ID).append(" WHERE C_ValidCombination_ID=").append(C_ValidCombination_ID);
						int no = DB.executeUpdate(sql.toString(), get_TrxName());
						log.fine("Replace #" + no + " - "
								+ "C_ValidCombination_ID=" + C_ValidCombination_ID + ", New Account_ID=" + C_ElementValue_ID);
						if (no == 1)
						{
							retValue = UPDATE_YES;
							//	Where Acct was used
							sql = new StringBuffer ("UPDATE C_ValidCombination SET Account_ID=")
								.append(C_ElementValue_ID).append(" WHERE Account_ID=").append(Account_ID);
							no = DB.executeUpdate(sql.toString(), get_TrxName());
							log.fine("ImportAccount.updateDefaultAccount - Replace VC #" + no + " - "
									+ "Account_ID=" + Account_ID + ", New Account_ID=" + C_ElementValue_ID);
							sql = new StringBuffer ("UPDATE Fact_Acct SET Account_ID=")
								.append(C_ElementValue_ID).append(" WHERE Account_ID=").append(Account_ID);
							no = DB.executeUpdate(sql.toString(), get_TrxName());
							log.fine("ImportAccount.updateDefaultAccount - Replace Fact #" + no + " - "
									+ "Account_ID=" + Account_ID + ", New Account_ID=" + C_ElementValue_ID);
						}
					}	//	replace combination
				}	//	need to update
			}	//	for all default accounts
			else
				log.log(Level.SEVERE, "Account not found " + sql);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}

		return retValue;
	}	//	updateDefaultAccount

}
