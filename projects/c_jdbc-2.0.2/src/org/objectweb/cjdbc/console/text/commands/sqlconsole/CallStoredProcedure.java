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
 * Initial developer(s): Jeff Mesnil.
 * Contributor(s): ______________________.
 */
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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.commands.ConsoleCommand;
import org.objectweb.cjdbc.console.text.formatter.ResultSetFormatter;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseConsole;

/**
 * This class defines a "timeout" sql command
 * 
 * @author <a href="mailto:jeff.mesnil@emicnetworks.com">Jeff Mesnil</a>
 */
public class CallStoredProcedure extends ConsoleCommand
{

  /**
   * Creates a new <code>SetTimeout</code> object
   * 
   * @param module the command is attached to
   */
  public CallStoredProcedure(VirtualDatabaseConsole module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws IOException, ConsoleException
  {
    callStoredProcedure(getCommandName() + " " + commandText);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "{call";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("sql.command.call.stored.procedure.params");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate
        .get("sql.command.call.stored.procedure.description");
  }

  /**
   * Call a store procedure.
   * 
   * @param proc the stored procedure to call
   */
  private synchronized void callStoredProcedure(String proc)
  {
    Connection connection = ((VirtualDatabaseConsole) module).getConnection();
    int fetchsize = ((VirtualDatabaseConsole) module).getFetchsize();
    CallableStatement cs = null;
    try
    {
      cs = connection.prepareCall(proc);
      cs.setQueryTimeout(((VirtualDatabaseConsole) module).getTimeout());

      long start = System.currentTimeMillis();
      long end;
      ResultSet rs = cs.executeQuery();
      end = System.currentTimeMillis();
      ResultSetFormatter.formatAndDisplay(rs, fetchsize, console);
      console.println(ConsoleTranslate.get("sql.display.query.time",
          new Long[]{new Long((end - start) / 1000),
              new Long((end - start) % 1000)}));
    }
    catch (Exception e)
    {
      console.printError(ConsoleTranslate.get(
          "sql.command.call.stored.procedure.failed", e), e);
    }
    finally
    {
      try
      {
        cs.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }
}