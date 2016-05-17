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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.loadbalancer.raidb1;

import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.loadbalancer.BackendWorkerThread;
import org.objectweb.cjdbc.controller.loadbalancer.policies.WaitForCompletionPolicy;
import org.objectweb.cjdbc.controller.loadbalancer.policies.errorchecking.ErrorCheckingPolicy;
import org.objectweb.cjdbc.controller.loadbalancer.tasks.KillThreadTask;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * RAIDb-1 load balancer.
 * <p>
 * This class is an abstract call because the read requests coming from the
 * request manager are NOT treated here but in the subclasses. Transaction
 * management and write requests are broadcasted to all backends.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public abstract class RAIDb1ec extends RAIDb1
{
  /*
   * How the code is organized ? 1. Member variables 2. Constructor(s) 3.
   * Request handling 4. Transaction handling 5. Backend management
   */

  protected ArrayList           backendReadThreads;
  protected int                 nbOfConcurrentReads;
  protected ErrorCheckingPolicy errorCheckingPolicy;

  protected static Trace        logger = Trace
                                           .getLogger("org.objectweb.cjdbc.controller.loadbalancer.RAIDb1ec");

  /*
   * Constructors
   */

  /**
   * Creates a new RAIDb-1 Round Robin request load balancer. A new backend
   * worker thread is created for each backend.
   * 
   * @param vdb the virtual database this load balancer belongs to
   * @param waitForCompletionPolicy how many backends must complete before
   *                    returning the result?
   * @param errorCheckingPolicy policy to apply for error checking.
   * @param nbOfConcurrentReads number of concurrent reads allowed
   * @exception Exception if an error occurs
   */
  public RAIDb1ec(VirtualDatabase vdb,
      WaitForCompletionPolicy waitForCompletionPolicy,
      ErrorCheckingPolicy errorCheckingPolicy, int nbOfConcurrentReads)
      throws Exception
  {
    super(vdb, waitForCompletionPolicy);
    backendReadThreads = new ArrayList();
    this.errorCheckingPolicy = errorCheckingPolicy;
    this.nbOfConcurrentReads = nbOfConcurrentReads;
  }

  /*
   * Backends management
   */

  /**
   * Enables a backend that was previously disabled.
   * <p>
   * Ask the corresponding connection manager to initialize the connections if
   * needed.
   * <p>
   * No sanity checks are performed by this function.
   * 
   * @param db the database backend to enable
   * @param writeEnabled True if the backend must be enabled for writes
   * @throws SQLException if an error occurs
   */
  public void enableBackend(DatabaseBackend db, boolean writeEnabled)
      throws SQLException
  {
    // Create 2 worker threads for writes
    BackendWorkerThread blockingThread = new BackendWorkerThread(db, this);
    BackendWorkerThread nonBlockingThread = new BackendWorkerThread(db, this);

    // Add first to the blocking thread list
    try
    {
      backendBlockingThreadsRWLock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }
    backendBlockingThreads.add(blockingThread);
    backendBlockingThreadsRWLock.releaseWrite();
    blockingThread.start();
    logger.info(Translate.get("loadbalancer.backend.workerthread.blocking.add",
        db.getName()));

    // Then add to the non-blocking thread list
    try
    {
      backendNonBlockingThreadsRWLock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      String msg = Translate.get(
          "loadbalancer.backendlist.acquire.writelock.failed", e);
      logger.error(msg);
      throw new SQLException(msg);
    }
    backendNonBlockingThreads.add(nonBlockingThread);
    backendNonBlockingThreadsRWLock.releaseWrite();
    nonBlockingThread.start();
    logger.info(Translate.get(
        "loadbalancer.backend.workerthread.non.blocking.add", db.getName()));

    if (!db.isInitialized())
      db.initializeConnections();
    db.enableRead();
    if (writeEnabled)
      db.enableWrite();
  }

  /**
   * Disables a backend that was previously enabled.
   * <p>
   * Ask the corresponding connection manager to finalize the connections if
   * needed.
   * <p>
   * No sanity checks are performed by this function.
   * 
   * @param db the database backend to disable
   * @throws SQLException if an error occurs
   */
  public synchronized void disableBackend(DatabaseBackend db)
      throws SQLException
  {
    int nbOfThreads = backendBlockingThreads.size();

    // Find the right blocking thread
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendBlockingThreads
          .get(i);
      if (thread.getBackend().equals(db))
      {
        logger.info(Translate.get(
            "loadbalancer.backend.workerthread.blocking.remove", db.getName()));

        // Remove it from the backendBlockingThread list
        try
        {
          backendBlockingThreadsRWLock.acquireWrite();
        }
        catch (InterruptedException e)
        {
          String msg = Translate.get(
              "loadbalancer.backendlist.acquire.writelock.failed", e);
          logger.error(msg);
          throw new SQLException(msg);
        }
        backendBlockingThreads.remove(thread);
        backendBlockingThreadsRWLock.releaseWrite();

        synchronized (thread)
        {
          // Kill the thread
          thread.addPriorityTask(new KillThreadTask(1, 1));
          thread.notify();
        }
        break;
      }
    }

    // Find the right non-blocking thread
    nbOfThreads = backendNonBlockingThreads.size();
    for (int i = 0; i < nbOfThreads; i++)
    {
      BackendWorkerThread thread = (BackendWorkerThread) backendNonBlockingThreads
          .get(i);
      if (thread.getBackend().equals(db))
      {
        logger.info(Translate.get(
            "loadbalancer.backend.workerthread.non.blocking.remove", db
                .getName()));

        // Remove it from the backendNonBlockingThreads list
        try
        {
          backendNonBlockingThreadsRWLock.acquireWrite();
        }
        catch (InterruptedException e)
        {
          String msg = Translate.get(
              "loadbalancer.backendlist.acquire.writelock.failed", e);
          logger.error(msg);
          throw new SQLException(msg);
        }
        backendNonBlockingThreads.remove(thread);
        backendNonBlockingThreadsRWLock.releaseWrite();

        synchronized (thread)
        {
          // Kill the thread
          thread.addPriorityTask(new KillThreadTask(1, 1));
          thread.notify();
        }
        break;
      }
    }

    db.disable();
    if (db.isInitialized())
      db.finalizeConnections();
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#getXmlImpl
   */
  public String getXmlImpl()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_RAIDb_1ec + " "
        + DatabasesXmlTags.ATT_nbOfConcurrentReads + "=\""
        + this.nbOfConcurrentReads + "\">");
    this.getRaidb1Xml();
    if (waitForCompletionPolicy != null)
      info.append(waitForCompletionPolicy.getXml());
    info.append("</" + DatabasesXmlTags.ELT_RAIDb_1ec + ">");
    return info.toString();
  }
}