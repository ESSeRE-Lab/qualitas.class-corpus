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
 * This connection manager returns <code>null</code> when the pool is empty.
 * Therefore all requests fail fast until connections are freed.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class FailFastPoolConnectionManager
    extends AbstractPoolConnectionManager
{

  /**
   * Creates a new <code>FailFastPoolConnectionManager</code> instance.
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
   */
  public FailFastPoolConnectionManager(String backendUrl, String backendName,
      String login, String password, String driverPath, String driverClassName,
      int poolSize)
  {
    super(backendUrl, backendName, login, password, driverPath,
        driverClassName, poolSize);
  }

  /**
   * Gets a connection from the pool. Returns <code>null</code> if the pool is
   * empty.
   * 
   * @return a connection from the pool or <code>null</code> if the pool is
   *         exhausted
   * @throws UnreachableBackendException if the backend must be disabled
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getConnection()
   */
  public synchronized Connection getConnection()
      throws UnreachableBackendException
  {
    if (!initialized)
    {
      logger.error(Translate.get("connection.request.not.initialized"));
      return null;
    }

    try
    { // Both freeConnections and activeConnections are synchronized
      Connection c = (Connection) freeConnections.removeLast();
      activeConnections.add(c);
      return c;
    }
    catch (NoSuchElementException e)
    { // No free connection
      int missing = poolSize
          - (activeConnections.size() + freeConnections.size());
      if (missing > 0)
      { // Re-allocate missing connections
        logger.info(Translate.get("connection.reallocate.missing", missing));
        Connection connectionToBeReturned = null;
        while (missing > 0)
        {
          Connection c = getConnectionFromDriver();
          if (c == null)
          {
            if (missing == poolSize)
            {
              logger.error(Translate.get("connection.backend.unreachable",
                  backendName));
              throw new UnreachableBackendException();
            }
            logger.warn(Translate.get("connection.reallocate.failed", missing));
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
      if (logger.isWarnEnabled())
        logger.warn(Translate.get("connection.backend.out.of.connections",
            new String[]{backendName, String.valueOf(poolSize)}));
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
      freeConnections.addLast(c);
    else
      logger.error(Translate.get("connection.release.failed", c.toString()));
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
          logger.error(Translate
              .get("connection.replaced.failed", c.toString()));
      }
      else
      {
        freeConnections.addLast(newConnection);
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("connection.replaced.success", c
              .toString()));
      }
    }
    else
      logger.error(Translate.get("connection.replaced.failed.exception", c
          .toString()));
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getXmlImpl()
   */
  public String getXmlImpl()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_FailFastPoolConnectionManager + " "
        + DatabasesXmlTags.ATT_poolSize + "=\"" + poolSize / 1000 + "\"/>");
    return info.toString();
  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    return new FailFastPoolConnectionManager(backendUrl, backendName, rLogin,
        rPassword, driverPath, driverClassName, poolSize);
  }
}
