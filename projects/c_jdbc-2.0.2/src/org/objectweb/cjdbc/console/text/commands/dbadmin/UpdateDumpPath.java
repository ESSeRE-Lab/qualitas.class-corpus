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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines a "update dump path" admin command.
 * 
 * @author <a href="mailto:jeff.mesnil@emicnetworks.com">Jeff Mesnil</a>
 */
public class UpdateDumpPath extends AbstractAdminCommand
{
  
  /**
   * Creates an "update backup path" command.
   * 
   * @param module the command is attached to
   */
  public UpdateDumpPath(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    String dumpName = null;
    String newPath = null;
    StringTokenizer st = new StringTokenizer(commandText.trim());

    if (st.countTokens() != 2) 
    {
      console.printError(getUsage());
      return;
    }
    try
    {
      dumpName = st.nextToken();
      newPath = st.nextToken();
      if (dumpName == null || newPath == null)
      {
        console.printError(getUsage());
        return;
      }

      console.println(ConsoleTranslate.get("admin.command.update.dump.path.echo",
          new String[]{dumpName, newPath}));
      VirtualDatabaseMBean vdjc = jmxClient.getVirtualDatabaseProxy(dbName, user,
          password);
      vdjc.updateDumpPath(dumpName, newPath);
      console.println(ConsoleTranslate.get("admin.command.update.dump.path.done",
          new String[]{dumpName, newPath}));
    }
    catch (Exception e)
    {
      console.printError("problem while updating path", e); //TODO I18N
    }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "force path";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.update.dump.path.description");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.update.dump.path.parameters");
  }
}
