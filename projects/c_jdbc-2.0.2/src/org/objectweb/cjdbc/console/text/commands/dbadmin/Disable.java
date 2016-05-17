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

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines a Disable
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Disable extends AbstractAdminCommand
{
  /**
   * Creates a new <code>Disable.java</code> object
   * 
   * @param module the command is attached to
   */
  public Disable(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    String backendName = null;

    StringTokenizer st = new StringTokenizer(commandText);
    if (st.countTokens() != 1)
    {
      console.printError(getUsage());
      return;
    }
    try
    {
      backendName = st.nextToken();
    }
    catch (Exception e)
    {
      console.printError(getUsage());
      return;
    }

    VirtualDatabaseMBean vjdc = jmxClient.getVirtualDatabaseProxy(dbName, user,
        password);
    if ("*".equals(backendName))
    {
      console.println(ConsoleTranslate
          .get("admin.command.disable.backend.all.with.checkpoint"));
      ArrayList backendNames = vjdc.getAllBackendNames();
      for (Iterator iter = backendNames.iterator(); iter.hasNext();)
      {
        String backend = (String) iter.next();
        vjdc.disableBackendWithCheckpoint(backend);
      }
    }
    else
    {
      vjdc.disableBackendWithCheckpoint(backendName);
      console.println(ConsoleTranslate.get(
          "admin.command.disable.backend.with.checkpoint", backendName));
    }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.disable.backend.params");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "disable";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.disable.backend.description");
  }

}
