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

package org.objectweb.cjdbc.common.sql;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.sql.schema.TableColumn;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;

/**
 * An <code>InsertRequest</code> is an SQL request of the following syntax:
 * 
 * <pre>
 *  INSERT INTO table-name [(column-name[,column-name]*)] {VALUES (constant|null[,constant|null]*)}|{SELECT query}
 * </pre>
 * <code>VALUES<code> are ignored.
 *   *
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class InsertRequest extends AbstractWriteRequest implements Serializable
{
  private static final long serialVersionUID = 4184570467338568383L;

  private final boolean     returnsRS;                              // blank
                                                                    // final for
                                                                    // safety

  /**
   * Creates a new <code>InsertRequest</code> instance. The caller must give
   * an SQL request, without any leading or trailing spaces and beginning with
   * 'insert into ' (it will not be checked).
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
   * @param isRead does the request expect a ResultSet ?
   * @exception SQLException if an error occurs
   */
  public InsertRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator, DatabaseSchema schema, int granularity,
      boolean isCaseSensitive, boolean isRead) throws SQLException
  {
    this(sqlQuery, escapeProcessing, timeout, lineSeparator, isRead);
    parse(schema, granularity, isCaseSensitive);
  }

  /**
   * Creates a new <code>InsertRequest</code> instance. The caller must give
   * an SQL request, without any leading or trailing spaces and beginning with
   * 'create table ' (it will not be checked).
   * <p>
   * The request is not parsed but it can be done later by a call to
   * {@link #parse(DatabaseSchema, int, boolean)}.
   * 
   * @param sqlQuery the SQL request
   * @param escapeProcessing should the driver to escape processing before
   *          sending to the database ?
   * @param timeout an <code>int</code> value
   * @param lineSeparator the line separator used in the query
   * @param isRead does the request expects a ResultSet with the auto-generated
   *          keys?
   * @see #parse
   */
  public InsertRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator, boolean isRead)
  {
    super(sqlQuery, escapeProcessing, timeout, lineSeparator,
        RequestType.INSERT);
    this.returnsRS = isRead;
  }

  /**
   * @see AbstractWriteRequest
   */
  public InsertRequest(CJDBCInputStream in) throws IOException
  {
    super(in, RequestType.INSERT);
    returnsRS = in.readBoolean();
    if (returnsRS)
      receiveResultSetParams(in);

  }

  /**
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#sendToStream(org.objectweb.cjdbc.common.stream.CJDBCOutputStream,
   *      boolean)
   */
  public void sendToStream(CJDBCOutputStream out, boolean needSkeleton)
      throws IOException
  {
    super.sendToStream(out, needSkeleton);
    out.writeBoolean(returnsRS);
    if (returnsRS)
      sendResultSetParams(out);
  }

  /**
   * Parse the query to know which table is affected. Also checks for the
   * columns if the parsing granularity requires it.
   * 
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#parse(org.objectweb.cjdbc.common.sql.schema.DatabaseSchema,
   *      int, boolean)
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

    // Strip 'insert into '
    sql = sql.substring(7).trim().substring(5).trim();

    // Look for the VALUES or SELECT clause
    int endIdx = sql.indexOf(" values ");
    if (endIdx == -1)
    {
      endIdx = sql.indexOf(" values(");
      if (endIdx == -1)
      {
        endIdx = sql.indexOf("select ");
        if (endIdx == -1)
          throw new SQLException(
              "Unable to find the VALUES or SELECT keyword in this INSERT statement: '"
                  + sqlQuery + "'");
      }
    }

    if (isCaseSensitive)
    {
      int shift = originalSQL.length() - sql.length();
      sql = originalSQL.substring(shift, shift + endIdx).trim();
    }
    else
      sql = sql.substring(0, endIdx).trim();

    int openParenthesisIdx = sql.indexOf("(");

    // Get the table on which INSERT occurs
    String insertTable;
    if (openParenthesisIdx == -1)
      // Query like INSERT INTO table VALUES(...)
      insertTable = sql;
    else
      // Query of the form INSERT INTO table(column1, ...) VALUES(...)
      insertTable = sql.substring(0, openParenthesisIdx).trim();

    DatabaseTable t = schema.getTable(insertTable, isCaseSensitive);
    if (t == null)
      throw new SQLException("Unknown table '" + insertTable
          + "' in this INSERT statement: '" + sqlQuery + "'");
    else
      tableName = t.getName();

    if ((granularity == ParsingGranularities.COLUMN)
        || (granularity == ParsingGranularities.COLUMN_UNIQUE))
    {
      if (openParenthesisIdx != -1)
      {
        // Fetch the affected columns
        int closingParenthesisIdx = sql.indexOf(")");
        if ((closingParenthesisIdx == -1) || (closingParenthesisIdx > endIdx))
        {
          tableName = null;
          columns = null;
          throw new SQLException(
              "Syntax error in columns definition for this INSERT statement: '"
                  + sqlQuery + "'");
        }

        // Column names are separated by comas
        StringTokenizer columnTokens = new StringTokenizer(sql.substring(
            openParenthesisIdx + 1, closingParenthesisIdx), ",");
        columns = new ArrayList();
        DatabaseColumn col = null;
        while (columnTokens.hasMoreTokens())
        {
          String token = columnTokens.nextToken().trim();
          if ((col = t.getColumn(token)) == null)
          {
            tableName = null;
            columns = null;
            throw new SQLException("Unknown column name '" + token
                + "' in this INSERT statement: '" + sqlQuery + "'");
          }
          else
          {
            columns.add(new TableColumn(tableName, col.getName()));
          }
        }
      }
      else
      {
        // All columns are affected
        columns = new ArrayList();
        ArrayList cols = t.getColumns();
        int size = cols.size();
        for (int j = 0; j < size; j++)
        {
          columns.add(new TableColumn(tableName, ((DatabaseColumn) cols.get(j))
              .getName()));
        }
      }
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
    return returnsRS;
  }

  /**
   * Displays some debugging information about this request.
   */
  public void debug()
  {
    super.debug();
    if (tableName != null)
      System.out.println("Inserted table: " + tableName);
    else
      System.out.println("No information about inserted table");

    if (columns != null)
    {
      System.out.println("Inserted columns:");
      for (int i = 0; i < columns.size(); i++)
        System.out.println("  "
            + ((TableColumn) columns.get(i)).getColumnName());
    }
    else
      System.out.println("No information about inserted columns");

    System.out.println("");
  }

}