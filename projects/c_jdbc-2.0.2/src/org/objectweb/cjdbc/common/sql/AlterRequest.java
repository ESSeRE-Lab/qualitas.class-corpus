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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.sql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.sql.schema.TableColumn;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;

/**
 * This class defines a AlterRequest
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class AlterRequest extends AbstractWriteRequest
{
  private static final long        serialVersionUID = -6511969618172455539L;

  /** The table to alter. */
  private transient DatabaseTable  table            = null;

  /** The column altered */
  private transient DatabaseColumn column           = null;

  private transient boolean        isDrop           = false;
  private transient boolean        isAdd            = false;

  /**
   * Creates a new <code>AlterRequest</code> instance. The caller must give an
   * SQL request, without any leading or trailing spaces and beginning with
   * 'alter table '
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
  public AlterRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator)
  {
    super(sqlQuery, escapeProcessing, timeout, lineSeparator, RequestType.ALTER);
  }

  /**
   * Creates a new <code>AlterRequest</code> instance. The caller must give an
   * SQL request, without any leading or trailing spaces and beginning with
   * 'alter table '
   * <p>
   * If the syntax is incorrect an exception is thrown.
   * 
   * @param sqlQuery the SQL request
   * @param escapeProcessing should the driver to escape processing before
   *          sending to the database?
   * @param timeout an <code>int</code> value
   * @param lineSeparator the line separator used in the query
   * @param schema a <code>DatabaseSchema</code> value
   * @param granularity parsing granularity as defined in
   *          <code>ParsingGranularities</code>
   * @param isCaseSensitive true if parsing is case sensitive
   * @exception SQLException if an error occurs
   */
  public AlterRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator, DatabaseSchema schema, int granularity,
      boolean isCaseSensitive) throws SQLException
  {
    this(sqlQuery, escapeProcessing, timeout, lineSeparator);
    parse(schema, granularity, isCaseSensitive);
  }

  /**
   * @see AbstractWriteRequest
   */
  public AlterRequest(CJDBCInputStream in) throws IOException
  {
    super(in, RequestType.ALTER);
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
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#parse(org.objectweb.cjdbc.common.sql.schema.DatabaseSchema,
   *      int, boolean)
   */
  public void parse(DatabaseSchema schema, int granularity,
      boolean isCaseSensitive) throws SQLException
  {
    /*
     * Example Alter statement: ALTER TABLE table_name ADD column_name datatype
     * ALTER TABLE table_name DROP COLUMN column_name
     */

    if (granularity == ParsingGranularities.NO_PARSING)
    {
      isParsed = true;
      return;
    }

    String originalSQL = this.trimCarriageReturnAndTabs();
    String sql = originalSQL.toLowerCase();

    // Strip 'alter table '
    int tableIdx = sql.indexOf("table");
    if (tableIdx == -1)
      throw new SQLException(
          "Malformed Alter Request. Should start with [ALTER TABLE]");
    sql = sql.substring(tableIdx + 5).trim();

    // Does the query contain a add?
    int addIdx = sql.indexOf(" add ");

    // Does the query contain a drop?
    int dropIdx = sql.indexOf(" drop ");

    if (addIdx != -1)
      isAdd = true;
    if (dropIdx != -1)
      isDrop = true;

    if (!isAdd && !isDrop)
      throw new SQLException(
          "Malformed Alter Request. No drop or add condition");

    if (isCaseSensitive) // Reverse to the original case
      sql = originalSQL.substring(originalSQL.length() - sql.length());

    int index = (isAdd) ? addIdx : dropIdx;

    tableName = sql.substring(0, index).trim();
    table = new DatabaseTable(tableName);

    if (granularity > ParsingGranularities.TABLE)
    {

      int subsIndex = index + 6 + 2; // index +
      // column.length()
      // + space
      if (isAdd)
        subsIndex += 3;
      else
        // Drop
        subsIndex += 4;

      columns = new ArrayList();
      sql = sql.substring(subsIndex).trim();

      if (isAdd)
      {
        int colIndex = sql.indexOf(' ');
        String colName = sql.substring(0, colIndex);

        int uniqueIndex = sql.toLowerCase().indexOf("unique");
        int primary = sql.toLowerCase().indexOf("primary");
        if (uniqueIndex != -1 || primary != -1)
          column = new DatabaseColumn(colName, true);
        else
          column = new DatabaseColumn(colName, false);
        columns.add(new TableColumn(tableName, colName));
      }
      else if (isDrop)
      {
        String colName = sql.trim();
        column = schema.getTable(tableName).getColumn(colName);
        columns.add(new TableColumn(tableName, colName));
      }
    }
    isParsed = true;
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#cloneParsing(org.objectweb.cjdbc.common.sql.AbstractRequest)
   */
  public void cloneParsing(AbstractRequest request)
  {
    if (!request.isParsed())
      return;
    AlterRequest alterRequest = (AlterRequest) request;
    cloneTableNameAndColumns((AbstractWriteRequest) request);
    table = alterRequest.getDatabaseTable();
    column = alterRequest.getColumn();
    isParsed = true;
  }

  /**
   * Returns the table value.
   * 
   * @return Returns the table.
   */
  public DatabaseTable getDatabaseTable()
  {
    return table;
  }

  /**
   * Returns the column value.
   * 
   * @return Returns the column.
   */
  public DatabaseColumn getColumn()
  {
    return column;
  }

  /**
   * Returns the isAdd value.
   * 
   * @return Returns the isAdd.
   */
  public boolean isAdd()
  {
    return isAdd;
  }
}