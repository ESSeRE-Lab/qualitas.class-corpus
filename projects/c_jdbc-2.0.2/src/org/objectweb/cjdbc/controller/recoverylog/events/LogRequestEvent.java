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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.recoverylog.LoggerThread;

/**
 * This class defines a LogRequestEvent to log a request log entry.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class LogRequestEvent implements LogEvent
{
  protected LogEntry logEntry;

  /**
   * Creates a new <code>LogRequestEvent</code> object
   * 
   * @param entry the log entry to use (not null)
   */
  public LogRequestEvent(LogEntry entry)
  {
    if (entry == null)
      throw new RuntimeException(
          "Invalid null entry in LogRequestEvent constructor");
    this.logEntry = entry;
  }

  /**
   * @see org.objectweb.cjdbc.controller.recoverylog.events.LogEvent#belongToTransaction(long)
   */
  public boolean belongToTransaction(long tid)
  {
    return logEntry.getTid() == tid;
  }

  /**
   * @see org.objectweb.cjdbc.controller.recoverylog.events.LogEvent#execute(org.objectweb.cjdbc.controller.recoverylog.LoggerThread)
   */
  public void execute(LoggerThread loggerThread)
  {
    Trace logger = loggerThread.getLogger();
    try
    {
      if (logger.isInfoEnabled())
        logger.info(Translate.get("recovery.jdbc.loggerthread.log.info",
            logEntry.getId()));

      PreparedStatement pstmt = loggerThread.getLogPreparedStatement();
      pstmt.setLong(1, logEntry.getId());
      pstmt.setString(2, logEntry.getLogin());
      pstmt.setString(3, logEntry.getQuery());
      pstmt.setLong(4, logEntry.getTid());
      if (logger.isDebugEnabled())
        logger.debug(pstmt.toString());
      try
      {
        pstmt.setEscapeProcessing(logEntry.getEscapeProcessing());
      }
      catch (Exception ignore)
      {
      }
      int updatedRows = pstmt.executeUpdate();
      if ((updatedRows != 1) && logger.isWarnEnabled())
        logger
            .warn("Recovery log did not update a single entry while executing: "
                + pstmt.toString());
    }
    catch (SQLException e)
    {
      loggerThread.invalidateLogStatements();
      logger.error(
          Translate.get("recovery.jdbc.loggerthread.log.failed", new String[]{
              logEntry.getQuery(), String.valueOf(logEntry.getTid())}), e);
      // Push object back in the queue, it needs to be logged again
      loggerThread.putBackAtHeadOfQueue(this);
    }
  }

}
