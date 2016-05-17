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

import java.sql.SQLException;

/**
 * This class defines a NoTransactionStartWhenDisablingException. It is thrown
 * when someone tries to start a new transaction on a backend that is disabling.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class NoTransactionStartWhenDisablingException extends SQLException
{
  private static final long serialVersionUID = -9008333075362715240L;

  /**
   * Creates a new <code>NoTransactionStartWhenDisablingException</code>
   * object
   */
  public NoTransactionStartWhenDisablingException()
  {
    super();
  }

  /**
   * Creates a new <code>NoTransactionStartWhenDisablingException</code>
   * object
   * 
   * @param reason the error message
   */
  public NoTransactionStartWhenDisablingException(String reason)
  {
    super(reason);
  }

  /**
   * Creates a new <code>NoTransactionStartWhenDisablingException</code>
   * object
   * 
   * @param reason the error message
   * @param sqlState the SQL state
   */
  public NoTransactionStartWhenDisablingException(String reason, String sqlState)
  {
    super(reason, sqlState);
  }

  /**
   * Creates a new <code>NoTransactionStartWhenDisablingException</code>
   * object
   * 
   * @param reason the error message
   * @param sqlState the SQL state
   * @param vendorCode vendor specific code
   */
  public NoTransactionStartWhenDisablingException(String reason,
      String sqlState, int vendorCode)
  {
    super(reason, sqlState, vendorCode);
  }

}