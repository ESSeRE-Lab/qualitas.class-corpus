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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.users;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This class defines a AdminUser
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class AdminUser extends AbstractDatabaseUser
{
  private static final long serialVersionUID = 5080497103560621226L;

  /**
   * Creates a new <code>AdminUser</code> instance. The caller must ensure
   * that the parameters are not <code>null</code>.
   * 
   * @param login the user name.
   * @param password the password.
   */
  public AdminUser(String login, String password)
  {
    super(login, password);
  }

  /**
   * @see org.objectweb.cjdbc.common.users.AbstractDatabaseUser#getXml()
   */
  public String getXml()
  {
    return "<" + DatabasesXmlTags.ELT_User + " "
        + DatabasesXmlTags.ATT_username + "=\"" + this.getName() + "\" "
        + DatabasesXmlTags.ATT_password + "=\"" + this.getPassword() + "\" />";
  }
}
