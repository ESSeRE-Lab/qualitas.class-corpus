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

package org.objectweb.cjdbc.scenario.tools.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * This class defines a GenerateDatabase
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class GenerateDatabase
{

  static final int START_INDEX      = 3500;
  static final int NUMBER_OF_TABLES = 3000;

  public static void main(String[] args) throws Exception
  {
    File f = new File("database-generated-2.template");
    System.out.println(f.getAbsolutePath());
    BufferedWriter writer = new BufferedWriter(new FileWriter(f));

    int finalIndex = START_INDEX + NUMBER_OF_TABLES;
    for (int i = START_INDEX; i < finalIndex; i++)
    {
      writer.write("CREATE TABLE BLOB" + i + " (id INTEGER,blob VARCHAR)");
      writer.write(System.getProperty("line.separator"));
      writer.write("INSERT INTO BLOB" + i + " VALUES(0,'Laura')");
      writer.write(System.getProperty("line.separator"));
    }
    writer.close();
  }
}