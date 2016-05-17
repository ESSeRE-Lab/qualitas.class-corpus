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

package org.objectweb.cjdbc.scenario.users;

import org.objectweb.cjdbc.scenario.templates.Raidb1RecoveryWithRequestSenderTemplate;
import org.objectweb.cjdbc.scenario.tools.util.QueryGenerator;

/**
 * This class defines a JunailiScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class JunailiScenario extends Raidb1RecoveryWithRequestSenderTemplate
{
  public void testSpeedDecrease() throws Exception
  {

    sender.setMonitorSpeed(true);
    sender.setRequestInterval(1);
    sender.setUsePreparedStatement(false);

    // stop the thread, does not have good options
    sender.setQuit(true);
    t.join();
    sender.setQuit(false);

    //  write intensive
    QueryGenerator generator = new QueryGenerator(getCJDBCConnection());
    generator.setSchemaUpdateRatio(0);
    generator.setCreateDropRatio(1);
    generator.setReadWriteRatio(0.5);
    generator.setInsertUpdateRatio(0);
    sender.setQueryGenerator(generator);
    sender.setUseQueryGenerator(true);
    t = new Thread(sender, "RequestSender");
    t.start();
    int count = 0;
    while (count++ < 10000)
    {
      Thread.sleep(1000);
      //System.out.println("Request(" + sender.getRequestCount() + "):"
      //    + sender.getAverage() + " ms");
    }

    sender.setQuit(true);
    System.out.println("Request Response Time Average( out of "
        + sender.getRequestCount() + " requests):" + sender.getAverage()
        + " ms");
    if (sender.getAverage() > 1000)
      fail("Average response time too slow ...");
  }
}