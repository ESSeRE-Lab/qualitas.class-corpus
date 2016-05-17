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
/** Generated Model for AD_LabelPrinter
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_AD_LabelPrinter extends PO
{
    /** Standard Constructor
    @param ctx context
    @param AD_LabelPrinter_ID id
    @param trx transaction
    */
    public X_AD_LabelPrinter (Ctx ctx, int AD_LabelPrinter_ID, Trx trx)
    {
        super (ctx, AD_LabelPrinter_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (AD_LabelPrinter_ID == 0)
        {
            setAD_LabelPrinter_ID (0);
            setName (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_AD_LabelPrinter (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=626 */
    public static final int Table_ID=626;
    
    /** TableName=AD_LabelPrinter */
    public static final String Table_Name="AD_LabelPrinter";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"AD_LabelPrinter");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Label printer.
    @param AD_LabelPrinter_ID Label Printer Definition */
    public void setAD_LabelPrinter_ID (int AD_LabelPrinter_ID)
    {
        if (AD_LabelPrinter_ID < 1) throw new IllegalArgumentException ("AD_LabelPrinter_ID is mandatory.");
        set_ValueNoCheck ("AD_LabelPrinter_ID", Integer.valueOf(AD_LabelPrinter_ID));
        
    }
    
    /** Get Label printer.
    @return Label Printer Definition */
    public int getAD_LabelPrinter_ID() 
    {
        return get_ValueAsInt("AD_LabelPrinter_ID");
        
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
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getName());
        
    }
    
    
}
