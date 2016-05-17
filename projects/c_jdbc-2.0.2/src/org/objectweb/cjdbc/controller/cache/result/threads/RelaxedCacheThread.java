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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.cache.result.threads;

import java.util.ArrayList;
import java.util.Iterator;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.cache.result.ResultCache;
import org.objectweb.cjdbc.controller.cache.result.entries.ResultCacheEntryRelaxed;

/**
 * This thread manages relaxed cache entries and remove them from the cache if
 * their deadline has expired or they are dirty.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public final class RelaxedCacheThread extends Thread
{
  private long              threadWakeUpTime = 0;
  private final ResultCache cache;
  int                       refreshCacheRate = 60;
  int                       refreshCacheTime = 60 / refreshCacheRate;
  private Trace             logger           = Trace
                                                 .getLogger(RelaxedCacheThread.class
                                                     .getName());

  private boolean           isKilled         = false;

  /**
   * Creates a new <code>RelaxedCacheThread</code> object
   * 
   * @param cache ResultCache creating this thread
   */
  public RelaxedCacheThread(ResultCache cache)
  {
    super("RelaxedCacheThread");
    this.cache = cache;
  }

  /**
   * Creates a new <code>RelaxedCacheThread</code> object
   * 
   * @param cache ResultCache creating this thread
   * @param refreshCacheRate cache refresh rate in seconds
   */
  public RelaxedCacheThread(ResultCache cache, int refreshCacheRate)
  {
    this(cache);
    this.refreshCacheRate = refreshCacheRate;
  }

  /**
   * Returns the threadWakeUpTime value.
   * 
   * @return Returns the threadWakeUpTime.
   */
  public long getThreadWakeUpTime()
  {
    return threadWakeUpTime;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    ResultCacheEntryRelaxed entry;
    long now;
    long sleep;
    // Keep trace of relaxed cache entries to delete
    ArrayList toRemoveFromRelaxedCache = new ArrayList();
    while (!isKilled)
    {
      synchronized (this)
      {
        try
        {
          threadWakeUpTime = 0;
          if (cache.getRelaxedCache().isEmpty())
          { // Nothing in the cache, just sleep!
            if (logger.isDebugEnabled())
              logger.debug(Translate.get("cachethread.cache.empty.sleeping"));
            wait();
          }
          else
          { // Look for first deadline
            now = System.currentTimeMillis();
            for (Iterator iter = cache.getRelaxedCache().iterator(); iter
                .hasNext();)
            {
              entry = (ResultCacheEntryRelaxed) iter.next();
              if (entry.getDeadline() < now)
              { // Deadline has expired
                if (entry.isDirty() || !entry.getKeepIfNotDirty())
                { // Remove this entry
                  toRemoveFromRelaxedCache.add(entry);
                  continue;
                }
                else
                  // Entry is still valid, reset deadline
                  entry.setDeadline(now + entry.getTimeout());
              }

              // Recompute next wakeup time if needed
              if (threadWakeUpTime == 0
                  || (entry.getDeadline() < threadWakeUpTime))
                threadWakeUpTime = entry.getDeadline();
            }

            // Clean up all dirty entries from the relaxed cache
            int size = toRemoveFromRelaxedCache.size();
            for (int i = 0; i < size; i++)
            {
              entry = (ResultCacheEntryRelaxed) toRemoveFromRelaxedCache.get(i);
              if (logger.isDebugEnabled())
                logger.debug(Translate.get(
                    "cachethread.remove.entry.from.cache", entry.getRequest()
                        .getSQL()));
              this.cache.removeFromCache(entry.getRequest());
              cache.getRelaxedCache().remove(entry);
            }
            toRemoveFromRelaxedCache.clear();
            if (threadWakeUpTime == 0)
            { // All entries were dirty and not kept in the cache, therefore
              // there is no next deadline. (and no cache entry to wait for)
              continue;
            }
            else
            { // Sleep until the next deadline
              sleep = (threadWakeUpTime - now) / 1000 + refreshCacheTime;
              if (logger.isDebugEnabled())
              {
                logger.debug(Translate.get("cachethread.sleeping", sleep));
              }
              sleep = (sleep) * 1000;
              wait(sleep);
            }
          }
        }
        catch (Exception e)
        {
          logger.warn(e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Shutdown the current thread.
   */
  public synchronized void shutdown()
  {
    isKilled = true;
    notify();
  }

}