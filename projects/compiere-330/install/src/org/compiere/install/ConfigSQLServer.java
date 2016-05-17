/******************************************************************************
 * Product: Compiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.install;

import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.db.*;
import org.compiere.util.*;
import org.compiere.startup.*;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
//import com.microsoft.sqlserver.jdbc.*;

/**
 * 	MS SQL Server Configuration Test
 *	
 *  @author Miklos Keresztes / BIT Software
 */
public class ConfigSQLServer extends Config
{
	/**
	 * 	ConfigSQLServer
	 * 	@param data configuration
	 */
	public ConfigSQLServer (ConfigurationData data)
	{
		super (data);
	}	//	ConfigSQLServer

	/** SQL Server Driver				*/
	private static SQLServerDriver s_SQLServerDriver = null;
	/** Last Connection			*/
	private Connection			m_con = null;

	/**
	 * 	Init
	 */
	@Override
	public void init()
	{
		p_data.setDatabasePort(String.valueOf(DB_SQLServer.DEFAULT_PORT));
		p_data.setDatabaseName("compiere");
		p_data.setDatabaseSystemPassword(true);
	}	//	init

	
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
		setProperty(Environment.COMPIERE_DB_PATH, p_data.getDatabaseType());

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
		String url = "jdbc:sqlserver://" + databaseServer.getHostName()
			+ ":" + databasePort;
			//+ ";databaseName=" + databaseName;  //run_setup before db created
		log.info("url = " + url);
		pass = testJDBC(url, "sa", systemPassword);
		log.info("testJDBC result = " + pass);

		error = "Error connecting: " + url 
			+ " - as sa/" + systemPassword;
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
		error = "Database imported? Cannot connect to User: " + databaseUser + "/" + databasePassword;
		if((getPanel())!=null)
			signalOK(getPanel().okDatabaseUser, "ErrorJDBC",
					pass, false, error);
		if (pass)
		{
			log.info("OK: Database User = " + databaseUser);
			if (m_con != null)
				setProperty(ConfigurationData.COMPIERE_WEBSTORES, getWebStores(m_con));
		}
		else
			log.warning(error);
		setProperty(Environment.COMPIERE_DB_USER, databaseUser);
		setProperty(Environment.COMPIERE_DB_PASSWORD, databasePassword);

		if (!p_data.getAppsServerType().equals(Environment.APPSTYPE_TOMCAT))
		{
			String cmd = "";
			if (Env.isWindows())
			{
				cmd = "osql -U sa -P " + systemPassword + " -d " + databaseName;
				String sqlcmd2 = cmd + "-i utils/sqlServer/Test.sql";
				log.config(sqlcmd2);
				pass = testSQL(sqlcmd2);
				error = "Error connecting via: " + sqlcmd2;
				if((getPanel())!=null)
					signalOK(getPanel().okDatabaseSQL, "ErrorTNS", 
							pass, true, error);
				if (pass)
					log.info("OK: Database SQL Connection");
			}
		}
		
		m_con = null;
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
		log.info("testJDBC: Url=" + url + ", UID=" + uid + ", PWD=" + pwd);
		try
		{
			if (s_SQLServerDriver == null)
			{
				log.info("s_SQLServerDriver == null");
				//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
				//log.info("forName = OK");
				s_SQLServerDriver = new SQLServerDriver();
				log.info("s_SQLServerDriver = " + s_SQLServerDriver.toString());
				DriverManager.registerDriver(s_SQLServerDriver);
				log.info("SQLServerDriver registered");
			}
			m_con = DriverManager.getConnection(url, uid, pwd);
			log.info("m_con = " + m_con.toString());
		}
		catch (Exception e)
		{
			log.warning(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC
	
	/**
	 * 	Test Command Line Connection
	 *  @param sqlcmd sql command line
	 * 	@return true if OK
	 */
	private boolean testSQL (String sqlcmd)
	{
		if (true)
			return true;
		//
		StringBuffer sbOut = new StringBuffer();
		StringBuffer sbErr = new StringBuffer();
		int result = -1;
		try
		{
			Process p = Runtime.getRuntime().exec (sqlcmd);
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
	
}	//	ConfigSQLServer
