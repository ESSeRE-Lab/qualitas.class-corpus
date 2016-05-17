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

package org.objectweb.cjdbc.scenario.standalone.authentication;

import org.objectweb.cjdbc.common.users.AdminUser;
import org.objectweb.cjdbc.common.users.DatabaseBackendUser;
import org.objectweb.cjdbc.common.users.VirtualDatabaseUser;
import org.objectweb.cjdbc.controller.authentication.AuthenticationManager;
import org.objectweb.cjdbc.controller.authentication.AuthenticationManagerException;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * <code>AuthenticationManager</code> test class.
 * 
 * @author <a href="Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * @see org.objectweb.cjdbc.controller.authentication.AuthenticationManager
 */
public class AuthenticationManagerTest extends NoTemplate
{
  /** Authentication manager. */
  private AuthenticationManager manager;

  /** Virtual database user. */
  private VirtualDatabaseUser virtualUser1,
    virtualUser2,
    virtualUser3,
    virtualUser4;
  
  /** Admin users */
  private AdminUser adminUser1,adminUser2,adminUser3,adminUser4;

  /** Database backend user. */
  private DatabaseBackendUser realUser1, realUser2, realUser3, realUser4;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    // create virtual database users
    virtualUser1 = new VirtualDatabaseUser("virtualLogin1", "userPassword1");
    virtualUser2 = new VirtualDatabaseUser("virtualLogin2", "userPassword2");
    virtualUser3 = new VirtualDatabaseUser("virtualLogin3", "userPassword3");
    virtualUser4 = new VirtualDatabaseUser("virtualLogin4", "userPassword4");

    // create admin users
    adminUser1 = new AdminUser("admin1","admin");
    adminUser2 = new AdminUser("admin2","admin");
    adminUser3 = new AdminUser("admin3","admin");
    adminUser4 = new AdminUser("admin4","admin");
    
    // create database backend users
    realUser1 =
      new DatabaseBackendUser("backend1", "realLogin1", "userPassword1");
    realUser2 =
      new DatabaseBackendUser("backend1", "realLogin2", "userPassword2");
    realUser3 =
      new DatabaseBackendUser("backend2", "realLogin3", "userPassword3");
    realUser4 =
      new DatabaseBackendUser("backend2", "realLogin4", "userPassword4");

    // create the authentication manager
    manager = new AuthenticationManager();
  }

  /**
   * @see org.objectweb.cjdbc.controller.authentication.AuthenticationManager#isValidAdminUser(AdminUser)
   */
  public void testIsValidAdminUser()
  {
    // no admin user set
    assertFalse(manager.isValidAdminUser(adminUser1));
    assertFalse(manager.isValidAdminUser(adminUser2));
    assertFalse(manager.isValidAdminUser(adminUser3));
    assertFalse(manager.isValidAdminUser(adminUser4));

    // set admin user
    manager.addAdminUser(adminUser1);
    assertTrue(manager.isValidAdminUser(adminUser1));
    assertFalse(manager.isValidAdminUser(adminUser2));
    assertFalse(manager.isValidAdminUser(adminUser3));
    assertFalse(manager.isValidAdminUser(adminUser4));

    // remplace admin user by another
    manager.addAdminUser(adminUser2);
    assertTrue(manager.isValidAdminUser(adminUser1));
    assertTrue(manager.isValidAdminUser(adminUser2));
    assertFalse(manager.isValidAdminUser(adminUser3));
    assertFalse(manager.isValidAdminUser(adminUser4));
  }

  /**
   * @see org.objectweb.cjdbc.controller.authentication.AuthenticationManager#isValidVirtualUser(VirtualDatabaseUser)
   */
  public void testIsValidVirtualUser()
  {
    // add virtualUser1 and virtualUser2 to the manager
    manager.addVirtualUser(virtualUser1);
    manager.addVirtualUser(virtualUser2);

    assertTrue(manager.isValidVirtualUser(virtualUser1));
    assertTrue(manager.isValidVirtualUser(virtualUser2));
    assertFalse(manager.isValidVirtualUser(virtualUser3));
    assertFalse(manager.isValidVirtualUser(virtualUser4));
  }

  /**
   * @see org.objectweb.cjdbc.controller.authentication.AuthenticationManager#isValidVirtualLogin(String)
   */
  public void testIsValidVirtualLogin()
  {
    // add virtualUser1 and virtualUser2 to the manager
    manager.addVirtualUser(virtualUser1);
    manager.addVirtualUser(virtualUser2);

    assertTrue(manager.isValidVirtualLogin("virtualLogin1"));
    assertTrue(manager.isValidVirtualLogin("virtualLogin2"));
    assertFalse(manager.isValidVirtualLogin("virtualLogin3"));
    assertFalse(manager.isValidVirtualLogin("virtualLogin4"));
  }

  /**
   * @see org.objectweb.cjdbc.controller.authentication.AuthenticationManager#addRealUser(String,
   *      DatabaseBackendUser)
   */
  public void testAddRealUser()
  {
    try
    {
      // virtualLogin1 is mapped to realLogin1 on backend1 and realLogin3 on
      // backend2
      manager.addRealUser("virtualLogin1", realUser1);
      manager.addRealUser("virtualLogin1", realUser3);

      // virtualLogin2 is mapped to realLogin2 (backend1)
      manager.addRealUser("virtualLogin2", realUser2);
    }
    catch (AuthenticationManagerException e)
    {
      fail("Exception thrown: " + e);
    }

    assertEquals(
      realUser1,
      manager.getDatabaseBackendUser("virtualLogin1", "backend1"));
    assertEquals(
      realUser3,
      manager.getDatabaseBackendUser("virtualLogin1", "backend2"));
    assertEquals(
      realUser2,
      manager.getDatabaseBackendUser("virtualLogin2", "backend1"));
    assertNull(manager.getDatabaseBackendUser("virtualLogin2", "backend2"));

    // try to add another real user on backend backend1 for virtual login
    // virtualLogin2
    try
    {
      manager.addRealUser("virtualLogin2", realUser1);
      fail("Exception not thrown while trying to add two real users on the same backend for the same virtual login");
    }
    catch (AuthenticationManagerException e)
    {
    }

    // map virtualLogin2 to realLogin4 on backend2
    try
    {
      manager.addRealUser("virtualLogin2", realUser4);
    }
    catch (AuthenticationManagerException e)
    {
      fail("Exception thrown: " + e);
    }

    assertEquals(
      realUser1,
      manager.getDatabaseBackendUser("virtualLogin1", "backend1"));
    assertEquals(
      realUser3,
      manager.getDatabaseBackendUser("virtualLogin1", "backend2"));
    assertEquals(
      realUser2,
      manager.getDatabaseBackendUser("virtualLogin2", "backend1"));
    assertEquals(
      realUser4,
      manager.getDatabaseBackendUser("virtualLogin2", "backend2"));
  }
}