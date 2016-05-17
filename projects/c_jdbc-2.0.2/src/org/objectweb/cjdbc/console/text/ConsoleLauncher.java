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

package org.objectweb.cjdbc.console.text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.objectweb.cjdbc.common.jmx.JmxConstants;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.jmx.RmiJmxClient;
import org.objectweb.cjdbc.controller.core.ControllerConstants;

/**
 * This class defines a ConsoleLauncher
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:mathieu.peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public class ConsoleLauncher
{

  // TODO use other way of passing credentials between frames
  /** the credentials used when starting the gui */
  // public static Object credentials;
  /* main() method */

  /**
   * Launchs the C-JDBC console. The available options are: <il>
   * <li><code>-d</code> or <code>--debug</code>: show stack trace when
   * error occurs.</li>
   * <li><code>-f</code> or <code>--file</code>: use a given file as the
   * source of commands instead of reading commands interactively.</li>
   * <li><code>-h</code> or <code>--help</code>: displays usage
   * information.</li>
   * <li><code>-i</code> or <code>--ip</code>: IP address of the host name
   * where the JMX Server hosting the controller is running (the default is
   * '0.0.0.0').</li>
   * <li><code>-n</code> or <code>--nocolor</code>: do not print colors in
   * interactive mode for supported systems.</li>
   * <li><code>-p</code> or <code>--port</code>: JMX/RMI Port number of
   * (the default is
   * {@link org.objectweb.cjdbc.common.jmx.JmxConstants#DEFAULT_JMX_RMI_PORT}).
   * </li>
   * <li><code>-s</code> or <code>--secret</code>: password for JMX
   * connection.</li>
   * <li><code>-u</code> or <code>--username</code>: username for JMX
   * connection.</li>
   * <li><code>-v</code> or <code>--version</code>: displays version
   * information.</li>
   * </ul>
   * 
   * @param args command line arguments (see above)
   * @throws Exception if fails
   */
  public static void main(String[] args) throws Exception
  {
    // Create options object
    Options options = createOptions();

    // Parse command line
    CommandLineParser parser = new GnuParser();
    CommandLine commandLine = null;
    try
    {
      commandLine = parser.parse(options, args);
    }
    catch (ParseException e)
    {
      System.err.println("Syntax error (" + e + ")");
      printUsage(options);
      System.exit(1);
    }

    // Non-recognized options
    int n = commandLine.getArgs().length;
    for (int i = 0; i < n; i++)
    {
      System.err.println("Syntax error (unrecognized option: "
          + commandLine.getArgs()[i] + ")");
      printUsage(options);
      System.exit(1);
    }

    // Handle --help option
    if (commandLine.hasOption('h'))
    {
      if (commandLine.getOptions().length > 1)
        System.err.println("Syntax error");

      printUsage(options);
      System.exit(1);
    }

    // Handle --version option
    if (commandLine.hasOption('v'))
    {
      if (commandLine.getOptions().length > 1)
      {
        System.err.println("Syntax error");
        printUsage(options);
      }
      else
        System.out.println("C-JDBC controller console version "
            + Constants.VERSION);

      System.exit(1);
    }

    // Handle text/gui console start
    if (commandLine.hasOption('t') || commandLine.hasOption('f'))
    {
      startTextConsole(commandLine);
    }
    else
    {
      try
      {
        startGuiConsole();
      }
      catch (Throwable t)
      {
        System.out
            .println("Cannot initiate graphic mode. Loading text console instead.");
        startTextConsole(commandLine);
      }
    }

  }

  /**
   * Starts the gui
   * 
   * @throws Exception if cannot load gui(probably no graphics)
   */
  public static void startGuiConsole() throws Exception
  {
    // Set look and feel: Kunstoff not supported in Mac os X
    // so revert to default one
    String system = System.getProperty("os.name");
    if (system.indexOf("Mac OS") != -1)
    {
      try
      {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
      }
    }

    // set default encoding to UTF so we support japanese
    Properties pi = System.getProperties();
    pi.put("file.encoding", "UTF-8"); // To add a new
    // one
    System.setProperties(pi);
    new CjdbcGui();
  }

  /**
   * Starts the text console with the given commandline
   * 
   * @param commandLine parameters for the text console
   * @throws Exception if fails
   */
  public static void startTextConsole(CommandLine commandLine) throws Exception
  {
    // check if we are in interactive mode, and if so, output no traces
    boolean isInteractive = !commandLine.hasOption('f');

    // Handle --ip option
    String ip;
    try
    {
      ip = InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e1)
    {
      ip = "127.0.0.1";
    }
    if (commandLine.hasOption('i'))
    {
      String tmp = commandLine.getOptionValue('i');
      if (tmp != null)
      {
        ip = tmp;
      }
    }

    // Handle --port option
    int port;
    if (commandLine.hasOption('p'))
    {
      String s = commandLine.getOptionValue('p');
      if (s == null)
      {
        port = JmxConstants.DEFAULT_JMX_RMI_PORT;
      }
      else
        try
        {
          port = Integer.parseInt(s);
          if (isInteractive)
            System.out.println("Using specified " + port + " port number");
        }
        catch (NumberFormatException e)
        {
          System.out.println("Bad port number (" + e + "), using default "
              + JmxConstants.DEFAULT_JMX_RMI_PORT + " port number");
          port = JmxConstants.DEFAULT_JMX_RMI_PORT;
        }
    }
    else
    {
      port = JmxConstants.DEFAULT_JMX_RMI_PORT;
    }

    // Handle --secret and --username options
    RmiJmxClient jmxClient = null;
    if (commandLine.hasOption('u') && commandLine.hasOption('s'))
    {
      String username = commandLine.getOptionValue('u');
      String password = commandLine.getOptionValue('s');
      jmxClient = new RmiJmxClient("" + port, ip, username, password);
    }
    else
    {
      try
      {
        jmxClient = new RmiJmxClient("" + port, ip, null);
      }
      catch (Exception e)
      {
        System.out.println("Cannot connect to the JMX server");
        System.exit(1);
      }
    }

    // Handle --debug option
    boolean debug = commandLine.hasOption('d');

    // Launch the console (handle --text and --file options)
    Console console;
    InputStream in = null;
    if (commandLine.hasOption('f'))
    {
      String filename = commandLine.getOptionValue('f');

      if ("-".equals(filename))
      {
        in = System.in;
      }
      else
      {
        try
        {
          in = new FileInputStream(filename);
        }
        catch (FileNotFoundException e)
        {
          System.err.println("Failed to open file '" + filename + "' (" + e
              + ")");
          System.exit(1);
        }
      }
      System.out
          .println("Launching the C-JDBC controller console in non interactive mode");
    }
    else
    {
      System.out.println("Launching the C-JDBC controller console");
      in = System.in;
    }

    console = new Console(jmxClient, in, isInteractive, debug);
    console.setPrintColor(!commandLine.hasOption('n'));
    console.handlePrompt();
    System.exit(0);
  }

  /**
   * Creates <code>Options</code> object that contains all available options
   * that can be used launching C-JDBC console.
   * 
   * @return an <code>Options</code> instance
   */
  private static Options createOptions()
  {
    Options options = new Options();
    OptionGroup group = new OptionGroup();

    // help, verbose, text only console and file options (mutually exclusive
    // options)
    group.addOption(new Option("h", "help", false,
        "Displays usage information."));
    group.addOption(new Option("t", "text", false, "Start text console."));
    group.addOption(new Option("v", "version", false,
        "Displays version information."));
    group
        .addOption(new Option(
            "f",
            "file",
            true,
            "Use a given file as the source of commands instead of reading commands interactively."));
    options.addOptionGroup(group);

    // controller ip option
    String defaultIp = ControllerConstants.DEFAULT_IP;
    options
        .addOption(new Option(
            "i",
            "ip",
            true,
            "IP address of the host name where the JMX server hosting the controller is running (the default is '"
                + defaultIp + "')."));

    // controller port option
    options.addOption(new Option("p", "port", true,
        "JMX/RMI port number of (the default is "
            + JmxConstants.DEFAULT_JMX_RMI_PORT + ")."));

    // JMX options
    options.addOption(new Option("u", "username", true,
        "Username for JMX connection."));
    options.addOption(new Option("s", "secret", true,
        "Password for JMX connection."));

    options.addOption(new Option("d", "debug", false,
        "Show stack trace when error occurs."));

    options.addOption(new Option("n", "nocolor", false,
        "Do not print colors in interactive mode for supported systems."));

    return options;
  }

  /**
   * Displays usage message.
   * 
   * @param options available command line options
   */
  private static void printUsage(Options options)
  {
    String header = "Launchs the C-JDBC controller console."
        + System.getProperty("line.separator") + "Options:";

    (new HelpFormatter()).printHelp(80, "console(.sh|.bat) [options]", header,
        options, "");
  }

}