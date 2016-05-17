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
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.common.exceptions;

/**
 * Virtual database exceptions.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class VirtualDatabaseException extends Exception
{
  private static final long serialVersionUID = -5359061849424706682L;

  /**
   * Creates a new <code>JmxException</code> instance.
   */
  public VirtualDatabaseException()
  {
  }

  /**
   * Creates a new <code>JmxException</code> instance.
   * 
   * @param message the error message
   */
  public VirtualDatabaseException(String message)
  {
    super(message);
  }

  /**
   * Creates a new <code>JmxException</code> instance.
   * 
   * @param cause the root cause
   */
  public VirtualDatabaseException(Throwable cause)
  {
    super(cause.getMessage());
  }

  /**
   * Creates a new <code>JmxException</code> instance.
   * 
   * @param message the error message
   * @param cause the root cause
   */
  public VirtualDatabaseException(String message, Throwable cause)
  {
    super(message, cause);
  }
}