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
 * Contributor(s): Julie Marguerite, Mathieu Peltier, Marc Herbert
 */

package org.objectweb.cjdbc.common.sql;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;

/**
 * An <code>AbstractRequest</code> defines the skeleton of an SQL request.
 * Requests have to be serializable (at least) for inter-controller
 * communications.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert </a>
 * @version 1.0
 */
public abstract class AbstractRequest implements Serializable
{
  // 
  // This object is currently (de-)serialized in two different ways:
  // - inter-controller commmunication uses java serialization (writeObject)
  // since it is convenient, terse and reliable.
  // - driver communication uses the manual serialization below, for
  // compatibility with C
  //
  // Ideally:
  // (1) unneeded fields for Java-Java communication are all tagged as
  // "transient"
  // (2) C-Java and Java-Java need to send the exact same fields.
  // And so:
  // (3) keeping up-to-date manual serialization methods below is easy: just
  // check
  // "transient" tags.

  /*
   * "In theory, practice and theory are the same, but in practice they are
   * different" (Larry McVoy).
   */
  /*
   * TODO: tag as "transient" fields that should be. And then TODO: if any, spot
   * each exception to (3), that is: spot each non-transient field that we don't
   * need for driver-controller com, and document why.
   */

  /** @see RequestType */
  int                      requestType      = RequestType.UNDEFINED;

  /** Request unique id (set by the controller). */
  protected transient long id;

  /** SQL query (should be set in constructor). */
  protected String         sqlQuery;

  /** SQL query skeleton as it appears in PreparedStatements. */
  protected String         sqlSkeleton      = null;

  /**
   * Login used to issue this request (must be set by the
   * VirtualDatabaseWorkerThread).
   */
  protected String         login;

  /** Whether this request is cacheable or not. */
  protected int            cacheable        = RequestType.UNCACHEABLE;

  /** Whether the SQL content has been parsed or not. */
  protected boolean        isParsed         = false;

  /*
   * ResultSet Parameters
   */
  int                      maxRows;
  int                      fetchSize;
  String                   cursorName;

  //
  // Connection related parameters
  //

  /** True if the connection has been set to read-only */
  protected boolean        isReadOnly       = false;

  /**
   * Whether this request has been sent in <code>autocommit</code> mode or
   * not.
   */
  protected boolean        isAutoCommit;

  /**
   * Transaction identifier if this request belongs to a transaction. The value
   * is set by the VirtualDatabaseWorkerThread.
   */
  protected long           transactionId;

  /**
   * Transaction isolation level to use when executing the query inside a
   * transaction. The value is set by the VirtualDatabaseWorkerThread.
   */
  protected int            transactionIsolation;

  /**
   * Timeout for this request in seconds, value 0 means no timeout (should be
   * set in constructor). This timeout is in seconds, reflecting the jdbc-spec,
   * and is passed as-is to the backends jdbc-driver. Internally converted to ms
   * via getTimeoutMs().
   */
  protected int            timeoutInSeconds;

  /**
   * Should the backend driver do escape processing before sending to the
   * database? Simply forwarded to backend driver. No setter for this member,
   * should be set in constructor.
   * 
   * @see java.sql.Statement#setEscapeProcessing(boolean)
   */
  protected boolean        escapeProcessing = true;

  /**
   * Set and sent by the driver to the controller. Required for parsing the
   * request.
   */
  private String           lineSeparator    = null;

  /**
   * If set to true, this query is/was interpreted on the driver side, if false
   * the various parameters are encoded and passed as is to the database native
   * driver by the controller. Look for "proxy mode" in the documentation.
   */
  private boolean          driverProcessed  = true;

  /**
   * Default constructor Creates a new <code>AbstractRequest</code> object
   * 
   * @param sqlQuery the SQL query
   * @param escapeProcessing should the driver to escape processing before
   *          sending to the database ?
   * @param timeout an <code>int</code> value
   * @param lineSeparator the line separator used in the query
   * @param requestType the request type as defined in RequestType class
   * @see RequestType
   */
  public AbstractRequest(String sqlQuery, boolean escapeProcessing,
      int timeout, String lineSeparator, int requestType)
  {
    this.sqlQuery = sqlQuery;
    this.escapeProcessing = escapeProcessing;
    this.timeoutInSeconds = timeout;
    this.lineSeparator = lineSeparator;
    this.requestType = requestType;
  }

