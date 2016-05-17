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

package org.objectweb.cjdbc.controller.scheduler.raidb1;

import java.sql.SQLException;
import java.util.LinkedList;

import org.objectweb.cjdbc.common.exceptions.RollbackException;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.requestmanager.RAIDbLevels;
import org.objectweb.cjdbc.controller.scheduler.AbstractScheduler;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.Commit;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.ReleaseSavepoint;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.Rollback;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.RollbackToSavepoint;
import org.objectweb.cjdbc.controller.virtualdatabase.protocol.SetSavepoint;

/**
 * This scheduler provides pass through scheduling for RAIDb-1 controllers.
 * Requests are only assigned a unique id and passed to the load balancer.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class RAIDb1PassThroughScheduler extends AbstractScheduler
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

  private long       requestId;
  private LinkedList totalOrderQueue = null;

  //
  // Constructor
  //

  /**
   * Creates a new Pass Through Scheduler that will set a new total order queue
   * on the virtual database.
   * 
   * @param vdb the virtual database we belong to
   */
  public RAIDb1PassThroughScheduler(VirtualDatabase vdb)
  {
    super(RAIDbLevels.RAIDb1, ParsingGranularities.NO_PARSING);
    requestId = 0;
    if (vdb.isDistributed())
    {
      totalOrderQueue = new LinkedList();
      vdb.setTotalOrderQueue(totalOrderQueue);
    }
  }

  //
  // Request Handling
  //

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleReadRequest(SelectRequest)
   */
  public final synchronized void scheduleReadRequest(SelectRequest request)
  {
    request.setId(requestId++);
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#readCompletedNotify(SelectRequest)
   */
  public final void readCompletedNotify(SelectRequest request)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleWriteRequest(AbstractWriteRequest)
   */
  public final synchronized void scheduleNonSuspendedWriteRequest(
      AbstractWriteRequest request)
  {
    request.setId(requestId++);
    if (totalOrderQueue != null)
    {
      synchronized (totalOrderQueue)
      {
        totalOrderQueue.addLast(request);
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#notifyWriteCompleted(AbstractWriteRequest)
   */
  public final void notifyWriteCompleted(AbstractWriteRequest request)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleNonSuspendedStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public final synchronized void scheduleNonSuspendedStoredProcedure(
      StoredProcedure proc) throws SQLException, RollbackException
  {
    proc.setId(requestId++);
    if (totalOrderQueue != null)
    {
      synchronized (totalOrderQueue)
      {
        totalOrderQueue.addLast(proc);
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#notifyStoredProcedureCompleted(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public final void notifyStoredProcedureCompleted(StoredProcedure proc)
  {
  }

  //
  // Transaction Management
  //

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#commitTransaction(long)
   */
  protected final void commitTransaction(long transactionId)
  {
    if (totalOrderQueue != null)
    {
      synchronized (totalOrderQueue)
      {
        totalOrderQueue.addLast(new Commit("", transactionId));
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#rollbackTransaction(long)
   */
  protected final void rollbackTransaction(long transactionId)
  {
    if (totalOrderQueue != null)
    {
      synchronized (totalOrderQueue)
      {
        totalOrderQueue.addLast(new Rollback("", transactionId));
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#rollbackTransaction(long,
   *      String)
   */
  protected final void rollbackTransaction(long transactionId,
      String savepointName)
  {
    if (totalOrderQueue != null)
    {
      synchronized (totalOrderQueue)
      {
        totalOrderQueue.addLast(new RollbackToSavepoint(transactionId,
            savepointName));
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#setSavepointTransaction(long,
   *      String)
   */
  protected final void setSavepointTransaction(long transactionId, String name)
  {
    if (totalOrderQueue != null)
    {
      synchronized (totalOrderQueue)
      {
        totalOrderQueue.addLast(new SetSavepoint(transactionId, name));
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#releaseSavepointTransaction(long,
   *      String)
   */
  protected final void releaseSavepointTransaction(long transactionId,
      String name)
  {
    if (totalOrderQueue != null)
    {
      synchronized (totalOrderQueue)
      {
        totalOrderQueue.addLast(new ReleaseSavepoint(transactionId, name));
      }
    }
  }

  //
  // Debug/Monitoring
  //
  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#getXmlImpl()
   */
  public String getXmlImpl()
  {
    return "<" + DatabasesXmlTags.ELT_RAIDb1Scheduler + " "
        + DatabasesXmlTags.ATT_level + "=\"" + DatabasesXmlTags.VAL_passThrough
        + "\"/>";
  }
}
