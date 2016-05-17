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
package org.objectweb.cjdbc.scenario.tools.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * This class defines a KillJava
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class KillJava
{

  private String killTarget;
  private int totalCount;

  /**
   * 
   * Creates a new <code>KillJava</code> object
   * 
   * @param killTarget one of java class name or 'all'
   */
  public KillJava(String killTarget)
  {
    this.killTarget = killTarget;
    totalCount = 0;
  }
  
  /**
   * 
   * Creates a new <code>KillJava</code> object
   * 
   *
   */
  public KillJava()
  {
    killTarget = "org.hsqldb.Server";
    totalCount = 0;
  }

  
  /**
   * 
   * butchery ...
   * 
   * @throws Exception if fails
   */
  public void execute() throws Exception
  {
    //System.out.println("Killing target:" + killTarget);
    String command = "ps -aux";
    Process p = Runtime.getRuntime().exec(command);
    BufferedReader br = new BufferedReader(new InputStreamReader(p
        .getInputStream()));
    String line = "";
    StringTokenizer tokenizer;
    int count = 0;
    while ((line = br.readLine()) != null)
    {
      if (line.indexOf("java") != -1 && line.indexOf(killTarget) != -1
          && line.indexOf("KillJava") == -1)
      {
        //System.out.println(line);
        tokenizer = new StringTokenizer(line);
        tokenizer.nextToken();
        String processNumber = tokenizer.nextToken();
        //System.out.println("Killing process:" + processNumber);
        Runtime.getRuntime().exec("kill -9 " + processNumber);
        count++;
      }
    }
    totalCount += count;
    if (count > 0)
      execute();
    else
      System.out.println("Terminated "+totalCount+" processes");
  }
}