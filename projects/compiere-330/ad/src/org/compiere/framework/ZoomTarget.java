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
package org.compiere.framework;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.util.*;

/**
 *	Zoom Target identifier.
 *  Used in Zoom across (Where used) and Zoom into
 *	
 *  @author Jorg Janke
 *  @version $Id: ZoomTarget.java,v 1.0 2007/10/17 00:51:27 nnayak Exp $
 */
public class ZoomTarget {

	/**	Static Logger	*/
	private static CLogger	log	= CLogger.getCLogger (ZoomTarget.class);

	/**
	 *  Parse String and add columnNames to the list.
	 *  String should be of the format ColumnName=<Value> AND ColumnName2=<Value2>
	 *  @param list list to be added to
	 *  @param parseString string to parse for variables
	 */
	public static void parseColumns (ArrayList<String> list, String parseString)
	{
		if (parseString == null || parseString.length() == 0)
			return;

		//	log.fine(parseString);
		String s = parseString;
		
		// Currently parsing algorithm does not handle parenthesis, IN clause or EXISTS clause
		if (s.contains(" EXISTS ") || s.contains(" IN ") || s.contains("(") || s.contains(")"))
				return;
		
		//  while we have columns
		while (s.indexOf("=") != -1)
		{
			int endIndex = s.indexOf("=");
			int beginIndex = s.lastIndexOf(' ', endIndex);
			
			String variable = s.substring(beginIndex+1, endIndex);
			
			if(variable.indexOf(".")!=-1)
			{
				beginIndex = variable.indexOf(".")+1;
				variable = variable.substring(beginIndex, endIndex);
			}
			
			if(!list.contains(variable))
				list.add(variable);
			
			s = s.substring(endIndex+1);
		}
	}   //  parseDepends

	/**
	 *  Evaluate where clause
	 *  @param columnValues columns with the values
	 *  @param whereClause where clause
	 *  @return true if where clause evaluates to true
	 */
	public static boolean evaluateWhereClause (ArrayList<ValueNamePair>	columnValues, String whereClause)
	{
		if(whereClause == null || whereClause.length()==0)
			return true;
		
		
		String s=whereClause;
		boolean result=true;

		// Currently parsing algorithm does not handle parenthesis, IN clause or EXISTS clause
		if (s.contains(" EXISTS ") || s.contains(" IN ") || s.contains("(") || s.contains(")"))
				return false;

		//  while we have variables
		while (s.indexOf("=") != -1)
		{
			int endIndex = s.indexOf("=");
			int beginIndex = s.lastIndexOf(' ', endIndex);
			
			String variable = s.substring(beginIndex+1, endIndex);
			String operand1="";
			String operand2="";
			String operator="=";
			
			if(variable.indexOf(".")!=-1)
			{
				beginIndex = variable.indexOf(".");
				variable = variable.substring(beginIndex, endIndex);
			}
			
			for(int i=0; i<columnValues.size(); i++)
			{						
				if(variable.equals(columnValues.get(i).getName()))
				{
					operand1 = "'"+ columnValues.get(i).getValue()+"'";
					break;
				}
			
			}

			s=s.substring(endIndex+1);
			beginIndex = 0;
			endIndex = s.indexOf(' ');
			if(endIndex==-1)
				operand2 = s.substring(beginIndex);
			else
				operand2=s.substring(beginIndex, endIndex);
			
			/* log.fine("operand1:"+operand1+ 
					" operator:"+ operator +
					" operand2:"+operand2); */
			if(!Evaluator.evaluateLogicTuple(operand1, operator, operand2))
			{
				result=false;
				break;
			}
		}

		return result;
	}
	
	/**
	 * 	Get the Zoom Into Target for a table.
	 *  
	 *  @param targetTableName for Target Table for zoom
	 *  @param curWindow_ID Window from where zoom is invoked
	 * 	@param targetWhereClause Where Clause in the format "Record_ID=<value>"
	 *  @param isSOTrx Sales contex of window from where zoom is invoked
	 */
	public static int getZoomAD_Window_ID (String targetTableName, int curWindow_ID, String targetWhereClause, boolean isSOTrx)
	{

		int zoomWindow_ID = 0;
		int PO_zoomWindow_ID = 0;
		// Find windows where the first tab is based on the table
		String sql = "SELECT DISTINCT AD_Window_ID, PO_Window_ID "
			+ "FROM AD_Table t "
			+ "WHERE TableName = ?";

		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, (Trx) null);
			int index = 1;
			pstmt.setString (index++, targetTableName);
			ResultSet rs = pstmt.executeQuery();
	
