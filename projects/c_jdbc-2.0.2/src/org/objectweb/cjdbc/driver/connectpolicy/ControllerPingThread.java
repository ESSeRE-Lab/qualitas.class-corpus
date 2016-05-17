/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
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

package org.objectweb.cjdbc.driver.connectpolicy;

import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;

import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;
import org.objectweb.cjdbc.driver.CjdbcUrl;
import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.driver.protocol.Commands;

/**
 * This class defines a ControllerPingThread that periodically tries to connect
 * to a failed controller to check if it is back online.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class ControllerPingThread extends Thread
{
  private AbstractControllerConnectPolicy policy;
  private long                            retryIntervalInMs;
  private boolean                         threadTerminated = false;
  private int                             debugLevel;

  /**
   * Creates a new <code>ControllerPingThread</code> object.
   * 
   * @param policy controller connection policy.
   * @param retryIntervalInMs interval in ms between 2 pings (checks).
   * @param debugLevel the debug level to use
   */
  public ControllerPingThread(AbstractControllerConnectPolicy policy,
      long retryIntervalInMs, int debugLevel)
  {
    super("ControllerPingThread");
    this.policy = policy;
    this.retryIntervalInMs = retryIntervalInMs;
    this.debugLevel = debugLevel;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    HashSet suspectedControllers = policy.getSuspectedControllers();
    synchronized (suspectedControllers)
    {
      while (!suspectedControllers.isEmpty())
      {
        // Sleep
        try
        {
          suspectedControllers.wait(retryIntervalInMs);
        }
        catch (InterruptedException ignore)
        {
        }

        // Try to connect to each controller
        for (Iterator iter = suspectedControllers.iterator(); iter.hasNext();)
        {
          ControllerInfo controller = (ControllerInfo) iter.next();
          try
          {
            Socket socket = new Socket(controller.getHostname(), controller
                .getPort());

            // Connection successful

            // Disable Nagle algorithm else small messages are not sent
            // (at least under Linux) even if we flush the output stream.
            socket.setTcpNoDelay(true);
            CJDBCOutputStream out = new CJDBCOutputStream(socket);
            // Tell the controller not to worry, just a ping
            out.writeInt(Commands.Ping);
            out.flush();
            // The socket will be closed by the controller

            // Full success, remove controller from suspect list
            policy.removeControllerFromSuspectList(controller);
            // Regenerate iterator
            iter = suspectedControllers.iterator();
          }
          catch (Exception failed)
          {
            if (debugLevel == CjdbcUrl.DEBUG_LEVEL_DEBUG)
              System.out.println("Ping failed to controller " + controller
                  + " (" + failed + ")");
          }
        }
      } // while
      this.threadTerminated = true;
    } // synchronized
  }

  /**
   * Returns true if the thread is terminated.
   * 
   * @return true if the thread is terminated.
   */
  public boolean isTerminated()
  {
    return threadTerminated;
  }
}
