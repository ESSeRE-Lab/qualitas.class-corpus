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
 * Contributor(s): Julie Marguerite, Mathieu Peltier.
 */

package org.objectweb.cjdbc.requestplayer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.objectweb.cjdbc.common.util.Stats;

/**
 * C-JDBC client emulator. Reads SQL requests in a file and forwards them to the
 * controller.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:julie.marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:mathieu.peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public class ClientEmulator
{
  /** Major version. */
  public static final int         MAJOR_VERSION      = 1;

  /** Minor version. */
  public static final int         MINOR_VERSION      = 0;

  /** Zero value. */
  private static final Integer    ZERO               = new Integer(0);

  /** Database URL. */
  private String                  propUrl;

  /** Database login. */
  private String                  propUsername;

  /** Database password. */
  private String                  propPassword;

  /** Access to the properties file. */
  private RequestPlayerProperties requestPlayerProp  = null;

  /** To read the SQL requests in the file. */
  protected BufferedReader        sqlTrace           = null;

  /** Statistics concerning the <code>SELECT</code> requests. */
  protected Stats                 selectStats        = new Stats("Select");

  /** Statistics concerning the unknown requests. */
  protected Stats                 unknownStats       = new Stats("Unknown");

  /** Statistics concerning the <code>UPDATE</code> requests. */
  protected Stats                 updateStats        = new Stats("Update");

  /** Statistics concerning the <code>INSERT</code> requests. */
  protected Stats                 insertStats        = new Stats("Insert");

  /** Statistics concerning the <code>DELETE</code> requests. */
  protected Stats                 deleteStats        = new Stats("Delete");

  /** Statistics concerning transaction begin. */
  protected Stats                 beginStats         = new Stats("Begin");

  /** Statistics concerning transaction commit. */
  protected Stats                 commitStats        = new Stats("Commit");

  /** Statistics concerning transaction rollback. */
  protected Stats                 rollbackStats      = new Stats("Rollback");

  /** Statistics about get connection from driver */
  protected Stats                 getConnectionStats = new Stats(
                                                         "Get connection from driver");

  /** Statistics about closing a connection */
  protected Stats                 closeStats         = new Stats(
                                                         "Close connection");

  /** Statistics about getting request from the log file */
  protected Stats                 getRequestStats    = new Stats(
                                                         "Get requests from log file");

  /** Number of requests. */
  private int                     nbRequests         = 0;

  /** Max number of requests. */
  private int                     maxRequests        = 0;

  /** Transaction id list. */
  private Hashtable               tidList            = new Hashtable();

  private HashSet                 ignoredTids        = new HashSet();

  /** Query timeout. */
  private int                     timeout;

  /** Type of connection management: standard, optimized or pooling. */
  private int                     connectionType;

  /** Stack of available connections (pool). */
  private Stack                   freeConnections    = null;

  /** Connection pool size. */
  private int                     poolSize;

  /** Transaction id. */
  private Integer                 transactionId;

  /**
   * Creates a new <code>ClientEmulator</code> instance. The program is
   * stopped on any error reading the configuration files.
   * 
   * @param configFile configuration file to used.
   */
  public ClientEmulator(String configFile)
  {
    // Initialization, check that all files are ok
    requestPlayerProp = new RequestPlayerProperties(configFile);
    if (!requestPlayerProp.checkPropertiesFile())
      Runtime.getRuntime().exit(1);

    propUrl = requestPlayerProp.getDatabaseURL();
    propUsername = requestPlayerProp.getDatabaseUsername();
    propPassword = requestPlayerProp.getDatabasePassword();

    // Load the cjdbc driver
    try
    {
      Class.forName(requestPlayerProp.getDatabaseDriver());
    }
    catch (Exception e)
    {
      System.out.println("Unable to load database driver '"
          + requestPlayerProp.getDatabaseDriver() + "' (" + e + ")");
      Runtime.getRuntime().exit(1);
    }

    connectionType = requestPlayerProp.getConnectionType();
    if (connectionType == RequestPlayerProperties.POOLING_CONNECTION)
    {
      poolSize = requestPlayerProp.getPoolSize();
      if (poolSize <= 0)
      {
        System.out.println("Connections pool size must be greater than 0.");
        Runtime.getRuntime().exit(1);
      }
      freeConnections = new Stack();
      initializeConnections();
    }

    int nbClients = requestPlayerProp.getNbClients();
    if (nbClients <= 0)
    {
      System.out.println("Number of clients must be greater than 0.");
      Runtime.getRuntime().exit(1);
    }

    timeout = requestPlayerProp.getTimeout();

    try
    {
      String fileName = requestPlayerProp.getTraceFile();
      sqlTrace = new BufferedReader(new FileReader(fileName));
    }
    catch (Exception e)
    {
      System.out.println("An error occured while opening trace file ("
          + e.getMessage() + ")");
      Runtime.getRuntime().exit(1);
    }

    maxRequests = requestPlayerProp.getNbRequests();

    System.out.println("Creating " + nbClients + " threads.");
    ClientThread[] threads = new ClientThread[nbClients];
    for (int i = 0; i < nbClients; i++)
      threads[i] = new ClientThread(i, this, connectionType);

    MonitoringThread monitor = new MonitoringThread(this, 60000);
    // Display stats every minute
    monitor.start();

    System.out.println("Starting threads.");
    long start = System.currentTimeMillis();
    for (int i = 0; i < nbClients; i++)
      threads[i].start();
    System.out.println("Done.");

    for (int i = 0; i < nbClients; i++)
    {
      try
      {
        threads[i].join();
      }
      catch (java.lang.InterruptedException ie)
      {
        System.err.println("ClientEmulator: Thread " + i
            + " has been interrupted.");
      }
    }
    long end = System.currentTimeMillis();
    monitor.setKilled(true);
    monitor.interrupt();
    System.out.println("Done\n");

    try
    {
      sqlTrace.close();
    }
    catch (Exception ignore)
    {
    }

    if (connectionType == RequestPlayerProperties.POOLING_CONNECTION)
    {
      try
      {
        finalizeConnections();
      }
      catch (SQLException e)
      {
        System.out.println("Failed to release connections from the pool.");
      }
    }

    // Merge and display the stats
    Stats globalStats = new Stats("Global");
    try
    {
      globalStats.merge(selectStats);
      globalStats.merge(insertStats);
      globalStats.merge(updateStats);
      globalStats.merge(deleteStats);
      globalStats.merge(getConnectionStats);
      globalStats.merge(beginStats);
      globalStats.merge(commitStats);
      globalStats.merge(rollbackStats);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    //For single line output
    //System.out.println("Name Count Error Hits %Hits Min Max Avg Total
    // Thoughput");
    getRequestStats.displayOnStdout();
    getConnectionStats.displayOnStdout();
    closeStats.displayOnStdout();
    beginStats.displayOnStdout();
    commitStats.displayOnStdout();
    rollbackStats.displayOnStdout();
    selectStats.displayOnStdout();
    insertStats.displayOnStdout();
    updateStats.displayOnStdout();
    deleteStats.displayOnStdout();
    globalStats.displayOnStdout();
    System.out.println("\nTotal execution time: " + (end - start) + " ms");
    if (end - start != 0)
    {
      System.out.println("Average requests/second: "
          + (globalStats.getCount() * 1000.0 / (end - start)));
      System.out.println("Average requests/minute: "
          + (globalStats.getCount() * 60000.0 / (end - start)));
    }
  }

  /**
   * Gets a new connection from the driver.
   * 
   * @return a connection
   */
  public Connection getConnection()
  {
    // Connect to the database
    try
    {
      return DriverManager.getConnection(propUrl, propUsername, propPassword);
    }
    catch (Exception e)
    {
      System.out.println("Unable to connect to database '"
          + requestPlayerProp.getDatabaseURL() + "' (" + e + ")");
    }
    return null;
  }

  /**
   * Initializes the pool of connections to the database. The caller must ensure
   * that the driver has already been loaded else an exception will be thrown.
   */
  public synchronized void initializeConnections()
  {
    for (int i = 0; i < poolSize; i++)
    {
      // Get connections to the database
      freeConnections.push(getConnection());
    }
  }

  /**
   * Closes a given connection.
   * 
   * @param connection connection to close
   */
  public void closeConnection(Connection connection)
  {
    try
    {
      if (connection != null)
        connection.close();
    }
    catch (Exception e)
    {
      System.out.println("Failed to close the connection (" + e + ")");
    }
  }

  /**
   * Gets a connection from the pool (round-robin).
   * 
   * @return a <code>Connection</code> instance or <code>null</code> if no
   *         connection is available
   */
  public synchronized Connection getConnectionFromPool()
  {
    try
    {
      // Wait for a connection to be available
      while (freeConnections.isEmpty())
      {
        try
        {
          wait();
        }
        catch (InterruptedException e)
        {
          System.out.println("Connection pool wait interrupted.");
        }
      }

      Connection c = (Connection) freeConnections.pop();
      return c;
    }
    catch (EmptyStackException e)
    {
      System.out.println("Out of connections.");
      return null;
    }
  }

  /**
   * Releases a connection to the pool.
   * 
   * @param connection the connection to release
   */
  public synchronized void releaseConnectionToPool(Connection connection)
  {
    boolean mustNotify = freeConnections.isEmpty();

    freeConnections.push(connection);

    // Wake up one thread waiting for a connection (if any)
    if (mustNotify)
      notify();
  }

  /**
   * Releases all the connections to the database.
   * 
   * @exception SQLException if an error occurs
   */
  public synchronized void finalizeConnections() throws SQLException
  {
    Connection c = null;
    while (!freeConnections.isEmpty())
    {
      c = (Connection) freeConnections.pop();
      c.close();
    }
  }

  /**
   * Gets the next SQL request from the trace file. Requests are executed in
   * parallel for each separate transaction.
   * 
   * @param tid transaction id
   * @return a <code>String</code> containing the SQL request or
   *         <code>null</code> if no more requests are available (end of file
   *         or maximum number of requests reached)
   */
  public synchronized String parallelGetNextSQLRequest(int tid)
  {
    // Check if we have already stored requests corresponding to tid
    ArrayList req = (ArrayList) tidList.get(new Integer(tid));
    if (req != null)
    {
      String request = (String) req.remove(0);
      if (req.isEmpty())
        tidList.remove(new Integer(tid));

      nbRequests++;
      return request;
    }

    // We don't have anything ready, let's read the input file
    while ((nbRequests <= maxRequests) || (maxRequests == 0))
    {
      String request = readRequest();

      if ((request == null) || (transactionId == null)) // Should always be null
        // together
        return null;

      // Does this entry match the requested tid ?
      if (transactionId.intValue() == tid)
      {
        nbRequests++;
        return request; // Yes
      }
      else
      { // No, store this request
        ArrayList requests = (ArrayList) tidList.get(transactionId);
        if (requests == null)
        {
          requests = new ArrayList();
          tidList.put(transactionId, requests);
        }
        requests.add(request);
      }
    }
    return null;
  }

  /**
   * Ignores all requests belonging to a specific transaction id. Only used for
   * sequential request execution.
   * 
   * @param tid the tid to ignore
   */
  public void ignoreTid(int tid)
  {
    ignoredTids.add(new Integer(tid));
  }

  /**
   * Gets the next SQL request from the trace file. If the current request does
   * not match the request transaction id, the current thread waits until the
   * current request transaction id matches the requested one.
   * 
   * @param tid transaction id
   * @return a <code>String</code> containing the SQL request or
   *         <code>null</code> if no more requests are available (end of file
   *         or maximum number of requests reached)
   */
  public synchronized String sequentialGetNextSQLRequest(int tid)
  {
    String request;
    do
    {
      request = readRequest();

      if (request == null) // This is the end, wake up everybody
      {
        notifyAll();
        return null;
      }
    }
    while (ignoredTids.contains(transactionId));

    // Does this entry match the requested tid ?
    if (transactionId.intValue() == tid)
      return request; // Yes
    else
    {
      notifyAll(); // Wake up the others
      while (request != null)
      {
        try
        {
          wait(1000);
        }
        catch (InterruptedException e)
        {
          System.err.println("sequentialGetNextSQLRequest wait interrupted");
        }
        // Is the current request for us ?
        if (transactionId != null)
          if (transactionId.intValue() != tid)
          {
            String myRequest = request;
            request = readRequest(); // fetch the next request
            notifyAll();
            return myRequest;
          }
      }
      notifyAll();
      return null;
    }
  }

  /**
   * Must be called from a synchronized statement.
   */
  private String readRequest()
  {
    String request = null;
    transactionId = null;

    try
    {
      if ((nbRequests <= maxRequests) || (maxRequests == 0))
        if ((request = sqlTrace.readLine()) != null)
        {
          // Expected format is: date vdbName requestType transactionId SQL
          nbRequests++;

          StringTokenizer requestTokenizer = new StringTokenizer(request, " ");
          //          String date = requestTokenizer.nextToken().trim();
          requestTokenizer.nextToken();
          //          String virtualDatabase = requestTokenizer.nextToken().trim();
          requestTokenizer.nextToken();
          String type = requestTokenizer.nextToken().trim();
          transactionId = new Integer(requestTokenizer.nextToken().trim());

          String sql;
          switch (type.charAt(0))
          {
            case 'B' :
              // Begin
              sql = "B " + transactionId;
              transactionId = ZERO;
              break;
            case 'C' :
            // Commit
            case 'R' :
              // Rollback
              sql = type;
              break;
            default :
              // return type+" "+SQL
              sql = type
                  + " "
                  + request.substring(
                      request.indexOf(" " + transactionId.toString() + " ")
                          + transactionId.toString().length() + 1).trim();
              break;
          }
          return sql.toString();
        }
    }
    catch (IOException e)
    {
    }
    return null;
  }

  /**
   * Returns the <code>DELETE</code> requests statictics.
   * 
   * @return a <code>Stats</code> instance.
   */
  public Stats getDeleteStats()
  {
    return deleteStats;
  }

  /**
   * Returns the <code>INSERT</code> requests statictics.
   * 
   * @return a <code>Stats</code> instance.
   */
  public Stats getInsertStats()
  {
    return insertStats;
  }

  /**
   * Returns the <code>SELECT</code> requests statictics.
   * 
   * @return a <code>Stats</code> instance.
   */
  public Stats getSelectStats()
  {
    return selectStats;
  }

  /**
   * Returns the unknown requests statictics.
   * 
   * @return a <code>Stats</code> instance.
   */
  public Stats getUnknownStats()
  {
    return unknownStats;
  }

  /**
   * Returns the <code>UPDATE</code> requests statictics. *
   * 
   * @return a <code>Stats</code> instance.
   */
  public Stats getUpdateStats()
  {
    return updateStats;
  }

  /**
   * Returns the beginStats value.
   * 
   * @return Returns the beginStats.
   */
  public Stats getBeginStats()
  {
    return beginStats;
  }

  /**
   * Returns the closeStats value.
   * 
   * @return Returns the closeStats.
   */
  public Stats getCloseStats()
  {
    return closeStats;
  }

  /**
   * Returns the commitStats value.
   * 
   * @return Returns the commitStats.
   */
  public Stats getCommitStats()
  {
    return commitStats;
  }

  /**
   * Returns the freeConnections value.
   * 
   * @return Returns the freeConnections.
   */
  public Stack getFreeConnections()
  {
    return freeConnections;
  }

  /**
   * Returns the getConnectionStats value.
   * 
   * @return Returns the getConnectionStats.
   */
  public Stats getGetConnectionStats()
  {
    return getConnectionStats;
  }

  /**
   * Returns the getRequestStats value.
   * 
   * @return Returns the getRequestStats.
   */
  public Stats getGetRequestStats()
  {
    return getRequestStats;
  }

  /**
   * Returns the rollbackStats value.
   * 
   * @return Returns the rollbackStats.
   */
  public Stats getRollbackStats()
  {
    return rollbackStats;
  }

  /**
   * Returns the query timeout.
   * 
   * @return <code>int</code> value.
   */
  public int getTimeout()
  {
    return timeout;
  }

  /**
   * Main method. The available options are:
   * <ul>
   * <li><code>-h</code> or <code>--help</code> <code>&lt;port&gt;</code>:
   * displays usage informations.</li>
   * <li><code>-v</code> or <code>--version</code>: displays version
   * informations.</li>
   * <li><code>-f</code> or <code>--file</code>: allows to use a given
   * configuration file instead of the default file.</li>
   * </ul>
   * 
   * @param args command line arguments (see above)
   */
  public static void main(String[] args)
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
      Runtime.getRuntime().exit(1);
    }

    // Non-recognized options
    int n = commandLine.getArgs().length;
    for (int i = 0; i < n; i++)
    {
      System.err.println("Syntax error (unrecognized option: "
          + commandLine.getArgs()[i] + ")");
      printUsage(options);
      Runtime.getRuntime().exit(1);
    }

    // Handle --help option
    if (commandLine.hasOption('h'))
    {
      if (commandLine.getOptions().length > 1)
        System.err.println("Syntax error");

      printUsage(options);
      Runtime.getRuntime().exit(1);
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
        System.out.println("C-JDBC request player version " + MAJOR_VERSION
            + "." + MINOR_VERSION);

      Runtime.getRuntime().exit(1);
    }

    // Handle -f option
    if (commandLine.hasOption('f'))
    {
      if (commandLine.getOptions().length > 1)
      {
        System.err.println("Syntax error");
        printUsage(options);
      }
      new ClientEmulator(commandLine.getOptionValue("f"));
    }
    else
    {
      new ClientEmulator(null);
    }
  }

  /**
   * Creates <code>Options</code> object that contains all available options
   * that can be used launching C-JDBC request player.
   * 
   * @return an <code>Options</code> instance
   */
  private static Options createOptions()
  {
    Options options = new Options();
    OptionGroup group = new OptionGroup();

    // help and verbose options
    group.addOption(new Option("h", "help", false,
        "Displays usage information."));
    group.addOption(new Option("v", "version", false,
        "Displays version information."));
    options.addOptionGroup(group);

    // file option
    options
        .addOption(new Option("f", "file", true,
            "Allows to use a given configuration file instead of the default file."));
    return options;
  }

  /**
   * Displays usage message.
   * 
   * @param options available command line options
   */
  private static void printUsage(Options options)
  {
    String header = "Launchs the C-JDBC request player: this tool reads SQL requests in a file and forwards them to the C-JDBC controller(s)."
        + System.getProperty("line.separator") + "Options:";
    String footer = "Statistics are displayed after the execution has finished. For more information, see the default configuration file contained in the <C_JDBC_HOME>/config/ directory.";

    (new HelpFormatter()).printHelp(80, "requestplayer(.sh|.bat) [options]",
        header, options, footer);
  }
}