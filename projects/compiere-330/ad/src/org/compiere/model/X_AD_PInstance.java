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
/** Generated Model for AD_PInstance
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_AD_PInstance extends PO
{
    /** Standard Constructor
    @param ctx context
    @param AD_PInstance_ID id
    @param trx transaction
    */
    public X_AD_PInstance (Ctx ctx, int AD_PInstance_ID, Trx trx)
    {
        super (ctx, AD_PInstance_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (AD_PInstance_ID == 0)
        {
            setAD_PInstance_ID (0);
            setAD_Process_ID (0);
            setIsProcessing (false);
            setRecord_ID (0);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_AD_PInstance (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27510884939789L;
    /** Last Updated Timestamp 2008-12-08 10:07:03.0 */
    public static final long updatedMS = 1228759623000L;
    /** AD_Table_ID=282 */
    public static final int Table_ID=282;
    
    /** TableName=AD_PInstance */
    public static final String Table_Name="AD_PInstance";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"AD_PInstance");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Process Instance.
    @param AD_PInstance_ID Instance of the process */
    public void setAD_PInstance_ID (int AD_PInstance_ID)
    {
        if (AD_PInstance_ID < 1) throw new IllegalArgumentException ("AD_PInstance_ID is mandatory.");
        set_ValueNoCheck ("AD_PInstance_ID", Integer.valueOf(AD_PInstance_ID));
        
    }
    
    /** Get Process Instance.
    @return Instance of the process */
    public int getAD_PInstance_ID() 
    {
        return get_ValueAsInt("AD_PInstance_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getAD_PInstance_ID()));
        
    }
    
    /** Set Process.
    @param AD_Process_ID Process or Report */
    public void setAD_Process_ID (int AD_Process_ID)
    {
        if (AD_Process_ID < 1) throw new IllegalArgumentException ("AD_Process_ID is mandatory.");
        set_Value ("AD_Process_ID", Integer.valueOf(AD_Process_ID));
        
    }
    
    /** Get Process.
    @return Process or Report */
    public int getAD_Process_ID() 
    {
        return get_ValueAsInt("AD_Process_ID");
        
    }
    
    /** Set Role.
    @param AD_Role_ID Responsibility Role */
    public void setAD_Role_ID (int AD_Role_ID)
    {
        if (AD_Role_ID <= 0) set_ValueNoCheck ("AD_Role_ID", null);
        else
        set_ValueNoCheck ("AD_Role_ID", Integer.valueOf(AD_Role_ID));
        
    }
    
    /** Get Role.
    @return Responsibility Role */
    public int getAD_Role_ID() 
    {
        return get_ValueAsInt("AD_Role_ID");
        
    }
    
    /** Set Session.
    @param AD_Session_ID User Session Online or Web */
    public void setAD_Session_ID (int AD_Session_ID)
    {
        if (AD_Session_ID <= 0) set_ValueNoCheck ("AD_Session_ID", null);
        else
        set_ValueNoCheck ("AD_Session_ID", Integer.valueOf(AD_Session_ID));
        
    }
    
    /** Get Session.
    @return User Session Online or Web */
    public int getAD_Session_ID() 
    {
        return get_ValueAsInt("AD_Session_ID");
        
    }
    
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
    
    /** Set Error Message.
    @param ErrorMsg Error Message */
    public void setErrorMsg (String ErrorMsg)
    {
        set_Value ("ErrorMsg", ErrorMsg);
        
    }
    
    /** Get Error Message.
    @return Error Message */
    public String getErrorMsg() 
    {
        return (String)get_Value("ErrorMsg");
        
    }
    
    /** Set Processing.
    @param IsProcessing Processing */
    public void setIsProcessing (boolean IsProcessing)
    {
        set_Value ("IsProcessing", Boolean.valueOf(IsProcessing));
        
    }
    
    /** Get Processing.
    @return Processing */
    public boolean isProcessing() 
    {
        return get_ValueAsBoolean("IsProcessing");
        
    }
    
    /** Set Record ID.
    @param Record_ID Direct internal record ID */
    public void setRecord_ID (int Record_ID)
    {
        if (Record_ID < 0) throw new IllegalArgumentException ("Record_ID is mandatory.");
        set_ValueNoCheck ("Record_ID", Integer.valueOf(Record_ID));
        
    }
    
    /** Get Record ID.
    @return Direct internal record ID */
    public int getRecord_ID() 
    {
        return get_ValueAsInt("Record_ID");
        
    }
    
    /** Set Result.
    @param Result Result of the action taken */
    public void setResult (int Result)
    {
        set_Value ("Result", Integer.valueOf(Result));
        
    }
    
    /** Get Result.
    @return Result of the action taken */
    public int getResult() 
    {
        return get_ValueAsInt("Result");
        
    }
    
    
}
