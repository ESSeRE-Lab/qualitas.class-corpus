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

package org.objectweb.cjdbc.controller.virtualdatabase.protocol;

import java.sql.SQLException;

import org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData;
import org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager;

/**
 * This class defines an UnlogCommit command that is sent when a commit has
 * failed on all controllers and it is necessary for controllers without backend
 * to remove this commit from their recovery log since they systematically log
 * all update statements.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class UnlogCommit extends DistributedRequest
{
  private static final long         serialVersionUID = -7792864497253772272L;

  private TransactionMarkerMetaData tm;

  /**
   * Creates a new <code>UnlogCommit</code> object
   * 
   * @param tm the identifier of the committed transaction to unlog from the
   *          recovery log on controller where the request should be unlogged
   */
  public UnlogCommit(TransactionMarkerMetaData tm)
  {
    super(null);
    this.tm = tm;
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
    drm.removeFailedCommitFromRecoveryLog(tm);
    return null;
  }

}