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


/**
 * This class defines a Raidb1Template.
 * 
 * This class extends the SimpleTemplate 
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modryzk</a>
 * @version 1.0
 */
public class Raidb1Template extends SimpleRaidb1Template
{

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  { 
    try
    {
      super.setUp();
      cm.loaddatabase("25322","hsqldb-raidb1.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
    }
    catch(Exception e)
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
