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

import java.util.HashMap;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines a Replicate
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Replicate extends AbstractAdminCommand
{

  /**
   * Creates a new <code>Replicate.java</code> object
   * 
   * @param module admin module
   */
  public Replicate(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    StringTokenizer st = new StringTokenizer(commandText, "; ");
    if (st.countTokens() < 3)
    {
      console.printError(getUsage());
      return;
    }
    
    String backend1 = st.nextToken();
    String backend2 = st.nextToken();
    String url = st.nextToken();

    HashMap parameters = new HashMap();
    parameters.put("url",url);
    StringTokenizer st2;
    while (st.hasMoreTokens())
    {
      st2 = new StringTokenizer(st.nextToken(), "=");
      if (st2.countTokens() == 2)
      {
        String param = st2.nextToken();
        String value = st2.nextToken();
        parameters.put(param, value);
        console.println(ConsoleTranslate.get("admin.command.replicate.param",
            new String[]{param, value}));
      }
    }

    console.println(ConsoleTranslate.get("admin.command.replicate.echo",
        new String[]{backend1, backend2, url}));
    jmxClient.getVirtualDatabaseProxy(dbName, user, password).replicateBackend(
        backend1, backend2, parameters);

  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.replicate.params");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "clone backend config";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.replicate.description");
  }

}