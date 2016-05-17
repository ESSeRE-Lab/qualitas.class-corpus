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

package org.objectweb.cjdbc.console.text.commands.monitor;

import org.objectweb.cjdbc.console.jmx.RmiJmxClient;
import org.objectweb.cjdbc.console.text.commands.ConsoleCommand;
import org.objectweb.cjdbc.console.text.module.MonitorConsole;

/**
 * This class defines a AbstractMonitorCommand
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public abstract class AbstractMonitorCommand extends ConsoleCommand
{

  protected MonitorConsole module;
  protected RmiJmxClient jmxClient;
  protected String currentTarget;
  
  /**
   * Creates a new <code>AbstractMonitorCommand.java</code> object
   * 
   * @param module the monitor console
   */
  public AbstractMonitorCommand(MonitorConsole module)
  {
    super(module);
    this.module = module;
    this.currentTarget = module.getCurrentTarget();
    this.jmxClient = console.getJmxClient();
  }

  /**
   * Sets the currentTarget value.
   * 
   * @param currentTarget The currentTarget to set.
   */
  public void setCurrentTarget(String currentTarget)
  {
    this.currentTarget = currentTarget;
  }
}
