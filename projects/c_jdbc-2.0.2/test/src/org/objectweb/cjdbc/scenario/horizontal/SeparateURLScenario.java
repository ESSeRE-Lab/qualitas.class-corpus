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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.scenario.horizontal;

import java.sql.Connection;

import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.scenario.templates.HorizontalRAIDb1WithCacheTemplate;
import org.objectweb.cjdbc.scenario.tools.testlet.UpdateTestLet;

/**
 * This class defines a SeparateURLScenario. We want to test connection on each
 * controller in a Horizontal Scalability scenario. We do an update and then a
 * select statement just after
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class SeparateURLScenario extends HorizontalRAIDb1WithCacheTemplate
{
  /**
   * Test queries on each controller in turn.
   * 
   * @throws Exception if fails
   */
  public void testControllers() throws Exception
  {
    ControllerInfo[] c2 = new ControllerInfo[]{new ControllerInfo("localhost",
        25323)};

    Connection con2 = getCJDBCConnection(c2);
    UpdateTestLet let = new UpdateTestLet(con2);
    let.set(UpdateTestLet.UPDATED_COLUMN_VALUE, "25323");
    let.execute();

    ControllerInfo[] c1 = new ControllerInfo[]{new ControllerInfo("localhost",
        25322)};
    Connection con1 = getCJDBCConnection(c1);
    let = new UpdateTestLet(con1);
    let.set(UpdateTestLet.UPDATED_COLUMN_VALUE, "25322");
    let.execute();
  }

}