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

package org.objectweb.cjdbc.scenario.raidb1.recoverylog;

import org.objectweb.cjdbc.controller.backend.DatabaseBackend;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.scenario.templates.Raidb1RecoveryTemplate;

/**
 * If a backend is disable from a checkpoint, the other backends receive
 * updates, and then we enable the backend again, the recovery log should replay
 * all the missing requests.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BasicRecoveryScenario extends Raidb1RecoveryTemplate
{

  /**
   * Test the last known checkpoint is properly set after a disable
   * 
   * @throws Exception if fails
   */
  public void testLastKnownCheckpoint() throws Exception
  {
    String backend = "localhost2";
    String checkpoint = "check" + System.currentTimeMillis();
    mainVdb.disableBackendWithCheckpoint(backend);
    DatabaseBackend db = mainVdb.getAndCheckBackend(backend,
        VirtualDatabase.NO_CHECK_BACKEND);
    assertEquals("Invalid checkpoint", db.getLastKnownCheckpoint(), checkpoint);
  }
}