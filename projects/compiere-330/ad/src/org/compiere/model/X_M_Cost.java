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
/** Generated Model for M_Cost
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_Cost extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_Cost_ID id
    @param trx transaction
    */
    public X_M_Cost (Ctx ctx, int M_Cost_ID, Trx trx)
    {
        super (ctx, M_Cost_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_Cost_ID == 0)
        {
            setC_AcctSchema_ID (0);
            setCurrentCostPrice (Env.ZERO);
            setCurrentQty (Env.ZERO);
            setLastCostPrice (Env.ZERO);	// 0
            setM_AttributeSetInstance_ID (0);
            setM_CostElement_ID (0);
            setM_CostType_ID (0);
            setM_Product_ID (0);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_Cost (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=771 */
    public static final int Table_ID=771;
    
    /** TableName=M_Cost */
    public static final String Table_Name="M_Cost";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_Cost");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Accounting Schema.
    @param C_AcctSchema_ID Rules for accounting */
    public void setC_AcctSchema_ID (int C_AcctSchema_ID)
    {
        if (C_AcctSchema_ID < 1) throw new IllegalArgumentException ("C_AcctSchema_ID is mandatory.");
        set_ValueNoCheck ("C_AcctSchema_ID", Integer.valueOf(C_AcctSchema_ID));
        
    }
    
    /** Get Accounting Schema.
    @return Rules for accounting */
    public int getC_AcctSchema_ID() 
    {
        return get_ValueAsInt("C_AcctSchema_ID");
        
    }
    
    
    /** CostingMethod AD_Reference_ID=122 */
    public static final int COSTINGMETHOD_AD_Reference_ID=122;
    /** Average PO = A */
    public static final String COSTINGMETHOD_AveragePO = X_Ref_C_AcctSchema_Costing_Method.AVERAGE_PO.getValue();
    /** FiFo = F */
    public static final String COSTINGMETHOD_FiFo = X_Ref_C_AcctSchema_Costing_Method.FI_FO.getValue();
    /** Average Invoice = I */
    public static final String COSTINGMETHOD_AverageInvoice = X_Ref_C_AcctSchema_Costing_Method.AVERAGE_INVOICE.getValue();
    /** LiFo = L */
    public static final String COSTINGMETHOD_LiFo = X_Ref_C_AcctSchema_Costing_Method.LI_FO.getValue();
    /** Standard Costing = S */
    public static final String COSTINGMETHOD_StandardCosting = X_Ref_C_AcctSchema_Costing_Method.STANDARD_COSTING.getValue();
    /** User Defined = U */
    public static final String COSTINGMETHOD_UserDefined = X_Ref_C_AcctSchema_Costing_Method.USER_DEFINED.getValue();
    /** Last Invoice = i */
    public static final String COSTINGMETHOD_LastInvoice = X_Ref_C_AcctSchema_Costing_Method.LAST_INVOICE.getValue();
    /** Last PO Price = p */
    public static final String COSTINGMETHOD_LastPOPrice = X_Ref_C_AcctSchema_Costing_Method.LAST_PO_PRICE.getValue();
    /** _ = x */
    public static final String COSTINGMETHOD__ = X_Ref_C_AcctSchema_Costing_Method._.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isCostingMethodValid(String test)
    {
         return X_Ref_C_AcctSchema_Costing_Method.isValid(test);
         
    }
    /** Set Costing Method.
    @param CostingMethod Indicates how Costs will be calculated */
    public void setCostingMethod (String CostingMethod)
    {
        if (!isCostingMethodValid(CostingMethod))
        throw new IllegalArgumentException ("CostingMethod Invalid value - " + CostingMethod + " - Reference_ID=122 - A - F - I - L - S - U - i - p - x");
        throw new IllegalArgumentException ("CostingMethod is virtual column");
        
    }
    
    /** Get Costing Method.
    @return Indicates how Costs will be calculated */
    public String getCostingMethod() 
    {
        return (String)get_Value("CostingMethod");
        
    }
    
    /** Set Accumulated Amt.
    @param CumulatedAmt Total Amount */
    public void setCumulatedAmt (java.math.BigDecimal CumulatedAmt)
    {
        set_ValueNoCheck ("CumulatedAmt", CumulatedAmt);
        
    }
    
    /** Get Accumulated Amt.
    @return Total Amount */
    public java.math.BigDecimal getCumulatedAmt() 
    {
        return get_ValueAsBigDecimal("CumulatedAmt");
        
    }
    
    /** Set Accumulated Qty.
    @param CumulatedQty Total Quantity */
    public void setCumulatedQty (java.math.BigDecimal CumulatedQty)
    {
        set_ValueNoCheck ("CumulatedQty", CumulatedQty);
        
    }
    
    /** Get Accumulated Qty.
    @return Total Quantity */
    public java.math.BigDecimal getCumulatedQty() 
    {
        return get_ValueAsBigDecimal("CumulatedQty");
        
    }
    
    /** Set Current Cost Price.
    @param CurrentCostPrice The currently used cost price */
    public void setCurrentCostPrice (java.math.BigDecimal CurrentCostPrice)
    {
        if (CurrentCostPrice == null) throw new IllegalArgumentException ("CurrentCostPrice is mandatory.");
        set_Value ("CurrentCostPrice", CurrentCostPrice);
        
    }
    
    /** Get Current Cost Price.
    @return The currently used cost price */
    public java.math.BigDecimal getCurrentCostPrice() 
    {
        return get_ValueAsBigDecimal("CurrentCostPrice");
        
    }
    
    /** Set Current Quantity.
    @param CurrentQty Current Quantity */
    public void setCurrentQty (java.math.BigDecimal CurrentQty)
    {
        if (CurrentQty == null) throw new IllegalArgumentException ("CurrentQty is mandatory.");
        set_Value ("CurrentQty", CurrentQty);
        
    }
    
    /** Get Current Quantity.
    @return Current Quantity */
    public java.math.BigDecimal getCurrentQty() 
    {
        return get_ValueAsBigDecimal("CurrentQty");
        
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
    
    /** Set Future Cost Price.
    @param FutureCostPrice Future Cost Price */
    public void setFutureCostPrice (java.math.BigDecimal FutureCostPrice)
    {
        set_Value ("FutureCostPrice", FutureCostPrice);
        
    }
    
    /** Get Future Cost Price.
    @return Future Cost Price */
    public java.math.BigDecimal getFutureCostPrice() 
    {
        return get_ValueAsBigDecimal("FutureCostPrice");
        
    }
    
    /** Set Last Cost Price.
    @param LastCostPrice Previous Cost */
    public void setLastCostPrice (java.math.BigDecimal LastCostPrice)
    {
        if (LastCostPrice == null) throw new IllegalArgumentException ("LastCostPrice is mandatory.");
        set_Value ("LastCostPrice", LastCostPrice);
        
    }
    
    /** Get Last Cost Price.
    @return Previous Cost */
    public java.math.BigDecimal getLastCostPrice() 
    {
        return get_ValueAsBigDecimal("LastCostPrice");
        
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
    
    /** Set Cost Element.
    @param M_CostElement_ID Product Cost Element */
    public void setM_CostElement_ID (int M_CostElement_ID)
    {
        if (M_CostElement_ID < 1) throw new IllegalArgumentException ("M_CostElement_ID is mandatory.");
        set_ValueNoCheck ("M_CostElement_ID", Integer.valueOf(M_CostElement_ID));
        
    }
    
    /** Get Cost Element.
    @return Product Cost Element */
    public int getM_CostElement_ID() 
    {
        return get_ValueAsInt("M_CostElement_ID");
        
    }
    
    /** Set Cost Type.
    @param M_CostType_ID Type of Cost (e.g. Current, Plan, Future) */
    public void setM_CostType_ID (int M_CostType_ID)
    {
        if (M_CostType_ID < 1) throw new IllegalArgumentException ("M_CostType_ID is mandatory.");
        set_ValueNoCheck ("M_CostType_ID", Integer.valueOf(M_CostType_ID));
        
    }
    
    /** Get Cost Type.
    @return Type of Cost (e.g. Current, Plan, Future) */
    public int getM_CostType_ID() 
    {
        return get_ValueAsInt("M_CostType_ID");
        
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
    
    /** Set Percent.
    @param PercentCost Percent Cost */
    public void setPercentCost (java.math.BigDecimal PercentCost)
    {
        set_Value ("PercentCost", PercentCost);
        
    }
    
    /** Get Percent.
    @return Percent Cost */
    public java.math.BigDecimal getPercentCost() 
    {
        return get_ValueAsBigDecimal("PercentCost");
        
    }
    
    /** Set Processed.
    @param Processed The document has been processed */
    public void setProcessed (boolean Processed)
    {
        throw new IllegalArgumentException ("Processed is virtual column");
        
    }
    
    /** Get Processed.
    @return The document has been processed */
    public boolean isProcessed() 
    {
        return get_ValueAsBoolean("Processed");
        
    }
    
    
}
