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

import java.io.*;

import javax.sql.*;

/**
 *  Send New Data To Remote for Update
 *
 *  @author Jorg Janke
 *  @version $Id: RemoteUpdateVO.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class RemoteUpdateVO implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Boolean			Test = Boolean.FALSE;
//	public Integer			AD_Table_ID = null;
	public String			TableName = null;
	public String			Sql = null;
	public String[]			KeyColumns = null;
	public RowSet			CentralData = null;

	/**
	 * 	String Representation
	 * 	@return info
	 */
	@Override
	public String toString()
	{
		return "RemoteUpdateVO[test=" + Test
			+ "-" + TableName
	//		+ "," + Sql
			+ "]";
	}	//	toString


}	//	RemoteUpdateVO
