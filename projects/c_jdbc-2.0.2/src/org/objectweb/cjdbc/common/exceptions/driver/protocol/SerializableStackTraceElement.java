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

import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;

/**
 * This class re-implements StackTraceElement of JDK 1.5, as a brute force
 * workaround for the forgotten constructor in JDK 1.4.
 * 
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert</a>
 * @version 1.0
 */
public class SerializableStackTraceElement
{
  private String declaringClass;
  private String methodName;
  private String fileName;
  private int    lineNumber;

//  /**
//   * This is the constructor forgotten in 1.4 (and available in 1.5)
//   * the only reason why we exist.
//   */
//  private SerializableStackTraceElement(String declaringClass, String methodName,
//      String fileName, int lineNumber)
//  {
//    this.declaringClass = declaringClass;
//    this.methodName = methodName;
//    this.fileName = fileName;
//    this.lineNumber = lineNumber;
//
//  }
  
  /**
   * Constructs/converts a standard StackTraceElement (non-serializable in 1.4)
   * into a SerializableStackTraceElement.
   * 
   * @param st the element to convert.
   */
  public SerializableStackTraceElement(StackTraceElement st)
  {
    this.declaringClass = st.getClassName();
    this.methodName = st.getMethodName();
    this.fileName = st.getFileName();
    this.lineNumber = st.getLineNumber();
  }

  /**
   * Deserializes a new <code>SerializableStackTraceElement</code> from the
   * stream
   * 
   * @param in the stream to read from
   * @throws IOException stream error
   */
  public SerializableStackTraceElement(CJDBCInputStream in) throws IOException
  {
    declaringClass = in.readUTF();
    methodName = in.readUTF();
    fileName = in.readUTF();
    lineNumber = in.readInt();
  }

  /**
   * Serializes the object to the given stream.
   * 
   * @param out the stream to send the object to
   * @throws IOException stream error
   */
  public void sendToStream(CJDBCOutputStream out) throws IOException
  {
    out.writeUTF(declaringClass);
    out.writeUTF(methodName);
    out.writeUTF(fileName);
    out.writeInt(lineNumber);

  }

  /**
   * 
   * @see StackTraceElement#getLineNumber()
   */
  public int getLineNumber()
  {
    return lineNumber;
  }

  /**
   * 
   * @see StackTraceElement#getClassName()
   */
  public String getClassName()
  {
    return declaringClass;
  }

  /**
   * 
   * @see StackTraceElement#getMethodName()
   */
  public String getMethodName()
  {
    return methodName;
  }

  /**
   * 
   * @see StackTraceElement#isNativeMethod()
   */
  public boolean isNativeMethod()
  {
    return lineNumber == -2;
  }

  /**
   * 
   * @see StackTraceElement#toString()
   */
  public String toString()
  {
    return getClassName()
        + "."
        + methodName
        + (isNativeMethod() ? "(Native Method)" : (fileName != null
            && lineNumber >= 0
            ? "(" + fileName + ":" + lineNumber + ")"
            : (fileName != null ? "(" + fileName + ")" : "(Unknown Source)")));
  }

}
