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

import org.compiere.model.*;

/**
 *  Copy Invoice Lines
 *
 *	@author Jorg Janke
 *	@version $Id: CopyFromInvoice.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class CopyFromInvoice extends SvrProcess
{
	private int		m_C_Invoice_ID = 0;

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
			else if (name.equals("C_Invoice_ID"))
				m_C_Invoice_ID = ((BigDecimal)element.getParameter()).intValue();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message 
	 *  @throws Exception if not successful
	 */
	@Override
	protected String doIt() throws Exception
	{
		int To_C_Invoice_ID = getRecord_ID();
		log.info("From C_Invoice_ID=" + m_C_Invoice_ID + " to " + To_C_Invoice_ID);
		if (To_C_Invoice_ID == 0)
			throw new IllegalArgumentException("Target C_Invoice_ID == 0");
		if (m_C_Invoice_ID == 0)
			throw new IllegalArgumentException("Source C_Invoice_ID == 0");
		MInvoice from = new MInvoice (getCtx(), m_C_Invoice_ID, null);
		MInvoice to = new MInvoice (getCtx(), To_C_Invoice_ID, null);
		//
		int no = to.copyLinesFrom (from, false, false);
		//
		return "@Copied@=" + no;
	}	//	doIt

}	//	CopyFromInvoice
