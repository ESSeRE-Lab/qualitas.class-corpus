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
 * Contributor(s): Nicolas Modrzyk.
 */

package org.objectweb.cjdbc.controller.cache.result;

import java.util.ArrayList;
import java.util.Iterator;

import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.DeleteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.RequestType;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.UpdateRequest;
import org.objectweb.cjdbc.common.sql.schema.TableColumn;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;
import org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseColumn;
import org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseTable;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * This is a query cache implementation with a column unique granularity:
 * <ul>
 * <li><code>COLUMN_UNIQUE</code>: same as <code>COLUMN</code> except that
 * <code>UNIQUE</code> queries that selects a single row based on a key are
 * invalidated only when needed.
 * </ul>
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ResultCacheColumnUnique extends ResultCache
{

  /**
   * Builds a new ResultCache with a column unique granularity.
   * 
   * @param maxEntries maximum number of entries
   * @param pendingTimeout pending timeout for concurrent queries
   */
  public ResultCacheColumnUnique(int maxEntries, int pendingTimeout)
  {
    super(maxEntries, pendingTimeout);
    parsingGranularity = ParsingGranularities.COLUMN_UNIQUE;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.ResultCache#processAddToCache(AbstractResultCacheEntry)
   */
  public void processAddToCache(AbstractResultCacheEntry qe)
  {
    SelectRequest request = qe.getRequest();
    ArrayList selectedColumns = request.getSelect();
    //  Update the tables columns dependencies
    if (selectedColumns == null || selectedColumns.isEmpty())
    {
      logger
          .warn("No parsing of select clause found - Fallback to table granularity");
      for (Iterator i = request.getFrom().iterator(); i.hasNext();)
      {
        CacheDatabaseTable table = cdbs.getTable((String) i.next());
        table.addCacheEntry(qe);
        // Add all columns, entries will be added below.
        ArrayList columns = table.getColumns();
        for (int j = 0; j < columns.size(); j++)
        {
          ((CacheDatabaseColumn) columns.get(j)).addCacheEntry(qe);
        }
        return;
      }
    }
    for (Iterator i = request.getSelect().iterator(); i.hasNext();)
    {
      TableColumn tc = (TableColumn) i.next();
      cdbs.getTable(tc.getTableName()).getColumn(tc.getColumnName())
          .addCacheEntry(qe);
    }
    if (request.getWhere() != null)
    { // Add all columns dependencies
      for (Iterator i = request.getWhere().iterator(); i.hasNext();)
      {
        TableColumn tc = (TableColumn) i.next();
        cdbs.getTable(tc.getTableName()).getColumn(tc.getColumnName())
            .addCacheEntry(qe);
      }
      if (request.getCacheAbility() == RequestType.UNIQUE_CACHEABLE)
      { // Add a specific entry for this pk
        String tableName = (String) request.getFrom().get(0);
        AbstractResultCacheEntry entry = cdbs.getTable(tableName)
            .getPkResultCacheEntry(request.getPkValue());
        if (entry != null)
        {
          if (entry.isValid())
          { // Do not add an entry which has a lower selection than the current
            // one
            if (entry.getRequest().getSelect().size() >= request.getSelect()
                .size())
              return;
          }
        }
        cdbs.getTable(tableName).addPkCacheEntry(request.getPkValue(), qe);
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.AbstractResultCache#isUpdateNecessary(org.objectweb.cjdbc.common.sql.UpdateRequest)
   */
  public boolean isUpdateNecessary(UpdateRequest request)
  {
    if (request.getCacheAbility() != RequestType.UNIQUE_CACHEABLE)
      return true;
    CacheDatabaseTable cacheTable = cdbs.getTable(request.getTableName());
    if (request.getColumns() == null)
      return true;
    String pk = request.getPk();
    AbstractResultCacheEntry qce = cacheTable.getPkResultCacheEntry(pk);
    if (qce != null)
    {
      if (!qce.isValid())
        return true;
      ControllerResultSet rs = qce.getResult();
      if (rs == null)
        return true;
      else
        return needInvalidate(rs, request)[1];
    }
    else
      return true;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.ResultCache#processWriteNotify(org.objectweb.cjdbc.common.sql.AbstractWriteRequest)
   */
  protected void processWriteNotify(AbstractWriteRequest request)
  {
    // Sanity check
    CacheDatabaseTable cacheTable = cdbs.getTable(request.getTableName());
    if (request.getColumns() == null)
    {
      logger.warn("No column parsing found - Fallback to table granularity ("
          + request.getSQL() + ")");
      cacheTable.invalidateAll();
      return;
    }
    if (request.isInsert())
    {
      for (Iterator i = request.getColumns().iterator(); i.hasNext();)
      {
        TableColumn tc = (TableColumn) i.next();
        cdbs.getTable(tc.getTableName()).getColumn(tc.getColumnName())
            .invalidateAllNonUnique();
      }
    }
    else
    {
      if (request.getCacheAbility() == RequestType.UNIQUE_CACHEABLE)
      {
        if (request.isUpdate())
        {
          String pk = ((UpdateRequest) request).getPk();
          AbstractResultCacheEntry qce = cacheTable.getPkResultCacheEntry(pk);
          if (qce != null)
          {
            boolean[] invalidate = needInvalidate(qce.getResult(),
                (UpdateRequest) request);
            if (invalidate[0])
            { // We must invalidate this entry
              cacheTable.removePkResultCacheEntry(pk);
              return;
            }
            else
            {
              if (logger.isDebugEnabled())
                logger.debug("No invalidate needed for request:"
                    + request.getSQLShortForm(20));
              return; // We don't need to invalidate
            }
          }
        }
        else if (request.isDelete())
        { // Invalidate the corresponding cache entry
          cacheTable
              .removePkResultCacheEntry(((DeleteRequest) request).getPk());
          return;
        }
      }
      // At this point this is a non unique write query or a request
      // we didn't handle properly (unknown request for example)
      for (Iterator i = request.getColumns().iterator(); i.hasNext();)
      {
        TableColumn tc = (TableColumn) i.next();
        CacheDatabaseTable table = cdbs.getTable(tc.getTableName());
        table.invalidateAll(); // Pk are associated to tables
        if (!request.isAlter())
          table.getColumn(tc.getColumnName()).invalidateAll();
      }
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.ResultCache#getName()
   */
  public String getName()
  {
    return "columnUnique";
  }

}