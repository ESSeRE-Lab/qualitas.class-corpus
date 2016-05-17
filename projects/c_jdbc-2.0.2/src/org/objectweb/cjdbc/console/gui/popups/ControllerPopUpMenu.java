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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.gui.popups;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.objects.ControllerObject;

/**
 * This class defines a ControllerPopUpMenu that listens to mouse events
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ControllerPopUpMenu extends AbstractPopUpMenu
{
  private ControllerObject controller;
  private String           controllerName;

  /**
   * Creates a new <code>ControllerPopUpMenu</code> object
   * 
   * @param gui link to the main gui
   * @param controller name of referenced controller
   */
  public ControllerPopUpMenu(CjdbcGui gui, ControllerObject controller)
  {
    super(gui);
    this.controller = controller;
    this.controllerName = controller.getName();
    this.add(new JMenuItem(GuiCommands.COMMAND_ADD_DRIVER)).addActionListener(
        this);
    this.add(new JMenuItem(GuiCommands.COMMAND_REFRESH_LOGS))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_DISPLAY_XML_CONTROLLER))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_GET_CONTROLLER_INFO))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_SHUTDOWN_CONTROLLER))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_CONTROLLER_REPORT))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_CONTROLLER_LOG_CONFIGURATION))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_CONTROLLER_REMOVE))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_CONTROLLER_MONITOR))
        .addActionListener(this);
  }

  /**
   * Returns the controller value.
   * 
   * @return Returns the controller.
   */
  public ControllerObject getController()
  {
    return controller;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    String action = e.getActionCommand();
    if (action.equals(GuiCommands.COMMAND_ADD_DRIVER))
    {
      gui.publicActionLoadDriver(controllerName);
    }
    else if (action.equals(GuiCommands.COMMAND_REFRESH_LOGS))
    {
      gui.publicActionRefreshLogs(controllerName);
    }
    else if (action.equals(GuiCommands.COMMAND_DISPLAY_XML_CONTROLLER))
    {
      gui.publicActionLoadXmlController(controllerName);
    }
    else if (action.equals(GuiCommands.COMMAND_GET_CONTROLLER_INFO))
    {
      gui.publicActionGetControllerInfo(controllerName);
    }
    else if (action.equals(GuiCommands.COMMAND_SHUTDOWN_CONTROLLER))
    {
      gui.publicActionShutdownController(controllerName);
    }
    else if (action.equals(GuiCommands.COMMAND_CONTROLLER_REPORT))
    {
      gui.publicActionControllerReport(controllerName);
    }
    else if (action.equals(GuiCommands.COMMAND_CONTROLLER_LOG_CONFIGURATION))
    {
      gui.publicActionControllerLogConfiguration(controllerName);
    }
    else if (action.equals(GuiCommands.COMMAND_CONTROLLER_REMOVE))
    {
      gui.publicActionControllerRemove(controllerName);
    }
    else if (action.equals(GuiCommands.COMMAND_CONTROLLER_MONITOR))
    {
      gui.publicActionStartMonitor(controllerName, true, false, false);
    }

  }
}