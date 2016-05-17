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
import java.util.Random;

/**
 * Implements a random strategy for <code>CREATE TABLE</code> statements.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class CreateTableRandom extends CreateTableRule
{
  private Random random = new Random();

  /**
   * Creates a new <code>CreateTableRandom</code>.
   */
  public CreateTableRandom()
  {
    super(CreateTablePolicy.RANDOM);
  }

  /**
   * Creates a new <code>CreateTableRandom</code>.
   * 
   * @param backendList <code>ArrayList</code> of <code>DatabaseBackend</code>
   */
  public CreateTableRandom(ArrayList backendList)
  {
    super(CreateTablePolicy.RANDOM, backendList);
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTableRule#getBackends(ArrayList)
   */
  public ArrayList getBackends(ArrayList backends) throws CreateTableException
  {
    if (nbOfNodes == 0)
      return null;
    
    ArrayList clonedList = super.getBackends(backends);

    int clonedSize = clonedList.size();

    if (nbOfNodes == clonedSize)
      return clonedList;
    else if (nbOfNodes > clonedSize)
      throw new CreateTableException(
        "Asking for more backends ("
          + nbOfNodes
          + ") than available ("
          + clonedSize
          + ")");

    ArrayList result = new ArrayList(nbOfNodes);

    for (int i = 0; i < nbOfNodes; i++)
      result.add(clonedList.remove(random.nextInt(clonedSize - i)));

    return result;
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTableRule#getInformation()
   */
  public String getInformation()
  {
    String s;
    if (tableName == null)
      s = "Default rule create table on ";
    else
      s = "Rule for table " + tableName + " create table on ";

    return s + nbOfNodes + " node(s) randomly from " + backendList;
  }
}
