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
 * Initial developer(s): Mathieu Peltier.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.tools.util;

import java.sql.Connection;

import junit.framework.Assert;

import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager;

/**
 * This thread gets a connection during a given time and releases it after.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 */
public class GetConnectionThread extends Thread
{
  /** Pool connection manager. */
  private AbstractPoolConnectionManager pool;

  /** Time in milliseconds the thread is using the connection. */
  private long                          time;

  /**
   * Creates a new <code>GetConnectionThread</code> instance.
   * 
   * @param name thread name.
   * @param pool pool connection manager.
   * @param time time in milliseconds the thread is using the connection.
   */
  public GetConnectionThread(String name, AbstractPoolConnectionManager pool,
      long time)
  {
    super(name);
    this.pool = pool;
    this.time = time;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    Connection c = null;
    try
    {
      c = pool.getConnection();
    }
    catch (UnreachableBackendException e1)
    {
      Assert.fail("Backend unreachable during test.");
    }
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException e)
    {
      Assert.fail("Exception thrown: " + e);
    }
    pool.releaseConnection(c);
  }
}