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

package org.objectweb.cjdbc.controller.cache.result;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractWriteRequest;
import org.objectweb.cjdbc.common.sql.ParsingGranularities;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.sql.UpdateRequest;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;
import org.objectweb.cjdbc.common.xml.XmlComponent;
import org.objectweb.cjdbc.controller.cache.CacheException;
import org.objectweb.cjdbc.controller.cache.CacheStatistics;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * This class defines the minimal functionnalities that a request cache must
 * provide.
 * <p>
 * Only read requests (<code>SELECT</code>s) can be cached, there is no
 * sense to cache writes as they do not provide any result to cache. However,
 * the cache must be notified of the write queries in order to maintain cache
 * coherency.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class AbstractResultCache implements XmlComponent
{
  //
  // How the code is organized?
  //
  // 1. Member variables
  // 2. Getter/Setter (possibly in alphabetical order)
  // 3. Cache management
  // 4. Transaction management
  // 5. Debug/Monitoring

  /**
   * Parsing granularity. Default is:
   * {@link org.objectweb.cjdbc.common.sql.ParsingGranularities#NO_PARSING}.
   */
  protected int          parsingGranularity = ParsingGranularities.NO_PARSING;

  /** Logger instance. */
  protected static Trace logger             = Trace
                                                .getLogger("org.objectweb.cjdbc.controller.cache");

  /*
   * Getter/Setter methods
   */

  /**
   * Gets the needed query parsing granularity.
   * 
   * @return needed query parsing granularity
   * @see #setParsingGranularity
   */
  public int getParsingGranularity()
  {
    return parsingGranularity;
  }

  /**
   * Sets the needed query parsing granularity.
   * 
   * @param parsingGranularity the query parsing granularity to set
   * @see #getParsingGranularity
   */
  public void setParsingGranularity(int parsingGranularity)
  {
    this.parsingGranularity = parsingGranularity;
  }

  /**
   * Sets the <code>DatabaseSchema</code> of the current virtual database.
   * 
   * @param dbs a <code>DatabaseSchema</code> value
   * @see org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseSchema
   */
  public void setDatabaseSchema(DatabaseSchema dbs)
  {
    if (logger.isInfoEnabled())
      logger.info(Translate.get("cache.schemas.not.supported"));
  }

  /**
   * Merge the given <code>DatabaseSchema</code> with the current one.
   * 
   * @param dbs a <code>DatabaseSchema</code> value
   * @see org.objectweb.cjdbc.controller.cache.result.schema.CacheDatabaseSchema
   */
  public void mergeDatabaseSchema(DatabaseSchema dbs)
  {
    if (logger.isInfoEnabled())
      logger.info(Translate.get("cache.scheduler.doesnt.support.schemas"));
  }

  /*
   * Cache Management
   */

  /**
   * Add precise management and configuration of the cache behavior. A cache
   * rule contains information on a query pattern and how to act if that pattern
   * was matched.
   * 
   * @param rule of action for the cache
   * @see org.objectweb.cjdbc.controller.cache.result.ResultCacheRule
   */
  public abstract void addCachingRule(ResultCacheRule rule);

  /**
   * Return the default cache rule
   * 
   * @return default query cache rule. Cannot be null
   */
  public abstract ResultCacheRule getDefaultRule();

  /**
   * Set the default query rule
   * 
   * @param defaultRule default rule to set
   */
  public abstract void setDefaultRule(ResultCacheRule defaultRule);

  /**
   * Adds an entry request/reply to the cache. Note that if the request was
   * already in the cache, its result must be updated in any case but the
   * request must never appear twice in the cache.
   * 
   * @param request the request
   * @param result the result corresponding to the request
   * @exception CacheException if an error occurs
   */
  public abstract void addToCache(SelectRequest request,
      ControllerResultSet result) throws CacheException;

  /**
   * Gets the result to the given request from the cache.
   * <p>
   * The returned <code>AbstractResultCacheEntry</code> is <code>null</code>
   * if the request is not present in the cache.
   * <p>
   * An invalid <code>CacheEntry</code> may be returned (it means that the
   * result is <code>null</code>) but the already parsed query can be
   * retrieved from the cache entry.
   * 
   * @param request an SQL select request
   * @param addToPendingQueries true if the request must be added to the pending
   *          query list on a cache miss
   * @return the <code>AbstractResultCacheEntry</code> if found, else null
   */
  public abstract AbstractResultCacheEntry getFromCache(SelectRequest request,
      boolean addToPendingQueries);

  /**
   * Removes an entry from the cache (both request and reply are dropped). The
   * request is NOT removed from the pending query list, but it shouldn't be in
   * this list.
   * 
   * @param request a <code>SelectRequest</code>
   */
  public abstract void removeFromCache(SelectRequest request);

  /**
   * Removes an entry from the pending query list.
   * 
   * @param request a <code>SelectRequest</code>
   */
  public abstract void removeFromPendingQueries(SelectRequest request);

  /**
   * Shutdown the result cache and all its threads.
   */
  public abstract void shutdown();

  /**
   * Notifies the cache that the given write request has been issued, so that
   * cache coherency can be maintained. If the cache is distributed, this method
   * is reponsible for broadcasting this information to other caches.
   * 
   * @param request an <code>AbstractWriteRequest</code> value
   * @exception CacheException if an error occurs
   */
  public abstract void writeNotify(AbstractWriteRequest request)
      throws CacheException;

  /**
   * Returns true if the cache does not contain the values that are given in the
   * update statement.
   * 
   * @param request the update request that needs to be executed
   * @return false if the request shouldn't be executed, true otherwise.
   * @exception CacheException if an error occurs
   */
  public abstract boolean isUpdateNecessary(UpdateRequest request)
      throws CacheException;

  /**
   * Removes all entries from the cache.
   */
  public abstract void flushCache();

  //
  // Transaction management
  //

  /**
   * Commit a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @throws CacheException if an error occurs
   */
  public abstract void commit(long transactionId) throws CacheException;

  /**
   * Rollback a transaction given its id.
   * 
   * @param transactionId the transaction id
   * @throws CacheException if an error occurs
   */
  public abstract void rollback(long transactionId) throws CacheException;

  /*
   * Debug/Monitoring
   */

  /**
   * Gets information about the request cache in xml
   * 
   * @return xml formatted <code>String</code> containing information
   */
  protected abstract String getXmlImpl();

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()

  {
    return getXmlImpl();
  }

  /**
   * Returns the content of the cache as displayable array of array of string
   * 
   * @return the data
   * @throws CacheException if fails
   */
  public abstract String[][] getCacheData() throws CacheException;

  /**
   * Returns a bunch of stats collected by the cache, such as cache hits.
   * 
   * @return the data
   * @throws CacheException if fails to collect the data.
   */
  public abstract String[][] getCacheStatsData() throws CacheException;

  /**
   * Returns pointer to the stats collector
   * 
   * @return <code>CacheStatistics</code> object
   */
  public abstract CacheStatistics getCacheStatistics();

  /**
   * Returns number of entries in the cache
   * 
   * @return integer value representing the total number of entries
   */
  public abstract long getCacheSize();

}