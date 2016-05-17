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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.console.text.module;

import java.util.Iterator;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.Console;
import org.objectweb.cjdbc.console.text.commands.monitor.AbstractMonitorCommand;
import org.objectweb.cjdbc.console.text.commands.monitor.ChangeTarget;
import org.objectweb.cjdbc.console.text.commands.monitor.ShowBackends;
import org.objectweb.cjdbc.console.text.commands.monitor.ShowCache;
import org.objectweb.cjdbc.console.text.commands.monitor.ShowCacheStats;
import org.objectweb.cjdbc.console.text.commands.monitor.ShowController;
import org.objectweb.cjdbc.console.text.commands.monitor.ShowDatabases;
import org.objectweb.cjdbc.console.text.commands.monitor.ShowRecoveryLog;
import org.objectweb.cjdbc.console.text.commands.monitor.ShowScheduler;
import org.objectweb.cjdbc.console.text.commands.monitor.ShowStats;

/**
 * Monitoring console to retrieve information from the controller.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 */
public class MonitorConsole extends AbstractConsoleModule
{

  private String currentTarget;

  /**
   * Returns the currentTarget value.
   * 
   * @return Returns the currentTarget.
   */
  public String getCurrentTarget()
  {
    return currentTarget;
  }

  /**
   * Sets the currentTarget value.
   * 
   * @param currentTarget The currentTarget to set.
   */
  public void setCurrentTarget(String currentTarget)
  {
    this.currentTarget = currentTarget;
    Object o;
    Iterator it = commands.iterator();
    while (it.hasNext())
    {
      o = it.next();
      if (o instanceof AbstractMonitorCommand)
      {
        ((AbstractMonitorCommand) o).setCurrentTarget(currentTarget);
      }
    }
  }

  /**
   * Creates a new <code>VirtualDatabaseAdmin</code> instance.
   * 
   * @param console console console
   */
  public MonitorConsole(Console console)
  {
    super(console);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#getDescriptionString()
   */
  public String getDescriptionString()
  {
    return "Monitoring";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#getPromptString()
   */
  public String getPromptString()
  {
    return "Monitoring:" + currentTarget;
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#loadCommands()
   */
  protected void loadCommands()
  {
    commands.add(new ChangeTarget(this));
    commands.add(new ShowBackends(this));
    commands.add(new ShowCache(this));
    commands.add(new ShowCacheStats(this));
    commands.add(new ShowController(this));
    commands.add(new ShowDatabases(this));
    commands.add(new ShowScheduler(this));
    commands.add(new ShowStats(this));
    commands.add(new ShowRecoveryLog(this));
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#login(java.lang.String[])
   */
  public void login(String[] params)
  {
    quit = false;
    
    String command = (params.length > 0 && params[0] != null) ? params[0] : "";
    if (command.equals(""))
    {
      console.printError(ConsoleTranslate.get("module.database.invalid", ""));
      quit = true;
    }
    else
    {
      try
      {
        new ChangeTarget(this).execute(params[0]);
        console.getConsoleReader().removeCompletor(
            console.getControllerModule().getCompletor());
        console.getConsoleReader().addCompletor(this.getCompletor());
      }
      catch (Exception e)
      {
        console.printError(e.getMessage(), e);
        quit = true;
      }
    }
  }
}