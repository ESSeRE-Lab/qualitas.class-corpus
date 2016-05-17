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
 * Initial developer(s): Willem Cazander.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.scenario.users;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 * Test case provided by Willem Cazander for a duplicate key in logtable.
 * 
 * <pre>
 * First create an table on both backend nodes:
 * 
 *  create table test (
 *  test integer not null
 *  );
 * 
 *  Set the initial checkpoints:
 * 
 *  m4ncluster(admin) > disable node1
 *  m4ncluster(admin) > setCheckpoint node1 emty1
 *  m4ncluster(admin) > enable node1 emty1
 * 
 *  m4ncluster(admin) > disable node2
 *  m4ncluster(admin) > setCheckpoint node2 emty2
 *  m4ncluster(admin) > enable node2 emty2 
 * 
 *  Start the load test:
 * 
 *  java -cp .:c-jdbc-driver.jar WillemScenario
 * 
 *  Disable an node:
 * 
 *  m4ncluster(admin) > disable node1
 *  Disabling backend node1 with no checkpoint
 * 
 *  Then I get the following error:
 * 
 *  2005-02-25 13:54:17,969 INFO  controller.RequestManager.m4ncluster Setting new virtual database schema.
 *  2005-02-25 14:00:10,168 INFO  controller.loadbalancer.RAIDb1 Removing blocking task worker thread for backend node1
 *  2005-02-25 14:00:10,168 INFO  controller.loadbalancer.RAIDb1 Removing non blocking task worker thread for backend node1
 *  2005-02-25 14:00:10,173 ERROR controller.backend.BackendStateListener Could not store informatione for backend:node1
 *  java.sql.SQLException: Unable to update checkpoint 'emty1' for backend:node1
 *  at org.objectweb.cjdbc.controller.recoverylog.RecoveryLog.storeBackendInfo(RecoveryLog.java:1226)
 * 
 * </pre>
 * 
 * @author <a href="mailto:willem.cazander@mbuyu.nl">Willem Cazander </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class WillemScenario
{
  String     driver     = "org.objectweb.cjdbc.driver.Driver";
  String     url        = "jdbc:cjdbc://localhost/m4ncluster";
  String     username   = "m4ncluster";
  String     password   = "clusterm4n";
  int        records    = 50000;
  long       waitTime   = 100000;
  Connection connection = null;
  Random     random     = null;

  /**
   * Creates a new <code>WillemScenario</code> object
   */
  public WillemScenario()
  {
    try
    {
      Class.forName(driver).newInstance();
      connection = DriverManager.getConnection(url, username, password);

      random = new Random();
      fillDB();
    }
    catch (SQLException e)
    {
      System.err.println("SQLException: " + e.getMessage());
    }
    catch (ClassCastException e)
    {
      System.err.println("ClassCastException: " + e.getMessage());
    }
    catch (ClassNotFoundException e)
    {
      System.err.println("ClassNotFoundException: " + e.getMessage());
    }
    catch (InstantiationException e)
    {
      System.err.println("InstantiationException: " + e.getMessage());
    }
    catch (IllegalAccessException e)
    {
      System.err.println("IllegalAccessException: " + e.getMessage());
    }
  }

  /**
   * Fill the database
   */
  public void fillDB()
  {
    try
    {
      Statement sm = connection.createStatement();
      for (int i = 0; i < records; i++)
      {
        String sql = "insert into test (test) values ("
            + random.nextInt(873264) + ")";
        System.out.println("sql: " + sql);
        sm.executeUpdate(sql);
        // wait a bit
        for (int r = 0; r < 3000; ++r)
        {
        }
      }
    }
    catch (SQLException e)
    {
      System.err.println("Could not fill test: " + e.getMessage());
    }
  }

  /**
   * Main method
   * 
   * @param args arguements (ignored)
   */
  static public void main(String[] args)
  {
    new WillemScenario();
  }
}