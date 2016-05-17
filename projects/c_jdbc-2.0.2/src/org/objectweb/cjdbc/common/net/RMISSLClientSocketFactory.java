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
 * Initial developer(s): Marc Wick.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.net;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * This class defines a RMISSLClientSocketFactory
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class RMISSLClientSocketFactory
    implements
      RMIClientSocketFactory,
      Serializable
{
  private static final long serialVersionUID = -5994304413561755872L;

  /**
   * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String,
   *      int)
   */
  public Socket createSocket(String host, int port) throws IOException
  {
    SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(
        host, port);
    if (System.getProperty("javax.net.ssl.trustStore") != null)
      socket.setNeedClientAuth(true);

    return socket;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   *      <p>
   *      http://developer.java.sun.com/developer/bugParade/bugs/4492317.html
   */
  public boolean equals(Object obj)
  {
    if (obj == null)
      return false;
    if (this == obj)
      return true;
    return getClass() == obj.getClass();
  }

  /**
   * @see java.lang.Object#hashCode()
   *      <p>
   *      http://developer.java.sun.com/developer/bugParade/bugs/4492317.html
   */
  public int hashCode()
  {
    return 13;
  }
}
