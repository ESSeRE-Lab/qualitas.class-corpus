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
 * Contributor(s): Nicolas Modrzyk, Mathieu Peltier.
 */

package org.objectweb.cjdbc.controller.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.util.Constants;
import org.objectweb.cjdbc.common.util.ReadWrite;
import org.objectweb.cjdbc.common.xml.ControllerXmlTags;
import org.objectweb.cjdbc.common.xml.XmlTools;

/**
 * Class to create report from Controller
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 */
public class ReportManager
{

  /** Logger instance. */
  static Trace             logger             = Trace
                                                  .getLogger(ReportManager.class
                                                      .getName());

  /** Settings */
  boolean                  reportEnabled      = false;
  boolean                  hideSensitiveData  = true;
  boolean                  generateOnShutdown = true;
  boolean                  generateOnFatal    = true;
  boolean                  enableFileLogging  = true;
  boolean                  showLogsOnly       = false;
  String                   reportLocation     = ControllerConstants.REPORT_LOCATION;

  private Controller       controller;
  private StringBuffer     buffer;
  private FileOutputStream fos;

  /**
   * Call above and write controller xml information and information
   * 
   * @param controller to report
   */
  public ReportManager(Controller controller)
  {
    this.controller = controller;
    buffer = new StringBuffer();
    //listLoggers();
  }

  /**
   * Starts generating the report. Effectively write the java properties,
   * controller settings, controller info but do not write the logs yet.
   */
  public void startReport()
  {
    writeTitle("CJDBC (version:" + Constants.VERSION + ") REPORT generated on "
        + new Date().toString());
    writeJavaProperties();
    writeControllerSettings();
    writeControllerInfo();
  }

  /**
   * Creates a new <code>ReportManager.java</code> object Report only logs
   * 
   * @param controller the controller to report logs from
   * @param showLogsOnly show logs
   */
  public ReportManager(Controller controller, boolean showLogsOnly)
  {
    this(controller);
    this.showLogsOnly = showLogsOnly;
  }

  /**
   * Call above and write about the exception
   * 
   * @param controller to report
   * @param e exception
   */
  public ReportManager(Controller controller, Exception e)
  {
    this(controller);
    writeException(e);
  }

