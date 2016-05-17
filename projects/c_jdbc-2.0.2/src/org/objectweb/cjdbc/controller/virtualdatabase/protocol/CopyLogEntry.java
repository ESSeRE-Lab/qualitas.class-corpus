/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 EmicNetworks.
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

package org.objectweb.cjdbc.controller.virtualdatabase.protocol;

import java.io.Serializable;

import org.objectweb.cjdbc.controller.recoverylog.events.LogEntry;

/**
 * This class defines a CopyLogEntry message. It is used to send recovery log
 * entries over to a remote peer. Entries are sent one by one instead of as big
 * bunch because each log entry can potentially be a huge object, e.g. if it
 * contains a blob, and it should fit in memory.
 * 
 * @author <a href="mailto:olivier.fambon@emicnetworks.com">Olivier Fambon </a>
 * @version 1.0
 */
public class CopyLogEntry implements Serializable
{
  private static final long serialVersionUID = 1L;

  private LogEntry          entry;

  /**
   * Creates a new <code>CopyLogEntry</code> object
   * 
   * @param entry the entry to be sent over to the remote peer.
   */
  public CopyLogEntry(LogEntry entry)
  {
    this.entry = entry;
  }

  /**
   * Returns the recovery LogEntry to be copied.
   * 
   * @return the entry
   */
  public LogEntry getEntry()
  {
    return entry;
  }
}
