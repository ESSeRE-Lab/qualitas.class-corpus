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
import java.util.logging.*;

import javax.sql.*;

import org.compiere.*;
import org.compiere.common.*;
import org.compiere.common.constants.*;
import org.compiere.startup.*;
import org.compiere.util.*;
import org.compiere.util.Util;

import com.microsoft.sqlserver.jdbc.*;
/**
 *  SQL Server Database Port
 *
 */
public class DB_SQLServer implements CompiereDatabase
{
	/**
	 *  Microsoft SQL Server Database
	 *  
	 *  @author Jinglun Zhang/Compiere, Miklos Keresztes-Vegh/Bit Software 
	 */
	public DB_SQLServer()
	{
	}   //  DB_SQLServer

	/** Static Driver           	*/
	private static SQLServerDriver s_driver = null;

	/** Driver Class Name			
	 * Additional info: 
	 * 	http://www.microsoft.com/downloads/details.aspx?FamilyID=6D483869-816A-44CB-9787-A866235EFC7C&displaylang=en 
	 */
	public static final String		DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	/** Default Port            	*/
	public static final int 		DEFAULT_PORT = 1433;
	
	/** Connection String       	*/
	private String          		m_connectionURL;
    /** Cached User Name			*/
    private String					m_userName = null;

	/** Statement Cache	(50)		*/
	//private static final int		MAX_STATEMENTS = 50;
	/** Data Source					*/
	private volatile SQLServerDataSource		m_ds = null;

	/** Statement Converter     */
	private Convert         m_convert = new Convert(Environment.DBTYPE_MS);
	
	private Connection 				m_conn = null;

    /**	Logger			*/
	private static CLogger			log	= CLogger.getCLogger (DB_SQLServer.class);
    
