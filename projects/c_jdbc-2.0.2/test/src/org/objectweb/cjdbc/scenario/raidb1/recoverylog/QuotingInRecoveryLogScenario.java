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

package org.objectweb.cjdbc.scenario.raidb1.recoverylog;

import java.sql.Connection;

import org.objectweb.cjdbc.scenario.templates.ExtendedRaidb1RecoveryTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;
import org.objectweb.cjdbc.scenario.tools.testlet.ColumnUpdateTestLet;

/**
 * This class defines a QuotingInRecoveryLogScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class QuotingInRecoveryLogScenario extends
    ExtendedRaidb1RecoveryTemplate
{
  /**
   * Test inserts statement with recovery logs enabled
   * 
   * @throws Exception if fails
   */
  public void testInsertsWithJDBCRecoveryLog() throws Exception
  {
    Connection con = getCJDBCConnection("25322", "MainVdb");
    ColumnUpdateTestLet let = new ColumnUpdateTestLet(con);
    let.execute();
    Connection con2 = getCJDBCConnection("25322", "Recovery");
    ScenarioUtility.displayResultOnScreen(ScenarioUtility.getSingleQueryResult(
        "Select * from RECOVERY", con2));
  }
}