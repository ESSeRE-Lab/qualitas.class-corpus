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
package org.compiere.apps.form;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.apps.*;
import org.compiere.grid.ed.*;
import org.compiere.swing.*;
import org.compiere.util.*;

/**
 *	Process SQL Commands
 *	
 *  @author Jorg Janke
 *  @version $Id: VSQLProcess.java,v 1.2 2006/07/30 00:51:28 jjanke Exp $
 */
public class VSQLProcess extends CPanel
	implements FormPanel, ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *	Initialize Panel
	 *  @param WindowNo window
	 *  @param frame frame
	 */
	public void init (int WindowNo, FormFrame frame)
	{
		log.info("VSQLProcess.init");
		m_frame = frame;
		try
		{
			jbInit();
			frame.getContentPane().add(this, BorderLayout.CENTER);
		//	frame.getContentPane().add(confirmPanel, BorderLayout.SOUTH);
		}
		catch(Exception e)
		{
			log.log(Level.SEVERE, "", e);
		}
	}	//	init

	/**
	 * 	Dispose - Free Resources
	 */
	public void dispose()
	{
		if (m_frame != null)
			m_frame.dispose();
		m_frame = null;
	}	//	dispose

	/**	FormFrame			*/
	private FormFrame 		m_frame;

	/**	Log					*/
	private static CLogger	log = CLogger.getCLogger(VSQLProcess.class);

	/** DML Statement		*/
	private static final String[] DML_KEYWORDS = new String[]{
		"SELECT", "UPDATE", "DELETE", "TRUNCATE"
	};

	private BorderLayout mainLayout = new BorderLayout();
	private CPanel northPanel = new CPanel();
	private CLabel sqlLabel = new CLabel("SQL");
	private VText sqlField = new VText("SQL", false, false, true, 3000, 9000);
	private CPanel centerPanel = new CPanel();
	private BorderLayout centerLayout = new BorderLayout();
	private BorderLayout northLayout = new BorderLayout();
	private CTextArea resultField = new CTextArea(20,60);
	private CButton sqlButton = ConfirmPanel.createProcessButton(true);

	/**
	 * 	Static Init
	 *	@throws Exception
	 */
	void jbInit() throws Exception
	{
		this.setLayout(mainLayout);
		mainLayout.setHgap(5);
		mainLayout.setVgap(5);
		//
		this.add(northPanel, BorderLayout.NORTH);
		northLayout.setHgap(5);
		northLayout.setVgap(5);
		northPanel.setLayout(northLayout);
		sqlLabel.setText("SQL");
		northPanel.add(sqlLabel, BorderLayout.WEST);
		//	
		northPanel.add(sqlField, BorderLayout.CENTER);
		sqlButton.addActionListener(this);	
		northPanel.add(sqlButton, BorderLayout.EAST);
		//
		this.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(centerLayout);
		centerLayout.setHgap(0);
		resultField.setReadWrite(false);		
		centerPanel.add(resultField, BorderLayout.CENTER);
	}	//	jbInit

	/**
	 *	Action Listener
	 *	@param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		resultField.setText(processStatements (sqlField.getText(), false));
	}	//	actionedPerformed
	
	/**
	 * 	Process SQL Statements
	 *	@param sqlStatements one or more statements separated by ;
	 *	@param allowDML allow DML statements
	 *	@return result
	 */
	public static String processStatements (String sqlStatements, boolean allowDML)
	{
		if (sqlStatements == null || sqlStatements.length() == 0)
			return "";
		StringBuffer result = new StringBuffer();
		//
		StringTokenizer st = new StringTokenizer(sqlStatements, ";", false);
		while (st.hasMoreTokens())
		{
			result.append(processStatement(st.nextToken(), allowDML));
			result.append(Env.NL);
		}
		//
		return result.toString();
	}	//	processStatements
	
	/**
	 * 	Process SQL Statements
	 *	@param sqlStatement one statement
	 *	@param allowDML allow DML statements
	 *	@return result
	 */
	public static String processStatement (String sqlStatement, boolean allowDML)
	{
		if (sqlStatement == null)
			return "";
		StringBuffer sb = new StringBuffer();
		char[] chars = sqlStatement.toCharArray();
		for (char c : chars) {
			if (Character.isWhitespace(c))
				sb.append(' ');
			else
				sb.append(c);
		}
		String sql = sb.toString().trim();
		if (sql.length() == 0)
			return "";
		//
		StringBuffer result = new StringBuffer("SQL> ")
			.append(sql)
			.append(Env.NL);
		if (!allowDML)
		{
			boolean error = false;
			String SQL = sql.toUpperCase();
			boolean isCreateView = SQL.startsWith("CREATE ") 
				&& SQL.indexOf (" VIEW ") != -1 
				&& SQL.indexOf (" AS SELECT ") != -1;
			if (!isCreateView)
			{
				for (String element : DML_KEYWORDS) {
					if (SQL.startsWith(element + " ") 
						|| SQL.indexOf(" " + element + " ") != -1
						|| SQL.indexOf("(" + element + " ") != -1)
					{
						result.append("===> ERROR: Not Allowed Keyword ")
							.append(element)
							.append(Env.NL);
						error = true;	
					}
				}
			}
			if (error)
				return result.toString();
		}	//	!allowDML
		
		//	Process
		Connection conn = DB.createConnection(true, Connection.TRANSACTION_READ_COMMITTED);
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement(); 
			stmt.execute(sql);
			int count = stmt.getUpdateCount();
			if (count == -1)
			{
				result.append("---> ResultSet");
			}
			else
				result.append("---> Result=").append(count);
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, sql + " - " + e.toString());
			result.append("===> ").append(e.toString());
		}
		
		//	Clean up
		try
		{
			stmt.close();
		}
		catch (SQLException e1)
		{
			log.log(Level.SEVERE, "close statement", e1);
		}
		stmt = null;
		try
		{
			conn.close();
		}
		catch (SQLException e2)
		{
			log.log(Level.SEVERE, "close connection", e2);
		}
		conn = null;
		//
		result.append(Env.NL);
		return result.toString();
	}	//	processStatement
	
}	//	VSQLProcess
