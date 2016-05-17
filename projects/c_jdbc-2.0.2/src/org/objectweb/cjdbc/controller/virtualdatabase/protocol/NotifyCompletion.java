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

import java.sql.SQLException;

import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager;

/**
 * This class defines a NotifyCompletion command that is sent to controllers
 * that have failed to notify of the success or failure of the request
 * execution.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class NotifyCompletion extends DistributedRequest
{
  private static final long serialVersionUID = -6229483423511152134L;

  private boolean           success;

  /**
   * Creates a new <code>NotifyCompletion</code> object
   * 
   * @param request the request that completed
   * @param success true if completion is successful, false if it is a failure
   */
  public NotifyCompletion(AbstractRequest request, boolean success)
  {
    super(request);
    this.success = success;
  }

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedRequest#scheduleRequest(org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager)
   */
  public final void scheduleRequest(DistributedRequestManager drm)
      throws SQLException
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedRequest#executeScheduledRequest(org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager)
   */
  public final Object executeScheduledRequest(DistributedRequestManager drm)
      throws SQLException
  {
    drm.completeFailedOnAllBackends(request, success);
    return null;
  }

}