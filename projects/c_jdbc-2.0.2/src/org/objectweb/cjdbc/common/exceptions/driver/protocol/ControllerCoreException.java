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
import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.NoMoreControllerException;
import org.objectweb.cjdbc.common.exceptions.NotImplementedException;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;

/**
 * This class is meant for exceptions originated in controller core (i.e.,
 * non-backend) that are serialized to the driver.
 * 
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert</a>
 * @version 1.0
 */
public class ControllerCoreException
    extends SerializableException
{

  private static final int UNKNOWN            = 0;
  private static final int NO_MORE_BACKEND    = 1;
  private static final int NO_MORE_CONTROLLER = 2;
  private static final int NOT_IMPLEMENTED    = 3;

  private static int exceptionTypeCode(Throwable ex)
  {
    if (ex instanceof NoMoreBackendException)
      return NO_MORE_BACKEND;
    if (ex instanceof NoMoreControllerException)
      return NO_MORE_CONTROLLER;
    if (ex instanceof NotImplementedException)
      return NOT_IMPLEMENTED;

    return UNKNOWN;
  }

  /**
   * This method returns a wrapper around 'this' ControllerCoreException, in
   * order to stay bug for bug compatible with legacy exception handling code.
   * This method should disappear and be implemented above, directly by the code
   * calling it.
   * 
   * @deprecated
   * @return a wrapper around this object
   */
  public Exception compatibilityWrapperHack()
  {
    Exception wrapper;
    switch (this.getErrorCode())
    {
      case NO_MORE_BACKEND :
        wrapper = new NoMoreBackendException(getMessage(), getSQLState(),
            getErrorCode());
        break;
      case NO_MORE_CONTROLLER :
        wrapper = new NoMoreControllerException(getMessage(), getSQLState(),
            getErrorCode());
        break;
      case NOT_IMPLEMENTED :
        wrapper = new NotImplementedException(getMessage(), getSQLState(),
            getErrorCode());
        break;
      default : // hey, why not ?
        wrapper = new SQLException(getMessage(), getSQLState(), getErrorCode());
    }
    wrapper.initCause(this);
    return wrapper;
  }

  /**
   * @see SerializableException#SerializableException(CJDBCInputStream)
   */
  public ControllerCoreException(CJDBCInputStream in) throws IOException
  {
    super(in);
  }

  /**
   * Converts a chain of Throwables to a new chain of SerializableExceptions
   * starting with a <code>ControllerCoreException</code>. The returned chain
   * has the same length.
   * 
   * @param ex head of chain to convert
   * @see SerializableException#SerializableException(Throwable)
   */
  public ControllerCoreException(Throwable ex)
  {
    super(ex);

    // This is the place where we could set your own SQL codes
    // super.SQLState = sqlE.getSQLState();

    // hack: let's use vendorCode int as a type
    setErrorCode(exceptionTypeCode(ex));

  }

}
