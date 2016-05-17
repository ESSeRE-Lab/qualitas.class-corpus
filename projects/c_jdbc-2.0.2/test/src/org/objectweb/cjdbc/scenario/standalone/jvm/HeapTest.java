/*
 * 00/08/01 @(#)HeapTest.java 1.3 Copyright (c) 2000 Sun Microsystems, Inc. All
 * Rights Reserved. Sun grants you ("Licensee") a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in source and binary
 * code form, provided that i) this copyright notice and license appear on all
 * copies of the software; and ii) Licensee does not utilize the software in a
 * manner which is disparaging to Sun.
 */

package org.objectweb.cjdbc.scenario.standalone.jvm;

import java.io.FileOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

public class HeapTest extends TestCase
{

  static final int NUM_ITERATIONS     = 500;
  static final int NUM_NODES_TO_ALLOC = 3000;
  static final int NUM_DATA_POINTS    = 50000;
  static final int MAX_NUMBER_THREAD  = 5;
  static final int TOTAL_CYCLES       = 2;

  public void testJVM()
  {
    main(new String[]{"" + MAX_NUMBER_THREAD, "" + TOTAL_CYCLES});
  }

  public static void main(String[] args)
  {

    int k_numIterations = NUM_ITERATIONS;
    int k_numNodesToAlloc = NUM_NODES_TO_ALLOC;
    int k_numDataPoints = NUM_DATA_POINTS;

    int maxNumThreads = 0;
    int numThreads = 0;
    int totalCycles = 0;
    int heapCycles = 0;
    int cpuCycles = 0;
    int i = 0;

    try
    {

      if (args.length < 2)
        usage();
      PrintStream logFile = null;
      try
      {
        logFile = new PrintStream(new FileOutputStream(args[2]));
      }
      catch (Exception e)
      {
        System.out
            .println("Unable to open log file. Printing to System.out...");
        logFile = System.out;
      }

      try
      {
        maxNumThreads = Integer.parseInt(args[0]);
        totalCycles = Integer.parseInt(args[1]);
      }
      catch (Exception e)
      {
      }

      if (maxNumThreads == 0 || totalCycles == 0)
        usage();
      else
      {
        logFile.println("\nMax # threads =      " + maxNumThreads + "\n"
            + "Total (heap + CPU) cycles = " + totalCycles + "\n\n");
      }

      HeapThread[] threads = new HeapThread[maxNumThreads];
      logFile.print("# Threads");
      for (cpuCycles = 0, heapCycles = totalCycles; cpuCycles <= totalCycles; cpuCycles++, heapCycles--)
      {
        logFile.print("\t" + heapCycles + " Heap, " + cpuCycles + " CPU");
      }
      logFile.flush();

      for (numThreads = 1; numThreads <= maxNumThreads; numThreads++)
      {
        logFile.print("\n\n" + numThreads);
        for (cpuCycles = 0, heapCycles = totalCycles; cpuCycles <= totalCycles; cpuCycles++, heapCycles--)
        {

          Barrier goFlag = new Barrier(numThreads);
          for (i = 0; i < numThreads; i++)
          {
            threads[i] = new HeapThread(i, numThreads,
                new int[k_numDataPoints], k_numDataPoints, k_numIterations,
                k_numNodesToAlloc, heapCycles, cpuCycles, goFlag);
          }

          long elapsedTime = System.currentTimeMillis();
          for (i = 0; i < numThreads; i++)
          {
            threads[i].start();
          }

          for (i = 0; i < numThreads; i++)
          {
            threads[i].join();
          }
          elapsedTime = System.currentTimeMillis() - elapsedTime;
          logFile.print("\t" + elapsedTime);
        }
      }
      logFile.flush();
    }
    catch (Exception e)
    {
      System.out.println("Caught exception!!");
      e.printStackTrace();
    }

  }

  public static void usage()
  {
    System.out
        .println("java HeapTest <numThreads> <num of (CPU + Heap) cycles>");
    System.exit(1);
  }
}

