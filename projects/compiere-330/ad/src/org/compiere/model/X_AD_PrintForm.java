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
/** Generated Model for AD_PrintForm
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_AD_PrintForm extends PO
{
    /** Standard Constructor
    @param ctx context
    @param AD_PrintForm_ID id
    @param trx transaction
    */
    public X_AD_PrintForm (Ctx ctx, int AD_PrintForm_ID, Trx trx)
    {
        super (ctx, AD_PrintForm_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (AD_PrintForm_ID == 0)
        {
            setAD_PrintForm_ID (0);
            setName (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_AD_PrintForm (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27502176101789L;
    /** Last Updated Timestamp 2008-08-29 15:59:45.0 */
    public static final long updatedMS = 1220050785000L;
    /** AD_Table_ID=454 */
    public static final int Table_ID=454;
    
    /** TableName=AD_PrintForm */
    public static final String Table_Name="AD_PrintForm";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"AD_PrintForm");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Print Form.
    @param AD_PrintForm_ID Form */
    public void setAD_PrintForm_ID (int AD_PrintForm_ID)
    {
        if (AD_PrintForm_ID < 1) throw new IllegalArgumentException ("AD_PrintForm_ID is mandatory.");
        set_ValueNoCheck ("AD_PrintForm_ID", Integer.valueOf(AD_PrintForm_ID));
        
    }
    
    /** Get Print Form.
    @return Form */
    public int getAD_PrintForm_ID() 
    {
        return get_ValueAsInt("AD_PrintForm_ID");
        
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
    
    
    /** Inventory_MailText_ID AD_Reference_ID=274 */
    public static final int INVENTORY_MAILTEXT_ID_AD_Reference_ID=274;
    /** Set Inventory Mail Text.
    @param Inventory_MailText_ID Email text used for sending physical inventory */
    public void setInventory_MailText_ID (int Inventory_MailText_ID)
    {
        if (Inventory_MailText_ID <= 0) set_Value ("Inventory_MailText_ID", null);
        else
        set_Value ("Inventory_MailText_ID", Integer.valueOf(Inventory_MailText_ID));
        
    }
    
    /** Get Inventory Mail Text.
    @return Email text used for sending physical inventory */
    public int getInventory_MailText_ID() 
    {
        return get_ValueAsInt("Inventory_MailText_ID");
        
    }
    
    
    /** Inventory_PrintFormat_ID AD_Reference_ID=404 */
    public static final int INVENTORY_PRINTFORMAT_ID_AD_Reference_ID=404;
    /** Set Inventory Print Format.
    @param Inventory_PrintFormat_ID Print Format for printing Physical Inventory */
    public void setInventory_PrintFormat_ID (int Inventory_PrintFormat_ID)
    {
        if (Inventory_PrintFormat_ID <= 0) set_Value ("Inventory_PrintFormat_ID", null);
        else
        set_Value ("Inventory_PrintFormat_ID", Integer.valueOf(Inventory_PrintFormat_ID));
        
    }
    
    /** Get Inventory Print Format.
    @return Print Format for printing Physical Inventory */
    public int getInventory_PrintFormat_ID() 
    {
        return get_ValueAsInt("Inventory_PrintFormat_ID");
        
    }
    
    
    /** Invoice_MailText_ID AD_Reference_ID=274 */
    public static final int INVOICE_MAILTEXT_ID_AD_Reference_ID=274;
    /** Set Invoice Mail Text.
    @param Invoice_MailText_ID Email text used for sending invoices */
    public void setInvoice_MailText_ID (int Invoice_MailText_ID)
    {
        if (Invoice_MailText_ID <= 0) set_Value ("Invoice_MailText_ID", null);
        else
        set_Value ("Invoice_MailText_ID", Integer.valueOf(Invoice_MailText_ID));
        
    }
    
    /** Get Invoice Mail Text.
    @return Email text used for sending invoices */
    public int getInvoice_MailText_ID() 
    {
        return get_ValueAsInt("Invoice_MailText_ID");
        
    }
    
    
    /** Invoice_PrintFormat_ID AD_Reference_ID=261 */
    public static final int INVOICE_PRINTFORMAT_ID_AD_Reference_ID=261;
    /** Set Invoice Print Format.
    @param Invoice_PrintFormat_ID Print Format for printing Invoices */
    public void setInvoice_PrintFormat_ID (int Invoice_PrintFormat_ID)
    {
        if (Invoice_PrintFormat_ID <= 0) set_Value ("Invoice_PrintFormat_ID", null);
        else
        set_Value ("Invoice_PrintFormat_ID", Integer.valueOf(Invoice_PrintFormat_ID));
        
    }
    
    /** Get Invoice Print Format.
    @return Print Format for printing Invoices */
    public int getInvoice_PrintFormat_ID() 
    {
        return get_ValueAsInt("Invoice_PrintFormat_ID");
        
    }
    
    
    /** Movement_MailText_ID AD_Reference_ID=274 */
    public static final int MOVEMENT_MAILTEXT_ID_AD_Reference_ID=274;
    /** Set Movement Mail Text.
    @param Movement_MailText_ID Email text used for sending Movements */
    public void setMovement_MailText_ID (int Movement_MailText_ID)
    {
        if (Movement_MailText_ID <= 0) set_Value ("Movement_MailText_ID", null);
        else
        set_Value ("Movement_MailText_ID", Integer.valueOf(Movement_MailText_ID));
        
    }
    
    /** Get Movement Mail Text.
    @return Email text used for sending Movements */
    public int getMovement_MailText_ID() 
    {
        return get_ValueAsInt("Movement_MailText_ID");
        
    }
    
    
    /** Movement_PrintFormat_ID AD_Reference_ID=403 */
    public static final int MOVEMENT_PRINTFORMAT_ID_AD_Reference_ID=403;
    /** Set Movement Print Format.
    @param Movement_PrintFormat_ID Print Format for using Movements */
    public void setMovement_PrintFormat_ID (int Movement_PrintFormat_ID)
    {
        if (Movement_PrintFormat_ID <= 0) set_Value ("Movement_PrintFormat_ID", null);
        else
        set_Value ("Movement_PrintFormat_ID", Integer.valueOf(Movement_PrintFormat_ID));
        
    }
    
    /** Get Movement Print Format.
    @return Print Format for using Movements */
    public int getMovement_PrintFormat_ID() 
    {
        return get_ValueAsInt("Movement_PrintFormat_ID");
        
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
    
    
    /** Order_MailText_ID AD_Reference_ID=274 */
    public static final int ORDER_MAILTEXT_ID_AD_Reference_ID=274;
    /** Set Order Mail Text.
    @param Order_MailText_ID Email text used for sending order acknowledgements or quotations */
    public void setOrder_MailText_ID (int Order_MailText_ID)
    {
        if (Order_MailText_ID <= 0) set_Value ("Order_MailText_ID", null);
        else
        set_Value ("Order_MailText_ID", Integer.valueOf(Order_MailText_ID));
        
    }
    
    /** Get Order Mail Text.
    @return Email text used for sending order acknowledgements or quotations */
    public int getOrder_MailText_ID() 
    {
        return get_ValueAsInt("Order_MailText_ID");
        
    }
    
    
    /** Order_PrintFormat_ID AD_Reference_ID=262 */
    public static final int ORDER_PRINTFORMAT_ID_AD_Reference_ID=262;
    /** Set Order Print Format.
    @param Order_PrintFormat_ID Print Format for Orders, Quotes, Offers */
    public void setOrder_PrintFormat_ID (int Order_PrintFormat_ID)
    {
        if (Order_PrintFormat_ID <= 0) set_Value ("Order_PrintFormat_ID", null);
        else
        set_Value ("Order_PrintFormat_ID", Integer.valueOf(Order_PrintFormat_ID));
        
    }
    
    /** Get Order Print Format.
    @return Print Format for Orders, Quotes, Offers */
    public int getOrder_PrintFormat_ID() 
    {
        return get_ValueAsInt("Order_PrintFormat_ID");
        
    }
    
    
    /** Pck_CluTList_Printformat_ID AD_Reference_ID=489 */
    public static final int PCK_CLUTLIST_PRINTFORMAT_ID_AD_Reference_ID=489;
    /** Set Cluster Picking Task List Print Format.
    @param Pck_CluTList_Printformat_ID Print Format for Pick Lists using Cluster Pick Method */
    public void setPck_CluTList_Printformat_ID (int Pck_CluTList_Printformat_ID)
    {
        if (Pck_CluTList_Printformat_ID <= 0) set_Value ("Pck_CluTList_Printformat_ID", null);
        else
        set_Value ("Pck_CluTList_Printformat_ID", Integer.valueOf(Pck_CluTList_Printformat_ID));
        
    }
    
    /** Get Cluster Picking Task List Print Format.
    @return Print Format for Pick Lists using Cluster Pick Method */
    public int getPck_CluTList_Printformat_ID() 
    {
        return get_ValueAsInt("Pck_CluTList_Printformat_ID");
        
    }
    
    
    /** Pck_OrdTList_Printformat_ID AD_Reference_ID=489 */
    public static final int PCK_ORDTLIST_PRINTFORMAT_ID_AD_Reference_ID=489;
    /** Set Order Picking Task List Print Format.
    @param Pck_OrdTList_Printformat_ID Print Format for Pick Lists using Order Pick Method */
    public void setPck_OrdTList_Printformat_ID (int Pck_OrdTList_Printformat_ID)
    {
        if (Pck_OrdTList_Printformat_ID <= 0) set_Value ("Pck_OrdTList_Printformat_ID", null);
        else
        set_Value ("Pck_OrdTList_Printformat_ID", Integer.valueOf(Pck_OrdTList_Printformat_ID));
        
    }
    
    /** Get Order Picking Task List Print Format.
    @return Print Format for Pick Lists using Order Pick Method */
    public int getPck_OrdTList_Printformat_ID() 
    {
        return get_ValueAsInt("Pck_OrdTList_Printformat_ID");
        
    }
    
    
    /** Project_MailText_ID AD_Reference_ID=274 */
    public static final int PROJECT_MAILTEXT_ID_AD_Reference_ID=274;
    /** Set Project Mail Text.
    @param Project_MailText_ID Standard text for Project EMails */
    public void setProject_MailText_ID (int Project_MailText_ID)
    {
        if (Project_MailText_ID <= 0) set_Value ("Project_MailText_ID", null);
        else
        set_Value ("Project_MailText_ID", Integer.valueOf(Project_MailText_ID));
        
    }
    
    /** Get Project Mail Text.
    @return Standard text for Project EMails */
    public int getProject_MailText_ID() 
    {
        return get_ValueAsInt("Project_MailText_ID");
        
    }
    
    
    /** Project_PrintFormat_ID AD_Reference_ID=402 */
    public static final int PROJECT_PRINTFORMAT_ID_AD_Reference_ID=402;
    /** Set Project Print Format.
    @param Project_PrintFormat_ID Standard Project Print Format */
    public void setProject_PrintFormat_ID (int Project_PrintFormat_ID)
    {
        if (Project_PrintFormat_ID <= 0) set_Value ("Project_PrintFormat_ID", null);
        else
        set_Value ("Project_PrintFormat_ID", Integer.valueOf(Project_PrintFormat_ID));
        
    }
    
    /** Get Project Print Format.
    @return Standard Project Print Format */
    public int getProject_PrintFormat_ID() 
    {
        return get_ValueAsInt("Project_PrintFormat_ID");
        
    }
    
    
    /** Put_TList_PrintFormat_ID AD_Reference_ID=489 */
    public static final int PUT_TLIST_PRINTFORMAT_ID_AD_Reference_ID=489;
    /** Set Putaway Task List Print Format.
    @param Put_TList_PrintFormat_ID Putaway List Print Format */
    public void setPut_TList_PrintFormat_ID (int Put_TList_PrintFormat_ID)
    {
        if (Put_TList_PrintFormat_ID <= 0) set_Value ("Put_TList_PrintFormat_ID", null);
        else
        set_Value ("Put_TList_PrintFormat_ID", Integer.valueOf(Put_TList_PrintFormat_ID));
        
    }
    
    /** Get Putaway Task List Print Format.
    @return Putaway List Print Format */
    public int getPut_TList_PrintFormat_ID() 
    {
        return get_ValueAsInt("Put_TList_PrintFormat_ID");
        
    }
    
    
    /** Remittance_MailText_ID AD_Reference_ID=274 */
    public static final int REMITTANCE_MAILTEXT_ID_AD_Reference_ID=274;
    /** Set Remittance Mail Text.
    @param Remittance_MailText_ID Email text used for sending payment remittances */
    public void setRemittance_MailText_ID (int Remittance_MailText_ID)
    {
        if (Remittance_MailText_ID <= 0) set_Value ("Remittance_MailText_ID", null);
        else
        set_Value ("Remittance_MailText_ID", Integer.valueOf(Remittance_MailText_ID));
        
    }
    
    /** Get Remittance Mail Text.
    @return Email text used for sending payment remittances */
    public int getRemittance_MailText_ID() 
    {
        return get_ValueAsInt("Remittance_MailText_ID");
        
    }
    
    
    /** Remittance_PrintFormat_ID AD_Reference_ID=268 */
    public static final int REMITTANCE_PRINTFORMAT_ID_AD_Reference_ID=268;
    /** Set Remittance Print Format.
    @param Remittance_PrintFormat_ID Print Format for separate Remittances */
    public void setRemittance_PrintFormat_ID (int Remittance_PrintFormat_ID)
    {
        if (Remittance_PrintFormat_ID <= 0) set_Value ("Remittance_PrintFormat_ID", null);
        else
        set_Value ("Remittance_PrintFormat_ID", Integer.valueOf(Remittance_PrintFormat_ID));
        
    }
    
    /** Get Remittance Print Format.
    @return Print Format for separate Remittances */
    public int getRemittance_PrintFormat_ID() 
    {
        return get_ValueAsInt("Remittance_PrintFormat_ID");
        
    }
    
    
    /** Rpl_TList_PrintFormat_ID AD_Reference_ID=489 */
    public static final int RPL_TLIST_PRINTFORMAT_ID_AD_Reference_ID=489;
    /** Set Replenishment Task List Print Format.
    @param Rpl_TList_PrintFormat_ID Replenishment List Print Format */
    public void setRpl_TList_PrintFormat_ID (int Rpl_TList_PrintFormat_ID)
    {
        if (Rpl_TList_PrintFormat_ID <= 0) set_Value ("Rpl_TList_PrintFormat_ID", null);
        else
        set_Value ("Rpl_TList_PrintFormat_ID", Integer.valueOf(Rpl_TList_PrintFormat_ID));
        
    }
    
    /** Get Replenishment Task List Print Format.
    @return Replenishment List Print Format */
    public int getRpl_TList_PrintFormat_ID() 
    {
        return get_ValueAsInt("Rpl_TList_PrintFormat_ID");
        
    }
    
    
    /** Shipment_MailText_ID AD_Reference_ID=274 */
    public static final int SHIPMENT_MAILTEXT_ID_AD_Reference_ID=274;
    /** Set Shipment Mail Text.
    @param Shipment_MailText_ID Email text used for sending delivery notes */
    public void setShipment_MailText_ID (int Shipment_MailText_ID)
    {
        if (Shipment_MailText_ID <= 0) set_Value ("Shipment_MailText_ID", null);
        else
        set_Value ("Shipment_MailText_ID", Integer.valueOf(Shipment_MailText_ID));
        
    }
    
    /** Get Shipment Mail Text.
    @return Email text used for sending delivery notes */
    public int getShipment_MailText_ID() 
    {
        return get_ValueAsInt("Shipment_MailText_ID");
        
    }
    
    
    /** Shipment_PrintFormat_ID AD_Reference_ID=263 */
    public static final int SHIPMENT_PRINTFORMAT_ID_AD_Reference_ID=263;
    /** Set Shipment Print Format.
    @param Shipment_PrintFormat_ID Print Format for Shipments, Receipts, Pick Lists */
    public void setShipment_PrintFormat_ID (int Shipment_PrintFormat_ID)
    {
        if (Shipment_PrintFormat_ID <= 0) set_Value ("Shipment_PrintFormat_ID", null);
        else
        set_Value ("Shipment_PrintFormat_ID", Integer.valueOf(Shipment_PrintFormat_ID));
        
    }
    
    /** Get Shipment Print Format.
    @return Print Format for Shipments, Receipts, Pick Lists */
    public int getShipment_PrintFormat_ID() 
    {
        return get_ValueAsInt("Shipment_PrintFormat_ID");
        
    }
    
    
    /** WorkOrder_PrintFormat_ID AD_Reference_ID=469 */
    public static final int WORKORDER_PRINTFORMAT_ID_AD_Reference_ID=469;
    /** Set Work Order Print Format.
    @param WorkOrder_PrintFormat_ID Print Format for printing Work Orders */
    public void setWorkOrder_PrintFormat_ID (int WorkOrder_PrintFormat_ID)
    {
        if (WorkOrder_PrintFormat_ID <= 0) set_Value ("WorkOrder_PrintFormat_ID", null);
        else
        set_Value ("WorkOrder_PrintFormat_ID", Integer.valueOf(WorkOrder_PrintFormat_ID));
        
    }
    
    /** Get Work Order Print Format.
    @return Print Format for printing Work Orders */
    public int getWorkOrder_PrintFormat_ID() 
    {
        return get_ValueAsInt("WorkOrder_PrintFormat_ID");
        
    }
    
    
}
