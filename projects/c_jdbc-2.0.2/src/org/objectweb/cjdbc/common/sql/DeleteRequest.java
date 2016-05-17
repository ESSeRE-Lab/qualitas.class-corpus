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
 * Contributor(s): Julie Marguerite, Mathieu Peltier, Sara Bouchenak.
 */

package org.objectweb.cjdbc.common.sql;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.sql.schema.AliasedDatabaseTable;
import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.sql.schema.TableColumn;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;

/**
 * An <code>DeleteRequest</code> is an SQL request with the following syntax:
 * 
 * <pre>DELETE [table1] FROM table1,table2,table3,... WHERE search-condition
 * or DELETE t WHERE search-condition
 * </pre>
 * 
 * Note that DELETE from multiple tables are not supported but this is not part
 * of the SQL standard.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Sara.Bouchenak@epfl.ch">Sara Bouchenak </a>
 * @version 1.0
 */
public class DeleteRequest extends AbstractWriteRequest implements Serializable
{
  private static final long   serialVersionUID = 6112365011840943168L;

  /** <code>true</code> if this query only deletes a single row. */
  private transient boolean   isUnique         = false;

  /** <code>ArrayList</code> of <code>String</code> objects */
  private transient ArrayList from;

  /**
   * <code>ArrayList</code> of values <code>String</code> associated with
   * the unique columns involved in this delete query.
   * <p>
   * The <code>values</code> instance variable is only used when a <code>
   * COLUMN_UNIQUE_DELETE</code>
   * granularity is applied. Here, the DELETE request is UNIQUE: all columns of
   * the WHERE clause are UNIQUE and used in the left part of an equality. When
   * such a granularity is used, the <code>columns</code> instance variable
   * contains only UNIQUE columns.
   * 
   * @see org.objectweb.cjdbc.controller.cache.result.CachingGranularities
   */
  protected ArrayList         whereValues;

