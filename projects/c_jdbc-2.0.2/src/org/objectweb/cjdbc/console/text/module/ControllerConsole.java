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

package org.objectweb.cjdbc.console.text.module;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.Console;
import org.objectweb.cjdbc.console.text.commands.controller.AddDriver;
import org.objectweb.cjdbc.console.text.commands.controller.Admin;
import org.objectweb.cjdbc.console.text.commands.controller.Bind;
import org.objectweb.cjdbc.console.text.commands.controller.Connect;
import org.objectweb.cjdbc.console.text.commands.controller.DropDB;
import org.objectweb.cjdbc.console.text.commands.controller.GetXml;
import org.objectweb.cjdbc.console.text.commands.controller.ListDatabases;
import org.objectweb.cjdbc.console.text.commands.controller.Load;
import org.objectweb.cjdbc.console.text.commands.controller.Monitor;
import org.objectweb.cjdbc.console.text.commands.controller.RefreshLogs;
import org.objectweb.cjdbc.console.text.commands.controller.SaveConfiguration;
import org.objectweb.cjdbc.console.text.commands.controller.ShowLoggingConfig;
import org.objectweb.cjdbc.console.text.commands.controller.Shutdown;

/**
 * This class defines a ControllerConsole
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ControllerConsole extends AbstractConsoleModule
{

  /**
   * Creates a new <code>ControllerConsole.java</code> object
   * 
   * @param console the controller console is attached to
   */
  public ControllerConsole(Console console)
  {
    super(console);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#loadCommands()
   */
  protected void loadCommands()
  {
    commands.add(new AddDriver(this));
    commands.add(new RefreshLogs(this));
    commands.add(new Shutdown(this));
    commands.add(new GetXml(this));
    commands.add(new SaveConfiguration(this));
    commands.add(new ShowLoggingConfig(this));
    commands.add(new ListDatabases(this));
    commands.add(new Load(this));
    commands.add(new Admin(this));
    commands.add(new Bind(this));
    commands.add(new Connect(this));
    commands.add(new Monitor(this));
    commands.add(new DropDB(this));
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#quit()
   */
  public void quit()
  {
    super.quit();
    console.println(ConsoleTranslate.get("console.byebye"));
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#getPromptString()
   */
  public String getPromptString()
  {
    return console.getJmxClient().getRemoteName();
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#login(String[])
   */
  public void login(String[] params)
  {
    // do nothing
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#getDescriptionString()
   */
  public String getDescriptionString()
  {
    return "Controller";
  }
}
