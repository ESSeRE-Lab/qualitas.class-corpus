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
 * Initial developer(s): Mathieu Peltier.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.standalone.connection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Stack;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.controller.connection.RandomWaitPoolConnectionManager;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.mock.MockDriver;
import org.objectweb.cjdbc.scenario.tools.util.GetConnectionThread;
import org.objectweb.cjdbc.scenario.tools.util.PrivilegedAccessor;

import com.mockobjects.sql.MockConnection2;

/**
 * <code>RandomWaitPoolConnectionManager</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @see org.objectweb.cjdbc.controller.connection.RandomWaitPoolConnectionManager
 */
public class RandomWaitPoolConnectionManagerTest extends NoTemplate
{
  /** Fake driver. */
  private MockDriver                      mockDriver;

  /** Random wait connection manager pool to test. */
  private RandomWaitPoolConnectionManager pool;

  /**
   * Builds a new TestSuite
   * 
   * @return the TestSuite
   */
  public static Test suite()
  {
    return new TestSuite(RandomWaitPoolConnectionManagerTest.class);
  }

  /**
   * Starts a new test
   * 
   * @param args test parameters
   */
  public static void main(String[] args)
  {
    TestRunner.run(suite());
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    // Create and register the mock driver
    try
    {
      mockDriver = new MockDriver();
      DriverManager.registerDriver(mockDriver);
    }
    catch (SQLException e)
    {
      fail("Failed to register driver: " + e);
    }
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {
    // Deregister the driver
    try
    {
      DriverManager.deregisterDriver(mockDriver);
    }
    catch (SQLException e)
    {
      fail("Failed to deregister driver: " + e);
    }
  }

  /**
   * Creates and initializes a <code>RandomWaitoolConnectionManager</code>.
   * 
   * @param poolSize pool size.
   * @param timeout timeout.
   */
  private void initializePool(int poolSize, int timeout)
  {
    // Initialize driver
    mockDriver.setExpectedConnectCalls(poolSize);
    for (int i = 0; i < poolSize; i++)
    {
      mockDriver.setupConnect(new MockConnection2());
    }

    // Create the pool
    try
    {
      pool = new RandomWaitPoolConnectionManager("", "", "", "", null, null,
          poolSize, timeout);
    }
    catch (Exception e)
    {
      fail("Failed to create pool connection manager: " + e);
    }

    // Initialize the pool
    try
    {
      pool.initializeConnections();
    }
    catch (SQLException e)
    {
      fail("Failed to initialize pool connection manager: " + e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.RandomWaitPoolConnectionManager#releaseConnection(Connection)
   * @see org.objectweb.cjdbc.controller.connection.RandomWaitPoolConnectionManager#getConnection()
   * 
   * @throws Exception if an error occurs
   */
  public void testGetAndReleaseConnection() throws Exception
  {
    // Create a random wait pool connection manager of 3 connections
    initializePool(3, 10);

    //     <- TIMEOUT ->
    //     ----------------
    // t1 ........
    // t2 ................................
    // t3 ............

    // Create 1 thread that gets a connection from the pool and sleep during
    // 1/ 2 * TIMEOUT seconds and then release the connection
    Thread t1 = new GetConnectionThread("thread1", pool, 5000);

    // Create 1 thread that get a connection from the pool and sleep during
    // 2 * TIMEOUT seconds and then release the connection
    Thread t2 = new GetConnectionThread("thread2", pool, 20000);

    // Create 1 thread that get a connection from the pool and sleep during
    // 3 / 4 * TIMEOUT seconds and then release the connection
    Thread t3 = new GetConnectionThread("thread3", pool, 7500);

    assertEquals(3, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(0, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());

    t1.start();
    t2.start();
    t3.start();

    // make sure that the threads have got their connection
    try
    {
      Thread.sleep(1000);
    }
    catch (InterruptedException e)
    {
      fail("Exception thrown: " + e);
    }

    // Get another connection: t1 should release his connection before the
    // timeout
    try
    {
      assertNotNull(pool.getConnection());
    }
    catch (UnreachableBackendException e1)
    {
      fail("Backend unreachable during test.");
    }

    assertEquals(0, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(3, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());

    // Get another connection: t3 should release his connection before the
    // timeout
    try
    {
      assertNotNull(pool.getConnection());
    }
    catch (UnreachableBackendException e2)
    {
      fail("Backend unreachable during test.");
    }

    assertEquals(0, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(3, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());

    // Try to get a second connection: t2 should not release his connection
    // before the timeout
    try
    {
      assertNull(pool.getConnection());
    }
    catch (UnreachableBackendException e3)
    {
      fail("Backend unreachable during test.");
    }

    assertEquals(0, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(3, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());

    // make sure to wait for the end of the thread 2
    try
    {
      t2.join();
    }
    catch (InterruptedException e)
    {
      fail("Exception thrown: " + e);
    }

    assertEquals(1, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(2, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());

  }
}