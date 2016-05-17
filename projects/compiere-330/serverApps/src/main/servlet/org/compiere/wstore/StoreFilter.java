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

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.compiere.util.*;

/**
 * 	Web Store Filter
 *	
 *  @author Jorg Janke
 *  @version $Id: StoreFilter.java,v 1.3 2006/07/30 00:53:21 jjanke Exp $
 */
public class StoreFilter implements javax.servlet.Filter
{
	/**	Logging								*/
	private static CLogger		log = null;

	/**
	 * 	Init
	 *	@param config configuration
	 *	@throws ServletException
	 */
	public void init (FilterConfig config) throws ServletException
	{
		WebEnv.initWeb(config.getServletContext(), "StoreFilter");
		if (log == null)
			log = CLogger.getCLogger(StoreFilter.class);
		log.info(config.getFilterName());
	}	//	init
	
	/**
	 * 	Destroy
	 */
	public void destroy ()
	{
	}	//	destroy

	/**
	 * 	Filter
	 *	@param request request
	 *	@param response response
	 *	@param chain chain
	 *	@throws IOException 
	 *	@throws ServletException
	 */
	public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException
	{
		//  Get URI
		String uri = "";
		if (request instanceof HttpServletRequest)
		{
			HttpServletRequest req = (HttpServletRequest)request;
			uri = req.getRequestURI();
		}

		//  Ignore static content
		boolean check = uri.indexOf("Servlet") != -1;
		boolean pass = true;

		// We need to check
		if (check)
		{
			String enc = request.getCharacterEncoding();
			try
			{
				enc = request.getCharacterEncoding();
				if (enc == null)
					request.setCharacterEncoding(WebEnv.ENCODING);
				if (enc == null)
					log.finer("Checked=" + uri);
				else
					log.finer("Checked=" + uri + " - Enc=" + enc);
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "Set CharacterEndocung=" 
					+ enc + "->" + WebEnv.ENCODING, e);
			}
		}
	//	else
	//		log.finer("NotChecked=" + uri);
		
		//  **  Start   **
		if (pass)
			chain.doFilter(request, response);
		else
		{
			log.warning("Rejected " + uri);
			String msg = "Error: Access Rejected";
			WebDoc doc = WebDoc.create (msg);
			//	Body
			body b = doc.getBody();
			b.addElement(new p(uri, AlignType.CENTER));
			//	fini
			response.setContentType("text/html");
			PrintWriter out = new PrintWriter (response.getOutputStream());
			doc.output(out);
			out.close();
		}

	}	//	doFilter
	
}	//	StoreFilter
