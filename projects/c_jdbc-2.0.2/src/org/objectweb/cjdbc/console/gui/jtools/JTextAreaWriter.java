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

package org.objectweb.cjdbc.console.gui.jtools;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JTextArea;

/**
 * A implementation of the java.io.Writer class which provides writing to a
 * JTextArea via a stream.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author Anthony Eden
 */

public class JTextAreaWriter extends Writer
{

  private boolean      closed = false;
  private JTextArea    textArea;
  private StringBuffer buffer;

  /**
   * Constructor.
   * 
   * @param textArea The JTextArea to write to.
   */

  public JTextAreaWriter(JTextArea textArea)
  {
    setTextArea(textArea);
  }

  /**
   * Set the JTextArea to write to.
   * 
   * @param textArea The JTextArea
   */

  public void setTextArea(JTextArea textArea)
  {
    if (textArea == null)
    {
      throw new IllegalArgumentException("The text area must not be null.");
    }
    this.textArea = textArea;
  }

  /** Close the stream. */

  public void close()
  {
    closed = true;
  }

  /**
   * Flush the data that is currently in the buffer.
   * 
   * @throws IOException if fails
   */

  public void flush() throws IOException
  {
    if (closed)
    {
      throw new IOException("The stream is closed.");
    }
    textArea.append(getBuffer().toString());
    textArea.setCaretPosition(textArea.getDocument().getLength());
    buffer = null;
  }

  /**
   * Write the given character array to the output stream.
   * 
   * @param charArray The character array
   * @throws IOException if fails
   */

  public void write(char[] charArray) throws IOException
  {
    write(charArray, 0, charArray.length);
  }

  /**
   * Write the given character array to the output stream beginning from the
   * given offset and proceeding to until the given length is reached.
   * 
   * @param charArray The character array
   * @param offset The start offset
   * @param length The length to write
   * @throws IOException if fails
   */

  public void write(char[] charArray, int offset, int length)
      throws IOException 
  {
    if (closed)
    {
      throw new IOException("The stream is not open.");
    }
    getBuffer().append(charArray, offset, length);
  }

  /**
   * Write the given character to the output stream.
   * 
   * @param c The character
   * @throws IOException if fails
   */

  public void write(int c) throws IOException
  {
    if (closed)
    {
      throw new IOException("The stream is not open.");
    }
    getBuffer().append((char) c);
  }

  /**
   * Write the given String to the output stream.
   * 
   * @param string The String
   * @throws IOException if fails
   */

  public void write(String string) throws IOException
  {
    if (closed)
    {
      throw new IOException("The stream is not open.");
    }
    getBuffer().append(string);
  }

  /**
   * Write the given String to the output stream beginning from the given
   * offset and proceeding to until the given length is reached.
   * 
   * @param string The String
   * @param offset The start offset
   * @param length The length to write
   * @throws IOException if fails
   */

  public void write(String string, int offset, int length) throws IOException
  {
    if (closed)
    {
      throw new IOException("The stream is not open.");
    }
    getBuffer().append(string.substring(offset, length));
  }

  /**
   * Get the StringBuffer which holds the data prior to writing via a call to
   * the <code>flush()</code> method. This method should never return null.
   * 
   * @return A StringBuffer
   */

  private StringBuffer getBuffer()
  {
    if (buffer == null)
    {
      buffer = new StringBuffer();
    }
    return buffer;
  }

}
