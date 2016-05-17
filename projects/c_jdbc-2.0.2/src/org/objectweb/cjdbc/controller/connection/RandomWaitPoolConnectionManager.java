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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.connection;

import java.sql.Connection;
import java.util.NoSuchElementException;

import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This connection manager waits when the pool is empty. Requests are stacked
 * using the Java wait/notify mechanism. Therefore the FIFO order is not
 * guaranteed and the first request to get the freed connection is the thread
 * that gets elected by the scheduler.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class RandomWaitPoolConnectionManager
    extends AbstractPoolConnectionManager
{
  /** Time to wait for a connection in milliseconds (0 means wait forever). */
  private int timeout;

  /**
   * Creates a new <code>RandomWaitPoolConnectionManager</code> instance.
   * 
   * @param backendUrl URL of the <code>DatabaseBackend</code> owning this
   *          connection manager
   * @param backendName name of the <code>DatabaseBackend</code> owning this
   *          connection manager
   * @param login backend connection login to be used by this connection manager
   * @param password backend connection password to be used by this connection
   *          manager
   * @param driverPath path for driver
   * @param driverClassName class name for driver
   * @param poolSize size of the connection pool
   * @param timeout time to wait for a connection in seconds (0 means wait
   *          forever)
   */
  public RandomWaitPoolConnectionManager(String backendUrl, String backendName,
      String login, String password, String driverPath, String driverClassName,
      int poolSize, int timeout)
  {
    super(backendUrl, backendName, login, password, driverPath,
        driverClassName, poolSize);
    this.timeout = timeout * 1000;
  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    return new RandomWaitPoolConnectionManager(backendUrl, backendName, rLogin,
        rPassword, driverPath, driverClassName, poolSize, timeout);
  }

  /**
   * Gets the timeout.
   * 
   * @return a <code>int</code> value.
   */
  public int getTimeout()
  {
    return timeout;
  }

  /**
   * Gets a connection from the pool.
   * <p>
   * If the pool is empty, this methods blocks until a connection is freed or
   * the timeout expires.
   * 
   * @return a connection from the pool or <code>null</code> if the timeout
   *         has expired.
   * @throws UnreachableBackendException if the backend must be disabled
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getConnection()
   */
  public synchronized Connection getConnection()
      throws UnreachableBackendException
  {
    if (!initialized)
    {
      logger
          .error("Requesting a connection from a non-initialized connection manager");
      return null;
    }

    long lTimeout = timeout;

    // We have to do a while loop() because there is a potential race here.
    // When freeConnections is notified in releaseConnection, a new thread
    // can
    // take the lock on freeConnections before we wake up/reacquire the lock
    // on freeConnections. Therefore, we could wake up and have no connection
    // to take! We ensure that everything is correct with a while statement
    // and recomputing the timeout between 2 wakeup.
    while (freeConnections.isEmpty())
    {
      // Wait
      try
      {
        if (lTimeout > 0)
        {
          long start = System.currentTimeMillis();
          // Convert seconds to milliseconds for wait call
          this.wait(timeout);
          long end = System.currentTimeMillis();
          lTimeout -= end - start;
          if (lTimeout <= 0)
          {
            if (activeConnections.size() == 0)
            { // No connection active and backend unreachable, the backend
              // is probably dead
              logger
                  .error("Backend " + backendName + " is no more accessible.");
              throw new UnreachableBackendException();
            }
            if (logger.isWarnEnabled())
              logger.warn("Timeout expired for connection on backend '"
                  + backendName
                  + "', consider increasing pool size (current size is "
                  + poolSize + ") or timeout (current timeout is "
                  + (timeout / 1000) + " seconds)");
            return null;
          }
        }
        else
          this.wait();
      }
      catch (InterruptedException e)
      {
        logger
            .error("Wait on freeConnections interrupted in RandomWaitPoolConnectionManager: "
                + e);
        return null;
      }
    }

    // Get the connection
    try
    {
      Connection c = (Connection) freeConnections.removeLast();
      activeConnections.add(c);
      return c;
    }
    catch (NoSuchElementException e)
    {
      int missing = poolSize
          - (activeConnections.size() + freeConnections.size());
      if (missing > 0)
      { // Re-allocate missing connections
        logger
            .info("Trying to reallocate " + missing + " missing connections.");
        Connection connectionToBeReturned = null;
        while (missing > 0)
        {
          Connection c = getConnectionFromDriver();
          if (c == null)
          {
            if (missing == poolSize)
            {
              String msg = Translate.get("loadbalancer.backend.unreacheable",
                  backendName);
              logger.error(msg);
              throw new UnreachableBackendException(msg);
            }
            logger.warn("Unable to re-allocate " + missing
                + " missing connections.");
            break;
          }
          else
          {
            if (connectionToBeReturned == null)
              connectionToBeReturned = c;
            else
              freeConnections.addLast(c);
          }
          missing--;
        }
        return connectionToBeReturned;
      }
      if (logger.isErrorEnabled())
        logger.error("Failed to get a connection on backend '" + backendName
            + "' whereas an idle connection was expected");
      return null;
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager#releaseConnection(Connection)
   */
  public synchronized void releaseConnection(Connection c)
  {
    if (!initialized)
      return; // We probably have been disabled

    if (activeConnections.remove(c))
    {
      freeConnections.addLast(c);
      this.notify();
    }
    else
      logger.error("Failed to release connection " + c
          + " (not found in active pool)");
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractPoolConnectionManager#deleteConnection(Connection)
   */
  public synchronized void deleteConnection(Connection c)
  {
    if (!initialized)
      return; // We probably have been disabled

    if (activeConnections.remove(c))
    {
      Connection newConnection = getConnectionFromDriver();
      if (newConnection == null)
      {
        if (logger.isDebugEnabled())
          logger.error("Bad connection " + c
              + " has been removed but cannot be replaced.");
      }
      else
      {
        freeConnections.addLast(newConnection);
        this.notify();
        if (logger.isDebugEnabled())
          logger.debug("Bad connection " + c
              + " has been replaced by a new connection.");
      }
    }
    else
      logger.error("Failed to release connection " + c
          + " (not found in active pool)");
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getXmlImpl()
   */
  public String getXmlImpl()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_RandomWaitPoolConnectionManager
        + " " + DatabasesXmlTags.ATT_poolSize + "=\"" + poolSize + "\" "
        + DatabasesXmlTags.ATT_timeout + "=\"" + timeout / 1000 + "\"/>");
    return info.toString();
  }

}
