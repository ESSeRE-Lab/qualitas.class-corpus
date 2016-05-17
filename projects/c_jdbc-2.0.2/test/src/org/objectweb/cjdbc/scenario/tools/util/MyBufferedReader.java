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

package org.objectweb.cjdbc.scenario.tools.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * I/O utilitary method.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 */
public class MyBufferedReader extends BufferedReader
{
  /** Begin request token. */
  private static final String BEGIN_REQUEST = "#begin";

  /** End request token. */
  private static final String END_REQUEST = "#end";

  /**
   * File title (eg: 'requests'). Allow to contruct better error messages.
   */
  private String fileDescription;

  /**
   * Creates a new <code>MyBufferedReader</code> instance.
   * 
   * @param reader a <code>Reader</code> instance.
   * @param description description text.
   */
  public MyBufferedReader(Reader reader, String description)
  {
    super(reader);
    fileDescription = description;
  }

  /**
   * Convenient method to read a boolean value in a text file.
   * 
   * @return the <code>boolean</code> read.
   * @throws IOException if error occurs.
   */
  public boolean readBoolean() throws IOException
  {
    String line = readLine();
    if ("true".equals(line))
      return true;
    else if ("false".equals(line))
      return false;
    throw new IOException(
      "Syntax error in "
        + fileDescription
        + " file: unknow token '"
        + line
        + "' found, expected 'true' or 'false'");
  }

  /**
   * Convenient method to read a <code>Strong</code> value in a text file.
   * 
   * @param stringName name of the property to read.
   * @return the <code>String</code> read.
   * @throws IOException if error occurs.
   */
  public String readString(String stringName) throws IOException
  {
    String line = readLine();

    if ((line == null) || line.equals(""))
      throw new IOException(
        "Syntax error in "
          + fileDescription
          + " file: "
          + stringName
          + " missing");
    return line;
  }

  /**
   * Convenient method to read a SQL request in a text file. The request must
   * be delimited by the {@link #BEGIN_REQUEST}and {@link #END_REQUEST}
   * tokens.
   * 
   * @param line text file line.
   * @return the <code>String</code> read.
   * @throws IOException if error occurs.
   */
  public String readSQLRequest(String line) throws IOException
  {
    if (!BEGIN_REQUEST.equals(line))
      throw new IOException(
        "Syntax error in requests file: '"
          + BEGIN_REQUEST
          + "' token expected instead of '"
          + line
          + "')");

    StringBuffer buffer = new StringBuffer();
    while (((line = readLine()) != null) && !line.equals(END_REQUEST))
    {
      buffer.append(line);
      buffer.append(System.getProperty("line.separator"));
    }
    String request = buffer.toString();

    if (!END_REQUEST.equals(line))
      throw new IOException(
        "Syntax error in requests file: '" + END_REQUEST + "' token not found");

    return request;
  }
  
  /**
   * Get the next non-commented line
   * @return the next non commented line
   * @throws IOException if error occurs
   */
  public String readNextLine() throws IOException
  {
    String line = this.readLine();
    while((line!=null)&&(line.startsWith("//")||line.startsWith("#")))
      line = readLine();
    return line;
  }
  
}