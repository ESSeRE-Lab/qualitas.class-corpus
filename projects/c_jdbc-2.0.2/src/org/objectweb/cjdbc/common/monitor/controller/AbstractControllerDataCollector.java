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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): 
 */

package org.objectweb.cjdbc.common.monitor.controller;

import org.objectweb.cjdbc.common.monitor.AbstractDataCollector;
import org.objectweb.cjdbc.controller.core.Controller;

/**
 * Abstract data collector to factor code for the controller collectors
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public abstract class AbstractControllerDataCollector
    extends
      AbstractDataCollector
{
  private String controllerName;

  /**
   * Default constructors
   */
  public AbstractControllerDataCollector()
  {
    //controllerName = ControllerConstants.DEFAULT_IP;
  }
  
  /**
   * Create a new collector for controller and set the name
   *@param controller attached to the collector
   */
  public AbstractControllerDataCollector(Object controller)
  {
    controllerName = ((Controller)controller).getIPAddress();
    this.controller = controller;
  }

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#collectValue()
   */
  public abstract long collectValue();

  /**
   * @see org.objectweb.cjdbc.common.monitor.AbstractDataCollector#getTargetName()
   */
  public String getTargetName()
  {
    return controllerName;
  }
}
