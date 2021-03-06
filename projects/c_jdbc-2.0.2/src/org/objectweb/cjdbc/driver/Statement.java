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
 * Contributor(s): Vadim Kassin, Jean-Bernard van Zuylen.
 */

package org.objectweb.cjdbc.driver;

import java.sql.BatchUpdateException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Vector;

import org.objectweb.cjdbc.common.exceptions.NotImplementedException;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.AlterRequest;
import org.objectweb.cjdbc.common.sql.CreateRequest;
import org.objectweb.cjdbc.common.sql.DeleteRequest;
import org.objectweb.cjdbc.common.sql.DropRequest;
import org.objectweb.cjdbc.common.sql.InsertRequest;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.StoredProcedure;
import org.objectweb.cjdbc.common.sql.UpdateRequest;

/**
 * A <code>Statement</code> object is used for executing a static SQL
 * statement and obtaining the results produced by it.
 * <p>
 * Only one <code>ResultSet</code> per <code>Statement</code> can be open at
 * any point in time. Therefore, if the reading of one <code>ResultSet</code>
 * is interleaved with the reading of another, each must have been generated by
 * different <code>Statements</code>. All <code>Statements</code> execute
 * methods implicitly close a statement's current <code>ResultSet</code> if an
 * open one exists.
 * 
 * @see java.sql.Statement
 * @see DriverResultSet
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:vadim@kase.kz">Vadim Kassin </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class Statement implements java.sql.Statement
{
  /** The connection that created us */
  protected Connection connection           = null;

  /** Vector for batch commands */
  protected Vector     batch                = null;

  /** The warnings chain */
  private SQLWarning   warnings             = null;

  /** The current result for a read request */
  protected ResultSet  result               = null;

  /** The update count for a write request */
  protected int        updateCount          = -1;

  /** Query timeout in seconds (0 means no timeout) */
  private int          timeout              = 0;

  /** Default ResultSet fetch size */
  private int          fetchSize            = 0;
  /** Cursor name used jointly with fetch size */
  private String       cursorName;

  /** Type of the ResultSet defaults to TYPE_FORWARD_ONLY */
  private int          resultSetType        = ResultSet.TYPE_FORWARD_ONLY;

  /** ResultSet Concurrency defaults to CONCUR_READ_ONLY */
  private int          resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

  /** Maximum field size (unused) */
  private int          maxFieldSize         = 0;

  /** Maximum number of rows */
  private int          maxRows              = 0;

  /**
   * Direction for fetching rows from ResultSet (note that this hint is
   * currently ignored
   */
  private int          fetchDirection       = ResultSet.FETCH_FORWARD;

  /**
   * Should the driver to escape processing before sending to the DB?
   */
  protected boolean    escapeProcessing     = true;

  /** Auto generated keys */
  protected ResultSet  generatedKeys        = null;
  protected int        generatedKeysFlag    = java.sql.Statement.NO_GENERATED_KEYS;

  /**
   * Creates a new <code>Statement</code> instance.
   * 
   * @param c the <code>Connection</code> that created us
   */
  public Statement(Connection c)
  {
    connection = c;
  }

  /**
   * Adds sql to the current list of commands.
   * 
   * @param sql an SQL statement that returns an update count (INSERT or UPDATE)
   * @exception SQLException if an error occurs
   */
  public synchronized void addBatch(String sql) throws SQLException
  {
    if (batch == null)
      batch = new Vector();
    batch.addElement(sql.trim());
  }

  /**
   * Could be use by one thread to cancel a statement that is being executed by
   * another thread. We don't support that for instance.
   * 
   * @exception SQLException if an error occurs
   */
  public void cancel() throws SQLException
  {
    throw new NotImplementedException("cancel()");
  }

  /**
   * Empties the current list of commands.
   * 
   * @exception SQLException if an error occurs
   */
  public void clearBatch() throws SQLException
  {
    if (batch != null)
      batch.removeAllElements();
  }

  /**
   * After this call, <code>getWarnings</code> returns <code>null</code>
   * until a new warning is reported for this <code>Statement</code>.
   * 
   * @exception SQLException if a database access error occurs (why?)
   */
  public void clearWarnings() throws SQLException
  {
    warnings = null;
  }

  /**
   * Execute a batch of commands
   * 
   * @return an array containing update count that corresponding to the commands
   *         that executed successfully
   * @exception BatchUpdateException if an error occurs on one statement (the
   *              number of updated rows for the successfully executed
   *              statements can be found in
   *              BatchUpdateException.getUpdateCounts())
   */
  public int[] executeBatch() throws BatchUpdateException
  {
    if (batch == null || batch.isEmpty())
      return new int[0];

    int size = batch.size();
    int[] batchResult = new int[size];
    int i = 0;

    try
    {
      for (i = 0; i < size; i++)
        batchResult[i] = this.executeUpdate((String) batch.elementAt(i));
      return batchResult;
    }
    catch (SQLException e)
    {
      String message = "Batch failed for request " + i + ": "
          + batch.elementAt(i) + " (" + e + ")";

      int[] updateCounts = new int[i];
      System.arraycopy(batchResult, 0, updateCounts, 0, i);

      throw new BatchUpdateException(message, updateCounts);
    }
    finally
    {
      batch.removeAllElements();
    }
  }

  /**
   * In many cases, it is desirable to immediately release a Statement's
   * database and JDBC resources instead of waiting for this to happen when it
   * is automatically closed. The close method provides this immediate release.
   * <p>
   * <B>Note: </B> A Statement is automatically closed when it is garbage
   * collected. When a Statement is closed, its current ResultSet, if one
   * exists, is also closed.
   * 
   * @exception SQLException if a database access error occurs (why?)
   */
  public void close() throws SQLException
  {
    // Force the ResultSet to close
    if (result != null)
      result.close();

    // Disasociate it from us (For Garbage Collection)
    result = null;
    connection = null;
  }

  /**
   * Execute a SQL statement that may return multiple results.
   * 
   * @param sql any SQL statement
   * @return true if the result is a ResultSet or false if it is an integer
   * @exception SQLException if an error occurs
   */
  public boolean execute(String sql) throws SQLException
  {
    int start = 0;
    try
    {
      // Ignore any leading parenthesis
      while (sql.charAt(start) == '(')
        start++;
    }
    catch (IndexOutOfBoundsException e)
    {
      // Probably a buggy request, let it go through and let thefollowing code
      // to report an accurate error if any.
      start = 0;
    }

    if (sql.regionMatches(true, start, "select", 0, 6)
        || (sql.regionMatches(true, start, "{call", 0, 5))
        || (sql.regionMatches(true, start, "values", 0, 6)))
    {
      result = executeQuery(sql);
      return true;
    }
    else
    {
      updateCount = executeUpdate(sql);
      return false;
    }
  }

  /**
   * Execute a SQL statement that returns a single ResultSet
   * 
   * @param sql typically a static SQL <code>SELECT</code> statement
   * @return a ResulSet that contains the data produced by the query
   * @exception SQLException if a database access error occurs
   */
  public java.sql.ResultSet executeQuery(String sql) throws SQLException
  {
    return executeQuery(null, sql.trim());
  }

  /**
   * Execute a SQL statement that returns a single ResultSet
   * 
   * @param sqlSkeleton the SQL request squeleton or null
   * @param sqlQuery typically a static SQL <code>SELECT</code> statement that
   *          is already trimed
   * @return a ResulSet that contains the data produced by the query
   * @exception SQLException if a database access error occurs or if this
   *              statement is closed
   */
  protected java.sql.ResultSet executeQuery(String sqlSkeleton, String sqlQuery)
      throws SQLException
  {
    if (isClosed())
    {
      throw new SQLException("Unable to execute query on a closed statement");
    }
    updateCount = -1; // invalidate the last write result
    if (result != null)
    { // Discard the previous result
      result.close();
      result = null;
    }

    if (sqlQuery.regionMatches(true, 0, "{call", 0, 5))
    {
      StoredProcedure proc = new StoredProcedure(sqlQuery, escapeProcessing,
          timeout, Connection.LINE_SEPARATOR, true /* isRead */);
      if (connection.controllerNeedsSqlSkeleton || !connection.isDriverProcessed())
        proc.setSqlSkeleton(sqlSkeleton);
      proc.setMaxRows(maxRows);
      proc.setFetchSize(fetchSize);
      proc.setCursorName(cursorName);
      result = connection.execReadStoredProcedure(proc);
    }
    else
    {
      SelectRequest request = new SelectRequest(sqlQuery, escapeProcessing,
          timeout, Connection.LINE_SEPARATOR);
      if (connection.controllerNeedsSqlSkeleton || !connection.isDriverProcessed())
        request.setSqlSkeleton(sqlSkeleton);
      request.setMaxRows(maxRows);
      request.setFetchSize(fetchSize);
      request.setCursorName(cursorName);
      result = connection.execReadRequest(request);
    }

    if (result instanceof DriverResultSet)
      ((DriverResultSet) result).setStatement(this);
    return result;
  }

  /**
   * Execute a SQL INSERT, UPDATE or DELETE statement. In addition SQL
   * statements that return nothing such as SQL DDL statements can be executed
   * 
   * @param sql a SQL statement
   * @return either a row count, or 0 for SQL commands
   * @exception SQLException if a database access error occurs
   */
  public int executeUpdate(String sql) throws SQLException
  {
    return executeUpdateWithSkeleton(null, sql.trim());
  }

  /**
   * Execute a SQL INSERT, UPDATE or DELETE statement. In addition SQL
   * statements that return nothing such as SQL DDL statements can be executed
   * 
   * @param sqlSkeleton the SQL request squeleton or null
   * @param sqlQuery a static SQL statement that is already trimed
   * @return either a row count, or 0 for SQL commands
   * @exception SQLException if a database access error occurs or if this
   *              statement is closed
   */
  protected int executeUpdateWithSkeleton(String sqlSkeleton, String sqlQuery)
      throws SQLException
  {
    if (isClosed())
    {
      throw new SQLException("Unable to execute query on a closed statement");
    }
    if (result != null)
    { // Discard the previous result
      result.close();
      result = null;
    }

    // Check that the command starts with
    // insert/update/delete/create/drop/{call
    String lower = sqlQuery.substring(0,
        6 < sqlQuery.length() ? 6 : sqlQuery.length()).toLowerCase();
    AbstractWriteRequest request;
    if (lower.equals("insert"))
      request = new InsertRequest(sqlQuery, escapeProcessing, timeout,
          Connection.LINE_SEPARATOR,
          (Statement.RETURN_GENERATED_KEYS == generatedKeysFlag) /* isRead */);
    else if (lower.equals("update"))
      request = new UpdateRequest(sqlQuery, escapeProcessing, timeout,
          Connection.LINE_SEPARATOR);
    else if (lower.equals("delete"))
      request = new DeleteRequest(sqlQuery, escapeProcessing, timeout,
          Connection.LINE_SEPARATOR);
    else if (lower.startsWith("create"))
      request = new CreateRequest(sqlQuery, escapeProcessing, timeout,
          Connection.LINE_SEPARATOR);
    else if (lower.startsWith("drop"))
      request = new DropRequest(sqlQuery, escapeProcessing, timeout,
          Connection.LINE_SEPARATOR);
    else if (lower.startsWith("alter"))
      request = new AlterRequest(sqlQuery, escapeProcessing, timeout,
          Connection.LINE_SEPARATOR);
    else if (lower.startsWith("{call"))
    { // Call stored procedure and return
      StoredProcedure proc = new StoredProcedure(sqlQuery, escapeProcessing,
          timeout, Connection.LINE_SEPARATOR,
          (Statement.RETURN_GENERATED_KEYS == generatedKeysFlag) /* isRead */);
      if (connection.controllerNeedsSqlSkeleton || !connection.isDriverProcessed())
        proc.setSqlSkeleton(sqlSkeleton);
      updateCount = connection.execWriteStoredProcedure(proc);
      return updateCount;
    }
    else if (lower.startsWith("}call"))
    { // Call stored procedure and return. This hack is used to allow someone to
      // use execute() to call a write stored procedure.
      StoredProcedure proc = new StoredProcedure("{" + sqlQuery.substring(1),
          escapeProcessing, timeout, Connection.LINE_SEPARATOR,
          (Statement.RETURN_GENERATED_KEYS == generatedKeysFlag) /* isRead */);
      if (connection.controllerNeedsSqlSkeleton || !connection.isDriverProcessed())
        proc.setSqlSkeleton(sqlSkeleton);
      updateCount = connection.execWriteStoredProcedure(proc);
      return updateCount;
    }
    else
      throw new SQLException(
          "executeUpdate only accepts statements starting with insert/update/delete/create/drop/{call ("
              + sqlQuery + ")");

    if (connection.controllerNeedsSqlSkeleton || !connection.isDriverProcessed())
      request.setSqlSkeleton(sqlSkeleton);

    if (generatedKeysFlag == Statement.RETURN_GENERATED_KEYS)
    { // Get the auto generated key back
      generatedKeys = connection.execWriteRequestWithKeys(request);

      /*
       * Usually it is one autoincrement field and one generated key but if it
       * is not acceptable - better way to make another function for return
       * count of updates Or leave execWriteRequestWithKeys to return count and
       * add function for return ResultSet
       */
      return 1;
    }
    else
    { // No generated keys
      updateCount = connection.execWriteRequest(request);
      return updateCount;
    }
  }

  /**
   * Retrieve the connection that created this Statement object
   * 
   * @return a <code>java.sql.Connection</code> object
   * @exception SQLException never
   */
  public java.sql.Connection getConnection() throws SQLException
  {
    return connection;
  }

  /**
   * @see java.sql.Statement#getFetchDirection()
   */
  public int getFetchDirection() throws SQLException
  {
    return fetchDirection;
  }

  /**
   * @see java.sql.Statement#getFetchSize()
   */
  public int getFetchSize() throws SQLException
  {
    return fetchSize;
  }

  /**
   * The maxFieldSize limit (in bytes) is the maximum amount of data returned
   * for any column value; it only applies to <code>BINARY</code>,
   * <code>VARBINARY</code>,<code>LONGVARBINARY</code>,<code>CHAR</code>,
   * <code>VARCHAR</code> and <code>LONGVARCHAR</code> columns. If the limit
   * is exceeded, the excess data is silently discarded.
   * <p>
   * <b>Note: </b> We don't do anything with this value yet.
   * 
   * @return the current max column size limit; zero means unlimited
   * @exception SQLException if a database access error occurs
   */
  public int getMaxFieldSize() throws SQLException
  {
    return maxFieldSize;
  }

  /**
   * The maxRows limit is set to limit the number of rows that any
   * <code>ResultSet</code> can contain. If the limit is exceeded, the excess
   * rows are silently dropped.
   * 
   * @return the current maximum row limit; zero means unlimited
   * @exception SQLException if a database access error occurs
   */
  public int getMaxRows() throws SQLException
  {
    return maxRows;
  }

  /**
   * Multiple results are not suppoted so this method always return false and
   * reset the update count to -1. Any open ResultSet is implicitly closed.
   * 
   * @return false
   * @exception SQLException if an error occurs
   */
  public boolean getMoreResults() throws SQLException
  {
    if (result != null)
      result.close();
    updateCount = -1;
    return false;
  }

  /**
   * The queryTimeout limit is the number of seconds the driver will wait for a
   * Statement to execute. If the limit is exceeded, a <code>SQLException</code>
   * is thrown.
   * 
   * @return the current query timeout limit in seconds; 0 = unlimited
   * @exception SQLException if a database access error occurs
   */
  public int getQueryTimeout() throws SQLException
  {
    return timeout;
  }

  /**
   * Returns the current result as a <code>ResultSet</code>.
   * 
   * @return the current result set; null if there are no more
   * @exception SQLException never
   */
  public java.sql.ResultSet getResultSet() throws SQLException
  {
    return result;
  }

  /**
   * Retrieve the concurrency mode for the <code>ResultSet</code>.
   * 
   * @return <code>CONCUR_READ_ONLY</code> or <code>CONCUR_UPDATABLE</code>
   * @exception SQLException never
   */
  public int getResultSetConcurrency() throws SQLException
  {
    return resultSetConcurrency;
  }

  /**
   * Retrieve the type of the generated <code>ResultSet</code>.
   * 
   * @return one of <code>TYPE_FORWARD_ONLY</code> or
   *         <code>TYPE_SCROLL_INSENSITIVE</code>
   * @exception SQLException never
   */
  public int getResultSetType() throws SQLException
  {
    return resultSetType;
  }

  /**
   * Returns the current result as an update count, if the result is a
   * <code>ResultSet</code> or there are no more results, -1 is returned. It
   * should only be called once per result.
   * 
   * @return the current result as an update count.
   * @exception SQLException if a database access error occurs
   */
  public int getUpdateCount() throws SQLException
  {
    return updateCount;
  }

  /**
   * The first warning reported by calls on this Statement is returned. A
   * Statement's execute methods clear its SQLWarning chain. Subsequent
   * <code>Statement</code> warnings will be chained to this SQLWarning.
   * <p>
   * The Warning chain is automatically cleared each time a statement is
   * (re)executed.
   * <p>
   * <B>Note: </B> if you are processing a <code>ResultSet</code> then any
   * warnings associated with <code>ResultSet</code> reads will be chained on
   * the <code>ResultSet</code> object.
   * 
   * @return the first SQLWarning on null
   * @exception SQLException if a database access error occurs
   */
  public SQLWarning getWarnings() throws SQLException
  {
    return warnings;
  }

  /**
   * Defines the SQL cursor name that will be used by subsequent execute
   * methods. This name can then be used in SQL positioned update/delete
   * statements to identify the current row in the ResultSet generated by this
   * statement. If a database doesn't support positioned update/delete, this
   * method is a no-op.
   * <p>
   * 
   * @param name the new cursor name
   * @exception SQLException not supported
   */
  public void setCursorName(String name) throws SQLException
  {
    cursorName = name;
  }

  /**
   * If escape scanning is on (the default), the driver will do escape
   * substitution before sending the SQL to the database.
   * 
   * @param enable true to enable; false to disable
   * @exception SQLException if a database access error occurs
   */
  public void setEscapeProcessing(boolean enable) throws SQLException
  {
    escapeProcessing = enable;
  }

  /**
   * @see java.sql.Statement#setFetchDirection(int)
   */
  public void setFetchDirection(int direction) throws SQLException
  {
    if ((direction == ResultSet.FETCH_FORWARD)
        || (direction == ResultSet.FETCH_REVERSE)
        || (direction == ResultSet.FETCH_UNKNOWN))
      this.fetchDirection = direction;
    else
      throw new SQLException("Unsupported direction " + direction
          + " in setFetchDirection");
  }

  /**
   * Set the default fetch size for the produced ResultSet.
   * 
   * @param rows number of rows that should be fetched from the database
   * @exception SQLException if a database access error occurs or the condition
   *              0 <= size <= this.getMaxRows is not satisfied
   */
  public void setFetchSize(int rows) throws SQLException
  {
    if (rows < 0
    // The spec forgets the case maxRows = 0.
        || 0 < maxRows && maxRows < rows)
    {
      throw new SQLException("Invalid fetch size value: " + rows);
    }
    // It also forgets the case where maxRows is set < fetchSize AFTERwards,
    // but we don't care about it.

    fetchSize = rows;
  }

  /**
   * Sets the <code>maxFieldSize</code>.
   * 
   * @param max the new max column size limit; 0 means unlimited
   * @exception SQLException if a database access error occurs or the condition
   *              max >= 0 is not satisfied
   */
  public void setMaxFieldSize(int max) throws SQLException
  {
    if (max < 0)
    {
      throw new SQLException("Invalid max field size value: " + max);
    }
    maxFieldSize = max;
  }

  /**
   * Sets the maximum number of rows that any <code>ResultSet</code> can
   * contain.
   * 
   * @param max the new max rows limit; 0 means unlimited
   * @exception SQLException if a database access error occurs or the condition
   *              max >= 0 is not satisfied
   */
  public void setMaxRows(int max) throws SQLException
  {
    if (max < 0)
    {
      throw new SQLException("Invalid max rows limit: " + max);
    }
    // this may break fetchSize <= maxRows
    maxRows = max;
  }

  /**
   * Sets the number of seconds the driver will wait for a
   * <code>Statement</code> object to execute.
   * 
   * @param seconds the new query timeout limit in seconds; 0 means no timeout
   * @exception SQLException if a database access error occurs or the condition
   *              seconds >= 0 is not satisfied
   */
  public void setQueryTimeout(int seconds) throws SQLException
  {
    if (seconds < 0)
    {
      throw new SQLException("Invalid query timeout value: " + seconds);
    }
    timeout = seconds;
  }

  /**
   * @param value an <code>int</code> value
   * @exception SQLException if an error occurs
   */
  public void setResultSetConcurrency(int value) throws SQLException
  {
    switch (value)
    {
      case ResultSet.CONCUR_READ_ONLY :
      case ResultSet.CONCUR_UPDATABLE :
        resultSetConcurrency = value;
        break;
      default :
        throw new SQLException("Invalid ResultSet " + "concurrency mode: "
            + value);
    }
  }

  /**
   * @param value an <code>int</code> value
   * @exception SQLException if an error occurs
   */
  public void setResultSetType(int value) throws SQLException
  {
    switch (value)
    {
      case ResultSet.TYPE_FORWARD_ONLY :
      case ResultSet.TYPE_SCROLL_INSENSITIVE :
        resultSetType = value;
        break;
      case ResultSet.TYPE_SCROLL_SENSITIVE :
        throw new SQLException(
            "TYPE_SCROLL_SENSITIVE is not a supported ResultSet type");
      default :
        throw new SQLException("Invalid ResultSet type");
    }
  }

  // --------------------------JDBC 3.0-----------------------------

  /**
   * Moves to this <code>Statement</code> object's next result, deals with any
   * current <code>ResultSet</code> object(s) according to the instructions
   * specified by the given flag, and returns <code>true</code> if the next
   * result is a <code>ResultSet</code> object.
   * <p>
   * There are no more results when the following is <code>true</code>:
   * 
   * <pre>
   * 
   *  
   *   
   *    
   *     
   *      
   *        (!getMoreResults() &amp;&amp; (getUpdateCount() == -1)
   *       
   *      
   *     
   *    
   *   
   *  
   * </pre>
   * 
   * @param current one of the following <code>Statement</code> constants
   *          indicating what should happen to current <code>ResultSet</code>
   *          objects obtained using the method
   *          <code>getResultSet</code: <code>CLOSE_CURRENT_RESULT</code>,
   *          <code>KEEP_CURRENT_RESULT</code>, or <code>CLOSE_ALL_RESULTS</code>
   * @return <code>true</code> if the next result is a <code>ResultSet</code>
   *         object; <code>false</code> if it is an update count or there are
   *         no more results
   * @exception SQLException if a database access error occurs
   * @since JDK 1.4
   * @see #execute(String)
   */
  public boolean getMoreResults(int current) throws SQLException
  {
    throw new NotImplementedException("getMoreResults");
  }

  /**
   * Retrieves any auto-generated keys created as a result of executing this
   * <code>Statement</code> object. If this <code>Statement</code> object
   * did not generate any keys, an empty <code>ResultSet</code> object is
   * returned.
   * 
   * @return a <code>ResultSet</code> object containing the auto-generated
   *         key(s) generated by the execution of this <code>Statement</code>
   *         object
   * @exception SQLException if a database access error occurs
   * @since JDK 1.4
   */
  public java.sql.ResultSet getGeneratedKeys() throws SQLException
  {
    return generatedKeys;
  }

  /**
   * Executes the given SQL statement and signals the driver with the given flag
   * about whether the auto-generated keys produced by this
   * <code>Statement</code> object should be made available for retrieval.
   * 
   * @param sql must be an SQL <code>INSERT</code>,<code>UPDATE</code> or
   *          <code>DELETE</code> statement or an SQL statement that returns
   *          nothing
   * @param autoGeneratedKeys a flag indicating whether auto-generated keys
   *          should be made available for retrieval; one of the following
   *          constants: <code>Statement.RETURN_GENERATED_KEYS</code>
   * <code>Statement.NO_GENERATED_KEYS</code>
   * @return either the row count for <code>INSERT</code>,
   *         <code>UPDATE</code> or <code>DELETE</code> statements, or
   *         <code>0</code> for SQL statements that return nothing
   * @exception SQLException if a database access error occurs, the given SQL
   *              statement returns a <code>ResultSet</code> object, or the
   *              given constant is not one of those allowed
   * @since JDK 1.4
   */
  public int executeUpdate(String sql, int autoGeneratedKeys)
      throws SQLException
  {
    generatedKeysFlag = autoGeneratedKeys;
    return executeUpdate(sql);
  }

  /**
   * Executes the given SQL statement and signals the driver that the
   * auto-generated keys indicated in the given array should be made available
   * for retrieval. The driver will ignore the array if the SQL statement is not
   * an <code>INSERT</code> statement.
   * 
   * @param sql an SQL <code>INSERT</code>,<code>UPDATE</code> or
   *          <code>DELETE</code> statement or an SQL statement that returns
   *          nothing, such as an SQL DDL statement
   * @param columnIndexes an array of column indexes indicating the columns that
   *          should be returned from the inserted row
   * @return either the row count for <code>INSERT</code>,
   *         <code>UPDATE</code>, or <code>DELETE</code> statements, or 0
   *         for SQL statements that return nothing
   * @exception SQLException if a database access error occurs or the SQL
   *              statement returns a <code>ResultSet</code> object
   * @since JDK 1.4
   */
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
  {
    throw new NotImplementedException("executeUpdate");
  }

  /**
   * Executes the given SQL statement and signals the driver that the
   * auto-generated keys indicated in the given array should be made available
   * for retrieval. The driver will ignore the array if the SQL statement is not
   * an <code>INSERT</code> statement.
   * 
   * @param sql an SQL <code>INSERT</code>,<code>UPDATE</code> or
   *          <code>DELETE</code> statement or an SQL statement that returns
   *          nothing
   * @param columnNames an array of the names of the columns that should be
   *          returned from the inserted row
   * @return either the row count for <code>INSERT</code>,
   *         <code>UPDATE</code>, or <code>DELETE</code> statements, or 0
   *         for SQL statements that return nothing
   * @exception SQLException if a database access error occurs
   * @since JDK 1.4
   */
  public int executeUpdate(String sql, String[] columnNames)
      throws SQLException
  {
    throw new NotImplementedException("executeUpdate");
  }

  /**
   * Executes the given SQL statement, which may return multiple results, and
   * signals the driver that any auto-generated keys should be made available
   * for retrieval. The driver will ignore this signal if the SQL statement is
   * not an <code>INSERT</code> statement.
   * <p>
   * In some (uncommon) situations, a single SQL statement may return multiple
   * result sets and/or update counts. Normally you can ignore this unless you
   * are (1) executing a stored procedure that you know may return multiple
   * results or (2) you are dynamically executing an unknown SQL string.
   * <p>
   * The <code>execute</code> method executes an SQL statement and indicates
   * the form of the first result. You must then use the methods
   * <code>getResultSet</code> or <code>getUpdateCount</code> to retrieve
   * the result, and <code>getMoreResults</code> to move to any subsequent
   * result(s).
   * 
   * @param sql any SQL statement
   * @param autoGeneratedKeys a constant indicating whether auto-generated keys
   *          should be made available for retrieval using the method
   *          <code>getGeneratedKeys</code>; one of the following constants:
   *          <code>Statement.RETURN_GENERATED_KEYS</code> or
   *          <code>Statement.NO_GENERATED_KEYS</code>
   * @return <code>true</code> if the first result is a <code>ResultSet</code>
   *         object; <code>false</code> if it is an update count or there are
   *         no results
   * @exception SQLException if a database access error occurs
   * @see #getResultSet
   * @see #getUpdateCount
   * @see #getMoreResults()
   * @see #getGeneratedKeys
   * @since JDK 1.4
   */
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
  {
    generatedKeysFlag = autoGeneratedKeys;
    return execute(sql);
  }

  /**
   * Executes the given SQL statement, which may return multiple results, and
   * signals the driver that the auto-generated keys indicated in the given
   * array should be made available for retrieval. This array contains the
   * indexes of the columns in the target table that contain the auto-generated
   * keys that should be made available. The driver will ignore the array if the
   * given SQL statement is not an <code>INSERT</code> statement.
   * <p>
   * Under some (uncommon) situations, a single SQL statement may return
   * multiple result sets and/or update counts. Normally you can ignore this
   * unless you are (1) executing a stored procedure that you know may return
   * multiple results or (2) you are dynamically executing an unknown SQL
   * string.
   * <p>
   * The <code>execute</code> method executes an SQL statement and indicates
   * the form of the first result. You must then use the methods
   * <code>getResultSet</code> or <code>getUpdateCount</code> to retrieve
   * the result, and <code>getMoreResults</code> to move to any subsequent
   * result(s).
   * 
   * @param sql any SQL statement
   * @param columnIndexes an array of the indexes of the columns in the inserted
   *          row that should be made available for retrieval by a call to the
   *          method <code>getGeneratedKeys</code>
   * @return <code>true</code> if the first result is a <code>ResultSet</code>
   *         object; <code>false</code> if it is an update count or there are
   *         no results
   * @exception SQLException if a database access error occurs
   * @see #getResultSet
   * @see #getUpdateCount
   * @see #getMoreResults()
   * @since JDK 1.4
   */
  public boolean execute(String sql, int[] columnIndexes) throws SQLException
  {
    throw new NotImplementedException("execute");
  }

  /**
   * Executes the given SQL statement, which may return multiple results, and
   * signals the driver that the auto-generated keys indicated in the given
   * array should be made available for retrieval. This array contains the names
   * of the columns in the target table that contain the auto-generated keys
   * that should be made available. The driver will ignore the array if the
   * given SQL statement is not an <code>INSERT</code> statement.
   * <p>
   * In some (uncommon) situations, a single SQL statement may return multiple
   * result sets and/or update counts. Normally you can ignore this unless you
   * are (1) executing a stored procedure that you know may return multiple
   * results or (2) you are dynamically executing an unknown SQL string.
   * <p>
   * The <code>execute</code> method executes an SQL statement and indicates
   * the form of the first result. You must then use the methods
   * <code>getResultSet</code> or <code>getUpdateCount</code> to retrieve
   * the result, and <code>getMoreResults</code> to move to any subsequent
   * result(s).
   * 
   * @param sql any SQL statement
   * @param columnNames an array of the names of the columns in the inserted row
   *          that should be made available for retrieval by a call to the
   *          method <code>getGeneratedKeys</code>
   * @return <code>true</code> if the next result is a <code>ResultSet</code>
   *         object; <code>false</code> if it is an update count or there are
   *         no more results
   * @exception SQLException if a database access error occurs
   * @see #getResultSet
   * @see #getUpdateCount
   * @see #getMoreResults()
   * @see #getGeneratedKeys
   * @since JDK 1.4
   */
  public boolean execute(String sql, String[] columnNames) throws SQLException
  {
    throw new NotImplementedException("execute");
  }

  /**
   * Retrieves the result set holdability for <code>ResultSet</code> objects
   * generated by this <code>Statement</code> object.
   * 
   * @return either <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
   *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
   * @exception SQLException if a database access error occurs
   * @since JDK 1.4
   */
  public int getResultSetHoldability() throws SQLException
  {
    throw new NotImplementedException("getResultSetHoldability");
  }

  /**
   * Test if this statement is closed.
   * 
   * @return <code>true</code> if this statement is closed
   */
  private boolean isClosed()
  {
    return (connection == null);
  }
}