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
/** Generated Model for C_ValidCombination
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_C_ValidCombination extends PO
{
    /** Standard Constructor
    @param ctx context
    @param C_ValidCombination_ID id
    @param trx transaction
    */
    public X_C_ValidCombination (Ctx ctx, int C_ValidCombination_ID, Trx trx)
    {
        super (ctx, C_ValidCombination_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (C_ValidCombination_ID == 0)
        {
            setAccount_ID (0);
            setC_AcctSchema_ID (0);
            setC_ValidCombination_ID (0);
            setIsFullyQualified (false);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_C_ValidCombination (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27511661206789L;
    /** Last Updated Timestamp 2008-12-17 09:44:50.0 */
    public static final long updatedMS = 1229535890000L;
    /** AD_Table_ID=176 */
    public static final int Table_ID=176;
    
    /** TableName=C_ValidCombination */
    public static final String Table_Name="C_ValidCombination";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"C_ValidCombination");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    
    /** AD_OrgTrx_ID AD_Reference_ID=130 */
    public static final int AD_ORGTRX_ID_AD_Reference_ID=130;
    /** Set Trx Organization.
    @param AD_OrgTrx_ID Performing or initiating organization */
    public void setAD_OrgTrx_ID (int AD_OrgTrx_ID)
    {
        if (AD_OrgTrx_ID <= 0) set_ValueNoCheck ("AD_OrgTrx_ID", null);
        else
        set_ValueNoCheck ("AD_OrgTrx_ID", Integer.valueOf(AD_OrgTrx_ID));
        
    }
    
    /** Get Trx Organization.
    @return Performing or initiating organization */
    public int getAD_OrgTrx_ID() 
    {
        return get_ValueAsInt("AD_OrgTrx_ID");
        
    }
    
    
    /** Account_ID AD_Reference_ID=362 */
    public static final int ACCOUNT_ID_AD_Reference_ID=362;
    /** Set Account.
    @param Account_ID Account used */
    public void setAccount_ID (int Account_ID)
    {
        if (Account_ID < 1) throw new IllegalArgumentException ("Account_ID is mandatory.");
        set_ValueNoCheck ("Account_ID", Integer.valueOf(Account_ID));
        
    }
    
    /** Get Account.
    @return Account used */
    public int getAccount_ID() 
    {
        return get_ValueAsInt("Account_ID");
        
    }
    
    /** Set Alias.
    @param Alias Defines an alternate method of indicating an account combination. */
    public void setAlias (String Alias)
    {
        set_Value ("Alias", Alias);
        
    }
    
    /** Get Alias.
    @return Defines an alternate method of indicating an account combination. */
    public String getAlias() 
    {
        return (String)get_Value("Alias");
        
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
    
    
    /** C_Activity_ID AD_Reference_ID=142 */
    public static final int C_ACTIVITY_ID_AD_Reference_ID=142;
    /** Set Activity.
    @param C_Activity_ID Business Activity */
    public void setC_Activity_ID (int C_Activity_ID)
    {
        if (C_Activity_ID <= 0) set_ValueNoCheck ("C_Activity_ID", null);
        else
        set_ValueNoCheck ("C_Activity_ID", Integer.valueOf(C_Activity_ID));
        
    }
    
    /** Get Activity.
    @return Business Activity */
    public int getC_Activity_ID() 
    {
        return get_ValueAsInt("C_Activity_ID");
        
    }
    
    
    /** C_BPartner_ID AD_Reference_ID=138 */
    public static final int C_BPARTNER_ID_AD_Reference_ID=138;
    /** Set Business Partner.
    @param C_BPartner_ID Identifies a Business Partner */
    public void setC_BPartner_ID (int C_BPartner_ID)
    {
        if (C_BPartner_ID <= 0) set_ValueNoCheck ("C_BPartner_ID", null);
        else
        set_ValueNoCheck ("C_BPartner_ID", Integer.valueOf(C_BPartner_ID));
        
    }
    
    /** Get Business Partner.
    @return Identifies a Business Partner */
    public int getC_BPartner_ID() 
    {
        return get_ValueAsInt("C_BPartner_ID");
        
    }
    
    
    /** C_Campaign_ID AD_Reference_ID=143 */
    public static final int C_CAMPAIGN_ID_AD_Reference_ID=143;
    /** Set Campaign.
    @param C_Campaign_ID Marketing Campaign */
    public void setC_Campaign_ID (int C_Campaign_ID)
    {
        if (C_Campaign_ID <= 0) set_ValueNoCheck ("C_Campaign_ID", null);
        else
        set_ValueNoCheck ("C_Campaign_ID", Integer.valueOf(C_Campaign_ID));
        
    }
    
    /** Get Campaign.
    @return Marketing Campaign */
    public int getC_Campaign_ID() 
    {
        return get_ValueAsInt("C_Campaign_ID");
        
    }
    
    
    /** C_LocFrom_ID AD_Reference_ID=133 */
    public static final int C_LOCFROM_ID_AD_Reference_ID=133;
    /** Set Location From.
    @param C_LocFrom_ID Location that inventory was moved from */
    public void setC_LocFrom_ID (int C_LocFrom_ID)
    {
        if (C_LocFrom_ID <= 0) set_ValueNoCheck ("C_LocFrom_ID", null);
        else
        set_ValueNoCheck ("C_LocFrom_ID", Integer.valueOf(C_LocFrom_ID));
        
    }
    
    /** Get Location From.
    @return Location that inventory was moved from */
    public int getC_LocFrom_ID() 
    {
        return get_ValueAsInt("C_LocFrom_ID");
        
    }
    
    
    /** C_LocTo_ID AD_Reference_ID=133 */
    public static final int C_LOCTO_ID_AD_Reference_ID=133;
    /** Set Location To.
    @param C_LocTo_ID Location that inventory was moved to */
    public void setC_LocTo_ID (int C_LocTo_ID)
    {
        if (C_LocTo_ID <= 0) set_ValueNoCheck ("C_LocTo_ID", null);
        else
        set_ValueNoCheck ("C_LocTo_ID", Integer.valueOf(C_LocTo_ID));
        
    }
    
    /** Get Location To.
    @return Location that inventory was moved to */
    public int getC_LocTo_ID() 
    {
        return get_ValueAsInt("C_LocTo_ID");
        
    }
    
    
    /** C_Project_ID AD_Reference_ID=141 */
    public static final int C_PROJECT_ID_AD_Reference_ID=141;
    /** Set Project.
    @param C_Project_ID Financial Project */
    public void setC_Project_ID (int C_Project_ID)
    {
        if (C_Project_ID <= 0) set_ValueNoCheck ("C_Project_ID", null);
        else
        set_ValueNoCheck ("C_Project_ID", Integer.valueOf(C_Project_ID));
        
    }
    
    /** Get Project.
    @return Financial Project */
    public int getC_Project_ID() 
    {
        return get_ValueAsInt("C_Project_ID");
        
    }
    
    
    /** C_SalesRegion_ID AD_Reference_ID=144 */
    public static final int C_SALESREGION_ID_AD_Reference_ID=144;
    /** Set Sales Region.
    @param C_SalesRegion_ID Sales coverage region */
    public void setC_SalesRegion_ID (int C_SalesRegion_ID)
    {
        if (C_SalesRegion_ID <= 0) set_ValueNoCheck ("C_SalesRegion_ID", null);
        else
        set_ValueNoCheck ("C_SalesRegion_ID", Integer.valueOf(C_SalesRegion_ID));
        
    }
    
    /** Get Sales Region.
    @return Sales coverage region */
    public int getC_SalesRegion_ID() 
    {
        return get_ValueAsInt("C_SalesRegion_ID");
        
    }
    
    /** Set Sub Account.
    @param C_SubAcct_ID Sub account for Element Value */
    public void setC_SubAcct_ID (int C_SubAcct_ID)
    {
        if (C_SubAcct_ID <= 0) set_ValueNoCheck ("C_SubAcct_ID", null);
        else
        set_ValueNoCheck ("C_SubAcct_ID", Integer.valueOf(C_SubAcct_ID));
        
    }
    
    /** Get Sub Account.
    @return Sub account for Element Value */
    public int getC_SubAcct_ID() 
    {
        return get_ValueAsInt("C_SubAcct_ID");
        
    }
    
    /** Set Combination.
    @param C_ValidCombination_ID Valid Account Combination */
    public void setC_ValidCombination_ID (int C_ValidCombination_ID)
    {
        if (C_ValidCombination_ID < 1) throw new IllegalArgumentException ("C_ValidCombination_ID is mandatory.");
        set_ValueNoCheck ("C_ValidCombination_ID", Integer.valueOf(C_ValidCombination_ID));
        
    }
    
    /** Get Combination.
    @return Valid Account Combination */
    public int getC_ValidCombination_ID() 
    {
        return get_ValueAsInt("C_ValidCombination_ID");
        
    }
    
    /** Set Combination.
    @param Combination Unique combination of account elements */
    public void setCombination (String Combination)
    {
        set_ValueNoCheck ("Combination", Combination);
        
    }
    
    /** Get Combination.
    @return Unique combination of account elements */
    public String getCombination() 
    {
        return (String)get_Value("Combination");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getCombination());
        
    }
    
    /** Set Description.
    @param Description Optional short description of the record */
    public void setDescription (String Description)
    {
        set_ValueNoCheck ("Description", Description);
        
    }
    
    /** Get Description.
    @return Optional short description of the record */
    public String getDescription() 
    {
        return (String)get_Value("Description");
        
    }
    
    /** Set Fully Qualified.
    @param IsFullyQualified This account is fully qualified */
    public void setIsFullyQualified (boolean IsFullyQualified)
    {
        set_ValueNoCheck ("IsFullyQualified", Boolean.valueOf(IsFullyQualified));
        
    }
    
    /** Get Fully Qualified.
    @return This account is fully qualified */
    public boolean isFullyQualified() 
    {
        return get_ValueAsBoolean("IsFullyQualified");
        
    }
    
    
    /** M_Product_ID AD_Reference_ID=162 */
    public static final int M_PRODUCT_ID_AD_Reference_ID=162;
    /** Set Product.
    @param M_Product_ID Product, Service, Item */
    public void setM_Product_ID (int M_Product_ID)
    {
        if (M_Product_ID <= 0) set_ValueNoCheck ("M_Product_ID", null);
        else
        set_ValueNoCheck ("M_Product_ID", Integer.valueOf(M_Product_ID));
        
    }
    
    /** Get Product.
    @return Product, Service, Item */
    public int getM_Product_ID() 
    {
        return get_ValueAsInt("M_Product_ID");
        
    }
    
    
    /** User1_ID AD_Reference_ID=134 */
    public static final int USER1_ID_AD_Reference_ID=134;
    /** Set User List 1.
    @param User1_ID User defined list element #1 */
    public void setUser1_ID (int User1_ID)
    {
        if (User1_ID <= 0) set_ValueNoCheck ("User1_ID", null);
        else
        set_ValueNoCheck ("User1_ID", Integer.valueOf(User1_ID));
        
    }
    
    /** Get User List 1.
    @return User defined list element #1 */
    public int getUser1_ID() 
    {
        return get_ValueAsInt("User1_ID");
        
    }
    
    
    /** User2_ID AD_Reference_ID=137 */
    public static final int USER2_ID_AD_Reference_ID=137;
    /** Set User List 2.
    @param User2_ID User defined list element #2 */
    public void setUser2_ID (int User2_ID)
    {
        if (User2_ID <= 0) set_ValueNoCheck ("User2_ID", null);
        else
        set_ValueNoCheck ("User2_ID", Integer.valueOf(User2_ID));
        
    }
    
    /** Get User List 2.
    @return User defined list element #2 */
    public int getUser2_ID() 
    {
        return get_ValueAsInt("User2_ID");
        
    }
    
    /** Set User Element 1.
    @param UserElement1_ID User defined accounting Element */
    public void setUserElement1_ID (int UserElement1_ID)
    {
        if (UserElement1_ID <= 0) set_Value ("UserElement1_ID", null);
        else
        set_Value ("UserElement1_ID", Integer.valueOf(UserElement1_ID));
        
    }
    
    /** Get User Element 1.
    @return User defined accounting Element */
    public int getUserElement1_ID() 
    {
        return get_ValueAsInt("UserElement1_ID");
        
    }
    
    /** Set User Element 2.
    @param UserElement2_ID User defined accounting Element */
    public void setUserElement2_ID (int UserElement2_ID)
    {
        if (UserElement2_ID <= 0) set_Value ("UserElement2_ID", null);
        else
        set_Value ("UserElement2_ID", Integer.valueOf(UserElement2_ID));
        
    }
    
    /** Get User Element 2.
    @return User defined accounting Element */
    public int getUserElement2_ID() 
    {
        return get_ValueAsInt("UserElement2_ID");
        
    }
    
    
}
