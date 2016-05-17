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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.jmx.mbeans;

/**
 * This class defines a RequestManagerMBean
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public interface RequestManagerMBean
{
  /**
   * Sets the parsing case sensitivity. If true the request are parsed in a case
   * sensitive way (table/column name must match exactly the case of the names
   * fetched from the database or enforced by a static schema).
   * 
   * @param isCaseSensitiveParsing true if parsing is case sensitive
   */
  void setCaseSensitiveParsing(boolean isCaseSensitiveParsing);

  /**
   * Returns the beginTimeout value.
   * 
   * @return Returns the beginTimeout.
   */
  long getBeginTimeout();

  /**
   * Sets the beginTimeout value.
   * 
   * @param beginTimeout The beginTimeout to set.
   */
  void setBeginTimeout(long beginTimeout);

  /**
   * Returns the cacheParsingranularity value.
   * 
   * @return Returns the cacheParsingranularity.
   */
  int getCacheParsingranularity();

  /**
   * Sets the cacheParsingranularity value.
   * 
   * @param cacheParsingranularity The cacheParsingranularity to set.
   */
  void setCacheParsingranularity(int cacheParsingranularity);

  /**
   * Returns the commitTimeout value.
   * 
   * @return Returns the commitTimeout.
   */
  long getCommitTimeout();

  /**
   * Sets the commitTimeout value.
   * 
   * @param commitTimeout The commitTimeout to set.
   */
  void setCommitTimeout(long commitTimeout);

  /**
   * Returns the loadBalancerParsingranularity value.
   * 
   * @return Returns the loadBalancerParsingranularity.
   */
  int getLoadBalancerParsingranularity();

  /**
   * Sets the loadBalancerParsingranularity value.
   * 
   * @param loadBalancerParsingranularity The loadBalancerParsingranularity to
   *          set.
   */
  void setLoadBalancerParsingranularity(int loadBalancerParsingranularity);

  /**
   * Returns the requiredParsingGranularity value.
   * 
   * @return Returns the requiredParsingGranularity.
   */
  int getRequiredParsingGranularity();

  /**
   * Sets the requiredParsingGranularity value.
   * 
   * @param requiredGranularity The required ParsingGranularity to set.
   */
  void setRequiredParsingGranularity(int requiredGranularity);

  /**
   * Returns the rollbackTimeout value.
   * 
   * @return Returns the rollbackTimeout.
   */
  long getRollbackTimeout();

  /**
   * Sets the rollbackTimeout value.
   * 
   * @param rollbackTimeout The rollbackTimeout to set.
   */
  void setRollbackTimeout(long rollbackTimeout);

  /**
   * Returns the schedulerParsingranularity value.
   * 
   * @return Returns the schedulerParsingranularity.
   */
  int getSchedulerParsingranularity();

  /**
   * Sets the schedulerParsingranularity value.
   * 
   * @param schedulerParsingranularity The schedulerParsingranularity to set.
   */
  void setSchedulerParsingranularity(int schedulerParsingranularity);

  /**
   * Returns the schemaIsStatic value.
   * 
   * @return Returns the schemaIsStatic.
   */
  boolean isSchemaIsStatic();

  /**
   * Sets the schemaIsStatic value.
   * 
   * @param schemaIsStatic The schemaIsStatic to set.
   */
  void setSchemaIsStatic(boolean schemaIsStatic);

  /**
   * Returns the isCaseSensitiveParsing value.
   * 
   * @return Returns the isCaseSensitiveParsing.
   */
  boolean isCaseSensitiveParsing();
}