  /**
   * Write Controller info as return by <code>getInformation()</code>
   */
  public void writeControllerInfo()
  {
    try
    {
      writeHeader("CONTROLLER INFO XML");
      write(controller.getXml());
      writeHeader("DATABASE INFO");
      write(XmlTools
          .applyXsl(controller.getXmlVirtualDatabases(), "c-jdbc.xsl"));
      writeHeader("DATABASE INFO XML");
      write(XmlTools.prettyXml(controller.getXmlVirtualDatabases()));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /** Write all parameters from <code>ControllerFactory</code> */
  public void writeControllerSettings()
  {
    writeHeader("CONTROLLER SETTINGS");
    write(controller.getConfiguration());
  }

  /** Write All Java Properties */
  public void writeJavaProperties()
  {
    writeHeader("JAVA SETTINGS");
    write(System.getProperties());
  }

  /** Write Logs */
  public void writeLogs()
  {
    writeHeader("LOG CONFIGURATION");
    String s = this.getClass().getResource(ControllerConstants.LOG4J_RESOURCE)
        .getFile();
    writeFile(s);
    writeHeader("LOGS");
    if (isEnableFileLogging())
    {
      Logger log = Logger.getRootLogger();
      FileAppender appender = (FileAppender) log.getAppender("Filetrace");
      s = appender.getFile();
      writeFile(s);
    }
  }

  /**
   * Write Details of the exception
   * 
   * @param e exception to write
   */
  public void writeException(Exception e)
  {
    writeHeader("EXCEPTION DESCRIPTION");
    write(e.getClass().toString());
    write(e.getMessage());
    write(e.toString());
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    write(sw.toString());
  }

  /**
   * Flush and close
   * 
   * @return report content
   */
  public String generate()
  {
    // Here we get all the logs before writing report
    writeLogs();
    try
    {
      File reportFile = new File(reportLocation + File.separator
          + ControllerConstants.REPORT_FILE);
      reportFile.getParentFile().mkdirs();
      fos = new FileOutputStream(reportFile);
      fos.write(buffer.toString().getBytes());
      fos.close();
      String returned = buffer.toString();
      // Reset buffer
      buffer.delete(0, buffer.length());
      return returned;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return "";
    }
  }

  /**
   * Get a trace of the logs only
   * 
   * @return <code>String<code> content of the log4j output
   */
  public String generateJustLogs()
  {
    String s = this.getClass().getResource(ControllerConstants.LOG4J_RESOURCE)
        .getFile();
    Logger log = Logger.getRootLogger();
    try
    {
      FileAppender appender = (FileAppender) log.getAppender("Filetrace");
      s = appender.getFile();
      writeFile(s);
      return buffer.toString();
    }
    catch (Exception e)
    {
      logger
          .warn("Filetrace appender is not of type FileAppender and cannot be dumped ("
              + e + ")");
      return "";
    }
  }

  /* Write Methods */

  private void write(String string)
  {
    try
    {
      buffer.append(string.toString());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void writeFile(String filename)
  {
    try
    {
      File f = new File(filename);
      FileInputStream fis = new FileInputStream(f);
      byte[] logs = new byte[(int) f.length()];
      fis.read(logs);
      write(new String(logs));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void write(Hashtable table)
  {
    buffer.append(ReadWrite.write(table, true));
  }

  private void writeTitle(String title)
  {
    write("==========================================================================="
        + System.getProperty("line.separator"));
    write("==========================================================================="
        + System.getProperty("line.separator"));
    write("==== " + title + System.getProperty("line.separator"));
    write("==========================================================================="
        + System.getProperty("line.separator"));
    write("==========================================================================="
        + System.getProperty("line.separator"));
  }

  private void writeHeader(String header)
  {
    write(System.getProperty("line.separator"));
    write("############################################################"
        + System.getProperty("line.separator"));
    write("####\t\t" + header + System.getProperty("line.separator"));
    write("############################################################"
        + System.getProperty("line.separator"));
  }

  private void setLogsDeleteOnExit()
  {
    try
    {
      Logger log = Logger.getRootLogger();
      RollingFileAppender appender = (RollingFileAppender) log
          .getAppender("Filetrace");
      File logFile = new File(appender.getFile());
      logFile.deleteOnExit();
    }
    catch (Exception e)
    {
      // appender has been removed or is not defined.
      logger.debug("Failed to set deleteOnExit on log file", e);
    }
  }

  /**
   * @param settings hashtable of settings
   */
  public final void setSettings(Hashtable settings)
  {
    //listLoggers();
    if (settings == null)
    {
      reportEnabled = false;
    }
    else if (settings.containsKey(ControllerXmlTags.ATT_REPORT_ENABLED))
    {
      reportEnabled = new Boolean((String) settings
          .get(ControllerXmlTags.ATT_REPORT_ENABLED)).booleanValue();
    }
    if (!reportEnabled)
    {
      hideSensitiveData = false;
      generateOnShutdown = false;
      generateOnFatal = false;
      enableFileLogging = false;
      //removeFileTraceAppender();
      return;
    }
    else
    {
      if ("true".equals(settings
          .get(ControllerXmlTags.ATT_REPORT_DELETE_ON_SHUTDOWN)))
      {
        setLogsDeleteOnExit();
      }
      if (settings
          .containsKey(ControllerXmlTags.ATT_REPORT_ENABLE_FILE_LOGGING))
      {
        enableFileLogging = new Boolean((String) settings
            .get(ControllerXmlTags.ATT_REPORT_ENABLE_FILE_LOGGING))
            .booleanValue();
        if (!enableFileLogging)
        {
          //removeFileTraceAppender();
        }
      }
      if (settings.containsKey(ControllerXmlTags.ATT_REPORT_GENERATE_ON_FATAL))
      {
        generateOnFatal = new Boolean((String) settings
            .get(ControllerXmlTags.ATT_REPORT_GENERATE_ON_FATAL))
            .booleanValue();
      }
      if (settings
          .containsKey(ControllerXmlTags.ATT_REPORT_GENERATE_ON_SHUTDOWN))
      {
        generateOnShutdown = new Boolean((String) settings
            .get(ControllerXmlTags.ATT_REPORT_GENERATE_ON_SHUTDOWN))
            .booleanValue();
      }
      if (settings
          .containsKey(ControllerXmlTags.ATT_REPORT_HIDE_SENSITIVE_DATA))
      {
        hideSensitiveData = new Boolean((String) settings
            .get(ControllerXmlTags.ATT_REPORT_HIDE_SENSITIVE_DATA))
            .booleanValue();
      }
      if (settings.containsKey(ControllerXmlTags.ATT_REPORT_REPORT_LOCATION))
      {
        reportLocation = (String) settings
            .get(ControllerXmlTags.ATT_REPORT_REPORT_LOCATION);
      }
    }
  }

  void listLoggers()
  {
    Logger log = Logger.getRootLogger();
    if (!log.isDebugEnabled())
      return;
    Enumeration loggers = Logger.getDefaultHierarchy().getCurrentLoggers();
    while (loggers.hasMoreElements())
    {
      Logger l = (Logger) loggers.nextElement();
      log.debug("Found logger:" + l.getName());
    }
  }

  /**
   * @return Returns the enableFileLogging.
   */
  public boolean isEnableFileLogging()
  {
    return enableFileLogging;
  }

  /**
   * @return Returns the generateOnFatal.
   */
  public boolean isGenerateOnFatal()
  {
    return reportEnabled && generateOnFatal;
  }

  /**
   * @return Returns the generateOnShutdown.
   */
  public boolean isGenerateOnShutdown()
  {
    return reportEnabled && generateOnShutdown;
  }

  /**
   * @return Returns the hideSensitiveData.
   */
  public boolean isHideSensitiveData()
  {
    return hideSensitiveData;
  }

  /**
   * @return Returns the reportEnabled.
   */
  public boolean isReportEnabled()
  {
    return reportEnabled;
  }

  /**
   * @return Returns the reportLocation.
   */
  public String getReportLocation()
  {
    return reportLocation;
  }

  /**
   * Sets the enableFileLogging value.
   * 
   * @param enableFileLogging The enableFileLogging to set.
   */
  public void setEnableFileLogging(boolean enableFileLogging)
  {
    this.enableFileLogging = enableFileLogging;
  }

  /**
   * Sets the generateOnFatal value.
   * 
   * @param generateOnFatal The generateOnFatal to set.
   */
  public void setGenerateOnFatal(boolean generateOnFatal)
  {
    this.generateOnFatal = generateOnFatal;
  }

  /**
   * Sets the generateOnShutdown value.
   * 
   * @param generateOnShutdown The generateOnShutdown to set.
   */
  public void setGenerateOnShutdown(boolean generateOnShutdown)
  {
    this.generateOnShutdown = generateOnShutdown;
  }

  /**
   * Sets the hideSensitiveData value.
   * 
   * @param hideSensitiveData The hideSensitiveData to set.
   */
  public void setHideSensitiveData(boolean hideSensitiveData)
  {
    this.hideSensitiveData = hideSensitiveData;
  }

  /**
   * Sets the reportEnabled value.
   * 
   * @param reportEnabled The reportEnabled to set.
   */
  public void setReportEnabled(boolean reportEnabled)
  {
    this.reportEnabled = reportEnabled;
  }

  /**
   * Sets the reportLocation value.
   * 
   * @param reportLocation The reportLocation to set.
   */
  public void setReportLocation(String reportLocation)
  {
    this.reportLocation = reportLocation;
  }
}