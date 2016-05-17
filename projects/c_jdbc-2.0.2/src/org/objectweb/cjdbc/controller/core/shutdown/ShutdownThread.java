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
 * Initial developer(s): Emmanuel Cecchet. 
 * Contributor(s): Nicolas Modrzyk.
 */

package org.objectweb.cjdbc.controller.core.shutdown;

import java.util.Date;

import org.objectweb.cjdbc.common.exceptions.ShutdownException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;

/**
 * Skeleton for shutdown threads. This includes <code>Controller</code>,
 * <code>VirtualDatabase</code> and <code>DatabaseBackend</code> shutdown
 * threads.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public abstract class ShutdownThread implements Runnable
{
  /** Group to join onto when shutting down */
  public ThreadGroup shutdownGroup = new ThreadGroup("shutdown" + new Date());

  protected int      shutdownLevel;

  /** Logger instance. */
  Trace              logger        = Trace
                                       .getLogger("org.objectweb.cjdbc.controller.shutdown");

  /**
   * Create a new shutdown thread
   * 
   * @param level Constants.SHUTDOWN_WAIT, Constants.SHUTDOWN_SAFE or
   *          Constants.SHUTDOWN_FORCE
   */
  public ShutdownThread(int level)
  {
    this.shutdownLevel = level;
    logger = Trace.getLogger("org.objectweb.cjdbc.controller.shutdown");
  }

  /**
   * Returns the shutdownGroup value.
   * 
   * @return Returns the shutdownGroup.
   */
  public ThreadGroup getShutdownGroup()
  {
    return shutdownGroup;
  }

  /**
   * Get shutdown level
   * 
   * @return level
   */
  public int getShutdownLevel()
  {
    return this.shutdownLevel;
  }

  /**
   * Execute the shutdown
   * 
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      shutdown();
    }
    catch (ShutdownException se)
    {
      se.printStackTrace();
      abortShutdown(se);
    }
  }

  /**
   * If shutdown fails ...
   * 
   * @param cause why shutdown was aborted
   */
  public void abortShutdown(Exception cause)
  {
    logger.info(Translate.get("controller.shutdown.aborted", cause));
  }

  /**
   * Specific implementation of the shutdown method.
   * 
   * @throws ShutdownException if fails
   */
  public abstract void shutdown() throws ShutdownException;

}