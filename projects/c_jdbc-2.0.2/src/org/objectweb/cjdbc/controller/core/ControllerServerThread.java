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
 * Contributor(s): Duncan Smith.
 */

package org.objectweb.cjdbc.controller.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ServerSocketFactory;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.net.SSLException;
import org.objectweb.cjdbc.common.net.SocketFactoryFactory;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;

/**
 * A <code>ControllerServerThread</code> listens for C-JDBC driver
 * connections. It accepts the connection and give them to
 * <code>ControllerWorkerThreads</code>.
 * 
 * @see org.objectweb.cjdbc.controller.core.ControllerWorkerThread
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:duncan@mightybot.com">Duncan Smith </a>
 * @version 1.0
 */
public class ControllerServerThread extends Thread
{
  private ServerSocket                   serverSocket;
  private boolean                        isShuttingDown                     = false;
  protected Controller                   controller;
  /** Pending queue of client (driver) socket connections */
  protected ArrayList                    controllerServerThreadPendingQueue = new ArrayList();
  /**
   * Number of idle <code>ControllerWorkerThread</code>. Access to this
   * variable must be synchronized using pendingQueue.
   */
  protected int                          idleWorkerThreads                  = 0;

  /** Logger instance. */
  static Trace                           logger                             = Trace
                                                                                .getLogger("org.objectweb.cjdbc.controller.core.Controller");

  /**
   * Creates a new ControllerServerThread that listens on the given port.
   * 
   * @param controller The controller which created this thread.
   */
  public ControllerServerThread(Controller controller)
  {
    super("ControllerServerThread");
    this.controller = controller;

    try
    {
      InetAddress bindAddress = InetAddress
          .getByName(controller.getIPAddress());

      // Determine if a specific IP address has been requested.
      /**
       * @see org.objectweb.cjdbc.controller.xml.ControllerParser#configureController(Attributes)
       *      for how controller's IPAddress is set by default.
       */
      if (!controller.getIPAddress().equals(
          InetAddress.getLocalHost().getHostAddress()))
      { // Non-default value: an IP has been specified that is not localhost...
        // If the user has *asked for* getLocalHost().getHostAddress(), bad luck
        // we lose.

        // Build an InetAddress by passing the requested IP address to the
        // InetAddress class constructor. This will validate the sanity of the
        // IP by either accepting the requested value or throwing a
        // BindException.
        if (controller.isSecurityEnabled()
            && controller.getSecurity().isSSLEnabled())
        {
          ServerSocketFactory sslFact = SocketFactoryFactory
              .createServerFactory(controller.getSecurity().getSslConfig());
          serverSocket = sslFact.createServerSocket(controller.getPortNumber(),
              controller.getBacklogSize(), bindAddress);
        }
        else
        {
          serverSocket = new ServerSocket(controller.getPortNumber(),
              controller.getBacklogSize(), bindAddress);
        }
      }
      else
      { // Default value: no specific IP was requested or was left as the
        // default. Create a basic local socket.

        if (controller.isSecurityEnabled()
            && controller.getSecurity().isSSLEnabled())
        {
          ServerSocketFactory sslFact = SocketFactoryFactory
              .createServerFactory(controller.getSecurity().getSslConfig());
          serverSocket = sslFact.createServerSocket(controller.getPortNumber(),
              controller.getBacklogSize());
        }
        else
        {
          serverSocket = new ServerSocket(controller.getPortNumber(),
              controller.getBacklogSize());
        }
      }

    }
    catch (java.net.BindException e)
    { // Thrown if an illegal IP address was specified.
      logger.fatal(Translate.get("controller.server.thread.illegal.ip",
          new String[]{controller.getIPAddress(), e.getMessage()}));
      controller.endOfController(e);
    }
    catch (IOException e)
    {
      logger.fatal(Translate.get("controller.server.thread.socket.failed",
          controller.getPortNumber() + ""));
      controller.endOfController(e);
    }
    catch (SSLException e)
    {
      logger.fatal(Translate.get("controller.server.thread.socket.failed",
          controller.getPortNumber() + ""));
      controller.endOfController(e);
    }
    if (logger.isInfoEnabled())
    {
      logger.info(Translate.get("controller.server.thread.waiting.connections",
          new String[]{serverSocket.getInetAddress().getHostAddress(),
              String.valueOf(serverSocket.getLocalPort())}));
      logger.debug(Translate.get("controller.server.thread.backlog.size", ""
          + controller.getBacklogSize()));
    }
  }

