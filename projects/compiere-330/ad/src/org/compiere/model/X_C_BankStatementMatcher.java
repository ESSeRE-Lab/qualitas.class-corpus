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
/** Generated Model for C_BankStatementMatcher
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_C_BankStatementMatcher extends PO
{
    /** Standard Constructor
    @param ctx context
    @param C_BankStatementMatcher_ID id
    @param trx transaction
    */
    public X_C_BankStatementMatcher (Ctx ctx, int C_BankStatementMatcher_ID, Trx trx)
    {
        super (ctx, C_BankStatementMatcher_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (C_BankStatementMatcher_ID == 0)
        {
            setC_BankStatementMatcher_ID (0);
            setClassname (null);
            setName (null);
            setSeqNo (0);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_C_BankStatementMatcher (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=658 */
    public static final int Table_ID=658;
    
    /** TableName=C_BankStatementMatcher */
    public static final String Table_Name="C_BankStatementMatcher";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"C_BankStatementMatcher");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Bank Statement Matcher.
    @param C_BankStatementMatcher_ID Algorithm to match Bank Statement Info to Business Partners, Invoices and Payments */
    public void setC_BankStatementMatcher_ID (int C_BankStatementMatcher_ID)
    {
        if (C_BankStatementMatcher_ID < 1) throw new IllegalArgumentException ("C_BankStatementMatcher_ID is mandatory.");
        set_ValueNoCheck ("C_BankStatementMatcher_ID", Integer.valueOf(C_BankStatementMatcher_ID));
        
    }
    
    /** Get Bank Statement Matcher.
    @return Algorithm to match Bank Statement Info to Business Partners, Invoices and Payments */
    public int getC_BankStatementMatcher_ID() 
    {
        return get_ValueAsInt("C_BankStatementMatcher_ID");
        
    }
    
    /** Set Classname.
    @param Classname Java Classname */
    public void setClassname (String Classname)
    {
        if (Classname == null) throw new IllegalArgumentException ("Classname is mandatory.");
        set_Value ("Classname", Classname);
        
    }
    
    /** Get Classname.
    @return Java Classname */
    public String getClassname() 
    {
        return (String)get_Value("Classname");
        
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
    
    /** Set Sequence.
    @param SeqNo Method of ordering elements;
     lowest number comes first */
    public void setSeqNo (int SeqNo)
    {
        set_Value ("SeqNo", Integer.valueOf(SeqNo));
        
    }
    
    /** Get Sequence.
    @return Method of ordering elements;
     lowest number comes first */
    public int getSeqNo() 
    {
        return get_ValueAsInt("SeqNo");
        
    }
    
    
}
