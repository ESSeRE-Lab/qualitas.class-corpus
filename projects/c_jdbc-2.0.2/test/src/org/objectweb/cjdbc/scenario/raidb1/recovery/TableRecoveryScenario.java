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

package org.objectweb.cjdbc.scenario.raidb1.recovery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.objectweb.cjdbc.scenario.templates.Raidb1RecoveryTemplate;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a TableRecoveryScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class TableRecoveryScenario extends Raidb1RecoveryTemplate
{
  private static final String BACKUP_LOGIN    = "user";
  private static final String BACKUP_PASSWORD = "";
  private static final String BACKUPER        = "Octopus";
  private static final String BACKUP_PATH     = "../backup";

  /**
   * I don't know if octopus is restoring all the tables
   * 
   * @throws Exception if fails
   */
  public void testListTables() throws Exception
  {
    // Create backup
    String dump = "dump" + System.currentTimeMillis();
    mainVdb.backupBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD, dump,
        BACKUPER, BACKUP_PATH, null);

    // Recover backends
    String backendName = "localhost2";
    mainVdb.forceDisableBackend(backendName);
    mainVdb.restoreDumpOnBackend("localhost", BACKUP_LOGIN, BACKUP_PASSWORD,
        dump, null);
    mainVdb.enableBackendFromCheckpoint("localhost");

    String[] types = new String[]{"TABLE", "VIEW"};

    Connection con1 = getHypersonicConnection(9001);
    ResultSet rs1 = con1.getMetaData().getTables(null, null, "%", types);

    Connection con2 = getHypersonicConnection(9002);
    ResultSet rs2 = con2.getMetaData().getTables(null, null, "%", types);

    assertTrue("Metadata is different for tables", ScenarioUtility.checkEquals(
        rs1, rs2));

    // Type forward only so request the metadata again from different connection
    ResultSet rs = getHypersonicConnection(9001).getMetaData().getTables(null,
        null, "%", types);
    while (rs.next())
    {
      // 1 is table catalog, 2 is table schema, 3 is table name, 4 is type
      String tableName = rs.getString(3);
      String sql = "Select * from " + tableName;
      System.out.println("Checking content of table:" + tableName);

      PreparedStatement ps1 = con1.prepareStatement(sql);
      PreparedStatement ps2 = con2.prepareStatement(sql);

      // ps1.setString(1, tableName);
      // ps2.setString(1, tableName);

      rs1 = ps1.executeQuery();
      rs2 = ps2.executeQuery();

      assertTrue("Data is different for table:" + tableName, ScenarioUtility
          .checkEquals(rs1, rs2));

      ResultSetMetaData rsmd1 = rs1.getMetaData();
      ResultSetMetaData rsmd2 = rs2.getMetaData();
      assertEquals("meta data column count are different for table:"
          + tableName, rsmd1.getColumnCount(), rsmd2.getColumnCount());
      int colCount = rsmd1.getColumnCount();
      for (int i = 1; i <= colCount; i++)
      {
        String colName = rsmd1.getColumnName(i);
        System.out.println("Checking metadata for column:" + colName);
        assertEquals(rsmd1.getCatalogName(i), rsmd2.getCatalogName(i));
        // assertEquals(rsmd1.getColumnClassName(i),
        // rsmd2.getColumnClassName(i)); NOT SUPPORTED BY HSQLDB
        assertEquals(rsmd1.getColumnDisplaySize(i), rsmd2
            .getColumnDisplaySize(i));
        assertEquals(rsmd1.getColumnLabel(i), rsmd2.getColumnLabel(i));
        assertEquals(rsmd1.getColumnName(i), rsmd2.getColumnName(i));
        assertEquals(rsmd1.getColumnType(i), rsmd2.getColumnType(i));
        assertEquals(rsmd1.getColumnTypeName(i), rsmd2.getColumnTypeName(i));
      }
    }
  }
}