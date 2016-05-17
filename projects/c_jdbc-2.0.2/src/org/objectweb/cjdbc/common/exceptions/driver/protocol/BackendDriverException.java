/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks
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
 * Initial developer(s): Marc Herbert
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.exceptions.driver.protocol;

import java.io.IOException;
import java.sql.SQLException;

import org.objectweb.cjdbc.common.stream.CJDBCInputStream;

/**
 * This class is an SQLException (typically from backend) made serializable.
 * 
 * @author <a href="mailto:Marc.Herbert@emicnetworks.com">Marc Herbert</a>
 * @version 1.0
 */
public class BackendDriverException
    extends SerializableException
{
  /**
   * @see SerializableException#SerializableException(CJDBCInputStream)
   */
  public BackendDriverException(CJDBCInputStream in) throws IOException
  {
    super(in);
  }

  /**
   * Converts a chain of Throwables to a new chain of SerializableException
   * starting with a <code>BackendDriverException</code>. The returned chain
   * has the same length. We don't use super's method but re-implement it since
   * we want to also convert SQLException old-style chaining into a new style
   * chain. "SyntheticSQLException-s" from
   * {@link org.objectweb.cjdbc.common.exceptions.SQLExceptionFactory} also
   * currently use old style chaining (with setNextException).
   * 
   * @param start head of chain to convert.
   * @see SerializableException#SerializableException(Throwable)
   */
  public BackendDriverException(Throwable start)
  {
    super(start.getMessage(), convertNext(start)); // recursion here
    convertStackTrace(start);

    if (start instanceof SQLException) // hopefully, else why are we here?
    {
      SQLException sqlE = (SQLException) start;
      setSQLState(sqlE.getSQLState());
      setErrorCode(sqlE.getErrorCode());
    }
  }

  /**
   * Get the first cause found (new or old style), and convert it to a new
   * BackendDriverException object (which is Serializable)
   */

  private static SerializableException convertNext(Throwable regularEx)
  {
    /*
     * If we find that the new standard 1.4 chain is used, then we don't even
     * look at the old SQLException chain.
     */
    /*
     * We could also <em>not</em> lose this information by: adding another
     * separated chain to this class, serialize both chains, and convert
     * everything back to SQLExceptions on the driver side so both chains can
     * separately be offered to the JDBC client...
     */
    Throwable newStyleCause = regularEx.getCause();
    if (null != newStyleCause)
      return new BackendDriverException(newStyleCause);

    // check legacy style chaining
    if (regularEx instanceof SQLException)
    {
      SQLException nextE = ((SQLException) regularEx).getNextException();
      if (null != nextE)
        return new BackendDriverException(nextE);
    }

    // found no more link, stop condition
    return null;

  }
}