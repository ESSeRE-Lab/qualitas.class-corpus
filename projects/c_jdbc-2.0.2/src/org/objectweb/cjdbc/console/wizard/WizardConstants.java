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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.wizard;

import java.util.ArrayList;

import javax.swing.JComboBox;

/**
 * The constants used throughout the <code>XmlWizard</code>
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public final class WizardConstants
{
  /** Frame width */
  public static final int      FRAME_WIDTH                   = 750;
  /** Frame height */
  public static final int      FRAME_HEIGHT                  = 500;
  /** Validator height */
  public static final int      VALIDATOR_HEIGHT              = 300;
  /** Validator width */
  public static final int      VALIDATOR_WIDTH               = 400;
  /** Backend Frame width */
  public static final int      BACKEND_FRAME_WIDTH           = 300;
  /** Backend Frame height */
  public static final int      BACKEND_FRAME_HEIGHT          = 250;
  /** Connection Frame width */
  public static final int      CONNECTION_FRAME_WIDTH        = 400;
  /** Connection Frame height */
  public static final int      CONNECTION_FRAME_HEIGHT       = 200;

  /** Quit command */
  public static final String   COMMAND_QUIT                  = "command.quit";
  /** Add user command */
  public static final String   COMMAND_ADD_USER              = "command.add.user";
  /** Remove user command */
  public static final String   COMMAND_REMOVE_USER           = "command.remove.user";
  /** Add backend command */
  public static final String   COMMAND_ADD_BACKEND           = "command.add.backend";
  /** Remove backend command */
  public static final String   COMMAND_REMOVE_BACKEND        = "command.remove.backend";
  /** Export XML command */
  public static final String   COMMAND_EXPORT_XML            = "command.export.xml";
  /** Import XML command */
  public static final String   COMMAND_IMPORT_XML            = "command.import.xml";
  /** Edit connection parameteres command */
  public static final String   COMMAND_EDIT_CONNECTION_PARAM = "command.edit.connection.parameters";
  /** Check wizard command */
  public static final String   COMMAND_CHECK_WIZARD          = "command.check.wizard";
  /** Validate XML command */
  public static final String   COMMAND_VALIDATE_XML          = "command.validate.xml";

  /** Blob filters */
  public static final String[] BLOB                          = {"none", "hexa",
      "escaped"                                              };
  /** Macro clock option */
  public static final String[] MACRO_CLOCK                   = {"none", "local"};
  /** Schema gathering precision */
  public static final String[] DYNAMIC_PRECISION             = {"static",
      "table", "column", "procedures", "all"                 };
  /** Result cache granularity */
  public static final String[] RESULT_CACHE_GRANULARITY      = {"database",
      "table", "column", "columnUnique"                      };
  /** Connection managers */
  public static final String[] CONNECTION_MANAGERS           = {
      "SimpleConnectionManager", "FailFastPoolConnectionManager",
      "RandomWaitPoolConnectionManager", "VariablePoolConnectionManager"};
  /** Load balancer wait policies */
  public static final String[] WAIT_POLICIES                 = {"first",
      "majority", "all"                                      };
  /** Schedulers */
  public static final String[] SCHEDULERS_STANDARD           = {
      "SingleDBScheduler", "RAIDb-0Scheduler", "RAIDb-1Scheduler",
      "RAIDb-2Scheduler"                                     };
  /** Distributed Schedulers */
  public static final String[] SCHEDULERS_DISTRIBUTED        = {
      "RAIDb-1Scheduler", "RAIDb-2Scheduler"                 };
  /** Single DB schedulers */
  public static final String[] SCHEDULER_SINGLEDB_LEVELS     = {"query",
      "optimisticTransaction", "pessimisticTransaction"      };
  /** RAIDb-0 schedulers */
  public static final String[] SCHEDULER_RAIDB0_LEVELS       = {"query",
      "pessimisticTransaction"                               };
  /** RAIDb-1 schedulers */
  public static final String[] SCHEDULER_RAIDB1_LEVELS       = {"query",
      "optimisticQuery", "optimisticTransaction", "pessimisticTransaction"};
  /** RAIDb-2 schedulers */
  public static final String[] SCHEDULER_RAIDB2_LEVELS       = {"query",
      "pessimisticTransaction"                               };

  /** Single DB load balancers */
  public static final String[] LOAD_BALANCER_SINGLEDB        = new String[]{"SingleDB"};
  /** RAIDb-0 load balancers */
  public static final String[] LOAD_BALANCER_RAIDB0          = new String[]{"RAIDb-0"};
  /** RAIDb-1 load balancers */
  public static final String[] LOAD_BALANCER_RAIDB1          = new String[]{
      "RAIDb-1-RoundRobin", "RAIDb-1-LeastPendingRequestsFirst",
      "ParallelDB-RoundRobin", "ParallelDB-LeastPendingRequestsFirst"};
  /** RAIDb-2 load balancers */
  public static final String[] LOAD_BALANCER_RAIDB2          = new String[]{
      "RAIDb-2-RoundRobin", "RAIDb-2-LeastPendingRequestsFirst",
      "ParallelDB-RoundRobin", "ParallelDB-LeastPendingRequestsFirst"};

  /** Default schema gathering precision */
  public static final String   DEFAULT_DYNAMIC_PRECISION     = DYNAMIC_PRECISION[4];

  /** Virtual database tab */
  public static final String   TAB_VIRTUAL_DATABASE          = "tab.virtualdatabase";
  /** Distribution tab */
  public static final String   TAB_DISTRIBUTION              = "tab.distribution";

  /** Authentication tab */
  public static final String   TAB_AUTHENTICATION            = "tab.authentication";
  /** Backends tab */
  public static final String   TAB_BACKENDS                  = "tab.backends";
  /** Request manager tab */
  public static final String   TAB_REQUEST_MANAGER           = "tab.requestmanager";
  /** Caching tab */
  public static final String   TAB_CACHING                   = "tab.caching";
  /** Recovery tab */
  public static final String   TAB_RECOVERY                  = "tab.recovery";

  /**
   * Get items from combo box.
   * 
   * @param usersBox the combo box
   * @return the list of items
   */
  public static ArrayList getItemsFromCombo(JComboBox usersBox)
  {
    if (usersBox == null)
      return new ArrayList(0);
    int count = usersBox.getItemCount();
    ArrayList list = new ArrayList(count);
    for (int i = 0; i < count; i++)
      list.add(usersBox.getItemAt(i));
    return list;
  }

}