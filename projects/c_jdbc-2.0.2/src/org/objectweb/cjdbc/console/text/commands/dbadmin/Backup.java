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
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines the Backup command for the text console. It backups a
 * backend to a dump file.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class Backup extends AbstractAdminCommand
{

  /**
   * Creates a new <code>Backup.java</code> object
   * 
   * @param module the command is attached to
   */
  public Backup(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {

    StringTokenizer st = new StringTokenizer(commandText.trim());

    if (st == null || st.countTokens() < 4)
      throw new ConsoleException("Usage: " + getCommandName() + " "
          + getCommandParameters());

    String backendName = st.nextToken();
    String dumpName = st.nextToken();
    String backuperName = st.nextToken();
    String path = st.nextToken();
    ArrayList tables = null;
    if (st.hasMoreTokens())
    {
      tables = new ArrayList();
      while (st.hasMoreTokens())
      {
        tables.add(st.nextToken());
      }
    }

    String login = console.readLine(ConsoleTranslate.get("admin.backup.user"));
    if (login == null)
      return;

    String pwd = console.readPassword(ConsoleTranslate
        .get("admin.backup.password"));
    if (pwd == null)
      return;

    console.println(ConsoleTranslate.get("admin.command.backup.echo",
        new String[]{backendName, dumpName}));
    if (tables != null)
      console.println(ConsoleTranslate.get("admin.command.backup.tables",
          tables));
    VirtualDatabaseMBean vdjc = jmxClient.getVirtualDatabaseProxy(dbName, user,
        password);
    vdjc.backupBackend(backendName, login, pwd, dumpName, backuperName, path,
        tables);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "backup";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.backup.description");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.backup.params");
  }

}