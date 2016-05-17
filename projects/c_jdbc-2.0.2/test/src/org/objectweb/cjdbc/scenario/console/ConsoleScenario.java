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

package org.objectweb.cjdbc.scenario.console;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;

import org.objectweb.cjdbc.common.jmx.JmxConstants;
import org.objectweb.cjdbc.console.jmx.RmiJmxClient;
import org.objectweb.cjdbc.console.text.Console;
import org.objectweb.cjdbc.scenario.templates.Raidb1Template;

/**
 * This class defines a ConsoleScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class ConsoleScenario extends Raidb1Template
{
  String consoleDir = "/console";
  
  /**
   * Test the text console
   * 
   * @throws Exception possibly
   */
  public void testConsoleStart() throws Exception
  {
    String ip = InetAddress.getLocalHost().getHostName();
    String jmxport = ""+JmxConstants.DEFAULT_JMX_RMI_PORT;
    RmiJmxClient jmxClient = new RmiJmxClient(jmxport, ip,null);
    
    File file = new File(getClass().getResource(consoleDir+"/test1.console").getFile());
    FileInputStream in = new FileInputStream(file);

    Console console = new Console(jmxClient, in, false, true);
    console.handlePrompt();
  }
}