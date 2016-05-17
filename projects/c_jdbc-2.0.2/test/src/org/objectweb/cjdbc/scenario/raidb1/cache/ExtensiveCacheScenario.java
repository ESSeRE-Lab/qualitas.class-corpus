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

package org.objectweb.cjdbc.scenario.raidb1.cache;

import java.sql.Connection;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.controller.cache.CacheStatistics;
import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;
import org.objectweb.cjdbc.scenario.tools.util.RequestSender;

/**
 * This class defines a ExtensiveCacheScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ExtensiveCacheScenario extends SimpleRaidb1Template
{
  static final int     LOOPS           = 500;
  static final int     INTERVAL        = 1;
  static final boolean DISPLAY_RESULTS = false;
  static final String  HSQLDB          = "hsqldb";

  /**
   * Test result cache with just reads
   * 
   * @throws Exception if error occurs
   */
  public void testReadResultCache() throws Exception
  {
    testBatchCache(-1);
  }

  /**
   * Test different cache with mixed read and write
   * 
   * @throws Exception if fails
   */
  public void testReadWriteResultCache() throws Exception
  {
    testBatchCache(300);
  }

  private void testBatchCache(int writeRatio) throws Exception
  {
    CacheResult cr1 = testCache("hsqldb-raidb1-cache-database.xml", writeRatio);
    CacheResult cr2 = testCache("hsqldb-raidb1-cache-table.xml", writeRatio);
    CacheResult cr3 = testCache("hsqldb-raidb1-cache-column.xml", writeRatio);
    CacheResult cr4 = testCache("hsqldb-raidb1-cache-columnUnique.xml",
        writeRatio);
    CacheResult cr5 = testCache("hsqldb-raidb1-cache-nocache.xml", writeRatio);
    CacheResult cr6 = testCache(HSQLDB, writeRatio);

    cr1.displayResult();
    cr2.displayResult();
    cr3.displayResult();
    cr4.displayResult();
    cr5.displayResult();
    cr6.displayResult();
  }

  private CacheResult testCache(String databaseFile, int writeRatio)
      throws Exception
  {
    Connection con = null;
    if (databaseFile.equals(HSQLDB))
    {
      con = getHypersonicConnection(9001);
    }
    else
    {
      // Load database
      cm.loadVirtualDatabases(controller, "myDB", databaseFile);
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      con = getCJDBCConnection();
    }

    // Start request sender thread
    RequestSender rs = new RequestSender(con);
    rs.setLoopInThread(LOOPS);
    rs.setRequestInterval(INTERVAL);
    rs.setDoWriteEvery(writeRatio);
    Thread t = new Thread(rs);

    // Get timed test
    long begin = System.currentTimeMillis();
    t.start();
    rs.setQuit(true);
    t.join();
    long end = System.currentTimeMillis();

    // Test is finished collect data
    CacheResult cr = new CacheResult();
    cr.cacheFile = databaseFile;
    cr.requests = LOOPS;
    cr.time = end - begin;
    cr.errors = rs.getExceptions();
    if (databaseFile.equals(HSQLDB))
      cr.nocache = true;
    else
    {
      AbstractResultCache arc = mainVdb.getRequestManager().getResultCache();
      if (arc != null)
      {
        CacheStatistics cs = arc.getCacheStatistics();
        cr.hits = cs.getHits();
        cr.ratio = cs.getCacheHitRatio();
      }
      else
        cr.nocache = true;
    }

    // Display results
    if (DISPLAY_RESULTS)
      cr.displayResult();

    mainVdb.shutdown(Constants.SHUTDOWN_FORCE);

    // Return data
    return cr;
  }

  class CacheResult
  {
    String    cacheFile;
    long      time     = 0;
    int       hits     = 0;
    long      ratio    = 0;
    int       requests = 0;
    boolean   nocache  = false;
    ArrayList errors;

    void displayResult()
    {
      System.out
          .println("-------------------------------------------------------");
      System.out.println("Cache File:" + cacheFile);
      System.out.println("Test lasted:" + time);
      if (!nocache)
      {
        System.out.println("Total hits:" + hits);
        System.out.println("Hit ratio:" + ratio);
      }
      else
        System.out.println("No Cache Enabled");
      for (int i = 0; i < errors.size(); i++)
        System.out.println("ERROR[" + i + "]:"
            + ((Exception) errors.get(i)).getMessage());
      System.out
          .println("-------------------------------------------------------");
    }
  }
}