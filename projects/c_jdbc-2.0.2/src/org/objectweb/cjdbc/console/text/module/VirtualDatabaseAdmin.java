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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Mathieu Peltier, Nicolas Modrzyk
 */

package org.objectweb.cjdbc.console.text.module;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.console.text.Console;
import org.objectweb.cjdbc.console.text.commands.Help;
import org.objectweb.cjdbc.console.text.commands.History;
import org.objectweb.cjdbc.console.text.commands.Quit;
import org.objectweb.cjdbc.console.text.commands.dbadmin.Backup;
import org.objectweb.cjdbc.console.text.commands.dbadmin.CopyLogFromCheckpoint;
import org.objectweb.cjdbc.console.text.commands.dbadmin.DeleteLogUpToCheckpoint;
import org.objectweb.cjdbc.console.text.commands.dbadmin.Disable;
import org.objectweb.cjdbc.console.text.commands.dbadmin.DisableRead;
import org.objectweb.cjdbc.console.text.commands.dbadmin.Enable;
import org.objectweb.cjdbc.console.text.commands.dbadmin.EnableRead;
import org.objectweb.cjdbc.console.text.commands.dbadmin.ExpertMode;
import org.objectweb.cjdbc.console.text.commands.dbadmin.ForceDisable;
import org.objectweb.cjdbc.console.text.commands.dbadmin.ForceEnable;
import org.objectweb.cjdbc.console.text.commands.dbadmin.GetBackendSchema;
import org.objectweb.cjdbc.console.text.commands.dbadmin.GetXml;
import org.objectweb.cjdbc.console.text.commands.dbadmin.ListBackends;
import org.objectweb.cjdbc.console.text.commands.dbadmin.RemoveDump;
import org.objectweb.cjdbc.console.text.commands.dbadmin.Replicate;
import org.objectweb.cjdbc.console.text.commands.dbadmin.Restore;
import org.objectweb.cjdbc.console.text.commands.dbadmin.SetCheckpoint;
import org.objectweb.cjdbc.console.text.commands.dbadmin.ShowBackend;
import org.objectweb.cjdbc.console.text.commands.dbadmin.ShowControllers;
import org.objectweb.cjdbc.console.text.commands.dbadmin.TransferBackend;
import org.objectweb.cjdbc.console.text.commands.dbadmin.TransferDump;
import org.objectweb.cjdbc.console.text.commands.dbadmin.UpdateDumpPath;
import org.objectweb.cjdbc.console.text.commands.dbadmin.ViewBackupers;
import org.objectweb.cjdbc.console.text.commands.dbadmin.ViewCheckpointNames;
import org.objectweb.cjdbc.console.text.commands.dbadmin.ViewDumps;

