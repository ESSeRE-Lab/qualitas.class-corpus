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
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.controller.cache.result.entries;

import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.stream.CJDBCStream;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * A <code>CacheEntry</code> represents a SQL select request with its reponse.
 * The cache entry can have 3 states:
 * <p>
 * <ul>
 * <li><code>CACHE_VALID</code> when it is valid</li>
 * <li><code>CACHE_DIRTY</code> when the result has been marked dirty (may be
 * invalid)</li>
 * <li><code>CACHE_INVALID</code> when there is no result (request has to be
 * re-issued to the database)</li>
 * </ul>
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public abstract class AbstractResultCacheEntry
{
  protected SelectRequest          request;
  protected ControllerResultSet    result;
  protected int                    state;

  //Chain for LRU
  private AbstractResultCacheEntry next;
  private AbstractResultCacheEntry prev;

  /** This entry has no deadline */
  public static final int          NO_DEADLINE   = -1;
  /** State this entry is valid */
  public static final int          CACHE_VALID   = 0;
  /** This entry is dirty */
  public static final int          CACHE_DIRTY   = 1;
  /** This entry is no more valid and is not consistent with real data. */
  public static final int          CACHE_INVALID = 2;

  /**
   * Creates a new <code>CacheEntry</code> instance.
   * 
   * @param request a <code>SelectRequest</code> value
   * @param result a <code>ControllerResultSet</code> value
   */
  public AbstractResultCacheEntry(SelectRequest request,
      ControllerResultSet result)
  {
    this.request = request;
    this.result = result;
    state = CACHE_VALID;
    next = null;
    prev = null;
  }

  /**
   * Get the type of this entry as a string
   * 
   * @return NoCache or Eager or Relaxed
   */
  public abstract String getType();

  /**
   * Get the state of this entry as a string
   * 
   * @return Valid or Dirty or Invalid
   */
  public String getState()
  {
    if (isValid())
      return "Valid";
    if (isDirty())
      return "Dirty";
    else
      return "Invalid";
  }

  /**
   * Return <code>true</code> if cache entry state is valid (state is
   * {@link #CACHE_VALID}).
   * 
   * @return a <code>boolean</code> value
   */
  public boolean isValid()
  {
    return state == CACHE_VALID;
  }

  /**
   * Returns <code>true</code> if cache entry state is marked dirty (state is
   * {@link #CACHE_DIRTY}).
   * 
   * @return a <code>boolean</code> value
   */
  public boolean isDirty()
  {
    return state == CACHE_DIRTY;
  }

  /**
   * Returns the <code>SELECT</code> request of this cache entry.
   * 
   * @return a <code>SelectRequest</code> value
   */
  public SelectRequest getRequest()
  {
    return request;
  }

  /**
   * Returns the <code>ControllerResultSet</code> of the cached select request
   * 
   * @return a <code>ControllerResultSet</code> value
   */
  public ControllerResultSet getResult()
  {
    return result;
  }

  /**
   * Set a new <code>ControllerResultSet</code> of the cached select request
   * (cache update).
   * <p>
   * The cache state is automatically set to valid ({@link #CACHE_VALID}).
   * 
   * @param result a <code>ControllerResultSet</code> value
   */
  public void setResult(ControllerResultSet result)
  {
    this.result = result;
    state = CACHE_VALID;
  }

  /**
   * Invalidates this cache entry (removes the <code>ResultSet</code> and turn
   * state to {@link #CACHE_INVALID}).
   */
  public abstract void invalidate();

  /**
   * Marks this entry dirty (state becomes {@link #CACHE_DIRTY}).
   * <p>
   * The <code>ResultSet</code> if not affected by this method.
   */
  public void markDirty()
  {
    state = CACHE_DIRTY;
  }

  /**
   * Marks this entry valid (state becomes {@link #CACHE_VALID}).
   */
  public void setValid()
  {
    state = CACHE_VALID;
  }

  /**
   * Gets the value of next <code>AbstractResultCacheEntry</code> in LRU.
   * 
   * @return value of next.
   */
  public AbstractResultCacheEntry getNext()
  {
    return next;
  }

  /**
   * Sets the value of next <code>AbstractResultCacheEntry</code> in LRU.
   * 
   * @param next value to assign to next.
   */
  public void setNext(AbstractResultCacheEntry next)
  {
    this.next = next;
  }

  /**
   * Gets the value of previous <code>AbstractResultCacheEntry</code> in LRU.
   * 
   * @return value of previous.
   */
  public AbstractResultCacheEntry getPrev()
  {
    return prev;
  }

  /**
   * Sets the value of previous <code>AbstractResultCacheEntry</code> in LRU.
   * 
   * @param prev value to assign to prev.
   */
  public void setPrev(AbstractResultCacheEntry prev)
  {
    this.prev = prev;
  }

  /**
   * Get data about this entry
   * 
   * @return an array [request,type,status(valid,notvalid,dirty),deadLine]
   */
  public abstract String[] toStringTable();

  /**
   * Size of the result in bytes
   * 
   * @return an integer
   */
  public int getSizeOfResult()
  {
    try
    {
      return CJDBCStream.countBytes(result);
    }
    catch (Exception e)
    {
      return -1;
    }
  }

}