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
 * Initial developer(s): Mathieu Peltier
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.standalone.sql.request;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.UpdateRequest;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.sql.schema.TableColumn;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.databases.AbstractDatabase;
import org.objectweb.cjdbc.scenario.tools.databases.RUBiSDatabase;
import org.objectweb.cjdbc.scenario.tools.util.MyBufferedReader;

/**
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 *         org.objectweb.cjdbc.common.sql..UpdateRequest
 */
public class UpdateRequestTest extends NoTemplate
{
  /** File name containing the requests to test. */
  private static final String RUBIS_UPDATE_REQUESTS_FILE = getTextPath("RUBiS-update-requests.txt");

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

      String request = null, tableName, columnList, updatedValues, updatedPk, errorMessage;
      boolean isUnique = false;

      try
      {
        File file = new File(RUBIS_UPDATE_REQUESTS_FILE);
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

            isUnique = in.readBoolean();

            updatedValues = in.readString("updated values");
            if (updatedValues.equals(EMPTY_VALUE))
              updatedValues = null;

            updatedPk = in.readString("updated pk");
            if (updatedPk.equals(EMPTY_VALUE))
              updatedPk = null;

            results.add(new ParsingResult(request, tableName, columnList,
                isUnique, updatedValues, updatedPk));
          }
          else
          {
            // invalid request
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
   * org.objectweb.cjdbc.common.sql.UpdateRequest#parse(DatabaseSchema, int,
   * boolean)
   */
  public void testParse()
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
    System.out.println("Parsing:"+sql);
    UpdateRequest req = null;
    try
    {
      req = new UpdateRequest(sql, false, 0, System
          .getProperty("line.separator"), database.getSchema(),
          ParsingGranularities.COLUMN_UNIQUE, isCaseSensitive);
    }
    catch (SQLException e)
    {
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
      // Check table
      assertEquals("Incorrect table found", req.getTableName(), result.table
          .getName());

      // Check columns ArrayList (the parsing does not guaranty the order so
      // we don't compare directly the ArrayList)
      TableColumn c;
      Iterator it = result.columns.iterator();
      while (it.hasNext())
      {
        c = (TableColumn) it.next();
        assertTrue("'" + c.getColumnName()
            + "' column not selected by parsing for request: '" + sql + "'",
            req.getColumns().contains(c));
      }

      // Check unicity
      assertEquals("Unicity not correct for request: '" + sql + "'",
          result.isUnique, req.isUnique());

      // Check updated values
      if (result.updatedValues != null)
      {
        HashMap hash = req.getUpdatedValues();
        ArrayList al = new ArrayList(req.getUpdatedValues().keySet());
        Object[] obs = al.toArray();
        Arrays.sort(obs, new MyComparator());
        al = new ArrayList(Arrays.asList(obs));
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < al.size(); i++)
        {
          if (i != 0)
            buf.append(",");
          buf.append(al.get(i) + "=" + hash.get(al.get(i)));
        }
        //System.out.println("GOT:" + buf.toString());
        //System.out.println("EXPECTED:" + result.updatedValues);
        assertEquals("Incorrect updatedValues for request: '" + sql + "'", buf
            .toString(), result.updatedValues);
      }
      else
      {
        assertNull("Incorrect not null updatedValues for request: '" + sql
            + "'", req.getUpdatedValues());
      }

      // Check updated pk
      //System.out.println("PK:" + req.getPk());
      assertEquals("Incorrect updated pk for request: '" + sql + "'", req
          .getPk(), result.updatedPk);
    }
  }

  class MyComparator implements Comparator
  {
    /**
     * Compare two <code>TableColumn</code> objects
     */
    public int compare(Object o1, Object o2)
    {
      if (o1 instanceof TableColumn && o2 instanceof TableColumn)
        return ((TableColumn) o1).getColumnName().compareTo(
            ((TableColumn) o2).getColumnName());
      else
        return (o1.toString().compareTo(o2.toString()));
    }
  }

  /**
   * Stores the expected result of the call to
   * {@link org.objectweb.cjdbc.common.sql.UpdateRequest#parse(DatabaseSchema, int, boolean)}
   * method.
   * 
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

    /** <code>ArrayList</code> of <code>TableColumn</code> objects. */
    protected ArrayList     columns;

    /** <code>true</code> if this query only concerns a single row. */
    protected boolean       isUnique;

    /** Values updated */
    protected String        updatedValues;

    /** Updated pk */
    protected String        updatedPk;

    /** Error message if the request is invalid. */
    protected String        errorMessage;

    /**
     * Creates a new <code>ParsingResult</code> instance for valid request.
     * 
     * @param request valid request to test.
     * @param tableName database name to test.
     * @param columnList column list (eg: col1.unique col2 col3). '.unique'
     *          means that the column is unique.
     * @param isUnique <code>true</code> if this query only concerns a single
     *          row.
     */
    protected ParsingResult(String request, String tableName,
        String columnList, boolean isUnique, String updatedValues,
        String updatedPk)
    {
      this.request = request;
      isValid = true;

      // Get table
      table = database.getSchema().getTable(tableName);
      if (table == null)
        fail("Possible syntax error in sql requests file: '" + tableName
            + "' not found in database schema");

      // Parse columns
      columns = new ArrayList();
      String columnName;
      StringTokenizer tokenizer = new StringTokenizer(columnList.trim(), " ");
      while (tokenizer.hasMoreTokens())
      {
        columnName = tokenizer.nextToken();
        if (table.getColumn(columnName) != null)
          columns.add(new TableColumn(tableName, columnName));
        else
          fail("Possible syntax error in sql requests file: '" + columnName
              + "' not found in table '" + tableName + "'");
      }

      this.isUnique = isUnique;
      this.updatedValues = (updatedValues == null) ? null : updatedValues
          .trim();
      this.updatedPk = updatedPk;
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