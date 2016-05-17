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

/**
 * This class defines a ConnectionInfo. It includes all the information needed
 * for a connection on a backend.
 * 
 * @see <code>ConnectionTypeInfo</code>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ConnectionInfo
{
  String             rLogin             = "";
  String             rPassword          = "";
  String             urlParameters      = "";
  ConnectionTypeInfo connectionTypeInfo = new ConnectionTypeInfo();

  /**
   * Returns the connectionTypeInfo value.
   * 
   * @return Returns the connectionTypeInfo.
   */
  public ConnectionTypeInfo getConnectionTypeInfo()
  {
    return connectionTypeInfo;
  }

  /**
   * Sets the connectionTypeInfo value.
   * 
   * @param connectionTypeInfo The connectionTypeInfo to set.
   */
  public void setConnectionTypeInfo(ConnectionTypeInfo connectionTypeInfo)
  {
    this.connectionTypeInfo = connectionTypeInfo;
  }

  /**
   * Returns the rLogin value.
   * 
   * @return Returns the rLogin.
   */
  public String getRLogin()
  {
    return rLogin;
  }

  /**
   * Sets the rLogin value.
   * 
   * @param login The rLogin to set.
   */
  public void setRLogin(String login)
  {
    rLogin = login;
  }

  /**
   * Returns the rPassword value.
   * 
   * @return Returns the rPassword.
   */
  public String getRPassword()
  {
    return rPassword;
  }

  /**
   * Sets the rPassword value.
   * 
   * @param password The rPassword to set.
   */
  public void setRPassword(String password)
  {
    rPassword = password;
  }

  /**
   * Returns the urlParameters value.
   * 
   * @return Returns the urlParameters.
   */
  public String getUrlParameters()
  {
    return urlParameters;
  }

  /**
   * Sets the urlParameters value.
   * 
   * @param urlParameters The urlParameters to set.
   */
  public void setUrlParameters(String urlParameters)
  {
    this.urlParameters = urlParameters;
  }
}