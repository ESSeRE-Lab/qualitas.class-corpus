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
 * Contributor(s): Emmanuel Cecchet, Nicolas Modrzyk, Mathieu Peltier, Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.controller.backend;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.metadata.MetadataContainer;
import org.objectweb.cjdbc.common.sql.metadata.MetadataDescription;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSQLMetaData;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.controller.connection.AbstractConnectionManager;

/**
 * A <code>DatabaseBackendMetaData</code> is used to retrieve the database
 * schema of a real database backend that will have to be bound to a virtual
 * C-JDBC database.
 * 
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public final class DatabaseBackendMetaData
{
  /** Connection manager to get a connection from. */
  private AbstractConnectionManager connectionManager;

  /** Logger instance. */
  private Trace                     logger;

  /** Schema of the database backend. */
  private DatabaseSchema            databaseSchema;

  /** The precision of the dynamically generated schema */
  private int                       dynamicPrecision;

  /** Should the system tables be gathered or not */
  private boolean                   gatherSystemTables = false;

  /** The name of the schema used to gather meta data */
  private String                    schemaName         = null;

  /** Container for static metadata */
  private MetadataContainer         metadataContainer  = null;

  /**
   * Creates a new <code>DatabaseBackendMetaData</code> instance. This class
   * takes care of initializing the connection manager if needed but the driver
   * must have been previously loaded else the connection manager's
   * initialization will fail.
   * 
   * @param connectionManager the connection manager to gather the schema from
   * @param logger the logger (usually the backend logger) to use
   * @param dynamicPrecision the precision with which we gather a schema
   *          directly from the backend
   * @param gatherSystemTables true if system tables must be gathered
   * @param schemaName schema name to use to retrieve information
   */
  public DatabaseBackendMetaData(AbstractConnectionManager connectionManager,
      Trace logger, int dynamicPrecision, boolean gatherSystemTables,
      String schemaName)
  {
    this.connectionManager = connectionManager;
    this.dynamicPrecision = dynamicPrecision;
    this.logger = logger;
    this.gatherSystemTables = gatherSystemTables;
    this.schemaName = schemaName;
  }

  /**
   * This method invokes by reflection the appropriate metadata method and fills
   * the container with the result.
   * 
   * @param metaData metadata to invoke
   * @param methodName method to invoke
   * @param parametersType parameters type of method to invoke
   * @param arguments arguments to invoke the method
   */
  private void insertMetadataInContainer(DatabaseMetaData metaData,
      String methodName, Class[] parametersType, Object[] arguments)
  {
    Class metadataClass = metaData.getClass();
    try
    {
      Method method = metadataClass.getMethod(methodName, parametersType);
      Object result = method.invoke(metaData, arguments);
      updateContainerInformation(methodName, parametersType, arguments, result);
    }
    catch (SecurityException e)
    {
      if (logger.isDebugEnabled())
        logger.debug("Problem calling " + methodName, e);
      else
        logger.warn("Unable to get information for " + methodName);
    }
    catch (IllegalArgumentException e)
    {
      if (logger.isDebugEnabled())
        logger.debug("Problem calling " + methodName, e);
      else
        logger.warn("Unable to get information for " + methodName);
    }
    catch (NoSuchMethodException e)
    {
      if (logger.isDebugEnabled())
        logger.debug("Metadata " + methodName
            + " does not exists. Probably not supported by your JDK.");
    }
    catch (IllegalAccessException e)
    {
      if (logger.isDebugEnabled())
        logger.debug("Problem calling " + methodName, e);
      else
        logger.warn("Unable to get information for " + methodName);
    }
    catch (InvocationTargetException e)
    {
      if (logger.isDebugEnabled())
        logger.debug("Problem calling " + methodName, e);
      else
        logger.warn("Unable to get information for " + methodName);
    }
    catch (AbstractMethodError e)
    {
      // user Wolfgang Koppenberger reported problems with code compiled with
      // java 1.3, it throws AbstractMethodError instead of
      // NoSuchMethodException
      if (logger.isDebugEnabled())
        logger.debug("Metadata " + methodName
            + " does not exists. Probably not supported by your jdbc-driver.");
    }
  }

  /**
   * Update the metadata container information for the given method.
   * 
   * @param methodName method invoked to generate the key in the container
   * @param parametersType parameters type of invoked method
   * @param arguments arguments used to invoke the method
   * @param result result of the method call
   */
  private void updateContainerInformation(String methodName,
      Class[] parametersType, Object[] arguments, Object result)
  {
    String key = MetadataContainer.getContainerKey(methodName, parametersType,
        arguments);
    metadataContainer.put(key, result);
    if (logger.isDebugEnabled())
      logger.debug("Updating metadata " + key + "=" + result);
  }

  /**
   * Retrieve metadata from a connection on this backend and build the
   * ("getDataXXX(Y,Z,...)", value) hash table. Note that some values are
   * overriden with C-JDBC specific features (add-ons or limitations).
   * 
   * @return a metadata container including all metadata values
   * @throws SQLException if an error occurs
   */
  public MetadataContainer retrieveDatabaseMetadata() throws SQLException
  {
    if (metadataContainer != null)
      return metadataContainer;

    // Retrieve metadata
    Connection connection = null;
    boolean wasInitialized = connectionManager.isInitialized();

    DatabaseMetaData metaData;
    try
    {
      if (!wasInitialized)
        connectionManager.initializeConnections();

      connection = connectionManager.getConnection();
      if (connection == null)
      {
        String msg = Translate.get("backend.meta.connection.failed");
        logger.error(msg);
        throw new SQLException(msg);
      }

      metaData = connection.getMetaData();
      metadataContainer = new MetadataContainer(connection.getMetaData()
          .getURL());

      // TODO: Move this to a separate command
      // Boolean.valueOf(metaData.supportsConvert(int,int)

      // Metadata in alphabetical order.
      // These calls do not seem to throw anything, but require a valid
      // connection (which is closed in the finally block).
      insertMetadataInContainer(metaData,
          MetadataDescription.ALL_PROCEDURES_ARE_CALLABLE, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.ALL_TABLES_ARE_SELECTABLE, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.DATA_DEFINITION_CAUSES_TRANSACTION_COMMIT, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.DATA_DEFINITION_IGNORED_IN_TRANSACTIONS, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.DELETES_ARE_DETECTED, new Class[]{Integer.TYPE},
          new Object[]{new Integer(ResultSet.TYPE_FORWARD_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.DELETES_ARE_DETECTED, new Class[]{Integer.TYPE},
          new Object[]{new Integer(ResultSet.TYPE_SCROLL_INSENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.DELETES_ARE_DETECTED, new Class[]{Integer.TYPE},
          new Object[]{new Integer(ResultSet.TYPE_SCROLL_SENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.DOES_MAX_ROW_SIZE_INCLUDE_BLOBS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_DEFAULT_TRANSACTION_ISOLATION, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_DATABASE_MAJOR_VERSION, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_DATABASE_MINOR_VERSION, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_DRIVER_MAJOR_VERSION, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_DRIVER_MINOR_VERSION, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_JDBC_MAJOR_VERSION, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_JDBC_MINOR_VERSION, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_BINARY_LITERAL_LENGTH, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_RESULTSET_HOLDABILITY, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_SQL_STATE_TYPE, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_CATALOG_NAME_LENGTH, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_CHAR_LITERAL_LENGTH, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_COLUMN_NAME_LENGTH, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_COLUMNS_IN_GROUP_BY, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_COLUMNS_IN_INDEX, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_COLUMNS_IN_ORDER_BY, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_COLUMNS_IN_SELECT, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_COLUMNS_IN_TABLE, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_CONNECTIONS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_CURSOR_NAME_LENGTH, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_INDEX_LENGTH, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_PROCEDURE_NAME_LENGTH, null, null);
      insertMetadataInContainer(metaData, MetadataDescription.GET_MAX_ROW_SIZE,
          null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_SCHEMA_NAME_LENGTH, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_STATEMENT_LENGTH, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_STATEMENTS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_TABLE_NAME_LENGTH, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_TABLES_IN_SELECT, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_MAX_USER_NAME_LENGTH, null, null);
      // strings
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_CATALOG_SEPARATOR, null, null);
      insertMetadataInContainer(metaData, MetadataDescription.GET_CATALOG_TERM,
          null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_DATABASE_PRODUCT_NAME, null, null);
      insertMetadataInContainer(metaData, MetadataDescription.GET_DRIVER_NAME,
          null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_DRIVER_VERSION, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_EXTRA_NAME_CHARACTERS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_IDENTIFIER_QUOTE_STRING, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_NUMERIC_FUNCTIONS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_PROCEDURE_TERM, null, null);
      insertMetadataInContainer(metaData, MetadataDescription.GET_SCHEMA_TERM,
          null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_SEARCH_STRING_ESCAPE, null, null);
      insertMetadataInContainer(metaData, MetadataDescription.GET_SQL_KEYWORDS,
          null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_STRING_FUNCTIONS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_SYSTEM_FUNCTIONS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.GET_TIME_DATE_FUNCTIONS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.INSERTS_ARE_DETECTED, new Class[]{Integer.TYPE},
          new Object[]{new Integer(ResultSet.TYPE_FORWARD_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.INSERTS_ARE_DETECTED, new Class[]{Integer.TYPE},
          new Object[]{new Integer(ResultSet.TYPE_SCROLL_INSENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.INSERTS_ARE_DETECTED, new Class[]{Integer.TYPE},
          new Object[]{new Integer(ResultSet.TYPE_SCROLL_SENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.LOCATORS_UPDATE_COPY, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.NULL_PLUS_NON_NULL_IS_NULL, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.NULLS_ARE_SORTED_AT_END, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.NULLS_ARE_SORTED_AT_START, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.NULLS_ARE_SORTED_HIGH, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.NULLS_ARE_SORTED_LOW, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.OTHERS_DELETES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_FORWARD_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OTHERS_DELETES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_INSENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OTHERS_DELETES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_SENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OTHERS_INSERTS_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_FORWARD_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OTHERS_INSERTS_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_INSENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OTHERS_INSERTS_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_SENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OTHERS_UPDATES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_FORWARD_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OTHERS_UPDATES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_INSENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OTHERS_UPDATES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_SENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OWN_DELETES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_FORWARD_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OWN_DELETES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_INSENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OWN_DELETES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_SENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OWN_INSERTS_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_FORWARD_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OWN_INSERTS_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_INSENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OWN_INSERTS_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_SENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OWN_UPDATES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_FORWARD_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OWN_UPDATES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_INSENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.OWN_UPDATES_ARE_VISIBLE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_SENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.STORES_LOWER_CASE_IDENTIFIERS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.STORES_LOWER_CASE_QUOTED_IDENTIFIERS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.STORES_MIXED_CASE_IDENTIFIERS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.STORES_MIXED_CASE_QUOTED_IDENTIFIERS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.STORES_UPPER_CASE_IDENTIFIERS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.STORES_UPPER_CASE_QUOTED_IDENTIFIERS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_ALTER_TABLE_WITH_ADD_COLUMN, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_ALTER_TABLE_WITH_DROP_COLUMN, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_ANSI92_ENTRY_LEVEL_SQL, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_ANSI92_FULL_SQL, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_ANSI92_INTERMEDIATE_SQL, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_BATCH_UPDATES, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_CATALOGS_IN_DATA_MANIPULATION, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_CATALOGS_IN_INDEX_DEFINITIONS, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_CATALOGS_IN_PRIVILEGE_DEFINITIONS, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_CATALOGS_IN_PROCEDURE_CALLS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_CATALOGS_IN_TABLE_DEFINITIONS, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_COLUMN_ALIASING, null, null);
      insertMetadataInContainer(metaData, MetadataDescription.SUPPORTS_CONVERT,
          null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_CORE_SQL_GRAMMAR, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_CORRELATED_SUBQUERIES, null, null);
      insertMetadataInContainer(
          metaData,
          MetadataDescription.SUPPORTS_DATA_DEFINITION_AND_DATA_MANIPULATION_TRANSACTIONS,
          null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_DATA_MANIPULATION_TRANSACTIONS_ONLY,
          null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_DIFFERENT_TABLE_CORRELATION_NAMES, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_EXPRESSIONS_IN_ORDER_BY, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_EXTENDED_SQL_GRAMMAR, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_FULL_OUTER_JOINS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_GET_GENERATED_KEYS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_GROUP_BY, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_GROUP_BY_BEYOND_SELECT, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_GROUP_BY_UNRELATED, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_INTEGRITY_ENHANCEMENT_FACILITY, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_LIKE_ESCAPE_CLAUSE, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_LIMITED_OUTER_JOINS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_MINIMUM_SQL_GRAMMAR, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_MIXED_CASE_IDENTIFIERS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_MIXED_CASE_QUOTED_IDENTIFIERS, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_MULTIPLE_OPEN_RESULTS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_MULTIPLE_RESULTSETS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_MULTIPLE_TRANSACTIONS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_NAMED_PARAMETERS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_NON_NULLABLE_COLUMNS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_OPEN_CURSORS_ACROSS_COMMIT, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_OPEN_CURSORS_ACROSS_ROLLBACK, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_OPEN_STATEMENTS_ACROSS_COMMIT, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_OPEN_STATEMENTS_ACROSS_ROLLBACK, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_ORDER_BY_UNRELATED, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_OUTER_JOINS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_POSITIONED_DELETE, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_POSITIONED_UPDATE, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
              Integer.TYPE, Integer.TYPE}, new Object[]{
              new Integer(ResultSet.TYPE_FORWARD_ONLY),
              new Integer(ResultSet.CONCUR_READ_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
              Integer.TYPE, Integer.TYPE}, new Object[]{
              new Integer(ResultSet.TYPE_FORWARD_ONLY),
              new Integer(ResultSet.CONCUR_UPDATABLE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
              Integer.TYPE, Integer.TYPE}, new Object[]{
              new Integer(ResultSet.TYPE_SCROLL_INSENSITIVE),
              new Integer(ResultSet.CONCUR_READ_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
              Integer.TYPE, Integer.TYPE}, new Object[]{
              new Integer(ResultSet.TYPE_SCROLL_INSENSITIVE),
              new Integer(ResultSet.CONCUR_UPDATABLE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
              Integer.TYPE, Integer.TYPE}, new Object[]{
              new Integer(ResultSet.TYPE_SCROLL_SENSITIVE),
              new Integer(ResultSet.CONCUR_READ_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
              Integer.TYPE, Integer.TYPE}, new Object[]{
              new Integer(ResultSet.TYPE_SCROLL_SENSITIVE),
              new Integer(ResultSet.CONCUR_UPDATABLE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_HOLDABILITY,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.HOLD_CURSORS_OVER_COMMIT)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_HOLDABILITY,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.CLOSE_CURSORS_AT_COMMIT)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_TYPE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_FORWARD_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_TYPE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_INSENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_RESULT_SET_TYPE,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              ResultSet.TYPE_SCROLL_SENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SAVEPOINTS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SCHEMAS_IN_DATA_MANIPULATION, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SCHEMAS_IN_INDEX_DEFINITIONS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SCHEMAS_IN_PRIVILEGE_DEFINITIONS, null,
          null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SCHEMAS_IN_PROCEDURE_CALLS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SCHEMAS_IN_TABLE_DEFINITIONS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SELECT_FOR_UPDATE, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_STATEMENT_POOLING, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_STORED_PROCEDURES, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SUB_QUERIES_IN_COMPARISONS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SUB_QUERIES_IN_EXISTS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SUB_QUERIES_IN_INS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_SUB_QUERIES_IN_QUANTIFIEDS, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_TABLE_CORRELATION_NAMES, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_TRANSACTION_ISOLATION_LEVEL,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              Connection.TRANSACTION_NONE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_TRANSACTION_ISOLATION_LEVEL,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              Connection.TRANSACTION_READ_COMMITTED)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_TRANSACTION_ISOLATION_LEVEL,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              Connection.TRANSACTION_READ_UNCOMMITTED)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_TRANSACTION_ISOLATION_LEVEL,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              Connection.TRANSACTION_REPEATABLE_READ)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_TRANSACTION_ISOLATION_LEVEL,
          new Class[]{Integer.TYPE}, new Object[]{new Integer(
              Connection.TRANSACTION_SERIALIZABLE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_TRANSACTIONS, null, null);
      insertMetadataInContainer(metaData, MetadataDescription.SUPPORTS_UNION,
          null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.SUPPORTS_UNION_ALL, null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.UPDATES_ARE_DETECTED, new Class[]{Integer.TYPE},
          new Object[]{new Integer(ResultSet.TYPE_FORWARD_ONLY)});
      insertMetadataInContainer(metaData,
          MetadataDescription.UPDATES_ARE_DETECTED, new Class[]{Integer.TYPE},
          new Object[]{new Integer(ResultSet.TYPE_SCROLL_INSENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.UPDATES_ARE_DETECTED, new Class[]{Integer.TYPE},
          new Object[]{new Integer(ResultSet.TYPE_SCROLL_SENSITIVE)});
      insertMetadataInContainer(metaData,
          MetadataDescription.USES_LOCAL_FILE_PER_TABLE, null, null);
      insertMetadataInContainer(metaData, MetadataDescription.USES_LOCAL_FILES,
          null, null);
      insertMetadataInContainer(metaData,
          MetadataDescription.IS_CATALOG_AT_START, null, null);

      overrideCJDBCSpecificFeatures();
    }
    catch (Exception e)
    {
      if (e instanceof RuntimeException)
        logger.error(Translate.get("backend.meta.runtime.error"), e);
      throw new SQLException(Translate.get("backend.meta.failed.get.info", e));
    }

    finally
    {
      // IGOR's fix: do that after we are finished with all DB accesses
      if (connection != null)
        connectionManager.releaseConnection(connection);

      if (!wasInitialized)
        connectionManager.finalizeConnections();
    }
    return metadataContainer;
  }

  /**
   * This method overrides metdata container information with C-JDBC specific
   * limitations.
   */
  private void overrideCJDBCSpecificFeatures()
  {
    // C-JDBC ResultSets are disconnected therefore they cannot detect deletes,
    // inserts or udpates.
    updateContainerInformation(MetadataDescription.DELETES_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_INSENSITIVE)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.DELETES_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_SENSITIVE)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.INSERTS_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_FORWARD_ONLY)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.INSERTS_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_INSENSITIVE)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.INSERTS_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_SENSITIVE)}, Boolean.FALSE);

    // we don't implement Blob references (= LOCATORs), the driver
    // delivers only copies that the application has to put back by itself.
    updateContainerInformation(MetadataDescription.LOCATORS_UPDATE_COPY, null,
        null, Boolean.TRUE);

    // C-JDBC ResultSets are disconnected therefore they cannot detect deletes,
    // inserts or udpates.
    updateContainerInformation(MetadataDescription.OTHERS_DELETES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_FORWARD_ONLY)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.OTHERS_DELETES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_INSENSITIVE)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.OTHERS_DELETES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_SENSITIVE)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.OTHERS_INSERTS_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_FORWARD_ONLY)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.OTHERS_INSERTS_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_INSENSITIVE)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.OTHERS_INSERTS_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_SENSITIVE)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.OTHERS_UPDATES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_FORWARD_ONLY)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.OTHERS_UPDATES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_INSENSITIVE)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.OTHERS_UPDATES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_SENSITIVE)}, Boolean.FALSE);

    // C-JDBC ResultSets are updated when making changes to the database using
    // updatable ResultSets
    updateContainerInformation(MetadataDescription.OWN_DELETES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_FORWARD_ONLY)}, Boolean.TRUE);
    updateContainerInformation(MetadataDescription.OWN_DELETES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_INSENSITIVE)}, Boolean.TRUE);
    updateContainerInformation(MetadataDescription.OWN_DELETES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_SENSITIVE)}, Boolean.TRUE);
    updateContainerInformation(MetadataDescription.OWN_INSERTS_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_FORWARD_ONLY)}, Boolean.TRUE);
    updateContainerInformation(MetadataDescription.OWN_INSERTS_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_INSENSITIVE)}, Boolean.TRUE);
    updateContainerInformation(MetadataDescription.OWN_INSERTS_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_SENSITIVE)}, Boolean.TRUE);
    updateContainerInformation(MetadataDescription.OWN_UPDATES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_FORWARD_ONLY)}, Boolean.TRUE);
    updateContainerInformation(MetadataDescription.OWN_UPDATES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_INSENSITIVE)}, Boolean.TRUE);
    updateContainerInformation(MetadataDescription.OWN_UPDATES_ARE_VISIBLE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_SENSITIVE)}, Boolean.TRUE);

    // We emulate batch updates even if they are not handled optimally yet
    updateContainerInformation(MetadataDescription.SUPPORTS_BATCH_UPDATES,
        null, null, Boolean.TRUE);

    // It is currently not possible for a CallableStatement to return multiple
    // ResultSets
    updateContainerInformation(
        MetadataDescription.SUPPORTS_MULTIPLE_OPEN_RESULTS, null, null,
        Boolean.FALSE);

    // A single call to execute can only return one ResultSet. It this suppose
    // to be retrived after an executeBatch?
    updateContainerInformation(
        MetadataDescription.SUPPORTS_MULTIPLE_RESULTSETS, null, null,
        Boolean.FALSE);

    // We support open cursors and statements across commit/rollback. Note this
    // only applies for the driver side since everything is closed right away on
    // the controller side.
    updateContainerInformation(
        MetadataDescription.SUPPORTS_OPEN_CURSORS_ACROSS_COMMIT, null, null,
        Boolean.TRUE);
    updateContainerInformation(
        MetadataDescription.SUPPORTS_OPEN_CURSORS_ACROSS_ROLLBACK, null, null,
        Boolean.TRUE);
    updateContainerInformation(
        MetadataDescription.SUPPORTS_OPEN_STATEMENTS_ACROSS_COMMIT, null, null,
        Boolean.TRUE);
    updateContainerInformation(
        MetadataDescription.SUPPORTS_OPEN_STATEMENTS_ACROSS_ROLLBACK, null,
        null, Boolean.TRUE);

    // We do not close ResultSets at commit time
    updateContainerInformation(
        MetadataDescription.SUPPORTS_RESULT_SET_HOLDABILITY,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.HOLD_CURSORS_OVER_COMMIT)}, Boolean.TRUE);
    updateContainerInformation(
        MetadataDescription.SUPPORTS_RESULT_SET_HOLDABILITY,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.CLOSE_CURSORS_AT_COMMIT)}, Boolean.FALSE);

    // We do support CONCUR_READ_ONLY and CONCUR_UPDATABLE concurrency modes
    // for all supported ResultSet types
    updateContainerInformation(
        MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
            Integer.TYPE, Integer.TYPE}, new Object[]{
            new Integer(ResultSet.TYPE_FORWARD_ONLY),
            new Integer(ResultSet.CONCUR_READ_ONLY)}, Boolean.TRUE);
    updateContainerInformation(
        MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
            Integer.TYPE, Integer.TYPE}, new Object[]{
            new Integer(ResultSet.TYPE_FORWARD_ONLY),
            new Integer(ResultSet.CONCUR_UPDATABLE)}, Boolean.TRUE);
    updateContainerInformation(
        MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
            Integer.TYPE, Integer.TYPE}, new Object[]{
            new Integer(ResultSet.TYPE_SCROLL_INSENSITIVE),
            new Integer(ResultSet.CONCUR_READ_ONLY)}, Boolean.TRUE);
    updateContainerInformation(
        MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
            Integer.TYPE, Integer.TYPE}, new Object[]{
            new Integer(ResultSet.TYPE_SCROLL_INSENSITIVE),
            new Integer(ResultSet.CONCUR_UPDATABLE)}, Boolean.TRUE);
    updateContainerInformation(
        MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
            Integer.TYPE, Integer.TYPE}, new Object[]{
            new Integer(ResultSet.TYPE_SCROLL_SENSITIVE),
            new Integer(ResultSet.CONCUR_READ_ONLY)}, Boolean.FALSE);
    updateContainerInformation(
        MetadataDescription.SUPPORTS_RESULT_SET_CONCURRENCY, new Class[]{
            Integer.TYPE, Integer.TYPE}, new Object[]{
            new Integer(ResultSet.TYPE_SCROLL_SENSITIVE),
            new Integer(ResultSet.CONCUR_UPDATABLE)}, Boolean.FALSE);

    // We do support FORWARD_ONLY and SCROLL_INSENSITIVE ResultSets only
    updateContainerInformation(MetadataDescription.SUPPORTS_RESULT_SET_TYPE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_FORWARD_ONLY)}, Boolean.TRUE);
    updateContainerInformation(MetadataDescription.SUPPORTS_RESULT_SET_TYPE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_INSENSITIVE)}, Boolean.TRUE);
    updateContainerInformation(MetadataDescription.SUPPORTS_RESULT_SET_TYPE,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_SENSITIVE)}, Boolean.FALSE);

    // No support for Savepoints yet
    updateContainerInformation(MetadataDescription.SUPPORTS_SAVEPOINTS, null,
        null, Boolean.FALSE);

    // Updates are never detected since our ResultSets are disconnected
    updateContainerInformation(MetadataDescription.UPDATES_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_FORWARD_ONLY)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.UPDATES_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_INSENSITIVE)}, Boolean.FALSE);
    updateContainerInformation(MetadataDescription.UPDATES_ARE_DETECTED,
        new Class[]{Integer.TYPE}, new Object[]{new Integer(
            ResultSet.TYPE_SCROLL_SENSITIVE)}, Boolean.FALSE);
  }

  /**
   * Gets the list of tables of a database and add them to the database schema.
   * The caller must ensure that the parameters are not <code>null</code>.
   * 
   * @exception SQLException if an error occurs
   */
  public void createDatabaseSchemaDynamically() throws SQLException
  {
    if (dynamicPrecision == DatabaseBackendSchemaConstants.DynamicPrecisionStatic)
      return;
    Connection connection = null;
    boolean wasInitialized = connectionManager.isInitialized();

    try
    {
      if (!wasInitialized)
        connectionManager.initializeConnections();

      connection = connectionManager.getConnection();
      if (connection == null)
      {
        String msg = Translate.get("backend.meta.connection.failed");
        logger.error(msg);
        throw new SQLException(msg);
      }

      databaseSchema = new DatabaseSQLMetaData(logger, connection,
          dynamicPrecision, gatherSystemTables, schemaName)
          .createDatabaseSchema();

    }
    catch (Exception e)
    {
      if (e instanceof RuntimeException)
        logger.error(Translate.get("backend.meta.runtime.error"), e);
      throw new SQLException(Translate.get("backend.meta.failed.get.info", e));
    }
    finally
    {
      if (connection != null)
        connectionManager.releaseConnection(connection);

      if (!wasInitialized)
        connectionManager.finalizeConnections();

    }
  }

  /**
   * Returns the database schema. Returns <code>null</code> If an error has
   * occured during the schema generation.
   * <p>
   * If the schema has not been previously computed,
   * {@link #createDatabaseSchemaDynamically()}is called.
   * 
   * @return a <code>DatabaseSchema</code> value
   * @exception SQLException if a problem occurs when creating the database
   *              schema
   * @see #createDatabaseSchemaDynamically
   */
  public DatabaseSchema getDatabaseSchema() throws SQLException
  {
    if (databaseSchema == null)
      createDatabaseSchemaDynamically();
    return databaseSchema;
  }
}
