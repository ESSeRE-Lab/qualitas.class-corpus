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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

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
public class AuthenticatedSocketFactory extends SSLSocketFactory
    implements
      Serializable
{

  private static final long serialVersionUID = 3408254276587727154L;

  private SSLSocketFactory  factory;

  /**
   * Creates a new <code>AuthenticatedSSLSocketFactory.java</code> object
   * 
   * @param factory - the factory
   */
  public AuthenticatedSocketFactory(SSLSocketFactory factory)
  {
    this.factory = factory;
  }

  /**
   * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
   */
  public Socket createSocket(String host, int port) throws IOException,
      UnknownHostException
  {
    SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
    socket.setNeedClientAuth(true);
    return socket;
  }

  /**
   * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
   */
  public Socket createSocket(InetAddress host, int port) throws IOException
  {
    SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
    socket.setNeedClientAuth(true);
    return socket;
  }

  /**
   * @see javax.net.SocketFactory#createSocket(java.lang.String, int,
   *      java.net.InetAddress, int)
   */
  public Socket createSocket(String host, int port, InetAddress localAddress,
      int localPort) throws IOException, UnknownHostException
  {
    SSLSocket socket = (SSLSocket) factory.createSocket(host, port,
        localAddress, localPort);
    socket.setNeedClientAuth(true);
    return socket;
  }

  /**
   * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int,
   *      java.net.InetAddress, int)
   */
  public Socket createSocket(InetAddress address, int port,
      InetAddress localAddress, int localPort) throws IOException
  {
    SSLSocket socket = (SSLSocket) factory.createSocket(address, port,
        localAddress, localPort);
    socket.setNeedClientAuth(true);
    return socket;
  }

  /**
   * @see javax.net.ssl.SSLSocketFactory#createSocket(java.net.Socket,
   *      java.lang.String, int, boolean)
   */
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException
  {
    SSLSocket socket = (SSLSocket) factory.createSocket(s, host, port,
        autoClose);
    socket.setNeedClientAuth(true);
    return socket;
  }

  /**
   * @see javax.net.ssl.SSLSocketFactory#getDefaultCipherSuites()
   */
  public String[] getDefaultCipherSuites()
  {
    return factory.getDefaultCipherSuites();
  }

  /**
   * @see javax.net.ssl.SSLSocketFactory#getSupportedCipherSuites()
   */
  public String[] getSupportedCipherSuites()
  {
    return factory.getDefaultCipherSuites();
  }

}
