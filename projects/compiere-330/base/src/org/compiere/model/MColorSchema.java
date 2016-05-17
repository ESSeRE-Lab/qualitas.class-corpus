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

import java.awt.*;
import java.math.*;
import java.sql.*;

import org.compiere.util.*;

/**
 * 	Performance Color Schema
 *	
 *  @author Jorg Janke
 *  @version $Id: MColorSchema.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class MColorSchema extends X_PA_ColorSchema
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Get Color
	 *	@param ctx context
	 *	@param PA_ColorSchema_ID id
	 *	@param target target value
	 *	@param actual actual value
	 *	@return color
	 */
	public static Color getColor (Ctx ctx, int PA_ColorSchema_ID, 
		BigDecimal target, BigDecimal actual)
	{
		int percent = 0;
		if (actual != null && actual.signum() != 0 
			&& target != null && target.signum() != 0)
		{
			BigDecimal pp = actual.multiply(Env.ONEHUNDRED)
				.divide(target, 0, BigDecimal.ROUND_HALF_UP);
			percent = pp.intValue();
		}
		return getColor(ctx, PA_ColorSchema_ID, percent);
	}	//	getColor

	/**
	 * 	Get Color
	 *	@param ctx context
	 *	@param PA_ColorSchema_ID id
	 *	@param percent percent
	 *	@return color
	 */
	public static Color getColor (Ctx ctx, int PA_ColorSchema_ID, int percent)
	{
		MColorSchema cs = get(ctx, PA_ColorSchema_ID);
		return cs.getColor(percent);
	}	//	getColor

	
	/**
	 * 	Get MColorSchema from Cache
	 *	@param ctx context
	 *	@param PA_ColorSchema_ID id
	 *	@return MColorSchema
	 */
	public static MColorSchema get (Ctx ctx, int PA_ColorSchema_ID)
	{
		if (PA_ColorSchema_ID == 0)
		{
			MColorSchema retValue = new MColorSchema(ctx, 0, null);
			retValue.setDefault();
			return retValue;
		}
		Integer key = Integer.valueOf (PA_ColorSchema_ID);
		MColorSchema retValue = s_cache.get (ctx, key);
		if (retValue != null)
			return retValue;
		retValue = new MColorSchema (ctx, PA_ColorSchema_ID, null);
		if (retValue.get_ID() != 0)
			s_cache.put (key, retValue);
		return retValue;
	}	//	get

	/**	Cache						*/
	private static final CCache<Integer, MColorSchema> s_cache 
		= new CCache<Integer, MColorSchema> ("PA_ColorSchema", 20);
	
	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param PA_ColorSchema_ID id
	 *	@param trx p_trx
	 */
	public MColorSchema (Ctx ctx, int PA_ColorSchema_ID, Trx trx)
	{
		super (ctx, PA_ColorSchema_ID, trx);
		if (PA_ColorSchema_ID == 0)
		{
		//	setName (null);
		//	setMark1Percent (50);
		//	setAD_PrintColor1_ID (102);		//	red
		//	setMark2Percent (100);
		//	setAD_PrintColor2_ID (113);		//	yellow
		}
	}	//	MColorSchema

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx p_trx
	 */
	public MColorSchema (Ctx ctx, ResultSet rs, Trx trx)
	{
		super (ctx, rs, trx);
	}	//	MColorSchema

	/**
	 * 	Set Default.
	 * 	Red (50) - Yellow (100) - Green
	 */
	public void setDefault()
	{
		setName("Default");
		setMark1Percent (50);
		setAD_PrintColor1_ID (102);		//	red
		setMark2Percent (100);
		setAD_PrintColor2_ID (113);		//	yellow
		setMark3Percent (9999);
		setAD_PrintColor3_ID (103);		//	green
	}	//	setDefault
	
	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	@Override
	protected boolean beforeSave (boolean newRecord)
	{
		if (getMark1Percent() > getMark2Percent())
			setMark1Percent(getMark2Percent());
		if (getMark2Percent() > getMark3Percent() && getMark3Percent() != 0)
			setMark2Percent(getMark3Percent());
		if (getMark3Percent() > getMark4Percent() && getMark4Percent() != 0)
			setMark4Percent(getMark4Percent());
		//
		return true;
	}	//	beforeSave
	
	/**
	 * 	Get Color
	 *	@param percent percent
	 *	@return color
	 */
	public Color getColor (int percent)
	{
		int AD_PrintColor_ID = 0;
		if (percent <= getMark1Percent() || getMark2Percent() == 0)
			AD_PrintColor_ID = getAD_PrintColor1_ID();
		else if (percent <= getMark2Percent() || getMark3Percent() == 0)
			AD_PrintColor_ID = getAD_PrintColor2_ID();
		else if (percent <= getMark3Percent() || getMark4Percent() == 0)
			AD_PrintColor_ID = getAD_PrintColor3_ID();
		else
			AD_PrintColor_ID = getAD_PrintColor4_ID();
		if (AD_PrintColor_ID == 0)
		{
			if (getAD_PrintColor3_ID() != 0)
				AD_PrintColor_ID = getAD_PrintColor3_ID();
			else if (getAD_PrintColor2_ID() != 0)
				AD_PrintColor_ID = getAD_PrintColor2_ID();
			else if (getAD_PrintColor1_ID() != 0)
				AD_PrintColor_ID = getAD_PrintColor1_ID();
		}
		if (AD_PrintColor_ID == 0)
			return Color.black;
		//
		MPrintColor pc = MPrintColor.get(getCtx(), AD_PrintColor_ID);
		if (pc != null)
			return pc.getColor();
		return Color.black;
	}	//	getColor

	/**
	 * 	String Representation
	 *	@return info
	 */
	@Override
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MColorSchema[");
		sb.append (get_ID()).append ("-").append (getName()).append ("]");
		return sb.toString ();
	}	//	toString
	
}	//	MColorSchema
