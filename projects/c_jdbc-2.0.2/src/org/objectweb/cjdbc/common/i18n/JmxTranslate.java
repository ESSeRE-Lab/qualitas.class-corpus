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
 * Contributor(s):  Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.common.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class to translate the different messages of cjdbc.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */

public final class JmxTranslate extends I18N
{
  /**
   * JMX message description is stored in a different file
   */
  public static final String   MBEANS_LANGUAGE_FILE = "c-jdbc-mbeans-description";

  /**
   * Translation bundle for C-JDBC messages
   */
  public static ResourceBundle bundle;

  static
  {
    try
    {
      bundle = ResourceBundle.getBundle(MBEANS_LANGUAGE_FILE);
    }
    catch (MissingResourceException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * @param key the key to translate
   * @return translation
   * @see I18N#get(ResourceBundle, String)
   */
  public static String get(String key)
  {
    return get(bundle, key);
  }

  /**
   * @param key the key to translate
   * @param parameter boolean parameter
   * @return translation
   * @see I18N#get(ResourceBundle, String, boolean)
   */
  public static String get(String key, boolean parameter)
  {
    return get(bundle, key, parameter);
  }

  /**
   * @param key the key to translate
   * @param parameter int parameter
   * @return translation
   * @see I18N#get(ResourceBundle, String, int)
   */
  public static String get(String key, int parameter)
  {
    return get(bundle, key, parameter);
  }

  /**
   * @param key the key to translate
   * @param parameter long parameter
   * @return translation
   * @see I18N#get(ResourceBundle, String, long)
   */
  public static String get(String key, long parameter)
  {
    return get(bundle, key, parameter);
  }

  /**
   * @param key the key to translate
   * @param parameters Object array parameter
   * @return translation
   * @see I18N#get(ResourceBundle, String, Object[])
   */
  public static String get(String key, Object[] parameters)
  {
    return get(bundle, key, parameters);
  }

  /**
   * @param key the key to translate
   * @param parameter Object parameter
   * @return translation
   * @see I18N#get(ResourceBundle, String, Object)
   */
  public static String get(String key, Object parameter)
  {
    return get(bundle, key, parameter);
  }
}