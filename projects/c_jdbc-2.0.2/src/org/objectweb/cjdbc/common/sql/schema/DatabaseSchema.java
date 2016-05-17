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
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.common.sql.schema;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * A <code>DatabaseSchema</code> describes all the tables and columns of a
 * database.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class DatabaseSchema implements Serializable
{
  private static final long serialVersionUID = 1105453274994163661L;

  /** <code>ArrayList</code> of <code>DatabaseTables</code>. */
  private ArrayList         tables;
  /** <code>ArrayList</code> of <code>DatabaseProcedures</code>. */
  private ArrayList         procedures;

  /**
   * Creates a new <code>DatabaseSchema</code> instance.
   */
  public DatabaseSchema()
  {
    tables = new ArrayList();
    procedures = new ArrayList();
  }

  /**
   * Creates a new <code>DatabaseSchema</code> instance with a specified
   * number of tables.
   * 
   * @param nbOfTables an <code>int</code> value
   */
  public DatabaseSchema(int nbOfTables)
  {
    tables = new ArrayList(nbOfTables);
    procedures = new ArrayList();
  }

  /**
   * Creates a new <code>DatabaseSchema</code> instance from an existing
   * database schema (the schema is cloned).
   * 
   * @param schema the existing database schema
   */
  public DatabaseSchema(DatabaseSchema schema)
  {
    if (schema == null)
      throw new IllegalArgumentException(
          "Illegal null database schema in DatabaseSchema(DatabaseSchema) constructor");

    tables = new ArrayList(schema.getTables());
    procedures = new ArrayList(schema.getProcedures());
  }

  /**
   * Adds a <code>DatabaseTable</code> describing a table of the database.
   * 
   * @param table the table to add
   */
  public void addTable(DatabaseTable table)
  {
    if (table == null)
      throw new IllegalArgumentException(
          "Illegal null database table in addTable(DatabaseTable) method");
    tables.add(table);
  }

  /**
   * Adds a <code>DatabaseProcedure</code> describing a procedure of the
   * database.
   * 
   * @param procedure the procedure to add
   */
  public void addProcedure(DatabaseProcedure procedure)
  {
    if (procedure == null)
      throw new IllegalArgumentException(
          "Illegal null database table in addTable(DatabaseTable) method");
    procedures.add(procedure);
  }

  /**
   * Removes a <code>DatabaseTable</code> describing a table of the database.
   * 
   * @param table the table to remove
   * @return true if the table was successfully removed
   */
  public boolean removeTable(DatabaseTable table)
  {
    if (table == null)
      throw new IllegalArgumentException(
          "Illegal null database table in removeTable(DatabaseTable) method");
    return tables.remove(table);
  }

  /**
   * removes a <code>DatabaseProcedure</code> describing a procedure of the
   * database.
   * 
   * @param procedure to remove
   * @return true if the procedure was successfully removed
   */
  public boolean removeProcedure(DatabaseProcedure procedure)
  {
    if (procedure == null)
      throw new IllegalArgumentException(
          "Illegal null database procedure in removeProcedure(DatabaseProcedure) method");
    return procedures.remove(procedure);
  }

  /**
   * Merges the given schema with the current one. All missing tables or columns
   * are added if no conflict is detected. An exception is thrown if the given
   * schema definition conflicts with the current one.
   * 
   * @param databaseSchema the schema to merge
   * @throws SQLException if the schemas conflict
   */
  public void mergeSchema(DatabaseSchema databaseSchema) throws SQLException
  {
    if (databaseSchema == null)
      throw new IllegalArgumentException(
          "Illegal null database schema in mergeSchema(DatabaseSchema) method");

    ArrayList otherTables = databaseSchema.getTables();
    if (otherTables.size() == 0)
      return;

    DatabaseTable table, originalTable;
    int size = otherTables.size();
    for (int i = 0; i < size; i++)
    {
      table = (DatabaseTable) otherTables.get(i);
      originalTable = getTable(table.getName());
      if (originalTable == null)
        addTable(table);
      else
        originalTable.mergeColumns(table);
    }

    ArrayList otherProcedures = databaseSchema.getProcedures();
    if (otherProcedures.size() == 0)
      return;

    DatabaseProcedure procedure, originalProcedure;
    int sizep = otherProcedures.size();
    for (int i = 0; i < sizep; i++)
    {
      procedure = (DatabaseProcedure) otherProcedures.get(i);
      originalProcedure = getProcedure(procedure.getName());
      if (originalProcedure == null)
        addProcedure(procedure);
      else
        originalProcedure.mergeParameters(procedure);
    }
  }

  /**
   * Returns an <code>ArrayList</code> of <code>DatabaseTable</code> objects
   * describing the database.
   * 
   * @return an <code>ArrayList</code> of <code>DatabaseTable</code>
   */
  public ArrayList getTables()
  {
    return tables;
  }

  /**
   * Returns an <code>ArrayList</code> of <code>DatabaseProcedure</code>
   * objects describing the database.
   * 
   * @return an <code>ArrayList</code> of <code>DatabaseProcedure</code>
   */
  public ArrayList getProcedures()
  {
    return procedures;
  }

  /**
   * Returns the <code>DatabaseTable</code> object matching the given table
   * name or <code>null</code> if not found.
   * 
   * @param tableName the table name to look for
   * @return a <code>DatabaseTable</code> value or null
   */
  public DatabaseTable getTable(String tableName)
  {
    if (tableName == null)
      return null;

    DatabaseTable t;
    int size = tables.size();
    for (int i = 0; i < size; i++)
    {
      t = (DatabaseTable) tables.get(i);
      if (tableName.compareTo(t.getName()) == 0)
        return t;
    }
    return null;
  }

  /**
   * Returns the <code>DatabaseProcedure</code> object matching the given
   * procedure name or <code>null</code> if not found.
   * 
   * @param procedureName the procedure name to look for
   * @return a <code>DatabaseProcedure</code> value or null
   */
  public DatabaseProcedure getProcedure(String procedureName)
  {
    if (procedureName == null)
      return null;

    DatabaseProcedure t;
    int size = procedures.size();
    for (int i = 0; i < size; i++)
    {
      t = (DatabaseProcedure) procedures.get(i);
      if (procedureName.compareTo(t.getName()) == 0)
        return t;
    }
    return null;
  }

  /**
   * Returns the <code>DatabaseTable</code> object matching the given table
   * name or <code>null</code> if not found. An extra boolean indicates if
   * table name matching is case sensitive or not.
   * 
   * @param tableName the table name to look for
   * @param isCaseSensitive true if name matching must be case sensitive
   * @return a <code>DatabaseTable</code> value or null
   */
  public DatabaseTable getTable(String tableName, boolean isCaseSensitive)
  {
    if (isCaseSensitive)
      return getTable(tableName);

    if (tableName == null)
      return null;

    DatabaseTable t;
    int size = tables.size();
    for (int i = 0; i < size; i++)
    {
      t = (DatabaseTable) tables.get(i);
      if (tableName.equalsIgnoreCase(t.getName()))
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
    if (tableName == null)
      return false;

    DatabaseTable t;
    int size = tables.size();
    for (int i = 0; i < size; i++)
    {
      t = (DatabaseTable) tables.get(i);
      if (tableName.equals(t.getName()))
        return true;
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the given <code>ProcedureName</code> is
   * found in this schema.
   * 
   * @param procedureName the name of the procedure you are looking for
   * @return <code>true</code> if the procedure has been found
   */
  public boolean hasProcedure(String procedureName)
  {
    if (procedureName == null)
      return false;

    DatabaseProcedure t;
    int size = procedures.size();
    for (int i = 0; i < size; i++)
    {
      t = (DatabaseProcedure) procedures.get(i);
      if (procedureName.equals(t.getName()))
        return true;
    }
    return false;
  }

  /**
   * Checks if this <code>DatabaseSchema</code> is a compatible subset of a
   * given schema. It means that all tables in this schema must be present with
   * the same definition in the other schema.
   * 
   * @param other the object to compare with
   * @return <code>true</code> if the two schemas are compatible
   */
  public boolean isCompatibleSubset(DatabaseSchema other)
  {
    if (other == null)
      return false;

    DatabaseTable table, otherTable;
    int size = tables.size();
    for (int i = 0; i < size; i++)
    {
      // Parse all tables
      table = (DatabaseTable) tables.get(i);
      otherTable = other.getTable(table.getName());
      if (otherTable == null)
        return false; // Not present
      else if (!table.equalsIgnoreType(otherTable))
        return false; // Not compatible
    }
    DatabaseProcedure procedure, otherProcedure;
    int sizep = procedures.size();
    for (int i = 0; i < sizep; i++)
    {
      // Parse all procedures
      procedure = (DatabaseProcedure) procedures.get(i);
      otherProcedure = other.getProcedure(procedure.getName());
      if (otherProcedure == null)
        return false; // Not present
      else if (!procedure.equals(otherProcedure))
        return false; // Not compatible
    }
    return true; // Ok, all tables passed the test
  }

  /**
   * Checks if this <code>DatabaseSchema</code> is compatible with the given
   * schema. It means that all tables in this schema that are common with the
   * other schema must be identical.
   * 
   * @param other the object to compare with
   * @return <code>true</code> if the two schemas are compatible
   */
  public boolean isCompatibleWith(DatabaseSchema other)
  {
    DatabaseTable table, otherTable;
    int size = tables.size();
    for (int i = 0; i < size; i++)
    { // Parse all tables
      table = (DatabaseTable) tables.get(i);
      otherTable = other.getTable(table.getName());
      if (otherTable == null)
        continue; // Not present in other schema
      else if (!table.equalsIgnoreType(otherTable))
        return false; // Not compatible
    }
    DatabaseProcedure procedure, otherProcedure;
    int sizep = procedures.size();
    for (int i = 0; i < sizep; i++)
    { // Parse all procedures
      procedure = (DatabaseProcedure) procedures.get(i);
      otherProcedure = other.getProcedure(procedure.getName());
      if (otherProcedure == null)
        continue; // Not present
      else if (!procedure.equals(otherProcedure))
        return false; // Not compatible
    }
    return true; // Ok, all tables passed the test
  }

  /**
   * Two <code>DatabaseSchema</code> are considered equal if they have the
   * same tables and the same procedures.
   * 
   * @param other the object to compare with
   * @return <code>true</code> if the schemas are equals
   */
  public boolean equals(Object other)
  {
    boolean equal = true;
    if ((other == null) || !(other instanceof DatabaseSchema))
      return false;
    if (tables == null)
      equal &= ((DatabaseSchema) other).getTables() == null;
    else
      equal &= tables.equals(((DatabaseSchema) other).getTables());
    if (procedures == null)
      equal &= ((DatabaseSchema) other).getProcedures() == null;
    else
      equal &= procedures.equals(((DatabaseSchema) other).getProcedures());
    return equal;
  }

  /**
   * Get xml information about this schema.
   * 
   * @return xml formatted information on this database schema.
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_DatabaseStaticSchema + ">");
    for (int i = 0; i < procedures.size(); i++)
      info.append(((DatabaseProcedure) procedures.get(i)).getXml());
    for (int i = 0; i < tables.size(); i++)
      info.append(((DatabaseTable) tables.get(i)).getXml());
    info.append("</" + DatabasesXmlTags.ELT_DatabaseStaticSchema + ">");
    return info.toString();
  }

}