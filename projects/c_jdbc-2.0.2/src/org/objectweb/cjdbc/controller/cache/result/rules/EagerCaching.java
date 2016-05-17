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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): 
 */

package org.objectweb.cjdbc.controller.cache.result.rules;

import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.controller.cache.result.CacheBehavior;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;
import org.objectweb.cjdbc.controller.cache.result.entries.ResultCacheEntryEager;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * EagerCaching means that all entries in the cache are always coherent and any
 * update query (insert,delete,update,...) will automatically invalidate the
 * corresponding entry in the cache. This was the previous cache behavior for
 * all queries
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class EagerCaching extends CacheBehavior
{
  private long timeout;

  /**
   * Define this CacheBehavior as EagerCaching
   * 
   * @param timeout Timeout for this cache entry
   */
  public EagerCaching(long timeout)
  {
    this.timeout = timeout;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.CacheBehavior#getCacheEntry(SelectRequest,
   *           ControllerResultSet, AbstractResultCache)
   */
  public AbstractResultCacheEntry getCacheEntry(SelectRequest sqlQuery,
      ControllerResultSet result, AbstractResultCache cache)
  {
    return new ResultCacheEntryEager(cache, sqlQuery, result, timeout);
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    return "<" + DatabasesXmlTags.ELT_EagerCaching + "/>";
  }

}