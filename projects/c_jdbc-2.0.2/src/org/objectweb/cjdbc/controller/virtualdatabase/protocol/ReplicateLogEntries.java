/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
 * Contact: c-jdbc@objectweb.org
 * 
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

/**
 * This message is used both to prepare the sending of a set of log entries to a
 * remote controller's vdb recovery log (intialization) and to terminate it
 * (termination). Initialization is the second step of the process to rebuild
 * the remote recovery log from a live one, termination is the final step of the
 * process. The process is as such so that eventually the remote vdb backends
 * can be restored from dumps. Upon reception of this message for the
 * initialisation phase (identified by a null dumpCheckpointName), the remote
 * recovery log is cleared from the beginning upto the 'now' checkpoint.
 * Subsequently, CopyLogEntry messages are sent and log entries are inserted 'as
 * are' into the remote recovery log. Then, upon reception of this message for
 * the termination phase (non-null dumpCheckpointName), the remote recovery log
 * makes the dumpCheckpointName available for restore operations.
 * 
 * @see CopyLogEntry
 * @author <a href="mailto:Olivier.Fambon@emicnetworks.com>Olivier Fambon </a>
 * @version 1.0
 */
public class ReplicateLogEntries implements Serializable
{
  private static final long serialVersionUID = -2813776509468770586L;

  private String            checkpointName;
  private String            dumpCheckpointName;
  private long              checkpointId;

  /**
   * Creates a new <code>ReplicateLogEntries</code> message
   * 
   * @param checkpointName The checkpoint (aka now checkpoint) before wich
   *          entries are to be replaced.
   * @param checkpointId The id associated to checkpoint THIS ID SHOULD BE
   *          HIDDEN
   * @param dumpCheckpointName The dump checkoint from wich entries are to be
   *          replaced.
   */
  public ReplicateLogEntries(String checkpointName, String dumpCheckpointName,
      long checkpointId)
  {
    this.dumpCheckpointName = dumpCheckpointName;
    this.checkpointName = checkpointName;
    this.checkpointId = checkpointId;
  }

  /**
   * Returns the checkpointName value.
   * 
   * @return Returns the 'now' checkpointName.
   */
  public String getCheckpointName()
  {
    return checkpointName;
  }

  /**
   * Returns the dump checkpoint name .
   * 
   * @return Returns the dump CheckpointName.
   */
  public String getDumpCheckpointName()
  {
    return dumpCheckpointName;
  }

  /**
   * Returns the Checkpoint id.
   * 
   * @return the Checkpoint id
   */
  public long getCheckpointId()
  {
    return checkpointId;
  }
}