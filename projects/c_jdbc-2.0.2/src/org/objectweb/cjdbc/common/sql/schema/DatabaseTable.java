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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Mathieu Peltier, Sara Bouchenak.
 */

package org.objectweb.cjdbc.common.sql.schema;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * A <code>DatabaseTable</code> represents a database table ! It is just an
 * array of <code>TableColumns</code> objects.
 * <p>
 * Keep it mind that <code>ArrayList</code> is not synchronized...
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * @author <a href="mailto:Sara.Bouchenak@epfl.ch">Sara Bouchenak</a>
 * @version 1.0
 */
public class DatabaseTable implements Serializable
{
  private static final long serialVersionUID = 7138810420058450235L;

  /** Database table name. */
  private String            name;

  /** <code>ArrayList</code> of <code>DatabaseColumn</code>. */
  private ArrayList         columns;

  /**
   * Creates a new <code>DatabaseTable</code> instance.
   * 
   * @param name table name
   */
  public DatabaseTable(String name)
  {
    this(name, new ArrayList());
  }

  /**
   * Creates a new <code>DatabaseTable</code> instance.
   * 
   * @param name table name
   * @param nbOfColumns number of columns
   */
  public DatabaseTable(String name, int nbOfColumns)
  {
    this(name, new ArrayList(nbOfColumns));
  }

  /**
   * Creates a new <code>DatabaseTable</code> instance.
   * 
   * @param name table name
   * @param columns columns list
   */
  private DatabaseTable(String name, ArrayList columns)
  {
    if (name == null)
      throw new IllegalArgumentException(
          "Illegal null database table name in DatabaseTable constructor");

    this.name = name;
    this.columns = columns;
  }

  /**
   * Gets the name of the table.
   * 
   * @return the table name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Adds a <code>DatabaseColumn</code> object to this table.
   * <p>
   * Warning! The underlying <code>ArrayList</code> is not synchronized.
   * 
   * @param column a <code>DatabaseColumn</code> value
   */
  public void addColumn(DatabaseColumn column)
  {
    columns.add(column);
  }

  /**
   * Drops a <code>DatabaseColumn</code> object from this table.
   * <p>
   * Warning! The underlying <code>ArrayList</code> is not synchronized.
   * 
   * @param columnName a <code>String</code> that maps to a
   *          <code>DatabaseColumn</code> value
   */
  public void remove(String columnName)
  {
    columns.remove(getColumn(columnName));
  }

  /**
   * Drops a <code>DatabaseColumn</code> object from this table.
   * <p>
   * Warning! The underlying <code>ArrayList</code> is not synchronized.
   * 
   * @param column a <code>DatabaseColumn</code> value
   */
  public void remove(DatabaseColumn column)
  {
    columns.remove(column);
  }

  /**
   * Merges this table with the given table's columns. All missing columns are
   * added if no conflict is detected. An exception is thrown if the given table
   * columns conflicts with this one.
   * 
   * @param table the table to merge
   * @throws SQLException if the schemas conflict
   */
  public void mergeColumns(DatabaseTable table) throws SQLException
  {
    if (table == null)
      return;

    ArrayList otherColumns = table.getColumns();
    if (otherColumns == null)
      return;

    DatabaseColumn c, original;
    int size = otherColumns.size();
    for (int i = 0; i < size; i++)
    {
      c = (DatabaseColumn) otherColumns.get(i);
      original = getColumn(c.getName());
      if (original == null)
        addColumn(c);
      else
      {
        if (!original.equalsIgnoreType(c))
          throw new SQLException("Unable to merge table [" + table.getName()
              + "]: column '" + c.getName() + "' definition mismatch");
      }
    }
  }

  /**
   * Returns a list of <code>DatabaseColumn</code> objects describing the
   * columns of this table.
   * <p>
   * Warning! The underlying <code>ArrayList</code> is not synchronized.
   * 
   * @return an <code>ArrayList</code> of <code>DatabaseColumn</code>
   */
  public ArrayList getColumns()
  {
    return columns;
  }

