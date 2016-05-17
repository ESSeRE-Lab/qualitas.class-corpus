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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.common.jmx;

import org.objectweb.cjdbc.common.exceptions.CJDBCException;
/**
 * Generic JMX Exception
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class JmxException extends CJDBCException
{
  private static final long serialVersionUID = 2591479799351433445L;

  /**
   * Creates a new <code>JmxException</code> instance.
   */
  public JmxException()
  {
  }

  /**
   * Creates a new <code>JmxException</code> instance.
   * 
   * @param message the error message
   */
  public JmxException(String message)
  {
    super(message);
  }

  /**
   * Creates a new <code>JmxException</code> instance.
   * 
   * @param cause the root cause
   */
  public JmxException(Throwable cause)
  {
    super(cause);
  }


}
