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

import javax.swing.border.*;

import org.compiere.swing.*;
import org.compiere.util.*;


/**
 *	Basic Key Sub Panel
 *	
 *  @author Jorg Janke
 *  @version $Id: SubBasicKeys.java,v 1.2 2006/07/30 00:51:27 jjanke Exp $
 */
public class SubBasicKeys extends PosSubPanel implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public SubBasicKeys (PosPanel posPanel)
	{
		super (posPanel);
	}	//	PosSubBasicKeys
	
	private CButton f_b1 = null;
	private CButton f_b2 = null;
	private CButton f_b3 = null;
	private CButton f_b4 = null;
	private CButton f_b5 = null;
	private CButton f_b6 = null;
	private CButton f_b7 = null;
	private CButton f_b8 = null;
	private CButton f_b9 = null;
	private CButton f_b0 = null;
	private CButton f_bDot = null;

	private CButton f_reset = null;
	private CButton f_new = null;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(SubBasicKeys.class);
	
	
	/**
	 * 	Initialize
	 */
	@Override
	public void init()
	{
		//	Title
		TitledBorder border = new TitledBorder("#");
		setBorder(border);
		
		//	Content
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = INSETS1;
		//
		f_b7 = createButton ("7");
		gbc.gridx = 0;
		gbc.gridy = 0;
		add (f_b7, gbc);
		//
		f_b8 = createButton ("8");
		gbc.gridx = 1;
		gbc.gridy = 0;
		add (f_b8, gbc);
		//
		f_b9 = createButton ("9");
		gbc.gridx = 2;
		gbc.gridy = 0;
		add (f_b9, gbc);
		//	--
		f_b4 = createButton ("4");
		gbc.gridx = 0;
		gbc.gridy = 1;
		add (f_b4, gbc);
		//
		f_b5 = createButton ("5");
		gbc.gridx = 1;
		gbc.gridy = 1;
		add (f_b5, gbc);
		//
		f_b6 = createButton ("6");
		gbc.gridx = 2;
		gbc.gridy = 1;
		add (f_b6, gbc);
		//	--
		f_b1 = createButton ("1");
		gbc.gridx = 0;
		gbc.gridy = 2;
		add (f_b1, gbc);
		//
		f_b2 = createButton ("2");
		gbc.gridx = 1;
		gbc.gridy = 2;
		add (f_b2, gbc);
		//
		f_b3 = createButton ("3");
		gbc.gridx = 2;
		gbc.gridy = 2;
		add (f_b3, gbc);
		//	--
		f_b0 = createButton ("0");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
		add (f_b0, gbc);
		//
		f_bDot = createButton (".");
		gbc.gridx = 2;
		gbc.gridy = 3;
        gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		add (f_bDot, gbc);
		
		//	--
		gbc.gridx = 4;
		gbc.insets = new Insets(1,15,1,1);
		gbc.gridy = 0;
		f_reset = createButtonAction("Reset", null);
		add (f_reset, gbc);
		//
		f_new = createButtonAction("New", null);
		gbc.gridy = 3;
		add (f_new, gbc);
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
		log.info( "PosSubBasicKeys - actionPerformed: " + action);
		//	Reset
		if (action.equals("Reset"))
			;
		//	New
		else if (action.equals("New"))
			p_posPanel.newOrder();
	}	//	actionPerformed
	
}	//	PosSubBasicKeys
