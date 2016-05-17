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
 * Contributor(s): _________________________.
 */
package org.objectweb.cjdbc.scenario.standalone.i18n;

import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.util.TranslationCheck;

/**
 * Makes use of the <code>TranslationCheck</code> class and display
 * information
 */
public class TranslationTest extends NoTemplate
{

  /**
   * Launch test case
   * 
   * @param args not needed
   */
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TranslationTest.class);
  }

  /**
   * No test but display the state of the translation work
   *  
   */
  public void testTranslationKeys()
  {
    TranslationCheck tc = new TranslationCheck();
    tc.displayTranslationState();
  }
}