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
/** Generated Model for M_BOMProduct
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_BOMProduct extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_BOMProduct_ID id
    @param trx transaction
    */
    public X_M_BOMProduct (Ctx ctx, int M_BOMProduct_ID, Trx trx)
    {
        super (ctx, M_BOMProduct_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_BOMProduct_ID == 0)
        {
            setBOMProductType (null);	// S
            setBOMQty (Env.ZERO);	// 1
            setIsPhantom (false);	// N
            setLeadTimeOffset (0);
            setLine (0);	// @SQL=SELECT NVL(MAX(Line),0)+10 AS DefaultValue FROM M_BOMProduct WHERE M_BOM_ID=@M_BOM_ID@
            setM_BOMProduct_ID (0);
            setM_BOM_ID (0);
            setM_ProductBOM_ID (0);
            setSupplyType (null);	// P
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_BOMProduct (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27498975992789L;
    /** Last Updated Timestamp 2008-07-23 15:04:36.0 */
    public static final long updatedMS = 1216850676000L;
    /** AD_Table_ID=801 */
    public static final int Table_ID=801;
    
    /** TableName=M_BOMProduct */
    public static final String Table_Name="M_BOMProduct";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_BOMProduct");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    
    /** BOMProductType AD_Reference_ID=349 */
    public static final int BOMPRODUCTTYPE_AD_Reference_ID=349;
    /** Alternative = A */
    public static final String BOMPRODUCTTYPE_Alternative = X_Ref_M_BOMProduct_Type.ALTERNATIVE.getValue();
    /** Alternative (Default) = D */
    public static final String BOMPRODUCTTYPE_AlternativeDefault = X_Ref_M_BOMProduct_Type.ALTERNATIVE_DEFAULT.getValue();
    /** Optional Product = O */
    public static final String BOMPRODUCTTYPE_OptionalProduct = X_Ref_M_BOMProduct_Type.OPTIONAL_PRODUCT.getValue();
    /** Standard Product = S */
    public static final String BOMPRODUCTTYPE_StandardProduct = X_Ref_M_BOMProduct_Type.STANDARD_PRODUCT.getValue();
    /** Outside Processing = X */
    public static final String BOMPRODUCTTYPE_OutsideProcessing = X_Ref_M_BOMProduct_Type.OUTSIDE_PROCESSING.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isBOMProductTypeValid(String test)
    {
         return X_Ref_M_BOMProduct_Type.isValid(test);
         
    }
    /** Set Component Type.
    @param BOMProductType BOM Product Type */
    public void setBOMProductType (String BOMProductType)
    {
        if (BOMProductType == null) throw new IllegalArgumentException ("BOMProductType is mandatory");
        if (!isBOMProductTypeValid(BOMProductType))
        throw new IllegalArgumentException ("BOMProductType Invalid value - " + BOMProductType + " - Reference_ID=349 - A - D - O - S - X");
        set_Value ("BOMProductType", BOMProductType);
        
    }
    
    /** Get Component Type.
    @return BOM Product Type */
    public String getBOMProductType() 
    {
        return (String)get_Value("BOMProductType");
        
    }
    
    /** Set Quantity.
    @param BOMQty Bill of Materials Quantity */
    public void setBOMQty (java.math.BigDecimal BOMQty)
    {
        if (BOMQty == null) throw new IllegalArgumentException ("BOMQty is mandatory.");
        set_Value ("BOMQty", BOMQty);
        
    }
    
    /** Get Quantity.
    @return Bill of Materials Quantity */
    public java.math.BigDecimal getBOMQty() 
    {
        return get_ValueAsBigDecimal("BOMQty");
        
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
    
    /** Set Phantom.
    @param IsPhantom Phantom Component */
    public void setIsPhantom (boolean IsPhantom)
    {
        set_Value ("IsPhantom", Boolean.valueOf(IsPhantom));
        
    }
    
    /** Get Phantom.
    @return Phantom Component */
    public boolean isPhantom() 
    {
        return get_ValueAsBoolean("IsPhantom");
        
    }
    
    /** Set Lead Time Offset.
    @param LeadTimeOffset Optional Lead Time offset before starting production */
    public void setLeadTimeOffset (int LeadTimeOffset)
    {
        set_Value ("LeadTimeOffset", Integer.valueOf(LeadTimeOffset));
        
    }
    
    /** Get Lead Time Offset.
    @return Optional Lead Time offset before starting production */
    public int getLeadTimeOffset() 
    {
        return get_ValueAsInt("LeadTimeOffset");
        
    }
    
    /** Set Line No.
    @param Line Unique line for this document */
    public void setLine (int Line)
    {
        set_Value ("Line", Integer.valueOf(Line));
        
    }
    
    /** Get Line No.
    @return Unique line for this document */
    public int getLine() 
    {
        return get_ValueAsInt("Line");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getLine()));
        
    }
    
    /** Set Attribute Set Instance.
    @param M_AttributeSetInstance_ID Product Attribute Set Instance */
    public void setM_AttributeSetInstance_ID (int M_AttributeSetInstance_ID)
    {
        if (M_AttributeSetInstance_ID <= 0) set_Value ("M_AttributeSetInstance_ID", null);
        else
        set_Value ("M_AttributeSetInstance_ID", Integer.valueOf(M_AttributeSetInstance_ID));
        
    }
    
    /** Get Attribute Set Instance.
    @return Product Attribute Set Instance */
    public int getM_AttributeSetInstance_ID() 
    {
        return get_ValueAsInt("M_AttributeSetInstance_ID");
        
    }
    
    /** Set Alternative Group.
    @param M_BOMAlternative_ID Product BOM Alternative Group */
    public void setM_BOMAlternative_ID (int M_BOMAlternative_ID)
    {
        if (M_BOMAlternative_ID <= 0) set_Value ("M_BOMAlternative_ID", null);
        else
        set_Value ("M_BOMAlternative_ID", Integer.valueOf(M_BOMAlternative_ID));
        
    }
    
    /** Get Alternative Group.
    @return Product BOM Alternative Group */
    public int getM_BOMAlternative_ID() 
    {
        return get_ValueAsInt("M_BOMAlternative_ID");
        
    }
    
    /** Set BOM Component Line.
    @param M_BOMProduct_ID Bill of Materials Component Line */
    public void setM_BOMProduct_ID (int M_BOMProduct_ID)
    {
        if (M_BOMProduct_ID < 1) throw new IllegalArgumentException ("M_BOMProduct_ID is mandatory.");
        set_ValueNoCheck ("M_BOMProduct_ID", Integer.valueOf(M_BOMProduct_ID));
        
    }
    
    /** Get BOM Component Line.
    @return Bill of Materials Component Line */
    public int getM_BOMProduct_ID() 
    {
        return get_ValueAsInt("M_BOMProduct_ID");
        
    }
    
    /** Set BOM.
    @param M_BOM_ID Bill of Materials */
    public void setM_BOM_ID (int M_BOM_ID)
    {
        if (M_BOM_ID < 1) throw new IllegalArgumentException ("M_BOM_ID is mandatory.");
        set_ValueNoCheck ("M_BOM_ID", Integer.valueOf(M_BOM_ID));
        
    }
    
    /** Get BOM.
    @return Bill of Materials */
    public int getM_BOM_ID() 
    {
        return get_ValueAsInt("M_BOM_ID");
        
    }
    
    /** Set Locator.
    @param M_Locator_ID Warehouse Locator */
    public void setM_Locator_ID (int M_Locator_ID)
    {
        if (M_Locator_ID <= 0) set_Value ("M_Locator_ID", null);
        else
        set_Value ("M_Locator_ID", Integer.valueOf(M_Locator_ID));
        
    }
    
    /** Get Locator.
    @return Warehouse Locator */
    public int getM_Locator_ID() 
    {
        return get_ValueAsInt("M_Locator_ID");
        
    }
    
    
    /** M_ProductBOMVersion_ID AD_Reference_ID=442 */
    public static final int M_PRODUCTBOMVERSION_ID_AD_Reference_ID=442;
    /** Set Component BOM.
    @param M_ProductBOMVersion_ID BOM for a component */
    public void setM_ProductBOMVersion_ID (int M_ProductBOMVersion_ID)
    {
        if (M_ProductBOMVersion_ID <= 0) set_Value ("M_ProductBOMVersion_ID", null);
        else
        set_Value ("M_ProductBOMVersion_ID", Integer.valueOf(M_ProductBOMVersion_ID));
        
    }
    
    /** Get Component BOM.
    @return BOM for a component */
    public int getM_ProductBOMVersion_ID() 
    {
        return get_ValueAsInt("M_ProductBOMVersion_ID");
        
    }
    
    
    /** M_ProductBOM_ID AD_Reference_ID=472 */
    public static final int M_PRODUCTBOM_ID_AD_Reference_ID=472;
    /** Set Component.
    @param M_ProductBOM_ID Bill of Materials Component (Product) */
    public void setM_ProductBOM_ID (int M_ProductBOM_ID)
    {
        if (M_ProductBOM_ID < 1) throw new IllegalArgumentException ("M_ProductBOM_ID is mandatory.");
        set_Value ("M_ProductBOM_ID", Integer.valueOf(M_ProductBOM_ID));
        
    }
    
    /** Get Component.
    @return Bill of Materials Component (Product) */
    public int getM_ProductBOM_ID() 
    {
        return get_ValueAsInt("M_ProductBOM_ID");
        
    }
    
    /** Set Product Operation.
    @param M_ProductOperation_ID Product Manufacturing Operation */
    public void setM_ProductOperation_ID (int M_ProductOperation_ID)
    {
        if (M_ProductOperation_ID <= 0) set_Value ("M_ProductOperation_ID", null);
        else
        set_Value ("M_ProductOperation_ID", Integer.valueOf(M_ProductOperation_ID));
        
    }
    
    /** Get Product Operation.
    @return Product Manufacturing Operation */
    public int getM_ProductOperation_ID() 
    {
        return get_ValueAsInt("M_ProductOperation_ID");
        
    }
    
    /** Set Sequence.
    @param SeqNo Method of ordering elements;
     lowest number comes first */
    public void setSeqNo (int SeqNo)
    {
        set_Value ("SeqNo", Integer.valueOf(SeqNo));
        
    }
    
    /** Get Sequence.
    @return Method of ordering elements;
     lowest number comes first */
    public int getSeqNo() 
    {
        return get_ValueAsInt("SeqNo");
        
    }
    
    
    /** SupplyType AD_Reference_ID=444 */
    public static final int SUPPLYTYPE_AD_Reference_ID=444;
    /** Assembly Pull = A */
    public static final String SUPPLYTYPE_AssemblyPull = X_Ref_M_BOMProduct_SupplyType.ASSEMBLY_PULL.getValue();
    /** Push = P */
    public static final String SUPPLYTYPE_Push = X_Ref_M_BOMProduct_SupplyType.PUSH.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isSupplyTypeValid(String test)
    {
         return X_Ref_M_BOMProduct_SupplyType.isValid(test);
         
    }
    /** Set Supply Type.
    @param SupplyType Supply type for components */
    public void setSupplyType (String SupplyType)
    {
        if (SupplyType == null) throw new IllegalArgumentException ("SupplyType is mandatory");
        if (!isSupplyTypeValid(SupplyType))
        throw new IllegalArgumentException ("SupplyType Invalid value - " + SupplyType + " - Reference_ID=444 - A - P");
        set_Value ("SupplyType", SupplyType);
        
    }
    
    /** Get Supply Type.
    @return Supply type for components */
    public String getSupplyType() 
    {
        return (String)get_Value("SupplyType");
        
    }
    
    
}
