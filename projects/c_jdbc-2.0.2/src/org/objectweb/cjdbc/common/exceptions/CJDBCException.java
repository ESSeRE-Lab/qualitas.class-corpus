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
 * Initial developer(s): Mathieu Peltier.
 * Contributor(s): _______________________.
 */

package org.objectweb.cjdbc.common.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * C-JDBC base exception.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public class CJDBCException extends Exception implements Serializable
{
  private static final long serialVersionUID = -1899348090329064503L;

  /** Optional exception cause */
  protected Throwable cause;

  /**
   * Creates a new <code>CJDBCException</code> instance.
   */
  public CJDBCException()
  {
  }

  /**
   * Creates a new <code>CJDBCException</code> instance.
   * 
   * @param message the error message
   */
  public CJDBCException(String message)
  {
    super(message);
  }

  /**
   * Creates a new <code>CJDBCException</code> instance.
   * 
   * @param cause the root cause
   */
  public CJDBCException(Throwable cause)
  {
    this.cause = cause;
  }

  /**
   * Creates a new <code>CJDBCException</code> instance.
   * 
   * @param message the error message
   * @param cause the root cause
   */
  public CJDBCException(String message, Throwable cause)
  {
    super(message);
    this.cause = cause;
  }

  /**
   * Gets the root cause of this exception.
   * 
   * @return a <code>Throwable</code> object
   */
  public Throwable getCause()
  {
    return cause;
  }

  /**
   * @see java.lang.Throwable#fillInStackTrace()
   */
  public synchronized Throwable fillInStackTrace()
  {
    if (cause != null)
    {
      return cause.fillInStackTrace();
    }
    else
    {
      return super.fillInStackTrace();
    }
  }

  /**
   * @see java.lang.Throwable#getStackTrace()
   */
  public StackTraceElement[] getStackTrace()
  {
    if (cause != null)
    {
      return cause.getStackTrace();
    }
    else
    {
      return super.getStackTrace();
    }
  }

  /**
   * @see java.lang.Throwable#getMessage()
   */
  public String getMessage()
  {
    if (cause != null)
    {
      return cause.getMessage();
    }
    else
    {
      return super.getMessage();
    }
  }

  /**
   * @see java.lang.Throwable#printStackTrace()
   */
  public void printStackTrace()
  {
    if (cause != null)
    {
      cause.printStackTrace();
    }
    else
    {
      super.printStackTrace();
    }
  }

  /**
   * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
   */
  public void printStackTrace(PrintStream arg0)
  {
    if (cause != null)
    {
      cause.printStackTrace(arg0);
    }
    else
    {
      super.printStackTrace(arg0);
    }
  }

  /**
   * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
   */
  public void printStackTrace(PrintWriter arg0)
  {
    if (cause != null)
    {
      cause.printStackTrace(arg0);
    }
    else
    {
      super.printStackTrace(arg0);
    }
  }

  /**
   * @see java.lang.Throwable#setStackTrace(java.lang.StackTraceElement[])
   */
  public void setStackTrace(StackTraceElement[] arg0)
  {
    if (cause != null)
    {
      cause.setStackTrace(arg0);
    }
    else
    {
      super.setStackTrace(arg0);
    }
  }

}
