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
 * This is a wrapper where logging has been statically disabled. It should
 * improve the performance if one wants to completely disable traces.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class StaticDisabledLogger extends Trace
{

  /**
   * Creates a new <code>StaticDisabledLogger</code> object from a given
   * log4j <code>Logger</code>.
   * 
   * @param log4jLogger the log4j <code>Logger</code>
   */
  public StaticDisabledLogger(Logger log4jLogger)
  {
    super(log4jLogger);
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see org.objectweb.cjdbc.common.log.Trace#debug(Object, Throwable)
   */
  public void debug(Object message, Throwable t)
  {
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see org.objectweb.cjdbc.common.log.Trace#debug(Object)
   */
  public void debug(Object message)
  {
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see org.objectweb.cjdbc.common.log.Trace#error(Object, Throwable)
   */
  public void error(Object message, Throwable t)
  {
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see org.objectweb.cjdbc.common.log.Trace#error(Object)
   */
  public void error(Object message)
  {
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see org.objectweb.cjdbc.common.log.Trace#fatal(Object, Throwable)
   */
  public void fatal(Object message, Throwable t)
  {
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see org.objectweb.cjdbc.common.log.Trace#fatal(Object)
   */
  public void fatal(Object message)
  {
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see org.objectweb.cjdbc.common.log.Trace#info(Object, Throwable)
   */
  public void info(Object message, Throwable t)
  {
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see org.objectweb.cjdbc.common.log.Trace#info(Object)
   */
  public void info(Object message)
  {
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see org.objectweb.cjdbc.common.log.Trace#warn(Object, Throwable)
   */
  public void warn(Object message, Throwable t)
  {
  }

  /**
   * This method is overriden with an empty body.
   * 
   * @see org.objectweb.cjdbc.common.log.Trace#warn(Object)
   */
  public void warn(Object message)
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

  /**
   * @return <code>false</code>
   * @see org.objectweb.cjdbc.common.log.Trace#isErrorEnabled()
   */
  public boolean isErrorEnabled()
  {
    return false;
  }

  /**
   * @return <code>false</code>
   * @see org.objectweb.cjdbc.common.log.Trace#isFatalEnabled()
   */
  public boolean isFatalEnabled()
  {
    return false;
  }

  /**
   * @return <code>false</code>
   * @see org.objectweb.cjdbc.common.log.Trace#isInfoEnabled()
   */
  public boolean isInfoEnabled()
  {
    return false;
  }

  /**
   * @return <code>false</code>
   * @see org.objectweb.cjdbc.common.log.Trace#isWarnEnabled()
   */
  public boolean isWarnEnabled()
  {
    return false;
  }
}
