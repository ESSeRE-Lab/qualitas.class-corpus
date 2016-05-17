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
package org.compiere.wf;

import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.process.*;

/**
 *	Manage Workflow Activity
 *	
 *  @author Jorg Janke
 *  @version $Id: WFActivityManage.java,v 1.2 2006/07/30 00:51:05 jjanke Exp $
 */
public class WFActivityManage extends SvrProcess
{
	/**	Abort It				*/	
	private boolean		p_IsAbort = false;
	/** New User				*/
	private int			p_AD_User_ID = 0;
	/** New Responsible			*/
	private int			p_AD_WF_Responsible_ID = 0;
	/** Record					*/
	private int			p_AD_WF_Activity_ID = 0;
	
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
			else if (name.equals("IsAbort"))
				p_IsAbort = "Y".equals(element.getParameter());
			else if (name.equals("AD_User_ID"))
				p_AD_User_ID = element.getParameterAsInt();
			else if (name.equals("AD_WF_Responsible_ID"))
				p_AD_WF_Responsible_ID = element.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		p_AD_WF_Activity_ID = getRecord_ID();
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (variables are parsed)
	 *  @throws Exception if not successful
	 */
	@Override
	protected String doIt() throws Exception
	{
		MWFActivity activity = new MWFActivity (getCtx(), p_AD_WF_Activity_ID, get_TrxName());
		log.info("" + activity);
		
		MUser user = MUser.get(getCtx(), getAD_User_ID());
		//	Abort
		if (p_IsAbort)
		{
			String msg = user.getName() + ": Abort";
			activity.setTextMsg(msg);
			activity.setAD_User_ID(getAD_User_ID());
			activity.setWFState(StateEngine.STATE_Aborted);
			return msg;
		}
		String msg = null;
		//	Change User
		if (p_AD_User_ID != 0 && activity.getAD_User_ID() != p_AD_User_ID)
		{
			MUser from = MUser.get(getCtx(), activity.getAD_User_ID());
			MUser to = MUser.get(getCtx(), p_AD_User_ID);
			msg = user.getName() + ": " + from.getName() + " -> " + to.getName();
			activity.setTextMsg(msg);
			activity.setAD_User_ID(p_AD_User_ID);
		}
		//	Change Responsible
		if (p_AD_WF_Responsible_ID != 0 && activity.getAD_WF_Responsible_ID() != p_AD_WF_Responsible_ID)
		{
			MWFResponsible from = MWFResponsible.get(getCtx(), activity.getAD_WF_Responsible_ID());
			MWFResponsible to = MWFResponsible.get(getCtx(), p_AD_WF_Responsible_ID);
			String msg1 = user.getName() + ": " + from.getName() + " -> " + to.getName();
			activity.setTextMsg(msg1);
			activity.setAD_WF_Responsible_ID(p_AD_WF_Responsible_ID);
			if (msg == null)
				msg = msg1;
			else
				msg += " - " + msg1;
		}
		//
		activity.save();
		
		return msg;
	}	//	doIt
	
}	//	WFActivityManage