  /**
   * Creates a new <code>DeleteRequest</code> instance. The caller must give
   * an SQL request, without any leading or trailing spaces and beginning with
   * 'delete ' (it will not be checked).
   * <p>
   * If the syntax is incorrect an exception is thrown.
   * 
   * @param sqlQuery the SQL request
   * @param escapeProcessing should the driver to escape processing before
   *          sending to the database ?
   * @param timeout an <code>int</code> value
   * @param lineSeparator the line separator used in the query
   * @param schema a <code>DatabaseSchema</code> value
   * @param granularity parsing granularity as defined in
   *          <code>ParsingGranularities</code>
   * @param isCaseSensitive true if parsing is case sensitive
   * @exception SQLException if an error occurs
   */
  public DeleteRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator, DatabaseSchema schema, int granularity,
      boolean isCaseSensitive) throws SQLException
  {
    this(sqlQuery, escapeProcessing, timeout, lineSeparator);
    parse(schema, granularity, isCaseSensitive);
  }

  /**
   * Creates a new <code>DeleteRequest</code> instance. The caller must give
   * an SQL request, without any leading or trailing spaces and beginning with
   * 'delete ' (it will not be checked).
   * <p>
   * The request is not parsed but it can be done later by a call to
   * {@link #parse(DatabaseSchema, int, boolean)}.
   * 
   * @param sqlQuery the SQL request
   * @param escapeProcessing should the driver to escape processing before
   *          sending to the database ?
   * @param timeout an <code>int</code> value
   * @param lineSeparator the line separator used in the query
   * @see #parse
   */
  public DeleteRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator)
  {
    super(sqlQuery, escapeProcessing, timeout, lineSeparator,
        RequestType.DELETE);
  }

  /**
   * @see AbstractWriteRequest
   */
  public DeleteRequest(CJDBCInputStream in) throws IOException
  {
    super(in, RequestType.DELETE);
  }

  /**
   * Parses the SQL request and extracts the selected columns and tables given
   * the <code>DatabaseSchema</code> of the database targeted by this request.
   * <p>
   * An exception is thrown when the parsing fails. Warning, this method does
   * not check the validity of the request. In particular, invalid request could
   * be parsed without throwing an exception. However, valid SQL request should
   * never throw an exception.
   * 
   * @param schema a <code>DatabaseSchema</code> value
   * @param granularity parsing granularity as defined in
   *          <code>ParsingGranularities</code>
   * @param isCaseSensitive if parsing must be case sensitive
   * @exception SQLException if the parsing fails
   */
  public void parse(DatabaseSchema schema, int granularity,
      boolean isCaseSensitive) throws SQLException
  {
    if (granularity == ParsingGranularities.NO_PARSING)
    {
      isParsed = true;
      return;
    }

    // Sanity check
    if (schema == null)
      throw new SQLException(
          "Unable to parse request with an undefined database schema");

    String originalSQL = this.trimCarriageReturnAndTabs();
    String sql = originalSQL.toLowerCase();

    int fromIdx = sql.indexOf("from ");
    if (fromIdx == -1)
    {
      // For queries like: DELETE t WHERE ... used by Oracle
      fromIdx = 6; // 6 = "delete".length()
    }
    else
    {
      // Syntax is usually DELETE FROM t WHERE ... but it can be
      // DELETE t1 FROM t1,t2,.... WHERE ...
      // If there is something between DELETE and FROM, tableName will use this
      // name but the FROM clause will have all tables.
      String tableBetweenDeleteAndFrom;
      if (isCaseSensitive)
        tableBetweenDeleteAndFrom = originalSQL.substring(6, fromIdx).trim();
      else
        tableBetweenDeleteAndFrom = sql.substring(6, fromIdx).trim();
      if (tableBetweenDeleteAndFrom.length() == 0)
        tableName = null;
      else
        tableName = tableBetweenDeleteAndFrom;
      fromIdx += 5; // 5 = "from".length()
    }

    sql = sql.substring(fromIdx).trim();

    // Look for the WHERE clause
    int whereIdx = sql.indexOf("where ");

    if (isCaseSensitive)
      sql = originalSQL.substring(originalSQL.length() - sql.length());
    if (tableName == null)
    { // It was not a DELETE t1 FROM xxx type of query
      if (whereIdx == -1)
        tableName = sql;
      else
        tableName = sql.substring(0, whereIdx).trim();
    }

    // Get the table on which DELETE occurs
    DatabaseTable t = schema.getTable(tableName, isCaseSensitive);
    if (t == null)
      throw new SQLException("Unknown table '" + tableName
          + "' in this DELETE statement: " + sqlQuery + "'");
    else
      // Get the real name here (resolves case sentivity problems)
      tableName = t.getName();

    try
    {
      switch (granularity)
      {
        case ParsingGranularities.NO_PARSING :
          return;
        case ParsingGranularities.TABLE :
          break;
        case ParsingGranularities.COLUMN :
          from = getFromTables(tableName, schema);
          columns = getWhereColumns(sql.substring(whereIdx + 6).trim(), from);

          if (from != null)
          {
            // Convert 'from' to an ArrayList of String objects instead of
            // AliasedTables objects
            int size = from.size();
            ArrayList unaliased = new ArrayList(size);
            for (int i = 0; i < size; i++)
              unaliased.add(((AliasedDatabaseTable) from.get(i)).getTable()
                  .getName());
            from = unaliased;
          }
          break;
        case ParsingGranularities.COLUMN_UNIQUE :
          from = getFromTables(tableName, schema);
          columns = getWhereColumns(sql.substring(whereIdx + 6).trim(), from);

          if (from != null)
          {
            // Convert 'from' to an ArrayList of String objects instead of
            // AliasedTables objects
            int size = from.size();
            ArrayList unaliased = new ArrayList(size);
            for (int i = 0; i < size; i++)
              unaliased.add(((AliasedDatabaseTable) from.get(i)).getTable()
                  .getName());
            from = unaliased;
          }
          break;
        default :
          throw new SQLException("Unsupported parsing granularity: '"
              + granularity + "'");
      }
    }
    catch (SQLException e)
    {
      from = null;
      columns = null;
      whereValues = null;
      throw e;
    }

    isParsed = true;
  }

  /**
   * @see AbstractRequest#cloneParsing(AbstractRequest)
   */
  public void cloneParsing(AbstractRequest request)
  {
    if (!request.isParsed())
      return;
    cloneTableNameAndColumns((AbstractWriteRequest) request);
    isParsed = true;
  }

  /**
   * Extracts the tables from the given <code>FROM</code> clause and retrieves
   * their alias if any.
   * 
   * @param fromClause the <code>FROM</code> clause of the request (without
   *          the <code>FROM</code> keyword)
   * @param dbs the <code>DatabaseSchema</code> this request refers to
   * @return an <code>ArrayList</code> of <code>AliasedDatabaseTable</code>
   *         objects
   * @exception an <code>SQLException</code> if an error occurs
   */
  private ArrayList getFromTables(String fromClause, DatabaseSchema dbs)
      throws SQLException
  {
    StringTokenizer tables = new StringTokenizer(fromClause, ",");
    ArrayList result = new ArrayList(tables.countTokens());
    while (tables.hasMoreTokens())
    {
      String tableName = tables.nextToken().trim();
      // Check if the table has an alias
      // Example: SELECT x.price FROM item x
      String alias = null;
      int aliasIdx = tableName.indexOf(' ');
      if (aliasIdx != -1)
      {
        alias = tableName.substring(aliasIdx);
        tableName = tableName.substring(0, aliasIdx);
      }

      DatabaseTable table = dbs.getTable(tableName);
      if (table == null)
        throw new SQLException("Unknown table '" + tableName
            + "' in FROM clause of this DELETE statement: '" + sqlQuery + "'");
      result.add(new AliasedDatabaseTable(table, alias));
    }

    return result;
  }

  /**
   * Gets all the columns involved in the given <code>WHERE</code> clause.
   * <p>
   * The selected columns or tables must be found in the given
   * <code>ArrayList</code> of <code>AliasedDatabaseTable</code>
   * representing the <code>FROM</code> clause of the same request.
   * 
   * @param whereClause <code>WHERE</code> clause of the request (without the
   *          <code>WHERE</code> keyword)
   * @param aliasedFrom an <code>ArrayList</code> of
   *          <code>AliasedDatabaseTable</code>
   * @return an <code>ArrayList</code> of <code>TableColumn</code>
   */
  private ArrayList getWhereColumns(String whereClause, ArrayList aliasedFrom)
  {
    ArrayList result = new ArrayList(); // TableColumn objects
    ArrayList dbColumns = new ArrayList(); // DatabaseColumn objects

    // Instead of parsing the clause, we use a brutal force technique
    // and we try to directly identify every column name of each table.
    DatabaseColumn col;
    for (int i = 0; i < aliasedFrom.size(); i++)
    {
      DatabaseTable t = ((AliasedDatabaseTable) aliasedFrom.get(i)).getTable();
      ArrayList cols = t.getColumns();
      int size = cols.size();
      for (int j = 0; j < size; j++)
      {
        col = (DatabaseColumn) cols.get(j);
        // if pattern found and column not already in result, it's a dependency
        // !
        int matchIdx = whereClause.indexOf(col.getName());
        while (matchIdx > 0)
        {
          // Try to check that we got the full pattern and not a sub-pattern
          char beforePattern = whereClause.charAt(matchIdx - 1);
          // Everything should be lowercase here
          if (((beforePattern >= 'a') && (beforePattern <= 'z')) // Everything
              || (beforePattern == '_'))
            matchIdx = whereClause.indexOf(col.getName(), matchIdx + 1);
          else
            break;
        }
        if (matchIdx == -1)
          continue;
        result.add(new TableColumn(t.getName(), col.getName()));
        if (col.isUnique())
          pkValue = col.getName();
        dbColumns.add(col);
      }
    }

    return result;
  }

  /**
   * Returns an <code>ArrayList</code> of <code>String</code> objects
   * representing the values associated with the unique columns involved in this
   * request.
   * 
   * @return an <code>ArrayList</code> value
   */
  public ArrayList getValues()
  {
    return whereValues;
  }

  /**
   * Returns <code>true</code> if this query only deletes a single row.
   * 
   * @return a <code>boolean</code> value
   */
  public boolean isUnique()
  {
    return isUnique;
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#needsMacroProcessing()
   */
  public boolean needsMacroProcessing()
  {
    return true;
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#returnsResultSet()
   */
  public boolean returnsResultSet()
  {
    return false;
  }

  /**
   * Displays some debugging information about this request.
   */
  public void debug()
  {
    super.debug();
    System.out.println("Is unique: " + isUnique);
    if (tableName != null)
      System.out.println("Deleted table: " + tableName);
    else
      System.out.println("No information about deleted table");

    if (columns != null)
    {
      System.out.println("Columns columns:");
      for (int i = 0; i < columns.size(); i++)
        System.out.println("  "
            + ((TableColumn) columns.get(i)).getColumnName());
    }
    else
      System.out.println("No information about updated columns");

    System.out.println();
  }
}