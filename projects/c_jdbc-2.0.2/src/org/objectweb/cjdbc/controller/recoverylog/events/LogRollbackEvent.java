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
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.recoverylog.LoggerThread;

/**
 * This class defines a LogRollbackEvent to log a rollback or potentially clean
 * the recovery log for that transaction that rollbacks.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class LogRollbackEvent extends LogRequestEvent
{
  /**
   * Creates a new <code>LogRollbackEvent</code> object
   * 
   * @param entry a log entry that describes a rollback (the SQL query is
   *          ignored)
   */
  public LogRollbackEvent(LogEntry entry)
  {
    super(entry);
  }

  /**
   * @see org.objectweb.cjdbc.controller.recoverylog.events.LogEvent#execute(org.objectweb.cjdbc.controller.recoverylog.LoggerThread)
   */
  public void execute(LoggerThread loggerThread)
  {
    Trace logger = loggerThread.getLogger();

    // Fist let's try to remove the transaction from the log if no-one is
    // currently processing the log
    try
    {
      if (!loggerThread.getRecoveryLog().isRecovering())
      {
        loggerThread.removeRollbackedTransaction(logEntry.getId());
        return;
      }
    }
    catch (SQLException e)
    {
      loggerThread.invalidateLogStatements();
      logger.error(Translate.get("recovery.jdbc.loggerthread.log.failed",
          new String[]{"rollback", String.valueOf(logEntry.getTid())}), e);
      // Push object back in the queue, it needs to be logged again
      loggerThread.putBackAtHeadOfQueue(this);
    }

    // Ok, someone has a lock on the log, just log the rollback.
    super.execute(loggerThread);
  }

}
