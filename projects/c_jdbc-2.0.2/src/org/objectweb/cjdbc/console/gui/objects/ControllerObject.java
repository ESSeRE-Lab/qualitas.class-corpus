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
 * This class defines a ControllerObject
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ControllerObject extends AbstractGuiObject
{
  private String state;

  /**
   * Creates a new <code>ControllerObject</code> object
   * 
   * @param name the name of the controller
   */
  public ControllerObject(String name)
  {
    super();
    setText(name);
    setName(name);
    setBackground(Color.white);
  }

  /**
   * Get ip address of this controller
   * 
   * @return ipAddress
   */
  public String getIpAdress()
  {
    return getName().substring(0, getName().indexOf(':'));
  }

  /**
   * Get port of this controller
   * 
   * @return port
   */
  public String getPort()
  {
    return getName().substring(getName().indexOf(':') + 1);
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
    {
      setIcon(GuiIcons.CONTROLLER_READY);
      setEnabled(true);
    }
    else
    {
      setIcon(GuiIcons.CONTROLLER_DOWN);
      //setEnabled(false);
    }
    this.state = state;
  }
}