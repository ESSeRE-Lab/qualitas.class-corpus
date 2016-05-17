/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.tools;

/**
 * This class defines a GenerateTrace
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class GenerateTrace
{

  /**
   * Generate a request player trace
   * 
   * @param args none
   */
  public static void main(String[] args)
  {
    for (int i = 1; i < 1000000; i++)
    {
      System.out.println("00:00:00,000 myDB B " + i);
      System.out.println("00:00:00,000 myDB W " + i
          + " INSERT INTO foo VALUES (" + (i * 10) + ")");
      System.out.println("00:00:00,000 myDB W " + i
          + " INSERT INTO foo VALUES (" + (i * 10 + 1) + ")");
      System.out.println("00:00:00,000 myDB W " + i
          + " INSERT INTO foo VALUES (" + (i * 10 + 2) + ")");
      System.out.println("00:00:00,000 myDB W " + i
          + " INSERT INTO foo VALUES (" + (i * 10 + 3) + ")");
      System.out.println("00:00:00,000 myDB W " + i
          + " INSERT INTO foo VALUES (" + (i * 10 + 4) + ")");
      System.out.println("00:00:00,000 myDB W " + i
          + " INSERT INTO foo VALUES (" + (i * 10 + 5) + ")");
      System.out.println("00:00:00,000 myDB W " + i
          + " INSERT INTO foo VALUES (" + (i * 10 + 6) + ")");
      System.out.println("00:00:00,000 myDB W " + i
          + " INSERT INTO foo VALUES (" + (i * 10 + 7) + ")");
      System.out.println("00:00:00,000 myDB W " + i
          + " INSERT INTO foo VALUES (" + (i * 10 + 8) + ")");
      System.out.println("00:00:00,000 myDB W " + i
          + " INSERT INTO foo VALUES (" + (i * 10 + 9) + ")");
      System.out.println("00:00:00,000 myDB C " + i);
    }
  }
}
