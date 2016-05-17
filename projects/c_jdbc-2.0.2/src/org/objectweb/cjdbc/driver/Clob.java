/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.driver;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.NotImplementedException;

/**
 * The representation (mapping) in the Java <sup><small>TM </small> </sup>
 * programming language of an SQL <code>CLOB</code> value. An SQL
 * <code>CLOB</code> is a built-in type that stores a Character Large Object
 * as a column value in a row of a database table. By default drivers implement
 * <code>Clob</code> using an SQL <code>locator(CLOB)</code>, which means
 * that a <code>Clob</code> object contains a logical pointer to the SQL
 * <code>CLOB</code> data rather than the data itself. A <code>Clob</code>
 * object is valid for the duration of the transaction in which is was created.
 * <p>
 * Methods in the interfaces {@link DriverResultSet},{@link CallableStatement},
 * and {@link PreparedStatement}, such as <code>getClob</code> and
 * <code>setClob</code> allow a programmer to access an SQL <code>CLOB</code>
 * value. The <code>Clob</code> interface provides methods for getting the
 * length of an SQL <code>CLOB</code> (Character Large Object) value, for
 * materializing a <code>CLOB</code> value on the client, and for determining
 * the position of a pattern of bytes within a <code>CLOB</code> value. In
 * addition, this interface has methods for updating a <code>CLOB</code>
 * value.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@emicnetworks.fr">Emmanuel Cecchet
 *         </a>
 * @since JDK 1.2
 */
public class Clob implements java.sql.Clob, Serializable
{
  private static final long serialVersionUID = 1832832422588968988L;

  /** The data represented as a string of this <code>CLOB</code> */
  private String            stringData       = null;

  /**
   * Creates a new <code>Clob</code> instance.
   * 
   * @param data a <code>String</code> of character data
   */
  public Clob(String data)
  {
    stringData = data;
  }

  /**
   * Returns the size of the <code>CLOB</code> value designated by this
   * <code>Clob</code> object
   * 
   * @return length of the <code>CLOB</code> value that this <code>clob</code>
   *         represents
   * @exception SQLException if there is an error accessing the length of the
   *              <code>CLOB</code>
   * @since JDK 1.2
   */
  public long length() throws SQLException
  {
    return stringData.length();
  }

  /**
   * Retrieves the <code>CLOB</code> value designated by this
   * <code>Clob</code> instance as a stream.
   * 
   * @return a stream containing the <code>CLOB</code> data
   * @exception SQLException if there is an error accessing the
   *              <code>CLOB</code> value
   * @since JDK 1.2
   */
  public java.io.InputStream getAsciiStream() throws SQLException
  {
    return new ByteArrayInputStream(stringData.getBytes());
  }

  /**
   * Materializes the <code>CLOB</code> value designated by this <Code>object
   * as a stream of Unicode character.
   * 
   * @return A reader object with all the data in the <code>CLOB</code> value
   *         designated by this clob object as unicode characters.
   * @exception SQLException if there is an error accessing the
   *              <code>CLOB</code> value
   */
  public java.io.Reader getCharacterStream() throws SQLException
  {
    return new StringReader(stringData);
  }

  /**
   * Returns a copy of the portion of the <code>CLOB</code> value represented
   * by this <code>CLOB</code> object that starts at position <i>position </i>
   * and has ip to <i>length </i> consecutive characters.
   * 
   * @param pos the position where to get the substring from
   * @param length the length of the substring
   * @return the substring
   * @exception SQLException if there is an error accessing the
   *              <code>CLOB</code>
   * @since JDK 1.2
   */
  public String getSubString(long pos, int length) throws SQLException
  {
    if (length > stringData.length())
      throw new SQLException("Clob contains only " + stringData.length()
          + " characters (asking for " + length + ").");
    return stringData.substring((int) pos, length);
  }

  /**
   * Retrieves the character position at which the specified string
   * <code>searchstr</code> begins within the <code>CLOB</code> value that
   * this <code>Clob</code> object represents. The search for
   * <code>searchstr</code> begins at position <code>start</code>.
   * 
   * @param searchstr the byte array for which to search
   * @param start the position at which to begin searching; the first position
   *          is 1
   * @return the position at which the pattern appears, else -1
   * @exception SQLException if there is an error accessing the
   *              <code>CLOB</code>
   * @since JDK 1.2
   */
  public long position(String searchstr, long start) throws SQLException
  {
    return stringData.indexOf(searchstr, (int) start);
  }

  /**
   * Retrieves the character position at which the specified <code>Clob</code>
   * object <code>searchstr</code> begins within the <code>CLOB</code> value
   * that this <code>Clob</code> object represents. The search for
   * <code>searchstr</code> begins at position <code>start</code>.
   * 
   * @param searchstr the byte array for which to search
   * @param start the position at which to begin searching; the first position
   *          is 1
   * @return the position at which the pattern appears, else -1
   * @exception SQLException if there is an error accessing the
   *              <code>CLOB</code>
   * @since JDK 1.2
   */
  public long position(java.sql.Clob searchstr, long start) throws SQLException
  {
    return position(searchstr.getSubString(0, (int) searchstr.length()),
        (int) start);
  }

  // -------------------------- JDBC 3.0 -----------------------------------

  /**
   * Retrieves a stream to be used to write Ascii characters to the CLOB value
   * that this Clob object represents, starting at position pos.
   * 
   * @param pos the position where to start the stream
   * @return the ascii outputstream to this <code>clob</code> object
   * @throws SQLException if there is an error accessing the <code>clob</code>
   */
  public OutputStream setAsciiStream(long pos) throws SQLException
  {
    throw new NotImplementedException("setAsciiStream");
  }

  /**
   * Retrieves a stream to be used to write a stream of Unicode characters to
   * the CLOB value that this Clob object represents, at position pos.
   * 
   * @param pos the position where to start the writer
   * @return the writer to this <code>clob</code> object
   * @throws SQLException if there is an error accessing the <code>clob</code>
   */
  public Writer setCharacterStream(long pos) throws SQLException
  {
    throw new NotImplementedException("setCharacterStream");
  }

  /**
   * Writes the given Java String to the CLOB value that this Clob object
   * designates at the position pos.
   * 
   * @param pos the position where to set the string
   * @param str string to insert in the <code>clob</code>
   * @return return value
   * @throws SQLException if there is an error accessing the <code>clob</code>
   */
  public int setString(long pos, String str) throws SQLException
  {
    throw new NotImplementedException("setString");
  }

  /**
   * Writes len characters of str, starting at character offset, to the CLOB
   * value that this Clob represents.
   * 
   * @param pos the position
   * @param str the string
   * @param offset the offset
   * @param len the length
   * @return return value
   * @throws SQLException if there is an error accessing the <code>clob</code>
   */
  public int setString(long pos, String str, int offset, int len)
      throws SQLException
  {
    throw new NotImplementedException("setString");
  }

  /**
   * Truncates the CLOB value that this Clob designates to have a length of len
   * characters.
   * 
   * @param len the length
   * @throws SQLException if there is an error accessing the <code>clob</code>
   */
  public void truncate(long len) throws SQLException
  {
    throw new NotImplementedException("truncate");
  }

@Override
public void free() throws SQLException {
	// TODO Auto-generated method stub
	
}

@Override
public Reader getCharacterStream(long pos, long length) throws SQLException {
	// TODO Auto-generated method stub
	return null;
}
}