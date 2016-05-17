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
 * Contributor(s): Diego Malpica.
 */

package org.objectweb.cjdbc.controller.virtualdatabase;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.controller.cache.metadata.MetadataCache;
import org.objectweb.cjdbc.driver.DriverResultSet;
import org.objectweb.cjdbc.driver.Field;

/**
 * A <code>ControllerResultSet</code> is a lightweight ResultSet for the
 * controller side. It only contains row data and column metadata. The real
 * ResultSet is constructed on by the driver on the client side from the
 * ControllerResultSet information.
 * 
 * @see org.objectweb.cjdbc.driver.DriverResultSet
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ControllerResultSet implements Serializable
{
  private static final ArrayList EMPTY_LIST     = new ArrayList(0);
  /** The results */
  private ArrayList           data              = EMPTY_LIST;
  /** The fields */
  private Field[]             fields            = null;
  /** Cursor name for this ResultSet (not used yet) */
  private String              cursorName        = null;
  /** Fetch size if we need to fetch only a subset of the ResultSet */
  private int                 fetchSize         = 0;
  /** Backend ResultSet. We need to hold it when streaming. */
  private transient ResultSet dbResultSet       = null;
  /** Optional statement dbResultSet is attached to if in streaming mode */
  private transient Statement owningStatement   = null;
  /** True if the underlying database ResultSet is closed */
  private boolean             dbResultSetClosed = true;
  /** True if there is still more data to fetch from dbResultSet */
  private boolean             hasMoreData       = false;
  /** Maximum number of rows remaining to fetch */
  private int                 maxRows           = 0;
  /**
   * When streaming, hold a reference to the DriverResultSet we built so we
   * don't have to re-compute everything.
   */
  DriverResultSet             driverResultSet;

  /**
   * Build a C-JDBC ResultSet from a database specific ResultSet. The metadata
   * can be retrieved from the MetadataCache if provided. If a metadata cache is
   * provided but the data is not in the cache, the MetadataCache is updated
   * accordingly. The remaining code is a straightforward copy of both metadata
   * and data.
   * <p>
   * The statement used to execute the query will be closed when the ResultSet
   * has been completely copied or when the ResultSet is closed while in
   * streaming mode.
   * 
   * @param request Request to which this ResultSet belongs
   * @param rs The database specific ResultSet
   * @param metadataCache MetadataCache (null if none)
   * @param s Statement used to get rs
   * @throws SQLException if an error occurs
   */
  public ControllerResultSet(AbstractRequest request, java.sql.ResultSet rs,
      MetadataCache metadataCache, Statement s) throws SQLException
  {
    this.owningStatement = s;
    try
    {
      if (rs == null)
        throw new SQLException("Null ResultSet");

      // This is already a result coming from another controller.
      //if (rs instanceof org.objectweb.cjdbc.driver.ResultSet)
      //  return (org.objectweb.cjdbc.driver.ResultSet) rs;

      // Build the ResultSet metaData
      int nbColumn;
      if (metadataCache != null)
        fields = metadataCache.getMetadata(request);

      if (fields == null)
      { // Metadata Cache miss
        // Build the fields from the MetaData
        java.sql.ResultSetMetaData metaData = rs.getMetaData();
        if (metaData == null)
          throw new SQLException("Unable to fetch metadata");
        nbColumn = metaData.getColumnCount();
        fields = new Field[nbColumn];
        for (int i = 0; i < nbColumn; i++)
        {
          // 1st column is 1
          String columnName = metaData.getColumnName(i + 1);
          String tableName = null;
          try
          {
            tableName = metaData.getTableName(i + 1);
          }
          catch (Exception ignore)
          {
          }
          if (metadataCache != null)
          { // Check Field cache
            fields[i] = metadataCache.getField(tableName + "." + columnName);
            if (fields[i] != null)
              continue; // Cache hit
          }
          // Field cache miss
          int columnDisplaySize = 0;
          try
          {
            columnDisplaySize = metaData.getColumnDisplaySize(i + 1);
          }
          catch (Exception ignore)
          {
          }
          int columnType = -1;
          try
          {
            columnType = metaData.getColumnType(i + 1);
          }
          catch (Exception ignore)
          {
          }
          String columnTypeName = null;
          try
          {
            columnTypeName = metaData.getColumnTypeName(i + 1);
          }
          catch (Exception ignore)
          {
          }
          String columnClassName = null;
          try
          {
            columnClassName = metaData.getColumnClassName(i + 1);
          }
          catch (Exception ignore)
          {
          }
          boolean isAutoIncrement = false;
          try
          {
            isAutoIncrement = metaData.isAutoIncrement(i + 1);
          }
          catch (Exception ignore)
          {
          }
          boolean isCaseSensitive = false;
          try
          {
            isCaseSensitive = metaData.isCaseSensitive(i + 1);
          }
          catch (Exception ignore)
          {
          }
          boolean isCurrency = false;
          try
          {
            isCurrency = metaData.isCurrency(i + 1);
          }
          catch (Exception ignore)
          {
          }
          int isNullable = ResultSetMetaData.columnNullableUnknown;
          try
          {
            isNullable = metaData.isNullable(i + 1);
          }
          catch (Exception ignore)
          {
          }
          boolean isReadOnly = false;
          try
          {
            isReadOnly = metaData.isReadOnly(i + 1);
          }
          catch (Exception ignore)
          {
          }
          boolean isWritable = false;
          try
          {
            isWritable = metaData.isWritable(i + 1);
          }
          catch (Exception ignore)
          {
          }
          boolean isDefinitelyWritable = false;
          try
          {
            isReadOnly = metaData.isDefinitelyWritable(i + 1);
          }
          catch (Exception ignore)
          {
          }
          boolean isSearchable = false;
          try
          {
            isSearchable = metaData.isSearchable(i + 1);
          }
          catch (Exception ignore)
          {
          }
          boolean isSigned = false;
          try
          {
            isSigned = metaData.isSigned(i + 1);
          }
          catch (Exception ignore)
          {
          }
          int precision = 0;
          try
          {
            precision = metaData.getPrecision(i + 1);
          }
          catch (Exception ignore)
          {
          }
          int scale = 0;
          try
          {
            scale = metaData.getScale(i + 1);
          }
          catch (Exception ignore)
          {
          }
          fields[i] = new Field(tableName, columnName, columnDisplaySize,
              columnType, columnTypeName, columnClassName, isAutoIncrement,
              isCaseSensitive, isCurrency, isNullable, isReadOnly, isWritable,
              isDefinitelyWritable, isSearchable, isSigned, precision, scale);

          if (metadataCache != null)
            // Add field to cache
            metadataCache.addField(tableName + "." + columnName, fields[i]);
        } // for
        if (metadataCache != null)
          metadataCache.addMetadata(request, fields);
      }
      else
        nbColumn = fields.length;

      // Build the ResultSet data
      if (rs.next())
      {
        cursorName = request.getCursorName();
        fetchSize = request.getFetchSize();
        maxRows = request.getMaxRows();
        if (maxRows == 0)
          maxRows = Integer.MAX_VALUE; // Infinite number of rows

        // Note that fetchData updates the data field
        dbResultSet = rs;
        fetchData();
        if (hasMoreData && (cursorName == null))
          // hashCode() is not guaranteed to be injective in theory,
          // but returns the address of the object in practice.
          cursorName = String.valueOf(dbResultSet.hashCode());
      }
      else
      {
        hasMoreData = false;
        dbResultSet = null;
        dbResultSetClosed = true;
        rs.close();
        if (owningStatement != null)
        {
          try
          {
            owningStatement.close();
          }
          catch (SQLException ignore)
          {
          }
          owningStatement = null;
        }
      }
    }
    catch (SQLException e)
    {
      throw (SQLException) new SQLException(
          "Error while building C-JDBC ResultSet (" + e.getLocalizedMessage()
              + ")").initCause(e);
    }
  }

  /**
   * Sets the fetch size and calls fetchData()
   * 
   * @param fetchSizeParam the number of rows to fetch
   * @throws SQLException if an error occurs
   * @see #fetchData()
   */
  public void fetchData(int fetchSizeParam) throws SQLException
  {
    this.fetchSize = fetchSizeParam;
    fetchData();
    if (!hasMoreData)
    {
      if (owningStatement != null)
      {
        try
        {
          owningStatement.close();
        }
        catch (SQLException ignore)
        {
        }
        owningStatement = null;
      }
    }
  }

  /**
   * Fetch the next rows of data from dbResultSet according to fetchSize and
   * maxRows parameters. This methods directly updates the data and hasMoreData
   * fields returned by getData() and hadMoreData() accessors.
   * 
   * @throws SQLException from the backend or if dbResultSet is closed. Maybe
   * we should use a different type internally.
   */
  public void fetchData() throws SQLException
  {
    if (dbResultSet == null)
      throw new SQLException("Backend ResultSet is closed");

    Object[] row;
    // We directly update the data field
    if (data == EMPTY_LIST)
      data = new ArrayList();
    else
      // Re-use the existing ArrayList with the good size to be more efficient
      data.clear();
    int toFetch;
    if (fetchSize > 0)
    {
      toFetch = fetchSize < maxRows ? fetchSize : maxRows;
      // instead of remembering how much we sent, it's simpler to decrease how
      // much we still may send.
      maxRows -= toFetch;
    }
    else
      toFetch = maxRows;
    int nbColumn = fields.length;
    Object object;
    do
    {
      row = new Object[nbColumn];
      for (int i = 0; i < nbColumn; i++)
      {
        object = dbResultSet.getObject(i + 1);
        // Convert database native Clob/Blob to String/byte[] that are
        // Serializable
        if (object != null)
        {
          if (object instanceof java.sql.Clob)
          {
            java.sql.Clob clob = (java.sql.Clob) object;
            object = clob.getSubString(1, (int) clob.length());
          }
          else if (object instanceof java.sql.Blob)
          {
            java.sql.Blob blob = (java.sql.Blob) object;
            object = blob.getBytes(1, (int) blob.length());
          }
        }
        row[i] = object;
      }
      data.add(row);
      toFetch--;
      hasMoreData = dbResultSet.next();
    }
    while (hasMoreData && (toFetch > 0));
    if (hasMoreData && (fetchSize > 0) && (maxRows > 0))
    { // More data to fetch later on
      maxRows += toFetch;
      dbResultSetClosed = false;
    }
    else
    {
      hasMoreData = false;
      dbResultSet.close();
      if (owningStatement != null)
        owningStatement.close();
      dbResultSet = null;
      dbResultSetClosed = true;
    }
  }

  /**
   * Returns the data value.
   * 
   * @return Returns the data.
   */
  public ArrayList getData()
  {
    return data;
  }

  /**
   * Returns the fields value.
   * 
   * @return Returns the fields.
   */
  public Field[] getFields()
  {
    return fields;
  }

  /**
   * Get the name of the SQL cursor used by this ResultSet
   * 
   * @return the ResultSet's SQL cursor name.
   */
  public String getCursorName()
  {
    return cursorName;
  }

  /**
   * Returns the hasMoreData value.
   * 
   * @return Returns the hasMoreData.
   */
  public boolean hasMoreData()
  {
    return hasMoreData;
  }

  /**
   * Closes the database ResultSet to release the resource and garbage collect
   * data.
   */
  public void closeResultSet()
  {
    if ((dbResultSet != null) && !dbResultSetClosed)
    {
      try
      {
        dbResultSet.close();
      }
      catch (SQLException ignore)
      {
      }
      dbResultSet = null; // to allow GC to work properly
      if (owningStatement != null)
      {
        try
        {
          owningStatement.close();
        }
        catch (SQLException ignore)
        {
        }
        owningStatement = null;
      }
    }
  }

}