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
/** Generated Model for C_Greeting_Trl
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_C_Greeting_Trl extends PO
{
    /** Standard Constructor
    @param ctx context
    @param C_Greeting_Trl_ID id
    @param trx transaction
    */
    public X_C_Greeting_Trl (Ctx ctx, int C_Greeting_Trl_ID, Trx trx)
    {
        super (ctx, C_Greeting_Trl_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (C_Greeting_Trl_ID == 0)
        {
            setAD_Language (null);
            setC_Greeting_ID (0);
            setIsTranslated (false);
            setName (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_C_Greeting_Trl (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=347 */
    public static final int Table_ID=347;
    
    /** TableName=C_Greeting_Trl */
    public static final String Table_Name="C_Greeting_Trl";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"C_Greeting_Trl");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    
    /** AD_Language AD_Reference_ID=106 */
    public static final int AD_LANGUAGE_AD_Reference_ID=106;
    /** Set Language.
    @param AD_Language Language for this entity */
    public void setAD_Language (String AD_Language)
    {
        set_ValueNoCheck ("AD_Language", AD_Language);
        
    }
    
    /** Get Language.
    @return Language for this entity */
    public String getAD_Language() 
    {
        return (String)get_Value("AD_Language");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getAD_Language()));
        
    }
    
    /** Set Greeting.
    @param C_Greeting_ID Greeting to print on correspondence */
    public void setC_Greeting_ID (int C_Greeting_ID)
    {
        if (C_Greeting_ID < 1) throw new IllegalArgumentException ("C_Greeting_ID is mandatory.");
        set_ValueNoCheck ("C_Greeting_ID", Integer.valueOf(C_Greeting_ID));
        
    }
    
    /** Get Greeting.
    @return Greeting to print on correspondence */
    public int getC_Greeting_ID() 
    {
        return get_ValueAsInt("C_Greeting_ID");
        
    }
    
    /** Set Greeting.
    @param Greeting For letters, e.g. "Dear 
    {
        0
    }
    " or "Dear Mr. 
    {
        0
    }
    " - At runtime, "
    {
        0
    }
    " is replaced by the name */
    public void setGreeting (String Greeting)
    {
        set_Value ("Greeting", Greeting);
        
    }
    
    /** Get Greeting.
    @return For letters, e.g. "Dear 
    {
        0
    }
    " or "Dear Mr. 
    {
        0
    }
    " - At runtime, "
    {
        0
    }
    " is replaced by the name */
    public String getGreeting() 
    {
        return (String)get_Value("Greeting");
        
    }
    
    /** Set Translated.
    @param IsTranslated This column is translated */
    public void setIsTranslated (boolean IsTranslated)
    {
        set_Value ("IsTranslated", Boolean.valueOf(IsTranslated));
        
    }
    
    /** Get Translated.
    @return This column is translated */
    public boolean isTranslated() 
    {
        return get_ValueAsBoolean("IsTranslated");
        
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
    
    
}
