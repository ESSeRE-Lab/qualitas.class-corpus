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

package org.objectweb.cjdbc.common.sql;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.sql.schema.TableColumn;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;

/**
 * An <code>UpdateRequest</code> is an SQL request with the following syntax:
 * 
 * <pre>
 *   UPDATE table-name SET (column-name=expression[,column-name=expression]*) WHERE search-condition
 * </pre>
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public class UpdateRequest extends AbstractWriteRequest implements Serializable
{
  private static final long serialVersionUID = 1943340529813559587L;

  /** <code>true</code> if this request updates a <code>UNIQUE</code> row. */
  private transient boolean isUnique;

  private transient HashMap updatedValues    = null;

  /**
   * Creates a new <code>UpdateRequest</code> instance. The caller must give
   * an SQL request, without any leading or trailing spaces and beginning with
   * 'update ' (it will not be checked).
   * <p>
   * If the syntax is incorrect an exception is thrown.
   * 
   * @param sqlQuery the SQL query
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
  public UpdateRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator, DatabaseSchema schema, int granularity,
      boolean isCaseSensitive) throws SQLException
  {
    this(sqlQuery, escapeProcessing, timeout, lineSeparator);
    parse(schema, granularity, isCaseSensitive);
  }

  /**
   * Creates a new <code>UpdateRequest</code> instance. The caller must give
   * an SQL request, without any leading or trailing spaces and beginning with
   * 'update ' (it will not be checked).
   * <p>
   * The request is not parsed but it can be done later by a call to
   * {@link #parse(DatabaseSchema, int, boolean)}.
   * 
   * @param sqlQuery the SQL query
   * @param escapeProcessing should the driver to escape processing before
   *          sending to the database ?
   * @param timeout an <code>int</code> value
   * @param lineSeparator the line separator used in the query
   * @see #parse
   */
  public UpdateRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator)
  {
    super(sqlQuery, escapeProcessing, timeout, lineSeparator,
        RequestType.UPDATE);
  }

  /**
   * @see AbstractWriteRequest
   */
  public UpdateRequest(CJDBCInputStream in) throws IOException
  {
    super(in, RequestType.UPDATE);
  }

  /**
   * Parses the SQL request and extract the selected columns and tables given
   * the <code>DatabaseSchema</code> of the database targeted by this request.
   * Determines also if this query only deletes a single row, and the equivalent
   * <code>INSERT</code> statement.
   * <p>
   * An exception is thrown when the parsing fails. Warning, this method does
   * not check the validity of the request. In particular, invalid request could
   * be parsed without throwing an exception. However, valid SQL request should
   * never throw an exception.
   * 
   * @param schema a <code>DatabaseSchema</code> value
   * @param granularity parsing granularity as defined in
   *          <code>ParsingGranularities</code>
   * @param isCaseSensitive true if table name parsing is case sensitive
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

    String whereClause = null;
    isUnique = true;

    String originalSQL = this.trimCarriageReturnAndTabs();
    String sql = originalSQL.toLowerCase();

    // Strip 'update '
    sql = sql.substring(7).trim();

    // Look for the SET or WHERE clause
    int setIdx = sql.indexOf("set ");
    int whereIdx = sql.indexOf("where ");
    if (setIdx == -1)
      throw new SQLException(
          "Unable to find the SET keyword in this UPDATE statement: '"
              + sqlQuery + "'");

    if (isCaseSensitive)
      sql = originalSQL.substring(7).trim();

    if (whereIdx == -1)
    {
      whereIdx = sql.length();
      isUnique = false;
    }
    else
    {
      whereClause = sql.substring(whereIdx + 5);
      // 5 = "where".length(), do not trim or remove anything after
      // else the following code will no more work
      sql = sql.substring(0, whereIdx + 1).trim();
    }

    // Get the table on which UPDATE occurs
    DatabaseTable t = schema.getTable(sql.substring(0, setIdx).trim(),
        isCaseSensitive);
    if (t == null)
      throw new SQLException("Unknown table '" + tableName
          + "' in this UPDATE statement: '" + sqlQuery + "'");
    else
      // Get the real name here (resolves case sentivity problems)
      tableName = t.getName();

    if (granularity > ParsingGranularities.TABLE)
    {
      // We have to get the affected columns
      // Column names are separated by comas and are before a '=' symbol
      StringTokenizer columnTokens = new StringTokenizer(sql.substring(
          setIdx + 4, whereIdx), ",");
      // 4=length("SET ")
      columns = new ArrayList();
      DatabaseColumn col = null;
      while (columnTokens.hasMoreTokens())
      {
        String token = columnTokens.nextToken();
        int eq = token.indexOf("=");
        if (eq == -1)
          continue;
        token = token.substring(0, eq).trim();
        col = t.getColumn(token, isCaseSensitive);
        if (col == null)
        {
          tableName = null;
          columns = null;
          throw new SQLException("Unknown column name '" + token
              + "' in this UPDATE statement: '" + sqlQuery + "'");
        }
        else
          columns.add(new TableColumn(tableName, col.getName()));
      }
    }

    isParsed = true;
    if (!isUnique)
      return;
    else
      isUnique = false;

    if (granularity < ParsingGranularities.COLUMN_UNIQUE)
      return;

    // Prepare hashtable for updated values
    updatedValues = new HashMap(columns.size());

    // Check whether this update affects a single row or not
    // Instead of parsing the clause, we use a brutal force technique
    // and we try to directly identify every column name of the table.
    DatabaseColumn col = null;
    ArrayList cols = t.getColumns();
    int size = cols.size();
    for (int j = 0; j < size; j++)
    {
      col = (DatabaseColumn) cols.get(j);
      String colName = col.getName();
      // if pattern found and column not already in result, it's a dependency !
      int matchIdx = whereClause.indexOf(colName);
      while (matchIdx > 0)
      {
        // Try to check that we got the full pattern and not a sub-pattern
        char beforePattern = whereClause.charAt(matchIdx - 1);
        if (((beforePattern >= 'a') && (beforePattern <= 'z'))
            || ((beforePattern >= 'A') && (beforePattern <= 'Z'))
            || (beforePattern == '_'))
          matchIdx = whereClause.indexOf(colName, matchIdx + 1);
        else
        { // Ok it's a good one, check if it is UNIQUE
          isUnique = col.isUnique();
          if (!isUnique)
            return;
          // Check if this UNIQUE columns stands in the left part of an
          // equality
          int eq = whereClause.indexOf("=", matchIdx);
          if ((eq == -1)
              || (whereClause.substring(matchIdx + colName.length(), eq).trim()
                  .length() > 0))
          {
            isUnique = false;
            return;
          }
          do
          {
            eq++; // Skip spaces
          }
          while (whereClause.charAt(eq) == ' ');

          // Check if we have "..." or '...'
          char startChar = whereClause.charAt(eq);
          int end;
          if ((startChar == '\'') || (startChar == '"'))
          {
            eq++;
            do
            { // Look for the end of the quote and take care of \' or \"
              end = whereClause.indexOf(startChar, eq);
            }
            while (whereClause.charAt(end - 1) == '\\');
          }
          else
          {
            // It's a regular value just find the next comma
            end = whereClause.indexOf(",", eq);
            if (end == -1)
              end = whereClause.length();
          }
          pkValue = whereClause.substring(eq, end);

          matchIdx = whereClause.indexOf(colName, matchIdx + 1);
        }
      }
    }

    cacheable = RequestType.UNIQUE_CACHEABLE;

    // Now get the values for each updated field
    sql = originalSQL.substring(7).substring(0, whereIdx).trim();
    if (!isCaseSensitive)
      sql.toLowerCase();
    int set = sql.toLowerCase().indexOf("set");
    sql = sql.substring(set + 3).trim();

    for (int j = 0; j < cols.size(); j++)
    {
      col = (DatabaseColumn) cols.get(j);
      // if pattern found and column not already in result, it's a dependency !
      String colName = (isCaseSensitive) ? col.getName() : col.getName()
          .toLowerCase();
      int matchIdx = sql.indexOf(colName);

      while (matchIdx >= 0)
      {
        char afterPattern = sql.charAt(matchIdx + colName.length());
        if ((afterPattern != '=') && (afterPattern != ' '))
        {
          matchIdx = sql.indexOf(colName, matchIdx + colName.length());
          continue;
        }

        // Try to check that we got the full pattern and not a sub-pattern
        char beforePattern = Character.CONTROL;
        try
        {
          beforePattern = sql.charAt(matchIdx - 1);
        }
        catch (RuntimeException e)
        {
          // nothing
        }
        if (((beforePattern >= 'a') && (beforePattern <= 'z')) // Everything
            // should be
            // lowercase here
            || (beforePattern == '_'))
          matchIdx = sql.indexOf(colName, matchIdx + 1);
        else
        { // Ok, it's good, get the value on the right part of the equality
          int eq = sql.indexOf("=", matchIdx);
          do
          {
            eq++; // Skip spaces
          }
          while (sql.charAt(eq) == ' ');

          // Check if we have "..." or '...'
          char startChar = sql.charAt(eq);
          int end;
          if ((startChar == '\'') || (startChar == '"'))
          {
            eq++;
            do
            { // Look for the end of the quote and take care of \' or \"
              end = sql.indexOf(startChar, eq);
            }
            while (sql.charAt(end - 1) == '\\');
          }
          else
          {
            // It's a regular value just find the next comma
            end = sql.indexOf(",", eq);
            if (end == -1)
              end = sql.length();
          }
          updatedValues.put(col.getName(), sql.substring(eq, end).trim());
          break;
        }
      }
    }
  }

  /**
   * What are the updated values in this request
   * 
   * @return a hashtable of (colname,value) or null if parsing granularity has
   *         stop computation
   */
  public HashMap getUpdatedValues()
  {
    return updatedValues;
  }

  /**
   * @see AbstractRequest#cloneParsing(AbstractRequest)
   */
  public void cloneParsing(AbstractRequest request)
  {
    if (!request.isParsed())
      return;
    cloneTableNameAndColumns((AbstractWriteRequest) request);
    updatedValues = ((UpdateRequest) request).getUpdatedValues();
    isParsed = true;
  }

  /**
   * Returns <code>true</code> as this request updates a <code>UNIQUE</code>
   * row.
   * 
   * @return <code>false</code>
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
    if (tableName != null)
      System.out.println("Updated table: " + tableName);
    else
      System.out.println("No information about updated table");

    if (columns != null)
    {
      System.out.println("Updated columns:");
      for (int i = 0; i < columns.size(); i++)
        System.out.println("  "
            + ((TableColumn) columns.get(i)).getColumnName());
    }
    else
      System.out.println("No information about updated columns");

    System.out.println("Unique update: " + isUnique);

    System.out.println("");
  }
}