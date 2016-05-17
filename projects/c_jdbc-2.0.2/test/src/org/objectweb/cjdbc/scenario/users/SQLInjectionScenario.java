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
 * Initial developer(s):
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.users;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.objectweb.cjdbc.scenario.templates.SQLInjectionTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * Starts multiple thread that execute inserts on backends. This class defines a
 * SQLInjectionScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public final class SQLInjectionScenario extends SQLInjectionTemplate
{

  static final String   LOG_FILE                     = "sqlinjection.txt";

  static final String[] URLS                         = (urls != null)
                                                         ? urls
                                                         : new String[]{
      "jdbc:hsqldb:hsql://localhost:9001", "jdbc:hsqldb:hsql://localhost:9002"};
  static final String[] USERS                        = (users != null)
                                                         ? users
                                                         : new String[]{"test",
      "test"                                             };
  static final String[] PASSWORDS                    = (passwords != null)
                                                         ? passwords
                                                         : new String[]{"", ""};
  static final String[] CLASSES                      = (classes != null)
                                                         ? classes
                                                         : new String[]{
      "org.hsqldb.jdbcDriver", "org.hsqldb.jdbcDriver"   };

  static final String   CJDBC_DRIVER                 = "org.objectweb.cjdbc.driver.Driver";
  static final String   CJDBC_URL                    = (cjdbcurl != null)
                                                         ? cjdbcurl
                                                         : "jdbc:cjdbc://localhost/myDB";
  static final String   CJDBC_USER                   = (cjdbcuser != null)
                                                         ? cjdbcuser
                                                         : "user";
  static final String   CJDBC_PASSWORD               = (cjdbcpassword != null)
                                                         ? cjdbcpassword
                                                         : "";

  static final int      THREAD_COUNT                 = (threadcount != null)
                                                         ? Integer
                                                             .parseInt(threadcount)
                                                         : 400;
  static final int      THREAD_START_WAIT_TIME_RANGE = (threadstartwaittimerange != null)
                                                         ? Integer
                                                             .parseInt(threadstartwaittimerange)
                                                         : 100;
  static final int      UNIT_RUN_COUNT               = (unitruncount != null)
                                                         ? Integer
                                                             .parseInt(unitruncount)
                                                         : 10;
  static final int      THREAD_WAIT_TIME             = (threadwaittime != null)
                                                         ? Integer
                                                             .parseInt(threadwaittime)
                                                         : 50;
  static final int      JOIN_THREAD_TIMEOUT          = (jointhreadtimeout != null)
                                                         ? Integer
                                                             .parseInt(jointhreadtimeout)
                                                         : 100000;

  static final boolean  DROP_TABLES                  = (dropTables != null)
                                                         ? Boolean.valueOf(
                                                             dropTables)
                                                             .booleanValue()
                                                         : true;

  static final boolean  CREATE_TABLES                = (createTables != null)
                                                         ? Boolean.valueOf(
                                                             createTables)
                                                             .booleanValue()
                                                         : true;

  static final String   SQLgameTrans                 = "INSERT INTO GAMETRANSACTION (transactionId, gameId, customerId) VALUES (?,1,1)";
  static final String   SQLgame                      = "INSERT INTO GAME (gameId, transactionId, gameType, gameAmount) VALUES (?,?,1,1)";
  static final String   SQLlotto                     = "INSERT INTO LOTTERY (lotteryId, gameId, NUMBER1, NUMBER2, NUMBER3) VALUES(?,?,1,1,1)";

  static final String   SQLgameTransMax              = "Select max(transactionId) FROM GAMETRANSACTION";
  static final String   SQLgameTransCount            = "Select count(transactionId) FROM GAMETRANSACTION";
  static final String   SQLgameMax                   = "Select max(gameId) from GAME";
  static final String   SQLgameCount                 = "Select count(gameId) from GAME";
  static final String   SQLlottoMax                  = "Select max(lotteryId) from LOTTERY";
  static final String   SQLlottoCount                = "Select count(lotteryId) from LOTTERY";

  static final String   createGameTransaction        = "CREATE TABLE GAMETRANSACTION (transactionId INTEGER NOT NULL PRIMARY KEY,gameId INTEGER NOT NULL,customerId INTEGER NOT NULL)";
  static final String   createGame                   = "CREATE TABLE GAME (gameId INTEGER NOT NULL PRIMARY KEY,transactionId INTEGER NOT NULL,gameType INTEGER NOT NULL,gameAmount INTEGER NOT NULL)";
  static final String   createLottery                = "CREATE TABLE LOTTERY (lotteryId INTEGER NOT NULL PRIMARY KEY,gameId INTEGER NOT NULL,NUMBER1 INTEGER NOT NULL,NUMBER2 INTEGER NOT NULL,NUMBER3 INTEGER NOT NULL)";

  static Logger         logger;
  static int            key                          = (keyIndex != null)
                                                         ? Integer
                                                             .parseInt(keyIndex)
                                                         : 0;
  static Object         synchObj                     = new Object();

  static
  {

    try
    {
      logger = Logger.getLogger(SQLInjectionTest.class);
      FileAppender fileappender = new FileAppender(new SimpleLayout(),
          LOG_FILE, true);
      fileappender.setImmediateFlush(true);
      logger.addAppender(fileappender);
      logger.setLevel(Level.INFO);
    }
    catch (Exception e)
    {
      System.exit(0);
    }
    try
    {
      Class.forName(CJDBC_DRIVER);
    }
    catch (Exception e)
    {
      logger.error("Unable to load cjdbc driver " + e.toString());
      fail("Unable to load cjdbc driver " + e.toString());
    }
  }

  /**
   * @see org.objectweb.cjdbc.scenario.templates.Template#getHypersonicConnection(int)
   */
  public Connection getBackendConnection(int index) throws Exception
  {
    Class.forName(CLASSES[index]);
    logger.debug("Connecting to:" + URLS[index] + ";" + USERS[index] + ";"
        + PASSWORDS[index]);
    return DriverManager.getConnection(URLS[index], USERS[index],
        PASSWORDS[index]);
  }

  /**
   * Start sql injection test
   */
  public void testSQLInjection()
  {

    logger.info("Starting sql injection test");
    if (standaloneTest)
      logger.info("The test has started an internal controller");
    else
      logger.info("The test is connected to cjdbc url:" + cjdbcurl);

    logger.info("The following parameters are used:");
    logger.info("CJDBC_URL:" + CJDBC_URL);
    logger.info("CJDBC_USER:" + CJDBC_USER);
    logger.info("CJDBC_PASSWORD:" + CJDBC_PASSWORD);
    logger.info("BACKENDS_URLS:" + Arrays.asList(URLS));
    logger.info("BACKENDS_USERS:" + Arrays.asList(USERS));
    logger.info("BACKENDS_PASSWORDS:" + Arrays.asList(PASSWORDS));
    logger.info("THREAD_COUNT:" + THREAD_COUNT);
    logger.info("THREAD_START_WAIT_TIME_RANGE:" + THREAD_START_WAIT_TIME_RANGE);
    logger.info("UNIT_RUN_COUNT:" + UNIT_RUN_COUNT);
    logger.info("THREAD_WAIT_TIME:" + THREAD_WAIT_TIME);
    logger.info("JOIN_THREAD_TIMEOUT:" + JOIN_THREAD_TIMEOUT);
    logger.info("CREATE_TABLE:" + CREATE_TABLES);
    logger.info("DROP_TABLES:" + DROP_TABLES);
    logger.info("KEY_INDEX:" + key);

    Connection con = null;

    if (DROP_TABLES)
    {
      logger.info("Dropping tables...");
      for (int i = 0; i < URLS.length; i++)
      {
        try
        {
          con = getBackendConnection(i);
          ResultSet tables = con.getMetaData().getTables(null, null, "%",
              new String[]{"TABLE"});
          while (tables.next())
          {
            String table = tables.getString("TABLE_NAME");
            logger.info("Found table:" + table);
            if (table.equalsIgnoreCase("GAMETRANSACTION")
                || table.equalsIgnoreCase("GAME")
                || table.equalsIgnoreCase("LOTTERY"))
            {
              logger.info("Dropping table " + table);
              con.createStatement().executeUpdate("DROP TABLE " + table);
            }
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
          // fail("Cannot drop tables:" + e.getMessage());
        }
      }
    }

    if (CREATE_TABLES)
    {
      logger.info("Creating tables...");
      for (int i = 0; i < URLS.length; i++)
      {
        try
        {
          con = getBackendConnection(i);
          con.createStatement().executeUpdate(createGameTransaction);
          con.createStatement().executeUpdate(createGame);
          con.createStatement().executeUpdate(createLottery);
        }
        catch (Exception e)
        {
          e.printStackTrace();
          // fail("Cannot create tables:" + e.getMessage());
        }
      }

    }

    try
    {
      logger.info("Creating connection...");
      con = getConnection();
    }
    catch (Exception e)
    {
      fail("Cannot connect to cjdbc:" + e.getMessage());
    }

    try
    {
      logger.info("Taking my breath before test...");
      Thread.sleep(2000);
    }
    catch (InterruptedException e2)
    {
    }

    Thread[] threads = new Thread[THREAD_COUNT];

    for (int i = 0; i < THREAD_COUNT; i++)
    {
      threads[i] = new Thread(new SQLInjectionTest());
      threads[i].start();

      try
      {
        Thread.sleep((int) (Math.random() * THREAD_START_WAIT_TIME_RANGE));
      }
      catch (Exception e)
      {
      }
    }

    for (int i = 0; i < THREAD_COUNT; i++)
    {
      try
      {
        if (threads[i].isAlive())
        {
          threads[i].join();
        }
      }
      catch (InterruptedException e1)
      {
        e1.printStackTrace();
      }
    }

    try
    {

      Connection cjdbc = getCJDBCConnection();
      Integer[] result3 = getIds(cjdbc);
      displayResult(result3, cjdbcurl);

      if (!standaloneTest)
      {
        logger.info("Checking following urls:" + Arrays.asList(urls));
        for (int i = 0; i < urls.length; i++)
        {
          Integer[] result = getIds(getBackendConnection(i));
          displayResult(result, urls[i]);

          assertEquals("Inconsistent result", Arrays.asList(result), Arrays
              .asList(result3));
        }
      }
      else
      {
        for (int i = 0; i < URLS.length; i++)
        {
          Connection backend = getBackendConnection(i);
          Integer[] result1 = getIds(backend);
          displayResult(result1, URLS[i]);
        }
        // assertEquals("Difference between hypersonic backends", Arrays
        // .asList(result1), Arrays.asList(result2));
        // assertEquals("C-JDBC count not consistent", Arrays.asList(result1),
        // Arrays.asList(result3));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("Verification failed:" + e.getMessage());
    }
  }

  private static final void displayResult(Integer[] res, String string)
  {
    logger.info("=========" + string + "========");
    for (int i = 0; i < res.length; i++)
      logger.info("RES[" + i + "]=" + res[i]);
    logger.info("========================");
  }

  private static final Integer[] getIds(Connection con) throws Exception
  {
    ArrayList list1 = ScenarioUtility
        .getSingleQueryResult(SQLgameTransMax, con);
    ArrayList list2 = ScenarioUtility.getSingleQueryResult(SQLgameMax, con);
    ArrayList list3 = ScenarioUtility.getSingleQueryResult(SQLlottoMax, con);
    ArrayList list4 = ScenarioUtility.getSingleQueryResult(SQLgameTransCount,
        con);
    ArrayList list5 = ScenarioUtility.getSingleQueryResult(SQLgameCount, con);
    ArrayList list6 = ScenarioUtility.getSingleQueryResult(SQLlottoCount, con);
    return new Integer[]{getId(list1), getId(list2), getId(list3),
        getId(list4), getId(list5), getId(list6)};
  }

  private static final Integer getId(ArrayList list)
  {
    String o = (String) ((ArrayList) list.get(0)).get(0);
    return new Integer(o);
  }

  /**
   * Return a unique key used for transaction
   * 
   * @return unique int
   */
  public static synchronized int getKey()
  {
    synchronized (synchObj)
    {
      key = key + 1;
      return key;
    }
  }

  /**
   * This class defines a SQLInjectionTest
   * 
   * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
   * @version 1.0
   */
  class SQLInjectionTest implements Runnable
  {
    private StringBuffer sqlGameTransBuf;
    private StringBuffer sqlGameBuf;
    private StringBuffer sqlLottoBuf;
    private StringBuffer buf;
    Connection           con = null;

    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      for (int i = 0; i < UNIT_RUN_COUNT; i++)
      {
        try
        {
          setUp();
          testCJDBCSqlInjection();
          tearDown();

          try
          {
            synchronized (this)
            {
              wait((int) (Math.random() * THREAD_WAIT_TIME) + 1);
            }
          }
          catch (Exception e)
          {
          }
        }
        catch (Exception e)
        {
          fail("Error in thread:" + e.toString());
          logger.error("" + e.toString());
        }
        finally
        {
          try
          {
            tearDown();
          }
          catch (Exception e)
          {
            logger.error("Unable to close connection after bad run: "
                + e.toString());
          }
        }
      }
    }

    /**
     * Set up the test. Prepare the statements and get the connection
     * 
     * @throws Exception if fails
     */
    public void setUp() throws Exception
    {
      con = null;
      SQLUtil s = new SQLUtil();
      logger = Logger.getLogger(SQLInjectionTest.class);
      try
      {
        con = DriverManager
            .getConnection(CJDBC_URL, CJDBC_USER, CJDBC_PASSWORD);
      }
      catch (Exception se)
      {
        se.printStackTrace();
        logger.error(se.toString());
        con = null;
        throw se;
      }

      int localKey = getKey();

      buf = new StringBuffer("\nTRANSKEY: ");
      buf.append(localKey).append("\n");

      // Setup proper SQL statements
      s.clearParams();
      s.clearSQL();
      // Calendar h = Calendar.getInstance();
      s.setSQL(SQLgameTrans);
      s.setInt(1, localKey);
      sqlGameTransBuf = s.prepareSQL();

      s.clearParams();
      s.clearSQL();
      s.setSQL(SQLgame);
      s.setInt(1, localKey);
      s.setInt(2, localKey);
      sqlGameBuf = s.prepareSQL();

      s.clearParams();
      s.clearSQL();
      s.setSQL(SQLlotto);
      s.setInt(1, localKey);
      s.setInt(2, localKey);
      sqlLottoBuf = s.prepareSQL();

    }

    /**
     * Clean object after test and reset connection.
     * 
     * @throws Exception if fails
     */
    public void tearDown() throws Exception
    {
      if (con != null)
      {
        con.close();
        con = null;
      }
    }

    /**
     * Start single unit test
     */
    public void testCJDBCSqlInjection()
    {
      if (con == null)
      {
        logger.error("Connection is NULL");
      }
      else
      {
        buf.append("\n---------SQL Statments created-------\n").append(
            sqlGameTransBuf.toString()).append("\n");
        buf.append(sqlGameBuf.toString()).append("\n").append(
            sqlLottoBuf.toString());
        buf.append("\n---------SQL Statments end    -------\n\n");
        try
        {
          logger.debug(buf.toString());

          con.setAutoCommit(false);

          Statement stmt = con.createStatement();
          try
          {
            stmt.execute(sqlGameTransBuf.toString());
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          finally
          {
            stmt.close();
          }

          stmt = con.createStatement();
          try
          {
            stmt.execute(sqlGameBuf.toString());
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          finally
          {
            stmt.close();
          }

          stmt = con.createStatement();
          try
          {
            stmt.execute(sqlLottoBuf.toString());
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          finally
          {
            stmt.close();
          }

          con.commit();
          /*
           * con.createStatement().execute(sqlGameTransBuf.toString());
           * con.createStatement().execute(sqlGameBuf.toString());
           * con.createStatement().execute(sqlLottoBuf.toString());
           */

          // con.setAutoCommit(true);
        }
        catch (SQLException se)
        {
          logger.warn("Commit Failure: " + se);
          try
          {
            con.rollback();
          }
          catch (SQLException s)
          {
            logger.error("RollBack Failure: " + s);
          }
        }
      }
    }
  }

  /**
   * Provides wrapper to JDBC.
   */
  class SQLUtil
  {
    private Connection connection = null;
    private String     sql        = "";
    private HashMap    params     = new HashMap();

    /**
     * Constructs a SQLUtil with a DataSource that can be used to manufacture
     * Connections
     */
    public SQLUtil()
    {
    }

    public void setInt(int index, int value) throws SQLException
    {
      params.put(new Integer(index), "" + value);
    }

    public void clearParams()
    {
      params.clear();
    }

    public void setSQL(String sql)
    {
      this.sql = sql;
    }

    public String getSQL(boolean withParams)
    {
      try
      {
        if (withParams)
        {
          StringBuffer builder = new StringBuffer();
          String[] tokens = sql.split("\\x3f");
          int tokenIterator = 0;
          Iterator i = new TreeSet(params.keySet()).iterator();
          while (i.hasNext())
          {
            builder.append(tokens[tokenIterator++]);
            builder.append(params.get(i.next()));
          }
          if (tokenIterator < tokens.length)
          {
            builder.append(tokens[tokenIterator++]);
          }
          return builder.toString();
        }
        return this.sql;
      }
      catch (Exception e)
      {
        e.printStackTrace();
        return null;
      }
    }

    public void clearSQL()
    {
      setSQL("");
    }

    public Connection getConnection() throws SQLException
    {
      return connection;
    }

    public StringBuffer prepareSQL()
    {
      try
      {
        StringBuffer builder = new StringBuffer();
        String[] tokens = sql.split("\\x3f");
        int tokenIterator = 0;
        Iterator i = new TreeSet(params.keySet()).iterator();
        while (i.hasNext())
        {
          builder.append(tokens[tokenIterator++]);
          builder.append(params.get(i.next()));
        }
        if (tokenIterator < tokens.length)
        {
          builder.append(tokens[tokenIterator++]);
        }
        return builder;
      }
      catch (Exception e)
      {
        System.err.println(e.toString());
      }
      finally
      {
        clearParams();
        clearSQL();
      }
      return null;
    }
  }
}