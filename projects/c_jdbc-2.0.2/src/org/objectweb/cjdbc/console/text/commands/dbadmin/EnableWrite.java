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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines a EnableWrite
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class EnableWrite extends AbstractAdminCommand
{

  /**
   * Creates a new <code>EnableWrite</code> object
   * 
   * @param module owning module
   */
  public EnableWrite(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    StringTokenizer st = new StringTokenizer(commandText);
    if (st.countTokens() != 1)
      throw new ConsoleException(ConsoleTranslate
          .get("console.error.invalid.comment.argument"));
    String backendName = st.nextToken();
    console.println(ConsoleTranslate.get("admin.command.enable.write.echo",
        backendName));
    VirtualDatabaseMBean vdjc = jmxClient.getVirtualDatabaseProxy(dbName, user,
        password);
    DatabaseBackendMBean mbean = jmxClient.getDatabaseBackendProxy(dbName,
        backendName, user, password);
    if(vdjc.hasRecoveryLog())
    {
      if(mbean.isWriteEnabled())
        return;
        vdjc.enableBackendFromCheckpoint(backendName);
    }
    else
    {
      mbean.enableWrite();
    }
      

  }
  
  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "enableWrite";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.enable.write");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return "<backendName>";
  }

}
