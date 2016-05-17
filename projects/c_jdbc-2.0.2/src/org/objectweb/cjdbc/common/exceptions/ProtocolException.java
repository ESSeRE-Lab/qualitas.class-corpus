/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks
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
 * Initial developer(s): Marc Herbert
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.exceptions;

/**
 * This class defines a ProtocolException
 * 
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert </a>
 * @version 1.0
 */
public class ProtocolException extends java.io.IOException
{
  private static final long serialVersionUID = 5211274551332052100L;

  /**
   * Creates a new <code>ProtocolException</code> object
   */
  public ProtocolException()
  {
    super();
  }

  /**
   * Creates a new <code>ProtocolException</code> object
   * 
   * @param message the error message
   */
  public ProtocolException(String message)
  {
    super(message);
  }

}
