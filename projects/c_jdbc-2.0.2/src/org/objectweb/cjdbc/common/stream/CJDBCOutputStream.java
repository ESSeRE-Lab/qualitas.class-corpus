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
 * Contributor(s): Emmanuel Cecchet, Marc Herbert
 */

package org.objectweb.cjdbc.common.stream;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * DataOutputStream wrapper used between the controller and the driver.
 * 
 * @see org.objectweb.cjdbc.common.stream.CJDBCStream
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com</a>
 */
public class CJDBCOutputStream
{
  private DataOutputStream output;
  private Socket           socket;
  private long             dateCreated;

  /**
   * Creates a new <code>CJDBCOutputStream</code> object for statistics on
   * sockets
   * 
   * @param socket socket to monitor
   * @throws IOException if an IO error occurs
   */
  public CJDBCOutputStream(Socket socket) throws IOException
  {
    this(socket.getOutputStream());
    this.socket = socket;
  }

  /**
   * Creates a new CJDBCOutputStream from the given input stream.
   * 
   * @param out outputstream to wrap
   */
  public CJDBCOutputStream(OutputStream out)
  {
    output = new DataOutputStream(new BufferedOutputStream(out));
    dateCreated = System.currentTimeMillis();
  }

  /**
   * @see java.io.DataOutputStream#flush()
   * @throws IOException if an error occurs
   */
  public void flush() throws IOException
  {
    output.flush();
  }

  /**
   * @see java.io.DataOutputStream#close()
   * @throws IOException if an error occurs
   */
  public void close() throws IOException
  {
    output.close();
  }

  /**
   * We split the String into 21K-long chunks and encode+send them one after the
   * other. This is because Java's writeUTF() fails when UTFlen is < 64K. UTFlen
   * max is: 2 + 3 * string.length()
   * <p>
   * Java's writeUTF() is fully specified in
   * {@link java.io.DataOutput#writeUTF(java.lang.String)} and stable since
   * JDK1.0. Implementation is located in
   * {@link java.io.DataOutputStream#writeUTF(java.lang.String,
   * java.io.DataOutput)}
   * <p>
   * At first this looks like a kludge but: (1) On one hand it's almost required
   * anyway to split into chunks (whatever technique we choose) since strings
   * can be infinitely long and we don't want to allocate tons of memory. (2) On
   * the other hand it is hard to find out a String serialization which is both
   * efficient and based upon a <em>standard</em> encoding. Let's briefly look
   * at the alternatives:
   * <ul>
   * <li>We would like to send java chars as is using DataOutput.writeChars()
   * (since they are just from an UTF-16 subset) but there is no readChars()
   * method (unknown size problem?);
   * <li>string.getBytes("UTFXX") is highly portable and flexible but involves
   * a heavy-weight and overkill encoding framework, whereas the encoding of
   * 64K-limited writeUTF() is hard-wired.
   * <li>OutputStreamWriter relies on the same framework and is even more
   * complex.
   * <li>Maybe some hope from the JNI side? Something like GetStringChars() but
   * in Java?
   * </ul>
   * 
   * @see java.io.DataOutputStream#writeUTF(java.lang.String)
   * @param string a String to write in UTF form to the stream
   * @throws IOException if an error occurs
   */
  public void writeUTF(String string) throws IOException
  {
    if (null == string)
    {
      output.writeBoolean(false);
      return;
    }

    output.writeBoolean(true);
    int idx;
    final int maxSize = CJDBCStream.STRING_CHUNK_SIZE;

    this.writeInt(string.length());

    // First send all full, maxSize long chunks
    for (idx = 0; idx + maxSize <= string.length(); idx += maxSize)
      // substring() does no copy, cool.
      output.writeUTF(string.substring(idx, idx + maxSize));

    // Send the tail separately because
    // - string.substring(begin, TOO_LONG) is unfortunately not legal.
    // - we do not send any empty string, this is useless and would complexify
    // the receiver.
    // The tail is in most (short) cases just the string as is.

    if (string.length() > idx)
      output.writeUTF(string.substring(idx));
  }

  /**
   * @see java.io.DataOutputStream#writeInt(int)
   * @param value an int value to write to the stream
   * @throws IOException if an error occurs
   */
  public void writeInt(int value) throws IOException
  {
    output.writeInt(value);
  }

  /**
   * @see java.io.DataOutputStream#writeLong(long)
   * @param value a long value to write to the stream
   * @throws IOException if an error occurs
   */
  public void writeLong(long value) throws IOException
  {
    output.writeLong(value);
  }

  /**
   * @see java.io.DataOutputStream#writeFloat(float)
   * @param value a float value to write to the stream
   * @throws IOException if an error occurs
   */
  public void writeFloat(float value) throws IOException
  {
    output.writeFloat(value);
  }

  /**
   * @see java.io.DataOutputStream#writeDouble(double)
   * @param value a double value to write to the stream
   * @throws IOException if an error occurs
   */
  public void writeDouble(double value) throws IOException
  {
    output.writeDouble(value);
  }

  /**
   * @see java.io.DataOutputStream#write(byte[])
   * @param b an array of bytes to write to the stream
   * @throws IOException if an error occurs
   */
  public void write(byte[] b) throws IOException
  {
    this.write(b, 0, b.length);
  }

  /**
   * @see java.io.DataOutputStream#write(byte[], int, int)
   */
  public void write(byte[] b, int offset, int length) throws IOException
  {
    output.write(b, offset, length);
  }

  /**
   * @see java.io.DataOutputStream#writeBoolean(boolean)
   * @param value a boolean value to write to the stream
   * @throws IOException if an error occurs
   */
  public void writeBoolean(boolean value) throws IOException
  {
    output.writeBoolean(value);
  }

  /**
   * @return Returns the socket.
   */
  public Socket getSocket()
  {
    return socket;
  }

  /**
   * @return Returns the dateCreated.
   */
  public long getDateCreated()
  {
    return dateCreated;
  }

}
