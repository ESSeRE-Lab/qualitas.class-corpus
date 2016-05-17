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

package org.objectweb.cjdbc.console.gui.constants;

import org.objectweb.cjdbc.common.i18n.GuiTranslate;

/**
 * This class defines all the Gui action Commands
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public abstract class GuiCommands
{
  /** Command QUIT */
  public static final String COMMAND_QUIT                                       = GuiTranslate
                                                                                    .get("command.quit");
  /** Command ADD CONFIG FILE */
  public static final String COMMAND_ADD_CONFIG_FILE                            = GuiTranslate
                                                                                    .get("command.add.config.file");
  /** Command ADD CONTROLLER */
  public static final String COMMAND_ADD_CONTROLLER                             = GuiTranslate
                                                                                    .get("command.add.controller");
  /** Command SET CHECKPOINT */
  public static final String COMMAND_BACKEND_SET_CHECKPOINT                     = GuiTranslate
                                                                                    .get("command.set.checkpoint");                    ;
  /** Remove configuration file */
  public static final String COMMAND_REMOVE_CONFIGURATION_FILE                  = GuiTranslate
                                                                                    .get("command.remove.config.file");
  /** Monitor database command */
  public static final String COMMAND_MONITOR_DATABASE                           = GuiTranslate
                                                                                    .get("command.monitor.database");                  ;
  /** Remove backend */
  public static final String COMMAND_BACKEND_REMOVE                             = GuiTranslate
                                                                                    .get("command.remove.backend");
  /** View recovery log */
  public static final String COMMAND_VIEW_RECOVERY_LOG                          = GuiTranslate
                                                                                    .get("command.view.recoverylog.content");
  /** Command backend test connection */
  public static final String COMMAND_BACKEND_TEST_CONNECTION                    = GuiTranslate
                                                                                    .get("command.backend.test.connection");
  /** Command add server logging */
  public static final String COMMAND_CONTROLLER_LOG_CONFIGURATION_ADD_SERVER    = GuiTranslate
                                                                                    .get("command.set.logger.server.add");
  /** Command remove server logging */
  public static final String COMMAND_CONTROLLER_LOG_CONFIGURATION_REMOVE_SERVER = GuiTranslate
                                                                                    .get("command.set.logger.server.remove");
  /** Create new backend command */
  public static final String COMMAND_BACKEND_CREATE_NEW                         = GuiTranslate
                                                                                    .get("command.create.backend");
  /** Command GET CONTROLLER INFO */
  public static final String COMMAND_GET_CONTROLLER_INFO                        = GuiTranslate
                                                                                    .get("command.get.info");
  /** Confirm action Command */
  public static final String COMMAND_CONFIRM_ACTION                             = GuiTranslate
                                                                                    .get("command.confirm.action");
  /** Command SAVE CONFIGURATION FILE */
  public static final String COMMAND_SAVE_CONFIGURATION_FILE                    = GuiTranslate
                                                                                    .get("command.save.xml.buffer");
  /** Confirm action Command */
  public static final String COMMAND_CANCEL_ACTION                              = GuiTranslate
                                                                                    .get("command.cancel.action");
  /** Command Load Xml Configuration */
  public static final String COMMAND_DISPLAY_XML_DATABASE                       = GuiTranslate
                                                                                    .get("command.display.database.xml");
  /** Command Load Xml Configuration */
  public static final String COMMAND_DISPLAY_XML_CONTROLLER                     = GuiTranslate
                                                                                    .get("command.display.controller.xml");
  /** Command display sql stats */
  public static final String COMMAND_VIEW_SQL_STATS                             = GuiTranslate
                                                                                    .get("command.view.sql.stats");
  /** Command display cache stats */
  public static final String COMMAND_VIEW_CACHE_STATS                           = GuiTranslate
                                                                                    .get("command.view.cache.stats");
  /** Display cache content */
  public static final String COMMAND_VIEW_CACHE_CONTENT                         = GuiTranslate
                                                                                    .get("command.view.cache.content");
  /** Command hide confirm frame */
  public static final String COMMAND_HIDE_CONFIRM_FRAME                         = GuiTranslate
                                                                                    .get("command.hide.confirm.frame");                ;
  /** Command Controller Log Configuration Info Mode */
  public static final String COMMAND_CONTROLLER_LOG_CONFIGURATION_INFO          = GuiTranslate
                                                                                    .get("command.set.logger.info");
  /** Command Controller Log Configuration Debug Mode */
  public static final String COMMAND_CONTROLLER_LOG_CONFIGURATION_DEBUG         = GuiTranslate
                                                                                    .get("command.set.logger.debug");
  /** Command CLEAN DEBUG BUFFER */
  public static final String COMMAND_CLEAN_DEBUG_BUFFER                         = GuiTranslate
                                                                                    .get("command.clean.debug.buffer");
  /** Monitor Controller */
  public static final String COMMAND_CONTROLLER_MONITOR                         = GuiTranslate
                                                                                    .get("command.monitor.controller");
  /** Remove controller */
  public static final String COMMAND_CONTROLLER_REMOVE                          = GuiTranslate
                                                                                    .get("command.remove.controller");
  /** Hude backup frame */
  public static final String COMMAND_HIDE_BACKUP_FRAME                          = GuiTranslate
                                                                                    .get("command.hide.backup.frame");
  /**
   * Command display monitor window
   */
  public static final String COMMAND_MONITOR_CURRENT_CONTROLLER                 = GuiTranslate
                                                                                    .get("command.monitor.window");                    ;
  /** Command Refresh Controller Logs */
  public static final String COMMAND_REFRESH_LOGS                               = GuiTranslate
                                                                                    .get("command.refresh.logs");
  /** Shutdown controller Command */
  public static final String COMMAND_SHUTDOWN_CONTROLLER                        = GuiTranslate
                                                                                    .get("command.shutdown.controller");
  /** Command get controller report */
  public static final String COMMAND_CONTROLLER_REPORT                          = GuiTranslate
                                                                                    .get("command.controller.report");
  /** Command view log configuration */
  public static final String COMMAND_CONTROLLER_LOG_CONFIGURATION               = GuiTranslate
                                                                                    .get("command.view.controller.log.configuration");
  /** Hide shutdown frame */
  public static final String COMMAND_HIDE_SHUTDOWN_FRAME                        = GuiTranslate
                                                                                    .get("command.hide.shutdown.frame");
  /** Command update log configuration */
  public static final String COMMAND_CONTROLLER_UPDATE_LOG_CONFIGURATION        = GuiTranslate
                                                                                    .get("command.update.log.configuration");
  /** Command Create new Backend approve */
  public static final String COMMAND_CREATE_BACKEND_APPROVE                     = GuiTranslate
                                                                                    .get("command.create.backend.approve");
  /** Hide checkpoint frame */
  public static final String COMMAND_HIDE_CHECKPOINT_FRAME                      = GuiTranslate
                                                                                    .get("command.hide.checkpoint.frame");
  /** Command Refresh Controller List */
  public static final String COMMAND_REFRESH_CONTROLLER_LIST                    = GuiTranslate
                                                                                    .get("command.refresh.controller.list");
  /** Command Create new Backend cancel */
  public static final String COMMAND_CREATE_BACKEND_CANCEL                      = GuiTranslate
                                                                                    .get("command.create.backend.cancel");
  /** Command show error trace */
  public static final String COMMAND_SHOW_ERROR_TRACE                           = GuiTranslate
                                                                                    .get("command.show.error.trace");
  /** Approve create backup command */
  public static final String COMMAND_CREATE_BACKUP_APPROVE                      = GuiTranslate
                                                                                    .get("command.create.backup.approve");
  /** Cancel create backup command */
  public static final String COMMAND_CREATE_BACKUP_CANCEL                       = GuiTranslate
                                                                                    .get("command.create.backup.cancel");
  /** Command Select Controller */
  public static final String COMMAND_SELECT_CONTROLLER                          = GuiTranslate
                                                                                    .get("command.select.controller");
  /** Command Shutdown database */
  public static final String COMMAND_SHUTDOWN_DATABASE                          = GuiTranslate
                                                                                    .get("command.shutdown.database");
  /** Command add driver */
  public static final String COMMAND_ADD_DRIVER                                 = GuiTranslate
                                                                                    .get("command.add.driver");
  /** Command database enable all */
  public static final String COMMAND_ENABLE_ALL                                 = GuiTranslate
                                                                                    .get("command.enable.all");
  /** Command database disable all */
  public static final String COMMAND_DISABLE_ALL                                = GuiTranslate
                                                                                    .get("command.disable.all");
  /** Command database disable all */
  public static final String COMMAND_DELETE_DUMP                                = GuiTranslate
                                                                                    .get("command.delete.dump");
  /** Command load online help */
  public static final String COMMAND_LOAD_ONLINE_HELP                           = GuiTranslate
                                                                                    .get("command.load.online.help");
  /** Command Select Database */
  public static final String COMMAND_SELECT_DATABASE                            = GuiTranslate
                                                                                    .get("command.select.database");
  /** Command Select xml file */
  public static final String COMMAND_SELECT_XML_FILE                            = GuiTranslate
                                                                                    .get("command.select.xml.file");
  /** Authentication command for database */
  public static final String COMMAND_DATABASE_AUTHENTICATE                      = GuiTranslate
                                                                                    .get("command.authenticate");
  /** Command LOAD CONTROLLER */
  public static final String COMMAND_ADD_CONTROLLER_APPROVE                     = GuiTranslate
                                                                                    .get("command.add.controller.approve");
  /** Command CANCEL CONTROLLER LOAD */
  public static final String COMMAND_ADD_CONTROLLER_CANCEL                      = GuiTranslate
                                                                                    .get("command.add.controller.cancel");
  /** Command hide error frame */
  public static final String COMMAND_HIDE_ERROR_FRAME                           = GuiTranslate
                                                                                    .get("command.hide.error.frame");
  /** Command clean logging content */
  public static final String COMMAND_CLEAN_LOGGING_PANEL                        = GuiTranslate
                                                                                    .get("command.clean.logging.pane");
  /** Command hide error trace */
  public static final String COMMAND_HIDE_ERROR_TRACE                           = GuiTranslate
                                                                                    .get("command.hide.error.trace");
  /** Shutdown level safe */
  public static final String COMMAND_SHUTDOWN_SAFE                              = GuiTranslate
                                                                                    .get("command.shutdown.safe");
  /** Shutdown level force */
  public static final String COMMAND_SHUTDOWN_FORCE                             = GuiTranslate
                                                                                    .get("command.shutdown.force");
  /** Shutdown level wait */
  public static final String COMMAND_SHUTDOWN_WAIT                              = GuiTranslate
                                                                                    .get("command.shutdown.wait");

  /** Backend unset checkpoint */
  public static final String COMMAND_BACKEND_UNSET_CHECKPOINT                   = GuiTranslate
                                                                                    .get("command.backend.checkpoint.unset");
}