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
 * Initial developer(s): Marc Wick.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.authentication;

import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;

import org.objectweb.cjdbc.common.log.Trace;

/**
 * This class defines a PasswordAuthenticator
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class PasswordAuthenticator implements JMXAuthenticator

{

  /**
   * to enable subject delegation we use a dummy authentication even if none is
   * configured
   */
  public static final PasswordAuthenticator NO_AUTHENICATION = new PasswordAuthenticator(
                                                                 null, null);

  static Trace                              logger           = Trace
                                                                 .getLogger("org.objectweb.cjdbc.controller.authentication");

  private String                            username;
  private String                            password;

  /**
   * Creates a new <code>PasswordAuthenticator.java</code> object
   * 
   * @param username username/loginname
   * @param password password
   */
  public PasswordAuthenticator(String username, String password)
  {
    this.username = username;
    this.password = password;
  }

  /**
   * create a credentials object with the supplied username and password
   * 
   * @param username username
   * @param password password
   * @return credentials Object to be used for authentication,
   */
  public static Object createCredentials(String username, String password)
  {
    return new String[]{username, password};
  }

  /**
   * @see javax.management.remote.JMXAuthenticator#authenticate(java.lang.Object)
   */
  public Subject authenticate(Object credentials) throws SecurityException
  {
    try
    {
      if (username == null && password == null)
      {
        // no authentication is required we return
        return new Subject();
      }

      if (credentials == null)
      {
        throw new SecurityException("credentials are required");
      }

      try
      {
        String[] credentialsArray = (String[]) credentials;
        if (username.equals(credentialsArray[0])
            && password.equals(credentialsArray[1]))
        {
          // username and password are ok
          if (logger.isDebugEnabled())
          {
            logger.debug("successfully authenitcated ");
          }
          return new Subject();
        }
      }
      catch (Exception e)
      {
        // the credentials object makes problems, is was probably not created
        // with the createCredentials method
        throw new SecurityException("problems with credentials object : "
            + e.getMessage());
      }

      // username and password do not match
      throw new SecurityException("invalid credentials");
    }
    catch (SecurityException e)
    {
      logger.error(e.getMessage());
      try
      {
        String clientId = java.rmi.server.RemoteServer.getClientHost();
        logger.warn("refused unauthorized access for client " + clientId);
      }
      catch (Exception ex)
      {

      }
      throw e;
    }
  }
}
