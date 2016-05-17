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
 * Contributor(s): Julie Marguerite.
 */

package org.objectweb.cjdbc.controller.recoverylog;

import org.objectweb.cjdbc.controller.loadbalancer.tasks.AbstractTask;

/**
 * Recovery task containing an <code>AbstractTask</code> and the id of the
 * task in the recovery log.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite </a>
 * @version 1.0
 */
public class RecoveryTask
{
  private long         id;
  private long         tid;
  private AbstractTask task;

  /**
   * Constructs a new <code>RecoveryTask</code> instance.
   * 
   * @param tid transaction id
   * @param id task id in the recovery log
   * @param task task to be executed
   */
  public RecoveryTask(long tid, long id, AbstractTask task)
  {
    this.id = id;
    this.tid = tid;
    this.task = task;
  }

  /**
   * Returns the tid value.
   * 
   * @return Returns the tid.
   */
  public long getTid()
  {
    return tid;
  }

  /**
   * Returns the id.
   * 
   * @return int
   */
  public long getId()
  {
    return id;
  }

  /**
   * Returns the task.
   * 
   * @return AbstractTask
   */
  public AbstractTask getTask()
  {
    return task;
  }

  /**
   * Sets the id.
   * 
   * @param id the id to set
   */
  public void setId(long id)
  {
    this.id = id;
  }

  /**
   * Sets the task.
   * 
   * @param task the task to set
   */
  public void setTask(AbstractTask task)
  {
    this.task = task;
  }
}
