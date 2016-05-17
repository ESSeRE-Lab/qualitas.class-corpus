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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Jean-Bernard van Zuylen.
 */

package java.sql;

/**
 * This class is just a fake to compile the C-JDBC driver with JDK 1.3. The
 * Savepoint interface is part of JDBC 3.0. Full JDBC 3.0 compliance is required
 * to compile with JDK 1.4.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:jbvanzuylen@transwide.com">Jean-Bernard van Zuylen
 *         </a>
 * @version 1.0
 */
public interface Savepoint
{
  /**
   * Retrieves the generated ID for the savepoint that this Savepoint object
   * represents.
   * 
   * @return the numeric ID of this savepoint
   * @throws SQLException if this is a named savepoint
   * @since 1.4
   */
  int getSavepointId() throws SQLException;

  /**
   * Retrieves the name of the savepoint that this Savepoint object represents.
   * 
   * @return the name of this savepoint
   * @throws SQLException if this is an un-named savepoint
   * @since 1.4
   */
  String getSavepointName() throws SQLException;
}
