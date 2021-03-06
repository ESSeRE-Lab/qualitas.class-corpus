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
package org.compiere.acct;

import static org.compiere.common.constants.AcctViewerConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.compiere.apps.*;
import org.compiere.apps.search.*;
import org.compiere.common.constants.*;
import org.compiere.grid.ed.*;
import org.compiere.model.*;
import org.compiere.report.core.*;
import org.compiere.swing.*;
import org.compiere.util.*;
import org.compiere.vos.*;

/**
 *  Account Viewer
 *
 *  @author Jorg Janke
 *  @version  $Id: AcctViewer.java,v 1.3 2006/08/10 01:00:27 jjanke Exp $
 */
public class AcctViewer extends CFrame implements ActionListener, ChangeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *  Default constructor
	 */
	public AcctViewer()
	{
		this (0, 0, 0);
	}   //  AcctViewer

	/**
	 *  Detail Constructor
	 *
	 *  @param AD_Client_ID Client
	 *  @param AD_Table_ID Table
	 *  @param Record_ID Record
	 */
	public AcctViewer(int AD_Client_ID, int AD_Table_ID, int Record_ID)
	{
		super (Msg.getMsg(Env.getCtx(), "AcctViewer"));
		log.info("AD_Table_ID=" + AD_Table_ID + ", Record_ID=" + Record_ID);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		m_data = new AcctViewerData (Env.getCtx(), Env.createWindowNo(this), 
			AD_Client_ID, AD_Table_ID);
		//
		try
		{
			jbInit();
			dynInit (AD_Table_ID, Record_ID);
			AEnv.showCenterScreen(this);
		}
		catch(Exception e)
		{
			log.log(Level.SEVERE, "", e);
			dispose();
		}
	}   //  AcctViewer

	/** State Info          */
	private AcctViewerData	m_data = null;
	/** Image Icon			*/
	private ImageIcon 		m_iFind = new ImageIcon(org.compiere.Compiere.class.getResource("images/Find16.gif"));
	
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(AcctViewer.class);

	/** @todo Display Record Info & Zoom */

	//
	private CPanel mainPanel = new CPanel();
	private CTabbedPane tabbedPane = new CTabbedPane();
	private CPanel query = new CPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private CScrollPane result = new CScrollPane();
	private ResultTable table = new ResultTable();
	private CPanel southPanel = new CPanel();
	private CButton bQuery = new CButton();
	private CButton bPrint = new CButton();
	private CButton bCancel = new CButton();
	private CLabel statusLine = new CLabel();
	private BorderLayout southLayout = new BorderLayout();
	private BorderLayout queryLayout = new BorderLayout();
	private CPanel selectionPanel = new CPanel();
	private CPanel displayPanel = new CPanel();
	private TitledBorder displayBorder;
	private TitledBorder selectionBorder;
	private GridBagLayout displayLayout = new GridBagLayout();
	private CCheckBox displayQty = new CCheckBox();
	private CCheckBox displaySourceAmt = new CCheckBox();
