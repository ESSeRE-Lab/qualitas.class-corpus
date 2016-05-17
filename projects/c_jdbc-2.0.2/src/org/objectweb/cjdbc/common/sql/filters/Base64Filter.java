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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.sql.filters;

import org.objectweb.cjdbc.common.stream.encoding.Base64;
import org.objectweb.cjdbc.common.stream.encoding.ZipEncoding;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This class defines a Base64Filter. It is based on the Base64 encoding class
 * from apache.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Base64Filter extends AbstractBlobFilter
{

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#encode(byte[])
   */
  public String encode(byte[] data)
  {
    try
    {
      return Base64.encode(ZipEncoding.encode(data));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return "";
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#encode(java.lang.String)
   */
  public String encode(String data)
  {

    return encode(data.getBytes());
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#decode(byte[])
   */
  public byte[] decode(byte[] data)
  {
    try
    {
      return ZipEncoding.decode(Base64.decode(new String(data)));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#decode(java.lang.String)
   */
  public byte[] decode(String data)
  {
    return decode(data.getBytes());
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#getXml()
   */
  public String getXml()
  {
    return DatabasesXmlTags.VAL_base64;
  }

}