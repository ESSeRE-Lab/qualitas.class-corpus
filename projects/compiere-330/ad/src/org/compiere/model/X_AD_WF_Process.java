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
/** Generated Model for AD_WF_Process
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_AD_WF_Process extends PO
{
    /** Standard Constructor
    @param ctx context
    @param AD_WF_Process_ID id
    @param trx transaction
    */
    public X_AD_WF_Process (Ctx ctx, int AD_WF_Process_ID, Trx trx)
    {
        super (ctx, AD_WF_Process_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (AD_WF_Process_ID == 0)
        {
            setAD_Table_ID (0);
            setAD_WF_Process_ID (0);
            setAD_WF_Responsible_ID (0);
            setAD_Workflow_ID (0);
            setProcessed (false);	// N
            setRecord_ID (0);
            setWFState (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_AD_WF_Process (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=645 */
    public static final int Table_ID=645;
    
    /** TableName=AD_WF_Process */
    public static final String Table_Name="AD_WF_Process";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"AD_WF_Process");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Message.
    @param AD_Message_ID System Message */
    public void setAD_Message_ID (int AD_Message_ID)
    {
        if (AD_Message_ID <= 0) set_Value ("AD_Message_ID", null);
        else
        set_Value ("AD_Message_ID", Integer.valueOf(AD_Message_ID));
        
    }
    
    /** Get Message.
    @return System Message */
    public int getAD_Message_ID() 
    {
        return get_ValueAsInt("AD_Message_ID");
        
    }
    
    /** Set Table.
    @param AD_Table_ID Database Table information */
    public void setAD_Table_ID (int AD_Table_ID)
    {
        if (AD_Table_ID < 1) throw new IllegalArgumentException ("AD_Table_ID is mandatory.");
        set_Value ("AD_Table_ID", Integer.valueOf(AD_Table_ID));
        
    }
    
    /** Get Table.
    @return Database Table information */
    public int getAD_Table_ID() 
    {
        return get_ValueAsInt("AD_Table_ID");
        
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
    
    /** Set Workflow Process.
    @param AD_WF_Process_ID Actual Workflow Process Instance */
    public void setAD_WF_Process_ID (int AD_WF_Process_ID)
    {
        if (AD_WF_Process_ID < 1) throw new IllegalArgumentException ("AD_WF_Process_ID is mandatory.");
        set_ValueNoCheck ("AD_WF_Process_ID", Integer.valueOf(AD_WF_Process_ID));
        
    }
    
    /** Get Workflow Process.
    @return Actual Workflow Process Instance */
    public int getAD_WF_Process_ID() 
    {
        return get_ValueAsInt("AD_WF_Process_ID");
        
    }
    
    /** Set Workflow Responsible.
    @param AD_WF_Responsible_ID Responsible for Workflow Execution */
    public void setAD_WF_Responsible_ID (int AD_WF_Responsible_ID)
    {
        if (AD_WF_Responsible_ID < 1) throw new IllegalArgumentException ("AD_WF_Responsible_ID is mandatory.");
        set_Value ("AD_WF_Responsible_ID", Integer.valueOf(AD_WF_Responsible_ID));
        
    }
    
    /** Get Workflow Responsible.
    @return Responsible for Workflow Execution */
    public int getAD_WF_Responsible_ID() 
    {
        return get_ValueAsInt("AD_WF_Responsible_ID");
        
    }
    
    /** Set Workflow.
    @param AD_Workflow_ID Workflow or combination of tasks */
    public void setAD_Workflow_ID (int AD_Workflow_ID)
    {
        if (AD_Workflow_ID < 1) throw new IllegalArgumentException ("AD_Workflow_ID is mandatory.");
        set_Value ("AD_Workflow_ID", Integer.valueOf(AD_Workflow_ID));
        
    }
    
    /** Get Workflow.
    @return Workflow or combination of tasks */
    public int getAD_Workflow_ID() 
    {
        return get_ValueAsInt("AD_Workflow_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getAD_Workflow_ID()));
        
    }
    
    /** Set Priority.
    @param Priority Indicates if this request is of a high, medium or low priority. */
    public void setPriority (int Priority)
    {
        set_Value ("Priority", Integer.valueOf(Priority));
        
    }
    
    /** Get Priority.
    @return Indicates if this request is of a high, medium or low priority. */
    public int getPriority() 
    {
        return get_ValueAsInt("Priority");
        
    }
    
    /** Set Processed.
    @param Processed The document has been processed */
    public void setProcessed (boolean Processed)
    {
        set_ValueNoCheck ("Processed", Boolean.valueOf(Processed));
        
    }
    
    /** Get Processed.
    @return The document has been processed */
    public boolean isProcessed() 
    {
        return get_ValueAsBoolean("Processed");
        
    }
    
    /** Set Process Now.
    @param Processing Process Now */
    public void setProcessing (boolean Processing)
    {
        set_Value ("Processing", Boolean.valueOf(Processing));
        
    }
    
    /** Get Process Now.
    @return Process Now */
    public boolean isProcessing() 
    {
        return get_ValueAsBoolean("Processing");
        
    }
    
    /** Set Record ID.
    @param Record_ID Direct internal record ID */
    public void setRecord_ID (int Record_ID)
    {
        if (Record_ID < 0) throw new IllegalArgumentException ("Record_ID is mandatory.");
        set_Value ("Record_ID", Integer.valueOf(Record_ID));
        
    }
    
    /** Get Record ID.
    @return Direct internal record ID */
    public int getRecord_ID() 
    {
        return get_ValueAsInt("Record_ID");
        
    }
    
    /** Set Text Message.
    @param TextMsg Text Message */
    public void setTextMsg (String TextMsg)
    {
        set_Value ("TextMsg", TextMsg);
        
    }
    
    /** Get Text Message.
    @return Text Message */
    public String getTextMsg() 
    {
        return (String)get_Value("TextMsg");
        
    }
    
    
    /** WFState AD_Reference_ID=305 */
    public static final int WFSTATE_AD_Reference_ID=305;
    /** Aborted = CA */
    public static final String WFSTATE_Aborted = X_Ref_WF_Instance_State.ABORTED.getValue();
    /** Completed = CC */
    public static final String WFSTATE_Completed = X_Ref_WF_Instance_State.COMPLETED.getValue();
    /** Terminated = CT */
    public static final String WFSTATE_Terminated = X_Ref_WF_Instance_State.TERMINATED.getValue();
    /** Not Started = ON */
    public static final String WFSTATE_NotStarted = X_Ref_WF_Instance_State.NOT_STARTED.getValue();
    /** Running = OR */
    public static final String WFSTATE_Running = X_Ref_WF_Instance_State.RUNNING.getValue();
    /** Suspended = OS */
    public static final String WFSTATE_Suspended = X_Ref_WF_Instance_State.SUSPENDED.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isWFStateValid(String test)
    {
         return X_Ref_WF_Instance_State.isValid(test);
         
    }
    /** Set Workflow State.
    @param WFState State of the execution of the workflow */
    public void setWFState (String WFState)
    {
        if (WFState == null) throw new IllegalArgumentException ("WFState is mandatory");
        if (!isWFStateValid(WFState))
        throw new IllegalArgumentException ("WFState Invalid value - " + WFState + " - Reference_ID=305 - CA - CC - CT - ON - OR - OS");
        set_Value ("WFState", WFState);
        
    }
    
    /** Get Workflow State.
    @return State of the execution of the workflow */
    public String getWFState() 
    {
        return (String)get_Value("WFState");
        
    }
    
    
}
