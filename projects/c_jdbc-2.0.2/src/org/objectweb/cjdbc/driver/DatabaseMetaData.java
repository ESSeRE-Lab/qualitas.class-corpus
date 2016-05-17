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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Jean-Bernard van Zuylen
 */

package org.objectweb.cjdbc.driver;

import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.HashMap;

import org.objectweb.cjdbc.common.sql.metadata.MetadataContainer;
import org.objectweb.cjdbc.common.sql.metadata.MetadataDescription;
import org.objectweb.cjdbc.common.util.Constants;

/**
 * DatabaseMetaData retrieves most of the values from the C-JDBC controller. If
 * you are using an heterogeneous cluster, the values returned are the one of
 * the first database that was enabled on the controller.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class DatabaseMetaData implements java.sql.DatabaseMetaData
{
  /** The connection association */
  private Connection connection;

  /**
   * ("getXXX(Y,Z,...)", value) hash table of the metadata queries.
   */
  private HashMap    metadataContainer;

  /**
   * Creates a new <code>DatabaseMetaData</code> instance.
   * 
   * @param conn a <code>Connection</code> value
   */
  public DatabaseMetaData(Connection conn)
  {
    this.connection = conn;
    metadataContainer = new HashMap();
  }

  /**
   * Lazy evaluation of a static metadata value. If the value is already in the
   * hash table, we return it. Else we send a network request to the controller
   * and add it to the table. <br>
   * 
   * @param methodName metadata method name
   * @param parametersType parameters type of method to invoke
   * @param arguments arguments to invoke the method
   * @param allowsNull true if a null metadata can be returned, if set to false
   *          an exception will be thrown if the metadata is null
   * @return the value returned by the given method
   * @throws SQLException if the connection fails
   */
  private Object getMetadata(String methodName, Class[] parametersType,
      Object[] arguments, boolean allowsNull) throws SQLException
  {
    String key = MetadataContainer.getContainerKey(methodName, parametersType,
        arguments);
    Object value = metadataContainer.get(key);

    if (value == null)
    { // Value not yet in container
      value = connection.getStaticMetadata(key);
      if ((value == null) && !allowsNull)
        throw new SQLException("Unable to retrieve metadata for " + key);
      metadataContainer.put(key, value);
    }
    return value;
  }

  /**
   * @see java.sql.DatabaseMetaData#allProceduresAreCallable()
   */
  public boolean allProceduresAreCallable() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.ALL_PROCEDURES_ARE_CALLABLE, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#allTablesAreSelectable()
   */
  public boolean allTablesAreSelectable() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.ALL_TABLES_ARE_SELECTABLE, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#dataDefinitionCausesTransactionCommit()
   */
  public boolean dataDefinitionCausesTransactionCommit() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.DATA_DEFINITION_CAUSES_TRANSACTION_COMMIT, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#dataDefinitionIgnoredInTransactions()
   */
  public boolean dataDefinitionIgnoredInTransactions() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.DATA_DEFINITION_IGNORED_IN_TRANSACTIONS, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#deletesAreDetected(int)
   */
  public boolean deletesAreDetected(int type) throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.DELETES_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(type)}, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#doesMaxRowSizeIncludeBlobs()
   */
  public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.DOES_MAX_ROW_SIZE_INCLUDE_BLOBS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getBestRowIdentifier(java.lang.String,
   *      java.lang.String, java.lang.String, int, boolean)
   */
  public java.sql.ResultSet getBestRowIdentifier(String catalog, String schema,
      String table, int scope, boolean nullable) throws SQLException
  {
    return connection.getBestRowIdentifier(catalog, schema, table, scope,
        nullable);
  }

  /**
   * @see java.sql.DatabaseMetaData#getCatalogs()
   */
  public java.sql.ResultSet getCatalogs() throws SQLException
  {
    return connection.getCatalogs();
  }

  /**
   * @see java.sql.DatabaseMetaData#getCatalogSeparator()
   */
  public String getCatalogSeparator() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_CATALOG_SEPARATOR,
        null, null, true));
  }

  /**
   * @see java.sql.DatabaseMetaData#getCatalogTerm()
   */
  public String getCatalogTerm() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_CATALOG_TERM, null,
        null, true));
  }

  /**
   * @param catalog a catalog name; "" retrieves those without a catalog
   * @param schemaPattern a schema name pattern; "" retrieves those without a
   *          schema
   * @param tableNamePattern a table name pattern
   * @param columnNamePattern a column name pattern
   * @return <code>null</code>
   * @exception SQLException if an error occurs
   * @see #getSearchStringEscape
   */
  public java.sql.ResultSet getColumns(String catalog, String schemaPattern,
      String tableNamePattern, String columnNamePattern) throws SQLException
  {
    return connection.getColumns(catalog, schemaPattern, tableNamePattern,
        columnNamePattern);
  }

  /**
   * @see java.sql.DatabaseMetaData#getColumnPrivileges(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getColumnPrivileges(String catalog,
      String schemaPattern, String tableNamePattern, String columnNamePattern)
      throws SQLException
  {
    return connection.getColumnPrivileges(catalog, schemaPattern,
        tableNamePattern, columnNamePattern);
  }

  /**
   * Retrieves the <code>Connection</code> that produced this
   * <code>DatabaseMetaData</code>.
   * 
   * @return the <code>Connection</code> object
   * @exception SQLException if an error occurs
   */
  public java.sql.Connection getConnection() throws SQLException
  {
    return connection;
  }

  /**
   * @see java.sql.DatabaseMetaData#getCrossReference(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getCrossReference(String primaryCatalog,
      String primarySchema, String primaryTable, String foreignCatalog,
      String foreignSchema, String foreignTable) throws SQLException
  {
    return connection.getCrossReference(primaryCatalog, primarySchema,
        primaryTable, foreignCatalog, foreignSchema, foreignTable);
  }

  /**
   * We return a comma separated list of database engine names connected to the
   * controller. A name appears only once regardless of the number of instances
   * of this particular db engine. If no database product name is provided by
   * the backend drivers, default is to return "C-JDBC Controller".
   * 
   * @return comma separated list of database product names
   * @exception SQLException if a database access error occurs
   */
  public String getDatabaseProductName() throws SQLException
  {
    return connection.getDatabaseProductName();
  }

  /**
   * What is the version of this database product.
   * 
   * @return the C-JDBC driver version
   * @exception SQLException if an error occurs
   */
  public String getDatabaseProductVersion() throws SQLException
  {
    return connection.getControllerVersionNumber();
  }

  /**
   * @see java.sql.DatabaseMetaData#getDefaultTransactionIsolation()
   */
  public int getDefaultTransactionIsolation() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_DEFAULT_TRANSACTION_ISOLATION, null, null,
        false)).intValue();
  }

  /**
   * What is this JDBC driver's major version number?
   * 
   * @return the JDBC driver major version
   */
  public int getDriverMajorVersion()
  {
    return Driver.MAJOR_VERSION;
  }

  /**
   * What is this JDBC driver's minor version number?
   * 
   * @return the JDBC driver minor version
   */
  public int getDriverMinorVersion()
  {
    return Driver.MINOR_VERSION;
  }

  /**
   * What is the name of this JDBC driver?
   * 
   * @return the JDBC driver name
   * @exception SQLException why?
   */
  public String getDriverName() throws SQLException
  {
    return "C-JDBC Generic Driver";
  }

  /**
   * What is the version string of this JDBC driver?
   * 
   * @return the JDBC driver name.
   * @exception SQLException why?
   */
  public String getDriverVersion() throws SQLException
  {
    return Constants.VERSION;
  }

  /**
   * @see java.sql.DatabaseMetaData#getExportedKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getExportedKeys(String catalog, String schema,
      String table) throws SQLException
  {
    return connection.getExportedKeys(catalog, schema, table);
  }

  /**
   * @see java.sql.DatabaseMetaData#getExtraNameCharacters()
   */
  public String getExtraNameCharacters() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_EXTRA_NAME_CHARACTERS,
        null, null, true));
  }

  /**
   * @see java.sql.DatabaseMetaData#getIdentifierQuoteString()
   */
  public String getIdentifierQuoteString() throws SQLException
  {
    return ((String) getMetadata(
        MetadataDescription.GET_IDENTIFIER_QUOTE_STRING, null, null, true));
  }

  /**
   * @see java.sql.DatabaseMetaData#getImportedKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getImportedKeys(String catalog, String schema,
      String table) throws SQLException
  {
    return connection.getImportedKeys(catalog, schema, table);
  }

  /**
   * @see java.sql.DatabaseMetaData#getIndexInfo(java.lang.String,
   *      java.lang.String, java.lang.String, boolean, boolean)
   */
  public java.sql.ResultSet getIndexInfo(String catalog, String schema,
      String table, boolean unique, boolean approximate) throws SQLException
  {
    return connection.getIndexInfo(catalog, schema, table, unique, approximate);
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxBinaryLiteralLength()
   */
  public int getMaxBinaryLiteralLength() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_BINARY_LITERAL_LENGTH, null, null, false))
        .intValue();

  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxCatalogNameLength()
   */
  public int getMaxCatalogNameLength() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_CATALOG_NAME_LENGTH, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxCharLiteralLength()
   */
  public int getMaxCharLiteralLength() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_CHAR_LITERAL_LENGTH, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnNameLength()
   */
  public int getMaxColumnNameLength() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_COLUMN_NAME_LENGTH, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInGroupBy()
   */
  public int getMaxColumnsInGroupBy() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_COLUMNS_IN_GROUP_BY, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInIndex()
   */
  public int getMaxColumnsInIndex() throws SQLException
  {
    return ((Integer) getMetadata(MetadataDescription.GET_MAX_COLUMNS_IN_INDEX,
        null, null, false)).intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInOrderBy()
   */
  public int getMaxColumnsInOrderBy() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_COLUMNS_IN_ORDER_BY, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInSelect()
   */
  public int getMaxColumnsInSelect() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_COLUMNS_IN_SELECT, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInTable()
   */
  public int getMaxColumnsInTable() throws SQLException
  {
    return ((Integer) getMetadata(MetadataDescription.GET_MAX_COLUMNS_IN_TABLE,
        null, null, false)).intValue();
  }

  /**
   * Maximum number of connections to the database (virtually no limit since
   * these are virtual connections to the Controller). The spec says 0 should be
   * returned if unknown.
   * 
   * @return value retrieved from first enabled backend
   * @exception SQLException if a database access error occurs
   */
  public int getMaxConnections() throws SQLException
  {
    // TODO: max connection should be the max number of connection to the
    // virtual database
    return ((Integer) getMetadata(MetadataDescription.GET_MAX_CONNECTIONS,
        null, null, false)).intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxCursorNameLength()
   */
  public int getMaxCursorNameLength() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_CURSOR_NAME_LENGTH, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxIndexLength()
   */
  public int getMaxIndexLength() throws SQLException
  {
    return ((Integer) getMetadata(MetadataDescription.GET_MAX_INDEX_LENGTH,
        null, null, false)).intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxProcedureNameLength()
   */
  public int getMaxProcedureNameLength() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_PROCEDURE_NAME_LENGTH, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxRowSize()
   */
  public int getMaxRowSize() throws SQLException
  {
    return ((Integer) getMetadata(MetadataDescription.GET_MAX_ROW_SIZE, null,
        null, false)).intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxSchemaNameLength()
   */
  public int getMaxSchemaNameLength() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_SCHEMA_NAME_LENGTH, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxStatementLength()
   */
  public int getMaxStatementLength() throws SQLException
  {
    return ((Integer) getMetadata(MetadataDescription.GET_MAX_STATEMENT_LENGTH,
        null, null, false)).intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxStatements()
   */
  public int getMaxStatements() throws SQLException
  {
    return ((Integer) getMetadata(MetadataDescription.GET_MAX_STATEMENTS, null,
        null, false)).intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxTableNameLength()
   */
  public int getMaxTableNameLength() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_MAX_TABLE_NAME_LENGTH, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxTablesInSelect()
   */
  public int getMaxTablesInSelect() throws SQLException
  {
    return ((Integer) getMetadata(MetadataDescription.GET_MAX_TABLES_IN_SELECT,
        null, null, false)).intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxUserNameLength()
   */
  public int getMaxUserNameLength() throws SQLException
  {
    return ((Integer) getMetadata(MetadataDescription.GET_MAX_USER_NAME_LENGTH,
        null, null, false)).intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getNumericFunctions()
   */
  public String getNumericFunctions() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_NUMERIC_FUNCTIONS,
        null, null, true));
  }

  /**
   * @see java.sql.DatabaseMetaData#getPrimaryKeys(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getPrimaryKeys(String catalog, String schema,
      String table) throws SQLException
  {
    return connection.getPrimaryKeys(catalog, schema, table);
  }

  /**
   * @see java.sql.DatabaseMetaData#getProcedureColumns(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getProcedureColumns(String catalog,
      String schemaPattern, String procedureNamePattern,
      String columnNamePattern) throws SQLException
  {
    return connection.getProcedureColumns(catalog, schemaPattern,
        procedureNamePattern, columnNamePattern);
  }

  /**
   * @see java.sql.DatabaseMetaData#getProcedures(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getProcedures(String catalog, String schemaPattern,
      String procedureNamePattern) throws SQLException
  {
    return connection.getProcedures(catalog, schemaPattern,
        procedureNamePattern);
  }

  /**
   * @see java.sql.DatabaseMetaData#getProcedureTerm()
   */
  public String getProcedureTerm() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_PROCEDURE_TERM, null,
        null, true));
  }

  /**
   * @see java.sql.DatabaseMetaData#getSchemas()
   */
  public java.sql.ResultSet getSchemas() throws SQLException
  {
    return connection.getSchemas();
  }

  /**
   * @see java.sql.DatabaseMetaData#getSchemaTerm()
   */
  public String getSchemaTerm() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_SCHEMA_TERM, null,
        null, true));
  }

  /**
   * @see java.sql.DatabaseMetaData#getSearchStringEscape()
   */
  public String getSearchStringEscape() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_SEARCH_STRING_ESCAPE,
        null, null, true));
  }

  /**
   * @see java.sql.DatabaseMetaData#getSQLKeywords()
   */
  public String getSQLKeywords() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_SQL_KEYWORDS, null,
        null, true));
  }

  /**
   * @see java.sql.DatabaseMetaData#getStringFunctions()
   */
  public String getStringFunctions() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_STRING_FUNCTIONS,
        null, null, true));
  }

  /**
   * @see java.sql.DatabaseMetaData#getSystemFunctions()
   */
  public String getSystemFunctions() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_SYSTEM_FUNCTIONS,
        null, null, true));
  }

  /**
   * Gets a description of the available tables.
   * 
   * @param catalog a catalog name; this is ignored, and should be set to
   *          <code>null</code>
   * @param schemaPattern a schema name pattern; this is ignored, and should be
   *          set to <code>null</code>
   * @param tableNamePattern a table name pattern. For all tables this should be
   *          "%"
   * @param types a list of table types to include; <code>null</code> returns
   *          all types
   * @return <code>null</code>
   * @exception SQLException if a database-access error occurs.
   */
  public java.sql.ResultSet getTables(String catalog, String schemaPattern,
      String tableNamePattern, String[] types) throws SQLException
  {
    return connection
        .getTables(catalog, schemaPattern, tableNamePattern, types);
  }

  /**
   * Gets a description of the access rights for each table available in a
   * catalog. Note that a table privilege applies to one or more columns in the
   * table. It would be wrong to assume that this priviledge applies to all
   * columns (this may be true for some systems but is not true for all.) Only
   * privileges matching the schema and table name criteria are returned. They
   * are ordered by TABLE_SCHEM, TABLE_NAME, and PRIVILEGE.
   * 
   * @param catalog a catalog name; "" retrieves those without a catalog; null
   *          means drop catalog name from the selection criteria
   * @param schemaPattern a schema name pattern; "" retrieves those without a
   *          schema
   * @param tableNamePattern a table name pattern
   * @return <code>ResultSet</code> each row is a table privilege description
   * @throws SQLException if a database access error occurs
   */
  public java.sql.ResultSet getTablePrivileges(String catalog,
      String schemaPattern, String tableNamePattern) throws SQLException
  {
    return connection.getTablePrivileges(catalog, schemaPattern,
        tableNamePattern);
  }

  /**
   * Gets the table types available in this database. The results are ordered by
   * table type.
   * 
   * @return <code>ResultSet</code> each row has a single String column that
   *         is a catalog name
   * @throws SQLException if a database error occurs
   */
  public java.sql.ResultSet getTableTypes() throws SQLException
  {
    return connection.getTableTypes();
  }

  /**
   * @see java.sql.DatabaseMetaData#getTimeDateFunctions()
   */
  public String getTimeDateFunctions() throws SQLException
  {
    return ((String) getMetadata(MetadataDescription.GET_TIME_DATE_FUNCTIONS,
        null, null, true));
  }

  /**
   * @see java.sql.DatabaseMetaData#getTypeInfo()
   */
  public java.sql.ResultSet getTypeInfo() throws SQLException
  {
    return connection.getTypeInfo();
  }

  /**
   * @see java.sql.DatabaseMetaData#getUDTs(java.lang.String, java.lang.String,
   *      java.lang.String, int[])
   */
  public java.sql.ResultSet getUDTs(String catalog, String schemaPattern,
      String typeNamePattern, int[] types) throws SQLException
  {
    return connection.getUDTs(catalog, schemaPattern, typeNamePattern, types);
  }

  /**
   * What is the URL for this database?
   * 
   * @return the url or null if it cannott be generated
   * @exception SQLException if a database access error occurs
   */
  public String getURL() throws SQLException
  {
    return connection.getUrl();
  }

  /**
   * What is our user name as known to the database?
   * 
   * @return our database user name
   * @exception SQLException if a database access error occurs
   */
  public String getUserName() throws SQLException
  {
    return connection.getUserName();
  }

  /**
   * @see java.sql.DatabaseMetaData#getVersionColumns(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getVersionColumns(String catalog, String schema,
      String table) throws SQLException
  {
    return connection.getVersionColumns(catalog, schema, table);
  }

  /**
   * @see java.sql.DatabaseMetaData#insertsAreDetected(int)
   */
  public boolean insertsAreDetected(int type) throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.INSERTS_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(type)}, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#isCatalogAtStart()
   */
  public boolean isCatalogAtStart() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.IS_CATALOG_AT_START,
        null, null, false)).booleanValue();
  }

  /**
   * Is the database in read-only mode?
   * 
   * @return <code>true</code> if so
   * @exception SQLException if a database access error occurs
   */
  public boolean isReadOnly() throws SQLException
  {
    return connection.isReadOnly();
  }

  /**
   * @see java.sql.DatabaseMetaData#nullPlusNonNullIsNull()
   */
  public boolean nullPlusNonNullIsNull() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.NULL_PLUS_NON_NULL_IS_NULL, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedAtEnd()
   */
  public boolean nullsAreSortedAtEnd() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.NULLS_ARE_SORTED_AT_END,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedAtStart()
   */
  public boolean nullsAreSortedAtStart() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.NULLS_ARE_SORTED_AT_START, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedHigh()
   */
  public boolean nullsAreSortedHigh() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.NULLS_ARE_SORTED_HIGH,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedLow()
   */
  public boolean nullsAreSortedLow() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.NULLS_ARE_SORTED_LOW,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#othersDeletesAreVisible(int)
   */
  public boolean othersDeletesAreVisible(int type) throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.OTHERS_DELETES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(type)}, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#othersInsertsAreVisible(int)
   */
  public boolean othersInsertsAreVisible(int type) throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.OTHERS_INSERTS_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(type)}, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#othersUpdatesAreVisible(int)
   */
  public boolean othersUpdatesAreVisible(int type) throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.OTHERS_UPDATES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(type)}, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#ownDeletesAreVisible(int)
   */
  public boolean ownDeletesAreVisible(int type) throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.OWN_DELETES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(type)}, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#ownInsertsAreVisible(int)
   */
  public boolean ownInsertsAreVisible(int type) throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.OWN_INSERTS_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(type)}, false))
        .booleanValue();

  }

  /**
   * @see java.sql.DatabaseMetaData#ownUpdatesAreVisible(int)
   */
  public boolean ownUpdatesAreVisible(int type) throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.OWN_UPDATES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(type)}, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()
   */
  public boolean storesLowerCaseIdentifiers() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.STORES_LOWER_CASE_IDENTIFIERS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()
   */
  public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.STORES_LOWER_CASE_QUOTED_IDENTIFIERS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#storesMixedCaseIdentifiers()
   */
  public boolean storesMixedCaseIdentifiers() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.STORES_MIXED_CASE_IDENTIFIERS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#storesMixedCaseQuotedIdentifiers()
   */
  public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.STORES_MIXED_CASE_QUOTED_IDENTIFIERS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()
   */
  public boolean storesUpperCaseIdentifiers() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.STORES_UPPER_CASE_IDENTIFIERS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()
   */
  public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.STORES_UPPER_CASE_QUOTED_IDENTIFIERS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsAlterTableWithAddColumn()
   */
  public boolean supportsAlterTableWithAddColumn() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_ALTER_TABLE_WITH_ADD_COLUMN, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsAlterTableWithDropColumn()
   */
  public boolean supportsAlterTableWithDropColumn() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_ALTER_TABLE_WITH_DROP_COLUMN, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsANSI92EntryLevelSQL()
   */
  public boolean supportsANSI92EntryLevelSQL() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_ANSI92_ENTRY_LEVEL_SQL, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsANSI92FullSQL()
   */
  public boolean supportsANSI92FullSQL() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_ANSI92_FULL_SQL,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsANSI92IntermediateSQL()
   */
  public boolean supportsANSI92IntermediateSQL() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_ANSI92_INTERMEDIATE_SQL, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsBatchUpdates()
   */
  public boolean supportsBatchUpdates() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_BATCH_UPDATES,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInDataManipulation()
   */
  public boolean supportsCatalogsInDataManipulation() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_CATALOGS_IN_DATA_MANIPULATION, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInIndexDefinitions()
   */
  public boolean supportsCatalogsInIndexDefinitions() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_CATALOGS_IN_INDEX_DEFINITIONS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInPrivilegeDefinitions()
   */
  public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_CATALOGS_IN_PRIVILEGE_DEFINITIONS, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInProcedureCalls()
   */
  public boolean supportsCatalogsInProcedureCalls() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_CATALOGS_IN_PROCEDURE_CALLS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInTableDefinitions()
   */
  public boolean supportsCatalogsInTableDefinitions() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_CATALOGS_IN_TABLE_DEFINITIONS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsColumnAliasing()
   */
  public boolean supportsColumnAliasing() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_COLUMN_ALIASING,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsConvert()
   */
  public boolean supportsConvert() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_CONVERT, null,
        null, false)).booleanValue();
  }

  /**
   * Not implemented (returns <code>false</code>). We should ask the
   * Controller to know.
   * 
   * @param fromType an <code>int</code> value
   * @param toType an <code>int</code> value
   * @return <code>false</code>
   * @exception SQLException <description>
   */
  public boolean supportsConvert(int fromType, int toType) throws SQLException
  {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCoreSQLGrammar()
   */
  public boolean supportsCoreSQLGrammar() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_CORE_SQL_GRAMMAR, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCorrelatedSubqueries()
   */
  public boolean supportsCorrelatedSubqueries() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_CORRELATED_SUBQUERIES, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsDataDefinitionAndDataManipulationTransactions()
   */
  public boolean supportsDataDefinitionAndDataManipulationTransactions()
      throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_DATA_DEFINITION_AND_DATA_MANIPULATION_TRANSACTIONS,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsDataManipulationTransactionsOnly()
   */
  public boolean supportsDataManipulationTransactionsOnly() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_DATA_MANIPULATION_TRANSACTIONS_ONLY, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsDifferentTableCorrelationNames()
   */
  public boolean supportsDifferentTableCorrelationNames() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_DIFFERENT_TABLE_CORRELATION_NAMES, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsExpressionsInOrderBy()
   */
  public boolean supportsExpressionsInOrderBy() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_EXPRESSIONS_IN_ORDER_BY, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsExtendedSQLGrammar()
   */
  public boolean supportsExtendedSQLGrammar() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_EXTENDED_SQL_GRAMMAR, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsFullOuterJoins()
   */
  public boolean supportsFullOuterJoins() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_FULL_OUTER_JOINS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsGroupBy()
   */
  public boolean supportsGroupBy() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_GROUP_BY, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsGroupByBeyondSelect()
   */
  public boolean supportsGroupByBeyondSelect() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_GROUP_BY_BEYOND_SELECT, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsGroupByUnrelated()
   */
  public boolean supportsGroupByUnrelated() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_GROUP_BY_UNRELATED, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsIntegrityEnhancementFacility()
   */
  public boolean supportsIntegrityEnhancementFacility() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_INTEGRITY_ENHANCEMENT_FACILITY, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsLikeEscapeClause()
   */
  public boolean supportsLikeEscapeClause() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_LIKE_ESCAPE_CLAUSE, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsLimitedOuterJoins()
   */
  public boolean supportsLimitedOuterJoins() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_LIMITED_OUTER_JOINS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMinimumSQLGrammar()
   */
  public boolean supportsMinimumSQLGrammar() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_MINIMUM_SQL_GRAMMAR, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMixedCaseIdentifiers()
   */
  public boolean supportsMixedCaseIdentifiers() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_MIXED_CASE_IDENTIFIERS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMixedCaseQuotedIdentifiers()
   */
  public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_MIXED_CASE_QUOTED_IDENTIFIERS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMultipleResultSets()
   */
  public boolean supportsMultipleResultSets() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_MULTIPLE_RESULTSETS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMultipleTransactions()
   */
  public boolean supportsMultipleTransactions() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_MULTIPLE_TRANSACTIONS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsNonNullableColumns()
   */
  public boolean supportsNonNullableColumns() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_NON_NULLABLE_COLUMNS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossCommit()
   */
  public boolean supportsOpenCursorsAcrossCommit() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_OPEN_CURSORS_ACROSS_COMMIT, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossRollback()
   */
  public boolean supportsOpenCursorsAcrossRollback() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_OPEN_CURSORS_ACROSS_ROLLBACK, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossCommit()
   */
  public boolean supportsOpenStatementsAcrossCommit() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_OPEN_STATEMENTS_ACROSS_COMMIT, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossRollback()
   */
  public boolean supportsOpenStatementsAcrossRollback() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_OPEN_STATEMENTS_ACROSS_ROLLBACK, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOrderByUnrelated()
   */
  public boolean supportsOrderByUnrelated() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_ORDER_BY_UNRELATED, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOuterJoins()
   */
  public boolean supportsOuterJoins() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_OUTER_JOINS,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsPositionedDelete()
   */
  public boolean supportsPositionedDelete() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_POSITIONED_DELETE, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsPositionedUpdate()
   */
  public boolean supportsPositionedUpdate() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_POSITIONED_UPDATE, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsResultSetConcurrency(int, int)
   */
  public boolean supportsResultSetConcurrency(int type, int concurrency)
      throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
            Integer.TYPE, Integer.TYPE}, new Object[]{new Integer(type),
            new Integer(concurrency)}, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsResultSetHoldability(int)
   */
  public boolean supportsResultSetHoldability(int holdability)
      throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_RESULT_SET_HOLDABILITY,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(holdability)},
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsResultSetType(int)
   */
  public boolean supportsResultSetType(int type) throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_RESULT_SET_TYPE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(type)}, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInDataManipulation()
   */
  public boolean supportsSchemasInDataManipulation() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_SCHEMAS_IN_DATA_MANIPULATION, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInIndexDefinitions()
   */
  public boolean supportsSchemasInIndexDefinitions() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_SCHEMAS_IN_INDEX_DEFINITIONS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInPrivilegeDefinitions()
   */
  public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_SCHEMAS_IN_PRIVILEGE_DEFINITIONS, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInProcedureCalls()
   */
  public boolean supportsSchemasInProcedureCalls() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_SCHEMAS_IN_PROCEDURE_CALLS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInTableDefinitions()
   */
  public boolean supportsSchemasInTableDefinitions() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_SCHEMAS_IN_TABLE_DEFINITIONS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSelectForUpdate()
   */
  public boolean supportsSelectForUpdate() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_SELECT_FOR_UPDATE, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsStoredProcedures()
   */
  public boolean supportsStoredProcedures() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_STORED_PROCEDURES, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInComparisons()
   */
  public boolean supportsSubqueriesInComparisons() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_SUB_QUERIES_IN_COMPARISONS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInExists()
   */
  public boolean supportsSubqueriesInExists() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_SUB_QUERIES_IN_EXISTS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInIns()
   */
  public boolean supportsSubqueriesInIns() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_SUB_QUERIES_IN_INS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInQuantifieds()
   */
  public boolean supportsSubqueriesInQuantifieds() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_SUB_QUERIES_IN_QUANTIFIEDS, null, null,
        false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsTableCorrelationNames()
   */
  public boolean supportsTableCorrelationNames() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_TABLE_CORRELATION_NAMES, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsTransactionIsolationLevel(int)
   */
  public boolean supportsTransactionIsolationLevel(int level)
      throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_TRANSACTION_ISOLATION_LEVEL,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(level)}, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsTransactions()
   */
  public boolean supportsTransactions() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_TRANSACTIONS,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsUnion()
   */
  public boolean supportsUnion() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_UNION, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsUnionAll()
   */
  public boolean supportsUnionAll() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_UNION_ALL, null,
        null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#updatesAreDetected(int)
   */
  public boolean updatesAreDetected(int type) throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.UPDATES_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(type)}, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#usesLocalFilePerTable()
   */
  public boolean usesLocalFilePerTable() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.USES_LOCAL_FILE_PER_TABLE, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#usesLocalFiles()
   */
  public boolean usesLocalFiles() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.USES_LOCAL_FILES, null,
        null, false)).booleanValue();
  }

  // ------------------- JDBC 3.0 -------------------------

  /**
   * @see java.sql.DatabaseMetaData#supportsSavepoints()
   */
  public boolean supportsSavepoints() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.SUPPORTS_SAVEPOINTS,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsNamedParameters()
   */
  public boolean supportsNamedParameters() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_NAMED_PARAMETERS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMultipleOpenResults()
   */
  public boolean supportsMultipleOpenResults() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_MULTIPLE_OPEN_RESULTS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
   */
  public boolean supportsGetGeneratedKeys() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_GET_GENERATED_KEYS, null, null, false))
        .booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getSuperTypes(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getSuperTypes(String catalog, String schemaPattern,
      String typeNamePattern) throws SQLException
  {
    return connection.getSuperTypes(catalog, schemaPattern, typeNamePattern);
  }

  /**
   * @see java.sql.DatabaseMetaData#getSuperTables(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getSuperTables(String catalog,
      String schemaPattern, String tableNamePattern) throws SQLException
  {
    return connection.getSuperTables(catalog, schemaPattern, tableNamePattern);
  }

  /**
   * @see java.sql.DatabaseMetaData#getAttributes(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  public java.sql.ResultSet getAttributes(String catalog, String schemaPattern,
      String typeNamePattern, String attributeNamePattern) throws SQLException
  {
    return connection.getAttributes(catalog, schemaPattern, typeNamePattern,
        attributeNamePattern);
  }

  /**
   * @see java.sql.DatabaseMetaData#getResultSetHoldability()
   */
  public int getResultSetHoldability() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_RESULTSET_HOLDABILITY, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getDatabaseMajorVersion()
   */
  public int getDatabaseMajorVersion() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_DATABASE_MAJOR_VERSION, null, null, false))
        .intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#getDatabaseMinorVersion()
   */
  public int getDatabaseMinorVersion() throws SQLException
  {
    return ((Integer) getMetadata(
        MetadataDescription.GET_DATABASE_MINOR_VERSION, null, null, false))
        .intValue();
  }

  /**
   * Retrieves the major JDBC version number for this driver.
   * 
   * @return JDBC version major number
   * @exception SQLException if a database access error occurs
   * @since JDK 1.4
   */
  public int getJDBCMajorVersion() throws SQLException
  {
    return Driver.MAJOR_VERSION;
  }

  /**
   * Retrieves the minor JDBC version number for this driver.
   * 
   * @return JDBC version minor number
   * @exception SQLException if a database access error occurs
   * @since JDK 1.4
   */
  public int getJDBCMinorVersion() throws SQLException
  {
    return Driver.MINOR_VERSION;
  }

  /**
   * @see java.sql.DatabaseMetaData#getSQLStateType()
   */
  public int getSQLStateType() throws SQLException
  {
    return ((Integer) getMetadata(MetadataDescription.GET_SQL_STATE_TYPE, null,
        null, false)).intValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#locatorsUpdateCopy()
   */
  public boolean locatorsUpdateCopy() throws SQLException
  {
    return ((Boolean) getMetadata(MetadataDescription.LOCATORS_UPDATE_COPY,
        null, null, false)).booleanValue();
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsStatementPooling()
   */
  public boolean supportsStatementPooling() throws SQLException
  {
    return ((Boolean) getMetadata(
        MetadataDescription.SUPPORTS_STATEMENT_POOLING, null, null, false))
        .booleanValue();
  }

@Override
public <T> T unwrap(Class<T> iface) throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public boolean isWrapperFor(Class<?> iface) throws SQLException {
	// TODO Auto-generated method stub
	return false;
}

@Override
public RowIdLifetime getRowIdLifetime() throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public ResultSet getSchemas(String catalog, String schemaPattern)
		throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
	// TODO Auto-generated method stub
	return false;
}

@Override
public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
	// TODO Auto-generated method stub
	return false;
}

@Override
public ResultSet getClientInfoProperties() throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public ResultSet getFunctions(String catalog, String schemaPattern,
		String functionNamePattern) throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public ResultSet getFunctionColumns(String catalog, String schemaPattern,
		String functionNamePattern, String columnNamePattern)
		throws SQLException {
	// TODO Auto-generated method stub
	return null;
}
}