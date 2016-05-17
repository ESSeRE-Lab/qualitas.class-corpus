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

package org.objectweb.cjdbc.controller.cache;

import org.objectweb.cjdbc.common.util.Stats;

/**
 * This class handles the statistics for request caches.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class CacheStatistics
{
  // Cache statistics
  private Stats select;
  private Stats hits;
  private Stats insert;
  private Stats update;
  private Stats uncacheable;
  private Stats delete;
  private Stats unknown;
  private Stats remove;
  private Stats create;
  private Stats drop;

  /**
   * Creates a new CacheStatistics object.
   */
  public CacheStatistics()
  {
    select = new Stats("select");
    hits = new Stats("hits");
    insert = new Stats("insert");
    update = new Stats("update");
    uncacheable = new Stats("uncacheable");
    delete = new Stats("delete");
    unknown = new Stats("unknown");
    remove = new Stats("remove");
    create = new Stats("create");
    drop = new Stats("drop");
  }

  /**
   * Resets all stats to zero.
   */
  public void reset()
  {
    select.reset();
    hits.reset();
    insert.reset();
    update.reset();
    uncacheable.reset();
    delete.reset();
    unknown.reset();
    remove.reset();
    create.reset();
    drop.reset();
  }

  /**
   * Returns the create.
   * 
   * @return an <code>int</code> value
   */
  public int getCreate()
  {
    return create.getCount();
  }

  /**
   * Returns the delete.
   * 
   * @return an <code>int</code> value
   */
  public int getDelete()
  {
    return delete.getCount();
  }

  /**
   * Returns the drop.
   * 
   * @return an <code>int</code> value
   */
  public int getDrop()
  {
    return drop.getCount();
  }

  /**
   * Returns the hits.
   * 
   * @return an <code>int</code> value
   */
  public int getHits()
  {
    return hits.getCount();
  }

  /**
   * Returns the insert.
   * 
   * @return an <code>int</code> value
   */
  public int getInsert()
  {
    return insert.getCount();
  }

  /**
   * Returns the remove.
   * 
   * @return an <code>int</code> value
   */
  public int getRemove()
  {
    return remove.getCount();
  }

  /**
   * Returns the select.
   * 
   * @return an <code>int</code> value
   */
  public int getSelect()
  {
    return select.getCount();
  }

  /**
   * Returns the unknown.
   * 
   * @return an <code>int</code> value
   */
  public int getUnknown()
  {
    return unknown.getCount();
  }

  /**
   * Returns the update.
   * 
   * @return an <code>int</code> value
   */
  public int getUpdate()
  {
    return update.getCount();
  }

  /**
   * Returns the uncacheable.
   * 
   * @return an <code>int</code> value
   */
  public int getUncacheable()
  {
    return uncacheable.getCount();
  }

  /**
   * Increments the create count.
   */
  public void addCreate()
  {
    create.incrementCount();
  }

  /**
   * Increments the delete count.
   */
  public void addDelete()
  {
    delete.incrementCount();
  }

  /**
   * Increments the drop count.
   */
  public void addDrop()
  {
    drop.incrementCount();
  }

  /**
   * Increments the hits count.
   */
  public void addHits()
  {
    hits.incrementCount();
  }

  /**
   * Increments the insert count.
   */
  public void addInsert()
  {
    insert.incrementCount();
  }

  /**
   * Increments the remove count.
   */
  public void addRemove()
  {
    remove.incrementCount();
  }

  /**
   * Increments the select count.
   */
  public void addSelect()
  {
    select.incrementCount();
  }

  /**
   * Increments the unkwnown count.
   */
  public void addUnknown()
  {
    unknown.incrementCount();
  }

  /**
   * Increments the update count.
   */
  public void addUpdate()
  {
    update.incrementCount();
  }

  /**
   * Increments the uncacheable count.
   */
  public void addUncacheable()
  {
    uncacheable.incrementCount();
  }

  /**
   * Retrieve cache statistics as a table
   * 
   * @return an array of String containing the different cache values, like
   *         number of select, number of hits ...
   */
  public String[] getCacheStatsData()
  {
    String[] stats = new String[11];
    stats[0] = "" + getSelect();
    stats[1] = "" + getHits();
    stats[2] = "" + getInsert();
    stats[3] = "" + getUpdate();
    stats[4] = "" + getUncacheable();
    stats[5] = "" + getDelete();
    stats[6] = "" + getUnknown();
    stats[7] = "" + getRemove();
    stats[8] = "" + getCreate();
    stats[9] = "" + getDrop();
    stats[10] = "" + getCacheHitRatio();
    return stats;
  }

  /**
   * Get percentage of hits
   * 
   * @return hits / select
   */
  public long getCacheHitRatio()
  {
    if (select.getCount() == 0)
      return 0;
    else
      return (long) ((float) hits.getCount() / (float) select.getCount() * 100.0);
  }
}
