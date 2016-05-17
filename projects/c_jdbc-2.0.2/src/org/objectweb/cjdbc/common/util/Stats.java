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
 * Contributor(s): Julie Marguerite, Sara Bouchenak.
 */

package org.objectweb.cjdbc.common.util;

/**
 * This class provides thread-safe statistics. Each statistic entry is composed
 * as follow:
 * <ul>
 * <li>count: statistic counter</li>
 * <li>error: statistic error counter</li>
 * <li>minTime: minimum time for this entry (automatically computed)</li>
 * <li>maxTime: maximum time for this entry (automatically computed)</li>
 * <li>totalTime: total time for this entry</li>
 * </ul>
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:julie.marguerite@inria.fr">Julie Marguerite </a>
 * @version 1.0
 */

public class Stats
{
  /** Statistic counter */
  private int    count;

  /** Statistic error counter */
  private int    error;

  /** Cache hits counter */
  private int    cacheHit;

  /** Minimum time for this entry (automatically computed) */
  private long   minTime;

  /** Maximum time for this entry (automatically computed) */
  private long   maxTime;

  /** Total time for this entry */
  private long   totalTime;

  /** Name of the stats. */
  private String name;

  /**
   * Creates a new <code>Stats</code> instance. The entries are reset to 0.
   * 
   * @param statName The stat name
   */
  public Stats(String statName)
  {
    name = statName;
    reset();
  }

  /**
   * Resets all entries to 0.
   */
  public synchronized void reset()
  {
    count = 0;
    error = 0;
    minTime = Long.MAX_VALUE;
    maxTime = Long.MIN_VALUE;
    totalTime = 0;
  }

  /**
   * Increments an entry count by one.
   */
  public synchronized void incrementCount()
  {
    count++;
  }

  /**
   * Increments an entry error by one.
   */
  public synchronized void incrementError()
  {
    error++;
  }

  /**
   * Increments an entry cache hit by one.
   */
  public synchronized void incrementCacheHit()
  {
    cacheHit++;
  }

  /**
   * Adds a new time sample for this entry. <code>time</code> is added to
   * total time and both minTime and maxTime are updated if needed.
   * 
   * @param time time to add to this entry
   */
  public synchronized void updateTime(long time)
  {
    if (time < 0)
    {
      System.err.println("Negative time received in Stats.updateTime(" + time
          + ")\n");
      return;
    }
    totalTime += time;
    if (time > maxTime)
      maxTime = time;
    if (time < minTime)
      minTime = time;
  }

  /**
   * Gets the name of the current stat.
   * 
   * @return stat name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Gets current count of an entry.
   * 
   * @return current entry count value
   */
  public synchronized int getCount()
  {
    return count;
  }

  /**
   * Gets current error count of an entry
   * 
   * @return current entry error value
   */
  public synchronized int getError()
  {
    return error;
  }

  /**
   * Gets current cache hit count of an entry
   * 
   * @return current entry cache hit value
   */
  public synchronized int getCacheHit()
  {
    return cacheHit;
  }

  /**
   * Gets the minimum time of an entry
   * 
   * @return entry minimum time
   */
  public synchronized long getMinTime()
  {
    return minTime;
  }

  /**
   * Gets the maximum time of an entry
   * 
   * @return entry maximum time
   */
  public synchronized long getMaxTime()
  {
    return maxTime;
  }

  /**
   * Gets the total time of an entry
   * 
   * @return entry total time
   */
  public synchronized long getTotalTime()
  {
    return totalTime;
  }

  /**
   * Adds the entries of another <code>Stats</code> object to this one.
   * 
   * @param anotherStat stat to merge with current stat
   * @throws Exception if you try to merge a stat with itself
   */
  public synchronized void merge(Stats anotherStat) throws Exception
  {
    if (this == anotherStat)
    {
      throw new Exception("You cannot merge a stat with itself");
    }

    count += anotherStat.getCount();
    error += anotherStat.getError();
    cacheHit += anotherStat.getCacheHit();
    if (minTime > anotherStat.getMinTime())
      minTime = anotherStat.getMinTime();
    if (maxTime < anotherStat.getMaxTime())
      maxTime = anotherStat.getMaxTime();
    totalTime += anotherStat.getTotalTime();
  }

  /**
   * Displays the statistics on the standard output.
   */
  public void displayOnStdout()
  {
    System.out.println(multipleLineDisplay());
  }

  /**
   * Displays the statistics information on multiple lines.
   * 
   * @return a <code>String</code> containing the Stat output
   */
  public String multipleLineDisplay()
  {
    String output = name + " statistics:\n" + "  Count: " + count + "\n"
        + "  Error: " + error + "\n";
    if (totalTime != 0)
    {
      output += "  Min time: " + minTime + " ms\n";
      output += "  Max time: " + maxTime + " ms\n";
    }
    else
    {
      output += "  Min time: 0 ms\n";
      output += "  Max time: 0 ms\n";
    }
    if (count == 0)
      output += "  Avg time: 0 ms\n";
    else
      output += "  Avg time: " + totalTime / count + " ms\n";
    output += "  Tot time: " + totalTime + " ms\n";

    double timeSec = totalTime / 1000;
    double timeMin = timeSec / 60, throup;
    throup = (timeMin != 0) ? (count / timeMin) : (count / timeSec);
    output += "  Throughput: " + throup
        + ((timeMin != 0) ? " requests/minute" : " requests/second");
    return output;
  }

  /**
   * Displays the statistics information on a single line in the format: name
   * count error cacheHit %hit minTime maxTime avgTime totalTime
   * 
   * @return a <code>String</code> containing the Stat output
   */
  public String singleLineDisplay()
  {
    String output = name + " " + count + " " + error + " " + cacheHit + " ";
    if (count == 0)
      output += "0 ";
    else
      output += ((double) cacheHit / (double) count * 100.0) + " ";
    if (totalTime != 0)
      output += minTime + " " + maxTime + " ";
    else
      output += " 0 0 ";
    if (count == 0)
      output += "0 ";
    else
      output += totalTime / count + " ";
    output += totalTime;
    double timeSec = totalTime / 1000;
    double timeMin = timeSec / 60, throup;
    throup = (timeMin != 0) ? (count / timeMin) : (count / timeSec);
    output += throup
        + ((timeMin != 0) ? " requests/minute" : " requests/second");
    return output;
  }

  /**
   * Get the stat information in the form of a String table. Format is: name
   * count error cacheHit %hit minTime maxTime avgTime totalTime
   * 
   * @return the String table corresponding to this stat
   */
  public String[] toStringTable()
  {
    String[] foo = {
        name,
        Integer.toString(count),
        Integer.toString(error),
        Integer.toString(cacheHit),
        (count == 0) ? "0" : Float.toString((float) cacheHit / (float) count
            * (float) 100.0), Long.toString(minTime), Long.toString(maxTime),
        (count == 0) ? "0" : Float.toString((float) totalTime / (float) count),
        Long.toString(totalTime)};
    return foo;
  }
}