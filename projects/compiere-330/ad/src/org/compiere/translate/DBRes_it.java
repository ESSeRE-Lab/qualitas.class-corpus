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
 *  Connection Resource Strings
 *
 *  @author     Alessandro Riolo 
 *  @version    $Id: DBRes_it.java,v 1.2 2006/07/30 00:55:13 jjanke Exp $
 */
public class DBRes_it extends ListResourceBundle
{
	/** Data        */
	static final Object[][] contents = new String[][]{
	  { "CConnectionDialog",  "Connessione a Compiere" },
	  { "Name",               "Nome" },
	  { "AppsHost",           "Host dell'Applicativo" },
	  { "AppsPort",           "Porta dell'Applicativo" }, 
	  { "TestApps",           "Applicazione di Test" },
	  { "DBHost",             "Host del Database" },
	  { "DBPort",             "Porta del Database" },
	  { "DBName",             "Nome del Database" },
	  { "DBUidPwd",           "Utente / Password" },
	  { "ViaFirewall",        "via Firewall" },
	  { "FWHost",             "Host del Firewall" },
	  { "FWPort",             "Porta del Firewall" },
	  { "TestConnection",     "Database di Test" },
	  { "Type",               "Tipo di Database" },
	  { "BequeathConnection", "Connessione Dedicata" },
	  { "Overwrite",          "Sovrascri" }, 
		{ "ConnectionProfile",	"Connection" },
		{ "LAN",		 		"LAN" },
		{ "TerminalServer",		"Terminal Server" },
		{ "VPN",		 		"VPN" },
		{ "WAN", 				"WAN" },
	  { "ConnectionError",    "Errore di Connessione" },
	  { "ServerNotActive",    "Server non Attivo" }};

	/**
	 * Get Contsnts
	 * @return contents
	 */
	@Override
	public Object[][] getContents()
	{
		return contents;
	}
}   //  Res
