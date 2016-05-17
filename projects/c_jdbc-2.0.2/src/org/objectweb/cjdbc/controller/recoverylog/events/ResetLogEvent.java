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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.recoverylog.events;

import java.sql.SQLException;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.controller.recoverylog.LoggerThread;

/**
 * This class defines a ResetLogEvent to reset the log
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class ResetLogEvent implements LogEvent
{
  private long   oldId;
  private long   newId;
  private String checkpointName;

  /**
   * Creates a new <code>ResetLogEvent</code> object
   * 
   * @param oldCheckpointId the old id of the checkpoint
   * @param newCheckpointId the new checkpoint identifier
   * @param checkpointName the checkpoint name to delete from.
   */
  public ResetLogEvent(long oldCheckpointId, long newCheckpointId,
      String checkpointName)
  {
    this.oldId = oldCheckpointId;
    this.newId = newCheckpointId;
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
      loggerThread.deleteCheckpointTable();
      loggerThread.storeCheckpoint(checkpointName, newId);
      loggerThread.deleteLogEntriesBeforeId(oldId);
      loggerThread.shiftLogEntriesIds(newId - oldId);
    }
    catch (SQLException e)
    {
      loggerThread.invalidateLogStatements();
      loggerThread.getLogger().error(
          Translate.get("recovery.jdbc.loggerthread.log.reset.failed",
              checkpointName), e);
      // Push object back in the queue, it needs to be logged again
      loggerThread.putBackAtHeadOfQueue(this);
    }
  }

}
