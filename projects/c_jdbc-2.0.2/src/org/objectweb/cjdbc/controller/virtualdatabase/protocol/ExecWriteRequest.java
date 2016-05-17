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
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.controller.loadbalancer.AllBackendsFailedException;
import org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager;

/**
 * Execute a write request between several controllers.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ExecWriteRequest extends DistributedRequest
{
  private static final long serialVersionUID = -4849018153223926739L;
  private int               numberOfEnabledBackends;

  /**
   * @param request write request to execute
   */
  public ExecWriteRequest(AbstractWriteRequest request)
  {
    super(request);
  }

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedRequest#scheduleRequest(org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager)
   */
  public void scheduleRequest(DistributedRequestManager drm)
      throws SQLException
  {
    // This call will trigger a lazyTransactionStart as well
    drm.scheduleExecWriteRequest((AbstractWriteRequest) request);
  }

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedRequest#executeScheduledRequest(org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager)
   */
  public Object executeScheduledRequest(DistributedRequestManager drm)
      throws SQLException
  {
    numberOfEnabledBackends = drm.getLoadBalancer()
        .getNumberOfEnabledBackends();

    try
    {
      int execWriteRequestResult = 0;
      if (numberOfEnabledBackends == 0)
        throw new NoMoreBackendException(
            "No backend enabled on this controller");
      execWriteRequestResult = drm
          .loadBalanceExecWriteRequest((AbstractWriteRequest) request);
      drm.updateAndNotifyExecWriteRequest((AbstractWriteRequest) request);
      return new Integer(execWriteRequestResult);
    }
    catch (NoMoreBackendException e)
    {
      if (drm.getLogger().isDebugEnabled())
        drm.getLogger().debug(
            Translate.get("virtualdatabase.distributed.write.logging.only",
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
        long logId = drm.getRecoveryLog().logRequest(
            (AbstractWriteRequest) request);
        e.setRecoveryLogId(logId);
      }
      // Notify scheduler of completion
      drm.getScheduler().writeCompleted((AbstractWriteRequest) request);
      throw e;
    }
    catch (AllBackendsFailedException e)
    {
      // Add to failed list, the scheduler will be notified when the response
      // will be received from the other controllers.
      drm.getScheduler().writeCompleted((AbstractWriteRequest) request);
      drm.addFailedOnAllBackends(request);
      if (drm.getLogger().isDebugEnabled())
        drm
            .getLogger()
            .debug(
                Translate
                    .get(
                        "virtualdatabase.distributed.write.all.backends.locally.failed",
                        request.getSQLShortForm(drm.getVirtualDatabase()
                            .getSQLShortFormLength())));
      return e;
    }
    catch (SQLException e)
    {
      // Something bad more likely happened during the notification. Let's
      // notify the scheduler (possibly again) to be safer.
      drm.getScheduler().writeCompleted((AbstractWriteRequest) request);
      drm.getLogger().warn(
          Translate.get("virtualdatabase.distributed.write.sqlexception", e
              .getMessage()), e);
      throw e;
    }
    catch (RuntimeException re)
    {
      // Something bad more likely happened during the notification. Let's
      // notify the scheduler (possibly again) to be safer.
      drm.getScheduler().writeCompleted((AbstractWriteRequest) request);
      drm.getLogger().warn(
          Translate.get("virtualdatabase.distributed.write.exception", re
              .getMessage()), re);
      throw new SQLException(re.getMessage());
    }
  }

}