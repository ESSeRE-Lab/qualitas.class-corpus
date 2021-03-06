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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): __________________.
 */

package org.objectweb.cjdbc.common.log;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Logger manager.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class LogManager
{
  /**
   * Retrieves a logger by its name.
   * 
   * @param name logger name
   * @return trace a <code>trace</code>
   */
  static Trace getLogger(String name)
  {
    return new Trace(Logger.getLogger(name));
  }

  /**
   * Configures log4j according to the given property file name.
   * 
   * @param propertyFileName the log4j property file name
   */
  public static void configure(String propertyFileName)
  {
    PropertyConfigurator.configure(propertyFileName);
  }
}
