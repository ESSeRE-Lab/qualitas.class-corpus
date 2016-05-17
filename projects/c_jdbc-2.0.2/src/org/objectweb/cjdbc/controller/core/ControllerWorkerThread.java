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
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.controller.core;

import java.io.IOException;
import java.io.OptionalDataException;
import java.net.Socket;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread;
import org.objectweb.cjdbc.driver.protocol.Commands;

/**
 * The <code>ControllerWorkerThread</code> handles a connection with a C-JDBC
 * driver. It reads a String containing the virtual database name from the
 * driver and sends back the corresponding <code>ConnectionPoint</code>.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ControllerWorkerThread extends Thread
{
  private ControllerServerThread serverThread;
  private boolean                isKilled = false;

  /** Logger instance. */
  static Trace                   logger   = Trace
                                              .getLogger("org.objectweb.cjdbc.controller.core.Controller");

  /*
   * Constructor
   */

  /**
   * Creates a new <code>ControllerWorkerThread</code> instance.
   * 
   * @param serverThread the <code>ControllerServerThread</code> that created
   *          us.
   */
  public ControllerWorkerThread(ControllerServerThread serverThread)
  {
    super("ControllerWorkerThread");
    this.serverThread = serverThread;
  }

  /**
   * Gets a connection from the connection queue and process it.
   */
  public void run()
  {
    Socket clientSocket;

    if (serverThread == null)
    {
      logger.error(Translate.get("controller.workerthread.null.serverthread"));
      isKilled = true;
    }
    else if (serverThread.controllerServerThreadPendingQueue == null)
    {
      logger.error(Translate.get("controller.workerthread.null.pendingqueue"));
      isKilled = true;
    }

    // Main loop
    while (!isKilled)
    {
      if (serverThread.isShuttingDown())
        break;
      // Get a connection from the pending queue
      synchronized (serverThread.controllerServerThreadPendingQueue)
      {
        while (serverThread.controllerServerThreadPendingQueue.isEmpty())
        {
          // Nothing to do, let's sleep ...
          serverThread.idleWorkerThreads++;
          boolean timeout = false;
          try
          {
            long before = System.currentTimeMillis();
            serverThread.controllerServerThreadPendingQueue
                .wait(ControllerConstants.DEFAULT_CONTROLLER_WORKER_THREAD_SLEEP_TIME);
            long now = System.currentTimeMillis();
            // Check if timeout has expired
            timeout = now - before >= ControllerConstants.DEFAULT_CONTROLLER_WORKER_THREAD_SLEEP_TIME;
          }
          catch (InterruptedException ignore)
          {
          }
          serverThread.idleWorkerThreads--;
          // We are shutting down
          if (serverThread.controllerServerThreadPendingQueue == null)
          {
            isKilled = true;
            break;
          }
          if (timeout
              && serverThread.controllerServerThreadPendingQueue.isEmpty())
          {
            // Nothing to do, let's die.
            isKilled = true;
            break;
          }
        }

        if (isKilled)
          break;

        // Get a connection
        clientSocket = (Socket) serverThread.controllerServerThreadPendingQueue
            .remove(0);
      } // synchronized (serverThread.controllerServerThreadPendingQueue)

      if (clientSocket == null)
      {
        logger.error(Translate.get("controller.workerthread.null.socket"));
        continue;
      }
      else if (logger.isDebugEnabled())
        logger.debug(Translate.get("controller.workerthread.connection.from",
            new String[]{clientSocket.getInetAddress().toString(),
                String.valueOf(clientSocket.getPort())}));

      try
      {
        // Disable Nagle algorithm else small messages are not sent
        // (at least under Linux) even if we flush the output stream.
        clientSocket.setTcpNoDelay(true);

        // Handle connection
        CJDBCInputStream in = new CJDBCInputStream(clientSocket);
        CJDBCOutputStream out = new CJDBCOutputStream(clientSocket);

        // Check protocol version for driver compatibility
        int driverVersion = in.readInt();

        if (driverVersion != Commands.ProtocolVersion)
        {
          if (driverVersion != Commands.Ping)
            logger
                .warn(Translate.get(
                    "controller.workerthread.protocol.incompatible",
                    driverVersion));
          else
          {
            if (logger.isDebugEnabled())
              logger.debug("Controller pinged");
            try
            {
              // Close the socket
              clientSocket.close();
            }
            catch (Exception ignore)
            {
            }
          }
          continue;
        }
        // Driver version OK
        String virtualDatabaseName = in.readUTF();

        // Read the virtual database name
        VirtualDatabase vdb = serverThread.controller
            .getVirtualDatabase(virtualDatabaseName);
        if (vdb == null)
        {
          String msg = Translate.get("virtualdatabase.not.found",
              virtualDatabaseName);
          logger.warn(msg);
          continue;
        }
        if (vdb.isShuttingDown())
        {
          String msg = Translate.get("virtualdatabase.shutting.down",
              virtualDatabaseName);
          logger.warn(msg);
          continue;
        }

        // At this point we have the virtual database the driver wants to
        // connect to and we have to give the job to a
        // VirtualDatabaseWorkerThread
        ArrayList vdbActiveThreads = vdb.getActiveThreads();
        ArrayList vdbPendingQueue = vdb.getPendingConnections();

        if (vdbActiveThreads == null)
        {
          logger.error(Translate
              .get("controller.workerthread.null.active.thread"));
          isKilled = true;
        }
        if (vdbPendingQueue == null)
        {
          logger
              .error(Translate.get("controller.workerthread.null.connection"));
          isKilled = true;
        }

        // Start minimum number of worker threads
        boolean tooManyConnections;
        synchronized (vdbActiveThreads)
        {
          while (vdb.getCurrentNbOfThreads() < vdb.getMinNbOfThreads())
          {
            forkVirtualDatabaseWorkerThread(vdb,
                "controller.workerthread.starting.thread.for.minimum");
          }

          // Check if maximum number of concurrent connections has been
          // reached
          tooManyConnections = (vdb.getMaxNbOfConnections() > 0)
              && vdbActiveThreads.size() + vdbPendingQueue.size() > vdb
                  .getMaxNbOfConnections();
        }
        if (tooManyConnections)
        {
          out.writeBoolean(false);
          out.writeUTF(Translate
              .get("controller.workerthread.too.many.connections"));
          out.close(); // closing is OK ?
          continue;
        }

        // Put the connection in the queue
        synchronized (vdbPendingQueue)
        {
          vdbPendingQueue.add(in);
          vdbPendingQueue.add(out);
          // Nullify the socket else it is closed in the finally block
          clientSocket = null;
          synchronized (vdbActiveThreads)
          { // Is a thread available?
            if (vdb.getIdleThreads() == 0)
            { // No
              if ((vdb.getCurrentNbOfThreads() <= vdb.getMaxNbOfThreads())
                  || (vdb.getMaxNbOfThreads() == 0))
              {
                forkVirtualDatabaseWorkerThread(vdb,
                    "controller.workerthread.starting.thread");
              }
              else if (logger.isInfoEnabled())
                logger.info(Translate.get(
                    "controller.workerthread.maximum.thread", vdb
                        .getMaxNbOfThreads()));
            }
            else
            {
              if (logger.isDebugEnabled())
                logger.debug(Translate
                    .get("controller.workerthread.notify.thread"));
              // Here we notify all threads else if one thread doesn't wake
              // up after the first notify() we will send a second notify()
              // and one signal will be lost. So the safe way is to wake up
              // everybody and that worker threads go back to sleep if there
              // is no job.
              vdbPendingQueue.notifyAll();
            }
          }
        }
      }
      // }
      catch (OptionalDataException e)
      {
        logger
            .error(Translate.get("controller.workerthread.protocol.error", e));
      }
      catch (IOException e)
      {
        logger.error(Translate.get("controller.workerthread.io.error", e));
      }
      finally
      {
        try
        {
          if (clientSocket != null)
          {
            if (logger.isDebugEnabled())
              logger.debug(Translate
                  .get("controller.workerthread.connection.closing"));
            clientSocket.close();
          }
        }
        catch (IOException ignore)
        {
        }
      }
    }

    if (logger.isDebugEnabled())
      logger.debug(Translate.get("controller.workerthread.terminating"));
  }

  /**
   * Fork a new worker thread.
   * 
   * @param vdb VirtualDatabase to be served
   * @param debugmesg debug message for the controller log
   */
  private void forkVirtualDatabaseWorkerThread(VirtualDatabase vdb,
      String debugmesg)
  {
    if (logger.isDebugEnabled())
      logger.debug(Translate.get(debugmesg));
    VirtualDatabaseWorkerThread thread;

    thread = new VirtualDatabaseWorkerThread(serverThread.controller, vdb);

    vdb.getActiveThreads().add(thread);
    vdb.addCurrentNbOfThread();
    thread.start();
  }
}