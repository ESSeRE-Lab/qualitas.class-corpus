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
 * Contributor(s): Mathieu Peltier, Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.console.text.commands.controller;

import java.io.IOException;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.commands.ConsoleCommand;
import org.objectweb.cjdbc.console.text.module.AbstractConsoleModule;

/**
 * This class defines a Shutdown
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class Shutdown extends ConsoleCommand
{

  /**
   * Creates a new <code>Shutdown.java</code> object
   * 
   * @param module the command is attached to
   */
  public Shutdown(AbstractConsoleModule module)
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
      if (commandText.indexOf(String.valueOf(Constants.SHUTDOWN_SAFE)) != -1)
        jmxClient.getControllerProxy().shutdown(Constants.SHUTDOWN_SAFE);
      else if (commandText.indexOf(String.valueOf(Constants.SHUTDOWN_WAIT)) != -1)
        jmxClient.getControllerProxy().shutdown(Constants.SHUTDOWN_WAIT);
      else if (commandText.indexOf(String.valueOf(Constants.SHUTDOWN_FORCE)) != -1)
        jmxClient.getControllerProxy().shutdown(Constants.SHUTDOWN_FORCE);
      else
        // Defaults to safe mode
        jmxClient.getControllerProxy().shutdown(Constants.SHUTDOWN_SAFE);

      console.println("Shutdown was complete");
    }
    catch (Exception e)
    {
      if (jmxClient.isValidConnection())
        console.printError("Could not shutdown the controller:"
            + e.getMessage());
      else
        console.printInfo("Controller has shutdown");
    }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "shutdown";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return "[mode]";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("controller.command.shutdown");
  }
}