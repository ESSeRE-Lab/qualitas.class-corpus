/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Initial developer(s): Jean-Bernard van Zuylen.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.driver;

import java.sql.SQLException;

/**
 * This class defines a Savepoint
 * 
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public class Savepoint implements java.sql.Savepoint
{
  private int savepointId;
  
  private String savepointName;
  
  /**
   * Creates a new un-named <code>Savepoint</code> object
   * 
   * @param savepointId the generated ID for this savepoint
   */
  public Savepoint(int savepointId)
  {
    this.savepointId = savepointId;
  }
  
  /**
   * Creates a new named <code>Savepoint</code> object
   * 
   * @param savepointName the name of the savepoint
   */
  public Savepoint(String savepointName)
  {
    this.savepointName = savepointName;
  }
  
  /**
   * @see java.sql.Savepoint#getSavepointId()
   */
  public int getSavepointId() throws SQLException
  {
    if (this.savepointName != null)
      throw new SQLException("This is a named savepoint");
    
    return this.savepointId;
  }
  
  /**
   * @see java.sql.Savepoint#getSavepointName()
   */
  public String getSavepointName() throws SQLException
  {
    if (this.savepointName == null)
      throw new SQLException("This is an unnamed savepoint");
    
    return this.savepointName;
  }
}
