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
 * Initial developer(s): Mathieu Peltier.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.tools.mock;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.scenario.tools.databases.AbstractDatabase;

import com.mockobjects.sql.MockConnection2;
import com.mockobjects.sql.MockMultiRowResultSet;

/**
 * Mock connection manager.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager
 */
public class MockConnectionManager extends AbstractConnectionManager
{
  /** Fake connection. */
  private MockConnection2 connection;

  /** <code>true</code> if the connections have been initialized. */
  private boolean         isInitialized;

  /**
   * Creates a new <code>MockConnectionManager</code> instance.
   * 
   * @param database <code>AbstractDatabase</code> instance.
   */
  public MockConnectionManager(AbstractDatabase database)
  {
    super("", "", "", "", null, null);
    isInitialized = false;
    connection = new MockConnection2();
    connection.setupMetaData(new MockDatabaseMetaData(database));
  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    return null;
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#finalizeConnections()
   */
  public void finalizeConnections() throws SQLException
  {
    isInitialized = false;
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getConnection()
   */
  public Connection getConnection()
  {
    return connection;
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#initializeConnections()
   */
  public void initializeConnections() throws SQLException
  {
    isInitialized = true;
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#isInitialized()
   */
  public boolean isInitialized()
  {
    return isInitialized;
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#releaseConnection(java.sql.Connection)
   */
  public void releaseConnection(Connection connection)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#deleteConnection(java.sql.Connection)
   */
  public void deleteConnection(Connection connection)
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getCurrentNumberOfConnections()
   */
  public int getCurrentNumberOfConnections()
  {
    return 0;
  }

  /**
   * @see org.objectweb.cjdbc.controller.connection.AbstractConnectionManager#getXmlImpl()
   */
  public String getXmlImpl()
  {
    return null;
  }

  /**
   * Mock database meta data.
   */
  public class MockDatabaseMetaData
      extends
        com.mockobjects.sql.MockDatabaseMetaData
  {
    /** Database columns <code>ResultSet</code>. */
    private MockMultiRowResultSet tables;

    /**
     * Hashtable of database columns <code>ResultSet</code> hashed by the
     * table name.
     */
    private Hashtable             columnsHashtable;

    /**
     * Hashtable of primary keys <code>ResultSet</code> hashed by the table
     * name.
     */
    private Hashtable             primaryKeysHashtable;

    /**
     * Creates a new <code>MockDatabaseMetaData</code> instance.
     * 
     * @param database <code>AbstractDatabase</code> instance.
     */
    public MockDatabaseMetaData(AbstractDatabase database)
    {
      ArrayList array1, array2, array3;
      MockMultiRowResultSet resultSet;
      DatabaseTable table;
      DatabaseColumn column;
      Object[][] expected;
      Iterator it1, it2;

      // Set expected tables, columns and primaryKeys ResultSet
      tables = new MockMultiRowResultSet();
      columnsHashtable = new Hashtable();
      primaryKeysHashtable = new Hashtable();
      array1 = new ArrayList();
      it1 = database.getSchema().getTables().iterator();
      while (it1.hasNext())
      {
        table = (DatabaseTable) it1.next();
        array1.add(new Object[]{null, null, table.getName(), null});

        // Set expected columns and primaryKeys ResultSet for the table
        // table.getName()
        array2 = new ArrayList();
        array3 = new ArrayList();
        it2 = table.getColumns().iterator();
        while (it2.hasNext())
        {
          column = (DatabaseColumn) it2.next();
          array2.add(new Object[]{null, null, table.getName(),
              column.getName(),
              new Short(new Integer(column.getType()).shortValue())});

          if (column.isUnique())
            array3.add(new Object[]{null, null, table.getName(),
                column.getName()});

        }

        // Transform array2 to Object[][]
        expected = new Object[array2.size()][5];
        for (int i = 0; i < array2.size(); i++)
        {
          expected[i] = (Object[]) array2.get(i);
        }
        // Set columns ResultSet
        resultSet = new MockMultiRowResultSet();
        resultSet.setupRows(expected);
        columnsHashtable.put(table.getName(), resultSet);

        // Transform array3 to Object[][]
        expected = new Object[array3.size()][5];
        for (int i = 0; i < array3.size(); i++)
        {
          expected[i] = (Object[]) array3.get(i);
        }
        // Set primaryKeys ResultSet
        resultSet = new MockMultiRowResultSet();
        resultSet.setupRows(expected);
        primaryKeysHashtable.put(table.getName(), resultSet);

      }

      // Transform array1 to Object[][]
      expected = new Object[array1.size()][4];
      for (int i = 0; i < array1.size(); i++)
      {
        expected[i] = (Object[]) array1.get(i);
      }
      // Set tables ResultSet
      tables.setupRows(expected);
    }

    /**
     * Miminal implementation for <code>DatabaseBackendMetaDataTest</code>.
     * 
     * @see java.sql.DatabaseMetaData#getColumns(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public ResultSet getColumns(String catalog, String schemaPattern,
        String tableNamePattern, String columnNamePattern) throws SQLException
    {
      if (columnNamePattern.equals("%"))
        return (ResultSet) columnsHashtable.get(tableNamePattern);
      else
        return null;
    }

    /**
     * Miminal implementation for <code>DatabaseBackendMetaDataTest</code>.
     * 
     * @see java.sql.DatabaseMetaData#getPrimaryKeys(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public ResultSet getPrimaryKeys(String catalog, String schema, String table)
        throws SQLException
    {
      return (ResultSet) primaryKeysHashtable.get(table);
    }

    /**
     * Miminal implementation for <code>DatabaseBackendMetaDataTest</code>.
     * 
     * @see java.sql.DatabaseMetaData#getTables(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String[])
     */
    public ResultSet getTables(String catalog, String schemaPattern,
        String tableNamePattern, String[] types) throws SQLException
    {
      if (tableNamePattern.equals("%"))
        return tables;
      else
        return null;
    }
  }
}
