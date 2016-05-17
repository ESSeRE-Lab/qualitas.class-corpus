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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.jmx;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.monitor.StringMonitor;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.security.auth.Subject;

import org.objectweb.cjdbc.common.exceptions.VirtualDatabaseException;
import org.objectweb.cjdbc.common.jmx.JmxConstants;
import org.objectweb.cjdbc.common.jmx.mbeans.ControllerMBean;
import org.objectweb.cjdbc.common.jmx.mbeans.DataCollectorMBean;
import org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.common.users.AdminUser;
import org.objectweb.cjdbc.controller.authentication.PasswordAuthenticator;

/**
 * This class defines a RmiJmxClient that uses Jmx 2.0 specifications to connect
 * to the RmiSever
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class RmiJmxClient
{
  private JMXConnector         connector;
  private Object               credentials;
  private String               remoteHostAddress;
  private String               remoteHostPort;

  private NotificationListener notificationListener;

  // List of last used MBeans
  private ControllerMBean      controllerMBean;
  private VirtualDatabaseMBean virtualDbMBean;
  private DatabaseBackendMBean backendMBean;
  private DataCollectorMBean   dataMBean;

  /**
   * Returns the notificationListener value.
   * 
   * @return Returns the notificationListener.
   */
  public NotificationListener getNotificationListener()
  {
    return notificationListener;
  }

  /**
   * Sets the notificationListener value.
   * 
   * @param notificationListener The notificationListener to set.
   */
  public void setNotificationListener(NotificationListener notificationListener)
  {
    this.notificationListener = notificationListener;
  }

  /**
   * Returns the credentials value.
   * 
   * @return Returns the credentials.
   */
  public Object getCredentials()
  {
    return credentials;
  }

  /**
   * Creates a new <code>RmiJmxClient.java</code> object
   * 
   * @param port the port of the host to connect to
   * @param host the host name to connect to
   * @param jmxUser the jmxUser if one, to be authenticated with
   * @param jmxPassword the jmxPassword if one, to be authenticated with
   * @throws IOException if cannot connect
   */
  public RmiJmxClient(String port, String host, String jmxUser,
      String jmxPassword) throws IOException
  {
    this(port, host, PasswordAuthenticator.createCredentials(jmxUser,
        jmxPassword));
  }

  /**
   * Creates a new <code>RmiJmxClient</code> object
   * 
   * @param url the jmx connector url
   * @param credentials to use for the connection
   * @throws IOException if connect fails
   */
  public RmiJmxClient(String url, Object credentials) throws IOException
  {
    int index = url.indexOf(":");
    String ip = url.substring(0, index);
    String port = url.substring(index + 1);
    connect(port, ip, credentials);
  }

  /**
   * Creates a new <code>RmiJmxClient.java</code> object
   * 
   * @param port the port of the host to connect to
   * @param host the host name to connect to
   * @param credentials to use for the connection
   * @throws IOException if connect fails
   */
  public RmiJmxClient(String port, String host, Object credentials)
      throws IOException
  {
    connect(port, host, credentials);
  }

  /**
   * Connect to the MBean server
   * 
   * @param port the port of the host to connect to
   * @param host the host name to connect to
   * @param credentials to use for the connection
   * @throws IOException if connect fails
   */
  public void connect(String port, String host, Object credentials)
      throws IOException
  {
    JMXServiceURL address = new JMXServiceURL("rmi", host, 0, "/jndi/jrmp");

    Map environment = new HashMap();
    environment.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.rmi.registry.RegistryContextFactory");
    environment.put(Context.PROVIDER_URL, "rmi://" + host + ":" + port);

    // use username and password for authentication of connections
    // with the controller, the values are compared to the ones
    // specified in the controller.xml config file.
    if (credentials != null)
    {
      // this line is not required if no username/password has been configered
      environment.put(JMXConnector.CREDENTIALS, credentials);
    }

    this.credentials = credentials;

    connector = JMXConnectorFactory.connect(address, environment);
    remoteHostAddress = host;
    remoteHostPort = port;
    invalidateMBeans();
  }

  /**
   * Invalidate all MBeans.
   * 
   * When connecting to a new Controller, all the local
   * MBean instances must be invalidated (since they
   * refered to the previous Controller and its associated
   * MBean server).
   */
  private void invalidateMBeans()
  {
    controllerMBean = null;
    virtualDbMBean = null;
    dataMBean = null;
    backendMBean = null;
  }

  /**
   * List of all the mbean on the current server
   * 
   * @return a set of <tt>ObjectInstance</tt>
   * @throws Exception if fails
   */
  public Set listCJDBCMBeans() throws Exception
  {
    Set set = connector.getMBeanServerConnection().queryMBeans(
        new ObjectName("c-jdbc:*"), null);
    return set;
  }

  /**
   * Get the mbean information
   * 
   * @param mbean the <tt>ObjectName</tt> of the mbean to access
   * @return <tt>MBeanInfo</tt> object
   * @throws Exception if fails
   */
  public MBeanInfo getMBeanInfo(ObjectName mbean) throws Exception
  {
    return connector.getMBeanServerConnection().getMBeanInfo(mbean);
  }

  /**
   * Get the value of an attribute on the given mbean
   * 
   * @param mbean the <tt>ObjectName</tt> of the mbean to access
   * @param attribute the attribute name
   * @return <tt>Object</tt> being the value returned by the get <Attribute>
   *         method
   * @throws Exception if fails
   */
  public Object getAttributeValue(ObjectName mbean, String attribute)
      throws Exception
  {
    return connector.getMBeanServerConnection().getAttribute(mbean, attribute);
  }

  /**
   * Change an attribute value
   * 
   * @param mbean the <tt>ObjectName</tt> of the mbean to access
   * @param attribute the attribute name
   * @param value the attribute new value
   * @throws Exception if fails
   */
  public void setAttributeValue(ObjectName mbean, String attribute, Object value)
      throws Exception
  {
    Attribute att = new Attribute(attribute, value);
    connector.getMBeanServerConnection().setAttribute(mbean, att);
  }

  /**
   * Set the current subject for authentication
   * 
   * @param user the user login
   * @param password the user password
   */
  public void setCurrentSubject(String user, String password)
  {
    if (user != null && password != null)
    {
      // we build a subject for authentication
      AdminUser dbUser = new AdminUser(user, password);
      Set principals = new HashSet();
      principals.add(dbUser);
      subject = new Subject(true, principals, new HashSet(), new HashSet());
    }
  }

  Subject subject;

  /**
   * Has the subject been set?
   * 
   * @return true if the subject is not null
   */
  public boolean isSubjectSet()
  {
    return subject != null;
  }

  /**
   * Invoke an operation on the given object.
   * 
   * @param name object name
   * @param operation operation to invoke
   * @param args method arguments
   * @return result of operation invocation
   * @throws Exception if an error occurs
   */
  public Object invokeOperation(ObjectName name, MBeanOperationInfo operation,
      Object[] args) throws Exception
  {
    if (JmxConstants.mbeanNeedAuthentication(name))
    {
      if (!isSubjectSet())
        throw new Exception(
            "Subject has not been set for this jmx client, and authentication is required");
      return connector.getMBeanServerConnection(subject).invoke(name,
          operation.getName(), args, getSignature(operation));
    }
    else
    {
      return connector.getMBeanServerConnection().invoke(name,
          operation.getName(), args, getSignature(operation));
    }
  }

  private String[] getSignature(MBeanOperationInfo operation)
  {
    MBeanParameterInfo[] info = operation.getSignature();
    String[] signature = new String[info.length];
    for (int i = 0; i < info.length; i++)
      signature[i] = info[i].getType();
    return signature;
  }

  /**
   * Get a reference to the virtualdatabaseMbean with the given authentication
   * 
   * @param database the virtual database name
   * @param user the user recognized as the <code>VirtualDatabaseUser</code>
   * @param password the password for the <code>VirtualDatabaseUser</code>
   * @return <code>VirtualDatabaseMBean</code> instance
   * @throws IOException if cannot connect to MBean
   * @throws InstanceNotFoundException if cannot locate MBean
   * @throws VirtualDatabaseException if virtual database fails
   */
  public VirtualDatabaseMBean getVirtualDatabaseProxy(String database,
      String user, String password) throws InstanceNotFoundException,
      IOException, VirtualDatabaseException
  {
    if (virtualDbMBean != null && isValidConnection()
        && virtualDbMBean.getVirtualDatabaseName().equals(database))
    {
      return virtualDbMBean;
    }
    else
    {
      ObjectName db = JmxConstants.getVirtualDbObjectName(database);

      // we build a subject for authentication
      AdminUser dbUser = new AdminUser(user, password);
      Set principals = new HashSet();
      principals.add(dbUser);
      Subject subj = new Subject(true, principals, new HashSet(), new HashSet());

      // we open a connection for this subject, all subsequent calls with this
      // connection will be executed on the behalf of our subject.
      MBeanServerConnection delegateConnection = connector
          .getMBeanServerConnection(subj);

      // we create a proxy to the virtual database
      VirtualDatabaseMBean local = (VirtualDatabaseMBean) MBeanServerInvocationHandler
          .newProxyInstance(delegateConnection, db, VirtualDatabaseMBean.class,
              false);

      // Check authentication
      boolean authenticated = false;
      try
      {
        authenticated = local.checkAdminAuthentication(user, password);
      }
      catch (Exception e)
      {
        throw new VirtualDatabaseException(
            "Could not check authentication. MBean is not accessible.");
      }
      if (!authenticated)
        throw new VirtualDatabaseException("Authentication Failed");

      // Add notification listener
      if (notificationListener != null)
      {
        delegateConnection.addNotificationListener(db, notificationListener,
            null, null);

        // CounterMonitor cm = new CounterMonitor();
        // cm.setNotify(true);
        // cm.setGranularityPeriod(100);
        // cm.setObservedObject(db);
        // cm.setObservedAttribute("currentNbOfThreads");
        // cm.setThreshold(new Integer(6));
        // cm.start();
      }

      this.virtualDbMBean = local;

      return virtualDbMBean;
    }
  }

  /**
   * Get a proxy to the ControllerMBean
   * 
   * @return <code>ControllerMBean</code> instance
   * @throws IOException if cannot connect to MBean
   */
  public ControllerMBean getControllerProxy() throws IOException
  {

    if (controllerMBean != null && isValidConnection())
    {
      return controllerMBean;
    }
    else
    {
      if (!isValidConnection())
        reconnect();

      ObjectName db = JmxConstants.getControllerObjectName();

      // we create a new proxy to the controller
      controllerMBean = (ControllerMBean) MBeanServerInvocationHandler
          .newProxyInstance(connector.getMBeanServerConnection(), db,
              ControllerMBean.class, false);

      // Add notification listener
      if (notificationListener != null)
      {
        try
        {
          connector.getMBeanServerConnection().addNotificationListener(db,
              notificationListener, null, null);
        }
        catch (Exception e)
        {
          throw new IOException("Could not register listener on the mbean");
        }
      }

      return controllerMBean;
    }
  }

  /**
   * Get a proxy to the DataCollectorMBean
   * 
   * @return <code>DataCollectorMBean</code> instance
   * @throws IOException if fails
   */
  public DataCollectorMBean getDataCollectorProxy() throws IOException
  {

    if (dataMBean != null && isValidConnection())
    {
      return dataMBean;
    }
    else
    {
      if (!isValidConnection())
        reconnect();
      ObjectName db = JmxConstants.getDataCollectorObjectName();

      // we create a new proxy to the data collector
      dataMBean = (DataCollectorMBean) MBeanServerInvocationHandler
          .newProxyInstance(connector.getMBeanServerConnection(), db,
              DataCollectorMBean.class, false);
      return dataMBean;
    }
  }

  /**
   * Get a proxy to the DatabaseBackendMBean
   * 
   * @return <code>DatabaseBackendMBean</code> instance
   * @param vdb virtual database name
   * @param backend backend name
   * @param user user name
   * @param password password name
   * @throws IOException if cannot connect to MBean
   * @throws InstanceNotFoundException if cannot locate MBean
   */
  public DatabaseBackendMBean getDatabaseBackendProxy(String vdb,
      String backend, String user, String password)
      throws InstanceNotFoundException, IOException
  {
    if (backendMBean != null && isValidConnection())
    {
      try
      {
        if (backendMBean.getName().equals(backend))
          return backendMBean;
      }
      catch (Exception e)
      {
        // backend is no more there
      }
    }

    if (!isValidConnection())
      reconnect();

    // we build a subject for authentication
    AdminUser dbUser = new AdminUser(user, password);
    Set principals = new HashSet();
    principals.add(dbUser);
    Subject subj = new Subject(true, principals, new HashSet(), new HashSet());

    ObjectName db = JmxConstants.getDatabaseBackendObjectName(vdb, backend);
    MBeanServerConnection delegateConnection = connector
        .getMBeanServerConnection(subj);

    if (notificationListener != null)
    {
      delegateConnection.addNotificationListener(db, notificationListener,
          null, null);
      StringMonitor sm = new StringMonitor();
      sm.setObservedObject(db);
      sm.setObservedAttribute("LastKnownCheckpoint");
      sm.setStringToCompare("hello");
      sm.setGranularityPeriod(100);
      sm.setNotifyDiffer(true);
      sm.addNotificationListener(notificationListener, null, null);
      sm.start();
    }

    // we create a proxy to the database backend
    backendMBean = (DatabaseBackendMBean) MBeanServerInvocationHandler
        .newProxyInstance(delegateConnection, db, DatabaseBackendMBean.class,
            false);
    return backendMBean;
  }

  /**
   * Get the controller name used for jmx connection This is
   * [hostname]:[jmxServerPort]
   * 
   * @return <code>remoteHostName+":"+remoteHostPort</code>
   */
  public String getRemoteName()
  {
    return remoteHostAddress + ":" + remoteHostPort;
  }

  /**
   * Returns the remoteHostAddress value.
   * 
   * @return Returns the remoteHostAddress.
   */
  public String getRemoteHostAddress()
  {
    return remoteHostAddress;
  }

  /**
   * Returns the remoteHostPort value.
   * 
   * @return Returns the remoteHostPort.
   */
  public String getRemoteHostPort()
  {
    return remoteHostPort;
  }

  /**
   * Reconnect to the same mbean server
   * 
   * @throws IOException if reconnection failed
   */
  public void reconnect() throws IOException
  {
    connect(remoteHostPort, remoteHostAddress, credentials);
  }

  /**
   * Test if the connection with the mbean server is still valid
   * 
   * @return true if it is
   */
  public boolean isValidConnection()
  {
    try
    {
      connector.getMBeanServerConnection().getMBeanCount();
      return true;
    }
    catch (Exception e)
    {
      controllerMBean = null;
      backendMBean = null;
      virtualDbMBean = null;
      dataMBean = null;
      return false;
    }
  }
}