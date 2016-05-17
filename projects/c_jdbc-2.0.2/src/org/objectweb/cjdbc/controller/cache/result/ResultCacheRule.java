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

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;

/**
 * This is the to define cache rules in the cache. A <code>ResultCacheRule</code>
 * is defined by a queryPattern, set to 'default' if default rule, and a <code>CacheBehavior</code>.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class ResultCacheRule implements XmlComponent
{
  Trace logger = Trace.getLogger(ResultCacheRule.class.getName());
  private RE queryPattern;
  private String queryString;
  private boolean isCaseSensitive;
  private boolean applyToSkeleton;
  private long timestampResolution;
  private CacheBehavior behavior;

  /**
   * Creates a new <code>ResultCacheRule</code>
   * 
   * @param queryString for this rule
   * @param caseSensitive true if matching is case sensitive
   * @param applyToSkeleton true if rule apply to query skeleton
   * @param timestampResolution timestamp resolution for NOW() macro
   * @throws RESyntaxException if the query pattern is invalid
   */
  public ResultCacheRule(
    String queryString,
    boolean caseSensitive,
    boolean applyToSkeleton,
    long timestampResolution)
    throws RESyntaxException
  {
    this.queryString = queryString;
    queryPattern = new RE(queryString);
    this.isCaseSensitive = caseSensitive;
    this.applyToSkeleton = applyToSkeleton;
    this.timestampResolution = timestampResolution;
  }

  /**
   * Get the query pattern
   * 
   * @return the queryPattern for this <code>ResultCacheRule</code>
   */
  public RE getQueryPattern()
  {
    return this.queryPattern;
  }

  /**
   * Get the cache behavior
   * 
   * @return the <code>CacheBehavior</code> for this <code>ResultCacheRule</code>
   */
  public CacheBehavior getCacheBehavior()
  {
    return behavior;
  }

  /**
   * Set the cache behavior
   * 
   * @param behavior behavior for this rule
   */
  public void setCacheBehavior(CacheBehavior behavior)
  {
    this.behavior = behavior;
  }

  /**
   * Retrieve the timestamp resolution of this scheduler
   * 
   * @return timestampResolution
   */
  public long getTimestampResolution()
  {
    return this.timestampResolution;
  }

  /**
   * @param request we may want to add to the cache
   * @return the behavior to get the entry
   */
  public CacheBehavior matches(AbstractRequest request)
  {
    if (queryPattern.match(request.getSQL()))
    {
      return behavior;
    }
    else
      return null;
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append(
      "<"
        + DatabasesXmlTags.ELT_ResultCacheRule
        + " "
        + DatabasesXmlTags.ATT_queryPattern
        + "=\""
        + queryString
        + "\" "
        + DatabasesXmlTags.ATT_caseSensitive
        + "=\""
        + isCaseSensitive
        + "\" "
        + DatabasesXmlTags.ATT_applyToSkeleton
        + "=\""
        + applyToSkeleton
        + "\" "
        + DatabasesXmlTags.ATT_timestampResolution
        + "=\""
        + timestampResolution / 1000
        + "\" >");
    info.append(behavior.getXml());
    info.append("</" + DatabasesXmlTags.ELT_ResultCacheRule + ">");
    return info.toString();
  }

}