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

import java.awt.*;
import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *	Assignment Slot.
 *  Display Information about Assignment Slot
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: MAssignmentSlot.java,v 1.2 2006/07/30 00:51:03 jjanke Exp $
 */
public class MAssignmentSlot implements Comparable<MAssignmentSlot>
{
	/**
	 *	Comparator Constructor
	 */
	public MAssignmentSlot ()
	{
		this (null, null, null, null, STATUS_TimeSlot);
	}	//	MAssignmentSlot

	/**
	 *	Timeslot Constructor
	 *  @param startTime start time
	 *  @param endTime end time
	 */
	public MAssignmentSlot (Timestamp startTime, Timestamp endTime)
	{
		this (startTime, endTime, null, null, STATUS_TimeSlot);
		setDisplay(DISPLAY_TIME_FROM);
	}	//	MAssignmentSlot

	/**
	 *	Timeslot Constructor
	 *  @param startTime start time
	 *  @param endTime end time
	 */
	public MAssignmentSlot (long startTime, long endTime)
	{
		this (new Timestamp(startTime), new Timestamp(endTime), null, null, STATUS_TimeSlot);
		setDisplay(DISPLAY_TIME_FROM);
	}	//	MAssignmentSlot

	/**
	 *	Non Assignment Constructor
	 *  @param startTime start time
	 *  @param endTime end time
	 *  @param name name
	 *  @param description description
	 *  @param status status
	 */
	public MAssignmentSlot (Timestamp startTime, Timestamp endTime,
		String name, String description, int status)
	{
		setStartTime(startTime);
		setEndTime(endTime);
		setName(name);
		setDescription(description);
		setStatus(status);
		//
	//	log.fine( toString());
	}	//	MAssignmentSlot

	/**
	 *	Assignment Constructor
	 *  @param assignment MAssignment
	 */
	public MAssignmentSlot (MResourceAssignment assignment)
	{
		setStatus(assignment.isConfirmed() ? STATUS_Confirmed : STATUS_NotConfirmed);
		setMAssignment(assignment);
	//	log.fine( toString());
	}	//	MAssignmentSlot


	/** Not Available Code				*/
	public static final int	STATUS_NotAvailable = 0;
	/** Not Available Code				*/
	public static final int	STATUS_UnAvailable = 11;
	/** Not Available Code				*/
	public static final int	STATUS_NonBusinessDay = 12;
	/** Not Available Code				*/
	public static final int	STATUS_NotInSlotDay = 21;
	/** Not Available Code				*/
	public static final int	STATUS_NotInSlotTime = 22;
	/** Assignment Code					*/
	public static final int	STATUS_NotConfirmed = 101;
	/** Assignment Code					*/
	public static final int	STATUS_Confirmed = 102;

	/** Assignment Code					*/
	public static final int	STATUS_TimeSlot = 100000;

	/**	Start Time						*/
	private Timestamp 		m_startTime;
	/**	End Time						*/
	private Timestamp 		m_endTime;
	/** Name							*/
	private String 			m_name;
	/** Description						*/
	private String 			m_description;
	/**	Status							*/
	private int 			m_status = STATUS_NotAvailable;
	/** Y position						*/
	private int				m_yStart = 0;
	private int				m_yEnd = 0;
	private int				m_xPos = 0;
	private int				m_xMax = 1;

	/**	The assignment					*/
	private MResourceAssignment 	m_mAssignment;

	/**	Language used for formatting			*/
	private Language 		m_language = Language.getLoginLanguage();

	/** toString displays everything			*/
	public static final int	DISPLAY_ALL = 0;

	/** toString displays formatted time from	*/
	public static final int	DISPLAY_TIME_FROM = 1;
	/** toString displays formatted time from-to		*/
	public static final int	DISPLAY_TIME_FROM_TO = 1;
	/** toString displays formatted day time from-to	*/
	public static final int	DISPLAY_DATETIME_FROM_TO = 1;
	/** toString displays name					*/
	public static final int	DISPLAY_NAME = 1;
	/** toString displays name and optional description	*/
	public static final int	DISPLAY_NAME_DESCRIPTION = 1;
	/** toString displays formatted all info	*/
	public static final int	DISPLAY_FULL = 1;

	/**	DisplayMode								*/
	private int				m_displayMode = DISPLAY_FULL;

