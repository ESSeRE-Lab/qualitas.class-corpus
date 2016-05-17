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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.objectweb.cjdbc.common.i18n.GuiTranslate;

/**
 * This class defines all the GuiConstants
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public final class GuiConstants
{
  /** Backend state enabled */
  public static final String BACKEND_STATE_ENABLED        = GuiTranslate
                                                              .get("gui.backend.enabled");
  /** Backend state disabled */
  public static final String BACKEND_STATE_DISABLED       = GuiTranslate
                                                              .get("gui.backend.disabled");
  /** Backend state disabling */
  public static final String BACKEND_STATE_DISABLING      = GuiTranslate
                                                              .get("gui.backend.disabling");
  /** Backend state restore */
  public static final String BACKEND_STATE_RESTORE        = GuiTranslate
                                                              .get("gui.backend.restore");
  /** Backend state backup */
  public static final String BACKEND_STATE_BACKUP         = GuiTranslate
                                                              .get("gui.backend.backup");
  /** Backend state recovery */
  public static final String BACKEND_STATE_RECOVERY       = GuiTranslate
                                                              .get("gui.backend.recovery");
  /** Backend state new */
  public static final String BACKEND_STATE_NEW            = GuiTranslate
                                                              .get("gui.backend.new");

  /** Debug Level All */
  public static final int    DEBUG_ALL                    = 0;
  /** Debug level all but do not show exception window */
  public static final int    DEBUG_NO_EXCEPTION_WINDOW    = 1;
  /** Debug level info */
  public static final int    DEBUG_INFO                   = 2;
  /** No Debug */
  public static final int    DEBUG_NONE                   = 3;
  /** Level of output of the debug */
  public static final int    DEBUG_LEVEL                  = DEBUG_ALL;

  /** Main frame width */
  public static final int    MAIN_FRAME_WIDTH             = 1024;
  /** Main gui frame height */
  public static final int    MAIN_FRAME_HEIGHT            = 600;

  /** Component Name for the list of controllers */
  public static final String LIST_CONTROLLER              = "ListController";

  /** Component Name for the list of databases */
  public static final String LIST_DATABASE                = "ListDatabase";
  /** Component Name for the files list */
  public static final String LIST_FILES                   = "ListFiles";

  /** Controller state up */
  public static final String CONTROLLER_STATE_UP          = "Controller_UP";
  /** Controller state down */
  public static final String CONTROLLER_STATE_DOWN        = "Controller_DOWN";
  /** URL to the help of CJDBC */
  public static final String CJDBC_URL_DOC                = "http://c-jdbc.objectweb.org/current/doc/userGuide/html/userGuide.html";
  /** Default save session file */
  public static final String CJDBC_DEFAULT_SESSION_NAME   = "session";
  /** Default save session file */
  public static final String CJDBC_DEFAULT_SESSION_FILE   = CJDBC_DEFAULT_SESSION_NAME
                                                              + ".properties";

  /** Font used for the center panel */
  public static final Font   CENTER_PANE_FONT             = new Font("Verdana",
                                                              Font.PLAIN, 9);

  /**
   * Default Font
   */
  public static final Font   DEFAULT_FONT                 = new Font("Verdana",
                                                              Font.PLAIN, 9);

  /**
   * Final Color for backend state enabled
   */
  public static final Color  BACKEND_STATE_ENABLED_COLOR  = new Color(180, 238,
                                                              180);
  /**
   * Final Color for backend state recovery
   */
  public static final Color  BACKEND_STATE_RECOVERY_COLOR = new Color(255, 211,
                                                              155);

  /**
   * Final Color for backend state disabled
   */
  public static final Color  BACKEND_STATE_DISABLED_COLOR = new Color(238, 180,
                                                              180);

  /**
   * Nice color
   */
  public static final Color  NICE_COLOR                   = new Color(99, 184,
                                                              255);

  /**
   * Lowered border
   */
  public static final Border LOWERED_BORDER               = BorderFactory
                                                              .createLoweredBevelBorder();

  /**
   * Line border
   */
  public static final Border LINE_BORDER                  = BorderFactory
                                                              .createLineBorder(Color.BLACK);

  /**
   * Titled border
   */
  public static final Border TITLED_BORDER                = BorderFactory
                                                              .createTitledBorder(
                                                                  LOWERED_BORDER,
                                                                  GuiTranslate
                                                                      .get("gui.border.selected"));

  /**
   * Default Custom Cursor for drags
   */
  public static Cursor       customCursor;

  private static final Color BACKEND_STATE_NEW_COLOR      = new Color(185, 211,
                                                              238);
  private static final Color BACKEND_STATE_BACKUP_COLOR   = new Color(255, 193,
                                                              193);
  private static final Color BACKEND_STATE_RESTORE_COLOR  = new Color(255, 174,
                                                              185);
  /**
   * Do not use any checkpoint for enable or disable of a backend
   */
  public static final String BACKEND_NO_CHECKPOINT        = GuiTranslate
                                                              .get("gui.backend.no.checkpoint");

  /** JMX attributes table */
  public static final String TABLE_JMX_ATTRIBUTES         = GuiTranslate
                                                              .get("table.jmx.attributes");
  /** JMX operations table */
  public static final String TABLE_JMX_OPERATIONS         = GuiTranslate
                                                              .get("table.jmx.operations");

  static
  {
    Toolkit tk = Toolkit.getDefaultToolkit();
    Point p = new Point(0, 0);
    try
    {
      customCursor = tk.createCustomCursor(GuiIcons.CUSTOM_CURSOR_ICON
          .getImage(), p, "CustomCursor");
    }
    catch (Exception e)
    {
      // Ignore
    }
  }

  /**
   * Get colors for backend panels
   * 
   * @param paneName name of the panel
   * @return <code>Color</code> NEVER <code>null</code>
   */
  public static Color getBackendBgColor(String paneName)
  {
    if (paneName.equals(BACKEND_STATE_ENABLED))
      return BACKEND_STATE_ENABLED_COLOR;
    else if (paneName.equals(BACKEND_STATE_DISABLED))
      return BACKEND_STATE_DISABLED_COLOR;
    else if (paneName.equals(BACKEND_STATE_RECOVERY))
      return BACKEND_STATE_RECOVERY_COLOR;
    else if (paneName.equals(BACKEND_STATE_NEW))
      return BACKEND_STATE_NEW_COLOR;
    else if (paneName.equals(BACKEND_STATE_RESTORE))
      return BACKEND_STATE_RESTORE_COLOR;
    else if (paneName.equals(BACKEND_STATE_BACKUP))
      return BACKEND_STATE_BACKUP_COLOR;
    else
      return Color.white;
  }

  /**
   * Test whether the given backend state is a valid state Since all the backend
   * states are defined here, this test method should also be defined here.
   * 
   * @param state the test to validate
   * @return true if valid state, false otherwise.
   */
  public static boolean isValidBackendState(String state)
  {
    if (state.equals(BACKEND_STATE_ENABLED))
      return true;
    else if (state.equals(BACKEND_STATE_DISABLED))
      return true;
    else if (state.equals(BACKEND_STATE_RESTORE))
      return true;
    else if (state.equals(BACKEND_STATE_RECOVERY))
      return true;
    else if (state.equals(BACKEND_STATE_BACKUP))
      return true;
    else if (state.equals(BACKEND_STATE_NEW))
      return true;
    else
      return false;
  }

  /**
   * Centers a new component on the screen
   * 
   * @param comp the window to center
   * @param width the width of the window
   * @param height the height of the window
   */
  public static void centerComponent(Window comp, int width, int height)
  {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension dim = toolkit.getScreenSize();
    int screenHeight = dim.height;
    int screenWidth = dim.width;
    int frameWidth = width;
    int frameHeight = height;
    comp.setSize(width, height);
    comp.setBounds((screenWidth - frameWidth) / 2,
        (screenHeight - frameHeight) / 2, frameWidth, frameHeight);
    comp.validate();
  }

  /**
   * Convert a parameter value depending on its type. Supported types are int
   * and boolean.
   * 
   * @param value value to convert
   * @param type "int" or "boolean"
   * @return value converted to an Integer or a Boolean object, or value as is
   *         for any other type
   */
  public static Object convertType(String value, String type)
  {
    if (type.equals("int"))
      return new Integer(value);
    if (type.equals("boolean"))
      return new Boolean(value);
    else
      return value;
  }

  /**
   * Get the parameter type.
   * 
   * @param tmp paramter to extract type from
   * @return the parameter type
   */
  public static String getParameterType(String tmp)
  {
    int indexOf = tmp.indexOf(";");
    if (indexOf != -1)
      tmp = tmp.substring(0, indexOf);
    while (tmp.charAt(0) == '[')
      tmp = tmp.substring(1) + "[]";
    if (tmp.charAt(0) == 'L')
      tmp = tmp.substring(1);
    if (tmp.indexOf(".") != -1)
      tmp = tmp.substring(tmp.lastIndexOf(".") + 1);
    return tmp;
  }

}