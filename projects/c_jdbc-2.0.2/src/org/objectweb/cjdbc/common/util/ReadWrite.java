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

package org.objectweb.cjdbc.common.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class defines reading and writing convenient methods
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ReadWrite
{

  /**
   * Write the content of the <code>Hashtable</code> in a readable format
   * 
   * @param table the hashtable to get keys and values from
   * @param prefix prefix some values with tabs
   * @return <code>String</code> conversion for the table content
   */
  public static String write(Hashtable table, boolean prefix)
  {
    if (table == null)
      return "";
    StringBuffer buffer = new StringBuffer();
    Enumeration e = table.keys();
    Object o;
    while (e.hasMoreElements())
    {
      o = e.nextElement();
      if (o.toString().indexOf(".path") != -1)
      {
        // This is a class path, make it look nice
        buffer.append(o + " = " + System.getProperty("line.separator"));
        String substring = (String) table.get(o);
        int index;
        while (true)
        {
          index = substring.indexOf(':');
          if (index == -1)
            break;
          if (prefix)
            buffer.append("\t\t");
          buffer.append(substring.substring(0, index)
              + System.getProperty("line.separator"));
          substring = substring.substring(index + 1);
        }
        if (prefix)
          buffer.append("\t\t");
        buffer.append(substring + System.getProperty("line.separator"));
      }
      else
      {
        buffer.append(o + " = " + table.get(o)
            + System.getProperty("line.separator"));
      }
    }
    return buffer.toString();
  }

  /**
   * Write the content of the <code>ArrayList<code> in a readable format
   * 
   * @param list the list to get the values from
   * @param listName give the prefix names for values
   * @param writeCountKey should we write the count keys
   * @return <code>String</code> conversion for the list content
   */
  public static String write(ArrayList list, String listName,
      boolean writeCountKey)
  {
    if (list == null)
      return "";
    StringBuffer buffer = new StringBuffer();
    int size = list.size();
    if (writeCountKey)
      buffer.append(listName + ".items.count=" + size
          + System.getProperty("line.separator"));
    for (int i = 0; i < size; i++)
      buffer.append(listName + "." + i + "=" + list.get(i)
          + System.getProperty("line.separator"));
    return buffer.toString();
  }
}