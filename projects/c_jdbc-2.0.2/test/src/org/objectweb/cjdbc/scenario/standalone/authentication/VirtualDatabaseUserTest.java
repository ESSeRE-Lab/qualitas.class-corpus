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

import org.objectweb.cjdbc.common.users.VirtualDatabaseUser;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * <code>VirtualDatabaseUser</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * @see org.objectweb.cjdbc.common.users.VirtualDatabaseUser
 */
public class VirtualDatabaseUserTest extends NoTemplate
{
  /** Virtual databse user. */
  private VirtualDatabaseUser user1, user2, user3, user4;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    user1 = new VirtualDatabaseUser("login1", "password1");
    user2 = new VirtualDatabaseUser("login1", "password1");
    user3 = new VirtualDatabaseUser("login2", "password");
    user4 = new VirtualDatabaseUser("login1", "password2");
  }

  /**
   * @see org.objectweb.cjdbc.common.users.VirtualDatabaseUser#matches(String,
   *      String)
   */
  public void testMatches()
  {
    assertTrue(user1.matches("login1", "password1"));
    assertFalse(user1.matches("login2", "password1"));
    assertFalse(user1.matches("login1", "password2"));
    assertFalse(user1.matches("", ""));
    assertFalse(user1.matches(null, "password2"));
    assertFalse(user1.matches("login1", null));
    assertFalse(user1.matches(null, null));
  }

  /**
   * @see org.objectweb.cjdbc.common.users.VirtualDatabaseUser#equals(Object)
   */
  public void testEquals()
  {
    assertTrue(user1.equals(user2));
    assertFalse(user1.equals(user3));
    assertFalse(user1.equals(user4));
  }
}