	/*************************************************************************/

	/**
	 * 	Set Status
	 * 	@param status STATUS_..
	 */
	public void setStatus (int status)
	{
		m_status = status;
	}	//	setStatus

	/**
	 * 	Get Status
	 * 	@return STATUS_..
	 */
	public int getStatus()
	{
		return m_status;
	}	//	getStatus

	/**
	 * 	Is the Slot an Assignment?
	 * 	@return true if slot is an assignment
	 */
	public boolean isAssignment()
	{
		return (m_status == STATUS_NotConfirmed || m_status == STATUS_Confirmed);
	}	//	isAssignment

	/**
	 * 	Get Color for Status
	 *  @param background true if background - or foreground
	 * 	@return Color
	 */
	public Color getColor (boolean background)
	{
		//	Not found, Inactive, not available
		if (m_status == STATUS_NotAvailable)
			return background ? Color.gray : Color.magenta;

		//	Holiday
		else if (m_status == STATUS_UnAvailable)
			return background ? Color.gray : Color.pink;

		//	Vacation
		else if (m_status == STATUS_NonBusinessDay)
			return background ? Color.lightGray : Color.red;

		//	Out of normal hours
		else if (m_status == STATUS_NotInSlotDay || m_status == STATUS_NotInSlotTime)
			return background ? Color.lightGray : Color.black;

		//	Assigned
		else if (m_status == STATUS_NotConfirmed)
			return background ? Color.blue  : Color.white;

		//	Confirmed
		else if (m_status == STATUS_Confirmed)
			return background ? Color.blue : Color.black;

		//	Unknown
		return background ? Color.black : Color.white;
	}	//	getColor

	/*************************************************************************/

	/**
	 * 	Get Start time
	 * 	@return start time
	 */
	public Timestamp getStartTime()
	{
		return m_startTime;
	}

	/**
	 * 	Set Start time
	 *  @param startTime start time, if null use current time
	 */
	public void setStartTime (Timestamp startTime)
	{
		if (startTime == null)
			m_startTime = new Timestamp(System.currentTimeMillis());
		else
			m_startTime = startTime;
	}	//	setStartTime

	/**
	 * 	Get End time
	 * 	@return end time
	 */
	public Timestamp getEndTime()
	{
		return m_endTime;
	}

	/**
	 *  Set End time
	 *  @param endTime end time, if null use start time
	 */
	public void setEndTime (Timestamp endTime)
	{
		if (endTime == null)
			m_endTime = m_startTime;
		else
			m_endTime = endTime;
	}

	/*************************************************************************/

	/**
	 * 	Set Assignment
	 * 	@param assignment MAssignment
	 */
	public void setMAssignment (MResourceAssignment assignment)
	{
		if (assignment == null)
			return;
		if (!isAssignment())
			throw new IllegalArgumentException("Assignment Slot not an Assignment");
		//
		m_mAssignment = assignment;
		setStartTime(m_mAssignment.getAssignDateFrom());
		setEndTime(m_mAssignment.getAssignDateTo());
		setName(m_mAssignment.getName());
		setDescription(m_mAssignment.getDescription());
		setStatus(m_mAssignment.isConfirmed() ? STATUS_Confirmed : STATUS_NotConfirmed);
	}	//	setMAssignment

	/**
	 * 	Get Assugnment
	 * 	@return assignment
	 */
	public MResourceAssignment getMAssignment()
	{
		return m_mAssignment;
	}	//	getAssignment

	/**
	 *  Set Name
	 * 	@param name name
	 */
	public void setName (String name)
	{
		if (name == null)
			m_name = "";
		else
			m_name = name;
	}	//	setName

	/**
	 * 	Get Name
	 *  @return name
	 */
	public String getName()
	{
		return m_name;
	}	//	getName

	/**
	 * 	Set Description
	 * 	@param description description
	 */
	public void setDescription (String description)
	{
		if (description == null)
			m_description = "";
		else
			m_description = description;
	}	//	setDescription

	/**
	 * 	Get Description
	 *  @return description
	 */
	public String getDescription()
	{
		return m_description;
	}	//	getDescription

	/*************************************************************************/

