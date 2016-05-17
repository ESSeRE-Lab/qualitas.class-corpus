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
import java.util.HashSet;

import org.objectweb.cjdbc.common.exceptions.RollbackException;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.requestmanager.RAIDbLevels;
import org.objectweb.cjdbc.controller.scheduler.AbstractScheduler;

/**
 * This scheduler provides optimistic query level scheduling for RAIDb-1
 * controllers. Reads can execute in parallel of any request. Writes are flagged
 * as blocking or not based on the completion of a previous write inside the
 * same transaction.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class RAIDb1OptimisticQueryLevelScheduler extends AbstractScheduler
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

  private long    requestId;
  private HashSet completedWrites = new HashSet(); // set of tids

  //
  // Constructor
  //

  /**
   * Creates a new Query Level Scheduler
   */
  public RAIDb1OptimisticQueryLevelScheduler()
  {
    super(RAIDbLevels.RAIDb1, ParsingGranularities.NO_PARSING);
    requestId = 0;
  }

  //
  // Request Handling
  //

  /**
   * Additionally to scheduling the request, this method replaces the SQL Date
   * macros such as now() with the current date.
   * 
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleReadRequest(SelectRequest)
   */
  public synchronized void scheduleReadRequest(SelectRequest request)
      throws SQLException
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
   * Additionally to scheduling the request, this method replaces the SQL Date
   * macros such as now() with the current date.
   * 
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleWriteRequest(AbstractWriteRequest)
   */
  public synchronized void scheduleNonSuspendedWriteRequest(
      AbstractWriteRequest request) throws SQLException
  {
    request.setId(requestId++);
    // if (request.isAutoCommit())
    // request.setBlocking(true);
    // else
    request.setBlocking(completedWrites.contains(new Long(request
        .getTransactionId())));
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#notifyWriteCompleted(AbstractWriteRequest)
   */
  public final synchronized void notifyWriteCompleted(
      AbstractWriteRequest request)
  {
    if (!request.isAutoCommit())
      completedWrites.add(new Long(request.getTransactionId()));
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#scheduleNonSuspendedStoredProcedure(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public final synchronized void scheduleNonSuspendedStoredProcedure(
      StoredProcedure proc) throws SQLException, RollbackException
  {
    proc.setId(requestId++);
    proc.setBlocking(completedWrites
        .contains(new Long(proc.getTransactionId())));
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#notifyStoredProcedureCompleted(org.objectweb.cjdbc.common.sql.StoredProcedure)
   */
  public final void notifyStoredProcedureCompleted(StoredProcedure proc)
  {
    if (!proc.isAutoCommit())
      completedWrites.add(new Long(proc.getTransactionId()));
  }

  //
  // Transaction Management
  //

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#commitTransaction(long)
   */
  protected final void commitTransaction(long transactionId)
  {
    completedWrites.remove(new Long(transactionId));
  }

  /**
   * @see org.objectweb.cjdbc.controller.scheduler.AbstractScheduler#rollbackTransaction(long)
   */
  protected final void rollbackTransaction(long transactionId)
  {
    completedWrites.remove(new Long(transactionId));
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
    return "<" + DatabasesXmlTags.ELT_RAIDb1Scheduler + " "
        + DatabasesXmlTags.ATT_level + "=\""
        + DatabasesXmlTags.VAL_optimisticQuery + "\"/>";
  }
}
