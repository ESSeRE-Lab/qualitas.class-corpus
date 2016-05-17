/*
 * 00/08/01 @(#)Node.java 1.3 Copyright (c) 2000 Sun Microsystems, Inc. All
 * Rights Reserved. Sun grants you ("Licensee") a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in source and binary
 * code form, provided that i) this copyright notice and license appear on all
 * copies of the software; and ii) Licensee does not utilize the software in a
 * manner which is disparaging to Sun.
 */

package org.objectweb.cjdbc.scenario.standalone.jvm;

public class Node
{
  public Node    m_next    = null;
  private char[] m_payload = null;                                 ;
  static char[]  k_payload = {'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a',
      'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a',
      'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a',
      'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a',
      'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'};

  public Node()
  {
    m_payload = new char[64];
    System.arraycopy(k_payload, 0, m_payload, 0, m_payload.length);
  }
}