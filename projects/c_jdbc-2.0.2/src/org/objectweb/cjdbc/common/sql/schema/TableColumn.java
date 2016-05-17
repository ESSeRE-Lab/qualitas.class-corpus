/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.common.sql.schema;

/**
 * A <code>TableColumn</code> is used to carry parsing information and
 * contains a database table name and one of its column.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * @version 1.0
 */
public class TableColumn
{
  /** The table name. */
  private String tableName;

  /** The column name. */
  private String columnName;

  /**
   * Creates a new <code>TableColumn</code>.
   * 
   * @param tableName the table name
   * @param columnName the column name
   */
  public TableColumn(String tableName, String columnName)
  {
    if (tableName == null)
      throw new IllegalArgumentException("Illegal null table name in TableColumn constructor");

    if (columnName == null)
      throw new IllegalArgumentException("Illegal null column name in TableColumn constructor");

    this.tableName = tableName;
    this.columnName = columnName;
  }

  /**
   * Returns the column name.
   * 
   * @return the column name.
   */
  public String getColumnName()
  {
    return columnName;
  }

  /**
   * Returns the table name.
   * 
   * @return the table name.
   */
  public String getTableName()
  {
    return tableName;
  }

  /**
   * Sets the column name.
   * 
   * @param columnName the column to set
   */
  public void setColumnName(String columnName)
  {
    this.columnName = columnName;
  }

  /**
   * Sets the table name.
   * 
   * @param tableName the table to set
   */
  public void setTableName(String tableName)
  {
    this.tableName = tableName;
  }

  /**
   * Two <code>TableColumn</code> objects are considered equal if they have
   * the same name and belong to the same table.
   * 
   * @param other the object to compare with
   * @return true if the 2 objects are the same
   */
  public boolean equals(Object other)
  {
    if ((other == null) || !(other instanceof TableColumn))
      return false;

    TableColumn c = (TableColumn) other;
    return columnName.equals(c.getColumnName())
      && tableName.equals(c.getTableName());
  }
}
