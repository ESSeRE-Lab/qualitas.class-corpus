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
/** Generated Model for M_ProductOperation
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_ProductOperation extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_ProductOperation_ID id
    @param trx transaction
    */
    public X_M_ProductOperation (Ctx ctx, int M_ProductOperation_ID, Trx trx)
    {
        super (ctx, M_ProductOperation_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_ProductOperation_ID == 0)
        {
            setM_ProductOperation_ID (0);
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
    public X_M_ProductOperation (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=796 */
    public static final int Table_ID=796;
    
    /** TableName=M_ProductOperation */
    public static final String Table_Name="M_ProductOperation";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_ProductOperation");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
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
    
    /** Set Product Operation.
    @param M_ProductOperation_ID Product Manufacturing Operation */
    public void setM_ProductOperation_ID (int M_ProductOperation_ID)
    {
        if (M_ProductOperation_ID < 1) throw new IllegalArgumentException ("M_ProductOperation_ID is mandatory.");
        set_ValueNoCheck ("M_ProductOperation_ID", Integer.valueOf(M_ProductOperation_ID));
        
    }
    
    /** Get Product Operation.
    @return Product Manufacturing Operation */
    public int getM_ProductOperation_ID() 
    {
        return get_ValueAsInt("M_ProductOperation_ID");
        
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
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getName());
        
    }
    
    /** Set Setup Time.
    @param SetupTime Setup time before starting Production */
    public void setSetupTime (java.math.BigDecimal SetupTime)
    {
        set_Value ("SetupTime", SetupTime);
        
    }
    
    /** Get Setup Time.
    @return Setup time before starting Production */
    public java.math.BigDecimal getSetupTime() 
    {
        return get_ValueAsBigDecimal("SetupTime");
        
    }
    
    /** Set Teardown Time.
    @param TeardownTime Time at the end of the operation */
    public void setTeardownTime (java.math.BigDecimal TeardownTime)
    {
        set_Value ("TeardownTime", TeardownTime);
        
    }
    
    /** Get Teardown Time.
    @return Time at the end of the operation */
    public java.math.BigDecimal getTeardownTime() 
    {
        return get_ValueAsBigDecimal("TeardownTime");
        
    }
    
    /** Set Runtime per Unit.
    @param UnitRuntime Time to produce one unit */
    public void setUnitRuntime (java.math.BigDecimal UnitRuntime)
    {
        set_Value ("UnitRuntime", UnitRuntime);
        
    }
    
    /** Get Runtime per Unit.
    @return Time to produce one unit */
    public java.math.BigDecimal getUnitRuntime() 
    {
        return get_ValueAsBigDecimal("UnitRuntime");
        
    }
    
    
}
