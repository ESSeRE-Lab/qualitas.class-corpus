/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 EmicNetworks.
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
 * Initial developer(s): Olivier Fambon.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.backup;

import java.io.Serializable;
import java.net.SocketAddress;

/**
 * DumpTransferInfo is used to store the necessary information for the client
 * backuper to fetch a dump from server backuper.
 * 
 * @author <a href="mailto:olivier.fambon@emicnetworks.com">Olivier Fambon</a>
 * @version 1.0
 */
public class DumpTransferInfo implements Serializable
{
  private static final long serialVersionUID = -7714674074423697782L;

  private long              sessionKey;
  private SocketAddress     backuperServerAddress;

  DumpTransferInfo(SocketAddress backuperServerAddress, long sessionKey)
  {
    this.sessionKey = sessionKey;
    this.backuperServerAddress = backuperServerAddress;
  }

  /**
   * Returns the Backuper server address that clients should use to fetch a
   * dump. The Backuper server at this address will ask for the session key.
   * 
   * @return the Backuper server address.
   */
  public SocketAddress getBackuperServerAddress()
  {
    return backuperServerAddress;
  }

  /**
   * Returns a SessionKey to be used as authentication token by the client when
   * fetching a dump.
   * 
   * @return a SessionKey to be used as authentication token.
   */
  public long getSessionKey()
  {
    return sessionKey;
  }
}
