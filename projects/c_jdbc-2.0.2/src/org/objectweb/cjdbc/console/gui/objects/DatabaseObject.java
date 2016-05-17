/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.gui.objects;

import java.awt.Color;

import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.constants.GuiIcons;

/**
 * This class defines a DatabaseObject
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class DatabaseObject extends AbstractGuiObject
{
  private String  state;
  private String  controllerName;
  private boolean isDistributed;

  /**
   * Creates a new <code>ControllerObject</code> object
   * 
   * @param databaseName the name of the database
   * @param controllerName the name of the controller the database belongs to
   * @param isDistributed if the database is distributed set to
   *          <code>true</code>
   */
  public DatabaseObject(String databaseName, String controllerName,
      boolean isDistributed)
  {
    super();
    setText(databaseName);
    setName(databaseName);
    this.controllerName = controllerName;
    this.isDistributed = isDistributed;
    setBackground(Color.white);
    if (isDistributed)
      setIcon(GuiIcons.DATABASE_DISTRIBUTED_ICON);
    else
      setIcon(GuiIcons.DATABASE_SINGLE_ICON);
  }

  /**
   * Get ip address of this controller
   * 
   * @return ipAddress
   */
  public String getIpAdress()
  {
    return controllerName.substring(0, controllerName.indexOf(':'));
  }

  /**
   * Get port of this controller
   * 
   * @return port
   */
  public String getPort()
  {
    return controllerName.substring(controllerName.indexOf(':') + 1);
  }

  /**
   * Get the state of the controller
   * 
   * @return state of controller as defined in gui constants , null if unknown
   */
  public String getState()
  {
    return state;
  }

  /**
   * Set state of controller and change its icon
   * 
   * @param state string description of the state
   */
  public void setState(String state)
  {
    if (state.equals(GuiConstants.CONTROLLER_STATE_UP))
      setIcon(GuiIcons.CONTROLLER_READY);
    else
      setIcon(GuiIcons.CONTROLLER_DOWN);
    this.state = state;
  }

  /**
   * Returns the controllerName value.
   * 
   * @return Returns the controllerName.
   */
  public String getControllerName()
  {
    return controllerName;
  }

  /**
   * Returns the isDistributed value.
   * 
   * @return Returns the isDistributed.
   */
  public boolean isDistributed()
  {
    return isDistributed;
  }

  /**
   * Sets the isDistributed value.
   * 
   * @param isDistributed The isDistributed to set.
   */
  public void setDistributed(boolean isDistributed)
  {
    this.isDistributed = isDistributed;
  }
}