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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): 
 */

package org.objectweb.cjdbc.scenario.standalone.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.common.xml.XmlValidator;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;

/**
 * Test to validate xml document of c-jdbc DTDs.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class XmlParserTest extends NoTemplate
{
  /** File name containing the C-JDBC URLs to test. */
  public static final String XML_FILE = "xmls.txt";
  /** The absolute path to the above xmlFile */
  public static String       xmlFiles;

  private int                count    = 0;
  private int                failed   = 0;

  /**
   * Initialize the xml parser
   */
  public void setUp()
  {
    xmlFiles = getTextPath(XML_FILE);
  }

  private String getRelativePath(String pathFromTxt)
  {
    return getUserDir() + File.separator + pathFromTxt;
  }

  /**
   * Use the file <code>XML_FILES</code> to verify XML document
   * 
   * @throws IOException when fails reading
   */
  public void testXmlDocuments() throws IOException
  {
    File file = new File(xmlFiles);
    BufferedReader d = new BufferedReader(new InputStreamReader(
        new FileInputStream(file)));
    String line;
    boolean allResultsOk = true;
    while ((line = d.readLine()) != null)
    {
      if (line.startsWith("###") || line.trim().equals(""))
        continue;
      else
      {
        StringTokenizer st = new StringTokenizer(line, ";");
        String xmlFile = getRelativePath(st.nextToken());
        String dtdFile = getRelativePath(st.nextToken());
        boolean valid = new Boolean(st.nextToken()).booleanValue();

        File xfile = new File(xmlFile);
        boolean value = false;
        if (xfile.isDirectory())
        {
          value = analyseDirectoryWithDtd(dtdFile, xfile);
        }
        else
        {
          value = analyseXmlAndDtd(dtdFile, xmlFile);
        }
	System.out.print("File '" + xmlFile + "': ");
	System.out.println((valid == value) ? "ok" : "ko");
        allResultsOk = allResultsOk && (valid == value);
      }
    }

    System.out.println("SUMMARY: " + count + " files tested,  " + failed
        + " failed.");
    assertTrue(failed + " tests failed", allResultsOk);
  }

  private boolean analyseDirectoryWithDtd(String pathToDtd, File dir)
  {
    System.out.println("Analysing directory:" + dir.getAbsolutePath() + ":");
    String[] toAnalyse = dir.list(new FilenameFilter()
    {
      /**
       * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
       */
      public boolean accept(File dir, String name)
      {
        if (name.endsWith(".xml"))
          return true;
        else
          return false;
      }
    });

    boolean allResultsOk = true;
    boolean test = true;

    for (int i = 0; i < toAnalyse.length; i++)
    {
      try
      {
        test = analyseXmlAndDtd(pathToDtd, dir.getAbsolutePath()
            + File.separator + toAnalyse[i]);
      }
      catch (Exception e)
      {
        test = false;
      }
      allResultsOk = allResultsOk && test;

    }

    return allResultsOk;
  }

  private boolean analyseXmlAndDtd(String pathToDtd, String pathToXml)
      throws IOException
  {

    File dtd = new File(pathToDtd);
    File file = new File(pathToXml);
    System.out.print("Analysing file[" + (count++) + "]\t:");
    FileReader reader = new FileReader(file);

    XmlValidator validator = new XmlValidator(pathToDtd, reader);
    if (!validator.isDtdValid())
      System.out.print("[FAILED: dtd is not valid]");
    else if (!validator.isXmlValid())
      System.out.print("[FAILED: xml is not valid]");
    else if (validator.isXmlValid())
      System.out.print("[OK]");

    System.out.println("\t:" + file.getName() + " with dtd:" + dtd.getName());

    if (validator.getLastException() != null)
    {
      ArrayList errors = validator.getExceptions();
      for (int i = 0; i < errors.size(); i++)
        System.out.println("\t(parsing error):"
            + ((Exception) errors.get(i)).getMessage());
    }

    if (!validator.isValid())
      failed++;

    return validator.isValid();
  }
}
