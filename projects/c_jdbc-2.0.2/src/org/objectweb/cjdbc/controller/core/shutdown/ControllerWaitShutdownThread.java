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

package org.objectweb.cjdbc.controller.core.shutdown;

import org.objectweb.cjdbc.common.exceptions.ShutdownException;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.controller.core.Controller;

/**
 * This thread waits for all clients to terminate their connections before
 * shutting down the controller.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class ControllerWaitShutdownThread extends ControllerShutdownThread
{

  /**
   * Creates a new <code>ControllerWaitShutdownThread</code> object
   * 
   * @param controller the controller to shutdown
   */
  public ControllerWaitShutdownThread(Controller controller)
  {
    super(controller, Constants.SHUTDOWN_WAIT);
  }

  /**
   * @see org.objectweb.cjdbc.controller.core.shutdown.ShutdownThread#shutdown()
   */
  public void shutdown() throws ShutdownException
  {
    logger.info("Starting controller shutdown");
    shutdownDatabases();
    generateReportIfNeeded();
    shutdownJmxAgent();
    shutdownServerConnectionThread(0);
    logger.info("Controller shutdown completed");
  }

}