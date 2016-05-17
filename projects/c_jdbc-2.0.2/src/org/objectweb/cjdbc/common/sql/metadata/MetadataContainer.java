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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.common.sql.metadata;

import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.cjdbc.common.log.Trace;

/**
 * A MetadataContainer is basically a hashtable of jdbc metadata. We may want to
 * override a few options from the usual Hashtable so I've put it in a separate
 * class.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public final class MetadataContainer extends HashMap
{
  private static final long serialVersionUID = 564436668119294938L;

  private final String      line             = System
                                                 .getProperty("line.separator");
  private String            url;

  /**
   * Creates a new <code>MetadataContainer</code> object
   * 
   * @param url which url is this container pointed to
   */
  public MetadataContainer(String url)
  {
    this.url = url;
  }

  /**
   * Check to see if two metadata sets are identical. All incompatible values
   * are logged as warning into the logger given.
   * 
   * @param container the container to check compatibility with
   * @param logger the logger, if null, echo on stderr
   * @return true if all metadata are identical.
   */
  public boolean isCompatible(MetadataContainer container, Trace logger)
  {
    if (keySet() == null)
      return container.keySet() == null;
    Iterator keys = keySet().iterator();
    boolean isCompatible = true;
    String key;
    Object value1;
    Object value2;
    String log;
    while (keys.hasNext())
    {
      key = (String) keys.next();
      value1 = get(key);
      value2 = container.get(key);
      if (!value1.equals(value2))
      {
        isCompatible = false;
        log = "Metadata key [" + key + "] is not compatible. (Backends are: ["
            + url + "] and [" + container.getUrl() + "] ; Values are:["
            + value1 + "] and [" + value2 + "])";
        if (logger != null)
          logger.warn(log);
        else
          System.err.println(log);
      }
    }
    return isCompatible;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    if (keySet() == null)
      return "no metadata";
    StringBuffer buffer = new StringBuffer();
    Iterator keys = keySet().iterator();
    String element;
    while (keys.hasNext())
    {
      element = (String) keys.next();
      buffer.append(element + " : " + this.get(element) + line);
    }
    return buffer.toString();
  }

  /**
   * Returns the url value.
   * 
   * @return Returns the url.
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Get the metadata container key for the given query. Serializes the method
   * call into a "getXXX(Y,Z,...)" String (with name, signature and arguments).
   * 
   * @param methodName method invoked to generate the key in the container
   * @param parametersType parameters type of invoked method
   * @param arguments arguments used to invoke the method
   * @return container key for the given method call
   */
  public static String getContainerKey(String methodName,
      Class[] parametersType, Object[] arguments)
  {
    if (parametersType == null)
    { // Function without parameters, just store the function name as the key
      return methodName;
    }
    else
    { // Include all argument in function name
      StringBuffer key = new StringBuffer(methodName);
      key.append('(');
      for (int i = 0; i < parametersType.length; i++)
      {
        Class c = parametersType[i];
        if (c != null)
          key.append(c.getName());
        else
          key.append("null");
        key.append('=');
        Object arg = arguments[i];
        if (arg != null)
          key.append(arg.toString());
        else
          key.append("null");
        key.append(',');
      }
      // Replace last comma with )
      key.setCharAt(key.length() - 1, ')');
      return key.toString();
    }
  }
}