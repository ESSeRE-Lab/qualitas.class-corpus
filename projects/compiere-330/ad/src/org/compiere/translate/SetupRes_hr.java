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
package org.compiere.translate;

import java.util.*;

/**
 *	Setup Resources
 *
 * 	@author 	Marko Bubalo
 * 	@version 	$Id: SetupRes_hr.java,v 1.2 2006/07/30 00:57:42 jjanke Exp $
 */
public class SetupRes_hr extends ListResourceBundle
{
	/**	Translation Info	*/
	/**	Translation Info	*/
	static final Object[][] contents = new String[][]{
	{ "CompiereServerSetup", 	"Postavke Compiere servera" },
	{ "Ok", 					"U redu" },
	{ "File", 					"Datoteka" },
	{ "Exit", 					"Izlaz" },
	{ "Help", 					"Pomo\u0107" },
	{ "PleaseCheck", 			"Molim provjeriti" },
	{ "UnableToConnect", 		"Gre\u0161ka u spajanju na Compiere web pomo\u0107" },
	//
	{ "CompiereHomeInfo", 		"Compiere Home je glavni direktorij" },
	{ "CompiereHome", 			"Compiere Home" },
	{ "WebPortInfo", 			"Web (HTML) Port" },
	{ "WebPort", 				"Web Port" },
	{ "AppsServerInfo", 		"Naziv servera aplikacije" },
	{ "AppsServer", 			"Server aplikacije" },
	{ "DatabaseTypeInfo", 		"Tip baze podataka" },
	{ "DatabaseType", 			"Tip baze podataka" },
	{ "DatabaseNameInfo", 		"Naziv baze " },
	{ "DatabaseName", 			"Naziv baze (SID)" },
	{ "DatabasePortInfo", 		"Database Listener Port" },
	{ "DatabasePort", 			"Port baze" },
	{ "DatabaseUserInfo", 		"Database Compiere User ID" },
	{ "DatabaseUser", 			"Korisnik baze" },
	{ "DatabasePasswordInfo", 	"Lozinka korisnika baze" },
	{ "DatabasePassword", 		"Lozinka baze" },
	{ "TNSNameInfo", 			"TNS or Global Database Name" },
	{ "TNSName", 				"TNS Name" },
	{ "SystemPasswordInfo", 	"Lozinka korisnika Sytem" },
	{ "SystemPassword", 		"System lozinka" },
	{ "MailServerInfo", 		"Mail Server" },
	{ "MailServer", 			"Mail Server" },
	{ "AdminEMailInfo", 		"Compiere Administrator EMail" },
	{ "AdminEMail", 			"Admin EMail" },
	{ "DatabaseServerInfo", 	"Naziv servera baze" },
	{ "DatabaseServer", 		"Server baze" },
	{ "JavaHomeInfo", 			"Java Home Folder" },
	{ "JavaHome", 				"Java Home" },
	{ "JNPPortInfo", 			"Application Server JNP Port" },
	{ "JNPPort", 				"JNP Port" },
	{ "MailUserInfo", 			"Compiere Mail korisnik" },
	{ "MailUser", 				"Mail korisnik" },
	{ "MailPasswordInfo", 		"Compiere Mail lozinka korisnika" },
	{ "MailPassword", 			"Mail lozinka" },
	{ "KeyStorePassword",		"Key Store Password" },
	{ "KeyStorePasswordInfo",	"Password for SSL Key Store" },
	//
	{ "JavaType",				"Java VM"},
	{ "JavaTypeInfo",			"Java VM Vendor"},
	{ "AppsType",				"Server Type"},
	{ "AppsTypeInfo",			"J2EE Application Server Type"},
	{ "DeployDir",				"Deployment"},
	{ "DeployDirInfo",			"J2EE Deployment Directory"},
	{ "ErrorDeployDir",			"Error Deployment Directory"},
	//
	{ "TestInfo", 				"Test postavki" },
	{ "Test", 					"Test" },
	{ "SaveInfo", 				"Sa\u010duvati postavke" },
	{ "Save", 					"Sa\u010duvati" },
	{ "HelpInfo", 				"Pomo\u0107" },
	//
	{ "ServerError", 			"Server Setup Error" },
	{ "ErrorJavaHome", 			"Error Java Home" },
	{ "ErrorCompiereHome", 		"Error Compiere Home" },
	{ "ErrorAppsServer", 		"Error Apps Server (do not use localhost)" },
	{ "ErrorWebPort", 			"Error Web Port" },
	{ "ErrorJNPPort", 			"Error JNP Port" },
	{ "ErrorDatabaseServer", 	"Error Database Server (do not use localhost)" },
	{ "ErrorDatabasePort", 		"Error Database Port" },
	{ "ErrorJDBC", 				"Error JDBC Connection" },
	{ "ErrorTNS", 				"Error TNS Connection" },
	{ "ErrorMailServer", 		"Error Mail Server (do not use localhost)" },
	{ "ErrorMail", 				"Error Mail" },
	{ "ErrorSave", 				"Error Sving File" },

	{ "NewSecurityKey",			"Your new Security Key (lib/CompiereSecure.dat)\n"
		+ "was created for you to be used for Encryption.\n" 
		+ "Please make sure to back it up securely.\n"
		+ "If lost, you are not able to recover encrypted data." },
	{ "SecurityKeyError",		"Error ocured when creating/installing your Security Key.\n"
		+ "Please contact support with Log information." }, 	
	{ "Start",					"Start" },
	{ "Done",					"Finished" },
	{ "SelectOption",			"Select Option" },
	{ "ServerInstall",			"Server Install" },
	{ "CreateNewDatabase",		"Create new Database" },
	{ "DropOldCreateNewDatabase",	"DROP OLD and create NEW Database" },
	{ "MigrateExistingDatabase",	"Migrate Database" },
		
	{ "ErrorProcess",			"Process Error\nPlease check log for details" },
	{ "ServerSetupComplete",	"Server Setup Complete\nPlease Migrate or Import new Database." },
	{ "DropExistingDatabase",	"Do you want to DROP the existing database\n(loosing all existing data)\nand create a NEW database?" }, 
		
	{ "JNDIPort", 				"JNDI Port" },
	{ "ErrorWasClient", 		"Error WAS Client PATH" }
	};

	/**
	 * 	Get Contents
	 * 	@return contents
	 */
	@Override
	public Object[][] getContents()
	{
		return contents;
	}	//	getContents

}	//	SerupRes
