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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.common.exceptions;

import java.sql.SQLException;

/**
 * This class defines a NoMoreBackendException. This means that a controller
 * does not have any backend left to execute the query. The exception might
 * carry an optional identifier of the request that failed. This is useful to
 * unlog a remote request that has failed since each controller has its own
 * local id for each distributed request.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class NoMoreBackendException extends SQLException
{
  private static final long serialVersionUID = 8265839783849395122L;

  private long   recoveryLogId = 0;
  private String login         = null;

  /**
   * Creates a new <code>NoMoreBackendException</code> object
   */
  public NoMoreBackendException()
  {
    super();
  }

  /**
   * Creates a new <code>NoMoreBackendException</code> object
   * 
   * @param reason the error message
   */
  public NoMoreBackendException(String reason)
  {
    super(reason);
  }

  /**
   * Creates a new <code>NoMoreBackendException</code> object
   * 
   * @param reason the error message
   * @param sqlState the SQL state
   */
  public NoMoreBackendException(String reason, String sqlState)
  {
    super(reason, sqlState);
  }

  /**
   * Creates a new <code>NoMoreBackendException</code> object
   * 
   * @param reason the error message
   * @param sqlState the SQL state
   * @param vendorCode vendor specific code
   */
  public NoMoreBackendException(String reason, String sqlState, int vendorCode)
  {
    super(reason, sqlState, vendorCode);
  }

  /**
   * Returns the recovery log id of the request that failed.
   * 
   * @return Returns the recoveryLogId.
   */
  public long getRecoveryLogId()
  {
    return recoveryLogId;
  }

  /**
   * Sets the recovery log id of the request that failed.
   * 
   * @param recoveryLogId The recoveryLogId to set.
   */
  public void setRecoveryLogId(long recoveryLogId)
  {
    this.recoveryLogId = recoveryLogId;
  }

  /**
   * Returns the login of the request that failed.
   * 
   * @return Returns the login.
   */
  public String getLogin()
  {
    return login;
  }

  /**
   * Sets the login of the request that failed.
   * 
   * @param login The login to set.
   */
  public void setLogin(String login)
  {
    this.login = login;
  }
}