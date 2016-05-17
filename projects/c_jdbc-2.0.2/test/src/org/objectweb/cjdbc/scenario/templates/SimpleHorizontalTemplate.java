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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.templates;

import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.scenario.tools.ScenarioConstants;
import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;
import org.objectweb.cjdbc.scenario.tools.components.backend.DatabaseManager;
import org.objectweb.cjdbc.scenario.tools.components.controller.ControllerManager;

/**
 * This class defines a SimpleHorizontalTemplate
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class SimpleHorizontalTemplate extends Template
{
  protected ControllerManager cm = new ControllerManager();
  protected DatabaseManager   hm = new DatabaseManager();
  protected ComponentInterface hm1 = null, hm2 = null, hm3 = null, hm4 = null;
  protected Controller         controller1 = null, controller2 = null;
  protected VirtualDatabase mainVdb1 = null, mainVdb2 = null;
  protected String port1 = "25322";
  protected String port2 = "25323";

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    try
    {
      // Start hypersonic databases
      hm1 = hm.start("9001");
      hm.loaddatabase("9001");
      hm2 = hm.start("9002");
      hm.loaddatabase("9002");
      hm3 = hm.start("9003");
      hm.loaddatabase("9003");
      hm4 = hm.start("9004");
      hm.loaddatabase("9004");

      // Start controllers
      // Load controller1
      controller1 = (Controller) cm.start(port1).getProcess();
      // Load controller2
      controller2 = (Controller) cm.start(port2).getProcess();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  protected void setDatabasesUp(String databaseFileDesc) throws Exception
  {
    cm.loaddatabase(port1,databaseFileDesc+"-1.xml");
    mainVdb1 = controller1.getVirtualDatabase("myDB");
    mainVdb1.enableAllBackends();

    cm.loaddatabase(port2,databaseFileDesc+"-2.xml");
    mainVdb2 = controller2.getVirtualDatabase("myDB");
    mainVdb2.enableAllBackends();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {
    System.out.println("Tearing down horizontal template");
    cm.stopAll();
    hm.stopAll();

    int total = (int) ScenarioConstants.WAIT_TIME_BETWEEN_HORIZONTAL_TEST
        / ScenarioConstants.WAIT_TIME_REFRESH_HORIZONTAL_TEST;
    System.out.println("Waiting for jgroup to close(" + total + "):");
    synchronized (this)
    {
      for (int i = 0; i < total; i++)
      {
        try
        {
          System.out.println("*");
          System.out.flush();
          wait(ScenarioConstants.WAIT_TIME_REFRESH_HORIZONTAL_TEST);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
          break;
        }
      }
    }
    System.out.println("Supposing all jgroups thread have timed out");
  }

}