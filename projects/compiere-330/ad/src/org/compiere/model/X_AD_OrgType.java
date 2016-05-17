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
/** Generated Model for AD_OrgType
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_AD_OrgType extends PO
{
    /** Standard Constructor
    @param ctx context
    @param AD_OrgType_ID id
    @param trx transaction
    */
    public X_AD_OrgType (Ctx ctx, int AD_OrgType_ID, Trx trx)
    {
        super (ctx, AD_OrgType_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (AD_OrgType_ID == 0)
        {
            setAD_OrgType_ID (0);
            setIsBalancing (false);	// N
            setIsLegalEntity (false);	// N
            setName (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_AD_OrgType (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=689 */
    public static final int Table_ID=689;
    
    /** TableName=AD_OrgType */
    public static final String Table_Name="AD_OrgType";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"AD_OrgType");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Organization Type.
    @param AD_OrgType_ID Organization Type allows you to categorize your organizations */
    public void setAD_OrgType_ID (int AD_OrgType_ID)
    {
        if (AD_OrgType_ID < 1) throw new IllegalArgumentException ("AD_OrgType_ID is mandatory.");
        set_ValueNoCheck ("AD_OrgType_ID", Integer.valueOf(AD_OrgType_ID));
        
    }
    
    /** Get Organization Type.
    @return Organization Type allows you to categorize your organizations */
    public int getAD_OrgType_ID() 
    {
        return get_ValueAsInt("AD_OrgType_ID");
        
    }
    
    /** Set Print Color.
    @param AD_PrintColor_ID Color used for printing and display */
    public void setAD_PrintColor_ID (int AD_PrintColor_ID)
    {
        if (AD_PrintColor_ID <= 0) set_Value ("AD_PrintColor_ID", null);
        else
        set_Value ("AD_PrintColor_ID", Integer.valueOf(AD_PrintColor_ID));
        
    }
    
    /** Get Print Color.
    @return Color used for printing and display */
    public int getAD_PrintColor_ID() 
    {
        return get_ValueAsInt("AD_PrintColor_ID");
        
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
    
    /** Set Balancing.
    @param IsBalancing All transactions within an element value must balance (e.g. legal entities) */
    public void setIsBalancing (boolean IsBalancing)
    {
        set_Value ("IsBalancing", Boolean.valueOf(IsBalancing));
        
    }
    
    /** Get Balancing.
    @return All transactions within an element value must balance (e.g. legal entities) */
    public boolean isBalancing() 
    {
        return get_ValueAsBoolean("IsBalancing");
        
    }
    
    /** Set Legal Entity.
    @param IsLegalEntity The organizations are legal entities */
    public void setIsLegalEntity (boolean IsLegalEntity)
    {
        set_Value ("IsLegalEntity", Boolean.valueOf(IsLegalEntity));
        
    }
    
    /** Get Legal Entity.
    @return The organizations are legal entities */
    public boolean isLegalEntity() 
    {
        return get_ValueAsBoolean("IsLegalEntity");
        
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
