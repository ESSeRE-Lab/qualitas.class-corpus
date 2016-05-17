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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.common.jmx.notifications;

/**
 * This is the list of the C-JDBC notification that can be sent from the mbean
 * server on the controller.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public abstract class CjdbcNotificationList
{

  /**
   * Notification level, notification is not an error
   */
  public static final String NOTIFICATION_LEVEL_INFO                  = "info";

  /**
   * Notification level, notification is an error
   */
  public static final String NOTIFICATION_LEVEL_ERROR                 = "error";

  /**
   * Virtual Database Removed
   */
  public static final String CONTROLLER_VIRTUALDATABASE_REMOVED       = "cjdbc.controller.virtualdatabases.removed";

  /**
   * VirtualDatabase added
   */
  public static final String CONTROLLER_VIRTUALDATABASE_ADDED         = "cjdbc.controller.virtualdatabase.added";

  /**
   * New Dump List for virtual database
   */
  public static final String VIRTUALDATABASE_NEW_DUMP_LIST            = "cjdbc.virtualdatabase.dump.list";

  /**
   * Backend added
   */
  public static final String VIRTUALDATABASE_BACKEND_ADDED            = "cjdbc.virtualdatabase.backend.added";

  /**
   * Controller added for Distributed VirtualDatabase
   */
  public static final String DISTRIBUTED_CONTROLLER_ADDED             = "cjdbc.distributed.controller.added";

  /**
   * Controller removed from Distributed VirtualDatabase
   */
  public static final String DISTRIBUTED_CONTROLLER_REMOVED           = "cjdbc.distributed.controller.removed";

  /**
   * Backend has been disabled
   */
  public static final String VIRTUALDATABASE_BACKEND_DISABLED         = "cjdbc.virtualdatabase.backend.disabled";

  /**
   * Backend has been enabled
   */
  public static final String VIRTUALDATABASE_BACKEND_ENABLED          = "cjdbc.virtualdatabase.backend.enabled";

  /**
   * Backend is recovering
   */
  public static final String VIRTUALDATABASE_BACKEND_RECOVERING       = "cjdbc.virtualdatabase.backend.recovering";

  /**
   * Backend recovery has failed
   */
  public static final String VIRTUALDATABASE_BACKEND_RECOVERY_FAILED  = "cjdbc.virtualdatabase.backend.recovery.failed";

  /**
   * Backend replaying has failed
   */
  public static final String VIRTUALDATABASE_BACKEND_REPLAYING_FAILED = "cjdbc.virtualdatabase.backend.replaying.failed";

  /**
   * Backend is backing up
   */
  public static final String VIRTUALDATABASE_BACKEND_BACKINGUP        = "cjdbc.virtualdatabase.backend.backingup";

  /**
   * Backend is write enabled
   */
  public static final String VIRTUALDATABASE_BACKEND_ENABLED_WRITE    = "cjdbc.virtualdatabase.backend.enable.write";

  /**
   * Backend has been removed
   */
  public static final String VIRTUALDATABASE_BACKEND_REMOVED          = "cjdbc.virtualdatabase.backend.removed";

  /**
   * Backend is being disabled
   */
  public static final String VIRTUALDATABASE_BACKEND_DISABLING        = "cjdbc.virtualdatabase.backend.disabling";

  /**
   * Backend has been killed by the load balancer. We don't know the state of
   * the backend so we have to restore it
   */
  public static final String VIRTUALDATABASE_BACKEND_UNKNOWN          = "cjdbc.virtualdatabase.backend.unknown";

  /**
   * Replaying content of recovery log
   */
  public static final String VIRTUALDATABASE_BACKEND_REPLAYING        = "cjdbc.virtualdatabase.backend.replaying";

  /** element that can be found in the data object */
  public static final String DATA_DATABASE                            = "database";
  /** element that can be found in the data object */
  public static final String DATA_CHECKPOINT                          = "checkpoint";
  /** element that can be found in the data object */
  public static final String DATA_URL                                 = "url";
  /** element that can be found in the data object */
  public static final String DATA_NAME                                = "name";
  /** element that can be found in the data object */
  public static final String DATA_DRIVER                              = "driver";

}