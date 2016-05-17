/*
 * 00/08/01 @(#)Barrier.java 1.3 Copyright (c) 2000 Sun Microsystems, Inc. All
 * Rights Reserved. Sun grants you ("Licensee") a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in source and binary
 * code form, provided that i) this copyright notice and license appear on all
 * copies of the software; and ii) Licensee does not utilize the software in a
 * manner which is disparaging to Sun.
 */

package org.objectweb.cjdbc.scenario.standalone.jvm;

public class Barrier
{

  private int m_numThreads   = 0;
  private int m_currentCount = 0;

  public Barrier(int numThreads)
  {
    m_numThreads = numThreads;
  }

  public void waitForGo()
  {
    synchronized (this)
    {
      m_currentCount++;
      if (m_currentCount < m_numThreads)
      {
        try
        {
          wait();
        }
        catch (InterruptedException exp)
        {
          System.out.println("Barrier caught interrupted exception!");
        }
      }
      else
        notifyAll();
    }
  }

}