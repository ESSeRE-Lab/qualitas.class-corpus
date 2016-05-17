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

package org.objectweb.cjdbc.common.sql.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.backend.DatabaseBackendSchemaConstants;

/**
 * This class defines a DatabaseSQLMetaData. It is used to collect metadata from
 * a live connection to a database
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class DatabaseSQLMetaData
{
  Trace      logger;
  Connection connection;
  int        dynamicPrecision;
  boolean    gatherSystemTables;
  String     schemaPattern;

  /**
   * Creates a new <code>MetaData</code> object
   * 
   * @param logger the log4j logger to output to
   * @param connection a jdbc connection to a database
   * @param dynamicPrecision precision used to create the schema
   * @param gatherSystemTables should we gather system tables
   * @param schemaPattern schema pattern to look for (reduce the scope of
   *          gathering if not null)
   */
  public DatabaseSQLMetaData(Trace logger, Connection connection,
      int dynamicPrecision, boolean gatherSystemTables, String schemaPattern)
  {
    super();
    this.logger = logger;
    this.connection = connection;
    this.dynamicPrecision = dynamicPrecision;
    this.gatherSystemTables = gatherSystemTables;
    this.schemaPattern = schemaPattern;
  }

  /**
   * Create a database schema from the given connection
   * 
   * @return <code>DataSchema</code> contructed from the information collected
   *         through jdbc
   * @throws SQLException if an error occurs with the given connection
   */
  public final DatabaseSchema createDatabaseSchema() throws SQLException
  {
    ResultSet rs = null;

    connection.setAutoCommit(false); // Needed for Derby Get DatabaseMetaData
    DatabaseMetaData metaData = connection.getMetaData();
    if (metaData == null)
    {
      logger.warn(Translate.get("backend.meta.received.null"));
      return null;
    }

    DatabaseSchema databaseSchema = new DatabaseSchema();

    // Check if we should get system tables or not
    String[] types;
    if (gatherSystemTables)
    {
      schemaPattern = null;
      types = new String[]{"TABLE", "VIEW", "SYSTEM TABLE", "SYSTEM VIEW"};
    }
    else
      types = new String[]{"TABLE", "VIEW"};

    // Get tables meta data
    // getTables() gets a description of tables matching the catalog, schema,
    // table name pattern and type. Sending in null for catalog and schema
    // drops them from the selection criteria. The table name pattern "%"
    // means match any substring of 0 or more characters.
    // Last argument allows to obtain only database tables
    try
    {
      rs = metaData.getTables(null, schemaPattern, "%", types);
    }
    catch (Exception e)
    {
      // VIEWS cannot be retrieved with this backend
      logger.error(Translate.get("backend.meta.view.not.supported"), e);
      if (gatherSystemTables)
        types = new String[]{"TABLE", "SYSTEM TABLE",};
      else
        types = new String[]{"TABLE"};
      rs = metaData.getTables(null, schemaPattern, "%", types);
    }

    if (rs == null)
    {
      logger.warn(Translate.get("backend.meta.received.null"));
      connection.commit();
      return null;
    }

    String tableName;
    DatabaseTable table = null;
    while (rs.next())
    {
      // 1 is table catalog, 2 is table schema, 3 is table name, 4 is type
      tableName = rs.getString(3);
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("backend.meta.found.table", tableName));

      // Create a new table and add it to the database schema
      table = new DatabaseTable(tableName);
      databaseSchema.addTable(table);

      if (dynamicPrecision >= DatabaseBackendSchemaConstants.DynamicPrecisionColumn)
      {
        // Get information about this table columns
        getColumns(metaData, table);
        // Get information about this table primary keys
        getPrimaryKeys(metaData, table);
      }
    }

    // Get Procedures for this database
    if (dynamicPrecision >= DatabaseBackendSchemaConstants.DynamicPrecisionProcedures)
      getProcedures(metaData, databaseSchema);

    try
    {
      rs.close();
    }
    catch (Exception ignore)
    {
    }

    try
    {
      connection.commit();
    }
    catch (Exception ignore)
    {
      // This was a read-only transaction
    }

    try
    {
      // restore connection
      connection.setAutoCommit(true);
    }
    catch (SQLException e1)
    {
      // ignore, transaction is no more valid
    }

    return databaseSchema;
  }

  /**
   * @see java.sql.DatabaseMetaData#getProcedures
   * @see java.sql.DatabaseMetaData#getProcedureColumns
   */
  private void getProcedures(DatabaseMetaData metaData, DatabaseSchema schema)
  {
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.meta.get.procedures"));
    ResultSet rs = null;
    ResultSet rs2 = null;
    try
    {
      // Get Procedures meta data
      rs = metaData.getProcedures(null, null, "%");

      if (rs == null)
      {
        logger.warn(Translate.get("backend.meta.get.procedures.failed",
            metaData.getConnection().getCatalog()));
        return;
      }

      while (rs.next())
      {
        // Each row is a procedure description
        // 3 = PROCEDURE_NAME
        // 7 = REMARKS
        // 8 = PROCEDURE_TYPE
        DatabaseProcedure procedure = new DatabaseProcedure(rs.getString(3), rs
            .getString(7), rs.getShort(8));

        if (schema.getProcedure(procedure.getName()) != null)
        {
          if (logger.isDebugEnabled())
            logger.debug(Translate
                .get("backend.meta.procedure.already.in.schema", procedure
                    .getName()));
          continue;
        }
        else
        {
          if (logger.isDebugEnabled())
            logger.debug(Translate.get("backend.meta.found.procedure",
                procedure.getName()));
        }

        // TODO: Current limitation is that we don't distinguish 2 procedures
        // with the same name but different signatures

        if (dynamicPrecision < DatabaseBackendSchemaConstants.DynamicPrecisionProcedures)
          continue;
        // This is a new stored procedure, get the column information
        rs2 = metaData
            .getProcedureColumns(null, null, procedure.getName(), "%");
        if (rs2 == null)
          logger.warn(Translate.get("backend.meta.get.procedure.params.failed",
              procedure.getName()));
        else
        {
          while (rs2.next())
          {
            // Each row is a parameter description for the current procedure
            // 4 = COLUMN_NAME
            // 5 = COLUMN_TYPE
            // 6 = DATA_TYPE
            // 7 = TYPE_NAME
            // 8 = PRECISION
            // 9 = LENGTH
            // 10 = SCALE
            // 11 = RADIX
            // 12 = NULLABLE
            // 13 = REMARKS
            DatabaseProcedureParameter param = new DatabaseProcedureParameter(
                rs2.getString(4), rs2.getInt(5), rs2.getInt(6), rs2
                    .getString(7), rs2.getFloat(8), rs2.getInt(9), rs2
                    .getInt(10), rs2.getInt(11), rs2.getInt(12), rs2
                    .getString(13));
            // TO CHECK:
            // This cannot happen since we don't allow 2 procedures with the
            // same name.
            // It seems useless to test if a parameter already exist since it
            // should never happen.
            // We will need to reuse some pieces of this code when we will
            // implement support for
            // stored procedures of the same name with different signatures.
            // ArrayList parameters = procedure.getParameters();
            // for (int i = 0; i < parameters.size(); i++)
            // {
            // String name =
            // ((DatabaseProcedureParameter) parameters.get(i)).getName();
            // if (name.equals(param.getName()))
            // break;
            // }
            procedure.addParameter(param);
          }
          rs2.close();
        }

        schema.addProcedure(procedure);
      }
    }
    catch (Exception e)
    {
      logger.error(Translate.get("backend.meta.get.procedures.failed", e
          .getMessage()), e);
    }
    finally
    {
      try
      {
        rs.close();
      }
      catch (Exception ignore)
      {
      }
      try
      {
        rs2.close();
      }
      catch (Exception ignoreAsWell)
      {
      }
    }
  }

  /**
   * Gets the list of columns of a given database table. The caller must ensure
   * that the parameters are not <code>null</code>.
   * 
   * @param metaData the database meta data
   * @param table the database table
   * @exception SQLException if an error occurs
   */
  private void getColumns(DatabaseMetaData metaData, DatabaseTable table)
      throws SQLException
  {
    ResultSet rs = null;
    try
    {
      // Get columns meta data
      // getColumns() gets a description of columns matching the catalog,
      // schema, table name and column name pattern. Sending in null for
      // catalog and schema drops them from the selection criteria. The
      // column pattern "%" allows to obtain all columns.
      rs = metaData.getColumns(null, null, table.getName(), "%");

      if (rs == null)
      {
        logger.warn(Translate.get("backend.meta.get.columns.failed", table
            .getName()));
        return;
      }

      DatabaseColumn column = null;
      int type;
      while (rs.next())
      {
        // 1 is table catalog, 2 is table schema, 3 is table name,
        // 4 is column name, 5 is data type
        type = rs.getShort(5);
        column = new DatabaseColumn(rs.getString(4), false, type);
        table.addColumn(column);

        if (logger.isDebugEnabled())
          logger.debug(Translate.get("backend.meta.found.column", rs
              .getString(4)));
      }
    }
    catch (SQLException e)
    {
      throw new SQLException(Translate.get("backend.meta.get.columns.failed",
          table.getName()));
    }
    finally
    {
      try
      {
        rs.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

  /**
   * Gets the primary keys of a given database table. The caller must ensure
   * that the parameters are not <code>null</code>.
   * 
   * @param metaData the database meta data
   * @param table the database table
   * @exception SQLException if an error occurs
   */
  private void getPrimaryKeys(DatabaseMetaData metaData, DatabaseTable table)
      throws SQLException
  {
    ResultSet rs = null;
    try
    {
      // Get primary keys meta data
      // getPrimaryKeys() gets a description of primary keys matching the
      // catalog, schema, and table name. Sending in null for catalog and
      // schema drop them from the selection criteria.

      rs = metaData.getPrimaryKeys(null, null, table.getName());

      if (rs == null)
      {
        logger.warn(Translate.get("backend.meta.get.primary.keys.failed", table
            .getName()));
        return;
      }

      String columnName = null;
      while (rs.next())
      {

        // 1 is table catalog, 2 is table schema, 3 is table name, 4 is column
        // name
        columnName = rs.getString(4);
        if (columnName == null)
          continue;
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("backend.meta.found.primary.key",
              columnName));

        // Set the column to unique
        table.getColumn(columnName).setIsUnique(true);
      }
    }
    catch (SQLException e)
    {
      throw new SQLException(Translate.get(
          "backend.meta.get.primary.keys.failed", table.getName()));
    }
    finally
    {
      try
      {
        rs.close();
      }
      catch (Exception ignore)
      {
      }
    }
  }

}
