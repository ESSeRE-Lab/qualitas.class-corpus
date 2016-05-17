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

import java.math.*;
import java.util.logging.*;

import org.compiere.util.*;

/**
 *	Delere Notes (Notice)
 *	
 *  @author Jorg Janke
 *  @version $Id: NoteDelete.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class NoteDelete extends SvrProcess
{
	private int		p_AD_User_ID = -1;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) {
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("AD_User_ID"))
				p_AD_User_ID = ((BigDecimal)element.getParameter()).intValue();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	@Override
	protected String doIt() throws Exception
	{
		log.info("doIt - AD_User_ID=" + p_AD_User_ID);
		
		String sql = "DELETE FROM AD_Note WHERE AD_Client_ID=" + getAD_Client_ID();
		if (p_AD_User_ID > 0)
			sql += " AND AD_User_ID=" + p_AD_User_ID;
		//
		int no = DB.executeUpdate(sql, get_TrxName());
		return "@Deleted@ = " + no;
	}	//	doIt

}	//	NoteDelete
