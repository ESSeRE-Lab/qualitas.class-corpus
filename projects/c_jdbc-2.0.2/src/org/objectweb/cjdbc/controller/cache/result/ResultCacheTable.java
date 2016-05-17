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
 * Contributor(s): Nicolas Modzyk.
 */

package org.objectweb.cjdbc.controller.cache.result;

import java.util.Iterator;

import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.UpdateRequest;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;
import org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseTable;

/**
 * This is a query cache implementation with a table granularity:
 * <ul>
 * <li><code>TABLE</code>: table granularity, entries in the cache are
 * invalidated based on table dependencies.</li>
 * </ul>
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ResultCacheTable extends ResultCache
{
  /**
   * Builds a new ResultCache with a table granularity.
   * 
   * @param maxEntries maximum number of entries
   * @param pendingTimeout pending timeout for concurrent queries
   */
  public ResultCacheTable(int maxEntries, int pendingTimeout)
  {
    super(maxEntries, pendingTimeout);
    parsingGranularity = ParsingGranularities.TABLE;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.ResultCache#processAddToCache
   */
  protected void processAddToCache(AbstractResultCacheEntry qe)
  {
    SelectRequest request = qe.getRequest();
    for (Iterator i = request.getFrom().iterator(); i.hasNext();)
      cdbs.getTable((String) i.next()).addCacheEntry(qe);
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
    CacheDatabaseTable cdt = cdbs.getTable(request.getTableName());

    if (cdt != null)
      cdt.invalidateAll();
    else
    {
      logger.warn("Table " + request.getTableName()
          + " not found in cache schema. Flushing whole cache.");
      flushCache();
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.ResultCache#getName()
   */
  public String getName()
  {
    return "table";
  }
}