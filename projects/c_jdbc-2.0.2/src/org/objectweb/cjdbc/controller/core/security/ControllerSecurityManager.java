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
 * Contributor(s): _______________________
 */

package org.objectweb.cjdbc.controller.core.security;

import java.net.Socket;
import java.util.ArrayList;

import org.apache.regexp.RE;
import org.objectweb.cjdbc.common.net.SSLConfiguration;
import org.objectweb.cjdbc.common.xml.ControllerXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;

/**
 * Call this to check if security is enforced ....
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ControllerSecurityManager implements XmlComponent
{
  private boolean          allowAdditionalDriver = true;
  private boolean          allowConsoleShutdown  = true;
  private boolean          allowLocalConsoleOnly = true;
  private boolean          allowClientShutdown   = true;
  private boolean          allowLocalClientOnly  = true;
  private boolean          defaultConnect        = true;
  private ArrayList        accept;
  private ArrayList        saccept;
  private ArrayList        block;
  private ArrayList        sblock;
  private SSLConfiguration sslConfig;

  /**
   * Create a new security manager
   */
  public ControllerSecurityManager()
  {
    block = new ArrayList();
    accept = new ArrayList();
    saccept = new ArrayList();
    sblock = new ArrayList();
  }

  /**
   * Check connection policy for a client socket
   * 
   * @param clientSocket that is trying to connect
   * @return true if connection is allowed, false otherwise
   */
  public boolean allowConnection(Socket clientSocket)
  {
    if (checkList(accept, clientSocket))
      return true;
    if (checkList(block, clientSocket))
      return false;
    return defaultConnect;
  }

  /**
   * Add an ip range to the secure list
   * 
   * @param range to accept like 192.167.1.*
   * @param baccept true if accept false if block
   */
  public void addToSecureList(RE range, boolean baccept)
  {
    if (baccept)
      accept.add(range);
    else
      block.add(range);
  }

  /**
   * Add an ip range to the secure list. Same as above, but we want to store the
   * original string pattern as well.
   * 
   * @param range to accept
   * @param baccept true if accept false if block
   * @throws Exception if the pattern is not valid
   */
  public void addToSecureList(String range, boolean baccept) throws Exception
  {
    RE re = new RE(range);
    addToSecureList(re, baccept);
    if (baccept)
      saccept.add(range);
    else
      sblock.add(range);
  }

  /**
   * Add this host name or ipaddress to the secure list
   * 
   * @param host name or ipaddress
   * @param baccept true if accept false if block
   */
  public void addHostToSecureList(String host, boolean baccept)
  {
    if (baccept)
      accept.add(host);
    else
      block.add(host);
  }

  private static boolean checkList(ArrayList list, Socket clientSocket)
  {
    String hostAddress = clientSocket.getInetAddress().getHostAddress();
    String hostName = clientSocket.getInetAddress().getHostName();
    String ipaddress = clientSocket.getInetAddress().toString();
    Object o;
    RE re;
    String s;
    for (int i = 0; i < list.size(); i++)
    {
      o = list.get(i);
      if (o instanceof RE)
      {
        re = (RE) o;
        if (re.match(ipaddress))
          return true;
      }
      if (o instanceof String)
      {
        s = (String) o;
        if (s.equalsIgnoreCase(hostAddress) || s.equalsIgnoreCase(hostName))
          return true;
      }
    }
    return false;
  }

  /**
   * @return Returns the allowAdditionalDriver.
   */
  public boolean getAllowAdditionalDriver()
  {
    return allowAdditionalDriver;
  }

  /**
   * @param allowAdditionalDriver The allowAdditionalDriver to set.
   */
  public void setAllowAdditionalDriver(boolean allowAdditionalDriver)
  {
    this.allowAdditionalDriver = allowAdditionalDriver;
  }

  /**
   * @return Returns the allowClientShutdown.
   */
  public boolean getAllowClientShutdown()
  {
    return allowClientShutdown;
  }

  /**
   * @param allowClientShutdown The allowClientShutdown to set.
   */
  public void setAllowClientShutdown(boolean allowClientShutdown)
  {
    this.allowClientShutdown = allowClientShutdown;
  }

  /**
   * @return Returns the allowConsoleShutdown.
   */
  public boolean getAllowConsoleShutdown()
  {
    return allowConsoleShutdown;
  }

  /**
   * @param allowConsoleShutdown The allowConsoleShutdown to set.
   */
  public void setAllowConsoleShutdown(boolean allowConsoleShutdown)
  {
    this.allowConsoleShutdown = allowConsoleShutdown;
  }

  /**
   * @return Returns the allowLocalClientOnly.
   */
  public boolean getAllowLocalClientOnly()
  {
    return allowLocalClientOnly;
  }

  /**
   * @param allowLocalClientOnly The allowLocalClientOnly to set.
   */
  public void setAllowLocalClientOnly(boolean allowLocalClientOnly)
  {
    this.allowLocalClientOnly = allowLocalClientOnly;
  }

  /**
   * @return Returns the allowLocalConsoleOnly.
   */
  public boolean getAllowLocalConsoleOnly()
  {
    return allowLocalConsoleOnly;
  }

  /**
   * @param allowLocalConsoleOnly The allowLocalConsoleOnly to set.
   */
  public void setAllowLocalConsoleOnly(boolean allowLocalConsoleOnly)
  {
    this.allowLocalConsoleOnly = allowLocalConsoleOnly;
  }

  /**
   * @return Returns the defaultConnect.
   */
  public boolean getDefaultConnect()
  {
    return defaultConnect;
  }

  /**
   * @param defaultConnect The defaultConnect to set.
   */
  public void setDefaultConnect(boolean defaultConnect)
  {
    this.defaultConnect = defaultConnect;
  }

  /**
   * @return Returns the saccept.
   */
  public ArrayList getSaccept()
  {
    return saccept;
  }

  /**
   * @return Returns the sblock.
   */
  public ArrayList getSblock()
  {
    return sblock;
  }

  /**
   * @return Returns the accept.
   */
  public ArrayList getAccept()
  {
    return accept;
  }

  /**
   * @return Returns the block.
   */
  public ArrayList getBlock()
  {
    return block;
  }

  /**
   * @param block The block to set.
   */
  public void setBlock(ArrayList block)
  {
    this.block = block;
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<" + ControllerXmlTags.ELT_SECURITY + " "
        + ControllerXmlTags.ATT_DEFAULT_CONNECT + "=\""
        + this.getDefaultConnect() + "\">");

    sb.append("<" + ControllerXmlTags.ELT_JAR + " "
        + ControllerXmlTags.ATT_ALLOW + "=\"" + this.getAllowAdditionalDriver()
        + "\"/>");

    sb.append("<" + ControllerXmlTags.ELT_SHUTDOWN + ">");
    sb.append("<" + ControllerXmlTags.ELT_CLIENT + " "
        + ControllerXmlTags.ATT_ALLOW + "=\"" + this.getAllowClientShutdown()
        + "\" " + ControllerXmlTags.ATT_ONLY_LOCALHOST + "=\""
        + this.getAllowLocalClientOnly() + "\" " + "/>");
    sb.append("<" + ControllerXmlTags.ELT_CONSOLE + " "
        + ControllerXmlTags.ATT_ALLOW + "=\"" + this.getAllowConsoleShutdown()
        + "\" " + ControllerXmlTags.ATT_ONLY_LOCALHOST + "=\""
        + this.getAllowLocalConsoleOnly() + "\" " + "/>");
    sb.append("</" + ControllerXmlTags.ELT_SHUTDOWN + ">");

    sb.append("<" + ControllerXmlTags.ELT_ACCEPT + ">");
    ArrayList list = this.getSaccept();
    String tmp;
    for (int i = 0; i < list.size(); i++)
    {
      sb.append("<" + ControllerXmlTags.ELT_IPRANGE + " "
          + ControllerXmlTags.ATT_VALUE + "=\"" + list.get(i) + "\"/>");
    }
    list = this.getAccept();
    for (int i = 0; i < list.size(); i++)
    {
      if (list.get(i) instanceof RE)
        continue;
      tmp = (String) list.get(i);
      if (tmp.indexOf(".") == -1)
        sb.append("<" + ControllerXmlTags.ELT_HOSTNAME + " "
            + ControllerXmlTags.ATT_VALUE + "=\"" + tmp + "\"/>");
      else
        sb.append("<" + ControllerXmlTags.ELT_IPADDRESS + " "
            + ControllerXmlTags.ATT_VALUE + "=\"" + tmp + "\"/>");
    }
    sb.append("</" + ControllerXmlTags.ELT_ACCEPT + ">");

    sb.append("<" + ControllerXmlTags.ELT_BLOCK + ">");
    list = this.getSblock();
    for (int i = 0; i < list.size(); i++)
    {
      sb.append("<" + ControllerXmlTags.ELT_IPRANGE + " "
          + ControllerXmlTags.ATT_VALUE + "=\"" + list.get(i) + "\"/>");
    }
    list = this.getBlock();
    for (int i = 0; i < list.size(); i++)
    {
      if (list.get(i) instanceof RE)
        continue;
      tmp = (String) list.get(i);
      if (tmp.indexOf(".") == -1)
        sb.append("<" + ControllerXmlTags.ELT_HOSTNAME + " "
            + ControllerXmlTags.ATT_VALUE + "=\"" + tmp + "\"/>");
      else
        sb.append("<" + ControllerXmlTags.ELT_IPADDRESS + " "
            + ControllerXmlTags.ATT_VALUE + "=\"" + tmp + "\"/>");
    }
    sb.append("</" + ControllerXmlTags.ELT_BLOCK + ">");

    sb.append("</" + ControllerXmlTags.ELT_SECURITY + ">");
    return sb.toString();
  }

  /**
   * is ssl enabled for this controller
   * 
   * @return Returns wether ssl is enabled or not
   */
  public boolean isSSLEnabled()
  {
    return sslConfig != null;
  }

  /**
   * Returns the sslConfig value.
   * 
   * @return Returns the sslConfig.
   */
  public SSLConfiguration getSslConfig()
  {
    return sslConfig;
  }

  /**
   * Sets the sslConfig value.
   * 
   * @param sslConfig The sslConfig to set.
   */
  public void setSslConfig(SSLConfiguration sslConfig)
  {
    this.sslConfig = sslConfig;
  }
}
