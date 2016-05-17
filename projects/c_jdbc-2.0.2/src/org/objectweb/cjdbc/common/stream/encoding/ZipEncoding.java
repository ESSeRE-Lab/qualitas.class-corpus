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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * This class defines ZipEncoding/Decoding methods
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ZipEncoding
{
  /**
   * Encode data using ZIP compression
   * 
   * @param data byte array to compress
   * @return <code>byte[]</code> of zip encoded data
   * @throws IOException if fails reading/writing streams
   */
  public static final byte[] encode(byte[] data) throws IOException
  {
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //GZIPOutputStream zipOutputStream = new GZIPOutputStream(baos);
    DeflaterOutputStream zipOutputStream = new DeflaterOutputStream(baos,
        new Deflater(Deflater.BEST_COMPRESSION, true));

    //BufferedOutputStream bos = new BufferedOutputStream(zipOutputStream);
    byte[] bdata = new byte[1024];
    int byteCount;
    while ((byteCount = bais.read(bdata, 0, 1024)) > -1)
    {
      zipOutputStream.write(bdata, 0, byteCount);
    }
    zipOutputStream.flush();
    zipOutputStream.finish();
    zipOutputStream.close();
    return baos.toByteArray();
  }

  /**
   * Decode data using ZIP Decompression
   * 
   * @param data the encoded data
   * @return <code>byte[]</code> of decoded data
   * @throws IOException if fails
   */
  public static final byte[] decode(byte[] data) throws IOException
  {
    InflaterInputStream input = new InflaterInputStream(
        new ByteArrayInputStream(data), new Inflater(true));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    byte[] bdata = new byte[1024];
    int byteCount;
    while ((byteCount = input.read(bdata, 0, 1024)) > -1)
      baos.write(bdata, 0, byteCount);
    baos.flush();
    baos.close();

    return baos.toByteArray();
  }
}