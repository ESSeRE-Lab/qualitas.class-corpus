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
/** Generated Model for M_TaskList
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_TaskList extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_TaskList_ID id
    @param trx transaction
    */
    public X_M_TaskList (Ctx ctx, int M_TaskList_ID, Trx trx)
    {
        super (ctx, M_TaskList_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_TaskList_ID == 0)
        {
            setC_DocType_ID (0);
            setM_TaskList_ID (0);
            setM_Warehouse_ID (0);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_TaskList (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27503883373789L;
    /** Last Updated Timestamp 2008-09-18 10:14:17.0 */
    public static final long updatedMS = 1221758057000L;
    /** AD_Table_ID=1023 */
    public static final int Table_ID=1023;
    
    /** TableName=M_TaskList */
    public static final String Table_Name="M_TaskList";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_TaskList");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    
    /** AD_User_ID AD_Reference_ID=286 */
    public static final int AD_USER_ID_AD_Reference_ID=286;
    /** Set User/Contact.
    @param AD_User_ID User within the system - Internal or Business Partner Contact */
    public void setAD_User_ID (int AD_User_ID)
    {
        if (AD_User_ID <= 0) set_Value ("AD_User_ID", null);
        else
        set_Value ("AD_User_ID", Integer.valueOf(AD_User_ID));
        
    }
    
    /** Get User/Contact.
    @return User within the system - Internal or Business Partner Contact */
    public int getAD_User_ID() 
    {
        return get_ValueAsInt("AD_User_ID");
        
    }
    
    /** Set Document Type.
    @param C_DocType_ID Document type or rules */
    public void setC_DocType_ID (int C_DocType_ID)
    {
        if (C_DocType_ID < 0) throw new IllegalArgumentException ("C_DocType_ID is mandatory.");
        set_Value ("C_DocType_ID", Integer.valueOf(C_DocType_ID));
        
    }
    
    /** Get Document Type.
    @return Document type or rules */
    public int getC_DocType_ID() 
    {
        return get_ValueAsInt("C_DocType_ID");
        
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
    
    
    /** DocBaseType AD_Reference_ID=432 */
    public static final int DOCBASETYPE_AD_Reference_ID=432;
    /** Set Document BaseType.
    @param DocBaseType Logical type of document */
    public void setDocBaseType (String DocBaseType)
    {
        throw new IllegalArgumentException ("DocBaseType is virtual column");
        
    }
    
    /** Get Document BaseType.
    @return Logical type of document */
    public String getDocBaseType() 
    {
        return (String)get_Value("DocBaseType");
        
    }
    
    /** Set Document No.
    @param DocumentNo Document sequence number of the document */
    public void setDocumentNo (String DocumentNo)
    {
        set_Value ("DocumentNo", DocumentNo);
        
    }
    
    /** Get Document No.
    @return Document sequence number of the document */
    public String getDocumentNo() 
    {
        return (String)get_Value("DocumentNo");
        
    }
    
    /** Set Task List.
    @param M_TaskList_ID List of Warehouse Tasks */
    public void setM_TaskList_ID (int M_TaskList_ID)
    {
        if (M_TaskList_ID < 1) throw new IllegalArgumentException ("M_TaskList_ID is mandatory.");
        set_ValueNoCheck ("M_TaskList_ID", Integer.valueOf(M_TaskList_ID));
        
    }
    
    /** Get Task List.
    @return List of Warehouse Tasks */
    public int getM_TaskList_ID() 
    {
        return get_ValueAsInt("M_TaskList_ID");
        
    }
    
    /** Set Warehouse.
    @param M_Warehouse_ID Storage Warehouse and Service Point */
    public void setM_Warehouse_ID (int M_Warehouse_ID)
    {
        if (M_Warehouse_ID < 1) throw new IllegalArgumentException ("M_Warehouse_ID is mandatory.");
        set_Value ("M_Warehouse_ID", Integer.valueOf(M_Warehouse_ID));
        
    }
    
    /** Get Warehouse.
    @return Storage Warehouse and Service Point */
    public int getM_Warehouse_ID() 
    {
        return get_ValueAsInt("M_Warehouse_ID");
        
    }
    
    
    /** PickMethod AD_Reference_ID=474 */
    public static final int PICKMETHOD_AD_Reference_ID=474;
    /** Cluster Picking = C */
    public static final String PICKMETHOD_ClusterPicking = X_Ref_PickMethod.CLUSTER_PICKING.getValue();
    /** Order Picking = O */
    public static final String PICKMETHOD_OrderPicking = X_Ref_PickMethod.ORDER_PICKING.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isPickMethodValid(String test)
    {
         return X_Ref_PickMethod.isValid(test);
         
    }
    /** Set Pick Method.
    @param PickMethod Pick method to be used when generating pick lists */
    public void setPickMethod (String PickMethod)
    {
        if (!isPickMethodValid(PickMethod))
        throw new IllegalArgumentException ("PickMethod Invalid value - " + PickMethod + " - Reference_ID=474 - C - O");
        set_Value ("PickMethod", PickMethod);
        
    }
    
    /** Get Pick Method.
    @return Pick method to be used when generating pick lists */
    public String getPickMethod() 
    {
        return (String)get_Value("PickMethod");
        
    }
    
    
}
