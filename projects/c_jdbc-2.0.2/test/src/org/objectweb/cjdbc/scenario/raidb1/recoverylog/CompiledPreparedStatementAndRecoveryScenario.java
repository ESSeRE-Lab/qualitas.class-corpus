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

import org.objectweb.cjdbc.scenario.templates.Raidb1RecoveryTemplate;
import org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.PreparedStatementRecoveryTestLet;

/**
 * This class defines a CompiledPreparedStatementAndRecoveryScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class CompiledPreparedStatementAndRecoveryScenario extends
    Raidb1RecoveryTemplate
{

  static final String NUMBER_OF_UPDATES = "50";

  /**
   * Test Random set and get on optimized prepared statement
   * 
   * @throws Exception if fails
   */
  public void testSetAndGetOnOptimized() throws Exception
  {
    PreparedStatementRecoveryTestLet let = new PreparedStatementRecoveryTestLet(
        mainVdb);
    let.set(AbstractTestLet.USE_OPTIMIZED_STATEMENT, Boolean.TRUE);
    let.set(AbstractTestLet.NUMBER_OF_UPDATES, NUMBER_OF_UPDATES);
    let.execute();
  }

  /**
   * Test Random set and get on standard prepared statement
   * 
   * @throws Exception if fails
   */
  public void testSetAndGetNonOptimized() throws Exception
  {
    PreparedStatementRecoveryTestLet let = new PreparedStatementRecoveryTestLet(
        mainVdb);
    let.set(AbstractTestLet.USE_OPTIMIZED_STATEMENT, Boolean.FALSE);
    let.set(AbstractTestLet.NUMBER_OF_UPDATES, NUMBER_OF_UPDATES);
    let.execute();
  }

}