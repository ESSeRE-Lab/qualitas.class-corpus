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
/** Generated Model for CM_ChatTypeUpdate
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_CM_ChatTypeUpdate extends PO
{
    /** Standard Constructor
    @param ctx context
    @param CM_ChatTypeUpdate_ID id
    @param trx transaction
    */
    public X_CM_ChatTypeUpdate (Ctx ctx, int CM_ChatTypeUpdate_ID, Trx trx)
    {
        super (ctx, CM_ChatTypeUpdate_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (CM_ChatTypeUpdate_ID == 0)
        {
            setAD_User_ID (0);
            setCM_ChatType_ID (0);
            setIsSelfService (false);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_CM_ChatTypeUpdate (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=875 */
    public static final int Table_ID=875;
    
    /** TableName=CM_ChatTypeUpdate */
    public static final String Table_Name="CM_ChatTypeUpdate";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"CM_ChatTypeUpdate");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set User/Contact.
    @param AD_User_ID User within the system - Internal or Business Partner Contact */
    public void setAD_User_ID (int AD_User_ID)
    {
        if (AD_User_ID < 1) throw new IllegalArgumentException ("AD_User_ID is mandatory.");
        set_ValueNoCheck ("AD_User_ID", Integer.valueOf(AD_User_ID));
        
    }
    
    /** Get User/Contact.
    @return User within the system - Internal or Business Partner Contact */
    public int getAD_User_ID() 
    {
        return get_ValueAsInt("AD_User_ID");
        
    }
    
    /** Set Chat Type.
    @param CM_ChatType_ID Type of discussion / chat */
    public void setCM_ChatType_ID (int CM_ChatType_ID)
    {
        if (CM_ChatType_ID < 1) throw new IllegalArgumentException ("CM_ChatType_ID is mandatory.");
        set_ValueNoCheck ("CM_ChatType_ID", Integer.valueOf(CM_ChatType_ID));
        
    }
    
    /** Get Chat Type.
    @return Type of discussion / chat */
    public int getCM_ChatType_ID() 
    {
        return get_ValueAsInt("CM_ChatType_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getCM_ChatType_ID()));
        
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
    
    
}
