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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.controller.connection.VariablePoolConnectionManager;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.mock.MockDriver;
import org.objectweb.cjdbc.scenario.tools.util.GetConnectionThread;
import org.objectweb.cjdbc.scenario.tools.util.PrivilegedAccessor;

import com.mockobjects.sql.MockConnection2;

/**
 * <code>VariablePoolConnectionManager</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @see org.objectweb.cjdbc.controller.connection.RandomWaitPoolConnectionManager
 */
public class VariablePoolConnectionManagerTest extends NoTemplate
{
  /** Fake driver. */
  private MockDriver                    mockDriver;

  /** Variable pool connection manager pool to test. */
  private VariablePoolConnectionManager pool;

  /**
   * Builds a new TestSuite
   * 
   * @return the TestSuite
   */
  public static Test suite()
  {
    return new TestSuite(VariablePoolConnectionManagerTest.class);
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
    // Create aand register the mock driver
    try
    {
      mockDriver = new MockDriver()
      {
        public Connection connect(String arg0, Properties arg1)
            throws SQLException
        {
          return new MockConnection2();
        }
      };
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

    pool = null;
    System.gc();
  }

  /**
   * Creates and initializes a <code>VariablePoolConnectionManager</code>.
   * 
   * @param initPoolSize init pool size.
   * @param minPoolSize min pool size.
   * @param maxPoolSize max pool size.
   * @param idleTimeout idle timeout
   * @param waitTimeout wait timeout.
   */
  private void initializePool(int initPoolSize, int minPoolSize,
      int maxPoolSize, int idleTimeout, int waitTimeout)
  {
    // Create a variable pool connection manager
    try
    {
      pool = new VariablePoolConnectionManager("", "", "", "", null, null,
          initPoolSize, minPoolSize, maxPoolSize, idleTimeout, waitTimeout);
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
   * Test of the wait timeout feature.
   * 
   * @throws Exception if an error occurs
   */
  public void testWaitTimeoutFeature() throws Exception
  {
    initializePool(3, 1, 3, 15, 10);

    // Create 1 thread that gets a connection from the pool, sleep during
    // 2.5 s and then release the connection
    Thread t1 = new GetConnectionThread("thread1", pool, 2500);

    // Create 1 thread that get a connection from the pool, sleep during
    // 20 s and then release the connection
    Thread t2 = new GetConnectionThread("thread2", pool, 20000);

    // Create 1 thread that get a connection from the pool, sleep during
    // 7.5 s and then release the connection
    Thread t3 = new GetConnectionThread("thread3", pool, 7500);

    //         <- IDLE_TIMEOUT ->
    //     ---------------------------
    //     <- WAIT_TIMEOUT ->
    //     ------------------
    // t1 .....
    // t2 ....................................
    // t3 .............
    //
    // with WAIT_TIMEOUT = 10 s
    //     0 2.5 7.5 20

    assertEquals(3, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(0, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());

    t1.start();
    t2.start();
    t3.start();

    // Make sure the thread have got their connection
    try
    {
      Thread.sleep(500);
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
    assertEquals(3, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
        .intValue());

    // Get another connection: t3 should release his connection before the
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
    assertEquals(3, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
        .intValue());

    // Try to get a second connection: t2 should not release his connection
    // before the timeout
    try
    {
      assertNull(pool.getConnection());
    }
    catch (UnreachableBackendException e1)
    {
      fail("Backend unreachable during test.");
    }

    assertEquals(0, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(3, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());
    assertEquals(3, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
        .intValue());

    try
    {
      t1.join();
      t2.join();
      t3.join();
    }
    catch (InterruptedException e)
    {
      fail("Unexpected exception");
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.VariablePoolConnectionManager#releaseConnection(Connection)
   * @see org.objectweb.cjdbc.controller.connection.VariablePoolConnectionManager#getConnection()
   * @throws Exception if an error occurs
   */
  public void testGetAndReleaseConnection() throws Exception
  {
    initializePool(3, 1, 3, 15, 10);

    // Create 1 thread that gets a connection from the pool, sleep during
    // 2.5 s and then release the connection
    Thread t1 = new GetConnectionThread("thread1", pool, 2500);

    // Create 1 thread that get a connection from the pool, sleep during
    // 20 s and then release the connection
    Thread t2 = new GetConnectionThread("thread2", pool, 20000);

    // Create 1 thread that get a connection from the pool, sleep during
    // 7.5 s and then release the connection
    Thread t3 = new GetConnectionThread("thread3", pool, 7500);

    //         <- IDLE_TIMEOUT ->
    //     ---------------------------
    //     <- WAIT_TIMEOUT ->
    //     ------------------
    // t1 .....
    // t2 ....................................
    // t3 .............
    // 
    // c1 CAAAAAFFFFFFFFFFFFFFFFFFFFFFFFFFFD
    // c2 CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
    // c3 CAAAAAAAAAAAAAFFFFFFFFFFFFFFFFFFFFFFFFFFFD
    //
    // C = creation
    // A = active
    // F = idle
    //
    //    0 2.5 7.5 17.5 20 22.5 35

    assertEquals(3, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(0, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());

    t1.start();
    t2.start();
    t3.start();

    // Make sure the thread have got their connection
    try
    {
      Thread.sleep(500);
    }
    catch (InterruptedException e)
    {
      fail("Exception thrown: " + e);
    }

    try
    {
      Thread.sleep(2500);
      // t = 3 s
      assertEquals(1, ((Stack) PrivilegedAccessor.getValue(pool,
          "freeConnections")).size());
      assertEquals(2, ((Vector) PrivilegedAccessor.getValue(pool,
          "activeConnections")).size());
      assertEquals(3, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
          .intValue());

      Thread.sleep(5500);
      // t = 8.5 s
      assertEquals(2, ((Stack) PrivilegedAccessor.getValue(pool,
          "freeConnections")).size());
      assertEquals(1, ((Vector) PrivilegedAccessor.getValue(pool,
          "activeConnections")).size());
      assertEquals(3, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
          .intValue());

      Thread.sleep(9500);
      // t = 18 s
      assertEquals(1, ((Stack) PrivilegedAccessor.getValue(pool,
          "freeConnections")).size());
      assertEquals(1, ((Vector) PrivilegedAccessor.getValue(pool,
          "activeConnections")).size());
      assertEquals(2, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
          .intValue());

      Thread.sleep(2500);
      // t = 20.5
      assertEquals(2, ((Stack) PrivilegedAccessor.getValue(pool,
          "freeConnections")).size());
      assertEquals(0, ((Vector) PrivilegedAccessor.getValue(pool,
          "activeConnections")).size());
      assertEquals(2, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
          .intValue());

      Thread.sleep(2500);
      // t = 23 s
      assertEquals(1, ((Stack) PrivilegedAccessor.getValue(pool,
          "freeConnections")).size());
      assertEquals(0, ((Vector) PrivilegedAccessor.getValue(pool,
          "activeConnections")).size());
      assertEquals(1, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
          .intValue());

      Thread.sleep(12500);
      // t = 35.5 s
      assertEquals(1, ((Stack) PrivilegedAccessor.getValue(pool,
          "freeConnections")).size());
      assertEquals(0, ((Vector) PrivilegedAccessor.getValue(pool,
          "activeConnections")).size());
      assertEquals(1, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
          .intValue());

      t1.join();
      t2.join();
      t3.join();
    }
    catch (InterruptedException e)
    {
      fail("Exception thrown: " + e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.VariablePoolConnectionManager.RemoveIdleConnectionsThread
   */
  public void testRemoveIdleConnectionsThread() throws Exception
  {
    // Create and initialize pool connection manager
    // initializePool(init,min,max,idle,wait)
    initializePool(20, 10, 30, 10, 5);

    assertEquals(20, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(0, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());
    assertEquals(30, pool.getMaxPoolSize());
    assertEquals(10, pool.getMinPoolSize());
    // pool is converting wait/idle timeout to seconds ...
    assertEquals(5000, pool.getWaitTimeout());
    assertEquals(10000, pool.getIdleTimeout());

    // Wait until the idle timeout has been reached
    try
    {
      Thread.sleep(11000);
    }
    catch (InterruptedException e)
    {
      fail("Exception thrown: " + e);
    }

    assertEquals(10, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(0, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());
    assertEquals(30, pool.getMaxPoolSize());
    assertEquals(10, pool.getMinPoolSize());
  }
}