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
import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;
import org.objectweb.cjdbc.scenario.tools.components.backend.DatabaseManager;
import org.objectweb.cjdbc.scenario.tools.components.controller.ControllerManager;

/**
 * This class defines a ExtendedRaidb1RecoveryTemplate.
 * The recovery log is a virtual database itself running inside the same controller.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class ExtendedRaidb1RecoveryTemplate extends Template
{
  protected ControllerManager cm         = new ControllerManager();
  protected DatabaseManager hm         = new DatabaseManager();
  protected ComponentInterface hm1        = null, hm2 = null, hm3 = null,hm4=null;
  protected Controller        controller = null;
  protected VirtualDatabase   mainVdb;
  protected VirtualDatabase recoveryVdb;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    try
    {
      hm1 = hm.start("9001");
      hm.loaddatabase("9001");
      hm2 = hm.start("9002");
      hm.loaddatabase("9002");
      hm3 = hm.start("9003");
      hm.loaddatabase("9003");
      hm4 = hm.start("9004");
      hm.loaddatabase("9004");
      controller = (Controller)cm.start("25322").getProcess();
      
      cm.loaddatabase("25322","hsqldb-recovery-recoveryvdb.xml","Recovery");
      recoveryVdb = controller.getVirtualDatabase("Recovery");
      recoveryVdb.enableAllBackends();
      
      cm.loaddatabase("25322","hsqldb-recovery-mainvdb.xml","MainVdb");
      mainVdb = controller.getVirtualDatabase("MainVdb");
      mainVdb.enableAllBackends();
      
      
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {
    cm.stopAll();
    hm.stopAll();
  }

}
