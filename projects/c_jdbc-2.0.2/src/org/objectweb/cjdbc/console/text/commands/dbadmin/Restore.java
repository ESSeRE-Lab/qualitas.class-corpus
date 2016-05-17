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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines a Restore
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class Restore extends AbstractAdminCommand
{

  /**
   * Creates a new <code>Restore.java</code> object
   * 
   * @param module the command is attached to
   */
  public Restore(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    String dumpName = null;
    String backendName = null;
    StringTokenizer st = new StringTokenizer(commandText.trim());

    try
    {
      backendName = st.nextToken();
      dumpName = st.nextToken();
      ArrayList tables = null;
      if (st.hasMoreTokens())
      {
        tables = new ArrayList();
        while (st.hasMoreTokens())
        {
          tables.add(st.nextToken());
        }
      }

      String login = console.readLine(ConsoleTranslate
          .get("admin.restore.user"));
      if (login == null)
        return;

      String pwd = console.readPassword(ConsoleTranslate
          .get("admin.restore.password"));
      if (pwd == null)
        return;

      console.println(ConsoleTranslate.get("admin.command.restore.echo",
          new String[]{backendName, dumpName}));
      VirtualDatabaseMBean vjdc = jmxClient.getVirtualDatabaseProxy(dbName,
          user, password);
      vjdc.restoreDumpOnBackend(backendName, login, pwd, dumpName, tables);
    }
    catch (Exception e)
    {
      if (dumpName == null)
      {
        console
            .println(ConsoleTranslate.get("admin.command.restore.need.dump"));
      }
      if (backendName == null)
      {
        console.println(ConsoleTranslate
            .get("admin.command.restore.need.backend"));
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {

    return "restore backend";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.restore.description");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.restore.params");
  }
}