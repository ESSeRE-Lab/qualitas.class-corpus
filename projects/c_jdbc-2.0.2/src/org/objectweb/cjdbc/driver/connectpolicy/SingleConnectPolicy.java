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

import org.objectweb.cjdbc.common.exceptions.NoMoreControllerException;
import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.driver.Driver;

/**
 * This class defines a SingleConnectPolicy used when a C-JDBC URL only contains
 * one controller.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class SingleConnectPolicy extends AbstractControllerConnectPolicy
{

  /**
   * Creates a new <code>SingleConnectPolicy</code> object
   * 
   * @param controllerList list of controller from C-JDBC url
   * @param debugLevel the debug level to use
   * @see org.objectweb.cjdbc.driver.CjdbcUrl#DEBUG_LEVEL_OFF
   */
  public SingleConnectPolicy(ControllerInfo[] controllerList, int debugLevel)
  {
    super(controllerList, Driver.DEFAULT_RETRY_INTERVAL_IN_MS, debugLevel);
    if (controllerList.length != 1)
      throw new RuntimeException("Invalid number of controllers ("
          + controllerList.length + ") in URL for SingleConnectPolicy");
  }

  /**
   * @see org.objectweb.cjdbc.driver.connectpolicy.AbstractControllerConnectPolicy#getController()
   */
  public ControllerInfo getController() throws NoMoreControllerException
  {
    if (suspectedControllers.size() == 1)
      throw new NoMoreControllerException();
    return controllerList[0];
  }

}
