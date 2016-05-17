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

/**
 * This is a wrapper where debug logging has been statically disabled. It
 * should improve the performance if one wants to completely disable debug
 * traces.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class StaticNoDebugLogger extends Trace
{

  /**
   * Creates a new <code>StaticNoDebugLogger</code> object from a given log4j
   * <code>Logger</code>.
   * 
   * @param log4jLogger the log4j <code>Logger</code>
   */
  public StaticNoDebugLogger(Logger log4jLogger)
  {
    super(log4jLogger);
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see Trace#debug(Object, Throwable)
   */
  public void debug(Object message, Throwable t)
  {
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see Trace#debug(Object)
   */
  public void debug(Object message)
  {
  }

  /**
   * @return <code>false</code>
   * @see org.objectweb.cjdbc.common.log.Trace#isDebugEnabled()
   */
  public boolean isDebugEnabled()
  {
    return false;
  }

}
