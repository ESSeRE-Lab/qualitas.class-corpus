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

import org.compiere.model.*;
import org.compiere.swing.*;
import org.compiere.util.*;


/**
 *	POS Product Sub Panel.
 *	Responsible for Product Selection and maintaining
 *	M_Product_ID, Name, UOM
 *	and setting Price
 *	
 *  @author Jorg Janke
 *  @version $Id: SubProduct.java,v 1.2 2006/07/30 00:51:27 jjanke Exp $
 */
public class SubProduct extends PosSubPanel 
	implements ActionListener, FocusListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public SubProduct (PosPanel posPanel)
	{
		super (posPanel);
	}	//	PosSubProduct
	
	protected CTextField	f_name;
	private CButton			f_bSearch;
	
	/**	The Product					*/
	private MProduct		m_product = null; 
	/** Warehouse					*/
	private int 			m_M_Warehouse_ID;
	/** PLV							*/
	private int 			m_M_PriceList_Version_ID;

    /** Product Query Window        */
    protected QueryProduct      f_queryProduct = new QueryProduct (p_posPanel);
    
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(SubProduct.class);
	
	/**
	 * 	Initialize
	 */
	@Override
	public void init()
	{
		//	Title
		TitledBorder border = new TitledBorder(Msg.translate(p_ctx, "M_Product_ID"));
		setBorder(border);
		
		//	Content
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = INSETS2;
		//	--
		f_name = new CTextField("");
		f_name.setName("Name");
		f_name.addActionListener(this);
		f_name.addFocusListener(this);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.1;
		add (f_name, gbc);
		//
		f_bSearch = createButtonAction ("Product", KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK));
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		add (f_bSearch, gbc);
	}	//	init
	
	/**
	 * 	Dispose - Free Resources
	 */
	@Override
	public void dispose()
	{
		if (f_name != null)
			f_name.removeFocusListener(this);
		removeAll();
		super.dispose();
	}	//	dispose


	/**************************************************************************
	 * 	Action Listener
	 *	@param e event
	 */
	@Override
	public void actionPerformed (ActionEvent e)
	{
		String action = e.getActionCommand();
		if (action == null || action.length() == 0)
			return;
		log.info( "PosSubProduct - actionPerformed: " + action);
		//	Product
		if (action.equals("Product"))
		{
			setParameter();
			p_posPanel.openQuery(f_queryProduct);
		}
		//	Name
		else if (e.getSource() == f_name)
			findProduct();
	}	//	actionPerformed
	
	/**
	 * 	Focus Gained
	 *	@param e
	 */
	public void focusGained (FocusEvent e)
	{
	}	//	focusGained

	/**
	 * 	Focus Lost
	 *	@param e
	 */
	public void focusLost (FocusEvent e)
	{
		if (e.isTemporary())
			return;
		log.info( "PosSubProduct - focusLost");
		findProduct();
	}	//	focusLost

	/**
	 * 	Set Query Paramter
	 */
	private void setParameter()
	{
		//	What PriceList ?
		m_M_Warehouse_ID = p_pos.getM_Warehouse_ID();
		m_M_PriceList_Version_ID = p_posPanel.f_bpartner.getM_PriceList_Version_ID();
		f_queryProduct.setQueryData(m_M_PriceList_Version_ID, m_M_Warehouse_ID);
	}	//	setParameter
	
	
	/**************************************************************************
	 * 	Find/Set Product & Price
	 */
	private void findProduct()
	{
		String query = f_name.getText();
		if (query == null || query.length() == 0)
			return;
		query = query.toUpperCase();
		//	Test Number
		boolean allNumber = true;
		try
		{
			Integer.parseInt(query);
		}
		catch (Exception e)
		{
			allNumber = false;
		}
		String Value = query;
		String Name = query;
		String UPC = (allNumber ? query : null);
		String SKU = (allNumber ? query : null);
		
		MWarehousePrice[] results = null;
		setParameter();
		//
		results = MWarehousePrice.find (p_ctx,
			m_M_PriceList_Version_ID, m_M_Warehouse_ID,
			Value, Name, UPC, SKU, null);
		
		//	Set Result
		if (results.length == 0)
		{
			setM_Product_ID(0);
			p_posPanel.f_currentLine.setPrice(Env.ZERO);
		}
		else if (results.length == 1)
		{
			setM_Product_ID(results[0].getM_Product_ID());
			f_name.setText(results[0].getName());
			p_posPanel.f_currentLine.setPrice(results[0].getPriceStd());
		}
		else	//	more than one
		{
			f_queryProduct.setResults (results);
			p_posPanel.openQuery(f_queryProduct);
		}
	}	//	findProduct

	/**
	 * 	Set Price for defined product 
	 */
	public void setPrice()
	{
		if (m_product == null)
			return;
		//
		setParameter();
		MWarehousePrice result = MWarehousePrice.get (m_product,
			m_M_PriceList_Version_ID, m_M_Warehouse_ID, null);
		if (result != null)
			p_posPanel.f_currentLine.setPrice(result.getPriceStd());
	}	//	setPrice
	
	/**************************************************************************
	 * 	Set Product
	 *	@param M_Product_ID id
	 */
	public void setM_Product_ID (int M_Product_ID)
	{
		log.fine( "PosSubProduct.setM_Product_ID=" + M_Product_ID);
		if (M_Product_ID <= 0)
			m_product = null;
		else
		{
			m_product = MProduct.get(p_ctx, M_Product_ID);
			if (m_product.get_ID() == 0)
				m_product = null;
		}
		//	Set String Info
		if (m_product != null)
		{
			f_name.setText(m_product.getName());
			f_name.setToolTipText(m_product.getDescription());
			p_posPanel.f_currentLine.setUOM(m_product.getUOMSymbol());
		}
		else
		{
			f_name.setText(null);
			f_name.setToolTipText(null);
			p_posPanel.f_currentLine.setUOM(null);
		}
	}	//	setM_Product_ID
	
	/**
	 * 	Get Product
	 *	@return M_Product_ID
	 */
	public int getM_Product_ID ()
	{
		if (m_product != null)
			return m_product.getM_Product_ID();
		return 0;
	}	//	getM_Product_ID
	
	/**
	 * 	Get UOM
	 *	@return C_UOM_ID
	 */
	public int getC_UOM_ID ()
	{
		if (m_product != null)
			return m_product.getC_UOM_ID();
		return 0;
	}	//	getC_UOM_ID

	/**
	 * 	Get Product Name
	 *	@return name of product
	 */
	public String getProductName()
	{
		if (m_product != null)
			return m_product.getName();
		return "";
	}	//	getProductName
	
	/**
	 * 	Get Product
	 *	@return product
	 */
	public MProduct getProduct()
	{
		return m_product;
	}	//	getProduct
	
}	//	PosSubProduct
