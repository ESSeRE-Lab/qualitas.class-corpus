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

package org.objectweb.cjdbc.scenario.standalone.jmx;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.objectweb.cjdbc.common.jmx.notifications.JmxNotification;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * This class defines a JmxNotificationTest class
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class JmxNotificationTest extends NoTemplate
{
  
  /**
   * Test xml serialization of jmx notifications
   * @throws Exception if fails
   */
  public void testCreateJmxNotification() throws Exception
  {
    System.out
        .println("###Create notification with parameters using the above method...");
    Hashtable data = new Hashtable();
    ArrayList backends = new ArrayList();
    backends.add("localhost");
    backends.add("localhost1");
    data.put("backends", backends);
    JmxNotification not = new JmxNotification("priority",
        "sequence", "type", "description", "time", "controller", "mbeanclass",
        "mbeanname", "serverIP", "serverPort",data);
    Document document = not.toXmlDocument();

    System.out.println("###Pretty print the document to System.out");
    OutputFormat format = OutputFormat.createPrettyPrint();
    XMLWriter writer = new XMLWriter(System.out, format);
    writer.write(document);

    System.out.println("###Compact format to string writer");
    StringWriter swriter = new StringWriter();
    format = OutputFormat.createCompactFormat();
    writer = new XMLWriter(swriter, format);
    writer.write(document);

    System.out.println("###Re-create the document from the string");
    StringReader sreader = new StringReader(swriter.toString());
    SAXReader reader = new SAXReader();
    Document document2 = reader.read(sreader);
    
    JmxNotification notif0 = JmxNotification.createNotificationFromXml(document2);
    JmxNotification notif1 = JmxNotification.createNotificationFromXml(document);
    assertEquals(notif0.toString(),notif1.toString());
    

    System.out.println("###Re-pretty print the document");
    writer = new XMLWriter(System.out, OutputFormat.createPrettyPrint());
    writer.write(document2);

    // Get some attribute
    Node node = document.selectSingleNode("//jmxevent/info/description");
    System.out.println("Description of the node:" + node.getText());
    assertEquals("Value not expected",node.getText(),"description");

    // Serialize and de-serialize the notification
    JmxNotification notification = JmxNotification
        .createNotificationFromXml(document2);
    Document document3 = notification.toXmlDocument();
    JmxNotification notif2 = JmxNotification
        .createNotificationFromXml(document3);
    assertEquals("Xml documents are different", notification.toString(), notif2
        .toString());
  }
}