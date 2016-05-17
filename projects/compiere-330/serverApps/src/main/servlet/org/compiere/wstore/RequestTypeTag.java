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
package org.compiere.wstore;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.ecs.xhtml.*;
import org.compiere.util.*;

/**
 *  Request Type Tag.
 * 	Create Drop Down List with valid values
 *	<code>
 *	<cws:requestType/>
 *	</code>
 *  @author Jorg Janke
 *  @version $Id: RequestTypeTag.java,v 1.2 2006/07/30 00:53:21 jjanke Exp $
 */
public class RequestTypeTag extends TagSupport
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**	Logger							*/
	private static CLogger			log = CLogger.getCLogger (RequestTypeTag.class);

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 */
	@Override
	public int doStartTag()
	{
		JspWriter out = pageContext.getOut();
		select select = getRequestType();
		select.output(out);
		//
		return (SKIP_BODY);
	}   //  doStartTag

	/**
	 * 	Create Select List
	 *	@return select list
	 */
	private select getRequestType()
	{
		select select = new select(RequestServlet.P_REQUESTTYPE_ID, getOptions());
		select.setID("ID_" + RequestServlet.P_REQUESTTYPE_ID);
		return select;
	}	//	getRequestType

	/**
	 * 	Get the Request Type options
	 * 	@return array of options
	 */
	private option[] getOptions()
	{
		Ctx ctx = JSPEnv.getCtx((HttpServletRequest)pageContext.getRequest());
		int AD_Client_ID = ctx.getAD_Client_ID();
		if (AD_Client_ID == 0)
			log.log(Level.SEVERE, "AD_Client_ID not found");
		else
			log.config("AD_Client_ID=" + AD_Client_ID);
		ArrayList<option> list = new ArrayList<option>();
		//
		String sql = "SELECT R_RequestType_ID, Name FROM R_RequestType "
			+ "WHERE AD_Client_ID=? AND IsActive='Y' AND IsSelfService='Y' "
			+ "ORDER BY ASCII(IsDefault) DESC, Name";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				option o = new option (rs.getString(1));
				o.addElement(Util.maskHTML(rs.getString(2)));
				list.add(o);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}

		//	Return to Array and return
		option options[] = new option [list.size()];
		list.toArray(options);
		log.fine("#" + options.length);
		return options;
	}	//	getOptions

}	//	RequestTypeTag
