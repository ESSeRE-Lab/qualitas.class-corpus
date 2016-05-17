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

package org.objectweb.cjdbc.common.exceptions.driver;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.driver.protocol.SerializableException;

/**
 * This class customizes SQLException. Since JDBC allows only SQLExceptions, it
 * is used to systematically wrap underlying exceptions (typically coming from
 * the controller). The main feature added to SQLException is to override
 * printStackTrace() methods so they also print the non-standard, serializable
 * stack traces of SerializableException coming from the controller. Another
 * feature is to provide constructors with a "cause" (chaining), avoiding the
 * use of initCause()
 */
/**
 * FIXME: this class relies on "multiple dispatch", which does not exist in Java
 * (doh!). The current workaround it to cast properly at each call site. The
 * definitive fix is to use instanceof.
 */
public class DriverSQLException extends SQLException
{
  /**
   * @see SQLException#SQLException()
   */
  public DriverSQLException()
  {
    super();
  }

  /**
   * @see SQLException#SQLException(java.lang.String)
   */
  public DriverSQLException(String reason)
  {
    super(reason);
  }

  /**
   * @see SQLException#SQLException(java.lang.String, java.lang.String)
   */
  public DriverSQLException(String reason, String sQLState)
  {
    super(reason, sQLState);
  }

  /**
   * @see SQLException#SQLException(java.lang.String, java.lang.String, int)
   */
  public DriverSQLException(String reason, String sQLState, int vendorCode)
  {
    super(reason, sQLState, vendorCode);
  }

  /**
   * Creates a new <code>DriverSQLException</code> around a
   * SerializableException received from controller, itself converted from an
   * SQLException in most cases. So we set SQLState and vendorCode.
   * 
   * @param message message
   * @param cause exception from controller to wrap
   */
  public DriverSQLException(String message, SerializableException cause)
  {
    super(message, cause.getSQLState(), cause.getErrorCode());
    initCause(cause);
  }

  /**
   * Missing message constructor: let's borrow message from cause.
   * 
   * @param cause exception to wrap
   */
  public DriverSQLException(SerializableException cause)
  {
    this("Message of cause: " + cause.getLocalizedMessage(), cause);
  }

  /**
   * Missing message constructor: let's borrow message from cause.
   * 
   * @param cause exception to wrap
   */
  public DriverSQLException(Exception cause)
  {
    /**
     * @see #DriverSQLException(String, SerializableException)
     * @see #DriverSQLException(String, Exception)
     */
    this("Message of cause: " + cause.getLocalizedMessage(), cause);
  }

  /**
   * Creates a new <code>DriverSQLException</code> around an exception of a
   * type not specifically handled elsewhere. Typically used for exceptions
   * internal to the driver.
   * 
   * @param message message
   * @param cause generic exception to wrap
   */
  public DriverSQLException(String message, Exception cause)
  {
    super(message);
    initCause(cause);
  }

  /**
   * @see #DriverSQLException(String, SQLException)
   * @deprecated
   */
  public DriverSQLException(SQLException cause)
  {
    this("", cause);
  }

  /**
   * An SQLException should not be wrapped inside a DriverSQLException: this is
   * a symptom of mixing different layers.
   * 
   * @param message message
   * @param cause cause
   * @deprecated
   * @throws IllegalArgumentException always
   */
  public DriverSQLException(String message, SQLException cause)
      throws IllegalArgumentException
  {
    // ok let's be tolerant for the moment
    super(message);
    initCause(cause);

    // TODO: ... but this is the future:
    // A (Driver-)SQLException should be created here and nowhere below

    // IllegalArgumentException iae = new IllegalArgumentException(
    // "Bug: cause of a DriverSQLException should not itself be an SQLException
    // "
    // + message);
    // iae.initCause(cause);
    // throw iae;
  }

  /**
   * Overrides super method so we print the serializable stack trace of next
   * exceptions in the chain (if they use our serializable stack trace)
   * 
   * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
   */
  public void printStackTrace(PrintStream s)
  {
    /*
     * super does unfortunately not call printStackTrace() recursively on the
     * chain: instead it breaks object encapsulation by calling instead printing
     * methods and private fields on nexts. And since our chain uses its own
     * private stack trace implementation (because of JDK 1.4 woes) this does
     * print nothing in the end.
     */
    super.printStackTrace(s);

    // So we have to call printStackStrace() ourselves.
    Throwable cause = getCause();
    if (null != cause && cause instanceof SerializableException)
    {
      s.println("SerializableStackTrace of each cause:");
      ((SerializableException) cause).printStackTrace(s);
    }
  }

  /**
   * Overrides super method so we print the serializable stack trace of next
   * exceptions in the chain (if they use our serializable stack trace)
   * 
   * @see java.lang.Throwable#printStackTrace()
   */
  public void printStackTrace()
  {
    /**
     * This comes back to
     * 
     * @see DriverSQLException#printStackTrace(PrintStream)
     */
    super.printStackTrace();

  }

  /**
   * Overrides super method so we print the serializable stack trace of next
   * exceptions in the chain (if they use our serializable stack trace)
   * 
   * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
   */
  public void printStackTrace(PrintWriter s)
  {
    /** @see #printStackTrace(PrintStream) */
    super.printStackTrace(s);

    Throwable cause = getCause();
    if (null != cause && cause instanceof SerializableException)
    {
      s.println("SerializableStackTrace of each cause:");
      ((SerializableException) cause).printStackTrace(s);
    }
  }

}