  /**
   * Creates a new <code>AbstractRequest</code> object, deserializing it from
   * an input stream. Has to mirror the serialization method below.
   * 
   * @param in the input stream to read from
   * @param requestType the request type as defined in RequestType class
   * @throws IOException if a network error occurs
   * @see RequestType
   */

  public AbstractRequest(CJDBCInputStream in, int requestType)
      throws IOException
  {
    this.sqlQuery = in.readUTF();
    this.escapeProcessing = in.readBoolean();
    this.lineSeparator = in.readUTF();
    this.timeoutInSeconds = in.readInt();

    this.isAutoCommit = in.readBoolean();
    this.driverProcessed = in.readBoolean();

    // Does this request has a template with question marks "?"
    // true for PreparedStatements
    // (AND did we ask for them at connection time?)
    if (in.readBoolean())
      this.sqlSkeleton = in.readUTF();
    
    // success, we received it all
    
    this.requestType = requestType;
  }

  /**
   * Serialize the request on the output stream by sending only the needed
   * parameters to reconstruct it on the controller. Has to mirror the
   * deserialization method above.
   * 
   * @param out destination CJDBCOutputStream
   * @param controllerWantsSqlSkeleton true if controller wants SQL templates
   * @throws IOException if fails
   */

  public void sendToStream(CJDBCOutputStream out,
      boolean controllerWantsSqlSkeleton) throws IOException
  {
    out.writeUTF(sqlQuery);
    out.writeBoolean(escapeProcessing);
    out.writeUTF(lineSeparator);
    out.writeInt(timeoutInSeconds);

    out.writeBoolean(isAutoCommit);
    out.writeBoolean(driverProcessed);

    // If Statements are not processed by the driver, the controller
    // will need the skeleton.
    if ((controllerWantsSqlSkeleton || !isDriverProcessed())
        && sqlSkeleton != null)
    {
      out.writeBoolean(true);
      out.writeUTF(sqlSkeleton);
    }
    else
      out.writeBoolean(false);

  }

  /**
   * Also fetch ResultSet parameters from the stream. Optionally used by
   * deserializers of those derived requests that expect a ResultSet.
   * 
   * @param in input stream
   * @throws IOException stream error
   */
  void receiveResultSetParams(CJDBCInputStream in) throws IOException
  {
    this.maxRows = in.readInt();
    this.fetchSize = in.readInt();

    if (in.readBoolean()) // do we have a cursor name ?
      this.cursorName = in.readUTF();
  }

  /**
   * Also serialize ResultSet parameters to the stream. Optionally used by
   * serializers of those derived requests that expect a ResultSet.
   * 
   * @param out output stream
   * @throws IOException stream error
   */
  void sendResultSetParams(CJDBCOutputStream out) throws IOException
  {
    out.writeInt(maxRows);
    out.writeInt(fetchSize);

    if (this.cursorName != null) // do we have a cursor name ?
    {
      out.writeBoolean(true);
      out.writeUTF(cursorName);
    }
    else
      out.writeBoolean(false);
  }

  /**
   * Returns <code>true</code> if this request requires macro (RAND(), NOW(),
   * ...) processing.
   * 
   * @return <code>true</code> if macro processing is required
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#handleMacros(AbstractRequest)
   */
  public abstract boolean needsMacroProcessing();

  /**
   * Does this request returns a ResultSet?
   * 
   * @return true is this request will return a ResultSet
   */
  public abstract boolean returnsResultSet();

  /**
   * Returns <code>true</code> if this request in a <code>ALTER</code>
   * statement.
   * 
   * @return a <code>boolean</code> value
   */
  public final boolean isAlter()
  {
    return RequestType.isAlter(this.requestType);
  }

  /**
   * Returns <code>true</code> if this request in a <code>CREATE</code>
   * statement.
   * 
   * @return a <code>boolean</code> value
   */
  public final boolean isCreate()
  {
    return RequestType.isCreate(this.requestType);
  }

  /**
   * Returns <code>true</code> if this request in a <code>DELETE</code>
   * statement.
   * 
   * @return a <code>boolean</code> value
   */
  public final boolean isDelete()
  {
    return RequestType.isDelete(this.requestType);
  }

