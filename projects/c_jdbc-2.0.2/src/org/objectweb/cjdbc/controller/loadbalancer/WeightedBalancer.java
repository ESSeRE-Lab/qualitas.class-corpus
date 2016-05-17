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
 */

package org.objectweb.cjdbc.controller.loadbalancer;

import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * To return information, weighted load balancers share the same kind of
 * information on backend configuration.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class WeightedBalancer
{
  /**
   * get different xml tags of the weights in the system.
   * 
   * @param weights a list ((String)name,(Integer)weight) of weights
   * @return xml formatted string of weighted backends
   */
  public static final String getWeightedXml(HashMap weights)
  {
    if (weights == null)
      return "";
    StringBuffer info = new StringBuffer();
    String nametmp;
    for (Iterator iterator = weights.keySet().iterator(); iterator.hasNext();)
    {
      nametmp = (String) iterator.next();
      info
          .append("<" + DatabasesXmlTags.ELT_BackendWeight + " "
              + DatabasesXmlTags.ATT_name + "=\"" + nametmp + "\" "
              + DatabasesXmlTags.ATT_weight + "=\"" + weights.get(nametmp)
              + "\"/>");
    }
    return info.toString();
  }

  /**
   * Convert raidb weighted balancers into xml because they share common views.
   * 
   * @param weights hashmap of (name,weight)
   * @param xmltag the xml tag to use
   * @return xml formatted string
   */
  public static final String getRaidbXml(HashMap weights, String xmltag)
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + xmltag + ">");
    info.append(WeightedBalancer.getWeightedXml(weights));
    info.append("</" + xmltag + ">");
    return info.toString();
  }
}