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

package org.objectweb.cjdbc.controller.jmx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.security.auth.Subject;

import org.objectweb.cjdbc.common.jmx.JmxConstants;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.users.AbstractDatabaseUser;

/**
 * An MBeanServer authenticating all invoke() requests.
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @author <a href="mailto:nicolas.modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class AuthenticatingMBeanServer extends ChainedMBeanServer
{

  /** Logger instance */
  static Trace logger = Trace
                          .getLogger("org.objectweb.cjdbc.controller.jmx.AuthenticatingMBeanServer");

  /**
   * Overridden just to make it public
   * <p>
   * 
   * @see org.objectweb.cjdbc.controller.jmx.ChainedMBeanServer#setMBeanServer(javax.management.MBeanServer)
   */
  public void setMBeanServer(MBeanServer server)
  {
    super.setMBeanServer(server);
  }

  /**
   * @see javax.management.MBeanServerConnection#invoke(javax.management.ObjectName,
   *             java.lang.String, java.lang.Object[], java.lang.String[])
   */
  public Object invoke(ObjectName name, String operationName, Object[] params,
      String[] signature) throws InstanceNotFoundException, MBeanException,
      ReflectionException
  {
    if (JmxConstants.mbeanNeedAuthentication(name)
        && (operationName.equalsIgnoreCase("checkAdminAuthentication") == false))
    {
      // we have to check all methods that access a virtual database
      // except
      // authentication
      boolean authenticationOk = false;
      String username = null;
      String password = null;

      Subject subject = Subject.getSubject(java.security.AccessController
          .getContext());
      if (subject == null || subject.getPrincipals().size() == 0)
      {
        username = (String) params[0];
        password = (String) params[1];
        authenticationOk = authenticate(name, username, password);
        if (!authenticationOk)
          throw new MBeanException(new Exception(
              "Authentication failed (username,password) invalid"));

        if (logger.isDebugEnabled())
          logger
              .debug("Authentication with username and password was successfull");

        // we have to strip the username and password from the params
        // and args
        return super.invoke(name, operationName, cleanO(params),
            cleanS(signature));
      }
      else
      {
        Set principals = subject.getPrincipals(AbstractDatabaseUser.class);
        for (Iterator it = principals.iterator(); it.hasNext();)
        {
          AbstractDatabaseUser user = (AbstractDatabaseUser) it.next();
          username = user.getName();
          password = user.getPassword();
          authenticationOk = authenticate(name, username, password);
          if (authenticationOk)
            break;
        }

        if (principals.size() == 0 && logger.isDebugEnabled())
          throw new MBeanException(new Exception(
              "Authentication failed : no principal"));

        if (!authenticationOk)
          throw new MBeanException(new Exception(
              "Authentication failed : principal invalid"));
        if (logger.isDebugEnabled())
          logger.debug("Authentication with principal was successfull");
        return super.invoke(name, operationName, params, signature);
      }
    }
    else
    {
      if (logger.isDebugEnabled())
        logger.debug("no authentication required");

      return super.invoke(name, operationName, params, signature);
    }
  }

  private boolean authenticate(ObjectName name, String username, String password)
  {
    try
    {
      boolean vdb = name.toString().indexOf(
          JmxConstants.CJDBC_TYPE_VIRTUALDATABASE) != -1;
      if (vdb)
        return ((Boolean) invoke(name, "checkAdminAuthentication",
            new Object[]{username, password}, new String[]{"java.lang.String",
                "java.lang.String"})).booleanValue();
      else
      {
        boolean backend = name.toString().indexOf(
            JmxConstants.CJDBC_TYPE_BACKEND) != -1;
        if (backend)
        {
          // Check with the owning database if the password is right
          ObjectName vdbName = JmxConstants
              .getVirtualDbObjectNameFromBackend(name);
          return ((Boolean) invoke(vdbName, "checkAdminAuthentication",
              new Object[]{username, password}, new String[]{
                  "java.lang.String", "java.lang.String"})).booleanValue();
        }
        else
          // No further check ...
          return true;
      }
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("authentication failed with exception ", e);
      }
      return false;
    }
  }

  private static Object[] cleanO(Object[] params)
  {
    List o = Arrays.asList(params);
    o = o.subList(2, o.size());
    return (new ArrayList(o).toArray());
  }

  private static String[] cleanS(String[] params)
  {
    List o = Arrays.asList(params);
    o = o.subList(2, o.size());
    String[] s = new String[o.size()];
    return (String[]) new ArrayList(o).toArray(s);
  }
}