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

package org.objectweb.cjdbc.common.sql.filters;

import org.objectweb.cjdbc.common.stream.encoding.HexaEncoding;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This class defines a HexaBlobFilterInterface. It encodes the blobs in hexa
 * values
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class HexaBlobFilter extends AbstractBlobFilter
{

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#decode(java.lang.String)
   */
  public byte[] decode(byte[] data)
  {
    return HexaEncoding.hex2data(new String(data));
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#encode(java.lang.String)
   */
  public String encode(String data)
  {
    return encode(data.getBytes());
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#decode(java.lang.String)
   */
  public byte[] decode(String data)
  {
    return HexaEncoding.hex2data(data);
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#encode(byte[])
   */
  public String encode(byte[] data)
  {
    return HexaEncoding.data2hex(data);
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#getXml()
   */
  public String getXml()
  {
    return DatabasesXmlTags.VAL_hexa;
  }

}