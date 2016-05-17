/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks
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
 * Initial developer(s): Marc Herbert
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.driver;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

/**
 * This class defines a BlobOutputStream.
 * <p>
 * TODO: we should implement close() then error once we are closed.
 * 
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert</a>
 * @version 1.0
 */
public class BlobOutputStream
    extends OutputStream
{
  /** The actual Blob we are pointing to */
  Blob blob;
  /** The current offset in the stream, counting from zero (NOT SQL style) */
  int  currentPos;

  /**
   * Creates a new <code>BlobOutputStream</code> object pointing to the given
   * Blob (currently implemented as an array).
   * 
   * @param b the reference to the underlying blob
   * @param startPos the starting position in the array (counting from zero).
   */
  public BlobOutputStream(Blob b, int startPos)
  {
    super();
    this.blob = b;
    currentPos = startPos;
  }

  /**
   * @see java.io.OutputStream#write(int)
   */
  public void write(int b) throws IOException
  {
    blob.getInternalByteArray()[currentPos] = (byte) b;
    currentPos++;
  }

  /**
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  public void write(byte[] b, int off, int len) throws IOException
  {
    try
    {
      // SQL indexes count from 1
      blob.setBytes(currentPos + 1, b, off, len);
      currentPos += len;
    }
    catch (SQLException sqle)
    {
      throw (IOException) new IOException(sqle.getLocalizedMessage())
          .initCause(sqle);
    }

  }

}
