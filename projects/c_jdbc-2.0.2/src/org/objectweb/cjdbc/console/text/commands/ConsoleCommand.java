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
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.console.text.commands;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.jmx.RmiJmxClient;
import org.objectweb.cjdbc.console.text.Console;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.module.AbstractConsoleModule;

/**
 * This class defines a ConsoleCommand
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public abstract class ConsoleCommand implements Comparable
{
  protected Console               console;
  protected RmiJmxClient          jmxClient;
  protected AbstractConsoleModule module;

  /**
   * Creates a new <code>ConsoleCommand.java</code> object
   * 
   * @param module module that owns this commands
   */
  public ConsoleCommand(AbstractConsoleModule module)
  {
    this.console = module.getConsole();
    this.module = module;
    jmxClient = console.getJmxClient();
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    if (o instanceof ConsoleCommand)
    {
      ConsoleCommand c = (ConsoleCommand) o;
      return getCommandName().compareTo(c.getCommandName());
    }
    else
    {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Parse the text of the command
   * 
   * @param commandText the command text
   * @throws Exception if connection with the mbean server is lost or command
   *           does not have the proper format
   */
  public abstract void parse(String commandText) throws Exception;

  /**
   * Check if the JMX connection is still valid. Otherwise reconnect.
   * 
   * @param commandText the parameters to execute the command with
   * @throws Exception if fails
   */
  public void execute(String commandText) throws Exception
  {
    if (!jmxClient.isValidConnection())
    {
      try
      {
        jmxClient.reconnect();
      }
      catch (Exception e)
      {
        throw new ConsoleException(ConsoleTranslate
            .get("jmx.server.connection.lost"));
      }
    }
    parse(commandText);
  }

  /**
   * Get the name of the command
   * 
   * @return <code>String</code> of the command name
   */
  public abstract String getCommandName();

  /**
   * Return a <code>String</code> description of the parameters of this
   * command.
   * 
   * @return <code>String</code> like &lt;driverPathName&gt;
   */
  public String getCommandParameters()
  {
    return "";
  }

  /**
   * Get the description of the command
   * 
   * @return <code>String</code> of the command description
   */
  public abstract String getCommandDescription();

  /**
   * Get the usage of the command.
   * 
   * @return <code>String</code> of the command usage ()
   */
  public String getUsage() 
  {
    String usage = ConsoleTranslate.get("command.usage", new String[] {getCommandName(), getCommandParameters()}); 
    usage += "\n   " + getCommandDescription();
    return usage;
  }
}

