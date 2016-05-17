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
 * Contributor(s): Sara Bouchenak.
 */

package org.objectweb.cjdbc.controller.cache.result.schema;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.cjdbc.common.sql.RequestType;
import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;

/**
 * A <code>CacheDatabaseTable</code> represents a database table and its
 * associated cache entries. It has an array of <code>CacheDatabaseColumn</code>
 * objects.
 * <p>
 * Keep it mind that <code>ArrayList</code> and <code>HashMap</code> are not
 * synchronized...
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Sara.Bouchenak@epfl.ch">Sara Bouchenak </a>
 * @version 1.0
 */
public class CacheDatabaseTable
{
  private String    name;
  private ArrayList columns;
  private ArrayList cacheEntries;  // Cache entries depending on this table
  private HashMap   pkCacheEntries; // Cache entries corresponding to a pk value

  /**
   * Creates a new <code>CacheDatabaseTable</code> instance.
   * 
   * @param databaseTable the database table to cache
   */
  public CacheDatabaseTable(DatabaseTable databaseTable)
  {
    // Clone the name and the columns
    name = databaseTable.getName();
    ArrayList origColumns = databaseTable.getColumns();
    int size = origColumns.size();
    columns = new ArrayList(size);
    for (int i = 0; i < size; i++)
      columns.add(new CacheDatabaseColumn(((DatabaseColumn) origColumns.get(i))
          .getName()));

    // Create an empty cache
    cacheEntries = new ArrayList();
    pkCacheEntries = new HashMap();
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
   * Adds a <code>CacheDatabaseColumn</code> object to this table.
   * <p>
   * Warning! The underlying <code>ArrayList</code> is not synchronized.
   * 
   * @param column a <code>CacheDatabaseColumn</code> value
   */
  public void addColumn(CacheDatabaseColumn column)
  {
    columns.add(column);
  }

  /**
   * Merge the given table's columns with the current table. All missing columns
   * are added if no conflict is detected. An exception is thrown if the given
   * table columns conflicts with the current one.
   * 
   * @param t the table to merge
   * @throws SQLException if the schemas conflict
   */
  public void mergeColumns(CacheDatabaseTable t) throws SQLException
  {
    if (t == null)
      return;

    ArrayList otherColumns = t.getColumns();
    if (otherColumns == null)
      return;

    int size = otherColumns.size();
    for (int i = 0; i < size; i++)
    {
      CacheDatabaseColumn c = (CacheDatabaseColumn) otherColumns.get(i);
      CacheDatabaseColumn original = getColumn(c.getName());
      if (original == null)
        addColumn(c);
      else
      {
        if (!original.equals(c))
          throw new SQLException("Column " + c.getName()
              + " definition mismatch.");
      }
    }
  }

  /**
   * Returns a list of <code>CacheDatabaseColumn</code> objects describing the
   * columns of this table.
   * <p>
   * Warning! The underlying <code>ArrayList</code> is not synchronized.
   * 
   * @return an <code>ArrayList</code> of <code>CacheDatabaseColumn</code>
   */
  public ArrayList getColumns()
  {
    return columns;
  }

  /**
   * Returns the <code>CacheDatabaseColumn</code> object matching the given
   * column name or <code>null</code> if not found.
   * 
   * @param columnName column name to look for
   * @return a <code>CacheDatabaseColumn</code> value or <code>null</code>
   */
  public CacheDatabaseColumn getColumn(String columnName)
  {
    for (Iterator i = columns.iterator(); i.hasNext();)
    {
      CacheDatabaseColumn c = (CacheDatabaseColumn) i.next();
      if (columnName.compareToIgnoreCase(c.getName()) == 0)
        return c;
    }
    return null;
  }

  /**
   * Two <code>CacheDatabaseColumn</code> are equals if they have the same
   * name and the same columns.
   * 
   * @param other the object to compare with
   * @return true if the objects are the same
   */
  public boolean equals(Object other)
  {
    if (!(other instanceof CacheDatabaseTable))
      return false;

    CacheDatabaseTable t = (CacheDatabaseTable) other;
    return t.getName().equals(name) && t.getColumns().equals(columns);
  }

  /**
   * Adds an <code>AbstractResultCacheEntry</code> object whose consistency
   * depends on this table.
   * 
   * @param ce an <code>AbstractResultCacheEntry</code> value
   */
  public synchronized void addCacheEntry(AbstractResultCacheEntry ce)
  {
    cacheEntries.add(ce);
  }

  /**
   * Adds an <code>AbstractResultCacheEntry</code> object associated to a pk
   * entry.
   * 
   * @param pk the pk entry
   * @param ce an <code>AbstractResultCacheEntry</code> value
   */
  public void addPkCacheEntry(String pk, AbstractResultCacheEntry ce)
  {
    synchronized (pkCacheEntries)
    {
      pkCacheEntries.put(pk, ce);
    }
  }

  /**
   * Gets a <code>CacheEntry</code> object associated to a pk entry.
   * 
   * @param pk the pk entry
   * @return the corresponding cache entry if any or null if nothing is found
   */
  public AbstractResultCacheEntry getPkResultCacheEntry(String pk)
  {
    if (pk == null)
      return null;
    synchronized (pkCacheEntries)
    {
      return (AbstractResultCacheEntry) pkCacheEntries.get(pk);
    }
  }

  /**
   * Remove a <code>CacheEntry</code> object associated to a pk entry.
   * 
   * @param pk the pk entry
   */
  public void removePkResultCacheEntry(Object pk)
  {
    synchronized (pkCacheEntries)
    {
      AbstractResultCacheEntry rce = (AbstractResultCacheEntry) pkCacheEntries
          .remove(pk);
      rce.invalidate();
    }
  }

  /**
   * Invalidates all cache entries of every column of this table. This does also
   * affect the entries based on pk values.
   */
  public void invalidateAll()
  {
    synchronized (this)
    {
      for (Iterator i = cacheEntries.iterator(); i.hasNext();)
        ((AbstractResultCacheEntry) i.next()).invalidate();
      cacheEntries.clear();

      for (int i = 0; i < columns.size(); i++)
        ((CacheDatabaseColumn) columns.get(i)).invalidateAll();
    }
    synchronized (pkCacheEntries)
    { // All pk cache entries have been invalidated as a side effect by the
      // above loop.
      pkCacheEntries.clear();
    }
  }

  /**
   * Invalidates all cache entries of every column of this table. This does not
   * affect the entries based on pk values.
   */
  public synchronized void invalidateAllExceptPk()
  {
    for (Iterator i = cacheEntries.iterator(); i.hasNext();)
    {
      AbstractResultCacheEntry qce = (AbstractResultCacheEntry) i.next();
      if (qce.getRequest().getCacheAbility() != RequestType.UNIQUE_CACHEABLE)
        qce.invalidate();
    }
    cacheEntries.clear();
  }

  /**
   * Returns information about the database table and its columns.
   * 
   * @param longFormat true for a long format, false for a short summary
   * @return String
   */
  public String getInformation(boolean longFormat)
  {
    String result = "Table " + name + ": ";
    int size = columns.size();
    for (int i = 0; i < size; i++)
    {
      CacheDatabaseColumn c = (CacheDatabaseColumn) columns.get(i);
      if (longFormat)
        result += "\n";
      result += c.getInformation();
      if (!longFormat && (i < size - 1))
        result += ",";
    }
    return result;
  }
}