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

package org.objectweb.cjdbc.controller.recoverylog;

/**
 * A instance of this class gives information on a specific backend state from
 * the recovery log. For a backend, we have its name, the virtual database that
 * owns it, the lastKnownCheckpoint,and the state of the backend ().
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public final class BackendRecoveryInfo
{
  private String backendName;
  private String checkpoint;
  /** State is defined in <code>BackendState</code> */
  private int    backendState;
  private String virtualDatabase;

  /**
   * Creates a new <code>BackendRecoveryInfo</code> object
   * 
   * @param backendName backend name
   * @param lastCheckpoint last known checkpoint name
   * @param backendState backend state as defined in <code>BackendState</code>
   * @param virtualDatabase virtual database name
   */
  public BackendRecoveryInfo(String backendName, String lastCheckpoint,
      int backendState, String virtualDatabase)
  {
    this.backendName = backendName;
    this.checkpoint = lastCheckpoint;
    this.backendState = backendState;
    this.virtualDatabase = virtualDatabase;
  }

  /**
   * Returns the backendName value.
   * 
   * @return Returns the backendName.
   */
  public String getBackendName()
  {
    return backendName;
  }

  /**
   * Sets the backendName value.
   * 
   * @param backendName The backendName to set.
   */
  public void setBackendName(String backendName)
  {
    this.backendName = backendName;
  }

  /**
   * Returns the backend state as defined in <code>BackendState</code>.
   * 
   * @return Returns the backend state.
   */
  public int getBackendState()
  {
    return backendState;
  }

  /**
   * Sets the backend state value. The value must be defined in
   * <code>BackendState</code>
   * 
   * @param backendState The backend state to set.
   */
  public void setBackendState(int backendState)
  {
    this.backendState = backendState;
  }

  /**
   * Returns the lastCheckpoint value.
   * 
   * @return Returns the lastCheckpoint.
   */
  public String getCheckpoint()
  {
    return checkpoint;
  }

  /**
   * Sets the lastCheckpoint value.
   * 
   * @param lastCheckpoint The lastCheckpoint to set.
   */
  public void setCheckpoint(String lastCheckpoint)
  {
    this.checkpoint = lastCheckpoint;
  }

  /**
   * Returns the virtualDatabase value.
   * 
   * @return Returns the virtualDatabase.
   */
  public String getVirtualDatabase()
  {
    return virtualDatabase;
  }

  /**
   * Sets the virtualDatabase value.
   * 
   * @param virtualDatabase The virtualDatabase to set.
   */
  public void setVirtualDatabase(String virtualDatabase)
  {
    this.virtualDatabase = virtualDatabase;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "Backend:" + this.backendName + ",VirtualDatabase:"
        + this.virtualDatabase + ",State:" + this.backendState + ",Checkpoint:"
        + this.checkpoint;
  }
}