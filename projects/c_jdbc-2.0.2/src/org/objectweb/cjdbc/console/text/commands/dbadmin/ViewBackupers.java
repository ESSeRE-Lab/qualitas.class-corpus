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
import org.objectweb.cjdbc.console.text.ColorPrinter;
import org.objectweb.cjdbc.console.text.formatter.TableFormatter;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines the command used to display available backupers.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class ViewBackupers extends AbstractAdminCommand
{

  /**
   * Creates a "view backupers" command for the admin module.
   * 
   * @param module module that owns this commands
   */
  public ViewBackupers(VirtualDatabaseAdmin module)
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
    String[] backuperNames = vdjc.getBackuperNames();
    if (backuperNames.length == 0)
    {
      console.printError(ConsoleTranslate.get("admin.command.view.backupers.nobackuper"));
      return;
    }
    String[] dumpFormats = new String[backuperNames.length];
      for (int i = 0; i < backuperNames.length; i++)
      {
        String backuperName = backuperNames[i];
        String dumpFormat = vdjc.getDumpFormatForBackuper(backuperName);
        if (dumpFormat == null) 
        {
          dumpFormat = "";
        }
        dumpFormats[i] = dumpFormat;
      }
    String formattedBackupers = TableFormatter.format(getBackupersHeaders(),
        getBackupersAsCells(backuperNames, dumpFormats), true);
    console.println(formattedBackupers, ColorPrinter.STATUS);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "show backupers";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.view.backupers.description");
  }

  private static String[][] getBackupersAsCells(String[] backuperNames, String[] dumpFormats)
  {
    String[][] backupersTable = new String[backuperNames.length][2];
    for (int i = 0; i < backupersTable.length; i++)
    {
      backupersTable[i][0] = backuperNames[i];
      backupersTable[i][1] = dumpFormats[i];
      }
    return backupersTable;
  }

  private static String[] getBackupersHeaders()
  {
    return new String[] {
        ConsoleTranslate.get("admin.command.view.backupers.prop.name"),
        ConsoleTranslate.get("admin.command.view.backupers.prop.dump.format")
    };
    }
}