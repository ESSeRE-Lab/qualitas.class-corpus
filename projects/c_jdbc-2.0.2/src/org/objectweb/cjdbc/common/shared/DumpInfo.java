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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.shared;

import java.io.Serializable;

/**
 * This class defines a DumpInfo which carries dump metadata information that is
 * mapped on a row in the dump table of the recovery log.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class DumpInfo implements Serializable
{
  private static final long serialVersionUID = -5627995243952765938L;

  private String            dumpName;
  private String            dumpDate;
  private String            dumpPath;
  private String            dumpFormat;
  private String            checkpointName;
  private String            backendName;
  private String            tables;

  /**
   * Creates a new <code>DumpInfo</code> object
   * 
   * @param dumpName the dump logical name
   * @param dumpDate the date at which the dump was started
   * @param dumpPath the path where the dump can be found
   * @param dumpFormat the format of the dump
   * @param checkpointName the checkpoint name associated to this dump
   * @param backendName the name of the backend that was dumped
   * @param tables the list of tables contained in the dump ('*' means all
   *          tables)
   */
  //FIXME dumpDate should be a java.util.Date
  public DumpInfo(String dumpName, String dumpDate, String dumpPath,
      String dumpFormat, String checkpointName, String backendName,
      String tables)
  {
    this.dumpName = dumpName;
    this.dumpDate = dumpDate;
    this.dumpPath = dumpPath;
    this.dumpFormat = dumpFormat;
    this.checkpointName = checkpointName;
    this.backendName = backendName;
    this.tables = tables;
  }

  /**
   * Returns the backendName value.
   * 
   * @return Returns the backendName.
   */
  public String getBackendName()
  {
    return backendName;
  }

  /**
   * Sets the backendName value.
   * 
   * @param backendName The backendName to set.
   */
  public void setBackendName(String backendName)
  {
    this.backendName = backendName;
  }

  /**
   * Returns the checkpointName value.
   * 
   * @return Returns the checkpointName.
   */
  public String getCheckpointName()
  {
    return checkpointName;
  }

  /**
   * Sets the checkpointName value.
   * 
   * @param checkpointName The checkpointName to set.
   */
  public void setCheckpointName(String checkpointName)
  {
    this.checkpointName = checkpointName;
  }

  /**
   * Returns the dumpDate value.
   * 
   * @return Returns the dumpDate.
   */
  public String getDumpDate()
  {
    return dumpDate;
  }

  /**
   * Sets the dumpDate value.
   * 
   * @param dumpDate The dumpDate to set.
   */
  public void setDumpDate(String dumpDate)
  {
    this.dumpDate = dumpDate;
  }

  /**
   * Returns the dumpName value.
   * 
   * @return Returns the dumpName.
   */
  public String getDumpName()
  {
    return dumpName;
  }

  /**
   * Sets the dumpName value.
   * 
   * @param dumpName The dumpName to set.
   */
  public void setDumpName(String dumpName)
  {
    this.dumpName = dumpName;
  }

  /**
   * Returns the dumpPath value.
   * 
   * @return Returns the dumpPath.
   */
  public String getDumpPath()
  {
    return dumpPath;
  }

  /**
   * Sets the dumpPath value.
   * 
   * @param dumpPath The dumpPath to set.
   */
  public void setDumpPath(String dumpPath)
  {
    this.dumpPath = dumpPath;
  }

  /**
   * Returns the dumpFormat value.
   * 
   * @return Returns the dumpFormat.
   */
  public String getDumpFormat()
  {
    return dumpFormat;
  }

  /**
   * Sets the dumpFormat value.
   * 
   * @param dumpFormat The dumpFormat to set.
   */
  public void setDumpFormat(String dumpFormat)
  {
    this.dumpFormat = dumpFormat;
  }

  /**
   * Returns the tables value.
   * 
   * @return Returns the tables.
   */
  public String getTables()
  {
    return tables;
  }

  /**
   * Sets the tables value.
   * 
   * @param tables The tables to set.
   */
  public void setTables(String tables)
  {
    this.tables = tables;
  }

}
