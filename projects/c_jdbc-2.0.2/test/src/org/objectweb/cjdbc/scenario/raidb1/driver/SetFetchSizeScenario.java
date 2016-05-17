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

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread;
import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.util.PrivilegedAccessor;

/**
 * This class defines a SetFetchSizeScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class SetFetchSizeScenario extends Raidb1Template
{

  /**
   * It seems the setFetchsize is ignored and the controller loads all the
   * resultset in memory, thus resulting in an <code>OutOfMemory</code>
   * exception
   * 
   * @throws Exception if fails
   */
  public void testFetchSize() throws Exception
  {
    final int fetchsize = 5;
    Connection con = getCJDBCConnection();
    con.setAutoCommit(false);
    String sql = "Select * from ADDRESS,DOCUMENT,PRODUCT";
    PreparedStatement ps = con.prepareStatement(sql);
    ps.setFetchSize(fetchsize);
    ResultSet rs = ps.executeQuery();
    ArrayList activeThreads = mainVdb.getActiveThreads();
    VirtualDatabaseWorkerThread vt = (VirtualDatabaseWorkerThread) activeThreads
        .get(0);
    HashMap streamedResultSet = (HashMap) PrivilegedAccessor.getValue(vt,
        "streamedResultSet");
    String key = (String) streamedResultSet.keySet().iterator().next();
    ControllerResultSet resultSet = (ControllerResultSet) streamedResultSet
        .get(key);

    int count = 0;
    ArrayList data = new ArrayList();
    while (rs.next())
    {
      // System.out.println("count[" + count + "]:" + resultSet.getData());
      if ((count++) % fetchsize == 0)
        assertNotSame(data, resultSet.getData());
      data = resultSet.getData();
      if (count > 10000)
        break;
    }
    assertEquals("Invalid fetch size from resultset", fetchsize, rs
        .getFetchSize());
  }
}