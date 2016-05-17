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

package org.objectweb.cjdbc.scenario.tools.testlet;

/**
 * This class defines a TestLetException. This exception should be thrown when
 * some parameters are missing in the testlets
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class TestLetException extends Exception
{

  /**
   * Creates a new <code>TestLetException</code> object
   */
  public TestLetException()
  {
    super();
  }

  /**
   * Creates a new <code>TestLetException</code> object
   * 
   * @param message message of the exception
   */
  public TestLetException(String message)
  {
    super(message);
  }

  /**
   * Creates a new <code>TestLetException</code> object
   * 
   * @param message  message of the exception
   * @param cause the cause of the exception
   */
  public TestLetException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /**
   * Creates a new <code>TestLetException</code> object
   * 
   * @param cause the cause of the exception
   */
  public TestLetException(Throwable cause)
  {
    super(cause);
  }
}