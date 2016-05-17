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

package org.objectweb.cjdbc.common.monitor;

import org.objectweb.cjdbc.common.i18n.Translate;

/**
 * Name convertions for data collection types.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 */
public final class DataCollectionNames
{
  /**
   * Convert the type reference of a collector into a <code>String</code>
   * 
   * @param dataType see DataCollection
   * @return a <code>String</code> that describes the collector
   */
  public static String get(int dataType)
  {
    switch (dataType)
    {
      /*
       * Controller Collectors
       */
      case DataCollection.CONTROLLER_TOTAL_MEMORY :
        return Translate.get("monitoring.controller.total.memory");
      case DataCollection.CONTROLLER_USED_MEMORY :
        return Translate.get("monitoring.controller.used.memory");
      case DataCollection.CONTROLLER_WORKER_PENDING_QUEUE :
        return Translate.get("monitoring.controller.pending.queue");
      case DataCollection.CONTROLLER_THREADS_NUMBER :
        return Translate.get("monitoring.controller.threads.number");
      case DataCollection.CONTROLLER_IDLE_WORKER_THREADS :
        return Translate.get("monitoring.controller.idle.worker.threads");
      /*
       * Backend collectors
       */
      case DataCollection.BACKEND_ACTIVE_TRANSACTION :
        return Translate.get("monitoring.backend.active.transactions");
      case DataCollection.BACKEND_PENDING_REQUESTS :
        return Translate.get("monitoring.backend.pending.requests");
      case DataCollection.BACKEND_TOTAL_ACTIVE_CONNECTIONS :
        return Translate.get("monitoring.backend.active.connections");
      case DataCollection.BACKEND_TOTAL_REQUEST :
        return Translate.get("monitoring.backend.total.requests");
      case DataCollection.BACKEND_TOTAL_READ_REQUEST :
        return Translate.get("monitoring.backend.total.read.requests");
      case DataCollection.BACKEND_TOTAL_WRITE_REQUEST :
        return Translate.get("monitoring.backend.total.write.requests");
      case DataCollection.BACKEND_TOTAL_TRANSACTIONS :
        return Translate.get("monitoring.backend.total.transactions");
      /*
       * VirtualDatabase collectors
       */
      case DataCollection.DATABASES_ACTIVE_THREADS :
        return Translate.get("monitoring.virtualdatabase.active.threads");
      case DataCollection.DATABASES_PENDING_CONNECTIONS :
        return Translate.get("monitoring.virtualdatabase.pending.connections");
      case DataCollection.DATABASES_NUMBER_OF_THREADS :
        return Translate.get("monitoring.virtualdatabase.threads.count");
      /*
       * Cache stats collectors
       */
      case DataCollection.CACHE_STATS_COUNT_HITS :
        return Translate.get("monitoring.cache.count.hits");
      case DataCollection.CACHE_STATS_COUNT_INSERT :
        return Translate.get("monitoring.cache.count.insert");
      case DataCollection.CACHE_STATS_COUNT_SELECT :
        return Translate.get("monitoring.cache.count.select");
      case DataCollection.CACHE_STATS_HITS_PERCENTAGE :
        return Translate.get("monitoring.cache.hits.ratio");
      case DataCollection.CACHE_STATS_NUMBER_ENTRIES :
        return Translate.get("monitoring.cache.number.entries");
      /*
       * Scheduler collectors
       */
      case DataCollection.SCHEDULER_NUMBER_READ :
        return Translate.get("monitoring.scheduler.number.read");
      case DataCollection.SCHEDULER_NUMBER_REQUESTS :
        return Translate.get("monitoring.scheduler.number.requests");
      case DataCollection.SCHEDULER_NUMBER_WRITES :
        return Translate.get("monitoring.scheduler.number.writes");
      case DataCollection.SCHEDULER_PENDING_TRANSACTIONS :
        return Translate.get("monitoring.scheduler.pending.transactions");
      case DataCollection.SCHEDULER_PENDING_WRITES :
        return Translate.get("monitoring.scheduler.pending.writes");
      /*
       * Client collectors
       */
      case DataCollection.CLIENT_TIME_ACTIVE :
        return Translate.get("monitoring.client.time.active");

      /*
       * Unknown collector
       */
      default :
        return "";
    }
  }

  /**
   * Return the type of the collector corresponding to the command
   * 
   * @param command to get type form
   * @return an <code>int</code>
   */
  public static int getTypeFromCommand(String command)
  {
    command = command.replace('_', ' ');
    /*
     * Controller Collectors
     */
    if (command.equalsIgnoreCase(Translate
        .get("monitoring.controller.total.memory")))
      return DataCollection.CONTROLLER_TOTAL_MEMORY;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.controller.used.memory")))
      return DataCollection.CONTROLLER_USED_MEMORY;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.controller.pending.queue")))
      return DataCollection.CONTROLLER_WORKER_PENDING_QUEUE;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.controller.threads.number")))
      return DataCollection.CONTROLLER_THREADS_NUMBER;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.controller.idle.worker.threads")))
      return DataCollection.CONTROLLER_IDLE_WORKER_THREADS;

    /*
     * Backend collectors
     */
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.backend.active.transactions")))
      return DataCollection.BACKEND_ACTIVE_TRANSACTION;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.backend.pending.requests")))
      return DataCollection.BACKEND_PENDING_REQUESTS;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.backend.active.connections")))
      return DataCollection.BACKEND_TOTAL_ACTIVE_CONNECTIONS;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.backend.total.read.requests")))
      return DataCollection.BACKEND_TOTAL_READ_REQUEST;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.backend.total.write.requests")))
      return DataCollection.BACKEND_TOTAL_WRITE_REQUEST;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.backend.total.requests")))
      return DataCollection.BACKEND_TOTAL_REQUEST;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.backend.total.transactions")))
      return DataCollection.BACKEND_TOTAL_TRANSACTIONS;

    /*
     * VirtualDatabase collectors
     */
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.virtualdatabase.active.threads")))
      return DataCollection.DATABASES_ACTIVE_THREADS;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.virtualdatabase.pending.connections")))
      return DataCollection.DATABASES_PENDING_CONNECTIONS;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.virtualdatabase.threads.count")))
      return DataCollection.DATABASES_NUMBER_OF_THREADS;

    /*
     * Cache stats collectors
     */
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.cache.count.hits")))
      return DataCollection.CACHE_STATS_COUNT_HITS;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.cache.count.insert")))
      return DataCollection.CACHE_STATS_COUNT_INSERT;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.cache.count.select")))
      return DataCollection.CACHE_STATS_COUNT_SELECT;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.cache.hits.ratio")))
      return DataCollection.CACHE_STATS_HITS_PERCENTAGE;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.cache.number.entries")))
      return DataCollection.CACHE_STATS_NUMBER_ENTRIES;

    /*
     * Scheduler collectors
     */
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.scheduler.number.read")))
      return DataCollection.SCHEDULER_NUMBER_READ;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.scheduler.number.requests")))
      return DataCollection.SCHEDULER_NUMBER_REQUESTS;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.scheduler.number.writes")))
      return DataCollection.SCHEDULER_NUMBER_WRITES;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.scheduler.pending.transactions")))
      return DataCollection.SCHEDULER_PENDING_TRANSACTIONS;
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.scheduler.pending.writes")))
      return DataCollection.SCHEDULER_PENDING_WRITES;

    /*
     * Client collectors
     */
    else if (command.equalsIgnoreCase(Translate
        .get("monitoring.client.time.active")))
      return DataCollection.CLIENT_TIME_ACTIVE;

    else
      return 0;
  }
}
