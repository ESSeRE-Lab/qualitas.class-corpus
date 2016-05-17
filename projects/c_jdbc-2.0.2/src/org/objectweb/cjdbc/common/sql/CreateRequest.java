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
 * Initial developer(s): Julie Marguerite.
 * Contributor(s): Mathieu Peltier, Emmanuel Cecchet.
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

/**
 * A <code>CreateRequest</code> is a SQL request of the following syntax:
 * 
 * <pre>
 *  CREATE [TEMPORARY] TABLE table-name [(column-name column-type [,column-name colum-type]* [,table-constraint-definition]*)]
 * </pre>
 * 
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class CreateRequest extends AbstractWriteRequest implements Serializable
{
  private static final long       serialVersionUID    = 2953585153643414325L;

  /** The table to create. */
  private transient DatabaseTable table               = null;

  /**
   * List of tables used to fill the created table in case of create query
   * containing a select.
   */
  private transient ArrayList     fromTables          = null;

  /**
   * alterDatabaseSchema is true if this create request alters the current
   * database schema (using create table, create schema, create view) and false
   * otherwise (create database, create index, create function, create method,
   * create role, create trigger, create type).
   * <p>
   * To force a refresh in case we don't understand the query, we default to
   * true.
   */
  private boolean                 alterDatabaseSchema = true;

  /**
   * alterDefinitions is true if this create request alters the current database
   * definitions (using create function, create method, create trigger, create
   * type) and false otherwise (create database, create table, create schema,
   * create view, create index, create role).
   * <p>
   * To force a refresh in case we don't understand the query, we default to
   * true.
   */
  private boolean                 alterDefinitions    = true;

  /**
   * Creates a new <code>CreateRequest</code> instance. The caller must give
   * an SQL request, without any leading or trailing spaces and beginning with
   * 'create table ' (it will not be checked).
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
  public CreateRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator, DatabaseSchema schema, int granularity,
      boolean isCaseSensitive) throws SQLException
  {
    this(sqlQuery, escapeProcessing, timeout, lineSeparator);
    parse(schema, granularity, isCaseSensitive);
  }

  /**
   * Creates a new <code>CreateRequest</code> instance. The caller must give
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
   * @see #parse
   */
  public CreateRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator)
  {
    super(sqlQuery, escapeProcessing, timeout, lineSeparator,
        RequestType.CREATE);
  }

  /**
   * @see AbstractWriteRequest
   */
  public CreateRequest(CJDBCInputStream in) throws IOException
  {
    super(in, RequestType.CREATE);
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
    String sql = originalSQL.toLowerCase();

    // Strip create
    sql = sql.substring("create".length()).trim();

    // Check what kind of create we are facing
    if (sql.startsWith("database") || sql.startsWith("index")
        || sql.startsWith("unique") || sql.startsWith("role"))
    { // CREATE DATABASE, CREATE [UNIQUE] INDEX, CREATE ROLE does not alter
      // anything
      alterDatabaseSchema = false;
      alterDefinitions = false;
      return;
    }
    if (sql.startsWith("function") || sql.startsWith("method")
        || sql.startsWith("procedure") || sql.startsWith("trigger")
        || sql.startsWith("type"))
    { // CREATE FUNCTION, CREATE METHOD, CREATE PROCEDURE, CREATE TRIGGER and
      // CREATE TYPE only alters definitions
      alterDatabaseSchema = false;
      alterDefinitions = true;
      return;
    }
    if (sql.startsWith("schema") || sql.startsWith("view"))
    {
      alterDatabaseSchema = true;
      alterDefinitions = false;
      return;
    }

    // Let's try to check if we have a 'create [temporary] table '
    int tableIdx = sql.indexOf("table");
    if (tableIdx < 0)
      throw new SQLException("Unsupported CREATE statement: '" + sqlQuery + "'");

    //
    // Starting from here, everything is just to handle CREATE TABLE statements
    //

    // Strip up to 'table'
    sql = sql.substring(tableIdx + 5).trim();

    // Does the query contain a select?
    int selectIdx = sql.indexOf("select");
    if (selectIdx != -1 && sql.charAt(selectIdx + 6) != ' ')
      selectIdx = -1;

    if (isCaseSensitive) // Reverse to the original case
      sql = originalSQL.substring(originalSQL.length() - sql.length());

    if (selectIdx != -1)
    {
      // Get the table on which CREATE occurs
      int nextSpaceIdx = sql.indexOf(" ");
      tableName = sql.substring(0, nextSpaceIdx).trim();
      table = new DatabaseTable(tableName);
      // Parse the select
      sql = sql.substring(selectIdx).trim();
      SelectRequest select = new SelectRequest(sql, false, 60,
          getLineSeparator());
      select.parse(schema, granularity, isCaseSensitive);
      fromTables = select.getFrom();
      if (granularity > ParsingGranularities.TABLE)
      { // Update the columns and add them to the table
        columns = select.getSelect();
        int size = columns.size();
        for (int i = 0; i < size; i++)
        {
          TableColumn tc = (TableColumn) columns.get(i);
          table.addColumn(new DatabaseColumn(tc.getColumnName(), false));
        }
      }
    }
    else
    {
      // Get the table on which CREATE occurs
      // Look for the parenthesis
      int openParenthesisIdx = sql.indexOf("(");
      int closeParenthesisIdx = sql.lastIndexOf(")");
      if ((openParenthesisIdx == -1) && (closeParenthesisIdx == -1))
      {
        // no parenthesis found
        table = new DatabaseTable(sql.trim());
        if (granularity > ParsingGranularities.TABLE)
          columns = new ArrayList();
        return;
      }
      else if ((openParenthesisIdx == -1) || (closeParenthesisIdx == -1)
          || (openParenthesisIdx > closeParenthesisIdx))
      {
        throw new SQLException("Syntax error in this CREATE statement: '"
            + sqlQuery + "'");
      }
      else
      {
        tableName = sql.substring(0, openParenthesisIdx).trim();
      }
      table = new DatabaseTable(tableName);

      // Get the column names
      if (granularity > ParsingGranularities.TABLE)
      {
        columns = new ArrayList();
        sql = sql.substring(openParenthesisIdx + 1, closeParenthesisIdx).trim();
        StringTokenizer columnTokens = new StringTokenizer(sql, ",");
        String word;
        String lowercaseWord;
        StringTokenizer wordTokens = null;
        String token;
        DatabaseColumn col = null;

        while (columnTokens.hasMoreTokens())
        {
          token = columnTokens.nextToken().trim();

          // work around to prevent bug: if the request contains for example:
          // INDEX foo (col1,col2)
          // we have to merge the 2 tokens: 'INDEX foo (col1' and 'col2)'
          if ((token.indexOf("(") != -1) && (token.indexOf(")") == -1))
          {
            if (columnTokens.hasMoreTokens())
              token = token + "," + columnTokens.nextToken().trim();
            else
            {
              tableName = null;
              columns = null;
              throw new SQLException("Syntax error in this CREATE statement: '"
                  + sqlQuery + "'");
            }
          }

          // First word of the line: either a column name or
          // a table constraint definition
          wordTokens = new StringTokenizer(token, " ");
          word = wordTokens.nextToken().trim();
          lowercaseWord = word.toLowerCase();

          // If it's a constraint, index or check keyword do not do anything
          // else parse the line
          if (!lowercaseWord.equals("constraint")
              && !lowercaseWord.equals("index")
              && !lowercaseWord.equals("check"))
          {
            String columnName;
            boolean isUnique = false;
            // Check for primary key or unique constraint
            if (lowercaseWord.equals("primary")
                || lowercaseWord.startsWith("unique"))
            {

              // Get the name of the column
              openParenthesisIdx = token.indexOf("(");
              closeParenthesisIdx = token.indexOf(")");
              if ((openParenthesisIdx == -1) || (closeParenthesisIdx == -1)
                  || (openParenthesisIdx > closeParenthesisIdx))
              {
                tableName = null;
                columns = null;
                throw new SQLException(
                    "Syntax error in this CREATE statement: '" + sqlQuery + "'");
              }

              columnName = token.substring(openParenthesisIdx + 1,
                  closeParenthesisIdx).trim();

              int comma;
              while ((comma = columnName.indexOf(',')) != -1)
              {
                String col1 = columnName.substring(0, comma).trim();
                col = table.getColumn(col1);
                if (col == null)
                {
                  tableName = null;
                  columns = null;
                  throw new SQLException(
                      "Syntax error in this CREATE statement: '" + sqlQuery
                          + "'");
                }
                else
                  col.setIsUnique(true);
                columnName = columnName.substring(comma + 1);
              }

              // Set this column to unique
              col = table.getColumn(columnName);

              // Test first if dbTable contains this column. This can fail with
              // some invalid request, for example:
              // CREATE TABLE categories(id INT4, name TEXT, PRIMARY KEY((id))
              if (col == null)
              {
                tableName = null;
                columns = null;
                throw new SQLException(
                    "Syntax error in this CREATE statement: '" + sqlQuery + "'");
              }
              else
                col.setIsUnique(true);
            }
            else
            {
              // It's a column name
              columnName = word;

              if (!wordTokens.hasMoreTokens())
              {
                // at least type declaration is required
                tableName = null;
                columns = null;
                throw new SQLException(
                    "Syntax error in this CREATE statement: '" + sqlQuery + "'");
              }

              // Check for primary key or unique constraints
              do
              {
                word = wordTokens.nextToken().trim().toLowerCase();
                if (word.equals("primary") || word.startsWith("unique"))
                {
                  // Create the column as unique
                  isUnique = true;
                  break;
                }
              }
              while (wordTokens.hasMoreTokens());

              // Add the column to the parsed columns list and
              // to the create DatabaseTable
              columns.add(new TableColumn(tableName, columnName));
              table.addColumn(new DatabaseColumn(columnName, isUnique));
            }
          }
        }
      }
    }
    isParsed = true;
  }

  /**
   * Returns true if this create request alters the current database schema
   * (using create table, create schema, create view) and false otherwise
   * (create database, create index, create function, create method, create
   * procedure, create role, create trigger, create type).
   * 
   * @return Returns true if this query alters the database schema.
   */
  public boolean altersDatabaseSchema()
  {
    return alterDatabaseSchema;
  }

  /**
   * Returns true if this create request alters the current database definitions
   * (using create function, create method, create procedure, create trigger,
   * create type) and false otherwise (create database, create table, create
   * schema, create view, create index, create role).
   * 
   * @return Returns true if this query alters database definitions.
   */
  public boolean altersDefinitions()
  {
    return alterDefinitions;
  }

  /**
   * @see AbstractRequest#cloneParsing(AbstractRequest)
   */
  public void cloneParsing(AbstractRequest request)
  {
    if (!request.isParsed())
      return;
    CreateRequest createRequest = (CreateRequest) request;
    cloneTableNameAndColumns((AbstractWriteRequest) request);
    table = createRequest.getDatabaseTable();
    fromTables = createRequest.getFromTables();
    isParsed = true;
  }

  /**
   * Gets the database table created by this statement (in case of a CREATE
   * TABLE statement).
   * 
   * @return a <code>DatabaseTable</code> value
   */
  public DatabaseTable getDatabaseTable()
  {
    return table;
  }

  /**
   * Returns the list of tables used to fill the created table in case of create
   * query containing a select.
   * 
   * @return and <code>ArrayList</code>
   */
  public ArrayList getFromTables()
  {
    return fromTables;
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
      System.out.println("Created table: " + tableName);
    else
      System.out.println("No information about created table");

    if (columns != null)
    {
      System.out.println("Created columns:");
      for (int i = 0; i < columns.size(); i++)
        System.out.println("  "
            + ((TableColumn) columns.get(i)).getColumnName());
    }
    else
      System.out.println("No information about created columns");

    System.out.println();
  }

}