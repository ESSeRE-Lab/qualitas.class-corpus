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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.management.Notification;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.naming.Context;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.JmxException;
import org.objectweb.cjdbc.common.jmx.notifications.JmxNotification;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.net.RMISSLClientSocketFactory;
import org.objectweb.cjdbc.common.net.RMISSLServerSocketFactory;
import org.objectweb.cjdbc.common.net.SSLConfiguration;
import org.objectweb.cjdbc.common.net.SocketFactoryFactory;
import org.objectweb.cjdbc.controller.authentication.PasswordAuthenticator;

/**
 * This class defines a RmiConnector
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class RmiConnector
{
  static Trace               logger        = Trace
                                               .getLogger("org.objectweb.cjdbc.controller.jmx");

  private String             controllerName;
  private String             hostName;
  private int                port;
  private JMXAuthenticator   authenticator;
  private SSLConfiguration   sslConfig;

  /**
   * we have to keep a reference to the server to avoid it from being garbage
   * collected otherwise the client will throw a java.rmi.NoSuchObjectException
   * (problem experienced with ssl connections)
   */
  private JMXConnectorServer connection;
  private Remote             rmiRegistry;

  private static List        rmiConnectors = new ArrayList();

  /**
   * Creates a new <code>RmiConnector.java</code> object
   * 
   * @param controllerName for reference when sending notification
   * @param hostName the name of the host we bind to, if null the default
   *          InetAddress.getLocalHost().getHostName() is used
   * @param port the port the rmi registry is listening on
   * @param authenticator the jmxauthenticator used for the connection
   * @param sslConfig ssl configuration
   * @throws JmxException the name of the localhost could not be determined
   */
  public RmiConnector(String controllerName, String hostName, int port,
      JMXAuthenticator authenticator, SSLConfiguration sslConfig)
      throws JmxException
  {
    if (hostName != null)
    {
      this.hostName = hostName;
    }
    else
    {
      try
      {
        /** TODO: dssmith - determine applicability of getLocalHost() */
        this.hostName = InetAddress.getLocalHost().getHostName();
      }
      catch (UnknownHostException ex)
      {
        throw new JmxException(ex);
      }
    }
    this.controllerName = controllerName;
    this.port = port;
    this.authenticator = authenticator;
    this.sslConfig = sslConfig;

    addRmiConnector(this);
  }

  /**
   * Returns the authenticator value.
   * 
   * @return Returns the authenticator.
   */
  public JMXAuthenticator getAuthenticator()
  {
    return authenticator;
  }

  /**
   * Sets the authenticator value.
   * 
   * @param authenticator The authenticator to set.
   */
  public void setAuthenticator(JMXAuthenticator authenticator)
  {
    this.authenticator = authenticator;
  }

  /**
   * Returns the port value.
   * 
   * @return Returns the port.
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Sets the port value.
   * 
   * @param port The port to set.
   */
  public void setPort(int port)
  {
    this.port = port;
  }

  /**
   * Returns the sslConfig value.
   * 
   * @return Returns the sslConfig.
   */
  public SSLConfiguration getSslConfig()
  {
    return sslConfig;
  }

  /**
   * Sets the sslConfig value.
   * 
   * @param sslConfig The sslConfig to set.
   */
  public void setSslConfig(SSLConfiguration sslConfig)
  {
    this.sslConfig = sslConfig;
  }

  /**
   * Returns the connection value.
   * 
   * @return Returns the connection.
   */
  public JMXConnectorServer getConnection()
  {
    return connection;
  }

  /**
   * start the rmi connector and the rmi naming service
   * 
   * @throws JmxException an exception
   */
  public void start() throws JmxException
  {
    createNamingService();
    createJRMPAdaptor();
  }

  /**
   * stop the rmi connector and the rmi registry
   * 
   * @throws JmxException an exception
   */
  public void stop() throws JmxException
  {
    try
    {
      if (connection != null)
        connection.stop();
      if (rmiRegistry != null)
        UnicastRemoteObject.unexportObject(rmiRegistry, true);
    }
    catch (Exception e)
    {
      throw new JmxException(e);
    }
    finally
    {
      connection = null;
      rmiRegistry = null;
    }
  }

  /**
   * Create naming service and starts rmi
   * 
   * @throws JmxException if creation fails
   */
  private void createNamingService() throws JmxException
  {
    try
    {
      // create and start the naming service
      logger.info(Translate.get("jmx.create.naming.service", new String[]{""
          + port}));
      rmiRegistry = LocateRegistry.createRegistry(port);
    }
    catch (Exception e)
    {
      throw new JmxException(e);
    }
  }

  private void createJRMPAdaptor() throws JmxException
  {
    try
    {
      // create the JRMP adaptator
      logger.info(Translate.get("jmx.create.jrmp.adaptor", "" + port));

      // Set the jndi name with which it will be registered
      // JNDI properties
      logger.debug(Translate.get("jmx.prepare.jndi"));

      javax.management.remote.JMXServiceURL address = new javax.management.remote.JMXServiceURL(
          "rmi", hostName, 0, "/jndi/jrmp");

      java.util.Map environment = new java.util.HashMap();
      environment.put(Context.INITIAL_CONTEXT_FACTORY,
          "com.sun.jndi.rmi.registry.RegistryContextFactory");
      environment.put(Context.PROVIDER_URL, "rmi://" + hostName + ":" + port);

      if (authenticator == null)
      {
        authenticator = PasswordAuthenticator.NO_AUTHENICATION;
      }

      if (authenticator != null)
      {
        environment.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
      }

      // ssl enabled ?
      if (sslConfig != null)
      {
        logger.info(Translate.get("jmx.create.jrmp.ssl.enabled"));

        RMISSLClientSocketFactory csf = new RMISSLClientSocketFactory();
        RMISSLServerSocketFactory ssf = new RMISSLServerSocketFactory(
            SocketFactoryFactory.createServerFactory(sslConfig));
        environment.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,
            csf);
        environment.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
            ssf);
      }

      connection = javax.management.remote.JMXConnectorServerFactory
          .newJMXConnectorServer(address, environment, MBeanServerManager
              .getInstance());

      connection.start();
    }
    catch (Exception e)
    {
      throw new JmxException(e);
    }
  }

  /**
   * Returns a list of rmiConnectors .
   * 
   * @return Returns list of RmiConnector.
   */
  public static List getRmiConnectors()
  {
    return rmiConnectors;
  }

  /**
   * Adds an rmiConnector to the list.
   * 
   * @param pRmiConnector The rmiConnector to add.
   */
  private static synchronized void addRmiConnector(RmiConnector pRmiConnector)
  {
    rmiConnectors.add(pRmiConnector);
  }

  /**
   * @return Returns the controllerName.
   */
  public String getControllerName()
  {
    return controllerName;
  }

  /**
   * @return Returns the hostName.
   */
  public String getHostName()
  {
    return hostName;
  }

  private Date            myDate;
  private long            time;
  private JmxNotification cjdbcNotification;
  private Notification    notification;
  private static long     sequence = 0;

  /**
   * This method sends notification to all client registered to an instance of
   * the <code>RmiConnector</code> class. The <code>JmxNotification</code>
   * class is used here to create an object with all the information gathered in
   * parameters, and then is serialized in xml for interaction on the client
   * side.
   * 
   * @see JmxNotification
   * @param mbean the mbean that is generating the notification
   * @param type the type as seen in <code>CjdbcNotificationList</code>
   * @param priority notification level as seen in
   *          <code>CjdbcNotificationList</code>
   * @param description a string description of the notification
   * @param data a hashtable of data that can be used to give more information
   *          on the notification
   */
  public synchronized void sendNotification(AbstractStandardMBean mbean,
      String type, String priority, String description, Hashtable data)
  {

    myDate = new Date();
    time = myDate.getTime();

    cjdbcNotification = new JmxNotification(priority, "" + sequence, type,
        description, "" + time, controllerName, mbean.getClass().getName(),
        "mbeanName", hostName, "" + port, data);
    notification = new Notification(type, mbean, sequence, myDate.getTime(),
        description);
    notification.setUserData(cjdbcNotification.toString());
    mbean.sendNotification(notification);
  }

  /**
   * Broadcast a jmx notification to any client connected to any RmiConnector
   * registered in the static list. The method is static because it is sending
   * notifications to all rmi connectors.
   * 
   * @param mbean the mbean that is generating the notification
   * @param type the type as seen in <code>CjdbcNotificationList</code>
   * @param priority notification level as seen in
   *          <code>CjdbcNotificationList</code>
   * @param description a string description of the notification
   * @param data a hashtable of data that can be used to give more information
   *          on the notification
   */
  public static void broadcastNotification(AbstractStandardMBean mbean,
      String type, String priority, String description, Hashtable data)
  {
    sequence++;
    logger.info("Sending notification:" + description + "(Message No:"
        + sequence + ")");
    Iterator iter = rmiConnectors.iterator();
    RmiConnector rmi;
    while (iter.hasNext())
    {
      rmi = ((RmiConnector) iter.next());
      rmi.sendNotification(mbean, type, priority, description, data);
    }
  }
}