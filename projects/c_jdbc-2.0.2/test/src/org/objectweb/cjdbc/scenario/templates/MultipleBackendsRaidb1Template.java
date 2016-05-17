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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.templates;

import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;


/**
 * This class defines a Raidb1Template.
 * 
 * This class extends the SimpleTemplate 
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modryzk</a>
 * @version 1.0
 */
public abstract class MultipleBackendsRaidb1Template extends SimpleRaidb1Template
{
  protected ComponentInterface hm3               = null;
  protected ComponentInterface hm4               = null;
  protected ComponentInterface hm5               = null;
  protected ComponentInterface hm6               = null;
  protected ComponentInterface hm7               = null;
  protected ComponentInterface hm8               = null;
  protected ComponentInterface hm9               = null;
  protected ComponentInterface hm10               = null;
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    try
    {
      //Recovery log
      hm.start("9000");
      hm.loaddatabase("9000");
      hm1 = hm.start("9001");
      hm.loaddatabase("9001");
      hm2 = hm.start("9002");
      hm.loaddatabase("9002");
      hm3 = hm.start("9003");
      hm.loaddatabase("9003");
      hm4 = hm.start("9004");
      hm.loaddatabase("9004");
      hm5 = hm.start("9005");
      hm.loaddatabase("9005");
      hm6 = hm.start("9006");
      hm.loaddatabase("9006");
      hm7 = hm.start("9007");
      hm.loaddatabase("9007");
      hm8 = hm.start("9008");
      hm.loaddatabase("9008");
      hm9 = hm.start("9009");
      hm.loaddatabase("9009");
      //hm10 = hm.start("9010");
      //hm.loaddatabase("9010");
      
      controller = (Controller) cm.start("25322").getProcess();
      
      cm.loaddatabase("25322","hsqldb-raidb1-multiple-backends.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("Could not start controller");
      tearDown();
    }
  }
  
  
  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {
    super.tearDown();
  }
}
