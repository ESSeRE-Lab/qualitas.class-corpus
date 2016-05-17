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

import java.util.ArrayList;

import org.objectweb.cjdbc.common.exceptions.NoMoreControllerException;
import org.objectweb.cjdbc.driver.CjdbcUrl;
import org.objectweb.cjdbc.driver.ControllerInfo;

/**
 * This class defines an OrderedConnectPolicy used when the C-JDBC URL has the
 * following form:
 * jdbc:cjdbc://node1,node2,node3/myDB?preferredController=ordered
 * <p>
 * This always direct to the first available controller in the list following
 * the order of the list. With this example, we first try node1, and if not
 * available then try to node2 and finally if none are available try node3.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class OrderedConnectPolicy extends AbstractControllerConnectPolicy
{
  private ArrayList availableControllerList;

  /**
   * Creates a new <code>OrderedConnectPolicy</code> object
   * 
   * @param controllerList list of controller from C-JDBC url
   * @param retryIntervalInMs Interval in milliseconds before retrying to
   *          re-connect to a controller that has failed
   * @param debugLevel the debug level to use
   * @see org.objectweb.cjdbc.driver.CjdbcUrl#DEBUG_LEVEL_OFF
   */
  public OrderedConnectPolicy(ControllerInfo[] controllerList,
      long retryIntervalInMs, int debugLevel)
  {
    super(controllerList, retryIntervalInMs, debugLevel);
    // Clone the list of controllers
    availableControllerList = new ArrayList(controllerList.length);
    for (int i = 0; i < controllerList.length; i++)
      availableControllerList.add(controllerList[i]);
  }

  /**
   * @see org.objectweb.cjdbc.driver.connectpolicy.AbstractControllerConnectPolicy#removeControllerFromSuspectList(org.objectweb.cjdbc.driver.ControllerInfo)
   */
  public synchronized void removeControllerFromSuspectList(
      ControllerInfo controller)
  {
    super.removeControllerFromSuspectList(controller);
    // Rebuild the list in the correct order
    availableControllerList.clear();
    for (int i = 0; i < controllerList.length; i++)
      if (!isSuspectedOfFailure(controllerList[i]))
        availableControllerList.add(controllerList[i]);
  }

  /**
   * @see org.objectweb.cjdbc.driver.connectpolicy.AbstractControllerConnectPolicy#suspectControllerOfFailure(org.objectweb.cjdbc.driver.ControllerInfo)
   */
  public synchronized void suspectControllerOfFailure(
      ControllerInfo controllerInfo)
  {
    super.suspectControllerOfFailure(controllerInfo);
    availableControllerList.remove(controllerInfo);
  }

  /**
   * @see org.objectweb.cjdbc.driver.connectpolicy.AbstractControllerConnectPolicy#getController()
   */
  public synchronized ControllerInfo getController()
      throws NoMoreControllerException
  {
    int size = availableControllerList.size();
    if (size == 0)
      throw new NoMoreControllerException();

    // Take the first available controller
    ControllerInfo controllerInfo = (ControllerInfo) availableControllerList
        .get(0);
    if (debugLevel == CjdbcUrl.DEBUG_LEVEL_DEBUG)
      System.out.println("Selected controller " + controllerInfo);
    return controllerInfo;
  }

}
