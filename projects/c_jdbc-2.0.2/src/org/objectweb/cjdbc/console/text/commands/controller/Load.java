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

package org.objectweb.cjdbc.console.text.commands.controller;

import java.io.BufferedReader;
import java.io.FileReader;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.commands.ConsoleCommand;
import org.objectweb.cjdbc.console.text.module.AbstractConsoleModule;

/**
 * This class defines a Load
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Load extends ConsoleCommand
{
  /**
   * Creates a new <code>Load.java</code> object
   * 
   * @param module the command is attached to 
   */
  public Load(AbstractConsoleModule module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    String filename = null;
    // Get the file name if needed
    if (commandText == null || commandText.trim().equals(""))
      filename = console.readLine(ConsoleTranslate
          .get("controller.command.load.vdb.input"));
    else
      filename = commandText.trim();
    if (filename == null)
      throw new ConsoleException(ConsoleTranslate
          .get("controller.command.load.vdb.file.null"));
    FileReader fileReader;
    fileReader = new FileReader(filename);

    // Read the file
    BufferedReader in = new BufferedReader(fileReader);
    StringBuffer xml = new StringBuffer();
    String line;
    do
    {
      line = in.readLine();
      if (line != null)
        xml.append(line);
    }
    while (line != null);

    // Send it to the controller
    jmxClient.getControllerProxy().addVirtualDatabases(xml.toString());
    console.println(ConsoleTranslate.get("controller.command.load.vdb.success",
        filename));
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "load virtualdatabase config";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("controller.command.load.vdb.description");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("controller.command.load.vdb.params");
  }
}
