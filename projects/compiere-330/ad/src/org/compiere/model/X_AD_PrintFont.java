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
/** Generated Model for AD_PrintFont
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_AD_PrintFont extends PO
{
    /** Standard Constructor
    @param ctx context
    @param AD_PrintFont_ID id
    @param trx transaction
    */
    public X_AD_PrintFont (Ctx ctx, int AD_PrintFont_ID, Trx trx)
    {
        super (ctx, AD_PrintFont_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (AD_PrintFont_ID == 0)
        {
            setAD_PrintFont_ID (0);
            setCode (null);
            setIsDefault (false);
            setName (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_AD_PrintFont (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=491 */
    public static final int Table_ID=491;
    
    /** TableName=AD_PrintFont */
    public static final String Table_Name="AD_PrintFont";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"AD_PrintFont");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Print Font.
    @param AD_PrintFont_ID Maintain Print Font */
    public void setAD_PrintFont_ID (int AD_PrintFont_ID)
    {
        if (AD_PrintFont_ID < 1) throw new IllegalArgumentException ("AD_PrintFont_ID is mandatory.");
        set_ValueNoCheck ("AD_PrintFont_ID", Integer.valueOf(AD_PrintFont_ID));
        
    }
    
    /** Get Print Font.
    @return Maintain Print Font */
    public int getAD_PrintFont_ID() 
    {
        return get_ValueAsInt("AD_PrintFont_ID");
        
    }
    
    /** Set Code.
    @param Code Code to execute or to validate */
    public void setCode (String Code)
    {
        if (Code == null) throw new IllegalArgumentException ("Code is mandatory.");
        set_Value ("Code", Code);
        
    }
    
    /** Get Code.
    @return Code to execute or to validate */
    public String getCode() 
    {
        return (String)get_Value("Code");
        
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
    
    
}
