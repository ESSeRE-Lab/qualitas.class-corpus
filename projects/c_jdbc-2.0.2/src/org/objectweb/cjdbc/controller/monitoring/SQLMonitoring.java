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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.util.Stats;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This class implements a SQL monitoring module.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class SQLMonitoring extends Monitoring
{
  private Hashtable    statList;     // SQL query -> Stat
  private ArrayList    ruleList;
  private boolean      defaultRule;

  private static Trace logger = null;

  /**
   * Create a SQLMonitoring object.
   * 
   * @param vdbName name of the virtual database to be used by the logger
   */
  public SQLMonitoring(String vdbName)
  {
    statList = new Hashtable();
    ruleList = new ArrayList();
    logger = Trace.getLogger("org.objectweb.cjdbc.controller.monitoring."
        + vdbName);
  }

  /**
   * @see org.objectweb.cjdbc.controller.monitoring.Monitoring#cleanStats()
   */
  public void cleanStats()
  {
    statList.clear();
  }
  
  /**
   * Log the time elapsed to execute the given request.
   * 
   * @param request the request executed
   * @param time time elapsed to execute this request
   */
  public final void logRequestTime(AbstractRequest request, long time)
  {
    Stats stat = getStatForRequest(request);
    if (stat == null)
      return;
    stat.incrementCount();
    stat.updateTime(time);
    if (logger.isDebugEnabled())
      logger.debug(time + " " + stat.getName());
  }

  /**
   * Log an error for the given request.
   * 
   * @param request the request that failed to execute
   */
  public final void logError(AbstractRequest request)
  {
    Stats stat = getStatForRequest(request);
    if (stat == null)
      return;
    stat.incrementError();
    if (logger.isDebugEnabled())
      logger.debug("ERROR " + stat.getName());
  }

  /**
   * Log a cache hit for the given request.
   * 
   * @param request the request that failed to execute
   */
  public final void logCacheHit(AbstractRequest request)
  {
    Stats stat = getStatForRequest(request);
    if (stat == null)
      return;
    stat.incrementCacheHit();
    if (logger.isDebugEnabled())
      logger.debug("Cache hit " + stat.getName());
  }

  /**
   * Reset the stats associated to a request.
   * 
   * @param request the request to reset
   */
  public final void resetRequestStat(AbstractRequest request)
  {
    Stats stat = getStatForRequest(request);
    if (stat == null)
      return;
    stat.reset();
  }

  /**
   * Retrieve the stat corresponding to a request and create it if it does not
   * exist.
   * 
   * @param request the request to look for
   * @return corresponding stat or null if a rule does not authorize this
   *         request to be monitored
   */
  public final Stats getStatForRequest(AbstractRequest request)
  {
    String sql = monitorRequestRule(request);
    if (sql == null)
      return null;

    // Note that the Hashtable is synchronized
    Stats stat = (Stats) statList.get(sql);
    if (stat == null)
    { // No entry for this query, create a new Stats entry
      stat = new Stats(sql);
      statList.put(sql, stat);
    }
    return stat;
  }

  /**
   * Return all stats information in the form of a String
   * 
   * @return stats information
   */
  public String[][] getAllStatsInformation()
  {
    Collection values = statList.values();
    String[][] result = new String[values.size()][];
    int i = 0;
    for (Iterator iter = values.iterator(); iter.hasNext(); i++)
    {
      Stats stat = (Stats) iter.next();
      result[i] = stat.toStringTable();
    }
    return result;
  }

  /**
   * Dump all stats using the current logger (INFO level).
   */
  public void dumpAllStatsInformation()
  {
    if (logger.isInfoEnabled())
    {
      for (Iterator iter = statList.values().iterator(); iter.hasNext();)
      {
        Stats stat = (Stats) iter.next();
        logger.info(stat.singleLineDisplay());
      }
    }
  }

  /*
   * Rules Management
   */

  /**
   * Get the default monitoring rule
   * 
   * @return true if default is monitoring enabled
   */
  public boolean getDefaultRule()
  {
    return defaultRule;
  }

  /**
   * Defines the default rule
   * 
   * @param monitoring true if on, false is off
   */
  public void setDefaultRule(boolean monitoring)
  {
    this.defaultRule = monitoring;
  }

  /**
   * Add a rule to the list.
   * 
   * @param rule the rule to add
   */
  public void addRule(SQLMonitoringRule rule)
  {
    this.ruleList.add(rule);
  }

  /**
   * Check the rule list to check of this request should be monitored or not.
   * 
   * @param request the query to look for
   * @return the SQL query to monitor or null if monitoring is off for this
   *         request
   */
  private String monitorRequestRule(AbstractRequest request)
  {
    for (int i = 0; i < ruleList.size(); i++)
    {
      SQLMonitoringRule rule = (SQLMonitoringRule) ruleList.get(i);
      String sql = rule.matches(request);
      if (sql != null)
      { // This rule matches
        if (rule.isMonitoring())
          return sql;
        else
          return null;
      }
    }

    // No rule matched, use the default rule
    if (defaultRule)
    {
      if (request.getSqlSkeleton() == null)
        return request.getSQL();
      else
        return request.getSqlSkeleton();
    }
    else
      return null;
  }

  /**
   * @return Returns the ruleList.
   */
  public ArrayList getRuleList()
  {
    return ruleList;
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXmlImpl()
  {
    String info = "<" + DatabasesXmlTags.ELT_SQLMonitoring + " "
        + DatabasesXmlTags.ATT_defaultMonitoring + "=\"";
    String defaultMonitoring = getDefaultRule()?"on":"off";
    info += defaultMonitoring;
    info += "\">";
    for (int i = 0; i < ruleList.size(); i++)
    {
      SQLMonitoringRule rule = (SQLMonitoringRule) ruleList.get(i);
      info += rule.getXml();
    }
    info += "</" + DatabasesXmlTags.ELT_SQLMonitoring + ">";
    return info;
  }

}