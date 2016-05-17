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
import javax.management.MBeanServerDelegate;

/**
 * This class is a custom implementation of MBeanServerBuilder, it builds a
 * MBeanServer decorated with an AuthenticatingMBeanServer.
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class MBeanServerBuilder extends ChainedMBeanServerBuilder
{
  /**
   * Creates a new <code>MBeanServerBuilder.java</code> object
   */
  public MBeanServerBuilder()
  {
    super(new javax.management.MBeanServerBuilder());
  }

  /**
   * @see javax.management.MBeanServerBuilder#newMBeanServer(java.lang.String,
   *      javax.management.MBeanServer, javax.management.MBeanServerDelegate)
   */
  public MBeanServer newMBeanServer(String defaultDomain, MBeanServer outer,
      MBeanServerDelegate delegate)
  {
    AuthenticatingMBeanServer extern = new AuthenticatingMBeanServer();
    MBeanServer nested = getMBeanServerBuilder().newMBeanServer(defaultDomain,
        outer == null ? extern : outer, delegate);
    extern.setMBeanServer(nested);
    return extern;
  }
}
