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
/** Generated Model for PA_Hierarchy
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_PA_Hierarchy extends PO
{
    /** Standard Constructor
    @param ctx context
    @param PA_Hierarchy_ID id
    @param trx transaction
    */
    public X_PA_Hierarchy (Ctx ctx, int PA_Hierarchy_ID, Trx trx)
    {
        super (ctx, PA_Hierarchy_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (PA_Hierarchy_ID == 0)
        {
            setAD_Tree_Account_ID (0);
            setAD_Tree_Activity_ID (0);
            setAD_Tree_BPartner_ID (0);
            setAD_Tree_Campaign_ID (0);
            setAD_Tree_Org_ID (0);
            setAD_Tree_Product_ID (0);
            setAD_Tree_Project_ID (0);
            setAD_Tree_SalesRegion_ID (0);
            setName (null);
            setPA_Hierarchy_ID (0);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_PA_Hierarchy (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=821 */
    public static final int Table_ID=821;
    
    /** TableName=PA_Hierarchy */
    public static final String Table_Name="PA_Hierarchy";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"PA_Hierarchy");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    
    /** AD_Tree_Account_ID AD_Reference_ID=184 */
    public static final int AD_TREE_ACCOUNT_ID_AD_Reference_ID=184;
    /** Set Account Tree.
    @param AD_Tree_Account_ID Tree for Natural Account Tree */
    public void setAD_Tree_Account_ID (int AD_Tree_Account_ID)
    {
        if (AD_Tree_Account_ID < 1) throw new IllegalArgumentException ("AD_Tree_Account_ID is mandatory.");
        set_Value ("AD_Tree_Account_ID", Integer.valueOf(AD_Tree_Account_ID));
        
    }
    
    /** Get Account Tree.
    @return Tree for Natural Account Tree */
    public int getAD_Tree_Account_ID() 
    {
        return get_ValueAsInt("AD_Tree_Account_ID");
        
    }
    
    
    /** AD_Tree_Activity_ID AD_Reference_ID=184 */
    public static final int AD_TREE_ACTIVITY_ID_AD_Reference_ID=184;
    /** Set Activity Tree.
    @param AD_Tree_Activity_ID Tree to determine activity hierarchy */
    public void setAD_Tree_Activity_ID (int AD_Tree_Activity_ID)
    {
        if (AD_Tree_Activity_ID < 1) throw new IllegalArgumentException ("AD_Tree_Activity_ID is mandatory.");
        set_Value ("AD_Tree_Activity_ID", Integer.valueOf(AD_Tree_Activity_ID));
        
    }
    
    /** Get Activity Tree.
    @return Tree to determine activity hierarchy */
    public int getAD_Tree_Activity_ID() 
    {
        return get_ValueAsInt("AD_Tree_Activity_ID");
        
    }
    
    
    /** AD_Tree_BPartner_ID AD_Reference_ID=184 */
    public static final int AD_TREE_BPARTNER_ID_AD_Reference_ID=184;
    /** Set BPartner Tree.
    @param AD_Tree_BPartner_ID Tree to determine business partner hierarchy */
    public void setAD_Tree_BPartner_ID (int AD_Tree_BPartner_ID)
    {
        if (AD_Tree_BPartner_ID < 1) throw new IllegalArgumentException ("AD_Tree_BPartner_ID is mandatory.");
        set_Value ("AD_Tree_BPartner_ID", Integer.valueOf(AD_Tree_BPartner_ID));
        
    }
    
    /** Get BPartner Tree.
    @return Tree to determine business partner hierarchy */
    public int getAD_Tree_BPartner_ID() 
    {
        return get_ValueAsInt("AD_Tree_BPartner_ID");
        
    }
    
    
    /** AD_Tree_Campaign_ID AD_Reference_ID=184 */
    public static final int AD_TREE_CAMPAIGN_ID_AD_Reference_ID=184;
    /** Set Campaign Tree.
    @param AD_Tree_Campaign_ID Tree to determine marketing campaign hierarchy */
    public void setAD_Tree_Campaign_ID (int AD_Tree_Campaign_ID)
    {
        if (AD_Tree_Campaign_ID < 1) throw new IllegalArgumentException ("AD_Tree_Campaign_ID is mandatory.");
        set_Value ("AD_Tree_Campaign_ID", Integer.valueOf(AD_Tree_Campaign_ID));
        
    }
    
    /** Get Campaign Tree.
    @return Tree to determine marketing campaign hierarchy */
    public int getAD_Tree_Campaign_ID() 
    {
        return get_ValueAsInt("AD_Tree_Campaign_ID");
        
    }
    
    
    /** AD_Tree_Org_ID AD_Reference_ID=184 */
    public static final int AD_TREE_ORG_ID_AD_Reference_ID=184;
    /** Set Organization Tree.
    @param AD_Tree_Org_ID Tree to determine organizational hierarchy */
    public void setAD_Tree_Org_ID (int AD_Tree_Org_ID)
    {
        if (AD_Tree_Org_ID < 1) throw new IllegalArgumentException ("AD_Tree_Org_ID is mandatory.");
        set_Value ("AD_Tree_Org_ID", Integer.valueOf(AD_Tree_Org_ID));
        
    }
    
    /** Get Organization Tree.
    @return Tree to determine organizational hierarchy */
    public int getAD_Tree_Org_ID() 
    {
        return get_ValueAsInt("AD_Tree_Org_ID");
        
    }
    
    
    /** AD_Tree_Product_ID AD_Reference_ID=184 */
    public static final int AD_TREE_PRODUCT_ID_AD_Reference_ID=184;
    /** Set Product Tree.
    @param AD_Tree_Product_ID Tree to determine product hierarchy */
    public void setAD_Tree_Product_ID (int AD_Tree_Product_ID)
    {
        if (AD_Tree_Product_ID < 1) throw new IllegalArgumentException ("AD_Tree_Product_ID is mandatory.");
        set_Value ("AD_Tree_Product_ID", Integer.valueOf(AD_Tree_Product_ID));
        
    }
    
    /** Get Product Tree.
    @return Tree to determine product hierarchy */
    public int getAD_Tree_Product_ID() 
    {
        return get_ValueAsInt("AD_Tree_Product_ID");
        
    }
    
    
    /** AD_Tree_Project_ID AD_Reference_ID=184 */
    public static final int AD_TREE_PROJECT_ID_AD_Reference_ID=184;
    /** Set Project Tree.
    @param AD_Tree_Project_ID Tree to determine project hierarchy */
    public void setAD_Tree_Project_ID (int AD_Tree_Project_ID)
    {
        if (AD_Tree_Project_ID < 1) throw new IllegalArgumentException ("AD_Tree_Project_ID is mandatory.");
        set_Value ("AD_Tree_Project_ID", Integer.valueOf(AD_Tree_Project_ID));
        
    }
    
    /** Get Project Tree.
    @return Tree to determine project hierarchy */
    public int getAD_Tree_Project_ID() 
    {
        return get_ValueAsInt("AD_Tree_Project_ID");
        
    }
    
    
    /** AD_Tree_SalesRegion_ID AD_Reference_ID=184 */
    public static final int AD_TREE_SALESREGION_ID_AD_Reference_ID=184;
    /** Set Sales Region Tree.
    @param AD_Tree_SalesRegion_ID Tree to determine sales regional hierarchy */
    public void setAD_Tree_SalesRegion_ID (int AD_Tree_SalesRegion_ID)
    {
        if (AD_Tree_SalesRegion_ID < 1) throw new IllegalArgumentException ("AD_Tree_SalesRegion_ID is mandatory.");
        set_Value ("AD_Tree_SalesRegion_ID", Integer.valueOf(AD_Tree_SalesRegion_ID));
        
    }
    
    /** Get Sales Region Tree.
    @return Tree to determine sales regional hierarchy */
    public int getAD_Tree_SalesRegion_ID() 
    {
        return get_ValueAsInt("AD_Tree_SalesRegion_ID");
        
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
    
    /** Set Reporting Hierarchy.
    @param PA_Hierarchy_ID Optional Reporting Hierarchy - If not selected the default hierarchy trees are used. */
    public void setPA_Hierarchy_ID (int PA_Hierarchy_ID)
    {
        if (PA_Hierarchy_ID < 1) throw new IllegalArgumentException ("PA_Hierarchy_ID is mandatory.");
        set_ValueNoCheck ("PA_Hierarchy_ID", Integer.valueOf(PA_Hierarchy_ID));
        
    }
    
    /** Get Reporting Hierarchy.
    @return Optional Reporting Hierarchy - If not selected the default hierarchy trees are used. */
    public int getPA_Hierarchy_ID() 
    {
        return get_ValueAsInt("PA_Hierarchy_ID");
        
    }
    
    
}
