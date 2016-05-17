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
package org.compiere.model;

import java.math.*;
import java.sql.*;

import org.compiere.util.*;


/**
 *	Discount Break Schema (Model)
 *	
 *  @author Jorg Janke
 *  @version $Id: MDiscountSchemaBreak.java,v 1.3 2006/07/30 00:51:05 jjanke Exp $
 */
public class MDiscountSchemaBreak extends X_M_DiscountSchemaBreak
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param M_DiscountSchemaBreak_ID id
	 *	@param trx transaction
	 */
	public MDiscountSchemaBreak (Ctx ctx, int M_DiscountSchemaBreak_ID, Trx trx)
	{
		super (ctx, M_DiscountSchemaBreak_ID, trx);
	}	//	MDiscountSchemaBreak

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MDiscountSchemaBreak (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MDiscountSchemaBreak

	
	/**
	 * 	Criteria apply
	 *	@param Value amt or qty
	 *	@param M_Product_ID product
	 *	@param M_Product_Category_ID category
	 *	@return true if criteria met
	 */
	public boolean applies (BigDecimal Value, int M_Product_ID, int M_Product_Category_ID)
	{
		if (!isActive())
			return false;
		
		//	below break value
		if (Value.compareTo(getBreakValue()) < 0)
			return false;
		
		//	No Product / Category 
		if (getM_Product_ID() == 0 
			&& getM_Product_Category_ID() == 0)
			return true;
		
		//	Product
		if (getM_Product_ID() == M_Product_ID)
			return true;
		
		//	Category
		if (M_Product_Category_ID != 0)
			return getM_Product_Category_ID() == M_Product_Category_ID;

		//	Look up Category of Product
		return MProductCategory.isCategory(getM_Product_Category_ID(), M_Product_ID);
	}	//	applies
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	@Override
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MDiscountSchemaBreak[");
		sb.append(get_ID()).append("-Seq=").append(getSeqNo());
		if (getM_Product_Category_ID() != 0)
			sb.append(",M_Product_Category_ID=").append(getM_Product_Category_ID());
		if (getM_Product_ID() != 0)
			sb.append(",M_Product_ID=").append(getM_Product_ID());
		sb.append(",Break=").append(getBreakValue());
		if (isBPartnerFlatDiscount())
			sb.append(",FlatDiscount");
		else
			sb.append(",Discount=").append(getBreakDiscount());
		sb.append ("]");
		return sb.toString ();
	}	//	toString
	
}	//	MDiscountSchemaBreak
