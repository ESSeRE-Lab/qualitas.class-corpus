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
/** Generated Model for AD_Column_Access
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_AD_Column_Access extends PO
{
    /** Standard Constructor
    @param ctx context
    @param AD_Column_Access_ID id
    @param trx transaction
    */
    public X_AD_Column_Access (Ctx ctx, int AD_Column_Access_ID, Trx trx)
    {
        super (ctx, AD_Column_Access_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (AD_Column_Access_ID == 0)
        {
            setAD_Column_ID (0);
            setAD_Role_ID (0);
            setIsExclude (true);	// Y
            setIsReadOnly (false);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_AD_Column_Access (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=571 */
    public static final int Table_ID=571;
    
    /** TableName=AD_Column_Access */
    public static final String Table_Name="AD_Column_Access";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"AD_Column_Access");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Column.
    @param AD_Column_ID Column in the table */
    public void setAD_Column_ID (int AD_Column_ID)
    {
        if (AD_Column_ID < 1) throw new IllegalArgumentException ("AD_Column_ID is mandatory.");
        set_ValueNoCheck ("AD_Column_ID", Integer.valueOf(AD_Column_ID));
        
    }
    
    /** Get Column.
    @return Column in the table */
    public int getAD_Column_ID() 
    {
        return get_ValueAsInt("AD_Column_ID");
        
    }
    
    /** Set Role.
    @param AD_Role_ID Responsibility Role */
    public void setAD_Role_ID (int AD_Role_ID)
    {
        if (AD_Role_ID < 0) throw new IllegalArgumentException ("AD_Role_ID is mandatory.");
        set_ValueNoCheck ("AD_Role_ID", Integer.valueOf(AD_Role_ID));
        
    }
    
    /** Get Role.
    @return Responsibility Role */
    public int getAD_Role_ID() 
    {
        return get_ValueAsInt("AD_Role_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getAD_Role_ID()));
        
    }
    
    /** Set Table.
    @param AD_Table_ID Database Table information */
    public void setAD_Table_ID (int AD_Table_ID)
    {
        if (AD_Table_ID <= 0) set_Value ("AD_Table_ID", null);
        else
        set_Value ("AD_Table_ID", Integer.valueOf(AD_Table_ID));
        
    }
    
    /** Get Table.
    @return Database Table information */
    public int getAD_Table_ID() 
    {
        return get_ValueAsInt("AD_Table_ID");
        
    }
    
    /** Set Exclude.
    @param IsExclude Exclude access to the data - if not selected Include access to the data */
    public void setIsExclude (boolean IsExclude)
    {
        set_Value ("IsExclude", Boolean.valueOf(IsExclude));
        
    }
    
    /** Get Exclude.
    @return Exclude access to the data - if not selected Include access to the data */
    public boolean isExclude() 
    {
        return get_ValueAsBoolean("IsExclude");
        
    }
    
    /** Set Read Only.
    @param IsReadOnly Field is read only */
    public void setIsReadOnly (boolean IsReadOnly)
    {
        set_Value ("IsReadOnly", Boolean.valueOf(IsReadOnly));
        
    }
    
    /** Get Read Only.
    @return Field is read only */
    public boolean isReadOnly() 
    {
        return get_ValueAsBoolean("IsReadOnly");
        
    }
    
    
}
