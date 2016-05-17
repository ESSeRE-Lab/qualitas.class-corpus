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
 * Initial developer(s): Marc Wick.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.standalone.sql.filters;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.objectweb.cjdbc.common.sql.filters.MacrosHandler;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * This class defines a MacrosHandlerTest
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class MacrosHandlerTest extends NoTemplate
{
  /**
   * @see org.objectweb.cjdbc.scenario.standalone.sql.filters.MacrosHandlerTest
   */
  public void testNowMacro()
  {
    MacrosHandler handler = new MacrosHandler(MacrosHandler.RAND_FLOAT, 120000,
        MacrosHandler.DATE_TIMESTAMP, MacrosHandler.DATE_DATE,
        MacrosHandler.DATE_TIMESTAMP, MacrosHandler.DATE_TIMESTAMP,
        MacrosHandler.DATE_TIMESTAMP);

    String select = " select With nothing to be Replaced";
    String replaced = handler.processMacros(select);
    assertEquals("", " select With nothing to be Replaced", replaced);

    // get rid of milliseconds, to compensate for time differences (hopefully)
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");

    String expectedResult = "";
    // we repeat the test twice if it failed, to compensate of time differences
    String update = "update TABLE set column = now() where name = 'AAA'";
    replaced = handler.processMacros(update);
    Date date = new Date();
    long td = date.getTime() - date.getTime() % 120000;
    String time = fmt.format(new Date(td));
    expectedResult = "update TABLE set column = {ts '" + time
        + "'} where name = 'AAA'";
    assertEquals("test replacement of now() " + replaced, replaced,
        expectedResult);

  }

  /**
   * Test CURRENT_TIMESTAMP macro
   */
  public void testTimeStampMacro()
  {
    MacrosHandler handler = new MacrosHandler(MacrosHandler.RAND_FLOAT, 120000,
        MacrosHandler.DATE_TIMESTAMP, MacrosHandler.DATE_DATE,
        MacrosHandler.DATE_TIMESTAMP, MacrosHandler.DATE_TIMESTAMP,
        MacrosHandler.DATE_TIMESTAMP);

    // get rid of milliseconds, to compensate for time differences (hopefully)
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");

    Date date = new Date();
    long td = date.getTime() - date.getTime() % 120000;
    String time = fmt.format(new Date(td));

    // get rid of milliseconds, to compensate for time differences (hopefully)
    fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");

    String update = "update table set column = CURRENT_TIMESTAMP";
    String replaced = handler.processMacros(update);
    date = new Date();
    td = date.getTime() - date.getTime() % 120000;
    time = fmt.format(new Date(td));
    String expectedResult = "update table set column = {ts '" + time + "'}";
    assertEquals("test replacement of CURRENT_TIMESTAMP " + replaced, replaced,
        expectedResult);
  }

  /**
   * @see org.objectweb.cjdbc.scenario.standalone.sql.filters.MacrosHandlerTest
   */
  public void testRandMacro() throws Exception
  {
    MacrosHandler handler = new MacrosHandler(MacrosHandler.RAND_FLOAT, 1000,
        MacrosHandler.DATE_DATE, MacrosHandler.DATE_DATE,
        MacrosHandler.DATE_TIMESTAMP, MacrosHandler.DATE_TIMESTAMP,
        MacrosHandler.DATE_TIMESTAMP);
    String query = " select With nothing to be Replaced";
    String replaced = handler.macroRand(query, null);
    assertEquals("", " select With nothing to be Replaced", replaced);
  }

  /**
   * @see org.objectweb.cjdbc.scenario.standalone.sql.filters.MacrosHandlerTest
   */
  public void testReplaceMacro()
  {
    MacrosHandler handler = new MacrosHandler(MacrosHandler.RAND_FLOAT, 1000,
        MacrosHandler.DATE_DATE, MacrosHandler.DATE_DATE,
        MacrosHandler.DATE_TIMESTAMP, MacrosHandler.DATE_TIMESTAMP,
        MacrosHandler.DATE_TIMESTAMP);

    String query = "update table set stringvalue= 'macros in string should not be replaced , like rand(), now() and current_timestamp'";
    String replaced = handler.processMacros(query);
    assertEquals("Query was replaced while it should not have", query, replaced);
  }
}