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

package org.objectweb.cjdbc.scenario.standalone.driver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.driver.CjdbcUrl;
import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.util.MyBufferedReader;

/**
 * <code>CjdbcUrl</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@emicnetworks.com">Mathieu Peltier
 *         </a>
 * @see org.objectweb.cjdbc.driver.Driver
 */
public class CjdbcUrlTest extends NoTemplate
{
  /** File name containing the C-JDBC URLs to test. */
  public static final String URLS_FILE    = getTextPath("urls.txt");

  /** Empty params token to use in the URLs file. */
  public static final String EMPTY_PARAMS = "<empty>";

  /** Driver to test. */
  private CjdbcUrl           cjdbcUrl;

  /**
   * @see org.objectweb.cjdbc.driver.CjdbcUrl#parseUrl()
   */
  public void testParseUrl()
  {
    // parse urls file
    ArrayList results = new ArrayList();

    String url = null;
    try
    {
      File file = new File(URLS_FILE);
      MyBufferedReader in = new MyBufferedReader(new FileReader(file), "URLs");

      String line;
      String databaseName = null;
      String controllerList = null;
      String paramList = null;

      while ((line = in.readLine()) != null)
      {
        if (line.equals("") || line.startsWith("//"))
          continue;

        url = line;
        if (in.readBoolean())
        {
          databaseName = in.readString("database name");
          controllerList = in.readString("controller list");
          paramList = in.readString("param list");
          if (paramList.equals(EMPTY_PARAMS))
          {
            paramList = "";
          }
          results.add(new ParsingResult(url, databaseName, controllerList,
              paramList));
        }
      }
    }
    catch (IOException e)
    {
      if (url == null)
        fail("An error occurs while parsing urls file: " + e);
      else
        fail("An error occurs while parsing urls file: " + e + " (URL: '" + url
            + "')");
    }

    // perform test on urls
    Iterator it = results.iterator();
    ParsingResult result;
    ControllerInfo[] controllerList;

    int countTest = 0;
    while (it.hasNext())
    {
      result = (ParsingResult) it.next();
      if (result.isValid)
      {
        try
        {
          System.out.println("Test[" + (countTest++) + "]:" + result.url);
          cjdbcUrl = new CjdbcUrl(result.url);
          assertEquals("Incorrect database name", result.databaseName, cjdbcUrl
              .getDatabaseName());

          controllerList = cjdbcUrl.getControllerList();
          for (int i = 0; i < controllerList.length; i++)
          {
            assertEquals("Incorrect hostname in controller list",
                result.hosts[i], controllerList[i].getHostname());
            assertEquals("Incorrect hostname in controller list",
                result.ports[i], controllerList[i].getPort());
          }

          assertEquals("Params list was different than expected", cjdbcUrl
              .getParameters(), result.parameters);
        }
        catch (Exception e)
        {
          fail("Unexpected exception thrown: " + e);
        }
      }
      else
      {
        try
        {
          cjdbcUrl = new CjdbcUrl(result.url);
          fail("Exception not thrown with illegal URL '" + result.url + "'");
        }
        catch (Exception e)
        {
        }
      }
    }
  }

  /**
   * Stores the expected result of the call to
   * {@link org.objectweb.cjdbc.driver.CjdbcUrl#parseUrl()}method.
   * 
   * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
   */
  protected class ParsingResult
  {
    /** URL to test. */
    private String    url;

    /** <code>true</code> if the URL is valid. */
    private boolean   isValid;

    /** Database name. */
    private String    databaseName;

    /** Controllers list. */
    private String[]  hosts;
    private int[]     ports;

    /** Parameters list */
    private Hashtable parameters;

    /**
     * Creates a new <code>ParsingResult</code> instance for bad URL.
     * 
     * @param url URL to test.
     */
    protected ParsingResult(String url)
    {
      this.url = url;
      this.isValid = false;
    }

    /**
     * Creates a new <code>ParsingResult</code> instance for valid URL.
     * 
     * @param url URL to test.
     * @param databaseName database name.
     * @param controllerList controllers list.
     */
    protected ParsingResult(String url, String databaseName,
        String controllerList, String paramList)
    {
      this.url = url;
      this.isValid = true;
      this.databaseName = databaseName;

      // parse controller list
      StringTokenizer tokenizer = new StringTokenizer(controllerList, " ");
      hosts = new String[tokenizer.countTokens()];
      ports = new int[tokenizer.countTokens()];
      int i = 0;
      while (tokenizer.hasMoreTokens())
      {
        StringTokenizer hostPort = new StringTokenizer(tokenizer.nextToken(),
            ":");
        hosts[i] = hostPort.nextToken();
        ports[i] = Integer.parseInt(hostPort.nextToken());
        i++;
      }

      // parse param list
      parameters = new Hashtable();
      tokenizer = new StringTokenizer(paramList, " ");
      while (tokenizer.hasMoreTokens())
      {
        StringTokenizer pp = new StringTokenizer(tokenizer.nextToken(), "=");
        if (pp.hasMoreTokens())
        {
          String param = pp.nextToken();
          String value = "";
          if (pp.hasMoreTokens())
            value = pp.nextToken();
          parameters.put(param, value);
        }
      }
    }
  }
}
