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

import javax.swing.*;
import javax.swing.border.*;

import org.compiere.swing.*;
import org.compiere.util.*;


/**
 *	POS Sales Rep Sub Panel
 *	
 *  @author Jorg Janke
 *  @version $Id: SubSalesRep.java,v 1.2 2006/07/30 00:51:26 jjanke Exp $
 */
public class SubSalesRep extends PosSubPanel implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public SubSalesRep (PosPanel posPanel)
	{
		super (posPanel);
	}	//	PosSubSalesRep
	
	private CLabel f_label = null;
	private CButton f_button = null;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(SubSalesRep.class);
	
	/**
	 * 	Initialize
	 */
	@Override
	public void init()
	{
		//	Title
		TitledBorder border = new TitledBorder(Msg.translate(Env.getCtx(), "C_POS_ID"));
		setBorder(border);
		
		//	Content
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = INSETS2;
		//	--
		f_label = new CLabel(p_pos.getName(), SwingConstants.LEADING);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		add (f_label, gbc);
		//
		f_button = new CButton (Msg.getMsg(Env.getCtx(), "Logout"));
		f_button.setActionCommand("LogOut");
		f_button.setFocusable(false);
		f_button.addActionListener(this);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		add (f_button, gbc);
	}	//	init
	
	/**
	 * 	Dispose - Free Resources
	 */
	@Override
	public void dispose()
	{
		super.dispose();
	}	//	dispose

	/**
	 * 	Action Listener
	 *	@param e event
	 */
	@Override
	public void actionPerformed (ActionEvent e)
	{
		String action = e.getActionCommand();
		if (action == null || action.length() == 0)
			return;
		log.info( "PosSubSalesRep - actionPerformed: " + action);
		//	Logout
		p_posPanel.dispose();
	}	//	actinPerformed
	
}	//	PosSubSalesRep
