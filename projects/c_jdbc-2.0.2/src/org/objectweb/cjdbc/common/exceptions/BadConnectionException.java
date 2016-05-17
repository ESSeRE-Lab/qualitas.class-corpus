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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.exceptions;

/**
 * A <code>BadConnectionException</code> is thrown when a connection fails the
 * validity test.
 * 
 * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend#isValidConnection(java.sql.Connection)
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class BadConnectionException extends CJDBCException
{
  private static final long serialVersionUID = 2379659551945349743L;

  /**
   * Creates a new <code>BadConnectionException</code> instance.
   * 
   * @param cause the root cause
   */
  public BadConnectionException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Creates a new <code>BadConnectionException</code> instance.
   * 
   * @param message the error message
   * @param cause the root cause
   */
  public BadConnectionException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
