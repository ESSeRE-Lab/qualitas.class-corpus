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
 * Contributor(s): Sara Bouchenak.
 */

package org.objectweb.cjdbc.controller.cache.result;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This class defines request cache granularities.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class CachingGranularities
{
  /**
   * Database granularity: entries in the cache are invalidated every time a
   * write (INSERT/UPDATE/DELETE/DROP/...) is sent to the database.
   */
  public static final int DATABASE = 0;

  /**
   * Table granularity: entries in the cache are invalidated based on table
   * dependencies.
   */
  public static final int TABLE = 1;

  /**
   * Column granularity: entries in the cache are invalidated based on column
   * dependencies.
   */
  public static final int COLUMN = 2;

  /**
   * Column granularity with <code>UNIQUE</code> queries: same as <code>COLUMN</code>
   * except that <code>UNIQUE</code> queries that selects a single row based
   * on a key are invalidated only when needed.
   */
  public static final int COLUMN_UNIQUE = 3;

  /**
   * Gets the name corresponding to a cache granularity level.
   * 
   * @param cacheGrain cache granularity level
   * @return the name of the granularity level
   */
  public static final String getGranularityName(int cacheGrain)
  {
    switch (cacheGrain)
    {
      case DATABASE :
        return "DATABASE";
      case TABLE :
        return "TABLE";
      case COLUMN :
        return "COLUMN";
      case COLUMN_UNIQUE :
        return "COLUMN_UNIQUE";
      default :
        return "UNSUPPORTED";
    }
  }

  /**
   * This method is needed to convert the value into the corresponding xml
   * attribute value. If fails, returns noInvalidation granularity value so the
   * xml retrieved can be used.
   * 
   * @param cacheGrain cache granularity level
   * @return the xml attribute value of the granularity level
   */
  public static final String getGranularityXml(int cacheGrain)
  {
    switch (cacheGrain)
    {
      case DATABASE :
        return DatabasesXmlTags.VAL_database;
      case TABLE :
        return DatabasesXmlTags.VAL_table;
      case COLUMN :
        return DatabasesXmlTags.VAL_column;
      case COLUMN_UNIQUE :
        return DatabasesXmlTags.VAL_columnUnique;
      default :
        return DatabasesXmlTags.VAL_noInvalidation;
    }
  }
}
