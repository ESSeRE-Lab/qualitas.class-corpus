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
 * Contributor(s): 
 */

package org.objectweb.cjdbc.controller.cache.result;

import java.util.Hashtable;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.cache.result.rules.EagerCaching;
import org.objectweb.cjdbc.controller.cache.result.rules.NoCaching;
import org.objectweb.cjdbc.controller.cache.result.rules.RelaxedCaching;

/**
 * Create a cache that conforms to AbstractResultCache, that is implementation
 * independant
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class ResultCacheFactory
{
  /**
   * Get an instance of the current cache implementation
   * 
   * @param granularityValue of the parsing
   * @param maxEntries to the cache
   * @param pendingTimeout before pending query timeout
   * @return <code>ResultCache</code> implementation of the
   *         <code>AbstractResultCache</code>
   * @throws InstantiationException if parsing granularity is not valid
   */
  public static AbstractResultCache getCacheInstance(int granularityValue,
      int maxEntries, int pendingTimeout) throws InstantiationException
  {
    AbstractResultCache currentRequestCache = null;
    switch (granularityValue)
    {
      case CachingGranularities.TABLE :
        currentRequestCache = new ResultCacheTable(maxEntries, pendingTimeout);
        break;
      case CachingGranularities.DATABASE :
        currentRequestCache = new ResultCacheDatabase(maxEntries,
            pendingTimeout);
        break;
      case CachingGranularities.COLUMN :
        currentRequestCache = new ResultCacheColumn(maxEntries, pendingTimeout);
        break;
      case CachingGranularities.COLUMN_UNIQUE :
        currentRequestCache = new ResultCacheColumnUnique(maxEntries,
            pendingTimeout);
        break;
      default :
        throw new InstantiationException("Invalid Granularity Value");
    }
    return currentRequestCache;
  }

  /**
   * Get an instance of a cache behavior for this cache
   * 
   * @param behaviorString representation of this cache behavior, xml tag
   * @param options for different cache rules
   * @return an instance of a cache behavior
   */
  public static CacheBehavior getCacheBehaviorInstance(String behaviorString,
      Hashtable options)
  {
    if (behaviorString.equalsIgnoreCase(DatabasesXmlTags.ELT_NoCaching))
      return new NoCaching();
    if (behaviorString.equals(DatabasesXmlTags.ELT_EagerCaching))
    {
      // Timeout is in seconds: *1000      
      // 0, is no timeout, and 0x1000=0 !
      long timeout = 1000 * Long.parseLong((String) options
          .get(DatabasesXmlTags.ATT_timeout));
      return new EagerCaching(timeout);
    }
    if (behaviorString.equals(DatabasesXmlTags.ELT_RelaxedCaching))
    {
      // Timeout is in seconds: *1000
      long timeout = 1000 * Long.parseLong((String) options
          .get(DatabasesXmlTags.ATT_timeout));
      boolean keepIfNotDirty = new Boolean((String) options
          .get(DatabasesXmlTags.ATT_keepIfNotDirty)).booleanValue();
      return new RelaxedCaching(keepIfNotDirty, timeout);
    }
    else
      return null;
  }
}