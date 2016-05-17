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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.commands.ConsoleCommand;
import org.objectweb.cjdbc.console.text.module.AbstractConsoleModule;

/**
 * This class defines a AddDriver
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class AddDriver extends ConsoleCommand
{

  /**
   * Creates a new <code>AddDriver.java</code> object
   * 
   * @param module the command is attached to
   */
  public AddDriver(AbstractConsoleModule module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws IOException, ConsoleException
  {
    String filename = null;
    //  Get the file name if needed
    if (commandText == null || commandText.trim().equals(""))
    {
      try
      {
        filename = console.readLine(ConsoleTranslate
            .get("controller.command.add.driver.input.filename"));
      }
      catch (Exception che)
      {
      }
    }
    else
      filename = commandText.trim();

    if (filename == null || filename.equals(""))
      throw new ConsoleException(ConsoleTranslate
          .get("controller.command.add.driver.null.filename"));

    try
    {
      // Send the file contents to the controller
      jmxClient.getControllerProxy().addDriver(readDriver(filename));
      console.println(ConsoleTranslate.get(
          "controller.command.add.driver.file.sent", filename));
    }
    catch (FileNotFoundException fnf)
    {
      throw new ConsoleException(ConsoleTranslate.get(
          "controller.command.add.driver.file.not.found", filename));
    }
    catch (Exception ioe)
    {
      throw new ConsoleException(ConsoleTranslate.get(
          "controller.command.add.driver.sent.failed", ioe));
    }
  }

  private byte[] readDriver(String filename) throws FileNotFoundException,
      IOException
  {
    File file;
    FileInputStream fileInput = null;
    file = new File(filename);
    fileInput = new FileInputStream(file);

    // Read the file into an array of bytes
    long size = file.length();
    if (size > Integer.MAX_VALUE)
      throw new IOException(ConsoleTranslate
          .get("controller.command.add.driver.file.too.big"));
    byte[] bytes = new byte[(int) size];
    int nb = fileInput.read(bytes);
    fileInput.close();
    if (nb != size)
      throw new IOException(ConsoleTranslate
          .get("controller.command.add.driver.file.not.read"));
    return bytes;
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "upload driver";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("controller.command.add.driver.description");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("controller.command.add.driver.params");
  }

}
