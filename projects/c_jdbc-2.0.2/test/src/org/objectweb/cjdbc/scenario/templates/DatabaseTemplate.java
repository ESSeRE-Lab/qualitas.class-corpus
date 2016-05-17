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

package org.objectweb.cjdbc.scenario.templates;

import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;
import org.objectweb.cjdbc.scenario.tools.components.backend.DatabaseManager;

/**
 * This class defines a DatabaseTemplate where a single database instance is started and stop
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modryzk</a>
 * @version 1.0
 */
public abstract class DatabaseTemplate extends Template
{
  protected DatabaseManager hm  = new DatabaseManager();
  protected ComponentInterface hm1 = null;

  protected void setUp()
  {
    hm = new DatabaseManager();
    try
    {
      hm1 = hm.start("9001");
      hm.loaddatabase("9001");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  protected void tearDown()
  {
    hm.stopAll();
  }
}