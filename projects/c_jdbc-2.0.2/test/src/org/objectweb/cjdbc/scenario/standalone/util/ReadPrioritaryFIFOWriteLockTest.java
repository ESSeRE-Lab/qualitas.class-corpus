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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.standalone.util;

import java.util.Random;

import org.objectweb.cjdbc.common.util.ReadPrioritaryFIFOWriteLock;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * Reader/Writer Lock test class.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class ReadPrioritaryFIFOWriteLockTest extends NoTemplate
{
  static final long                   TIMEOUT = 1000;        // in ms

  private ReadPrioritaryFIFOWriteLock lock;
  private boolean                     done    = false;       // To check test
  // completion

  private int                         randomTest;
  private Random                      r       = new Random();

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    lock = new ReadPrioritaryFIFOWriteLock();
  }

  /**
   * Acquire 4 read locks, then release them. Acquire 1 write lock and release
   * it. Finally acquire 1 read lock and release it.
   */
  public void testNonBlocking()
  {
    done = false;

    Thread ti[] = new Thread[4];

    // Acquire 4 read locks
    for (int i = 0; i < 4; i++)
    {
      ti[i] = acquireReadDoneTrue();
      ti[i].start();
    }
    try
    {
      for (int i = 0; i < 4; i++)
        ti[i].join(TIMEOUT);
    }
    catch (InterruptedException ignore)
    {
    }
    assertEquals("Timeout read lock in NonBlocking test", done, true);
    // Release read locks
    for (int i = 0; i < 4; i++)
      lock.releaseRead();

    // Acquire write lock
    done = false;
    Thread t = acquireWriteDoneTrue();
    t.start();
    try
    {
      t.join(TIMEOUT);
    }
    catch (InterruptedException ignore)
    {
    }
    assertEquals("Timeout write lock 1 in NonBlocking test", done, true);
    // Release write lock
    lock.releaseWrite();

    // Acquire write lock
    done = false;
    t = acquireWriteDoneTrue();
    t.start();
    try
    {
      t.join(TIMEOUT);
    }
    catch (InterruptedException ignore)
    {
    }
    assertEquals("Timeout write lock 2 in NonBlocking test", done, true);
    // Release write lock
    lock.releaseWrite();

    done = false;
    t = acquireReadDoneTrue();
    t.start();
    try
    {
      t.join(TIMEOUT);
    }
    catch (InterruptedException ignore)
    {
    }
    assertEquals("Timeout read lock 2 in NonBlocking test", done, true);
    // Release write lock
    lock.releaseRead();
  }

  /**
   * Acquire 4 read locks. Acquire 1 write lock and check that the write lock is
   * blocked. Release each read lock in turn and check that the write lock is
   * blocked. Finally check that the write lock has been released.
   */
  public void testWriteAfterRead()
  {
    done = true;
    // t1 acquires 4 read then 1 write
    Thread ti[] = new Thread[4];

    // Acquire 4 read locks
    for (int i = 0; i < 4; i++)
    {
      ti[i] = acquireReadDoneFalse();
      ti[i].start();
    }
    try
    {
      for (int i = 0; i < 4; i++)
        ti[i].join(TIMEOUT);
    }
    catch (InterruptedException ignore)
    {
    }
    assertEquals("Timeout read lock in NonBlocking test", done, false);

    // Acquire 1 read
    Thread t = acquireWriteDoneTrue();
    t.start();
    try
    {
      t.join(TIMEOUT);
    }
    catch (InterruptedException ignore)
    {
    }
    assertEquals("Timeout write lock in NonBlocking test", done, false);

    // Release the read locks 1 by 1
    for (int i = 0; i < 4; i++)
    {
      try
      {
        t.join(TIMEOUT);
      }
      catch (InterruptedException success)
      {
      }
      assertEquals("Write lock should be blocked after (" + i + " release)",
          done, false);
      lock.releaseRead();
    }

    // Now the write lock should be released
    try
    {
      t.join(TIMEOUT);
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e.getMessage());
    }
    assertEquals("Timeout in WriteAfterRead test", done, true);
  }

  /**
   * Acquire 1 write lock. Acquire 4 read locks and check that they are all
   * blocked. Release the write lock and check that all read locks have been
   * released.
   */
  public void testReadAfterWrite()
  {
    done = false;
    // t1 acquires 1 write and then 4 read
    try
    {
      lock.acquireWrite();
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e.getMessage());
    }

    Thread t1 = acquireReadDoneFalse();
    Thread t2 = acquireReadDoneFalse();
    Thread t3 = acquireReadDoneTrue();

    t1.start();
    try
    {
      t1.join(TIMEOUT);
    }
    catch (InterruptedException success)
    {
    }
    assertEquals("Read lock 1 should be blocked", done, false);

    t2.start();
    try
    {
      t2.join(TIMEOUT);
    }
    catch (InterruptedException success)
    {
    }
    assertEquals("Read lock 2 should be blocked", done, false);

    t3.start();
    try
    {
      t3.join(TIMEOUT);
    }
    catch (InterruptedException success)
    {
    }
    assertEquals("Read lock 3 should be blocked", done, false);

    // Release the write lock
    lock.releaseWrite();

    // Now the read locks should be released
    try
    {
      t1.join(TIMEOUT);
      t2.join(TIMEOUT);
      t3.join(TIMEOUT);
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e.getMessage());
    }
    assertEquals("Timeout in ReadAfterWrite test", done, true);
  }

  /**
   * Randomly acquire and release locks. This test should terminate
   */
  public void testRandom()
  {
    int threadNb = 100;
    Thread t[] = new Thread[threadNb];

    randomTest = threadNb;

    for (int i = 0; i < threadNb; i++)
    {
      if (r.nextGaussian() > 0.5)
        t[i] = randomAcquireReleaseWrite();
      else
        t[i] = randomAcquireReleaseRead();
    }

    for (int i = 0; i < threadNb; i++)
      t[i].start();

    try
    {
      Thread.sleep(10 * threadNb);
    }
    catch (InterruptedException ignore)
    {
    }

    try
    {
      for (int i = 0; i < threadNb; i++)
        t[i].join(TIMEOUT);
    }
    catch (InterruptedException ignore)
    {
    }
    assertEquals("Bad random test state (" + randomTest
        + " threads did not finish", randomTest, 0);
    System.out.println();
  }

  private synchronized void randomTestComplete()
  {
    randomTest--;
    //    System.out.println(randomTest);
  }

  /*
   * Private method to call lock acquire methods in a separate thread
   */

  private Thread acquireReadDoneTrue()
  {
    return new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          lock.acquireRead();
        }
        catch (InterruptedException e)
        {
          throw new RuntimeException(e.getMessage());
        }
        done = true;
      }
    });
  }

  private Thread acquireReadDoneFalse()
  {
    return new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          lock.acquireRead();
        }
        catch (InterruptedException e)
        {
          throw new RuntimeException(e.getMessage());
        }
        done = false;
      }
    });
  }

  private Thread acquireWriteDoneTrue()
  {
    return new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          lock.acquireWrite();
        }
        catch (InterruptedException e)
        {
          throw new RuntimeException(e.getMessage());
        }
        done = true;
      }
    });
  }

  private Thread randomAcquireReleaseRead()
  {
    return new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          long timeout = r.nextLong() % TIMEOUT;
          if (timeout < 0)
            timeout = -timeout;
          else if (timeout == 0)
            timeout = TIMEOUT;
          Thread.sleep(timeout);

          lock.acquireRead();
          assertEquals("Acquired read lock but lock not held by a reader", lock
              .isReadLocked(), true);
          assertEquals("Acquired read lock but lock held by a writer", lock
              .isWriteLocked(), false);
          System.out.print("R");
          lock.releaseRead();
          randomTestComplete();
        }
        catch (InterruptedException e)
        {
          throw new RuntimeException(e.getMessage());
        }
      }
    });
  }

  private Thread randomAcquireReleaseWrite()
  {
    return new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          long timeout = r.nextLong() % TIMEOUT;
          if (timeout < 0)
            timeout = -timeout;
          else if (timeout == 0)
            timeout = TIMEOUT;
          Thread.sleep(timeout);

          lock.acquireWrite();
          assertEquals("Acquired write lock but lock not held by a writer",
              lock.isWriteLocked(), true);
          assertEquals("Acquired write lock but lock held by a reader", lock
              .isReadLocked(), false);
          System.out.print("W");
          lock.releaseWrite();
          randomTestComplete();
        }
        catch (InterruptedException e)
        {
          throw new RuntimeException(e.getMessage());
        }
      }
    });
  }
}