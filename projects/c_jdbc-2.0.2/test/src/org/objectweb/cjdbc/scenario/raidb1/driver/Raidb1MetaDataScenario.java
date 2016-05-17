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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;

/**
 * This class defines a Raidb1MetaDataScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Raidb1MetaDataScenario extends Raidb1Template
{

  /**
   * Test the meta data return by the driver for the tables
   * 
   * @throws Exception if fails
   */
  public void testMetaDataTables() throws Exception
  {
    System.out.println("Tables:");
    Connection conn = getCJDBCConnection();
    DatabaseMetaData dbmd = conn.getMetaData();
    ResultSet rs = dbmd.getTables(null, null, null, null);
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] columnNames = new String[columnCount];
    for (int i = 1; i <= columnCount; i++)
      columnNames[i - 1] = rsmd.getColumnName(i);

    assertEquals("TABLE_CAT invalid", "TABLE_CAT", columnNames[0]);
    assertEquals("TABLE_SCHEM invalid", "TABLE_SCHEM", columnNames[1]);
    assertEquals("TABLE_NAME invalid", "TABLE_NAME", columnNames[2]);
    assertEquals("TABLE_TYPE invalid", "TABLE_TYPE", columnNames[3]);
    assertEquals("REMARKS invalid", "REMARKS", columnNames[4]);

    //    # TABLE_CAT String => table catalog (may be null)
    //    # TABLE_SCHEM String => table schema (may be null)
    //    # TABLE_NAME String => table name
    //    # TABLE_TYPE String => table type. Typical types are "TABLE", "VIEW",
    // "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS",
    // "SYNONYM".
    //    # REMARKS String => explanatory comment on the table

    while (rs.next())
    {
      String catalog = rs.getString(1);
      String schema = rs.getString(2);
      String name = rs.getString(3);
      String type = rs.getString(4);
      String remarks = rs.getString(5);
      System.out.println("  " + catalog + "." + schema + "." + name + " : "
          + type + " (" + remarks + ")");
    }

    conn.close();
  }

  /**
   * Retrieve metadata from the driver, simple asserts, mainly for display
   * 
   * @throws Exception when fails
   */
  public void testMetaDataSampleOutput() throws Exception
  {
    Connection conn = getCJDBCConnection();
    DatabaseMetaData dbmd = conn.getMetaData();
    assertEquals("Catalog term different than expected", dbmd.getCatalogTerm(),
        "catalog");
    String catalog1 = conn.getCatalog();
    System.out.println("Current Catalog:" + catalog1);
    assertEquals("Current catalog different than expected", catalog1, "myDB");

    System.out.println("Catalog: (" + dbmd.getSchemaTerm() + "):");
    ResultSet rs = dbmd.getCatalogs();
    while (rs.next())
    {
      String s = rs.getString("TABLE_CAT");
      System.out.println("  " + s);
    }

    System.out.println("Schemas (" + dbmd.getSchemaTerm() + "):");
    rs = dbmd.getSchemas();
    if (rs != null)
    {
      while (rs.next())
      {
        String s = rs.getString(1);
        System.out.println("  " + s);
      }
    }

    System.out.println("Tables types:");
    rs = dbmd.getTableTypes();
    if (rs != null)
    {
      while (rs.next())
      {
        String s = rs.getString(1);
        System.out.println("  " + s);
      }
    }

    System.out.println("Table Privileges:");
    rs = dbmd.getTablePrivileges(null, null, null);

    if (rs != null)
      while (rs.next())
      {
        String catalog = rs.getString(1);
        String schema = rs.getString(2);
        String name = rs.getString(3);
        String grantor = rs.getString(4);
        String grantee = rs.getString(5);
        String privilege = rs.getString(6);
        String isGrantable = rs.getString(7);
        System.out.println("  " + catalog + "." + schema + "." + name);
        System.out.println("    Grantor: " + grantor);
        System.out.println("    Grantee: " + grantee);
        System.out.println("    Privilege: " + privilege);
        System.out.println("    isGrantable: " + isGrantable);
      }
    
    conn.close();
  }

  /**
   * Test the meta data return by the driver for the procedures
   * 
   * @throws Exception if fails
   */
  public void testMetaDataProcedures() throws Exception
  {
    System.out.println("Procedures:");
    Connection conn = getCJDBCConnection();
    DatabaseMetaData dbmd = conn.getMetaData();
    ResultSet rs = dbmd.getProcedures(null, null, null);
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] columnNames = new String[columnCount];
    for (int i = 1; i <= columnCount; i++)
    {
      columnNames[i - 1] = rsmd.getColumnName(i);
      System.out.println(columnNames[i - 1]);
    }
    //    1. PROCEDURE_CAT String => procedure catalog (may be null)
    //    2. PROCEDURE_SCHEM String => procedure schema (may be null)
    //    3. PROCEDURE_NAME String => procedure name
    //    4. reserved for future use
    //    5. reserved for future use
    //    6. reserved for future use
    //    7. REMARKS String => explanatory comment on the procedure
    //    8. PROCEDURE_TYPE short => kind of procedure:
    //           * procedureResultUnknown - May return a result
    //           * procedureNoResult - Does not return a result
    //           * procedureReturnsResult - Returns a result

    assertEquals("PROCEDURE_CAT invalid", "PROCEDURE_CAT", columnNames[0]);
    assertEquals("PROCEDURE_SCHEM invalid", "PROCEDURE_SCHEM", columnNames[1]);
    assertEquals("PROCEDURE_NAME invalid", "PROCEDURE_NAME", columnNames[2]);
    assertEquals("REMARKS invalid", "REMARKS", columnNames[6]);
    assertEquals("PROCEDURE_TYPE invalid", "PROCEDURE_TYPE", columnNames[7]);
    
    conn.close();
  }

  /**
   * Test the meta data return by the driver for the table privileges
   * 
   * @throws Exception if fails
   */
  public void testMetaDataTablePrivileges() throws Exception
  {
    System.out.println("Table Privileges:");
    Connection conn = getCJDBCConnection();
    DatabaseMetaData dbmd = conn.getMetaData();
    ResultSet rs = dbmd.getTablePrivileges(null, null, null);
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] columnNames = new String[columnCount];
    for (int i = 1; i <= columnCount; i++)
      columnNames[i - 1] = rsmd.getColumnName(i);

    //    # TABLE_CAT String => table catalog (may be null)
    //    # TABLE_SCHEM String => table schema (may be null)
    //    # TABLE_NAME String => table name
    //    # GRANTOR => grantor of access (may be null)
    //    # GRANTEE String => grantee of access
    //    # PRIVILEGE String => name of access (SELECT, INSERT, UPDATE, REFRENCES,
    // ...)
    //    # IS_GRANTABLE String => "YES" if grantee is permitted to grant to
    // others; "NO" if not; null if unknown

    assertEquals("TABLE_CAT invalid", "TABLE_CAT", columnNames[0]);
    assertEquals("TABLE_SCHEM invalid", "TABLE_SCHEM", columnNames[1]);
    assertEquals("TABLE_NAME invalid", "TABLE_NAME", columnNames[2]);
    assertEquals("GRANTOR invalid", "GRANTOR", columnNames[3]);
    assertEquals("GRANTEE invalid", "GRANTEE", columnNames[4]);
    assertEquals("PRIVILEGE invalid", "PRIVILEGE", columnNames[5]);
    assertEquals("IS_GRANTABLE invalid", "IS_GRANTABLE", columnNames[6]);
    
    conn.close();
  }

  /**
   * Test the meta data return by the driver for the columns
   * 
   * @throws Exception if fails
   */
  public void testMetaDataColumns() throws Exception
  {
    System.out.println("Columns:");
    Connection conn = getCJDBCConnection();
    DatabaseMetaData dbmd = conn.getMetaData();
    ResultSet rs = dbmd.getColumns(null, null, null, null);
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] columnNames = new String[columnCount];
    for (int i = 1; i <= columnCount; i++)
      columnNames[i - 1] = rsmd.getColumnName(i);

    assertEquals("TABLE_CAT invalid", "TABLE_CAT", columnNames[0]);
    assertEquals("TABLE_SCHEM invalid", "TABLE_SCHEM", columnNames[1]);
    assertEquals("TABLE_NAME invalid", "TABLE_NAME", columnNames[2]);
    assertEquals("COLUMN_NAME invalid", "COLUMN_NAME", columnNames[3]);
    assertEquals("DATA_TYPE invalid", "DATA_TYPE", columnNames[4]);

    assertEquals("TYPE_NAME invalid", "TYPE_NAME", columnNames[5]);
    assertEquals("COLUMN_SIZE invalid", "COLUMN_SIZE", columnNames[6]);
    assertEquals("BUFFER_LENGTH invalid", "BUFFER_LENGTH", columnNames[7]);
    assertEquals("DECIMAL_DIGITS invalid", "DECIMAL_DIGITS", columnNames[8]);
    assertEquals("NUM_PREC_RADIX invalid", "NUM_PREC_RADIX", columnNames[9]);

    assertEquals("NULLABLE invalid", "NULLABLE", columnNames[10]);
    assertEquals("REMARKS invalid", "REMARKS", columnNames[11]);
    assertEquals("COLUMN_DEF invalid", "COLUMN_DEF", columnNames[12]);
    assertEquals("SQL_DATA_TYPE invalid", "SQL_DATA_TYPE", columnNames[13]);
    assertEquals("SQL_DATETIME_SUB invalid", "SQL_DATETIME_SUB",
        columnNames[14]);

    assertEquals("CHAR_OCTET_LENGTH invalid", "CHAR_OCTET_LENGTH",
        columnNames[15]);
    assertEquals("ORDINAL_POSITION invalid", "ORDINAL_POSITION",
        columnNames[16]);
    assertEquals("IS_NULLABLE invalid", "IS_NULLABLE", columnNames[17]);

    //    # TABLE_CAT String => table catalog (may be null)
    //    # TABLE_SCHEM String => table schema (may be null)
    //    # TABLE_NAME String => table name
    //    # COLUMN_NAME String => column name
    //    # DATA_TYPE short => SQL type from java.sql.Types
    //    # TYPE_NAME String => Data source dependent type name, for a UDT the
    // type name is fully qualified
    //    # COLUMN_SIZE int => column size. For char or date types this is the
    // maximum number of characters, for numeric or decimal types this is
    // precision.
    //    # BUFFER_LENGTH is not used.
    //    # DECIMAL_DIGITS int => the number of fractional digits
    //    # NUM_PREC_RADIX int => Radix (typically either 10 or 2)
    //    # NULLABLE int => is NULL allowed?
    //
    //        * columnNoNulls - might not allow NULL values
    //        * columnNullable - definitely allows NULL values
    //        * columnNullableUnknown - nullability unknown
    //
    //    # REMARKS String => comment describing column (may be null)
    //    # COLUMN_DEF String => default value (may be null)
    //    # SQL_DATA_TYPE int => unused
    //    # SQL_DATETIME_SUB int => unused
    //    # CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in
    // the column
    //    # ORDINAL_POSITION int => index of column in table (starting at 1)
    //    # IS_NULLABLE String => "NO" means column definitely does not allow NULL
    // values; "YES" means the column might allow NULL values. An empty string
    // means nobody knows.

    conn.close();
  }

  /**
   * Test the meta data return by the driver for the schema
   * 
   * @throws Exception if fails
   */
  public void testMetaDataSchema() throws Exception
  {
    System.out.println("Schema:");
    Connection conn = getCJDBCConnection();
    DatabaseMetaData dbmd = conn.getMetaData();
    ResultSet rs = dbmd.getSchemas();
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] columnNames = new String[columnCount];
    for (int i = 1; i <= columnCount; i++)
      columnNames[i - 1] = rsmd.getColumnName(i);

    assertEquals("TABLE_SCHEM invalid", "TABLE_SCHEM", columnNames[0]);
    
    conn.close();
  }

  /**
   * Test the meta data return by the driver for the catalogs
   * 
   * @throws Exception if fails
   */
  public void testMetaDataCatalogs() throws Exception
  {
    System.out.println("Catalogs:");
    Connection conn = getCJDBCConnection();
    DatabaseMetaData dbmd = conn.getMetaData();
    ResultSet rs = dbmd.getCatalogs();
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] columnNames = new String[columnCount];
    for (int i = 1; i <= columnCount; i++)
      columnNames[i - 1] = rsmd.getColumnName(i);

    assertEquals("TABLE_CAT invalid", "TABLE_CAT", columnNames[0]);
    
    conn.close();
  }

  /**
   * Test the meta data return by the driver for the table types
   * 
   * @throws Exception if fails
   */
  public void testMetaDataTableTypes() throws Exception
  {
    System.out.println("Table Types:");
    Connection conn = getCJDBCConnection();
    DatabaseMetaData dbmd = conn.getMetaData();
    ResultSet rs = dbmd.getTableTypes();
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] columnNames = new String[columnCount];
    for (int i = 1; i <= columnCount; i++)
      columnNames[i - 1] = rsmd.getColumnName(i);

    assertEquals("TABLE_TYPE invalid", "TABLE_TYPE", columnNames[0]);
    
    conn.close();
  }

  /**
   * Test the meta data return by the driver for the procedure columns
   * 
   * @throws Exception if fails
   */
  public void testMetaDataProcedureColumns() throws Exception
  {
    System.out.println("Procedures Columns:");
    Connection conn = getCJDBCConnection();
    DatabaseMetaData dbmd = conn.getMetaData();
    ResultSet rs = dbmd.getProcedureColumns(null, null, null, null);
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] columnNames = new String[columnCount];
    for (int i = 1; i <= columnCount; i++)
      columnNames[i - 1] = rsmd.getColumnName(i);

    //    # PROCEDURE_CAT String => procedure catalog (may be null)
    //    # PROCEDURE_SCHEM String => procedure schema (may be null)
    //    # PROCEDURE_NAME String => procedure name
    //    # COLUMN_NAME String => column/parameter name
    //    # COLUMN_TYPE Short => kind of column/parameter:
    //
    //        * procedureColumnUnknown - nobody knows
    //        * procedureColumnIn - IN parameter
    //        * procedureColumnInOut - INOUT parameter
    //        * procedureColumnOut - OUT parameter
    //        * procedureColumnReturn - procedure return value
    //        * procedureColumnResult - result column in ResultSet
    //
    //    # DATA_TYPE short => SQL type from java.sql.Types
    //    # TYPE_NAME String => SQL type name, for a UDT type the type name is
    // fully qualified
    //    # PRECISION int => precision
    //    # LENGTH int => length in bytes of data
    //    # SCALE short => scale
    //    # RADIX short => radix
    //    # NULLABLE short => can it contain NULL?
    //
    //        * procedureNoNulls - does not allow NULL values
    //        * procedureNullable - allows NULL values
    //        * procedureNullableUnknown - nullability unknown
    //
    //    # REMARKS String => comment describing parameter/column

    assertEquals("PROCEDURE_CAT invalid", "PROCEDURE_CAT", columnNames[0]);
    assertEquals("PROCEDURE_SCHEM invalid", "PROCEDURE_SCHEM", columnNames[1]);
    assertEquals("PROCEDURE_NAME invalid", "PROCEDURE_NAME", columnNames[2]);
    assertEquals("COLUMN_NAME invalid", "COLUMN_NAME", columnNames[3]);
    assertEquals("COLUMN_TYPE invalid", "COLUMN_TYPE", columnNames[4]);
    assertEquals("DATA_TYPE invalid", "DATA_TYPE", columnNames[5]);
    assertEquals("TYPE_NAME invalid", "TYPE_NAME", columnNames[6]);
    assertEquals("PRECISION invalid", "PRECISION", columnNames[7]);
    assertEquals("LENGTH invalid", "LENGTH", columnNames[8]);
    assertEquals("SCALE invalid", "SCALE", columnNames[9]);
    assertEquals("RADIX invalid", "RADIX", columnNames[10]);
    assertEquals("NULLABLE invalid", "NULLABLE", columnNames[11]);
    assertEquals("REMARKS invalid", "REMARKS", columnNames[12]);
    
    conn.close();
  }

  /**
   * Test the meta data return by the driver for the primary keys
   * 
   * @throws Exception if fails
   */
  public void testMetaDataPrimaryKeys() throws Exception
  {
    System.out.println("Primary Keys:");
    Connection conn = getCJDBCConnection();
    DatabaseMetaData dbmd = conn.getMetaData();
    ResultSet rs = dbmd.getPrimaryKeys(null, null, "ADDRESS");
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] columnNames = new String[columnCount];
    for (int i = 1; i <= columnCount; i++)
      columnNames[i - 1] = rsmd.getColumnName(i);

    assertTrue("Could not get result set for primary keys", rs.next());
    String pkName = rs.getString("PK_NAME");
    assertEquals("PK Name different than expected", "ID", pkName);
    
    conn.close();
  }
}