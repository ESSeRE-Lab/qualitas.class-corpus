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
 * Initial developer(s): Paul Ferraro
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.sql.filters;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This class defines a NoneBlobFilter. It provides a pass-through filter
 * implementation that defers encoding to the underlying database driver
 * 
 * @author <a href="mailto:pmf8@columbia.edu">Paul Ferraro</a>
 * @version 1.0
 */
public class NoneBlobFilter extends AbstractBlobFilter
{
  /**
   * @see org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter#encode(byte[])
   */
  public String encode(byte[] data)
  {
    return new String(data);
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
    return DatabasesXmlTags.VAL_none;
  }

}