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

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.testlet.UpdateTestLet;

/**
 * This class defines a MemoryUsageScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class MemoryUsageScenario extends Raidb1Template
{
  private static final int BATCH              = 2000;
  private static final int MAX_MEMORY_ALLOWED = 10000;

  /**
   * Test memory usage in intensive write
   * 
   * @throws Exception if something fails
   */
  public void testIntensiveUpdateMemoryUsage() throws Exception
  {
    Connection con = getCJDBCConnection();
    UpdateTestLet let = new UpdateTestLet(con);
    let.executeBatch(BATCH);
    long timeUsage = let.getTotalTimeUsage();
    long totalUsed = let.getTotalMemoryUsage();
    System.out.println("USED:" + totalUsed + "k");
    System.out.println("Lapsed Time:" + timeUsage + "s");
    assertTrue("Memory leak", totalUsed < MAX_MEMORY_ALLOWED);
  }

  /**
   * Test memory usage in intensive write
   * 
   * @throws Exception if something fails
   */
  public void testIntensiveUpdateMemoryUsageWithHypersonic() throws Exception
  {
    Connection con = getHypersonicConnection(9001);
    UpdateTestLet let = new UpdateTestLet(con);
    let.executeBatch(BATCH);
    long timeUsage = let.getTotalTimeUsage();
    long totalUsed = let.getTotalMemoryUsage();
    System.out.println("USED:" + totalUsed + "k");
    System.out.println("Lapsed Time:" + timeUsage + "s");
    assertTrue("Memory leak", totalUsed < MAX_MEMORY_ALLOWED);
  }

}