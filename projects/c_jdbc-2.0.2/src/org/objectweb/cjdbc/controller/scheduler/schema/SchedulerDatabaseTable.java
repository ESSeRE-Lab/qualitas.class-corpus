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
 * Contributor(s): _________________________.
 */

package org.objectweb.cjdbc.controller.scheduler.schema;

import java.io.Serializable;

import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;

/**
 * A <code>CacheDatabaseTable</code> represents a database table and its
 * associated cache entries. It has an array of <code>CacheDatabaseColumn</code>
 * objects.
 * 
 * <p>
 * Keep it mind that <code>ArrayList</code> is not synchronized...
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class SchedulerDatabaseTable implements Serializable
{
  /** Database table name. */
  private String name;

  private TransactionExclusiveLock lock = new TransactionExclusiveLock();

  /**
   * Creates a new <code>CacheDatabaseTable</code> instance.
   * 
   * @param databaseTable the database table
   */
  public SchedulerDatabaseTable(DatabaseTable databaseTable)
  {
    // Clone the name and the columns
    name = databaseTable.getName();
  }

  /**
   * Gets the name of the table.
   * 
   * @return the table name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the lock for this table.
   * 
   * @return a <code>TransactionExclusiveLock</code> instance
   * @see TransactionExclusiveLock
   */
  public TransactionExclusiveLock getLock()
  {
    return lock;
  }

  /**
   * Two <code>CacheDatabaseColumn</code> are equals if they have the same
   * name and the same columns.
   * 
   * @param other the object to compare with
   * @return true if the 2 objects are the same
   */
  public boolean equals(Object other)
  {
    if ((other == null) || !(other instanceof SchedulerDatabaseTable))
      return false;
    else
      return name.equals(((SchedulerDatabaseTable) other).getName());
  }

  /**
   * Returns information about the database table and its columns.
   * 
   * @param longFormat <code>true</code> for a long format, <code>false</code>
   *          for a short summary
   * @return a <code>String</code> value
   */
  public String getInformation(boolean longFormat)
  {
    return "Table " + name + ": ";
  }
}
