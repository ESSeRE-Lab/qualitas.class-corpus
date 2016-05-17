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
 * Initial developer(s): Mathieu Peltier.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.standalone.sql.request;

import org.objectweb.cjdbc.common.sql.SelectRequest;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * <code>AbstractRequestTest</code> test class.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @see org.objectweb.cjdbc.common.sql.AbstractRequest
 */
public class AbstractRequestTest extends NoTemplate
{
    
  /**
   * @see org.objectweb.cjdbc.common.sql.AbstractRequest#trimCarriageReturnAndTabs()
   */
  public void testTrimCarriageReturnAndTabs() throws Exception
  {
    String original = "I have a dream";
    String carriaged1 = new SelectRequest("I"
        + System.getProperty("line.separator") + "have"
        + System.getProperty("line.separator") + "a"
        + System.getProperty("line.separator") + "dream", false, 0, System
        .getProperty("line.separator")).trimCarriageReturnAndTabs();

    String carriaged2 = new SelectRequest("I"
        + System.getProperty("line.separator") + "have"
        + System.getProperty("line.separator") + "a"
        + System.getProperty("line.separator") + "dream", false, 0, System
        .getProperty("line.separator")).trimCarriageReturnAndTabs();

    String carriaged3 = new SelectRequest("I\thave\ta\tdream", false, 0, System
        .getProperty("line.separator")).trimCarriageReturnAndTabs();

    String carriaged4 = new SelectRequest("I"
        + System.getProperty("line.separator") + "have\ta"
        + System.getProperty("line.separator") + "dream", false, 0, System
        .getProperty("line.separator")).trimCarriageReturnAndTabs();

    assertEquals(original, carriaged1);
    assertEquals(original, carriaged2);
    assertEquals(original, carriaged3);
    assertEquals(original, carriaged4);
  }
}
