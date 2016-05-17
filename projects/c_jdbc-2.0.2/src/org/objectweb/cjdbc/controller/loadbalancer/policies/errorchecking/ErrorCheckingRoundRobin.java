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

import org.objectweb.cjdbc.controller.backend.DatabaseBackend;

/**
 * Chooses the number of nodes nodes for error checking using a round-robin
 * algorithm.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class ErrorCheckingRoundRobin extends ErrorCheckingPolicy
{
  /** Round-robin index. */
  private int index = 0;

  /**
   * Creates a new <code>ErrorCheckingRoundRobin</code> instance.
   * 
   * @param numberOfNodes number of nodes
   */
  public ErrorCheckingRoundRobin(int numberOfNodes)
  {
    super(ErrorCheckingPolicy.ROUND_ROBIN, numberOfNodes);
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.policies.errorchecking.ErrorCheckingPolicy#getBackends(ArrayList)
   */
  public ArrayList getBackends(ArrayList backends)
    throws ErrorCheckingException
  {
    int size = backends.size();

    if (nbOfNodes == 0)
      return null;
    else if (nbOfNodes == size)
      return backends;

    ArrayList result = new ArrayList(nbOfNodes);
    ArrayList clonedList = new ArrayList(size);
    for (int i = 0; i < size; i++)
    { // Take all enabled backends
      DatabaseBackend db = (DatabaseBackend) backends.get(i);
      if (db.isReadEnabled() || db.isWriteEnabled())
        clonedList.add(db);
    }

    int clonedSize = clonedList.size();

    if (nbOfNodes == clonedSize)
      return backends;
    else if (nbOfNodes > clonedSize)
      throw new ErrorCheckingException(
        "Asking for more backends ("
          + nbOfNodes
          + ") than available ("
          + clonedSize
          + ")");

    synchronized (this)
    { // index must be modified in mutual exclusion
      for (int i = 0; i < nbOfNodes; i++)
      {
        index = (index + 1) % clonedSize;
        result.add(clonedList.remove(index));
      }
    }

    return result;
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.policies.errorchecking.ErrorCheckingPolicy#getInformation()
   */
  public String getInformation()
  {
    return "Error checking using "
      + nbOfNodes
      + " nodes choosen using a round-robin algorithm";
  }
}