/**
 * This is the C-JDBC controller console virtual database administration module.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class VirtualDatabaseAdmin extends AbstractConsoleModule
{
  private String virtualDbName, login, password;
  
  /**
   * Returns the login value.
   * 
   * @return Returns the login.
   */
  public String getLogin()
  {
    return login;
  }

  /**
   * Returns the password value.
   * 
   * @return Returns the password.
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Returns the virtualDbName value.
   * 
   * @return Returns the virtualDbName.
   */
  public String getVirtualDbName()
  {
    return virtualDbName;
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#login(java.lang.String[])
   */
  public void login(String[] params)
  {
    // In case a login has failed before
    quit = false;
    String vdbName = params[0];
    try
    {
      console.getConsoleReader().addCompletor(this.getCompletor());
      console.getConsoleReader().removeCompletor(
          console.getControllerModule().getCompletor());
      
      if (vdbName == null || vdbName.trim().equals(""))
      {
        vdbName = console.readLine(ConsoleTranslate.get("admin.login.dbname"));
        if (vdbName == null)
          return;
      }

      login = console.readLine(ConsoleTranslate.get("admin.login.user"));
      if (login == null)
        return;

      password = console.readPassword(ConsoleTranslate
          .get("admin.login.password"));
      if (password == null)
        return;

      try
      {
        ControllerMBean mbean = console.getJmxClient().getControllerProxy();
        if (!mbean.hasVirtualDatabase(vdbName))
        {
          console.printError(ConsoleTranslate.get("module.database.invalid",
              vdbName));
          quit();
          return;
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        // does not exists: quit
        console.printError(ConsoleTranslate.get("module.database.invalid",
            vdbName), e);
        quit();
      }
      try
      {
        VirtualDatabaseMBean vdb = console.getJmxClient()
            .getVirtualDatabaseProxy(vdbName, login, password);
        if (!vdb.checkAdminAuthentication(login, password))
        {
          console.printError(ConsoleTranslate.get("module.database.login.fail",
              login));
          quit();
        }
      }
      catch (Exception e)
      {
        console.printError(ConsoleTranslate.get("module.database.login.fail",
            login));
        quit();
      }
      this.virtualDbName = vdbName;
      if (quit)
        return;

      // Reload commands because target has changed
      loadCommands();
      console.println(ConsoleTranslate.get("admin.login.ready", virtualDbName));
    }
    catch (Exception e)
    {
      console.printError(e.getMessage(), e);
      quit();
    }

  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#getDescriptionString()
   */
  public String getDescriptionString()
  {
    return "VirtualDatabase Administration";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#getPromptString()
   */
  public String getPromptString()
  {
    return virtualDbName + "(" + login + ")";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#loadCommands()
   */
  protected void loadCommands()
  {
    commands.clear();
    commands.add(new Help(this));
    commands.add(new History(this));
    commands.add(new Quit(this));
    commands.add(new Backup(this));
    commands.add(new Disable(this));
    commands.add(new Enable(this));
    commands.add(new ExpertMode(this));
    commands.add(new GetXml(this));
    commands.add(new ListBackends(this));
    commands.add(new RemoveDump(this));
    commands.add(new Restore(this));
    commands.add(new ShowBackend(this));
    commands.add(new ShowControllers(this));
    commands.add(new TransferDump(this));
    commands.add(new ViewBackupers(this));
    commands.add(new ViewDumps(this));
  }

  /**
   * Create a <code>Set</code> of expert commands.
   * This <code>Set</code> of commands can be added/removed dynamically
   * to the list of admin commands with the methods {@link addExpertCommands()}
   * and {@link removeExpertCommands()}
   * @return
   */
  private Set expertCommandsSet()
  {
    Set expertCmds = new HashSet();
    expertCmds.add(new CopyLogFromCheckpoint(this));
    expertCmds.add(new DeleteLogUpToCheckpoint(this));
    expertCmds.add(new DisableRead(this));
    expertCmds.add(new EnableRead(this));
    expertCmds.add(new ForceEnable(this));
    expertCmds.add(new ForceDisable(this));
    expertCmds.add(new GetBackendSchema(this));
    expertCmds.add(new Replicate(this));
    expertCmds.add(new SetCheckpoint(this));
    expertCmds.add(new TransferBackend(this));
    expertCmds.add(new UpdateDumpPath(this));
    expertCmds.add(new ViewCheckpointNames(this));
    return expertCmds;
  }
  
  /**
   * Add the expert commands to the list of admin commands.
   * 
   */
  public void addExpertCommands()
  {
    commands.addAll(expertCommandsSet());
    // reload the completor or the newly added
    // commands won't be taken into account
    reloadCompletor();
  }

  /**
   * Revmoe the expert commands from the list of admin commands.
   * 
   */
  public void removeExpertCommands()
  {
    commands.removeAll(expertCommandsSet());
    // reload the completor or the removed
    // commands will still be taken into account
    reloadCompletor();
  }

  /**
   * Creates a new <code>VirtualDatabaseAdmin</code> instance.
   * 
   * @param console console console
   */
  public VirtualDatabaseAdmin(Console console)
  {
    super(console);
  }
}