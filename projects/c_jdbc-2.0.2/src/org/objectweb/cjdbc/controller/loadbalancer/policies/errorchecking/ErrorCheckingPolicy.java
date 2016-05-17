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
 * Contributor(s): _______________________
 */

package org.objectweb.cjdbc.controller.loadbalancer.policies.errorchecking;

import java.util.ArrayList;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * Defines the policy to adopt for error checking.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public abstract class ErrorCheckingPolicy
{
  /** Pickup backends randomly. */
  public static final int RANDOM      = 0;

  /** Backends are chosen using a round-robin algorithm. */
  public static final int ROUND_ROBIN = 1;

  /** Request is sent to all backends. */
  public static final int ALL         = 2;

  /** Number of nodes that are involved in error-checking per request. */
  protected int           nbOfNodes   = 0;

  protected int           policy;

  /**
   * Creates a new <code>CreateTableRule</code>.
   * 
   * @param policy implemented policy
   * @param numberOfNodes number of nodes to use to check for errors on a query
   */
  public ErrorCheckingPolicy(int policy, int numberOfNodes)
  {
    setPolicy(policy);
    setNumberOfNodes(numberOfNodes);
  }

  /**
   * Returns the number of nodes.
   * 
   * @return an <code>int</code> value
   * @see #setNumberOfNodes
   */
  public int getNumberOfNodes()
  {
    return nbOfNodes;
  }

  /**
   * Sets the number of nodes.
   * 
   * @param numberOfNodes the number of nodes to set
   * @see #getNumberOfNodes
   */
  public void setNumberOfNodes(int numberOfNodes)
  {
    if (numberOfNodes < 3)
      throw new IllegalArgumentException(
          "You must use at least 3 nodes for error checking (" + numberOfNodes
              + " is not acceptable)");
    this.nbOfNodes = numberOfNodes;
  }

  /**
   * Returns the policy.
   * 
   * @return an <code>int</code> value
   * @see #setPolicy
   */
  public int getPolicy()
  {
    return policy;
  }

  /**
   * Sets the policy.
   * 
   * @param policy the policy to set
   * @see #getPolicy
   */
  public void setPolicy(int policy)
  {
    this.policy = policy;
  }

  /**
   * Pickups backends from the given backends arraylist according to the current
   * rule policy.
   * 
   * @param backends backends to choose from
   * @return Arraylist of choosen <code>DatabaseBackend</code>
   * @exception ErrorCheckingException if the rule cannot be applied
   */
  public abstract ArrayList getBackends(ArrayList backends)
      throws ErrorCheckingException;

  /**
   * Gives information about the current policy.
   * 
   * @return a <code>String</code> value
   */
  public abstract String getInformation();

  /**
   * Convert this error checking policy to xml
   * 
   * @return xml formatted string
   */
  public String getXml()

  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_ErrorChecking + " />"
        + DatabasesXmlTags.ATT_numberOfNodes + "=\"" + this.getNumberOfNodes()
        + "\" " + DatabasesXmlTags.ATT_policy + "=\"");
    switch (policy)
    {
      case RANDOM :
        info.append(DatabasesXmlTags.VAL_random);
        break;
      case ROUND_ROBIN :
        info.append(DatabasesXmlTags.VAL_roundRobin);
        break;
      case ALL :
        info.append(DatabasesXmlTags.VAL_all);
        break;
      default :
        break;
    }
    info.append("\"/>");
    return info.toString();
  }
}
