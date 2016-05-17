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
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.controller.requestmanager;

import java.sql.SQLException;

import org.objectweb.cjdbc.common.sql.AbstractRequest;
import org.objectweb.cjdbc.common.sql.schema.DatabaseSchema;

/**
 * This thread is used to process request parsing in background.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class ParserThread extends Thread
{
  private boolean isCaseSensitive;
  private AbstractRequest request;
  private DatabaseSchema dbs;
  private int granularity;

  /**
   * Creates a new ParserThread
   * 
   * @param request the request to parse
   * @param dbs the database schema
   * @param granularity the parsing granularity to use
   * @param isCaseSensitive true if parsing is case sensitive
   */
  public ParserThread(
    AbstractRequest request,
    DatabaseSchema dbs,
    int granularity,
    boolean isCaseSensitive)
  {
    this.request = request;
    this.dbs = dbs;
    this.granularity = granularity;
    this.isCaseSensitive = isCaseSensitive;
    start();
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      if (!request.isParsed())
        request.parse(dbs, granularity, isCaseSensitive);
    }
    catch (SQLException e)
    {
      System.err.println("Error while parsing request (" + e + ")");
    }
  }

}
