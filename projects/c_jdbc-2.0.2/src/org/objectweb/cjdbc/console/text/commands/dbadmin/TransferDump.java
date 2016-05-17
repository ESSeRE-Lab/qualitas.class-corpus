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
 * Initial developer(s): Jeff Mesnil.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines the command used to transfer a dump (identified by a dump
 * name ) from the current controller to another one.
 * 
 * @author <a href="mailto:jeff.mesnil@emicnetworks.com">Jeff Mesnil</a>
 */
public class TransferDump extends AbstractAdminCommand
{
  /**
   * Creates a new <code>TransferDump</code> object
   * 
   * @param module the command is attached to
   */
  public TransferDump(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    StringTokenizer st = new StringTokenizer(commandText);
    String dumpName = null;
    String controllerName = null;
    boolean noCopy = false;
    try
    {
      dumpName = st.nextToken();
      controllerName = st.nextToken();
      if (st.hasMoreTokens())
      {
        noCopy = st.nextToken().equalsIgnoreCase("nocopy");
      }
    }
    catch (Exception e)
    {
      console.printError(getUsage());
      return;
    }

    console.print(ConsoleTranslate.get("admin.command.transfer.dump.echo",
        new String[]{dumpName, controllerName}));
    jmxClient.getVirtualDatabaseProxy(dbName, user, password).transferDump(
        dumpName, controllerName, noCopy);
    console.print(ConsoleTranslate.get("admin.command.transfer.dump.done"));
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.transfer.dump.params");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "transfer dump";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.transfer.dump.description");
  }

}
