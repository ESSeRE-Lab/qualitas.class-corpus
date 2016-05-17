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
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.UnknownRequest;
import org.objectweb.cjdbc.controller.loadbalancer.AllBackendsFailedException;
import org.objectweb.cjdbc.controller.requestmanager.TransactionMarkerMetaData;
import org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager;

/**
 * Execute a distributed commit.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class Commit extends DistributedTransactionMarker
{
  private static final long                   serialVersionUID = 1222810057093662283L;

  private transient TransactionMarkerMetaData tm               = null;
  private transient Long                      tid;
  private transient int                       numberOfEnabledBackends;
  private transient boolean                   transactionStartedOnThisController;
  // Login that commits the transaction. This is used in case the remote
  // controller has to log the commit but didn't see the begin in which case it
  // will not be able to retrieve the transaction marker metadata
  private String                              login;

  /**
   * Creates a new <code>Commit</code> message.
   * 
   * @param login login that commit the transaction
   * @param transactionId id of the transaction to commit
   */
  public Commit(String login, long transactionId)
  {
    super(transactionId);
    this.login = login;
  }

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedTransactionMarker#scheduleCommand(org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager)
   */
  public void scheduleCommand(DistributedRequestManager drm)
      throws SQLException
  {
    transactionStartedOnThisController = true;
    tid = new Long(transactionId);
    try
    {
      tm = drm.getTransactionMarker(tid);
    }
    catch (SQLException ignore)
    {
      // The transaction was started before the controller joined the
      // cluster, build a fake tm so that we will be able to log it.
      transactionStartedOnThisController = false;
      tm = new TransactionMarkerMetaData(transactionId, 0, login);
      return;
    }

    try
    {
      // Post in the total order queue
      drm.getScheduler().commit(tm);
    }
    catch (SQLException e)
    {
      drm.getLogger().warn(
          Translate.get("virtualdatabase.distributed.commit.sqlexception"), e);
      throw e;
    }
    catch (RuntimeException re)
    {
      drm.getLogger().warn(
          Translate.get("virtualdatabase.distributed.commit.exception"), re);
      throw new SQLException(re.getMessage());
    }
  }

  /**
   * Execution of a distributed commit command on the specified
   * <code>DistributedRequestManager</code>
   * 
   * @param drm the DistributedRequestManager that will execute the commit
   * @return Boolean.TRUE if everything went fine or a SQLException if an error
   *         occured
   * @throws SQLException if an error occurs
   */
  public Object executeCommand(DistributedRequestManager drm)
      throws SQLException
  {
    Trace logger = drm.getLogger();
    numberOfEnabledBackends = drm.getLoadBalancer()
        .getNumberOfEnabledBackends();
    try
    {
      if (numberOfEnabledBackends == 0)
        throw new NoMoreBackendException(
            "No backend enabled on this controller");

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("transaction.commit", "" + tid));

      // Send to load balancer
      drm.getLoadBalancer().commit(tm);

      // Notify the cache
      if (drm.getResultCache() != null)
        drm.getResultCache().commit(tm.getTransactionId());

      // Notify the recovery log manager
      if (drm.getRecoveryLog() != null)
        drm.getRecoveryLog().logCommit(tm);
    }
    catch (NoMoreBackendException e)
    {
      // Log the query in any case for later recovery (if the request really
      // failed, it will be unloged later)
      if (drm.getRecoveryLog() != null)
      {
        if (logger.isDebugEnabled())
          logger
              .debug(Translate.get(
                  "virtualdatabase.distributed.commit.logging.only",
                  transactionId));

        if (numberOfEnabledBackends == 0)
        { // Wait to be sure that we log in the proper order
          Commit totalOrderCommit = new Commit(tm.getLogin(), transactionId);
          if (drm.getLoadBalancer().waitForTotalOrder(totalOrderCommit, false))
            drm.getLoadBalancer().removeHeadFromAndNotifyTotalOrderQueue();
        }
        long logId = drm.getRecoveryLog().logCommit(tm);
        e.setRecoveryLogId(logId);
        e.setLogin(tm.getLogin());
      }
      throw e;
    }
    catch (SQLException e)
    {
      logger.warn(Translate
          .get("virtualdatabase.distributed.commit.sqlexception"), e);
      return e;
    }
    catch (RuntimeException re)
    {
      logger.warn(
          Translate.get("virtualdatabase.distributed.commit.exception"), re);
      throw new SQLException(re.getMessage());
    }
    catch (AllBackendsFailedException e)
    {
      AbstractRequest request = new UnknownRequest("commit", false, 0, "\n");
      request.setTransactionId(transactionId);
      drm.addFailedOnAllBackends(request);
      if (logger.isDebugEnabled())
        logger.debug(Translate.get(
            "virtualdatabase.distributed.commit.all.backends.locally.failed",
            transactionId));
      return e;
    }
    finally
    {
      if (transactionStartedOnThisController)
      {
        // Notify scheduler for completion
        drm.getScheduler().commitCompleted(transactionId);
        drm.completeTransaction(tid);
      }
    }
    return Boolean.TRUE;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "Commit transaction " + transactionId;
  }
}