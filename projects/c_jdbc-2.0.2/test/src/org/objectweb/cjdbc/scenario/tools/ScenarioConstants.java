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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.scenario.tools;

/**
 * Defines all the constants used in the scenari.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:mathieu.peltier@emicnetworks.com">Mathieu Peltier
 *         </a>
 * @version 1.0
 */
public interface ScenarioConstants
{
  /** Default controller hostname. */
  String DEFAULT_CONTROLLER_HOSTNAME       = "127.0.0.1";

  /** Default controller port. */
  String DEFAULT_CONTROLLER_PORT           = "25322";

  /** Default virtual database name to use. */
  String DEFAULT_VDB_NAME                  = "myDB";

  /** Default virtual database user name. */
  String DEFAULT_VDB_USER_NAME             = "user";

  /** Default virtual database user password. */
  String DEFAULT_VDB_USER_PASSWORD         = "";

  /** Default controller configuration directory. */
  String CONTROLLER_DIR                    = "/controller";

  /** Default virtual database configuration file directory. */
  String VIRTUALDATABASE_DIR               = "/virtualdatabase";

  /** Default controller configuration file. */
  String CONTROLLER_DEFAULT_FILE           = "/controller.xml";

  /** Default virtual database configuration file. */
  String DATABASE_CONFIG_FILE              = "/hsqdb-raidb1.xml";

  /** Process directory for databases files. */
  String PROCESS_DIRECTORY                 = "process";

  /** Time to wait between two horizontal test to let jgroups close itself. */
  long   WAIT_TIME_BETWEEN_HORIZONTAL_TEST = 3000;

  /** Time two refresh when shutting down jgroups. */
  int    WAIT_TIME_REFRESH_HORIZONTAL_TEST = 100;
}
