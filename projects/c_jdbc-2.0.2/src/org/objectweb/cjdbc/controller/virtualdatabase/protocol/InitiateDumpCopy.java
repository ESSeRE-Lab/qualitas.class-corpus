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

import org.objectweb.cjdbc.common.shared.DumpInfo;
import org.objectweb.cjdbc.controller.backup.DumpTransferInfo;

/**
 * This message is used to prepare the sending of a dump to a remote
 * controller's vdb backup manager. This is used as an integrated remote-copy
 * facility in the occurence of restore, e.g. after a rebuild of the remote
 * recovery log from a live one. Upon reception of this message, the remote
 * backup manager initiates a transfer onto the sending controller's backuper.
 * 
 * @author <a href="mailto:Olivier.Fambon@emicnetworks.com>Olivier Fambon </a>
 * @version 1.0
 */
public class InitiateDumpCopy implements Serializable
{
  private static final long serialVersionUID = 4674422809133556752L;

  private DumpInfo          dumpInfo;
  private DumpTransferInfo  dumpTransferInfo;

  // private int timeout;

  /**
   * Creates a new <code>ReplicateLogEntries</code> message
   * 
   * @param dumpInfo The DumpInfo object returned by the Backuper.
   * @param dumpTransferInfo The dump transfer information
   */
  public InitiateDumpCopy(DumpInfo dumpInfo, DumpTransferInfo dumpTransferInfo)
  {
    this.dumpInfo = dumpInfo;
    this.dumpTransferInfo = dumpTransferInfo;
  }

  /**
   * Returns the dump info (name, checkpoint, etc).
   * 
   * @return Returns the dump info (on the sending side).
   */
  public DumpInfo getDumpInfo()
  {
    return dumpInfo;
  }

  /**
   * Returns the dump checkpoint name (global).
   * 
   * @return Returns the dump CheckpointName.
   */
  public String getDumpCheckpointName()
  {
    return dumpInfo.getCheckpointName();
  }

  /**
   * Return the dump name (sending side).
   * 
   * @return the dump name (sending side).
   */
  public String getDumpName()
  {
    return dumpInfo.getDumpName();
  }

  /**
   * Returns the session key to be used to authenticate the destination on the
   * sender.
   * 
   * @return the session key
   */
  public DumpTransferInfo getDumpTransferInfo()
  {
    return dumpTransferInfo;
  }

}