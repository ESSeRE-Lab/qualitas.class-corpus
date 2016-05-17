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

import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.JmxException;
import org.objectweb.cjdbc.common.log.Trace;

/**
 * The MBeanServerManager (Singleton) creates a single MBeanServer in an JVM.
 * The server can be accessed with the getInstance() method.
 * <p>
 * The server is created with
 * org.objectweb.cjdbc.controller.jmx.MBeanServerBuilder
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class MBeanServerManager
{

  static Trace               logger       = Trace
                                              .getLogger("org.objectweb.cjdbc.controller.jmx.MBeanServer");

  private static MBeanServer mbs;
  private static boolean     isJmxEnabled = true;

  /**
   * creating a MBeanServer, if it does not exist, otherwise a reference to the
   * MBeanServer is returned
   * 
   * @return the mbeanserver instance, null if jmx is disabled
   */
  public static synchronized MBeanServer getInstance()
  {

    if (!isJmxEnabled)
    {
      return null;
    }

    if (mbs != null)
    {
      return mbs;
    }

    String defaultServerBuilder = System
        .getProperty("javax.management.builder.initial");

    if (!MBeanServerBuilder.class.getName().equals(defaultServerBuilder))
    {
      if (defaultServerBuilder != null)
        logger.error("property javax.management.builder.initial was "
            + defaultServerBuilder);

      logger.debug("setting property javax.management.builder.initial");
      System
          .setProperty("javax.management.builder.initial",
              org.objectweb.cjdbc.controller.jmx.MBeanServerBuilder.class
                  .getName());

    }

    mbs = MBeanServerFactory.createMBeanServer();
    return mbs;
  }

  /**
   * Returns the isJmxEnabled value.
   * 
   * @return Returns the isJmxEnabled.
   */
  public static boolean isJmxEnabled()
  {
    return isJmxEnabled;
  }

  /**
   * enable or disable jmx
   * 
   * @param isJmxEnabled The isJmxEnabled to set.
   * @throws JmxException an exception
   */
  public static void setJmxEnabled(boolean isJmxEnabled) throws JmxException
  {
    if (MBeanServerManager.isJmxEnabled != isJmxEnabled && !isJmxEnabled && mbs!=null)
    {
      // stop rmi connectors
      List list = RmiConnector.getRmiConnectors();
      for (Iterator it = list.iterator(); it.hasNext();)
      {
        RmiConnector rmi = (RmiConnector) it.next();
        rmi.stop();
      }

      // stop http adaptors
      list = HttpAdaptor.getHttpAdaptors();
      for (Iterator it = list.iterator(); it.hasNext();)
      {
        HttpAdaptor http = (HttpAdaptor) it.next();
        http.stop();
      }
      // Stop mbean server
      MBeanServerFactory.releaseMBeanServer(mbs);
      mbs = null;
    }
    // set jmx enabled to its value
    MBeanServerManager.isJmxEnabled = isJmxEnabled;
  }

  /**
   * Registers an MBean with the MBean server if jmx is enabled, otherwise it
   * returns null.
   * <p>
   * This method is equivalend to
   * 
   * <pre>
   * MBeanServer server = MBeanServerManager.getInstance();
   * if (server != null)
   * {
   *   server.registerMBean(object, name);
   * }
   * </pre>
   * 
   * @param object The MBean to be registered as an MBean.
   * @param name The object name of the MBean. May be null.
   * @return An ObjectInstance, containing the ObjectName and the Java class
   *         name of the newly registered MBean. If the contained ObjectName is
   *         n, the contained Java class name is getMBeanInfo(n).getClassName().
   *         Or null if jmx is disabled
   * @throws JmxException the object could not be registered
   */
  public static ObjectInstance registerMBean(Object object, ObjectName name)
      throws JmxException
  {
    MBeanServer server = getInstance();
    try
    {

      if (server != null)
      {
        logger.debug(Translate.get("jmx.register.mbean", new String[]{
            object.getClass().toString(), name.getCanonicalName()}));

        ObjectInstance objInstance = null;
        if (!server.isRegistered(name))
        {
          objInstance = server.registerMBean(object, name);
        }
        else
        {
          logger.error(Translate.get("jmx.register.mbean.already.exist",
              new String[]{name.getCanonicalName()}));
          try
          {
            server.unregisterMBean(name);
          }
          catch (Exception e)
          {
            logger.error(Translate.get("jmx.delete.mbean.failed", new String[]{
                name.toString(), e.getMessage()}));
          }
          objInstance = server.registerMBean(object, name);
        }

        logger.debug(Translate.get("jmx.server.mbean.count", ""
            + server.getMBeanCount()));
        return objInstance;
      }
      return null;
    }
    catch (Exception e)
    {
      logger.error(Translate.get("jmx.register.mbean.failed",
          new String[]{object.getClass().toString(), e.getMessage(),
              e.getClass().toString()}));
      e.printStackTrace();
      e.getCause().printStackTrace();
      throw new JmxException(e);
    }
  }

  /**
   * unregister an mbean.
   * 
   * @param name the name of the bean to unregister
   * @throws JmxException problems
   */
  public static void unregister(ObjectName name) throws JmxException
  {
    MBeanServer server = getInstance();
    if (server != null)
    {
      try
      {
        // unregister the MBean
        server.unregisterMBean(name);
        logger.debug(Translate.get("jmx.server.mbean.count", ""
            + server.getMBeanCount()));

      }
      catch (Exception e)
      {
        logger.error(Translate.get("jmx.register.mbean.failed", new String[]{
            name.getCanonicalName(), e.getMessage()}));
        throw new JmxException(e);
      }
    }
  }

}