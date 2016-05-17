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
/** Generated Model for M_WorkOrderClass
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_WorkOrderClass extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_WorkOrderClass_ID id
    @param trx transaction
    */
    public X_M_WorkOrderClass (Ctx ctx, int M_WorkOrderClass_ID, Trx trx)
    {
        super (ctx, M_WorkOrderClass_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_WorkOrderClass_ID == 0)
        {
            setIsDefault (false);	// N
            setM_WorkOrderClass_ID (0);
            setName (null);
            setWOT_DocType_ID (0);
            setWO_DocType_ID (0);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_WorkOrderClass (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27497605339789L;
    /** Last Updated Timestamp 2008-07-07 18:20:23.0 */
    public static final long updatedMS = 1215480023000L;
    /** AD_Table_ID=1056 */
    public static final int Table_ID=1056;
    
    /** TableName=M_WorkOrderClass */
    public static final String Table_Name="M_WorkOrderClass";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_WorkOrderClass");
    
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
    
    /** Set Work Order Class.
    @param M_WorkOrderClass_ID Indicates the document types and accounts to be used for a work order */
    public void setM_WorkOrderClass_ID (int M_WorkOrderClass_ID)
    {
        if (M_WorkOrderClass_ID < 1) throw new IllegalArgumentException ("M_WorkOrderClass_ID is mandatory.");
        set_ValueNoCheck ("M_WorkOrderClass_ID", Integer.valueOf(M_WorkOrderClass_ID));
        
    }
    
    /** Get Work Order Class.
    @return Indicates the document types and accounts to be used for a work order */
    public int getM_WorkOrderClass_ID() 
    {
        return get_ValueAsInt("M_WorkOrderClass_ID");
        
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
    
    
    /** WOT_DocType_ID AD_Reference_ID=170 */
    public static final int WOT_DOCTYPE_ID_AD_Reference_ID=170;
    /** Set Transaction Document Type.
    @param WOT_DocType_ID Transaction Document Type */
    public void setWOT_DocType_ID (int WOT_DocType_ID)
    {
        if (WOT_DocType_ID < 1) throw new IllegalArgumentException ("WOT_DocType_ID is mandatory.");
        set_ValueNoCheck ("WOT_DocType_ID", Integer.valueOf(WOT_DocType_ID));
        
    }
    
    /** Get Transaction Document Type.
    @return Transaction Document Type */
    public int getWOT_DocType_ID() 
    {
        return get_ValueAsInt("WOT_DocType_ID");
        
    }
    
    
    /** WOType AD_Reference_ID=450 */
    public static final int WOTYPE_AD_Reference_ID=450;
    /** Refurbish = F */
    public static final String WOTYPE_Refurbish = X_Ref_M_WorkOrder_Type.REFURBISH.getValue();
    /** Repair = R */
    public static final String WOTYPE_Repair = X_Ref_M_WorkOrder_Type.REPAIR.getValue();
    /** Standard = S */
    public static final String WOTYPE_Standard = X_Ref_M_WorkOrder_Type.STANDARD.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isWOTypeValid(String test)
    {
         return X_Ref_M_WorkOrder_Type.isValid(test);
         
    }
    /** Set Work Order Type.
    @param WOType Work Order Type */
    public void setWOType (String WOType)
    {
        if (!isWOTypeValid(WOType))
        throw new IllegalArgumentException ("WOType Invalid value - " + WOType + " - Reference_ID=450 - F - R - S");
        set_ValueNoCheck ("WOType", WOType);
        
    }
    
    /** Get Work Order Type.
    @return Work Order Type */
    public String getWOType() 
    {
        return (String)get_Value("WOType");
        
    }
    
    
    /** WO_DocType_ID AD_Reference_ID=170 */
    public static final int WO_DOCTYPE_ID_AD_Reference_ID=170;
    /** Set Document Type.
    @param WO_DocType_ID Document Type */
    public void setWO_DocType_ID (int WO_DocType_ID)
    {
        if (WO_DocType_ID < 1) throw new IllegalArgumentException ("WO_DocType_ID is mandatory.");
        set_ValueNoCheck ("WO_DocType_ID", Integer.valueOf(WO_DocType_ID));
        
    }
    
    /** Get Document Type.
    @return Document Type */
    public int getWO_DocType_ID() 
    {
        return get_ValueAsInt("WO_DocType_ID");
        
    }
    
    
}
