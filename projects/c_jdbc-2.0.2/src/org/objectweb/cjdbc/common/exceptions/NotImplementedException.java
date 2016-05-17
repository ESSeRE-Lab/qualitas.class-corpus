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
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.common.exceptions;

import java.sql.SQLException;

/**
 * This exception is thrown for all non implemented features in the C-JDBC
 * driver.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class NotImplementedException extends SQLException
{
  private static final long serialVersionUID = 6615147787748938642L;

  /**
   * Creates a new <code>NotImplementedException</code> instance.
   * 
   * @param reason the exception cause
   * @param sqlState the SQL state
   * @param vendorCode the vendor code
   */
  public NotImplementedException(
    String reason,
    String sqlState,
    int vendorCode)
  {
    super(reason, sqlState, vendorCode);
  }

  /**
   * Creates a new <code>NotImplementedException</code> instance.
   * 
   * @param reason the exception cause
   * @param sqlState the SQL state
   */
  public NotImplementedException(String reason, String sqlState)
  {
    super(reason, sqlState);
  }

  /**
   * Creates a new <code>NotImplementedException</code> instance.
   * 
   * @param callingMethod the calling method that failed
   */
  public NotImplementedException(String callingMethod)
  {
    super(callingMethod + " not implemented");
  }

  /**
   * Creates a new <code>NotImplementedException</code> instance.
   */
  public NotImplementedException()
  {
    super("Feature not implemented");
  }
}
