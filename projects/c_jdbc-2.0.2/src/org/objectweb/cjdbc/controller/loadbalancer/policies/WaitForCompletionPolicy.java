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

package org.objectweb.cjdbc.controller.loadbalancer.policies;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
/**
 * Defines the policy to adopt before returning a result to the client.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class WaitForCompletionPolicy
{
  /** Return as soon as one node has completed the request. */
  public static final int FIRST = 0;

  /**
   * Return as soon as a majority (n/2+1) of nodes has completed the request.
   */
  public static final int MAJORITY = 1;

  /** Wait for all nodes to complete the request before returning the result. */
  public static final int ALL = 2;

  /** Policy (default is {@link #FIRST}). */
  private int policy = FIRST;

  /**
   * Returns the policy.
   * 
   * @return an <code>int</code> value
   */
  public int getPolicy()
  {
    return policy;
  }

  /**
   * Sets the policy.
   * 
   * @param policy the policy to set
   */
  public void setPolicy(int policy)
  {
    this.policy = policy;
  }

  /**
   * Gives information about the current policy.
   * 
   * @return a <code>String</code> value
   */
  public String getInformation()
  {
    switch (policy)
    {
      case FIRST :
        return "return when first node completes";
      case MAJORITY :
        return "return when a majority of nodes completes";
      case ALL :
        return "return when all nodes have completed";
      default :
        return "unknown policy";
    }
  }

  /**
   * Returns this wait policy in xml format.
   * 
   * @return xml formatted string
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append(
      "<"
        + DatabasesXmlTags.ELT_WaitForCompletion
        + " "
        + DatabasesXmlTags.ATT_policy
        + "=\"");
    switch (policy)
    {
      case FIRST :
        info.append(DatabasesXmlTags.VAL_first);
        break;
      case ALL :
        info.append(DatabasesXmlTags.VAL_all);
        break;
      case MAJORITY :
        info.append(DatabasesXmlTags.VAL_majority);
        break;
      default :
        }
    info.append("\"/>");
    return info.toString();
  }
}
