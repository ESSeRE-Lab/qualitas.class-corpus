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
package org.compiere.install;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import oracle.jdbc.*;
import org.compiere.db.*;
import org.compiere.startup.*;


/**
 *	Oracle Confguration
 *	
 *  @author Jorg Janke
 *  @version $Id: ConfigOracle.java,v 1.3 2006/07/30 00:57:42 jjanke Exp $
 */
public class ConfigOracle extends Config
{
	/**
	 * 	ConfigOracle
	 * 	@param data configuration
	 * 	@param XE express edition
	 */
	public ConfigOracle (ConfigurationData data, boolean XE)
	{
		super (data);
		m_XE = XE;
	}	//	ConfigOracle
	
	/**	Oracle Driver			*/
	private static OracleDriver s_oracleDriver = null;
	/** Discoverd TNS			*/
	private String[] 			p_discovered = null;
	/** Last Connection			*/
	private Connection			m_con = null;
	/** Express Edition			*/
	private boolean 			m_XE = false;
	
	/**
	 * 	Init
	 */
	@Override
	public void init()
	{
		p_data.setDatabasePort(String.valueOf(DB_Oracle.DEFAULT_PORT));
		//
		p_data.setDatabaseSystemPassword(true);
		// Database name
		p_data.setDatabaseName(true);
		// Database Discovered
		p_data.setDatabaseDiscovered(!m_XE);
		// Database name
		p_data.setDatabaseUser(true);
	}	//	init
	
	/**
	 * 	Discover Databases.
	 * 	To be overwritten by database configs
	 *	@param selected selected database
	 *	@return array of databases
	 */
	@Override
	public String[] discoverDatabases(String selected)
	{
		if (p_discovered != null)
			return p_discovered;
		//
		ArrayList<String> list = new ArrayList<String>();
		//	default value to lowercase or null
		String def = selected;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
			list.add(def.toLowerCase());

		if (m_XE)
		{
			String serviceName = "xe";
			if (!list.contains(serviceName))
				list.add(serviceName);
		}
		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (String element : entries) {
			String entry = element.toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (element.substring(0, element.length()-4));
				String[] serviceNames = getTNS_Names (sb);
				if (serviceNames != null)
				{
					for (String element2 : serviceNames) {
						String serviceName = element2.toLowerCase();
						if (!list.contains(serviceName))
							list.add(serviceName);
					}
					break;
				}
			}
		}	//	for all path entries

