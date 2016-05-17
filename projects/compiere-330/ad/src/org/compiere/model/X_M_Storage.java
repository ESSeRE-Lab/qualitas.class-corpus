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
/** Generated Model for M_Storage
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_Storage extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_Storage_ID id
    @param trx transaction
    */
    public X_M_Storage (Ctx ctx, int M_Storage_ID, Trx trx)
    {
        super (ctx, M_Storage_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_Storage_ID == 0)
        {
            setM_AttributeSetInstance_ID (0);
            setM_Locator_ID (0);
            setM_Product_ID (0);
            setQtyAllocated (Env.ZERO);	// 0
            setQtyDedicated (Env.ZERO);	// 0
            setQtyExpected (Env.ZERO);	// 0
            setQtyOnHand (Env.ZERO);
            setQtyOrdered (Env.ZERO);
            setQtyReserved (Env.ZERO);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_Storage (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495801043789L;
    /** Last Updated Timestamp 2008-06-16 21:08:47.0 */
    public static final long updatedMS = 1213675727000L;
    /** AD_Table_ID=250 */
    public static final int Table_ID=250;
    
    /** TableName=M_Storage */
    public static final String Table_Name="M_Storage";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_Storage");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Date last inventory count.
    @param DateLastInventory Date of Last Inventory Count */
    public void setDateLastInventory (Timestamp DateLastInventory)
    {
        set_Value ("DateLastInventory", DateLastInventory);
        
    }
    
    /** Get Date last inventory count.
    @return Date of Last Inventory Count */
    public Timestamp getDateLastInventory() 
    {
        return (Timestamp)get_Value("DateLastInventory");
        
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
    
    /** Set Locator.
    @param M_Locator_ID Warehouse Locator */
    public void setM_Locator_ID (int M_Locator_ID)
    {
        if (M_Locator_ID < 1) throw new IllegalArgumentException ("M_Locator_ID is mandatory.");
        set_ValueNoCheck ("M_Locator_ID", Integer.valueOf(M_Locator_ID));
        
    }
    
    /** Get Locator.
    @return Warehouse Locator */
    public int getM_Locator_ID() 
    {
        return get_ValueAsInt("M_Locator_ID");
        
    }
    
    /** Set Product.
    @param M_Product_ID Product, Service, Item */
    public void setM_Product_ID (int M_Product_ID)
    {
        if (M_Product_ID < 1) throw new IllegalArgumentException ("M_Product_ID is mandatory.");
        set_ValueNoCheck ("M_Product_ID", Integer.valueOf(M_Product_ID));
        
    }
    
    /** Get Product.
    @return Product, Service, Item */
    public int getM_Product_ID() 
    {
        return get_ValueAsInt("M_Product_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getM_Product_ID()));
        
    }
    
    /** Set Quantity Allocated.
    @param QtyAllocated Quantity that has been picked and is awaiting shipment */
    public void setQtyAllocated (java.math.BigDecimal QtyAllocated)
    {
        if (QtyAllocated == null) throw new IllegalArgumentException ("QtyAllocated is mandatory.");
        set_ValueNoCheck ("QtyAllocated", QtyAllocated);
        
    }
    
    /** Get Quantity Allocated.
    @return Quantity that has been picked and is awaiting shipment */
    public java.math.BigDecimal getQtyAllocated() 
    {
        return get_ValueAsBigDecimal("QtyAllocated");
        
    }
    
    /** Set Quantity Dedicated.
    @param QtyDedicated Quantity for which there is a pending Warehouse Task */
    public void setQtyDedicated (java.math.BigDecimal QtyDedicated)
    {
        if (QtyDedicated == null) throw new IllegalArgumentException ("QtyDedicated is mandatory.");
        set_ValueNoCheck ("QtyDedicated", QtyDedicated);
        
    }
    
    /** Get Quantity Dedicated.
    @return Quantity for which there is a pending Warehouse Task */
    public java.math.BigDecimal getQtyDedicated() 
    {
        return get_ValueAsBigDecimal("QtyDedicated");
        
    }
    
    /** Set Expected Quantity.
    @param QtyExpected Quantity expected to be received into a locator */
    public void setQtyExpected (java.math.BigDecimal QtyExpected)
    {
        if (QtyExpected == null) throw new IllegalArgumentException ("QtyExpected is mandatory.");
        set_ValueNoCheck ("QtyExpected", QtyExpected);
        
    }
    
    /** Get Expected Quantity.
    @return Quantity expected to be received into a locator */
    public java.math.BigDecimal getQtyExpected() 
    {
        return get_ValueAsBigDecimal("QtyExpected");
        
    }
    
    /** Set On Hand Quantity.
    @param QtyOnHand On Hand Quantity */
    public void setQtyOnHand (java.math.BigDecimal QtyOnHand)
    {
        if (QtyOnHand == null) throw new IllegalArgumentException ("QtyOnHand is mandatory.");
        set_ValueNoCheck ("QtyOnHand", QtyOnHand);
        
    }
    
    /** Get On Hand Quantity.
    @return On Hand Quantity */
    public java.math.BigDecimal getQtyOnHand() 
    {
        return get_ValueAsBigDecimal("QtyOnHand");
        
    }
    
    /** Set Ordered Quantity.
    @param QtyOrdered Ordered Quantity */
    public void setQtyOrdered (java.math.BigDecimal QtyOrdered)
    {
        if (QtyOrdered == null) throw new IllegalArgumentException ("QtyOrdered is mandatory.");
        set_ValueNoCheck ("QtyOrdered", QtyOrdered);
        
    }
    
    /** Get Ordered Quantity.
    @return Ordered Quantity */
    public java.math.BigDecimal getQtyOrdered() 
    {
        return get_ValueAsBigDecimal("QtyOrdered");
        
    }
    
    /** Set Quantity Reserved.
    @param QtyReserved Quantity Reserved */
    public void setQtyReserved (java.math.BigDecimal QtyReserved)
    {
        if (QtyReserved == null) throw new IllegalArgumentException ("QtyReserved is mandatory.");
        set_ValueNoCheck ("QtyReserved", QtyReserved);
        
    }
    
    /** Get Quantity Reserved.
    @return Quantity Reserved */
    public java.math.BigDecimal getQtyReserved() 
    {
        return get_ValueAsBigDecimal("QtyReserved");
        
    }
    
    
}
