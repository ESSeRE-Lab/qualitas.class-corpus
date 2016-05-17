/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
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
 * Initial developer(s): Emmanuel Checchet.
 * Contributor(s): Olivier Fambon.
 */

package org.objectweb.cjdbc.controller.recoverylog.events;

import java.io.Serializable;

/**
 * This class defines a recovery log entry that needs to be stored in the
 * recovery database. This is also sent over the wire between controllers, when
 * copying recovery log entries to a remote controller (copyLogFromCheckpoint).
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class LogEntry implements Serializable
{
  private static final long serialVersionUID = 1363084035201225164L;

  private long              tid;
  private String            query;
  private String            login;
  private long              id;
  private boolean           escapeProcessing;

  /**
   * Create a log object.
   * 
   * @param id unique request id
   * @param login login used for this request
   * @param query query to log
   * @param tid transaction id of this request
   * @param escapeProcessing true if escape processing must be done
   */
  public LogEntry(long id, String login, String query, long tid,
      boolean escapeProcessing)
  {
    this.id = id;
    this.login = login;
    this.query = query;
    this.tid = tid;
    this.escapeProcessing = escapeProcessing;
  }

  /**
   * @return the request id
   */
  public long getId()
  {
    return id;
  }

  /**
   * @return the login used for this request
   */
  public String getLogin()
  {
    return login;
  }

  /**
   * @return the request itself
   */
  public String getQuery()
  {
    return query;
  }

  /**
   * @return the transaction id
   */
  public long getTid()
  {
    return tid;
  }

  /**
   * @return true if escape processing is needed
   */
  public boolean getEscapeProcessing()
  {
    return escapeProcessing;
  }

}