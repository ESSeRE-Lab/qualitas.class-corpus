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

import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;

/**
 * Base class for chained MBeanServerBuilders. <br>
 * By default this class delegates all method calls to the nested
 * MBeanServerBuilder. <br>
 * See the MX4J documentation on how to use correctly this class. <br>
 * <br>
 * Example implementation:
 * 
 * <pre>
 * 
 *  
 *   
 *    public class LoggingBuilder extends ChainedMBeanServerBuilder
 *    {
 *       public LoggingBuilder()
 *       {
 *          super(new MBeanServerBuilder());
 *       }
 *   
 *       public MBeanServer newMBeanServer(String defaultDomain, MBeanServer outer, MBeanServerDelegate delegate)
 *       {
 *          LoggingMBeanServer external = new LoggingMBeanServer();
 *          MBeanServer nested = getBuilder().newMBeanServer(defaultDomain, outer == null ? external : outer, delegate);
 *          external.setMBeanServer(nested);
 *          return external;
 *       }
 *    }
 *   
 *    public class LoggingMBeanServer extends ChainedMBeanServer
 *    {
 *       protected void setMBeanServer(MBeanServer server)
 *       {
 *          super.setMBeanServer(server);
 *       }
 *   
 *       public Object getAttribute(ObjectName objectName, String attribute)
 *               throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException
 *       {
 *          Object value = super.getAttribute(objectName, attribute);
 *          System.out.println(&quot;Value is: &quot; + value);
 *          return value;
 *       }
 *   
 *       ...
 *    }
 *    
 *   
 *  
 * </pre>
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class ChainedMBeanServerBuilder extends javax.management.MBeanServerBuilder
{
  private final MBeanServerBuilder builder;

  /**
   * Creates a new chained MBeanServerBuilder
   * 
   * @param builder The MBeanServerBuilder this object delegates to.
   */
  public ChainedMBeanServerBuilder(MBeanServerBuilder builder)
  {
    if (builder == null)
      throw new IllegalArgumentException();
    this.builder = builder;
  }

  /**
   * Forwards the call to the chained builder.
   * 
   * @see MBeanServerBuilder#newMBeanServerDelegate
   */
  public MBeanServerDelegate newMBeanServerDelegate()
  {
    return getMBeanServerBuilder().newMBeanServerDelegate();
  }

  /**
   * Forwards the call to the chained builder.
   * 
   * @see MBeanServerBuilder#newMBeanServer(java.lang.String,
   *      javax.management.MBeanServer, javax.management.MBeanServerDelegate)
   */
  public MBeanServer newMBeanServer(String defaultDomain, MBeanServer outer,
      MBeanServerDelegate delegate)
  {
    return getMBeanServerBuilder().newMBeanServer(defaultDomain, outer,
        delegate);
  }

  /**
   * Returns the chained MBeanServerBuilder this object delegates to.
   */
  protected MBeanServerBuilder getMBeanServerBuilder()
  {
    return builder;
  }

}
