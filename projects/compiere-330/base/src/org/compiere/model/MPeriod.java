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

import org.compiere.common.constants.*;
import org.compiere.framework.*;
import org.compiere.util.*;

/**
 *  Calendar Period Model
 *
 *	@author Jorg Janke
 *	@version $Id: MPeriod.java,v 1.4 2006/07/30 00:51:05 jjanke Exp $
 */
public class MPeriod extends X_C_Period
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 	Get Period from Cache
	 *	@param ctx context
	 *	@param C_Period_ID id
	 *	@return MPeriod
	 */
	public static MPeriod get (Ctx ctx, int C_Period_ID)
	{
		Integer key = Integer.valueOf (C_Period_ID);
		MPeriod retValue = s_cache.get (ctx, key);
		if (retValue != null)
			return retValue;
		//
		retValue = new MPeriod (ctx, C_Period_ID, null);
		if (retValue.get_ID () != 0)
			s_cache.put (key, retValue);
		return retValue;
	} 	//	get

	/**
	 * 	Find standard Period of DateAcct based on Client Calendar
	 *	@param ctx context
	 *	@param DateAcct date
	 *	@return active Period or null
	 */
	public static MPeriod getOfOrg (Ctx ctx, int AD_Org_ID,  Timestamp DateAcct)
	{
		if (DateAcct == null)
			return null;
		int C_Calendar_ID = 0;
		if (AD_Org_ID != 0)
		{
			MOrgInfo info = MOrgInfo.get(ctx, AD_Org_ID, null);
			C_Calendar_ID = info.getC_Calendar_ID();
		}
		if (C_Calendar_ID == 0)
		{
			MClientInfo cInfo = MClientInfo.get(ctx);
			C_Calendar_ID = cInfo.getC_Calendar_ID();
		}

		//	Search in Cache first
		Iterator<MPeriod> it = s_cache.values().iterator();
		while (it.hasNext())
		{
			MPeriod period = it.next();
			if (period.getC_Calendar_ID() == C_Calendar_ID 
					&& period.isStandardPeriod() 
					&& period.isInPeriod(DateAcct))
				return period;
		}
		
		//	Get it from DB
		MPeriod retValue = null;
		String sql = "SELECT * "
			+ "FROM C_Period "
			+ "WHERE C_Year_ID IN "
				+ "(SELECT C_Year_ID FROM C_Year WHERE C_Calendar_ID=?) "
			+ " AND ? BETWEEN TRUNC(StartDate,'DD') AND TRUNC(EndDate,'DD')"
			+ " AND IsActive='Y' AND PeriodType='S'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, C_Calendar_ID);
			pstmt.setTimestamp (2, TimeUtil.getDay(DateAcct));
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MPeriod period = new MPeriod(ctx, rs, null);
				Integer key = Integer.valueOf (period.getC_Period_ID());
				s_cache.put (key, period);
				if (period.isStandardPeriod())
					retValue = period;
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, "DateAcct=" + DateAcct, e);
		}
		if (retValue == null)
			s_log.warning("No Standard Period for " + DateAcct 
				+ " (C_Calendar_ID=" + C_Calendar_ID + ")");
		return retValue;
	}	//	get

	/**
	 * 	Find standard Period of DateAcct based on Client Calendar
	 *	@param ctx context
	 *	@param C_Calendar_ID calendar
	 *	@param DateAcct date
	 *	@return active Period or null
	 */
	public static MPeriod getOfCalendar (Ctx ctx, int C_Calendar_ID, Timestamp DateAcct)
	{
		if (DateAcct == null)
		{
			s_log.warning("No DateAcct");
			return null;
		}
		if (C_Calendar_ID == 0)
		{
			s_log.warning("No Calendar");
			return null;
		}
		//	Search in Cache first
		Iterator<MPeriod> it = s_cache.values().iterator();
		while (it.hasNext())
		{
			MPeriod period = it.next();
			if (period.getC_Calendar_ID() == C_Calendar_ID 
				&& period.isStandardPeriod() 
				&& period.isInPeriod(DateAcct))
				return period;
		}
		
		//	Get it from DB
		MPeriod retValue = null;
		String sql = "SELECT * FROM C_Period "
			+ "WHERE C_Year_ID IN "
				+ "(SELECT C_Year_ID FROM C_Year WHERE C_Calendar_ID=?)"
			+ " AND ? BETWEEN TRUNC(StartDate,'DD') AND TRUNC(EndDate,'DD')"
			+ " AND IsActive='Y' AND PeriodType='S'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, C_Calendar_ID);
			pstmt.setTimestamp (2, TimeUtil.getDay(DateAcct));
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MPeriod period = new MPeriod(ctx, rs, null);
				Integer key = Integer.valueOf (period.getC_Period_ID());
				s_cache.put (key, period);
				if (period.isStandardPeriod())
					retValue = period;
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, "DateAcct=" + DateAcct, e);
		}
		if (retValue == null)
			s_log.warning("No Standard Period for " + DateAcct 
				+ " (C_Calendar_ID=" + C_Calendar_ID + ")");
		return retValue;
	}	//	get

	
	/**
	 * 	Find valid standard Period of DateAcct based on Client Calendar
	 *	@param ctx context
	 *	@param DateAcct date
	 *	@return C_Period_ID or 0
	 */
	public static int getC_Period_ID (Ctx ctx, int AD_Org_ID, Timestamp DateAcct)
	{
		MPeriod period = getOfOrg(ctx, AD_Org_ID, DateAcct);
		if (period == null)
			return 0;
		return period.getC_Period_ID();
	}	//	getC_Period_ID

	/**
	 * 	Is standard Period Open for Document Base Type - does not check Orgs
	 *	@param ctx context
	 *	@param DateAcct date
	 *	@param DocBaseType base type
	 *	@return true if open
	 *	@deprecated use new isOpen
	 */
	@Deprecated
	public static boolean isOpenOld (Ctx ctx, Timestamp DateAcct, String DocBaseType)
	{
		if (DateAcct == null)
		{
			s_log.warning("No DateAcct");
			return false;
		}
		if (DocBaseType == null)
		{
			s_log.warning("No DocBaseType");
			return false;
		}
		MPeriod period = MPeriod.getOfOrg(ctx, 0, DateAcct);
		if (period == null)
		{
			s_log.warning("No Period for " + DateAcct + " (" + DocBaseType + ")");
			return false;
		}
		String error = period.isOpen(DocBaseType, DateAcct);
		if (error != null)
			s_log.warning(error + " - " + period.getName());
		return error == null;
	}	//	isOpen
	
	/**
	 * 	Is standard Period Open for Document Base Type
	 *	@param header header document record
	 *	@param lines document lines optional
	 *	@param DateAcct accounting date
	 *	@param DocBaseType document base type
	 *	@return error message or null
	 */
	public static String isOpen (PO header, PO[] lines, Timestamp DateAcct, String DocBaseType)
	{
		Ctx ctx = header.getCtx();
		if (DateAcct == null)
			return "@NotFound@ @DateAcct@";
		if (DocBaseType == null)
			return "@NotFound@ @DocBaseType@";
		
		MAcctSchema as = MClient.get(header.getCtx(), 
			header.getAD_Client_ID()).getAcctSchema();
		if (as == null)
			return "@NotFound@ @C_AcctSchema_ID@ for AD_Client_ID=" + header.getAD_Client_ID();
		if (as.isAutoPeriodControl())
		{
			if (as.isAutoPeriodControlOpen(DateAcct))
				return null;
			else
				return "@PeriodClosed@ - @AutoPeriodControl@";
		}
		
		//	Get All Orgs
		ArrayList<Integer> orgs = new ArrayList<Integer>();
		orgs.add(header.getAD_Org_ID());
		if (lines != null)
		{
			for (PO element : lines) {
				int AD_Org_ID = element.getAD_Org_ID();
				if (!orgs.contains(AD_Org_ID))
					orgs.add(AD_Org_ID);
			}
		}
		//	Get all Calendars in line with Organizations
		MClientInfo cInfo = MClientInfo.get(ctx, header.getAD_Client_ID(), null);
		ArrayList<Integer> orgCalendars = new ArrayList<Integer>();
		ArrayList<Integer> calendars = new ArrayList<Integer>();
		for (int i = 0; i < orgs.size(); i++)
		{
			MOrgInfo info = MOrgInfo.get(ctx, orgs.get(i), null);
			int C_Calendar_ID = info.getC_Calendar_ID();
			if (C_Calendar_ID == 0)
				C_Calendar_ID = cInfo.getC_Calendar_ID();
			orgCalendars.add(C_Calendar_ID);
			if (!calendars.contains(C_Calendar_ID))
				calendars.add(C_Calendar_ID);
		}
		//	Should not happen
		if (calendars.size() == 0)
			return "@NotFound@ @C_Calendar_ID@";
		
		//	For all Calendars get Periods
		for (int i = 0; i < calendars.size(); i++)
		{
			int C_Calendar_ID = calendars.get(i);
			MPeriod period = MPeriod.getOfCalendar (ctx, C_Calendar_ID, DateAcct);
			//	First Org for Calendar
			int AD_Org_ID = 0;
			for (int j = 0; j < orgCalendars.size(); j++)
			{
				if (orgCalendars.get(j) == C_Calendar_ID)
				{
					AD_Org_ID = orgs.get(j);
					break;
				}
			}
			if (period == null)
			{
				MCalendar cal = MCalendar.get(ctx, C_Calendar_ID);
				String date = DisplayType.getDateFormat(DisplayTypeConstants.Date)
					.format(DateAcct);
				if (cal != null)
					return "@NotFound@ @C_Period_ID@: " + date
						+ " - " + MOrg.get(ctx, AD_Org_ID).getName() 
						+ " -> " + cal.getName();
				else
					return "@NotFound@ @C_Period_ID@: " + date
						+ " - " + MOrg.get(ctx, AD_Org_ID).getName() 
						+ " -> C_Calendar_ID=" + C_Calendar_ID;
			}
			String error = period.isOpen(DocBaseType, DateAcct);
			if (error != null)
				return error
					+ " - " + MOrg.get(ctx, AD_Org_ID).getName()
					+ " -> " + MCalendar.get(ctx, C_Calendar_ID).getName();
		}
		return null;	//	open
	}	//	isOpen

	/**
	 * 	Is standard Period closed for all Document Base Types
	 *	@param ctx context for AD_Client
	 *	@param DateAcct accounting date
	 *	@return true if closed
	 */
	public static boolean isClosed (Ctx ctx, Timestamp DateAcct)
	{
		if (DateAcct == null)
			return false;
		MAcctSchema as = MClient.get(ctx, ctx.getAD_Client_ID())
			.getAcctSchema();
		if (as.isAutoPeriodControl())
			return !as.isAutoPeriodControlOpen(DateAcct);
		
		//	Get all Calendars in line with Organizations
		MClientInfo cInfo = MClientInfo.get(ctx, ctx.getAD_Client_ID(), null);
		ArrayList<Integer> calendars = new ArrayList<Integer>();
		MOrg[] orgs = MOrg.getOfClient(cInfo);
		for (int i = 0; i < orgs.length; i++)
		{
			MOrgInfo info = MOrgInfo.get(ctx, orgs[i].getAD_Org_ID(), null);
			int C_Calendar_ID = info.getC_Calendar_ID();
			if (C_Calendar_ID == 0)
				C_Calendar_ID = cInfo.getC_Calendar_ID();
			if (!calendars.contains(C_Calendar_ID))
				calendars.add(C_Calendar_ID);
		}
		//	Should not happen
		if (calendars.size() == 0)
			throw new IllegalArgumentException("@NotFound@ @C_Calendar_ID@");
		
		//	For all Calendars get Periods
		for (int i = 0; i < calendars.size(); i++)
		{
			int C_Calendar_ID = calendars.get(i);
			MPeriod period = MPeriod.getOfCalendar (ctx, C_Calendar_ID, DateAcct);
			//	Period not found
			if (period == null)
				return false;
			if (!period.isClosed())
				return false;
		}
		return true;	//	closed
	}	//	isClosed

	
	/**
	 * 	Find first Year Period of DateAcct based on Client Calendar
	 *	@param ctx context
	 *	@param C_Calendar_ID calendar
	 *	@param DateAcct date
	 *	@return active first Period
	 */
	public static MPeriod getFirstInYear (Ctx ctx, int C_Calendar_ID, Timestamp DateAcct)
	{
		MPeriod retValue = null;
		String sql = "SELECT * "
			+ "FROM C_Period "
			+ "WHERE C_Year_ID IN "
				+ "(SELECT p.C_Year_ID "
				+ "FROM C_Year y"
				+ " INNER JOIN C_Period p ON (y.C_Year_ID=p.C_Year_ID) "
				+ "WHERE y.C_Calendar_ID=?"
				+ "	AND ? BETWEEN StartDate AND EndDate)"
			+ " AND IsActive='Y' AND PeriodType='S' "
			+ "ORDER BY StartDate";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt (1, C_Calendar_ID);
			pstmt.setTimestamp (2, DateAcct);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())	//	first only
				retValue = new MPeriod(ctx, rs, null);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		return retValue;
	}	//	getFirstInYear

	/**	Cache							*/
	private static final CCache<Integer,MPeriod> s_cache = new CCache<Integer,MPeriod>("C_Period", 10);
	
	/**	Logger							*/
	private static final CLogger		s_log = CLogger.getCLogger (MPeriod.class);
	
	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param C_Period_ID id
	 *	@param trx transaction
	 */
	public MPeriod (Ctx ctx, int C_Period_ID, Trx trx)
	{
		super (ctx, C_Period_ID, trx);
		if (C_Period_ID == 0)
		{
		//	setC_Period_ID (0);		//	PK
		//  setC_Year_ID (0);		//	Parent
		//  setName (null);
		//  setPeriodNo (0);
		//  setStartDate (new Timestamp(System.currentTimeMillis()));
			setPeriodType (PERIODTYPE_StandardCalendarPeriod);
		}
	}	//	MPeriod

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MPeriod (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MPeriod

	/**
	 * 	Parent constructor
	 *	@param year year
	 *	@param PeriodNo no
	 *	@param name name
	 *	@param startDate start
	 *	@param endDate end
	 */
	public MPeriod (MYear year, int PeriodNo, String name, 
		Timestamp startDate,Timestamp endDate)
	{
		this (year.getCtx(), 0, year.get_Trx());
		setClientOrg(year);
		setC_Year_ID(year.getC_Year_ID());
		setPeriodNo(PeriodNo);
		setName(name);
		setStartDate(startDate);
		setEndDate(endDate);
	}	//	MPeriod
	
	
	/**	Period Controls			*/
	private MPeriodControl[]	m_controls = null;
	/** Calendar				*/
	private int					m_C_Calendar_ID = 0;
		
	/**
	 * 	Get Period Control
	 *	@param requery requery
	 *	@return period controls
	 */
	public MPeriodControl[] getPeriodControls (boolean requery)
	{
		if (m_controls != null && !requery)
			return m_controls;
		//
		ArrayList<MPeriodControl> list = new ArrayList<MPeriodControl>();
		String sql = "SELECT * FROM C_PeriodControl "
			+ "WHERE C_Period_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt(1, getC_Period_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add (new MPeriodControl (getCtx(), rs, null));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
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
		//
		m_controls = new MPeriodControl[list.size ()];
		list.toArray (m_controls);
		return m_controls;
	}	//	getPeriodControls
	
	/**
	 * 	Get Period Control
	 *	@param DocBaseType Document Base Type
	 *	@return period control or null
	 */
	public MPeriodControl getPeriodControl (String DocBaseType)
	{
		if (DocBaseType == null)
			return null;
		getPeriodControls(false);
		for (MPeriodControl element : m_controls) {
		//	log.fine("getPeriodControl - " + 1 + " - " + m_controls[i]);
			if (DocBaseType.equals(element.getDocBaseType()))
				return element;
		}
		return null;
	}	//	getPeriodControl

	/**
	 * 	Date In Period
	 *	@param date date
	 *	@return true if in period
	 */
	public boolean isInPeriod (Timestamp date)
	{
		if (date == null)
			return false;
		Timestamp dateOnly = TimeUtil.getDay(date);
		Timestamp from = TimeUtil.getDay(getStartDate());
		if (dateOnly.before(from))
			return false;
		Timestamp to = TimeUtil.getDay(getEndDate());
		if (dateOnly.after(to))
			return false;
		return true;
	}	//	isInPeriod
	
	/**
	 * 	Is Period Open for Doc Base Type
	 *	@param DocBaseType document base type
	 *	@param dateAcct accounting date
	 *	@return error message or null
	 */
	public String isOpen (String DocBaseType, Timestamp dateAcct)
	{
		if (!isActive())
		{
			s_log.warning("Period not active: " + getName());
			return "@C_Period_ID@ <> @IsActive@";
		}

		MAcctSchema as = MClient.get(getCtx(), getAD_Client_ID()).getAcctSchema();
		if (as != null && as.isAutoPeriodControl())
		{
			if (!as.isAutoPeriodControlOpen(dateAcct))
				return "@PeriodClosed@ - @AutoPeriodControl@";
			//	We are OK
			Timestamp today = new Timestamp (System.currentTimeMillis());
			if (isInPeriod(today) && as.getC_Period_ID() != getC_Period_ID())
			{
				as.setC_Period_ID(getC_Period_ID());
				as.save();
			}
			return null;
		}
		
		//	Standard Period Control
		if (DocBaseType == null)
		{
			log.warning(getName() + " - No DocBaseType");
			return "@NotFound@ @DocBaseType@";
		}
		MPeriodControl pc = getPeriodControl (DocBaseType);
		if (pc == null)
		{
			log.warning(getName() + " - Period Control not found for " + DocBaseType);
			return "@NotFound@ @C_PeriodControl_ID@: " + DocBaseType;
		}
		log.fine(getName() + ": " + DocBaseType);
		if (pc.isOpen())
			return null;
		return "@PeriodClosed@ - @C_PeriodControl_ID@ (" 
			+ DocBaseType + ", " + dateAcct + ")";
	}	//	isOpen

	/**
	 * 	Return true if all PC are closed
	 *	@return true if closed
	 */
	public boolean isClosed()
	{
		MPeriodControl[] pcs = getPeriodControls(false);
		for (MPeriodControl pc : pcs)
        {
	        if (!pc.isClosed())
	        	return false;
        }
		return true;
	}	//	isClosed
	
	/**
	 * 	Standard Period
	 *	@return true if standard calendar period
	 */
	public boolean isStandardPeriod()
	{
		return PERIODTYPE_StandardCalendarPeriod.equals(getPeriodType());
	}	//	isStandardPeriod
	
	/**
	 * 	Get Calendar of Period
	 *	@return calendar
	 */
	public int getC_Calendar_ID()
	{
		if (m_C_Calendar_ID == 0)
		{
			MYear year = MYear.get(getCtx(), getC_Year_ID());
			if (year != null)
				m_C_Calendar_ID = year.getC_Calendar_ID();
			else
				log.severe("@NotFound@ C_Year_ID=" + getC_Year_ID());
		}
		return m_C_Calendar_ID;
	}	//	getC_Calendar_ID
	
	/**
	 * 	Before Save.
	 * 	Truncate Dates
	 *	@param newRecord new
	 *	@return true
	 */
	@Override
	protected boolean beforeSave (boolean newRecord)
	{
		//	Truncate Dates
		Timestamp date = getStartDate(); 
		if (date != null)
			setStartDate(TimeUtil.getDay(date));
		else
			return false;
		//
		date = getEndDate();
		if (date != null)
			setEndDate(TimeUtil.getDay(date));
		else
			setEndDate(TimeUtil.getMonthLastDay(getStartDate()));
		return true;
	}	//	beforeSave
	
	/**
	 * 	After Save
	 *	@param newRecord new
	 *	@param success success
	 *	@return success
	 */
	@Override
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		if (newRecord)
		{
		//	SELECT Value FROM AD_Ref_List WHERE AD_Reference_ID=183
			MDocType[] types = MDocType.getOfClient(getCtx());
			int count = 0;
			ArrayList<String> baseTypes = new ArrayList<String>();
			for (MDocType type : types) {
				String DocBaseType = type.getDocBaseType();
				if (baseTypes.contains(DocBaseType))
					continue;
				MPeriodControl pc = new MPeriodControl(this, DocBaseType);
				if (pc.save())
					count++;
				baseTypes.add (DocBaseType);
			}
			log.fine("PeriodControl #" + count);
		}
		return success;
	}	//	afterSave
	
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	@Override
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MPeriod[");
		sb.append (get_ID())
			.append("-").append (getName())
			.append(", ").append(getStartDate()).append("-").append(getEndDate())
			.append ("]");
		return sb.toString ();
	}	//	toString
	
}	//	MPeriod
