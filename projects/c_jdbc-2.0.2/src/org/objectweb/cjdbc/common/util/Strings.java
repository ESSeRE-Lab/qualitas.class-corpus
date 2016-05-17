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
 * Initial developer(s): Marc Wick.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.common.util;

/**
 * This class provides utilities for Strings manipulation.
 * 
 * @author <a href="mailto:mwick@dplanet.ch">Marc Wick</a>
 * @author <a href="mailto:mathieu.peltier@emicnetworks.com">Mathieu Peltier</a>
 * @version 1.0
 */
public class Strings
{

  /**
   * Replaces all occurrences of a String within another String.
   * 
   * @param sourceString source String
   * @param replace text pattern to replace
   * @param with replacement text
   * @return the text with any replacements processed, <code>null</code> if
   *         null String input
   */
  public static String replace(String sourceString, String replace, String with)
  {
    if (sourceString == null || replace == null || with == null
        || "".equals(replace))
    {
      return sourceString;
    }

    StringBuffer buf = new StringBuffer(sourceString.length());
    int start = 0, end = 0;
    while ((end = sourceString.indexOf(replace, start)) != -1)
    {
      buf.append(sourceString.substring(start, end)).append(with);
      start = end + replace.length();
    }
    buf.append(sourceString.substring(start));
    return buf.toString();
  }

  /**
   * Replaces all occurrences of a String within another String. The String to
   * be replaced will be replaced ignoring cases, all other cases are preserved
   * in the returned string
   * 
   * @param sourceString source String
   * @param replace text to replace, case insensitive
   * @param with replacement text
   * @return the text with any replacements processed, <code>null</code> if
   *         null String input
   */
  public static String replaceCasePreserving(String sourceString,
      String replace, String with)
  {
    if (sourceString == null || replace == null || with == null)
    {
      return sourceString;
    }
    String lower = sourceString.toLowerCase();
    int shift = 0;
    int idx = lower.indexOf(replace);
    int length = replace.length();
    StringBuffer resultString = new StringBuffer(sourceString);
    do
    {
      resultString = resultString.replace(idx + shift, idx + shift + length,
          with);
      shift += with.length() - length;
      idx = lower.indexOf(replace, idx + length);
    }
    while (idx > 0);

    return resultString.toString();
  }

}