  /**
   * Returns <code>true</code> if this request is a DDL (Data Definition
   * Language) statement such as CREATE, ALTER or DROP. Not supported yet are:
   * TRUNCATE, COMMENT, GRANT and REVOKE (see
   * http://www.orafaq.com/faq/Server_Utilities/SQL/faq53.htm)
   * <p>
   * Note that stored procedures are both considered as DDL and DML as they can
   * include both.
   * 
   * @return true if this request is a DDL
   */
  public final boolean isDDL()
  {
    return RequestType.isDDL(this.requestType);
  }

  /**
   * Returns <code>true</code> if this request is a DML (Data Manipulation
   * Language) statement such SELECT, INSERT, UPDATE or DELETE (see
   * http://www.orafaq.com/faq/Server_Utilities/SQL/faq53.htm)
   * <p>
   * Note that stored procedures are both considered as DDL and DML as they can
   * include both.
   * 
   * @return true if this request is a DDL
   */
  public final boolean isDML()
  {
    return RequestType.isDML(this.requestType);
  }

  /**
   * Returns <code>true</code> if this request in a <code>DROP</code>
   * statement.
   * 
   * @return a <code>boolean</code> value
   */
  public final boolean isDrop()
  {
    return RequestType.isDrop(this.requestType);
  }

  /**
   * Returns <code>true</code> if this request in an <code>INSERT</code>
   * statement.
   * 
   * @return a <code>boolean</code> value
   */
  public final boolean isInsert()
  {
    return RequestType.isInsert(this.requestType);
  }

  /**
   * Returns <code>true</code> if the request SQL content has been already
   * parsed.
   * 
   * @return a <code>boolean</code> value
   */
  public boolean isParsed()
  {
    return isParsed;
  }

  /**
   * Returns <code>true</code> if the connection is set to read-only
   * 
   * @return a <code>boolean</code> value
   */
  public boolean isReadOnly()
  {
    return isReadOnly;
  }

  /**
   * Returns <code>true</code> if this request in a <code>SELECT</code>
   * statement.
   * 
   * @return a <code>boolean</code> value
   */
  public final boolean isSelect()
  {
    return RequestType.isSelect(this.requestType);
  }

  /**
   * Returns <code>true</code> if this request in an <code>UPDATE</code>
   * statement.
   * 
   * @return a <code>boolean</code> value
   */
  public final boolean isUpdate()
  {
    return RequestType.isUpdate(this.requestType);
  }

  /**
   * Sets the read-only mode for this request.
   * 
   * @param isReadOnly <code>true</code> if connection is read-only
   */
  public void setIsReadOnly(boolean isReadOnly)
  {
    this.isReadOnly = isReadOnly;
  }

  /**
   * Returns the cacheable status of this request. It can be:
   * {@link org.objectweb.cjdbc.common.sql.RequestType#CACHEABLE},
   * {@link org.objectweb.cjdbc.common.sql.RequestType#UNCACHEABLE}or
   * {@link org.objectweb.cjdbc.common.sql.RequestType#UNIQUE_CACHEABLE}
   * 
   * @return a <code>int</code> value
   */
  public int getCacheAbility()
  {
    return cacheable;
  }

  /**
   * Set the cacheable status of this request. It can be:
   * {@link org.objectweb.cjdbc.common.sql.RequestType#CACHEABLE},
   * {@link org.objectweb.cjdbc.common.sql.RequestType#UNCACHEABLE}or
   * {@link org.objectweb.cjdbc.common.sql.RequestType#UNIQUE_CACHEABLE}
   * 
   * @param cacheAbility a <code>int</code> value
   */
  public void setCacheAbility(int cacheAbility)
  {
    this.cacheable = cacheAbility;
  }

  /**
   * Returns <code>true</code> if the driver should escape processing before
   * sending to the database?
   * 
   * @return a <code>boolean</code> value
   */
  public boolean getEscapeProcessing()
  {
    return escapeProcessing;
  }

  /**
   * Returns the unique id of this request.
   * 
   * @return the request id
   */
  public long getId()
  {
    return id;
  }

  /**
   * Sets the unique id of this request.
   * 
   * @param id the id to set
   */
  public void setId(long id)
  {
    this.id = id;
  }

  /**
   * Returns <code>true</code> if the request should be executed in
   * <code>autocommit</code> mode.
   * 
   * @return a <code>boolean</code> value
   */
  public boolean isAutoCommit()
  {
    return isAutoCommit;
  }

