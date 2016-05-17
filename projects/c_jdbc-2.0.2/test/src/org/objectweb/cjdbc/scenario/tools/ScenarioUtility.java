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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.objectweb.cjdbc.driver.Blob;

/**
 * This class defines a ScenarioUtility
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ScenarioUtility
{

  /**
   * Get the result as an arraylist of executing a sql query on a connection
   * 
   * @param query sql query
   * @param con connection to use to execute the query
   * @param prepare if set to true, use preparedStatement instead of statement
   * @return <code>ArrayList</code> of the converted <code>ResultSet</code>
   * @throws Exception if fails
   */
  public static ArrayList getSingleQueryResult(String query, Connection con,
      boolean prepare) throws Exception
  {
    if (prepare)
      return convertResultSet(con.prepareStatement(query).executeQuery());
    else
      return convertResultSet(con.createStatement().executeQuery(query));
  }

  /**
   * Get the result as an arraylist of executing a sql query on a connection
   * 
   * @param query sql query
   * @param con connection to use to execute the query
   * @return <code>ArrayList</code> of the converted <code>ResultSet</code>
   * @throws Exception if fails
   */
  public static ArrayList getSingleQueryResult(String query, Connection con)
      throws Exception
  {
    return getSingleQueryResult(query, con, false);
  }

  /**
   * @see #getSingleQueryResult(String, Connection)
   */
  public static void displaySingleQueryResult(String query, Connection con)
      throws Exception
  {
    displayResultOnScreen(getSingleQueryResult(query, con));
  }

  /**
   * @see #getSingleQueryResult(String, Connection)
   */
  public static void displaySingleQueryResult(String query, Connection con,
      boolean prepare) throws Exception
  {
    displayResultOnScreen(getSingleQueryResult(query, con, prepare));
  }

  /**
   * Format a result and display it on the screen
   * 
   * @param result a converted resultset
   */
  public static void displayResultOnScreen(ArrayList result)
  {
    int size = result.size();
    ArrayList list;
    for (int i = 0; i < size; i++)
    {
      list = (ArrayList) result.get(i);
      System.out.println("row[" + i + "]:" + list);
    }
  }

  /**
   * Format a result and display it on the screen
   * 
   * @param set a ResultSet
   * @throws Exception if an error occurs
   */
  public static void displayResultOnScreen(ResultSet set) throws Exception
  {
    displayResultOnScreen(convertResultSet(set));
  }

  /**
   * Converts the result set to an array list so can be display
   * 
   * @param set the result set with data
   * @return <code>ArrayList</code> of data
   * @throws Exception if result set is not valid
   */
  public static final ArrayList convertResultSet(ResultSet set)
      throws Exception
  {
    ArrayList list = new ArrayList();
    int colCount = set.getMetaData().getColumnCount();
    while (set.next())
    {
      ArrayList row = new ArrayList();
      for (int i = 1; i <= colCount; i++)
      {
        row.add(set.getString(i));
      }
      list.add(row);
    }
    //System.out.println(list);
    return list;
  }

  /**
   * ReadBinary data from the file. Tested ok with writeBinary param file
   * destination target
   * 
   * @param file file to read from
   * @return file content as an array of byte
   * @exception IOException if read fails from target
   */
  public static byte[] readBinary(File file) throws IOException
  {
    long len = file.length();
    FileInputStream fis = new FileInputStream(file);
    byte[] data = new byte[(int) len];
    DataInputStream bais = new DataInputStream(fis);
    bais.read(data);
    return data;
  }

  /**
   * Creates a new blob from a given file
   * 
   * @param storeFile the path to the file to use
   * @return <code>Blob</code> which content is that of the file
   * @throws Exception if fails or if cannot find file
   */
  public static Blob createBlob(String storeFile) throws Exception
  {
    File fis = new File(storeFile);
    if (!fis.exists())
      fis = new File(new ScenarioUtility().getClass().getResource(storeFile)
          .getFile());
    if (!fis.exists())
      throw new FileNotFoundException();
    Blob bob = new org.objectweb.cjdbc.driver.Blob(ScenarioUtility
        .readBinary(fis));
    return bob;
  }

  /**
   * WriteBinary data to the file. Tested ok with readBinary
   * 
   * @param data to be written to the file
   * @param file destination target
   * @exception IOException if write fails on target
   */
  public static void writeBinary(byte[] data, File file) throws IOException
  {
    FileOutputStream fos = new FileOutputStream(file);
    DataOutputStream baos = new DataOutputStream(fos);
    baos.write(data);
  }

  /**
   * Checks if two result sets have the same content
   * 
   * @param rs1 an open result set
   * @param rs2 an open result set
   * @return <tt>true</tt> if the two results sets have the same data(does not
   *         checks for metadata)
   */
  public static boolean checkEquals(ResultSet rs1, ResultSet rs2)
  {
    try
    {
      ArrayList list1 = ScenarioUtility.convertResultSet(rs1);
      //System.out.println(list1);
      ArrayList list2 = ScenarioUtility.convertResultSet(rs2);
      //System.out.println(list2);
      if (list1.size() != list2.size())
        return false;

      Object o1;
      Object o2;
      for (int i = 0; i < list1.size(); i++)
      {
        ArrayList list11 = (ArrayList) list1.get(i);
        ArrayList list22 = (ArrayList) list2.get(i);
        if (list11.size() != list22.size())
          return false;
        for (int j = 0; j < list11.size(); j++)
        {
          o1 = list11.get(j);
          o2 = list22.get(j);
          if (o1 == null && o2 == null)
            continue;
          else if (o1 == null || o2 == null)
            return false;
          if (o1.equals(o2))
            continue;
          else
            return false;
        }
      }
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Completely deletes a directory
   * 
   * @param dir to delete
   * @return true if it was successfully deleted
   */
  public static boolean deleteDir(File dir)
  {
    // to see if this directory is actually a symbolic link to a directory,
    // we want to get its canonical path - that is, we follow the link to
    // the file it's actually linked to
    File candir;
    try
    {
      candir = dir.getCanonicalFile();
    }
    catch (IOException e)
    {
      return false;
    }

    // a symbolic link has a different canonical path than its actual path,
    // unless it's a link to itself
    if (!candir.equals(dir.getAbsoluteFile()))
    {
      // this file is a symbolic link, and there's no reason for us to
      // follow it, because then we might be deleting something outside of
      // the directory we were told to delete
      return false;
    }

    // now we go through all of the files and subdirectories in the
    // directory and delete them one by one
    File[] files = candir.listFiles();
    if (files != null)
    {
      for (int i = 0; i < files.length; i++)
      {
        File file = files[i];

        // in case this directory is actually a symbolic link, or it's
        // empty, we want to try to delete the link before we try
        // anything
        boolean deleted = file.delete();
        if (!deleted)
        {
          // deleting the file failed, so maybe it's a non-empty
          // directory
          if (file.isDirectory())
            deleteDir(file);

          // otherwise, there's nothing else we can do
        }
      }
    }

    // now that we tried to clear the directory out, we can try to delete it
    // again
    return dir.delete();
  }
}