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

import java.io.ObjectInputStream;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;

/**
 * Base class for chained MBeanServers. By default this class delegates all
 * method calls to the nested MBeanServer. Subclass it to add behavior to one or
 * more (or all) methods.
 * <p>
 * This class takes its origin in mx4j.server.ChainedMBeanServer
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class ChainedMBeanServer implements MBeanServer

{
  private MBeanServer mbServer;

  /**
   * Creates a new ChainedMBeanServer that will delegate to an MBeanServer
   * specified using {@link #setMBeanServer}
   */
  public ChainedMBeanServer()
  {
    this(null);
  }

  /**
   * Creates a new ChainedMBeanServer that delegates to the specified
   * <code>MBeanServer</code>.
   * 
   * @param server MBeanServer
   */
  public ChainedMBeanServer(MBeanServer server)
  {
    setMBeanServer(server);
  }

  /**
   * Returns the nested MBeanServer
   */
  protected synchronized MBeanServer getMBeanServer()
  {
    return mbServer;
  }

  protected synchronized void setMBeanServer(MBeanServer server)
  {
    mbServer = server;
  }

  /**
   * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName,
   *      javax.management.NotificationListener,
   *      javax.management.NotificationFilter, java.lang.Object)
   */
  public void addNotificationListener(ObjectName observed,
      NotificationListener listener, NotificationFilter filter, Object handback)
      throws InstanceNotFoundException
  {
    getMBeanServer().addNotificationListener(observed, listener, filter,
        handback);
  }

  /**
   * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName,
   *      javax.management.ObjectName, javax.management.NotificationFilter,
   *      java.lang.Object)
   */
  public void addNotificationListener(ObjectName observed, ObjectName listener,
      NotificationFilter filter, Object handback)
      throws InstanceNotFoundException
  {
    getMBeanServer().addNotificationListener(observed, listener, filter,
        handback);
  }

  /**
   * @see javax.management.MBeanServerConnection#createMBean(java.lang.String,
   *      javax.management.ObjectName)
   */
  public ObjectInstance createMBean(String className, ObjectName objectName)
      throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException, NotCompliantMBeanException
  {
    return getMBeanServer().createMBean(className, objectName);
  }

  /**
   * @see javax.management.MBeanServerConnection#createMBean(java.lang.String,
   *      javax.management.ObjectName, java.lang.Object[], java.lang.String[])
   */
  public ObjectInstance createMBean(String className, ObjectName objectName,
      Object[] args, String[] parameters) throws ReflectionException,
      InstanceAlreadyExistsException, MBeanRegistrationException,
      MBeanException, NotCompliantMBeanException
  {
    return getMBeanServer()
        .createMBean(className, objectName, args, parameters);
  }

  /**
   * @see javax.management.MBeanServerConnection#createMBean(java.lang.String,
   *      javax.management.ObjectName, javax.management.ObjectName)
   */
  public ObjectInstance createMBean(String className, ObjectName objectName,
      ObjectName loaderName) throws ReflectionException,
      InstanceAlreadyExistsException, MBeanRegistrationException,
      MBeanException, NotCompliantMBeanException, InstanceNotFoundException
  {
    return getMBeanServer().createMBean(className, objectName, loaderName);
  }

  /**
   * @see javax.management.MBeanServerConnection#createMBean(java.lang.String,
   *      javax.management.ObjectName, javax.management.ObjectName,
   *      java.lang.Object[], java.lang.String[])
   */
  public ObjectInstance createMBean(String className, ObjectName objectName,
      ObjectName loaderName, Object[] args, String[] parameters)
      throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
      InstanceNotFoundException
  {
    return getMBeanServer().createMBean(className, objectName, loaderName,
        args, parameters);
  }

  /**
   * @see javax.management.MBeanServer#deserialize(java.lang.String, byte[])
   */
  public ObjectInputStream deserialize(String className, byte[] bytes)
      throws OperationsException, ReflectionException
  {
    return getMBeanServer().deserialize(className, bytes);
  }

  /**
   * @see javax.management.MBeanServer#deserialize(java.lang.String,
   *      javax.management.ObjectName, byte[])
   */
  public ObjectInputStream deserialize(String className, ObjectName loaderName,
      byte[] bytes) throws InstanceNotFoundException, OperationsException,
      ReflectionException
  {
    return getMBeanServer().deserialize(className, loaderName, bytes);
  }

  /**
   * @see javax.management.MBeanServer#deserialize(javax.management.ObjectName,
   *      byte[])
   */
  public ObjectInputStream deserialize(ObjectName objectName, byte[] bytes)
      throws InstanceNotFoundException, OperationsException
  {
    return getMBeanServer().deserialize(objectName, bytes);
  }

  /**
   * @see javax.management.MBeanServerConnection#getAttribute(javax.management.ObjectName,
   *      java.lang.String)
   */
  public Object getAttribute(ObjectName objectName, String attribute)
      throws MBeanException, AttributeNotFoundException,
      InstanceNotFoundException, ReflectionException
  {
    return getMBeanServer().getAttribute(objectName, attribute);
  }

  /**
   * @see javax.management.MBeanServerConnection#getAttributes(javax.management.ObjectName,
   *      java.lang.String[])
   */
  public AttributeList getAttributes(ObjectName objectName, String[] attributes)
      throws InstanceNotFoundException, ReflectionException
  {
    return getMBeanServer().getAttributes(objectName, attributes);
  }

  /**
   * @see javax.management.MBeanServerConnection#getDefaultDomain()
   */
  public String getDefaultDomain()
  {
    return getMBeanServer().getDefaultDomain();
  }

  /**
   * @see javax.management.MBeanServerConnection#getDomains()
   */
  public String[] getDomains()
  {
    return getMBeanServer().getDomains();
  }

  /**
   * @see javax.management.MBeanServerConnection#getMBeanCount()
   */
  public Integer getMBeanCount()
  {
    return getMBeanServer().getMBeanCount();
  }

  /**
   * @see javax.management.MBeanServerConnection#getMBeanInfo(javax.management.ObjectName)
   */
  public MBeanInfo getMBeanInfo(ObjectName objectName)
      throws InstanceNotFoundException, IntrospectionException,
      ReflectionException
  {
    return getMBeanServer().getMBeanInfo(objectName);
  }

  /**
   * @see javax.management.MBeanServerConnection#getObjectInstance(javax.management.ObjectName)
   */
  public ObjectInstance getObjectInstance(ObjectName objectName)
      throws InstanceNotFoundException
  {
    return getMBeanServer().getObjectInstance(objectName);
  }

  /**
   * @see javax.management.MBeanServer#instantiate(java.lang.String)
   */
  public Object instantiate(String className) throws ReflectionException,
      MBeanException
  {
    return getMBeanServer().instantiate(className);
  }

  /**
   * @see javax.management.MBeanServer#instantiate(java.lang.String,
   *      java.lang.Object[], java.lang.String[])
   */
  public Object instantiate(String className, Object[] args, String[] parameters)
      throws ReflectionException, MBeanException
  {
    return getMBeanServer().instantiate(className, args, parameters);
  }

  /**
   * @see javax.management.MBeanServer#instantiate(java.lang.String,
   *      javax.management.ObjectName)
   */
  public Object instantiate(String className, ObjectName loaderName)
      throws ReflectionException, MBeanException, InstanceNotFoundException
  {
    return getMBeanServer().instantiate(className, loaderName);
  }

  /**
   * @see javax.management.MBeanServer#instantiate(java.lang.String,
   *      javax.management.ObjectName, java.lang.Object[], java.lang.String[])
   */
  public Object instantiate(String className, ObjectName loaderName,
      Object[] args, String[] parameters) throws ReflectionException,
      MBeanException, InstanceNotFoundException
  {
    return getMBeanServer()
        .instantiate(className, loaderName, args, parameters);
  }

  /**
   * @see javax.management.MBeanServerConnection#invoke(javax.management.ObjectName,
   *      java.lang.String, java.lang.Object[], java.lang.String[])
   */
  public Object invoke(ObjectName objectName, String methodName, Object[] args,
      String[] parameters) throws InstanceNotFoundException, MBeanException,
      ReflectionException
  {
    return getMBeanServer().invoke(objectName, methodName, args, parameters);
  }

  /**
   * @see javax.management.MBeanServerConnection#isInstanceOf(javax.management.ObjectName,
   *      java.lang.String)
   */
  public boolean isInstanceOf(ObjectName objectName, String className)
      throws InstanceNotFoundException
  {
    return getMBeanServer().isInstanceOf(objectName, className);
  }

  /**
   * @see javax.management.MBeanServerConnection#isRegistered(javax.management.ObjectName)
   */
  public boolean isRegistered(ObjectName objectname)
  {
    return getMBeanServer().isRegistered(objectname);
  }

  /**
   * @see javax.management.MBeanServerConnection#queryMBeans(javax.management.ObjectName,
   *      javax.management.QueryExp)
   */
  public Set queryMBeans(ObjectName patternName, QueryExp filter)
  {
    return getMBeanServer().queryMBeans(patternName, filter);
  }

  /**
   * @see javax.management.MBeanServerConnection#queryNames(javax.management.ObjectName,
   *      javax.management.QueryExp)
   */
  public Set queryNames(ObjectName patternName, QueryExp filter)
  {
    return getMBeanServer().queryNames(patternName, filter);
  }

  /**
   * @see javax.management.MBeanServer#registerMBean(java.lang.Object,
   *      javax.management.ObjectName)
   */
  public ObjectInstance registerMBean(Object mbean, ObjectName objectName)
      throws InstanceAlreadyExistsException, MBeanRegistrationException,
      NotCompliantMBeanException
  {
    return getMBeanServer().registerMBean(mbean, objectName);
  }

  /**
   * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
   *      javax.management.NotificationListener)
   */
  public void removeNotificationListener(ObjectName observed,
      NotificationListener listener) throws InstanceNotFoundException,
      ListenerNotFoundException
  {
    getMBeanServer().removeNotificationListener(observed, listener);
  }

  /**
   * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
   *      javax.management.ObjectName)
   */
  public void removeNotificationListener(ObjectName observed,
      ObjectName listener) throws InstanceNotFoundException,
      ListenerNotFoundException
  {
    getMBeanServer().removeNotificationListener(observed, listener);
  }

  /**
   * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
   *      javax.management.ObjectName, javax.management.NotificationFilter,
   *      java.lang.Object)
   */
  public void removeNotificationListener(ObjectName observed,
      ObjectName listener, NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException
  {
    getMBeanServer().removeNotificationListener(observed, listener, filter,
        handback);
  }

  /**
   * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
   *      javax.management.NotificationListener,
   *      javax.management.NotificationFilter, java.lang.Object)
   */
  public void removeNotificationListener(ObjectName observed,
      NotificationListener listener, NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException
  {
    getMBeanServer().removeNotificationListener(observed, listener, filter,
        handback);
  }

  /**
   * @see javax.management.MBeanServerConnection#setAttribute(javax.management.ObjectName,
   *      javax.management.Attribute)
   */
  public void setAttribute(ObjectName objectName, Attribute attribute)
      throws InstanceNotFoundException, AttributeNotFoundException,
      InvalidAttributeValueException, MBeanException, ReflectionException
  {
    getMBeanServer().setAttribute(objectName, attribute);
  }

  /**
   * @see javax.management.MBeanServerConnection#setAttributes(javax.management.ObjectName,
   *      javax.management.AttributeList)
   */
  public AttributeList setAttributes(ObjectName objectName,
      AttributeList attributes) throws InstanceNotFoundException,
      ReflectionException
  {
    return getMBeanServer().setAttributes(objectName, attributes);
  }

  /**
   * @see javax.management.MBeanServerConnection#unregisterMBean(javax.management.ObjectName)
   */
  public void unregisterMBean(ObjectName objectName)
      throws InstanceNotFoundException, MBeanRegistrationException
  {
    getMBeanServer().unregisterMBean(objectName);
  }

  /**
   * @see javax.management.MBeanServer#getClassLoaderFor(javax.management.ObjectName)
   */
  public ClassLoader getClassLoaderFor(ObjectName mbeanName)
      throws InstanceNotFoundException
  {
    return getMBeanServer().getClassLoaderFor(mbeanName);
  }

  /**
   * @see javax.management.MBeanServer#getClassLoader(javax.management.ObjectName)
   */
  public ClassLoader getClassLoader(ObjectName loaderName)
      throws InstanceNotFoundException
  {
    return getMBeanServer().getClassLoader(loaderName);
  }

  /**
   * @see javax.management.MBeanServer#getClassLoaderRepository()
   */
  public ClassLoaderRepository getClassLoaderRepository()
  {
    return getMBeanServer().getClassLoaderRepository();
  }

}
