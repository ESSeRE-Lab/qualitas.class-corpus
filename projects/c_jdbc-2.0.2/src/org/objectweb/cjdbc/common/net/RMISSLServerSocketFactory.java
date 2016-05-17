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
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;

/**
 * This class defines a RMISSLServerSocketFactory
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class RMISSLServerSocketFactory
    implements
      RMIServerSocketFactory,
      Serializable
{
  private static final long serialVersionUID = -1173753000488037655L;

  ServerSocketFactory       factory;

  /**
   * Creates a new <code>RMISSLServerSocketFactory.java</code> object
   * 
   * @param socketFactory - the factory to be used
   */
  public RMISSLServerSocketFactory(ServerSocketFactory socketFactory)
  {
    this.factory = socketFactory;
  }

  /**
   * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
   */
  public ServerSocket createServerSocket(int port) throws IOException
  {
    SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(port);
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
    if (factory == null)
      return false;
    return (getClass() == obj.getClass() && factory.equals(factory));
  }

  /**
   * @see java.lang.Object#hashCode()
   *      <p>
   *      http://developer.java.sun.com/developer/bugParade/bugs/4492317.html
   */
  public int hashCode()
  {
    return factory.hashCode();
  }

}