			if (rs.next())
			{
				zoomWindow_ID= rs.getInt(1);
				PO_zoomWindow_ID = rs.getInt(2);
			}
			
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql, e);
		}

		if(PO_zoomWindow_ID == 0)
			return zoomWindow_ID;

		int AD_Window_ID=0;
		
		if(targetWhereClause!=null && targetWhereClause.length() !=0)
		{
			ArrayList<KeyNamePair>	zoomList = new ArrayList<KeyNamePair>();
			zoomList= ZoomTarget.getZoomTargets(targetTableName, curWindow_ID, targetWhereClause);
			if(zoomList != null && zoomList.size()>0)
				AD_Window_ID=zoomList.get(0).getKey();
		}
		
		if (AD_Window_ID != 0)
			return AD_Window_ID;
	
		if(isSOTrx)
			return zoomWindow_ID;
			
		return PO_zoomWindow_ID;

	}

	/**
	 * 	Get the Zoom Across Targets for a table.
	 *  
	 *  @param targetTableName for Target Table for zoom
	 *  @param curWindow_ID Window from where zoom is invoked
	 * 	@param targetWhereClause Where Clause in the format "WHERE Record_ID=?"
	 *  @param params[] parameter to whereClause. Should be the Record_ID
	 */
	public static ArrayList<KeyNamePair> getZoomTargets (String targetTableName, int curWindow_ID, String targetWhereClause, Object[] params)
	{
		if (params.length != 1)
			return null;
		
		Integer record_ID = (Integer) params[0];
		String whereClause = targetWhereClause.replace("?", record_ID.toString());
		whereClause = whereClause.replace("WHERE ", " ");
		
		log.fine("WhereClause : " + whereClause);
		return getZoomTargets(targetTableName, curWindow_ID, whereClause);

	}
	
	/**
	 * 	Get the Zoom Across Targets for a table.
	 *  
	 *  @param targetTableName for Target Table for zoom
	 *  @param curWindow_ID Window from where zoom is invoked
	 * 	@param targetWhereClause Where Clause in the format "Record_ID=<value>"
	 */
	public static ArrayList<KeyNamePair> getZoomTargets (String targetTableName, int curWindow_ID, String targetWhereClause)
	{
		/**
		 * 	Window WhereClause 
		 */
		class WindowWhereClause
		{
			/**
			 * 	Org Access constructor
			 *	@param ad_Window_ID window
			 *	@param name Window Name
			 *	@param where Where Clause on the first tab of the window 
			 */
			public WindowWhereClause (int ad_Window_ID, String name, String where)
			{
				this.AD_Window_ID = ad_Window_ID;
				this.windowName = name;
				this.whereClause = where;
			}
			/** Window				*/
			public int AD_Window_ID = 0;
			/** Window Name			*/
			public String windowName = "";
			/** Window Where Clause	*/
			public String whereClause = "";
			
			
			/**
			 * 	Extended String Representation
			 *	@return extended info
			 */
			@Override
			public String toString ()
			{
				StringBuffer sb = new StringBuffer();
				sb.append(Msg.translate(Env.getCtx(), "AD_Window_ID")).append("=")
					.append(windowName).append(" - ")
					.append(whereClause);
				return sb.toString();
			}	//	toString

		}	//	WindowWhereClause

		/**	The Option List					*/
		ArrayList<KeyNamePair>	zoomList = new ArrayList<KeyNamePair>();
		ArrayList<WindowWhereClause> windowList = new ArrayList<WindowWhereClause> ();
		ArrayList<String> columns = new ArrayList<String>();
		int zoom_Window_ID = 0;
		int PO_Window_ID=0;
		String zoom_WindowName = "";
		String whereClause = "";
		boolean windowFound = false;

		// Find windows where the first tab is based on the table
		String sql = "SELECT DISTINCT w.AD_Window_ID, w.Name, tt.WhereClause, t.TableName, " +
				"wp.AD_Window_ID, wp.Name, ws.AD_Window_ID, ws.Name "
			+ "FROM AD_Table t "
			+ "INNER JOIN AD_Tab tt ON (tt.AD_Table_ID = t.AD_Table_ID) ";
		boolean baseLanguage = Env.isBaseLanguage(Env.getCtx(), "AD_Window"); 
		if (baseLanguage)
		{
			sql += "INNER JOIN AD_Window w ON (tt.AD_Window_ID=w.AD_Window_ID)";
			sql += " LEFT OUTER JOIN AD_Window ws ON (t.AD_Window_ID=ws.AD_Window_ID)"
				+  " LEFT OUTER JOIN AD_Window wp ON (t.PO_Window_ID=wp.AD_Window_ID)";
		}
		else
		{
			sql += "INNER JOIN AD_Window_Trl w ON (tt.AD_Window_ID=w.AD_Window_ID AND w.AD_Language=?)";
			sql += " LEFT OUTER JOIN AD_Window_Trl ws ON (t.AD_Window_ID=ws.AD_Window_ID AND ws.AD_Language=?)"
				+  " LEFT OUTER JOIN AD_Window_Trl wp ON (t.PO_Window_ID=wp.AD_Window_ID AND wp.AD_Language=?)";
		}
		sql	+= "WHERE t.TableName = ?"
			+ " AND w.AD_Window_ID <> ?"
			+ " AND tt.SeqNo=10"
			+ " AND (wp.AD_Window_ID IS NOT NULL "
					+ "OR EXISTS (SELECT 1 FROM AD_Tab tt2 WHERE tt2.AD_Window_ID = ws.AD_Window_ID AND tt2.AD_Table_ID=t.AD_Table_ID AND tt2.SeqNo=10))"
			+ " ORDER BY 2";
	
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, (Trx) null);
			int index = 1;
			if (!baseLanguage)
			{
				pstmt.setString (index++, Env.getAD_Language(Env.getCtx()));
				pstmt.setString (index++, Env.getAD_Language(Env.getCtx()));
				pstmt.setString (index++, Env.getAD_Language(Env.getCtx()));
			}
			pstmt.setString (index++, targetTableName);
			pstmt.setInt (index++, curWindow_ID);
			ResultSet rs = pstmt.executeQuery();
	
			while (rs.next())
			{
				windowFound = true;
				zoom_Window_ID= rs.getInt(7);
				zoom_WindowName = rs.getString(8);
				PO_Window_ID = rs.getInt(5);
				whereClause= rs.getString(3);
				
				// Multiple window support only for Order, Invoice, Shipment/Receipt which have PO windows
				if (PO_Window_ID == 0)
					break;

				WindowWhereClause windowClause = new WindowWhereClause (rs.getInt(1), rs.getString(2), whereClause);
				windowList.add(windowClause);
			}
			
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql, e);
		}

	
		String sql1="";
		
		if (!windowFound || (windowList.size() <=1 && zoom_Window_ID == 0))
			return zoomList;
		
		//If there is a single window for the table, no parsing is neccessary
		if(windowList.size() <= 1)
		{
			
			//Check if record exists in target table
			sql1 = "SELECT count(*) FROM " +targetTableName + " WHERE "
						+ targetWhereClause;
			if(whereClause != null && whereClause.length() !=0)
				sql1 += " AND " + Evaluator.replaceVariables(whereClause,Env.getCtx(), null);
			
		}
		else if (windowList.size() > 1)
		{
			// Get the columns used in the whereClause
			for (int i=0; i< windowList.size();i++)
				parseColumns(columns,windowList.get(i).whereClause);				
	
			// Get the distinct values of the columns from the table if record exists
			sql1 = "SELECT DISTINCT ";
			for(int i=0; i<columns.size();i++)
			{
				if(i!=0)
					sql1 +=",";
				sql1 += columns.get(i);
			}
			
			if(columns.size()==0)
				sql1 += "count(*) ";
			sql1 += " FROM " +targetTableName + " WHERE "
					+ targetWhereClause;
		}
	
		
		log.fine(sql1);

		ArrayList<ValueNamePair>	columnValues = new ArrayList<ValueNamePair>();		
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql1, (Trx) null);
	
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				if (columns.size() > 0)
				{
					columnValues.clear();
					for(int i=0; i<columns.size();i++)
					{
						String columnName = columns.get(i);
						String columnValue = (String)rs.getObject(columnName);
						log.fine(columnName + " = "+columnValue);
						columnValues.add(new ValueNamePair(columnValue,columnName));
					}
					
					// Find matching windows
					for (int i=0; i<windowList.size(); i++)
					{
						log.fine("Window : "+windowList.get(i).windowName + " WhereClause : " + windowList.get(i).whereClause);
						if(evaluateWhereClause(columnValues,windowList.get(i).whereClause))
						{
							log.fine("MatchFound : "+windowList.get(i).windowName );
							KeyNamePair pp = new KeyNamePair (windowList.get(i).AD_Window_ID, windowList.get(i).windowName);
							zoomList.add(pp);
							// Use first window found. Ideally there should be just one matching
							break;
						}
					}
				}
				else
				{
					int rowCount = rs.getInt(1);
					if(rowCount != 0)
					{
						KeyNamePair pp = new KeyNamePair (zoom_Window_ID, zoom_WindowName);
						zoomList.add(pp);
					}
				}
			}
			
			
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql1, e);
		}
	
		
		return zoomList;

	}// getZoomTargets 


}// ZoomTarget
