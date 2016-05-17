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

import org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter;
import org.objectweb.cjdbc.common.sql.filters.HexaBlobFilter;
import org.objectweb.cjdbc.common.stream.encoding.ZipEncoding;
import org.objectweb.cjdbc.scenario.templates.Template;

/**
 * This class defines scenario to test encoding and decoding
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class EncodingScenario extends Template
{

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {

  }
  
  /**
   * Test ZipEncoding. Works but we cannot convert back and forth to string so 
   * we can't use this method 
   * 
   * @throws Exception if fails.
   */
  public void testZipEncoding() throws Exception
  {
    String ss = "This is a very long string, please do not cut me";
    byte[] zip = ZipEncoding.encode(ss.getBytes());
    System.out.println(new String(zip));
    byte[] unzip = ZipEncoding.decode(zip);
    System.out.println(new String(unzip));
    assertTrue("Decompressed byte[] is not what expected",ss.equals(new String(unzip)));    
  }
 

  /**
   * Compare between different encoding methods for blobs
   * 
   * @throws Exception if fails
   */
  public void testCompareEncoding() throws Exception
  {
    AbstractBlobFilter filter1 = new HexaBlobFilter();
    //AbstractBlobFilter filter2 = new Base64BlobFilter();
    //AbstractBlobFilter filter3 = new ZipBlobFilter();
    String ss = "This is a very long string, please do not cut me";
    
    String ss1 = filter1.encode(ss);
    System.out.println(ss1);
    //String ss2 = filter2.encode(ss);
    //System.out.println(ss2);
    //String ss3 = filter3.encode(ss);
    //System.out.println(ss3);

    //assertTrue("Base64 encoding result is bigger than hexa encoding:[" + ss1
    //    + "][" + ss2 + "]", ss1.length() > ss2.length());
    String test1 = new String(filter1.decode(ss1));
    System.out.println(test1);
    //String test2 = new String(filter2.decode(ss2));
    //System.out.println(test2);
    //String test3 = new String(filter3.decode(ss3));
    //System.out.println(test3);

    assertTrue("test1[" + test1 + "]!=original[" + ss + "]", test1
        .equals(ss));
    //assertTrue("test2[" + test2 + "]!=test3[" + test3 + "]", test2
    //    .equals(test3));
    //assertTrue("test2[" + test2 + "]!=original[" + ss + "]", test2.equals(ss));
  }

}
