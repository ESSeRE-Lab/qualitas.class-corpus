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

import org.objectweb.cjdbc.common.users.DatabaseBackendUser;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * <code>DatabaseBackendUser</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * @see org.objectweb.cjdbc.common.users.DatabaseBackendUser
 */
public class DatabaseBackendUserTest extends NoTemplate
{
  DatabaseBackendUser user1, user2, user3, user4, user5;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    user1 = new DatabaseBackendUser("backend1", "login1", "password1");
    user2 = new DatabaseBackendUser("backend1", "login1", "password1");
    user3 = new DatabaseBackendUser("backend2", "login1", "password1");
    user4 = new DatabaseBackendUser("backend1", "login2", "password1");
    user5 = new DatabaseBackendUser("backend1", "login1", "password2");
  }

  /**
   * @see org.objectweb.cjdbc.common.users.DatabaseBackendUser#matches(String,
   *      String, String)
   */
  public void testMatches()
  {
    assertTrue(user1.matches("backend1", "login1", "password1"));
    assertFalse(user1.matches("backend1", "login2", "password1"));
    assertFalse(user1.matches("backend1", "login1", "password2"));
    assertFalse(user1.matches("backend2", "login1", "password2"));
    assertFalse(user1.matches("", "", ""));
    assertFalse(user1.matches(null, "login1", "password"));
    assertFalse(user1.matches("backend1", null, "password"));
    assertFalse(user1.matches("backend1", "login1", null));
    assertFalse(user1.matches(null, null, null));
  }

  /**
   * @see org.objectweb.cjdbc.common.users.DatabaseBackendUser#equals(Object)
   */
  public void testEquals()
  {
    assertEquals(user1, user1);
    assertEquals(user1, user2);
    assertFalse(user1.equals(user3));
    assertFalse(user1.equals(user4));
    assertFalse(user1.equals(user4));
  }
}
