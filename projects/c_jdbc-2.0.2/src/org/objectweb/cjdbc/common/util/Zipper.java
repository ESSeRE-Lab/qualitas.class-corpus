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
 * Initial developer(s): Emmanuel Cecchet
 * Contributor(s): Nicolas Modrzyk
 */

package org.objectweb.cjdbc.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;

/**
 * Zip utility class to compress a directory into a single zip file and
 * vice-versa.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet</a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class Zipper
{
  /** Extension for zipped file names */
  public static final String ZIP_EXT                    = ".zip";

  private static final int   BUFFER_SIZE                = 2048;

  /** Store full path in zip when archiving */
  public static final int    STORE_FULL_PATH_IN_ZIP     = 0;
  /** Store only file names in zip when archiving */
  public static final int    STORE_NAME_ONLY_IN_ZIP     = 1;
  /** Store relative path in zip when archiving */
  public static final int    STORE_RELATIVE_PATH_IN_ZIP = 2;
  /** Store path relative to root directory in zip when archiving */
  public static final int    STORE_PATH_FROM_ZIP_ROOT   = 3;

  static Trace               logger                     = Trace
                                                            .getLogger(Zipper.class
                                                                .getName());

  /**
   * Create a zip file from directory
   * 
   * @param zipName name of the file to create
   * @param rootDir root directory to archive
   * @param storePolicy the store policy to use (STORE_FULL_PATH_IN_ZIP,
   *          STORE_NAME_ONLY_IN_ZIP, STORE_RELATIVE_PATH_IN_ZIP or
   *          STORE_PATH_FROM_ZIP_ROOT)
   * @throws Exception if fails
   */
  public static void zip(String zipName, String rootDir, int storePolicy)
      throws Exception
  {
    if (zipName == null || rootDir == null)
      throw new Exception("Invalid arguments to create zip file");
    try
    {
      FileOutputStream fos = new FileOutputStream(zipName);
      ZipOutputStream zos = new ZipOutputStream(fos);

      directoryWalker(rootDir, rootDir, zos, storePolicy);

      zos.flush();
      zos.finish();
      zos.close();
      fos.close();
    }
    catch (IOException e)
    {
      logger.error(e.getMessage());
      throw e;
    }
  }

  /**
   * Expand the content of the zip file
   * 
   * @param zipName of the file to expand
   * @param targetDir where to place unzipped files
   * @throws Exception if fails
   */
  public static void unzip(String zipName, String targetDir) throws Exception
  {
    File ftargetDir = new File(targetDir);
    if (!ftargetDir.exists())
      ftargetDir.mkdirs();
    if (!ftargetDir.exists())
      throw new Exception(Translate.get("zip.invalid.target.directory"));

    File fzipname = new File(zipName);
    if (!fzipname.exists())
      throw new Exception(Translate.get("zip.invalid.source.file", zipName));

    try
    {
      FileInputStream fis = new FileInputStream(fzipname);
      ZipInputStream zis = new ZipInputStream(fis);

      ZipEntry entry;

      byte[] data = new byte[BUFFER_SIZE];
      while ((entry = zis.getNextEntry()) != null)
      {
        int count;
        String target = targetDir + File.separator + entry.getName();
        File fget = new File(target);
        fget.mkdirs(); // create needed new directories
        fget.delete(); // delete directory but not parents
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("zip.extracting", new String[]{
              String.valueOf(entry), fget.getAbsolutePath()}));
        FileOutputStream fos = new FileOutputStream(target);

        BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);
        while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1)
        {
          dest.write(data, 0, count);
        }
        dest.flush();
        dest.close();
      }
      zis.close();
    }
    catch (Exception e)
    {
      logger.error("Error while uncompressing archive", e);
      throw e;
    }
  }

  /**
   * Walk through currentDir and recursively in its subdirectories. Each file
   * found is zipped.
   * 
   * @param currentDir directory to walk through
   * @param rootDir root directory for path references
   * @param zos ZipOutputSteam to write to
   * @param storePolicy file path storing policy
   * @throws IOException if an error occurs
   */
  private static void directoryWalker(String currentDir, String rootDir,
      ZipOutputStream zos, int storePolicy) throws IOException
  {
    File dirObj = new File(currentDir);
    if (dirObj.exists() == true)
    {
      if (dirObj.isDirectory() == true)
      {

        File[] fileList = dirObj.listFiles();

        for (int i = 0; i < fileList.length; i++)
        {
          if (fileList[i].isDirectory())
          {
            directoryWalker(fileList[i].getPath(), rootDir, zos, storePolicy);
          }
          else if (fileList[i].isFile())
          {
            zipFile(fileList[i].getPath(), zos, storePolicy, rootDir);
          }
        }
      }
      else
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("zip.not.directory", rootDir));
      }
    }
    else
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("zip.directory.not.found", rootDir));
    }
  }

  /**
   * TODO: zipFunc definition.
   * 
   * @param filePath file to compress
   * @param zos ZipOutputSteam to write to
   * @param storePolicy file path storing policy
   * @param rootDir root directory for path references
   * @throws IOException if an error occurs
   */
  private static void zipFile(String filePath, ZipOutputStream zos,
      int storePolicy, String rootDir) throws IOException
  {
    File ffilePath = new File(filePath);
    String path = "";
    switch (storePolicy)
    {
      case STORE_FULL_PATH_IN_ZIP :
        path = ffilePath.getAbsolutePath();
        break;
      case STORE_NAME_ONLY_IN_ZIP :
        ffilePath.getName();
        break;
      case STORE_RELATIVE_PATH_IN_ZIP :
        File f = new File("");
        String pathToHere = f.getAbsolutePath();
        path = ffilePath.getAbsolutePath();
        path = path.substring(path.indexOf(pathToHere + File.separator)
            + pathToHere.length());
        break;
      case STORE_PATH_FROM_ZIP_ROOT :
        path = ffilePath.getAbsolutePath();
        String tmpDir = rootDir + File.separator;
        // Strip rootdir from absolute path
        path = path.substring(path.indexOf(tmpDir) + tmpDir.length());
        break;
      default :
        break;
    }

    if (logger.isDebugEnabled())
      logger
          .debug(Translate.get("zip.archiving", new String[]{filePath, path}));

    FileInputStream fileStream = new FileInputStream(filePath);
    BufferedInputStream bis = new BufferedInputStream(fileStream);

    ZipEntry fileEntry = new ZipEntry(path);
    zos.putNextEntry(fileEntry);

    byte[] data = new byte[BUFFER_SIZE];
    int byteCount;
    while ((byteCount = bis.read(data, 0, BUFFER_SIZE)) > -1)
      zos.write(data, 0, byteCount);
  }

}