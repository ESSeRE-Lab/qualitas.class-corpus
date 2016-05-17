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

import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager;

/**
 * This class defines a CacheInvalidate
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class CacheInvalidate extends DistributedRequest
{
  private static final long serialVersionUID = -3330942475224539877L;

  /**
   * Creates a new <code>CacheInvalidate</code> object
   * 
   * @param request Write request that invalidates the cache
   */
  public CacheInvalidate(AbstractWriteRequest request)
  {
    super(request);
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
    // Notify cache if any
    if (drm.getResultCache() != null)
    { // Update cache
      drm.getResultCache().writeNotify((AbstractWriteRequest) request);
    }
    return null;
  }

}