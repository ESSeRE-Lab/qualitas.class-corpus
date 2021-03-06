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
/** Generated Model for T_Aging
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_T_Aging extends PO
{
    /** Standard Constructor
    @param ctx context
    @param T_Aging_ID id
    @param trx transaction
    */
    public X_T_Aging (Ctx ctx, int T_Aging_ID, Trx trx)
    {
        super (ctx, T_Aging_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (T_Aging_ID == 0)
        {
            setAD_PInstance_ID (0);
            setC_BP_Group_ID (0);
            setC_BPartner_ID (0);
            setC_Currency_ID (0);
            setC_InvoicePaySchedule_ID (0);
            setC_Invoice_ID (0);
            setDue0 (Env.ZERO);
            setDue0_30 (Env.ZERO);
            setDue0_7 (Env.ZERO);
            setDue1_7 (Env.ZERO);
            setDue31_60 (Env.ZERO);
            setDue31_Plus (Env.ZERO);
            setDue61_90 (Env.ZERO);
            setDue61_Plus (Env.ZERO);
            setDue8_30 (Env.ZERO);
            setDue91_Plus (Env.ZERO);
            setDueAmt (Env.ZERO);
            setDueDate (new Timestamp(System.currentTimeMillis()));
            setInvoicedAmt (Env.ZERO);
            setIsListInvoices (false);
            setIsSOTrx (false);
            setOpenAmt (Env.ZERO);
            setPastDue1_30 (Env.ZERO);
            setPastDue1_7 (Env.ZERO);
            setPastDue31_60 (Env.ZERO);
            setPastDue31_Plus (Env.ZERO);
            setPastDue61_90 (Env.ZERO);
            setPastDue61_Plus (Env.ZERO);
            setPastDue8_30 (Env.ZERO);
            setPastDue91_Plus (Env.ZERO);
            setPastDueAmt (Env.ZERO);
            setStatementDate (new Timestamp(System.currentTimeMillis()));
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_T_Aging (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27511671668789L;
    /** Last Updated Timestamp 2008-12-17 12:39:12.0 */
    public static final long updatedMS = 1229546352000L;
    /** AD_Table_ID=631 */
    public static final int Table_ID=631;
    
    /** TableName=T_Aging */
    public static final String Table_Name="T_Aging";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"T_Aging");
    
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
    
    /** Set Business Partner Group.
    @param C_BP_Group_ID Business Partner Group */
    public void setC_BP_Group_ID (int C_BP_Group_ID)
    {
        if (C_BP_Group_ID < 1) throw new IllegalArgumentException ("C_BP_Group_ID is mandatory.");
        set_Value ("C_BP_Group_ID", Integer.valueOf(C_BP_Group_ID));
        
    }
    
    /** Get Business Partner Group.
    @return Business Partner Group */
    public int getC_BP_Group_ID() 
    {
        return get_ValueAsInt("C_BP_Group_ID");
        
    }
    
    /** Set Business Partner.
    @param C_BPartner_ID Identifies a Business Partner */
    public void setC_BPartner_ID (int C_BPartner_ID)
    {
        if (C_BPartner_ID < 1) throw new IllegalArgumentException ("C_BPartner_ID is mandatory.");
        set_ValueNoCheck ("C_BPartner_ID", Integer.valueOf(C_BPartner_ID));
        
    }
    
    /** Get Business Partner.
    @return Identifies a Business Partner */
    public int getC_BPartner_ID() 
    {
        return get_ValueAsInt("C_BPartner_ID");
        
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
    
    /** Set Currency.
    @param C_Currency_ID The Currency for this record */
    public void setC_Currency_ID (int C_Currency_ID)
    {
        if (C_Currency_ID < 1) throw new IllegalArgumentException ("C_Currency_ID is mandatory.");
        set_ValueNoCheck ("C_Currency_ID", Integer.valueOf(C_Currency_ID));
        
    }
    
    /** Get Currency.
    @return The Currency for this record */
    public int getC_Currency_ID() 
    {
        return get_ValueAsInt("C_Currency_ID");
        
    }
    
    /** Set Invoice Payment Schedule.
    @param C_InvoicePaySchedule_ID Invoice Payment Schedule */
    public void setC_InvoicePaySchedule_ID (int C_InvoicePaySchedule_ID)
    {
        if (C_InvoicePaySchedule_ID < 1) throw new IllegalArgumentException ("C_InvoicePaySchedule_ID is mandatory.");
        set_ValueNoCheck ("C_InvoicePaySchedule_ID", Integer.valueOf(C_InvoicePaySchedule_ID));
        
    }
    
    /** Get Invoice Payment Schedule.
    @return Invoice Payment Schedule */
    public int getC_InvoicePaySchedule_ID() 
    {
        return get_ValueAsInt("C_InvoicePaySchedule_ID");
        
    }
    
    /** Set Invoice.
    @param C_Invoice_ID Invoice Identifier */
    public void setC_Invoice_ID (int C_Invoice_ID)
    {
        if (C_Invoice_ID < 1) throw new IllegalArgumentException ("C_Invoice_ID is mandatory.");
        set_ValueNoCheck ("C_Invoice_ID", Integer.valueOf(C_Invoice_ID));
        
    }
    
    /** Get Invoice.
    @return Invoice Identifier */
    public int getC_Invoice_ID() 
    {
        return get_ValueAsInt("C_Invoice_ID");
        
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
    
    /** Set Days due.
    @param DaysDue Number of days due (negative: due in number of days) */
    public void setDaysDue (int DaysDue)
    {
        set_Value ("DaysDue", Integer.valueOf(DaysDue));
        
    }
    
    /** Get Days due.
    @return Number of days due (negative: due in number of days) */
    public int getDaysDue() 
    {
        return get_ValueAsInt("DaysDue");
        
    }
    
    /** Set Due Today.
    @param Due0 Due Today */
    public void setDue0 (java.math.BigDecimal Due0)
    {
        if (Due0 == null) throw new IllegalArgumentException ("Due0 is mandatory.");
        set_Value ("Due0", Due0);
        
    }
    
    /** Get Due Today.
    @return Due Today */
    public java.math.BigDecimal getDue0() 
    {
        return get_ValueAsBigDecimal("Due0");
        
    }
    
    /** Set Due Today-30.
    @param Due0_30 Due Today-30 */
    public void setDue0_30 (java.math.BigDecimal Due0_30)
    {
        if (Due0_30 == null) throw new IllegalArgumentException ("Due0_30 is mandatory.");
        set_Value ("Due0_30", Due0_30);
        
    }
    
    /** Get Due Today-30.
    @return Due Today-30 */
    public java.math.BigDecimal getDue0_30() 
    {
        return get_ValueAsBigDecimal("Due0_30");
        
    }
    
    /** Set Due Today-7.
    @param Due0_7 Due Today-7 */
    public void setDue0_7 (java.math.BigDecimal Due0_7)
    {
        if (Due0_7 == null) throw new IllegalArgumentException ("Due0_7 is mandatory.");
        set_Value ("Due0_7", Due0_7);
        
    }
    
    /** Get Due Today-7.
    @return Due Today-7 */
    public java.math.BigDecimal getDue0_7() 
    {
        return get_ValueAsBigDecimal("Due0_7");
        
    }
    
    /** Set Due 1-7.
    @param Due1_7 Due 1-7 */
    public void setDue1_7 (java.math.BigDecimal Due1_7)
    {
        if (Due1_7 == null) throw new IllegalArgumentException ("Due1_7 is mandatory.");
        set_Value ("Due1_7", Due1_7);
        
    }
    
    /** Get Due 1-7.
    @return Due 1-7 */
    public java.math.BigDecimal getDue1_7() 
    {
        return get_ValueAsBigDecimal("Due1_7");
        
    }
    
    /** Set Due 31-60.
    @param Due31_60 Due 31-60 */
    public void setDue31_60 (java.math.BigDecimal Due31_60)
    {
        if (Due31_60 == null) throw new IllegalArgumentException ("Due31_60 is mandatory.");
        set_Value ("Due31_60", Due31_60);
        
    }
    
    /** Get Due 31-60.
    @return Due 31-60 */
    public java.math.BigDecimal getDue31_60() 
    {
        return get_ValueAsBigDecimal("Due31_60");
        
    }
    
    /** Set Due > 31.
    @param Due31_Plus Due > 31 */
    public void setDue31_Plus (java.math.BigDecimal Due31_Plus)
    {
        if (Due31_Plus == null) throw new IllegalArgumentException ("Due31_Plus is mandatory.");
        set_Value ("Due31_Plus", Due31_Plus);
        
    }
    
    /** Get Due > 31.
    @return Due > 31 */
    public java.math.BigDecimal getDue31_Plus() 
    {
        return get_ValueAsBigDecimal("Due31_Plus");
        
    }
    
    /** Set Due 61-90.
    @param Due61_90 Due 61-90 */
    public void setDue61_90 (java.math.BigDecimal Due61_90)
    {
        if (Due61_90 == null) throw new IllegalArgumentException ("Due61_90 is mandatory.");
        set_Value ("Due61_90", Due61_90);
        
    }
    
    /** Get Due 61-90.
    @return Due 61-90 */
    public java.math.BigDecimal getDue61_90() 
    {
        return get_ValueAsBigDecimal("Due61_90");
        
    }
    
    /** Set Due > 61.
    @param Due61_Plus Due > 61 */
    public void setDue61_Plus (java.math.BigDecimal Due61_Plus)
    {
        if (Due61_Plus == null) throw new IllegalArgumentException ("Due61_Plus is mandatory.");
        set_Value ("Due61_Plus", Due61_Plus);
        
    }
    
    /** Get Due > 61.
    @return Due > 61 */
    public java.math.BigDecimal getDue61_Plus() 
    {
        return get_ValueAsBigDecimal("Due61_Plus");
        
    }
    
    /** Set Due 8-30.
    @param Due8_30 Due 8-30 */
    public void setDue8_30 (java.math.BigDecimal Due8_30)
    {
        if (Due8_30 == null) throw new IllegalArgumentException ("Due8_30 is mandatory.");
        set_Value ("Due8_30", Due8_30);
        
    }
    
    /** Get Due 8-30.
    @return Due 8-30 */
    public java.math.BigDecimal getDue8_30() 
    {
        return get_ValueAsBigDecimal("Due8_30");
        
    }
    
    /** Set Due > 91.
    @param Due91_Plus Due > 91 */
    public void setDue91_Plus (java.math.BigDecimal Due91_Plus)
    {
        if (Due91_Plus == null) throw new IllegalArgumentException ("Due91_Plus is mandatory.");
        set_Value ("Due91_Plus", Due91_Plus);
        
    }
    
    /** Get Due > 91.
    @return Due > 91 */
    public java.math.BigDecimal getDue91_Plus() 
    {
        return get_ValueAsBigDecimal("Due91_Plus");
        
    }
    
    /** Set Amount due.
    @param DueAmt Amount of the payment due */
    public void setDueAmt (java.math.BigDecimal DueAmt)
    {
        if (DueAmt == null) throw new IllegalArgumentException ("DueAmt is mandatory.");
        set_Value ("DueAmt", DueAmt);
        
    }
    
    /** Get Amount due.
    @return Amount of the payment due */
    public java.math.BigDecimal getDueAmt() 
    {
        return get_ValueAsBigDecimal("DueAmt");
        
    }
    
    /** Set Due Date.
    @param DueDate Date when the payment is due */
    public void setDueDate (Timestamp DueDate)
    {
        if (DueDate == null) throw new IllegalArgumentException ("DueDate is mandatory.");
        set_Value ("DueDate", DueDate);
        
    }
    
    /** Get Due Date.
    @return Date when the payment is due */
    public Timestamp getDueDate() 
    {
        return (Timestamp)get_Value("DueDate");
        
    }
    
    /** Set Invoiced Amount.
    @param InvoicedAmt The amount invoiced */
    public void setInvoicedAmt (java.math.BigDecimal InvoicedAmt)
    {
        if (InvoicedAmt == null) throw new IllegalArgumentException ("InvoicedAmt is mandatory.");
        set_Value ("InvoicedAmt", InvoicedAmt);
        
    }
    
    /** Get Invoiced Amount.
    @return The amount invoiced */
    public java.math.BigDecimal getInvoicedAmt() 
    {
        return get_ValueAsBigDecimal("InvoicedAmt");
        
    }
    
    /** Set List Invoices.
    @param IsListInvoices Include List of Invoices */
    public void setIsListInvoices (boolean IsListInvoices)
    {
        set_Value ("IsListInvoices", Boolean.valueOf(IsListInvoices));
        
    }
    
    /** Get List Invoices.
    @return Include List of Invoices */
    public boolean isListInvoices() 
    {
        return get_ValueAsBoolean("IsListInvoices");
        
    }
    
    /** Set Sales Transaction.
    @param IsSOTrx This is a Sales Transaction */
    public void setIsSOTrx (boolean IsSOTrx)
    {
        set_Value ("IsSOTrx", Boolean.valueOf(IsSOTrx));
        
    }
    
    /** Get Sales Transaction.
    @return This is a Sales Transaction */
    public boolean isSOTrx() 
    {
        return get_ValueAsBoolean("IsSOTrx");
        
    }
    
    /** Set Open Amount.
    @param OpenAmt Open item amount */
    public void setOpenAmt (java.math.BigDecimal OpenAmt)
    {
        if (OpenAmt == null) throw new IllegalArgumentException ("OpenAmt is mandatory.");
        set_Value ("OpenAmt", OpenAmt);
        
    }
    
    /** Get Open Amount.
    @return Open item amount */
    public java.math.BigDecimal getOpenAmt() 
    {
        return get_ValueAsBigDecimal("OpenAmt");
        
    }
    
    /** Set Past Due 1-30.
    @param PastDue1_30 Past Due 1-30 */
    public void setPastDue1_30 (java.math.BigDecimal PastDue1_30)
    {
        if (PastDue1_30 == null) throw new IllegalArgumentException ("PastDue1_30 is mandatory.");
        set_Value ("PastDue1_30", PastDue1_30);
        
    }
    
    /** Get Past Due 1-30.
    @return Past Due 1-30 */
    public java.math.BigDecimal getPastDue1_30() 
    {
        return get_ValueAsBigDecimal("PastDue1_30");
        
    }
    
    /** Set Past Due 1-7.
    @param PastDue1_7 Past Due 1-7 */
    public void setPastDue1_7 (java.math.BigDecimal PastDue1_7)
    {
        if (PastDue1_7 == null) throw new IllegalArgumentException ("PastDue1_7 is mandatory.");
        set_Value ("PastDue1_7", PastDue1_7);
        
    }
    
    /** Get Past Due 1-7.
    @return Past Due 1-7 */
    public java.math.BigDecimal getPastDue1_7() 
    {
        return get_ValueAsBigDecimal("PastDue1_7");
        
    }
    
    /** Set Past Due 31-60.
    @param PastDue31_60 Past Due 31-60 */
    public void setPastDue31_60 (java.math.BigDecimal PastDue31_60)
    {
        if (PastDue31_60 == null) throw new IllegalArgumentException ("PastDue31_60 is mandatory.");
        set_Value ("PastDue31_60", PastDue31_60);
        
    }
    
    /** Get Past Due 31-60.
    @return Past Due 31-60 */
    public java.math.BigDecimal getPastDue31_60() 
    {
        return get_ValueAsBigDecimal("PastDue31_60");
        
    }
    
    /** Set Past Due > 31.
    @param PastDue31_Plus Past Due > 31 */
    public void setPastDue31_Plus (java.math.BigDecimal PastDue31_Plus)
    {
        if (PastDue31_Plus == null) throw new IllegalArgumentException ("PastDue31_Plus is mandatory.");
        set_Value ("PastDue31_Plus", PastDue31_Plus);
        
    }
    
    /** Get Past Due > 31.
    @return Past Due > 31 */
    public java.math.BigDecimal getPastDue31_Plus() 
    {
        return get_ValueAsBigDecimal("PastDue31_Plus");
        
    }
    
    /** Set Past Due 61-90.
    @param PastDue61_90 Past Due 61-90 */
    public void setPastDue61_90 (java.math.BigDecimal PastDue61_90)
    {
        if (PastDue61_90 == null) throw new IllegalArgumentException ("PastDue61_90 is mandatory.");
        set_Value ("PastDue61_90", PastDue61_90);
        
    }
    
    /** Get Past Due 61-90.
    @return Past Due 61-90 */
    public java.math.BigDecimal getPastDue61_90() 
    {
        return get_ValueAsBigDecimal("PastDue61_90");
        
    }
    
    /** Set Past Due > 61.
    @param PastDue61_Plus Past Due > 61 */
    public void setPastDue61_Plus (java.math.BigDecimal PastDue61_Plus)
    {
        if (PastDue61_Plus == null) throw new IllegalArgumentException ("PastDue61_Plus is mandatory.");
        set_Value ("PastDue61_Plus", PastDue61_Plus);
        
    }
    
    /** Get Past Due > 61.
    @return Past Due > 61 */
    public java.math.BigDecimal getPastDue61_Plus() 
    {
        return get_ValueAsBigDecimal("PastDue61_Plus");
        
    }
    
    /** Set Past Due 8-30.
    @param PastDue8_30 Past Due 8-30 */
    public void setPastDue8_30 (java.math.BigDecimal PastDue8_30)
    {
        if (PastDue8_30 == null) throw new IllegalArgumentException ("PastDue8_30 is mandatory.");
        set_Value ("PastDue8_30", PastDue8_30);
        
    }
    
    /** Get Past Due 8-30.
    @return Past Due 8-30 */
    public java.math.BigDecimal getPastDue8_30() 
    {
        return get_ValueAsBigDecimal("PastDue8_30");
        
    }
    
    /** Set Past Due > 91.
    @param PastDue91_Plus Past Due > 91 */
    public void setPastDue91_Plus (java.math.BigDecimal PastDue91_Plus)
    {
        if (PastDue91_Plus == null) throw new IllegalArgumentException ("PastDue91_Plus is mandatory.");
        set_Value ("PastDue91_Plus", PastDue91_Plus);
        
    }
    
    /** Get Past Due > 91.
    @return Past Due > 91 */
    public java.math.BigDecimal getPastDue91_Plus() 
    {
        return get_ValueAsBigDecimal("PastDue91_Plus");
        
    }
    
    /** Set Past Due.
    @param PastDueAmt Past Due */
    public void setPastDueAmt (java.math.BigDecimal PastDueAmt)
    {
        if (PastDueAmt == null) throw new IllegalArgumentException ("PastDueAmt is mandatory.");
        set_Value ("PastDueAmt", PastDueAmt);
        
    }
    
    /** Get Past Due.
    @return Past Due */
    public java.math.BigDecimal getPastDueAmt() 
    {
        return get_ValueAsBigDecimal("PastDueAmt");
        
    }
    
    /** Set Statement date.
    @param StatementDate Date of the statement */
    public void setStatementDate (Timestamp StatementDate)
    {
        if (StatementDate == null) throw new IllegalArgumentException ("StatementDate is mandatory.");
        set_Value ("StatementDate", StatementDate);
        
    }
    
    /** Get Statement date.
    @return Date of the statement */
    public Timestamp getStatementDate() 
    {
        return (Timestamp)get_Value("StatementDate");
        
    }
    
    
}
