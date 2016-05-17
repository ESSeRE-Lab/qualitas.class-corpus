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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.common.users;

import java.io.Serializable;
import java.security.Principal;

/**
 * An <code>AbstractDatabaseUser</code> is just a login/password combination
 * to represent an abstract database user.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public abstract class AbstractDatabaseUser implements Serializable, Principal
{
  /** Login name. */
  protected String login;

  /** Password. */
  protected String password;

  /**
   * Creates a new <code>AbstractDatabaseUser</code> instance. The caller must
   * ensure that the parameters are not <code>null</code>.
   * 
   * @param login the user name.
   * @param password the password.
   */
  protected AbstractDatabaseUser(String login, String password)
  {
    this.login = login;
    this.password = password;
  }

  /**
   * Gets the login name.
   * 
   * @return the login name.
   */
  public String getLogin()
  {
    return login;
  }

  /**
   * Gets the login name.
   * 
   * @return the login name.
   */
  public String getName()
  {
    return getLogin();
  }

  /**
   * Gets the password.
   * 
   * @return the password.
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Tests if the login and password provided matches the login/password of this
   * object.
   * 
   * @param login a user name.
   * @param password a password.
   * @return <code>true</code> if it matches this object's login/password.
   */
  public boolean matches(String login, String password)
  {
    return (this.login.equals(login) && this.password.equals(password));
  }

  /**
   * Two <code>AbstractDatabaseUser</code> are equals if both objects have
   * same login & password.
   * 
   * @param other the object to compare with.
   * @return <code>true</code> if both objects have same login & password.
   */
  public boolean equals(Object other)
  {
    if ((other == null) || !(other instanceof AbstractDatabaseUser))
      return false;

    AbstractDatabaseUser user = (AbstractDatabaseUser) other;
    return matches(user.login, user.password);
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public abstract String getXml();
}
