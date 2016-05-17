/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.scenario.horizontal;

import java.sql.Connection;

import org.objectweb.cjdbc.driver.ControllerInfo;
import org.objectweb.cjdbc.scenario.templates.HorizontalTemplate;
import org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.BlobTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.ClobTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.CopyTestLet;

/**
 * This class defines a BlobScenarioRevisited class
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class BlobRevisitedScenario extends HorizontalTemplate
{
  ControllerInfo[] controllers = new ControllerInfo[]{
      new ControllerInfo("localhost", 25322),
      new ControllerInfo("localhost", 25323)};

  /**
   * Test method for clobs.
   * 
   * @throws Exception of course...
   */
  public void testClob() throws Exception
  {
    Connection con = getCJDBCConnection(controllers);
    ClobTestLet let = new ClobTestLet(con);
    let.execute();
  }

  /**
   * Test a number of files to be copied and transfer and retrieved to the
   * database. The test fails if a single file test has failed, but a more in
   * depth results is displayed
   * 
   * @throws Exception if fails
   */

  public void testBlobs() throws Exception
  {
    //String[] testFiles = new
    // String[]{"/image/logo-noel.jpg","/image/smallpdf.pdf","/image/photo.jpg","/image/cbsetup.exe"};
    String[] testFiles = new String[]{"/image/logo-noel.jpg"};

    CopyTestLet let = new CopyTestLet();
    let.executeBatch(AbstractTestLet.FILE_NAME, testFiles);

    Connection con = getCJDBCConnection(controllers);
    BlobTestLet bloblet = new BlobTestLet(con);
    bloblet.set(AbstractTestLet.USE_CJDBC_CLASS, "true");
    bloblet.executeBatch(AbstractTestLet.FILE_NAME, testFiles);

    bloblet.set(AbstractTestLet.USE_CJDBC_CLASS, "true");
    bloblet.executeBatch(AbstractTestLet.FILE_NAME, testFiles);
  }
}