//	private CPanel graphPanel = new CPanel();
	private CCheckBox displayDocumentInfo = new CCheckBox();
	private CLabel lSort = new CLabel();
	
	private CComboBox[] sortBy = new CComboBox[ AcctViewerData.s_numSortByFields ];
	private CCheckBox[] group = new CCheckBox[ AcctViewerData.s_numSortByFields ];
	private CLabel lGroup = new CLabel();
	private GridBagLayout selectionLayout = new GridBagLayout();
	private CComboBox selAcctSchema = new CComboBox();
	private CComboBox selPostingType = new CComboBox();
	private CCheckBox selDocument = new CCheckBox();
	private CComboBox selTable = new CComboBox();
	private CButton selRecord = new CButton();
	private CLabel lOrg = new CLabel();
	private CComboBox selOrg = new CComboBox();
	private CLabel lAcct = new CLabel();
	private CButton selAcct = new CButton();
	private CLabel lDate = new CLabel();
	private CLabel lacctSchema = new CLabel();
	private CLabel lpostingType = new CLabel();
	private VDate selDateFrom = new VDate("DateFrom", false, false, true, DisplayTypeConstants.Date, Msg.translate(Env.getCtx(), "DateFrom"));
	private VDate selDateTo = new VDate("DateTo", false, false, true, DisplayTypeConstants.Date, Msg.translate(Env.getCtx(), "DateTo"));
	private CLabel lsel1 = new CLabel();
	private CLabel lsel2 = new CLabel();
	private CLabel lsel3 = new CLabel();
	private CLabel lsel4 = new CLabel();
	private CLabel lsel5 = new CLabel();
	private CLabel lsel6 = new CLabel();
	private CLabel lsel7 = new CLabel();
	private CLabel lsel8 = new CLabel();
	private CButton sel1 = new CButton();
	private CButton sel2 = new CButton();
	private CButton sel3 = new CButton();
	private CButton sel4 = new CButton();
	private CButton sel5 = new CButton();
	private CButton sel6 = new CButton();
	private CButton sel7 = new CButton();
	private CButton sel8 = new CButton();
	//
	private CButton bRePost = new CButton();
	private CCheckBox forcePost = new CCheckBox();

	/**
	 *  Static Init.
	 *  <pre>
	 *  - mainPanel
	 *      - tabbedPane
	 *          - query
	 *          - result
	 *          - graphPanel
	 *  </pre>
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		ImageIcon ii = new ImageIcon(org.compiere.Compiere.class.getResource("images/InfoAccount16.gif"));
		setIconImage(ii.getImage());
		//
		mainLayout.setHgap(5);
		mainLayout.setVgap(5);
		mainPanel.setLayout(mainLayout);
		selectionPanel.setLayout(selectionLayout);
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		//  Selection
		selectionBorder = new TitledBorder(BorderFactory.createEtchedBorder(
			Color.white,new Color(148, 145, 140)), Msg.getMsg(Env.getCtx(),"Selection"));
		selectionPanel.setBorder(selectionBorder);
		lacctSchema.setLabelFor(selAcctSchema);
		lacctSchema.setText(Msg.translate(Env.getCtx(), ACCT_SCHEMA));
		lpostingType.setLabelFor(selPostingType);
		lpostingType.setText(Msg.translate(Env.getCtx(), POSTING_TYPE));
		selDocument.setText(Msg.getMsg(Env.getCtx(), SELECT_DOCUMENT ));
		selDocument.addActionListener(this);

		lOrg.setLabelFor(selOrg);
		lOrg.setText(Msg.translate(Env.getCtx(), ORG ));
		lAcct.setLabelFor(selAcct);
		lAcct.setText(Msg.translate(Env.getCtx(), ACCT ));
		lDate.setLabelFor(selDateFrom);
		lDate.setText(Msg.translate(Env.getCtx(), ACCT_DATE ));
		lsel1.setLabelFor(sel1);
		lsel2.setLabelFor(sel2);
		lsel3.setLabelFor(sel3);
		lsel4.setLabelFor(sel4);
		lsel5.setLabelFor(sel5);
		lsel6.setLabelFor(sel6);
		lsel7.setLabelFor(sel7);
		lsel8.setLabelFor(sel8);

		//  Display
		displayBorder = new TitledBorder(BorderFactory.createEtchedBorder(
			Color.white,new Color(148, 145, 140)), Msg.getMsg(Env.getCtx(),"Display"));
		displayPanel.setBorder(displayBorder);
		displayPanel.setLayout(displayLayout);
		displayQty.setText(Msg.getMsg(Env.getCtx(), "DisplayQty"));
		displaySourceAmt.setText(Msg.getMsg(Env.getCtx(), "DisplaySourceInfo"));
		displayDocumentInfo.setText(Msg.getMsg(Env.getCtx(), "DisplayDocumentInfo"));
		lSort.setText(Msg.getMsg(Env.getCtx(), "SortBy"));
		lGroup.setText(Msg.getMsg(Env.getCtx(), "GroupBy"));
		//
		displayPanel.add(displaySourceAmt,          new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		displayPanel.add(displayDocumentInfo,        new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		displayPanel.add(lSort,       new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		
		for( int i = 0; i < AcctViewerData.s_numSortByFields; ++i )
		{
			sortBy[i] = new CComboBox();
			displayPanel.add( sortBy[i], new GridBagConstraints( 0, 5 + i, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets( 5, 5, 0, 5 ), 0, 0 ) );

			group[i] = new CCheckBox();
			displayPanel.add( group[i], new GridBagConstraints( 1, 5 + i, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets( 5, 5, 0, 5 ), 0, 0 ) );
		}
		
		displayPanel.add(lGroup,       new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		displayPanel.add(displayQty,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		//
		selectionPanel.add(lacctSchema,         new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		selectionPanel.add(selAcctSchema,        new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
			
		selectionPanel.add(selDocument,           new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		selectionPanel.add(selTable,          new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 10, 5), 0, 0));
		selectionPanel.add(selRecord,         new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));

		selectionPanel.add(lpostingType,         new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(selPostingType,        new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(lDate,   new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(selDateFrom,     new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(selDateTo,   new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(lOrg,         new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
		selectionPanel.add(selOrg,         new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(lAcct,         new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(selAcct,         new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(lsel1,  new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(lsel2,     new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(lsel3,   new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(sel1,   new GridBagConstraints(1, 6, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(sel2,   new GridBagConstraints(1, 7, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(sel3,   new GridBagConstraints(1, 8, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(lsel4,  new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(sel4,  new GridBagConstraints(1, 9, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));

		selectionPanel.add(lsel5,  new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(sel5,  new GridBagConstraints(1, 10, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(lsel6,  new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(sel6,  new GridBagConstraints(1, 11, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(lsel7,  new GridBagConstraints(0, 12, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(sel7,  new GridBagConstraints(1, 12, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(lsel8,  new GridBagConstraints(0, 13, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		selectionPanel.add(sel8,  new GridBagConstraints(1, 13, 2, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
		//
		queryLayout.setHgap(5);
		queryLayout.setVgap(5);
		query.setLayout(queryLayout);
		query.add(selectionPanel,  BorderLayout.CENTER);
		query.add(displayPanel,  BorderLayout.EAST);
		//
		tabbedPane.add(query,        Msg.getMsg(Env.getCtx(), "ViewerQuery"));
		tabbedPane.add(result,       Msg.getMsg(Env.getCtx(), "ViewerResult"));
//		tabbedPane.add(graphPanel,   Msg.getMsg(Env.getCtx(), "ViewerGraph"));
		tabbedPane.addChangeListener(this);
		result.getViewport().add(table, null);
		//  South
		southLayout.setHgap(5);
		southLayout.setVgap(5);
		southPanel.setLayout(southLayout);
		statusLine.setForeground(Color.blue);
		statusLine.setBorder(BorderFactory.createLoweredBevelBorder());
		southPanel.add(statusLine, BorderLayout.CENTER);
		bRePost.setText(Msg.getMsg(Env.getCtx(), "RePost"));
		bRePost.setToolTipText(Msg.getMsg(Env.getCtx(), "RePostInfo"));
		bRePost.addActionListener(this);
		bRePost.setVisible(false);
		forcePost.setText(Msg.getMsg(Env.getCtx(), "Force"));
		forcePost.setToolTipText(Msg.getMsg(Env.getCtx(), "ForceInfo"));
		forcePost.setVisible(false);
		CPanel leftSide = new CPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		leftSide.add(bRePost);
		leftSide.add(forcePost);
		southPanel.add(leftSide, BorderLayout.WEST);
		//
		bQuery.setIcon(Env.getImageIcon("Refresh16.gif"));
		bQuery.setToolTipText(Msg.getMsg(Env.getCtx(), "Refresh"));
		bQuery.addActionListener(this);
		bPrint.setIcon(Env.getImageIcon("Print16.gif"));
		bPrint.setToolTipText(Msg.getMsg(Env.getCtx(), "Print"));
		bPrint.addActionListener(this);
		bCancel.setIcon(Env.getImageIcon("Cancel16.gif"));
		bCancel.setToolTipText(Msg.getMsg(Env.getCtx(), "Cancel"));
		bCancel.addActionListener(this);
		CPanel rightSide = new CPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
		rightSide.add(bPrint);
		rightSide.add(bQuery);
		rightSide.add(bCancel);
		southPanel.add(rightSide,  BorderLayout.EAST);
		this.getContentPane().add(southPanel, BorderLayout.SOUTH);
		//
	}   //  jbInit

	/**
	 *  Dynamic Init
	 *
	 *  @param AD_Table_ID table
	 *  @param Record_ID record
	 */
	private void dynInit (int AD_Table_ID, int Record_ID)
	{
		fillComboBox( selAcctSchema, m_data.getAcctSchema() );
		selAcctSchema.addActionListener(this);
		actionAcctSchema();
		//
		fillComboBox( selTable, m_data.getTable( Env.getCtx()) );
		selTable.addActionListener(this);
		selRecord.setIcon(m_iFind);
		selRecord.addActionListener(this);
		selRecord.setText("");
		//
		fillComboBox( selPostingType, m_data.getPostingType() );

		//  Mandatory Elements
		fillComboBox( selOrg, m_data.getOrg() );
		selAcct.setActionCommand( ACCT );
		selAcct.addActionListener(this);
		selAcct.setText("");
		selAcct.setIcon(m_iFind);

		//  Document Select
		boolean haveDoc = AD_Table_ID != 0 && Record_ID != 0;
		selDocument.setSelected (haveDoc);
		actionDocument();
		actionTable();
		statusLine.setText(" " + Msg.getMsg(Env.getCtx(), "ViewerOptions"));

		//  Initial Query
		if (haveDoc)
		{
			m_data.AD_Table_ID = AD_Table_ID;
			m_data.Record_ID = Record_ID;
			actionQuery();
		}
	}   //  dynInit

	/**
	 *  Dispose
	 */
	@Override
	public void dispose()
	{
		m_data.dispose();
		m_data = null;
		super.dispose();
	}   //  dispose;

	/**************************************************************************
	 *  Tab Changed
	 *  @param e ChangeEvent
	 */
	public void stateChanged(ChangeEvent e)
	{
	//	log.info( "AcctViewer.stateChanged");
		boolean visible = m_data.documentQuery && tabbedPane.getSelectedIndex() == 1;
		bRePost.setVisible(visible);
		if (Env.getCtx().getContext("#ShowAdvanced").equals("Y"))
			forcePost.setVisible(visible);
	}   //  stateChanged


	/**
	 *  Action Performed (Action Listener)
	 *  @param e ActionEvent
	 */
	public void actionPerformed(ActionEvent e)
	{
	//	log.info(e.getActionCommand());
		Object source = e.getSource();
		if (source == selAcctSchema)
			actionAcctSchema();
		else if (source == bQuery)
			actionQuery();
		else if (source == selDocument)
			actionDocument();
		else if (source == selTable)
			actionTable();
		else if (source == bRePost)
			actionRePost();
		else if  (source == bPrint)
			PrintScreenPainter.printScreen(this);
		//  InfoButtons
		else if  (source == bCancel)
			dispose();
		else if (source instanceof CButton)
			actionButton((CButton)source);
	}   //  actionPerformed

	/**
	 * 	New Acct Schema
	 */
	private void actionAcctSchema()
	{
		KeyNamePair kp = (KeyNamePair)selAcctSchema.getSelectedItem();
		if (kp == null)
			return;
		m_data.C_AcctSchema_ID = kp.getKey();
		m_data.ASchema = MAcctSchema.get(Env.getCtx(), m_data.C_AcctSchema_ID);
		log.info(m_data.ASchema.toString());
		//
		//  Sort Options
		for( int i = 0; i < AcctViewerData.s_numSortByFields; ++i )
		{
			sortBy[i].removeAllItems();
		}
		sortAddItem(new ValueNamePair("",""));
		sortAddItem(new ValueNamePair("DateAcct", Msg.translate(Env.getCtx(), "DateAcct")));
		sortAddItem(new ValueNamePair("DateTrx", Msg.translate(Env.getCtx(), "DateTrx")));
		sortAddItem(new ValueNamePair("C_Period_ID", Msg.translate(Env.getCtx(), "C_Period_ID")));
		//
		CLabel[] labels = new CLabel[] {lsel1, lsel2, lsel3, lsel4, lsel5, lsel6, lsel7, lsel8};
		CButton[] buttons = new CButton[] {sel1, sel2, sel3, sel4, sel5, sel6, sel7, sel8};
		int selectionIndex = 0;
		MAcctSchemaElement[] elements = m_data.ASchema.getAcctSchemaElements();
		for (int i = 0; i < elements.length && selectionIndex < labels.length; i++)
		{
			MAcctSchemaElement ase = elements[i];
			String columnName = ase.getColumnName();
			String displayColumnName = ase.getDisplayColumnName();
			//  Add Sort Option
			sortAddItem(new ValueNamePair(columnName, Msg.translate(Env.getCtx(), displayColumnName)));
			//  Additional Elements
			if (!ase.isElementType(X_C_AcctSchema_Element.ELEMENTTYPE_Organization) 
				&& !ase.isElementType(X_C_AcctSchema_Element.ELEMENTTYPE_Account))
			{
				labels[selectionIndex].setText(Msg.translate(Env.getCtx(), displayColumnName));
				labels[selectionIndex].setVisible(true);
				buttons[selectionIndex].setActionCommand(columnName);
				buttons[selectionIndex].addActionListener(this);
				buttons[selectionIndex].setIcon(m_iFind);
				buttons[selectionIndex].setText("");
				buttons[selectionIndex].setVisible(true);
				selectionIndex++;
			}
		}
		//	don't show remaining
		while (selectionIndex < labels.length)
		{
			labels[selectionIndex].setVisible(false);
			buttons[selectionIndex++].setVisible(false);
		}
	}	//	actionAcctSchema
	
	/**
	 * 	Add to Sort
	 *	@param vn name pair
	 */
	private void sortAddItem(ValueNamePair vn)
	{
		for( int i = 0; i < AcctViewerData.s_numSortByFields; ++i )
		{
			sortBy[i].addItem( vn );
		}
	}	//	sortAddItem

	/**
	 *  Query
	 */
	private void actionQuery()
	{
		//  Parameter Info
		StringBuffer para = new StringBuffer();
		//  Reset Selection Data
		m_data.C_AcctSchema_ID = 0;
		m_data.AD_Org_ID = 0;

		//  Save Selection Choices
		KeyNamePair kp = (KeyNamePair)selAcctSchema.getSelectedItem();
		if (kp != null)
			m_data.C_AcctSchema_ID = kp.getKey();
		para.append("C_AcctSchema_ID=").append(m_data.C_AcctSchema_ID);
		//
		ValueNamePair vp = (ValueNamePair)selPostingType.getSelectedItem();
		m_data.PostingType = vp.getValue();
		para.append(", PostingType=").append(m_data.PostingType);

		//  Document
		m_data.documentQuery = selDocument.isSelected();
		para.append(", DocumentQuery=").append(m_data.documentQuery);
		if (selDocument.isSelected())
		{
			if (m_data.AD_Table_ID == 0 || m_data.Record_ID == 0)
				return;
			para.append(", AD_Table_ID=").append(m_data.AD_Table_ID)
				.append(", Record_ID=").append(m_data.Record_ID);
		}
		else
		{
			m_data.DateFrom = (Timestamp)selDateFrom.getValue();
			para.append(", DateFrom=").append(m_data.DateFrom);
			m_data.DateTo = (Timestamp)selDateTo.getValue();
			para.append(", DateTo=").append(m_data.DateTo);
			kp = (KeyNamePair)selOrg.getSelectedItem();
			if (kp != null)
				m_data.AD_Org_ID = kp.getKey();
			para.append(", AD_Org_ID=").append(m_data.AD_Org_ID);
			//
			Iterator<String> it = m_data.whereInfo.values().iterator();
			while (it.hasNext())
				para.append(", ").append(it.next());
		}

		//  Save Display Choices
		m_data.displayQty = displayQty.isSelected();
		para.append(" - Display Qty=").append(m_data.displayQty);
		m_data.displaySourceAmt = displaySourceAmt.isSelected();
		para.append(", Source=").append(m_data.displaySourceAmt);
		m_data.displayDocumentInfo = displayDocumentInfo.isSelected();
		para.append(", Doc=").append(m_data.displayDocumentInfo);
		//
		
		para.append( " - Sorting: " );
		boolean first = true;
		for( int i = 0; i < AcctViewerData.s_numSortByFields; ++i )
		{
			m_data.sortBy[i] = ((ValueNamePair) sortBy[i].getSelectedItem()).getValue();
			m_data.group[i] = group[i].isSelected();
			if( !first )
				para.append( ", " );
			para.append( m_data.sortBy[i] ).append( "/" ).append( m_data.group[i] );
			first = false;
		}

		bQuery.setEnabled(false);
		statusLine.setText(" " + Msg.getMsg(Env.getCtx(), "Processing"));

		log.config(para.toString());
		Thread.yield();

		//  Switch to Result pane
		tabbedPane.setSelectedIndex(1);

		//  Set TableModel with Query
		table.setModel(m_data.query( Env.getCtx() ));

		bQuery.setEnabled(true);
		statusLine.setText(" " + Msg.getMsg(Env.getCtx(), "ViewerOptions"));
	}   //  actionQuery

	/**
	 *  Document selection
	 */
	private void actionDocument()
	{
		boolean doc = selDocument.isSelected();
		selTable.setEnabled(doc);
		selRecord.setEnabled(doc);
		//
		selDateFrom.setReadWrite(!doc);
		selDateTo.setReadWrite(!doc);
		selOrg.setEnabled(!doc);
		selAcct.setEnabled(!doc);
		sel1.setEnabled(!doc);
		sel2.setEnabled(!doc);
		sel3.setEnabled(!doc);
		sel4.setEnabled(!doc);
		sel5.setEnabled(!doc);
		sel6.setEnabled(!doc);
		sel7.setEnabled(!doc);
		sel8.setEnabled(!doc);
	}   //  actionDocument

	/**
	 *  Save Table selection (reset Record selection)
	 */
	private void actionTable()
	{
		ValueNamePair vp = (ValueNamePair)selTable.getSelectedItem();
		m_data.AD_Table_ID = (m_data.tableInfo.get(vp.getValue())).intValue();
		log.config(vp.getValue() + " = " + m_data.AD_Table_ID);
		//  Reset Record
		m_data.Record_ID = 0;
		selRecord.setText("");
		selRecord.setActionCommand(vp.getValue() + "_ID");
	}   //  actionTable

	/**
	 *  Action Button
	 *
	 *  @param button pressed button
	 *  @return ID
	 */
	private int actionButton(CButton button)
	{
		String keyColumn = button.getActionCommand();
		log.info(keyColumn);
		String whereClause = "IsSummary='N'";
		String lookupColumn = keyColumn;
		if (keyColumn.equals("Account_ID"))
		{
			lookupColumn = "C_ElementValue_ID";
			MAcctSchemaElement ase = m_data.ASchema
				.getAcctSchemaElement(X_C_AcctSchema_Element.ELEMENTTYPE_Account);
			if (ase != null)
				whereClause += " AND C_Element_ID=" + ase.getC_Element_ID();
		}
		else if (keyColumn.equals("User1_ID"))
		{
			lookupColumn = "C_ElementValue_ID";
			MAcctSchemaElement ase = m_data.ASchema
				.getAcctSchemaElement(X_C_AcctSchema_Element.ELEMENTTYPE_UserList1);
			if (ase != null)
				whereClause += " AND C_Element_ID=" + ase.getC_Element_ID();
		}
		else if (keyColumn.equals("User2_ID"))
		{
			lookupColumn = "C_ElementValue_ID";
			MAcctSchemaElement ase = m_data.ASchema
				.getAcctSchemaElement(X_C_AcctSchema_Element.ELEMENTTYPE_UserList2);
			if (ase != null)
				whereClause += " AND C_Element_ID=" + ase.getC_Element_ID();
		}
		else if (selDocument.isSelected())
			whereClause = "";
		String tableName = lookupColumn.substring(0, lookupColumn.length()-3);
		Info info = Info.create(this, true, m_data.WindowNo, tableName, lookupColumn, "", false, whereClause);
		if (!info.loadedOK())
		{
			info.dispose();
			info = null;
			button.setText("");
			m_data.whereInfo.put(keyColumn, "");
			return 0;
		}
		info.setVisible(true);
		String selectSQL = info.getSelectedSQL();       //  C_Project_ID=100 or ""
		Integer key = (Integer)info.getSelectedKey();
		info = null;
		if (selectSQL == null || selectSQL.length() == 0 || key == null)
		{
			button.setText("");
			m_data.whereInfo.put(keyColumn, "");    //  no query
			return 0;
		}

		//  Save for query
		log.config(keyColumn + " - " + key);
		if (button == selRecord)                            //  Record_ID
			m_data.Record_ID = key.intValue();
		else
			m_data.whereInfo.put(keyColumn, keyColumn + "=" + key.intValue());

		//  Display Selection and resize
		button.setText(m_data.getButtonText(tableName, lookupColumn, selectSQL));
		pack();
		return key.intValue();
	}   //  actionButton

	/**
	 *  RePost Record
	 */
	private void actionRePost()
	{
		if (m_data.documentQuery 
			&& m_data.AD_Table_ID != 0 && m_data.Record_ID != 0
			&& ADialog.ask(m_data.WindowNo, this, "PostImmediate?"))
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			boolean force = forcePost.isSelected();
			String error = AEnv.postImmediate ( Env.getCtx(), m_data.WindowNo, m_data.AD_Client_ID,
				m_data.AD_Table_ID, m_data.Record_ID, force);
			setCursor(Cursor.getDefaultCursor());
			if (error != null)
				ADialog.error(0, this, "PostingError-N", error);
			actionQuery();
		}
	}   //  actionRePost

	
	/**
	 * Fill a JComboBox using a list of NamePair objects
	 * @param cb the JComboBox to fill
	 * @param vo the ListBoxVO containing the data
	 */
	protected void fillComboBox( JComboBox cb, ListBoxVO vo )
	{
		for( Object p : vo.getOptions() )
		{
			cb.addItem( p );
		}
		if( vo.getDefaultKey() != null )
			cb.setSelectedItem( vo.getDefaultKey() );
	}
	
}   //  AcctViewer