	/**
	 * 	Set Y position
	 * 	@param yStart zero based Y start index
	 * 	@param yEnd zero based Y end index
	 */
	public void setY (int yStart, int yEnd)
	{
		m_yStart = yStart;
		m_yEnd = yEnd;
	}	//	setY

	/**
	 * 	Get Y start position
	 * 	@return zero based Y start index
	 */
	public int getYStart ()
	{
		return m_yStart;
	}	//	getYStart

	/**
	 * 	Get Y end position
	 * 	@return zero based Y end index
	 */
	public int getYEnd ()
	{
		return m_yEnd;
	}	//	setYEnd

	/**
	 * 	Set X position
	 * 	@param xPos zero based X position index
	 * 	@param xMax number of parallel columns
	 */
	public void setX (int xPos, int xMax)
	{
		m_xPos = xPos;
		if (xMax > m_xMax)
			m_xMax = xMax;
	}	//	setX

	/**
	 * 	Get X position
	 * 	@return zero based X position index
	 */
	public int getXPos()
	{
		return m_xPos;
	}	//	setXPos

	/**
	 * 	Get X columns
	 * 	@return number of parallel columns
	 */
	public int getXMax()
	{
		return m_xMax;
	}	//	setXMax

	/*************************************************************************/

	/**
	 * 	Set Language
	 * 	@param language language
	 */
	public void setLanguage (Language language)
	{
		m_language = language;
	}	//	setLanguage

	/**
	 * 	Set Display Mode of toString()
	 * 	@param displayMode DISPLAY_
	 */
	public void setDisplay (int displayMode)
	{
		m_displayMode = displayMode;
	}	//	setDisplay


	/**
	 * 	String representation
	 *  @return info
	 */
	@Override
	public String toString()
	{
		if (m_displayMode == DISPLAY_TIME_FROM)
			return getInfoTimeFrom();
		else if (m_displayMode == DISPLAY_TIME_FROM_TO)
			return getInfoTimeFromTo();
		else if (m_displayMode == DISPLAY_DATETIME_FROM_TO)
			return getInfoDateTimeFromTo();
		else if (m_displayMode == DISPLAY_NAME)
			return m_name;
		else if (m_displayMode == DISPLAY_NAME_DESCRIPTION)
			return getInfoNameDescription();
		else if (m_displayMode == DISPLAY_FULL)
			return getInfo();

		//	DISPLAY_ALL
		StringBuffer sb = new StringBuffer("MAssignmentSlot[");
		sb.append(m_startTime).append("-").append(m_endTime)
			.append("-Status=").append(m_status).append(",Name=")
			.append(m_name).append(",").append(m_description).append("]");
		return sb.toString();
	}	//	toString

	/**
	 * 	Get Info with Time From
	 *  @return info 00:00
	 */
	public String getInfoTimeFrom()
	{
		return m_language.getTimeFormat().format(m_startTime);
	}	//	getInfoTimeFrom

