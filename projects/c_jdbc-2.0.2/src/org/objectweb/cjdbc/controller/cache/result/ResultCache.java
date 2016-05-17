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
 * Contributor(s): Julie Marguerite, Sara Bouchenak, Nicolas Modrzyk.
 */

package org.objectweb.cjdbc.controller.cache.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.CreateRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.RequestType;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.UpdateRequest;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.cache.CacheException;
import org.objectweb.cjdbc.controller.cache.CacheStatistics;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;
import org.objectweb.cjdbc.controller.cache.result.entries.ResultCacheEntryEager;
import org.objectweb.cjdbc.controller.cache.result.entries.ResultCacheEntryNoCache;
import org.objectweb.cjdbc.controller.cache.result.entries.ResultCacheEntryRelaxed;
import org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseSchema;
import org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseTable;
import org.objectweb.cjdbc.controller.cache.result.threads.EagerCacheThread;
import org.objectweb.cjdbc.controller.cache.result.threads.RelaxedCacheThread;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;
import org.objectweb.cjdbc.driver.Field;

/**
 * This is a query cache implementation with tunable granularity. <br>
 * Cache invalidation granularity can take on of the following values:
 * <ul>
 * <li><code>NO_INVALIDATE</code>: no invalidation, the cache is
 * inconsistent and this should just be used to determine hit ratio upper bound.
 * </li>
 * <li><code>DATABASE</code>: the cache is flushed each time the database is
 * updated (every INSERT, UPDATE, DELETE, ... statement).</li>
 * <li><code>TABLE</code>: table granularity, entries in the cache are
 * invalidated based on table dependencies.</li>
 * <li><code>COLUMN</code>: column granularity, entries in the cache are
 * invalidated based on column dependencies</li>
 * <li><code>COLUMN_UNIQUE</code>: same as <code>COLUMN</code> except that
 * <code>UNIQUE</code> queries that selects a single row based on a key are
 * invalidated only when needed.
 * </ul>
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @author <a href="mailto:Sara.Bouchenak@epfl.ch">Sara Bouchenak </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class ResultCache extends AbstractResultCache
{
  //
  // How the code is organized?
  //
  // 1. Member variables
  // 2. Constructor
  // 3. Cache management
  // 4. Transaction management
  // 5. Debug/Monitoring
  //

  // Max number of cache entries
  private int                      maxEntries;
  /** Pending query timeout in ms. Default is: 0 (wait forever). */
  private long                     pendingQueryTimeout = 0;
  // queries: SQL -> AbstractResultCacheEntry
  private HashMap                  queries;
  // Pending SQL requests (String)
  private HashSet                  pendingQueries;
  // The rules to apply for this cache
  private HashSet                  cachingRules;
  private ResultCacheRule          defaultRule;
  private ArrayList                relaxedCache;

  // LRU (head) of cache entries for replacement
  private AbstractResultCacheEntry lruHead;
  // LRU (tail) of cache entries for replacement
  private AbstractResultCacheEntry lruTail;

  // Database schema
  protected CacheDatabaseSchema    cdbs;

  private CacheStatistics          stats;

  private RelaxedCacheThread       relaxedThread;
  private static final boolean[]   TRUE_TRUE           = new boolean[]{true,
      true                                             };
  private boolean                  flushingCache;
  private EagerCacheThread         eagerThread;
  private ArrayList                eagerCache;

  /*
   * Constructor
   */

  /**
   * Creates a new <code>Cache</code> instance.
   * 
   * @param maxEntries maximum number of cache entries
   * @param pendingTimeout pending queries timeout
   */
  public ResultCache(int maxEntries, int pendingTimeout)
  {
    this.maxEntries = maxEntries;
    this.pendingQueryTimeout = pendingTimeout;
    cdbs = null;
    stats = new CacheStatistics();
    queries = new HashMap(1000, (float) 0.75);
    pendingQueries = new HashSet();
    cachingRules = new HashSet();
    relaxedCache = new ArrayList();
    eagerCache = new ArrayList();
    lruHead = null;
    lruTail = null;
    defaultRule = null;
    relaxedThread = new RelaxedCacheThread(this);
    relaxedThread.setPriority(9);
    relaxedThread.start();
    eagerThread = new EagerCacheThread(this);
    eagerThread.setPriority(9);
    eagerThread.start();
  }

  /**
   * Shutdown the result cache and all its threads.
   */
  public synchronized void shutdown()
  {
    relaxedThread.shutdown();
    eagerThread.shutdown();
  }

  /**
   * Returns the pending query timeout in seconds.
   * 
   * @return the pending query timeout.
   * @see #setPendingQueryTimeout
   */
  public int getPendingQueryTimeout()
  {
    return (int) (pendingQueryTimeout / 1000);
  }

  /**
   * Sets the pending query timeout in seconds.
   * 
   * @param pendingQueryTimeout the pending query timeout to set.
   * @see #getPendingQueryTimeout
   */
  public void setPendingQueryTimeout(int pendingQueryTimeout)
  {
    this.pendingQueryTimeout = pendingQueryTimeout * 1000L;
  }

  /**
   * Possibly we want to access the queries in the cache for timing purposes
   * 
   * @return the <code>HashMap</code> of queries (not synchronized)
   */
  public HashMap getQueries()
  {
    return this.queries;
  }

  /**
   * Sets the <code>DatabaseSchema</code> of the current virtual database.
   * 
   * @param dbs a <code>DatabaseSchema</code> value
   * @see org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseSchema
   */
  public void setDatabaseSchema(DatabaseSchema dbs)
  {
    if (cdbs == null)
    {
      logger.info(Translate.get("resultcache.setting.database.schema"));
      cdbs = new CacheDatabaseSchema(dbs);
    }
    else
    { // Schema is updated, compute the diff !
      CacheDatabaseSchema newSchema = new CacheDatabaseSchema(dbs);
      ArrayList tables = cdbs.getTables();
      ArrayList newTables = newSchema.getTables();
      if (newTables == null)
      { // New schema is empty (no backend is active anymore)
        logger.info(Translate.get("resultcache.flusing.whole.cache"));
        flushCache();
        cdbs = null;
        return;
      }

      // Remove extra-tables
      for (int i = 0; i < tables.size(); i++)
      {
        CacheDatabaseTable t = (CacheDatabaseTable) tables.get(i);
        if (!newSchema.hasTable(t.getName()))
        {
          t.invalidateAll();
          cdbs.removeTable(t);
          if (logger.isInfoEnabled())
            logger.info(Translate
                .get("resultcache.removing.table", t.getName()));
        }
      }

      // Add missing tables
      int size = newTables.size();
      for (int i = 0; i < size; i++)
      {
        CacheDatabaseTable t = (CacheDatabaseTable) newTables.get(i);
        if (!cdbs.hasTable(t.getName()))
        {
          cdbs.addTable(t);
          if (logger.isInfoEnabled())
            logger.info(Translate.get("resultcache.adding.table", t.getName()));
        }
      }
    }
  }

  /**
   * Merge the given <code>DatabaseSchema</code> with the current one.
   * 
   * @param dbs a <code>DatabaseSchema</code> value
   * @see org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseSchema
   */
  public void mergeDatabaseSchema(DatabaseSchema dbs)
  {
    try
    {
      logger.info(Translate.get("resultcache.merging.new.database.schema"));
      cdbs.mergeSchema(new CacheDatabaseSchema(dbs));
    }
    catch (Exception e)
    {
      logger.error(Translate.get("resultcache.error.while.merging", e));
    }
  }

  /**
   * Add a rule for this <code>ResultCache</code>
   * 
   * @param rule that contains information on the action to perform for a
   *          specific query
   */
  public void addCachingRule(ResultCacheRule rule)
  {
    cachingRules.add(rule);
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.AbstractResultCache#getDefaultRule()
   */
  public ResultCacheRule getDefaultRule()
  {
    return defaultRule;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.AbstractResultCache#setDefaultRule(ResultCacheRule)
   */
  public void setDefaultRule(ResultCacheRule defaultRule)
  {
    this.defaultRule = defaultRule;
  }

  /**
   * Finds the behavior of the cache with the given query skeleton. If the query
   * match a pattern of a rule then we get the associated action for this,
   * otherwise we look for the default behavior.
   * 
   * @param request to get action for
   * @return the <code>CacheBehavior</code> associated for this query.
   */
  private CacheBehavior getCacheBehavior(SelectRequest request)
  {
    CacheBehavior behavior = null;
    for (Iterator iter = cachingRules.iterator(); iter.hasNext();)
    {
      behavior = ((ResultCacheRule) iter.next()).matches(request);
      if (behavior != null)
      {
        break;
      }
    }
    if (behavior == null)
      behavior = defaultRule.getCacheBehavior();
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("resultcache.behavior.for.request",
          new String[]{request.getSQL(), behavior.getType()}));
    return behavior;
  }

  /*
   * Cache Management
   */

  /**
   * Creates a unique cache entry key from the given request. The key is
   * currently composed of the login name and the request SQL statement.
   * 
   * @param request the request to generate the key from
   * @return a unique cache key for this request
   */
  private String getCacheKeyFromRequest(SelectRequest request)
  {
    return request.getLogin() + "," + request.getSQL();
  }

  /**
   * Do we need invalidation after an update request, given a
   * ControllerResultSet. Note that this method is meant to be used with unique
   * queries where the ControllerResultSet is the result of a pk selection (like
   * an Entity Bean).
   * 
   * @param result that could be in the cache
   * @param request the update we want to get updated values from
   * @return boolean[] {needInvalidate,needToSendQuery}
   */
  public boolean[] needInvalidate(ControllerResultSet result,
      UpdateRequest request)
  {
    HashMap updatedValues = request.getUpdatedValues();
    boolean needInvalidate = false;
    boolean needToSendQuery = false;
    String value;
    String columnName;
    try
    {
      // If we don't have exactly one row, we don't handle the optimization
      if ((result == null) || (result.getData() == null)
          || (result.getData().size() != 1))
        return TRUE_TRUE;
    }
    catch (Exception e)
    {
      return TRUE_TRUE;
    }
    Field[] fields = result.getFields();
    ArrayList data = result.getData();
    int size = fields.length;
    for (Iterator iter = updatedValues.keySet().iterator(); iter.hasNext();)
    {
      columnName = (String) iter.next();
      value = (String) updatedValues.get(columnName);
      for (int i = 0; i < size; i++)
      { // Find the corresponding column in the ResultSet by comparing column
        // names

        // We can have something like:
        // FIRSTNAME and ADDRESS.FIRSTNAME
        if (columnName.equals(fields[i].getFieldName()))
        {
          Object o = ((Object[]) data.get(0))[i];
          if (!value.equals(o))
          {
            // The value from the cache entry is different we need to update
            // the
            // cache and the database
            return TRUE_TRUE;
          }
          else
            break;
        }
      }
      // We don't need to invalidate the cache because the columns affected are
      // different but we need to send the query to the database.
      needToSendQuery = true;
      // We don't stop here though because other columns could be updated and
      // we
      // could need invalidation
    }
    return new boolean[]{needInvalidate, needToSendQuery};
  }

  /**
   * Adds an entry request/reply to the cache. Note that if the request was
   * already in the cache, only the result is updated.
   * 
   * @param request the request
   * @param result the result corresponding to the request
   * @exception CacheException if an error occurs
   */
  public void addToCache(SelectRequest request, ControllerResultSet result)
      throws CacheException
  {
    boolean notifyThread = false;

    try
    {
      synchronized (pendingQueries)
      {
        // Remove the pending query from the list and wake up
        // all waiting queries
        removeFromPendingQueries(request);

        String sqlQuery = getCacheKeyFromRequest(request);

        // Sanity checks
        if (request.getCacheAbility() == RequestType.UNCACHEABLE)
          throw new CacheException(Translate.get(
              "resultcache.uncacheable.request", sqlQuery));

        if (result == null)
          throw new CacheException(Translate.get("resultcache.null.result",
              sqlQuery));

        // Check against streamable ResultSets
        if (result.hasMoreData())
        {
          logger.info(Translate.get("resultcache.streamed.resultset", request
              .getSQLShortForm(20)));
          return;
        }

        if (logger.isDebugEnabled())
          logger.debug(Translate.get("resultcache.adding.query", sqlQuery));

        AbstractResultCacheEntry ce;
        synchronized (queries)
        {
          // Check first that the query is not already in the cache
          ce = (AbstractResultCacheEntry) queries.get(sqlQuery);
          if (ce == null)
          {
            // Not in cache, add this entry
            // check the rule
            CacheBehavior behavior = getCacheBehavior(request);
            ce = behavior.getCacheEntry(request, result, this);
            if (ce instanceof ResultCacheEntryNoCache)
              return;

            // Test size of cache
            if (maxEntries > 0)
            {
              int size = queries.size();
              if (size >= maxEntries)
                // LRU replacement policy: Remove the oldest cache entry
                removeOldest();
            }
            // Add to the cache
            queries.put(sqlQuery, ce);

            notifyThread = true;
          }
          else
          { // Oh, oh, already in cache ...
            if (ce.isValid())
              logger.warn(Translate.get(
                  "resultcache.modifying.result.valid.entry", sqlQuery));
            ce.setResult(result);
          }

          // Update LRU
          if (lruHead != null)
          {
            lruHead.setPrev(ce);
            ce.setNext(lruHead);
            ce.setPrev(null);
          }
          if (lruTail == null)
            lruTail = ce;
          lruHead = ce; // This is also fine if LRUHead == null
        }
        processAddToCache(ce);

        // process thread notification out of the synchronized block on
        // pending queries to avoid deadlock, while adding/removing
        // on cache
        if (notifyThread)
        {
          //      relaxed entry
          if (ce instanceof ResultCacheEntryRelaxed)
          {
            ResultCacheEntryRelaxed qcer = (ResultCacheEntryRelaxed) ce;
            synchronized (relaxedThread)
            {
              relaxedCache.add(qcer);
              if (qcer.getDeadline() < relaxedThread.getThreadWakeUpTime()
                  || relaxedThread.getThreadWakeUpTime() == 0)
              {
                relaxedThread.notify();
              }
            }
          }
          else if (ce instanceof ResultCacheEntryEager)
          {
            // eager entry
            ResultCacheEntryEager qcee = (ResultCacheEntryEager) ce;
            if (qcee.getDeadline() != AbstractResultCacheEntry.NO_DEADLINE)
            { // Only deal with entries that specify a timeout
              synchronized (eagerThread)
              {
                eagerCache.add(qcee);
                if (qcee.getDeadline() < eagerThread.getThreadWakeUpTime()
                    || eagerThread.getThreadWakeUpTime() == 0)
                {
                  eagerThread.notify();
                }
              }
            }
          }
        }
      }
    }
    catch (OutOfMemoryError oome)
    {
      flushCache();
      System.gc();
      logger.warn(Translate.get("cache.memory.error.cache.flushed", this
          .getClass()));
    }
  }

  /**
   * Process the add to cache to update implementation specific data structures.
   * 
   * @param qe to add to the cache.
   */
  protected abstract void processAddToCache(AbstractResultCacheEntry qe);

  /**
   * Gets the result to the given request from the cache. The returned
   * <code>AbstractResultCacheEntry</code> is <code>null</code> if the
   * request is not present in the cache.
   * <p>
   * An invalid <code>AbstractResultCacheEntry</code> may be returned (it
   * means that the result is <code>null</code>) but the already parsed query
   * can be retrieved from the cache entry.
   * 
   * @param request an SQL select request
   * @param addToPendingQueries <code>true</code> if the request must be added
   *          to the pending query list on a cache miss
   * @return the <code>AbstractResultCacheEntry</code> if found, else
   *         <code>null</code>
   */
  public AbstractResultCacheEntry getFromCache(SelectRequest request,
      boolean addToPendingQueries)
  {
    stats.addSelect();

    if (request.getCacheAbility() == RequestType.UNCACHEABLE)
    {
      stats.addUncacheable();
      return null;
    }

    String sqlQuery = getCacheKeyFromRequest(request);

    // Check if we have the same query pending
    synchronized (pendingQueries)
    {
      if (addToPendingQueries)
      {
        long timeout = pendingQueryTimeout;
        // Yes, wait for the result
        // As we use a single lock for all pending queries, we use a
        // while to re-check that this wake-up was for us!
        while (pendingQueries.contains(sqlQuery))
        {
          try
          {
            if (logger.isDebugEnabled())
              logger.debug(Translate.get("resultcache.waiting.pending.query",
                  sqlQuery));

            if (timeout > 0)
            {
              long start = System.currentTimeMillis();
              pendingQueries.wait(pendingQueryTimeout);
              long end = System.currentTimeMillis();
              timeout = timeout - (end - start);
              if (timeout <= 0)
              {
                logger.warn(Translate.get("resultcache.pending.query.timeout"));
                break;
              }
            }
            else
              pendingQueries.wait();
          }
          catch (InterruptedException e)
          {
            logger.warn(Translate.get("resultcache.pending.query.timeout"));
            break;
          }
        }
      }

      // Check the cache
      AbstractResultCacheEntry ce;
      synchronized (queries)
      {
        ce = (AbstractResultCacheEntry) queries.get(sqlQuery);
        if (ce == null)
        // if ((ce == null) || !ce.isValid())
        { // Cache miss or dirty entry
          if (addToPendingQueries)
          {
            pendingQueries.add(sqlQuery);
            // Add this query to the pending queries
            if (logger.isDebugEnabled())
            {
              logger.debug(Translate.get("resultcache.cache.miss"));
              logger.debug(Translate.get(
                  "resultcache.adding.to.pending.queries", sqlQuery));
            }
          }
          return null;
        }
        else
        { // Cache hit (must update LRU)
          // Move cache entry to head of LRU
          AbstractResultCacheEntry before = ce.getPrev();
          if (before != null)
          {
            AbstractResultCacheEntry after = ce.getNext();
            before.setNext(after);
            if (after != null)
              after.setPrev(before);
            else
              // We were the tail, update the tail
              lruTail = before;
            ce.setNext(lruHead);
            ce.setPrev(null);
            if (lruHead != ce)
              lruHead.setPrev(ce);
            lruHead = ce;
          }
          // else it was already the LRU head
        }
      }

      if (ce.getResult() == null)
      {
        if (addToPendingQueries)
        {
          pendingQueries.add(sqlQuery);
          // Add this query to the pending queries
          if (logger.isDebugEnabled())
          {
            logger.debug(Translate.get("resultcache.cache.miss"));
            logger.debug(Translate.get("resultcache.adding.to.pending.queries",
                sqlQuery));
          }
        }
        if (ce.isValid() && logger.isInfoEnabled())
          logger.info(Translate.get("resultcache.valid.entry.without.result",
              ce.getRequest().getSQL()));
      }
      else
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("resultcache.cache.hit", sqlQuery));
        stats.addHits();
      }

      return ce;
    }
  }

  /**
   * Removes an entry from the cache (both request and reply are dropped). The
   * request is NOT removed from the pending query list, but it shouldn't be in
   * this list.
   * 
   * @param request a <code>SelectRequest</code>
   */
  public void removeFromCache(SelectRequest request)
  {
    String sqlQuery = request.getSQL();

    if (logger.isDebugEnabled())
      logger.debug("Removing from cache: " + sqlQuery);

    synchronized (queries)
    {
      // Remove from the cache
      AbstractResultCacheEntry ce = (AbstractResultCacheEntry) queries
          .remove(sqlQuery);
      if (ce == null)
        return; // Was not in the cache!
      else
      {
        // Update result set
        ce.setResult(null);
        // Update LRU
        AbstractResultCacheEntry before = ce.getPrev();
        AbstractResultCacheEntry after = ce.getNext();
        if (before != null)
        {
          before.setNext(after);
          if (after != null)
            after.setPrev(before);
          else
            // We were the tail, update the tail
            lruTail = before;
        }
        else
        { // We are the LRUHead
          lruHead = ce.getNext();
          if (after != null)
            after.setPrev(null);
          else
            // We were the tail, update the tail
            lruTail = before;
        }
        // Remove links to other cache entries for GC
        ce.setNext(null);
        ce.setPrev(null);
      }
    }
  }

  /**
   * Removes an entry from the pending query list.
   * 
   * @param request a <code>SelectRequest</code>
   */
  public void removeFromPendingQueries(SelectRequest request)
  {
    String sqlQuery = getCacheKeyFromRequest(request);

    synchronized (pendingQueries)
    {
      // Remove the pending query from the list and wake up
      // all waiting queries
      if (pendingQueries.remove(sqlQuery))
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("resultcache.removing.pending.query",
              sqlQuery));
        pendingQueries.notifyAll();
      }
      else
        logger.warn(Translate.get("resultcache.removing.pending.query.failed",
            sqlQuery));
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.AbstractResultCache#isUpdateNecessary(org.objectweb.cjdbc.common.sql.UpdateRequest)
   */
  public abstract boolean isUpdateNecessary(UpdateRequest request)
      throws CacheException;

  /**
   * Notifies the cache that this write request has been issued, so that cache
   * coherency can be maintained. If the cache is distributed, this method is
   * reponsible for broadcasting this information to other caches.
   * 
   * @param request an <code>AbstractRequest</code> value
   * @exception CacheException if an error occurs
   */
  public void writeNotify(AbstractWriteRequest request) throws CacheException
  {
    // Update the stats
    if (request.isInsert())
      stats.addInsert();
    else if (request.isUpdate())
      stats.addUpdate();
    else if (request.isDelete())
      stats.addDelete();
    else if (request.isCreate())
    {
      stats.addCreate();
      // Create: we only need to update the schema
      if (parsingGranularity != ParsingGranularities.NO_PARSING)
      {
        CreateRequest createRequest = (CreateRequest) request;
        if (createRequest.altersDatabaseSchema()
            && (createRequest.getDatabaseTable() != null))
          cdbs
              .addTable(new CacheDatabaseTable(createRequest.getDatabaseTable()));
      }
      return;
    }
    else if (request.isDrop())
    {
      stats.addDrop();
      // Drop: we need to update the schema
      if (parsingGranularity != ParsingGranularities.NO_PARSING)
      {
        // Invalidate the cache entries associated with this table
        CacheDatabaseTable cdt = cdbs.getTable(request.getTableName());
        if (cdt != null)
        {
          cdt.invalidateAll();
          cdbs.removeTable(cdt);
          return;
        }
        // else: the table was not previously cached
        // (no previous 'select' requests on the table).
      }
    }
    else
    {
      stats.addUnknown();
    }
    if (logger.isDebugEnabled())
      logger.debug("Notifying write " + request.getSQL());

    processWriteNotify(request);
  }

  /**
   * Implementation specific invalidation of the cache.
   * 
   * @param request Write request that invalidates the cache.
   */
  protected abstract void processWriteNotify(AbstractWriteRequest request);

  /**
   * Removes all entries from the cache.
   */
  public void flushCache()
  {
    // Check if we are already flushing the cache
    synchronized (this)
    {
      if (flushingCache)
        return;
      flushingCache = true;
    }

    try
    {
      synchronized (queries)
      { // Invalidate the whole cache until it is empty
        while (!queries.isEmpty())
        {
          Iterator iter = queries.values().iterator();
          ((AbstractResultCacheEntry) iter.next()).invalidate();
        }
      }

      synchronized (pendingQueries)
      { // Clean pending queries to unblock everyone if some queries/backends
        // remained in an unstable state.
        pendingQueries.clear();
        pendingQueries.notifyAll();
      }
    }
    finally
    {
      synchronized (this)
      {
        flushingCache = false;
      }
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("resultcache.cache.flushed"));
    }
  }

  /**
   * Get Cache size
   * 
   * @return the approximate size of the cache in bytes
   */
  public long getCacheSize()
  {
    // No need to synchronize, the implementation returns an int
    return queries.size();
  }

  /**
   * Removes the oldest entry from the cache.
   * <p>
   * <b>!Warning! </b> This method is not synchronized and should be called in
   * the scope of a synchronized(queries)
   */
  private void removeOldest()
  {
    if (lruTail == null)
      return;
    // Update the LRU
    AbstractResultCacheEntry oldce = lruTail;
    lruTail = lruTail.getPrev();
    if (lruTail != null)
      lruTail.setNext(null);

    if (logger.isDebugEnabled())
      logger.debug(Translate.get("resultcache.removing.oldest.cache.entry",
          oldce.getRequest().getSQL()));

    /*
     * We remove the query from the hashtable so that the garbage collector can
     * do its job. We need to remove the query from the queries HashTable first
     * in case we invalidate an eager cache entry that will call removeFromCache
     * (and will try to update the LRU is the entry is still in the queries
     * HashTable). So, to be compatible with all type of cache entries: 1.
     * queries.remove(ce) 2. ce.invalidate
     */
    queries.remove(oldce.getRequest().getSQL());

    if (oldce.isValid())
    {
      oldce.setResult(null);
      oldce.invalidate();
    }

    stats.addRemove();
  }

  /**
   * Gets the needed query parsing granularity.
   * 
   * @return needed query parsing granularity
   */
  public int getParsingGranularity()
  {
    return this.parsingGranularity;
  }

  /**
   * Retrieve the name of this cache
   * 
   * @return name
   */
  public abstract String getName();

  //
  // Transaction management
  //

  /**
   * Commit a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @throws CacheException if an error occurs
   */
  public void commit(long transactionId) throws CacheException
  {
    // Ok, the transaction has commited, nothing to do
  }

  /**
   * Rollback a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @throws CacheException if an error occurs
   */
  public void rollback(long transactionId) throws CacheException
  {
    logger.info(Translate.get("resultcache.flushing.cache.cause.rollback",
        transactionId));
    flushCache();
  }

  /*
   * Debug/Monitoring
   */

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.AbstractResultCache#getCacheData
   */
  public String[][] getCacheData() throws CacheException
  {
    try
    {
      synchronized (queries)
      {
        String[][] data = new String[queries.size()][];
        int count = 0;
        for (Iterator iter = queries.values().iterator(); iter.hasNext(); count++)
        {
          AbstractResultCacheEntry qe = (AbstractResultCacheEntry) iter.next();
          if (qe != null)
          {
            data[count] = qe.toStringTable();
          }
        }
        return data;
      }
    }
    catch (Exception e)
    {
      logger.error(Translate.get("resultcache.error.retrieving.cache.data", e));
      throw new CacheException(e.getMessage());
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.AbstractResultCache#getCacheStatsData()
   */
  public String[][] getCacheStatsData() throws CacheException
  {
    String[][] data = new String[1][];
    String[] stat = stats.getCacheStatsData();
    data[0] = new String[stat.length + 1];
    for (int i = 0; i < stat.length; i++)
      data[0][i] = stat[i];
    data[0][data[0].length - 1] = "" + queries.size();
    return data;
  }

  /**
   * @return Returns the stats.
   */
  public CacheStatistics getCacheStatistics()
  {
    return stats;
  }

  /**
   * Returns the eagerCache value.
   * 
   * @return Returns the eagerCache.
   */
  public ArrayList getEagerCache()
  {
    return eagerCache;
  }

  /**
   * Returns the relaxedCache value.
   * 
   * @return Returns the relaxedCache.
   */
  public ArrayList getRelaxedCache()
  {
    return relaxedCache;
  }

  /**
   * Gets information about the request cache
   * 
   * @return <code>String</code> containing information
   */
  protected String getXmlImpl()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_ResultCache + " "
        + DatabasesXmlTags.ATT_pendingTimeout + "=\"" + pendingQueryTimeout
        + "\" " + DatabasesXmlTags.ATT_maxNbOfEntries + "=\"" + maxEntries
        + "\" " + DatabasesXmlTags.ATT_granularity + "=\"" + getName() + "\">");
    info.append("<" + DatabasesXmlTags.ELT_DefaultResultCacheRule + " "
        + DatabasesXmlTags.ATT_timestampResolution + "=\""
        + defaultRule.getTimestampResolution() / 1000 + "\">");
    info.append(defaultRule.getCacheBehavior().getXml());
    info.append("</" + DatabasesXmlTags.ELT_DefaultResultCacheRule + ">");
    for (Iterator iter = cachingRules.iterator(); iter.hasNext();)
      info.append(((ResultCacheRule) iter.next()).getXml());
    info.append("</" + DatabasesXmlTags.ELT_ResultCache + ">");
    return info.toString();
  }

}