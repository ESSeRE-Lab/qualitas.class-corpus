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
 * Contributor(s): Sara Bouchenak.
 */

package org.objectweb.cjdbc.common.sql;

/**
 * Defines SQL queries parsing granularities.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @author <a href="mailto:Sara.Bouchenak@epfl.ch">Sara Bouchenak</a>
 * @version 1.0
 */
public class ParsingGranularities
{
  /** The request is not parsed. */
  public static final int NO_PARSING = 0;

  /**
   * Table granularity. Only table dependencies are computed.
   */
  public static final int TABLE = 1;

  /**
   * Column granularity. Column dependencies are computed (both select and
   * where clauses).
   */
  public static final int COLUMN = 2;

  /**
   * Column granularity with <code>UNIQUE</code> queries.
   * 
   * <p>
   * Same as <code>COLUMN</code> except that <code>UNIQUE</code> queries
   * that select a single row based on a key are flagged <code>UNIQUE</code>
   * (and should not be invalidated on <code>INSERTs</code>).
   */
  public static final int COLUMN_UNIQUE = 3;

  /**
   * Returns the granularity value in a <code>String</code> form.
   * 
   * @param granularity a granularity value
   * @return the <code>String</code> form of the granularity
   */
  public static String getInformation(int granularity)
  {
    switch (granularity)
    {
      case NO_PARSING :
        return "NO_PARSING";
      case TABLE :
        return "TABLE";
      case COLUMN :
        return "COLUMN";
      case COLUMN_UNIQUE :
        return "COLUMN_UNIQUE";
      default :
        return "Illegal parsing granularity";
    }
  }
}
