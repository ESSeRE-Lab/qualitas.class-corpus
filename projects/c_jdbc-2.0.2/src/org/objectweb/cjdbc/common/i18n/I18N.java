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

package org.objectweb.cjdbc.common.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * This class defines a I18N
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class I18N
{
  /**
   * Returns associated sentence to that key
   * 
   * @param key the key to find in the translation file
   * @param bundle then translation bundle to use
   * @return the corresponding sentence of the key if not found
   */
  public static String get(ResourceBundle bundle, String key)
  {
    try
    {
      return bundle.getString(key);
    }
    catch (Exception e)
    {
      return key;
    }
  }

  /**
   * Returns translated key with instanciated parameters
   * 
   * @param bundle then translation bundle to use
   * @param key the key to find in translation file.
   * @param parameter the parameter value
   * @return the corresponding sentence with key and parameters
   */
  public static String get(ResourceBundle bundle, String key, boolean parameter)
  {
    return MessageFormat.format(get(bundle, key), new Object[]{String
        .valueOf(parameter)});
  }

  /**
   * Returns translated key with instanciated parameters
   * 
   * @param bundle then translation bundle to use
   * @param key the key to find in translation file.
   * @param parameter the parameter value
   * @return the corresponding sentence with key and parameters
   */
  public static String get(ResourceBundle bundle, String key, int parameter)
  {
    return MessageFormat.format(get(bundle, key), new Object[]{String
        .valueOf(parameter)});
  }

  /**
   * Returns translated key with instanciated parameters
   * 
   * @param bundle then translation bundle to use
   * @param key the key to find in translation file.
   * @param parameter the parameter value
   * @return the corresponding sentence with key and parameters
   */
  public static String get(ResourceBundle bundle, String key, long parameter)
  {
    return MessageFormat.format(get(bundle, key), new Object[]{String
        .valueOf(parameter)});
  }

  /**
   * Replace <code>REPLACE_CHAR</code> in the translated message with
   * parameters. If you have more parameters than charaters to replace,
   * remaining parameters are appended as a comma separated list at the end of
   * the message.
   * 
   * @param bundle then translation bundle to use
   * @param key the key to find in the translation file
   * @param parameters to put inside square braquets
   * @return the corresponding sentence of the key if not found
   */
  public static String get(ResourceBundle bundle, String key,
      Object[] parameters)
  {
    return MessageFormat.format(get(bundle, key), parameters);
  }

  /**
   * Same as above but implies creation of an array for the parameter
   * 
   * @param bundle then translation bundle to use
   * @param key to translate
   * @param parameter to put in translation
   * @return translated message
   */
  public static String get(ResourceBundle bundle, String key, Object parameter)
  {
    return MessageFormat.format(get(bundle, key), new Object[]{parameter});
  }

}