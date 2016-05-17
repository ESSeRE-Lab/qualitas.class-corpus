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

package org.objectweb.cjdbc.scenario.standalone.streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.objectweb.cjdbc.common.stream.CJDBCInputStream;
import org.objectweb.cjdbc.common.stream.CJDBCOutputStream;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * This class defines a UTFEncodingTest
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class UTFEncodingTest extends NoTemplate
{
  /**
   * Test Encoding 
   * 
   * @throws Exception if fails
   */
  public void testCJDBCStreamsUTFEncoding() throws Exception
  {
    File f = new File("test");
    f.deleteOnExit();
    
    FileOutputStream fos = new FileOutputStream(f);
    CJDBCOutputStream output = new CJDBCOutputStream(fos);
    
    String arigatou = "ありがとう";
    output.writeUTF(arigatou);
    output.flush();
    output.close();
    
    FileInputStream fis = new FileInputStream(f);
    CJDBCInputStream input = new CJDBCInputStream(fis);
    String arigatouRead = input.readUTF();
    System.out.println(arigatouRead);
    assertEquals(arigatou,arigatouRead);
  }
}
