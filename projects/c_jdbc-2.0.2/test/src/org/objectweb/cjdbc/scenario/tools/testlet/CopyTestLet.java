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

import org.objectweb.cjdbc.scenario.tools.ScenarioUtility;

/**
 * This class defines a CopyTestLet
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class CopyTestLet extends AbstractTestLet
{

  /**
   * Creates a new <code>CopyTestLet</code> object
   */
  public CopyTestLet()
  {
    super();
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet#execute()
   */
  public void execute() throws Exception
  {
    String storeFile = (String)config.get(FILE_NAME);
    File f1 = new File(storeFile);
    if(!f1.exists())
      f1 = new File(getClass().getResource(storeFile).getFile());
    if(!f1.exists())
      throw new TestLetException("Cannot find file:"+storeFile);
    
    boolean ok = true;
    
    File f2 = new File(f1.getAbsolutePath() + ".copy");
    ScenarioUtility.writeBinary(ScenarioUtility.readBinary(f1), f2);
    if (f1.length() != f2.length())
      ok = false;
    f2.delete();
    if(!ok)
      throw new TestLetException("CopyTestLet failed with:"+storeFile);
  }

}
