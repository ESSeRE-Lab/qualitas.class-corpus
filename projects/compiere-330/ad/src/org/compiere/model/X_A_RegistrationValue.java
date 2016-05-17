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
/** Generated Model for A_RegistrationValue
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_A_RegistrationValue extends PO
{
    /** Standard Constructor
    @param ctx context
    @param A_RegistrationValue_ID id
    @param trx transaction
    */
    public X_A_RegistrationValue (Ctx ctx, int A_RegistrationValue_ID, Trx trx)
    {
        super (ctx, A_RegistrationValue_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (A_RegistrationValue_ID == 0)
        {
            setA_RegistrationAttribute_ID (0);
            setA_Registration_ID (0);
            setName (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_A_RegistrationValue (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=653 */
    public static final int Table_ID=653;
    
    /** TableName=A_RegistrationValue */
    public static final String Table_Name="A_RegistrationValue";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"A_RegistrationValue");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Registration Attribute.
    @param A_RegistrationAttribute_ID Asset Registration Attribute */
    public void setA_RegistrationAttribute_ID (int A_RegistrationAttribute_ID)
    {
        if (A_RegistrationAttribute_ID < 1) throw new IllegalArgumentException ("A_RegistrationAttribute_ID is mandatory.");
        set_ValueNoCheck ("A_RegistrationAttribute_ID", Integer.valueOf(A_RegistrationAttribute_ID));
        
    }
    
    /** Get Registration Attribute.
    @return Asset Registration Attribute */
    public int getA_RegistrationAttribute_ID() 
    {
        return get_ValueAsInt("A_RegistrationAttribute_ID");
        
    }
    
    /** Set Registration.
    @param A_Registration_ID User Asset Registration */
    public void setA_Registration_ID (int A_Registration_ID)
    {
        if (A_Registration_ID < 1) throw new IllegalArgumentException ("A_Registration_ID is mandatory.");
        set_ValueNoCheck ("A_Registration_ID", Integer.valueOf(A_Registration_ID));
        
    }
    
    /** Get Registration.
    @return User Asset Registration */
    public int getA_Registration_ID() 
    {
        return get_ValueAsInt("A_Registration_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getA_Registration_ID()));
        
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
