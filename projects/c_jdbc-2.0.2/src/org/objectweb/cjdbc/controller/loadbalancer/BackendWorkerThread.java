/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
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
 * Contributor(s): 
 */

package org.objectweb.cjdbc.controller.loadbalancer;

import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.AbstractTask;

/**
 * Process sequentially a set of tasks and send them to a backend.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class BackendWorkerThread extends Thread
{
  //
  // How the code is organized ?
  // 1. Member variables
  // 2. Constructor(s)
  // 3. Task management
  // 4. Getter/Setters
  //

  private AbstractLoadBalancer loadBalancer;
  private DatabaseBackend      backend;
  private ArrayList            taskList;
  private ArrayList            tidList;
  private boolean              isKilled = false;

  // not null if we are currently processing a task
  private AbstractTask         currentlyProcessingTask;
  // Tid of the current task if currentlyProcessingTask.hasTid()==true
  private Long                 currentTaskTid;

  private Trace                logger   = null;

  /*
   * Constructor
   */

  /**
   * Creates a new <code>BackendWorkerThread</code>.
   * 
   * @param backend the backend this thread is associated to.
   * @param loadBalancer the load balancer instanciating this thread
   * @throws SQLException if an error occurs
   */
  public BackendWorkerThread(DatabaseBackend backend,
      AbstractLoadBalancer loadBalancer) throws SQLException
  {
    this("BackendWorkerThread for backend '" + backend.getName()
        + "' with RAIDb level:" + loadBalancer.getRAIDbLevel(), backend,
        loadBalancer);
  }

  /**
   * Creates a new <code>BackendWorkerThread</code>.
   * 
   * @param name the name to give to the thread
   * @param backend the backend this thread is associated to.
   * @param loadBalancer the load balancer instanciating this thread
   * @throws SQLException if an error occurs
   */
  public BackendWorkerThread(String name, DatabaseBackend backend,
      AbstractLoadBalancer loadBalancer) throws SQLException
  {
    super(name);
    // Sanity checks
    if (backend == null)
    {
      String msg = Translate.get("backendworkerthread.null.backend");
      logger = Trace
          .getLogger("org.objectweb.cjdbc.controller.backend.DatabaseBackend");
      logger.error(msg);
      throw new SQLException(msg);
    }

    backend.checkDriverCompliance();

    logger = Trace
        .getLogger("org.objectweb.cjdbc.controller.backend.DatabaseBackend."
            + backend.getName());

    if (loadBalancer == null)
    {
      String msg = Translate.get("backendworkerthread.null.loadbalancer");
      logger.error(msg);
      throw new SQLException(msg);
    }

    this.backend = backend;
    this.loadBalancer = loadBalancer;
    taskList = new ArrayList();
    tidList = new ArrayList();
  }

  /*
   * Task Management
   */

  /**
   * Adds a task at the end of the task list. Warning! This method is not
   * synchronized and the caller must synchronize on the thread before calling
   * this method.
   * 
   * @param task the task to add
   */
  public void addTask(AbstractTask task)
  {
    if (!isKilled)
    {
      taskList.add(task);
      // We assume that all requests here are writes
      backend.addPendingWriteRequest(task);
    }
    else
      task.notifyCompletion();
  }

  /**
   * Adds a task at the end of the task list. Warning! This method is not
   * synchronized and the caller must synchronize on the thread before calling
   * this method.
   * 
   * @param task the task to add
   * @param transactionId transaction id in which this task execute
   */
  public void addTask(AbstractTask task, long transactionId)
  {
    if (!isKilled)
    {
      tidList.add(new Long(transactionId));
      task.setHasTid(true);
      addTask(task);
    }
    else
      task.notifyCompletion();
  }

  /**
   * Adds a task just after the last write task for the given transaction in the
   * task list. Warning! This method is not synchronized and the caller must
   * synchronize on the thread before calling this method.
   * <p>
   * This method is usually used to insert a commit/rollback task when
   * asynchrony is allowed between backends.
   * 
   * @param task the task to add
   * @param transactionId transaction id in which this task execute
   */
  public void insertTaskAfterLastWriteForTransaction(AbstractTask task,
      Long transactionId)
  {
    if (!isKilled)
    {
      task.setHasTid(true);

      // Find the last task index in the tid queue
      int lastTidIndex = tidList.lastIndexOf(transactionId);
      if (lastTidIndex == -1)
      { // Not found, add in last position
        taskList.add(task);
        tidList.add(transactionId);
        backend.addPendingWriteRequest(task);
        return;
      }

      // Find the corresponding task in the task list (we have to skip
      // autocommit tasks)
      int lastRequestIndex = 0;
      while (lastTidIndex >= 0)
      {
        AbstractTask t = (AbstractTask) taskList.get(lastRequestIndex);
        if (t.hasTid())
          lastTidIndex--;
        lastRequestIndex++;
      }

      // Add the task after the last write task and the tid in the tid list.
      taskList.add(lastRequestIndex, task);
      tidList.add(lastTidIndex + 1, transactionId);
      // Warning, the task is added in queue (not sorted) in the backend pending
      // request list.
      backend.addPendingWriteRequest(task);
    }
    else
      task.notifyCompletion();
  }

  /**
   * Adds a task upfront to the task list so that this task will be the next
   * executed task. Warning! This method is not synchronized and the caller must
   * synchronize on the thread before calling this method.
   * 
   * @param task the task to add
   */
  public void addPriorityTask(AbstractTask task)
  {
    if (!isKilled)
    {
      taskList.add(0, task);
      // We assume that all requests here are writes
      backend.addPendingWriteRequest(task);
    }
    else
      task.notifyCompletion();
  }

  /**
   * Adds a task upfront to the task list so that this task will be the next
   * executed task. Warning! This method is not synchronized and the caller must
   * synchronize on the thread before calling this method
   * 
   * @param task the task to add
   * @param transactionId transaction id in which this task execute
   */
  public void addPriorityTask(AbstractTask task, long transactionId)
  {
    if (!isKilled)
    {
      task.setHasTid(true);
      addPriorityTask(task);
      tidList.add(0, new Long(transactionId));
    }
    else
      task.notifyCompletion();
  }

  /**
   * Returns true if the thread has pending tasks for the given transaction.
   * 
   * @param tid the transaction identifier
   * @return true if the task list contains task(s) for transaction tid.
   */
  public boolean hasTaskForTransaction(Long tid)
  {
    synchronized (this)
    {
      if ((currentTaskTid != null) && (currentTaskTid.equals(tid)))
        // Currently executing task belong to this transaction
        return true;
      else
        return tidList.contains(tid);
    }
  }

  /**
   * Waits for all tasks of the specified transaction to complete.
   * 
   * @param transactionId the transaction identifier
   */
  public void waitForAllTasksToComplete(long transactionId)
  {
    if ((transactionId == 0) || (tidList == null))
      return;

    Long tid = new Long(transactionId);
    synchronized (this)
    {
      if (!tidList.contains(tid))
      {
        if ((currentTaskTid != null)
            && (currentTaskTid.longValue() == transactionId))
        {
          try
          {
            if (logger.isDebugEnabled())
              logger.debug(Translate.get("backendworkerthread.waiting.task"));
            wait();
          }
          catch (InterruptedException ignore)
          {
          }
          return;
        }
        else
          return;
      }

      while (tidList.contains(tid))
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("backendworkerthread.waiting.transaction",
              String.valueOf(tid)));

        try
        {
          wait();
        }
        catch (InterruptedException ignore)
        {
        }
      }
    }
  }

  /**
   * Waits for all current tasks to complete.
   */
  public void waitForAllTasksToComplete()
  {
    synchronized (this)
    {
      Object current;
      if (taskList.size() == 0)
      {
        if (currentlyProcessingTask != null)
        {
          try
          {
            if (logger.isDebugEnabled())
              logger.debug(Translate.get("backendworkerthread.waiting.task"));
            wait();
          }
          catch (InterruptedException ignore)
          {
            logger.warn(Translate
                .get("backendworkerthread.no.full.task.synchronization"));
          }
          return;
        }
        else
        { // No task currently executing
          return;
        }
      }
      else
        current = taskList.get(taskList.size() - 1);

      if (logger.isDebugEnabled())
        logger.debug(Translate.get("backendworkerthread.waiting.request",
            current.toString()));

      while (taskList.contains(current))
      {
        try
        {
          wait();
        }
        catch (InterruptedException ignore)
        {
        }
      }
    }
  }

  /**
   * Kills this thread after the next task processing and forces the load
   * balancer to disable the backend. It also marks all remaining tasks in the
   * task list as failed.
   */
  public void kill()
  {
    kill(true);
  }

  /**
   * Kills this thread after the next task processing. It also marks all
   * remaining tasks in the task list as failed.
   */
  public void killWithoutDisablingBackend()
  {
    kill(false);
  }

  /**
   * Kills this thread after the next task processing. It also marks all
   * remaining tasks in the task list as failed.
   * 
   * @param forceDisable true if the task must call the load balancer to disable
   *          the backend
   */
  private void kill(boolean forceDisable)
  {
    synchronized (this)
    {
      if (backend.isKilled())
        return;

      String msg = this.getName() + " is shutting down";
      logger.info(msg);

      // Remove all tasks
      while (!taskList.isEmpty())
      {
        AbstractTask task = (AbstractTask) taskList.remove(0);
        try
        {
          task.notifyFailure(this, 1, new SQLException(msg));
        }
        catch (SQLException ignore)
        {
        }
      }
      isKilled = true;
      notify(); // Wake up thread
    }
    if (forceDisable)
    {
      try
      {
        // This ensure that all worker threads get removed from the load
        // balancer
        // list and that the backend state is set to disable.
        loadBalancer.disableBackend(backend);
      }
      catch (SQLException ignore)
      {
      }
    }
  }

  /**
   * Process the tasklist and call <code>wait()</code> (on itself) when the
   * tasklist becomes empty.
   */
  public void run()
  {
    currentlyProcessingTask = null;

    while (!isKilled)
    {
      synchronized (this)
      {
        while (taskList.isEmpty() && !isKilled)
        { // Nothing to do, go to bed!
          try
          {
            wait();
          }
          catch (InterruptedException e)
          {
            logger.warn(Translate.get("backendworkerthread.wait.interrupted"));
            break;
          }
        }
        try
        { // Take the 1st task from the list
          currentlyProcessingTask = (AbstractTask) taskList.remove(0);
          if (currentlyProcessingTask.hasTid())
            currentTaskTid = (Long) tidList.remove(0);
          else
            currentTaskTid = null;
        }
        catch (IndexOutOfBoundsException oob)
        {
          // Should only happen if the thread was interrupted (see above)
          logger.warn(Translate.get("backendworkerthread.no.task"), oob);
          currentlyProcessingTask = null;
        }
      }
      // Execute the task out of the sync block
      try
      {
        if (currentlyProcessingTask == null)
        {
          logger.warn("Null task in BackendWorkerThread");
          continue;
        }
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("backendworkerthread.execute.task",
              currentlyProcessingTask.toString()));
        currentlyProcessingTask.execute(this);
      }
      catch (SQLException e)
      {
        // Task should have notified of failure
        logger.warn(Translate.get("backendworkerthread.task.failed", e));
      }
      catch (RuntimeException re)
      {
        // We can't know for sure if the task has notified the failure or not.
        // To prevent a deadlock, we force the failure notification here.
        try
        {
          currentlyProcessingTask.notifyFailure(this, 1, new SQLException(re
              .getMessage()));
        }
        catch (SQLException e1)
        {
          // just notify
        }
        logger.fatal(Translate.get(
            "backendworkerthread.task.runtime.exception",
            currentlyProcessingTask.toString()), re);
      }
      finally
      {
        try
        {
          backend.removePendingRequest(currentlyProcessingTask);
        }
        catch (RuntimeException e)
        {
          logger.warn(
              Translate.get("backendworkerthread.remove.task.error", e), e);
        }
      }

      // Notify the completion of the task if someone is waiting for
      // the completion of this transaction.
      // @see #waitForAllTasksToComplete()
      // @see #waitForAllTasksToComplete(long)
      synchronized (this)
      {
        notifyAll();
        currentlyProcessingTask = null;
        currentTaskTid = null;
      }
    } // end while (!isKilled)

    // Automatically disable the backend when the thread dies
    try
    {
      if (backend.isReadEnabled() || backend.isWriteEnabled())
        loadBalancer.disableBackend(backend);
    }
    catch (SQLException e)
    {
      logger.error(Translate.get("backendworkerthread.backend.disable.failed",
          new String[]{backend.getName(), e.getMessage()}));
    }
  }

  /*
   * Getter/Setter
   */

  /**
   * Returns the backend.
   * 
   * @return a <code>DatabaseBackend</code> instance
   */
  public DatabaseBackend getBackend()
  {
    return backend;
  }

  /**
   * Returns the logger for tracing.
   * 
   * @return a <code>Trace</code> instance
   */
  public Trace getLogger()
  {
    return logger;
  }

}