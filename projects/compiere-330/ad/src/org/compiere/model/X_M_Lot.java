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
/** Generated Model for M_Lot
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_Lot extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_Lot_ID id
    @param trx transaction
    */
    public X_M_Lot (Ctx ctx, int M_Lot_ID, Trx trx)
    {
        super (ctx, M_Lot_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_Lot_ID == 0)
        {
            setM_Lot_ID (0);
            setM_Product_ID (0);
            setName (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_Lot (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=557 */
    public static final int Table_ID=557;
    
    /** TableName=M_Lot */
    public static final String Table_Name="M_Lot";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_Lot");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Date From.
    @param DateFrom Starting date for a range */
    public void setDateFrom (Timestamp DateFrom)
    {
        set_Value ("DateFrom", DateFrom);
        
    }
    
    /** Get Date From.
    @return Starting date for a range */
    public Timestamp getDateFrom() 
    {
        return (Timestamp)get_Value("DateFrom");
        
    }
    
    /** Set Date To.
    @param DateTo End date of a date range */
    public void setDateTo (Timestamp DateTo)
    {
        set_Value ("DateTo", DateTo);
        
    }
    
    /** Get Date To.
    @return End date of a date range */
    public Timestamp getDateTo() 
    {
        return (Timestamp)get_Value("DateTo");
        
    }
    
    /** Set Description.
    @param Description Optional short description of the record */
    public void setDescription (String Description)
    {
        set_Value ("Description", Description);
        
    }
    
    /** Get Description.
    @return Optional short description of the record */
    public String getDescription() 
    {
        return (String)get_Value("Description");
        
    }
    
    /** Set Comment.
    @param Help Comment, Help or Hint */
    public void setHelp (String Help)
    {
        set_Value ("Help", Help);
        
    }
    
    /** Get Comment.
    @return Comment, Help or Hint */
    public String getHelp() 
    {
        return (String)get_Value("Help");
        
    }
    
    /** Set Lot Control.
    @param M_LotCtl_ID Product Lot Control */
    public void setM_LotCtl_ID (int M_LotCtl_ID)
    {
        if (M_LotCtl_ID <= 0) set_ValueNoCheck ("M_LotCtl_ID", null);
        else
        set_ValueNoCheck ("M_LotCtl_ID", Integer.valueOf(M_LotCtl_ID));
        
    }
    
    /** Get Lot Control.
    @return Product Lot Control */
    public int getM_LotCtl_ID() 
    {
        return get_ValueAsInt("M_LotCtl_ID");
        
    }
    
    /** Set Lot.
    @param M_Lot_ID Product Lot Definition */
    public void setM_Lot_ID (int M_Lot_ID)
    {
        if (M_Lot_ID < 1) throw new IllegalArgumentException ("M_Lot_ID is mandatory.");
        set_ValueNoCheck ("M_Lot_ID", Integer.valueOf(M_Lot_ID));
        
    }
    
    /** Get Lot.
    @return Product Lot Definition */
    public int getM_Lot_ID() 
    {
        return get_ValueAsInt("M_Lot_ID");
        
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
    
    /** Set Name.
    @param Name Alphanumeric identifier of the entity */
    public void setName (String Name)
    {
        if (Name == null) throw new IllegalArgumentException ("Name is mandatory.");
        set_Value ("Name", Name);
        
    }
    
    /** Get Name.
    @return Alphanumeric identifier of the entity */
    public String getName() 
    {
        return (String)get_Value("Name");
        
    }
    
    
}
