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

import org.objectweb.cjdbc.common.sql.DropRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.databases.AbstractDatabase;
import org.objectweb.cjdbc.scenario.tools.databases.RUBiSDatabase;
import org.objectweb.cjdbc.scenario.tools.util.MyBufferedReader;

/**
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 */
public class DropRequestTest extends NoTemplate
{
  /** File name containing the requests to test. */
  private static final String RUBIS_DROP_REQUESTS_FILE = getTextPath("RUBiS-drop-requests.txt");

  /** Database on which the requests are performed. */
  private AbstractDatabase    database;

  /** List of <code>ParsingResult</code> objects. */
  private ArrayList           results;

  static boolean              inited                   = false;

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
      String request = null, tableName, errorMessage;

      try
      {
        File file = new File(RUBIS_DROP_REQUESTS_FILE);
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
            results.add(new ParsingResult(request, true, tableName));
          }
          else
          {
            // Invalid request
            errorMessage = in.readString("error message");
            results.add(new ParsingResult(request, false, errorMessage));
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
   * org.objectweb.cjdbc.common.sql.DropRequest#parse(DatabaseSchema, int,
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
    DropRequest req = null;
    try
    {
      req = new DropRequest(sql, false, 0,
          System.getProperty("line.separator"), database.getSchema(),
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
      // check table
      assertEquals("Incorrect table found", req.getTableName(), result.table
          .getName());
    }
  }

  /**
   * Stores the expected result of the call to
   * {@link org.objectweb.cjdbc.common.sql.DropRequest#parse(DatabaseSchema, int, boolean)}
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

    /** Database table to delete the request is valid. */
    protected DatabaseTable table;

    /** Error message if the request is invalid. */
    protected String        errorMessage;

    /**
     * Creates a new <code>ParsingResult</code> instance.
     * 
     * @param request request to test.
     * @param isValid <code>true</code> if the request is valid.
     * @param s table name if the request is valid or error message if the
     *          request is invalid
     */
    protected ParsingResult(String request, boolean isValid, String s)
    {
      this.request = request;
      this.isValid = isValid;

      if (isValid)
      {
        table = database.getSchema().getTable(s);
        if (table == null)
          fail("Possible syntax error in sql requests file: '" + s
              + "' not found in database schema");
      }
      else
      {
        errorMessage = s;
      }
    }
  }
}