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
/** Generated Model for Fact_Accumulation
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_Fact_Accumulation extends PO
{
    /** Standard Constructor
    @param ctx context
    @param Fact_Accumulation_ID id
    @param trx transaction
    */
    public X_Fact_Accumulation (Ctx ctx, int Fact_Accumulation_ID, Trx trx)
    {
        super (ctx, Fact_Accumulation_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (Fact_Accumulation_ID == 0)
        {
            setBalanceAccumulation (null);	// M
            setC_AcctSchema_ID (0);
            setDateTo (new Timestamp(System.currentTimeMillis()));
            setFact_Accumulation_ID (0);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_Fact_Accumulation (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27503300546789L;
    /** Last Updated Timestamp 2008-09-11 16:20:30.0 */
    public static final long updatedMS = 1221175230000L;
    /** AD_Table_ID=1068 */
    public static final int Table_ID=1068;
    
    /** TableName=Fact_Accumulation */
    public static final String Table_Name="Fact_Accumulation";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"Fact_Accumulation");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    
    /** BalanceAccumulation AD_Reference_ID=481 */
    public static final int BALANCEACCUMULATION_AD_Reference_ID=481;
    /** Daily = D */
    public static final String BALANCEACCUMULATION_Daily = X_Ref_Fact_Accumulation_Type.DAILY.getValue();
    /** Calendar Month = M */
    public static final String BALANCEACCUMULATION_CalendarMonth = X_Ref_Fact_Accumulation_Type.CALENDAR_MONTH.getValue();
    /** Period of a Compiere Calendar = P */
    public static final String BALANCEACCUMULATION_PeriodOfACompiereCalendar = X_Ref_Fact_Accumulation_Type.PERIOD_OF_A_COMPIERE_CALENDAR.getValue();
    /** Calendar Week = W */
    public static final String BALANCEACCUMULATION_CalendarWeek = X_Ref_Fact_Accumulation_Type.CALENDAR_WEEK.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isBalanceAccumulationValid(String test)
    {
         return X_Ref_Fact_Accumulation_Type.isValid(test);
         
    }
    /** Set Balance Accumulation.
    @param BalanceAccumulation Balance Accumulation Type */
    public void setBalanceAccumulation (String BalanceAccumulation)
    {
        if (BalanceAccumulation == null) throw new IllegalArgumentException ("BalanceAccumulation is mandatory");
        if (!isBalanceAccumulationValid(BalanceAccumulation))
        throw new IllegalArgumentException ("BalanceAccumulation Invalid value - " + BalanceAccumulation + " - Reference_ID=481 - D - M - P - W");
        set_Value ("BalanceAccumulation", BalanceAccumulation);
        
    }
    
    /** Get Balance Accumulation.
    @return Balance Accumulation Type */
    public String getBalanceAccumulation() 
    {
        return (String)get_Value("BalanceAccumulation");
        
    }
    
    /** Set Accounting Schema.
    @param C_AcctSchema_ID Rules for accounting */
    public void setC_AcctSchema_ID (int C_AcctSchema_ID)
    {
        if (C_AcctSchema_ID < 1) throw new IllegalArgumentException ("C_AcctSchema_ID is mandatory.");
        set_Value ("C_AcctSchema_ID", Integer.valueOf(C_AcctSchema_ID));
        
    }
    
    /** Get Accounting Schema.
    @return Rules for accounting */
    public int getC_AcctSchema_ID() 
    {
        return get_ValueAsInt("C_AcctSchema_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getC_AcctSchema_ID()));
        
    }
    
    /** Set Calendar.
    @param C_Calendar_ID Accounting Calendar Name */
    public void setC_Calendar_ID (int C_Calendar_ID)
    {
        if (C_Calendar_ID <= 0) set_Value ("C_Calendar_ID", null);
        else
        set_Value ("C_Calendar_ID", Integer.valueOf(C_Calendar_ID));
        
    }
    
    /** Get Calendar.
    @return Accounting Calendar Name */
    public int getC_Calendar_ID() 
    {
        return get_ValueAsInt("C_Calendar_ID");
        
    }
    
    /** Set Date To.
    @param DateTo End date of a date range */
    public void setDateTo (Timestamp DateTo)
    {
        if (DateTo == null) throw new IllegalArgumentException ("DateTo is mandatory.");
        set_Value ("DateTo", DateTo);
        
    }
    
    /** Get Date To.
    @return End date of a date range */
    public Timestamp getDateTo() 
    {
        return (Timestamp)get_Value("DateTo");
        
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
    
    /** Set Accumulation.
    @param Fact_Accumulation_ID Accounting Balance Accumulation */
    public void setFact_Accumulation_ID (int Fact_Accumulation_ID)
    {
        if (Fact_Accumulation_ID < 1) throw new IllegalArgumentException ("Fact_Accumulation_ID is mandatory.");
        set_ValueNoCheck ("Fact_Accumulation_ID", Integer.valueOf(Fact_Accumulation_ID));
        
    }
    
    /** Get Accumulation.
    @return Accounting Balance Accumulation */
    public int getFact_Accumulation_ID() 
    {
        return get_ValueAsInt("Fact_Accumulation_ID");
        
    }
    
    
}
