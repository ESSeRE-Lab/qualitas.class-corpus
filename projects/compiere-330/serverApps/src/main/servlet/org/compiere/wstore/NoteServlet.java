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

import javax.servlet.*;
import javax.servlet.http.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *  Web Notice.
 *
 *  @author     Jorg Janke
 *  @version    $Id: NoteServlet.java,v 1.3 2006/09/16 08:32:34 comdivision Exp $
 */
public class NoteServlet extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**	Logging						*/
	private CLogger					log = CLogger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "NoteServlet";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	@Override
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("NoteServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	@Override
	public String getServletInfo()
	{
		return "Compiere Web Note Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	@Override
	public void destroy()
	{
		log.fine("destroy");
	}   //  destroy

	/*************************************************************************/

	public static final String  P_Note_ID			= "AD_Note_ID";
	public static final String	P_ATTACHMENT_INDEX 	= "AttachmentIndex";

	/**
	 *  Process the HTTP Get request.
	 *  Attachment Download request
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
		String url = "/notes.jsp";
		//
		HttpSession session = request.getSession(false);
		if (session == null 
			|| session.getAttribute(WebInfo.NAME) == null)
			url = "/login.jsp";
		else
		{
			session.removeAttribute(WebSessionCtx.HDR_MESSAGE);
			WebInfo info = (WebInfo)session.getAttribute(WebInfo.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Note_ID - if is valid and belongs to wu then create PDF & stream it
			String msg = streamAttachment (request, response);
			if (msg == null || msg.length() == 0)
				return;
			if (info != null)
				info.setMessage(msg);
		}

		log.info ("Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}   //  doGet

	/**
	 * 	Stream Attachment
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	private String streamAttachment (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Note ID
		int AD_Note_ID = WebUtil.getParameterAsInt(request, P_Note_ID);
		if (AD_Note_ID == 0)
		{
			log.fine("No AD_Note_ID)");
			return "No Notice ID";
		}
		int attachmentIndex = WebUtil.getParameterAsInt(request, P_ATTACHMENT_INDEX);
		if (attachmentIndex == 0)
		{
			log.fine("No index)");
			return "No Request Attachment index";
		}
		log.info("AD_Notice_ID=" + AD_Note_ID + " / " + attachmentIndex);

		//	Get Note
		Ctx ctx = JSPEnv.getCtx(request);
		MNote doc = new MNote (ctx, AD_Note_ID, null);
		if (doc.getAD_Note_ID() != AD_Note_ID)
		{
			log.fine("Note not found - ID=" + AD_Note_ID);
			return "Notice not found";
		}
		
		MAttachment attachment = doc.getAttachment(false);
		if (attachment == null)
		{
			log.fine("No Attachment for AD_Note_ID=" + AD_Note_ID);
			return "Notice Attachment not found";
		}
		
		//	Get WebUser & Compare with invoice
		HttpSession session = request.getSession(true);
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getAD_User_ID() != doc.getAD_User_ID())
		{
			log.warning ("AD_Note_ID="
				+ AD_Note_ID + " - User_ID=" + doc.getAD_User_ID()
				+ " = Web_User=" + wu.getAD_User_ID());
			return "Your Notice not found";
		}
		//	Stream it
		return WebUtil.streamAttachment (response, attachment, attachmentIndex);
	}	//	streamAttachment


	/**************************************************************************
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
		String url = "/notes.jsp";

		//  Get Session attributes
		HttpSession session = request.getSession(true);
		session.removeAttribute(WebSessionCtx.HDR_MESSAGE);
		//
		Ctx ctx = JSPEnv.getCtx(request);
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu == null)
		{
			log.warning("No web user");
			if (!response.isCommitted ())
				response.sendRedirect("loginServlet?ForwardTo=notes.jsp");	//	entry
			return;
		}
		WebEnv.dump(request);
		
		int AD_Note_ID = WebUtil.getParameterAsInt(request, P_Note_ID);
		String processed = WebUtil.getParameter (request, "Processed");
		boolean prc = processed != null && processed.length() > 0;
		if (prc)
		{
			MNote note = new MNote (ctx, AD_Note_ID, null);
			if (note.get_ID() == AD_Note_ID)
			{
				note.setProcessed(true);
				note.save();
				log.fine("doPost - " + note);
			}
		}
		
		//	Redisplay
		log.info ("Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}   //  doPost

}   //  NoteServlet
