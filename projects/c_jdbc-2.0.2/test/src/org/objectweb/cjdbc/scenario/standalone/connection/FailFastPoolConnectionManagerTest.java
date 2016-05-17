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
import java.util.Stack;
import java.util.Vector;

import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.controller.connection.FailFastPoolConnectionManager;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.mock.MockDriver;
import org.objectweb.cjdbc.scenario.tools.util.PrivilegedAccessor;

import com.mockobjects.sql.MockConnection2;

/**
 * <code>FailFastPoolConnectionManager</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @see org.objectweb.cjdbc.controller.connection.FailFastPoolConnectionManager
 */
public class FailFastPoolConnectionManagerTest extends NoTemplate
{
  /** Pool size to use for the test. */
  private static final int              POOL_SIZE = 30;

  /** Fake driver. */
  private MockDriver                    mockDriver;

  /** Fail fast pool connection manager pool to test. */
  private FailFastPoolConnectionManager pool;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    // Create and register the mock driver
    try
    {
      mockDriver = new MockDriver();
      mockDriver.setExpectedConnectCalls(POOL_SIZE);
      for (int i = 0; i < POOL_SIZE; i++)
      {
        mockDriver.setupConnect(new MockConnection2());
      }

      DriverManager.registerDriver(mockDriver);
    }
    catch (SQLException e)
    {
      fail("Failed to register driver: " + e);
    }

    // Create a fail fast pool connection manager of POOL_SIZE connections
    try
    {
      pool = new FailFastPoolConnectionManager("", "", "", "", null, null,
          POOL_SIZE);
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
    mockDriver.verify();
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
   * @see org.objectweb.cjdbc.controller.connection.FailFastPoolConnectionManager#getConnection()
   */
  public void testGetConnection() throws Exception
  {
    // Get POOL_SIZE connections from the pool
    Connection c = null;

    int free = ((Stack) PrivilegedAccessor.getValue(pool, "freeConnections"))
        .size();
    int active = ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size();
    assertEquals(30, ((Integer) PrivilegedAccessor.getValue(pool, "poolSize"))
        .intValue());

    int i = 0;
    while (i < POOL_SIZE)
    {
      try
      {
        c = pool.getConnection();
      }
      catch (UnreachableBackendException e)
      {
        fail("Backend unreachable during test.");
      }
      if (c == null)
        fail("Failed to get a connection from the pool");
      else
      {
        i++;
        assertEquals(--free, ((Stack) PrivilegedAccessor.getValue(pool,
            "freeConnections")).size());
        assertEquals(++active, ((Vector) PrivilegedAccessor.getValue(pool,
            "activeConnections")).size());

      }
    }

    // Try to get one more connection from the pool
    try
    {
      c = pool.getConnection();
    }
    catch (UnreachableBackendException e)
    {
      fail("Backend unreachable during test.");
    }
    if (c != null)
      fail("Got more connection than available from the pool");
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.FailFastPoolConnectionManager#releaseConnection(Connection)
   */
  public void testReleaseConnection() throws Exception
  {
    // Get a connections from the pool
    Connection c = null;
    try
    {
      c = pool.getConnection();
    }
    catch (UnreachableBackendException e)
    {
      fail("Backend unreachable during test.");
    }
    if (c == null)
      fail("Failed to get connection from the pool");
    assertEquals(POOL_SIZE - 1, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(1, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());

    // Release the connection
    pool.releaseConnection(c);
    assertEquals(POOL_SIZE, ((Stack) PrivilegedAccessor.getValue(pool,
        "freeConnections")).size());
    assertEquals(0, ((Vector) PrivilegedAccessor.getValue(pool,
        "activeConnections")).size());

  }
}