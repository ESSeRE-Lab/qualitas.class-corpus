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
 * Initial developer(s): Mathieu Peltier.
 * Contributor(s): _______________________
 */

package org.objectweb.cjdbc.controller.authentication;

import org.objectweb.cjdbc.common.exceptions.CJDBCException;

/**
 * Authentication manager exception.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * @version 1.0
 */
public class AuthenticationManagerException extends CJDBCException
{
  private static final long serialVersionUID = 4789982508554668552L;

  /**
   * Creates a new <code>AuthenticationManagerException</code> instance.
   */
  public AuthenticationManagerException()
  {
  }

  /**
   * Creates a new <code>AuthenticationManagerException</code> instance.
   * 
   * @param message the error message
   */
  public AuthenticationManagerException(String message)
  {
    super(message);
  }

  /**
   * Creates a new <code>AuthenticationManagerException</code> instance.
   * 
   * @param cause the root cause
   */
  public AuthenticationManagerException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Creates a new <code>AuthenticationManagerException</code> instance.
   * 
   * @param message the error message
   * @param cause the root cause
   */
  public AuthenticationManagerException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
