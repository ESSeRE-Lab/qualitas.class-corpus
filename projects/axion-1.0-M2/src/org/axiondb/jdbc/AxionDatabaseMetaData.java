/*
 * $Id: AxionDatabaseMetaData.java,v 1.17 2003/07/10 22:53:50 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002-2003 Axion Development Team.  All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above 
 *    copyright notice, this list of conditions and the following 
 *    disclaimer. 
 *   
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution. 
 *   
 * 3. The names "Tigris", "Axion", nor the names of its contributors may 
 *    not be used to endorse or promote products derived from this 
 *    software without specific prior written permission. 
 *  
 * 4. Products derived from this software may not be called "Axion", nor 
 *    may "Tigris" or "Axion" appear in their names without specific prior
 *    written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =======================================================================
 */

package org.axiondb.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.Database;

/**
 * A {@link DatabaseMetaData} implementation.
 * 
 * @TODO Support currently unsupported JDBC 3 methods.
 * @version $Revision: 1.17 $ $Date: 2003/07/10 22:53:50 $
 * @author Rodney Waldhoff
 */
public class AxionDatabaseMetaData implements DatabaseMetaData {

    public AxionDatabaseMetaData(AxionConnection conn, Database db) {
        _connection = conn;
        _db = db;
    }

    //-------------------------------------------------------------------------    

    /** Currently returns "<code>AxionDB</code>". */
    public String getDatabaseProductName() throws SQLException {
        return "AxionDB"; // XXX FIX ME XXX
    }

    /** Currently returns "<code>1.0M1</code>". */
    public String getDatabaseProductVersion() throws SQLException {
        return AXION_VERSION;
    }

    /** Currently returns "<code>Axion JDBC Driver". */
    public String getDriverName() throws SQLException {
        return "Axion JDBC Driver";
    }

    /** Currently returns "<code>1.0M1</code>". */
    public String getDriverVersion() throws SQLException {
        return AXION_VERSION;
    }

    /** Currently returns <code>0</code>. */
    public int getDriverMajorVersion() {
        return DB_MAJOR_VERSION;
    }

    /** Currently returns <code>1</code>. */
    public int getDriverMinorVersion() {
        return DB_MINOR_VERSION;
    }

    //-------------------------------------------------------------------------    

    /** Currently returns <code>null</code>. */
    public String getUserName() throws SQLException {  
        return null;
    }

