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
 * Initial developer(s): Jeff Mesnil.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines the command used to switch to <em>expert mode</em>
 * (i.e. advanced admin commands are available).
 * 
 * @author <a href="mailto:jeff.mesnil@emicnetworks.com">Jeff Mesnil</a>
 * @version 1.0
 */
public class ExpertMode extends AbstractAdminCommand
{

  private static final String ON = "on";
  private static final String OFF = "off";
  
  /**
   * Creates a new <code>ExpertMode</code> object
   * 
   * @param module module that owns this commands
   */
  public ExpertMode(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {     
    if (ON.equals(commandText.trim())) 
    {
      ((VirtualDatabaseAdmin)module).addExpertCommands(); 
      console.printInfo(ConsoleTranslate.get("admin.command.expert.mode.switched.on"));
    } else if (OFF.equals(commandText.trim())) 
    {
      ((VirtualDatabaseAdmin)module).removeExpertCommands(); 
      console.printInfo(ConsoleTranslate.get("admin.command.expert.mode.switched.off"));         
    } else 
    {
      console.printError(getUsage());
    }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "expert";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.expert.mode.description");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.expert.mode.params");
  }
}
