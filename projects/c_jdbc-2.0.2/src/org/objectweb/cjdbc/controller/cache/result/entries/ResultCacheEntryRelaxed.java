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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.controller.cache.result.entries;

import java.util.Date;

import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * A <code>CacheEntry</code> that is to be recognized as Relaxed entry.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ResultCacheEntryRelaxed extends AbstractResultCacheEntry
{
  private long    timeout;
  private long    deadline;
  private boolean keepIfNotDirty;

  /**
   * Create a new Relaxed Query Cache entry
   * 
   * @param request Select request to cache
   * @param result ResultSet to cache
   * @param timeout timeout in ms for this entry
   * @param keepIfNotDirty true if entry must be kept in cache if not dirty once
   *          timeout has expired
   */
  public ResultCacheEntryRelaxed(SelectRequest request,
      ControllerResultSet result, long timeout, boolean keepIfNotDirty)
  {
    super(request, result);
    this.timeout = timeout;
    this.deadline = System.currentTimeMillis() + timeout;
    this.keepIfNotDirty = keepIfNotDirty;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry#invalidate()
   */
  public void invalidate()
  {
    state = CACHE_DIRTY;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry#getType()
   */
  public String getType()
  {
    return "Relaxed";
  }

  /**
   * Get the expiration deadline
   * 
   * @return the expiration deadline
   */
  public long getDeadline()
  {
    return deadline;
  }

  /**
   * Set the expiration deadline
   * 
   * @param deadline time in ms relative to current time
   */
  public void setDeadline(long deadline)
  {
    this.deadline = deadline;
  }

  /**
   * Get the timeout for this entry.
   * 
   * @return timeout in ms
   */
  public long getTimeout()
  {
    return timeout;
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
   * Should the entry must be kept in the cache if the entry is not dirty once
   * the timeout has expired.
   * 
   * @return true if yes
   */
  public boolean getKeepIfNotDirty()
  {
    return keepIfNotDirty;
  }

}