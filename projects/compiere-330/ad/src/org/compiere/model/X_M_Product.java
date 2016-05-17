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
/** Generated Model for M_Product
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_Product extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_Product_ID id
    @param trx transaction
    */
    public X_M_Product (Ctx ctx, int M_Product_ID, Trx trx)
    {
        super (ctx, M_Product_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_Product_ID == 0)
        {
            setC_TaxCategory_ID (0);
            setC_UOM_ID (0);
            setIsBOM (false);	// N
            setIsDropShip (false);
            setIsExcludeAutoDelivery (false);	// N
            setIsInvoicePrintDetails (false);
            setIsPickListPrintDetails (false);
            setIsPurchased (true);	// Y
            setIsPurchasedToOrder (false);	// N
            setIsSelfService (true);	// Y
            setIsSold (true);	// Y
            setIsStocked (true);	// Y
            setIsSummary (false);
            setIsVerified (false);	// N
            setIsWebStoreFeatured (false);
            setM_Product_Category_ID (0);
            setM_Product_ID (0);
            setName (null);
            setProductType (null);	// I
            setValue (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_Product (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27508080425789L;
    /** Last Updated Timestamp 2008-11-05 23:05:09.0 */
    public static final long updatedMS = 1225955109000L;
    /** AD_Table_ID=208 */
    public static final int Table_ID=208;
    
    /** TableName=M_Product */
    public static final String Table_Name="M_Product";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_Product");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Subscription Type.
    @param C_SubscriptionType_ID Type of subscription */
    public void setC_SubscriptionType_ID (int C_SubscriptionType_ID)
    {
        if (C_SubscriptionType_ID <= 0) set_Value ("C_SubscriptionType_ID", null);
        else
        set_Value ("C_SubscriptionType_ID", Integer.valueOf(C_SubscriptionType_ID));
        
    }
    
    /** Get Subscription Type.
    @return Type of subscription */
    public int getC_SubscriptionType_ID() 
    {
        return get_ValueAsInt("C_SubscriptionType_ID");
        
    }
    
    /** Set Tax Category.
    @param C_TaxCategory_ID Tax Category */
    public void setC_TaxCategory_ID (int C_TaxCategory_ID)
    {
        if (C_TaxCategory_ID < 1) throw new IllegalArgumentException ("C_TaxCategory_ID is mandatory.");
        set_Value ("C_TaxCategory_ID", Integer.valueOf(C_TaxCategory_ID));
        
    }
    
    /** Get Tax Category.
    @return Tax Category */
    public int getC_TaxCategory_ID() 
    {
        return get_ValueAsInt("C_TaxCategory_ID");
        
    }
    
    /** Set UOM Group.
    @param C_UOMGroup_ID Group for managing sets of Unit of Measure */
    public void setC_UOMGroup_ID (int C_UOMGroup_ID)
    {
        if (C_UOMGroup_ID <= 0) set_Value ("C_UOMGroup_ID", null);
        else
        set_Value ("C_UOMGroup_ID", Integer.valueOf(C_UOMGroup_ID));
        
    }
    
    /** Get UOM Group.
    @return Group for managing sets of Unit of Measure */
    public int getC_UOMGroup_ID() 
    {
        return get_ValueAsInt("C_UOMGroup_ID");
        
    }
    
    /** Set UOM.
    @param C_UOM_ID Unit of Measure */
    public void setC_UOM_ID (int C_UOM_ID)
    {
        if (C_UOM_ID < 1) throw new IllegalArgumentException ("C_UOM_ID is mandatory.");
        set_Value ("C_UOM_ID", Integer.valueOf(C_UOM_ID));
        
    }
    
    /** Get UOM.
    @return Unit of Measure */
    public int getC_UOM_ID() 
    {
        return get_ValueAsInt("C_UOM_ID");
        
    }
    
    /** Set Classification.
    @param Classification Classification for grouping */
    public void setClassification (String Classification)
    {
        set_Value ("Classification", Classification);
        
    }
    
    /** Get Classification.
    @return Classification for grouping */
    public String getClassification() 
    {
        return (String)get_Value("Classification");
        
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
    
    /** Set Description URL.
    @param DescriptionURL URL for the description */
    public void setDescriptionURL (String DescriptionURL)
    {
        set_Value ("DescriptionURL", DescriptionURL);
        
    }
    
    /** Get Description URL.
    @return URL for the description */
    public String getDescriptionURL() 
    {
        return (String)get_Value("DescriptionURL");
        
    }
    
    /** Set Discontinued.
    @param Discontinued This product is no longer available */
    public void setDiscontinued (boolean Discontinued)
    {
        set_Value ("Discontinued", Boolean.valueOf(Discontinued));
        
    }
    
    /** Get Discontinued.
    @return This product is no longer available */
    public boolean isDiscontinued() 
    {
        return get_ValueAsBoolean("Discontinued");
        
    }
    
    /** Set Discontinued by.
    @param DiscontinuedBy Discontinued By */
    public void setDiscontinuedBy (Timestamp DiscontinuedBy)
    {
        set_Value ("DiscontinuedBy", DiscontinuedBy);
        
    }
    
    /** Get Discontinued by.
    @return Discontinued By */
    public Timestamp getDiscontinuedBy() 
    {
        return (Timestamp)get_Value("DiscontinuedBy");
        
    }
    
    /** Set Document Note.
    @param DocumentNote Additional information for a Document */
    public void setDocumentNote (String DocumentNote)
    {
        set_Value ("DocumentNote", DocumentNote);
        
    }
    
    /** Get Document Note.
    @return Additional information for a Document */
    public String getDocumentNote() 
    {
        return (String)get_Value("DocumentNote");
        
    }
    
    /** Set Guarantee Days.
    @param GuaranteeDays Number of days the product is guaranteed or available */
    public void setGuaranteeDays (int GuaranteeDays)
    {
        set_Value ("GuaranteeDays", Integer.valueOf(GuaranteeDays));
        
    }
    
    /** Get Guarantee Days.
    @return Number of days the product is guaranteed or available */
    public int getGuaranteeDays() 
    {
        return get_ValueAsInt("GuaranteeDays");
        
    }
    
    /** Set Min Guarantee Days.
    @param GuaranteeDaysMin Minimum number of guarantee days */
    public void setGuaranteeDaysMin (int GuaranteeDaysMin)
    {
        set_Value ("GuaranteeDaysMin", Integer.valueOf(GuaranteeDaysMin));
        
    }
    
    /** Get Min Guarantee Days.
    @return Minimum number of guarantee days */
    public int getGuaranteeDaysMin() 
    {
        return get_ValueAsInt("GuaranteeDaysMin");
        
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
    
    /** Set Image URL.
    @param ImageURL URL of image */
    public void setImageURL (String ImageURL)
    {
        set_Value ("ImageURL", ImageURL);
        
    }
    
    /** Get Image URL.
    @return URL of image */
    public String getImageURL() 
    {
        return (String)get_Value("ImageURL");
        
    }
    
    /** Set Bill of Materials.
    @param IsBOM Bill of Materials */
    public void setIsBOM (boolean IsBOM)
    {
        set_Value ("IsBOM", Boolean.valueOf(IsBOM));
        
    }
    
    /** Get Bill of Materials.
    @return Bill of Materials */
    public boolean isBOM() 
    {
        return get_ValueAsBoolean("IsBOM");
        
    }
    
    /** Set Drop Shipment.
    @param IsDropShip Drop Shipments are sent from the Vendor directly to the Customer */
    public void setIsDropShip (boolean IsDropShip)
    {
        set_Value ("IsDropShip", Boolean.valueOf(IsDropShip));
        
    }
    
    /** Get Drop Shipment.
    @return Drop Shipments are sent from the Vendor directly to the Customer */
    public boolean isDropShip() 
    {
        return get_ValueAsBoolean("IsDropShip");
        
    }
    
    /** Set Exclude Auto Delivery.
    @param IsExcludeAutoDelivery Exclude from automatic Delivery */
    public void setIsExcludeAutoDelivery (boolean IsExcludeAutoDelivery)
    {
        set_Value ("IsExcludeAutoDelivery", Boolean.valueOf(IsExcludeAutoDelivery));
        
    }
    
    /** Get Exclude Auto Delivery.
    @return Exclude from automatic Delivery */
    public boolean isExcludeAutoDelivery() 
    {
        return get_ValueAsBoolean("IsExcludeAutoDelivery");
        
    }
    
    /** Set Print detail records on invoice.
    @param IsInvoicePrintDetails Print detail BOM elements on the invoice */
    public void setIsInvoicePrintDetails (boolean IsInvoicePrintDetails)
    {
        set_Value ("IsInvoicePrintDetails", Boolean.valueOf(IsInvoicePrintDetails));
        
    }
    
    /** Get Print detail records on invoice.
    @return Print detail BOM elements on the invoice */
    public boolean isInvoicePrintDetails() 
    {
        return get_ValueAsBoolean("IsInvoicePrintDetails");
        
    }
    
    /** Set Print detail records on pick list.
    @param IsPickListPrintDetails Print detail BOM elements on the pick list */
    public void setIsPickListPrintDetails (boolean IsPickListPrintDetails)
    {
        set_Value ("IsPickListPrintDetails", Boolean.valueOf(IsPickListPrintDetails));
        
    }
    
    /** Get Print detail records on pick list.
    @return Print detail BOM elements on the pick list */
    public boolean isPickListPrintDetails() 
    {
        return get_ValueAsBoolean("IsPickListPrintDetails");
        
    }
    
    /** Set Purchased.
    @param IsPurchased Organization purchases this product */
    public void setIsPurchased (boolean IsPurchased)
    {
        set_Value ("IsPurchased", Boolean.valueOf(IsPurchased));
        
    }
    
    /** Get Purchased.
    @return Organization purchases this product */
    public boolean isPurchased() 
    {
        return get_ValueAsBoolean("IsPurchased");
        
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
    
    /** Set Sold.
    @param IsSold Organization sells this product */
    public void setIsSold (boolean IsSold)
    {
        set_Value ("IsSold", Boolean.valueOf(IsSold));
        
    }
    
    /** Get Sold.
    @return Organization sells this product */
    public boolean isSold() 
    {
        return get_ValueAsBoolean("IsSold");
        
    }
    
    /** Set Stocked.
    @param IsStocked Organization stocks this product */
    public void setIsStocked (boolean IsStocked)
    {
        set_Value ("IsStocked", Boolean.valueOf(IsStocked));
        
    }
    
    /** Get Stocked.
    @return Organization stocks this product */
    public boolean isStocked() 
    {
        return get_ValueAsBoolean("IsStocked");
        
    }
    
    /** Set Summary Level.
    @param IsSummary This is a summary entity */
    public void setIsSummary (boolean IsSummary)
    {
        set_Value ("IsSummary", Boolean.valueOf(IsSummary));
        
    }
    
    /** Get Summary Level.
    @return This is a summary entity */
    public boolean isSummary() 
    {
        return get_ValueAsBoolean("IsSummary");
        
    }
    
    /** Set Verified.
    @param IsVerified The BOM configuration has been verified */
    public void setIsVerified (boolean IsVerified)
    {
        set_ValueNoCheck ("IsVerified", Boolean.valueOf(IsVerified));
        
    }
    
    /** Get Verified.
    @return The BOM configuration has been verified */
    public boolean isVerified() 
    {
        return get_ValueAsBoolean("IsVerified");
        
    }
    
    /** Set Featured in Web Store.
    @param IsWebStoreFeatured If selected, the product is displayed in the initial or any empty search */
    public void setIsWebStoreFeatured (boolean IsWebStoreFeatured)
    {
        set_Value ("IsWebStoreFeatured", Boolean.valueOf(IsWebStoreFeatured));
        
    }
    
    /** Get Featured in Web Store.
    @return If selected, the product is displayed in the initial or any empty search */
    public boolean isWebStoreFeatured() 
    {
        return get_ValueAsBoolean("IsWebStoreFeatured");
        
    }
    
    /** Set License Info.
    @param LicenseInfo License Information */
    public void setLicenseInfo (String LicenseInfo)
    {
        set_Value ("LicenseInfo", LicenseInfo);
        
    }
    
    /** Get License Info.
    @return License Information */
    public String getLicenseInfo() 
    {
        return (String)get_Value("LicenseInfo");
        
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
    
    /** Set Attribute Set.
    @param M_AttributeSet_ID Product Attribute Set */
    public void setM_AttributeSet_ID (int M_AttributeSet_ID)
    {
        if (M_AttributeSet_ID <= 0) set_Value ("M_AttributeSet_ID", null);
        else
        set_Value ("M_AttributeSet_ID", Integer.valueOf(M_AttributeSet_ID));
        
    }
    
    /** Get Attribute Set.
    @return Product Attribute Set */
    public int getM_AttributeSet_ID() 
    {
        return get_ValueAsInt("M_AttributeSet_ID");
        
    }
    
    /** Set Freight Category.
    @param M_FreightCategory_ID Category of the Freight */
    public void setM_FreightCategory_ID (int M_FreightCategory_ID)
    {
        if (M_FreightCategory_ID <= 0) set_Value ("M_FreightCategory_ID", null);
        else
        set_Value ("M_FreightCategory_ID", Integer.valueOf(M_FreightCategory_ID));
        
    }
    
    /** Get Freight Category.
    @return Category of the Freight */
    public int getM_FreightCategory_ID() 
    {
        return get_ValueAsInt("M_FreightCategory_ID");
        
    }
    
    
    /** M_Locator_ID AD_Reference_ID=446 */
    public static final int M_LOCATOR_ID_AD_Reference_ID=446;
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
    
    
    /** M_Product_Category_ID AD_Reference_ID=163 */
    public static final int M_PRODUCT_CATEGORY_ID_AD_Reference_ID=163;
    /** Set Product Category.
    @param M_Product_Category_ID Category of a Product */
    public void setM_Product_Category_ID (int M_Product_Category_ID)
    {
        if (M_Product_Category_ID < 1) throw new IllegalArgumentException ("M_Product_Category_ID is mandatory.");
        set_Value ("M_Product_Category_ID", Integer.valueOf(M_Product_Category_ID));
        
    }
    
    /** Get Product Category.
    @return Category of a Product */
    public int getM_Product_Category_ID() 
    {
        return get_ValueAsInt("M_Product_Category_ID");
        
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
    
    /** Set Process Now.
    @param Processing Process Now */
    public void setProcessing (boolean Processing)
    {
        set_Value ("Processing", Boolean.valueOf(Processing));
        
    }
    
    /** Get Process Now.
    @return Process Now */
    public boolean isProcessing() 
    {
        return get_ValueAsBoolean("Processing");
        
    }
    
    
    /** ProductType AD_Reference_ID=270 */
    public static final int PRODUCTTYPE_AD_Reference_ID=270;
    /** Expense type = E */
    public static final String PRODUCTTYPE_ExpenseType = X_Ref_M_Product_ProductType.EXPENSE_TYPE.getValue();
    /** Item = I */
    public static final String PRODUCTTYPE_Item = X_Ref_M_Product_ProductType.ITEM.getValue();
    /** Online = O */
    public static final String PRODUCTTYPE_Online = X_Ref_M_Product_ProductType.ONLINE.getValue();
    /** Resource = R */
    public static final String PRODUCTTYPE_Resource = X_Ref_M_Product_ProductType.RESOURCE.getValue();
    /** Service = S */
    public static final String PRODUCTTYPE_Service = X_Ref_M_Product_ProductType.SERVICE.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isProductTypeValid(String test)
    {
         return X_Ref_M_Product_ProductType.isValid(test);
         
    }
    /** Set Product Type.
    @param ProductType Type of product */
    public void setProductType (String ProductType)
    {
        if (ProductType == null) throw new IllegalArgumentException ("ProductType is mandatory");
        if (!isProductTypeValid(ProductType))
        throw new IllegalArgumentException ("ProductType Invalid value - " + ProductType + " - Reference_ID=270 - E - I - O - R - S");
        set_Value ("ProductType", ProductType);
        
    }
    
    /** Get Product Type.
    @return Type of product */
    public String getProductType() 
    {
        return (String)get_Value("ProductType");
        
    }
    
    /** Set Mail Template.
    @param R_MailText_ID Text templates for mailings */
    public void setR_MailText_ID (int R_MailText_ID)
    {
        if (R_MailText_ID <= 0) set_Value ("R_MailText_ID", null);
        else
        set_Value ("R_MailText_ID", Integer.valueOf(R_MailText_ID));
        
    }
    
    /** Get Mail Template.
    @return Text templates for mailings */
    public int getR_MailText_ID() 
    {
        return get_ValueAsInt("R_MailText_ID");
        
    }
    
    /** Set Source.
    @param R_Source_ID Source for the Lead or Request */
    public void setR_Source_ID (int R_Source_ID)
    {
        if (R_Source_ID <= 0) set_Value ("R_Source_ID", null);
        else
        set_Value ("R_Source_ID", Integer.valueOf(R_Source_ID));
        
    }
    
    /** Get Source.
    @return Source for the Lead or Request */
    public int getR_Source_ID() 
    {
        return get_ValueAsInt("R_Source_ID");
        
    }
    
    /** Set SKU.
    @param SKU Stock Keeping Unit */
    public void setSKU (String SKU)
    {
        set_Value ("SKU", SKU);
        
    }
    
    /** Get SKU.
    @return Stock Keeping Unit */
    public String getSKU() 
    {
        return (String)get_Value("SKU");
        
    }
    
    /** Set Expense Type.
    @param S_ExpenseType_ID Expense report type */
    public void setS_ExpenseType_ID (int S_ExpenseType_ID)
    {
        if (S_ExpenseType_ID <= 0) set_ValueNoCheck ("S_ExpenseType_ID", null);
        else
        set_ValueNoCheck ("S_ExpenseType_ID", Integer.valueOf(S_ExpenseType_ID));
        
    }
    
    /** Get Expense Type.
    @return Expense report type */
    public int getS_ExpenseType_ID() 
    {
        return get_ValueAsInt("S_ExpenseType_ID");
        
    }
    
    /** Set Resource.
    @param S_Resource_ID Resource */
    public void setS_Resource_ID (int S_Resource_ID)
    {
        if (S_Resource_ID <= 0) set_ValueNoCheck ("S_Resource_ID", null);
        else
        set_ValueNoCheck ("S_Resource_ID", Integer.valueOf(S_Resource_ID));
        
    }
    
    /** Get Resource.
    @return Resource */
    public int getS_Resource_ID() 
    {
        return get_ValueAsInt("S_Resource_ID");
        
    }
    
    
    /** SalesRep_ID AD_Reference_ID=190 */
    public static final int SALESREP_ID_AD_Reference_ID=190;
    /** Set Representative.
    @param SalesRep_ID Company Agent like Sales Representative, Purchase Agent, and Customer Service Representative... */
    public void setSalesRep_ID (int SalesRep_ID)
    {
        if (SalesRep_ID <= 0) set_Value ("SalesRep_ID", null);
        else
        set_Value ("SalesRep_ID", Integer.valueOf(SalesRep_ID));
        
    }
    
    /** Get Representative.
    @return Company Agent like Sales Representative, Purchase Agent, and Customer Service Representative... */
    public int getSalesRep_ID() 
    {
        return get_ValueAsInt("SalesRep_ID");
        
    }
    
    /** Set Shelf Depth.
    @param ShelfDepth Shelf depth required */
    public void setShelfDepth (int ShelfDepth)
    {
        set_Value ("ShelfDepth", Integer.valueOf(ShelfDepth));
        
    }
    
    /** Get Shelf Depth.
    @return Shelf depth required */
    public int getShelfDepth() 
    {
        return get_ValueAsInt("ShelfDepth");
        
    }
    
    /** Set Shelf Height.
    @param ShelfHeight Shelf height required */
    public void setShelfHeight (int ShelfHeight)
    {
        set_Value ("ShelfHeight", Integer.valueOf(ShelfHeight));
        
    }
    
    /** Get Shelf Height.
    @return Shelf height required */
    public int getShelfHeight() 
    {
        return get_ValueAsInt("ShelfHeight");
        
    }
    
    /** Set Shelf Width.
    @param ShelfWidth Shelf width required */
    public void setShelfWidth (int ShelfWidth)
    {
        set_Value ("ShelfWidth", Integer.valueOf(ShelfWidth));
        
    }
    
    /** Get Shelf Width.
    @return Shelf width required */
    public int getShelfWidth() 
    {
        return get_ValueAsInt("ShelfWidth");
        
    }
    
    /** Set Support Units.
    @param SupportUnits Number of Support Units, e.g. Supported Internal Users */
    public void setSupportUnits (int SupportUnits)
    {
        set_Value ("SupportUnits", Integer.valueOf(SupportUnits));
        
    }
    
    /** Get Support Units.
    @return Number of Support Units, e.g. Supported Internal Users */
    public int getSupportUnits() 
    {
        return get_ValueAsInt("SupportUnits");
        
    }
    
    /** Set Trial Phase Days.
    @param TrialPhaseDays Days for a Trail */
    public void setTrialPhaseDays (int TrialPhaseDays)
    {
        set_Value ("TrialPhaseDays", Integer.valueOf(TrialPhaseDays));
        
    }
    
    /** Get Trial Phase Days.
    @return Days for a Trail */
    public int getTrialPhaseDays() 
    {
        return get_ValueAsInt("TrialPhaseDays");
        
    }
    
    /** Set UPC/EAN.
    @param UPC Bar Code (Universal Product Code or its superset European Article Number) */
    public void setUPC (String UPC)
    {
        set_Value ("UPC", UPC);
        
    }
    
    /** Get UPC/EAN.
    @return Bar Code (Universal Product Code or its superset European Article Number) */
    public String getUPC() 
    {
        return (String)get_Value("UPC");
        
    }
    
    /** Set Units per Pallet.
    @param UnitsPerPallet Units per Pallet */
    public void setUnitsPerPallet (int UnitsPerPallet)
    {
        set_Value ("UnitsPerPallet", Integer.valueOf(UnitsPerPallet));
        
    }
    
    /** Get Units per Pallet.
    @return Units per Pallet */
    public int getUnitsPerPallet() 
    {
        return get_ValueAsInt("UnitsPerPallet");
        
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
    
    /** Set Version No.
    @param VersionNo Version Number */
    public void setVersionNo (String VersionNo)
    {
        set_Value ("VersionNo", VersionNo);
        
    }
    
    /** Get Version No.
    @return Version Number */
    public String getVersionNo() 
    {
        return (String)get_Value("VersionNo");
        
    }
    
    /** Set Volume.
    @param Volume Volume of a product */
    public void setVolume (java.math.BigDecimal Volume)
    {
        set_Value ("Volume", Volume);
        
    }
    
    /** Get Volume.
    @return Volume of a product */
    public java.math.BigDecimal getVolume() 
    {
        return get_ValueAsBigDecimal("Volume");
        
    }
    
    /** Set Weight.
    @param Weight Weight of a product */
    public void setWeight (java.math.BigDecimal Weight)
    {
        set_Value ("Weight", Weight);
        
    }
    
    /** Get Weight.
    @return Weight of a product */
    public java.math.BigDecimal getWeight() 
    {
        return get_ValueAsBigDecimal("Weight");
        
    }
    
    
}
