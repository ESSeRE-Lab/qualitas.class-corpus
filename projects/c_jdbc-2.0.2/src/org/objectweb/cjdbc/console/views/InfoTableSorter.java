/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: c-jdbc@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Philip Milne.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.console.views;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * This code is inspired from the Swing tutorial demo version 1.5 12/17/97 from
 * Philip Milne. It sorts the table when you click on a header.
 * 
 * @author Philip Milne
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */

public class InfoTableSorter extends AbstractTableModel
    implements
      TableModelListener
{
  private TableModel model;
  private int[]      indexes;
  private Vector     sortingColumns = new Vector();
  private boolean    ascending      = true;
  private int        compares;

  /**
   * Constructor
   * 
   * @param model TableModel to use
   */
  public InfoTableSorter(TableModel model)
  {
    setModel(model);
  }

  private void setModel(TableModel model)
  {
    this.model = model;
    model.addTableModelListener(this);
    reallocateIndexes();
  }

  /**
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount()
  {
    return (model == null) ? 0 : model.getRowCount();
  }

  /**
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return (model == null) ? 0 : model.getColumnCount();
  }

  /**
   * @see javax.swing.table.TableModel#getColumnName(int)
   */
  public String getColumnName(int aColumn)
  {
    return model.getColumnName(aColumn);
  }

  /**
   * @see javax.swing.table.TableModel#getColumnClass(int)
   */
  public Class getColumnClass(int aColumn)
  {
    return model.getColumnClass(aColumn);
  }

  /**
   * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
   */
  public void tableChanged(TableModelEvent e)
  {
    reallocateIndexes();
    fireTableChanged(e);
  }

  // The mapping only affects the contents of the data rows.
  // Pass all requests to these rows through the mapping array: "indexes".

  /**
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(int aRow, int aColumn)
  {
    return model.getValueAt(indexes[aRow], aColumn);
  }

  /**
   * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt(Object aValue, int aRow, int aColumn)
  {
    model.setValueAt(aValue, indexes[aRow], aColumn);
  }

  /**
   * Add a mouse listener to the Table to trigger a table sort when a column
   * heading is clicked in the JTable.
   * 
   * @param table the JTable to sort
   */
  public void addMouseListenerToHeaderInTable(JTable table)
  {
    final InfoTableSorter sorter = this;
    final JTable tableView = table;
    tableView.setColumnSelectionAllowed(false);
    MouseAdapter listMouseListener = new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        TableColumnModel columnModel = tableView.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        int column = tableView.convertColumnIndexToModel(viewColumn);
        if (e.getClickCount() == 1 && column != -1)
        {
          int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
          sorter.sortByColumn(column, shiftPressed == 0);
        }
      }
    };
    JTableHeader th = tableView.getTableHeader();
    th.addMouseListener(listMouseListener);
  }

  private void sortByColumn(int column, boolean ascending)
  {
    this.ascending = ascending;
    sortingColumns.removeAllElements();
    sortingColumns.addElement(new Integer(column));
    compares = 0;
    for (int i = 0; i < getRowCount(); i++)
      for (int j = i + 1; j < getRowCount(); j++)
        if (compare(indexes[i], indexes[j]) == -1)
          swap(i, j);

    fireTableChanged(new TableModelEvent(this));
  }

  private int compareRowsByColumn(int row1, int row2, int column)
  {
    int result = 0;
    Class type = model.getColumnClass(column);
    TableModel data = model;

    // Check for nulls.

    Object o1 = data.getValueAt(row1, column);
    Object o2 = data.getValueAt(row2, column);

    // If both values are null, return 0.
    if (o1 == null && o2 == null)
      return 0;
    else if (o1 == null)
      // Define null less than everything.
      return -1;
    else if (o2 == null)
      return 1;

    if (type.getSuperclass() == java.lang.Number.class)
    {
      Number n1 = (Number) data.getValueAt(row1, column);
      double d1 = n1.doubleValue();
      Number n2 = (Number) data.getValueAt(row2, column);
      double d2 = n2.doubleValue();
      result = (int) (d1 - d2);
    }
    else if (type == java.util.Date.class)
    {
      Date d1 = (Date) data.getValueAt(row1, column);
      long n1 = d1.getTime();
      Date d2 = (Date) data.getValueAt(row2, column);
      long n2 = d2.getTime();
      result = (int) (n1 - n2);
    }
    else if (type == String.class)
    {
      String s1 = (String) data.getValueAt(row1, column);
      String s2 = (String) data.getValueAt(row2, column);
      result = s1.compareTo(s2);
    }
    else
    {
      Object v1 = data.getValueAt(row1, column);
      String s1 = v1.toString();
      Object v2 = data.getValueAt(row2, column);
      String s2 = v2.toString();
      result = s1.compareTo(s2);
    }
    if (result < 0)
      return -1;
    else if (result > 0)
      return 1;
    else
      return 0;
  }

  private int compare(int row1, int row2)
  {
    compares++;
    for (int level = 0; level < sortingColumns.size(); level++)
    {
      Integer column = (Integer) sortingColumns.elementAt(level);
      int result = compareRowsByColumn(row1, row2, column.intValue());
      if (result != 0)
        return ascending ? result : -result;
    }
    return 0;
  }

  private void reallocateIndexes()
  {
    int rowCount = model.getRowCount();

    // Set up a new array of indexes with the right number of elements
    // for the new data model.
    indexes = new int[rowCount];

    // Initialise with the identity mapping.
    for (int row = 0; row < rowCount; row++)
    {
      indexes[row] = row;
    }
  }

  private void swap(int i, int j)
  {
    int tmp = indexes[i];
    indexes[i] = indexes[j];
    indexes[j] = tmp;
  }

}
