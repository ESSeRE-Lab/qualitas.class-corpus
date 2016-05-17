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

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * A <code>VirtualDatabaseUser</code> is a login/password combination to
 * represent a virtual database user.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * @version 1.0
 */
public class VirtualDatabaseUser extends AbstractDatabaseUser
{
  private static final long serialVersionUID = 535330556687836840L;

  /**
   * Creates a new <code>VirtualDatabaseUser</code> instance. The caller must
   * ensure that the parameters are not <code>null</code>.
   * 
   * @param login the user name.
   * @param password the password.
   */
  public VirtualDatabaseUser(String login, String password)
  {
    super(login, password);
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    return "<" + DatabasesXmlTags.ELT_VirtualLogin + " "
        + DatabasesXmlTags.ATT_vLogin + "=\"" + getLogin() + "\" "
        + DatabasesXmlTags.ATT_vPassword + "=\"" + getPassword() + "\"/>";
  }
}
