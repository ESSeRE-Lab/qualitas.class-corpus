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

package org.objectweb.cjdbc.console.text.module;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.FileNameCompletor;
import jline.SimpleCompletor;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.console.text.Console;
import org.objectweb.cjdbc.console.text.commands.ConsoleCommand;
import org.objectweb.cjdbc.console.text.commands.Help;
import org.objectweb.cjdbc.console.text.commands.History;
import org.objectweb.cjdbc.console.text.commands.Quit;

/**
 * This class defines a AbstractConsoleModule
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public abstract class AbstractConsoleModule
{
  private static final int HISTORY_MAX = 10;

  Console                  console;

  TreeSet                  commands;

  boolean                  quit        = false;

  LinkedList               history;

  protected Completor      consoleCompletor;

  /**
   * Creates a new <code>AbstractConsoleModule.java</code> object
   * 
   * @param console to refer from
   */
  public AbstractConsoleModule(Console console)
  {
    this.console = console;
    this.commands = new TreeSet();
    this.history = new LinkedList();
    commands.add(new Help(this));
    commands.add(new History(this));
    commands.add(new Quit(this));
    if (console.isInteractive())
      console.println(ConsoleTranslate.get("module.loading",
          getDescriptionString()));
    this.loadCommands();
    this.loadCompletor();
  }

  /**
   * Loads the commands for this module
   */
  protected abstract void loadCommands();

  /**
   * Loads the commands for this module
   */
  protected void loadCompletor()
  {
    List completors = new LinkedList();
    int size = commands.size();
    if (size > 0)
    {
      TreeSet set = new TreeSet();
      Iterator it = commands.iterator();
      while (it.hasNext())
      {
        set.add(((ConsoleCommand) it.next()).getCommandName());
      }
      completors.add(new SimpleCompletor((String[]) set
          .toArray(new String[size])));
    }
    completors.add(new FileNameCompletor());

    Completor[] completorsArray = (Completor[]) completors
        .toArray(new Completor[completors.size()]);
    consoleCompletor = new ArgumentCompletor(completorsArray,
        new CommandDelimiter());
  }

  /**
   * Reload the completor associated with this module. This method must be
   * called if the list of commands has been dynamically modified.
   */
  protected synchronized void reloadCompletor()
  {
    console.getConsoleReader().removeCompletor(consoleCompletor);
    loadCompletor();
    console.getConsoleReader().addCompletor(consoleCompletor);
  }

  /**
   * Text description of this module
   * 
   * @return <code>String</code> description to display
   */
  public abstract String getDescriptionString();

  /**
   * Display help for this module
   */
  public void help()
  {
    console.println(ConsoleTranslate.get("module.commands.available",
        getDescriptionString()));
    ConsoleCommand command;
    Iterator it = commands.iterator();
    while (it.hasNext())
    {
      command = (ConsoleCommand) it.next();
      console.printInfo(command.getCommandName() + " "
          + command.getCommandParameters());
      console.println("   " + command.getCommandDescription());
    }
  }

  /**
   * Quit this module
   */
  public void quit()
  {
    quit = true;
    storeHistory();
    console.getConsoleReader().removeCompletor(getCompletor());
    console.getConsoleReader().addCompletor(
        console.getControllerModule().getCompletor());
  }

  /**
   * Load History from java Preferences
   */
  protected void loadHistory()
  {
    try
    {
      Preferences prefs = Preferences.userRoot()
          .node(this.getClass().getName());
      String[] historyKeys = prefs.keys();
      Arrays.sort(historyKeys, 0, historyKeys.length);
      for (int i = 0; i < historyKeys.length; i++)
      {
        String key = historyKeys[i];
        String value = prefs.get(key, "");
        manageHistory(value);
      }
    }
    catch (Exception e)
    {
      // unable to load prefs: do nothing
    }
  }

  /**
   * Strore History to java Preferences
   */
  protected void storeHistory()
  {
    try
    {
      Preferences prefs = Preferences.userRoot()
          .node(this.getClass().getName());
      prefs.clear();
      for (int i = 0; i < history.size(); i++)
      {
        prefs.put(String.valueOf(i), (String) history.get(i));
      }
      prefs.flush();
    }
    catch (Exception e)
    {
      // unable to store prefs: do nothing
    }
  }

  /**
   * Get all the commands for this module
   * 
   * @return <code>TreeSet</code> of commands (commandName|commandObject)
   */
  public TreeSet getCommands()
  {
    return commands;
  }

  /**
   * Get the prompt string for this module
   * 
   * @return <code>String</code> to place before prompt
   */
  public abstract String getPromptString();

  /**
   * Handle a serie of commands
   */
  public void handlePrompt()
  {

    loadHistory();
    
    if (quit)
    {
      if (console.isInteractive())
        console.printError(ConsoleTranslate.get("module.quitting",
            getDescriptionString()));
      return;
    }

    // login();
    quit = false;
    while (!quit)
    {
      
      Hashtable hashCommands = getHashCommands();
      try
      {
        String commandLine = console.readLine(getPromptString());
        if (commandLine == null)
        {
          quit = true;
          break;
        }
        if (commandLine.equals(""))
          continue;
        else
          manageHistory(commandLine);

        handleCommandLine(commandLine, hashCommands);

      }
      catch (Exception e)
      {
        console.printError(ConsoleTranslate.get("module.command.got.error",
            new Object[]{e.getClass(), e.getMessage()}), e);
      }
    }
  }

  /**
   * Get the list of commands as strings for this module
   * 
   * @return <code>Hashtable</code> list of <code>String</code> objects
   */
  public final Hashtable getHashCommands()
  {
    Hashtable hashCommands = new Hashtable();
    ConsoleCommand consoleCommand;
    Iterator it = commands.iterator();
    while (it.hasNext())
    {
      consoleCommand = (ConsoleCommand) it.next();
      hashCommands.put(consoleCommand.getCommandName(), consoleCommand);
    }
    return hashCommands;
  }

  /**
   * Handle module command
   * 
   * @param commandLine the command line to handle
   * @param hashCommands the list of commands available for this module
   * @throws Exception if fails *
   */
  public final void handleCommandLine(String commandLine, Hashtable hashCommands)
      throws Exception
  {
    StringTokenizer st = new StringTokenizer(commandLine);
    if (!st.hasMoreTokens())
    {
      console.printError(ConsoleTranslate.get("module.command.not.supported",
          ""));
      return;
    }

    ConsoleCommand consoleCommand = findConsoleCommand(commandLine,
        hashCommands);
    if (consoleCommand == null)
    {
      console.printError(ConsoleTranslate.get("module.command.not.supported",
          commandLine));
    }
    else
    {
      consoleCommand.execute(commandLine.substring(consoleCommand
          .getCommandName().length()));
    }
  }

  /**
   * Find the <code>ConsoleCommand</code> based on the name of the command
   * from the <code>commandLine</code> in the <code>hashCommands</code>.
   * 
   * If more than one <code>ConsoleCommand</code>'s command name start the 
   * same way, return the <code>ConsoleCommand</code> with the longest one.
   *
   * @param commandLine the command line to handle
   * @param hashCommands the list of commands available for this module
   * @return the <code>ConsoleCommand</code> corresponding to the name of the
   *         command from the <code>commandLine</code> or <code>null</code>
   *         if there is no matching
   */
  public ConsoleCommand findConsoleCommand(String commandLine,
      Hashtable hashCommands)
  {
    ConsoleCommand foundCommand = null;
    for (Iterator iter = hashCommands.entrySet().iterator(); iter.hasNext();)
    {
      Map.Entry commandEntry = (Map.Entry) iter.next();
      String commandName = (String) commandEntry.getKey();
      if (commandLine.startsWith(commandName))
      {
        ConsoleCommand command = (ConsoleCommand) commandEntry.getValue();
        if (foundCommand == null) 
        {
          foundCommand = command;
        }
        if (command.getCommandName().length() > foundCommand.getCommandName().length()) 
        {
          foundCommand = command;
        }
      }
    }
    return foundCommand;
  }

  /**
   * Add the command to the history. Removes the first item in the list if the
   * history is too large.
   * 
   * @param command taken from the command line
   */
  public final void manageHistory(String command)
  {
    history.add(command);
    if (history.size() > HISTORY_MAX)
      history.removeFirst();
  }

  /**
   * Handles login in this module
   * 
   * @param params parameters to use to login in this module
   * @throws Exception if fails
   */
  public abstract void login(String[] params) throws Exception;

  /**
   * Get access to the console
   * 
   * @return <code>Console</code> instance
   */
  public Console getConsole()
  {
    return console;
  }

  /**
   * Returns the history value.
   * 
   * @return Returns the history.
   */
  public LinkedList getHistory()
  {
    return history;
  }

  /**
   * Returns the console completor to use for this module.
   * 
   * @return <code>Completor</code> object.
   */
  public Completor getCompletor()
  {
    return consoleCompletor;
  }

  /**
   * This class defines a CommandDelimiter used to delimit a command from user
   * input
   */
  class CommandDelimiter extends ArgumentCompletor.AbstractArgumentDelimiter
  {
    /**
     * @see jline.ArgumentCompletor$AbstractArgumentDelimiter#isDelimiterChar(java.lang.String,
     *      int)
     */
    public boolean isDelimiterChar(String buffer, int pos)
    {
      String tentativeCmd = buffer.substring(0, pos);
      return isACompleteCommand(tentativeCmd);
    }

    /**
     * Test if the String input by the user insofar is a complete command or
     * not.
     * 
     * @param input Text input by the user
     * @return <code>true</code> if the text input by the user is a complete
     *         command name, <code>false</code> else
     */
    private boolean isACompleteCommand(String input)
    {
      boolean foundCompleteCommand = false;
      for (Iterator iter = commands.iterator(); iter.hasNext();)
      {
        ConsoleCommand command = (ConsoleCommand) iter.next();
        if (input.equals(command.getCommandName()))
        {
          foundCompleteCommand = !otherCommandsStartWith(command
              .getCommandName());
        }
      }
      return foundCompleteCommand;
    }

    private boolean otherCommandsStartWith(String commandName)
    {
      for (Iterator iter = commands.iterator(); iter.hasNext();)
      {
        ConsoleCommand command = (ConsoleCommand) iter.next();
        if (command.getCommandName().startsWith(commandName)
            && !command.getCommandName().equals(commandName))
        {
          return true;
        }
      }
      return false;
    };
  };
}