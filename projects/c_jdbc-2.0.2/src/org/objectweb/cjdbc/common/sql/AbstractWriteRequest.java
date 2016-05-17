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
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.common.sql;

import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.stream.CJDBCInputStream;

/**
 * An <code>AbstractWriteRequest</code> defines the skeleton of read requests
 * that are sent from the driver to the controller.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public abstract class AbstractWriteRequest extends AbstractRequest
{
  /** Name of the table involved in this write query. */
  protected transient String    tableName;

  /**
   * <code>ArrayList</code> of <code>TableColumn</code> involved in this
   * write query.
   */
  protected transient ArrayList columns;

  /** <code>true</code> if this request might block. */
  protected transient boolean   blocking = true;

  /** Primary key value */
  protected transient String    pkValue  = null;

  /**
   * Creates a new <code>AbstractWriteRequest</code> object
   * 
   * @param sqlQuery the SQL query
   * @param escapeProcessing should the driver to escape processing before
   *          sending to the database ?
   * @param timeout an <code>int</code> value
   * @param lineSeparator the line separator used in the query
   * @param requestType the request type as defined in RequestType class
   * @see RequestType
   */
  public AbstractWriteRequest(String sqlQuery, boolean escapeProcessing,
      int timeout, String lineSeparator, int requestType)
  {
    super(sqlQuery, escapeProcessing, timeout, lineSeparator, requestType);
  }

  /**
   * Creates a new <code>AbstractWriteRequest</code> object
   * 
   * @param in the input stream to read the request from
   * @param requestType the request type as defined in RequestType class
   * @throws IOException if an error occurs
   * @see RequestType
   * @see AbstractRequest#AbstractRequest(CJDBCInputStream, int)
   */
  public AbstractWriteRequest(CJDBCInputStream in, int requestType)
      throws IOException
  {
    super(in, requestType);
  }

  /**
   * Returns the name of the table affected by this statement.
   * 
   * @return a <code>String</code> value
   */
  public String getTableName()
  {
    return tableName;
  }

  /**
   * Returns an <code>ArrayList</code> of <code>TableColumn</code> objects
   * representing the columns affected by this statement.
   * 
   * @return an <code>ArrayList</code> value
   */
  public ArrayList getColumns()
  {
    return columns;
  }

  /**
   * Clones table name and columns from an already parsed request.
   * 
   * @param abstractWriteRequest the already parsed request
   */
  protected void cloneTableNameAndColumns(
      AbstractWriteRequest abstractWriteRequest)
  {
    tableName = abstractWriteRequest.getTableName();
    columns = abstractWriteRequest.getColumns();
    pkValue = abstractWriteRequest.getPk();
    cacheable = abstractWriteRequest.getCacheAbility();
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
   * @return Returns the pk.
   */
  public String getPk()
  {
    return pkValue;
  }

}