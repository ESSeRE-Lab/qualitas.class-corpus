/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
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
 * Initial developer(s): Emmanuel Cecchet
 * Contributor(s): 
 */

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;
import org.objectweb.cjdbc.common.xml.XmlTools;
import org.objectweb.cjdbc.console.text.ConsoleException;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines a GetXml command that gather an XML dump of the virtual
 * database.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class GetXml extends AbstractAdminCommand
{

  /**
   * Creates a new <code>GetXml.java</code> object
   * 
   * @param module the command is attached to
   */
  public GetXml(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    try
    {
      String vdbXml = jmxClient.getVirtualDatabaseProxy(dbName, user, password)
      .getXml();
      StringBuffer validXml = new StringBuffer(XmlComponent.XML_VERSION);
      validXml.append("\n");
      validXml.append("<" + DatabasesXmlTags.ELT_CJDBC + ">\n");
      validXml.append(vdbXml);
      validXml.append("</" + DatabasesXmlTags.ELT_CJDBC + ">");
      
      String prettyXml = XmlTools.prettyXml(validXml.toString());
      prettyXml = XmlTools.insertCjdbcDoctype(prettyXml);
      console.println(prettyXml);
    }
    catch (Exception e)
    {
      throw new ConsoleException(ConsoleTranslate
          .get("controller.command.get.xml.error"));
    }

  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "show virtualdatabase config";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.get.xml.description");
  }

}