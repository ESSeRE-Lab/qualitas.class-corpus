/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
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
 * This class defines an AuthenticationException in case the authentication with
 * the controller fails.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class AuthenticationException extends CJDBCException
{
  private static final long serialVersionUID = 933382937208867189L;

  /**
   * Creates a new <code>AuthenticationException</code> object
   */
  public AuthenticationException()
  {
    super();
  }

  /**
   * Creates a new <code>AuthenticationException</code> object
   * 
   * @param message the error message
   */
  public AuthenticationException(String message)
  {
    super(message);
  }

  /**
   * Creates a new <code>AuthenticationException</code> object
   * 
   * @param cause the root cause
   */
  public AuthenticationException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Creates a new <code>AuthenticationException</code> object
   * 
   * @param message the error message
   * @param cause the root cause
   */
  public AuthenticationException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
