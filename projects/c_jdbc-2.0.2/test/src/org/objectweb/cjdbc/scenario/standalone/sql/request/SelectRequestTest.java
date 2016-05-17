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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.RequestType;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.schema.TableColumn;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.databases.AbstractDatabase;
import org.objectweb.cjdbc.scenario.tools.databases.RUBiSDatabase;
import org.objectweb.cjdbc.scenario.tools.util.MyBufferedReader;

/**
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 *         org.objectweb.cjdbc.common.sql..SelectRequest
 */
public class SelectRequestTest extends NoTemplate
{
  /** File name containing the requests to test. */
  private static final String RUBIS_SELECT_REQUESTS_FILE = getTextPath("RUBiS-select-requests.txt");

  /** Null value to used in the requests file if needed. */
  private static final String EMPTY_VALUE                = "null";

  /** Database on which the requests are performed. */
  private AbstractDatabase    database;

  /** List of <code>ParsingResult</code> objects. */
  private ArrayList           results;

  static boolean              inited                     = false;

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

      String request = null, selectColumns, fromTables, whereColumns, errorMessage;
      int requestType = 0;

      try
      {
        File file = new File(RUBIS_SELECT_REQUESTS_FILE);
        MyBufferedReader in = new MyBufferedReader(new FileReader(file),
            "requests");

        String line;
        while ((line = in.readLine()) != null)
        {
          if (line.trim().equals("") || line.startsWith("//"))
            continue;

          // get the request
          request = null;
          request = in.readSQLRequest(line);

          // get expected results for this request
          if (in.readBoolean())
          {
            // valid request
            selectColumns = in.readString("columns selected in SELECT clause");
            fromTables = in.readString("tables selected in FROM clause");
            whereColumns = in.readString("columns selected in where clause");

            if (selectColumns.equals(EMPTY_VALUE))
              selectColumns = "";
            if (fromTables.equals(EMPTY_VALUE))
              fail("Syntax error in requests file (tables selected in FROM clause missing for request '"
                  + request + "')");
            if (whereColumns.equals(EMPTY_VALUE))
              whereColumns = "";

            line = in.readLine();
            if ("CACHEABLE".equals(line))
              requestType = RequestType.CACHEABLE;
            else if ("UNCACHEABLE".equals(line))
              requestType = RequestType.UNCACHEABLE;
            else if ("UNIQUE_CACHEABLE".equals(line))
              requestType = RequestType.UNIQUE_CACHEABLE;
            else
              fail("Syntax error in requests file (unknow request type: '"
                  + requestType + "')");

            results.add(new ParsingResult(request, selectColumns, fromTables,
                whereColumns, requestType));
          }
          else
          {
            // invalid request
            errorMessage = in.readString("error message");
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
   * org.objectweb.cjdbc.common.sql.SelectRequest#parse(DatabaseSchema, int,
   * boolean)
   */
  public void testParse()
  {
    Iterator it = results.iterator();
    int testCount = 0;
    while (it.hasNext())
    {
      testCount++;
      //System.out.println("Test:" + testCount);
      parse((ParsingResult) it.next(), false);

    }
  }

  private void parse(ParsingResult result, boolean isCaseSensitive)
  {
    String sql = result.request.toLowerCase().trim();

    SelectRequest req = null;
    try
    {
      req = new SelectRequest(sql, false, 0, System
          .getProperty("line.separator"), database.getSchema(),
          ParsingGranularities.COLUMN_UNIQUE, isCaseSensitive);

      //System.out.println("pkvalue="+req.getPkValue());
    }
    catch (Exception e)
    {
      e.printStackTrace();
      if (result.isValid)
      {
        fail("Exception thrown with valid request '" + result.request + "' ("
            + e + ")");
      }
      else
      {
        assertEquals(
            "Incorrect error message found while parsing this DELETE statement: '"
                + result.request + "'", result.errorMessage, e.getMessage());
        return;
      }
    }

    if (!result.isValid)
      fail("SQLException not thrown with invalid request: '" + result.request
          + "'");
    else
    {
      // the parsing does not guarantee the order so we don't compare directly
      // the ArrayList

      Iterator it;
      TableColumn c;
      String s;

      // check select ArrayList
      it = result.select.iterator();
      while (it.hasNext())
      {
        c = (TableColumn) it.next();
        assertTrue("'" + c.getColumnName()
            + "' column not selected by parsing for request: '" + sql + "'",
            req.getSelect().contains(c));
      }

      // check from ArrayList
      it = result.from.iterator();
      while (it.hasNext())
      {
        s = (String) it.next();
        assertTrue("'" + s + "' table not selected by parsing for request: '"
            + sql + "'", req.getFrom().contains(s));
      }

      // check where ArrayList
      it = result.where.iterator();
      while (it.hasNext())
      {
        c = (TableColumn) it.next();
        //System.out.println("I should find column("+c.getColumnName()+") in
        // result");
        assertTrue("'" + c.getColumnName()
            + "' column not selected by parsing for request: '" + sql + "'",
            req.getWhere().contains(c));
      }

      // check request type
      //System.out.println("RequestType:" + result.requestType);
      //System.out.println("CacheAbility:" + req.getCacheAbility());
      assertEquals("Incorrect request type for request '" + sql + "'",
          result.requestType, req.getCacheAbility());
    }
  }

  /**
   * Stores the expected result of the call to
   * {@link org.objectweb.cjdbc.common.sql.SelectRequest#parse(DatabaseSchema, int, boolean)}
   * method.
   * 
   * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
   */
  protected class ParsingResult
  {
    /** Request to test. */
    protected String    request;

    /** <code>true</code> if the request is valid. */
    protected boolean   isValid;

    /**
     * Columns concerned by the <code>SELECT</code> clause:
     * <code>ArrayList</code> of <code>TableColumn</code> objects.
     */
    protected ArrayList select;

    /**
     * Columns concerned by the <code>FROM</code> clause:
     * <code>ArrayList</code> of <code>AliasedDatabaseTable</code> objects.
     */
    protected ArrayList from;

    /**
     * Columns concerned by the <code>WHERE</code> clause:
     * <code>ArrayList</code> of <code>TableColumn</code> objects.
     */
    protected ArrayList where;

    /**
     * Request type: either CACHEABLE, UNCACHEABLE or UNIQUE_CACHEABLE.
     */
    protected int       requestType;

    /** Error message if the request is invalid. */
    protected String    errorMessage;

    /**
     * Creates a new <code>ParsingResult</code> instance for valid request.
     * 
     * @param request request to test.
     * @param selectColumns columns concerned by the <code>SELECT</code>
     *          clause.
     * @param fromTables columns concerned by the <code>FROM</code> clause.
     * @param whereColumns columns concerned by the <code>WHERE</code> clause.
     * @param requestType request type.
     */
    protected ParsingResult(String request, String selectColumns,
        String fromTables, String whereColumns, int requestType)
    {
      StringTokenizer tokenizer;
      String s, tableName, columnName;
      int i;

      this.request = request;
      isValid = true;

      // Parse selectColumns
      select = new ArrayList();
      tokenizer = new StringTokenizer(selectColumns.trim(), " ");
      while (tokenizer.hasMoreTokens())
      {
        s = tokenizer.nextToken();
        i = s.indexOf(".");
        if (i == -1)
          fail("Syntax error in sql requests file: . needed in " + s);

        tableName = s.substring(0, i);
        columnName = s.substring(i + 1, s.length());

        if (database.getSchema().getTable(tableName).getColumn(columnName) != null)
        {
          select.add(new TableColumn(tableName, columnName));
        }
        else
        {
          if (database.getSchema().getTable(tableName) != null)
            fail("Possible syntax error in sql requests file: '" + columnName
                + "' not found in table " + tableName);
          else
            fail("Possible syntax error in sql requests file: '" + tableName
                + "' not found in database schema");
        }
      }

      // Parse fromTables
      from = new ArrayList();
      tokenizer = new StringTokenizer(fromTables.trim(), " ");
      while (tokenizer.hasMoreTokens())
      {
        tableName = tokenizer.nextToken();

        if (database.getSchema().getTable(tableName) == null)
          fail(tableName + " not found in database schema");
        else
          from.add(tableName);
      }

      // Parse whereColumns
      where = new ArrayList();
      tokenizer = new StringTokenizer(whereColumns.trim(), " ");
      while (tokenizer.hasMoreTokens())
      {
        s = tokenizer.nextToken();
        i = s.indexOf(".");
        if (i == -1)
          fail("Syntax error in sql requests file: . needed in " + s);

        tableName = s.substring(0, i);
        columnName = s.substring(i + 1, s.length());

        if (database.getSchema().getTable(tableName).getColumn(columnName) != null)
        {
          where.add(new TableColumn(tableName, columnName));
        }
        else
        {
          if (database.getSchema().getTable(tableName) != null)
            fail("Possible syntax error in sql requests file: '" + columnName
                + "' not found in table '" + tableName + "'");
          else
            fail("Possible syntax error in sql requests file: '" + tableName
                + "' not found in database schema");
        }
      }

      this.requestType = requestType;
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