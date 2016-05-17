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
 * Contributor(s): _______________________.
 */

package org.objectweb.cjdbc.common.jmx;

import javax.management.ObjectName;

/**
 * This class contains static information on the jmx services.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public final class JmxConstants
{
  /** Overall Debug tag for Jmx calls */
  public static final boolean DEBUG                      = false;

  /** Keep connection alive ? */
  public static final boolean KEEP_CONNECTION_ALIVE      = true;

  /** Default domain name for JMX */
  public static final String  JMX_DEFAULT_DOMAIN_NAME    = "jmx";
  /** Default Jmx type */
  public static final String  JMX_DEFAULT_MBEAN_TYPE     = "mbean";

  /** Reference name for Jndi */
  public static final String  JndiName                   = "jrmp";

  /** The default jmx name for the agent to connect to */
  public static final String  DEFAULT_JMX_AGENT_NAME     = "default";

  /** RMI Adaptor */
  public static final String  ADAPTOR_TYPE_RMI           = "rmiAdaptor";

  /** ssl config for rmi */
  public static final String  CONNECTOR_RMI_SSL          = "jmx.rmi.ssl";

  /** Http adaptor */
  public static final String  ADAPTOR_TYPE_HTTP          = "httpAdaptor";

  /** jmx authenticator username */
  public static final String  CONNECTOR_AUTH_USERNAME    = "jmx.auth.username";
  /** jmx authenticator password */
  public static final String  CONNECTOR_AUTH_PASSWORD    = "jmx.auth.password";

  /** Default RMI port number value. */
  public static final int     DEFAULT_JMX_RMI_PORT       = 1090;

  /** Default JMX server HTTP adaptor port value. */
  public static final int     DEFAULT_JMX_HTTP_PORT      = 8090;

  /**
   * This is in the xsl transformation file, so we should leave as is. Other
   * domain are filtered.
   */
  public static final String  CJDBC_DOMAIN_NAME          = "c-jdbc";

  /** the controller mbean type */
  public static final String  CJDBC_TYPE_CONTROLLER      = "controller";
  /** the virtual database mbean type */
  public static final String  CJDBC_TYPE_VIRTUALDATABASE = "virtualdatabase";
  /** the data collector mbean type */
  public static final String  CJDBC_TYPE_DATACOLLECTOR   = "datacollector";
  /** the backend mbean type */
  public static final String  CJDBC_TYPE_BACKEND         = "backend";
  /** the recovery log mbean type */
  private static final String CJDBC_TYPE_RECOVERYLOG     = "recoverylog";
  /** the cache mbean type */
  private static final String CJDBC_TYPE_CACHE           = "cache";
  /** the request manager mbean type */
  private static final String CJDBC_TYPE_REQUEST_MANAGER = "requestmanager";

  private static final String CJDBC_TYPE_LOAD_BALANCER   = "loadbalancer";      ;

  /**
   * Get the associated jmx object name
   * 
   * @param name the name of the mbean
   * @param type the c-jdbc type of the mbean
   * @return the associated object name, no exception is thrown as the object
   *         name calculated is always valid ex;
   *         c-jdbc:type:=&lt;type&gt;:name:=&lt;name&gt;
   */
  public static ObjectName getJmxObjectName(String name, String type)
  {
    try
    {
      return new ObjectName(CJDBC_DOMAIN_NAME + ":type=" + type + ",name="
          + name);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      // impossible?
      return null;
    }
  }

  /**
   * Get the associated controller object name
   * 
   * @return c-jdbc:type:=&lt;controller&gt;:name:=&lt;name&gt;
   */
  public static ObjectName getControllerObjectName()
  {
    return getJmxObjectName(CJDBC_TYPE_CONTROLLER, CJDBC_TYPE_CONTROLLER);
  }

  /**
   * Get the associated virtualdatabase object name
   * 
   * @param name the name of the virtualdatabase
   * @return c-jdbc:type:=&lt;virtualdatabase&gt;:name:=&lt;name&gt;
   */
  public static ObjectName getVirtualDbObjectName(String name)
  {
    return getJmxObjectName(name, CJDBC_TYPE_VIRTUALDATABASE);
  }

  /**
   * Get the associated data collector object name
   * 
   * @return c-jdbc:type:=&lt;datacollector&gt;:name:=&lt;name&gt;
   */
  public static ObjectName getDataCollectorObjectName()
  {
    return getJmxObjectName(CJDBC_TYPE_DATACOLLECTOR, CJDBC_TYPE_DATACOLLECTOR);
  }

  /**
   * Get the associated data collector object name
   * 
   * @param vdbName name of the virtual database
   * @param name name of the backend
   * @return c-jdbc:type:=&lt;datacollector&gt;:name:=&lt;name&gt;
   */
  public static ObjectName getDatabaseBackendObjectName(String vdbName,
      String name)
  {
    return getJmxObjectName(vdbName + "--" + name, CJDBC_TYPE_BACKEND);
  }

  /**
   * Get the associated recovery log object name
   * 
   * @param vdbName name of the virtual database
   * @return c-jdbc:type:=&lt;recoverylog&gt;:name:=&lt;name&gt;
   */
  public static ObjectName getRecoveryLogObjectName(String vdbName)
  {
    return getJmxObjectName(vdbName + "--recoverylog", CJDBC_TYPE_RECOVERYLOG);
  }

  /**
   * Get the associated cache object name
   * 
   * @param vdbName name of the virtual database
   * @return c-jdbc:type:=&lt;cache&gt;:name:=&lt;name&gt;
   */
  public static ObjectName getCacheObjectName(String vdbName)
  {
    return getJmxObjectName(vdbName + "--cache", CJDBC_TYPE_CACHE);
  }

  /**
   * Get the associated request manager object name
   * 
   * @param vdbName name of the virtual database
   * @return c-jdbc:type:=&lt;requestmanager&gt;:name:=&lt;name&gt;
   */
  public static ObjectName getRequestManagerObjectName(String vdbName)
  {
    return getJmxObjectName(vdbName + "--requestmanager",
        CJDBC_TYPE_REQUEST_MANAGER);
  }

  /**
   * Retrieve the owning database objectname of this backend's objectname
   * 
   * @param backend the objectname of the backend
   * @return the objectname of the owning database.
   */
  public static ObjectName getVirtualDbObjectNameFromBackend(ObjectName backend)
  {
    String name = backend.toString();
    int ind1 = name.indexOf("name=") + 5;
    int ind2 = name.indexOf("--", ind1);
    String vdbName = name.substring(ind1, ind2);
    return getJmxObjectName(vdbName, CJDBC_TYPE_VIRTUALDATABASE);
  }

  /**
   * Get the associated request manager object name
   * 
   * @param name name of the virtual database
   * @return c-jdbc:type:=&lt;--loadbalancer&gt;:name:=&lt;name&gt;
   */
  public static ObjectName getLoadBalancerObjectName(String name)
  {
    return getJmxObjectName(name + "--loadbalancer", CJDBC_TYPE_LOAD_BALANCER);
  }

  /**
   * C-JDBC rules to determine if a mbean need authentication or not. By default
   * all the mbeans need authentication apart from the controller mbean and the
   * data collector mbean
   * 
   * @param mbean <tt>ObjectName</tt> of the mbean to test
   * @return <tt>true</tt> if the call to the mbean should have a user and a
   *         password attribute attached to it.
   */
  public static boolean mbeanNeedAuthentication(ObjectName mbean)
  {
    return (mbean.toString().indexOf(CJDBC_TYPE_CONTROLLER) == -1 && mbean
        .toString().indexOf(CJDBC_TYPE_DATACOLLECTOR) == -1);
  }

}