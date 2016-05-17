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

package org.objectweb.cjdbc.controller.virtualdatabase.protocol;

import java.io.Serializable;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager;

/**
 * <code>DistributedRequest</code> is an abstract class that defines the
 * interface for distributed execution of a request (horizontal scalability).
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public abstract class DistributedRequest implements Serializable
{
  protected AbstractRequest request;

  /**
   * Builds a new <code>DistributedRequest</code> object.
   * 
   * @param request request to execute
   */
  public DistributedRequest(AbstractRequest request)
  {
    this.request = request;
  }

  /**
   * Returns the request value.
   * 
   * @return Returns the request.
   */
  public final AbstractRequest getRequest()
  {
    return request;
  }

  /**
   * Schedule the request. This method blocks until the request is scheduled.
   * 
   * @param drm a distributed request manager
   * @throws SQLException if an error occurs.
   */
  public abstract void scheduleRequest(DistributedRequestManager drm)
      throws SQLException;

  /**
   * Code to be executed by the distributed request manager receiving the
   * request.
   * 
   * @param drm a distributed request manager
   * @return an Object to be sent back to the caller
   * @throws SQLException if an error occurs.
   */
  public abstract Object executeScheduledRequest(DistributedRequestManager drm)
      throws SQLException;

}