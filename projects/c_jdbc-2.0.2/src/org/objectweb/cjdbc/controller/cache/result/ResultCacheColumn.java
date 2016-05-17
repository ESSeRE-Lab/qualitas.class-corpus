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
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.UpdateRequest;
import org.objectweb.cjdbc.common.sql.schema.TableColumn;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;
import org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseColumn;
import org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseTable;

/**
 * This is a query cache implementation with a column granularity:
 * <ul>
 * <li><code>COLUMN</code>: column granularity, entries in the cache are
 * invalidated based on column dependencies</li>
 * </ul>
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */

public class ResultCacheColumn extends ResultCache
{
  /**
   * Builds a new ResultCache with a Column granularity.
   * 
   * @param maxEntries maximum number of entries
   * @param pendingTimeout pending timeout for concurrent queries
   */
  public ResultCacheColumn(int maxEntries, int pendingTimeout)
  {
    super(maxEntries, pendingTimeout);
    parsingGranularity = ParsingGranularities.COLUMN;
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
    for (Iterator i = selectedColumns.iterator(); i.hasNext();)
    {
      TableColumn tc = (TableColumn) i.next();
      cdbs.getTable(tc.getTableName()).getColumn(tc.getColumnName())
          .addCacheEntry(qe);
    }
    if (request.getWhere() != null)
      for (Iterator i = request.getWhere().iterator(); i.hasNext();)
      {
        TableColumn tc = (TableColumn) i.next();
        cdbs.getTable(tc.getTableName()).getColumn(tc.getColumnName())
            .addCacheEntry(qe);
      }
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.AbstractResultCache#isUpdateNecessary(org.objectweb.cjdbc.common.sql.UpdateRequest)
   */
  public boolean isUpdateNecessary(UpdateRequest request)
  {
    return true;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.ResultCache#processWriteNotify(org.objectweb.cjdbc.common.sql.AbstractWriteRequest)
   */
  protected void processWriteNotify(AbstractWriteRequest request)
  {
    //    Sanity check
    if (request.getColumns() == null)
    {
      logger.warn("No column parsing found - Fallback to table granularity ("
          + request.getSQL() + ")");
      cdbs.getTable(request.getTableName()).invalidateAll();
      return;
    }
    if (request.isAlter())
    {
      cdbs.getTable(request.getTableName()).invalidateAll();
      return;
    }
    for (Iterator i = request.getColumns().iterator(); i.hasNext();)
    {
      TableColumn tc = (TableColumn) i.next();
      cdbs.getTable(tc.getTableName()).getColumn(tc.getColumnName())
          .invalidateAll();
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.ResultCache#getName()
   */
  public String getName()
  {
    return "column";
  }

}