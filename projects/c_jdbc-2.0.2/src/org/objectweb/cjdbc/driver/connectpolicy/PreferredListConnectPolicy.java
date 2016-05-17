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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.exceptions.NoMoreControllerException;
import org.objectweb.cjdbc.driver.CjdbcUrl;
import org.objectweb.cjdbc.driver.ControllerInfo;

/**
 * This class defines a PreferredListConnectPolicy
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class PreferredListConnectPolicy extends AbstractControllerConnectPolicy
{
  private int       index = -1;
  private ArrayList preferredControllers;
  private ArrayList deadPreferredControllers;

  /**
   * Creates a new <code>PreferredListConnectPolicy</code> object
   * 
   * @param controllerList list of controller from C-JDBC url
   * @param retryIntervalInMs Interval in milliseconds before retrying to
   *          re-connect to a controller that has failed
   * @param preferredControllerList comma separated list of preferred
   *          controllers
   * @param debugLevel the debug level to use
   * @see org.objectweb.cjdbc.driver.CjdbcUrl#DEBUG_LEVEL_OFF
   */
  public PreferredListConnectPolicy(ControllerInfo[] controllerList,
      long retryIntervalInMs, String preferredControllerList, int debugLevel)
  {
    super(controllerList, retryIntervalInMs, debugLevel);

    // Check the validity of each controller in the list
    StringTokenizer controllers = new StringTokenizer(preferredControllerList,
        ",", true);
    int tokenNumber = controllers.countTokens();
    preferredControllers = new ArrayList(tokenNumber - 1);
    deadPreferredControllers = new ArrayList(tokenNumber - 1);
    int i = 0;
    String s;
    boolean lastTokenWasComma = false;
    while (controllers.hasMoreTokens())
    {
      s = controllers.nextToken().trim();
      if (s.equals(","))
      {
        if (lastTokenWasComma || (i == 0) || (i == tokenNumber - 1))
          // ',' cannot be the first or the last token
          // another ',' cannot follow a ','
          throw new RuntimeException(
              "Syntax error in controller list for preferredController attribute '"
                  + preferredControllerList + "'");
        else
        {
          lastTokenWasComma = true;
          continue;
        }
      }
      lastTokenWasComma = false;
      try
      {
        preferredControllers.add(CjdbcUrl.parseController(s));
      }
      catch (SQLException e)
      {
        throw new RuntimeException("Invalid controller " + s
            + " in controller list for preferredController attribute");
      }
      i++;
    }

  }

  /**
   * @see org.objectweb.cjdbc.driver.connectpolicy.AbstractControllerConnectPolicy#removeControllerFromSuspectList(org.objectweb.cjdbc.driver.ControllerInfo)
   */
  public synchronized void removeControllerFromSuspectList(
      ControllerInfo controller)
  {
    super.removeControllerFromSuspectList(controller);
    if (deadPreferredControllers.remove(controller))
      preferredControllers.add(controller);
  }

  /**
   * @see org.objectweb.cjdbc.driver.connectpolicy.AbstractControllerConnectPolicy#suspectControllerOfFailure(org.objectweb.cjdbc.driver.ControllerInfo)
   */
  public synchronized void suspectControllerOfFailure(
      ControllerInfo controllerInfo)
  {
    super.suspectControllerOfFailure(controllerInfo);
    if (preferredControllers.remove(controllerInfo))
      deadPreferredControllers.add(controllerInfo);
  }

  /**
   * @see org.objectweb.cjdbc.driver.connectpolicy.AbstractControllerConnectPolicy#getController()
   */
  public synchronized ControllerInfo getController()
      throws NoMoreControllerException
  {
    if (suspectedControllers.size() == controllerList.length)
      throw new NoMoreControllerException();

    if (preferredControllers.isEmpty())
    { // Find 1st available controller in round-robin
      do
      {
        index = (index + 1) % controllerList.length;
      }
      while (suspectedControllers.contains(controllerList[index]));
      if (debugLevel == CjdbcUrl.DEBUG_LEVEL_DEBUG)
        System.out.println("Selected controller[" + index + "]:"
            + controllerList[index]);
      return controllerList[index];
    }
    else
    {
      index = (index + 1) % preferredControllers.size();
      ControllerInfo controllerInfo = (ControllerInfo) preferredControllers
          .get(index);
      if (debugLevel == CjdbcUrl.DEBUG_LEVEL_DEBUG)
        System.out.println("Selected controller[" + index + "]:"
            + controllerInfo);
      return controllerInfo;
    }
  }

}
