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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.templates;

import org.objectweb.cjdbc.scenario.tools.util.RequestSender;

/**
 * This class defines a Raidb1RecoveryWithRequestSenderTemplate
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class Raidb1RecoveryWithRequestSenderTemplate
    extends Raidb1RecoveryTemplate
{
  protected RequestSender sender;
  protected Thread        t;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    // Do set up
    super.setUp();
    try
    {
      // Start request thread
      sender = new RequestSender(getCJDBCConnection());
      sender.setRequestInterval(500);
      t = new Thread(sender,"RequestSender");
      t.start();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("Could not start sender thread:" + e);
    }
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {
    // End request thread
    if (t != null)
    {
      sender.setQuit(true);

      try
      {
        t.join(1000);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }

    // Do tear down
    super.tearDown();
  }
}
