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
 * Initial developer(s): Julie Marguerite.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.common.sql;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;

/**
 * An <code>DropRequest</code> is an SQL request with the following syntax:
 * 
 * <pre>
 *  DROP TABLE table-name
 * </pre>
 * 
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public class DropRequest extends AbstractWriteRequest implements Serializable
{
  private static final long serialVersionUID = 7362381853446232926L;

  /**
   * Creates a new <code>DropRequest</code> instance. The caller must give an
   * SQL request, without any leading or trailing spaces and beginning with
   * 'drop table ' (it will not be checked).
   * <p>
   * If the syntax is incorrect an exception is thrown.
   * 
   * @param sqlQuery a <code>String</code> value
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
  public DropRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator, DatabaseSchema schema, int granularity,
      boolean isCaseSensitive) throws SQLException
  {
    this(sqlQuery, escapeProcessing, timeout, lineSeparator);
    parse(schema, granularity, isCaseSensitive);
  }

  /**
   * Creates a new <code>DropRequest</code> instance. The caller must give an
   * SQL request, without any leading or trailing spaces and beginning with
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
   * @see #parse
   */
  public DropRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator)
  {
    super(sqlQuery, escapeProcessing, timeout, lineSeparator, RequestType.DROP);
  }

  /**
   * @see AbstractWriteRequest
   */
  public DropRequest(CJDBCInputStream in) throws IOException
  {
    super(in, RequestType.DROP);
  }

  /**
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

    String originalSQL = this.trimCarriageReturnAndTabs();
    String dropTable = originalSQL.toLowerCase();

    // Strip 'drop (temporary) table '
    int tableIdx = dropTable.indexOf("table");
    if (tableIdx < 0)
      throw new SQLException("TABLE not found in this DROP statement: '"
          + sqlQuery + "'");

    if (isCaseSensitive)
      dropTable = originalSQL.substring(tableIdx + 5).trim();
    else
      dropTable = dropTable.substring(tableIdx + 5).trim();

    if (schema == null)
      tableName = dropTable;
    else
    {
      // Get the table on which DROP occurs
      DatabaseTable t = schema.getTable(dropTable, isCaseSensitive);
      if (t == null)
        throw new SQLException("Unknown table '" + dropTable
            + "' in this DROP statement '" + sqlQuery + "'");
      else
        tableName = t.getName();
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
    return false;
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
      System.out.println("Dropped table '" + tableName + "'");
    else
      System.out.println("No information about dropped table");

    System.out.println();
  }
}