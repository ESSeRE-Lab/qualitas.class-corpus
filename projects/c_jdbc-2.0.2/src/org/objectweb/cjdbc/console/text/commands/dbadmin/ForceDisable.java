/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import java.util.ArrayList;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines the ForceDisable command that forces the disabling of a
 * backend without setting a checkpoint.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class ForceDisable extends AbstractAdminCommand
{
  /**
   * Creates a new <code>Disable.java</code> object
   * 
   * @param module the command is attached to
   */
  public ForceDisable(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    if (commandText.trim().length() == 0)
    {
      console.printError(getUsage());
      return;
    }

    String[] backendNames;
    VirtualDatabaseMBean vjdc = jmxClient.getVirtualDatabaseProxy(dbName, user,
        password);
    
    if (("*").equals(commandText.trim()))
    {
      ArrayList backendNamesList = vjdc.getAllBackendNames();
      backendNames = (String[]) backendNamesList
          .toArray(new String[backendNamesList.size()]);
    }
    else
    {
      String backendName = commandText.trim();
      backendNames = new String[]{backendName};
    }
    for (int i = 0; i < backendNames.length; i++)
    {
      String backendName = backendNames[i];
      vjdc.forceDisableBackend(backendName);
      console.println(ConsoleTranslate.get(
          "admin.command.force.disable.backend", backendName));
      }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.force.disable.description");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "force disable";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.force.disable.params");
  }

}
