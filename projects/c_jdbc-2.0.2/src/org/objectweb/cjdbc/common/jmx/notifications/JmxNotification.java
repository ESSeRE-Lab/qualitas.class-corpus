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

package org.objectweb.cjdbc.common.jmx.notifications;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * This class defines a JmxNotification class. This class is used by the
 * <code>RmiConnector</code> on the controller to send a JMXNotification. This
 * is done by the following code:
 * 
 * <pre>
 * JmxNotification cjdbcNotification = new JmxNotification(priority,
 *                                       &quot;&quot; + sequence, type, description, &quot;&quot;
 *                                           + time, controllerName, mbean
 *                                           .getClass().getName(), &quot;mbeanName&quot;,
 *                                       hostName, &quot;&quot; + port, data);
 * </pre>
 * 
 * This create an instance of this JmxNotification class, specific to CJDBC. We
 * then create a new instance of the Notification object as specified in the
 * javax.management package
 * 
 * <pre>
 * Notification notification = new Notification(type, mbean, sequence, myDate
 *     .getTime(), description);
 * </pre>
 * 
 * This class accepts a userData object. We wanted to set this user object to a
 * specific CJDBC class but this forces generic JMX client to have this class in
 * their classpath. We just serialize the JmxNotification into an XML string and
 * feed it in the notification.
 * 
 * <pre>
 * notification.setUserData(cjdbcNotification.toString());
 * </pre>
 * 
 * This can be retrieved on any jmx client, and on C-JDBC specific clients, the
 * xml is transformed into an instance of this class again for easier
 * notification handling.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class JmxNotification
{
  /**
   * All the tags and attributes used to parse the notification
   */
  private static final String ELT_jmxevent    = "jmxevent";
  private static final String ELT_info        = "info";
  private static final String ELT_source      = "source";
  private static final String ELT_data        = "data";
  private static final String ELT_priority    = "priority";
  private static final String ELT_sequence    = "sequence";
  private static final String ELT_type        = "type";
  private static final String ELT_description = "description";
  private static final String ELT_time        = "time";
  private static final String ELT_controller  = "controller";
  private static final String ELT_mbean       = "mbean";
  private static final String ELT_class       = "class";
  private static final String ELT_server      = "server";
  private static final String ELT_value       = "value";

  private static final String ATT_ip          = "ip";
  private static final String ATT_port        = "port";
  private static final String ATT_name        = "name";

  String                      priority;
  String                      sequence;
  String                      type;
  String                      description;
  String                      controllerName;
  String                      mbeanClass;
  String                      mbeanName;
  String                      mbeanServerIP;
  String                      mbeanServerPort;
  String                      time;
  Hashtable                   dataList;

  /**
   * Create a new JmxNotification object
   * 
   * @param priority notification priority
   * @param sequence sequence number
   * @param type notification type
   * @param description notification description
   * @param time time the notification was issued
   * @param controllerName name of the controller issuing the notification
   * @param mbeanClass class of the mbean issuing the notification
   * @param mbeanName name of the mbean issuing the notification
   * @param mbeanServerIP IP address of the mbean
   * @param mbeanServerPort Port of the mbean
   * @param dataList Additional data
   */
  public JmxNotification(String priority, String sequence, String type,
      String description, String time, String controllerName,
      String mbeanClass, String mbeanName, String mbeanServerIP,
      String mbeanServerPort, Hashtable dataList)
  {
    this.priority = priority;
    this.sequence = sequence;
    this.type = type;
    this.description = description;
    this.controllerName = controllerName;
    this.mbeanClass = mbeanClass;
    this.mbeanName = mbeanName;
    this.mbeanServerIP = mbeanServerIP;
    this.mbeanServerPort = mbeanServerPort;
    this.time = time;
    this.dataList = dataList;
  }

  /**
   * Parse the given xml to create a notification
   * 
   * @param xml the xml to use to create a <code>JmxNotification</code>
   *          instance
   * @return a <code>JmxNotification</code> instance which xml version is that
   *         of the given xml arg
   * @throws Exception if cannot create an instance (the xml is invalid)
   */
  public static JmxNotification createNotificationFromXmlString(String xml)
      throws Exception
  {
    StringReader sreader = new StringReader(xml);
    SAXReader reader = new SAXReader();
    Document document = reader.read(sreader);
    return createNotificationFromXml(document);
  }

  /**
   * @return Returns the dataList.
   */
  public Hashtable getDataList()
  {
    return dataList;
  }

  /**
   * Returns the first value of an entry in the data list of values
   * 
   * @param key value of a data parameter in the list
   * @return <code>String</code> value of the corresponding key
   */
  public String getDataValue(String key)
  {
    if (!dataList.containsKey(key))
      return null;
    else
      return (String) ((ArrayList) dataList.get(key)).get(0);
  }

  /**
   * @return Returns the controllerName.
   */
  public String getControllerName()
  {
    return controllerName;
  }

  /**
   * Return the controller jmx name
   * 
   * @return <code>IP:Port</code>
   */
  public String getControllerJmxName()
  {
    return getMbeanServerIP() + ":" + getMbeanServerPort();
  }

  /**
   * @param controllerName The controllerName to set.
   */
  public void setControllerName(String controllerName)
  {
    this.controllerName = controllerName;
  }

  /**
   * @return Returns the description.
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * @param description The description to set.
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * @return Returns the mbeanClass.
   */
  public String getMbeanClass()
  {
    return mbeanClass;
  }

  /**
   * @param mbeanClass The mbeanClass to set.
   */
  public void setMbeanClass(String mbeanClass)
  {
    this.mbeanClass = mbeanClass;
  }

  /**
   * @return Returns the mbeanName.
   */
  public String getMbeanName()
  {
    return mbeanName;
  }

  /**
   * @param mbeanName The mbeanName to set.
   */
  public void setMbeanName(String mbeanName)
  {
    this.mbeanName = mbeanName;
  }

  /**
   * @return Returns the mbeanServerIP.
   */
  public String getMbeanServerIP()
  {
    return mbeanServerIP;
  }

  /**
   * @param mbeanServerIP The mbeanServerIP to set.
   */
  public void setMbeanServerIP(String mbeanServerIP)
  {
    this.mbeanServerIP = mbeanServerIP;
  }

  /**
   * @return Returns the mbeanServerPort.
   */
  public String getMbeanServerPort()
  {
    return mbeanServerPort;
  }

  /**
   * @param mbeanServerPort The mbeanServerPort to set.
   */
  public void setMbeanServerPort(String mbeanServerPort)
  {
    this.mbeanServerPort = mbeanServerPort;
  }

  /**
   * @return Returns the priority.
   */
  public String getPriority()
  {
    return priority;
  }

  /**
   * @param priority The priority to set.
   */
  public void setPriority(String priority)
  {
    this.priority = priority;
  }

  /**
   * @return Returns the sequence.
   */
  public String getSequence()
  {
    return sequence;
  }

  /**
   * @param sequence The sequence to set.
   */
  public void setSequence(String sequence)
  {
    this.sequence = sequence;
  }

  /**
   * @return Returns the type.
   */
  public String getType()
  {
    return type;
  }

  /**
   * @param type The type to set.
   */
  public void setType(String type)
  {
    this.type = type;
  }

  /**
   * Used as a factory to create an instance of this class from a xml document
   * 
   * @param document a dom4j document
   * @return an instance of this class with the corresponding parameters
   */
  public static JmxNotification createNotificationFromXml(Document document)
  {
    String priority = document.selectSingleNode(
        "//" + ELT_jmxevent + "/" + ELT_info + "/" + ELT_priority).getText();
    String sequence = document.selectSingleNode(
        "//" + ELT_jmxevent + "/" + ELT_info + "/" + ELT_sequence).getText();
    String type = document.selectSingleNode(
        "//" + ELT_jmxevent + "/" + ELT_info + "/" + ELT_type).getText();
    String description = document.selectSingleNode(
        "//" + ELT_jmxevent + "/" + ELT_info + "/" + ELT_description).getText();
    String time = document.selectSingleNode(
        "//" + ELT_jmxevent + "/" + ELT_info + "/" + ELT_time).getText();

    String controllerName = document.selectSingleNode(
        "//" + ELT_jmxevent + "/" + ELT_source + "/" + ELT_controller)
        .getText();
    String mbeanclass = document.selectSingleNode(
        "//" + ELT_jmxevent + "/" + ELT_source + "/" + ELT_mbean + "/"
            + ELT_class).getText();
    String mbeanname = document.selectSingleNode(
        "//" + ELT_jmxevent + "/" + ELT_source + "/" + ELT_mbean).valueOf(
        "@" + ATT_name);
    String serverip = document.selectSingleNode(
        "//" + ELT_jmxevent + "/" + ELT_source + "/" + ELT_mbean + "/"
            + ELT_server).valueOf("@" + ATT_ip);
    String serverport = document.selectSingleNode(
        "//" + ELT_jmxevent + "/" + ELT_source + "/" + ELT_mbean + "/"
            + ELT_server).valueOf("@" + ATT_port);

    Element root = document.getRootElement();

    Hashtable dataList = new Hashtable();
    for (Iterator i = root.elementIterator(ELT_data); i.hasNext();)
    {
      Element data = (Element) i.next();
      ArrayList list = new ArrayList();
      for (Iterator j = data.elementIterator(ELT_value); j.hasNext();)
      {
        Element value = (Element) j.next();
        list.add(value.getTextTrim());
      }
      dataList.put(data.valueOf("@" + ATT_name), list);
    }
    JmxNotification notif = new JmxNotification(priority, sequence, type,
        description, time, controllerName, mbeanclass, mbeanname, serverip,
        serverport, dataList);
    return notif;
  }

  /**
   * Convert the object to the corresponding xml document instance
   * 
   * @return <code>Document</code> object with the proper values
   */
  public Document toXmlDocument()
  {
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement(ELT_jmxevent);

    // Describe info
    Element info = root.addElement(ELT_info);
    info.addElement(ELT_priority).addText(priority);
    info.addElement(ELT_sequence).addText(sequence);
    info.addElement(ELT_type).addText(type);
    info.addElement(ELT_description).addText(description);
    info.addElement(ELT_time).addText(time);

    // Describe source
    Element source = root.addElement(ELT_source);
    source.addElement(ELT_controller).addText(controllerName);

    // Describe mbean
    Element mbean = source.addElement(ELT_mbean).addAttribute(ATT_name,
        mbeanName);
    mbean.addElement(ELT_class).addText(mbeanClass);
    mbean.addElement(ELT_server).addAttribute(ATT_ip, mbeanServerIP)
        .addAttribute(ATT_port, mbeanServerPort);

    // Describe data
    Enumeration keys = dataList.keys();
    while (keys.hasMoreElements())
    {
      String key = (String) keys.nextElement();
      Element data = root.addElement(ELT_data).addAttribute(ATT_name, key);

      Object entry = dataList.get(key);
      if (entry instanceof ArrayList)
      {
        ArrayList list = (ArrayList) entry;
        for (int i = 0; i < list.size(); i++)
          data.addElement(ELT_value).addText((String) list.get(i));
      }
      else if (entry instanceof String[])
      {
        String[] list = (String[]) entry;
        for (int i = 0; i < list.length; i++)
          data.addElement(ELT_value).addText(list[i]);
      }
      else if (entry instanceof String)
      {
        data.addElement(ELT_value).addText((String) entry);
      }
    }
    return document;
  }

  /**
   * @return <code>String</code> version in xml formatted text
   */
  public String toString()
  {
    StringWriter swriter = new StringWriter();
    OutputFormat format = OutputFormat.createCompactFormat();
    XMLWriter writer = new XMLWriter(swriter, format);
    try
    {
      writer.write(toXmlDocument());
    }
    catch (IOException e)
    {
      // ignore
    }
    return swriter.getBuffer().toString();
  }

  /**
   * @return Returns the time.
   */
  public String getTime()
  {
    return time;
  }

  /**
   * @param time The time to set.
   */
  public void setTime(String time)
  {
    this.time = time;
  }

  /**
   * @param dataList The dataList to set.
   */
  public void setDataList(Hashtable dataList)
  {
    this.dataList = dataList;
  }
}