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
/** Generated Model for AD_WF_Responsible
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_AD_WF_Responsible extends PO
{
    /** Standard Constructor
    @param ctx context
    @param AD_WF_Responsible_ID id
    @param trx transaction
    */
    public X_AD_WF_Responsible (Ctx ctx, int AD_WF_Responsible_ID, Trx trx)
    {
        super (ctx, AD_WF_Responsible_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (AD_WF_Responsible_ID == 0)
        {
            setAD_WF_Responsible_ID (0);
            setEntityType (null);	// U
            setName (null);
            setResponsibleType (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_AD_WF_Responsible (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=646 */
    public static final int Table_ID=646;
    
    /** TableName=AD_WF_Responsible */
    public static final String Table_Name="AD_WF_Responsible";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"AD_WF_Responsible");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
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
    
    
    /** AD_User_ID AD_Reference_ID=286 */
    public static final int AD_USER_ID_AD_Reference_ID=286;
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
    
    /** Set Workflow Responsible.
    @param AD_WF_Responsible_ID Responsible for Workflow Execution */
    public void setAD_WF_Responsible_ID (int AD_WF_Responsible_ID)
    {
        if (AD_WF_Responsible_ID < 1) throw new IllegalArgumentException ("AD_WF_Responsible_ID is mandatory.");
        set_ValueNoCheck ("AD_WF_Responsible_ID", Integer.valueOf(AD_WF_Responsible_ID));
        
    }
    
    /** Get Workflow Responsible.
    @return Responsible for Workflow Execution */
    public int getAD_WF_Responsible_ID() 
    {
        return get_ValueAsInt("AD_WF_Responsible_ID");
        
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
    
    
    /** ResponsibleType AD_Reference_ID=304 */
    public static final int RESPONSIBLETYPE_AD_Reference_ID=304;
    /** Human = H */
    public static final String RESPONSIBLETYPE_Human = X_Ref_WF_Participant_Type.HUMAN.getValue();
    /** Organization = O */
    public static final String RESPONSIBLETYPE_Organization = X_Ref_WF_Participant_Type.ORGANIZATION.getValue();
    /** Role = R */
    public static final String RESPONSIBLETYPE_Role = X_Ref_WF_Participant_Type.ROLE.getValue();
    /** System Resource = S */
    public static final String RESPONSIBLETYPE_SystemResource = X_Ref_WF_Participant_Type.SYSTEM_RESOURCE.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isResponsibleTypeValid(String test)
    {
         return X_Ref_WF_Participant_Type.isValid(test);
         
    }
    /** Set Responsible Type.
    @param ResponsibleType Type of the Responsibility for a workflow */
    public void setResponsibleType (String ResponsibleType)
    {
        if (ResponsibleType == null) throw new IllegalArgumentException ("ResponsibleType is mandatory");
        if (!isResponsibleTypeValid(ResponsibleType))
        throw new IllegalArgumentException ("ResponsibleType Invalid value - " + ResponsibleType + " - Reference_ID=304 - H - O - R - S");
        set_Value ("ResponsibleType", ResponsibleType);
        
    }
    
    /** Get Responsible Type.
    @return Type of the Responsibility for a workflow */
    public String getResponsibleType() 
    {
        return (String)get_Value("ResponsibleType");
        
    }
    
    
}
