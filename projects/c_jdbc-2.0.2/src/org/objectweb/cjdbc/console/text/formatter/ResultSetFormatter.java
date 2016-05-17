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
 * Initial developer(s): Jeff Mesnil.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.formatter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.ColorPrinter;
import org.objectweb.cjdbc.console.text.Console;
import org.objectweb.cjdbc.console.text.ConsoleException;

/**
 * Utility class to format a <code>ResultSet</code> to display it prettily in the text console
 * 
 * @author <a href="mailto:jeff.mesnil@emicnetworks.com">Jeff Mesnil</a>
 */
public class ResultSetFormatter
{
  /** Max column width when displaying a <code>ResultSet</code>. */
  private static final int MAX_COLUMN_DISPLAY_WIDTH = 25;

  /**
   * Format and display the given <code>ResultSet</code> on the console.
   * 
   * @param rs the <code>ResultSet</code> to display
   * @param fetchsize fetchisze value
   * @param console console where the ResultSet will be displayed
   * and he size of the result set)
   * 
   * @throws SQLException if an error occurs
   */
  public static void formatAndDisplay(ResultSet rs, int fetchsize, Console console) throws SQLException
  {
    // Get the metadata
    ResultSetMetaData meta = rs.getMetaData();
    int columnCount = meta.getColumnCount();
  
    console.println();
    
    appendSeparatorLine(columnCount, meta, console);
  
    // Print the column names
    console.print("|", ColorPrinter.STATUS);
    for (int i = 1; i <= columnCount; i++)
    {
      console.print(" ");
      // Pad the column name and print it
      int size = meta.getColumnDisplaySize(i);
      String columnName = meta.getColumnName(i);
      if (size <= 0)
      {
        if (columnName != null)
          size = columnName.length();
        else
          size = 0;
      }
      appendPad(columnName, size, console);
      console.print(" |", ColorPrinter.STATUS);
    }
    console.println();
  
    appendSeparatorLine(columnCount, meta, console);
  
    // Display the results
    Object object;
    int line = 0;
    while (rs.next())
    {
      console.print("|", ColorPrinter.STATUS);
      for (int i = 1; i <= columnCount; i++)
      {
        console.print(" ");
        object = rs.getObject(i);
        String value = (object != null) ? rs.getObject(i).toString() : "";
        // Pad the value and print it
        int size = meta.getColumnDisplaySize(i);
        if (size <= 0)
        {
          if (value != null)
            size = value.length();
          else
            size = 0;
        }
        appendPad(value, size, console);
        console.print(" |", ColorPrinter.STATUS);
      }
      console.println();
      line++;
      if (fetchsize != 0)
      {
        if (line % fetchsize == 0)
        {
          try
          {
            console.readLine(ConsoleTranslate.get("sql.display.next.rows",
                new Integer[]{new Integer(fetchsize), new Integer(line)}));
          }
          catch (ConsoleException ignore)
          {
          }
        }
      }
    }
  
    appendSeparatorLine(columnCount, meta, console);
  }

  private static void appendSeparatorLine(int columnCount,
      ResultSetMetaData meta, Console console) throws SQLException
  {
  
    console.print("+", ColorPrinter.STATUS);
    for (int i = 1; i <= columnCount; i++)
    {
      int size = meta.getColumnDisplaySize(i);
      if (size > MAX_COLUMN_DISPLAY_WIDTH)
        size = MAX_COLUMN_DISPLAY_WIDTH;
      console.print("-", ColorPrinter.STATUS);
      for (int j = 0; j < size; j++)
        console.print("-", ColorPrinter.STATUS);
      console.print("-+", ColorPrinter.STATUS);
    }
    console.println();
  }

  private static void appendPad(String text, int size, Console console)
  {
    if (size > MAX_COLUMN_DISPLAY_WIDTH)
      size = MAX_COLUMN_DISPLAY_WIDTH;
    if (size < text.length())
    {
      console.print(text.substring(0, size - 1) + "~", ColorPrinter.STATUS);
      return;
    }
    StringBuffer toPad = new StringBuffer(size);
    toPad.insert(0, text);
    while (toPad.length() < size)
      toPad.append(' ');
    console.print(toPad.toString(), ColorPrinter.STATUS);
  }
}
