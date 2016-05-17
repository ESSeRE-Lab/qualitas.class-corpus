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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.common.sql.filters;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This class defines a BlobFilterInterface. All implementing interface should
 * satisfy the following: - Implementation is not dependant of the database -
 * decode(encode(data)) = data
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.fr">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public abstract class AbstractBlobFilter
{

  /**
   * Get an instance of an <code>AbstractBlobFilter</code> given the
   * blobEndodingMethod description. Currently supported are: <br>
   * <code>hexa</code><br>
   * <code>none</code><br>
   * <code>escaped</code><br>
   * If the parameter specified is not appropriate then a
   * <code>NoneBlobFilter</code> instance is returned.
   * 
   * @param blobEncodingMethod the string description
   * @return <code>AbstractBlobFilter</code> instance
   */
  public static AbstractBlobFilter getBlobFilterInstance(
      String blobEncodingMethod)
  {
    if (blobEncodingMethod.equals(DatabasesXmlTags.VAL_hexa))
      return new HexaBlobFilter();
    else if (blobEncodingMethod.equals(DatabasesXmlTags.VAL_escaped))
      return new BlobEscapedFilter();
    else if (blobEncodingMethod.equals(DatabasesXmlTags.VAL_base64))
      return new Base64Filter();
    else
      return new NoneBlobFilter();
  }

  /**
   * Encode the blob data in a form that is independant of the database.
   * 
   * @param data the byte array to convert
   * @return <code>String</code> object is returned for convenience as this is
   *         the way it is going to be handled afterwards.
   */
  public abstract String encode(byte[] data);

  /**
   * Encode the blob data in a form that is independant of the database.
   * 
   * @param data the byte array to convert
   * @return <code>String</code> object is returned for convenience as this is
   *         the way it is going to be handled afterwards.
   */
  public abstract String encode(String data);

  /**
   * Decode the blob data from the database. This must done in a database
   * independant manner.
   * 
   * @param data the data to decode
   * @return <code>byte[]</code> decoded byte array of data
   */
  public abstract byte[] decode(byte[] data);

  /**
   * Decode the blob data from the database. This must done in a database
   * independant manner.
   * 
   * @param data the data to decode
   * @return <code>byte[]</code> decoded byte array of data
   */
  public abstract byte[] decode(String data);

  /**
   * Get the XML attribute value of the filter as defined in the DTD.
   * 
   * @return XML attribute value
   */
  public abstract String getXml();
}