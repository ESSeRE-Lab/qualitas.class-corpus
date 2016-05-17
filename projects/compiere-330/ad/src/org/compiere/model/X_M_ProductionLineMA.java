/******************************************************************************
 * Product: Compiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2008 Compiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us at *
 * Compiere, Inc., 3600 Bridge Parkway #102, Redwood City, CA 94065, USA      *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

/** Generated Model - DO NOT CHANGE */
import java.sql.*;
import org.compiere.framework.*;
import org.compiere.util.*;
/** Generated Model for M_ProductionLineMA
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_ProductionLineMA extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_ProductionLineMA_ID id
    @param trx transaction
    */
    public X_M_ProductionLineMA (Ctx ctx, int M_ProductionLineMA_ID, Trx trx)
    {
        super (ctx, M_ProductionLineMA_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_ProductionLineMA_ID == 0)
        {
            setM_AttributeSetInstance_ID (0);
            setM_ProductionLine_ID (0);
            setMovementQty (Env.ZERO);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_ProductionLineMA (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=765 */
    public static final int Table_ID=765;
    
    /** TableName=M_ProductionLineMA */
    public static final String Table_Name="M_ProductionLineMA";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_ProductionLineMA");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Attribute Set Instance.
    @param M_AttributeSetInstance_ID Product Attribute Set Instance */
    public void setM_AttributeSetInstance_ID (int M_AttributeSetInstance_ID)
    {
        if (M_AttributeSetInstance_ID < 0) throw new IllegalArgumentException ("M_AttributeSetInstance_ID is mandatory.");
        set_ValueNoCheck ("M_AttributeSetInstance_ID", Integer.valueOf(M_AttributeSetInstance_ID));
        
    }
    
    /** Get Attribute Set Instance.
    @return Product Attribute Set Instance */
    public int getM_AttributeSetInstance_ID() 
    {
        return get_ValueAsInt("M_AttributeSetInstance_ID");
        
    }
    
    /** Set Production Line.
    @param M_ProductionLine_ID Document Line representing a production */
    public void setM_ProductionLine_ID (int M_ProductionLine_ID)
    {
        if (M_ProductionLine_ID < 1) throw new IllegalArgumentException ("M_ProductionLine_ID is mandatory.");
        set_ValueNoCheck ("M_ProductionLine_ID", Integer.valueOf(M_ProductionLine_ID));
        
    }
    
    /** Get Production Line.
    @return Document Line representing a production */
    public int getM_ProductionLine_ID() 
    {
        return get_ValueAsInt("M_ProductionLine_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getM_ProductionLine_ID()));
        
    }
    
    /** Set Movement Quantity.
    @param MovementQty Quantity of a product moved. */
    public void setMovementQty (java.math.BigDecimal MovementQty)
    {
        if (MovementQty == null) throw new IllegalArgumentException ("MovementQty is mandatory.");
        set_Value ("MovementQty", MovementQty);
        
    }
    
    /** Get Movement Quantity.
    @return Quantity of a product moved. */
    public java.math.BigDecimal getMovementQty() 
    {
        return get_ValueAsBigDecimal("MovementQty");
        
    }
    
    
}
