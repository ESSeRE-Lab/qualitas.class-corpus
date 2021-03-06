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
 *  License Dialog Translation
 *
 *  @author     Robin Hoo
 *  @version    $Id: IniRes_ml.java,v 1.2 2006/07/30 00:52:23 jjanke Exp $
 */
public class IniRes_ml extends ListResourceBundle
{
	/** Translation Content     */
	static final Object[][] contents = new String[][]
	{
	{ "Compiere_License",   "License Agreement" },
	{ "Do_you_accept",      "Do you accept the License ?" },
	{ "No",                 "No" },
	{ "Yes_I_Understand",   "Yes, I Understand and  Accept" },
	{ "license_htm",        "org/compiere/license.htm" },
	{ "License_rejected",   "License rejected or expired" }
	};

	/**
	 *  Get Content
	 *  @return Content
	 */
	@Override
	public Object[][] getContents()
	{
		return contents;
	}   //  getContent
}   //  IniRes
