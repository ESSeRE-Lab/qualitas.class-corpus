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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.requestplayer;

import org.objectweb.cjdbc.common.util.Stats;

/**
 * Displays the number of requests processed at a given time period.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class MonitoringThread extends Thread
{
  /** Number of read requests. */
  private Stats   selectStats  = null;

  /** Number of unknown requests. */
  private Stats   unknownStats = null;

  /** Number of update requests. */
  private Stats   updateStats  = null;

  /** Number of insert requests. */
  private Stats   insertStats  = null;

  /** Number of delete requests. */
  private Stats   deleteStats  = null;

  /** Time to wait between 2 outputs in milleseconds. */
  private long    timeInMs;            // 

  /** <code>true</code> if this thread has been killed. */
  private boolean killed       = false;

  /**
   * Creates a new <code>MonitoringThread</code>.
   * 
   * @param father the client emulator.
   * @param timeInMs the time to wait between 2 outputs in milleseconds.
   */
  public MonitoringThread(ClientEmulator father, long timeInMs)
  {
    super("MonitoringThread");

    // Init the pointers to the stats
    selectStats = father.getSelectStats();
    unknownStats = father.getUnknownStats();
    updateStats = father.getUpdateStats();
    insertStats = father.getInsertStats();
    deleteStats = father.getDeleteStats();

    this.timeInMs = timeInMs;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    int oldStats = 0;
    int currentStats;
    for (int i = 0; !killed; i++)
    {
      try
      {
        Thread.sleep(timeInMs);
      }
      catch (InterruptedException e)
      {
        System.out.println("Monitoring thread interrupted");
        break;
      }
      currentStats = selectStats.getCount() + updateStats.getCount()
          + insertStats.getCount() + deleteStats.getCount()
          + unknownStats.getCount();
      System.out.println("Monitor:" + i + ": " + (currentStats - oldStats));
      oldStats = currentStats;
    }
  }

  /**
   * Returns <code>true</code> if this thread has been killed.
   * 
   * @return a <code>boolean</code>
   */
  public boolean isKilled()
  {
    return killed;
  }

  /**
   * Used to stop the execution of this thread.
   * 
   * @param killed <code>boolean</code>.
   */
  public void setKilled(boolean killed)
  {
    this.killed = killed;
  }
}