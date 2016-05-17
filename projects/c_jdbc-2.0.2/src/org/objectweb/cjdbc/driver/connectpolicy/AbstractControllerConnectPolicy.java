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

import java.util.HashSet;

import org.objectweb.cjdbc.common.exceptions.NoMoreControllerException;
import org.objectweb.cjdbc.driver.CjdbcUrl;
import org.objectweb.cjdbc.driver.ControllerInfo;

/**
 * This class defines an AbstractControllerConnectPolicy used by the driver to
 * choose a controller to connect to.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public abstract class AbstractControllerConnectPolicy
{
  protected ControllerInfo[]   controllerList;
  protected HashSet            suspectedControllers;
  private long                 retryIntervalInMs;
  private ControllerPingThread controllerPingThread = null;
  protected int                debugLevel           = CjdbcUrl.DEBUG_LEVEL_OFF;

  /**
   * Creates a new <code>AbstractControllerConnectPolicy</code> object
   * 
   * @param controllerList the controller list on which the policy applies
   * @param retryIntervalInMs Interval in milliseconds before retrying to
   *          re-connect to a controller that has failed
   * @param debugLevel the debug level to use
   * @see org.objectweb.cjdbc.driver.CjdbcUrl#DEBUG_LEVEL_OFF
   */
  public AbstractControllerConnectPolicy(ControllerInfo[] controllerList,
      long retryIntervalInMs, int debugLevel)
  {
    if (controllerList == null)
      throw new NullPointerException(
          "Invalid null controller list in connect policy constructor");
    if (controllerList.length == 0)
      throw new RuntimeException(
          "Invalid empty controller list in connect policy constructor");
    this.controllerList = controllerList;
    this.suspectedControllers = new HashSet(controllerList.length);
    this.retryIntervalInMs = retryIntervalInMs;
    this.debugLevel = debugLevel;
  }

  /**
   * Terminate the controller ping thread if any and cleanup the suspected
   * controller list.
   * 
   * @see java.lang.Object#finalize()
   */
  protected void finalize() throws Throwable
  {
    super.finalize();
    // Kill controller ping thread
    suspectedControllers.clear();
    if (controllerPingThread != null)
      synchronized (controllerPingThread)
      {
        controllerPingThread.notify();
      }
  }

  /**
   * Get a controller using the implementation specific policy
   * 
   * @return <code>ControllerInfo</code> of the selected controller
   * @throws NoMoreControllerException if no controller in the controller list
   *           is reachable
   */
  public abstract ControllerInfo getController()
      throws NoMoreControllerException;

  /**
   * Returns the controllerList value.
   * 
   * @return Returns the controllerList.
   */
  public ControllerInfo[] getControllerList()
  {
    return controllerList;
  }

  /**
   * Returns the suspectedControllers value.
   * 
   * @return Returns the suspectedControllers.
   */
  public HashSet getSuspectedControllers()
  {
    return suspectedControllers;
  }

  /**
   * Returns true if the specified controller is suspected of failure.
   * 
   * @param controllerInfo the controller to check
   * @return true if the controller is in the suspect list
   */
  public boolean isSuspectedOfFailure(ControllerInfo controllerInfo)
  {
    return suspectedControllers.contains(controllerInfo);
  }

  /**
   * Sets the controllerList value.
   * 
   * @param controllerList The controllerList to set.
   */
  public void setControllerList(ControllerInfo[] controllerList)
  {
    this.controllerList = controllerList;
  }

  /**
   * Add the controller to the list of suspects.
   * 
   * @param controllerInfo the controller suspected of failure
   */
  public synchronized void suspectControllerOfFailure(
      ControllerInfo controllerInfo)
  {
    // Check that the controllerInfo is correct and add it to the list
    for (int i = 0; i < controllerList.length; i++)
    {
      ControllerInfo controller = controllerList[i];
      if (controller.equals(controllerInfo))
      {
        synchronized (suspectedControllers)
        {
          if (debugLevel >= CjdbcUrl.DEBUG_LEVEL_INFO)
            System.out.println("Controller " + controllerInfo
                + " is now suspected of failure");
          suspectedControllers.add(controllerInfo);
          // If no controller ping thread has been created then we create a new
          // one. As we are in the synchronized block, we are sure that the
          // thread cannot die now but if it is already dead, then we have to
          // create a new one (restart is not possible since the thread might
          // not be completely dead yet).
          // @see ControllerPingThread
          if ((controllerPingThread == null)
              || (controllerPingThread.isTerminated()))
          {
            controllerPingThread = new ControllerPingThread(this,
                retryIntervalInMs, debugLevel);
            controllerPingThread.start();
          }
          return;
        }
      }
    }
  }

  /**
   * Remove the specified controller from the list of suspect controllers
   * 
   * @param controller the controller to remove from the list
   */
  public void removeControllerFromSuspectList(ControllerInfo controller)
  {
    synchronized (suspectedControllers)
    {
      if (debugLevel >= CjdbcUrl.DEBUG_LEVEL_INFO)
        System.out.println("Controller " + controller
            + " is removed from suspect list");
      suspectedControllers.remove(controller);
    }
  }

}
