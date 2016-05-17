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
 * Contributor(s): Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.controller.scheduler.raidb2;

import java.sql.SQLException;

import org.objectweb.cjdbc.common.exceptions.RollbackException;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.requestmanager.RAIDbLevels;
import org.objectweb.cjdbc.controller.scheduler.AbstractScheduler;

/**
 * This scheduler provides query level scheduling for RAIDb-2 controllers. Reads
 * can execute in parallel until a write comes in. Then the write waits for the
 * completion of the reads. Any new read is stacked after the write and they are
 * released together when the write has completed its execution.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class RAIDb2QueryLevelScheduler extends AbstractScheduler
{

  //
  // How the code is organized ?
  //
  // 1. Member variables
  // 2. Constructor
  // 3. Request handling
  // 4. Transaction management
  // 5. Debug/Monitoring
  //

  private long   requestId;
  private int    pendingReads;

  // We have to distinguish read and write to wake up only
  // waiting reads or writes according to the situation
  private Object readSync;    // to synchronize on reads completion
  private Object writeSync;   // to synchronize on writes completion

  //
  // Constructor
  //

  /**
   * Creates a new Query Level Scheduler
   */
  public RAIDb2QueryLevelScheduler()
  {
    super(RAIDbLevels.RAIDb2, ParsingGranularities.NO_PARSING);
    requestId = 0;
    pendingReads = 0;
    readSync = new Object();
    writeSync = new Object();
  }

  //
  // Request Handling
  //

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleReadRequest(SelectRequest)
   */
  public void scheduleReadRequest(SelectRequest request) throws SQLException
  {
    // Now deal with synchronization
    synchronized (this.writeSync)
    {
      if (getPendingWrites() == 0)
      { // No writes pending, go ahead !
        synchronized (this.readSync)
        {
          request.setId(requestId++);
          pendingReads++;
          if (logger.isDebugEnabled())
            logger.debug("Request " + request.getId() + " scheduled for read ("
                + pendingReads + " pending reads)");
          return;
        }
      }

      // Wait for the writes completion
      try
      {
        if (logger.isDebugEnabled())
          logger.debug("Request " + request.getId() + " waiting for "
              + getPendingWrites() + " pending writes)");

        int timeout = request.getTimeout();
        if (timeout > 0)
        {
          long start = System.currentTimeMillis();
          // Convert seconds to milliseconds for wait call
          long lTimeout = timeout * 1000;
          this.writeSync.wait(lTimeout);
          long end = System.currentTimeMillis();
          int remaining = (int) (lTimeout - (end - start));
          if (remaining > 0)
            request.setTimeout(remaining);
          else
          {
            String msg = "Timeout (" + request.getTimeout() + ") for request: "
                + request.getId();
            logger.warn(msg);
            throw new SQLException(msg);
          }
        }
        else
          this.writeSync.wait();

        synchronized (this.readSync)
        {
          request.setId(requestId++);
          pendingReads++;
          if (logger.isDebugEnabled())
            logger.debug("Request " + request.getId() + " scheduled for read ("
                + pendingReads + " pending reads)");
          return; // Ok, write completed before timeout
        }
      }
      catch (InterruptedException e)
      {
        // Timeout
        if (logger.isWarnEnabled())
          logger.warn("Request " + request.getId() + " timed out ("
              + request.getTimeout() + " s)");
        throw new SQLException("Timeout (" + request.getTimeout()
            + ") for request: " + request.getId());
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#readCompletedNotify(SelectRequest)
   */
  public final void readCompletedNotify(SelectRequest request)
  {
    synchronized (this.readSync)
    {
      pendingReads--;
      if (logger.isDebugEnabled())
        logger.debug("Request " + request.getId() + " completed");
      if (pendingReads == 0)
      {
        if (logger.isDebugEnabled())
          logger.debug("Last read completed, notifying writes");
        readSync.notifyAll(); // Wakes up any waiting write query
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleWriteRequest(AbstractWriteRequest)
   */
  public void scheduleNonSuspendedWriteRequest(AbstractWriteRequest request)
      throws SQLException
  {
    // We have to take the locks in the same order as reads else
    // we could have a deadlock
    synchronized (this.writeSync)
    {
      synchronized (this.readSync)
      {
        if (pendingReads == 0)
        { // No read pending, go ahead
          request.setId(requestId++);
          if (logger.isDebugEnabled())
            logger.debug("Request " + request.getId()
                + " scheduled for write (" + getPendingWrites()
                + " pending writes)");
          return;
        }
      }
    }

    waitForReadCompletion(request);
    scheduleNonSuspendedWriteRequest(request);
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#notifyWriteCompleted(AbstractWriteRequest)
   */
  public final synchronized void notifyWriteCompleted(
      AbstractWriteRequest request)
  {
    synchronized (this.writeSync)
    {
      if (logger.isDebugEnabled())
        logger.debug("Request " + request.getId() + " completed");
      if (getPendingWrites() == 0)
      {
        if (logger.isDebugEnabled())
          logger.debug("Last write completed, notifying reads");
        writeSync.notifyAll(); // Wakes up all waiting read queries
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleNonSuspendedStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public final synchronized void scheduleNonSuspendedStoredProcedure(
      StoredProcedure proc) throws SQLException, RollbackException
  {
    // We have to take the locks in the same order as reads else
    // we could have a deadlock
    synchronized (this.writeSync)
    {
      synchronized (this.readSync)
      {
        if (pendingReads == 0)
        { // No read pending, go ahead
          proc.setId(requestId++);
          if (logger.isDebugEnabled())
            logger.debug("Stored procedure "
                + proc.getId()
                + (proc.isAutoCommit() ? "" : " transaction "
                    + proc.getTransactionId()) + " scheduled for write ("
                + getPendingWrites() + " pending writes)");
          return;
        }
      }
    }

    waitForReadCompletion(proc);
    scheduleNonSuspendedStoredProcedure(proc);
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#notifyStoredProcedureCompleted(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public final void notifyStoredProcedureCompleted(StoredProcedure proc)
  {
    synchronized (this.writeSync)
    {
      if (logger.isDebugEnabled())
        logger.debug("Stored procedure " + proc.getId() + " completed - "
            + getPendingWrites() + " pending writes");
      if (getPendingWrites() == 0)
      {
        if (logger.isDebugEnabled())
          logger.debug("Last write completed, notifying reads");
        writeSync.notifyAll(); // Wakes up all waiting read queries
      }
    }
  }

  /**
   * Wait for the reads completion. Synchronizes on this.readSync.
   * 
   * @param request the request that is being scheduled
   * @throws SQLException if an error occurs
   */
  private void waitForReadCompletion(AbstractRequest request)
      throws SQLException
  {
    synchronized (this.readSync)
    {
      // Wait for the reads completion
      try
      {
        if (logger.isDebugEnabled())
          logger.debug("Request " + request.getId() + " waiting for "
              + pendingReads + " pending reads)");

        int timeout = request.getTimeout();
        if (timeout > 0)
        {
          long start = System.currentTimeMillis();
          // Convert seconds to milliseconds for wait call
          long lTimeout = timeout * 1000;
          this.readSync.wait(lTimeout);
          long end = System.currentTimeMillis();
          int remaining = (int) (lTimeout - (end - start));
          if (remaining > 0)
            request.setTimeout(remaining);
          else
          {
            String msg = "Timeout (" + request.getTimeout() + ") for request: "
                + request.getId();
            logger.warn(msg);
            throw new SQLException(msg);
          }
        }
        else
          this.readSync.wait();
      }
      catch (InterruptedException e)
      {
        // Timeout
        if (logger.isWarnEnabled())
          logger.warn("Request " + request.getId() + " timed out ("
              + request.getTimeout() + " ms)");
        throw new SQLException("Timeout (" + request.getTimeout()
            + ") for request: " + request.getId());
      }
    }
  }

  //
  // Transaction Management
  //

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#commitTransaction(long)
   */
  protected final void commitTransaction(long transactionId)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#rollbackTransaction(long)
   */
  protected final void rollbackTransaction(long transactionId)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#rollbackTransaction(long,
   *      String)
   */
  protected final void rollbackTransaction(long transactionId,
      String savepointName)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#setSavepointTransaction(long,
   *      String)
   */
  protected final void setSavepointTransaction(long transactionId, String name)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#releaseSavepointTransaction(long,
   *      String)
   */
  protected final void releaseSavepointTransaction(long transactionId,
      String name)
  {
  }

  //
  // Debug/Monitoring
  //
  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#getXmlImpl()
   */
  public String getXmlImpl()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_RAIDb2Scheduler + " "
        + DatabasesXmlTags.ATT_level + "=\"" + DatabasesXmlTags.VAL_query
        + "\"/>");
    info.append(System.getProperty("line.separator"));
    return info.toString();
  }
}
