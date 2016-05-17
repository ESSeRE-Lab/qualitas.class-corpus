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

package org.objectweb.cjdbc.scenario.raidb1.loadbalancer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import org.objectweb.cjdbc.scenario.templates.SimpleRaidb1Template;

/**
 * This class defines a Raidb1ParallelDBScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modryzk </a>
 * @version 1.0
 */
public class Raidb1ParallelDBScenario extends SimpleRaidb1Template
{
  /**
   * Test parallelDB loadbalancer of type lprf
   */
  public void testFailFast()
  {
    try
    {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb1-parallel-lprf.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      execute();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("failed to test c-jdbc parallel loadbalancer of type least pending request first");
    }
  }

  /**
   * Test parallelDB loadbalancer of type roundrobin
   */
  public void testRoundRobin()
  {
    try
    {
      //Load database
      cm.loadVirtualDatabases(controller, "myDB",
          "hsqldb-raidb1-parallel-roundrobin.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
      execute();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("failed to test c-jdbc parallel loadbalancer of type round robin");
    }
  }

  private void execute() throws Exception
  {
    Class.forName("org.objectweb.cjdbc.driver.Driver");
    int threadCount = 3;
    ArrayList threads = new ArrayList(threadCount);
    for (int i = 0; i < threadCount; i++)
    {
      ParallelThread par = new ParallelThread();
      par.start();
      threads.add(par);
    }
    for (int i = 0; i < threadCount; i++)
    {
      ParallelThread par = (ParallelThread) threads.get(i);
      par.join();
    }
    for (int i = 0; i < threadCount; i++)
    {
      ParallelThread par = (ParallelThread) threads.get(i);
      assertNull("A thread had an exception" ,
          par.exception);
    }
  }

  class ParallelThread extends Thread
  {
    Exception exception = null;

    /**
     * Starts the Parallel Thread 
     */
    public void run()
    {
      try
      {
        //    Execute request
        Properties props = new Properties();
        props.put("user", "user");
        props.put("password", "");
        Connection con = DriverManager.getConnection(
            "jdbc:cjdbc://localhost/myDB", props);
        assertNotNull("Connection is null", con);
        Statement statement = con.createStatement();
        for (int i = 0; i < 20; i++)
        {
          statement.executeUpdate("update product set name='Parallel Test" + i
              + "'");
          statement.executeQuery("select * from PRODUCT");
        }
      }
      catch (Exception e)
      {
        if (exception == null)
        {
          exception = e;
        }
        e.printStackTrace();
      }
    }
  }
}
