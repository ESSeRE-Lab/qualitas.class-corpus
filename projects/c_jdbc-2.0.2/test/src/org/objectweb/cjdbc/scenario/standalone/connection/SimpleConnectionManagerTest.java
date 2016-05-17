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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.controller.connection.SimpleConnectionManager;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.mock.MockDriver;

import com.mockobjects.sql.MockConnection2;

/**
 * <code>SimpleConnectionManager</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @see org.objectweb.cjdbc.controller.connection.SimpleConnectionManager
 */
public class SimpleConnectionManagerTest extends NoTemplate
{
  /** Fake driver. */
  private MockDriver              mockDriver;

  /** Fake connection. */
  private MockConnection2         mockConnection;

  /** Simple connection manager pool to test. */
  private SimpleConnectionManager manager;

  /**
   * Builds a new TestSuite
   * 
   * @return the TestSuite
   */
  public static Test suite()
  {
    return new TestSuite(SimpleConnectionManagerTest.class);
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
      mockConnection = new MockConnection2();
      mockConnection.setExpectedCloseCalls(1);
      mockConnection.setupIsClosed(true);
      mockDriver = new MockDriver();
      mockDriver.setExpectedConnectCalls(1);
      mockDriver.setupConnect(mockConnection);

      DriverManager.registerDriver(mockDriver);
    }
    catch (SQLException e)
    {
      fail("Failed to register driver: " + e);
    }

    // Create a simple connection manager
    try
    {
      manager = new SimpleConnectionManager("", "", "", "", null, null);
    }
    catch (Exception e)
    {
      fail("Failed to create simple pool connection manager: " + e);
    }

    // initialize the connection manager
    try
    {
      manager.initializeConnections();
    }
    catch (SQLException e)
    {
      fail("Failed to initialize pool connection manager: " + e);
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
   * Simple connection manager functionnal test.
   * 
   * @throws UnreachableBackendException if no connections available test should
   *           fail !
   */
  public void testSimpleConnectionManager() throws UnreachableBackendException
  {
    // Get a connection from simple connection manager
    Connection c = manager.getConnection();
    if (c == null)
      fail("Failed to get a connection from simple connection manager");

    // Release the connection
    manager.releaseConnection(c);
    try
    {
      assertTrue(c.isClosed());
    }
    catch (SQLException e)
    {
      fail("Exception thrown: " + e);
    }
    mockConnection.verify();
    mockDriver.verify();
  }
}