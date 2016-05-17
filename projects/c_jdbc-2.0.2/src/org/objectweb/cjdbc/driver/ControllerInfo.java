/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
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

package org.objectweb.cjdbc.driver;

/**
 * Controller related information, namely the host name and the port on which
 * the controller is running.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class ControllerInfo
{
  private String hostname;
  private int    port;

  /**
   * Creates a ControllerInfo object
   */
  public ControllerInfo()
  {
  }

  /**
   * Creates a new <code>ControllerInfo</code> object
   * 
   * @param hostname the controller host name
   * @param port the controller port
   */
  public ControllerInfo(String hostname, int port)
  {
    this.hostname = hostname;
    this.port = port;
  }

  /**
   * Get the hostname where the controller is running
   * 
   * @return controller hostname
   */
  public String getHostname()
  {
    return hostname;
  }

  /**
   * Get the port number on which the controller is listening.
   * 
   * @return port number.
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Set the controller hostname.
   * 
   * @param string hostname to set
   */
  public void setHostname(String string)
  {
    hostname = string;
  }

  /**
   * Set the port number.
   * 
   * @param port port number
   */
  public void setPort(int port)
  {
    this.port = port;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj instanceof ControllerInfo)
    {
      ControllerInfo other = (ControllerInfo) obj;
      return hostname.equals(other.getHostname()) && (port == other.port);
    }
    return false;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return hostname + ":" + port;
  }
}