/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.virtualdatabase.protocol;

import java.io.Serializable;

import org.objectweb.cjdbc.common.shared.BackendInfo;

/**
 * This class defines a BackendTransfer message
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BackendTransfer implements Serializable
{
  private static final long serialVersionUID = 520265407391630486L;

  BackendInfo               info;
  String                    controllerDest;
  String                    checkpointName;

  /**
   * Creates a new <code>BackendTransfer</code> object
   * 
   * @param info the info on the backend to transfer
   * @param controllerDest the JMX name of the target controller
   * @param checkpointName the name of the ckeckpoint from which to restore
   */
  public BackendTransfer(String controllerDest, String checkpointName,
      BackendInfo info)
  {
    this.info = info;
    this.controllerDest = controllerDest;
    this.checkpointName = checkpointName;
  }

  /**
   * Returns the controllerDest value.
   * 
   * @return Returns the controllerDest.
   */
  public String getControllerDest()
  {
    return controllerDest;
  }

  /**
   * Returns the info value.
   * 
   * @return Returns the info.
   */
  public BackendInfo getInfo()
  {
    return info;
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