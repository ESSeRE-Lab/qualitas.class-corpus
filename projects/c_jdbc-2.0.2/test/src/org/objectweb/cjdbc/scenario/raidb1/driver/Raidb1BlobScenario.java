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

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.BlobTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.ClobTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.CopyTestLet;

/**
 * This class defines a Raidb1BlobScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class Raidb1BlobScenario extends Raidb1Template
{
  /**
   * Test method for clobs.
   * 
   * @throws Exception of course...
   */
  public void testClob() throws Exception
  {
    Connection con = getCJDBCConnection();
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
    //String[] testFiles = new String[]{"/image/logo-noel.jpg","/image/smallpdf.pdf","/image/photo.jpg","/image/cbsetup.exe"};
    String[] testFiles = new String[]{"/image/logo-noel.jpg","/image/photo.jpg","/image/cbsetup.exe"};
     
    CopyTestLet let = new CopyTestLet();
    let.executeBatch(AbstractTestLet.FILE_NAME,testFiles);
    
    Connection con = getCJDBCConnection();
    BlobTestLet bloblet = new BlobTestLet(con);
    bloblet.set(AbstractTestLet.USE_CJDBC_CLASS,"true");
    bloblet.executeBatch(AbstractTestLet.FILE_NAME,testFiles);
    
    bloblet.set(AbstractTestLet.USE_CJDBC_CLASS,"true");
    bloblet.executeBatch(AbstractTestLet.FILE_NAME,testFiles);
  }

}