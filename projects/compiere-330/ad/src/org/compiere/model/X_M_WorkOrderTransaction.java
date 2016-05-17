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
/** Generated Model for M_WorkOrderTransaction
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_WorkOrderTransaction extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_WorkOrderTransaction_ID id
    @param trx transaction
    */
    public X_M_WorkOrderTransaction (Ctx ctx, int M_WorkOrderTransaction_ID, Trx trx)
    {
        super (ctx, M_WorkOrderTransaction_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_WorkOrderTransaction_ID == 0)
        {
            setC_DocType_ID (0);
            setC_UOM_ID (0);	// @#C_UOM_ID@
            setDateAcct (new Timestamp(System.currentTimeMillis()));	// @#Date@
            setDocAction (null);	// CO
            setDocStatus (null);	// DR
            setDocumentNo (null);
            setIsApproved (false);	// @IsApproved@
            setIsComplete (false);	// N
            setIsTransferred (false);
            setM_Product_ID (0);
            setM_WorkOrderTransaction_ID (0);
            setM_WorkOrder_ID (0);
            setPosted (false);	// N
            setProcessed (false);	// N
            setQtyEntered (Env.ZERO);	// 1
            setWOComplete (false);	// N
            setWOTxnSource (null);	// M
            setWorkOrderTxnType (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_WorkOrderTransaction (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27509346820789L;
    /** Last Updated Timestamp 2008-11-20 14:51:44.0 */
    public static final long updatedMS = 1227221504000L;
    /** AD_Table_ID=1030 */
    public static final int Table_ID=1030;
    
    /** TableName=M_WorkOrderTransaction */
    public static final String Table_Name="M_WorkOrderTransaction";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_WorkOrderTransaction");
    
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
    
    
    /** C_DocType_ID AD_Reference_ID=170 */
    public static final int C_DOCTYPE_ID_AD_Reference_ID=170;
    /** Set Document Type.
    @param C_DocType_ID Document type or rules */
    public void setC_DocType_ID (int C_DocType_ID)
    {
        if (C_DocType_ID < 0) throw new IllegalArgumentException ("C_DocType_ID is mandatory.");
        set_ValueNoCheck ("C_DocType_ID", Integer.valueOf(C_DocType_ID));
        
    }
    
    /** Get Document Type.
    @return Document type or rules */
    public int getC_DocType_ID() 
    {
        return get_ValueAsInt("C_DocType_ID");
        
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
    
    /** Set UOM.
    @param C_UOM_ID Unit of Measure */
    public void setC_UOM_ID (int C_UOM_ID)
    {
        if (C_UOM_ID < 1) throw new IllegalArgumentException ("C_UOM_ID is mandatory.");
        set_Value ("C_UOM_ID", Integer.valueOf(C_UOM_ID));
        
    }
    
    /** Get UOM.
    @return Unit of Measure */
    public int getC_UOM_ID() 
    {
        return get_ValueAsInt("C_UOM_ID");
        
    }
    
    /** Set Account Date.
    @param DateAcct General Ledger Date */
    public void setDateAcct (Timestamp DateAcct)
    {
        if (DateAcct == null) throw new IllegalArgumentException ("DateAcct is mandatory.");
        set_Value ("DateAcct", DateAcct);
        
    }
    
    /** Get Account Date.
    @return General Ledger Date */
    public Timestamp getDateAcct() 
    {
        return (Timestamp)get_Value("DateAcct");
        
    }
    
    /** Set Transaction Date.
    @param DateTrx Transaction Date */
    public void setDateTrx (Timestamp DateTrx)
    {
        set_ValueNoCheck ("DateTrx", DateTrx);
        
    }
    
    /** Get Transaction Date.
    @return Transaction Date */
    public Timestamp getDateTrx() 
    {
        return (Timestamp)get_Value("DateTrx");
        
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
    
    
    /** DocAction AD_Reference_ID=135 */
    public static final int DOCACTION_AD_Reference_ID=135;
    /** <None> = -- */
    public static final String DOCACTION_None = X_Ref__Document_Action.NONE.getValue();
    /** Approve = AP */
    public static final String DOCACTION_Approve = X_Ref__Document_Action.APPROVE.getValue();
    /** Close = CL */
    public static final String DOCACTION_Close = X_Ref__Document_Action.CLOSE.getValue();
    /** Complete = CO */
    public static final String DOCACTION_Complete = X_Ref__Document_Action.COMPLETE.getValue();
    /** Invalidate = IN */
    public static final String DOCACTION_Invalidate = X_Ref__Document_Action.INVALIDATE.getValue();
    /** Post = PO */
    public static final String DOCACTION_Post = X_Ref__Document_Action.POST.getValue();
    /** Prepare = PR */
    public static final String DOCACTION_Prepare = X_Ref__Document_Action.PREPARE.getValue();
    /** Reverse - Accrual = RA */
    public static final String DOCACTION_Reverse_Accrual = X_Ref__Document_Action.REVERSE__ACCRUAL.getValue();
    /** Reverse - Correct = RC */
    public static final String DOCACTION_Reverse_Correct = X_Ref__Document_Action.REVERSE__CORRECT.getValue();
    /** Re-activate = RE */
    public static final String DOCACTION_Re_Activate = X_Ref__Document_Action.RE__ACTIVATE.getValue();
    /** Reject = RJ */
    public static final String DOCACTION_Reject = X_Ref__Document_Action.REJECT.getValue();
    /** Void = VO */
    public static final String DOCACTION_Void = X_Ref__Document_Action.VOID.getValue();
    /** Wait Complete = WC */
    public static final String DOCACTION_WaitComplete = X_Ref__Document_Action.WAIT_COMPLETE.getValue();
    /** Unlock = XL */
    public static final String DOCACTION_Unlock = X_Ref__Document_Action.UNLOCK.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isDocActionValid(String test)
    {
         return X_Ref__Document_Action.isValid(test);
         
    }
    /** Set Document Action.
    @param DocAction The targeted status of the document */
    public void setDocAction (String DocAction)
    {
        if (DocAction == null) throw new IllegalArgumentException ("DocAction is mandatory");
        if (!isDocActionValid(DocAction))
        throw new IllegalArgumentException ("DocAction Invalid value - " + DocAction + " - Reference_ID=135 - -- - AP - CL - CO - IN - PO - PR - RA - RC - RE - RJ - VO - WC - XL");
        set_Value ("DocAction", DocAction);
        
    }
    
    /** Get Document Action.
    @return The targeted status of the document */
    public String getDocAction() 
    {
        return (String)get_Value("DocAction");
        
    }
    
    
    /** DocStatus AD_Reference_ID=131 */
    public static final int DOCSTATUS_AD_Reference_ID=131;
    /** Unknown = ?? */
    public static final String DOCSTATUS_Unknown = X_Ref__Document_Status.UNKNOWN.getValue();
    /** Approved = AP */
    public static final String DOCSTATUS_Approved = X_Ref__Document_Status.APPROVED.getValue();
    /** Closed = CL */
    public static final String DOCSTATUS_Closed = X_Ref__Document_Status.CLOSED.getValue();
    /** Completed = CO */
    public static final String DOCSTATUS_Completed = X_Ref__Document_Status.COMPLETED.getValue();
    /** Drafted = DR */
    public static final String DOCSTATUS_Drafted = X_Ref__Document_Status.DRAFTED.getValue();
    /** Invalid = IN */
    public static final String DOCSTATUS_Invalid = X_Ref__Document_Status.INVALID.getValue();
    /** In Progress = IP */
    public static final String DOCSTATUS_InProgress = X_Ref__Document_Status.IN_PROGRESS.getValue();
    /** Not Approved = NA */
    public static final String DOCSTATUS_NotApproved = X_Ref__Document_Status.NOT_APPROVED.getValue();
    /** Reversed = RE */
    public static final String DOCSTATUS_Reversed = X_Ref__Document_Status.REVERSED.getValue();
    /** Voided = VO */
    public static final String DOCSTATUS_Voided = X_Ref__Document_Status.VOIDED.getValue();
    /** Waiting Confirmation = WC */
    public static final String DOCSTATUS_WaitingConfirmation = X_Ref__Document_Status.WAITING_CONFIRMATION.getValue();
    /** Waiting Payment = WP */
    public static final String DOCSTATUS_WaitingPayment = X_Ref__Document_Status.WAITING_PAYMENT.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isDocStatusValid(String test)
    {
         return X_Ref__Document_Status.isValid(test);
         
    }
    /** Set Document Status.
    @param DocStatus The current status of the document */
    public void setDocStatus (String DocStatus)
    {
        if (DocStatus == null) throw new IllegalArgumentException ("DocStatus is mandatory");
        if (!isDocStatusValid(DocStatus))
        throw new IllegalArgumentException ("DocStatus Invalid value - " + DocStatus + " - Reference_ID=131 - ?? - AP - CL - CO - DR - IN - IP - NA - RE - VO - WC - WP");
        set_Value ("DocStatus", DocStatus);
        
    }
    
    /** Get Document Status.
    @return The current status of the document */
    public String getDocStatus() 
    {
        return (String)get_Value("DocStatus");
        
    }
    
    /** Set Document No.
    @param DocumentNo Document sequence number of the document */
    public void setDocumentNo (String DocumentNo)
    {
        if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory.");
        set_Value ("DocumentNo", DocumentNo);
        
    }
    
    /** Get Document No.
    @return Document sequence number of the document */
    public String getDocumentNo() 
    {
        return (String)get_Value("DocumentNo");
        
    }
    
    /** Set Create Component Txn Lines.
    @param GenerateLines Create Component Txn Lines */
    public void setGenerateLines (String GenerateLines)
    {
        set_Value ("GenerateLines", GenerateLines);
        
    }
    
    /** Get Create Component Txn Lines.
    @return Create Component Txn Lines */
    public String getGenerateLines() 
    {
        return (String)get_Value("GenerateLines");
        
    }
    
    /** Set Comment.
    @param Help Comment, Help or Hint */
    public void setHelp (String Help)
    {
        set_Value ("Help", Help);
        
    }
    
    /** Get Comment.
    @return Comment, Help or Hint */
    public String getHelp() 
    {
        return (String)get_Value("Help");
        
    }
    
    /** Set Approved.
    @param IsApproved Indicates if this document requires approval */
    public void setIsApproved (boolean IsApproved)
    {
        set_Value ("IsApproved", Boolean.valueOf(IsApproved));
        
    }
    
    /** Get Approved.
    @return Indicates if this document requires approval */
    public boolean isApproved() 
    {
        return get_ValueAsBoolean("IsApproved");
        
    }
    
    /** Set Complete.
    @param IsComplete It is complete */
    public void setIsComplete (boolean IsComplete)
    {
        set_Value ("IsComplete", Boolean.valueOf(IsComplete));
        
    }
    
    /** Get Complete.
    @return It is complete */
    public boolean isComplete() 
    {
        return get_ValueAsBoolean("IsComplete");
        
    }
    
    /** Set Transferred.
    @param IsTransferred Transferred to General Ledger (i.e. accounted) */
    public void setIsTransferred (boolean IsTransferred)
    {
        set_Value ("IsTransferred", Boolean.valueOf(IsTransferred));
        
    }
    
    /** Get Transferred.
    @return Transferred to General Ledger (i.e. accounted) */
    public boolean isTransferred() 
    {
        return get_ValueAsBoolean("IsTransferred");
        
    }
    
    
    /** M_Locator_ID AD_Reference_ID=446 */
    public static final int M_LOCATOR_ID_AD_Reference_ID=446;
    /** Set Locator.
    @param M_Locator_ID Warehouse Locator */
    public void setM_Locator_ID (int M_Locator_ID)
    {
        if (M_Locator_ID <= 0) set_Value ("M_Locator_ID", null);
        else
        set_Value ("M_Locator_ID", Integer.valueOf(M_Locator_ID));
        
    }
    
    /** Get Locator.
    @return Warehouse Locator */
    public int getM_Locator_ID() 
    {
        return get_ValueAsInt("M_Locator_ID");
        
    }
    
    /** Set Product.
    @param M_Product_ID Product, Service, Item */
    public void setM_Product_ID (int M_Product_ID)
    {
        if (M_Product_ID < 1) throw new IllegalArgumentException ("M_Product_ID is mandatory.");
        set_Value ("M_Product_ID", Integer.valueOf(M_Product_ID));
        
    }
    
    /** Get Product.
    @return Product, Service, Item */
    public int getM_Product_ID() 
    {
        return get_ValueAsInt("M_Product_ID");
        
    }
    
    /** Set Work Order Transaction.
    @param M_WorkOrderTransaction_ID Work Order Transaction */
    public void setM_WorkOrderTransaction_ID (int M_WorkOrderTransaction_ID)
    {
        if (M_WorkOrderTransaction_ID < 1) throw new IllegalArgumentException ("M_WorkOrderTransaction_ID is mandatory.");
        set_ValueNoCheck ("M_WorkOrderTransaction_ID", Integer.valueOf(M_WorkOrderTransaction_ID));
        
    }
    
    /** Get Work Order Transaction.
    @return Work Order Transaction */
    public int getM_WorkOrderTransaction_ID() 
    {
        return get_ValueAsInt("M_WorkOrderTransaction_ID");
        
    }
    
    /** Set Work Order.
    @param M_WorkOrder_ID Work Order */
    public void setM_WorkOrder_ID (int M_WorkOrder_ID)
    {
        if (M_WorkOrder_ID < 1) throw new IllegalArgumentException ("M_WorkOrder_ID is mandatory.");
        set_Value ("M_WorkOrder_ID", Integer.valueOf(M_WorkOrder_ID));
        
    }
    
    /** Get Work Order.
    @return Work Order */
    public int getM_WorkOrder_ID() 
    {
        return get_ValueAsInt("M_WorkOrder_ID");
        
    }
    
    
    /** OperationFrom_ID AD_Reference_ID=451 */
    public static final int OPERATIONFROM_ID_AD_Reference_ID=451;
    /** Set Operation From.
    @param OperationFrom_ID Process the operations in a work order transaction starting at this one. */
    public void setOperationFrom_ID (int OperationFrom_ID)
    {
        if (OperationFrom_ID <= 0) set_Value ("OperationFrom_ID", null);
        else
        set_Value ("OperationFrom_ID", Integer.valueOf(OperationFrom_ID));
        
    }
    
    /** Get Operation From.
    @return Process the operations in a work order transaction starting at this one. */
    public int getOperationFrom_ID() 
    {
        return get_ValueAsInt("OperationFrom_ID");
        
    }
    
    
    /** OperationTo_ID AD_Reference_ID=451 */
    public static final int OPERATIONTO_ID_AD_Reference_ID=451;
    /** Set Operation To.
    @param OperationTo_ID Process the operations in a work order transaction ending at this one (inclusive). */
    public void setOperationTo_ID (int OperationTo_ID)
    {
        if (OperationTo_ID <= 0) set_Value ("OperationTo_ID", null);
        else
        set_Value ("OperationTo_ID", Integer.valueOf(OperationTo_ID));
        
    }
    
    /** Get Operation To.
    @return Process the operations in a work order transaction ending at this one (inclusive). */
    public int getOperationTo_ID() 
    {
        return get_ValueAsInt("OperationTo_ID");
        
    }
    
    
    /** ParentWorkOrderTxn_ID AD_Reference_ID=452 */
    public static final int PARENTWORKORDERTXN_ID_AD_Reference_ID=452;
    /** Set Parent Work Order Transaction.
    @param ParentWorkOrderTxn_ID Work Order Transaction that created this Work Order Transaction */
    public void setParentWorkOrderTxn_ID (int ParentWorkOrderTxn_ID)
    {
        if (ParentWorkOrderTxn_ID <= 0) set_ValueNoCheck ("ParentWorkOrderTxn_ID", null);
        else
        set_ValueNoCheck ("ParentWorkOrderTxn_ID", Integer.valueOf(ParentWorkOrderTxn_ID));
        
    }
    
    /** Get Parent Work Order Transaction.
    @return Work Order Transaction that created this Work Order Transaction */
    public int getParentWorkOrderTxn_ID() 
    {
        return get_ValueAsInt("ParentWorkOrderTxn_ID");
        
    }
    
    /** Set Posted.
    @param Posted Posting status */
    public void setPosted (boolean Posted)
    {
        set_ValueNoCheck ("Posted", Boolean.valueOf(Posted));
        
    }
    
    /** Get Posted.
    @return Posting status */
    public boolean isPosted() 
    {
        return get_ValueAsBoolean("Posted");
        
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
    
    /** Set Quantity.
    @param QtyEntered The Quantity Entered is based on the selected UoM */
    public void setQtyEntered (java.math.BigDecimal QtyEntered)
    {
        if (QtyEntered == null) throw new IllegalArgumentException ("QtyEntered is mandatory.");
        set_Value ("QtyEntered", QtyEntered);
        
    }
    
    /** Get Quantity.
    @return The Quantity Entered is based on the selected UoM */
    public java.math.BigDecimal getQtyEntered() 
    {
        return get_ValueAsBigDecimal("QtyEntered");
        
    }
    
    
    /** User1_ID AD_Reference_ID=134 */
    public static final int USER1_ID_AD_Reference_ID=134;
    /** Set User List 1.
    @param User1_ID User defined list element #1 */
    public void setUser1_ID (int User1_ID)
    {
        if (User1_ID <= 0) set_Value ("User1_ID", null);
        else
        set_Value ("User1_ID", Integer.valueOf(User1_ID));
        
    }
    
    /** Get User List 1.
    @return User defined list element #1 */
    public int getUser1_ID() 
    {
        return get_ValueAsInt("User1_ID");
        
    }
    
    
    /** User2_ID AD_Reference_ID=137 */
    public static final int USER2_ID_AD_Reference_ID=137;
    /** Set User List 2.
    @param User2_ID User defined list element #2 */
    public void setUser2_ID (int User2_ID)
    {
        if (User2_ID <= 0) set_Value ("User2_ID", null);
        else
        set_Value ("User2_ID", Integer.valueOf(User2_ID));
        
    }
    
    /** Get User List 2.
    @return User defined list element #2 */
    public int getUser2_ID() 
    {
        return get_ValueAsInt("User2_ID");
        
    }
    
    /** Set Complete this Assembly.
    @param WOComplete Indicates that a move transaction should include a completion for the assembly. */
    public void setWOComplete (boolean WOComplete)
    {
        set_Value ("WOComplete", Boolean.valueOf(WOComplete));
        
    }
    
    /** Get Complete this Assembly.
    @return Indicates that a move transaction should include a completion for the assembly. */
    public boolean isWOComplete() 
    {
        return get_ValueAsBoolean("WOComplete");
        
    }
    
    
    /** WOTxnSource AD_Reference_ID=453 */
    public static final int WOTXNSOURCE_AD_Reference_ID=453;
    /** Generated = G */
    public static final String WOTXNSOURCE_Generated = X_Ref_M_WorkOrderTransaction_source_type.GENERATED.getValue();
    /** Manually Entered = M */
    public static final String WOTXNSOURCE_ManuallyEntered = X_Ref_M_WorkOrderTransaction_source_type.MANUALLY_ENTERED.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isWOTxnSourceValid(String test)
    {
         return X_Ref_M_WorkOrderTransaction_source_type.isValid(test);
         
    }
    /** Set Transaction Source.
    @param WOTxnSource Indicates where the work order transaction originated. */
    public void setWOTxnSource (String WOTxnSource)
    {
        if (WOTxnSource == null) throw new IllegalArgumentException ("WOTxnSource is mandatory");
        if (!isWOTxnSourceValid(WOTxnSource))
        throw new IllegalArgumentException ("WOTxnSource Invalid value - " + WOTxnSource + " - Reference_ID=453 - G - M");
        set_ValueNoCheck ("WOTxnSource", WOTxnSource);
        
    }
    
    /** Get Transaction Source.
    @return Indicates where the work order transaction originated. */
    public String getWOTxnSource() 
    {
        return (String)get_Value("WOTxnSource");
        
    }
    
    
    /** WorkOrderTxnType AD_Reference_ID=454 */
    public static final int WORKORDERTXNTYPE_AD_Reference_ID=454;
    /** Assembly Issue to Inventory = AI */
    public static final String WORKORDERTXNTYPE_AssemblyIssueToInventory = X_Ref_M_WorkOrderTransaction_Type.ASSEMBLY_ISSUE_TO_INVENTORY.getValue();
    /** Assembly Return from Inventory = AR */
    public static final String WORKORDERTXNTYPE_AssemblyReturnFromInventory = X_Ref_M_WorkOrderTransaction_Type.ASSEMBLY_RETURN_FROM_INVENTORY.getValue();
    /** Component Issue to Work Order = CI */
    public static final String WORKORDERTXNTYPE_ComponentIssueToWorkOrder = X_Ref_M_WorkOrderTransaction_Type.COMPONENT_ISSUE_TO_WORK_ORDER.getValue();
    /** Component Return from Work Order = CR */
    public static final String WORKORDERTXNTYPE_ComponentReturnFromWorkOrder = X_Ref_M_WorkOrderTransaction_Type.COMPONENT_RETURN_FROM_WORK_ORDER.getValue();
    /** Work Order Movement = WM */
    public static final String WORKORDERTXNTYPE_WorkOrderMovement = X_Ref_M_WorkOrderTransaction_Type.WORK_ORDER_MOVEMENT.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isWorkOrderTxnTypeValid(String test)
    {
         return X_Ref_M_WorkOrderTransaction_Type.isValid(test);
         
    }
    /** Set Transaction Type.
    @param WorkOrderTxnType Transaction Type */
    public void setWorkOrderTxnType (String WorkOrderTxnType)
    {
        if (WorkOrderTxnType == null) throw new IllegalArgumentException ("WorkOrderTxnType is mandatory");
        if (!isWorkOrderTxnTypeValid(WorkOrderTxnType))
        throw new IllegalArgumentException ("WorkOrderTxnType Invalid value - " + WorkOrderTxnType + " - Reference_ID=454 - AI - AR - CI - CR - WM");
        set_Value ("WorkOrderTxnType", WorkOrderTxnType);
        
    }
    
    /** Get Transaction Type.
    @return Transaction Type */
    public String getWorkOrderTxnType() 
    {
        return (String)get_Value("WorkOrderTxnType");
        
    }
    
    
}
