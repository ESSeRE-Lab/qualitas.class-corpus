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
 * Initial developer(s): Julie Marguerite.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.driver;

import java.io.Serializable;

/**
 * A <code>DatabaseUser</code> is just a login/password combination to
 * represent database user.
 * 
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite</a>
 * @author <a href="Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * @version 1.0
 */
public class DatabaseUser implements Serializable
{
  private static final long serialVersionUID = 4733183236454586066L;

  /** Virtual database name. */
  private String            dbName;

  /** User name. */
  private String            login;

  /** Password. */
  private String            password;

  /**
   * Creates a new <code>DatabaseUser</code> instance.
   * 
   * @param dbName The virtual database name
   * @param login User name
   * @param password Password
   */
  public DatabaseUser(String dbName, String login, String password)
  {
    this.dbName = dbName;
    this.login = login;
    this.password = password;
  }

  /**
   * Tests if the virtual database name login and password provided matches the
   * virtual database name/login/password of this object.
   * 
   * @param dbName virtual database name
   * @param login a user name
   * @param password a password
   * @return <code>true</code> if it matches this object's virtual database
   *         name/login/password
   */
  public boolean matches(String dbName, String login, String password)
  {
    return (this.dbName.equals(dbName) && this.login.equals(login) && this.password
        .equals(password));
  }

  /**
   * Compares an object with this object.
   * 
   * @param other an <code>Object</code>
   * @return <code>true</code> if both objects have same virtual database
   *         name, login and password
   */
  public boolean equals(Object other)
  {
    if (!(other instanceof DatabaseUser))
      return false;

    DatabaseUser castOther = (DatabaseUser) other;
    return matches(castOther.dbName, castOther.login, castOther.password);
  }

  /**
   * Returns the virtual database name.
   * 
   * @return database name
   */
  public String getDbName()
  {
    return dbName;
  }

  /**
   * Gets the login name.
   * 
   * @return login name
   */
  public String getLogin()
  {
    return login;
  }

  /**
   * Gets the password.
   * 
   * @return password
   */
  public String getPassword()
  {
    return password;
  }
}
