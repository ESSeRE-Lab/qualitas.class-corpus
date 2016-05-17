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
 * Contributor(s): Emmanuel Cecchet. Marc Herbert.
 */

package org.objectweb.cjdbc.common.stream;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;

/**
 * DataInputStream wrapper used between the controller and the driver.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert</a>
 */
public class CJDBCInputStream
{
  private DataInputStream input;
  private Socket          socket;
  private long            dateCreated;

  /**
   * Creates a new CJDBCInputStream from the given input stream.
   * 
   * @param in inputstream to wrap
   */
  public CJDBCInputStream(InputStream in)
  {
    input = new DataInputStream(new BufferedInputStream(in));
  }

  /**
   * Useful constructor for statistics on sockets ..
   * 
   * @param socket socket for this stream
   * @throws IOException if an error occurs
   * @throws StreamCorruptedException if an error occurs
   */
  public CJDBCInputStream(Socket socket) throws IOException,
      StreamCorruptedException
  {
    this(socket.getInputStream());
    this.socket = socket;
    dateCreated = System.currentTimeMillis();
  }

  /**
   * @see java.io.DataInputStream#close()
   * @throws IOException if an error occurs
   */
  public void close() throws IOException
  {
    input.close();
  }

  /**
   * @see java.io.DataInputStream#readBoolean()
   * @return a boolean value
   * @throws IOException if an error occurs
   */
  public boolean readBoolean() throws IOException
  {
    return input.readBoolean();
  }

  /**
   * @see java.io.DataInputStream#readInt()
   * @return an int value
   * @throws IOException if an error occurs
   */
  public int readInt() throws IOException
  {
    return input.readInt();
  }

  /**
   * @see java.io.DataInputStream#readLong()
   * @return a long value
   * @throws IOException if an error occurs
   */
  public long readLong() throws IOException
  {
    return input.readLong();
  }

  /**
   * @see java.io.DataInputStream#readFloat()
   * @return a floatvalue
   * @throws IOException if an error occurs
   */
  public double readFloat() throws IOException
  {
    return input.readFloat();
  }

  /**
   * @see java.io.DataInputStream#readDouble()
   * @return a doublevalue
   * @throws IOException if an error occurs
   */
  public double readDouble() throws IOException
  {
    return input.readDouble();
  }

  /**
   * @see java.io.DataInputStream#readFully(byte[])
   * @param b the byte array to fill up
   * @throws IOException if an error occurs
   */
  public void readFully(byte[] b) throws IOException
  {
    input.readFully(b);
  }

  /**
   * @see java.io.DataInputStream#readUTF()
   * @see CJDBCOutputStream#writeUTF(String)
   * @return a String in UTF format
   * @throws IOException if an error occurs
   */
  public String readUTF() throws IOException
  {
    if (!input.readBoolean())
      return null;

    final int maxSize = CJDBCStream.STRING_CHUNK_SIZE;

    int strlen = input.readInt();
    StringBuffer sbuf = new StringBuffer(strlen);

    // idx semantic: chars at idx and after had not yet the opportunity
    // to be received.
    for (int idx = 0; idx < strlen; idx += maxSize)
      sbuf.append(input.readUTF());

    return new String(sbuf);
  }

  /**
   * @see java.io.DataInputStream#available()
   * @return the number of available bytes.
   * @throws IOException if an error occurs
   */
  public int available() throws IOException
  {
    return input.available();
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