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
 * Contributor(s): _______________________
 */

package org.objectweb.cjdbc.common.exceptions;


/**
 * Controller exception.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * @version 1.0
 */
public class ShutdownException extends CJDBCException
{
  private static final long serialVersionUID = 1421907016443553749L;

  /**
   * Creates a new <code>ShutdownException</code> instance.
   */
  public ShutdownException()
  {
  }

  /**
   * Creates a new <code>ShutdownException</code> instance.
   * 
   * @param message the error message
   */
  public ShutdownException(String message)
  {
    super(message);
  }

  /**
   * Creates a new <code>ShutdownException</code> instance.
   * 
   * @param cause the root cause
   */
  public ShutdownException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Creates a new <code>ShutdownException</code> instance.
   * 
   * @param message the error message
   * @param cause the root cause
   */
  public ShutdownException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
