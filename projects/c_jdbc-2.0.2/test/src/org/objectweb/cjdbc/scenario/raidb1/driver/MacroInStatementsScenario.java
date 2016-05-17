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

package org.objectweb.cjdbc.scenario.raidb1.driver;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.MacroStatementTestLet;

/**
 * Testing of macros replacement with real backends
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class MacroInStatementsScenario extends Raidb1Template
{

  static final String[] BATCH = new String[]{"rand()", "now()", "current_date",
      "current_time", "timeOfDay()", "current_timestamp"};

  /**
   * Test macro replacement with statement and controller processing
   * 
   * @throws Exception if fails
   */
  public void testMacrosInStatementNotDriverProcessed() throws Exception
  {
    MacroStatementTestLet let = new MacroStatementTestLet(mainVdb);
    let.set(AbstractTestLet.USE_OPTIMIZED_STATEMENT, "true");
    let.set(AbstractTestLet.USE_PREPARED_STATEMENT, "false");
    let.executeBatch(AbstractTestLet.MACRO_NAME, BATCH);
  }

  /**
   * Test macro replacement with statement and driver processing
   * 
   * @throws Exception if fails
   */
  public void testMacrosInStatementDriverProcessed() throws Exception
  {
    MacroStatementTestLet let = new MacroStatementTestLet(mainVdb);
    let.set(AbstractTestLet.USE_OPTIMIZED_STATEMENT, "false");
    let.set(AbstractTestLet.USE_PREPARED_STATEMENT, "false");
    let.executeBatch(AbstractTestLet.MACRO_NAME, BATCH);
  }

  /**
   * Test macro replacement with prepared statement and controller processing
   * 
   * @throws Exception if fails
   */

  public void testMacrosInPreparedStatementNotDriverProcessed()
      throws Exception
  {
    MacroStatementTestLet let = new MacroStatementTestLet(mainVdb);
    let.set(AbstractTestLet.USE_OPTIMIZED_STATEMENT, "true");
    let.set(AbstractTestLet.USE_PREPARED_STATEMENT, "true");
    let.executeBatch(AbstractTestLet.MACRO_NAME, BATCH);
  }

  /**
   * Test macro replacement with prepared statement and driver processing
   * 
   * @throws Exception if fails
   */
  public void testMacrosInPreparedStatementDriverProcessed() throws Exception
  {
    MacroStatementTestLet let = new MacroStatementTestLet(mainVdb);
    let.set(AbstractTestLet.USE_OPTIMIZED_STATEMENT, "false");
    let.set(AbstractTestLet.USE_PREPARED_STATEMENT, "true");
    let.executeBatch(AbstractTestLet.MACRO_NAME, BATCH);
  }
}