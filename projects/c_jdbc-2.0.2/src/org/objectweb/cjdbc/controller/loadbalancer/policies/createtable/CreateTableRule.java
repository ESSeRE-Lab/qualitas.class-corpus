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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Jean-Bernard van Zuylen
 */

package org.objectweb.cjdbc.controller.loadbalancer.policies.createtable;

import java.util.ArrayList;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;

/**
 * Defines the policy to adopt when creating a new table.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public abstract class CreateTableRule
{
  /** List of backend names to wait for. */
  protected ArrayList backendList;

  /** Number of nodes that must create the table. */
  protected int       nbOfNodes = 0;

  /**
   * Table name pattern to which this rule apply (null means it is the default
   * rule).
   */
  protected String    tableName = null;

  protected int       policy;

  /**
   * Constructor for CreateTableRule.
   * 
   * @param policy the implemented policy
   */
  public CreateTableRule(int policy)
  {
    this.policy = policy;
    backendList = new ArrayList();
  }

  /**
   * Creates a new <code>CreateTableRule</code> instance.
   * 
   * @param policy the implemented policy
   * @param backendList the backend list to use
   */
  public CreateTableRule(int policy, ArrayList backendList)
  {
    if (backendList == null)
      throw new IllegalArgumentException(
          "Null backendList in CreateTableRule constructor");

    this.policy = policy;
    this.backendList = backendList;
  }

  /**
   * Add a backend name to the list of backends to wait for.
   * 
   * @param name backend name
   */
  public void addBackendName(String name)
  {
    backendList.add(name);
  }

  /**
   * Returns the backendList.
   * 
   * @return ArrayList
   */
  public ArrayList getBackendList()
  {
    return backendList;
  }

  /**
   * Returns the number of nodes.
   * 
   * @return an <code>int</code> value
   */
  public int getNumberOfNodes()
  {
    return nbOfNodes;
  }

  /**
   * Sets the number of nodes.
   * 
   * @param numberOfNodes the number of nodes to set
   */
  public void setNumberOfNodes(int numberOfNodes)
  {
    this.nbOfNodes = numberOfNodes;
  }

  /**
   * Returns the table name.
   * 
   * @return a <code>String</code> value
   */
  public String getTableName()
  {
    return tableName;
  }

  /**
   * Sets the table name.
   * 
   * @param tableName the table name to set
   */
  public void setTableName(String tableName)
  {
    this.tableName = tableName;
  }

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
   * Returns <code>true</code> if this rule is the default rule.
   * 
   * @return <code>boolean</code>
   */
  public boolean isDefaultRule()
  {
    return this.tableName == null;
  }

  /**
   * Pickups backends from the given backends arraylist according to the current
   * rule policy.
   * 
   * @param backends backends to choose from
   * @return <code>Arraylist</code> of choosen <code>DatabaseBackend</code>
   * @throws CreateTableException in some specific implementations (not this
   *           one)
   */
  public ArrayList getBackends(ArrayList backends) throws CreateTableException
  {
    ArrayList clonedList;

    int size = backends.size();

    if (backendList.size() > 0)
    { // Keep only the backends that are affected by this rule
      clonedList = new ArrayList(size);
      for (int i = 0; i < size; i++)
      {
        DatabaseBackend db = (DatabaseBackend) backends.get(i);
        if (db.isWriteEnabled() && backendList.contains(db.getName()))
          clonedList.add(db);
      }
    }
    else
    { // Take all enabled backends
      clonedList = new ArrayList(size);
      for (int i = 0; i < size; i++)
      {
        DatabaseBackend db = (DatabaseBackend) backends.get(i);
        if (db.isWriteEnabled())
          clonedList.add(db);
      }
    }

    return clonedList;
  }

  /**
   * Gives information about the current policy.
   * 
   * @return a <code>String</code> value
   */
  public abstract String getInformation();

  /**
   * Gives information about the current policy in xml
   * 
   * @return a <code>String</code> value in xml
   */
  public String getXml()

  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_CreateTable + " "
        + DatabasesXmlTags.ATT_tableName + "=\"" + tableName + "\" "
        + DatabasesXmlTags.ATT_policy + "=\""
        + CreateTablePolicy.getXmlValue(policy) + "\" "
        + DatabasesXmlTags.ATT_numberOfNodes + "=\"" + nbOfNodes + "\">");
    ArrayList list = this.getBackendList();
    int count = list.size();
    for (int i = 0; i < count; i++)
    {
      info
          .append("<" + DatabasesXmlTags.ELT_BackendName + " "
              + DatabasesXmlTags.ATT_name + "=\"" + ((String) list.get(i))
              + "\"/>");
    }
    info.append("</" + DatabasesXmlTags.ELT_CreateTable + ">");
    return info.toString();
  }

}
