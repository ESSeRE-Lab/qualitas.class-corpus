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

package org.objectweb.cjdbc.scenario.raidb1.driver;

import java.sql.Connection;
import java.util.ArrayList;

import org.objectweb.cjdbc.scenario.templates.Raidb1Template;
import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;
import org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet;
import org.objectweb.cjdbc.scenario.tools.testlet.UpdateTestLet;

/**
 * This class defines a InternationalQueriesScenario
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class InternationalQueriesScenario extends Raidb1Template
{
  /**
   * Test if we can insert and retrieve japanese characters
   * 
   * @throws Exception if fails
   */
  public void testJapaneseCharacters() throws Exception
  {
    String arigatou = "ありがとうございます。。。";
    System.out.println(arigatou);
    Connection con = getCJDBCConnection();
    UpdateTestLet let = new UpdateTestLet(con);
    let.set(AbstractTestLet.UPDATED_COLUMN_VALUE,arigatou);
    let.execute();
    
    ArrayList result = ScenarioUtility.getSingleQueryResult("select * from PRODUCT",con);
    for(int i = 0;i<result.size();i++)
    {
      ArrayList line = (ArrayList)result.get(i);
      assertTrue(((String)line.get(1)).startsWith(arigatou));
    }
  }
}
