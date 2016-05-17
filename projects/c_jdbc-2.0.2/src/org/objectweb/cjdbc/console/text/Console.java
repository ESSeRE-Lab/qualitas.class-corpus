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
 * Initial developer(s): Emmanuel Cecchet. 
 * Contributor(s): Mathieu Peltier,Nicolas Modrzyk
 */

package org.objectweb.cjdbc.console.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.prefs.Preferences;

import jline.ConsoleReader;
import jline.History;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.jmx.RmiJmxClient;
import org.objectweb.cjdbc.console.text.module.ControllerConsole;
import org.objectweb.cjdbc.console.text.module.MonitorConsole;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseConsole;
import org.objectweb.cjdbc.console.views.InfoViewer;

/**
 * This is the C-JDBC controller console that allows remote administration and
 * monitoring of any C-JDBC controller.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Console
{
  private static final Character PASSWORD_CHAR = new Character('\u0000');

  /** <code>ConsoleReader</code> allowing to reading input. */
  private ConsoleReader          consoleReader;

  /** <code>true</code> if the console is used in interactive mode. */
  private boolean                interactive;

  private RmiJmxClient           jmxClient;

  /** Virtual database administration console. */
  private VirtualDatabaseAdmin   adminModule;

  /** Monitoring console */
  private MonitorConsole         monitorModule;

  /** Virtual database console. */
  private VirtualDatabaseConsole consoleModule;

  /** Controller Console */
  private ControllerConsole      controllerModule;

  /** Debug Mode */
  private boolean                debug;

  /**
   * <code>true</code> if colors should be displayed in interactive mode (work
   * only on non Windows system).
   */
  private boolean                printColor;

  /**
   * Creates a new <code>Console</code> instance.
   * 
   * @param jmxClient to connect to the jmxServer
   * @param in the input stream to get the command from
   * @param interactive if set to <code>true</code> will display prompt
   * @param debug <code>true</code> if debug mode should be activated.
   */
  public Console(RmiJmxClient jmxClient, InputStream in, boolean interactive,
      boolean debug)
  {
    try
    {
      consoleReader = new ConsoleReader(in, new PrintWriter(System.out));
    }
    catch (IOException e)
    {
      System.err.println("Unable to create console: " + e.toString());
    }
    this.interactive = interactive;
    this.jmxClient = jmxClient;
    this.debug = debug;

    controllerModule = new ControllerConsole(this);
    adminModule = new VirtualDatabaseAdmin(this);
    monitorModule = new MonitorConsole(this);
    consoleModule = new VirtualDatabaseConsole(this);
    setPrintColor(true);
    consoleReader.addCompletor(controllerModule.getCompletor());
    consoleReader.setHistory(loadJLineHistory());
  }

  private History loadJLineHistory()
  {
    jline.History jHistory = new jline.History();
    try
    {
      Preferences prefs = Preferences.userRoot()
          .node(ControllerConsole.class.getName());
      String[] historyKeys = prefs.keys();
      Arrays.sort(historyKeys, 0, historyKeys.length);
      for (int i = 0; i < historyKeys.length; i++)
      {
        String key = historyKeys[i];
        String value = prefs.get(key, "");
        jHistory.addToHistory(value);
      }
    }
    catch (Exception e)
    {
      // unable to load prefs: do nothing
    }
    return jHistory;
  }

  /**
   * Should this console display color in interactive mode? Warning, colors only
   * work on non Windows system.
   * 
   * @param b <code>true</code> if the console should display color (ignored
   *          on Windows system).
   */
  public void setPrintColor(boolean b)
  {
    String os = System.getProperty("os.name").toLowerCase();
    boolean windows = os.indexOf("nt") > -1 || os.indexOf("windows") > -1;
    if (windows)
      printColor = false;
    else
      printColor = b;
    
    if (System.getProperty("cjdbc.console.nocolor") != null)
      printColor = false;
  }

  /**
   * Returns the interactive value.
   * 
   * @return Returns the interactive.
   */
  public boolean isInteractive()
  {
    return interactive;
  }

  /**
   * Main menu prompt handling.
   */
  public void handlePrompt()
  {
    controllerModule.handlePrompt();
  }

  /**
   * @see Console#readLine(java.lang.String)
   */
  public String readLine(String prompt) throws ConsoleException
  {
    String line = "";
    try
    {
      if (interactive)
      {
        prompt += " > ";
        if (printColor)
        {
          prompt = ColorPrinter.getColoredMessage(prompt, ColorPrinter.PROMPT);
        }
        line = consoleReader.readLine(prompt);
      }
      else
      {
        line = consoleReader.readLine();
      }
    }
    catch (IOException e)
    {
      throw new ConsoleException(ConsoleTranslate.get(
          "console.read.command.failed", e));
    }
    if (line != null)
      line = line.trim();
    return line;
  }

  /**
   * @see Console#readPassword(java.lang.String)
   */
  public String readPassword(String prompt) throws ConsoleException
  {
    String password;
    try
    {
      if (interactive)
      {
        prompt += " > ";
        if (printColor)
        {
          prompt = ColorPrinter.getColoredMessage(prompt, ColorPrinter.PROMPT);
        }
        password = consoleReader.readLine(prompt, PASSWORD_CHAR);
      }
      else
      {
        password = consoleReader.readLine(PASSWORD_CHAR);
      }
    }
    catch (IOException e)
    {
      throw new ConsoleException(ConsoleTranslate.get(
          "console.read.password.failed", e));
    }
    return password;
  }

  /**
   * @see Console#print(java.lang.String)
   */
  public void print(String s)
  {
    System.out.print(s);
  }

  /**
   * @see Console#print(java.lang.String)
   */
  public void print(String s, int color)
  {
    if (printColor)
      ColorPrinter.printMessage(s, System.out, color, false);
    else
    System.out.print(s);
  }

  /**
   * @see Console#println(java.lang.String)
   */
  public void println(String s)
  {
    System.out.println(s);
  }

  /**
   * Print in color
   * 
   * @param s the message to display
   * @param color the color to use
   */
  public void println(String s, int color)
  {
    if (printColor)
      ColorPrinter.printMessage(s, System.out, color);
    else
      System.out.println(s);
  }

  /**
   * @see Console#println()
   */
  public void println()
  {
    System.out.println();
  }

  /**
   * @see Console#printError(java.lang.String)
   */
  public void printError(String message)
  {
    if (printColor)
      ColorPrinter.printMessage(message, System.err, ColorPrinter.ERROR);
    else
      System.err.println(message);
  }

  /**
   * @see Console#println()
   */
  public void printInfo(String message)
  {
    println(message, ColorPrinter.INFO);
  }

  /**
   * Display an error and stack trace if in debug mode.
   * 
   * @param message error message
   * @param e exception that causes the error
   */
  public void printError(String message, Exception e)
  {
    if (debug)
      e.printStackTrace();
    printError(message);
  }

  /**
   * Show a table of info in a formatted way
   * 
   * @param info the data to display
   * @param viewer the viewer to use to get the name of the columns and the
   *          format of data
   */
  public void showInfo(String[][] info, InfoViewer viewer)
  {
    if (printColor)
      println(viewer.displayText(info), ColorPrinter.STATUS);
    else
      System.out.println(viewer.displayText(info));
  }

  /**
   * Returns the jmxClient value.
   * 
   * @return Returns the jmxClient.
   */
  public RmiJmxClient getJmxClient()
  {
    return jmxClient;
  }

  /**
   * Returns the adminModule value.
   * 
   * @return Returns the adminModule.
   */
  public VirtualDatabaseAdmin getAdminModule()
  {
    return adminModule;
  }

  /**
   * Returns the consoleModule value.
   * 
   * @return Returns the consoleModule.
   */
  public VirtualDatabaseConsole getConsoleModule()
  {
    return consoleModule;
  }

  /**
   * Returns the controllerModule value.
   * 
   * @return Returns the controllerModule.
   */
  public ControllerConsole getControllerModule()
  {
    return controllerModule;
  }

  /**
   * Returns the monitorModule value.
   * 
   * @return Returns the monitorModule.
   */
  public MonitorConsole getMonitorModule()
  {
    return monitorModule;
  }

  /**
   * Returns the consoleReader value.
   * 
   * @return Returns the consoleReader.
   */
  public final ConsoleReader getConsoleReader()
  {
    return consoleReader;
  }
}