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

import java.sql.SQLException;

/**
 * This class defines a NoMoreControllerException. This exception is thrown when
 * all controllers in a C-JDBC URL are unavailable.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class NoMoreControllerException extends SQLException
{
  private static final long serialVersionUID = -1970216751572913552L;

  /**
   * Creates a new <code>NoMoreControllerException</code> object
   */
  public NoMoreControllerException()
  {
    super();
  }

  /**
   * Creates a new <code>NoMoreControllerException</code> object
   * 
   * @param reason the error message
   */
  public NoMoreControllerException(String reason)
  {
    super(reason);
  }

  /**
   * Creates a new <code>NoMoreControllerException</code> object
   * 
   * @param reason the error message
   * @param sqlState the SQL state
   */
  public NoMoreControllerException(String reason, String sqlState)
  {
    super(reason, sqlState);
  }

  /**
   * Creates a new <code>NoMoreControllerException</code> object
   * 
   * @param reason the error message
   * @param sqlState the SQL state
   * @param vendorCode vendor specific code
   */
  public NoMoreControllerException(String reason, String sqlState,
      int vendorCode)
  {
    super(reason, sqlState, vendorCode);
  }

}
