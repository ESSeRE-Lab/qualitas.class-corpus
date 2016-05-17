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

import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines the command used to copy the local virtual database
 * recovery log onto a remote controller's peer virtual database log. The copy
 * is performed from the specified checkpoint uptil 'now' (a new global
 * checkpoint). The copy is sent to the specified remote node.
 * 
 * @author <a href="mailto:Olivier.Fambon@emicnetworks.com">Olivier Fambon </a>
 * @version 1.0
 */
public class CopyLogFromCheckpoint extends AbstractAdminCommand
{
  /**
   * Creates a new <code>Disable.java</code> object
   * 
   * @param module the command is attached to
   */
  public CopyLogFromCheckpoint(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    StringTokenizer st = new StringTokenizer(commandText);
    String checkpointName = null, controllerName = null;
    try
    {
      checkpointName = st.nextToken();
      controllerName = st.nextToken();
    }
    catch (Exception e)
    {
      console.printError(getUsage());
    }

    jmxClient.getVirtualDatabaseProxy(dbName, user, password)
        .copyLogFromCheckpoint(checkpointName, controllerName);
    /*
     * console.println(ConsoleTranslate.get(
     * "admin.command.disableBackend.with.checkpoint", new String[]{
     * backendName, checkpoint}));
     */
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.restore.log.params");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "restore log";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.restore.log.description");
  }

}
