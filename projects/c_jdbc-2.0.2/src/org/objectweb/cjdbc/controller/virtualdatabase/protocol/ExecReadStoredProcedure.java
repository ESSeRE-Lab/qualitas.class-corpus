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

import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.controller.loadbalancer.AllBackendsFailedException;
import org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * This class defines a ExecReadStoredProcedure
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ExecReadStoredProcedure extends DistributedRequest
{
  private static final long serialVersionUID = 8634424524848530342L;

  private int               numberOfEnabledBackends;

  /**
   * Creates a new <code>ExecReadStoredProcedure</code> object to execute a
   * read stored procedure on multiple controllers.
   * 
   * @param proc the stored procedure to execute
   */
  public ExecReadStoredProcedure(StoredProcedure proc)
  {
    super(proc);
  }

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedRequest#scheduleRequest(org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager)
   */
  public void scheduleRequest(DistributedRequestManager drm)
      throws SQLException
  {
    // Even if we do not execute this query, we have to log the begin if any
    drm.lazyTransactionStart(request);
    drm.scheduleStoredProcedure((StoredProcedure) request);
  }

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedRequest#executeScheduledRequest(org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager)
   */
  public Object executeScheduledRequest(DistributedRequestManager drm)
      throws SQLException
  {
    ControllerResultSet result = null;
    StoredProcedure proc = (StoredProcedure) request;
    numberOfEnabledBackends = drm.getLoadBalancer()
        .getNumberOfEnabledBackends();
    try
    {
      if (numberOfEnabledBackends == 0)
        throw new NoMoreBackendException(
            "No backend enabled on this controller");

      if (drm.getLogger().isDebugEnabled())
        drm.getLogger().debug(
            Translate.get("requestmanager.read.stored.procedure", new String[]{
                String.valueOf(request.getId()),
                request.getSQLShortForm(drm.getVirtualDatabase()
                    .getSQLShortFormLength())}));

      result = drm.loadBalanceReadStoredProcedure(proc);

      drm.flushCacheAndLogStoredProcedure(proc, true);

      // Notify scheduler of completion
      drm.getScheduler().storedProcedureCompleted(proc);

      return result;
    }
    catch (NoMoreBackendException e)
    {
      if (drm.getLogger().isDebugEnabled())
        drm.getLogger().debug(
            Translate.get(
                "virtualdatabase.distributed.read.procedure.logging.only",
                request.getSQLShortForm(drm.getVirtualDatabase()
                    .getSQLShortFormLength())));
      // Log the query in any case for later recovery (if the request really
      // failed, it will be unloged later)
      if (drm.getRecoveryLog() != null)
      {
        if (numberOfEnabledBackends == 0)
        { // Wait to be sure that we log in the proper order
          if (drm.getLoadBalancer().waitForTotalOrder(request, false))
            drm.getLoadBalancer().removeHeadFromAndNotifyTotalOrderQueue();
        }
        long logId = drm.getRecoveryLog().logRequest(proc, true);
        e.setRecoveryLogId(logId);
      }
      // Notify scheduler of completion if the query was scheduled
      drm.getScheduler().storedProcedureCompleted(proc);
      throw e;
    }
    catch (AllBackendsFailedException e)
    {
      // Add to failed list, the scheduler will be notified when the response
      // will be received from the other controllers.
      drm.getScheduler().storedProcedureCompleted(proc);
      drm.addFailedOnAllBackends(request);
      if (drm.getLogger().isDebugEnabled())
        drm
            .getLogger()
            .debug(
                Translate
                    .get(
                        "virtualdatabase.distributed.read.procedure.all.backends.locally.failed",
                        request.getSQLShortForm(drm.getVirtualDatabase()
                            .getSQLShortFormLength())));
      return e;
    }
    catch (SQLException e)
    {
      // Something bad more likely happened during the notification. Let's
      // notify the scheduler (possibly again) to be safer.
      drm.getScheduler().storedProcedureCompleted(proc);
      drm.getLogger().warn(
          Translate.get(
              "virtualdatabase.distributed.read.procedure.sqlexception", e
                  .getMessage()), e);
      throw e;
    }
    catch (RuntimeException re)
    {
      // Something bad more likely happened during the notification. Let's
      // notify the scheduler (possibly again) to be safer.
      drm.getScheduler().storedProcedureCompleted(proc);
      drm.getLogger().warn(
          Translate.get("virtualdatabase.distributed.read.procedure.exception",
              re.getMessage()), re);
      throw new SQLException(re.getMessage());
    }
  }
}