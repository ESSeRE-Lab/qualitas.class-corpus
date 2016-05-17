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

/**
 * Error checking using all backends.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class ErrorCheckingAll extends ErrorCheckingPolicy
{

  /**
   * Creates a new <code>ErrorCheckingAll</code> instance.
   */
  public ErrorCheckingAll()
  {
    // We don't care about the number of nodes but the father's constructor
    // needs at least 3 nodes.
    super(ErrorCheckingPolicy.ALL, 3);
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.policies.errorchecking.ErrorCheckingPolicy#getBackends(ArrayList)
   */
  public ArrayList getBackends(ArrayList backends)
    throws ErrorCheckingException
  {
    return backends;
  }

  /**
   * @see org.objectweb.cjdbc.controller.loadbalancer.policies.errorchecking.ErrorCheckingPolicy#getInformation()
   */
  public String getInformation()
  {
    return "Error checking using all backends";
  }
}
