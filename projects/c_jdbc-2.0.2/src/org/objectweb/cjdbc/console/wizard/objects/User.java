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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.wizard.objects;

import javax.swing.JComponent;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;

/**
 * <code>User</code> class is used to store user name and password and access
 * them from different fields and forms in the wizard
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class User extends JComponent
{
  String password;
  String username = WizardTranslate.get("label.user.undefined");

  /**
   * Returns the password value.
   * 
   * @return Returns the password.
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Sets the password value.
   * 
   * @param password The password to set.
   */
  public void setPassword(String password)
  {
    this.password = password;
  }

  /**
   * Returns the username value.
   * 
   * @return Returns the username.
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Sets the username value.
   * 
   * @param username The username to set.
   */
  public void setUsername(String username)
  {
    this.username = username;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return username;
  }
}