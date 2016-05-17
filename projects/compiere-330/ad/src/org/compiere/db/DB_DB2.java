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
package org.compiere.db;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.sql.*;

import org.compiere.common.constants.*;
import org.compiere.startup.*;
import org.compiere.util.*;

import com.ibm.db2.jcc.*;

/**
 * 	DB2 Database Driver
 *	
 *  @author Jorg Janke
 *  @version $Id: DB_DB2.java,v 1.5 2006/09/22 23:35:19 jjanke Exp $
 */
public class DB_DB2 implements CompiereDatabase
{
	/**
	 * 	Database DB2
	 */
	public DB_DB2()
	{
		try
		{
			getDriver();
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, e.getMessage());
		}
	}	//	DB_DB2
	
	/** Static Driver           	*/
	private static DB2Driver	   	s_driver = null;
	/** Driver Class Name			*/
	public static final String		DRIVER = "com.ibm.db2.jcc.DB2Driver";
	/** Type 2 Driver				*/
	public static final String		DRIVER2 = "COM.ibm.db2.jdbc.app.DB2Driver";
	
	/** Default Port 50000           	*/
	//jz public static final int 		DEFAULT_PORT = 446;
	public static final int 		DEFAULT_PORT = 50000;
	/** Default Port 50000         	*/
	public static final int 		DEFAULT_PORT_0 = 446;
	/** Default database name          	*/
	public static final String 		DEFAULT_DBNAME = "compiere";
	
    /** Cached User Name			*/
    private String					m_userName = null;
	
	/** Connection String       	*/
	private String          		m_connectionURL;
	/** Data Source					*/
	private volatile DB2DataSource	m_ds = null;
	/** Connection					*/
	private Connection 				m_conn = null;

	/** Statement Converter     */
	private Convert         m_convert = new Convert(Environment.DBTYPE_DB2);

	/**	Logger	*/
	private static CLogger log = CLogger.getCLogger (DB_DB2.class);

	/** Random for key */
	private 		java.util.Random r_key = new java.util.Random(123456);

	
	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	@Override
	public String getName()
	{
		return Environment.DBTYPE_DB2;
	}   //  getName

	/**
	 *  Get Database Description
	 *  @return database long name and version
	 */
	@Override
	public String getDescription()
	{
		try
		{
			if (s_driver == null)
				getDriver();
		}
		catch (Exception e)
		{
		}
		if (s_driver != null)
			return s_driver.toString();
		return "No Driver";
	}   //  getDescription

	/**
	 *  Get Standard JDBC Port
	 *  @return standard port
	 */
	@Override
	public int getStandardPort()
	{
		return DEFAULT_PORT_0;
	}   //  getStandardPort

	/**
	 *  Get and register Database Driver
	 *  @return Driver
	 *	@throws SQLException
	 */
	@Override
	public Driver getDriver() throws SQLException
	{
		if (s_driver == null)
		{
			s_driver = new DB2Driver();
			DriverManager.registerDriver (s_driver);
			DriverManager.setLoginTimeout (Database.CONNECTION_TIMEOUT);
		}
		return s_driver;
	}   //  getDriver

	/**
	 *  Get Database Connection String.
	 *  <pre>
	 *  Timing:
	 *  </pre>
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	@Override
	public String getConnectionURL (CConnection connection)
	{
		StringBuffer sb = null;
		//	connection//server:port/database
		sb = new StringBuffer ("jdbc:db2:");
		//	Cloudscape = jdbc:db2j:net: 
		sb.append("//")
			.append(connection.getDbHost())
			.append(":").append(connection.getDbPort())
			.append("/").append(connection.getDbName()+":retrieveMessagesFromServerOnGetMessage=true;");
		m_connectionURL = sb.toString();
	//	log.config(m_connectionURL);
		//
		m_userName = connection.getDbUid();
		return m_connectionURL;
	}   //  getConnectionURL

	/**
	 * 	Get Connection URL.
	 *	@param dbHost db Host
	 *	@param dbPort db Port
	 *	@param dbName db Name
	 *	@param userName user name
	 *	@return connection
	 */
	@Override
	public String getConnectionURL (String dbHost, int dbPort, String dbName, 
		String userName)
	{
		m_userName = userName;
		m_connectionURL = "jdbc:db2://" 
			+ dbHost + ":" + dbPort + "/" + dbName + ":retrieveMessagesFromServerOnGetMessage=true;";
		return m_connectionURL;
	}	//	getConnectionURL

	/**
	 *  Get Database Connection String
	 *  @param connectionURL Connection URL
	 *  @param userName user name
	 *  @return connection String
	 */
	@Override
	public String getConnectionURL (String connectionURL, String userName)
	{
		m_userName = userName;
		m_connectionURL = connectionURL;
		return m_connectionURL;
	}	//	getConnectionURL

	/**
	 * 	Get JDBC Catalog
	 *	@return null - not used
	 */
	@Override
	public String getCatalog()
	{
		return null;
	}	//	getCatalog
	
	/**
	 * 	Get JDBC Schema
	 *	@return user name
	 */
	@Override
	public String getSchema()
	{
		if (m_userName != null)
			return m_userName.toUpperCase();
		log.severe("User Name not set (yet) - call getConnectionURL first");
		return null;
	}	//	getSchema

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	@Override
	public boolean supportsBLOB()
	{
		return true;
	}   //  supportsBLOB

	/**
	 *  String Representation
	 *  @return info
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("DB_DB2[");
		sb.append(m_connectionURL);
		sb.append("]");
		return sb.toString();
	}   //  toString

	/**
	 * 	Get Status
	 * 	@return status info
	 */
	@Override
	public String getStatus()
	{
		StringBuffer sb = new StringBuffer();
		return sb.toString();
	}	//	getStatus

	
	
	/**************************************************************************
	 * jz
	 * 
	 * 	rewrite SQL if there is ROWNUM=1 in a query with  updated in select max(updated) from ...
	 *  this is not a generic solution, with
	 *  assume: 1. only ROWNUM=1
	 *  		2. it is in a query with 1 table and the table has a column called "UPDATED"
	 *  		3. key words in upper case
	 *  
	 *	@param oraS oracle style statement
	 *	@return statement
	 */
	public String repRownum (String oraS)
	{
		return DBUtils.repRownum(oraS);

	}	
		
	
	
	
	/**************************************************************************
	 *  Convert an individual Oracle Style statements to target database statement syntax.
	 *  @param oraStatement oracle statement
	 *  @return converted Statement oracle statement
	 */
	@Override
	public String convertStatement (String oraStatement)
	{
		if (oraStatement.startsWith("ALTER TABLE") && oraStatement.indexOf(" MODIFY ")>0)
		{
			String tokens[] = oraStatement.split(" ");
			String sql = "ALTER TABLE " + tokens[2] + " ALTER " + tokens[4];
			int idef = oraStatement.indexOf(" DEFAULT ");
			if (idef>0 || oraStatement.indexOf(" NULL")<0)
			{
				int i = sql.length() + 1; //alter v.s. modify
				if (idef > 0)
				{
					sql += " SET DATA TYPE " + oraStatement.substring(i, idef+1); //type stuff
					sql += ", ALTER " + tokens[4] + " SET DEFAULT " + oraStatement.substring(idef + 9, oraStatement.length());
				}
				else
					sql += " TYPE " + oraStatement.substring(i, oraStatement.length());
				oraStatement = sql;				
			}
			else
			{
				if (oraStatement.indexOf(" NOT NULL")>0)
				{
					sql += " SET NOT NULL";
					return sql;
				}
				else if (oraStatement.indexOf(" NULL")>0)
				{
					sql += " DROP NOT NULL";
					return sql;				
				}
			}
		}

		//jz return oraStatement;
		//rewrite SQL if there is ROWNUM=1 in a query with  updated in select max(updated) from ...
		//oraStatement = oraStatement.replaceAll("DECIMAL(10,0)", "INTEGER"); //jz
		//oraStatement = oraStatement.replaceAll("DECIMAL(22,0)", "BIGINT");
		//jz //TODO all these replacement are temp solutions
		while (oraStatement.indexOf("DECIMAL(10,0)")>-1)
			oraStatement = oraStatement.replace("DECIMAL(10,0)", "INTEGER");
		while (oraStatement.indexOf("currencyBase(invoiceOpen")>-1)
			oraStatement = oraStatement.replace("currencyBase(invoiceOpen", "currencyBaseD(invoiceOpen");
		if (oraStatement.startsWith("SELECT p.M_Product_ID, p.Discontinued, p.Value, p.Name, bomQtyAvailable"))
			while (oraStatement.indexOf("(p.M_Product_ID,?,0)")>-1)
				oraStatement = oraStatement.replace("(p.M_Product_ID,?,0)", "(p.M_Product_ID,cast(? as integer),0)");
		if (oraStatement.startsWith("SELECT C_BPartner_ID,C_Currency_ID, invoiceOpen(C_Invoice_ID, ?),"))
		{
			oraStatement = oraStatement.replace("invoiceOpen(C_Invoice_ID, ?),", "invoiceOpen(C_Invoice_ID, cast(? as integer)),");
			oraStatement = oraStatement.replace("invoiceDiscount(C_Invoice_ID,?,?),", "invoiceDiscount(C_Invoice_ID,cast(? as timestamp),cast(? as integer)),");
		}
		if (oraStatement.startsWith("SELECT C_Invoice_ID, currencyConvert(invoiceOpen("))
		{
			oraStatement = oraStatement.replace("currencyConvert(invoiceOpen(i.C_Invoice_ID, 0),i.C_Currency_ID, ?,?, i.C_ConversionType_ID,i.AD_Client_ID,i.AD_Org_ID)", 
					"currencyConvertD(invoiceOpen(i.C_Invoice_ID, 0),i.C_Currency_ID, cast(? as integer),cast(? as timestamp), i.C_ConversionType_ID,i.AD_Client_ID,i.AD_Org_ID)");
			oraStatement = oraStatement.replace("currencyConvert(paymentTermDiscount(i.GrandTotal,i.C_Currency_ID,i.C_PaymentTerm_ID,i.DateInvoiced, ?),i.C_Currency_ID, ?,?,i.C_ConversionType_ID,i.AD_Client_ID,i.AD_Org_ID)", 
				"currencyConvertD(paymentTermDiscount(i.GrandTotal,i.C_Currency_ID,i.C_PaymentTerm_ID,i.DateInvoiced, cast(? as timestamp)),i.C_Currency_ID, cast(? as integer),cast(? as timestamp),i.C_ConversionType_ID,i.AD_Client_ID,i.AD_Org_ID)");
			oraStatement = oraStatement.replace("paymentTermDiscount(invoiceOpen(C_Invoice_ID, 0), C_Currency_ID, C_PaymentTerm_ID, DateInvoiced, ?)", 
				"paymentTermDiscountD(invoiceOpen(C_Invoice_ID, 0), C_Currency_ID, C_PaymentTerm_ID, DateInvoiced, cast(? as timestamp))");
			oraStatement = oraStatement.replace("paymentTermDueDays(C_PaymentTerm_ID, DateInvoiced, ?)", 
				"paymentTermDueDays(C_PaymentTerm_ID, DateInvoiced, cast(? as timestamp))");
		}
		if (oraStatement.startsWith("SELECT currencyConvert("))
		{
			oraStatement = oraStatement.replace("currencyConvert(ol.PriceCost, o.C_Currency_ID, ?,", "currencyConvert(ol.PriceCost, o.C_Currency_ID, cast(? as integer),");
			oraStatement = oraStatement.replace("currencyConvert(ol.PriceActual, o.C_Currency_ID, ?,", "currencyConvert(ol.PriceActual, o.C_Currency_ID, cast(? as integer),");
			oraStatement = oraStatement.replace("currencyConvert(il.PriceActual, i.C_Currency_ID, ?,", "currencyConvert(il.PriceActual, i.C_Currency_ID, cast(? as integer),");
		}
		
		if (oraStatement.startsWith("UPDATE "))
		{
			oraStatement = DBUtils.updateSetSelectList(oraStatement);
		}

		//PaySelectionCreateFrom.doIt: doIt - 
		//SELECT C_Invoice_ID, currencyConvert(invoiceOpen(i.C_Invoice_ID, 0),i.C_Currency_ID, ?,?, i.C_ConversionType_ID,i.AD_Client_ID,i.AD_Org_ID),
		//currencyConvert(paymentTermDiscount(i.GrandTotal,i.C_Currency_ID,i.C_PaymentTerm_ID,i.DateInvoiced, ?),i.C_Currency_ID, ?,?,i.C_ConversionType_ID,i.AD_Client_ID,i.AD_Org_ID), 
		//PaymentRule, IsSOTrx FROM C_Invoice i 
		//WHERE IsSOTrx='N' AND IsPaid='N' AND DocStatus IN ('CO','CL') AND AD_Client_ID=? AND NOT EXISTS 
		//(SELECT * FROM C_PaySelectionLine psl WHERE i.C_Invoice_ID=psl.C_Invoice_ID AND psl.IsActive='Y' AND psl.C_PaySelectionCheck_ID IS NOT NULL) AND i.IsInDispute='N' AND PaymentRule=? AND C_BPartner_ID=? [167]
		//paymentTermDiscount(invoiceOpen(C_Invoice_ID, 0), C_Currency_ID, C_PaymentTerm_ID, DateInvoiced, ?)
		//paymentTermDueDays(C_PaymentTerm_ID, DateInvoiced, ?)
		//
		
		oraStatement = repRownum(oraStatement);
		//jz screen and replace key words, such as "YEAR"
		//oraStatement = repWords(oraStatement);
		String retValue[] = m_convert.convert(oraStatement);
		/*//jz there is error from formal convert, just return the original SQL to let DB2 to execute it
		if (retValue == null)
			throw new IllegalArgumentException
				("Not Converted (" + oraStatement + ") - "
					+ m_convert.getConversionError());
		if (retValue.length != 1)
			throw new IllegalArgumentException
				("Convert Command Number=" + retValue.length
					+ " (" + oraStatement + ") - " + m_convert.getConversionError());
			*/
		if (retValue == null)
		{
			log.severe("Not Converted (" + oraStatement + ") - "
					+ m_convert.getConversionError());
			return oraStatement;
		}
		if (retValue.length != 1)
		{
			log.severe("Convert Command Number=" + retValue.length
					+ " (" + oraStatement + ") - " + m_convert.getConversionError());
			return oraStatement;
		}
		//  Diagnostics (show changed, but not if AD_Error
		if (!oraStatement.equals(retValue[0]) && retValue[0].indexOf("AD_Error") == -1)
			log.finest("=>" + retValue[0] + "<= [" + oraStatement + "]");
		//
		
		//jz: check if we support the sql, if not, return ""
		if (!isSupported(retValue[0]))
		{
			log.finest("DB2 doesn't support this sql: " + retValue[0]);
			return "";
		}
		return retValue[0];

	}   //  convertStatement


	/**
	 *  Check if DBMS support the sql statement
	 *  @sql SQL statement
	 *  @return true: yes
	 */
	@Override
	public boolean isSupported(String sql)
	{
		if (sql.equals("ResultSet_MoveToInsertRow"))
			return false;
		return true;
		//jz temp, modify later
	}
	

	/**
	 *  Get constraint type associated with the index
	 *  @tableName table name
	 *  @IXName Index name
	 *  @return String[0] = 0: do not know, 1: Primary Key  2: Foreign Key
	 *  		String[1] - String[n] = Constraint Name
	 */
	@Override
	public String getConstraintType(Connection conn, String tableName, String IXName) 
	{
		if (IXName == null || IXName.length()==0)
			return "0";
		
		String sqlp = "select a.uniquerule, b.dconstname from sysibm.sysindexes a "
			+ "join sysibm.sysconstdep b on (a.creator=b.bcreator and a.name=b.bname) "
			+ "where a.creator='" + IXName + "' and a.uniquerule='P' and b.btype='I' and "
			+ "a.name='PK_EMPLOYEE'" ;

		//Connection conn = null;
		try
		{
			//conn = m_ds.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs1 = null;
			String ct = null;
			String cn = null;
			
			rs1 = stmt.executeQuery(sqlp);
			
			if (rs1 == null)
			{
				//conn.close();
				return "0";
			}
			else
			{
				ct = null;
				cn = null;
				if (rs1.next())
				{
					ct = rs1.getString(1);
					cn = rs1.getString(2);
					if (ct != null && ct.equals("P"))
						ct = "1";
					else if (ct != null && ct.equals("F")) 
						ct = "2";
					else
						ct = "0";
				}//if
			}		
			//conn.close();
			if (ct == null)
				return "0";
			else if (cn == null)
				return ct;
			else
				return ct+cn;
		}
		catch (Exception ex)
		{
			//if (conn != null)
				//conn.close();
			ex.printStackTrace();
		}
		
		return "0";
		//jz temp, modify later
	}

	/**
	 *  Get Name of System User
	 *  @return system
	 */
	@Override
	public String getSystemUser()
	{
		return "db2adm";
	}	//	getSystemUser
	
	/**
	 *  Get Name of System Database
	 *  @param databaseName database Name
	 *  @return e.g. master or database Name
	 */
	@Override
	public String getSystemDatabase(String databaseName)
	{
		return databaseName;
	}	//	getSystemDatabase


	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('1999-12-31 23:59:59', 'YYYY-MM-DD HH24:MI:SS')
	 *  or TIMESTAMP('2000-01-10-00.00.00.000000')
	 */
	@Override
	public String TO_DATE (Timestamp time, boolean dayOnly)
	{
		//return DBUtils.TO_DATE(time, dayOnly);
		
		if (time == null)
		{
			if (dayOnly)
				return "trunc(CURRENT TIMESTAMP, 'DD')"; //jz default to 'DD'
			return "CURRENT TIMESTAMP";
		}

		//	TIMESTAMP('2000-01-10-00.00.00.000000')
		StringBuffer dateString = new StringBuffer("TIMESTAMP('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		/*
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("-00.00.00.000000')");
		}
		else
		{
			myDate = myDate.replace('-', ' ');
			myDate = myDate.replace(':', '.');
			dateString.append(myDate);
			dateString.append("00')");
		}
		*/
		//jz DB2 timestamp string YYYYMMDDHHMISS 14 chars
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time.getTime());
		
		String myDate = Integer.toString(cal.get(Calendar.YEAR));
		
		String mm =  Integer.toString(cal.get(Calendar.MONTH)+1);
		if (mm.length()==1)
			mm = "0" + mm;
		myDate += mm;
		
		mm =  Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		if (mm.length()==1)
			mm = "0" + mm;
		myDate += mm;
		
		if (dayOnly)
		{
			dateString.append(myDate + "000000')");
		}
		else
		{
			mm =  Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
			if (mm.length()==1)
				mm = "0" + mm;
			myDate += mm;
			
			mm =  Integer.toString(cal.get(Calendar.MINUTE));
			if (mm.length()==1)
				mm = "0" + mm;
			myDate += mm;
			
			mm =  Integer.toString(cal.get(Calendar.SECOND));
			if (mm.length()==1)
				mm = "0" + mm;
			myDate += mm;
			
			dateString.append(myDate + "')");
		}

		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	 *  @see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *   */
	@Override
	public String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		return DBUtils.TO_CHAR(columnName, displayType, AD_Language);
		//jz need more work later 

	}   //  TO_CHAR

	
	/**
	 * Create DataSource
	 * 
	 * @param connection
	 *            connection
	 * @return data dource
	 */
	@Override
	public DataSource getDataSource(CConnection connection) {
		if (m_ds == null) {
			DB2DataSource ds = new DB2DataSource();
			ds.setServerName(connection.getDbHost());
			ds.setPortNumber(connection.getDbPort());
			ds.setDatabaseName(connection.getDbName());
			ds.setDescription("Compiere DataSource");
			ds.setUser(connection.getDbUid());
			ds.setPassword(connection.getDbPwd());
			ds.setLoginTimeout(5); // seconds
			ds.setCurrentSchema("COMPIERE");// jz

			// ds.setUseCachedCursor(true);
			m_ds = ds;
		}
		return m_ds;
	} // getDataSource


	/**
	 * Get new Connection from Cache
	 * 
	 * @param connection
	 *            info
	 * @param autoCommit
	 *            true if autocommit connection
	 * @param transactionIsolation
	 *            Connection transaction level
	 * @return connection or null
	 * @throws Exception
	 */
	@Override
	public Connection getCachedConnection (CConnection connection, 
		boolean autoCommit, int transactionIsolation)
		throws SQLException
	{
		Connection conn = getDataSource(connection).getConnection();
		if (conn != null) {
			conn.rollback();
			if (conn.getTransactionIsolation() != transactionIsolation)
				conn.setTransactionIsolation(transactionIsolation);
			if (conn.getAutoCommit() != autoCommit)
				conn.setAutoCommit(autoCommit);
		}
		return conn;
	}	//	getCachedConnection

	/**
	 * 	Get new Connection from Driver
	 *	@param connection info
	 *	@return connection or null
	 *	@throws SQLException
	 */
	@Override
	public Connection getDriverConnection (CConnection connection) throws SQLException
	{
		getDriver();
		m_conn = DriverManager.getConnection (getConnectionURL (connection), 
			connection.getDbUid(), connection.getDbPwd());
		return m_conn;
	}	//	getDriverConnection

	/**
	 * 	Get new Driver Connection
	 *	@param dbUrl URL
	 *	@param dbUid user
	 *	@param dbPwd password
	 *	@return connection
	 *	@throws SQLException
	 */
	@Override
	public Connection getDriverConnection (String dbUrl, String dbUid, String dbPwd) 
		throws SQLException
	{
		getDriver();
		m_conn = DriverManager.getConnection (dbUrl, dbUid, dbPwd);
		return m_conn;
	}	//	getDriverConnection

	/**
	 * 	Close
	 */
	@Override
	public void close()
	{
		log.config(toString());
		m_ds = null;
	}	//	close

	/**
	 * 	Clean up
	 */
	public void cleanup()
	{
		log.config("");
	}	//	cleanup

	
	/**
	 * 	Get Data Type
	 *	@param displayType display type
	 *	@param precision precision
	 *	@param defaultValue if true adds default value
	 *	@return data type
	 */
	public String getDataType (int displayType, int precision,
		boolean defaultValue)
	{
		String retValue = null;
		switch (displayType)
		{
			//	IDs
			case DisplayTypeConstants.Account:
			case DisplayTypeConstants.Assignment:
			case DisplayTypeConstants.Color:
			case DisplayTypeConstants.ID:
			case DisplayTypeConstants.Location:
			case DisplayTypeConstants.Locator:
			case DisplayTypeConstants.PAttribute:
			case DisplayTypeConstants.Search:
			case DisplayTypeConstants.Table:
			case DisplayTypeConstants.TableDir:
			case DisplayTypeConstants.Image:
				retValue = "INTEGER";
				break;
				
			// Dynamic Precision
			case DisplayTypeConstants.Amount:
				retValue = "DECIMAL(18,2)";
				if (defaultValue)
					retValue += " DEFAULT 0";
				break;
				
			case DisplayTypeConstants.Binary:
				retValue = "BLOB";
				break;
				
			case DisplayTypeConstants.Button:
				retValue = "CHAR(1)";
				break;
				
			// Number Dynamic Precision
			case DisplayTypeConstants.CostPrice:
				retValue = "DECIMAL(22,6)";
				if (defaultValue)
					retValue += " DEFAULT 0";
				break;
				
			//	Date	
			case DisplayTypeConstants.Date:
			case DisplayTypeConstants.DateTime:
			case DisplayTypeConstants.Time:
				retValue = "Timestamp";
				if (defaultValue)
					retValue += " DEFAULT 0";
				break;
				
			// 	Number(10)
			case DisplayTypeConstants.Integer:
				retValue = "NUMBER(10)";
				break;
				
			case DisplayTypeConstants.List:
				retValue = "CHAR(" + precision + ")";
				break;

			//	NVARCHAR
			case DisplayTypeConstants.Memo:
			case DisplayTypeConstants.String:
			case DisplayTypeConstants.Text:
				retValue = "NVARCHAR(" + precision + ")";
				break;

			case DisplayTypeConstants.TextLong:
				retValue = "CLOB";
				break;

			//	Dyn Prec
			case DisplayTypeConstants.Quantity:
				retValue = "NUMBER";
				break;

			case DisplayTypeConstants.YesNo:
				retValue = "CHAR(1)";
				break;
				
			default:
				log.severe("Unknown: " + displayType);
				break;
		}
		return retValue;
	}	//	getDataType

	
	
	private String getNewkey(String oldkey, String ktype, String sql)
	{
		String newKey = null;
		
		String s_key = null;
		if (ktype.equals("K") || m_conn == null)//pk
		{
			int ikey = r_key.nextInt();
			if (ikey <0)
				ikey *= -1;
			s_key = Integer.toString(ikey);
			if (s_key.length()>12)
				s_key = s_key.substring(s_key.length()-12,s_key.length());
			newKey = ktype+s_key+"_KEY";
			//newKey.replace('-','_');
			
			return newKey;
		}
		else //fk && conn assigned
		{
			//get AD_table_IDs
			int it1 = sql.indexOf("ALTER TABLE ")+12;
			int it1e = sql.indexOf(" ADD ", it1);
			int it2 = sql.indexOf("REFERENCES ")+11;
			int it2e = sql.indexOf("(", it2);
			if (it1 == -1 || it1e==-1 ||it2==-1 || it2e==-1)
				return oldkey;
			
			String[] tbname = {sql.substring(it1, it1e),sql.substring(it2, it2e)};
			int[] tid = {0,0};
			try
			{
				Statement s = m_conn.createStatement();
				
				for (int i=0; i<tbname.length; i++)
				{
				 
					String sql1 = "select ad_table_id from ad_table where tablename = '" + tbname[i] + "'";
					ResultSet rs = s.executeQuery(sql1);
					if (rs.next())
					{
						tid[i] = rs.getInt(1);
					}
					rs.close();
				}
				if (tid[0]==0 || tid[1] ==0)
					return oldkey;
				s_key = ktype+String.valueOf(tid[0])+"_"+String.valueOf(tid[1]);
				return s_key;
			}
			catch (SQLException e)
			{
				//
				return oldkey;
			}
			
		}
	}
	/**
	 * jz
	 * 
	 *  Check and generate an alternative SQL. It only handle constraint name length > 18 in create table PK and alter table FK .
	 *  @reExNo number of re-execution
	 *  @msg previous execution error message
	 *  @sql previous executed SQL
	 *  @return String, the alternative SQL, null if no alternative
	 */

	@Override
	public String getAlternativeSQL(int reExNo, String msg, String sql)
	{
		if (sql == null || sql.length() == 0)
			return null;
		
		
		StringBuffer newSQL = null;
		int is = msg.indexOf("The name \"") ;
		int ie = msg.indexOf("\" is too long.  The maximum length ") ;
		int ifk = sql.indexOf("FOREIGN KEY");
		if (sql.startsWith("CREATE TABLE ") && is>=0 && ie>0
				|| sql.startsWith("ALTER TABLE ") && is>=0 && ie>0 && ifk>0)
		{
			String ktype = "K";
			if (ifk>0)
				ktype = "F";
			
			String oldkey = msg.substring(is+10,ie);
			String newKey = getNewkey(oldkey, ktype, sql);
			
			if (newKey==null)
				return null;
			
			newSQL = new StringBuffer(sql);
			int bk = 0;
			int ak = 0;
			if (sql.startsWith("CREATE TABLE "))
			{
				bk = sql.indexOf(" CONSTRAINT ") + 12;
				ak = sql.indexOf(" PRIMARY KEY ", bk);
			}
			else
			{
				bk = sql.indexOf(" CONSTRAINT ") + 12;
				ak = sql.indexOf(" FOREIGN KEY ", bk);
			}
			
			return oldkey + " | " + newSQL.toString().substring(0,bk)  + newKey  + newSQL.toString().substring(ak,newSQL.length());
		}		
		
		return CConstraint.forTrigger(reExNo, msg, sql); 
	}

	
	


	/**
	 *  Get a string representation of literal used in SQL clause
	 *
	 *  @param  sqlClause "S", "U","I", "W"
	 *  @param  dataType java.sql.Types
	 *
	 *  @return derby: nullif(x,x)
	 */
	
	@Override
	public String nullValue (String sqlClause, int dataType)
	{
		return DBUtils.nullValue(sqlClause, dataType);
	}   //  


	/**
	 * 
	 * jz
	 * 
	 *  change update set (...) = (select ... from ) standard format 
	 *
	 *  @param  sql update clause
	 *
	 *  @return new sql
	 */
	
	@Override
	public String updateSetSelectList (String sql)
	{
		return DBUtils.updateSetSelectList(sql);
	}   //  

	
	/**
	 *  Get the Database specific Clob data type
	 *  @param connection connection
	 *  @param clobString clob string
	 *  @return Clob
	 */
	@Override
	public Clob getClob(Connection con, String clobString)
	{
		return null;
	}  // getClob()
	
	/**
	 *  Get the Database specific Blob data type
	 *  @param connection connection
	 *  @param bytes bytes
	 *  @return Blob
	 */
	@Override
	public Blob getBlob(Connection con, byte[] bytes)
	{
		return null;
	}  // getBlob()
	
	/**************************************************************************
	 * 	Testing
	 * 	@param args ignored
	 */
	public static void main (String[] args)
	{
		/**
		Compiere.startupEnvironment(true);
		CConnection cc = CConnection.get();
		DB_Oracle db = (DB_Oracle)cc.getDatabase();
		db.cleanup();
		
		try
		{
			Connection conn = ;
		//	System.out.println("Driver=" + db.getDriverConnection(cc));
			DataSource ds = db.getDataSource(cc);
			System.out.println("DS=" + ds.getConnection());
			conn = db.getCachedConnection(cc, true, Connection.TRANSACTION_READ_COMMITTED);
			System.out.println("Cached=" + conn);
			System.out.println(db);
			//////////////////////////
			System.out.println("JAVA classpath: [\n" +
				System.getProperty("java.class.path") + "\n]");
				DatabaseMetaData dmd = conn.getMetaData();
				System.out.println("DriverVersion: ["+
				dmd.getDriverVersion()+"]");
				System.out.println("DriverMajorVersion: ["+
				dmd.getDriverMajorVersion()+"]");
				System.out.println("DriverMinorVersion: ["+
				dmd.getDriverMinorVersion()+"]");
				System.out.println("DriverName: ["+
				dmd.getDriverName()+"]");
				System.out.println("ProductName: ["+
				dmd.getDatabaseProductName() +"]");
				System.out.println("ProductVersion: [\n"+
				dmd.getDatabaseProductVersion()+"\n]"); 
			//////////////////////////
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		db.cleanup();
		
		System.out.println("--------------------------------------------------");
		try
		{
			Connection conn1 = db.getCachedConnection(cc, false, Connection.TRANSACTION_READ_COMMITTED);
			Connection conn2 = db.getCachedConnection(cc, true, Connection.TRANSACTION_READ_COMMITTED);
			Connection conn3 = db.getCachedConnection(cc, false, Connection.TRANSACTION_READ_COMMITTED);
			System.out.println("3 -> " + db);
			conn1.close();
			conn2.close();
			conn1 = db.getCachedConnection(cc, true, Connection.TRANSACTION_READ_COMMITTED);
			conn2 = db.getCachedConnection(cc, true, Connection.TRANSACTION_READ_COMMITTED);
			System.out.println("3 -> " + db);
			conn1.close();
			conn2.close();
			conn3.close();
			System.out.println("0 -> " + db);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		
		db.cleanup();
		
	//	System.exit(0);
		System.out.println("--------------------------------------------------");
		
		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));

		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED));

		System.out.println(db);


		try
		{
			System.out.println("-- Sleeping --");
			Thread.sleep(60000);
			System.out.println(db);
			db.close();
			db.cleanup();
			System.out.println(db);
		}
		catch (InterruptedException e)
		{
		}
		/** **/
		
		
		/**	**/
		//	Connection option 1
		try
		{
			DB2Driver driver = new DB2Driver();
			DriverManager.registerDriver(driver);
			
			Connection con = DriverManager.getConnection("jdbc:db2://dev1:50000/sample",
				"db2admin", "db2admin");
//				"compiere", "compiere");
//				"db2inst1", "daDm7rfr");
			System.out.println("Connection Catalog = " + con.getCatalog());
			//
			DatabaseMetaData md = con.getMetaData();
			System.out.println(md.getDatabaseProductName() + " - " + md.getDatabaseProductVersion());
		//	System.out.println(md.getDatabaseMajorVersion() + " - " + md.getDatabaseMinorVersion());
			System.out.println(md.getDriverName() + " - " + md.getDriverVersion());
		//	System.out.println(md.getDriverMajorVersion() + " - " + md.getDriverMinorVersion());
			System.out.println("URL=" + md.getURL());
			System.out.println("User=" + md.getUserName());
			//
			System.out.println(md.getNumericFunctions());
			System.out.println(md.getStringFunctions());
			System.out.println(md.getTimeDateFunctions());
			System.out.println(md.getSystemFunctions());
			//
			System.out.println("Catalogs - " + md.getCatalogTerm());
			ResultSet rs = md.getCatalogs();
			while (rs.next())
				System.out.println("- " + rs.getString(1));
			//
			System.out.println("Schemas - " + md.getSchemaTerm());
			rs = md.getSchemas();
			while (rs.next())
				System.out.println("- " + rs.getString(1));

			
			String sql = "SELECT GRANTOR,GRANTEE,DBADMAUTH FROM SYSCAT.DBAUTH";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = con.prepareStatement (sql);
				rs = pstmt.executeQuery ();
				while (rs.next ())
				{
					String GRANTOR = rs.getString(1);
					String GRANTEE = rs.getString(2);
					String DBADMAUTH = rs.getString(3);
					System.out.println(GRANTOR + " -> " + GRANTEE + " = " + DBADMAUTH);
				}
				rs.close ();
				pstmt.close ();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.log (Level.SEVERE, sql, e);
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
			
			
			System.out.println("SysCat Table");
			rs = md.getTables(null, "SYSCAT", null, new String[] {"TABLE", "VIEW"});
			while (rs.next())
				System.out.println("- User=" + rs.getString(2) + " | Table=" + rs.getString(3)
					+ " | Type=" + rs.getString(4) + " | " + rs.getString(5));
			//
			System.out.println("Column");
			rs = md.getColumns(null, "SYSCAT", "DBAUTH", null);
			while (rs.next())
				System.out.println("- Tab=" + rs.getString(3) + " | Col=" + rs.getString(4)
					+ " | Type=" + rs.getString(5) + ", " + rs.getString(6)
					+ " | Size=" + rs.getString(7) + " | " + rs.getString(8)
					+ " | Digits=" + rs.getString(9) + " | Radix=" + rs.getString(10)
					+ " | Null=" + rs.getString(11) + " | Rem=" + rs.getString(12)
					+ " | Def=" + rs.getString(13) + " | " + rs.getString(14)
					+ " | " + rs.getString(15) + " | " + rs.getString(16)
					+ " | Ord=" + rs.getString(17) + " | Null=" + rs.getString(18)
					);

			con.close();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		/** **/
	}	//	main
	
}	//	DB_DB2
