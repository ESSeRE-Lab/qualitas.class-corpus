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
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.driver;

import java.util.ArrayList;

import org.objectweb.cjdbc.driver.protocol.Commands;

/**
 * The <code>ConnectionClosingThread</code> wakes up every 5 seconds when
 * close() has been called on a connection and it frees the connection if it has
 * not been reused.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ConnectionClosingThread extends Thread
{
  /* wait time before closing a connection in ms */
  private static final int WAIT_TIME = 5000;

  private Driver           driver;
  private ArrayList        pendingConnectionClosing;

  /**
   * Builds a new ConnectionClosingThread
   * 
   * @param driver The driver that created us
   */
  public ConnectionClosingThread(Driver driver)
  {
    super("ConnectionClosingThread");
    this.driver = driver;
    this.pendingConnectionClosing = driver.pendingConnectionClosing;
    driver.connectionClosingThreadisAlive = true;
  }

  /**
   * The connection closing thread wakes up every WAIT_TIME seconds when close()
   * has been called on a connection and it frees the connection if it has not
   * been reused.
   */
  public void run()
  {
    try
    {
      Connection firstConnectionToClose = null;
      Connection lastConnectionToClose = null;
      int pendingConnectionSize;
      ArrayList closingList = new ArrayList();
      boolean killed = false;

      while (!killed)
      {
        synchronized (pendingConnectionClosing)
        {
          pendingConnectionSize = pendingConnectionClosing.size();
          if (pendingConnectionSize == 0)
            break;

          try
          {
            // Look at the connections in the queue before sleeping
            firstConnectionToClose = (Connection) pendingConnectionClosing
                .get(0);
            lastConnectionToClose = (Connection) pendingConnectionClosing
                .get(pendingConnectionSize - 1);

            // Sleep
            pendingConnectionClosing.wait(WAIT_TIME);
          }
          catch (InterruptedException ignore)
          {
          }

          pendingConnectionSize = pendingConnectionClosing.size();
          // Exit, no more connections
          if (pendingConnectionSize == 0)
            break;

          // Compare the queue now with its state when we got to sleep
          if (firstConnectionToClose == pendingConnectionClosing.get(0))
          { // Ok, the connection has not been reused, let's close it
            if (lastConnectionToClose == (Connection) pendingConnectionClosing
                .get(pendingConnectionSize - 1))
            { // No connection has been reused, remove them all
              closingList.addAll(pendingConnectionClosing);
              pendingConnectionClosing.clear();
              killed = true; // Let's die, there are no more connections
            }
            else
              // Close only the first connection
              closingList.add(pendingConnectionClosing.remove(0));
          }
        }

        // Effectively close the connections outside the synchronized block
        while (!closingList.isEmpty())
          closeConnection((Connection) closingList.remove(0));
      }
    }
    catch (RuntimeException e)
    {
      e.printStackTrace();
    }
    finally
    {
      synchronized (pendingConnectionClosing)
      {
        driver.connectionClosingThreadisAlive = false;
      }
    }
  }

  /**
   * Closes a connection. This cleanup should belong to the underlying class.
   * 
   * @param c the connection to close
   */
  private void closeConnection(Connection c)
  {
    try
    {
      // Unlink those objects so that this connection can be garbage collected
      c.driver = null;
      if (c.socketOutput != null)
      {
        c.socketOutput.writeInt(Commands.Close);
        c.socketOutput.flush();
        if (c.socketInput != null)
        { // Wait for the controller to receive the connection and close the
          // stream. If we do not wait for the controller ack, the connection is
          // closed on the controller before the closing is handled which
          // results in an ugly warning message on the controller side. We are
          // not in a hurry when closing the connection so let do the things
          // nicely!
          c.socketInput.readBoolean();
          c.socketInput.close();
        }
        c.socketOutput.close();
      }

      if (c.socket != null)
        c.socket.close();
    }
    catch (Exception ignore)
    {
    }
  }

}