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
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.controller.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.objectweb.cjdbc.common.i18n.Translate;

/**
 * This connection manager uses a pool of persistent connections with the
 * database. The allocation/release policy is implemented by the subclasses
 * (abstract
 * {@link org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getConnection()}/
 * {@link org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#releaseConnection(Connection)}
 * from
 * {@link org.objectweb.cjdbc.controller.connection.AbstractConnectionManager}).
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public abstract class AbstractPoolConnectionManager
    extends AbstractConnectionManager
{
  //
  // How the code is organized ?
  //
  // 1. Member variables
  // 2. Constructor(s)
  // 3. Connection handling
  // 4. Getter/Setter (possibly in alphabetical order)
  //

  /** Stack of available connections (pool). */
  protected transient LinkedList freeConnections;

  /**
   * Pool of currently used connections (<code>Vector</code> type because
   * synchronisation is needed).
   */
  protected transient ArrayList  activeConnections;

  /** Size of the connection pool with the real database. */
  protected int                  poolSize;

  /*
   * Constructor(s)
   */

  /**
   * Creates a new <code>AbstractPoolConnectionManager</code> instance.
   * 
   * @param backendUrl URL of the <code>DatabaseBackend</code> owning this
   *          connection manager.
   * @param backendName name of the <code>DatabaseBackend</code> owning this
   *          connection manager.
   * @param login backend connection login to be used by this connection
   *          manager.
   * @param password backend connection password to be used by this connection
   *          manager.
   * @param driverPath path for driver
   * @param driverClassName class name for driver
   * @param poolSize size of the connection pool.
   */
  public AbstractPoolConnectionManager(String backendUrl, String backendName,
      String login, String password, String driverPath, String driverClassName,
      int poolSize)
  {
    super(backendUrl, backendName, login, password, driverPath, driverClassName);

    if (poolSize < 1)
      throw new IllegalArgumentException(
          "Illegal value for size of the pool connection manager: " + poolSize);

    this.poolSize = poolSize;
    this.freeConnections = new LinkedList();
    this.activeConnections = new ArrayList(poolSize);
    this.initialized = false;

    if (logger.isDebugEnabled())
      logger.debug(Translate.get("connection.backend.pool.created",
          new String[]{backendName, String.valueOf(poolSize)}));
  }

  /*
   * Connection handling
   */

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#initializeConnections()
   */
  public synchronized void initializeConnections() throws SQLException
  {
    initializeConnections(poolSize);
  }

  /**
   * Initialize initPoolSize connections in the pool.
   * 
   * @param initPoolSize number of connections to initialize
   * @throws SQLException if an error occurs
   */
  public synchronized void initializeConnections(int initPoolSize)
      throws SQLException
  {
    if (initialized)
      throw new SQLException("Connection pool for backend '" + backendUrl
          + "' already initialized");

    if (initPoolSize > poolSize)
    {
      logger.warn(Translate.get("connection.max.poolsize.reached",
          new String[]{String.valueOf(initPoolSize), String.valueOf(poolSize),
              String.valueOf(poolSize)}));
      initPoolSize = poolSize;
    }

    Connection c = null;

    boolean connectionsAvailable = true;
    int i = 0;
    while ((i < initPoolSize) && connectionsAvailable)
    {
      c = getConnectionFromDriver();

      if (c == null)
        connectionsAvailable = false;

      if (!connectionsAvailable)
      {
        if (i > 0)
        {
          logger.warn(Translate.get("connection.limit.poolsize", i));
        }
        else
        {
          logger.warn(Translate.get("connection.initialize.pool.failed"));
          poolSize = 0;
        }
      }
      else
      {
        freeConnections.addLast(c);
        i++;
      }
    }

    poolSize = i;
    initialized = true;

    if (poolSize == 0) // Should never happen
      logger.error(Translate.get("connection.empty.pool"));
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("connection.pool.initialized", new String[]{
          String.valueOf(initPoolSize), backendUrl}));

  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#finalizeConnections()
   */
  public synchronized void finalizeConnections() throws SQLException
  {
    if (!initialized)
    {
      String msg = Translate.get("connection.pool.not.initialized");
      logger.error(msg);
      throw new SQLException(msg);
    }

    Connection c;
    boolean error = false;

    // Close free connections
    initialized = false;
    int freed = 0;
    while (!freeConnections.isEmpty())
    {
      c = (Connection) freeConnections.removeLast();
      try
      {
        c.close();
      }
      catch (SQLException e)
      {
        error = true;
      }
      freed++;
    }
    if (logger.isInfoEnabled())
      logger.info(Translate.get("connection.freed.connection", new String[]{
          String.valueOf(freed), backendUrl}));

    // Close active connections
    int size = activeConnections.size();
    if (size > 0)
    {
      logger.warn(Translate.get("connection.connections.still.active", size));
      for (int i = 0; i < size; i++)
      {
        c = (Connection) activeConnections.get(i);
        try
        {
          c.close();
        }
        catch (SQLException e)
        {
          error = true;
        }
      }
    }

    // Clear connections to ensure that the eventually not closed connections
    // will be closed when the objects will be garbage collected
    freeConnections.clear();
    activeConnections.clear();

    if (error)
    {
      String msg = Translate.get("connection.free.connections.failed");
      logger.error(msg);
      throw new SQLException(msg);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getCurrentNumberOfConnections()
   */
  public int getCurrentNumberOfConnections()
  {
    return poolSize;
  }

}