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
 * Initial developer(s): Olivier Fambon.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines the command used to cleanup the recovery log upto a
 * specified checkpoint.
 * 
 * @author <a href="mailto:olivier.fambon@emicnetworks.com">Olivier Fambon </a>
 * @version 1.0
 */
public class DeleteLogUpToCheckpoint extends AbstractAdminCommand
{
  /**
   * Creates a new <code>RemoveCheckpoint.java</code> object
   * 
   * @param module the command is attached to
   */
  public DeleteLogUpToCheckpoint(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    String checkpointName = commandText.trim();

    if ("".equals(checkpointName))
    {
      console.printError(getUsage());
      return;
    }

    jmxClient.getVirtualDatabaseProxy(dbName, user, password)
        .deleteLogUpToCheckpoint(checkpointName);
    /*
     * jmxClient.getVirtualDatabaseProxy(dbName, user, password).removeDumpFile(
     * new File(commandText.trim()));
     */
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "purge log";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.deleteLogUpToCheckpoint.params");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate
        .get("admin.command.deleteLogUpToCheckpoint.description");
  }

}