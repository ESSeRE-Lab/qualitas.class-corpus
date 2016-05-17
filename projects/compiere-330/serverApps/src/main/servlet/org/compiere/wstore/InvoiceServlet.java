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
import java.sql.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.compiere.model.*;
import org.compiere.util.*;


/**
 *  Check Out.
 *
 *  @author Jorg Janke
 *  @version $Id: InvoiceServlet.java,v 1.3 2006/09/16 08:32:34 comdivision Exp $
 */
public class InvoiceServlet extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**	Logging						*/
	private static CLogger			log = CLogger.getCLogger(InvoiceServlet.class);
	/** Name						*/
	static public final String		NAME = "invoiceServlet";

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
			throw new ServletException("InvoiceServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	@Override
	public String getServletInfo()
	{
		return "Compiere Web Invoice Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	@Override
	public void destroy()
	{
		log.fine("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
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

		String url = "/invoices.jsp";
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

			//	Parameter = Invoice_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamInvoice (request, response);
			if (msg == null || msg.length() == 0)
				return;
			if (info != null)
				info.setMessage(msg);
		}

		log.info ("Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
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
	}	//	doPost

	
	/**
	 * 	Stream invoice
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	private String streamInvoice (HttpServletRequest request, HttpServletResponse response)
	{
		int MIN_SIZE = 2000; 	//	if not created size is 1015
		
		//	Get Invoice ID
		int C_Invoice_ID = WebUtil.getParameterAsInt (request, "Invoice_ID");
		if (C_Invoice_ID == 0)
		{
			log.fine("No ID)");
			return "No Invoice ID";
		}

		//	Get Invoice
		Ctx ctx = JSPEnv.getCtx(request);
		MInvoice invoice = new MInvoice (ctx, C_Invoice_ID, null);
		if (invoice.getC_Invoice_ID() != C_Invoice_ID)
		{
			log.fine("Invoice not found - ID=" + C_Invoice_ID);
			return "Invoice not found";
		}
		//	Get WebUser & Compare with invoice
		HttpSession session = request.getSession(true);
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != invoice.getC_BPartner_ID())
		{
			log.warning ("Invoice from BPartner - C_Invoice_ID="
				+ C_Invoice_ID + " - BP_Invoice=" + invoice.getC_BPartner_ID()
				+ " = BP_Web=" + wu.getC_BPartner_ID());
			return "Your invoice not found";
		}

		//	Check Directory
		String dirName = ctx.getContext("documentDir");
		if (dirName == null || dirName.length() == 0)
			dirName = ".";
		try
		{
			File dir = new File (dirName);
			if (!dir.exists ())
				dir.mkdir ();
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, "Could not create directory " + dirName, ex);
			return "Streaming error - directory";
		}
		//	Check if Invoice already created
		String fileName = invoice.getPDFFileName (dirName);
		File file = new File(fileName);
		if (file.exists() && file.isFile() && file.length() > MIN_SIZE)	
			log.info("Existing: " + file  
				+ " - " + new Timestamp(file.lastModified()));
		else
		{
			log.info("New: " + fileName);
			file = invoice.createPDF (file);
			if (file != null)
			{
				invoice.setDatePrinted (new Timestamp(System.currentTimeMillis()));
				invoice.save();
			}
		}
		//	Issue Error
		if (file == null || !file.exists() || file.length() < MIN_SIZE) 
		{
			log.warning("File does not exist - " + file);
			return "Streaming error - file";
		}

		//	Send PDF
		try
		{
			int bufferSize = 2048; //	2k Buffer
			int fileLength = (int)file.length();
			//
			response.setContentType("application/pdf");
			response.setBufferSize(bufferSize);
			response.setContentLength(fileLength);
			//
			log.fine(file.getAbsolutePath() + ", length=" + fileLength);
			long time = System.currentTimeMillis();		//	timer start
			//
			FileInputStream in = new FileInputStream (file);
			ServletOutputStream out = response.getOutputStream ();
			byte[] buffer = new byte[bufferSize];
			double totalSize = 0;
			int count = 0;
			do
			{
				count = in.read(buffer, 0, bufferSize);
				if (count > 0)
				{
					totalSize += count;
					out.write (buffer, 0, count);
				}
			} while (count != -1);
			out.flush();
			out.close();
			//
			in.close();
			time = System.currentTimeMillis() - time;
			double speed = (totalSize/1024) / ((double)time/1000);
			log.fine("Length=" 
				+ totalSize + " - " 
				+ time + " ms - " 
				+ speed + " kB/sec");
		}
		catch (IOException ex)
		{
			log.log(Level.SEVERE, ex.toString());
			return "Streaming error";
		}

		return null;
	}	//	streamInvoice

}	//	InvoiceServlet
