/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
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
 * Initial developer(s): Olivier Fambon.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.recoverylog.events;

import java.sql.SQLException;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.controller.recoverylog.LoggerThread;

/**
 * This class is used to Store a Dump Checkpoint in the recovery log checkpoint
 * tables so that it is available e.g. for restore operations.
 * 
 * @author <a href="mailto:olivier.fambon@emicnetworks.com">Olivier Fambon</a>
 * @version 1.0
 */
public class StoreDumpCheckpointEvent implements LogEvent
{
  private long   checkpointId;
  private String checkpointName;

  /**
   * Creates a new <code>StoreDumpCheckpointEvent</code> object
   * 
   * @param checkpointName the checkpoint name to create.
   * @param checkpointId the id of the checkpoint
   */
  public StoreDumpCheckpointEvent(String checkpointName, long checkpointId)
  {
    this.checkpointId = checkpointId;
    this.checkpointName = checkpointName;
  }

  /**
   * @see org.objectweb.cjdbc.controller.recoverylog.events.LogEvent#belongToTransaction(long)
   */
  public boolean belongToTransaction(long tid)
  {
    return false;
  }

  /**
   * @see org.objectweb.cjdbc.controller.recoverylog.events.LogEvent#execute(org.objectweb.cjdbc.controller.recoverylog.LoggerThread)
   */
  public void execute(LoggerThread loggerThread)
  {
    try
    {
      loggerThread.storeCheckpoint(checkpointName, checkpointId);
    }
    catch (SQLException e)
    {
      loggerThread.invalidateLogStatements();
      loggerThread.getLogger().error(
          Translate.get("recovery.jdbc.checkpoint.store.failed", new Object[]{
              checkpointName, e}), e);
      // Push object back in the queue, it needs to be logged again => potential
      // infinite loop
      loggerThread.putBackAtHeadOfQueue(this);
    }
  }
}
