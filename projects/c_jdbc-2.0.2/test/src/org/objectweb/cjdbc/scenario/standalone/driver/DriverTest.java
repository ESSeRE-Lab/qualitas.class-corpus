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
import java.sql.SQLException;

import org.objectweb.cjdbc.driver.Driver;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.util.MyBufferedReader;

/**
 * <code>Driver</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @see org.objectweb.cjdbc.driver.Driver
 */
public class DriverTest extends NoTemplate
{
  /** File name containing the C-JDBC URLs to test. */
  public static final String URLS_FILE = getTextPath("urls.txt");

  /** Driver to test. */
  private Driver             driver;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    driver = new Driver();
  }

  /**
   * @see org.objectweb.cjdbc.driver.Driver#acceptsURL(String)
   */
  public void testAcceptURL()
  {
    String url = null;
    int countTest = 0;
    try
    {
      File file = new File(URLS_FILE);
      MyBufferedReader in = new MyBufferedReader(new FileReader(file), "URLs");
      String line;
      while ((line = in.readLine()) != null)
      {
        if (line.equals("") || line.startsWith("//"))
          continue;
        url = line;
        System.out.println("Test[" + (countTest++) + "]:" + url);
        boolean valid = in.readBoolean();
        try
        {
          if (valid)
          {
            assertTrue("Failed to accept a valid URL", driver.acceptsURL(url));
            in.readLine();
            in.readLine();
            in.readLine();
          }
          else
          {
            assertFalse("Accepted an incorrect URL", driver.acceptsURL(url));
          }
        }
        catch (SQLException e)
        {
          fail("Unexpected exception thrown: " + e);
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
  }
}