	/**
	 * 	Get Info with Time From-To
	 *  @return info 00:00 - 01:00
	 */
	public String getInfoTimeFromTo()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(m_language.getTimeFormat().format(m_startTime))
			.append(" - ")
			.append(m_language.getTimeFormat().format(m_endTime));
		return sb.toString();
	}	//	getInfoTimeFromTo

	/**
	 * 	Get Info with Date & Time From-To
	 *  @return info 12/12/01 00:00 - 01:00 or 12/12/01 00:00 - 12/13/01 01:00
	 */
	public String getInfoDateTimeFromTo()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(m_language.getDateTimeFormat().format(m_startTime))
			.append(" - ");
		if (TimeUtil.isSameDay(m_startTime, m_endTime))
			sb.append(m_language.getTimeFormat().format(m_endTime));
		else
			m_language.getDateTimeFormat().format(m_endTime);
		return sb.toString();
	}

	/**
	 * 	Get Info with Name and optional Description
	 * 	@return Name (Description)
	 */
	public String getInfoNameDescription()
	{
		StringBuffer sb = new StringBuffer(m_name);
		if (m_description.length() > 0)
			sb.append(" (").append(m_description).append(")");
		return sb.toString();
	}	//	getInfoNameDescription

	/**
	 *	Get Info with Date, Time From-To Name Description
	 * 	@return 12/12/01 00:00 - 01:00: Name (Description)
	 */
	public String getInfo()
	{
		StringBuffer sb = new StringBuffer(getInfoDateTimeFromTo());
		sb.append(": ").append(m_name);
		if (m_description.length() > 0)
			sb.append(" (").append(m_description).append(")");
		return sb.toString();
	}	//	getInfo

	/*************************************************************************
	 * 	Returns true if time is between start and end Time.
	 *  Date part is ignored.
	 *  <pre>
	 *  Example:
	 *  - Slots: 0:00-9:00 - 9:00-10:00 - 10:00-11:00 - ...
	 *  - inSlot (9:00, false) -> 1		//	start time
	 *  - inSlot (10:00, true) -> 1		//	end time
	 *  </pre>
	 * 	@param time time of the day
	 *  @param endTime if true, the end time is included
	 * 	@return true if within slot
	 */
	public boolean inSlot (Timestamp time, boolean endTime)
	{
		//	Compare	--
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(time);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		//	handle -00:00 (end time)
		if (endTime && cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0)
		{
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
		}
		Time compare = new Time (cal.getTimeInMillis());
		//	Start Time --
		cal.setTime(m_startTime);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		Time start = new Time (cal.getTimeInMillis());
		//	End time --
		cal.setTime(m_endTime);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0)
		{
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
		}
		Time end = new Time (cal.getTimeInMillis());

		//	before start			x |---|
		if (compare.before(start))
		{
		//	System.out.println("InSlot-false Compare=" + compare + " before start " + start);
			return false;
		}
		//	after end				|---| x
		if (compare.after(end))
		{
		//	System.out.println("InSlot-false Compare=" + compare + " after end " + end);
			return false;
		}

		//	start					x---|
		if (!endTime && compare.equals(start))
		{
		//	System.out.println("InSlot-true Compare=" + compare + " = Start=" + start);
			return true;
		}

		//
		//	end						|---x
		if (endTime && compare.equals(end))
		{
		//	System.out.println("InSlot-true Compare=" + compare + " = End=" + end);
			return true;
		}
		//	between start/end		|-x-|
		if (compare.before(end))
		{
		//	System.out.println("InSlot-true Compare=" + compare + " before end " + end);
			return true;
		}
		return false;
	}	//	inSlot

	/*************************************************************************
	 * Compares its two arguments for order.  Returns a negative integer,
	 * zero, or a positive integer as the first argument is less than, equal
	 * to, or greater than the second.
	 *
	 * @param obj the first object to be compared.
	 * @param o2 the second object to be compared.
	 * @return a negative integer, zero, or a positive integer as the
	 * 	       first argument is less than, equal to, or greater than the
	 *	       second.
	 */
	public int compareTo(MAssignmentSlot slot)
	{
		if (slot == null)
			return -1;

		//	Start Date
		int result = getStartTime().compareTo(slot.getStartTime());
		if (result != 0)
			return result;
		//	Status
		result = slot.getStatus() - getStatus();
		if (result != 0)
			return result;
		//	End Date
		result = getEndTime().compareTo(slot.getEndTime());
		if (result != 0)
			return result;
		//	Name
		result = getName().compareTo(slot.getName());
		if (result != 0)
			return result;
		//	Description
		return getDescription().compareTo(slot.getDescription());
	}	//	compare

	/**
	 * Indicates whether some other object is &quot;equal to&quot; this
	 * Comparator.
	 * @param   obj   the reference object with which to compare.
	 * @return  <code>true</code> only if the specified object is also
	 *		a comparator and it imposes the same ordering as this
	 *		comparator.
	 * @see     java.lang.Object#equals(java.lang.Object)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof MAssignmentSlot)
		{
			MAssignmentSlot cmp = (MAssignmentSlot)obj;
			if (m_startTime.equals(cmp.getStartTime())
				&& m_endTime.equals(cmp.getEndTime())
				&& m_status == cmp.getStatus()
				&& m_name.equals(cmp.getName())
				&& m_description.equals(cmp.getDescription()))
				return true;
		}
		return false;
	}	//	equals

	/**
	 * 	HashCode of MAssignmentSlot
	 * 	@return has code
	 */
	@Override
	public int hashCode()
	{
		return m_startTime.hashCode() + m_endTime.hashCode() + m_status
			+ m_name.hashCode() + m_description.hashCode();
	}	//	hashCode

}	//	MAssignmentSlot
