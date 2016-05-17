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

package org.objectweb.cjdbc.controller.cache.metadata;

import java.util.Hashtable;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.driver.Field;

/**
 * This class implements a ResultSet metadata cache.
 * <p>
 * ResultSet Fields are kept here to prevent recomputing them and allocating
 * them each time a query is executed.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class MetadataCache
{
  private static Trace logger = Trace.getLogger(MetadataCache.class.getName());

  // SQL -> Field[]
  private Hashtable    metadataCache;

  // Schema.Table.Column name -> Field
  private Hashtable    fieldCache;
  private int          maxNbOfMetadata;
  private int          maxNbOfField;

  /**
   * Constructor for MetadataCache.
   * 
   * @param maxNbOfMetadata maximum nb of entries in metadata cache
   * @param maxNbOfField maximum nb of entries in field cache
   */
  public MetadataCache(int maxNbOfMetadata, int maxNbOfField)
  {
    metadataCache = new Hashtable(maxNbOfMetadata == 0
        ? 10000
        : maxNbOfMetadata);
    fieldCache = new Hashtable(maxNbOfField == 0 ? 100 : maxNbOfField);
    if (maxNbOfMetadata < 0)
      throw new RuntimeException(Translate.get("cache.metadata.invalid.size",
          maxNbOfMetadata));
    if (maxNbOfMetadata == 0)
      this.maxNbOfMetadata = Integer.MAX_VALUE;
    else
      this.maxNbOfMetadata = maxNbOfMetadata;
    if (maxNbOfField < 0)
      throw new RuntimeException(Translate.get("cache.metadata.invalid.size",
          maxNbOfField));
    if (maxNbOfField == 0)
      this.maxNbOfField = Integer.MAX_VALUE;
    else
      this.maxNbOfField = maxNbOfField;
  }

  /**
   * Flush the cache
   */
  public void flushCache()
  {
    synchronized (metadataCache)
    {
      metadataCache.clear();
    }
    synchronized (fieldCache)
    {
      fieldCache.clear();
    }
  }

  /**
   * Get metadata associated to a request.
   * <p>
   * Returns null if the cache contains no metadata for the given request.
   * 
   * @param request the request we look for
   * @return the metadata or null if not in cache
   */
  public Field[] getMetadata(AbstractRequest request)
  {
    String sqlSkeleton = request.getSqlSkeleton();
    if (sqlSkeleton != null)
      return (Field[]) metadataCache.get(sqlSkeleton);
    else
      return (Field[]) metadataCache.get(request.getSQL());
  }

  /**
   * Add a metadata entry to the cache and associate it to the given request.
   * 
   * @param request request to which the metadata belong
   * @param metadata metadata to cache
   */
  public void addMetadata(AbstractRequest request, Field[] metadata)
  {
    // Note that the underlying cache Hashtable is synchronized and we usually
    // do not need to synchronize on it.
    // As we will have to add a cache entry, check if the cache size is ok
    // else remove the first entry of the hashtable.
    while (metadataCache.size() > maxNbOfMetadata)
    { // Remove first entry from Hashtable. We need to synchronize here to be
      // sure that we are not trying to concurrently remove the first cache
      // entry.
      synchronized (metadataCache)
      {
        try
        {
          metadataCache.remove(metadataCache.keys().nextElement());
        }
        catch (Exception ignore)
        {
          break;
        }
      }
    }

    // Add to cache
    try
    {
      String sqlSkeleton = request.getSqlSkeleton();
      if (sqlSkeleton != null)
        metadataCache.put(sqlSkeleton, metadata);
      else
        metadataCache.put(request.getSQL(), metadata);
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
   * Get the field corresponding to a column name.
   * <p>
   * Returns null if the cache contains no field for the given name.
   * 
   * @param fullyQualifiedFieldName the field name (table.column) to look for
   * @return the corresponding Field or null if not in cache
   */
  public Field getField(String fullyQualifiedFieldName)
  {
    return (Field) fieldCache.get(fullyQualifiedFieldName);
  }

  /**
   * Add a Field entry to the cache and associate it to the given name.
   * 
   * @param fullyQualifiedFieldName table.column name that uniquely identifies
   *          the field
   * @param field field to cache
   */
  public void addField(String fullyQualifiedFieldName, Field field)
  {
    // Note that the underlying cache Hashtable is synchronized and we usually
    // do not need to synchronize on it.
    // As we will have to add a cache entry, check if the cache size is ok
    // else remove the first entry of the hashtable.
    while (fieldCache.size() > maxNbOfField)
    { // Remove first entry from Hashtable. We need to synchronize here to be
      // sure that we are not trying to concurrently remove the first cache
      // entry.
      synchronized (fieldCache)
      {
        try
        {
          fieldCache.remove(fieldCache.keys().nextElement());
        }
        catch (Exception ignore)
        {
          break;
        }
      }
    }
    // Add to cache
    try
    {
      fieldCache.put(fullyQualifiedFieldName, field);
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
   * Get xml information about this ParsingCache
   * 
   * @return <code>String</code> in xml formatted text
   */
  public String getXml()
  {
    return "<" + DatabasesXmlTags.ELT_MetadataCache + " "
        + DatabasesXmlTags.ATT_maxNbOfMetadata + "=\"" + maxNbOfMetadata
        + "\" " + DatabasesXmlTags.ATT_maxNbOfField + "=\""
        + (maxNbOfField == Integer.MAX_VALUE ? 0 : maxNbOfField) + "\"/>";
  }

}