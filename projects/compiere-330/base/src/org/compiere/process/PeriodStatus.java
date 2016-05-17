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

import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *	Open/Close all Period (Control)
 *	
 *  @author Jorg Janke
 *  @version $Id: PeriodStatus.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class PeriodStatus extends SvrProcess
{
	/** Period						*/
	private int			p_C_Period_ID = 0;
	/** Action						*/
	private String		p_PeriodAction = null;
	
	
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
			else if (name.equals("PeriodAction"))
				p_PeriodAction = (String)element.getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		p_C_Period_ID = getRecord_ID();
	}	//	prepare

	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		log.info ("C_Period_ID=" + p_C_Period_ID + ", PeriodAction=" + p_PeriodAction);
		MPeriod period = new MPeriod (getCtx(), p_C_Period_ID, get_TrxName());
		if (period.get_ID() == 0)
			throw new CompiereUserException("@NotFound@  @C_Period_ID@=" + p_C_Period_ID);

		StringBuffer sql = new StringBuffer ("UPDATE C_PeriodControl ");
		sql.append("SET PeriodStatus='");
		//	Open
		if (X_C_PeriodControl.PERIODACTION_OpenPeriod.equals(p_PeriodAction))
			sql.append (X_C_PeriodControl.PERIODSTATUS_Open);
		//	Close
		else if (X_C_PeriodControl.PERIODACTION_ClosePeriod.equals(p_PeriodAction))
			sql.append (X_C_PeriodControl.PERIODSTATUS_Closed);
		//	Close Permanently
		else if (X_C_PeriodControl.PERIODACTION_PermanentlyClosePeriod.equals(p_PeriodAction))
			sql.append (X_C_PeriodControl.PERIODSTATUS_PermanentlyClosed);
		else
			return "-";
		//
		sql.append("', PeriodAction='N', Updated=SysDate,UpdatedBy=").append(getAD_User_ID());
		//	WHERE
		sql.append(" WHERE C_Period_ID=").append(period.getC_Period_ID())
			.append(" AND PeriodStatus<>'P'")
			.append(" AND PeriodStatus<>'").append(p_PeriodAction).append("'");
			
		int no = DB.executeUpdate(sql.toString(), get_TrxName());
		
		CacheMgt.get().reset("C_PeriodControl", 0);
		CacheMgt.get().reset("C_Period", p_C_Period_ID);
		return "@Updated@ #" + no;
	}	//	doIt

}	//	PeriodStatus
