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
 * This message is used to set a cluster-wide atomic checkpoint into the
 * recovery log. This is used e.g prior to a TransfertBackend.
 * 
 * @author <a href="mailto:Olivier.Fambon@emicnetworks.com>Olivier Fambon </a>
 * @version 1.0
 */
public class SetCheckpoint implements Serializable
{
  private static final long serialVersionUID = -3457852098156752862L;

  private String            checkpointName;

  /**
   * Creates a new SetCheckpoint message.
   * 
   * @param checkpointName desired name of the checkpoint.
   */
  public SetCheckpoint(String checkpointName)
  {
    this.checkpointName = checkpointName;
  }

  /**
   * Returns the checkpointName value.
   * 
   * @return Returns the checkpointName.
   */
  public String getCheckpointName()
  {
    return checkpointName;
  }
}