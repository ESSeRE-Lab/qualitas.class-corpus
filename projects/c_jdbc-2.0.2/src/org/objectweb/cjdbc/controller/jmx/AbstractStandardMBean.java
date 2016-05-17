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

import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.StandardMBean;

import org.objectweb.cjdbc.common.i18n.JmxTranslate;

/**
 * This class defines a AbstractStandardMBean
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public abstract class AbstractStandardMBean extends StandardMBean
    implements
      NotificationEmitter
{
  /**
   * the broadcaster instance we write a wrapper for
   */
  private transient NotificationBroadcasterSupport broadcaster;

  /**
   * @see javax.management.NotificationBroadcaster#addNotificationListener(javax.management.NotificationListener,
   *      javax.management.NotificationFilter, java.lang.Object)
   */
  public void addNotificationListener(NotificationListener listener,
      NotificationFilter filter, Object handback)
  {
    broadcaster.addNotificationListener(listener, filter, handback);
  }

  /**
   * @see javax.management.NotificationBroadcaster#getNotificationInfo()
   */
  public MBeanNotificationInfo[] getNotificationInfo()
  {
    // is the broadcaster already initialized ?
    if (broadcaster == null)
      // no we return empty array
      return new MBeanNotificationInfo[0];

    return broadcaster.getNotificationInfo();
  }

  /**
   * @see javax.management.NotificationBroadcaster#removeNotificationListener(javax.management.NotificationListener)
   */
  public void removeNotificationListener(NotificationListener listener)
      throws ListenerNotFoundException
  {
    broadcaster.removeNotificationListener(listener);
  }

  /**
   * @see javax.management.NotificationEmitter#removeNotificationListener(javax.management.NotificationListener,
   *      javax.management.NotificationFilter, java.lang.Object)
   */
  public void removeNotificationListener(NotificationListener listener,
      NotificationFilter filter, Object handback)
      throws ListenerNotFoundException
  {
    broadcaster.removeNotificationListener(listener, filter, handback);
  }

  /**
   * Sends a notification.
   * 
   * @param notification The notification to send.
   */
  public void sendNotification(Notification notification)
  {
    broadcaster.sendNotification(notification);
  }

  /*****************************************************************************
   * StandardMBean methods
   ****************************************************************************/

  /**
   * Creates a new <code>AbstractStandardMBean.java</code> object
   * 
   * @param mbeanInterface The Management Interface exported by this MBean.
   * @throws NotCompliantMBeanException - if the mbeanInterface does not follow
   *           JMX design patterns for Management Interfaces, or if this does
   *           not implement the specified interface.
   */
  public AbstractStandardMBean(Class mbeanInterface)
      throws NotCompliantMBeanException
  {
    super(mbeanInterface);
    broadcaster = new NotificationBroadcasterSupport();
  }

  /**
   * Allow to retrieve internationalization description on mbeans as well
   * 
   * @return part of the key to look for in the translation file.
   */
  public abstract String getAssociatedString();

  /**
   * Returns the description of the MBean.
   * 
   * @return a <code>String</code> containing the description
   */
  protected String getDescription(MBeanInfo info)
  {
    return JmxTranslate.get("mbean." + getAssociatedString() + ".description");
  }

  /**
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanConstructorInfo)
   */
  protected String getDescription(MBeanConstructorInfo ctor)
  {
    return JmxTranslate.get("mbean." + getAssociatedString() + ".constructor."
        + ctor.getSignature().length);
  }

  /**
   * @see javax.management.StandardMBean#getParameterName(javax.management.MBeanConstructorInfo,
   *      javax.management.MBeanParameterInfo, int)
   */
  protected String getParameterName(MBeanConstructorInfo ctor,
      MBeanParameterInfo param, int sequence)
  {
    return JmxTranslate.get("mbean." + getAssociatedString() + ".constructor."
        + ctor.getSignature().length + ".parameter.name." + sequence);
  }

  /**
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanConstructorInfo,
   *      javax.management.MBeanParameterInfo, int)
   */
  protected String getDescription(MBeanConstructorInfo ctor,
      MBeanParameterInfo param, int sequence)
  {
    return JmxTranslate.get("mbean." + getAssociatedString() + ".constructor."
        + ctor.getSignature().length + ".parameter.description." + sequence);
  }

  /**
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanAttributeInfo)
   */
  protected String getDescription(MBeanAttributeInfo info)
  {
    return JmxTranslate.get("mbean." + getAssociatedString() + ".attribute."
        + info.getName());
  }

  /**
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanOperationInfo)
   */
  protected String getDescription(MBeanOperationInfo info)
  {
    return JmxTranslate.get("mbean." + getAssociatedString() + "."
        + info.getName());
  }

  /**
   * @see javax.management.StandardMBean#getParameterName(javax.management.MBeanOperationInfo,
   *      javax.management.MBeanParameterInfo, int)
   */
  protected String getParameterName(MBeanOperationInfo op,
      MBeanParameterInfo param, int sequence)
  {
    return JmxTranslate.get("mbean." + getAssociatedString() + "."
        + op.getName() + ".parameter.name." + sequence);
  }

  /**
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanOperationInfo,
   *      javax.management.MBeanParameterInfo, int)
   */
  protected String getDescription(MBeanOperationInfo op,
      MBeanParameterInfo param, int sequence)
  {
    return JmxTranslate.get("mbean." + getAssociatedString() + "."
        + op.getName() + ".parameter.description." + sequence);
  }
}