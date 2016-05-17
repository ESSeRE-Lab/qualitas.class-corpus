/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.scenario.horizontal;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.scenario.templates.HorizontalTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * We want to test on a read request, that if a controller has lost all its
 * backends, the driver should send the request on the other controller.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ControllerHasLostAllBackendsFailoverScenario
    extends HorizontalTemplate
{
  /**
   * We send a read request on a controller that has no more backend, and we
   * want the other controller to send the result with its backends
   * 
   * @throws Exception if fails
   */
  public void testReadFailover() throws Exception
  {
    Connection con = getCJDBCConnection(new ControllerInfo[]{
        new ControllerInfo("localhost", 25322),
        new ControllerInfo("localhost", 25323)});
    hm.stop(hm1);
    hm.stop(hm2);
    String query = "Select * from document";
    ScenarioUtility.displaySingleQueryResult(query, con);
  }

  /**
   * We send a write request on a controller that has no more backend, and we
   * want the other controller to send the result with its backends
   * 
   * @throws Exception if fails
   */
  public void testWriteFailover() throws Exception
  {
    Connection con = getCJDBCConnection(new ControllerInfo[]{
        new ControllerInfo("localhost", 25322),
        new ControllerInfo("localhost", 25323)});
    hm.stop(hm1);
    hm.stop(hm2);
    String query = "update document set total=?";
    PreparedStatement ps = con.prepareStatement(query);
    ps.setDouble(1, 0.0);
    int updateC = ps.executeUpdate();
    logger.warn("Number of updates:" + updateC);
    assertTrue(updateC > 1);
  }
}