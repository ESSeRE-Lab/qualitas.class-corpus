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

import java.awt.event.*;
import java.sql.*;
import java.util.logging.*;

import javax.swing.*;

import org.compiere.apps.form.*;
import org.compiere.model.*;
import org.compiere.swing.*;
import org.compiere.util.*;


/**
 *	Archive Button Consequences.
 *	Popup Menu
 *	
 *  @author Jorg Janke
 *  @version $Id: AArchive.java,v 1.2 2006/07/30 00:51:27 jjanke Exp $
 */
public class AArchive implements ActionListener
{
	/**
	 * 	Constructor
	 *	@param invoker button
	 *	@param AD_Table_ID table
	 *	@param Record_ID record
	 */
	public AArchive (JComponent invoker, int AD_Table_ID, int Record_ID)
	{
		super ();
		log.config("AD_Table_ID=" + AD_Table_ID + ", Record_ID=" + Record_ID);
		m_AD_Table_ID = AD_Table_ID;
		m_Record_ID = Record_ID;
		getArchives(invoker);
	}	//	AArchive
	
	/**	The Table						*/
	private int			m_AD_Table_ID;
	/** The Record						*/
	private int			m_Record_ID;
	
	/**	The Popup						*/
	private JPopupMenu 	m_popup = new JPopupMenu("ArchiveMenu");
	private CMenuItem 	m_reports = null;
	private CMenuItem 	m_reportsAll = null;
	private CMenuItem 	m_documents = null;
	/** Where Clause					*/
	StringBuffer 		m_where = null;
	
	/**	Logger	*/
	private static CLogger	log	= CLogger.getCLogger (AArchive.class);
	
	/**
	 * 	Display Request Options - New/Existing.
	 * 	@param invoker button
	 */
	private void getArchives (JComponent invoker)
	{
		int reportCount = 0;
		int documentCount = 0;
		
		m_where = new StringBuffer();
		m_where.append("(AD_Table_ID=").append(m_AD_Table_ID)
			.append(" AND Record_ID=").append(m_Record_ID)
			.append(")");
		//	Get all for BP
		if (m_AD_Table_ID == X_C_BPartner.Table_ID)
			m_where.append(" OR C_BPartner_ID=").append(m_Record_ID);
		//
		StringBuffer sql = new StringBuffer("SELECT IsReport, COUNT(*) FROM AD_Archive ")
			.append("WHERE (AD_Table_ID=? AND Record_ID=?) ");
		if (m_AD_Table_ID == X_C_BPartner.Table_ID)
			sql.append(" OR C_BPartner_ID=?");
		sql.append(" GROUP BY IsReport"); 
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), (Trx) null);
			pstmt.setInt(1, m_AD_Table_ID);
			pstmt.setInt(2, m_Record_ID);
			if (m_AD_Table_ID == X_C_BPartner.Table_ID)
				pstmt.setInt(3, m_Record_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				if ("Y".equals(rs.getString(1)))
					reportCount += rs.getInt(2);
				else
					documentCount += rs.getInt(2);
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		//
		if (documentCount > 0)
		{
			m_documents = new CMenuItem(Msg.getMsg(Env.getCtx(), "ArchivedDocuments") 
				+ " (" + documentCount + ")");
			m_popup.add(m_documents).addActionListener(this);
		}
		if (reportCount > 0)
		{
			m_reports = new CMenuItem(Msg.getMsg(Env.getCtx(), "ArchivedReports") 
				+ " (" + reportCount + ")");
			m_popup.add(m_reports).addActionListener(this);
		}
		//	All Reports
		String sql1 = "SELECT COUNT(*) FROM AD_Archive WHERE AD_Table_ID=? AND IsReport='Y'";
		int allReports = DB.getSQLValue(null, sql1, m_AD_Table_ID); 
		if (allReports > 0)
		{
			m_reportsAll = new CMenuItem(Msg.getMsg(Env.getCtx(), "ArchivedReportsAll") 
				+ " (" + reportCount + ")");
			m_popup.add(m_reportsAll).addActionListener(this);
		}
		
		if (documentCount == 0 && reportCount == 0 && allReports == 0)
			m_popup.add(Msg.getMsg(Env.getCtx(), "ArchivedNone"));
		//
		if (invoker.isShowing())
			m_popup.show(invoker, 0, invoker.getHeight());	//	below button
	}	//	getZoomTargets
	
	/**
	 * 	Listner
	 *	@param e event
	 */
	public void actionPerformed (ActionEvent e)
	{
		int AD_Form_ID = 118;	//	ArchiveViewer
		FormFrame ff = new FormFrame();
		ff.openForm(AD_Form_ID);
		ArchiveViewer av = (ArchiveViewer)ff.getFormPanel();
		//
		if (e.getSource() == m_documents)
			av.query(false, m_AD_Table_ID, m_Record_ID);
		else if (e.getSource() == m_reports)
			av.query(true, m_AD_Table_ID, m_Record_ID);
		else	//	all Reports
			av.query(true, m_AD_Table_ID, 0);
		//
		ff.pack();
		AEnv.showCenterScreen(ff);
		ff = null;
	}	//	actionPerformed

}	//	AArchive
