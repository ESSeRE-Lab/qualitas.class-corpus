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
/** Generated Model for M_WorkOrderOperation
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_WorkOrderOperation extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_WorkOrderOperation_ID id
    @param trx transaction
    */
    public X_M_WorkOrderOperation (Ctx ctx, int M_WorkOrderOperation_ID, Trx trx)
    {
        super (ctx, M_WorkOrderOperation_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_WorkOrderOperation_ID == 0)
        {
            setIsExecuted (false);	// N
            setM_RoutingOperation_ID (0);
            setM_Routing_ID (0);
            setM_WorkOrderOperation_ID (0);
            setM_WorkOrder_ID (0);
            setProcessed (false);	// N
            setQtyAssembled (Env.ZERO);	// 0
            setQtyRejected (Env.ZERO);	// 0
            setQtyScrapped (Env.ZERO);	// 0
            setSeqNo (0);	// @SQL=SELECT NVL(MAX(SeqNo),0)+10 AS DefaultValue FROM M_WorkOrderOperation WHERE M_WorkOrder_ID=@M_WorkOrder_ID@
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_WorkOrderOperation (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27498276517789L;
    /** Last Updated Timestamp 2008-07-15 12:46:41.0 */
    public static final long updatedMS = 1216151201000L;
    /** AD_Table_ID=1029 */
    public static final int Table_ID=1029;
    
    /** TableName=M_WorkOrderOperation */
    public static final String Table_Name="M_WorkOrderOperation";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_WorkOrderOperation");
    
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
    
    /** Set Date Processed.
    @param DateProcessed Date Processed */
    public void setDateProcessed (Timestamp DateProcessed)
    {
        set_Value ("DateProcessed", DateProcessed);
        
    }
    
    /** Get Date Processed.
    @return Date Processed */
    public Timestamp getDateProcessed() 
    {
        return (Timestamp)get_Value("DateProcessed");
        
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
    
    /** Set Executed.
    @param IsExecuted Executed */
    public void setIsExecuted (boolean IsExecuted)
    {
        set_Value ("IsExecuted", Boolean.valueOf(IsExecuted));
        
    }
    
    /** Get Executed.
    @return Executed */
    public boolean isExecuted() 
    {
        return get_ValueAsBoolean("IsExecuted");
        
    }
    
    /** Set Department.
    @param M_Department_ID Department */
    public void setM_Department_ID (int M_Department_ID)
    {
        if (M_Department_ID <= 0) set_Value ("M_Department_ID", null);
        else
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
        set_Value ("M_RoutingOperation_ID", Integer.valueOf(M_RoutingOperation_ID));
        
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
    
    /** Set Work Order Operation.
    @param M_WorkOrderOperation_ID Production routing operation on a work order */
    public void setM_WorkOrderOperation_ID (int M_WorkOrderOperation_ID)
    {
        if (M_WorkOrderOperation_ID < 1) throw new IllegalArgumentException ("M_WorkOrderOperation_ID is mandatory.");
        set_ValueNoCheck ("M_WorkOrderOperation_ID", Integer.valueOf(M_WorkOrderOperation_ID));
        
    }
    
    /** Get Work Order Operation.
    @return Production routing operation on a work order */
    public int getM_WorkOrderOperation_ID() 
    {
        return get_ValueAsInt("M_WorkOrderOperation_ID");
        
    }
    
    /** Set Work Order.
    @param M_WorkOrder_ID Work Order */
    public void setM_WorkOrder_ID (int M_WorkOrder_ID)
    {
        if (M_WorkOrder_ID < 1) throw new IllegalArgumentException ("M_WorkOrder_ID is mandatory.");
        set_ValueNoCheck ("M_WorkOrder_ID", Integer.valueOf(M_WorkOrder_ID));
        
    }
    
    /** Get Work Order.
    @return Work Order */
    public int getM_WorkOrder_ID() 
    {
        return get_ValueAsInt("M_WorkOrder_ID");
        
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
    
    /** Set Qty Assembled.
    @param QtyAssembled Quantity finished at a production routing step */
    public void setQtyAssembled (java.math.BigDecimal QtyAssembled)
    {
        if (QtyAssembled == null) throw new IllegalArgumentException ("QtyAssembled is mandatory.");
        set_Value ("QtyAssembled", QtyAssembled);
        
    }
    
    /** Get Qty Assembled.
    @return Quantity finished at a production routing step */
    public java.math.BigDecimal getQtyAssembled() 
    {
        return get_ValueAsBigDecimal("QtyAssembled");
        
    }
    
    /** Set Qty Rejected.
    @param QtyRejected Quantity rejected at a production routing step */
    public void setQtyRejected (java.math.BigDecimal QtyRejected)
    {
        if (QtyRejected == null) throw new IllegalArgumentException ("QtyRejected is mandatory.");
        set_Value ("QtyRejected", QtyRejected);
        
    }
    
    /** Get Qty Rejected.
    @return Quantity rejected at a production routing step */
    public java.math.BigDecimal getQtyRejected() 
    {
        return get_ValueAsBigDecimal("QtyRejected");
        
    }
    
    /** Set Qty Scrapped.
    @param QtyScrapped Quantity scrapped at a production routing step */
    public void setQtyScrapped (java.math.BigDecimal QtyScrapped)
    {
        if (QtyScrapped == null) throw new IllegalArgumentException ("QtyScrapped is mandatory.");
        set_Value ("QtyScrapped", QtyScrapped);
        
    }
    
    /** Get Qty Scrapped.
    @return Quantity scrapped at a production routing step */
    public java.math.BigDecimal getQtyScrapped() 
    {
        return get_ValueAsBigDecimal("QtyScrapped");
        
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
