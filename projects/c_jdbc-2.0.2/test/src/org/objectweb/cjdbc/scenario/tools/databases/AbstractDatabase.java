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
 * Initial developer(s): Mathieu Peltier.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.tools.databases;

import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;

/**
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 */
public abstract class AbstractDatabase
{
  /** Database schema. */
  protected DatabaseSchema schema;

  /**
   * Creates a new <code>AbstractDatabase</code> instance.
   */
  public AbstractDatabase()
  {
    schema = new DatabaseSchema();
  }

  /**
   * Gets the database schema.
   * 
   * @return a <code>DatabaseSchema</code> instance.
   */
  public DatabaseSchema getSchema()
  {
    return schema;
  }
}
