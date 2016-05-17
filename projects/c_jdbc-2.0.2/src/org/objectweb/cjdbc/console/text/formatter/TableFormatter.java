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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.formatter;

/**
 * Utility class to format a table which can be pretty displayed in the text console.
 * 
 * @author <a href="mailto:jeff.mesnil@emicnetworks.com">Jeff Mesnil</a>
 */
public class TableFormatter
{
  /**
   * Format as a table.
   * 
   * @param headers the headers of the table
   * @param cells the cells of the table
   * @param headersAsRow <code>true</code> if the headers must be displayed on the same row,
   *    <code>false</code> if the headers must be displayed on the same column
   *
   * @return a String representing a pretty formatted table
   */
  public static String format(String[] headers, String[][] cells,
      boolean headersAsRow)
  {
    StringBuffer buf = new StringBuffer();
    int[] longestLengths = findLongestDataLengths(headers, cells, headersAsRow);
    String horizontalRule = createHorizontalRule(longestLengths);
    if (headersAsRow)
    {
      buf.append(horizontalRule);
      appendHeadersTo(headers, buf, longestLengths);
      buf.append(horizontalRule);
      appendCellsTo(cells, buf, longestLengths);
      buf.append(horizontalRule);
    }
    else
    {
      appendHeaderAndCells(headers, cells, buf, longestLengths, horizontalRule);
    }
    return buf.toString();

  }

  /**
   * Create an horizontal rule following the format:
   * <code>+-----+-------+-----+</code>
   * where the size of each part is determinged based on the longest length
   * for each column
   * 
   * @param longestLengths an array of int corresponding to the longest length of cells for a given column
   * 
   * @return a <code>String</code> to used as an horizontal rule
   */
  private static String createHorizontalRule(int[] longestLengths)
  {
    StringBuffer buf = new StringBuffer("+");
    for (int i = 0; i < longestLengths.length; i++)
    {
      // we add 2 to the longest length for each column to put one
      // space before and after any cell content
      appendStringTo(buf, "-", longestLengths[i] + 2);
      buf.append("+");
    }
    buf.append("\n");
    return buf.toString();
  }

  
  /**
   * Append <code>n</code> times the <code>string</code> to <code>buf</code>.
   * 
   * @param buf a <code>StringBuffer</code>
   * @param string the <code>String</code> to append
   * @param n the number of times the string should be appended
   */
  private static void appendStringTo(StringBuffer buf, String string, int n)
  {
    for (int i = 0; i < n; i++)
    {
      buf.append(string);
    }
  }

  /**
   * Find the longest lengths for the data.
   * Used internally to create an horizontal rule and to
   * append whitespaces in each cell so that everything
   * is propertly formatted.
   * 
   * @param headers headers of the table
   * @param cells cells of the table
   * @param headersAsRow <code>true</code> if the headers must be displayed on the same row,
   *    <code>false</code> if the headers must be displayed on the same column
   *
   * @return an array of <code>int</code> containing the longest length for each column
   */
  private static int[] findLongestDataLengths(String[] headers,
      String[][] cells, boolean headersAsRow)
  {
    if (headersAsRow)
    {
      // there is has much columns as heafers.length
      int[] longestLengths = new int[headers.length];
      for (int j = 0; j < longestLengths.length; j++)
      {
        int maxLength = 0;
        maxLength = Math.max(maxLength, headers[j].length());
        for (int i = 0; i < cells.length; i++)
        {
          maxLength = Math.max(maxLength, cells[i][j].length());
        }
        longestLengths[j] = maxLength;
      }
      return longestLengths;
    }
    else
    {
      // there is only two columns:
      // - the first for the header
      // - the second for the cell
      int[] longestLengths = new int[2];
      longestLengths[0] = 0;
      for (int i = 0; i < headers.length; i++)
      {
        longestLengths[0] = Math.max(longestLengths[0], headers[i].length());
      }
      longestLengths[1] = 0;
      for (int i = 0; i < cells.length; i++)
      {
        for (int j = 0; j < cells[i].length; j++)
        {
          String cell = cells[i][j];
          longestLengths[1] = Math.max(longestLengths[1], cell.length());
        }
      }
      return longestLengths;
    }
  }

  
  /**
   * Append a table of cells to the buffer
   * 
   * @param cells the table of cells to append
   * @param buf the <code>StringBuffer</code> where to append
   * @param longestLengths used to fill each cell with whitespaces so
   * that everything is properly formatted
   */
  private static void appendCellsTo(String[][] cells, StringBuffer buf,
      int[] longestLengths)
  {
    for (int i = 0; i < cells.length; i++)
    {
      buf.append("| ");
      for (int j = 0; j < cells[i].length; j++)
      {
        String cell = cells[i][j];
        buf.append(cell);
        appendStringTo(buf, " ", longestLengths[j] - cell.length());
        buf.append(" | ");
      }
      buf.append("\n");
    }
  }

  /**
   * Append table's headers to the buffer
   * 
   * @param headers the headers of the table
   * @param buf the <code>StringBuffer</code> where to append
   * @param longestLengths used to fill each cell with whitespaces so
   * that everything is properly formatted
   */
  private static void appendHeadersTo(String[] headers, StringBuffer buf,
      int[] longestLengths)
  {
    buf.append("| ");
    for (int i = 0; i < headers.length; i++)
    {
      String header = headers[i];
      buf.append(header);
      appendStringTo(buf, " ", longestLengths[i] - header.length());
      buf.append(" | ");
    }
    buf.append("\n");
  }

  /**
   * Append table's headers and cells in one pass.
   * used when the headers should be displayed on the same colum
   * (i.e. <code>headersAsRow</code> was <code>false</code>).
   * 
   * @param headers the headers of the table
   * @param headers the cells of the table
   * @param buf the <code>StringBuffer</code> where to append
   * @param longestLengths used to fill each cell with whitespaces so
   * that everything is properly formatted
   * @param horizontalRule an horizontal rule
   */
  private static void appendHeaderAndCells(String[] headers, String[][] cells,
      StringBuffer buf, int[] longestLengths, String horizontalRule)
  {
    buf.append(horizontalRule);
    for (int i = 0; i < cells.length; i++)
    {
      for (int j = 0; j < cells[i].length; j++)
      {
        buf.append("| ");
        buf.append(headers[j]);
        appendStringTo(buf, " ", longestLengths[0] - headers[j].length());
        buf.append(" | ");
        String cell = cells[i][j];
        buf.append(cell);
        appendStringTo(buf, " ", longestLengths[1] - cell.length());
        buf.append(" |\n");
      }
      buf.append(horizontalRule);
    }
  }
}
