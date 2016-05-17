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

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager;

/**
 * Execute a read request.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ExecReadRequest extends DistributedRequest
{
  private static final long serialVersionUID = -7183844510032678987L;

  /**
   * Creates a new <code>ExecReadRequest</code> object.
   * 
   * @param request select request to execute
   */
  public ExecReadRequest(SelectRequest request)
  {
    super(request);
  }

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedRequest#scheduleRequest(org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager)
   */
  public void scheduleRequest(DistributedRequestManager drm)
      throws SQLException
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.virtualdatabase.protocol.DistributedRequest#executeScheduledRequest(org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager)
   */
  public Object executeScheduledRequest(DistributedRequestManager drm)
      throws SQLException
  {
    // Check if the transaction has been started
    if (!request.isAutoCommit())
    {
      long tid = request.getTransactionId();
      try
      {
        drm.getTransactionMarker(new Long(tid));
      }
      catch (SQLException e)
      { // Transaction not started. If we start a new transaction now, it will
        // never be commited if this is a read-only transaction since the commit
        // will never get distributed and reach us (read-only transactions are
        // only commited locally). Therefore, we decide to execute this read in
        // autoCommit mode which should not be a real big issue (TODO: check
        // impact on transaction isolation).
        // Note that further write queries on that transaction will really start
        // a transaction and subsequent reads would then execute in the proper
        // transaction.
        request.setIsAutoCommit(true);
      }
    }

    try
    {
      return drm.execLocalReadRequest((SelectRequest) request);
    }
    catch (SQLException e)
    {
      drm.getLogger().warn(
          Translate.get("virtualdatabase.distributed.read.sqlexception", e
              .getMessage()), e);
      throw e;
    }
    catch (RuntimeException re)
    {
      drm.getLogger().warn(
          Translate.get("virtualdatabase.distributed.read.exception", re
              .getMessage()), re);
      throw new SQLException(re.getMessage());
    }
  }

}