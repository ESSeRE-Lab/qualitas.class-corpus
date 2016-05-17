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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.controller.cache.result.entries;

import java.util.Date;

import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * A <code>CacheEntry</code> that is to be recognized as Eager entry.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ResultCacheEntryEager extends AbstractResultCacheEntry
{
  private AbstractResultCache cache;
  private long                timeout;
  private long                deadline;

  /**
   * Create a new Eager Query Cache entry
   * 
   * @param cache The query cache we belong to
   * @param request Select request to cache
   * @param result ResultSet to cache
   * @param timeout The timeout to use for the deadline (0 for no timeout)
   */
  public ResultCacheEntryEager(AbstractResultCache cache,
      SelectRequest request, ControllerResultSet result, long timeout)
  {
    super(request, result);
    this.cache = cache;
    if (timeout > 0)
      this.deadline = System.currentTimeMillis() + timeout;
    else
      this.deadline = NO_DEADLINE;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry#invalidate()
   */
  public void invalidate()
  {
    state = CACHE_INVALID;
    if (cache != null)
      cache.removeFromCache(request);
    if (result != null)
      result = null;
    cache = null;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry#getType()
   */
  public String getType()
  {
    return "Eager";
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry#toStringTable()
   */
  public String[] toStringTable()
  {
    return new String[]{request.getSQL(), getType(), getState(),
        new Date(getDeadline()).toString(), "" + getSizeOfResult()};
  }

  /**
   * Returns the deadline value.
   * 
   * @return Returns the deadline.
   */
  public long getDeadline()
  {
    return deadline;
  }

  /**
   * Returns the timeout value.
   * 
   * @return Returns the timeout.
   */
  public long getTimeout()
  {
    return timeout;
  }
}