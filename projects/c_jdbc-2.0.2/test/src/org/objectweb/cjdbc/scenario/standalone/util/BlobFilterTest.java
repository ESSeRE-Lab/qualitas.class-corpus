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

package org.objectweb.cjdbc.scenario.standalone.util;

import org.objectweb.cjdbc.common.sql.filters.AbstractBlobFilter;
import org.objectweb.cjdbc.common.sql.filters.HexaBlobFilter;
import org.objectweb.cjdbc.common.sql.filters.NoneBlobFilter;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * This class defines a BlobFilterTest. Test different blob filters.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BlobFilterTest extends NoTemplate
{

  /**
   * Test hexa blob filter
   *
   * @throws Exception if fails
   */
  public void testHexMethods() throws Exception
  {
    String ss = "This is a string";
    AbstractBlobFilter filter = new HexaBlobFilter();
    System.out.println(filter.getClass());
    String ss1 = filter.encode(ss);
    String test = new String(filter.decode(ss1));
    assertTrue("Strings are different:[" + ss + "][" + test + "]", ss
        .equalsIgnoreCase(test));
  }
  
  /**
   * Test none blob filter
   *
   * @throws Exception if fails
   */
  public void testNoneMethods() throws Exception
  {
    String ss = "This is a string";
    AbstractBlobFilter filter = new NoneBlobFilter();
    System.out.println(filter.getClass());
    String ss1 = filter.encode(ss);
    String test = new String(filter.decode(ss1));
    assertTrue("Strings are different:[" + ss + "][" + test + "]", ss
        .equalsIgnoreCase(test));
  }

}