  /**
   * Returns a list of <code>DatabaseColumn</code> objects representing the
   * unique columns of this table.
   * <p>
   * Warning! The underlying <code>ArrayList</code> is not synchronized.
   * 
   * @return an <code>ArrayList</code> of <code>DatabaseColumn</code>
   *         objects
   */
  public ArrayList getUniqueColumns()
  {
    ArrayList cols = new ArrayList();
    Iterator i;
    DatabaseColumn c;

    for (i = columns.iterator(); i.hasNext();)
    {
      c = (DatabaseColumn) i.next();
      if (c.isUnique())
        cols.add(c);
    }
    return cols;
  }

  /**
   * Returns the <code>DatabaseColumn</code> object matching the given column
   * name or <code>null</code> if not found (the case is ignored).
   * 
   * @param columnName column name to look for
   * @return a <code>DatabaseColumn</code> value or <code>null</code>
   */
  public DatabaseColumn getColumn(String columnName)
  {
    DatabaseColumn c;
    for (Iterator i = columns.iterator(); i.hasNext();)
    {
      c = (DatabaseColumn) i.next();
      if (columnName.equalsIgnoreCase(c.getName()))
        return c;

    }
    return null;
  }

  /**
   * Returns the <code>DatabaseColumn</code> object matching the given column
   * name or <code>null</code> if not found (the case can be enforced).
   * 
   * @param columnName column name to look for
   * @param isCaseSensitive true if name matching must be case sensitive
   * @return a <code>DatabaseColumn</code> value or <code>null</code>
   */
  public DatabaseColumn getColumn(String columnName, boolean isCaseSensitive)
  {
    if (!isCaseSensitive)
      return getColumn(columnName);

    DatabaseColumn c;
    for (Iterator i = columns.iterator(); i.hasNext();)
    {
      c = (DatabaseColumn) i.next();
      if (columnName.equals(c.getName()))
        return c;

    }
    return null;
  }

  /**
   * Two <code>DatabaseColumn</code> are considered equal if they have the
   * same name and the same columns.
   * 
   * @param other the object to compare with
   * @return <code>true</code> if the tables are equal
   */
  public boolean equals(Object other)
  {
    if ((other == null) || !(other instanceof DatabaseTable))
      return false;

    DatabaseTable t = (DatabaseTable) other;
    return (t.getName().equals(name)) && (t.getColumns().equals(columns));
  }

  /**
   * This function is the same as equals but ignores the column type.
   * 
   * @param other the object to compare with
   * @return <code>true</code> if the table are equal ignoring the columns
   *         type
   * @see #equals(Object)
   */
  public boolean equalsIgnoreType(Object other)
  {
    if ((other == null) || !(other instanceof DatabaseTable))
      return false;

    DatabaseTable t = (DatabaseTable) other;
    if (!t.getName().equals(name))
      return false;

    DatabaseColumn c1, c2;
    Iterator iter = columns.iterator();
    while (iter.hasNext())
    {
      c1 = (DatabaseColumn) iter.next();
      c2 = t.getColumn(c1.getName());

      if (!c1.equalsIgnoreType(c2))
        return false; // Not compatible
    }
    return true;
  }

  /**
   * Get xml information about this table.
   * 
   * @return xml formatted information on this database table.
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_DatabaseTable + " "
        + DatabasesXmlTags.ATT_tableName + "=\"" + name + "\" "
        + DatabasesXmlTags.ATT_nbOfColumns + "=\"" + columns.size() + "\">");
    for (int i = 0; i < columns.size(); i++)
      info.append(((DatabaseColumn) columns.get(i)).getXml());
    info.append("</" + DatabasesXmlTags.ELT_DatabaseTable + ">");
    return info.toString();
  }
}
