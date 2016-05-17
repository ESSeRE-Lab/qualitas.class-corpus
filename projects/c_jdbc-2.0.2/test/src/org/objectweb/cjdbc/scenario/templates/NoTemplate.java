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
 *  Initial developer(s): Marc Wick.
 *  Contributor(s): Nicolas Modrzyk
 */

package org.objectweb.cjdbc.scenario.templates;

import java.io.File;

import junit.framework.TestCase;

import org.objectweb.cjdbc.common.log.Trace;

/**
 * This class defines a NoTemplate template, the test does not need anything to
 * run
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modryzk </a>
 * @version 1.0
 */
public class NoTemplate extends TestCase
{
  /**
   * Logger for all scenari
   */
  public static Trace logger = Trace.getLogger("org.objectweb.cjdbc.scenario");
  
  /**
   * The directory where to find text files
   */
  public static final String TEXT_DIR = "/text";

  /**
   * Get the user dir for availability on eclipse and ant test suite
   * 
   * @return the root path for the cjdbc directory
   */
  public static final String getUserDir()
  {
    String userDir = System.getProperty("cjdbc.dir");
    return userDir;
  }

  /**
   * Get the path of this text file
   * 
   * @param textFile the textfile to find the path
   * @return Complete <code>URL</code> in string format for the path
   */
  public static final String getTextPath(String textFile)
  {
    File file = new File((new String("")).getClass().getResource(
        TEXT_DIR + File.separator + textFile).getFile());
    return file.getAbsolutePath();
  }  
}