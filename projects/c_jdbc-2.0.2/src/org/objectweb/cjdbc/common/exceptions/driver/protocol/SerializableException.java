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

package org.objectweb.cjdbc.common.exceptions.driver.protocol;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;

/**
 * This class implements our own Exception chain, overriding Throwable as much
 * as possible, relying on our own SerializableStackTraceElement instead of
 * java.lang.StackStraceElement.
 * <p>
 * The main reason for this class is that java.lang.StackStraceElement is not
 * serializable in JDK 1.4 (the constructor is missing, whereas 1.5 has it).
 * Unfortunately we cannot override everything we would like to because
 * StackTraceElement is also final and so SerializableStackTraceElement cannot
 * subtype+override it.
 * 
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert</a>
 * @version 1.0
 */
public class SerializableException
    extends Exception
{

  private String                          sqlState;
  private int                             vendorCode;

  private SerializableStackTraceElement[] stackTrace;

  /**
   * @see Exception#Exception(java.lang.Throwable)
   */
  SerializableException(String message, SerializableException cause)
  {
    super(message, cause);
  }

  /**
   * Converts a chain of Throwables to a chain of
   * <code>SerializableException</code>. The resulting chain has the same
   * length.
   * 
   * @param start head of chain to convert
   */

  SerializableException(Throwable start)
  {
    this(start.getMessage(), null == start.getCause()
        ? null
        : new SerializableException(start.getCause())); // recursion

    convertStackTrace(start);
  }

  void convertStackTrace(Throwable regularEx)
  {
    StackTraceElement[] regularST = regularEx.getStackTrace();
    stackTrace = new SerializableStackTraceElement[regularST.length];
    for (int i = 0; i < regularST.length; i++)
      stackTrace[i] = new SerializableStackTraceElement(regularST[i]);

    // nullifies super's, non-serializable stack trace since we don't want to
    // use it anymore
    setStackTrace(null /* ignored arg */);

  }

  /* ***** Serialization / Networking ********* */

  /**
   * Constructs/reads a new SerializableException chain from the stream
   */
  SerializableException(CJDBCInputStream in) throws IOException
  {
    // receive message and next exception (recursive)
    // (left to right evaluation is guaranteed by JLS bible)
    super(in.readUTF(), in.readBoolean() ? new SerializableException(in) : null);

    // receive stackTrace
    stackTrace = new SerializableStackTraceElement[in.readInt()];
    for (int i = 0; i < stackTrace.length; i++)
      stackTrace[i] = new SerializableStackTraceElement(in);

    // receive SQL fields
    setSQLState(in.readUTF());
    setErrorCode(in.readInt());

  }

  /**
   * Send the Serializable chain to the given stream.
   * 
   * @param out destination stream
   * @throws IOException stream error
   */
  public void sendToStream(CJDBCOutputStream out) throws IOException
  {
    // send message
    out.writeUTF(getMessage());

    // send next exception if any (chaining)
    if (null != getCause())
    {
      out.writeBoolean(true);
      ((SerializableException) getCause()).sendToStream(out); // recursion
    }
    else
      out.writeBoolean(false); // stop condition

    // send stack trace
    out.writeInt(stackTrace.length);
    for (int i = 0; i < stackTrace.length; i++)
      stackTrace[i].sendToStream(out);

    // send SQL fields
    out.writeUTF(getSQLState());
    out.writeInt(getErrorCode());

    out.flush();
  }

  /**
   * @see java.lang.Throwable#printStackTrace()
   */
  public void printStackTrace()
  {
    printStackTrace(System.err);
  }

  /**
   * Prints this throwable and its backtrace to the specified print stream.
   * 
   * @param s <code>PrintStream</code> to use for output
   */
  public void printStackTrace(PrintStream s)
  {
    synchronized (s)
    {
      s.println(this);
      for (int i = 0; i < stackTrace.length; i++)
        s.println("\tAt: " + stackTrace[i]);

      SerializableException ourCause = (SerializableException) getCause();
      if (ourCause != null)
      {
        s.println("Caused   by");
        ourCause.printStackTrace(s);
      }
    }
  }

  /**
   * Prints this throwable and its backtrace to the specified print writer.
   * 
   * @param s <code>PrintWriter</code> to use for output
   */
  public void printStackTrace(PrintWriter s)
  {
    synchronized (s)
    {
      s.println(this);
      for (int i = 0; i < stackTrace.length; i++)
        s.println("\tAt: " + stackTrace[i]);

      SerializableException ourCause = (SerializableException) getCause();
      if (ourCause != null)
      {
        s.println("Caused   by");
        ourCause.printStackTrace(s);
      }
    }
  }


  /**
   * @deprecated
   * @see java.lang.Throwable#fillInStackTrace()
   */
  public synchronized Throwable fillInStackTrace()
  {
    setStackTrace(null);
    return this;
  }

  /**
   * Please use getSerializedStackTrace() instead. Unfortunately
   * StackTraceElement has no constructor in 1.4 and cannot be overriden
   * (final).
   * 
   * @deprecated
   * @see java.lang.Throwable#getStackTrace()
   */
  public StackTraceElement[] getStackTrace()
  {
    return new StackTraceElement[0];
  }

  /**
   * Returns our private stack trace, the one which is serializable.
   * 
   * @return our private stack trace
   */
  public SerializableStackTraceElement[] getSerializableStackTrace()
  {
    return (SerializableStackTraceElement[]) stackTrace.clone();
  }

  /**
   * This method is deprecated and erases the regular, non serializable stack
   * trace. Please use setSerializedStackTrace() instead. Unfortunately
   * StackTraceElement has no constructor in 1.4 and cannot be overriden
   * (final).
   * 
   * @deprecated
   * @see java.lang.Throwable#setStackTrace(java.lang.StackTraceElement[])
   */
  public void setStackTrace(StackTraceElement[] ignored)
  {
    super.setStackTrace(new StackTraceElement[0]);
  }

  /**
   * Sets the vendorCode value.
   * 
   * @param vendorCode The vendorCode to set.
   */
  void setErrorCode(int vendorCode)
  {
    this.vendorCode = vendorCode;
  }

  /**
   * Returns the vendorCode value.
   * 
   * @return Returns the vendorCode.
   */
  public int getErrorCode()
  {
    return vendorCode;
  }

  /**
   * Sets the sQLState value.
   * 
   * @param sQLState The sQLState to set.
   */
  public void setSQLState(String sQLState)
  {
    this.sqlState = sQLState;
  }

  /**
   * Returns the sQLState value.
   * 
   * @return Returns the sQLState.
   */
  public String getSQLState()
  {
    return sqlState;
  }

  /**
   * Override super, adding an extra check because we do not want a mixed chain.
   * 
   * @see java.lang.Throwable#initCause(java.lang.Throwable)
   */
  public Throwable initCause(Throwable cause)
  {
    throwsIfNotSerializable(cause);

    super.initCause(cause);
    return this;
  }

  private void throwsIfNotSerializable(Throwable cause)
      throws IllegalArgumentException
  {
    if (null == cause)
      return;

    if (!(cause instanceof SerializableException))
      throw new IllegalArgumentException(
          "The cause of SerializableException has to be a SerializableException");
  }

}
