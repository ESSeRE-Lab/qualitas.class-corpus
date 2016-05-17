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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.common.shared;

/**
 * This describes the different backend states
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public final class BackendState
{
  /**
   * The backend can execute read requests, but not write requests
   */
  public static final int READ_ENABLED_WRITE_DISABLED = 0;

  /**
   * The backend can execute read and write requests
   */
  public static final int READ_ENABLED_WRITE_ENABLED  = 1;

  /**
   * The backend can execute write requests but not read requests
   */
  public static final int READ_DISABLED_WRITE_ENABLED = 2;

  /**
   * The backend cannot execute any requests
   */
  public static final int DISABLED                    = 3;

  /**
   * The backend is loading data from a dump file
   */
  public static final int RECOVERING                  = 4;

  /**
   * The backend is set for disabled. It is finishing to execute open
   * transactions
   */
  public static final int DISABLING                   = 5;

  /**
   * The backend is in a restore process. The content of a backup file is being
   * copied onto the backen
   */
  public static final int BACKUPING                   = 6;

  /**
   * Unknown backend state. This is used when the state of the backend cannot be
   * determined properly. This is the state the backend is set to after a
   * backup, restore or recovery failure.
   */
  public static final int UNKNOWN                     = -1;

  /**
   * Replaying request from the recovery log
   */
  public static final int REPLAYING                   = 7;
}