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
 * Initial developer(s): Mathieu Peltier.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.standalone.backend;

import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.backend.DatabaseBackendSchemaConstants;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.databases.AbstractDatabase;
import org.objectweb.cjdbc.scenario.tools.databases.RUBiSDatabase;
import org.objectweb.cjdbc.scenario.tools.mock.MockConnectionManager;

/**
 * <code>DatabaseBackend</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend
 */
public class DatabaseBackendTest extends NoTemplate
{
  /** Test database. */
  private AbstractDatabase database1;

  /** Database backend. */
  private DatabaseBackend  backend1, backend2, backend3, backend4, backend5;

  /** Fake connection manager. */
  private AbstractConnectionManager connectionManager1, connectionManager2;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    String level = DatabaseBackendSchemaConstants
        .getDynamicSchemaLevel(DatabaseBackendSchemaConstants.DynamicPrecisionColumn);
    backend1 = new DatabaseBackend("backend1", null, "MockDriver1", "URL1",
        "rubis", "SELECT 1", level);
    backend2 = new DatabaseBackend("backend2", null, "MockDriver1", "URL1",
        "rubis", "SELECT 1", level);
    backend3 = new DatabaseBackend("backend1", null, "MockDriver2", "URL1",
        "rubis", "SELECT 1", level);
    backend4 = new DatabaseBackend("backend1", null, "MockDriver1", "URL2",
        "rubis", "SELECT 1", level);
    backend5 = new DatabaseBackend("backend1", null, "MockDriver1", "URL1",
        "rubis", "SELECT 1", level);

    database1 = new RUBiSDatabase();
    connectionManager1 = new MockConnectionManager(database1);
    connectionManager2 = new MockConnectionManager(database1);

    backend1.addConnectionManager("vLogin1", connectionManager1);
    backend1.addConnectionManager("vLogin2", connectionManager2);
  }

  /**
   * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend#equals(Object)
   */
  public void testEquals()
  {
    assertTrue(backend1.equals(backend1));
    assertTrue(backend1.equals(backend5));
    assertFalse(backend1.equals(backend2));
    assertFalse(backend1.equals(backend3));
    assertFalse(backend1.equals(backend4));
  }

  /**
   * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend#hasTables(ArrayList)
   */
  public void testHasTables()
  {
    backend1.setDatabaseSchema(database1.getSchema(), true);

    ArrayList list = new ArrayList();
    list.add("categories");
    list.add("regions");
    list.add("users");
    list.add("items");
    list.add("old_items");
    list.add("bids");
    list.add("comments");
    list.add("buy_now");
    list.add("ids");
    assertTrue(backend1.hasTables(list));

    list = new ArrayList();
    list.add("categories");
    list.add("foo");
    assertFalse(backend1.hasTables(list));
  }

  /**
   * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend#hasTable(String)
   */
  public void testHasTable()
  {
    backend1.setDatabaseSchema(database1.getSchema(), true);

    assertTrue(backend1.hasTable("categories"));
    assertTrue(backend1.hasTable("regions"));
    assertTrue(backend1.hasTable("users"));
    assertTrue(backend1.hasTable("items"));
    assertTrue(backend1.hasTable("old_items"));
    assertTrue(backend1.hasTable("bids"));
    assertTrue(backend1.hasTable("comments"));
    assertTrue(backend1.hasTable("buy_now"));
    assertTrue(backend1.hasTable("ids"));

    assertFalse(backend1.hasTable("foo"));
    assertFalse(backend1.hasTable(""));
    assertFalse(backend1.hasTable(null));
  }

  /**
   * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend#initializeConnections()
   */
  public void testInitializeConnections()
  {
    try
    {
      backend2.initializeConnections();
      fail("Exception not thrown when trying to initialize connections of an empty database backend");
    }
    catch (SQLException ignore)
    {
    }

    try
    {
      backend1.initializeConnections();
      assertTrue(backend1.isInitialized());
    }
    catch (SQLException e)
    {
      fail("Exception thrown " + e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend#finalizeConnections()
   */
  public void testFinalizeConnections()
  {
    try
    {
      backend2.finalizeConnections();
      fail("Exception not thrown when trying to finalize connections of an empty database backend");
    }
    catch (SQLException ignore)
    {
    }

    try
    {
      backend1.finalizeConnections();
      assertFalse(backend1.isInitialized());
    }
    catch (SQLException e)
    {
      fail("Exception thrown " + e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend#getConnectionManager(String)
   */
  public void testAddConnectionManager()
  {
    assertNull(backend2.getConnectionManager("vLogin1"));
    backend2.addConnectionManager("vLogin1", connectionManager1);
    assertEquals(connectionManager1, backend2.getConnectionManager("vLogin1"));
  }

  /**
   * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend#isInitialized()
   */
  public void testIsInitialized()
  {
    try
    {
      assertFalse(backend1.isInitialized());
    }
    catch (SQLException e)
    {
      fail("Exception thrown " + e);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend#getConnectionManager(String)
   */
  public void testGetConnectionManager(String vLogin)
  {
    backend1.addConnectionManager("vLogin1", connectionManager1);
    backend1.addConnectionManager("vLogin2", connectionManager2);
    assertNull(backend1.getConnectionManager("vLogin3"));

    assertNull(backend2.getConnectionManager("vLogin1"));
  }

  /**
   * @see org.objectweb.cjdbc.controller.backend.DatabaseBackend#checkDatabaseSchema()
   */
  public void testCheckDatabaseSchema()
  {
    backend1.checkDatabaseSchema();
    assertTrue(database1.getSchema().isCompatibleWith(
        backend1.getDatabaseSchema()));
  }
}