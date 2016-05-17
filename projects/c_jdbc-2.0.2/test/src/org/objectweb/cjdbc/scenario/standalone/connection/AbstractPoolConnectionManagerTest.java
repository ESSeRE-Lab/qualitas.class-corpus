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
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Vector;

import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.mock.MockDriver;
import org.objectweb.cjdbc.scenario.tools.util.PrivilegedAccessor;

import com.mockobjects.sql.MockConnection2;

/**
 * <code>AbstractPoolConnectionManager</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager
 */
public class AbstractPoolConnectionManagerTest extends NoTemplate
{
  /** Fake driver. */
  private MockDriver                    mockDriver;

  /** Pool manager to test. */
  private AbstractPoolConnectionManager pool;

  /** Idle connections contained in the pool. */
  private Stack                         freeConnections;

  /** Active connections contained in the pool. */
  private Vector                        activeConnections;

  /**
   * Creates and initializes a <code>VariablePoolConnectionManager</code>.
   * 
   * @param maxConnectionNumber maximum number of connection allowed by the
   *          driver.
   * @param requestedPoolSize requested pool size.
   */
  private void initializePool(int maxConnectionNumber, int requestedPoolSize)
  {
    // Create a new driver that will return only maxConnectionNumber
    // connections
    mockDriver = new MyMockDriver(maxConnectionNumber);

    // Register the driver
    try
    {
      DriverManager.registerDriver(mockDriver);
    }
    catch (SQLException e)
    {
      fail("Failed to register driver: " + e);
    }

    // Create a new pool
    try
    {
      pool = new MockPoolConnectionManager("", "", "", "", requestedPoolSize);
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

    // Get a reference to the idle and active connections
    try
    {
      freeConnections = (Stack) PrivilegedAccessor.getValue(pool,
          "freeConnections");
      activeConnections = (Vector) PrivilegedAccessor.getValue(pool,
          "activeConnections");
    }
    catch (Exception e)
    {
      fail("Unexpected exception thrown: " + e);
    }

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
   * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager#initializeConnections()
   */
  public void testInitializeConnections()
  {
    try
    {
      // Request connections from a driver that never gets one connection
      initializePool(0, 10);
      assertEquals(0, freeConnections.size());
      assertEquals(0, activeConnections.size());
      assertEquals(0, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
          .intValue());
      assertTrue(pool.isInitialized());

      // Normal case
      initializePool(20, 10);
      assertEquals(10, freeConnections.size());
      assertEquals(0, activeConnections.size());
      assertEquals(10,
          ((Integer) PrivilegedAccessor.getValue(pool, "poolSize")).intValue());
      assertTrue(pool.isInitialized());

      // Limit case
      initializePool(20, 20);
      assertEquals(20, freeConnections.size());
      assertEquals(0, activeConnections.size());
      assertEquals(20,
          ((Integer) PrivilegedAccessor.getValue(pool, "poolSize")).intValue());
      assertTrue(pool.isInitialized());

      // Request too much connections
      initializePool(20, 30);
      assertEquals(20, freeConnections.size());
      assertEquals(0, activeConnections.size());
      assertEquals(20,
          ((Integer) PrivilegedAccessor.getValue(pool, "poolSize")).intValue());
      assertTrue(pool.isInitialized());
    }
    catch (Exception e)
    {
      fail("Unexpected exception thrown: " + e);
    }

    // Try to initialize a pool twice
    try
    {
      pool.initializeConnections();
      fail("Exception not thrown when trying to initialized twice a pool");
    }
    catch (SQLException expected)
    {
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager#finalizeConnections()
   */
  public void testFinalizeConnections()
  {
    // Create a pool of 10 connections and initialize it
    initializePool(10, 10);

    // Get 6 connections to make them active
    for (int i = 0; i < 6; i++)
    {
      try
      {
        pool.getConnection();
      }
      catch (UnreachableBackendException e1)
      {
        fail("Backend unreachable during test.");
      }
    }

    // Finalize the pool
    try
    {
      pool.finalizeConnections();
    }
    catch (SQLException e)
    {
      fail("Failed to finalize pool connection manager: " + e);
    }

    // Check that connections are closed
    mockDriver.verify();
  }

  /**
   * Mock pool connection manager (minimum implementation of the
   * <code>AbstractPoolConnectionManager</code> class).
   * 
   * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager
   */
  public class MockPoolConnectionManager extends AbstractPoolConnectionManager
  {
    /**
     * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager#AbstractPoolConnectionManager
     */
    public MockPoolConnectionManager(String backendUrl, String backendName,
        String rLogin, String rPassword, int poolSize)
    {
      super(backendUrl, backendName, rLogin, rPassword, null, null, poolSize);
    }

    /**
     * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager#getConnection()
     */
    public Connection getConnection()
    {
      try
      {
        Connection c = (Connection) this.freeConnections.removeLast();
        this.activeConnections.add(c);
        return c;
      }
      catch (EmptyStackException e)
      {
        return null;
      }
    }

    /**
     * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager#releaseConnection(Connection)
     */
    public void releaseConnection(Connection connection)
    {
      this.freeConnections.addLast(connection);
    }

    /**
     * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager#deleteConnection(Connection)
     */
    public void deleteConnection(Connection connection)
    {
      this.activeConnections.remove(connection);
    }

    /**
     * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager#getXmlImpl()
     */
    public String getXmlImpl()
    {
      return null;
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException
    {
      return null;
    }
  }

  /**
   * Mock driver. The method <code>connect()</code> will return only a limited
   * number of connections, specified by <code>maxConnectionNumber</code>. If
   * more connections are requested, an exception is thrown.
   */
  public class MyMockDriver extends MockDriver
  {
    /** Expected connections. */
    private MockConnection2[] mockConnections;

    /**
     * Creates a new <code>MyMockDriver</code> instance.
     * 
     * @param maxConnectionNumber maximum number of connection allowed by the
     *          driver.
     */
    public MyMockDriver(int maxConnectionNumber)
    {
      mockConnections = new MockConnection2[maxConnectionNumber];
      for (int i = 0; i < maxConnectionNumber; i++)
      {
        mockConnections[i] = new MockConnection2();
        mockConnections[i].setExpectedCloseCalls(1);
        mockConnections[i].setupIsClosed(true);
        setupConnect(mockConnections[i]);
      }
      setupExceptionConnect(new SQLException(
          "Max number of connection reached (" + maxConnectionNumber + ")"));
    }

    /**
     * Verifies that all connections are closed.
     * 
     * @see org.objectweb.cjdbc.scenario.tools.mock.MockDriver#verify()
     */
    public void verify()
    {
      super.verify();
      for (int i = 0; i < mockConnections.length; i++)
      {
        mockConnections[i].verify();
      }
    }
  }
}