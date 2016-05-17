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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): 
 */

package org.objectweb.cjdbc.common.monitor.controller;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.controller.core.Controller;

/**
 * Get the worker pending queue size
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 */
public class ControllerWorkerPendingQueueCollector
    extends AbstractControllerDataCollector
{
  private static final long serialVersionUID = 1948732510266075664L;

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#collectValue()
   */
  public long collectValue()
  {
    return ((Controller) controller).getConnectionThread()
        .getControllerServerThreadPendingQueueSize();
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#getDescription()
   */
  public String getDescription()
  {
    return Translate.get("monitoring.controller.pending.queue");
  }

  /**
   * Creates a new <code>ControllerWorkerPendingQueueCollector.java</code>
   * object
   * 
   * @param controller to collect data from
   */
  public ControllerWorkerPendingQueueCollector(Object controller)
  {
    super(controller);
  }
}
