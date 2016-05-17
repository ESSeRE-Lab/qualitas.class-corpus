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

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.common.shared.DumpInfo;
import org.objectweb.cjdbc.console.text.ColorPrinter;
import org.objectweb.cjdbc.console.text.formatter.TableFormatter;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines the command used to display available dumps of a given
 * virtual database.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class ViewDumps extends AbstractAdminCommand
{

  /**
   * Creates a "view dumps" command for the admin module.
   * 
   * @param module module that owns this commands
   */
  public ViewDumps(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    VirtualDatabaseMBean vdjc = jmxClient.getVirtualDatabaseProxy(dbName, user,
        password);
    DumpInfo[] dumps = vdjc.getAvailableDumps();
    if (dumps.length == 0)
    {
      console.printError(ConsoleTranslate.get("admin.command.view.dumps.nodump"));
    }
    else
    {
      String formattedDumps = TableFormatter.format(getDumpsDescriptions(),
          getDumpsAsStrings(dumps), true);
      console.println(formattedDumps, ColorPrinter.STATUS);
    }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "show dumps";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.view.dumps.description");
  }

  private static String[][] getDumpsAsStrings(DumpInfo[] dumps)
  {
    String[][] dumpStr = new String[dumps.length][7];
    for (int i = 0; i < dumpStr.length; i++)
    {
      DumpInfo dump = dumps[i];
      dumpStr[i][0] = dump.getDumpName();
      dumpStr[i][1] = dump.getCheckpointName();
      dumpStr[i][2] = dump.getDumpFormat();
      dumpStr[i][3] = dump.getDumpPath();
      dumpStr[i][4] = dump.getDumpDate();
      dumpStr[i][5] = dump.getBackendName();
      dumpStr[i][6] = dump.getTables();
    }
    return dumpStr;
  }

  private static String[] getDumpsDescriptions()
  {
    String[] desc = new String[7];
    desc[0] = ConsoleTranslate.get("admin.command.view.dumps.prop.name");
    desc[1] = ConsoleTranslate.get("admin.command.view.dumps.prop.checkpoint");
    desc[2] = ConsoleTranslate.get("admin.command.view.dumps.prop.format");
    desc[3] = ConsoleTranslate.get("admin.command.view.dumps.prop.path");
    desc[4] = ConsoleTranslate.get("admin.command.view.dumps.prop.date");
    desc[5] = ConsoleTranslate.get("admin.command.view.dumps.prop.backend");
    desc[6] = ConsoleTranslate.get("admin.command.view.dumps.prop.tables");
    return desc;
  }
}