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

import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.UpdateRequest;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;

/**
 * This is a query cache implementation with a database granularity:
 * <ul>
 * <li><code>DATABASE</code>: the cache is flushed each time the database is
 * updated (every INSERT, UPDATE, DELETE, ... statement).</li>
 * </ul>
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */

public class ResultCacheDatabase extends ResultCache
{

  /**
   * Builds a new ResultCache with a database granularity.
   * 
   * @param maxEntries maximum number of entries
   * @param pendingTimeout pending timeout for concurrent queries
   */
  public ResultCacheDatabase(int maxEntries, int pendingTimeout)
  {
    super(maxEntries, pendingTimeout);
    parsingGranularity = ParsingGranularities.NO_PARSING;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.ResultCache#processAddToCache
   */
  protected void processAddToCache(AbstractResultCacheEntry qe)
  {
    return;
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
    flushCache();
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.ResultCache#getName()
   */
  public String getName()
  {
    return "database";
  }

}