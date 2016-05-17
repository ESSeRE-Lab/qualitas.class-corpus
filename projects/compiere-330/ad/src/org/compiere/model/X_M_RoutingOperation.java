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
/** Generated Model for M_RoutingOperation
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_RoutingOperation extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_RoutingOperation_ID id
    @param trx transaction
    */
    public X_M_RoutingOperation (Ctx ctx, int M_RoutingOperation_ID, Trx trx)
    {
        super (ctx, M_RoutingOperation_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_RoutingOperation_ID == 0)
        {
            setIsHazmat (false);	// N
            setIsOptional (false);	// N
            setIsPermitRequired (false);	// N
            setM_Department_ID (0);
            setM_RoutingOperation_ID (0);
            setM_Routing_ID (0);
            setSeqNo (0);	// @SQL=SELECT NVL(MAX(SeqNo),0)+10 AS DefaultValue FROM M_RoutingOperation WHERE M_Routing_ID=@M_Routing_ID@
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_RoutingOperation (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27498276648789L;
    /** Last Updated Timestamp 2008-07-15 12:48:52.0 */
    public static final long updatedMS = 1216151332000L;
    /** AD_Table_ID=1028 */
    public static final int Table_ID=1028;
    
    /** TableName=M_RoutingOperation */
    public static final String Table_Name="M_RoutingOperation";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_RoutingOperation");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    
    /** AD_OrgTrx_ID AD_Reference_ID=130 */
    public static final int AD_ORGTRX_ID_AD_Reference_ID=130;
    /** Set Trx Organization.
    @param AD_OrgTrx_ID Performing or initiating organization */
    public void setAD_OrgTrx_ID (int AD_OrgTrx_ID)
    {
        if (AD_OrgTrx_ID <= 0) set_Value ("AD_OrgTrx_ID", null);
        else
        set_Value ("AD_OrgTrx_ID", Integer.valueOf(AD_OrgTrx_ID));
        
    }
    
    /** Get Trx Organization.
    @return Performing or initiating organization */
    public int getAD_OrgTrx_ID() 
    {
        return get_ValueAsInt("AD_OrgTrx_ID");
        
    }
    
    /** Set Activity.
    @param C_Activity_ID Business Activity */
    public void setC_Activity_ID (int C_Activity_ID)
    {
        if (C_Activity_ID <= 0) set_Value ("C_Activity_ID", null);
        else
        set_Value ("C_Activity_ID", Integer.valueOf(C_Activity_ID));
        
    }
    
    /** Get Activity.
    @return Business Activity */
    public int getC_Activity_ID() 
    {
        return get_ValueAsInt("C_Activity_ID");
        
    }
    
    /** Set Business Partner.
    @param C_BPartner_ID Identifies a Business Partner */
    public void setC_BPartner_ID (int C_BPartner_ID)
    {
        if (C_BPartner_ID <= 0) set_Value ("C_BPartner_ID", null);
        else
        set_Value ("C_BPartner_ID", Integer.valueOf(C_BPartner_ID));
        
    }
    
    /** Get Business Partner.
    @return Identifies a Business Partner */
    public int getC_BPartner_ID() 
    {
        return get_ValueAsInt("C_BPartner_ID");
        
    }
    
    /** Set Partner Location.
    @param C_BPartner_Location_ID Identifies the (ship to) address for this Business Partner */
    public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
    {
        if (C_BPartner_Location_ID <= 0) set_Value ("C_BPartner_Location_ID", null);
        else
        set_Value ("C_BPartner_Location_ID", Integer.valueOf(C_BPartner_Location_ID));
        
    }
    
    /** Get Partner Location.
    @return Identifies the (ship to) address for this Business Partner */
    public int getC_BPartner_Location_ID() 
    {
        return get_ValueAsInt("C_BPartner_Location_ID");
        
    }
    
    /** Set Campaign.
    @param C_Campaign_ID Marketing Campaign */
    public void setC_Campaign_ID (int C_Campaign_ID)
    {
        if (C_Campaign_ID <= 0) set_Value ("C_Campaign_ID", null);
        else
        set_Value ("C_Campaign_ID", Integer.valueOf(C_Campaign_ID));
        
    }
    
    /** Get Campaign.
    @return Marketing Campaign */
    public int getC_Campaign_ID() 
    {
        return get_ValueAsInt("C_Campaign_ID");
        
    }
    
    /** Set Project Phase.
    @param C_ProjectPhase_ID Phase of a Project */
    public void setC_ProjectPhase_ID (int C_ProjectPhase_ID)
    {
        if (C_ProjectPhase_ID <= 0) set_Value ("C_ProjectPhase_ID", null);
        else
        set_Value ("C_ProjectPhase_ID", Integer.valueOf(C_ProjectPhase_ID));
        
    }
    
    /** Get Project Phase.
    @return Phase of a Project */
    public int getC_ProjectPhase_ID() 
    {
        return get_ValueAsInt("C_ProjectPhase_ID");
        
    }
    
    /** Set Project Task.
    @param C_ProjectTask_ID Actual Project Task in a Phase */
    public void setC_ProjectTask_ID (int C_ProjectTask_ID)
    {
        if (C_ProjectTask_ID <= 0) set_Value ("C_ProjectTask_ID", null);
        else
        set_Value ("C_ProjectTask_ID", Integer.valueOf(C_ProjectTask_ID));
        
    }
    
    /** Get Project Task.
    @return Actual Project Task in a Phase */
    public int getC_ProjectTask_ID() 
    {
        return get_ValueAsInt("C_ProjectTask_ID");
        
    }
    
    /** Set Project.
    @param C_Project_ID Financial Project */
    public void setC_Project_ID (int C_Project_ID)
    {
        if (C_Project_ID <= 0) set_Value ("C_Project_ID", null);
        else
        set_Value ("C_Project_ID", Integer.valueOf(C_Project_ID));
        
    }
    
    /** Get Project.
    @return Financial Project */
    public int getC_Project_ID() 
    {
        return get_ValueAsInt("C_Project_ID");
        
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
    
    /** Set Hazmat.
    @param IsHazmat Involves hazardous materials */
    public void setIsHazmat (boolean IsHazmat)
    {
        set_Value ("IsHazmat", Boolean.valueOf(IsHazmat));
        
    }
    
    /** Get Hazmat.
    @return Involves hazardous materials */
    public boolean isHazmat() 
    {
        return get_ValueAsBoolean("IsHazmat");
        
    }
    
    /** Set Optional.
    @param IsOptional Optional */
    public void setIsOptional (boolean IsOptional)
    {
        set_Value ("IsOptional", Boolean.valueOf(IsOptional));
        
    }
    
    /** Get Optional.
    @return Optional */
    public boolean isOptional() 
    {
        return get_ValueAsBoolean("IsOptional");
        
    }
    
    /** Set Permit Required.
    @param IsPermitRequired Permit Required */
    public void setIsPermitRequired (boolean IsPermitRequired)
    {
        set_Value ("IsPermitRequired", Boolean.valueOf(IsPermitRequired));
        
    }
    
    /** Get Permit Required.
    @return Permit Required */
    public boolean isPermitRequired() 
    {
        return get_ValueAsBoolean("IsPermitRequired");
        
    }
    
    /** Set Department.
    @param M_Department_ID Department */
    public void setM_Department_ID (int M_Department_ID)
    {
        if (M_Department_ID < 1) throw new IllegalArgumentException ("M_Department_ID is mandatory.");
        set_Value ("M_Department_ID", Integer.valueOf(M_Department_ID));
        
    }
    
    /** Get Department.
    @return Department */
    public int getM_Department_ID() 
    {
        return get_ValueAsInt("M_Department_ID");
        
    }
    
    /** Set Routing Operation.
    @param M_RoutingOperation_ID Standard Routing Operation */
    public void setM_RoutingOperation_ID (int M_RoutingOperation_ID)
    {
        if (M_RoutingOperation_ID < 1) throw new IllegalArgumentException ("M_RoutingOperation_ID is mandatory.");
        set_ValueNoCheck ("M_RoutingOperation_ID", Integer.valueOf(M_RoutingOperation_ID));
        
    }
    
    /** Get Routing Operation.
    @return Standard Routing Operation */
    public int getM_RoutingOperation_ID() 
    {
        return get_ValueAsInt("M_RoutingOperation_ID");
        
    }
    
    /** Set Routing.
    @param M_Routing_ID Routing for an assembly */
    public void setM_Routing_ID (int M_Routing_ID)
    {
        if (M_Routing_ID < 1) throw new IllegalArgumentException ("M_Routing_ID is mandatory.");
        set_Value ("M_Routing_ID", Integer.valueOf(M_Routing_ID));
        
    }
    
    /** Get Routing.
    @return Routing for an assembly */
    public int getM_Routing_ID() 
    {
        return get_ValueAsInt("M_Routing_ID");
        
    }
    
    /** Set Name.
    @param Name Alphanumeric identifier of the entity */
    public void setName (String Name)
    {
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
    
    /** Set Processing Time.
    @param ProcessingTime Processing Time */
    public void setProcessingTime (int ProcessingTime)
    {
        set_Value ("ProcessingTime", Integer.valueOf(ProcessingTime));
        
    }
    
    /** Get Processing Time.
    @return Processing Time */
    public int getProcessingTime() 
    {
        return get_ValueAsInt("ProcessingTime");
        
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
    
    /** Set Setup Time.
    @param SetupTime Setup time before starting Production */
    public void setSetupTime (int SetupTime)
    {
        set_Value ("SetupTime", Integer.valueOf(SetupTime));
        
    }
    
    /** Get Setup Time.
    @return Setup time before starting Production */
    public int getSetupTime() 
    {
        return get_ValueAsInt("SetupTime");
        
    }
    
    
}
