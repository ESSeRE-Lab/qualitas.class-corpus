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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.backup;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.objectweb.cjdbc.common.exceptions.BackupException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.util.FileManagement;
import org.objectweb.cjdbc.common.util.Zipper;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;

/**
 * This class defines a Backuper for Apache Derby databases.
 * <p>
 * Supported URLs are jdbc:derby:PathToDerbyDatabase[;options]
 * <p>
 * The Backuper itself does not take any option. It simply dumps the Derby
 * directory into a zip file.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class DerbyEmbeddedBackuper implements Backuper
{
  static Trace logger = Trace.getLogger(DerbyEmbeddedBackuper.class.getName());

  /**
   * Creates a new <code>DerbyEmbeddedBackuper</code> object
   */
  public DerbyEmbeddedBackuper()
  {
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#getDumpFormat()
   */
  public String getDumpFormat()
  {
    return "Derby embedded compressed dump";
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#getOptions()
   */
  public String getOptions()
  {
    return null;
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#setOptions(java.lang.String)
   */
  public void setOptions(String options)
  {
    // Ignored, no options
  }

  /**
   * @see org.objectweb.cjdbc.controller.backup.Backuper#backup(org.objectweb.cjdbc.controller.backend.DatabaseBackend,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String, java.util.ArrayList)
   */
  public Date backup(DatabaseBackend backend, String login, String password,
      String dumpName, String path, ArrayList tables) throws BackupException
  {
    String derbyPath = getDerbyPath(backend, true);

    try
    {
      File pathDir = new File(path);
      if (!pathDir.exists())
      {
        pathDir.mkdirs();
        pathDir.mkdir();
      }

      if (logger.isDebugEnabled())
        logger.debug("Archiving " + derbyPath + " in " + path + File.separator
            + dumpName + Zipper.ZIP_EXT);

      Zipper.zip(getDumpPhysicalPath(path, dumpName), derbyPath,
          Zipper.STORE_PATH_FROM_ZIP_ROOT);
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
   */
  public void restore(DatabaseBackend backend, String login, String password,
      String dumpName, String path, ArrayList tables) throws BackupException
  {
    String derbyPath = getDerbyPath(backend, false);

    File derbyDir = new File(derbyPath);

    // First delete any existing directory
    if (FileManagement.deleteDir(derbyDir))
      logger.info("Existing Derby directory " + derbyPath
          + " has been deleted.");

    // Now create the dir
    derbyDir.mkdirs();
    derbyDir.mkdir();

    // Unzip the dump
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Uncompressing dump");
      Zipper.unzip(getDumpPhysicalPath(path, dumpName), derbyPath);
    }
    catch (Exception e)
    {
      String msg = "Error while uncompressing dump";
      logger.error(msg, e);
      throw new BackupException(msg, e);
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
   * @return path to zip file
   */
  private String getDumpPhysicalPath(String path, String dumpName)
  {
    return path + File.separator + dumpName + Zipper.ZIP_EXT;
  }

  /**
   * Extract the path where the Derby database is stored by parsing the backend
   * JDBC URL.
   * 
   * @param backend the Derby backend
   * @param checkPath if true we check if the path is a valid directory
   * @return path to the Derby database
   * @throws BackupException if the URL is not valid or the path not valid
   */
  private String getDerbyPath(DatabaseBackend backend, boolean checkPath)
      throws BackupException
  {
    String url = backend.getURL();
    if (!url.startsWith("jdbc:derby:"))
      throw new BackupException("Unsupported url " + url
          + " expecting jdbc:derby:pathToDb");

    // Strip 'jdbc:derby:'
    // 11 = "jdbc:derby:".length()
    String derbyPath = url.substring(11);
    // Remove all options that are after the first semicolon
    int semicolon = derbyPath.indexOf(';');
    if (semicolon > -1)
      derbyPath = derbyPath.substring(0, semicolon);

    if (checkPath)
    {
      File checkDerbyPath = new File(derbyPath);
      if (!checkDerbyPath.isDirectory())
        throw new BackupException(
            "Directory "
                + derbyPath
                + " does not exist. This might be due to an unsupported URL format (expectin jdbc:derby:pathToDb)");
    }

    return derbyPath;
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
