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

import java.io.*;
import java.util.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *	Web Store Subscription Info.
 *	https://ws-jj/wstore/infoServlet?mode=subscribe&area=101&contact=1000000
 *	http://ws-jj/wstore/infoServlet?mode=subscribe&area=101&contact=1000000
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: InfoServlet.java,v 1.2 2006/07/30 00:53:21 jjanke Exp $
 */
public class InfoServlet  extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**	Logging						*/
	private CLogger			log = CLogger.getCLogger(getClass());

	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("InfoServlet.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	@Override
	public String getServletInfo()
	{
		return "Compiere Interest Area Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	@Override
	public void destroy()
	{
		log.info("destroy");
	}   //  destroy


	/**
	 *  Process the initial HTTP Get request.
	 *  Reads the Parameter mode and subscribes/unsubscribes
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("From " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		HttpSession session = request.getSession(false);
		if (session != null)
			session.removeAttribute(WebSessionCtx.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		String url = "/info.jsp";
		boolean notLoggedIn = session == null 
			|| session.getAttribute(WebInfo.NAME) == null
			|| session.getAttribute(WebUser.NAME) == null;

		//	Allow to unsubscribe w/o login
		boolean success = processParameter(request, !notLoggedIn);
		if (!success)
			url = "/login.jsp";
		
		log.info ("Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}   //  doGet

	/**
	 *  Process the HTTP Post request.
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("From " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		doGet (request, response);
	}   //  doPost


	/**************************************************************************
	 * 	Process Parameter and check them
	 * 	@param request request
	 * 	@param isLoggedIn session exists
	 *	@return true if processed
	 */
	private boolean processParameter (HttpServletRequest request, boolean isLoggedIn)
	{
		HttpSession session = request.getSession(true);
		session.removeAttribute(WebSessionCtx.HDR_MESSAGE);
		Ctx ctx = JSPEnv.getCtx(request);

		//	mode = subscribe
		String mode = WebUtil.getParameter (request, "mode");
		if (mode == null)
			return false;
		boolean subscribe = !mode.startsWith("un");
		//	area = 101
		int R_InterestArea_ID = WebUtil.getParameterAsInt(request, "area");
		MInterestArea ia = MInterestArea.get(ctx, R_InterestArea_ID);
		//	contact = -1
		int AD_User_ID = WebUtil.getParameterAsInt(request, "contact");
		//
		if (subscribe && !isLoggedIn)
		{
			log.config("Subscribe Rejected (Not LoggedIn) R_InterestArea_ID=" 
				+ R_InterestArea_ID + ",AD_User_ID=" + AD_User_ID);
			return false;
		}
		log.fine("Subscribe=" + subscribe
			+ ",R_InterestArea_ID=" + R_InterestArea_ID
			+ ",AD_User_ID=" + AD_User_ID);
		if (R_InterestArea_ID == 0 || AD_User_ID == 0)
			return false;
		//
		MContactInterest ci = MContactInterest.get (ctx, R_InterestArea_ID, AD_User_ID, false, null);
		ci.subscribe(subscribe);
		ci.setRemote_Host(request.getRemoteHost());
		ci.setRemote_Addr(request.getRemoteAddr());
		boolean ok = ci.save();
		if (ok)
			log.fine("success");
		else
			log.log(Level.SEVERE, "subscribe failed");

		//	Lookup user if direct link
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu == null)
		{
			wu = WebUser.get(ctx, AD_User_ID);
			session.setAttribute(WebUser.NAME, wu);
		}
		sendEMail (request, wu, ia.getName(), subscribe);

		return ok;
	}	//	processParameter

	/**
	 * 	Send Subscription EMail.
	 * 	@param request request
	 * 	@param wu web user
	 */
	private void sendEMail (HttpServletRequest request, WebUser wu, 
		String listName, boolean subscribe)
	{
		JSPEnv.sendEMail(request, wu, 
			subscribe ? X_W_MailMsg.MAILMSGTYPE_Subscribe : X_W_MailMsg.MAILMSGTYPE_Unsubscribe,
			new Object[]{listName, wu.getName(), listName});
	}	//	sendEMail


}	//	InfoServlet
