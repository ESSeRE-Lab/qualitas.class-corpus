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

import java.beans.*;
import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.event.*;
import javax.swing.table.*;

import org.compiere.common.*;
import org.compiere.common.constants.*;
import org.compiere.framework.*;
import org.compiere.swing.*;
import org.compiere.util.*;

/**
 *	Grid Table Model for JDBC access including buffering.
 *  <pre>
 *		The following data types are handled
 *			Integer		for all IDs
 *			BigDecimal	for all Numbers
 *			Timestamp	for all Dates
 *			String		for all others
 *  The data is read via r/o resultset and cached in m_buffer. Writes/updates
 *  are via dynamically constructed SQL INSERT/UPDATE statements. The record
 *  is re-read via the resultset to get results of triggers.
 *
 *  </pre>
 *  The model maintains and fires the requires TableModelEvent changes,
 *  the DataChanged events (loading, changed, etc.)
 *  as well as Vetoable Change event "RowChange"
 *  (for row changes initiated by moving the row in the table grid).
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: GridTable.java,v 1.9 2006/08/09 16:38:25 jjanke Exp $
 */
public class GridTable extends AbstractTableModel
	implements Serializable
{

	/** */
    private static final long serialVersionUID = -5579148977075510511L;

	/**
	 *	JDBC Based Buffered Table
	 *
	 *  @param ctx Properties
	 *  @param AD_Table_ID table id
	 *  @param TableName table name
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param withAccessControl    if true adds AD_Client/Org restrictions
	 */
	public GridTable(Ctx ctx, int AD_Table_ID, String TableName, int WindowNo, int TabNo,
		boolean withAccessControl)
	{
		super();
		log.info(TableName);
		m_ctx = ctx;
		m_AD_Table_ID = AD_Table_ID;
		setTableName(TableName);
		m_WindowNo = WindowNo;
		m_TabNo = TabNo;
		m_withAccessControl = withAccessControl;
	}	//	MTable

	/** Logging					*/
	static CLogger				log = CLogger.getCLogger(GridTable.class);

	private final Ctx					m_ctx;
	private final int					m_AD_Table_ID;
	private String 		        m_tableName = "";
	private final int				    m_WindowNo;
	/** Tab No 0..				*/
	private final int				    m_TabNo;
	private final boolean			    m_withAccessControl;
	private boolean			    m_readOnly = true;
	private boolean			    m_deleteable = true;
	//

	/**	Row count                    */
	private int				    m_rowCount = 0;
	/**	Has Data changed?           */
	private boolean			    m_changed = false;
	/** Index of changed row via SetValueAt */
	private int				    m_rowChanged = -1;
	/** Insert mode active          */
	private boolean			    m_inserting = false;
	/** Inserted Row number         */
	private int                 m_newRow = -1;
	/**	Is the Resultset open?      */
	private boolean			    m_open = false;
	/**	Compare to DB before save	*/
	private boolean				m_compareDB = true;		//	set to true after every save

	/**	The buffer for all data		*/
	volatile ArrayList<Object[]>	m_buffer = new ArrayList<Object[]>(100);
	/** Sort array					*/
	volatile ArrayList<MSort>		m_sort = new ArrayList<MSort>(100);
	/** Original row data               */
	private Object[]			m_rowData = null;
	/** Original data [row,col,data]    */
	private Object[]            m_oldValue = null;
	/** Loader							*/
	Loader					m_loader = null;

	/**	Columns                 		*/
	ArrayList<GridField>	m_fields = new ArrayList<GridField>(30);
	ArrayList<Object>		m_parameterSELECT = new ArrayList<Object>(5);
	ArrayList<Object>		m_parameterWHERE = new ArrayList<Object>(5);

	/** Complete SQL statement          */
	String 		        m_SQL;
	/** SQL Statement for Row Count     */
	String 		        m_SQL_Count;
	/** The SELECT clause with FROM     */
	private String 		        m_SQL_Select;
	/** The static where clause         */
	private String 		        m_whereClause = "";
	/** Static ORDER BY clause          */
	private String		        m_orderClause = "";
	/** Max Rows to query or 0 for all	*/
	private int					m_maxRows = 0;

	/** Index of Key Column                 */
	private int			        m_indexKeyColumn = -1;
	/** Index of Color Column               */
	private int			        m_indexColorColumn = -1;
	/** Index of Processed Column           */
	private int                 m_indexProcessedColumn = -1;
	/** Index of IsActive Column            */
	private int                 m_indexActiveColumn = -1;
	/** Index of AD_Client_ID Column        */
	private int					m_indexClientColumn = -1;
	/** Index of AD_Org_ID Column           */
	private int					m_indexOrgColumn = -1;

	/** Vetoable Change Bean support    */
	private VetoableChangeSupport   m_vetoableChangeSupport = new VetoableChangeSupport(this);
	/** Property of Vetoable Bean support "RowChange" */
	public static final String  PROPERTY = "MTable-RowSave";

	/**
	 *	Set Table Name
	 *  @param newTableName table name
	 */
	public void setTableName(String newTableName)
	{
		if (m_open)
		{
			log.log(Level.SEVERE, "Table already open - ignored");
			return;
		}
		if (newTableName == null || newTableName.length() == 0)
			return;
		m_tableName = newTableName;
	}	//	setTableName

	/**
	 *	Get Table Name
	 *  @return table name
	 */
	public String getTableName()
	{
		return m_tableName;
	}	//	getTableName

	/**
	 *	Set Where Clause (w/o the WHERE and w/o History).
	 *  @param newWhereClause sql where clause
	 *	@return true if where clause set
	 */
	public boolean setSelectWhereClause(String newWhereClause)
	{
		if (m_open)
		{
			log.log(Level.SEVERE, "Table already open - ignored");
			return false;
		}
		//
		m_whereClause = newWhereClause;
		if (m_whereClause == null)
			m_whereClause = "";
		return true;
	}	//	setWhereClause

	/**
	 *	Get record set Where Clause (w/o the WHERE and w/o History)
	 *  @return where clause
	 */
	public String getSelectWhereClause()
	{
		return m_whereClause;
	}	//	getWhereClause


	/**
	 *	Set Order Clause (w/o the ORDER BY)
	 *  @param newOrderClause sql order by clause
	 */
	public void setOrderClause(String newOrderClause)
	{
		m_orderClause = newOrderClause;
		if (m_orderClause == null)
			m_orderClause = "";
	}	//	setOrderClause

	/**
	 *	Get Order Clause (w/o the ORDER BY)
	 *  @return order by clause
	 */
	public String getOrderClause()
	{
		return m_orderClause;
	}	//	getOrderClause

	/**
	 *	Assemble & store
	 *	m_SQL and m_countSQL
	 *  @return m_SQL
	 */
	private String createSelectSql()
	{
		if (m_fields.size() == 0 || m_tableName == null || m_tableName.equals(""))
			return "";

		//	Create SELECT Part
		StringBuffer select = new StringBuffer("SELECT ");
		for (int i = 0; i < m_fields.size(); i++)
		{
			if (i > 0)
				select.append(",");
			GridField field = m_fields.get(i);
			select.append(field.getColumnSQL(true));	//	ColumnName or Virtual Column
		}
		//
		select.append(" FROM ").append(m_tableName);
		m_SQL_Select = select.toString();
		m_SQL_Count = "SELECT COUNT(*) FROM " + m_tableName;
		//

		StringBuffer m_SQL_Where = new StringBuffer("");
		//	WHERE
		if (m_whereClause.length() > 0)
		{
			m_SQL_Where.append(" WHERE ");
			if (m_whereClause.indexOf("@") == -1)
			{
				m_SQL_Where.append(m_whereClause);
			}
			else    //  replace variables
				m_SQL_Where.append(Env.parseContext(m_ctx, m_WindowNo, m_whereClause, false));
			//
			if (m_whereClause.toUpperCase().indexOf("=NULL")>0)
				log.severe("Invalid NULL - " + m_tableName + "=" + m_whereClause);
		}

		//	RO/RW Access
		m_SQL = m_SQL_Select + m_SQL_Where.toString();
		m_SQL_Count += m_SQL_Where.toString();
		if (m_withAccessControl)
		{
		//	boolean ro = MRole.SQL_RO;
		//	if (!m_readOnly)
		//		ro = MRole.SQL_RW;
			m_SQL = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL,
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
			m_SQL_Count = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL_Count,
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
		}

		//	ORDER BY
		if (!m_orderClause.equals(""))
			m_SQL += " ORDER BY " + m_orderClause;
		//
		log.fine(m_SQL_Count);
		m_ctx.setContext(m_WindowNo, m_TabNo, "SQL", m_SQL);
		return m_SQL;
	}	//	createSelectSql

	/**
	 *	Add Field to Table
	 *  @param field field
	 */
	public void addField (GridField field)
	{
		log.fine("(" + m_tableName + ") - " + field.getColumnName());
		if (m_open)
		{
			log.log(Level.SEVERE, "Table already open - ignored: " + field.getColumnName());
			return;
		}
		if (!MRole.getDefault(m_ctx, false).isColumnAccess (m_AD_Table_ID, field.getAD_Column_ID(), true))
		{
			log.fine("No Column Access " + field.getColumnName());
			return;
		}
		//  Set Index for Key column
		if (field.isKey())
			m_indexKeyColumn = m_fields.size();
		else if (field.getColumnName().equals("IsActive"))
			m_indexActiveColumn = m_fields.size();
		else if (field.getColumnName().equals("Processed"))
			m_indexProcessedColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Client_ID"))
			m_indexClientColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Org_ID"))
			m_indexOrgColumn = m_fields.size();
		//
		m_fields.add(field);

		fireTableStructureChanged();
	}	//	addColumn

	/**
	 *  Returns database column name
	 *
	 *  @param index  the column being queried
	 *  @return column name
	 */
	@Override
	public String getColumnName (int index)
	{
		if (index < 0 || index > m_fields.size())
		{
			log.log(Level.SEVERE, "Invalid index=" + index);
			return "";
		}
		//
		GridField field = m_fields.get(index);
		return field.getColumnName();
	}   //  getColumnName

	/**
	 * Returns a column given its name.
	 *
	 * @param columnName string containing name of column to be located
	 * @return the column index with <code>columnName</code>, or -1 if not found
	 */
	@Override
	public int findColumn (String columnName)
	{
		for (int i = 0; i < m_fields.size(); i++)
		{
			GridField field = m_fields.get(i);
			if (columnName.equals(field.getColumnName()))
				return i;
		}
		return -1;
	}   //  findColumn

	/**
	 *  Returns Class of database column/field
	 *
	 *  @param index  the column being queried
	 *  @return the class
	 */
	@Override
	public Class<?> getColumnClass (int index)
	{
		if (index < 0 || index >= m_fields.size())
		{
			log.log(Level.SEVERE, "Invalid index=" + index);
			return null;
		}
		GridField field = m_fields.get(index);
		return DisplayType.getClass(field.getDisplayType(), false);
	}   //  getColumnClass

	/**
	 *	Set Select Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterSELECT (int index, Object parameter)
	{
		if (index >= m_parameterSELECT.size())
			m_parameterSELECT.add(parameter);
		else
			m_parameterSELECT.set(index, parameter);
	}	//	setParameterSELECT

	/**
	 *	Set Where Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterWHERE (int index, Object parameter)
	{
		if (index >= m_parameterWHERE.size())
			m_parameterWHERE.add(parameter);
		else
			m_parameterWHERE.set(index, parameter);
	}	//	setParameterWHERE


	/**
	 *	Get Column at index
	 *  @param index index
	 *  @return MField
	 */
	protected GridField getField (int index)
	{
		if (index < 0 || index >= m_fields.size())
			return null;
		return m_fields.get(index);
	}	//	getColumn

	/**
	 *	Return Columns with Indentifier (ColumnName)
	 *  @param identifier column name
	 *  @return MField
	 */
	protected GridField getField (String identifier)
	{
		if (identifier == null || identifier.length() == 0)
			return null;
		int cols = m_fields.size();
		for (int i = 0; i < cols; i++)
		{
			GridField field = m_fields.get(i);
			if (identifier.equalsIgnoreCase(field.getColumnName()))
				return field;
		}
	//	log.log(Level.WARNING, "Not found: '" + identifier + "'");
		return null;
	}	//	getField

	/**
	 *  Get all Fields
	 *  @return MFields
	 */
	public GridField[] getFields ()
	{
		GridField[] retValue = new GridField[m_fields.size()];
		m_fields.toArray(retValue);
		return retValue;
	}   //  getField


	/**************************************************************************
	 *	Open Database.
	 *  if already opened, data is refreshed
	 *	@param maxRows maximum number of rows or 0 for all
	 *	@return true if success
	 */
	public boolean open (int maxRows)
	{
		log.info("MaxRows=" + maxRows);
		m_maxRows = maxRows;
		if (m_open)
		{
			log.fine("already open");
			dataRefreshAll();
			return true;
		}

		//	create m_SQL and m_countSQL
		createSelectSql();
		if (m_SQL == null || m_SQL.equals(""))
		{
			log.log(Level.SEVERE, "No SQL");
			return false;
		}

		//	Start Loading
		m_loader = new Loader();
		m_rowCount = m_loader.open(maxRows);
		m_buffer = new ArrayList<Object[]>(m_rowCount+10);
		m_sort = new ArrayList<MSort>(m_rowCount+10);
		if (m_rowCount > 0)
			m_loader.start();
		else
			m_loader.close();
		m_open = true;
		//
		m_changed = false;
		m_rowChanged = -1;

		fireTableDataChanged();
		//	Audit
		if (m_rowCount > 0)
		{
			MSession session = MSession.get(m_ctx);
			if (session != null)
				session.queryLog(m_ctx.getAD_Client_ID(), m_ctx.getAD_Org_ID(), m_AD_Table_ID,
					m_SQL_Count, m_rowCount);
			else
			{
				log.warning("No Session");
				close(true);
				return false;
			}
		}
		return true;
	}	//	open

	/**
	 *  Wait until async loader of Table and Lookup Fields is complete
	 *  Used for performance tests
	 */
	public void loadComplete()
	{
		//  Wait for loader
		if (m_loader != null)
		{
			if (m_loader.isAlive())
			{
				try
				{
					m_loader.join();
				}
				catch (InterruptedException ie)
				{
					log.log(Level.SEVERE, "Join interrupted", ie);
				}
			}
		}
		//  wait for field lookup loaders
		for (int i = 0; i < m_fields.size(); i++)
		{
			GridField field = m_fields.get(i);
			field.lookupLoadComplete();
		}
	}   //  loadComplete

	/**
	 *  Is Loading
	 *  @return true if loading
	 */
	public boolean isLoading()
	{
		if (m_loader != null && m_loader.isAlive())
			return true;
		return false;
	}   //  isLoading

	/**
	 *	Is it open?
	 *  @return true if opened
	 */
	public boolean isOpen()
	{
		return m_open;
	}	//	isOpen

	/**
	 *	Close Resultset
	 *  @param finalCall final call
	 */
	public void close (boolean finalCall)
	{
		if (!m_open)
			return;
		log.fine("final=" + finalCall);

		//  remove listeners
		if (finalCall)
		{
			DataStatusListener evl[] = listenerList.getListeners(DataStatusListener.class);
			for (DataStatusListener element : evl)
				listenerList.remove(DataStatusListener.class, element);
			TableModelListener ev2[] = listenerList.getListeners(TableModelListener.class);
			for (TableModelListener element : ev2)
				listenerList.remove(TableModelListener.class, element);
			VetoableChangeListener vcl[] = m_vetoableChangeSupport.getVetoableChangeListeners();
			for (VetoableChangeListener element : vcl)
				m_vetoableChangeSupport.removeVetoableChangeListener(element);
		}

		//	Stop loader
		while (m_loader != null && m_loader.isAlive())
		{
			log.fine("Interrupting Loader ...");
			m_loader.interrupt();
			try
			{
				Thread.sleep(200);		//	.2 second
			}
			catch (InterruptedException ie)
			{}
		}

		if (!m_inserting)
			dataSave(false);	//	not manual

		if (m_buffer != null)
			m_buffer.clear();
		m_buffer = null;
		if (m_sort != null)
			m_sort.clear();
		m_sort = null;

		if (finalCall)
			dispose();

		//  Fields are disposed from MTab
		log.fine("");
		m_open = false;
	}	//	close

	/**
	 *  Dispose MTable.
	 *  Called by close-final
	 */
	private void dispose()
	{
		//  MFields
		for (int i = 0; i < m_fields.size(); i++)
			m_fields.get(i).dispose();
		m_fields.clear();
		m_fields = null;
		//
		m_vetoableChangeSupport = null;
		//
		m_parameterSELECT.clear();
		m_parameterSELECT = null;
		m_parameterWHERE.clear();
		m_parameterWHERE = null;
		//  clear data arrays
		m_buffer = null;
		m_sort = null;
		m_rowData = null;
		m_oldValue = null;
		m_loader = null;
	}   //  dispose

	/**
	 *	Get total database column count (displayed and not displayed)
	 *  @return column count
	 */
	public int getColumnCount()
	{
		return m_fields.size();
	}	//	getColumnCount

	/**
	 *	Get (displayed) field count
	 *  @return field count
	 */
	public int getFieldCount()
	{
		return m_fields.size();
	}	//	getFieldCount

	/**
	 *  Return number of rows
	 *  @return Number of rows or 0 if not opened
	 */
	public int getRowCount()
	{
		return m_rowCount;
	}	//	getRowCount

	/**
	 *	Set the Column to determine the color of the row
	 *  @param columnName column name
	 */
	public void setColorColumn (String columnName)
	{
		m_indexColorColumn = findColumn(columnName);
	}	//  setColorColumn

	/**
	 *	Get ColorCode for Row.
	 *  <pre>
	 *	If numerical value in compare column is
	 *		negative = -1,
	 *      positive = 1,
	 *      otherwise = 0
	 *  </pre>
	 *  @see #setColorColumn
	 *  @param row row
	 *  @return color code
	 */
	public int getColorCode (int row)
	{
		if (m_indexColorColumn  == -1)
			return 0;
		Object data = getValueAt(row, m_indexColorColumn);
		//	We need to have a Number
		if (data == null || !(data instanceof BigDecimal))
			return 0;
		BigDecimal bd = (BigDecimal)data;
		return bd.signum();
	}	//	getColorCode


	/**
	 *	Sort Entries by Column.
	 *  actually the rows are not sorted, just the access pointer ArrayList
	 *  with the same size as m_buffer with MSort entities
	 *  @param col col
	 *  @param ascending ascending
	 */
	public void sort (int col, boolean ascending)
	{
		log.info("#" + col + " " + ascending);
		if (getRowCount() == 0)
			return;
		GridField field = getField (col);
		//	RowIDs are not sorted
		if (field.getDisplayType() == DisplayTypeConstants.RowID)
			return;
		boolean isLookup = FieldType.isLookup(field.getDisplayType());
		boolean isASI = DisplayTypeConstants.PAttribute == field.getDisplayType();

		//	fill MSort entities with data entity
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort sort = m_sort.get(i);
			Object[] rowData = m_buffer.get(sort.index);
			if (rowData[col] == null)
				sort.data = null;
			else if (isLookup || isASI)
				sort.data = field.getLookup().getDisplay(rowData[col]);	//	lookup
			else
				sort.data = rowData[col];								//	data
		}
		log.info(field.toString() + " #" + m_sort.size());

		//	sort it
		MSort sort = new MSort(0, null);
		sort.setSortAsc(ascending);
		Collections.sort(m_sort, sort);
		//	update UI
		fireTableDataChanged();
		//  Info detected by MTab.dataStatusChanged and current row set to 0
		fireDataStatusIEvent("Sorted", "#" + m_sort.size());
	}	//	sort

	/**
	 *	Get Key ID or -1 of none
	 *  @param row row
	 *  @return ID or -1
	 */
	public int getKeyID (int row)
	{
	//	Log.info("MTable.getKeyID - row=" + row + ", keyColIdx=" + m_indexKeyColumn);
		if (m_indexKeyColumn != -1)
		{
			try
			{
				Integer ii = (Integer)getValueAt(row, m_indexKeyColumn);
				if (ii == null)
					return -1;
				return ii.intValue();
			}
			catch (Exception e)     //  Alpha Key
			{
/**				if (m_AD_Table_ID == MEntityType.Table_ID)
				{
					m_indexKeyColumn = findColumn("AD_EntityType_ID");
					try
					{
						Integer ii = (Integer)getValueAt(row, m_indexKeyColumn);
						if (ii == null)
							return -1;
						return ii.intValue();
					}
					catch (Exception ee)
					{
					}
				}
**/				return -1;
			}
		}
		return -1;
	}	//	getKeyID

	/**
	 *	Get Key ColumnName
	 *  @return key column name
	 */
	public String getKeyColumnName()
	{
		if (m_indexKeyColumn != -1)
			return getColumnName(m_indexKeyColumn);
		return "";
	}	//	getKeyColumnName


	/**************************************************************************
	 * 	Get Value in Resultset
	 *  @param row row
	 *  @param col col
	 *  @return Object of that row/column
	 */
	public Object getValueAt (int row, int col)
	{
	//	log.config( "MTable.getValueAt r=" + row + " c=" + col);
		if (!m_open || row < 0 || col < 0 || row >= m_rowCount)
		{
		//	log.fine( "Out of bounds - Open=" + m_open + ", RowCount=" + m_rowCount);
			return null;
		}

		//	need to wait for data read into buffer
		int loops = 0;
		while (row >= m_buffer.size() && m_loader.isAlive() && loops < 15)
		{
			log.fine("Waiting for loader row=" + row + ", size=" + m_buffer.size());
			try
			{
				Thread.sleep(500);		//	1/2 second
			}
			catch (InterruptedException ie)
			{}
			loops++;
		}

		//	empty buffer
		if (row >= m_buffer.size())
		{
		//	log.fine( "Empty buffer");
			return null;
		}

		//	return Data item
		MSort sort = m_sort.get(row);
		Object[] rowData = m_buffer.get(sort.index);
		//	out of bounds
		if (rowData == null || col > rowData.length)
		{
		//	log.fine( "No data or Column out of bounds");
			return null;
		}
		return rowData[col];
	}	//	getValueAt

	/**
	 *	Indicate that there will be a change
	 *  @param changed changed
	 */
	public void setChanged (boolean changed)
	{
		//	Can we edit?
		if (!m_open || m_readOnly)
			return;

		//	Indicate Change
		m_changed = changed;
		if (!changed)
			m_rowChanged = -1;
		if (changed)
			fireDataStatusIEvent("", "");
	}	//	setChanged

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 */
	@Override
	public final void setValueAt (Object value, int row, int col)
	{
		setValueAt (value, row, col, false);
	}	//	setValueAt

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 * 	@param	force force setting new value
	 */
	public final void setValueAt (Object value, int row, int col, boolean force)
	{
		//	Can we edit?
		if (!m_open || m_readOnly       //  not accessible
				|| row < 0 || col < 0   //  invalid index
				|| m_rowCount == 0)     //  no rows
		{
			log.finest("r=" + row + " c=" + col + " - R/O=" + m_readOnly + ", Rows=" + m_rowCount + " - Ignored");
			return;
		}

		dataSave(row, false);

		//	Has anything changed?
		Object oldValue = getValueAt(row, col);
		if (!force && (
			oldValue == null && value == null
			||	oldValue != null && oldValue.equals(value)
			||	oldValue != null && value != null && oldValue.toString().equals(value.toString())
			||  oldValue == null && "".equals( value )
			))
		{
			log.finest("r=" + row + " c=" + col + " - New=" + value + "==Old=" + oldValue + " - Ignored");
			return;
		}

		log.fine("r=" + row + " c=" + col + " = " + value + " (" + oldValue + ")");

		//  Save old value
		m_oldValue = new Object[3];
		m_oldValue[0] = Integer.valueOf(row);
		m_oldValue[1] = Integer.valueOf(col);
		m_oldValue[2] = oldValue;

		//	Set Data item
		MSort sort = m_sort.get(row);
		Object[] rowData = m_buffer.get(sort.index);
		m_rowChanged = row;

		/**	Selection
		if (col == 0)
		{
			rowData[col] = value;
			m_buffer.set(sort.index, rowData);
			return;
		}	**/

		//	save original value - shallow copy
		if (m_rowData == null)
		{
			int size = m_fields.size();
			m_rowData = new Object[size];
			for (int i = 0; i < size; i++)
				m_rowData[i] = rowData[i];
		}

		//	save & update
		rowData[col] = value;
		m_buffer.set(sort.index, rowData);
		//  update Table
		fireTableCellUpdated(row, col);
		//  update MField
		GridField field = getField(col);
		field.setValue(value, m_inserting);
		//  inform
		DataStatusEvent evt = createDSE();
		evt.setChangedColumn(col, field.getColumnName());
		fireDataStatusChanged(evt);
	}	//	setValueAt

	/**
	 *  Get Old Value
	 *  @param row row
	 *  @param col col
	 *  @return old value
	 */
	public Object getOldValue (int row, int col)
	{
		if (m_oldValue == null)
			return null;
		if (((Integer)m_oldValue[0]).intValue() == row
				&& ((Integer)m_oldValue[1]).intValue() == col)
			return m_oldValue[2];
		return null;
	}   // getOldValue

	/**
	 *	Check if the current row needs to be saved.
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(boolean onlyRealChange)
	{
		return needSave(m_rowChanged, onlyRealChange);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only if nothing was changed
	 *	@return true it needs to be saved
	 */
	public boolean needSave()
	{
		return needSave(m_rowChanged, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow)
	{
		return needSave(newRow, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow, boolean onlyRealChange)
	{
		log.fine("Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return false;
		//  E.g. New unchanged records
		if (m_changed && m_rowChanged == -1 && onlyRealChange)
			return false;
		//  same row
		if (newRow == m_rowChanged)
			return false;

		return true;
	}	//	needSave

	/*************************************************************************/

	/** Save OK - O		*/
	public static final char	SAVE_OK = 'O';			//	the only OK condition
	/** Save Error - E	*/
	public static final char	SAVE_ERROR = 'E';
	/** Save Access Error - A	*/
	public static final char	SAVE_ACCESS = 'A';
	/** Save Mandatory Error - M	*/
	public static final char	SAVE_MANDATORY = 'M';
	/** Save Abort Error - U	*/
	public static final char	SAVE_ABORT = 'U';

	/**
	 *	Check if it needs to be saved and save it.
	 *  @param newRow row
	 *  @param manualCmd manual command to save
	 *	@return true if not needed to be saved or successful saved
	 */
	public boolean dataSave (int newRow, boolean manualCmd)
	{
		log.fine("Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return true;
		//  same row, don't save yet
		if (newRow == m_rowChanged)
			return true;

		return dataSave(manualCmd) == SAVE_OK;
	}   //  dataSave

	/**
	 *	Save unconditional.
	 *  @param manualCmd if true, no vetoable PropertyChange will be fired for save confirmation
	 *	@return OK Or Error condition
	 *  Error info (Access*, FillMandatory, SaveErrorNotUnique,
	 *  SaveErrorRowNotFound, SaveErrorDataChanged) is saved in the log
	 */
	public char dataSave (boolean manualCmd)
	{
		//	cannot save
		if (!m_open)
		{
			log.warning ("Error - Open=" + m_open);
			return SAVE_ERROR;
		}
		//	no need - not changed - row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			log.config("NoNeed - Changed=" + m_changed + ", Row=" + m_rowChanged);
		//	return SAVE_ERROR;
			if (!manualCmd)
				return SAVE_OK;
		}
		//  Value not changed
		if (m_rowData == null)
		{
			log.fine("No Changes");
			return SAVE_ERROR;
		}

		if (m_readOnly)
		//	If Processed - not editable (Find always editable)  -> ok for changing payment terms, etc.
		{
			log.warning("IsReadOnly - ignored");
			dataIgnore();
			return SAVE_ACCESS;
		}

		//	row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			if (m_newRow != -1)     //  new row and nothing changed - might be OK
				m_rowChanged = m_newRow;
			else
			{
				fireDataStatusEEvent("SaveErrorNoChange", "", false);
				return SAVE_ERROR;
			}
		}

		//	Can we change?
		int[] co = getClientOrg(m_rowChanged);
		int AD_Client_ID = co[0];
		int AD_Org_ID = co[1];
		boolean createError = true;
		if (!MRole.getDefault(m_ctx, false)
			.canUpdate(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, 0, createError))
		{
			fireDataStatusEEvent(CLogger.retrieveError());
			dataIgnore();
			return SAVE_ACCESS;
		}

		log.info("Row=" + m_rowChanged);

		//  inform about data save action, if not manually initiated
		try
		{
			if (!manualCmd)
				m_vetoableChangeSupport.fireVetoableChange(PROPERTY, -1, m_rowChanged);
		}
		catch (PropertyVetoException pve)
		{
			log.warning(pve.getMessage());
			dataIgnore();
			return SAVE_ABORT;
		}

		//	get updated row data
		MSort sort = m_sort.get(m_rowChanged);
		Object[] rowData = m_buffer.get(sort.index);

		//	Check Mandatory
		String missingColumns = getMandatory(rowData);
		if (missingColumns.length() != 0)
		{
		//	Trace.printStack(false, false);
			fireDataStatusEEvent("FillMandatory", missingColumns + "\n", true);
			return SAVE_MANDATORY;
		}

		//	Check miscellaneous errors
		String errorColumns = getErrorColumns();
		if (errorColumns.length() != 0)
		{
		//	Trace.printStack(false, false);
			fireDataStatusEEvent("Error", errorColumns + "\n", true);
			return SAVE_ERROR;
		}

		/**
		 *	Update row *****
		 */
		int Record_ID = 0;
		if (!m_inserting)
			Record_ID = getKeyID(m_rowChanged);
		try
		{
			if (!m_tableName.endsWith("_Trl"))	//	translation tables have no model
				return dataSavePO (Record_ID);
		}
		catch (Exception e)
		{
			if (e instanceof ClassNotFoundException)
				log.warning(m_tableName + " - " + e.getLocalizedMessage());
			else
			{
				log.log(Level.SEVERE, "Persistency Issue - "
					+ m_tableName + ": " + e.getLocalizedMessage(), e);
				return SAVE_ERROR;
			}
		}

		/*******	Manual Update of Row (i.e. not via PO class)	*******/
		log.info("NonPO");

		boolean error = false;
		lobReset();
		//
		String is = null;
		final String ERROR = "ERROR: ";
		final String INFO  = "Info: ";

		//	Update SQL with specific where clause
		StringBuffer select = new StringBuffer("SELECT ");
		for (int i = 0; i < m_fields.size(); i++)
		{
			GridField field = m_fields.get(i);
			if (m_inserting && field.isVirtualColumn())
				continue;
			if (i > 0)
				select.append(",");
			select.append(field.getColumnSQL(true));	//	ColumnName or Virtual Column
		}
		//
		select.append(" FROM ").append(m_tableName);
		StringBuffer singleRowWHERE = new StringBuffer();
		StringBuffer multiRowWHERE = new StringBuffer();
		//	Create SQL	& RowID
		if (m_inserting)
			select.append(" WHERE 1=2");
		else	//  FOR UPDATE causes  -  ORA-01002 fetch out of sequence
			select.append(" WHERE ").append(getWhereClause(rowData));
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (select.toString(),
				ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, null);
			ResultSet rs = pstmt.executeQuery();
			//	only one row
			if (!(m_inserting || rs.next()))
			{
				rs.close();
				pstmt.close();
				fireDataStatusEEvent("SaveErrorRowNotFound", "", true);
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			Object[] rowDataDB = null;
			//	Prepare
			boolean manualUpdate = ResultSet.CONCUR_READ_ONLY == rs.getConcurrency();
			if (DB.isRemoteObjects())
				manualUpdate = true;
			if (manualUpdate)
				createUpdateSqlReset();
			if (m_inserting)
			{
				if (manualUpdate)
					log.fine("Prepare inserting ... manual");
				else
				{
					log.fine("Prepare inserting ... RowSet");
					rs.moveToInsertRow ();
				}
			}
			else
			{
				log.fine("Prepare updating ... manual=" + manualUpdate);
				//	get current Data in DB
				rowDataDB = readData (rs);
			}

			/**	Data:
			 *		m_rowData	= original Data
			 *		rowData 	= updated Data
			 *		rowDataDB	= current Data in DB
			 *	1) Difference between original & updated Data?	N:next
			 *	2) Difference between original & current Data?	Y:don't update
			 *	3) Update current Data
			 *	4) Refresh to get last Data (changed by trigger, ...)
			 */

			//	Constants for Created/Updated(By)
			Timestamp now = new Timestamp(System.currentTimeMillis());
			int user = m_ctx.getAD_User_ID();

			/**
			 *	for every column
			 */
			int size = m_fields.size();
			int colRs = 1;
			for (int col = 0; col < size; col++)
			{
				GridField field = m_fields.get (col);
				if (field.isVirtualColumn())
				{
					if (!m_inserting)
						colRs++;
					continue;
				}
				String columnName = field.getColumnName ();
			//	log.fine(columnName + "= " + m_rowData[col] + " <> DB: " + rowDataDB[col] + " -> " + rowData[col]);

				//	RowID, Virtual Column
				if (field.getDisplayType () == DisplayTypeConstants.RowID
					|| field.isVirtualColumn())
					; //	ignore

				//	New Key
				else if (field.isKey () && m_inserting)
				{
					if (columnName.endsWith ("_ID") || columnName.toUpperCase().endsWith ("_ID"))
					{
						int insertID = DB.getNextID (m_ctx, m_tableName, null);	//	no p_trx
						if (manualUpdate)
							createUpdateSql (columnName, String.valueOf (insertID));
						else
							rs.updateInt (colRs, insertID); 						// ***
						singleRowWHERE.append (columnName).append ("=").append (insertID);
						//
						is = INFO + columnName + " -> " + insertID + " (Key)";
					}
					else //	Key with String value
					{
						String str = rowData[col].toString ();
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (str));
						else
							rs.updateString (colRs, str); 						// ***
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(str));
						//
						is = INFO + columnName + " -> " + str + " (StringKey)";
					}
					log.fine(is);
				}	//	New Key

				//	New DocumentNo
				else if (columnName.equals ("DocumentNo"))
				{
					boolean newDocNo = false;
					String docNo = (String)rowData[col];
					//  we need to have a doc number
					if (docNo == null || docNo.length () == 0)
						newDocNo = true;
						//  Preliminary ID from CalloutSystem
					else if (docNo.startsWith ("<") && docNo.endsWith (">"))
						newDocNo = true;

					if (newDocNo || m_inserting)
					{
						String insertDoc = null;
						//  always overwrite if insering with mandatory DocType DocNo
						if (m_inserting)
							insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo,
								m_tableName, true, null);	//	only doc type - no p_trx
						log.fine("DocumentNo entered=" + docNo + ", DocTypeInsert=" + insertDoc + ", newDocNo=" + newDocNo);
						// can we use entered DocNo?
						if (insertDoc == null || insertDoc.length () == 0)
						{
							if (!newDocNo && docNo != null && docNo.length () > 0)
								insertDoc = docNo;
							else //  get a number from DocType or Table
								insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo,
									m_tableName, false, null);	//	no p_trx
						}
						//	There might not be an automatic document no for this document
						if (insertDoc == null || insertDoc.length () == 0)
						{
							//  in case DB function did not return a value
							if (docNo != null && docNo.length () != 0)
								insertDoc = (String)rowData[col];
							else
							{
								error = true;
								is = ERROR + field.getColumnName () + "= " + rowData[col] + " NO DocumentNo";
								log.fine(is);
								break;
							}
						}
						//
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (insertDoc));
						else
							rs.updateString (colRs, insertDoc);					//	***
							//
						is = INFO + columnName + " -> " + insertDoc + " (DocNo)";
						log.fine(is);
					}
				}	//	New DocumentNo

				//  New Value(key)
				else if (columnName.equals ("Value") && m_inserting)
				{
					String value = (String)rowData[col];
					//  Get from Sequence, if not entered
					if (value == null || value.length () == 0)
					{
						value = DB.getDocumentNo (m_ctx, m_WindowNo, m_tableName, false, null);
						//  No Value
						if (value == null || value.length () == 0)
						{
							error = true;
							is = ERROR + field.getColumnName () + "= " + rowData[col]
								 + " No Value";
							log.fine(is);
							break;
						}
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_STRING (value));
					else
						rs.updateString (colRs, value); 							//	***
						//
					is = INFO + columnName + " -> " + value + " (Value)";
					log.fine(is);
				}	//	New Value(key)

				//	Updated		- check database
				else if (columnName.equals ("Updated"))
				{
					if (m_compareDB && !m_inserting && !m_rowData[col].equals (rowDataDB[col]))	//	changed
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col];
						log.fine(is);
						break;
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (colRs, now); 							//	***
						//
					is = INFO + "Updated/By -> " + now + " - " + user;
					log.fine(is);
				} //	Updated

				//	UpdatedBy	- update
				else if (columnName.equals ("UpdatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (colRs, user); 								//	***
				} //	UpdatedBy

				//	Created
				else if (m_inserting && columnName.equals ("Created"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (colRs, now); 							//	***
				} //	Created

				//	CreatedBy
				else if (m_inserting && columnName.equals ("CreatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (colRs, user); 								//	***
				} //	CreatedBy

				//	Nothing changed & null
				else if (m_rowData[col] == null && rowData[col] == null)
				{
					if (m_inserting)
					{
						if (manualUpdate)
							createUpdateSql (columnName, "NULL");
						else
							rs.updateNull (colRs); 								//	***
						is = INFO + columnName + "= NULL";
						log.fine(is);
					}
				}

				//	***	Data changed ***
				else if (m_inserting
				  || !Util.isEqual(m_rowData[col], rowData[col])) 			//	changed
				{
					//	Original == DB
					if (m_inserting || !m_compareDB
					  || Util.isEqual(m_rowData[col], rowDataDB[col]))
					{
						if (CLogMgt.isLevelFinest())
							log.fine(columnName + "=" + rowData[col]
								+ " " + (rowData[col]==null ? "" : rowData[col].getClass().getName()));
						//
						boolean encrypted = field.isEncryptedColumn();
						//
						String type = "String";
						if (rowData[col] == null)
						{
							if (manualUpdate)
								createUpdateSql (columnName, "NULL");
							else
								rs.updateNull (colRs); 							//	***
						}

						//	ID - int
						else if (FieldType.isID (field.getDisplayType())
							|| field.getDisplayType() == DisplayTypeConstants.Integer)
						{
							try
							{
								Object dd = rowData[col];
								Integer iii = null;
								if (dd instanceof Integer)
									iii = (Integer)dd;
								else
									iii = Integer.valueOf(dd.toString());
								if (encrypted)
									iii = (Integer)encrypt(iii);
								if (manualUpdate)
									createUpdateSql (columnName, String.valueOf (iii));
								else
									rs.updateInt (colRs, iii.intValue()); 		// 	***
							}
							catch (Exception e) //  could also be a String (AD_Language, AD_Message)
							{
								if (manualUpdate)
									createUpdateSql (columnName, DB.TO_STRING (rowData[col].toString ()));
								else
									rs.updateString (colRs, rowData[col].toString ()); //	***
							}
							type = "Int";
						}
						//	Numeric - BigDecimal
						else if (FieldType.isNumeric (field.getDisplayType ()))
						{
							BigDecimal bd = (BigDecimal)rowData[col];
							if (encrypted)
								bd = (BigDecimal)encrypt(bd);
							if (manualUpdate)
								createUpdateSql (columnName, bd.toString ());
							else
								rs.updateBigDecimal (colRs, bd); 				//	***
							type = "Number";
						}
						//	Date - Timestamp
						else if (FieldType.isDate (field.getDisplayType ()))
						{
							Timestamp ts = (Timestamp)rowData[col];
							if (encrypted)
								ts = (Timestamp)encrypt(ts);
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_DATE (ts, false));
							else
								rs.updateTimestamp (colRs, ts); 				//	***
							type = "Date";
						}
						//	LOB
						else if (field.getDisplayType() == DisplayTypeConstants.TextLong)
						{
							PO_LOB lob = new PO_LOB (getTableName(), columnName,
								null, field.getDisplayType(), rowData[col]);
							lobAdd(lob);
							type = "CLOB";
						}
						//	BLOB
						else if (field.getDisplayType() == DisplayTypeConstants.Binary
							||	field.getDisplayType() == DisplayTypeConstants.Image)
						{
							PO_LOB lob = new PO_LOB (getTableName(), columnName,
								null, field.getDisplayType(), rowData[col]);
							lobAdd(lob);
							type = "BLOB";
						}
						//	Boolean
						else if (field.getDisplayType() == DisplayTypeConstants.YesNo)
						{
							String yn = null;
							if (rowData[col] instanceof Boolean)
							{
								Boolean bb = (Boolean)rowData[col];
								yn = bb.booleanValue() ? "Y" : "N";
							}
							else
								yn = "Y".equals(rowData[col]) ? "Y" : "N";
							if (encrypted)
								;
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (yn));
							else
								rs.updateString (colRs, yn); 					//	***
						}
						//	String and others
						else
						{
							String str = rowData[col].toString ();
							if (encrypted)
								str = (String)encrypt(str);
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (str));
							else
								rs.updateString (colRs, str); 					//	***
						}
						//
						is = INFO + columnName + "= " + m_rowData[col]
							 + " -> " + rowData[col] + " (" + type + ")";
						if (encrypted)
							is += " encrypted";
						log.fine(is);
					}
					//	Original != DB
					else
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col] + " -> " + rowData[col];
						log.fine(is);
						Object o1 = m_rowData[col];
						Object o2 = rowDataDB[col];
						boolean eq = o1.equals(o2);
						log.fine((o1 == o2) + "  " + eq);
					}
				}	//	Data changed

				//	Single Key - retrieval sql
				if (field.isKey() && !m_inserting)
				{
					if (rowData[col] == null)
						throw new RuntimeException("Key is NULL - " + columnName);
					if (columnName.endsWith ("_ID"))
						singleRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
					{
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
					}
				}
				//	MultiKey Inserting - retrieval sql
				if (field.isParentColumn())
				{
					if (rowData[col] == null)
						throw new RuntimeException("MultiKey Parent is NULL - " + columnName);
					if (multiRowWHERE.length() != 0)
						multiRowWHERE.append(" AND ");
					if (columnName.endsWith ("_ID"))
						multiRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
						multiRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
				}
				//
				colRs++;
			}	//	for every column

			if (error)
			{
				if (manualUpdate)
					createUpdateSqlReset();
				else
					rs.cancelRowUpdates();
				rs.close();
				pstmt.close();
				fireDataStatusEEvent("SaveErrorDataChanged", "", true);
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			/**
			 *	Save to Database
			 */
			//
			String whereClause = singleRowWHERE.toString();
			if (whereClause.length() == 0)
				whereClause = multiRowWHERE.toString();
			if (m_inserting)
			{
				log.fine("Inserting ...");
				if (manualUpdate)
				{
					String sql = createUpdateSql(true, null);
					int no = DB.executeUpdateEx (sql, null);	//	no Trx
					if (no != 1)
						log.log(Level.SEVERE, "Insert #=" + no + " - " + sql);
				}
				else
					rs.insertRow();
			}
			else
			{
				log.fine("Updating ... " + whereClause);
				if (manualUpdate)
				{
					String sql = createUpdateSql(false, whereClause);
					int no = DB.executeUpdateEx (sql, null);	//	no Trx
					if (no != 1)
						log.log(Level.SEVERE, "Update #=" + no + " - " + sql);
				}
				else
					rs.updateRow();
			}

			log.fine("Committing ...");
			//DB.commit(true, null);	//	no Trx
			//
			lobSave(whereClause);
			rs.close();
			pstmt.close();

			//	Need to re-read row to get ROWID, Key, DocumentNo, Trigger, virtual columns
			log.fine("Reading ... " + whereClause);
			StringBuffer refreshSQL = new StringBuffer(m_SQL_Select)
				.append(" WHERE ").append(whereClause);
			pstmt = DB.prepareStatement(refreshSQL.toString(), (Trx) null);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				rowDataDB = readData(rs);
				//	update buffer
				m_buffer.set(sort.index, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			else
				log.log(Level.SEVERE, "Inserted row not found");
			//
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			try
			{
				if (pstmt != null)
				  pstmt.close ();
				pstmt = null;
			}
			catch (Exception ex)
			{
			}

			String msg = "SaveError";
			if (e.getErrorCode() == 1)		//	Unique Constraint
			{
				log.log(Level.WARNING, "Key Not Unique", e);
				msg = "SaveErrorNotUnique";
			}
			else
				log.log(Level.SEVERE, select.toString(), e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage(), true);
			return SAVE_ERROR;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		fireDataStatusIEvent("Saved", "");
		//
		log.info("fini");
		return SAVE_OK;
	}	//	dataSave

	/**
	 * 	Save via PO
	 *	@param Record_ID
	 *	@return SAVE_ERROR or SAVE_OK
	 *	@throws Exception
	 */
	private char dataSavePO (int Record_ID) throws Exception
	{
		log.fine("ID=" + Record_ID);
		//
		MSort sort = m_sort.get(m_rowChanged);
		Object[] rowData = m_buffer.get(sort.index);
		//
		MTable table = MTable.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		if (table.isSingleKey() || Record_ID == 0)
			po = table.getPO(m_ctx, Record_ID, null);
		else	//	Multi - Key
			po = table.getPO(m_ctx, getWhereClause(rowData), null);
		//	No Persistent Object
		if (po == null)
			throw new ClassNotFoundException ("No Persistent Object");

		int size = m_fields.size();
		for (int col = 0; col < size; col++)
		{
			GridField field = m_fields.get (col);
			if (field.isVirtualColumn())
				continue;
			String columnName = field.getColumnName ();
			Object value = rowData[col];
			Object oldValue = m_rowData[col];
			//	RowID
			if (field.getDisplayType() == DisplayTypeConstants.RowID)
				; 	//	ignore

			//	Nothing changed & null
			else if (oldValue == null && value == null)
				;	//	ignore

			//	***	Data changed ***
			else if (m_inserting
			  || !Util.isEqual(oldValue, value)) 			//	changed
			{
				//	Check existence
				int poIndex = po.get_ColumnIndex(columnName);
				if (poIndex < 0)
				{
					//	Custom Fields not in PO
					po.set_CustomColumn(columnName, value);
				//	log.log(Level.SEVERE, "Column not found: " + columnName);
					continue;
				}

				Object dbValue = po.get_Value(poIndex);
				if (m_inserting
					|| !m_compareDB
					//	Original == DB
					|| Util.isEqual(oldValue, dbValue)
					//	Target == DB (changed by trigger to new value already)
					|| Util.isEqual(value, dbValue))
				{
					if (!po.set_ValueNoCheck (columnName, value))
					{
						fireDataStatusEEvent("ValidationError", columnName, true);
						dataRefresh(m_rowChanged);
						return SAVE_ERROR;
					}
				}
				//	Original != DB
				else
				{
					String msg = columnName
						+ "= " + oldValue
							+ (oldValue==null ? "" : "(" + oldValue.getClass().getName() + ")")
						+ " != DB: " + dbValue
							+ (dbValue==null ? "" : "(" + dbValue.getClass().getName() + ")")
						+ " -> New: " + value
							+ (value==null ? "" : "(" + value.getClass().getName() + ")");
				//	CLogMgt.setLevel(Level.FINEST);
				//	po.dump();
					fireDataStatusEEvent("SaveErrorDataChanged", msg, true);
					dataRefresh(m_rowChanged);
					return SAVE_ERROR;
				}
			}	//	Data changed

		}	//	for every column

		if (!po.save())
		{
			String msg = "SaveError";
			String info = "";
			ValueNamePair ppE = CLogger.retrieveError();
			if (ppE == null)
				ppE = CLogger.retrieveWarning();
			if (ppE != null)
			{
				msg = ppE.getValue();
				info = ppE.getName();
				//	Unique Constraint
				Exception ex = CLogger.retrieveException();
				if (ex != null
					&& ex instanceof SQLException
					&& ((SQLException)ex).getErrorCode() == 1)
					msg = "SaveErrorNotUnique";
			}
			fireDataStatusEEvent(msg, info, true);
			return SAVE_ERROR;
		}

		//	Refresh - update buffer
		String whereClause = po.get_WhereClause(true);
		log.fine("Reading ... " + whereClause);
		StringBuffer refreshSQL = new StringBuffer(m_SQL_Select)
			.append(" WHERE ").append(whereClause);
		PreparedStatement pstmt = DB.prepareStatement(refreshSQL.toString(), (Trx) null);
		try
		{
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				Object[] rowDataDB = readData(rs);
				//	update buffer
				m_buffer.set(sort.index, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			try
			{
				if (pstmt != null)
				  pstmt.close ();
				pstmt = null;
			}
			catch (Exception ex)
			{
			}

			String msg = "SaveError";
			log.log(Level.SEVERE, refreshSQL.toString(), e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage(), true);
			return SAVE_ERROR;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		//
		ValueNamePair pp = CLogger.retrieveWarning();
		if (pp != null)
		{
			String msg = pp.getValue();
			String info = pp.getName();
			fireDataStatusEEvent(msg, info, false);
		}
		else
		{
			pp = CLogger.retrieveInfo();
			String msg = "Saved";
			String info = "";
			if (pp != null)
			{
				msg = pp.getValue();
				info = pp.getName();
			}
			fireDataStatusIEvent(msg, info);
		}
		//
		log.config("fini");
		return SAVE_OK;
	}	//	dataSavePO

	/**
	 * 	Get Record Where Clause from data (single key or multi-parent)
	 *	@param rowData data
	 *	@return where clause or null
	 */
	private String getWhereClause (Object[] rowData)
	{
		int size = m_fields.size();
		StringBuffer singleRowWHERE = null;
		StringBuffer multiRowWHERE = null;
		for (int col = 0; col < size; col++)
		{
			GridField field = m_fields.get (col);
			if (field.isKey())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col];
				if (value == null)
				{
					log.log(Level.WARNING, "PK data is null - " + columnName);
					return null;
				}
				if (columnName.endsWith ("_ID"))
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (value);
				else
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
			else if (field.isParentColumn())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col];
				if (value == null)
				{
					log.log(Level.INFO, "FK data is null - " + columnName);
					continue;
				}
				if (multiRowWHERE == null)
					multiRowWHERE = new StringBuffer();
				else
					multiRowWHERE.append(" AND ");
				if (columnName.endsWith ("_ID"))
					multiRowWHERE.append (columnName)
						.append ("=").append (value);
				else
					multiRowWHERE.append (columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
		}	//	for all columns
		if (singleRowWHERE != null)
			return singleRowWHERE.toString();
		if (multiRowWHERE != null)
			return multiRowWHERE.toString();
		log.log(Level.WARNING, "No key Found");
		return null;
	}	//	getWhereClause

	/*************************************************************************/

	private ArrayList<String>	m_createSqlColumn = new ArrayList<String>();
	private ArrayList<String>	m_createSqlValue = new ArrayList<String>();

	/**
	 * 	Prepare SQL creation
	 * 	@param columnName column name
	 * 	@param value value
	 */
	private void createUpdateSql (String columnName, String value)
	{
		m_createSqlColumn.add(columnName);
		m_createSqlValue.add(value);
		log.finest("#" + m_createSqlColumn.size()
				+ " - " + columnName + "=" + value);
	}	//	createUpdateSQL

	/**
	 * 	Create update/insert SQL
	 * 	@param insert true if insert - update otherwise
	 * 	@param whereClause where clause for update
	 * 	@return sql statement
	 */
	private String createUpdateSql (boolean insert, String whereClause)
	{
		StringBuffer sb = new StringBuffer();
		if (insert)
		{
			sb.append("INSERT INTO ").append(m_tableName).append(" (");
			for (int i = 0; i < m_createSqlColumn.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlColumn.get(i));
			}
			sb.append(") VALUES ( ");
			for (int i = 0; i < m_createSqlValue.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlValue.get(i));
			}
			sb.append(")");
		}
		else
		{
			sb.append("UPDATE ").append(m_tableName).append(" SET ");
			for (int i = 0; i < m_createSqlColumn.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlColumn.get(i)).append("=").append(m_createSqlValue.get(i));
			}
			sb.append(" WHERE ").append(whereClause);
		}
		log.fine(sb.toString());
		//	reset
		createUpdateSqlReset();
		return sb.toString();
	}	//	createUpdateSql

	/**
	 * 	Reset Update Data
	 */
	private void createUpdateSqlReset()
	{
		m_createSqlColumn = new ArrayList<String>();
		m_createSqlValue = new ArrayList<String>();
	}	//	createUpdateSqlReset

	/**
	 *	Get Mandatory empty columns
	 *  @param rowData row data
	 *  @return String with missing column headers/labels
	 */
	private String getMandatory(Object[] rowData)
	{
		//  see also => ProcessParameter.saveParameter
		StringBuffer sb = new StringBuffer();

		//	Check all columns
		int size = m_fields.size();
		for (int i = 0; i < size; i++)
		{
			GridField field = m_fields.get(i);
			if (field.isMandatory(true))        //  check context
			{
				if (rowData[i] == null || rowData[i].toString().length() == 0)
				{
					field.setInserting (true);  //  set editable otherwise deadlock
					field.setError(true);
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(field.getHeader());
				}
			}
		}

		if (sb.length() == 0)
			return "";
		return sb.toString();
	}	//	getMandatory


	/**
	 *	Get columns in Error status
	 *  @return String with missing column headers/labels
	 */
	private String getErrorColumns()
	{
		//  see also => ProcessParameter.saveParameter
		StringBuffer sb = new StringBuffer();

		//	Check all columns
		int size = m_fields.size();
		for (int i = 0; i < size; i++)
		{
			GridField field = m_fields.get(i);
			if (field.isError())        //  check context
			{
				if (sb.length() > 0)
					sb.append(", ");
				sb.append(field.getHeader());
			}
		}

		if (sb.length() == 0)
			return "";
		return sb.toString();
	}	//	getErrorColumns


	/*************************************************************************/

	/**	LOB Info				*/
	private ArrayList<PO_LOB>	m_lobInfo = null;

	/**
	 * 	Reset LOB info
	 */
	private void lobReset()
	{
		m_lobInfo = null;
	}	//	resetLOB

	/**
	 * 	Prepare LOB save
	 *	@param lob value
	 */
	private void lobAdd (PO_LOB lob)
	{
		log.fine("LOB=" + lob);
		if (m_lobInfo == null)
			m_lobInfo = new ArrayList<PO_LOB>();
		m_lobInfo.add(lob);
	}	//	lobAdd

	/**
	 * 	Save LOB
	 * 	@param whereClause where clause
	 */
	private void lobSave (String whereClause)
	{
		if (m_lobInfo == null)
			return;
		for (int i = 0; i < m_lobInfo.size(); i++)
		{
			PO_LOB lob = m_lobInfo.get(i);
			lob.save(whereClause, null);		//	no p_trx
		}	//	for all LOBs
		lobReset();
	}	//	lobSave


	/**************************************************************************
	 *	New Record after current Row
	 *  @param currentRow row
	 *  @param copyCurrent copy
	 *  @return true if success -
	 *  Error info (Access*, AccessCannotInsert) is saved in the log
	 */
	public boolean dataNew (int currentRow, boolean copyCurrent)
	{
		log.info("Current=" + currentRow + ", Copy=" + copyCurrent);
		//  Read only
		if (m_readOnly)
		{
			fireDataStatusEEvent("AccessCannotInsert", "", true);
			return false;
		}

		/** @todo No TableLevel */
		//  || !Access.canViewInsert(m_ctx, m_WindowNo, tableLevel, true, true))
		//  fireDataStatusEvent(Log.retrieveError());

		//  see if we need to save
		dataSave(-2, false);


		m_inserting = true;
		//	Create default data
		int size = m_fields.size();
		m_rowData = new Object[size];	//	"original" data
		Object[] rowData = new Object[size];
		int tempWindowNo = m_WindowNo + EnvConstants.WINDOW_TEMP;
		//	fill data
		if (copyCurrent)
		{
			MSort sort = m_sort.get(currentRow);
			Object[] origData = m_buffer.get(sort.index);
			for (int i = 0; i < size; i++)
			{
				GridField field = m_fields.get(i);
				String columnName = field.getColumnName();

				if (field.isVirtualColumn())
					;
				else if (field.isKey()
					|| columnName.equals("AD_Client_ID")
					//
					|| columnName.startsWith("Created") || columnName.startsWith("Updated")
					|| columnName.equals("EntityType") || columnName.equals("DocumentNo")
					|| columnName.equals("Processed") || columnName.equals("IsSelfService")
					|| columnName.equals("DocAction") || columnName.equals("DocStatus")
					|| columnName.startsWith("Ref_")
					|| columnName.equals("Posted")
					//	Order/Invoice
					|| columnName.equals("GrandTotal") || columnName.equals("TotalLines")
					|| columnName.equals("C_CashLine_ID") || columnName.equals("C_Payment_ID")
					|| columnName.equals("IsPaid") || columnName.equals("IsAllocated")
					|| columnName.equalsIgnoreCase("C_Location_ID")
				)
				{
					Object oo = field.getDefault(m_ctx, tempWindowNo);
					rowData[i] = oo;
					if (oo != null)
						m_ctx.setContext(tempWindowNo, field.getColumnName(), oo.toString());
				}
				else
				{
					Object oo = origData[i];
					rowData[i] = oo;
					if (oo != null)
						m_ctx.setContext(tempWindowNo, field.getColumnName(), oo.toString());
				}
			}
		}
		else	//	new
		{
			for (int i = 0; i < size; i++)
			{
				GridField field = m_fields.get(i);
				Object oo = field.getDefault(m_ctx, tempWindowNo);
				rowData[i] = oo;
				if (oo != null)
					m_ctx.setContext(tempWindowNo, field.getColumnName(), oo.toString());
			}
		}
		m_ctx.removeWindow(tempWindowNo);
		m_changed = true;
		m_compareDB = true;
		m_rowChanged = -1;  //  only changed in setValueAt
		m_newRow = currentRow + 1;
		//  if there is no record, the current row could be 0 (and not -1)
		if (m_buffer.size() < m_newRow)
			m_newRow = m_buffer.size();

		//	add Data at end of buffer
		MSort sort = new MSort(m_buffer.size(), null);	//	index
		m_buffer.add(rowData);
		//	add Sort pointer
		m_sort.add(m_newRow, sort);
		m_rowCount++;

		//	inform
		log.finer("Current=" + currentRow + ", New=" + m_newRow);
		fireTableRowsInserted(m_newRow, m_newRow);
		fireDataStatusIEvent(copyCurrent ? "UpdateCopied" : "Inserted", "");
		log.fine("Current=" + currentRow + ", New=" + m_newRow + " - complete");
		return true;
	}	//	dataNew


	/**************************************************************************
	 *	Delete Data
	 *  @param row row
	 *  @return true if success -
	 *  Error info (Access*, AccessNotDeleteable, DeleteErrorDependent,
	 *  DeleteError) is saved in the log
	 */
	public boolean dataDelete (int row)
	{
		log.info("Row=" + row);
		if (row < 0)
			return false;

		//	Tab R/O
		if (m_readOnly)
		{
			fireDataStatusEEvent("AccessCannotDelete", "", true);	//	privileges
			return false;
		}

		//	Is this record deletable?
		if (!m_deleteable)
		{
			fireDataStatusEEvent("AccessNotDeleteable", "", true);	//	audit
			return false;
		}

		//	Processed Column and not an Import Table
		if (m_indexProcessedColumn > 0 && !m_tableName.startsWith("I_"))
		{
			Boolean processed = (Boolean)getValueAt(row, m_indexProcessedColumn);
			if (processed != null && processed.booleanValue())
			{
				fireDataStatusEEvent("CannotDeleteTrx", "", true);
				return false;
			}
		}

		/** @todo check Access */
		//  fireDataStatusEvent(Log.retrieveError());

		MSort sort = m_sort.get(row);
		Object[] rowData = m_buffer.get(sort.index);
		//
		MTable table = MTable.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		int Record_ID = getKeyID(m_rowChanged);
		Trx p_trx = Trx.get("GridDel");
		if (Record_ID != -1)
			po = table.getPO(m_ctx, Record_ID, p_trx);
		else	//	Multi - Key
			po = table.getPO(m_ctx, getWhereClause(rowData), p_trx);

		//	Delete via PO
		if (po != null)
		{
			boolean ok = false;
			try
			{
				ok = po.delete(false);
			}
			catch (Throwable t)
			{
				log.log(Level.SEVERE, "Delete", t);
			}
			if (!ok)
			{
				p_trx.rollback();
				ValueNamePair vp = CLogger.retrieveError();
				if (vp != null)
					fireDataStatusEEvent(vp);
				else
					fireDataStatusEEvent("DeleteError", "", true);
				return false;
			}
			else
				p_trx.commit();
		}
		else	//	Delete via SQL
		{
			StringBuffer sql = new StringBuffer("DELETE FROM ");
			sql.append(m_tableName).append(" WHERE ").append(getWhereClause(rowData));
			int no = 0;
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(sql.toString(), (Trx) null);
				no = pstmt.executeUpdate();
				pstmt.close();
			}
			catch (SQLException e)
			{
				log.log(Level.SEVERE, sql.toString(), e);
				String msg = "DeleteError";
				if (e.getErrorCode() == 2292)	//	Child Record Found
					msg = "DeleteErrorDependent";
				fireDataStatusEEvent(msg, e.getLocalizedMessage(), true);
				return false;
			}
			//	Check Result
			if (no != 1)
			{
				log.log(Level.SEVERE, "Number of deleted rows = " + no);
				return false;
			}
		}

		p_trx.close();
		p_trx = null;


		//	Get Sort
		int bufferRow = sort.index;
		//	Delete row in Buffer and shifts all below up
		m_buffer.remove(bufferRow);
		m_rowCount--;

		//	Delete row in Sort
		m_sort.remove(row);
		//	Correct pointer in Sort
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort ptr = m_sort.get(i);
			if (ptr.index > bufferRow)
				ptr.index--;	//	move up
		}

		//	inform
		m_changed = false;
		m_rowChanged = -1;
		fireTableRowsDeleted(row, row);
		fireDataStatusIEvent("Deleted", "");
		log.fine("Row=" + row + " complete");
		return true;
	}	//	dataDelete


	/**************************************************************************
	 *	Ignore changes
	 */
	public void dataIgnore()
	{
		if (!m_inserting && !m_changed && m_rowChanged < 0)
		{
			log.fine("Nothing to ignore");
			return;
		}
		log.info("Inserting=" + m_inserting);

		//	Inserting - delete new row
		if (m_inserting)
		{
			//	Get Sort
			MSort sort = m_sort.get(m_newRow);
			int bufferRow = sort.index;
			//	Delete row in Buffer and shifts all below up
			m_buffer.remove(bufferRow);
			m_rowCount--;
			//	Delete row in Sort
			m_sort.remove(m_newRow);	//	pintint to the last column, so no adjustment
			//
			m_changed = false;
			m_rowData = null;
			m_rowChanged = -1;
			m_inserting = false;
			//	inform
			fireTableRowsDeleted(m_newRow, m_newRow);
		}
		else
		{
			//	update buffer
			if (m_rowData != null)
			{
				MSort sort = m_sort.get(m_rowChanged);
				m_buffer.set(sort.index, m_rowData);
			}
			m_changed = false;
			m_rowData = null;
			m_rowChanged = -1;
			m_inserting = false;
			//	inform
		//	fireTableRowsUpdated(m_rowChanged, m_rowChanged); >> messes up display?? (clearSelection)
		}
		m_newRow = -1;
		fireDataStatusIEvent("Ignored", "");
	}	//	dataIgnore


	/**
	 *	Refresh Row - ignore changes
	 *  @param row row
	 */
	public void dataRefresh (int row)
	{
		log.info("Row=" + row);

		if (row < 0 || m_sort.size() == 0 || m_inserting)
			return;

		MSort sort = m_sort.get(row);
		Object[] rowData = m_buffer.get(sort.index);

		//  ignore
		dataIgnore();

		//	Create SQL
		String where = getWhereClause(rowData);
		if (where == null || where.length() == 0)
			where = "1=2";
		String sql = m_SQL_Select + " WHERE " + where;
		sort = m_sort.get(row);
		Object[] rowDataDB = null;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, (Trx) null);
			ResultSet rs = pstmt.executeQuery();
			//	only one row
			if (rs.next())
				rowDataDB = readData(rs);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql, e);
			fireTableRowsUpdated(row, row);
			fireDataStatusEEvent("RefreshError", sql, true);
			return;
		}

		//	update buffer
		m_buffer.set(sort.index, rowDataDB);
		//	info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableRowsUpdated(row, row);
		fireDataStatusIEvent("Refreshed", "");
	}	//	dataRefresh


	/**
	 *	Refresh all Rows - ignore changes
	 */
	public void dataRefreshAll()
	{
		log.info("");
		m_inserting = false;	//	should not happen
		dataIgnore();
		close(false);
		open(m_maxRows);
		//	Info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableDataChanged();
		fireDataStatusIEvent("Refreshed", "");
	}	//	dataRefreshAll


	/**
	 *	Requery with new whereClause
	 *  @param whereClause sql where clause
	 *  @param onlyCurrentRows only current rows
	 *  @param onlyCurrentDays how many days back
	 *  @return true if success
	 */
	public boolean dataRequery (String whereClause)
	{
		log.info(whereClause);
		close(false);
		setSelectWhereClause(whereClause);
		boolean success = open(m_maxRows);
		//  Info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableDataChanged();
		fireDataStatusIEvent("Refreshed", "");
		return success;
	}	//	dataRequery


	/**************************************************************************
	 *	Is Cell Editable.
	 *	Is queried from JTable before checking VCellEditor.isCellEditable
	 *  @param  row the row index being queried
	 *  @param  col the column index being queried
	 *  @return true, if editable
	 */
	@Override
	public boolean isCellEditable (int row, int col)
	{
	//	log.fine( "MTable.isCellEditable - Row=" + row + ", Col=" + col);
		//	Make Rows selectable
	//	if (col == 0)
	//		return true;

		//	Entire Table not editable
		if (m_readOnly)
			return false;
		//	Key not editable
		if (col == m_indexKeyColumn)
			return false;
		/** @todo check link columns */

		//	Check column range
		if (col < 0 && col >= m_fields.size())
			return false;
		//  IsActive Column always editable if no processed exists
		if (col == m_indexActiveColumn && m_indexProcessedColumn == -1)
			return true;
		//	Row
		if (!isRowEditable(row))
			return false;

		//	Column
		return m_fields.get(col).isEditable(false);
	}	//	IsCellEditable


	/**
	 *	Is Current Row Editable
	 *  @param row row
	 *  @return true if editable
	 */
	public boolean isRowEditable (int row)
	{
	//	log.fine( "MTable.isRowEditable - Row=" + row);
		//	Entire Table not editable or no row
		if (m_readOnly || row < 0)
			return false;
		//	If not Active - not editable
		if (m_indexActiveColumn > 0)		//	&& m_TabNo != Find.s_TabNo)
		{
			Object value = getValueAt(row, m_indexActiveColumn);
			if (value instanceof Boolean)
			{
				if (!((Boolean)value).booleanValue())
					return false;
			}
			else if ("N".equals(value))
				return false;
		}
		//	If Processed - not editable (Find always editable)
		if (m_indexProcessedColumn > 0)		//	&& m_TabNo != Find.s_TabNo)
		{
			Object processed = getValueAt(row, m_indexProcessedColumn);
			if (processed instanceof Boolean)
			{
				if (((Boolean)processed).booleanValue())
					return false;
			}
			else if ("Y".equals(processed))
				return false;
		}
		//
		int[] co = getClientOrg(row);
		int AD_Client_ID = co[0];
		int AD_Org_ID = co[1];
		int Record_ID = getKeyID(row);
		return MRole.getDefault(m_ctx, false).canUpdate
			(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, Record_ID, false);
	}	//	isRowEditable

	/**
	 * 	Get Client Org for row
	 *	@param row row
	 *	@return array [0] = Client [1] = Org - a value of -1 is not defined/found
	 */
	private int[] getClientOrg (int row)
	{
		int AD_Client_ID = -1;
		if (m_indexClientColumn != -1)
		{
			Integer ii = (Integer)getValueAt(row, m_indexClientColumn);
			if (ii != null)
				AD_Client_ID = ii.intValue();
		}
		int AD_Org_ID = 0;
		if (m_indexOrgColumn != -1)
		{
			Integer ii = (Integer)getValueAt(row, m_indexOrgColumn);
			if (ii != null)
				AD_Org_ID = ii.intValue();
		}
		return new int[] {AD_Client_ID, AD_Org_ID};
	}	//	getClientOrg

	/**
	 *	Set entire table as read only
	 *  @param value new read only value
	 */
	public void setReadOnly (boolean value)
	{
		log.fine("ReadOnly=" + value);
		m_readOnly = value;
	}	//	setReadOnly

	/**
	 *  Is entire Table Read/Only
	 *  @return true if read only
	 */
	public boolean isReadOnly()
	{
		return m_readOnly;
	}   //  isReadOnly

	/**
	 *  Is inserting
	 *  @return true if inserting
	 */
	public boolean isInserting()
	{
		return m_inserting;
	}   //  isInserting

	/**
	 *	Set Compare DB.
	 * 	If Set to false, save overwrites the record, regardless of DB changes.
	 *  (When a payment is changed in Sales Order, the payment reversal clears the payment id)
	 * 	@param compareDB compare DB - false forces overwrite
	 */
	public void setCompareDB (boolean compareDB)
	{
		m_compareDB = compareDB;
	}  	//	setCompareDB

	/**
	 *	Get Compare DB.
	 * 	@return false if save overwrites the record, regardless of DB changes
	 * 	(false forces overwrite).
	 */
	public boolean getCompareDB ()
	{
		return m_compareDB;
	}  	//	getCompareDB


	/**
	 *	Can Table rows be deleted
	 *  @param value new deleteable value
	 */
	public void setDeleteable (boolean value)
	{
		log.fine("Deleteable=" + value);
		m_deleteable = value;
	}	//	setDeleteable


	/**************************************************************************
	 *	Read Data from Recordset
	 *  @param rs result set
	 *  @return Data Array
	 */
	Object[] readData (ResultSet rs)
	{
		int size = m_fields.size();
		Object[] rowData = new Object[size];
		String columnName = null;
		int displayType = 0;

		//	Types see also MField.createDefault
		try
		{
			//	get row data
			for (int j = 0; j < size; j++)
			{
				//	Column Info
				GridField field = m_fields.get(j);
				columnName = field.getColumnName();
				displayType = field.getDisplayType();
				//	Integer, ID, Lookup (UpdatedBy is a numeric column)
				if (displayType == DisplayTypeConstants.Integer
					|| FieldType.isID(displayType) // JJ: don't touch!
						&& (columnName.endsWith("_ID") || columnName.endsWith("_Acct"))
					|| columnName.endsWith("atedBy"))
				{
					rowData[j] = Integer.valueOf(rs.getInt(j+1));	//	Integer
					if (rs.wasNull())
						rowData[j] = null;
				}
				//	Number
				else if (FieldType.isNumeric(displayType))
					rowData[j] = rs.getBigDecimal(j+1);			//	BigDecimal
				//	Date
				else if (FieldType.isDate(displayType))
					rowData[j] = rs.getTimestamp(j+1);			//	Timestamp
				//	RowID or Key (and Selection)
				else if (displayType == DisplayTypeConstants.RowID)
					rowData[j] = null;
				//	YesNo
				else if (displayType == DisplayTypeConstants.YesNo)
				{
					String str = rs.getString(j+1);
					if (field.isEncryptedColumn())
						str = (String)decrypt(str);
					rowData[j] = Boolean.valueOf ("Y".equals(str));	//	Boolean
				}
				//	LOB
				else if (FieldType.isLOB(displayType))
				{
					Object value = rs.getObject(j+1);
					if (rs.wasNull())
						rowData[j] = null;
					else if (value instanceof Clob)
					{
						Clob lob = (Clob)value;
						long length = lob.length();
						rowData[j] = lob.getSubString(1, (int)length);
					}
					else if (value instanceof Blob)
					{
						Blob lob = (Blob)value;
						long length = lob.length();
						rowData[j] = lob.getBytes(1, (int)length);
					}
					// For EnterpriseDB (Compiere Type Long Text is stored as Text in EDB)
					else if (value instanceof java.lang.String) {
						rowData[j] = value.toString();
					}

				}
				//	String
				else
					rowData[j] = rs.getString(j+1);				//	String
				//	Encrypted
				if (field.isEncryptedColumn() && displayType != DisplayTypeConstants.YesNo)
					rowData[j] = decrypt(rowData[j]);
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, columnName + ", DT=" + displayType, e);
		}
		return rowData;
	}	//	readData

	/**
	 *	Encrypt
	 *	@param xx clear data
	 *	@return encrypted value
	 */
	private Object encrypt (Object xx)
	{
		if (xx == null)
			return null;
		return SecureEngine.encrypt(xx);
	}	//	encrypt

	/**
	 * 	Decrypt
	 *	@param yy encrypted data
	 *	@return clear data
	 */
	private Object decrypt (Object yy)
	{
		if (yy == null)
			return null;
		return SecureEngine.decrypt(yy);
	}	//	decrypt

	/**************************************************************************
	 *	Remove Data Status Listener
	 *  @param l listener
	 */
	public synchronized void removeDataStatusListener(DataStatusListener l)
	{
		listenerList.remove(DataStatusListener.class, l);
	}	//	removeDataStatusListener

	/**
	 *	Add Data Status Listener
	 *  @param l listener
	 */
	public synchronized void addDataStatusListener(DataStatusListener l)
	{
		listenerList.add(DataStatusListener.class, l);
	}	//	addDataStatusListener

	/**
	 *	Inform Listeners
	 *  @param e event
	 */
	void fireDataStatusChanged (DataStatusEvent e)
	{
		DataStatusListener[] listeners = listenerList.getListeners(DataStatusListener.class);
        for (DataStatusListener element : listeners)
			element.dataStatusChanged(e);
	}	//	fireDataStatusChanged

	/**
	 *  Create Data Status Event
	 *  @return data status event
	 */
	DataStatusEvent createDSE()
	{
		boolean changed = m_changed;
		if (m_rowChanged != -1)
			changed = true;
		DataStatusEvent dse = new DataStatusEvent(this, m_rowCount, changed,
			m_ctx.isAutoCommit(m_WindowNo), m_inserting);
		dse.AD_Table_ID = m_AD_Table_ID;
		dse.Record_ID = null;
		return dse;
	}   //  createDSE

	/**
	 *  Create and fire Data Status Info Event
	 *  @param AD_Message message
	 *  @param info additional info
	 */
	protected void fireDataStatusIEvent (String AD_Message, String info)
	{
		DataStatusEvent e = createDSE();
		e.setInfo(AD_Message, info, false,false);
		fireDataStatusChanged (e);
	}   //  fireDataStatusEvent

	/**
	 *  Create and fire Data Status Error Event
	 *  @param AD_Message message
	 *  @param info info
	 *  @param isError error
	 */
	protected void fireDataStatusEEvent (String AD_Message, String info, boolean isError)
	{
	//	org.compiere.util.Trace.printStack();
		//
		DataStatusEvent e = createDSE();
		e.setInfo(AD_Message, info, isError, !isError);
		if (isError)
			log.saveWarning(AD_Message, info);
		fireDataStatusChanged (e);
	}   //  fireDataStatusEvent

	/**
	 *  Create and fire Data Status Event (from Error Log)
	 *  @param errorLog error log info
	 */
	protected void fireDataStatusEEvent (ValueNamePair errorLog)
	{
		if (errorLog != null)
			fireDataStatusEEvent (errorLog.getValue(), errorLog.getName(), true);
	}   //  fireDataStatusEvent


	/**************************************************************************
	 *  Remove Vetoable change listener for row changes
	 *  @param l listener
	 */
	public synchronized void removeVetoableChangeListener(VetoableChangeListener l)
	{
		m_vetoableChangeSupport.removeVetoableChangeListener(l);
	}   //  removeVetoableChangeListener

	/**
	 *  Add Vetoable change listener for row changes
	 *  @param l listener
	 */
	public synchronized void addVetoableChangeListener(VetoableChangeListener l)
	{
		m_vetoableChangeSupport.addVetoableChangeListener(l);
	}   //  addVetoableChangeListener

	/**
	 *  Fire Vetoable change listener for row changes
	 *  @param e event
	 *  @throws PropertyVetoException
	 */
	protected void fireVetoableChange(PropertyChangeEvent e) throws java.beans.PropertyVetoException
	{
		m_vetoableChangeSupport.fireVetoableChange(e);
	}   //  fireVetoableChange

	/**
	 *  toString
	 *  @return String representation
	 */
	@Override
	public String toString()
	{
		return new StringBuffer("MTable[").append(m_tableName)
			.append(",WindowNo=").append(m_WindowNo)
			.append(",Tab=").append(m_TabNo).append("]").toString();
	}   //  toString

	/**************************************************************************
	 *	ASync Loader
	 */
	class Loader extends Thread implements Serializable
	{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		/**
		 *  Construct Loader
		 */
		public Loader()
		{
			super("TLoader");
		}	//	Loader

		private PreparedStatement   m_pstmt = null;
		private ResultSet 		    m_rs = null;

		/**
		 *	Open ResultSet
		 *	@param maxRows maximum number of rows or 0 for all
		 *	@return number of records
		 */
		protected int open (int maxRows)
		{
		//	log.config( "MTable Loader.open");
			//	Get Number of Rows
			int rows = 0;
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(m_SQL_Count, (Trx) null);
				setParameter (pstmt, true);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					rows = rs.getInt(1);
				rs.close();
				pstmt.close();
			}
			catch (SQLException e0)
			{
				//	Zoom Query may have invalid where clause
				if (e0.getErrorCode() == 904) 	//	ORA-00904: "C_x_ID": invalid identifier
					log.warning("Count - " + e0.getLocalizedMessage() + "\nSQL=" + m_SQL_Count);
				else
					log.log(Level.SEVERE, "Count SQL=" + m_SQL_Count, e0);
				return 0;
			}
			StringBuffer info = new StringBuffer("Rows=");
			info.append(rows);
			if (rows == 0)
				info.append(" - ").append(m_SQL_Count);

			//	open Statement (closed by Loader.close)
			try
			{
				m_pstmt = DB.prepareStatement(m_SQL, (Trx) null);
				if (maxRows > 0 && rows > maxRows)
				{
					m_pstmt.setMaxRows(maxRows);
					info.append(" - MaxRows=").append(maxRows);
					rows = maxRows;
				}
			//	m_pstmt.setFetchSize(20);
				setParameter (m_pstmt, false);
				m_rs = m_pstmt.executeQuery();
			}
			catch (SQLException e)
			{
				log.log(Level.SEVERE, m_SQL, e);
				return 0;
			}
			log.fine(info.toString());
			return rows;
		}	//	open

		/**
		 *	Close RS and Statement
		 */
		void close()
		{
		//	log.config( "MTable Loader.close");
			try
			{
				if (m_rs != null)
					m_rs.close();
				if (m_pstmt != null)
					m_pstmt.close();
			}
			catch (SQLException e)
			{
				log.log(Level.SEVERE, "closeRS", e);
			}
			m_rs = null;
			m_pstmt = null;
		}	//	close

		/**
		 *	Fill Buffer to include Row
		 */
		@Override
		public void run()
		{
			log.info("");
			if (m_rs == null)
				return;

			try
			{
				while (m_rs.next())
				{
					if (this.isInterrupted())
					{
						log.fine("Interrupted");
						close();
						return;
					}
					//  Get Data
					Object[] rowData = readData(m_rs);
					//	add Data
					MSort sort = new MSort(m_buffer.size(), null);	//	index
					m_buffer.add(rowData);
					m_sort.add(sort);

					//	Statement all 250 rows & sleep
					if (m_buffer.size() % 250 == 0)
					{
						//	give the other processes a chance
						try
						{
							yield();
							sleep(10);		//	.01 second
						}
						catch (InterruptedException ie)
						{
							log.fine("Interrupted while sleeping");
							close();
							return;
						}
						DataStatusEvent evt = createDSE();
						evt.setLoading(m_buffer.size());
						fireDataStatusChanged(evt);
					}
				}	//	while(rs.next())
			}
			catch (SQLException e)
			{
				log.log(Level.SEVERE, "run", e);
			}
			close();
			fireDataStatusIEvent("", "");
		}	//	run

		/**
		 *	Set Parameter for Query.
		 *		elements must be Integer, BigDecimal, String (default)
		 *  @param pstmt prepared statement
		 *  @param countSQL count
		 */
		private void setParameter (PreparedStatement pstmt, boolean countSQL)
		{
			if (m_parameterSELECT.size() == 0 && m_parameterWHERE.size() == 0)
				return;
			try
			{
				int pos = 1;	//	position in Statement
				//	Select Clause Parameters
				for (int i = 0; !countSQL && i < m_parameterSELECT.size(); i++)
				{
					Object para = m_parameterSELECT.get(i);
					if (para != null)
						log.fine("Select " + i + "=" + para);
					//
					if (para == null)
						;
					else if (para instanceof Integer)
					{
						Integer ii = (Integer)para;
						pstmt.setInt (pos++, ii.intValue());
					}
					else if (para instanceof BigDecimal)
						pstmt.setBigDecimal (pos++, (BigDecimal)para);
					else
						pstmt.setString(pos++, para.toString());
				}
				//	Where Clause Parameters
				for (int i = 0; i < m_parameterWHERE.size(); i++)
				{
					Object para = m_parameterWHERE.get(i);
					if (para != null)
						log.fine("Where " + i + "=" + para);
					//
					if (para == null)
						;
					else if (para instanceof Integer)
					{
						Integer ii = (Integer)para;
						pstmt.setInt (pos++, ii.intValue());
					}
					else if (para instanceof BigDecimal)
						pstmt.setBigDecimal (pos++, (BigDecimal)para);
					else
						pstmt.setString(pos++, para.toString());
				}
			}
			catch (SQLException e)
			{
				log.log(Level.SEVERE, "parameter", e);
			}
		}	//	setParameter

	}	//	Loader

}
