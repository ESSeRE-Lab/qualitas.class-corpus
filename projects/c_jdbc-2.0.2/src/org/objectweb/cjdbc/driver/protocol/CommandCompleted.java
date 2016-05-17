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
 * Contributor(s): __________________.
 */

package org.objectweb.cjdbc.driver.protocol;

import org.objectweb.cjdbc.common.exceptions.CJDBCException;

/**
 * This exception is used by the controller to signal the driver from the
 * successful completion of a command.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class CommandCompleted extends CJDBCException
{
  private static final long serialVersionUID = -958502170680893639L;

  /**
   * @see org.objectweb.cjdbc.common.exceptions.CJDBCException#CJDBCException()
   */
  public CommandCompleted()
  {
    super();
  }

  /**
   * @see org.objectweb.cjdbc.common.exceptions.CJDBCException#CJDBCException(String)
   */
  public CommandCompleted(String message)
  {
    super(message);
  }

  /**
   * @see org.objectweb.cjdbc.common.exceptions.CJDBCException#CJDBCException(Throwable)
   */
  public CommandCompleted(Throwable cause)
  {
    super(cause);
  }

  /**
   * @see org.objectweb.cjdbc.common.exceptions.CJDBCException#CJDBCException(String,Throwable)
   */
  public CommandCompleted(String message, Throwable cause)
  {
    super(message, cause);
  }
}
