/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks
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

import java.io.Serializable;
import java.sql.SQLException;

import org.objectweb.cjdbc.controller.requestmanager.distributed.DistributedRequestManager;

/**
 * This class defines a DistributedTransactionMarker which is used to transport
 * commit/rollback/savepoint type of commands.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public abstract class DistributedTransactionMarker implements Serializable
{

  protected long transactionId;

  /**
   * Creates a new <code>DistributedTransactionMarker</code> object
   * 
   * @param transactionId the transaction identifier
   */
  public DistributedTransactionMarker(long transactionId)
  {
    this.transactionId = transactionId;
  }

  /**
   * Schedule the command (i.e. commit or rollback). This method blocks until
   * the command is scheduled.
   * 
   * @param drm a distributed request manager
   * @throws SQLException if an error occurs.
   */
  public abstract void scheduleCommand(DistributedRequestManager drm)
      throws SQLException;

  /**
   * Code to be executed by the distributed request manager receiving the
   * command.
   * 
   * @param drm a distributed request manager
   * @return an Object to be sent back to the caller
   * @throws SQLException if an error occurs.
   */
  public abstract Object executeCommand(DistributedRequestManager drm)
      throws SQLException;

  /**
   * Returns the transactionId value.
   * 
   * @return Returns the transactionId.
   */
  public long getTransactionId()
  {
    return transactionId;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj == null)
      return false;
    if (obj.getClass().equals(this.getClass()))
      return transactionId == ((DistributedTransactionMarker) obj)
          .getTransactionId();
    else
      return false;
  }
}
