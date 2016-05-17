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
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.scenario.raidb1.startup;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;

/**
 * This class defines a Raidb1DynamicPrecisionScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk</a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class Raidb1DynamicPrecisionScenario extends SimpleRaidb1Template
{
  /**
   * Test setting of DynamicPrecisionStatic
   * 
   * @throws Exception if fails
   */
  public void testDynamicPrecisionStatic() throws Exception
  {
    cm.loadVirtualDatabases(controller, "myDB", "hsqldb-raidb1-dynamic-precision-static.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();
  }

  /**
   * Test setting of DynamicPrecisionTable
   * 
   * @throws Exception if fails
   */
  public void testDynamicPrecisionTable() throws Exception
  {
    cm.loadVirtualDatabases(controller, "myDB", "hsqldb-raidb1-dynamic-precision-table.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();
  }

  /**
   * Test setting of DynamicPrecisionColumn
   * 
   * @throws Exception if fails
   */
  public void testDynamicPrecisionColumn() throws Exception
  {
    cm.loadVirtualDatabases(controller, "myDB", "hsqldb-raidb1-dynamic-precision-column.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();
  }

  /**
   * Test setting of DynamicPrecisionProcedures
   * 
   * @throws Exception if fails
   */
  public void testDynamicPrecisionProcedures() throws Exception
  {
    cm.loadVirtualDatabases(controller, "myDB", "hsqldb-raidb1-dynamic-precision-procedures.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();
  }

  /**
   * Test setting of DynamicPrecisionAll
   * 
   * @throws Exception if fails
   */
  public void testDynamicPrecisionAll() throws Exception
  {
    cm.loadVirtualDatabases(controller, "myDB", "hsqldb-raidb1-dynamic-precision-all.xml");
    mainVdb = controller.getVirtualDatabase("myDB");
    mainVdb.enableAllBackends();
  }
}
