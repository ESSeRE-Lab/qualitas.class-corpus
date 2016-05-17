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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Dylan Hansen.
 */

package org.objectweb.cjdbc.controller.backup;

import java.io.File;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.exceptions.BackupException;
import org.objectweb.cjdbc.common.log.Trace;

/**
 * This abstract class provides base methods for PostgreSQL backupers.
 * 
 * @author <a href="mailto:dhansen@h2st.com">Dylan Hansen</a>
 * @version 1.1
 */
public abstract class AbstractPostgreSQLBackuper implements Backuper
{
  // Logger
  static Trace    logger        = Trace.getLogger(AbstractPostgreSQLBackuper.class
                                    .getName());

  // HashMap containing options passed to Backuper
  protected HashMap optionsMap    = new HashMap();

  // String representation of the options
  protected String  optionsString = null;

  /**
   * Creates a new <code>AbstractPostgreSQLBackuper</code> object
   * 
   * @author Dylan Hansen
   */
  public AbstractPostgreSQLBackuper()
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#getOptions()
   * @author Dylan Hansen
   */
  public String getOptions()
  {
    return optionsString;
  }
  
  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#setOptions(java.lang.String)
   * @author Dylan Hansen
   */
  public void setOptions(String options)
  {
    if (options != null)
    {    
      StringTokenizer strTok = new StringTokenizer(options, ",");
      String option = null;
      String name = null;
      String value = null;

      // Parse the string of options, add them to the HashMap
      while (strTok.hasMoreTokens())
      {
        option = strTok.nextToken();
        name = option.substring(0, option.indexOf("="));
        value = option.substring(option.indexOf("=") + 1, option.length());
        optionsMap.put(name, value);
      }
 
      optionsString = options;
    }  
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#deleteDump(java.lang.String,
   *      java.lang.String)
   */
  public void deleteDump(String path, String dumpName) throws BackupException
  {
    File toRemove = new File(getDumpPhysicalPath(path, dumpName));
    if (logger.isDebugEnabled())
      logger.debug("Deleting compressed dump " + toRemove);
    toRemove.delete();
  }

  /**
   * Get the dump physical path from its logical name
   * 
   * @param path the path where the dump is stored
   * @param dumpName dump logical name
   * @return path to dump file
   */
  protected String getDumpPhysicalPath(String path, String dumpName)
  {
    String fullPath = null;

    if (path.endsWith(File.separator))
      fullPath = path + dumpName;
    else
      fullPath = path + File.separator + dumpName;

    return fullPath;
  }

  /**
   * Get the host of a given PostgreSQL connection string
   * 
   * @param url The full PostgreSQL URL
   * @return Host of the URL
   * @author Dylan Hansen
   */
  protected String getHostFromURL(String url)
  {
    // Strip 'jdbc:postgresql://'
    // 18 = "jdbc:postgresql://".length()
    return url.substring(18, url.lastIndexOf(":"));
  }

  /**
   * Get the port of a given PostgreSQL connection string
   * 
   * @param url The full PostgreSQL URL
   * @return Port of the URL
   * @author Dylan Hansen
   */
  protected String getPortFromURL(String url)
  {
    // Get port number after the last ":" but before the last "/"
    return url.substring(url.lastIndexOf(":") + 1, url.lastIndexOf("/"));
  }

  /**
   * Get the database name of a given PostgreSQL connection string
   * 
   * @param url The full PostgreSQL URL
   * @return Name of the database
   * @author Dylan Hansen
   */
  protected String getDatabaseNameFromURL(String url)
  {
    String dbName = null;

    // If the url has parameters, there will be a "?"
    if (url.indexOf("?") > -1)
      // There are parameters, ignore them
      dbName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
    else
      dbName = url.substring(url.lastIndexOf("/") + 1, url.length());

    return dbName;
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#fetchDump(org.objectweb.cjdbc.controller.backup.DumpTransferInfo,
   *      java.lang.String, java.lang.String)
   */
  public void fetchDump(DumpTransferInfo dumpTransferInfo, String path,
      String dumpName) throws BackupException
  {
    // TODO: Auto-generated method stub
    // getBackupManager().fetchDump(this, backuperServerAddress, sessionKey,
    // path, dumpName);
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#setupServer()
   */
  public DumpTransferInfo setupServer()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#getBackupManager()
   */
  public BackupManager getBackupManager()
  {
    // TODO: Auto-generated method stub
    return null;
    // return BackupManager.TheOneAndOnlyBackupManager;
  }
}
