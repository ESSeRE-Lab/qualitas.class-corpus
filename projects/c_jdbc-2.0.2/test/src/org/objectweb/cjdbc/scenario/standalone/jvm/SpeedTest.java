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

package org.objectweb.cjdbc.scenario.standalone.jvm;

import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * This class defines a SpeedTest
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class SpeedTest extends NoTemplate
{

  private static boolean isPrime(long i)
  {
    for (long test = 2; test < i; test++)
    {
      if (i % test == 0)
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Test JVM speed by computing primes
   */
  public void testJVMSpeed()
  {
    long start_time = System.currentTimeMillis();

    long n_loops = 50000;
    long n_primes = 0;

    for (long i = 0; i < n_loops; i++)
    {
      if (isPrime(i))
      {
        n_primes++;
      }
    }

    long end_time = System.currentTimeMillis();

    System.out.println(n_primes + " primes found");
    System.out.println("Time taken = " + (end_time - start_time));
  }

  public void testJVMThread()
  {
    int count = 0;
    try
    {
      while (true)
      {
        new WaitThread().start();
        count++;
      }
    }
    catch (RuntimeException re)
    {
      re.printStackTrace();
      System.out.println("Spawned " + count + " threads before failing");
    }
    catch (Error e)
    {
      e.printStackTrace();
      System.out.println("Spawned " + count + " threads before failing");
    }
  }

  class WaitThread extends Thread
  {
    public void run()
    {
      synchronized (this)
      {
        try
        {
          this.wait();
        }
        catch (InterruptedException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
}