  /**
   * Sets the autocommit mode for this request.
   * 
   * @param isAutoCommit <code>true</code> if <code>autocommit</code> should
   *          be used
   */
  public void setIsAutoCommit(boolean isAutoCommit)
  {
    this.isAutoCommit = isAutoCommit;
  }

  /**
   * Returns the login used to issue this request.
   * 
   * @return a <code>String</code> value
   */
  public String getLogin()
  {
    return login;
  }

  /**
   * Returns the lineSeparator value.
   * 
   * @return Returns the lineSeparator.
   */
  public String getLineSeparator()
  {
    return lineSeparator;
  }

  /**
   * Sets the login to use to issue this request.
   * 
   * @param login a <code>String</code> value
   */
  public void setLogin(String login)
  {
    this.login = login;
  }

  /**
   * Gets the SQL code of this request.
   * 
   * @return the SQL query
   */
  public String getSQL()
  {
    return sqlQuery;
  }

  /**
   * Get a short form of this request if the SQL statement exceeds
   * nbOfCharacters.
   * 
   * @param nbOfCharacters number of characters to include in the short form.
   * @return the nbOfCharacters first characters of the SQL statement
   */
  public String getSQLShortForm(int nbOfCharacters)
  {
    if ((nbOfCharacters == 0) || (sqlQuery.length() < nbOfCharacters))
      return sqlQuery;
    else
      return sqlQuery.substring(0, nbOfCharacters) + "...";
  }

  /**
   * Get the maximum number of rows the ResultSet can contain.
   * 
   * @return maximum number of rows
   * @see java.sql.Statement#getMaxRows()
   */
  public int getMaxRows()
  {
    return maxRows;
  }

  /**
   * Set the maximum number of rows in the ResultSet. Used only by Statement.
   * 
   * @param rows maximum number of rows
   * @see java.sql.Statement#setMaxRows(int)
   */
  public void setMaxRows(int rows)
  {
    maxRows = rows;
  }

  /**
   * Set the SQL code of this request. Warning! The request parsing validity is
   * not checked. The caller has to recall
   * {@link #parse(DatabaseSchema, int, boolean)}if needed.
   * 
   * @param sql SQL statement
   * @see org.objectweb.cjdbc.controller.loadbalancer.AbstractLoadBalancer#handleMacros(AbstractRequest)
   */
  public void setSQL(String sql)
  {
    this.sqlQuery = sql;
  }

  /**
   * Gets the timeout for this request in seconds.
   * 
   * @return timeout in seconds (0 means no timeout)
   */
  public int getTimeout()
  {
    return timeoutInSeconds;
  }

  /**
   * Sets the new timeout in seconds for this request.
   * 
   * @param timeout an <code>int</code> value
   * @see org.objectweb.cjdbc.controller.scheduler
   */
  public void setTimeout(int timeout)
  {
    this.timeoutInSeconds = timeout;
  }

  /**
   * Gets the identifier of the transaction if this request belongs to a
   * transaction, or -1 if this request does not belong to a transaction.
   * 
   * @return transaction identifier or -1
   */
  public long getTransactionId()
  {
    return transactionId;
  }

  /**
   * Sets the transaction identifier this request belongs to.
   * 
   * @param id transaction id
   */
  public void setTransactionId(long id)
  {
    transactionId = id;
  }

  /**
   * Two requests are equal if they have the same SQL statement, transaction id
   * and login.
   * 
   * @param other an object
   * @return a <code>boolean</code> value
   */
  public boolean equals(Object other)
  {
    if ((other == null) || !(other instanceof AbstractRequest))
      return false;

    AbstractRequest r = (AbstractRequest) other;
    return id == r.getId();
  }

  /**
   * Parses the SQL request and extract the selected columns and tables given
   * the <code>DatabaseSchema</code> of the database targeted by this request.
   * <p>
   * An exception is thrown when the parsing fails. Warning, this method does
   * not check the validity of the request. In particular, invalid request could
   * be parsed without throwing an exception. However, valid SQL request should
   * never throw an exception.
   * 
   * @param schema a <code>DatabaseSchema</code> value
   * @param granularity parsing granularity as defined in
   *          <code>ParsingGranularities</code>
   * @param isCaseSensitive true if parsing must be case sensitive
   * @exception SQLException if the parsing fails
   */
  public abstract void parse(DatabaseSchema schema, int granularity,
      boolean isCaseSensitive) throws SQLException;

