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

package org.objectweb.cjdbc.controller.recoverylog;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.notifications.CjdbcNotificationList;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.shared.BackendState;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer;
import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.AbstractTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.BeginTask;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.KillThreadTask;
import org.objectweb.cjdbc.controller.scheduler.AbstractScheduler;

/**
 * This class defines a RecoverThread that is in charge of replaying the
 * recovery log on a given backend to re-synchronize it with the other nodes of
 * the cluster.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class RecoverThread extends Thread
{
  static Trace                 logger = Trace.getLogger(RecoverThread.class
                                          .getName());

  private RecoveryLog          recoveryLog;
  private DatabaseBackend      backend;
  private AbstractLoadBalancer loadBalancer;
  private SQLException         exception;

  private BackendWorkerThread  bwt;
  private ArrayList            tids;

  private AbstractScheduler    scheduler;

  private String               checkpointName;

  /** Size of the pendingRecoveryTasks queue used during recovery */
  private int                  recoveryBatchSize;

  /**
   * Creates a new <code>RecoverThread</code> object
   * 
   * @param scheduler the currently used scheduler
   * @param recoveryLog Recovery log that creates this thread
   * @param backend database backend for logging
   * @param loadBalancer index to start from for recovery
   * @param checkpointName load balancer to use to create a BackendWorkerThread
   */
  public RecoverThread(AbstractScheduler scheduler, RecoveryLog recoveryLog,
      DatabaseBackend backend, AbstractLoadBalancer loadBalancer,
      String checkpointName)
  {
    this.scheduler = scheduler;
    this.recoveryLog = recoveryLog;
    this.backend = backend;
    this.loadBalancer = loadBalancer;
    this.checkpointName = checkpointName;
    this.recoveryBatchSize = recoveryLog.getRecoveryBatchSize();
    tids = new ArrayList();
  }

  /**
   * Returns the exception value.
   * 
   * @return Returns the exception.
   */
  public SQLException getException()
  {
    return exception;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    backend.setState(BackendState.REPLAYING);
    try
    {
      backend.initializeConnections();
    }
    catch (SQLException e)
    {
      recoveryFailed(e);
      return;
    }
    recoveryLog.beginRecovery();

    // Get the checkpoint from the recovery log
    long logIdx;
    try
    {
      logIdx = recoveryLog.getCheckpointRequestId(checkpointName);
    }
    catch (SQLException e)
    {
      recoveryLog.endRecovery();
      String msg = Translate.get("recovery.cannot.get.checkpoint", e);
      logger.error(msg);
      recoveryFailed(new SQLException(msg));
      return;
    }

    try
    {
      startRecovery();

      // Play writes from the recovery log until the last possible transaction
      // without blocking the scheduler
      logIdx = recover(logIdx);

      // Suspend the writes
      scheduler.suspendWrites();

      // Play the remaining writes that were pending
      logIdx = recover(logIdx);

    }
    catch (SQLException e)
    {
      recoveryFailed(e);
      return;
    }
    finally
    {
      endRecovery();
    }

    // Now enable it
    try
    {
      loadBalancer.enableBackend(backend, true);
      scheduler.resumeWrites();
    }
    catch (SQLException e)
    {
      recoveryFailed(e);
      return;
    }
    logger.info(Translate.get("backend.state.enabled", backend.getName()));
  }

  /**
   * Unset the last known checkpoint and set the backend to disabled state. This
   * should be called when the recovery has failed.
   * 
   * @param e cause of the recovery failure
   */
  private void recoveryFailed(SQLException e)
  {
    this.exception = e;

    if (scheduler.isSuspendedWrites())
      scheduler.resumeWrites();

    backend.setLastKnownCheckpoint(null);
    backend.setState(BackendState.DISABLED);
    backend.notifyJmxError(
        CjdbcNotificationList.VIRTUALDATABASE_BACKEND_REPLAYING_FAILED, e);
  }

  /**
   * Replay the recovery log from the given logIdx index. Note that
   * startRecovery() must have been called to fork and start the
   * BackendWorkerThread before calling recover. endRecovery() must be called
   * after recover() to terminate the thread.
   * 
   * @param logIdx logIdx used to start the recovery
   * @return last logIdx that was replayed.
   * @throws SQLException if fails
   * @see #startRecovery()
   * @see #endRecovery()
   */
  private long recover(long logIdx) throws SQLException
  {
    if (bwt == null)
      throw new RuntimeException(
          "No BackendWorkerThread to recover, you should have called RecoveryLog.startRecovery()");
    RecoveryTask recoveryTask = null;
    AbstractTask abstractTask = null;

    logger.info(Translate.get("recovery.start.process"));

    long tid;
    LinkedList pendingRecoveryTasks = new LinkedList();
    // Replay the whole log
    while (logIdx != -1)
    {
      try
      {
        recoveryTask = recoveryLog.recoverNextRequest(logIdx);
      }
      catch (SQLException e)
      {
        // Signal end of recovery and kill worker thread
        recoveryLog.endRecovery();
        addWorkerTask(bwt, new KillThreadTask(1, 1));
        String msg = Translate.get("recovery.cannot.recover.from.index", e);
        logger.error(msg, e);
        throw new SQLException(msg);
      }
      if (recoveryTask == null)
        break;
      tid = recoveryTask.getTid();
      if (tid != 0)
      {
        if (recoveryTask.getTask() instanceof BeginTask)
          tids.add(new Long(tid));
        else if (!tids.contains(new Long(tid)))
        {
          /*
           * if the task transaction id does not have a corresponding begin (it
           * is not in the tids arraylist), then this task has already been
           * played when the backend was disabled. So we can skip it.
           */
          logIdx++;
          continue;
        }
      } // else autocommit ok

      abstractTask = recoveryTask.getTask();
      logIdx = recoveryTask.getId();
      // Add the task for execution by the BackendWorkerThread
      addWorkerTask(bwt, abstractTask);
      // Add it to the list of currently executing tasks
      pendingRecoveryTasks.addLast(abstractTask);

      // Now let's check which task have completed and remove them from the
      // pending queue.
      do
      { // Take the first task of the list
        abstractTask = (AbstractTask) pendingRecoveryTasks.getFirst();
        if (abstractTask.hasFullyCompleted())
        {
          // Task has completed, remove it from the list
          pendingRecoveryTasks.removeFirst();
          if (abstractTask.getFailed() > 0)
          { // We fail to recover that task. Signal end of recovery and kill
            // worker thread
            recoveryLog.endRecovery();
            addWorkerTask(bwt, new KillThreadTask(1, 1));
            pendingRecoveryTasks.clear();
            String msg = Translate.get("recovery.failed.with.error",
                new String[]{
                    abstractTask.toString(),
                    ((Exception) abstractTask.getExceptions().get(0))
                        .getMessage()});
            logger.error(msg);
            throw new SQLException(msg);
          }
        }
        else
        { // Task has not completed yet
          if (pendingRecoveryTasks.size() > recoveryBatchSize)
          { // Queue is full, wait for first query to complete
            synchronized (abstractTask)
            {
              if (!abstractTask.hasFullyCompleted())
                try
                {
                  abstractTask.wait();
                }
                catch (InterruptedException ignore)
                {
                }
              // Let's check the task completion status by restarting the loop
              continue;
            }
          }
          else
            // All current queries are executing but the queue is not full,
            // let's add some more
            break;
        }
      }
      while (!pendingRecoveryTasks.isEmpty());
    } // while we have not reached the last query

    // Ok, now everything has been replayed but we have to wait for the last
    // queries in the pending queue to complete.
    while (!pendingRecoveryTasks.isEmpty())
    {
      abstractTask = (AbstractTask) pendingRecoveryTasks.remove(0);
      synchronized (abstractTask)
      {
        // Wait for task completion if needed
        while (!abstractTask.hasFullyCompleted())
          try
          {
            abstractTask.wait();
          }
          catch (InterruptedException ignore)
          {
          }

        // Check if tasks completed successfully
        if (abstractTask.getFailed() > 0)
        { // We fail to recover that task. Signal end of recovery and kill
          // worker thread
          recoveryLog.endRecovery();
          addWorkerTask(bwt, new KillThreadTask(1, 1));
          pendingRecoveryTasks.clear();
          String msg = Translate.get("recovery.failed.with.error",
              new String[]{
                  abstractTask.toString(),
                  ((Exception) abstractTask.getExceptions().get(0))
                      .getMessage()});
          logger.error(msg);
          throw new SQLException(msg);
        }
      }
    }
    return logIdx;
  }

  /**
   * Add a task to a BackendWorkerThread using the proper synchronization.
   * 
   * @param bwt BackendWorkerThread to synchronize on
   * @param task the task to add to the thread queue
   */
  private void addWorkerTask(BackendWorkerThread bwt, AbstractTask task)
  {
    synchronized (bwt)
    {
      bwt.addTask(task);
      bwt.notify();
    }
  }

  /**
   * Properly end the recovery and kill the worker thread used for recovery if
   * it exists.
   * 
   * @see #startRecovery()
   */
  public void endRecovery()
  {
    // We are done with the recovery
    logger.info(Translate.get("recovery.process.complete"));
    if (bwt != null)
    {
      addWorkerTask(bwt, new KillThreadTask(1, 1));
      try
      {
        bwt.join();
      }
      catch (InterruptedException e)
      {
        recoveryLog.endRecovery();
        String msg = Translate.get("recovery.join.failed", e);
        logger.error(msg, e);
        exception = new SQLException(msg);
      }
    }

    recoveryLog.endRecovery();
  }

  /**
   * Start the recovery process by forking a BackendWorkerThread. You must call
   * endRecovery() to terminate the thread.
   * 
   * @throws SQLException if an error occurs
   * @see #endRecovery()
   */
  public void startRecovery() throws SQLException
  {
    bwt = new BackendWorkerThread("Worker thread for recovery on backend:"
        + backend.getName(), backend, loadBalancer);
    bwt.start();
  }
}