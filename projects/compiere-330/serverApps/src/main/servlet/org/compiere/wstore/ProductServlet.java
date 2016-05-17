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

import org.compiere.util.*;

/**
 *	Web Product Serach
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ProductServlet.java,v 1.2 2006/07/30 00:53:21 jjanke Exp $
 */
public class ProductServlet  extends HttpServlet
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
			throw new ServletException("ProductServlet.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	@Override
	public String getServletInfo()
	{
		return "Compiere Product Serach Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	@Override
	public void destroy()
	{
		log.info("destroy");
	}   //  destroy

	public static String	P_SEARCHSTRING = "SearchString";
	public static String	P_M_PRODUCT_CATEGORY_ID = "M_Product_Category_ID";


	/*************************************************************************/

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

		Ctx ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		session.removeAttribute(WebSessionCtx.HDR_MESSAGE);
	//	WEnv.dump(session);
	//	WEnv.dump(request);

		//	Web User
	//	WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);

		//	Save in ctx for PriceListTag

		//	Search Parameter
		String searchString = WebUtil.getParameter (request, P_SEARCHSTRING);
		if (searchString != null)
			ctx.put(P_SEARCHSTRING, searchString);
		//	Product Category
		String category = WebUtil.getParameter (request, P_M_PRODUCT_CATEGORY_ID);
		if (category != null)
			ctx.put(P_M_PRODUCT_CATEGORY_ID, category);

		//	Forward
		String url = "/index.jsp";
		log.info ("Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}   //  doPost



	/**
	 *  Process the initial HTTP Get request.
	 *  Reads the Parameter Amt and optional C_Invoice_ID
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
		doPost (request, response);
	}   //  doGet

}	//	ProductServlet
