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
import java.util.ArrayList;

import org.objectweb.cjdbc.scenario.templates.Raidb1RecoveryTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;
import org.objectweb.cjdbc.scenario.tools.util.RequestSender;

/**
 * This class defines a TransactionReplayScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class TransactionReplayScenario extends Raidb1RecoveryTemplate
{

  static final int LOOPS = 500;
  
  /**
   * Replay with no transactions and no concurrent requests
   * 
   * @throws Exception if fails
   */
  public void testReplayMissingRequestsNoTransactionNoConcurrentRequest()
      throws Exception
  {
    executeTestReplay(false, false, LOOPS, 1);
  }

  /**
   * Replay with no transactions and no concurrent requests
   * 
   * @throws Exception if fails
   */
  public void testReplayMissingRequestsTransactionAndConcurrentRequests()
      throws Exception
  {
    executeTestReplay(true, true, LOOPS, 1);
  }

  /**
   * Execute the recovery test, with given parameters
   * 
   * @param concurrent should write/read queries be executed during recovery
   * @param transaction should we use transaction
   * @param loops how many times do we loop
   * @throws Exception if fails
   */
  private void executeTestReplay(boolean concurrent, boolean transaction,
      int loops, int clients) throws Exception
  {
    // Execute select query
    Connection con = getCJDBCConnection();
    String sql = "select * from document";
    ArrayList list = ScenarioUtility.getSingleQueryResult(sql, con);
    ArrayList emptyList = new ArrayList();
    assertNotSame("Empty list after requests", list, emptyList);

    // Disable backend
    String backend = "localhost2";
    String checkpoint = "check" + System.currentTimeMillis();
    mainVdb.disableBackendWithCheckpoint(backend);

    // Write on tables
    ArrayList threads = new ArrayList();
    for (int i = 0; i < clients; i++)
    {
      RequestSender rs = new RequestSender(con);
      rs.setUseTransactions(transaction);
      rs.setDoWriteEvery(1); // write intensive test
      rs.setLoopInThread(loops);
      Thread t = new Thread(rs);
      threads.add(t);
      t.start();
      rs.setQuit(true);
    }

    if (!concurrent)
      joinThreads(threads);

    // Check content has been updated
    ArrayList listaafter = ScenarioUtility.getSingleQueryResult(sql, con);
    assertNotSame("No Updates after requests", list, listaafter);

    // Get content of recovery
    Connection h3 = getHypersonicConnection(9003);
    ArrayList recovery = ScenarioUtility.getSingleQueryResult(
        "select * from recovery", h3);
    ScenarioUtility.displayResultOnScreen(recovery);

    // Enable backend, should start the recovery log
    mainVdb.enableBackendFromCheckpoint(backend, checkpoint);

    if (!concurrent)
      joinThreads(threads);

    // Test directely on hypersonic connection
    Connection h1 = getHypersonicConnection(9001);
    ArrayList hl1 = ScenarioUtility.getSingleQueryResult(sql, h1);
    Connection h2 = getHypersonicConnection(9002);
    ArrayList hl2 = ScenarioUtility.getSingleQueryResult(sql, h2);

    // Check all updates have been replayed
    assertEquals("Backend is missing some updates", hl1, hl2);
  }

  /**
   * Joins all the threads in the arraylist
   * 
   * @param threads a list of the threads
   * @throws Exception if fails
   */
  private void joinThreads(ArrayList threads) throws Exception
  {
    for (int i = 0; i < threads.size(); i++)
      ((Thread) threads.get(i)).join();
  }


}
