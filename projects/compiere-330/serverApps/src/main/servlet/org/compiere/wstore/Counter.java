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

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import java.util.logging.*;
import org.compiere.util.*;

/**
 *  Web Page Counter
 *  <code>
	http://www.compiere.com/wstore/counter
 *  </code>
 *
 *  @author     Jorg Janke
 *  @version    $Id: Counter.java,v 1.2 2006/07/30 00:53:21 jjanke Exp $
 */
public class Counter extends HttpServlet implements Runnable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**	Logging						*/
	private static CLogger			log = CLogger.getCLogger(Counter.class);

	/** Name						*/
	static public final String		NAME = "counter";

	/**	Requests					*/
	private List<HttpServletRequest>	m_requests = Collections.synchronizedList(new ArrayList<HttpServletRequest>());

	/**
	 * Initialize global variables
	 *
	 * @param config servlet config
	 * @throws ServletException
	 */
	@Override
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("Counter.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	@Override
	public String getServletInfo()
	{
		return "Compiere Web Counter";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	@Override
	public void destroy()
	{
		log.info("");
	}   //  destroy

	
	/**************************************************************************
	 *  Process the HTTP Get request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	@Override
	public void doGet (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		m_requests.add(request);
		new Thread(this).start();
	}   //  doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	@Override
	public void doPost (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet (request, response);
	}   //  doPost

	/**************************************************************************
	 * 	Async Process
	 */
	public void run()
	{
		//	get Request
		HttpServletRequest request = null;
		if (m_requests.size() > 0)
			request = m_requests.remove(0);
		if (request == null)
		{
			log.log(Level.SEVERE, "Nothing in queue");
			return;
		}

		JSPEnv.getCtx(request);
		String ref = request.getHeader("referer");
		if (ref == null || ref.length() == 0)
			ref = request.getRequestURL().toString();
		log.info("Referer=" + request.getHeader("referer") + " - URL=" + request.getRequestURL());
	}	//	run

}   //  Counter
