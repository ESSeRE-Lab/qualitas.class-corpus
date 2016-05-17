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
import java.util.ArrayList;
import java.util.Date;

import org.objectweb.cjdbc.common.exceptions.BackupException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;

/**
 * This class defines a Backuper for PostgreSQL databases. This backuper makes
 * dumps in a plain-text format with a .sql extension.
 * <p>
 * Supported URLs are jdbc:postgresql://host:port/dbname?param1=foo,param2=bar
 * <p>
 * Currently the Backuper only takes 1 parameter, the encoding of the database
 * that is created upon restore. More options can be easily added. This class
 * makes calls to the pg_dump, createdb, dropdb and psql commands.
 * 
 * @author <a href="mailto:dhansen@h2st.com">Dylan Hansen</a>
 * @version 1.1
 */
public class PostgreSQLPlainTextBackuper extends AbstractPostgreSQLBackuper
{
  // Logger
  static Trace    logger        = Trace.getLogger(PostgreSQLPlainTextBackuper.class
                                    .getName());

  /**
   * Creates a new <code>PostgreSQLPlainTextBackuper</code> object
   * 
   * @author Dylan Hansen
   */
  public PostgreSQLPlainTextBackuper()
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#getDumpFormat()
   * @author Dylan Hansen
   */
  public String getDumpFormat()
  {
    return "PostgreSQL Plain Text Dump";
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#backup(org.objectweb.cjdbc.controller.backend.DatabaseBackend,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String, java.util.ArrayList)
   * @author Dylan Hansen
   */
  public Date backup(DatabaseBackend backend, String login, String password,
      String dumpName, String path, ArrayList tables) throws BackupException
  {

    // Parse the URL for the connection information
    String url = backend.getURL();
    String host = getHostFromURL(url);
    String port = getPortFromURL(url);
    String dbName = getDatabaseNameFromURL(url);

    if (logger.isDebugEnabled())
      logger.debug("Backing up database '" + dbName + "' on host '" + host
          + ":" + port + "'");

    try
    {
      // Create the path, if it does not already exist
      File pathDir = new File(path);
      if (!pathDir.exists())
      {
        pathDir.mkdirs();
        pathDir.mkdir();
      }

      String fullPath = getDumpPhysicalPath(path, dumpName) + ".sql";

      // Execute the pg_dump command
      Runtime runtime = Runtime.getRuntime();
      String[] cmd = {"pg_dump", "-f", fullPath, "-h", host, "-p", port, "-U",
          login, dbName};

      Process process = runtime.exec(cmd);
      process.waitFor();
      if (process.exitValue() != 0)
        throw new BackupException(
            "pg_dump execution did not complete successfully!");
    }
    catch (Exception e)
    {
      String msg = "Error while performing backup";
      logger.error(msg, e);
      throw new BackupException(msg, e);
    }

    return new Date(System.currentTimeMillis());
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#restore(org.objectweb.cjdbc.controller.backend.DatabaseBackend,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String, java.util.ArrayList)
   * @author Dylan Hansen
   */
  public void restore(DatabaseBackend backend, String login, String password,
      String dumpName, String path, ArrayList tables) throws BackupException
  {
    // Parse the URL for the connection information
    String url = backend.getURL();
    String host = getHostFromURL(url);
    String port = getPortFromURL(url);
    String dbName = getDatabaseNameFromURL(url);

    if (logger.isDebugEnabled())
      logger.debug("Restoring database '" + dbName + "' on host '" + host + ":"
          + port + "'");

    // Check to see if the given path + dumpName exists
    String fullPath = getDumpPhysicalPath(path, dumpName) + ".sql";
    File dump = new File(fullPath);
    if (!dump.exists())
      throw new BackupException("Backup '" + fullPath + "' does not exist!");

    try
    {
      Runtime runtime = Runtime.getRuntime();
      Process process = null;

      if (logger.isDebugEnabled())
        logger.debug("Dropping database '" + dbName + "'...");

      // Drop the database if it already exists
      String[] dropCmd = {"dropdb", "-h", host, "-p", port, "-U", login, dbName};
      process = runtime.exec(dropCmd);
      process.waitFor();
      if (process.exitValue() != 0)
        throw new BackupException(
            "dropdb execution did not complete successfully!");

      // Re-create the database, use the specified encoding if provided
      if (optionsMap.containsKey("encoding"))
      {
        String encoding = (String) optionsMap.get("encoding");

        if (logger.isDebugEnabled())
          logger.debug("Creating databse '" + dbName + "' with encoding '"
              + encoding + "'");

        String[] createCmd = {"createdb", "-h", host, "-p", port, "-U", login,
            "--encoding=" + encoding, dbName};
        process = runtime.exec(createCmd);
        process.waitFor();
        if (process.exitValue() != 0)
          throw new BackupException(
              "createdb execution did not complete successfully!");
      }
      else
      {
        if (logger.isDebugEnabled())
          logger.debug("Creating databse '" + dbName + "'");

        String[] createCmd = {"createdb", "-h", host, "-p", port, "-U", login,
            dbName};
        process = runtime.exec(createCmd);
        process.waitFor();
        if (process.exitValue() != 0)
          throw new BackupException(
              "createdb execution did not complete successfully!");
      }

      if (logger.isDebugEnabled())
        logger
            .debug("Rebuilding '" + dbName + "' from dump '" + dumpName + "'");

      // Replay the dump to the new database
      String[] replayCmd = {"psql", "-h", host, "-p", port, "-U", login, "-f", fullPath, dbName};
      process = runtime.exec(replayCmd);
      process.waitFor();
      if (process.exitValue() != 0)
        throw new BackupException(
            "psql execution did not complete successfully!");
    }
    catch (Exception e)
    {
      String msg = "Error while performing backup";
      logger.error(msg, e);
      throw new BackupException(msg, e);
    }
  }
}
