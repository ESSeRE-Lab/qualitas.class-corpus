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
 * Initial developer(s): Mathieu Peltier.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.standalone.sql.request;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.sql.CreateRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.databases.AbstractDatabase;
import org.objectweb.cjdbc.scenario.tools.databases.RUBiSDatabase;
import org.objectweb.cjdbc.scenario.tools.util.MyBufferedReader;

/**
 * <code>CreateRequest</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 *         org.objectweb.cjdbc.common.sql..CreateRequest
 */
public class CreateRequestTest extends NoTemplate
{
  /** File name containing the requests to test. */
  private static final String RUBIS_CREATE_REQUESTS_FILE = getTextPath("RUBiS-create-requests.txt");

  /** Null value to used in the requests file if needed. */
  private static final String EMPTY_VALUE                = "null";

  /** Database on which the requests are performed. */
  private AbstractDatabase    database;

  /** List of <code>ParsingResult</code> objects. */
  private ArrayList           results;

  private static boolean      inited                     = false;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {

    synchronized (this)
    {
      if (inited)
        return;
      database = new RUBiSDatabase();
      results = new ArrayList();
      String request = null, tableName, columnList, errorMessage;

      try
      {
        File file = new File(RUBIS_CREATE_REQUESTS_FILE);
        MyBufferedReader in = new MyBufferedReader(new FileReader(file),
            "requests");

        String line;
        while ((line = in.readLine()) != null)
        {
          if (line.trim().equals("") || line.startsWith("//"))
            continue;

          // Get the request
          request = null;
          request = in.readSQLRequest(line);

          // Get expected results for this request
          if (in.readBoolean())
          {
            // Valid request
            tableName = in.readString("table name");
            columnList = in.readString("column list");
            results.add(new ParsingResult(request, tableName, columnList));
          }
          else
          {
            // Invalid request
            errorMessage = in.readString("errorMessage");
            results.add(new ParsingResult(request, errorMessage));
          }
        }
      }
      catch (IOException e)
      {
        String error = "An error occurs while parsing requests file: " + e;
        if (request != null)
          error += " (request: '" + request + "')";
        fail(error);
      }
      inited = true;
    }

  }

  /**
   * org.objectweb.cjdbc.common.sql.CreateRequest#parse(DatabaseSchema, int,
   * boolean)
   */
  public void testParseCreateRequest()
  {
    Iterator it = results.iterator();
    while (it.hasNext())
    {
      parse((ParsingResult) it.next(), false);
    }
  }

  /**
   * Perfoms the parsing test.
   * 
   * @param result expected result
   * @param isCaseSensitive <code>true</code> if the parsing must be case
   *          sensitive.
   */
  private void parse(ParsingResult result, boolean isCaseSensitive)
  {
    String sql = result.request.toLowerCase().trim();
    CreateRequest req = null;
    try
    {
      req = new CreateRequest(sql, false, 0, System
          .getProperty("line.separator"), database.getSchema(),
          ParsingGranularities.COLUMN_UNIQUE, isCaseSensitive);
    }
    catch (SQLException e)
    {
      if (result.isValid)
      {
        e.printStackTrace();
        fail("Exception thrown with valid request '" + result.request + "' ("
            + e + ")");
      }
      else
      {
        assertEquals(
            "Incorrect error message found while parsing this CREATE statement: '"
                + result.request + "'", result.errorMessage, e.getMessage());
        return;
      }
    }

    if (!result.isValid)
      fail("SQLException not thrown with invalid request: '" + result.request
          + "'");
    else
    {
      assertEquals(
          "Incorrect table name found while parsing this CREATE statement: '"
              + result.request + "'", result.table.getName(), req
              .getDatabaseTable().getName());

      assertEquals(
          "Incorrect columns found while parsing this CREATE statement: '"
              + result.request + "'", result.table.getXml(), req
              .getDatabaseTable().getXml());

      assertEquals(
          "Incorrect table found while parsing this CREATE statement: '"
              + result.request + "'", result.table, req.getDatabaseTable());
    }
  }

  /**
   * Stores the expected result of the call to
   * 
   * @link org.objectweb.cjdbc.common.sql.CreateRequest#parse(DatabaseSchema,
   *       int, boolean) method.
   * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
   */
  protected class ParsingResult
  {
    /** Request to test. */
    protected String        request;

    /** <code>true</code> if the request is valid. */
    protected boolean       isValid;

    /** Database table to create if the request is valid. */
    protected DatabaseTable table;

    /** Error message if the request is invalid. */
    protected String        errorMessage;

    /**
     * Creates a new <code>ParsingResult</code> instance for valid request.
     * 
     * @param request valid request to test.
     * @param tableName database name to test.
     * @param columnList column list (eg: col1.unique col2 col3). '.unique'
     *          means that the column is unique.
     */
    protected ParsingResult(String request, String tableName, String columnList)
    {
      this.request = request;
      isValid = true;

      // Create new table
      table = new DatabaseTable(tableName);

      // Parse columns to create
      if (!columnList.equals(EMPTY_VALUE))
      {
        String columnName, s;
        StringTokenizer tokenizer = new StringTokenizer(columnList.trim(), " ");
        boolean isUnique = false;
        int i;
        while (tokenizer.hasMoreTokens())
        {
          s = tokenizer.nextToken();
          i = s.indexOf(".");
          if (i != -1)
          {
            columnName = s.substring(0, i);
            s = s.substring(i + 1, s.length());
            if ("unique".equals(s))
              isUnique = true;
            else
              fail("Syntax error in sql requests file ('unique' token expected instead of: '"
                  + s + "' for request '" + request + "')");
          }
          else
          {
            isUnique = false;
            columnName = s;
          }
          table.addColumn(new DatabaseColumn(columnName, isUnique));
        }
      }
    }

    /**
     * Creates a new <code>ParsingResult</code> instance for invalid request.
     * 
     * @param request invalid request to test.
     * @param errorMessage error message.
     */
    protected ParsingResult(String request, String errorMessage)
    {
      this.request = request;
      isValid = false;
      this.errorMessage = errorMessage;
    }
  }
}