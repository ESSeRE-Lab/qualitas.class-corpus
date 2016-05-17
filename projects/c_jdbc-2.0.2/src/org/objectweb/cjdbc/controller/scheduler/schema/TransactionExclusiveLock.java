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
 * Contributor(s): _________________________.
 */

package org.objectweb.cjdbc.controller.scheduler.schema;

import java.util.ArrayList;

import org.objectweb.cjdbc.common.sql.AbstractRequest;

/**
 * A <code>TransactionExclusiveLock</code> is an exclusive lock that let the
 * owner of the lock acquire several times the lock (but it needs to be released
 * only once). Acquire supports timeout and graceful withdrawal of timed out
 * requests.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class TransactionExclusiveLock
{
  private boolean   isLocked    = false;

  /** Transaction id of the lock holder. */
  private long      locker;

  /** <code>ArrayList</code> of <code>WaitingListElement</code>. */
  private ArrayList waitingList = new ArrayList();

  /**
   * The element stored in the waiting list is the waiting thread and the
   * transaction id of the request waiting.
   */
  private class WaitingListElement
  {
    /** Waiting thread */
    Thread thread;

    /** Transaction id of the request waiting. */
    long   transactionId;

    /**
     * Creates a new <code>WaitingListElement</code> instance.
     * 
     * @param thread the waiting thread.
     * @param transactionId the transaction id of the request waiting.
     */
    WaitingListElement(Thread thread, long transactionId)
    {
      this.thread = thread;
      this.transactionId = transactionId;
    }

    /**
     * Returns the transaction id of the request waiting.
     * 
     * @return an <code>int</code> value
     */
    public long getTransactionId()
    {
      return transactionId;
    }

    /**
     * Returns the waiting thread.
     * 
     * @return a <code>Thread</code> value
     */
    public Thread getThread()
    {
      return thread;
    }
  }

  /**
   * Acquires an exclusive lock on this table. If the lock is already held by
   * the same transaction as the given request, this method is non-blocking else
   * the caller is blocked until the transaction holding the lock releases it at
   * commit/rollback time.
   * 
   * @param request request asking for the lock (timeout field is used and
   *          updated upon waiting)
   * @return boolean true is the lock has been successfully acquired, false on
   *         timeout or error
   * @see #release()
   */
  public boolean acquire(AbstractRequest request)
  {
    long tid = request.getTransactionId();

    synchronized (Thread.currentThread())
    {
      WaitingListElement wle = null;
      synchronized (this)
      {
        if (!isLocked)
        { // Lock is free, take it
          locker = tid;
          isLocked = true;
          return true;
        }
        else
        {
          if ((!request.isAutoCommit()) && (locker == tid))
            return true; // We already have the lock
          else
          { // Wait for the lock
            wle = new WaitingListElement(Thread.currentThread(), tid);
            waitingList.add(wle);
          }
        }
      }
      // At this point, we have to wait for the lock.
      try
      {
        int timeout = request.getTimeout();
        if (timeout == 0)
        {
          Thread.currentThread().wait(); // No timeout
          // Note: isLocked and locker are already set.
          return true;
        }
        else
        { // Wait with timeout
          long start = System.currentTimeMillis();
          // Convert seconds to milliseconds for wait call
          long lTimeout = timeout * 1000;
          Thread.currentThread().wait(lTimeout);
          long end = System.currentTimeMillis();
          int remaining = (int) (lTimeout - (end - start));
          if (remaining > 0)
          { // Ok
            request.setTimeout(remaining);
            // Note: isLocked and locker are already set.
            return true;
          }
          else
          { // Too late, remove ourselves from the waiting list
            synchronized (this)
            {
              int idx = waitingList.indexOf(wle);
              if (idx == -1)
                // We got the lock before being able to acquire the lock on
                // this. Give the lock to the next one.
                release();
              else
                waitingList.remove(idx);
            }
            return false;
          }
        }
      }
      catch (InterruptedException ie)
      {
        synchronized (this)
        { // Something wrong happened, remove ourselves from the waiting list
          waitingList.remove(Thread.currentThread());
        }
        return false;
      }
    }
  }

  /**
   * Releases the lock on this table.
   * 
   * @see #acquire(AbstractRequest)
   */
  public synchronized void release()
  {
    while (!waitingList.isEmpty())
    {
      // Wake up the first waiting thread and update locker transaction id
      WaitingListElement e = (WaitingListElement) waitingList.remove(0);
      Thread thread = e.getThread();
      locker = e.getTransactionId();
      synchronized (thread)
      {
        thread.notify();
        // isLocked remains true
        return;
      }
    }
    isLocked = false;
  }

  /**
   * Returns <code>true</code> if the lock is owned by someone.
   * 
   * @return <code>boolean</code> value
   */
  public boolean isLocked()
  {
    return isLocked;
  }

  /**
   * Returns the transaction id of the lock owner. The return value is undefined
   * if the lock is not owned (usually it is the last owner).
   * 
   * @return int the transaction id.
   */
  public long getLocker()
  {
    return locker;
  }

  /**
   * Returns the waitingList.
   * 
   * @return an <code>ArrayList</code> of <code>WaitingListElement</code>
   */
  public ArrayList getWaitingList()
  {
    return waitingList;
  }

  /**
   * Returns <code>true</code> if the given transaction id is contained in
   * this lock waiting queue.
   * 
   * @param transactionId a transaction id
   * @return a <code>boolean</code> value
   */
  public synchronized boolean isWaiting(long transactionId)
  {
    WaitingListElement e;
    int size = waitingList.size();
    for (int i = 0; i < size; i++)
    {
      e = (WaitingListElement) waitingList.get(i);
      if (e.getTransactionId() == transactionId)
        return true;
    }
    return false;
  }
}