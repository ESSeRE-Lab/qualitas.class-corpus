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
/** Generated Model for CM_AccessContainer
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_CM_AccessContainer extends PO
{
    /** Standard Constructor
    @param ctx context
    @param CM_AccessContainer_ID id
    @param trx transaction
    */
    public X_CM_AccessContainer (Ctx ctx, int CM_AccessContainer_ID, Trx trx)
    {
        super (ctx, CM_AccessContainer_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (CM_AccessContainer_ID == 0)
        {
            setCM_AccessProfile_ID (0);
            setCM_Container_ID (0);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_CM_AccessContainer (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=888 */
    public static final int Table_ID=888;
    
    /** TableName=CM_AccessContainer */
    public static final String Table_Name="CM_AccessContainer";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"CM_AccessContainer");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Web Access Profile.
    @param CM_AccessProfile_ID Web Access Profile */
    public void setCM_AccessProfile_ID (int CM_AccessProfile_ID)
    {
        if (CM_AccessProfile_ID < 1) throw new IllegalArgumentException ("CM_AccessProfile_ID is mandatory.");
        set_ValueNoCheck ("CM_AccessProfile_ID", Integer.valueOf(CM_AccessProfile_ID));
        
    }
    
    /** Get Web Access Profile.
    @return Web Access Profile */
    public int getCM_AccessProfile_ID() 
    {
        return get_ValueAsInt("CM_AccessProfile_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getCM_AccessProfile_ID()));
        
    }
    
    /** Set Web Container.
    @param CM_Container_ID Web Container contains content like images, text etc. */
    public void setCM_Container_ID (int CM_Container_ID)
    {
        if (CM_Container_ID < 1) throw new IllegalArgumentException ("CM_Container_ID is mandatory.");
        set_ValueNoCheck ("CM_Container_ID", Integer.valueOf(CM_Container_ID));
        
    }
    
    /** Get Web Container.
    @return Web Container contains content like images, text etc. */
    public int getCM_Container_ID() 
    {
        return get_ValueAsInt("CM_Container_ID");
        
    }
    
    
}
