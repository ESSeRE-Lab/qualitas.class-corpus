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
/** Generated Model for AD_BView_Access
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_AD_BView_Access extends PO
{
    /** Standard Constructor
    @param ctx context
    @param AD_BView_Access_ID id
    @param trx transaction
    */
    public X_AD_BView_Access (Ctx ctx, int AD_BView_Access_ID, Trx trx)
    {
        super (ctx, AD_BView_Access_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (AD_BView_Access_ID == 0)
        {
            setAD_BView_Access_ID (0);
            setAD_BView_ID (0);
            setIsReadWrite (false);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_AD_BView_Access (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27499671245789L;
    /** Last Updated Timestamp 2008-07-31 16:12:09.0 */
    public static final long updatedMS = 1217545929000L;
    /** AD_Table_ID=1047 */
    public static final int Table_ID=1047;
    
    /** TableName=AD_BView_Access */
    public static final String Table_Name="AD_BView_Access";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"AD_BView_Access");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set AD_BView_Access_ID.
    @param AD_BView_Access_ID Identifier of the type af access allowed to a Business View */
    public void setAD_BView_Access_ID (int AD_BView_Access_ID)
    {
        if (AD_BView_Access_ID < 1) throw new IllegalArgumentException ("AD_BView_Access_ID is mandatory.");
        set_ValueNoCheck ("AD_BView_Access_ID", Integer.valueOf(AD_BView_Access_ID));
        
    }
    
    /** Get AD_BView_Access_ID.
    @return Identifier of the type af access allowed to a Business View */
    public int getAD_BView_Access_ID() 
    {
        return get_ValueAsInt("AD_BView_Access_ID");
        
    }
    
    /** Set Business View.
    @param AD_BView_ID The logical subset of related data for the purposes of reporting */
    public void setAD_BView_ID (int AD_BView_ID)
    {
        if (AD_BView_ID < 1) throw new IllegalArgumentException ("AD_BView_ID is mandatory.");
        set_ValueNoCheck ("AD_BView_ID", Integer.valueOf(AD_BView_ID));
        
    }
    
    /** Get Business View.
    @return The logical subset of related data for the purposes of reporting */
    public int getAD_BView_ID() 
    {
        return get_ValueAsInt("AD_BView_ID");
        
    }
    
    /** Set Role.
    @param AD_Role_ID Responsibility Role */
    public void setAD_Role_ID (int AD_Role_ID)
    {
        if (AD_Role_ID <= 0) set_Value ("AD_Role_ID", null);
        else
        set_Value ("AD_Role_ID", Integer.valueOf(AD_Role_ID));
        
    }
    
    /** Get Role.
    @return Responsibility Role */
    public int getAD_Role_ID() 
    {
        return get_ValueAsInt("AD_Role_ID");
        
    }
    
    
    /** EntityType AD_Reference_ID=389 */
    public static final int ENTITYTYPE_AD_Reference_ID=389;
    /** Set Entity Type.
    @param EntityType Dictionary Entity Type;
     Determines ownership and synchronization */
    public void setEntityType (String EntityType)
    {
        set_Value ("EntityType", EntityType);
        
    }
    
    /** Get Entity Type.
    @return Dictionary Entity Type;
     Determines ownership and synchronization */
    public String getEntityType() 
    {
        return (String)get_Value("EntityType");
        
    }
    
    /** Set Read Write.
    @param IsReadWrite Field is read / write */
    public void setIsReadWrite (boolean IsReadWrite)
    {
        set_Value ("IsReadWrite", Boolean.valueOf(IsReadWrite));
        
    }
    
    /** Get Read Write.
    @return Field is read / write */
    public boolean isReadWrite() 
    {
        return get_ValueAsBoolean("IsReadWrite");
        
    }
    
    
}
