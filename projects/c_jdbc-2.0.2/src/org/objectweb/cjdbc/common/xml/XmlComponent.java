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

package org.objectweb.cjdbc.common.xml;

import org.objectweb.cjdbc.common.util.Constants;

/**
 * This class defines a XmlComponent
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public interface XmlComponent
{
  /**
   * Get xml formatted representation of this cjdbc component
   * 
   * @return xml formatted fragment
   */
  String getXml();

  /**
   * Xml Definition
   */
  String XML_VERSION        = "<?xml version=\"1.0\" encoding=\"UTF8\" ?>";
  /**
   * Doctype for the virtual database
   */
  String DOCTYPE_DB         = "<!DOCTYPE C-JDBC PUBLIC \"-//ObjectWeb//DTD C-JDBC "
                                + Constants.VERSION
                                + "//EN\" \"http://c-jdbc.objectweb.org/dtds/c-jdbc-"
                                + Constants.VERSION + ".dtd\">";
  /**
   * Doctype for the virtual database
   */
  String DOCTYPE_CONTROLLER = "<!DOCTYPE C-JDBC-CONTROLLER PUBLIC \"-//ObjectWeb//DTD C-JDBC-CONTROLLER "
                                + Constants.VERSION
                                + "//EN\" \"http://c-jdbc.objectweb.org/dtds/c-jdbc-controller-"
                                + Constants.VERSION + ".dtd\">";
}