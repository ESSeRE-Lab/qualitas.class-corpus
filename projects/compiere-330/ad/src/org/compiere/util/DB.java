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
package org.compiere.util;

import java.io.*;
import java.math.*;
import java.rmi.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import javax.sql.*;
import javax.swing.*;

import org.compiere.*;
import org.compiere.db.*;
import org.compiere.interfaces.*;
import org.compiere.model.*;


/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.8 2006/10/09 00:22:29 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/**	Logger							*/
	private static final CLogger			log = CLogger.getCLogger (DB.class);

	/** SQL Statement Separator "; "	*/
	public static final String SQLSTATEMENT_SEPARATOR = "; ";


	/**
	 * 	Update Mail Settings for System Client and System User
	 */
	public static void updateMail()
	{
		//	Get Property File
		String envName = Ini.getCompiereHome();
		if (envName == null)
			return;
		envName += File.separator + "CompiereEnv.properties";
		File envFile = new File(envName);
		if (!envFile.exists())
			return;

		Properties env = new Properties();
		try
		{
			FileInputStream in = new FileInputStream(envFile);
			env.load(in);
			in.close();
		}
		catch (Exception e)
		{
			return;
		}
		String updated = env.getProperty("COMPIERE_MAIL_UPDATED");
		if ((updated != null) && updated.equals("Y"))
			return;

		//	See org.compiere.install.ConfigurationData
		String server = env.getProperty("COMPIERE_MAIL_SERVER");
		if ((server == null) || (server.length() == 0))
			return;
		String adminEMail = env.getProperty("COMPIERE_ADMIN_EMAIL");
		if ((adminEMail == null) || (adminEMail.length() == 0))
			return;
		String mailUser = env.getProperty("COMPIERE_MAIL_USER");
		if ((mailUser == null) || (mailUser.length() == 0))
			return;
		String mailPassword = env.getProperty("COMPIERE_MAIL_PASSWORD");
	//	if (mailPassword == null || mailPassword.length() == 0)
	//		return;
		//
		StringBuffer sql = new StringBuffer("UPDATE AD_Client SET")
			.append(" SMTPHost=").append(DB.TO_STRING(server))
			.append(", RequestEMail=").append(DB.TO_STRING(adminEMail))
			.append(", RequestUser=").append(DB.TO_STRING(mailUser))
			.append(", RequestUserPW=").append(DB.TO_STRING(mailPassword))
			.append(", IsSMTPAuthorization='Y' WHERE AD_Client_ID=0");
		int no = DB.executeUpdate(sql.toString(), (Trx) null);
		//
		sql = new StringBuffer("UPDATE AD_User SET ")
			.append(" EMail=").append(DB.TO_STRING(adminEMail))
			.append(", EMailUser=").append(DB.TO_STRING(mailUser))
			.append(", EMailUserPW=").append(DB.TO_STRING(mailUser))
			.append(" WHERE AD_User_ID IN (0,100)");
		no = DB.executeUpdate(sql.toString(), (Trx) null);
		if (no < 0)
			log.warning("User Update Error");
		//
		try
		{
			env.setProperty("COMPIERE_MAIL_UPDATED", "Y");
			FileOutputStream out = new FileOutputStream(envFile);
			env.store(out, "");
			out.flush();
			out.close();
		}
		catch (Exception e)
		{
		}

	}	//	updateMail

	/**************************************************************************
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("Connection is NULL");

		if ((s_cc != null) && s_cc.equals(cc))
			return;

		DB.closeTarget();
		//
		if (s_cc == null)
			s_cc = cc;
		s_cc.setDataSource();
		
		log.config(s_cc + " - DS=" + s_cc.isDataSource());
	//	Trace.printStack();
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		boolean success = false;
		CLogErrorBuffer eb = CLogErrorBuffer.get(false);
		if ((eb != null) && eb.isIssueError())
			eb.setIssueError(false);
		else
			eb = null;	//	don't reset
		try
		{
			Trx trx = Trx.get("isConnected");
			success = trx.getConnection() != null;
			trx.close();
		}
		catch (Exception e)
		{
			success = false;
		}
		if (eb != null)
			eb.setIssueError(true);
		return success;
	}   //  isConnected


	/**
	 * Create new Connection. The connection must be closed explicitly by the
	 * application
	 * 
	 * @param autoCommit
	 *            auto commit
	 * @param trxLevel -
	 *            Connection.TRANSACTION_READ_UNCOMMITTED,
	 *            Connection.TRANSACTION_READ_COMMITTED,
	 *            Connection.TRANSACTION_REPEATABLE_READ, or
	 *            Connection.TRANSACTION_READ_COMMITTED.
	 * @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		Connection conn = s_cc.createConnection (autoCommit, trxLevel);
		if (CLogMgt.isLevelFinest())
		{
			/**
			try
			{
				log.finest(s_cc.getConnectionURL()
					+ ", UserID=" + s_cc.getDbUid()
					+ ", AutoCommit=" + conn.getAutoCommit() + " (" + autoCommit + ")"
					+ ", TrxIso=" + conn.getTransactionIsolation() + "( " + trxLevel + ")");
			}
			catch (Exception e)
			{
			}
			**/
		}
		return conn;
	}	//	createConnection


	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		log.severe("No Database Connection");
		return null;
	}   //  getDatabase

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @param URL JDBC connection url
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase(String URL)
	{
		if (URL == null)
		{
			log.severe("No Database URL");
			return null;
		}
		if (URL.indexOf("oracle") != -1)
			return new DB_Oracle();
		if (URL.indexOf("db2") != -1)
			return new DB_DB2();
		if (URL.indexOf("edb") != -1)
			return new DB_PostgreSQL();
		log.severe("No Database for " + URL);
		return null;
	}   //  getDatabase

	/**
	 * 	Do we have an Oracle DB ?
	 *	@return true if connected to Oracle
	 */
	public static boolean isOracle()
	{
		if (s_cc != null)
			return s_cc.isOracle();
		log.warning("No Database Connection");
		return false;
	}	//	isOracle

	/**
	 * 	Do we have Oracle XE ?
	 *	@return true if connected to Oracle XE
	 */
	public static boolean isOracleXE()
	{
		if (s_cc != null)
			return s_cc.isOracleXE();
		log.warning("No Database Connection");
		return false;
	}	//	isOracleXE

	/**
	 * 	Do we have a DB2 DB ?
	 *	@return true if connected to DB2
	 */
	public static boolean isDB2()
	{
		if (s_cc != null)
			return s_cc.isDB2();
		log.warning("No Database Connection");
		return false;
	}	//	isDB2

	/**
	 * 	Do we have a PostgreSQL DB ?
	 *	@return true if connected to PostgreSQL
	 */
	public static boolean isPostgreSQL()
	{
		if (s_cc != null)
			return s_cc.isPostgreSQL();
		log.warning("No Database Connection");
		return false;
	}	//	isDB2

 	/**
		 * 	Do we have a MS SQL Server ?
		 *	@return true if connected to MS SQL
		 */
	public static boolean isMSSQLServer()
	{
		if (s_cc != null)
			return s_cc.isMSSQLServer();
		log.warning("No Database Connection");
		return false;
	}	//	isMSSQLServer


	/**
	 * 	Get Database Info
	 *	@return info
	 */
	public static String getDatabaseInfo()
	{
		if (s_cc != null)
			return s_cc.getDBInfo();
		return "No Database";
	}	//	getDatabaseInfo


	/**************************************************************************
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Ctx ctx)
	{
		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql, (Trx) null);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, "Problem with AD_System Table - Run system.sql script - " + e.toString());
			return false;
		}
		log.info("DB_Version=" + version);
		//  Identical DB version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			JOptionPane.showMessageDialog (null,
				"For details about running migration\nsee: http://www.compiere.com/support/software-migration.php",
				title, JOptionPane.INFORMATION_MESSAGE);
			Env.exitEnv(1);
		}
		return false;
	}   //  isDatabaseOK


	/**************************************************************************
	 *	Close Target Connections
	 */
	public static void closeTarget()
	{
		//	CConnection
		if (s_cc != null)
		{
			dumpBorrowers();
			s_cc.setDataSource(null);
			log.fine("closed");
		}
		s_cc = null;
	}	//	closeTarget


	/**
	 *	Execute Forward RW Call
	 * @param RO_SQL
	 * @param AD_PInstance_ID
	 * @throws SQLException
	 */
	public static void executeCall(String RO_SQL, int AD_PInstance_ID) throws SQLException {
		String sql = getDatabase().convertStatement(RO_SQL);
		Trx trx = Trx.getAutoCommitTrx();
		CallableStatement cstmt = trx.getConnection().prepareCall(sql); // ro??
		try {
			cstmt.setInt(1, AD_PInstance_ID);
			cstmt.executeUpdate();
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				cstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			trx.close();
		}
	}
	

	/**************************************************************************
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 *  @deprecated
	 */
	@Deprecated
	public static CPreparedStatement prepareStatement (String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
	}	//	prepareStatement

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 * 	@param trx transaction
	 *  @return Prepared Statement
	 */
	public static CPreparedStatement prepareStatement (String RO_SQL, Trx trx)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, trx);
	}	//	prepareStatement


	
	/**
	 *	Prepare Statement.
	 *  @param sql sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 *  @deprecated
	 */
	@Deprecated
	public static CPreparedStatement prepareStatement (String sql,
		int resultSetType, int resultSetConcurrency)
	{
		return prepareStatement(sql, resultSetType, resultSetConcurrency, null);
	}	//	prepareStatement

	/**
	 *	Prepare Statement.
	 *  @param sql sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 * 	@param trx transaction name
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static CPreparedStatement prepareStatement(String sql,
		int resultSetType, int resultSetConcurrency, Trx trx)
	{
		if ((sql == null) || (sql.length() == 0))
			throw new IllegalArgumentException("No SQL");
		//
		return new CPreparedStatement(resultSetType, resultSetConcurrency, sql, trx);
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	@Deprecated
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
	}	//	createStatement

	/**
	 *	Create Statement.
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 * 	@param trx transaction name
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	@Deprecated
	public static Statement createStatement(int resultSetType, int resultSetConcurrency, Trx trx)
	{
		return new CStatement(resultSetType, resultSetConcurrency, trx);
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param trx optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, Trx trx)
	{
		return executeUpdate(sql, (Object[])null, false, trx);
	}	//	executeUpdate


	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param ignoreError if true, no execution error is reported
	 * 	@param trx transaction
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, boolean ignoreError, Trx trx)
	{
		return executeUpdate (sql, (Object[])null, ignoreError, trx);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 *  @param param int param
	 * 	@param trx transaction
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, int param, Trx trx)
	{
		return executeUpdate (sql, new Object[]{Integer.valueOf(param)}, false, trx);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 *  @param param int parameter
	 * 	@param ignoreError if true, no execution error is reported
	 * 	@param trx transaction
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, int param, boolean ignoreError, Trx trx)
	{
		return executeUpdate (sql, new Object[]{Integer.valueOf(param)}, ignoreError, trx);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 *  @param params list of parameters
	 * 	@param ignoreError if true, no execution error is reported
	 * 	@param trx optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, ArrayList<Object> params, boolean ignoreError, Trx trx)
	{
		if (params == null)
			return executeUpdate(sql,(Object[])null, ignoreError, trx);
		return executeUpdate(sql,params.toArray(new Object[params.size()]), ignoreError, trx);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 *  @param params array of parameters
	 * 	@param ignoreError if true, no execution error is reported
	 * 	@param trx optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, Object[] params, boolean ignoreError, Trx trx)
	{
		if ((sql == null) || (sql.length() == 0))
			throw new IllegalArgumentException("Required parameter missing - " + sql);
		//
		int no = -1;
		CPreparedStatement cs = new CPreparedStatement(ResultSet.TYPE_FORWARD_ONLY,
			ResultSet.CONCUR_UPDATABLE, sql, trx);	//	converted in call

		try
		{
			//	Set Parameter
			if (params != null)
			{
				for (int i = 0; i < params.length; i++)
				{
					Object param = params[i];
					if (param instanceof String)
						cs.setString(i+1, (String)param);
					else if (param instanceof Integer)
						cs.setInt(i+1, ((Integer)param).intValue());
					else if (param instanceof BigDecimal)
						cs.setBigDecimal(i+1, (BigDecimal)param);
					else if (param instanceof Timestamp)
						cs.setTimestamp(i+1, (Timestamp)param);
					else if (param instanceof NullParameter)
						cs.setNull(i + 1, ((NullParameter) param).getType());
				}
			}
			//
			no = cs.executeUpdate();
			if (DB.isMSSQLServer() && (no == -1))
				no = 0;  //
			//	No Transaction - Commit
			if (trx == null)
			{
				cs.commit();	//	Local commit
			//	Connection conn = cs.getConnection();
			//	if (conn != null && !conn.getAutoCommit())	//	is null for remote
			//		conn.commit();
			}
		}
		catch (SQLException e)
		{
			if (e.getErrorCode() == 1)		//	Unique Constraint
			{
				log.log(Level.WARNING, "Not Unique: " + e.getLocalizedMessage());
				log.saveError("SaveErrorNotUnique", e);
			}
			else if(isMSSQLServer() && sql.startsWith("ALTER TABLE ") && (e.getMessage().indexOf("may cause cycles or multiple")>0))
			{
				//convert FK to trigger
				log.log(Level.INFO, e.getMessage());
				log.log(Level.INFO, "Try to generate an alternative SQL and execute it.");
				try
				{
					no = executeTrigger(cs.getSql(), trx);
				}
				catch (SQLException ee)
				{
					log.log(Level.SEVERE, "Fail to execute alternative SQL for " + cs.getSql() + " [" + trx + "]", ee);
				}
			}
			else if (ignoreError)
				log.log(Level.WARNING, cs.getSql() + " [" + trx + "] - " +  e.getMessage());
			else
			{
				log.log(Level.SEVERE, cs.getSql() + " [" + trx + "]", e);
				log.saveError ("DBExecuteError", e);
			}
		//	throw new DBException(e);
		}
		finally
		{
			//  Always close cursor
			try
			{
				cs.close();
			}
			catch (SQLException e2)
			{
				log.log(Level.SEVERE, "Cannot close statement");
			}
		}
		return no;
	}	//	executeUpdate


	/**************************************************************************
	 *
	 *  JZ
	 *
	 * 	Modify and execute Single Update on Target database.
	 *  @param sql sql
	 * 	@return -1: not success
	 */
	private static int executeTrigger (String sql, Trx trx) throws SQLException
	{
		int no = -1;
		String newSQL = null;

		newSQL = CConstraint.forTrigger(0, null, sql);

		if ((newSQL == null) || (newSQL.length() == 0))
		{
			log.log(Level.SEVERE, "Unable to generate alternative SQL.");
			return no;
		}

		//do a drop before create
		String[] st = newSQL.split(" ");
		if (st.length > 3)
		{
			String preSQL = "DROP " + st[1] + " " + st[2];

			no = executeUpdate(preSQL, true, trx);
			if (no > -1)
			{
				log.log(Level.INFO,"Successfully executed pre-create SQL: " + preSQL);
			}
		}

		no = executeUpdate (newSQL, false, trx);
		if (no>-1)
		{
			Trx localTrx = Trx.getAutoCommitTrx();
			int iret = CConstraint.save(localTrx.getConnection(), newSQL, null);
			if (iret > -1)
				log.log(Level.INFO,"Executed and logged for " + newSQL);
			else
			{
				log.log(Level.INFO,"Successfully executed " + newSQL);
				log.warning("      but unable to log for " + newSQL +
						localTrx==null? " - Connection is null.":" - " + localTrx.toString());
			}
			localTrx.close();

		}
		return no;
	}




	/**
	 *	Execute multiple Update statements.
	 *  saves (last) "DBExecuteError" in Log
	 *  @param sql multiple sql statements separated by "; " SQLSTATEMENT_SEPARATOR
	 * 	@param ignoreError if true, no execution error is reported
	 * 	@param trx optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdateMultiple (String sql, boolean ignoreError, Trx trx)
	{
		if ((sql == null) || (sql.length() == 0))
			throw new IllegalArgumentException("Required parameter missing - " + sql);
		int index = sql.indexOf(SQLSTATEMENT_SEPARATOR);
		if (index == -1)
			return executeUpdate(sql, (Object[])null, ignoreError, trx);
		int no = 0;
		//
		String statements[] = sql.split(SQLSTATEMENT_SEPARATOR);
		for (String element : statements)
		{
			log.fine(element);
			no += executeUpdate(element, (Object[])null, ignoreError, trx);
		}

		return no;
	}	//	executeUpdareMultiple

	/**
	 *	Execute Update and throw exception.
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 * 	@param trx transaction
	 * 	@throws SQLException
	 */
	public static int executeUpdateEx (String SQL, Trx trx) throws SQLException
	{
		if ((SQL == null) || (SQL.length() == 0))
			throw new IllegalArgumentException("Required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		SQLException ex = null;
		Connection conn = null;
		Statement stmt = null;
		Trx p_trx = trx;
		try
		{
			if (p_trx != null)
				conn = p_trx.getConnection();
			else{				
				conn = DB.getCachedConnection();
				conn.setAutoCommit(true);
			}
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql + " [" + trx + "]", e);
			ex = e;
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				log.log(Level.SEVERE, "Cannot close statement");
			}
			if( p_trx == null )
			{
				conn.close();
				DB.returnCachedConnection(conn);
			}
		}
		if (ex != null)
			throw new SQLException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		return no;
	}	//	execute Update

	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit (exception: with transaction)
	 *  @param throwException if true, re-throws exception
	 * 	@param trx transaction name
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	@Deprecated
	public static boolean commit(boolean throwException, Trx trx) throws SQLException {
		if (trx != null) {
			return trx.commit();
		} else {
			log.warning("Trx:" + trx + " is null, commit nop");
			return true;
		}
	} // commit

	/**
	 * Rollback - rollback on RW connection. Is has no effect as RW connection
	 * is AutoCommit (exception: with transaction)
	 * 
	 * @param throwException
	 *            if true, re-throws exception
	 * @param trx
	 *            transaction name
	 * @return true if not needed or success
	 * @throws SQLException
	 */
	@Deprecated
	public static boolean rollback(boolean throwException, Trx trx) throws SQLException {
		if (trx != null) {
			return trx.rollback();
		} else {
			log.warning("Trx:" + trx + " is null, rollback nop");
			return true;
		}
	} // rollback

	/**
	 * 	Get Row Set.
	 * 	When a Rowset is closed, it also closes the underlying connection.
	 * 	If the created RowSet is transfered by RMI, closing it makes no difference
	 *	@param sql sql
	 *	@param local local RowSet (own connection)
	 *	@return row set or null
	 */
	public static RowSet getRowSet (String sql, boolean local)
	{
		RowSet retValue = null;
		CStatementVO info = new CStatementVO (
			ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, sql);
		CPreparedStatement stmt = new CPreparedStatement(info);
		if (local)
		{
			retValue = stmt.local_getRowSet();
		}
		else
		{
			retValue = stmt.remote_getRowSet();
		}
		return retValue;
	}	//	getRowSet

	/**
	 * 	Get Value from sql
	 * 	@param trx p_trx
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (Trx trx, String sql, Object[] params)
	{
		if ((sql == null) || (sql.length() == 0))
			throw new IllegalArgumentException("Required parameter missing - " + sql);

		CPreparedStatement cs = new CPreparedStatement(ResultSet.TYPE_FORWARD_ONLY,
			ResultSet.CONCUR_READ_ONLY, sql, trx);	

		int retValue = -1;
		
		try
		{
			//	Set Parameter
			if (params != null)
			{
				for (int i = 0; i < params.length; i++)
				{
					Object param = params[i];
					if (param instanceof String)
						cs.setString(i+1, (String)param);
					else if (param instanceof Integer)
						cs.setInt(i+1, ((Integer)param).intValue());
					else if (param instanceof BigDecimal)
						cs.setBigDecimal(i+1, (BigDecimal)param);
					else if (param instanceof Timestamp)
						cs.setTimestamp(i+1, (Timestamp)param);
					else if (param instanceof NullParameter)
						cs.setNull(i + 1, ((NullParameter) param).getType());
				}
			}
			//
			ResultSet rs = cs.executeQuery();
			
			if (rs.next())
				retValue = rs.getInt(1);
			else
				log.fine("No Value " + sql);
			rs.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, cs.getSql() + " [" + trx + "]", e);
			log.saveError ("GetSQLValue error", e);
		}
		finally
		{
			//  Always close cursor
			try
			{
				cs.close();
			}
			catch (SQLException e2)
			{
				log.log(Level.SEVERE, "Cannot close statement");
			}
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param trx p_trx
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (Trx trx, String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql, trx);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				log.fine("No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param trx p_trx
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (Trx trx, String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql, trx);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				log.config("No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql + " - Param1=" + int_param1 + " [" + trx + "]", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param trx p_trx
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param int_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (Trx trx, String sql, int int_param1, int int_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql, trx);
			pstmt.setInt(1, int_param1);
			pstmt.setInt(2, int_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				log.info("No Value " + sql
					+ " - Param1=" + int_param1 + ",Param2=" + int_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql + " - Param1=" + int_param1 + ",Param2=" + int_param2
				+ " [" + trx + "]", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param trx p_trx
	 * 	@param sql sql
	 * 	@param str_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (Trx trx, String sql, String str_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql, trx);
			pstmt.setString(1, str_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				log.info("No Value " + sql + " - Param1=" + str_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql + " - Param1=" + str_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param trx p_trx
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (Trx trx, String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql, trx);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				log.info("No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param trx p_trx
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param int_param2 parameter 2
	 * 	@param s_param3 parameter 3
	 * 	@return first value or -1
	 */
	public static int getSQLValue (Trx trx, String sql,
		int int_param1, int int_param2, String s_param3)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql, trx);
			pstmt.setInt(1, int_param1);
			pstmt.setInt(2, int_param2);
			pstmt.setString(3, s_param3);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				log.info("No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" +int_param2
							+ ", Param3="+ s_param3);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql + " - Param1=" + int_param1 + ",Param2=" + int_param2+
								",Param3=" + s_param3, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get String Value from sql
	 * 	@param trx p_trx
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or null
	 */
	public static String getSQLValueString (Trx trx, String sql, int int_param1)
	{
		String retValue = null;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql, trx);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getString(1);
			else
				log.info("No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValueString

	/**
	 * 	Get BigDecimal Value from sql
	 * 	@param trx p_trx
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or null
	 */
	public static BigDecimal getSQLValueBD (Trx trx, String sql, int int_param1)
	{
		BigDecimal retValue = null;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql, trx);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getBigDecimal(1);
			else
				log.info("No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql + " - Param1=" + int_param1 + " [" + trx + "]", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValueBD


	/**
	 * 	Get Array of Key Name Pairs
	 *	@param sql select with id / name as first / second column
	 *	@param optional if true (-1,"") is added
	 *	@return array of key name pairs
	 */
	public static KeyNamePair[] getKeyNamePairs(String sql, boolean optional)
	{
		PreparedStatement pstmt = null;
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		if (optional)
			list.add (new KeyNamePair(-1, ""));
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
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
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
	//	s_log.fine("getKeyNamePairs #" + retValue.length);
		return retValue;
	}	//	getKeyNamePairs

	/**
	 * 	Is Sales Order Trx.
	 * 	Assumes Sales Order. Queries IsSOTrx of table with where clause
	 *	@param TableName table
	 *	@param whereClause where clause
	 *	@return true (default) or false if tested that not SO
	 */
	public static boolean isSOTrx (String TableName, String whereClause)
	{
		if ((TableName == null) || (TableName.length() == 0))
		{
			log.severe("No TableName");
			return true;
		}
		if ((whereClause == null) || (whereClause.length() == 0))
		{
			log.severe("No Where Clause");
			return true;
		}
		//
		boolean isSOTrx = true;
		String sql = "SELECT IsSOTrx FROM " + TableName
			+ " WHERE " + whereClause;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
				isSOTrx = "Y".equals(rs.getString(1));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			if (TableName.endsWith("Line"))
			{
				String hdr = TableName.substring(0, TableName.indexOf("Line"));
				sql = "SELECT IsSOTrx FROM " + hdr
					+ " h WHERE EXISTS (SELECT * FROM " + TableName
					+ " l WHERE h." + hdr + "_ID=l." + hdr + "_ID AND "
					+ whereClause + ")";
				PreparedStatement pstmt2 = null;
				try
				{
					pstmt2 = DB.prepareStatement(sql, (Trx) null);
					ResultSet rs2 = pstmt2.executeQuery ();
					if (rs2.next ())
						isSOTrx = "Y".equals(rs2.getString(1));
					rs2.close ();
					pstmt2.close ();
					pstmt2 = null;
				}
				catch (Exception ee)
				{
					log.finest(sql + " - " + e.getMessage());
				}
				try
				{
					if (pstmt2 != null)
						pstmt2.close ();
					pstmt2 = null;
				}
				catch (Exception ee)
				{
					pstmt2 = null;
				}
			}
			else
			{
				log.finest(TableName + " - No SOTrx");
			//	log.finest(sql + " - " + e.getMessage());
			}
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
		return isSOTrx;
	}	//	isSOTrx


	/**************************************************************************
	 *	Get next number for Key column = 0 is Error.
	 *   * @param ctx client
	@param TableName table name
	 * 	@param trx optionl transaction name
	 *  @return next no
	 */
	public static int getNextID (Ctx ctx, String TableName, Trx trx)
	{
		if (ctx == null)
			throw new IllegalArgumentException("Context missing");
		if ((TableName == null) || (TableName.length() == 0))
			throw new IllegalArgumentException("TableName missing");
		return getNextID(ctx.getAD_Client_ID(), TableName, trx);
	}	//	getNextID

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 * 	@param trx optional Transaction Name
	 *  @return next no
	 */
	public static int getNextID (int AD_Client_ID, String TableName, Trx trx)
	{
		if (((trx == null)) && isRemoteObjects())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{	//	See ServerBean
					int id = server.getNextID(AD_Client_ID, TableName, null);
					log.finest("server => " + id);
					if (id < 0)
						throw new DBException("No NextID");
					return id;
				}
				log.log(Level.SEVERE, "AppsServer not found - " + TableName);
			}
			catch (RemoteException ex)
			{
				log.log(Level.SEVERE, "AppsServer error", ex);
			}
			//	Try locally
		}
		//jz let trx = null so that the previous workflow node wouldn't block startnext  to invoke this method
		//   in the case trx rollback, it's OK to keep the pumpup sequence.
		//TODO
		if (isDB2())
			trx = null;	//	tries 3 times
		int id = MSequence.getNextID (AD_Client_ID, TableName);	//	tries 3 times
	//	if (id <= 0)
	//		throw new DBException("No NextID (" + id + ")");
		return id;
	}	//	getNextID

	/**
	 * 	Get Document No based on Document Type
	 *	@param C_DocType_ID document type
	 * 	@param trx optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo(int C_DocType_ID, Trx trx)
	{
		if (((trx == null)) && isRemoteObjects())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{	//	See ServerBean
					String dn = server.getDocumentNo (C_DocType_ID, null);
					log.finest("Server => " + dn);
					if (dn != null)
						return dn;
				}
				log.log(Level.SEVERE, "AppsServer not found - " + C_DocType_ID);
			}
			catch (RemoteException ex)
			{
				log.log(Level.SEVERE, "AppsServer error", ex);
			}
		}
		//	fallback
		if (DB.isDB2())  //jz //TODO
			trx = null;
		String dn = MSequence.getDocumentNo (C_DocType_ID, trx);
		if (dn == null)		//	try again
			dn = MSequence.getDocumentNo (C_DocType_ID, trx);
	//	if (dn == null)
	//		throw new DBException ("No DocumentNo");
		return dn;
	}	//	getDocumentNo


	/**
	 * 	Get Document No from table
	 *	@param AD_Client_ID client
	 *	@param TableName table name
	 * 	@param trx optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo (int AD_Client_ID, String TableName, Trx trx)
	{
		if (((trx == null)) && isRemoteObjects())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{	//	See ServerBean
					String dn = server.getDocumentNo (AD_Client_ID, TableName, null);
					log.finest("Server => " + dn);
					if (dn != null)
						return dn;
				}
				log.log(Level.SEVERE, "AppsServer not found - " + TableName);
			}
			catch (RemoteException ex)
			{
				log.log(Level.SEVERE, "AppsServer error", ex);
			}
		}
		//	fallback
		if (DB.isDB2())  //jz //TODO
			trx = null;
		String dn = MSequence.getDocumentNo (AD_Client_ID, TableName, trx);
		if (dn == null)		//	try again
			dn = MSequence.getDocumentNo (AD_Client_ID, TableName, trx);
		if (dn == null)
			throw new DBException ("No DocumentNo");
		return dn;
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 * 	@param trx optional Transaction Name
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Ctx ctx, int WindowNo,
		String TableName, boolean onlyDocType, Trx trx)
	{
		if ((ctx == null) || (TableName == null) || (TableName.length() == 0))
			throw new IllegalArgumentException("Required parameter missing");
		int AD_Client_ID = ctx.getContextAsInt( WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = ctx.getContextAsInt( WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = ctx.getContextAsInt( WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			log.fine("Window=" + WindowNo
				+ " - Target=" + ctx.getContextAsInt( WindowNo + "|C_DocTypeTarget_ID") + "/" + ctx.getContextAsInt( WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + ctx.getContextAsInt( WindowNo + "|C_DocType_ID") + "/" + ctx.getContextAsInt( WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, TableName, trx);
		}

		String retValue = getDocumentNo (C_DocType_ID, trx);
		if (!onlyDocType && (retValue == null))
			return getDocumentNo (AD_Client_ID, TableName, trx);
		return retValue;
	}	//	getDocumentNo

	/**
	 * 	Is this a remote client connection
	 *	@return true if client and RMI or Objects on Server
	 */
	public static boolean isRemoteObjects()
	{
		return CConnection.get().isServerObjects()
			&& CConnection.get().isAppsServerOK(false);
	}	//	isRemoteObjects

	/**
	 * 	Is this a remote client connection
	 *	@return true if client and RMI or Process on Server
	 */
	public static boolean isRemoteProcess()
	{
		return CConnection.get().isServerProcess()
			&& CConnection.get().isAppsServerOK(false);
	}	//	isRemoteProcess


	/**************************************************************************
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning("comment", rs.getWarnings());
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning (String comment, SQLWarning warning)
	{
		if ((comment == null) || (warning == null) || (comment.length() == 0))
			throw new IllegalArgumentException("Required parameter missing");
		log.warning(comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage())
				.append("; State=").append(warn.getSQLState())
				.append("; ErrorCode=").append(warn.getErrorCode());
			log.warning(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Get a string representation of literal used in SQL clause
	 *  @param  sqlClause "S", "U", "I", "W"
	 *  @param  dataType java.sql.Types
	 *  @return NULL or db2: nullif(x,x)
	 */
	public static String NULL (String sqlClause, int dataType)
	{
		if (isDB2())
			return s_cc.getDatabase().nullValue(sqlClause, dataType);
		else
			return "NULL";
	}   //	NULL

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		return s_cc.getDatabase().TO_DATE(time, dayOnly);
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param day day time
	 *  @return TO_DATE String (day only)
	 */
	public static String TO_DATE (Timestamp day)
	{
		return TO_DATE(day, true);
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
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if ((columnName == null) || (AD_Language == null) || (columnName.length() == 0))
			throw new IllegalArgumentException("Required parameter missing");
		return s_cc.getDatabase().TO_CHAR(columnName, displayType, AD_Language);
	}   //  TO_CHAR

	/**
	 *  Package Strings for SQL command in quotes
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command in quotes.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if ((txt == null) || (txt.length() == 0))
			return "NULL";

		//  Length
		String text = txt;
		if ((maxLength != 0) && (text.length() > maxLength))
			text = txt.substring(0, maxLength);

		//  copy characters		(we need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(QUOTE);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == QUOTE)
				out.append("''");
			else
				out.append(c);
		}
		out.append(QUOTE);		//	'
		//
		return out.toString();
	}	//	TO_STRING

	/**
	 * 	Return where clause for column and parameter
	 *	@param columnName column name
	 *	@param parameter parameter value
	 *	@return where clause with IS NULL, LIKE or = based on parameter
	 */
	public static String getSqlWhere (String columnName, String parameter)
	{
		StringBuffer retValue = new StringBuffer(columnName);
		if (Util.isEmpty(parameter))
			retValue.append(" IS NULL");
		else
		{
			if (parameter.indexOf("%") != -1)
				retValue.append(" LIKE ");
			else
				retValue.append("=");
			retValue.append(TO_STRING(parameter));
		}
		return retValue.toString();
	}	//	getSQLWhere

	/**
	 * 	Return where clause for column and parameter
	 *	@param columnName column name
	 *	@param parameter parameter value
	 *	@return where clause with IS NULL, or = based on parameter
	 */
	public static String getSqlWhere (String columnName, Timestamp parameter)
	{
		StringBuffer retValue = new StringBuffer(columnName);
		if (parameter == null)
			retValue.append(" IS NULL");
		else
			retValue.append("=").append(TO_DATE(parameter, true));	//	date
		return retValue.toString();
	}	//	getSQLWhere

	/** Quote			*/
	private static final char QUOTE = '\'';

	private static void dumpBorrowers()
	{
		int i = 0;
		Iterator<Map.Entry<Connection, Throwable>> iter = s_borrowers.entrySet().iterator();
		while( iter.hasNext() ) {
			Map.Entry<Connection, Throwable> entry = iter.next();
			
			try {
				if (entry.getKey().isClosed()) {
					iter.remove();
				} else {
					log.log(Level.WARNING, "Connection[" + i + "]=" + entry.getKey() + " borrowed from: ", entry.getValue());
					++i;
				}
			} catch (SQLException e) {
			}
		}
		if( s_borrowers.size() > 0)
			log.log(Level.WARNING, "Connections remaining: " + s_borrowers.size());
	}

	private static final Map<Connection, Throwable> s_borrowers = Collections
			.synchronizedMap(new IdentityHashMap<Connection, Throwable>()); 

	private static StackTraceElement getBorrowPoint()
	{
		for( StackTraceElement elem : new Throwable().getStackTrace() ) {
			if(!elem.getClassName().startsWith("org.compiere.util"))
				return elem;
		}
		return null;
	}
	
	static Connection getCachedConnection() throws SQLException {
		Connection conn = getDatabase().getCachedConnection(s_cc, false, Connection.TRANSACTION_READ_COMMITTED);
		if (conn != null) {
			s_borrowers.put(conn, new Throwable());
		} else {
			dumpBorrowers();
			throw new UnsupportedOperationException("DB.getCachedConnection() - @NoDBConnection@");
		}
		log.finest("Connection count: " + s_borrowers.size() + "; borrowed at " + getBorrowPoint());
		return conn;
	}
	
	static void returnCachedConnection( Connection conn )
	{
		Throwable t = s_borrowers.remove(conn);
		if( t == null )
			log.log(Level.WARNING, "Connection=" + conn + " was not previously borrowed; returned at " + getBorrowPoint());
		log.finest("Connection count: " + s_borrowers.size() + "; returned at " + getBorrowPoint());
	}
	
	static Throwable getBorrower(Connection conn)
	{
		return s_borrowers.get(conn);
	}
	
}	//	DB

