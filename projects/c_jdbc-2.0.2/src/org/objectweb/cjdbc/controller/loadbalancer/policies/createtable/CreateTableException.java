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
 * Contributor(s): _______________________
 */

package org.objectweb.cjdbc.controller.loadbalancer.policies.createtable;

import org.objectweb.cjdbc.common.exceptions.CJDBCException;

/**
 * A <code>CreateTableException</code> is thrown when a
 * <code>CreateTableRule</code> policy cannot be applied in the
 * rule.getBackends() method.
 * 
 * @see org.objectweb.cjdbc.controller.loadbalancer.policies.createtable.CreateTableRule#getBackends(java.util.ArrayList)
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class CreateTableException extends CJDBCException
{
  private static final long serialVersionUID = -1818582588221008537L;

  /**
   * Creates a new <code>CreateTableException</code>.
   */
  public CreateTableException()
  {
    super();
  }

  /**
   * Creates a new <code>CreateTableException</code>.
   * 
   * @param message the error message
   */
  public CreateTableException(String message)
  {
    super(message);
  }

  /**
   * Creates a new <code>CreateTableException</code>.
   * 
   * @param cause the root cause
   */
  public CreateTableException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Creates a new <code>CreateTableException</code>.
   * 
   * @param message the error message
   * @param cause the root cause
   */
  public CreateTableException(String message, Throwable cause)
  {
    super(message, cause);
  }
}