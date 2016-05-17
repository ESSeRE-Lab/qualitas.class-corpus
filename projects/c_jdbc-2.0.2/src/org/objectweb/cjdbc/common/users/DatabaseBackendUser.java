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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.common.users;

/**
 * A <code>DatabaseBackendUser</code> is a login/password combination to
 * represent a database backend user.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public class DatabaseBackendUser extends AbstractDatabaseUser
{
  private static final long serialVersionUID = 92260597820622650L;

  /** Backend logical name. */
  private String            backendName;

  /**
   * Creates a new <code>DatabaseBackendUser</code> instance. The caller must
   * ensure that the parameters are not <code>null</code>.
   * 
   * @param backendName the backend logical name.
   * @param login the user name.
   * @param password the password.
   */
  public DatabaseBackendUser(String backendName, String login, String password)
  {
    super(login, password);
    this.backendName = backendName;
  }

  /**
   * Returns the backend logical name.
   * 
   * @return the backend logical name.
   */
  public String getBackendName()
  {
    return backendName;
  }

  /**
   * Tests if the login and password provided matches the login/password of this
   * object.
   * 
   * @param backendName backend logical name
   * @param login a user name
   * @param password a password
   * @return <code>true</code> if it matches this object's login/password
   */
  public boolean matches(String backendName, String login, String password)
  {
    return (super.matches(login, password) && this.backendName
        .equals(backendName));
  }

  /**
   * Two <code>DatabaseBackendUser</code> are equals if both objects have the
   * same login & password.
   * 
   * @param other the object to compare with.
   * @return <code>true</code> if both objects have the same login & password.
   */
  public boolean equals(Object other)
  {
    if ((other == null) || !(other instanceof DatabaseBackendUser))
      return false;

    DatabaseBackendUser user = (DatabaseBackendUser) other;
    return (super.matches(user.login, user.password) && backendName
        .equals(user.backendName));
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    return "";
    // return "<"
    // + DatabasesXmlTags.ELT_RealLogin
    // + " "
    // + DatabasesXmlTags.ATT_backendName
    // + "=\""
    // + getBackendName()
    // + "\" "
    // + DatabasesXmlTags.ATT_rLogin
    // + "=\""
    // + getLogin()
    // + "\" "
    // + DatabasesXmlTags.ATT_rPassword
    // + "=\""
    // + getPassword()
    // + "\"/>";
  }
}
