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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.gui.constants;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * This class defines a GuiIcons
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class GuiIcons
{
  /** Backend Panel Icon */
  public static final Icon      BACKEND_PANEL_ICON             = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Panel_Backend.gif"));

  /** Xml Panel Icon */
  public static final Icon      XML_PANEL_ICON                 = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Panel_Xml.gif"));

  /** Debug panel Icon */
  public static final Icon      DEBUG_PANEL_ICON               = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Panel_Debug.gif"));

  /** Backend Enabled Icon */
  public static final Icon      BACKEND_ENABLED_ICON           = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Backend_Enabled.gif"));

  /** Backend Disabled Icon */
  public static final Icon      BACKEND_DISABLED_ICON          = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Backend_Disabled.gif"));

  /** Backend BACKUP Icon */
  public static final Icon      BACKEND_BACKUP_ICON            = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Backend_Backup.gif"));

  /** Backend RESTORE Icon */
  public static final Icon      BACKEND_RESTORE_ICON           = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Backend_Restore.gif"));

  /** Backend RECOVERY Icon */
  public static final Icon      BACKEND_STATE_RECOVERY         = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Backend_Recovery.gif"));

  /** Backend disabling icon */
  public static final Icon      BACKEND_DISABLING_ICON         = BACKEND_STATE_RECOVERY;

  /** Log panel Icon */
  public static final Icon      INFO_PANEL_ICON                = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Panel_Info.gif"));

  /** Configuration file Icon */
  public static final Icon      CONFIGURATION_FILE_OBJECT_ICON = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Object_Configuration_File.gif"));

  /** Controller Ready Icon */
  public static final Icon      CONTROLLER_READY               = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Object_Controller_Ready.gif"));

  /** Controller down Icon */
  public static final Icon      CONTROLLER_DOWN                = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Object_Controller_Down.gif"));

  /** Database Icon */
  public static final Icon      DATABASE_ICON                  = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Object_Database.gif"));

  /** Help Panel Icon */
  public static final Icon      HELP_PANEL_ICON                = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Panel_Help.gif"));

  /** Logging Panel Icon */
  public static final Icon      LOGGING_PANEL_ICON             = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Panel_Logging.gif"));

  /** Dump File Icon */
  public static final Icon      DUMP_FILE_ICON                 = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Object_Dump_File.gif"));

  /** Log Config Panel Icon */
  public static final Icon      LOG_CONFIG_PANEL_ICON          = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Panel_Log_Configuration.gif"));

  /** Frame error Icon */
  public static final Icon      FRAME_ERROR_ICON               = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Frame_Error.gif"));

  /** Database Single Icon */
  public static final Icon      DATABASE_SINGLE_ICON           = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Database_Single.gif"));

  /** Database Distributed Icon */
  public static final Icon      DATABASE_DISTRIBUTED_ICON      = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Database_Distributed.gif"));

  /** Custom Cursor Icon */
  public static final ImageIcon CUSTOM_CURSOR_ICON             = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Cursor_Custom.gif"));

  /** Logo */
  public static final ImageIcon CJDBC_LOGO                     = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/c-jdbc.png"));

  /** Jmx * */
  public static final ImageIcon JMX_PANEL_ICON                 = new ImageIcon(
                                                                   GuiIcons.class
                                                                       .getResource("/Panel_JMX.gif"));

}