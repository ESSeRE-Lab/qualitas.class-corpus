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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Mathieu Peltier, Nicolas Modrzyk.
 */

package org.objectweb.cjdbc.console.text.module;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Hashtable;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.console.text.Console;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.commands.ConsoleCommand;
import org.objectweb.cjdbc.console.text.commands.Help;
import org.objectweb.cjdbc.console.text.commands.History;
import org.objectweb.cjdbc.console.text.commands.Quit;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.Begin;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.CallStoredProcedure;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.Commit;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.Load;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.Rollback;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.SetFetchSize;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.SetIsolation;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.SetMaxRows;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.SetSavePoint;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.SetTimeout;
import org.objectweb.cjdbc.console.text.commands.sqlconsole.ShowTables;
import org.objectweb.cjdbc.console.text.formatter.ResultSetFormatter;

/**
 * C-JDBC Controller Virtual Database Console module.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class VirtualDatabaseConsole extends AbstractConsoleModule
{
  private Connection connection = null;
  /** contains a hash of <String, SavePoint>
   * to handle savepoints (used by SetSavePoint and Rollback
   * commands)
   */
  private Hashtable  savePoints  = new Hashtable();

  /** Default query timeout. */
  private int        timeout    = 60;

  private int        fetchsize  = 0;

  private int        maxrows    = 0;

  private String     login;
  private String     url;

  /**
   * Creates a new <code>VirtualDatabaseAdmin</code> instance. Loads the
   * driver
   * 
   * @param console console console
   */
  public VirtualDatabaseConsole(Console console)
  {
    super(console);
    try
    {
      Class.forName("org.objectweb.cjdbc.driver.Driver");
    }
    catch (Exception e)
    {
      console.printError(ConsoleTranslate
          .get("sql.login.cannot.load.driver", e), e);
      Runtime.getRuntime().exit(1);
    }
    if (console.isInteractive())
      console.println(ConsoleTranslate.get("sql.login.loaded.driver",
          Constants.VERSION));
  }

  /**
   * Get the JDBC connection used by the sql console.
   * 
   * @return a JDBC connection
   */
  public Connection getConnection()
  {
    return connection;
  }

  /**
   * Create a new connection from the driver.
   * 
   * @param url the C-JDBC url
   * @param login the login to use to open the connection
   * @param password the password to use to open the connection
   * @return a new connection
   * @throws ConsoleException if login failed
   */
  public Connection createConnection(String url, String login, String password)
      throws ConsoleException
  {
    try
    {
      return DriverManager.getConnection(url, login, password);
    }
    catch (Exception e)
    {
      throw new ConsoleException(ConsoleTranslate.get(
          "sql.login.connection.failed", new String[]{url, e.getMessage()}));
    }
  }

  /**
   * Executes a SQL statement.
   * 
   * @param request the SQL request to execute
   * @param displayResult <code>true</code> if the result must be printed on
   *          the standard output
   */
  public synchronized void execSQL(String request, boolean displayResult)
  {
    PreparedStatement stmt = null;
    try
    {
      stmt = connection.prepareStatement(request);
      stmt.setQueryTimeout(timeout);
      if (fetchsize != 0)
        stmt.setFetchSize(fetchsize);
      if (maxrows != 0)
        stmt.setMaxRows(maxrows);

      long start = System.currentTimeMillis();
      long end;
      if (request.regionMatches(true, 0, "select ", 0, 7))
      {
        ResultSet rs = stmt.executeQuery();
        end = System.currentTimeMillis();
        if (displayResult)
          ResultSetFormatter.formatAndDisplay(rs, fetchsize, console);
      }
      else
      {
        int result = stmt.executeUpdate();
        end = System.currentTimeMillis();
        if (displayResult)
          console.println(ConsoleTranslate.get("sql.display.affected.rows",
              result));
      }
      console.println(ConsoleTranslate.get("sql.display.query.time",
          new Long[]{new Long((end - start) / 1000),
              new Long((end - start) % 1000)}));
    }
    catch (Exception e)
    {
      console.printError(ConsoleTranslate.get("sql.command.sqlquery.error", e),
          e);
    }
    finally
    {
      try
      {
        stmt.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Connects to a virtual database.
   */
  public void handlePrompt()
  {
    loadHistory();

    while (!quit)
    {
      try
      {
        String cmd = console.readLine(this.getPromptString());
        if (cmd == null)
          cmd = "";

        if (cmd.length() == 0)
          continue;

        manageHistory(cmd);

        ConsoleCommand currentCommand = findConsoleCommand(cmd,
            getHashCommands());

        if (currentCommand == null)
        {
          execSQL(cmd, true);
        }
        else
        {
          currentCommand.execute(cmd.substring(
              currentCommand.getCommandName().length()).trim());
        }

      }
      catch (Exception e)
      {
        console.printError(ConsoleTranslate.get("sql.display.exception", e), e);
        if (e instanceof RuntimeException)
        {
          System.exit(0);
        }
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#getDescriptionString()
   */
  public String getDescriptionString()
  {
    return "SQL Console";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#getPromptString()
   */
  public String getPromptString()
  {
    int ind1 = url.lastIndexOf('?');
    int ind2 = url.lastIndexOf(';');
    if (ind1 != -1 || ind2 != -1)
    {
      String prompt;
      prompt = (ind1 != -1) ? url.substring(0, ind1) : url;
      prompt = (ind2 != -1) ? url.substring(0, ind2) : url;
      return prompt + " (" + login + ")";
    }
    else
      return url + " (" + login + ")";
  }
  
  /**
   * Get a <code>SavePoint</code> identified by its <code>name</code>
   * 
   * @param name name fo the <code>SavePoint</code>
   * @return a <code>SavePoint</code> or <code>null</code> if no <code>SavePoint</code> with
   * such a name has been previously added
   */
  public Savepoint getSavePoint(String name)
  {
    return (Savepoint) savePoints.get(name);
  }
  
  /**
   * add a <code>SavePoint</code>
   * 
   * @param savePoint the <code>SavePoint</code> to add
   * @throws SQLException if the <code>savePoint</code> is unnamed
   */
  public void addSavePoint(Savepoint savePoint) throws SQLException
  {
    savePoints.put(savePoint.getSavepointName(), savePoint);
  } 

  /**
   * Get the timeout value (in seconds)
   * 
   * @return the timeout value (in seconds)
   */
  public int getTimeout()
  {
    return timeout;
  }

  /**
   * Set the timeout value (in seconds)
   * 
   * @param timeout new timeout value (in seconds)
   */
  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  /**
   * Get the fetchsize value
   * 
   * @return the fetchsize value
   */
  public int getFetchsize()
  {
    return fetchsize;
  }

  /**
   * Set the fetchsize
   * 
   * @param fetchsize new fetchsize value
   */
  public void setFetchsize(int fetchsize)
  {
    this.fetchsize = fetchsize;
  }

  /**
   * Set the maxrows
   * 
   * @param maxrows new maxrows value
   */
  public void setMaxrows(int maxrows)
  {
    this.maxrows = maxrows;
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#loadCommands()
   */
  protected void loadCommands()
  {
    commands.add(new Begin(this));
    commands.add(new CallStoredProcedure(this));
    commands.add(new Commit(this));
    commands.add(new SetFetchSize(this));
    commands.add(new Help(this));
    commands.add(new Load(this));
    commands.add(new SetMaxRows(this));
    commands.add(new SetSavePoint(this));
    commands.add(new Rollback(this));
    commands.add(new SetIsolation(this));
    commands.add(new ShowTables(this));
    commands.add(new SetTimeout(this));
    commands.add(new Quit(this));
    commands.add(new History(this));
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#login(java.lang.String[])
   */
  public void login(String[] params) throws Exception
  {
    login = null;
    url = (params.length > 0 && params[0] != null) ? params[0].trim() : null;
    try
    {
      if ((url == null) || url.trim().equals(""))
      {
        url = console.readLine(ConsoleTranslate.get("sql.login.prompt.url"));
        if (url == null)
          return;
      }
      login = console.readLine(ConsoleTranslate.get("sql.login.prompt.user"));
      if (login == null)
        return;
      String password = console.readPassword(ConsoleTranslate
          .get("sql.login.prompt.password"));
      if (password == null)
        return;

      connection = createConnection(url, login, password);
      
      // Change console reader completor
      console.getConsoleReader().removeCompletor(
          console.getControllerModule().getCompletor());
      console.getConsoleReader().addCompletor(this.getCompletor());
    }
    catch (Exception e)
    {
      throw new ConsoleException(ConsoleTranslate.get("sql.login.exception", e));
    }
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#quit()
   */
  public void quit()
  {
    if (connection != null)
    {
      try
      {
        connection.close();
      }
      catch (Exception e)
      {
        // ignore
      }
    }
    super.quit();
  }

  /**
   * @see org.objectweb.cjdbc.console.text.module.AbstractConsoleModule#help()
   */
  public void help()
  {
    super.help();
    console.println();
    console.println(ConsoleTranslate.get("sql.command.description"));
  }
}