    /** Currently returns <code>false</code>. */
    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }

    /** Returns <code>true</code>, since all tables are indeed selectable. */
    public boolean allTablesAreSelectable() throws SQLException {
        return true;
    }

    /** 
     * Returns <code>true</code> when this database is 
     * known to be read only, false otherwise.
     */
    public boolean isReadOnly() throws SQLException {
        return _db.isReadOnly();
    }

    /** Returns <code>false</code>, since Axion currently ignores case in identifiers. */
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    /** Returns <code>true</code>, since Axion supports column aliasing. */
    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }
    
    /** Returns <code>true</code>, since Axion supports addBatch,clearBatch and executeBatch. */
    public boolean supportsBatchUpdates() throws SQLException {
        return true;
    }

    /** Returns my {@link Connection}. */
    public Connection getConnection() throws SQLException {
        return _connection;
    }

    /** 
     * Returns <code>true</code>, since <code>null</code>s are
     * considered greater than any non-<code>null</code> value.
     */
    public boolean nullsAreSortedHigh() throws SQLException {
        return true;
    }

    /** 
     * Returns <code>false</code>, since <code>null</code>s are
     * considered greater than any non-<code>null</code> value.
     * @see #nullsAreSortedHigh
     */
    public boolean nullsAreSortedLow() throws SQLException {
        return false;
    }
    
    /** 
     * Returns <code>false</code>, since <code>null</code>s are
     * considered greater than any non-<code>null</code> value.
     * @see #nullsAreSortedHigh
     */
    public boolean nullsAreSortedAtStart() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>false</code>, since <code>null</code>s are
     * considered greater than any non-<code>null</code> value.
     * @see #nullsAreSortedHigh
     */
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>false</code>, since Axion currently
     * ignores case in identifiers, and stores them internally 
     * as upper case values.
     */
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>false</code>, since Axion currently
     * ignores case in identifiers.
     */
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>false</code>, since Axion currently
     * ignores case in identifiers, and stores them internally 
     * as upper case values.
     */
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>true</code>, since Axion currently
     * ignores case in identifiers, and stores them internally 
     * as upper case values.
     */
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return true;
    }

    /** 
     * Returns <code>false</code>, since Axion currently
     * ignores case in identifiers, and stores them internally 
     * as upper case values.  Quoted identifiers are also
     * currently unsupported.
     */
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }


    /** 
     * Returns <code>false</code>, since Axion currently
     * ignores case in identifiers, and stores them internally 
     * as upper case values.
     */
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>0</code>, since Axion has no hard limit
     * on the size of a row.
     */
    public int getMaxRowSize() throws SQLException {
        return 0;
    }

    /** 
     * Returns <code>0</code>, since Axion has no hard limit
     * on the size of a statement.
     */
    public int getMaxStatementLength() throws SQLException {
        return 0;
    }

    /** 
     * Returns <code>0</code>, since Axion has no hard limit
     * on the number of connections.
     */
    public int getMaxConnections() throws SQLException {
        return 0;
    }

    /** 
     * Returns <code>0</code>, since Axion has no hard limit
     * on the length of a column name.
     */
    public int getMaxColumnNameLength() throws SQLException {
        return Integer.MAX_VALUE;
    }

    /** 
     * Returns <code>1</code>, since Axion currently doesn't
     * support multi-column indices.
     */
    public int getMaxColumnsInIndex() throws SQLException {
        return 1; // 0 when we support multi-column indices
    }

    /** 
     * Returns {@link java.lang.Integer#MAX_VALUE}, the
     * maximum number of tables Axion can manage in a single
     * SELECT statement.
     */
    public int getMaxTablesInSelect() throws SQLException {
        return Integer.MAX_VALUE;
    }

    /** 
     * Returns {@link java.lang.Integer#MAX_VALUE}, the
     * maximum number of columns Axion can manage in a single
     * ORDER BY clause.
     */
    public int getMaxColumnsInOrderBy() throws SQLException {
        return Integer.MAX_VALUE;
    }

    /** 
     * Returns {@link java.lang.Integer#MAX_VALUE}, the
     * maximum number of columns Axion can manage in a single
     * SELECT clause.
     */
    public int getMaxColumnsInSelect() throws SQLException {
        return Integer.MAX_VALUE;
    }
    
    /** 
     * Returns {@link java.lang.Integer#MAX_VALUE}, the
     * maximum number of columns Axion can manage in a single
     * table.
     */
    public int getMaxColumnsInTable() throws SQLException {
        return Integer.MAX_VALUE;
    }

    /** 
     * Returns 0.
     */
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 0;
    }

    /** 
     * Returns <code>false</code> since UNION queries are
     * currently not supported..
     */
    public boolean supportsUnion() throws SQLException {
        return false;
    }
    
    /** Returns <code>0</code>. */
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    }
            
    /** Returns <code>0</code>. */
    public int getMaxStatements() throws SQLException {
        return 0;
    }
    
    /** Returns <code>Integer.MAX_VALUE</code>. */
    public int getMaxTableNameLength() throws SQLException {
        return Integer.MAX_VALUE;
    }
    
    /** Returns <code>0</code>. */
    public int getMaxUserNameLength() throws SQLException {
        return 0;
    }
    
    /** Returns <code>0</code>. */
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0;
    }
        
    /** Returns <code>0</code>. */
    public int getMaxCharLiteralLength() throws SQLException {
        return 0;
    }
    
    /** Returns <code>0</code>. */
    public int getMaxIndexLength() throws SQLException {
        return 0;
    }
    
    /** Returns <code>0</code>. */
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }
    
    /** Returns <code>0</code>. */
    public int getMaxCatalogNameLength() throws SQLException {
        return 0;
    }
    
    /** Returns {@link Connection#TRANSACTION_SERIALIZABLE}. */
    public int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_SERIALIZABLE;
    }

    /** Returns the connect string used to establish my {@link Connection}. */
    public String getURL() throws SQLException {
        return _connection.getURL();
    }

    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsSelectForUpdate() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsStoredProcedures() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsSubqueriesInExists() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsSubqueriesInIns() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return false;
    }

    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return false;
    }

    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return false;
    }

    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }
    
    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsGroupBy() throws SQLException {
        return false;
    }

    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsGroupByUnrelated() throws SQLException {
        return false;
    }

    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return false;
    }

    /** Returns <code>true</code>. */
    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }

    /** Returns <code>true</code>. */
    public boolean supportsFullOuterJoins() throws SQLException {
        return true;
    }

    /** Returns <code>true</code>. */
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }

    /** Returns <code>true</code>, since Axion allows arbitrary columns in an ORDER BY. */
    public boolean supportsOrderByUnrelated() throws SQLException {
        return true;
    }

    /** Returns <code>true</code>, since Axion supports transactions. */
    public boolean supportsTransactions() throws SQLException {
        return true;
    }

    /**
     * Returns <code>true</code> iff <i>level</i> is
     * {@link Connection#TRANSACTION_SERIALIZABLE} since 
     * Axion supports TRANSACTION_SERIALIZABLE transactions
     * only. 
     */
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        switch(level) {
            case Connection.TRANSACTION_SERIALIZABLE:
                return true;
            case Connection.TRANSACTION_NONE:
            case Connection.TRANSACTION_READ_COMMITTED:
            case Connection.TRANSACTION_READ_UNCOMMITTED:
            case Connection.TRANSACTION_REPEATABLE_READ:
                return false;
            default:
                return false;
        }
    }

    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsConvert() throws SQLException {
        return false; // though this would be easy to add
    }

    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return false;
    }

    /** Returns <code>false</code> as this feature is currently not supported. */
    public boolean supportsUnionAll() throws SQLException {
        return false;
    }

    /** Returns <code>true</code> as Axion supports table aliasing. */
    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }

    /** Returns <code>true</code> as Axion supports table aliasing. */
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return true;
    }

    /** Returns <code>true</code>. */
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    /** 
     * Returns <code>true</code> as Axion supports the 
     * <a href="http://msdn.microsoft.com/library/en-us/odbc/htm/odbcsql_minimum_grammar.asp">"ODBC Minimum SQL Grammar"</a>.
     * Namely:
     * <pre>
     * CREATE TABLE base-table-name (column-identifier data-type [,column-identifier data-type]*)
     * DELETE FROM table-name [WHERE search-condition]
     * DROP TABLE base-table-name 
     * INSERT INTO table-name [( column-identifier [, column-identifier]...)]
     *        VALUES (insert-value[, insert-value]... )
     * SELECT [ALL | DISTINCT] select-list
     *        FROM table-reference-list
     *        [WHERE search-condition]
     *        [order-by-clause]
     * UPDATE table-name SET column-identifier = {expression | NULL } 
     *        [, column-identifier = {expression | NULL}]*
     *        [WHERE search-condition]
     * </pre>
     */
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    }

    /** Returns <code>true</code>. */
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    }

    /** 
     * Supported, 
     * although the only supported patterns are <code>"%"</code> 
     * (matching all) or a total match 
     * (no <code>'%'</code> or <code>'.'</code> wildcards).
     */
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        if(_log.isDebugEnabled()) {
            _log.debug("getColumns(" + catalog + "," + schemaPattern + "," + tableNamePattern + "," + columnNamePattern + ")");
        }
        Statement stmt = _connection.createStatement();
        String where = "";
        {
            StringBuffer buf = new StringBuffer();
            if(null != catalog) {
                buf.append("TABLE_CAT = '").append(catalog).append("'");
            }
            if(null != schemaPattern && !("%".equals(schemaPattern))) {
                if(buf.length() != 0) { buf.append(" AND "); }
                buf.append("TABLE_SCHEM = '").append(schemaPattern).append("'"); // XXX FIX ME XXX should be LIKE
            }
            if(null != tableNamePattern && !("%".equals(tableNamePattern))) {
                if(buf.length() != 0) { buf.append(" AND "); }
                buf.append("TABLE_NAME = '").append(tableNamePattern).append("'"); // XXX FIX ME XXX should be LIKE
            }
            if(null != columnNamePattern && !("%".equals(columnNamePattern))) {
                if(buf.length() != 0) { buf.append(" AND "); }
                buf.append("COLUMN_NAME = '").append(columnNamePattern).append("'"); // XXX FIX ME XXX should be LIKE
            }
            if(buf.length() > 0) {
                where = "WHERE " + buf.toString();
            }
        }
        ResultSet rset = stmt.executeQuery("select TABLE_CAT, TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, DATA_TYPE, TYPE_NAME, COLUMN_SIZE, BUFFER_LENGTH, DECIMAL_DIGITS, NUM_PREC_RADIX, NULLABLE, REMARKS, COLUMN_DEF, SQL_DATA_TYPE, SQL_DATETIME_SUB, CHAR_OCTET_LENGTH, ORDINAL_POSITION, IS_NULLABLE from AXION_COLUMNS " + where + " order by TABLE_SCHEM, TABLE_NAME, ORDINAL_POSITION");
        return rset;
    }

    /** 
     * Supported, 
     * although the only supported patterns are <code>"%"</code> 
     * (matching all) or a total match 
     * (no <code>'%'</code> or <code>'.'</code> wildcards).
     */
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String types[]) throws SQLException {
        if(_log.isDebugEnabled()) {
            _log.debug("getTables(" + catalog + "," + schemaPattern + "," + tableNamePattern + "," + types + ")");
        }
        Statement stmt = _connection.createStatement();
        String where = "";
        StringBuffer buf = new StringBuffer();
        if(null != catalog) {
            buf.append("TABLE_CAT = '").append(catalog).append("'");
        }
        if(null != schemaPattern && !("%".equals(schemaPattern))) {
            if(buf.length() != 0) { buf.append(" AND "); }
            buf.append("TABLE_SCHEM = '").append(schemaPattern).append("'"); // XXX FIX ME XXX should be LIKE
        }
        if(null != tableNamePattern && !("%".equals(tableNamePattern))) {
            if(buf.length() != 0) { buf.append(" AND "); }
            buf.append("TABLE_NAME = '").append(tableNamePattern).append("'"); // XXX FIX ME XXX should be LIKE
        }
        if(null != types) {
            if(buf.length() != 0) { buf.append(" AND "); }
            buf.append("(");
            for(int i=0;i<types.length;i++) {
                if(i != 0) {
                    buf.append(" OR ");
                }
                buf.append("TABLE_TYPE = '").append(types[i]).append("'"); 
            }
            buf.append(")");
        }
        if(buf.length() != 0) {
            where = "WHERE " + buf.toString();
        }
        ResultSet rset = stmt.executeQuery("select TABLE_CAT, TABLE_SCHEM, TABLE_NAME, TABLE_TYPE, REMARKS from AXION_TABLES " + where + " order by TABLE_TYPE, TABLE_SCHEM, TABLE_NAME");
        return rset;
    }

    /** Supported. */
    public ResultSet getSchemas() throws SQLException {
        if(_log.isDebugEnabled()) {
            _log.debug("getSchemas()");
        }
        Statement stmt = _connection.createStatement();
        ResultSet rset = stmt.executeQuery("select TABLE_SCHEM from AXION_SCHEMATA ORDER BY TABLE_SCHEM");
        return rset;
    }

    /** Supported. */
    public ResultSet getCatalogs() throws SQLException {
        if(_log.isDebugEnabled()) {
            _log.debug("getCatalogs()");
        }
        Statement stmt = _connection.createStatement();
        ResultSet rset = stmt.executeQuery("select TABLE_CAT from AXION_CATALOGS ORDER BY TABLE_CAT");
        return rset;
    }

    /** Supported. */
    public ResultSet getTableTypes() throws SQLException {
        if(_log.isDebugEnabled()) {
            _log.debug("getTableTypes()");
        }
        Statement stmt = _connection.createStatement();
        ResultSet rset = stmt.executeQuery("select TABLE_TYPE from AXION_TABLE_TYPES order by TABLE_TYPE");
        return rset;
    }

    /** Supported. */
    public ResultSet getTypeInfo() throws SQLException {
        if(_log.isDebugEnabled()) {
            _log.debug("getTypeInfo()");
        }
        Statement stmt = _connection.createStatement();
        ResultSet rset = stmt.executeQuery("select TYPE_NAME, DATA_TYPE, PRECISION, LITERAL_PREFIX, LITERAL_SUFFIX, CREATE_PARAMS, NULLABLE, CASE_SENSITIVE, SEARCHABLE, UNSIGNED_ATTRIBUTE, FIXED_PREC_SCALE, AUTO_INCREMENT, LOCAL_TYPE_NAME, MINIMUM_SCALE, MAXIMUM_SCALE, SQL_DATA_TYPE, SQL_DATETIME_SUB, NUM_PREC_RADIX from AXION_TYPES order by DATA_TYPE");
        return rset;
    }

    /** Returns <code>false</code> as this feature is currently unsupported. */
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }
    
    /** Returns <code>true</code>, Axion supports multiple transactions. */
    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }

    /** Returns <code>true</code>, Axion supports NOT NULL constraints. */
    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }

    /** 
     * Returns <code>true</code>. 
     */
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return true;
    }
    
    /** 
     * Returns <code>false</code>. Closing a transaction
     * will close any open ResultSets. 
     */
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>false</code>. Closing a transaction
     * will close any open ResultSets. 
     */
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>true</code>. Statements remain valid 
     * accross a transaction boundary.
     */
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return true;
    }

    /** 
     * Returns <code>true</code>. Statements remain valid 
     * accross a transaction boundary.
     */
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return true;
    }

    /** 
     * Returns <code>false</code>, since Axion currently
     * doesn't treat Data Definition Language (DDL) statements 
     * like CREATE or DROP transactionally. 
     */
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }


    /** 
     * Returns <code>false</code>, since Axion currently
     * doesn't treat Data Definition Language (DDL) statements 
     * like CREATE or DROP transactionally. 
     */
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>false</code>, since Axion currently
     * doesn't treat Data Definition Language (DDL) statements 
     * like CREATE or DROP transactionally. 
     */
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>false</code> since LOB sizes are not 
     * counted in the {@link #getMaxRowSize maximum row size} 
     * (which is unbounded anyway).
     */
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return false;
    }

    /** 
     * Returns <code>false</code>, since the driver does not
     * require local files
     */
    public boolean usesLocalFiles() throws SQLException {
        return false; // XXX ??? XXX
    }

    /** 
     * Returns <code>false</code>, since the driver does not
     * require local files
     */
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }
    
    /** Returns <code>true</code>. */
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return true;
    }

    /** Returns <code>false</code> since this feature is currently not supported. */
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return false; // we don't support any subqueries
    }

    /** Returns <code>false</code> since this feature is currently not supported. */
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }

    /** Returns <code>false</code> since this feature is currently not supported. */
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }

    /** Returns <code>0</code> since named cursors are not supported. */
    public int getMaxCursorNameLength() throws SQLException {
        return 0;
    }

    /** Returns <code>"&nbsp;"</code> since quoted identifiers are not supported currently. */
    public String getIdentifierQuoteString() throws SQLException {
        return " "; // XXX FIX ME XXX: should support "
    }

    /** Returns <code>true</code> iff <i>type</i> is {@link ResultSet#TYPE_FORWARD_ONLY}. */
    public boolean supportsResultSetType(int type) throws SQLException {
        switch(type) {
            case ResultSet.TYPE_FORWARD_ONLY:
                return true;
            default:
                return false;
        }
    }
    
    //----------------------------

    /** Currently not supported. */
    public String getNumericFunctions() throws SQLException {
        throw new SQLException("getNumericFunctions() is not supported");
    }

    /** Currently not supported. */
    public String getSystemFunctions() throws SQLException {
        throw new SQLException("getSystemFunctions() is not supported");
    }

    /** Currently not supported. */
    public String getSQLKeywords() throws SQLException {
        throw new SQLException("getSQLKeywords() is not supported");
    }

    /** Currently not supported. */
    public String getSearchStringEscape() throws SQLException {
        throw new SQLException("getSearchStringEscape() is not supported");
    }

    /** Currently not supported. */
    public String getStringFunctions() throws SQLException {
        throw new SQLException("getStringFunctions() is not supported");
    }

    /** Currently not supported. */
    public String getTimeDateFunctions() throws SQLException {
        throw new SQLException("getTimeDateFunctions() is not supported");
    }

    /** Currently not supported. */
    public String getExtraNameCharacters() throws SQLException {
        throw new SQLException("getExtraNameCharacters() is not supported");
    }

    /** Currently not supported. */
    public boolean supportsCoreSQLGrammar() throws SQLException {
        throw new SQLException("supportsCoreSQLGrammar() is not supported");
    }

    /** Currently not supported. */
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        throw new SQLException("supportsANSI92EntryLevelSQL() is not supported");
    }

    /** Currently not supported. */
    public boolean supportsLikeEscapeClause() throws SQLException {
        throw new SQLException("supportsLikeEscapeClause() is not supported");
    }

    /** Currently not supported. */
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        throw new SQLException("supportsSchemasInTableDefinitions() is not supported");
    }

    /** Currently not supported. */
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        throw new SQLException("supportsExtendedSQLGrammar() is not supported");
    }

    /** Currently not supported. */
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        throw new SQLException("supportsSchemasInPrivilegeDefinitions() is not supported");
    }

    /** Currently not supported. */
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        throw new SQLException("supportsANSI92IntermediateSQL() is not supported");
    }

    /** Currently not supported. */
    public boolean supportsANSI92FullSQL() throws SQLException {
        throw new SQLException("supportsANSI92FullSQL() is not supported");
    }

    /** Currently not supported. */
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        throw new SQLException("supportsIntegrityEnhancementFacility() is not supported");
    }

    /** Currently not supported. */
    public String getSchemaTerm() throws SQLException {
        throw new SQLException("getSchemaTerm() is not supported");
    }
 
    /** Currently not supported. */
   public String getProcedureTerm() throws SQLException {
        throw new SQLException("getProcedureTerm() is not supported");
    }

    /** Currently not supported. */
    public String getCatalogTerm() throws SQLException {
        throw new SQLException("getCatalogTerm() is not supported");
    }

    /** Currently not supported. */
    public boolean isCatalogAtStart() throws SQLException {
        throw new SQLException("isCatalogAtStart() is not supported");
    }

    /** Currently not supported. */
    public String getCatalogSeparator() throws SQLException {
        throw new SQLException("getCatalogSeparator() is not supported");
    }

    /** Currently not supported. */
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        throw new SQLException("getProcedures() is not supported");
    }

    /** Currently not supported. */
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        throw new SQLException("getProcedureColumns() is not supported");
    }

    /** Currently not supported. */
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        throw new SQLException("getColumnPrivileges() is not supported");
    }

    /** Currently not supported. */
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        throw new SQLException("getTablePrivileges() is not supported");
    }

    /** Currently not supported. */
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        throw new SQLException("getBestRowIdentifier() is not supported");
    }

    /** Currently not supported. */
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        throw new SQLException("getVersionColumns() is not supported");
    }

    /** Currently not supported. */
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        throw new SQLException("getPrimaryKeys() is not supported"); // XXX IMPLEMENT ME XXX
    }

    /** Currently not supported. */
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        throw new SQLException("getImportedKeys() is not supported");
    }

    /** Currently not supported. */
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        throw new SQLException("getExportedKeys() is not supported");
    }

    /** Currently not supported. */
    public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        throw new SQLException("getCrossReference() is not supported");
    }

    /** Currently not supported. */
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        throw new SQLException("getIndexInfo() is not supported"); // XXX IMPLEMENT ME XXX
    }

    /** Currently not supported. */
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        throw new SQLException("supportsResultSetConcurrency() is not supported");
    }

    /** Currently not supported. */
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        throw new SQLException("ownUpdatesAreVisible() is not supported");
    }

    /** Currently not supported. */
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        throw new SQLException("ownDeletesAreVisible() is not supported");
    }

    /** Currently not supported. */
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        throw new SQLException("ownInsertsAreVisible() is not supported");
    }

    /** Currently not supported. */
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        throw new SQLException("othersUpdatesAreVisible() is not supported");
    }

    /** Currently not supported. */
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        throw new SQLException("othersDeletesAreVisible() is not supported");
    }

    /** Currently not supported. */
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        throw new SQLException("othersInsertsAreVisible() is not supported");
    }

    /** Currently not supported. */
    public boolean updatesAreDetected(int type) throws SQLException {
        throw new SQLException("updatesAreDetected() is not supported");
    }

    /** Currently not supported. */
    public boolean deletesAreDetected(int type) throws SQLException {
        throw new SQLException("deletesAreDetected() is not supported");
    }

    /** Currently not supported. */
    public boolean insertsAreDetected(int type) throws SQLException {
        throw new SQLException("insertsAreDetected() is not supported");
    }        

    /** Currently not supported. */
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        throw new SQLException("getUDTs() is not supported");
    }

    private AxionConnection _connection = null;
    private Database _db = null;
    private static final String AXION_VERSION = "1.0M2"; // XXX CHANGE ME ON RELEASE XXX
    private static final int DB_MAJOR_VERSION = 0;
    private static final int DB_MINOR_VERSION = 2;
    private static final Log _log = LogFactory.getLog(AxionDatabaseMetaData.class);

    // JDBC 3/JDK 1.4 methods

    public int getDatabaseMajorVersion() throws SQLException {
        return DB_MAJOR_VERSION;
    }

    public int getDatabaseMinorVersion() throws SQLException {
        return DB_MINOR_VERSION;
    }

    /** Always empty, super tables are currenlty not supported. */
    public ResultSet getSuperTables(String arg0, String arg1, String arg2) throws SQLException {
        return AxionResultSet.createEmptyResultSet();
    }

    /** Always empty, super types are currenlty not supported. */
    public ResultSet getSuperTypes(String arg0, String arg1, String arg2) throws SQLException {
        return AxionResultSet.createEmptyResultSet();
    }

    /** Currently always false. */
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;
    }

    /** Currently always false. */
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false; // per the javadoc, this refers to CallableStatements, which we don't support at all
    }

    /** Currently always false. */
    public boolean supportsNamedParameters() throws SQLException {
        return false; // per the javadoc, this refers to CallableStatements, which we don't support at all
    }

    /** Currently always false. */
    public boolean supportsSavepoints() throws SQLException {
        return false;
    }

    /** Currently always false. */
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    /** Currently unsupported. */
    public ResultSet getAttributes(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        throw new UnsupportedOperationException("getAttributes is currently not supported");
    }

    /** Currently unsupported. */
    public int getJDBCMajorVersion() throws SQLException {
        throw new SQLException("getJDBCMajorVersion is currently not supported");
    }

    /** Currently unsupported. */
    public int getJDBCMinorVersion() throws SQLException {
        throw new SQLException("getJDBCMinorVersion is currently not supported");
    }

    /** Currently unsupported. */
    public int getResultSetHoldability() throws SQLException {
        throw new SQLException("getResultSetHoldability is currently not supported");
    }

    /** Currently unsupported. */
    public int getSQLStateType() throws SQLException {
        throw new SQLException("getSQLStateType is currently not supported");
    }

    /** Currently unsupported. */
    public boolean locatorsUpdateCopy() throws SQLException {
        throw new SQLException("locatorsUpdateCopy is currently not supported");
    }

    /** Currently unsupported. */
    public boolean supportsResultSetHoldability(int arg0) throws SQLException {
        throw new SQLException("supportsResultSetHoldability is currently not supported");
    }

	public Object unwrap(Class iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isWrapperFor(Class iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public RowIdLifetime getRowIdLifetime() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getSchemas(String catalog, String schemaPattern)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public ResultSet getClientInfoProperties() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getFunctions(String catalog, String schemaPattern,
			String functionNamePattern) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getFunctionColumns(String catalog, String schemaPattern,
			String functionNamePattern, String columnNamePattern)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
