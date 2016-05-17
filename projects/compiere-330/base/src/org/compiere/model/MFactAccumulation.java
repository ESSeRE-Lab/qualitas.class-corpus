/******************************************************************************
 * Product: Compiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 3600 Bridge Parkway #102, Redwood City, CA 94065, USA      *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.util.*;


/**
 * 	Accounting Balances Accumulation Model
 *	@author Jorg Janke
 */
public class MFactAccumulation extends X_Fact_Accumulation
{
	/** */
    private static final long serialVersionUID = 4694907481460935528L;

    
    /**
     * 	Get All for Tenant
     *	@param ctx context
     *	@param C_AcctSchema_ID optional acct schema
     *	@return list of Accumulation Rules ordered by AcctSchema and DateTo
     */
    public static ArrayList<MFactAccumulation> getAll (Ctx ctx, int C_AcctSchema_ID)
    {
    	StringBuffer sql = new StringBuffer("SELECT * FROM Fact_Accumulation "
    		+ "WHERE IsActive='Y' AND AD_Client_ID=? ");
    	if (C_AcctSchema_ID > 0)
    		sql.append("AND C_AcctSchema_ID=? ");
    	sql.append("ORDER BY C_AcctSchema_ID, DateTo");
    	ArrayList<MFactAccumulation> list = new ArrayList<MFactAccumulation>();
        PreparedStatement pstmt = null;
        try
        {
	        pstmt = DB.prepareStatement(sql.toString(), (Trx) null);
	        pstmt.setInt(1, ctx.getAD_Client_ID());
	        if (C_AcctSchema_ID > 0)
	        	pstmt.setInt(2, C_AcctSchema_ID);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next())
		        list.add(new MFactAccumulation(ctx, rs, null));
	        rs.close();
	        pstmt.close();
	        pstmt = null;
        }
        catch (Exception e)
        {
        	s_log.log(Level.SEVERE, sql.toString(), e);
        }
        try
        {
	        if (pstmt != null)
		        pstmt.close();
	        pstmt = null;
        }
        catch (Exception e)
        {
	        pstmt = null;
        }
        return list;
    }	//	getAll
    
    /**
     * 	Get Date From.
     * 	Assumes that closed periods are not changed
     *	@param accums accumulations with same acct schema ordered by dateTo
     *	@param dateFrom original
     *	@return dateFrom
     */
    public static Timestamp getDateFrom(ArrayList<MFactAccumulation> accums, Timestamp dateFrom)
    {
    	if (accums.size() == 0 || dateFrom == null)
    		return dateFrom;
    	//
    	Timestamp earliestOK = new Timestamp(0L);
    	if (dateFrom != null)
    		earliestOK = dateFrom;
    	for (MFactAccumulation accum : accums)
        {
	        Timestamp dateTo = accum.getDateTo();
	        if (dateFrom.after(dateTo))
	        	continue;
	        //	Accumulation applies
	        earliestOK = accum.getDateFrom(dateFrom);	//	fix start date;
	        //	Find first open period
	        while (MPeriod.isClosed(accum.getCtx(), earliestOK))	//	assumes also closed before
	        {
	        	Timestamp temp = accum.getDateFromNext(earliestOK);
	        	if (temp == null)
	        		break;
        		earliestOK = temp;
	        }
        }
    	if (dateFrom != null && !dateFrom.equals(earliestOK))
    		s_log.info("Changed from " + dateFrom + " to " + earliestOK);
    	return earliestOK;
    }	//	getDateFrom
    
    /**	Logger	*/
    private static CLogger s_log = CLogger.getCLogger(MFactAccumulation.class);
    
    
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param Fact_Accumulation_ID id
	 *	@param trx p_trx
	 */
	public MFactAccumulation(Ctx ctx, int Fact_Accumulation_ID, Trx trx)
	{
		super(ctx, Fact_Accumulation_ID, trx);
	}	//	MFactAccumulation

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx p_trx
	 */
	public MFactAccumulation(Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MFactAccumulation

	/**
	 * 	Validate the date to set
	 */
	@Override
	public void setDateTo(Timestamp DateTo)
	{
		if (BALANCEACCUMULATION_CalendarMonth.equals(getBalanceAccumulation()))
		{
			DateTo = TimeUtil.truncLast(DateTo, TimeUtil.TRUNC_MONTH);
		}
		else if (BALANCEACCUMULATION_CalendarWeek.equals(getBalanceAccumulation()))
		{
			DateTo = TimeUtil.truncLast(DateTo, TimeUtil.TRUNC_WEEK);
		}
		else if (BALANCEACCUMULATION_PeriodOfACompiereCalendar.equals(getBalanceAccumulation())
			&& getC_Calendar_ID() != 0)
		{
			
		}
	    super.setDateTo(DateTo);
	}	//	setDateTo
	
	/**
	 * 	Get the first Date From date based on Accumulation
	 *	@param from date
	 *	@return  first date
	 */
	public Timestamp getDateFrom (Timestamp from)
	{
		if (from == null)
			return from;
		
		if (BALANCEACCUMULATION_CalendarMonth.equals(getBalanceAccumulation()))
		{
			return TimeUtil.trunc(from, TimeUtil.TRUNC_MONTH);
		}
		else if (BALANCEACCUMULATION_CalendarWeek.equals(getBalanceAccumulation()))
		{
			return TimeUtil.truncLast(from, TimeUtil.TRUNC_WEEK);
		}
		else if (BALANCEACCUMULATION_PeriodOfACompiereCalendar.equals(getBalanceAccumulation())
			&& getC_Calendar_ID() != 0)
		{
			
		}
		return from;
	}	//	getDateFrom
	
	/**
	 * 	Get the first Date From date of the next period based on Accumulation
	 *	@param from date
	 *	@return  first date of next period
	 */
	public Timestamp getDateFromNext (Timestamp from)
	{
		if (from == null)
			return from;
		
		Timestamp retValue = from;
		if (BALANCEACCUMULATION_CalendarMonth.equals(getBalanceAccumulation()))
		{
			retValue = TimeUtil.addMonths(from, 1);
			retValue = TimeUtil.trunc(retValue, TimeUtil.TRUNC_MONTH);
		}
		else if (BALANCEACCUMULATION_CalendarWeek.equals(getBalanceAccumulation()))
		{
			retValue = TimeUtil.addDays(from, 7);
			retValue = TimeUtil.trunc(retValue, TimeUtil.TRUNC_WEEK);
		}
		else if (BALANCEACCUMULATION_PeriodOfACompiereCalendar.equals(getBalanceAccumulation())
			&& getC_Calendar_ID() != 0)
		{
			
		}
		//
		if (retValue.after(getDateTo()))
			return null;
		return retValue;
	}	//	getDateFromNext

	/**
	 * 	Before Save - Check Calendar
	 * 	@param newRecord new
	 * 	@return true if it can be saved
	 */
	@Override
	protected boolean beforeSave(boolean newRecord)
	{
		//	Calendar
		if (BALANCEACCUMULATION_PeriodOfACompiereCalendar.equals(getBalanceAccumulation()))
		{
			if (getC_Calendar_ID() == 0)
			{
				log.saveError("FillMandatory", Msg.getElement(getCtx(), "C_Calendar_ID"));
				return false;
			}
		}
		else if (getC_Calendar_ID() != 0)
			setC_Calendar_ID(0);
		
		//	Check Date
		if (newRecord || is_ValueChanged("DateTo") || is_ValueChanged("BalanceAccumulation"))
			setDateTo(getDateTo());
		
	    return true;
	}	//	beforeSave
	
	/**
     * 	String Representation
     *	@return info
     */
    @Override
	public String toString()
    {
	    StringBuffer sb = new StringBuffer("MFactAccumulation[")
	    	.append(get_ID())
	    	.append("-").append(getDateTo())
	    	.append(",BalanceAccumulation=").append(getBalanceAccumulation());
	    if (getC_Calendar_ID() != 0)
	    	sb.append(",C_Calendar_ID=").append(getC_Calendar_ID());
	    sb.append("]");
	    return sb.toString();
    } //	toString
	
}	//	MFactAccumulation
