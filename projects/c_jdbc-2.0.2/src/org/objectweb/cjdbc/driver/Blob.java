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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.NotImplementedException;
import org.objectweb.cjdbc.common.exceptions.driver.DriverSQLException;

/**
 * The representation (mapping) in the Java <sup><small>TM </small> </sup>
 * programming language of an SQL <code>BLOB</code> value. By default drivers
 * implement <code>Blob</code> using an SQL <code>locator(BLOB)</code>,
 * which means that a <code>Blob</code> object contains a logical pointer to
 * the SQL <code>BLOB</code> data rather than the data itself. But since this
 * is highly database-specific, we are unable to do that and implement Blobs
 * using a simple private byte array copy instead. This may consume a lot of
 * memory but this is both portable across databases and even legal from a JDBC
 * standard point of view as long as our method
 * {@link org.objectweb.cjdbc.driver.DatabaseMetaData#locatorsUpdateCopy()}
 * returns true.
 * 
 * @see java.sql.Blob
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert </a>
 * @since JDK 1.2
 */
public class Blob implements java.sql.Blob, Serializable
{
  /** The binary data that makes up this <code>BLOB</code>. */
  byte[] internalArray;


  // ------------- JDBC 2.1 / JDK 1.2-------------------

  /**
   * @see java.sql.Blob#length()
   */
  public long length() throws SQLException
  {
    checkInitialized();
    return internalArray.length;
  }

  /**
   * @see java.sql.Blob#getBytes(long, int)
   */
  public byte[] getBytes(long sqlPos, int length) throws SQLException
  {
    checkInitialized();
    checkSQLRangeIsSupported(sqlPos, length);

    int arrayPos = (int) (sqlPos - 1);
    return resizedByteArray(internalArray, arrayPos, //
        Math.min(length, // no more than asked for
            internalArray.length - arrayPos)); // no more than what we have
  }

  /**
   * @see java.sql.Blob#getBinaryStream()
   */
  public java.io.InputStream getBinaryStream() throws SQLException
  {
    checkInitialized();
    return new ByteArrayInputStream(internalArray);
  }

  /**
   * @see java.sql.Blob#position(byte[], long)
   */
  public long position(byte[] pattern, long sqlStart) throws SQLException
  {
    checkInitialized();
    checkSQLRangeIsSupported(sqlStart, 0);

    throw new NotImplementedException("position not yet implemented");
  }

  /**
   * @see java.sql.Blob#position(java.sql.Blob, long)
   */
  public long position(java.sql.Blob pattern, long sqlStart)
      throws SQLException
  {
    checkInitialized();
    checkSQLRangeIsSupported(sqlStart, 0);

    // FIXME: implement me
    return position(pattern.getBytes(0, (int) pattern.length()), sqlStart);
  }

  // ------------- JDBC 3.0 / JDK 1.4-------------------

  /**
   * @see java.sql.Blob#setBytes(long, byte[], int, int)
   */
  public int setBytes(long sqlStartPos, byte[] srcArray) throws SQLException
  {
    return this.setBytes(sqlStartPos, srcArray, 0, srcArray.length);
  }

  /**
   * @see java.sql.Blob#setBytes(long, byte[], int, int)
   */
  public int setBytes(long sqlStartPos, byte[] srcArray, int srcArrayOffset,
      int copiedLength) throws SQLException
  {
    checkInitialized();
    checkSQLRangeIsSupported(sqlStartPos, copiedLength);

    int minimumLengthNeeded = (int) (sqlStartPos - 1) + copiedLength;

    // If we are too small, let's extend ourselves
    // FIXME: do the specs say we should do this?
    if (this.length() < minimumLengthNeeded)
      internalArray = resizedByteArray(internalArray, 0, minimumLengthNeeded);

    // else FIXME: when we are "longer", should we remove our tail or keep
    // it? Do the specs say something about this? Let's keep the tail for now.

    // Finally copy argument to ourselves.
    // Bytes between binaryData.length and pos-1 will stay to zero....
    // FIXME: do the specs say something about this?
    System.arraycopy(srcArray, srcArrayOffset, internalArray,
        (int) (sqlStartPos - 1), copiedLength);

    /*
     * huh, what else ? OK, something else would make sense in case we don't
     * extend the array.
     */
    return copiedLength;
  }

  /**
   * @see java.sql.Blob#setBinaryStream(long)
   */
  public java.io.OutputStream setBinaryStream(long sqlStart)
      throws SQLException
  {
    checkInitialized();
    checkSQLRangeIsSupported(sqlStart, 0);

    return new BlobOutputStream(this, (int) (sqlStart - 1));
  }

  /**
   * @see java.sql.Blob#truncate(long)
   */
  public void truncate(long newLen) throws SQLException
  {
    checkInitialized();

    if (newLen >= this.length())
      return;

    internalArray = resizedByteArray(internalArray, 0, (int) newLen);
  }

  // ----------- JDBC 4.0 --------------
  /**
   * This method frees the Blob object and releases the resources that it holds.
   */
  public void free()
  {
    internalArray = null;
  }

  // ------------------ BLOB internals ------------

  /**
   * Creates a new <code>Blob</code> object built from a copy of the given
   * byte array.
   * 
   * @param src the array to copy
   */
  public Blob(byte[] src)
  {
    // just clone the array
    this.internalArray = resizedByteArray(src, 0, src.length);
  }

  /**
   * BlobOutputStream needs it
   */
  byte[] getInternalByteArray()
  {
    return internalArray;
  }

  private void checkInitialized() throws DriverSQLException
  {
    if (null == internalArray)
      throw new DriverSQLException("Blob has been freed");
  }

  /**
   * Checks that indexes (sqlStart) and (sqlStart+len) are correct. Valid
   * sqlStart begins at 1 (SQL-style). A reasonable sqlEnd of Blob is no more
   * than Integer.MAX_VALUE+1 because we implement using Java arrays. This
   * method is basically a check to use before casting from long to int.
   * 
   * @param sqlStart start index
   * @param len length
   * @throws SQLException
   */
  private static void checkSQLRangeIsSupported(long sqlStart, int len)
      throws SQLException
  {
    long arrayStart = sqlStart - 1;
    if (arrayStart < 0)
    {
      throw new DriverSQLException("Illegal argument: start of Blob ("
          + sqlStart + ") cannot be less than 1");
    }
    if (Integer.MAX_VALUE <= arrayStart + len)
    {
      throw new NotImplementedException("End of Blob (" + (sqlStart + len)
          + ") is too great. Blobs greater than " + Integer.MAX_VALUE
          + " are not supported");
    }
  }

  /**
   * Returns a copy of the byte array argument starting at srcFrom and extended
   * or shortened to newLength. srcFrom index starts from zero (regular style).
   * <p>
   * This roughly double the memory used... *sigh*
   * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4655503 is tagged
   * "fixed" but nothing changed ?!
   */
  private byte[] resizedByteArray(byte[] src, int srcStart, int newSize)
  {
    byte[] newArray = new byte[newSize];
    System.arraycopy(src, srcStart, newArray, 0, Math.min(
        src.length - srcStart, // don't pass the old size
        newSize)); // don't pass the new size
    return newArray;
  }

@Override
public InputStream getBinaryStream(long pos, long length) throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

}