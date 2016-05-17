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
 * Initial developer(s): Marc Wick.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.exceptions;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * This class defines a SQLExceptionFactory
 * 
 * @author Marc Wick
 * @version 1.0
 */
public class SQLExceptionFactory
{

  /**
   * creates a new SQLException with the cjdbcMessage
   * 
   * @param sqlEx the original exception
   * @param cjdbcMessage the cjdbc message to use for the new sqlexception
   * @return a new SQLException
   */
  public static SQLException getSQLException(SQLException sqlEx,
      String cjdbcMessage)
  {
    SQLException newException = new SQLException(cjdbcMessage, sqlEx
        .getSQLState(), sqlEx.getErrorCode());
    // TODO: shouldn't we use the new initCause() standard chaining instead ?
    // if we move to a new "SyntheticSQLException" type we will have to do
    // it anyway.
    // See also same issue below.
    newException.setNextException(sqlEx);
    return newException;
  }

  /**
   * creates a new SQLException with the cjdbcMessage, if all exceptions in the
   * list have the same errorcode and sqlstate the returned SQLExcepion will be
   * constructed with this values otherwise with null and 0
   * 
   * @param exceptions list of exceptions
   * @param cjdbcMessage the cjdbc message
   * @return a new SQLException
   */
  public static SQLException getSQLException(List exceptions,
      String cjdbcMessage)
  {
    String sqlState = null;
    int errorCode = 0;
    for (int i = 0; i < exceptions.size(); i++)
    {
      SQLException ex = (SQLException) exceptions.get(i);
      cjdbcMessage += ex.getMessage() + "\n";
      if (i == 0)
      {
        //first exception
        sqlState = ex.getSQLState();
        errorCode = ex.getErrorCode();
      }
      else
      {
        //make sure sqlState is the same for all backends
        if (sqlState != null && !sqlState.equals(ex.getSQLState()))
          sqlState = null;
        //make sure the error code is the same for all backends
        if (errorCode != ex.getErrorCode())
          errorCode = 0;
      }
    }
    SQLException newHead = new SQLException(cjdbcMessage, sqlState, errorCode);
    Iterator exIter = exceptions.iterator();
    
    // TODO: shouldn't we use the new initCause() standard chaining instead ?
    // See more comments above.
    while (exIter.hasNext())
      newHead.setNextException((SQLException)exIter.next());
    
    return newHead;
  }

}