  /**
   * Clones the parsing of a request.
   * 
   * @param request the parsed request to clone
   */
  public abstract void cloneParsing(AbstractRequest request);

  /**
   * If the query has a skeleton defined, return the skeleton with all carriage
   * returns and tabs replaced with spaces. If no SQL skeleton is defined, we
   * perform the same processing on the instanciated SQL statement.
   * <p>
   * Note that if no modification has to be done, the original string is
   * returned else a new string is constructed with the replaced elements.
   * 
   * @return statement with CR replaced by spaces
   */
  public String trimCarriageReturnAndTabs()
  {
    if (sqlSkeleton != null)
      return replaceStringWithSpace(replaceStringWithSpace(sqlSkeleton,
          lineSeparator), "\t");
    else
      return replaceStringWithSpace(replaceStringWithSpace(sqlQuery,
          lineSeparator), "\t");
  }

  /**
   * Replaces any given <code>String</code> by a space in a given
   * <code>String</code>.
   * 
   * @param s the <code>String</code> to transform
   * @param toReplace the <code>String</code> to replace with spaces
   * @return the transformed <code>String</code>
   */
  private String replaceStringWithSpace(String s, String toReplace)
  {
    int toReplaceLength = toReplace.length();
    int idx = s.indexOf(toReplace);
    if (idx == -1)
      return s;
    else
    {
      if (idx == 0)
        // is the first character
        return replaceStringWithSpace(s.substring(toReplaceLength), toReplace);
      else if (idx == (s.length() - toReplaceLength))
        // is the last character
        return s.substring(0, s.length() - toReplaceLength);
      else
        // is somewhere in the string
        return s.substring(0, idx)
            + " "
            + replaceStringWithSpace(s.substring(idx + toReplaceLength),
                toReplace);
    }
  }

  /**
   * @return the SQL query skeleton given in a <code>PreparedStatement</code>.
   */
  public String getSqlSkeleton()
  {
    return sqlSkeleton;
  }

  /**
   * @param skel set the SQL query skeleton given in a
   *          <code>PreparedStatement</code>.
   */
  public void setSqlSkeleton(String skel)
  {
    sqlSkeleton = skel;
  }

  /**
   * Returns the driverProcessed value.
   * 
   * @return Returns the driverProcessed.
   */
  public boolean isDriverProcessed()
  {
    return driverProcessed;
  }

  /**
   * Sets the driverProcessed value.
   * 
   * @param driverProcessed The driverProcessed to set.
   */
  public void setDriverProcessed(boolean driverProcessed)
  {
    this.driverProcessed = driverProcessed;
  }

  /**
   * Sets the fetchSize value.
   * 
   * @param fetchSize The fetchSize to set.
   * @see org.objectweb.cjdbc.driver.Statement
   */
  public void setFetchSize(int fetchSize)
  {
    this.fetchSize = fetchSize;
  }

  /**
   * Returns the fetchSize value.
   * 
   * @return Returns the fetchSize.
   */
  public int getFetchSize()
  {
    return fetchSize;
  }

  /**
   * Returns the transaction isolation level.
   * 
   * @return Returns the transaction isolation.
   */
  public int getTransactionIsolation()
  {
    return transactionIsolation;
  }

  /**
   * Sets the transaction isolation level that must be used to execute this
   * request
   * 
   * @param isolationLevel the transaction isolation level
   */
  public void setTransactionIsolation(int isolationLevel)
  {
    this.transactionIsolation = isolationLevel;
  }

  /**
   * Returns the cursorName value.
   * 
   * @return Returns the cursorName.
   */
  public String getCursorName()
  {
    return cursorName;
  }

  /**
   * Sets the cursorName value.
   * 
   * @param cursorName The cursorName to set.
   */
  public void setCursorName(String cursorName)
  {
    this.cursorName = cursorName;
  }

  /**
   * Displays some debugging information about this request.
   */
  public void debug()
  {
    System.out.println("Request: " + sqlQuery);
    System.out.print("Cacheable status: ");
    System.out.println(RequestType.getInformation(cacheable));
  }

}
