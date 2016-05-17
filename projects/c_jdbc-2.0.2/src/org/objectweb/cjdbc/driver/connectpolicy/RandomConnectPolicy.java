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
import java.util.Random;

import org.objectweb.cjdbc.common.exceptions.NoMoreControllerException;
import org.objectweb.cjdbc.driver.CjdbcUrl;
import org.objectweb.cjdbc.driver.ControllerInfo;

/**
 * This class defines a RandomConnectPolicy used when the C-JDBC URL has the
 * following form:
 * jdbc:cjdbc://node1,node2,node3/myDB?preferredController=random
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class RandomConnectPolicy extends AbstractControllerConnectPolicy
{
  private Random    rand;
  private ArrayList availableControllerList;

  /**
   * Creates a new <code>RandomConnectPolicy</code> object
   * 
   * @param controllerList list of controller from C-JDBC url
   * @param retryIntervalInMs Interval in milliseconds before retrying to
   *          re-connect to a controller that has failed
   * @param debugLevel the debug level to use
   * @see org.objectweb.cjdbc.driver.CjdbcUrl#DEBUG_LEVEL_OFF
   */
  public RandomConnectPolicy(ControllerInfo[] controllerList,
      long retryIntervalInMs, int debugLevel)
  {
    super(controllerList, retryIntervalInMs, debugLevel);
    // Clone the list of controllers
    availableControllerList = new ArrayList(controllerList.length);
    for (int i = 0; i < controllerList.length; i++)
    {
      if (controllerList[i] == null)
        throw new RuntimeException(
            "Invalid null controller in list while instanciating RandomConnectPolicy");
      availableControllerList.add(controllerList[i]);
    }
    rand = new Random(System.currentTimeMillis());
  }

  /**
   * @see org.objectweb.cjdbc.driver.connectpolicy.AbstractControllerConnectPolicy#removeControllerFromSuspectList(org.objectweb.cjdbc.driver.ControllerInfo)
   */
  public void removeControllerFromSuspectList(ControllerInfo controller)
  {
    super.removeControllerFromSuspectList(controller);
    availableControllerList.add(controller);
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

    ControllerInfo controllerInfo = (ControllerInfo) availableControllerList
        .get(rand.nextInt(size));
    if (debugLevel == CjdbcUrl.DEBUG_LEVEL_DEBUG)
      System.out.println("Selected controller " + controllerInfo);
    return controllerInfo;
  }

}
