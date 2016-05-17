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
 * Initial developer(s): Jeff Mesnil
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.commands.sqlconsole;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.ColorPrinter;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.commands.ConsoleCommand;
import org.objectweb.cjdbc.console.text.formatter.TableFormatter;
import org.objectweb.cjdbc.console.text.module.AbstractConsoleModule;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseConsole;

/**
 * This class defines a ShowTables
 * 
 * @author <a href="mailto:jeff.mesnil@emicnetworks.com">Jeff Mesnil</a>
 */
public class ShowTables extends ConsoleCommand
{

  /**
   * Creates a new <code>Quit.java</code> object
   * 
   * @param module the command is attached to
   */
  public ShowTables(AbstractConsoleModule module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws IOException, ConsoleException
  {
    try
    {
      DatabaseMetaData dbmd = ((VirtualDatabaseConsole)module).getConnection().getMetaData();
      ResultSet tableSet = dbmd.getTables(null, null, null, new String[] {"TABLE", "VIEW"});
      if (tableSet == null) 
      {
        console.printInfo(ConsoleTranslate.get("sql.command.show.tables.no.tables"));
        return;
      }
      ArrayList tableNames = new ArrayList();
      while (!tableSet.isLast())
      {
        tableSet.next();
        tableNames.add(tableSet.getString(tableSet.findColumn("TABLE_NAME")));
      }
      console.println(TableFormatter.format(new String[] {"tables"}, getTableNamesAsCells(tableNames), true), ColorPrinter.STATUS);
    }
    catch (Exception e)
    {
      console.printError(ConsoleTranslate.get("sql.command.sqlquery.error", e),
          e);
    }
  }

  private String[][] getTableNamesAsCells(ArrayList tableNames)
  { 
    String[][] cells = new String[tableNames.size()][1];
    for (int i = 0; i < tableNames.size(); i++)
    {
      cells[i][0] = (String)tableNames.get(i);
    }
    return cells;
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "show tables";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("sql.command.show.tables.description");
  }
}