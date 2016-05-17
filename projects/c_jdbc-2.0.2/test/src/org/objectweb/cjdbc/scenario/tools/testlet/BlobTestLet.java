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

package org.objectweb.cjdbc.scenario.tools.testlet;

import java.io.File;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * Make tests on blobs, using bytes or directly the blob object
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BlobTestLet extends AbstractConnectionTestLet
{

  /**
   * Creates a new <code>BlobTestLet</code> object
   * 
   * @param con connection to use for testing
   */
  public BlobTestLet(Connection con)
  {
    super(con);
    config.put(TABLE_NAME, "BLOB");
    config.put(COLUMN_NAME, "blob");
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet#execute()
   */
  public void execute() throws Exception
  {
    String storeFile = (String) config.get(FILE_NAME);
    String tableName = (String) config.get(TABLE_NAME);
    String columnName = (String) config.get(COLUMN_NAME);
    
    File fis = new File(storeFile);
    if(!fis.exists())
      fis = new File(getClass().getResource(storeFile).getFile());

    if (storeFile == null || tableName == null || !fis.exists())
      throw new Exception("Cannot run BlobTestLet with given arguments");

    boolean ok = true;
    // Delete previous entry
    String query = "Delete from " + tableName + " where id='1'";
    PreparedStatement ps1, ps2;
    jdbcConnection.createStatement().executeUpdate(query);

    // Store file in database

    query = "Insert into " + tableName + " values(1,?)";
    ps1 = jdbcConnection.prepareStatement(query);
    if (useCJDBCClass())
    {
      Blob bob = new org.objectweb.cjdbc.driver.Blob(ScenarioUtility
          .readBinary(fis));
      ps1.setBlob(1, bob);
    }
    else
    {
      ps1.setBytes(1, ScenarioUtility.readBinary(fis));
    }
    ps1.executeUpdate();

    // Read File from database
    query = "select * from " + tableName + " where id=1";
    ps2 = jdbcConnection.prepareStatement(query);
    ResultSet rs = ps2.executeQuery();
    assertFalse("Unexpected null result", rs.wasNull());
    assertTrue("Unexpected result", rs.first());

    byte[] lisette;
    if (useCJDBCClass())
    {
      Blob blisette = rs.getBlob(columnName);
      lisette = blisette.getBytes(1, (int) blisette.length());
    }
    else
      lisette = rs.getBytes(columnName);

    // Write Retrieved blob
    String filename = (useCJDBCClass()) ? fis.getAbsolutePath() + ".blobed" : fis.getAbsolutePath()
        + ".byted";
    File fos = new File(filename);
    ScenarioUtility.writeBinary(lisette, fos);

    // Test retrieved Blob
    if (fis.length() != fos.length())
    {
      System.out.println("Lenght are different:" + fis.length() + ";"
          + fos.length());
      ok = false;
    }
    // Close statements
    ps1.close();
    ps2.close();
    fos.delete();

    assertTrue("BlobTestLet failed with:" + storeFile
        + ". CalledBlob was used:" + useCJDBCClass(), ok);

  }

}