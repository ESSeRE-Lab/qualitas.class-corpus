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
 * Initial developer(s): Marc Wick.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * This class defines a AuthenticatedSSLSocketFactory
 * <p>
 * It is a wrapper around the socket factory in the constructor and sets the
 * setNeedClientAuth to true to enforce client authentication with the public
 * key
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class AuthenticatedServerSocketFactory extends SSLServerSocketFactory

{

  private SSLServerSocketFactory factory;

  /**
   * Creates a new <code>AuthenticatedSSLSocketFactory.java</code> object
   * 
   * @param factory - the factory
   */
  public AuthenticatedServerSocketFactory(SSLServerSocketFactory factory)
  {
    this.factory = factory;
  }

  /**
   * @see javax.net.ServerSocketFactory#createServerSocket(int)
   */
  public ServerSocket createServerSocket(int port) throws IOException,
      UnknownHostException
  {
    SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(port);
    socket.setNeedClientAuth(true);
    return socket;
  }

  /**
   * @see javax.net.ServerSocketFactory#createServerSocket(int,int)
   */
  public ServerSocket createServerSocket(int port, int backlog)
      throws IOException, UnknownHostException
  {
    SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(port,
        backlog);
    socket.setNeedClientAuth(true);
    return socket;
  }

  /**
   * @see javax.net.ServerSocketFactory#createServerSocket(int, int,
   *      java.net.InetAddress)
   */
  public ServerSocket createServerSocket(int port, int backlog,
      InetAddress ifAddress) throws IOException, UnknownHostException
  {
    SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(port,
        backlog, ifAddress);
    socket.setNeedClientAuth(true);
    return socket;
  }

  /**
   * @see javax.net.ssl.SSLServerSocketFactory#getDefaultCipherSuites()
   */
  public String[] getDefaultCipherSuites()
  {
    return factory.getDefaultCipherSuites();
  }

  /**
   * @see javax.net.ssl.SSLServerSocketFactory#getSupportedCipherSuites()
   */
  public String[] getSupportedCipherSuites()
  {
    return factory.getDefaultCipherSuites();
  }

}