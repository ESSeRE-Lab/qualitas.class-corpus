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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines a ShowBackendState
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ShowBackendState extends AbstractAdminCommand
{

  /**
   * Creates a new <code>ShowBackendState</code> object
   * 
   * @param module the calling module
   */
  public ShowBackendState(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    if (commandText == null || commandText.trim().equals(""))
    {
      console.printError(ConsoleTranslate
          .get("admin.command.showbackend.state.no.input"));
      return;
    }
    StringTokenizer st = new StringTokenizer(commandText);

    String backendName = st.nextToken();

    if (!jmxClient.getVirtualDatabaseProxy(dbName, user, password)
        .getAllBackendNames().contains(backendName))
    {
      console.printError(ConsoleTranslate.get(
          "admin.command.showbackend.state.invalid.backend", backendName));
      return;
    }

    int count = 0;
    if (st.hasMoreTokens())
    {
      try
      {
        count = Integer.parseInt(st.nextToken());
      }
      catch (NumberFormatException e)
      {
      }
    }
    boolean fromFirst = true;
    if (st.hasMoreTokens())
      fromFirst = Boolean.valueOf(st.nextToken()).booleanValue();
    boolean clone = false;
    if (st.hasMoreTokens())
      clone = Boolean.valueOf(st.nextToken()).booleanValue();

    //TODO: Use a BackendInfo object to transfer the backend state
    console.println(ConsoleTranslate.get(
        "admin.command.showbackend.state.echo", new Object[]{backendName,
            String.valueOf(count), String.valueOf(fromFirst),
            String.valueOf(clone)}));
    DatabaseBackendMBean db = jmxClient.getDatabaseBackendProxy(dbName,
        backendName, user, password);

    ArrayList list = db.getActiveTransactions();
    console.printInfo(ConsoleTranslate
        .get("admin.command.showbackend.state.state"));
    console.println(db.getState());
    console.printInfo(ConsoleTranslate
        .get("admin.command.showbackend.state.active.transactions"));
    for (int i = 0; i < list.size(); i++)
    {
      if (i != 0)
        console.print(",");
      console.print(list.get(i).toString());
    }
    console.println();
    console.printInfo(ConsoleTranslate
        .get("admin.command.showbackend.state.pending.requests"));
    try
    {
      list = db.getPendingRequestsDescription(count, fromFirst, clone);
      for (int i = 0; i < list.size(); i++)
        console.println((String) list.get(i));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "showbackend";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.showbackend.state");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return "<backendName> [<count>] [<fromFirst>] [<clone>]";
  }
}