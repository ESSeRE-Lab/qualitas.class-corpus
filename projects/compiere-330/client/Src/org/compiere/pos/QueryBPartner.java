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
package org.compiere.pos;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.compiere.apps.*;
import org.compiere.minigrid.*;
import org.compiere.model.*;
import org.compiere.swing.*;
import org.compiere.util.*;

/**
 *	POS Query BPartner
 *	
 *  @author Jorg Janke
 *  @version $Id: QueryBPartner.java,v 1.2 2006/07/30 00:51:26 jjanke Exp $
 */
public class QueryBPartner extends PosSubPanel
	implements ActionListener, MouseListener, ListSelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Constructor
	 */
	public QueryBPartner (PosPanel posPanel)
	{
		super(posPanel);
	}	//	PosQueryBPartner

	/** The Table					*/
	private MiniTable		m_table;
	
	private CPanel 			northPanel;
	private CScrollPane 	centerScroll;
	private ConfirmPanel	confirm;
	
	private CTextField		f_value;
	private CTextField		f_name;
	private CTextField		f_contact;
	private CTextField		f_email;
	private CTextField		f_phone;
	private CTextField		f_city;

	private CButton			f_up;
	private CButton			f_down;

	private int				m_C_BPartner_ID;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(QueryBPartner.class);
	
	
	/**	Table Column Layout Info			*/
	private static ColumnInfo[] s_layout = new ColumnInfo[] 
	{
		new ColumnInfo(" ", "C_BPartner_ID", IDColumn.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "Value"), "Value", String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "Name"), "Name", String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "ContactName"), "ContactName", String.class), 
		new ColumnInfo(Msg.translate(Env.getCtx(), "EMail"), "EMail", String.class), 
		new ColumnInfo(Msg.translate(Env.getCtx(), "Phone"), "Phone", String.class), 
		new ColumnInfo(Msg.translate(Env.getCtx(), "Postal"), "Postal", String.class), 
		new ColumnInfo(Msg.translate(Env.getCtx(), "City"), "City", String.class) 
	};
	/**	From Clause							*/
	private static String s_sqlFrom = "RV_BPartner";
	/** Where Clause						*/
	private static String s_sqlWhere = "IsActive='Y'"; 

	/**
	 * 	Set up Panel
	 */
	@Override
	protected void init()
	{
		setLayout(new BorderLayout(5,5));
        
		//	North
		northPanel = new CPanel(new GridBagLayout());
		add (northPanel, BorderLayout.NORTH);
		northPanel.setBorder(new TitledBorder(Msg.getMsg(p_ctx, "Query")));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = PosSubPanel.INSETS2;
		//
		gbc.gridy = 0;
		gbc.gridx = GridBagConstraints.RELATIVE;
		CLabel lvalue = new CLabel(Msg.translate(p_ctx, "Value"));
		gbc.anchor = GridBagConstraints.EAST;
		northPanel.add (lvalue, gbc);
		f_value = new CTextField(10);
		lvalue.setLabelFor(f_value);
		gbc.anchor = GridBagConstraints.WEST;
		northPanel.add(f_value, gbc);
		f_value.addActionListener(this);
		//
		CLabel lcontact = new CLabel(Msg.translate(p_ctx, "Contact"));
		gbc.anchor = GridBagConstraints.EAST;
		northPanel.add (lcontact, gbc);
		f_contact = new CTextField(10);
		lcontact.setLabelFor(f_contact);
		gbc.anchor = GridBagConstraints.WEST;
		northPanel.add(f_contact, gbc);
		f_contact.addActionListener(this);
		//
		CLabel lphone = new CLabel(Msg.translate(p_ctx, "Phone"));
		gbc.anchor = GridBagConstraints.EAST;
		northPanel.add (lphone, gbc);
		f_phone = new CTextField(10);
		lphone.setLabelFor(f_phone);
		gbc.anchor = GridBagConstraints.WEST;
		northPanel.add(f_phone, gbc);
		f_phone.addActionListener(this);
		//
		gbc.gridy = 1;
		CLabel lname = new CLabel(Msg.translate(p_ctx, "Name"));
		gbc.anchor = GridBagConstraints.EAST;
		northPanel.add (lname, gbc);
		f_name = new CTextField(10);
		lname.setLabelFor(f_name);
		gbc.anchor = GridBagConstraints.WEST;
		northPanel.add(f_name, gbc);
		f_name.addActionListener(this);
		//
		CLabel lemail = new CLabel(Msg.translate(p_ctx, "Email"));
		gbc.anchor = GridBagConstraints.EAST;
		northPanel.add (lemail, gbc);
		f_email = new CTextField(10);
		lemail.setLabelFor(f_email);
		gbc.anchor = GridBagConstraints.WEST;
		northPanel.add(f_email, gbc);
		f_email.addActionListener(this);
		//
		CLabel lcity = new CLabel(Msg.translate(p_ctx, "City"));
		gbc.anchor = GridBagConstraints.EAST;
		northPanel.add (lcity, gbc);
		f_city = new CTextField(10);
		lcity.setLabelFor(f_city);
		gbc.anchor = GridBagConstraints.WEST;
		northPanel.add(f_city, gbc);
		f_city.addActionListener(this);
		//
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = .1;
		f_up = createButtonAction("Previous", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
		northPanel.add(f_up, gbc);
		gbc.weightx = 0;
		f_down = createButtonAction("Next", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		northPanel.add(f_down, gbc);
		
		//	Confirm
		confirm = new ConfirmPanel (true, true, true, false, false, false, false);
		add (confirm, BorderLayout.SOUTH);
		confirm.addActionListener(this);

		//	Center
		m_table = new MiniTable();
		m_table.prepareTable (s_layout, s_sqlFrom, s_sqlWhere, false, "RV_BPartner");
        Dimension size = m_table.getPreferredScrollableViewportSize();
        size.height = m_table.getRowHeight();
        m_table.setPreferredScrollableViewportSize(size);
		m_table.setRowSelectionAllowed(true);
		m_table.setColumnSelectionAllowed(false);
		m_table.setMultiSelection(false);
		m_table.addMouseListener(this);
		m_table.getSelectionModel().addListSelectionListener(this);
		enableButtons();
		centerScroll = new CScrollPane(m_table);
		add (centerScroll, BorderLayout.CENTER);
	}	//	init
	
	/**
	 * 	Dispose
	 */
	@Override
	public void dispose()
	{
		removeAll();
		northPanel = null;
		centerScroll = null;
		confirm = null;
		m_table = null;
	}	//	dispose
	
	/**
	 * 	Action Listener
	 *	@param e event
	 */
	@Override
	public void actionPerformed (ActionEvent e)
	{
		log.info(e.getActionCommand());
		if ("Refresh".equals(e.getActionCommand())
			|| e.getSource() == f_value // || e.getSource() == f_upc
			|| e.getSource() == f_name // || e.getSource() == f_sku
			)
		{
			ArrayList<MBPartnerInfo> bpartners = MBPartnerInfo.findAll (p_ctx,
				f_value.getText(), f_name.getText(), 
				f_contact.getText(), f_email.getText(),
				f_phone.getText(), f_city.getText());
			MBPartnerInfo[] bps = new MBPartnerInfo[bpartners.size()];
			bpartners.toArray(bps);
			setResults(bps);
			return;
		}
		else if ("Reset".equals(e.getActionCommand()))
		{
			f_value.setText(null);
			f_name.setText(null);
			f_contact.setText(null);
			f_email.setText(null);
			f_phone.setText(null);
			f_city.setText(null);
			setResults(new MBPartnerInfo[0]);
			return;
		}
		else if ("Previous".equalsIgnoreCase(e.getActionCommand()))
		{
			int rows = m_table.getRowCount();
			if (rows == 0)
				return;
			int row = m_table.getSelectedRow();
			row--;
			if (row < 0)
				row = 0;
			m_table.getSelectionModel().setSelectionInterval(row, row);
			return;
		}
		else if ("Next".equalsIgnoreCase(e.getActionCommand()))
		{
			int rows = m_table.getRowCount();
			if (rows == 0)
				return;
			int row = m_table.getSelectedRow();
			row++;
			if (row >= rows)
				row = rows - 1;
			m_table.getSelectionModel().setSelectionInterval(row, row);
			return;
		}
		//	Exit
		close();
	}	//	actionPerformed
	
	
	/**
	 * 	Set/display Results
	 *	@param results results
	 */
	public void setResults (MBPartnerInfo[] results)
	{
		m_table.loadTable(results);
		enableButtons();
	}	//	setResults
	
	/**
	 * 	Table selection changed
	 *	@param e event
	 */
	public void valueChanged (ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting())
			return;
		enableButtons();
	}	//	valueChanged

	/**
	 * 	Enable/Set Buttons and set ID
	 */
	private void enableButtons()
	{
		m_C_BPartner_ID = -1;
		int row = m_table.getSelectedRow();
		boolean enabled = row != -1;
		if (enabled)
		{
			Integer ID = m_table.getSelectedRowKey();
			if (ID != null)
			{
				m_C_BPartner_ID = ID.intValue();
			//	m_BPartnerName = (String)m_table.getValueAt(row, 2);
			//	m_Price = (BigDecimal)m_table.getValueAt(row, 7);
			}
		}
		confirm.getOKButton().setEnabled(enabled);
		log.fine("C_BPartner_ID=" + m_C_BPartner_ID); 
	}	//	enableButtons

	/**
	 *  Mouse Clicked
	 *  @param e event
	 */
	public void mouseClicked(MouseEvent e)
	{
		//  Double click with selected row => exit
		if (e.getClickCount() > 1 && m_table.getSelectedRow() != -1)
		{
			enableButtons();
			close();
		}
	}   //  mouseClicked

	public void mouseEntered (MouseEvent e) {}
	public void mouseExited (MouseEvent e) {}
	public void mousePressed (MouseEvent e) {}
	public void mouseReleased (MouseEvent e) {}

	/**
	 * 	Close.
	 * 	Set Values on other panels and close
	 */
	private void close()
	{
		log.fine("C_BPartner_ID=" + m_C_BPartner_ID); 
		
		if (m_C_BPartner_ID > 0)
		{
			p_posPanel.f_bpartner.setC_BPartner_ID(m_C_BPartner_ID);
		//	p_posPanel.f_curLine.setCurrency(m_Price);
		}
		else
		{
			p_posPanel.f_bpartner.setC_BPartner_ID(0);
		//	p_posPanel.f_curLine.setPrice(Env.ZERO);
		}
		p_posPanel.closeQuery();
	}	//	close
	
}	//	PosQueryBPartner
