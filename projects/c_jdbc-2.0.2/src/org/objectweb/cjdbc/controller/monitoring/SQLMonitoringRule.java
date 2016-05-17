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
package org.objectweb.cjdbc.controller.monitoring;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This class implements a SQL monitoring rule.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class SQLMonitoringRule
{
  private RE queryPattern;
  private boolean isCaseSentive;
  private boolean applyToSkeleton;
  private boolean monitoring;

  /**
   * Creates a new SQL Monitoring rule
   * 
   * @param queryPattern the query pattern to match
   * @param isCaseSentive true if matching is case sensitive
   * @param applyToSkeleton true if matching applies to the query skeleton
   * @param monitoring true if the request must be monitored
   */
  public SQLMonitoringRule(
    String queryPattern,
    boolean isCaseSentive,
    boolean applyToSkeleton,
    boolean monitoring)
  {
    try
    {
      this.queryPattern = new RE(queryPattern);
    }
    catch (RESyntaxException e)
    {
      throw new RuntimeException(
        "Invalid regexp in SQL Monitoring rule (" + e + ")");
    }
    this.isCaseSentive = isCaseSentive;
    if (isCaseSentive)
      this.queryPattern.setMatchFlags(RE.MATCH_NORMAL);
    else
      this.queryPattern.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
    this.applyToSkeleton = applyToSkeleton;
    this.monitoring = monitoring;
  }

  /**
   * If matching is case sensitive or not
   * 
   * @return true if the matching is case sensitive
   */
  public boolean isCaseSentive()
  {
    return isCaseSentive;
  }

  /**
   * If monitoring is activated or not.
   * 
   * @return true if monitoring is activated for this pattern
   */
  public boolean isMonitoring()
  {
    return monitoring;
  }

  /**
   * Get query pattern
   * 
   * @return the query pattern
   */
  public String getQueryPattern()
  {
    return queryPattern.toString();
  }

  /**
   * Set the matching case sensitiveness
   * 
   * @param b true if matching is case sensitive
   */
  public void setCaseSentive(boolean b)
  {
    isCaseSentive = b;
  }

  /**
   * Set the monitoring on or off
   * 
   * @param b true if monitoring must be activated for this rule
   */
  public void setMonitoring(boolean b)
  {
    monitoring = b;
  }

  /**
   * Sets the query pattern
   * 
   * @param queryPattern the queryPattern
   */
  public void setQueryPattern(String queryPattern)
  {
    try
    {
      this.queryPattern = new RE(queryPattern);
    }
    catch (RESyntaxException e)
    {
      throw new RuntimeException(
        "Invalid regexp in SQL Monitoring rule (" + e + ")");
    }
  }

  /**
   * If the pattern apply to the skeleton ot the instanciated query.
   * 
   * @return true if the pattern apply to the query skeleton
   */
  public boolean isApplyToSkeleton()
  {
    return applyToSkeleton;
  }

  /**
   * Set to true if the pattern apply to the query skeleton
   * 
   * @param b true if the pattern apply to the query skeleton
   */
  public void setApplyToSkeleton(boolean b)
  {
    applyToSkeleton = b;
  }

  /**
   * Returns true if the given query matches the pattern of this rule. This
   * function applies the applytoSkeleton rule.
   * 
   * @param request the query
   * @return the SQL that matches the rule or null if it does not match
   */
  public String matches(AbstractRequest request)
  {
    if (applyToSkeleton)
    {
      String skel = request.getSqlSkeleton();
      if (skel == null)
        return null;
      else
      {
        if (queryPattern.match(skel))
          return skel;
        else
          return null;
      }
    }
    else
    {
      if (queryPattern.match(request.getSQL()))
        return request.getSQL();
      else
        return null;
    }
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    String info =
      "<"
        + DatabasesXmlTags.ELT_SQLMonitoringRule
        + " "
        + DatabasesXmlTags.ATT_queryPattern
        + "=\""
        + getQueryPattern()
        + "\" "
        + DatabasesXmlTags.ATT_caseSensitive
        + "=\""
        + isCaseSentive()
        + "\" "
        + DatabasesXmlTags.ATT_applyToSkeleton
        + "=\""
        + isApplyToSkeleton()
        + "\" "
        + DatabasesXmlTags.ATT_monitoring
        + "=\"";
    if (isMonitoring())
      info += "on";
    else
      info += "off";
    info += "\"/>";
    return info;
  }

}
