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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.controller.cache.result;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.controller.cache.result.entries.AbstractResultCacheEntry;
import org.objectweb.cjdbc.controller.virtualdatabase.ControllerResultSet;

/**
 * Abstract class for the different cache actions. We need this class for adding
 * versatility in the parameters of each Caching action.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public abstract class CacheBehavior
{
  Trace logger = Trace.getLogger(CacheBehavior.class.getName());

  protected CacheBehavior()
  {
    logger.debug(Translate.get("cachebehavior.new.action", getType()));
  }

  /**
   * The name of the class instance
   * 
   * @return class name of the current type
   */
  public String getType()
  {
    return this.getClass().getName();
  }

  /**
   * Builds a cache entry from a <code>SelectRequest</code> and a
   * <code>ControllerResultSet</code>. This cache entry can then be inserted
   * in the cache.
   * 
   * @param sqlQuery entry to add in the cache
   * @param result value to add in the cache
   * @param cache reference for EagerCaching in case the entry needs to remove
   *          itself from the cache.
   * @return the query cache entry to add to the cache
   */
  public abstract AbstractResultCacheEntry getCacheEntry(
      SelectRequest sqlQuery, ControllerResultSet result,
      AbstractResultCache cache);

  /**
   * Implementation specific xml dump of the cache behavior.
   * 
   * @return xml dump of the cache behavior
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public abstract String getXml();
}