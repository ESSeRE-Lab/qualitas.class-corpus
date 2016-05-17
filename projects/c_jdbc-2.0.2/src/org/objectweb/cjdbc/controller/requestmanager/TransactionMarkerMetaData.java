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

package org.objectweb.cjdbc.controller.requestmanager;

/**
 * This class carry transaction marker (begin/commit/rollback) metadata.
 * <p>
 * Metadata include a transaction id, a login and a timeout.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class TransactionMarkerMetaData
{
  private long transactionId;
  private long timeout;
  private String login;

  /**
   * Creates a new <code>TransactionMarkerMetaData</code>.
   * 
   * @param transactionId the transaction identifier.
   * @param timeout the transaction timeout in seconds.
   * @param login the user login.
   */
  public TransactionMarkerMetaData(
    long transactionId,
    long timeout,
    String login)
  {
    this.transactionId = transactionId;
    this.timeout = timeout;
    this.login = login;
  }

  /**
   * Returns the login.
   * 
   * @return String
   */
  public String getLogin()
  {
    return login;
  }

  /**
   * Returns the timeout.
   * 
   * @return long
   */
  public long getTimeout()
  {
    return timeout;
  }

  /**
   * Returns the transactionId.
   * 
   * @return int
   */
  public long getTransactionId()
  {
    return transactionId;
  }

  /**
   * Sets the login.
   * 
   * @param login the login to set.
   */
  public void setLogin(String login)
  {
    this.login = login;
  }

  /**
   * Sets the timeout.
   * 
   * @param timeout the timeout to set.
   */
  public void setTimeout(long timeout)
  {
    this.timeout = timeout;
  }

  /**
   * Sets the transactionId.
   * 
   * @param transactionId the transactionId to set
   */
  public void setTransactionId(long transactionId)
  {
    this.transactionId = transactionId;
  }
}
