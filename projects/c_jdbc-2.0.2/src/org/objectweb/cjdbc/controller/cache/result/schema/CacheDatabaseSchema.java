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
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.controller.cache.result.schema;

import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;

/**
 * A <code>CacheDatabaseSchema</code> describes all the tables and columns of
 * a database and its associated cache entries.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class CacheDatabaseSchema
{
  /** Database tables. */
  private ArrayList tables;

  /**
   * Creates a new <code>CacheDatabaseSchema</code> instance by cloning an
   * existing <code>DatabaseSchema</code>.
   * 
   * @param dbs the <code>DatabaseSchema</code> to clone
   */
  public CacheDatabaseSchema(DatabaseSchema dbs)
  {
    if (dbs == null)
    {
      tables = new ArrayList();
      return;
    }

    // Clone the tables
    ArrayList origTables = dbs.getTables();
    int size = origTables.size();
    tables = new ArrayList(size);
    for (int i = 0; i < size; i++)
      tables.add(new CacheDatabaseTable((DatabaseTable) origTables.get(i)));
  }

  /**
   * Adds a <code>CacheDatabaseTable</code> describing a table of the
   * database.
   * 
   * @param table the table to add
   */
  public void addTable(CacheDatabaseTable table)
  {
    tables.add(table);
  }

  /**
   * Removes a <code>CacheDatabaseTable</code> describing a table of the
   * database.
   * 
   * @param table the table to remove
   */
  public void removeTable(CacheDatabaseTable table)
  {
    tables.remove(table);
  }

  /**
   * Merge the given schema with the current one. All missing tables or columns
   * are added if no conflict is detected. An exception is thrown if the given
   * schema definition conflicts with the current one.
   * 
   * @param databaseSchema the schema to merge
   * @throws SQLException if the schemas conflict
   */
  public void mergeSchema(CacheDatabaseSchema databaseSchema)
      throws SQLException
  {
    if (databaseSchema == null)
      return;

    ArrayList otherTables = databaseSchema.getTables();
    if (otherTables == null)
      return;

    int size = otherTables.size();
    for (int i = 0; i < size; i++)
    {
      CacheDatabaseTable t = (CacheDatabaseTable) otherTables.get(i);
      CacheDatabaseTable original = getTable(t.getName());
      if (original == null)
        addTable(t);
      else
        original.mergeColumns(t);
    }
  }

  /**
   * Returns an <code>ArrayList</code> of <code>CacheDatabaseTable</code>
   * objects describing the database.
   * 
   * @return an <code>ArrayList</code> of <code>CacheDatabaseTable</code>
   */
  public ArrayList getTables()
  {
    return tables;
  }

  /**
   * Returns the <code>CacheDatabaseTable</code> object matching the given
   * table name or <code>null</code> if not found.
   * 
   * @param tableName the table name to look for
   * @return a <code>CacheDatabaseTable</code> value or null
   */
  public CacheDatabaseTable getTable(String tableName)
  {
    int size = tables.size();
    for (int i = 0; i < size; i++)
    {
      CacheDatabaseTable t = (CacheDatabaseTable) tables.get(i);
      if (t.getName().compareTo(tableName) == 0)
        return t;
    }
    return null;
  }

  /**
   * Returns <code>true</code> if the given <code>TableName</code> is found
   * in this schema.
   * 
   * @param tableName the name of the table you are looking for
   * @return <code>true</code> if the table has been found
   */
  public boolean hasTable(String tableName)
  {
    int size = tables.size();
    for (int i = 0; i < size; i++)
    {
      CacheDatabaseTable t = (CacheDatabaseTable) tables.get(i);
      if (tableName.equals(t.getName()))
        return true;
    }
    return false;
  }

  /**
   * Two <code>CacheDatabaseSchema</code> are equals if they have the same
   * tables.
   * 
   * @param other the object to compare with
   * @return true if the 2 objects are the same.
   */
  public boolean equals(Object other)
  {
    if (!(other instanceof CacheDatabaseSchema))
      return false;

    if (tables == null)
      return ((CacheDatabaseSchema) other).getTables() == null;
    else
      return tables.equals(((CacheDatabaseSchema) other).getTables());
  }

  /**
   * Returns information about the database schema.
   * 
   * @param longFormat <code>true</code> for a long format, false for a short
   *          summary
   * @return a <code>String</code> value
   */
  public String getInformation(boolean longFormat)
  {
    String result = "";
    int size = tables.size();
    for (int i = 0; i < size; i++)
    {
      CacheDatabaseTable t = (CacheDatabaseTable) tables.get(i);
      result += t.getInformation(longFormat) + "\n";
    }
    return result;
  }

}