  /**
   * Accepts connections from drivers, read the virtual database name and
   * returns the connection point.
   */
  public void run()
  {
    if (controller == null)
    {
      logger.error(Translate.get("controller.server.thread.controller.null"));
      isShuttingDown = true;
    }

    Socket clientSocket = null;
    while (!isShuttingDown)
    {
      try
      { // Accept a connection
        clientSocket = serverSocket.accept();
        if (isShuttingDown)
          break;
        if (controller.isSecurityEnabled()
            && !controller.getSecurity().allowConnection(clientSocket))
        {
          String errmsg = Translate.get(
              "controller.server.thread.connection.refused", clientSocket
                  .getInetAddress().getHostName());
          logger.warn(errmsg);
          CJDBCOutputStream out = new CJDBCOutputStream(clientSocket);
          out.writeBoolean(false);
          out.writeUTF(errmsg);
          out.flush(); // FIXME: should we .close() instead ?
          clientSocket = null;
          continue;
        }
        else
        {
          if (logger.isDebugEnabled())
            logger.debug(Translate.get(
                "controller.server.thread.connection.accept", clientSocket
                    .getInetAddress().getHostName()));
        }
        boolean createThread = false;
        if (isShuttingDown)
          break;
        synchronized (controllerServerThreadPendingQueue)
        {
          // Add the connection to the queue
          controllerServerThreadPendingQueue.add(clientSocket);
          // Check if we need to create a new thread or just wake up an
          // existing one
          if (idleWorkerThreads == 0)
            createThread = true;
          else
            // Here we notify all threads else if one thread doesn't wake up
            // after the first notify() we will send a second notify() and
            // one signal will be lost. So the safe way is to wake up everybody
            // and that worker threads go back to sleep if there is no job.
            controllerServerThreadPendingQueue.notifyAll();
        }
        if (createThread)
        { // Start a new worker thread if needed
          ControllerWorkerThread thread = new ControllerWorkerThread(this);
          thread.start();
          if (logger.isDebugEnabled())
            logger.debug(Translate.get("controller.server.thread.starting"));
        }
      }
      catch (IOException e)
      {
        if (!isShuttingDown)
        {
          logger.warn(Translate.get(
              "controller.server.thread.new.connection.error", e), e);
        }
      }
    }
    if (logger.isInfoEnabled())
      logger.info(Translate.get("controller.server.thread.terminating"));
  }

  /**
   * Refuse new connection to clients and finish transaction
   */
  public void shutdown()
  {
    isShuttingDown = true;
    // Shutting down server thread
    try
    {
      serverSocket.close();
    }
    catch (Exception e)
    {
      logger.warn(Translate.get("controller.shutdown.server.socket.exception"),
          e);
    }

    /*
     * Close pending connections (not yet served by any ControllerWorkerThread)
     * and wake up idle ControllerWorkerThreads.
     */
    Object lock = controllerServerThreadPendingQueue;
    synchronized (lock)
    {
      // close pending connections
      int nbSockets = controllerServerThreadPendingQueue.size();
      Socket socket = null;
      for (int i = 0; i < nbSockets; i++)
      {
        socket = (Socket) controllerServerThreadPendingQueue.get(i);
        logger.info(Translate.get("controller.shutdown.client.socket", socket
            .getInetAddress().toString()));

        try
        {
          socket.close();
        }
        catch (Exception e)
        {
          logger.warn(Translate
              .get("controller.shutdown.client.socket.exception"), e);
        }
      }

      // wake up idle ControllerWorkerThreads,
      // asking them to die (controllerServerThreadPendingQueue=null)
      this.controllerServerThreadPendingQueue = null;
      lock.notifyAll();
    }
  }

  /**
   * @return Returns the controllerServerThreadPendingQueue size.
   */
  public int getControllerServerThreadPendingQueueSize()
  {
    synchronized (controllerServerThreadPendingQueue)
    {
      return controllerServerThreadPendingQueue.size();
    }
  }

  /**
   * @return Returns the idleWorkerThreads.
   */
  public int getIdleWorkerThreads()
  {
    synchronized (controllerServerThreadPendingQueue)
    {
      return idleWorkerThreads;
    }
  }

  /**
   * Returns the isShuttingDown value.
   * 
   * @return Returns the isShuttingDown.
   */
  public boolean isShuttingDown()
  {
    return isShuttingDown;
  }
}