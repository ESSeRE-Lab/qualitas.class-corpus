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
/** Generated Model for M_Product_Category
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_Product_Category extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_Product_Category_ID id
    @param trx transaction
    */
    public X_M_Product_Category (Ctx ctx, int M_Product_Category_ID, Trx trx)
    {
        super (ctx, M_Product_Category_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_Product_Category_ID == 0)
        {
            setIsDefault (false);
            setIsPurchasedToOrder (false);	// N
            setIsSelfService (true);	// Y
            setMMPolicy (null);	// F
            setM_Product_Category_ID (0);
            setName (null);
            setPlannedMargin (Env.ZERO);
            setValue (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_Product_Category (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=209 */
    public static final int Table_ID=209;
    
    /** TableName=M_Product_Category */
    public static final String Table_Name="M_Product_Category";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_Product_Category");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Print Color.
    @param AD_PrintColor_ID Color used for printing and display */
    public void setAD_PrintColor_ID (int AD_PrintColor_ID)
    {
        if (AD_PrintColor_ID <= 0) set_Value ("AD_PrintColor_ID", null);
        else
        set_Value ("AD_PrintColor_ID", Integer.valueOf(AD_PrintColor_ID));
        
    }
    
    /** Get Print Color.
    @return Color used for printing and display */
    public int getAD_PrintColor_ID() 
    {
        return get_ValueAsInt("AD_PrintColor_ID");
        
    }
    
    /** Set Asset Group.
    @param A_Asset_Group_ID Group of Assets */
    public void setA_Asset_Group_ID (int A_Asset_Group_ID)
    {
        if (A_Asset_Group_ID <= 0) set_Value ("A_Asset_Group_ID", null);
        else
        set_Value ("A_Asset_Group_ID", Integer.valueOf(A_Asset_Group_ID));
        
    }
    
    /** Get Asset Group.
    @return Group of Assets */
    public int getA_Asset_Group_ID() 
    {
        return get_ValueAsInt("A_Asset_Group_ID");
        
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
    
    /** Set Default.
    @param IsDefault Default value */
    public void setIsDefault (boolean IsDefault)
    {
        set_Value ("IsDefault", Boolean.valueOf(IsDefault));
        
    }
    
    /** Get Default.
    @return Default value */
    public boolean isDefault() 
    {
        return get_ValueAsBoolean("IsDefault");
        
    }
    
    /** Set Purchased To Order.
    @param IsPurchasedToOrder Products that are usually not kept in stock, but are purchased whenever there is a demand */
    public void setIsPurchasedToOrder (boolean IsPurchasedToOrder)
    {
        set_Value ("IsPurchasedToOrder", Boolean.valueOf(IsPurchasedToOrder));
        
    }
    
    /** Get Purchased To Order.
    @return Products that are usually not kept in stock, but are purchased whenever there is a demand */
    public boolean isPurchasedToOrder() 
    {
        return get_ValueAsBoolean("IsPurchasedToOrder");
        
    }
    
    /** Set Self-Service.
    @param IsSelfService This is a Self-Service entry or this entry can be changed via Self-Service */
    public void setIsSelfService (boolean IsSelfService)
    {
        set_Value ("IsSelfService", Boolean.valueOf(IsSelfService));
        
    }
    
    /** Get Self-Service.
    @return This is a Self-Service entry or this entry can be changed via Self-Service */
    public boolean isSelfService() 
    {
        return get_ValueAsBoolean("IsSelfService");
        
    }
    
    
    /** MMPolicy AD_Reference_ID=335 */
    public static final int MMPOLICY_AD_Reference_ID=335;
    /** FiFo = F */
    public static final String MMPOLICY_FiFo = X_Ref__MMPolicy.FI_FO.getValue();
    /** LiFo = L */
    public static final String MMPOLICY_LiFo = X_Ref__MMPolicy.LI_FO.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isMMPolicyValid(String test)
    {
         return X_Ref__MMPolicy.isValid(test);
         
    }
    /** Set Material Policy.
    @param MMPolicy Material Movement Policy */
    public void setMMPolicy (String MMPolicy)
    {
        if (MMPolicy == null) throw new IllegalArgumentException ("MMPolicy is mandatory");
        if (!isMMPolicyValid(MMPolicy))
        throw new IllegalArgumentException ("MMPolicy Invalid value - " + MMPolicy + " - Reference_ID=335 - F - L");
        set_Value ("MMPolicy", MMPolicy);
        
    }
    
    /** Get Material Policy.
    @return Material Movement Policy */
    public String getMMPolicy() 
    {
        return (String)get_Value("MMPolicy");
        
    }
    
    /** Set Product Category.
    @param M_Product_Category_ID Category of a Product */
    public void setM_Product_Category_ID (int M_Product_Category_ID)
    {
        if (M_Product_Category_ID < 1) throw new IllegalArgumentException ("M_Product_Category_ID is mandatory.");
        set_ValueNoCheck ("M_Product_Category_ID", Integer.valueOf(M_Product_Category_ID));
        
    }
    
    /** Get Product Category.
    @return Category of a Product */
    public int getM_Product_Category_ID() 
    {
        return get_ValueAsInt("M_Product_Category_ID");
        
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
    
    /** Set Planned Margin %.
    @param PlannedMargin Project's planned margin as a percentage */
    public void setPlannedMargin (java.math.BigDecimal PlannedMargin)
    {
        if (PlannedMargin == null) throw new IllegalArgumentException ("PlannedMargin is mandatory.");
        set_Value ("PlannedMargin", PlannedMargin);
        
    }
    
    /** Get Planned Margin %.
    @return Project's planned margin as a percentage */
    public java.math.BigDecimal getPlannedMargin() 
    {
        return get_ValueAsBigDecimal("PlannedMargin");
        
    }
    
    /** Set Search Key.
    @param Value Search key for the record in the format required - must be unique */
    public void setValue (String Value)
    {
        if (Value == null) throw new IllegalArgumentException ("Value is mandatory.");
        set_Value ("Value", Value);
        
    }
    
    /** Get Search Key.
    @return Search key for the record in the format required - must be unique */
    public String getValue() 
    {
        return (String)get_Value("Value");
        
    }
    
    
}
