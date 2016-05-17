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
import org.objectweb.cjdbc.console.gui.objects.DatabaseObject;

/**
 * This class defines a DatabasePopUpMenu
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class DatabasePopUpMenu extends AbstractPopUpMenu
{
  private DatabaseObject database;
  private String         databaseName;

  /**
   * Creates a new <code>DatabasePopUpMenu.java</code> object
   * 
   * @param gui link to the main gui
   * @param database referenced db
   */
  public DatabasePopUpMenu(CjdbcGui gui, DatabaseObject database)
  {
    super(gui);
    this.database = database;
    this.databaseName = database.getName();
    this.add(new JMenuItem(GuiCommands.COMMAND_DISPLAY_XML_DATABASE))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_ENABLE_ALL)).addActionListener(
        this);
    this.add(new JMenuItem(GuiCommands.COMMAND_DISABLE_ALL)).addActionListener(
        this);
    this.add(new JMenuItem(GuiCommands.COMMAND_SHUTDOWN_DATABASE))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_VIEW_SQL_STATS))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_VIEW_CACHE_CONTENT))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_VIEW_RECOVERY_LOG))
        .addActionListener(this);
    this.add(new JMenuItem(GuiCommands.COMMAND_MONITOR_DATABASE))
        .addActionListener(this);
  }

  /**
   * Returns the database value.
   * 
   * @return Returns the database.
   */
  public DatabaseObject getDatabaseName()
  {
    return database;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    String action = e.getActionCommand();

    if (action.equals(GuiCommands.COMMAND_ENABLE_ALL))
    {
      gui.publicActionDatabaseEnableAll(databaseName);
    }
    else if (action.equals(GuiCommands.COMMAND_DISABLE_ALL))
    {
      gui.publicActionDatabaseDisableAll(databaseName);
    }
    else if (action.equals(GuiCommands.COMMAND_SHUTDOWN_DATABASE))
    {
      gui.publicActionDisplayShutdownFrame(database);
    }
    else if (action.equals(GuiCommands.COMMAND_DISPLAY_XML_DATABASE))
    {
      gui.publicActionLoadXmlDatabase(databaseName);
    }
    else if (action.equals(GuiCommands.COMMAND_VIEW_CACHE_CONTENT))
    {
      gui.publicActionViewCache(databaseName);
    }
    else if (action.equals(GuiCommands.COMMAND_VIEW_SQL_STATS))
    {
      gui.publicActionViewSQLStats(databaseName);
    }
    else if (action.equals(GuiCommands.COMMAND_VIEW_CACHE_STATS))
    {
      gui.publicActionViewCacheStats(databaseName);
    }
    else if (action.equals(GuiCommands.COMMAND_VIEW_RECOVERY_LOG))
    {
      gui.publicActionViewRecoveryLog(databaseName);
    }
    else if (action.equals(GuiCommands.COMMAND_MONITOR_DATABASE))
    {
      gui.publicActionStartMonitor(database.getControllerName(), false, true,
          false);
    }
  }
}