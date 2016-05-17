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
 * Initial developer(s): Silvan Eugen Lincan
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.sql.filters;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This class defines a BlobEscapedFilter. It acts just the same as the
 * <code>NoneBlobFilter</code> except that the content is escaped to prevent
 * strange characters for disturbing the data. This has been adapted mainly for
 * PostgreSQL bytea data, but it should be usable for any other database.
 * 
 * @author <a href="mailto:s.lincan@moodmedia.ro">Silvan Eugen Lincan </a>
 * @version 1.0
 */
public class BlobEscapedFilter extends AbstractBlobFilter
{

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#encode(byte[])
   */
  public String encode(byte[] data)
  {
    return BlobEscapedFilter.toPGString(data);
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#encode(java.lang.String)
   */
  public String encode(String data)
  {
    return data;
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#decode(byte[])
   */
  public byte[] decode(byte[] data)
  {
    return data;
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#decode(java.lang.String)
   */
  public byte[] decode(String data)
  {
    return data.getBytes();
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#getXml()
   */
  public String getXml()
  {
    return DatabasesXmlTags.VAL_escaped;
  }

  /**
   * Converts a java byte[] into a PG bytea string (i.e. the text representation
   * of the bytea data type). Escape characters between 32 and 126.
   * 
   * @param postgresBuf The byte array to be converted
   * @return the string representation
   */
  public static String toPGString(byte[] postgresBuf)
  {
    if (postgresBuf == null)
    {
      return null;
    }
    StringBuffer stringBuffer = new StringBuffer(2 * postgresBuf.length);
    for (int i = 0; i < postgresBuf.length; i++)
    {
      int lInt = postgresBuf[i];
      if (lInt < 0)
      {
        lInt = 256 + lInt;
      }
      /*
       * Escape the same non-printable characters as the backend. Must escape
       * all 8bit characters otherwise when convering from java unicode to the
       * db character set we may end up with question marks if the character set
       * is SQL_ASCII.
       */
      if (lInt < 040 || lInt > 0176)
      {
        /* escape charcter '\0000', but need two \\ because of the parser. */
        stringBuffer.append("\\");
        stringBuffer.append((char) (((lInt >> 6) & 0x3) + 48));
        stringBuffer.append((char) (((lInt >> 3) & 0x7) + 48));
        stringBuffer.append((char) ((lInt & 0x07) + 48));
      }
      else if (postgresBuf[i] == (byte) '\\')
      {
        /*
         * escape the backslash character as \\, but need four \\\\ because of
         * the parser.
         */
        stringBuffer.append("\\\\");
      }
      else
      {
        /* other characters are left alone */
        stringBuffer.append((char) postgresBuf[i]);
      }
    }
    String x = stringBuffer.toString();
    /* Add 10% for escaping. */
    StringBuffer sbuf = new StringBuffer(2 + x.length() * 11 / 10);
    for (int i = 0; i < x.length(); ++i)
    {
      char c = x.charAt(i);
      if ((c == '\'') || (c == '\\'))
        sbuf.append("\\");
      sbuf.append(c);
    }

    return sbuf.toString();
  }

  /**
   * Converts a PG bytea raw value (i.e. the raw binary representation of the
   * bytea data type) into a java byte[].
   * 
   * @param s The byte array to be converted.
   * @return an java byte[]
   */
  public static byte[] toBytes(byte[] s)
  {
    if (s == null)
    {
      return null;
    }
    int slength = s.length;
    byte[] buf = new byte[slength];
    int bufpos = 0;
    int thebyte;
    byte nextbyte;
    byte secondbyte;
    for (int i = 0; i < slength; i++)
    {
      nextbyte = s[i];
      if (nextbyte == (byte) '\\')
      {
        secondbyte = s[++i];
        if (secondbyte == (byte) '\\')
        {
          /* escaped \ */
          buf[bufpos++] = (byte) '\\';
        }
        else
        {
          thebyte = (secondbyte - 48) * 64 + (s[++i] - 48) * 8 + (s[++i] - 48);
          if (thebyte > 127)
            thebyte -= 256;
          buf[bufpos++] = (byte) thebyte;
        }
      }
      else
      {
        buf[bufpos++] = nextbyte;
      }
    }
    byte[] resultReturn = new byte[bufpos];
    System.arraycopy(buf, 0, resultReturn, 0, bufpos);
    return resultReturn;
  }
}