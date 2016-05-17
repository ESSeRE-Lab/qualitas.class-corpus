/*
 * 00/08/01 @(#)HeapThread.java 1.3 Copyright (c) 2000 Sun Microsystems, Inc.
 * All Rights Reserved. Sun grants you ("Licensee") a non-exclusive, royalty
 * free, license to use, modify and redistribute this software in source and
 * binary code form, provided that i) this copyright notice and license appear
 * on all copies of the software; and ii) Licensee does not utilize the software
 * in a manner which is disparaging to Sun.
 */

package org.objectweb.cjdbc.scenario.standalone.jvm;

public class HeapThread extends Thread
{

  private int     m_id              = 0;
  private int     m_numThreads      = 0;
  private int[]   m_data            = null;
  private int     m_numDataPoints   = 0;
  private int     m_numIterations   = 0;
  private int     m_numNodesToAlloc = 0;
  private int     m_heapCycles      = 0;
  private int     m_cpuCycles       = 0;
  private Barrier m_goFlag          = null;
  private Node    m_firstNode       = null;

  public HeapThread(int id, int numThreads, int[] data, int numDataPoints,
      int numIterations, int numNodesToAlloc, int heapCycles, int cpuCycles,
      Barrier goFlag)
  {
    m_id = id;
    m_numThreads = numThreads;
    m_data = data;
    m_numDataPoints = numDataPoints;
    m_numIterations = numIterations;
    m_numNodesToAlloc = numNodesToAlloc;
    m_heapCycles = heapCycles;
    m_cpuCycles = cpuCycles;
    m_goFlag = goFlag;
    m_firstNode = new Node();
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {

    // Wait for all the threads are ready to go
    m_goFlag.waitForGo();

    for (int iter = m_id; iter < m_numIterations; iter += m_numThreads)
    {
      for (int heapIter = 0; heapIter < m_heapCycles; heapIter++)
      {
        doHeapBoundStuff(m_numNodesToAlloc);
      }
      for (int cpuIter = 0; cpuIter < m_cpuCycles; cpuIter++)
      {
        doCPUBoundStuff(m_data, m_numDataPoints);
      }
    }
  }

  void doHeapBoundStuff(int numNodesToAlloc)
  {
    if (m_firstNode.m_next == null)
    {
      for (Node node = m_firstNode; numNodesToAlloc > 0; numNodesToAlloc--)
      {
        node.m_next = new Node();
        node = node.m_next;
      }
    }
    else
    {
      while (m_firstNode.m_next != null)
      {
        m_firstNode.m_next = m_firstNode.m_next.m_next;
      }
    }
  }

  /*
   * double sqrtByNewtonsMethod(double x) { double lastEst = x; double est = 0;
   * double epsilon = 1e-10; if (x == 0) est = 0; else { for (;;) { est =
   * (lastEst*lastEst + x)/(2*lastEst); if (-epsilon < (est-lastEst) && (est -
   * lastEst) < epsilon) { break; } else lastEst = est; } } return est; }
   */

  void doCPUBoundStuff(int[] data, int numDataPoints)
  {
    int i = 0;
    double avg = 0;
    double var = 0;

    data[0] = m_id;
    data[1] = m_id + 1;

    for (i = 2; i < numDataPoints; i++)
    {
      data[i] = data[i - 1] + data[i - 2];
    }

    for (avg = 0, i = 0; i < numDataPoints; i++)
    {
      avg += numDataPoints;
    }
    avg /= numDataPoints;

    for (var = 0, i = 0; i < numDataPoints; i++)
    {
      double diff = data[i] - avg;
      var += diff * diff;
    }
    var /= (numDataPoints - 1);
  }
}

