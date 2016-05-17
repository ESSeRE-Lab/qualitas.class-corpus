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
package org.objectweb.cjdbc.scenario.raidb1.xml;

import org.objectweb.cjdbc.common.xml.XmlComponent;
import org.objectweb.cjdbc.controller.core.ControllerConstants;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.scenario.templates.Raidb1Template;

/**
 * This class defines a XmlGenerationScenario class
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class XmlGenerationScenario extends Raidb1Template
{
  
  /**
   * Test if the xml generated from the virtualdatabase can be used again
   * to construct the same virtual database.
   * The test is repeated twice to be sure the xml is consistent
   * @throws Exception if fails
   */
  public void testDatabaseXml() throws Exception
  {
    VirtualDatabase vd = controller.getVirtualDatabase("myDB");
    String xml1 = vd.getXml();
    xml1 = XmlComponent.XML_VERSION +XmlComponent.DOCTYPE_DB + xml1;
    controller.removeVirtualDatabase("myDB");
    System.out.println(xml1);
    controller.addVirtualDatabases(xml1,"myDB",ControllerConstants.AUTO_ENABLE_TRUE,"");
    VirtualDatabase vd2 = controller.getVirtualDatabase("myDB");
    String xml2 = vd2.getXml();
    xml2 = XmlComponent.XML_VERSION +XmlComponent.DOCTYPE_DB + xml2;
    System.out.println(xml2);
    
    controller.removeVirtualDatabase("myDB");
    controller.addVirtualDatabases(xml2,"myDB",ControllerConstants.AUTO_ENABLE_TRUE,"");
    VirtualDatabase vd3 = controller.getVirtualDatabase("myDB");
    String xml3 = vd3.getXml();
    xml3 = XmlComponent.XML_VERSION +XmlComponent.DOCTYPE_DB + xml3;
    
    assertEquals("Xml files are different",xml3,xml2);
  }
}
