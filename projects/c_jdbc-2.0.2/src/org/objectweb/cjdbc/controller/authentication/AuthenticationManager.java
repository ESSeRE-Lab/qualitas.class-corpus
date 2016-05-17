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
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.controller.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.users.AdminUser;
import org.objectweb.cjdbc.common.users.DatabaseBackendUser;
import org.objectweb.cjdbc.common.users.VirtualDatabaseUser;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * The <code>AuthenticationManager</code> manages the mapping between virtual
 * login/password (to the <code>VirtualDatabase</code>) and the real
 * login/password for each backend.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class AuthenticationManager
{
  /*
   * How the code is organized ? 1. Member variables 2. Constructor(s) 3.
   * Login/Password checking functions 4. Getter/Setter (possibly in
   * alphabetical order) 5. Xml
   */

  /** <code>ArrayList</code> of <code>VirtualDatabaseUser</code> objects. */
  private ArrayList           virtualLogins;

  /** <code>ArrayList</code> of <code>AdminUser</code> objects. */
  private ArrayList           adminUsers;

 
  /**
   * <code>HashMap</code> of <code>HashMap</code> of <code>DatabaseBackendUser</code>
   * objects hashed by the backend name, hashed by their virtual database
   * login. A virtual user can have several real logins, but has only one real
   * login for a given backend.
   */
  private HashMap             realLogins;

  /*
   * Constructor(s)
   */

  /**
   * Creates a new <code>AuthenticationManager</code> instance.
   */
  public AuthenticationManager()
  {
    virtualLogins = new ArrayList();
    adminUsers = new ArrayList();
    realLogins = new HashMap();
  }

  /*
   * Login/Password checking functions
   */

  /**
   * Checks whether this administrator user has been registered to this <code>AuthenticationManager</code>
   * or not. Returns <code>false</code> if no admin user has been set.
   * 
   * @param user administrator user login/password to check.
   * @return <code>true</code> if it matches the registered admin user.
   */
  public boolean isValidAdminUser(AdminUser user)
  {
    return adminUsers.contains(user);
  }

  /**
   * Checks whether a given virtual database user has been registered to this
   * <code>AuthenticationManager</code> or not.
   * 
   * @param vUser the virtual database user.
   * @return <code>true</code> if the user login/password is valid.
   */
  public boolean isValidVirtualUser(VirtualDatabaseUser vUser)
  {
    return virtualLogins.contains(vUser);
  }

  /**
   * Checks whether a given virtual login has been registered to this <code>AuthenticationManager</code>
   * or not.
   * 
   * @param vLogin the virtual database login.
   * @return <code>true</code> if the virtual database login is valid.
   */
  public boolean isValidVirtualLogin(String vLogin)
  {
    Iterator iter = virtualLogins.iterator();
    VirtualDatabaseUser u;
    while (iter.hasNext())
    {
      u = (VirtualDatabaseUser) iter.next();
      if (u.getLogin().equals(vLogin))
      {
        return true;
      }
    }
    return false;
  }

  /*
   * Getter/Setter
   */

  /**
   * Sets the administrator user.
   * 
   * @param adminUser the administor user to set.
   */
  //  public void setAdminUser(VirtualDatabaseUser adminUser)
  //  {
  //    this.adminUser = adminUser;
  //  }
  /**
   * Registers a new virtual database user.
   * 
   * @param vUser the <code>VirtualDatabaseUser</code> to register.
   */
  public synchronized void addVirtualUser(VirtualDatabaseUser vUser)
  {
    virtualLogins.add(vUser);
  }

  /**
   * Associates a new database backend user to a virtual database login.
   * 
   * @param vLogin the virtual database login.
   * @param rUser the database backend user to add.
   * @exception AuthenticationManagerException if a real user already exists
   *                for this backend.
   */
  public void addRealUser(String vLogin, DatabaseBackendUser rUser)
      throws AuthenticationManagerException
  {
    HashMap list = (HashMap) realLogins.get(vLogin);
    if (list == null)
    {
      list = new HashMap();
      list.put(rUser.getBackendName(), rUser);
      realLogins.put(vLogin, list);
    }
    else
    {
      DatabaseBackendUser u = (DatabaseBackendUser) list.get(rUser
          .getBackendName());
      if (u != null)
        throw new AuthenticationManagerException(
            Translate.get("authentication.failed.add.user.already.exists",
                new String[]{rUser.getLogin(), vLogin, rUser.getBackendName(),
                    u.getLogin()}));
      list.put(rUser.getBackendName(), rUser);
    }
  }

  /**
   * Gets the <code>DatabaseBackendUser</code> given a virtual database login
   * and a database backend logical name.
   * 
   * @param vLogin virtual database login.
   * @param backendName database backend logical name.
   * @return a <code>DatabaseBackendUser</code> value or <code>null</code>
   *         if not found.
   */
  public DatabaseBackendUser getDatabaseBackendUser(String vLogin,
      String backendName)
  {
    Object list = realLogins.get(vLogin);
    if (list == null)
      return null;
    else
      return (DatabaseBackendUser) ((HashMap) list).get(backendName);
  }

  /**
   * @return Returns the adminUser.
   */
  //  public VirtualDatabaseUser getAdminUser()
  //  {
  //    return adminUser;
  //  }
  /**
   * @return Returns the realLogins.
   */
  public HashMap getRealLogins()
  {
    return realLogins;
  }

  /**
   * @return Returns the virtualLogins.
   */
  public ArrayList getVirtualLogins()
  {
    return virtualLogins;
  }

  /*
   * 5. Xml
   */
  /**
   * Format to xml
   * 
   * @return xml formatted representation
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_AuthenticationManager + ">");
    info.append("<" + DatabasesXmlTags.ELT_Admin + ">");
    for (int i = 0; i < adminUsers.size(); i++)
    {
      AdminUser vu = (AdminUser) adminUsers.get(i);
      info.append(vu.getXml());          
    }
    info.append("</" + DatabasesXmlTags.ELT_Admin + ">");

    info.append("<" + DatabasesXmlTags.ELT_VirtualUsers + ">");
    for (int i = 0; i < virtualLogins.size(); i++)
    {
      VirtualDatabaseUser vu = (VirtualDatabaseUser) virtualLogins.get(i);
      info.append(vu.getXml());
    }
    info.append("</" + DatabasesXmlTags.ELT_VirtualUsers + ">");
    info.append("</" + DatabasesXmlTags.ELT_AuthenticationManager + ">");
    return info.toString();
  }

  /**
   * Add an admin user for this authentication manager.
   * 
   * @param user the <code>AdminUser</code> to add to this <code>AuthenticationManager</code>
   */
  public void addAdminUser(AdminUser user)
  {
    adminUsers.add(user);
  }

  /**
   * Remove an admin user from the admin list
   * 
   * @param user the admin to remove
   * @return <code>true</code> if was removed.
   */
  public boolean removeAdminUser(AdminUser user)
  {
    return adminUsers.remove(user);
  }
  
   /**
   * @return Returns the adminUsers.
   */
  public ArrayList getAdminUsers()
  {
    return adminUsers;
  }

}
