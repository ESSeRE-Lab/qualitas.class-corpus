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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.stream.encoding;

/**
 * This class implements Hexa encoding and decoding
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class HexaEncoding
{
  /**
   * Convert data into hexa
   * 
   * @param data to convert
   * @return the converted string
   */
  public static final String data2hex(byte[] data)
  {
    if (data == null)
      return null;

    int len = data.length;
    StringBuffer buf = new StringBuffer(len * 2);
    for (int pos = 0; pos < len; pos++)
      buf.append(toHexChar((data[pos] >>> 4) & 0x0F)).append(
          toHexChar(data[pos] & 0x0F));
    return buf.toString();
  }

  /**
   * convert hexa into data
   * 
   * @param str to convert
   * @return the converted byte array
   */
  public static final byte[] hex2data(String str)
  {
    if (str == null)
      return new byte[0];

    int len = str.length();
    char[] hex = str.toCharArray();
    byte[] buf = new byte[len / 2];

    for (int pos = 0; pos < len / 2; pos++)
      buf[pos] = (byte) (((toDataNibble(hex[2 * pos]) << 4) & 0xF0) | (toDataNibble(hex[2 * pos + 1]) & 0x0F));

    return buf;
  }

  /**
   * convert value to hexa value
   * 
   * @param i byte to convert
   * @return hexa char
   */
  public static char toHexChar(int i)
  {
    if ((0 <= i) && (i <= 9))
      return (char) ('0' + i);
    else
      return (char) ('a' + (i - 10));
  }

  /**
   * convert hexa char to byte value
   * 
   * @param c hexa character
   * @return corresponding byte value
   */
  public static byte toDataNibble(char c)
  {
    if (('0' <= c) && (c <= '9'))
      return (byte) ((byte) c - (byte) '0');
    else if (('a' <= c) && (c <= 'f'))
      return (byte) ((byte) c - (byte) 'a' + 10);
    else if (('A' <= c) && (c <= 'F'))
      return (byte) ((byte) c - (byte) 'A' + 10);
    else
      return -1;
  }
}
