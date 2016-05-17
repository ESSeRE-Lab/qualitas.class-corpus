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
 * Initial developer(s): Julie Marguerite.
 * Contributor(s): Emmanuel Cecchet, Nicolas Modrzyk.
 */

package org.objectweb.cjdbc.controller.virtualdatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import org.objectweb.cjdbc.common.exceptions.NoMoreBackendException;
import org.objectweb.cjdbc.common.exceptions.UnreachableBackendException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseProcedure;
import org.objectweb.cjdbc.common.sql.schema.DatabaseProcedureParameter;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;
import org.objectweb.cjdbc.common.users.VirtualDatabaseUser;
import org.objectweb.cjdbc.controller.authentication.AuthenticationManager;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;
import org.objectweb.cjdbc.controller.requestmanager.RAIDbLevels;
import org.objectweb.cjdbc.controller.requestmanager.RequestManager;
import org.objectweb.cjdbc.driver.DriverResultSet;
import org.objectweb.cjdbc.driver.Field;

/**
 * Class that gathers the dynamic metadata for a virtual database, that means
 * all the metadata subject to changes during the lifetime of the application.
 * 
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie.Marguerite </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class VirtualDatabaseDynamicMetaData
{

  /** Detect a null valu for int */
  public static final int NULL_VALUE = -999;

  private String          vdbName;
  private RequestManager  requestManager;

  /** Logger instance. */
  private Trace           logger     = null;

  /**
   * Reference the database for this metadata. Do not fetch any data at this
   * time
   * 
   * @param database to link this metadata to
   */
  public VirtualDatabaseDynamicMetaData(VirtualDatabase database)
  {
    this.vdbName = database.getVirtualDatabaseName();
    requestManager = database.getRequestManager();
    if (requestManager == null)
      throw new RuntimeException(
          "Null request manager in VirtualDatabaseMetaData");

    this.logger = Trace
        .getLogger("org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread."
            + vdbName + ".metadata");
  }

  /**
   * @see java.sql.DatabaseMetaData#getAttributes(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  public DriverResultSet getAttributes(String login, String catalog,
      String schemaPattern, String typeNamePattern, String attributeNamePattern)
      throws SQLException
  {
    // This is a JDBC 3.0 feature
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getAttributes(catalog, schemaPattern,
              typeNamePattern, attributeNamePattern);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[21];
            row[0] = cols.getObject(1); // TYPE_CAT
            row[1] = cols.getObject(2); // TYPE_SCHEM
            row[2] = cols.getObject(3); // TYPE_NAME
            row[3] = cols.getObject(4); // DATA_TYPE
            row[4] = cols.getObject(5); // ATTR_NAME
            row[5] = cols.getObject(6); // ATTR_TYPE_NAME
            row[6] = cols.getObject(7); // ATTR_SIZE
            row[7] = cols.getObject(8); // DECIMAL_DIGITS
            row[8] = cols.getObject(9); // NUM_PREC_RADIX
            row[9] = cols.getObject(10); // NULLABLE
            row[10] = cols.getObject(11); // REMARKS
            row[11] = cols.getObject(12); // ATTR_DEF
            row[12] = cols.getObject(13); // SQL_DATA_TYPE
            row[13] = cols.getObject(14); // SQL_DATETIME_SUB
            row[14] = cols.getObject(15); // CHAR_OCTET_LENGTH
            row[15] = cols.getObject(16); // ORDINAL_POSITION
            row[16] = cols.getObject(17); // IS_NULLABLE
            row[17] = cols.getObject(18); // SCOPE_CATALOG
            row[18] = cols.getObject(19); // SCOPE_SCHEMA
            row[19] = cols.getObject(20); // SCOPE_TABLE
            row[20] = cols.getObject(21); // SOURCE_DATA_TYPE
            data.add(row);
          }
          return new DriverResultSet(getAttributesFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet

    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(getAttributesFields, data);
    return rs;
  }

  /**
   * @see java.sql.DatabaseMetaData#getBestRowIdentifier(java.lang.String,
   *      java.lang.String, java.lang.String, int, boolean)
   */
  public DriverResultSet getBestRowIdentifier(String login, String catalog,
      String schema, String table, int scope, boolean nullable)
      throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getBestRowIdentifier(catalog, schema, table,
              scope, nullable);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[8];
            row[0] = cols.getObject(1); // SCOPE
            row[1] = cols.getObject(2); // COLUMN_NAME
            row[2] = cols.getObject(3); // DATA_TYPE
            row[3] = cols.getObject(4); // TYPE_NAME
            row[4] = cols.getObject(5); // COLUMN_SIZE
            row[5] = cols.getObject(6); // BUFFER_LENGTH
            row[6] = cols.getObject(7); // DECIMAL_DIGITS
            row[7] = cols.getObject(8); // PSEUDO_COLUMN
            data.add(row);
          }
          return new DriverResultSet(
              getBestRowIdentifierAndVersionColumnsFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet

    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(
        getBestRowIdentifierAndVersionColumnsFields, data);
    return rs;
  }

  /**
   * Build a list of Catalogs from a givem list of virtual database names
   * 
   * @param list of virtual database from the controller
   * @return <code>ResultSet</code> with list of catalogs
   */
  public DriverResultSet getCatalogs(ArrayList list)
  {
    int size = list.size();
    ArrayList data = new ArrayList(size);
    for (int i = 0; i < size; i++)
    {
      Object[] row = new Object[1];
      row[0] = (String) list.get(i);
      data.add(row);
    }
    DriverResultSet rs = new DriverResultSet(getCatalogsFields, data);
    return rs;
  }

  /**
   * @see java.sql.DatabaseMetaData#getColumnPrivileges(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  public DriverResultSet getColumnPrivileges(String login, String catalog,
      String schema, String table, String columnNamePattern)
      throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getColumnPrivileges(catalog, schema, table,
              columnNamePattern);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[8];
            row[0] = cols.getObject(1); // TABLE_CAT
            row[1] = cols.getObject(2); // TABLE_SCHEM
            row[2] = cols.getObject(3); // TABLE_NAME
            row[3] = cols.getObject(4); // COLUMN_NAME
            row[4] = cols.getObject(5); // GRANTOR
            row[5] = cols.getObject(6); // GRANTEE
            row[6] = cols.getObject(7); // PRIVILEGE
            row[7] = cols.getObject(8); // IS_GRANTABLE
            data.add(row);
          }
          return new DriverResultSet(getColumnPrivilegesFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    AuthenticationManager manager = requestManager.getVirtualDatabase()
        .getAuthenticationManager();

    DatabaseSchema dbs = requestManager.getDatabaseSchema();
    if (dbs == null)
      throw new SQLException("Unable to fetch the virtual database schema");

    if (columnNamePattern == null)
      // if null is passed then select all tables
      columnNamePattern = "%";

    DatabaseTable dbTable = dbs.getTable(table);
    if (dbTable == null)
      throw new SQLException("Unable to find table " + table);

    ArrayList columns = dbTable.getColumns();
    int size = columns.size();
    ArrayList data = new ArrayList();

    ArrayList virtualLogins = manager.getVirtualLogins();
    int vsize = virtualLogins.size();
    VirtualDatabaseUser vu;

    for (int i = 0; i < size; i++)
    {
      DatabaseColumn c = (DatabaseColumn) columns.get(i);
      if (columnNamePattern.equals("%")
          || columnNamePattern.equals(c.getName()))
      {
        for (int j = 0; j < vsize; j++)
        {
          vu = (VirtualDatabaseUser) virtualLogins.get(0);

          if (logger.isDebugEnabled())
            logger.debug("Found privilege for user:" + vu.getLogin()
                + " on column:" + c.getName());
          Object[] row = new Object[8];
          row[0] = vdbName; // table cat
          row[1] = null; // table schema
          row[2] = table; // table name
          row[3] = c.getName(); // column name
          row[4] = null; // grantor
          row[5] = vu.getLogin(); // grantee
          row[6] = "UPDATE"; // privilege
          row[7] = "NO"; // IS_GRANTABLE
          data.add(row);
        }
      }
    }

    DriverResultSet rs = new DriverResultSet(getColumnPrivilegesFields, data);
    return rs;
  }

  /**
   * @see java.sql.DatabaseMetaData#getColumns(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  public DriverResultSet getColumns(String login, String catalog,
      String schemaPattern, String tableNamePattern, String columnNamePattern)
      throws SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug("Getting columns for " + vdbName);

    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getColumns(catalog, schemaPattern,
              tableNamePattern, columnNamePattern);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[22];
            row[0] = cols.getObject(1); // TABLE_CAT
            row[1] = cols.getObject(2); // TABLE_SCHEM
            row[2] = cols.getObject(3); // TABLE_NAME
            row[3] = cols.getObject(4); // COLUMN_NAME
            row[4] = cols.getObject(5); // DATA_TYPE
            row[5] = cols.getObject(6); // TYPE_NAME
            row[6] = cols.getObject(7); // COLUMN_SIZE
            row[7] = cols.getObject(8); // BUFFER_LENGTH
            row[8] = cols.getObject(9); // DECIMAL_DIGITS
            row[9] = cols.getObject(10); // NUM_PREC_RADIX
            row[10] = cols.getObject(11); // NULLABLE
            row[11] = cols.getObject(12); // REMARKS
            row[12] = cols.getObject(13); // COLUMN_DEF
            row[13] = cols.getObject(14); // SQL_DATA_TYPE
            row[14] = cols.getObject(15); // SQL_DATETIME_SUB
            row[15] = cols.getObject(16); // CHAR_OCTET_LENGTH
            row[16] = cols.getObject(17); // ORDINAL_POSITION
            row[17] = cols.getObject(18); // IS_NULLABLE
            // JDBC 3.0 starts here
            try
            {
              row[18] = cols.getObject(19); // SCOPE_CATALOG
              row[19] = cols.getObject(20); // SCOPE_SCHEMA
              row[20] = cols.getObject(21); // SCOPE_TABLE
              row[21] = cols.getObject(22); // SOURCE_DATA_TYPE
            }
            catch (Exception e)
            { // Driver does not support JDBC 3.0 cut here
              row[18] = null; // SCOPE_CATALOG
              row[19] = null;// SCOPE_SCHEMA
              row[20] = null;// SCOPE_TABLE
              row[21] = null; // SOURCE_DATA_TYPE
            }
            data.add(row);
          }
          return new DriverResultSet(getColumnsFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Ok from this point on, this is RAIDb-0 or RAIDb-2 and we have to build
    // the results ourselves.
    DatabaseSchema dbs = requestManager.getDatabaseSchema();
    if (dbs == null)
      throw new SQLException("Unable to fetch the virtual database schema");

    if (tableNamePattern == null)
      tableNamePattern = "%"; // if null is passed then select
    // all tables

    if (columnNamePattern == null)
      columnNamePattern = "%"; // if null is passed then

    // Build the ResultSet
    ArrayList tables = dbs.getTables();
    int size = tables.size();
    ArrayList data = new ArrayList();

    for (int i = 0; i < size; i++)
    {
      DatabaseTable t = (DatabaseTable) tables.get(i);

      if (tableNamePattern.equals("%") || tableNamePattern.equals(t.getName()))
      {
        if (logger.isDebugEnabled())
          logger.debug("Found table " + t.getName());
        ArrayList columns = t.getColumns();
        for (int j = 0; j < columns.size(); j++)
        {
          DatabaseColumn c = (DatabaseColumn) columns.get(j);
          if (columnNamePattern.equals("%")
              || columnNamePattern.equals(c.getName()))
          {
            if (logger.isDebugEnabled())
              logger.debug("Found column " + c.getName());
            Object[] row = new Object[22];
            row[0] = vdbName; // TABLE_CAT
            row[1] = null; // TABLE_SCHEM
            row[2] = t.getName(); // TABLE_NAME
            row[3] = c.getName(); // COLUMN_NAME
            row[4] = new Integer(c.getType()); // DATA_TYPE
            row[5] = null; // TYPE_NAME
            row[6] = null; // COLUMN_SIZE
            row[7] = null; // BUFFER_LENGTH
            row[8] = null; // DECIMAL_DIGITS
            row[9] = null; // NUM_PREC_RADIX
            row[10] = null; // NULLABLE
            row[11] = null; // REMARKS
            row[12] = null; // COLUMN_DEF
            row[13] = null; // SQL_DATA_TYPE
            row[14] = null; // SQL_DATETIME_SUB
            row[15] = null; // CHAR_OCTET_LENGTH
            row[16] = null; // ORDINAL_POSITION
            row[17] = ""; // IS_NULLABLE
            row[18] = null; // SCOPE_CATALOG
            row[19] = null;// SCOPE_SCHEMA
            row[20] = null;// SCOPE_TABLE
            row[21] = null; // SOURCE_DATA_TYPE
            data.add(row);
          }
        }
      }
    }
    DriverResultSet rs = new DriverResultSet(getColumnsFields, data);
    return rs;
  }

  /**
   * @see java.sql.DatabaseMetaData#getCrossReference(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public DriverResultSet getCrossReference(String login, String primaryCatalog,
      String primarySchema, String primaryTable, String foreignCatalog,
      String foreignSchema, String foreignTable) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getCrossReference(primaryCatalog, primarySchema,
              primaryTable, foreignCatalog, foreignSchema, foreignTable);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[14];
            row[0] = cols.getObject(1); // PKTABLE_CAT
            row[1] = cols.getObject(2); // PKTABLE_SCHEM
            row[2] = cols.getObject(3); // PKTABLE_NAME
            row[3] = cols.getObject(4); // PKCOLUMN_NAME
            row[4] = cols.getObject(5); // FKTABLE_CAT
            row[5] = cols.getObject(6); // FKTABLE_SCHEM
            row[6] = cols.getObject(7); // FKTABLE_NAME
            row[7] = cols.getObject(8); // FKCOLUMN_NAME
            row[8] = cols.getObject(9); // KEY_SEQ
            row[9] = cols.getObject(10); // UPDATE_RULE
            row[10] = cols.getObject(11); // DELETE_RULE
            row[11] = cols.getObject(12); // FK_NAME
            row[12] = cols.getObject(13); // PK_NAME
            row[13] = cols.getObject(14); // DEFERRABILITY
            data.add(row);
          }
          return new DriverResultSet(
              getCrossReferenceOrImportExportedKeysFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet

    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(
        getCrossReferenceOrImportExportedKeysFields, data);
    return rs;
  }

  /**
   * @see java.sql.DatabaseMetaData#getExportedKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public DriverResultSet getExportedKeys(String login, String catalog,
      String schema, String table) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getExportedKeys(catalog, schema, table);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[14];
            row[0] = cols.getObject(1); // PKTABLE_CAT
            row[1] = cols.getObject(2); // PKTABLE_SCHEM
            row[2] = cols.getObject(3); // PKTABLE_NAME
            row[3] = cols.getObject(4); // PKCOLUMN_NAME
            row[4] = cols.getObject(5); // FKTABLE_CAT
            row[5] = cols.getObject(6); // FKTABLE_SCHEM
            row[6] = cols.getObject(7); // FKTABLE_NAME
            row[7] = cols.getObject(8); // FKCOLUMN_NAME
            row[8] = cols.getObject(9); // KEY_SEQ
            row[9] = cols.getObject(10); // UPDATE_RULE
            row[10] = cols.getObject(11); // DELETE_RULE
            row[11] = cols.getObject(12); // FK_NAME
            row[12] = cols.getObject(13); // PK_NAME
            row[13] = cols.getObject(14); // DEFERRABILITY
            data.add(row);
          }
          return new DriverResultSet(
              getCrossReferenceOrImportExportedKeysFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet

    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(
        getCrossReferenceOrImportExportedKeysFields, data);
    return rs;
  }

  /**
   * @see java.sql.DatabaseMetaData#getImportedKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public DriverResultSet getImportedKeys(String login, String catalog,
      String schema, String table) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getImportedKeys(catalog, schema, table);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[14];
            row[0] = cols.getObject(1); // PKTABLE_CAT
            row[1] = cols.getObject(2); // PKTABLE_SCHEM
            row[2] = cols.getObject(3); // PKTABLE_NAME
            row[3] = cols.getObject(4); // PKCOLUMN_NAME
            row[4] = cols.getObject(5); // FKTABLE_CAT
            row[5] = cols.getObject(6); // FKTABLE_SCHEM
            row[6] = cols.getObject(7); // FKTABLE_NAME
            row[7] = cols.getObject(8); // FKCOLUMN_NAME
            row[8] = cols.getObject(9); // KEY_SEQ
            row[9] = cols.getObject(10); // UPDATE_RULE
            row[10] = cols.getObject(11); // DELETE_RULE
            row[11] = cols.getObject(12); // FK_NAME
            row[12] = cols.getObject(13); // PK_NAME
            row[13] = cols.getObject(14); // DEFERRABILITY
            data.add(row);
          }
          return new DriverResultSet(
              getCrossReferenceOrImportExportedKeysFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet

    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(
        getCrossReferenceOrImportExportedKeysFields, data);
    return rs;
  }

  /**
   * @see java.sql.DatabaseMetaData#getIndexInfo(java.lang.String,
   *      java.lang.String, java.lang.String, boolean, boolean)
   */
  public DriverResultSet getIndexInfo(String login, String catalog,
      String schema, String table, boolean unique, boolean approximate)
      throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getIndexInfo(catalog, schema, table, unique,
              approximate);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[13];
            row[0] = cols.getObject(1); // TABLE_CAT
            row[1] = cols.getObject(2); // TABLE_SCHEM
            row[2] = cols.getObject(3); // TABLE_NAME
            row[3] = cols.getObject(4); // NON_UNIQUE
            row[4] = cols.getObject(5); // INDEX_QUALIFIER
            row[5] = cols.getObject(6); // INDEX_NAME
            row[6] = cols.getObject(7); // TYPE
            row[7] = cols.getObject(8); // ORDINAL_POSITION
            row[8] = cols.getObject(9); // COLUMN_NAME
            row[9] = cols.getObject(10); // ASC_OR_DESC
            row[10] = cols.getObject(11); // CARDINALITY
            row[11] = cols.getObject(12); // PAGES
            row[12] = cols.getObject(13); // FILTER_CONDITION
            data.add(row);
          }
          return new DriverResultSet(getIndexInfoFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet
    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(getIndexInfoFields, data);
    return rs;
  }

  /**
   * Gets a description of a table's primary key columns for the given login.
   * Primary keys are ordered by COLUMN_NAME.
   * 
   * @see java.sql.DatabaseMetaData#getPrimaryKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public DriverResultSet getPrimaryKeys(String login, String catalog,
      String schema, String table) throws SQLException
  {
    if (logger.isDebugEnabled())
      logger.debug("Getting getPrimaryKeys for " + vdbName);

    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet pks = m.getPrimaryKeys(catalog, schema, table);
          ArrayList data = new ArrayList();
          while (pks.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[6];
            row[0] = pks.getObject(1); // TABLE_CAT
            row[1] = pks.getObject(2); // TABLE_SCHEM
            row[2] = pks.getObject(3); // TABLE_NAME
            row[3] = pks.getObject(4); // COLUMN_NAME
            row[4] = pks.getObject(5); // KEY_SEQ
            row[5] = pks.getObject(6); // PK_NAME
            data.add(row);
          }
          return new DriverResultSet(getPrimaryKeysFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Ok from this point on, this is RAIDb-0 or RAIDb-2 and we have to build
    // the results ourselves.
    DatabaseSchema dbs = requestManager.getDatabaseSchema();
    if (dbs == null)
      throw new SQLException("Unable to fetch the virtual database schema");

    if (table == null)
      table = "%"; // if null is passed then
    // select all tables

    // Build the ResultSet
    ArrayList tables = dbs.getTables();
    int size = tables.size();
    ArrayList data = new ArrayList();

    for (int i = 0; i < size; i++)
    {
      DatabaseTable t = (DatabaseTable) tables.get(i);
      if (table.equals("%") || table.equals(t.getName()))
      {
        ArrayList columns = t.getColumns();
        for (int j = 0; j < columns.size(); j++)
        {
          DatabaseColumn c = (DatabaseColumn) columns.get(j);
          if (c.isUnique())
          {
            if (logger.isDebugEnabled())
              logger.debug("Found primary key" + c.getName());
            Object[] row = new Object[6];
            row[0] = vdbName; // TABLE_CAT
            row[1] = null; // TABLE_SCHEM
            row[2] = t.getName(); // TABLE_NAME
            row[3] = c.getName(); // COLUMN_NAME
            row[4] = new Integer(c.getType()); // KEY_SEQ
            row[5] = c.getName(); // PK_NAME
            data.add(row);
          }
          else
          {
            if (logger.isDebugEnabled())
              logger.debug("Key " + c.getName() + " is not unique");
          }
        }
      }
    }
    DriverResultSet rs = new DriverResultSet(getPrimaryKeysFields, data);
    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData#getProcedureColumns
   */
  public DriverResultSet getProcedureColumns(String login, String catalog,
      String schemaPattern, String procedureNamePattern,
      String columnNamePattern) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getColumns(catalog, schemaPattern,
              procedureNamePattern, columnNamePattern);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[13];
            row[0] = cols.getObject(1); // PROCEDURE_CAT
            row[1] = cols.getObject(2); // PROCEDURE_SCHEM
            row[2] = cols.getObject(3); // PROCEDURE_NAME
            row[3] = cols.getObject(4); // COLUMN_NAME
            row[4] = cols.getObject(5); // COLUMN_TYPE
            row[5] = cols.getObject(6); // DATA_TYPE
            row[6] = cols.getObject(7); // TYPE_NAME
            row[7] = cols.getObject(8); // PRECISION
            row[8] = cols.getObject(9); // LENGTH
            row[9] = cols.getObject(10); // SCALE
            row[10] = cols.getObject(11); // RADIX
            row[11] = cols.getObject(12); // NULLABLE
            row[12] = cols.getObject(13); // REMARKS
            data.add(row);
          }
          return new DriverResultSet(getProcedureColumnsFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    DatabaseSchema dbs = requestManager.getDatabaseSchema();
    if (dbs == null)
      throw new SQLException("Unable to fetch the virtual database schema");

    if (procedureNamePattern == null)
      procedureNamePattern = "%";

    if (columnNamePattern == null)
      columnNamePattern = "%";

    // Build the ResultSet
    ArrayList procedures = dbs.getProcedures();
    int size = procedures.size();
    ArrayList data = new ArrayList();
    for (int i = 0; i < size; i++)
    {
      DatabaseProcedure t = (DatabaseProcedure) procedures.get(i);
      if (procedureNamePattern.equals("%")
          || procedureNamePattern.equals(t.getName()))
      {
        if (logger.isDebugEnabled())
          logger.debug("Found matching procedure " + t.getName());

        ArrayList params = t.getParameters();
        int sizep = params.size();
        DatabaseProcedureParameter param;
        for (int k = 0; k < sizep; k++)
        {
          param = (DatabaseProcedureParameter) params.get(k);
          if (columnNamePattern.equals("%")
              || columnNamePattern.equals(t.getName()))
          {
            if (logger.isDebugEnabled())
              logger.debug("Found matching procedure parameter"
                  + param.getName());

            Object[] row = new Object[13];
            row[0] = vdbName; // PROCEDURE_CAT
            row[1] = null; // PROCEDURE_SCHEM
            row[2] = t.getName(); // PROCEDURE_NAME
            row[3] = param.getName(); // COLUMN_NAME
            row[4] = new Integer(param.getColumnType()); // COLUMN_TYPE
            row[5] = new Integer(param.getDataType()); // DATA_TYPE
            row[6] = param.getTypeName(); // TYPE_NAME
            row[7] = new Float(param.getPrecision()); // PRECISION
            row[8] = new Integer(param.getLength()); // LENGTH
            row[9] = new Integer(param.getScale()); // SCALE
            row[10] = new Integer(param.getRadix()); // RADIX
            row[11] = new Integer(param.getNullable()); // NULLABLE
            row[12] = param.getRemarks();

            data.add(row);
          }
        }
      }
    }
    DriverResultSet rs = new DriverResultSet(getProcedureColumnsFields, data);
    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData#getProcedures(String,
   *      String, String)
   */
  public DriverResultSet getProcedures(String login, String catalog,
      String schemaPattern, String procedureNamePattern) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getProcedures(catalog, schemaPattern,
              procedureNamePattern);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[8];
            row[0] = cols.getObject(1); // PROCEDURE_CAT
            row[1] = cols.getObject(2); // PROCEDURE_SCHEM
            row[2] = cols.getObject(3); // PROCEDURE_NAME
            row[3] = cols.getObject(4); // reserved for future use
            row[4] = cols.getObject(5); // reserved for future use
            row[5] = cols.getObject(6); // reserved for future use
            row[6] = cols.getObject(7); // REMARKS
            row[7] = cols.getObject(8); // PROCEDURE_TYPE
            data.add(row);
          }
          return new DriverResultSet(getProceduresFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    DatabaseSchema dbs = requestManager.getDatabaseSchema();
    if (dbs == null)
      throw new SQLException("Unable to fetch the virtual database schema");

    if (procedureNamePattern == null)
      procedureNamePattern = "%"; // if null is passed then
    // select all procedures

    // Build the ResultSet
    ArrayList procedures = dbs.getProcedures();
    int size = procedures.size();
    ArrayList data = new ArrayList();
    for (int i = 0; i < size; i++)
    {
      DatabaseProcedure t = (DatabaseProcedure) procedures.get(i);
      if (procedureNamePattern.equals("%")
          || procedureNamePattern.equals(t.getName()))
      {
        if (logger.isDebugEnabled())
          logger.debug("Found procedure " + t.getName());
        Object[] row = new Object[8];
        row[0] = vdbName; // PROCEDURE_CAT
        row[1] = null; // PROCEDURE_SCHEM
        row[2] = t.getName(); // PROCEDURE_NAME
        row[3] = null; // reserved for future use
        row[4] = null; // reserved for future use
        row[5] = null; // reserved for future use
        row[6] = t.getRemarks(); // REMARKS
        row[7] = new Integer(t.getProcedureType()); // PROCEDURE_TYPE
        data.add(row);
      }
    }
    DriverResultSet rs = new DriverResultSet(getProceduresFields, data);
    return rs;
  }

  /**
   * Will return the schema from the call to getSchemas() on the first available
   * node.
   * 
   * @see java.sql.DatabaseMetaData#getSchemas()
   */
  public DriverResultSet getSchemas(String login) throws SQLException
  {
    try
    { // Forward directly to the underlying backend
      ConnectionAndDatabaseMetaData info = null;
      try
      {
        info = getMetaDataFromFirstAvailableBackend(login);
        DatabaseMetaData m = info.getDatabaseMetaData();
        ResultSet cols = m.getSchemas();
        ArrayList data = new ArrayList();
        while (cols.next())
        { // Unroll the loop for comments (and speed?)
          Object[] row = new Object[2];
          row[0] = cols.getObject(1); // TABLE_SCHEM
          // JDBC 3.0 starts here
          try
          {
            row[1] = cols.getObject(2); // TABLE_CATALOG
          }
          catch (Exception e)
          { // Driver does not support JDBC 3.0 cut here
            row[1] = null; // TABLE_SCHEM
          }
          data.add(row);
        }
        return new DriverResultSet(getSchemasFields, data);
      }
      catch (SQLException e)
      {
        throw e;
      }
      finally
      {
        releaseConnection(info);
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    Object[] row = new Object[2];
    row[0] = vdbName; // TABLE_SCHEM
    row[1] = null; // TABLE_CATALOG
    ArrayList data = new ArrayList();
    data.add(row);
    return new DriverResultSet(getSchemasFields, data);
  }

  /**
   * @see java.sql.DatabaseMetaData#getSuperTables(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public DriverResultSet getSuperTables(String login, String catalog,
      String schemaPattern, String tableNamePattern) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getSuperTables(catalog, schemaPattern,
              tableNamePattern);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[4];
            row[0] = cols.getObject(1); // TABLE_CAT
            row[1] = cols.getObject(2); // TABLE_SCHEM
            row[2] = cols.getObject(3); // TABLE_NAME
            row[3] = cols.getObject(4); // SUPERTABLE_NAME
            data.add(row);
          }
          return new DriverResultSet(getSuperTablesFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet

    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(getSuperTablesFields, data);
    return rs;
  }

  /**
   * @see java.sql.DatabaseMetaData#getSuperTypes(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public DriverResultSet getSuperTypes(String login, String catalog,
      String schemaPattern, String tableNamePattern) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getSuperTypes(catalog, schemaPattern,
              tableNamePattern);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[5];
            row[0] = cols.getObject(1); // TYPE_CAT
            row[1] = cols.getObject(2); // TYPE_SCHEM
            row[2] = cols.getObject(3); // TYPE_NAME
            row[3] = cols.getObject(4); // SUPERTYPE_CAT
            row[4] = cols.getObject(5); // SUPERTYPE_SCHEM
            data.add(row);
          }
          return new DriverResultSet(getSuperTypesFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet

    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(getSuperTypesFields, data);
    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData#getTablePrivileges(String,
   *      String, String)
   */
  public DriverResultSet getTablePrivileges(String login, String catalog,
      String schemaPattern, String tableNamePattern) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getTablePrivileges(catalog, schemaPattern,
              tableNamePattern);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[7];
            row[0] = cols.getObject(1); // TABLE_CAT
            row[1] = cols.getObject(2); // TABLE_SCHEM
            row[2] = cols.getObject(3); // TABLE_NAME
            row[3] = cols.getObject(4); // GRANTOR
            row[4] = cols.getObject(5); // GRANTEE
            row[5] = cols.getObject(6); // PRIVILEGE
            row[6] = cols.getObject(7); // IS_GRANTABLE
            data.add(row);
          }
          return new DriverResultSet(getTablePrivilegesFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    AuthenticationManager manager = requestManager.getVirtualDatabase()
        .getAuthenticationManager();

    DatabaseSchema dbs = requestManager.getDatabaseSchema();
    if (dbs == null)
      throw new SQLException("Unable to fetch the virtual database schema");

    if (tableNamePattern == null)
      // if null is passed then select all tables
      tableNamePattern = "%";

    ArrayList tables = dbs.getTables();
    int size = tables.size();
    ArrayList data = new ArrayList();

    ArrayList virtualLogins = manager.getVirtualLogins();
    int vsize = virtualLogins.size();
    VirtualDatabaseUser vu;

    for (int i = 0; i < size; i++)
    {
      DatabaseTable t = (DatabaseTable) tables.get(i);
      if (tableNamePattern.equals("%") || tableNamePattern.equals(t.getName()))
      {
        for (int j = 0; j < vsize; j++)
        {
          vu = (VirtualDatabaseUser) virtualLogins.get(0);

          if (logger.isDebugEnabled())
            logger.debug("Found privilege for user:" + vu.getLogin()
                + " on table:" + t.getName());
          Object[] row = new Object[7];
          row[0] = vdbName; // table cat
          row[1] = null; // table schema
          row[2] = t.getName(); // table name
          row[3] = null; // grantor
          row[4] = vu.getLogin(); // grantee
          row[5] = "UPDATE"; // privilege
          row[6] = "NO"; // IS_GRANTABLE
          data.add(row);
        }
      }
    }

    DriverResultSet rs = new DriverResultSet(getTablePrivilegesFields, data);
    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData#getTables(String, String,
   *      String, String[])
   */
  public DriverResultSet getTables(String login, String catalog,
      String schemaPattern, String tableNamePattern, String[] types)
      throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getTables(catalog, schemaPattern,
              tableNamePattern, types);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[10];
            row[0] = cols.getObject(1); // TABLE_CAT
            row[1] = cols.getObject(2); // TABLE_SCHEM
            row[2] = cols.getObject(3); // TABLE_NAME
            row[3] = cols.getObject(4); // TABLE_TYPE
            row[4] = cols.getObject(5); // REMARKS

            // JDBC 3.0 starts here
            try
            {
              row[5] = cols.getObject(6); // TYPE_CAT
              row[6] = cols.getObject(7); // TYPE_SCHEM
              row[7] = cols.getObject(8); // TYPE_NAME
              row[8] = cols.getObject(9); // SELF_REFERENCING_COL_NAME
              row[9] = cols.getObject(10); // REF_GENERATION
            }
            catch (Exception e)
            { // Driver does not support JDBC 3.0 cut here
              row[5] = null;
              row[6] = null;
              row[7] = null;
              row[8] = null;
              row[9] = null;
            }
            data.add(row);
          }
          return new DriverResultSet(getTablesFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    DatabaseSchema dbs = requestManager.getDatabaseSchema();
    if (dbs == null)
      throw new SQLException("Unable to fetch the virtual database schema");

    if (tableNamePattern == null)
      // if null is passed then select all tables
      tableNamePattern = "%";

    // Build the ResultSet
    ArrayList tables = dbs.getTables();
    int size = tables.size();
    ArrayList data = new ArrayList();
    for (int i = 0; i < size; i++)
    {
      DatabaseTable t = (DatabaseTable) tables.get(i);
      if (tableNamePattern.equals("%")
          || t.getName().indexOf(tableNamePattern) != -1)
      {
        if (logger.isDebugEnabled())
          logger.debug("Found table " + t.getName());
        Object[] row = new Object[10];
        row[0] = vdbName; // TABLE_CAT
        row[1] = null; // TABLE_SCHEM
        row[2] = t.getName(); // TABLE_NAME
        row[3] = "TABLE"; // TABLE_TYPE
        row[4] = null; // REMARKS
        row[5] = null; // TYPE_CAT
        row[6] = null; // TYPE_SCHEM
        row[7] = null; // TYPE_NAME
        row[8] = null; // SELF_REFERENCING_COL_NAME
        row[9] = "SYSTEM"; // REF_GENERATION
        data.add(row);
      }
    }
    DriverResultSet rs = new DriverResultSet(getTablesFields, data);
    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData#getTableTypes()
   */
  public DriverResultSet getTableTypes(String login) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getTableTypes();
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[1];
            row[0] = cols.getObject(1); // TABLE_TYPE
            data.add(row);
          }
          return new DriverResultSet(getTableTypesFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    ArrayList list = new ArrayList(1);
    Object[] row = new Object[1];
    row[0] = "TABLE"; // TABLE_TYPE
    list.add(row);
    DriverResultSet rs = new DriverResultSet(getTableTypesFields, list);
    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData#getTypeInfo()
   */
  public DriverResultSet getTypeInfo(String login) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getTypeInfo();
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[18];
            row[0] = cols.getObject(1); // TYPE_NAME
            row[1] = cols.getObject(2); // DATA_TYPE
            row[2] = cols.getObject(3); // PRECISION
            row[3] = cols.getObject(4); // LITERAL_PREFIX
            row[4] = cols.getObject(5); // LITERAL_SUFFIX
            row[5] = cols.getObject(6); // CREATE_PARAMS
            row[6] = cols.getObject(7); // NULLABLE
            row[7] = cols.getObject(8); // CASE_SENSITIVE
            row[8] = cols.getObject(9); // SEARCHABLE
            row[9] = cols.getObject(10); // UNSIGNED_ATTRIBUTE
            row[10] = cols.getObject(11); // FIXED_PREC_SCALE
            row[11] = cols.getObject(12); // AUTO_INCREMENT
            row[12] = cols.getObject(13); // LOCAL_TYPE_NAME
            row[13] = cols.getObject(14); // MINIMUM_SCALE
            row[14] = cols.getObject(15); // MAXIMUM_SCALE
            row[15] = cols.getObject(16); // SQL_DATA_TYPE
            row[16] = cols.getObject(17); // SQL_DATETIME_SUB
            row[17] = cols.getObject(18); // NUM_PREC_RADIX
            data.add(row);
          }
          return new DriverResultSet(getTypeInfoFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet

    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(getTypeInfoFields, data);
    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData#getUDTs(String, String,
   *      String, int[])
   */
  public DriverResultSet getUDTs(String login, String catalog,
      String schemaPattern, String tableNamePattern, int[] types)
      throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getUDTs(catalog, schemaPattern, tableNamePattern,
              types);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[6];
            row[0] = cols.getObject(1); // TYPE_CAT
            row[1] = cols.getObject(2); // TYPE_SCHEM
            row[2] = cols.getObject(3); // TYPE_NAME
            row[3] = cols.getObject(4); // CLASS_NAME
            row[4] = cols.getObject(5); // DATA_TYPE
            row[5] = cols.getObject(6); // REMARKS

            // JDBC 3.0 starts here
            try
            {
              row[6] = cols.getObject(7); // BASE_TYPE
            }
            catch (Exception e)
            { // Driver does not support JDBC 3.0 cut here
              row[6] = null; // BASE_TYPE
            }

            data.add(row);
          }
          return new DriverResultSet(getUDTsFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet

    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(getUDTsFields, data);
    return rs;
  }

  /**
   * @see org.objectweb.cjdbc.driver.DatabaseMetaData#getVersionColumns(String,
   *      String, String)
   */
  public DriverResultSet getVersionColumns(String login, String catalog,
      String schema, String table) throws SQLException
  {
    try
    {
      int raidbLevel = requestManager.getLoadBalancer().getRAIDbLevel();
      if ((raidbLevel == RAIDbLevels.RAIDb1)
          || (raidbLevel == RAIDbLevels.SingleDB))
      { // Forward directly to the underlying backend
        ConnectionAndDatabaseMetaData info = null;
        try
        {
          info = getMetaDataFromFirstAvailableBackend(login);
          DatabaseMetaData m = info.getDatabaseMetaData();
          ResultSet cols = m.getVersionColumns(catalog, schema, table);
          ArrayList data = new ArrayList();
          while (cols.next())
          { // Unroll the loop for comments (and speed?)
            Object[] row = new Object[8];
            row[0] = cols.getObject(1); // SCOPE
            row[1] = cols.getObject(2); // COLUMN_NAME
            row[2] = cols.getObject(3); // DATA_TYPE
            row[3] = cols.getObject(4); // TYPE_NAME
            row[4] = cols.getObject(5); // COLUMN_SIZE
            row[5] = cols.getObject(6); // BUFFER_LENGTH
            row[6] = cols.getObject(7); // DECIMAL_DIGITS
            row[7] = cols.getObject(8); // PSEUDO_COLUMN
            data.add(row);
          }
          return new DriverResultSet(
              getBestRowIdentifierAndVersionColumnsFields, data);
        }
        catch (SQLException e)
        {
          throw e;
        }
        finally
        {
          releaseConnection(info);
        }
      }
    }
    catch (NoMoreBackendException ignore)
    {
      // No backend is available, try with the default method
    }

    // Feature not supported in RAIDb-0 and RAIDb-2, return an empty ResultSet

    ArrayList data = new ArrayList();
    DriverResultSet rs = new DriverResultSet(
        getBestRowIdentifierAndVersionColumnsFields, data);
    return rs;
  }

  /**
   * Get the first available backend from the virtual database
   * 
   * @return the first available backend or null if no backend enabled is found
   */
  private DatabaseBackend getFirstAvailableBackend()
  {
    // Parse the list in a failfast manner
    ArrayList backends = requestManager.getVirtualDatabase().getBackends();
    try
    {
      for (Iterator iter = backends.iterator(); iter.hasNext();)
      {
        DatabaseBackend b = (DatabaseBackend) iter.next();
        if (b.isReadEnabled())
          return b;
      }
    }
    catch (ConcurrentModificationException e)
    {
      return getFirstAvailableBackend();
    }

    return null;
  }

  /**
   * Get DatabaseMetaData from the first available backend.
   * 
   * @param login the login to use to fetch metadata
   * @return the DatabaseMetaData obtained from the first available backend
   *         among with the connection information
   * @throws NoMoreBackendException if no backend is enabled on this controller
   * @throws SQLException if an error occured while getting MetaData
   */
  private ConnectionAndDatabaseMetaData getMetaDataFromFirstAvailableBackend(
      String login) throws NoMoreBackendException, SQLException
  {
    DatabaseBackend b = getFirstAvailableBackend();
    if (b == null)
      throw new NoMoreBackendException(
          "No backend is enabled in virtual database " + vdbName);
    AbstractConnectionManager cm = b.getConnectionManager(login);
    if (cm == null)
      throw new SQLException("Invalid login " + login + " on backend "
          + b.getName());
    Connection c;
    try
    {
      c = cm.getConnection();
    }
    catch (UnreachableBackendException e)
    {
      throw new SQLException("Unable to get a connection for login " + login);
    }
    return new ConnectionAndDatabaseMetaData(c, cm, c.getMetaData());
  }

  /**
   * Release the connection used to fetch the metadata
   * 
   * @param info the connection information returned by
   *          getMetaDataFromFirstAvailableBackend
   * @see #getMetaDataFromFirstAvailableBackend(String)
   */
  private void releaseConnection(ConnectionAndDatabaseMetaData info)
  {
    if (info == null)
      return;
    info.getConnectionManager().releaseConnection(info.getConnection());
  }

  /**
   * This class defines a ConnectionAndDatabaseMetaData used to carry metadata
   * and connection related information to properly release the connection later
   * on.
   * 
   * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet</a>
   * @version 1.0
   */
  private class ConnectionAndDatabaseMetaData
  {
    DatabaseMetaData          databaseMetaData;
    AbstractConnectionManager connectionManager;
    Connection                connection;

    /**
     * Creates a new <code>ConnectionAndDatabaseMetaData</code> object
     * 
     * @param c the connection used to get the metadata
     * @param cm the connection manager used to get the connection
     * @param metadata the metadata fetched from the connection
     */
    public ConnectionAndDatabaseMetaData(Connection c,
        AbstractConnectionManager cm, DatabaseMetaData metadata)
    {
      this.connection = c;
      this.connectionManager = cm;
      this.databaseMetaData = metadata;
    }

    /**
     * Returns the connection value.
     * 
     * @return Returns the connection.
     */
    public Connection getConnection()
    {
      return connection;
    }

    /**
     * Returns the connectionManager value.
     * 
     * @return Returns the connectionManager.
     */
    public AbstractConnectionManager getConnectionManager()
    {
      return connectionManager;
    }

    /**
     * Returns the databaseMetaData value.
     * 
     * @return Returns the databaseMetaData.
     */
    public DatabaseMetaData getDatabaseMetaData()
    {
      return databaseMetaData;
    }

  }

  /**
   * @see java.sql.DatabaseMetaData#getAttributes(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  private static Field[] getAttributesFields                         = new Field[]{
      new Field("TYPE_CAT", "TYPE_CAT", 9, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TYPE_SCHEM", "TYPE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TYPE_NAME", "TYPE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("DATA_TYPE", "DATA_TYPE", 10, Types.SMALLINT, "SMALLINT",
          "Short"),
      new Field("ATTR_NAME", "ATTR_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("ATTR_TYPE_NAME", "ATTR_TYPE_NAME", 10, Types.VARCHAR,
          "VARCHAR", "String"),
      new Field("ATTR_SIZE", "ATTR_SIZE", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("DECIMAL_DIGITS", "DECIMAL_DIGITS", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("NUM_PREC_RADIX", "NUM_PREC_RADIX", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("NULLABLE", "NULLABLE", 10, Types.INTEGER, "INTEGER", "Integer"),
      new Field("REMARKS", "REMARKS", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("ATTR_DEF", "ATTR_DEF", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("SQL_DATA_TYPE", "SQL_DATA_TYPE", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("SQL_DATETIME_SUB", "SQL_DATETIME_SUB", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("CHAR_OCTET_LENGTH", "CHAR_OCTET_LENGTH", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("ORDINAL_POSITION", "ORDINAL_POSITION", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("IS_NULLABLE", "IS_NULLABLE", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SCOPE_CATALOG", "SCOPE_CATALOG", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SCOPE_SCHEMA", "SCOPE_SCHEMA", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SCOPE_TABLE", "SCOPE_TABLE", 10, Types.VARCHAR, "VARCHAR",
          "String")                                                  };

  /**
   * @see java.sql.DatabaseMetaData#getBestRowIdentifier(java.lang.String,
   *      java.lang.String, java.lang.String, int, boolean)
   */
  private static Field[] getBestRowIdentifierAndVersionColumnsFields = new Field[]{
      new Field("SCOPE", "SCOPE", 10, Types.SMALLINT, "SMALLINT", "Short"),
      new Field("COLUMN_NAME", "COLUMN_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("DATA_TYPE", "DATA_TYPE", 10, Types.SMALLINT, "SMALLINT",
          "Short"),
      new Field("TYPE_NAME", "TYPE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("COLUMN_SIZE", "COLUMN_SIZE", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("BUFFER_LENGTH", "BUFFER_LENGTH", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("DECIMAL_DIGITS", "DECIMAL_DIGITS", 10, Types.SMALLINT,
          "SMALLINT", "Short"),
      new Field("PSEUDO_COLUMN", "PSEUDO_COLUMN", 10, Types.SMALLINT,
          "SMALLINT", "Short")                                       };

  /**
   * @see java.sql.DatabaseMetaData#getCatalogs()
   */
  private static Field[] getCatalogsFields                           = new Field[]{new Field(
                                                                         "TABLE_CAT",
                                                                         "TABLE_CAT",
                                                                         9,
                                                                         Types.VARCHAR,
                                                                         "VARCHAR",
                                                                         "String")};

  /**
   * @see java.sql.DatabaseMetaData#getColumnPrivileges(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  private static Field[] getColumnPrivilegesFields                   = new Field[]{
      new Field("TABLE_CAT", "TABLE_CAT", 9, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TABLE_SCHEM", "TABLE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TABLE_NAME", "TABLE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("COLUMN_NAME", "COLUMN_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("GRANTOR", "GRANTOR", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("GRANTEE", "GRANTEE", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("PRIVILEGE", "PRIVILEGE", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("IS_GRANTABLE", "IS_GRANTABLE", 10, Types.VARCHAR, "VARCHAR",
          "String"),                                                 };

  /**
   * @see java.sql.DatabaseMetaData#getColumns(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  private static Field[] getColumnsFields                            = new Field[]{
      new Field("TABLE_CAT", "TABLE_CAT", 9, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TABLE_SCHEM", "TABLE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TABLE_NAME", "TABLE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("COLUMN_NAME", "COLUMN_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("DATA_TYPE", "DATA_TYPE", 10, Types.SMALLINT, "SMALLINT",
          "Short"),
      new Field("TYPE_NAME", "TYPE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("COLUMN_SIZE", "COLUMN_SIZE", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("BUFFER_LENGTH", "BUFFER_LENGTH", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("DECIMAL_DIGITS", "DECIMAL_DIGITS", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("NUM_PREC_RADIX", "NUM_PREC_RADIX", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("NULLABLE", "NULLABLE", 10, Types.INTEGER, "INTEGER", "Integer"),
      new Field("REMARKS", "REMARKS", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("COLUMN_DEF", "COLUMN_DEF", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SQL_DATA_TYPE", "SQL_DATA_TYPE", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("SQL_DATETIME_SUB", "SQL_DATETIME_SUB", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("CHAR_OCTET_LENGTH", "CHAR_OCTET_LENGTH", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("ORDINAL_POSITION", "ORDINAL_POSITION", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("IS_NULLABLE", "IS_NULLABLE", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SCOPE_CATALOG", "SCOPE_CATALOG", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SCOPE_SCHEMA", "SCOPE_SCHEMA", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SCOPE_TABLE", "SCOPE_TABLE", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SOURCE_DATA_TYPE", "SOURCE_DATA_TYPE", 10, Types.SMALLINT,
          "SMALLINT", "Short")                                       };

  /**
   * @see java.sql.DatabaseMetaData#getCrossReference(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String)
   * @see java.sql.DatabaseMetaData#getImportedKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private static Field[] getCrossReferenceOrImportExportedKeysFields = new Field[]{
      new Field("PKTABLE_CAT", "PKTABLE_CAT", 9, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("PKTABLE_SCHEM", "PKTABLE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("PKTABLE_NAME", "PKTABLE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("PKCOLUMN_NAME", "PKCOLUMN_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("FKTABLE_CAT", "FKTABLE_CAT", 9, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("FKTABLE_SCHEM", "FKTABLE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("FKTABLE_NAME", "FKTABLE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("FKCOLUMN_NAME", "FKCOLUMN_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("KEY_SEQ", "KEY_SEQ", 10, Types.SMALLINT, "SMALLINT", "Short"),
      new Field("UPDATE_RULE", "UPDATE_RULE", 10, Types.SMALLINT, "SMALLINT",
          "Short"),
      new Field("DELETE_RULE", "DELETE_RULE", 10, Types.SMALLINT, "SMALLINT",
          "Short"),
      new Field("FK_NAME", "FK_NAME", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("PK_NAME", "PK_NAME", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("DEFERRABILITY", "DEFERRABILITY", 10, Types.SMALLINT,
          "SMALLINT", "Short")                                       };

  /**
   * @see java.sql.DatabaseMetaData#getIndexInfo(java.lang.String,
   *      java.lang.String, java.lang.String, boolean, boolean)
   */
  private static Field[] getIndexInfoFields                          = new Field[]{
      new Field("TABLE_CAT", "TABLE_CAT", 9, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TABLE_SCHEM", "TABLE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TABLE_NAME", "TABLE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("NON_UNIQUE", "NON_UNIQUE", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("INDEX_QUALIFIER", "INDEX_QUALIFIER", 10, Types.VARCHAR,
          "VARCHAR", "String"),
      new Field("INDEX_NAME", "INDEX_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TYPE", "TYPE", 10, Types.SMALLINT, "SMALLINT", "Short"),
      new Field("ORDINAL_POSITION", "ORDINAL_POSITION", 10, Types.SMALLINT,
          "SMALLINT", "Short"),
      new Field("COLUMN_NAME", "COLUMN_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("ASC_OR_DESC", "ASC_OR_DESC", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("CARDINALITY", "CARDINALITY", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("PAGES", "PAGES", 10, Types.INTEGER, "INTEGER", "Integer"),
      new Field("FILTER_CONDITION", "FILTER_CONDITION", 10, Types.VARCHAR,
          "VARCHAR", "String")                                       };

  /**
   * @see java.sql.DatabaseMetaData#getPrimaryKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private static Field[] getPrimaryKeysFields                        = new Field[]{
      new Field("TABLE_CAT", "TABLE_CAT", 9, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TABLE_SCHEM", "TABLE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TABLE_NAME", "TABLE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("COLUMN_NAME", "COLUMN_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("KEY_SEQ", "KEY_SEQ", 10, Types.SMALLINT, "SMALLINT", "Short"),
      new Field("PK_NAME", "PK_NAME", 10, Types.VARCHAR, "VARCHAR", "String")};
  /**
   * @see java.sql.DatabaseMetaData#getProcedureColumns(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  private static Field[] getProcedureColumnsFields                   = new Field[]{
      new Field("PROCEDURE_CAT", "PROCEDURE_CAT", 9, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("PROCEDURE_SCHEM", "PROCEDURE_SCHEM", 10, Types.VARCHAR,
          "VARCHAR", "String"),
      new Field("PROCEDURE_NAME", "PROCEDURE_NAME", 10, Types.VARCHAR,
          "VARCHAR", "String"),
      new Field("COLUMN_NAME", "COLUMN_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("COLUMN_TYPE", "COLUMN_TYPE", 10, Types.SMALLINT, "SMALLINT",
          "Short"),
      new Field("DATA_TYPE", "DATA_TYPE", 10, Types.SMALLINT, "SMALLINT",
          "Short"),
      new Field("TYPE_NAME", "TYPE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("PRECISION", "PRECISION", 10, Types.FLOAT, "FLOAT", "Float"),
      new Field("LENGTH", "LENGTH", 10, Types.INTEGER, "INTEGER", "Integer"),
      new Field("SCALE", "SCALE", 10, Types.SMALLINT, "SMALLINT", "Short"),
      new Field("RADIX", "RADIX", 10, Types.SMALLINT, "SMALLINT", "Short"),
      new Field("NULLABLE", "NULLABLE", 10, Types.SMALLINT, "SMALLINT", "Short"),
      new Field("REMARKS", "REMARKS", 10, Types.VARCHAR, "VARCHAR", "String")};

  /**
   * @see java.sql.DatabaseMetaData#getProcedures(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private static Field[] getProceduresFields                         = new Field[]{
      new Field("PROCEDURE_CAT", "PROCEDURE_CAT", 9, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("PROCEDURE_SCHEM", "PROCEDURE_SCHEM", 10, Types.VARCHAR,
          "VARCHAR", "String"),
      new Field("PROCEDURE_NAME", "PROCEDURE_NAME", 10, Types.VARCHAR,
          "VARCHAR", "String"),
      new Field("", "", 0, Types.VARCHAR, "VARCHAR", "String"),
      new Field("", "", 0, Types.VARCHAR, "VARCHAR", "String"),
      new Field("", "", 0, Types.VARCHAR, "VARCHAR", "String"),
      new Field("REMARKS", "REMARKS", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("PROCEDURE_TYPE", "PROCEDURE_TYPE", 10, Types.SMALLINT,
          "SMALLINT", "Short")                                       };

  /**
   * @see java.sql.DatabaseMetaData#getSchemas()
   */
  private static Field[] getSchemasFields                            = new Field[]{
      new Field("TABLE_SCHEM", "TABLE_SCHEM", 9, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TABLE_CATALOG", "TABLE_CATALOG", 9, Types.VARCHAR, "VARCHAR",
          "String")                                                  };

  /**
   * @see java.sql.DatabaseMetaData#getSuperTables(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private static Field[] getSuperTablesFields                        = new Field[]{
      new Field("TABLE_CAT", "TABLE_CAT", 9, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TABLE_SCHEM", "TABLE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TABLE_NAME", "TABLE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SUPERTABLE_NAME", "SUPERTABLE_NAME", 10, Types.VARCHAR,
          "VARCHAR", "String")                                       };

  /**
   * @see java.sql.DatabaseMetaData#getSuperTypes(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private static Field[] getSuperTypesFields                         = new Field[]{
      new Field("TYPE_CAT", "TYPE_CAT", 9, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TYPE_SCHEM", "TYPE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TYPE_NAME", "TYPE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SUPERTYPE_CAT", "SUPERTYPE_CAT", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SUPERTYPE_SCHEM", "SUPERTYPE_SCHEM", 10, Types.VARCHAR,
          "VARCHAR", "String")                                       };

  /**
   * @see java.sql.DatabaseMetaData#getTablePrivileges(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  private static Field[] getTablePrivilegesFields                    = new Field[]{
      new Field("TABLE_CAT", "TABLE_CAT", 9, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TABLE_SCHEM", "TABLE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TABLE_NAME", "TABLE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("GRANTOR", "GRANTOR", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("GRANTEE", "GRANTEE", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("PRIVILEGE", "PRIVILEGE", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("IS_GRANTABLE", "IS_GRANTABLE", 10, Types.VARCHAR, "VARCHAR",
          "String"),                                                 };

  /**
   * @see java.sql.DatabaseMetaData#getTables(String, String, String, String[])
   */
  private static Field[] getTablesFields                             = new Field[]{
      new Field("TABLE_CAT", "TABLE_CAT", 9, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TABLE_SCHEM", "TABLE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TABLE_NAME", "TABLE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TABLE_TYPE", "TABLE_TYPE", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("REMARKS", "REMARKS", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TYPE_CAT", "TYPE_CAT", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TYPE_SCHEM", "TYPE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TYPE_NAME", "TYPE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("SELF_REFERENCING_COL_NAME", "SELF_REFERENCING_COL_NAME", 25,
          Types.VARCHAR, "VARCHAR", "String"),
      new Field("REF_GENERATION", "REF_GENERATION", 15, Types.VARCHAR,
          "VARCHAR", "String")                                       };

  /**
   * @see java.sql.DatabaseMetaData#getTableTypes()
   */
  private static Field[] getTableTypesFields                         = new Field[]{new Field(
                                                                         "TABLE_TYPE",
                                                                         "TABLE_TYPE",
                                                                         9,
                                                                         Types.VARCHAR,
                                                                         "VARCHAR",
                                                                         "String")};

  /**
   * @see java.sql.DatabaseMetaData#getTypeInfo()
   */
  private static Field[] getTypeInfoFields                           = new Field[]{
      new Field("TYPE_NAME", "TYPE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("DATA_TYPE", "DATA_TYPE", 10, Types.SMALLINT, "SMALLINT",
          "Short"),
      new Field("PRECISION", "PRECISION", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("LITERAL_PREFIX", "LITERAL_PREFIX", 10, Types.VARCHAR,
          "VARCHAR", "String"),
      new Field("LITERAL_SUFFIX", "LITERAL_SUFFIX", 10, Types.VARCHAR,
          "VARCHAR", "String"),
      new Field("CREATE_PARAMS", "CREATE_PARAMS", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("NULLABLE", "NULLABLE", 10, Types.INTEGER, "INTEGER", "Integer"),
      new Field("CASE_SENSITIVE", "CASE_SENSITIVE", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("SEARCHABLE", "SEARCHABLE", 10, Types.SMALLINT, "SMALLINT",
          "Short"),
      new Field("UNSIGNED_ATTRIBUTE", "UNSIGNED_ATTRIBUTE", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("FIXED_PREC_SCALE", "FIXED_PREC_SCALE", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("AUTO_INCREMENT", "AUTO_INCREMENT", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("LOCAL_TYPE_NAME", "LOCAL_TYPE_NAME", 10, Types.VARCHAR,
          "VARCHAR", "String"),
      new Field("MINIMUM_SCALE", "MINIMUM_SCALE", 10, Types.SMALLINT,
          "SMALLINT", "Short"),
      new Field("MAXIMUM_SCALE", "MAXIMUM_SCALE", 10, Types.SMALLINT,
          "SMALLINT", "Short"),
      new Field("SQL_DATA_TYPE", "SQL_DATA_TYPE", 10, Types.INTEGER, "INTEGER",
          "Integer"),
      new Field("SQL_DATETIME_SUB", "SQL_DATETIME_SUB", 10, Types.INTEGER,
          "INTEGER", "Integer"),
      new Field("NUM_PREC_RADIX", "NUM_PREC_RADIX", 10, Types.INTEGER,
          "INTEGER", "Integer")                                      };

  /**
   * @see java.sql.DatabaseMetaData#getUDTs(java.lang.String, java.lang.String,
   *      java.lang.String, int[])
   */
  private static Field[] getUDTsFields                               = new Field[]{
      new Field("TYPE_CAT", "TYPE_CAT", 9, Types.VARCHAR, "VARCHAR", "String"),
      new Field("TYPE_SCHEM", "TYPE_SCHEM", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("TYPE_NAME", "TYPE_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("CLASS_NAME", "CLASS_NAME", 10, Types.VARCHAR, "VARCHAR",
          "String"),
      new Field("DATA_TYPE", "DATA_TYPE", 10, Types.SMALLINT, "SMALLINT",
          "Short"),
      new Field("REMARKS", "REMARKS", 10, Types.VARCHAR, "VARCHAR", "String"),
      new Field("BASE_TYPE", "BASE_TYPE", 10, Types.SMALLINT, "SMALLINT",
          "Short")                                                   };

}