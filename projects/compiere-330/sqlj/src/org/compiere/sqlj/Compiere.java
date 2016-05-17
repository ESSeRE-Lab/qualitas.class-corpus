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
package org.compiere.sqlj;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;


/**
 *	SQLJ Compiere Control and Utility Class
 *	
 *  @author Jorg Janke
 *  @version $Id$
 */
public class Compiere implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Get Version
	 *	@return version
	 */
	public static String getVersion()
	{
		return "Compiere SQLJ $Id$";
	}	//	version
	
	/**
	 * 	Get Environment Info
	 *	@return properties
	 */
	public static String getProperties()
	{
		StringBuffer sb = new StringBuffer();
		Enumeration en = System.getProperties().keys();
		while (en.hasMoreElements())
		{
			if (sb.length() != 0)
				sb.append(" - ");
			String key = (String)en.nextElement();
			String value = System.getProperty(key);
			sb.append(key).append("=").append(value);
		}
		return sb.toString();
	}	//	environment

	/**
	 * 	Get Environment Info
	 * 	@param key key
	 *	@return property info
	 */
	public static String getProperty (String key)
	{
		if (key == null || key.length() == 0)
			return "null";
		return System.getProperty(key, "NotFound");
	}	//	environment

	/** Oracle Server					*/
	public static final String TYPE_ORACLE = "oracle";
	/** DB2 Server						*/
	public static final String TYPE_DB2 = "db2";
	/** Server Type						*/
	public static String 	s_type = "x";
	
	/**
	 * 	Get Server Type
	 *	@return server type
	 */
	public static String getServerType()
	{
		if (s_type == null)
		{
			String vendor = System.getProperty("java.vendor");
			if (vendor.startsWith("Oracle"))
				s_type = TYPE_ORACLE;
			else
				s_type = "??";
		}
		return s_type;
	}	//	getServerType
	
	/**
	 * 	Is this Oracle ?
	 *	@return true if Oracle
	 */
	static boolean isOracle()
	{
		if (s_type == null)
			getServerType();
		if (s_type != null)
			return TYPE_ORACLE.equals(s_type);
		return false;
	}	//	isOracle
	
	
	/**
	 * 	Get Connection URL
	 *	@return connection URL
	 */
	static String getConnectionURL()
	{
		if (s_url != null)
			return s_url;
		
		s_url = "jdbc:default:connection:";
		//
		return s_url;
	}	//	getConnectionURL
	
	/**	Connection URL				*/
	protected static String		s_url = null;
	/**	Connection User				*/
	protected static String		s_uid = null;
	/**	Connection Password			*/
	protected static String		s_pwd = null;
	
	/**
	 * 	Get Server side Connection
	 *	@return connection
	 *	@throws SQLException
	 */
	private static Connection getConnection() throws SQLException
	{
		if (s_uid != null && s_pwd != null)
			return DriverManager.getConnection(getConnectionURL(), s_uid, s_pwd);
		return DriverManager.getConnection(getConnectionURL());
	}	//	getConnection

	/**
	 * 	Prepare Statement (Forward, ReadOnly)
	 *	@param sql sql
	 *	@return prepared statement
	 *	@throws SQLException
	 */
	static PreparedStatement prepareStatement (String sql) throws SQLException
	{
		return prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	prepareStatement
	
	/**
	 * 	Prepare Statement
	 *	@param sql sql
	 *	@param resultSetType result set type
	 *	@param resultSetCurrency result type currency
	 *	@return prepared statement
	 *	@throws SQLException
	 */
	static PreparedStatement prepareStatement (String sql, int resultSetType, int resultSetCurrency)
		throws SQLException
	{
		synchronized (ONE) //jz although diff VM with setConnection
		{

			if (s_conn == null)
				s_conn = getConnection();
		}
		try
		{
			PreparedStatement ps = s_conn.prepareStatement(sql, resultSetType, resultSetCurrency);
			return ps;
		}
		catch (Exception e)	//	connection not good anymore
		{
		}
		
		//	get new Connection
		synchronized (ONE)
		{
			s_conn = getConnection();
		}
		return s_conn.prepareStatement(sql, resultSetType, resultSetCurrency);
	}	//	

	/**
	 * 	Get SQL int Value with param 
	 *	@param sql sql command
	 *	@param param1 parameter
	 *	@return value or -1 if not found
	 *	@throws SQLException
	 */
	static int getSQLValue (String sql, int param1) throws SQLException
	{
		int retValue = -1;
		PreparedStatement pstmt = prepareStatement(sql);
		pstmt.setInt(1, param1);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
			retValue = rs.getInt(1);
		rs.close();
		pstmt.close();
		return retValue;
	}	//	getSQLValue
	
	/** Permanently open Connection		*/
	private static Connection s_conn = null;
	
	/** Zero 0				*/
	public static final BigDecimal ZERO = new BigDecimal(0.0);
	/** One 1				*/
	public static final BigDecimal ONE = new BigDecimal(1.0);
	/** Hundred 100				*/
	public static final BigDecimal HUNDRED = new BigDecimal(100.0);
	

	/** Truncate Day - D			*/
	public static final String	TRUNC_DAY = "DD";
	/** Truncate Week - W			*/
	public static final String	TRUNC_WEEK = "DY";
	/** Truncate Month - MM			*/
	public static final String	TRUNC_MONTH = "MM";
	/** Truncate Quarter - Q		*/
	public static final String	TRUNC_QUARTER = "Q";
	/** Truncate Year - Y			*/
	public static final String	TRUNC_YEAR = "Y";
	
	/**
	 * 	Get truncated day/time
	 *  @param dayTime day
	 *  @param trunc how to truncate TRUNC_*
	 *  @return next day with 00:00
	 */
	static public Timestamp trunc (Timestamp dayTime, String trunc)
	{
		if (dayTime == null)
			dayTime = new Timestamp(System.currentTimeMillis());
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(dayTime.getTime());
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		//	D
		cal.set(Calendar.HOUR_OF_DAY, 0);
		if (trunc == null || trunc.equals(TRUNC_DAY))
			return new Timestamp (cal.getTimeInMillis());
		//	W
		if (trunc.equals(TRUNC_WEEK))
		{
			cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
			return new Timestamp (cal.getTimeInMillis());
		}
		// MM
		cal.set(Calendar.DAY_OF_MONTH, 1);
		if (trunc.equals(TRUNC_MONTH))
			return new Timestamp (cal.getTimeInMillis());
		//	Q
		if (trunc.equals(TRUNC_QUARTER))
		{
			int mm = cal.get(Calendar.MONTH);
			if (mm < 4)
				mm = 1;
			else if (mm < 7)
				mm = 4;
			else if (mm < 10)
				mm = 7;
			else
				mm = 10;
			cal.set(Calendar.MONTH, mm);
			return new Timestamp (cal.getTimeInMillis());
		}
		cal.set(Calendar.DAY_OF_YEAR, 1);
		return new Timestamp (cal.getTimeInMillis());
	}	//	trunc
	
	/**
	 * 	Truncate Date
	 *	@param t time
	 *	@return day
	 */
	protected static Timestamp trunc (Timestamp t)
	{
		return trunc(t, TRUNC_DAY);
	}	//	trunc
	
	/**
	 * 	Fist of Date
	 *	@param p_dateTime date
	 *	@param XX date part - Supported: DD(default),DY,MM,Q 
	 *	@return day (first)
	 */
	public static Timestamp firstOf (Timestamp p_dateTime, String XX)
	{
		Timestamp time = p_dateTime;
		if (time == null)
			time = new Timestamp(System.currentTimeMillis());
		//
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(time);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		//
		if (TRUNC_MONTH.equals(XX))					//	Month
			cal.set(Calendar.DAY_OF_MONTH, 1);
		else if (TRUNC_WEEK.equals(XX))				//	Week
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		else if (TRUNC_QUARTER.equals(XX))			//	Quarter
		{
			cal.set(Calendar.DAY_OF_MONTH, 1);
			int mm = cal.get(Calendar.MONTH);	//	January = 0
			if (mm < Calendar.APRIL)
				cal.set(Calendar.MONTH, Calendar.JANUARY);
			else if (mm < Calendar.JULY)
				cal.set(Calendar.MONTH, Calendar.APRIL);
			else if (mm < Calendar.OCTOBER)
				cal.set(Calendar.MONTH, Calendar.JULY);
			else
				cal.set(Calendar.MONTH, Calendar.OCTOBER);
		}
		//
		java.util.Date temp = cal.getTime();
		return new Timestamp (temp.getTime());
	}	//	trunc
	
	/**
	 * 	Calculate the number of days between start and end.
	 * 	@param start start date
	 * 	@param end end date
	 * 	@return number of days (0 = same)
	 */
	static public int getDaysBetween (Timestamp start, Timestamp end)
	{
		boolean negative = false;
		
		if (start == null && end == null)
			return 0;
		if (start == null)
			start = new Timestamp(new java.util.Date().getTime());
		if (end == null)
			end = new Timestamp(new java.util.Date().getTime());
		
		if (end.before(start))
		{
			negative = true;
			Timestamp temp = start;
			start = end;
			end = temp;
		}
		//
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(start);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		GregorianCalendar calEnd = new GregorianCalendar();
		calEnd.setTime(end);
		calEnd.set(Calendar.HOUR_OF_DAY, 0);
		calEnd.set(Calendar.MINUTE, 0);
		calEnd.set(Calendar.SECOND, 0);
		calEnd.set(Calendar.MILLISECOND, 0);

	//	System.out.println("Start=" + start + ", End=" + end + ", dayStart=" + cal.get(Calendar.DAY_OF_YEAR) + ", dayEnd=" + calEnd.get(Calendar.DAY_OF_YEAR));

		//	in same year
		if (cal.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR))
		{
			if (negative)
				return (calEnd.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR)) * -1;
			return calEnd.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR);
		}

		//	not very efficient, but correct
		int counter = 0;
		while (calEnd.after(cal))
		{
			cal.add (Calendar.DAY_OF_YEAR, 1);
			counter++;
		}
		if (negative)
			return counter * -1;
		return counter;
	}	//	getDaysBetween

	/**
	 * 	Return Day + offset (truncates)
	 * 	@param day Day
	 * 	@param offset day offset
	 * 	@return Day + offset at 00:00
	 */
	static public Timestamp addDays (Timestamp day, int offset)
	{
		if (day == null)
			day = new Timestamp(System.currentTimeMillis());
		//
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if (offset != 0)
			cal.add(Calendar.DAY_OF_YEAR, offset);	//	may have a problem with negative (before 1/1)
		//
		java.util.Date temp = cal.getTime();
		return new Timestamp (temp.getTime());
	}	//	addDays

	/**
	 * 	Next Business Day.
	 * 	(Only Sa/Su -> Mo)
	 *	@param day day
	 *	@return next business dat if day is "off"
	 */
	static public Timestamp nextBusinessDay (Timestamp day)
	{
		if (day == null)
			day = new Timestamp(System.currentTimeMillis());
		//
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		//
		int dow = cal.get(Calendar.DAY_OF_WEEK);
		if (dow == Calendar.SATURDAY)
			cal.add(Calendar.DAY_OF_YEAR, 2);
		else if (dow == Calendar.SUNDAY)
			cal.add(Calendar.DAY_OF_YEAR, 1);
		//
		java.util.Date temp = cal.getTime();
		return new Timestamp (temp.getTime());
	}	//	nextBusinessDay	
	
	
	/**
	 * 	Character At Position
	 *	@param source source
	 *	@param posIndex position 1 = first
	 *	@return substring or null
	 */
	public static String charAt (String source, int posIndex)
	{
		if (source == null || source.length() == 0 || posIndex < 0 || posIndex >= source.length())
			return null;
		try
		{
			return (source.substring(posIndex+1, posIndex+2));
		}
		catch (Exception e)
		{}
		return null;
	}	//	charAt
	
	/**
	 * 	Mext ID
	 *	@param AD_Sequence_ID sequence
	 *	@param System system
	 *	@return ID or -1
	 *	@throws SQLException
	 *
	public static int nextID (int AD_Sequence_ID, String System)
		throws SQLException
	{
		boolean isSystem = System != null && "Y".equals(System);
		int retValue = -1;
		StringBuffer sql = new StringBuffer ("SELECT CurrentNext");
		if (isSystem)
			sql.append("Sys");
		sql.append(",IncrementNo FROM AD_Sequence WHERE AD_Sequence_ID=? FOR UPDATE");
		PreparedStatement pstmt = prepareStatement(sql.toString(),
			ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
		{
			retValue = rs.getInt(1);
			int incrementNo = rs.getInt(2);
			//jz
			retValue += incrementNo;
			rs.updateInt(2, retValue);
			//pstmt.getConnection().commit();
		}
		rs.close();
		pstmt.close();
		//
		return retValue;
	}	//	nextID
	
	/*
	 *  ProcNextID is a procedure version of nextID
	 *  Since Derby could not tell diff of signatures, use ProcNextID
	 *
	public static void ProcNextID (int AD_Sequence_ID, String System, int[] retVal)
	throws SQLException
	{
		retVal[0] = nextID(AD_Sequence_ID, System);
	}
	
	/**
	 * 	Get current Date (Timestamp)
	 *	@return Timestamp
	 */
	public static Timestamp getDate()
	{
		return new Timestamp(System.currentTimeMillis());
	}	//	getDate
	
	/**
	 * 	To Characters
	 *  @param d double
	 *	@return String
	 */	
	public static String getChars(BigDecimal d)
	{
		if (d== null)
			return "0";
		String s = d.toString();
		int i = s.indexOf('.');
		boolean non0 = false;
		if (i!=-1)
			for (int j=i+1; j<s.length(); j++)
				if (s.charAt(j)!= '0')
				{
					non0 = true;
					break;
				}
		if (!non0)
			s=s.substring(0,i);
		
		if (s==null || s.length()==0)
			return "0";
		
		return s;
	}	//	getChars
	
	/**
	 * 	get chars from a number
	 *  @param d double
	 *	@return String
	 *	
	 */	
	//jz not found anywhere, add it
	public static String Time2Chars(Timestamp t )
	{
		if (t== null)
			return "";
		return t.toString();
	}	//	getChars
	
	
}	//	Compiere
