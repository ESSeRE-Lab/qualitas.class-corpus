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
import java.sql.Connection;
import java.sql.Savepoint;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.commands.ConsoleCommand;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseConsole;

/**
 * This class defines a "savepoint" sql command
 * 
 * @author <a href="mailto:jeff.mesnil@emicnetworks.com">Jeff Mesnil</a>
 */
public class SetSavePoint extends ConsoleCommand
{

  /**
   * Creates a new <code>SetSavePoint</code> object
   * 
   * @param module the command is attached to
   */
  public SetSavePoint(VirtualDatabaseConsole module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws IOException, ConsoleException
  {
    if (commandText.trim().length() == 0) 
    {
      console.printError(getUsage());
      return;
    }
    String name = commandText.trim();
    
    Connection connection = ((VirtualDatabaseConsole) module).getConnection();
    try
    {
      Savepoint savePoint = connection.setSavepoint(name);
      ((VirtualDatabaseConsole) module).addSavePoint(savePoint);
      console.println(ConsoleTranslate.get("sql.command.savepoint.saved", name));
    }
    catch (Exception e)
    {
      console.printError(ConsoleTranslate.get("sql.display.exception", e), e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "savepoint";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("sql.command.savepoint.params");
     }
  
  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("sql.command.savepoint.description");
  }
}