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
package org.compiere.apps;

import java.awt.*;
import java.awt.print.*;
import java.util.*;

import org.compiere.common.constants.*;
import org.compiere.print.*;
import org.compiere.util.*;

/**
 *	PrintScreen Painter
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: PrintScreenPainter.java,v 1.2 2006/07/30 00:51:27 jjanke Exp $
 */
public class PrintScreenPainter implements Pageable, Printable
{
	/**
	 *  PrintScreen Painter
	 *  @param element Window to print
	 */
	public PrintScreenPainter (Window element)
	{
		m_element = element;
	}	//	PrintScreenPainter

	/**	Element				*/
	private Window		m_element;

	/**
	 * 	Get Number of pages
	 * 	@return 1
	 */
	public int getNumberOfPages()
	{
		return 1;
	}	//	getNumberOfPages

	/**
	 * 	Get Printable
	 * 	@param pageIndex page index
	 * 	@return this
	 * 	@throws java.lang.IndexOutOfBoundsException
	 */
	public Printable getPrintable(int pageIndex) throws java.lang.IndexOutOfBoundsException
	{
		return this;
	}	//	getPrintable

	/**
	 * 	Get Page Format
	 * 	@param pageIndex page index
	 * 	@return Portrait
	 * 	@throws java.lang.IndexOutOfBoundsException
	 */
	public PageFormat getPageFormat(int pageIndex) throws java.lang.IndexOutOfBoundsException
	{
		CPaper paper = new CPaper(false);
		return paper.getPageFormat();
	}	//	getPageFormat

	/**
	 *	Print
	 *  @param graphics graphics
	 *  @param pageFormat page format
	 *  @param pageIndex page index
	 *  @return NO_SUCH_PAGE or PAGE_EXISTS
	 *  @throws PrinterException
	 */
	public int print (Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException
	{
	//	log.config( "PrintScreenPainter.print " + pageIndex, "ClipBounds=" + graphics.getClipBounds());
		if (pageIndex > 0)
			return Printable.NO_SUCH_PAGE;
		//
		Graphics2D g2 = (Graphics2D) graphics;

		//	Start position - top of page
		g2.translate (pageFormat.getImageableX(), pageFormat.getImageableY());

		//	Print Header
		String header = Msg.getMsg(Env.getCtx(), "PrintScreen") + " - "
			+ DisplayType.getDateFormat(DisplayTypeConstants.DateTime).format(new Date());
		int y = g2.getFontMetrics().getHeight();	//	leading + ascent + descent
		g2.drawString(header, 0, y);
		//	Leave one row free
		g2.translate (0, 2*y);

		double xRatio = pageFormat.getImageableWidth() / m_element.getSize().width;
		double yRatio = (pageFormat.getImageableHeight() - 2*y) / m_element.getSize().height;
		//	Sacle evenly, but don't inflate
		double ratio = Math.min(Math.min(xRatio, yRatio), 1.0);
		g2.scale (ratio, ratio);
		//	Print Element
		m_element.printAll (g2);

		return Printable.PAGE_EXISTS;
	}	//	print

	/*************************************************************************/

	/**
	 *	Static print start
	 *  @param element window
	 */
	public static void printScreen (Window element)
	{
		PrintUtil.print(new PrintScreenPainter(element), null, "PrintScreen", 1, false);
	}	//	printScreen

}	//	PrintScreenPainter
