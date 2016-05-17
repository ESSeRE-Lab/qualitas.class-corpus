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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.sql;

import java.io.IOException;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;

/**
 * A <code>StoredProcedure</code> is a SQL request with the following syntax:
 * 
 * <pre>
 * 
 *   {call &lt;procedure-name&gt;[&lt;arg1&gt;,&lt;arg2&gt;, ...]}
 *  
 * </pre>
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class StoredProcedure extends AbstractRequest
{
  private static final long serialVersionUID = 8425933724676827694L;

  /** <code>true</code> if this request might block. */
  private transient boolean blocking         = true;

  private transient String  procedureName    = null;
  // blank final for safety
  private final boolean     returnsRS;

  /**
   * Creates a new <code>StoredProcedure</code> instance.
   * 
   * @param sqlQuery the SQL request
   * @param escapeProcessing should the driver to escape processing before
   *          sending to the database ?
   * @param timeout an <code>int</code> value
   * @param lineSeparator the line separator used in the query
   * @param isRead does the request expects a ResultSet ?
   * @see #parse
   */
  public StoredProcedure(String sqlQuery, boolean escapeProcessing,
      int timeout, String lineSeparator, boolean isRead)
  {
    super(sqlQuery, escapeProcessing, timeout, lineSeparator,
        RequestType.STORED_PROCEDURE);
    this.returnsRS = isRead;
  }

  /**
   * @see AbstractRequest
   */
  public StoredProcedure(CJDBCInputStream in) throws IOException
  {
    super(in, RequestType.STORED_PROCEDURE);
    this.returnsRS = in.readBoolean();
    if (returnsRS)
      receiveResultSetParams(in);

  }

  /**
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#sendToStream(org.objectweb.cjdbc.common.stream.CJDBCOutputStream,
   *      boolean)
   */
  public void sendToStream(CJDBCOutputStream out, boolean needSkeleton)
      throws IOException
  {
    super.sendToStream(out, needSkeleton);
    out.writeBoolean(returnsRS);
    if (returnsRS)
      sendResultSetParams(out);
  }

  /**
   * @return <code>false</code>
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#needsMacroProcessing()
   */
  public boolean needsMacroProcessing()
  {
    return true;
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#returnsResultSet()
   */
  public boolean returnsResultSet()
  {
    return returnsRS;
  }

  /**
   * Get the stored procedure name
   * 
   * @return the stored procedure name
   */
  public String getProcedureName()
  {
    if (procedureName == null)
      try
      {
        parse(null, 0, true);
      }
      catch (SQLException e)
      {
        return null;
      }
    return procedureName;
  }

  /**
   * Tests if this request might block.
   * 
   * @return <code>true</code> if this request might block
   */
  public boolean mightBlock()
  {
    return blocking;
  }

  /**
   * Sets if this request might block.
   * 
   * @param blocking a <code>boolean</code> value
   */
  public void setBlocking(boolean blocking)
  {
    this.blocking = blocking;
  }

  /**
   * Just get the stored procedure name.
   * 
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#parse(org.objectweb.cjdbc.common.sql.schema.DatabaseSchema,
   *      int, boolean)
   */
  public void parse(DatabaseSchema schema, int granularity,
      boolean isCaseSensitive) throws SQLException
  {
    sqlQuery = sqlQuery.trim();
    if (sqlQuery.length() < 6) // 6='call x'
      throw new SQLException("Malformed stored procedure call '" + sqlQuery
          + "'");

    int parenthesis = sqlQuery.indexOf('(');
    if (parenthesis == -1)
      procedureName = sqlQuery.substring(5); // 5 = 'call '
    else
      procedureName = sqlQuery.substring(5, parenthesis); // 5 = 'call '
    // Remove possible extra spaces between call and procedure name
    procedureName = procedureName.trim();
  }

  /**
   * Always throws a <code>SQLException</code>: it is useless to parse a
   * stored procedure call since we can't know which tables are affected by this
   * procedure.
   * 
   * @see AbstractRequest#cloneParsing(AbstractRequest)
   */
  public void cloneParsing(AbstractRequest request)
  {
    throw new RuntimeException(
        "Unable to clone the parsing of a stored procedure call");
  }

}