	/**
	 *  Get Database Type Name
	 *  @return database short name
	 */
	@Override
	public String getName()
	{
		return Environment.DBTYPE_MS;
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
		return DEFAULT_PORT;
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
			s_driver = new SQLServerDriver();
			DriverManager.registerDriver (s_driver);
			DriverManager.setLoginTimeout (Database.CONNECTION_TIMEOUT);
		}
		return s_driver;
	}   //  getDriver

	/**
	 *  Get Database Connection String.
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	@Override
	public String getConnectionURL (CConnection connection)
	{
		StringBuffer sb = new StringBuffer ("jdbc:sqlserver:");
		sb.append("//")
			.append(connection.getDbHost())
			.append(":").append(connection.getDbPort())
			.append(";databaseName=").append(connection.getDbName()).append(";");
		m_connectionURL = sb.toString();
		log.config(m_connectionURL);
		//
		m_userName = connection.getDbUid();
		return m_connectionURL;
	}   //  getConnectionURL

	/**
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
		m_connectionURL = "jdbc:sqlserver://" 
			+ dbHost + ":" + dbPort + ";databaseName=" + dbName + ";";
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
			//return "DBO";

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
		StringBuffer sb = new StringBuffer("DB_SQLServer[");
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
	 *  Convert an individual Oracle Style statements to target database statement syntax.
	 *  @param oraStatement oracle statement
	 *  @return converted Statement 
	 */
	@Override
	public String convertStatement (String oraStatement)
	{
		String convertedStatement = new String(oraStatement).trim();
		int i, j, k;
		String tmpString = "";
		
		//test for using TRUNC(date, fmt) or TRUNC(date)
		j = 0;
		while (j > -1) 
		{
			i = convertedStatement.indexOf("TRUNC", j);
			boolean notDo = convertedStatement.startsWith("2_DATE", i+5) || convertedStatement.startsWith("_DATE", i+5);
			//log.warning("debug-miki: i = " + i);
			if (i < 0 )
				break;
			
			int left = 0;
			for (k = i; k < convertedStatement.length(); k++) 
			{
				if (notDo)
					break;
				
				if(convertedStatement.charAt(k) == '(') //TRUNC(date)
				{
					left++;
					continue;
				}
				
				if(convertedStatement.charAt(k) == ',' && left == 1) //TRUNC(date, fmt)
				{
					tmpString = convertedStatement.substring(0, i);
					//log.warning("debug-miki: tmpString (1) = " + tmpString);
					tmpString = tmpString + "TRUNC2_DATE" + convertedStatement.substring(i+5);
					//log.warning("debug-miki: tmpString (2) = " + tmpString);
					convertedStatement = tmpString;
					break;
				}
				if(convertedStatement.charAt(k) == ')') //TRUNC(date)
				{
					left --;
					if (left > 0)
						continue;
					tmpString = convertedStatement.substring(0, i);
					//log.warning("debug-miki: tmpString (3) = " + tmpString);
					tmpString = tmpString + "TRUNC_DATE" + convertedStatement.substring(i+5);
					//log.warning("debug-miki: tmpString (4) = " + tmpString);
					convertedStatement = tmpString;
					break;
				}
				
			}
			j = i + 6;
		}

		//jz default expre changed to constraint tableName_columnName default expr
		/*
		if ((convertedStatement.startsWith("CREATE TABLE") || convertedStatement.startsWith("ALTER TABLE")) && convertedStatement.indexOf(" DEFAULT ")>0) 			
		{
			String tokens[] = convertedStatement.split(" ");
			String tableName = tokens[2].toUpperCase();
			for (i=0; i<tokens.length; i++)
			{
				if ("DEFAULT".equals(tokens[i]))
				{
					j = i - 2;
					if ("NULL".equals(tokens[i-1]))
						j--;
					if ("NOT".equals(tokens[i-2]))
						j--;
					if (tokens[j].startsWith("("))
						tokens[j] = tokens[j].substring(1).toUpperCase();
					convertedStatement = convertedStatement.replace("DEFAULT", "CONSTRAINT " + tableName + "_" + tokens[j] + " DEFAULT");
				}
			}
		}
		*/
		
		if (convertedStatement.startsWith("ALTER TABLE") && convertedStatement.indexOf(" MODIFY ")>0)
		{
			String tokens[] = convertedStatement.split(" ");
			String sql = "ALTER TABLE " + tokens[2];
			int idef = convertedStatement.indexOf(" DEFAULT ");
			if (idef<0)
			{
				sql += " ALTER COLUMN " + tokens[4];
				i = sql.length() - 6; //alter v.s. modify, + column: +1-7
				if (idef > 0)
				{
					sql += convertedStatement.substring(i, idef+1); //type stuff
					sql += ", ALTER " + tokens[4] + " SET DEFAULT " + convertedStatement.substring(idef + 9, convertedStatement.length());
				}
				else
					sql += " TYPE " + convertedStatement.substring(i, convertedStatement.length());
				convertedStatement = sql;				
			}
			else
			{
				if (convertedStatement.indexOf(" NOT NULL")>0)
				{
					sql += " SET NOT NULL";
					return sql;
				}
				else if (convertedStatement.indexOf(" NULL")>0)
				{
					sql += " DROP NOT NULL";
					return sql;				
				}
			}
		}
		
		if (convertedStatement.startsWith("CREATE TABLE") || convertedStatement.startsWith("ALTER TABLE")) 			
		{
			while (convertedStatement.indexOf("NUMBER(10,0)")>-1)
				convertedStatement = convertedStatement.replace("NUMBER(10,0)", "INTEGER");
			while (convertedStatement.indexOf("NUMBER(10)")>-1)
				convertedStatement = convertedStatement.replace("NUMBER(10)", "INTEGER");
			/*
			while (convertedStatement.indexOf(" DATE ")>-1)
				convertedStatement = convertedStatement.replace(" DATE ", " DATETIME ");
			while (convertedStatement.indexOf(" DATE,")>-1)
				convertedStatement = convertedStatement.replace(" DATE,", " DATETIME,");
			while (convertedStatement.indexOf(" DATE)")>-1)
				convertedStatement = convertedStatement.replace(" DATE)", " DATETIME)");
			while (convertedStatement.indexOf(" NVARCHAR2")>-1)
				convertedStatement = convertedStatement.replace(" NVARCHAR2", " NVARCHAR");
				*/
		}
		
		//CREATE UNIQUE INDEX AD_User_EMail ON AD_User (AD_Client_ID,COALESCE(UPPER(EMail),TO_NCHAR(AD_User_ID)))
		if (convertedStatement.startsWith("CREATE UNIQUE INDEX ")) 			
		{
			if (convertedStatement.indexOf("UPPER(COALESCE(EMail, Value))")>-1)
			//	if (convertedStatement.indexOf("COALESCE(UPPER(EMail),TO_NCHAR(AD_User_ID))")>-1)
			{
				convertedStatement = convertedStatement.replace("UPPER(COALESCE(EMail, Value))", "EMail");
				convertedStatement = convertedStatement.replace(" UNIQUE INDEX ", " INDEX ");
			}
			if (convertedStatement.indexOf("UPPER(ColumnName)")>-1)
				convertedStatement = convertedStatement.replace("UPPER(ColumnName)", "ColumnName");
			if (convertedStatement.indexOf(",UserElement1_ID,UserElement2_ID")>-1) //temp
				convertedStatement = convertedStatement.replace(",UserElement1_ID,UserElement2_ID", "");
			if (convertedStatement.indexOf("CREATE UNIQUE INDEX M_Product_ExpenseType")>-1) //temp
				convertedStatement = convertedStatement.replace(" UNIQUE ", " ");
			if (convertedStatement.indexOf("CREATE UNIQUE INDEX M_Product_Resource")>-1) //temp
				convertedStatement = convertedStatement.replace(" UNIQUE ", " ");
		}
		
		//('PK' || AD_Table_ID)
		if (convertedStatement.indexOf("('PK' || AD_Table_ID)")>0
				|| convertedStatement.indexOf("('PK' + AD_Table_ID)")>0) 			
		{
			convertedStatement = convertedStatement.replace(" AD_Table_ID)", " ltrim(str(AD_Table_ID)))");
		}
		//('FK' || AD_Table_ID || '_' || AD_Column_ID)
		if (convertedStatement.indexOf("('FK' || AD_Table_ID || '_' || AD_Column_ID)")>0) 			
		{
			convertedStatement = convertedStatement.replace("('FK' || AD_Table_ID || '_' || AD_Column_ID)", 
					"('FK' + ltrim(str(AD_Table_ID)) + '_' + ltrim(str(AD_Column_ID)))");
		}
		else 		
		if (convertedStatement.indexOf("('FK' + AD_Table_ID + '_' + AD_Column_ID)")>0) 			
		{
			convertedStatement = convertedStatement.replace("('FK' + AD_Table_ID + '_' + AD_Column_ID)", 
					"('FK' + ltrim(str(AD_Table_ID)) + '_' + ltrim(str(AD_Column_ID)))");
		}

		
		if (!convertedStatement.startsWith("INSERT INTO AD_Issue"))
			convertedStatement = DBUtils.whereSelectList(convertedStatement); //jz check equivalence
		
		if (convertedStatement.startsWith("UPDATE "))
		{
			String[] tks = convertedStatement.split(" ");
			if (tks.length>4 && tks[3].trim().equalsIgnoreCase("SET"))
			{
				int iwh = convertedStatement.indexOf(" WHERE ");
				int iset = convertedStatement.indexOf(" SET ");
				if (iset>-1)
				{
					int isubQ = convertedStatement.indexOf("(SELECT ", iset);
					int lsql = convertedStatement.length();
					int ip = 0;
					while (isubQ>0)
					{
						if (isubQ>-1 && isubQ<iwh)
						{
							int il = 1;
							int ir = 0;
							ip = isubQ+7;
							while (il>ir && ++ip<lsql)
							{
								if (convertedStatement.charAt(ip)=='(')
									il++;
								else 
									if (convertedStatement.charAt(ip)==')')
										ir++;
							}
							if (ip>iwh)
								iwh = convertedStatement.indexOf("WHERE ", ip);
						}
						else
							break;
						isubQ = convertedStatement.indexOf("(SELECT ", ip);
					}
					if (iwh>-1)
					{
						convertedStatement = "UPDATE " + tks[2] + convertedStatement.substring(iset, iwh) + " FROM " + 
												tks[1] + " " + tks[2] + " " + convertedStatement.substring(iwh, lsql);
					}
					else
						convertedStatement = "UPDATE " + tks[2] + convertedStatement.substring(iset, lsql) + " FROM " + 
						tks[1] + " " + tks[2];
				}//iset
			}//co-rel_ID
			convertedStatement = DBUtils.updateSetSelectList(convertedStatement);
			
		}//update

		if (convertedStatement.startsWith("DELETE FROM "))
		{
			String[] tks = convertedStatement.split(" ");
			if (tks.length>5 && tks[4].trim().equalsIgnoreCase("WHERE"))
			{
				int iwh = convertedStatement.indexOf(" WHERE ");
				if (iwh>-1)
				{
					int lsql = convertedStatement.length();
					convertedStatement = "DELETE " + tks[3] + " FROM " + 
											tks[2] + " " + tks[3] + convertedStatement.substring(iwh, lsql);
				}
			}
		}

		while (convertedStatement.indexOf("||")>-1)
			convertedStatement = convertedStatement.replace("||", "+");  //string concatenation

		String retValue[] = null;
		if (!(convertedStatement.startsWith("CREATE FUNCTION") || convertedStatement.startsWith("CREATE TRIGGER")))
			retValue = m_convert.convert(convertedStatement);

		if (retValue  != null && retValue.length == 1)
			convertedStatement = retValue[0];
		
		if (convertedStatement.indexOf("[[LineNo]]")>-1)
			convertedStatement = convertedStatement.replace("[[LineNo]]", "[LineNo]");  //some converted in value

		String curr_user = "[" + m_userName + "]";
		if (m_userName!=null && !m_userName.equals("compiere"))
		{
			while (convertedStatement.indexOf("[compiere]")>-1)
				convertedStatement = convertedStatement.replace("[compiere]", curr_user);
		}
		
		//String doubleCU = "[" + m_userName + "].[" + m_userName + "].";
		String doubleCU = curr_user + "." + curr_user;
		while (convertedStatement.indexOf(doubleCU)>-1)
			convertedStatement = convertedStatement.replace(doubleCU, curr_user);
		
		if (!(convertedStatement.startsWith("CREATE FUNCTION") || convertedStatement.startsWith("CREATE TRIGGER")))
		{
			if (retValue == null)
			{
				log.severe("Not Converted (" + convertedStatement + ") - "
						+ m_convert.getConversionError());
				return convertedStatement;
			}
			if (retValue.length != 1)
			{
				log.warning("Convert Command Number=" + retValue.length
						+ " (" + convertedStatement + ") - " + m_convert.getConversionError());
				return convertedStatement;
			}
		}
		//  Diagnostics (show changed, but not if AD_Error
		if (!convertedStatement.equals(oraStatement) && convertedStatement.indexOf("AD_Error") == -1)
			log.finest("=>" + convertedStatement + "<= [" + oraStatement + "]");
		
		//check if we support the sql, if not, return ""
		if (!isSupported(convertedStatement))
		{
			log.warning("MS SQL Server doesn't support this sql: " + oraStatement);
			return "";
		}
		return convertedStatement;

	}   //  convertStatement

	

	/**
	 *  Check if DBMS support the sql statement
	 *  @sql SQL statement
	 *  @return true: yes
	 */
	@Override
	public boolean isSupported(String sql)
	{
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
		/*if (IXName == null || IXName.length()==0)
			return "0";
		if (IXName.endsWith("_KEY"))
			return "1"+IXName;
		else*/
			return "0";
		//jz temp, modify later from user.constraints
	}

	/**
	 *  Get Name of System User
	 *  @return system
	 */
	@Override
	public String getSystemUser()
	{
		return "sa";
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
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	@Override
	public String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "CAST(STR(YEAR(Getdate()))+'-'+STR(Month(Getdate()))+'-'+STR(Day(Getdate())) AS DATETIME)";
			return "getdate()";
		}

		StringBuffer dateString = new StringBuffer("CAST('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("' AS DATETIME)");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("' AS DATETIME)");
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
	 *  @return string of conversion expression
	 *  @see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *   */
	@Override
	public String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (FieldType.isText(displayType) || FieldType.isTextArea(displayType))
			return columnName;
		
		StringBuffer retValue = new StringBuffer("LTRIM(");
		boolean moreRP = false;
		//  Numbers
		if (FieldType.isNumeric(displayType)||columnName.endsWith("_ID"))
		{
			retValue.append("STR(");
			moreRP = true;
		}
		else if (FieldType.isDate(displayType))
		{
			retValue.append("CONVERT(VARCHAR,");
			moreRP = true;
		}
		retValue.append(columnName + ")");
		if (moreRP)
			retValue.append(")");
		//
		return retValue.toString();
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
			try {
				SQLServerDataSource ds = new SQLServerDataSource();
				ds.setServerName(connection.getDbHost());
				ds.setDatabaseName(connection.getDbName());
				ds.setPortNumber(connection.getDbPort());
				ds.setUser(connection.getDbUid());
				ds.setPassword(connection.getDbPwd());
				ds.setDescription("Compiere MSSql Data Source");
				ds.setApplicationName("Compiere");
				ds.setIntegratedSecurity(false);

				log.config(toString());
				//
				m_ds = ds;
			} catch (Exception e) {
				log.log(Level.SEVERE, toString(), e);
			}
		}
		return m_ds;
	} // getDataSource


	/**
	 * 	Get Cached Connection
	 *	@param connection info
	 *  @param autoCommit true if autocommit connection
	 *  @param transactionIsolation Connection transaction level
	 *	@return connection or null
	 *	@throws Exception
	 */
	@Override
	public Connection getCachedConnection (CConnection connection, 
		boolean autoCommit, int transactionIsolation)
		throws SQLException
	{
		SQLServerConnection conn = (SQLServerConnection) getDataSource(connection).getConnection();
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
	 * 	Get Connection from Driver
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
	 * 	Get Driver Connection
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
		return DriverManager.getConnection (dbUrl, dbUid, dbPwd);
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

	/**************************************************************************
	 * 	Handle Abandoned Connection
	 *	@param conn connection
	 *	@param userObject 
	 *	@return true if close - false for keeping it
	 */
	public boolean handleAbandonedConnection (SQLServerConnection conn, Object userObject)
	{
		System.out.println("--------------------handleAbandonedConnection " + conn + " - " + userObject);
		return true;	//	reclaim it
	}	//	handleAbandonedConnection

	/**
	 * 	Release Connection
	 *	@param conn connection
	 *	@param userObject 
	 */
	public void releaseConnection (SQLServerConnection conn, Object userObject)
	{
		System.out.println("----------------------releaseConnection " + conn + " - " + userObject);
	}	//	releaseConnection

	
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
				retValue = "NUERIC(10)";
				break;
				
			// Dynamic Precision
			case DisplayTypeConstants.Amount:
				retValue = "NUMERIC(32,6)";
				if (defaultValue)
					retValue += " DEFAULT 0";
				break;
				
			case DisplayTypeConstants.Binary:
				retValue = "VARBINARY(MAX)";
				break;
				
			case DisplayTypeConstants.Button:
				retValue = "CHAR(1)";
				break;
				
			// Number Dynamic Precision
			case DisplayTypeConstants.CostPrice:
				retValue = "NUMERIC(32,6)";
				if (defaultValue)
					retValue += " DEFAULT 0";
				break;
				
			//	Date	
			case DisplayTypeConstants.Date:
			case DisplayTypeConstants.DateTime:
			case DisplayTypeConstants.Time:
				retValue = "DATETIME";
				if (defaultValue)
					retValue += " DEFAULT CURRENT_TIMESTAMP";
				break;
				
			// 	Number(10)
			case DisplayTypeConstants.Integer:
				retValue = "NUMERIC(10)";
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
				retValue = "VARBINARY(MAX)";
				break;

			//	Dyn Prec
			case DisplayTypeConstants.Quantity:
				retValue = "NUMERIC(32,6)";
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


	
	/**************************************************************************
	 * 	Testing
	 * 	@param args ignored
	 * @throws SQLException 
	 */
	public static void main (String[] args) throws SQLException
	{
		Compiere.startup(true);
		CConnection cc = CConnection.get();
		new CConnectionDialog(cc);
		System.out.println ("Connection >> " + cc.toStringLong ());
		DB_SQLServer db = (DB_SQLServer)cc.getDatabase();
		System.out.println ("db >> " + db.toString());

		db.cleanup();
		
		try
		{
			Connection conn = null;
		//	System.out.println("Driver=" + db.getDriverConnection(cc));
			DataSource ds = db.getDataSource(cc);
			System.out.println("DS=" + ds.getConnection());
			conn = db.getCachedConnection(cc, true, Connection.TRANSACTION_READ_COMMITTED);
			System.out.println("Cached=" + conn);
			System.out.println(db);
			//////////////////////////
			System.out.println("JAVA classpath: [" +
				System.getProperty("java.class.path") + "]");
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
				System.out.println("ProductVersion: ["+
				dmd.getDatabaseProductVersion()+"]"); 
			//////////////////////////
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		db.cleanup();
		
		System.out.println("--------------------------------------------------");
		/**
		DROP TABLE X_Test;
		CREATE TABLE X_Test
		(
		    Text1   NVARCHAR2(2000) NULL,
		    Text2   VARCHAR2(2000)  NULL
		);
		**/
		try
		{
			String myString1 = "123456789 12345678";
			String myString = "";
			for (int i = 0; i < 99; i++)
				myString += myString1 + (char)('a'+i) + "\n";
			System.out.println("myString.length() = " + myString.length());
			System.out.println("Util.size(myString) = " + Util.size(myString));
			//
			myString = Util.trimSize(myString, 2000);
			System.out.println("myString.length() = " + myString.length());
			System.out.println("Util.size(myString) = " + Util.size(myString));
			//
			Connection conn2 = db.getCachedConnection(cc, true, Connection.TRANSACTION_READ_COMMITTED);
			System.out.println("conn2 >> " + conn2.toString());
			/** **/
			PreparedStatement pstmt = conn2.prepareStatement
				("INSERT INTO X_Test(Text1, Text2) values(?,?)");
			pstmt.setString(1, myString); // NVARCHAR2 column
			pstmt.setString(2, myString); // VARCHAR2 column
			System.out.println("pstmt.executeUpdate() = " + pstmt.executeUpdate());
			/** **/
			Statement stmt = conn2.createStatement();
			System.out.println("stmt.executeUpdate(...) >> " + stmt.executeUpdate("INSERT INTO X_Test(Text1, Text2) values('" + myString + "','" + myString + "')"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		db.cleanup();
		System.out.println("--------------------------------------------------");
		//System.exit(0);
		
		
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
		
	}	//	main


	@Override
	public String nullValue(String sqlClause, int dataType) {
		return "NULL";
	}

	@Override
	public String updateSetSelectList(String sql) {
		return DBUtils.updateSetSelectList(sql);
	}

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
			return null;
	}


}   //  DB_SQLServer
