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

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.common.*;
import org.compiere.common.constants.*;
import org.compiere.model.*;
import org.compiere.util.*;


/**
 *	Process Change Logs
 *	
 *  @author Jorg Janke
 *  @version $Id: ChangeLogProcess.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class ChangeLogProcess extends SvrProcess
{
	/** The Change Log (when applied directly)		*/
	private int				p_AD_ChangeLog_ID = 0;
	
	/** UnDo - Check New Value			*/
	private Boolean			p_CheckNewValue = null;
	/** ReDo - Check Old Value			*/
	private Boolean			p_CheckOldValue = null;
	/** Set Customization				*/
	private boolean			p_SetCustomization = false;
	/** Entity Type						*/
	private String			p_EntityType = null;

	/**	The Update Set Command		*/
	private StringBuffer	m_sqlUpdate = null;
	/**	The Where Clause			*/
	private StringBuffer	m_sqlUpdateWhere = null;
	/**	Is it an insert command		*/
	private boolean			m_isInsert = false;
	/**	The Insert Command			*/
	private StringBuffer	m_sqlInsert = null;
	/**	The Insert Value clause		*/
	private StringBuffer	m_sqlInsertValue = null;
	
	/** The Table					*/
	private MTable			m_table = null;
	/** The Column					*/
	private MColumn 		m_column = null;
	/** Old Record ID				*/
	private int				m_oldRecord_ID = 0;
	/**	Old Record2 ID				*/
	private String			m_oldRecord2_ID = "";
	/** Key Column Name				*/
	private String			m_keyColumn = null;
	/** Number of Columns			*/
	private int				m_numberColumns = 0;
	/** Array of Columns			*/
	private ArrayList<String>	m_columns = new ArrayList<String>();

	/**	Number of Errors			*/
	private int				m_errors = 0;
	/** Number of Failures			*/
	private int 			m_checkFailed = 0;
	/** Number of Successes			*/
	private int				m_ok = 0;

	
	/**
	 * 	Prepare
	 */
	@Override
	protected void prepare ()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) {
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("CheckNewValue"))
				p_CheckNewValue = Boolean.valueOf("Y".equals(element.getParameter()));
			else if (name.equals("CheckOldValue"))
				p_CheckOldValue = Boolean.valueOf("Y".equals(element.getParameter()));
			else if (name.equals("SetCustomization"))
				p_SetCustomization = "Y".equals(element.getParameter());
			else if (name.equals("EntityType"))
				p_EntityType = element.getParameter().toString();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		p_AD_ChangeLog_ID = getRecord_ID();
	}	//	prepare

	
	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt () throws Exception
	{
		if (p_SetCustomization)
			return setCustomization();
		
		log.info("AD_ChangeLog_ID=" + p_AD_ChangeLog_ID
			+ ", CheckOldValue=" + p_CheckOldValue + ", CheckNewValue=" + p_CheckNewValue);
		
		//	Single Change or All Customizations
		String sql = "SELECT * FROM AD_ChangeLog WHERE AD_ChangeLog_ID=? "
			+ "ORDER BY AD_Table_ID, Record_ID, AD_Column_ID";
		if (p_AD_ChangeLog_ID == 0){
			if (p_EntityType == null) 
				sql = "SELECT * FROM AD_ChangeLog WHERE IsCustomization='Y' AND IsActive='Y' "
					+ "ORDER BY AD_Table_ID, AD_ChangeLog_ID, Record_ID, AD_Column_ID";
			else
				sql = "SELECT cl.* FROM AD_ChangeLog cl, AD_Column c WHERE cl.IsCustomization='Y' "
					+ "AND cl.IsActive='Y' AND cl.AD_Column_ID = c.AD_Column_ID AND c.EntityType = ? "
					+ "ORDER BY cl.AD_Table_ID, cl.AD_ChangeLog_ID, cl.Record_ID, cl.AD_Column_ID";
		}
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			if (p_AD_ChangeLog_ID != 0)
				pstmt.setInt (1, p_AD_ChangeLog_ID);
			if (p_EntityType != null)
				pstmt.setString(1, p_EntityType);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				createStatement (new MChangeLog(getCtx(), rs, get_TrxName()), get_TrxName());
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
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
		//	final call
		executeStatement();
		
		return "@OK@: " + m_ok + " - @Errors@: " + m_errors + " - @Failed@: " + m_checkFailed;
	}	//	doIt

	
	/**
	 * 	Create + execute Statement
	 *	@param cLog log
	 *	@param trx p_trx
	 */
	private void createStatement (MChangeLog cLog, Trx trx)
	{
		//	New Table 
		if (m_table != null)
		{
			if (cLog.getAD_Table_ID() != m_table.getAD_Table_ID())
			{
				executeStatement();
				m_table = null;
			}
		}
		if (m_table == null)
			m_table = new MTable (getCtx(), cLog.getAD_Table_ID(), trx);

		//	New Record
		if (m_sqlUpdate != null 
			&& (cLog.getRecord_ID() != m_oldRecord_ID
				|| !cLog.getRecord2_ID().equals(m_oldRecord2_ID)))
			executeStatement();

		//	Column Info
		m_column = new MColumn (getCtx(), cLog.getAD_Column_ID(), get_TrxName());
		//	Same Column twice
		if (m_columns.contains(m_column.getColumnName()))
			executeStatement();
		m_columns.add(m_column.getColumnName());

		//	Create new Statement
		if (m_sqlUpdate == null)
		{
			String tableName = m_table.getTableName();
			m_keyColumn = m_table.getTableName() + "_ID";
			if (tableName.equals("AD_Ref_Table"))
				m_keyColumn = "AD_Reference_ID";
			//
			m_sqlUpdate = new StringBuffer ("UPDATE ")
				.append(tableName)
				.append(" SET ");
			//	Key
			m_sqlUpdateWhere = new StringBuffer (" WHERE ");
			if (cLog.getRecord_ID() != 0)
				m_sqlUpdateWhere.append(m_keyColumn).append("=").append(cLog.getRecord_ID());
			else
				m_sqlUpdateWhere.append(cLog.getRecord2_ID());
			m_oldRecord_ID = cLog.getRecord_ID();
			m_oldRecord2_ID = cLog.getRecord2_ID();
			
			//	Insert - new value is null and UnDo only
			m_isInsert = cLog.isNewNull() && p_CheckNewValue != null;
			if (m_isInsert)
			{
				m_sqlInsert = new StringBuffer ("INSERT INTO ")
					.append(tableName).append("(");
				m_sqlInsertValue = new StringBuffer (") VALUES (");
				if (cLog.getRecord_ID() != 0)
				{
					m_sqlInsert.append(m_keyColumn);
					m_sqlInsertValue.append(cLog.getRecord_ID());
					if (!m_keyColumn.equals(m_column.getColumnName()))
					{
						m_sqlInsert.append(",").append(m_column.getColumnName());
						m_sqlInsertValue.append(",").append(getSQLValue(cLog.getOldValue()));
					}
				}
			}
			m_numberColumns = 1;
		}
		//	Just new Column
		else
		{
			m_sqlUpdate.append(", ");
			//	Insert
			if (m_isInsert)
				m_isInsert = cLog.isNewNull();
			if (m_isInsert && !m_keyColumn.equals(m_column.getColumnName()))
			{
				m_sqlInsert.append(",").append(m_column.getColumnName());
				m_sqlInsertValue.append(",").append(getSQLValue(cLog.getOldValue()));
			}
			m_numberColumns++;
		}
		
		//	Update Set clause -- columnName=value
		m_sqlUpdate.append(m_column.getColumnName())
			.append("=");
		//	UnDo a <- (b)
		if (p_CheckNewValue != null)
		{
			m_sqlUpdate.append(getSQLValue(cLog.getOldValue()));
			if (p_CheckNewValue.booleanValue())
			{
				m_sqlUpdateWhere.append(" AND ").append(m_column.getColumnName());
				String newValue = getSQLValue(cLog.getNewValue());
				if (newValue == null || "NULL".equals(newValue))
					m_sqlUpdateWhere.append(" IS NULL");
				else
					m_sqlUpdateWhere.append("=").append(newValue);
			}
		}
		//	ReDo (a) -> b
		else if (p_CheckOldValue != null)
		{
			m_sqlUpdate.append(getSQLValue(cLog.getNewValue()));
			if (p_CheckOldValue.booleanValue())
			{
				String newValue = getSQLValue(cLog.getOldValue());
				m_sqlUpdateWhere.append(" AND ").append(m_column.getColumnName());
				if (newValue == null || "NULL".equals(newValue))
					m_sqlUpdateWhere.append(" IS NULL");
				else
					m_sqlUpdateWhere.append("=").append(newValue);
			}
		}
	}	//	createStatement

	/**
	 * 	Get SQL Value
	 *	@param value string value
	 *	@return sql compliant value
	 */
	private String getSQLValue (String value)
	{
		if (value == null || value.length() == 0 || value.equals("NULL"))
			return "NULL";
		
		//	Data Types
		if (FieldType.isNumeric (m_column.getAD_Reference_ID())
			|| FieldType.isID (m_column.getAD_Reference_ID()) )
			return value;
		if (DisplayTypeConstants.YesNo == m_column.getAD_Reference_ID()) 
		{
			if (value.equals("true"))
				return "'Y'";
			else
				return "'N'";
		}
		if (FieldType.isDate(m_column.getAD_Reference_ID()) )
			return DB.TO_DATE (Timestamp.valueOf(value));

		//	String, etc.
		return DB.TO_STRING(value);
	}	//	getSQLValue
	
	
	/**
	 *	Execute Statement
	 *	@return true if OK
	 */
	private boolean executeStatement()
	{
		if (m_sqlUpdate == null)
			return false;
		int no = 0;
		
		//	Insert SQL
		if (m_isInsert && m_numberColumns > 2)
		{
			m_sqlInsert.append(m_sqlInsertValue).append(")");
			log.info(m_sqlInsert.toString());
			//
			no = DB.executeUpdate(m_sqlInsert.toString(), get_TrxName());
			if (no == -1)
			{
			//	log.warning("Insert failed - " + m_sqlInsert);
				m_errors++;
			}
			else if (no == 0)
			{
				log.warning("Cannot reapply customization. No record inserted by " + m_sqlInsert);
				m_checkFailed++;
			}
			else
				m_ok++;
		}
		else	//	Update SQL
		{
			m_sqlUpdate.append(m_sqlUpdateWhere);
			log.info(m_sqlUpdate.toString());
			//
			no = DB.executeUpdate(m_sqlUpdate.toString(), get_TrxName());
			if (no == -1)
			{
			//	log.warning("Failed - " + m_sqlUpdate);
				m_errors++;
			}
			else if (no == 0)
			{
				log.warning("Cannot reapply customization. No record updated by " + m_sqlUpdate);
				m_checkFailed++;
			}
			else
				m_ok++;
		}
		//	Reset
		m_sqlUpdate = null;
		m_sqlUpdateWhere = null;
		m_sqlInsert = null;
		m_sqlInsertValue = null;
		m_columns = new ArrayList<String>();
		return no > 0;
	}	//	executeStatement
	
	/**
	 * 	Set Customization Flag
	 *	@return summary
	 */
	private String setCustomization()
	{
		log.info("");
		String sql = "UPDATE AD_ChangeLog SET IsCustomization='N' WHERE IsCustomization='Y'";
		int resetNo = DB.executeUpdate(sql, get_TrxName());
		
		int updateNo = 0;
		//	Get Tables
		sql = "SELECT * FROM AD_Table t "
		//	Table with EntityType
			+ "WHERE EXISTS (SELECT * FROM AD_Column c "
				+ "WHERE t.AD_Table_ID=c.AD_Table_ID AND c.ColumnName='EntityType')"
		//	Changed Tables
			+ " AND EXISTS (SELECT * FROM AD_ChangeLog l "
				+ "WHERE t.AD_Table_ID=l.AD_Table_ID)";
		StringBuffer update = null;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				MTable table = new MTable (getCtx(), rs, get_TrxName());
				
				String tableName = table.getTableName();
				String columnName = tableName + "_ID";
				if (tableName.equals("AD_Ref_Table"))
					columnName = "AD_Reference_ID";
				update = new StringBuffer ("UPDATE AD_ChangeLog SET IsCustomization='Y' "
					+ "WHERE AD_Table_ID=").append(table.getAD_Table_ID());
				update.append (" AND Record_ID IN (SELECT ")
					.append (columnName)
					.append (" FROM ").append(tableName)
					.append (" WHERE EntityType IN ('D','C'))");
				int no = DB.executeUpdate(update.toString(), get_TrxName());
				log.config(table.getTableName() + " = " + no);
				updateNo += no;
				
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql + " --- " + update, e);
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
		
		return "@Reset@: " + resetNo + " - @Updated@: " + updateNo;
	}	//	setCustomization
	
}	//	ChangeLogProcess
