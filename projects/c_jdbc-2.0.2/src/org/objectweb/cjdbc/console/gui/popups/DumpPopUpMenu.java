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
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.console.gui.popups;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.objects.DumpFileObject;

/**
 * This class defines a DumpPopUpMenu
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class DumpPopUpMenu extends AbstractPopUpMenu
{
  private String         databaseName;
  private DumpFileObject dump;

  /**
   * Creates a new <code>DumpPopUpMenu.java</code> object
   * 
   * @param gui the main interface
   * @param databaseName the virtual database name to look for dumps
   * @param dump the dumpObject associated to this menu
   */
  public DumpPopUpMenu(CjdbcGui gui, String databaseName, DumpFileObject dump)
  {
    super(gui);
    this.databaseName = databaseName;
    this.dump = dump;
    this.add(new JMenuItem(GuiCommands.COMMAND_DELETE_DUMP)).addActionListener(
        this);

  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    String action = e.getActionCommand();
    if (action.equals(GuiCommands.COMMAND_DELETE_DUMP))
    {
      gui.publicActionDeleteDump(databaseName, dump);
    }
  }
}