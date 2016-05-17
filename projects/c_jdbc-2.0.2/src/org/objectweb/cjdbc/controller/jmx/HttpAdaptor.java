/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.JmxException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.net.SSLConfiguration;

/**
 * This class defines a HttpAdaptor
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class HttpAdaptor

{
  static Trace                                logger       = Trace
                                                               .getLogger("org.objectweb.cjdbc.controller.jmx");

  private String                              hostName;
  private int                                 port;
  //private JMXAuthenticator authenticator;
  private SSLConfiguration                    sslConfig;

  private mx4j.tools.adaptor.http.HttpAdaptor adaptor;
  private ObjectName                          objectName;
  private ObjectName                          processorName;

  private static List                         httpAdaptors = new ArrayList();

  /**
   * Creates a new <code>HttpAdaptor</code> object
   * 
   * @param hostName the host name the adaptor binds to
   * @param port the http port
   * @param sslConfig the ssl configuration, if null ssl is disabled
   * @throws JmxException problems to get name of localhost
   */
  public HttpAdaptor(String hostName, int port, SSLConfiguration sslConfig)
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
    this.port = port;
    //this.authenticator = authenticator;
    this.sslConfig = sslConfig;
    addHttpAdaptor(this);
  }

  /**
   * Start the HTTP adaptor
   * 
   * @throws JmxException the adaptor could not be started
   */
  public void start() throws JmxException
  {
    try
    {

      MBeanServer server = MBeanServerManager.getInstance();
      // register the HTTP adaptor MBean to the agent
      logger.info(Translate.get("jmx.create.http.adaptor", new Object[]{
          hostName, String.valueOf(port)}));
      adaptor = new mx4j.tools.adaptor.http.HttpAdaptor();
      objectName = new ObjectName("Server:name=HttpAdaptor");
      server.registerMBean(adaptor, objectName);
      adaptor.setHost(hostName);
      adaptor.setPort(port);
      // set XSLT processor
      logger.debug(Translate.get("jmx.create.xslt.processor"));
      processorName = new ObjectName("Server:name=XSLTProcessor");
      server.createMBean(mx4j.tools.adaptor.http.XSLTProcessor.class.getName(),
          processorName, null);
      server.setAttribute(objectName, new Attribute("ProcessorName",
          processorName));
      if (this.sslConfig != null)
      {
        // TODO: SSL support for HTTP adaptor
        throw new JmxException("ssl for http not implemented");
      }
      adaptor.start();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new JmxException(e);
    }
  }

  /**
   * stop the http adaptor
   * 
   * @throws JmxException problems stoping the adaptor
   */
  public void stop() throws JmxException
  {
    try
    {
      MBeanServer server = MBeanServerManager.getInstance();
      adaptor.stop();
      server.unregisterMBean(objectName);
      server.unregisterMBean(processorName);
    }
    catch (Exception e)
    {
      throw new JmxException(e);
    }
  }

  /**
   * Returns a list of HttpAdaptor .
   * 
   * @return Returns list of HttpAdaptor.
   */
  public static List getHttpAdaptors()
  {
    return httpAdaptors;
  }

  /**
   * Adds an HttpAdaptor to the list.
   * 
   * @param httpAdaptor The HttpAdaptor to add.
   */
  private static synchronized void addHttpAdaptor(HttpAdaptor httpAdaptor)
  {
    httpAdaptors.add(httpAdaptor);
  }
}
