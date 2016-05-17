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

package org.objectweb.cjdbc.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Defines the SplitXml Ant target used to prepare the C-JDBC scripts
 * generation.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class SplitXmlTask extends Task
{
  private String xmlFilePath;
  private String outputDir;
  private String attributeName;
  private String startTagName;
  private String endTagName;

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException
  {
    try
    {
      BufferedReader reader = new BufferedReader(new FileReader(xmlFilePath));
      String lineBuffer;
      while ((lineBuffer = reader.readLine()) != null)
      {
        if (lineBuffer.indexOf(startTagName) != -1)
        {
          //System.out.println(lineBuffer);
          int index = lineBuffer.indexOf(attributeName)
              + attributeName.length() + 2;
          String fileName = lineBuffer.substring(index, lineBuffer.indexOf(
              '\"', index));
          BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir
              + File.separator + fileName + ".xml"));
          writer.write(lineBuffer + System.getProperty("line.separator"));
          while ((lineBuffer = reader.readLine()) != null
              && lineBuffer.indexOf(endTagName) == -1)
          {
            writer.write(lineBuffer + System.getProperty("line.separator"));
          }
          if (lineBuffer != null) // append last line
            writer.write(lineBuffer + System.getProperty("line.separator"));
          writer.flush();
          writer.close();
          continue;
        }
      }
    }
    catch (Exception e)
    {
      throw new BuildException(e.getMessage());
    }
  }

  /**
   * Set the path to the xml path containing the scripts definition.
   * 
   * @param xmlFilePath path to the xml file
   */
  public void setScriptXmlFile(String xmlFilePath)
  {
    this.xmlFilePath = xmlFilePath;
  }

  /**
   * Specify the output directory.
   * 
   * @param outputDirPath the path to the directory
   */
  public void setOutputDir(String outputDirPath)
  {
    this.outputDir = outputDirPath;
    File newDir = new File(outputDir);
    newDir.mkdirs();
  }

  /**
   * Set parsing tag name.
   * 
   * @param tagName the tag name
   */
  public void setParsingTagName(String tagName)
  {
    this.startTagName = "<" + tagName + " ";
    this.endTagName = "</" + tagName + ">";
  }

  /**
   * Set the attribute that contains the name of the file.
   * 
   * @param attributeName the name of the attribute to get the name of the file
   *          to write
   */
  public void setOuputFileAttribute(String attributeName)
  {
    this.attributeName = attributeName;
  }
}