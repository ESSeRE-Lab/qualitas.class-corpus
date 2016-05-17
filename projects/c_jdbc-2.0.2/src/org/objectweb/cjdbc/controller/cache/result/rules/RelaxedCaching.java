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
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.controller.cache.result.rules;

import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.cache.result.AbstractResultCache;
import org.objectweb.cjdbc.controller.cache.result.CacheBehavior;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;
import org.objectweb.cjdbc.controller.cache.result.entries.ResultCacheEntryRelaxed;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * RelaxedCaching means we set a timeout value for this entry, and when expired
 * we keep in the cache if no write has modified the corresponding result, we
 * wait for the same amount of time again. RelaxedCaching may provide stale
 * data. The timeout defines the maximum staleness of a cache entry. It means
 * that the cache may return an entry that is out of date. timeout: is a value
 * in seconds and 0 means no timeout (always in the cache) keepIfNotDirty: if
 * true the entry is kept in the cache and the timeout is reset, if false, the
 * entry is removed from the cache after the timeout has expired even if the
 * entry was not affected by a write.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class RelaxedCaching extends CacheBehavior
{
  private long    timeout;
  private boolean keepIfNotDirty;

  /**
   * Create new RelaxedCaching action
   * 
   * @param timeout before we check the validity of an entry
   * @param keepIfNotDirty true if non-dirty entries must be kept in the cache
   */
  public RelaxedCaching(boolean keepIfNotDirty, long timeout)
  {
    this.keepIfNotDirty = keepIfNotDirty;
    this.timeout = timeout;
  }

  /**
   * @see org.objectweb.cjdbc.controller.cache.result.CacheBehavior#getCacheEntry(SelectRequest,
   *      ControllerResultSet, AbstractResultCache)
   */
  public AbstractResultCacheEntry getCacheEntry(SelectRequest sqlQuery,
      ControllerResultSet result, AbstractResultCache cache)
  {
    return new ResultCacheEntryRelaxed(sqlQuery, result, timeout,
        keepIfNotDirty);
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    return "<" + DatabasesXmlTags.ELT_RelaxedCaching + " "
        + DatabasesXmlTags.ATT_timeout + "=\"" + timeout / 1000 + "\" "
        + DatabasesXmlTags.ATT_keepIfNotDirty + "=\"" + keepIfNotDirty + "\"/>";
  }

}