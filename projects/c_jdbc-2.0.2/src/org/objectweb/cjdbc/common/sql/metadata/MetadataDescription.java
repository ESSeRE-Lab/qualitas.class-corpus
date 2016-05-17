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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet, Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.common.sql.metadata;

/**
 * This class defines string description for the jdbc driver metadata
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public final class MetadataDescription
{
  /**
   * @see java.sql.DatabaseMetaData#getDefaultTransactionIsolation()
   */
  public static final String GET_DEFAULT_TRANSACTION_ISOLATION                           = "getDefaultTransactionIsolation";
  /**
   * @see java.sql.DatabaseMetaData#getDatabaseMajorVersion()
   */
  public static final String GET_DATABASE_MAJOR_VERSION                                  = "getDatabaseMajorVersion";
  /**
   * @see java.sql.DatabaseMetaData#getDatabaseMinorVersion()
   */
  public static final String GET_DATABASE_MINOR_VERSION                                  = "getDatabaseMinorVersion";
  /**
   * @see java.sql.DatabaseMetaData#getDriverMajorVersion()
   */
  public static final String GET_DRIVER_MAJOR_VERSION                                    = "getDriverMajorVersion";
  /**
   * @see java.sql.DatabaseMetaData#getDatabaseMinorVersion()
   */
  public static final String GET_DRIVER_MINOR_VERSION                                    = "getDriverMinorVersion";
  /**
   * @see java.sql.DatabaseMetaData#getJDBCMajorVersion()
   */
  public static final String GET_JDBC_MAJOR_VERSION                                      = "getJDBCMajorVersion";
  /**
   * @see java.sql.DatabaseMetaData#getJDBCMinorVersion()
   */
  public static final String GET_JDBC_MINOR_VERSION                                      = "getJDBCMinorVersion";
  /**
   * @see java.sql.DatabaseMetaData#getMaxBinaryLiteralLength()
   */
  public static final String GET_MAX_BINARY_LITERAL_LENGTH                               = "getMaxBinaryLiteralLength";
  /**
   * @see java.sql.DatabaseMetaData#getResultSetHoldability()
   */
  public static final String GET_RESULTSET_HOLDABILITY                                   = "getResultSetHoldability";
  /**
   * @see java.sql.DatabaseMetaData#getSQLStateType()
   */
  public static final String GET_SQL_STATE_TYPE                                          = "getSQLStateType";
  /**
   * @see java.sql.DatabaseMetaData#getMaxCatalogNameLength()
   */
  public static final String GET_MAX_CATALOG_NAME_LENGTH                                 = "getMaxCatalogNameLength";
  /**
   * @see java.sql.DatabaseMetaData#getMaxCharLiteralLength()
   */
  public static final String GET_MAX_CHAR_LITERAL_LENGTH                                 = "getMaxCharLiteralLength";
  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnNameLength()
   */
  public static final String GET_MAX_COLUMN_NAME_LENGTH                                  = "getMaxColumnNameLength";
  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInGroupBy()
   */
  public static final String GET_MAX_COLUMNS_IN_GROUP_BY                                 = "getMaxColumnsInGroupBy";
  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInIndex()
   */
  public static final String GET_MAX_COLUMNS_IN_INDEX                                    = "getMaxColumnsInIndex";
  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInOrderBy()
   */
  public static final String GET_MAX_COLUMNS_IN_ORDER_BY                                 = "getMaxColumnsInOrderBy";
  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInSelect()
   */
  public static final String GET_MAX_COLUMNS_IN_SELECT                                   = "getMaxColumnsInSelect";
  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInTable()
   */
  public static final String GET_MAX_COLUMNS_IN_TABLE                                    = "getMaxColumnsInTable";
  /**
   * @see java.sql.DatabaseMetaData#getMaxConnections()
   */
  public static final String GET_MAX_CONNECTIONS                                         = "getMaxConnections";
  /**
   * @see java.sql.DatabaseMetaData#getMaxCursorNameLength()
   */
  public static final String GET_MAX_CURSOR_NAME_LENGTH                                  = "getMaxCursorNameLength";
  /**
   * @see java.sql.DatabaseMetaData#getMaxIndexLength()
   */
  public static final String GET_MAX_INDEX_LENGTH                                        = "getMaxIndexLength";
  /**
   * @see java.sql.DatabaseMetaData#getMaxProcedureNameLength()
   */
  public static final String GET_MAX_PROCEDURE_NAME_LENGTH                               = "getMaxProcedureNameLength";
  /**
   * @see java.sql.DatabaseMetaData#getMaxRowSize()
   */
  public static final String GET_MAX_ROW_SIZE                                            = "getMaxRowSize";
  /**
   * @see java.sql.DatabaseMetaData#getMaxSchemaNameLength()
   */
  public static final String GET_MAX_SCHEMA_NAME_LENGTH                                  = "getMaxSchemaNameLength";
  /**
   * @see java.sql.DatabaseMetaData#getMaxStatementLength()
   */
  public static final String GET_MAX_STATEMENT_LENGTH                                    = "getMaxStatementLength";
  /**
   * @see java.sql.DatabaseMetaData#getMaxStatements()
   */
  public static final String GET_MAX_STATEMENTS                                          = "getMaxStatements";
  /**
   * @see java.sql.DatabaseMetaData#getMaxTableNameLength()
   */
  public static final String GET_MAX_TABLE_NAME_LENGTH                                   = "getMaxTableNameLength";
  /**
   * @see java.sql.DatabaseMetaData#getMaxTablesInSelect()
   */
  public static final String GET_MAX_TABLES_IN_SELECT                                    = "getMaxTablesInSelect";
  /**
   * @see java.sql.DatabaseMetaData#getMaxUserNameLength()
   */
  public static final String GET_MAX_USER_NAME_LENGTH                                    = "getMaxUserNameLength";
  /**
   * @see java.sql.DatabaseMetaData#getCatalogSeparator()
   */
  public static final String GET_CATALOG_SEPARATOR                                       = "getCatalogSeparator";
  /**
   * @see java.sql.DatabaseMetaData#getCatalogTerm()
   */
  public static final String GET_CATALOG_TERM                                            = "getCatalogTerm";
  /**
   * @see java.sql.DatabaseMetaData#getDatabaseProductName()
   */
  public static final String GET_DATABASE_PRODUCT_NAME                                   = "getDatabaseProductName";
  /**
   * @see java.sql.DatabaseMetaData#getDriverName()
   */
  public static final String GET_DRIVER_NAME                                             = "getDriverName";
  /**
   * @see java.sql.DatabaseMetaData#getDriverVersion()
   */
  public static final String GET_DRIVER_VERSION                                          = "getDriverVersion";
  /**
   * @see java.sql.DatabaseMetaData#getExtraNameCharacters()
   */
  public static final String GET_EXTRA_NAME_CHARACTERS                                   = "getExtraNameCharacters";
  /**
   * @see java.sql.DatabaseMetaData#getIdentifierQuoteString()
   */
  public static final String GET_IDENTIFIER_QUOTE_STRING                                 = "getIdentifierQuoteString";
  /**
   * @see java.sql.DatabaseMetaData#getNumericFunctions()
   */
  public static final String GET_NUMERIC_FUNCTIONS                                       = "getNumericFunctions";
  /**
   * @see java.sql.DatabaseMetaData#getProcedureTerm()
   */
  public static final String GET_PROCEDURE_TERM                                          = "getProcedureTerm";
  /**
   * @see java.sql.DatabaseMetaData#getSchemaTerm()
   */
  public static final String GET_SCHEMA_TERM                                             = "getSchemaTerm";
  /**
   * @see java.sql.DatabaseMetaData#getSearchStringEscape()
   */
  public static final String GET_SEARCH_STRING_ESCAPE                                    = "getSearchStringEscape";
  /**
   * @see java.sql.DatabaseMetaData#getSQLKeywords()
   */
  public static final String GET_SQL_KEYWORDS                                            = "getSQLKeywords";
  /**
   * @see java.sql.DatabaseMetaData#getStringFunctions()
   */
  public static final String GET_STRING_FUNCTIONS                                        = "getStringFunctions";
  /**
   * @see java.sql.DatabaseMetaData#getSystemFunctions()
   */
  public static final String GET_SYSTEM_FUNCTIONS                                        = "getSystemFunctions";
  /**
   * @see java.sql.DatabaseMetaData#getTimeDateFunctions()
   */
  public static final String GET_TIME_DATE_FUNCTIONS                                     = "getTimeDateFunctions";
  /**
   * @see java.sql.DatabaseMetaData#allProceduresAreCallable()
   */
  public static final String ALL_PROCEDURES_ARE_CALLABLE                                 = "allProceduresAreCallable";
  /**
   * @see java.sql.DatabaseMetaData#allTablesAreSelectable()
   */
  public static final String ALL_TABLES_ARE_SELECTABLE                                   = "allTablesAreSelectable";
  /**
   * @see java.sql.DatabaseMetaData#dataDefinitionCausesTransactionCommit()
   */
  public static final String DATA_DEFINITION_CAUSES_TRANSACTION_COMMIT                   = "dataDefinitionCausesTransactionCommit";
  /**
   * @see java.sql.DatabaseMetaData#dataDefinitionIgnoredInTransactions()
   */
  public static final String DATA_DEFINITION_IGNORED_IN_TRANSACTIONS                     = "dataDefinitionIgnoredInTransactions";
  /**
   * @see java.sql.DatabaseMetaData#deletesAreDetected(int)
   */
  public static final String DELETES_ARE_DETECTED                                        = "deletesAreDetected";
  /**
   * @see java.sql.DatabaseMetaData#doesMaxRowSizeIncludeBlobs()
   */
  public static final String DOES_MAX_ROW_SIZE_INCLUDE_BLOBS                             = "doesMaxRowSizeIncludeBlobs";
  /**
   * @see java.sql.DatabaseMetaData#insertsAreDetected(int)
   */
  public static final String INSERTS_ARE_DETECTED                                        = "insertsAreDetected";
  /**
   * @see java.sql.DatabaseMetaData#locatorsUpdateCopy()
   */
  public static final String LOCATORS_UPDATE_COPY                                        = "locatorsUpdateCopy";
  /**
   * @see java.sql.DatabaseMetaData#nullPlusNonNullIsNull()
   */
  public static final String NULL_PLUS_NON_NULL_IS_NULL                                  = "nullPlusNonNullIsNull";
  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedAtEnd()
   */
  public static final String NULLS_ARE_SORTED_AT_END                                     = "nullsAreSortedAtEnd";
  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedAtStart()
   */
  public static final String NULLS_ARE_SORTED_AT_START                                   = "nullsAreSortedAtStart";
  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedHigh()
   */
  public static final String NULLS_ARE_SORTED_HIGH                                       = "nullsAreSortedHigh";
  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedLow()
   */
  public static final String NULLS_ARE_SORTED_LOW                                        = "nullsAreSortedLow";
  /**
   * @see java.sql.DatabaseMetaData#othersDeletesAreVisible(int)
   */
  public static final String OTHERS_DELETES_ARE_VISIBLE                                  = "othersDeletesAreVisible";
  /**
   * @see java.sql.DatabaseMetaData#othersInsertsAreVisible(int)
   */
  public static final String OTHERS_INSERTS_ARE_VISIBLE                                  = "othersInsertsAreVisible";
  /**
   * @see java.sql.DatabaseMetaData#othersUpdatesAreVisible(int)
   */
  public static final String OTHERS_UPDATES_ARE_VISIBLE                                  = "othersUpdatesAreVisible";
  /**
   * @see java.sql.DatabaseMetaData#ownDeletesAreVisible(int)
   */
  public static final String OWN_DELETES_ARE_VISIBLE                                     = "ownDeletesAreVisible";
  /**
   * @see java.sql.DatabaseMetaData#ownInsertsAreVisible(int)
   */
  public static final String OWN_INSERTS_ARE_VISIBLE                                     = "ownInsertsAreVisible";
  /**
   * @see java.sql.DatabaseMetaData#ownUpdatesAreVisible(int)
   */
  public static final String OWN_UPDATES_ARE_VISIBLE                                     = "ownUpdatesAreVisible";
  /**
   * @see java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()
   */
  public static final String STORES_LOWER_CASE_IDENTIFIERS                               = "storesLowerCaseIdentifiers";
  /**
   * @see java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()
   */
  public static final String STORES_LOWER_CASE_QUOTED_IDENTIFIERS                        = "storesLowerCaseQuotedIdentifiers";
  /**
   * @see java.sql.DatabaseMetaData#storesMixedCaseIdentifiers()
   */
  public static final String STORES_MIXED_CASE_IDENTIFIERS                               = "storesMixedCaseIdentifiers";
  /**
   * @see java.sql.DatabaseMetaData#storesMixedCaseQuotedIdentifiers()
   */
  public static final String STORES_MIXED_CASE_QUOTED_IDENTIFIERS                        = "storesMixedCaseQuotedIdentifiers";
  /**
   * @see java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()
   */
  public static final String STORES_UPPER_CASE_IDENTIFIERS                               = "storesUpperCaseIdentifiers";
  /**
   * @see java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()
   */
  public static final String STORES_UPPER_CASE_QUOTED_IDENTIFIERS                        = "storesUpperCaseQuotedIdentifiers";
  /**
   * @see java.sql.DatabaseMetaData#supportsAlterTableWithAddColumn()
   */
  public static final String SUPPORTS_ALTER_TABLE_WITH_ADD_COLUMN                        = "supportsAlterTableWithAddColumn";
  /**
   * @see java.sql.DatabaseMetaData#supportsAlterTableWithDropColumn()
   */
  public static final String SUPPORTS_ALTER_TABLE_WITH_DROP_COLUMN                       = "supportsAlterTableWithDropColumn";
  /**
   * @see java.sql.DatabaseMetaData#supportsANSI92EntryLevelSQL()
   */
  public static final String SUPPORTS_ANSI92_ENTRY_LEVEL_SQL                             = "supportsANSI92EntryLevelSQL";
  /**
   * @see java.sql.DatabaseMetaData#supportsANSI92FullSQL()
   */
  public static final String SUPPORTS_ANSI92_FULL_SQL                                    = "supportsANSI92FullSQL";
  /**
   * @see java.sql.DatabaseMetaData#supportsANSI92IntermediateSQL()
   */
  public static final String SUPPORTS_ANSI92_INTERMEDIATE_SQL                            = "supportsANSI92IntermediateSQL";
  /**
   * @see java.sql.DatabaseMetaData#supportsBatchUpdates()
   */
  public static final String SUPPORTS_BATCH_UPDATES                                      = "supportsBatchUpdates";
  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInDataManipulation()
   */
  public static final String SUPPORTS_CATALOGS_IN_DATA_MANIPULATION                      = "supportsCatalogsInDataManipulation";
  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInIndexDefinitions()
   */
  public static final String SUPPORTS_CATALOGS_IN_INDEX_DEFINITIONS                      = "supportsCatalogsInIndexDefinitions";
  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInPrivilegeDefinitions()
   */
  public static final String SUPPORTS_CATALOGS_IN_PRIVILEGE_DEFINITIONS                  = "supportsCatalogsInPrivilegeDefinitions";
  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInProcedureCalls()
   */
  public static final String SUPPORTS_CATALOGS_IN_PROCEDURE_CALLS                        = "supportsCatalogsInProcedureCalls";
  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInTableDefinitions()
   */
  public static final String SUPPORTS_CATALOGS_IN_TABLE_DEFINITIONS                      = "supportsCatalogsInTableDefinitions";
  /**
   * @see java.sql.DatabaseMetaData#supportsColumnAliasing()
   */
  public static final String SUPPORTS_COLUMN_ALIASING                                    = "supportsColumnAliasing";
  /**
   * @see java.sql.DatabaseMetaData#supportsConvert()
   */
  public static final String SUPPORTS_CONVERT                                            = "supportsConvert";
  /**
   * @see java.sql.DatabaseMetaData#supportsCoreSQLGrammar()
   */
  public static final String SUPPORTS_CORE_SQL_GRAMMAR                                   = "supportsCoreSQLGrammar";
  /**
   * @see java.sql.DatabaseMetaData#supportsCorrelatedSubqueries()
   */
  public static final String SUPPORTS_CORRELATED_SUBQUERIES                              = "supportsCorrelatedSubqueries";
  /**
   * @see java.sql.DatabaseMetaData#supportsDataDefinitionAndDataManipulationTransactions()
   */
  public static final String SUPPORTS_DATA_DEFINITION_AND_DATA_MANIPULATION_TRANSACTIONS = "supportsDataDefinitionAndDataManipulationTransactions";
  /**
   * @see java.sql.DatabaseMetaData#supportsDataManipulationTransactionsOnly()
   */
  public static final String SUPPORTS_DATA_MANIPULATION_TRANSACTIONS_ONLY                = "supportsDataManipulationTransactionsOnly";
  /**
   * @see java.sql.DatabaseMetaData#supportsDifferentTableCorrelationNames()
   */
  public static final String SUPPORTS_DIFFERENT_TABLE_CORRELATION_NAMES                  = "supportsDifferentTableCorrelationNames";
  /**
   * @see java.sql.DatabaseMetaData#supportsExpressionsInOrderBy()
   */
  public static final String SUPPORTS_EXPRESSIONS_IN_ORDER_BY                            = "supportsExpressionsInOrderBy";
  /**
   * @see java.sql.DatabaseMetaData#supportsExtendedSQLGrammar()
   */
  public static final String SUPPORTS_EXTENDED_SQL_GRAMMAR                               = "supportsExtendedSQLGrammar";
  /**
   * @see java.sql.DatabaseMetaData#supportsFullOuterJoins()
   */
  public static final String SUPPORTS_FULL_OUTER_JOINS                                   = "supportsFullOuterJoins";
  /**
   * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
   */
  public static final String SUPPORTS_GET_GENERATED_KEYS                                 = "supportsGetGeneratedKeys";
  /**
   * @see java.sql.DatabaseMetaData#supportsGroupBy()
   */
  public static final String SUPPORTS_GROUP_BY                                           = "supportsGroupBy";
  /**
   * @see java.sql.DatabaseMetaData#supportsGroupByBeyondSelect()
   */
  public static final String SUPPORTS_GROUP_BY_BEYOND_SELECT                             = "supportsGroupByBeyondSelect";
  /**
   * @see java.sql.DatabaseMetaData#supportsGroupByUnrelated()
   */
  public static final String SUPPORTS_GROUP_BY_UNRELATED                                 = "supportsGroupByUnrelated";
  /**
   * @see java.sql.DatabaseMetaData#supportsIntegrityEnhancementFacility()
   */
  public static final String SUPPORTS_INTEGRITY_ENHANCEMENT_FACILITY                     = "supportsIntegrityEnhancementFacility";
  /**
   * @see java.sql.DatabaseMetaData#supportsLikeEscapeClause()
   */
  public static final String SUPPORTS_LIKE_ESCAPE_CLAUSE                                 = "supportsLikeEscapeClause";
  /**
   * @see java.sql.DatabaseMetaData#supportsLimitedOuterJoins()
   */
  public static final String SUPPORTS_LIMITED_OUTER_JOINS                                = "supportsLimitedOuterJoins";
  /**
   * @see java.sql.DatabaseMetaData#supportsMinimumSQLGrammar()
   */
  public static final String SUPPORTS_MINIMUM_SQL_GRAMMAR                                = "supportsMinimumSQLGrammar";
  /**
   * @see java.sql.DatabaseMetaData#supportsMixedCaseIdentifiers()
   */
  public static final String SUPPORTS_MIXED_CASE_IDENTIFIERS                             = "supportsMixedCaseIdentifiers";
  /**
   * @see java.sql.DatabaseMetaData#supportsMixedCaseQuotedIdentifiers()
   */
  public static final String SUPPORTS_MIXED_CASE_QUOTED_IDENTIFIERS                      = "supportsMixedCaseQuotedIdentifiers";
  /**
   * @see java.sql.DatabaseMetaData#supportsMultipleOpenResults()
   */
  public static final String SUPPORTS_MULTIPLE_OPEN_RESULTS                              = "supportsMultipleOpenResults";
  /**
   * @see java.sql.DatabaseMetaData#supportsMultipleResultSets()
   */
  public static final String SUPPORTS_MULTIPLE_RESULTSETS                                = "supportsMultipleResultSets";
  /**
   * @see java.sql.DatabaseMetaData#supportsMultipleTransactions()
   */
  public static final String SUPPORTS_MULTIPLE_TRANSACTIONS                              = "supportsMultipleTransactions";
  /**
   * @see java.sql.DatabaseMetaData#supportsNamedParameters()
   */
  public static final String SUPPORTS_NAMED_PARAMETERS                                   = "supportsNamedParameters";
  /**
   * @see java.sql.DatabaseMetaData#supportsNonNullableColumns()
   */
  public static final String SUPPORTS_NON_NULLABLE_COLUMNS                               = "supportsNonNullableColumns";
  /**
   * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossCommit()
   */
  public static final String SUPPORTS_OPEN_CURSORS_ACROSS_COMMIT                         = "supportsOpenCursorsAcrossCommit";
  /**
   * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossRollback()
   */
  public static final String SUPPORTS_OPEN_CURSORS_ACROSS_ROLLBACK                       = "supportsOpenCursorsAcrossRollback";
  /**
   * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossCommit()
   */
  public static final String SUPPORTS_OPEN_STATEMENTS_ACROSS_COMMIT                      = "supportsOpenStatementsAcrossCommit";
  /**
   * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossRollback()
   */
  public static final String SUPPORTS_OPEN_STATEMENTS_ACROSS_ROLLBACK                    = "supportsOpenStatementsAcrossRollback";
  /**
   * @see java.sql.DatabaseMetaData#supportsOrderByUnrelated()
   */
  public static final String SUPPORTS_ORDER_BY_UNRELATED                                 = "supportsOrderByUnrelated";
  /**
   * @see java.sql.DatabaseMetaData#supportsOuterJoins()
   */
  public static final String SUPPORTS_OUTER_JOINS                                        = "supportsOuterJoins";
  /**
   * @see java.sql.DatabaseMetaData#supportsPositionedDelete()
   */
  public static final String SUPPORTS_POSITIONED_DELETE                                  = "supportsPositionedDelete";
  /**
   * @see java.sql.DatabaseMetaData#supportsPositionedUpdate()
   */
  public static final String SUPPORTS_POSITIONED_UPDATE                                  = "supportsPositionedUpdate";
  /**
   * @see java.sql.DatabaseMetaData#supportsResultSetConcurrency(int, int)
   */
  public static final String SUPPORTS_RESULT_SET_CONCURRENCY                             = "supportsResultSetConcurrency";
  /**
   * @see java.sql.DatabaseMetaData#supportsResultSetHoldability(int)
   */
  public static final String SUPPORTS_RESULT_SET_HOLDABILITY                             = "supportsResultSetHoldability";
  /**
   * @see java.sql.DatabaseMetaData#supportsResultSetType(int)
   */
  public static final String SUPPORTS_RESULT_SET_TYPE                                    = "supportsResultSetType";
  /**
   * @see java.sql.DatabaseMetaData#supportsSavepoints()
   */
  public static final String SUPPORTS_SAVEPOINTS                                         = "supportsSavepoints";
  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInDataManipulation()
   */
  public static final String SUPPORTS_SCHEMAS_IN_DATA_MANIPULATION                       = "supportsSchemasInDataManipulation";
  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInIndexDefinitions()
   */
  public static final String SUPPORTS_SCHEMAS_IN_INDEX_DEFINITIONS                       = "supportsSchemasInIndexDefinitions";
  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInPrivilegeDefinitions()
   */
  public static final String SUPPORTS_SCHEMAS_IN_PRIVILEGE_DEFINITIONS                   = "supportsSchemasInPrivilegeDefinitions";
  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInProcedureCalls()
   */
  public static final String SUPPORTS_SCHEMAS_IN_PROCEDURE_CALLS                         = "supportsSchemasInProcedureCalls";
  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInTableDefinitions()
   */
  public static final String SUPPORTS_SCHEMAS_IN_TABLE_DEFINITIONS                       = "supportsSchemasInTableDefinitions";
  /**
   * @see java.sql.DatabaseMetaData#supportsSelectForUpdate()
   */
  public static final String SUPPORTS_SELECT_FOR_UPDATE                                  = "supportsSelectForUpdate";
  /**
   * @see java.sql.DatabaseMetaData#supportsStatementPooling()
   */
  public static final String SUPPORTS_STATEMENT_POOLING                                  = "supportsStatementPooling";
  /**
   * @see java.sql.DatabaseMetaData#supportsStoredProcedures()
   */
  public static final String SUPPORTS_STORED_PROCEDURES                                  = "supportsStoredProcedures";
  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInComparisons()
   */
  public static final String SUPPORTS_SUB_QUERIES_IN_COMPARISONS                         = "supportsSubqueriesInComparisons";
  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInExists()
   */
  public static final String SUPPORTS_SUB_QUERIES_IN_EXISTS                              = "supportsSubqueriesInExists";
  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInIns()
   */
  public static final String SUPPORTS_SUB_QUERIES_IN_INS                                 = "supportsSubqueriesInIns";
  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInQuantifieds()
   */
  public static final String SUPPORTS_SUB_QUERIES_IN_QUANTIFIEDS                         = "supportsSubqueriesInQuantifieds";
  /**
   * @see java.sql.DatabaseMetaData#supportsTableCorrelationNames()
   */
  public static final String SUPPORTS_TABLE_CORRELATION_NAMES                            = "supportsTableCorrelationNames";
  /**
   * @see java.sql.DatabaseMetaData#supportsTransactionIsolationLevel(int)
   */
  public static final String SUPPORTS_TRANSACTION_ISOLATION_LEVEL                        = "supportsTransactionIsolationLevel";
  /**
   * @see java.sql.DatabaseMetaData#supportsTransactions()
   */
  public static final String SUPPORTS_TRANSACTIONS                                       = "supportsTransactions";
  /**
   * @see java.sql.DatabaseMetaData#supportsUnion()
   */
  public static final String SUPPORTS_UNION                                              = "supportsUnion";
  /**
   * @see java.sql.DatabaseMetaData#supportsUnionAll()
   */
  public static final String SUPPORTS_UNION_ALL                                          = "supportsUnionAll";
  /**
   * @see java.sql.DatabaseMetaData#updatesAreDetected(int)
   */
  public static final String UPDATES_ARE_DETECTED                                        = "updatesAreDetected";
  /**
   * @see java.sql.DatabaseMetaData#usesLocalFilePerTable()
   */
  public static final String USES_LOCAL_FILE_PER_TABLE                                   = "usesLocalFilePerTable";
  /**
   * @see java.sql.DatabaseMetaData#usesLocalFiles()
   */
  public static final String USES_LOCAL_FILES                                            = "usesLocalFiles";
  /**
   * @see java.sql.DatabaseMetaData#isCatalogAtStart()
   */
  public static final String IS_CATALOG_AT_START                                         = "isCatalogAtStart";

}