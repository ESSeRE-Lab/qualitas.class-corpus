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

package org.objectweb.cjdbc.controller.core.shutdown;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.controller.recoverylog.RecoveryLog;
import org.objectweb.cjdbc.controller.virtualdatabase.DistributedVirtualDatabase;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread;

/**
 * Abstract class for all implementations of virtual database shutdown
 * strategies.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public abstract class VirtualDatabaseShutdownThread extends ShutdownThread
{
  protected VirtualDatabase virtualDatabase;

  /**
   * Prepare the thread for shutting down.
   * 
   * @param vdb the database to shutdown
   * @param level Constants.SHUTDOWN_WAIT, Constants.SHUTDOWN_SAFE or
   *          Constants.SHUTDOWN_FORCE
   */
  public VirtualDatabaseShutdownThread(VirtualDatabase vdb, int level)
  {
    super(level);
    this.virtualDatabase = vdb;
  }

  /**
   * Shutdown the result cache, recovery log and close the distributed virtual
   * database group communication channel (if the virtual database is
   * distributed).
   */
  protected void shutdownCacheRecoveryLogAndGroupCommunication()
  {
    // Shutdown the result cache
    AbstractResultCache resultCache = virtualDatabase.getRequestManager()
        .getResultCache();
    if (resultCache != null)
      resultCache.shutdown();

    // Shutdown the recovery log
    RecoveryLog recoveryLog = virtualDatabase.getRequestManager()
        .getRecoveryLog();
    if (recoveryLog != null)
      recoveryLog.shutdown();

    if (virtualDatabase.isDistributed())
    {
      logger.info("Shutting down group communication");
      try
      {
        ((DistributedVirtualDatabase) virtualDatabase).quitChannel();
      }
      catch (Exception e)
      {
        logger
            .warn(
                "An error occured while shutting down the group communication channel",
                e);
      }
    }
  }

  /**
   * Disable all database backends with a checkpoint named after the current
   * time if a recovery log is available.
   */
  protected void disableAllBackends()
  {
    if (virtualDatabase.getRequestManager().getRecoveryLog() != null)
    {
      try
      { // disable and checkpoint for recovery log
        virtualDatabase.storeBackendsInfo();
        virtualDatabase.disableAllBackendsWithCheckpoint(new Date().toString());
      }
      catch (Exception ve)
      {
        logger.error(Translate
            .get("controller.shutdown.backends.exception", ve));
      }
    }
    else
    { // no recovery log, so just disable backends
      try
      {
        virtualDatabase.disableAllBackends();
      }
      catch (Exception vde)
      {
        logger.error(Translate.get("controller.shutdown.backends.exception",
            vde));
      }
    }
  }

  /**
   * Terminate the VirtualDatabaseWorkerThreads
   */
  protected void terminateVirtualDatabaseWorkerThreads()
  {
    ArrayList threads = virtualDatabase.getActiveThreads();
    logger.info(Translate.get("controller.shutdown.active.threads", threads
        .size()));
    VirtualDatabaseWorkerThread wt;
    synchronized (threads)
    {
      for (int i = 0; i < threads.size(); i++)
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("controller.shutdown.database.thread",
              new String[]{virtualDatabase.getVirtualDatabaseName(),
                  String.valueOf(i)}));
        wt = ((VirtualDatabaseWorkerThread) threads.get(i));
        wt.shutdown();
      }
      threads.clear();
    }

    // Kill inactive threads
    virtualDatabase.setPoolConnectionThreads(false);
    ArrayList idleThreads = virtualDatabase.getPendingConnections();
    synchronized (idleThreads)
    {
      idleThreads.notifyAll();
    }
  }

  /**
   * Wait for all VirtualDatabaseWorkerThreads to terminate when all clients
   * have disconnected.
   */
  protected void waitForClientsToDisconnect()
  {
    boolean wait = true;
    while (wait)
    {
      ArrayList threads = virtualDatabase.getActiveThreads();
      synchronized (threads)
      {
        int nbThreads = threads.size();
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("controller.shutdown.active.threads",
              nbThreads));
        if (nbThreads == 0)
          wait = false;
      }
      if (wait)
      {
        synchronized (this)
        {
          try
          {
            wait(1000);
          }
          catch (InterruptedException e)
          {
            // Ignore
          }
        }
      }
    }
  }

  /**
   * Wait for currently open transactions and pending writes to complete (in
   * this order: 1.transaction, 2.writes).
   */
  protected void waitForTransactionsAndWritesToComplete()
  {
    try
    {
      virtualDatabase.getRequestManager().getScheduler()
          .suspendNewTransactionsForCheckpoint();
    }
    catch (SQLException e)
    {
      logger
          .error(
              "An error occured while waiting for current transactions to complete.",
              e);
    }
    try
    {
      virtualDatabase.getRequestManager().getScheduler().suspendWrites();
    }
    catch (SQLException e)
    {
      logger.error(
          "An error occured while waiting for pending writes to complete.", e);
    }
  }

}