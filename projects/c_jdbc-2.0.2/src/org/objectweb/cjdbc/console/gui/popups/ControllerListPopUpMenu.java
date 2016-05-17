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

package org.objectweb.cjdbc.console.gui.popups;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;

/**
 * This class defines a ControllerListPopUpMenu that listens to mouse events
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ControllerListPopUpMenu extends AbstractPopUpMenu
{
  /**
   * Creates a new <code>ControllerListPopUpMenu.java</code> object
   * 
   * @param gui link to the main gui
   */
  public ControllerListPopUpMenu(CjdbcGui gui)
  {
    super(gui);
    this.gui = gui;
    this.add(new JMenuItem(GuiCommands.COMMAND_ADD_CONTROLLER))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_REFRESH_CONTROLLER_LIST))
        .addActionListener(this);
  }
  
  

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    String action = e.getActionCommand();
    if (action.equals(GuiCommands.COMMAND_ADD_CONTROLLER))
    {
      gui.publicActionAddControllerView();
    }
    else if (action.equals(GuiCommands.COMMAND_REFRESH_CONTROLLER_LIST))
    {
      gui.publicActionLoadControllerList();
    }
  }
}
