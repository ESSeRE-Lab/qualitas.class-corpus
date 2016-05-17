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
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.common.sql;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;

/**
 * An <code>UnknownRequest</code> is an SQL request that does not match any
 * SQL query known by this software.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @version 1.0
 */
public class UnknownRequest extends AbstractRequest implements Serializable
{
  private static final long serialVersionUID = -1990341658455593552L;

  /**
   * Creates a new <code>UnknownRequest</code> instance.
   * 
   * @param sqlQuery the SQL query
   * @param escapeProcessing should the driver to escape processing before
   *          sending to the database?
   * @param timeout an <code>int</code> value
   * @param lineSeparator the line separator used in the query
   */
  public UnknownRequest(String sqlQuery, boolean escapeProcessing, int timeout,
      String lineSeparator)
  {
    super(sqlQuery, escapeProcessing, timeout, lineSeparator,
        RequestType.UNDEFINED);
  }

  /**
   * @see AbstractRequest
   */
  public UnknownRequest(CJDBCInputStream in) throws IOException
  {
    super(in, RequestType.UNDEFINED);
    receiveResultSetParams(in);
  }

  /**
   * @see AbstractRequest
   */
  public void sendToStream(CJDBCOutputStream out, boolean needSqlSkeleton)
      throws IOException
  {
    super.sendToStream(out, needSqlSkeleton);
    sendResultSetParams(out);
  }

  /**
   * @return <code>false</code>
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#needsMacroProcessing()
   */
  public boolean needsMacroProcessing()
  {
    return false;
  }

  /**
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#returnsResultSet()
   */
  public boolean returnsResultSet()
  {
    // this is the more cautious
    return true;
  }

  /**
   * Throws always an <code>SQLException</code>: it is not possible to parse
   * an unknown request because we don't know its syntax or semantic.
   * 
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#parse(org.objectweb.cjdbc.common.sql.schema.DatabaseSchema,
   *      int, boolean)
   */
  public void parse(DatabaseSchema schema, int granularity,
      boolean isCaseSensitive) throws SQLException
  {
    throw new SQLException("Unable to parse an unknown request");
  }

  /**
   * Throws always an <code>SQLException</code>: it is not possible to parse
   * an unknown request because we don't know its syntax or semantic.
   * 
   * @see AbstractRequest#cloneParsing(AbstractRequest)
   */
  public void cloneParsing(AbstractRequest request)
  {
    throw new RuntimeException(
        "Unable to clone the parsing of an unknown request");
  }
}