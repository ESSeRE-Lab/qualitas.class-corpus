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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;

/**
 * This class defines a XmlTools
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public final class XmlTools
{

  /** Logger instance. */
  static Trace                      logger = Trace.getLogger(XmlTools.class
                                               .getName());

  /** XSL Transformation */
  private static TransformerFactory tFactory;
  private static Transformer        infoTransformer;

  /**
   * Indent xml with xslt
   * 
   * @param xml to indent
   * @return indented xml
   * @throws Exception if an error occurs
   */
  public static String prettyXml(String xml) throws Exception
  {
    return applyXsl(xml, "c-jdbc-pretty.xsl");
  }

  /**
   * Apply xslt to xml
   * 
   * @param xml to transform
   * @param xsl transformation to apply
   * @return xml formatted string or error message
   */
  public static String applyXsl(String xml, String xsl)
  {
    try
    {
      StringWriter result = new StringWriter();
      if (tFactory == null)
        tFactory = TransformerFactory.newInstance();
      File infoXsl = internationalizeXsl(new File(URLDecoder
          .decode(XmlTools.class.getResource("/" + xsl).getFile())));
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("controller.xml.use.xsl", infoXsl));
      //if(infoTransformer==null)
      infoTransformer = tFactory.newTransformer(new StreamSource(infoXsl));
      infoTransformer.transform(new StreamSource(new StringReader(xml)),
          new StreamResult(result));
      return result.toString();
    }
    catch (Exception e)
    {
      String msg = Translate.get("controller.xml.transformation.failed", e);

      if (logger.isDebugEnabled())
        logger.debug(msg, e);
      logger.error(msg);
      return msg;
    }
  }

  /**
   * Transform the xsl file so that it is internationalized.
   * 
   * @param xsl xsl file
   * @return internationalized file
   * @throws Exception if an error occurs
   */
  public static File internationalizeXsl(File xsl) throws Exception
  {
    int point = xsl.getAbsolutePath().lastIndexOf('.');
    String xslPath = xsl.getAbsolutePath();
    xslPath = xslPath.substring(0, point) + "_" + Locale.getDefault()
        + xslPath.substring(point);
    File i18nXsl = new File(xslPath);
    if (i18nXsl.exists() == false)
    {
      ResourceBundle rb = ResourceBundle.getBundle("c-jdbc-xsl");
      BufferedReader br = new BufferedReader(new FileReader(xsl));
      String xml = "";
      String oi18n = "<i18n>";
      String ci18n = "</i18n>";
      int oi18nl = oi18n.length();
      int ci18nl = oi18nl + 1;
      StringBuffer buffer = new StringBuffer();
      String i18n = "";
      while ((xml = br.readLine()) != null)
      {
        int indexOpen = 0, indexClose = 0;
        while ((indexOpen = xml.indexOf(oi18n)) != -1)
        {
          indexClose = xml.indexOf(ci18n);
          i18n = xml.substring(indexOpen + oi18nl, indexClose).trim();
          try
          {
            i18n = rb.getString(i18n);
          }
          catch (Exception ignore)
          // if the key has no match return the key itself
          {
          }
          xml = xml.substring(0, indexOpen) + i18n
              + xml.substring(indexClose + ci18nl);
        }
        buffer.append(xml + System.getProperty("line.separator"));
      }
      BufferedWriter bw = new BufferedWriter(new FileWriter(i18nXsl));
      bw.write(buffer.toString());
      bw.flush();
      bw.close();
    }
    return i18nXsl;
  }

  /**
   * Insert C-JDBC DOCTYPE in a XML file.
   * Ugly hack: the DOCTYPE is inserted this way since the DOCTYPE
   * is stripped from the xml when applying the pretty xsl stylesheet
   * and I could not find a way to access it from within the xsl.
   * Any suggestion is welcome...
   * 
   * Insert C-JDBC DOCTYPE after the &lt;?xml ... ?&gt; and before the
   * rest of the content.
   * 
   * @param xml XML content
   * 
   * @return the xml where the C-JDBC DOCTYPE has been inserted so that the xml
   * can be validated against this DTD
   * 
   * @see XmlComponent.DOCTYPE_DB
   */
  public static String insertCjdbcDoctype(String xml)
  {
    int index = xml.indexOf("?>");
    if (index < 0) 
    {
      return xml;
    }
    String xmlWithDoctype = xml.substring(0, index+2);
    xmlWithDoctype += "\n";
    xmlWithDoctype += XmlComponent.DOCTYPE_DB;
    xmlWithDoctype += "\n";
    xmlWithDoctype += xml.substring(index+3, xml.length());
    return xmlWithDoctype;
  }
  
  /**
   * Insert C-JDBC-CONTROLLER DOCTYPE in a XML file.
   * Ugly hack: the DOCTYPE is inserted this way since the DOCTYPE
   * is stripped from the xml when applying the pretty xsl stylesheet
   * and I could not find a way to access it from within the xsl.
   * Any suggestion is welcome...
   * 
   * Insert C-JDBC-CONTROLLER DOCTYPE after the &lt;?xml ... ?&gt; and before the
   * rest of the content.
   * 
   * @param xml XML content
   * 
   * @return the xml where the C-JDBC-CONTROLLER DOCTYPE has been inserted so that the xml
   * can be validated against this DTD
   * 
   * @see XmlComponent.DOCTYPE_CONTROLLER
   */
  public static String insertCjdbcControllerDoctype(String xml)
  {
    int index = xml.indexOf("?>");
    if (index < 0) 
    {
      return xml;
    }
    String xmlWithDoctype = xml.substring(0, index+2);
    xmlWithDoctype += "\n";
    xmlWithDoctype += XmlComponent.DOCTYPE_CONTROLLER;
    xmlWithDoctype += "\n";
    xmlWithDoctype += xml.substring(index+3, xml.length());
    return xmlWithDoctype;
  }
}