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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.virtualdatabase.protocol;

import java.io.Serializable;

import org.objectweb.tribe.common.GroupIdentifier;
import org.objectweb.tribe.messages.GroupMessage;

/**
 * This class defines a CJDBCGroupMessage.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class CJDBCGroupMessage extends GroupMessage
{
  private static final long serialVersionUID = 6314846800150706597L;

  /**
   * Creates a new <code>CJDBCGroupMessage</code> object
   * 
   * @param msg Message to send
   * @param gid Group id where to send the message
   */
  public CJDBCGroupMessage(Serializable msg, GroupIdentifier gid)
  {
    super(msg, gid);
  }

  /**
   * The default timeout when waiting for group message
   */
  public static long defaultCastTimeOut = 0;

}
