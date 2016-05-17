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

import org.objectweb.cjdbc.controller.recoverylog.LoggerThread;

/**
 * This interface defines a LogEvent that defines the profile of actions that
 * manipulate the recovery log like logging a request.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet</a>
 * @version 1.0
 */
public interface LogEvent
{
  /**
   * Returns true if this logEvent belongs to the given transaction.
   * 
   * @param tid the transaction identifier
   * @return true if this logEvent belongs to this transaction
   */
  boolean belongToTransaction(long tid);

  /**
   * Called by the LoggerThread to perform the needed operation on the log for
   * this entry.
   * 
   * @param loggerThread the logger thread calling this method
   */
  void execute(LoggerThread loggerThread);

}