		p_discovered = new String[list.size()];
		list.toArray(p_discovered);
		return p_discovered;
	}	//	discoverDatabases
	
	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		log.fine(tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			log.warning("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return service names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList<String> list = new ArrayList<String>();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			log.finest(i + ": " + line);
			if (false)	//	get TNS Name
			{
				if (line.length() > 0
					&& Character.isLetter(line.charAt(0))	//	no # (
					&& line.indexOf("=") != -1
					&& line.indexOf("EXTPROC_") == -1
					&& line.indexOf("_HTTP") == -1)
				{
					String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
					log.fine(entry);
					list.add(entry);
				}
			}
			else	//	search service names
			{
				if (line.length() > 0
					&& line.toUpperCase().indexOf("SERVICE_NAME") != -1)
				{
					String entry = line.substring(line.indexOf('=')+1).trim().toLowerCase();
					int index = entry.indexOf(')');
					if (index != 0)
						entry = entry.substring(0, index).trim();
					log.fine(entry);
					list.add(entry);
				}
				
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names
	
	
	/**************************************************************************
	 * 	Test
	 *	@return error message or null if OK
	 */
	@Override
	public String test()
	{
		//	Database Server
		String server = p_data.getDatabaseServer();
		boolean pass = server != null && server.length() > 0
			&& server.toLowerCase().indexOf("localhost") == -1 
			&& !server.equals("127.0.0.1");
		String error = "Not correct: DB Server = " + server;
		InetAddress databaseServer = null;
		try
		{
			if (pass)
				databaseServer = InetAddress.getByName(server);
			pass = (databaseServer != null);
		}
		catch (Exception e)
		{
			error += " - " + e.getMessage();
			pass = false;
		}
		if((getPanel())!=null)
			signalOK(getPanel().okDatabaseServer, "ErrorDatabaseServer", 
					pass, true, error); 
		if (!pass)
		{
			log.info("NOT OK: Database Server = " + databaseServer);
			return error;
		}
		log.info("OK: Database Server = " + databaseServer);
		setProperty(Environment.COMPIERE_DB_SERVER, databaseServer.getHostName());
		setProperty(Environment.COMPIERE_DB_TYPE, p_data.getDatabaseType());
		setProperty(Environment.COMPIERE_DB_PATH, Environment.DBTYPE_ORACLE);

		//	Database Port
		int databasePort = p_data.getDatabasePort();
		pass = p_data.testPort (databaseServer, databasePort, true);
		error = "DB Server Port = " + databasePort; 
		if((getPanel())!=null)
			signalOK(getPanel().okDatabaseServer, "ErrorDatabasePort",
					pass, true, error);
		if (!pass)
			return error;
		log.info("OK: Database Port = " + databasePort);
		setProperty(Environment.COMPIERE_DB_PORT, String.valueOf(databasePort));


		//	JDBC Database Info
		String databaseName = p_data.getDatabaseName();	//	Service Name
		String systemPassword = p_data.getDatabaseSystemPassword();
		pass = systemPassword != null && systemPassword.length() > 0;
		error = "No Database System Password entered";
		
		if((getPanel())!=null)
			signalOK(getPanel().okDatabaseSystem, "ErrorJDBC",
					pass, true,	error);
		if (!pass)
			return error;
		//
		//	URL (derived)	jdbc:oracle:thin:@//prod1:1521/prod1
		String url = "jdbc:oracle:thin:@//" + databaseServer.getHostName()
			+ ":" + databasePort
			+ "/" + databaseName;
		pass = testJDBC(url, "system", systemPassword);
		error = "Error connecting: " + url 
			+ " - as system/" + systemPassword;
		
		if((getPanel())!=null)
			signalOK(getPanel().okDatabaseSystem, "ErrorJDBC",
					pass, true, error);
		if (!pass)
			return error;
		log.info("OK: Connection = " + url);
		setProperty(Environment.COMPIERE_DB_URL, url);
		log.info("OK: Database System User " + databaseName);
		setProperty(Environment.COMPIERE_DB_NAME, databaseName);
		setProperty(Environment.COMPIERE_DB_SYSTEM, systemPassword);


		//	Database User Info
		String databaseUser = p_data.getDatabaseUser();	//	UID		
		if (databaseUser.equalsIgnoreCase("System")||databaseUser.equalsIgnoreCase("SYS")){
			error="The database user must be different from System and Sys";
			signalOK(getPanel().okDatabaseUser, "ErrorJDBC", false, true, error);
			return error;
		}
		String databasePassword = p_data.getDatabasePassword();	//	PWD
		pass = databasePassword != null && databasePassword.length() > 0;
		error = "Invalid Database User Password";
		if((getPanel())!=null)
			signalOK(getPanel().okDatabaseUser, "ErrorJDBC",
					pass, true, error); 
		if (!pass)
			return error;
		//	Ignore result as it might not be imported
		pass = testJDBC(url, databaseUser, databasePassword);
		error = "Cannot connect to User: " + databaseUser + "/" + databasePassword + " - Database may not be imported yet (OK on initial run).";
		if((getPanel())!=null)
			signalOK(getPanel().okDatabaseUser, "ErrorJDBC",
					pass, false, error);
		if (pass)
		{
			log.info("OK: Database User = " + databaseUser);
			if (m_con != null)
				setProperty(ConfigurationData.COMPIERE_WEBSTORES, getWebStores(m_con));
			setProperty(Environment.COMPIERE_DB_USER_EXISTS, "Y");
		}
		else
		{
			setProperty(Environment.COMPIERE_DB_USER_EXISTS, "N");
			log.warning(error);
		}
		setProperty(Environment.COMPIERE_DB_USER, databaseUser);
		setProperty(Environment.COMPIERE_DB_PASSWORD, databasePassword);

		/** No need to test - TNS Name Info via sqlplus - if not tomcat 
		if (!p_data.getAppsServerType().equals(Environment.APPSTYPE_TOMCAT))
		{
			String sqlplus = "sqlplus system/" + systemPassword + "@" + databaseName
				+ " @" + getProperty(ConfigurationData.COMPIERE_HOME)
				+ "/utils/oracle/Test.sql";
			log.config(sqlplus);
			pass = testSQL(sqlplus);
			error = "Error connecting via: " + sqlplus;
			signalOK(getPanel().okDatabaseSQL, "ErrorTNS", 
				pass, true, error);
			if (pass)
				log.info("OK: Database SQL Connection");
		}
		**/
		
		//	OCI Test
		if (System.getProperty("TestOCI", "N").equals("Y"))
		{
			url = "jdbc:oracle:oci8:@" + databaseName;
			pass = testJDBC(url, "system", systemPassword);
			if (pass)
				log.info("OK: Connection = " + url);
			else
				log.warning("Cannot connect via Net8: " + url);
		}
		else
			log.info("OCI Test Skipped");
		
		if (m_con != null) {
			try {
				m_con.close();
				m_con = null;
			}
			catch (Exception e){
				log.warning("Cannot close connection: " + e);
			}
		}

		return null;
	}	//	test


	/**
	 * 	Test JDBC Connection to Server
	 * 	@param url connection string
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String url, String uid, String pwd)
	{
		log.fine("Url=" + url + ", UID=" + uid);
		try
		{
			if (s_oracleDriver == null)
			{
				s_oracleDriver = new OracleDriver();
				DriverManager.registerDriver(s_oracleDriver);
			}
			m_con = DriverManager.getConnection(url, uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			log.warning("Check [ORACLE_HOME]/jdbc/Readme.txt for (OCI) driver setup");
			log.warning(ule.toString());
		}
		catch (Exception e)
		{
			log.warning(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param sqlplus sqlplus command line
	 * 	@return true if OK
	 *
	private boolean testSQL (String sqlplus)
	{
		StringBuffer sbOut = new StringBuffer();
		StringBuffer sbErr = new StringBuffer();
		int result = -1;
		try
		{
			Process p = Runtime.getRuntime().exec (sqlplus);
			InputStream in = p.getInputStream();
			int c;
			while ((c = in.read()) != -1)
			{
				sbOut.append((char)c);
				System.out.print((char)c);
			}
			in.close();
			in = p.getErrorStream();
			while ((c = in.read()) != -1)
				sbErr.append((char)c);
			in.close();
			//	Get result
			try
			{
				Thread.yield();
				result = p.exitValue();
			}
			catch (Exception e)		//	Timing issue on Solaris.
			{
				Thread.sleep(200);	//	.2 sec
				result = p.exitValue();
			}
		}
		catch (Exception ex)
		{
			log.warning(ex.toString());
		}
		log.finer(sbOut.toString());
		if (sbErr.length() > 0)
			log.warning(sbErr.toString());
		return result == 0;
	}	//	testSQL
	**/
			
}	//	ConfigOracle
