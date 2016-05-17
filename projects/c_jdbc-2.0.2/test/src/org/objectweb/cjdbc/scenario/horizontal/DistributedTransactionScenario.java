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
 * Initial developer(s): Frederic Laugier
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.horizontal;

import java.sql.Connection;
import java.sql.Statement;

import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.scenario.templates.HorizontalTemplate;

/**
 * This class defines a DistributedTransactionScenario
 * 
 * @author <a href="mailto:flaugier@micropole-univers.com">Frederic Laugier </a>
 * @version 1.0
 */
public class DistributedTransactionScenario extends HorizontalTemplate
{

  /**
   * test distributed transaction
   * 
   * @throws Exception if fails
   */
  public void testDistributedTransaction() throws Exception
  {
    Connection con = getCJDBCConnection(new ControllerInfo[]{
        new ControllerInfo("localhost", 25322),
        new ControllerInfo("localhost", 25323)});
    con.setReadOnly(false);
    con.setAutoCommit(false);
    Statement stmt = con.createStatement();
    stmt.executeUpdate("update product set name='horizontalTest'");
    con.commit();
    con.setAutoCommit(true);
    con.close();
  }
}