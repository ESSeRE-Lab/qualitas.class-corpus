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
 * Initial developer(s): Marc Wick.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.standalone.util;

import org.objectweb.cjdbc.common.util.Strings;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * This class defines a StringsTest
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class StringsTest extends NoTemplate
{

  /**
   * @see org.objectweb.cjdbc.common.util.Strings#replace
   */
  public void testReplace()
  {
    String origString = "test string with no meaning, no meaning at all";
    String replaced = Strings.replace(origString, "no", "important");
    assertEquals("simple test",
        "test string with important meaning, important meaning at all",
        replaced);

    // test empty string
    origString = "test empty string";
    replaced = Strings.replace(origString, "", "do not replace");
    assertEquals("empty string test", origString, replaced);

    // test null
    origString = "test null string";
    replaced = Strings.replace(origString, null, "do not replace");
    assertEquals("null test", origString, replaced);

    // test date string with quotes
    origString = "update test set datestamp = now()";
    replaced = Strings.replace(origString, "now()", "'2004-04-14'");
    assertEquals("date field test", "update test set datestamp = '2004-04-14'",
        replaced);
  }

}